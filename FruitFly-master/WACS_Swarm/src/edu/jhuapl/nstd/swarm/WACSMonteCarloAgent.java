package edu.jhuapl.nstd.swarm;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.Ellipse;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.net.IPAddress;
import edu.jhuapl.nstd.swarm.action.CloudMoverAction;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.CloudBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.NetworkConfig;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TrueWindSpeedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;
import edu.jhuapl.nstd.swarm.display.WACSMonteCarloDisplay;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;


public class WACSMonteCarloAgent implements Updateable
{
    private enum TestingState {SETUP, LOITER, TRACKING, COMLPETE};

    private TestingState m_testingState = TestingState.SETUP;
    private String m_agentName;
    private BeliefManager m_beliefManager;
    private UpdateManager m_updateManager;
    private LatLonAltPosition m_plumeStartLocation;
    private String m_plumeTrackingModeName;
    private int m_numIterationsToTest;
    private Speed m_minWindSpeed;
    private Speed m_maxWindSpeed;
    private float m_maxWindSpeedErrorPercentage;
    private float m_maxWindBearingError_deg;
    private float m_meanCloudRiseRate_ftpm;
    private float m_maxCloudRiseRateErrorPercentage;
    private Length m_initialPlumeRadius;
    private int m_iterationDuration_sec;
    private int m_currIterationNum = 0;
    private long m_currIterationStartTime_ms;
    private int m_unweightedHitTotal = 0;
    private int m_weightedHitTotal = 0;
    private long m_firstHitTime_ms = 0;
    private long m_lastHitTime_ms = 0;
    private double m_timeAccelerationCoefficient;
    private int m_loiterOrbitRadius_m;
    private List<IterationData> m_performanceData = new LinkedList<IterationData>();
    private BufferedWriter m_logFileWriter;
    private IterationData m_currIterationData;
    private double m_PlumeTopAltitudeCoeffMax;
    private double m_PlumeTopAltitudeCoeffMin;
    private double m_PlumeThicknessToTopAltitudeRatioMin;
    private double m_PlumeThicknessToTopAltitudeRatioMax;
    private double m_ExplosionTNTEquivLbs;

    
    static private BeliefManagerImpl s_beliefManager;
    static private CloudMoverAction s_cloudMoverAction;

    private class IterationData
    {
        public double trueWindSpeed_knots;
        public double perceivedWindSpeed_knots;
        public double trueWindDirection_deg;
        public double perceivedWindDirection_deg;
        public double plumeRiseRate_ftpm;
        public int unweightedHitTotal;
        public int weightedHitTotal;
    }


