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
public class ExplosionTimeBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "ExplosionTimeBaseBelief";

    private long m_time_ms;

    public ExplosionTimeBaseBelief()
    {
        super("unspecified");
    }

    public ExplosionTimeBaseBelief(String agentId)
    {
        super(agentId);
    }

    public ExplosionTimeBaseBelief(String agentId, long time_ms)
    {
        this (agentId, time_ms, new Date (System.currentTimeMillis()));
    }
            
    public ExplosionTimeBaseBelief(String agentId, long time_ms, Date time)
    {
        super(agentId);
        timestamp = time;
        m_time_ms = time_ms;
    }

    @Override
    public void addBelief(Belief b)
    {
        ExplosionTimeBaseBelief belief = (ExplosionTimeBaseBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_time_ms = belief.m_time_ms;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeLong(m_time_ms);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_time_ms = in.readLong();
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

        ExplosionTimeBaseBelief belief = null;

        try
        {
            belief = (ExplosionTimeBaseBelief) clas.newInstance();
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
    public boolean equals (Object o)
    {
        if (o instanceof ExplosionTimeBaseBelief)
        {
            if (this.getTime_ms() == ((ExplosionTimeBaseBelief)o).getTime_ms())
                return true;
        }
        return false;
    }


    public long getTime_ms()
    {
        return m_time_ms;
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
}
