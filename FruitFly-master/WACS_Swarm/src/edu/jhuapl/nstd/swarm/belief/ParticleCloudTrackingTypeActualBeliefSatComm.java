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
public class ParticleCloudTrackingTypeActualBeliefSatComm extends ParticleCloudTrackingTypeActualBelief
{
    public static final String BELIEF_NAME = ParticleCloudTrackingTypeActualBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ParticleCloudTrackingTypeActualBeliefSatComm(ParticleCloudPredictionBehavior.TRACKING_TYPE newTrackingType, long timestampMs)
    {
        super (newTrackingType, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
