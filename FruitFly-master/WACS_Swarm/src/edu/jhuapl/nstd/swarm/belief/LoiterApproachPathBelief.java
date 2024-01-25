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
 * @author humphjc1
 */
public class LoiterApproachPathBelief  extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "LoiterApproachPathBelief";

    private LatLonAltPosition m_SafePosition = null;
    private LatLonAltPosition m_ContactPosition = null;
    private LatLonAltPosition m_FirstRangePosition = null;
    private boolean m_IsPathValid = false;

    public LoiterApproachPathBelief()
    {
        super();
    }

    public LoiterApproachPathBelief(LatLonAltPosition safePosition, LatLonAltPosition contactPosition, LatLonAltPosition firstRangePosition, boolean isPathValid)
    {
        super();

        if (safePosition != null)
            m_SafePosition = safePosition.duplicate();
        else
            m_SafePosition = null;
        
        if (contactPosition != null)
            m_ContactPosition = contactPosition.duplicate();
        else
            m_ContactPosition = null;

        if (firstRangePosition != null)
            m_FirstRangePosition = firstRangePosition.duplicate();
        else
            m_FirstRangePosition = null;

        m_IsPathValid = isPathValid;

        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
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
    protected void addBelief(Belief b)
    {
        LoiterApproachPathBelief belief = (LoiterApproachPathBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();

            this.m_SafePosition = belief.m_SafePosition;
            this.m_ContactPosition = belief.m_ContactPosition;
            this.m_FirstRangePosition = belief.m_FirstRangePosition;
            this.m_IsPathValid = belief.m_IsPathValid;
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

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[76]);

        if (m_SafePosition != null)
        {
            buffer.put((byte)1);
            buffer.putDouble(m_SafePosition.getLatitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_SafePosition.getLongitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_SafePosition.getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            buffer.put((byte)0);
            buffer.putDouble(0);
            buffer.putDouble(0);
            buffer.putDouble(0);
        }

        if (m_ContactPosition != null)
        {
            buffer.put((byte)1);
            buffer.putDouble(m_ContactPosition.getLatitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_ContactPosition.getLongitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_ContactPosition.getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            buffer.put((byte)0);
            buffer.putDouble(0);
            buffer.putDouble(0);
            buffer.putDouble(0);
        }

        if (m_FirstRangePosition != null)
        {
            buffer.put((byte)1);
            buffer.putDouble(m_FirstRangePosition.getLatitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_FirstRangePosition.getLongitude().getDoubleValue(Angle.DEGREES));
            buffer.putDouble(m_FirstRangePosition.getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            buffer.put((byte)0);
            buffer.putDouble(0);
            buffer.putDouble(0);
            buffer.putDouble(0);
        }

        buffer.put((byte)(m_IsPathValid?1:0));
        out.write(buffer.array());
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[76]);
            in.readFully(buffer.array());

            if (buffer.get() == (byte)1)
            {
                double latitude_DEG = buffer.getDouble();
                double longitude_DEG = buffer.getDouble();
                double altitude_M = buffer.getDouble();

                m_SafePosition = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                    new Longitude(longitude_DEG, Angle.DEGREES),
                                                    new Altitude(altitude_M, Length.METERS));
            }
            else
            {
                m_SafePosition = null;
                buffer.getDouble();
                buffer.getDouble();
                buffer.getDouble();
            }

            if (buffer.get() == (byte)1)
            {
                double latitude_DEG = buffer.getDouble();
                double longitude_DEG = buffer.getDouble();
                double altitude_M = buffer.getDouble();

                m_ContactPosition = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                    new Longitude(longitude_DEG, Angle.DEGREES),
                                                    new Altitude(altitude_M, Length.METERS));
            }
            else
            {
                m_ContactPosition = null;
                buffer.getDouble();
                buffer.getDouble();
                buffer.getDouble();
            }

            if (buffer.get() == (byte)1)
            {
                double latitude_DEG = buffer.getDouble();
                double longitude_DEG = buffer.getDouble();
                double altitude_M = buffer.getDouble();

                m_FirstRangePosition = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                    new Longitude(longitude_DEG, Angle.DEGREES),
                                                    new Altitude(altitude_M, Length.METERS));
            }
            else
            {
                m_FirstRangePosition = null;
                buffer.getDouble();
                buffer.getDouble();
                buffer.getDouble();
            }

            if (buffer.get() == (byte)1)
            {
                m_IsPathValid = true;
            }
            else
            {
                m_IsPathValid = false;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public LatLonAltPosition getSafePosition()
    {
        return m_SafePosition;
    }

    public LatLonAltPosition getContactPosition()
    {
        return m_ContactPosition;
    }

    public LatLonAltPosition getFirstRangePosition()
    {
        return m_FirstRangePosition;
    }

    public boolean getIsPathValid ()
    {
        return m_IsPathValid;
    }
}
