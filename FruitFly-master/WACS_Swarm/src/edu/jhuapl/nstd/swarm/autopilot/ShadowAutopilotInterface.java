package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;


public class ShadowAutopilotInterface extends Thread implements KnobsAutopilotInterface
{
    static public class ShadowCommandMessage extends CommandMessage
    {
        public ShadowCommandMessage()
        {
            messageLength = 48;
            messageVersion = 1;
            throttleEnable = 0;   //0-not enabled
        }
        
        @Override
        public void populateSpecificDetails(TelemetryMessage telemetryMessageBase, BeliefManager belMgr) 
        {
            ShadowTelemetryMessage telemetryMessage = (ShadowTelemetryMessage) telemetryMessageBase;
            
            busNumber = telemetryMessage.busNumber;
            knobsModeReport = telemetryMessage.knobsModeCommand;
        }

        //public long timestamp_ms;
        public int messageLength;
        public int messageVersion;
        public int busNumber;
        public int knobsModeReport;          //0-internal  1-external
        //public double altitudeCommand_m;   //-609.6m(-2000ft) to 4876.8(16000ft)
        //public double rollCommand_rad;     //-0.3491(-20deg) to 0.3491(20deg)
        //public double airspeedCommand_mps; //33.4389(65knots) to 56.5889(110knots)
        public int throttlePercentageCommand; //0 to 100. Only valid if knobsModeReport is external and throttleEnable is enabled
        public int throttleEnable;           //0-not enabled  1-enabled
    }

    static public class ShadowTelemetryMessage extends TelemetryMessage
    {
        public ShadowTelemetryMessage()
        {
            messageLength = 172;
            messageVersion = 0;
            busNumber = 0;
        }

        //public long timestamp_ms;
        public int messageLength;
        public int messageVersion;
        public int busNumber;
        public int knobsModeCommand;   //0-internal  1-external
        public int insMode;            //0-init  1-align w/GPS  2-full INS(air/gnd)  3-INS no GPS(air)  4-dead reckoning  5-INS no GPS(gnd)  6-align w/o GPS
        //public double longitude_rad;
        //public double latitude_rad;
        //public double groundSpeedNorth_mps;
        //public double groundSpeedEast_mps;
        //public double trueHeading_rad;
        public double magneticDeclination_rad;
        //public double roll_rad;
        //public double pitch_rad;
        public double gpsAltitude_m;
        //public double barometricAltitude_m;
        public double verticalSpeed_mps;
        public double verticalAcceleration_mps2;
        //public double indicatedAirspeed_mps;
        //public double trueAirspeed_mps;
        public double airTemp_k;
        public double gpsPositionalDilutionOfPrecision;
        public double fuelLevel_l;
        public double gpsTimeOfWeek_s;
        public int gpsWeekNumber;
        public int flapsPosition;      //0-Up(no flaps)  1-mid(partial flaps)  2-down(full flaps)
    }
    
    private class LogThread extends Thread
    {
        final private int LOOP_RATE_HZ = 40;
        final private long LOOP_DURATION_MS = 1000 / 40;
        
        ShadowCommandMessage localCommandMessage = new ShadowCommandMessage();
        ShadowTelemetryMessage localTelemetryMessage = new ShadowTelemetryMessage();

        public LogThread ()
        {
            this.setName ("WACS-ShadowLogThread");
        }
        
        @Override
        public void run()
        {
            try
            {

                long logStartTime_ms = -1;

                long loopStartTime_ms;
                while (true)
                {
                    synchronized (m_lock)
                    {
                        copyLatestCommandMessage(localCommandMessage);
                        copyLatestTelemetryMessage(localTelemetryMessage);
                    }
                    
                    if (localCommandMessage != null && localTelemetryMessage != null)
                    {
                        loopStartTime_ms = System.currentTimeMillis();

                        if (logStartTime_ms < 0)
                        {
                            logStartTime_ms = loopStartTime_ms;
                            File logFolder = new File ("ShadowLogs");
                            if (!logFolder.exists())
                                logFolder.mkdirs();
                            
                            m_logFile = new FileWriter("ShadowLogs/Shadow4DTInterface_" + logStartTime_ms + ".csv");
                            m_logFile.write("Time (sec), Commanded Roll (deg), Reported Roll (deg), Commanded Altitude MSL (m), Reported Altitude MSL (m), Latitude (deg), Longitude (deg)\n");
                        }

                        m_logFile.write(Float.toString((System.currentTimeMillis() - logStartTime_ms) / 1000.0f));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(Math.toDegrees(localCommandMessage.rollCommand_rad)));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(Math.toDegrees(localTelemetryMessage.roll_rad)));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(localCommandMessage.altitudeCommand_m));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(localTelemetryMessage.barometricAltitudeMsl_m));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(Math.toDegrees(localTelemetryMessage.latitude_rad)));
                        m_logFile.write(",");
                        m_logFile.write(Double.toString(Math.toDegrees(localTelemetryMessage.longitude_rad)));
                        m_logFile.write("\n");

