/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
import static edu.jhuapl.nstd.swarm.belief.ManualInterceptBaseOrbitBelief.BELIEF_NAME;

/**
 *
 * @author humphjc1
 */
public class ManualInterceptCommandedOrbitBelief extends ManualInterceptBaseOrbitBelief
{
    public static final String BELIEF_NAME = "ManualInterceptCommandedOrbitBelief";
    
    public ManualInterceptCommandedOrbitBelief ()
    {
        super ();
    }
    
    public ManualInterceptCommandedOrbitBelief (String agentName, Latitude interceptLatitude, Longitude interceptLongitude, boolean forceHoldPosition)
    {
        super (agentName, interceptLatitude, interceptLongitude, forceHoldPosition);
    }
    
    public ManualInterceptCommandedOrbitBelief (String agentName, boolean releaseImmediately)
    {
        super(agentName, releaseImmediately);
    }
    
    /**
     * Retuns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
}
