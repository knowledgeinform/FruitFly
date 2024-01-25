/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage.AnacondaDataPair;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author stipeja1
 */
public class AnacondaDetectionBelief extends Belief
{

    public static final String BELIEF_NAME = "AnacondaDetectionBelief";

    private AnacondaDataPair []_lcda;
    private AnacondaDataPair []_lcdb;
    private String _anacondaDetectionString;
    
    private static final HashMap <Integer,String> m_AgentMap = new HashMap<Integer, String>();
    private static final HashMap <String,Integer> m_ReverseAgentMap = new HashMap<String, Integer>();

    public AnacondaDetectionBelief()
    {  
        this ("unknown", null, null);
    }
    
    public AnacondaDetectionBelief (String agentID, String detectionString)
    {
        parseDetectionString(detectionString);
    }

    public AnacondaDetectionBelief(String agentID, AnacondaDataPair []lcda, AnacondaDataPair []lcdb)
    {
        this (agentID, lcda, lcdb, System.currentTimeMillis());
    }

    public AnacondaDetectionBelief(String agentID, AnacondaDataPair []lcda, AnacondaDataPair []lcdb, long timestampMs)
    {
        super(agentID);
        
        timestamp = new Date(timestampMs);
        setAnacondaDetections(lcda, lcdb);
    }
    
    private synchronized static void formAgentMap ()
    {
        if (m_AgentMap.isEmpty())
        {
            // Parse configuration properties.
            for(int i=1; i<=37; i++)
            {
                m_AgentMap.put(i, Config.getConfig().getProperty("podAction.anaconda.agent"+i, "NULL"+i));
                m_ReverseAgentMap.put(Config.getConfig().getProperty("podAction.anaconda.agent"+i, "NULL"+i), i);
            }
        }
    }
        

    public void setAnacondaDetections(AnacondaDataPair []lcda, AnacondaDataPair []lcdb)
    {
        _lcda = lcda;
        _lcdb = lcdb;
        formAnacondaDetectionString ();
    }
    
    private void formAnacondaDetectionString()
    {
        _anacondaDetectionString = "LCDA: " + formDataPairString (_lcda) + "\nLCDB: " + formDataPairString (_lcdb);
    }
    
    public static HashMap <Integer,String> accessAgentMap ()
    {
        return m_AgentMap;
    }
    
    public int getMaxLcdBars ()
    {
        int maxBars = 0;
        if (_lcda != null)
        {
            int bars = 0;
            for (AnacondaDataPair pair : _lcda)
                bars += pair.bars;
            if (bars > maxBars)
                maxBars = bars;
        }
        
        if (_lcdb != null)
        {
            int bars = 0;
            for (AnacondaDataPair pair : _lcdb)
                bars += pair.bars;
            if (bars > maxBars)
                maxBars = bars;
        }
        
        return maxBars;
    }
    
    public AnacondaDataPair[] getLcda()
    {
        return _lcda;
    }
    
    public AnacondaDataPair[] getLcdb()
    {
        return _lcdb;
    }
    
    private String formDataPairString (AnacondaDataPair [] dataPair)
    {
        String retVal = "";
        if(dataPair != null)
        {
            // Traverse entire list, printing them all out
            for (AnacondaDataPair d : dataPair)
            {
                retVal += m_AgentMap.get(d.agentID) +":" + d.bars + " ";
            }
        }
        return retVal;
    }
    
    private void parseDetectionString (String detectionString)
    {
        String[] lcdLines = detectionString.split("\n");
        String [] lcdaLine = null;
        String [] lcdbLine = null;
        if (lcdLines.length == 2)
        {
            lcdaLine = lcdLines[0].split("[: ]");
            lcdbLine = lcdLines[1].split("[: ]");
        }
        else
        {
            return;
        }
        
        try
        {
            if (lcdaLine != null && lcdaLine.length > 2)
            {
                _lcda = new AnacondaDataPair[1];
                _lcda[0] = new AnacondaDataPair();
                _lcda[0].agentID = m_ReverseAgentMap.get(lcdaLine[3]);
                _lcda[0].bars = Integer.parseInt(lcdaLine[4]);
            }
            if (lcdbLine != null && lcdbLine.length > 2)
            {
                _lcdb = new AnacondaDataPair[1];
                _lcdb[0] = new AnacondaDataPair();
                _lcdb[0].agentID = m_ReverseAgentMap.get(lcdbLine[3]);
                _lcdb[0].bars = Integer.parseInt(lcdbLine[4]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getAnacondaDetectionString()
    {
        return _anacondaDetectionString;
    }
    
    public String getLcdaDetectionString()
    {
        if (_anacondaDetectionString == null)
            return null;
        
        return _anacondaDetectionString.substring(0, _anacondaDetectionString.indexOf("\n"));
    }
    
    public String getLcdbDetectionString()
    {
        if (_anacondaDetectionString == null)
            return null;
        
        return _anacondaDetectionString.substring(_anacondaDetectionString.indexOf("\n")+1);
    }

    @Override
    protected void addBelief(Belief b)
    {
         AnacondaDetectionBelief belief = (AnacondaDetectionBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._lcda = belief._lcda;
          this._lcdb = belief._lcdb;
          formAnacondaDetectionString();
        }
    }


    @Override
    public byte[] serialize() throws IOException
    {
        setTransmissionTime();
        ByteArrayOutputStream baStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baStream);

        writeExternal(out);
        out.flush();
        out.close();
        return baStream.toByteArray();
    }

    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        AnacondaDetectionBelief belief = null;

        try
        {
            belief = (AnacondaDetectionBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        if (_lcda == null)
        {    
            out.writeInt(0);
        }
        else
        {
            out.writeInt (_lcda.length);
            for (int i = 0; i < _lcda.length; i ++)
            {
                out.writeInt(_lcda[i].bars);
                out.writeInt(_lcda[i].agentID);
            }
        }
        
        if (_lcdb == null)
        {
            out.writeInt (0);
        }
        else
        {
            out.writeInt (_lcdb.length);
            for (int i = 0; i < _lcdb.length; i ++)
            {
                out.writeInt(_lcdb[i].bars);
                out.writeInt(_lcdb[i].agentID);
            }
        }
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        int lcdaLength = in.readInt();
        if (lcdaLength == 0)
        {
            _lcda = null;
        }
        else
        {
            _lcda = new AnacondaDataPair[lcdaLength];
            for (int i = 0; i < _lcda.length; i ++)
            {
                AnacondaDataPair pair = anacondaLCDReportMessage.newDataPairInstance();
                pair.bars = in.readInt();
                pair.agentID = in.readInt();
                _lcda[i] = pair;
            }
        }
        
        int lcdbLength = in.readInt();
        if (lcdbLength == 0)
        {
            _lcdb = null;
        }
        else
        {
            _lcdb = new AnacondaDataPair[lcdbLength];
            for (int i = 0; i < _lcdb.length; i ++)
            {
                AnacondaDataPair pair = anacondaLCDReportMessage.newDataPairInstance();
                pair.bars = in.readInt();
                pair.agentID = in.readInt();
                _lcdb[i] = pair;
            }
        }

        formAnacondaDetectionString();
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }

  
  static {
      formAgentMap ();
  }
}
