/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class to provide simple math utility functions
 *
 * @author humphjc1
 */
public class MathUtils
{
    static GeoidHeightLookup m_GeoidLookup = null;

    /**
     * Formatter with 4 decimal places
     */
    static private DecimalFormat m_DecFormat4 = new DecimalFormat ("#.####");

        /**
     * Formatter with 2 decimal places
     */
    static private DecimalFormat m_DecFormat2 = new DecimalFormat ("#.##");
    
    static private SimpleDateFormat m_MilDTCFormat = new SimpleDateFormat("ddHHmm'Z'MMMyy");


    /**
     * Convert latitude degrees to meters, assuming spherical earth
     * @return
     */
    public static double getLatDegreesToMeters ()
    {
        return MathConstants.DEG2M;
    }

    /**
     * Convert longitude degrees to meters, assuming spherical earth
     * @return
     */
    public static double getLonDegreesToMeters (double latDegrees)
    {
        return MathConstants.DEG2M * Math.cos(latDegrees * MathConstants.DEG2RAD);
    }

    /**
     * Calculate elevation angle between two locations.  -90 would mean 'to' position is directly below 'from position
     *
     * @param fromLatDecDeg Latitude to look from
     * @param fromLonDecDeg Longitude to look from
     * @param fromAltM Altitude to look from
     * @param toLatDecDeg Latitude to look to
     * @param toLonDecDeg Longitude to look to
     * @param toAltM Altitude to look to
     * @param useCurvedEarth If true, use curved earth calculations
     * @return Elevation angle from point to point
     */
    public static double calcElevationAngleDeg(double fromLatDecDeg, double fromLonDecDeg, double fromAltM, double toLatDecDeg, double toLonDecDeg, double toAltM, boolean useCurvedEarth)
    {
        double gcRangeM = calcGreatCircleDistance(fromLatDecDeg, fromLonDecDeg, toLatDecDeg, toLonDecDeg);

        double x;
        double z;
        if (!useCurvedEarth)
        {
            //Flat earth
            x = gcRangeM;
            z = (toAltM - fromAltM);
        }
        else
        {
            double effectiveRadius = MathConstants.EARTHSPHERERADIUSM;
            double rToAct = effectiveRadius + toAltM;
            double phi = gcRangeM/effectiveRadius;
        
            x = rToAct*Math.sin(phi);
            z = rToAct*Math.cos(phi) - effectiveRadius - fromAltM;
        }

        double elevAng = Math.atan2(z, x);
        return (elevAng/MathConstants.DEG2RAD)%360;
    }

    /**
     * Calculate azimuth angle at 1st position to 2nd position.
     *
     * Not to be used around poles
     *
     * @param fromLatDecDeg Latitude to look from
     * @param fromLonDecDeg Longitude to look from
     * @param toLatDecDeg Latitude to look from
     * @param toLonDecDeg Longitude to look from
     * @return Azimuth angle from 1st position to 2nd position
     */
    public static double calcAzimuthDeg(double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg)
    {
        double cosLat1 = Math.cos(lat1DecDeg*MathConstants.DEG2RAD);
        double cosLat2 = Math.cos(lat2DecDeg*MathConstants.DEG2RAD);
        
        double sinLat1 = Math.sin(lat1DecDeg*MathConstants.DEG2RAD);
        double sinLat2 = Math.sin(lat2DecDeg*MathConstants.DEG2RAD);
        
        double az = Math.atan2(cosLat2*Math.sin((lon2DecDeg-lon1DecDeg)*MathConstants.DEG2RAD),
                        cosLat1*sinLat2 - sinLat1*cosLat2*Math.cos((lon2DecDeg-lon1DecDeg)*MathConstants.DEG2RAD));

        return az/MathConstants.DEG2RAD;
    }

