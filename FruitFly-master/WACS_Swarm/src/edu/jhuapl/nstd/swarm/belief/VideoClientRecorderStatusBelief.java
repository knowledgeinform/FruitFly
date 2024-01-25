/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.util.Date;
import edu.jhuapl.nstd.tase.RecorderStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

/**
 *
 * @author xud1
 */

public class VideoClientRecorderStatusBelief extends Belief //implements BeliefExternalizable
{
    public static final String BELIEF_NAME = "VideoClientRecorderStatusBelief";
    protected RecorderStatus mStatus;
            
    public VideoClientRecorderStatusBelief()
    {
        super();
    }
    
    public VideoClientRecorderStatusBelief(String agentID, RecorderStatus status)
    {
        this(agentID, status, new Date(System.currentTimeMillis()));
    }
    
    public VideoClientRecorderStatusBelief(String agentID, RecorderStatus status, Date time)
    {
        super(agentID);
        timestamp = time;
        mStatus = status;
    }
    
    public void setState(RecorderStatus status)
    {
        mStatus = status;
    }
    
    public RecorderStatus getState()
    {
        return mStatus;
    }
    
    public boolean equals (Object o)
    {
        if (o instanceof VideoClientRecorderCmdBelief)
        {
            return (mStatus.isRecording == ((VideoClientRecorderCmdBelief)o).getRecorderCmd());
        }
        return super.equals(o);
    }
    
    @Override
    protected void addBelief(Belief b)
    {
        VideoClientRecorderStatusBelief belief = (VideoClientRecorderStatusBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp)>0)
        {
            this.timestamp = belief.getTimeStamp();
            this.mStatus = belief.getState();
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
        byte[] tmp= baStream.toByteArray();
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
        super.writeExternal(out);
        
        byte[] bytes = new byte[32];
        
        int index = 0;
        index = ByteManipulator.addInt(bytes, mStatus.hasReceivedTimeSync?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.timestamp?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.isRecording?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.frameRate?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.numFramesInQueue?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.numFramesDropped?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.isAdeptPresent?1:0, index, false);
        index = ByteManipulator.addInt(bytes, mStatus.numWarpQueueUnderflows?1:0, index, false);
        out.write(bytes);        
    }
    
    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            mStatus = new RecorderStatus();
            super.readExternal(in);
            
            byte[] bytes = new byte[32];
            in.readFully(bytes);
            int index = 0;
            
            mStatus.hasReceivedTimeSync = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;            
            mStatus.timestamp = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;         
            mStatus.isRecording = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;
            mStatus.frameRate = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;
            mStatus.numFramesInQueue = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;
            mStatus.numFramesDropped = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;
            mStatus.isAdeptPresent = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;
            index += 4;
            mStatus.numWarpQueueUnderflows = (ByteManipulator.getInt(bytes, index, false) == 1)?true:false;                 
            index += 4;
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
}