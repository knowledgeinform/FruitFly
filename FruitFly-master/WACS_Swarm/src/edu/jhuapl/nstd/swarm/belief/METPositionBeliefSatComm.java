/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class METPositionBeliefSatComm extends METPositionBelief
{
    /**
     * The unique string for this belief type.
     */
    public static final String BELIEF_NAME = METPositionBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    public METPositionBeliefSatComm()
    {
        this ("unspecified");
    }
    
    /**
     * Empty constructor.
     */
    public METPositionBeliefSatComm(String agentID)
    {
        super(agentID);
    }
    
    public METPositionBeliefSatComm(String agentID, METPositionTimeName met)
    {
        super(agentID, met);
    }

    public METPositionBeliefSatComm(String agentID, METPositionTimeName met, Date time)
    {
      super(agentID, met, time);
    }
    
    @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
