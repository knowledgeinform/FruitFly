/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.math.CoordConversions;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author humphjc1
 */
public class ConvertMGRSStringToLatLonAlt
{

    /**
     * Convert MGRS formatted text to LatLonAltPosition object
     * 
     * @param mgrsText
     * @return
     */
    static public LatLonAltPosition convert (String mgrsText)
    {
       MGRSPoint mgrs = new MGRSPoint(mgrsText);
       LatLonPoint latlon = MGRSPoint.MGRStoLL(mgrs, Ellipsoid.WGS_84, null);

       LatLonAltPosition lla = new LatLonAltPosition(new Latitude (latlon.getLatitude(), Angle.DEGREES), new Longitude (latlon.getLongitude(), Angle.DEGREES), new Altitude(0, Length.METERS));
       return lla;
    }
}
