/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class CBRNHeartbeatBeliefSatComm extends CBRNHeartbeatBelief
{
    public static final String BELIEF_NAME = CBRNHeartbeatBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    
    public CBRNHeartbeatBeliefSatComm ()
    {
        super();
    }
    
    public CBRNHeartbeatBeliefSatComm (String agentID, podHeartbeatMessage heartbeatPod0, podHeartbeatMessage heartbeatPod1)
    {
        super (agentID, heartbeatPod0, heartbeatPod1);
    }
    
    public CBRNHeartbeatBeliefSatComm (String agentID, podHeartbeatMessage heartbeatPod0, podHeartbeatMessage heartbeatPod1, Date time)
    {
        super (agentID, heartbeatPod0, heartbeatPod1, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
