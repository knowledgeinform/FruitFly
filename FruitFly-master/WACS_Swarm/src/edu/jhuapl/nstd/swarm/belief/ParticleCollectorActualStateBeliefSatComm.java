/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class ParticleCollectorActualStateBeliefSatComm extends ParticleCollectorActualStateBelief
{
    public static final String BELIEF_NAME = ParticleCollectorActualStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ParticleCollectorActualStateBeliefSatComm(String agentID,ParticleCollectorMode s, boolean s1, boolean s2, boolean s3, boolean s4)
    {
        super(agentID, s, s1, s2, s3, s4);
    }
    
    public ParticleCollectorActualStateBeliefSatComm(String agentID,ParticleCollectorMode s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
