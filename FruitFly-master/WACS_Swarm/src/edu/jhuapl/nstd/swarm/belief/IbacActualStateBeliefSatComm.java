/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class IbacActualStateBeliefSatComm extends IbacActualStateBelief
{
    public static final String BELIEF_NAME = IbacActualStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public IbacActualStateBeliefSatComm(String agentID,boolean s)
    {
        super(agentID, s);
    }
    
    public IbacActualStateBeliefSatComm(String agentID,boolean s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
