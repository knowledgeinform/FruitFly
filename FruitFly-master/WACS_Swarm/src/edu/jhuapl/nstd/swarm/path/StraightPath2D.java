/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.path;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.util.*;

/**
 *
 * @author colejg1
 */
public class StraightPath2D implements Path2D {
    protected AbsolutePosition _start, _end;
    protected NavyAngle _path_bearing;

    public StraightPath2D(AbsolutePosition start, AbsolutePosition end) {
        _start = new LatLonAltPosition(start.asLatLonAltPosition().getLatitude(),
                                       start.asLatLonAltPosition().getLongitude(),
                                       new Altitude(0.0, Length.METERS));
        _end = new LatLonAltPosition(end.asLatLonAltPosition().getLatitude(),
                                     end.asLatLonAltPosition().getLongitude(),
                                     new Altitude(0.0, Length.METERS));
        _path_bearing = _start.getBearingTo(_end);
    }
    public StraightPath2D(AbsolutePosition startPos, NavyAngle startBearing, Length length) {
        _start = new LatLonAltPosition(startPos.asLatLonAltPosition().getLatitude(),
                                       startPos.asLatLonAltPosition().getLongitude(),
                                       new Altitude(0.0, Length.METERS));
        _path_bearing = startBearing;
        _end = _start.translatedBy(new RangeBearingHeightOffset(length, _path_bearing, Length.ZERO));
    }
    
    @Override
    public Length length() {
        return _start.getRangeTo(_end);
    }

    @Override
    public PathError getPathError(AbsolutePosition currPos, NavyAngle currHeading) {
        PathError err = new PathError();
        
        Length r_to_start = _start.getRangeTo(currPos);
        Length r_to_end = _end.getRangeTo(currPos);
        AbsolutePosition middle = _start.translatedBy(new RangeBearingHeightOffset(_start.getRangeTo(_end).dividedBy(2.0),
                                                                                   _start.getBearingTo(_end),
                                                                                   Length.ZERO));
        Length r_to_mid = middle.getRangeTo(currPos); 
        
        AbsolutePosition closestPoint;
        if ((r_to_mid.compareTo(r_to_start)<=0 && r_to_mid.compareTo(r_to_end)<=0) ||
            (r_to_mid.compareTo(r_to_start)>=0 && r_to_mid.compareTo(r_to_end)>=0)) {
            // the point is outside the perpindicular plane of the straight path
            //     closest point is just closer between start and end
            closestPoint = (r_to_end.compareTo(r_to_start)<=0) ? end() : start();
        } else {
            // ok, now we can do our intesect
            
            ECEFPosition start_ecef = new ECEFPosition(_start);
            LVPosition end_lv = new LVPosition(start_ecef, new ECEFPosition(_end));
            //System.out.println("end_lv -> "+end_lv.x()+","+end_lv.y()+","+end_lv.z());
            LVPosition spot_lv = new LVPosition(start_ecef, new ECEFPosition(currPos));
            //System.out.println("spot_lv -> "+spot_lv.x()+","+spot_lv.y()+","+spot_lv.z());

            // going to solve for intersection of two lines:
            //    line 1: the path
            //    line 2: perpindicular line to path, going through currPos

            // line 1:   y - y_s = m1 * (x - x_s)
            //                m1 = (y_s - y_e) / (x_s - x_e)
            //        in this case, x_s = y_s = 0 (since that's our origin)

            // line 2:  y - y_p = m2 * (x - x_p)
            //                m2 = -1.0 / m1

            // combining equations (upon intersection), 
            //     y_s + m1*(x-x_s) = y_p + m2*(x-x_p)
            // solves down to:
            //       x = (m1*x_s - y_s + y_p - m2*s_p) / (m1 - m2)

            double m1 = end_lv.y() / end_lv.x();
            double m2 = -1.0 / m1;

            double x = (spot_lv.y() - m2*spot_lv.x()) / (m1-m2);
            double y = m1*x;

            LVPosition intersect = new LVPosition(start_ecef, x, y, 0);
            //System.out.println("intersect -> "+intersect.x()+","+intersect.y()+","+intersect.z());
            closestPoint = intersect.toECEF().toAbsolutePosition();
        }
        
        // compute distance from path (make it negative if its left of path)
        err.DistanceFromPath = closestPoint.getRangeTo(currPos);
        NavyAngle closestToPos = closestPoint.getBearingTo(currPos);
        Angle diff = _path_bearing.clockwiseAngleTo(closestToPos);
        if (diff.compareTo(Angle.HALF_CIRCLE) > 0)
            err.DistanceFromPath = err.DistanceFromPath.times(-1.0);
        
        double ang_rad = currHeading.minus(_path_bearing).getDoubleValue(Angle.RADIANS);
        err.BearingFromPath = new Angle(JLIB_Helpers.anglePItoPI(ang_rad), Angle.RADIANS);
        err.BearingAtPath = _path_bearing;
        
        return err;
        
    }

    @Override
    public AbsolutePosition start() {
        return _start;
    }

    @Override
    public AbsolutePosition end() {
        return _end;
    }
    
    @Override
    public NavyAngle startBearing() {
        return _path_bearing;
    }

    @Override
    public NavyAngle endBearing() {
        return _path_bearing;
    }
    
    public static void main(String[] args) {
        LatLonAltPosition orig = new LatLonAltPosition(new Latitude(0.0, Angle.DEGREES),
                                                       new Longitude(0.0, Angle.DEGREES),
                                                       new Altitude(0.0, Length.METERS));
        ECEFPosition orig_ecef = new ECEFPosition(orig);
        
        LVPosition start = new LVPosition(orig_ecef, 100, 0, 0);
        LVPosition end = new LVPosition(orig_ecef, 0, 100, 0);
        
        StraightPath2D sp = new StraightPath2D(start.toECEF().toAbsolutePosition(), 
                                               NavyAngle.NORTHWEST, new Length(141.4214, Length.METERS));
        
        LVPosition st_lv = new LVPosition(orig_ecef, new ECEFPosition(sp.start()));
        System.out.println("Arc start -> "+st_lv.x()+","+st_lv.y());
        LVPosition end_lv = new LVPosition(orig_ecef, new ECEFPosition(sp.end()));
        System.out.println("Arc end   -> "+end_lv.x()+","+end_lv.y());
        System.out.println("Arc len   -> "+sp.length());
        
        
        LVPosition spot = new LVPosition(orig_ecef, 75, 10, 0);
        System.out.println("\nspot -> "+spot.x()+","+spot.y());
        NavyAngle spotBearing = new NavyAngle(-25.0, Angle.DEGREES);
        PathError pe = sp.getPathError(spot.toECEF().toAbsolutePosition(), spotBearing);
        System.out.println("\n\nPath ERROR:");
        System.out.println("\tDist Err -> "+pe.DistanceFromPath);
        System.out.println("\tBear Err -> "+pe.BearingFromPath.toString(Angle.DEGREES));
    }
    
}
