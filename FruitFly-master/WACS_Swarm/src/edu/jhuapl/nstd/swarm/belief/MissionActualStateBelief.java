/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class MissionActualStateBelief extends MissionBaseStateBelief
{
    public static final String BELIEF_NAME = "MissionActualStateBelief";
    
    public MissionActualStateBelief(String agentID,int state)
    {
        super (agentID, state);
    }
    
    public MissionActualStateBelief(String agentID,int state, Date time)
    {
        super (agentID, state, time);
    }

    
      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
    @Override
  public String getName()
  {
    return BELIEF_NAME;
  }
}
