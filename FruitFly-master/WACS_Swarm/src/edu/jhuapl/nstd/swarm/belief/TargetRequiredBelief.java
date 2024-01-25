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
public class TargetRequiredBelief  extends Belief
{
    public static final String BELIEF_NAME = "TargetRequiredBelief";

    public TargetRequiredBelief()
    {
        timestamp = new Date ();
    }

    @Override
    public void addBelief(Belief b)
    {
        TargetRequiredBelief belief = (TargetRequiredBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
        }
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
