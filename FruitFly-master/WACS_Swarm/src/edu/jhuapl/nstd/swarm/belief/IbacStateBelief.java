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
public class IbacStateBelief extends Belief
{
    public static final String BELIEF_NAME = "IbacStateBelief";
    
    protected boolean _IbacOn;

    public IbacStateBelief(String agentID,Boolean s)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
    }
    
    public IbacStateBelief(String agentID,Boolean s, Date time)
    {
        super(agentID);
        timestamp = time;
        _IbacOn = s;
    }

    public void setState(boolean s)
    {
        _IbacOn = s;
    }

    public boolean getState()
    {
        return _IbacOn;
    }


    public String getStateText()
    {
        if(_IbacOn)
            return "Pump On";
        else
            return "Pump Off";
    }
    

    @Override
    protected void addBelief(Belief b)
    {
         IbacStateBelief belief = (IbacStateBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._IbacOn = belief.getState();
        }
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof IbacStateBelief))
        {
            return false;
        }
        return (getState()== ((IbacStateBelief)obj).getState());
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
