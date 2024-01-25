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
import java.util.LinkedList;

/**
 *
 * @author buckar1
 */
public class CloudPredictionBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "CloudProbabilityBelief";
    private Altitude m_cloudMinAltitudeMSL = null;
    private Altitude m_cloudMaxAltitudeMSL = null;
    private LatLonAltPosition m_predictedCloudCenterPositionMSL = null;
    private LatLonAltPosition m_predictedCloudInterceptPositionMSL = null;
    private LinkedList <LatLonAltPosition> m_predictedHighestLocations;
    private LinkedList <LatLonAltPosition> m_predictedRandomLocations;
    public static final int MAX_HIGHESTLOCATION_LIST_SIZE = 20;
    public static final int MAX_RANDOMLOCATION_LIST_SIZE = 20;

    public CloudPredictionBelief()
    {
        super();
    }

    public CloudPredictionBelief(String agentId,
                                 Altitude cloudMinAltitudeMSL,
                                 Altitude cloudMaxAltitudeMSL,
                                 LatLonAltPosition predictedCloudCenterPositionMSL,
                                 LatLonAltPosition predictedCloudInterceptPositionMSL,
                                 LinkedList <LatLonAltPosition> predictedHighestLocations,
                                 LinkedList <LatLonAltPosition> predictedRandomLocations)
    {
        super(agentId);

        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());

        m_cloudMinAltitudeMSL = cloudMinAltitudeMSL;
        m_cloudMaxAltitudeMSL = cloudMaxAltitudeMSL;
        m_predictedCloudCenterPositionMSL = predictedCloudCenterPositionMSL;
        m_predictedCloudInterceptPositionMSL = predictedCloudInterceptPositionMSL;
        m_predictedHighestLocations = predictedHighestLocations;
        m_predictedRandomLocations = predictedRandomLocations;
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

    @Override
    protected void addBelief(Belief b)
    {
        CloudPredictionBelief belief = (CloudPredictionBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this.m_cloudMinAltitudeMSL = belief.m_cloudMinAltitudeMSL;
            this.m_cloudMaxAltitudeMSL = belief.m_cloudMaxAltitudeMSL;
            this.m_predictedCloudCenterPositionMSL = belief.m_predictedCloudCenterPositionMSL;
            this.m_predictedCloudInterceptPositionMSL = belief.m_predictedCloudInterceptPositionMSL;
            this.m_predictedHighestLocations = belief.m_predictedHighestLocations;
            this.m_predictedRandomLocations = belief.m_predictedRandomLocations;
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        if (m_cloudMinAltitudeMSL != null)
        {
            out.writeDouble(m_cloudMinAltitudeMSL.getDoubleValue(Length.METERS));
        }
        else
        {
            out.writeDouble(0);
        }

        if (m_cloudMaxAltitudeMSL != null)
        {
            out.writeDouble(m_cloudMaxAltitudeMSL.getDoubleValue(Length.METERS));
        }
        else
        {
            out.writeDouble(0);
        }

        if (m_predictedCloudCenterPositionMSL != null)
        {
            out.writeDouble(m_predictedCloudCenterPositionMSL.getLatitude().getDoubleValue(Angle.DEGREES));
            out.writeDouble(m_predictedCloudCenterPositionMSL.getLongitude().getDoubleValue(Angle.DEGREES));
            out.writeDouble(m_predictedCloudCenterPositionMSL.getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            out.writeDouble(0);
            out.writeDouble(0);
            out.writeDouble(0);
        }

        if (m_predictedCloudInterceptPositionMSL != null)
        {
            out.writeDouble(m_predictedCloudInterceptPositionMSL.getLatitude().getDoubleValue(Angle.DEGREES));
            out.writeDouble(m_predictedCloudInterceptPositionMSL.getLongitude().getDoubleValue(Angle.DEGREES));
            out.writeDouble(m_predictedCloudInterceptPositionMSL.getAltitude().getDoubleValue(Length.METERS));
        }
        else
        {
            out.writeDouble(0);
            out.writeDouble(0);
            out.writeDouble(0);
        }

        if (m_predictedHighestLocations != null)
        {
            out.writeInt(m_predictedHighestLocations.size());
            for (LatLonAltPosition lla : m_predictedHighestLocations)
            {
                float latDecDeg = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
                float lonDecDeg = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
                out.writeFloat(latDecDeg);
                out.writeFloat(lonDecDeg);
            }
        }
        else
            out.writeInt(0);
        
        if (m_predictedRandomLocations != null)
        {
            out.writeInt(m_predictedRandomLocations.size());
            for (LatLonAltPosition lla : m_predictedRandomLocations)
            {
                float latDecDeg = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
                float lonDecDeg = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
                out.writeFloat(latDecDeg);
                out.writeFloat(lonDecDeg);
            }
        }
        else
            out.writeInt(0);
    }

    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            m_cloudMinAltitudeMSL = new Altitude(in.readDouble(), Length.METERS);
            m_cloudMaxAltitudeMSL = new Altitude(in.readDouble(), Length.METERS);

            double predictedCloudCenterPosLat_deg = in.readDouble();
            double predictedCloudCenterPosLon_deg = in.readDouble();
            double predictedCloudCenterPosAlt_m = in.readDouble();
            m_predictedCloudCenterPositionMSL = new LatLonAltPosition(new Latitude(predictedCloudCenterPosLat_deg, Angle.DEGREES),
                                                                      new Longitude(predictedCloudCenterPosLon_deg, Angle.DEGREES),
                                                                      new Altitude(predictedCloudCenterPosAlt_m, Length.METERS));

            double predictedCloudInterceptPosLat_deg = in.readDouble();
            double predictedCloudInterceptPosLon_deg = in.readDouble();
            double predictedCloudInterceptPosAlt_m = in.readDouble();
            m_predictedCloudInterceptPositionMSL = new LatLonAltPosition(new Latitude(predictedCloudInterceptPosLat_deg, Angle.DEGREES),
                                                                         new Longitude(predictedCloudInterceptPosLon_deg, Angle.DEGREES),
                                                                         new Altitude(predictedCloudInterceptPosAlt_m, Length.METERS));

            int size = in.readInt();
            m_predictedHighestLocations = new LinkedList<LatLonAltPosition>();
            for (int i = 0; i < size; i ++)
            {
                float latDecDeg = in.readFloat();
                float lonDecDeg = in.readFloat();
                
                LatLonAltPosition lla = new LatLonAltPosition (new Latitude (latDecDeg, Angle.DEGREES),
                        new Longitude (lonDecDeg, Angle.DEGREES),
                        Altitude.ZERO);
                m_predictedHighestLocations.add(lla);
            }
            
            size = in.readInt();
            m_predictedRandomLocations = new LinkedList<LatLonAltPosition>();
            for (int i = 0; i < size; i ++)
            {
                float latDecDeg = in.readFloat();
                float lonDecDeg = in.readFloat();
                
                LatLonAltPosition lla = new LatLonAltPosition (new Latitude (latDecDeg, Angle.DEGREES),
                        new Longitude (lonDecDeg, Angle.DEGREES),
                        Altitude.ZERO);
                m_predictedRandomLocations.add(lla);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

        CloudPredictionBelief belief = null;

        try
        {
            belief = (CloudPredictionBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    public Altitude getCloudMinAltitudeMSL()
    {
        return m_cloudMinAltitudeMSL;
    }

    public Altitude getCloudMaxAltitudeMSL()
    {
        return m_cloudMaxAltitudeMSL;
    }

    public LatLonAltPosition getCloudPredictedCenterPositionMSL()
    {
        return m_predictedCloudCenterPositionMSL;
    }

    public LatLonAltPosition getPredictedCloudInterceptPositionMSL()
    {
        return m_predictedCloudInterceptPositionMSL;
    }

    public LinkedList<LatLonAltPosition> getPredictedHighestLocations()
    {
        return m_predictedHighestLocations;
    }
    
    public LinkedList<LatLonAltPosition> getPredictedRandomLocations()
    {
        return m_predictedRandomLocations;
    }
}
