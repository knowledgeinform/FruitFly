package edu.jhuapl.nstd.swarm;

import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.belief.AlphaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.GammaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;


public class RNSpoofer
{

    public static void main(String[] args)
    {
        try
        {
            BeliefManagerImpl beliefManager = new BeliefManagerImpl("RNSpoofer");
            RobotUpdateManager updateManager = new RobotUpdateManager();
            updateManager.register(beliefManager, Updateable.BELIEF);
            updateManager.start();

            BeliefManagerClient client = new BeliefManagerClient(beliefManager);

            Pic_Telemetry telemetry = new Pic_Telemetry();
            telemetry.AltWGS84 = 1;
            telemetry.AltMSL = 1;
            telemetry.IndAirSpeed_mps = 2;
            telemetry.Lat = 3;
            telemetry.Lon = 4;
            telemetry.OutsideAirTempC = 5;
            telemetry.PDOP = 6;
            telemetry.Pitch = 7;
            telemetry.Roll = 8;
            telemetry.StaticPressPa = 9;
            telemetry.TrueHeading = 10;
            telemetry.VelDown = 11;
            telemetry.VelEast = 12;
            telemetry.VelNorth = 13;
            telemetry.WindSouth = 14;
            telemetry.WindWest = 15;
            telemetry.Yaw = 16;
            PiccoloTelemetryBelief telemetryBelief = new PiccoloTelemetryBelief(WACSAgent.AGENTNAME, telemetry);
            beliefManager.put(telemetryBelief);

            int[] histogramData = {0, 1, 2, 3};
            AlphaCompositeHistogramBelief alphaHistogramBelief = new AlphaCompositeHistogramBelief(WACSAgent.AGENTNAME, histogramData, 1, 2);
            //beliefManager.put(alphaHistogramBelief);

            AlphaDetectionBelief alphaDetectionBelief = new AlphaDetectionBelief(WACSAgent.AGENTNAME, "Alpha Detection 0");
            //beliefManager.put(alphaDetectionBelief);

            GammaCompositeHistogramBelief gammaCompositeHistogramBelief = new GammaCompositeHistogramBelief(WACSAgent.AGENTNAME, histogramData, 1, 2);
            //beliefManager.put(gammaCompositeHistogramBelief);

            GammaDetectionBelief gammaDetectionBelief = new GammaDetectionBelief(WACSAgent.AGENTNAME, "Gamma Detection 0");
            //beliefManager.put(gammaDetectionBelief);

            //ParticleDetectionBelief particleDetectionBelief = new ParticleDetectionBelief(WACSAgent.AGENTNAME, "Particle Detection 0");
            //beliefManager.put(particleDetectionBelief);

            while (true)
            {
                telemetry.AltWGS84 += 1;
                telemetry.AltMSL += 1;
                telemetryBelief.setPiccoloTelemetry(telemetry);
                beliefManager.put(telemetryBelief);

                histogramData[0] += 1;
                alphaHistogramBelief.setHistogramData(histogramData);
                //beliefManager.put(alphaHistogramBelief);

                alphaDetectionBelief = new AlphaDetectionBelief(WACSAgent.AGENTNAME, "Alpha Detection " + histogramData[0]);
                //beliefManager.put(alphaDetectionBelief);

                gammaCompositeHistogramBelief.setHistogramData(histogramData);
                //beliefManager.put(gammaCompositeHistogramBelief);

                gammaDetectionBelief = new GammaDetectionBelief(WACSAgent.AGENTNAME, "Gamma Detection " + histogramData[0]);
                //beliefManager.put(gammaDetectionBelief);

                //particleDetectionBelief = new ParticleDetectionBelief(WACSAgent.AGENTNAME, "Particle Detection " + histogramData[0]);
                //beliefManager.put(particleDetectionBelief);

                Thread.sleep(1000);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
