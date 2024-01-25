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
public class WACSShutdownBelief extends Belief  implements BeliefExternalizable
{
        public static final String BELIEF_NAME = "WACSShutdownBelief";

    boolean _shutdown;

    public WACSShutdownBelief(String agentID, boolean shutdown)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _shutdown = shutdown;
    }

    public void setToShutDown()
    {
        _shutdown = true;
    }

    public void setToPowerUp()
    {
        _shutdown = false;
    }

    public boolean isShutDown()
    {
        return _shutdown;
    }


    @Override
    protected void addBelief(Belief b)
    {
         WACSShutdownBelief belief = (WACSShutdownBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._shutdown = belief.isShutDown();
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
