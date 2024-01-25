package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.nstd.swarm.util.*;
import java.io.*;
import java.util.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.*;

/**
 *
 * @author buckar1
 */
public class CircularOrbitBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "CircularOrbitBelief";
    //Center Position is Lat, Lon and Altitude (MSL)
    private AbsolutePosition _centerPosition;
    private Length _radius;
    private boolean _isClockwise;

    public CircularOrbitBelief()
    {
        super();
    }

    public CircularOrbitBelief(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise)
    {
        this (agentId, centerPosition, radius, isClockwise, new Date (System.currentTimeMillis()));
    }
    
    public CircularOrbitBelief(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, Date time)
    {
        super(agentId);

        _centerPosition = centerPosition;
        _radius = radius;
        _isClockwise = isClockwise;
        timestamp = time;
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
        CircularOrbitBelief belief = (CircularOrbitBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            _radius = belief._radius;
            _centerPosition = belief._centerPosition;
            _isClockwise = belief._isClockwise;
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

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[33]);
        buffer.putDouble(_radius.getDoubleValue(Length.METERS));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
        buffer.putDouble(_centerPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS));
        buffer.put((byte)(_isClockwise ? 1 : 0));

        out.write(buffer.array());
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[33]);
            in.readFully(buffer.array());

            _radius = new Length(buffer.getDouble(), Length.METERS);
            double latitude_DEG = buffer.getDouble();
            double longitude_DEG = buffer.getDouble();
            double altitude_M = buffer.getDouble();

            _centerPosition = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                                                    new Longitude(longitude_DEG, Angle.DEGREES),
                                                    new Altitude(altitude_M, Length.METERS));

            _isClockwise = (buffer.get() == 0) ? false : true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

//     public byte[] serialize() throws IOException {
//		setTransmissionTime();
//		ByteArrayOutputStream baStream = new ByteArrayOutputStream();
//		DataOutputStream out = new DataOutputStream(baStream);
//
//		writeExternal(out);
//		out.flush();
//		out.close();
//		return baStream.toByteArray();
//	}
//
//	public static Belief deserialize(InputStream iStream, Class clas) throws IOException {
//		DataInputStream in = new DataInputStream(iStream);
//
//		Belief belief = null;
//
//		try {
//			belief = (Belief)clas.newInstance();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		belief.readExternal(in);
//
//		return belief;
//	}
//
//	public void writeExternal(DataOutput out) throws IOException {
//
//		super.writeExternal(out);
//
//		byte[] bytes = new byte[32];
//
//		int index = 0;
//		index = ByteManipulator.addDouble(bytes,_radius.getDoubleValue(Length.METERS),index,false);
//        index = ByteManipulator.addDouble(bytes,_centerPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),index,false);
//        index = ByteManipulator.addDouble(bytes,_centerPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),index,false);
//        index = ByteManipulator.addDouble(bytes,_centerPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS),index,false);
//		out.write(bytes);
//	}
//
//	public void readExternal(DataInput in) throws IOException {
//		try
//		{
//			super.readExternal(in);
//
//			byte[] bytes  = new byte[32];
//			in.readFully(bytes);
//			int index = 0;
//
//			setRadius(new Length(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
//			index += 8;
//            double lat = ByteManipulator.getDouble(bytes, index, false);
//			index += 8;
//            double lon = ByteManipulator.getDouble(bytes, index, false);
//            index += 8;
//            double alt = ByteManipulator.getDouble(bytes, index, false);
//			index += 8;
//
//            setPosition( new LatLonAltPosition(new Latitude(lat, Angle.DEGREES),
//                                               new Longitude(lon, Angle.DEGREES),
//                                               new Altitude(alt, Length.METERS)));
//
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
    public Length getRadius()
    {
        return _radius;
    }

    public void setRadius(Length radius)
    {
        _radius = radius;
    }

    public AbsolutePosition getPosition()
    {
        return _centerPosition;
    }

    public void setPosition(AbsolutePosition centerPosition)
    {
        _centerPosition = centerPosition;
    }

    public boolean getIsClockwise()
    {
        return _isClockwise;
    }

    public void setIsClockwise(final boolean isClockwise)
    {
        _isClockwise = isClockwise;
    }
}