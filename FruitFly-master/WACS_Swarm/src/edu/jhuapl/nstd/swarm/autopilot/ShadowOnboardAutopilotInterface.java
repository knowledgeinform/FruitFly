/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.TASEPointingAnglesBelief;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TestCircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.display.DisplayUnitsManager;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.wacs.WacsMode;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class ShadowOnboardAutopilotInterface implements KnobsAutopilotInterface
{
    public static ShadowOnboardAutopilotInterface s_instance = null;
    
    static public class ShadowCommandMessage extends CommandMessage
    {
        public ShadowCommandMessage()
        {
            m_WacsMode = WacsMode.NO_MODE;
        }
        
        @Override
        public void populateSpecificDetails(TelemetryMessage telemetryMessage, BeliefManager belMgr) 
        {
            this.timestamp_ms = System.currentTimeMillis();
            
            AgentModeActualBelief agentMode = (AgentModeActualBelief)belMgr.get(AgentModeActualBelief.BELIEF_NAME);
            this.m_WacsMode = WacsMode.NO_MODE;
            if (agentMode != null)
            {
                if (agentMode.getMode(WACSAgent.AGENTNAME) != null)
                {
                    if (agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
                    {
                        this.m_WacsMode = WacsMode.INTERCEPT;
                    }
                    else if(agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                    {
                        this.m_WacsMode = WacsMode.LOITER;
                    }
                }
            }
            
            if (this.m_WacsMode == WacsMode.NO_MODE)
                return;
            
            //Get current commanded orbit, based on which mode we're in
            if (this.m_WacsMode.equals(WacsMode.LOITER))
            {
                RacetrackOrbitBelief racetrackBelief = null;
                //In loiter mode, put current orbit as racetrack orbit
                if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                {
                    TestCircularOrbitBelief tobBelief = (TestCircularOrbitBelief) belMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
                    if (tobBelief != null && tobBelief.isRacetrack())
                    {
                        racetrackBelief = tobBelief.asRacetrackOrbitBelief();
                    }
                }
                if (racetrackBelief == null)
                {
                    racetrackBelief = (RacetrackOrbitBelief) belMgr.get(RacetrackOrbitBelief.BELIEF_NAME);
                }
                
                if (racetrackBelief != null)
                {
                    this.m_CurrentOrbitRadiusM = racetrackBelief.getRadius().getDoubleValue(Length.METERS);
                    this.m_CurrentOrbitCWDir = racetrackBelief.getIsClockwise();
                    this.m_CurrentOrbitLatitudeRad = racetrackBelief.getLatitude1().getDoubleValue(Angle.RADIANS);
                    this.m_CurrentOrbitLongitudeRad = racetrackBelief.getLongitude1().getDoubleValue(Angle.RADIANS);
                    this.m_CurrentOrbitAltitudeMslM = racetrackBelief.getFinalAltitudeMsl().getDoubleValue(Length.METERS);
                }
            }
            else if (this.m_WacsMode.equals(WacsMode.INTERCEPT))
            {
                //In intercept mode, put current orbit as circular orbit
                CircularOrbitBelief orbitBelief = null;
                //In loiter mode, put current orbit as racetrack orbit
                if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                {
                    TestCircularOrbitBelief tobBelief = (TestCircularOrbitBelief) belMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
                    if (tobBelief != null && !tobBelief.isRacetrack())
                    {
                        orbitBelief = tobBelief.asCircularOrbitBelief();
                    }
                }
                if (orbitBelief == null)
                {
                    orbitBelief = (CircularOrbitBelief) belMgr.get(CircularOrbitBelief.BELIEF_NAME);
                }
                
                if (orbitBelief != null)
                {
                    this.m_CurrentOrbitRadiusM = orbitBelief.getRadius().getDoubleValue(Length.METERS);
                    this.m_CurrentOrbitCWDir = orbitBelief.getIsClockwise();
                    this.m_CurrentOrbitLatitudeRad = orbitBelief.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.RADIANS);
                    this.m_CurrentOrbitLongitudeRad = orbitBelief.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.RADIANS);
                    this.m_CurrentOrbitAltitudeMslM = orbitBelief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
                }
            }

            this.m_CurrentOrbitAirspeedMps = new Speed (Config.getConfig().getPropertyAsDouble("ShadowDriver.commandedAirspeed_knots", 65.0), Speed.KNOTS).getDoubleValue(Speed.METERS_PER_SECOND);
             
            TASEPointingAnglesBelief taseAngBlf = (TASEPointingAnglesBelief)belMgr.get(TASEPointingAnglesBelief.BELIEF_NAME);
            if (taseAngBlf != null)
            {
                this.m_GimbalElevationRad = taseAngBlf.getTASEPointingAngles().Tilt;
                this.m_GimbalAzimuthRad = taseAngBlf.getTASEPointingAngles().Pan;
            }
            else
            {
                this.m_GimbalElevationRad = 0;
                this.m_GimbalAzimuthRad = 0;
            }
            
            PiccoloTelemetryBelief picbel = (PiccoloTelemetryBelief)belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if(picbel != null)
            {
                Pic_Telemetry pictel = picbel.getPiccoloTelemetry();
                
                if (picbel.getTimeStamp().after(new Date (System.currentTimeMillis() - 5000)))
                    this.m_PiccoloOperational = true;
                else
                    this.m_PiccoloOperational = false;
                
                this.m_LaserAltitudeAglM = picbel.getPiccoloTelemetry().AltLaserValid ? (picbel.getPiccoloTelemetry().AltLaser_m) : -1;
                
                this.m_WindSpeedMps = Math.sqrt(pictel.WindSouth*pictel.WindSouth +pictel.WindWest*pictel.WindWest);
                this.m_WindDirFromRad = Math.atan2(- pictel.WindWest,-pictel.WindSouth);
                this.m_WindEstimateConverged = true;
            }
            
            TASETelemetryBelief taseBelief = (TASETelemetryBelief)belMgr.get(TASETelemetryBelief.BELIEF_NAME);
            if(taseBelief != null)
            {
                if (taseBelief.getTimeStamp().after(new Date (System.currentTimeMillis() - 5000)))
                    this.m_TaseOperational = true;
                else
                    this.m_TaseOperational = false;
            }
        }
    
        
        public void copyFrom (ShadowCommandMessage copyFrom)
        {
            if (copyFrom == null)
                return;
            
            this.timestamp_ms = copyFrom.timestamp_ms;
            this.rollCommand_rad = copyFrom.rollCommand_rad;
            this.airspeedCommand_mps = copyFrom.airspeedCommand_mps;
            this.altitudeCommand_m = copyFrom.altitudeCommand_m;
            this.m_CurrentOrbitRadiusM = copyFrom.m_CurrentOrbitRadiusM;
            this.m_CurrentOrbitCWDir = copyFrom.m_CurrentOrbitCWDir;
            this.m_CurrentOrbitAltitudeMslM = copyFrom.m_CurrentOrbitAltitudeMslM;
            this.m_CurrentOrbitAirspeedMps = copyFrom.m_CurrentOrbitAirspeedMps;
            this.m_CurrentOrbitLatitudeRad = copyFrom.m_CurrentOrbitLatitudeRad;
            this.m_CurrentOrbitLongitudeRad = copyFrom.m_CurrentOrbitLongitudeRad;
            this.m_GimbalElevationRad = copyFrom.m_GimbalElevationRad;
            this.m_GimbalAzimuthRad = copyFrom.m_GimbalAzimuthRad;
            this.m_LaserAltitudeAglM = copyFrom.m_LaserAltitudeAglM;
            this.m_WindSpeedMps = copyFrom.m_WindSpeedMps;
            this.m_WindDirFromRad = copyFrom.m_WindDirFromRad;
            this.m_WacsMode = copyFrom.m_WacsMode;
            this.m_PiccoloOperational = copyFrom.m_PiccoloOperational;
            this.m_TaseOperational = copyFrom.m_TaseOperational;
            this.m_WindEstimateConverged = copyFrom.m_WindEstimateConverged;
        }
        
        //public long m_TimestampMs;
        //public double rollCommand_rad;
        //public double airspeedCommand_mps;
        //public double altitudeCommand_m;
        public double m_CurrentOrbitRadiusM;
        public boolean m_CurrentOrbitCWDir;
        public double m_CurrentOrbitAltitudeMslM;
        public double m_CurrentOrbitAirspeedMps;
        public double m_CurrentOrbitLatitudeRad;
        public double m_CurrentOrbitLongitudeRad;
        public double m_GimbalElevationRad;
        public double m_GimbalAzimuthRad;
        public double m_LaserAltitudeAglM;
        public double m_WindSpeedMps;
        public double m_WindDirFromRad;
        public WacsMode m_WacsMode;
        public boolean m_PiccoloOperational;
        public boolean m_TaseOperational;
        public boolean m_WindEstimateConverged;
    }

    static public class ShadowTelemetryMessage extends TelemetryMessage
    {
        public ShadowTelemetryMessage()
        {
            m_CommandedWacsMode = WacsMode.NO_MODE;
            this.m_LoiterValid = false;
            this.m_StrikeValid = false;
        }
        
        public void copyFrom (ShadowTelemetryMessage copyFrom)
        {
            if (copyFrom == null)
                return;
            
            this.timestamp_ms = copyFrom.timestamp_ms;
            this.m_CommandedAirspeedMps = copyFrom.m_CommandedAirspeedMps;
            this.m_LoiterLatitudeRad = copyFrom.m_LoiterLatitudeRad;
            this.m_LoiterLongitudeRad = copyFrom.m_LoiterLongitudeRad;
            this.m_LoiterRadiusM = copyFrom.m_LoiterRadiusM;
            this.m_LoiterAltitudeMslM = copyFrom.m_LoiterAltitudeMslM;
            this.m_LoiterCWDir = copyFrom.m_LoiterCWDir;
            this.m_CommandedGimbalMode = copyFrom.m_CommandedGimbalMode;
            this.m_CommandedWacsMode = copyFrom.m_CommandedWacsMode;
            this.m_LoiterValid = copyFrom.m_LoiterValid;
            this.m_StrikeLatitudeRad = copyFrom.m_StrikeLatitudeRad;
            this.m_StrikeLongitudeRad = copyFrom.m_StrikeLongitudeRad;
            this.m_StrikeAltitudeMslM = copyFrom.m_StrikeAltitudeMslM;
            this.m_StrikeValid = copyFrom.m_StrikeValid;

            this.longitude_rad = copyFrom.longitude_rad;
            this.latitude_rad = copyFrom.latitude_rad;
            this.barometricAltitudeMsl_m = copyFrom.barometricAltitudeMsl_m;
            this.roll_rad = copyFrom.roll_rad;
            this.pitch_rad = copyFrom.pitch_rad;
            this.trueHeading_rad = copyFrom.trueHeading_rad;
            this.trueAirspeed_mps = copyFrom.trueAirspeed_mps;
            this.indicatedAirspeed_mps = copyFrom.indicatedAirspeed_mps;
            this.groundSpeedNorth_mps = copyFrom.groundSpeedNorth_mps;
            this.groundSpeedEast_mps = copyFrom.groundSpeedEast_mps;
            this.m_VerticalVelocityMps = copyFrom.m_VerticalVelocityMps;
            this.m_TCDLLinkActive = copyFrom.m_TCDLLinkActive;
            this.m_SatCommThrottling = copyFrom.m_SatCommThrottling;
        }

        //public long m_TimestampMs;
        public double m_CommandedAirspeedMps;
        public double m_LoiterLatitudeRad;
        public double m_LoiterLongitudeRad;
        public double m_LoiterRadiusM;
        public double m_LoiterAltitudeMslM;
        public boolean m_LoiterCWDir;
        public int m_CommandedGimbalMode;
        public WacsMode m_CommandedWacsMode;
        public boolean m_LoiterValid;
        public double m_StrikeLatitudeRad;
        public double m_StrikeLongitudeRad;
        public double m_StrikeAltitudeMslM;
        public boolean m_StrikeValid;
        
        //public double longitude_rad;
        //public double latitude_rad;
        //public double barometricAltitudeMsl_m;
        //public double m_RollRad;
        //public double pitch_rad;
        //public double trueHeading_rad;
        //public double trueAirspeed_mps;
        //public double indicatedAirspeed_mps;
        //public double groundSpeedNorth_mps;
        //public double groundSpeedEast_mps;
        public double m_VerticalVelocityMps;
        public boolean m_TCDLLinkActive;
        public boolean m_SatCommThrottling;
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
                            
                            m_logFile = new FileWriter("ShadowLogs/ShadowInterface_" + logStartTime_ms + ".csv");
                            m_logFile.write("Time (sec), Commanded Roll (rad), Reported Roll (rad), Commanded Altitude AGL (m), Reported Altitude AGL (m), Latitude (rad), Longitude (rad)\n");
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
    
    private final Object m_lock = new Object();
    private ShadowCommandMessage m_commandMessage = null;
    private ShadowTelemetryMessage m_telemetryMessage = null;
    private LogThread m_logThread = new LogThread();
    private boolean m_logData;
    private FileWriter m_logFile;
    private BeliefManager m_BeliefManager;
    
    public ShadowOnboardAutopilotInterface(BeliefManager belMgr)
    {        
        m_logData = Config.getConfig().getPropertyAsBoolean("ShadowAutopilotInterface.logData");
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
        //The current command message sent to the autopilot is being stored here for reference and for logging.  Values in it are based
        //on the current beliefs onboard the UAV.
        
        ShadowCommandMessage copyFrom = (ShadowCommandMessage)copyFromBase;
        synchronized (m_lock)
        {
            if (m_commandMessage == null)
            {
                m_commandMessage = new ShadowCommandMessage();
            }
            
            m_commandMessage.copyFrom (copyFrom);
        }
    }
    
    public void setTelemetryMessage (TelemetryMessage copyFromBase)
    {
        //The current telemetry message received from the autopilot is being stored here for reference and for logging.  
        //Also, values in it need to update the belief network, potentially.  For example, if the telemetry message indicates
        //we should switch to loiter mode, then update the belief.  This needs to be reconciled with changes from the WACS GCS
        //through the belief manager
        
        ShadowTelemetryMessage copyFrom = (ShadowTelemetryMessage)copyFromBase;
        synchronized (m_lock)
        {
            if (m_telemetryMessage == null)
            {
                m_telemetryMessage = new ShadowTelemetryMessage();
            }
            
            //We are updating the current telemetry message.  We need to check the fields being set vs the existing beliefs
            //that relate to them to determine if the belief should be updated
            
            //If commanded wacs mode is not the same thing that Shadow commanded last time, then update it.
            if (m_telemetryMessage == null || m_telemetryMessage.m_CommandedWacsMode.getValue() != copyFrom.m_CommandedWacsMode.getValue())
            {
                boolean sameValue = false;
                AgentModeCommandedBelief extgBlf = (AgentModeCommandedBelief)m_BeliefManager.get(AgentModeCommandedBelief.BELIEF_NAME);
                if (extgBlf != null)
                {
                    Mode mode = extgBlf.getMode(WACSAgent.AGENTNAME);
                    if (mode != null)
                    {
                        if (mode.getName().equals (copyFrom.m_CommandedWacsMode.getModeString()))
                            sameValue = true;
                    }
                }
                
                //Check to be sure we're not updating a belief to the same value, just so don't needlessly update things/timestamps
                if (!sameValue)
                {
                    boolean skip = false;
                    if (copyFrom.m_CommandedWacsMode.equals (WacsMode.INTERCEPT))
                    {
                        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)m_BeliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
                        if (allowInterceptBlf == null || !allowInterceptBlf.getAllow())
                            skip = true;
                        else
                        {
                            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                            TargetActualBelief targets = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
                            if(targets != null)
                            {
                                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);
                                if(positionTimeName != null)
                                {
                                    m_BeliefManager.put(new ExplosionBelief(positionTimeName.getPosition(), copyFrom.timestamp_ms));
                                }
                            }
                        }
                    }
                    
                    if (!skip)
                    {
                        AgentModeCommandedBelief agModeBlf = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, copyFrom.m_CommandedWacsMode.getModeString(), new Date (copyFrom.timestamp_ms));
                        m_BeliefManager.put(agModeBlf);
                    }
                }
            }
            
            //If the loiter location/altitude or strike location have been changed, then we need to adjust the race track defintion to fit around it
            if (m_telemetryMessage == null || !m_telemetryMessage.m_LoiterValid || !m_telemetryMessage.m_StrikeValid || 
                    (m_telemetryMessage.m_LoiterLatitudeRad != copyFrom.m_LoiterLatitudeRad ||
                    m_telemetryMessage.m_LoiterLongitudeRad != copyFrom.m_LoiterLongitudeRad || m_telemetryMessage.m_LoiterAltitudeMslM != copyFrom.m_LoiterAltitudeMslM ||
                    m_telemetryMessage.m_StrikeLatitudeRad != copyFrom.m_StrikeLatitudeRad || m_telemetryMessage.m_StrikeLongitudeRad != copyFrom.m_StrikeLongitudeRad))
            {
                if (copyFrom.m_LoiterValid && copyFrom.m_StrikeValid)
                {
                    //System.out.println ("ShadowOnboardAutopilot update racetrack definition: " + copyFrom.m_LoiterAltitudeMslM);
                    RacetrackDefinitionActualBelief extgBelief = (RacetrackDefinitionActualBelief)m_BeliefManager.get(RacetrackDefinitionActualBelief.BELIEF_NAME);

                    LatLonAltPosition desiredLoiterPosition = new LatLonAltPosition(new Latitude(copyFrom.m_LoiterLatitudeRad, Angle.RADIANS),
                                                                            new Longitude(copyFrom.m_LoiterLongitudeRad, Angle.RADIANS),
                                                                            Altitude.ZERO);
                 
                    //TODO:  Check if strike position is within loiter position, align if so?
                    
                    boolean sameValue = false;
                    if (extgBelief != null)
                    {
                        if (desiredLoiterPosition.getLatitude().equals(extgBelief.getStartPosition().getLatitude()) && desiredLoiterPosition.getLatitude().equals(extgBelief.getStartPosition().getLatitude()))
                            sameValue = true;
                    }
                    
                    //Check to be sure we're not updating a belief to the same value, just so don't needlessly update things/timestamps
                    if (!sameValue)
                    {
                        RacetrackDefinitionCommandedBelief newDefBelief = new RacetrackDefinitionCommandedBelief(desiredLoiterPosition);
                        m_BeliefManager.put(newDefBelief);
                    }
                }
            }
            
            //Check if strike location updated, then update the belief
            if (m_telemetryMessage == null || !m_telemetryMessage.m_StrikeValid || 
                                                (m_telemetryMessage.m_StrikeLatitudeRad != copyFrom.m_StrikeLatitudeRad ||
                                                m_telemetryMessage.m_StrikeLongitudeRad != copyFrom.m_StrikeLongitudeRad ||
                                                m_telemetryMessage.m_StrikeAltitudeMslM != copyFrom.m_StrikeAltitudeMslM))
            {
                if (copyFrom.m_StrikeValid)
                {
                    LatLonAltPosition gimbalPosition = new LatLonAltPosition(new Latitude(copyFrom.m_StrikeLatitudeRad, Angle.RADIANS), 
                                                                                new Longitude(copyFrom.m_StrikeLongitudeRad, Angle.RADIANS),
                                                                                Altitude.ZERO);
                    Altitude gimbalAltitudeMsl = new Altitude (copyFrom.m_StrikeAltitudeMslM, Length.METERS);
                    gimbalPosition = new LatLonAltPosition(gimbalPosition.getLatitude(), gimbalPosition.getLongitude(), gimbalAltitudeMsl);
                    
                    boolean sameVal = false;
                    String targetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                    TargetCommandedBelief extgBelief = (TargetCommandedBelief)m_BeliefManager.get(TargetCommandedBelief.BELIEF_NAME);
                    if (extgBelief != null)
                    {
                        if (extgBelief.getPositionTimeName(targetName) != null)
                        {
                            LatLonAltPosition targetPos = extgBelief.getPositionTimeName(targetName).getPosition().asLatLonAltPosition();
                            if (targetPos.getLatitude().equals(gimbalPosition.getLatitude()) && targetPos.getLongitude().equals(gimbalPosition.getLongitude()))
                                sameVal = true;
                        }
                    }
                    
                    //Check to be sure we're not updating a belief to the same value, just so don't needlessly update things/timestamps
                    if (!sameVal)
                    {
                        //System.out.println ("ShadowOnboardAutopilot: Update gimbal target= " + DtedGlobalMap.getDted().getJlibAltitude(gimbalPosition).getDoubleValue (Length.METERS) + " at " + gimbalPosition.toString());
                        //System.out.println ("ShadowOnboardAutopilot: Update gimbal target time: " + copyFrom.timestamp_ms);
                        
                        m_BeliefManager.put(new TargetCommandedBelief(WACSAgent.AGENTNAME,
                                gimbalPosition,
                                Length.ZERO,
                                targetName,
                                new Date (copyFrom.timestamp_ms)));
                    }
                }
                
            }
            
            //If the waypoint settings (radius, altitudes) have changed, update the belief
            if (m_telemetryMessage == null || !m_telemetryMessage.m_LoiterValid || 
                    (m_telemetryMessage.m_LoiterRadiusM != copyFrom.m_LoiterRadiusM || m_telemetryMessage.m_LoiterAltitudeMslM != copyFrom.m_LoiterAltitudeMslM || 
                    m_telemetryMessage.m_StrikeLatitudeRad != copyFrom.m_StrikeLatitudeRad || m_telemetryMessage.m_StrikeLongitudeRad != copyFrom.m_StrikeLongitudeRad))
            {      
                if (copyFrom.m_StrikeValid && copyFrom.m_LoiterValid)
                {
                    WACSWaypointCommandedBelief extgBelief = (WACSWaypointCommandedBelief)m_BeliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME);

                    //Default strike position (in case the belief doesn't exist yet)
                    LatLonAltPosition strikePosition = new LatLonAltPosition(new Latitude(copyFrom.m_StrikeLatitudeRad, Angle.RADIANS),
                                                                            new Longitude(copyFrom.m_StrikeLongitudeRad, Angle.RADIANS),
                                                                            Altitude.ZERO);
                    
                    Altitude strikeAltitudeAgl = new Altitude (copyFrom.m_LoiterAltitudeMslM, Length.METERS).minus(DtedGlobalMap.getDted().getJlibAltitude(strikePosition).asLength());

                    //System.out.println ("ShadowOnboardAutopilot: Change wwb for msl alt = " + copyFrom.m_LoiterAltitudeMslM + " to agl of " + strikeAltitudeAgl.getDoubleValue(Length.METERS) + "; terrain height = " + DtedGlobalMap.getDted().getJlibAltitude(strikePosition).getDoubleValue (Length.METERS) + " at " + strikePosition.toString());
                    //System.out.println ("ShadowOnboardAutopilot: strike time: " + copyFrom.timestamp_ms);
                    WACSWaypointCommandedBelief wwBelief;
                    if (extgBelief != null)
                    {
                        wwBelief = new WACSWaypointCommandedBelief (WACSAgent.AGENTNAME, 
                                                            extgBelief.getIntersectAltitude(),
                                                            extgBelief.getIntersectRadius(),
                                                            strikeAltitudeAgl,
                                                            extgBelief.getStandoffLoiterAltitude(),
                                                            new Length (copyFrom.m_LoiterRadiusM, Length.METERS),
                                                            new Date (copyFrom.timestamp_ms));
                    }
                    else
                    {
                        wwBelief = new WACSWaypointCommandedBelief (WACSAgent.AGENTNAME, 
                                                            strikeAltitudeAgl,
                                                            new Length (copyFrom.m_LoiterRadiusM, Length.METERS),
                                                            strikeAltitudeAgl,
                                                            strikeAltitudeAgl,
                                                            new Length (copyFrom.m_LoiterRadiusM, Length.METERS),
                                                            new Date (copyFrom.timestamp_ms));
                    }
                
                    boolean sameValue = false;
                    if (extgBelief != null && wwBelief.getStandoffLoiterAltitude().equals (extgBelief.getStandoffLoiterAltitude()) && 
                            wwBelief.getFinalLoiterAltitude().equals (extgBelief.getFinalLoiterAltitude()) &&
                            wwBelief.getIntersectAltitude().equals (extgBelief.getIntersectAltitude()) &&
                            wwBelief.getIntersectRadius().equals (extgBelief.getIntersectRadius()) &&
                            wwBelief.getLoiterRadius().equals (extgBelief.getLoiterRadius()))
                            sameValue = true;
                    
                    //Check to be sure we're not updating a belief to the same value, just so don't needlessly update things/timestamps
                    if (!sameValue)
                        m_BeliefManager.put(wwBelief);
                }
            }
              
            m_telemetryMessage.copyFrom (copyFrom);
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
                copyTo.copyFrom(m_commandMessage);
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
                copyTo.copyFrom(m_telemetryMessage);
                return true;
            }
            else
            {
                return false;
            }
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
