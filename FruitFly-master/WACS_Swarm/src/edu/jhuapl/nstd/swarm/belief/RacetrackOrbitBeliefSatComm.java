/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import java.util.Date;

/**
 *
 * @author humphjc1
 */
public class RacetrackOrbitBeliefSatComm extends RacetrackOrbitBelief
{
    public static final String BELIEF_NAME = RacetrackOrbitBelief.BELIEF_NAME + BeliefManagerWacs.SATCOMM_EXTENSION;

    
    public RacetrackOrbitBeliefSatComm()
    {
        super ();
    }

    public RacetrackOrbitBeliefSatComm(Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise)
    {
        super (lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise);
    }
    
    public RacetrackOrbitBeliefSatComm(Latitude lat1, Longitude lon1, Altitude altMslFinal, Altitude altMslStandoff, Length radius, boolean isClockwise, Date time)
    {
        super(lat1, lon1, altMslFinal, altMslStandoff, radius, isClockwise, time);
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
    
    /**
   * Check if this belief's data matches exactly the data for another belief, excluding the belief timestamp
   * @param belief
   * @return Check 
   */
    @Override
  public boolean equals (Object belief)
  {
      if (belief == null || !belief.getClass().equals(this.getClass()))
          return false;
          
      RacetrackOrbitBelief rtBelief = (RacetrackOrbitBelief)belief;
      
      return (getLatitude1().equals (rtBelief.getLatitude1()) && 
                getLongitude1().equals (rtBelief.getLongitude1()) && 
                getFinalAltitudeMsl().equals (rtBelief.getFinalAltitudeMsl()) && 
                getStandoffAltitudeMsl().equals (rtBelief.getStandoffAltitudeMsl()) && 
                getRadius().equals (rtBelief.getRadius()) && 
                getIsClockwise() == rtBelief.getIsClockwise());
  }
}