    /**
     * Calculate the straight line distance between two points.  Assumes a straight path can be accomplished, regardless
     * of any terrain/earth curvature between the two points.
     * 
     * Assumes spherical earth
     *
     * @param lat1 Latitude of 1st point
     * @param lon1 Longitude of 1st point
     * @param alt1 Altitude of 1st point, Alt Above Ellipse meters
     * @param lat2 Latitude of 2nd point
     * @param lon2 Longitude of 2nd point
     * @param alt2 Altitude of 2nd point, Alt Above Ellipse meters
     * @return Straight line distance in meters between two points
     */
    public static double calcStraightLineDistance(double lat1, double lon1, float alt1, double lat2, double lon2, double alt2)
    {
        double a  = MathConstants.EARTHSPHERERADIUSM;
        double e2 = 0;

        double phi1 = lat1*MathConstants.DEG2RAD;
        double lambda1 = lon1*MathConstants.DEG2RAD;
        double sinphi1 = Math.sin(phi1);
        double cosphi1 = Math.cos(phi1);
        double N1  = a / Math.sqrt(1 - e2 * sinphi1*sinphi1);
        double x1 = (N1 + alt1) * cosphi1 * Math.cos(lambda1);
        double y1 = (N1 + alt1) * cosphi1 * Math.sin(lambda1);
        double z1 = (N1*(1 - e2) + alt1) * sinphi1;

        double phi2 = lat2*MathConstants.DEG2RAD;
        double lambda2 = lon2*MathConstants.DEG2RAD;
        double sinphi2 = Math.sin(phi2);
        double cosphi2 = Math.cos(phi2);
        double N2  = a / Math.sqrt(1 - e2 * sinphi2*sinphi2);
        double x2 = (N2 + alt2) * cosphi2 * Math.cos(lambda2);
        double y2 = (N2 + alt2) * cosphi2 * Math.sin(lambda2);
        double z2 = (N2*(1 - e2) + alt2) * sinphi2;

        double length = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2));
        return length;
    }

    /**
     * Calculate great circle distance between two points, considering the path must curve around the earth.
     *
     * @param lat1DecDeg Latitude of 1st point
     * @param lon1DecDeg Longitude of 1st point
     * @param lat2DecDeg Latitude of 2nd point
     * @param lon2DecDeg Longitude of 2nd point
     * @return
     */
    public static double calcGreatCircleDistance (double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg)
    {
        double cosLat1 = Math.cos(lat1DecDeg*MathConstants.DEG2RAD);
        double cosLat2 = Math.cos(lat2DecDeg*MathConstants.DEG2RAD);
        
        double a = Math.pow(Math.sin((lat2DecDeg*MathConstants.DEG2RAD-lat1DecDeg*MathConstants.DEG2RAD)/2),2.0) + cosLat1*cosLat2*Math.pow(Math.sin((lon2DecDeg*MathConstants.DEG2RAD-lon1DecDeg*MathConstants.DEG2RAD)/2),2.0);
        double rng = rng = 2 * MathConstants.EARTHSPHERERADIUSM * Math.atan2(Math.sqrt(a),Math.sqrt(1 - a));

        return rng;
    }

    /**
     * Using the geoid lookup table, convert an MSL altitude (referenced to geoid) to height
     * above ellipsoid (referenced to ellipsoid)
     *
     * @param latDecDeg Latitude position of point to convert
     * @param lonDecDeg Longiutde position of point to convert
     * @param mslM MSL altitude, meters, of point to convert
     * @return Altitude above ellipsoid, in meters, of provided point
     */
    static public double convertMslToAltAbvEllip (double latDecDeg, double lonDecDeg, double mslM)
    {
        if (m_GeoidLookup == null)
        {
            m_GeoidLookup = GeoidHeightLookup.getInstance();
            m_GeoidLookup.readGeoidHeight("geoidHeight15min.img");
        }

        double geoidAltitude = m_GeoidLookup.getGeoidHeight(latDecDeg, lonDecDeg);

        double altAbvEllip = mslM + geoidAltitude;
        return altAbvEllip;
    }
    
    /**
     * Using the geoid lookup table, convert an MSL altitude (referenced to geoid) to height
     * above ellipsoid (referenced to ellipsoid)
     *
     * @param latDecDeg Latitude position of point to convert
     * @param lonDecDeg Longiutde position of point to convert
     * @param altWgs84 WGS84 altitude, meters, of point to convert
     * @return Altitude above MSL, in meters, of provided point
     */
    static public double convertAltAbvElliptoMsl (double latDecDeg, double lonDecDeg, double altWgs84)
    {
        if (m_GeoidLookup == null)
        {
            m_GeoidLookup = GeoidHeightLookup.getInstance();
            m_GeoidLookup.readGeoidHeight("geoidHeight15min.img");
        }

        double geoidAltitude = m_GeoidLookup.getGeoidHeight(latDecDeg, lonDecDeg);

        double mslM = altWgs84 - geoidAltitude;
        return mslM;
    }

    /**
     * Faster, unsafer version of Math.max
     *
     * @param val1
     * @param val2
     * @return
     */
    public static double fastMax (double val1, double val2)
    {
        return (val1<val2?val2:val1);
    }

    /**
     * Faster, unsafer version of Math.min
     *
     * @param val1
     * @param val2
     * @return
     */
    public static double fastMin (double val1, double val2)
    {
        return (val1<val2?val1:val2);
    }

    /**
     * Faster, unsafer version of Math.max
     *
     * @param val1
     * @param val2
     * @return
     */
    public static int fastMax (int val1, int val2)
    {
        return (val1<val2?val2:val1);
    }

    /**
     * Faster, unsafer version of Math.min
     *
     * @param val1
     * @param val2
     * @return
     */
    public static int fastMin (int val1, int val2)
    {
        return (val1<val2?val1:val2);
    }

    /**
     * Compute logarithm of a value with base 2
     * @param num
     * @return Log base 2 of num
     */
    public static double log2(double num)
    {
        return (Math.log(num)/Math.log(2));
    }
    
    public static double logBaseN (double num, double n)
    {
        return (Math.log(num)/Math.log(n));
    }

    /**
     * Parse a string in decimal degrees minutes format (DD'MM.MMM) and extract the decimal degrees value
     *
     * @param text Text in decimal minutes format
     * @return Decimal degrees value for input value
     */
    public static double parseDDMMMtoDecDeg(String text) throws Exception
    {
        int splitIdx = text.indexOf("'");
        if (splitIdx < 0)
            throw new Exception ("Delimiter ' not found (Format is DD'MM.MMM)");

        String degText = text.substring(0, splitIdx);
        double degVal = 0;
        try
        {
            degVal = Integer.parseInt(degText);
        }
        catch (Exception e)
        {
            throw new Exception ("Invalid degree value found (Format is DD'MM.MMM)");
        }

        String minText = text.substring(splitIdx+1);
        double minVal = 0;
        try
        {
            minVal = Double.parseDouble(minText);
        }
        catch (Exception e)
        {
            throw new Exception ("Invalid minute value found (Format is DD'MM.MMM)");
        }

        double decDeg = degVal + minVal / 60;
        return decDeg;
    }

    /**
     * Format a given lat/lon position as DD'MM.MM format
     *
     * @param latDecDeg
     * @param lonDecDeg
     * @return Formatted string
     */
    public static String formatDecDegAsDDMMM (double decDeg)
    {
        //Truncate off decimal for degrees value
        int latDeg = (int)decDeg;
        double latMin = Math.abs(decDeg-latDeg)*60;

        String retVal = latDeg + "'" + m_DecFormat4.format(latMin);
        return retVal;
    }

    /**
     * Parse a string in degrees minutes seconds format (DD'MM'SS.SS) and extract the decimal degrees value
     *
     * @param text Text in DMS format
     * @return Decimal degrees value for input value
     */
    public static double parseDDMMSSSStoDecDeg(String text) throws Exception
    {
        int split1Idx = text.indexOf("'");
        if (split1Idx < 0)
            throw new Exception ("1st Delimiter ' not found (Format is DD'MM'SS.SS)");

        String degText = text.substring(0, split1Idx);
        double degVal = 0;
        try
        {
            degVal = Integer.parseInt(degText);
        }
        catch (Exception e)
        {
            throw new Exception ("Invalid degree value found (Format is DD'MM'SS.SS)");
        }

        String remText = text.substring(split1Idx+1);

        int split2Idx = remText.indexOf("'");
        if (split2Idx < 0)
            throw new Exception ("2nd Delimiter ' not found (Format is DD'MM'SS.SS)");

        String minText = remText.substring(0, split2Idx);
        double minVal = 0;
        try
        {
            minVal = Integer.parseInt(minText);
        }
        catch (Exception e)
        {
            throw new Exception ("Invalid minute value found (Format is DD'MM'SS.SS)");
        }

        String secText = remText.substring(split2Idx+1);
        double secVal = 0;
        try
        {
            secVal = Double.parseDouble(secText);
        }
        catch (Exception e)
        {
            throw new Exception ("Invalid second value found (Format is DD'MM'SS.SS)");
        }

        double decDeg = degVal + minVal / 60 + secVal / 3600;
        return decDeg;
    }

    /**
     * Format a given lat/lon position as DD'MM'SS.SS format
     *
     * @param latDecDeg
     * @param lonDecDeg
     * @return Formatted string
     */
    public static String formatDecDegAsDDMMSSSS (double decDeg)
    {
        //Truncate off decimal for degrees value
        int latDeg = (int)decDeg;
        double latDecMin = Math.abs(decDeg-latDeg)*60;

        int latMin = (int)latDecMin;
        double latSec = Math.abs(latDecMin-latMin)*60;

        String retVal = latDeg + "'" + latMin + "'" + m_DecFormat2.format(latSec);
        return retVal;
    }

    /**
     * Return a 'clean' value near the specified parameter.  Will reduce complex decimals
     * to their next highest 1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, etc value.
     * 
     * @param val
     * @return
     */
    public static double getCleanValue(double val)
    {
        double cleanValue = 0;

        if (val < 0.1) {
            cleanValue = 0.1;
        }
        else if(val < 0.25) {
            cleanValue = 0.25;
        }
        else if (val < 0.5) {
            cleanValue = 0.5;
        }
        else if (val < 1.0) {
            cleanValue = 1;
        }
        else if (val < 2.0) {
            cleanValue = 2;
        }
        else if (val < 5.0) {
            cleanValue = 5;
        }
        else if (val < 10.0) {
            cleanValue = 10;
        }
        else if (val < 25.0) {
            cleanValue = 25;
        }
        else if (val < 50.0) {
            cleanValue = 50;
        }
        else if (val < 100.0) {
            cleanValue = 100;
        }
        else if (val < 250.0) {
            cleanValue = 250;
        }
        else if (val < 500.0) {
            cleanValue = 500;
        }
        else if (val < 1000.0) {
            cleanValue = 1000;
        }
        else if (val < 2500.0) {
            cleanValue = 2500;
        }
        else if (val < 5000.0) {
            cleanValue = 5000;
        }
        else if (val < 10000.0) {
            cleanValue = 10000;
        }
        else if (val < 25000.0) {
            cleanValue = 25000;
        }
        else if (val < 50000.0) {
            cleanValue = 50000;
        }
        else if (val < 100000.0) {
            cleanValue = 100000;
        }
        else if (val < 250000.0) {
            cleanValue = 250000;
        }
        else if (val < 500000.0) {
            cleanValue = 500000;
        }
        else if (val < 1000000.0) {
            cleanValue = 1000000;
        }
        else
        {
            cleanValue = 10000000;
        }

        return cleanValue;

    }
    
    public static String getMilDateTimeGroup (long epochTimeMs)
    {
        m_MilDTCFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return m_MilDTCFormat.format(new Date (epochTimeMs));
    }
    
    
    
    public static double fmod(double x, double y) 
    {
		int temp = (int) (x / y);
		double out = x - temp * y;
		return out;
    }
    static int month_day[][] = {
           {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365},
           {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366}
            };
    public static long getEpochTimestampMsFromGpsTow (int gpsWeekNumber, long gpsTimeOfWeekMs)
    {
        int guess;
        int yday, mjd, days_fr_jan1_1901;
        int delta_yrs, num_four_yrs, years_so_far, days_left;
        double fmjd;
        float gpsTimeOfWeekSec = gpsTimeOfWeekMs/1000;
        
        mjd = (int)((gpsWeekNumber)*7 + gpsTimeOfWeekSec/MathConstants.SEC_PER_DAY + MathConstants.JAN61980);
        //mjd = (int)(gpsWeekNumber*7 + gpsTimeOfWeekSec/MathConstants.SEC_PER_DAY + MathConstants.AUG151999);
        fmjd = fmod(gpsTimeOfWeekSec,MathConstants.SEC_PER_DAY)/MathConstants.SEC_PER_DAY;

        days_fr_jan1_1901 = mjd - MathConstants.JAN11901;
        num_four_yrs = days_fr_jan1_1901/1461;
        years_so_far = 1901 + 4*num_four_yrs;
        days_left = days_fr_jan1_1901 - 1461*num_four_yrs;
        delta_yrs = days_left/365 - days_left/1460;

        int year = years_so_far + delta_yrs;
        yday = days_left - 365*delta_yrs + 1;
        int hour = (int)(fmjd*24.0);
        int minute = (int)(fmjd*1440.0 - hour*60.0);
        int second = (int)(fmjd*86400.0 - hour*3600.0 - minute*60.0);

        boolean leap = (year%4 == 0 );
        guess = (int)(yday*0.032);
        boolean more = (  ( yday - month_day[leap?1:0][(int)guess+1] )  > 0  );
        int month = guess + (more?1:0) + 1;
        int mday = yday - month_day[leap?1:0][(int)guess+(more?1:0)];
        
        Date newDate = new Date(year-1900, month-1, mday, hour, minute, second);
        return newDate.getTime();
    }
}
