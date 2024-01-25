package edu.jhuapl.nstd.swarm.behavior;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.Time;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudPredictionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.ManualInterceptActualOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.ManualInterceptCommandedOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeActualBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TestCircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.swarm.util.SafetyBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JOptionPane;

public class ParticleCloudPredictionBehavior extends Behavior
{
    public static final String MODENAME = "particle-cloud";

    private boolean shouldTrackParticle(TRACKING_TYPE trackingType, PARTICLE_SOURCE source) 
    {
        if (trackingType == TRACKING_TYPE.MIXTURE)
            return true;
        if (source == PARTICLE_SOURCE.UNKNOWN)
            return true;
        
        
        if (trackingType == TRACKING_TYPE.PARTICLE && source == PARTICLE_SOURCE.PARTICLE)
            return true;
        if (trackingType == TRACKING_TYPE.CHEMICAL && source == PARTICLE_SOURCE.CHEMICAL)
            return true;
        return false;
    }

    public static enum PARTICLE_SOURCE {UNKNOWN, PARTICLE, CHEMICAL, BIOLOGICAL};
    public static enum TRACKING_TYPE {MIXTURE, PARTICLE, CHEMICAL};
    
    public static int idxItr = 0;
    private class Particle
    {
        public LatLonAltPosition position;
        public long lastUpdateTime_ms;
        public long creationTime_ms;
        public double weight;
        public PARTICLE_SOURCE source;
        public ParticleArrayList agedParticleListCell;
        public int idx;

        public Particle(final LatLonAltPosition position,
                        final long time_ms,
                        final int weight,
                        final PARTICLE_SOURCE source,
                        final ParticleArrayList agedParticleListCell)
        {
            this.position = position;
            lastUpdateTime_ms = time_ms;
            creationTime_ms = time_ms;
            this.weight = weight;
            this.source = source;
            this.agedParticleListCell = agedParticleListCell;

            idx = idxItr;
            idxItr ++;
        }
    }

    private class ParticleLinkedList extends LinkedList<Particle>
    {
        public double totalWeightOfContainedParticles = 0;
    }

    private class ParticleArrayList extends ArrayList<Particle>
    {
        public ParticleArrayList()
        {
            super();
        }

        public ParticleArrayList(int initialSize)
        {
            super(initialSize);
        }
    }

    protected static final RangeBearingHeightOffset RANGE_ZERO = new RangeBearingHeightOffset(Length.ZERO, NavyAngle.ZERO, Length.ZERO);
    private static final int m_numOrbitsPerCycle = 4;
    private static final Angle[] m_orbitVarianceOffsets = {new Angle(-90, Angle.DEGREES),
                                                           new Angle(90, Angle.DEGREES),
                                                           new Angle(0, Angle.DEGREES),
                                                           new Angle(180, Angle.DEGREES),
                                                           new Angle(-45, Angle.DEGREES),
                                                           new Angle(-135, Angle.DEGREES),
                                                           new Angle(45, Angle.DEGREES),
                                                           new Angle(135, Angle.DEGREES)};

    private int m_orbitVarianceIndex;
    private long m_lastUpdateTime_ms;
    private long m_lastUpdateDuration_ms;
    private Date m_LastManualOrbitTimestamp;
    private Date m_LastWaypointBeliefTime;
    private Date m_LastNewOrbitPositionTime;
    private boolean m_AltitudeOverride;
    private boolean m_ForceCalcNewOrbit;
    private String m_prevSwarmMode;
    private int m_updatePeriod_ms;
    private Latitude m_orbitCenterLatitude = null;
    private Longitude m_orbitCenterLongitude = null;
    private Altitude m_orbitCenterAltitudeMSL = null;
    private NavyAngle m_orbitStartAngle = null;
    private NavyAngle m_prevAngleToTarget = null;
    private int m_orbitState;
    private boolean m_justCompletedOrbit = false;
    private boolean m_sendAltitudeUpdate = false;
    private long m_lastDetectionTime_ms;
    private LinkedList<ParticleArrayList> m_ageSortedParticleList;
    private Particle m_ReservedUnknownParticle = null;
    private int m_numUnusedParticles;
    private CloudPredictionBelief m_cloudPredictionBelief = null;
    private Length m_orbitRadius;
    private Speed m_windSpeed = Speed.ZERO;
    private NavyAngle m_windBearing = NavyAngle.ZERO;
    private Angle m_cellLatitudeDelta;
    private Angle m_cellLongitudeDelta;
    private float m_detectionScoreSlope;
    private float m_detectionScoreStartingPoint;
    private LatLonAltPosition m_myCurrentPosition = null;
    private Altitude m_minAltitudeAGL;
    private boolean m_reachedNewOrbit = false;
    private Speed m_planeInterceptSpeed;
    private double m_windSpeedPercentGaussianSigma;
    private double m_windDirectionRadiansGaussianSigma;
    private double m_plumeRiseRatePercentGaussianSigma;
    private int m_TimeAfterExplosionToStopRiseRateMs;
    private int m_numParticles;
    private Random m_randomNumGenerator = new Random();
    private double m_timeAccelerationCoefficient;
    private boolean m_orbitClockwise = true;
    private int m_interceptDelay_sec;
    private long m_explosionTime_ms = 0;
    private LatLonAltPosition m_currUAVPosition = null;
    private NavyAngle m_currUAVHeading = null;
    private LatLonAltPosition m_predictedPlumeCenterPositionMSL = null;
    private LatLonAltPosition m_predictedPlumeInterceptPositionMSL = null;
    private int m_numParticlesPerHit;
    private double m_PlumeHeightCoeffMax;
    private double m_PlumeHeightCoeffMin;
    private double m_PlumeHeightCoeffAvg;
    private double m_PlumeTopHeightTimeExponentAvg;
    private double m_PlumeTopHeightTimeExponentMax;
    private double m_PlumeTopHeightTimeExponentMin;
    private double m_PlumeHeightCalcDelaySec;
    private double m_ExplosionTNTEquivLbs;
    private double m_TimeToIntercept;
    private double m_AltitudeVariationPerStepMeters;
    private long m_PrevAltUpdateTimeMs;
    private int m_AltUpdateIntervalMs;
    private double m_MaxExpectedPlumeAltitudeMeters;
    private double m_MinExpectedPlumeAltitudeMeters;
    private double m_ExpectedPlumeThicknessFeet;
    private double m_DefaultAltitudeVariationMeters;
    private LatLonAltPosition m_orbitCenterPosition;
    private double m_UpperAltAGLMeters;
    private double m_LowerAltAGLMeters;
    private double m_CommandedAltAGLMeters;
    private double m_AltUpdatePeriodMs;
    private double m_particleDecayHalfLife_ms = 0;
    private Particle m_heaviestTotalParticle;
    private Particle m_heaviestCurrParticle;
    private Particle m_heaviestChemParticle;
    private Particle m_heaviestCountsParticle;
    private boolean m_GotChemDetections;
    private boolean m_GotCountsDetections;
    private boolean m_GotCurrDetections;
    private boolean m_GotCurrDetectionsBeforeCurrent;
    private Particle m_youngestParticle;
    private double m_orbitSearchDisplacementPerOrbitsCycle_m;
    private int m_numSearchOrbitsBetweenNormalOrbits_m;
    private int m_numOrbitsSinceDetection;
    private Length m_minTurnRadius;
    private LatLonAltPosition m_turnArcExitPoint;
    private Length m_turnArcLength;
    private long m_lastOrbitCompletionTime_ms;
    private double m_windScaleFactor;
    private Length m_minTurnExitDistanceFromEdgeToReverseDirection;
    private SafetyBox m_safetyBox;
    private int m_ParticleCountScalar;
    private double m_ParticleCountPrediction_a;
    private double m_ParticleCountPrediction_b;
    double m_ParticleCountPercentageOfPredicted;
    double m_ParticleCountPredictionThresholdPercentage;
    private double m_MaxPredictedChemBars;
    private int m_PredictedChemBarsDecayRate_Sec;
    double m_MaxParticleHitHalfLifeMultiplier;
    double m_MinimumAltitudeTrackingSpanMeters;
    double m_MaximumAltitudeTrackingSpanMeters;
    double m_AltitudeTrackingSpanDuringTrackingPercentage;
    double m_AltitudeTrackingSpanDuringSearchingPercentage;
    double m_PercentageOfAltitudeSpanAboveCenter;
    double m_PercentageOfAltitudeSpanBelowCenter;
    double m_MinAltThresholdPercentage;
    double m_RestrictedMaxAltThresholdPercentage;
    long m_TimeOfLastGateChangeMs;
    Length m_TrackingAltitudeBufferForNextGate;
    boolean m_ForceNextGate;
    boolean m_FirstTimeRaisingAltitude;
    TRACKING_TYPE m_TrackingType = TRACKING_TYPE.MIXTURE;
    
    double m_ParticleDeleteHorizDistanceMetersSquared;
    double m_ParticleDeleteAltDistanceMeters;
    int m_ParticleDeleteDelaySeconds;
    int m_ageSortedParticleListIndexToDeleteFrom = 0;
    
    private LinkedList<Particle> m_DisplayPredictionRandomParticles = new LinkedList<Particle>();
    boolean m_ResampleRandomDetections = true;



    private boolean m_PorpoiseAltitudeAlgorithm;
    private boolean m_GateRampAltitudeAlgorithm;

    private double m_ReachedInterceptOrbitFactor;
    private double m_DistanceToReverseInOrbits;

    static private class AltitudeGateParameters
    {
        public AltitudeGateParameters(double startAltPercentage, double endAltPercentage, boolean increaseAltitude)
        {
            this.startAltPercentage = startAltPercentage;
            this.endAltPercentage = endAltPercentage;
            this.increaseAltitude = increaseAltitude;
        }

        public double startAltPercentage;
        public double endAltPercentage;
        public boolean increaseAltitude;
    }
    private static final AltitudeGateParameters[] m_AltitudeGateSequence = {
                                                                            new AltitudeGateParameters(0.00, 0.33, true),
                                                                            new AltitudeGateParameters(0.33, 0.67, true),
                                                                            new AltitudeGateParameters(0.67, 1.00, true),
                                                                            new AltitudeGateParameters(1.00, 0.67, false),
                                                                            new AltitudeGateParameters(0.67, 0.33, false),
                                                                            new AltitudeGateParameters(0.33, 0.00, false)
                                                                            };
    private static final AltitudeGateParameters[] m_AltitudeGateSequenceTracking = {
                                                                            new AltitudeGateParameters(0, 1, true),
                                                                            new AltitudeGateParameters(1, 0, false),
                                                                            };

    private int m_AltitudeGateIndex;
    private int m_NumGateSteps;



