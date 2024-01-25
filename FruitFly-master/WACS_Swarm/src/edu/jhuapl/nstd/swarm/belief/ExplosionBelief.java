package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class ExplosionBelief  extends Belief
{
    public static final String BELIEF_NAME = "ExplosionBelief";

    private long m_time_ms;
    private AbsolutePosition m_location;

    public ExplosionBelief()
    {
    }

    public ExplosionBelief(AbsolutePosition location, long time_ms)
    {
        super();
        timestamp = new Date(System.currentTimeMillis());
        m_location = location;
        m_time_ms = time_ms;
    }

    @Override
    public void addBelief(Belief b)
    {
        ExplosionBelief belief = (ExplosionBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_location = belief.m_location;
            this.m_time_ms = belief.m_time_ms;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeLong(m_time_ms);
        LatLonAltPosition location = m_location.asLatLonAltPosition();
        out.writeDouble(location.getLatitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(location.getLongitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(location.getAltitude().getDoubleValue(Length.METERS));
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_time_ms = in.readLong();
        Latitude latitude = new Latitude(in.readDouble(), Angle.DEGREES);
        Longitude longitude = new Longitude(in.readDouble(), Angle.DEGREES);
        Altitude altitude = new Altitude(in.readDouble(), Length.METERS);
        m_location = new LatLonAltPosition(latitude, longitude, altitude);
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

        ExplosionBelief belief = null;

        try
        {
            belief = (ExplosionBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }


    public AbsolutePosition getLocation()
    {
        return m_location;
    }


    public long getTime_ms()
    {
        return m_time_ms;
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
