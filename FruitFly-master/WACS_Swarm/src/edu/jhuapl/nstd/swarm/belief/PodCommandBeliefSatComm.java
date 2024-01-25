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
public class PodCommandBeliefSatComm extends PodCommandBelief
{
    public static final String BELIEF_NAME = PodCommandBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public PodCommandBeliefSatComm(String agentID, int commandcode, long timestampMs)
    {
        super(agentID, commandcode, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
