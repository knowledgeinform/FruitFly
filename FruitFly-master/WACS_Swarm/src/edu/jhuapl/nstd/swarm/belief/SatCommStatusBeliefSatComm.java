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
public class SatCommStatusBeliefSatComm extends Belief
{
    public static final String BELIEF_NAME = "SatCommStatusBelief" + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public static final int MODEMSTATE_INIT = 0;
    public static final int MODEMSTATE_CONNECT = 2;
    public static final int MODEMSTATE_RUN = 4;
    public static final int MODEMSTATE_RESET = 5;
    
    int m_ModemState;
    boolean m_ModemStatus;
    int m_LatencyMs;
    long m_BackLogBytes;
    
    public SatCommStatusBeliefSatComm(int modemState, boolean modemStatus, int latencyMs, long backLogBytes)
    {
        timestamp = new Date(System.currentTimeMillis());
        m_ModemState = modemState;
        m_ModemStatus = modemStatus;
        m_LatencyMs = latencyMs;
        m_BackLogBytes = backLogBytes;
    }

    public int getModemState ()
    {
        return m_ModemState;
    }
    
    public boolean getModemStatus ()
    {
        return m_ModemStatus;
    }
    
    public int getLatencyMs ()
    {
        return m_LatencyMs;
    }
    
    public long getBackLogBytes ()
    {
        return m_BackLogBytes;
    }

    @Override
    protected void addBelief(Belief b)
    {
         SatCommStatusBeliefSatComm belief = (SatCommStatusBeliefSatComm)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this.m_ModemState = belief.m_ModemState;
          this.m_ModemStatus = belief.m_ModemStatus;
          this.m_LatencyMs = belief.m_LatencyMs;
          this.m_BackLogBytes = belief.m_BackLogBytes;
          
        }
    }

    
      /**
   * Returns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }
}
