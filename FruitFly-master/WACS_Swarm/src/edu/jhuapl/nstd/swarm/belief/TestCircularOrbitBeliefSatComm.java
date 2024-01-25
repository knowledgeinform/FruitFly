/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class TestCircularOrbitBeliefSatComm extends TestCircularOrbitBelief
{
    /**
   * The unique name for this belief type.
   */	
  public static final String BELIEF_NAME = TestCircularOrbitBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;
  
  /*
     * Used when recreating this object in a different environment
     * Class fields must be initialized in readExternal
     */
    public TestCircularOrbitBeliefSatComm()
    {
        super();
    }

    /**
     * 
     * Note: minimum safe altitude is enforced through safety box function getSafeCircularOrbit(CircularOrbitBelief)
     * @param agentId
     * @param centerPosition
     * @param radius
     * @param isClockwise 
     */
    public TestCircularOrbitBeliefSatComm(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise)
    {
        super(agentId, centerPosition, radius, isClockwise);
        
    }
    //TODO make this the only option for a constructor
    public TestCircularOrbitBeliefSatComm(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, double mps, double windSouth, double windWest)
    {
        super (agentId, centerPosition, radius, isClockwise, mps, windSouth, windWest);
    }
    
    //TODO make this the only option for a constructor
    /*
     * Use this constructor for circularOrbitBelief
     */
    public TestCircularOrbitBeliefSatComm(final String agentId, final AbsolutePosition centerPosition, final Length radius, final boolean isClockwise, double mps, double windSouth, double windWest, Date time)
    {
        super (agentId, centerPosition, radius, isClockwise, mps, windSouth, windWest, time);
    }
    
    
    /*Use this constructgor for racettrack
     * 
     */
    public TestCircularOrbitBeliefSatComm(final String agentId, Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise,double velocity, double windSouth, double windWest, LatLonAltPosition gimbleTarget)
    {
        super (agentId, lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise, velocity, windSouth, windWest, gimbleTarget);
    }
    
    public TestCircularOrbitBeliefSatComm(final String agentId, Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise,double velocity, double windSouth, double windWest, Date time, LatLonAltPosition gimbleTarget)
    {
        super (agentId, lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise, velocity, windSouth, windWest, time, gimbleTarget);
    }
    
    
  
  @Override
    public String getName() 
    {
        return BELIEF_NAME;
    }
}
