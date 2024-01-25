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
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author kayjl1
 */
public class EtdDetectionBelief extends Belief implements BeliefExternalizable{
    
    public static final String BELIEF_NAME = "EtdDetectionBelief";
    
    private EtdDetection _etdDetection;
    
    public EtdDetectionBelief() {
        super("none");
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public EtdDetectionBelief(String agentID, Float concentration, AbsolutePosition pos, long timeMs) {
        super(agentID);
        
        _etdDetection = new EtdDetection(concentration, pos, timeMs);
        
        timestamp = new Date(timeMs);
    }
    
    public EtdDetection getEtdDetection() {
        return _etdDetection;
    }

    @Override
    public void addBelief(Belief b)
    {
        EtdDetectionBelief belief = (EtdDetectionBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
            _etdDetection = belief.getEtdDetection();
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        
        out.write(_etdDetection.writeExternal());
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        _etdDetection = new EtdDetection();
        _etdDetection.readExternal(in);
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

        EtdDetectionBelief belief = null;

        try
        {
            belief = (EtdDetectionBelief) clas.newInstance();
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
        return _etdDetection.getTime();
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