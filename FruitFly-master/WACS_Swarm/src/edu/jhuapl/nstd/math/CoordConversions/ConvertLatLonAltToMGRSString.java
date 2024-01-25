/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.math.CoordConversions;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author humphjc1
 */
public class ConvertLatLonAltToMGRSString
{

    public static String convert (LatLonAltPosition lla)
    {
        LatLonPoint point = new LatLonPoint(lla.getLatitude().getDoubleValue(Angle.DEGREES), lla.getLongitude().getDoubleValue(Angle.DEGREES));
        MGRSPoint mgrs = new MGRSPoint(point);

        return mgrs.getMGRS();
    }
}
