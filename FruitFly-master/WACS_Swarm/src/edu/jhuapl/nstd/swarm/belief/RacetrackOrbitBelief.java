/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;


import edu.jhuapl.jlib.math.*;
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
public class RacetrackOrbitBelief extends Belief
{
    public static final String BELIEF_NAME = "RacetrackOrbitBelief";

    private Latitude m_Latitude1;
    private Longitude m_Longitude1;
    private Altitude m_FinalAltitudeMsl;
    private Altitude m_StandoffAltitudeMsl;
    //private Altitude m_MinimumSafeAltitude;
    private Length m_Radius;
    private boolean m_isClockwise;

    public RacetrackOrbitBelief()
    {
    }

    public RacetrackOrbitBelief(Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise)
    {
        this (lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise, new Date (System.currentTimeMillis()));
    }
    
    public RacetrackOrbitBelief(Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise, Date time)
    {
        super();
        timestamp = time;
        m_Latitude1 = lat1;
        m_Longitude1 = lon1;
        m_FinalAltitudeMsl = altMslFinal;
        m_StandoffAltitudeMsl = altMslStandoff; 
        m_Radius = radius;
        m_isClockwise = isClockwise;
        
    }

    @Override
    public void addBelief(Belief b)
    {
        RacetrackOrbitBelief belief = (RacetrackOrbitBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_Latitude1 = belief.m_Latitude1;
            this.m_Longitude1 = belief.m_Longitude1;
            this.m_FinalAltitudeMsl = belief.m_FinalAltitudeMsl;
            this.m_StandoffAltitudeMsl = belief.m_StandoffAltitudeMsl;
            //this.m_MinimumSafeAltitude = belief.m_MinimumSafeAltitude;
            this.m_Radius = belief.m_Radius;
            this.m_isClockwise = belief.m_isClockwise;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeDouble(m_Latitude1.getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_Longitude1.getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_FinalAltitudeMsl.getDoubleValue(Length.METERS));
        out.writeDouble(m_StandoffAltitudeMsl.getDoubleValue(Length.METERS));
        out.writeDouble(m_Radius.getDoubleValue(Length.METERS));
        out.writeBoolean(m_isClockwise);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_Latitude1 = new Latitude(in.readDouble(), Angle.DEGREES);
        m_Longitude1 = new Longitude(in.readDouble(), Angle.DEGREES);
        m_FinalAltitudeMsl = new Altitude(in.readDouble(), Length.METERS);
        m_StandoffAltitudeMsl = new Altitude(in.readDouble(), Length.METERS);
        m_Radius = new Length(in.readDouble(), Length.METERS);
        m_isClockwise = in.readBoolean();
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

        RacetrackOrbitBelief belief = null;

        try
        {
            belief = (RacetrackOrbitBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }


    public Latitude getLatitude1 ()
    {
        return m_Latitude1;
    }

    public Longitude getLongitude1 ()
    {
        return m_Longitude1;
    }

    public Altitude getFinalAltitudeMsl ()
    {
        return m_FinalAltitudeMsl;
    }

    public Altitude getStandoffAltitudeMsl ()
    {
        return m_StandoffAltitudeMsl;
    }

    public Length getRadius()
    {
        return m_Radius;
    }

    public boolean getIsClockwise()
    {
        return m_isClockwise;
    }

    public void setRadius(Length radius)
    {
        m_Radius = radius;
    }

    public void setIsClockwise(boolean isClockwise)
    {
        m_isClockwise = isClockwise;
    }

    public void setPosition1 (Latitude lat1, Longitude lon1)
    {
        m_Latitude1 = lat1;
        m_Longitude1 = lon1;
    }

    public void setFinalAltitudeMsl (Altitude altMsl)
    {
        m_FinalAltitudeMsl = altMsl;
    }
    public void setStandoffAltitudeMsl (Altitude altMsl)
    {
        m_StandoffAltitudeMsl = altMsl;
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
