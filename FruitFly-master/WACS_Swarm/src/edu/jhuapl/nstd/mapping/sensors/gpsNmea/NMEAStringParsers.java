package edu.jhuapl.nstd.mapping.sensors.gpsNmea;

import edu.jhuapl.jlib.math.DateTime;
import edu.jhuapl.jlib.math.Time;
import edu.jhuapl.nstd.util.SystemTime;
import java.sql.Timestamp;
import java.util.TimeZone;


public class NMEAStringParsers 
{
  public NMEAStringParsers()
  {
  }

  public static double parseVHW(String s)
  {
    /* Structure is 
     *  $aaVHW,x.x,T,x.x,M,x.x,N,x.x,K*hh(CR)(LF)
     *         |     |     |     |
     *         |     |     |     Speed in km/h
     *         |     |     Speed in knots
     *         |     Heading in degrees, Magnetic
     *         Heading in degrees, True   
     */
    // We're interested only in Speed in knots.
    double speed = 0.0;
    String str = "";
    try
    {
      if (s.indexOf(",N,") > -1 && s.indexOf(",M,") > -1)
      {
        if (s.indexOf(",N,") > s.indexOf(",M,"))
          str = s.substring(s.indexOf(",M,") + ",M,".length(), s.indexOf(",N,"));        
      }
      speed = Double.parseDouble(str);
    }
    catch (Exception e)
    {
      System.err.println("For " + s + ", " + e.toString());
    e.printStackTrace();
    }
    return speed;
  }

  public static GeoPos parseGLL(String s)
  {
    /* Structure is 
     *  $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A*hh
     *         |       | |       | |         |
     *         |       | |       | |         A:data valid
     *         |       | |       | UTC of position
     *         |       | |       Long sign :E/W
     *         |       | Longitude
     *         |       Lat sign :N/S
     *         Latitude
     */
    String str = "";
    GeoPos ll = null;
    try
    {
      if (s.indexOf("A*") == -1) // Data invalid
        return ll;
      else
      {
        int i = s.indexOf(",");
        if (i > -1)
        {
          String lat = "";
          int j = s.indexOf(",", i+1);
          lat = s.substring(i+1, j);
          double l = Double.parseDouble(lat);
          int intL = (int)l/100;
          double m = ((l/100.0)-intL) * 100.0;
          m *= (100.0/60.0);
          l = intL + (m/100.0);
          String latSgn = s.substring(j+1, j+2);
          if (latSgn.equals("S"))
            l *= -1.0;
          int k = s.indexOf(",", j+3);
          String lng = s.substring(j+3, k);
          double g = Double.parseDouble(lng);
          int intG = (int)g/100;
          m = ((g/100.0)-intG) * 100.0;
          m *= (100.0/60.0);
          g = intG + (m/100.0);
          String lngSgn = s.substring(k+1, k+2);
          if (lngSgn.equals("W"))
            g *= -1.0;
          
          ll = new GeoPos(l, g);          
        }
      }
//    System.out.println(str);
    }
    catch (Exception e)
    {
      System.err.println("For " + s + ", " + e.toString());
    e.printStackTrace();
    }
    return ll;
  }

  public static int parseHDM(String s)
  {
    /* Structure is 
     *  $aaHDG,xxx,M*hh(CR)(LF)
     *         |   |   
     *         |   Magnetic, True
     *         Heading in degrees
     */
    int hdg = 0;
    String str = "";
    try
    {
      if (s.indexOf("HDM,") > -1)
      {
        str = s.substring(s.indexOf("HDM,") + "HDM,".length());
        if (str.indexOf(",") > -1)
        {
          str = str.substring(0, str.indexOf(","));
          hdg = Integer.parseInt(str);
        }
      }
    }
    catch (Exception e)
    {
      System.err.println("For " + s + ", " + e.toString());
    e.printStackTrace();
    }
    return hdg;
  }

