/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.tase.TASE_Telemetry;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class TASETelemetryBeliefSatComm extends TASETelemetryBelief
{
    public static final String BELIEF_NAME = TASETelemetryBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    
    public TASETelemetryBeliefSatComm()
    {
        super();
    }

    public TASETelemetryBeliefSatComm(String agentID,TASE_Telemetry t)
    {
        super(agentID, t);
    }
    
    public TASETelemetryBeliefSatComm(String agentID,TASE_Telemetry t, Date time)
    {
        super(agentID, t, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
    
}
