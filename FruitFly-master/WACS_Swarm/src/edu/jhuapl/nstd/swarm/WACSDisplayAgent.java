/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.jlib.net.IPAddress;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.NetworkConfig;
import edu.jhuapl.nstd.swarm.belief.NoGoBelief;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.belief.SearchGoalBelief;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.display.SearchDisplay;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.action.*;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowDriver;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionListBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdRawMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.NmeaRawMessageBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.display.AglDisplay;
import edu.jhuapl.nstd.swarm.display.GcsDisplay;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.wacs.GCSSatCommMessageAbritrator;
import edu.jhuapl.nstd.swarm.wacs.PodSatCommMessageAribtrator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.*;
import org.jfree.data.time.Millisecond;

/**
 *
 * @author stipeja1
 */
public class WACSDisplayAgent implements Updateable
{

    private BehaviorGroup behaviorGroup;
	/**
	 * Stores the unique id of the robot.
	 */
	public final static String AGENTNAME = "display";
	/**
	 * The belief manager for the robot.
	 */
	private BeliefManager belMgr;
	/**
	 * The update manager that controls the "main" thread.
	 */
	private UpdateManager upMgr;
	//private InertialStates _inertialState = new InertialStates();

        
        private GCSSatCommMessageAbritrator m_SatCommHandler;
        
        private BufferedWriter _etdLogWriter;
        private BufferedWriter _nmeaLogWriter;
        private BufferedWriter _rawEtdLogWriter;
        private Long nmeaRawMessageUpdateTime = 0L;
        private Long etdRawUpdateTime = 0L;
        private Long etdUpdateTime = 0L;
        Boolean _shouldFilter = false;

