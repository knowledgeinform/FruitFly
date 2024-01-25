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
public class ArcPath2D implements Path2D {
    protected AbsolutePosition _center;
    protected Length _radius;
    protected NavyAngle _arc_start, _arc_end;
    protected boolean _clockwise;
    protected Angle _arc_len;
    
    /* ArcPath()
     * NOTE: Arc Path is defined as starting at "start" angle, then moving 
     *       either clockwise or counter-clockwise (based on "clockwise" input)
     *       around orbit to the angle of "end"
     */
    public ArcPath2D(AbsolutePosition center, Length radius, NavyAngle start, NavyAngle end, boolean clockwise) {
        _center = new LatLonAltPosition(center.asLatLonAltPosition().getLatitude(),
                                        center.asLatLonAltPosition().getLongitude(),
                                        new Altitude(0.0, Length.METERS));
        _radius = radius;
        _arc_start = start;
        _arc_end = end;
        _clockwise = clockwise;
        _arc_len = (clockwise ? _arc_start.clockwiseAngleTo(_arc_end) : _arc_start.counterClockwiseAngleTo(_arc_end));
    }
    public ArcPath2D(Length radius, AbsolutePosition startLocation, NavyAngle startBearing, Length length, boolean clockwise) {
        _clockwise = clockwise;
        _radius = radius;
        NavyAngle toCenter = (_clockwise ? startBearing.plus(Angle.RIGHT_ANGLE) : startBearing.minus(Angle.RIGHT_ANGLE));
        _center = startLocation.translatedBy(new RangeBearingHeightOffset(_radius, toCenter, Length.ZERO));
        _center = new LatLonAltPosition(_center.asLatLonAltPosition().getLatitude(),
                                        _center.asLatLonAltPosition().getLongitude(),
                                        new Altitude(0.0, Length.METERS));
        _arc_start = _center.getBearingTo(startLocation);
        
        double percent = 2*Math.PI*radius.getDoubleValue(Length.METERS);
        percent /= length.getDoubleValue(Length.METERS);
        _arc_len = Angle.FULL_CIRCLE.dividedBy(percent);
        _arc_end = (clockwise ? _arc_start.plus(_arc_len) : _arc_start.minus(_arc_len));
    }
    
    @Override
    public String toString() {
        String s = "\t\tArcPath2D:\n";
        s += "\t\tarc_start = "+_arc_start.toString(Angle.DEGREES)+"\n";
        s += "\t\tarc_end = "+_arc_end.toString(Angle.DEGREES)+"\n";
        s += "\t\tarc_len = "+_arc_len.toString(Angle.DEGREES)+"\n";
        s += "\t\tclockwise = "+_clockwise+"\n";
        LVPosition end_lv = new LVPosition(new ECEFPosition(start()), new ECEFPosition(end()));
        s += "\t\tArc end = "+end_lv.x()+","+end_lv.y()+"\n";
        return s;
    }

    @Override
    public Length length() {
        double circum = ((Math.PI * 2.0) * _radius.getDoubleValue(Length.METERS));
        double arcPer = (_arc_len.getDoubleValue(Angle.RADIANS) / (Math.PI * 2.0));
        return new Length(circum * arcPer, Length.METERS);
    }

