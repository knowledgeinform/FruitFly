/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

/**
 *
 * @author humphjc1
 */
public class WindEstimateSourceActualBelief extends WindEstimateSourceBaseBelief
{
    public static final String BELIEF_NAME = "WindEstimateSourceActualBelief";

    public WindEstimateSourceActualBelief()
    {
        super ();
    }
    
    public WindEstimateSourceActualBelief(int newWindSource)
    {
        super (newWindSource);
    }

    public WindEstimateSourceActualBelief(int newWindSource, long timestampMs)
    {
        super(newWindSource, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
