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
public class VideoClientRecorderCmdBeliefSatComm extends VideoClientRecorderCmdBelief
{
    public static final String BELIEF_NAME = VideoClientRecorderCmdBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientRecorderCmdBeliefSatComm(String agentID, boolean cmd)
    {
        super(agentID, cmd);
    }
    
    public VideoClientRecorderCmdBeliefSatComm(String agentID, boolean cmd, Date time)
    {
        super(agentID, cmd, time);
    }
    
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }    
    
}
