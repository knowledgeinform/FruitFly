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
 * @author xud1
 */
public class GuaranteedVideoDataBelief extends Belief
{
    public static final String BELIEF_NAME = "GuaranteedVideoDataBelief";    
    private int size = 0;
    private byte[] data = null;
    
    public GuaranteedVideoDataBelief()
    {
        super();
    }    
 
    public GuaranteedVideoDataBelief(String agentID, int size, byte[] data)
    {
        this(agentID, size, data, new Date(System.currentTimeMillis()));
    }    
    
    public GuaranteedVideoDataBelief(String agentID, int size, byte[] data, Date timestamp)
    {
        super(agentID);
        this.size = size;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    @Override
    public void addBelief(Belief b)
    {
        GuaranteedVideoDataBelief belief = (GuaranteedVideoDataBelief)b;
        
        if(belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.size = belief.getSize();
            this.data = belief.getData();
            this.timestamp = belief.getTimeStamp();
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
        
        Belief belief = null;
        
        try
        {
            belief = (Belief)clas.newInstance();            
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
    public void writeExternal(DataOutput out) throws IOException
    {
        try
        {
            super.writeExternal(out);
            out.writeLong(timestamp.getTime());
            out.writeInt(size);
            out.write(data, 0, data.length);                  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);
            timestamp = new Date(in.readLong());
            size = in.readInt();
            data = new byte[size];
            in.readFully(data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }                

    public int getSize()
    {
        return this.size;
    }
    
    public byte[] getData()
    {
        return this.data;
    }
    
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }    
}