  public static void parseRMC(String data, GpsReceiverInterfaceThread.GpsReading gpsReading)
  {
      String[] msgTokens = data.split (",");
      
      String gpsTimeStr = msgTokens[1];
      String gpsValidStr = msgTokens[2];
      String gpsLatStr = msgTokens[3];
      String gpsLatDirStr = msgTokens[4];
      String gpsLonStr = msgTokens[5];
      String gpsLonDirStr = msgTokens[6];
      String gpsDateStr = msgTokens[9];
      
      if (gpsValidStr == null || gpsValidStr.length() == 0)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          if (gpsValidStr.equals ("A"))
              gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.GPS_FIX;
          else
              gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
      }
      
      if (gpsTimeStr == null || gpsTimeStr.length() == 0 || gpsDateStr == null || gpsDateStr.length() == 0)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          int hour = Integer.parseInt(gpsTimeStr.substring(0, 2));
          int minute = Integer.parseInt(gpsTimeStr.substring(2, 4));
          float seconds = Float.parseFloat(gpsTimeStr.substring(4));
          
          int day = Integer.parseInt(gpsDateStr.substring(0, 2));
          int month = Integer.parseInt(gpsDateStr.substring(2, 4));
          int year = 2000 + Integer.parseInt(gpsDateStr.substring(4));
          
          DateTime dt = new DateTime (TimeZone.getTimeZone("UTC"), year, month, day, hour, minute, (int)Math.round(seconds));
          gpsReading.m_TimestampSec = (long)dt.getDoubleValue(Time.SECONDS);
          
