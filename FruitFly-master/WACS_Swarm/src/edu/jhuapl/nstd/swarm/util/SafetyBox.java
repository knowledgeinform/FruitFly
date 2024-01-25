package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.util.PolygonRegion;
import edu.jhuapl.nstd.util.RectangleRegion;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafetyBox
{
    BeliefManager m_beliefManager;
    DtedGlobalMap m_Dted = null;
    boolean ignoreSafetyBox = false;
    

    public SafetyBox(BeliefManager beliefManager)
    {
        m_beliefManager = beliefManager;
        
        m_Dted = DtedGlobalMap.getDted();
    }

    public CircularOrbitBelief getSafeCircularOrbit(CircularOrbitBelief circularOrbitBelief)
    {
        circularOrbitBelief.setRadius(getSafeOrbitRaidius(circularOrbitBelief.getRadius()));
        circularOrbitBelief.setPosition(getSafeOrbitCenterPosition(circularOrbitBelief.getPosition().asLatLonAltPosition(), circularOrbitBelief.getRadius()));
      
        return circularOrbitBelief;
    }
    
    public TestCircularOrbitBelief getSafeCircularOrbit(TestCircularOrbitBelief circularOrbitBelief) {
        circularOrbitBelief.setRadius(getSafeOrbitRaidius(circularOrbitBelief.getRadius()));
        circularOrbitBelief.setPosition(getSafeOrbitCenterPosition(circularOrbitBelief.getPosition().asLatLonAltPosition(), circularOrbitBelief.getRadius()));
        /*LatLonAltPosition agentPosition = null;
        AgentPositionBelief agentPositionBelief = (AgentPositionBelief) m_beliefManager.get(AgentPositionBelief.BELIEF_NAME);
        if (agentPositionBelief != null) {
            PositionTimeName planePosition = agentPositionBelief.getPositionTimeName(circularOrbitBelief.getName());
            if (planePosition != null) {
                agentPosition = planePosition.getPosition().asLatLonAltPosition();

            }
        }
        */
        LatLonAltPosition agentPosition = null;
                    AgentPositionBelief agentPositionBelief = (AgentPositionBelief)m_beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                        if (agentPositionBelief != null)
                        {
                            PositionTimeName planePosition = agentPositionBelief.getPositionTimeName(circularOrbitBelief.getUniqueAgentID());
                            if (planePosition != null)
                            {
                                agentPosition = planePosition.getPosition().asLatLonAltPosition();
                                    
                            }
                        }
        
        
        if(agentPosition ==null){
            agentPosition = circularOrbitBelief.getPosition().asLatLonAltPosition();
        }
        

        //circularOrbitBelief.adjustOrbitCenterToSafeLocation(agentPosition, this);

        return circularOrbitBelief;
    }

    public CircularOrbitBelief getSafeCircularOrbitIgnoreRadius(CircularOrbitBelief circularOrbitBelief)
    {
        circularOrbitBelief.setPosition(getSafeOrbitCenterPosition(circularOrbitBelief.getPosition().asLatLonAltPosition(), circularOrbitBelief.getRadius()));

        return circularOrbitBelief;
    }

    public TestCircularOrbitBelief getSafeRacetrackOrbit (TestCircularOrbitBelief racetrackOrbitBelief)
    {
        racetrackOrbitBelief.setRadius(getSafeOrbitRaidius(racetrackOrbitBelief.getRadius()));

        //LatLonAltPosition position1 = new LatLonAltPosition (racetrackOrbitBelief.getLatitude1(), racetrackOrbitBelief.getLongitude1(), racetrackOrbitBelief.getFinalAltitudeMsl());
        //LatLonAltPosition position2 = new LatLonAltPosition (racetrackOrbitBelief.getLatitude2(), racetrackOrbitBelief.getLongitude2(), racetrackOrbitBelief.getFinalAltitudeMsl());

        racetrackOrbitBelief.setPosition(getSafeOrbitCenterPosition(racetrackOrbitBelief.getPosition().asLatLonAltPosition(), racetrackOrbitBelief.getRadius()));
        //position1 = getSafeOrbitCenterPosition(position1, racetrackOrbitBelief.getRadius());
        //position2 = getSafeOrbitCenterPosition(position2, racetrackOrbitBelief.getRadius());

        //racetrackOrbitBelief.setPosition1(position1.getLatitude(), position1.getLongitude());
        //racetrackOrbitBelief.setPosition2(position2.getLatitude(), position2.getLongitude());
        
        //Set minimum altitude from either point
        //if (position1.getAltitude().isLessThan (position2.getAltitude()))
        //    racetrackOrbitBelief.setFinalAltitudeMsl(position2.getAltitude());
        //else
        //    racetrackOrbitBelief.setFinalAltitudeMsl(position1.getAltitude());

        //Threshold standoff altitude to be above final altitude
        //if (racetrackOrbitBelief.getFinalAltitudeMsl().isGreaterThan (racetrackOrbitBelief.getStandoffAltitudeMsl()))
        //    racetrackOrbitBelief.setStandoffAltitudeMsl(racetrackOrbitBelief.getFinalAltitudeMsl());
       
                       
        return racetrackOrbitBelief;
    }

    public Length getSafeOrbitRaidius (Length radius)
    {
        SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double maxRadius_m = safetyBoxBelief.getMaxRadius_m();
            double minRadius_m = safetyBoxBelief.getMinRadius_m();

            double radius_m;
            //
            // Adjust radius if it's not within limits
            //
            radius_m = radius.getDoubleValue(Length.METERS);
            if (radius_m < minRadius_m)
            {
                radius_m = minRadius_m;
            }
            else if (radius_m > maxRadius_m)
            {
                radius_m = maxRadius_m;
            }

            radius = new Length (radius_m, Length.METERS);
        }
        return radius;
    }

    public LatLonAltPosition getSafeOrbitCenterPosition (LatLonAltPosition centerPosition, Length radius)
    {
        return getSafeOrbitCenterPosition(centerPosition, radius, false);
    }

    public LatLonAltPosition getSafeOrbitCenterPosition (LatLonAltPosition centerPosition, Length radius, boolean ignoreAltitude)
    {

        SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double latitude1_deg = safetyBoxBelief.getLatitude1_deg();
            double longitude1_deg = safetyBoxBelief.getLongitude1_deg();
            double latitude2_deg = safetyBoxBelief.getLatitude2_deg();
            double longitude2_deg = safetyBoxBelief.getLongitude2_deg();

            double maxLatitude_deg;
            double maxLongitude_deg;
            double minLatitude_deg;
            double minLongitude_deg;

            if (latitude1_deg < latitude2_deg)
            {
                minLatitude_deg = latitude1_deg;
                maxLatitude_deg = latitude2_deg;
            }
            else
            {
                minLatitude_deg = latitude2_deg;
                maxLatitude_deg = latitude1_deg;
            }
            if (longitude1_deg < longitude2_deg)
            {
                minLongitude_deg = longitude1_deg;
                maxLongitude_deg = longitude2_deg;
            }
            else
            {
                minLongitude_deg = longitude2_deg;
                maxLongitude_deg = longitude1_deg;
            }


            double northernmostLatitude_deg;
            double southernmostLatitude_deg;
            double westernmostLongitude_deg;
            double easternmostLongitude_deg;
            double centerLatitude_deg;
            double centerLongitude_deg;
            double altitudeMSL_m;
            

            //
            // If orbit would take plane outside of safety box, move orbit
            // edge back inside box.
            //
            centerLatitude_deg = centerPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            centerLongitude_deg = centerPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
            northernmostLatitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.NORTH,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            southernmostLatitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.SOUTH,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            westernmostLongitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.WEST,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
            easternmostLongitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.EAST,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);

            if (southernmostLatitude_deg < northernmostLatitude_deg && southernmostLatitude_deg < minLatitude_deg)
            {
                centerPosition = new LatLonAltPosition(new Latitude(minLatitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getLongitude(),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.NORTH,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(southernmostLatitude_deg > northernmostLatitude_deg && southernmostLatitude_deg > maxLatitude_deg)
            {
                centerPosition = new LatLonAltPosition(new Latitude(maxLatitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getLongitude(),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.NORTH,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(northernmostLatitude_deg < southernmostLatitude_deg && northernmostLatitude_deg < minLatitude_deg)
            {
                centerPosition = new LatLonAltPosition(new Latitude(minLatitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getLongitude(),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.SOUTH,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(northernmostLatitude_deg > southernmostLatitude_deg && northernmostLatitude_deg > maxLatitude_deg)
            {
                centerPosition = new LatLonAltPosition(new Latitude(maxLatitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getLongitude(),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.SOUTH,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }

            if (westernmostLongitude_deg < easternmostLongitude_deg && westernmostLongitude_deg < minLongitude_deg)
            {
                centerPosition = new LatLonAltPosition(centerPosition.asLatLonAltPosition().getLatitude(),
                                                                      new Longitude(minLongitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.EAST,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(westernmostLongitude_deg > easternmostLongitude_deg && westernmostLongitude_deg > maxLongitude_deg)
            {
                centerPosition = new LatLonAltPosition(centerPosition.asLatLonAltPosition().getLatitude(),
                                                                      new Longitude(maxLongitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.EAST,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(easternmostLongitude_deg < westernmostLongitude_deg && easternmostLongitude_deg < minLongitude_deg)
            {
                centerPosition = new LatLonAltPosition(centerPosition.asLatLonAltPosition().getLatitude(),
                                                                      new Longitude(minLongitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.WEST,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }
            else if(easternmostLongitude_deg > westernmostLongitude_deg && easternmostLongitude_deg > maxLongitude_deg)
            {
                centerPosition = new LatLonAltPosition(centerPosition.asLatLonAltPosition().getLatitude(),
                                                                      new Longitude(maxLongitude_deg, Angle.DEGREES),
                                                                      centerPosition.asLatLonAltPosition().getAltitude());
                centerPosition = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                            NavyAngle.WEST,
                                                                                                                            Length.ZERO)).asLatLonAltPosition();
            }


            if (!ignoreAltitude)
            {
                altitudeMSL_m = centerPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
                altitudeMSL_m = getSafeOrbitAltitudeMSLFromMSL(centerLatitude_deg, centerLongitude_deg, altitudeMSL_m, safetyBoxBelief);
                
                // Update the altitude
                LatLonAltPosition originalPosition = centerPosition.asLatLonAltPosition();
                centerPosition = new LatLonAltPosition(originalPosition.getLatitude(),
                                                                      originalPosition.getLongitude(),
                                                                      new Altitude(altitudeMSL_m, Length.METERS));
            }
        }

        return centerPosition;
    }

    /*public double getMinAltMslMeters (LatLonAltPosition m_myCurrentPosition)
    {
        SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double minAltMSL = getSafeOrbitAltitudeMSL(m_myCurrentPosition.getLatitude().getDoubleValue(Angle.DEGREES), m_myCurrentPosition.getLongitude().getDoubleValue(Angle.DEGREES), -1, safetyBoxBelief);
            return minAltMSL;
        }

        return 2000;
    }*/

    public boolean isPointInsideSafetyBox(LatLonAltPosition m_SafePositionLLA)
    {
        SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double latitude1_deg = safetyBoxBelief.getLatitude1_deg();
            double longitude1_deg = safetyBoxBelief.getLongitude1_deg();
            double latitude2_deg = safetyBoxBelief.getLatitude2_deg();
            double longitude2_deg = safetyBoxBelief.getLongitude2_deg();

            double maxLatitude_deg;
            double maxLongitude_deg;
            double minLatitude_deg;
            double minLongitude_deg;

            if (latitude1_deg < latitude2_deg)
            {
                minLatitude_deg = latitude1_deg;
                maxLatitude_deg = latitude2_deg;
            }
            else
            {
                minLatitude_deg = latitude2_deg;
                maxLatitude_deg = latitude1_deg;
            }
            if (longitude1_deg < longitude2_deg)
            {
                minLongitude_deg = longitude1_deg;
                maxLongitude_deg = longitude2_deg;
            }
            else
            {
                minLongitude_deg = longitude2_deg;
                maxLongitude_deg = longitude1_deg;
            }

            if (m_SafePositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) < minLatitude_deg)
            {
                return false;
            }
            else if(m_SafePositionLLA.getLatitude().getDoubleValue(Angle.DEGREES) > maxLatitude_deg)
            {
                return false;
            }
            else if(m_SafePositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) < minLongitude_deg)
            {
                return false;
            }
            else if(m_SafePositionLLA.getLongitude().getDoubleValue(Angle.DEGREES) > maxLongitude_deg)
            {
                return false;
            }
        }
        return true;
    }
  
    public boolean isRectangleOutsideKeepOutRegions(RectangleRegion r){
        KeepOutRegionBelief belief = (KeepOutRegionBelief)this.m_beliefManager.get(KeepOutRegionBelief.BELIEF_NAME);
        if(belief!=null){
            ArrayList<PolygonRegion> regions = belief.getRegions();
            for(PolygonRegion p: regions){
                try {
                    if(p.intersects(r))return false;
                } catch (Exception ex) {
                    Logger.getLogger(SafetyBox.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }
    
    public boolean isOrbitInsideSafetyBox(LatLonAltPosition centerPosition, Length radius)
    {
        if(ignoreSafetyBox){
            return true;
        }
        SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double latitude1_deg = safetyBoxBelief.getLatitude1_deg();
            double longitude1_deg = safetyBoxBelief.getLongitude1_deg();
            double latitude2_deg = safetyBoxBelief.getLatitude2_deg();
            double longitude2_deg = safetyBoxBelief.getLongitude2_deg();

            double maxLatitude_deg;
            double maxLongitude_deg;
            double minLatitude_deg;
            double minLongitude_deg;

            if (latitude1_deg < latitude2_deg)
            {
                minLatitude_deg = latitude1_deg;
                maxLatitude_deg = latitude2_deg;
            }
            else
            {
                minLatitude_deg = latitude2_deg;
                maxLatitude_deg = latitude1_deg;
            }
            if (longitude1_deg < longitude2_deg)
            {
                minLongitude_deg = longitude1_deg;
                maxLongitude_deg = longitude2_deg;
            }
            else
            {
                minLongitude_deg = longitude2_deg;
                maxLongitude_deg = longitude1_deg;
            }


            double northernmostLatitude_deg;
            double southernmostLatitude_deg;
            double westernmostLongitude_deg;
            double easternmostLongitude_deg;

            northernmostLatitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.NORTH,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            southernmostLatitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.SOUTH,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
            westernmostLongitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.WEST,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
            easternmostLongitude_deg = centerPosition.translatedBy(new RangeBearingHeightOffset(radius,
                                                                                                                   NavyAngle.EAST,
                                                                                                                   Length.ZERO)).asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);

            if (southernmostLatitude_deg < northernmostLatitude_deg && southernmostLatitude_deg < minLatitude_deg)
            {
                return false;
            }
            else if(southernmostLatitude_deg > northernmostLatitude_deg && southernmostLatitude_deg > maxLatitude_deg)
            {
                return false;
            }
            else if(northernmostLatitude_deg < southernmostLatitude_deg && northernmostLatitude_deg < minLatitude_deg)
            {
                return false;
            }
            else if(northernmostLatitude_deg > southernmostLatitude_deg && northernmostLatitude_deg > maxLatitude_deg)
            {
                return false;
            }

            if (westernmostLongitude_deg < easternmostLongitude_deg && westernmostLongitude_deg < minLongitude_deg)
            {
                return false;
            }
            else if(westernmostLongitude_deg > easternmostLongitude_deg && westernmostLongitude_deg > maxLongitude_deg)
            {
                return false;
            }
            else if(easternmostLongitude_deg < westernmostLongitude_deg && easternmostLongitude_deg < minLongitude_deg)
            {
                return false;
            }
            else if(easternmostLongitude_deg > westernmostLongitude_deg && easternmostLongitude_deg > maxLongitude_deg)
            {
                return false;
            }
        }
        return true;
    }

    public double getSafeOrbitAltitudeMSLFromMSL(double latDecDeg, double lonDecDeg, double altMsl_m, SafetyBoxBelief safetyBoxBelief) 
    {
        if (safetyBoxBelief == null)
            safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double maxAltitude_m = safetyBoxBelief.getMaxAltitude_m();
            boolean maxAlt_IsAGL = safetyBoxBelief.getMaxAlt_IsAGL();
            double minAltitude_m = safetyBoxBelief.getMinAltitude_m();
            boolean minAlt_IsAGL = safetyBoxBelief.getMinAlt_IsAGL();
        
            if (maxAlt_IsAGL)
            {
                //enforce maximum AGL limit to MSL altitude value
                double ground = DtedGlobalMap.getDted().getJlibAltitude(latDecDeg, lonDecDeg).getDoubleValue(Length.METERS);
                double altitude_m_agl = altMsl_m - ground;
                if (altitude_m_agl > maxAltitude_m)
                {
                    altitude_m_agl = maxAltitude_m;
                }
                altMsl_m = ground + altitude_m_agl;
            }
            else
            {
                //enforce maximum MSL limit to MSL altitude value
                if (altMsl_m > maxAltitude_m)
                {
                    altMsl_m = maxAltitude_m;
                }
            }
            
            //check minimum altitude second so that even if max alt is less than min alt, we don't fly too low.
            if (minAlt_IsAGL)
            {
                //enforce minimum AGL limit to MSL altitude value
                double ground = DtedGlobalMap.getDted().getJlibAltitude(latDecDeg, lonDecDeg).getDoubleValue(Length.METERS);
                double altitude_m_agl = altMsl_m - ground;
                if (altitude_m_agl < minAltitude_m)
                {
                    altitude_m_agl = minAltitude_m;
                }
                altMsl_m = ground + altitude_m_agl;
            }
            else
            {
                //enforce minimum MSL limit to MSL altitude value
                if (altMsl_m < minAltitude_m)
                {
                    altMsl_m = minAltitude_m;
                }
            }
        }
        
        return altMsl_m;
    }

    public double getSafeOrbitAltitudeMSLFromAGL(double latDecDeg, double lonDecDeg, double altAgl_m) 
    {
        double ground = DtedGlobalMap.getDted().getJlibAltitude(latDecDeg, lonDecDeg).getDoubleValue(Length.METERS);
        double altitudeMSL_m = altAgl_m + ground;
        return getSafeOrbitAltitudeMSLFromMSL(latDecDeg, lonDecDeg, altitudeMSL_m, null);
    }

    /*
    public TestCircularOrbitBelief getSafeOrbitAltitude(TestCircularOrbitBelief circularOrbitBelief, Altitude alt) {
        double altitude = alt.getDoubleValue(Length.METERS);
        double minAltitude = circularOrbitBelief.getMinimumSafeAltitdue();
        if(altitude<minAltitude)altitude = minAltitude;
        
        LatLonAltPosition pos = circularOrbitBelief.getPosition().asLatLonAltPosition();
        
        
        
        return circularOrbitBelief;
    }
    * */

    public double findMaxAlt(double alt) {
        
        SafetyBoxBelief   safetyBoxBelief = (SafetyBoxBelief)m_beliefManager.get(SafetyBoxBelief.BELIEF_NAME);
        if (safetyBoxBelief != null)
        {
            double maxAltitude_m = safetyBoxBelief.getMaxAltitude_m();
            boolean maxAlt_IsAGL = safetyBoxBelief.getMaxAlt_IsAGL();
            
        
            if (maxAlt_IsAGL)
            {
                return alt+maxAltitude_m;
            }
            else
            {
                //enforce maximum MSL limit to MSL altitude value
                return maxAltitude_m;
            }
        }
        return Double.MAX_VALUE;
    }
    
    public boolean getIgnoreSafetyBox(){
        return ignoreSafetyBox;
    }
    
    public void setIgnoreSafetyBox(boolean ignore){
        ignoreSafetyBox = ignore;
    }

}
