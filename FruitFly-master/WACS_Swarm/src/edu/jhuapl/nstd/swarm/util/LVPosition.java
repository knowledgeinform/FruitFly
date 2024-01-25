/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
/**
 * Local Value Position
 *   East, North, Up coordinate frame
 * @author colejg1
 */
public class LVPosition {
    protected ECEFPosition _origin;
    protected double _x, _y, _z;
    
    public LVPosition(ECEFPosition orig, double x, double y, double z) {
        _origin = orig;
        _x = x;
        _y = y;
        _z = z;
    }
    
    public LVPosition(ECEFPosition orig, ECEFPosition position) {
        _origin = orig;
        
        double[] diff = new double[3];

        diff[0] = position.x() - _origin.x();
        diff[1] = position.y() - _origin.y();
        diff[2] = position.z() - _origin.z();

        LatLonAltPosition lla_orig = orig.toAbsolutePosition().asLatLonAltPosition();
        double slat = Math.sin(lla_orig.getLatitude().getDoubleValue(Angle.RADIANS));
        double clat = Math.cos(lla_orig.getLatitude().getDoubleValue(Angle.RADIANS));
        double slon = Math.sin(lla_orig.getLongitude().getDoubleValue(Angle.RADIANS));
        double clon = Math.cos(lla_orig.getLongitude().getDoubleValue(Angle.RADIANS));

        _x = (-slon*diff[0]) + (clon*diff[1]);
        _y = (slat*-clon*diff[0]) + (slat*-slon*diff[1]) + (clat*diff[2]);
        _z = (clat*clon*diff[0]) + (clat*slon*diff[1]) + (slat*diff[2]);
    }
    
    public double x() { return _x; }
    public double y() { return _y; }
    public double z() { return _z; }
    
    public ECEFPosition toECEF() {
        LatLonAltPosition lla_orig = _origin.toAbsolutePosition().asLatLonAltPosition();
        double clat = Math.cos(lla_orig.getLatitude().getDoubleValue(Angle.RADIANS));
        double slat = Math.sin(lla_orig.getLatitude().getDoubleValue(Angle.RADIANS));
        double clon = Math.cos(lla_orig.getLongitude().getDoubleValue(Angle.RADIANS));
        double slon = Math.sin(lla_orig.getLongitude().getDoubleValue(Angle.RADIANS));
        return new ECEFPosition(
            _origin.x() + (-slon * _x + -slat * clon * _y + clat * clon * _z),
            _origin.y() + (clon * _x + -slat * slon * _y + clat * slon * _z),
            _origin.z() + (clat * _y + slat * _z));
    }
    
}