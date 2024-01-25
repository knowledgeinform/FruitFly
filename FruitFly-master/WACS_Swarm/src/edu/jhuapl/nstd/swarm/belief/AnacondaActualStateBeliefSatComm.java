/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class AnacondaActualStateBeliefSatComm extends AnacondaActualStateBelief
{
    public static final String BELIEF_NAME = AnacondaActualStateBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AnacondaActualStateBeliefSatComm(String agentID,AnacondaModeEnum s)
    {
        super(agentID, s);
    }
    
    public AnacondaActualStateBeliefSatComm(String agentID,AnacondaModeEnum s, Date time)
    {
        super(agentID, s, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
