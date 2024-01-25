/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AgentPositionBeliefSatComm   extends AgentPositionBelief
{
    /**
   * The unique name for this belief type.
   */  
  public static final String BELIEF_NAME = AgentPositionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

  /**
   * Empty constructor.
   */  
  public AgentPositionBeliefSatComm() {
		super ();
  }

  /** 
   * Constructs a new belief for an agent.
   * @param agentID The agentID associated with the position.
   * @param pos The position of the agent.
   */  
  public AgentPositionBeliefSatComm(String agentID, AbsolutePosition pos)
  {
    super (agentID, pos);
  }
  
  public AgentPositionBeliefSatComm(String agentID, AbsolutePosition pos, NavyAngle heading)
  {
    super(agentID, pos, heading);
  }
 
  public AgentPositionBeliefSatComm(String agentID, AbsolutePosition pos, NavyAngle heading, Length error) {
    super (agentID, pos, heading, error);
  }

  public AgentPositionBeliefSatComm(String agentID, AbsolutePosition pos, NavyAngle heading, 
		  Length error, Date timestamp) 
  {
    super(agentID, pos, heading, error, timestamp);
  }

  
  @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
