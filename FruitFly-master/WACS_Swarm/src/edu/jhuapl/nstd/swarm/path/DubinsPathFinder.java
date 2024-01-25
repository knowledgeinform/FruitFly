/*
 * This class will use the Dubins formula to solve for the shortest
 *   arc between two poses (in 2D)
 *   The original paper can be found here: http://www.jstor.org/stable/2372560
 */
package edu.jhuapl.nstd.swarm.path;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.util.*;

import java.util.*;

/**
 *  This class will be uses statically, as it mostly is simple equations
 */
public class DubinsPathFinder {
    
    private enum DubinsPathTypes {
        LEFT_TURN,
        STRAIGHT,
        RIGHT_TURN
    }
    
    /* code based on test_dubins.m Matlab file from John Cole and Adam Watkins
     * 
     * We will first put positions into cartesian coordinates (with start pos at (0,0))
     * Then we will rotate the frame such that the positions are along the x-axis
     */
    public static Path2D FindDubinsPath(AbsolutePosition startPos, NavyAngle startHeading,
                                          AbsolutePosition endPos, NavyAngle endHeading,
                                          Length minRadius) {
        
        // come up with cartesian coordinates for endPos (with startPos at (0,0))
        ECEFPosition start_ecef = new ECEFPosition(startPos);
        ECEFPosition end_ecef = new ECEFPosition(endPos);
        LVPosition start_lv = new LVPosition(start_ecef, start_ecef);
        LVPosition end_lv = new LVPosition(start_ecef, end_ecef);
        
        //System.out.println("endLV -> ("+endLV[0]+","+endLV[1]+")");
        
        double rot_ang = Math.atan2(end_lv.y(), end_lv.x());
        Angle rot = new Angle(rot_ang, Angle.RADIANS);
        //System.out.println("rotation angle: "+rot.getDoubleValue(Angle.DEGREES));
        double s_rot = Math.sin(rot_ang);
        double c_rot = Math.cos(rot_ang);
        double endx_rot = c_rot*end_lv.x() + s_rot*end_lv.y();
        double endy_rot = -s_rot*end_lv.x() + c_rot*end_lv.y();
        Angle startAng = JLIB_Helpers.NavyAngle2Angle(startHeading).minus(rot);
        Angle endAng = JLIB_Helpers.NavyAngle2Angle(endHeading).minus(rot);
        
        /*System.out.println("Arguments to sub-function:");
        System.out.println("\tstartAng -> "+startAng.getDoubleValue(Angle.DEGREES));
        System.out.println("\tendPos -> ("+endx_rot+","+endy_rot+")");
        System.out.println("\tendAng -> "+endAng.getDoubleValue(Angle.DEGREES));
        System.out.println("\tminTurnRadius -> "+minRadius);*/
        
        Length[] pathLengths = new Length[3];
        DubinsPathTypes[] pathTypes = new DubinsPathTypes[3];
        DubinsPathFinder._dubins_class(startAng, endx_rot, endy_rot, endAng, minRadius, pathLengths, pathTypes);
        
        System.out.println("\n\n-----------");
        System.out.println("\tDubins path start -> "+start_lv.x()+","+start_lv.y());
        System.out.println("\tDubins path start bearing -> "+startHeading.toString(Angle.DEGREES));
        
        Path2D p1 = DubinsPathFinder._dubins_path(pathTypes[0], pathLengths[0], 
                                                  startPos, startHeading, minRadius);
        System.out.print(p1);
        LVPosition p1_end = new LVPosition(start_ecef, new ECEFPosition(p1.end()));
        System.out.println("\tDubins p1 end -> "+p1_end.x()+","+p1_end.y());
        System.out.println("\tDubins p1 end bearing -> "+p1.endBearing().toString(Angle.DEGREES));
        
        Path2D p2 = DubinsPathFinder._dubins_path(pathTypes[1], pathLengths[1],
                                                  p1.end(), p1.endBearing(), minRadius);
        LVPosition p2_end = new LVPosition(start_ecef, new ECEFPosition(p2.end()));
        System.out.println("\tDubins p2 end -> "+p2_end.x()+","+p2_end.y());
        System.out.println("\tDubins p2 end bearing -> "+p2.endBearing().toString(Angle.DEGREES));
        
        Path2D p3 = DubinsPathFinder._dubins_path(pathTypes[2], pathLengths[2],
                                                  p2.end(), p2.endBearing(), minRadius);
        LVPosition p3_end = new LVPosition(start_ecef, new ECEFPosition(p3.end()));
        System.out.println("\tDubins p3 end -> "+p3_end.x()+","+p3_end.y());
        System.out.println("\tDubins p3 end bearing -> "+p3.endBearing().toString(Angle.DEGREES));
        
        ArrayList<Path2D> paths = new ArrayList<Path2D>();
        paths.add(p1); paths.add(p2); paths.add(p3);
        
        return new CompositePath2D(paths);
    }
    
    
    /* code based on dubins_path.m Matlab file from John Cole and Adam Watkins
     * 
     */
    private static Path2D   _dubins_path(  DubinsPathTypes pathType, Length pathLength,
                                           AbsolutePosition startPos, 
                                           NavyAngle startBearing, 
                                           Length minTurnRadius  ) {
        
        switch (pathType) {
            case LEFT_TURN:
                return new ArcPath2D(minTurnRadius, startPos, startBearing, pathLength, false);
                
            case STRAIGHT:
                return new StraightPath2D(startPos, startBearing, pathLength);
                
            case RIGHT_TURN:
                return new ArcPath2D(minTurnRadius, startPos, startBearing, pathLength, true);
            
            default:
                // we be in trouble here...
                return null;
        }
    
    }
    
    
    
