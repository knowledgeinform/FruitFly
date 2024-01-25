package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Acceleration;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Area;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.Time;
import edu.jhuapl.jlib.math.position.CartesianPosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.jlib.ui.overlay.RectangleDrawable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.CommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.TelemetryMessage;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.LoiterApproachPathBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceActualBelief;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.util.SafetyBox;
import edu.jhuapl.nstd.swarm.util.Vector3;
import edu.jhuapl.nstd.swarm.util.WindHistory;
import edu.jhuapl.nstd.util.WindEstimator;
import edu.jhuapl.nstd.util.WindEstimator.EstimatedWind;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import javax.swing.JOptionPane;

public class ShadowDriver extends Thread
{
    static final protected int UPDATE_PERIOD_MS = 100;
    static final protected int BARO_ALTITUDE_ERROR_AVERAGING_PERIOD_SEC = 15;
    static final protected int BARO_ALTITUDE_ERROR_AVERAGING_PERIOD_UPDATE_CYCLES = (int)(BARO_ALTITUDE_ERROR_AVERAGING_PERIOD_SEC * (1000.0 / UPDATE_PERIOD_MS));
    protected BeliefManager m_beliefManager;
    protected String m_agentID;
    protected double m_maxBankAngle_rad;
    protected Length m_minTurnRadius;
    protected Speed m_DesiredCommandedAirspeed;
    protected Speed m_ActualCommandedAirspeed;
    protected double m_bearingToTurnRateCoefficient;
    protected SafetyBox m_safetyBox;
    protected long m_circularOrbitBeliefTimestamp = 0;
    protected CircularOrbitBelief m_safeCircularOrbitBelief = null;
    protected long m_RacetrackBeliefTimestamp = 0;
    protected long m_ExplosionTimeBeliefTimestamp = 0;
    protected long m_TargetsBeliefTimestamp = 0;
    protected RacetrackOrbitBelief m_SafeRacetrackBelief = null;
    protected EstimatedWind m_estimatedWind = new EstimatedWind();
    protected WindHistory m_WindHistory;
    protected double m_centerVectorBias;
    protected double m_toApproachPathVectorBias;
    protected double m_barometricAltitudeErrorMovingAverage_m = 0;
    protected double m_ErrorIntegral = 0.0;
    protected Length m_DistanceToOrbit;
    protected double m_IntegralGain;
    protected double m_IntegralControlDistanceMeters;
    protected double m_maxBarometricAltitudeError_m;
    protected double m_laserAltitudeDataTimeout_ms;
    protected final Object m_baroAltitudeErrorLock = new Object();
    protected final Object m_commandMessageLock = new Object();
    
    protected KnobsAutopilotInterface.TelemetryMessage m_telemetryMessage;
    protected KnobsAutopilotInterface.CommandMessage m_autopilotCommandMessage = null;
    protected KnobsAutopilotInterface m_autopilotInterface;
    
    protected boolean m_DoLoiterApproach = false;
    protected NavyAngle m_BearingOfLoiterApproachPath;
    protected LatLonAltPosition m_ContactPositionLLA;
    protected LatLonAltPosition m_SafePositionLLA;
    protected LatLonAltPosition m_FirstRangePositionLLA;
    protected CartesianPosition m_ContactPositionCart;
    protected CartesianPosition m_ClosestPointOnPathCart;
    protected boolean m_ApproachPathInvalid;
    protected boolean m_ApproachPointsInitialized = false;
    protected double m_FinalRacetrackOrbitBufferPercentage;
    protected Length m_ApproachPathDistanceBuffer;
    protected NavyAngle m_ApproachPathBearingBuffer;
    protected double m_ApproachPathMaxSpeedIncreaseScale;
    protected Time m_ApproachPathTimeBuffer;
    protected Time m_TimeAgentToContact;
    protected Length m_DiffContactToFirstRangePoints;
    protected Length m_DiffCenterToSafePoints;
    protected Speed m_AltitudeDescentRate;
    protected Speed m_AltitudeAscentRate;
    protected Acceleration m_AirspeedDecelRate;
    protected Speed m_ApproachPathGroundSpeed;
    protected Angle m_AngleAroundFinalOrbitToWarn;
    protected boolean m_WarnedFinalLoiter;
    protected float[] m_AltitudeSteps;
    protected long m_TimeAtEachAltitudeStepMs;
    protected boolean m_StartAltitudeDescent;
    protected boolean m_StopAltitudeDescent;
    protected int m_AltitudeStepIdx;
    protected long m_TimeFirstCrossedCurrStepAltMs;
    protected Altitude m_AltitudeStepBufferDistance;
    protected boolean m_ForceDescentOnLoiterApproach;
    protected boolean m_LastCommandedAirspeedWasFast = false;
    protected boolean m_OnFinalLoiter;
    protected NavyAngle m_AvgWindToDirection = NavyAngle.ZERO;
    protected Speed m_AvgWindSpeed = Speed.ZERO;
    protected long m_LastTimeToExplosionMs;
    protected long m_TimeDeltaToSwitchFromApproachPathMs;
    protected boolean m_ApproachTargetFromLeft = false;
    protected double m_MaximumAltitudeLeadingCommand_m;
    
    protected long m_LastReportTimeMs = -1;
    protected long m_ReportUpdateTimeMs = 2000;

    public ShadowDriver(BeliefManager beliefManager)
    {
        initialize (beliefManager, null, null);
    }

    public ShadowDriver(BeliefManager beliefManager, String agentID, KnobsAutopilotInterface autoPilotInterface)
    {
        initialize (beliefManager, agentID, autoPilotInterface);
    }

