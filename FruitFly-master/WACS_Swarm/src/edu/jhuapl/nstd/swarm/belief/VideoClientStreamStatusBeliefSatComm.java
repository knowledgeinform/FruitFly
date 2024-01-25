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
public class VideoClientStreamStatusBeliefSatComm extends VideoClientStreamStatusBelief
{
    public static final String BELIEF_NAME = VideoClientStreamStatusBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientStreamStatusBeliefSatComm(String agentID, boolean status)
    {
        super(agentID, status);
    }
    
    public VideoClientStreamStatusBeliefSatComm(String agentID, boolean status, Date timeStamp)
    {
        super(agentID, status, timeStamp);
    }
       
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    } 
}
