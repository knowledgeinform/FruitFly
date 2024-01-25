/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class AlphaSensorActualStateBelief extends AlphaSensorStateBelief
{

    public static final String BELIEF_NAME = "AlphaSensorActualStateBelief";

    public AlphaSensorActualStateBelief(String agentID, boolean s)
    {
        super(agentID, s);
    }
    
    public AlphaSensorActualStateBelief(String agentID, boolean s, Date time)
    {
        super (agentID, s, time);
    }

    @Override
    protected void addBelief(Belief b)
    {
         AlphaSensorStateBelief belief = (AlphaSensorStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._AlphaOn = belief.getState();
        }
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
