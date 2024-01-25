/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class PiccoloTelemetryBeliefSatComm extends PiccoloTelemetryBelief
{
    public static final String BELIEF_NAME = PiccoloTelemetryBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public PiccoloTelemetryBeliefSatComm()
    {
        super();
    }

    public PiccoloTelemetryBeliefSatComm(String agentID,Pic_Telemetry t)
    {
        super(agentID, t);
    }
    
    public PiccoloTelemetryBeliefSatComm(String agentID,Pic_Telemetry t, Date time)
    {
        super (agentID, t, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
