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
public class VideoClientSatCommFrameRequestBelief extends Belief
{
    public static final String BELIEF_NAME = "VideoClientGuaranteedFrameRequestBelief";
    private int mReceipt;
    private boolean mLastInterlaceReceived;
    
    public VideoClientSatCommFrameRequestBelief()
    {
        super();
    }    
 
    public VideoClientSatCommFrameRequestBelief(String agentID, int receipt, boolean lastInterlaceReceived)
    {
        this(agentID, receipt, lastInterlaceReceived, new Date(System.currentTimeMillis()));
    }    
    
    public VideoClientSatCommFrameRequestBelief(String agentID, int receipt, boolean lastInterlaceReceived, Date timestamp)
    {
        super(agentID);
        mReceipt = receipt;
        mLastInterlaceReceived = lastInterlaceReceived;
        this.timestamp = timestamp;        
    }
    
    @Override
    public void addBelief(Belief b)
    {
        VideoClientSatCommFrameRequestBelief belief = (VideoClientSatCommFrameRequestBelief)b;
        
        if(belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            mReceipt = belief.getClientReceipt();
            mLastInterlaceReceived = belief.getLastInterlaceReceived();
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
            out.writeInt(mReceipt);  
            out.writeBoolean(mLastInterlaceReceived);
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
            mReceipt = in.readInt();            
            mLastInterlaceReceived = in.readBoolean();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
            
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
    
    public int getClientReceipt()
    {
        return mReceipt;
    }    
    
    public boolean getLastInterlaceReceived()
    {
        return mLastInterlaceReceived;
    }
}
