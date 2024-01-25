package edu.jhuapl.nstd.swarm.behavior;

import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BearingTimeName;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.IrCameraFOVBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionActualBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.TestCircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import java.util.*;

/**
 *
 * @author buckar1
 */
public class LoiterBehavior extends Behavior
{
    public static final String MODENAME = "loiter";
    
    protected static final RangeBearingHeightOffset RANGE_ZERO = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
    private Date _prevTargetTime;
    private Date _prevSafetyBoxTime;
    private Length _radius;
    private Altitude _finalaltitudeAGL;
    private Altitude _standoffaltitudeAGL;
    private Latitude m_TargetLatitude;
    private Longitude m_TargetLongitude;
    private Date m_TargetTime;
    private Length _prevTerrainHeight = Length.ZERO;
    private String _prevAgentMode = null;
    private boolean _orbitClockwise;
    private SafetyBox _safetyBox;


    private LatLonAltPosition m_LoiterCenterPosition;
    private long m_EnablePlumeDetectionBeforeExplosion_ms;
    private boolean m_UseExplosionTimer;
    private Length m_MaxRangeToTargetToIntercept = null;

    public LoiterBehavior(BeliefManager beliefManager, String agentID)
    {
        super(beliefManager, agentID);

        _safetyBox = new SafetyBox(beliefManager);

        //
        // Load default loiter settings from the config file. Initial position
        // doesn't matter because we won't publish this belief until we have
        // a valid TASE target position.
        //
        
        //If camera is on the right wing, we orbit clockwise around the gimbal target so that the target is to our left.  But,
        //if we are doing an offset loiter, we actually do the reverse.
        _orbitClockwise = Config.getConfig().getPropertyAsBoolean("FlightControl.CameraOnRightWing", false);
        _radius = new Length (1000, Length.METERS);
        _finalaltitudeAGL = new Altitude (1000, Length.FEET);
        _standoffaltitudeAGL = new Altitude (2000, Length.FEET);
        m_TargetLatitude = new Latitude(0, Angle.DEGREES);
        m_TargetLongitude = new Longitude(0, Angle.DEGREES);
        m_TargetTime = new Date (0);
        m_EnablePlumeDetectionBeforeExplosion_ms = Config.getConfig().getPropertyAsLong("LoiterBehavior.TimeBeforeExplosionToEnablePlumeDetection.ms", 15000);
        m_UseExplosionTimer = Config.getConfig().getPropertyAsBoolean("WacsSettingsDefaults.UseExplosionTimer", true);
        m_MaxRangeToTargetToIntercept = new Length (Config.getConfig().getPropertyAsDouble("LoiterBehavior.MaxRangeFromTarget.AllowIntercept.Meters", 5000), Length.METERS);

    }

