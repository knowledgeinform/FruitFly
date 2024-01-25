/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class IrExplosionAlgorithmEnabledBeliefSatComm extends IrExplosionAlgorithmEnabledBelief
{
    public static final String BELIEF_NAME = IrExplosionAlgorithmEnabledBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public IrExplosionAlgorithmEnabledBeliefSatComm ()
    {
        super("unspecified");
    }
        
    public IrExplosionAlgorithmEnabledBeliefSatComm(String agentID)
    {
        super (agentID);
    }

    public IrExplosionAlgorithmEnabledBeliefSatComm(String agentID, boolean s, long timeUntilExpMs)
    {
        super(agentID, s, timeUntilExpMs);
    }
            
    public IrExplosionAlgorithmEnabledBeliefSatComm(String agentID, boolean s, long timeUntilExpMs, Date time)
    {
        super (agentID, s, timeUntilExpMs, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
