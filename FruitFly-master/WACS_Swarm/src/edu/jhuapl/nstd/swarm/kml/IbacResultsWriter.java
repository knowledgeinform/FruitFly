/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.kml;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author humphjc1
 */
public class IbacResultsWriter
{
    String outputFilename;
    String thresholdString;
    DataOutputStream output;

    public IbacResultsWriter (String filename, String threshString)
    {
        outputFilename = filename;
        thresholdString = threshString;

        try
        {
            File newFile = new File (outputFilename);
            if (!newFile.exists())
                newFile.createNewFile();

            output = new DataOutputStream(new BufferedOutputStream (new FileOutputStream (new File (outputFilename)), 1000));

            writeHeader ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void close() throws IOException
    {
        output.flush();
        output.close();
    }

    public void writeHeader () throws IOException
    {
        output.writeBytes ("Time,Total Counts,Average,Std Dev,Threshold,Alarm (" + thresholdString + ")\r\n");
    }

    public void writeLine (long time, int counts, double average, double stdDev, double threshold, int alarm) throws IOException
    {
        output.writeBytes (time + "," + counts + "," + average + "," + stdDev + "," + threshold + "," + alarm + "\r\n");
    }
}
