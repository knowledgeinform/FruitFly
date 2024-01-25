/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.util;

/**
 * Random math constants
 *
 * @author humphjc1
 */
public class MathConstants
{
    /**
     * 1/60
     */
    static final public double INV60 = 0.01666666667;

    /**
     * 1/3600
     */
    static final public double INV3600 = 0.00027777777778;

    /**
     * pi
     */
    static final public double M_PI = 3.14159265358979323;

    /**
     * Conversion from degrees to radians
     */
    static final public double DEG2RAD = M_PI / 180.0;

    /**
     * Radius of earth assuming sphere, meters
     */
    static final public double EARTHSPHERERADIUSM = 6378137;
    
    static final public double EARTH_WGS84_Eccentricity = 0.0818191908426;


    /**
     * Ratio of meters/degree at the equator assuming spherical earth.  Used to calculate
     * meter-resolution from degree-resolution
     */
    static final public double DEG2M = 2*M_PI*EARTHSPHERERADIUSM / 360.0;

    /**
     * Scale factor converting from feet to meters
     */
    static final public double FT2M = 0.3048;

    /**
     * Scale factor converting from kilometers to meters
     */
    static final public double KM2M = 1000.0;

    /**
     * Scale factor converting from miles to meters
     */
    static final public double MI2M = 1609.344;

    /**
     * Scale factor converting from nautical miles to meters
     */
    static final public double NM2M = 1852;
    
    
    
    static final int JAN61980 = 44244;
    static final int AUG151999 = 51405;
    static final int JAN11901 = 15385;
    static final float SEC_PER_DAY = 86400.0f;

}
