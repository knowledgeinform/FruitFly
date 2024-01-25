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
public class IbacActualStateBelief extends IbacStateBelief
{
    public static final String BELIEF_NAME = "IbacActualStateBelief";

    public IbacActualStateBelief(String agentID,Boolean s)
    {
        super(agentID, s);
    }
    
    public IbacActualStateBelief(String agentID,Boolean s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    protected void addBelief(Belief b)
    {
         IbacActualStateBelief belief = (IbacActualStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._IbacOn = belief.getState();
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
