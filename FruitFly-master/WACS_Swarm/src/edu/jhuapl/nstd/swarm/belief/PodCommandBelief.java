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
public class PodCommandBelief extends Belief
{
    public static final String BELIEF_NAME = "PodCommandBelief";

    private int m_CommandCode;


    public PodCommandBelief(String agentID, int commandcode)
    {
        this (agentID, commandcode, System.currentTimeMillis());
    }
    
    public PodCommandBelief(String agentID, int commandcode, long timestampMs)
    {
        super(agentID);
        timestamp = new Date(timestampMs);
        m_CommandCode = commandcode;
    }

    @Override
    protected void addBelief(Belief b)
    {
         PodCommandBelief belief = (PodCommandBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)> 0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_CommandCode = belief.getCommandCode();
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


    /**
     * @return the m_CommandCode
     */
    public int getCommandCode() {
        return m_CommandCode;
    }

    /**
     * @param m_CommandCode the m_CommandCode to set
     */
    public void setCommandCode(int m_CommandCode) {
        this.m_CommandCode = m_CommandCode;
    }

}