    @Override
    public void update()
    {

        try
        {
            //doOldUpdate ();
            doNewUpdate ();

            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkForAllowIrExplosionDetection()
    {
        AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefMgr.get(AgentModeActualBelief.BELIEF_NAME);
        
            
        ExplosionTimeActualBelief expTimeBelief = (ExplosionTimeActualBelief) beliefMgr.get(ExplosionTimeActualBelief.BELIEF_NAME);
        if (expTimeBelief != null)
        {
            boolean allow = true;

            //If outside time window, don't allow IR plume detection to trigger intercept
            if ((expTimeBelief.getTime_ms()-System.currentTimeMillis()) > m_EnablePlumeDetectionBeforeExplosion_ms)
                allow = false;

            //If target is not cleanly visible to agent camera, don't allow IR plume detection to trigger intercept
            AgentPositionBelief posBlf = (AgentPositionBelief)beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
            AgentBearingBelief bearingBlf = (AgentBearingBelief)beliefMgr.get(AgentBearingBelief.BELIEF_NAME);
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief) beliefMgr.get(TargetActualBelief.BELIEF_NAME);
            IrCameraFOVBelief fovBlf = (IrCameraFOVBelief)beliefMgr.get(IrCameraFOVBelief.BELIEF_NAME);
            if (posBlf != null && bearingBlf != null && targets != null && fovBlf != null)
            {
                PositionTimeName ptn = posBlf.getPositionTimeName(agentID);
                BearingTimeName btn = bearingBlf.getBearingTimeName(agentID);
                PositionTimeName targetTn = targets.getPositionTimeName(gimbalTargetName);
                NavyAngle maxAngleFromForward = new NavyAngle (fovBlf.getMaxAngleFromForwardDeg(), Angle.DEGREES);
                NavyAngle minAngleFromForward = new NavyAngle (fovBlf.getMinAngleFromForwardDeg(), Angle.DEGREES);
                if (ptn != null && btn != null && targetTn != null)
                {
                    NavyAngle bearingUavToTarg = ptn.getPosition().getBearingTo(targetTn.getPosition());
                    NavyAngle bearingUav = btn.getCurrentBearing();
                    NavyAngle mostClockwiseVisAngle = bearingUav.plus(maxAngleFromForward.asAngle());
                    NavyAngle leastClockwiseVisAngle = bearingUav.plus(minAngleFromForward.asAngle());
                    Length rangeUavToTarget = ptn.getPosition().getRangeTo(targetTn.getPosition());

                    if (bearingUavToTarg.minus(leastClockwiseVisAngle.asAngle()).isGreaterThan(NavyAngle.ZERO) &&
                            bearingUavToTarg.minus(leastClockwiseVisAngle.asAngle()).isLessThan(mostClockwiseVisAngle.minus(leastClockwiseVisAngle.asAngle())) &&
                            rangeUavToTarget.isLessThan(m_MaxRangeToTargetToIntercept))
                    {
                        //good, target is visible and close enough
                    }
                    else
                    {
                        allow = false;
                    }
                }
            }

            IrExplosionAlgorithmEnabledBelief irBlf = new IrExplosionAlgorithmEnabledBelief(WACSAgent.AGENTNAME, allow, expTimeBelief.getTime_ms()-System.currentTimeMillis());
            beliefMgr.put(irBlf);
        }
        else if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(LoiterBehavior.MODENAME))
        {
            ExplosionTimeRequiredBelief oldReqBlf = (ExplosionTimeRequiredBelief)beliefMgr.get(ExplosionTimeRequiredBelief.BELIEF_NAME);
            if (oldReqBlf == null)
            {
                ExplosionTimeRequiredBelief reqBlf = new ExplosionTimeRequiredBelief();
                beliefMgr.put (reqBlf);
            }
            return false;
        }
        return true;
        
        
        
        
        
        
            /*Require target location

            Require loiter location

            Require explosion time ??
            ExplosionTimeBelief timeBelief = (ExplosionTimeBelief)m_BeliefManager.get (ExplosionTimeBelief.BELIEF_NAME);
            if (timeBelief == null || timeBelief.getTime_ms() < System.currentTimeMillis())
            {
                ExplosionTimeRequiredBelief timeReqBelief = new ExplosionTimeRequiredBelief();
                m_BeliefManager.put (timeReqBelief);            
            }*/
    }

    
    private void doNewUpdate()
    {
        //Do IrExplosionAlgorithmEnabledBelief before checking for loiter.  This way it's updated before we put agent into loiter,
        //giving confirmation that the timer is working before we go into loiter.
        boolean errorDetected = false;
        if (m_UseExplosionTimer)
        {
            if (!checkForAllowIrExplosionDetection()){
                errorDetected = true;
            }
        }

        Length targetTerrainHeight = new Length (0, Length.METERS);
        Length standoffTerrainHeight = new Length (0, Length.METERS);
        //Length endTerrainHeight = new Length (0, Length.METERS);

        //
        // Since all behaviors get called all the time, we need to check if we are in the correct mode
        // for this behavior.  Normally this is not necessary since most behaviors output a vector
        // that can be scaled, but this behavior
        //
        AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefMgr.get(AgentModeActualBelief.BELIEF_NAME);
        if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(LoiterBehavior.MODENAME))
        {
            
            boolean loiterChanged = false;

            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief) beliefMgr.get(TargetActualBelief.BELIEF_NAME);
            if (targets == null || targets.getPositionTimeName(gimbalTargetName) == null)
            {
                TargetRequiredBelief currReqBelief = (TargetRequiredBelief)beliefMgr.get(TargetRequiredBelief.BELIEF_NAME);
                if (currReqBelief == null)
                {
                    TargetRequiredBelief reqBelief = new TargetRequiredBelief ();
                    beliefMgr.put (reqBelief);
                }
                errorDetected = true;
            }
            else
            {
                if ((targets != null && _prevTargetTime == null) ||
                    (targets != null && _prevTargetTime != null && targets.getTimeStamp().after(_prevTargetTime)))
                {

                    _prevTargetTime = targets.getTimeStamp();
                    PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                    if (positionTimeName != null)
                    {
                        //
                        // The TASE target position has changed. Update the CircularOrbitBelief
                        // so that we will loiter around this new target. Use the altitude and
                        // radius from the WACSWaypointBelief which is published by the display agent.
                        //
                        LatLonAltPosition centerPosition = positionTimeName.getPosition().asLatLonAltPosition();

                        if (!m_TargetLatitude.equals(centerPosition.getLatitude()) || !m_TargetLongitude.equals(centerPosition.getLongitude()))
                        {
                            m_TargetLatitude = centerPosition.getLatitude();
                            m_TargetLongitude = centerPosition.getLongitude();
                            m_TargetTime = positionTimeName.getTime();
                            loiterChanged = true;
                        }
                    }
                }
            }

