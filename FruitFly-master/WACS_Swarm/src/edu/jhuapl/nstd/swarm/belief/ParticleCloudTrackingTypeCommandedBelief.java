/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;

/**
 *
 * @author humphjc1
 */
public class ParticleCloudTrackingTypeCommandedBelief extends ParticleCloudTrackingTypeBaseBelief
{
    public static final String BELIEF_NAME = "ParticleCloudTrackingTypeCommandedBelief";

    public ParticleCloudTrackingTypeCommandedBelief()
    {
    }
    
    public ParticleCloudTrackingTypeCommandedBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE newTrackingType)
    {
        super (newTrackingType);
    }

    public ParticleCloudTrackingTypeCommandedBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE newTrackingType, long timestampMs)
    {
        super (newTrackingType, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