    public WACSMonteCarloAgent(final String agentName, BeliefManager beliefManager, UpdateManager updateManager)
    {
        m_agentName = agentName;
        m_beliefManager = beliefManager;
        m_updateManager = updateManager;

        m_plumeTrackingModeName = Config.getConfig().getProperty("MonteCarlo.plumeTrackingModeName");
        Latitude plumeStartLatitude = new Latitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLatitude_deg"), Angle.DEGREES);
        Longitude plumeStartLongitude = new Longitude(Config.getConfig().getPropertyAsDouble("MonteCarlo.plumeStartLongitude_deg"), Angle.DEGREES);
        Altitude plumeStartAltitudeMSL = DtedGlobalMap.getDted().getJlibAltitude(plumeStartLatitude.getDoubleValue(Angle.DEGREES),
                                                                        plumeStartLongitude.getDoubleValue(Angle.DEGREES));
        m_plumeStartLocation = new LatLonAltPosition(plumeStartLatitude, plumeStartLongitude, plumeStartAltitudeMSL);
        m_numIterationsToTest = Config.getConfig().getPropertyAsInteger("MonteCarlo.numIterations");
        m_minWindSpeed = new Speed(Config.getConfig().getPropertyAsDouble("MonteCarlo.minWindSpeed_knots"), Speed.KNOTS);
        m_maxWindSpeed = new Speed(Config.getConfig().getPropertyAsDouble("MonteCarlo.maxWindSpeed_knots"), Speed.KNOTS);
        m_meanCloudRiseRate_ftpm = (float)Config.getConfig().getPropertyAsDouble("MonteCarlo.averageCloudRiseRate_ftpm");
        m_maxWindSpeedErrorPercentage = (float)Config.getConfig().getPropertyAsDouble("MonteCarlo.maxWindSpeedErrorPercentage");
        m_maxWindBearingError_deg = (float)Config.getConfig().getPropertyAsDouble("MonteCarlo.maxWindBearingError_deg");
        m_maxCloudRiseRateErrorPercentage = (float)Config.getConfig().getPropertyAsDouble("MonteCarlo.maxCloudRiseRateErrorPercentage");
        m_initialPlumeRadius = new Length(Config.getConfig().getPropertyAsDouble("MonteCarlo.initialPlumeRadius_m"), Length.METERS);
        m_iterationDuration_sec = Config.getConfig().getPropertyAsInteger("MonteCarlo.iterationDuration_sec");
        m_timeAccelerationCoefficient = Config.getConfig().getPropertyAsDouble("SimulationMode.timeAccelerationCoefficient",1);
        m_loiterOrbitRadius_m = Config.getConfig().getPropertyAsInteger("MonteCarlo.loiterRadius_m");
        m_PlumeTopAltitudeCoeffMax = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCoeffMax", 9.8804);
        m_PlumeTopAltitudeCoeffMin = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCoeffMin", 4.8007);
        m_PlumeThicknessToTopAltitudeRatioMin = Config.getConfig().getPropertyAsDouble("PlumeRiseEquation.plumeThicknessToTopAltitudeRatioMin", 0.4);
        m_PlumeThicknessToTopAltitudeRatioMax = Config.getConfig().getPropertyAsDouble("PlumeRiseEquation.plumeThicknessToTopAltitudeRatioMax", 0.6);
        m_ExplosionTNTEquivLbs = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.explosionTNTEquivLbs", 108);

        try
        {
            m_logFileWriter = new BufferedWriter(new FileWriter("WACSMonteCarlo-output-" + System.currentTimeMillis() + ".log"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            String ipaddr = IPAddress.getHostIPAddress();
            System.setProperty("java.rmi.server.hostname", ipaddr);

            String agentName = System.getProperty("agent.name");

            NetworkConfig.createNetworkConfig(new File("config" + File.separatorChar + "networkConfig.txt"));
          
            s_beliefManager = new BeliefManagerImpl(agentName);
            RobotUpdateManager updateManager = new RobotUpdateManager();

            WACSMonteCarloAgent monteCarloAgent = new WACSMonteCarloAgent(agentName, s_beliefManager, updateManager);

            s_cloudMoverAction = new CloudMoverAction(s_beliefManager);
            updateManager.register(s_cloudMoverAction, Updateable.ACTION);


            WACSMonteCarloDisplay display = new WACSMonteCarloDisplay(s_beliefManager);
            updateManager.register(display, Updateable.DISPLAY);


            updateManager.register(monteCarloAgent, Updateable.DISPLAY);
            updateManager.register(s_beliefManager, Updateable.BELIEF);
            updateManager.setDaemon(false);
            updateManager.start();

            BeliefManagerClient client = new BeliefManagerClient(s_beliefManager);

            WACSWaypointCommandedBelief waypointBelief = new WACSWaypointCommandedBelief(WACSDisplayAgent.AGENTNAME,
                                                                       new Altitude(300, Length.FEET),
                                                                       new Length(800, Length.FEET),
                                                                       new Altitude(800, Length.FEET),
                                                                       new Altitude(800, Length.FEET),
                                                                       new Length(1, Length.MILES));
            s_beliefManager.put(waypointBelief);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void update()
    {

        if (m_testingState == TestingState.SETUP)
        {
            m_currIterationData = new IterationData();

            //
            // Generate the wind speed randomly and publish it
            //
            Speed trueWindSpeed = new Speed((Math.random() * (m_maxWindSpeed.minus(m_minWindSpeed)).getDoubleValue(Speed.KNOTS)), Speed.KNOTS).plus(m_minWindSpeed);
            NavyAngle trueWindBearing = new NavyAngle(Math.random() * 360, Angle.DEGREES);
            TrueWindSpeedBelief trueWindSpeedBelief = new TrueWindSpeedBelief(trueWindSpeed, trueWindBearing);
            m_beliefManager.put(trueWindSpeedBelief);
            m_currIterationData.trueWindSpeed_knots = trueWindSpeed.getDoubleValue(Speed.KNOTS);
            m_currIterationData.trueWindDirection_deg = trueWindBearing.getDoubleValue(Angle.DEGREES);

            //
            // Generate perceived wind speed and publish it
            //
            NavyAngle perceivedWindBearing = new NavyAngle((trueWindBearing.getDoubleValue(Angle.DEGREES) + (Math.random() * 2 - 1) * m_maxWindBearingError_deg) % 360.0 , Angle.DEGREES);
            Speed perceivedWindSpeed = trueWindSpeed.plus(trueWindSpeed.times((Math.random() * 2 - 1) * (m_maxWindSpeedErrorPercentage / 100)));
            METTimeName metTimeName = new METTimeName(WACSAgent.AGENTNAME, perceivedWindBearing, perceivedWindSpeed, new Date());
            METBelief metBelief = new METBelief(WACSAgent.AGENTNAME, metTimeName);
            m_beliefManager.put(metBelief);
            m_currIterationData.perceivedWindSpeed_knots = perceivedWindSpeed.getDoubleValue(Speed.KNOTS);
            m_currIterationData.perceivedWindDirection_deg = perceivedWindBearing.getDoubleValue(Angle.DEGREES);

            //
            // Generate the plume rise rate
            //
            double riseRate_ftpm = m_meanCloudRiseRate_ftpm + (m_meanCloudRiseRate_ftpm * (Math.random() * 2 - 1) * (m_maxCloudRiseRateErrorPercentage / 100));
            double plumeTopAltitudeCoeff = (Math.random() * (m_PlumeTopAltitudeCoeffMax - m_PlumeTopAltitudeCoeffMin)) + m_PlumeTopAltitudeCoeffMin;
            double plumeThicknessToTopAltitudeRatio = (Math.random() * (m_PlumeThicknessToTopAltitudeRatioMax - m_PlumeThicknessToTopAltitudeRatioMin)) + m_PlumeThicknessToTopAltitudeRatioMin;
            s_cloudMoverAction.setPlumeTopAltitudeCoeff(plumeTopAltitudeCoeff);
            s_cloudMoverAction.setPlumeThicknessToTopAltitudeRatio(plumeThicknessToTopAltitudeRatio);
            s_cloudMoverAction.setExplosionEquivalentTNTPounds(m_ExplosionTNTEquivLbs);
            m_currIterationData.plumeRiseRate_ftpm = riseRate_ftpm;

            //
            // Publish the gimbal target and loiter orbit
            //
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetCommandedBelief targetBelief = new TargetCommandedBelief(WACSAgent.AGENTNAME, m_plumeStartLocation, Length.ZERO, gimbalTargetName);
            m_beliefManager.put(targetBelief);

            AgentModeCommandedBelief loiterModeBelief = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
            m_beliefManager.put(loiterModeBelief);
            m_testingState = TestingState.LOITER;

            m_currIterationStartTime_ms = System.currentTimeMillis();
        }
        else if (m_testingState == TestingState.LOITER)
        {
            //
            // Wait until the plane has reached the loiter orbit
            //
            boolean reachedLoiterOrbit = false;
            AgentPositionBelief agentPositionBelief = (AgentPositionBelief)s_beliefManager.get(AgentPositionBelief.BELIEF_NAME);
            if (agentPositionBelief != null)
            {
                PositionTimeName positionTimeName = agentPositionBelief.getPositionTimeName(WACSAgent.AGENTNAME);
                if (positionTimeName != null)
                {
                    LatLonAltPosition currPosition = positionTimeName.getPosition().asLatLonAltPosition();

                    Length distanceToOrbitCenter = currPosition.getRangeTo(m_plumeStartLocation);
                    if (Math.abs(distanceToOrbitCenter.getDoubleValue(Length.METERS) - m_loiterOrbitRadius_m) < (m_loiterOrbitRadius_m * 0.01))
                    {
                        reachedLoiterOrbit = true;
                    }
                }
            }

            if (reachedLoiterOrbit)
            {
                createSimulatedPlume();

                AgentModeCommandedBelief trackingModeBelief = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(m_plumeTrackingModeName));
                m_beliefManager.put(trackingModeBelief);                
                
                m_testingState = TestingState.TRACKING;
            }
        }
        else if (m_testingState == TestingState.TRACKING)
        {
            long newLastHitTime_ms = 0;
            CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief)m_beliefManager.get(CloudDetectionBelief.BELIEF_NAME);
            if (cloudDetectionBelief != null)
            {
                List<CloudDetection> detections = cloudDetectionBelief.getDetections();
                for (CloudDetection detection : detections)
                {
                    if (m_firstHitTime_ms == 0)
                    {
                        m_firstHitTime_ms = detection.getTime();
                    }
                    else if (detection.getTime() < m_firstHitTime_ms)
                    {
                        m_firstHitTime_ms = detection.getTime();
                    }

                    if (detection.getTime() > m_lastHitTime_ms)
                    {
                        if (detection.getTime() > newLastHitTime_ms)
                        {
                            newLastHitTime_ms = detection.getTime();
                        }

                        ++m_unweightedHitTotal;
                        m_weightedHitTotal += detection.getScaledValue() * detection.getScaledValue();
                    }
                }

                if (newLastHitTime_ms > m_lastHitTime_ms)
                {
                    m_lastHitTime_ms = newLastHitTime_ms;
                }
            }

            if ((System.currentTimeMillis() - m_currIterationStartTime_ms) >= (m_iterationDuration_sec * 1000 / m_timeAccelerationCoefficient))
            {
                m_testingState = TestingState.SETUP;
                ++m_currIterationNum;

                
                m_currIterationData.unweightedHitTotal = m_unweightedHitTotal;
                m_currIterationData.weightedHitTotal = m_weightedHitTotal;
                m_performanceData.add(m_currIterationData);

                m_unweightedHitTotal = 0;
                m_weightedHitTotal = 0;

                try
                {
                    m_logFileWriter.write("Iteration Num: ");
                    m_logFileWriter.write(String.format("%d", m_currIterationNum));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("True Wind Speed (knots): ");
                    m_logFileWriter.write(String.format("%f", m_currIterationData.trueWindSpeed_knots));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("Perceived Wind Speed (knots): ");
                    m_logFileWriter.write(String.format("%f", m_currIterationData.perceivedWindSpeed_knots));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("True Wind Direction (deg): ");
                    m_logFileWriter.write(String.format("%f", m_currIterationData.trueWindDirection_deg));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("Perceived Wind Direction (deg): ");
                    m_logFileWriter.write(String.format("%f", m_currIterationData.perceivedWindDirection_deg));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("Plume Rise Rate (ftpm): ");
                    m_logFileWriter.write(String.format("%f", m_currIterationData.plumeRiseRate_ftpm));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("Unweighted Hit Total: ");
                    m_logFileWriter.write(String.format("%d", m_currIterationData.unweightedHitTotal));
                    m_logFileWriter.newLine();
                    m_logFileWriter.write("Weighted Hit Total: ");
                    m_logFileWriter.write(String.format("%d", m_currIterationData.weightedHitTotal));
                    m_logFileWriter.newLine();
                    m_logFileWriter.newLine();
                    m_logFileWriter.newLine();
                    m_logFileWriter.newLine();
                    m_logFileWriter.flush();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (m_currIterationNum == m_numIterationsToTest)
            {
                m_testingState = TestingState.COMLPETE;

                try
                {
                    m_logFileWriter.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //MonteCarloResultsForm resultsForm = new MonteCarloResultsForm();
                //resultsForm.setResults(m_plumeTrackingModeName, m_unweightedHitTotal, m_weightedHitTotal, (int)((m_lastHitTime_ms - m_firstHitTime_ms) / 1000));
                //resultsForm.setVisible(true);
                JOptionPane.showMessageDialog(null, "Simulation Complete.");
                System.exit(0);
            }
        }
    }

    private void createSimulatedPlume()
    {
        //
        // Creates the simulated plume that will be displayed on the map
        //
        Ellipse ellipse = new Ellipse(m_plumeStartLocation, m_plumeStartLocation, m_initialPlumeRadius);
        CloudBelief cloudBelief = new CloudBelief(m_agentName,
                                         ellipse,
                                         new Length(Config.getConfig().getPropertyAsDouble("SimulationMode.initialSimulatedCloudHeight_ft", 0), Length.FEET),
                                         new Altitude(Config.getConfig().getPropertyAsDouble("initialSimulatedCloudAltitudeAGL_ft", 0), Length.FEET));
        m_beliefManager.put(cloudBelief);

        
        //
        // Publishes the position and time of the explosion for the tracking behavior to use
        //
        ExplosionBelief explosionBelief = new ExplosionBelief(m_plumeStartLocation, System.currentTimeMillis());
        m_beliefManager.put(explosionBelief);
    }
}
