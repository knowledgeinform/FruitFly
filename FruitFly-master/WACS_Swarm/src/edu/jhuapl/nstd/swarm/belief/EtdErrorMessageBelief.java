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
public class EtdErrorMessageBelief extends Belief implements BeliefExternalizable{
    
    public static final String BELIEF_NAME = "EtdErrorMessageBelief";
    
    private long _time;
    private String _errorMessage;
    
    public EtdErrorMessageBelief() {
        super("none");
        _time = System.currentTimeMillis();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public EtdErrorMessageBelief(String agentID, long time, String errorMessage) {
        super(agentID);
        
        _time = time;
        _errorMessage = errorMessage;
        timestamp = new Date(time);
    }
    
    public String getErrorMessage() {
        return _errorMessage;
    }

    @Override
    public void addBelief(Belief b)
    {
        EtdErrorMessageBelief belief = (EtdErrorMessageBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
            _errorMessage = belief.getErrorMessage();
            _time = belief.getTime();
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        
        out.writeBytes(_errorMessage);
        out.writeBytes("\n" + Long.toString(_time));
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        _errorMessage = in.readLine();
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

        EtdErrorMessageBelief belief = null;

        try
        {
            belief = (EtdErrorMessageBelief) clas.newInstance();
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