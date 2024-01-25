/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class ParticleDetectionBelief extends Belief
{
    public static final String BELIEF_NAME = "ParticleDetectionBelief";
    
    private int m_LCI;
    private int m_SCI;
    private int m_BLCI;
    private int m_BSCI;
    private float m_BioPercent;
    String m_ParticleDetectionString = "";

    public ParticleDetectionBelief()
    {
    }
    
    public ParticleDetectionBelief(String agentID, String detectionString)
    {
        parseDetectionString (detectionString);
        timestamp = new Date(System.currentTimeMillis());
    }

    public ParticleDetectionBelief(String agentID, int lci, int sci, int blci, int bsci, float bioPercent)
    {
        this (agentID, lci, sci, blci, bsci, bioPercent, System.currentTimeMillis());
    }

    public ParticleDetectionBelief(String agentID, int lci, int sci, int blci, int bsci, float bioPercent, long timeMs)
    {
        super(agentID);
        timestamp = new Date(timeMs);
        setParticleDetections(lci, sci, blci, bsci, bioPercent);
        
    }

    public void setParticleDetections(int lci, int sci, int blci, int bsci, float bioPercent)
    {
        m_LCI = lci;
        m_SCI = sci;
        m_BLCI = blci;
        m_BSCI = bsci;
        m_BioPercent = bioPercent;
        formParticleDetectionString ();
    }
    
    private void formParticleDetectionString()
    {
        m_ParticleDetectionString = "Large: " + m_LCI + "   Small: "+ m_SCI +
                      "\nBio Large: "+ m_BLCI + " Bio Small: "+ m_BSCI + "\n";
    }
    
    private void parseDetectionString (String detectionString)
    {
        String[] tokens = detectionString.split("[: \n]");
        try
        {
            m_LCI = Integer.parseInt(tokens[3]);
            m_SCI = Integer.parseInt(tokens[8]);
            m_BLCI = Integer.parseInt(tokens[13]);
            m_BSCI = Integer.parseInt(tokens[17]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public int getLCI ()
    {
        return m_LCI;
    }
    
    public int getSCI ()
    {
        return m_SCI;
    }
    
    public int getBLCI ()
    {
        return m_BLCI;
    }
    
    public int getBSCI ()
    {
        return m_BSCI;
    }
    
    public float getBioPercent ()
    {
        return m_BioPercent;
    }

    public String getParticleDetectionString()
    {
        return m_ParticleDetectionString;
    }  

    @Override
    protected void addBelief(Belief b)
    {
         ParticleDetectionBelief belief = (ParticleDetectionBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_LCI = belief.m_LCI;
          this.m_SCI = belief.m_SCI;
          this.m_BLCI = belief.m_BLCI;
          this.m_BSCI = belief.m_BSCI;
          this.m_BioPercent = belief.m_BioPercent;
          formParticleDetectionString();
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

        ParticleDetectionBelief belief = null;

        try
        {
            belief = (ParticleDetectionBelief) clas.newInstance();
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

        out.writeInt (m_LCI);
        out.writeInt (m_SCI);
        out.writeInt (m_BLCI);
        out.writeInt (m_BSCI);
        out.writeFloat (m_BioPercent);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_LCI = in.readInt();
        m_SCI = in.readInt();
        m_BLCI = in.readInt();
        m_BSCI = in.readInt();
        m_BioPercent = in.readFloat();
        
        formParticleDetectionString();
    }


      /**
   * Returns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }

}
