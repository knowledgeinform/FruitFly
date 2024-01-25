/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.kml;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.actions.podAction;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacParticleCountMessage;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author humphjc1
 */
public class KMLGenerator {

    MetricsReader metricsReader;
    PlumeFileReader plumeReader;
    KMLWriter writer;
    podAction action = new podAction(null, null, null);

    boolean logIbacResults = false;
    IbacResultsWriter ibacWriter = null;
    double m_PlumeTransparencyPercent = 40;
    
    private final static int MIN_HUE = 120;
    private final static int MAX_HUE = 350;
    private int _EtdMinDisplayValue = -75; // should get from metrics file b/c was a config, 0 is default
    private int _EtdMaxDisplayValue = -30; // should get from metrics file b/c was a config, 50 is default
    private Set<String> styles;
    private List<EtdDetection> _etdDetections;
    private long _lastEtdUpdateTime = 0L;
    private int _etdHistoryLength = 15;


    private KMLGenerator (String metricsFilename)
    {
        styles = new TreeSet<String>();
        _etdDetections = new LinkedList<EtdDetection>();
        _etdHistoryLength = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);
        run (metricsFilename, null, 0L, Long.MAX_VALUE, true, true);
    }

    private KMLGenerator (String metricsFilename, String plumeFilename, long startTime, long endTime, boolean useRawAnacondaHits, boolean useRawIbacHits)
    {
        styles = new TreeSet<String>();
        _etdDetections = new LinkedList<EtdDetection>();
        _etdHistoryLength = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);
        run (metricsFilename, plumeFilename, startTime, endTime, useRawAnacondaHits, useRawIbacHits);
    }
    
    private Color getColorForValue(float value) {
        if (value < _EtdMinDisplayValue) {
            value = _EtdMinDisplayValue;
        }
        
        if (value > _EtdMaxDisplayValue) {
            value = _EtdMaxDisplayValue;
        }
        
        float hue = MIN_HUE + (MAX_HUE - MIN_HUE) * (value - _EtdMinDisplayValue) / (_EtdMaxDisplayValue - _EtdMinDisplayValue) ;        
        return new Color(Color.HSBtoRGB(hue/360.0f, 1.0f, 1.0f));
    }
    
    private String convertColorToABGR(Color color) {
        return String.format("FF%02X%02X%02X", color.getBlue(), color.getGreen(), color.getRed());  
    }

    private void run (String metricsFilename, String plumeFilename, long startTime, long endTime, boolean useRawAnacondaHits, boolean useRawIbacHits)
    {
        String inputMetricsFilename = metricsFilename;
        String inputPlumeFilename = plumeFilename;
        String thresholdString = "";
        if (useRawIbacHits)
            thresholdString = action.getParticleThresholdString();
        else
            thresholdString = "CloudI";
        if (useRawAnacondaHits)
            thresholdString += "_RawA";
        else
            thresholdString += "_CloudA";

        String outputFilename = inputMetricsFilename.substring(0, inputMetricsFilename.lastIndexOf(".")) + "_" + thresholdString + ".kml";
        String ibacFilename = inputMetricsFilename.substring(0, inputMetricsFilename.lastIndexOf(".")) + "_ibac_" + thresholdString + ".csv";
        String description = "Generated from: " + inputMetricsFilename.substring(inputMetricsFilename.lastIndexOf("\\")+1) + "\r\n" + thresholdString;

        if (logIbacResults)
            ibacWriter = new IbacResultsWriter (ibacFilename, thresholdString);

        metricsReader = new MetricsReader(inputMetricsFilename);
        if (inputPlumeFilename != null)
            plumeReader = new PlumeFileReader(inputPlumeFilename);
        else
            plumeReader = null;

        //long finalTimestamp = metricsReader.getLastTimestamp();
        long finalTimestamp = endTime;
        /*
        if (finalTimestamp > endTime)
            finalTimestamp = endTime;
        else
            finalTimestamp = finalTimestamp + 1000;
        */
        boolean endLoop = (!metricsReader.stepForward());
        AgentPositionBelief lastPos = null;

        while (!endLoop)
        {
            try
            {
                long startTimeMillis = metricsReader.currTime;
                long endTimeMillis = -1;
                AgentPositionBelief pos = metricsReader.currAgentPositionBelief;
                AnacondaDetectionBelief anacondaRaw = metricsReader.currAnacondaDetectionBelief;
                ParticleDetectionBelief ibacRaw = metricsReader.currParticleDetectionBelief;
                CloudDetectionBelief anacondaCloud = metricsReader.currAnacondaCloudBelief;
                CloudDetectionBelief ibacCloud = metricsReader.currIbacCloudBelief;
                EtdDetectionBelief etdDetection = metricsReader.etdDetectionBelief;
                EtdDetectionMessageBelief etdDetectionMessage = metricsReader.etdDetectionMessage;
                EtdStatusMessageBelief etdStatusMessage = metricsReader.etdStatusMessage;

                if (!metricsReader.stepForward())
                {
                    endTimeMillis = finalTimestamp;
                    endLoop = true;
                }
                else
                {
                    endTimeMillis = metricsReader.currTime;
                }

                if (startTimeMillis > startTime && startTimeMillis < endTime)
                {
                    if (writer == null)
                    {
                        double baseLat = metricsReader.currAgentPositionBelief.getPositionTimeName("unk").getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
                        double baseLon = metricsReader.currAgentPositionBelief.getPositionTimeName("unk").getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
                        writer = new KMLWriter (outputFilename, inputMetricsFilename, description, baseLat, baseLon);
                    }

                    if (pos != null)
                    {
                        LatLonAltPosition llaPos = pos.getPositionTimeName("unk").getPosition().asLatLonAltPosition();
                        
                        if(llaPos.getAltitude().getDoubleValue(Length.METERS)<100.0) {
                            continue;
                        }
                        
                        writer.writePolygonBox(startTimeMillis, endTimeMillis, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getAltitude().getDoubleValue(Length.METERS),
                                                                                KMLWriter.blackPolyStyle, null, null);
                        

                        if (plumeReader != null)
                        {

                            PlumeFileReader.PlumeData plume = plumeReader.getDataAtTime (startTimeMillis/1000);
                            if (plume != null)
                            {
                                writer.writeTransparentSphere (startTimeMillis, endTimeMillis, plume.m_LatDeg, plume.m_LonDeg, plume.m_CenterAltM,
                                                                                    plume.m_xWidthM, plume.m_yDepthM, plume.m_zHeightM,
                                                                                    plume.m_BearingDeg, m_PlumeTransparencyPercent,
                                                                                    Color.GREEN, null, null);

                                //System.out.print ("plume alt: " + plume.m_AltM);
                                /*writer.writeTransparentSphere (startTimeMillis, endTimeMillis, 40.1666667,
                                                                                    -113.1666667,
                                                                                    llaPos.getAltitude().getDoubleValue(Length.METERS),
                                                                                    625, 125, 125, 10, 50,
                                                                                    Color.GREEN, null, null);*/
                            }
                        }
                        
                        if (lastPos != null)
                        {
                            LatLonAltPosition llaLastPos = lastPos.getPositionTimeName("unk").getPosition().asLatLonAltPosition();
                            writer.writePathLine(startTimeMillis, finalTimestamp, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getAltitude().getDoubleValue(Length.METERS),
                                                                                llaLastPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                llaLastPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                llaLastPos.getAltitude().getDoubleValue(Length.METERS));

                        }
                        
                        lastPos = pos;
                        
                        if(etdDetection!=null) {
                            EtdDetection detection = etdDetection.getEtdDetection();
                            if(detection.getTime()>_lastEtdUpdateTime) {
                                _etdDetections.add(detection);
                                _lastEtdUpdateTime = detection.getTime();
                            }
                            
                            /*
                            while(_etdDetections.size()>_etdHistoryLength) {
                                _etdDetections.remove(0);
                            }
                            */
                        }
                        


                        boolean anacondaDetection = false;
                        if (anacondaRaw != null && useRawAnacondaHits)
                        {
                            String dets = anacondaRaw.getAnacondaDetectionString();
                            String []detsSplit = dets.split (" ");
                            if (detsSplit.length > 3)
                            {
                                writer.writePolygonBox(startTimeMillis, finalTimestamp, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getAltitude().getDoubleValue(Length.METERS),
                                                                                KMLWriter.yellowPolyStyle, dets, null);
                                anacondaDetection = true;
                            }
                        }
                        if (anacondaCloud != null && !useRawAnacondaHits)
                        {
                            writer.writePolygonBox(startTimeMillis, finalTimestamp, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                llaPos.getAltitude().getDoubleValue(Length.METERS),
                                                                                KMLWriter.yellowPolyStyle, "hit", null);
                            anacondaDetection = true;
                        }

                        if (ibacRaw != null && useRawIbacHits)
                        {
                            String dets = ibacRaw.getParticleDetectionString();
                            String []detsSplit = dets.split (" ");
                            if (detsSplit.length > 1)
                            {
                                ibacParticleCountMessage msg = new ibacParticleCountMessage();
                                msg.setCLI(Integer.parseInt(detsSplit[1]));
                                msg.setCSI(Integer.parseInt(detsSplit[5]));

                                if (msg.getHitStrength() > 0)
                                {
                                    boolean alarm = false;
                                    if (action.handleParticleCountThresh(msg) > 0)
                                    {
                                        alarm = true;
                                        double altBump = 0;
                                        if (anacondaDetection)
                                        {
                                            altBump = 2;
                                        }
                                        writer.writePolygonBox(startTimeMillis, finalTimestamp, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                    llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                    llaPos.getAltitude().getDoubleValue(Length.METERS) + altBump,
                                                                                    KMLWriter.bluePolyStyle, dets, null);
                                    }

                                    if (logIbacResults)
                                    {
                                        ibacWriter.writeLine (startTimeMillis, msg.getCSI() + msg.getCLI(), action.getParticleCountAverage(), action.getParticleCountStdev(), action.getParticleThreshold(), alarm?1:0);
                                    }
                                }
                            }
                        }
                        if (ibacCloud != null && !useRawIbacHits)
                        {
                            boolean alarm = false;

                            alarm = true;
                            double altBump = 0;
                            if (anacondaDetection)
                            {
                                altBump = 2;
                            }
                            writer.writePolygonBox(startTimeMillis, finalTimestamp, llaPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                        llaPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                                        llaPos.getAltitude().getDoubleValue(Length.METERS) + altBump,
                                                                        KMLWriter.bluePolyStyle, "hit", null);
                        }
                    }

                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            writer.endFolder();
            writer.writeEtdFolder();
            writeEtdDetections();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try
        {
            if (writer != null)
                writer.close();
            if (logIbacResults)
                ibacWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void writeEtdDetections() {
        for(int iEtd=0; iEtd<_etdDetections.size(); iEtd++) {
           EtdDetection etd = _etdDetections.get(iEtd);
           Long time = etd.getTime();
           Long endTime = time+500;
           Float concentration = etd.getConcentration();
           LatLonAltPosition etdPos = etd.getPosition().asLatLonAltPosition();

           Color etdColor = getColorForValue(concentration);
           String style = convertColorToABGR(etdColor);
           Boolean newStyle = styles.add(style);
           
           String name = Long.toString(time);
           String description = "Concentration: " + Float.toString(concentration) + "\nPosition: " + etdPos.toString();

           try {
                if(newStyle) {
                     writer.writePolyStyle(style, style);
                }

                writer.writePolygonSquare(time, endTime, etdPos.getLatitude().getDoubleValue(Angle.DEGREES),
                                                 etdPos.getLongitude().getDoubleValue(Angle.DEGREES),
                                                 etdPos.getAltitude().getDoubleValue(Length.METERS),
                                                 style, name, description);
           } catch(Exception e) {
               e.printStackTrace();
           }
        }
    }

    public static void main(String[] args)
    {
        boolean useRawIbac = false;
        boolean useRawAnaconda = false;

        //AP Hill
        //String filename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\8-12-2010 Ft AP Hill\\Run1\\wacsagent.47.metrics";

        //9-17-2010
        /*String metricsFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-17-2010\\Metrics\\wacsagent.128.metrics";
        String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-17-2010\\Metrics\\plumeData.txt";
        long startTimestamp = 1284757740077L;
        long endTimestamp = 1284759340609L;*/

        //9-18-2010
        /*String metricsFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-18-2010\\Metrics\\wacsagent.138.metrics";
        String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-18-2010\\Metrics\\plumeData.txt";
        long startTimestamp = 1284836117280L;
        long endTimestamp = 1284838717483L;*/

        //9-20-2010
        /**String metricsFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-20-2010\\Metrics\\wacsagent.155.metrics";
        String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-20-2010\\Metrics\\plumeData.txt";
        long startTimestamp = 1285007329984L;
        long endTimestamp = 1285009847421L;*/

        //9-23-2010
        /*String metricsFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-23-2010\\Metrics\\wacsagent.171.metrics";
        String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-23-2010\\Metrics\\plumeData.txt";
        long startTimestamp = 1285259039374L;
        long endTimestamp = 1285261869889L;*/

        //3-11-2011 Flight 1 Part 1
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\Desktop\\Redstone 3-11-11\\display.389.metrics";
        String plumeFilename = null;
        long startTimestamp = 1299853598390L;
        long endTimestamp = 1299862465390L;*/

        //3-11-2011 Flight 1 Part 2
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\Desktop\\Redstone 3-11-11\\display.390.metrics";
        String plumeFilename = null;
        long startTimestamp = 1299862487984L;
        long endTimestamp = 1299865751015L;*/

        //5-20-2011
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - May 2011\\5-20-2011 [Bt]\\Metrics\\wacsagent.152.metrics";
        //String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-23-2010\\Metrics\\plumeData.txt";
        String plumeFilename = null;
        long startTimestamp = 1305915064233L;
        long endTimestamp = 1305916803608L;
        useRawIbac = true;*/

        //5-21-2011
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - May 2011\\5-21-2011 [InO]\\Metrics\\wacsagent.156.metrics";
        //String plumeFilename = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\09-23-2010\\Metrics\\plumeData.txt";
        String plumeFilename = null;
        long startTimestamp = 1306001085093L;
        long endTimestamp = 1306005062530L;*/
        
        //5-23-2011
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - May 2011\\5-23-2011 [TEP]\\Metrics\\wacsagent.168.metrics";
        String plumeFilename = null;
        long startTimestamp = 1306177484859L;
        long endTimestamp = 1506005062530L;*/

        //5-25-2011
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - May 2011\\5-25-2011 [Bt]\\Metrics\\wacsagent.177.metrics";
        String plumeFilename = null;
        long startTimestamp = 1306347164843L;
        long endTimestamp = 1306349318499L;*/

        //5-25-2011
        /*String metricsFilename = "C:\\Documents and Settings\\humphjc1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - May 2011\\5-26-2011 [TEP]\\Metrics\\wacsagent.186.metrics";
        String plumeFilename = null;
        long startTimestamp = 1306439625561L;
        long endTimestamp = 1306441994061L;
        useRawAnaconda = true;*/

        //4-19-2012
        /*String metricsFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Distinct Raptor Logs/Explosion Day [04-19-2012]/metrics/wacsagent.382.metrics";
        String plumeFilename = null;
        long startTimestamp = 1334873991218L;
        long endTimestamp = 1334880466593L;*/
        
        //6-27-2012
        /*
        String metricsFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Distinct Raptor Logs/Explosion Day II [06-27-2012]/metrics/wacsagent.395.metrics";
        String plumeFilename = null;
        long startTimestamp = 1340809153471L;
        long endTimestamp =   1340816671000L;
*/
        
        //String metricsFilename = "C:\\Users\\biggimh1\\Desktop\\WACS\\WACS_Swarm\\metrics\\wacsagent.72.metrics";
        String metricsFilename = "C:\\Users\\kayjl1\\Desktop\\FruitFlyAberdeen\\metrics\\wacsagent.41.metrics";
        String plumeFilename = null;
        //long startTimestamp = 1533753843456L;
        long startTimestamp = 1533756480000L;
        long endTimestamp =   1533758574620L;
        KMLGenerator generator = new KMLGenerator (metricsFilename, plumeFilename, startTimestamp, endTimestamp, useRawAnaconda, useRawIbac);
        //KMLGenerator generator = new KMLGenerator (filename);
    }
}
