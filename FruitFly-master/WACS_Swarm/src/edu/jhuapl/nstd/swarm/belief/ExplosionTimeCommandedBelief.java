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
public class ExplosionTimeCommandedBelief extends ExplosionTimeBaseBelief
{
    public static final String BELIEF_NAME = "ExplosionTimeCommandedBelief";

    public ExplosionTimeCommandedBelief()
    {
        super();
    }

    public ExplosionTimeCommandedBelief(String agentId)
    {
        super(agentId);
    }

    public ExplosionTimeCommandedBelief(String agentId, long time_ms)
    {
        super (agentId, time_ms);
    }
            
    public ExplosionTimeCommandedBelief(String agentId, long time_ms, Date time)
    {
        super(agentId, time_ms, time);
    }

    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
}
