/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AnacondaStateBeliefSatComm extends AnacondaStateBelief
{
    public static final String BELIEF_NAME = AnacondaStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AnacondaStateBeliefSatComm (String agentID,AnacondaModeEnum s, Date time)
    {
        super (agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
