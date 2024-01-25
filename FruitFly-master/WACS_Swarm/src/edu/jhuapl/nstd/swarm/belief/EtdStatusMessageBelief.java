/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.TimeManagerFactory;
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
 * @author kayjl1
 */
public class EtdStatusMessageBelief extends Belief implements BeliefExternalizable{
    
    public static final String BELIEF_NAME = "EtdStatusMessageBelief";
    
    private long _time;
    private String _statusMessage;
    
    public EtdStatusMessageBelief() {
        super("none");
        _time = System.currentTimeMillis();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public EtdStatusMessageBelief(String agentID, long time, String statusMessage) {
        super(agentID);
        
        _time = time;
        _statusMessage = statusMessage;
        timestamp = new Date(time);
    }
    
    public String getStatusMessage() {
        return _statusMessage;
    }

    @Override
    public void addBelief(Belief b)
    {
        EtdStatusMessageBelief belief = (EtdStatusMessageBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
            _statusMessage = belief.getStatusMessage();
            _time = belief.getTime();
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        
        out.writeBytes(_statusMessage);
        out.writeBytes("\n" + Long.toString(_time));
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        _statusMessage = in.readLine();
        _time = Long.valueOf(in.readLine());
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

        EtdStatusMessageBelief belief = null;

        try
        {
            belief = (EtdStatusMessageBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    public long getTime()
    {
        return _time;
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