    public ParticleCloudPredictionBehavior(BeliefManager beliefManager, String agentID)
    {
        super(beliefManager, agentID);
        
        m_safetyBox = new SafetyBox(beliefManager);

        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate") && !Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation"))
        {
            m_timeAccelerationCoefficient = Config.getConfig().getPropertyAsDouble("SimulationMode.timeAccelerationCoefficient", 1);
        }
        else
        {
            m_timeAccelerationCoefficient = 1;
        }

        m_GotCurrDetections = false;
        m_GotCurrDetectionsBeforeCurrent = false;
        m_GotChemDetections = false;
        m_GotCountsDetections = false;
        
        m_ReachedInterceptOrbitFactor = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.reachedInterceptOrbitFactor", 1.8);
        m_DistanceToReverseInOrbits = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.distanceToReverseInOrbits", 1.25);
        m_PlumeHeightCoeffMax = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCoeffMax", 9.8804);
        m_PlumeHeightCoeffMin = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCoeffMin", 4.8007);
        m_PlumeHeightCoeffAvg = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCoeffAvg", 6.9462);
        m_PlumeHeightCalcDelaySec = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeHeightCalcDelaySec", 15);
        m_ExplosionTNTEquivLbs = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.explosionTNTEquivLbs", 108);
        m_PlumeTopHeightTimeExponentAvg = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PlumeTopHeightTimeExponentAvg", .5);
        m_PlumeTopHeightTimeExponentMax = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PlumeTopHeightTimeExponentMax", .5);
        m_PlumeTopHeightTimeExponentMin = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PlumeTopHeightTimeExponentMin", .5);
        m_DefaultAltitudeVariationMeters = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PorpoiseDefaultAltVaration.Meters", 15);
        m_AltUpdateIntervalMs = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.PorpoiseAltUpdateIntervalMs", 15000);
        m_AltUpdatePeriodMs = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.PorpoiseAltUpdatePeriodMs", 45000);
        m_updatePeriod_ms = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.updatePeriod_ms");
        //m_orbitRadius = new Length(Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptRad_m",305.0), Length.METERS);
        m_orbitRadius = new Length(500.0, Length.METERS);
        m_detectionScoreStartingPoint = 200;
        m_detectionScoreSlope = (float) Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.detectionScoreSlope");
        //m_minAltitudeAGL = new Altitude(Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptMinAltAGL_ft",350.0), Length.FEET);
        m_minAltitudeAGL = new Altitude(900.0, Length.FEET);
        m_planeInterceptSpeed = new Speed(Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.planeInterceptSpeed_knots"), Speed.KNOTS);
        m_windSpeedPercentGaussianSigma = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.windSpeedPercentGaussianSigma");
        m_windDirectionRadiansGaussianSigma = Math.toRadians(Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.windDirectionDegreesGaussianSigma"));
        m_plumeRiseRatePercentGaussianSigma = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.plumeRiseRatePercentGaussianSigma");
        m_TimeAfterExplosionToStopRiseRateMs = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.timeAfterExplosionToStopRiseRate.Sec", 300)*1000;
        m_numParticles = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.numParticles",10000);
        m_interceptDelay_sec = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.InterceptDelay_sec");
        m_numParticlesPerHit = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.numParticlesPerHit", 10);
        m_particleDecayHalfLife_ms = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.particleDecayHalfLife_sec") * 1000;
        m_orbitSearchDisplacementPerOrbitsCycle_m = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.orbitSearchDisplacementPerOrbitsCycle_m");
        m_numSearchOrbitsBetweenNormalOrbits_m = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.numSearchOrbitsBetweenNormalOrbits");
        double maxSpeed_mps = (new Speed(Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.maxSpeed_knots"), Speed.KNOTS)).getDoubleValue(Speed.METERS_PER_SECOND);
        m_minTurnRadius = new Length((maxSpeed_mps * maxSpeed_mps) / (9.81 * Math.tan(Math.toRadians(Config.getConfig().getPropertyAsDouble("FlightControl.maxBankAngle_deg")))), Length.METERS);
        m_windScaleFactor = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.windScaleFactor");
        m_minTurnExitDistanceFromEdgeToReverseDirection = m_minTurnRadius.times(m_DistanceToReverseInOrbits).times(2.0);

        m_PorpoiseAltitudeAlgorithm = Config.getConfig().getPropertyAsBoolean("ParticleCloudPredictionBehavior.PorpoiseAltitudeAlgorithm", false);
        m_GateRampAltitudeAlgorithm = Config.getConfig().getPropertyAsBoolean("ParticleCloudPredictionBehavior.GateRampAltitudeAlgorithm", true);
        if (m_GateRampAltitudeAlgorithm)
            m_PorpoiseAltitudeAlgorithm = false;
        m_NumGateSteps = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.NumGateSteps", 10);
        m_ParticleCountPrediction_a = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.ParticleCountPrediction.a", 1098933);
        m_ParticleCountPrediction_b = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.ParticleCountPrediction.b", -0.848033690935294);
        m_ParticleCountPercentageOfPredicted = 0;
        m_MaxParticleHitHalfLifeMultiplier = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.MaxParticleHitHalfLifeMultiplier", 4);
        m_MaxPredictedChemBars = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.ChemBarPrediction.MaxBars", 4);
        m_PredictedChemBarsDecayRate_Sec = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.ChemBarPrediction.DecayRate.Sec", 30);

        m_ParticleCountPredictionThresholdPercentage = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.ParticleCountPredictionThresholdPercentage", 60)/100;
        m_MinimumAltitudeTrackingSpanMeters = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.MinimumAltitudeTrackingSpanMeters", 60);
        m_MaximumAltitudeTrackingSpanMeters = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.MaximumAltitudeTrackingSpanMeters", 250);
        m_AltitudeTrackingSpanDuringTrackingPercentage = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.AltitudeTrackingSpanDuringTrackingPercentage", 100)/100;
        m_AltitudeTrackingSpanDuringSearchingPercentage = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.AltitudeTrackingSpanDuringSearchingPercentage", 100)/100;
        m_PercentageOfAltitudeSpanAboveCenter = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PercentageOfAltitudeSpanAboveCenter", 45)/100;
        m_PercentageOfAltitudeSpanBelowCenter = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PercentageOfAltitudeSpanBelowCenter", 35)/100;
        m_MinAltThresholdPercentage = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.MinAltThresholdPercentage", 25)/100;
        m_RestrictedMaxAltThresholdPercentage = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.RestrictedMaxAltThresholdPercentage", 0)/100;
        m_TrackingAltitudeBufferForNextGate = new Length (Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.TrackingAltitudeBufferForNextGateMeters", 5), Length.METERS);
        
        double particleDeleteHorizDistanceMeters = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PartDeleteHorizDistanceMeters", 40);
        m_ParticleDeleteHorizDistanceMetersSquared = particleDeleteHorizDistanceMeters*particleDeleteHorizDistanceMeters;
        m_ParticleDeleteAltDistanceMeters = Config.getConfig().getPropertyAsDouble("ParticleCloudPredictionBehavior.PartDeleteAltDistanceMeters", 10);
        m_ParticleDeleteDelaySeconds = Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.PartDeleteDelaySeconds", 5);
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

    /*static BufferedWriter writer;
    static {
        try
        {
            writer = new BufferedWriter(new FileWriter("log" + System.currentTimeMillis() + ".csv"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

    @Override
    public void update()
    {
        try
        {
            //Find commanded manual intercept location for WACSagent, and update actual setting if necessary
            ManualInterceptCommandedOrbitBelief commandedManualIntercept = (ManualInterceptCommandedOrbitBelief)beliefMgr.get(ManualInterceptCommandedOrbitBelief.BELIEF_NAME);
            ManualInterceptActualOrbitBelief actualManualIntercept = (ManualInterceptActualOrbitBelief)beliefMgr.get(ManualInterceptActualOrbitBelief.BELIEF_NAME);
            if (commandedManualIntercept != null && (actualManualIntercept == null || commandedManualIntercept.isNewerThan(actualManualIntercept)))
            {
                if (!commandedManualIntercept.getReleaseImmediately())
                    actualManualIntercept = new ManualInterceptActualOrbitBelief (WACSAgent.AGENTNAME, commandedManualIntercept.getInterceptLatitude(), commandedManualIntercept.getInterceptLongitude(), commandedManualIntercept.getForceHoldPosition());
                else
                    actualManualIntercept = new ManualInterceptActualOrbitBelief (WACSAgent.AGENTNAME, commandedManualIntercept.getReleaseImmediately());
                    
                beliefMgr.put(actualManualIntercept);
                m_ForceCalcNewOrbit = true;
            }
            
            
            //
            // Since all behaviors get called all the time, we need to check if we are in the correct mode
            // for this behavior.  Normally this is not necessary since most behaviors output a vector
            // that can be scaled, but this behavior
            //
            AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefMgr.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(agentID).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
            {
                if ((System.currentTimeMillis() - m_lastUpdateTime_ms) > m_updatePeriod_ms)
                {
                    if (m_prevSwarmMode != null && m_prevSwarmMode.equals(LoiterBehavior.MODENAME))
                    {
                        //
                        // If some beliefs are missing, indicating the system isn't full up and running, go back into
                        // loiter to indicate a problem.  For example, if there is no gimbal target, we don't know
                        // where to intercept.
                        //
                        boolean targetFullyDefined = false;
                        
                        //Is intercept allowed?
                        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)beliefMgr.get(AllowInterceptActualBelief.BELIEF_NAME);
                        if (allowInterceptBlf != null && allowInterceptBlf.getAllow())
                        {
                            //Is target defined?
                            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                            TargetActualBelief targets = (TargetActualBelief) beliefMgr.get(TargetActualBelief.BELIEF_NAME);
                            if (targets != null)
                            {
                                PositionTimeName gimbalPosition = targets.getPositionTimeName(gimbalTargetName);

                                if (gimbalPosition != null)
                                {
                                    AgentPositionBelief agentPositionBelief = (AgentPositionBelief)beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
                                    if (agentPositionBelief != null)
                                    {
                                        PositionTimeName planePosition = agentPositionBelief.getPositionTimeName(agentID);
                                        if (planePosition != null)
                                        {
                                            WACSWaypointActualBelief waypointBelief = (WACSWaypointActualBelief) beliefMgr.get(WACSWaypointActualBelief.BELIEF_NAME);
                                            if (waypointBelief != null)
                                            {
                                                //
                                                // There is a waypoint belief published, so update loiter altitude and radius settings
                                                //
                                                m_minAltitudeAGL = waypointBelief.getIntersectAltitude();
                                                m_orbitRadius = waypointBelief.getIntersectRadius();
                                            }

                                            targetFullyDefined = true;
                                        }
                                    }
                                }
                            }
                        }
                        
                        
                        if (!targetFullyDefined)
                        {
                            AgentModeActualBelief loiterModeBelief = new AgentModeActualBelief(agentID, new Mode(LoiterBehavior.MODENAME));
                            beliefMgr.put(loiterModeBelief);
                            return;
                        }
                    }

                    if (m_prevSwarmMode == null || m_prevSwarmMode.equals(LoiterBehavior.MODENAME))
                    {
                        if (m_explosionTime_ms == 0)
                        {
                            ExplosionBelief explosionBelief = (ExplosionBelief)beliefMgr.get(ExplosionBelief.BELIEF_NAME);
                            if (explosionBelief != null)
                            {
                                m_explosionTime_ms = explosionBelief.getTime_ms();
                            }
                            else
                            {
                                m_explosionTime_ms = System.currentTimeMillis();
                            }
                        }

                        //
                        // Wait a configurable number of seconds before diving in to intercept plume
                        // to make sure the explosion is gone first.
                        //
                        if (((System.currentTimeMillis() - m_explosionTime_ms) * m_timeAccelerationCoefficient) > (m_interceptDelay_sec * 1000))
                        {
                            doInitialSetup();
                        }
                        else
                        {
                            return;
                        }
                    }
                    else
                    {
                        WACSWaypointActualBelief waypointBelief = (WACSWaypointActualBelief) beliefMgr.get(WACSWaypointActualBelief.BELIEF_NAME);
                        if (waypointBelief != null)
                        {
                            //
                            // There is a waypoint belief published, so update intercept altitude and radius settings
                            //
                            m_minAltitudeAGL = waypointBelief.getIntersectAltitude();
                            m_orbitRadius = waypointBelief.getIntersectRadius();
                            
                            if (m_LastWaypointBeliefTime == null || waypointBelief.getTimeStamp().after(m_LastWaypointBeliefTime))
                            {
                                m_LastWaypointBeliefTime = waypointBelief.getTimeStamp();
                                //If we're manually controlling the altitude and the altitude settings MAY have changed, then update the orbit
                                if (m_AltitudeOverride)
                                    m_ForceCalcNewOrbit = true;
                            }
                        }
                        
                        ParticleCloudTrackingTypeActualBelief trackingTypeBelief = (ParticleCloudTrackingTypeActualBelief) beliefMgr.get(ParticleCloudTrackingTypeActualBelief.BELIEF_NAME);
                        if (trackingTypeBelief != null)
                        {
                            setTrackingType(trackingTypeBelief.getTrackingType());
                        }

                        if (m_lastUpdateDuration_ms > 0)
                        {
                            //
                            // Find any plume sensor detections and mark
                            // the corresponding matrix cells
                            //
                            updateCellDetections();


                            //
                            // Move the cells downwind and expand outwards
                            //
                            long starttime = System.currentTimeMillis();
                            propagateAndAgeParticles();
                            long endtime = System.currentTimeMillis();
                            System.out.println("PROPOGATE PARTICLES took " + (endtime-starttime) + " milliseconds");


                            double percentageAroundOrbit = calcCurrOrbitAngle();
                            updatePlumeVariables();
                            if (m_justCompletedOrbit || m_ForceCalcNewOrbit)
                            {
                                ++m_numOrbitsSinceDetection;
                                
                                calcNewOrbit();
                                //updatePlumeVariables();

                                m_PrevAltUpdateTimeMs = System.currentTimeMillis();

                                if (m_numOrbitsSinceDetection >= m_numOrbitsPerCycle)
                                {
                                    m_ParticleCountPercentageOfPredicted = 0;
                                }
                            }
                            else if (m_AltitudeOverride)
                            {
                                //if using manual override, then don't update altitude
                                /*m_orbitCenterPosition = new LatLonAltPosition(m_orbitCenterPosition.getLatitude(), 
                                                                                m_orbitCenterPosition.getLongitude(),
                                                                                new Altitude (getMinAllowableMslMeters (m_orbitCenterPosition), Length.METERS)
                                                                                );
                                m_orbitCenterAltitudeMSL = m_orbitCenterPosition.getAltitude();
                                setOrbitAltitude();*/
                            }
                            else if (m_GateRampAltitudeAlgorithm)
                            {
                                updateOrbitAltitude(false || m_ForceNextGate, percentageAroundOrbit);
                                setOrbitAltitude ();
                            }
                            else if(m_PorpoiseAltitudeAlgorithm && (m_reachedNewOrbit || m_numOrbitsSinceDetection >= m_numOrbitsPerCycle) && ((System.currentTimeMillis() - m_PrevAltUpdateTimeMs)* m_timeAccelerationCoefficient) > m_AltUpdateIntervalMs)
                            {
                                updateOrbitAltitude();
                                m_PrevAltUpdateTimeMs = System.currentTimeMillis();
                            }


                        }
                    }

                    long currTime_ms = System.currentTimeMillis();
                    if (m_lastUpdateTime_ms != 0)
                    {
                        m_lastUpdateDuration_ms = (long)(currTime_ms - m_lastUpdateTime_ms);
                    }
                    m_lastUpdateTime_ms = currTime_ms;
                }
            }
            else
            {
                m_explosionTime_ms = 0;
            }

            if (agentModeBelief != null && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null)
            {
                m_prevSwarmMode = agentModeBelief.getMode(agentID).getName();
            }
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }


    private void updateSearchedCell(final LatLonAltPosition position, final float plumeDetectionScore, final PARTICLE_SOURCE detectionSource)
    {
        {
            {

                //
                // If we detected the plume in this cell, create a number of new particles
                // based on the strength of the detection. But first, find an equal
                // number of particles to destroy so that we maintain a constant
                // number of total particles.
                //
                if (plumeDetectionScore >= 0)
                {
                    m_numOrbitsSinceDetection = 0;
                    m_ResampleRandomDetections = true;

                    double totalAddedlParticleWeight = (int)(m_detectionScoreStartingPoint + (m_detectionScoreSlope * plumeDetectionScore));

                    ParticleArrayList newParticleList = new ParticleArrayList(m_numParticlesPerHit);

                    //
                    // If we have destroyed more particles in the past
                    // than we created, we can use some of those particles
                    // rather than destroying more.
                    //
                    int numParticlesToDestroy = m_numParticlesPerHit;
                    if (m_numUnusedParticles >= numParticlesToDestroy)
                    {
                        m_numUnusedParticles -= numParticlesToDestroy;
                        numParticlesToDestroy = 0;
                    }
                    else if (m_numUnusedParticles > 0)
                    {
                        numParticlesToDestroy -= m_numUnusedParticles;
                        m_numUnusedParticles  = 0;
                    }

                    //
                    // Destroy the oldest particles first.
                    //
                    for (int i = 0; (i < numParticlesToDestroy) && (m_ageSortedParticleList.size() > 0); ++i)
                    {
                        try
                        {
                            Particle particleToDelete = null;
                            int particleNumToDelete = 0;
                            //Avoid the reserved unknown particle, so that it is never deleted
                            while (particleToDelete == null)
                            {
                                particleNumToDelete = (int)(Math.round(Math.random() * (m_ageSortedParticleList.get(m_ageSortedParticleListIndexToDeleteFrom).size() - 1)));
                                particleToDelete = m_ageSortedParticleList.get(m_ageSortedParticleListIndexToDeleteFrom).get(particleNumToDelete);
                                
                                if (particleToDelete == m_ReservedUnknownParticle)
                                    particleToDelete = particleToDelete;
                                
                                if (particleToDelete == m_ReservedUnknownParticle)
                                {
                                    particleToDelete = null;
                                    if (m_ageSortedParticleList.get(m_ageSortedParticleListIndexToDeleteFrom).size() == 1)
                                        m_ageSortedParticleListIndexToDeleteFrom ++;
                                }
                                    
                            }
                            m_ageSortedParticleList.get(m_ageSortedParticleListIndexToDeleteFrom).remove(particleNumToDelete);
                            if (m_ageSortedParticleList.get(m_ageSortedParticleListIndexToDeleteFrom).isEmpty() && m_ageSortedParticleList.size() > m_ageSortedParticleListIndexToDeleteFrom)
                            {
                                m_ageSortedParticleList.remove(m_ageSortedParticleListIndexToDeleteFrom);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    //
                    // Add new particles to this cell since we got a hit here.
                    // Weight those particles more than the original particles.
                    // This allows us to weight the spots where we got hits without
                    // creating a huge number of particles and weighing down
                    // the system.
                    //
                    for (int j = 0; j < m_numParticlesPerHit; ++j)
                    {
                        Particle particle = new Particle(position,
                                                         m_lastUpdateTime_ms,
                                                         (int)(totalAddedlParticleWeight / m_numParticlesPerHit),
                                                         detectionSource,
                                                         newParticleList);
                        newParticleList.add(particle);
                        m_youngestParticle = particle;
                    }
                    
                    m_ageSortedParticleList.addLast(newParticleList);
                }
                //
                // Otherwise, we are in a cell with no detections.
                // Delete all particles in that cell.
                //
                else
                {
                    LinkedList<Particle> particleToDelete = new LinkedList<Particle> ();
                    for (ParticleArrayList particleList : m_ageSortedParticleList)
                    {
                        for (Particle particle : particleList)
                        {
                            double latDiffMeters = (particle.position.getLatitude().getDoubleValue(Angle.DEGREES) - position.getLatitude().getDoubleValue(Angle.DEGREES))*MathUtils.getLatDegreesToMeters();
                            double lonDiffMeters = (particle.position.getLongitude().getDoubleValue(Angle.DEGREES) - position.getLongitude().getDoubleValue(Angle.DEGREES))*MathUtils.getLonDegreesToMeters(particle.position.getLatitude().getDoubleValue(Angle.DEGREES));
                            double altDiffMeters = (particle.position.getAltitude().getDoubleValue(Length.METERS) - position.getAltitude().getDoubleValue(Length.METERS));
                            double totalHorizDiffMetersSquared = latDiffMeters*latDiffMeters + lonDiffMeters*lonDiffMeters;
                            
                            if (totalHorizDiffMetersSquared < m_ParticleDeleteHorizDistanceMetersSquared && altDiffMeters < m_ParticleDeleteAltDistanceMeters)
                            {
                                //This particle is close to the area we just searched that we decided was not a positive detection.  Check creation 
                                //time of the particle to be sure it isn't recently created
                                if (m_lastUpdateTime_ms - particle.creationTime_ms > m_ParticleDeleteDelaySeconds)
                                {
                                    //This particle was created sufficiently long ago that we can safely delete it. (so long as its not the reserved particle)
                                    if (particle != m_ReservedUnknownParticle)
                                    {   
                                        m_numUnusedParticles ++;
                                        particleToDelete.add(particle);
                                        m_ResampleRandomDetections = true;
                                    }
                                }
                            }
                        }
                    }
                    for (Particle particle : particleToDelete)
                    {
                        if (particle.agedParticleListCell != null)
                        {
                            particle.agedParticleListCell.remove(particle);
                        }

                        if (particle.agedParticleListCell.isEmpty() && m_ageSortedParticleList.size() > 1)
                        {
                            m_ageSortedParticleList.remove(particle.agedParticleListCell);
                        }
                    }
                }
            }
        }
    }

    private int getPredictedParticleCount (double timeSinceExplosionSec)
    {
        return (int)(m_ParticleCountPrediction_a*Math.pow(timeSinceExplosionSec, m_ParticleCountPrediction_b));
    }
    
    private double getPredictedChemBars (double timeSinceExplosionSec)
    {
        return Math.max(1, ((m_MaxPredictedChemBars-(timeSinceExplosionSec/m_PredictedChemBarsDecayRate_Sec))));
    }

    private void updateCellDetections()
    {
        CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief) beliefMgr.get(CloudDetectionBelief.BELIEF_NAME);
        if (cloudDetectionBelief != null)
        {
            synchronized(cloudDetectionBelief.getLock())
            {
                for (CloudDetection cloudDetection : cloudDetectionBelief.getDetections())
                {
                    if (cloudDetection.getTime() > m_lastDetectionTime_ms)// && cloudDetection.getValue() >= 1 && cloudDetection.getSource() != 0)
                    {
                        /*PARTICLE_SOURCE particleSource = (cloudDetection.getSource()==CloudDetection.SOURCE_PARTICLE?PARTICLE_SOURCE.PARTICLE:PARTICLE_SOURCE.CHEMICAL);
                        TRACKING_TYPE trackingType = m_TrackingType;
                        if (trackingType != TRACKING_TYPE.MIXTURE && ((particleSource == PARTICLE_SOURCE.PARTICLE && trackingType != TRACKING_TYPE.PARTICLE) || (particleSource == PARTICLE_SOURCE.CHEMICAL && trackingType != TRACKING_TYPE.CHEMICAL)))
                        {
                            //We are not tracking this source, so don't add a detection
                            m_lastDetectionTime_ms = cloudDetection.getTime();
                        }
                        else*/
                        {
                            updateSearchedCell(cloudDetection.getDetection(), cloudDetection.getScaledValue(), (cloudDetection.getSource()==CloudDetection.SOURCE_PARTICLE?PARTICLE_SOURCE.PARTICLE:PARTICLE_SOURCE.CHEMICAL));
                            m_lastDetectionTime_ms = cloudDetection.getTime();

                            if (cloudDetection.getSource() == CloudDetection.SOURCE_PARTICLE)
                            {
                                //particle detection
                                int particleCount = (int)(cloudDetection.getRawValue());
                                double predictedParticleCount = Math.min(25000,getPredictedParticleCount (getTimeSinceExplosionSec()));

                                double percentRate = Math.min(1,particleCount/predictedParticleCount);
                                if (percentRate > m_ParticleCountPercentageOfPredicted)
                                {
                                    m_ParticleCountPercentageOfPredicted = percentRate;
                                }
                            }
                            else if (cloudDetection.getSource() == CloudDetection.SOURCE_CHEMICAL)
                            {
                                //chemical detection
                                int chemBars = (int)(cloudDetection.getRawValue());
                                double predictedChemBars = getPredictedChemBars (getTimeSinceExplosionSec());

                                double percentRate = Math.min(1,chemBars/predictedChemBars);
                                if (percentRate > m_ParticleCountPercentageOfPredicted)
                                {
                                    m_ParticleCountPercentageOfPredicted = percentRate;
                                }
                            }
                        }
                    }
                }
            }
        }

        AgentPositionBelief agentPositionBelief = (AgentPositionBelief) beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
        if (agentPositionBelief != null)
        {
            PositionTimeName positionTimeName = agentPositionBelief.getPositionTimeName(WACSAgent.AGENTNAME);
            if (positionTimeName != null)
            {
                m_myCurrentPosition = positionTimeName.getPosition().asLatLonAltPosition();

                updateSearchedCell(m_myCurrentPosition, -1, PARTICLE_SOURCE.UNKNOWN);
            }
        }
    }
   
    private double getAvgTopPlumeHeightMeters(double time_sec)
    {
        return Math.pow(m_ExplosionTNTEquivLbs, 0.25) * m_PlumeHeightCoeffAvg * Math.pow(time_sec, m_PlumeTopHeightTimeExponentAvg);
    }

    private double getMaxTopPlumeHeightMeters(double time_sec)
    {
        return Math.pow(m_ExplosionTNTEquivLbs, 0.25) * m_PlumeHeightCoeffMax * Math.pow(time_sec, m_PlumeTopHeightTimeExponentMax);
    }
        
    private double getMinTopPlumeHeightMeters(double time_sec)
    {
        return Math.pow(m_ExplosionTNTEquivLbs, 0.25) * m_PlumeHeightCoeffMin * Math.pow(time_sec, m_PlumeTopHeightTimeExponentMin);
    }


    private double getTimeSinceExplosionSec()
    {
        if (m_explosionTime_ms <= 0 )
        {
            ExplosionBelief explosionBelief = (ExplosionBelief)beliefMgr.get(ExplosionBelief.BELIEF_NAME);
            if (explosionBelief != null)
            {
                m_explosionTime_ms = explosionBelief.getTime_ms();
            }
            else
               return  0.0;
        }
        if(m_explosionTime_ms > 0)
        {
            return (System.currentTimeMillis() - m_explosionTime_ms) / 1000.0 * m_timeAccelerationCoefficient;
        }
        return 0.0;
    }



    private double getRiseRate_ftps()
    {

        double timeSinceExplosion_sec = getTimeSinceExplosionSec();
        if(timeSinceExplosion_sec<=0.0)
            return 0.0;

                if(timeSinceExplosion_sec < m_PlumeHeightCalcDelaySec)
                    return 0.0;
                else if (m_explosionTime_ms > 0 && (timeSinceExplosion_sec*1000 > m_TimeAfterExplosionToStopRiseRateMs))
                    return 0.0;
                else
                    return 3.2808* Math.pow(m_ExplosionTNTEquivLbs,0.25)*m_PlumeHeightCoeffAvg*m_PlumeTopHeightTimeExponentAvg* Math.pow(timeSinceExplosion_sec, m_PlumeTopHeightTimeExponentAvg-1);

    }
    
    void setTrackingType (TRACKING_TYPE newTrackingType)
    {
        if (newTrackingType != m_TrackingType)
        {
            m_TrackingType = newTrackingType;
            m_ForceCalcNewOrbit = true;
            m_ResampleRandomDetections = true;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void propagateAndAgeParticles()
    {
        try
        {
            Time updateDuration = new Time(m_lastUpdateDuration_ms, Time.MILLISECONDS);
            getLatestWindVelocity();

            final double updateDuration_sec = updateDuration.getDoubleValue(Time.SECONDS);
            final double meanWindSpeed_mps = m_windSpeed.getDoubleValue(Speed.METERS_PER_SECOND);
            final double windSpeedSigma_mps =  m_windSpeedPercentGaussianSigma / 100 * meanWindSpeed_mps;
            final double meanWindBearing_rad = m_windBearing.getDoubleValue(Angle.RADIANS);
            final double windBearingSigma_rad = m_windDirectionRadiansGaussianSigma;
    //      double meanRiseRate_ftps = m_plumeRiseRate.getDoubleValue(Speed.FEET_PER_SECOND);
            final double meanRiseRate_ftps = getRiseRate_ftps();
            final double riseRateSigma_ftps = m_plumeRiseRatePercentGaussianSigma / 100 * meanRiseRate_ftps;

            //
            // Propogate the cell scores using gaussian model
            //
            LinkedList<Particle> particlesToDelete = null;
            Particle heaviestTotalParticle = null;
            Particle heaviestChemParticle = null;
            Particle heaviestCountsParticle = null;
            Particle heaviestUnknownParticle = null;
            {
                {
                    ListIterator<ParticleArrayList> listItr = m_ageSortedParticleList.listIterator();
                    while (listItr.hasNext())
                    {
                        ParticleArrayList list = listItr.next();
                        ListIterator<Particle> iterator = list.listIterator();
                        while (iterator.hasNext())
                        {
                            Particle particle = iterator.next();

                            // Since particles may move between cells, we mark them with
                            // a last update time to make sure that they only get moved
                            // once per cycle.
                            if (particle.lastUpdateTime_ms < m_lastUpdateTime_ms)
                            {
                                //
                                // Age decay the particle
                                //
                                if (particle.creationTime_ms <= m_lastOrbitCompletionTime_ms)
                                {
                                    double particleStartingWeight = particle.weight;
                                    particle.weight = particle.weight * Math.pow(0.5, (m_lastUpdateDuration_ms * m_timeAccelerationCoefficient) / m_particleDecayHalfLife_ms);

                                    if (particleStartingWeight > 1)
                                    {
                                        if (particle.source == PARTICLE_SOURCE.PARTICLE)
                                            m_GotCountsDetections = true;
                                        else if (particle.source == PARTICLE_SOURCE.CHEMICAL)
                                            m_GotChemDetections = true;
                                    }
                                }
                                else
                                {
                                    if (particle.source == PARTICLE_SOURCE.PARTICLE)
                                        m_GotCountsDetections = true;
                                    else if (particle.source == PARTICLE_SOURCE.CHEMICAL)
                                        m_GotChemDetections = true;
                                }


                                //
                                // Propagate the particle
                                //
                                double horizontalDisplacement_m = ((m_randomNumGenerator.nextGaussian() * windSpeedSigma_mps) + meanWindSpeed_mps) * updateDuration_sec * m_timeAccelerationCoefficient;
                                double bearing_rad = (m_randomNumGenerator.nextGaussian() * windBearingSigma_rad) + meanWindBearing_rad;
                                double verticalDisplacement_ft = ((m_randomNumGenerator.nextGaussian() * riseRateSigma_ftps) + meanRiseRate_ftps) * updateDuration_sec * m_timeAccelerationCoefficient;
                                
                                particle.position = particle.position.translatedBy(new RangeBearingHeightOffset(new Length(horizontalDisplacement_m, Length.METERS),
                                                                                                                new NavyAngle(bearing_rad, Angle.RADIANS),
                                                                                                                new Length(verticalDisplacement_ft, Length.FEET))).asLatLonAltPosition();
                                particle.lastUpdateTime_ms = m_lastUpdateTime_ms;


                                //
                                // Keep track of which particle is the heaviest for later use.
                                // Use total score of cell the particle is in to break ties.
                                //
                                if ((heaviestTotalParticle == null) || (particle.weight > heaviestTotalParticle.weight))
                                {
                                    heaviestTotalParticle = particle;
                                }
                                if (particle.source == PARTICLE_SOURCE.UNKNOWN && ((heaviestUnknownParticle == null) || (particle.weight > heaviestUnknownParticle.weight)))
                                {
                                    heaviestUnknownParticle = particle;
                                }
                                if (particle.source == PARTICLE_SOURCE.CHEMICAL && ((heaviestChemParticle == null) || (particle.weight > heaviestChemParticle.weight)))
                                {
                                    heaviestChemParticle = particle;
                                }
                                if (particle.source == PARTICLE_SOURCE.PARTICLE && ((heaviestCountsParticle == null) || (particle.weight > heaviestCountsParticle.weight)))
                                {
                                    heaviestCountsParticle = particle;
                                }
                            }
                        }
                    }
                }
            }
            
            //decay particle count detection percent, so very old strong hits will be forgotten and altitude range will increase
            m_ParticleCountPercentageOfPredicted = m_ParticleCountPercentageOfPredicted * Math.pow(0.5, (m_lastUpdateDuration_ms * m_timeAccelerationCoefficient) / (m_particleDecayHalfLife_ms*m_MaxParticleHitHalfLifeMultiplier));


            //
            // Determine extents of positive prediciton scores so that
            // we know whether to resize the matrix or not.  Also, copy
            // values from the current z-level into the byte matrix to
            // be published.
            //
            int totalParticles = 0;
            double highestUnknownCellScore = 0;
            double highestTotalCellScore = 0;
            int xIndexOfHighestUnknownScore = -1;
            int yIndexOfHighestUnknownScore = -1;
            int zIndexOfHighestUnknownScore = -1;
            Altitude minPlumeHeight = Altitude.POSITIVE_INFINITY;
            Altitude maxPlumeHeight = Altitude.ZERO;
            LinkedList<Particle> displayPredictionAllParticles = new LinkedList<Particle>();
            double maxTotalScore = 0;
            LinkedList <LatLonAltPosition> displayPredictionRandomLocations = new LinkedList<LatLonAltPosition> ();
            LinkedList<LatLonAltPosition> displayPredictionHighestLocations = new LinkedList<LatLonAltPosition>();
            LinkedList<Double> displayPredictionHighestScores = new LinkedList<Double>();
                    
            {
                {
                    ListIterator<ParticleArrayList> listItr = m_ageSortedParticleList.listIterator();
                    while (listItr.hasNext())
                    {
                        ParticleArrayList list = listItr.next();
                        Particle lastParticleAdded = null;
                        for (Particle particle : list)
                        {
                            if (particle.position.getAltitude().isLowerThan(minPlumeHeight))
                                minPlumeHeight = particle.position.getAltitude();
                            if (particle.position.getAltitude().isHigherThan(maxPlumeHeight))
                                maxPlumeHeight = particle.position.getAltitude();
                            
                            if (shouldTrackParticle (m_TrackingType, particle.source))
                            {
                                displayPredictionAllParticles.add(particle);
                                maxTotalScore = Math.max (maxTotalScore, particle.weight);
                                
                                //Make sure we aren't adding a bunch of particles that are all right on top of each other and were created at the same time.
                                if (lastParticleAdded == null || lastParticleAdded.position.getRangeTo(particle.position).isGreaterThan(new Length (10, Length.METERS)))
                                {
                                    boolean added = false;
                                    for (int i = 0; i < displayPredictionHighestScores.size(); i ++)
                                    {
                                        if (particle.weight > displayPredictionHighestScores.get(i))
                                        {
                                            displayPredictionHighestScores.add(i, particle.weight);
                                            displayPredictionHighestLocations.add(i, particle.position);
                                            lastParticleAdded = particle;
                                            added = true;
                                            break;
                                        }
                                    }
                                    if (!added && displayPredictionHighestScores.size() < CloudPredictionBelief.MAX_HIGHESTLOCATION_LIST_SIZE)
                                    {
                                        displayPredictionHighestScores.addLast(particle.weight);
                                        displayPredictionHighestLocations.addLast (particle.position);    
                                        lastParticleAdded = particle;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (m_ResampleRandomDetections)
                    {
                        m_DisplayPredictionRandomParticles.clear();
                        //
                        // Populate the 2D slice to be published to the display
                        //
                        for (int i = 0; i < CloudPredictionBelief.MAX_RANDOMLOCATION_LIST_SIZE && i < displayPredictionAllParticles.size(); i ++)
                        {
                            //Try to randomly choose particles, but lean towards stronger ones
                            int testIdx = (int)(Math.random()*displayPredictionAllParticles.size());
                            double testScore = displayPredictionAllParticles.get(testIdx).weight;
                            double adjustment = testScore/maxTotalScore;
                            if ((Math.random()+adjustment) > 0.99)
                            {
                                if (!m_DisplayPredictionRandomParticles.contains (displayPredictionAllParticles.get(testIdx)))
                                    m_DisplayPredictionRandomParticles.add(displayPredictionAllParticles.get(testIdx));
                            }
                            else
                                i--;
                        }
                        m_ResampleRandomDetections = false;
                    }
                    
                    for (Particle p : m_DisplayPredictionRandomParticles)
                        displayPredictionRandomLocations.add(p.position);
                    
                    while (displayPredictionHighestLocations.size() > CloudPredictionBelief.MAX_HIGHESTLOCATION_LIST_SIZE)
                        displayPredictionHighestLocations.removeLast(); 
                }
            }

            //The first time through, this reserved particle will be null.  Select any particle from the heaviest
            //location to reserve it.  When particles are deleted, this cell will always be preserved so that we 
            //always have at least one particle from the original unknown explosion particles.
            if (m_ReservedUnknownParticle == null)
            {
                m_ReservedUnknownParticle = heaviestUnknownParticle;
            }
            
            //Determine what is the highest score cell for each of the possible tracking options (total, chem, counts)
            if (heaviestTotalParticle != null)
            {
                if (m_heaviestTotalParticle == null || (Math.abs(heaviestTotalParticle.weight - m_heaviestTotalParticle.weight) > 1E-20))
                    m_heaviestTotalParticle = heaviestTotalParticle;
            }
            
            if (m_GotChemDetections)
            {
                if (m_heaviestChemParticle == null || (Math.abs(heaviestChemParticle.weight - m_heaviestChemParticle.weight) > 1E-20))
                    m_heaviestChemParticle = heaviestChemParticle;
            }
            
            if (m_GotCountsDetections)
            {
                if (m_heaviestCountsParticle == null || (Math.abs(heaviestCountsParticle.weight - m_heaviestCountsParticle.weight) > 1E-20))
                    m_heaviestCountsParticle = heaviestCountsParticle;
            }
            
            //Determine which particle to track based on the user's tracking selection
            if (m_TrackingType == TRACKING_TYPE.PARTICLE)
            {
                m_heaviestCurrParticle = m_heaviestCountsParticle;
                
                m_GotCurrDetections = m_GotCountsDetections;
            }
            else if (m_TrackingType == TRACKING_TYPE.CHEMICAL)
            {
                m_heaviestCurrParticle = m_heaviestChemParticle;
                
                m_GotCurrDetections = m_GotChemDetections;
            }
            else //if (m_TrackingType == TRACKING_TYPE.MIXTURE)
            {
                m_heaviestCurrParticle = m_heaviestTotalParticle;
                
                m_GotCurrDetections = m_GotCountsDetections || m_GotChemDetections;
            }
            
            if (m_heaviestCurrParticle == null)
                m_heaviestCurrParticle = heaviestUnknownParticle;
            if (m_heaviestCurrParticle == null)
            {
                m_heaviestCurrParticle = m_ReservedUnknownParticle;
                JOptionPane.showMessageDialog(null, "Lost unknown particle!!!!", "error", JOptionPane.ERROR_MESSAGE);
            }
            
            System.out.println("TOTAL PARTICLES: " + totalParticles);
            System.out.println("HIGHEST CELL SCORE: " + highestTotalCellScore);
            System.out.println("HEAVIEST PARTICLE WEIGHT (being tracked): " + m_heaviestCurrParticle.weight);
            System.out.println("WEIGHT OF YOUNGEST PARTICLE: " + m_youngestParticle.weight + "\n\n");

            //Altitude minPlumeAltitude = m_matrixBottomNWCornerPosition.getAltitude().plus(m_matrixCellVerticalSideSize.times(minUsedZIndex));
            Altitude minPlumeAltitude = (Altitude)minPlumeHeight.clone();
            //Altitude maxPlumeAltitude = m_matrixBottomNWCornerPosition.getAltitude().plus(m_matrixCellVerticalSideSize.times(maxUsedZIndex));
            Altitude maxPlumeAltitude = (Altitude)maxPlumeHeight.clone();
            
            m_predictedPlumeCenterPositionMSL = m_heaviestCurrParticle.position;


            m_cloudPredictionBelief = new CloudPredictionBelief(agentID,
                                                                minPlumeAltitude,
                                                                maxPlumeAltitude,
                                                                m_predictedPlumeCenterPositionMSL,
                                                                m_predictedPlumeInterceptPositionMSL,
                                                                displayPredictionHighestLocations,
                                                                displayPredictionRandomLocations);
            beliefMgr.put(m_cloudPredictionBelief);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void updateOrbitAltitude()
    {
        m_CommandedAltAGLMeters = m_CommandedAltAGLMeters + m_AltitudeVariationPerStepMeters;

        if(m_CommandedAltAGLMeters <= (m_LowerAltAGLMeters + 5 ) || m_CommandedAltAGLMeters <= (m_minAltitudeAGL.getDoubleValue(Length.METERS)))
        {

            m_CommandedAltAGLMeters = Math.max(m_minAltitudeAGL.getDoubleValue(Length.METERS), m_LowerAltAGLMeters);
            m_AltitudeVariationPerStepMeters = Math.abs(m_AltitudeVariationPerStepMeters);
        }
        else if (m_CommandedAltAGLMeters >= m_UpperAltAGLMeters - 5)
        {
            m_CommandedAltAGLMeters = m_UpperAltAGLMeters;
            m_AltitudeVariationPerStepMeters = -Math.abs(m_AltitudeVariationPerStepMeters);
        }

        double altMSL = m_CommandedAltAGLMeters +  DtedGlobalMap.getDted().getJlibAltitude(m_orbitCenterLatitude.getDoubleValue(Angle.DEGREES), m_orbitCenterLongitude.getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
        System.out.println("Commanded Altitude:  "+ altMSL + " AGL: "+m_CommandedAltAGLMeters + "\n" );
        System.out.println("Max Altitude:  "+ m_UpperAltAGLMeters  + "\n" );
        System.out.println("Min Altitude:  "+ m_LowerAltAGLMeters  + "\n" );

        m_orbitCenterPosition = new LatLonAltPosition(m_orbitCenterPosition.getLatitude(),m_orbitCenterPosition.getLongitude(),new Altitude(altMSL,Length.METERS));

        setOrbitAltitude();
    }

    private void updateOrbitAltitude(boolean nextGate, double percentageOfOrbitComplete)
    {
        /*try
        {
        writer.write (getTimeSinceExplosionSec() + ",");
        writer.flush();
        } catch (Exception e)
        {
            e.printStackTrace();
        }*/

        AltitudeGateParameters currGate = null;
        //Only update altitudes and gates when first orbit with detections is finished
        if(m_GotCurrDetectionsBeforeCurrent)
        {
            //Tracking heaviest particle, only do middle gates
            if (nextGate)
            {
                m_TimeOfLastGateChangeMs = System.currentTimeMillis();

                m_AltitudeGateIndex ++;
            }
            m_AltitudeGateIndex = m_AltitudeGateIndex%m_AltitudeGateSequenceTracking.length;
            currGate = m_AltitudeGateSequenceTracking[m_AltitudeGateIndex];

            //upper and lower limits dependent on heaviest particle location, climb rate, expected detection strength, etc.
            //Update here.
            setTrackingAltLimits ();
        }
        else
        {
            //upper and lower limits calculated by updatePlumeVariables function, called in update function

            //cap upper altitude if plume is likely still below us.
            double minAltAglThresholdMeters = getMinAllowableAglMeters (m_myCurrentPosition);
            //double currMinThresholdAltitudeMeters = (m_UpperAltAGLMeters - m_LowerAltAGLMeters)*m_MinAltThresholdPercentage + m_LowerAltAGLMeters;
            double currMinThresholdAltitudeMeters = (m_LowerAltAGLMeters)*m_MinAltThresholdPercentage + m_LowerAltAGLMeters;
            if (currMinThresholdAltitudeMeters < minAltAglThresholdMeters && m_FirstTimeRaisingAltitude)
            {
                m_LowerAltAGLMeters = Math.min(m_LowerAltAGLMeters, minAltAglThresholdMeters);
                m_UpperAltAGLMeters = (m_UpperAltAGLMeters - m_LowerAltAGLMeters)*m_RestrictedMaxAltThresholdPercentage + m_LowerAltAGLMeters;
            }
            else if (m_FirstTimeRaisingAltitude)
            {
                m_AltitudeGateIndex = Math.max(Math.min(Config.getConfig().getPropertyAsInteger("ParticleCloudPredictionBehavior.StartingAltitudeGateIndex", 4),m_AltitudeGateSequence.length-1),0);
                m_FirstTimeRaisingAltitude = false;
		nextGate = false;
            }


            //No detections yet, do all gates
            if (nextGate)
            {
                m_AltitudeGateIndex = (m_AltitudeGateIndex + 1)%m_AltitudeGateSequence.length;
                m_TimeOfLastGateChangeMs = System.currentTimeMillis();
            }
            currGate = m_AltitudeGateSequence[m_AltitudeGateIndex];

        }


        double totalAltitudeSpanMeters = m_UpperAltAGLMeters - m_LowerAltAGLMeters;

        double currGateStartAltAgl, currGateEndAltAgl, currGateSpanMeters;
        currGateStartAltAgl = m_LowerAltAGLMeters + currGate.startAltPercentage*totalAltitudeSpanMeters;
        currGateEndAltAgl = m_LowerAltAGLMeters + currGate.endAltPercentage*totalAltitudeSpanMeters;
        currGateSpanMeters = currGateEndAltAgl - currGateStartAltAgl;

        m_ForceNextGate = false;
        double altMSL;
        if(m_GotCurrDetectionsBeforeCurrent)
        {
            m_CommandedAltAGLMeters = currGateEndAltAgl;
            altMSL = m_CommandedAltAGLMeters +  DtedGlobalMap.getDted().getJlibAltitude(m_orbitCenterLatitude.getDoubleValue(Angle.DEGREES), m_orbitCenterLongitude.getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
            altMSL = Math.max (altMSL, getMinAllowableMslMeters(m_orbitCenterPosition)); //If commanded altitude is less than safety box, use safety box altitude as limits so next gate will start

            double altDiffM = Math.abs(m_myCurrentPosition.getAltitude().getDoubleValue(Length.METERS) - altMSL);
            if (altDiffM < m_TrackingAltitudeBufferForNextGate.getDoubleValue(Length.METERS))
            {
                System.out.println ("altDiffM = " + altDiffM  + "  curr:" + m_myCurrentPosition.getAltitude().getDoubleValue(Length.METERS) + "  orbit: " + altMSL);
                m_ForceNextGate = true;
            }
            /*switch to altitude check
            if (System.currentTimeMillis() - m_TimeOfLastGateChangeMs > (Math.abs(currGateSpanMeters)/m_TrackingClimbRate_mps*1000))
            {
                
            }*/
        }
        else
        {
            double intervalPercentageToChangeGate = 1.0/m_NumGateSteps;
            int currStep = (int)Math.floor(percentageOfOrbitComplete/intervalPercentageToChangeGate);
            double currStepAltMslMeters = currGateStartAltAgl + currGateSpanMeters*currStep/m_NumGateSteps;
            m_CommandedAltAGLMeters = currStepAltMslMeters;

            altMSL = m_CommandedAltAGLMeters +  DtedGlobalMap.getDted().getJlibAltitude(m_orbitCenterLatitude.getDoubleValue(Angle.DEGREES), m_orbitCenterLongitude.getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
            altMSL = Math.max (altMSL, getMinAllowableMslMeters(m_orbitCenterPosition)); //If commanded altitude is less than safety box, use safety box altitude as limits so next gate will start
        }

        System.out.println("Commanded Altitude:  "+ altMSL + " AGL: "+m_CommandedAltAGLMeters + "\n" );
        System.out.println("Max Altitude:  "+ m_UpperAltAGLMeters  + "\n" );
        System.out.println("Min Altitude:  "+ m_LowerAltAGLMeters  + "\n" );

        /*try
        {
        writer.write ((nextGate?"1":"0") + "," + m_CommandedAltAGLMeters + "," + m_LowerAltAGLMeters + "," + m_UpperAltAGLMeters + "," + currGateStartAltAgl + "," + currGateEndAltAgl + "," + percentageOfOrbitComplete + "," + m_myCurrentPosition.getAltitude().getDoubleValue(Length.METERS) + "," + altMSL + ",\r\n");
        writer.flush();
        } catch (Exception e)
        {
            e.printStackTrace();
        }*/

        m_orbitCenterPosition = new LatLonAltPosition(m_orbitCenterPosition.getLatitude(),m_orbitCenterPosition.getLongitude(),new Altitude(altMSL,Length.METERS));
    }

    private void setOrbitAltitude ()
    {
        TestCircularOrbitBelief tCOB = (TestCircularOrbitBelief)beliefMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
        
        //Ensure the new altitue is >= the minimum alt for the given belief
        if(tCOB!=null){
            double minAlt = tCOB.getMinimumAltitude();
            
            if(minAlt> m_orbitCenterPosition.getAltitude().getDoubleValue(Length.METERS)){
                m_orbitCenterPosition = new LatLonAltPosition(tCOB.getPosition().asLatLonAltPosition().getLatitude(),tCOB.getPosition().asLatLonAltPosition().getLongitude(),new Altitude(minAlt,Length.METERS));
            }else{
                m_orbitCenterPosition = new LatLonAltPosition(tCOB.getPosition().asLatLonAltPosition().getLatitude(),tCOB.getPosition().asLatLonAltPosition().getLongitude(),m_orbitCenterPosition.getAltitude());
            }
        

            TestCircularOrbitBelief circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                                                                  m_orbitCenterPosition,
                                                                  m_orbitRadius,
                                                                  m_orbitClockwise);
        
        //Since this is not at a new location, set as used
        
            if(tCOB!=null && !tCOB.isNew()){
                circularOrbitBelief.use();
                //circularOrbitBelief.setDTEDMapCenter(tCOB.getDTEDMapCenter());
                circularOrbitBelief.setMinimumSafeAltitude(tCOB.getMinimumAltitude());
                m_orbitCenterPosition = circularOrbitBelief.getPosition().asLatLonAltPosition();
                m_orbitCenterLatitude = m_orbitCenterPosition.getLatitude();
                m_orbitCenterLongitude = m_orbitCenterPosition.getLongitude();
                m_orbitCenterAltitudeMSL = m_orbitCenterPosition.getAltitude();
                beliefMgr.put(circularOrbitBelief);
            }else{
                System.out.println("Test if here");
            }
        }
       
    }

    private LatLonAltPosition verifyNewOrbitAltitudePorpoise (LatLonAltPosition orbitCenterPosition)
    {
        Latitude orbitCenterLatitude = orbitCenterPosition.getLatitude();
        Longitude orbitCenterLongitude = orbitCenterPosition.getLongitude();
        Altitude orbitCenterAltitudeMSL = orbitCenterPosition.getAltitude();
        double terrainHeightMeters = DtedGlobalMap.getDted().getJlibAltitude(orbitCenterLatitude.getDoubleValue(Angle.DEGREES), orbitCenterLongitude.getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
        if (terrainHeightMeters <= 0)
        {
            terrainHeightMeters = 0.3048*Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet", 0);
        }

        if (terrainHeightMeters < 100)
        {
            System.out.println("Low DTED");
        }

        double commandedAltAGLMeters = orbitCenterAltitudeMSL.getDoubleValue(Length.METERS) - terrainHeightMeters;



        updatePlumeVariables();
        if (m_GotCurrDetections)
        {
            m_UpperAltAGLMeters = commandedAltAGLMeters + (m_DefaultAltitudeVariationMeters+1);
            m_LowerAltAGLMeters = Math.max(m_minAltitudeAGL.getDoubleValue(Length.METERS), commandedAltAGLMeters - (m_DefaultAltitudeVariationMeters+1));
        }
        if(commandedAltAGLMeters <= (m_LowerAltAGLMeters + 5 ) || commandedAltAGLMeters <= (m_minAltitudeAGL.getDoubleValue(Length.METERS)))
        {

            commandedAltAGLMeters = Math.max(m_minAltitudeAGL.getDoubleValue(Length.METERS), m_LowerAltAGLMeters);
            m_AltitudeVariationPerStepMeters = Math.abs(m_AltitudeVariationPerStepMeters);
        }
        else if (commandedAltAGLMeters >= m_UpperAltAGLMeters - 5)
        {
            commandedAltAGLMeters = m_UpperAltAGLMeters;
            m_AltitudeVariationPerStepMeters = -Math.abs(m_AltitudeVariationPerStepMeters);
        }


        return new LatLonAltPosition (orbitCenterLatitude, orbitCenterLongitude, new Altitude (commandedAltAGLMeters+terrainHeightMeters, Length.METERS));
    }

    private void setTrackingAltLimits ()
    {
        double heavyParticleAltMslMeters = m_heaviestCurrParticle.position.getAltitude().getDoubleValue(Length.METERS);
        double terrainHeightMeters = DtedGlobalMap.getDted().getJlibAltitude(m_heaviestCurrParticle.position.getLatitude().getDoubleValue(Angle.DEGREES), m_heaviestCurrParticle.position.getLongitude().getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
        if (terrainHeightMeters <= 0)
        {
            terrainHeightMeters = 0.3048*Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet", 0);
        }
        if (terrainHeightMeters < 100)
        {
            System.out.println("Low DTED");
        }
        double heavyParticleAltAglMeters = heavyParticleAltMslMeters - terrainHeightMeters;

        /*try
        {
        writer.write (heavyParticleAltAglMeters + ",");
        } catch (Exception e) { e.printStackTrace();}*/

        if(m_GotCurrDetectionsBeforeCurrent && m_numOrbitsSinceDetection < m_numOrbitsPerCycle)
        {
            double trackingSpanMeters;

            //Tracking cloud, restrict altitude swing based on expected particle detections
            if (m_ParticleCountPercentageOfPredicted > m_ParticleCountPredictionThresholdPercentage)
            {
                trackingSpanMeters = m_MinimumAltitudeTrackingSpanMeters;
            }
            else if(m_ParticleCountPercentageOfPredicted < 1E-10)
            {
                trackingSpanMeters = m_MaximumAltitudeTrackingSpanMeters;
            }
            else
            {
                trackingSpanMeters = (1-(m_ParticleCountPercentageOfPredicted/m_ParticleCountPredictionThresholdPercentage))*(m_MaximumAltitudeTrackingSpanMeters-m_MinimumAltitudeTrackingSpanMeters) + m_MinimumAltitudeTrackingSpanMeters;
            }

            m_UpperAltAGLMeters = heavyParticleAltAglMeters + trackingSpanMeters*m_AltitudeTrackingSpanDuringTrackingPercentage*m_PercentageOfAltitudeSpanAboveCenter;
            m_LowerAltAGLMeters = heavyParticleAltAglMeters - trackingSpanMeters*m_AltitudeTrackingSpanDuringTrackingPercentage*m_PercentageOfAltitudeSpanBelowCenter;

            /*try
            {
            writer.write (trackingSpanMeters + "," + m_ParticleCountPercentageOfPredicted + ",");
            } catch (Exception e) { e.printStackTrace();}*/
        }
        else if (m_GotCurrDetectionsBeforeCurrent)
        {
            //Searching for cloud, let altitude search entire climb rate
            m_UpperAltAGLMeters = heavyParticleAltAglMeters + m_MaximumAltitudeTrackingSpanMeters*m_AltitudeTrackingSpanDuringSearchingPercentage*m_PercentageOfAltitudeSpanAboveCenter;
            m_LowerAltAGLMeters = heavyParticleAltAglMeters - m_MaximumAltitudeTrackingSpanMeters*m_AltitudeTrackingSpanDuringSearchingPercentage*m_PercentageOfAltitudeSpanBelowCenter;

            /*try
            {
            writer.write (m_MaximumAltitudeTrackingSpanMeters + ",0,");
            } catch (Exception e) { e.printStackTrace();}*/
        }
    }

    private void calcNewOrbit()
    {
	System.out.println ("calcNewOrbit");
        m_GotCurrDetectionsBeforeCurrent = m_GotCurrDetections;
//        double highScoreRelativeXPos_m = (m_highScoreCellXIndex + 0.5) * m_matrixCellHorizontalSideSize.getDoubleValue(Length.METERS);
//        double highScoreRelativeYPos_m = (m_highScoreCellYIndex + 0.5) * m_matrixCellHorizontalSideSize.getDoubleValue(Length.METERS);
//        double highScoreRelativeZPos_m = (m_highScoreCellZIndex + 0.5) * m_matrixCellVerticalSideSize.getDoubleValue(Length.METERS);
//
//
//        if (highScoreRelativeZPos_m < m_minAltitude.getDoubleValue(Length.METERS))
//        {
//            highScoreRelativeZPos_m = m_minAltitude.getDoubleValue(Length.METERS);
//        }
//
//        Length diagonalToCornerDistance = new Length(Math.sqrt(highScoreRelativeXPos_m * highScoreRelativeXPos_m + highScoreRelativeYPos_m * highScoreRelativeYPos_m), Length.METERS);
//        NavyAngle angleToCorner = new NavyAngle(Math.atan2(highScoreRelativeYPos_m, highScoreRelativeXPos_m) + (Math.PI / 2), Angle.RADIANS);
//        //
//        // Resolve the average index values into a lat/lon/alt position
//        //
//        LatLonAltPosition predictedPlumeCenterPosition = m_matrixBottomNWCornerPosition.translatedBy(new RangeBearingHeightOffset(diagonalToCornerDistance,
//                                                                                                                                  angleToCorner,
//                                                                                                                                  new Length(highScoreRelativeZPos_m, Length.METERS))).asLatLonAltPosition();
        
        m_ForceCalcNewOrbit = false;
        ManualInterceptActualOrbitBelief manualOrbitBelief = (ManualInterceptActualOrbitBelief)beliefMgr.get(ManualInterceptActualOrbitBelief.BELIEF_NAME);
        if (manualOrbitBelief != null && !manualOrbitBelief.getReleaseImmediately() && (m_LastManualOrbitTimestamp == null || manualOrbitBelief.getTimeStamp().after(m_LastManualOrbitTimestamp) || manualOrbitBelief.getForceHoldPosition()))
        {
            Altitude commandedAltitudeMsl;
            if (m_orbitCenterPosition != null)
            {
                WACSWaypointActualBelief currentWwbSettings = (WACSWaypointActualBelief)beliefMgr.get (WACSWaypointActualBelief.BELIEF_NAME);
                if (m_LastNewOrbitPositionTime == null || currentWwbSettings == null || currentWwbSettings.getTimeStamp().after (m_LastNewOrbitPositionTime))
                {
                    //Use default intercept altitutde
                    commandedAltitudeMsl = new Altitude (getMinAllowableMslMeters (m_orbitCenterPosition), Length.METERS);
                }
                else
                {
                    //Get current orbit altitude AGL and maintain it at new manual position
                    Altitude currentAltitudeMsl = m_orbitCenterPosition.getAltitude();
                    double groundAltMsl = DtedGlobalMap.getDted().getAltitudeMSL(m_orbitCenterPosition.getLatitude().getDoubleValue(Angle.DEGREES), m_orbitCenterPosition.getLongitude().getDoubleValue(Angle.DEGREES));
                    double currentAltitudeAglM = currentAltitudeMsl.getDoubleValue(Length.METERS) - groundAltMsl;

                    groundAltMsl = DtedGlobalMap.getDted().getAltitudeMSL(manualOrbitBelief.getInterceptLatitude().getDoubleValue(Angle.DEGREES), manualOrbitBelief.getInterceptLongitude().getDoubleValue(Angle.DEGREES));
                    commandedAltitudeMsl = new Altitude (groundAltMsl + currentAltitudeAglM, Length.METERS);
                }
            }
            else
            {
                //Use default intercept altitutde
                commandedAltitudeMsl = new Altitude (getMinAllowableMslMeters (m_orbitCenterPosition), Length.METERS);
            }
            
                    
            //If this is a new (to us) manual orbit belief, or it requires that it be held, then stick to it
            m_orbitCenterPosition = new LatLonAltPosition(manualOrbitBelief.getInterceptLatitude(), 
                                                            manualOrbitBelief.getInterceptLongitude(),
                                                            commandedAltitudeMsl
                                                            );
            
            m_LastManualOrbitTimestamp = manualOrbitBelief.getTimeStamp();
            m_AltitudeOverride = true;
        }
        else
        {
            LatLonAltPosition predictedPlumeCenterPosition = m_heaviestCurrParticle.position;

            m_orbitCenterPosition = adjustOrbitForTravelTimeAndApproachAngle(predictedPlumeCenterPosition);
            m_AltitudeOverride = false;

            if (m_PorpoiseAltitudeAlgorithm) {
                //Check altitude limits that will be enforced on altitude updates
                m_orbitCenterPosition = verifyNewOrbitAltitudePorpoise(m_orbitCenterPosition);
            } else if (m_GateRampAltitudeAlgorithm) {
                if (m_GotCurrDetectionsBeforeCurrent) {
                    updateOrbitAltitude(false, 0);
                } else {
                    updateOrbitAltitude(true, 0);
                }
            }
        }
        
        

        TestCircularOrbitBelief circularOrbitBelief;

        PiccoloTelemetryBelief telemetryBelief = (PiccoloTelemetryBelief) beliefMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
        double currentUAVSpeed_mps = 0;
        double windSpeedSouth = 0;
        double windSpeedWest = 0;
        if (telemetryBelief != null) {
            currentUAVSpeed_mps = telemetryBelief.getPiccoloTelemetry().IndAirSpeed_mps;
            windSpeedSouth = telemetryBelief.getPiccoloTelemetry().WindSouth;
            windSpeedWest = telemetryBelief.getPiccoloTelemetry().WindWest;
            circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                    m_orbitCenterPosition,
                    m_orbitRadius,
                    m_orbitClockwise,
                    currentUAVSpeed_mps,
                    windSpeedSouth,
                    windSpeedWest);
        } else {
            circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                    m_orbitCenterPosition,
                    m_orbitRadius,
                    m_orbitClockwise);
        }

        
       System.out.println("TCOB Before move " + circularOrbitBelief.toString());
            

        if (m_safetyBox.getSafeCircularOrbit(circularOrbitBelief) != null)
        {
            m_orbitCenterPosition = circularOrbitBelief.getPosition().asLatLonAltPosition();
            beliefMgr.put(circularOrbitBelief);
            
            
            System.out.println("TCOB After move "+circularOrbitBelief.toString());
            
            
            m_orbitCenterLatitude = m_orbitCenterPosition.getLatitude();
            m_orbitCenterLongitude = m_orbitCenterPosition.getLongitude();
            m_orbitCenterAltitudeMSL = m_orbitCenterPosition.getAltitude();
            double terrainHeightMeters = DtedGlobalMap.getDted().getJlibAltitude(m_orbitCenterLatitude.getDoubleValue(Angle.DEGREES), m_orbitCenterLongitude.getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
            if (terrainHeightMeters <= 0)
            {
                terrainHeightMeters = 0.3048*Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet", 0);
            }

            if (terrainHeightMeters < 100)
            {
                System.out.println("Low DTED");
            }

            m_CommandedAltAGLMeters = m_orbitCenterAltitudeMSL.getDoubleValue(Length.METERS) - terrainHeightMeters;
        }
        else
        {
            // The path to the proposed orbit is not safe. Use the previous orbit, but put the altitude to match the predicted cloud altitude
            TestCircularOrbitBelief prevCircularOrbitBelief = (TestCircularOrbitBelief)beliefMgr.get(TestCircularOrbitBelief.BELIEF_NAME);
            LatLonAltPosition prevPosition = prevCircularOrbitBelief.getPosition().asLatLonAltPosition();
            prevCircularOrbitBelief.setPosition(new LatLonAltPosition(prevPosition.getLatitude(), prevPosition.getLongitude(), m_orbitCenterPosition.getAltitude()));
            prevCircularOrbitBelief = m_safetyBox.getSafeCircularOrbit(prevCircularOrbitBelief);
            //prevCircularOrbitBelief.adjustOrbitCenterToSafeLocation(m_myCurrentPosition);
            beliefMgr.put(prevCircularOrbitBelief);
        }


        m_LastNewOrbitPositionTime = new Date (System.currentTimeMillis());
        m_justCompletedOrbit = false;
    }


    private void updatePlumeVariables()
    {
        double timesince = getTimeSinceExplosionSec();

        m_MinExpectedPlumeAltitudeMeters = getMinTopPlumeHeightMeters(Math.max(timesince, m_PlumeHeightCalcDelaySec));
        m_MaxExpectedPlumeAltitudeMeters = getMaxTopPlumeHeightMeters(Math.max(timesince, m_PlumeHeightCalcDelaySec));
        m_ExpectedPlumeThicknessFeet =(m_MaxExpectedPlumeAltitudeMeters - m_MinExpectedPlumeAltitudeMeters) * 3.2808;

        if(m_GotCurrDetections && m_numOrbitsSinceDetection < m_numOrbitsPerCycle)
        {
           if(m_AltitudeVariationPerStepMeters > 0)
                m_AltitudeVariationPerStepMeters = m_DefaultAltitudeVariationMeters;
            else
                m_AltitudeVariationPerStepMeters = -m_DefaultAltitudeVariationMeters;
        }
        else
        {
            double temp = m_MaxExpectedPlumeAltitudeMeters - Math.max(m_MinExpectedPlumeAltitudeMeters, m_minAltitudeAGL.getDoubleValue(Length.METERS));
            m_UpperAltAGLMeters = m_MaxExpectedPlumeAltitudeMeters;
            m_LowerAltAGLMeters = Math.max(m_MinExpectedPlumeAltitudeMeters, m_minAltitudeAGL.getDoubleValue(Length.METERS));
            if(m_AltitudeVariationPerStepMeters > 0)
                m_AltitudeVariationPerStepMeters = Math.max(0,temp)*Math.min(1.0,m_AltUpdateIntervalMs/m_AltUpdatePeriodMs);
            else
                m_AltitudeVariationPerStepMeters = -Math.max(0,temp)*Math.min(1.0,m_AltUpdateIntervalMs/m_AltUpdatePeriodMs);

        }
    }
            


    private void doInitialSetup() throws Exception
    {
        m_predictedPlumeCenterPositionMSL = null;
        m_predictedPlumeInterceptPositionMSL = null;
        m_heaviestCurrParticle = null;
        m_heaviestTotalParticle = null;
        m_heaviestChemParticle = null;
        m_heaviestCountsParticle = null;
        m_youngestParticle = null;
        m_lastUpdateTime_ms = 0;
        m_lastDetectionTime_ms = System.currentTimeMillis();
        m_numUnusedParticles = 0;
        m_ResampleRandomDetections = true;
        m_orbitVarianceIndex = 0;
        m_GotCurrDetections = false;
        m_GotCurrDetectionsBeforeCurrent = false;
        m_GotCountsDetections = false;
        m_GotChemDetections = false;
        m_numOrbitsSinceDetection = 0;
        m_orbitState = 0;
        m_lastOrbitCompletionTime_ms = 0;
        m_FirstTimeRaisingAltitude = true;
        m_AltitudeGateIndex = 0;
        m_TimeOfLastGateChangeMs = System.currentTimeMillis();
        m_ForceNextGate = false;

        //
        // Clear out plume detections from previous intercepts
        //
        CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief) beliefMgr.get(CloudDetectionBelief.BELIEF_NAME);
        if (cloudDetectionBelief != null)
        {
            cloudDetectionBelief.clearDetections();
            beliefMgr.put(cloudDetectionBelief);
        }
 

        updatePlumeVariables();
        double initialNormalizedRange_m = m_MaxExpectedPlumeAltitudeMeters * 4;
        //
        // Generate initial prediction grid
        //
        ExplosionBelief explosionBelief = (ExplosionBelief)beliefMgr.get(ExplosionBelief.BELIEF_NAME);
        if (explosionBelief != null)
        {
            LatLonAltPosition explosionPosition = explosionBelief.getLocation().asLatLonAltPosition();
            Time timeSinceExplosion = new Time(System.currentTimeMillis() - m_explosionTime_ms, Time.MILLISECONDS);
            AbsolutePosition plumePosition = explosionPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeSinceExplosion), m_windBearing, new Length(getRiseRate_ftps() * timeSinceExplosion.getDoubleValue(Time.SECONDS), Length.FEET)));

            m_ageSortedParticleList = new LinkedList<ParticleArrayList>();
            m_ageSortedParticleList.add(new ParticleArrayList(m_numParticles));

            
            double standardDeviationHorizDistanceMeters = 150;
            double standardDeviationAltitudeMeters = 30;
            double maxstandardDeviations = 3;
            Random distRand = new Random();
            Random altRand = new Random (distRand.nextInt());
            for (int i = 0; i < m_numParticles; i ++)
            {
                double gauss = distRand.nextGaussian();
                if (Math.abs(gauss) > maxstandardDeviations)
                    gauss *= maxstandardDeviations/Math.abs(gauss);
                double horizDistanceMeters = gauss*standardDeviationHorizDistanceMeters;
                double angRadians = Math.random()*2*Math.PI;
                gauss = altRand.nextGaussian();
                if (Math.abs(gauss) > maxstandardDeviations)
                    gauss *= maxstandardDeviations/Math.abs(gauss);
                double altDistanceMeters = gauss*standardDeviationAltitudeMeters;
                
                RangeBearingHeightOffset offset = new RangeBearingHeightOffset(new Length (horizDistanceMeters, Length.METERS), 
                        new NavyAngle (angRadians, Angle.RADIANS), 
                        new Length (altDistanceMeters, Length.METERS));
                LatLonAltPosition lla = explosionPosition.translatedBy(offset).asLatLonAltPosition();
            
                Particle particle = new Particle(lla, 0, 1, PARTICLE_SOURCE.UNKNOWN, m_ageSortedParticleList.element());
                m_ageSortedParticleList.element().add(particle);
                m_youngestParticle = particle;
            }


            //
            // Randomize the order of the initial particles in the
            // aged list so that when the are selected to be
            // destroyed, we destroy random ones throughout
            // rather than a bunch of adjacent ones.
            //
            Collections.shuffle(m_ageSortedParticleList.element());
            

            /*jch!!
             * form a matrix based on 
            Altitude minPlumeAltitude = m_matrixBottomNWCornerPosition.getAltitude().plus(m_matrixCellVerticalSideSize.times(minCloudZIndex));
            Altitude maxPlumeAltitude = m_matrixBottomNWCornerPosition.getAltitude().plus(m_matrixCellVerticalSideSize.times(maxCloudZIndex));

            m_cloudPredictionBelief = new CloudPredictionBelief(agentID,
                                                                m_matrixBottomNWCornerPosition,
                                                                m_matrixTopSECornerPosition,
                                                                m_matrixSizeX_cells,
                                                                m_matrixSizeY_cells,
                                                                m_matrixCellHorizontalSideSize,
                                                                minPlumeAltitude,
                                                                maxPlumeAltitude,
                                                                null,
                                                                null,
                                                                m_displayPredictionByteMatrix2DSlice);
            beliefMgr.put(m_cloudPredictionBelief);
*/

            getLatestWindVelocity();


            LatLonAltPosition currentPosition = null;
            NavyAngle currentHeading = null;
            AgentPositionBelief agentPositionBelief = (AgentPositionBelief) beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
            if (agentPositionBelief != null)
            {
                PositionTimeName positionTimeName = agentPositionBelief.getPositionTimeName(WACSAgent.AGENTNAME);
                if (positionTimeName != null)
                {
                    currentPosition = positionTimeName.getPosition().asLatLonAltPosition();
                    currentHeading = positionTimeName.getHeading();
                }
            }
            m_myCurrentPosition = currentPosition;

            m_orbitCenterPosition = adjustOrbitForTravelTimeAndApproachAngle(plumePosition.asLatLonAltPosition());

            //Calculate Where the max top of the plume will be on hitting the intercept orbit and set the initial intercept alt to 
            // 0.9 of this value (Just below the top of the plume)
            
            double terrainHeightMeters = DtedGlobalMap.getDted().getJlibAltitude(m_orbitCenterPosition.getLatitude().getDoubleValue(Angle.DEGREES), m_orbitCenterPosition.getLongitude().getDoubleValue(Angle.DEGREES)).asLength().getDoubleValue(Length.METERS);
            m_CommandedAltAGLMeters =  0.9 * getMaxTopPlumeHeightMeters(m_TimeToIntercept + timeSinceExplosion.getDoubleValue(Time.SECONDS));
            m_orbitCenterPosition = new LatLonAltPosition(m_orbitCenterPosition.getLatitude(),m_orbitCenterPosition.getLongitude(), new Altitude(terrainHeightMeters + m_CommandedAltAGLMeters, Length.METERS));

            TestCircularOrbitBelief circularOrbitBelief;/* = new TestCircularOrbitBelief(agentID,
                                                                              m_orbitCenterPosition,
                                                                              m_orbitRadius,
                                                                              m_orbitClockwise);*/
                      
            PiccoloTelemetryBelief telemetryBelief = (PiccoloTelemetryBelief)beliefMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            double currentUAVSpeed_mps = 0;
            double windSpeedSouth = 0;
            double windSpeedWest = 0;
            if (telemetryBelief != null)
            {
                currentUAVSpeed_mps = telemetryBelief.getPiccoloTelemetry().IndAirSpeed_mps;
                windSpeedSouth = telemetryBelief.getPiccoloTelemetry().WindSouth;
                windSpeedWest = telemetryBelief.getPiccoloTelemetry().WindWest;
                circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                                                                  m_orbitCenterPosition,
                                                                  m_orbitRadius,
                                                                  m_orbitClockwise,
                                                                  currentUAVSpeed_mps,
                                                                  windSpeedSouth,
                                                                  windSpeedWest);
            }else{
                circularOrbitBelief = new TestCircularOrbitBelief(agentID,
                                                                  m_orbitCenterPosition,
                                                                  m_orbitRadius,
                                                                  m_orbitClockwise);
            }
            
            System.out.println("TCOB Before move " + circularOrbitBelief.toString());
            if ((circularOrbitBelief=m_safetyBox.getSafeCircularOrbit(circularOrbitBelief)) != null)
            { 
                System.out.println("TCOB AFter move " + circularOrbitBelief.toString());
                m_orbitCenterPosition = circularOrbitBelief.getPosition().asLatLonAltPosition();
                beliefMgr.put(circularOrbitBelief);

                m_orbitCenterLatitude = m_orbitCenterPosition.getLatitude();
                m_orbitCenterLongitude = m_orbitCenterPosition.getLongitude();
                m_orbitCenterAltitudeMSL = m_orbitCenterPosition.getAltitude();
            }
            else
            {
                AgentModeActualBelief loiterModeBelief = new AgentModeActualBelief(agentID, new Mode(LoiterBehavior.MODENAME));
                beliefMgr.put(loiterModeBelief);
                return;
            }
        }
        else
        {
            throw new Exception("Can't do setup, no ExplsionBelief present");
        }
    }

    private void getLatestWindVelocity()
    {
        METBelief metBelief = (METBelief) beliefMgr.get(METBelief.BELIEF_NAME);
        if (metBelief != null)
        {
            METTimeName metTimeName = metBelief.getMETTimeName(agentID);
            m_windBearing = metTimeName.getWindBearing();
            m_windSpeed = metTimeName.getWindSpeed();
        }
        else
        {
            m_windBearing = NavyAngle.ZERO;
            m_windSpeed = Speed.ZERO;
        }

        //m_windBearing = new NavyAngle(15.0, Angle.DEGREES);
        //m_windSpeed = new Speed(5.0,Speed.METERS_PER_SECOND);

        m_windSpeed = m_windSpeed.times(m_windScaleFactor);
    }

    private double calcCurrOrbitAngle()
    {
        double orbitAngle = 0;

        if (m_orbitCenterLatitude != null &&
                m_orbitCenterLongitude != null &&
                m_orbitCenterAltitudeMSL != null)
        {
            LatLonAltPosition currOrbitPosition = new LatLonAltPosition(m_orbitCenterLatitude, m_orbitCenterLongitude, m_orbitCenterAltitudeMSL);
            LatLonAltPosition uavPosition = getUAVPosition();

            NavyAngle angleToTarget = currOrbitPosition.getBearingTo(uavPosition);

            if (!m_reachedNewOrbit)
            {
                Length distanceToOrbitCenter = uavPosition.getRangeTo(currOrbitPosition);
                if (distanceToOrbitCenter.isLessThan(m_orbitRadius.times(m_ReachedInterceptOrbitFactor)))
                {
                    m_reachedNewOrbit = true;
                    orbitAngle = 0;
                }
            }
            else
            {
                if (m_orbitStartAngle == null)
                {
                    if (m_numOrbitsSinceDetection < m_numOrbitsPerCycle)
                    {
                        m_orbitStartAngle = angleToTarget;
                        m_orbitState = 0;
                        orbitAngle = 0;
                    }
                    else
                    {
                        //
                        // Only do half an orbit if we're in search mode
                        //
                        m_orbitStartAngle = angleToTarget.plus(new Angle(180, Angle.DEGREES));
                        m_orbitState = 1;
                        orbitAngle = 0;
                    }
                }
                else if (m_orbitClockwise)
                {
                    if (m_orbitState == 0)
                    {
                        NavyAngle oppositeStartAngle = m_orbitStartAngle.plus(new Angle(180, Angle.DEGREES));
                        if (angleToTarget.negativeAngleTo(oppositeStartAngle).isLessThan(angleToTarget.negativeAngleTo(m_prevAngleToTarget)) &&
                                (angleToTarget.negativeAngleTo(m_prevAngleToTarget).isLessThan(180, Angle.DEGREES)))
                        {
                            m_orbitState = 1;
                        }
                        m_justCompletedOrbit = false;
                    }
                    else
                    {
                        if (m_orbitState == 1)
                        {
                            if (angleToTarget.negativeAngleTo(m_orbitStartAngle).isLessThan(angleToTarget.negativeAngleTo(m_prevAngleToTarget)) &&
                                    (angleToTarget.negativeAngleTo(m_prevAngleToTarget).isLessThan(180, Angle.DEGREES)))
                            {
                                m_lastOrbitCompletionTime_ms = System.currentTimeMillis();
                                m_justCompletedOrbit = true;                                
                                m_orbitStartAngle = null;
                                m_reachedNewOrbit = false;
                            }
                            else
                            {
                                m_justCompletedOrbit = false;
                            }
                        }
                        else
                        {
                            m_justCompletedOrbit = false;
                        }
                    }
                    if (!m_justCompletedOrbit)
                        orbitAngle = angleToTarget.negativeAngleTo(m_orbitStartAngle).getDoubleValue(Angle.DEGREES);
                    else
                        orbitAngle = 360;
                }
                else
                {
                    if (m_orbitState == 0)
                    {
                        NavyAngle oppositeStartAngle = m_orbitStartAngle.plus(new Angle(180, Angle.DEGREES));
                        if (angleToTarget.positiveAngleTo(oppositeStartAngle).isLessThan(angleToTarget.positiveAngleTo(m_prevAngleToTarget)) &&
                                (angleToTarget.positiveAngleTo(m_prevAngleToTarget).isLessThan(180, Angle.DEGREES)))
                        {
                            m_orbitState = 1;
                        }

                        m_justCompletedOrbit = false;
                    }
                    else
                    {
                        if (m_orbitState == 1)
                        {
                            if (angleToTarget.positiveAngleTo(m_orbitStartAngle).isLessThan(angleToTarget.positiveAngleTo(m_prevAngleToTarget)) &&
                                    (angleToTarget.positiveAngleTo(m_prevAngleToTarget).isLessThan(180, Angle.DEGREES)))
                            {
                                m_justCompletedOrbit = true;
                                m_lastOrbitCompletionTime_ms = System.currentTimeMillis();
                                m_orbitState = 0;
                                m_orbitStartAngle = null;
                                m_reachedNewOrbit = false;
                            }
                            else
                            {
                                m_justCompletedOrbit = false;
                            }
                        }
                        else
                        {
                            m_justCompletedOrbit = false;
                        }
                    }
                    if (!m_justCompletedOrbit)
                        orbitAngle = angleToTarget.positiveAngleTo(m_orbitStartAngle).getDoubleValue(Angle.DEGREES);
                    else
                        orbitAngle = 360;
                }

                m_prevAngleToTarget = angleToTarget;
            }
        }

        System.err.println ("*************************Orbit angle: " + orbitAngle + " deg");
        if (m_numOrbitsSinceDetection >= m_numOrbitsPerCycle)
        {
            //Search mode only goes around 180 degrees
            return Math.max(orbitAngle-180,0)/180;
        }
        else
        {
            return orbitAngle/360;
        }
        
    }

    private LatLonAltPosition getUAVPosition()
    {
        AgentPositionBelief agentPositionBelief = (AgentPositionBelief) beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
        if (agentPositionBelief != null)
        {
            m_currUAVPosition = agentPositionBelief.getPositionTimeName(agentID).getPosition().asLatLonAltPosition();
            m_currUAVHeading = agentPositionBelief.getPositionTimeName(agentID).getHeading();
            return m_currUAVPosition;
        }
        else
        {
            return null;
        }
    }

    private double getNumSecondsDirectToInterceptPosition(LatLonAltPosition currentPlumePosition)
    {              
        double bearingToPlume_rad = m_myCurrentPosition.getBearingTo(currentPlumePosition).getDoubleValue(Angle.RADIANS);
        double distanceToPlume_m = m_myCurrentPosition.getRangeTo(currentPlumePosition).getDoubleValue(Length.METERS);
        double plumeRelativeX_m = distanceToPlume_m * Math.sin(bearingToPlume_rad);
        double plumeRelativeY_m = distanceToPlume_m * Math.cos(bearingToPlume_rad);
        if (plumeRelativeY_m == 0)
        {
            //Add a small value here to keep the calculations from blowing up when relative Y is zero
            plumeRelativeY_m = 0.000001;
        }
        double windBearing_rad = m_windBearing.getDoubleValue(Angle.RADIANS);
        double windSpeed_mps = m_windSpeed.getDoubleValue(Speed.METERS_PER_SECOND);
        double planeSpeed_mps = m_planeInterceptSpeed.getDoubleValue(Speed.METERS_PER_SECOND);
        double A = plumeRelativeX_m / plumeRelativeY_m;
        double C = (A * windSpeed_mps * Math.sin(windBearing_rad) - windSpeed_mps * Math.cos(windBearing_rad)) / planeSpeed_mps;

        //
        // There are two solutions to the intercept equations, one for positive time and
        // one for negative time.  We have to calculate both and then use whichever
        // one results in a positive value for time.
        //
        double interceptAngle1_rad = 2 * Math.atan((A + Math.sqrt(A * A - C * C + 1)) / (C - 1));
        double interceptAngle2_rad = 2 * Math.atan((A - Math.sqrt(A * A - C * C + 1)) / (C - 1));
        double timeToIntercept1_sec = plumeRelativeY_m / (planeSpeed_mps * Math.sin(interceptAngle1_rad) - windSpeed_mps * Math.sin(windBearing_rad));
        double timeToIntercept2_sec = plumeRelativeY_m / (planeSpeed_mps * Math.sin(interceptAngle2_rad) - windSpeed_mps * Math.sin(windBearing_rad));
        if (timeToIntercept1_sec >= 0)
        {
            return timeToIntercept1_sec;
        }
        else
        {
            return timeToIntercept2_sec;
        }

        
    }


    private LatLonAltPosition adjustOrbitForTravelTimeAndApproachAngle(LatLonAltPosition predictedPlumeCenterPosition)
    {
        




        
        //
        // Determine if this will be a search orbit and if so what the displacement will be
        //
        boolean inSearchMode = false;
        boolean isSeachOrbit = false;
        Length searchOrbitDisplacement = Length.ZERO;

        if (m_numOrbitsSinceDetection >= m_numOrbitsPerCycle)
        {
            inSearchMode = true;

            if (((m_numOrbitsSinceDetection - m_numOrbitsPerCycle) % (m_numSearchOrbitsBetweenNormalOrbits_m + 1)) != m_numSearchOrbitsBetweenNormalOrbits_m)
            {
                isSeachOrbit = true;
                searchOrbitDisplacement = new Length(m_orbitSearchDisplacementPerOrbitsCycle_m * (m_numOrbitsSinceDetection / m_numOrbitsPerCycle) , Length.METERS);
            }
        }



        Time timeToGetStraightToPlumeIntercept = new Time(getNumSecondsDirectToInterceptPosition(predictedPlumeCenterPosition), Time.SECONDS);

        //
        // We first calculate the plume intercept position and the orbit to intercept it.  Then we have to recalculate
        // to take into account the fact that we won't be flying directly to the intercept point, but rather to
        // and around the orbit to get to the intercept point.
        //
        LatLonAltPosition orbitCenterPosition = predictedPlumeCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeToGetStraightToPlumeIntercept),
                                                                                                                       m_windBearing,
                                                                                                                       new Length(getRiseRate_ftps() * timeToGetStraightToPlumeIntercept.getDoubleValue(Time.SECONDS), Length.FEET))).asLatLonAltPosition();


        //
        // Vary the bearing of the orbit rather than always putting it directly downwind
        //
        orbitCenterPosition = orbitCenterPosition.translatedBy(new RangeBearingHeightOffset(m_orbitRadius.plus(searchOrbitDisplacement),
                                                                                            m_windBearing.plus(m_orbitVarianceOffsets[m_orbitVarianceIndex]),
                                                                                            Length.ZERO)).asLatLonAltPosition();        



        // Adjust orbit position to take into account the fact that we will hit the orbit
        // not necessarily at the plume intercept position.  We need to calculate the time
        // to get to the orbit itself first.
        Length distanceToOrbitEdge;
        Length distanceToOrbitCenter = m_myCurrentPosition.getRangeTo(orbitCenterPosition);
        if (distanceToOrbitCenter.isLessThan(m_orbitRadius))
        {
            distanceToOrbitEdge = m_orbitRadius.minus(distanceToOrbitCenter);
        }
        else
        {
            distanceToOrbitEdge = distanceToOrbitCenter.minus(m_orbitRadius);
        }
        
        Time timeToGetToOrbit = distanceToOrbitEdge.dividedBy(m_planeInterceptSpeed);
        m_TimeToIntercept = timeToGetToOrbit.getDoubleValue(Time.SECONDS);

        orbitCenterPosition = predictedPlumeCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeToGetToOrbit),
                                                                                                     m_windBearing,
                                                                                                     new Length(getRiseRate_ftps() * timeToGetToOrbit.getDoubleValue(Time.SECONDS), Length.FEET))).asLatLonAltPosition();

        //
        // Re-insert the orbit angle variance on the adjusted orbit location
        //
        orbitCenterPosition = orbitCenterPosition.translatedBy(new RangeBearingHeightOffset(m_orbitRadius.plus(searchOrbitDisplacement),
                                                                                            m_windBearing.plus(m_orbitVarianceOffsets[m_orbitVarianceIndex]),
                                                                                            Length.ZERO)).asLatLonAltPosition();






        //
        // If we have to turn around to get to the next orbit, calculate
        // the time it will take and where we will be at the
        // end of the turn.
        //
        calcTurnArc(orbitCenterPosition);

        //
        // Recalculate distance to orbit given new orbit position and
        // turn arc calculations.
        //
        Length distanceFromTurnExitToOrbitCenter = m_turnArcExitPoint.getRangeTo(orbitCenterPosition);
        Length distanceFromTurnExitToOrbitEdge = distanceFromTurnExitToOrbitCenter.minus(m_orbitRadius);

        Time timeToGetToOrbitIncludingTurnArc;
        if (distanceFromTurnExitToOrbitEdge.isLessThan(Length.ZERO))
        {
            timeToGetToOrbitIncludingTurnArc = distanceToOrbitEdge.dividedBy(m_planeInterceptSpeed);
        }
        else
        {
            timeToGetToOrbitIncludingTurnArc = m_turnArcLength.plus(distanceFromTurnExitToOrbitEdge).dividedBy(m_planeInterceptSpeed);
        }

        orbitCenterPosition = orbitCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeToGetToOrbitIncludingTurnArc.minus(timeToGetToOrbit)),
                                                                                            m_windBearing,
                                                                                            new Length(getRiseRate_ftps() * timeToGetToOrbitIncludingTurnArc.minus(timeToGetToOrbit).getDoubleValue(Time.SECONDS), Length.FEET))).asLatLonAltPosition();

        NavyAngle bearingFromOrbitCenterToTurnExitPoint = orbitCenterPosition.getBearingTo(m_turnArcExitPoint);
        NavyAngle bearingFromOrbitCenterToInterceptPoint = orbitCenterPosition.getBearingTo(predictedPlumeCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeToGetToOrbitIncludingTurnArc),
                                                                                                                                                                   m_windBearing,
                                                                                                                                                                   Length.ZERO)));

        //
        // Calculate how far along the circle we would have to travel in each
        // direction to intercept the plume
        //
        Angle clockwiseArcAngle = bearingFromOrbitCenterToTurnExitPoint.clockwiseAngleTo(bearingFromOrbitCenterToInterceptPoint);
        Angle counterclockwiseArcAngle = bearingFromOrbitCenterToTurnExitPoint.counterClockwiseAngleTo(bearingFromOrbitCenterToInterceptPoint);


        //
        // Determine whether our next orbit should be clockwise or counterclockwise
        //
        distanceFromTurnExitToOrbitEdge = m_turnArcExitPoint.getRangeTo(orbitCenterPosition);

        //
        // Determine our current min turn radius
        //
        PiccoloTelemetryBelief telemetryBelief = (PiccoloTelemetryBelief)beliefMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
        if (telemetryBelief != null)
        {
            double currentSpeed_mps = telemetryBelief.getPiccoloTelemetry().IndAirSpeed_mps;
            m_minTurnRadius = new Length((currentSpeed_mps * currentSpeed_mps) / (9.81 * Math.tan(Math.toRadians(Config.getConfig().getPropertyAsDouble("FlightControl.maxBankAngle_deg")))), Length.METERS);
            m_minTurnExitDistanceFromEdgeToReverseDirection = m_minTurnRadius.times(m_DistanceToReverseInOrbits).times(2.0);
        }

        if (distanceToOrbitEdge.isGreaterThan(m_minTurnExitDistanceFromEdgeToReverseDirection))
        {
            if (clockwiseArcAngle.isLessThan(counterclockwiseArcAngle))
            {
                m_orbitClockwise = true;
            }
            else
            {
                m_orbitClockwise = false;
            }
        }
        else
        {

            //
            // If we are close to the plume edge, orbit clockwise if the orbit
            // is to our right. If the orbit is to our left, orbit counterclockwise.
            //
            NavyAngle bearingFromCurrPosToOrbitCenter = m_currUAVPosition.getBearingTo(orbitCenterPosition);
            if (m_currUAVHeading.clockwiseAngleTo(bearingFromCurrPosToOrbitCenter).isLessThan(m_currUAVHeading.counterClockwiseAngleTo(bearingFromCurrPosToOrbitCenter)))
            {
                m_orbitClockwise = true;
            }
            else
            {
                m_orbitClockwise = false;
            }
        }
        
        
        //
        // Calculate distance we'll travel around the orbit and adjust
        // the orbit location further down wind to account for this
        //
        Angle arcAngle;        
        if (m_orbitClockwise)
        {
            arcAngle = clockwiseArcAngle;
        }
        else
        {
            arcAngle = counterclockwiseArcAngle;
        }

        Length orbitLengthBeforeIntercept = m_orbitRadius.times(arcAngle.getDoubleValue(Angle.RADIANS));
        Time timeOrbitingBeforeIntercept = orbitLengthBeforeIntercept.dividedBy(m_planeInterceptSpeed);
        orbitCenterPosition = orbitCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeOrbitingBeforeIntercept),
                                                                                            m_windBearing,
                                                                                            new Length(getRiseRate_ftps() * timeOrbitingBeforeIntercept.getDoubleValue(Time.SECONDS), Length.FEET))).asLatLonAltPosition();

        m_predictedPlumeInterceptPositionMSL = predictedPlumeCenterPosition.translatedBy(new RangeBearingHeightOffset(m_windSpeed.times(timeToGetToOrbitIncludingTurnArc.plus(timeOrbitingBeforeIntercept)),
                                                                                                                      m_windBearing,
                                                                                                                      Length.ZERO)).asLatLonAltPosition();

        m_predictedPlumeInterceptPositionMSL = m_predictedPlumeInterceptPositionMSL.translatedBy(new RangeBearingHeightOffset(searchOrbitDisplacement,
                                                                                                                              m_windBearing.plus(m_orbitVarianceOffsets[m_orbitVarianceIndex]),
                                                                                                                              Length.ZERO)).asLatLonAltPosition();


        // Don't increment orbit variance index if this is a normal orbit
        // during search mode rather than a search orbit
        if (!inSearchMode || isSeachOrbit)
        {
            m_orbitVarianceIndex = (m_orbitVarianceIndex + 1) % m_orbitVarianceOffsets.length;
        }


        return orbitCenterPosition;
    }


    //
    // Uses equation in Geometric Tools for Computer Graphics by Schneider and Eberly
    // on page 289.
    //
    void calcTurnArc(final LatLonAltPosition destination)
    {
        LatLonAltPosition myPosition = getUAVPosition();
        NavyAngle myHeading = m_currUAVHeading;

        Angle bearingToDestination = myPosition.getBearingTo(destination).minus(myHeading);
        if (bearingToDestination.getDoubleValue(Angle.DEGREES) < 0)
        {
            bearingToDestination = bearingToDestination.plus(Angle.FULL_CIRCLE);
        }

        if ((bearingToDestination.isGreaterThan(270, Angle.DEGREES) || bearingToDestination.isLessThan(90, Angle.DEGREES)) ||
            (myPosition.getRangeTo(destination).isLessThan(m_orbitRadius) && bearingToDestination.isGreaterThan(45, Angle.DEGREES) && bearingToDestination.isLessThan(135, Angle.DEGREES)) ||
            (myPosition.getRangeTo(destination).isLessThan(m_orbitRadius) && bearingToDestination.isGreaterThan(225, Angle.DEGREES) && bearingToDestination.isLessThan(305, Angle.DEGREES)))
        {
            m_turnArcExitPoint = myPosition;
            m_turnArcLength = Length.ZERO;
        }
        else
        {
            boolean turnToTheRight;
            if (bearingToDestination.isGreaterThan(0, Angle.DEGREES) && bearingToDestination.isLessThan(180, Angle.DEGREES))
            {
                turnToTheRight = true;
            }
            else
            {
                turnToTheRight = false;
            }

            LatLonAltPosition turnCircleCenterPoint;
            if (turnToTheRight)
            {
                turnCircleCenterPoint = myPosition.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius,
                                                                                             myHeading.plus(new Angle(90, Angle.DEGREES)),
                                                                                             Length.ZERO)).asLatLonAltPosition();
            }
            else
            {
                turnCircleCenterPoint = myPosition.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius,
                                                                                             myHeading.plus(new Angle(-90, Angle.DEGREES)),
                                                                                             Length.ZERO)).asLatLonAltPosition();
            }

            RangeBearingHeightOffset Uvector = destination.getRangeBearingHeightOffsetTo(turnCircleCenterPoint);
            double Ux = Math.sin(Uvector.getBearing().getDoubleValue(Angle.RADIANS)) * Uvector.getRange().getDoubleValue(Length.METERS);
            double Uy = Math.cos(Uvector.getBearing().getDoubleValue(Angle.RADIANS)) * Uvector.getRange().getDoubleValue(Length.METERS);
            double r = m_minTurnRadius.getDoubleValue(Length.METERS);

            if (Ux == 0)
            {
                Ux = 0.001;
            }

            if (Uy == 0)
            {
                Uy = 0.001;
            }

            double Ux_sqrd = Ux * Ux;
            double Uy_sqrd = Uy * Uy;
            double r_sqrd = r * r;

            double sqrt_component = Math.sqrt(-1 * Math.pow(r, 4) * Ux_sqrd + r_sqrd * Math.pow(Ux, 4) + r_sqrd * Ux_sqrd * Uy_sqrd);
            double Vx1 = (r_sqrd - (r_sqrd * Uy_sqrd) / (Ux_sqrd + Uy_sqrd) + ((Uy * sqrt_component) / (Ux_sqrd + Uy_sqrd))) / Ux;
            double Vx2 = (r_sqrd - (r_sqrd * Uy_sqrd) / (Ux_sqrd + Uy_sqrd) - ((Uy * sqrt_component) / (Ux_sqrd + Uy_sqrd))) / Ux;
            double Vy1 = (r_sqrd * Uy + sqrt_component) / (Ux_sqrd + Uy_sqrd);
            double Vy2 = (r_sqrd * Uy - sqrt_component) / (Ux_sqrd + Uy_sqrd);

            NavyAngle V1Heading = new NavyAngle(Math.atan2(Vy1, Vx1), Angle.RADIANS);
            NavyAngle V2Heading = new NavyAngle(Math.atan2(Vy2, Vx2), Angle.RADIANS);

            NavyAngle currPositionOnArc = turnCircleCenterPoint.getBearingTo(myPosition);

            if (turnToTheRight)
            {
                if ((!V1Heading.isNaN() && V2Heading.isNaN()) ||
                    (!V1Heading.isNaN() && currPositionOnArc.clockwiseAngleTo(V1Heading).isLessThan(currPositionOnArc.clockwiseAngleTo(V2Heading))))
                {
                    m_turnArcExitPoint = turnCircleCenterPoint.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius, V1Heading, Length.ZERO)).asLatLonAltPosition();
                    m_turnArcLength = m_minTurnRadius.times(currPositionOnArc.clockwiseAngleTo(V1Heading).getDoubleValue(Angle.RADIANS));
                }
                else if (!V2Heading.isNaN())
                {
                    m_turnArcExitPoint = turnCircleCenterPoint.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius, V2Heading, Length.ZERO)).asLatLonAltPosition();
                    m_turnArcLength = m_minTurnRadius.times(currPositionOnArc.clockwiseAngleTo(V2Heading).getDoubleValue(Angle.RADIANS));
                }
                else
                {
                    // The destination point is inside our min turn radius, so there
                    // is no solution to the equation.
                    m_turnArcExitPoint = myPosition;
                    m_turnArcLength = Length.ZERO;
                }
            }
            else
            {
                if ((!V1Heading.isNaN() && V2Heading.isNaN()) ||
                    (!V1Heading.isNaN() && currPositionOnArc.counterClockwiseAngleTo(V1Heading).isLessThan(currPositionOnArc.counterClockwiseAngleTo(V2Heading))))

                {
                    m_turnArcExitPoint = turnCircleCenterPoint.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius, V1Heading, Length.ZERO)).asLatLonAltPosition();
                    m_turnArcLength = m_minTurnRadius.times(currPositionOnArc.counterClockwiseAngleTo(V1Heading).getDoubleValue(Angle.RADIANS));
                }
                else if (!V2Heading.isNaN())
                {
                    m_turnArcExitPoint = turnCircleCenterPoint.translatedBy(new RangeBearingHeightOffset(m_minTurnRadius, V2Heading, Length.ZERO)).asLatLonAltPosition();
                    m_turnArcLength = m_minTurnRadius.times(currPositionOnArc.counterClockwiseAngleTo(V2Heading).getDoubleValue(Angle.RADIANS));
                }
                else
                {
                    // The destination point is inside our min turn radius, so there
                    // is no solution to the equation.
                    m_turnArcExitPoint = myPosition;
                    m_turnArcLength = Length.ZERO;
                }
            }
        }
    }
    
    private double getMinAllowableAglMeters(LatLonAltPosition lla) 
    {
        double prefMinAltAgl_m = m_minAltitudeAGL.getDoubleValue(Length.METERS);
        double minMSL_m = m_safetyBox.getSafeOrbitAltitudeMSLFromAGL (lla.getLatitude().getDoubleValue(Angle.DEGREES), lla.getLongitude().getDoubleValue(Angle.DEGREES), prefMinAltAgl_m);
        return (minMSL_m - DtedGlobalMap.getDted().getJlibAltitude(lla.getLatitude().getDoubleValue(Angle.DEGREES), lla.getLongitude().getDoubleValue(Angle.DEGREES)).getDoubleValue(Length.METERS));
    }
    
    private double getMinAllowableMslMeters(LatLonAltPosition lla) 
    {
        double prefMinAltAgl_m = m_minAltitudeAGL.getDoubleValue(Length.METERS);
        return m_safetyBox.getSafeOrbitAltitudeMSLFromAGL (lla.getLatitude().getDoubleValue(Angle.DEGREES), lla.getLongitude().getDoubleValue(Angle.DEGREES), prefMinAltAgl_m);
    }
    
}
