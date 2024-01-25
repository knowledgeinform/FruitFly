package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.Time;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.util.Config;
import java.util.LinkedList;
import java.util.List;

/**
 * Predicts a region that the wacsagent is guaranteed to be found in, so that in
 * a loss of COMMs there is a known region where the plane can be found. Bases
 * prediction on information found in the config file and telemetry provided by
 * the Piccolo unit. Updates itself every x seconds, where x is specified in the
 * config file.
 * 
 * @author fishmsm1
 */
public class AgentPrediction
{
    private BeliefManager belMgr;
    private String agentName;
    //private WindHistory windHistory;
    /**
     * Time (in milliseconds) to wait before showing the bubble
     */
    private int waitTime;
    /**
     * Time (in milliseconds) to wait between prediction bubble updates
     */
    private int frequency;
    /**
     * Reference to the thread that automatically updates the prediction after
     * <code>frequency</code> milliseconds
     */
    private PredictionThread updateThread;
    
    private Speed commandedAirspeed; // Not actually used
    /**
     * Maximum amount that the plane can roll (in degrees)
     */
    private double maxBank;
    /**
     * Maximum speed of ascent
     */
    private Speed altAscent;
    /**
     * Maximum speed of descent
     */
    private Speed altDescent;
    /**
     * Amount of change in speed that the plane can be expected to make
     */
    private Speed airspeedBuffer;
    /**
     * Holds the most recent speed reading before the plane lost comms
     */
    private Speed avgWindSpeed;
    /**
     * Direction wind was blowing in
     */
    private NavyAngle avgWindDir;
    /**
     * Resolution of prediction bubble. This number is multiplied by 360 to get
     * the requested number of steps to make. Actual number of steps will differ
     * due to bubble wrapping back onto itself or plane not having enough time
     * to reach all positions.
     */
    private double resolution;
    
    /**
     * Holds the most recent set of predictions
     */
    private List<LatLonAltPosition> predictions;
    /**
     * Bounds on the prediction bubble
     */
    private Latitude minLat, maxLat;
    private Longitude minLon, maxLon;
    /**
     * Stores whether or not the most recent doPredict wrapped all the way around
     * or not
     */
    private boolean bubbleClosed = true;
    
    /**
     * If <code>true</code>, doBankTrace applies the same wind vector to each point,
     * otherwise it makes the much cooler picture, which bases the wind vector
     * on the expected amount of time to reach that point.
     */
    private static final boolean CONSTANT_TIME_BANK_TRACE = true;
    /**
     * If <code>true</code>, the buffer is applied to increase and decrease, showing
     * a crescent expected region until the bubble closes.
     */
    private static final boolean SHOW_CRESCENT = true;
    