    /* code based on dubins_class.m Matlab file from John Cole and Adam Watkins
     * 
     * IMPORTANT: This path length calculator assumes the start point is at the
     *              origin.
     */
    private static void   _dubins_class( Angle start, 
                                         double endX, double endY, 
                                         Angle end, Length radius,
                                         Length[] pathLengths, DubinsPathTypes[] pathTypes) {
        double minLength = Double.MAX_VALUE;
        double alpha = start.getDoubleValue(Angle.RADIANS);
        double beta = end.getDoubleValue(Angle.RADIANS);
        double d = Math.sqrt(endX*endX + endY*endY) / radius.getDoubleValue(Length.METERS);
        
        double sa = Math.sin(alpha);
        double ca = Math.cos(alpha);
        double sb = Math.sin(beta);
        double cb = Math.cos(beta);
        double camb = Math.cos(alpha-beta);
        double at1, at2, at3, at4, p5, p6, temp, isreal, len;
        double len1, len2, len3;
        
        // case 1: LSL
        at1 = Math.atan2(cb-ca, d+sa-sb);
        len1 = JLIB_Helpers.angle0to2PI(-alpha + at1);
        len2 = Math.sqrt(2 + d*d - 2*camb+ 2*d*(sa-sb));
        len3 = JLIB_Helpers.angle0to2PI(beta-at1);
        len = len1 + len2 + len3;
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.LEFT_TURN; 
            pathTypes[1] = DubinsPathTypes.STRAIGHT; 
            pathTypes[2] = DubinsPathTypes.LEFT_TURN;
        }
        
