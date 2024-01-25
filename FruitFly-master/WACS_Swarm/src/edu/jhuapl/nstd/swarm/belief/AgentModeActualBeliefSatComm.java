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
public class AgentModeActualBeliefSatComm extends AgentModeActualBelief
{
    /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = AgentModeActualBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

  /**
   * Constructor.
   */  
  public AgentModeActualBeliefSatComm(){
    super ();
  }

  /**
   * Constructor used to create a new mode belief for a particular agent.
   * @param agentID The unique agent id that generated this belief.
   * @param mode The mode.
   */  
  public AgentModeActualBeliefSatComm(String agentID, Mode mode)
  {
    super (agentID, mode);
  }
  
  public AgentModeActualBeliefSatComm(String agentID, String mode)
  {
      super (agentID, mode);
  }
  
  public AgentModeActualBeliefSatComm(String agentID, String mode, Date time)
  {
    super (agentID, mode, time);
  }
  
  @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
  
  /**
   * Check if this belief's data matches exactly the data for another belief, excluding the belief timestamp
   * @param belief
   * @return Check 
   */
    @Override
  public boolean equals (Object belief)
  {
      return false;
  }
    
}
