/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

/**
 *
 * @author humphjc1
 */
public class WindEstimateSourceCommandedBelief extends WindEstimateSourceBaseBelief
{
    public static final String BELIEF_NAME = "WindEstimateSourceCommandedBelief";

    public WindEstimateSourceCommandedBelief()
    {
        super ();
    }
    
    public WindEstimateSourceCommandedBelief(int newWindSource)
    {
        super (newWindSource);
    }

    public WindEstimateSourceCommandedBelief(int newWindSource, long timestampMs)
    {
        super(newWindSource, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
