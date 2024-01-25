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
public class AllowInterceptCommandedBeliefSatComm extends AllowInterceptCommandedBelief
{
    public static final String BELIEF_NAME = AllowInterceptCommandedBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AllowInterceptCommandedBeliefSatComm(String agentID,boolean s)
    {
        super (agentID, s);
    }
    
    public AllowInterceptCommandedBeliefSatComm(String agentID,boolean s, Date time)
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
