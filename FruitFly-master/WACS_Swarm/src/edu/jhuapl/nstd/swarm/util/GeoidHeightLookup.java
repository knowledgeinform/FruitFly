/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Class to read geoid height data from file and provide access to it
 * based on lat/lon
 *
 * @author humphjc1
 */
public class GeoidHeightLookup
{
    /**
     * Single instantiation of this class, accessed through static methods.
     */
    private static GeoidHeightLookup _heights = null;

    /**
     * Data matrix with height data;
     */
    private float m_EllipsoidMap[][] = null;

    /**
     * Filename data matrix was read from
     */
    private String m_Filename = null;

    private static int m_HardCodedMapXCount = 1441;
    private static int m_HardCodedMapYCount = 721;

    /**
     * Constructor, called the first time getInstance is called to generate the heap
     */
    private GeoidHeightLookup ()
    {

    }

    /**
     * Read geo height data from file
     *
     * @param filename
     * @return True if geoid height data is loaded, false otherwise
     */
    public boolean readGeoidHeight (String filename)
    {
        if (filename.equals(m_Filename))
        {
            System.out.println ("Geoid data already read from: " + m_Filename);
            return true;
        }

        File readFile = new File (filename);
        if (!readFile.exists())
        {
            System.out.println ("Geoid data file not found at: " + m_Filename);
            return false;
        }

        try
        {
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(readFile)));
            m_EllipsoidMap = new float [m_HardCodedMapXCount][m_HardCodedMapYCount];
            m_Filename = filename;

            for (int j = m_HardCodedMapYCount-1; j >= 0; j--)
            {
                for (int i = 0; i < m_HardCodedMapXCount; i ++)
                {
                    float val = reader.readFloat();
                    int valInteger = Float.floatToIntBits(val);
                    int valIntRev = Integer.reverseBytes(valInteger);
                    float valRev = Float.intBitsToFloat(valIntRev);

                    m_EllipsoidMap[i][j] = valRev;
                }
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return the geoid height at the specified lat/lon position.  Will be interpolated
     * between corners if necessary
     * 
     * @param latDecDeg
     * @param lonDecDeg
     * @return
     */
    public float getGeoidHeight (double latDecDeg, double lonDecDeg)
    {
        double swLat, swLon, topLat, leftLon, btmLat, rightLon;
        int col1, col2, row1, row2;
        float upLeft, upRight, btmLeft, btmRight;
        float left, right;
        float geoidHeight;
        double perRightOfLeft, perDownOfTop;

        if (lonDecDeg < 0)
            lonDecDeg += 360.0;
        if (latDecDeg<-90.0 || latDecDeg>90.0 || lonDecDeg<0 || lonDecDeg>360)
        {
            System.out.println("ERROR: lat/lon (" + latDecDeg + "," + lonDecDeg + ") out of bounds\n");
            return 0.0f;
        }

        swLat = ((int)(latDecDeg*4.0) / 4.0);       /* range -90 to 90 */
        swLon = ((int)(lonDecDeg*4.0) / 4.0);     /* range 0 to 360 */

        col1 = (int)((double)(swLon*4.0));
        col2 = col1+1;
        row1 = (int)((double)((swLat+90.0)*4.0));
        row2 = row1+1;
        if (row2>720)
        {
            row1--;
            row2--;
        }
        if (col2>1440)
        {
            col1--;
            col2--;
        }

        topLat = ((row1/4.0) - 90.0);
        btmLat = ((row2/4.0) - 90.0);
        leftLon = (col1/4.0);
        rightLon = (col2/4.0);

        perRightOfLeft = (lonDecDeg-leftLon) / (rightLon-leftLon);
        perDownOfTop = (latDecDeg-topLat) / (btmLat-topLat);

        upLeft = m_EllipsoidMap[col1][row1];
        upRight = m_EllipsoidMap[col2][row1];
        btmLeft = m_EllipsoidMap[col1][row2];
        btmRight = m_EllipsoidMap[col2][row2];


        left = (float)((upLeft*(1.0-perDownOfTop)) + (btmLeft*perDownOfTop));
        right = (float)((upRight*(1.0-perDownOfTop)) + (btmRight*perDownOfTop));
        geoidHeight = (float)((left*(1.0-perRightOfLeft)) + (right*perRightOfLeft));

        return geoidHeight;
    }


    /**
     * Accessor for the singleton instance of this object
     *
     * @return The object
     */
    public static GeoidHeightLookup getInstance()
    {
        if (_heights == null)
        {
            _heights = new GeoidHeightLookup();
        }
        return _heights;
    }



    public static void main (String args[])
    {
        GeoidHeightLookup lookup = GeoidHeightLookup.getInstance();
        lookup.readGeoidHeight("geoidHeight15min.img");

        System.out.println ("lon");
        for (int i = 0; i < 360; i ++)
        {
            //System.out.println ("lon = " + i + ", geoid height = " + lookup.getGeoidHeight(0, i));
            System.out.println (lookup.getGeoidHeight(0, i));
        }

        System.out.println ("lat");
        for (int i = -90; i < 90; i ++)
        {
            //System.out.println ("lat = " + i + ", geoid height = " + lookup.getGeoidHeight(i, 0));
            System.out.println (lookup.getGeoidHeight(i, 0));
        }
    }
}
