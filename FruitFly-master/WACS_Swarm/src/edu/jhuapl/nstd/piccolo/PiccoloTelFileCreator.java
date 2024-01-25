/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.piccolo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 *
 * @author humphjc1
 */
public class PiccoloTelFileCreator
{

    public static void main (String args[])
    {
        //String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-20-2011 [Bt]/PiccoloLogs/fullLogs.log";
        //String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-21-2011 [InO]/PiccoloLogs/PiccoloLog_1305996219718.log";
        //String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-23-2011 [TEP]/Piccolo Logs/PiccoloLog_1306172542093.log";
        //String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-25-2011 [Bt]/PiccoloLogs/PiccoloLog_1306336864218.log";
        //String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Dugway Logs - May 2011/5-26-2011 [TEP]/PiccoloLogs/PiccoloLog_1306429701561.log";
        String inputFilename = "C:/Documents and Settings/humphjc1/My Documents/wacs/Pod Logs/Distinct Raptor Logs/Explosion Day [04-19-2012]/Piccolo Logs/PiccoloLog_1334868059109.log";
        String outputFilename = inputFilename.substring(0, inputFilename.length()-3) + "tel";

        try
        {
            BufferedReader reader = new BufferedReader (new FileReader(inputFilename));
            File outFile = new File (outputFilename);
            if (!outFile.exists())
                outFile.createNewFile();
            BufferedOutputStream writer = new BufferedOutputStream( new DataOutputStream(new FileOutputStream(outFile)));

            String readLine = null;
            
            while ((readLine = reader.readLine()) != null)
            {
                String[] splitLine = readLine.split ("\t");
                
                for (int i = 1; i < splitLine.length; i ++)
                {
                    writer.write(Integer.parseInt (splitLine[i]));
                }
            }
            writer.flush();
            writer.close();
            reader.close();



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
