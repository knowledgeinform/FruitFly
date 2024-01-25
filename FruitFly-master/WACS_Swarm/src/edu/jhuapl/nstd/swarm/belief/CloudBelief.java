//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
// File created: Tue Nov  9 13:10:34 1999
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.Ellipse;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;



/**
 * This is the class that stores information about which cells should be searched.
 *
 * <P>UNCLASSIFIED
 * @author Chad Hawthorne ext. 3728
 */
public class CloudBelief extends Belief implements BeliefExternalizable
{

    private AbsolutePosition[] m_foci = new LatLonAltPosition[2];
    private Length m_majorAxis;
    private Length m_height;
    private Altitude m_bottomAltitudeAGL;
    /**
     * The unique name of this belief type.
     */
    public static final String BELIEF_NAME = "CloudBelief";

    /**
     * Empty Constructor.
     */
    public CloudBelief()
    {
    }

    public CloudBelief(String agentID, Ellipse cloud)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        this.m_foci = cloud.getFoci();
        this.m_majorAxis = cloud.getMajorAxis();

        this.m_height = Length.ZERO;
        this.m_bottomAltitudeAGL = Altitude.ZERO;
    }

    /**
     * Constructor that creates a matrix using Agent.createCartesianMatrix. All values are initially set to 0.0.
     * @param agentID The id of the agent creating this matrix.
     */
    public CloudBelief(String agentID, Ellipse cloud, Length height, Altitude bottomAltitude)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        this.m_foci = cloud.getFoci();
        this.m_majorAxis = cloud.getMajorAxis();
        this.m_height = height;
        this.m_bottomAltitudeAGL = bottomAltitude;
    }

    /**
     * adds two beliefs together.
     * @param b The belief to add to this belief.
     */
    @Override
    public void addBelief(Belief b)
    {
        CloudBelief belief = (CloudBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {            
            this.timestamp = belief.getTimeStamp();
            this.m_foci = belief.getEllipse().getFoci();
            this.m_majorAxis = belief.getEllipse().getMajorAxis();
            this.m_height = belief.m_height;
            this.m_bottomAltitudeAGL = belief.m_bottomAltitudeAGL;
        }
    }

    public synchronized Ellipse getEllipse()
    {
        return new Ellipse(m_foci[0], m_foci[1], m_majorAxis);
    }

    public synchronized void setEllipse(Ellipse ellipse)
    {
        m_foci[0] = ellipse.getFoci()[0];
        m_foci[1] = ellipse.getFoci()[1];
        m_majorAxis = ellipse.getMajorAxis();
        timestamp = new Date(System.currentTimeMillis());
    }

    public synchronized Length getHeight()
    {
        return m_height;
    }

    public synchronized void setHeight(Length height)
    {
        this.m_height = height;
        timestamp = new Date(System.currentTimeMillis());
    }

    public synchronized Altitude getBottomAltitudeAGL()
    {
        return m_bottomAltitudeAGL;
    }

    public synchronized void setBottomAltitudeAGL(Altitude bottomAltitude)
    {
        this.m_bottomAltitudeAGL = bottomAltitude;
        timestamp = new Date(System.currentTimeMillis());
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

        CloudBelief belief = null;

        try
        {
            belief = (CloudBelief) clas.newInstance();
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
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        LatLonAltPosition foci1 = m_foci[0].asLatLonAltPosition();
        LatLonAltPosition foci2 = m_foci[1].asLatLonAltPosition();
        float foci1Lat = (float) foci1.getLatitude().getDoubleValue(Angle.DEGREES);
        float foci1Lon = (float) foci1.getLongitude().getDoubleValue(Angle.DEGREES);
        float foci2Lat = (float) foci2.getLatitude().getDoubleValue(Angle.DEGREES);
        float foci2Lon = (float) foci2.getLongitude().getDoubleValue(Angle.DEGREES);
        float axis = (float) m_majorAxis.getDoubleValue(Length.METERS);

        //5 * 4 bytes
        byte[] bytes = new byte[36];
        int index = 0;
        index = ByteManipulator.addFloat(bytes, foci1Lat, index, false);
        index = ByteManipulator.addFloat(bytes, foci1Lon, index, false);
        index = ByteManipulator.addFloat(bytes, foci2Lat, index, false);
        index = ByteManipulator.addFloat(bytes, foci2Lon, index, false);
        index = ByteManipulator.addFloat(bytes, axis, index, false);
        index = ByteManipulator.addDouble(bytes, m_height.getDoubleValue(Length.FEET), index, false);
        index = ByteManipulator.addDouble(bytes, m_bottomAltitudeAGL.getDoubleValue(Length.FEET), index, false);
        out.write(bytes);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        byte[] bytes = new byte[36];
        in.readFully(bytes);

        int index = 0;
        float foci1Lat = ByteManipulator.getFloat(bytes, index, false);
        index += 4;
        float foci1Lon = ByteManipulator.getFloat(bytes, index, false);
        index += 4;
        float foci2Lat = ByteManipulator.getFloat(bytes, index, false);
        index += 4;
        float foci2Lon = ByteManipulator.getFloat(bytes, index, false);
        index += 4;
        float axis = ByteManipulator.getFloat(bytes, index, false);
        index += 4;
        m_height = new Length(ByteManipulator.getDouble(bytes, index, false), Length.FEET);
        index += 8;
        m_bottomAltitudeAGL = new Altitude(ByteManipulator.getDouble(bytes, index, false), Length.FEET);



        m_foci[0] = new LatLonAltPosition(new Latitude(foci1Lat, Angle.DEGREES),
                                          new Longitude(foci1Lon, Angle.DEGREES),
                                          Altitude.ZERO);
        m_foci[1] = new LatLonAltPosition(new Latitude(foci2Lat, Angle.DEGREES),
                                          new Longitude(foci2Lon, Angle.DEGREES),
                                          Altitude.ZERO);
        m_majorAxis = new Length(axis, Length.METERS);
    }
}

//=============================== UNCLASSIFIED ==================================

