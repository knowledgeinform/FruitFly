package edu.jhuapl.nstd.swarm.action;

import java.util.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.CartesianPosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.piccolo.*;
import edu.jhuapl.nstd.swarm.autopilot.PlaneInfo;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.LoiterApproachPathBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceActualBelief;
import javax.swing.JOptionPane;

/**
 *
 * @author buckar1
 */
public class PiccoloGroundStationAction implements Updateable, Pic_TelemetryListener
{

    private BeliefManager _beliefMgr;
    private Date _lastUpdateInterceptTime = null;
    private long _lastSendInterceptTime = 0;
    private Date _lastUpdateLoiterTime = null;
    private long _lastSendLoiterTime = 0;
    private long m_ExplosionTimeBeliefTimestamp = 0;
    private long m_TargetsBeliefTimestamp = 0;
    private long m_RacetrackTimeBeliefTimestamp = 0;
    private Pic_Interface _groundStationInterface;
    private WindHistory m_WindHistory;
    private SafetyBox m_safetyBox;

    private long m_InterceptUpdatePeriodMs;
    private long m_LoiterUpdatePeriodMs;

    private boolean m_DoLoiterApproach = false;
    private boolean m_ApproachPointsInitialized = false;
    private boolean m_WarnedFinalLoiter = false;
    
    private final Object m_telemetryMessageLock = new Object();
    private Pic_Telemetry m_telemetryMessage = null;
    private Speed m_ActualCommandedAirspeed;
    private Speed m_DesiredCommandedAirspeed;

    private NavyAngle m_BearingOfLoiterApproachPath;
    private LatLonAltPosition m_ContactPositionLLA;
    private LatLonAltPosition m_SafePositionLLA;
    private LatLonAltPosition m_FirstRangePositionLLA;
    private Length m_DiffContactToFirstRangePoints;
    private Length m_DiffCenterToSafePoints;
    private Length m_DiffCenterToFirstRangePoint;
    private boolean m_ApproachPathInvalid;
    private CartesianPosition m_ContactPositionCart;
    private CartesianPosition m_ClosestPointOnPathCart;
    private Length m_minTurnRadius;
    private double m_toApproachPathVectorBias;
    private Speed m_ApproachPathGroundSpeed;
    private Time m_TimeAgentToContact;
    private Speed m_AltitudeDescentRate;
    private Speed m_AltitudeAscentRate;
    private double m_FinalRacetrackOrbitBufferPercentage;
    private Angle m_AngleAroundFinalOrbitToWarn;
    private boolean m_PermitZeroRadiusOrbit;
    private double m_DistCenterToFirstRangeFactorForPathStart;
    private long m_LastTimeToExplosionMs;
    private long m_TimeDeltaToSwitchFromApproachPathMs;
    private boolean m_ApproachTargetFromLeft = false;
    

