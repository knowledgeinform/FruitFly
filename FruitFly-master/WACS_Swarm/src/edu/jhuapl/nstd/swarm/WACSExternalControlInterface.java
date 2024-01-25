package edu.jhuapl.nstd.swarm;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import java.util.logging.Logger;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MonitorClient;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import javax.swing.JOptionPane;

public class WACSExternalControlInterface extends Thread
{
    static Properties s_safetyBoxConfig = new Properties();
    static private WACSAgentThread s_wacsAgentThread = new WACSAgentThread();
    static private BeliefManagerImpl s_beliefManager;
    static private LatLonAltPosition s_gimbalLookLocation;

    static public void main(String[] args)
    {
        StartWACSAgent();

        WACSExternalControlInterface externalControlInterface = new WACSExternalControlInterface();
        externalControlInterface.run();        
    }


    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(10777);

            Socket clientSocket = serverSocket.accept();

            serverSocket.close();

            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());


            boolean systemReady = false;
            while (!systemReady)
            {
                AgentPositionBelief agentPositionBelief = (AgentPositionBelief) s_beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                if (agentPositionBelief != null)
                {
                    //
                    // Wait for both applications to publish positions to indicate that
                    // they are initialized before proceeding.
                    //
                    PositionTimeName agentPositionTimeName = agentPositionBelief.getPositionTimeName(WACSAgent.AGENTNAME);
                    PositionTimeName displayPositionTimeName = agentPositionBelief.getPositionTimeName(WACSDisplayAgent.AGENTNAME);
                    if (agentPositionTimeName != null && displayPositionTimeName != null)
                    {
                        Thread.sleep(5000);

                        systemReady = true;

                        //
                        // Send WACSSystemReady Packet
                        //
                        outputStream.writeShort(4);
                        outputStream.writeShort(4);
                        outputStream.flush();
                    }
                }

                Thread.sleep(1000);
            }

            try
            {
                while (clientSocket.isConnected())
                {
                    short messageType = inputStream.readShort();
                    short messageLength = inputStream.readShort();


                    if (messageType == 0)
                    {
                        //start wacs agent, probably not used because agent is started immediately
                        if (messageLength == 4)
                        {
                            StartWACSAgent();
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Start-Agent packet of invalid length received on external control interface");
                        }
                    }
                    else if (messageType == 1)
                    {
                        //set gimbal target position
                        if (messageLength == 28)
                        {
                            double gimbalLat_deg = inputStream.readDouble();
                            double gimbalLon_deg = inputStream.readDouble();
                            double gimbalAltMSL_m = inputStream.readDouble();
                            SetGimbalLookLocation(gimbalLat_deg, gimbalLon_deg, gimbalAltMSL_m);
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Set-Gimbal-Look-Location packet of invalid length received on external control interface");
                        }
                    }
                    else if (messageType == 2)
                    {
                        //set mode, loiter or intercept
                        if (messageLength == 6)
                        {
                            short newModeId = inputStream.readShort();
                            if (newModeId == 0)
                            {
                                GoIntoLoiterMode();
                            }
                            else if (newModeId == 1)
                            {
                                GoIntoInterceptMode();
                            }
                            else
                            {
                                System.err.println("Mode-change packet with invalid Mode-ID received on external control interface");
                            }
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Change-Mode packet of invalid length received on external control interface");
                        }
                    }
                    else if(messageType == 3)
                    {
                        //stop agent, kill everything
                        if (messageLength == 4)
                        {
                            //StopWACSAgent();
                            System.exit(0);
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Stop-Agent packet of invalid length received on external control interface");
                        }
                    }
                    else if (messageType == 5)
                    {
                        //set loiter center position
                        if (messageLength == 20)
                        {
                            double centerLat_deg = inputStream.readDouble();
                            double centerLon_deg = inputStream.readDouble();
                            setLoiterCenterLocation(centerLat_deg, centerLon_deg);
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Set-Gimbal-Look-Location packet of invalid length received on external control interface");
                        }
                    }
                    else if (messageType == 6)
                    {
                        //set explosion time
                        if (messageLength == 12)
                        {
                            long explosionTimeMs = inputStream.readLong();
                            setExplosionTime(explosionTimeMs);
                        }
                        else
                        {
                            dumpUnrecognizedMessage(inputStream, messageLength);
                            System.err.println("Set-Explosion-Time packet of invalid length received on external control interface");
                        }
                    }
                    else
                    {
                        System.err.println("Packet with invalid message type received on external control interface");
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static private void dumpUnrecognizedMessage(DataInputStream inputStream, final int messageLength) throws java.io.IOException
    {
        for (int i = 0; i < (messageLength - 4); ++i)
        {
            inputStream.read();
        }
    }

    static private class WACSAgentThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
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
                s_beliefManager = new BeliefManagerImpl(name);
                SearchBelief searchBel = new SearchBelief(name);
                s_beliefManager.put(searchBel);
                WACSAgent a = new WACSAgent(name, s_beliefManager, upMgr, planeID);
                upMgr.register(s_beliefManager, Updateable.BELIEF);
                BeliefManagerClient client = new BeliefManagerClient(s_beliefManager);
                m = new WACSMetrics(s_beliefManager, upMgr);

                upMgr.start();

                while (true)
                {
                    Thread.sleep(2000);

                    //
                    // If another agent has modified the safety box, write the new safety box to file
                    // and then republish it under our own agent's name to let the other agents know
                    // that we got the new settings.
                    //
                    SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)s_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
                    if (safetyBoxBelief != null && !safetyBoxBelief.getPublishingAgentName().equals(WACSAgent.AGENTNAME))
                    {
                        if (safetyBoxBelief.getIsPermanent())
                        {        
                            s_safetyBoxConfig.setProperty("latitude1_deg", Double.toString(safetyBoxBelief.getLatitude1_deg()));
                            s_safetyBoxConfig.setProperty("latitude2_deg", Double.toString(safetyBoxBelief.getLatitude2_deg()));
                            s_safetyBoxConfig.setProperty("longitude1_deg", Double.toString(safetyBoxBelief.getLongitude1_deg()));
                            s_safetyBoxConfig.setProperty("longitude2_deg", Double.toString(safetyBoxBelief.getLongitude2_deg()));
                            s_safetyBoxConfig.setProperty("maxAltitude_m", Double.toString(safetyBoxBelief.getMaxAltitude_m()));
                            s_safetyBoxConfig.setProperty("maxAlt_IsAGL", Boolean.toString(safetyBoxBelief.getMinAlt_IsAGL()));
                            s_safetyBoxConfig.setProperty("maxRadius_m", Double.toString(safetyBoxBelief.getMaxRadius_m()));
                            s_safetyBoxConfig.setProperty("minRadius_m", Double.toString(safetyBoxBelief.getMinRadius_m()));
                            s_safetyBoxConfig.setProperty("minAltitude_m", Double.toString(safetyBoxBelief.getMaxAltitude_m()));
                            s_safetyBoxConfig.setProperty("minAlt_IsAGL", Boolean.toString(safetyBoxBelief.getMinAlt_IsAGL()));

                            FileOutputStream safetyBoxConfigFileOut = new FileOutputStream("config/safetyBox.properties");
                            s_safetyBoxConfig.store(safetyBoxConfigFileOut, "");
                            safetyBoxConfigFileOut.close();
                        }

                        //
                        // Republish the belief with this agent's name on it
                        //
                        safetyBoxBelief.setPublishingAgentName(WACSAgent.AGENTNAME);
                        s_beliefManager.put(safetyBoxBelief);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static public void StartWACSAgent()
    {
        s_wacsAgentThread.start();
    }

    static public void StopWACSAgent()
    {
        System.exit(0);
    }

    static public void SetGimbalLookLocation(final double latitude_deg, final double longitude_deg, final double altitudeMSL_m)
    {
        Latitude gimbalLookLatitude = new Latitude(latitude_deg, Angle.DEGREES);
        Longitude gimbalLookLongitude = new Longitude(longitude_deg, Angle.DEGREES);
        Altitude gimblaLookAltitudeMSL = new Altitude(altitudeMSL_m, Length.METERS);
        s_gimbalLookLocation = new LatLonAltPosition(gimbalLookLatitude,
                                                     gimbalLookLongitude,
                                                     gimblaLookAltitudeMSL);

        String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        TargetCommandedBelief targetBelief = new TargetCommandedBelief(WACSAgent.AGENTNAME, s_gimbalLookLocation, Length.ZERO, gimbalTargetName);
        s_beliefManager.put(targetBelief);
    }

    static public void GoIntoLoiterMode()
    {
        ExplosionTimeActualBelief timeBlf = (ExplosionTimeActualBelief)s_beliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME);
        if (timeBlf == null)
        {
            timeBlf = new ExplosionTimeActualBelief(WACSAgent.AGENTNAME, System.currentTimeMillis());
            s_beliefManager.put (timeBlf);
        }

        AgentModeCommandedBelief loiterModeBelief = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
        s_beliefManager.put(loiterModeBelief);
    }

    static public void GoIntoInterceptMode()
    {
        ExplosionBelief explosionBelief = new ExplosionBelief(s_gimbalLookLocation, System.currentTimeMillis());
        s_beliefManager.put(explosionBelief);

        AgentModeCommandedBelief trackingModeBelief = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(ParticleCloudPredictionBehavior.MODENAME));
        s_beliefManager.put(trackingModeBelief);
    }

    static public void setLoiterCenterLocation (double centerLatDecDeg, double centerLonDecDeg)
    {
        LatLonAltPosition centerPosition = new LatLonAltPosition (new Latitude (centerLatDecDeg, Angle.DEGREES),
                                                                    new Longitude (centerLonDecDeg, Angle.DEGREES),
                                                                    Altitude.ZERO);

        //Circular racetrack orbit, which means offset orbit with approach path.

        //Target needs to be at least "contact point distance" away from edge of orbit edge.  This would put contact point
        //on the radius.  Should be further away than that, so add a buffer factor, too
        //Or, if target is within orbit radius, let's center orbit on target automatically
        TargetActualBelief targets = (TargetActualBelief)s_beliefManager.get(TargetActualBelief.BELIEF_NAME);
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        if (targets != null && (targets.getPositionTimeName(tmp) != null || s_gimbalLookLocation != null))
        {
            LatLonAltPosition gimTargPos;
            if (s_gimbalLookLocation != null)
                gimTargPos = s_gimbalLookLocation;
            else
                gimTargPos = targets.getPositionTimeName(tmp).getPosition().asLatLonAltPosition();

            Length rangeFromCenterToTarget = gimTargPos.getRangeTo(centerPosition);

            WACSWaypointActualBelief wwb = (WACSWaypointActualBelief) s_beliefManager.get (WACSWaypointActualBelief.BELIEF_NAME);
            Length m_LoiterRadius = new Length (wwb.getLoiterRadius().getDoubleValue(Length.METERS), Length.METERS);

            Length firstRangeDistanceM = new Length (Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.FirstRangeDistanceFromTarget.Meters", 2500), Length.METERS);
            if (rangeFromCenterToTarget.isLessThan (m_LoiterRadius))
            {
                //Gimbal target is within loiter orbit.  Snap center of loiter to gimbal target
                JOptionPane.showMessageDialog(null, "Gimbal target and loiter center have been aligned!", "Warning", JOptionPane.WARNING_MESSAGE);
                RacetrackDefinitionCommandedBelief rtBlf = new RacetrackDefinitionCommandedBelief (gimTargPos);
                s_beliefManager.put(rtBlf);
                return;
            }
            else if(rangeFromCenterToTarget.isLessThan(m_LoiterRadius.plus(firstRangeDistanceM)))
            {
                //Gimbal target and loiter area are too close.  Show pop-up window and don't send it
                JOptionPane.showMessageDialog(null, "Gimbal target and loiter center and too close!  Loiter not updated!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        RacetrackDefinitionCommandedBelief rtBlf = new RacetrackDefinitionCommandedBelief (centerPosition);
        s_beliefManager.put(rtBlf);
    }

    static public void setExplosionTime (long explosionTimeMs)
    {
        //set explosion time, or set no explosion time (explode now)
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, explosionTimeMs);
        s_beliefManager.put (timeBlf);
    }
}
