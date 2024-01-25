package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class SafetyBoxBelief extends Belief
{
    public static final String BELIEF_NAME = "SafetyBoxBelief";

    private String publishingAgentName = null;
    private double maxRadius_m;
    private double minRadius_m;
    private double minAltitude_m;
    private double maxAltitude_m;
    private boolean minAlt_IsAGL;  //If false, then its MSL
    private boolean maxAlt_IsAGL;  //If false, then its MSL
    private double latitude1_deg;
    private double longitude1_deg;
    private double latitude2_deg;
    private double longitude2_deg;
    private boolean isPermanent;

    public SafetyBoxBelief()
    {
        timestamp = new Date(0);
    }

    public SafetyBoxBelief(String publishingAgentName,
                           double latitude1_deg,
                           double longitude1_deg,
                           double latitude2_deg,
                           double longitude2_deg,
                           double maxAltitude_m,
                           boolean maxAlt_IsAGL,
                           double minAltitude_m,
                           boolean minAlt_IsAGL,
                           double maxRadius_m,
                           double minRadius_m,
                           boolean isPermanent)
    {
        this (publishingAgentName, latitude1_deg, longitude1_deg, latitude2_deg, longitude2_deg, maxAltitude_m, maxAlt_IsAGL, minAltitude_m, minAlt_IsAGL, maxRadius_m, minRadius_m, isPermanent, new Date (System.currentTimeMillis()));
    }
    
    public SafetyBoxBelief(String publishingAgentName,
                           double latitude1_deg,
                           double longitude1_deg,
                           double latitude2_deg,
                           double longitude2_deg,
                           double maxAltitude_m,
                           boolean maxAlt_IsAGL,
                           double minAltitude_m,
                           boolean minAlt_IsAGL,
                           double maxRadius_m,
                           double minRadius_m,
                           boolean isPermanent,
                           Date timestamp)
    {
        this.timestamp = timestamp;
        this.publishingAgentName = publishingAgentName;
        this.maxAltitude_m = maxAltitude_m;
        this.maxAlt_IsAGL = maxAlt_IsAGL;
        this.maxRadius_m = maxRadius_m;
        this.minRadius_m = minRadius_m;
        this.minAltitude_m = minAltitude_m;
        this.minAlt_IsAGL = minAlt_IsAGL;
        this.latitude1_deg = latitude1_deg;
        this.longitude1_deg = longitude1_deg;
        this.latitude2_deg = latitude2_deg;
        this.longitude2_deg = longitude2_deg;
        this.isPermanent = isPermanent;
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    public String toLogMessage ()
    {
        return timestamp + ", " +
        publishingAgentName + ", " +
        maxAltitude_m + " " + (maxAlt_IsAGL?"AGL":"MSL") + ", " + 
        minAltitude_m + " " + (minAlt_IsAGL?"AGL":"MSL") + ", " +
        maxRadius_m + ", " +
        minRadius_m + ", " +
        latitude1_deg + ", " +
        longitude1_deg + ", " +
        latitude2_deg + ", " +
        longitude2_deg + ", " +
        isPermanent;
    }

    @Override
    public synchronized void addBelief(Belief b)
    {
        if (b.getTimeStamp().compareTo(timestamp) > 0)
        {
            SafetyBoxBelief toCopy = (SafetyBoxBelief)b;
            timestamp = toCopy.timestamp;
            publishingAgentName = toCopy.publishingAgentName;
            maxAltitude_m = toCopy.maxAltitude_m;
            maxAlt_IsAGL = toCopy.maxAlt_IsAGL;
            maxRadius_m = toCopy.maxRadius_m;
            minRadius_m = toCopy.minRadius_m;
            minAltitude_m = toCopy.minAltitude_m;
            minAlt_IsAGL = toCopy.minAlt_IsAGL;
            latitude1_deg = toCopy.latitude1_deg;
            longitude1_deg = toCopy.longitude1_deg;
            latitude2_deg = toCopy.latitude2_deg;
            longitude2_deg = toCopy.longitude2_deg;
            isPermanent = toCopy.isPermanent;
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
        try
        {
            super.writeExternal(out);

            out.writeLong(timestamp.getTime());
            out.writeUTF(publishingAgentName);
            out.writeDouble(maxAltitude_m);
            out.writeBoolean(maxAlt_IsAGL);
            out.writeDouble(maxRadius_m);
            out.writeDouble(minRadius_m);
            out.writeDouble(minAltitude_m);
            out.writeBoolean(minAlt_IsAGL);
            out.writeDouble(latitude1_deg);
            out.writeDouble(longitude1_deg);
            out.writeDouble(latitude2_deg);
            out.writeDouble(longitude2_deg);
            out.writeBoolean(isPermanent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            timestamp = new Date(in.readLong());
            publishingAgentName = in.readUTF();
            maxAltitude_m = in.readDouble();
            maxAlt_IsAGL = in.readBoolean();
            maxRadius_m = in.readDouble();
            minRadius_m = in.readDouble();
            minAltitude_m = in.readDouble();
            minAlt_IsAGL = in.readBoolean();
            latitude1_deg = in.readDouble();
            longitude1_deg = in.readDouble();
            latitude2_deg = in.readDouble();
            longitude2_deg = in.readDouble();
            isPermanent = in.readBoolean();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public double getLatitude1_deg()
    {
        return latitude1_deg;
    }

    public double getLatitude2_deg()
    {
        return latitude2_deg;
    }

    public double getLongitude1_deg()
    {
        return longitude1_deg;
    }

    public double getLongitude2_deg()
    {
        return longitude2_deg;
    }

    public double getMaxAltitude_m()
    {
        return maxAltitude_m;
    }

    public boolean getMaxAlt_IsAGL()
    {
        return maxAlt_IsAGL;
    }

    public double getMaxRadius_m()
    {
        return maxRadius_m;
    }

    public double getMinAltitude_m()
    {
        return minAltitude_m;
    }

    public boolean getMinAlt_IsAGL()
    {
        return minAlt_IsAGL;
    }

    public double getMinRadius_m()
    {
        return minRadius_m;
    }

    public String getPublishingAgentName()
    {
        return publishingAgentName;
    }

    public boolean getIsPermanent()
    {
        return isPermanent;
    }

    public void setLatitude1_deg(double latitude1)
    {
        timestamp = new Date();
        this.latitude1_deg = latitude1;
    }

    public void setLatitude2_deg(double latitude2)
    {
        timestamp = new Date();
        this.latitude2_deg = latitude2;
    }

    public void setLongitude1_deg(double longitude1)
    {
        timestamp = new Date();
        this.longitude1_deg = longitude1;
    }

    public void setLongitude2_deg(double longitude2)
    {
        timestamp = new Date();
        this.longitude2_deg = longitude2;
    }

    public void setMaxAltitude_m(double maxAltitudeAGL_m, boolean isAGL)
    {
        timestamp = new Date();
        this.maxAltitude_m = maxAltitudeAGL_m;
        this.maxAlt_IsAGL = isAGL;
    }

    public void setMaxRadius_m(double maxRadius_m)
    {
        timestamp = new Date();
        this.maxRadius_m = maxRadius_m;
    }

    public void setMinAltitude_m(double minAltitudeAGL_m, boolean isAGL)
    {
        timestamp = new Date();
        this.minAltitude_m = minAltitudeAGL_m;
        this.minAlt_IsAGL = isAGL;
    }

    public void setMinRadius_m(double minRadius_m)
    {
        timestamp = new Date();
        this.minRadius_m = minRadius_m;
    }

    public void setPublishingAgentName(String publishingAgentName)
    {
        timestamp = new Date();
        this.publishingAgentName = publishingAgentName;
    }

    public void setIsPermanent(boolean isPermanent)
    {
        timestamp = new Date();
        this.isPermanent = isPermanent;
    }

    public boolean positionWithinSafetyBox(LatLonAltPosition lla) 
    {
        double currLatDecDeg = lla.getLatitude().getDoubleValue(Angle.DEGREES);
        double currLonDecDeg = lla.getLongitude().getDoubleValue(Angle.DEGREES);
        double currAltMslM = lla.getAltitude().getDoubleValue(Length.METERS);
        double currAltAglM = currAltMslM - DtedGlobalMap.getDted().getAltitudeMSL(currLatDecDeg, currLonDecDeg);
        
        if (currLatDecDeg < latitude1_deg && currLatDecDeg < latitude2_deg)
            return false;
        if (currLatDecDeg > latitude1_deg && currLatDecDeg > latitude2_deg)
            return false;
        if (currLonDecDeg < longitude1_deg && currLonDecDeg < longitude2_deg)
            return false;
        if (currLonDecDeg > longitude1_deg && currLonDecDeg > longitude2_deg)
            return false;
        
        if (minAlt_IsAGL)
        {
            if (currAltAglM < minAltitude_m)
                return false;
        }
        else
        {
            if (currAltMslM < minAltitude_m)
                return false;
        }
        if (maxAlt_IsAGL)
        {
            if (currAltAglM > maxAltitude_m)
                return false;
        }
        else
        {
            if (currAltMslM > maxAltitude_m)
                return false;
        }
        
        return true;
    }
}
