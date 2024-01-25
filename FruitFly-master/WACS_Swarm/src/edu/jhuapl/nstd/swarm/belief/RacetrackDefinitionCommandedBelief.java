/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.position.LatLonAltPosition;

/**
 *
 * @author humphjc1
 */
public class RacetrackDefinitionCommandedBelief  extends RacetrackDefinitionBaseBelief
{
    public static final String BELIEF_NAME = "RacetrackDefinitionCommandedBelief";

    public RacetrackDefinitionCommandedBelief()
    {
        super ();
    }

    public RacetrackDefinitionCommandedBelief(LatLonAltPosition loiterPos)
    {
        super (loiterPos);
    }
    
    public RacetrackDefinitionCommandedBelief(LatLonAltPosition startPos, long beliefTimestampMs)
    {
        super(startPos, beliefTimestampMs);
    }
    
    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
}
