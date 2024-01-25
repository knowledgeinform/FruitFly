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
public class GimbalDeployBelief extends Belief  implements BeliefExternalizable
{
        public static final String BELIEF_NAME = "GimbalDeployBelief";

    boolean _deployed;

    public GimbalDeployBelief(String agentID, boolean deployed)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _deployed = deployed;
    }

    public void setGimbalDeployed()
    {
        _deployed = true;
    }

    public void setGimbalRetracted()
    {
        _deployed = false;
    }

    public boolean isDeployed()
    {
        return _deployed;
    }


    @Override
    protected void addBelief(Belief b)
    {
         GimbalDeployBelief belief = (GimbalDeployBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._deployed = belief.isDeployed();
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