          /*Timestamp ts = new Timestamp(year, month, day, hour, minute, (int)Math.round(seconds), 0);
          gpsReading.m_TimestampMs = ts.getTime();*/
      }
      
      if (gpsLatStr == null || gpsLatStr.length() == 0 || gpsLatDirStr == null || gpsLatDirStr.length() == 0 )
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          int latDeg = Integer.parseInt(gpsLatStr.substring(0,2));
          double latMinDec = Double.parseDouble(gpsLatStr.substring(2));
          
          double latDecDeg = latDeg + latMinDec/60;
          if (gpsLatDirStr.equals("S"))
              latDecDeg = -latDecDeg;
          
          gpsReading.m_LatDecDeg = latDecDeg;
      }
      
      if (gpsLonStr == null || gpsLonStr.length() == 0 || gpsLonDirStr == null || gpsLonDirStr.length() == 0 )
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          int lonDeg = Integer.parseInt(gpsLonStr.substring(0,3));
          double lonMinDec = Double.parseDouble(gpsLonStr.substring(3));
          
          double lonDecDeg = lonDeg + lonMinDec/60;
          if (gpsLonDirStr.equals("W"))
              lonDecDeg = -lonDecDeg;
          
          gpsReading.m_LonDecDeg = lonDecDeg;
      }
  }
  
  public static void parseGGA(String data, GpsReceiverInterfaceThread.GpsReading gpsReading)
  {
      String[] msgTokens = data.split ("[,*]");
      String gpsLatStr = msgTokens[2];
      String gpsLatDirStr = msgTokens[3];
      String gpsLonStr = msgTokens[4];
      String gpsLonDirStr = msgTokens[5];
      String gpsFixQuality = msgTokens[6];
      String gpsNumSatellites = msgTokens[7];
      String hdop = msgTokens[8];
      String altMSL = msgTokens[9];
      String altWGS84 = msgTokens[11];
      String timeSinceDGPS = msgTokens[13];
      String dgpsId = msgTokens[14];
      
      //We are choosing to ignore GPS system time and apply computer system time.  This way if EMAPS isn't sync'ed to GPS,
      //we'll still have pretty good data.  This is probably the best case scenario, short of doing it in C++.
      gpsReading.m_TimestampSec = System.currentTimeMillis()/1000;
      
      gpsReading.m_NumSatellites = Integer.parseInt(gpsNumSatellites);
      int fixQuality = Integer.parseInt(gpsFixQuality);
      if (fixQuality == 1)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.GPS_FIX;
      }
      else if (fixQuality == 2)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.DGPS_FIX;
      }
      else if (fixQuality == 4 || fixQuality == 5)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.RTK_FIX;
      }
      else //if (fixQuality == 0)
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      //gpsReading.m_HDOP = Double.parseDouble(hdop);  Drop HDOP on the floor for now
      gpsReading.m_AltitudeMSLMeters = Double.parseDouble(altMSL);
      gpsReading.m_GeoidHeightAboveWGS84Meters = Double.parseDouble(altWGS84);
      if (timeSinceDGPS != null && !timeSinceDGPS.isEmpty())
        gpsReading.m_TimeSinceDGPSSec = Double.parseDouble(timeSinceDGPS);
      if (dgpsId != null && !dgpsId.isEmpty())
        gpsReading.m_DGPSStationId = Integer.parseInt(dgpsId);
          
      if (gpsLatStr == null || gpsLatStr.length() == 0 || gpsLatDirStr == null || gpsLatDirStr.length() == 0 )
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          int latDeg = Integer.parseInt(gpsLatStr.substring(0,2));
          double latMinDec = Double.parseDouble(gpsLatStr.substring(2));
          
          double latDecDeg = latDeg + latMinDec/60;
          if (gpsLatDirStr.equals("S"))
              latDecDeg = -latDecDeg;
          
          gpsReading.m_LatDecDeg = latDecDeg;
      }
      
      if (gpsLonStr == null || gpsLonStr.length() == 0 || gpsLonDirStr == null || gpsLonDirStr.length() == 0 )
      {
          gpsReading.m_FixQuality = GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID;
          return;
      }
      else
      {
          int lonDeg = Integer.parseInt(gpsLonStr.substring(0,3));
          double lonMinDec = Double.parseDouble(gpsLonStr.substring(3));
          
          double lonDecDeg = lonDeg + lonMinDec/60;
          if (gpsLonDirStr.equals("W"))
              lonDecDeg = -lonDecDeg;
          
          gpsReading.m_LonDecDeg = lonDecDeg;
      }
  }

  public static final short DEPTH_IN_FEET    = 0;
  public static final short DEPTH_IN_METERS  = 1;
  public static final short DEPTH_IN_FATHOMS = 2;
  
  public static float parseDBT(String s, short unit)
  {
    /* Structure is 
     *  $aaDBT,011.0,f,03.3,M,01.8,F*18(CR)(LF)
     *         |     | |    | |    |
     *         |     | |    | |    F for fathoms
     *         |     | |    | Depth in fathoms
     *         |     | |    M for meters
     *         |     | Depth in meters
     *         |     f for feet
     *         Depth in feet   
     */
    float feet    = 0.0F;
    float meters  = 0.0F;
    float fathoms = 0.0F;
    String str = "";
    String first = "", last = "";
    try
    {
      first = "DBT,";
      last  = ",f,";
      if (s.indexOf(first) > -1 && s.indexOf(last) > -1)
      {
        if (s.indexOf(first) < s.indexOf(last))
          str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));        
      }
      feet = Float.parseFloat(str);
      first = ",f,";
      last  = ",M,";
      if (s.indexOf(first) > -1 && s.indexOf(last) > -1)
      {
        if (s.indexOf(first) < s.indexOf(last))
          str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));        
      }
      meters = Float.parseFloat(str);
      first = ",M,";
      last  = ",F";
      if (s.indexOf(first) > -1 && s.indexOf(last) > -1)
      {
        if (s.indexOf(first) < s.indexOf(last))
          str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));        
      }
      fathoms = Float.parseFloat(str);
    }
    catch (Exception e)
    {
      System.err.println("For " + s + ", " + e.toString());
    e.printStackTrace();
    }

    if (unit == DEPTH_IN_FEET)
      return feet;
    else if (unit == DEPTH_IN_METERS)
      return meters;
    else if (unit == DEPTH_IN_FATHOMS)
      return fathoms;
    else
      return feet;
  }
}