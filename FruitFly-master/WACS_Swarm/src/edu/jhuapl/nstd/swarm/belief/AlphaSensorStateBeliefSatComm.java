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
public class AlphaSensorStateBeliefSatComm extends AlphaSensorStateBelief
{
    public static final String BELIEF_NAME = AlphaSensorStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AlphaSensorStateBeliefSatComm(String agentID,boolean s)
    {
        super(agentID, s);
    }
    
    public AlphaSensorStateBeliefSatComm(String agentID,boolean s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
