/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class PlumeDetectionBelief extends Belief
{
    public static final String BELIEF_NAME = "PlumeDetectionBelief";
    
    float m_AlignScore;
    float m_DetectScore;
    String m_PlumeDetectionString;

    public PlumeDetectionBelief()
    {
        super();
    }

    public PlumeDetectionBelief(String agentID, String detectionString)
    {
        super(agentID);
        parsePlumeDetectionString (detectionString);
        timestamp = new Date(System.currentTimeMillis());
    }
    
    public PlumeDetectionBelief (String agentID, float alignScore, float detectScore)
    {
        this (agentID, alignScore, detectScore, System.currentTimeMillis());
    }
    
    public PlumeDetectionBelief (String agentID, float alignScore, float detectScore, long timeMs)
    {
        super(agentID);
        timestamp = new Date(timeMs);
        setPlumeDetection(alignScore, detectScore);
    }
    
    public float getAlign()
    {
        return m_AlignScore;
    }
    
    public float getDetect()
    {
        return m_DetectScore;
    }

    public void setPlumeDetection(float alignScore, float detectScore)
    {
        m_AlignScore = alignScore;
        m_DetectScore = detectScore;
        formPlumeDetectionString ();
    }

    public String getPlumeDetectionString()
    {
        return m_PlumeDetectionString;
    }  
    
    private void formPlumeDetectionString()
    {
        m_PlumeDetectionString = "Align:" + DecimalFormat.getNumberInstance().format(m_AlignScore) + 
                " Detect: " + m_DetectScore;
    }
    
    private void parsePlumeDetectionString (String detectionString)
    {
        String[] tokens = detectionString.split("[: \n]");
        try
        {
            m_AlignScore = Float.parseFloat(tokens[1]);
            m_DetectScore = Float.parseFloat(tokens[4]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void addBelief(Belief b)
    {
         PlumeDetectionBelief belief = (PlumeDetectionBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_AlignScore = belief.m_AlignScore;
          this.m_DetectScore = belief.m_DetectScore;
          formPlumeDetectionString();
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

        PlumeDetectionBelief belief = null;

        try
        {
            belief = (PlumeDetectionBelief) clas.newInstance();
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

        out.writeFloat (m_AlignScore);
        out.writeFloat (m_DetectScore);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_AlignScore = in.readFloat();
        m_DetectScore = in.readFloat();
        
        formPlumeDetectionString();
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }
}
