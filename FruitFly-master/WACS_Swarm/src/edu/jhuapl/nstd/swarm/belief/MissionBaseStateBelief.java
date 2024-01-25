/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class MissionBaseStateBelief extends Belief
{
    public static final String BELIEF_NAME = "MissionBaseStateBelief";
    
    public static final int PREFLIGHT_STATE = 1;
    public static final int INGRESS_STATE = 2;
    public static final int SEARCH1_STATE = 3;
    public static final int SEARCH2_STATE = 4;
    public static final int SEARCH3_STATE = 5;
    public static final int SEARCH4_STATE = 6;
    public static final int EGRESS1_STATE = 7;
    public static final int EGRESS2_STATE = 8;
    public static final int EGRESS3_STATE = 9;
    public static final int EGRESS4_STATE = 10;
    public static final int MANUAL_STATE = 11;
    
    int m_State;

    public MissionBaseStateBelief(String agentID,int state)
    {
        this (agentID, state, new Date(System.currentTimeMillis()));
    }
    
    public MissionBaseStateBelief(String agentID,int state, Date time)
    {
        super(agentID);
        timestamp = time;
        m_State = state;
    }

    public void setState(int state)
    {
        m_State = state;
    }

    public int getState()
    {
        return m_State;
    }

    public String getStateText()
    {
        return getStateText (m_State);
    }
    
    public static String getStateText(int state)
    {
        String retval;
        
        switch(state)
        {
            case MANUAL_STATE:
                retval = "Manual";
                break;
            case PREFLIGHT_STATE:
                retval = "Preflight";
                break;
            case INGRESS_STATE:
                retval = "Ingress";
                break;
            case SEARCH1_STATE:
                retval = "Strike 1";
                break;
            case SEARCH2_STATE:
                retval = "Strike 2";
                break;
            case SEARCH3_STATE:
                retval = "Strike 3";
                break;
            case SEARCH4_STATE:
                retval = "Strike 4";
                break;
            case EGRESS1_STATE:
                retval = "Egress 1";
                break;
            case EGRESS2_STATE:
                retval = "Egress 2";
                break;
            case EGRESS3_STATE:
                retval = "Egress 3";
                break;
            case EGRESS4_STATE:
                retval = "Egress 4";
                break;
            default:
                retval = "Unknown";
        }
        return retval;
    }
    

    @Override
    protected void addBelief(Belief b)
    {
         MissionBaseStateBelief belief = (MissionBaseStateBelief)b;
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_State = belief.getState();
        }
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof MissionBaseStateBelief))
        {
            return false;
        }
        return (getState()== ((MissionBaseStateBelief)obj).getState());
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
