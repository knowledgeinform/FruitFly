//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2003 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
package edu.jhuapl.nstd.swarm.action;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.CartesianPosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.SearchBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.SafetyBox;
import edu.jhuapl.nstd.swarm.util.Vector3;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

/**
 * This class drives a simulated robot based on the output of the search behavior.
 * In the future we will need to add a mechanism for accumulating multiple behaviors
 * and weighting them appropriately to generate out output.
 */
public class SimulatedRobotDriver implements Updateable, PropertyChangeListener
{

    /**
     * The belief manager we will get our belefs about the world from.
     */
    BeliefManager beliefManager;
    /**
     * The unique agentid of our agent.
     */
    String agentID = "";
    /**
     * The behavior group merges all of the behaviors into a single vector based on weights
     */
    BehaviorGroup behaviorGroup;
    /**
     * The speed we will move.
     */
    Length speedPerUpdate;
    Length altitudePerUpdate;
    /**
     * The string that we will use to get the speed per update out of the config file (in meters)
     */
    public static final String VERTICAL_SPEED = new String("SimulatedRobotDriver.verticalSpeed");
    protected AvoidObstacleAction _avoidObstacleAction;
    /**
     * The position of our robot.
     */
    AgentPositionBelief belName = new AgentPositionBelief();
    Length _windSpeed = new Length(Config.getConfig().getPropertyAsDouble("SimulationMode.windspeed", 0),
                                   Length.METERS);
    NavyAngle _windBearing = new NavyAngle(Config.getConfig().getPropertyAsDouble("SimulationMode.windbearing", 0),
                                           Angle.DEGREES);
    RangeBearingHeightOffset _wind = new RangeBearingHeightOffset(_windSpeed,
                                                                  _windBearing, Length.ZERO);
    double _maxTurn = Config.getConfig().getPropertyAsDouble("SimulatedRobotDriver.maxTurn", 360.0);
    long _lastUpdated = -1;
    int _responseDelay = Config.getConfig().getPropertyAsInteger("SimulatedRobotDriver.responseDelay", 0);
    LinkedList<RangeBearingHeightOffset> _responseQueue = new LinkedList<RangeBearingHeightOffset>();
    int _queuedResults = 0;
    int _positionID = 0;
    double m_vehicleSpeed_mps;
    double m_vehicleTurn_dps;
    double m_timeAccelerationCoefficient;
    SafetyBox m_safetyBox;
    long m_circularOrbitBeliefTimestamp = 0;
    CircularOrbitBelief m_safeCircularOrbitBelief = null;

    /**
     * Constructor.
     * @param mgr The belief manager that we will get all our beliefs from.
     * @param agentID The unique agent id.
     * @param behavior The behavior that we should use to guide our motion actions.
     */
    public SimulatedRobotDriver(BeliefManager mgr, String agentID, BehaviorGroup group)
    {
        this.agentID = agentID;
        this.beliefManager = mgr;
        this.behaviorGroup = group;
        double spdkts = Config.getConfig().getPropertyAsDouble("SimulatedRobotDriver.vehicleSpeed_knots", 0.0);
        m_vehicleSpeed_mps = (new Speed(spdkts, Speed.KNOTS)).getDoubleValue(Speed.METERS_PER_SECOND);
        m_timeAccelerationCoefficient = Config.getConfig().getPropertyAsDouble("SimulationMode.timeAccelerationCoefficient",1);
 
        double bankAngle = Config.getConfig().getPropertyAsDouble("SimulatedRobotDriver.bankAngle",20.0);
        m_vehicleTurn_dps = 1092.95*Math.tan(Math.toRadians(bankAngle))/spdkts;
        double vspeed = Config.getConfig().getPropertyAsDouble(VERTICAL_SPEED, 0.0);
        
        altitudePerUpdate = new Length(vspeed, Length.METERS);
        _avoidObstacleAction = new AvoidObstacleAction(mgr);
        Config.getConfig().addPropertyChangeListener(this);

        m_safetyBox = new SafetyBox(beliefManager);
    }

