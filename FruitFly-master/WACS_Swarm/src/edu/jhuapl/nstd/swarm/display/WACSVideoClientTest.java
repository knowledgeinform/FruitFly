/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import java.io.File;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.jlib.net.IPAddress;
import edu.jhuapl.nstd.swarm.belief.NetworkConfig;
import edu.jhuapl.nstd.swarm.RobotUpdateManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSMetrics;
import edu.jhuapl.nstd.swarm.action.CloudMoverAction;
import edu.jhuapl.nstd.swarm.action.PiccoloGroundStationAction;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowDriver;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.util.Config;

/**
 *
 * @author xud1
 */
public class WACSVideoClientTest 
{
    public static void startGUI(BeliefManager beliefManager)
    {
        WACSVideoClientPanel videoClient = new WACSVideoClientPanel(beliefManager);
        
        javax.swing.JFrame testFrame = new javax.swing.JFrame();
        testFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        testFrame.setSize(320, 360);
        testFrame.add(videoClient);
        testFrame.setVisible(true);   
    }
    
    public static void main(String args[])
    {        
        BeliefManagerWacs believeManager = null;
        
        try
        {                    
            Thread.currentThread().setName("WACS-VideoClient");
            
            String ipaddr = IPAddress.getHostIPAddress();
            System.setProperty("java.rmi.server.hostname", ipaddr);
            String name = System.getProperty("agent.name");            
            
            NetworkConfig.createNetworkConfig(new File("config" + File.separatorChar + "networkConfig.txt"));
            
            // Create a Belief Manager            
            believeManager = new BeliefManagerWacs(name, false, true);
            RobotUpdateManager upMgr = new RobotUpdateManager();
            
            startGUI(believeManager);
            
            //SearchDisplay display = new SearchDisplay(believeManager);
            WACSDisplayAgent agent = new WACSDisplayAgent(name, believeManager, upMgr);
            
            WACSMetrics m = new WACSMetrics(believeManager, upMgr);

            CloudMoverAction cma = new CloudMoverAction(believeManager);
            //SearchMatrixReconciliator smr = new SearchMatrixReconciliator(belMgr);               
            //upMgr.register(smr, 150);
            upMgr.register(cma, Updateable.ACTION);            
            upMgr.register(agent, Updateable.DISPLAY);
            upMgr.register(believeManager, Updateable.BELIEF);
            //upMgr.register(display, Updateable.DISPLAY);
            upMgr.start();

            //create a BeliefManagerClient
            BeliefManagerClient client = new BeliefManagerClient(believeManager);
            
            while (true)
            {
                Thread.currentThread().sleep(Long.MAX_VALUE);
            }            
         }
          
        catch( Exception ex)
        {
            System.out.println(ex.getMessage());
        }
  
    }
}

