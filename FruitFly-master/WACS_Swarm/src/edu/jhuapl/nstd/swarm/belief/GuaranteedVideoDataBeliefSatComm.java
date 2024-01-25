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
public class GuaranteedVideoDataBeliefSatComm extends GuaranteedVideoDataBelief
{
    public static final String BELIEF_NAME = GuaranteedVideoDataBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
    
    public GuaranteedVideoDataBeliefSatComm()
    {
        super();
    }
    
    public GuaranteedVideoDataBeliefSatComm(String agentID, int size, byte[] data)
    {
        super(agentID, size, data);
    }
    
    public GuaranteedVideoDataBeliefSatComm(String agentID, int size, byte[] data, Date timestamp)
    {
        super(agentID, size, data, timestamp);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }    
}
