/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class anacondaSetGpsCommand extends cbrnPodCommand
{
    private long time;   //Current time, in seconds (not millis)
    
    private double lat;  //gps lat
    
    private double lon;  //gps lon
    
    private int alt;  //gps alt
    
    public anacondaSetGpsCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_GPS, 15);
    }
    
    /**
     * @param rate New date/time for GPS, in seconds
     * 
     */
    public anacondaSetGpsCommand (long newTime, double newLat, double newLon, int newAlt)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_GPS, 15);
         setTimeSec(newTime);
         setLat (newLat);
         setLon (newLon);
         setAlt (newAlt);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataInt(1, (int)time);
        
        //convert lat to integer offset equivalent
        //int latConv = (int)(Math.floor((lat+90.0)/0.000001));
        int latConv = (int)(Math.floor((lat*1E6)));
        writeDataInt (5, latConv);
        //convert lon to integer offset equivalent
        //int lonConv = (int)(Math.floor((lon+180.0)/0.000001));
        int lonConv = (int)(Math.floor((lon*1E6)));
        writeDataInt (9, lonConv);
        writeDataShort (13, (short)alt);
        return super.toByteArray();
    }

    /**
     * @return the GPS Time
     */
    public long getTimeSec() {
        return time;
    }

    /**
     * @param newTime New GPS Time
     */
    public void setTimeSec(long newTime) {
        this.time = newTime;
    }
    
    /**
     * 
     * @param newLat GPS Latitude
     */
    public void setLat (double newLat)
    {
        lat = newLat;
    }
    
    /**
     * 
     * @return GPS Latitude
     */
    public double getLat ()
    {
        return lat;
    }
    
    /**
     * 
     * @param newLon GPS Longitude
     */
    public void setLon (double newLon)
    {
        lon = newLon;
    }
    
    /**
     * 
     * @return GPS Longitude
     */
    public double getLon ()
    {
        return lon;
    }
    
    /**
     * 
     * @param newAlt GPS Altitude
     */
    public void setAlt (int newAlt)
    {
        alt = newAlt;
    }
    
    /**
     * 
     * @return GPS Altitude
     */
    public int getAlt ()
    {
        return alt;
    }
}
