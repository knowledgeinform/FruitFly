/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.util.PolygonRegion;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 * biggimh1
 */
public class KeepOutRegionBelief extends Belief implements BeliefExternalizable{

    private ArrayList<PolygonRegion> regions;
    public static final String BELIEF_NAME = "KeepOutRegions";
    
    public KeepOutRegionBelief(ArrayList<PolygonRegion> regions){
        this.regions = regions;
    }
    
    public KeepOutRegionBelief(){
        super();
    }
    
    public ArrayList<PolygonRegion> getRegions(){
        return this.regions;
    }
    
    @Override
    protected void addBelief(Belief b) {
        KeepOutRegionBelief belief = (KeepOutRegionBelief)b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            regions = belief.regions;
        }
        
        
    }

    @Override
    public String getName() {
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

    /*
     * Not up to Date
     */
    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        Belief belief;

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

    /*
     * Missing field that do not overlap with COB
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        int size = 4;
        for(PolygonRegion p: this.regions){
            size+= p.serializeSize();
        }
        
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[size]);

        buffer.putInt(regions.size());
        for(PolygonRegion p: this.regions){
            
            buffer.put(p.serialize());
        }

        out.write(buffer.array());
    }
    
    /*
     * Missing field that do not overlap with COB
     */
    @Override
    public void readExternal(DataInput in) throws IOException
    {
        try
        {
            super.readExternal(in);

            int size = in.readInt();
            
            //int size = buffer.getInt();
            if(regions == null)regions = new ArrayList<PolygonRegion>();
            while(size >0){
                size --;
                
                int length = in.readInt();
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(new byte[length*24]);
                in.readFully(buffer.array(), 0, length*24);
                LatLonAltPosition[] points = new LatLonAltPosition[length];
                
                
                while(length>0){
                    length--;
                    points[length] = readLatLonAlt(buffer);
                }
                regions.add(new PolygonRegion(points));
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private LatLonAltPosition readLatLonAlt(java.nio.ByteBuffer buffer) {
        double latitude_DEG = buffer.getDouble();
        double longitude_DEG = buffer.getDouble();
        double altitude_M = buffer.getDouble();
        LatLonAltPosition p = new LatLonAltPosition(new Latitude(latitude_DEG, Angle.DEGREES),
                new Longitude(longitude_DEG, Angle.DEGREES),
                new Altitude(altitude_M, Length.METERS));
        return p;
    }
    
    public static void main(String[] args){
        beliefSantiyCheck();
    }
    
    public static void beliefSantiyCheck(){
    
    String name = System.getProperty("agent.name");
        try {
            BeliefManagerImpl belMgr = new BeliefManagerImpl(name);
            //BeliefManagerClient client = new BeliefManagerClient(belMgr);
            Latitude lat = new Latitude(5, Angle.DEGREES);
            Longitude lon = new Longitude(5, Angle.DEGREES);
            Altitude alt = new Altitude(1000, Length.METERS);
            
            Latitude lat1 = new Latitude(2, Angle.DEGREES);
            Longitude lon1 = new Longitude(0,Angle.DEGREES);
            Altitude alt1 = new Altitude(1000, Length.METERS);
            
            Latitude lat2 = new Latitude(0,Angle.DEGREES);
            Longitude lon2 = new Longitude(2, Angle.DEGREES);
            Altitude alt2 = new Altitude(1000, Length.METERS);
            
            ArrayList<LatLonAltPosition> pList = new ArrayList<LatLonAltPosition>();
            
            pList.add(new LatLonAltPosition(lat, lon,alt));
            pList.add(new LatLonAltPosition(lat1, lon1,alt1));
            pList.add(new LatLonAltPosition(lat2, lon2,alt2));
            
            PolygonRegion p = new PolygonRegion(pList.toArray(new LatLonAltPosition[pList.size()]));

            ArrayList<PolygonRegion> regions = new ArrayList<PolygonRegion>();
            
            regions.add(p);

            KeepOutRegionBelief testBelief1 = new KeepOutRegionBelief(regions);
            belMgr.put(testBelief1);

            Thread.sleep(10000);
            
            KeepOutRegionBelief belief = (KeepOutRegionBelief)belMgr.get(KeepOutRegionBelief.BELIEF_NAME);
            
            LatLonAltPosition[] points = (belief.getRegions().get(0).getPoints());
            
            for(LatLonAltPosition pos: points){
                System.out.println(pos);
            }
            

        } catch (IOException ex) {
            Logger.getLogger(TestCircularOrbitBelief.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
        }
    }

    public void updateTimeStamp() {
        timestamp = new Date(System.currentTimeMillis());
    }
    
}
