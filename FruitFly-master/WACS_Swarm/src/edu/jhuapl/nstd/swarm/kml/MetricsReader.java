/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.kml;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionMessageBelief;
import edu.jhuapl.nstd.swarm.belief.EtdStatusMessageBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

/**
 *
 * @author humphjc1
 */
public class MetricsReader
{
    String inputFilename;
    BufferedReader reader;
    LinkedList <LinkedList<String>> metrics;
    int currPosition;


    long currTime = -1;
    AgentPositionBelief currAgentPositionBelief = null;
    AnacondaDetectionBelief currAnacondaDetectionBelief = null;
    ParticleDetectionBelief currParticleDetectionBelief = null;
    CloudDetectionBelief currIbacCloudBelief = null;
    CloudDetectionBelief currAnacondaCloudBelief = null;
    EtdDetectionBelief etdDetectionBelief = null;
    EtdStatusMessageBelief etdStatusMessage = null;
    EtdDetectionMessageBelief etdDetectionMessage = null;

    public MetricsReader (String filename)
    {
        try 
        {
            inputFilename = filename;
            reader = new BufferedReader(new FileReader(new File(inputFilename)));
            initReader ();
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }

    }
    
    private void initReader ()
    {
        String line = null;
        LinkedList fullLine = null;
        metrics = new LinkedList<LinkedList<String>> ();
        
        try
        {
            while ((line = reader.readLine()) != null) 
            {
                if(line.startsWith("time:"))
                {
                    fullLine = new LinkedList<String> ();
                    fullLine.add(line);
                }
                else if (line.contains ("end time"))
                {
                    fullLine.add(line);
                    metrics.add(fullLine);
                    fullLine = null;
                }
                else if(fullLine != null)
                {
                    fullLine.add(line);
                }
            }

            reader.close();
            currPosition = 0;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public long getLastTimestamp()
    {
        evaluateData (metrics.get(metrics.size()-1));
        return currTime;
    }

    public boolean stepForward()
    {
        LinkedList<String> data = null;
        if (currPosition > metrics.size() - 1)
            currPosition = metrics.size() - 1;

        if (currPosition < metrics.size() && (data = metrics.get(currPosition++)) != null)
        {
            evaluateData(data);
            System.out.println ("Processing metrics line " + currPosition + " of " + metrics.size());
        }

        if (currPosition > metrics.size() - 1 || data == null)
            return false;
        return true;
    }

    public boolean stepBackward()
    {
        LinkedList<String> data = null;
        if (currPosition < 0)
            currPosition = 0;

        if (currPosition >= 0 && (data = metrics.get(currPosition--)) != null)
        {
            evaluateData(data);
        }

        if (currPosition < 0 || data == null)
            return false;
	return true;
    }

    private void evaluateData(LinkedList <String> data)
    {
        currAgentPositionBelief = null;
        currAnacondaDetectionBelief = null;
        currParticleDetectionBelief = null;
        currIbacCloudBelief = null;
        currAnacondaCloudBelief = null;

        for (int i = 0; i < data.size(); i ++)
        {
            if (data.get(i).startsWith("time:"))
            {
                String line = data.get(i);
                String time = line.substring(line.lastIndexOf(" ")+1);

                currTime = Long.parseLong(time);
            }
            else if(data.get(i).equals("belief: agentPositionBelief"))
            {
                String line = null;
                for (int j = i+1; j < data.size(); j ++)
                {
                    if (data.get(j).contains(WACSAgent.AGENTNAME))
                    {
                        line = data.get(j);
                        break;
                    }
                }

                if (line != null)
                {
                    String[] splitLine = line.split (" ");

                    double lat = Double.parseDouble(splitLine[3]);
                    double lon = Double.parseDouble(splitLine[6]);
                    double alt = Double.parseDouble(splitLine[9]);
                    currAgentPositionBelief = new AgentPositionBelief("unk", new LatLonAltPosition(new Latitude (lat, Angle.DEGREES), new Longitude (lon, Angle.DEGREES), new Altitude (alt, Length.METERS)));
                }
            }
            else if (data.get(i).equals ("anacondadetectionbelief:"))
            {
                String line = "";
                for (int j = i+1; j < data.size(); j ++)
                {
                    if (data.get(j).contains("LCDA") || data.get(j).contains("LCDB"))
                    {
                        line += data.get(j) + " ";
                    }
                    else if (data.get(j).contains ("end"))
                    {
                        break;
                    }
                }

                if (line.length() > 0)
                    currAnacondaDetectionBelief = new AnacondaDetectionBelief("unk", line);
            }
            else if (data.get(i).equals ("biopoddetectionbelief:"))
            {
                String line = "";
                for (int j = i+1; j < data.size(); j ++)
                {
                    if (data.get(j).contains("Large") || data.get(j).contains("Small"))
                    {
                        line += data.get(j) + " ";
                    }
                    else if (data.get(j).contains ("end"))
                    {
                        break;
                    }
                }

                if (line.length() > 0)
                    currParticleDetectionBelief = new ParticleDetectionBelief("unk", line);
            }
            else if (data.get(i).equals ("clouddetectionbelief:"))
            {
                boolean isAnaconda = false;
                boolean isIbac = false;

                for (int j = i+1; j < data.size(); j ++)
                {
                    if (data.get(j).contains("source: 0"))
                    {
                        isAnaconda = true;
                    }
                    else if(data.get(j).contains("source: 1"))
                    {
                        isIbac = true;
                    }
                    else if (data.get(j).contains ("end"))
                    {
                        break;
                    }
                }

                if (isAnaconda)
                    currAnacondaCloudBelief = new CloudDetectionBelief();
                if (isIbac)
                    currIbacCloudBelief = new CloudDetectionBelief();
            }
            else if (data.get(i).equals ("etddetectionbelief:"))
            {
                long time = 0;
                float latitude = 0f;
                float longitude = 0f;
                float altitude = 0f;
                float concentration = 0f;
        
                for (int j = i+1; j < data.size(); j ++)
                {
                    String[] splitLine = data.get(j).split (":");
                    
                    if (splitLine[0].contains("time"))
                    {
                        time = Long.parseLong(splitLine[1].trim());
                    }
                    else if(splitLine[0].contains("latitude"))
                    {
                        latitude = Float.parseFloat(splitLine[1].trim());
                    }
                    else if(splitLine[0].contains("longitude"))
                    {
                        longitude = Float.parseFloat(splitLine[1].trim());
                    }
                    else if(splitLine[0].contains("altitude"))
                    {
                        altitude = Float.parseFloat(splitLine[1].trim());
                    }
                    else if(splitLine[0].contains("concentration"))
                    {
                        concentration = Float.parseFloat(splitLine[1].trim());
                    }
                    else if (data.get(j).contains ("end"))
                    {
                        break;
                    }
                }
                
                AbsolutePosition pos = new LatLonAltPosition(new Latitude(latitude, Angle.DEGREES),
				new Longitude(longitude, Angle.DEGREES),
				new Altitude(altitude, Length.METERS));
                etdDetectionBelief = new EtdDetectionBelief("unk", concentration, pos, time);
            }
            else if (data.get(i).equals ("etddetectionmessagebelief:"))
            {
                
                String line = data.get(i+1);
                etdDetectionMessage = new EtdDetectionMessageBelief("unk", currTime, line);
            }
            else if (data.get(i).equals ("etdstatusmessagebelief:"))
            {
                String line = data.get(i+1);
                etdStatusMessage = new EtdStatusMessageBelief("unk", currTime, line);
            }            
        }
    }
}