   public WACSDisplayAgent(String id, BeliefManager belMgr, UpdateManager upMgr)
   {
       _shouldFilter = Config.getConfig().getPropertyAsBoolean("Etd.shouldFilter", false);
       
        if (!id.equals(AGENTNAME))
        {
            System.err.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println ("Mismatch in agent names!!!");
            System.err.println ("    Specified: " + id);
            System.err.println ("    Required : " + AGENTNAME);
            System.err.println ("Mismatch in agent names!!!");
            System.err.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.exit(0);
        }
        this.belMgr = belMgr;

        

		// NO BEHAVIORS

        String name = System.getProperty("agent.name");
			if (name == null)
            {
				System.err.println("You must specify an agent name as a system property, -Dagent.name=");
				System.exit(1);
			}

		// inject our classification as a base
	//	ClassificationBelief cBelief = new ClassificationBelief(id,id,Classification.PERSON_TARGET);
	//	belMgr.put(cBelief);

		// inject an empty NoGo Belief
		NoGoBelief nogoB = new NoGoBelief(id);
		belMgr.put(nogoB);

        LatLonAltPosition lla = new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat"), Angle.DEGREES), new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon"), Angle.DEGREES), new Altitude(Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet"), Length.FEET));
	AgentPositionBelief pos = new AgentPositionBelief(id, lla, NavyAngle.NORTH);
	belMgr.put(pos);

        //double x = Config.getConfig().getPropertyAsDouble("agent.startx", 20.0);
	//double y = Config.getConfig().getPropertyAsDouble("agent.starty", 20.0);
	//AbsolutePosition p = getStartPosition(x, y);

        
        METPositionTimeName met = new METPositionTimeName(name, new NavyAngle(0.0, Angle.DEGREES), new Speed(5.0,Speed.KNOTS), pos.getPositionTimeName(id).getPosition(), new Date());
        METPositionBelief metBel = new METPositionBelief(name, met);
        belMgr.put(metBel);

        METTimeName mt = new METTimeName(WACSAgent.AGENTNAME, new NavyAngle(0.0, Angle.DEGREES), new Speed(5.0,Speed.KNOTS),  new Date());
        METBelief mBel = new METBelief(WACSAgent.AGENTNAME, mt);
        belMgr.put(mBel);
        //mBel = new METBelief(WACSAgent.AGENTNAME, mt);
        //belMgr.put(mBel);
        
        //EtdDetectionBelief etd = new EtdDetectionBelief(WACSAgent.AGENTNAME);
        //belMgr.put(etd);

        double laf = Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterAltAGL_ft", 1000.0);
        double las = Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterAltAGLStandoff_ft", 2000.0);
        double lr = Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterRad_m", 1000.0);
        double ia = Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptMinAltAGL_ft", 300.0);
	double ir = Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptRad_m", 244.0);
        //Make a default belief in the past, so that if something else has already updated these settings, we don't override them
        WACSWaypointCommandedBelief wwb = new WACSWaypointCommandedBelief(WACSDisplayAgent.AGENTNAME,new Altitude(ia,Length.FEET),
                                                                  new Length(ir,Length.METERS),
                                                                  new Altitude(laf,Length.FEET),
                                                                  new Altitude(las,Length.FEET),
                                                                  new Length(lr,Length.METERS),
                                                                    new Date (1000));
        belMgr.put(wwb);

        //create a test search goal belief
        Logger.getLogger("GLOBAL").info("creating searchgoalbelief");
        SearchGoalBelief goal = new SearchGoalBelief(name);
        Logger.getLogger("GLOBAL").info("creating searchbelief");
        SearchBelief searchBel = new SearchBelief(name);

        belMgr.put(goal);
        belMgr.put(searchBel);
        
        //I don't think this does anything useful
        /*ConfigBelief configBel = new ConfigBelief(name,
        NetworkConfig.getConfig().getProperties());
        belMgr.put(configBel);*/

		if (!Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate", false))
        {

			// set up action to listen to GPS receiver

			// set up sensor to listen to Wind sensor (whatever that is)

		}
        else
        {

			// TODO: implement, instantiate, and register simulated sensor classes
			//       (sensor to inject wind sensor beliefs) -> (or this can be done through GUI)

			// only register ourselves with the update manager if we are in simulation (to provide the base's location)
			upMgr.register(this, Updateable.BELIEF);

			System.out.println("simulating");
		}

   
        boolean enableSatCommMessages = Config.getConfig().getPropertyAsBoolean("SatComm.Enabled", false);
        if (enableSatCommMessages)
        {
            try
            {
                String sendVsmIpAddrStr = Config.getConfig().getProperty("SatComm.GcsSendToVsm.IPAddr", "233.1.3.3");
                int sendVsmPort = Config.getConfig().getPropertyAsInteger("SatComm.GcsSendToVsm.Port", 57190);
                String recvVsmIpAddrStr = Config.getConfig().getProperty("SatComm.GcsRecvFromVsm.IPAddr", "233.1.3.3");
                int recvVsmPort = Config.getConfig().getPropertyAsInteger("SatComm.GcsRecvFromVsm.Port", 57190);
                int beliefNetworkAllowableLatencyMs = Config.getConfig().getPropertyAsInteger("SatComm.BeliefNetworkAllowableLatency.Ms", 5000);
                
                InetAddress sendVsmIpAddr = Inet4Address.getByName(sendVsmIpAddrStr);
                InetAddress recvVsmIpAddr = Inet4Address.getByName(recvVsmIpAddrStr);
                m_SatCommHandler = new GCSSatCommMessageAbritrator((BeliefManagerWacs)belMgr, sendVsmIpAddr, sendVsmPort, recvVsmIpAddr, recvVsmPort, beliefNetworkAllowableLatencyMs);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
   }
   
   private void setNmeaLogger(BufferedWriter nmeaLogWriter) {
       _nmeaLogWriter = nmeaLogWriter;
   }
   
   private void setEtdLogger(BufferedWriter etdLogWriter) {
       _etdLogWriter = etdLogWriter;
   }
   
   private void setRawEtdLogger(BufferedWriter rawEtdLogWriter) {
       _rawEtdLogWriter = rawEtdLogWriter;
   }

   private static void initLookAndFeel(String lnf, String theme)
   {
        String lookAndFeel = null;

        if (lnf != null) {
            if (lnf.equals("Metal")) {
              lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";

            }

            else if (lnf.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            }

            else if (lnf.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            }

            else if (lnf.equals("GTK")) {
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            }

            else {
                System.err.println("Unexpected value of lnf specified: "
                                   + lnf);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {


                UIManager.setLookAndFeel(lookAndFeel);

                // If L&F = "Metal", set the theme

                if (lnf.equals("Metal"))
                {
                  if (theme.equals("Ocean"))
                     MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                  else
                     MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());

                  UIManager.setLookAndFeel(new MetalLookAndFeel());
                }




            }

            catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            }

            catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            }

            catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }


   private boolean m_PublishBasePosition = true;
   public void update()
    {
        if (m_PublishBasePosition)
        {
            //only publish this once since it's not changing
                LatLonAltPosition lla = new LatLonAltPosition(new Latitude(Config.getConfig().getPropertyAsDouble("agent.startLat"), Angle.DEGREES), new Longitude(Config.getConfig().getPropertyAsDouble("agent.startLon"), Angle.DEGREES), new Altitude(Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet"), Length.FEET));
                AgentPositionBelief pos = new AgentPositionBelief(AGENTNAME, lla, NavyAngle.NORTH);
                belMgr.put(pos);
                //m_PublishBasePosition = false;
        }
		// put out a single position belief as the base station location (will not change)
//		double x = Config.getConfig().getPropertyAsDouble("agent.startx", 20.0);
//		double y = Config.getConfig().getPropertyAsDouble("agent.starty", 20.0);
//		AbsolutePosition p = getStartPosition(x, y);
//		AgentPositionBelief positionBelief = new AgentPositionBelief(id, p, NavyAngle.NORTH);
//		try {
//			belMgr.put(positionBelief);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}


        EtdRawMessageBelief etdRawMessageBelief = (EtdRawMessageBelief) belMgr.get(EtdRawMessageBelief.BELIEF_NAME); 
        if(etdRawMessageBelief!=null) {
            synchronized(etdRawMessageBelief) {
                if(etdRawMessageBelief.getTime()>etdRawUpdateTime) {
                    try {
                        _rawEtdLogWriter.write("Time: " + etdRawMessageBelief.getTime() + ", " + etdRawMessageBelief.getRawMessage());
                        _rawEtdLogWriter.newLine();
                        _rawEtdLogWriter.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    etdRawUpdateTime = etdRawMessageBelief.getTime();
                }
            }
        }
        
        EtdDetectionListBelief etdDetectionListBelief = (EtdDetectionListBelief) belMgr.get(EtdDetectionListBelief.BELIEF_NAME);
        if (etdDetectionListBelief != null) {
            synchronized(etdDetectionListBelief) {
                List<EtdDetection> etdDetections = etdDetectionListBelief.getDetections();
                
                /*
                if(_shouldFilter) {
                    etdDetections = etdDetectionListBelief.getFilteredDetections();
                }
                */
                
                for(int i=0; i<etdDetections.size(); i++) {
                    EtdDetection etd = etdDetections.get(i);

                    long time = etd.getTime();
                    long deltaTime = System.currentTimeMillis()-time;
                    LatLonAltPosition pos = etd.getPosition().asLatLonAltPosition();
                    Latitude lat = pos.getLatitude();
                    Longitude lon = pos.getLongitude();
                    Altitude alt = pos.getAltitude();
                    Float concentration = etd.getConcentration();

                    if(time>etdUpdateTime) {
                        try {
                            _etdLogWriter.write("Time: " + time + ", Lat: " + lat + ", Lon: " + lon + ", Alt: " + alt + ", Conc: " + concentration);
                            _etdLogWriter.newLine();
                            _etdLogWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }       

                        etdUpdateTime = etd.getTime();
                    }
                }
            }
        }
        
        NmeaRawMessageBelief nmeaRawMessageBelief = (NmeaRawMessageBelief) belMgr.get(NmeaRawMessageBelief.BELIEF_NAME); 
        if(nmeaRawMessageBelief!=null) {
            synchronized(nmeaRawMessageBelief) {
                if(nmeaRawMessageBelief.getTime()>nmeaRawMessageUpdateTime) {
                    try {
                        _nmeaLogWriter.write("Time: " + nmeaRawMessageBelief.getTime() + ", " + nmeaRawMessageBelief.getNmeaRawMessage());
                        _nmeaLogWriter.newLine();
                        _nmeaLogWriter.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    nmeaRawMessageUpdateTime = nmeaRawMessageBelief.getTime();
                }
            }
        }        
        

    }


	public AbsolutePosition getStartPosition(double x, double y)
    {
		AbsolutePosition start = GridFactory.STARTING_POSITION.translatedBy(new RangeBearingHeightOffset(new Length(x, Length.METERS),
																										 NavyAngle.EAST, Length.ZERO));
		start = start.translatedBy(new RangeBearingHeightOffset(new Length(y, Length.METERS),
																NavyAngle.NORTH, Length.ZERO));
		return start;
	}

   public static void main(String[] args)
   {
        File rawEtdOutFile;
        try {
            new File(".\\rawEtdLogs").mkdirs();
            rawEtdOutFile = new File (".\\rawEtdLogs\\rawEtdLog_display_" + System.currentTimeMillis() + ".txt");
            rawEtdOutFile.createNewFile();
        } catch (Exception e) {
            System.out.println("Error opening raw ETD log file");
            return;
        }
        
        File etdOutFile;
        try {
            new File(".\\etdLogs").mkdirs();
            etdOutFile = new File (".\\etdLogs\\etdLog_display_" + System.currentTimeMillis() + ".txt");
            etdOutFile.createNewFile();
        } catch (Exception e) {
            System.out.println("Error opening ETD log file");
            return;
        }        
        
        File nmeaOutFile;
        try {
            new File("./rawNmeaLogs").mkdirs();
            nmeaOutFile = new File (".\\rawNmeaLogs\\rawNmeaLog_display_" + System.currentTimeMillis() + ".txt");
            nmeaOutFile.createNewFile();
        } catch (Exception e) {
            System.out.println("Error opening NMEA Log file");
            return;
        }        
        
        try (BufferedWriter nmeaLogWriter = new BufferedWriter(new FileWriter(nmeaOutFile))) {
            try (BufferedWriter etdLogWriter = new BufferedWriter(new FileWriter(etdOutFile))) {
                try (BufferedWriter rawEtdLogWriter = new BufferedWriter(new FileWriter(rawEtdOutFile))) {
                    // Set System L&F
                    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    boolean useAdvancedDisplay = Config.getConfig().getPropertyAsBoolean("WACSDisplay.UseAdvancedDisplay", true);

                    Thread.currentThread().setName("WACS-DisplayAgent");
                    if (useAdvancedDisplay)
                        initLookAndFeel("Metal", "Ocean");
                    else
                        initLookAndFeel("System", null);
                    // initLookAndFeel("--", null);
                     //initLookAndFeel("Metal", "Ocean");
                    // initLookAndFeel("Metal", "");
                    //initLookAndFeel("GTK", null);
                    //initLookAndFeel("System", null);
                    //initLookAndFeel("Motif", null);

                     DtedGlobalMap.getDted();

                     String ipaddr = IPAddress.getHostIPAddress();
                     System.setProperty("java.rmi.server.hostname", ipaddr);

                     String name = System.getProperty("agent.name");

                     NetworkConfig.createNetworkConfig(new File("config" + File.separatorChar + "networkConfig.txt"));

                     //create a Belief Manager
                     BeliefManagerWacs belMgr = new BeliefManagerWacs(name, false, true);
                     RobotUpdateManager upMgr = new RobotUpdateManager();

                     if (Config.getConfig().getPropertyAsBoolean("FlightControl.usePiccoloGroundStationAction", false))
                     {
                         PiccoloGroundStationAction groundStationAction = new PiccoloGroundStationAction(belMgr);
                         upMgr.register(groundStationAction, 149);
                     }
                     else if (Config.getConfig().getPropertyAsBoolean("FlightControl.useShadowDriver", false))
                     {
                         if (Config.getConfig().getPropertyAsBoolean("FlightControl.ControlUavThroughGCS", true))
                         {
                             ShadowAutopilotInterface shadowAutopilotInterface = new ShadowAutopilotInterface(belMgr);
                             shadowAutopilotInterface.start();
                             ShadowDriver shadowDriver = new ShadowDriver(belMgr, WACSAgent.AGENTNAME, shadowAutopilotInterface);
                             shadowDriver.start();
                         }
                     }

                     Updateable display = null;
                     if (useAdvancedDisplay)
                         display = new SearchDisplay(belMgr);
                     else
                         display = new GcsDisplay(belMgr);
                     WACSDisplayAgent agent = new WACSDisplayAgent(name, belMgr, upMgr);
                     agent.setEtdLogger(etdLogWriter);
                     agent.setNmeaLogger(nmeaLogWriter);
                     agent.setRawEtdLogger(rawEtdLogWriter);

                     MissionErrorManager missionErrorManager = MissionErrorManager.getInstance(belMgr);

                     WACSMetrics m = new WACSMetrics(belMgr, upMgr);


                     CloudMoverAction cma = new CloudMoverAction(belMgr);
                     //SearchMatrixReconciliator smr = new SearchMatrixReconciliator(belMgr);



                     //upMgr.register(smr, 150);
                     upMgr.register(cma, Updateable.ACTION);

                     upMgr.register(agent, Updateable.DISPLAY);
                     upMgr.register(belMgr, Updateable.BELIEF);
                     upMgr.register(display, Updateable.DISPLAY);
                     upMgr.start();

                     //create a BeliefManagerClient
                     BeliefManagerClient client = new BeliefManagerClient(belMgr);

                     while (true)
                     {
                         Thread.currentThread().sleep(Long.MAX_VALUE);
                     }
                } catch (Exception e) {
                     e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
  }

}
