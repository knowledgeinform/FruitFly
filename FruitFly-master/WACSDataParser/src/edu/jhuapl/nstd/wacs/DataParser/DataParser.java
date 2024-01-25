/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.wacs.DataParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class DataParser
{
    static String lastBladewerxResultsLine;
    static long lastBladewerxResultsTime;
    static String lastBladewerxRawLine;
    static long lastBladewerxRawTime;
    static String lastBridgeportResultsLine;
    static long lastBridgeportResultsTime;
    static String lastBridgeportRawLine;
    static long lastBridgeportRawTime;



    /**
     * If Non null, filenames are valid.  Output folder is created.
     * 
     * @param metricsFilename
     * @param outputFolder
     * @throws HumpsException
     */
    static public void process (String metricsFilename, String bladewerxRawFilename, String bladewerxResultsFilename, String bridgeportRawFilename, String bridgeportResultsFilename, String trackerFilename, String collectorFilename, String piccoloFilename, String shadowFilename, String outputFolder) throws HumpsException
    {
        if (metricsFilename != null)
            processMetrics(metricsFilename, bladewerxRawFilename, bladewerxResultsFilename, bridgeportRawFilename, bridgeportResultsFilename, outputFolder);

        if (collectorFilename != null || trackerFilename != null)
            processXD(trackerFilename, collectorFilename, outputFolder);

        if (piccoloFilename != null)
            processPiccolo (piccoloFilename, outputFolder);

        if (shadowFilename != null)
            processShadow (shadowFilename, outputFolder);
    }


    static public void processMetrics(String metricsFilename, String bladewerxRawFilename, String bladewerxResultsFilename, String bridgeportRawFilename, String bridgeportResultsFilename, String outputFolder) throws HumpsException
    {
        //Open summary output file
        String outputFilename = outputFolder + "\\" + metricsFilename.substring(metricsFilename.lastIndexOf("\\"), metricsFilename.length()) + ".out";
        BufferedWriter outputWriter;
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFilename));
        } catch (IOException ex) {
            throw new HumpsException ("Unable to open output file!");
        }
        int writeIndex = 0;

        //Need to have metrics file to mesh with
        if (metricsFilename != null)
        {
            String bladewerxResultsCopyFilename = null;
            String bladewerxRawCopyFilename = null;
            String bridgeportResultsCopyFilename = null;
            String bridgeportRawCopyFilename = null;

            //Open metrics file to read
            BufferedReader metricsReader;
            try {
                metricsReader = new BufferedReader(new FileReader(metricsFilename));
            } catch (FileNotFoundException ex) {
                throw new HumpsException ("Metrics file not found!");
            }

            //Open bladewerx file to read
            BufferedReader bladewerxResultsReader = null;
            if (bladewerxResultsFilename != null)
            {
                try {
                    bladewerxResultsReader = new BufferedReader(new FileReader(bladewerxResultsFilename));
                    bladewerxResultsCopyFilename = outputFolder + "\\" + bladewerxResultsFilename.substring (bladewerxResultsFilename.lastIndexOf("\\"), bladewerxResultsFilename.length());
                } catch (FileNotFoundException ex) {
                    throw new HumpsException ("Bladewerx results file not found!");
                }
            }

            //Open bladewerx file to read
            BufferedReader bladewerxRawReader = null;
            if (bladewerxRawFilename != null)
            {
                try {
                    bladewerxRawReader = new BufferedReader(new FileReader(bladewerxRawFilename));
                    bladewerxRawCopyFilename = outputFolder + "\\" + bladewerxRawFilename.substring (bladewerxRawFilename.lastIndexOf("\\"), bladewerxRawFilename.length());
                } catch (FileNotFoundException ex) {
                    throw new HumpsException ("Bladewerx raw file not found!");
                }
            }

            //Open bridgeport file to read
            BufferedReader bridgeportResultsReader = null;
            if (bridgeportResultsFilename != null)
            {
                try {
                    bridgeportResultsReader = new BufferedReader(new FileReader(bridgeportResultsFilename));
                    bridgeportResultsCopyFilename = outputFolder + "\\" + bridgeportResultsFilename.substring (bridgeportResultsFilename.lastIndexOf("\\"), bridgeportResultsFilename.length());
                } catch (FileNotFoundException ex) {
                    throw new HumpsException ("Bridgeport results file not found!");
                }
            }

            //Open bridgeport file to read
            BufferedReader bridgeportRawReader = null;
            if (bridgeportRawFilename != null)
            {
                try {
                    bridgeportRawReader = new BufferedReader(new FileReader(bridgeportRawFilename));
                    bridgeportRawCopyFilename = outputFolder + "\\" + bridgeportRawFilename.substring (bridgeportRawFilename.lastIndexOf("\\"), bridgeportRawFilename.length());
                } catch (FileNotFoundException ex) {
                    throw new HumpsException ("Bridgeport raw file not found!");
                }
            }



            //Open bladewerx file to copy to
            BufferedWriter bladewerxResultsCopy = null;
            if (bladewerxResultsCopyFilename != null)
            {
                try {
                    bladewerxResultsCopy = new BufferedWriter(new FileWriter(bladewerxResultsCopyFilename));
                } catch (IOException ex) {
                    throw new HumpsException ("Bladewerx copy results file not created!");
                }
            }

            //Open bladewerx file to copy to
            BufferedWriter bladewerxRawCopy = null;
            if (bladewerxRawCopyFilename != null)
            {
                try {
                    bladewerxRawCopy = new BufferedWriter(new FileWriter(bladewerxRawCopyFilename));
                } catch (IOException ex) {
                    throw new HumpsException ("Bladewerx copy results file not created!");
                }
            }

            //Open bridgeport file to copy to
            BufferedWriter bridgeportResultsCopy = null;
            if (bridgeportResultsCopyFilename != null)
            {
                try {
                    bridgeportResultsCopy = new BufferedWriter(new FileWriter(bridgeportResultsCopyFilename));
                } catch (IOException ex) {
                    throw new HumpsException ("Bridgeport copy results file not created!");
                }
            }

            //Open bridgeport file to copy to
            BufferedWriter bridgeportRawCopy = null;
            if (bridgeportRawCopyFilename != null)
            {
                try {
                    bridgeportRawCopy = new BufferedWriter(new FileWriter(bridgeportRawCopyFilename));
                } catch (IOException ex) {
                    throw new HumpsException ("Bridgeport copy results file not created!");
                }
            }


            //Create data object
            DataElement data = new DataElement ("-1");
            String line = "";

            try
            {
                //Loop through metrics file and collect all beliefs for single time step into data object
                while((line = metricsReader.readLine())!= null)
                {
                    if(line.contains("time:")){
                        data = new DataElement(line.split(" ")[1]);
                    }else if(line.equals("belief: agentPositionBelief")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.agentPosition = data.agentPosition + " " + line + " ";
                        }
                    }else if(line.equals("belief: targetBelief")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.target = data.target + " " + line + " ";
                        }
                    }else if(line.equals("metbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.met = data.met + " " + line + " ";
                        }
                    }else if(line.equals("plumedetectionbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.plume = data.plume + " " + line + " ";
                        }
                    }else if(line.equals("clouddetectionbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.cloud = data.cloud + " " +  line + " ";
                        }
                    }else if(line.equals("anacondadetectionbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.chem = data.chem + " " + line + " ";
                        }
                    }else if(line.equals("biopoddetectionbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.bio  = data.bio + " " + line + " ";
                        }
                    }else if(line.equals("pic_telem:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.picTelem  = data.picTelem + " " + line + " ";
                        }
                    }else if(line.equals("circularorbitbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.orbit  = data.orbit + " " + line + " ";
                        }
                    }
                    else if(line.equals("racetrackorbitbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.racetrack  = data.racetrack + " " + line + " ";
                        }
                    }
                    else if(line.equals("explosiontimebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.explosionTimeRaw  = data.explosionTimeRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("explosionbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.explosionPosRaw  = data.explosionPosRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("agentmodebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.wacsagentModeRaw  = data.wacsagentModeRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("racetrackorbitbelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.racetrackOrbitRaw  = data.racetrackOrbitRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("ibacactualstatebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.ibacStateRaw  = data.ibacStateRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("alphasensoractualstatebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.alphaStateRaw  = data.alphaStateRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("particlecollectoractualstatebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.c100StateRaw  = data.c100StateRaw + " " + line + " ";
                        }
                    }
                    else if(line.equals("anacondaactualstatebelief:")){
                        while(!(line = metricsReader.readLine()).contains("end")){
                            data.anacondaStateRaw  = data.anacondaStateRaw + " " + line + " ";
                        }
                    }
                    
                    else if(line.equals("end time"))
                    {
                        //This is the end of current timestep reading in metrics file.  Here we would check other files' data

                        try
                        {
                            //If first time, open RN files
                            if (writeIndex == 0)
                            {
                                try
                                {
                                    if (bladewerxResultsReader != null)
                                        initializeBladewerxResults (Long.parseLong(data.startTime), bladewerxResultsReader, bladewerxResultsCopy, true);
                                }
                                catch (Exception e)
                                {
                                    throw new HumpsException ("Error initializing Bladewerx Results file!");
                                }

                                try
                                {
                                    if (bladewerxRawReader != null)
                                        initializeBladewerxRaw (Long.parseLong(data.startTime), bladewerxRawReader, bladewerxRawCopy, true);
                                }
                                catch (Exception e)
                                {
                                    throw new HumpsException ("Error initializing Bladewerx Raw file!");
                                }

                                try
                                {
                                    if (bridgeportResultsReader != null)
                                        initializeBridgeportResults (Long.parseLong(data.startTime), bridgeportResultsReader, bridgeportResultsCopy, true);
                                }
                                catch (Exception e)
                                {
                                    throw new HumpsException ("Error initializing Bridgeport Results file!");
                                }

                                try
                                {
                                    if (bridgeportRawReader != null)
                                        initializeBridgeportRaw (Long.parseLong(data.startTime), bridgeportRawReader, bridgeportRawCopy, true);
                                }
                                catch (Exception e)
                                {
                                    throw new HumpsException ("Error initializing Bridgeport Raw file!");
                                }
                            }


                            //Read Bladewerx results file for data matching current timestamp
                            try
                            {
                                if (bladewerxResultsReader != null)
                                {
                                    if (readBladewerxResults (data, bladewerxResultsReader, bladewerxResultsCopy))
                                    {
                                        //Try to open next bladewerx file in sequence
                                        try
                                        {
                                            String lastIndexS = bladewerxResultsFilename.substring(bladewerxResultsFilename.lastIndexOf("_")+1, bladewerxResultsFilename.length()-4);
                                            int lastIndex = Integer.parseInt(lastIndexS);
                                            int newIndex = lastIndex + 1;

                                            String newIndexS = String.format ("%0" + lastIndexS.length() + "d", newIndex);
                                            String newfilename = bladewerxResultsFilename.substring(0, bladewerxResultsFilename.lastIndexOf("_") + 1) + newIndexS + ".dat";
                                            bladewerxResultsFilename = newfilename;

                                            try {
                                                bladewerxResultsReader = new BufferedReader(new FileReader(bladewerxResultsFilename));
                                                initializeBladewerxResults (Long.parseLong(data.startTime), bladewerxResultsReader, bladewerxResultsCopy, false);
                                            } catch (FileNotFoundException ex) {
                                                //We must be done reading bladewerx becuase we can't find anymore files.
                                                bladewerxResultsReader = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new HumpsException ("Error finding next Bladewerx Results file");
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                throw new HumpsException ("Error reading Bladewerx Results file!");
                            }

                            //Read Bladewerx raw file for data matching current timestamp
                            try
                            {
                                if (bladewerxRawReader != null)
                                {
                                    if (readBladewerxRaw (data, bladewerxRawReader, bladewerxRawCopy))
                                    {
                                        //Try to open next bladewerx file in sequence
                                        try
                                        {
                                            String lastIndexS = bladewerxRawFilename.substring(bladewerxRawFilename.lastIndexOf("_")+1, bladewerxRawFilename.length()-4);
                                            int lastIndex = Integer.parseInt(lastIndexS);
                                            int newIndex = lastIndex + 1;

                                            String newIndexS = String.format ("%0" + lastIndexS.length() + "d", newIndex);
                                            String newfilename = bladewerxRawFilename.substring(0, bladewerxRawFilename.lastIndexOf("_") + 1) + newIndexS + ".dat";
                                            bladewerxRawFilename = newfilename;

                                            try {
                                                bladewerxRawReader = new BufferedReader(new FileReader(bladewerxRawFilename));
                                                initializeBladewerxRaw (Long.parseLong(data.startTime), bladewerxRawReader, bladewerxRawCopy, false);
                                            } catch (FileNotFoundException ex) {
                                                //We must be done reading bladewerx becuase we can't find anymore files.
                                                bladewerxRawReader = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new HumpsException ("Error finding next Bladewerx Raw file");
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                throw new HumpsException ("Error reading Bladewerx Raw file!");
                            }

                            //Read Bridgeport results file for data matching current timestamp
                            try
                            {
                                if (bridgeportResultsReader != null)
                                {
                                    if (readBridgeportResults (data, bridgeportResultsReader, bridgeportResultsCopy))
                                    {
                                        //Try to open next bridgeport file in sequence
                                        try
                                        {
                                            String lastIndexS = bridgeportResultsFilename.substring(bridgeportResultsFilename.lastIndexOf("_")+1, bridgeportResultsFilename.length()-4);
                                            int lastIndex = Integer.parseInt(lastIndexS);
                                            int newIndex = lastIndex + 1;

                                            String newIndexS = String.format ("%0" + lastIndexS.length() + "d", newIndex);
                                            String newfilename = bridgeportResultsFilename.substring(0, bridgeportResultsFilename.lastIndexOf("_") + 1) + newIndexS + ".dat";
                                            bridgeportResultsFilename = newfilename;

                                            try {
                                                bridgeportResultsReader = new BufferedReader(new FileReader(bridgeportResultsFilename));
                                                initializeBridgeportResults (Long.parseLong(data.startTime), bridgeportResultsReader, bridgeportResultsCopy, false);
                                            } catch (FileNotFoundException ex) {
                                                //We must be done reading bridgeport becuase we can't find anymore files.
                                                bridgeportResultsReader = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new HumpsException ("Error finding next Bridgeport Results file");
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                throw new HumpsException ("Error reading Bridgeport Results file!");
                            }

                            //Read Bridgeport raw file for data matching current timestamp
                            try
                            {
                                if (bridgeportRawReader != null)
                                {
                                    if (readBridgeportRaw (data, bridgeportRawReader, bridgeportRawCopy))
                                    {
                                        //Try to open next bridgeport file in sequence
                                        try
                                        {
                                            String lastIndexS = bridgeportRawFilename.substring(bridgeportRawFilename.lastIndexOf("_")+1, bridgeportRawFilename.length()-4);
                                            int lastIndex = Integer.parseInt(lastIndexS);
                                            int newIndex = lastIndex + 1;

                                            String newIndexS = String.format ("%0" + lastIndexS.length() + "d", newIndex);
                                            String newfilename = bridgeportRawFilename.substring(0, bridgeportRawFilename.lastIndexOf("_") + 1) + newIndexS + ".flt";
                                            bridgeportRawFilename = newfilename;

                                            try {
                                                bridgeportRawReader = new BufferedReader(new FileReader(bridgeportRawFilename));
                                                initializeBridgeportRaw (Long.parseLong(data.startTime), bridgeportRawReader, bridgeportRawCopy, false);
                                            } catch (FileNotFoundException ex) {
                                                //We must be done reading bridgeport becuase we can't find anymore files.
                                                bridgeportRawReader = null;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new HumpsException ("Error finding next Bridgeport Raw file");
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                throw new HumpsException ("Error reading Bridgeport Raw file!");
                            }


                            data.writeToFile (outputWriter, writeIndex++);

                            //copy necessary files over too?
                        }
                        catch (IOException e)
                        {
                            throw new HumpsException ("Error writing output file!");
                        }
                    }
                }

                try
                {
                    metricsReader.close();
                    outputWriter.close();
                }
                catch (Exception e)
                {
                    throw new HumpsException ("Error closing metrics file!");
                }
            }
            catch (IOException e)
            {
                throw new HumpsException ("Error reading metrics file!");
            }

            try
            {
                if (bladewerxResultsCopy != null) bladewerxResultsCopy.close();;
                if (bladewerxRawCopy != null) bladewerxRawCopy.close();;
                if (bridgeportResultsCopy != null) bridgeportResultsCopy.close();;
                if (bridgeportRawCopy != null) bridgeportRawCopy.close();;
            }
            catch (Exception e)
            {
                throw new HumpsException ("Error closing files");
            }
        }
        
        try
        {
            if (outputWriter != null) outputWriter.close();
        }
        catch (Exception e)
        {
            throw new HumpsException ("Error closing files");
        }

    }

    static void initializeBladewerxResults (long dataTime, BufferedReader bladewerxReader, BufferedWriter copyWriter, boolean copyHeader) throws Exception
    {
        //Header line
        readLineAndCopy (bladewerxReader, copyHeader?copyWriter:null);

        //Scroll until Bladewerx time is after data time
        long bladewerxTime = -1;
        while (bladewerxTime < dataTime && bladewerxReader.ready())
        {
            String data = readLineAndCopy (bladewerxReader, copyWriter);
            String [] splitData = data.split("\t");

            bladewerxTime = Long.parseLong(splitData[1]);

            lastBladewerxResultsLine = data;
            lastBladewerxResultsTime = bladewerxTime;
        }
    }

    //Return true if we should try to find next file in sequence
    static boolean readBladewerxResults (DataElement data, BufferedReader bladewerxReader, BufferedWriter copyWriter) throws Exception
    {
        long dataTime = Long.parseLong(data.startTime);
        if (dataTime > lastBladewerxResultsTime && bladewerxReader.ready())
        {
            data.bladewerxResults = lastBladewerxResultsLine;
            data.bladewerxResults += "\t" + readLineAndCopy (bladewerxReader, copyWriter);
            data.bladewerxResults += "\t" + readLineAndCopy (bladewerxReader, copyWriter);
            data.bladewerxResults += "\t" + readLineAndCopy (bladewerxReader, copyWriter);
            data.bladewerxResults += "\t" + readLineAndCopy (bladewerxReader, copyWriter);

            if (bladewerxReader.ready())
            {
                lastBladewerxResultsLine = readLineAndCopy (bladewerxReader, copyWriter);
                String[] lineSplit = lastBladewerxResultsLine.split(("\t"));
                lastBladewerxResultsTime = Long.parseLong(lineSplit[1]);
            }
            else
            {
                //Try to open the next file in the sequence
                return true;
            }
        }
        return false;
    }

    static void initializeBladewerxRaw (long dataTime, BufferedReader bladewerxReader, BufferedWriter copyWriter, boolean copyHeader) throws Exception
    {
        //Header line
        readLineAndCopy (bladewerxReader, copyHeader?copyWriter:null);

        //Scroll until Bladewerx time is after data time
        long bladewerxTime = -1;
        while (bladewerxTime < dataTime && bladewerxReader.ready())
        {
            String data = readLineAndCopy (bladewerxReader, copyWriter);
            String [] splitData = data.split("\t");

            bladewerxTime = Long.parseLong(splitData[1]);

            lastBladewerxRawLine = data;
            lastBladewerxRawTime = bladewerxTime;
        }
    }

    //Return true if we should try to find next file in sequence
    static boolean readBladewerxRaw (DataElement data, BufferedReader bladewerxReader, BufferedWriter copyWriter) throws Exception
    {
        long dataTime = Long.parseLong(data.startTime);
        if (dataTime > lastBladewerxRawTime)
        {
            data.bladewerxRaw = lastBladewerxRawLine;

            if (bladewerxReader.ready())
            {
                lastBladewerxRawLine = readLineAndCopy (bladewerxReader, copyWriter);
                String[] lineSplit = lastBladewerxRawLine.split(("\t"));
                lastBladewerxRawTime = Long.parseLong(lineSplit[1]);
            }
            else
            {
                //Try to open the next file in the sequence
                return true;
            }
        }
        return false;
    }

    static void initializeBridgeportRaw (long dataTime, BufferedReader bridgeportReader, BufferedWriter copyWriter, boolean copyHeader) throws Exception
    {
        //Header line
        readLineAndCopy (bridgeportReader, copyHeader?copyWriter:null);

        //Scroll until Bridgeport time is after data time
        long bridgeportTime = -1;
        while (bridgeportTime < dataTime && bridgeportReader.ready())
        {
            String data = readLineAndCopy (bridgeportReader, copyWriter);
            String [] splitData = data.split("\t");

            bridgeportTime = Long.parseLong(splitData[0]);

            lastBridgeportRawLine = data;
            lastBridgeportRawTime = bridgeportTime;
        }
    }

    //Return true if we should try to find next file in sequence
    static boolean readBridgeportRaw (DataElement data, BufferedReader bridgeportReader, BufferedWriter copyWriter) throws Exception
    {
        long dataTime = Long.parseLong(data.startTime);
        if (dataTime > lastBridgeportRawTime)
        {
            data.bridgeportRaw = lastBridgeportRawLine;

            if (bridgeportReader.ready())
            {
                lastBridgeportRawLine = readLineAndCopy (bridgeportReader, copyWriter);
                String[] lineSplit = lastBridgeportRawLine.split(("\t"));
                lastBridgeportRawTime = Long.parseLong(lineSplit[0]);
            }
            else
            {
                //Try to open the next file in the sequence
                return true;
            }
        }
        return false;
    }

    static void initializeBridgeportResults (long dataTime, BufferedReader bridgeportReader, BufferedWriter copyWriter, boolean copyHeader) throws Exception
    {
        //Header line
        readLineAndCopy (bridgeportReader, copyHeader?copyWriter:null);

        //Scroll until Bridgeport time is after data time
        long bridgeportTime = -1;
        while (bridgeportTime < dataTime && bridgeportReader.ready())
        {
            String data = readLineAndCopy (bridgeportReader, copyWriter);
            String [] splitData = data.split("\t");

            bridgeportTime = Long.parseLong(splitData[0]);

            lastBridgeportResultsLine = data;
            lastBridgeportResultsTime = bridgeportTime;
        }
    }

    //Return true if we should try to find next file in sequence
    static boolean readBridgeportResults (DataElement data, BufferedReader bridgeportReader, BufferedWriter copyWriter) throws Exception
    {
        long dataTime = Long.parseLong(data.startTime);
        if (dataTime > lastBridgeportResultsTime)
        {
            data.bridgeportResults = lastBridgeportResultsLine;

            if (bridgeportReader.ready())
            {
                lastBridgeportResultsLine = readLineAndCopy (bridgeportReader, copyWriter);
                String[] lineSplit = lastBridgeportResultsLine.split(("\t"));
                lastBridgeportResultsTime = Long.parseLong(lineSplit[1]);
            }
            else
            {
                //Try to open the next file in the sequence
                return true;
            }
        }
        return false;
    }

    static String readLineAndCopy (BufferedReader reader, BufferedWriter writer) throws Exception
    {
        String nextLine = reader.readLine();
        if (writer != null)
            writer.write (nextLine + "\r\n");
        return nextLine;
    }




    static public void processXD (String trackerFilename, String collectorFilename, String outputFolder) throws HumpsException
    {
        //Open summary output file
        String outputFilename = outputFolder + "\\xD_Logs_";
        BufferedWriter ibacWriterDiag;
        BufferedWriter ibacWriterCount;
        BufferedWriter tempWriterT;
        BufferedWriter tempWriterC;
        BufferedWriter c100Writer;
        BufferedWriter anacondaWriterStatus;
        BufferedWriter anacondaWriterLcda;
        BufferedWriter anacondaWriterLcdb;
        
        float temp = -100, humidity = -100;
        int fan = -100, servo = -100, heat = -100;

        if (trackerFilename != null)
        {
            try
            {
                ibacWriterDiag = new BufferedWriter(new FileWriter(outputFilename + "IBAC_Diag.log"));
                ibacWriterCount = new BufferedWriter(new FileWriter(outputFilename + "IBAC_Count.log"));
                tempWriterT = new BufferedWriter(new FileWriter(outputFilename + "Tracker_Temp.log"));
            } catch (IOException ex) {
                throw new HumpsException ("Unable to open output file!");
            }

            //Open file to read
            BufferedReader fileReader;
            try {
                fileReader = new BufferedReader(new FileReader(trackerFilename));
            } catch (FileNotFoundException ex) {
                throw new HumpsException ("Tracker file not found!");
            }


            String line = "";
            try
            {
                //Header stuff
                fileReader.readLine();
                fileReader.readLine();
                fileReader.readLine();
                fileReader.readLine();

                ibacWriterCount.write ("Timestamp,CSI,CLI,BCSI,BCLI,CSA,CLA,BCSA,BCLA,B%SA,B%LA,SFI,SFA,Alarm Counter,Valid Baseline,Alarm Status,Alarm Latch");
                ibacWriterCount.newLine();
                ibacWriterDiag.write ("Timestamp,Outlet Press (psi),P Alarm,Temp (C), T Alarm,Laser Power Monitor, Laser Power Alarm,Laser Current Monitor, Laser Current Alarm, Background Monitor, Background Alarm, Input Voltage, Input V Alarm, Input Current, Input Curr Alarm");
                ibacWriterDiag.newLine();
                tempWriterT.write ("Timestamp,Temperature (C),Humidity,Fan State,Servo State,Heat State");
                tempWriterT.newLine();

                //Loop through file and parse each line
                while((line = fileReader.readLine())!= null)
                {
                    if (line.contains(":Serial port E"))
                    {
                        long timestamp = getRTC (line);
                        if (line.contains ("$trace"))
                        {
                            ibacWriterCount.write(timestamp + ",");
                            String textLine = line.substring (line.lastIndexOf("$trace,")+7, line.length());
                            ibacWriterCount.write (textLine);
                            ibacWriterCount.newLine();
                        }
                        else if(line.contains("$diagnostics"))
                        {
                            ibacWriterDiag.write(timestamp + ",");
                            String textLine = line.substring (line.lastIndexOf("$diagnostics,")+13, line.length());
                            ibacWriterDiag.write (textLine);
                            ibacWriterDiag.newLine();
                        }
                    }
                    else if (line.contains(":Temp = "))
                    {
                        temp = Float.parseFloat (line.substring (line.indexOf(":Temp = ") + 8, line.length()));
                    }
                    else if (line.contains(":Humidity = "))
                    {
                        humidity = Float.parseFloat (line.substring (line.indexOf(":Humidity = ") + 12, line.length()));
                    }
                    else if (line.contains(":Fan "))
                    {
                        long timestamp = getRTC (line);
                        fan = Integer.parseInt (line.charAt(line.indexOf("Fan ") + 4) + "");
                        servo = Integer.parseInt (line.charAt (line.indexOf("Servo ") + 6) + "");
                        heat = Integer.parseInt (line.charAt (line.indexOf("Heater ") + 7) + "");

                        tempWriterT.write (timestamp + ",");
                        tempWriterT.write (temp + "," + humidity + "," + fan + "," + servo + "," + heat);
                        tempWriterT.newLine();

                        temp = -100; humidity = -100;
                        fan = -100; servo = -100; heat = -100;
                    }
                    else
                    {
                        //Ignore Bladewerx and Brigdeport messages.  We have logs elsewhere for them
                    }
                }
            }
            catch (IOException e)
            {
                throw new HumpsException ("Error reading tracker file");
            }


            try
            {
                ibacWriterDiag.close();
                ibacWriterCount.close();
                tempWriterT.close();
            }
            catch (Exception e)
            {
                throw new HumpsException ("Error closing files");
            }
        }



        if (collectorFilename != null)
        {
            try
            {
                tempWriterC = new BufferedWriter(new FileWriter(outputFilename + "Collector_Temp.log"));
                c100Writer = new BufferedWriter(new FileWriter(outputFilename + "C100.log"));
                anacondaWriterStatus = new BufferedWriter(new FileWriter(outputFilename + "Anaconda_Status.log"));
                anacondaWriterLcda = new BufferedWriter(new FileWriter(outputFilename + "Anaconda_LCDA.log"));
                anacondaWriterLcdb = new BufferedWriter(new FileWriter(outputFilename + "Anaconda_LCDB.log"));
            } catch (IOException ex) {
                throw new HumpsException ("Unable to open output file!");
            }

            //Open file to read
            BufferedReader fileReader;
            try {
                fileReader = new BufferedReader(new FileReader(collectorFilename));
            } catch (FileNotFoundException ex) {
                throw new HumpsException ("Collector file not found!");
            }


            String line = "";
            try
            {
                //Header stuff
                fileReader.readLine();
                fileReader.readLine();
                fileReader.readLine();
                fileReader.readLine();

                anacondaWriterStatus.write("Timestamp,lcdaCurrMode,lcdaReqMode,lcdbCurrMode,lcdbReqMode,manTargTemp,manTargTemp,manActTemp,pitTargTemp,pitActTemp,volts,amps");
                anacondaWriterStatus.newLine();
                anacondaWriterLcda.write ("Timestamp,Pressure,Temperature,DMMP,TEP,MS,AA,Unk");
                anacondaWriterLcda.newLine();
                anacondaWriterLcdb.write ("Timestamp,Pressure,Temperature,DMMP,TEP,MS,AA,Unk");
                anacondaWriterLcdb.newLine();
                c100Writer.write ("Timestamp,Idle,DrySample,WetSample1,WetSample2,WetSample3,WetSample4,Priming,Cleaning");
                c100Writer.newLine();
                tempWriterC.write ("Timestamp,Temperature (C),Humidity,Fan State,Servo State,Heat State");
                tempWriterC.newLine();

                int lastC100SampleCommand = 0;
                int forceDrySample = 0;

                //Loop through file and parse each line
                while((line = fileReader.readLine())!= null)
                {
                    if (line.contains(":C100_SAMPLE received"))
                    {
                        //Catch command for C100 sample.  ICX Changed status message so vial set of wet sample
                        //isn't in logs where it used ot be.
                        lastC100SampleCommand = Integer.parseInt(line.substring(line.length()-1));
                    }
                    else if (line.contains ("collector turned on"))
                    {
                        forceDrySample = 1;
                    }
                    else if (line.contains ("collector turned off"))
                    {
                        forceDrySample = 0;
                    }
                    else if(line.contains(":Serial port C"))
                    {
                        long timestamp = getRTC (line);
                        if (line.contains (": $s,"))
                        {
                            c100Writer.write(timestamp + ",");
                            int idle = 0, drySample = 0, wetSample1 = 0, wetSample2 = 0, wetSample3 = 0, wetSample4 = 0, priming = 0, cleaning = 0;

                            if (line.contains (",idle"))
                                idle = 1;
                            else if(line.contains(",collecting dry sample"))
                                drySample = 1;
                            else if (line.contains (",collecting wet sample 1"))
                                wetSample1 = 1;
                            else if (line.contains (",collecting wet sample 2"))
                                wetSample2 = 1;
                            else if (line.contains (",collecting wet sample 3"))
                                wetSample3 = 1;
                            else if (line.contains (",collecting wet sample 4"))
                                wetSample4 = 1;
                            else if (line.contains (",collecting wet sample"))
                            {
                                if (lastC100SampleCommand == 1)
                                    wetSample1 = 1;
                                if (lastC100SampleCommand == 2)
                                    wetSample2 = 1;
                                if (lastC100SampleCommand == 3)
                                    wetSample3 = 1;
                                if (lastC100SampleCommand == 4)
                                    wetSample4 = 1;
                            }
                            else if (line.contains (",priming"))
                                priming = 1;
                            else if (line.contains (",cleaning"))
                                cleaning = 1;


                            if (idle == 1 && forceDrySample == 1)
                            {
                                idle = 0;
                                drySample = 1;
                            }
                            
                            c100Writer.write (idle + "," + drySample + "," + wetSample1 + ","  + wetSample2 + ","  + wetSample3 + ","  + wetSample4 + ","  + priming + ","  + cleaning);
                            c100Writer.newLine();
                        }
                    }
                    else if(line.contains(":Serial port E"))
                    {
                        long timestamp = getRTC (line);
                        String message = line.substring (line.indexOf(" message received: ") + 19);
                        if (message.startsWith ("7e,7e,"))
                        {
                            String[] chars = message.split (",");
                            int[] bytes = new int [chars.length];
                            for (int i = 0; i < bytes.length; i ++)
                                bytes[i] = Integer.parseInt(chars[i], 16);

                            if (bytes[2] == 0x80)
                            {
                                //Status message
                                int lcdaCurrMode = bytes[20];
                                int lcdaReqMode = bytes[19];
                                int lcdbCurrMode = bytes[18];
                                int lcdbReqMode = bytes[17];
                                int manTargTemp = bytes[22] + bytes[24]*256;
                                int manActTemp = bytes[21] + bytes[22]*256;
                                int pitTargTemp = bytes[27] + bytes[28]*256;
                                int pitActTemp = bytes[25] + bytes[26]*256;
                                float volts = (bytes[31] + bytes[32]*256)*102.4f/4095;
                                float amps = (bytes[29] + bytes[30]*256)*4.096f/4095;

                                anacondaWriterStatus.write(timestamp + ",");
                                anacondaWriterStatus.write (lcdaCurrMode + "," + lcdaReqMode + "," + lcdbCurrMode + "," + lcdbReqMode + "," + manTargTemp + "," + manTargTemp + "," + manActTemp + "," + pitTargTemp + "," + pitActTemp + "," + volts + "," + amps);
                                anacondaWriterStatus.newLine();
                            }
                            else if (bytes[2] == 0x81)
                            {
                                //LCDA message
                                float temperature = (bytes[11] + bytes[12]*256)/10.0f;
                                float pressure = (bytes[9] + bytes[10]*256)/10.0f;
                                int numAgents = bytes[13];

                                int dmmp = 0;
                                int aa = 0;
                                int tep = 0;
                                int ms = 0;
                                int unk = 0;

                                for (int i = 0; i < numAgents; i += 2)
                                {
                                    int agentID = bytes[14 + i];
                                    int bars = bytes[14 + i + 1];

                                    if (agentID == 8)
                                        dmmp = bars;
                                    else if (agentID == 9)
                                        tep = bars;
                                    else if (agentID == 17)
                                        ms = bars;
                                    else
                                        unk = bars;
                                }
                                anacondaWriterLcda.write (timestamp + ",");
                                anacondaWriterLcda.write (pressure + "," + temperature + "," + dmmp + "," + tep + "," + ms + "," + aa + "," + unk + ",");
                                anacondaWriterLcda.newLine();
                            }
                            else if (bytes[2] == 0x82)
                            {
                                //LCDB message
                                float temperature = (bytes[11] + bytes[12]*256)/10.0f;
                                float pressure = (bytes[9] + bytes[10]*256)/10.0f;
                                int numAgents = bytes[13];

                                int dmmp = 0;
                                int aa = 0;
                                int tep = 0;
                                int ms = 0;
                                int unk = 0;

                                for (int i = 0; i < numAgents; i += 2)
                                {
                                    int agentID = bytes[14 + i];
                                    int bars = bytes[14 + i + 1];

                                    if (agentID == 8)
                                        dmmp = bars;
                                    else if (agentID == 9)
                                        tep = bars;
                                    else if (agentID == 17)
                                        ms = bars;
                                    else
                                        unk = bars;
                                }
                                anacondaWriterLcdb.write (timestamp + ",");
                                anacondaWriterLcdb.write (pressure + "," + temperature + "," + dmmp + "," + tep + "," + ms + "," + aa + "," + unk + ",");
                                anacondaWriterLcdb.newLine();
                            }
                        }
                        else
                        {
                            //Not sure, message is screwed up
                        }
                    }
                    else if (line.contains(":Temp = "))
                    {
                        temp = Float.parseFloat (line.substring (line.indexOf(":Temp = ") + 8, line.length()));
                    }
                    else if (line.contains(":Humidity = "))
                    {
                        humidity = Float.parseFloat (line.substring (line.indexOf(":Humidity = ") + 12, line.length()));
                    }
                    else if (line.contains(":Fan "))
                    {
                        long timestamp = getRTC (line);
                        fan = Integer.parseInt (line.charAt(line.indexOf("Fan ") + 4) + "");
                        servo = Integer.parseInt (line.charAt (line.indexOf("Servo ") + 6) + "");
                        heat = Integer.parseInt (line.charAt (line.indexOf("Heater ") + 7) + "");

                        tempWriterC.write (timestamp + ",");
                        tempWriterC.write (temp + "," + humidity + "," + fan + "," + servo + "," + heat);
                        tempWriterC.newLine();

                        temp = -100; humidity = -100;
                        fan = -100; servo = -100; heat = -100;
                    }
                    else
                    {
                        //Ignore anything else
                    }
                }
            }
            catch (IOException e)
            {
                throw new HumpsException ("Error reading collector file");
            }
            catch (NumberFormatException e)
            {
                throw new HumpsException ("Error parsing collector file");
            }


            try
            {
                tempWriterC.close();
                c100Writer.close();
                anacondaWriterStatus.close();
                anacondaWriterLcda.close();
                anacondaWriterLcdb.close();
            }
            catch (Exception e)
            {
                throw new HumpsException ("Error closing files");
            }
        }
    }

    static long getRTC (String line) throws HumpsException
    {
        long retVal = 0;
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        try
        {
            Date date = formatter.parse(line.substring(0, line.indexOf(" :")) + " GMT-00:00");
            retVal = date.getTime();
        }
        catch (Exception e)
        {
            throw new HumpsException ("Error parsing date in xD file");
        }
        return retVal;
    }



    static public void processPiccolo (String piccoloFilename, String outputFolder) throws HumpsException
    {
        //Open summary output file
        String outputFilename = outputFolder + piccoloFilename.substring(piccoloFilename.lastIndexOf("\\"));
        BufferedWriter piccoloWriter;
        try {
            piccoloWriter = new BufferedWriter(new FileWriter(outputFilename));
        } catch (IOException ex) {
            throw new HumpsException ("Unable to open output file!");
        }

        if (piccoloFilename != null)
        {
            //Open file to read
            BufferedReader fileReader;
            try {
                fileReader = new BufferedReader(new FileReader(piccoloFilename));
            } catch (FileNotFoundException ex) {
                throw new HumpsException ("Piccolo file not found!");
            }


            String line = "";
            try
            {
                //Header stuff
                piccoloWriter.write (fileReader.readLine() + "\r\n");

                int index = 0;
                int skipCount = 5;
                //Loop through file and sparse out lines
                while((line = fileReader.readLine())!= null)
                {
                    if (index == 0)
                        piccoloWriter.write (line + "\r\n");

                    index = (index+1)%skipCount;
                }
            }
            catch (IOException e)
            {
                throw new HumpsException ("Error reading piccolo file");
            }
        }


        try
        {
            piccoloWriter.close();
        }
        catch (Exception e)
        {
            throw new HumpsException ("Error closing files");
        }
    }

    static public void processShadow (String shadowFilename, String outputFolder) throws HumpsException
    {
        //Open summary output file
        String outputFilename = outputFolder + shadowFilename.substring(shadowFilename.lastIndexOf("\\"));
        BufferedWriter shadowWriter;
        try {
            shadowWriter = new BufferedWriter(new FileWriter(outputFilename));
        } catch (IOException ex) {
            throw new HumpsException ("Unable to open output file!");
        }

        if (shadowFilename != null)
        {
            //Open file to read
            BufferedReader fileReader;
            try {
                fileReader = new BufferedReader(new FileReader(shadowFilename));
            } catch (FileNotFoundException ex) {
                throw new HumpsException ("Shadow file not found!");
            }


            String line = "";
            try
            {
                //Loop through file and copy lines
                while((line = fileReader.readLine())!= null)
                {
                    shadowWriter.write (line + "\r\n");
                }
            }
            catch (IOException e)
            {
                throw new HumpsException ("Error reading shadow file");
            }
        }


        try
        {
            shadowWriter.close();
        }
        catch (Exception e)
        {
            throw new HumpsException ("Error closing files");
        }
    }

};


class HumpsException extends Exception
{
    String what;
    public HumpsException (String what)
    {
        this.what = what;
    }
};
