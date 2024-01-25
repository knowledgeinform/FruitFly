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
public class ExplosionTimeActualBeliefSatComm extends ExplosionTimeActualBelief
{
    public static final String BELIEF_NAME = ExplosionTimeActualBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ExplosionTimeActualBeliefSatComm()
    {
        super();
    }

    public ExplosionTimeActualBeliefSatComm(String agentId)
    {
        super(agentId);
    }

    public ExplosionTimeActualBeliefSatComm(String agentId, long time_ms)
    {
        super (agentId, time_ms);
    }
            
    public ExplosionTimeActualBeliefSatComm(String agentId, long time_ms, Date time)
    {
        super (agentId, time_ms, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
