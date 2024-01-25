/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class TargetActualBelief extends TargetBaseBelief
{

    /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = "TargetActualBelief";

  public TargetActualBelief()
  {
    super ();
  }
  
  /**
   * Constructor
   * @param agentID The id of the agent that created this belief.
   * @param pos The position of the target.
   * @param error The error associated with the target position.
   */	
  public TargetActualBelief(String agentID, AbsolutePosition pos, Length error)
  {
    super (agentID, pos, error);
  }
  
  /**
   * Builds a target belief, utilizing a unique target ID.
   *
   * @param agentID The id of the agent that created this belief.
   * @param pos The position of the target.
   * @param error The error associated with the target position.
   */	
  public TargetActualBelief(String agentID, AbsolutePosition pos, Length error, String targetID)
  {
        super (agentID, pos, error, targetID);
  }
  
  public TargetActualBelief(String agentID, AbsolutePosition pos, Length error, String targetID, Date time)
  {
      super (agentID, pos, error, targetID, time);
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