            WACSWaypointActualBelief waypointBelief = (WACSWaypointActualBelief) beliefMgr.get(WACSWaypointActualBelief.BELIEF_NAME);
            if (waypointBelief != null)
            {
                //
                // There is a waypoint belief published, so update loiter altitude and radius settings
                //
                if (_radius == null || _finalaltitudeAGL == null || _standoffaltitudeAGL == null
                        || !_radius.equals(waypointBelief.getLoiterRadius())
                        || !_finalaltitudeAGL.equals(waypointBelief.getFinalLoiterAltitude())
                        || !_standoffaltitudeAGL.equals(waypointBelief.getStandoffLoiterAltitude()))
                {
                    _radius = waypointBelief.getLoiterRadius();
                    _finalaltitudeAGL = waypointBelief.getFinalLoiterAltitude();
                    _standoffaltitudeAGL = waypointBelief.getStandoffLoiterAltitude();
                    loiterChanged = true;
                }
            }
            
            SafetyBoxBelief safetyBelief = (SafetyBoxBelief) beliefMgr.get(SafetyBoxBelief.BELIEF_NAME);
            if (safetyBelief != null && (_prevSafetyBoxTime == null || safetyBelief.getTimeStamp().after (_prevSafetyBoxTime)))
            {
                loiterChanged = true;
                _prevSafetyBoxTime = safetyBelief.getTimeStamp();
            }

            RacetrackDefinitionActualBelief racetrackBelief = (RacetrackDefinitionActualBelief) beliefMgr.get(RacetrackDefinitionActualBelief.BELIEF_NAME);
            if (racetrackBelief != null)
            {
                //
                // There is a racetrack definition belief published, so update orbit track
                //
                if (!racetrackBelief.getStartPosition().equals(m_LoiterCenterPosition))
                {
                    m_LoiterCenterPosition = racetrackBelief.getStartPosition();
                    loiterChanged = true;
                }
            }
            else
            {
                RacetrackDefinitionRequiredBelief currReqBelief = (RacetrackDefinitionRequiredBelief)beliefMgr.get(RacetrackDefinitionRequiredBelief.BELIEF_NAME);
                if (currReqBelief == null)
                {
                    RacetrackDefinitionRequiredBelief reqBelief = new RacetrackDefinitionRequiredBelief ();
                    beliefMgr.put (reqBelief);
                }
                errorDetected = true;
                //m_LoiterCenterPosition = new LatLonAltPosition (m_TargetLatitude, m_TargetLongitude, new Altitude (0, Length.METERS));
            }
            