    private void initialize (BeliefManager beliefManager, String agentID, KnobsAutopilotInterface autoPilotInterface)
    {
        this.setName ("WACS-ShadowDriver");
        m_telemetryMessage = autoPilotInterface.getBlankTelemetryMessage();
        m_beliefManager = beliefManager;
        m_agentID = agentID;
        m_autopilotInterface = autoPilotInterface;
        m_autopilotCommandMessage = m_autopilotInterface.getBlankCommandMessage();
        m_safetyBox = new SafetyBox(beliefManager);
        m_LastTimeToExplosionMs = Long.MAX_VALUE;
        m_maxBankAngle_rad = Math.toRadians(Config.getConfig().getPropertyAsDouble("FlightControl.maxBankAngle_deg", 20.0));
        m_DesiredCommandedAirspeed = new Speed(Config.getConfig().getPropertyAsDouble("ShadowDriver.commandedAirspeed_knots", 65.0), Speed.KNOTS);
        m_ActualCommandedAirspeed = (Speed)m_DesiredCommandedAirspeed.clone();
        m_minTurnRadius = new Length((m_DesiredCommandedAirspeed.getDoubleValue(Speed.METERS_PER_SECOND) * m_DesiredCommandedAirspeed.getDoubleValue(Speed.METERS_PER_SECOND)) / (9.81 * Math.tan(m_maxBankAngle_rad)), Length.METERS);
        m_bearingToTurnRateCoefficient = Config.getConfig().getPropertyAsDouble("ShadowDriver.bearingToTurnRateCoefficient");
        m_centerVectorBias = Config.getConfig().getPropertyAsDouble("ShadowDriver.centerVectorBias");
        m_toApproachPathVectorBias = Config.getConfig().getPropertyAsDouble("ShadowDriver.ToApproachPathVectorBias", 1.5);
        m_IntegralGain = Config.getConfig().getPropertyAsDouble("ShadowDriver.IntegralGain", 0.01);
        m_IntegralControlDistanceMeters = Config.getConfig().getPropertyAsDouble("ShadowDriver.IntegralControlDistanceMeters", 50.0);
        m_WindHistory = new WindHistory(agentID);
        m_maxBarometricAltitudeError_m = Config.getConfig().getPropertyAsDouble("ShadowDriver.maxBarometricAltitudeError_m", 0);
        m_laserAltitudeDataTimeout_ms = Config.getConfig().getPropertyAsDouble("ShadowDriver.laserAltitudeDataTimeout_ms", 0);
        m_FinalRacetrackOrbitBufferPercentage = 1 + Math.max (0, Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.FinalOrbitBufferPercentage", .05));
        m_ApproachPathDistanceBuffer = new Length (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.ApproachPathDistanceBuffer.Meters", 100), Length.METERS);
        m_ApproachPathBearingBuffer = new NavyAngle (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.ApproachPathBearingBuffer.Degrees", 10), Angle.DEGREES);
        m_ApproachPathMaxSpeedIncreaseScale = 1 + Math.max (0, Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.ApproachPathMaxSpeedIncreaseScale", 0.1));
        m_ApproachPathTimeBuffer = new Time (Config.getConfig().getPropertyAsLong("ShadowDriver.RacetrackLoiter.ApproachPathTimeBuffer.Ms", 5000), Time.MILLISECONDS);
        m_AltitudeDescentRate = new Speed (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AltitudeDescentRate.fps", 10), Speed.FEET_PER_SECOND);
        m_AltitudeAscentRate = new Speed (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AltitudeAscentRate.fps", 6), Speed.FEET_PER_SECOND);
        m_AirspeedDecelRate = new Acceleration (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AirspeedDecelerationRate.ktspersec", 5), Acceleration.KNOTS_PER_SECOND);
        m_AngleAroundFinalOrbitToWarn = new Angle (Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AngleAroundFinalOrbitToWarn.Degrees", 45), Angle.DEGREES);
        m_autopilotCommandMessage.airspeedCommand_mps = m_DesiredCommandedAirspeed.getDoubleValue(Speed.METERS_PER_SECOND);
        m_TimeDeltaToSwitchFromApproachPathMs = Config.getConfig().getPropertyAsLong("ShadowDriver.TimeDeltaToSwitchFromApproachPathMs", 60000);
        m_MaximumAltitudeLeadingCommand_m = Config.getConfig().getPropertyAsDouble("ShadowDriver.MaxAltLeadingCommand.Meters", 100);
        
        //If camera is on right wing, we should approach target on left so we can see it from our right side
        m_ApproachTargetFromLeft = Config.getConfig().getPropertyAsBoolean("FlightControl.CameraOnRightWing", false);

        readAltitudeSteps();
    }

    private void readAltitudeSteps()
    {
        int numAltitudeSteps = Config.getConfig().getPropertyAsInteger("ShadowDriver.NumAltitudeSteps", 2);
        String altitudeSteps = Config.getConfig().getProperty("ShadowDriver.AltitudeSteps", "50,90,");
        m_TimeAtEachAltitudeStepMs = Config.getConfig().getPropertyAsLong("ShadowDriver.TimeAtEachAltitudeStep.Ms", 10000);
        m_AltitudeStepBufferDistance = new Altitude (Config.getConfig().getPropertyAsDouble ("ShadowDriver.AltitudeStepBufferDistance.Meters", 5), Length.METERS);
        m_ForceDescentOnLoiterApproach = Config.getConfig().getPropertyAsBoolean("ShadowDriver.ForceDescentOnLoiterApproach", true);
        String[] altitudeStepsList = altitudeSteps.split(",");
        m_AltitudeSteps = new float [numAltitudeSteps+1];
        for (int i = 0; i < numAltitudeSteps; i ++)
        {
            if (i < altitudeStepsList.length)
            {
                try
                {
                    m_AltitudeSteps[i] = Float.parseFloat(altitudeStepsList[i]);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.err.println ("Error parsing altitude steps in shadow driver");

                    if (i > 0)
                        m_AltitudeSteps[i] = m_AltitudeSteps[i-1];
                    else
                        m_AltitudeSteps[i] = 0;
                }
            }
            else
            {
                m_AltitudeSteps[i] = 100;
            }
        }

        m_AltitudeSteps[numAltitudeSteps] = 100;
    }

    public CommandMessage getAutopilotCommandMessage()
    {
        synchronized (m_autopilotCommandMessage)
        {
            return m_autopilotCommandMessage;
        }
    }

    @Override
    public void run()
    {
        Latitude planeLatitude = null;
        Longitude planeLongitude = null;
        LatLonAltPosition planePosition = null;
        NavyAngle planeHeading = null;

        try
        {
            while(true)
            {
                long lastUpdateTime_ms = System.currentTimeMillis();


                if (m_autopilotInterface.copyLatestTelemetryMessage(m_telemetryMessage))
                {
                    m_autopilotInterface.copyLatestTelemetryMessage(m_telemetryMessage);
                    AgentPositionBelief posBelief = (AgentPositionBelief) m_beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                    if (posBelief != null && posBelief.getPositionTimeName(m_agentID) != null)
                    {
                        //
                        // Since the Shadow doesn't have a laser altimeter, keep track of the baro drift by using the pod's
                        // laser altimeter and then offsetting the altitude commands to the Shadow autopilot by the appropriate amount.
                        //
                        Altitude planeAltitudeMSL;
                        double barometricAltitudeError_m = 0;

                        PiccoloTelemetryBelief piccoloTelemetryBelief = (PiccoloTelemetryBelief)m_beliefManager.get(PiccoloTelemetryBelief.BELIEF_NAME);

                        if ((piccoloTelemetryBelief != null) && (System.currentTimeMillis() - piccoloTelemetryBelief.getTimeStamp().getTime() < m_laserAltitudeDataTimeout_ms) && (piccoloTelemetryBelief.getPiccoloTelemetry().AltLaserValid))
                        {
                            planeAltitudeMSL = new Altitude(piccoloTelemetryBelief.getPiccoloTelemetry().AltLaser_m, Length.METERS);
                            planeAltitudeMSL = planeAltitudeMSL.plus(DtedGlobalMap.getDted().getJlibAltitude(piccoloTelemetryBelief.getPiccoloTelemetry().Lat, piccoloTelemetryBelief.getPiccoloTelemetry().Lon).asLength());
                            barometricAltitudeError_m = m_telemetryMessage.barometricAltitudeMsl_m - planeAltitudeMSL.getDoubleValue(Length.METERS);
                        }
                        else
                        {
                            planeAltitudeMSL = new Altitude(m_telemetryMessage.barometricAltitudeMsl_m, Length.METERS);
                        }

                        synchronized (m_baroAltitudeErrorLock)
                        {
                            m_barometricAltitudeErrorMovingAverage_m = ((m_barometricAltitudeErrorMovingAverage_m * (BARO_ALTITUDE_ERROR_AVERAGING_PERIOD_UPDATE_CYCLES - 1)) + barometricAltitudeError_m) / BARO_ALTITUDE_ERROR_AVERAGING_PERIOD_UPDATE_CYCLES;
                        }

                        //
                        // Cap barometric altitude error
                        //
                        if (m_barometricAltitudeErrorMovingAverage_m > m_maxBarometricAltitudeError_m)
                        {
                            m_barometricAltitudeErrorMovingAverage_m = m_maxBarometricAltitudeError_m;
                        }
                        else if ((m_barometricAltitudeErrorMovingAverage_m < -m_maxBarometricAltitudeError_m))
                        {
                            m_barometricAltitudeErrorMovingAverage_m = -m_maxBarometricAltitudeError_m;
                        }

                        planeLatitude = new Latitude(m_telemetryMessage.latitude_rad, Angle.RADIANS);
                        planeLongitude = new Longitude(m_telemetryMessage.longitude_rad, Angle.RADIANS);
                        planePosition = new LatLonAltPosition(planeLatitude, planeLongitude, planeAltitudeMSL);
                        planeHeading = new NavyAngle(m_telemetryMessage.trueHeading_rad, Angle.RADIANS);


                        AgentModeActualBelief agentMode = (AgentModeActualBelief)m_beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
                        if (agentMode != null)
                        {
                            if (agentMode.getMode(WACSAgent.AGENTNAME) != null)
                            {
                                if (agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
                                {
                                    //Do regular circular orbits
                                    CircularOrbitBelief circularOrbitBelief = (CircularOrbitBelief) m_beliefManager.get(CircularOrbitBelief.BELIEF_NAME);
                                    if (circularOrbitBelief != null)
                                    {
                                        // Make sure orbit stays within local safety limits
                                        if (circularOrbitBelief.getTimeStamp().getTime() > m_circularOrbitBeliefTimestamp)
                                        {
                                            m_circularOrbitBeliefTimestamp = circularOrbitBelief.getTimeStamp().getTime();
                                            m_safeCircularOrbitBelief = m_safetyBox.getSafeCircularOrbit(circularOrbitBelief);
                                        }

                                        updateCommandDataIntercept(m_safeCircularOrbitBelief, m_telemetryMessage, planeAltitudeMSL);


                                         m_autopilotInterface.setCommandMessage(m_autopilotCommandMessage);
                                    }
                                }
                                else if(agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                                {
                                    //Do loiter racetrack orbits
                                    RacetrackOrbitBelief racetrackBelief = (RacetrackOrbitBelief) m_beliefManager.get(RacetrackOrbitBelief.BELIEF_NAME);
                                    boolean stayOnLoiterApproach = false;
                                    if (racetrackBelief != null)
                                    {
                                        ExplosionTimeActualBelief expTimeBlf = (ExplosionTimeActualBelief)m_beliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME);
                                        if (expTimeBlf == null)
                                            expTimeBlf = new ExplosionTimeActualBelief(WACSAgent.AGENTNAME, Long.MAX_VALUE);
                                        String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                                        TargetActualBelief targets = (TargetActualBelief) m_beliefManager.get(TargetActualBelief.BELIEF_NAME);
                                        LatLonAltPosition gimTarg = targets.getPositionTimeName(gimbalTargetName).getPosition().asLatLonAltPosition();
                                        boolean needUpdate = false;
                                        if (racetrackBelief.getTimeStamp().getTime() > m_RacetrackBeliefTimestamp)
                                        {
                                            m_RacetrackBeliefTimestamp = racetrackBelief.getTimeStamp().getTime();
                                            
                                            //Don't do check here.  It should have been done elsewhere and we should fix it if it's commanded, not just ignore that it happened in the first place.
                                            //m_safeRacetrackBelief = m_safetyBox.getSafeCircularOrbit(racetrackBelief);
                                            m_SafeRacetrackBelief =  racetrackBelief;

                                            needUpdate = true;
                                        }
                                        if (expTimeBlf.getTimeStamp().getTime() > m_ExplosionTimeBeliefTimestamp)
                                        {
                                            m_ExplosionTimeBeliefTimestamp = expTimeBlf.getTimeStamp().getTime();
                                            needUpdate = true;


                                            if (m_DoLoiterApproach && (Math.abs(expTimeBlf.getTime_ms()-System.currentTimeMillis() - m_LastTimeToExplosionMs) < m_TimeDeltaToSwitchFromApproachPathMs))
                                            {
                                                stayOnLoiterApproach = true;
                                            }
                                        }
                                        if (targets.getTimeStamp().getTime() > m_TargetsBeliefTimestamp)
                                        {
                                            m_TargetsBeliefTimestamp = targets.getTimeStamp().getTime();
                                            needUpdate = true;
                                        }

                                        if (needUpdate)
                                        {
                                            m_DoLoiterApproach = false;
                                            m_ApproachPointsInitialized = false;
                                            m_WarnedFinalLoiter = false;
                                            m_StartAltitudeDescent = false;
                                            m_OnFinalLoiter = false;
                                            m_ApproachPathInvalid = false;
                                        }

                                        if (needUpdate && stayOnLoiterApproach)
                                        {
                                            m_DoLoiterApproach = true;
                                        }

                                        m_LastTimeToExplosionMs = expTimeBlf.getTime_ms()-System.currentTimeMillis();
                                        updateCommandDataLoiterCircle(m_SafeRacetrackBelief, gimTarg, m_LastTimeToExplosionMs, m_telemetryMessage, planeAltitudeMSL);
                                        
                                        m_autopilotInterface.setCommandMessage(m_autopilotCommandMessage);
                                    }
                                }
                            }
                        }

                        
                    }

                    boolean simulate = Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate");
                    boolean useExternalSimulation = Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation");


                    if (simulate && useExternalSimulation && planePosition != null && planeHeading != null)
                    {
                        AgentPositionBelief agentPositionBelief = new AgentPositionBelief(WACSAgent.AGENTNAME, planePosition, planeHeading);
                        m_beliefManager.put(agentPositionBelief);

                        AgentBearingBelief agentBearingBelief = new AgentBearingBelief(WACSAgent.AGENTNAME, planeHeading, planeHeading);
                        m_beliefManager.put(agentBearingBelief);
                    }

                    WindEstimateSourceActualBelief windSourceBelief = (WindEstimateSourceActualBelief)m_beliefManager.get (WindEstimateSourceActualBelief.BELIEF_NAME);
                    if (windSourceBelief != null && windSourceBelief.getWindSource() == WindEstimateSourceActualBelief.WINDSOURCE_UAVAUTOPILOT && planePosition != null)
                    {
                        WindEstimator.estimateWind(m_telemetryMessage.groundSpeedNorth_mps,
                                                   m_telemetryMessage.groundSpeedEast_mps,
                                                   m_telemetryMessage.trueAirspeed_mps,
                                                   m_telemetryMessage.pitch_rad,
                                                   m_telemetryMessage.trueHeading_rad,
                                                   m_estimatedWind);


                       METPositionTimeName metPositionTimeName = new METPositionTimeName(m_agentID,
                                                                                         m_estimatedWind.blowingToHeading,
                                                                                         m_estimatedWind.speed,
                                                                                         planePosition,
                                                                                         new Date());
                        m_WindHistory.addMETPosition(metPositionTimeName);
                        METTimeName metTimeName = m_WindHistory.getAverageWind();
                        m_AvgWindToDirection = metTimeName.getWindBearing();
                        m_AvgWindSpeed = metTimeName.getWindSpeed();
                        METBelief metBelief = new METBelief(m_agentID, metTimeName);
                        m_beliefManager.put(metBelief);
                        metBelief = new METBelief(WACSDisplayAgent.AGENTNAME, metTimeName);
                        m_beliefManager.put(metBelief);
                        METPositionBelief metPositionBelief = new METPositionBelief(m_agentID, metPositionTimeName);
                        m_beliefManager.put(metPositionBelief);
                    }
                    else
                    {
                        METBelief metBelief = (METBelief)m_beliefManager.get(METBelief.BELIEF_NAME);
                        METTimeName mtn = null;
                        if (metBelief != null && (mtn=metBelief.getMETTimeName(WACSAgent.AGENTNAME)) != null)
                        {
                            m_AvgWindToDirection = mtn.getWindBearing();
                            m_AvgWindSpeed = mtn.getWindSpeed();
                        }
                    }
                }

                long timeUntilNextUpdate_ms = UPDATE_PERIOD_MS - (System.currentTimeMillis() - lastUpdateTime_ms);
                if (timeUntilNextUpdate_ms <= 0)
                {
                    timeUntilNextUpdate_ms = 1;
                }

                Thread.sleep(timeUntilNextUpdate_ms);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public double getBaroAltitudeError_m()
    {
        synchronized (m_baroAltitudeErrorLock)
        {
            return m_barometricAltitudeErrorMovingAverage_m;
        }
    }

    private void updateContactPositionCart(LatLonAltPosition agentLatLon)
    {
        m_ContactPositionCart = m_ContactPositionLLA.asCartesianPosition(agentLatLon);
        m_ContactPositionCart = new CartesianPosition(m_ContactPositionCart.getX(), m_ContactPositionCart.getY(), new Length(0, Length.METERS));
    }

    private CartesianPosition getClosestPointOnPathCart(CartesianPosition contactPositionCart, CartesianPosition centerPositionCart)
    {
        double dx = contactPositionCart.getX().getDoubleValue(Length.METERS) - centerPositionCart.getX().getDoubleValue(Length.METERS);
        double dy = contactPositionCart.getY().getDoubleValue(Length.METERS) - centerPositionCart.getY().getDoubleValue(Length.METERS);
        double cx = 0 - centerPositionCart.getX().getDoubleValue(Length.METERS); //agent is at x=0
        double cy = 0 - centerPositionCart.getY().getDoubleValue(Length.METERS); //agent is at y=0
        double t = (cx*dx + cy*dy)/(dx*dx + dy*dy);
        double x0 = centerPositionCart.getX().getDoubleValue(Length.METERS) + t*dx;
        double y0 = centerPositionCart.getY().getDoubleValue(Length.METERS) + t*dy;
        CartesianPosition closestPointOnPathCart = new CartesianPosition(new Length (x0, Length.METERS), new Length(y0, Length.METERS), Length.ZERO);

        return closestPointOnPathCart;
    }

    private double doBasicPlaneHeading(RangeBearingHeightOffset targetMovementVector, double trueHeading_rad)
    {
        double planeHeading_rad = (trueHeading_rad % (Math.PI * 2));
        if (planeHeading_rad < 0)
        {
            planeHeading_rad += (Math.PI * 2);
        }
        double targetMovementVectorBearing_rad = targetMovementVector.getBearing().getDoubleValue(Angle.RADIANS) - planeHeading_rad;
        if (targetMovementVectorBearing_rad > Math.PI)
        {
            targetMovementVectorBearing_rad -= Math.PI * 2;
        }
        else if (targetMovementVectorBearing_rad < -Math.PI)
        {
            targetMovementVectorBearing_rad += Math.PI * 2;
        }
        return targetMovementVectorBearing_rad * m_bearingToTurnRateCoefficient;
    }

    private double getTurnRateOrbit(CartesianPosition centerPositionCart, boolean isClockwise, Length radius, TelemetryMessage telemetryMessage)
    {
        RangeBearingHeightOffset targetMovementVector = null;


        Vector3 centerVector = new Vector3(centerPositionCart, Length.METERS);
        Length distanceToCenter = centerVector.length();
        centerVector.normalize();

        // create the tangent vector
        Vector3 tangent = new Vector3(centerVector);
        if (isClockwise)
        {
            tangent.rotateZ(Math.PI / 2.0);
        }
        else
        {
            tangent.rotateZ(-Math.PI / 2.0);
        }
        tangent.normalize();


        //
        // Calculate how much we need to move toward or away from the center of the circle
        //
        m_DistanceToOrbit = distanceToCenter.minus(radius);
        double errorRatio = m_DistanceToOrbit.dividedBy(m_minTurnRadius).getDoubleValue();
        centerVector.scale(errorRatio * m_centerVectorBias);

        targetMovementVector = centerVector.plus(tangent).normalized().asRangeBearingHeightOffset();
        double targetTurnRate_radps = doBasicPlaneHeading(targetMovementVector, telemetryMessage.trueHeading_rad);

        //Do Integral control
        if(m_DistanceToOrbit.isGreaterThan(-m_IntegralControlDistanceMeters,Length.METERS) && m_DistanceToOrbit.isLessThan(m_IntegralControlDistanceMeters,Length.METERS))
        {
            m_ErrorIntegral += errorRatio;

            if(isClockwise)
            {
                targetTurnRate_radps += m_ErrorIntegral * m_IntegralGain;
            }
            else
            {
               targetTurnRate_radps -= m_ErrorIntegral * m_IntegralGain;
            }
        }
        else
        {
            m_ErrorIntegral = 0.0;
        }

        return targetTurnRate_radps;
    }

    private RangeBearingHeightOffset computeMovementVectorToFollowPath(CartesianPosition toPosition, CartesianPosition fromPosition)
    {
        m_ClosestPointOnPathCart = getClosestPointOnPathCart (toPosition, fromPosition);

        //Vector to path
        Vector3 toPathVector = new Vector3(m_ClosestPointOnPathCart, Length.METERS);
        Length distToPath = toPathVector.length();
        toPathVector.normalize();

        // create the path tangent vector
        RangeBearingHeightOffset rbh = fromPosition.getRangeBearingHeightOffsetTo(toPosition);
        Vector3 tangent = new Vector3 (rbh.asCartesianPosition(CartesianPosition.ORIGIN), Length.METERS);
        tangent.normalize();

        //Form movement vector by combining all vectors
        RangeBearingHeightOffset targetMovementVector = null;
        double errorRatio = distToPath.dividedBy(m_minTurnRadius).getDoubleValue();
        toPathVector.scale(errorRatio * m_toApproachPathVectorBias);
        targetMovementVector = toPathVector.plus(tangent).normalized().asRangeBearingHeightOffset();

        return targetMovementVector;
    }


    static BufferedWriter loiterCircleLog = null;
    static {
        try
        {
            if (Config.getConfig().getPropertyAsBoolean("ShadowDriver.LogLoiterCircleData", true))
            {
                File newFile = new File ("loiterCircleLog_" + System.currentTimeMillis() + ".csv");
                newFile.createNewFile();
                loiterCircleLog = new BufferedWriter (new FileWriter(newFile));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateCommandDataLoiterCircle(RacetrackOrbitBelief racetrackBelief, LatLonAltPosition targetPosition, long timeUntilExplosionMs, TelemetryMessage telemetryMessage, Altitude planeAltitude)
    {

        try
        {
            if (loiterCircleLog != null)
            {
                loiterCircleLog.write(System.currentTimeMillis() + ",");
            }

            //
            // Calculate vector to circular orbit
            //
            LatLonAltPosition agentLatLon = new LatLonAltPosition(new Latitude(telemetryMessage.latitude_rad, Angle.RADIANS),
                                                                  new Longitude(telemetryMessage.longitude_rad, Angle.RADIANS),
                                                                  planeAltitude);

            double targetTurnRate_radps = 0;

            if (agentLatLon == null || racetrackBelief == null)
            {
                //do nothing
            }
            else
            {
                if (loiterCircleLog != null)
                {
                    loiterCircleLog.write(agentLatLon.getLatitude().getDoubleValue(Angle.DEGREES) + ",");
                    loiterCircleLog.write(agentLatLon.getLongitude().getDoubleValue(Angle.DEGREES) + ",");
                    loiterCircleLog.write(agentLatLon.getAltitude().getDoubleValue(Length.METERS) + ",");
                }

                // get the rotation center position, defaulting to the set one
                LatLonAltPosition centerPositionLLA = new LatLonAltPosition(racetrackBelief.getLatitude1(), racetrackBelief.getLongitude1(), racetrackBelief.getFinalAltitudeMsl());
                CartesianPosition centerPositionCart = centerPositionLLA.asCartesianPosition(agentLatLon);
                centerPositionCart = new CartesianPosition(centerPositionCart.getX(), centerPositionCart.getY(), new Length(0, Length.METERS));

                // get target location
                LatLonAltPosition targetPositionLLA = targetPosition;
                CartesianPosition targetPositionCart = targetPositionLLA.asCartesianPosition(agentLatLon);
                targetPositionCart = new CartesianPosition(targetPositionCart.getX(), targetPositionCart.getY(), new Length(0, Length.METERS));

                if (centerPositionCart != null)
                {
                    if (targetPositionLLA.getRangeTo(centerPositionLLA).isLessThan(racetrackBelief.getRadius()))
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("0,");
                            loiterCircleLog.write(centerPositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) + ",");
                            loiterCircleLog.write(centerPositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) + ",");
                            loiterCircleLog.write(centerPositionLLA.getAltitude().getDoubleValue(Length.METERS) + ",");
                            loiterCircleLog.write(targetPositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) + ",");
                            loiterCircleLog.write(targetPositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) + ",");
                            loiterCircleLog.write(targetPositionLLA.getAltitude().getDoubleValue(Length.METERS) + ",");
                            loiterCircleLog.write ("\n");
                        }

                        //We always do a simple orbit for this case (target within orbit radius), no approach path
                        //do regular circle orbit loiter.  Stay at final orbit the entire time
                        targetTurnRate_radps = getTurnRateOrbit (centerPositionCart, racetrackBelief.getIsClockwise(), racetrackBelief.getRadius(), telemetryMessage);
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                        LoiterApproachPathBelief points = new LoiterApproachPathBelief(null, null, null, false);
                        m_beliefManager.put(points);
                        populateAutopilotCommandMessage (telemetryMessage, racetrackBelief.getFinalAltitudeMsl().getDoubleValue(Length.METERS), targetTurnRate_radps);
                        return;
                    }


                    //If we got here, then we are doing an offset loiter with an approach path.  Figure out what part we are on and move accordingly
                    if (loiterCircleLog != null)
                    {
                        loiterCircleLog.write("1,");
                        loiterCircleLog.write(centerPositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) + ",");
                        loiterCircleLog.write(centerPositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) + ",");
                        loiterCircleLog.write(centerPositionLLA.getAltitude().getDoubleValue(Length.METERS) + ",");
                        loiterCircleLog.write(targetPositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) + ",");
                        loiterCircleLog.write(targetPositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) + ",");
                        loiterCircleLog.write(targetPositionLLA.getAltitude().getDoubleValue(Length.METERS) + ",");
                    }



                    //If not on loiter approach pattern yet, check time required to get to contact point on approach path.  If have more time than
                    //needed, continue orbit.  If not enough time, begin approach path.
                    if (!m_DoLoiterApproach)
                    {
                        //Initialize contact point and path whenever orbit/target/time is changed
                        if (!m_ApproachPointsInitialized)
                        {
                            NavyAngle bearingCenterToTarget = centerPositionLLA.getBearingTo(targetPositionLLA);
                            double safeDistanceMeters = racetrackBelief.getRadius().getDoubleValue(Length.METERS);
                            Length safeDistance = racetrackBelief.getRadius();
                            double firstRangeDistanceMeters = Math.max(safeDistanceMeters, Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.FirstRangeDistanceFromTarget.Meters", 2500));
                            Length firstRangeDistance = new Length (firstRangeDistanceMeters, Length.METERS);
                            //double contactDistanceMeters = (safeDistanceMeters + firstRangeDistanceMeters)/2;
                            //Length contactDistance = new Length (contactDistanceMeters, Length.METERS);
                            
                            double theta = Math.asin(safeDistance.getDoubleValue(Length.METERS) / centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS));
                            if (m_ApproachTargetFromLeft)
                                theta = -theta;
                            NavyAngle bearingCenterToSafePoint = bearingCenterToTarget.plus(new Angle (theta, Angle.RADIANS));
                            m_BearingOfLoiterApproachPath  = (NavyAngle)bearingCenterToSafePoint.clone();
                            //Length distFromSafeToContactPoints = new Length (Math.sqrt(contactDistance.times(contactDistance).minus(safeDistance.times(safeDistance)).getDoubleValue(Area.METERS_SQUARED)), Length.METERS);
                            Length distFromSafeToFirstRangePoints = new Length (Math.sqrt(firstRangeDistance.times(firstRangeDistance).minus(safeDistance.times(safeDistance)).getDoubleValue(Area.METERS_SQUARED)), Length.METERS);
                            Length distFromSafeToContactPoints = distFromSafeToFirstRangePoints.dividedBy(2);
                            Length distFromCenterToContactPoint = new Length (centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS)*Math.cos(theta) - distFromSafeToContactPoints.getDoubleValue(Length.METERS), Length.METERS);
                            Length distFromCenterToFirstRangePoint = new Length (centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS)*Math.cos(theta) - distFromSafeToFirstRangePoints.getDoubleValue(Length.METERS), Length.METERS);
                            m_ContactPositionLLA = centerPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromCenterToContactPoint, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                            m_SafePositionLLA = m_ContactPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromSafeToContactPoints, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                            m_FirstRangePositionLLA = centerPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromCenterToFirstRangePoint, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                            m_DiffContactToFirstRangePoints = m_ContactPositionLLA.getRangeTo(m_FirstRangePositionLLA);
                            m_DiffCenterToSafePoints = centerPositionLLA.getRangeTo(m_SafePositionLLA);
                            m_ApproachPointsInitialized = true;

                            if (m_FirstRangePositionLLA.getRangeTo(centerPositionLLA).isLessThan(racetrackBelief.getRadius())
                                || targetPositionLLA.getRangeTo(centerPositionLLA).isLessThan(firstRangeDistance)
                                || !m_safetyBox.isPointInsideSafetyBox (m_SafePositionLLA)
                                || !m_safetyBox.isOrbitInsideSafetyBox (targetPositionLLA, racetrackBelief.getRadius()))
                                m_ApproachPathInvalid = true;

                            LoiterApproachPathBelief points = new LoiterApproachPathBelief(m_SafePositionLLA, m_ContactPositionLLA, m_FirstRangePositionLLA, !m_ApproachPathInvalid);
                            m_beliefManager.put(points);
                        }


                        /*
                         * Estimate of time required to get to contact point along approach path uses two calculations.
                         *  - Time required to turn plane from current bearing to approach path bearing (ignoring that plane really turns to fly directly towards approach path, then turns to approach path bearing)
                         *  - Time required for plane to fly straight to contact point from the current point (ignoring rotation and ignoring that plane doesn't fly directly to contact point, but first gets on approach path)
                         *
                         * Adjustments:
                         *  Turn radius of rotation is assumed to be minimum turn radius with no wind speed conditions
                         *  Total turn rotation depends on which way the plane turns, which is dependent on movement vectors
                         *  Straight line path from current plane position to contact position is offset by distance plane blown by wind during rotation
                         *  Straight line path time is affected by wind speed estimate parallel to this fake, straight-to-contact path.  In actuality, the wind speed parallel to the actual flight path will affect time, but this is an estimate
                         *  Speed during approach path can be increased if we are running late.  This is depenedent on time to contact point and actual ground speed based on wind speed parallel to the appraoch path.  If we are early, the plane does not slow down.  Must be back at slower airspeed by first range point (before contact point)
                         *
                         * Altitude:
                         *  Fly at standoff altitude until descent rate and time until explosion dictate we must descend to be at the final altitude by the first range point (before contact point)
                         *  Even if we have sufficient time to descend, we will start descend to final altitude as soon as approach pattern begins.
                         */



                        //Find rotation angle from current heading to path vector
                        RangeBearingHeightOffset targetMovementVector = null;
                        updateContactPositionCart(agentLatLon);
                        targetMovementVector = computeMovementVectorToFollowPath (m_ContactPositionCart, centerPositionCart);
                        //targetMovementVector is which way plane will first turn.  Assume this is the rotation direction, and find angle to path
                        //assuming this direction.  Might be greater than 180 degrees.
                        NavyAngle rotation = null;
                        if (Math.cos(new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS)) <= 0)
                        {
                            if(targetMovementVector.getBearing().clockwiseAngleTo(new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS)).isLessThan(Angle.HALF_CIRCLE))
                            {
                                //If not already going in general direction of the eventual path, vectors indicate we'll turn counter-clockwise to path
                                rotation = new NavyAngle (telemetryMessage.trueHeading_rad, Angle.RADIANS).counterClockwiseAngleTo(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                            }
                            else
                            {
                                //If not already going in general direction of the eventual path, vectors indicate we'll turn clockwise to path
                                rotation = new NavyAngle (telemetryMessage.trueHeading_rad, Angle.RADIANS).clockwiseAngleTo(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                            }
                        }
                        else if(Math.cos(m_BearingOfLoiterApproachPath.shortAngleBetween(agentLatLon.getBearingTo(m_FirstRangePositionLLA)).getDoubleValue(Angle.RADIANS)) <= 0)
                        {
                            //Sign of cosine between path and bearing to first point is sign of dot product If sign is positive, vectors are similar direction

                            //This means we are on wrong side of path and going the wrong way.  We need to do big loop back to get on track.
                            rotation = new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS).longAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                        }
                        else if (Math.cos(new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS)) > 0)
                        {
                            //Sign of cosine between heading and path is sign of dot product of heading and path.  If sign is positive, vectors are similar direction

                            //If we are already going in the general direction of the eventual path and are pointing towards the contact point, we'll take the short turn to continue on the path.
                            rotation = new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS).shortAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                        }

                        //Get time for rotation and offset of origination point assuming blown by wind during rotation
                        Length distAgentRotateToContact = m_minTurnRadius.times(rotation.getDoubleValue(Angle.RADIANS));
                        Time timeAgentRotate = distAgentRotateToContact.dividedBy(m_ActualCommandedAirspeed);
                        Length distBlownByWindDuringRotation = m_AvgWindSpeed.times(timeAgentRotate);
                        LatLonAltPosition agentLatLonOffsetByWindDuringRotation = agentLatLon.translatedBy(new RangeBearingHeightOffset(distBlownByWindDuringRotation, m_AvgWindToDirection, Length.ZERO)).asLatLonAltPosition();

                        //Correct for wind blowing against approach path
                        NavyAngle bearingDiffWindToPath = m_AvgWindToDirection.shortAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                        Speed windSpeedAlongPath = m_AvgWindSpeed.times(Math.cos(bearingDiffWindToPath.getDoubleValue(Angle.RADIANS)));
                        NavyAngle bearingDiffWindToStraightFakePath = m_AvgWindToDirection.shortAngleBetween(agentLatLon.getBearingTo(m_FirstRangePositionLLA)).plus(NavyAngle.ZERO);
                        Speed windSpeedAlongStraightFakePath = m_AvgWindSpeed.times(Math.cos(bearingDiffWindToStraightFakePath.getDoubleValue(Angle.RADIANS)));

                        //Compute time for straight approach path
                        Length distAgentStraightToContact = agentLatLonOffsetByWindDuringRotation.getRangeTo(m_FirstRangePositionLLA);
                        m_ApproachPathGroundSpeed = m_ActualCommandedAirspeed.plus(windSpeedAlongPath);
                        Speed straightFakeApproachPathGroundSpeed = m_ActualCommandedAirspeed.plus(windSpeedAlongStraightFakePath);
                        Time timeAgentPath = distAgentStraightToContact.dividedBy(straightFakeApproachPathGroundSpeed);

                        //Sum time required for rotation and approach path for total time
                        Time timeAgentToFirstRange = timeAgentPath.plus (timeAgentRotate);
                        Time timeFirstRangeToContact = m_ContactPositionLLA.getRangeTo(m_FirstRangePositionLLA).dividedBy(m_ApproachPathGroundSpeed);
                        m_TimeAgentToContact = timeAgentToFirstRange.plus (timeFirstRangeToContact);


                        //If we don't have time to screw around anymore, start the approach.
                        if (m_TimeAgentToContact.getDoubleValue(Time.MILLISECONDS) > timeUntilExplosionMs)
                        {
                            m_DoLoiterApproach = true;
                        }

                        /*System.out.println ("Straight length:" + distAgentStraightToContact);
                        System.out.println ("Angle:" + rotation.getDoubleValue (Angle.DEGREES) + " deg");
                        System.out.println ("Rotate length:" + distAgentRotateToContact);*/
                        long currTimeMs = System.currentTimeMillis();
                        if (currTimeMs - m_LastReportTimeMs > m_ReportUpdateTimeMs)
                        {
                            System.out.println ("Time agent to contact:    " + m_TimeAgentToContact.getDoubleValue(Time.SECONDS) + " sec");
                            m_LastReportTimeMs = currTimeMs;
                        }


                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("1,");
                            loiterCircleLog.write(m_TimeAgentToContact.getDoubleValue(Time.SECONDS) + ",");
                            loiterCircleLog.write(timeUntilExplosionMs/1000 + ",");
                        }
                    }
                    else
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("0,");
                            loiterCircleLog.write(m_TimeAgentToContact.getDoubleValue(Time.SECONDS) + ",");
                            loiterCircleLog.write(timeUntilExplosionMs/1000 + ",");
                        }
                    }



                    //Set altitude of loiter
                    Altitude commandedAltitude = racetrackBelief.getFinalAltitudeMsl();
                    if (!m_StartAltitudeDescent)
                    {
                        Time timeReqdForDescentToFinalAltitude = new Time (Math.max((racetrackBelief.getStandoffAltitudeMsl().getDistanceAbove(racetrackBelief.getFinalAltitudeMsl())).getDoubleValue(Length.METERS), 0)/m_AltitudeDescentRate.getDoubleValue(Speed.METERS_PER_SECOND), Time.SECONDS);
                        Time timeReqdForAscentToStandoffAltitude = new Time (Math.max((racetrackBelief.getStandoffAltitudeMsl().getDistanceAbove(planeAltitude)).getDoubleValue(Length.METERS), 0)/m_AltitudeAscentRate.getDoubleValue(Speed.METERS_PER_SECOND), Time.SECONDS);
                        Time estTimeAgentToFirstRangePoint = new Time (timeUntilExplosionMs, Time.MILLISECONDS).minus(m_DiffContactToFirstRangePoints.dividedBy(m_ActualCommandedAirspeed));
                        Time timeAtEachStep = new Time (m_TimeAtEachAltitudeStepMs*(m_AltitudeSteps.length), Time.MILLISECONDS);

                        if (m_DoLoiterApproach && m_ForceDescentOnLoiterApproach)
                        {
                            //If on approach, force altitude to final altitude immediately.
                            m_StartAltitudeDescent = true;
                            m_AltitudeStepIdx = 0;
                            m_StopAltitudeDescent = false;
                            m_TimeFirstCrossedCurrStepAltMs = 0;
                        }
                        else
                        {
                            //If not on approach, start descent to final altitude when descent rate dictates time required
                            if (estTimeAgentToFirstRangePoint.isLessThan(timeReqdForDescentToFinalAltitude.plus(timeAtEachStep).plus(timeReqdForAscentToStandoffAltitude)))
                            {
                                m_StartAltitudeDescent = true;
                                m_AltitudeStepIdx = 0;
                                m_StopAltitudeDescent = false;
                                m_TimeFirstCrossedCurrStepAltMs = 0;
                            }
                            else
                            {
                                commandedAltitude = racetrackBelief.getStandoffAltitudeMsl();
                            }

                        }


                        //Find the next lowest step and start there as the first step command.  This way, we won't
                        //command higher altitudes once we start the altitude descent path.  We'll just keep
                        //going down, starting at the next altitude step.
                        if (m_StartAltitudeDescent)
                        {
                            double startAltM = racetrackBelief.getStandoffAltitudeMsl().getDoubleValue(Length.METERS);
                            double endAltM = racetrackBelief.getFinalAltitudeMsl().getDoubleValue(Length.METERS);
                            for (int i = 0; i < m_AltitudeSteps.length; i ++)
                            {
                                double stepPercent = m_AltitudeSteps[i];
                                double stepAltM = startAltM - stepPercent*(startAltM - endAltM)/100;

                                if (stepAltM < planeAltitude.getDoubleValue (Length.METERS) || (i == m_AltitudeSteps.length-1))
                                {
                                    m_AltitudeStepIdx = i;
                                    break;
                                }
                            }
                        }
                    }

                    if (m_StartAltitudeDescent && !m_StopAltitudeDescent)
                    {
                        double startAltM = racetrackBelief.getStandoffAltitudeMsl().getDoubleValue(Length.METERS);
                        double endAltM = racetrackBelief.getFinalAltitudeMsl().getDoubleValue(Length.METERS);
                        double stepPercent = m_AltitudeSteps[m_AltitudeStepIdx];
                        double stepAltM = startAltM - stepPercent*(startAltM - endAltM)/100;
                        commandedAltitude = new Altitude(stepAltM, Length.METERS);

                        if (m_TimeFirstCrossedCurrStepAltMs > 0)
                        {
                            //We have reached curr step altitude before, so we are in overshoot/stable mode.  Wait
                            //set time and proceed to next step
                            if (System.currentTimeMillis() - m_TimeFirstCrossedCurrStepAltMs > m_TimeAtEachAltitudeStepMs)
                            {
                                m_AltitudeStepIdx++;
                                if (m_AltitudeStepIdx >= m_AltitudeSteps.length)
                                    m_StopAltitudeDescent = true;
                                m_TimeFirstCrossedCurrStepAltMs = 0;
                            }
                        }
                        else if (commandedAltitude.getDistanceBetween(planeAltitude).isLessThan(m_AltitudeStepBufferDistance.asLength())
                                || planeAltitude.isLowerThan(commandedAltitude))
                        {
                            m_TimeFirstCrossedCurrStepAltMs = System.currentTimeMillis();
                        }

                    }


                    //Set path of loiter
                    if (m_ApproachPathInvalid)
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("0,");
                        }


