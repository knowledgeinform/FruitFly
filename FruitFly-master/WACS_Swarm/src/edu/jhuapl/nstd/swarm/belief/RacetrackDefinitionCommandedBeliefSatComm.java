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
public class RacetrackDefinitionCommandedBeliefSatComm  extends RacetrackDefinitionCommandedBelief
{
    public static final String BELIEF_NAME = "RacetrackDefinitionCommandedBeliefSatComm";

    public RacetrackDefinitionCommandedBeliefSatComm()
    {
        super ();
    }

    public RacetrackDefinitionCommandedBeliefSatComm(LatLonAltPosition loiterPos)
    {
        super (loiterPos);
    }
    
    public RacetrackDefinitionCommandedBeliefSatComm(LatLonAltPosition startPos, long beliefTimestampMs)
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
