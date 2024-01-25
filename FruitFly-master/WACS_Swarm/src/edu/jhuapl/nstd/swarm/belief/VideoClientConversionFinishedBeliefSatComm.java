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
public class VideoClientConversionFinishedBeliefSatComm extends VideoClientConversionFinishedBelief
{
    public static final String BELIEF_NAME = VideoClientConversionFinishedBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientConversionFinishedBeliefSatComm(String agentID)
    {
        super(agentID);
    }
    
    public VideoClientConversionFinishedBeliefSatComm(String agentID, Date timeStamp)
    {
        super(agentID, timeStamp);
    }
       
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }     
}

