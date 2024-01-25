/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.path;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import java.util.*;

/**
 *
 * @author colejg1
 */
public class CompositePath2D implements Path2D {
    protected List<Path2D> _paths;
    
    public CompositePath2D(List<Path2D> paths) throws IllegalArgumentException {
        _paths = paths;
        if (_paths.isEmpty())
            throw new IllegalArgumentException("Composite Path must be instatiated with at least one path");
    }

    @Override
    public Length length() {
        Length tot_len = Length.ZERO;
        for (Path2D path : _paths)
            tot_len = tot_len.plus(path.length());
        return tot_len;
    }

    @Override
    public PathError getPathError(AbsolutePosition currPos, NavyAngle currHeading) {
        PathError err = new PathError();
        err.DistanceFromPath = Length.POSITIVE_INFINITY;
        
        for (Path2D path : _paths) {
            PathError pe = path.getPathError(currPos, currHeading);
            if (pe.DistanceFromPath.abs().compareTo(err.DistanceFromPath.abs()) < 0)
                err = pe;
        }
        return err;
    }

    @Override
    public AbsolutePosition start() {
        return _paths.get(0).start();
    }

    @Override
    public AbsolutePosition end() {
        return _paths.get(_paths.size()-1).end();
    }
    
    @Override
    public NavyAngle startBearing() {
        return _paths.get(0).startBearing();
    }

    @Override
    public NavyAngle endBearing() {
        return _paths.get(_paths.size()-1).endBearing();
    }
    
    
}
