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
public class WindEstimateSourceBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "WindEstimateSourceBaseBelief";
    
    public static final int WINDSOURCE_WACSAUTOPILOT = 0;
    public static final int WINDSOURCE_UAVAUTOPILOT = 1;
    public static final int WINDSOURCE_WACSGROUNDSTATION = 2;
    
    int m_CurrWindSource;
    
    public WindEstimateSourceBaseBelief()
    {
    }
    
    public WindEstimateSourceBaseBelief(int newWindSource)
    {
        this (newWindSource, System.currentTimeMillis());
    }

    public WindEstimateSourceBaseBelief(int newWindSource, long timestampMs)
    {
        super();
        timestamp = new Date(timestampMs);
        m_CurrWindSource = newWindSource;
    }
    
    @Override
    public boolean equals (Object o)
    {
        if (o == null || !(o instanceof WindEstimateSourceBaseBelief))
            return false;
        
        return (((WindEstimateSourceBaseBelief)o).getWindSource() == this.getWindSource());
    }

    @Override
    public void addBelief(Belief b)
    {
        WindEstimateSourceBaseBelief belief = (WindEstimateSourceBaseBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_CurrWindSource = belief.m_CurrWindSource;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeInt(m_CurrWindSource);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_CurrWindSource = in.readInt();
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

        WindEstimateSourceBaseBelief belief = null;

        try
        {
            belief = (WindEstimateSourceBaseBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }


    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
    
    public int getWindSource ()
    {
        return m_CurrWindSource;
    }
}
