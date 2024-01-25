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
public class VideoClientSatCommFrameRequestBeliefSatComm extends VideoClientSatCommFrameRequestBelief
{
    public static final String BELIEF_NAME = VideoClientSatCommFrameRequestBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientSatCommFrameRequestBeliefSatComm()
    {
        super();
    }
    
    public VideoClientSatCommFrameRequestBeliefSatComm(String agentID, int receipt, boolean lastInterlaceReceived)
    {
        super(agentID, receipt, lastInterlaceReceived);        
    }
    
    public VideoClientSatCommFrameRequestBeliefSatComm(String agentID, int receipt, boolean lastInterlaceReceived, Date timestamp)
    {
        super(agentID, receipt, lastInterlaceReceived, timestamp);        
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
