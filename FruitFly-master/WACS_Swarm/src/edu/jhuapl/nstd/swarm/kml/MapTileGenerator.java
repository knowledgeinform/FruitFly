/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.kml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class MapTileGenerator {


    public MapTileGenerator ()
    {
        String folderName = "C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Pod Logs\\Dugway Logs - Sept 2010\\MapTiles";
        //double altMeters = 4000;
        double altMeters = 14000;
        //double lonInc = 0.053625;
        double lonInc = 0.184722;
        //double latInc = 0.02474;
        double latInc = 0.037333;
        double latMinEdge = 39.93318611;
        double latMaxEdge = 40.29446389;
        double lonMinEdge = -113.36080028;
        double lonMaxEdge = -112.9018667;

        int maxI = (int)Math.ceil((lonMaxEdge - lonMinEdge)/lonInc);
        int maxJ = (int)Math.ceil((latMaxEdge - latMinEdge)/latInc);

        String outputFoldername = folderName;
        File outputFolder = new File (outputFoldername);
        if (!outputFolder.exists())
            outputFolder.mkdirs();

        String outputFilename = outputFoldername + "\\GoogleEarthTile_" + System.currentTimeMillis() + "_";



        for (int i = 0; i < maxI; i ++)
        {
            for (int j = 0; j < maxJ; j ++)
            {
                double baseLat = latMinEdge + latInc*(j+0.5);
                double baseLon = lonMinEdge + lonInc*(i+0.5);
                double baseAlt = 0;
                double baseRange = altMeters;
                double baseTilt = 0;
                double baseHeading = 0;

                KMLWriter writer = new KMLWriter (outputFilename + i + "_" + j + ".kml", null, "i:" + i + ",j:" + j,
                        baseLat, baseLon, baseAlt, baseRange, baseTilt, baseHeading);

                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(MapTileGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


    public static void main(String[] args)
    {
        MapTileGenerator gen = new MapTileGenerator ();
    }
}
