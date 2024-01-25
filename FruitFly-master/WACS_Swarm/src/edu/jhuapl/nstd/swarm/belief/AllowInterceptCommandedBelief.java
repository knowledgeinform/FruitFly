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
public class AllowInterceptCommandedBelief extends AllowInterceptBaseBelief
{
    public static final String BELIEF_NAME = "AllowInterceptCommandedBelief";

    public AllowInterceptCommandedBelief(String agentID,Boolean s)
    {
        super (agentID, s);
    }
    
    public AllowInterceptCommandedBelief(String agentID,Boolean s, Date time)
    {
        super (agentID, s, time);
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
