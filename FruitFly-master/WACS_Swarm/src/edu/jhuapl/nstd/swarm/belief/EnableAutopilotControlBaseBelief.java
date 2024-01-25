/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import static edu.jhuapl.nstd.swarm.belief.AllowInterceptBaseBelief.BELIEF_NAME;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class EnableAutopilotControlBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "EnableAutopilotControlBaseBelief";

    protected boolean m_ControlAutopilot;

    public EnableAutopilotControlBaseBelief(String agentID,boolean s)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
    }
    
    public EnableAutopilotControlBaseBelief(String agentID,boolean s, Date time)
    {
        super(agentID);
        timestamp = time;
        m_ControlAutopilot = s;
    }

    public void setAllow(boolean s)
    {
        m_ControlAutopilot = s;
    }

    public boolean getAllow()
    {
        return m_ControlAutopilot;
    }


    @Override
    protected void addBelief(Belief b)
    {
         EnableAutopilotControlBaseBelief belief = (EnableAutopilotControlBaseBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_ControlAutopilot = belief.getAllow();
        }
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof EnableAutopilotControlBaseBelief))
        {
            return false;
        }
        return (getAllow() == ((EnableAutopilotControlBaseBelief)obj).getAllow());
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
