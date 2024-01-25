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
public class ExplosionTimeCommandedBeliefSatComm extends ExplosionTimeCommandedBelief
{
    public static final String BELIEF_NAME = ExplosionTimeCommandedBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ExplosionTimeCommandedBeliefSatComm()
    {
        super();
    }

    public ExplosionTimeCommandedBeliefSatComm(String agentId)
    {
        super(agentId);
    }

    public ExplosionTimeCommandedBeliefSatComm(String agentId, long time_ms)
    {
        super (agentId, time_ms);
    }
            
    public ExplosionTimeCommandedBeliefSatComm(String agentId, long time_ms, Date time)
    {
        super (agentId, time_ms, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
