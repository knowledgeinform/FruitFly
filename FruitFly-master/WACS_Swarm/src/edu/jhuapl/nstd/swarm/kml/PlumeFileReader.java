/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author humphjc1
 */
public class PlumeFileReader
{
    public class PlumeData
    {
        public double m_LatDeg;
        public double m_LonDeg;
        public double m_CenterAltM;
        public double m_xWidthM;
        public double m_yDepthM;
        public double m_zHeightM;
        public double m_BearingDeg;

        public PlumeData ()
        {
            m_LatDeg = 0;
            m_LonDeg = 0;
            m_CenterAltM = 0;
            m_xWidthM = 0;
            m_yDepthM = 0;
            m_zHeightM = 0;
            m_BearingDeg = 0;
        }

        public PlumeData (double latDeg, double lonDeg, double altM, double xWidthM, double yDepthM, double zHeightM, double bearingDeg)
        {
            m_LatDeg = latDeg;
            m_LonDeg = lonDeg;
            m_CenterAltM = altM;
            m_xWidthM = xWidthM;
            m_yDepthM = yDepthM;
            m_zHeightM = zHeightM;
            m_BearingDeg = bearingDeg;
        }
    }


    String m_InputFilename;
    BufferedReader reader;
    ArrayList <PlumeData> m_PlumeData;

    long lastTimestamp = -1;
    long firstTimestamp = -1;
    PlumeData lastData = null;

    public PlumeFileReader (String inputFilename)
    {
        m_PlumeData = new ArrayList<PlumeData> ();

        try
        {
            m_InputFilename = inputFilename;
            reader = new BufferedReader(new FileReader(new File(m_InputFilename)));
            readData ();
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }

    private void readData ()
    {
        String line = null;
        String tokens[] = null;

        try
        {
            //Skip header line
            reader.readLine();

            while ((line = reader.readLine()) != null)
            {
                tokens = line.split("\t");

                long newTimestamp = Long.parseLong(tokens[1]);
                PlumeData newData = new PlumeData();
                newData.m_LatDeg = Double.parseDouble(tokens[4]);
                newData.m_LonDeg = Double.parseDouble(tokens[5]);
                newData.m_CenterAltM = Double.parseDouble(tokens[7]);
                newData.m_xWidthM = Math.max (1, Double.parseDouble(tokens[8]));
                newData.m_yDepthM = Math.max (1, Double.parseDouble(tokens[9]));
                newData.m_zHeightM = Math.max (1, Double.parseDouble(tokens[10]));
                newData.m_BearingDeg = Double.parseDouble(tokens[11]);

                if (lastTimestamp < 0 || lastData == null)
                {
                    //First one, just use it
                    firstTimestamp = newTimestamp;
                    m_PlumeData.add(newData);

                    lastData = newData;
                    lastTimestamp = newTimestamp;
                }
                else
                {
                    //Interpolate timestamps from last to this one so we can estimate data in between
                    long timeSpan = newTimestamp - lastTimestamp;
                    for (long i = 1; i < timeSpan; i ++)
                    {
                        PlumeData interpData = new PlumeData ();
                        interpData.m_LatDeg = (newData.m_LatDeg-lastData.m_LatDeg)*(i/((double)timeSpan)) + lastData.m_LatDeg;
                        interpData.m_LonDeg = (newData.m_LonDeg-lastData.m_LonDeg)*(i/((double)timeSpan)) + lastData.m_LonDeg;
                        interpData.m_CenterAltM = (newData.m_CenterAltM-lastData.m_CenterAltM)*(i/((double)timeSpan)) + lastData.m_CenterAltM;
                        interpData.m_xWidthM = (newData.m_xWidthM-lastData.m_xWidthM)*(i/((double)timeSpan)) + lastData.m_xWidthM;
                        interpData.m_yDepthM = (newData.m_yDepthM-lastData.m_yDepthM)*(i/((double)timeSpan)) + lastData.m_yDepthM;
                        interpData.m_zHeightM = (newData.m_zHeightM-lastData.m_zHeightM)*(i/((double)timeSpan)) + lastData.m_zHeightM;
                        interpData.m_BearingDeg = (newData.m_BearingDeg-lastData.m_BearingDeg)*(i/((double)timeSpan)) + lastData.m_BearingDeg;

                        m_PlumeData.add(interpData);
                    }

                    //Add this current data and prepare for next one
                    m_PlumeData.add(newData);
                    lastData = newData;
                    lastTimestamp = newTimestamp;
                }

            }

            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    PlumeData getDataAtTime (long timestamp)
    {
        //Array has data every one second from firstTimetsamp.  Just pick index and pull it out
        int idx = (int)(timestamp - firstTimestamp);
        if (idx >= 0 && idx < m_PlumeData.size())
            return m_PlumeData.get(idx);
        else
            return null;
    }
    

}