    /**
     * The method is called whenever our global configuration changes
     */
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        double vspeed = Config.getConfig().getPropertyAsDouble(VERTICAL_SPEED, 0.0);
        altitudePerUpdate = new Length(vspeed, Length.METERS);
    }

    /**
     * This method is called every update cycle. It does the work of actually moving the simualted robot.
     */
    @Override
    public void update()
    {
        try
        {
            AbsolutePosition p;
            AgentPositionBelief b = (AgentPositionBelief) beliefManager.get(belName.getName());
            if (b != null)
            {
                p = b.getPositionTimeName(agentID).getPosition();

                NavyAngle currentHeading = b.getPositionTimeName(agentID).getHeading();



                if (_lastUpdated == -1)
                {
                    _lastUpdated = System.currentTimeMillis();
                }
                long updateDuration_ms = System.currentTimeMillis() - _lastUpdated;
                double updateDuration_sec = (double) updateDuration_ms / 1000; //to get to seconds
                Length movementDistance = new Length(m_vehicleSpeed_mps * updateDuration_sec * m_timeAccelerationCoefficient, Length.METERS);
                double turnValue = m_vehicleTurn_dps * updateDuration_sec * m_timeAccelerationCoefficient;
                Length resultAltitude = (Length) altitudePerUpdate.clone();
                resultAltitude = resultAltitude.times(updateDuration_sec);
                _wind = new RangeBearingHeightOffset(_windSpeed.times(updateDuration_sec), _windBearing, Length.ZERO);


                RangeBearingHeightOffset r = null;
                if (Config.getConfig().getPropertyAsBoolean("SimulatedRobotDriver.avoidObstacles", true))
                {
                    r = _avoidObstacleAction.avoidObstacle(agentID, p, currentHeading);
                }


                //
                // If there is a CircularOrbitBelief, we want it to override the vector returned by the behavior group
                //
                Altitude orderedAltitude = null;
                CircularOrbitBelief circularOrbitBelief = (CircularOrbitBelief) beliefManager.get(CircularOrbitBelief.BELIEF_NAME);
                if (circularOrbitBelief != null)
                {                    
                    if (circularOrbitBelief.getTimeStamp().getTime() > m_circularOrbitBeliefTimestamp)
                    { 
                        m_circularOrbitBeliefTimestamp = circularOrbitBelief.getTimeStamp().getTime();
                        m_safeCircularOrbitBelief = m_safetyBox.getSafeCircularOrbit(circularOrbitBelief);
                    }

                    //
                    // Calculate vector to circular orbit
                    //
                    AgentPositionBelief posBelief = (AgentPositionBelief) beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                    LatLonAltPosition agentLatLon = (LatLonAltPosition) posBelief.getPositionTimeName(agentID).getPosition();

                    if (agentLatLon == null)
                    {
                        r = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
                    }
                    else
                    {
                        CartesianPosition agentPosition = agentLatLon.asCartesianPosition(agentLatLon);
                        orderedAltitude = m_safeCircularOrbitBelief.getPosition().asLatLonAltPosition().getAltitude();

                        // get the center position, defaulting to the set one
                        CartesianPosition centerPosition = m_safeCircularOrbitBelief.getPosition().asLatLonAltPosition().asCartesianPosition(agentLatLon);
                        centerPosition = new CartesianPosition(centerPosition.getX(), centerPosition.getY(), new Length(0, Length.METERS));

                        if (centerPosition == null)
                        {
                            r = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
                        }
                        else
                        {
                            Vector3 centerVector = new Vector3(centerPosition, Length.METERS);
                            Length distanceToCenter = centerVector.length();
                            if (distanceToCenter.equals (Length.ZERO))
                            {
                                centerVector.setX(0.0001);
                                distanceToCenter = centerVector.length();
                            }
                            centerVector.normalize();

                            // create the tangent vector
                            Vector3 tangent = new Vector3(centerVector);
                            if (m_safeCircularOrbitBelief.getIsClockwise())
                            {
                                tangent.rotateZ(Math.PI / 2.0);
                            }
                            else
                            {
                                tangent.rotateZ(-Math.PI / 2.0);
                            }
                            tangent.normalize();

                            //
                            // Calculate how much we need to move toward or away from the center of the circle
                            //
                            double errorRatio = distanceToCenter.minus(m_safeCircularOrbitBelief.getRadius()).dividedBy(new Length(800, Length.FEET)).getDoubleValue();
                            centerVector.scale(errorRatio * 3);

                            r = centerVector.plus(tangent).normalized().asRangeBearingHeightOffset();
                        }
                    }
                }
                else
                {
                    if (r == null || r.getRange().isLessThan(new Length(0.0000001, Length.METERS)))
                    {
                        r = behaviorGroup.getResult();
                    }
                    else
                    {
                        RangeBearingHeightOffset r2 = behaviorGroup.getResult();
                        r = new RangeBearingHeightOffset(r.getRange(), r.getBearing(),
                                                         r2.getHeight());
                    }
                }

                // response queueing. To simulate the delay in response to an update,
                // we queue the result vector a specified number of timesteps.
                if (_responseDelay > 0)
                {
                    if (_queuedResults < _responseDelay)
                    {
                        _responseQueue.add(r);
                        _queuedResults++;
                        return;
                    }
                    _responseQueue.add(r);

                    r = _responseQueue.remove();
                }


                Angle clockwise = currentHeading.clockwiseAngleTo(r.getBearing());
                Angle counterClockwise = currentHeading.counterClockwiseAngleTo(r.getBearing());
                double clockwiseValue = clockwise.getDoubleValue(Angle.DEGREES);
                double counterClockwiseValue = counterClockwise.getDoubleValue(Angle.DEGREES);
                double minValue = Math.min(clockwiseValue, counterClockwiseValue);

                NavyAngle turn = r.getBearing();


                if (minValue > turnValue)
                {
                    if (clockwiseValue <= counterClockwiseValue)
                    { //turning right
                        turn = currentHeading.plus(new Angle(turnValue, Angle.DEGREES));
                    }
                    else
                    { //turning left
                        turn = currentHeading.minus(new Angle(turnValue, Angle.DEGREES));
                    }
                }



                if (!r.getRange().equals(Length.ZERO))
                {
                    double height = p.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
                    double direction = 1.0;
                    if (orderedAltitude.getDoubleValue(Length.METERS) < height)
                        direction = -1.0;

                   // double direction = (Math.abs(height) > 0.001) ? height / Math.abs(height) : 0.0;
//                    if (true)//behaviorGroup.hasDesiredAltitude())
//                    {
                    p = p.translatedBy(new RangeBearingHeightOffset(movementDistance,
                                                                        turn,
                                                                        new Length(direction * resultAltitude.getDoubleValue(Length.METERS), Length.METERS)));
//                    }
//                    else
//                    {
//                        p = p.translatedBy(new RangeBearingHeightOffset(movementDistance,
//                                                                        turn, new Length(0, Length.METERS)));
//                        p = new LatLonAltPosition(p.asLatLonAltPosition().getLatitude(),
//                                                  p.asLatLonAltPosition().getLongitude(),
//                                                  new Altitude(Config.getConfig().getPropertyAsDouble("preferredAltitude", 0.0), Length.FEET));
//                    }

                    if (_windSpeed.isGreaterThan(Length.ZERO))
                    {
                        p = p.translatedBy(_wind);
                    }
                }


//                if (overrideAltitude != null)
//                {
//                    p = new LatLonAltPosition(p.asLatLonAltPosition().getLatitude(),
//                                              p.asLatLonAltPosition().getLongitude(),
//                                              overrideAltitude);
//                }
                AgentPositionBelief positionBelief = new AgentPositionBelief(agentID, p, turn);
                AgentBearingBelief bearingBelief = new AgentBearingBelief(
                        agentID, turn, turn);

                beliefManager.put(bearingBelief);
                beliefManager.put(positionBelief);

                SearchBelief searchBelief = (SearchBelief) beliefManager.get(SearchBelief.BELIEF_NAME);
                if (searchBelief != null)
                {
                    Point point = searchBelief.getStateSpace().getPoint(p);
                    if (point != null)
                    {
                        searchBelief.getStateSpace().set(point, SearchBelief.SEARCHED_VALUE);
                    }
                }
                else
                {
                    searchBelief = new SearchBelief(agentID, p);
                    beliefManager.put(searchBelief);
                }
                _lastUpdated = System.currentTimeMillis();
            }
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
} // end class SimulatedRobotDriver

