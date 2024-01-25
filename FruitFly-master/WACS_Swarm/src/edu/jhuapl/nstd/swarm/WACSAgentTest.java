package edu.jhuapl.nstd.swarm;


import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.actions.podAction;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterfaceTest;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.swarm.action.SensorPerception;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.ModeWeights;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.util.MonitorClient;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class WACSAgentTest
{
	private BehaviorGroup behaviorGroup;

	/**
	 * The unique id for the agent.
	 */
	private String id;

	/**
	 * The belief manager that stores and shares belief.
	 */
	private BeliefManager belMgr;

	/**
	 * The update manager that controls the main thread.
	 */
	private UpdateManager upMgr;

	
    
    podAction _heartbeat;
	
    
    
	/**
	 * Constructor that takes the id, beliefmanager, and update manager.
	 *
	 * @param id
	 *			the unique id
	 * @param belMgr
	 *			belief manager that will share beliefs across the swarm
	 * @param upMgr
	 *			update managet that controls the main thread
	 */
	public WACSAgentTest(String id, BeliefManager belMgr, UpdateManager upMgr, int planeID)
    {

        this.id = id;
        this.belMgr = belMgr;
        // this.upMgr = upMgr;

        Logger.getLogger("GLOBAL").setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        //ch.setFormatter(new SwarmFormatter(false));
        try
        {
            String path = Config.getConfig().getProperty("WACSAgent.logging.path", "./logs");
            if(!(path.endsWith("/") || path.endsWith("\\")))
                path = path + "/";
            boolean success = new File(path).mkdirs();
            FileHandler fh = new FileHandler(path+"WACSOutput%g.log");
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

        //SearchBehavior searchBehavior = new SearchBehavior(belMgr, id);
		
		

        //FriendlyInfluenceBehavior friendly = new FriendlyInfluenceBehavior(belMgr, id);
		//SearchCloudBehavior cloudBehavior = new SearchCloudBehavior(belMgr, id);
		//behaviorGroup.addBehavior(friendly);

        LoiterBehavior loiterBehavior = new LoiterBehavior(belMgr, id);
        
        behaviorGroup = new BehaviorGroup(belMgr, modeMap, id);
        behaviorGroup.addBehavior(loiterBehavior);
        

		ModeWeights loiterMode = new ModeWeights(new Mode(LoiterBehavior.MODENAME), behaviorGroup);
        loiterMode.addWeight(loiterBehavior, new Double(1.0));
        	modeMap.addMode(loiterMode);

		ModeWeights trackCloudMode = new ModeWeights(new Mode("track"), behaviorGroup);
        trackCloudMode.addWeight(loiterBehavior, new Double(1.0));
        	modeMap.addMode(trackCloudMode);

		ModeWeights reaquireCloudMode = new ModeWeights(new Mode("reacquire"), behaviorGroup);
        reaquireCloudMode.addWeight(loiterBehavior, new Double(1.0));
        	modeMap.addMode(reaquireCloudMode);



		// Set the weights for the behaviors
		ModeWeights searchMode = new ModeWeights(new Mode("search"), behaviorGroup);
		//searchMode.addWeight(friendly, new Double(1.0));

		// add the modes to the mode map
		modeMap.addMode(searchMode);

		AgentModeActualBelief defaultMode = new AgentModeActualBelief(id, new Mode("search"));
		belMgr.put(defaultMode);

		ClassificationBelief cBelief = new ClassificationBelief(id,id,
				Classification.FRIENDLY);
		belMgr.put(cBelief);

        LatLonAltPosition lla = new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat"), Angle.DEGREES), new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon"), Angle.DEGREES), new Altitude(Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet"), Length.FEET));
		AgentPositionBelief pos = new AgentPositionBelief(id, lla, NavyAngle.NORTH);
		belMgr.put(pos);

        //add a gimbal target initially to look at start location
        
        AnacondaStateBelief ssb = new AnacondaStateBelief(id, AnacondaModeEnum.Standby);
        belMgr.put(ssb);

		// TODO only one mode
		//RuleSet ruleSet = new RuleSet(modeMap);
		//ModeSpaceBehavior modeBehavior = new ModeSpaceBehavior(belMgr, ruleSet,id);

		// Create the Actions
        
		
        
        
        
        
        cbrnPodsInterfaceTest m_Pods = new cbrnPodsInterfaceTest(belMgr);
        m_Pods.setVisible(true);
        
        _heartbeat = new podAction(belMgr, id, m_Pods.shareInterface());

            upMgr.register(behaviorGroup, Updateable.BEHAVIOR);
		SensorPerception perception = new SensorPerception(belMgr, id);
		//upMgr.register(modeBehavior, Updateable.MODE);
		upMgr.register(perception, Updateable.ACTION);

		// Create and add other updateable actions
       upMgr.register(_heartbeat, Updateable.ACTION);


        
	}

	/**
	 * main method that creates the simulated agent, this will run the simulated
	 * agent.
	 *
	 * @param args
	 *			takes no arguments
	 */
	public static void main(String[] args) {

		try {
			String name = WACSAgent.AGENTNAME;
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
            catch(Exception e)
            {
                planeID = 0;
            }
			BeliefManagerImpl belMgr = new BeliefManagerImpl(name);
			SearchBelief searchBel = new SearchBelief(name);
			belMgr.put(searchBel);
			WACSAgentTest a = new WACSAgentTest(name, belMgr, upMgr, planeID);
			upMgr.register(belMgr, Updateable.BELIEF);
			BeliefManagerClient client = new BeliefManagerClient(belMgr);
			m = new WACSMetrics(belMgr, upMgr);

			upMgr.start();

			while (true)
            {
				Thread.sleep(Long.MAX_VALUE);
			}
		}
		catch (Exception e)
        {
			e.printStackTrace();
		}
	}



}