    @Override
    public PathError getPathError(AbsolutePosition currPos, NavyAngle currHeading) {
        PathError err = new PathError();
        
        // first, we need to find the closest point on the arc to this given currPos
        AbsolutePosition closestPoint;
        NavyAngle toPos = _center.getBearingTo(currPos);
        //System.out.println("Angle from center to point -> "+toPos.toString(Angle.DEGREES));
        if (isBearingWithinArcPath(toPos)) {
            //System.out.println("Point IS within arc...");
            // currPos bearing is within our arc, so we just pick that bearing's point on arc
            closestPoint = _center.translatedBy(new RangeBearingHeightOffset(_radius, toPos, Length.ZERO));
        } else {
            //System.out.println("Point IS NOT within arc...");
            // currPos bearing is outside our arc, so we either pick end or start as closest point
            Length toStart = start().getRangeTo(currPos);
            Length toEnd = end().getRangeTo(currPos);
            if (toStart.compareTo(toEnd) <= 0)
                closestPoint = start();
            else
                closestPoint = end();
        }
        err.DistanceFromPath = closestPoint.getRangeTo(currPos);
        
        NavyAngle toClosest = _center.getBearingTo(closestPoint);
        err.BearingAtPath = (_clockwise ? toClosest.plus(Angle.RIGHT_ANGLE) : toClosest.minus(Angle.RIGHT_ANGLE));
        double ang_rad = currHeading.minus(err.BearingAtPath).getDoubleValue(Angle.RADIANS);
        err.BearingFromPath = new Angle(JLIB_Helpers.anglePItoPI(ang_rad), Angle.RADIANS);
        
        // if currPos is left of path, we need to negate distance error
        NavyAngle closestToPos = closestPoint.getBearingTo(currPos);
        Angle pathBearingDiff = err.BearingAtPath.clockwiseAngleTo(closestToPos);
        if (pathBearingDiff.compareTo(Angle.HALF_CIRCLE) > 0)
            err.DistanceFromPath = err.DistanceFromPath.times(-1.0);
         
        return err;
    }

    @Override
    public AbsolutePosition start() {
        return _center.translatedBy(new RangeBearingHeightOffset(_radius, _arc_start, Length.ZERO));
    }

    @Override
    public AbsolutePosition end() {
        return _center.translatedBy(new RangeBearingHeightOffset(_radius, _arc_end, Length.ZERO));
    }
    
    @Override
    public NavyAngle startBearing() {
        return (_clockwise ? _arc_start.plus(Angle.RIGHT_ANGLE) : _arc_start.minus(Angle.RIGHT_ANGLE));
    }

    @Override
    public NavyAngle endBearing() {
        return (_clockwise ? _arc_end.plus(Angle.RIGHT_ANGLE) : _arc_end.minus(Angle.RIGHT_ANGLE));
    }
    
    
    /* bearingWithinArcPath(bearing)
     * Function used to determine if a given bearing is within our arc
     */
    private boolean isBearingWithinArcPath(NavyAngle bearing) {
        if (_clockwise) {
            return (bearing.compareTo(_arc_start)>=0) && (bearing.compareTo(_arc_end)<=0);
        } else {
            return (bearing.compareTo(_arc_start)<=0) && (bearing.compareTo(_arc_end)>=0);
        }
    }
    
    public static void main(String[] args) {
        LatLonAltPosition cent = new LatLonAltPosition(new Latitude(0.0, Angle.DEGREES),
                                                       new Longitude(0.0, Angle.DEGREES),
                                                       new Altitude(0.0, Length.METERS));
        
        Length radius = new Length(100.0, Length.METERS);
        NavyAngle arcStart = NavyAngle.EAST;
        NavyAngle arcStop = NavyAngle.NORTH;
        boolean cw = false;
       
        // (Length radius, AbsolutePosition startLocation, NavyAngle startBearing, Length length, boolean clockwise)
        ArcPath2D ap = new ArcPath2D(radius, cent, arcStart, new Length(157.0796, Length.METERS), cw);
        
        LVPosition st_lv = new LVPosition(new ECEFPosition(cent), new ECEFPosition(ap.start()));
        System.out.println("Arc start -> "+st_lv.x()+","+st_lv.y());
        LVPosition end_lv = new LVPosition(new ECEFPosition(cent), new ECEFPosition(ap.end()));
        System.out.println("Arc end   -> "+end_lv.x()+","+end_lv.y());
        System.out.println("Arc len   -> "+ap.length());
        
        LVPosition spot_lv = new LVPosition(new ECEFPosition(cent), 50, 50, 0);
        System.out.println("\nspot -> "+spot_lv.x()+","+spot_lv.y());
        NavyAngle spotBearing = NavyAngle.EAST;
        PathError pe = ap.getPathError(spot_lv.toECEF().toAbsolutePosition(), spotBearing);
        System.out.println("\n\nPath ERROR:");
        System.out.println("\tDist Err -> "+pe.DistanceFromPath);
        System.out.println("\tBear Err -> "+pe.BearingFromPath.toString(Angle.DEGREES));
    }
    
}