                        long loopEndTime_ms = System.currentTimeMillis();
                        long loopDuration_ms = loopEndTime_ms - loopStartTime_ms;

                        if (loopDuration_ms > LOOP_DURATION_MS)
                        {
                            Thread.sleep(1);
                        }
                        else
                        {
                            Thread.sleep(LOOP_DURATION_MS - loopDuration_ms);
                        }                            
                    }
                    
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class ReceiveThread extends Thread
    {
        public ReceiveThread ()
        {
            this.setName ("WACS-ShadowReceiveThread");
        }
        
        @Override
        public void run()
        {
            try
            {
                byte[] receiveData = new byte[1024];
                ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveData);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                DatagramSocket receiveSocket = new DatagramSocket(m_receivePort);
                
                InetAddress remoteTelemetryRepeaterAddress = null;
                DatagramSocket remoteTelemetryRepeaterSocket = null;
                DatagramPacket retransmitPacket = null;

                if (m_telemetryRepeaterEnabled)
                {
                    remoteTelemetryRepeaterAddress = InetAddress.getByName(m_telemetryRepeaterHostname);
                    remoteTelemetryRepeaterSocket = new DatagramSocket();
                    retransmitPacket = new DatagramPacket(receiveData, receiveData.length, remoteTelemetryRepeaterAddress, m_telemetryRepeaterPort);
                }


                while (true)
                {
                    receiveSocket.receive(packet);
                    int calculatedChecksum = calcChecksum(receiveData, packet.getLength() - 4);
                    int receivedChecksum = receiveBuffer.getInt(packet.getLength() - 4);

                    if (calculatedChecksum == receivedChecksum)
                    {
                        // Re-send the incoming telemety to another destination such as an antenna pointer if configured to do so
                        if (m_telemetryRepeaterEnabled)
                        {
                            retransmitPacket.setLength(packet.getLength());
                            remoteTelemetryRepeaterSocket.send(retransmitPacket);
                        }

                        receiveBuffer.rewind();

                        synchronized (m_lock)
                        {
                            if (m_telemetryMessage == null)
                            {
                                m_telemetryMessage = new ShadowTelemetryMessage();
                            }

                            m_telemetryMessage.timestamp_ms = System.currentTimeMillis();
                            m_telemetryMessage.messageLength = receiveBuffer.getInt();
                            m_telemetryMessage.messageVersion = receiveBuffer.getInt();
                            m_telemetryMessage.busNumber = receiveBuffer.getInt();
                            m_telemetryMessage.knobsModeCommand = receiveBuffer.getInt();
                            m_telemetryMessage.insMode = receiveBuffer.getInt();
                            m_telemetryMessage.longitude_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.latitude_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.groundSpeedNorth_mps = receiveBuffer.getDouble();
                            m_telemetryMessage.groundSpeedEast_mps = receiveBuffer.getDouble();
                            m_telemetryMessage.trueHeading_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.magneticDeclination_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.roll_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.pitch_rad = receiveBuffer.getDouble();
                            m_telemetryMessage.gpsAltitude_m = receiveBuffer.getDouble();
                            m_telemetryMessage.barometricAltitudeMsl_m = receiveBuffer.getDouble();
                            m_telemetryMessage.verticalSpeed_mps = receiveBuffer.getDouble();
                            m_telemetryMessage.verticalAcceleration_mps2 = receiveBuffer.getDouble();
                            m_telemetryMessage.indicatedAirspeed_mps = receiveBuffer.getDouble();
                            m_telemetryMessage.trueAirspeed_mps = receiveBuffer.getDouble();
                            m_telemetryMessage.airTemp_k = receiveBuffer.getDouble();
                            m_telemetryMessage.gpsPositionalDilutionOfPrecision = receiveBuffer.getDouble();
                            m_telemetryMessage.fuelLevel_l = receiveBuffer.getDouble();
                            m_telemetryMessage.gpsTimeOfWeek_s = receiveBuffer.getDouble();
                            m_telemetryMessage.gpsWeekNumber = receiveBuffer.getInt();
                            m_telemetryMessage.flapsPosition = receiveBuffer.getInt();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private final Object m_lock = new Object();
    private ShadowCommandMessage m_commandMessage = null;
    private ShadowTelemetryMessage m_telemetryMessage = null;
    private ReceiveThread m_receiveThread = new ReceiveThread();
    private LogThread m_logThread = new LogThread();
    private int m_receivePort;
    private String m_remoteHostname;
    private int m_remotePort;
    private float m_sendRate_hz;
    private long m_sendPeriod_ms;
    private boolean m_logData;
    private FileWriter m_logFile;
    public static ShadowAutopilotInterface s_instance = null;
    private boolean m_telemetryRepeaterEnabled;
    private String m_telemetryRepeaterHostname;
    private int m_telemetryRepeaterPort;
    private BeliefManager m_BeliefManager;

    public ShadowAutopilotInterface(BeliefManager belMgr)
    {        
        this.setName ("WACS-ShadowAutopilotThread");
        m_receivePort = Config.getConfig().getPropertyAsInteger("ShadowAutopilotInterface.localPort");
        m_remotePort = Config.getConfig().getPropertyAsInteger("ShadowAutopilotInterface.remotePort");
        m_remoteHostname = Config.getConfig().getProperty("ShadowAutopilotInterface.remoteHostname");
        m_sendRate_hz = Config.getConfig().getPropertyAsInteger("ShadowAutopilotInterface.sendRateHz");
        m_sendPeriod_ms = (long)(1000 / m_sendRate_hz);
        m_logData = Config.getConfig().getPropertyAsBoolean("ShadowAutopilotInterface.logData");
        m_telemetryRepeaterEnabled = Config.getConfig().getPropertyAsBoolean("ShadowAutopilotInterface.telemetryRepeaterEnabled");
        m_telemetryRepeaterHostname = Config.getConfig().getProperty("ShadowAutopilotInterface.telemetryRepeaterHostname");
        m_telemetryRepeaterPort = Config.getConfig().getPropertyAsInteger("ShadowAutopilotInterface.telemetryRepeaterPort");
        m_BeliefManager = belMgr;

        if (m_logData)
        {
            m_logThread = new LogThread();
            m_logThread.start();
        }

        s_instance = this;
    }

    @Override
    public void setCommandMessage(CommandMessage copyFromBase)
    {
        ShadowCommandMessage copyFrom = (ShadowCommandMessage)copyFromBase;
        synchronized (m_lock)
        {
            if (m_commandMessage == null)
            {
                m_commandMessage = new ShadowCommandMessage();
            }
            m_commandMessage.messageLength = copyFrom.messageLength;
            m_commandMessage.messageVersion = copyFrom.messageVersion;
            m_commandMessage.busNumber = copyFrom.busNumber;
            m_commandMessage.knobsModeReport = copyFrom.knobsModeReport;
            m_commandMessage.altitudeCommand_m = copyFrom.altitudeCommand_m;
            m_commandMessage.rollCommand_rad = copyFrom.rollCommand_rad;
            m_commandMessage.airspeedCommand_mps = copyFrom.airspeedCommand_mps;
            m_commandMessage.throttlePercentageCommand = copyFrom.throttlePercentageCommand;
            m_commandMessage.throttleEnable = copyFrom.throttleEnable;
        }
    }

    @Override
    public void copyLatestCommandMessage(CommandMessage copyToBase)
    {
        ShadowCommandMessage copyTo = (ShadowCommandMessage)copyToBase;
        synchronized (m_lock)
        {
            if (m_commandMessage != null)
            {
                copyTo.messageLength = m_commandMessage.messageLength;
                copyTo.messageVersion = m_commandMessage.messageVersion;
                copyTo.busNumber = m_commandMessage.busNumber;
                copyTo.knobsModeReport = m_commandMessage.knobsModeReport;
                copyTo.altitudeCommand_m = m_commandMessage.altitudeCommand_m;
                copyTo.rollCommand_rad = m_commandMessage.rollCommand_rad;
                copyTo.airspeedCommand_mps = m_commandMessage.airspeedCommand_mps;
                copyTo.throttlePercentageCommand = m_commandMessage.throttlePercentageCommand;
                copyTo.throttleEnable = m_commandMessage.throttleEnable;
            }
        }
    }

    @Override
    public boolean copyLatestTelemetryMessage(TelemetryMessage copyToBase)
    {
        ShadowTelemetryMessage copyTo = (ShadowTelemetryMessage)copyToBase;
        synchronized (m_lock)
        {
            if (m_telemetryMessage != null && m_telemetryMessage.timestamp_ms != copyTo.timestamp_ms)
            {
                copyTo.timestamp_ms = m_telemetryMessage.timestamp_ms;
                copyTo.messageLength = m_telemetryMessage.messageLength;
                copyTo.messageVersion = m_telemetryMessage.messageVersion;
                copyTo.busNumber = m_telemetryMessage.busNumber;
                copyTo.knobsModeCommand = m_telemetryMessage.knobsModeCommand;
                copyTo.insMode = m_telemetryMessage.insMode;
                copyTo.longitude_rad = m_telemetryMessage.longitude_rad;
                copyTo.latitude_rad = m_telemetryMessage.latitude_rad;
                copyTo.groundSpeedNorth_mps = m_telemetryMessage.groundSpeedNorth_mps;
                copyTo.groundSpeedEast_mps = m_telemetryMessage.groundSpeedEast_mps;
                copyTo.trueHeading_rad = m_telemetryMessage.trueHeading_rad;
                copyTo.magneticDeclination_rad = m_telemetryMessage.magneticDeclination_rad;
                copyTo.roll_rad = m_telemetryMessage.roll_rad;
                copyTo.pitch_rad = m_telemetryMessage.pitch_rad;
                copyTo.gpsAltitude_m = m_telemetryMessage.gpsAltitude_m;
                copyTo.barometricAltitudeMsl_m = m_telemetryMessage.barometricAltitudeMsl_m;
                copyTo.verticalSpeed_mps = m_telemetryMessage.verticalSpeed_mps;
                copyTo.verticalAcceleration_mps2 = m_telemetryMessage.verticalAcceleration_mps2;
                copyTo.indicatedAirspeed_mps = m_telemetryMessage.indicatedAirspeed_mps;
                copyTo.trueAirspeed_mps = m_telemetryMessage.trueAirspeed_mps;
                copyTo.airTemp_k = m_telemetryMessage.airTemp_k;
                copyTo.gpsPositionalDilutionOfPrecision = m_telemetryMessage.gpsPositionalDilutionOfPrecision;
                copyTo.fuelLevel_l = m_telemetryMessage.fuelLevel_l;
                copyTo.gpsTimeOfWeek_s = m_telemetryMessage.gpsTimeOfWeek_s;
                copyTo.gpsWeekNumber = m_telemetryMessage.gpsWeekNumber;
                copyTo.flapsPosition = m_telemetryMessage.flapsPosition;

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private int calcChecksum(final byte[] data, final int dataSize)
    {
        int checksum = 0;
        for (int i = 0; i < dataSize; ++i)
        {
            checksum += (data[i] & 0xFF);
        }

        return checksum;
    }


    @Override
    public void run()
    {
        try
        {
            m_receiveThread.start();

            byte[] sendData = new byte[52];
            ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
            InetAddress remoteIPAddress = InetAddress.getByName(m_remoteHostname);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, remoteIPAddress, m_remotePort);
            DatagramSocket socket = new DatagramSocket();

            while (true)
            {
                long startTime_ms = System.currentTimeMillis();

                boolean autopilotControlEnabled = false;
                EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)m_BeliefManager.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                if (actualControl != null && actualControl.getAllow())
                    autopilotControlEnabled = true;
                
                if (autopilotControlEnabled || 
                   (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate") && (Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation"))))
                {
                    sendBuffer.rewind();

                    synchronized (m_lock)
                    {
                        if (m_commandMessage != null)
                        {
                            sendBuffer.putInt(m_commandMessage.messageLength);
                            sendBuffer.putInt(m_commandMessage.messageVersion);
                            sendBuffer.putInt(m_commandMessage.busNumber);
                            sendBuffer.putInt(m_commandMessage.knobsModeReport);
                            sendBuffer.putDouble(m_commandMessage.altitudeCommand_m);
                            sendBuffer.putDouble(m_commandMessage.rollCommand_rad);
                            sendBuffer.putDouble(m_commandMessage.airspeedCommand_mps);
                            sendBuffer.putInt(m_commandMessage.throttlePercentageCommand);
                            sendBuffer.putInt(m_commandMessage.throttleEnable);
                            sendBuffer.putInt(calcChecksum(sendData, sendBuffer.position()));

                            socket.send(packet);
                        }
                    }
                }

                long loopDuration_ms = System.currentTimeMillis() - startTime_ms;

                if (loopDuration_ms < m_sendPeriod_ms)
                {
                    Thread.sleep(m_sendPeriod_ms - loopDuration_ms);
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
    
    @Override
    public CommandMessage getBlankCommandMessage()
    {
        return new ShadowCommandMessage();
    }
    
    @Override
    public TelemetryMessage getBlankTelemetryMessage()
    {
        return new ShadowTelemetryMessage();
    }
}