    public AgentPrediction(BeliefManager mgr, String agent)
    {
        belMgr = mgr;
        agentName = agent;
        
        //windHistory = new WindHistory(agentName);
        avgWindSpeed = Speed.ZERO;
        avgWindDir = NavyAngle.ZERO;
        
        waitTime = Config.getConfig().getPropertyAsInteger("AgentPrediction.ShowPredictionAfter.Ms", 5000);
        frequency = Config.getConfig().getPropertyAsInteger("AgentPrediction.UpdateFrequency.Ms", 1000);
        resolution = Config.getConfig().getPropertyAsDouble("AgentPrediction.Resolution", 1);
        
        commandedAirspeed = new Speed(Config.getConfig().getPropertyAsDouble("ShadowDriver.commandedAirspeed_knots", 65.0), Speed.KNOTS);
        maxBank = Config.getConfig().getPropertyAsDouble("FlightControl.maxBankAngle_deg", 20.0) * Math.PI / 180;
        altAscent = new Speed(Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AltitudeAscentRate.fps", 6), Speed.FEET_PER_SECOND);
        altDescent = new Speed(Config.getConfig().getPropertyAsDouble("ShadowDriver.RacetrackLoiter.AltitudeDescentRate.fps", 10), Speed.FEET_PER_SECOND);
        airspeedBuffer = new Speed(Config.getConfig().getPropertyAsDouble("AgentPrediction.AirspeedBuffer.Knots", 5), Speed.KNOTS);
    }
    
    /**
     * When called, this method starts up the thread that automatically makes a
     * prediction every <code>frequency</code> milliseconds. Without calling this method,
     * <code>predictions</code> will need to be updated by manually calling
     * <code>updatePredictions()</code>.
     */
    public void startPredicting()
    {
        updateThread = new PredictionThread();
        updateThread.start();
    }
    
    public Altitude getMaxAltitude(long time)
    {
        AgentPositionBelief posBlf = (AgentPositionBelief) belMgr.get(AgentPositionBelief.BELIEF_NAME);
        
        if (posBlf == null || posBlf.getPositionTimeName(agentName) == null)
        {
            return null;
        }
        else
        {
            PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
            LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
            Time timeDelta = new Time(time - ptn.getTime().getTime(), Time.MILLISECONDS);
            
            return lla.getAltitude().plus(altAscent.times(timeDelta));
        }
    }
    
    public Altitude getMaxAltitude()
    {
        return getMaxAltitude(System.currentTimeMillis());
    }
    
    public Altitude getMinAltitude(long time)
    {
        AgentPositionBelief posBlf = (AgentPositionBelief) belMgr.get(AgentPositionBelief.BELIEF_NAME);
        
        if (posBlf == null || posBlf.getPositionTimeName(agentName) == null)
        {
            return null;
        }
        else
        {
            PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
            LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
            Time timeDelta = new Time(time - ptn.getTime().getTime(), Time.MILLISECONDS);
            
            return lla.getAltitude().minus(altDescent.times(timeDelta));
        }
    }
    
    public Altitude getMinAltitude()
    {
        return getMinAltitude(System.currentTimeMillis());
    }
    
    public List<LatLonAltPosition> getPredictions()
    {
        return predictions;
    }
    
    public Latitude getMinLatitude()
    {
        return minLat;
    }
    
    public Latitude getMaxLatitude()
    {
        return maxLat;
    }
    
    public Longitude getMinLongitude()
    {
        return minLon;
    }
    
    public Longitude getMaxLongitude()
    {
        return maxLon;
    }
    
    public boolean hasMinMaxPos()
    {
        return minLat != null && maxLat != null && minLon != null && maxLon != null;
    }
    
    /**
     * Updates the predictions array with a new prediction based on the current
     * time.
     */
    private void updatePredictions()
    {
        updatePredictions(System.currentTimeMillis());
    }
    
    /**
     * Updates the predictions array with a new prediction based on the time
     * specified.
     * @param time the specified time, in milliseconds since 1970
     */
    private synchronized void updatePredictions(long time)
    {
        predictions = predict(time, true);
    }
    
    /**
     * Informs calling code whether or not it should display the bubble based
     * on the time passed since the last position update.
     * 
     * @return <code>true</code> if the bubble should be displayed, <code>false</code>
     * otherwise
     */
    public boolean shouldShowBubble()
    {
        AgentPositionBelief posBlf = (AgentPositionBelief) belMgr.get(AgentPositionBelief.BELIEF_NAME);
        if (posBlf != null && posBlf.getPositionTimeName(agentName) != null)
        {
            PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
            if (ptn != null)
            {
                int timeDelta = (int) (System.currentTimeMillis() - ptn.getTime().getTime());

                return timeDelta >= waitTime;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns a list of points which represent the prediction for the plane's
     * position.
     * 
     * @param time the time (in milliseconds since 1970) to predict for
     * @param saveMaxMin whether or not the global bounding box values should be
     * updated by this operation
     * @return the prediction
     */
    private List<LatLonAltPosition> predict(long time, boolean saveMaxMin)
    {
        if (saveMaxMin)
        {
            minLat = null;
            maxLat = null;
            minLon = null;
            maxLon = null;
        }
        
        PiccoloTelemetryBelief piccoloTelemetryBelief = (PiccoloTelemetryBelief) belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
        AgentPositionBelief posBlf = (AgentPositionBelief) belMgr.get(AgentPositionBelief.BELIEF_NAME);
                
        if (posBlf == null || posBlf.getPositionTimeName(agentName) == null || piccoloTelemetryBelief == null)
        {
            // Nothing to predict
            return null;
        }
        else
        {
            int steps = (int) (resolution * 720);
            LinkedList<LatLonAltPosition> positions = new LinkedList<LatLonAltPosition>();
            
            PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
            LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
            int timeDelta = (int) (time - ptn.getTime().getTime());
            
            Pic_Telemetry telem = piccoloTelemetryBelief.getPiccoloTelemetry();
            
            // Possibly need to use velocity direction instead:
            double heading = telem.TrueHeading;
//            double heading = Math.atan2(telem.VelNorth, telem.VelEast);
//            // Convert standard angle in range (-pi, pi) to Navy Angle in radians
//            if (heading < 0) // Heading is in quadrant 3 or 4
//                heading = Math.PI / 2 + (-heading); // pi/2 in NavyAngles is equal to 0 in standard
//            else if (heading > 0) // Heading is in quadrant 1 or 2
//                heading = 5 / 2 * Math.PI - heading;
//            else // heading is precisely rightward
//                heading = Math.PI / 2;
            
            Speed speed = new Speed(telem.IndAirSpeed_mps, Speed.METERS_PER_SECOND);
            
            Speed maxSpeed = speed.plus(airspeedBuffer);
            Speed minSpeed = speed.minus(airspeedBuffer);
            
            Length minRadius = new Length((Math.pow(maxSpeed.getDoubleValue(Speed.METERS_PER_SECOND), 2))/(9.81 * Math.tan(maxBank)), Length.METERS);
            
            NavyAngle leftAngle = new NavyAngle(heading, Angle.DEGREES).minus(Angle.RIGHT_ANGLE);
            NavyAngle rightAngle = new NavyAngle(heading, Angle.DEGREES).plus(Angle.RIGHT_ANGLE);
            LatLonAltPosition leftCenter = (LatLonAltPosition)lla.translatedBy(new RangeBearingHeightOffset(minRadius, leftAngle, Length.ZERO));
            LatLonAltPosition rightCenter = (LatLonAltPosition)lla.translatedBy(new RangeBearingHeightOffset(minRadius, rightAngle, Length.ZERO));

            // This is unnecessary
            /*if (!Config.getConfig().getPropertyAsBoolean("WacsSettingsDefaults.useguiwind", true))
            {
                Speed windSpeed = new Speed(Math.sqrt(telem.WindSouth * telem.WindSouth + telem.WindWest * telem.WindWest), Speed.METERS_PER_SECOND);
                
                // Caclulate heading and convert to Navy Angles
                NavyAngle windHeading;
                double stdAngle = Math.atan2(telem.WindSouth, telem.WindWest);
                if (stdAngle < 0)
                    windHeading = new NavyAngle(-stdAngle + Math.PI/2, Angle.RADIANS);
                else if (stdAngle > 0)
                    windHeading = new NavyAngle(5/2 * Math.PI - stdAngle, Angle.RADIANS);
                else
                    windHeading = new NavyAngle(Math.PI/2, Angle.RADIANS);
                
                METPositionTimeName metPositionTimeName = new METPositionTimeName(agentName,
                                                                                  windHeading,
                                                                                  windSpeed,
                                                                                  lla,
                                                                                  new Date());
                windHistory.addMETPosition(metPositionTimeName);
                METTimeName metTimeName = windHistory.getAverageWind();
                avgWindDir = metTimeName.getWindBearing();
                avgWindSpeed = metTimeName.getWindSpeed();
            }
            else
            {*/
                METBelief metBelief = (METBelief) belMgr.get(METBelief.BELIEF_NAME);
                METTimeName mtn = null;
                if (metBelief != null && (mtn = metBelief.getMETTimeName(WACSAgent.AGENTNAME)) != null)
                {
                    avgWindDir = mtn.getWindBearing();
                    avgWindSpeed = mtn.getWindSpeed();
                }
            //}
            
//            if (telem.IndAirSpeed_mps > maxSpeed.getDoubleValue(Speed.METERS_PER_SECOND))
//            {
//                speed = new Speed(telem.IndAirSpeed_mps, Speed.METERS_PER_SECOND);
//            }
//            else
//            {
//                speed = maxSpeed;
//            }
            
            
            if (!SHOW_CRESCENT)
            {
                List<LatLonAltPosition> leftSide = doPredict(leftCenter, rightAngle, maxSpeed, minRadius, steps/2, timeDelta, false, false, saveMaxMin);
                List<LatLonAltPosition> rightSide = doPredict(rightCenter, leftAngle, maxSpeed, minRadius, steps/2, timeDelta, true, true, saveMaxMin);
                positions.addAll(leftSide);
                positions.addAll(rightSide);
            }
            else
            {
                List<LatLonAltPosition> leftSide = doPredict(leftCenter, rightAngle, maxSpeed, minRadius, steps / 2, timeDelta, false, false,saveMaxMin);
                positions.addAll(leftSide);
                if (!bubbleClosed)
                {
                    List<LatLonAltPosition> leftSideMin = doPredict(leftCenter, rightAngle, minSpeed, minRadius, steps / 2, timeDelta, false, true, saveMaxMin);
                    List<LatLonAltPosition> rightSideMin = doPredict(rightCenter, leftAngle, minSpeed, minRadius, steps / 2, timeDelta, true, false, saveMaxMin);

                    positions.addAll(leftSideMin);
                    positions.addAll(rightSideMin);
                }

                List<LatLonAltPosition> rightSide = doPredict(rightCenter, leftAngle, maxSpeed, minRadius, steps / 2, timeDelta, true, true, saveMaxMin);
                positions.addAll(rightSide);
            }

            return positions;
        }
    }
    
    /**
     * Makes one leg of the prediction.
     * 
     * @param trackCenter the center point of the circle to predict on. This circle
     * represents the tightest turn circle the plane could make
     * @param startAngle angle point back from the center of the circle to the plane's
     * last recorded position
     * @param speed the speed with which the plane is assumed to continue moving
     * at
     * @param minRadius the radius for the turn track
     * @param steps the number of predictions to make
     * @param timeDelta the amount of time that has passed since the plane lost
     * COMMs
     * @param isRight whether or not we are prediction for the track to the right
     * of the plane
     * @param moveIn whether or not the order of predictions should move toward the
     * plane or away (i.e. start at max angle or min angle)
     * @param saveMaxMin whether or not to update the global bounding box
     * @return a leg of the predictions
     */
    private List<LatLonAltPosition> doPredict(LatLonAltPosition trackCenter, NavyAngle startAngle, Speed speed, Length minRadius, int steps, int timeDelta, boolean isRight, boolean moveIn, boolean saveMaxMin)
    {
        LinkedList<LatLonAltPosition> pred = new LinkedList<LatLonAltPosition>();
        
        if (minRadius.equals(Length.ZERO))
            minRadius = new Length((Math.pow(speed.getDoubleValue(Speed.METERS_PER_SECOND), 2))/(9.81 * Math.tan(maxBank)), Length.METERS);
        
        boolean closed = false;
        
        NavyAngle step = new NavyAngle(Angle.FULL_CIRCLE.dividedBy((double)steps));
        NavyAngle cur;
        if (moveIn)
        {
            cur = new NavyAngle(Angle.FULL_CIRCLE).minus(step.asAngle());
        }
        else
        {
            cur = new NavyAngle(Angle.ZERO);
        }
        for (int i = 0; i < steps - 1; i++)
        {
            Length circleDistance = minRadius.times(cur.getDoubleValue(Angle.RADIANS));
            Time circleTime = circleDistance.dividedBy(speed);
            if (circleTime.getDoubleValue(Time.MILLISECONDS) > timeDelta)
            {
                Length actualDistance = speed.times(new Time(timeDelta, Time.MILLISECONDS));
                Angle rawAngle = new Angle(actualDistance.dividedBy(minRadius).getDoubleValue(), Angle.RADIANS);
                if (!isRight)
                {
                    rawAngle = rawAngle.times(-1);
                }
                NavyAngle actualAngle = startAngle.plus(rawAngle);
                RangeBearingHeightOffset planeOffset = new RangeBearingHeightOffset(minRadius, actualAngle, Length.ZERO);
                
                RangeBearingHeightOffset windOffset = new RangeBearingHeightOffset(avgWindSpeed.times(new Time(timeDelta, Time.MILLISECONDS)), avgWindDir, Length.ZERO);
                
//                pred.add((LatLonAltPosition) trackCenter.translatedBy(planeOffset).translatedBy(windOffset));
            }
            else
            {
                Angle rawCircleHeading = cur.asAngle();
                if (!isRight)
                {
                    rawCircleHeading = rawCircleHeading.times(-1);
                }
                NavyAngle circleHeading = startAngle.plus(rawCircleHeading);
                RangeBearingHeightOffset circleOffset = new RangeBearingHeightOffset(minRadius, circleHeading, Length.ZERO);
                                
                Angle rawLineHeading = cur.plus(Angle.RIGHT_ANGLE).asAngle();
                if (!isRight)
                {
                    rawLineHeading = rawLineHeading.times(-1);
                }
                NavyAngle lineHeading = startAngle.plus(rawLineHeading);
                Time lineTime = new Time(timeDelta, Time.MILLISECONDS).minus(circleTime);
                Length lineDistance = speed.times(lineTime);
                RangeBearingHeightOffset lineOffset = new RangeBearingHeightOffset(lineDistance, lineHeading, Length.ZERO);
                
                RangeBearingHeightOffset windOffset = new RangeBearingHeightOffset(avgWindSpeed.times(new Time(timeDelta, Time.MILLISECONDS)), avgWindDir, Length.ZERO);
                
                LatLonAltPosition pos = (LatLonAltPosition)trackCenter.translatedBy(circleOffset).translatedBy(lineOffset).translatedBy(windOffset);
                LatLonAltPosition plane = (LatLonAltPosition)trackCenter.translatedBy(new RangeBearingHeightOffset(minRadius, startAngle, Length.ZERO)).translatedBy(windOffset);
                
                if (!isRight)
                {
                    double deref = angleNormalize(pos.getBearingTo(plane).asAngle().minus(startAngle.minus(Angle.RIGHT_ANGLE).asAngle())).getDoubleValue(Angle.DEGREES);
                    if (deref >= 0 && deref <= 180.1)
                    {
                        pred.add(pos);
                        
                        if (saveMaxMin)
                        {
                            if (maxLat == null || pos.getLatitude().compareTo(maxLat) > 0)
                                maxLat = pos.getLatitude();
                            else if (minLat == null || pos.getLatitude().compareTo(minLat) < 0)
                                minLat = pos.getLatitude();
                            
                            if (maxLon == null || pos.getLongitude().compareTo(maxLon) > 0)
                                maxLon = pos.getLongitude();
                            else if (minLon == null || pos.getLongitude().compareTo(minLon) < 0)
                                minLon = pos.getLongitude();
                        }
                    }
                    else if (deref < 0 || deref > 190)
                    {
                        closed = true;
                        break;
                    }
                }
                else if (isRight)
                {
                    double deref = angleNormalize(pos.getBearingTo(plane).asAngle().minus(startAngle.plus(Angle.RIGHT_ANGLE).asAngle())).getDoubleValue(Angle.DEGREES);
                    if (deref >= 179.9 && deref <= 365)
                    {
                        pred.add(pos);
                        
                        if (saveMaxMin)
                        {
                            if (maxLat == null || pos.getLatitude().compareTo(maxLat) > 0)
                                maxLat = pos.getLatitude();
                            else if (minLat == null || pos.getLatitude().compareTo(minLat) < 0)
                                minLat = pos.getLatitude();

                            if (maxLon == null || pos.getLongitude().compareTo(maxLon) > 0)
                                maxLon = pos.getLongitude();
                            else if (minLon == null || pos.getLongitude().compareTo(minLon) < 0)
                                minLon = pos.getLongitude();
                        }
                    }
                    else
                    {
                        if (!closed)
                            pred.clear();
                        
                        closed = true;
                    }
                }
            }
            if (moveIn)
            {
                cur = cur.minus(step.asAngle());
            }
            else
            {
                cur = cur.plus(step.asAngle());
            }
        }
        
        bubbleClosed = closed;
        
        if (!SHOW_CRESCENT)
        {
            if (!closed)
            {
                if (!isRight)
                    pred.addAll(doBankTrace(trackCenter, startAngle, speed, steps, timeDelta, true));
                else
                    pred.addAll(0, doBankTrace(trackCenter, startAngle, speed, steps, timeDelta, false));
            }
        }
        
        return pred;
    }
    
    /**
     * Traces out the minimum radius track based up to the amount of time indicated.
     * @param trackCenter see doPredict
     * @param startAngle see doPredict
     * @param speed see doPredict
     * @param steps see doPredict
     * @param timeDelta see doPredict
     * @param isRight see doPredict
     * @return 
     */
    private List<LatLonAltPosition> doBankTrace(LatLonAltPosition trackCenter, NavyAngle startAngle, Speed speed, int steps, int timeDelta, boolean isRight)
    {
        List<LatLonAltPosition> list = new LinkedList<LatLonAltPosition>();
        
        Length minRadius = new Length((Math.pow(speed.getDoubleValue(Speed.METERS_PER_SECOND), 2))/(9.81 * Math.tan(maxBank)), Length.METERS);
            
        
        NavyAngle step = new NavyAngle(Angle.FULL_CIRCLE.dividedBy((double)steps));
        NavyAngle actualAngle;
        
        Length circleDistance = minRadius.times(2 * Math.PI);
        Time circleTime = circleDistance.dividedBy(speed);
        if (circleTime.getDoubleValue(Time.MILLISECONDS) > timeDelta)
        {
            Length actualDistance = speed.times(new Time(timeDelta, Time.MILLISECONDS));
            Angle rawAngle = new Angle(actualDistance.dividedBy(minRadius).getDoubleValue(), Angle.RADIANS);
            if (!isRight)
            {
                rawAngle = rawAngle.times(-1);
            }
            actualAngle = startAngle.plus(rawAngle);
        }
        else
        {
            return list; // ignore
        }
        
        Time timePassed = new Time(timeDelta, Time.MILLISECONDS);
        RangeBearingHeightOffset constWindOffset = new RangeBearingHeightOffset(avgWindSpeed.times(timePassed), avgWindDir, Length.ZERO);
        
        if (!isRight)
        {
            NavyAngle cur = actualAngle;
            double curVal = startAngle.getDoubleValue(Angle.RADIANS) - cur.getDoubleValue(Angle.RADIANS);
            if (curVal < 0)
                curVal = curVal + 2*Math.PI;
            // Sweeps from endpoint (maximum possible point on the minRadius loop) to the startAngle (ray from center of minRadius
            // circle to plane position).
//            while (Math.abs(cur.minus(startAngle.asAngle()).getDoubleValue(Angle.DEGREES)) > step.getDoubleValue(Angle.DEGREES))
            while (curVal > 0)
            {
                RangeBearingHeightOffset offset = new RangeBearingHeightOffset(minRadius, cur, Length.ZERO);
                
                RangeBearingHeightOffset windOffset;
                if (CONSTANT_TIME_BANK_TRACE)
                {
                    windOffset = constWindOffset;
                }
                else
                {
                    Time timeToReach = new Length(curVal * minRadius.getDoubleValue(Length.METERS), Length.METERS).dividedBy(speed);
                    windOffset = new RangeBearingHeightOffset(avgWindSpeed.times(timeToReach), avgWindDir, Length.ZERO);
                }
                
                list.add((LatLonAltPosition) trackCenter.translatedBy(offset).translatedBy(windOffset));
                cur = cur.plus(step.asAngle());
                curVal = curVal - step.getDoubleValue(Angle.RADIANS);
            }
        }
        else
        {
            NavyAngle cur = startAngle;
            double curVal = actualAngle.getDoubleValue(Angle.RADIANS) - cur.getDoubleValue(Angle.RADIANS);
            if (curVal < 0)
                curVal = curVal + 2*Math.PI;
            
            double maxVal = curVal;
            // Sweeps from startAngle (ray from center of minRadius circle to plane position) to 
            // the endpoint (maximum possible point on the minRadius loop).
//            while (Math.abs(cur.minus(actualAngle.asAngle()).getDoubleValue(Angle.DEGREES)) > step.getDoubleValue(Angle.DEGREES))
            while (curVal > 0)
            {
                RangeBearingHeightOffset offset = new RangeBearingHeightOffset(minRadius, cur, Length.ZERO);
                
                RangeBearingHeightOffset windOffset;
                if (CONSTANT_TIME_BANK_TRACE)
                {
                    windOffset = constWindOffset;
                }
                else
                {
                    Time timeToReach = new Length((maxVal - curVal) * minRadius.getDoubleValue(Length.METERS), Length.METERS).dividedBy(speed);
                    windOffset = new RangeBearingHeightOffset(avgWindSpeed.times(timeToReach), avgWindDir, Length.ZERO);
                }
                
                list.add((LatLonAltPosition) trackCenter.translatedBy(offset).translatedBy(windOffset));
                cur = cur.plus(step.asAngle());
                curVal = curVal - step.getDoubleValue(Angle.RADIANS);
            }
        }

        return list;
    }
    
    /**
     * Compares to angles 'correctly' regardless of whether or not the short angle
     * between them crosses 0 or not.
     * @param one first angle
     * @param other second angle
     * @return 1 if angle <code>one</code> is greater, -1 if angle <code>other</code>
     * is greater. 0 if equal.
     */
    private int angleCompareTo(Angle one, Angle other)
    {
        if (one.compareTo(Angle.RIGHT_ANGLE) <= 0 && other.compareTo(Angle.THREE_QUARTERS_CIRCLE) >= 0)
        {
            return 1;
        }
        else if (other.compareTo(Angle.RIGHT_ANGLE) <= 0 && one.compareTo(Angle.THREE_QUARTERS_CIRCLE) >= 0)
        {
            return -1;
        }
        else
        {
            return one.compareTo(other);
        }
    }
    
    /**
     * Finds the angle representation within 0-360
     * @param angle Angle to convert
     * @return converted Angle
     */
    private Angle angleNormalize(Angle angle)
    {
        Angle ret = angle;
        
        while (ret.getDoubleValue(Angle.DEGREES) < 0)
            ret = angle.plus(Angle.FULL_CIRCLE);
        
        while (ret.getDoubleValue(Angle.DEGREES) > 360)
            ret = angle.minus(Angle.FULL_CIRCLE);
        
        return ret;
    }
    
    /**
     * Updates predictions after every interval (interval amount specified in
     * config setting)
     */
    public class PredictionThread extends Thread
    {
        public PredictionThread()
        {
            super();
            
            this.setName("AgentPrediction");
        }
        
        public void run()
        {
            while (true)
            {
                //This is unnecessary
                /*PiccoloTelemetryBelief piccoloTelemetryBelief = (PiccoloTelemetryBelief) belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
                AgentPositionBelief posBlf = (AgentPositionBelief) belMgr.get(AgentPositionBelief.BELIEF_NAME);
                
                if (piccoloTelemetryBelief != null && posBlf != null && posBlf.getPositionTimeName(agentName) != null)
                {
                    PositionTimeName ptn = posBlf.getPositionTimeName(agentName);
                    LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                    Pic_Telemetry telem = piccoloTelemetryBelief.getPiccoloTelemetry();

                    Speed windSpeed = new Speed(Math.sqrt(telem.WindSouth * telem.WindSouth + telem.WindWest * telem.WindWest), Speed.METERS_PER_SECOND);

                    // Caclulate heading and convert to Navy Angles
                    NavyAngle windHeading;
                    double stdAngle = Math.atan2(telem.WindSouth, telem.WindWest);
                    if (stdAngle < 0)
                    {
                        windHeading = new NavyAngle(-stdAngle + Math.PI / 2, Angle.RADIANS);
                    }
                    else if (stdAngle > 0)
                    {
                        windHeading = new NavyAngle(5 / 2 * Math.PI - stdAngle, Angle.RADIANS);
                    }
                    else
                    {
                        windHeading = new NavyAngle(Math.PI / 2, Angle.RADIANS);
                    }

                    METPositionTimeName metPositionTimeName = new METPositionTimeName(agentName,
                                                                                      windHeading,
                                                                                      windSpeed,
                                                                                      lla,
                                                                                      new Date());
                    windHistory.addMETPosition(metPositionTimeName);
                }*/
                
                if (shouldShowBubble())
                    updatePredictions();
                else
                    predictions = null;
                
                try {
                    Thread.sleep(frequency);
                }
                catch (InterruptedException e) {
                    // Ignore and update predictions again
                }
            }
        }
    }
}
