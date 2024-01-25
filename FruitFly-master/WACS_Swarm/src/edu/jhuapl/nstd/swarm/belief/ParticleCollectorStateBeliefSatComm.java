/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class ParticleCollectorStateBeliefSatComm extends ParticleCollectorStateBelief
{
    public static final String BELIEF_NAME = ParticleCollectorStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ParticleCollectorStateBeliefSatComm(String agentID,ParticleCollectorMode s, Date time)
    {
        super (agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}