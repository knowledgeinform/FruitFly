/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.tase.RecorderStatus;
import java.util.Date;
/**
 *
 * @author xud1
 */
public class VideoClientRecorderStatusBeliefSatComm extends VideoClientRecorderStatusBelief
{
    public static final String BELIEF_NAME = VideoClientRecorderStatusBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public VideoClientRecorderStatusBeliefSatComm()
    {
        super();
    }
    
    public VideoClientRecorderStatusBeliefSatComm(String agentID, RecorderStatus status)
    {
        super(agentID, status);
    }
    
    public VideoClientRecorderStatusBeliefSatComm(String agentID, RecorderStatus status, Date timeStamp)
    {
        super(agentID, status, timeStamp);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
