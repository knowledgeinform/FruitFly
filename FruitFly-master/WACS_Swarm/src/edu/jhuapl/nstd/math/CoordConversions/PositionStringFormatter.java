/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.math.CoordConversions;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.math.CoordinateConversion;
import java.text.DecimalFormat;

/**
 *
 * @author humphjc1
 */
public class PositionStringFormatter 
{
    private static DecimalFormat m_DecFormat2 = new DecimalFormat ("0.00");
    private static DecimalFormat m_DecFormat4 = new DecimalFormat ("0.0000");
    private static DecimalFormat m_DecFormat6 = new DecimalFormat ("0.000000");
    
    public static String formatLatORLonAsDecDeg (double positionDecDeg, String positiveChar, String negativeChar)
    {
        return (m_DecFormat6.format (Math.abs (positionDecDeg)) + " " + (positionDecDeg>0?positiveChar:negativeChar));
    }
    
    public static String formatLatORLonAsDegMin (double positionDecDeg, String positiveChar, String negativeChar)
    {
        int positionDegPosINT = (int)Math.abs (positionDecDeg);
        double positionMinPos = (Math.abs (positionDecDeg)-positionDegPosINT)*60;
        return (positionDegPosINT + "" + (char)176 + " " + m_DecFormat4.format (positionMinPos) + "' " + (positionDecDeg>0?positiveChar:negativeChar));
    }
    
    public static String formatLatORLonAsDegMinSec (double positionDecDeg, String positiveChar, String negativeChar)
    {
        int positionDegPosINT = (int)Math.abs (positionDecDeg);
        double positionMinPos = (Math.abs (positionDecDeg)-positionDegPosINT)*60;
        int positionMinPosINT = (int)positionMinPos;
        double positionSecPos = (Math.abs (positionMinPos)-positionMinPosINT)*60;
        return (positionDegPosINT + "" + (char)176 + " " + positionMinPosINT + "' " + m_DecFormat2.format (positionSecPos) + "\" " + (positionDecDeg>0?positiveChar:negativeChar));
    }
    
    public static String formatLatLonAsMGRS (double latDecDeg, double lonDecDeg)
    {
        LatLonAltPosition lla = new LatLonAltPosition (new Latitude (latDecDeg, Angle.DEGREES), new Longitude (lonDecDeg, Angle.DEGREES), Altitude.ZERO);
        
        if (false)
        {
            //Hide actual grid for sensitive locations
            String retVal = ConvertLatLonAltToMGRSString.convert(lla);
            retVal = "12ABC" + retVal.substring(5);
            return retVal;
        }
        
        return ConvertLatLonAltToMGRSString.convert(lla);
    }
}
