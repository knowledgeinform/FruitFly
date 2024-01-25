package edu.jhuapl.nstd.cbrnPods.actions;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportEthernetMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportUsbMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCalibration;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaEthernetCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage.GammaDetectorMessage;
import edu.jhuapl.nstd.cbrnPods.Canberra.CanberraDetectionMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDAReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDBReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage.AnacondaDataPair;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeAirframeCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeIdleCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModePodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeSearchCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeStandbyCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetDateTimeCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetGpsCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxAETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDllDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpPowerCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100ActionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100CleanCommand;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100CollectOffCommand;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100CollectOnCommand;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100PrimeCommand;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100ResetVialsCommand;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.c100SampleCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Canberra.CanberraDetectionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.Fan.fanAutoControlCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Fan.fanSetOffCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Fan.fanSetOnCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Heater.heaterAutoControlCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Heater.heaterSetOffCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Heater.heaterSetOnCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacDiagnosticsMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacParticleCountMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacSleepCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacStatusCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podSetRtcCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podShutdownLogCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podStartLogCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoAutoControlCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetClosedCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetOpenCommand;
import edu.jhuapl.nstd.cbrnPods.messages.countMessage;
import edu.jhuapl.nstd.etd.EtdListener;
import edu.jhuapl.nstd.etd.Etd_Interface;
import edu.jhuapl.nstd.swarm.ReplayNmeaEtdLogs;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.BladewerxStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BladewerxCountBelief;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.CanberraDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.CountBelief;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionListBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdErrorMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdRawMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.GammaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.MissionCommandedStateBelief;
import edu.jhuapl.nstd.swarm.belief.ModeTimeName;
import edu.jhuapl.nstd.swarm.belief.Nbc1ReportBelief;
import edu.jhuapl.nstd.swarm.belief.NmeaRawMessageBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PodCommandBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.SensorSummary;
import edu.jhuapl.nstd.swarm.belief.SensorSummaryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ThermalCommandBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConverterCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WacsHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceActualBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataRequiredBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.spectra.AETNAResult.IsotopeConcentraion;
import edu.jhuapl.nstd.tase.RecorderInterface;
import edu.jhuapl.nstd.tase.VideoServerInterface;
import edu.jhuapl.nstd.tase.RecordingProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.jfree.data.time.Month;
import org.jfree.date.MonthConstants;


/**
 *
 * @author humphjc1
 */
public class podAction implements Updateable, cbrnPodMessageListener, BridgeportEthernetMessageListener, BridgeportUsbMessageListener, CanberraDetectionMessageListener, EtdListener
{
    /**
     * Pod heartbeat synch object.
     */
    private final Object _lock;

    /**
     * Interface to the pods.
     */
    protected cbrnPodsInterface m_Pods;
    
    /**
     * Stores the last heartbeat message received from the collector pod.
     */
    protected podHeartbeatMessage m_LastHeartbeatPod0;
    
    /**
     * Stores the last heartbeat message received from the tracker pod.
     */
    protected podHeartbeatMessage m_LastHeartbeatPod1;

    /**
     * Stores the timestamp of last Belief message received.
     * 
     *  iprevTime => timestamp of IbacStateBelief
     *  anprevTime => timestamp of AnacondaStateBelief
     *  aprevTime => timestamp of AlphaSensorStateBelief
     *  cprevTime => timestamp of ParticleCollectorStateBelief
     *  pprevTime => timestamp of PodCommandBelief
     *  tprevTime => timestamp of ThermalCommandBelief
     *  gfrprevTime => timestamp of VideoClientStateBelief
     */
    private Date iprevTime, anprevTime, aprevTime, cprevTime, pprevTime, tprevTime, vrprevTime, vsprevTime, gfrprevTime, frprevTime, vccprevTime;

    /**
     * IBAC on/off flag.
     */
    private boolean _IbacOn;
    
    /**
     * Anaconda LCD strings.
     */
    private AnacondaDataPair[] _lcdaList, _lcdbList;

    /**
     * Maps agent numbers (1-37) to  
     */
    private static HashMap<Integer,String> m_AgentMap;

    private boolean podHeartbeatUpdate;
    private RecorderInterface mRecorderUpdateThread = null;
    private VideoServerInterface mVSInterface = null;
    private RecordingProcessor mRecordingProcessor = null;
    
    private BufferedWriter _etdLogWriter;
    
    private class AverageCountsThreshold
    {
        double _countAverage = 0;
        double _countStdev = 0;
        int _counts;
        int _countsToAverage;
        int _minCountsToThreshold;
        boolean detectionReceived = false;
    };
    private AverageCountsThreshold m_ParticleCountsThreshold = new AverageCountsThreshold();
    private AverageCountsThreshold m_BiologicalCountsThreshold = new AverageCountsThreshold();
    
    
    
    SensorSummary m_ChemicalSensorSummary;
    SensorSummary m_BiologicalSensorSummary;
    SensorSummary m_RadNucSensorSummary;
    SensorSummary m_CloudSensorSummary;
    private long m_LastUpdatedSensorSummaryBeliefTimeMs;
    private boolean m_ChemicalSensorSummarySent;
    private long m_LastAnacondaGpsUpdateTimeMs = -1;
    private boolean m_FirstDetectionSinceIbacOn = false;
    
    
    BeliefManager beliefManager;

    String agentID = "";
    
    private final Object m_NbcReportLock = new Object();
    private Nbc1ReportBelief m_RecentNbcReport;
    private double m_RecentNcbDetectionStrength = -1;
    private long m_LastNbcReportPublishedTimeMs = -1;
    private long m_NbcReportUpdatePeriodMs;
    private int m_NbcReportCounter;
    private String m_BaseNbcReportText;
    private String m_RecordingConverterInputDirectory;
    private String m_RecordingConverterOutputDirectory;
    
    private static final String PLUME_NBC_REPORTTEXT = "Plume";
    
    private Etd_Interface m_EtdInterface;
    
    private LinkedList<PositionTimeName> _posTimeNameHistory;
    private long m_LastPosTimeNameHistoryTimeMs = -1;
    private long _etdDelayMs;
    private String _dataType = "CONC";
    
