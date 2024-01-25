/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.path;


import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

/**
 *
 * @author colejg1
 */
public interface Path2D {
    
    /* class Path Error
     * Used as a mechanism to return both distance from Path and Bearing from Path
     * 
     * DistanceFromPath - negative values indicate areas left of path
     * BearingFromPath - negative values indicate headings left of path's heading
     */
    public class PathError {
        public Length DistanceFromPath;
        public Angle BearingFromPath;
        public NavyAngle BearingAtPath;
    }
    
    
    /* length()
     * Function returns the entire length of the path
     */
    public Length length();
    
    /* getPathError(AbsolutePosition pos)
     * Function returns the distance from Path, along with bearing from Path
     */
    public PathError getPathError(AbsolutePosition currPos, NavyAngle currHeading);
    
    /* start()
     * Function returns the starting location of a path
     */
    public AbsolutePosition start();
    
    /* end()
     * Function returns the ending location of a path
     */
    public AbsolutePosition end();
    
    /* startBearing()
     * Function returns the start bearing location of a path
     */
    public NavyAngle startBearing();
    
    /* endBearing()
     * Function returns the end bearing location of a path
     */
    public NavyAngle endBearing();
    
    
}
