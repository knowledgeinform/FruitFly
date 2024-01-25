/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;
/**
 *
 * @author xud1
 */
public class VideoClientFrameRequestBeliefSatComm extends VideoClientFrameRequestBelief
{
    public static final String BELIEF_NAME = VideoClientFrameRequestBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientFrameRequestBeliefSatComm(String agentID)
    {
        super(agentID);
    }
    
    public VideoClientFrameRequestBeliefSatComm(String agentID, Date time)
    {
        super(agentID, time);
    }
    
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }    
}