            //Not all of the necessary settings were set for loiter mode.  Return until they are available
            if (errorDetected)
                return;

            
            targetTerrainHeight = DtedGlobalMap.getDted().getJlibAltitude(m_TargetLatitude.getDoubleValue(Angle.DEGREES), m_TargetLongitude.getDoubleValue(Angle.DEGREES)).asLength();
            standoffTerrainHeight = DtedGlobalMap.getDted().getJlibAltitude(m_LoiterCenterPosition.getLatitude().getDoubleValue(Angle.DEGREES), m_LoiterCenterPosition.getLongitude().getDoubleValue(Angle.DEGREES)).asLength();
            /*endTerrainHeight = DtedGlobalMap.getDted().getJlibAltitude(m_RacetrackEndPosition.getLatitude().getDoubleValue(Angle.DEGREES), m_RacetrackEndPosition.getLongitude().getDoubleValue(Angle.DEGREES)).asLength();*/
            Length terrainHeight = targetTerrainHeight;
            /*if (startTerrainHeight != null && startTerrainHeight.isGreaterThan(terrainHeight))
                terrainHeight = startTerrainHeight;
            if (endTerrainHeight != null && endTerrainHeight.isGreaterThan(terrainHeight))
                terrainHeight = endTerrainHeight;*/

            
            if (loiterChanged
                || (_prevTerrainHeight.minus(terrainHeight).abs().getDoubleValue(Length.METERS) > 3)
                || (_prevAgentMode == null || !_prevAgentMode.equals(LoiterBehavior.MODENAME)))
            {

                /*RacetrackOrbitBelief racetrackOrbitBelief = new RacetrackOrbitBelief (m_RacetrackStartPosition.getLatitude(), m_RacetrackStartPosition.getLongitude(),
                                                                                        m_RacetrackEndPosition.getLatitude(), m_RacetrackEndPosition.getLongitude(),
                                                                                        _finalaltitudeAGL.plus (terrainHeight), _standoffaltitudeAGL.plus (standoffTerrainHeight), _radius, _orbitClockwise);
*/
                PiccoloTelemetryBelief telemetryBelief = (PiccoloTelemetryBelief) beliefMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
                double currentUAVSpeed_mps = -1;
                double windSpeedSouth = -1;
                double windSpeedWest = -1;
        
                if (telemetryBelief != null) {
                    currentUAVSpeed_mps = telemetryBelief.getPiccoloTelemetry().IndAirSpeed_mps;
                    windSpeedSouth = telemetryBelief.getPiccoloTelemetry().WindSouth;
                    windSpeedWest = telemetryBelief.getPiccoloTelemetry().WindWest;
                }
                LatLonAltPosition gimbleTarget = new LatLonAltPosition(this.m_TargetLatitude, this.m_TargetLongitude, _finalaltitudeAGL.plus (terrainHeight));
                TestCircularOrbitBelief racetrackOrbitBelief = new TestCircularOrbitBelief (agentID, m_LoiterCenterPosition.getLatitude(), m_LoiterCenterPosition.getLongitude(),
                                                                                        _finalaltitudeAGL.plus (terrainHeight), _standoffaltitudeAGL.plus (standoffTerrainHeight), _radius, _orbitClockwise,
                                                                                        currentUAVSpeed_mps,windSpeedSouth,windSpeedWest, gimbleTarget);
                racetrackOrbitBelief = _safetyBox.getSafeRacetrackOrbit(racetrackOrbitBelief);
                
                

                //boolean isSafe = racetrackOrbitBelief.getIsRacetrackSafe(_safetyBox, gimbleTarget, currentUAVSpeed_mps, windSpeedSouth, windSpeedWest);
                //if (!isSafe)
                //    System.err.println ("CURRENT RACETRACK ORBIT IS NOT SAFE!!!!");
                //TODO Fix racetrack safety
                //boolean isSafe = racetrackOrbitBelief.getIsRacetrackSafe(_safetyBox, gimbleTarget, currentUAVSpeed_mps, windSpeedSouth, windSpeedWest);
                //boolean isSafe = true;
                //if (!isSafe)
                //{
                 //   RacetrackDefinitionRequiredBelief reqBelief = new RacetrackDefinitionRequiredBelief();
                 //   beliefMgr.put(reqBelief);
                //    System.err.println ("CURRENT RACETRACK ORBIT IS NOT SAFE!!!!");
                //}
                //else
               // {
                    //Check direction after adjusting for safety box, since orbit can move - affecting orbit direction
                updateRacetrackDirection (racetrackOrbitBelief);
                beliefMgr.put(racetrackOrbitBelief);
                //}

                _prevTerrainHeight = terrainHeight;
            }