                        //stay on center loiter because approach path is invalid
                        targetTurnRate_radps = getTurnRateOrbit (centerPositionCart, racetrackBelief.getIsClockwise(), racetrackBelief.getRadius(), telemetryMessage);
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                        populateAutopilotCommandMessage (telemetryMessage, commandedAltitude.getDoubleValue(Length.METERS), targetTurnRate_radps);
                    }
                    //If loiter approach is active -
                    else if(m_OnFinalLoiter || (m_DoLoiterApproach && agentLatLon.getRangeTo(targetPositionLLA).isLessThan(racetrackBelief.getRadius().times(m_FinalRacetrackOrbitBufferPercentage))))
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("1,");
                        }
                        //if on loiter approach and are very close to final orbit/safe position, switch to orbit around target
                        //do regular circle orbit loiter, but reverse direction because we're not offset anymore
                        m_OnFinalLoiter = true;
                        targetTurnRate_radps = getTurnRateOrbit (targetPositionCart, !racetrackBelief.getIsClockwise(), racetrackBelief.getRadius(), telemetryMessage);
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                        populateAutopilotCommandMessage (telemetryMessage, commandedAltitude.getDoubleValue(Length.METERS), targetTurnRate_radps);

                        if (!m_WarnedFinalLoiter && 
                                new NavyAngle(telemetryMessage.trueHeading_rad, Angle.RADIANS).shortAngleBetween(m_BearingOfLoiterApproachPath).isGreaterThan(m_AngleAroundFinalOrbitToWarn))
                        {
                            m_WarnedFinalLoiter = true;

                            //Only show this if the shadow driver is on the GCS, not on the pod.
                            //Should we show it on the GCS if the driver is on the pod???
                            if (m_autopilotInterface.getClass().equals (ShadowAutopilotInterface.class))
                            {
                                new Thread () {
                                    public void run ()
                                    {
                                        JOptionPane.showMessageDialog(null, "WACS reached final loiter orbit without detecting\nexplosion.  Designate new release time or continue final orbit", "Warning", JOptionPane.WARNING_MESSAGE);
                                    }
                                }.start();
                            }
                        }
                    }
                    else if(m_DoLoiterApproach && agentLatLon.getRangeTo(centerPositionLLA).times(Math.cos(centerPositionLLA.getBearingTo(agentLatLon).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS))).isGreaterThan(m_DiffCenterToSafePoints))
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("2,");
                        }
                        //if on loiter approach but are further down path than final orbit, go back to do regular circle orbit loiter, but reverse direction because we're not offset anymore
                        m_OnFinalLoiter = true;
                        targetTurnRate_radps = getTurnRateOrbit (targetPositionCart, !racetrackBelief.getIsClockwise(), racetrackBelief.getRadius(), telemetryMessage);
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                        populateAutopilotCommandMessage (telemetryMessage, commandedAltitude.getDoubleValue(Length.METERS), targetTurnRate_radps);
                    }
                    else if (m_DoLoiterApproach)
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("3,");
                        }
                        //abort the loiter and get on the approach path quickly
                        //System.out.println("Approach!");

                        //Get closest point on path to approach
                        updateContactPositionCart(agentLatLon);

                        //Get vector to follow based on path location and direction
                        RangeBearingHeightOffset targetMovementVector = null;
                        targetMovementVector = computeMovementVectorToFollowPath (m_ContactPositionCart, centerPositionCart);

                        //Turn plane towards target movement vector
                        targetTurnRate_radps = doBasicPlaneHeading (targetMovementVector, telemetryMessage.trueHeading_rad);

                        //Set airpseed to default airspeed
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;

                        LatLonAltPosition closestPointOnPathLLA = m_ClosestPointOnPathCart.asLatLonAltPosition(agentLatLon);
                        NavyAngle heading = new NavyAngle (telemetryMessage.trueHeading_rad, Angle.RADIANS);
                        if (agentLatLon.getRangeTo(closestPointOnPathLLA).isLessThan(m_ApproachPathDistanceBuffer) &&
                            heading.shortAngleBetween(m_BearingOfLoiterApproachPath).isLessThan(m_ApproachPathBearingBuffer.asAngle()))
                        {
                            //On straight leg of approach path, update speed if needed to reach contact point at right time
                            Length remainingRange = agentLatLon.getRangeTo (m_ContactPositionLLA);
                            long remainingTimeNeededMs = (long)remainingRange.dividedBy(m_ApproachPathGroundSpeed).getDoubleValue(Time.MILLISECONDS);
                            long remainingTimeAvailMs = timeUntilExplosionMs;

                            long currTimeMs = System.currentTimeMillis();
                            if (currTimeMs - m_LastReportTimeMs > m_ReportUpdateTimeMs)
                            {
                                System.out.println ("On final approach");
                                System.out.println("Time-  Have:" + remainingTimeAvailMs + "ms, Need:" + remainingTimeNeededMs + "ms, Behind:" + (remainingTimeNeededMs-remainingTimeAvailMs) + "ms");
                                m_LastReportTimeMs = currTimeMs;
                            }
                            
                            if (remainingTimeAvailMs > 0
                                    && ((!m_LastCommandedAirspeedWasFast && remainingTimeNeededMs > (remainingTimeAvailMs + m_ApproachPathTimeBuffer.getDoubleValue(Time.MILLISECONDS)))
                                    ||   (m_LastCommandedAirspeedWasFast && remainingTimeNeededMs > (remainingTimeAvailMs + m_ApproachPathTimeBuffer.getDoubleValue(Time.MILLISECONDS)/2))))
                            {
                                //We expect to hit the contact point outside of our buffer time, so we should speed up
                                //to get closer to the contact point

                                //However, we should be going slow by the time we reach the first range point, so overriede the speed
                                //increase if we are close enough.  Dependent on deceleration time.
                                Time estTimeAgentToFirstRangePoint = new Time (timeUntilExplosionMs, Time.MILLISECONDS).minus(m_DiffContactToFirstRangePoints.dividedBy(m_ApproachPathGroundSpeed));
                                Time timeReqdForDecelToFinalSpeed = new Time (Math.max((m_DesiredCommandedAirspeed.times (m_ApproachPathMaxSpeedIncreaseScale-1)).getDoubleValue(Speed.METERS_PER_SECOND), 0)/(m_AirspeedDecelRate.getDoubleValue(Acceleration.KNOTS_PER_SECOND)*0.5144444444), Time.SECONDS);
                                if (estTimeAgentToFirstRangePoint.isLessThan(timeReqdForDecelToFinalSpeed))
                                {
                                    m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                                    m_LastCommandedAirspeedWasFast = false;
                                }
                                else
                                {
                                    m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed.times (m_ApproachPathMaxSpeedIncreaseScale);
                                    //System.out.println ("Speed up!!!");
                                    m_LastCommandedAirspeedWasFast = true;
                                }

                            }
                            else
                            {
                                m_LastCommandedAirspeedWasFast = false;
                            }
                        }
                        else
                        {
                            m_LastCommandedAirspeedWasFast = false;
                        }

                        //Make command
                        populateAutopilotCommandMessage (telemetryMessage, commandedAltitude.getDoubleValue(Length.METERS), targetTurnRate_radps);

                    }
                    else
                    {
                        if (loiterCircleLog != null)
                        {
                            loiterCircleLog.write("4,");
                        }
                        //do regular circle orbit loiter
                        targetTurnRate_radps = getTurnRateOrbit (centerPositionCart, racetrackBelief.getIsClockwise(), racetrackBelief.getRadius(), telemetryMessage);
                        m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                        populateAutopilotCommandMessage (telemetryMessage, commandedAltitude.getDoubleValue(Length.METERS), targetTurnRate_radps);
                    }

                }
            }

            if (loiterCircleLog != null)
            {
                boolean autopilotControlEnabled = false;
                EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)m_beliefManager.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                if (actualControl != null && actualControl.getAllow())
                    autopilotControlEnabled = true;
                
                loiterCircleLog.write((autopilotControlEnabled?1:0) + ",");
                loiterCircleLog.write("\n");
                loiterCircleLog.flush();
            }
        
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



    }

    public void updateCommandDataIntercept(CircularOrbitBelief circularOrbitBelief, TelemetryMessage telemetryMessage, Altitude planeAltitude)
    {
        //
        // Calculate vector to circular orbit
        //
        LatLonAltPosition agentLatLon = new LatLonAltPosition(new Latitude(telemetryMessage.latitude_rad, Angle.RADIANS),
                                                              new Longitude(telemetryMessage.longitude_rad, Angle.RADIANS),
                                                              planeAltitude);

        double targetTurnRate_radps = 0;

        if (agentLatLon == null || circularOrbitBelief == null)
        {
            //do nothing
        }
        else
        {
            // get the center position, defaulting to the set one
            CartesianPosition centerPosition = circularOrbitBelief.getPosition().asLatLonAltPosition().asCartesianPosition(agentLatLon);
            centerPosition = new CartesianPosition(centerPosition.getX(), centerPosition.getY(), new Length(0, Length.METERS));

            if (centerPosition != null)
            {   
                //do regular circle orbit
                targetTurnRate_radps = getTurnRateOrbit (centerPosition, circularOrbitBelief.getIsClockwise(), circularOrbitBelief.getRadius(), telemetryMessage);
                
                //form command
                populateAutopilotCommandMessage (telemetryMessage, circularOrbitBelief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS), targetTurnRate_radps);
            }
        }
    }

    private void populateAutopilotCommandMessage (TelemetryMessage telemetryMessage, double altitudeCommandM, double targetTurnRate_radps)
    {
        synchronized (m_commandMessageLock)
        {
            m_autopilotCommandMessage.populateSpecificDetails (telemetryMessage, m_beliefManager);
            m_autopilotCommandMessage.airspeedCommand_mps = m_ActualCommandedAirspeed.getDoubleValue(Speed.METERS_PER_SECOND);
            if (altitudeCommandM > telemetryMessage.barometricAltitudeMsl_m + m_MaximumAltitudeLeadingCommand_m)
                altitudeCommandM = telemetryMessage.barometricAltitudeMsl_m + m_MaximumAltitudeLeadingCommand_m;
            if (altitudeCommandM < telemetryMessage.barometricAltitudeMsl_m - m_MaximumAltitudeLeadingCommand_m)
                altitudeCommandM = telemetryMessage.barometricAltitudeMsl_m - m_MaximumAltitudeLeadingCommand_m;
            m_autopilotCommandMessage.altitudeCommand_m = m_safetyBox.getSafeOrbitAltitudeMSLFromMSL (telemetryMessage.latitude_rad/MathConstants.DEG2RAD, telemetryMessage.longitude_rad/MathConstants.DEG2RAD, altitudeCommandM, null);
            
            //
            // Use laser altimeter to correct for barometric drift. Only correct upwards!
            //
            if (m_barometricAltitudeErrorMovingAverage_m > 0)
            {
                m_autopilotCommandMessage.altitudeCommand_m += m_barometricAltitudeErrorMovingAverage_m;
            }

            /*SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
            if (safetyBoxBelief != null)
            {
                if (m_autopilotCommandMessage.altitudeCommand_m < safetyBoxBelief.getMinAltitudeMSL_m())
                {
                    m_autopilotCommandMessage.altitudeCommand_m = safetyBoxBelief.getMinAltitudeMSL_m();
                }
                else if (m_autopilotCommandMessage.altitudeCommand_m > safetyBoxBelief.getMaxAltitudeMSL_m())
                {
                    m_autopilotCommandMessage.altitudeCommand_m = safetyBoxBelief.getMaxAltitudeMSL_m();
                }
            }*/

            // Calculate bank angle required for desired turn rate
            m_autopilotCommandMessage.rollCommand_rad = Math.atan((targetTurnRate_radps * telemetryMessage.indicatedAirspeed_mps) / 9.8);
            if (m_autopilotCommandMessage.rollCommand_rad > Math.PI)
            {
                m_autopilotCommandMessage.rollCommand_rad -= (2 * Math.PI);
            }
            if (m_autopilotCommandMessage.rollCommand_rad > m_maxBankAngle_rad)
            {
                m_autopilotCommandMessage.rollCommand_rad = m_maxBankAngle_rad;
            }
            else if (m_autopilotCommandMessage.rollCommand_rad < -m_maxBankAngle_rad)
            {
                m_autopilotCommandMessage.rollCommand_rad = -m_maxBankAngle_rad;
            }
            
        }
    }


}