    public podAction (BeliefManager mgr, String agentID, cbrnPodsInterface pods)
    {        
        _dataType = Config.getConfig().getProperty("Etd.dataType", "CONC");
        _posTimeNameHistory = new LinkedList<PositionTimeName>();
        _etdDelayMs = Config.getConfig().getPropertyAsInteger("Etd.delayMs", 0);
        
        _lock = new Object();

        m_AgentMap = new HashMap<Integer,String>();
        
        this.agentID = agentID;
        beliefManager = mgr;

        // start the recorder update thread
        mRecorderUpdateThread = new RecorderInterface(beliefManager, this.agentID);
        mRecorderUpdateThread.start();

        if(mgr!=null && agentID!=null) {
            m_EtdInterface = new Etd_Interface();
            m_EtdInterface.addEtdListener(this);
            Thread pt = (new Thread(m_EtdInterface));
            pt.setName ("WACS-EtdInterface");
            pt.start();        
        }
        
        m_Pods = pods;
        m_ChemicalSensorSummary = new SensorSummary();
        m_BiologicalSensorSummary = new SensorSummary();
        m_RadNucSensorSummary = new SensorSummary();
        m_CloudSensorSummary = new SensorSummary();
        m_ChemicalSensorSummarySent = false;
        

        
        //TODO: Actually implement this
        m_RadNucSensorSummary.m_CurrDetectionTimeMs = System.currentTimeMillis();
        m_RadNucSensorSummary.m_CurrDetectionValue = 0;
        m_RadNucSensorSummary.m_CurrDetectionString = null;
        m_RadNucSensorSummary.m_MaxDetectionValue = 0;
        m_RadNucSensorSummary.m_MaxDetectionValueTimeMs = System.currentTimeMillis();
        m_RadNucSensorSummary.m_MaxDetectionString = null;
        m_RadNucSensorSummary.m_GreenLightMinValue = 0;
        m_RadNucSensorSummary.m_YellowLightMinValue = 30;
        m_RadNucSensorSummary.m_RedLightMinValue = 60;
        m_RadNucSensorSummary.m_RedLightMaxValue = 100;
        m_RadNucSensorSummary.m_LastAboveGreenDetectionTimeMs = -1;
        m_RadNucSensorSummary.m_InBackgroundCollection = false;

        _lcdaList = null;
        _lcdbList = null;
        
        podHeartbeatUpdate = false;

        // Parse configuration properties.
        for(int i=1; i<=37; i++)
            m_AgentMap.put(i, Config.getConfig().getProperty("podAction.anaconda.agent"+i, "NULL"));

        m_ParticleCountsThreshold._countsToAverage = Config.getConfig().getPropertyAsInteger("podAction.particledetection.CountsToAverage", 600);
        m_ParticleCountsThreshold._minCountsToThreshold = Config.getConfig().getPropertyAsInteger("podAction.particledetection.MinCountsToThreshold", 60);
        m_BiologicalCountsThreshold._countsToAverage = m_ParticleCountsThreshold._countsToAverage;
        m_BiologicalCountsThreshold._minCountsToThreshold = m_ParticleCountsThreshold._minCountsToThreshold;
        m_NbcReportUpdatePeriodMs = Config.getConfig().getPropertyAsLong("podAction.NbcReportUpdatePeriodMs", 30000);
        m_RecordingConverterInputDirectory = Config.getConfig().getProperty("EthernetVideoClient.RecordingConverterInputDirectory", "D:\\recordings");
        m_RecordingConverterOutputDirectory = Config.getConfig().getProperty("EthernetVideoClient.RecordingConverterOutputDirectory", "D:\\recordingAVI");
        
        m_NbcReportCounter = 0;
        //TODO:  Implement system specific and mission specific creation of base NBC report text
        m_BaseNbcReportText = "WACS01-0017-";
        
        // Register listeners for the following sensor messages.
        if (m_Pods != null)
        {
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_STATUS_TYPE, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_REPORT, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_REPORT, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodMsg.BLADEWERX_PUMP_STATUS_TYPE, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_ACTION_TYPE, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_DIAGNOSTICS_TYPE, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_PARTICLE_COUNT_TYPE, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.RABBIT_BOARD, cbrnPodMsg.POD_HEARTBEAT_TYPE, this);

            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_COMPOSITE_HISTOGRAM, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.COUNT_ITEM, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_DLL_DETECTION_REPORT, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_AETNA_DETECTION_REPORT, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_COMPOSITE_HISTOGRAM, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_DETECTION_REPORT, this);

            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_HISTOGRAM, this);
           // m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_STATISTICS, this);

            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_G_SPECTRA, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_H_SPECTRA, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_G_SPECTRA, this);
            m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_H_SPECTRA, this);
            
            m_Pods.addCanberraDetectionListener(this);
            
          	// Set this class as a Bridgeport Message listener
        	if (Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.useBridgeportUsbInterface", false))
	        {
        		m_Pods.addBridgeportListener((BridgeportUsbMessageListener) this);
	        }
        	else
	        {
        		m_Pods.addBridgeportListener((BridgeportEthernetMessageListener) this);
	        }
        }
        
        if(mgr!=null) {
            //Init/sync and starts logging
            PodCommandBelief pcb = new PodCommandBelief(agentID, cbrnPodCommand.POD_SET_RTC);
            mgr.put(pcb);

            //When pod boots up, put out a commanded pre-flight mission mode.  But, set the time for that belief to be very old,
            //that so that any existing beliefs will immediately override this one.  This just ensures that some mode exists.
            MissionCommandedStateBelief belief = new MissionCommandedStateBelief (mgr.getName(), MissionCommandedStateBelief.PREFLIGHT_STATE, new Date (1000));
            mgr.put(belief);

            WindEstimateSourceActualBelief windBelief = new WindEstimateSourceActualBelief(WindEstimateSourceActualBelief.WINDSOURCE_WACSAUTOPILOT, 1000);
            mgr.put (windBelief);
        }
        
        File etdOutFile;
        try {
            new File("./etdLogs").mkdirs();
            etdOutFile = new File ("./etdLogs/etdLog_" + System.currentTimeMillis() + ".txt");
            etdOutFile.createNewFile();
            _etdLogWriter = new BufferedWriter(new FileWriter(etdOutFile));
        } catch (Exception e) {
            System.out.println("Error opening ETD log file");
            return;
        }        
    }
    
    public void setReplayNmeaEtdLogs(ReplayNmeaEtdLogs replayNmeaEtdLogs) {
        m_EtdInterface.setReplayNmeaEtdLogs(replayNmeaEtdLogs);
        
    }
    
    short ctr = 0;
        public void handleEtd(long currentTimeMs, String etdLine) {
            if(beliefManager != null && agentID != null) 
            {
                //AgentPositionBelief b = (AgentPositionBelief) beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                //AbsolutePosition pos = b.getPositionTimeName(agentID).getPosition();
                                
                //long currentTimeMs = System.currentTimeMillis();
                long timeMs = currentTimeMs - _etdDelayMs;
             
                try {
                    EtdRawMessageBelief etdRawMessage = new EtdRawMessageBelief(agentID, currentTimeMs, etdLine);
                    beliefManager.put(etdRawMessage);          
                    
                    String semicolonSplit[] = etdLine.split(";");

                    String concentrationString = null;
                    String statusString = null;
                    String errorString = null;
                    String concValue = null;
                    String pa1Value = null;
                    
                    String firstMsg = null;
                    String secondMsg = null;
                    String thirdMsg = null;
                    
                    if(semicolonSplit.length>0) {
                        firstMsg = semicolonSplit[0].trim();
                        
                        if(semicolonSplit.length>1) {
                            secondMsg = semicolonSplit[1].trim();
                            
                            if(semicolonSplit.length>2) {
                                thirdMsg = semicolonSplit[2].trim();
                            }
                        }
                    }
                    
                    if(firstMsg!=null && !firstMsg.isEmpty()) {
                        if(firstMsg.startsWith("Conc")) {
                            concentrationString = firstMsg;
                            String[] colonSplit = concentrationString.split(":|,");
                            concValue = colonSplit[1].trim();
                            pa1Value = colonSplit[5].trim();
                        } else if(firstMsg.startsWith("LTemp")) {
                            statusString = firstMsg;
                        } else {
                            System.out.println("Error: ETD raw message in wrong format");
                            return;
                        }
                    } else {
                        System.out.println("Error: ETD raw message in wrong format");
                        return;
                    }
                    
                    if(secondMsg!=null && !secondMsg.isEmpty()) {
                        if(secondMsg.startsWith("LTemp")) {
                            statusString = secondMsg;
                        } else {
                            errorString = secondMsg;
                        }
                    }
                    
                    if(thirdMsg!=null && !thirdMsg.isEmpty()) {
                        errorString = thirdMsg;
                    }


                    if (concentrationString!=null && concValue!=null && _posTimeNameHistory.size()>0) {
                        EtdDetectionMessageBelief etdDetectionMessage = new EtdDetectionMessageBelief(agentID, currentTimeMs, concentrationString);
                        beliefManager.put(etdDetectionMessage);
                                    
                        PositionTimeName _nextPositionTimeName = null;
                        long tempTime1 = currentTimeMs-_etdDelayMs;
                        long tempTime2 = _posTimeNameHistory.peek().getTime().getTime();
                        boolean found = false;
                        while(!found && _posTimeNameHistory.size()>=2 && currentTimeMs-_etdDelayMs>_posTimeNameHistory.peek().getTime().getTime()) {
                            _nextPositionTimeName = _posTimeNameHistory.remove();
                            
                            long tempTime3 = currentTimeMs-_etdDelayMs;
                            long tempTime4 = _posTimeNameHistory.peek().getTime().getTime();
                            if(currentTimeMs-_etdDelayMs<_posTimeNameHistory.peek().getTime().getTime()) {
                                found = true;
                                _posTimeNameHistory.addFirst(_nextPositionTimeName); // need to add back into list because may be multiple concentration readings in this time window
                            }
                        }
                        
                        if(found) {
                            AbsolutePosition pos  = _nextPositionTimeName.getPosition();

                            Float concentration = Float.parseFloat(concValue);
                            Float pa1 = Float.parseFloat(pa1Value);

                            if(_dataType.equalsIgnoreCase("PA1")) {
                                EtdDetectionBelief etd = new EtdDetectionBelief(agentID, pa1, pos, currentTimeMs);
                                beliefManager.put(etd);

                                EtdDetectionListBelief etdList = new EtdDetectionListBelief(agentID, pa1, pos, currentTimeMs);
                                beliefManager.put(etdList);
                            } else {
                                EtdDetectionBelief etd = new EtdDetectionBelief(agentID, concentration, pos, currentTimeMs);
                                beliefManager.put(etd);

                                EtdDetectionListBelief etdList = new EtdDetectionListBelief(agentID, concentration, pos, currentTimeMs);
                                beliefManager.put(etdList);                               
                            }
                            

                            LatLonAltPosition llaPos = pos.asLatLonAltPosition();

                            try {
                                _etdLogWriter.write("Time: " + currentTimeMs + ", Lat: " + llaPos.getLatitude() + ", Lon: " + llaPos.getLongitude() + ", Alt: " + llaPos.getAltitude() + ", Conc: " + concentration);
                                _etdLogWriter.newLine();
                                _etdLogWriter.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }   
                        }
                    }
                    
                    if(statusString!=null) {
                        EtdStatusMessageBelief etdStatusMessage = new EtdStatusMessageBelief(agentID, currentTimeMs, statusString);
                        beliefManager.put(etdStatusMessage);     
                    } else {
                        System.out.println("Error: no status string parsed from ETD raw message");
                    }
                     
                    if(errorString!=null) {
                        EtdErrorMessageBelief etdErrorMessage = new EtdErrorMessageBelief(agentID, currentTimeMs, errorString);
                        beliefManager.put(etdErrorMessage);
                    }
                } catch (Exception e) {
                    System.out.println("error parsing ETD raw messsage");
                }                  
            }
        }

    /**
     * Routes the message to the appropriate object for parsing.
     */
    @Override
    public void handleMessage(cbrnPodMsg m)
    {
        if(m instanceof podHeartbeatMessage)
        {
            handleHeartbeat((podHeartbeatMessage)m);
        }
        else if(m instanceof ibacParticleCountMessage)
        {
            handleParticleCount((ibacParticleCountMessage)m);
        }
        else if(m instanceof ibacDiagnosticsMessage)
        {
            handleIbacDiagnostics((ibacDiagnosticsMessage)m);
        }
        else if(m instanceof c100ActionMessage)
        {
            handleCollector((c100ActionMessage)m);
        }
        else if(m instanceof anacondaStatusMessage)
        {
           handleAnacondaStatus((anacondaStatusMessage)m);
        }
        else if(m instanceof anacondaLCDAReportMessage)
        {
           handleAnacondaReportA((anacondaLCDAReportMessage) m);
        }
        else if(m instanceof anacondaLCDBReportMessage)
        {
           handleAnacondaReportB((anacondaLCDBReportMessage) m);
        }
        else if(m instanceof bladewerxPumpStatusMessage)
        {
           handleAlphaPumpStatus((bladewerxPumpStatusMessage) m);
        }
        else if(m instanceof bladewerxCompositeHistogramMessage)
        {
           handleBladewerxCompositeHistogramMessage((bladewerxCompositeHistogramMessage) m);
        }
        else if(m instanceof bladewerxDllDetectionReportMessage)
        {
           handleBladewerxDllDetectionReportMessage((bladewerxDllDetectionReportMessage) m);
        }
        else if(m instanceof bladewerxAETNADetectionReportMessage)
        {
           handleBladewerxAETNADetectionReportMessage((bladewerxAETNADetectionReportMessage) m);
        }
        else if(m instanceof bridgeportCompositeHistogramMessage)
        {
           handleBridgeportCompositeHistogramMessage((bridgeportCompositeHistogramMessage) m);
        }
        else if(m instanceof countMessage)
        {
            handleCountMessage((countMessage) m);
        }
        else if(m instanceof bridgeportDetectionReportMessage)
        {
           handleBridgeportDetectionReportMessage((bridgeportDetectionReportMessage) m);
        }
        else if(m instanceof anacondaSpectraLCDAGMessage)
        {
            handleSpectraMessage((anacondaSpectraLCDAGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBGMessage)
        {
            handleSpectraMessage((anacondaSpectraLCDBGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDAHMessage)
        {
            handleSpectraMessage((anacondaSpectraLCDAHMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBHMessage)
        {
            handleSpectraMessage((anacondaSpectraLCDBHMessage)m);
        }
    }


//    @Override
    public void handleHistogramMessage(RNHistogram m)
    {
        //Some stupid thing because I can't get the graph to resize itself in the constructor
//        if (m_DisplayGraph.getWidth() == 0)
//            m_DisplayGraph.setSize(m_Panel.getWidth(), m_Panel.getHeight());
//
//        m_DisplayGraph.updateCurrentHistogram (m);
//
//        if (m_DisplayGraph.getAccumulatingHistogram() == null)
//            m_DisplayGraph.addAccumulatingHistogram(m.getCopyOfHistogram());
    }

    @Override
    public void handleStatisticsMessage(GammaEthernetCountRates m)
    {
        GammaStatisticsBelief gsb= new GammaStatisticsBelief(agentID);
        gsb.setDeadTicks(m.getDeadTicks());
        gsb.setDeadTimeFraction(m.getDeadTimeFraction());
        gsb.setEventRate(m.getEventRate());
        gsb.setM_InputRate(m.getInputRate());
        gsb.setNumEvents(m.getNumEvents());
        gsb.setNumTriggers(m.getNumTriggers());
        gsb.setRealTicks(m.getRealTicks());
        gsb.setRealTime(m.getRealTime());
        gsb.setTriggerRate(m.getTriggerRate());
        beliefManager.put(gsb);
    }

    @Override
    public void handleCalibrationMessage (GammaCalibration m)
    {
        //Not sending calibration message onto belief network
    }
    
    private void publishRecentNbcReport (NavyAngle windBearingFrom, Speed windSpeed)
    {
        synchronized (m_NbcReportLock)
        {
            if (m_RecentNbcReport != null && m_RecentNcbDetectionStrength > 0)
            {
                //update wind speed/dir
                m_RecentNbcReport.setWind (windBearingFrom, windSpeed);
                
                beliefManager.put(m_RecentNbcReport);
                m_RecentNbcReport = null;
            }
            m_RecentNcbDetectionStrength = -1;
        }
    }
    
    private void updateRecentNbcReport (float scaledDetectionStrength, LatLonAltPosition detectionLocation, long detectionTimeMs, String detectionText)
    {
        synchronized (m_NbcReportLock)
        {
            //Only use 'plume' nbc reports if there is no other more specific NBC report available
            if (m_RecentNbcReport == null || m_RecentNbcReport.getDetectionString().equals (PLUME_NBC_REPORTTEXT) || !detectionText.equals (PLUME_NBC_REPORTTEXT))
            { 
                boolean overridePlume = (m_RecentNbcReport != null && m_RecentNbcReport.getDetectionString().equals (PLUME_NBC_REPORTTEXT) && !detectionText.equals (PLUME_NBC_REPORTTEXT));
                
                //this is a new detction, check to see if it's the strongest recent detection
                if (scaledDetectionStrength > m_RecentNcbDetectionStrength || overridePlume)
                {
                    //This is the strongest recent detection, store it as the current NBC report information
                    LatLonAltPosition lla = (LatLonAltPosition)(detectionLocation.clone());
                    Date dt = new Date (detectionTimeMs);
                    //dt.setMonth(Month.AUGUST);
                    //dt.setDate(27);

                    if (m_RecentNbcReport == null)
                        m_NbcReportCounter ++;
                    String reportNumber = m_BaseNbcReportText + String.format ("%03d", m_NbcReportCounter);
                    m_RecentNbcReport = new Nbc1ReportBelief(reportNumber, lla, dt, detectionText, NavyAngle.ZERO, Speed.ZERO);
                    m_RecentNcbDetectionStrength = scaledDetectionStrength;
                }
            }
        }
    }
    
    @Override
    public void update() 
    {
        try
        {
            //Send wacs heartbeat belief, first thing in case any exceptions are killing this update thread
            WacsHeartbeatBelief wacsHb = new WacsHeartbeatBelief();
            beliefManager.put(wacsHb);
            
            
            //Make sure air data is zero'd at some point
            ZeroAirDataBelief currZeroAirDataBelief = (ZeroAirDataBelief)beliefManager.get(ZeroAirDataBelief.BELIEF_NAME);
            if (currZeroAirDataBelief == null)
            {
                ZeroAirDataRequiredBelief currReqBelief = (ZeroAirDataRequiredBelief)beliefManager.get(ZeroAirDataRequiredBelief.BELIEF_NAME);
                if (currReqBelief == null)
                {
                    ZeroAirDataRequiredBelief reqBelief = new ZeroAirDataRequiredBelief();
                    beliefManager.put(reqBelief);
                }
            }
            
            //Send GPS Message to Anaconda
            AgentPositionBelief posbel = (AgentPositionBelief)beliefManager.get(AgentPositionBelief.BELIEF_NAME);
            if(posbel!=null && m_Pods != null)
            {
                PositionTimeName pos = posbel.getPositionTimeName(WACSAgent.AGENTNAME);
                if (pos.getTime().getTime() > m_LastAnacondaGpsUpdateTimeMs)
                {
                    double lat =  pos.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
                    double lon =  pos.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
                    int alt =  (int)(pos.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET));
                    m_Pods.sendCommand(new anacondaSetGpsCommand(pos.getTime().getTime()/1000,lat, lon, alt)); //Time in seconds, not milliseconds
                    m_LastAnacondaGpsUpdateTimeMs = pos.getTime().getTime();
                }
            }
            
            NmeaRawMessageBelief nmeaBel = (NmeaRawMessageBelief)beliefManager.get(NmeaRawMessageBelief.BELIEF_NAME);
            if(nmeaBel!=null && m_Pods!=null) 
            {
                PositionTimeName pos = nmeaBel.getPositionTimeName();
                if(pos.getTime().getTime() > m_LastPosTimeNameHistoryTimeMs) {
                    _posTimeNameHistory.add(pos);
                    m_LastPosTimeNameHistoryTimeMs = pos.getTime().getTime();                   
                }
            }

            //Get the current belief for hearbeat
            CBRNHeartbeatBelief bel = (CBRNHeartbeatBelief)beliefManager.get(CBRNHeartbeatBelief.BELIEF_NAME);
            //Determine if we have updates for the belief
            if (bel == null || podHeartbeatUpdate == true)
            {
                synchronized(_lock)
                {
                        //If we have updates to the belief, make a new belief and send it out
                        CBRNHeartbeatBelief newBel = new CBRNHeartbeatBelief(agentID, m_LastHeartbeatPod0, m_LastHeartbeatPod1);
                        beliefManager.put(newBel);
                        podHeartbeatUpdate = false;
                }
            }

            //ThermalCommandBelief
           ThermalCommandBelief tbel = (ThermalCommandBelief)beliefManager.get(ThermalCommandBelief.BELIEF_NAME);
            if((tbel != null && m_Pods != null && tprevTime == null) ||(tbel != null && tprevTime!=null && tbel.getTimeStamp().after(tprevTime)))
            {
                tprevTime = tbel.getTimeStamp();
                // Servo 0 (on pin 15) opens/closes the valve, so it is the servo to control
                switch(tbel.getThermalCommand())
                {
                    case FanOn:
                        m_Pods.sendCommandToBoard(new fanSetOnCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoSetOpenCommand((char)0),tbel.getPod());
                        break;
                    case FanOff:
                        m_Pods.sendCommandToBoard(new fanSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoSetClosedCommand((char)0),tbel.getPod());
                        break;
                    case HeaterOn:
                       m_Pods.sendCommandToBoard(new fanSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterSetOnCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoSetClosedCommand((char)0),tbel.getPod());
                        break;
                    case HeaterOff:
                        m_Pods.sendCommandToBoard(new fanSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoSetClosedCommand((char)0),tbel.getPod());
                        break;
                    case AutoOn:
                        m_Pods.sendCommandToBoard(new fanAutoControlCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterAutoControlCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoAutoControlCommand((char)0),tbel.getPod());
                        break;
                     case AutoOff:
                        m_Pods.sendCommandToBoard(new fanSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new heaterSetOffCommand(),tbel.getPod());
                        m_Pods.sendCommandToBoard(new servoSetClosedCommand((char)0),tbel.getPod());
                        break;
                }
            }

         // AlphaSensorStateBelief
            AlphaSensorStateBelief abel = (AlphaSensorStateBelief)beliefManager.get(AlphaSensorStateBelief.BELIEF_NAME);

            if((abel != null && m_Pods != null && aprevTime == null) ||(abel != null && aprevTime!=null && abel.getTimeStamp().after(aprevTime)))
            {
                aprevTime = abel.getTimeStamp();
                if (abel.getState())
                    m_Pods.sendCommand(new bladewerxPumpPowerCommand(true));
                else
                    m_Pods.sendCommand(new bladewerxPumpPowerCommand(false));
            }
            
            // VideoClientStateBelief
            VideoClientRecorderCmdBelief vcrbel = (VideoClientRecorderCmdBelief)beliefManager.get(VideoClientRecorderCmdBelief.BELIEF_NAME);            
            if ((vcrbel != null && vrprevTime == null) || (vcrbel != null && vrprevTime!=null && vcrbel.getTimeStamp().after(vrprevTime)))
            {                
                vrprevTime = vcrbel.getTimeStamp();      
                RecorderInterface.SendRecorderCommand(vcrbel.getRecorderCmd());
            }
            
            // VideoClientInitiateStreamBelief
            VideoClientStreamCmdBelief vcsbel = (VideoClientStreamCmdBelief)beliefManager.get(VideoClientStreamCmdBelief.BELIEF_NAME);
            if ((vcsbel != null && vsprevTime == null) || (vcsbel != null && vsprevTime !=null && vcsbel.getTimeStamp().after(vsprevTime)))
            {             
                vsprevTime = vcsbel.getTimeStamp();
                //System.out.println("Got VideoClientStreamCmdBelief = " +  vcsbel.getStreamCmd());
                if (vcsbel.getStreamCmd())
                {                                        
                    if (mVSInterface == null)
                    {
                        mVSInterface  = new VideoServerInterface(vcsbel.getClientIp(), vcsbel.getClientPort(), beliefManager);  
                    }                                      
                    mVSInterface.startPodStreaming();
                }
                else
                {
                    if (mVSInterface != null)
                    {
                        mVSInterface.stopPodStreaming();
                        mVSInterface.setClientReady(false);
                    }
                }
                
                //update stream status
                VideoClientStreamStatusBelief streamStatusBelief = new VideoClientStreamStatusBelief(agentID, vcsbel.getStreamCmd());
                beliefManager.put(streamStatusBelief);
            }
            
            // VideoClientSatCommFrameRequestBelief
            VideoClientSatCommFrameRequestBelief vcbel = (VideoClientSatCommFrameRequestBelief)beliefManager.get(VideoClientSatCommFrameRequestBelief.BELIEF_NAME);
            if (mVSInterface != null && ((vcbel != null && gfrprevTime == null) || (vcbel != null && gfrprevTime != null && vcbel.getTimeStamp().after(gfrprevTime))))
            {                    
                System.out.println("POD_ACTION: Received guaranteed frame request, receipt = " + vcbel.getClientReceipt());
                mVSInterface.setClientReady(true);
                mVSInterface.setGuaranteedMode(true);
                mVSInterface.setClientReceivedFrame(vcbel.getClientReceipt());  
                gfrprevTime = vcbel.getTimeStamp(); 
            }         
            
            // VideoClientFrameRequestBelief
            VideoClientFrameRequestBelief vcfBel = (VideoClientFrameRequestBelief)beliefManager.get(VideoClientFrameRequestBelief.BELIEF_NAME);
            if (mVSInterface != null && ((vcfBel != null && frprevTime == null) || (vcfBel != null && frprevTime != null && vcfBel.getTimeStamp().after(frprevTime))))
            {
                System.out.println("POD received non guaranteed frame request");
                mVSInterface.setClientReady(true);
                mVSInterface.setGuaranteedMode(false);
                frprevTime = vcfBel.getTimeStamp();
            }
            
            // VideoClientConverterCmdBelief
            VideoClientConverterCmdBelief vccrbel = (VideoClientConverterCmdBelief)beliefManager.get(VideoClientConverterCmdBelief.BELIEF_NAME);
            if ((vccrbel != null && vccprevTime == null) || (vccrbel != null && vccprevTime != null && vccrbel.getTimeStamp().after(vccprevTime)))
            {
                vccprevTime = vccrbel.getTimeStamp();
                if(mRecordingProcessor == null)
                {
                    mRecordingProcessor = new RecordingProcessor("xvid", m_RecordingConverterInputDirectory, m_RecordingConverterOutputDirectory, 0, beliefManager, this.agentID);
                }
                
                mRecordingProcessor.convert();                
            }
            
            //  AnacondaStateBelief
            AnacondaStateBelief anbel = (AnacondaStateBelief)beliefManager.get(AnacondaStateBelief.BELIEF_NAME);
            if((anbel != null && m_Pods != null && anprevTime == null) ||(anbel != null && anprevTime!=null && anbel.getTimeStamp().after(anprevTime)))
            {
                anprevTime = anbel.getTimeStamp();
                switch(anbel.getAnacondState())
                {
                    case Idle:
                        m_Pods.sendCommand(new anacondaModeIdleCommand());
                        break;
                    case Standby:
                        m_Pods.sendCommand(new anacondaModeStandbyCommand());
                        break;
                    case Search1:
                        m_Pods.sendCommand(new anacondaModeSearchCommand(1));
                        break;
                    case Search2:
                        m_Pods.sendCommand(new anacondaModeSearchCommand(2));
                        break;
                    case Search3:
                        m_Pods.sendCommand(new anacondaModeSearchCommand(3));
                        break;
                    case Search4:
                        m_Pods.sendCommand(new anacondaModeSearchCommand(4));
                        break;
                    case Pod:
                        m_Pods.sendCommand(new anacondaModePodCommand());
                        break;
                    case Airframe:
                        m_Pods.sendCommand(new anacondaModeAirframeCommand());
                        break;
                }
            }

       //   Handle IbacStateBelief
            IbacStateBelief ibel = (IbacStateBelief)beliefManager.get(IbacStateBelief.BELIEF_NAME);

            if((ibel != null && m_Pods != null && iprevTime == null) ||(ibel != null && iprevTime!=null && ibel.getTimeStamp().after(iprevTime)))
            {
                iprevTime = ibel.getTimeStamp();
                if(ibel.getState())
                    m_Pods.sendCommand(new ibacStatusCommand());
                else
                    m_Pods.sendCommand(new ibacSleepCommand());
            }

       //   ParticleCollectorStateBelief
            ParticleCollectorStateBelief cbel = (ParticleCollectorStateBelief)beliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME);

            if((cbel != null && m_Pods != null && cprevTime == null) ||(cbel != null && cprevTime!=null && cbel.getTimeStamp().after(cprevTime)))
            {
                cprevTime = cbel.getTimeStamp();
                switch (cbel.getParticleCollectorState())
                {
                    case  Priming:
                       m_Pods.sendCommand(new c100PrimeCommand(Config.getConfig().getPropertyAsInteger("podAction.C100.PrimeDuration", 5)));
                       break;
                    case  Collecting:
                       m_Pods.sendCommand(new c100CollectOnCommand());
                       break;
                    case  StoringSample1:
                        m_Pods.sendCommand(new c100SampleCommand(1));
                        break;
                    case  StoringSample2:
                        m_Pods.sendCommand(new c100SampleCommand(2));
                        break;
                    case  StoringSample3:
                        m_Pods.sendCommand(new c100SampleCommand(3));
                        break;
                    case  StoringSample4:
                        m_Pods.sendCommand(new c100SampleCommand(4));
                        break;
                    case  Idle:
                        m_Pods.sendCommand(new c100CollectOffCommand());
                       break;
                    case  Cleaning:
                        m_Pods.sendCommand(new c100CleanCommand());
                       break;
                    case Reset:
                        m_Pods.sendCommand(new c100ResetVialsCommand());
                        cbel = (ParticleCollectorStateBelief)beliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
                        beliefManager.put(cbel);

                        break;
                }
            }
            else if(cbel == null)
            {
                 cbel = new ParticleCollectorStateBelief(agentID, ParticleCollectorMode.Idle);
                 beliefManager.put(cbel);
            }
            
       //   PodCommandBelief
           PodCommandBelief pbel = (PodCommandBelief)beliefManager.get(PodCommandBelief.BELIEF_NAME);

            if((pbel != null && m_Pods != null && pprevTime == null) ||(pbel != null && pprevTime!=null && pbel.getTimeStamp().after(pprevTime)))
            {
                pprevTime = pbel.getTimeStamp();
                switch(pbel.getCommandCode())
                {
                    case cbrnPodCommand.POD_SET_RTC:
                        //Sync Rabbit RTC
                        m_Pods.sendCommand(new podSetRtcCommand(System.currentTimeMillis()/1000));
                        //Sync Anaconda Timestamp while we're at it
                        m_Pods.sendCommand (new anacondaSetDateTimeCommand(System.currentTimeMillis()/1000));
                        break;
                    case cbrnPodCommand.POD_LOG_NEW:
                        m_Pods.sendCommand(new podStartLogCommand());
                        break;
                    case cbrnPodCommand.POD_LOG_END:
                        m_Pods.sendCommand(new podShutdownLogCommand());
                        break;
                }
            }
            
            //Find commanded mode for WACSagent, and update actual mode if necessary
            AgentModeCommandedBelief commandedMode = (AgentModeCommandedBelief)beliefManager.get(AgentModeCommandedBelief.BELIEF_NAME);
            AgentModeActualBelief actualMode = (AgentModeActualBelief)beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if (commandedMode != null)// && (actualMode == null || commandedMode.isNewerThan(actualMode)))
            {
                ModeTimeName commandedMtn = commandedMode.getModeTimeName(WACSAgent.AGENTNAME);
                ModeTimeName actualMtn = null;
                if (actualMode != null)
                    actualMtn = actualMode.getModeTimeName(WACSAgent.AGENTNAME);
                
                if (commandedMtn != null && (actualMtn == null || commandedMtn.isNewerThan(actualMtn)))
                {
                    actualMode = new AgentModeActualBelief (WACSAgent.AGENTNAME, commandedMtn.getMode());
                    beliefManager.put(actualMode);
                }
            }
            
            //Find commanded target belief settings for WACSagent, and update actual settings if necessary
            TargetCommandedBelief commandedTarget = (TargetCommandedBelief)beliefManager.get(TargetCommandedBelief.BELIEF_NAME);
            TargetActualBelief actualTarget = (TargetActualBelief)beliefManager.get(TargetActualBelief.BELIEF_NAME);
            if (commandedTarget != null)// && (actualTarget == null || commandedTarget.isNewerThan(actualTarget)))
            {
                String targetID = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                PositionTimeName commandedPtn = commandedTarget.getPositionTimeName(targetID);
                PositionTimeName actualPtn = null;
                if (actualTarget != null)
                    actualPtn = actualTarget.getPositionTimeName(targetID);
                
                if (commandedPtn != null && (actualPtn == null || commandedPtn.isNewerThan(actualPtn)))
                {
                    actualTarget = new TargetActualBelief (WACSAgent.AGENTNAME, commandedPtn.getPosition(), Length.ZERO, commandedPtn.getName());
                    beliefManager.put(actualTarget);
                }
            }                    
            
            //Find commanded 'allow intercept' setting for WACSagent, and update actual mode if necessary
            AllowInterceptCommandedBelief commandedAllow = (AllowInterceptCommandedBelief)beliefManager.get(AllowInterceptCommandedBelief.BELIEF_NAME);
            AllowInterceptActualBelief actualAllow = (AllowInterceptActualBelief)beliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
            if (commandedAllow != null && (actualAllow == null || commandedAllow.isNewerThan(actualAllow)))
            {
                actualAllow = new AllowInterceptActualBelief (WACSAgent.AGENTNAME, commandedAllow.getAllow());
                beliefManager.put(actualAllow);
            }
            
            //Find commanded wacs waypoint settings for WACSagent, and update actual settings if necessary
            WACSWaypointCommandedBelief commandedWaypoints = (WACSWaypointCommandedBelief)beliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME);
            WACSWaypointActualBelief actualWaypoints = (WACSWaypointActualBelief)beliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
            if (commandedWaypoints != null && (actualWaypoints == null || commandedWaypoints.isNewerThan(actualWaypoints)))
            {
                actualWaypoints = new WACSWaypointActualBelief (WACSAgent.AGENTNAME, 
                        commandedWaypoints.getIntersectAltitude(),
                        commandedWaypoints.getIntersectRadius(),
                        commandedWaypoints.getFinalLoiterAltitude(),
                        commandedWaypoints.getStandoffLoiterAltitude(),
                        commandedWaypoints.getLoiterRadius()
                        );
                beliefManager.put(actualWaypoints);
            }
            
            //Find commanded loiter location for WACSagent, and update actual settings if necessary
            RacetrackDefinitionCommandedBelief commandedRacetrack = (RacetrackDefinitionCommandedBelief)beliefManager.get(RacetrackDefinitionCommandedBelief.BELIEF_NAME);
            RacetrackDefinitionActualBelief actualRacetrack = (RacetrackDefinitionActualBelief)beliefManager.get(RacetrackDefinitionActualBelief.BELIEF_NAME);
            if (commandedRacetrack != null && (actualRacetrack == null || commandedRacetrack.isNewerThan(actualRacetrack)))
            {
                actualRacetrack = new RacetrackDefinitionActualBelief (commandedRacetrack.getStartPosition());
                beliefManager.put(actualRacetrack);
            }
            
            SensorSummaryBelief currSummaryBelief = (SensorSummaryBelief)beliefManager.get(SensorSummaryBelief.BELIEF_NAME);
            if (currSummaryBelief == null || System.currentTimeMillis() - m_LastUpdatedSensorSummaryBeliefTimeMs > 500)
            {
                //publish current sensor status belief, if data changed, every second or so
                
                if ((m_ChemicalSensorSummary != null && m_ChemicalSensorSummary.m_CurrDetectionTimeMs > m_LastUpdatedSensorSummaryBeliefTimeMs) ||
                        (m_BiologicalSensorSummary != null && m_BiologicalSensorSummary.m_CurrDetectionTimeMs > m_LastUpdatedSensorSummaryBeliefTimeMs) ||
                        (m_RadNucSensorSummary != null && m_RadNucSensorSummary.m_CurrDetectionTimeMs > m_LastUpdatedSensorSummaryBeliefTimeMs) ||
                        (m_CloudSensorSummary != null && m_CloudSensorSummary.m_CurrDetectionTimeMs > m_LastUpdatedSensorSummaryBeliefTimeMs) )
                {
                    SensorSummaryBelief newSummaryBelief = new SensorSummaryBelief(agentID, m_ChemicalSensorSummary, m_BiologicalSensorSummary, m_RadNucSensorSummary, m_CloudSensorSummary);
                    beliefManager.put(newSummaryBelief);
                    m_LastUpdatedSensorSummaryBeliefTimeMs = newSummaryBelief.getTimeStamp().getTime();
                    m_ChemicalSensorSummarySent = true;
                }
            }
            
            long currTimeMs = System.currentTimeMillis();
            if (currTimeMs - m_LastNbcReportPublishedTimeMs > m_NbcReportUpdatePeriodMs)
            {
                NavyAngle windBearingFrom = NavyAngle.ZERO;
                Speed windSpeed = Speed.ZERO;
                METBelief currMetBelief = (METBelief)beliefManager.get(METBelief.BELIEF_NAME);
                if (currMetBelief != null)
                {
                    METTimeName metTN = currMetBelief.getMETTimeName(WACSAgent.AGENTNAME);
                    if (metTN != null)
                    {
                        windBearingFrom = metTN.getWindBearing().plus (Angle.HALF_CIRCLE);
                        windSpeed = metTN.getWindSpeed();
                    }
                }
                
                publishRecentNbcReport (windBearingFrom, windSpeed);
                m_LastNbcReportPublishedTimeMs = currTimeMs;
            }
            
            //Find commanded cloud tracking type for WACSagent, and update actual settings if necessary
            ParticleCloudTrackingTypeCommandedBelief commandedTracking = (ParticleCloudTrackingTypeCommandedBelief)beliefManager.get(ParticleCloudTrackingTypeCommandedBelief.BELIEF_NAME);
            ParticleCloudTrackingTypeActualBelief actualTracking = (ParticleCloudTrackingTypeActualBelief)beliefManager.get(ParticleCloudTrackingTypeActualBelief.BELIEF_NAME);
            if (commandedTracking != null && (actualTracking == null || commandedTracking.isNewerThan(actualTracking)))
            {
                actualTracking = new ParticleCloudTrackingTypeActualBelief (commandedTracking.getTrackingType());
                beliefManager.put(actualTracking);
            }
            
            //Find commanded wind estimation source for WACSagent, and update actual settings if necessary
            WindEstimateSourceCommandedBelief commandedWindSource = (WindEstimateSourceCommandedBelief)beliefManager.get(WindEstimateSourceCommandedBelief.BELIEF_NAME);
            WindEstimateSourceActualBelief actualWindSource = (WindEstimateSourceActualBelief)beliefManager.get(WindEstimateSourceActualBelief.BELIEF_NAME);
            if (commandedWindSource != null && (actualWindSource == null || commandedWindSource.isNewerThan(actualWindSource)))
            {
                actualWindSource = new WindEstimateSourceActualBelief (commandedWindSource.getWindSource());
                beliefManager.put(actualWindSource);
            }
            
            //Find commanded explostion time for WACSagent, and update actual settings if necessary
            ExplosionTimeCommandedBelief commandedExpTime = (ExplosionTimeCommandedBelief)beliefManager.get(ExplosionTimeCommandedBelief.BELIEF_NAME);
            ExplosionTimeActualBelief actualExpTime = (ExplosionTimeActualBelief)beliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME);
            if (commandedExpTime != null && (actualExpTime == null || commandedExpTime.isNewerThan(actualExpTime)))
            {
                actualExpTime = new ExplosionTimeActualBelief (beliefManager.getName(), commandedExpTime.getTime_ms());
                beliefManager.put(actualExpTime);
            }
            
            //Find commanded wind estimation source for WACSagent, and update actual settings if necessary
            EnableAutopilotControlCommandedBelief commandedEnableControl = (EnableAutopilotControlCommandedBelief)beliefManager.get(EnableAutopilotControlCommandedBelief.BELIEF_NAME);
            EnableAutopilotControlActualBelief actualEnableControl = (EnableAutopilotControlActualBelief)beliefManager.get(EnableAutopilotControlActualBelief.BELIEF_NAME);
            if (commandedEnableControl != null && (actualEnableControl == null || commandedEnableControl.isNewerThan(actualEnableControl)))
            {
                actualEnableControl = new EnableAutopilotControlActualBelief (beliefManager.getName(), commandedEnableControl.getAllow());
                beliefManager.put(actualEnableControl);
            }
        }
        catch(Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    void handleSpectraMessage(anacondaSpectraLCDAGMessage msg)
    {

    }

    void handleSpectraMessage(anacondaSpectraLCDBGMessage msg)
    {

    }

    void handleSpectraMessage(anacondaSpectraLCDAHMessage msg)
    {

    }

    void handleSpectraMessage(anacondaSpectraLCDBHMessage msg)
    {

    }

    private void handleBladewerxCompositeHistogramMessage(bladewerxCompositeHistogramMessage message)
    {
        AlphaCompositeHistogramBelief chb = new AlphaCompositeHistogramBelief(agentID, message.copySummedSpectra(), message.getTotalCounts(), message.getLiveTime());
        beliefManager.put(chb);
    }
    
    private void handleBladewerxAETNADetectionReportMessage(bladewerxAETNADetectionReportMessage message)
    {
    	DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";
        Vector<IsotopeConcentraion> isotopes = message.accessIsotopes();
        
        if(isotopes.size() > 0)
        {
	        for (int i = 0; i < isotopes.size(); i ++)
	        {
	            detmsg += isotopes.get(i).isotope + ":" + df.format(isotopes.get(i).concentration) + "%";
	        }
        }
        else
        {
        	detmsg = "No Detection Results";
        }

        System.out.println("Alpha Detection Message: " + detmsg);

        AlphaDetectionBelief adb = new AlphaDetectionBelief(agentID, detmsg);
        beliefManager.put(adb);
    }
    
    private void handleBladewerxDllDetectionReportMessage(bladewerxDllDetectionReportMessage message)
    {
        DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";
        int numResults = message.getNumIdResults();
        
        if(numResults > 0)
        {
            //detmsg = detmsg +  message.returnIdResult(0).m_IsotopeName +":" + df.format(message.returnIdResult(0).m_GoodnessOfFit);
            detmsg = detmsg +  message.returnIdResult(0).m_IsotopeName +":" + df.format(message.returnIdResult(0).m_Confidence);
            for(int i=1; i<numResults; i++)
            {
                //detmsg = detmsg + " - " + message.returnIdResult(i).m_IsotopeName +":" + df.format(message.returnIdResult(i).m_GoodnessOfFit);
                detmsg = detmsg + " - " + message.returnIdResult(i).m_IsotopeName +":" + df.format(message.returnIdResult(i).m_Confidence);
            }
        }
        else
        {
            detmsg = "No Detection Results";
        }

        System.out.println("Alpha Detection Message: " + detmsg);

        AlphaDetectionBelief adb = new AlphaDetectionBelief(agentID, detmsg);
        beliefManager.put(adb);
    }

    private void handleBridgeportCompositeHistogramMessage(bridgeportCompositeHistogramMessage message)
    {
        GammaCompositeHistogramBelief chb = new GammaCompositeHistogramBelief(agentID, message.copySummedSpectra(), message.getTotalCounts(), message.getLiveTime());
        chb.setIsBackground(message.isBackground());
        beliefManager.put(chb);
    }
    
    /**
     * Receives messages exclusively from the Bladewerx sensor, but could be
     * modified to accept messages from other sensors as well.
     * 
     * @param m the countMessage 
     */
    private void handleCountMessage(countMessage m)
    {
        if (m.getSensorType() == cbrnSensorTypes.SENSOR_BLADEWERX)
        {
            BladewerxStatisticsBelief bsb = new BladewerxStatisticsBelief(m.getAlphaCounts(),
                                                                          m.getChanZeroCounts(),
                                                                          m.getChanOneCounts(),
                                                                          ((double)m.getDuration())/1000);
        
            beliefManager.put(bsb);
        }
    }

    private void handleBridgeportDetectionReportMessage(bridgeportDetectionReportMessage message)
    {
        DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";

        Vector <IsotopeConcentraion> isotopes = message.accessIsotopes();
        for (int i = 0; i < isotopes.size(); i ++)
        {
            detmsg += isotopes.get(i).isotope + ":" + df.format (isotopes.get(i).concentration) + "% - ";
        }
         
        System.out.println("Gamma Detection Message: " + detmsg);

        GammaDetectionBelief adb = new GammaDetectionBelief(agentID, detmsg);
        beliefManager.put(adb);

    }

    private void handleAlphaPumpStatus(bladewerxPumpStatusMessage message)
    {
        AlphaSensorActualStateBelief asb = new AlphaSensorActualStateBelief(agentID, message.getPower());
        beliefManager.put(asb);
    }

    
    private void handleAnacondaStatus(anacondaStatusMessage message)
    {
        boolean s1 = message.getSample1Used();
        boolean s2 = message.getSample2Used();
        boolean s3 = message.getSample3Used();
        boolean s4 = message.getSample4Used();

        AnacondaActualStateBelief asb = new AnacondaActualStateBelief(agentID, message.getAnacondaMode(), s1, s2, s3, s4);
        beliefManager.put(asb);
    }

    public void handleAnacondaReportA(anacondaLCDReportMessage message)
    {
        handleAnacondaReport(message, anacondaLCDReportMessage.ANACONDA_LCDA);
    }

    public void handleAnacondaReportB(anacondaLCDReportMessage message)
    {
       handleAnacondaReport(message, anacondaLCDReportMessage.ANACONDA_LCDB);
    }

    protected void handleAnacondaReport(anacondaLCDReportMessage message, int lcdIdx)
    {
        m_ChemicalSensorSummary.m_CurrDetectionTimeMs = System.currentTimeMillis();
        if (!m_ChemicalSensorSummarySent)
        {
            if (message.getMaxDetectionBars() > m_ChemicalSensorSummary.m_CurrDetectionValue)
            {
                m_ChemicalSensorSummary.m_CurrDetectionValue = message.getMaxDetectionBars();
                m_ChemicalSensorSummary.m_CurrDetectionString = m_AgentMap.get(message.getMaxDetectionIdCode());
            }
        }
        else
        {
            m_ChemicalSensorSummary.m_CurrDetectionValue = message.getMaxDetectionBars();
            m_ChemicalSensorSummary.m_CurrDetectionString = m_AgentMap.get(message.getMaxDetectionIdCode());
        }
        m_ChemicalSensorSummarySent = false;
        
        m_ChemicalSensorSummary.m_GreenLightMinValue = 0;
        m_ChemicalSensorSummary.m_YellowLightMinValue = 1;
        m_ChemicalSensorSummary.m_RedLightMinValue = 3;
        m_ChemicalSensorSummary.m_RedLightMaxValue = 8;
        
        if (m_ChemicalSensorSummary.m_CurrDetectionValue > m_ChemicalSensorSummary.m_MaxDetectionValue)
        {
            m_ChemicalSensorSummary.m_MaxDetectionValue = m_ChemicalSensorSummary.m_CurrDetectionValue;
            m_ChemicalSensorSummary.m_MaxDetectionValueTimeMs = m_ChemicalSensorSummary.m_CurrDetectionTimeMs;   
            m_ChemicalSensorSummary.m_MaxDetectionString = m_ChemicalSensorSummary.m_CurrDetectionString;
        }
    
        boolean agentInLoiter = false;
        AgentModeActualBelief agentModeBelief = null;
        if (beliefManager != null)
        {
            agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(LoiterBehavior.MODENAME))
                agentInLoiter = true;
        }
        
        m_ChemicalSensorSummary.m_InBackgroundCollection = false;
        
        AnacondaDataPair[] pairList = message.accessAgentPairList ();
        if(lcdIdx == anacondaLCDReportMessage.ANACONDA_LCDA)
            _lcdaList = pairList;
        else if(lcdIdx == anacondaLCDReportMessage.ANACONDA_LCDB)
            _lcdbList = pairList;


        AnacondaDetectionBelief sbel = new AnacondaDetectionBelief(agentID, _lcdaList, _lcdbList);
        beliefManager.put(sbel);

        //we have a detection
        double hitStrength = message.getDetectionStrength();
        short id = (short)message.getMaxDetectionIdCode();
        if(hitStrength >= 1  && !Config.getConfig().getPropertyAsBoolean("podAction.anaconda.ignore", false))
        {
            try
            {
                if (!agentInLoiter)
                {
                    AbsolutePosition position = getMyPosition();
                    addChemicalDetection(position, hitStrength, id);
                    
                    m_ChemicalSensorSummary.m_LastAboveGreenDetectionTimeMs = m_ChemicalSensorSummary.m_CurrDetectionTimeMs;
                    
                    String text = m_AgentMap.get ((int)id);
                    if (text == null || text.length () == 0)
                        text = "Chemical";
                    updateRecentNbcReport ((float)hitStrength, position.asLatLonAltPosition(), System.currentTimeMillis(), text);
    
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private void handleCollector(c100ActionMessage message)
    {

       ParticleCollectorMode actualCollectorMode = message.getParticleCollectorMode();
       boolean s1 = message.getSample1Used();
       boolean s2 = message.getSample2Used();
       boolean s3 = message.getSample3Used();
       boolean s4 = message.getSample4Used();

       ParticleCollectorActualStateBelief bel = new ParticleCollectorActualStateBelief(WACSAgent.AGENTNAME, actualCollectorMode, s1, s2, s3, s4);

       beliefManager.put(bel);
    }

    
    
    private void handleHeartbeat(podHeartbeatMessage message)
    {
        synchronized(_lock)
        {
            if (message.getPodNumber() == 0)
            {
                    m_LastHeartbeatPod0 = message;
            }
            else if (message.getPodNumber() == 1)
            {
                    m_LastHeartbeatPod1 = message;
            }
        }
        
        podHeartbeatUpdate = true;
    }

    private void handleIbacDiagnostics(ibacDiagnosticsMessage message)
    {
        _IbacOn = !message.isSleeping();
        if (!_IbacOn)
            m_FirstDetectionSinceIbacOn = true;
        
        IbacActualStateBelief ibel = (IbacActualStateBelief)beliefManager.get(IbacActualStateBelief.BELIEF_NAME);
            if( ibel==null || (ibel!=null && ibel.getState()!= _IbacOn))
            {
                IbacActualStateBelief asb = new IbacActualStateBelief(agentID,_IbacOn);
                beliefManager.put(asb);
            }
    }

    private void handleParticleCount (ibacParticleCountMessage message)
    {
        ParticleDetectionBelief sbel = new ParticleDetectionBelief(agentID, message.getCLI(), message.getCSI(), message.getBCLI(), message.getBCSI(), message.getBpLA());
        beliefManager.put(sbel);
        
        int totalCounts = message.getCLI() + message.getCSI();
        float bioRatio = message.getBpLA()/100;
        int bioAvgHalfPercent = 0;
        if (totalCounts > 0)
            bioAvgHalfPercent = (int)(50*Math.max(0,Math.min(1,bioRatio)));

        double hitStrength = 0;
        if ((hitStrength=handleParticleCountThresh(message)) > 0.0001)
        {
            AbsolutePosition position = getMyPosition();
            addParticleDetection(position, hitStrength, (short)bioAvgHalfPercent, (short)message.getHitStrength());
        }
        if ((hitStrength=handleBiologicalCountThresh(message)) > 0.0001)
        {
            //TODO: If need biological detections included in plume tracking, add it here
            //AbsolutePosition position = getMyPosition();
            //addBiologicalDetection(position, hitStrength, (short)bioAvgHalfPercent, (short)message.getHitStrength());
        }
    }

    public double getParticleCountAverage ()
    {
        return m_ParticleCountsThreshold._countAverage;
    }

    public double getParticleCountStdev ()
    {
        return m_ParticleCountsThreshold._countStdev;
    }

    public double getParticleThreshold ()
    {
        if(Config.getConfig().getPropertyAsBoolean("podAction.particledetection.usestdandthresh", false))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.particledetection.stdthresh", 4);
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.particledetection.detectionthresh", 500);

            return Math.max(numdevs*m_ParticleCountsThreshold._countStdev + m_ParticleCountsThreshold._countAverage,thresh);
        }
        else if(Config.getConfig().getPropertyAsBoolean("podAction.particledetection.usestd", true))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.particledetection.stdthresh", 4);

            return (numdevs*m_ParticleCountsThreshold._countStdev + m_ParticleCountsThreshold._countAverage);
        }
        else
        {
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.particledetection.detectionthresh", 500);

            return (thresh);
        }
    }
    
    public double getBiologicalCountAverage ()
    {
        return m_BiologicalCountsThreshold._countAverage;
    }

    public double getBiologicalCountStdev ()
    {
        return m_BiologicalCountsThreshold._countStdev;
    }

    public double getBiologicalThreshold ()
    {
        double retVal = 0;
        if(Config.getConfig().getPropertyAsBoolean("podAction.biologicaldetection.usestdandthresh", false))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.biologicaldetection.stdthresh", 4);
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.biologicaldetection.detectionthresh", 500);

            retVal = Math.max(numdevs*m_BiologicalCountsThreshold._countStdev + m_BiologicalCountsThreshold._countAverage,thresh);
        }
        else if(Config.getConfig().getPropertyAsBoolean("podAction.biologicaldetection.usestd", true))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.biologicaldetection.stdthresh", 4);

            retVal = (numdevs*m_BiologicalCountsThreshold._countStdev + m_BiologicalCountsThreshold._countAverage);
        }
        else
        {
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.biologicaldetection.detectionthresh", 500);

            retVal = (thresh);
        }
        
        return (Math.max(0, Math.min(100, retVal)));
    }


    public String getParticleThresholdString ()
    {
        if(Config.getConfig().getPropertyAsBoolean("podAction.particledetection.usestdandthresh", true))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.particledetection.stdthresh", 4);
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.particledetection.detectionthresh", 500);

            return ("StdDev(" + numdevs + ") and Thresh(" + thresh + ")");
        }
        else if(Config.getConfig().getPropertyAsBoolean("podAction.particledetection.usestd", true))
        {
            double numdevs = Config.getConfig().getPropertyAsInteger("podAction.particledetection.stdthresh", 4);

            return ("StdDev(" + numdevs + ")");
        }
        else
        {
            double thresh = Config.getConfig().getPropertyAsInteger("podAction.particledetection.detectionthresh", 500);

            return ("Thresh(" + thresh + ")");
        }
    }

    /**
     * Returns hit strength if threshold reached.  Returns negative hit strength if threshold not reached
     * @param message
     * @return
     */
    public double handleBiologicalCountThresh(ibacParticleCountMessage message)
    {
        double hits = message.getHitStrength();
        double biopercent = message.getBpLA();
        double threshold = getBiologicalThreshold ();
        
        if (hits != 0 && m_FirstDetectionSinceIbacOn)
        {
            m_FirstDetectionSinceIbacOn = false;
            return 0; //Sometimes it seems like bio data from first data set after turning on is weird.  Ignore it and wait for next
        }
        
        boolean positiveHit = handleStdDevThresh (biopercent, (hits!=0), threshold, m_BiologicalCountsThreshold, m_BiologicalSensorSummary);
        if (positiveHit)
        {
            updateBiologicalSensorSummary();

            //Get baseline bar level for threshold bio%.
            double baselineBarThreshold = calculateBaselineBarLevelForBioPercent (threshold);
            //Get baseline bar level for current bio%;
            double baselineBarCurrent = calculateBaselineBarLevelForBioPercent (biopercent);
            //Actual bar level is difference between baselines bars for current and threshold, but starting at 1
            double hitStrength = Math.max(0,(baselineBarCurrent - baselineBarThreshold)) + 1;
            
            //Generate NBC report
            String text = "Biological";
            AbsolutePosition position = getMyPosition();
            updateRecentNbcReport ((float)hitStrength, position.asLatLonAltPosition(), System.currentTimeMillis(), text);

            return 0.5;
        }
        
        updateBiologicalSensorSummary();
        return 0;
    }
    
    /**
     * Returns hit strength if threshold reached.  Returns negative hit strength if threshold not reached
     * @param message
     * @return
     */
    public double handleParticleCountThresh(ibacParticleCountMessage message)
    {
        double hits = message.getHitStrength();
        double threshold = getParticleThreshold ();
        
        boolean positiveHit = handleStdDevThresh (hits, (hits!=0), threshold, m_ParticleCountsThreshold, m_CloudSensorSummary);
        if (positiveHit)
        {
            //Get baseline bar level for threshold counts.
            double baselineBarThreshold = calculateBaselineBarLevelForCounts (threshold);
            //Get baseline bar level for current counts;
            double baselineBarCurrent = calculateBaselineBarLevelForCounts (message.getHitStrength());
            //Actual bar level is difference between baselines bars for current and threshold, but starting at 1
            double hitStrength = Math.max(0,(baselineBarCurrent - baselineBarThreshold)) + 1;
            updateCloudSensorSummary();

            String text = PLUME_NBC_REPORTTEXT;
            AbsolutePosition position = getMyPosition();
            updateRecentNbcReport ((float)hitStrength, position.asLatLonAltPosition(), System.currentTimeMillis(), text);

            return hitStrength;
        }
        
        updateCloudSensorSummary();
        return 0;
    }
    
    private boolean handleStdDevThresh (double currValue, boolean currValueValid, double threshold, AverageCountsThreshold thresholdObject, SensorSummary sensorSummary)
    {
        sensorSummary.m_CurrDetectionTimeMs = System.currentTimeMillis();
        sensorSummary.m_CurrDetectionValue = (float)currValue;
        sensorSummary.m_CurrDetectionString = null;
        if (sensorSummary.m_CurrDetectionValue > sensorSummary.m_MaxDetectionValue)
        {
            sensorSummary.m_MaxDetectionValue = sensorSummary.m_CurrDetectionValue;
            sensorSummary.m_MaxDetectionValueTimeMs = sensorSummary.m_CurrDetectionTimeMs;   
            sensorSummary.m_MaxDetectionString = null;
        }
        
        boolean agentInLoiter = false;
        AgentModeActualBelief agentModeBelief = null;
        if (beliefManager != null)
        {
            agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(LoiterBehavior.MODENAME))
                agentInLoiter = true;
        }
         
        sensorSummary.m_InBackgroundCollection = true;
        if (thresholdObject._counts > thresholdObject._minCountsToThreshold)
        {
            sensorSummary.m_InBackgroundCollection = false;
            if (!agentInLoiter)
            {
                if(currValue > threshold)
                {
                    thresholdObject.detectionReceived = true;
                    sensorSummary.m_LastAboveGreenDetectionTimeMs = sensorSummary.m_CurrDetectionTimeMs;
                    
                    return true;
                }
            }
        }
        
        boolean updateAverage = false;
        if (agentInLoiter)
        {
            updateAverage = true;
        }
        else if (beliefManager != null)
        {
            updateAverage = false;
        }
        else if (!thresholdObject.detectionReceived)
        {
            updateAverage = true;
        }

        if (!currValueValid)
        {
            //Only update stats if counts are non-zero.  Should only be zero when pump is not running.
            updateAverage = false;
        }

        if (updateAverage)
        {
            //Only update stats in loiter mode

            if(thresholdObject._counts<thresholdObject._countsToAverage)
                thresholdObject._counts++;

            thresholdObject._countAverage = ((thresholdObject._counts-1.0)/thresholdObject._counts)*thresholdObject._countAverage + (1.0/thresholdObject._counts)*currValue;
            if (thresholdObject._counts == 1)
                thresholdObject._countStdev = 1;
            else
                thresholdObject._countStdev = Math.sqrt(((thresholdObject._counts-1.0)/thresholdObject._counts)*Math.pow(thresholdObject._countStdev,2.0) + (1.0/thresholdObject._counts)*Math.pow((currValue-thresholdObject._countAverage),2.0));
        }

        return false;
    }
    
    
    double calculateBaselineBarLevelForCounts (double counts)
    {
        //Currently assuming Anaconda is max at 8 bars
        //Current equation is Counts = 188.95*(Bars^2.7559)
        //Inverse equation is Bars = ((Counts*0.0052924)^0.36286)
                    
        return Math.pow (counts*0.0052924,0.36286);
    }
    
    double calculateBaselineBarLevelForBioPercent (double bioPercent)
    {
        //Currently assuming Anaconda is max at 8 bars
        //Current equation is BioPercent = 11.8*Bars + 4
        //Inverse equation is Bars = ((BioPercent-4)/11.8)
        
        return (Math.max(bioPercent-4, 0)/11.8);
    }
    
    private void updateCloudSensorSummary ()
    {
        if (getParticleThreshold() > 1 && getParticleCountAverage() > 1)
        {
            m_CloudSensorSummary.m_GreenLightMinValue = 0;
            m_CloudSensorSummary.m_YellowLightMinValue = Math.max((float)getParticleThreshold(), 5);
            m_CloudSensorSummary.m_RedLightMinValue = Math.max((float)(getParticleThreshold() + (getParticleThreshold() - getParticleCountAverage())), m_CloudSensorSummary.m_YellowLightMinValue+5);
            m_CloudSensorSummary.m_RedLightMaxValue = 30000;
        }
        else
        {
            m_CloudSensorSummary.m_GreenLightMinValue = 0;
            m_CloudSensorSummary.m_YellowLightMinValue = 28000;
            m_CloudSensorSummary.m_RedLightMinValue = 29000;
            m_CloudSensorSummary.m_RedLightMaxValue = 30000;
        }
    }
    
    private void updateBiologicalSensorSummary ()
    {
        if (getBiologicalThreshold() > 0.01 && getBiologicalCountAverage() > 0.01)
        {
            m_BiologicalSensorSummary.m_GreenLightMinValue = 0;
            m_BiologicalSensorSummary.m_YellowLightMinValue = Math.max((float)getBiologicalThreshold(), 1);
            m_BiologicalSensorSummary.m_RedLightMinValue = Math.max((float)(getBiologicalThreshold() + (getBiologicalThreshold() - getBiologicalCountAverage())), m_BiologicalSensorSummary.m_YellowLightMinValue+1);
            m_BiologicalSensorSummary.m_RedLightMaxValue = 100;
        }
        else
        {
            m_BiologicalSensorSummary.m_GreenLightMinValue = 0;
            m_BiologicalSensorSummary.m_YellowLightMinValue = 98;
            m_BiologicalSensorSummary.m_RedLightMinValue = 99;
            m_BiologicalSensorSummary.m_RedLightMaxValue = 100;
        }
    }

    /**
     * Creates a particle CloudDetectionBelief and registers it with the BeliefManager.
     * 
     * @param position
     * @param value
     */
    public void addParticleDetection(AbsolutePosition position, double scaledValue, short id, short rawValue)
    {
		CloudDetectionBelief cdb = new CloudDetectionBelief(agentID, position.asLatLonAltPosition(), scaledValue, CloudDetection.SOURCE_PARTICLE, id, rawValue);
		beliefManager.put(cdb);
    }

    /**
     * Creates a chemical CloudDetectionBelief and registers it with the BeliefManager.
     * 
     * @param position
     * @param value
     */
    public void addChemicalDetection(AbsolutePosition position, double value, short id)
    {
        CloudDetectionBelief cdb = new CloudDetectionBelief(agentID, position.asLatLonAltPosition(), value, CloudDetection.SOURCE_CHEMICAL, id, (short)value);
        beliefManager.put(cdb);
    }
    
    public static void addParticleDetection(BeliefManager belMgr, String agentID, AbsolutePosition position, double scaledValue, short id, short rawValue) 
    {
        CloudDetectionBelief cdb = new CloudDetectionBelief(agentID, position.asLatLonAltPosition(), scaledValue, CloudDetection.SOURCE_PARTICLE, id, rawValue);
        belMgr.put(cdb);
    }
    
    public static void addChemicalDetection(BeliefManager belMgr, String agentID, AbsolutePosition position, double value, short id) 
    {
        CloudDetectionBelief cdb = new CloudDetectionBelief(agentID, position.asLatLonAltPosition(), value, CloudDetection.SOURCE_CHEMICAL, id, (short)value);
        belMgr.put(cdb);
	
    }

    /**
     * Returns the agent's current postion.
     * 
     * @return LatLonAltPosition
     */
	protected LatLonAltPosition getMyPosition()
    {
        AgentPositionBelief b = (AgentPositionBelief)beliefManager.get(AgentPositionBelief.BELIEF_NAME);
        if (b != null)
        {
            return b.getPositionTimeName(agentID).getPosition().asLatLonAltPosition();
        }
        else
            return null;
    }

	/**
	 * Returns the agent's current bearing angle.
	 * 
	 * @return bearing angle
	 */
    protected Angle getMyBearing()
    {
        AgentBearingBelief b = (AgentBearingBelief)beliefManager.get(AgentBearingBelief.BELIEF_NAME);
        if (b != null)
        {
            return b.getBearingTimeName(agentID).getCurrentBearing().asAngle();
        }
        else
            return null;
    }

    @Override
    public void handleSensorDriverMessage(SensorDriverMessage m)
    {
        //Sensor Driver seems to send messages even if no sensor is there
        if (m == null || m.gammaDetectorMessageList.isEmpty() || !m.gammaDetectorMessageList.get(m.gammaDetectorMessageList.size()-1).getStatus())
            return;
        
        GammaStatisticsBelief gsb= new GammaStatisticsBelief(agentID);
        GammaDetectorMessage msg = m.getGammaDetectorMessageList().get(m.getGammaDetectorMessageList().size() - 1);
        gsb.setEventRate((float)msg.getCountRate());
//        gsb.setDeadTicks(m.getDeadTicks());
//        gsb.setDeadTimeFraction(m.getDeadTimeFraction());
//        gsb.setEventRate(m.getEventRate());
//        gsb.setM_InputRate(m.getInputRate());
//        gsb.setNumEvents(m.getNumEvents());
//        gsb.setNumTriggers(m.getNumTriggers());
//        gsb.setRealTicks(m.getRealTicks());
//        gsb.setRealTime(m.getRealTime());
//        gsb.setTriggerRate(m.getTriggerRate());
        beliefManager.put(gsb);
    }

    @Override
    public void handleDetectionMessage(CanberraDetectionMessage m)
    {
        CanberraDetectionBelief blf = new CanberraDetectionBelief();
        blf.setCount(m.getCount());
        blf.setFilteredDoseRate(m.getFilteredDoseRate());
        blf.setMissionAccumulatedDose(m.getMisssionAccumulatedDose());
        blf.setPeakRate(m.getPeakRate());
        blf.setRelativeTime(m.getRelativeTime());
        blf.setTemperature(m.getTemperature());
        blf.setUnfilteredDoseRate(m.getUnfilteredDoseRate());
        
        beliefManager.put(blf);
    }
}
