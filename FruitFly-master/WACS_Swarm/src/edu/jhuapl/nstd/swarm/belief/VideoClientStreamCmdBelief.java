/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

/**
 *
 * @author xud1
 */
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class VideoClientStreamCmdBelief extends Belief
{
    public static final String BELIEF_NAME = "VideoClientStreamCmdBelief";
    private String mClientHost = "";
    private short mClientPort = 0;
    private boolean mStreamCmd;

    public VideoClientStreamCmdBelief()
    {
        super();
    }
    
    public VideoClientStreamCmdBelief(String agentID, String lClientIp, short lClientPort, boolean lStream)
    {
        this(agentID, lClientIp, lClientPort, lStream, new Date (System.currentTimeMillis()));
    }
    
    public VideoClientStreamCmdBelief(String agentID, String lClientIp, short lClientPort, boolean lStream, Date lTimestamp)
    {
        super(agentID);
        mClientHost = lClientIp;
        mClientPort = lClientPort;
        timestamp = lTimestamp;
        mStreamCmd = lStream;
    }
    
    @Override
    public void addBelief(Belief b)
    {
        VideoClientStreamCmdBelief belief = (VideoClientStreamCmdBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp)>0)
        {
            this.timestamp = belief.getTimeStamp();
            this.mClientHost = belief.getClientIp();
            this.mClientPort = belief.getClientPort();
            this.mStreamCmd = belief.getStreamCmd();
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
            out.writeUTF(mClientHost);
            out.writeShort(mClientPort);
            out.writeBoolean(mStreamCmd);
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
            mClientHost = in.readUTF();
            mClientPort = in.readShort();
            mStreamCmd = in.readBoolean();
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
    
    public String getClientIp()
    {
        return mClientHost;
    }
    
    public int[] getClientIpAsIntegerArray()
    {
        int[] ip = new int[4];
        String[] parts = mClientHost.split("\\.");
        
        for (int i = 0; i < 4; i ++)
        {
            ip[i] = Integer.parseInt(parts[i]);
        }        
        return ip;
    }
    
    public short getClientPort()
    {
        return mClientPort;
    }                
    
    public boolean getStreamCmd()
    {
        return mStreamCmd;
    }
}
