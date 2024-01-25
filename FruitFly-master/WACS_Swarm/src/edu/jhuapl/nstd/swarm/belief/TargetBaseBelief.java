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

// File created: Tue Nov	9 13:10:34 1999

package edu.jhuapl.nstd.swarm.belief;

import java.lang.*;
import java.util.*;
import java.io.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;

/**
 * This is the belief that stores target information. 
 * 
 *
 * <P>UNCLASSIFIED
 * @author Chad Hawthorne ext. 3728
 */

public class TargetBaseBelief extends TimeNameBelief {
  
  /**
   * The unique name for this belief type.
   */	
  public static final String BELIEF_NAME = "TargetBaseBelief";
  
  /**
   * Empty constructor.
   */	
  public TargetBaseBelief(){
    degradeProperty =  new String("targetbelief.decaytime");
  }
  
  /**
   * Constructor
   * @param agentID The id of the agent that created this belief.
   * @param pos The position of the target.
   * @param error The error associated with the target position.
   */	
  public TargetBaseBelief(String agentID, AbsolutePosition pos, Length error){
    super(agentID);
    timestamp = new Date();
    // TODO should unknown targets be unique?
    PositionTimeName name = 
      new PositionTimeName(
					 pos.asLatLonAltPosition(), 
					 timestamp, 
					 TimeName.UNKNOWN_NAME, 
					 error); 
    synchronized(this){
      elements.put(TimeName.UNKNOWN_NAME, name);
    }
    degradeProperty =  new String("targetbelief.decaytime");
  }
  
  /**
   * Builds a target belief, utilizing a unique target ID.
   *
   * @param agentID The id of the agent that created this belief.
   * @param pos The position of the target.
   * @param error The error associated with the target position.
   */	
  public TargetBaseBelief(String agentID, AbsolutePosition pos, Length error, String targetID){
    this (agentID, pos, error, targetID, new Date());
  }
  
  /**
   * Builds a target belief, utilizing a unique target ID.
   *
   * @param agentID The id of the agent that created this belief.
   * @param pos The position of the target.
   * @param error The error associated with the target position.
   */	
  public TargetBaseBelief(String agentID, AbsolutePosition pos, Length error, String targetID, Date time){
    super(agentID);
    timestamp = new Date (time.getTime());
    PositionTimeName name = 
      new PositionTimeName(pos.asLatLonAltPosition(), timestamp, targetID, error);
    synchronized(this){
      elements.put(targetID, name);
    }
    degradeProperty =  new String("targetbelief.decaytime");
  }
  
  public synchronized PositionTimeName getPositionTimeName(String targetID){
    return (PositionTimeName)elements.get(targetID);
  }

	public synchronized List getOrderedByTime() {
		ArrayList list = (ArrayList)getAll();
		TimeComparator compare = new TimeComparator();
		Collections.sort(list,compare);
		return list;
	}
  
  /**
   * Returns the unique name of this belief type.
   * @return The unique name of this belief type.
   */	
  public String getName(){
    return BELIEF_NAME;
  }

	public class TimeComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			PositionTimeName p1 = (PositionTimeName)o1;
			PositionTimeName p2 = (PositionTimeName)o2;

			if (p1.getTime().after(p2.getTime()))
				return 1;
			if (p1.getTime().before(p2.getTime()))
				return -1;
			else 
				return 0;
		}
	}

	protected TimeName readTimeName(DataInput in) throws IOException {
		PositionTimeName ptn = new PositionTimeName();
		ptn.readExternal(in);
		return ptn;
	}

}
//=============================== UNCLASSIFIED ==================================
