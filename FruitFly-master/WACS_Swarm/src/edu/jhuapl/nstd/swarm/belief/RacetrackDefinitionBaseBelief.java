/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
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
public class RacetrackDefinitionBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "RacetrackDefinitionBaseBelief";

    private LatLonAltPosition m_StartPosition;
    
    public RacetrackDefinitionBaseBelief()
    {
    }

    public RacetrackDefinitionBaseBelief(LatLonAltPosition loiterPos)
    {
        this (loiterPos, System.currentTimeMillis());
    }
    
    public RacetrackDefinitionBaseBelief(LatLonAltPosition startPos, long beliefTimestampMs)
    {
        super();
        timestamp = new Date(beliefTimestampMs);
        m_StartPosition = startPos;
    }

    @Override
    public void addBelief(Belief b)
    {
        RacetrackDefinitionBaseBelief belief = (RacetrackDefinitionBaseBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_StartPosition = belief.m_StartPosition;
        }
    }
    
    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof RacetrackDefinitionBaseBelief))
        {
            return false;
        }
        
        return (Math.abs(m_StartPosition.getLatitude().getDoubleValue(Angle.DEGREES) - ((RacetrackDefinitionBaseBelief)(obj)).m_StartPosition.getLatitude().getDoubleValue(Angle.DEGREES)) < 0.0001 && 
                Math.abs(m_StartPosition.getLongitude().getDoubleValue(Angle.DEGREES) - ((RacetrackDefinitionBaseBelief)(obj)).m_StartPosition.getLongitude().getDoubleValue(Angle.DEGREES)) < 0.0001 && 
                Math.abs(m_StartPosition.getAltitude().getDoubleValue(Length.FEET) - ((RacetrackDefinitionBaseBelief)(obj)).m_StartPosition.getAltitude().getDoubleValue(Length.FEET)) < 1.5
                );
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeDouble(m_StartPosition.getLatitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_StartPosition.getLongitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_StartPosition.getAltitude().getDoubleValue(Length.METERS));
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        Latitude latitude = new Latitude(in.readDouble(), Angle.DEGREES);
        Longitude longitude = new Longitude(in.readDouble(), Angle.DEGREES);
        Altitude altitude = new Altitude(in.readDouble(), Length.METERS);
        m_StartPosition = new LatLonAltPosition(latitude, longitude, altitude);
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

        RacetrackDefinitionBaseBelief belief = null;

        try
        {
            belief = (RacetrackDefinitionBaseBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }


    public LatLonAltPosition getStartPosition()
    {
        return m_StartPosition;
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
