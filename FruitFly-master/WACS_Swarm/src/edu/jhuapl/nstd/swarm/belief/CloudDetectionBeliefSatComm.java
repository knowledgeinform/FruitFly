/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author humphjc1
 */
public class CloudDetectionBeliefSatComm extends CloudDetectionBelief
{
    public static final String BELIEF_NAME = CloudDetectionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public CloudDetectionBeliefSatComm(String agentID, LatLonAltPosition pos, double value, short source, short id, short rawValue, long timeMs)
    {
        super (agentID, pos, value, source, id, rawValue, timeMs);
    }
    
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
