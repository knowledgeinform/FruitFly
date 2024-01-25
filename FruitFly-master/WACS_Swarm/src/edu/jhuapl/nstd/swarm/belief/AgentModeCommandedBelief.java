/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AgentModeCommandedBelief extends AgentModeBaseBelief
{

    /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = "AgentModeCommandedBelief";

  /**
   * Constructor.
   */  
  public AgentModeCommandedBelief()
  {
    super ();
  }

  /**
   * Constructor used to create a new mode belief for a particular agent.
   * @param agentID The unique agent id that generated this belief.
   * @param mode The mode.
   */  
  public AgentModeCommandedBelief(String agentID, Mode mode)
  {
    super(agentID, mode);
  }
  
  public AgentModeCommandedBelief(String agentID, String mode)
  {
    super (agentID, mode);
  }
  
  public AgentModeCommandedBelief(String agentID, String mode, Date time)
  {
    super (agentID, mode, time);
  }
  
  /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }
}
