/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
 * @author kayjl1
 */
public class NmeaRawMessageBelief extends Belief implements BeliefExternalizable{
    
    public static final String BELIEF_NAME = "NmeaRawMessageBelief";
    
    private long _time;
    private String _nmeaRawMessage;
    private PositionTimeName _posTimeName;
    private double _lat;
    private double _lon;
    private double _altMSL;
    
    public NmeaRawMessageBelief() {
        super("none");
        _time = System.currentTimeMillis();
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    }
    
    public NmeaRawMessageBelief(String agentID, long time, String nmeaRawMessage, double lat, double lon, double altMSL) {
        super(agentID);
        
        _time = time;
        _nmeaRawMessage = nmeaRawMessage;
        timestamp = new Date(time);
        _lat = lat;
        _lon = lon;
        _altMSL = altMSL;
    }
    
    public String getNmeaRawMessage() {
        return _nmeaRawMessage;
    }

    @Override
    public void addBelief(Belief b)
    {
        NmeaRawMessageBelief belief = (NmeaRawMessageBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = b.getTimeStamp();
            _nmeaRawMessage = belief.getNmeaRawMessage();
            _time = belief.getTime();
            _lat = belief.getLat();
            _lon = belief.getLon();
            _altMSL = belief.getAltMSL();
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);
        
        out.writeBytes(_nmeaRawMessage);
        out.writeBytes("\n" + Long.toString(_time));
        out.writeBytes("\n" + Double.toString(_lat));
        out.writeBytes("\n" + Double.toString(_lon));
        out.writeBytes("\n" + Double.toString(_altMSL));
        
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);
        _nmeaRawMessage = in.readLine();
        _time = Long.valueOf(in.readLine());
        _lat = Double.valueOf(in.readLine());
        _lon = Double.valueOf(in.readLine());
        _altMSL = Double.valueOf(in.readLine());
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

        NmeaRawMessageBelief belief = null;

        try
        {
            belief = (NmeaRawMessageBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    public long getTime()
    {
        return _time;
    }
    
    public double getLat() {
        return _lat;
    }
    
    public double getLon() {
        return _lon;
    }
    
    public double getAltMSL() {
        return _altMSL;
    }
    
    public PositionTimeName getPositionTimeName() {
        return new PositionTimeName(new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES), new Longitude(_lon, Angle.DEGREES), new Altitude(_altMSL, Length.METERS)), new Date(_time), "");
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