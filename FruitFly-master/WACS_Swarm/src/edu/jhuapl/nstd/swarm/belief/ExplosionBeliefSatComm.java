/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.position.AbsolutePosition;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class ExplosionBeliefSatComm extends ExplosionBelief
{
    public static final String BELIEF_NAME = ExplosionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public ExplosionBeliefSatComm()
    {
        super ();
    }

    public ExplosionBeliefSatComm(AbsolutePosition location, long time_ms)
    {
        super(location, time_ms);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
