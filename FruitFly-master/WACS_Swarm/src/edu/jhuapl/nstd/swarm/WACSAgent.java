package edu.jhuapl.nstd.swarm;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.actions.podAction;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterfaceTest;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.swarm.action.MissionManagerAction;
import edu.jhuapl.nstd.swarm.action.SimulateCloudSensingAction;
import edu.jhuapl.nstd.swarm.action.SimulatedRobotDriver;
import edu.jhuapl.nstd.swarm.autopilot.PiccoloAutopilotAction;
import edu.jhuapl.nstd.swarm.autopilot.PiccoloAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowDriver;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.OrbitPathingBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.IrCameraFOVBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.ModeWeights;
import edu.jhuapl.nstd.swarm.belief.PlumeDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PodCommandBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.display.ChemDetectionSimulator;
import edu.jhuapl.nstd.swarm.display.IbacDetectionSimulator;
import edu.jhuapl.nstd.swarm.display.BridgeportDetectionSimulator;
import edu.jhuapl.nstd.swarm.display.EtdErrorsPanel;
import edu.jhuapl.nstd.swarm.display.EtdPanel;
import edu.jhuapl.nstd.swarm.display.EtdHistoryPanel;
import edu.jhuapl.nstd.swarm.display.EtdTempPanel;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MonitorClient;
import edu.jhuapl.nstd.swarm.util.TASEGimbalInterface;
import edu.jhuapl.nstd.swarm.wacs.PodSatCommMessageAribtrator;
import java.awt.Dimension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class WACSAgent
{

    private BehaviorGroup behaviorGroup;
    /**
     * The unique id for the agent.
     */
    public static final String AGENTNAME = System.getProperty("agent.name"); //"wacsagent";
    /**
     * The belief manager that stores and shares belief.
     */
    private BeliefManager belMgr;
    /**
     * The update manager that controls the main thread.
     */
    private UpdateManager upMgr;
    /**
     * The Swarm Client for connecting to the control Mediator
     */
    PiccoloAutopilotInterface _api;
    //WACS_Etd_Interface _etd;
//    TASEGimbalInterface _tgi;
    podAction _pod;
    MissionManagerAction m_MissionManagerAction;
    cbrnPodsInterfaceTest m_Pods;
    ImageProcessingThread _imageProcessingThread = new ImageProcessingThread();
    static Properties s_safetyBoxConfig = new Properties();
    
    //If true, we should regenerate the safety box config file because we are updating its format
    private boolean m_ForceSafetyBoxFileRegen = false;
    
    private PodSatCommMessageAribtrator m_SatCommHandler;
    private ShadowOnboardAutopilotInterface m_ShadowAutopilotInterface;
    
    private EtdPanel m_EtdPanel;
    private EtdHistoryPanel m_EtdHistoryPanel;
    private EtdErrorsPanel m_EtdErrorsPanel;

    private ReplayNmeaEtdLogs m_replayNmeaEtdLogs;

    /**
     * Constructor that takes the id, belief manager, and update manager.
     *
     * @param id
     *			the unique id
     * @param belMgr
     *			belief manager that will share beliefs across the swarm
     * @param upMgr
     *			update manager that controls the main thread
     */
    public WACSAgent(String id, BeliefManager belMgr, UpdateManager upMgr, int planeID)
    {
        if (id.equals(null)) //(!id.equals(AGENTNAME))
        {
            System.err.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println ("Mismatch in agent names!!!");
            System.err.println ("    Specified: null");
            //System.err.println ("    Required : " + AGENTNAME);
            System.err.println ("Mismatch in agent names!!!");
            System.err.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.exit(0);
        }
        this.belMgr = belMgr;
        // this.upMgr = upMgr;

        Logger.getLogger("GLOBAL").setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        //ch.setFormatter(new SwarmFormatter(false));

        try
        {
            String path = Config.getConfig().getProperty("WACSAgent.logging.path", "./logs");
            if (!(path.endsWith("/") || path.endsWith("\\")))
            {
                path = path + "/";
            }
            boolean success = new File(path).mkdirs();
            FileHandler fh = new FileHandler(path + "WACSOutput%g.log");
            //fh.setFormatter(new SwarmFormatter(true));
            Logger.getLogger("GLOBAL").addHandler(fh);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Logger.getLogger("GLOBAL").addHandler(ch);
        ModeMap modeMap = new ModeMap(id);
        behaviorGroup = new BehaviorGroup(belMgr, modeMap, id);

        float maxIrFovFromForwardDeg = (float)Config.getConfig().getPropertyAsDouble("WACSAgent.IrFovFromForward.Maximum.Deg", 5);
        float minIrFovFromForwardDeg = (float)Config.getConfig().getPropertyAsDouble("WACSAgent.IrFovFromForward.Minimum.Deg", -100);
        
        //Limits are defined assuming camera is on left wing.  So, max is positive degrees clock-wise from front.  If camera
        //is on right right, the min is positive degrees counter-clockwise from front.
        boolean reverseCameraLimits = Config.getConfig().getPropertyAsBoolean("FlightControl.CameraOnRightWing", false);
        if (reverseCameraLimits)
        {
            float temp = maxIrFovFromForwardDeg;
            maxIrFovFromForwardDeg = -minIrFovFromForwardDeg;
            minIrFovFromForwardDeg = -temp;
        }
        
        IrCameraFOVBelief fovBlf = new IrCameraFOVBelief(maxIrFovFromForwardDeg, minIrFovFromForwardDeg);
        belMgr.put(fovBlf);

        //uncomment the following commented lines if wanting to revert to old behaviors


        //SearchAngleBehavior searchBehavior = new SearchAngleBehavior(belMgr, id);
        //MapCloudAltitudeBehavior altitudeBehavior = new MapCloudAltitudeBehavior(belMgr, id);

        LoiterBehavior loiterBehavior = new LoiterBehavior(belMgr, id);
        ParticleCloudPredictionBehavior particleCloudPredictionBehavior = new ParticleCloudPredictionBehavior(belMgr, id);
        OrbitPathingBehavior orbitPathingBehavior = new OrbitPathingBehavior(belMgr, id);
        //ReacquireCloudBehavior reaquireCloudBehavior = new ReacquireCloudBehavior(belMgr, id);


        behaviorGroup = new BehaviorGroup(belMgr, modeMap, id);
        behaviorGroup.addBehavior(loiterBehavior);
        behaviorGroup.addBehavior(particleCloudPredictionBehavior);
        behaviorGroup.addBehavior(orbitPathingBehavior);
        //behaviorGroup.addBehavior(reaquireCloudBehavior);
        //behaviorGroup.addBehavior(searchBehavior);


        ModeWeights loiterMode = new ModeWeights(new Mode(LoiterBehavior.MODENAME), behaviorGroup);
        loiterMode.addWeight(loiterBehavior, new Double(1.0));
        loiterMode.addWeight(particleCloudPredictionBehavior, new Double(1.0));
        loiterMode.addWeight(orbitPathingBehavior, new Double(1.0));
        //loiterMode.addWeight(reaquireCloudBehavior, new Double(1.0));
        modeMap.addMode(loiterMode);

        ModeWeights particleCloudPredictionMode = new ModeWeights(new Mode(ParticleCloudPredictionBehavior.MODENAME), behaviorGroup);
        particleCloudPredictionMode.addWeight(loiterBehavior, new Double(1.0));
        particleCloudPredictionMode.addWeight(particleCloudPredictionBehavior, new Double(1.0));
        particleCloudPredictionMode.addWeight(orbitPathingBehavior, new Double(1.0));
        //particleCloudPredictionMode.addWeight(reaquireCloudBehavior, new Double(1.0));
        modeMap.addMode(particleCloudPredictionMode);


//	ModeWeights reaquireCloudMode = new ModeWeights(new Mode("reacquire"), behaviorGroup);
//        reaquireCloudMode.addWeight(loiterBehavior, new Double(1.0));
//        reaquireCloudMode.addWeight(particleCloudPredictionBehavior, new Double(1.0));
//        reaquireCloudMode.addWeight(reaquireCloudBehavior, new Double(1.0));
//	modeMap.addMode(reaquireCloudMode);



        // Set the weights for the behaviors
        ModeWeights searchMode = new ModeWeights(new Mode("idle"), behaviorGroup);

        // add the modes to the mode map
        modeMap.addMode(searchMode);

        AgentModeActualBelief defaultMode = new AgentModeActualBelief(id, "idle", new Date (0));
        belMgr.put(defaultMode);

        ClassificationBelief cBelief = new ClassificationBelief(id, id, Classification.FRIENDLY);
        belMgr.put(cBelief);

        LatLonAltPosition lla = new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat"), Angle.DEGREES), new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon"), Angle.DEGREES), new Altitude(Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet"), Length.FEET));
        AgentPositionBelief pos = new AgentPositionBelief(id, lla, NavyAngle.NORTH);
        AgentBearingBelief bearing = new AgentBearingBelief(id, NavyAngle.NORTH, NavyAngle.NORTH);
        belMgr.put(pos);
        belMgr.put(bearing);

        /*double gimbalLat = Config.getConfig().getPropertyAsDouble("agent.GimbalTarget.defaultLatD");
        double gimbalLon = Config.getConfig().getPropertyAsDouble("agent.GimbalTarget.defaultLonD");
        LatLonAltPosition gimbal_lla = new LatLonAltPosition(new Latitude(gimbalLat, Angle.DEGREES), new Longitude(gimbalLon, Angle.DEGREES), new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(gimbalLat, gimbalLon), Length.METERS));
        //add a gimbal target initially to look at start location
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        int clas = Config.getConfig().getPropertyAsInteger(
                "WACSAgent.gimbalTargetClassification", Classification.ASSET);
        belMgr.put(new TargetBelief(id, gimbal_lla, Length.ZERO, tmp));
        belMgr.put(new ClassificationBelief(id, tmp, clas));*/

        // Create the Actions
        _api = new PiccoloAutopilotInterface(belMgr, id, planeID);
        _api.start();
        //_etd = new WACS_Etd_Interface(belMgr, id);

        try
        {
//            _tgi = new TASEGimbalInterface(belMgr, id, planeID);
        }
        catch (Exception e)
        {
            //TODO uncomment
            //e.printStackTrace();
        }


        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate", false))
        {
            if (!Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation"))
            {
                SimulatedRobotDriver driver = null;
                driver = new SimulatedRobotDriver(belMgr, id,
                                                  behaviorGroup);
                upMgr.register(driver, Updateable.ACTION);
            }

            Logger.getLogger("GLOBAL").info("simulating");
        }
        else
        {
//            PiccoloAutopilotAction paa = new PiccoloAutopilotAction(belMgr, id, behaviorGroup, _api, _tgi, planeID);
//            upMgr.register(paa, Updateable.ACTION);
        }


        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate", false) ||
                Config.getConfig().getPropertyAsBoolean("WACSAgent.simulateOnlyCloudSensing", false))
        {
            SimulateCloudSensingAction simulateCloudSensingAction = new SimulateCloudSensingAction(belMgr, id);
            upMgr.register(simulateCloudSensingAction, Updateable.ACTION);

            Logger.getLogger("GLOBAL").info("simulating cloud sensing");
        }



        m_Pods = new cbrnPodsInterfaceTest(belMgr);
        m_Pods.setVisible(false);
        
        Dimension minDim = new Dimension(1000,500);  
        m_EtdPanel = new EtdPanel(belMgr);
        m_EtdPanel.setMinimumSize(minDim);
        JFrame m_etdFrame = new JFrame();
        m_etdFrame.setSize(minDim);
        m_etdFrame.add(m_EtdPanel);
        m_etdFrame.setVisible(true);
        upMgr.register(m_EtdPanel, Updateable.DISPLAY);   
        
        m_EtdHistoryPanel = new EtdHistoryPanel(belMgr);
        m_EtdHistoryPanel.setMinimumSize(minDim);
        JFrame m_etdHistoryFrame = new JFrame();
        m_etdHistoryFrame.setSize(minDim);
        m_etdHistoryFrame.add(m_EtdHistoryPanel);
        m_etdHistoryFrame.setVisible(true);
        upMgr.register(m_EtdHistoryPanel, Updateable.DISPLAY);
        
        m_EtdErrorsPanel = new EtdErrorsPanel(belMgr);
        m_EtdErrorsPanel.setMinimumSize(minDim);
        JFrame m_etdErrorsFrame = new JFrame();
        m_etdErrorsFrame.setSize(minDim);
        m_etdErrorsFrame.add(m_EtdErrorsPanel);
        m_etdErrorsFrame.setVisible(true);
        upMgr.register(m_EtdErrorsPanel, Updateable.DISPLAY);

        _pod = new podAction(belMgr, id, m_Pods.shareInterface());
        m_MissionManagerAction = new MissionManagerAction(belMgr);
        
        if(Config.getConfig().getProperty("Etd.source").equalsIgnoreCase("File") || Config.getConfig().getProperty("Piccolo.source").equalsIgnoreCase("File")) {
            m_replayNmeaEtdLogs = new ReplayNmeaEtdLogs();
            
            if(Config.getConfig().getProperty("Etd.source").equalsIgnoreCase("File")) {
                _pod.setReplayNmeaEtdLogs(m_replayNmeaEtdLogs);
            }
            
            if(Config.getConfig().getProperty("Piccolo.source").equalsIgnoreCase("File")) {
                _api.setReplayNmeaEtdLogs(m_replayNmeaEtdLogs);
            }
        }

        upMgr.register(_pod, Updateable.ACTION);
        upMgr.register(m_MissionManagerAction, Updateable.ACTION);
        
        upMgr.register(behaviorGroup, Updateable.BEHAVIOR);
        /*SensorPerception perception = new SensorPerception(belMgr, id);
        upMgr.register(perception, Updateable.ACTION);*/

        // Create and add other updateable actions


        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.ImageProcessingThreadEnabled", false))
        {
            _imageProcessingThread.start();
        }
        else
        {
        }
        
        //Default 'Allow Intercept'
        AllowInterceptActualBelief actualAllow = new AllowInterceptActualBelief (WACSAgent.AGENTNAME, false);
        belMgr.put(actualAllow);

        //
        // Load the initial safety box from file
        //
        try
        {
            FileInputStream safetyBoxConfigFileIn = new FileInputStream("config/safetyBox.properties");
            s_safetyBoxConfig.load(safetyBoxConfigFileIn);
            safetyBoxConfigFileIn.close();
            double safetyBoxLatitude1_deg = Double.parseDouble(s_safetyBoxConfig.getProperty("latitude1_deg"));
            double safetyBoxLatitude2_deg = Double.parseDouble(s_safetyBoxConfig.getProperty("latitude2_deg"));
            double safetyBoxLongitude1_deg = Double.parseDouble(s_safetyBoxConfig.getProperty("longitude1_deg"));
            double safetyBoxLongitude2_deg = Double.parseDouble(s_safetyBoxConfig.getProperty("longitude2_deg"));
            double safetyBoxMaxRadius_m = Double.parseDouble(s_safetyBoxConfig.getProperty("maxRadius_m"));
            double safetyBoxMinRadius_m = Double.parseDouble(s_safetyBoxConfig.getProperty("minRadius_m"));
            double safetyBoxMaxAltitude_m, safetyBoxMinAltitude_m;
            boolean safetyBoxMaxAlt_IsAGL, safetyBoxMinAlt_IsAGL;
            
            if (s_safetyBoxConfig.getProperty("maxAlt_IsAGL") == null)
            {
                //old style safety box file.  Read MSL and regenerate safety box file with new format.
                double safetyBoxMaxAltitudeMSL_m = Double.parseDouble(s_safetyBoxConfig.getProperty("maxAltitudeMSL_m"));
                double safetyBoxMinAltitudeMSL_m = Double.parseDouble(s_safetyBoxConfig.getProperty("minAltitudeMSL_m"));
                
                s_safetyBoxConfig.remove("maxAltitudeMSL_m");
                s_safetyBoxConfig.remove("minAltitudeMSL_m");
                s_safetyBoxConfig.remove("maxAltitudeAGL_m");
                s_safetyBoxConfig.remove("minAltitudeAGL_m");
                
                m_ForceSafetyBoxFileRegen = true;
                safetyBoxMaxAltitude_m = safetyBoxMaxAltitudeMSL_m;
                safetyBoxMinAltitude_m = safetyBoxMinAltitudeMSL_m;
                safetyBoxMaxAlt_IsAGL = false;
                safetyBoxMinAlt_IsAGL = false;
            }
            else
            {
                //new style safety box file
                m_ForceSafetyBoxFileRegen = false;
                safetyBoxMaxAltitude_m = Double.parseDouble(s_safetyBoxConfig.getProperty("maxAltitude_m"));
                safetyBoxMinAltitude_m = Double.parseDouble(s_safetyBoxConfig.getProperty("minAltitude_m"));
                safetyBoxMaxAlt_IsAGL = Boolean.parseBoolean(s_safetyBoxConfig.getProperty("maxAlt_IsAGL"));
                safetyBoxMinAlt_IsAGL = Boolean.parseBoolean(s_safetyBoxConfig.getProperty("minAlt_IsAGL"));
            }
            
            SafetyBoxBelief safetyBoxBelief = new SafetyBoxBelief(WACSAgent.AGENTNAME,
                                                                  safetyBoxLatitude1_deg,
                                                                  safetyBoxLongitude1_deg,
                                                                  safetyBoxLatitude2_deg,
                                                                  safetyBoxLongitude2_deg,
                                                                  safetyBoxMaxAltitude_m,
                                                                  safetyBoxMaxAlt_IsAGL,
                                                                  safetyBoxMinAltitude_m,
                                                                  safetyBoxMinAlt_IsAGL,
                                                                  safetyBoxMaxRadius_m,
                                                                  safetyBoxMinRadius_m,
                                                                  true);
            belMgr.put(safetyBoxBelief);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (!Config.getConfig().getPropertyAsBoolean("FlightControl.ControlUavThroughGCS", true))
        {
            m_ShadowAutopilotInterface = new ShadowOnboardAutopilotInterface(belMgr);
            ShadowDriver shadowDriver = new ShadowDriver(belMgr, WACSAgent.AGENTNAME, m_ShadowAutopilotInterface);
            shadowDriver.start();
        }
        
        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Anaconda.SimulateDialogPresent", false))
        {
            new ChemDetectionSimulator(_pod).setVisible(true);
        }
        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Ibac.SimulateDialogPresent", false))
        {
            new IbacDetectionSimulator(_pod).setVisible(true);
        }
        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Bridgeport.SimulateDialogPresent", false))
        {
            new BridgeportDetectionSimulator(_pod).setVisible(true);
        }
        
        boolean enableSatCommMessages = Config.getConfig().getPropertyAsBoolean("SatComm.Enabled", false);
        if (enableSatCommMessages)
        {
            try
            {
                String sendVsmIpAddrStr = Config.getConfig().getProperty("SatComm.PodSendToVsm.IPAddr", "233.1.3.3");
                int sendVsmPort = Config.getConfig().getPropertyAsInteger("SatComm.PodSendToVsm.Port", 57190);
                String recvVsmIpAddrStr = Config.getConfig().getProperty("SatComm.PodRecvFromVsm.IPAddr", "233.1.3.3");
                int recvVsmPort = Config.getConfig().getPropertyAsInteger("SatComm.PodRecvFromVsm.Port", 57190);
                int statusPeriodMs = Config.getConfig().getPropertyAsInteger("SatComm.PodStatusMsgPeriod.Ms", 5000);
                int connectionTimeoutSec = Config.getConfig().getPropertyAsInteger("SatComm.SensorConnectionTimeout.Sec", 20);
                int beliefNetworkAllowableLatencyMs = Config.getConfig().getPropertyAsInteger("SatComm.BeliefNetworkAllowableLatency.Ms", 5000);

                InetAddress sendVsmIpAddr = Inet4Address.getByName(sendVsmIpAddrStr);
                InetAddress recvVsmIpAddr = Inet4Address.getByName(recvVsmIpAddrStr);
                m_SatCommHandler = new PodSatCommMessageAribtrator((BeliefManagerWacs)belMgr, sendVsmIpAddr, sendVsmPort, recvVsmIpAddr, recvVsmPort, statusPeriodMs, connectionTimeoutSec, beliefNetworkAllowableLatencyMs, m_ShadowAutopilotInterface);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * main method that creates the simulated agent, this will run the simulated
     * agent.
     *
     * @param args
     *			takes no arguments
     */
    public static void main(String[] args)
    {

        try
        {
            Thread.currentThread().setName ("WACS-WACSAgent");
            String name = System.getProperty("agent.name");
            MonitorClient monitor = new MonitorClient();
            monitor.listen();
            RobotUpdateManager upMgr = new RobotUpdateManager();
            Metrics m = null;
            Logger.getLogger("GLOBAL").info("starting " + name);
            int planeID;
            try
            {
                planeID = Integer.valueOf(System.getProperty("planeID")).intValue();
            }
            catch (Exception e)
            {
                planeID = 0;
            }
            BeliefManagerWacs belMgr = new BeliefManagerWacs(name, true, false);  
            SearchBelief searchBel = new SearchBelief(name);
            belMgr.put(searchBel);
            WACSAgent a = new WACSAgent(name, belMgr, upMgr, planeID);
            upMgr.register(belMgr, Updateable.BELIEF);
            BeliefManagerClient client = new BeliefManagerClient(belMgr);
            m = new WACSMetrics(belMgr, upMgr);

            upMgr.start();

            long lastSavedSafetyBox = -1;
            while (true)
            {
                Thread.sleep(2000);

                //
                // If another agent has modified the safety box, write the new safety box to file
                // and then republish it under our own agent's name to let the other agents know
                // that we got the new settings.
                //
                SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)belMgr.get(SafetyBoxBelief.BELIEF_NAME);
                if (safetyBoxBelief != null && safetyBoxBelief.getTimeStamp().getTime() > lastSavedSafetyBox && (!safetyBoxBelief.getPublishingAgentName().equals(WACSAgent.AGENTNAME) || a.m_ForceSafetyBoxFileRegen || safetyBoxBelief.getName().equals (SafetyBoxBeliefSatComm.BELIEF_NAME)))
                {
                    if (safetyBoxBelief.getIsPermanent())
                    {        
                        s_safetyBoxConfig.setProperty("latitude1_deg", Double.toString(safetyBoxBelief.getLatitude1_deg()));
                        s_safetyBoxConfig.setProperty("latitude2_deg", Double.toString(safetyBoxBelief.getLatitude2_deg()));
                        s_safetyBoxConfig.setProperty("longitude1_deg", Double.toString(safetyBoxBelief.getLongitude1_deg()));
                        s_safetyBoxConfig.setProperty("longitude2_deg", Double.toString(safetyBoxBelief.getLongitude2_deg()));
                        s_safetyBoxConfig.setProperty("maxAltitude_m", Double.toString(safetyBoxBelief.getMaxAltitude_m()));
                        s_safetyBoxConfig.setProperty("maxAlt_IsAGL", Boolean.toString(safetyBoxBelief.getMaxAlt_IsAGL()));
                        s_safetyBoxConfig.setProperty("maxRadius_m", Double.toString(safetyBoxBelief.getMaxRadius_m()));
                        s_safetyBoxConfig.setProperty("minRadius_m", Double.toString(safetyBoxBelief.getMinRadius_m()));
                        s_safetyBoxConfig.setProperty("minAltitude_m", Double.toString(safetyBoxBelief.getMinAltitude_m()));
                        s_safetyBoxConfig.setProperty("minAlt_IsAGL", Boolean.toString(safetyBoxBelief.getMinAlt_IsAGL()));

                        FileOutputStream safetyBoxConfigFileOut = new FileOutputStream("config/safetyBox.properties");
                        s_safetyBoxConfig.store(safetyBoxConfigFileOut, "");
                        safetyBoxConfigFileOut.close();
                    }

                    //
                    // Republish the belief with this agent's name on it
                    //
                    safetyBoxBelief.setPublishingAgentName(WACSAgent.AGENTNAME);
                    lastSavedSafetyBox = safetyBoxBelief.getTimeStamp().getTime();
                    belMgr.put(safetyBoxBelief);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private class ImageProcessingThread extends Thread
    {
        public ImageProcessingThread ()
        {
            this.setName ("WACS-ImageProcessingThread");
        }

        private double thresh;
        private boolean prevmode;
        private long loiterTime;

        @Override
        public void run()
        {
            
            if (Config.getConfig().getPropertyAsBoolean("WACSAgent.wacs.showdebugimages", false))
            {
                setParameter("showImages", 1.0f);
                setParameter("showTrackerImages", 1.0f);
                setParameter("showDetections", 1.0f);
                setParameter("showAlignment", 1.0f);
            }
            else
            {
                setParameter("showImages", 0.0f);
                setParameter("showTrackerImages", 0.0f);
                setParameter("showDetections", 0.0f);
                setParameter("showAlignment", 0.0f);
            }
            
            prevmode = false;

            //detectionStatus(0.36563456f, 1023.12341434f);
            if (Config.getConfig().getPropertyAsBoolean("WACSAgent.doimageprocessing", false))
            {
                String recordingDir = Config.getConfig().getProperty("WACSAgent.RecordingDirectory", "d:\\recordings");
                
                System.out.println("POD calling image processing thread with recDirectory = " + recordingDir);
                imageProcessingThreadProc(recordingDir);
            }
        }

        public void detectionStatus(float alignScore, float detectionScore)
        {
            DecimalFormat.getNumberInstance().setMaximumFractionDigits(3);
            thresh = Config.getConfig().getPropertyAsDouble("WACSAgent.ImageProcessingThread.detectionthreshold", 200.0);
            //System.out.println("************* Align Score: " + alignScore + "***************\n********* Detection Score: " + detectionScore + "***************\n");
            PlumeDetectionBelief pd = new PlumeDetectionBelief(AGENTNAME, alignScore, detectionScore);
            belMgr.put(pd);

            AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) belMgr.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
            {
                if (!prevmode)
                {
                    loiterTime = System.currentTimeMillis();
                }
                prevmode = true;

                IrExplosionAlgorithmEnabledBelief allowBlf = (IrExplosionAlgorithmEnabledBelief) belMgr.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
                if (allowBlf == null || (allowBlf.getEnabled()))
                {
                    if (detectionScore > thresh)
                    {
                        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)belMgr.get(AllowInterceptActualBelief.BELIEF_NAME);
                        if (allowInterceptBlf != null && allowInterceptBlf.getAllow())
                        {
                            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                            TargetActualBelief targets = (TargetActualBelief) belMgr.get(TargetActualBelief.BELIEF_NAME);
                            if (targets != null)
                            {
                                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                                if (positionTimeName != null)
                                {
                                    belMgr.put(new ExplosionBelief(positionTimeName.getPosition(), System.currentTimeMillis()));

                                    AgentModeCommandedBelief newAgentModeBelief = new AgentModeCommandedBelief(AGENTNAME, new Mode(ParticleCloudPredictionBehavior.MODENAME));
                                    belMgr.put(newAgentModeBelief);
                                }
                            }
                        
                        }
                    }
                }
            }
            else
            {
                prevmode = false;
            }
        }

        //
        // Prototype of JNI native code method
        //
        private native void imageProcessingThreadProc(String recordingDirectory);

        private native void setParameter(String key, float value);
    }

    static
    {
        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.ImageProcessingThreadEnabled", false))
        {

            try
            {
                System.loadLibrary("PlumeDetector");

            }
            catch (UnsatisfiedLinkError e)
            {
                System.out.println(e.getLocalizedMessage());
                System.out.println(e.getMessage());
            }
        }
    }
}
