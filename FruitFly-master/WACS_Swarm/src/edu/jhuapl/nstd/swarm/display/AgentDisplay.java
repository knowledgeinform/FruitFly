//=============================== UNCLASSIFIED ================================== 
// 
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory 
// Developed by JHU/APL. 
// 
// This material may be reproduced by or for the U.S. Government pursuant to 
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988). 
// For any other permissions please contact JHU/APL. 
// 
//=============================== UNCLASSIFIED ================================== 
 
// File created: Tue Nov  9 13:10:34 1999 
 
package edu.jhuapl.nstd.swarm.display;

import java.util.logging.*; 
 
import java.lang.*; 




import java.io.*; 
 
import edu.jhuapl.nstd.swarm.*; 
import edu.jhuapl.nstd.swarm.action.*; 
import edu.jhuapl.nstd.swarm.comms.*; 
import edu.jhuapl.jlib.net.*; 
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.NetworkConfig;
import edu.jhuapl.nstd.swarm.belief.NoGoBelief;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.belief.SearchGoalBelief;
import edu.jhuapl.nstd.swarm.util.*; 
 
/** 
 * This is the class that implements the agent 
 * 
 * <P>UNCLASSIFIED 
 * 
 * @author Chad Hawthorne ext. 3728 
 */ 
 
public class AgentDisplay{ 
 
  public AgentDisplay(){} 
 
  public static void main(String[] args){ 
 
    String devicePath = Config.getConfig().getProperty("driveCommand");  
    System.err.println("DevicePath: "+devicePath); 
 
    try{ 
      String ipaddr = IPAddress.getHostIPAddress(); 
      System.setProperty("java.rmi.server.hostname", ipaddr); 
 
      String name = System.getProperty("agent.name"); 
      if (name == null){ 
      	System.err.println("You must specify an agent name as a system property, -Dagent.name="); 
      	System.exit(1); 
      } 
       
		NetworkConfig.createNetworkConfig(new File("config"  
			+ File.separatorChar + "networkConfig.txt")); 
 
      //create a Belief Manager 
      BeliefManagerImpl belMgr = new BeliefManagerImpl(name); 
      RobotUpdateManager upMgr = new RobotUpdateManager(); 
       
      //create a test search goal belief 
	  Logger.getLogger("GLOBAL").info("creating searchgoalbelief");
      SearchGoalBelief goal = new SearchGoalBelief(name);
	  Logger.getLogger("GLOBAL").info("creating searchbelief");
      SearchBelief searchBel = new SearchBelief(name);

      	//belMgr.put(globalDefaults); 
      belMgr.put(goal);
      belMgr.put(searchBel);
      
      //I don't know what this is doing, but I removed it because we're getting rid of all the ConfigBelief usages
      /*ConfigBelief configBel = new ConfigBelief(name,
	  NetworkConfig.getConfig().getProperties());
          belMgr.put(configBel); 
          */
      
       
		String loadNoGo = null;
		if (Config.getConfig().hasProperty("AgentDisplay.loadNoGoFile") &&
				!(loadNoGo = Config.getConfig().getProperty("AgentDisplay.loadNoGoFile")).equals("")) 
		{
			try {
				File f = new File(loadNoGo);
				ObjectInputStream in = new ObjectInputStream( 
						new FileInputStream(f)); 
				NoGoBelief nogo = new NoGoBelief();
				nogo.readExternal(in);
				Logger.getLogger("GLOBAL").info("loaded " + f); 
				belMgr.put(nogo);
				in.close(); 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
		}

//			String addr = Config.getConfig().getProperty("AgentDisplay.listener.addr");
//			int port = Config.getConfig().getPropertyAsInteger("AgentDisplay.listener.port");
//			CBDEWSListener cbListener = null;
//			try {
//				cbListener = new CBDEWSListener(belMgr, addr, port);
//				cbListener.start();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
      SearchDisplay display = new SearchDisplay(belMgr); 
			//CotTracker tracker = new CotTracker(belMgr); 
 
			//VCTargeting vcTarget = new VCTargeting(1903,belMgr,Classification.ASSET); 
			//test of the obstacle avoidance generation  
      //ObstacleAvoidanceRegions avoidRegions = new ObstacleAvoidanceRegions(name, belMgr);  
      //try{  
        //avoidRegions.start();  
      //}  
      //catch (Exception e){  
        //e.printStackTrace();  
      //}  
 
      WACSMetrics m = new WACSMetrics(belMgr, upMgr);
 
			CloudMoverAction cma = new CloudMoverAction(belMgr); 
			SearchMatrixReconciliator smr = new SearchMatrixReconciliator(belMgr); 
			upMgr.register(smr, 150); 
			upMgr.register(cma, Updateable.ACTION); 
       
      upMgr.register(belMgr, Updateable.BELIEF); 
      upMgr.register(display, Updateable.DISPLAY); 
      //upMgr.register(tracker, Updateable.DISPLAY); 
      upMgr.start(); 
       
      //create a BeliefManagerClient 
      BeliefManagerClient client = new BeliefManagerClient(belMgr); 
       
      //create a simulated agent 
			if (Config.getConfig().getPropertyAsBoolean("SimulateComms", false)) { 
	  //    UAVAgent.createUAVAgent("agent2", upMgr,1);
  	 //  UAVAgent.createUAVAgent("agent3", upMgr,2);
  	 //   UAVAgent.createUAVAgent("agent4", upMgr,3);
			} 
       
       
       
 
       
      while(true){ 
	Thread.currentThread().sleep(Long.MAX_VALUE); 
      } 
    } 
    catch (Exception e){ 
      e.printStackTrace(); 
    } 
  } 
 
} 
 
//=============================== UNCLASSIFIED ================================== 
