/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Length;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class WACSWaypointCommandedBelief extends WACSWaypointBaseBelief
{
    public static final String BELIEF_NAME = "WACSWaypointCommandedBelief";
    
    public WACSWaypointCommandedBelief()
    {
        super();
    }

    public WACSWaypointCommandedBelief(String agentID,  Altitude intersectalt, Length intersectrad, Altitude finalloiteralt, Altitude standoffloiteralt, Length loiterrad)
    {
        super(agentID, intersectalt, intersectrad, finalloiteralt, standoffloiteralt, loiterrad);
    }
            
    public WACSWaypointCommandedBelief(String agentID,  Altitude intersectalt, Length intersectrad, Altitude finalloiteralt, Altitude standoffloiteralt, Length loiterrad, Date time)
    {
        super (agentID, intersectalt, intersectrad, finalloiteralt, standoffloiteralt, loiterrad, time);
    }
    
    @Override
    public String getName() {
        return BELIEF_NAME;
    }
}
