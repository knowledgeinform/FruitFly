/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AgentModeActualBelief extends AgentModeBaseBelief
{

    /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = "AgentModeActualBelief";

  /**
   * Constructor.
   */  
  public AgentModeActualBelief()
  {
    super ();
  }

  /**
   * Constructor used to create a new mode belief for a particular agent.
   * @param agentID The unique agent id that generated this belief.
   * @param mode The mode.
   */  
  public AgentModeActualBelief(String agentID, Mode mode)
  {
    super(agentID, mode);
  }
  
  public AgentModeActualBelief(String agentID, String mode)
  {
    super (agentID, mode);
  }
  
  public AgentModeActualBelief(String agentID, String mode, Date time)
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
