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
public class VideoClientStreamCmdBeliefSatComm extends VideoClientStreamCmdBelief
{
    public final static String BELIEF_NAME = VideoClientStreamCmdBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientStreamCmdBeliefSatComm()
    {
        super();
    }
    
    public VideoClientStreamCmdBeliefSatComm(String agentID, String clientIP, short clientPort, boolean stream)
    {
        super(agentID, clientIP, clientPort, stream);    
    }
 
    public VideoClientStreamCmdBeliefSatComm(String agentID, String clientIP, short clientPort, boolean stream, Date timeStamp)
    {
        super(agentID, clientIP, clientPort, stream, timeStamp);    
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }    
}
