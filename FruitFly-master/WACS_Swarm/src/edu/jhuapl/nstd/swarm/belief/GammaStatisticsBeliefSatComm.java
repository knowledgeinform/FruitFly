/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class GammaStatisticsBeliefSatComm extends GammaStatisticsBelief
{
    public static final String BELIEF_NAME = GammaStatisticsBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public GammaStatisticsBeliefSatComm(String agentID)
    {
        super (agentID);
    }
    
    public GammaStatisticsBeliefSatComm(String agentID, Date time)
    {
        super(agentID, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
