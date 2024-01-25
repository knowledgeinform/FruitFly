package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * This class is used to store the contents of an Anaconda LCD report message.
 * 
 * @author humphjc1
 */
public abstract class anacondaLCDReportMessage extends cbrnPodMsg 
{
    private long anacondaTimestamp;
    private float pressure;
    private float temperature;
    private int numAgents;
    
    public static final int ANACONDA_LCDA = 0;
    public static final int ANACONDA_LCDB = 1;
    
    /**
     * Data structure used to store a chemical agent id
     * and its strength in bars as reported by this message.
     */
    public static class AnacondaDataPair 
    {
        public int agentID;
        public int bars;
    }
    
    public static AnacondaDataPair newDataPairInstance()
    {
        return new AnacondaDataPair();
    }
    
    /**
     * Stores a list of AnacondaDataPair objects obtained by this message.
     */
    private List <AnacondaDataPair> agentPairs = new ArrayList <AnacondaDataPair> ();
    
    /**
     * anacondaLCDReportMessage constructor
     * 
     * @param messageType
     * @param dataSize
     */
    protected anacondaLCDReportMessage(int messageType, int dataSize) {
        super(cbrnSensorTypes.SENSOR_ANACONDA, messageType, dataSize);
    }
    
    /**
     * Parses this message.
     * 
     * @param cbrnPodMsg a type of anacondaLCDReportMessage
     */
    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        anacondaTimestamp = readDataUnsignedInt();
        pressure = readDataShort()/10.0f;
        temperature = readDataShort()/10.0f;
        numAgents = readDataByte();
        for (int i = 0; i < numAgents; i ++)
        {
            AnacondaDataPair newPair = new AnacondaDataPair();
            newPair.agentID = (char) readDataByte();
            newPair.bars = (char) readDataByte();
            agentPairs.add(newPair);
        }
    }

	/**
	 * Returns the total detection strength of all chemical hits
	 * recorded in the last detection report message.
	 *   
	 * @return chemical strength summation
	 */
    public int getDetectionStrength()
    {
        int detection = 0;
        if(agentPairs != null)
        {
            // Traverse entire list, summing detection strength
            for (AnacondaDataPair d : agentPairs)
            {
                detection +=  d.bars;
            }
        }
        return detection;
    }
    
    public int getMaxDetectionIdCode()
    {
        int maxBars = 0;
        int maxBarsIdCode = 0;
        if(agentPairs != null)
        {
            // Traverse entire list, summing detection strength
            for (AnacondaDataPair d : agentPairs)
            {
                if (d.bars > maxBars)
                {
                    maxBars = d.bars;
                    maxBarsIdCode = d.agentID;
                }
            }
        }
        return maxBarsIdCode;
    }
    
    public int getMaxDetectionBars()
    {
        int maxBars = 0;
        if(agentPairs != null)
        {
            // Traverse entire list, summing detection strength
            for (AnacondaDataPair d : agentPairs)
            {
                if (d.bars > maxBars)
                {
                    maxBars = d.bars;
                }
            }
        }
        return maxBars;
    }
    
    /**
     * Returns the Anaconda timestamp.
     * @return
     */
    public long getAnacondaTimestamp ()
    {
        return anacondaTimestamp;
    }
    
    /**
     * Returns the Anaconda pressure.
     * @return
     */
    public float getPressure ()
    {
        return pressure;
    }
    
    /**
     * Returns the Anaconda temperature.
     * @return
     */
    public float getTemperature ()
    {
        return temperature;
    }
    
    /**
     * Returns the number of agents reported by the Anaconda.
     * @return
     */
    public int getNumAgents ()
    {
        return numAgents;
    }
    
    /**
     * Returns the AnacondaDataPair in the index <i>num</i>.
     * @param num
     * @return
     */
    public AnacondaDataPair accessAgentPair (int num)
    {
        if (num < agentPairs.size())
            return agentPairs.get(num);
        return null;
    }
    
    public AnacondaDataPair[] accessAgentPairList ()
    {
        if (agentPairs == null || agentPairs.isEmpty())
            return null;
        AnacondaDataPair[] pairs = new AnacondaDataPair[agentPairs.size()];
        return (AnacondaDataPair[])agentPairs.toArray(pairs);
    }
    
    public void insertPair (AnacondaDataPair dataPair)
    {
        if (agentPairs == null)
            agentPairs = new LinkedList<AnacondaDataPair>();
        
        agentPairs.add(dataPair);
        numAgents ++;
    }
}
