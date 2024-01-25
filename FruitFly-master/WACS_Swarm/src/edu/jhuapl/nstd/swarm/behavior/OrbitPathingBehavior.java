/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.behavior;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.SafetyBox;
import edu.jhuapl.nstd.util.RectangleRegion;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 * biggimh1
 */
public class OrbitPathingBehavior extends Behavior {

    protected static final RangeBearingHeightOffset RANGE_ZERO = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
    private TestCircularOrbitBelief m_currentCircularOrbitBelief;
    private ArrayList<LatLonAltPosition> orbitPath = new ArrayList<LatLonAltPosition>();
    private LatLonAltPosition m_currentTargetCenter;
    private double m_timeAccelerationCoefficient;
    private final double m_ReachedInterceptOrbitFactor;
    //private final double m_DistanceToReverseInOrbits;
    private final int m_updatePeriod_ms;
    private long m_PrevAltUpdateTimeMs;
    private long m_lastUpdateTime_ms;
    //private long m_lastUpdateDuration_ms = 0;
    private SafetyBox m_safetyBox;
    private Length m_orbitRadius;
    //private boolean _onAlignOrbit = true;
    //private boolean _useHeading = false;
    protected int _xyBuffer;
    protected int _minAGLBuffer;
    protected LatLonAltPosition dtedCenter = null;
    private TreeMap<Integer, TreeMap<Integer, AltitudePair>> dtedMap =new TreeMap<Integer, TreeMap<Integer, AltitudePair>>();;
    private Length pathOrbitRadius;
    private double minimumRadius;
    private double minRadiusAdjustmentFactor;
    private KeepOutRegionBelief keepOutRegionBelief=null;
    Mode mode = Mode.ORBIT;
    
    //BufferedWriter out;

       
    public enum Mode{NEW, ALIGN, PATH, ORBIT};

