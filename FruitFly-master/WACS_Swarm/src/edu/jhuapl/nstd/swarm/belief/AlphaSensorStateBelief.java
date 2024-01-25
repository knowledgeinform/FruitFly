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
public class AlphaSensorStateBelief extends Belief
{
        public static final String BELIEF_NAME = "AlphaSensorStateBelief";

    protected boolean _AlphaOn;

    public AlphaSensorStateBelief(String agentID,Boolean s)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
    }
    
    public AlphaSensorStateBelief(String agentID,Boolean s, Date time)
    {
        super(agentID);
        timestamp = time;
        _AlphaOn = s;
    }

    public void setState(boolean s)
    {
        _AlphaOn = s;
    }

    public boolean getState()
    {
        return _AlphaOn;
    }


    public String getStateText()
    {
        if(_AlphaOn)
            return "Pump On";
        else
            return "Pump Off";
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

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof AlphaSensorStateBelief))
        {
            return false;
        }
        return (getState() == ((AlphaSensorStateBelief)obj).getState());
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
