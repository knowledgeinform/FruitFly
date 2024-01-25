/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm;

import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.swarm.action.MissionManagerAction;
import edu.jhuapl.nstd.swarm.action.MissionManagerAction.CommandDesiredSettings;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.CanberraDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.MissionActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.MissionCommandedStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.SatCommStatusBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TargetRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.TimeName;
import edu.jhuapl.nstd.swarm.belief.TimeNameBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WacsHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceActualBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataRequiredBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author humphjc1
 */
public class MissionErrorManager extends Thread
{
    /**
     * Singleton instance
     */
    private static MissionErrorManager m_MissionErrorManager = null;
    
    public static synchronized MissionErrorManager getInstance(BeliefManager belMgr)
    {
        if (m_MissionErrorManager == null)
        {
            m_MissionErrorManager = new MissionErrorManager();
        }
        
        if (m_MissionErrorManager.m_BeliefManager == null)
            m_MissionErrorManager.m_BeliefManager = belMgr;
        return m_MissionErrorManager;
    }
    
    /**
     * Get singleton instance of Mission Error Manager
     * @return 
     */
    public static synchronized MissionErrorManager getInstance()
    {
        return MissionErrorManager.getInstance(null);
    }
    
    public static final int ALARMLEVEL_NOALARM = -1;
    public static final int ALARMLEVEL_NOTIFICATION = 0;
    public static final int ALARMLEVEL_WARNING = 1;
    public static final int ALARMLEVEL_ERROR = 2;
    
    
    public static final int UNKNOWN_ERRORCODE = 0;
    
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_MISSIONMODE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded mission mode mismatch", MissionCommandedStateBelief.class, MissionActualStateBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_ANACONDAMODE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded chemical sensor state mismatch", AnacondaStateBelief.class, AnacondaActualStateBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_IBACMODE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded particle sensor state mismatch", IbacStateBelief.class, IbacActualStateBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_C100MODE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded particle collector state mismatch", ParticleCollectorStateBelief.class, ParticleCollectorActualStateBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_BLADEWERXMODE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded alpha sensor state mismatch", AlphaSensorStateBelief.class, AlphaSensorActualStateBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_ALLOWINTERCEPT_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded allow intercept state mismatch", AllowInterceptCommandedBelief.class, AllowInterceptActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_IRRECORDSTATE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded IR record state mismatch", VideoClientRecorderStatusBelief.class, VideoClientRecorderStatusBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_WACSWAYPOINT_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded flight settings mismatch", WACSWaypointCommandedBelief.class, WACSWaypointActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_RACETRACKDEFINITION_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded loiter position mismatch", RacetrackDefinitionCommandedBelief.class, RacetrackDefinitionActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_TRACKINGTYPE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded cloud tracking state mismatch", ParticleCloudTrackingTypeCommandedBelief.class, ParticleCloudTrackingTypeActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_WINDSOURCE_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded wind source mismatch", WindEstimateSourceCommandedBelief.class, WindEstimateSourceActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_ENABLEAUTOPILOTCONTROL_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded autopilot control mismatch", EnableAutopilotControlCommandedBelief.class, EnableAutopilotControlActualBelief.class);
    public static final ErrorCodeCommandedBelief COMMANDACTUALSYNC_EXPLOSIONTIME_ERRORCODE = new ErrorCodeCommandedBelief ("Commanded explosion time mismatch", ExplosionTimeCommandedBelief.class, ExplosionTimeActualBelief.class);

    public static final ErrorCodeCommandedTimeNameBelief COMMANDACTUALSYNC_WACSMODE_ERRORCODE = new ErrorCodeCommandedTimeNameBelief ("Commanded WACS state mismatch", AgentModeCommandedBelief.class, AgentModeActualBelief.class, WACSAgent.AGENTNAME);
    public static final ErrorCodeCommandedTimeNameBelief COMMANDACTUALSYNC_TARGETBELIEF_ERRORCODE = new ErrorCodeCommandedTimeNameBelief ("Commanded target location mismatch", TargetCommandedBelief.class, TargetActualBelief.class, Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget"));
    
    public static final ErrorCodeInfoNeeded INFONEEDED_EXPTIMELOITER_ERRORCODE = new ErrorCodeInfoNeeded ("Explosion time estimate needed in loiter mode", ExplosionTimeRequiredBelief.class, ExplosionTimeActualBelief.class);
    public static final ErrorCodeInfoNeeded INFONEEDED_ZEROAIRDATA_ERRORCODE = new ErrorCodeInfoNeeded ("Air data must be calibrated before flight", ZeroAirDataRequiredBelief.class, ZeroAirDataBelief.class);
    public static final ErrorCodeInfoNeeded INFONEEDED_TARGETLOCATION_ERRORCODE = new ErrorCodeInfoNeeded ("Strike location required", TargetRequiredBelief.class, TargetActualBelief.class);
    public static final ErrorCodeInfoNeeded INFONEEDED_LOITERLOCATION_ERRORCODE = new ErrorCodeInfoNeeded ("UAS loiter location required", RacetrackDefinitionRequiredBelief.class, RacetrackDefinitionActualBelief.class);
    
    public static final ErrorCodeHeartbeatSerialBelief PODHEARTBEAT_ANACONDARECV_ERRORCODE = new ErrorCodeHeartbeatSerialBelief ("Chemical sensor communication timeout: ", CBRNHeartbeatBelief.SERIAL_E, cbrnPodsInterface.COLLECTOR_POD);
    public static final ErrorCodeHeartbeatSerialBelief PODHEARTBEAT_IBACRECV_ERRORCODE = new ErrorCodeHeartbeatSerialBelief ("Particle sensor communication timeout: ", CBRNHeartbeatBelief.SERIAL_E, cbrnPodsInterface.TRACKER_POD);
    public static final ErrorCodeHeartbeatSerialBelief PODHEARTBEAT_C100RECV_ERRORCODE = new ErrorCodeHeartbeatSerialBelief ("Particle collector communication timeout: ", CBRNHeartbeatBelief.SERIAL_C, cbrnPodsInterface.COLLECTOR_POD);
    public static final ErrorCodeHeartbeatSerialBelief PODHEARTBEAT_BLADEWERXRECV_ERRORCODE = new ErrorCodeHeartbeatSerialBelief ("Alpha sensor communication timeout: ", CBRNHeartbeatBelief.SERIAL_C, cbrnPodsInterface.TRACKER_POD);
    
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_ANACONDADETECTION_ERRORCODE = new ErrorCodeConstantUpdateBelief("Chemical sensor detection message timeout: ", AnacondaDetectionBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_IBACDETECTION_ERRORCODE = new ErrorCodeConstantUpdateBelief("Particle sensor detection message timeout: ", ParticleDetectionBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_BLADEWERXDETECTION_ERRORCODE = new ErrorCodeConstantUpdateBelief("Alpha sensor detection message timeout: ", AlphaDetectionBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_BRIDGEPORTDETECTION_ERRORCODE = new ErrorCodeConstantUpdateBelief("Gamma classification detection message timeout: ", GammaDetectionBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_BRIDGEPORTSTATUS_ERRORCODE = new ErrorCodeConstantUpdateBelief("Gamma sensor communication timeout: ", GammaStatisticsBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_CANBERRADETECTION_ERRORCODE = new ErrorCodeConstantUpdateBelief("Gamma dose rate detection message timeout: ", CanberraDetectionBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_PICCOLOTELEMETRY_ERRORCODE = new ErrorCodeConstantUpdateBelief("WACS autopilot message timeout: ", PiccoloTelemetryBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_TASETELEMETRY_ERRORCODE = new ErrorCodeConstantUpdateBelief("WACS IR camera message timeout: ", TASETelemetryBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_WACSHEARTBEAT_ERRORCODE = new ErrorCodeConstantUpdateBelief("WACS communication timeout: ", WacsHeartbeatBelief.class);
    public static final ErrorCodeConstantUpdateBelief CONSTANTUPDATE_SATCOMMHEARTBEAT_ERRORCODE = new ErrorCodeConstantUpdateBelief("Satellite modem communication timeout: ", SatCommStatusBeliefSatComm.class);
    
    public static final ErrorCodeBase IBACNOPARTICLES_ERRORCODE = new ErrorCodeBase("Particle sensor invalid reading");
    public static final ErrorCodeBase SAFETYBOXCONFLICT_ERRORCODE = new ErrorCodeBase("UAS outside safety box");
    
    public static final ErrorCodeBase STRIKEDETECTED_NOTIFYCODE = new ErrorCodeBase ("Strike detected!");
            
    public static final ConcurrentHashMap<Class, ErrorCodeBase> MISSIONSETTINGS_ERRORCODES = new ConcurrentHashMap<Class, ErrorCodeBase>();
    public static final ConcurrentHashMap<String, LinkedList<AllowableMissionErrorInfo>> MISSIONSETTINGS_ALLOWABLEVALUES = new ConcurrentHashMap<String, LinkedList<AllowableMissionErrorInfo>>();
    
    
    private BeliefManager m_BeliefManager;
    private LinkedList<ErrorCodeBase> m_CommandedBeliefErrors = new LinkedList<ErrorCodeBase>();
    private LinkedList<ErrorCodeInfoNeeded> m_InfoNeededBeliefErrors = new LinkedList<ErrorCodeInfoNeeded>();
    private LinkedList<ErrorCodeBase> m_CommandedTimeNameBeliefErrors = new LinkedList<ErrorCodeBase>();
    private LinkedList<ErrorCodeBase> m_HeartbeatSerialTimeErrors = new LinkedList<ErrorCodeBase>();
    private LinkedList<ErrorCodeBase> m_ConstantUpdateBeliefErrors = new LinkedList<ErrorCodeBase>();
    private LinkedList<ErrorCodeBase> m_CustomBeliefErrors = new LinkedList<ErrorCodeBase>();
    
    private LinkedList<CommandedBeliefErrorListener> m_ErrorListeners;
    private LinkedList<TimedBeliefErrorListener> m_TimedErrorListeners;
    
    
    
    private MissionErrorManager ()
    {        
        m_ErrorListeners = new LinkedList<CommandedBeliefErrorListener>();
        m_TimedErrorListeners = new LinkedList<TimedBeliefErrorListener>();
        
        //Beliefs that need to verify the commanded belief matches the actual belief
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_MISSIONMODE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_ANACONDAMODE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_IBACMODE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_C100MODE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_BLADEWERXMODE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_ALLOWINTERCEPT_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_IRRECORDSTATE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_WACSWAYPOINT_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_RACETRACKDEFINITION_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_TRACKINGTYPE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_WINDSOURCE_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_ENABLEAUTOPILOTCONTROL_ERRORCODE);
        m_CommandedBeliefErrors.add(COMMANDACTUALSYNC_EXPLOSIONTIME_ERRORCODE);
        
        //Beliefs that require input from user
        m_InfoNeededBeliefErrors.addFirst(INFONEEDED_EXPTIMELOITER_ERRORCODE);
        m_InfoNeededBeliefErrors.addFirst(INFONEEDED_ZEROAIRDATA_ERRORCODE);
        m_InfoNeededBeliefErrors.addFirst(INFONEEDED_TARGETLOCATION_ERRORCODE);
        m_InfoNeededBeliefErrors.addFirst(INFONEEDED_LOITERLOCATION_ERRORCODE);
        
        
        //Beliefs that need to verify the commanded timenamebelief matches the actual timenamebelief
        m_CommandedTimeNameBeliefErrors.add(COMMANDACTUALSYNC_WACSMODE_ERRORCODE);
        m_CommandedTimeNameBeliefErrors.add(COMMANDACTUALSYNC_TARGETBELIEF_ERRORCODE);
        
        //Beliefs that check time of serial communications within pod
        m_HeartbeatSerialTimeErrors.add(PODHEARTBEAT_ANACONDARECV_ERRORCODE);
        m_HeartbeatSerialTimeErrors.add(PODHEARTBEAT_IBACRECV_ERRORCODE);
        m_HeartbeatSerialTimeErrors.add(PODHEARTBEAT_C100RECV_ERRORCODE);
        m_HeartbeatSerialTimeErrors.add(PODHEARTBEAT_BLADEWERXRECV_ERRORCODE);
        
        
        //Beliefs that should be constantly updated
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_ANACONDADETECTION_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_IBACDETECTION_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_BLADEWERXDETECTION_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_BRIDGEPORTDETECTION_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_BRIDGEPORTSTATUS_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_CANBERRADETECTION_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_PICCOLOTELEMETRY_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_TASETELEMETRY_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_WACSHEARTBEAT_ERRORCODE);
        m_ConstantUpdateBeliefErrors.add (CONSTANTUPDATE_SATCOMMHEARTBEAT_ERRORCODE);
        
        //Custom errors
        m_CustomBeliefErrors.add (IBACNOPARTICLES_ERRORCODE);
        m_CustomBeliefErrors.add (SAFETYBOXCONFLICT_ERRORCODE);
        m_CustomBeliefErrors.add (STRIKEDETECTED_NOTIFYCODE);
        
        
        this.setDaemon(true);
        this.start();
    }
    
    public void setBeliefManager (BeliefManager belMgr)
    {
        m_BeliefManager = belMgr;
    }
    
    public static void addMissionErrorType (String errorMessage, Class actualBeliefClass)
    {
        MISSIONSETTINGS_ERRORCODES.put(actualBeliefClass, new ErrorCodeBase(errorMessage));
    }
    
    public static void addMissionAllowableErrorType (Belief allowableValue, Belief priorRequiredValue, Class actualBeliefClass, int missionMode)
    {
        AllowableMissionErrorInfo info = new AllowableMissionErrorInfo ();
        info.m_AllowableValue = allowableValue;
        info.m_PriorRequiredValue = priorRequiredValue;
        info.m_PriorRequiredValueTimeMs = -1;
        
        String key = actualBeliefClass + "" + missionMode;
        LinkedList<AllowableMissionErrorInfo> extgList = MISSIONSETTINGS_ALLOWABLEVALUES.get(key);
        if (extgList != null)
            extgList.add(info);
        else
        {
            LinkedList<AllowableMissionErrorInfo> newList = new LinkedList <AllowableMissionErrorInfo>();
            newList.add(info);
            MISSIONSETTINGS_ALLOWABLEVALUES.put(actualBeliefClass + "" + missionMode, newList);
        }
    }
    
    public void registerErrorListener (CommandedBeliefErrorListener listener)
    {
        //Add a listener for errors
        m_ErrorListeners.add(listener);
    }
    
    public void registerTimedErrorListener (TimedBeliefErrorListener listener)
    {
        //Add a listener for errors
        m_TimedErrorListeners.add(listener);
    }
    
    @Override
    public void run ()
    {
        //Sleep to let things get started before making errors
        try
        {
            Thread.sleep (1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        while (true)
        {
            try
            {
                if (m_BeliefManager == null)
                {
                    System.out.println ("No belief manager provided to Mission Error Manager");
                    Thread.sleep (250);
                    continue;
                }
                
                //Check that all commanded beliefs match their actual beliefs
                for (ErrorCodeBase errorBase : m_CommandedBeliefErrors)
                {
                    ErrorCodeCommandedBelief errorCode = (ErrorCodeCommandedBelief)errorBase;
                    Belief commandedBelief = m_BeliefManager.get(errorCode.CommandedBelief.getSimpleName());
                    Belief actualBelief = m_BeliefManager.get(errorCode.ActualBelief.getSimpleName());
                    
                    int alarmLevel = getAlarmLevelBelief (commandedBelief, actualBelief);
                    notifyListeners (errorCode, alarmLevel);
                }
                
                //Check that all commanded timenamebeliefs match their actual beliefs
                for (ErrorCodeBase errorBase : m_CommandedTimeNameBeliefErrors)
                {
                    ErrorCodeCommandedTimeNameBelief errorCode = (ErrorCodeCommandedTimeNameBelief)errorBase;
                    Belief commandedBelief = m_BeliefManager.get(errorCode.CommandedBelief.getSimpleName());
                    Belief actualBelief = m_BeliefManager.get(errorCode.ActualBelief.getSimpleName());
                    String timeNameInstance = errorCode.m_Instance;
                    
                    int alarmLevel = getAlarmLevelTimeNameBelief(commandedBelief, actualBelief, timeNameInstance);
                    notifyListeners (errorCode, alarmLevel);
                }
                
                //Check that all beliefs that require user input
                for (ErrorCodeInfoNeeded errorBase : m_InfoNeededBeliefErrors)
                {
                    ErrorCodeInfoNeeded errorCode = (ErrorCodeInfoNeeded)errorBase;
                    Belief infoReqBelief = m_BeliefManager.get(errorCode.InfoNeededBelief.getSimpleName());
                    Belief infoBelief = m_BeliefManager.get(errorCode.InfoBelief.getSimpleName());
                    
                    int alarmLevel = getAlarmLevelInfoNeededBelief(infoReqBelief, infoBelief);
                    notifyListeners (errorCode, alarmLevel);
                }
                
                //Check that all serial communications times within heartbeat message
                CBRNHeartbeatBelief podBelief = (CBRNHeartbeatBelief)m_BeliefManager.get(CBRNHeartbeatBelief.BELIEF_NAME);
                long currTimeMs = System.currentTimeMillis();
                for (ErrorCodeBase errorBase : m_HeartbeatSerialTimeErrors)
                {
                    ErrorCodeHeartbeatSerialBelief errorCode = (ErrorCodeHeartbeatSerialBelief)errorBase;
                    
                    long serialDelaySec = 0;
                    if (podBelief != null)
                    {
                        //long heartbeatTimeSec = podBelief.getTimeStamp().getTime()/1000L;
                        long currTimeSec = currTimeMs/1000L;
                        long lastSerialRecvSec = podBelief.getLastSerialRecSec(errorCode.m_SerialNumber, errorCode.m_PodNumber);
                        if (lastSerialRecvSec > 0 && currTimeSec > 0)
                            serialDelaySec = Math.max (0, currTimeSec - lastSerialRecvSec);
                        else
                            serialDelaySec = Integer.MAX_VALUE;
                    }
                    else
                    {
                        serialDelaySec = Integer.MAX_VALUE;
                    }
                    
                    
                    int alarmLevel = getAlarmLevelTimedBelief(serialDelaySec);
                    notifyTimedListeners (errorCode, alarmLevel, serialDelaySec);
                }
                
                //Check that all beliefs that should be constantly updated are 
                for (ErrorCodeBase errorBase : m_ConstantUpdateBeliefErrors)
                {
                    ErrorCodeConstantUpdateBelief errorCode = (ErrorCodeConstantUpdateBelief)errorBase;
                    
                    long serialDelaySec = 0;
                    if (errorCode == null || errorCode.UpdateBelief == null)
                        serialDelaySec = Integer.MAX_VALUE;
                    else
                    {
                        Belief currBelief = m_BeliefManager.get(errorCode.UpdateBelief.getSimpleName());
                        if (currBelief != null)
                        {
                            long beliefTimeSec = currBelief.getTimeStamp().getTime()/1000L;
                            long currTimeSec = System.currentTimeMillis()/1000L;
                            serialDelaySec = Math.max (0, currTimeSec - beliefTimeSec);
                        }
                        else
                        {
                            serialDelaySec = Integer.MAX_VALUE;
                        }
                    }
                    
                    int alarmLevel = getAlarmLevelTimedBelief(serialDelaySec);
                    if (errorCode == CONSTANTUPDATE_BLADEWERXDETECTION_ERRORCODE)
                    {
                        alarmLevel = getAlarmLevelTimedBelief (serialDelaySec, 70, 40);
                    }
                    else if (errorCode == CONSTANTUPDATE_BRIDGEPORTDETECTION_ERRORCODE)
                    {
                        alarmLevel = getAlarmLevelTimedBelief (serialDelaySec, 130, 70);
                    }
                    else if (errorCode == CONSTANTUPDATE_WACSHEARTBEAT_ERRORCODE)
                    {
                        alarmLevel = getAlarmLevelTimedBelief (serialDelaySec, 10, 5);
                    }
                    
                    notifyTimedListeners (errorCode, alarmLevel, serialDelaySec);
                }
                
                MissionActualStateBelief actualMissionMode = (MissionActualStateBelief)m_BeliefManager.get(MissionActualStateBelief.BELIEF_NAME);
                if (actualMissionMode != null)
                {
                    int actualState = actualMissionMode.getState();
                    
                    LinkedList <CommandDesiredSettings> desiredSettingsList = MissionManagerAction.getMissionModeDesiredSettings (actualState);
                    if (desiredSettingsList != null)
                    {
                        for (int i = 0; i < desiredSettingsList.size(); i ++)
                        {
                            CommandDesiredSettings desiredSettings = desiredSettingsList.get(i);
                            Class actualBeliefClass = desiredSettings.m_ActualBeliefClass;
                            Belief actualBelief = m_BeliefManager.get(actualBeliefClass.getSimpleName());
                            Belief desiredBelief = desiredSettings.m_DesiredBelief;

                            int alarmLevel = ALARMLEVEL_NOALARM;
                            if (actualBelief != null && desiredBelief != null)
                            {
                                if (actualBelief instanceof TimeNameBelief)
                                    alarmLevel = getAlarmLevelTimeNameBelief(desiredBelief, actualBelief, desiredSettings.m_BeliefVariable);
                                else
                                    alarmLevel = getAlarmLevelBelief (desiredBelief, actualBelief);
                            }
                            else if (desiredBelief != null)
                            {
                                alarmLevel = getAlarmLevelBelief (desiredBelief, actualBelief);
                            }
                            
                            
                            //Check settings in mission modes that can change
                            LinkedList<AllowableMissionErrorInfo> allowableValues = MISSIONSETTINGS_ALLOWABLEVALUES.get(actualBeliefClass + "" + actualState);
                            if (allowableValues != null)
                            {
                                //This means for the mission setting in question, there are multiple values that shouldn't generate errors
                                //For every allowable value...
                                for (AllowableMissionErrorInfo allowed : allowableValues)
                                {
                                    //Only check if alarm is present
                                    if (alarmLevel != MissionErrorManager.ALARMLEVEL_NOALARM)
                                    {
                                        if (allowed.m_PriorRequiredValueTimeMs > actualMissionMode.getTimeStamp().getTime())
                                        {
                                            int testAlarmLevel = MissionErrorManager.ALARMLEVEL_NOALARM;
                                            //The required prior setting has been detected since this mode was entered.
                                            //That means the allowable value here is a valid candidate
                                            if (allowed.m_AllowableValue instanceof TimeNameBelief)
                                                testAlarmLevel = getAlarmLevelTimeNameBelief(allowed.m_AllowableValue, actualBelief, desiredSettings.m_BeliefVariable);
                                            else
                                                testAlarmLevel = getAlarmLevelBelief (allowed.m_AllowableValue, actualBelief);
                                            if (testAlarmLevel == MissionErrorManager.ALARMLEVEL_NOALARM)
                                                alarmLevel = MissionErrorManager.ALARMLEVEL_NOALARM;
                                        }
                                    }
                                    
                                    //Check if the required prior setting has been detected since this mode was entered.
                                    int testAlarmLevel = MissionErrorManager.ALARMLEVEL_NOALARM;
                                    if (allowed.m_PriorRequiredValue instanceof TimeNameBelief)
                                        testAlarmLevel = getAlarmLevelTimeNameBelief(allowed.m_PriorRequiredValue, actualBelief, desiredSettings.m_BeliefVariable);
                                    else
                                        testAlarmLevel = getAlarmLevelBelief (allowed.m_PriorRequiredValue, actualBelief);
                                    if (testAlarmLevel == MissionErrorManager.ALARMLEVEL_NOALARM)
                                        allowed.m_PriorRequiredValueTimeMs = System.currentTimeMillis();
                                }
                            }
                            
                            ErrorCodeBase errorCode = MISSIONSETTINGS_ERRORCODES.get(actualBeliefClass);
                            notifyListeners(errorCode, alarmLevel);
                        }
                    }
                }
                
                //Check that all custom error messages
                for (ErrorCodeBase errorBase : m_CustomBeliefErrors)
                {
                    int alarmLevel = ALARMLEVEL_NOALARM;
                    if (errorBase == IBACNOPARTICLES_ERRORCODE)
                    {
                        IbacActualStateBelief state = (IbacActualStateBelief)m_BeliefManager.get(IbacActualStateBelief.BELIEF_NAME);
                        ParticleDetectionBelief particles = (ParticleDetectionBelief)m_BeliefManager.get(ParticleDetectionBelief.BELIEF_NAME);
                        if (state != null && particles != null)
                        {
                            if (state.getState() && particles.getLCI() == 0 && particles.getSCI() == 0)
                            {
                                //Sensor is on, but no particles detected
                                alarmLevel = ALARMLEVEL_ERROR;
                            }
                        }
                    }
                    else if (errorBase == SAFETYBOXCONFLICT_ERRORCODE)
                    {
                        SafetyBoxBelief safety = (SafetyBoxBelief)m_BeliefManager.get(SafetyBoxBelief.BELIEF_NAME);
                        AgentPositionBelief position = (AgentPositionBelief)m_BeliefManager.get(AgentPositionBelief.BELIEF_NAME);
                        if (safety != null && position != null)
                        {
                            PositionTimeName ptn = position.getPositionTimeName(WACSAgent.AGENTNAME);
                            if (ptn != null)
                            {
                                if (!safety.positionWithinSafetyBox(ptn.getPosition().asLatLonAltPosition()))
                                {
                                    //UAV position is outside of safety box
                                    alarmLevel = ALARMLEVEL_ERROR;
                                }
                            }
                        }
                    }
                    else if (errorBase == STRIKEDETECTED_NOTIFYCODE)
                    {
                        ExplosionBelief expBelief = (ExplosionBelief)m_BeliefManager.get(ExplosionBelief.BELIEF_NAME);
                        if (expBelief != null && ((System.currentTimeMillis() - expBelief.getTime_ms()) < 30000))
                        {
                            alarmLevel = ALARMLEVEL_NOTIFICATION;
                        }
                    }
                    
                    notifyListeners (errorBase, alarmLevel);
                }
                
                Thread.sleep (250);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private int getAlarmLevelTimedBelief (long timeDelaySec)
    {
        return getAlarmLevelTimedBelief(timeDelaySec, 60, 20);
    }
    
    private int getAlarmLevelTimedBelief (long timeDelaySec, long errorTimeDelaySec, long warningTimeDelaySec)
    {
        int alarmLevel = ALARMLEVEL_NOALARM;
        
        //Set alarm level
        if (timeDelaySec > errorTimeDelaySec)
            alarmLevel = ALARMLEVEL_ERROR;
        else if (timeDelaySec > warningTimeDelaySec)
            alarmLevel = ALARMLEVEL_WARNING;
        else
            alarmLevel = ALARMLEVEL_NOALARM;
        return alarmLevel;
    }
    
    private int getAlarmLevelBelief (Belief commandedBelief, Belief actualBelief)
    {
        int alarmLevel = ALARMLEVEL_NOALARM;
        if (commandedBelief != null && (actualBelief == null || !actualBelief.equals(commandedBelief)))
        {
            //Set alarm level
            if (System.currentTimeMillis() > commandedBelief.getTimeStamp().getTime() + 10000)
                alarmLevel = ALARMLEVEL_ERROR;
            else if (System.currentTimeMillis() > commandedBelief.getTimeStamp().getTime() + 5000)
                alarmLevel = ALARMLEVEL_WARNING;
            else
                alarmLevel = ALARMLEVEL_NOTIFICATION;
        }
        else if (commandedBelief == null)
        {
            alarmLevel = ALARMLEVEL_ERROR;
        }
        return alarmLevel;
    }
    
    private int getAlarmLevelInfoNeededBelief(Belief infoReqBelief, Belief infoBelief)
    {
        int alarmLevel = ALARMLEVEL_NOALARM;
        if (infoReqBelief != null && (infoBelief == null || infoReqBelief.isNewerThan(infoBelief)))
        {
            //System.out.println ("Info required: " + infoReqBelief.getName() + " " + (infoBelief==null?"inf":(infoReqBelief.getTimeStamp().getTime() - infoBelief.getTimeStamp().getTime())));
            //Set alarm level
            alarmLevel = ALARMLEVEL_WARNING;
        }
        return alarmLevel;
    }
    
    private int getAlarmLevelTimeNameBelief (Belief commandedBelief, Belief actualBelief, String timeNameInstance)
    {
        int alarmLevel = ALARMLEVEL_NOALARM;
        if (commandedBelief != null)
        {
            TimeName commandedTimeName = ((TimeNameBelief)commandedBelief).getTimeName(timeNameInstance);
            if (commandedTimeName != null)
            {
                boolean mismatch = false;
                if (actualBelief == null)
                    mismatch = true;
                else
                {
                    TimeName actualTimeName = ((TimeNameBelief)actualBelief).getTimeName(timeNameInstance);
                    if (actualTimeName == null || !actualTimeName.equals(commandedTimeName))
                        mismatch = true;
                }

                if (mismatch)
                {
                    //Set alarm level
                    if (System.currentTimeMillis() > commandedTimeName.getTime().getTime() + 10000)
                        alarmLevel = ALARMLEVEL_ERROR;
                    else if (System.currentTimeMillis() > commandedTimeName.getTime().getTime() + 5000)
                        alarmLevel = ALARMLEVEL_WARNING;
                    else
                        alarmLevel = ALARMLEVEL_NOTIFICATION;
                }
            }
        }
        else
        {
            alarmLevel = ALARMLEVEL_ERROR;
        }
        return alarmLevel;
    }
    
    private void notifyListeners (ErrorCodeBase error, int alarmLevel)
    {
        for (CommandedBeliefErrorListener listener : m_ErrorListeners)
            listener.handleCommandedBeliefError(error, alarmLevel);
    }
    
    private void notifyTimedListeners (ErrorCodeBase error, int alarmLevel, long delaySec)
    {
        for (TimedBeliefErrorListener listener : m_TimedErrorListeners)
            listener.handleTimedBeliefError(error, alarmLevel, delaySec);
    }
    
    
    public abstract interface CommandedBeliefErrorListener
    {
        /**
         * 
         * @param errorCode
         * @param alarmLevel -1 if no alarm, 0 if alarm but not urgent, increasing numers for increasing urgency
         */
        public void handleCommandedBeliefError (ErrorCodeBase errorCode, int alarmLevel);
    }
    
    public abstract interface TimedBeliefErrorListener
    {
        /**
         * 
         * @param errorCode
         * @param alarmLevel -1 if no alarm, 0 if alarm but not urgent, increasing numers for increasing urgency
         * @param delaySec Seconds of timed delay presented
         */
        public void handleTimedBeliefError (ErrorCodeBase errorCode, int alarmLevel, long delaySec);
    }
    
    public static class ErrorCodeBase
    {
        public String m_AlarmText;
        
        public ErrorCodeBase (String text)
        {
            m_AlarmText = text;
        }
    }
    
    public static class ErrorCodeConstantUpdateBelief extends ErrorCodeBase
    {
        public Class UpdateBelief;
        
        public ErrorCodeConstantUpdateBelief (String text, Class updateBelief)
        {
            super (text);
            UpdateBelief = updateBelief;
        }
    }
    
    public static class ErrorCodeHeartbeatSerialBelief extends ErrorCodeBase
    {
        public int m_SerialNumber;
        public int m_PodNumber;
        
        public ErrorCodeHeartbeatSerialBelief (String text, int serialNumber, int podNumber)
        {
            super (text);
            m_SerialNumber = serialNumber;
            m_PodNumber = podNumber;
        }
    }
    
    public static class ErrorCodeCommandedBelief extends ErrorCodeBase
    {
        public Class CommandedBelief;
        public Class ActualBelief;
        
        public ErrorCodeCommandedBelief (String text, Class commandedBelief, Class actualBelief)
        {
            super (text);
            CommandedBelief = commandedBelief;
            ActualBelief = actualBelief;
        }
    }
    
    public static class ErrorCodeInfoNeeded extends ErrorCodeBase
    {
        public Class InfoNeededBelief;
        public Class InfoBelief;
        
        public ErrorCodeInfoNeeded (String text, Class infoNeededBelief, Class infoBelief)
        {
            super (text);
            InfoNeededBelief = infoNeededBelief;
            InfoBelief = infoBelief;
        }
    }
    
    public static class ErrorCodeCommandedTimeNameBelief extends ErrorCodeCommandedBelief
    {
        public String m_Instance;
        
        public ErrorCodeCommandedTimeNameBelief (String text, Class commandedBelief, Class actualBelief, String instance)
        {
            super (text, commandedBelief, actualBelief);
            m_Instance = instance;
        }
    }
    
    
    
    public static class AllowableMissionErrorInfo
    {
        public Belief m_AllowableValue;
        public Belief m_PriorRequiredValue;
        public long m_PriorRequiredValueTimeMs;
    }
}