            _prevAgentMode = LoiterBehavior.MODENAME;
        }
        else if (agentModeBelief != null && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null)
        {
            _prevAgentMode = agentModeBelief.getMode(agentID).getName();
        }
        
        
    }

    private void updateRacetrackDirection (TestCircularOrbitBelief racetrackOrbitBelief)
    {
        boolean targetWithinRacetrack = false;

        LatLonAltPosition targetPosition = new LatLonAltPosition (m_TargetLatitude, m_TargetLongitude, new Altitude (0, Length.METERS));

        if (m_LoiterCenterPosition.getRangeTo(targetPosition).isLessThan(_radius))
        {
            targetWithinRacetrack = true;
        }
        else
        {
            NavyAngle bearingTo = m_LoiterCenterPosition.getBearingTo(m_LoiterCenterPosition);
            NavyAngle bearingLeft = bearingTo.increaseToPort(Angle.RIGHT_ANGLE);
            NavyAngle bearingRight = bearingTo.increaseToStarboard(Angle.RIGHT_ANGLE);

            LatLonAltPosition []pointsList = new LatLonAltPosition[4];
            pointsList[0] = (LatLonAltPosition) (m_LoiterCenterPosition.translatedBy(new RangeBearingHeightOffset(_radius, bearingLeft, new Length (0, Length.METERS))));
            pointsList[1] = (LatLonAltPosition) (m_LoiterCenterPosition.translatedBy(new RangeBearingHeightOffset(_radius, bearingRight, new Length (0, Length.METERS))));
            pointsList[2] = (LatLonAltPosition) (m_LoiterCenterPosition.translatedBy(new RangeBearingHeightOffset(_radius, bearingRight, new Length (0, Length.METERS))));
            pointsList[3] = (LatLonAltPosition) (m_LoiterCenterPosition.translatedBy(new RangeBearingHeightOffset(_radius, bearingLeft, new Length (0, Length.METERS))));

            Region region = new Region(pointsList);
            if (region.contains(targetPosition))
                targetWithinRacetrack = true;
        }

        if (_orbitClockwise) //If orbit clockwise flag is true, indicating camera is on right wing, we want to reverse directions here.
            racetrackOrbitBelief.setIsClockwise(targetWithinRacetrack);
        else
            racetrackOrbitBelief.setIsClockwise(!targetWithinRacetrack);
    }

    private void doOldUpdate ()
    {
        //
        // Since all behaviors get called all the time, we need to check if we are in the correct mode
        // for this behavior.  Normally this is not necessary since most behaviors output a vector
        // that can be scaled, but this behavior
        //
        AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefMgr.get(AgentModeActualBelief.BELIEF_NAME);
        if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(LoiterBehavior.MODENAME))
        {

            boolean loiterChanged = false;


            //
            // We want to loiter in a circle centered around the TASE target.
            // Get the TASE target position and convert it to Lat/Lon.
            //
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief) beliefMgr.get(TargetActualBelief.BELIEF_NAME);
            if ((targets != null && _prevTargetTime == null) ||
                (targets != null && _prevTargetTime != null && targets.getTimeStamp().after(_prevTargetTime)))
            {

                _prevTargetTime = targets.getTimeStamp();
                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                if (positionTimeName != null)
                {
                    //
                    // The TASE target position has changed. Update the CircularOrbitBelief
                    // so that we will loiter around this new target. Use the altitude and
                    // radius from the WACSWaypointBelief which is published by the display agent.
                    //
                    LatLonAltPosition centerPosition = positionTimeName.getPosition().asLatLonAltPosition();

                    if (!m_TargetLatitude.equals(centerPosition.getLatitude()) || !m_TargetLongitude.equals(centerPosition.getLongitude()))
                    {
                        m_TargetLatitude = centerPosition.getLatitude();
                        m_TargetLongitude = centerPosition.getLongitude();
                        loiterChanged = true;
                    }
                }
            }

            WACSWaypointActualBelief waypointBelief = (WACSWaypointActualBelief) beliefMgr.get(WACSWaypointActualBelief.BELIEF_NAME);
            if (waypointBelief != null)
            {
                //
                // There is a waypoint belief published, so update loiter altitude and radius settings
                //
                //_radius = waypointBelief.getLoiterRadius();
                //_altitudeAGL = waypointBelief.getLoiterAltitude();
                if (_radius == null || _finalaltitudeAGL == null || !_radius.equals(waypointBelief.getLoiterRadius()) || !_finalaltitudeAGL.equals(waypointBelief.getFinalLoiterAltitude()))
                {
                    _radius = waypointBelief.getLoiterRadius();
                    _finalaltitudeAGL = waypointBelief.getFinalLoiterAltitude();
                    loiterChanged = true;
                }
            }

            Length terrainHeight = DtedGlobalMap.getDted().getJlibAltitude(m_TargetLatitude.getDoubleValue(Angle.DEGREES), m_TargetLongitude.getDoubleValue(Angle.DEGREES)).asLength();

            if (loiterChanged
                || (_prevTerrainHeight.minus(terrainHeight).abs().getDoubleValue(Length.METERS) > 3)
                || (_prevAgentMode == null || !_prevAgentMode.equals(LoiterBehavior.MODENAME)))
            {
                TestCircularOrbitBelief circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                                                                                  new LatLonAltPosition(m_TargetLatitude,
                                                                                                        m_TargetLongitude,
                                                                                                        _finalaltitudeAGL.plus(terrainHeight)),
                                                                                                        _radius,
                                                                                                        _orbitClockwise);

                circularOrbitBelief = _safetyBox.getSafeCircularOrbit(circularOrbitBelief);
                //circularOrbitBelief.adjustOrbitCenterToSafeLocation();
                beliefMgr.put(circularOrbitBelief);

                _prevTerrainHeight = terrainHeight;
            }

            _prevAgentMode = LoiterBehavior.MODENAME;
        }
        else if (agentModeBelief.getMode(WACSAgent.AGENTNAME) != null)
        {
            _prevAgentMode = agentModeBelief.getMode(agentID).getName();
        }
    }

    @Override
    public synchronized RangeBearingHeightOffset getResult()
    {
        //
        // This behavior does not output a vector, but instead outputs a
        // circular orbit belief.  Therefore, always return a zero vector.
        //
        return RANGE_ZERO;
    }
}
