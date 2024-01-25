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
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class SensorSummaryBelief extends Belief
{
    
    
    public static final String BELIEF_NAME = "SensorSummaryBelief";
    
    private SensorSummary m_ChemicalSummary;
    private SensorSummary m_BiologicalSummary;
    private SensorSummary m_RadNucSummary;
    private SensorSummary m_CloudSummary;

    private SensorSummaryBelief()
    {
        
    }
            
    public SensorSummaryBelief(String agentID, SensorSummary chemicalSummary, SensorSummary biologicalSummary, SensorSummary radNucSummary, SensorSummary cloudSummary)
    {
        super (agentID);
        timestamp = new Date ();

        if (chemicalSummary.m_CurrDetectionTimeMs > 0)
            m_ChemicalSummary = new SensorSummary(chemicalSummary);
        if (biologicalSummary.m_CurrDetectionTimeMs > 0)
            m_BiologicalSummary = new SensorSummary(biologicalSummary);
        if (radNucSummary.m_CurrDetectionTimeMs > 0)
            m_RadNucSummary = new SensorSummary(radNucSummary);
        if (cloudSummary.m_CurrDetectionTimeMs > 0)
            m_CloudSummary = new SensorSummary(cloudSummary);
    }
    
    
    @Override
    protected void addBelief(Belief b)
    {
         SensorSummaryBelief belief = (SensorSummaryBelief)b;
        if (belief.getTimeStamp().compareTo(timestamp)> 0 )
        {
          this.timestamp = belief.getTimeStamp();
          if (belief.m_BiologicalSummary != null)
            this.m_BiologicalSummary = new SensorSummary (belief.m_BiologicalSummary);
          if (belief.m_ChemicalSummary != null)
            this.m_ChemicalSummary = new SensorSummary (belief.m_ChemicalSummary);
          if (belief.m_CloudSummary != null)
            this.m_CloudSummary = new SensorSummary (belief.m_CloudSummary);
          if (belief.m_RadNucSummary != null)
            this.m_RadNucSummary = new SensorSummary (belief.m_RadNucSummary);
        }
    }
    
    private void writeExternalSummary (DataOutput out, SensorSummary summary) throws IOException
    {
        if (summary == null)
            out.writeByte(0);
        else
        {
            out.writeByte(1);
            summary.writeExternal (out);
        }
    }
    
    private SensorSummary readExternalSummary (DataInput in) throws IOException
    {
        byte validByte = in.readByte();
        if (validByte == (byte)0)
            return null;
        
        SensorSummary summary = new SensorSummary();
        summary.readExternal (in);
        return summary;
    }
    
    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        
        writeExternalSummary (out, m_ChemicalSummary);
        writeExternalSummary (out, m_BiologicalSummary);
        writeExternalSummary (out, m_RadNucSummary);
        writeExternalSummary (out, m_CloudSummary);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        
        m_ChemicalSummary = readExternalSummary (in);
        m_BiologicalSummary = readExternalSummary (in);
        m_RadNucSummary = readExternalSummary (in);
        m_CloudSummary = readExternalSummary (in);
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

        SensorSummaryBelief belief = null;

        try
        {
            belief = (SensorSummaryBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }
    
    public SensorSummary getChemicalSummary ()
    {
        return m_ChemicalSummary;
    }
    
    public SensorSummary getBiologicalSummary ()
    {
        return m_BiologicalSummary;
    }
    
    public SensorSummary getRadNucSummary ()
    {
        return m_RadNucSummary;
    }
    
    public SensorSummary getCloudSummary ()
    {
        return m_CloudSummary;
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