    public OrbitPathingBehavior(BeliefManager mngr, String agentID) {
        super(mngr, agentID);
        
        /*try {
            out = new BufferedWriter(new FileWriter(new File("C:\\Users\\biggimh1\\Desktop\\orbitPathingOutput.txt")));
        } catch (IOException ex) {
            Logger.getLogger(OrbitPathingBehavior.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate") && !Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation")) {
            m_timeAccelerationCoefficient = Config.getConfig().getPropertyAsDouble("SimulationMode.timeAccelerationCoefficient", 1);
        } else {
            m_timeAccelerationCoefficient = 1;
        }

        m_ReachedInterceptOrbitFactor = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.reachedInterceptOrbitFactor", 1.8);
        //m_DistanceToReverseInOrbits = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.distanceToReverseInOrbits", 1.25);
        m_updatePeriod_ms = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.updatePeriod_ms");


        m_lastUpdateTime_ms = 0;
        m_PrevAltUpdateTimeMs = System.currentTimeMillis();
        
        m_safetyBox = new SafetyBox(this.beliefMgr);
        
        _minAGLBuffer = (int)(Config.getConfig().getPropertyAsDouble("WACSAgent.MinAgl_ft",300)*.3048+1);
        _xyBuffer = (int)(Config.getConfig().getPropertyAsDouble("WACSAgent.HorizontalBuffer_ft",300)*.3048+1);
        
        _xyBuffer = (_xyBuffer<_minAGLBuffer) ? _minAGLBuffer: _xyBuffer;
        
        minimumRadius = Config.getConfig().getPropertyAsDouble("WACSAgent.minumumRadius_m", 304.8);
        pathOrbitRadius = new Length(minimumRadius, Length.METERS);
        minRadiusAdjustmentFactor = Config.getConfig().getPropertyAsDouble("WACSAgent.minRadiusAdjustmentFactor_m", 100);
    }

    @Override
    public synchronized RangeBearingHeightOffset getResult() {
        return RANGE_ZERO;
    }

    /**
     * Retrieve a test belief if there is one.
     * The orbit should already be adjusted to a safe position
     * Calculate a safe path and
     */
    @Override
    public void update() {
        try {
            //
            // Since all behaviors get called all the time, we need to check if we are in the correct mode
            // for this behavior.  Normally this is not necessary since most behaviors output a vector
            // that can be scaled, but this behavior
            //
            TestCircularOrbitBelief testCOB = (TestCircularOrbitBelief) beliefMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
            if (testCOB != null) {


                if ((System.currentTimeMillis() - m_lastUpdateTime_ms) > m_updatePeriod_ms) {
                    //True if we just got a new orbit,  this should mean that we just finished the last Test orbit
                    PositionTimeName planePosition = null;
                    LatLonAltPosition agentPosition = null;
                    AgentPositionBelief agentPositionBelief = (AgentPositionBelief) beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
                    PiccoloTelemetryBelief telemetryBelief = (PiccoloTelemetryBelief) beliefMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
                    double currentUAVSpeed_mps = 0;
                    double windSpeedSouth = 0;
                    double windSpeedWest = 0;
                    if (agentPositionBelief != null) {
                        planePosition = agentPositionBelief.getPositionTimeName(agentID);
                        if (planePosition != null) {
                            
                            //Recalculate path due to wind change conditions
                            if(telemetryBelief!=null){
                                currentUAVSpeed_mps = telemetryBelief.getPiccoloTelemetry().IndAirSpeed_mps;
                                windSpeedSouth = telemetryBelief.getPiccoloTelemetry().WindSouth;
                                windSpeedWest = telemetryBelief.getPiccoloTelemetry().WindWest;
                                
                                if(currentUAVSpeed_mps>0){
                                    
                                    double minRadius = testCOB.getMinimumTurnRadiusMeters(currentUAVSpeed_mps, windSpeedSouth, windSpeedWest);
                                    if(minRadius<minimumRadius)minRadius = minimumRadius;
                                    
                                    if(minRadius>pathOrbitRadius.getDoubleValue(Length.METERS)){
                                        pathOrbitRadius = new Length(minRadius+minRadiusAdjustmentFactor, Length.METERS);
                                        testCOB.setIsNew(true);
                                    }else if(minRadius <pathOrbitRadius.getDoubleValue(Length.METERS)-minRadiusAdjustmentFactor){
                                        pathOrbitRadius = new Length(minRadius, Length.METERS);
                                        testCOB.setIsNew(true);
                                    }
                                    
                                    if(minRadius>testCOB.getRadius().getDoubleValue(Length.METERS)){
                                        testCOB.setRadius(new Length(minRadius+minRadiusAdjustmentFactor, Length.METERS));
                                        testCOB.setIsNew(true);
                                    }else if(minRadius <testCOB.getRadius().getDoubleValue(Length.METERS)-minRadiusAdjustmentFactor){
                                        //testCOB.setRadius(new Length(minRadius+minRadiusAdjustmentFactor, Length.METERS));
                                        //testCOB.setIsNew(true);
                                    }
                                    
                                }
                            }
                            
                            KeepOutRegionBelief tempKeepOutRegionBelief = (KeepOutRegionBelief)beliefMgr.get(KeepOutRegionBelief.BELIEF_NAME);
                            if(tempKeepOutRegionBelief!=null){
                                if(keepOutRegionBelief!=null){
                                    if(!tempKeepOutRegionBelief.getTimeStamp().equals(keepOutRegionBelief.getTimeStamp())){
                                        testCOB.setIsNew(true);
                                    }
                                }else{
                                    keepOutRegionBelief = tempKeepOutRegionBelief;
                                    testCOB.setIsNew(true);
                                }
                            }
                            
                            //Recalculate Path due to safety box change
                            
                            
                            agentPosition = planePosition.getPosition().asLatLonAltPosition();
                                                        
                            
                            if (testCOB.isNew()) {
                                   mode = Mode.NEW;
                                //Initialize stuff
                                dtedMap.clear();
                                                                
                                //TODO pick a better center point
                                dtedCenter = agentPosition.translatedBy(
                                new RangeBearingHeightOffset(
                                testCOB.getRadius(),
                                planePosition.getHeading().plus(Angle.THREE_QUARTERS_CIRCLE),
                                Length.ZERO)).asLatLonAltPosition();
                                m_currentTargetCenter = dtedCenter;
                                m_safetyBox.setIgnoreSafetyBox(false);
                                
                                orbitPath = null;
                            }
                            if(mode == Mode.NEW){
                                                               
                               newOrbitMode(testCOB, agentPosition,  planePosition);
                            } else if(mode == Mode.ALIGN){
                                
                                if(!flyUavToPath(agentPosition, planePosition.getHeading())){
                                    mode = Mode.PATH;
                                    if(orbitPath!=null && orbitPath.size()>0){
                                        newOrbitPathOrbit(agentPosition);
                                    }
                                }
                                
                            }else if(mode == Mode.PATH){

                                if(!flyOrbitPath(agentPosition,planePosition.getHeading())){
                                    if(this.m_currentCircularOrbitBelief.isRacetrack()){
                                        flyRacetrackOrbitBelief();
                                    }else{
                                        flyCircularOrbitBelief();
                                    }
                                    mode=Mode.ORBIT;
                                }
                            }else{
                                
                                //Do noting until there is a new TCOB
                                if(mode != Mode.ORBIT){
                                    throw new Exception("Bad mode in OrbitPathingBehavior");
                                }
                                if ((System.currentTimeMillis() - m_PrevAltUpdateTimeMs) * m_timeAccelerationCoefficient > m_updatePeriod_ms) {
                                    updateOrbitAltitude(testCOB);
                                    m_PrevAltUpdateTimeMs = System.currentTimeMillis();
                                }
                            }
                        }
                        //out.flush();
                    }


                    //Update time
                    long currTime_ms = System.currentTimeMillis();
                    //if (m_lastUpdateTime_ms != 0) {
                    //    m_lastUpdateDuration_ms = (long) (currTime_ms - m_lastUpdateTime_ms);
                    //}
                    m_lastUpdateTime_ms = currTime_ms;
                }

            }

        } catch (Exception e) {
            System.err.println("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    /*
     * Find the path for a new orbit.
     * 
     * If there is no acceptable path for the requested orbit, then
     */
    private void newOrbitMode(TestCircularOrbitBelief testCOB, LatLonAltPosition agentPosition, PositionTimeName planePosition) throws Exception {
        //out.write("TCOB: " + testCOB.toString());
       //out.newLine();
        //_useHeading = false;
        m_currentCircularOrbitBelief = testCOB;
        m_currentCircularOrbitBelief.use();
        //out.write("TCOB before move: " + m_currentCircularOrbitBelief.toString());
        //out.newLine();
        m_currentCircularOrbitBelief = adjustOrbitCenterToSafeLocation(agentPosition, m_safetyBox, m_currentCircularOrbitBelief);

        beliefMgr.put(m_currentCircularOrbitBelief);

        m_currentCircularOrbitBelief = (TestCircularOrbitBelief) beliefMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
        //out.write("TCOB after move: " + m_currentCircularOrbitBelief.toString());
        //out.newLine();
        //Initialize position to orbit position,  if we have a position belief update this to the correct value
        double heading = 0;
        if (agentPosition == null) {
            agentPosition = m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
        }
        if (planePosition != null) {
            heading = planePosition.getHeading().getDoubleValue(Angle.DEGREES);
        }

        //out.write("OrbitPath AgentPositoin is: " + agentPosition.asLatLonAltPosition().toString() + " " + agentPosition.getAltitude().getDoubleValue(Length.METERS));
        //out.newLine();
        m_orbitRadius = m_currentCircularOrbitBelief.getRadius();

        try {
            if(!m_safetyBox.isOrbitInsideSafetyBox(agentPosition, testCOB.getRadius().plus(new Length(_xyBuffer,Length.METERS)))){
                m_safetyBox.setIgnoreSafetyBox(true);
            }
            
            orbitPath = getSafePathToOrbit(agentPosition, m_safetyBox, heading, m_currentCircularOrbitBelief);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        if(m_currentCircularOrbitBelief.isRacetrack()){
            double maxAlt = m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
            double temp = m_currentCircularOrbitBelief.getFinalAltitude().getDoubleValue(Length.METERS);
            
            if(temp<maxAlt){
                maxAlt = temp;
            }
            
            maxAlt -= _minAGLBuffer;
            
            getIsRacetrackSafe(maxAlt);
        }

        if (orbitPath == null) {
            //No good path was found.
            //Push a new TCOB to the belief manager so PCPB requests a new orbit
            CircularOrbitBelief cob = (CircularOrbitBelief) beliefMgr.get(CircularOrbitBelief.BELIEF_NAME);
            if (cob != null) {
                m_currentCircularOrbitBelief.setPosition(cob.getPosition());
                m_currentCircularOrbitBelief.setIsClockwise(cob.getIsClockwise());

                m_currentCircularOrbitBelief.use();
                beliefMgr.put(m_currentCircularOrbitBelief);
            } else {
                //Worst case?
                m_currentCircularOrbitBelief.setPosition(dtedCenter);


                m_currentCircularOrbitBelief.setIsClockwise(false);

                m_currentCircularOrbitBelief.use();
                beliefMgr.put(m_currentCircularOrbitBelief);

            }

            mode = Mode.ORBIT;
            //No prior orbit either. Do not change the orbit the autopilot has.  The autopilot is responsible for the plane at this point.
        } else {

            //out.write("TCOB after plan path: " + m_currentCircularOrbitBelief.toString());
            //out.newLine();
            newAlignOrbit(agentPosition, planePosition.getHeading());
            mode = Mode.ALIGN;
        }
    }
    
    private void newAlignOrbit(LatLonAltPosition agentPosition, NavyAngle heading) throws Exception{
        double minAlt = agentPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        boolean isClockwise = false;
        LatLonAltPosition ccwOrbit = agentPosition.translatedBy(new RangeBearingHeightOffset(
                                    pathOrbitRadius,
                                    heading.plus(Angle.THREE_QUARTERS_CIRCLE),
                                    Length.ZERO)).asLatLonAltPosition();
        LatLonAltPosition cwOrbit = agentPosition.translatedBy(new RangeBearingHeightOffset(
                                    pathOrbitRadius,
                                    heading.plus(Angle.RIGHT_ANGLE),
                                    Length.ZERO)).asLatLonAltPosition();
        
        LatLonAltPosition orbit = null;
        
        double ccwAlt = getAreaAltitude(ccwOrbit, (int)(pathOrbitRadius.getDoubleValue(Length.METERS)+2*this._xyBuffer +1), m_safetyBox, m_currentCircularOrbitBelief);
        double cwAlt = getAreaAltitude(cwOrbit, (int)(pathOrbitRadius.getDoubleValue(Length.METERS)+2*this._xyBuffer +1), m_safetyBox, m_currentCircularOrbitBelief);
        
        if(ccwAlt>minAlt){
            if(cwAlt>minAlt){
                throw new Exception();
            }else{
                orbit = cwOrbit;
                isClockwise = true;
            }
        }else if(cwAlt>minAlt){
            orbit = ccwOrbit;
        }else{
            LatLonAltPosition next;
            if(orbitPath!=null &&orbitPath.size()>0){
                next = orbitPath.get(0);
            }else{
                next = m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
            }
            Length cwlen = cwOrbit.getRangeTo(next);
            Length ccwlen = ccwOrbit.getRangeTo(next);
            if(cwOrbit.getRangeTo(next).isLessThan(ccwOrbit.getRangeTo(next))){
                orbit = cwOrbit;
                isClockwise = true;
            }else{
                orbit = ccwOrbit;
            }
            
        }
        
            
        if (m_currentCircularOrbitBelief.isRacetrack()) {
            Altitude finalAltitude = m_currentTargetCenter.getAltitude();

            if (m_currentTargetCenter == m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition()) {
                finalAltitude = this.m_currentCircularOrbitBelief.getFinalAltitude();
            }
            RacetrackOrbitBelief rob = new RacetrackOrbitBelief(orbit.getLatitude(), orbit.getLongitude(),
                    finalAltitude, orbit.getAltitude(), pathOrbitRadius, isClockwise);
            this.beliefMgr.put(rob);
        } else {

            CircularOrbitBelief cob = new CircularOrbitBelief(agentID, orbit, pathOrbitRadius, isClockwise);
            beliefMgr.put(cob);
        }
        
    }
    
    private boolean flyUavToPath(LatLonAltPosition agentPosition, NavyAngle heading) throws Exception {
        if(doneWaypoint(agentPosition, agentPosition,heading)) {
            return false;
        }
        return true;
    }
    
    private boolean flyOrbitPath(LatLonAltPosition agentPosition, NavyAngle heading) throws Exception {
        if (orbitPath != null && !orbitPath.isEmpty()){
            if(doneWaypoint(m_currentTargetCenter, agentPosition,heading)) {
                orbitPath.remove(0);
                if(orbitPath.size()>0){
                    newOrbitPathOrbit(agentPosition);
                    m_PrevAltUpdateTimeMs = System.currentTimeMillis();
                    return true;
                }else{
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private void flyCircularOrbitBelief(){
        m_PrevAltUpdateTimeMs = System.currentTimeMillis();
        CircularOrbitBelief cob = new CircularOrbitBelief(agentID, m_currentCircularOrbitBelief.getPosition(), m_currentCircularOrbitBelief.getRadius(),m_currentCircularOrbitBelief.getIsClockwise());
        beliefMgr.put(cob);
    }
    
    private void flyRacetrackOrbitBelief(){
        
        /*
        Altitude finalAltitude = m_currentTargetCenter.getAltitude();
                
        if(m_currentTargetCenter == m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition()){
            finalAltitude = this.m_currentCircularOrbitBelief.getFinalAltitude();
        }
        */
        RacetrackOrbitBelief rob = new RacetrackOrbitBelief (m_currentTargetCenter.getLatitude(), m_currentTargetCenter.getLongitude(),
            m_currentCircularOrbitBelief.getFinalAltitude(), m_currentTargetCenter.getAltitude(), m_currentCircularOrbitBelief.getRadius() , m_currentCircularOrbitBelief.getIsClockwise());
        this.beliefMgr.put(rob);
    }
    
    /*
     * Create a new orbit from the next LatLonAltPosition along the orbit path
     * 1) Find if new orbit should be clockwise or counterclockwise
     * 2) Move LatLonAlt so that the edge of the orbit is at the desired LatLonAlt
     * 
     * Should be called once heading is already pointed towards the next orbit desired location
     */
    private void newOrbitPathOrbit(LatLonAltPosition agentPosition) throws Exception {
        LatLonAltPosition nextGoal = null;
        boolean isClockwise = true;
        boolean sameOrbit =false;
        boolean isFinalOrbit = false;
        //LatLonAltPosition oldTargetOrbit = m_currentTargetCenter;
            if(orbitPath != null&&!orbitPath.isEmpty()){
                if(m_currentTargetCenter!=null && m_currentTargetCenter.getRangeTo(orbitPath.get(0)).getDoubleValue(Length.METERS) <pathOrbitRadius.getDoubleValue(Length.METERS)){
                    sameOrbit = true;
                }
                m_currentTargetCenter = orbitPath.get(0);
                if(orbitPath.size()>1){
                    nextGoal = orbitPath.get(1);
                }else{
                    nextGoal = m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
                    if(m_currentTargetCenter.getRangeTo(nextGoal).getDoubleValue(Length.METERS) <pathOrbitRadius.getDoubleValue(Length.METERS)){
                        isFinalOrbit = true;
                    }
                }
            }else{
                throw new Exception("Null Pointer/empty orbit path");
            }
            //double bearing = agentPosition.getBearingTo(m_currentTargetCenter).getDoubleValue(Angle.DEGREES)-heading.getDoubleValue(Angle.DEGREES);
            //double bearing = m_currentTargetCenter.getBearingTo(nextGoal).minus(agentPosition.getBearingTo(m_currentTargetCenter)).getDoubleValue(Angle.DEGREES);
            double bearing = agentPosition.getBearingTo(nextGoal).minus(agentPosition.getBearingTo(m_currentTargetCenter)).getDoubleValue(Angle.DEGREES);
            double radius = pathOrbitRadius.getDoubleValue(Length.METERS);
            double distance = agentPosition.getRangeTo(m_currentTargetCenter).getDoubleValue(Length.METERS);
            Altitude altitude = m_currentTargetCenter.getAltitude();
            
            if((bearing <0 && bearing >-180)|| bearing >180){
                isClockwise = false;
                //Rotate counterclockwise to make tangent to robit intercept at desired lat lon
                double angle = -Math.asin(radius/distance);
                distance = Math.sqrt(Math.pow(distance, 2)-Math.pow(radius, 2));
                RangeBearingHeightOffset offset = new RangeBearingHeightOffset(new Length(distance,Length.METERS),
                        new NavyAngle(angle, Angle.DEGREES).plus(agentPosition.getBearingTo(m_currentTargetCenter).asAngle()),
                        Length.ZERO);
                //m_currentTargetCenter = agentPosition.translatedBy(offset).asLatLonAltPosition();
            }else{
                //Rotate clockwise to make tangent to orbit intercept at desired lat lon
                double angle = Math.asin(radius/distance);
                distance = Math.sqrt(Math.pow(distance, 2)-Math.pow(radius, 2));
                RangeBearingHeightOffset offset = new RangeBearingHeightOffset(new Length(distance,Length.METERS),
                        new NavyAngle(angle, Angle.DEGREES).plus(agentPosition.getBearingTo(m_currentTargetCenter).asAngle()),
                        Length.ZERO);
                //m_currentTargetCenter = agentPosition.translatedBy(offset).asLatLonAltPosition();
           }
           
           m_currentTargetCenter = new LatLonAltPosition(m_currentTargetCenter.getLatitude(), m_currentTargetCenter.getLongitude(), altitude);
            
           //Override clockwise or counter clockwise if the new orbit is only a climb
           if(isFinalOrbit){
               isClockwise = m_currentCircularOrbitBelief.getIsClockwise();
           }
           
           if(sameOrbit){
               isClockwise = ((CircularOrbitBelief)this.beliefMgr.getAllBeliefs().get(CircularOrbitBelief.BELIEF_NAME)).getIsClockwise();
           }
            
                /*
            if(m_currentTargetCenter.getAltitude().equals(Altitude.NEGATIVE_INFINITY)){
                   m_currentTargetCenter = new LatLonAltPosition(oldTargetOrbit.getLatitude(), oldTargetOrbit.getLongitude(), nextGoal.getAltitude());
                   CircularOrbitBelief b = (CircularOrbitBelief)beliefMgr.get(CircularOrbitBelief.BELIEF_NAME);
                   if(b!=null){
                        isClockwise = b.getIsClockwise();
                   }
            }else if(nextGoal!=null){
                double bearing = agentPosition.getBearingTo(m_currentTargetCenter).getDoubleValue(Angle.DEGREES)-heading.getDoubleValue(Angle.DEGREES);
                if(bearing <0 || bearing >180){
                    isClockwise = false;
                }
            }else{
                isClockwise = m_currentCircularOrbitBelief.getIsClockwise();
            }
            */
                
            if(m_currentCircularOrbitBelief.isRacetrack()){
                Altitude finalAltitude = m_currentTargetCenter.getAltitude();
                
                if(m_currentTargetCenter == m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition()){
                    finalAltitude = this.m_currentCircularOrbitBelief.getFinalAltitude();
                }
                RacetrackOrbitBelief rob = new RacetrackOrbitBelief (m_currentTargetCenter.getLatitude(), m_currentTargetCenter.getLongitude(),
                                                                                        finalAltitude, m_currentTargetCenter.getAltitude(), pathOrbitRadius, isClockwise);
                this.beliefMgr.put(rob);
            }else{
                
                //out.write("TCOB: placing orbit: "+m_currentTargetCenter.getAltitude().getDoubleValue(Length.METERS)+ " in feet: "+ m_currentTargetCenter.getAltitude().getDoubleValue(Length.FEET));
                //out.newLine();
                //CircularOrbitBelief cob = new CircularOrbitBelief(agentID, m_currentTargetCenter, m_currentCircularOrbitBelief.getRadius(),isClockwise);
                CircularOrbitBelief cob = new CircularOrbitBelief(agentID, m_currentTargetCenter, pathOrbitRadius,isClockwise);
                beliefMgr.put(cob);
            }
    }

    
    /*
     * Update the altitude of the circular orbit belief
     * If update value is too low, set orbit alt to minimum allowable altitude
     */
    private void updateOrbitAltitude(TestCircularOrbitBelief b) {
        if(orbitPath==null || orbitPath.isEmpty()){
            CircularOrbitBelief circularOrbitBelief = new CircularOrbitBelief(agentID,
                                                                  b.getPosition(),
                                                                  m_orbitRadius,
                                                                  m_currentCircularOrbitBelief.getIsClockwise());
            m_currentTargetCenter = b.getPosition().asLatLonAltPosition();
            beliefMgr.put(circularOrbitBelief);
        }
    }
    
    //Check if within one radius of the waypoint.
    //return false if not at waypoint, or waypoint is the actual orbit.
    private boolean doneWaypoint(LatLonAltPosition currOrbitPosition, LatLonAltPosition uavPosition, NavyAngle heading) {
        //if (currOrbitPosition.equals(this.m_currentCircularOrbitBelief.getPosition())) {
        //   return false;
        //}

        //TODO make this a config. This is how close to the orbit altitude we require to get before moving on.
        int altitudeRange = 10;
        Length distanceToOrbitCenter = uavPosition.getRangeTo(currOrbitPosition);
        if (distanceToOrbitCenter.isLessThan(m_orbitRadius.times(m_ReachedInterceptOrbitFactor))) {
           
          int uavAlt = (int)uavPosition.getAltitude().getDoubleValue(Length.METERS);
          int orbitAlt = (int)currOrbitPosition.getAltitude().floor(Length.METERS).getDoubleValue(Length.METERS);
          
          /*
          if(uavAlt+1<orbitAlt) {
            return false;
          }
         */
          if(Math.abs(uavAlt-orbitAlt) > altitudeRange ){
              return false;
          }
          LatLonAltPosition nextPos;
          if (orbitPath.size() > 1) {
              nextPos = orbitPath.get(1);
          } else {
              nextPos = this.m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
          }
          NavyAngle angleToGoal = uavPosition.getBearingTo(nextPos);
          if (angleToGoal.shortAngleBetween(heading).isLessThan(new Angle(30, Angle.DEGREES))) {  
              return true;
          }
          distanceToOrbitCenter = uavPosition.getRangeTo(nextPos);
          if (distanceToOrbitCenter.isLessThan(m_orbitRadius.times(m_ReachedInterceptOrbitFactor))) {
              return true;
          }
          
          
        }

        return false;
    }
   
   /*
    private boolean finishedFirstOrbit(LatLonAltPosition agentPosition, NavyAngle heading) {
        
        if(orbitPath==null || orbitPath.isEmpty())return true;
        LatLonAltPosition nextPos, currentPos=orbitPath.get(0);
        if (orbitPath.size() > 1) {
            nextPos = orbitPath.get(1);
        } else {
            nextPos = this.m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
            
        }
        
        NavyAngle angleToGoal = currentPos.getBearingTo(nextPos);
        
        
        if(this.orbitPath.get(0).getAltitude().equals(Altitude.NEGATIVE_INFINITY)){
            int alt = (int)agentPosition.getAltitude().getDoubleValue(Length.METERS);
            int orbit = (int)nextPos.getAltitude().floor(Length.METERS).getDoubleValue(Length.METERS);
            out.write("Orbit pathing agent alt: " +alt+" orbit alt: "+ orbit+ "nextPosAlt = "+nextPos.getAltitude().getDoubleValue(Length.FEET));
            if(alt+1<orbit){
                return false;
            }
        }
        
        //TODO maybe not hard code 30?
        if(angleToGoal.shortAngleBetween(heading).isLessThan(new Angle(30,Angle.DEGREES))){
            return true;   
        }
        return false;
    }
    */
    /**
     * 
     * @param agentPosition position to check distance from
     * @return The minimum distance from terrain regardless of direction
     */
    private double minDistanceFromTerrain(LatLonAltPosition agentPosition){
        double latitude = agentPosition.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
        double longitude= agentPosition.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
        double distance = DtedGlobalMap.getDted().getAltitudeMSL(latitude, longitude);
        
        int length = (int) (distance+1);
        double temp;
        
        LatLonAltPosition topLeft = agentPosition.translatedBy(new RangeBearingHeightOffset(new Length(Math.sqrt(Math.pow(distance, 2)*2), Length.METERS), NavyAngle.NORTHEAST,new Length(-distance, Length.METERS))).asLatLonAltPosition();
        LatLonAltPosition tempPos;
        //TODO change this to the resolution of DTED data
        for(int x = (int) (-distance-1); x<length; x+=30){
            tempPos = topLeft.translatedBy(new RangeBearingHeightOffset(new Length(x,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS))).asLatLonAltPosition();
            for(int y=  (int) (-distance-1); y<length; y+=30){
                tempPos = tempPos.translatedBy(new RangeBearingHeightOffset(new Length(y,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS))).asLatLonAltPosition();
                
                latitude = tempPos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
                longitude= tempPos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
                temp = DtedGlobalMap.getDted().getAltitudeMSL(latitude, longitude);
                
                tempPos = tempPos.translatedBy(new RangeBearingHeightOffset(new Length(0, Length.METERS), NavyAngle.ZERO,new Length(temp, Length.METERS))).asLatLonAltPosition();
                
                temp = tempPos.getRangeTo(agentPosition).getDoubleValue(Length.METERS);
                
                distance = (temp<distance) ? temp: distance;
            }
        }
           
        return distance;
    }
    
    
    /**********************************************************************************************************/
    /**********************************************************************************************************/
    //Path finding code
    /**
     * First check if orbit is in a safe location.  Adjust the altitude if necessary
     * 
     * This function is responsible for keeping the orbit in the safety box
     * 
     * @return list of additional orbits to chase on route to this target waypoint
     */
    public TestCircularOrbitBelief adjustOrbitCenterToSafeLocation(LatLonAltPosition agentPosition, SafetyBox safetyBox, TestCircularOrbitBelief belief){
        double minSafeAlt=0;
        double minSafeGridAlt = 0;
        double radius = belief.getRadius().getDoubleValue(Length.METERS);
        int diameter = (int)(2*(belief.getRadius().getDoubleValue(Length.METERS)+_xyBuffer))+1;
        double maxAllowableAlt = agentPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        if(belief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS)<maxAllowableAlt){
            maxAllowableAlt = belief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        }

        minSafeAlt = (getAreaAltitude(belief.getPosition().asLatLonAltPosition(), diameter, safetyBox, belief)+1);
        PathNode goal= createPathNodeFromLatLonAlt(belief.getPosition().asLatLonAltPosition(), null, radius);
        minSafeGridAlt = findGridAltitude(goal.dtedMapX, goal.dtedMapY, safetyBox, radius).max;
        
        if(minSafeAlt == Double.MAX_VALUE|| minSafeGridAlt == Double.MAX_VALUE||minSafeAlt+ _minAGLBuffer> maxAllowableAlt || minSafeGridAlt+_minAGLBuffer>maxAllowableAlt){
              belief = translateToSafePosistion(safetyBox, belief);
        }else{
            if(minSafeAlt<minSafeGridAlt)minSafeAlt = minSafeGridAlt;
            
            belief.setMinimumSafeAltitude(minSafeAlt+ _minAGLBuffer);
        }
        
        return belief;
    }
    
      
    /**
     * Find the minimum safe altitude for orbit centered at position, with radius of this object
     * return inf if location is not in safety box.
     * 
     * X and Y coordinates should be offsets from the center of this orbits DTED map data.
     * @param xCord the index for the x coordinate for local cache of analyzed dted data
     * @param yCord the index for the x coordinate for local cache of analyzed dted data
     * @return grid altitude
     */
    private AltitudePair findGridAltitude(int xCord, int yCord, SafetyBox safetyBox, double orbitRadius){
        double gridMaxAltitude = -1;
        double gridMinAltitude = Double.MAX_VALUE;
        
        if(dtedMap.containsKey(xCord)&&dtedMap.get(xCord).containsKey(yCord)){
            //out.write("X: " + xCord + " Y: " + yCord+" Alt: " + dtedMap.get(xCord).get(yCord));
            AltitudePair p = dtedMap.get(xCord).get(yCord);
            return new AltitudePair(p.max, p.min);
        }
        
        
       
        int diameter = (int)(2*orbitRadius)+(int)this._xyBuffer+1;
        
        RangeBearingHeightOffset offsetFromCenter = new RangeBearingHeightOffset(new Length(Math.sqrt(2*Math.pow(diameter>>1,2)),Length.METERS), NavyAngle.NORTHWEST,new Length(0,Length.METERS));
        RangeBearingHeightOffset horizontalOffset = new RangeBearingHeightOffset(new Length(xCord*diameter,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
        RangeBearingHeightOffset verticleOffset = new RangeBearingHeightOffset(new Length(yCord*diameter,Length.METERS),NavyAngle.NORTH ,new Length(0,Length.METERS));
        
        //LatLonAltPosition center = _dtedMapCenter.translatedBy(verticleOffset).translatedBy(horizontalOffset).asLatLonAltPosition();
        LatLonAltPosition center = dtedCenter.translatedBy(verticleOffset).translatedBy(horizontalOffset).asLatLonAltPosition();
        LatLonAltPosition topLeft = center.translatedBy(offsetFromCenter).asLatLonAltPosition();
        
        horizontalOffset = new RangeBearingHeightOffset(new Length(diameter,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
        verticleOffset = new RangeBearingHeightOffset(new Length(diameter,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS));
        LatLonAltPosition swCorner = topLeft.translatedBy(verticleOffset).asLatLonAltPosition();
        LatLonAltPosition neCorner = topLeft.translatedBy(horizontalOffset).asLatLonAltPosition();
        
        LatLonAltPosition[] verts = new LatLonAltPosition[4];
       
        verts[0] = swCorner;
        verts[3] = new LatLonAltPosition(neCorner.getLatitude(), swCorner.getLongitude(), Altitude.ZERO);
        verts[2] = neCorner;
        verts[1] = new LatLonAltPosition(swCorner.getLatitude(), neCorner.getLongitude(), Altitude.ZERO);
        RectangleRegion r;
        try {
            r = new RectangleRegion(verts);
            if(safetyBox!=null&&!safetyBox.isRectangleOutsideKeepOutRegions(r)){
                
                //Save this area to save time later        
                TreeMap<Integer, AltitudePair> map;
        
                if((map = dtedMap.get(xCord))==null){
                     map = new TreeMap();
                }
                AltitudePair p = new AltitudePair(Double.MAX_VALUE, Double.MAX_VALUE);
                map.put(yCord, p);
                dtedMap.put(xCord, map);
                
                return new AltitudePair(Double.MAX_VALUE, Double.MAX_VALUE);
            }
        } catch (Exception ex) {
            Logger.getLogger(TestCircularOrbitBelief.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Length radius = new Length(orbitRadius+_xyBuffer,Length.METERS);
        if(safetyBox!=null && !safetyBox.isOrbitInsideSafetyBox(center, radius)){
             TreeMap<Integer, AltitudePair> map;
        
                if((map = dtedMap.get(xCord))==null){
                     map = new TreeMap();
                }
                AltitudePair p = new AltitudePair(Double.MAX_VALUE, Double.MAX_VALUE);
                map.put(yCord, p);
                dtedMap.put(xCord, map);
            return new AltitudePair(Double.MAX_VALUE, Double.MAX_VALUE);
        }
       
        
        for(int i = 0; i<diameter; i+=30){
            horizontalOffset = new RangeBearingHeightOffset(new Length(i,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
            LatLonAltPosition column = topLeft.translatedBy(horizontalOffset).asLatLonAltPosition();
            for(int j = 0; j<diameter; j+=30){
                verticleOffset = new RangeBearingHeightOffset(new Length(j,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS));
                LatLonAltPosition cord = column.translatedBy(verticleOffset).asLatLonAltPosition();
                
                double cordAlt = DtedGlobalMap.getDted().getAltitudeMSL(cord.getLatitude().getDoubleValue(Angle.DEGREES), cord.getLongitude().getDoubleValue(Angle.DEGREES));
                if(cordAlt>gridMaxAltitude)gridMaxAltitude = cordAlt;
                //correction for random dted points having a garbage value
                if(cordAlt<gridMinAltitude && cordAlt>-5000)gridMinAltitude = cordAlt;
            }
        }
                
        //Save this area to save time later        
        TreeMap<Integer, AltitudePair> map;
        
        //This is true if there is a cliff in the grid
        if(safetyBox.findMaxAlt(gridMinAltitude)<gridMaxAltitude+_minAGLBuffer){
            gridMaxAltitude = Double.MAX_VALUE;
            gridMinAltitude = Double.MAX_VALUE;
        }
            
        if((map = dtedMap.get(xCord))==null){
            map = new TreeMap();
        }
        AltitudePair p = new AltitudePair(gridMaxAltitude, gridMinAltitude);
        map.put(yCord, p);
        dtedMap.put(xCord, map);
        
        /*try {
            out.write("X: " + xCord + " Y: " + yCord+" Alt: " + p.toString());
            out.newLine();
        } catch (IOException ex) {
            Logger.getLogger(OrbitPathingBehavior.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        return new AltitudePair(p.max, p.min);
    }
    
    private boolean gridIsSafe(int xCord, int yCord, SafetyBox safetyBox, TestCircularOrbitBelief belief) {
        int diameter = (int)(2*belief.getRadius().getDoubleValue(Length.METERS))+(int)this._xyBuffer+1;
        
        RangeBearingHeightOffset offsetFromCenter = new RangeBearingHeightOffset(new Length(Math.sqrt(2*Math.pow(diameter>>1,2)),Length.METERS), NavyAngle.NORTHWEST,new Length(0,Length.METERS));
        RangeBearingHeightOffset horizontalOffset = new RangeBearingHeightOffset(new Length(xCord*diameter,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
        RangeBearingHeightOffset verticleOffset = new RangeBearingHeightOffset(new Length(yCord*diameter,Length.METERS),NavyAngle.NORTH ,new Length(0,Length.METERS));
        
        LatLonAltPosition center = belief.getPosition().translatedBy(verticleOffset).translatedBy(horizontalOffset).asLatLonAltPosition();
        LatLonAltPosition topLeft = center.translatedBy(offsetFromCenter).asLatLonAltPosition();
        
        horizontalOffset = new RangeBearingHeightOffset(new Length(diameter,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
        verticleOffset = new RangeBearingHeightOffset(new Length(diameter,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS));
        LatLonAltPosition swCorner = topLeft.translatedBy(verticleOffset).asLatLonAltPosition();
        LatLonAltPosition neCorner = topLeft.translatedBy(horizontalOffset).asLatLonAltPosition();
        
        LatLonAltPosition[] verts = new LatLonAltPosition[4];
       
        verts[0] = swCorner;
        verts[1] = new LatLonAltPosition(neCorner.getLatitude(), swCorner.getLongitude(), Altitude.ZERO);
        verts[2] = neCorner;
        verts[3] = new LatLonAltPosition(swCorner.getLatitude(), neCorner.getLongitude(), Altitude.ZERO);
        RectangleRegion r;
        try {
            r = new RectangleRegion(verts);
            if(safetyBox!=null&& !safetyBox.isRectangleOutsideKeepOutRegions(r)){
                return false;
            }
        } catch (Exception ex) {
            Logger.getLogger(TestCircularOrbitBelief.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        if(safetyBox!=null && !safetyBox.isOrbitInsideSafetyBox(center, belief.getRadius())){
            return false;
        }
        
        return true;
    }

    /**
     * 
     * @param center - LatLon of center of area to be surveyed
     * @param diameter - diameter of area
     * @param safetyBox - safetyBox
     * @return max ground altitude in this area
     */
    private double getAreaAltitude(LatLonAltPosition center, int diameter, SafetyBox safetyBox, TestCircularOrbitBelief belief){
        double gridAltitude=-1;
       
        RangeBearingHeightOffset offsetFromCenter = new RangeBearingHeightOffset(new Length(Math.sqrt(2*Math.pow(diameter>>1+(int)_xyBuffer,2)),Length.METERS), NavyAngle.NORTHWEST,new Length(0,Length.METERS));
        
        LatLonAltPosition topLeft = center.translatedBy(offsetFromCenter).asLatLonAltPosition();
        
        RangeBearingHeightOffset horizontalOffset = new RangeBearingHeightOffset(new Length(diameter+2*(int)_xyBuffer,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
        RangeBearingHeightOffset verticleOffset = new RangeBearingHeightOffset(new Length(diameter+2*(int)_xyBuffer,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS));
        LatLonAltPosition swCorner = topLeft.translatedBy(verticleOffset).asLatLonAltPosition();
        LatLonAltPosition neCorner = topLeft.translatedBy(horizontalOffset).asLatLonAltPosition();
        
        LatLonAltPosition[] verts = new LatLonAltPosition[4];
       
        verts[0] = swCorner;
        verts[3] = new LatLonAltPosition(neCorner.getLatitude(), swCorner.getLongitude(), Altitude.ZERO);
        verts[2] = neCorner;
        verts[1] = new LatLonAltPosition(swCorner.getLatitude(), neCorner.getLongitude(), Altitude.ZERO);
        
        /*for(int i = 0; i<4; i++){
            try {
                out.write("Verts: " + verts[i]);
                out.newLine();
                out.flush();
            } catch (IOException ex) {
                Logger.getLogger(OrbitPathingBehavior.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        
        RectangleRegion r;
        try {
            r = new RectangleRegion(verts);
           
            if(safetyBox!=null&&!safetyBox.isRectangleOutsideKeepOutRegions(r)){
                return Double.MAX_VALUE;
            }
        } catch (Exception ex) {
            Logger.getLogger(TestCircularOrbitBelief.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Length radius = new Length((belief.getRadius().getDoubleValue(Length.METERS)+_xyBuffer),Length.METERS);
        if(safetyBox!=null && !safetyBox.isOrbitInsideSafetyBox(center, radius)){
            return Double.MAX_VALUE;
        }
       
        
        for(int i = 0; i<diameter; i+=30){
            horizontalOffset = new RangeBearingHeightOffset(new Length(i,Length.METERS),NavyAngle.EAST ,new Length(0,Length.METERS));
            LatLonAltPosition column = topLeft.translatedBy(horizontalOffset).asLatLonAltPosition();
            for(int j = 0; j<diameter; j+=30){
                verticleOffset = new RangeBearingHeightOffset(new Length(j,Length.METERS),NavyAngle.SOUTH ,new Length(0,Length.METERS));
                LatLonAltPosition cord = column.translatedBy(verticleOffset).asLatLonAltPosition();
                
                double cordAlt = DtedGlobalMap.getDted().getAltitudeMSL(cord.getLatitude().getDoubleValue(Angle.DEGREES), cord.getLongitude().getDoubleValue(Angle.DEGREES));
                if(cordAlt>gridAltitude)gridAltitude = cordAlt;
            }
        }
        return gridAltitude;
    }
    
    
    
    public boolean getIsRacetrackSafe(double maxAlt) {
        dtedMap.clear();

        double radius = this.m_currentCircularOrbitBelief.getRadius().getDoubleValue(Length.METERS);
        dtedCenter = m_currentCircularOrbitBelief.getPosition().asLatLonAltPosition();
        m_currentTargetCenter = dtedCenter;
        PathNode goal = this.createPathNodeFromLatLonAlt(m_currentCircularOrbitBelief.getGimbleTarget(), null, radius);
        
        for(int i = -1; i<2; i++){
            for(int j = -1; j<2; j++){
                AltitudePair pair = existsFeasiblePath(new PathNode(goal,null,null,i,j), goal, m_safetyBox, radius);
                if(pair.max<pair.min || pair.max< maxAlt){
                    return false;
                }
            }
        }
        
        double areaAlt = getAreaAltitude(m_currentCircularOrbitBelief.getGimbleTarget(), (int)(2*(radius+_xyBuffer)), m_safetyBox, m_currentCircularOrbitBelief);

        if(areaAlt>maxAlt){
            return false;
        }
        
       return true;

    }
    
        
    /**
     * 
     * @param safetyBox
     * @param p2 last waypoint from loiter behavior
     * @param target
     * @param alt min altitude possible for UAV
     * @param velocity uav velocity
     * @param windSouth wind velocity south
     * @param windWest wind velocity west
     * @return 
     */
    //TODO how far off is starting point to strait line travel to the waypoints
    //TODO UPDATE FOR NEW BEHAVIOR CODE
    public boolean oldGetIsRacetrackSafe(SafetyBox safetyBox, LatLonAltPosition target, double velocity, double windSouth, double windWest, TestCircularOrbitBelief belief){
        double alt=belief.getRacetrackFinalAltitude().getDoubleValue(Length.METERS) -this._minAGLBuffer;
        double minTurnRadius = belief.getMinimumTurnRadiusMeters(velocity,windSouth, windWest)+_xyBuffer;
        double radius = belief.getRadius().getDoubleValue(Length.METERS);
        double terrainAlt = getAreaAltitude(belief.getPosition().asLatLonAltPosition(), ((int)(belief.getRadius().getDoubleValue(Length.METERS) +minTurnRadius))<<1, safetyBox, belief);
        if(terrainAlt>alt)return false;
        
        terrainAlt = getAreaAltitude(belief.getPosition().asLatLonAltPosition(), ((int)(belief.getRadius().getDoubleValue(Length.METERS) +minTurnRadius))<<1, safetyBox, belief);
        if(terrainAlt>alt)return false;
                
        //Calcualte the waypoint on the way to the target (plus ninety for right, minus for left)
        NavyAngle angle = belief.getPosition().getBearingTo(target);
        
        if(Config.getConfig().getPropertyAsBoolean("m_ApproachTargetFromLeft", true)){
            angle = angle.minus(Angle.RIGHT_ANGLE);
        }else{
            angle = angle.plus(Angle.RIGHT_ANGLE);
        }
        
        RangeBearingHeightOffset offset = new RangeBearingHeightOffset(new Length(minTurnRadius,Length.METERS),angle,new Length(0,Length.METERS));
        LatLonAltPosition waypoint = target.translatedBy(offset).asLatLonAltPosition();
        
        angle = belief.getPosition().getBearingTo(waypoint);
                       
        offset = new RangeBearingHeightOffset(new Length(minTurnRadius +belief.getRadius().getDoubleValue(Length.METERS),Length.METERS),angle.plus(Angle.RIGHT_ANGLE),new Length(0,Length.METERS));
        LatLonAltPosition tempPos = belief.getPosition().translatedBy(offset).asLatLonAltPosition();
        PathNode p1 = this.createPathNodeFromLatLonAlt(tempPos,null, belief.getRadius().getDoubleValue(Length.METERS));
        
        offset = new RangeBearingHeightOffset(new Length(minTurnRadius +belief.getRadius().getDoubleValue(Length.METERS),Length.METERS),angle.minus(Angle.RIGHT_ANGLE),new Length(0,Length.METERS));
        tempPos = belief.getPosition().translatedBy(offset).asLatLonAltPosition();
        PathNode p2 = this.createPathNodeFromLatLonAlt(tempPos, null, belief.getRadius().getDoubleValue(Length.METERS));
        PathNode waypointNode = this.createPathNodeFromLatLonAlt(waypoint, null, belief.getRadius().getDoubleValue(Length.METERS));
            
        AltitudePair pair1 = existsFeasiblePath(p1, waypointNode, safetyBox, radius);
        AltitudePair pair2 = existsFeasiblePath(p2, waypointNode, safetyBox, radius);
        
        if(pair1!=null&&pair2!=null&&alt<pair1.max && alt<pair2.max && alt>pair1.min+_minAGLBuffer&&alt>pair2.min+_minAGLBuffer){
                return true;
        }else{
            return false;
        }
        
    }
    
    /**
     * Side effect is the move the center position to a safe latLongAlt position;
     * Choose position closest to original orbit
     * Record the offset of center position in relation to DTEDMapCenter
     * 
     * @param acceptableChange
     * @return the change made in altitude
     */
    private TestCircularOrbitBelief translateToSafePosistion(SafetyBox safetyBox, TestCircularOrbitBelief belief) {
       //double altitudeChange = Double.MAX_VALUE;
        int diameter = (int)(2*(belief.getRadius().getDoubleValue(Length.METERS)+_xyBuffer))+1;
        double radius = belief.getRadius().getDoubleValue(Length.METERS);
        //double newMinSafeAlt;
        //double wacsAlt = wacsPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        //RangeBearingHeightOffset horizontalOffsetFromCenter, verticleOffsetFromCenter;
        NavyAngle translationDirection = new NavyAngle(Angle.ZERO);
        RangeBearingHeightOffset offset;
       double minAlt = Double.MAX_VALUE;
       
        //Translate until you find a safe orbit
        //translationDirection = belief.getPosition().getBearingTo(wacsPosition).minus(Angle.RIGHT_ANGLE);
        double piOver6 = Math.PI/6;
        double i = 0;
        LatLonAltPosition temp;
        PathNode tempNode;
        LatLonAltPosition newCenter = belief.getPosition().asLatLonAltPosition();
        int factor = 1;
        while(minAlt==Double.MAX_VALUE){
            while(i<=Math.PI){
                offset = new RangeBearingHeightOffset(new Length(factor*belief.getRadius().getDoubleValue(Length.METERS),Length.METERS), translationDirection.plus(new Angle(i,Angle.RADIANS)),new Length(0,Length.METERS));
                temp = belief.getPosition().translatedBy(offset).asLatLonAltPosition();
            
                tempNode = createPathNodeFromLatLonAlt(temp, null, radius);
                
                double newAlt = getAreaAltitude(temp, diameter, safetyBox,belief);
                double gridAlt = findGridAltitude(tempNode.dtedMapX, tempNode.dtedMapY, safetyBox, radius).max;
                
                if(gridAlt>newAlt)newAlt = gridAlt;
                
                if(newAlt<minAlt){
                    minAlt = newAlt;
                    newCenter = temp;
                }
                i+=piOver6;
            }
            i=0;
            factor++;
        }
     
        double newAlt = belief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        if(newAlt< minAlt){
            newAlt = minAlt+_minAGLBuffer;
        }else{
            newAlt +=_minAGLBuffer;
        }
        
        belief.setPosition(new LatLonAltPosition(newCenter.getLatitude(), newCenter.getLongitude(), new Altitude(newAlt,Length.METERS)));
        belief.setMinimumSafeAltitude(minAlt+_minAGLBuffer);
        return belief;
    }
    

    /**Depth first search through grid until reaching the the destination
     * Must set DTED map center
     * 
     * After finding the path, do smoothing algorithm from "Small aircraft theory and Practice" pg. 215
     * 
     * @param agentPosition current position of the agent
     * @return list of way-points to reach the orbit this object represents
     * null means there is no path,  an empty path means it is safe to go directly to destination orbit
     */
    private ArrayList<LatLonAltPosition> getSafePathToOrbit(LatLonAltPosition agentPosition, SafetyBox safetyBox, double heading, TestCircularOrbitBelief belief) throws IOException {
        ArrayList<LatLonAltPosition> path = new ArrayList<LatLonAltPosition>();

        double radius = belief.getRadius().getDoubleValue(Length.METERS);
        //out.write("Plan Path Agent Alt: " + agentPosition.getAltitude().getDoubleValue(Length.METERS));
        //out.newLine();            
        PathNode end = createPathNodeFromLatLonAlt(belief.getPosition().asLatLonAltPosition(), null, belief.getRadius().getDoubleValue(Length.METERS));
        
        //out.write("End grid alt is: "+findGridAltitude(end.dtedMapX, end.dtedMapY, safetyBox, belief.getRadius().getDoubleValue(Length.METERS)));
        //out.newLine(); 
        PathNode start = createPathNodeFromLatLonAlt(dtedCenter, end, belief.getRadius().getDoubleValue(Length.METERS));
        //out.write("Start grid alt is: "+findGridAltitude(start.dtedMapX, start.dtedMapY, safetyBox, belief.getRadius().getDoubleValue(Length.METERS)));
        //out.newLine(); 
        //out.flush();
        double agentAlt = agentPosition.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        double goalAlt = belief.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
        //double maxAlt = goalAlt;
        double minAlt = goalAlt;
        //if (maxAlt < agentAlt) {
        //    maxAlt = agentAlt;
       //}
        
        if(minAlt>agentAlt){
            minAlt = agentAlt;
        }
        
        
        //maxAlt-= _minAGLBuffer;
        
        ArrayList<PathNode> pathNodePath = getAStarPathToGoal(start, end, minAlt, safetyBox, true, radius);

        if (pathNodePath != null) {
            path = smoothPath(pathNodePath, agentAlt, safetyBox, radius);

            /*
            LatLonAltPosition clockwiseOrbit = agentPosition.translatedBy(new RangeBearingHeightOffset(belief.getRadius(), new NavyAngle(heading + 90, Angle.DEGREES), new Length(0, Length.METERS))).asLatLonAltPosition();
            LatLonAltPosition counterClockwiseOrbit = agentPosition.translatedBy(new RangeBearingHeightOffset(belief.getRadius(), new NavyAngle(heading + 270, Angle.DEGREES), new Length(0, Length.METERS))).asLatLonAltPosition();
            
            
            if(path !=null && path.size()>0){
                double firstOrbitAlt = path.get(0).getAltitude().getDoubleValue(Length.METERS);
                if(firstOrbitAlt<maxAlt)maxAlt = firstOrbitAlt;
            }
            
            if (!orbitIsSafe(clockwiseOrbit, agentPosition, start.dtedMapX, start.dtedMapY, maxAlt, safetyBox, belief)) {
                path.add(0, counterClockwiseOrbit);
                return path;
            }
            if (!orbitIsSafe(counterClockwiseOrbit, agentPosition, start.dtedMapX, start.dtedMapY, maxAlt, safetyBox, belief)) {
                path.add(0,clockwiseOrbit);
                return path;
            }
            */


        } else {
            
            //ArrayList<LatLonAltPosition> path = new ArrayList<LatLonAltPosition>();
            pathNodePath = getAStarPathToGoal(start, end, Double.MAX_VALUE-1, safetyBox, false, radius);
            //out.write("TestCircular use up and over for "+end.position.toString());
            //out.newLine();
            //out.write("A star path is: "+ pathNodePath);
            //out.newLine();
            //out.flush();
            path = smoothPath(pathNodePath, agentAlt, safetyBox, radius);
            
            //out.write("Smooth Up and over path");
            //out.newLine();
            //for(LatLonAltPosition p: path){
            //    out.write(p.getAltitude().getDoubleValue(Length.METERS)+" "+p);
            //    out.newLine();
            //}
            //out.flush();
            
            
        }


        return path;
    }

    /*
     * Temp fix for a grid having both a keep out and the center of the orbit
     *  ______
     * |     B|
     * |O    B|
     * |_____B|
     */
    /*
    private boolean adjacentToGoal(PathNode node, PathNode goal){
        if(node.dtedMapX == goal.dtedMapX-1 || node.dtedMapX == goal.dtedMapX+1){
            if(node.dtedMapY==goal.dtedMapY)return true;
        }else if(node.dtedMapY == goal.dtedMapY-1 || node.dtedMapY == goal.dtedMapY+1){
            if(node.dtedMapX==goal.dtedMapX)return true;
        }
        return false;
    }
    */
    /*Use a star algorithm to find a safe path through the grid to the goal
     * At some point the a star path is more expensive than up and over, return null to do up and over action
     */
    private ArrayList<PathNode> getAStarPathToGoal(PathNode start, PathNode goal, double maxAltitude, SafetyBox safetyBox, boolean useDTED, double radius) throws IOException {
        TreeMap<PathNode,PathNode> openSet = new TreeMap<PathNode,PathNode>();
        TreeSet<PathNode> closedSet = new TreeSet<PathNode>();
        
        
        int maxClosedSetSize = (int)Math.pow(Math.max(Math.abs(start.dtedMapX-goal.dtedMapX), Math.abs(start.dtedMapY-goal.dtedMapY)),2);
        if(maxClosedSetSize<9)maxClosedSetSize = 9;
        if(useDTED == false)maxClosedSetSize = Integer.MAX_VALUE;
        //PriorityQueue<PathNode> pq = new PriorityQueue<PathNode>();
        boolean foundGoal = false;

        openSet.put(start,start);
        //pq.add(start);
        //out.write("Goal: " + goal.toString() + " Start: " + start.toString());
        //out.newLine();
        while (openSet.size() > 0) {
            //PathNode a = pq.poll();
            //PathNode a = openSet.first();
            PathNode a = openSet.pollFirstEntry().getValue();
            
            //openSet.remove(a);
            

            if (a.dtedMapX==goal.dtedMapX && a.dtedMapY==goal.dtedMapY) {
                //goal now has a parent
                //out.write("Found goal");
                //out.newLine();
                //goal.parent = a;
                goal = a;
                foundGoal = true;
                break;
            } else {
                //out.write("Closing " + a.toString());
                closedSet.add(a);
               
                //return null if it is better to go up and over
                if(closedSet.size()>=maxClosedSetSize)return null;
               
                ArrayList<PathNode> neighbors = getValidNeighbors(goal, a, maxAltitude, safetyBox, useDTED, radius);
                for (PathNode n : neighbors) {

                    if (!closedSet.contains(n)) {
                        double g = a.score + 1;

                        if (!openSet.containsKey(n)) {
                            n.score = g;
                            openSet.put(n,n);
                            //out.write("Adding neighbor to open set: " + n.toString());
                            //pq.add(n);
                        } else if (g < openSet.get(n).score) {
                            // put better node path into the open set
                            n.score = g;
                            n.parent = a;
                            //out.write("Updating open set score and parent for: " + n.toString());
                            //openSet.remove(n);
                            //openSet.add(n);
                            ///pq.remove(n);
                            //pq.add(n);
                            openSet.put(n, n);
                        }
                    } else {
                        //out.write("Neighbor in closed set: " + n.toString());
                    }
                }
            }
        }

        ArrayList<PathNode> path = new ArrayList<PathNode>();
        if (foundGoal) {
            Stack<PathNode> stack = new Stack<PathNode>();
            stack.push(goal);
            PathNode parent = goal.parent;

            while (parent != null) {
                stack.push(parent);
                parent = parent.parent;
            }

            int size = stack.size();
            for (int i = 0; i < size; i++) {
                path.add(stack.pop());
            }
            return path;
        } else {
            return null;
        }

        

    }

    /**
     * 
     * @param aStarPathToGoal parameter is the list returned from aStar function
     * @param  altLimit is the alt the agent should be at once it has reached the orbit
     * @param  goalAlt is the altitude that the goal orbit is at
     * @return return a more direct safe route to this circular orbit
     */
    private ArrayList<LatLonAltPosition> smoothPath(ArrayList<PathNode> aStarPathToGoal, double agentAlt, SafetyBox safetyBox, double radius) {
        ArrayList<LatLonAltPosition> path = new ArrayList<LatLonAltPosition>();
        //ArrayList<AltitudePair> pathAltitudes = new ArrayList<AltitudePair>();
       // AltitudePair currentAltitudeLimit;
        double altLimit = agentAlt;
        
        
        if(aStarPathToGoal.size()<2){
            aStarPathToGoal.clear();
            return path;
        }
        
        PathNode i = aStarPathToGoal.remove(0);
        PathNode j = i;
        PathNode k = aStarPathToGoal.remove(0);
        
        AltitudePair p = findGridAltitude(j.dtedMapX, j.dtedMapY, safetyBox, radius);
        //currentAltitudeLimit = new AltitudePair(p.min, p.max);
        AltitudePair pathPair= new AltitudePair(safetyBox.findMaxAlt(p.min), p.min+_minAGLBuffer);
        boolean done = false;
        while(!done){
           
           //P gives a minimum ceiling under max, and a maximum floor under min
           p=existsFeasiblePath(i,k, safetyBox, radius);
           if(p==null || p.min+_minAGLBuffer>p.max || p.min+_minAGLBuffer>altLimit || p.max<altLimit){
               if(p == null){
                    //altLimit = findGridAltitude(k.dtedMapX,k.dtedMapY,safetyBox, radius).max+_minAGLBuffer;
               }
               
               if(altLimit <p.min+_minAGLBuffer){
                   p = existsFeasiblePath(j,k, safetyBox, radius);
                   
                   altLimit = p.min+_minAGLBuffer;
               }
               else if(altLimit>p.max){
                   p = existsFeasiblePath(j,k, safetyBox, radius);
                   
                   altLimit = p.max;
               }
               //}
               path.add(new LatLonAltPosition(j.position.getLatitude(), j.position.getLongitude(), new Altitude(altLimit, Length.METERS)));
               
               //pathAltitudes.add(currentAltitudeLimit);
                //altLimit = pathPair.max;
                i=j;
                //pathPair.min = findGridAltitude(i.dtedMapX, i.dtedMapY,safetyBox,radius).max;
                //pathPair.max = safetyBox.findMaxAlt(pathPair.min);
            }else{
               //currentAltitudeLimit = p;
                pathPair.max = p.max;
                pathPair.min = p.min;
                j = k;
                if(aStarPathToGoal.isEmpty()){
                    done = true;
                }else{
                    k = aStarPathToGoal.remove(0);
                }
            }
        }
        
        
        return path;
    }
    
    /*
     * Check the neighbors around the path node.  
     */
    private ArrayList<PathNode> getValidNeighbors(PathNode goal, PathNode a, double maxAltitude, SafetyBox safetyBox, boolean useDTED, double radius) {
        ArrayList<PathNode> neighbors = new ArrayList<PathNode>();
        double gridDiameter = 2*(radius+_xyBuffer);
        AltitudePair aGridAltitude = findGridAltitude(a.dtedMapX, a.dtedMapY, safetyBox, radius);
        double gridMax = safetyBox.findMaxAlt(aGridAltitude.max);
        double gridMin = aGridAltitude.min;
        
        RangeBearingHeightOffset horizontalOffset;
        RangeBearingHeightOffset verticleOffset;
        
        LatLonAltPosition temp; 
        for(int i = a.dtedMapX-1; i<=a.dtedMapX+1; i++){
            for(int j = a.dtedMapY-1; j<=a.dtedMapY+1; j++){
                if(i==a.dtedMapX|| j == a.dtedMapY){
                    horizontalOffset = new RangeBearingHeightOffset(new Length(gridDiameter*i,Length.METERS), NavyAngle.EAST,new Length(0,Length.METERS));
                    verticleOffset = new RangeBearingHeightOffset(new Length(gridDiameter*j,Length.METERS), NavyAngle.NORTH,new Length(0,Length.METERS));
                    temp = dtedCenter.translatedBy(horizontalOffset).asLatLonAltPosition();
                    temp = temp.translatedBy(verticleOffset).asLatLonAltPosition();
                    AltitudePair p= findGridAltitude(i, j, safetyBox, radius);
                    if(useDTED){                    
                        if(p.max<maxAltitude&&safetyBox.findMaxAlt(p.min)>p.max){
                            if((safetyBox.findMaxAlt(p.max)>=gridMax && p.min+_minAGLBuffer<gridMax)||(gridMax>safetyBox.findMaxAlt(p.max)&&gridMin+_minAGLBuffer<safetyBox.findMaxAlt(p.max))){
                                neighbors.add(new PathNode(goal,a,temp,i,j));
                            }
                        }
                    }else{
                        if(p.max!=Double.MAX_VALUE){
                            //Make sure there is overlap between current valid altitudes and the neighbor.
                            if((safetyBox.findMaxAlt(p.max)>=gridMax && p.min+_minAGLBuffer<gridMax)||(gridMax>safetyBox.findMaxAlt(p.max)&&gridMin+_minAGLBuffer<safetyBox.findMaxAlt(p.max))){
                                neighbors.add(new PathNode(goal,a,temp,i,j));
                            }
                        }
                    }
                }
            }
        }
        
        //Remove self which will be included because we dont get neighbors from invalid nodes,
        
        neighbors.remove(a);
        
        return neighbors;
    }
    /**
     * Coverage algorithm from http://lifc.univ-fcomte.fr/~dedu/projects/bresenham/index.html
     * @param i one endpoint of "line segment"
     * @param j other endpoint of "line segment"
     * @return p gives a minimum ceiling under max, and a maximum floor under min.  
     * Return null if the plane cannot fly to this grid with given altLimit or floor/ceiling cross
     * 
     */
    private AltitudePair existsFeasiblePath(PathNode endpoint1, PathNode endpoint2, SafetyBox safetyBox, double radius) {
        
        int i;               // loop counter 
        int ystep, xstep;    // the step on y and x axis 
        int error;           // the error accumulated during the increment 
        int errorprev;       // *vision the previous value of the error variable 
        int y = endpoint1.dtedMapY, x = endpoint1.dtedMapX;  // the line points 
        int x1 = endpoint1.dtedMapX, x2=endpoint2.dtedMapX, y1=endpoint1.dtedMapY,y2=endpoint2.dtedMapY;
        int ddy, ddx;        // compulsory variables: the double values of dy and dx 
        int dx = x2 - x1;
        int dy = y2 - y1;
        
        AltitudePair pathAlt = findGridAltitude(x, y, safetyBox, radius);
        AltitudePair p = new AltitudePair(pathAlt.max, pathAlt.min);
        if(p.max>pathAlt.min)pathAlt.min=p.max;
        p.max = safetyBox.findMaxAlt(p.min);
        pathAlt.max = p.max;
        
        
        // NB the last point can't be here, because of its previous point (which has to be verified) 
        if (dy < 0) {
            ystep = -1;
            dy = -dy;
        } else {
            ystep = 1;
        }
        if (dx < 0) {
            xstep = -1;
            dx = -dx;
        } else {
            xstep = 1;
        }
        ddy = 2 * dy;  // work with double values for full precision 
        ddx = 2 * dx;
        if (ddx >= ddy) {  // first octant (0 <= slope <= 1) 
            // compulsory initialization (even for errorprev, needed when dx==dy) 
            errorprev = error = dx;  // start in the middle of the square 
            for (i = 0; i < dx; i++) {  // do not use the first point (already done) 
                x += xstep;
                error += ddy;
                if (error > ddx) {  // increment y if AFTER the middle ( > ) 
                    y += ystep;
                    error -= ddx;
                    // three cases (octant == right->right-top for directions below): 
                    if (error + errorprev < ddx) // bottom square also 
                    {
                        p=findGridAltitude(x, y-ystep, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    } else if (error + errorprev > ddx) // left square also 
                    {
                        p=findGridAltitude(x-xstep,y, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    } else {  // corner: bottom and left squares also 
                        p=findGridAltitude(x,y-ystep, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        p=findGridAltitude(x-xstep,y, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    }
                }
                p=findGridAltitude(x,y, safetyBox, radius);
                if(p.max>pathAlt.min)pathAlt.min=p.max;
                //if(p.max>altLimit)return null;
                p.max = safetyBox.findMaxAlt(p.min);
                if(p.max<pathAlt.max)pathAlt.max=p.max;
                //if(pathAlt.min>=p.max)return null;
                
                errorprev = error;
            }
        } else {  // the same as above 
            errorprev = error = dy;
            for (i = 0; i < dy; i++) {
                y += ystep;
                error += ddx;
                if (error > ddy) {
                    x += xstep;
                    error -= ddy;
                    if (error + errorprev < ddy) {
                        p=findGridAltitude(x-xstep,y, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    } else if (error + errorprev > ddy) {
                        p=findGridAltitude(x,y-ystep, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    } else {
                        p=findGridAltitude(x-xstep, y, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        p=findGridAltitude(x,y-ystep, safetyBox, radius);
                        if(p.max>pathAlt.min)pathAlt.min=p.max;
                        //if(p.max>altLimit)return null;
                        p.max = safetyBox.findMaxAlt(p.min);
                        if(p.max<pathAlt.max)pathAlt.max=p.max;
                        //if(pathAlt.min>=p.max)return null;
                        
                    }
                }
                p=findGridAltitude(x,y, safetyBox, radius);
                if(p.max>pathAlt.min)pathAlt.min=p.max;
                //if(p.max>altLimit)return null;
                p.max = safetyBox.findMaxAlt(p.min);
                if(p.max<pathAlt.max)pathAlt.max=p.max;
                //if(pathAlt.min>=p.max)return null;
                
                errorprev = error;
            }
        }
        
        
        return pathAlt;
    }

    //TODO should a calculation be done to get position
    private PathNode createPathNodeFromLatLonAlt(LatLonAltPosition position, PathNode goal,double radius) {
        float x,y;
        int i, j;
        RangeBearingHeightOffset offset = dtedCenter.getRangeBearingHeightOffsetTo(position);
        double r = offset.getRange().getDoubleValue(Length.METERS);
        radius = radius*2+this._xyBuffer;
        double angle = Math.PI/2.0-offset.getBearing().getDoubleValue(Angle.DEGREES)/360.0*Math.PI*2;
        
        x = (float)(r*Math.cos(angle)/radius);
        y = (float)(r*Math.sin(angle)/radius);
        i = Math.round(x);
        j = Math.round(y);
        //out.write("Position is "+agentPosition.toString()+" grid position: "+x+" "+y);
        return new PathNode(goal, null, position, i, j);
    }

    private boolean orbitIsSafe(LatLonAltPosition orbit, LatLonAltPosition agentPosition, short i, short j, double minSafeAltitude,SafetyBox safetyBox, TestCircularOrbitBelief belief) {
        short iTranslation, jTranslation;
        double radius = belief.getRadius().getDoubleValue(Length.METERS);
        double heading = agentPosition.getBearingTo(orbit).getDoubleValue(Angle.DEGREES);
        
        if(heading<180){
            iTranslation = (short)(i+1);
        }else{
            iTranslation = (short)(i-1);
        }
        if(heading<90 || heading >270){
            jTranslation = (short)(j+1);
        }else{
            jTranslation = (short)(j-1);
        }
        
        if(this.findGridAltitude(i, jTranslation,safetyBox, radius).max>minSafeAltitude){
            return false;
        }
        if(this.findGridAltitude(iTranslation, jTranslation,safetyBox, radius).max>minSafeAltitude){
            return false;
        }
        if(this.findGridAltitude(iTranslation, j,safetyBox, radius).max>minSafeAltitude){
            return false;
        }
        
        
        return true;
    }



    
    public class AltitudePair{
        double max, min;
        public AltitudePair(double max, double min){
            this.max = max;
            this.min = min;
        }
        
        @Override
        public String toString(){
            return "Min: "+ min +" Max: "+max;
        }
    }
    
    /**
     * Nodes for temporary low res DTED grid
     */
    public class PathNode implements Comparable<PathNode>{
        PathNode goal;
        PathNode parent;
        ArrayList<PathNode> children;
        LatLonAltPosition position;
        short dtedMapX, dtedMapY;
        double score = Double.MAX_VALUE;
        
        public PathNode(PathNode goal,PathNode parent, LatLonAltPosition pos, int x, int y){
            this.goal = goal;
            this.parent = parent;
            position = pos;
            dtedMapX = (short)x;
            dtedMapY = (short)y;
            children = new ArrayList<PathNode>();
            
        }
        
        @Override
        //Return one if this objects value is greater than the last
        public int compareTo(PathNode p) {
            if(this.dtedMapX == p.dtedMapX && this.dtedMapY == p.dtedMapY){
                return 0;
            }else if(this.getDistance(goal) <p.getDistance(goal)){
                return -1;
            }
            return 1;
        }
 
        @Override
        public boolean equals(Object o){
            if(!(o instanceof PathNode))return false;
            PathNode n =(PathNode)o;
             if(n.dtedMapX ==dtedMapX && n.dtedMapY==dtedMapY){
                return true;
            }
            return false;   
        }

        /*
         * Map NxN -> n mapping shorts to integer for hash function
         */
        @Override
        public int hashCode() {
           int x = this.dtedMapX;
           int y = this.dtedMapY;
           return (x<<16 | y&0xFFFF);
        }

        
        private double getDistance(PathNode a) {
            return Math.sqrt(Math.pow(this.dtedMapX-a.dtedMapX, 2)+Math.pow(this.dtedMapY-a.dtedMapY, 2));
        }
        
        @Override
        public String toString(){
            String s = "";
            s += ""+this.dtedMapX+" "+ this.dtedMapY;
            return s;
        }
        
    }
   
}
