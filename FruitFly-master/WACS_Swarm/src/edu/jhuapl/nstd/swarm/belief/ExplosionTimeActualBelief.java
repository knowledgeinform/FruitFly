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
public class ExplosionTimeActualBelief extends ExplosionTimeBaseBelief
{
    public static final String BELIEF_NAME = "ExplosionTimeActualBelief";

    public ExplosionTimeActualBelief()
    {
        super();
    }

    public ExplosionTimeActualBelief(String agentId)
    {
        super(agentId);
    }

    public ExplosionTimeActualBelief(String agentId, long time_ms)
    {
        super (agentId, time_ms);
    }
            
    public ExplosionTimeActualBelief(String agentId, long time_ms, Date time)
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
