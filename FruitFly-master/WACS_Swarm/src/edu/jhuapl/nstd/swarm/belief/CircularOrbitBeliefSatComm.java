/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class CircularOrbitBeliefSatComm  extends CircularOrbitBelief
{
    public static final String BELIEF_NAME = CircularOrbitBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    
    public CircularOrbitBeliefSatComm()
    {
        super();
    }

    public CircularOrbitBeliefSatComm(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise)
    {
        super (agentId, centerPosition, radius, isClockwise);
        
    }
    
    public CircularOrbitBeliefSatComm(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, Date time)
    {
        super(agentId, centerPosition, radius, isClockwise, time);
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
      if (belief == null || !belief.getClass().equals(this.getClass()))
          return false;
          
      CircularOrbitBelief coBelief = (CircularOrbitBelief)belief;
      return (getPosition().equals (coBelief.getPosition()) && 
              getRadius().equals (coBelief.getRadius()) && 
              getIsClockwise() == coBelief.getIsClockwise());
  }
}
