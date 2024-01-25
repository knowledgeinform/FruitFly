/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage;

/**
 *
 * @author humphjc1
 */
public class AnacondaDetectionBeliefSatComm extends AnacondaDetectionBelief
{
    public static final String BELIEF_NAME = AnacondaDetectionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public AnacondaDetectionBeliefSatComm(String agentID, anacondaLCDReportMessage.AnacondaDataPair []lcda, anacondaLCDReportMessage.AnacondaDataPair []lcdb, long timestampMs)
    {
        super(agentID, lcda, lcdb, timestampMs);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
