/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
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
public abstract class ParticleCloudTrackingTypeBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "ParticleCloudTrackingTypeBaseBelief";

    ParticleCloudPredictionBehavior.TRACKING_TYPE m_CurrTrackingType = ParticleCloudPredictionBehavior.TRACKING_TYPE.MIXTURE;
    
    public ParticleCloudTrackingTypeBaseBelief()
    {
    }
    
    public ParticleCloudTrackingTypeBaseBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE newTrackingType)
    {
        this (newTrackingType, System.currentTimeMillis());
    }

    public ParticleCloudTrackingTypeBaseBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE newTrackingType, long timestampMs)
    {
        super();
        timestamp = new Date(timestampMs);
        m_CurrTrackingType = newTrackingType;
    }
    
    @Override
    public boolean equals (Object o)
    {
        if (o == null || !(o instanceof ParticleCloudTrackingTypeBaseBelief))
            return false;
        
        return (((ParticleCloudTrackingTypeBaseBelief)o).getTrackingType() == this.getTrackingType());
    }

    @Override
    public void addBelief(Belief b)
    {
        ParticleCloudTrackingTypeBaseBelief belief = (ParticleCloudTrackingTypeBaseBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_CurrTrackingType = belief.m_CurrTrackingType;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeInt(m_CurrTrackingType.ordinal());
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_CurrTrackingType = ParticleCloudPredictionBehavior.TRACKING_TYPE.values()[in.readInt()];
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

        ParticleCloudTrackingTypeBaseBelief belief = null;

        try
        {
            belief = (ParticleCloudTrackingTypeBaseBelief) clas.newInstance();
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
    
    public ParticleCloudPredictionBehavior.TRACKING_TYPE getTrackingType ()
    {
        return m_CurrTrackingType;
    }
}