    public PiccoloGroundStationAction(BeliefManager beliefMgr)
    {
        _beliefMgr = beliefMgr;

        m_WindHistory = new WindHistory(WACSAgent.AGENTNAME);
        m_safetyBox = new SafetyBox(beliefMgr);
        m_LastTimeToExplosionMs = Long.MAX_VALUE;
        
        boolean useTCP = Config.getConfig().getPropertyAsBoolean("PiccoloGroundStationAction.useTCP", true);
        int planeNum = Config.getConfig().getPropertyAsInteger("PiccoloGroundStationAction.planenum", 1);
        m_InterceptUpdatePeriodMs = Config.getConfig().getPropertyAsInteger("PiccoloGroundStationAction.InterceptUpdatePeriod.ms", 5000);
        m_LoiterUpdatePeriodMs = Config.getConfig().getPropertyAsInteger("PiccoloGroundStationAction.LoiterUpdatePeriod.ms", 5000);
        m_DesiredCommandedAirspeed = new Speed(Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.commandedAirspeed_knots", 65.0), Speed.KNOTS);
        m_ActualCommandedAirspeed = (Speed)m_DesiredCommandedAirspeed.clone();
        m_minTurnRadius = new Length(Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.MinTurnRadius.Meters", 350), Length.METERS);
        m_toApproachPathVectorBias = Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.ToApproachPathVectorBias", 1.5);
        m_AltitudeDescentRate = new Speed (Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.RacetrackOrbit.AltitudeDescentRate.fps", 10), Speed.FEET_PER_SECOND);
        m_AltitudeAscentRate = new Speed (Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.RacetrackLoiter.AltitudeAscentRate.fps", 6), Speed.FEET_PER_SECOND);
        m_AngleAroundFinalOrbitToWarn = new Angle (Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.RacetrackOrbit.AngleAroundFinalOrbitToWarn.Degrees", 45), Angle.DEGREES);
        m_FinalRacetrackOrbitBufferPercentage = 1 + Math.max (0, Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.RacetrackOrbit.FinalOrbitBufferPercentage", .05));
        m_DistCenterToFirstRangeFactorForPathStart = Config.getConfig().getPropertyAsDouble("PiccoloGroundStationAction.DistCenterToFirstRange.FactorForPathStart", 0.999);
        m_TimeDeltaToSwitchFromApproachPathMs = Config.getConfig().getPropertyAsLong("PiccoloGroundStationAction.TimeDeltaToSwitchFromApproachPathMs", 60000);
        
        //If camera is on right wing, we should approach target on left so we can see it from our right side
        m_ApproachTargetFromLeft = Config.getConfig().getPropertyAsBoolean("FlightControl.CameraOnRightWing", false);

        if (useTCP)
        {
            int port = Config.getConfig().getPropertyAsInteger("PiccoloGroundStationAction.port", 2000);
            String addr = Config.getConfig().getProperty("PiccoloGroundStationAction.ipaddr", "176.16.2.180");
            _groundStationInterface = new Pic_Interface(planeNum, addr, port, true);
            _groundStationInterface.addPicTelemetryListener(this);
        }
        else
        {
            String groundStationComPort = Config.getConfig().getProperty("PiccoloGroundStationAction.comport", "COM1");
            int groundStationBaud = Config.getConfig().getPropertyAsInteger("PiccoloGroundStationAction.baud", 9600);
            _groundStationInterface = new Pic_Interface(planeNum, groundStationComPort, groundStationBaud, false);
            _groundStationInterface.addPicTelemetryListener(this);
        }
        
        Thread pt = (new Thread(_groundStationInterface));
        pt.setName ("WACS-PiccoloGSAction");
        pt.start();

        //******************
        // TODO: determine if we need to make this separate thread or not
        //       given that I think we only need to send bytes to groundstation,
        //       not receive.
        //Thread groundStationInterfaceThread = new Thread(_groundStationInterface);
        //groundStationInterfaceThread.start();
        //******************
    }

    public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem)
    {
        synchronized (m_telemetryMessageLock)
        {
            m_telemetryMessage = new Pic_Telemetry(telem);
        }

        System.out.println("Pic Telemetry => ");
        System.out.println("\t(lat,lon,altWGS84) = (" + telem.Lat + "," + telem.Lon + "," + telem.AltWGS84 + ")");
        System.out.println("\t(r,p,y,comp) = (" + telem.Roll + "," + telem.Pitch + "," + telem.Yaw + "," + telem.TrueHeading + ")");
        System.out.println("\t(WindSouth,WindWest,IAS) = (" + telem.WindSouth + "," + telem.WindWest + "," + telem.IndAirSpeed_mps + ")");
        System.out.println("\t(vnorth,vest,vdown) = (" + telem.VelNorth + "," + telem.VelEast + "," + telem.VelDown + ")");
        System.out.println("\t(PDOP,Status) = (" + telem.PDOP + "," + telem.GPS_Status + ")\n\n");

        boolean simulate = Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate");
        boolean useExternalSimulation = Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation");

        if (telem != null)
        {
            double windHeading = Math.atan2(-telem.WindWest, -telem.WindSouth);
            double windSpeed = Math.sqrt(telem.WindSouth * telem.WindSouth + telem.WindWest * telem.WindWest);
            NavyAngle winddir = new NavyAngle(windHeading, Angle.RADIANS);
            winddir = winddir.plus(new Angle(180, Angle.DEGREES));
            METPositionTimeName mtn = new METPositionTimeName(WACSAgent.AGENTNAME, winddir, new Speed(windSpeed, Speed.METERS_PER_SECOND), null, new Date());
            m_WindHistory.addMETPosition(mtn);

            WindEstimateSourceActualBelief windSourceBelief = (WindEstimateSourceActualBelief)_beliefMgr.get (WindEstimateSourceActualBelief.BELIEF_NAME);
            if (windSourceBelief != null && windSourceBelief.getWindSource() == WindEstimateSourceActualBelief.WINDSOURCE_UAVAUTOPILOT)
            {
                METTimeName mt = m_WindHistory.getAverageWind();
                METBelief mBel = new METBelief(WACSDisplayAgent.AGENTNAME, mt);
                _beliefMgr.put(mBel);
                mBel = new METBelief(WACSAgent.AGENTNAME, mt);
                _beliefMgr.put(mBel);
            }
            else
            {

            }
        }
         
        if (simulate && useExternalSimulation)
        {
            PlaneInfo info = new PlaneInfo(telem.Lat, telem.Lon, telem.AltWGS84, telem.AltMSL, telem.IndAirSpeed_mps, telem.Yaw);
            if (info != null)
            {
                Latitude latitude = new Latitude(info.getLatitude(), Angle.DEGREES);
                Longitude longitude = new Longitude(info.getLongitude(), Angle.DEGREES);
                NavyAngle heading = new NavyAngle(info.getBearing(), Angle.DEGREES);
                Altitude altitude = new Altitude(info.getAltitudeMSL(), Length.METERS);
                LatLonAltPosition position = new LatLonAltPosition(latitude, longitude, altitude);
                double gimbalAz = info.getGimbalAz();
                double gimbalEl = info.getGimbalEl();
                AgentPositionBelief positionBelief = new AgentPositionBelief(WACSAgent.AGENTNAME, position, heading);
                AgentBearingBelief abb = new AgentBearingBelief(WACSAgent.AGENTNAME, heading, heading);

                _beliefMgr.put(positionBelief);
                _beliefMgr.put(abb);
            }

            if (telem != null)
            {
                _beliefMgr.put(new PiccoloTelemetryBelief(WACSAgent.AGENTNAME, telem));
            }
        }

    }

    @Override
    public void update()
    {

        try
        {
            AgentModeActualBelief agentMode = (AgentModeActualBelief)_beliefMgr.get(AgentModeActualBelief.BELIEF_NAME);
            if (agentMode != null)
            {
                if (agentMode.getMode(WACSAgent.AGENTNAME) != null)
                {
                    if (agentMode.getMode(WACSAgent.AGENTNAME) != null && agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
                    {
                        CircularOrbitBelief circularOrbitBelief = (CircularOrbitBelief) _beliefMgr.get(CircularOrbitBelief.BELIEF_NAME);
                        if (circularOrbitBelief != null)
                        {
                            //Don't send commands if not enabled
                            boolean autopilotControlEnabled = false;
                            EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)_beliefMgr.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                            if (actualControl != null && actualControl.getAllow())
                                autopilotControlEnabled = true;
                            if (!autopilotControlEnabled)
                                return;

                            Date beliefTimestamp = circularOrbitBelief.getTimeStamp();
                            if (_lastUpdateInterceptTime == null || beliefTimestamp.after(_lastUpdateInterceptTime) || ((System.currentTimeMillis() - _lastSendInterceptTime) > m_InterceptUpdatePeriodMs))
                            {
                                _lastUpdateInterceptTime = beliefTimestamp;
                                _lastSendInterceptTime = System.currentTimeMillis();

                                circularOrbitBelief = m_safetyBox.getSafeCircularOrbit(circularOrbitBelief);

                                _groundStationInterface.changeInterceptWaypoint(circularOrbitBelief.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                circularOrbitBelief.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                circularOrbitBelief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS),
                                                                                circularOrbitBelief.getRadius().getDoubleValue(Length.METERS),
                                                                                circularOrbitBelief.getIsClockwise());

                                _groundStationInterface.sendToInterceptWaypoint();
                            }
                        }
                    }
                    else if(agentMode.getMode(WACSAgent.AGENTNAME) != null && agentMode.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                    {
                        RacetrackOrbitBelief racetrackBelief = (RacetrackOrbitBelief) _beliefMgr.get(RacetrackOrbitBelief.BELIEF_NAME);
                        boolean stayOnLoiterApproach = false;
                        if (racetrackBelief != null)
                        {
                            ExplosionTimeActualBelief expTimeBlf = (ExplosionTimeActualBelief)_beliefMgr.get(ExplosionTimeActualBelief.BELIEF_NAME);
                            if (expTimeBlf == null)
                                expTimeBlf = new ExplosionTimeActualBelief(WACSAgent.AGENTNAME, Long.MAX_VALUE);
                            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                            TargetActualBelief targets = (TargetActualBelief) _beliefMgr.get(TargetActualBelief.BELIEF_NAME);
                            LatLonAltPosition gimTarg = targets.getPositionTimeName(gimbalTargetName).getPosition().asLatLonAltPosition();

                            Date beliefTimestamp = racetrackBelief.getTimeStamp();
                            boolean needUpdate = false;

                            if (beliefTimestamp.getTime() > m_RacetrackTimeBeliefTimestamp)
                            {
                                m_RacetrackTimeBeliefTimestamp = beliefTimestamp.getTime();
                                needUpdate = true;
                            }
                            if (expTimeBlf.getTimeStamp().getTime() > m_ExplosionTimeBeliefTimestamp)
                            {
                                m_ExplosionTimeBeliefTimestamp = expTimeBlf.getTimeStamp().getTime();
                                needUpdate = true;

                                if (m_DoLoiterApproach && (Math.abs(expTimeBlf.getTime_ms()-System.currentTimeMillis() - m_LastTimeToExplosionMs) < m_TimeDeltaToSwitchFromApproachPathMs))
                                {
                                    stayOnLoiterApproach = true;
                                }
                            }
                            if (targets.getTimeStamp().getTime() > m_TargetsBeliefTimestamp)
                            {
                                m_TargetsBeliefTimestamp = targets.getTimeStamp().getTime();
                                needUpdate = true;
                            }

                            if (needUpdate)
                            {
                                m_DoLoiterApproach = false;
                                m_ApproachPointsInitialized = false;
                                m_WarnedFinalLoiter = false;
                                m_ApproachPathInvalid = false;
                            }

                            if (needUpdate && stayOnLoiterApproach)
                            {
                                m_DoLoiterApproach = true;
                            }
                            m_LastTimeToExplosionMs = expTimeBlf.getTime_ms()-System.currentTimeMillis();

                            if (needUpdate || _lastUpdateLoiterTime == null || beliefTimestamp.after(_lastUpdateLoiterTime) || ((System.currentTimeMillis() - _lastSendLoiterTime) > m_LoiterUpdatePeriodMs))
                            {
                                _lastUpdateLoiterTime = beliefTimestamp;
                                _lastSendLoiterTime = System.currentTimeMillis();

                                Pic_Telemetry telemetryMessage = null;
                                synchronized (m_telemetryMessageLock)
                                {
                                    telemetryMessage = m_telemetryMessage;
                                }


                                CircularOrbitBelief circularOrbitBelief = null;
                                if (telemetryMessage != null)
                                {
                                    circularOrbitBelief = updateCommandDataLoiterCircle(racetrackBelief, gimTarg, m_LastTimeToExplosionMs, telemetryMessage);
                                    
                                    //Don't send commands if not enabled
                                    boolean autopilotControlEnabled = false;
                                    EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)_beliefMgr.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                                    if (actualControl != null && actualControl.getAllow())
                                        autopilotControlEnabled = true;
                                    if (!autopilotControlEnabled)
                                        return;

                                    if (m_PermitZeroRadiusOrbit)
                                        circularOrbitBelief = m_safetyBox.getSafeCircularOrbitIgnoreRadius(circularOrbitBelief);
                                    else
                                        circularOrbitBelief = m_safetyBox.getSafeCircularOrbit(circularOrbitBelief);

                                    _groundStationInterface.changeLoiterWaypoint(circularOrbitBelief.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                    circularOrbitBelief.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES),
                                                                                    circularOrbitBelief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS),
                                                                                    circularOrbitBelief.getRadius().getDoubleValue(Length.METERS),
                                                                                    circularOrbitBelief.getIsClockwise());

                                    _groundStationInterface.sendToLoiterWaypoint();

                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
    
    private void updateContactPositionCart(LatLonAltPosition agentLatLon)
    {
        m_ContactPositionCart = m_ContactPositionLLA.asCartesianPosition(agentLatLon);
        m_ContactPositionCart = new CartesianPosition(m_ContactPositionCart.getX(), m_ContactPositionCart.getY(), new Length(0, Length.METERS));
    }

    private CartesianPosition getClosestPointOnPathCart(CartesianPosition contactPositionCart, CartesianPosition centerPositionCart)
    {
        double dx = contactPositionCart.getX().getDoubleValue(Length.METERS) - centerPositionCart.getX().getDoubleValue(Length.METERS);
        double dy = contactPositionCart.getY().getDoubleValue(Length.METERS) - centerPositionCart.getY().getDoubleValue(Length.METERS);
        double cx = 0 - centerPositionCart.getX().getDoubleValue(Length.METERS); //agent is at x=0
        double cy = 0 - centerPositionCart.getY().getDoubleValue(Length.METERS); //agent is at y=0
        double t = (cx*dx + cy*dy)/(dx*dx + dy*dy);
        double x0 = centerPositionCart.getX().getDoubleValue(Length.METERS) + t*dx;
        double y0 = centerPositionCart.getY().getDoubleValue(Length.METERS) + t*dy;
        CartesianPosition closestPointOnPathCart = new CartesianPosition(new Length (x0, Length.METERS), new Length(y0, Length.METERS), Length.ZERO);

        return closestPointOnPathCart;
    }

    private RangeBearingHeightOffset computeMovementVectorToFollowPath(CartesianPosition toPosition, CartesianPosition fromPosition)
    {
        m_ClosestPointOnPathCart = getClosestPointOnPathCart (toPosition, fromPosition);

        //Vector to path
        Vector3 toPathVector = new Vector3(m_ClosestPointOnPathCart, Length.METERS);
        Length distToPath = toPathVector.length();
        toPathVector.normalize();

        // create the path tangent vector
        RangeBearingHeightOffset rbh = fromPosition.getRangeBearingHeightOffsetTo(toPosition);
        Vector3 tangent = new Vector3 (rbh.asCartesianPosition(CartesianPosition.ORIGIN), Length.METERS);
        tangent.normalize();

        //Form movement vector by combining all vectors
        RangeBearingHeightOffset targetMovementVector = null;
        double errorRatio = distToPath.dividedBy(m_minTurnRadius).getDoubleValue();
        toPathVector.scale(errorRatio * m_toApproachPathVectorBias);
        targetMovementVector = toPathVector.plus(tangent).normalized().asRangeBearingHeightOffset();

        return targetMovementVector;
    }

    private CircularOrbitBelief updateCommandDataLoiterCircle(RacetrackOrbitBelief racetrackBelief, LatLonAltPosition targetPosition, long timeUntilExplosionMs, Pic_Telemetry telemetryMessage)
    {
        //
        // Calculate vector to circular orbit
        //
        LatLonAltPosition agentLatLon = new LatLonAltPosition(new Latitude(telemetryMessage.Lat, Angle.DEGREES),
                                                              new Longitude(telemetryMessage.Lon, Angle.DEGREES),
                                                              new Altitude(telemetryMessage.AltMSL, Length.METERS));

        m_PermitZeroRadiusOrbit = false;
        
        if (agentLatLon == null || racetrackBelief == null)
        {
            //do nothing
        }
        else
        {
            // get the rotation center position, defaulting to the set one
            LatLonAltPosition centerPositionLLA = new LatLonAltPosition(racetrackBelief.getLatitude1(), racetrackBelief.getLongitude1(), racetrackBelief.getFinalAltitudeMsl());
            CartesianPosition centerPositionCart = centerPositionLLA.asCartesianPosition(agentLatLon);
            centerPositionCart = new CartesianPosition(centerPositionCart.getX(), centerPositionCart.getY(), new Length(0, Length.METERS));

            // get target location
            LatLonAltPosition targetPositionLLA = targetPosition;
            CartesianPosition targetPositionCart = targetPositionLLA.asCartesianPosition(agentLatLon);
            targetPositionCart = new CartesianPosition(targetPositionCart.getX(), targetPositionCart.getY(), new Length(0, Length.METERS));

            if (centerPositionCart != null)
            {
                if (targetPositionLLA.getRangeTo(centerPositionLLA).isLessThan(racetrackBelief.getRadius()))
                {
                    //We always do a simple orbit for this case (target within orbit radius), no approach path
                    //do regular circle orbit loiter.  Stay at final orbit the entire time
                    m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;
                    LoiterApproachPathBelief points = new LoiterApproachPathBelief(null, null, null, false);
                    _beliefMgr.put(points);
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, centerPositionLLA.asLatLonAltPosition(), racetrackBelief.getRadius(), racetrackBelief.getIsClockwise());
                }

                //If we got here, then we are doing an offset loiter with an approach path.  Figure out what part we are on and move accordingly



                //If not on loiter approach pattern yet, check time required to get to contact point on approach path.  If have more time than
                //needed, continue orbit.  If not enough time, begin approach path.
                if (!m_DoLoiterApproach)
                {
                    //Initialize contact point and path whenever orbit/target/time is changed
                    if (!m_ApproachPointsInitialized)
                    {
                        NavyAngle bearingCenterToTarget = centerPositionLLA.getBearingTo(targetPositionLLA);
                        double safeDistanceMeters = racetrackBelief.getRadius().getDoubleValue(Length.METERS);
                        Length safeDistance = racetrackBelief.getRadius();
                        double firstRangeDistanceMeters = Math.max(safeDistanceMeters, Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.FirstRangeDistanceFromTarget.Meters", 2500));
                        Length firstRangeDistance = new Length (firstRangeDistanceMeters, Length.METERS);
                        //double contactDistanceMeters = (safeDistanceMeters + firstRangeDistanceMeters)/2;
                        //Length contactDistance = new Length (contactDistanceMeters, Length.METERS);

                        double theta = Math.asin(safeDistance.getDoubleValue(Length.METERS) / centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS));
                        if (m_ApproachTargetFromLeft)
                            theta = -theta;
                        NavyAngle bearingCenterToSafePoint = bearingCenterToTarget.plus(new Angle (theta, Angle.RADIANS));
                        m_BearingOfLoiterApproachPath  = (NavyAngle)bearingCenterToSafePoint.clone();
                        //Length distFromSafeToContactPoints = new Length (Math.sqrt(contactDistance.times(contactDistance).minus(safeDistance.times(safeDistance)).getDoubleValue(Area.METERS_SQUARED)), Length.METERS);
                        Length distFromSafeToFirstRangePoints = new Length (Math.sqrt(firstRangeDistance.times(firstRangeDistance).minus(safeDistance.times(safeDistance)).getDoubleValue(Area.METERS_SQUARED)), Length.METERS);
                        Length distFromSafeToContactPoints = distFromSafeToFirstRangePoints.dividedBy(2);
                        Length distFromCenterToContactPoint = new Length (centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS)*Math.cos(theta) - distFromSafeToContactPoints.getDoubleValue(Length.METERS), Length.METERS);
                        Length distFromCenterToFirstRangePoint = new Length (centerPositionLLA.getRangeTo(targetPositionLLA).getDoubleValue(Length.METERS)*Math.cos(theta) - distFromSafeToFirstRangePoints.getDoubleValue(Length.METERS), Length.METERS);
                        m_ContactPositionLLA = centerPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromCenterToContactPoint, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                        m_SafePositionLLA = m_ContactPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromSafeToContactPoints, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                        m_FirstRangePositionLLA = centerPositionLLA.translatedBy(new RangeBearingHeightOffset(distFromCenterToFirstRangePoint, bearingCenterToSafePoint, Length.ZERO)).asLatLonAltPosition();
                        m_DiffContactToFirstRangePoints = m_ContactPositionLLA.getRangeTo(m_FirstRangePositionLLA);
                        m_DiffCenterToSafePoints = centerPositionLLA.getRangeTo(m_SafePositionLLA);
                        m_DiffCenterToFirstRangePoint = centerPositionLLA.getRangeTo(m_FirstRangePositionLLA);
                        m_ApproachPointsInitialized = true;

                        if (m_FirstRangePositionLLA.getRangeTo(centerPositionLLA).isLessThan(racetrackBelief.getRadius())
                            || targetPositionLLA.getRangeTo(centerPositionLLA).isLessThan(firstRangeDistance)
                            || !m_safetyBox.isPointInsideSafetyBox (m_SafePositionLLA)
                            || !m_safetyBox.isOrbitInsideSafetyBox (targetPositionLLA, racetrackBelief.getRadius()))
                            m_ApproachPathInvalid = true;

                        LoiterApproachPathBelief points = new LoiterApproachPathBelief(m_SafePositionLLA, m_ContactPositionLLA, m_FirstRangePositionLLA, !m_ApproachPathInvalid);
                        _beliefMgr.put(points);
                    }


                    /*
                     * Estimate of time required to get to contact point along approach path uses two calculations.
                     *  - Time required to turn plane from current bearing to approach path bearing (ignoring that plane really turns to fly directly towards approach path, then turns to approach path bearing)
                     *  - Time required for plane to fly straight to contact point from the current point (ignoring rotation and ignoring that plane doesn't fly directly to contact point, but first gets on approach path)
                     *
                     * Adjustments:
                     *  Turn radius of rotation is assumed to be minimum turn radius with no wind speed conditions
                     *  Total turn rotation depends on which way the plane turns, which is dependent on movement vectors
                     *  Straight line path from current plane position to contact position is offset by distance plane blown by wind during rotation
                     *  Straight line path time is affected by wind speed estimate parallel to this fake, straight-to-contact path.  In actuality, the wind speed parallel to the actual flight path will affect time, but this is an estimate
                     *  Speed during approach path can be increased if we are running late.  This is depenedent on time to contact point and actual ground speed based on wind speed parallel to the appraoch path.  If we are early, the plane does not slow down.  Must be back at slower airspeed by first range point (before contact point)
                     *
                     * Altitude:
                     *  Fly at standoff altitude until descent rate and time until explosion dictate we must descend to be at the final altitude by the first range point (before contact point)
                     *  Even if we have sufficient time to descend, we will start descend to final altitude as soon as approach pattern begins.
                     */



                    //Find rotation angle from current heading to path vector
                    RangeBearingHeightOffset targetMovementVector = null;
                    updateContactPositionCart(agentLatLon);
                    targetMovementVector = computeMovementVectorToFollowPath (m_ContactPositionCart, centerPositionCart);
                    //targetMovementVector is which way plane will first turn.  Assume this is the rotation direction, and find angle to path
                    //assuming this direction.  Might be greater than 180 degrees.
                    NavyAngle rotation = null;
                    if (Math.cos(new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS)) <= 0)
                    {
                        if(targetMovementVector.getBearing().clockwiseAngleTo(new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES)).isLessThan(Angle.HALF_CIRCLE))
                        {
                            //If not already going in general direction of the eventual path, vectors indicate we'll turn counter-clockwise to path
                            rotation = new NavyAngle (telemetryMessage.Yaw, Angle.DEGREES).counterClockwiseAngleTo(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                        }
                        else
                        {
                            //If not already going in general direction of the eventual path, vectors indicate we'll turn clockwise to path
                            rotation = new NavyAngle (telemetryMessage.Yaw, Angle.DEGREES).clockwiseAngleTo(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                        }
                    }
                    else if(Math.cos(m_BearingOfLoiterApproachPath.shortAngleBetween(agentLatLon.getBearingTo(m_FirstRangePositionLLA)).getDoubleValue(Angle.RADIANS)) <= 0)
                    {
                        //Sign of cosine between path and bearing to first point is sign of dot product If sign is positive, vectors are similar direction

                        //This means we are on wrong side of path and going the wrong way.  We need to do big loop back to get on track.
                        rotation = new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES).longAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                    }
                    else if (Math.cos(new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS)) > 0)
                    {
                        //Sign of cosine between heading and path is sign of dot product of heading and path.  If sign is positive, vectors are similar direction

                        //If we are already going in the general direction of the eventual path and are pointing towards the contact point, we'll take the short turn to continue on the path.
                        rotation = new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES).shortAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                    }

                    //Get time for rotation and offset of origination point assuming blown by wind during rotation
                    Length distAgentRotateToContact = m_minTurnRadius.times(rotation.getDoubleValue(Angle.RADIANS));
                    Time timeAgentRotate = distAgentRotateToContact.dividedBy(m_ActualCommandedAirspeed);
                    Length distBlownByWindDuringRotation = m_WindHistory.getAvgWindSpeed().times(timeAgentRotate);
                    LatLonAltPosition agentLatLonOffsetByWindDuringRotation = agentLatLon.translatedBy(new RangeBearingHeightOffset(distBlownByWindDuringRotation, m_WindHistory.getAvgWindDir(), Length.ZERO)).asLatLonAltPosition();

                    //Correct for wind blowing against approach path
                    NavyAngle bearingDiffWindToPath = m_WindHistory.getAvgWindDir().shortAngleBetween(m_BearingOfLoiterApproachPath).plus(NavyAngle.ZERO);
                    Speed windSpeedAlongPath = m_WindHistory.getAvgWindSpeed().times(Math.cos(bearingDiffWindToPath.getDoubleValue(Angle.RADIANS)));
                    NavyAngle bearingDiffWindToStraightFakePath = m_WindHistory.getAvgWindDir().shortAngleBetween(agentLatLon.getBearingTo(m_FirstRangePositionLLA)).plus(NavyAngle.ZERO);
                    Speed windSpeedAlongStraightFakePath = m_WindHistory.getAvgWindSpeed().times(Math.cos(bearingDiffWindToStraightFakePath.getDoubleValue(Angle.RADIANS)));

                    //Compute time for straight approach path
                    Length distAgentStraightToContact = agentLatLonOffsetByWindDuringRotation.getRangeTo(m_FirstRangePositionLLA);
                    m_ApproachPathGroundSpeed = m_ActualCommandedAirspeed.plus(windSpeedAlongPath);
                    Speed straightFakeApproachPathGroundSpeed = m_ActualCommandedAirspeed.plus(windSpeedAlongStraightFakePath);
                    Time timeAgentPath = distAgentStraightToContact.dividedBy(straightFakeApproachPathGroundSpeed);

                    //Sum time required for rotation and approach path for total time
                    Time timeAgentToFirstRange = timeAgentPath.plus (timeAgentRotate);
                    Time timeFirstRangeToContact = m_ContactPositionLLA.getRangeTo(m_FirstRangePositionLLA).dividedBy(m_ApproachPathGroundSpeed);
                    m_TimeAgentToContact = timeAgentToFirstRange.plus (timeFirstRangeToContact);


                    //If we don't have time to screw around anymore, start the approach.
                    if (m_TimeAgentToContact.getDoubleValue(Time.MILLISECONDS) > timeUntilExplosionMs)
                    {
                        m_DoLoiterApproach = true;
                    }

                    /*System.out.println ("Straight length:" + distAgentStraightToContact);
                    System.out.println ("Angle:" + rotation.getDoubleValue (Angle.DEGREES) + " deg");
                    System.out.println ("Rotate length:" + distAgentRotateToContact);*/
                    System.out.println ("Time agent to contact:    " + m_TimeAgentToContact.getDoubleValue(Time.SECONDS) + " sec");
                }


                //Set altitude of loiter
                Altitude commandedAltitude = racetrackBelief.getFinalAltitudeMsl();
                if (m_DoLoiterApproach)
                {
                    //If on approach, force altitude to final altitude immediately.
                    commandedAltitude = racetrackBelief.getFinalAltitudeMsl();
                }
                else
                {
                    //If not on approach, start descent to final altitude when descent rate dictates time required
                    Time estTimeAgentToFirstRangePoint = new Time (timeUntilExplosionMs, Time.MILLISECONDS).minus(m_DiffContactToFirstRangePoints.dividedBy(m_ActualCommandedAirspeed));
                    Time timeReqdForDescentToFinalAltitude = new Time (Math.max((racetrackBelief.getStandoffAltitudeMsl().getDistanceAbove(racetrackBelief.getFinalAltitudeMsl())).getDoubleValue(Length.METERS), 0)/m_AltitudeDescentRate.getDoubleValue(Speed.METERS_PER_SECOND), Time.SECONDS);
                    Time timeReqdForAscentToStandoffAltitude = new Time (Math.max((racetrackBelief.getStandoffAltitudeMsl().getDistanceAbove(new Altitude(telemetryMessage.AltMSL, Length.METERS))).getDoubleValue(Length.METERS), 0)/m_AltitudeAscentRate.getDoubleValue(Speed.METERS_PER_SECOND), Time.SECONDS);

                    if (estTimeAgentToFirstRangePoint.isLessThan(timeReqdForDescentToFinalAltitude.plus(timeReqdForAscentToStandoffAltitude)))
                    {
                        commandedAltitude = racetrackBelief.getFinalAltitudeMsl();
                    }
                    else
                    {
                        commandedAltitude = racetrackBelief.getStandoffAltitudeMsl();
                    }
                }



                //Set path of loiter
                //If loiter approach is active -
                if(m_DoLoiterApproach && agentLatLon.getRangeTo(targetPositionLLA).isLessThan (racetrackBelief.getRadius().times (m_FinalRacetrackOrbitBufferPercentage)))
                {
                    if (!m_WarnedFinalLoiter && new NavyAngle(telemetryMessage.Yaw, Angle.DEGREES).shortAngleBetween(m_BearingOfLoiterApproachPath).isGreaterThan(m_AngleAroundFinalOrbitToWarn))
                    {
                        m_WarnedFinalLoiter = true;

                        new Thread () {
                            public void run ()
                            {
                                JOptionPane.showMessageDialog(null, "WACS reached final loiter orbit without detecting\nexplosion.  Designate new release time or continue final orbit", "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        }.start();
                    }
        
                    //if on loiter approach and are very close to final orbit/safe position, switch to orbit around target
                    //do regular circle orbit loiter, but reverse direction because we're not offset anymore
                    targetPositionLLA = new LatLonAltPosition (targetPositionLLA.getLatitude(),
                                                                targetPositionLLA.getLongitude(),
                                                                commandedAltitude);
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, targetPositionLLA, racetrackBelief.getRadius(), !racetrackBelief.getIsClockwise());
                }
                else if(m_DoLoiterApproach && agentLatLon.getRangeTo(centerPositionLLA).times(Math.cos(centerPositionLLA.getBearingTo(agentLatLon).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS))).isGreaterThan(m_DiffCenterToSafePoints))
                {
                    //if on loiter approach but are further down path than final orbit, go back to do regular circle orbit loiter, but reverse direction because we're not offset anymore
                    targetPositionLLA = new LatLonAltPosition (targetPositionLLA.getLatitude(),
                                                                targetPositionLLA.getLongitude(),
                                                                commandedAltitude);
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, targetPositionLLA, racetrackBelief.getRadius(), !racetrackBelief.getIsClockwise());
                }
                else if (m_DoLoiterApproach && agentLatLon.getRangeTo(centerPositionLLA).times(Math.cos(centerPositionLLA.getBearingTo(agentLatLon).shortAngleBetween(m_BearingOfLoiterApproachPath).getDoubleValue(Angle.RADIANS))).isGreaterThan(m_DiffCenterToFirstRangePoint.times(m_DistCenterToFirstRangeFactorForPathStart)))
                {
                    //abort the loiter and get on the approach path quickly
                    //System.out.println("Approach!");

                    //Set airpseed to default airspeed
                    m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;

                    LatLonAltPosition safePositionLLA = new LatLonAltPosition (m_SafePositionLLA.getLatitude(),
                                                                m_SafePositionLLA.getLongitude(),
                                                                commandedAltitude);
                    m_PermitZeroRadiusOrbit = true;
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, safePositionLLA, new Length (0, Length.METERS), true);
                }
                else if (m_DoLoiterApproach)
                {
                    //follow the approach path
                    //System.out.println("Approach!");

                    //Set airpseed to default airspeed
                    m_ActualCommandedAirspeed = m_DesiredCommandedAirspeed;

                    LatLonAltPosition safePositionLLA = new LatLonAltPosition (m_FirstRangePositionLLA.getLatitude(),
                                                                m_FirstRangePositionLLA.getLongitude(),
                                                                commandedAltitude);
                    m_PermitZeroRadiusOrbit = true;
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, safePositionLLA, new Length (0, Length.METERS), true);
                }
                else
                {
                    //do regular circle orbit loiter
                    centerPositionLLA = new LatLonAltPosition (centerPositionLLA.getLatitude(),
                                                                centerPositionLLA.getLongitude(),
                                                                commandedAltitude);
                    return new CircularOrbitBelief (WACSAgent.AGENTNAME, centerPositionLLA, racetrackBelief.getRadius(), racetrackBelief.getIsClockwise());
                }

            }
        }

        LoiterApproachPathBelief points = new LoiterApproachPathBelief(null, null, null, false);
        _beliefMgr.put(points);
        return new CircularOrbitBelief(WACSAgent.AGENTNAME, new LatLonAltPosition(racetrackBelief.getLatitude1(), racetrackBelief.getLongitude1(), racetrackBelief.getFinalAltitudeMsl()), racetrackBelief.getRadius(), racetrackBelief.getIsClockwise());
    }

}
