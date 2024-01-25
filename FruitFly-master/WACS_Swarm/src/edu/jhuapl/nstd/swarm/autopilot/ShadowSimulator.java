package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.CommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.TelemetryMessage;
import edu.jhuapl.nstd.swarm.util.Config;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ShadowSimulator
{
    static final long TELEMETRY_UPDATE_RATE_HZ = 8;
    static final long TELEMETRY_UPDATE_PERIOD_MS = 1000 / TELEMETRY_UPDATE_RATE_HZ;
    static final long POSITION_TELEMETRY_UPDATE_RATE_HZ = 2;
    static final long POSITION_TELEMETRY_UPDATE_PERIOD_MS = 1000 / POSITION_TELEMETRY_UPDATE_RATE_HZ;
    static final long COMMAND_UPDATE_RATE_HZ = 2;
    static final long COMMAND_UPDATE_PERIOD_MS = 1000 / COMMAND_UPDATE_RATE_HZ;
    static final long INTERNAL_BIDIRECTIONAL_LATENCY_MS = 60;
    static final long EXPECTED_COMMAND_RECEIVE_RATE_HZ = 8;
    static final long EXPECTED_COMMAND_RECEIVE_PERIOD_MS = 1000 / EXPECTED_COMMAND_RECEIVE_RATE_HZ;

    protected final Object m_commandMessageLock = new Object();
    protected final Object m_telemetryMessageLock = new Object();
    protected final Object m_positionLock = new Object();
    protected final int INCOMING_LATENCY_QUEUE_SIZE = 20;
    protected Queue<CommandMessage> m_incomingLatencyQueue = new LinkedBlockingQueue<CommandMessage>(INCOMING_LATENCY_QUEUE_SIZE);
    protected final int OUTGOING_LATENCY_QUEUE_SIZE = 20;
    protected Queue<TelemetryMessage> m_outgoingLatencyQueue = new LinkedBlockingQueue<TelemetryMessage>(OUTGOING_LATENCY_QUEUE_SIZE);

    protected CommandMessage m_commandMessage = null;
    protected TelemetryMessage m_telemetryMessage;

    protected int m_receivePort;
    protected String m_remoteHostname;
    protected int m_remotePort;
    protected float m_sendRate_hz;
    protected long m_sendPeriod_ms;

    protected double m_updateRateHz;
    protected long m_updatePeriod_ms;
    protected double m_minAirspeed_mps;
    protected double m_maxAirspeed_mps;
    protected double m_accelerationRate_mps2;
    protected double m_decelerationRate_mps2;
    protected double m_maxRoll_rad;
    protected double m_maxRollVelocity_rad_per_sec;
    protected double m_rollAcceleration_rad_per_sec2;
    protected double m_rollDecelerationStartRatio;
    protected double m_rollErrorIntegratorCoeff;
    protected double m_minAltitudeMSL_m;
    protected double m_maxAltitudeMSL_m;
    protected double m_ascendRate_mps;
    protected double m_descendRate_mps;
    protected double m_initialLatitude_rad;
    protected double m_initialLongitude_rad;
    protected double m_initialAltitudeMSL_m;
    protected long m_lastUpdateTime_ms;
    protected double m_windSpeed_mps;
    protected double m_windBlowingtoDirection_rad;

    protected double m_airSpeed_mps;
    protected double m_groundSpeed_mps;
    protected double m_groundDisplacement_m;
    protected double m_groundDisplacementDirection_rad;
    protected double m_heading_rad;
    protected double m_roll_rad;
    protected double m_rollVelocity_rad_per_sec;
    protected Latitude m_latitude;
    protected Longitude m_longitude;
    protected double m_altitudeMSL_m;

    protected double m_commandedAltitudeMSL_m;
    protected double m_commandedRoll_rad;
    protected double m_commandedAirspeed_mps;

    protected long m_lastPrintTime_ms = 0;
    protected long m_lastCommandCopyTime_ms = 0;
    protected long m_lastTelemetryUpdateTime_ms = 0;
    protected long m_lastPositionTelemetryUpdateTime_ms = 0;

    protected boolean m_enablePiccoloSim;
    protected double m_piccoloSimAltitudeOffset_m;
    protected String m_piccoloSimComPortName;    
    protected int m_piccoloSimSerialBaudRate;
    PiccoloSimThread m_PicSimThread = null;

    protected ShadowSimulatorForm m_form = null;

    protected boolean m_useAPIOnly;




    final static protected double KNOTS_TO_MPS = 0.514444;
    final static protected double FT_TO_METERS = 0.3048;
    final static protected double GRAVITY_ACCEL_MPS2 = 9.807;

    public ShadowSimulator()
    {
        this(false);
    }

    public ShadowSimulator(final boolean useAPIOnly)
    {
        m_useAPIOnly = useAPIOnly;
        m_telemetryMessage = getNewTelemetryMessage();

        if (!useAPIOnly)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    m_form = new ShadowSimulatorForm();
                    m_form.setVisible(true);
                }
            });
        }

        m_enablePiccoloSim = Config.getConfig().getPropertyAsBoolean("ShadowSimulator.enablePiccoloSim");
        m_piccoloSimAltitudeOffset_m = Config.getConfig().getPropertyAsDouble("ShadowSimulator.piccoloSimAltitudeOffset_m");
        m_piccoloSimComPortName = Config.getConfig().getProperty("ShadowSimulator.piccoloSimOutputSerialPort");
        m_piccoloSimSerialBaudRate = Config.getConfig().getPropertyAsInteger("ShadowSimulator.piccoloSimSerialBaudRate");
        m_updateRateHz = Config.getConfig().getPropertyAsDouble("ShadowSimulator.updateRateHz");
        m_updatePeriod_ms = (long)(1000 / m_updateRateHz);
        m_minAirspeed_mps = KNOTS_TO_MPS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.minAirspeed_knots");
        m_maxAirspeed_mps = KNOTS_TO_MPS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.maxAirspeed_knots");
        m_accelerationRate_mps2 = KNOTS_TO_MPS * Config.getConfig().getPropertyAsDouble("Shadowsimulator.accelerationRate_knots_per_sec");
        m_decelerationRate_mps2 = KNOTS_TO_MPS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.decelerationRate_knots_per_sec");
        m_maxRoll_rad = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.maxRoll_deg"));
        m_maxRollVelocity_rad_per_sec = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.maxRollVelocity_deg_per_sec"));
        m_rollAcceleration_rad_per_sec2 = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.rollAcceleration_deg_per_sec2"));
        m_rollDecelerationStartRatio = Config.getConfig().getPropertyAsDouble("ShadowSimulator.rollDecelerationStartRatio");
        m_minAltitudeMSL_m = FT_TO_METERS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.minAltitudeMSL_ft");
        m_maxAltitudeMSL_m = FT_TO_METERS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.maxAltitudeMSL_ft");
        m_ascendRate_mps = FT_TO_METERS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.ascendRate_ft_per_min") / 60.0;
        m_descendRate_mps = FT_TO_METERS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.descendRate_ft_per_min") / 60.0;
        m_initialLatitude_rad = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.initialLatitude_deg"));
        m_initialLongitude_rad = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.initialLongitude_deg"));
        m_initialAltitudeMSL_m = FT_TO_METERS * Config.getConfig().getPropertyAsDouble("ShadowSimulator.initialAltitudeMSL_ft");
        m_windSpeed_mps = Config.getConfig().getPropertyAsDouble("ShadowSimulator.windSpeed_mps");
        m_windBlowingtoDirection_rad = Math.toRadians(Config.getConfig().getPropertyAsDouble("ShadowSimulator.windBlowingToDirection_deg"));

        m_lastUpdateTime_ms = System.currentTimeMillis();

        m_airSpeed_mps = 0;
        m_groundSpeed_mps = 0;
        m_groundDisplacement_m = 0;
        m_groundDisplacementDirection_rad = 0;
        m_heading_rad = 0;
        m_roll_rad = 0;
        m_rollVelocity_rad_per_sec = 0;
        m_latitude = new Latitude(m_initialLatitude_rad, Angle.RADIANS);
        m_longitude = new Longitude(m_initialLongitude_rad, Angle.RADIANS);
        m_altitudeMSL_m = m_initialAltitudeMSL_m;


        m_receivePort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.localPort");
        m_remotePort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.remotePort");
        m_remoteHostname = Config.getConfig().getProperty("ShadowSimulator.remoteHostname");
        m_sendRate_hz = Config.getConfig().getPropertyAsInteger("ShadowSimulator.sendRateHz");
        m_sendPeriod_ms = (long)(1000 / m_sendRate_hz);
        
        m_commandedAltitudeMSL_m = 0;
        m_commandedRoll_rad = 0;
        m_commandedAirspeed_mps = 0;

        if (m_enablePiccoloSim)
        {
            try
            {                
                m_PicSimThread = new PiccoloSimThread(m_piccoloSimComPortName, m_piccoloSimSerialBaudRate, m_piccoloSimAltitudeOffset_m);
                m_PicSimThread.setDaemon(true);
                m_PicSimThread.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void manuallySetPlanePosition(final LatLonAltPosition currentPosition, final double currentHeading_rad, final double currentSpeed_mps)
    {
        synchronized (m_positionLock)
        {
            m_latitude = currentPosition.getLatitude();
            m_longitude = currentPosition.getLongitude();
            m_altitudeMSL_m = currentPosition.getAltitude().getDoubleValue(Length.METERS);
            m_heading_rad = currentHeading_rad;
            m_airSpeed_mps = currentSpeed_mps;
        }
    }

    public void manuallySetCommandMessage(final CommandMessage commandMessage)
    {
        synchronized (m_commandMessageLock)
        {
            m_commandMessage = commandMessage;
        }
    }

    public void manuallySetWindSpeed(final double windSpeed_mps, final double windBlowingToDirection_rad)
    {
        synchronized (m_commandMessageLock)
        {
            m_windSpeed_mps = windSpeed_mps;
            m_windBlowingtoDirection_rad = windBlowingToDirection_rad;
        }
    }

    public TelemetryMessage getLatestTelemetry()
    {
        synchronized (m_telemetryMessageLock)
        {
            return m_telemetryMessage;
        }
    }

    public void run()
    {
        try
        {        
            while (true)
            {
                long startTime_ms = System.currentTimeMillis();
                double timeSinceLastUpdate_sec = ((double)(startTime_ms - m_lastUpdateTime_ms)) / 1000.0;
                m_lastUpdateTime_ms = startTime_ms;

                updateSimulation(timeSinceLastUpdate_sec);


                if (m_form != null)
                {
                    m_form.setPlaneData(m_roll_rad,
                                        m_altitudeMSL_m,
                                        m_heading_rad,
                                        m_latitude.getDoubleValue(Angle.RADIANS),
                                        m_longitude.getDoubleValue(Angle.RADIANS),
                                        m_airSpeed_mps,
                                        m_commandedRoll_rad,
                                        m_commandedAltitudeMSL_m,
                                        m_commandedAirspeed_mps);
                }
                if (m_enablePiccoloSim && m_PicSimThread != null)
                {
                    m_PicSimThread.setTelemetry (m_latitude.getDoubleValue(Angle.DEGREES), m_longitude.getDoubleValue(Angle.DEGREES), m_altitudeMSL_m, m_heading_rad, 0, m_roll_rad, m_windSpeed_mps, m_airSpeed_mps, m_windBlowingtoDirection_rad);
                }

                if (System.currentTimeMillis() - m_lastPrintTime_ms > 250)
                {
                    /*System.out.printf("Heading (deg): %f\n", Math.toDegrees(m_heading_rad));
                    System.out.printf("Roll (deg): %f    (%f)\n", Math.toDegrees(m_roll_rad), Math.toDegrees(m_commandedRoll_rad));
                    System.out.printf("Altitude MSL (m): %f    (%f)\n\n", m_altitudeMSL_m, m_commandedAltitudeMSL_m);*/

                    m_lastPrintTime_ms = System.currentTimeMillis();
                }


                
                long loopDuration_ms = System.currentTimeMillis() - startTime_ms;


                if (loopDuration_ms < m_updatePeriod_ms)
                {
                    Thread.sleep(m_updatePeriod_ms - loopDuration_ms);
                }
                else
                {
                    Thread.sleep(1);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected abstract void fillTelemetryMessageDetails (TelemetryMessage telemetryMessage);

    public void updateSimulation(final double timeSinceLastUpdate_sec)
    {
        //
        // Calculate ground speed
        //
        if (m_commandedAirspeed_mps == 0)
        {
            m_groundSpeed_mps = 0;
        }
        else
        {
            double planeAirDeltaX_m = m_airSpeed_mps * Math.sin(m_heading_rad) * timeSinceLastUpdate_sec;
            double planeAirDeltaY_m = m_airSpeed_mps * Math.cos(m_heading_rad) * timeSinceLastUpdate_sec;
            double windDeltaX_m;
            double windDeltaY_m;
            synchronized (m_commandMessageLock)
            {
                windDeltaX_m = m_windSpeed_mps * Math.sin(m_windBlowingtoDirection_rad) * timeSinceLastUpdate_sec;
                windDeltaY_m = m_windSpeed_mps * Math.cos(m_windBlowingtoDirection_rad) * timeSinceLastUpdate_sec;
            }
            double planeGroundDeltaX_m = planeAirDeltaX_m + windDeltaX_m;
            double planeGroundDeltaY_m = planeAirDeltaY_m + windDeltaY_m;
            if (planeGroundDeltaY_m == 0 && planeGroundDeltaX_m == 0)
            {
                m_groundDisplacementDirection_rad = 0;
            }
            else if (planeGroundDeltaY_m == 0 && planeGroundDeltaX_m > 0)
            {
                m_groundDisplacementDirection_rad = Math.PI / 2;
            }
            else if (planeGroundDeltaY_m == 0 && planeGroundDeltaX_m < 0)
            {
                m_groundDisplacementDirection_rad = Math.PI * 3 / 2;
            }
            else
            {
                m_groundDisplacementDirection_rad = Math.atan(Math.abs(planeGroundDeltaX_m) / Math.abs(planeGroundDeltaY_m));

                // Shift to correct quadrant
                if (planeGroundDeltaX_m >= 0 && planeGroundDeltaY_m < 0)
                {
                    m_groundDisplacementDirection_rad = Math.PI - m_groundDisplacementDirection_rad;
                }
                else if(planeGroundDeltaX_m < 0 && planeGroundDeltaY_m < 0)
                {
                    m_groundDisplacementDirection_rad = Math.PI + m_groundDisplacementDirection_rad;
                }
                else if(planeGroundDeltaX_m <= 0 && planeGroundDeltaY_m > 0)
                {
                    m_groundDisplacementDirection_rad = (Math.PI * 2) - m_groundDisplacementDirection_rad;
                }
            }

            m_groundDisplacement_m = Math.sqrt(planeGroundDeltaX_m * planeGroundDeltaX_m + planeGroundDeltaY_m * planeGroundDeltaY_m);
            m_groundSpeed_mps = m_groundDisplacement_m / timeSinceLastUpdate_sec;
        }

        //
        // Calculate new latitude and longitude
        //
        LatLonAltPosition planePosition = new LatLonAltPosition(m_latitude, m_longitude, Altitude.ZERO);
        planePosition = planePosition.translatedBy(new RangeBearingHeightOffset(new Length(m_groundSpeed_mps * timeSinceLastUpdate_sec, Length.METERS),
                                                                                //new NavyAngle(m_heading_rad, Angle.RADIANS),
                                                                                new NavyAngle(m_groundDisplacementDirection_rad, Angle.RADIANS),
                                                                                Length.ZERO)).asLatLonAltPosition();
        m_latitude = planePosition.getLatitude();
        m_longitude = planePosition.getLongitude();


        double turnRate_rad_per_sec;
        if (m_airSpeed_mps == 0)
        {
            turnRate_rad_per_sec = 0;
        }
        else
        {
            double airSpeed_mps_X = m_airSpeed_mps * Math.sin(m_heading_rad);
            double airSpeed_mps_Y = m_airSpeed_mps * Math.cos(m_heading_rad);
            double windSpeed_mps_X = m_windSpeed_mps * Math.sin(m_windBlowingtoDirection_rad);
            double windSpeed_mps_Y = m_windSpeed_mps * Math.cos(m_windBlowingtoDirection_rad);

            double gndSpeed_mps_X = airSpeed_mps_X + windSpeed_mps_X;
            double gndSpeed_mps_Y = airSpeed_mps_Y + windSpeed_mps_Y;
            double gndSpeed_mps = Math.sqrt(gndSpeed_mps_X*gndSpeed_mps_X+gndSpeed_mps_Y*gndSpeed_mps_Y);

            //turnRate_rad_per_sec =  (GRAVITY_ACCEL_MPS2 * Math.tan(m_roll_rad)) / gndSpeed_mps;
            turnRate_rad_per_sec =  (GRAVITY_ACCEL_MPS2 * Math.tan(m_roll_rad)) / m_airSpeed_mps;
        }
        m_heading_rad += turnRate_rad_per_sec * timeSinceLastUpdate_sec;
        if (m_heading_rad < 0)
        {
            m_heading_rad += Math.PI * 2;
        }
        else if (m_heading_rad > (Math.PI * 2))
        {
            m_heading_rad %= (Math.PI * 2);
        }

        //
        // Copy latest command
        //
        if ((System.currentTimeMillis() - m_lastCommandCopyTime_ms) > COMMAND_UPDATE_PERIOD_MS)
        {
            m_lastCommandCopyTime_ms = System.currentTimeMillis();

            synchronized (m_commandMessageLock)
            {
                if (m_commandMessage != null)
                {
                    m_commandedAltitudeMSL_m = m_commandMessage.altitudeCommand_m;
                    m_commandedRoll_rad = m_commandMessage.rollCommand_rad;
                    m_commandedAirspeed_mps = m_commandMessage.airspeedCommand_mps;
                }
            }
        }

        //
        // Enforce command limits
        //
        if (m_commandedAltitudeMSL_m == 0)
        {
            m_commandedAltitudeMSL_m = m_altitudeMSL_m;
        }
        else if(m_commandedAltitudeMSL_m < m_minAltitudeMSL_m)
        {
            m_commandedAltitudeMSL_m = m_minAltitudeMSL_m;
        }
        else if (m_commandedAltitudeMSL_m > m_maxAltitudeMSL_m)
        {
            m_commandedAltitudeMSL_m = m_maxAltitudeMSL_m;
        }
        if (m_commandedRoll_rad <  -m_maxRoll_rad)
        {
            m_commandedRoll_rad = -m_maxRoll_rad;
        }
        else if (m_commandedRoll_rad > m_maxRoll_rad)
        {
            m_commandedRoll_rad = m_maxRoll_rad;
        }
        if (m_commandedAirspeed_mps < m_minAirspeed_mps && m_commandedAirspeed_mps != 0)
        {
            m_commandedAirspeed_mps = m_minAirspeed_mps;
        }
        else if (m_commandedAirspeed_mps > m_maxAirspeed_mps)
        {
            m_commandedAirspeed_mps = m_maxAirspeed_mps;
        }


        double airspeedDiscrepancy_mps = m_commandedAirspeed_mps - m_airSpeed_mps;
        double airspeedDelta_mps;
        if (airspeedDiscrepancy_mps > 0)
        {
            airspeedDelta_mps = m_accelerationRate_mps2 * timeSinceLastUpdate_sec;
            if (airspeedDelta_mps > airspeedDiscrepancy_mps)
            {
                airspeedDelta_mps = airspeedDiscrepancy_mps;
            }
        }
        else
        {
            airspeedDelta_mps = -m_decelerationRate_mps2 * timeSinceLastUpdate_sec;
            if (airspeedDelta_mps < airspeedDiscrepancy_mps)
            {
                airspeedDelta_mps = airspeedDiscrepancy_mps;
            }
        }
        m_airSpeed_mps += airspeedDelta_mps;


        double rollDelta_rad = m_rollVelocity_rad_per_sec * timeSinceLastUpdate_sec;

        double rollError_rad = m_roll_rad - m_commandedRoll_rad;
        double absoluteRollError_rad = Math.abs(rollError_rad);
        final double accelDivisor = 1;
        if (rollError_rad != 0)
        {
            if (m_rollDecelerationStartRatio > Math.abs(m_rollVelocity_rad_per_sec / rollError_rad)) //Accelerate towards target
            {
                if (rollError_rad > 0)
                {
                    m_rollVelocity_rad_per_sec -= m_rollAcceleration_rad_per_sec2 * (absoluteRollError_rad / accelDivisor) * timeSinceLastUpdate_sec;

                    if (m_rollVelocity_rad_per_sec < -m_maxRollVelocity_rad_per_sec)
                    {
                        m_rollVelocity_rad_per_sec = -m_maxRollVelocity_rad_per_sec;
                    }
                }
                else
                {
                    m_rollVelocity_rad_per_sec += m_rollAcceleration_rad_per_sec2 * (absoluteRollError_rad / accelDivisor) * timeSinceLastUpdate_sec;

                    if (m_rollVelocity_rad_per_sec > m_maxRollVelocity_rad_per_sec)
                    {
                        m_rollVelocity_rad_per_sec = m_maxRollVelocity_rad_per_sec;
                    }
                }
            }
            else //Start decelerating
            {
                if (Math.abs(m_rollVelocity_rad_per_sec) > (m_rollAcceleration_rad_per_sec2 * timeSinceLastUpdate_sec))
                {
                    if (m_rollVelocity_rad_per_sec < 0)
                    {
                        m_rollVelocity_rad_per_sec += m_rollAcceleration_rad_per_sec2 * timeSinceLastUpdate_sec;
                    }
                    else
                    {
                        m_rollVelocity_rad_per_sec -= m_rollAcceleration_rad_per_sec2 * timeSinceLastUpdate_sec;
                    }
                }
                else
                {
                    m_rollVelocity_rad_per_sec = 0;
                }
            }
        }

        m_roll_rad += rollDelta_rad;


        double altitudeDiscrepancy_m = m_commandedAltitudeMSL_m - m_altitudeMSL_m;
        double altitudeDelta_m;
        if (altitudeDiscrepancy_m > 0)
        {
            altitudeDelta_m = m_ascendRate_mps * timeSinceLastUpdate_sec;
            if (altitudeDelta_m > altitudeDiscrepancy_m)
            {
                altitudeDelta_m = altitudeDiscrepancy_m;
            }
        }
        else
        {
            altitudeDelta_m = -m_descendRate_mps * timeSinceLastUpdate_sec;
            if (altitudeDelta_m < altitudeDiscrepancy_m)
            {
                altitudeDelta_m = altitudeDiscrepancy_m;
            }
        }
        m_altitudeMSL_m += altitudeDelta_m;
        
        //
        // Update telemetry message with latest data
        //
        if ((System.currentTimeMillis() - m_lastTelemetryUpdateTime_ms) > TELEMETRY_UPDATE_PERIOD_MS)
        {
            m_lastTelemetryUpdateTime_ms = System.currentTimeMillis();

            TelemetryMessage newTelemetryMessage = getNewTelemetryMessage();
            newTelemetryMessage.timestamp_ms = System.currentTimeMillis();

            if ((System.currentTimeMillis() - m_lastPositionTelemetryUpdateTime_ms) > POSITION_TELEMETRY_UPDATE_PERIOD_MS)
            {
                m_lastPositionTelemetryUpdateTime_ms = System.currentTimeMillis();

                newTelemetryMessage.longitude_rad = m_longitude.getDoubleValue(Angle.RADIANS);
                newTelemetryMessage.latitude_rad = m_latitude.getDoubleValue(Angle.RADIANS);
                //newTelemetryMessage.gpsAltitude_m = m_altitudeMSL_m;
                newTelemetryMessage.groundSpeedNorth_mps = m_groundSpeed_mps * Math.cos(m_groundDisplacementDirection_rad);
                newTelemetryMessage.groundSpeedEast_mps = m_groundSpeed_mps * Math.sin(m_groundDisplacementDirection_rad);
            }
            else
            {
                newTelemetryMessage.longitude_rad = m_telemetryMessage.longitude_rad;
                newTelemetryMessage.latitude_rad = m_telemetryMessage.latitude_rad;
                //newTelemetryMessage.gpsAltitude_m = m_telemetryMessage.gpsAltitude_m;
                newTelemetryMessage.groundSpeedNorth_mps = m_telemetryMessage.groundSpeedNorth_mps;
                newTelemetryMessage.groundSpeedEast_mps = m_telemetryMessage.groundSpeedEast_mps;
            }

            newTelemetryMessage.trueHeading_rad = m_heading_rad;
            newTelemetryMessage.roll_rad = m_roll_rad;
            newTelemetryMessage.barometricAltitudeMsl_m = m_altitudeMSL_m;
            newTelemetryMessage.indicatedAirspeed_mps = m_airSpeed_mps;
            newTelemetryMessage.trueAirspeed_mps = m_airSpeed_mps;
            
            fillTelemetryMessageDetails (newTelemetryMessage);

            synchronized (m_telemetryMessageLock)
            {
                if (m_useAPIOnly)
                {
                    m_telemetryMessage = newTelemetryMessage;
                }
                else
                {
                    m_outgoingLatencyQueue.add(newTelemetryMessage);
                }
            }
        }
    }
    
    protected abstract TelemetryMessage getNewTelemetryMessage ();

    static protected int calcChecksum(final byte[] data, final int dataSize)
    {
        int checksum = 0;
        for (int i = 0; i < dataSize; ++i)
        {
            checksum += data[i] & 0xFF;
        }

        return checksum;
    }


    protected class LatencyEmulationThread extends Thread
    {
        public LatencyEmulationThread ()
        {
            this.setName ("WACS-ShadowSimLatencyEmulationThread");
        }
        
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    long startTime_ms = System.currentTimeMillis();

                    synchronized (m_commandMessageLock)
                    {
                        while (m_incomingLatencyQueue.size() > 0 && (System.currentTimeMillis() - m_incomingLatencyQueue.peek().timestamp_ms) >= INTERNAL_BIDIRECTIONAL_LATENCY_MS)
                        {
                            m_commandMessage = m_incomingLatencyQueue.remove();
                        }
                    }

                    synchronized (m_telemetryMessageLock)
                    {
                        while (m_outgoingLatencyQueue.size() > 0 && (System.currentTimeMillis() - m_outgoingLatencyQueue.peek().timestamp_ms) >= INTERNAL_BIDIRECTIONAL_LATENCY_MS)
                        {
                            m_telemetryMessage = m_outgoingLatencyQueue.remove();
                        }
                    }

                    long loopDuration_ms = System.currentTimeMillis() - startTime_ms;

                    if (loopDuration_ms < 10)
                    {
                        Thread.sleep(10 - loopDuration_ms);
                    }
                    else
                    {
                        Thread.sleep(1);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
