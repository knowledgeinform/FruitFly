/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

/**
 * Earth Centered - Earth Fixed position
 * 
 * @author colejg1
 */
public class ECEFPosition {
    protected double _x, _y, _z;
    
    public ECEFPosition(double x, double y, double z) {
        _x = x;
        _y= y;
        _z = z;
    }
    
    public ECEFPosition(AbsolutePosition position) {
        LatLonAltPosition pos = position.asLatLonAltPosition();
        double slat = Math.sin(pos.getLatitude().getDoubleValue(Angle.RADIANS));
        double clat = Math.cos(pos.getLatitude().getDoubleValue(Angle.RADIANS));
        double slon = Math.sin(pos.getLongitude().getDoubleValue(Angle.RADIANS));
        double clon = Math.cos(pos.getLongitude().getDoubleValue(Angle.RADIANS));
        double e2 = MathConstants.EARTH_WGS84_Eccentricity*MathConstants.EARTH_WGS84_Eccentricity;
        double r_earth = MathConstants.EARTHSPHERERADIUSM / Math.sqrt(1 - (e2*slat*slat));
        double abs_alt = r_earth + pos.getAltitude().getDoubleValue(Length.METERS);
        _x = abs_alt * clat * clon;
        _y = abs_alt * clat * slon;
        _z = (r_earth * (1-e2) + pos.getAltitude().getDoubleValue(Length.METERS)) * slat;
    }
    
    public double x() { return _x; }
    public double y() { return _y; }
    public double z() { return _z; }
    
    public AbsolutePosition toAbsolutePosition() {
        double e2 = MathConstants.EARTH_WGS84_Eccentricity * MathConstants.EARTH_WGS84_Eccentricity;
        double r = MathConstants.EARTHSPHERERADIUSM;
        double ep2 = e2 / (1 - e2);
        double f = 1 - Math.sqrt(1 - e2);
        double b = r * (1 - f);
        double lambda = Math.atan2(_y, _x);
        double rho = Math.sqrt(_x * _x + _y * _y);
        double beta = Math.atan2(_z, (1 - f) * rho);
        double sbeta = Math.sin(beta);
        double cbeta = Math.cos(beta);
        double phi = Math.atan2(_z + b * ep2 * sbeta * sbeta * sbeta,
                                rho - r * e2 * cbeta * cbeta * cbeta);

        double betaNew = Math.atan2((1 - f) * Math.sin(phi), Math.cos(phi));
        int count = 0;
        while (Math.abs(beta - betaNew) > 0.0000001 && count < 5)
        {
            beta = betaNew;
            sbeta = Math.sin(beta);
            cbeta = Math.cos(beta);
            phi = Math.atan2(_z + b * ep2 * sbeta * sbeta * sbeta,
                             rho - r * e2 * cbeta * cbeta * cbeta);
            betaNew = Math.atan2((1 - f) * Math.sin(phi), Math.cos(phi));
            count++;
        }
        double sphi = Math.sin(phi);
        double N = r / Math.sqrt(1 - e2 * sphi * sphi);

        Latitude lat = new Latitude(phi, Angle.RADIANS);
        Longitude lon = new Longitude(lambda, Angle.RADIANS);
        Altitude alt = new Altitude(rho * Math.cos(phi) + (_z + e2 * N * sphi) * sphi - N, Length.METERS);
        return new LatLonAltPosition(lat, lon, alt);
    }
}
