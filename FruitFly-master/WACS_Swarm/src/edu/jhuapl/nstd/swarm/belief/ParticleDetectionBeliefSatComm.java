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
public class ParticleDetectionBeliefSatComm extends ParticleDetectionBelief
{
    public static final String BELIEF_NAME = ParticleDetectionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ParticleDetectionBeliefSatComm(String agentID, int lci, int sci, int blci, int bsci, long timeMs)
    {
        super (agentID, lci, sci, blci, bsci, timeMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
