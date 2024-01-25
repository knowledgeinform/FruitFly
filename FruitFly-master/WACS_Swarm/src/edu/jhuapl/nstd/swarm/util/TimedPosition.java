//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================

// File created: Tue Nov  9 13:10:34 1999

package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;

import java.lang.*;



import java.util.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.math.random.*;


import edu.jhuapl.jlib.net.*;

/**
 * This code estimates a postion based on the previous 5 positions
 * 
 * <P>
 * UNCLASSIFIED
 * 
 * @author Chad Hawthorne ext. 3728
 */

public class TimedPosition implements Comparable<TimedPosition> {
    
    private Date time = null;
    private AbsolutePosition p = null;
    
    public TimedPosition(Date time, AbsolutePosition p){
        this.time = time;
        this.p = p;
    }
    
    public int compareTo(TimedPosition t){
        return time.compareTo(t.getTime());
    }
    
    public Date getTime(){
        return time;
    }
    
    public AbsolutePosition getPosition(){
        return p;
    }
}

//=============================== UNCLASSIFIED ==================================

