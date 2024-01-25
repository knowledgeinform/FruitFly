/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import static edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief.BELIEF_NAME;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AllowInterceptBaseBelief extends Belief
{
    public static final String BELIEF_NAME = "AllowInterceptBaseBelief";

    protected boolean m_AllowIntercept;

    public AllowInterceptBaseBelief(String agentID,boolean s)
    {
        this (agentID, s, new Date(System.currentTimeMillis()));
    }
    
    public AllowInterceptBaseBelief(String agentID,boolean s, Date time)
    {
        super(agentID);
        timestamp = time;
        m_AllowIntercept = s;
    }

    public void setAllow(boolean s)
    {
        m_AllowIntercept = s;
    }

    public boolean getAllow()
    {
        return m_AllowIntercept;
    }


    @Override
    protected void addBelief(Belief b)
    {
         AllowInterceptBaseBelief belief = (AllowInterceptBaseBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_AllowIntercept = belief.getAllow();
        }
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof AllowInterceptBaseBelief))
        {
            return false;
        }
        return (getAllow() == ((AllowInterceptBaseBelief)obj).getAllow());
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
