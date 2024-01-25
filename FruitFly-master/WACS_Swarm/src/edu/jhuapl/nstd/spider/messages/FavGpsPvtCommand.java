/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class FavGpsPvtCommand extends SpiderMessage {

    /**
     * 4.5.5 Message Type 16, FAV GPS PVT

    This message is sent to the SPIDER for logging in the data archive (as either a separate data record, or as part of a detection record).
    Message Length:		19 Bytes
    Message Repetition Rate:	1 per second
    Message Structure:
    4 Bytes, unsigned long = GPS Time of Week (LSB=0.001 Sec)
    4 Bytes, long = GPS Lat (LSB=1E-6 deg, range +90 to –90 deg, +=North Lat)
    4 Bytes, long = GPS Lon (LSB=1E-6 deg, range +180 to –180 deg, +=East Lon)
    2 Bytes, int = GPS Altitude (LSB=1 m)
    2 Bytes, unsigned int = Speed Over Ground (LSB=0.01 m/sec)
    2 Bytes, unsigned int = Track Over Ground (LSB=0.02 deg, range 0 to 359.98 deg)
    1 Byte, unsigned char = GPS State:
    4 Bits = GPS Mode:
    0 = Init Required
    1 = Initialized
    2 = Nav 3-D
    3 = Nav 2-D
    4 = Diff Nav 3-D
    5 = Diff Nav 2-D
    6 = Dead Reckoning
    4 Bits = Number of Satellites Currently Tracking
    Total Packet Length:		24 Bytes

    Note that GPS data will not be recorded during "Data Transmit" mode.

     */
    private long gpsTimeOfWeek;
    private int gpsLat;
    private int gpsLon;
    private int gpsAlt;
    private int speedOverGround;
    private int trackOverGround;
    private SpiderGpsModeEnum gpsMode;
    private int numberSatellitesTracking;

    public FavGpsPvtCommand() {
        super(SpiderMessageType.FAV_GPS_PVT, HeaderSize + ChecksumSize + 19);
    }

    @Override
    public byte[] toByteArray() {
        int idx = 0;
        idx += writeDataInt(idx, (int) gpsTimeOfWeek);
        idx += writeDataInt(idx, gpsLat);
        idx += writeDataInt(idx, gpsLon);
        idx += writeDataShort(idx, gpsAlt);
        idx += writeDataShort(idx, speedOverGround);
        idx += writeDataShort(idx, trackOverGround);
        writeDataNibbleLow(idx, gpsMode.ordinal());
        writeDataNibbleHigh(idx, numberSatellitesTracking);
        idx++;
        return super.toByteArray();
    }

    /**
     * @return the gpsTimeOfWeek
     */
    public long getGpsTimeOfWeek() {
        return gpsTimeOfWeek;
    }

    /**
     * @param gpsTimeOfWeek the gpsTimeOfWeek to set
     */
    public void setGpsTimeOfWeek(long gpsTimeOfWeek) {
        this.gpsTimeOfWeek = gpsTimeOfWeek;
    }

    /**
     * @return the gpsLat
     */
    public int getGpsLat() {
        return gpsLat;
    }

    /**
     * @param gpsLat the gpsLat to set
     */
    public void setGpsLat(int gpsLat) {
        this.gpsLat = gpsLat;
    }
        /**

   * @param gpsLat the gpsLat to set
     */
    public void setGpsLat(double gpsLat)
    {
        this.gpsLat = (int)(gpsLat*1E6);
    }

    /**
     * @return the gpsLon
     */
    public int getGpsLon() {
        return gpsLon;
    }

    /**
     * @param gpsLon the gpsLon to set
     */
    public void setGpsLon(int gpsLon) {
        this.gpsLon = gpsLon;
    }

    /**
     * @param gpsLon the gpsLon to set
     */
    public void setGpsLon(double gpsLon)
    {
        this.gpsLon = (int)(gpsLon*1E6);
    }

    /**
     * @return the gpsAlt
     */
    public int getGpsAlt() {
        return gpsAlt;
    }

    /**
     * @param gpsAlt the gpsAlt to set
     */
    public void setGpsAlt(int gpsAlt) {
        this.gpsAlt = gpsAlt;
    }

    /**
     * @return the speedOverGround
     */
    public int getSpeedOverGround() {
        return speedOverGround;
    }

    /**
     * @param speedOverGround the speedOverGround to set
     */
    public void setSpeedOverGround(int speedOverGround) {
        this.speedOverGround = speedOverGround;
    }

    /**
     * @return the trackOverGround
     */
    public int getTrackOverGround() {
        return trackOverGround;
    }

    /**
     * @param trackOverGround the trackOverGround to set
     */
    public void setTrackOverGround(int trackOverGround)
    {
        this.trackOverGround = trackOverGround;
    }

   /**
     * @param trackOverGround the trackOverGround to set
     */
    public void setTrackOverGround(double trackOverGround)
    {
        this.trackOverGround = (int)(trackOverGround/0.2);
    }



    /**
     * @return the gpsMode
     */
    public SpiderGpsModeEnum getGpsMode() {
        return gpsMode;
    }

    /**
     * @param gpsMode the gpsMode to set
     */
    public void setGpsMode(SpiderGpsModeEnum gpsMode) {
        this.gpsMode = gpsMode;
    }

    /**
     * @return the numberSatellitesTracking
     */
    public int getNumberSatellitesTracking() {
        return numberSatellitesTracking;
    }

    /**
     * @param numberSatellitesTracking the numberSatellitesTracking to set
     */
    public void setNumberSatellitesTracking(int numberSatellitesTracking) {
        this.numberSatellitesTracking = numberSatellitesTracking;
    }
}
