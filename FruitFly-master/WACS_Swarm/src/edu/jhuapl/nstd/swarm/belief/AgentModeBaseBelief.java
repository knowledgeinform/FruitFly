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

package edu.jhuapl.nstd.swarm.belief;

import java.io.*;
import java.util.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.*;

/**
 * This beleif contains the mode of all agents in the swarm.
 *
 * <P>UNCLASSIFIED
 * @author Chad Hawthorne ext. 3728
 */

public class AgentModeBaseBelief extends TimeNameBelief {

  /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = "AgentModeBaseBelief";

  /**
   * Constructor.
   */  
  public AgentModeBaseBelief(){
    degradeProperty =  new String("modebelief.decaytime");
  }

  /**
   * Constructor used to create a new mode belief for a particular agent.
   * @param agentID The unique agent id that generated this belief.
   * @param mode The mode.
   */  
  public AgentModeBaseBelief(String agentID, Mode mode)
  {
    super(agentID);
    timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
    ModeTimeName name = new ModeTimeName(mode, new Date(timestamp.getTime()), agentID);
    elements.put(agentID, name);
    degradeProperty =  new String("modebelief.decaytime");
  }
  
  public AgentModeBaseBelief(String agentID, String mode)
  {
      this (agentID, mode, new Date(TimeManagerFactory.getTimeManager().getTime()));
  }
  
  public AgentModeBaseBelief(String agentID, String mode, Date time)
  {
    super(agentID);
    timestamp = time;
    ModeTimeName name = new ModeTimeName(new Mode(mode), new Date(timestamp.getTime()), agentID);
    elements.put(agentID, name);
    degradeProperty =  new String("modebelief.decaytime");
  }

  /**
   * Returns a timestamped mode information for the agent id.
   * @param agentID The agentid that we want mode information for.
   * @return A timestamped mode.
   */  
  public synchronized ModeTimeName getModeTimeName(String agentID){
    return (ModeTimeName)elements.get(agentID);
  }
  
  /**
   * Returns a timestamped mode information for the agent id.
   * @param agentID The agentid that we want mode information for.
   * @return A timestamped mode.
   */  
  public synchronized Mode getMode(String agentID){
      ModeTimeName mtn = ((ModeTimeName)elements.get(agentID));
      return (mtn == null) ? null : mtn.getMode();
  }

  /**
   * Returns the unique name for this belief type.
   * @return
   */  
  public String getName(){
    return BELIEF_NAME;
  }


	protected TimeName readTimeName(DataInput in) throws IOException {
		ModeTimeName tn = new ModeTimeName();
		tn.readExternal(in);
		return tn;
	}


}
//=============================== UNCLASSIFIED ==================================
