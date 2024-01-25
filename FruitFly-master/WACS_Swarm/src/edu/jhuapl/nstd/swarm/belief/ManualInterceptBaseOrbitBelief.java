/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
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
public class ManualInterceptBaseOrbitBelief extends Belief
{
    public static final String BELIEF_NAME = "ManualInterceptBaseOrbitBelief";
    
    private Latitude m_InterceptLatitude;
    private Longitude m_InterceptLongitude;
    private boolean m_HoldPosition;
    private boolean m_ReleaseImmediately;
    

    public ManualInterceptBaseOrbitBelief ()
    {
        super ();
    }
    
    public ManualInterceptBaseOrbitBelief (String agentName, Latitude interceptLatitude, Longitude interceptLongitude, boolean forceHoldPosition)
    {
        super(agentName);
        timestamp = new Date();
        m_InterceptLatitude = interceptLatitude;
        m_InterceptLongitude = interceptLongitude;
        m_HoldPosition = forceHoldPosition;
        m_ReleaseImmediately = false;
    }
    
    public ManualInterceptBaseOrbitBelief (String agentName, boolean releaseImmediately)
    {
        super(agentName);
        timestamp = new Date();
        m_InterceptLatitude = Latitude.ZERO;
        m_InterceptLongitude = Longitude.ZERO;
        m_HoldPosition = false;
        m_ReleaseImmediately = releaseImmediately;
    }
    
    
    /**
     * Retuns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
    
    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof ManualInterceptBaseOrbitBelief))
        {
            return false;
        }
        
        ManualInterceptBaseOrbitBelief other = (ManualInterceptBaseOrbitBelief)obj;
        
        return (getInterceptLatitude().equals(other.getInterceptLatitude()) && 
                getInterceptLongitude().equals(other.getInterceptLongitude()) && 
                getForceHoldPosition() == other.getForceHoldPosition() && 
                getReleaseImmediately() == other.getReleaseImmediately()
                );
    }

    @Override
    protected void addBelief(Belief b)
    {
        ManualInterceptBaseOrbitBelief belief = (ManualInterceptBaseOrbitBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();

            this.m_InterceptLatitude = belief.m_InterceptLatitude;
            this.m_InterceptLongitude = belief.m_InterceptLongitude;
            this.m_HoldPosition = belief.m_HoldPosition;
            this.m_ReleaseImmediately = belief.m_ReleaseImmediately;
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
            belief = (Belief) clas.newInstance();
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

        out.writeDouble(m_InterceptLatitude.getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_InterceptLongitude.getDoubleValue(Angle.DEGREES));
        out.writeBoolean(m_HoldPosition);
        out.writeBoolean(m_ReleaseImmediately);
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            m_InterceptLatitude = new Latitude (in.readDouble(), Angle.DEGREES);
            m_InterceptLongitude = new Longitude (in.readDouble(), Angle.DEGREES);
            m_HoldPosition = in.readBoolean();
            m_ReleaseImmediately = in.readBoolean();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Latitude getInterceptLatitude()
    {
        return m_InterceptLatitude;
    }
    
    public Longitude getInterceptLongitude()
    {
        return m_InterceptLongitude;
    }

    public boolean getForceHoldPosition()
    {
        return m_HoldPosition;
    }
    
    public boolean getReleaseImmediately()
    {
        return m_ReleaseImmediately;
    }
}
