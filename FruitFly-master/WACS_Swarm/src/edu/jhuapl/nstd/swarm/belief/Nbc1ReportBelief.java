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
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
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
public class Nbc1ReportBelief extends Belief
{
    public static final String BELIEF_NAME = "Nbc1ReportBelief";

    String m_ReportNumber;
    LatLonAltPosition m_DetectionLocation;
    Date m_DetectionTime;
    String m_DetectionString;
    NavyAngle m_WindBearingFrom;
    Speed m_WindSpeed;
    
    
    
    public Nbc1ReportBelief()
    {
        super();
        timestamp = new Date(System.currentTimeMillis());
    }
    
    public Nbc1ReportBelief(Nbc1ReportBelief copyFrom)
    {
        super();
        timestamp = new Date (-1);
        addBelief (copyFrom);
    }
    
    public Nbc1ReportBelief(String reportNumber, LatLonAltPosition detectionLocation, Date detectionTime, String detectionString, NavyAngle windBearingFrom, Speed windSpeed)
    {
        super();
        timestamp = new Date(System.currentTimeMillis());
        
        m_ReportNumber = reportNumber;
        m_DetectionLocation = detectionLocation;
        m_DetectionTime = detectionTime;
        m_DetectionString = detectionString;
        m_WindBearingFrom = windBearingFrom;
        m_WindSpeed = windSpeed;
    }
    
    public void setWind (NavyAngle windBearingFrom, Speed windSpeed)
    {
        timestamp = new Date(System.currentTimeMillis());
        m_WindBearingFrom = windBearingFrom;
        if (m_WindBearingFrom == null)
            m_WindBearingFrom = NavyAngle.ZERO;
        m_WindSpeed = windSpeed;
        if (m_WindSpeed == null)
            m_WindSpeed = Speed.ZERO;
    }

    @Override
    public void addBelief(Belief b)
    {
        Nbc1ReportBelief belief = (Nbc1ReportBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_ReportNumber = belief.m_ReportNumber;
            this.m_DetectionLocation = belief.m_DetectionLocation;
            this.m_DetectionTime = belief.m_DetectionTime;
            this.m_DetectionString = belief.m_DetectionString;
            this.m_WindBearingFrom = belief.m_WindBearingFrom;
            this.m_WindSpeed = belief.m_WindSpeed;
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeUTF (m_ReportNumber);
        out.writeDouble(m_DetectionLocation.getLatitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_DetectionLocation.getLongitude().getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_DetectionLocation.getAltitude().getDoubleValue(Length.METERS));
        out.writeLong(m_DetectionTime.getTime());
        out.writeUTF(m_DetectionString);
        out.writeDouble(m_WindBearingFrom.getDoubleValue(Angle.DEGREES));
        out.writeDouble(m_WindSpeed.getDoubleValue(Speed.METERS_PER_SECOND));
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        m_ReportNumber = in.readUTF();
        Latitude lat = new Latitude (in.readDouble(), Angle.DEGREES);
        Longitude lon = new Longitude (in.readDouble(), Angle.DEGREES);
        Altitude alt = new Altitude (in.readDouble(), Length.METERS);
        m_DetectionLocation = new LatLonAltPosition(lat, lon, alt);
        m_DetectionTime = new Date (in.readLong());
        m_DetectionString = in.readUTF();
        m_WindBearingFrom = new NavyAngle (in.readDouble(), Angle.DEGREES);
        m_WindSpeed = new Speed (in.readDouble(), Speed.METERS_PER_SECOND);
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

        Nbc1ReportBelief belief = null;

        try
        {
            belief = (Nbc1ReportBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }
    
    public String getReportNumber ()
    {
        return m_ReportNumber;
    }
    
    public LatLonAltPosition getDetectionLocation ()
    {
        return m_DetectionLocation;
    }
    
    public Date getDetectionTime ()
    {
        return m_DetectionTime;
    }
    
    public String getDetectionString ()
    {
        return m_DetectionString;
    }
    
    public NavyAngle getWindBearingFrom ()
    {
        return m_WindBearingFrom;
    }
    
    public Speed getWindSpeed ()
    {
        return m_WindSpeed;
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
