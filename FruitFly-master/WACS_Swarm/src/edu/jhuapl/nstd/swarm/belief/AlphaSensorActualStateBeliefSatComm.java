/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AlphaSensorActualStateBeliefSatComm extends AlphaSensorActualStateBelief
{
    public static final String BELIEF_NAME = AlphaSensorActualStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AlphaSensorActualStateBeliefSatComm(String agentID, boolean s)
    {
        super(agentID, s);
    }
    
    public AlphaSensorActualStateBeliefSatComm(String agentID, boolean s, Date time)
    {
        super (agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