        // case 2: RSR
        at2 = Math.atan2(ca-cb, d-sa+sb);
        len1 = JLIB_Helpers.angle0to2PI(alpha-at2);
        len2 = Math.sqrt(2 + d*d - 2*camb+ 2*d*(sb-sa));
        len3 = JLIB_Helpers.angle0to2PI(-beta+at2);
        len = len1 + len2 + len3;
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.RIGHT_TURN; 
            pathTypes[1] = DubinsPathTypes.STRAIGHT; 
            pathTypes[2] = DubinsPathTypes.RIGHT_TURN;
        }
        
        // case 3: LSR
        isreal = -2 + d*d + 2*camb + 2*d*(sa+sb);
        if (isreal < 0)
            len = Double.MAX_VALUE;
        else {
            at3 = Math.atan2(-ca-cb, d+sa+sb);
            isreal = Math.sqrt(isreal);
            len1 = JLIB_Helpers.angle0to2PI(-alpha+at3-Math.atan2(-2,isreal));
            len2 = isreal;
            len3 = JLIB_Helpers.angle0to2PI(-beta+at3-Math.atan2(-2,isreal));
            len = len1 + len2 + len3;
        }
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.LEFT_TURN; 
            pathTypes[1] = DubinsPathTypes.STRAIGHT; 
            pathTypes[2] = DubinsPathTypes.RIGHT_TURN;
        }
        
        // case 4: RSL
        isreal = d*d - 2 + 2*camb - 2*d*(sa+sb);
        if (isreal < 0)
            len = Double.MAX_VALUE;
        else {
            at4 = Math.atan2(ca+cb, d-sa-sb);
            isreal = Math.sqrt(isreal);
            len1 = JLIB_Helpers.angle0to2PI(alpha-at4+Math.atan2(2,isreal));
            len2 = isreal;
            len3 = JLIB_Helpers.angle0to2PI(beta-at4+Math.atan2(2,isreal));
            len = len1 + len2 + len3;
        }
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.RIGHT_TURN; 
            pathTypes[1] = DubinsPathTypes.STRAIGHT; 
            pathTypes[2] = DubinsPathTypes.LEFT_TURN;
        }
        
        // case 5: RLR
        isreal = 0.125 * (6 - d*d + 2*camb + 2*d*(sa-sb));
        if (Math.abs(isreal) > 1.0)
            len = Double.MAX_VALUE;
        else {
            p5 = -Math.acos(isreal);
            len2 = JLIB_Helpers.angle0to2PI(p5);
            temp = JLIB_Helpers.angle0to2PI(alpha-at2+p5/2);
            len1 += temp;
            len3 += JLIB_Helpers.angle0to2PI(alpha-beta-temp+p5);
            len = len1 + len2 + len3;
        }
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.RIGHT_TURN; 
            pathTypes[1] = DubinsPathTypes.LEFT_TURN; 
            pathTypes[2] = DubinsPathTypes.RIGHT_TURN;
        }
        
        // case 6: LRL
        isreal = 0.125 * (6 - d*d + 2*camb + 2*d*(sb-sa));
        if (Math.abs(isreal) > 1.0)
            len = Double.MAX_VALUE;
        else {
            p6 = -Math.acos(isreal);
            len2 = JLIB_Helpers.angle0to2PI(p6);
            temp = JLIB_Helpers.angle0to2PI(-alpha+at1+p6/2);
            len1 += temp;
            len3 += JLIB_Helpers.angle0to2PI(-alpha+beta-temp+p6);
            len = len1 + len2 + len3;
        }
        if (len < minLength) {
            minLength = len;
            pathLengths[0] = new Length(len1, Length.METERS);
            pathLengths[1] = new Length(len2, Length.METERS);
            pathLengths[2] = new Length(len3, Length.METERS);
            pathTypes[0] = DubinsPathTypes.LEFT_TURN; 
            pathTypes[1] = DubinsPathTypes.RIGHT_TURN; 
            pathTypes[2] = DubinsPathTypes.LEFT_TURN;
        }
        
        for (int i=0; i<3; i++) {
            pathLengths[i] = new Length(pathLengths[i].getDoubleValue(Length.METERS) * 
                                            radius.getDoubleValue(Length.METERS), 
                                        Length.METERS);
            System.out.println("Dubins segment "+(i+1)+":");
            System.out.println("\tType = "+pathTypes[i]);
            System.out.println("\tLength = "+pathLengths[i].toString(Length.METERS));
        }
        
        return;
    }
    
    public static void main(String[] args) {
        
        LatLonAltPosition orig = new LatLonAltPosition( new Latitude(0, Angle.DEGREES),
                                                      new Longitude(0, Angle.DEGREES),
                                                      new Altitude(0.0, Length.METERS));
        ECEFPosition orig_ecef = new ECEFPosition(orig);
        NavyAngle orig_heading = NavyAngle.NORTH;
        
        LVPosition dest_lv = new LVPosition(orig_ecef, 0.0, 200.0, 0.0);
        AbsolutePosition dest = dest_lv.toECEF().toAbsolutePosition();
        NavyAngle dest_heading = NavyAngle.NORTH;
        
        Length min_radius = new Length(50.0, Length.METERS);
        
        Path2D dubins = DubinsPathFinder.FindDubinsPath(orig, orig_heading, dest, dest_heading, min_radius); 
        
        System.out.println("\n\n------------------------------");
        ECEFPosition start_ecef = new ECEFPosition(dubins.start());
        ECEFPosition end_ecef = new ECEFPosition(dubins.end());
        LVPosition start = new LVPosition(start_ecef, start_ecef);
        System.out.println("\tStarting position of path: "+start.x()+","+start.y());
        System.out.println("\tStarting bearing: "+dubins.startBearing().toString(Angle.DEGREES));
        LVPosition end = new LVPosition(start_ecef, end_ecef);
        System.out.println("\tEnd position of path: "+end.x()+","+end.y());
        System.out.println("\tEnd bearing: "+dubins.endBearing().toString(Angle.DEGREES));
        System.out.println("------------------------------\n\n\n");
    }
}
