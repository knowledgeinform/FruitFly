/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class ParticleCollectorActualStateBelief extends ParticleCollectorStateBelief
{

     public static final String BELIEF_NAME = "ParticleCollectorActualStateBelief";

    public ParticleCollectorActualStateBelief(String agentID,ParticleCollectorMode s, boolean s1, boolean s2, boolean s3, boolean s4)
    {
        super(agentID, s, s1, s2, s3, s4);
    }
    
    public ParticleCollectorActualStateBelief(String agentID,ParticleCollectorMode s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    protected void addBelief(Belief b)
    {
        ParticleCollectorActualStateBelief belief = (ParticleCollectorActualStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._state = belief.getParticleCollectorState();
          this.setSample1full(belief.isSample1full());
          this.setSample2full(belief.isSample2full());
          this.setSample3full(belief.isSample3full());
          this.setSample4full(belief.isSample4full());
        }
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
