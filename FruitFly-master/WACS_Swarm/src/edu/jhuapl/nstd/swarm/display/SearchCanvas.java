//=============================== UNCLASS ================================== 
//  
// Copyright (c) 2001 The Johns Hopkins University/Applied Physics Laboratory 
// Developed by JHU/APL. 
// 
// This material may be reproduced by or for the U.S. Government pursuant to 
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988). 
// For any other permissions please contact JHU/APL. 
// 
//=============================== UNCLASS ================================== 
//========================================================================== 
//                   ABANDON HOPE ALL YE WHO ENTER HERE
//==========================================================================
package edu.jhuapl.nstd.swarm.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;
import java.io.*;
import java.text.*;
import java.net.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.event.*;
import edu.jhuapl.jlib.jgeo.*;
import edu.jhuapl.jlib.jgeo.event.*;
import edu.jhuapl.jlib.jgeo.action.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.cbrnPods.bridgeportProcessor;
import edu.jhuapl.nstd.math.CoordConversions.PositionStringFormatter;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.BearingTimeName;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.ClassificationTimeName;
import edu.jhuapl.nstd.swarm.belief.CloudBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudPredictionBelief;
import edu.jhuapl.nstd.swarm.belief.ConfigProperty;
import edu.jhuapl.nstd.swarm.belief.DynamicNoGoBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetection;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EtdDetectionListBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.GammaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.IrCameraFOVBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.KeepOutRegionBelief;
import edu.jhuapl.nstd.swarm.belief.LidarBelief;
import edu.jhuapl.nstd.swarm.belief.LoiterApproachPathBelief;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.ManualInterceptCommandedOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.NoGoBelief;
import edu.jhuapl.nstd.swarm.belief.NoGoTimeName;
import edu.jhuapl.nstd.swarm.belief.PacketInfoBelief;
import edu.jhuapl.nstd.swarm.belief.PathBelief;
import edu.jhuapl.nstd.swarm.belief.PathTimeName;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackDefinitionCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.RegionBelief;
import edu.jhuapl.nstd.swarm.belief.SNRBelief;
import edu.jhuapl.nstd.swarm.belief.SearchGoalBelief;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TestCircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.UAVWaypointBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.display.DisplayUnitsManager.DisplayUnitsChangeListener;
import edu.jhuapl.nstd.swarm.display.docking.DraggableTitleOverlayPanel;
import edu.jhuapl.nstd.swarm.display.geoimageryfetcher.Cell;
import edu.jhuapl.nstd.swarm.display.geoimageryfetcher.GeoImageryCanvas;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.util.PolygonRegion;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.renderable.RenderableImage;


public class SearchCanvas extends GeoImageryCanvas implements JGeoMouseListener,
        JGeoMouseMotionListener,
        TableModelListener,
        ActionListener,
        Updateable,
        DisplayUnitsChangeListener {

    public static final Length EIGHT_HUNDRED_METERS = new Length(800, Length.METERS);
    public static final Length SIX_HUNDRED_METERS = new Length(600, Length.METERS);
    public static final Length FOUR_HUNDRED_METERS = new Length(400, Length.METERS);
    public static final Length TEN_METERS = new Length(10, Length.METERS);
    public static final Length CLOUD_ELLIPSE_DIST = new Length(50, Length.METERS);
    private static final int OVERLAY_LEVEL = 2;
    private static final int POSITION_LEVEL = 1;
    private static final int MAPS_LEVEL = 0;
    public static final int METERS = 0;
    public static final int FEET = 1;
    private java.util.List _points = null;
    private boolean _showLines = false;
    private PanUpAction panUp;
    private PanDownAction panDown;
    private PanLeftAction panLeft;
    private PanRightAction panRight;
    private Action mercatorAction;
    private Action orthoAction;
    private Action exitAction;
    //private ZoomInAction zoomInAction;
    //private ZoomOutAction zoomOutAction;
    protected Latitude m_MouseGroundLat = Latitude.ZERO;
    protected Longitude m_MouseGroundLon = Longitude.ZERO;
    protected Altitude m_MouseGroundAltM = Altitude.ZERO;
    protected DecimalFormat m_DecFormat1 = new DecimalFormat("#.#");
    protected DecimalFormat m_DecFormat2 = new DecimalFormat("#.##");
    protected DecimalFormat m_DecFormat6 = new DecimalFormat("#.######");
    public BeliefManagerWacs beliefManager;
    public AgentTracker agentTracker; //For AgentRClickMenu
    private AbsolutePosition startGoalPosition = null;
    private AbsolutePosition currentGoalPosition = null;
    protected PrimitiveTypeGeocentricMatrix resetGoalMatrix = null;
    private AbsolutePosition cloudPointAPosition;
    private AbsolutePosition cloudPointBPosition;
    private boolean _drawOnlyOverlay = false;
    private JButton finishButton = new JButton("Finish");
    private JToolBar m_ToolBar;
    private ButtonGroup m_ToolButtonGroup;
    private JToggleButton rulerButton;
    //private JToggleButton gimbalTargetButton;
    //private JToggleButton loiterPatternButton;
    //private JToggleButton trackPatternButton;
    //private JToggleButton holdTrackPatternButton;
    //private JButton holdTrackReleaseButton;
    private JButton locateUavButton;
    //private int m_HoldTrackReleaseButtonToolbarIndex;
    //private JCheckBox interceptTargetButton = new JCheckBox("Move Intercept Orbit");
    private JButton showAltitudeButton = new JButton("Show Altitude");
    private JButton showRNButton = new JButton("Show RN Histograms");
    private JButton showShadowDataButton = new JButton("Show Shadow Data");
    private JRadioButton lockButton = new JRadioButton("Lock-in Changes");
    private JRadioButton goalButton = new JRadioButton("Refresh Display");
    private JRadioButton addButton = new JRadioButton("Add Search Region");
    private JRadioButton noGoButton = new JRadioButton("NoGo");
    private JRadioButton noGoRegionButton = new JRadioButton("Add NoGo Region");
    private JRadioButton removeNoGoRegionButton = new JRadioButton("Remove NoGo Region");
    private JRadioButton addCloudButton = new JRadioButton("Add Simulated Cloud");
    private boolean m_AddSimulatedCloud;
    private JRadioButton addCloudDetectButton = new JRadioButton("Add Cloud Detection");
    private JRadioButton targetButton = new JRadioButton("Add Target");
    private JRadioButton namesButton = new JRadioButton("Show Names");
    private boolean m_ShowNamesOpt;
    private JRadioButton showCloud = new JRadioButton("Show Cloud");
    private boolean m_ShowCloudOpt;
    private JRadioButton showDirection = new JRadioButton("Show Bearing");
    private boolean m_ShowBearingOpt;
    private JRadioButton showHistory = new JRadioButton("Show Position History");
    private boolean m_ShowHistoryOpt;
    private JRadioButton showWind = new JRadioButton("Show Wind");
    private boolean m_ShowWindOpt;
    private boolean m_ShowNextOrbitOpt;
    private JRadioButton showUAVWaypoint = new JRadioButton("Show UAS Waypoints");
    private JRadioButton showMatrixButton = new JRadioButton("Show Matrix");
    private JRadioButton showRealObstacles = new JRadioButton("Show Real Obstacles");
    private JRadioButton showLittleObstacles = new JRadioButton("Show Little Obstacles");
    private JRadioButton showPath = new JRadioButton("Show Path");
    private JRadioButton showSNREdges = new JRadioButton("Show SNR");
    private JRadioButton showVideoMetadata = new JRadioButton("Show Video Metadata");
    private boolean m_ShowKeepOutOpt;
    private boolean m_ShowSafetyBoxOpt;
    private boolean m_ShowLocationBubbleOpt;
    private boolean m_ShowScaleOpt;
    private boolean m_ShowMouseInfoOpt;
    private boolean m_ShowWindCompassOpt;
    private double m_WindTextAlignTransientDeg;
    //private JRadioButton imageButton = new JRadioButton("MoveImage");
    private ButtonGroup controlGroup = new ButtonGroup();
    private final Object m_HoverMousePositionLock = new Object();
    private LatLonAltPosition m_HoverMousePosition = null;
    private LatLonAltPosition m_LoiterGhostStartPosition = null;
    private LatLonAltPosition m_TrackGhostPosition = null;
    //private LatLonAltPosition m_LoiterGhostEndPosition = null;
    private JFrame parentFrame;
    private JLabel gpsTelemetry = new JLabel(" UAS Not Connected ");
    protected boolean lockSelected = true;
    protected boolean goalChanged = false;
    //the agent id is pulled from the system properties
    protected String agentID = " ";
    private boolean redrawSafetyBox = true;
    private boolean redrawSearchBelief = true;
    private boolean redrawPositionBelief = true;
    private boolean redrawBearingBelief = true;
    private boolean redrawTargetBelief = true;
    //Sentel sensor
    private ModePanel modePanel = null;
    private int target_counter = 0;
    private Vector images = new Vector();
    private ModeMap modeMap;
    
    private AgentPrediction _bubblePredictor;
    
    private boolean altShowing = false;
    private boolean rnShowing = false;
    private SearchDisplay _display;
    private AbsolutePosition _safetyBoxNWCorner = null;
    private Length _safetyBoxLongitudinalLength;
    private Length _safetyBoxLatitudinalLength;
    private final Object _safetyBoxLock = new Object();
    private SafetyBox _safetyBox;
    private boolean _inDrawSafetyBoxMode = false;
    private SafetyBoxPanel _safetyBoxPanel = null;
    private AbsolutePosition _proposedSafetyBoxNWCorner = null;
    private Length _proposedSafetyBoxLongitudinalLength;
    private Length _proposedSafetyBoxLatitudinalLength;
    private CircularOrbitBelief _safeCircularOrbitBelief = null;
    private RacetrackOrbitBelief _safeRacetrackOrbitBelief = null;
    private long _safeCircularOrbitBeliefTimestamp = 0;
    private long _safeRacetrackOrbitBeliefTimestamp = 0;
    private long _mouseHoverLastUpdate_ms = 0;
    private CloudDetection m_detectionMouseHoveredOn = null;
    private CircularOrbitBelief m_orbitMouseHoveredOn = null;
    private RacetrackOrbitBelief m_racetrackMouseHoveredOn = null;
    private boolean m_bubbleMouseHoveredOn = false;
    private final Object m_mouseHoverLock = new Object();
    private Point m_mouseHoverPoint = null;
    private final Color m_mouseHoverOverlayBackgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.6f);
    private final Color m_mouseHoverOverlayForegroundColor = new Color(1.0f, 1.0f, 1.0f, 0.9f);
    private final SimpleDateFormat m_mouseHoverDateFormatter = new SimpleDateFormat("HH:mm:ss  yyyy/MM/dd");
    private ShadowDataForm m_shadowDataForm = null;


    LatLonAltPosition m_RacetrackOrbitStartPosition;
    Length m_RacetrackOrbitDiameter;
    Angle m_RacetrackNormalAngleLeft;
    LatLonAltPosition m_RacetrackOrbitEndPosition;
    Angle m_RacetrackNormalAngleRight;
    LatLonAltPosition m_RacetrackOrbitNormalPosLeftStart;
    LatLonAltPosition m_RacetrackOrbitNormalPosLeftEnd;
    LatLonAltPosition m_RacetrackOrbitNormalPosRightStart;
    LatLonAltPosition m_RacetrackOrbitNormalPosRightEnd;
    LatLonAltPosition m_RacetrackTickPositions[];
    NavyAngle m_RacetrackTickBearings[];
    LatLonAltPosition m_CircularOrbitTickPositions[];
    NavyAngle m_CircularOrbitTickBearings[];
    int m_RacetrackDirectionTickIdx = 0;
    int m_RacetrackDirectionTickCount = 0;
    int m_RacetrackDirectionTickInterval = 0;
    int m_CircularOrbitDirectionTickIdx = 0;
    int m_CircularOrbitDirectionTickCount = 0;
    int m_CircularOrbitDirectionTickInterval = 0;
    int m_ApproachPathDotsSize;
    int m_CountdownTimerFontSize;

    Date ghprevTime;
    Date ahprevTime;
    Date adprevTime;
    Date gdprevTime;

    Length m_FinalLoiterAlt;
    Length m_StandoffLoiterAlt;
    Length m_LoiterRadius;
    Length m_InterceptAlt;
    Length m_InterceptRadius;
    Date lastUpdatedWwbSettingsTime;
    Date lastUpdatedRdbTime;

    
    double currRotationDeg = 0;    

    int  mainSplitPixels;
    int  splitPixels;

    int altDisplayPixels;
    int rnDisplayPixels;

    JFrame _owner;
    boolean m_UseImageryFetcher;
    JMenuItem m_UnitsMenuItem;
    float m_PanDistance;
    
    JMenuItem m_WindEstimationMenuItem;
    WindEstimationPanel m_WindPanel = null;
    JMenuItem m_AglDisplayMenuItem;
    JFrame m_AglPanel = null;
                

    RNTotalCountsTracker gammaTotalCountsTracker;
    RNTotalCountsTracker alphaTotalCountsTracker;
    boolean accumlatingGammaBackground = false;

    int m_GammaCountsToAverageTracker;
    int m_GammaStdDevLimitTracker;
    int m_GammaMinimumCountsTracker;
    int m_AlphaCountsToAverageTracker;
    int m_AlphaStdDevLimitTracker;
    int m_AlphaMinimumCountsTracker;
    private boolean definingKeepOutRegion=false;
    
    private Length m_CircleRadius = new Length (100, Length.METERS);

    private final static int MIN_HUE = 120;
    private final static int MAX_HUE = 350;
    //private int _EtdMinDisplayValue;
    //private int _EtdMaxDisplayValue;
    private float _EtdMinDisplayValue;
    private float _EtdMaxDisplayValue;
    private List<EtdDetection> _etdDetections;
    private long _etdLatestUpdateTime = 0L;
    private int _etdHistoryLength = 15;
    private int _etdMarkerSize = 10;
    private double _etdHoverRange = 100.0;
    private Boolean _shouldFilter = false;
    
    public SearchCanvas(int numberOfLevels,
            Length range,
            int width,
            int height,
            int projectionType,
            LatLonAltPosition center,
            BeliefManager belMgr, ModeMap modeMap,
            JFrame owner,
            AgentTracker agentTracker)
    {
        this(numberOfLevels,range,width,height,projectionType,center,belMgr,modeMap,owner,agentTracker,null);
    }
    


    /**
     * Constructor to create JGeoCanvas, bypassing imagery fetcher.
     *
     * @param numberOfLevels
     * @param range
     * @param width
     * @param height
     * @param projectionType
     * @param center
     * @param belMgr
     * @param modeMap
     * @param owner
     * @param agentTracker
     * @param d
     */
    public SearchCanvas(int numberOfLevels,
            Length range,
            int width,
            int height,
            int projectionType,
            LatLonAltPosition center,
            BeliefManager belMgr, ModeMap modeMap,
            JFrame owner,
            AgentTracker agentTracker,
            SearchDisplay d)
    {
        super(numberOfLevels, range, width, height, projectionType, center, 7, 19);
        m_UseImageryFetcher = false;
        intializeCanvas (range, width, height, center, belMgr, modeMap, owner, agentTracker, d);
    }

    /**
     * Constructor to create canvas with imagery fetcher.  Projection type is forced to Mercator
     * @param numberOfLevels
     * @param range
     * @param width
     * @param height
     * @param center
     * @param belMgr
     * @param modeMap
     * @param owner
     * @param agentTracker
     * @param d
     */
        public SearchCanvas(int numberOfLevels,
            Length range,
            int width,
            int height,
            LatLonAltPosition center,
            BeliefManager belMgr, ModeMap modeMap,
            JFrame owner,
            AgentTracker agentTracker,
            SearchDisplay d)
    {
        super(numberOfLevels, range, width, height, center, "./imagesFetched/", true, 7, 19);
        m_UseImageryFetcher = true;
        intializeCanvas (range, width, height, center, belMgr, modeMap, owner, agentTracker, d);
    }

    private void intializeCanvas (Length range,
            int width,
            int height,
            LatLonAltPosition center,
            BeliefManager belMgr, ModeMap modeMap,
            JFrame owner,
            AgentTracker agentTracker,
            SearchDisplay d)
    {
        _etdDetections = new LinkedList<EtdDetection>();
        _etdHistoryLength = Config.getConfig().getPropertyAsInteger("EtdDetectionBelief.historyLength", 15);
        _etdMarkerSize = Config.getConfig().getPropertyAsInteger("Etd.markerSize", 10);
        _etdHoverRange = Config.getConfig().getPropertyAsDouble("Etd.hoverRange", 100.0);
        _shouldFilter = Config.getConfig().getPropertyAsBoolean("Etd.shouldFilter", false);

        try
        {
            _display = d;
            _owner = owner;
            this.beliefManager = (BeliefManagerWacs)belMgr;
            this.modeMap = modeMap;
            this.agentTracker = agentTracker;
            _safetyBox = new SafetyBox(beliefManager);
            this.setShowBackgroundImage (true);
            gpsTelemetry.setMinimumSize(new Dimension (100, 15));
            gpsTelemetry.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
            
//            setTileBufferSize(Config.getConfig().getPropertyAsInteger("SearchCanvas.TileBuffer", 10));

            _EtdMinDisplayValue = (float) Config.getConfig().getPropertyAsDouble("Etd.MinDisplayValue", 0.0);
            _EtdMaxDisplayValue = (float) Config.getConfig().getPropertyAsDouble("Etd.MaxDisplayValue", 0.2);
            
            //_EtdMinDisplayValue = Config.getConfig().getPropertyAsInteger("Etd.MinDisplayValue", 0);
            //_EtdMaxDisplayValue = Config.getConfig().getPropertyAsInteger("Etd.MaxDisplayValue", 50);            
            
            altDisplayPixels = Config.getConfig().getPropertyAsInteger("SearchCanvas.altdisplayheight", 150);
            rnDisplayPixels = Config.getConfig().getPropertyAsInteger("SearchCanvas.rndisplayheight", 300);
            m_ApproachPathDotsSize = Config.getConfig().getPropertyAsInteger("SearchCanvas.ApproachPathDotsSize", 10);
            m_CountdownTimerFontSize = Config.getConfig().getPropertyAsInteger("SearchCanvas.CountdownTimerFontSize", 25);
            m_PanDistance = (float)Config.getConfig().getPropertyAsDouble("SearchCanvas.PanDistance", 0.05);

            m_GammaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaCountsToAverageTracker", 5);
            m_GammaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaStdDevLimitTracker", 3);
            m_GammaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaMinimumCountsTracker", 100);
            m_AlphaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaCountsToAverageTracker", 5);
            m_AlphaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaStdDevLimitTracker", 3);
            m_AlphaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaMinimumCountsTracker", 10);

            gammaTotalCountsTracker = new RNTotalCountsTracker(m_GammaCountsToAverageTracker, m_GammaStdDevLimitTracker, m_GammaMinimumCountsTracker);
            alphaTotalCountsTracker = new RNTotalCountsTracker(m_AlphaCountsToAverageTracker, m_AlphaStdDevLimitTracker, m_AlphaMinimumCountsTracker);
            
            _bubblePredictor = new AgentPrediction(beliefManager, WACSAgent.AGENTNAME);
            _bubblePredictor.startPredicting();
            
            m_ToolButtonGroup = new ButtonGroup() {
                @Override
                public void setSelected(ButtonModel model, boolean selected) 
                {
                    if (selected) 
                    {
                        super.setSelected(model, selected);
                    } 
                    else 
                    {
                        clearSelection();
                    }
                }
            };
            
            locateUavButton = new JButton(new ImageIcon (getClass().getResource("/icons/LocateUAV32.png")));
            locateUavButton.setToolTipText ("Locate UAS on map");
            
            rulerButton = new JToggleButton(new ImageIcon (getClass().getResource("/icons/Ruler32.png")));
            rulerButton.setToolTipText("Use ruler tool");
            m_ToolButtonGroup.add(rulerButton);
            
            /*
            gimbalTargetButton = new JToggleButton(new ImageIcon (getClass().getResource("/icons/Target32.png")));
            gimbalTargetButton.setToolTipText("Set strike location");
            m_ToolButtonGroup.add(gimbalTargetButton);
            */
            
            /*
            loiterPatternButton = new JToggleButton(new ImageIcon (getClass().getResource("/icons/Loiter32.png")));
            loiterPatternButton.setToolTipText("Set loiter location");
            m_ToolButtonGroup.add(loiterPatternButton);
            */
            
            /*
            trackPatternButton = new JToggleButton(new ImageIcon (getClass().getResource("/icons/Track32.png")));
            trackPatternButton.setToolTipText("Set tracking location");
            m_ToolButtonGroup.add(trackPatternButton);
            */
            
            /*
            holdTrackPatternButton = new JToggleButton(new ImageIcon (getClass().getResource("/icons/Hold32.png")));
            holdTrackPatternButton.setToolTipText("Hold tracking location");
            m_ToolButtonGroup.add(holdTrackPatternButton);
            */
            
            /*
            holdTrackReleaseButton = new JButton(new ImageIcon (getClass().getResource("/icons/HoldRelease32.png")));
            holdTrackReleaseButton.setToolTipText("Release tracking hold");
            */
            
            mainSplitPixels = 1400;
            splitPixels = altDisplayPixels;

            parentFrame = owner;
            agentID = System.getProperty("agent.name");
            
            m_RacetrackDirectionTickIdx = 0;
            m_RacetrackDirectionTickCount = Config.getConfig().getPropertyAsInteger("SearchCanvas.RotationTickCount", 5);
            m_CircularOrbitDirectionTickIdx = 0;
            m_CircularOrbitDirectionTickCount = Config.getConfig().getPropertyAsInteger("SearchCanvas.RotationTickCount", 5);
            

            //create our ButtonGroup
            controlGroup.add(lockButton);
            controlGroup.add(goalButton);
            controlGroup.add(noGoButton);
            controlGroup.add(targetButton);
            controlGroup.add(addButton);
            controlGroup.add(noGoRegionButton);
            controlGroup.add(removeNoGoRegionButton);
            controlGroup.add(addCloudButton);
            controlGroup.add(addCloudDetectButton);
            lockButton.setSelected(true);

            //add listeners to the buttons
            lockButton.addActionListener(this);
            goalButton.addActionListener(this);
            noGoButton.addActionListener(this);
            addButton.addActionListener(this);
            locateUavButton.addActionListener(this);
            rulerButton.addActionListener(this);
            //gimbalTargetButton.addActionListener(this);
            //loiterPatternButton.addActionListener(this);
            //trackPatternButton.addActionListener(this);
            //holdTrackPatternButton.addActionListener(this);
            //holdTrackReleaseButton.addActionListener(this);

            namesButton.addActionListener(this);
            showCloud.addActionListener(this);
            showDirection.addActionListener(this);
            showHistory.addActionListener(this);
            showWind.addActionListener(this);
            showUAVWaypoint.addActionListener(this);
            showMatrixButton.addActionListener(this);
            showRealObstacles.addActionListener(this);
            showLittleObstacles.addActionListener(this);
            showPath.addActionListener(this);
            showSNREdges.addActionListener(this);
            showVideoMetadata.addActionListener(this);
            noGoRegionButton.addActionListener(this);
            removeNoGoRegionButton.addActionListener(this);
            addCloudButton.addActionListener(this);
            addCloudDetectButton.addActionListener(this);
            finishButton.addActionListener(this);
            showHistory.setSelected(true);
            showHistory (showHistory.isSelected());
            showWind.setSelected(false);
            showWind (showWind.isSelected());
            namesButton.setSelected(true);
            showNames (namesButton.isSelected());
            showUAVWaypoint.setSelected(true);
            showMatrixButton.setSelected(true);
            showCloud.setSelected(true);
            showCloud (showCloud.isSelected());
            showRealObstacles.setSelected(true);



            //imageButton.addActionListener(this);

            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension (width, height));
            setBackground(Color.black);

            // NOTE: The layout needs to be set null in order to prevent inadvertent
            //			 layout of the components for this canvas (which need to have absolute positions)
            //
            // The layout is used for the pop-up display when the user mouses over an item on the map
            setLayout(null);

            Length[] keyLengths = new Length[]{
                new Length(1, Length.FEET),
                new Length(10, Length.FEET),
                new Length(50, Length.FEET),
                new Length(100, Length.FEET),
                new Length(500, Length.FEET),
                new Length(1000, Length.FEET),
                new Length(2000, Length.FEET),
                new Length(3000, Length.FEET),
                new Length(4000, Length.FEET),
                new Length(5000, Length.FEET),
                new Length(1, Length.MILES),
                new Length(5, Length.MILES),
                new Length(10, Length.MILES),
                new Length(50, Length.MILES),
                new Length(100, Length.MILES),
                new Length(500, Length.MILES),
                new Length(1000, Length.MILES),
                new Length(5000, Length.MILES),
                new Length(10000, Length.MILES),};


            mercatorAction = new MercatorAction(this);
            orthoAction = new OrthographicAction(this);
            
            panUp = new PanUpAction(this);
            panUp.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/Up32.png")));
            panDown = new PanDownAction(this);
            panDown.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/Down32.png")));
            panLeft = new PanLeftAction(this);
            panLeft.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/Left32.png")));
            panRight = new PanRightAction(this);
            panRight.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/Right32.png")));
            
            panUp.setScreenDistance(m_PanDistance);
            panDown.setScreenDistance(m_PanDistance);
            panLeft.setScreenDistance(m_PanDistance);
            panRight.setScreenDistance(m_PanDistance);

            exitAction = new ExitAction();
            
            if (m_UseImageryFetcher)
            {
                this.setViewCenter(center);
                this.setHorizontalRange(range);
            }

            addJGeoMouseListener(this);
            addJGeoMouseMotionListener(this);
            addMouseWheelListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (!m_UseImageryFetcher)
        {
            URL url;

            try {

                File imageDir = new File("./images");
                System.err.println("getting files");
                String[] files = imageDir.list();
                LatLonAltPosition position;
                Length imgWidth;
                BufferedImage image;
                Length imgHeight;

                for (int i = 0; i < files.length; i++) {
                    System.err.println("File: " + files[i]);
                    if ((new File("./images/" + files[i])).isDirectory()) {
                        continue;
                    }
                    position = getImagePosition(files[i]);
                    imgWidth = getImageWidth(files[i]);
                    imgHeight = getImageHeight(files[i]);
                    if (position != null) {
                        url = getClass().getClassLoader().getResource(files[i]);
                        image = ImageIO.read(url);
                        images.add(new ImageData(imgHeight, imgWidth, position, image));
                    }
                }
                this.setViewCenter(GridFactory.STARTING_POSITION);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //this.setHorizontalRange(SIX_HUNDRED_METERS);
            this.setHorizontalRange(new Length(4, Length.KILOMETERS));
        }

        DisplayUnitsManager.addChangeListener(this);

    // SJM - No more polling 
    //new Thread(this).start();

    }

    private LatLonAltPosition getImagePosition(String filename) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(filename, "_");
            tokenizer.nextToken();
            tokenizer.nextToken();
            String lat = tokenizer.nextToken();
            String lon = tokenizer.nextToken();
            System.err.println("lat: " + lat);
            System.err.println("lon: " + lon);
            Double latDouble = new Double(lat);
            Double lonDouble = new Double(lon);

            Latitude jLat = new Latitude(latDouble.doubleValue(), Angle.DEGREES);
            Longitude jLon = new Longitude(lonDouble.doubleValue(), Angle.DEGREES);
            return new LatLonAltPosition(jLat, jLon, Altitude.ZERO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Length getImageWidth(String filename) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(filename, "_");
            String imgWidth = tokenizer.nextToken();
            Double widthDouble = new Double(imgWidth);
            return new Length(widthDouble.doubleValue(), Length.METERS);
        } catch (Exception e) {
            return null;
        }
    }

    private Length getImageHeight(String filename) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(filename, "_");
            tokenizer.nextToken();
            String imgHeight = tokenizer.nextToken();
            Double heightDouble = new Double(imgHeight);
            return new Length(heightDouble.doubleValue(), Length.METERS);
        } catch (Exception e) {
            return null;
        }
    }

    public JFrame getParentFrame() {
        return parentFrame;
    }

    private void doOverlays (JGeoGraphics jg)
    {
        jg.setLayer(OVERLAY_LEVEL);
        jg.clear();
        FontMetrics fm = jg.getFontMetrics();
        
        if (m_ShowMouseInfoOpt)
        {
            String mouseText = "";
            mouseText += "Cursor  ";
            
            int posUnits = DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.POSITION_UNITS);
            if (posUnits == DisplayUnitsManager.POSITION_DD)
            {
                mouseText += "Lat: " + PositionStringFormatter.formatLatORLonAsDecDeg(m_MouseGroundLat.getDoubleValue(Angle.DEGREES), "N", "S");
                mouseText += "  Lon: " + PositionStringFormatter.formatLatORLonAsDecDeg(m_MouseGroundLon.getDoubleValue(Angle.DEGREES), "E", "W");
            }
            else if (posUnits == DisplayUnitsManager.POSITION_DM)
            {
                mouseText += "Lat: " + PositionStringFormatter.formatLatORLonAsDegMin(m_MouseGroundLat.getDoubleValue(Angle.DEGREES), "N", "S");
                mouseText += "  Lon: " + PositionStringFormatter.formatLatORLonAsDegMin(m_MouseGroundLon.getDoubleValue(Angle.DEGREES), "E", "W");
            }
            else if (posUnits == DisplayUnitsManager.POSITION_DMS)
            {
                mouseText += "Lat: " + PositionStringFormatter.formatLatORLonAsDegMinSec(m_MouseGroundLat.getDoubleValue(Angle.DEGREES), "N", "S");
                mouseText += "  Lon: " + PositionStringFormatter.formatLatORLonAsDegMinSec(m_MouseGroundLon.getDoubleValue(Angle.DEGREES), "E", "W");
            }
            else if (posUnits == DisplayUnitsManager.POSITION_MGRS)
            {
                mouseText += "Pos: " + PositionStringFormatter.formatLatLonAsMGRS(m_MouseGroundLat.getDoubleValue(Angle.DEGREES), m_MouseGroundLon.getDoubleValue(Angle.DEGREES));
            }
            
            mouseText += "  MSL: ";
            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                mouseText += m_DecFormat1.format(m_MouseGroundAltM.getDoubleValue(Length.FEET)) + "ft";
            else
                mouseText += m_DecFormat1.format(m_MouseGroundAltM.getDoubleValue(Length.METERS)) + "m";

            jg.setColor(m_mouseHoverOverlayBackgroundColor);
//            jg.fillRoundRect(5, 5, 350, 25, 20, 20);
            
            jg.fillRoundRect(5, 5, fm.stringWidth(mouseText) + 20, 25, 20, 20);
            jg.setColor(Color.WHITE);
            jg.drawString(mouseText, 15, 20);
            
            String etdText = getEtdText();
            if(etdText!=null) {
                jg.setColor(m_mouseHoverOverlayBackgroundColor);
                jg.fillRoundRect(5, 35, fm.stringWidth(etdText)+20, 25, 20, 20);
                jg.setColor(Color.WHITE);
                jg.drawString(etdText, 15, 50);
            }
        }


        if (m_HoverMousePosition != null)
        {
            /*
            if (loiterPatternButton.isSelected() && m_LoiterRadius != null)
            {
                synchronized (m_HoverMousePositionLock)
                {
                    if (m_HoverMousePosition != null)
                    {
                        jg.setColor (Color.LIGHT_GRAY);
                        Stroke oldStroke = jg.getStroke();
                        jg.setStroke (new BasicStroke (2));
                        Ellipse e = new Ellipse (m_LoiterRadius.times(2), m_LoiterRadius.times(2), m_HoverMousePosition, NavyAngle.NORTH);
                        jg.drawEllipse(e);

                        jg.drawLine(m_HoverMousePosition, NavyAngle.NORTH, m_LoiterRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.SOUTH, m_LoiterRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.EAST, m_LoiterRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.WEST, m_LoiterRadius.times(.1));

                        jg.setStroke (oldStroke);
                    }
                }
            }
            */
            /*
            if ((holdTrackPatternButton.isSelected() || trackPatternButton.isSelected()) && m_InterceptRadius != null)
            {
                synchronized (m_HoverMousePositionLock)
                {
                    if (m_HoverMousePosition != null)
                    {
                        jg.setColor (Color.LIGHT_GRAY);
                        Stroke oldStroke = jg.getStroke();
                        jg.setStroke (new BasicStroke (2));
                        Ellipse e = new Ellipse (m_InterceptRadius.times(2), m_InterceptRadius.times(2), m_HoverMousePosition, NavyAngle.NORTH);
                        jg.drawEllipse(e);

                        jg.drawLine(m_HoverMousePosition, NavyAngle.NORTH, m_InterceptRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.SOUTH, m_InterceptRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.EAST, m_InterceptRadius.times(.1));
                        jg.drawLine(m_HoverMousePosition, NavyAngle.WEST, m_InterceptRadius.times(.1));

                        jg.setStroke (oldStroke);
                    }
                }
            }
            */
            
            /*
            if (gimbalTargetButton.isSelected())
            {
                synchronized (m_HoverMousePositionLock)
                {
                    if (m_HoverMousePosition != null)
                    {
                        drawTargetCrosshairs (jg, m_HoverMousePosition);
                    }
                }
            }
            */
        }

        /*
        //Ghost loiter orbit
        if (m_LoiterGhostStartPosition != null && m_LoiterRadius != null)
        {
            synchronized (loiterPatternButton)
            {
                if (m_LoiterGhostStartPosition != null && m_LoiterRadius != null)
                {
                    jg.setColor (Color.LIGHT_GRAY);
                    Stroke oldStroke = jg.getStroke();
                    jg.setStroke (new BasicStroke (1));

                    //If racetrack isn't possible, then this is good enough
                    Ellipse e = new Ellipse (m_LoiterRadius.times(2), m_LoiterRadius.times(2), m_LoiterGhostStartPosition, NavyAngle.NORTH);
                    jg.drawEllipse(e);
                    
                    jg.setStroke(oldStroke);
                }
            }
        }
        */
        
        /*
        //Ghost manual hold orbit
        if (m_TrackGhostPosition != null && m_InterceptRadius != null)
        {
            synchronized (holdTrackPatternButton)
            {
                if (m_TrackGhostPosition != null && m_InterceptRadius != null)
                {
                    jg.setColor (Color.LIGHT_GRAY);
                    Stroke oldStroke = jg.getStroke();
                    jg.setStroke (new BasicStroke (1));

                    Ellipse e = new Ellipse (m_InterceptRadius.times(2), m_InterceptRadius.times(2), m_TrackGhostPosition, NavyAngle.NORTH);
                    jg.drawEllipse(e);
                    
                    jg.setStroke(oldStroke);
                }
            }
        }
        */
        jg.setColor(Color.WHITE);
        
        IrExplosionAlgorithmEnabledBelief irBlf = (IrExplosionAlgorithmEnabledBelief)beliefManager.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
        if (irBlf != null)
        {
            int totalSeconds = (int)(irBlf.getTimeUntilExplosionMs()/1000);
            String baseText = null;
            
            ExplosionTimeActualBelief expTimeBelief = (ExplosionTimeActualBelief)beliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME);
            ExplosionBelief explosionBelief = (ExplosionBelief)beliefManager.get(ExplosionBelief.BELIEF_NAME);
            if (explosionBelief != null && expTimeBelief != null && expTimeBelief.getTimeStamp().before(new Date (explosionBelief.getTime_ms())))
            {
                //If explosion belief says an explosion occurred after the expected explosion time was entered (ignoring when the expected explosion was, just when the time was put in
                //Then we should make a past strike time based on explosion
                totalSeconds = (int)(System.currentTimeMillis() - explosionBelief.getTime_ms())/1000;
                baseText = "Past strike: ";   //Explosion shouldn't have happened yet anyway
            }
            else if (expTimeBelief != null)
            {
                //In this case, the no explosion has been detected since the expected explosion time was entered.
                if (totalSeconds > 0)
                    baseText = "Strike in: ";   //Explosion shouldn't have happened yet anyway
                else
                    baseText = "Missed strike: ";  //Explosion should have happened already but hasn't
            }
            
            if (baseText != null)
            {
                totalSeconds = Math.abs(totalSeconds);
                int minutes = totalSeconds/60;
                int seconds = totalSeconds - minutes*60;
                
                String text = "";
                if (minutes > 0)
                    text = baseText + minutes + " min, " + seconds + "sec";
                else
                    text = baseText + seconds + "sec";
                
                Font oldFont = jg.getFont();
                jg.setFont (oldFont.deriveFont((float)m_CountdownTimerFontSize));
                jg.setColor(m_mouseHoverOverlayBackgroundColor);
                fm = jg.getFontMetrics();
                int bgWidth = fm.stringWidth(text) + 20;
//                jg.fillRoundRect(this.getWidth()-300, this.getHeight() - 40, 290, 30, 20, 20);
                jg.fillRoundRect(this.getWidth() - (bgWidth + 10), this.getHeight() - 40, bgWidth, 30, 20, 20);
                jg.setColor(Color.WHITE);

                jg.drawString(text, this.getWidth() - bgWidth, this.getHeight() - 15);
                jg.setFont (oldFont);
            }
            
        }
        
        if (m_ShowWindCompassOpt)
        {
            METBelief currMetBelief = (METBelief)beliefManager.get(METBelief.BELIEF_NAME);
            if (currMetBelief != null)
            {
                METTimeName metTN = currMetBelief.getMETTimeName(WACSAgent.AGENTNAME);
                if (metTN != null)
                {
                    int bufferSize = 10;
                    int totalSize = 100;
                    int radiusSize = (int)(totalSize /2 - bufferSize*1.5);
                    int centerX = this.getWidth() - bufferSize - totalSize/2;
                    int centerY = bufferSize + totalSize/2;
                    NavyAngle windBearingTo = metTN.getWindBearing();
                    Speed windSpeed = metTN.getWindSpeed();

                    jg.setColor(m_mouseHoverOverlayBackgroundColor);
                    jg.fillRoundRect(this.getWidth()-totalSize-bufferSize, bufferSize, totalSize, totalSize, bufferSize, bufferSize);
                    jg.setColor(Color.GREEN);

                    Stroke oldStroke = jg.getStroke();
                    jg.setStroke(new BasicStroke (2));
                    jg.drawArc(centerX-radiusSize, centerY-radiusSize, radiusSize*2, radiusSize*2, 0, 360);
                    jg.drawString ("N", centerX, centerY-radiusSize);

                    jg.setStroke(new BasicStroke (4));
                    int xTailPixels = -(int)((radiusSize-bufferSize)*Math.sin(windBearingTo.getDoubleValue(Angle.RADIANS)));
                    int yTailPixels = (int)((radiusSize-bufferSize)*Math.cos(windBearingTo.getDoubleValue(Angle.RADIANS)));
                    jg.drawLine(centerX, centerY, centerX + xTailPixels, centerY + yTailPixels);

                    //Arrow heads
                    int xheadPixels = -(int)((bufferSize)*Math.sin(windBearingTo.plus(new Angle (45, Angle.DEGREES)).getDoubleValue(Angle.RADIANS)));
                    int yheadPixels = (int)((bufferSize)*Math.cos(windBearingTo.plus(new Angle (45, Angle.DEGREES)).getDoubleValue(Angle.RADIANS)));
                    jg.drawLine(centerX, centerY, centerX + xheadPixels, centerY + yheadPixels);
                    xheadPixels = -(int)((bufferSize)*Math.sin(windBearingTo.minus(new Angle (45, Angle.DEGREES)).getDoubleValue(Angle.RADIANS)));
                    yheadPixels = (int)((bufferSize)*Math.cos(windBearingTo.minus(new Angle (45, Angle.DEGREES)).getDoubleValue(Angle.RADIANS)));
                    jg.drawLine(centerX, centerY, centerX + xheadPixels, centerY + yheadPixels);

                    String speedText = "";
                    int speedUnits = DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.WINDSPEED_UNITS);
                    if (speedUnits == DisplayUnitsManager.SPEED_KNOTS)
                        speedText = m_DecFormat1.format(windSpeed.getDoubleValue(Speed.KNOTS)) + " kts";
                    else if (speedUnits == DisplayUnitsManager.SPEED_METERSPERSEC)
                        speedText = m_DecFormat1.format(windSpeed.getDoubleValue(Speed.METERS_PER_SECOND)) + " mps";
                    else //if (speedUnits == DisplayUnitsManager.SPEED_MILESPERHOUR)
                        speedText = m_DecFormat1.format((windSpeed.getDoubleValue(Speed.FEET_PER_SECOND)*3600.0/5280)) + " mph";

                    double windBearingToDeg = windBearingTo.getDoubleValue(Angle.DEGREES);
                    if (windBearingToDeg > (90+m_WindTextAlignTransientDeg) && windBearingToDeg < (270-m_WindTextAlignTransientDeg))
                    {
                        //Tend to paint speed at the bottom
                        jg.drawString(speedText, centerX - bufferSize*2, centerY + bufferSize*2);
                        m_WindTextAlignTransientDeg = -10;
                    }
                    else
                    {
                        //Tend to paint speed at the top
                        jg.drawString(speedText, centerX - bufferSize*2, centerY - bufferSize);
                        m_WindTextAlignTransientDeg = 10;
                    }

                    jg.drawString("To: " + (int)windBearingToDeg + " deg", this.getWidth()-totalSize, totalSize + bufferSize - 1);
                    jg.setStroke (oldStroke);
                    //upper right corner - current wind estimate compass
                }
            }
        }
        
        if (rulerButton.isSelected())
            super.paintRuler(jg);
    }
    
    private String getEtdText() {
        //m_MouseGroundLat, m_MouseGroundLon
        AbsolutePosition mousePosition = new LatLonAltPosition(m_MouseGroundLat, m_MouseGroundLon, new Altitude(0.0, Length.FEET));
        //Length mouseRadius = new Length(10, Length.METERS);
        
        for(int iEtd=0; iEtd<_etdDetections.size(); iEtd++) {
            EtdDetection etd = _etdDetections.get(iEtd);
            Float concentration = etd.getConcentration();
            AbsolutePosition pos = etd.getPosition();

            Length temp = pos.getRangeFrom(mousePosition).abs();
            if(pos.getRangeFrom(mousePosition).abs().isLessThan(_etdHoverRange, Length.METERS)) {
                return "Concentration: " + concentration.toString();
            }
        }    
        
        return null;
    }

    /**
     *
     *Override to take advantage of the map images (don't clear images)
     *
     */
    public void jgeoUpdateComponent(JGeoGraphics graphics) {
        jgeoPaintComponent(graphics);
    }

    /**
     * Redraw the level from scratch if the view changes or if the map components
     * have been "updated" i.e., toggled off and on
     *
     * @param jg The graphics object to redraw our map to
     */
    @SuppressWarnings("empty-statement")
    public void jgeoPaintComponent(JGeoGraphics jg)
    {
        long start = System.currentTimeMillis();

        if (_display != null)
        {
             int h = _display._MainSplitPane.getHeight();

             _display._MainSplitPane.setDividerLocation(mainSplitPixels);
             _display._SplitPane.setDividerLocation(splitPixels);
        }

        try {

            if (_drawOnlyOverlay) 
            {
                doOverlays (jg);

                _drawOnlyOverlay = false;
                return;
            }

            boolean redrawMapLevel = jg.viewChanged();
            if (redrawMapLevel || m_PaintImmediately)
            {
                m_PaintImmediately = false;
                jg.setLayer(MAPS_LEVEL);
                jg.clear();
                jg.setColor(Color.WHITE);

                if (m_UseImageryFetcher)
                {
                    super.paintCanvas(jg, true);
                    if (m_ShowScaleOpt)
                        super.paintScale (jg);

                    jg.setColor(Color.LIGHT_GRAY);
                }
                else
                {
                    Iterator i = images.iterator();
                    while (i.hasNext())
                    {
                        ImageData data = (ImageData) i.next();
                        jg.drawImage(data.getPosition(), data.getWidth(), data.getHeight(), data.getImage());
                    }

                    jg.setColor(Color.LIGHT_GRAY);
                }
            }

            if (beliefManager == null) {
                return;
            }

            
            if(_WACSControlPanel != null)
            {
                _WACSPanel.updateLabels();
                _WACSControlPanel.updateLabels();
            }
            
            //Logger.getLogger("GLOBAL").info("clock map: " + (System.currentTimeMillis() - start));


            jg.setLayer(POSITION_LEVEL);
            jg.clear(); 
            
            // SEARCH BELIEF
            if (redrawSearchBelief || redrawMapLevel)
            {
               
                try
                {
                    
                    
            
                    CloudPredictionBelief cloudPredictionBelief = (CloudPredictionBelief) beliefManager.get(CloudPredictionBelief.BELIEF_NAME);

                    if (cloudPredictionBelief != null)
                    {
                        Color oldColor = jg.getColor();
                        
                        LinkedList<LatLonAltPosition> llaList = cloudPredictionBelief.getPredictedRandomLocations();
                        if (llaList != null)
                            paintGradientCircles (jg, llaList, new Color (0.0f, 1.0f, 1.0f, 0.1f), m_CircleRadius.times(5));
                        llaList = cloudPredictionBelief.getPredictedHighestLocations();
                        if (llaList != null)
                            paintGradientCircles (jg, llaList, new Color (0.0f, 1.0f, 1.0f, 0.6f), m_CircleRadius);
                        
                        if (cloudPredictionBelief.getPredictedCloudInterceptPositionMSL() != null)
                        {
                            jg.setColor(Color.YELLOW);
                            jg.fillOval(cloudPredictionBelief.getPredictedCloudInterceptPositionMSL(), new Length(40, Length.METERS), new Length(40, Length.METERS));
                        }

                        if (cloudPredictionBelief.getCloudPredictedCenterPositionMSL() != null)
                        {
                            jg.setColor(Color.RED);
                            jg.fillOval(cloudPredictionBelief.getCloudPredictedCenterPositionMSL(), new Length(40, Length.METERS), new Length(40, Length.METERS));
                        }

                        jg.setColor(oldColor);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


//                //Logger.getLogger("GLOBAL").info("clock sb: " + (System.currentTimeMillis() - start));
//                CloudBeliefMatrix cbm =
//                        (CloudBeliefMatrix) beliefManager.get(CloudBeliefMatrix.BELIEF_NAME);
//                if (cbm != null)
//                {
//                    fillMatrix(cbm.getStateSpace(), jg, cbm.getColormatrix());
//                }

//                Logger.getLogger("GLOBAL").info("clock cloud: " + (System.currentTimeMillis() - start));
                SearchGoalBelief goal = (SearchGoalBelief) beliefManager.get(SearchGoalBelief.BELIEF_NAME);
//
                if (goal != null && lockButton.isSelected())
                {
                    resetGoalMatrix = goal.getStateSpace();
                }

                redrawSearchBelief = false;
            }


            Region drawingRegion = getDrawingRegion();
            if (drawingRegion != null) {
                jg.setColor(Color.WHITE);
                jg.setStroke(new BasicStroke(4));
                jg.drawRegion(drawingRegion);
            }

            // TARGET
            if (redrawTargetBelief || redrawMapLevel)
            {
                jg.setColor(Color.RED.brighter());
                
                TargetActualBelief targetBel = (TargetActualBelief) beliefManager.get(TargetActualBelief.BELIEF_NAME);
                if (targetBel != null)
                {
                    synchronized (targetBel)
                    {
                        Iterator itr = targetBel.getAll().iterator();
                        while (itr.hasNext())
                        {
                            PositionTimeName ptn = (PositionTimeName) itr.next();
                            LatLonAltPosition p = ptn.getPosition().asLatLonAltPosition();
                            jg.setColor(Color.WHITE);
                            
                            
                            String name = ptn.getName();
                            if (m_ShowNamesOpt && name != null)
                            {
                                jg.drawString(" " + name, p);
                            }
                            
                            jg.fillOval(p, new Length(50, Length.METERS), new Length(50, Length.METERS));
                        }
                    }
                }
                TargetCommandedBelief targetCommBel = (TargetCommandedBelief) beliefManager.get(TargetCommandedBelief.BELIEF_NAME);
                if (targetCommBel != null)
                {
                    synchronized (targetCommBel)
                    {
                        Iterator itr = targetCommBel.getAll().iterator();
                        while (itr.hasNext())
                        {
                            PositionTimeName ptn = (PositionTimeName) itr.next();
                            LatLonAltPosition p = ptn.getPosition().asLatLonAltPosition();
                            
                            drawTargetCrosshairs (jg, p);
                        }
                    }
                }
                
                redrawTargetBelief = false;
            }

            if (redrawSafetyBox)
            {
                if (m_ShowSafetyBoxOpt)
                {
                    synchronized (_safetyBoxLock)
                    {
                        if (_proposedSafetyBoxNWCorner != null)
                        {
                            Color oldColor = jg.getColor();
                            Stroke oldStroke = jg.getStroke();
                            jg.setStroke(new BasicStroke(5));
                            jg.setColor(new Color(1.0f, 0.7f, 0.1f));
                            jg.drawRect(_proposedSafetyBoxNWCorner, _proposedSafetyBoxLongitudinalLength, _proposedSafetyBoxLatitudinalLength);
                            jg.setColor(oldColor);
                            jg.setStroke(oldStroke);
                        }
                    }

                    synchronized (_safetyBoxLock)
                    {
                        if (_safetyBoxNWCorner != null)
                        {
                            Color oldColor = jg.getColor();
                            Stroke oldStroke = jg.getStroke();
                            jg.setStroke(new BasicStroke(5));
                            jg.setColor(new Color(0.8f, 0.0f, 0.0f));
                            jg.drawRect(_safetyBoxNWCorner, _safetyBoxLongitudinalLength, _safetyBoxLatitudinalLength);
                            jg.setColor(oldColor);
                            jg.setStroke(oldStroke);

                        }
                    }
                }

                redrawSafetyBox = false;
            }

            //Logger.getLogger("GLOBAL").info("clock target: " + (System.currentTimeMillis() - start));
            Iterator i = null;
            if (m_ShowHistoryOpt)
            {
                //get each history out of the hashmap
                i = agentTracker.getAllAgents();
                while (i.hasNext()) {
                    Collection positions = agentTracker.getPositionHistory((String) i.next());
                    Iterator posItr = positions.iterator();
                    AbsolutePosition[] arr = new AbsolutePosition[positions.size()];
                    int counter = 0;
                    while (posItr.hasNext()) {
                        Object o = posItr.next();
                        arr[counter++] = ((PositionTimeName) o).getPosition();
                    }

                    //create a polyline
                    jg.setColor(Color.yellow);
                    jg.drawPolyline(arr);
                }
            }

            //Commaded Racetrack Orbit
            try
            {
                AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
                if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                {
                    RacetrackOrbitBelief rtBel = null;
                    if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                    {
                        TestCircularOrbitBelief tobBel = (TestCircularOrbitBelief) beliefManager.get(TestCircularOrbitBelief.BELIEF_NAME);
                        if (tobBel != null && tobBel.isRacetrack())
                        {
                            rtBel = tobBel.asRacetrackOrbitBelief();
                        }
                    }
                    
                    if (rtBel == null)
                        rtBel = (RacetrackOrbitBelief) beliefManager.get(RacetrackOrbitBelief.BELIEF_NAME);
                    
                     Ellipse e;
                     if ((rtBel != null && rtBel.getLatitude1() != null) && rtBel.getTimeStamp().getTime() > _safeRacetrackOrbitBeliefTimestamp)
                     {
                         _safeRacetrackOrbitBeliefTimestamp = rtBel.getTimeStamp().getTime();

                         //Shouldn't check safety box, we should display an unsafe orbit so we know about it!  -JcH
                         //_safeCircularOrbitBelief = _safetyBox.getSafeCircularOrbit(cirBel);
                         _safeRacetrackOrbitBelief = rtBel;

                         initializeNewRacetrackOrbit ();
                     }

                     if (_safeRacetrackOrbitBelief != null)
                     {
                        Stroke old = jg.getStroke();
                        BasicStroke fat = new BasicStroke(4);
                        Color oldColor = jg.getColor();
                        jg.setStroke(fat);
                        jg.setColor(new Color(0.0f, 1.0f, 0.2f, 0.5f));

                        if (m_RacetrackOrbitStartPosition.equals(m_RacetrackOrbitEndPosition))
                        {
                            //if circular racetrack, draw full circle and center dot
                            jg.drawArc(m_RacetrackOrbitStartPosition, m_RacetrackOrbitDiameter, m_RacetrackOrbitDiameter, m_RacetrackNormalAngleLeft, Angle.FULL_CIRCLE);
                            jg.drawLine (m_RacetrackOrbitStartPosition, m_RacetrackOrbitEndPosition);
                        }
                        else
                        {
                            //if full racetrack, draw two half circles with lines connecting racetrack
                            jg.drawArc(m_RacetrackOrbitStartPosition, m_RacetrackOrbitDiameter, m_RacetrackOrbitDiameter, m_RacetrackNormalAngleLeft, Angle.HALF_CIRCLE);
                            jg.drawArc(m_RacetrackOrbitEndPosition, m_RacetrackOrbitDiameter, m_RacetrackOrbitDiameter, m_RacetrackNormalAngleRight, Angle.HALF_CIRCLE);

                            jg.drawLine (m_RacetrackOrbitNormalPosLeftStart,
                                        m_RacetrackOrbitNormalPosLeftEnd);
                            jg.drawLine (m_RacetrackOrbitNormalPosRightStart,
                                        m_RacetrackOrbitNormalPosRightEnd);
                            jg.drawLine (m_RacetrackOrbitStartPosition, m_RacetrackOrbitEndPosition);
                        }


                        Angle tailAngle;
                        if (_safeRacetrackOrbitBelief.getIsClockwise())
                            tailAngle = new Angle (120, Angle.DEGREES);
                        else
                            tailAngle = new Angle (60, Angle.DEGREES);
                        m_RacetrackDirectionTickIdx = updateRotationDirectionTickIdx (m_RacetrackDirectionTickIdx, m_RacetrackTickPositions, _safeRacetrackOrbitBelief.getIsClockwise());
                        drawRotationTickMarks (jg, m_RacetrackDirectionTickIdx, m_RacetrackDirectionTickCount, m_RacetrackDirectionTickInterval, m_RacetrackTickPositions, m_RacetrackTickBearings, tailAngle);
                        
                        //approach path points, if relevant
                        if (m_RacetrackOrbitStartPosition.equals(m_RacetrackOrbitEndPosition))
                        {
                            LoiterApproachPathBelief points = (LoiterApproachPathBelief)beliefManager.get (LoiterApproachPathBelief.BELIEF_NAME);
                            if (points != null)
                            {
                                Color pathColor = jg.getColor ();
                                //Length firstRangeDistanceM = new Length (Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.FirstRangeDistanceFromTarget.Meters", 2500), Length.METERS);
                                if (!points.getIsPathValid())
                                {
                                    //Invalid approach path, highlight red
                                    jg.setColor(new Color(1.0f, 0.0f, 0.2f, 0.5f));
                                }

                                if (points.getSafePosition() != null)
                                    jg.fillOval(points.getSafePosition(), m_ApproachPathDotsSize, m_ApproachPathDotsSize);
                                if (points.getContactPosition() != null)
                                    jg.fillOval(points.getContactPosition(), m_ApproachPathDotsSize, m_ApproachPathDotsSize);
                                if (points.getFirstRangePosition() != null)
                                    jg.fillOval(points.getFirstRangePosition(), m_ApproachPathDotsSize, m_ApproachPathDotsSize);

                                jg.setColor(pathColor);
                            }
                        }

                        jg.setColor(oldColor);
                        jg.setStroke(old);
                     }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }


            if (m_ShowNextOrbitOpt)
            {
                // Commanded Circular Orbit
                try
                {
                    AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
                    if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
                    {
                        CircularOrbitBelief cirBel = null;
                        if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                        {
                            TestCircularOrbitBelief tobBel = (TestCircularOrbitBelief) beliefManager.get(TestCircularOrbitBelief.BELIEF_NAME);
                            if (tobBel != null && !tobBel.isRacetrack())
                            {
                                cirBel = tobBel.asCircularOrbitBelief();
                            }
                        }
                        if (cirBel == null)
                            cirBel = (CircularOrbitBelief) beliefManager.get(CircularOrbitBelief.BELIEF_NAME);



                         Ellipse e;
                         if ((cirBel != null && cirBel.getPosition() != null) && cirBel.getTimeStamp().getTime() > _safeCircularOrbitBeliefTimestamp)
                         {
                             _safeCircularOrbitBeliefTimestamp = cirBel.getTimeStamp().getTime();

                             //Shouldn't check safety box, we should display an unsafe orbit so we know about it!  -JcH
                            //_safeCircularOrbitBelief = _safetyBox.getSafeCircularOrbit(cirBel);
                             _safeCircularOrbitBelief = cirBel;

                             initializeNewCircularOrbit ();
                         }

                         if (_safeCircularOrbitBelief != null)
                         {
                            Length l = _safeCircularOrbitBelief.getRadius().times(2);
                            e = new Ellipse(l, l, _safeCircularOrbitBelief.getPosition(), NavyAngle.NORTH);
                            Stroke old = jg.getStroke();
                            BasicStroke fat = new BasicStroke(4);
                            Color oldColor = jg.getColor();
                            jg.setStroke(fat);
                            //jg.setColor(new Color(0.0f, 1.0f, 0.2f, 0.5f));
                            jg.setColor(new Color(0.0f, 1.0f, 1.0f, 0.6f));
                            jg.drawEllipse(e);


                            Angle tailAngle;
                            if (_safeCircularOrbitBelief.getIsClockwise())
                                tailAngle = new Angle (120, Angle.DEGREES);
                            else
                                tailAngle = new Angle (60, Angle.DEGREES);
                            m_CircularOrbitDirectionTickIdx = updateRotationDirectionTickIdx (m_CircularOrbitDirectionTickIdx, m_CircularOrbitTickPositions, _safeCircularOrbitBelief.getIsClockwise());
                            drawRotationTickMarks (jg, m_CircularOrbitDirectionTickIdx, m_CircularOrbitDirectionTickCount, m_CircularOrbitDirectionTickInterval, m_CircularOrbitTickPositions, m_CircularOrbitTickBearings, tailAngle);

                            //Rotating arrows for orbit direction.  Not optimizized, just enough to work for now.
                            /*boolean clockwise = _safeCircularOrbitBelief.getIsClockwise();
                            double rotationInc = 2;
                            if (clockwise)
                                currRotationDeg += rotationInc;
                            else
                                 currRotationDeg -= rotationInc;
                            NavyAngle ne = NavyAngle.NORTHEAST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle nw = NavyAngle.NORTHWEST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle se = NavyAngle.SOUTHEAST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle sw = NavyAngle.SOUTHWEST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle west = NavyAngle.WEST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle east = NavyAngle.EAST.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle south = NavyAngle.SOUTH.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            NavyAngle north = NavyAngle.NORTH.plus(new Angle (currRotationDeg, Angle.DEGREES));
                            AbsolutePosition outArcW = null;
                            AbsolutePosition inArcW = null;
                            AbsolutePosition outArcE = null;
                            AbsolutePosition inArcE = null;
                            AbsolutePosition outArcN = null;
                            AbsolutePosition inArcN = null;
                            AbsolutePosition outArcS = null;
                            AbsolutePosition inArcS = null;
                            Length directionTailLength = new Length (20, Length.METERS);

                            AbsolutePosition onArcW = _safeCircularOrbitBelief.getPosition().translatedBy(new RangeBearingHeightOffset(_safeCircularOrbitBelief.getRadius(), west, new Length (0, Length.METERS)));
                            if (clockwise)
                                outArcW = onArcW.translatedBy(new RangeBearingHeightOffset(directionTailLength, sw, new Length (0, Length.METERS)));
                            else
                                outArcW = onArcW.translatedBy(new RangeBearingHeightOffset(directionTailLength, nw, new Length (0, Length.METERS)));
                            if (clockwise)
                                inArcW = onArcW.translatedBy(new RangeBearingHeightOffset(directionTailLength, se, new Length (0, Length.METERS)));
                            else
                                inArcW = onArcW.translatedBy(new RangeBearingHeightOffset(directionTailLength, ne, new Length (0, Length.METERS)));
                            jg.drawLine(onArcW, outArcW);
                            jg.drawLine(onArcW, inArcW);

                            AbsolutePosition onArcE = _safeCircularOrbitBelief.getPosition().translatedBy(new RangeBearingHeightOffset(_safeCircularOrbitBelief.getRadius(), east, new Length (0, Length.METERS)));
                            if (clockwise)
                                outArcE = onArcE.translatedBy(new RangeBearingHeightOffset(directionTailLength, ne, new Length (0, Length.METERS)));
                            else
                                outArcE = onArcE.translatedBy(new RangeBearingHeightOffset(directionTailLength, se, new Length (0, Length.METERS)));
                            if (clockwise)
                                inArcE = onArcE.translatedBy(new RangeBearingHeightOffset(directionTailLength, nw, new Length (0, Length.METERS)));
                            else
                                inArcE = onArcE.translatedBy(new RangeBearingHeightOffset(directionTailLength, sw, new Length (0, Length.METERS)));
                            jg.drawLine(onArcE, outArcE);
                            jg.drawLine(onArcE, inArcE);

                            AbsolutePosition onArcN = _safeCircularOrbitBelief.getPosition().translatedBy(new RangeBearingHeightOffset(_safeCircularOrbitBelief.getRadius(), north, new Length (0, Length.METERS)));
                            if (clockwise)
                                outArcN = onArcN.translatedBy(new RangeBearingHeightOffset(directionTailLength, nw, new Length (0, Length.METERS)));
                            else
                                outArcN = onArcN.translatedBy(new RangeBearingHeightOffset(directionTailLength, ne, new Length (0, Length.METERS)));
                            if (clockwise)
                                inArcN = onArcN.translatedBy(new RangeBearingHeightOffset(directionTailLength, sw, new Length (0, Length.METERS)));
                            else
                                inArcN = onArcN.translatedBy(new RangeBearingHeightOffset(directionTailLength, se, new Length (0, Length.METERS)));
                            jg.drawLine(onArcN, outArcN);
                            jg.drawLine(onArcN, inArcN);

                            AbsolutePosition onArcS = _safeCircularOrbitBelief.getPosition().translatedBy(new RangeBearingHeightOffset(_safeCircularOrbitBelief.getRadius(), south, new Length (0, Length.METERS)));
                            if (clockwise)
                                outArcS = onArcS.translatedBy(new RangeBearingHeightOffset(directionTailLength, se, new Length (0, Length.METERS)));
                            else
                                outArcS = onArcS.translatedBy(new RangeBearingHeightOffset(directionTailLength, sw, new Length (0, Length.METERS)));
                            if (clockwise)
                                inArcS = onArcS.translatedBy(new RangeBearingHeightOffset(directionTailLength, ne, new Length (0, Length.METERS)));
                            else
                                inArcS = onArcS.translatedBy(new RangeBearingHeightOffset(directionTailLength, nw, new Length (0, Length.METERS)));
                            jg.drawLine(onArcS, outArcS);
                            jg.drawLine(onArcS, inArcS);*/

                                                jg.setColor(oldColor);
                            jg.setStroke(old);

                         }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

//                Logger.getLogger("GLOBAL").info("clock sb: " + (System.currentTimeMillis() - start));
//                CloudBeliefMatrix cbm =
//                        (CloudBeliefMatrix) beliefManager.get(CloudBeliefMatrix.BELIEF_NAME);
//                if (cbm != null)
//                {
//                    fillMatrix(cbm.getStateSpace(), jg, cbm.getColormatrix());
//                }

            
            
        CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief) beliefManager.get(CloudDetectionBelief.BELIEF_NAME);
        /*if (cloudDetectionBelief != null)
        {
            synchronized(cloudDetectionBelief.getLock())
            {
                for (CloudDetection cloudDetection : cloudDetectionBelief.getDetections())
                {
                    if(cloudDetection.getSource()==CloudDetection.SOURCE_PARTICLE)
                          jg.setColor(new Color(0.0f, 0.0f, 1.0f, 0.5f));
                      else
                          jg.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
                     jg.fillOval(cloudDetection.getDetection(), new Length(40, Length.METERS), new Length(40, Length.METERS));

                }
            }
        }*/
        
        if (m_ShowLocationBubbleOpt && _bubblePredictor != null && _bubblePredictor.shouldShowBubble())
        {
            List<LatLonAltPosition> border = _bubblePredictor.getPredictions();
            if (border != null && border.size() > 0)
            {
                jg.setColor(new Color(1.0f, 0.0f, 0.0f, 0.4f));
                jg.fillPolygon(border.toArray(new LatLonAltPosition[1]));

                // DISPLAYS VERTICES:
//                jg.setColor(Color.ORANGE);
//                for (LatLonAltPosition lla : border)
//                {
//                    if (lla == null)
//                        jg.setColor(Color.MAGENTA);
//                    jg.fillOval(lla, new Length(20, Length.METERS), new Length(20, Length.METERS));
//                }
            }
        }

        synchronized (m_mouseHoverLock)
        {
            FontMetrics fm = jg.getFontMetrics();
            final int lineHeight_pixels = 16;
            boolean boxShowing = false;
            if (m_detectionMouseHoveredOn != null)
            {
                String[] msg = new String[6];
                msg[0] = "Plume Detection";
                msg[1] = "";
                msg[2] = "Source: " + (m_detectionMouseHoveredOn.getSource() == CloudDetection.SOURCE_CHEMICAL ? "Chemical" : "Particle");
                msg[3] = "Scaled Strength: " + m_detectionMouseHoveredOn.getScaledValue();
                msg[4] = "Raw Strength: " + m_detectionMouseHoveredOn.getRawValue();
                msg[5] = "Time: " + m_mouseHoverDateFormatter.format(new Date(m_detectionMouseHoveredOn.getTime()));
                
                showMultilineBox(jg, msg, 7, false);

//                final int boxHeight = 110;
//                final int boxWidth = 190;
//                final int textStartX = m_mouseHoverPoint.x + 7;
//                final int textStartY = m_mouseHoverPoint.y - boxHeight + 18;
//                
//                jg.setColor(m_mouseHoverOverlayBackgroundColor);
//                jg.fillRoundRect(m_mouseHoverPoint.x, m_mouseHoverPoint.y - boxHeight, boxWidth, boxHeight, 20, 20);
//                jg.setColor(m_mouseHoverOverlayForegroundColor);
//                jg.drawString((new AttributedString("Plume Detection")).getIterator(), textStartX, textStartY);
//                jg.drawString((new AttributedString("Source: " + (m_detectionMouseHoveredOn.getSource() == CloudDetection.SOURCE_CHEMICAL ? "Chemical" : "Particle"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 2);
//                jg.drawString((new AttributedString("Scaled Strength: " + m_detectionMouseHoveredOn.getScaledValue())).getIterator(), textStartX, textStartY + lineHeight_pixels * 3);
//                jg.drawString((new AttributedString("Raw Strength: " + m_detectionMouseHoveredOn.getRawValue())).getIterator(), textStartX, textStartY + lineHeight_pixels * 4);
//                jg.drawString((new AttributedString("Time: " + m_mouseHoverDateFormatter.format(new Date(m_detectionMouseHoveredOn.getTime())))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);

                boxShowing = true;
            }
            else if (m_orbitMouseHoveredOn != null)
            {
                LatLonAltPosition orbitCenterPosition = m_orbitMouseHoveredOn.getPosition().asLatLonAltPosition();
                
                String[] msg = new String[7];
                msg[0] = "Circular Orbit Belief";
                msg[1] = "";
                msg[2] = "Latitude: " + m_DecFormat6.format((orbitCenterPosition.getLatitude().getDoubleValue(Angle.DEGREES)));
                msg[3] = "Longitude: " + m_DecFormat6.format((orbitCenterPosition.getLongitude().getDoubleValue(Angle.DEGREES)));
                
                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    msg[4] = "Alt-MSL: " + m_DecFormat1.format(orbitCenterPosition.getAltitude().getDoubleValue(Length.FEET)) + "ft";
                else
                    msg[4] = "Alt-MSL: " + m_DecFormat1.format(orbitCenterPosition.getAltitude().getDoubleValue(Length.METERS)) + "m";
                
                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    msg[5] = "Radius: " + m_DecFormat1.format(m_orbitMouseHoveredOn.getRadius().getDoubleValue(Length.FEET)) + "ft";
                else
                    msg[5] = "Radius: " + m_DecFormat1.format(m_orbitMouseHoveredOn.getRadius().getDoubleValue(Length.METERS)) + "m";
                
                msg[6] = "Direction: " + (m_orbitMouseHoveredOn.getIsClockwise() ? "clockwise" : "counter-clockwise");
                
                showMultilineBox(jg, msg, 7, false);
                    
//                final int boxHeight = 125;
//                final int boxWidth = 210;
//                final int textStartX = m_mouseHoverPoint.x + 7;
//                final int textStartY = m_mouseHoverPoint.y - boxHeight + 18;
//
//                jg.setColor(m_mouseHoverOverlayBackgroundColor);
//                jg.fillRoundRect(m_mouseHoverPoint.x, m_mouseHoverPoint.y - boxHeight, boxWidth, boxHeight, 20, 20);
//                jg.setColor(m_mouseHoverOverlayForegroundColor);
//
//                jg.drawString((new AttributedString("Circular Orbit Belief")).getIterator(), textStartX, textStartY);
//                jg.drawString((new AttributedString("Latitude: " + m_DecFormat6.format((orbitCenterPosition.getLatitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 2);
//                jg.drawString((new AttributedString("Longitude: " + m_DecFormat6.format((orbitCenterPosition.getLongitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 3);
//                
//                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
//                    jg.drawString(((new AttributedString("Alt-MSL: " + m_DecFormat1.format(orbitCenterPosition.getAltitude().getDoubleValue(Length.FEET)) + "ft"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 4);
//                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
//                    jg.drawString(((new AttributedString("Alt-MSL: " + m_DecFormat1.format(orbitCenterPosition.getAltitude().getDoubleValue(Length.METERS)) + "m"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 4);
//
//                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
//                    jg.drawString(((new AttributedString("Radius: " + m_DecFormat1.format(m_orbitMouseHoveredOn.getRadius().getDoubleValue(Length.FEET)) + "ft"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);
//                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
//                    jg.drawString(((new AttributedString("Radius: " + m_DecFormat1.format(m_orbitMouseHoveredOn.getRadius().getDoubleValue(Length.METERS)) + "m"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);
//
//                jg.drawString((new AttributedString("Direction: " + (m_orbitMouseHoveredOn.getIsClockwise() ? "clockwise" : "counter-clockwise"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 6);
                
                boxShowing = true;
            }
            else if (m_racetrackMouseHoveredOn != null)
            {
//                final int boxHeight = 140;
//                final int boxWidth = 210;
//                final int textStartX = m_mouseHoverPoint.x + 7;
//                final int textStartY = m_mouseHoverPoint.y - boxHeight + 18;
                
//                jg.setColor(m_mouseHoverOverlayBackgroundColor);
//                jg.fillRoundRect(m_mouseHoverPoint.x, m_mouseHoverPoint.y - boxHeight, boxWidth, boxHeight, 20, 20);
//                jg.setColor(m_mouseHoverOverlayForegroundColor);
                LatLonAltPosition racetrackCenter = new LatLonAltPosition(m_racetrackMouseHoveredOn.getLatitude1(), m_racetrackMouseHoveredOn.getLongitude1(), m_racetrackMouseHoveredOn.getFinalAltitudeMsl());

                String[] msg = new String[8];
                msg[0] = "Racetrack Orbit Belief\n";
                msg[1] = "";
                msg[2] = "Latitude: " + m_DecFormat6.format((racetrackCenter.getLatitude().getDoubleValue(Angle.DEGREES))) + "\n";
                msg[3] = "Longitude: " + m_DecFormat6.format((racetrackCenter.getLongitude().getDoubleValue(Angle.DEGREES))) + "\n";
                
                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    msg[4] = "Final Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getFinalAltitudeMsl().getDoubleValue(Length.FEET)) + "ft\n";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    msg[4] = "Final Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getFinalAltitudeMsl().getDoubleValue(Length.METERS)) + "m\n";
                
                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    msg[5] = "Standoff Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getStandoffAltitudeMsl().getDoubleValue(Length.FEET)) + "ft\n";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    msg[5] = "Standoff Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getStandoffAltitudeMsl().getDoubleValue(Length.METERS)) + "m\n";

                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    msg[6] = "Radius: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getRadius().getDoubleValue(Length.FEET)) + "ft\n";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    msg[6] = "Radius: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getRadius().getDoubleValue(Length.METERS)) + "m\n";

                msg[7] = "Direction: " + (m_racetrackMouseHoveredOn.getIsClockwise() ? "clockwise" : "counter-clockwise") + "\n";

                showMultilineBox(jg, msg, 7, false);
                
//                jg.drawString((new AttributedString("Racetrack Orbit Belief")).getIterator(), textStartX, textStartY);
//                jg.drawString((new AttributedString("Latitude: " + m_DecFormat6.format((racetrackCenter.getLatitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 2);
//                jg.drawString((new AttributedString("Longitude: " + m_DecFormat6.format((racetrackCenter.getLongitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 3);
//                
//                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
//                    jg.drawString(((new AttributedString("Final Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getFinalAltitudeMsl().getDoubleValue(Length.FEET)) + "ft"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 4);
//                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
//                    jg.drawString(((new AttributedString("Final Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getFinalAltitudeMsl().getDoubleValue(Length.METERS)) + "m"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 4);
//
//                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
//                    jg.drawString(((new AttributedString("Standoff Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getStandoffAltitudeMsl().getDoubleValue(Length.FEET)) + "ft"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);
//                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
//                    jg.drawString(((new AttributedString("Standoff Alt-MSL: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getStandoffAltitudeMsl().getDoubleValue(Length.METERS)) + "m"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);
//
//                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
//                    jg.drawString(((new AttributedString("Radius: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getRadius().getDoubleValue(Length.FEET)) + "ft"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 6);
//                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
//                    jg.drawString(((new AttributedString("Radius: " + m_DecFormat1.format(m_racetrackMouseHoveredOn.getRadius().getDoubleValue(Length.METERS)) + "m"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 6);
//
//                jg.drawString((new AttributedString("Direction: " + (m_racetrackMouseHoveredOn.getIsClockwise() ? "clockwise" : "counter-clockwise"))).getIterator(), textStartX, textStartY + lineHeight_pixels * 7);
                
                boxShowing = true;
            }
            
            if (m_bubbleMouseHoveredOn && m_ShowLocationBubbleOpt) // NOT `else if` because we can display location text box along with others
            {
                String[] msg = new String[6];
                msg[0] = "Location Projection";
                msg[1] = "";
                msg[2] = "High Latitude: " + m_DecFormat6.format((_bubblePredictor.getMaxLatitude().getDoubleValue(Angle.DEGREES)));
                msg[3] = "Low Latitude: " + m_DecFormat6.format((_bubblePredictor.getMinLatitude().getDoubleValue(Angle.DEGREES)));
                msg[4] = "High Longitude: " + m_DecFormat6.format((_bubblePredictor.getMaxLongitude().getDoubleValue(Angle.DEGREES)));
                msg[5] = "Low Longitude: " + m_DecFormat6.format((_bubblePredictor.getMinLongitude().getDoubleValue(Angle.DEGREES)));
                
                showMultilineBox(jg, msg, 7, boxShowing);
                
//                final int boxHeight = 125;
//                final int boxWidth = 210;
//                final int yAdj;
//                if (!boxShowing)
//                    yAdj = -boxHeight;
//                else
//                    yAdj = 2;
//                final int textStartX = m_mouseHoverPoint.x + 7;
//                final int textStartY = m_mouseHoverPoint.y + yAdj + 18;
//                
//                jg.setColor(m_mouseHoverOverlayBackgroundColor);
//                jg.fillRoundRect(m_mouseHoverPoint.x, m_mouseHoverPoint.y + yAdj, boxWidth, boxHeight, 20, 20);
//                jg.setColor(m_mouseHoverOverlayForegroundColor);
//                
//                jg.drawString((new AttributedString("Location Projection")).getIterator(), textStartX, textStartY);
//                jg.drawString((new AttributedString("High Latitude: " + m_DecFormat6.format((_bubblePredictor.getMaxLatitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 2);
//                jg.drawString((new AttributedString("Low Latitude: " + m_DecFormat6.format((_bubblePredictor.getMinLatitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 3);
//                jg.drawString((new AttributedString("High Longitude: " + m_DecFormat6.format((_bubblePredictor.getMaxLongitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 5);
//                jg.drawString((new AttributedString("Low Longitude: " + m_DecFormat6.format((_bubblePredictor.getMinLongitude().getDoubleValue(Angle.DEGREES))))).getIterator(), textStartX, textStartY + lineHeight_pixels * 6);
            }
        }
//            CloudDetectionBelief cdb =(CloudDetectionBelief) beliefManager.get(CloudDetectionBelief.BELIEF_NAME);
//            Collection detections = agentTracker.getDetectionHistory(WACSAgent.AGENTNAME);
//                if (detections != null)
//                {
//                    Iterator detItr = detections.iterator();
//                    int ccc = 0;
//                    while (detItr.hasNext())
//                    {
//                        ccc++;
//
//                        Object o = detItr.next();
//                        //System.out.println("$$$ Detection " +ccc + ": " + ((CloudDetection) o).getLat_deg());
//
//                        CloudDetection cd = ((CloudDetection) o);
//                        if(cd.getSource()==1)
//                            jg.setColor(Color.blue);
//                        else
//                            jg.setColor(Color.red);
//                        jg.fillOval(cd.getDetection(), new Length(40, Length.METERS), new Length(40, Length.METERS));
//                    }
//                }


            if (m_ShowWindOpt)
            {
                //get each history out of the hashmap
                i = agentTracker.getAllAgents();
                String name;
                while (i.hasNext()) {
                    name = (String) i.next();
                    Collection wind = agentTracker.getWindHistory(name);
                    if (wind != null) {
                        Iterator posItr = wind.iterator();
                        NavyAngle[] angle = new NavyAngle[wind.size()];
                        AbsolutePosition[] arr = new AbsolutePosition[wind.size()];
                        double[] speed = new double[wind.size()];
                        double s = 0.0;
                        NavyAngle a = null;
                        AbsolutePosition p = null;
                        int counter = 0;
                        while (posItr.hasNext()) {
                            Object o = posItr.next();
                            a = angle[counter] = ((METPositionTimeName) o).getWindBearing();
                            s = speed[counter] = ((METPositionTimeName) o).getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND);
                            p = arr[counter++] = ((METPositionTimeName) o).getPosition();
                        }

                        double scale = 0.2;
                        //create small triangles for wind
                        jg.setColor(Color.RED);

                        for (int c = 0; c < wind.size(); c++) {
                            a = angle[c];
                            p = arr[c];
                            s = speed[c] + 2;
                            s = s / 10;

                            RangeBearingHeightOffset top = new RangeBearingHeightOffset(new Length(s * 50.0, Length.METERS),
                                    a,
                                    Length.ZERO);
                            RangeBearingHeightOffset right = new RangeBearingHeightOffset(new Length(scale * 35.0, Length.METERS),
                                    a.plus(Angle.RIGHT_ANGLE),
                                    Length.ZERO);
                            RangeBearingHeightOffset right2 = new RangeBearingHeightOffset(new Length(s * 50.0, Length.METERS),
                                    a.plus(Angle.HALF_CIRCLE),
                                    Length.ZERO);
                            RangeBearingHeightOffset left = new RangeBearingHeightOffset(new Length(scale * 35.0, Length.METERS),
                                    a.plus(Angle.NEGATIVE_RIGHT_ANGLE),
                                    Length.ZERO);
                            AbsolutePosition[] triangle = new AbsolutePosition[3];
                            triangle[0] = p.translatedBy(top);
                            triangle[1] = p.translatedBy(right).translatedBy(right2);
                            triangle[2] = p.translatedBy(left).translatedBy(right2);

                            Vector<AbsolutePosition> points = new Vector<AbsolutePosition>();
                            points.add(triangle[0]);
                            points.add(triangle[1]);
                            points.add(triangle[2]);
                            Region shape;
                            shape = new Region(points);
                            jg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            jg.fillRegion(shape);
                        }
                    }
                }
            }

            if (redrawPositionBelief || redrawMapLevel)
            {
                AgentPositionBelief belief = new AgentPositionBelief();
                belief = (AgentPositionBelief) beliefManager.get(belief.getName());
                AgentBearingBelief abb = (AgentBearingBelief) beliefManager.get(AgentBearingBelief.BELIEF_NAME);

                // POSITION
                jg.setColor(Color.BLACK);
                i = agentTracker.getAllAgents();
                ClassificationBelief cbb = (ClassificationBelief) beliefManager.get(ClassificationBelief.BELIEF_NAME);
                while (i.hasNext()) {
                    String name = (String) i.next();
                    /*whats it doing
                    ConfigBelief cb = (ConfigBelief) beliefManager.get(ConfigBelief.BELIEF_NAME);
                    String isDisabled = null;
                    if (cb != null)
                    {
                        cb.getProperty(name, SwarmDisabler.SWARM_DISABLED_PROPERTY);
                    }

                    if (isDisabled != null && isDisabled.equals(Boolean.toString(true))) {
                        jg.setColor(Color.RED);
                    } else {
                        jg.setColor(Color.BLACK);
                    }
                    if (name.equals("blueAgent")) {
                        jg.setColor(Color.BLUE);
                    }*/
                    
                    //Sensor?
                    boolean isSensor = false;
                    boolean isInfo = false;
                    if (cbb != null) {
                        ClassificationTimeName classTimeName = cbb.getClassificationTimeName(name);
                        if (classTimeName != null) {
                            int c = classTimeName.getClassification();
                            if (c == Classification.COT_NEUTRAL || c == Classification.NEUTRAL) {
                                jg.setColor(Color.cyan);
                            } else if (c == Classification.SENSOR_UNCOVERED || c == Classification.SENSOR_COVERED) {
                                isSensor = true;
                            } else if (c == Classification.INFO) {
                                isInfo = true;
                            } else {
                                jg.setColor(Color.orange);
                            }
                        }
                    }

                    PositionTimeName posTimeName = agentTracker.getAgentInfo(name);
                    AbsolutePosition p = posTimeName.getPosition();
                    
                    NavyAngle currHeading = NavyAngle.NORTH;
                    if (name.toLowerCase().contains("commandpost") || name.toLowerCase().equals(WACSDisplayAgent.AGENTNAME)) {
                        double scale = 1.0;
                        jg.setColor(new Color(0.0f, 0.8f, 0.8f, 0.5f));
                        //jg.setColor(Color.cyan);
                        jg.fillOval(p, new Length(100, Length.METERS), new Length(100, Length.METERS)); //this is where it draws the dot for the agent

                        RangeBearingHeightOffset textOffset = new RangeBearingHeightOffset(
                                new Length(scale * 70.0, Length.METERS),
                                NavyAngle.EAST,
                                Length.ZERO);
                        jg.setColor(Color.black);
                        jg.drawString(name, p.translatedBy(textOffset));
                    } else {
                        if (abb != null) {
                            BearingTimeName btn = abb.getBearingTimeName(name);
                            if (btn != null) {
                                currHeading = btn.getCurrentBearing();
                            } else {
                                currHeading = posTimeName.getHeading();
                            }
                        }
                        else {
                            currHeading = posTimeName.getHeading();
                        }
                        
                        Region shape;
                        double scale = 1.0;
                        if (isSensor) {
                            //Dimensions of sensor image
                            Length width = new Length(20, Length.METERS);
                            Length height = new Length(20, Length.METERS);
                            AbsolutePosition southWest = p;
                            AbsolutePosition southEast = southWest.translatedBy(new RangeBearingHeightOffset(width, NavyAngle.EAST, Length.ZERO));
                            AbsolutePosition northEast = southEast.translatedBy(new RangeBearingHeightOffset(height, NavyAngle.NORTH, Length.ZERO));
                            AbsolutePosition northWest = northEast.translatedBy(new RangeBearingHeightOffset(height, NavyAngle.WEST, Length.ZERO));
                            Vector<AbsolutePosition> points = new Vector<AbsolutePosition>();
                            points.add(northWest);
                            points.add(northEast);
                            points.add(southEast);
                            points.add(southWest);
                            shape = new Region(points);
                        } else {
                            jg.setColor(new Color(0.0f, 0.0f, 0.0f, 0.7f));

                            if (isInfo) {
                                jg.setColor(Color.cyan);
                                scale = 0.6;
                            }

                            RangeBearingHeightOffset top = new RangeBearingHeightOffset(new Length(scale * 50.0, Length.METERS),
                                    currHeading,
                                    Length.ZERO);
                            RangeBearingHeightOffset right = new RangeBearingHeightOffset(new Length(scale * 35.0, Length.METERS),
                                    currHeading.plus(Angle.RIGHT_ANGLE),
                                    Length.ZERO);
                            RangeBearingHeightOffset right2 = new RangeBearingHeightOffset(new Length(scale * 50.0, Length.METERS),
                                    currHeading.plus(Angle.HALF_CIRCLE),
                                    Length.ZERO);
                            RangeBearingHeightOffset left = new RangeBearingHeightOffset(new Length(scale * 35.0, Length.METERS),
                                    currHeading.plus(Angle.NEGATIVE_RIGHT_ANGLE),
                                    Length.ZERO);
                            AbsolutePosition[] triangle = new AbsolutePosition[3];
                            triangle[0] = p.translatedBy(top);
                            triangle[1] = p.translatedBy(right).translatedBy(right2);
                            triangle[2] = p.translatedBy(left).translatedBy(right2);

                            Vector<AbsolutePosition> points = new Vector<AbsolutePosition>();
                            points.add(triangle[0]);
                            points.add(triangle[1]);
                            points.add(triangle[2]);
                            shape = new Region(points);
                            if (!isInfo) {
                                Length l = new Length(Config.getConfig().getPropertyAsDouble("SearchCanvas.AgentCircleRadius.Meters"), Length.METERS);
                                Ellipse e = new Ellipse(l, l, p, NavyAngle.NORTH);

                                Color oldColor = jg.getColor();
                                jg.setColor(new Color(0.0f, 0.0f, 0.8f, 0.3f)); //this is where it sets the color for the circle behind the agent
                                jg.fillEllipse(e);
                                jg.setColor(oldColor);
                            }
                        }
                        jg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        jg.setColor (Color.GREEN);//this is where it sets the color for dot for the agent
                        jg.fillRegion(shape);
                        if (m_ShowNamesOpt) {

                            RangeBearingHeightOffset textOffset = new RangeBearingHeightOffset(
                                    new Length(scale * 70.0, Length.METERS),
                                    NavyAngle.EAST,
                                    Length.ZERO);
                            jg.setColor(Color.black);
                            jg.drawString(name, p.translatedBy(textOffset));
                        }


                        IrCameraFOVBelief fovBlf = (IrCameraFOVBelief)beliefManager.get(IrCameraFOVBelief.BELIEF_NAME);
                        AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
                        if(false) //if (fovBlf != null && (agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
                        {
                            jg.setColor (new Color (0.0f, 0.0f, 0.0f, 0.1f));
                            NavyAngle maxAngleFromForward = new NavyAngle (fovBlf.getMaxAngleFromForwardDeg(), Angle.DEGREES);
                            NavyAngle minAngleFromForward = new NavyAngle (fovBlf.getMinAngleFromForwardDeg(), Angle.DEGREES);

                            NavyAngle mostClockwiseVisAngle = currHeading.plus(maxAngleFromForward.asAngle());
                            NavyAngle leastClockwiseVisAngle = currHeading.plus(minAngleFromForward.asAngle());
                            LatLonAltPosition triangle[] = new LatLonAltPosition[3];
                            triangle[0] = p.asLatLonAltPosition();
                            triangle[1] = p.translatedBy(new RangeBearingHeightOffset(Length.ONE_NMI.times(100), mostClockwiseVisAngle, Length.ZERO)).asLatLonAltPosition();
                            triangle[2] = p.translatedBy(new RangeBearingHeightOffset(Length.ONE_NMI.times(100), leastClockwiseVisAngle, Length.ZERO)).asLatLonAltPosition();
                            Region reg = new Region (triangle);
                            jg.fillRegion(reg);
                        }

                        //jg.fillOval(p, new Length(3, Length.METERS),
                        //    new Length(3, Length.METERS));
                        jg.setColor(Color.blue);
                    //jg.drawOval(p, new Length(Config.getConfig().getPropertyAsDouble("simulation.commsRange",250),Length.METERS),
                    //new Length(Config.getConfig().getPropertyAsDouble("simulation.commsRange",250),Length.METERS));
                    }


                    //if the uav waypoint display setting is set draw the UAV waypoint
                    if (showUAVWaypoint.isSelected()) {
                        UAVWaypointBelief waypointBelief = (UAVWaypointBelief) beliefManager.get(UAVWaypointBelief.BELIEF_NAME);
                        //draw a circle at the waypoint
                        if (waypointBelief != null) {
                            synchronized (waypointBelief) {
                                Iterator itr = waypointBelief.getAll().iterator();
                                while (itr.hasNext()) {
                                    PositionTimeName ptn = (PositionTimeName) itr.next();
                                    if (ptn.getName().equals(posTimeName.getName())) {

                                        jg.drawLine(ptn.getPosition(), p);
                                        jg.fillOval(ptn.getPosition(), new Length(5, Length.METERS), new Length(5, Length.METERS));
                                    }
                                }
                            }
                        }

                    }
                    
                    Boolean displayETD = true;
                    if (displayETD) {
                        EtdDetectionListBelief etdDetectionListBelief = (EtdDetectionListBelief) beliefManager.get(EtdDetectionListBelief.BELIEF_NAME);
                        if(etdDetectionListBelief != null) {
                            synchronized(etdDetectionListBelief) {
                                List<EtdDetection> etdDetections = etdDetectionListBelief.getDetections();
                                if(_shouldFilter) {
                                    etdDetections = etdDetectionListBelief.getFilteredDetections();
                                }
                
                                for(int iEtd=0; iEtd<etdDetections.size(); iEtd++) {
                                    EtdDetection etdDetection = etdDetections.get(iEtd);
                                    if(etdDetection.getTime()>_etdLatestUpdateTime) {
                                        _etdDetections.add(etdDetection);
                                        _etdLatestUpdateTime = etdDetection.getTime();
                                    }
                                }
                            }
                        }
                        
                        while(_etdDetections.size()>_etdHistoryLength) {
                            _etdDetections.remove(0);
                        }
                        
                        for(int iEtd=0; iEtd<_etdDetections.size(); iEtd++) {
                            EtdDetection etd = _etdDetections.get(iEtd);
                            Float concentration = etd.getConcentration();
                            AbsolutePosition pos = etd.getPosition();
                            
                            if(iEtd==(_etdDetections.size()-1)) {
                                jg.setColor(Color.BLACK);
                                jg.fillOval(pos, _etdMarkerSize+5, _etdMarkerSize+5);
                            }

                            jg.setColor(getColorForValue(concentration));
                            jg.fillOval(pos, _etdMarkerSize, _etdMarkerSize);
                        }
                    }
                    
                }

                if (_showLines) {
                    if (_points != null && _points.size() > 1) {
                        Iterator itr = _points.iterator();
                        AbsolutePosition first = (AbsolutePosition) itr.next();
                        jg.setColor(Color.GRAY);
                        jg.setStroke(new BasicStroke(4));
                        while (itr.hasNext()) {
                            AbsolutePosition second = (AbsolutePosition) itr.next();
                            jg.drawLine(first, second);
                            first = second;
                        }
                    }
                }



                if (showLittleObstacles.isSelected()) {
                    NoGoBelief noGo = (NoGoBelief) beliefManager.get(NoGoBelief.BELIEF_NAME);

                    Length translate = new Length(
                            Config.getConfig().getPropertyAsDouble("display.translateNoGo", 0),
                            Length.METERS);
                    if (noGo != null && translate.isGreaterThan(Length.ZERO)) {
                        HashMap regs = noGo.getAllRegions();
                        jg.setColor(Color.orange);
                        jg.setStroke(new BasicStroke(4));
                        Set keys = regs.keySet();
                        Iterator itr = keys.iterator();
                        while (itr.hasNext()) {
                            Region reg = (Region) ((NoGoTimeName) regs.get((String) itr.next())).getObstacle();
                            if (reg != null) {
                                AbsolutePosition[] posArray = reg.getPositions();
                                AbsolutePosition[] newPosArray = new AbsolutePosition[posArray.length];
                                LatLonAltPosition center = reg.getCenterOfGravity();
                                int count = 0;
                                for (AbsolutePosition pos : posArray) {
                                    NavyAngle bearing = pos.getBearingTo(center);
                                    newPosArray[count++] = pos.translatedBy(new RangeBearingHeightOffset(translate, bearing, Length.ZERO));
                                }
                                Region littleRegion = new Region(newPosArray);
                                jg.drawRegion(littleRegion);
                            }
                        }
                    }
                    noGo = (DynamicNoGoBelief) beliefManager.get(DynamicNoGoBelief.BELIEF_NAME);

                    if (noGo != null) {
                        HashMap regs = noGo.getAllRegions();
                        jg.setColor(Color.magenta);
                        jg.setStroke(new BasicStroke(4));
                        Set keys = regs.keySet();
                        Iterator itr = keys.iterator();
                        while (itr.hasNext()) {
                            Region reg = (Region) ((NoGoTimeName) regs.get((String) itr.next())).getObstacle();
                            if (reg != null) {
                                jg.drawRegion(reg);
                            }
                        }
                    }
                }

                if (showRealObstacles.isSelected()) {
                    NoGoBelief noGo = (NoGoBelief) beliefManager.get(NoGoBelief.BELIEF_NAME);

                    if (noGo != null) {
                        HashMap regs = noGo.getAllRegions();
                        jg.setColor(Color.red);
                        jg.setStroke(new BasicStroke(4));
                        Set keys = regs.keySet();
                        Iterator itr = keys.iterator();
                        while (itr.hasNext()) {
                            Region reg = (Region) ((NoGoTimeName) regs.get((String) itr.next())).getObstacle();
                            if (reg != null) {
                                jg.drawRegion(reg);
                            }
                        }
                    }
                    noGo = (DynamicNoGoBelief) beliefManager.get(DynamicNoGoBelief.BELIEF_NAME);

                    if (noGo != null) {
                        HashMap regs = noGo.getAllRegions();
                        jg.setColor(Color.magenta);
                        jg.setStroke(new BasicStroke(4));
                        Set keys = regs.keySet();
                        Iterator itr = keys.iterator();
                        while (itr.hasNext()) {
                            Region reg = (Region) ((NoGoTimeName) regs.get((String) itr.next())).getObstacle();
                            if (reg != null) {
                                jg.drawRegion(reg);
                            }
                        }
                    }
                    //Draw keep out regions
                    KeepOutRegionBelief keepOut = (KeepOutRegionBelief) beliefManager.get(KeepOutRegionBelief.BELIEF_NAME);
                    
                    if(keepOut!=null && m_ShowKeepOutOpt){
                        ArrayList<PolygonRegion> regions = keepOut.getRegions();
                        jg.setColor(Color.red);
                        jg.setStroke(new BasicStroke(4));
                        for(PolygonRegion r: regions){
                            jg.drawRegion(r);
                        }
                        
                    }
                            
                }

                if (showPath.isSelected()) {
                    PathBelief path = (PathBelief) beliefManager.get(PathBelief.BELIEF_NAME);

                    if (path != null) {
                        Collection paths = path.getAllPaths();
                        Iterator itr = paths.iterator();
                        while (itr.hasNext()) {
                            java.util.List<AbsolutePosition> pathList =
                                    (java.util.List<AbsolutePosition>) ((PathTimeName) itr.next()).getPath();
                            AbsolutePosition[] positions = new AbsolutePosition[pathList.size()];
                            int counter = 0;
                            for (AbsolutePosition p : pathList) {
                                positions[counter++] = p;
                            }
                            jg.setColor(Color.blue);
                            jg.setStroke(new BasicStroke(4));
                            jg.drawPolyline(positions);
                        }
                    }
                }

                if (showSNREdges.isSelected())
                {
                    SNRBelief snrb = (SNRBelief) beliefManager.get(SNRBelief.BELIEF_NAME);

                    if (snrb != null) {
                        jg.setStroke(new BasicStroke(3));
                        jg.setColor(Color.RED);
                        Set<SNR> c = snrb.getKeys();
                        for (SNR snr : c) {
                            try {
                                PositionTimeName ptn = belief.getPositionTimeName(snr._agent1);
                                double snrVal = snr._snr;
                                AbsolutePosition pos1 = ptn.getPosition();
                                ptn = belief.getPositionTimeName(snr._agent2);
                                AbsolutePosition pos2 = ptn.getPosition();
                                jg.drawLine(pos1, pos2);

                                RangeBearingHeightOffset offset = pos1.getRangeBearingHeightOffsetTo(pos2);
                                offset = new RangeBearingHeightOffset(offset.getRange().dividedBy(3),
                                        offset.getBearing(), offset.getHeight());
                                AbsolutePosition midPos = pos1.translatedBy(offset);
                                double percent = snr._snr / SNRBelief.SNR_MAX;
                                percent = (percent > 1.0) ? 1.0 : percent;
                                int red = (percent < 0.5 ? 255 : (int) (255 * percent));
                                int green = (percent > 0.5 ? 255 : (int) (255 * percent));
                                jg.setStroke(new BasicStroke(2));
                                jg.setColor(new Color(red, green, 0));
                                jg.drawString("" + (int) snr._snr, midPos);
                            } catch (NullPointerException e) {
                            }
                        }
                    }
                }

                redrawPositionBelief = false;
            }

            //Logger.getLogger("GLOBAL").info("clock pos: " + (System.currentTimeMillis() - start));

            long timestamp = System.currentTimeMillis();

            // BEARING
            if (m_ShowBearingOpt && (redrawBearingBelief || redrawMapLevel)) {
                
                AgentBearingBelief bearingBelief =
                        (AgentBearingBelief) beliefManager.get(
                        AgentBearingBelief.BELIEF_NAME);

                if (bearingBelief != null) {
                    synchronized (bearingBelief) {
                        Collection c = bearingBelief.getAll();
                        i = c.iterator();
                        while (i.hasNext()) {
                            BearingTimeName bearingTimeName = (BearingTimeName) i.next();
                            String name = bearingTimeName.getName();
                            NavyAngle currentBearing = bearingTimeName.getCurrentBearing();
                            NavyAngle desiredBearing = bearingTimeName.getDesiredBearing();

                            PositionTimeName ptn = agentTracker.getAgentInfo(name);
                            if (ptn != null) {
                                AbsolutePosition p = ptn.getPosition();
                                //draw the desired and current bearing at this position
                                jg.setStroke(new BasicStroke(4));
                                jg.setColor(Color.RED);
                                jg.drawLine(p, currentBearing, new Length(6, Length.METERS));
                                jg.setColor(Color.CYAN);
                                jg.drawLine(p, desiredBearing, new Length(4, Length.METERS));
                            }
                        }
                    }
                }
                redrawBearingBelief = false;
            } else if (!m_ShowBearingOpt && redrawBearingBelief) {
                
            }

            //Logger.getLogger("GLOBAL").info("clock bearing: " + (System.currentTimeMillis() - start));

            // Redraws the simulated cloud
            if (cloudPointAPosition != null && cloudPointBPosition != null) {
                Length distance = cloudPointAPosition.getRangeTo(cloudPointBPosition);
                distance = distance.plus(CLOUD_ELLIPSE_DIST);

                LatLonAltPosition aPos = new LatLonAltPosition(cloudPointAPosition.asLatLonAltPosition().getLatitude(),
                        cloudPointAPosition.asLatLonAltPosition().getLongitude(),
                        Altitude.ZERO);
                LatLonAltPosition bPos = new LatLonAltPosition(cloudPointBPosition.asLatLonAltPosition().getLatitude(),
                        cloudPointBPosition.asLatLonAltPosition().getLongitude(),
                        Altitude.ZERO);
                Ellipse e = new Ellipse(aPos, bPos, distance);

                jg.setColor(new Color(0.4f, 0f, 0f, 0.4f));
                jg.fillEllipse(e);
            } else {
                if (m_ShowCloudOpt) {
                    CloudBelief cb = (CloudBelief) beliefManager.get(CloudBelief.BELIEF_NAME);
                    if (cb != null) {
                        jg.setColor(new Color(0.4f, 0f, 0f, 0.4f));
                        jg.fillEllipse(cb.getEllipse());
                    }
                }
            }


            doOverlays(jg);

        //Logger.getLogger("GLOBAL").info("clock cloud: " + (System.currentTimeMillis() - start));


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //this code shows the center of the quadrant that the agents are searching for
/* 
        if(SearchBehavior.ovals.size() > 0) {
        jg.setColor(Color.RED);
        Iterator it = SearchBehavior.ovals.iterator();
        while(it.hasNext()) {
        AbsolutePosition pos = (AbsolutePosition)it.next();
        jg.fillOval(pos,10,10);
        }
        }
         */

        //Logger.getLogger("GLOBAL").info("clock all: " + (System.currentTimeMillis() - start));
    }

    private void showMultilineBox(JGeoGraphics jg, String[] msg, int xPad, boolean below)
    {
        Font oldFont = jg.getFont();
        jg.setFont(oldFont.deriveFont(12.0f));
        FontMetrics fm = jg.getFontMetrics();
        
        int radius = 10;
        
        int xStart = m_mouseHoverPoint.x;
        int yStart;
        if (!below)
            yStart = m_mouseHoverPoint.y - fm.getHeight() * msg.length - radius * 2;
        else
            yStart = m_mouseHoverPoint.y + 2;

        int bWidth = 0;
        for (String line : msg)
            if (fm.stringWidth(line) > bWidth)
                bWidth = fm.stringWidth(line);

        jg.setColor(m_mouseHoverOverlayBackgroundColor);
        jg.fillRoundRect(xStart, yStart, bWidth + xPad * 2, msg.length * fm.getHeight() + radius * 2, radius * 2, radius * 2);
        jg.setColor(m_mouseHoverOverlayForegroundColor);

        for (int j = 0; j < msg.length; j++)
        {
            jg.drawString(msg[j], xStart + xPad, yStart + fm.getHeight() * (j + 1) + (radius - 3));
        }
        
        jg.setFont(oldFont);
    }


    /*static BufferedWriter writer ;
    static
    {

        try
        {
            File file = new File ("points.csv");
            file.createNewFile();
            writer = new BufferedWriter (new FileWriter (file));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }*/
    
    private void initializeNewRacetrackOrbit ()
    {
        LatLonAltPosition position1 = new LatLonAltPosition(_safeRacetrackOrbitBelief.getLatitude1(), _safeRacetrackOrbitBelief.getLongitude1(), _safeRacetrackOrbitBelief.getFinalAltitudeMsl());
        Length radius = _safeRacetrackOrbitBelief.getRadius();

        NavyAngle bearingTo = position1.getBearingTo(position1);
        NavyAngle bearingLeft = bearingTo.increaseToPort(Angle.RIGHT_ANGLE);
        NavyAngle bearingRight = bearingTo.increaseToStarboard(Angle.RIGHT_ANGLE);
        Angle aLeft = Angle.RIGHT_ANGLE.minus(bearingLeft.asAngle());
        Angle aRight = aLeft.plus (Angle.HALF_CIRCLE);
        
        m_RacetrackOrbitStartPosition = position1;
        m_RacetrackOrbitDiameter = radius.times(2);
        m_RacetrackNormalAngleLeft = aLeft;
        m_RacetrackOrbitEndPosition = position1;
        m_RacetrackNormalAngleRight = aRight;
        m_RacetrackOrbitNormalPosLeftStart = position1.translatedBy(new RangeBearingHeightOffset(radius, bearingLeft, new Length (0, Length.METERS))).asLatLonAltPosition();
        m_RacetrackOrbitNormalPosLeftEnd = position1.translatedBy(new RangeBearingHeightOffset(radius, bearingLeft, new Length (0, Length.METERS))).asLatLonAltPosition();
        m_RacetrackOrbitNormalPosRightStart = position1.translatedBy(new RangeBearingHeightOffset(radius, bearingRight, new Length (0, Length.METERS))).asLatLonAltPosition();
        m_RacetrackOrbitNormalPosRightEnd = position1.translatedBy(new RangeBearingHeightOffset(radius, bearingRight, new Length (0, Length.METERS))).asLatLonAltPosition();

        Length totalRacetrackLength = m_RacetrackOrbitStartPosition.getRangeTo(m_RacetrackOrbitEndPosition).times(2).plus(m_RacetrackOrbitDiameter.times(Math.PI));
        int desiredArrowTicks = Config.getConfig().getPropertyAsInteger("SearchCanvas.RaceTrackOrbit.TotalArrowTicks", 100);
        if (desiredArrowTicks > 0)
        {
            Length distanceBetweenTicks = totalRacetrackLength.dividedBy(desiredArrowTicks);

            m_RacetrackTickPositions = new LatLonAltPosition[desiredArrowTicks];
            m_RacetrackTickBearings = new NavyAngle[desiredArrowTicks];
            m_RacetrackDirectionTickInterval = m_RacetrackTickPositions.length/m_RacetrackDirectionTickCount;
            int tickIdx = 0;

            Length straightLegLength = m_RacetrackOrbitStartPosition.getRangeTo(m_RacetrackOrbitEndPosition);
            Length itrLength = Length.ZERO;
            //Loop along straight edge
            while (itrLength.isLessThan(straightLegLength))
            {
                m_RacetrackTickPositions[tickIdx] = m_RacetrackOrbitNormalPosLeftStart.translatedBy(new RangeBearingHeightOffset(itrLength, bearingTo, Length.ZERO)).asLatLonAltPosition();
                m_RacetrackTickBearings[tickIdx++] = bearingTo;
                itrLength = itrLength.plus(distanceBetweenTicks);
            }

            //Difference between itrLength and straightLegLength is how far around semi-circle to start
            Angle circleStartAngleOffset = new Angle (itrLength.minus(straightLegLength).dividedBy(radius).getDoubleValue(), Angle.RADIANS);
            Angle circleStartAngle = Angle.RIGHT_ANGLE.minus(aLeft);
            Angle itrAngleCheck = circleStartAngleOffset;
            //Loop around circle
            while (itrAngleCheck.isLessThan(Angle.HALF_CIRCLE))
            {
                m_RacetrackTickPositions[tickIdx] = m_RacetrackOrbitEndPosition.translatedBy(new RangeBearingHeightOffset(radius, circleStartAngle.plus(itrAngleCheck).plus(NavyAngle.ZERO), Length.ZERO)).asLatLonAltPosition();
                m_RacetrackTickBearings[tickIdx++] = bearingTo.plus (circleStartAngle.plus(itrAngleCheck)).plus(Angle.RIGHT_ANGLE);
                Angle angleInc = new Angle (distanceBetweenTicks.dividedBy(radius).getDoubleValue(), Angle.RADIANS);
                itrAngleCheck = itrAngleCheck.plus(angleInc);
            }

            //Length along arc of itrAngle beyond Angle.HALF_CIRCLE is how far along second straight part to start
            Length straightStartOffset = radius.times(itrAngleCheck.minus(Angle.HALF_CIRCLE).getDoubleValue(Angle.RADIANS));
            itrLength = straightStartOffset;
            //Loop along straight edge
            while (itrLength.isLessThan(straightLegLength))
            {
                m_RacetrackTickPositions[tickIdx] = m_RacetrackOrbitNormalPosRightEnd.translatedBy(new RangeBearingHeightOffset(itrLength, bearingTo.plus(Angle.HALF_CIRCLE), Length.ZERO)).asLatLonAltPosition();
                m_RacetrackTickBearings[tickIdx++] = bearingTo.plus(Angle.HALF_CIRCLE);
                itrLength = itrLength.plus(distanceBetweenTicks);
            }

            //Difference between itrLength and straightLegLength is how far around semi-circle to start
            circleStartAngleOffset = new Angle (itrLength.minus(straightLegLength).dividedBy(radius).getDoubleValue(), Angle.RADIANS);
            circleStartAngle = Angle.RIGHT_ANGLE.minus(aRight);
            itrAngleCheck = circleStartAngleOffset;
            //Loop around circle
            while (itrAngleCheck.isLessThan(Angle.HALF_CIRCLE.minus(Angle.ONE_DEGREE)))
            {
                m_RacetrackTickPositions[tickIdx] = m_RacetrackOrbitStartPosition.translatedBy(new RangeBearingHeightOffset(radius, circleStartAngle.plus(itrAngleCheck).plus(NavyAngle.ZERO), Length.ZERO)).asLatLonAltPosition();
                m_RacetrackTickBearings[tickIdx++] = bearingTo.plus ((circleStartAngle.plus(itrAngleCheck))).plus(Angle.RIGHT_ANGLE);
                Angle angleInc = new Angle (distanceBetweenTicks.dividedBy(radius).getDoubleValue(), Angle.RADIANS);
                itrAngleCheck = itrAngleCheck.plus(angleInc);
            }


        }
        else
        {
            m_RacetrackTickPositions = null;
            m_RacetrackTickBearings = null;
        }
    }
    
    private void drawRotationTickMarks (JGeoGraphics jg, int startTickIdx, int tickCount, int tickInterval, LatLonAltPosition tickPositions[], NavyAngle tickBearings[], Angle tailAngle)
    {
        Length range = new Length (Config.getConfig().getPropertyAsInteger("SearchCanvas.RotationDirectionTailLength.Meters", 20), Length.METERS);
            
        //Draw rotation ticks.  Iterates through a pre-computed list, could do more pre-computations and simplify the repeated calls,
        //but I didn't feel like it, yet.
        for (int itr = 0; itr < tickCount; itr ++)
        {
            int tickIdx = (startTickIdx + tickInterval*itr)%tickPositions.length;
            LatLonAltPosition onArcPos = tickPositions[tickIdx];
            //NavyAngle bearing = Angle.RIGHT_ANGLE.minus(m_RacetrackTickBearings[tickIdx].plus(new Angle (120, Angle.DEGREES)).asAngle()).plus(NavyAngle.ZERO);
            NavyAngle bearing = tickBearings[tickIdx].plus(tailAngle);
            LatLonAltPosition leftArcPos = onArcPos.translatedBy(new RangeBearingHeightOffset(range, bearing, Length.ZERO)).asLatLonAltPosition();
            //bearing = Angle.RIGHT_ANGLE.minus(m_RacetrackTickBearings[tickIdx].minus(new Angle (120, Angle.DEGREES)).asAngle()).plus(NavyAngle.ZERO);
            bearing = tickBearings[tickIdx].minus(tailAngle);
            LatLonAltPosition rightArcPos = onArcPos.translatedBy(new RangeBearingHeightOffset(range, bearing, Length.ZERO)).asLatLonAltPosition();

            jg.drawLine (onArcPos, leftArcPos);
            jg.drawLine (onArcPos, rightArcPos);
        }
    }
    
    private int updateRotationDirectionTickIdx(int lastTickIdx, LatLonAltPosition[] tickPositions, boolean clockwise) 
    {
        long tickLoopPeriodMs = 10000; //10 seconds for a single tick to go around the circle
        long timeSinceLastLoopStartMs = (System.currentTimeMillis()%tickLoopPeriodMs);
        double timeFractionOfLoop = ((double)timeSinceLastLoopStartMs)/tickLoopPeriodMs;
        int tickPosition = (int)((tickPositions.length-1)*timeFractionOfLoop);
        if (!clockwise)
            tickPosition = tickPositions.length-1 - tickPosition;
        return tickPosition;
        
        /*if (clockwise)
            lastTickIdx = (++lastTickIdx)%tickPositions.length;
        else
        {
            lastTickIdx = (--lastTickIdx)%tickPositions.length;
            if (lastTickIdx < 0)
                lastTickIdx = tickPositions.length-1;
        }
        
        return lastTickIdx;*/
    }
    
    private void initializeNewCircularOrbit ()
    {
        LatLonAltPosition position1 = _safeCircularOrbitBelief.getPosition().asLatLonAltPosition();
        Length radius = _safeCircularOrbitBelief.getRadius();

        Length totalOrbitLength = (radius.times(2*Math.PI));
        int desiredArrowTicks = Config.getConfig().getPropertyAsInteger("SearchCanvas.CircularOrbit.TotalArrowTicks", 100);
        if (desiredArrowTicks > 0)
        {
            Length distanceBetweenTicks = totalOrbitLength.dividedBy(desiredArrowTicks);

            m_CircularOrbitTickPositions = new LatLonAltPosition[desiredArrowTicks];
            m_CircularOrbitTickBearings = new NavyAngle[desiredArrowTicks];
            m_CircularOrbitDirectionTickInterval = m_CircularOrbitTickPositions.length/m_CircularOrbitDirectionTickCount;
            int tickIdx = 0;

            Angle itrAngleCheck = Angle.ZERO;
            //Loop around circle and make ticks
            while (itrAngleCheck.isLessThan(Angle.FULL_CIRCLE) && tickIdx < desiredArrowTicks)
            {
                m_CircularOrbitTickPositions[tickIdx] = position1.translatedBy(new RangeBearingHeightOffset(radius, itrAngleCheck.plus(NavyAngle.ZERO), Length.ZERO)).asLatLonAltPosition();
                m_CircularOrbitTickBearings[tickIdx++] = Angle.RIGHT_ANGLE.plus(itrAngleCheck.plus(NavyAngle.ZERO));
                Angle angleInc = new Angle (distanceBetweenTicks.dividedBy(radius).getDoubleValue(), Angle.RADIANS);
                itrAngleCheck = itrAngleCheck.plus(angleInc);
            }
        }
        else
        {
            m_CircularOrbitTickPositions = null;
            m_CircularOrbitTickBearings = null;
        }
    }

    protected void saveNoGos() {
        NoGoBelief ngb = (NoGoBelief) beliefManager.get(NoGoBelief.BELIEF_NAME);
        if (ngb == null) {
            return;
        }
        try {
            File f;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setMultiSelectionEnabled(false);
            int option = chooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            } else {
                return;
            }
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(f));
            ngb.writeExternal(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadNoGos() {
        try {
            File f;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setMultiSelectionEnabled(false);
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            } else {
                return;
            }
            ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(f));
            NoGoBelief nogo = new NoGoBelief();
            nogo.readExternal(in);
            //Logger.getLogger("GLOBAL").info("loaded");
            beliefManager.put(nogo);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void saveSearchGoalBelief() {
        try {
            File f;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setMultiSelectionEnabled(false);
            int option = chooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            } else {
                return;
            }
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(f));
            out.writeObject(resetGoalMatrix);
            //Logger.getLogger("GLOBAL").info("saved");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void loadSearchGoalBelief() {
        try {
            File f;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setMultiSelectionEnabled(false);
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            } else {
                return;
            }
            ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(f));
            resetGoalMatrix = (PrimitiveTypeGeocentricMatrix) in.readObject();
            //Logger.getLogger("GLOBAL").info("loaded");
            createGoal();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void update()
    {
        try
        {
            redrawSafetyBox = true;
            redrawBearingBelief = true;
            redrawPositionBelief = true;
            redrawSearchBelief = true;
            redrawTargetBelief = true;
            _drawOnlyOverlay = false;

            if (modePanel != null)
            {
                modePanel.update();
            }
            if (_agentSettings != null)
            {
                _agentSettings.update();
            }


            GammaCompositeHistogramBelief ghbel = (GammaCompositeHistogramBelief) beliefManager.get(GammaCompositeHistogramBelief.BELIEF_NAME);

            if((ghbel != null && ghprevTime == null) ||(ghbel != null && ghprevTime!=null && ghbel.getTimeStamp().after(ghprevTime)))
            {
                ghprevTime = ghbel.getTimeStamp();
                RNHistogram data = new RNHistogram(ghbel.getHistogramData());
                if(_display!= null && _display.GammaPanel != null)
                {
                    _display.GammaPanel.setLiveTime((int)(ghbel.getLiveTime()/1000));
                    _display.GammaPanel.setStatMessage("Total Counts = " + ghbel.getSpectraCount());
                    _display.GammaPanel.updateCurrentHistogram(data);


                    if (!accumlatingGammaBackground)
                    {
                        String countsAlertMessage = gammaTotalCountsTracker.getCountsAlertMessage(ghbel.getSpectraCount());
                        _display.GammaPanel.setStatAlertMessage(countsAlertMessage);
                    }
                }
            }

            GammaDetectionBelief gdbel = (GammaDetectionBelief)beliefManager.get(GammaDetectionBelief.BELIEF_NAME);
            if((gdbel != null && adprevTime == null) ||(gdbel != null && adprevTime!=null && gdbel.getTimeStamp().after(adprevTime)))
            {
                gdprevTime = gdbel.getTimeStamp();
                 if(_display!= null && _display.GammaPanel != null)
                {
                    if (gdbel.getGammaDetections().startsWith(bridgeportProcessor.accumBackgroundMsg))
                        accumlatingGammaBackground = true;
                    else
                        accumlatingGammaBackground = false;

                    _display.GammaPanel.setDetectionMessage(gdbel.getGammaDetections());
                    _display.GammaPanel.repaint();

                 }
            }

            AlphaCompositeHistogramBelief ahbel = (AlphaCompositeHistogramBelief) beliefManager.get(AlphaCompositeHistogramBelief.BELIEF_NAME);

            if((ahbel != null && ahprevTime == null) ||(ahbel != null && ahprevTime!=null && ahbel.getTimeStamp().after(ahprevTime)))
            {
                ahprevTime = ahbel.getTimeStamp();
                RNHistogram data = new RNHistogram(ahbel.getHistogramData());
                if(_display!= null && _display.AlphaPanel != null)
                {
                    _display.AlphaPanel.setLiveTime((int)(ahbel.getLiveTime()/1000));
                    _display.AlphaPanel.setStatMessage("Total Counts = " + ahbel.getSpectraCount());
                    _display.AlphaPanel.updateCurrentHistogram(data);


                    String countsAlertMessage = alphaTotalCountsTracker.getCountsAlertMessage(ahbel.getSpectraCount());
                                    _display.AlphaPanel.setStatAlertMessage(countsAlertMessage);
                }
            }

            AlphaDetectionBelief adbel = (AlphaDetectionBelief)beliefManager.get(AlphaDetectionBelief.BELIEF_NAME);
            if((adbel != null && adprevTime == null) ||(adbel != null && adprevTime!=null && adbel.getTimeStamp().after(adprevTime)))
            {
                adprevTime = adbel.getTimeStamp();
                 if(_display!= null && _display.AlphaPanel != null)
                {
                    _display.AlphaPanel.setDetectionMessage(adbel.getAlphaDetections());
                    _display.AlphaPanel.repaint();
                 }
            }

            String t1 = "";
            String t2 = "";

            /*TASETelemetryBelief tasebel = (TASETelemetryBelief)beliefManager.get(TASETelemetryBelief.BELIEF_NAME);
            if(tasebel != null)
            {
                DecimalFormat tf = new DecimalFormat("#00.00000");
                DecimalFormat af = new DecimalFormat("#0.0");
                t1 = " | TASE LAT: " + tf.format(tasebel.getTASETelemetry().Lat) + (char)176 + " LON: " +  tf.format(tasebel.getTASETelemetry().Lon) + (char)176+ " ALT: ";

                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    t1 += af.format(tasebel.getTASETelemetry().AltEllip/0.3048) + "ft (WGS84) PDOP: ";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    t1 += af.format(tasebel.getTASETelemetry().AltEllip) + "m (WGS84) PDOP: ";

                t1 += af.format(tasebel.getTASETelemetry().PDOP) ;
            }*/


            PiccoloTelemetryBelief picbel = (PiccoloTelemetryBelief)beliefManager.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if(picbel != null)
            {

                Pic_Telemetry pictel = picbel.getPiccoloTelemetry();
                double windHeading = Math.toDegrees(Math.atan2(- pictel.WindWest,-pictel.WindSouth)+Math.PI );
                double windSpeed = Math.sqrt(pictel.WindSouth*pictel.WindSouth +pictel.WindWest*pictel.WindWest);

                DecimalFormat tf = new DecimalFormat("#00.00000");
                DecimalFormat af = new DecimalFormat("#0.0");
                
                t2 = "AUTOPILOT ";
                int posUnits = DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.POSITION_UNITS);
                if (posUnits == DisplayUnitsManager.POSITION_DD)
                {
                    t2 += "LAT: " + PositionStringFormatter.formatLatORLonAsDecDeg(picbel.getPiccoloTelemetry().Lat, "N", "S");
                    t2 += "  LON: " + PositionStringFormatter.formatLatORLonAsDecDeg(picbel.getPiccoloTelemetry().Lon, "E", "W");
                }
                else if (posUnits == DisplayUnitsManager.POSITION_DM)
                {
                    t2 += "LAT: " + PositionStringFormatter.formatLatORLonAsDegMin(picbel.getPiccoloTelemetry().Lat, "N", "S");
                    t2 += "  LON: " + PositionStringFormatter.formatLatORLonAsDegMin(picbel.getPiccoloTelemetry().Lon, "E", "W");
                }
                else if (posUnits == DisplayUnitsManager.POSITION_DMS)
                {
                    t2 += "LAT: " + PositionStringFormatter.formatLatORLonAsDegMinSec(picbel.getPiccoloTelemetry().Lat, "N", "S");
                    t2 += "  LON: " + PositionStringFormatter.formatLatORLonAsDegMinSec(picbel.getPiccoloTelemetry().Lon, "E", "W");
                }
                else if (posUnits == DisplayUnitsManager.POSITION_MGRS)
                {
                    t2 += "POS: " + PositionStringFormatter.formatLatLonAsMGRS(picbel.getPiccoloTelemetry().Lat, picbel.getPiccoloTelemetry().Lon);
                }

                if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    t2 += " ALT: " + af.format(picbel.getPiccoloTelemetry().AltMSL/0.3048) + "ft (MSL)";
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    t2 += " ALT: " + af.format(picbel.getPiccoloTelemetry().AltMSL) + "m (MSL)";

                 t2 += " PDOP: " + af.format(picbel.getPiccoloTelemetry().PDOP);

                 if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_MILESPERHOUR)
                    t2 += " AIRSPEED: "+ af.format(picbel.getPiccoloTelemetry().IndAirSpeed_mps*2.236936) +  "mph";
                 else if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_KNOTS)
                    t2 += " AIRSPEED: "+ af.format(picbel.getPiccoloTelemetry().IndAirSpeed_mps*1.943844) +  "kts";
                 else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_METERSPERSEC)
                    t2 += " AIRSPEED: "+ af.format(picbel.getPiccoloTelemetry().IndAirSpeed_mps) +  "m/s";

                 if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.WINDSPEED_UNITS) == DisplayUnitsManager.SPEED_MILESPERHOUR)
                    t2 += " WIND SPEED: "+ af.format(windSpeed*2.236936) +  "mph";
                 else if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.WINDSPEED_UNITS) == DisplayUnitsManager.SPEED_KNOTS)
                    t2 += " WIND SPEED: "+ af.format(windSpeed*1.943844) +  "kts";
                 else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.WINDSPEED_UNITS) == DisplayUnitsManager.SPEED_METERSPERSEC)
                    t2 += " WIND SPEED: "+ af.format(windSpeed) +  "m/s";

                 t2 += " WIND TO: "+ af.format(windHeading)+ (char)176;

                 if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                    t2 += " ALT-LASER: " + (picbel.getPiccoloTelemetry().AltLaserValid ? (af.format(picbel.getPiccoloTelemetry().AltLaser_m/.3048) + "ft") : "?");
                else //if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                    t2 += " ALT-LASER: " + (picbel.getPiccoloTelemetry().AltLaserValid ? (picbel.getPiccoloTelemetry().AltLaser_m + "m") : "?");



            }
            if(gpsTelemetry.isValid() && (t2.length() > 0 || t1.length() > 0))
                gpsTelemetry.setText(t2+ t1);
            
            
            RacetrackDefinitionCommandedBelief rdb = (RacetrackDefinitionCommandedBelief)beliefManager.get (RacetrackDefinitionCommandedBelief.BELIEF_NAME);
            if (rdb != null && (lastUpdatedRdbTime == null || rdb.getTimeStamp().after(lastUpdatedRdbTime)))
            {
                m_LoiterGhostStartPosition = rdb.getStartPosition();
                lastUpdatedRdbTime = rdb.getTimeStamp();
            }

            WACSWaypointActualBelief wwb = (WACSWaypointActualBelief) beliefManager.get (WACSWaypointActualBelief.BELIEF_NAME);
            if (wwb != null && (lastUpdatedWwbSettingsTime == null || wwb.getTimeStamp().after(lastUpdatedWwbSettingsTime)))
            {
                m_FinalLoiterAlt = new Length (wwb.getFinalLoiterAltitude().getDoubleValue(Length.FEET), Length.FEET);
                m_StandoffLoiterAlt = new Length (wwb.getStandoffLoiterAltitude().getDoubleValue(Length.FEET), Length.FEET);
                m_LoiterRadius = new Length (wwb.getLoiterRadius().getDoubleValue(Length.METERS), Length.METERS);
                m_InterceptAlt = new Length (wwb.getIntersectAltitude().getDoubleValue(Length.FEET), Length.FEET);
                m_InterceptRadius = new Length (wwb.getIntersectRadius().getDoubleValue(Length.METERS), Length.METERS);
                lastUpdatedWwbSettingsTime = wwb.getTimeStamp();

                TargetCommandedBelief targets = (TargetCommandedBelief)beliefManager.get(TargetCommandedBelief.BELIEF_NAME, true);
                TargetCommandedBelief satCommTargets = (TargetCommandedBelief)beliefManager.get(TargetCommandedBelief.BELIEF_NAME);
                if (targets == satCommTargets)
                {
                    String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                    if (targets != null && targets.getPositionTimeName(tmp) != null)
                        createSafeTargetLocation(targets.getPositionTimeName(tmp).getPosition().asLatLonAltPosition());
                }
                else
                {
                    //The gimbal target input by the GCS user is not the active location, so don't screw with it.
                }
            }
            else if (m_LoiterRadius == null)
            {
                m_FinalLoiterAlt = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterAltAGL_ft", 1000.0), Length.FEET);
                m_StandoffLoiterAlt = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterAltAGLStandoff_ft", 2000.0), Length.FEET);
                m_LoiterRadius = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterRad_m", 1000.0), Length.METERS);
                m_InterceptAlt = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptMinAltAGL_ft", 300.0), Length.FEET);
                m_InterceptRadius = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.interceptRad_m", 244.0), Length.METERS);
                lastUpdatedWwbSettingsTime = new Date ();
            }
            
            //check actual loiter versus loiter ghost - remove ghost once actual matches it??
                    
            //check manual/hold intercept versus ghost - remove ghost once actual matches it??

            jgeoRepaint();
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
    
    private void drawTargetCrosshairs (JGeoGraphics jg, LatLonAltPosition position)
    {
        jg.setColor (Color.GREEN);
        Stroke oldStroke = jg.getStroke();
        jg.setStroke (new BasicStroke (2));

        Point hoverPixel = this.getCurrentView().earthToMouse(position);
        jg.drawLine (hoverPixel.x, hoverPixel.y + 5, hoverPixel.x, hoverPixel.y + 25);
        jg.drawLine (hoverPixel.x, hoverPixel.y - 5, hoverPixel.x, hoverPixel.y - 25);
        jg.drawLine (hoverPixel.x + 5, hoverPixel.y, hoverPixel.x + 25, hoverPixel.y);
        jg.drawLine (hoverPixel.x - 5, hoverPixel.y, hoverPixel.x - 25, hoverPixel.y);

        jg.setStroke (oldStroke);
    }

    public void tableChanged(TableModelEvent e) {
        System.err.println("tableChanged:: TODO CHANGE MODE MAP");
    }

    private Region getDrawingRegion() {
        if (startGoalPosition != null && currentGoalPosition != null) {
            AbsolutePosition[] positions = new AbsolutePosition[3];
            Length r = startGoalPosition.getRangeTo(currentGoalPosition);
            r = r.dividedBy(2.0d);
            NavyAngle b = startGoalPosition.getBearingTo(currentGoalPosition);
            AbsolutePosition midPoint = startGoalPosition.translatedBy(new RangeBearingHeightOffset(r, b, Length.ZERO));
            positions[0] = startGoalPosition;
            positions[1] = midPoint;
            positions[2] = currentGoalPosition;
            Region region = new Region(positions);
            return region.getMinimumBoundingRectangle();
        }
        return null;
    }

    private void drawMatrix(PrimitiveTypeGeocentricMatrix matrix, JGeoGraphics jg) {
        drawMatrix(matrix, jg, false);
    }

    /**
     * Draws the matrix with the correct gridline colors for chosen search areas, etc..
     */
    private void drawMatrix(PrimitiveTypeGeocentricMatrix matrix, JGeoGraphics jg, boolean cloud) {
        int xSize = matrix.getXSize();
        int ySize = matrix.getYSize();

        Region r;
        float n;

        ArrayList regions = new ArrayList();
        ArrayList colors = new ArrayList();

        // goes through entire grid and checks to draw the search areas within that grid
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                r = matrix.getRegion(new Point(x, y));
                n = matrix.get(x, y);
                float d = n;

//            Logger.getLogger("GLOBAL").info("d is: " + d); 

                if (d != 0) {
                    regions.add(0, r);
                    colors.add(0, new Float(d));
                }
            }
        }

        for (int z = 0; z < regions.size(); z++) {
            r = (Region) regions.get(z);
            float d = ((Float) colors.get(z)).floatValue();
            if (cloud) {
                jg.setColor(new Color(d, d * 0.5f, 0.0f));
            } else {
                if (d == 1.0) {
                    jg.setColor(new Color(0.8f, 0.8f, 0.8f, 0.5f));
                //jg.setColor(Color.GREEN);
                } else if (d == -1.0) {
                    jg.setColor(Color.RED);
                } else {
                    jg.setColor(Color.BLACK);
                }
            }
            jg.setStroke(new BasicStroke(1));
            jg.drawRegion(r);
        }
    }

    /**
     * Checks to see if each block in the grid has been searched and fills the block region
     * with the appropriate color
     */
    private void fillMatrix(PrimitiveTypeGeocentricMatrix matrix, JGeoGraphics jg, float[] mask) {
        int xSize = matrix.getXSize();
        int ySize = matrix.getYSize();

        Region r;
        float n;
        float min = matrix.getMinimum().value;
        float max = matrix.getMaximum().value;

        float diff = min - max;
        //Logger.getLogger("GLOBAL").info("Max: " + max + "	 Min: " + min);


        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                r = matrix.getRegion(new Point(x, y));
                n = matrix.get(x, y);

                float myDiff = min - n;
                float dividedDiff = myDiff / diff;

                //d is a value between 0 and 1
                float d = Math.abs(myDiff);
                if (d > 1.0f) {
                    d = 1.0f;
                }

                //Logger.getLogger("GLOBAL").info("d is: " + d);
                jg.setColor(new Color(d * mask[0], d * mask[1], d * mask[2], mask[3]));

                if (d >= .1) {
                    jg.fillRegion(r);
                //jg.setColor(Color.BLACK);
                //jg.drawRegion(r);
                }
            }
        }
    }

    private void fillMatrix(PrimitiveTypeGeocentricMatrix matrix, JGeoGraphics jg, PrimitiveTypeGeocentricMatrix cmatrix) {
        int xSize = matrix.getXSize();
        int ySize = matrix.getYSize();

        Region r;
        float n;
        float min = matrix.getMinimum().value;
        float max = matrix.getMaximum().value;

        float diff = min - max;
        //Logger.getLogger("GLOBAL").info("Max: " + max + "	 Min: " + min);


        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                r = matrix.getRegion(new Point(x, y));
                n = matrix.get(x, y);

                float myDiff = min - n;
                float dividedDiff = myDiff / diff;

                //d is a value between 0 and 1
                float d = Math.abs(myDiff);
                if (d > 1.0f) {
                    d = 1.0f;
                }

                //Logger.getLogger("GLOBAL").info("d is: " + d);

                float color = cmatrix.get(x, y);

                switch ((int) color)
                {
                    case 1:
                        jg.setColor(new Color(0.0f, 0.0f, 1.0f, 0.4f));
                        break;
                    case 2:
                        jg.setColor(new Color(1.0f, 0.0f, 1.0f, 0.4f));
                        break;
                    default:
                        jg.setColor(new Color(1.0f, 0.0f, 0.0f, 0.4f));
                }

                if (d >= .1) {
                    jg.fillRegion(r);
                //jg.setColor(Color.BLACK);
                //jg.drawRegion(r);
                }
            }
        }
    }

    public JPanel getSearchSettingPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 1));
        panel.add(lockButton);
        panel.add(goalButton);
        panel.add(addButton);
        panel.add(noGoButton);
        panel.add(targetButton);
        panel.add(noGoRegionButton);
        panel.add(removeNoGoRegionButton);
        panel.add(addCloudButton);
        panel.add(addCloudDetectButton);
        panel.add(finishButton);

        return panel;
    }

    public synchronized JPanel getSwarmModePanel() {
        if (modePanel == null) {
            modePanel = new ModePanel(beliefManager, agentTracker, modeMap);
        }
        return modePanel;
    }

    public JPanel getDisplaySettingPanel() {
        JPanel panel = new JPanel(new GridLayout(12, 1));
        panel.add(namesButton);
        panel.add(showCloud);
        panel.add(showDirection);
        panel.add(showHistory);
        panel.add(showWind);
        panel.add(showUAVWaypoint);
        panel.add(showMatrixButton);
        panel.add(showRealObstacles);
        panel.add(showLittleObstacles);
        //panel.add(showSNRWindow);
        //panel.add(showPath);
        //panel.add(showSNREdges);
        //panel.add(showPacketInfoWindow);
        panel.setVisible(true);
        return panel;
    }
    protected WACSSettingsPanel _WACSPanel;
    protected WACSControlPanel _WACSControlPanel;
    protected AgentSettingsPanel _agentSettings;

    public void setWACSPanel(WACSSettingsPanel w) {
        _WACSPanel = w;
    }

    public void setWACSControlPanel(WACSControlPanel w) {
        _WACSControlPanel = w;
    }

    public JPanel getAgentSettingsPanel()
    {
        if (_agentSettings == null)
        {
            _agentSettings = new AgentSettingsPanel(this, beliefManager, agentTracker);
        }
        modePanel = new ModePanel(beliefManager, agentTracker, modeMap);
        JPanel panel = new JPanel(new GridLayout(2,1));
        panel.add(modePanel);
        panel.add(_agentSettings);
        

        return panel;
    }

    /** 
     * This method returns the default COMID toolbar.  
     * 
     * @return JToolBar 
     */
    public JToolBar createToolBar(boolean simple) {
        m_ToolBar = new JToolBar();
        m_ToolBar.setFloatable(!simple);
        m_ToolBar.setRollover(true);
        m_ToolBar.setFocusable(false);        

        /*JButton panUpButton = new JButton (panUp);
        panUpButton.setFocusable(false);*/
        m_ToolBar.add(panUp);
        m_ToolBar.add(panDown);
        m_ToolBar.add(panLeft);
        m_ToolBar.add(panRight);
        m_ToolBar.add(zoomInAction);
        m_ToolBar.add(zoomOutAction);
        m_ToolBar.add(locateUavButton);
        m_ToolBar.add(rulerButton);
        if (!simple)
            m_ToolBar.add(exitAction);
        m_ToolBar.addSeparator();
        //m_ToolBar.add(gimbalTargetButton);
        //m_ToolBar.add(loiterPatternButton);
        //m_ToolBar.add(trackPatternButton);
        //m_ToolBar.add(holdTrackPatternButton);
        //m_HoldTrackReleaseButtonToolbarIndex = m_ToolBar.getComponentCount();
        
        
        
        if (!simple)
        {
            //toolBar.add(interceptTargetButton);
            m_ToolBar.add(showAltitudeButton);
            m_ToolBar.add(showRNButton);

            if (Config.getConfig().getPropertyAsBoolean("FlightControl.useShadowDriver", false))
            {
                m_ToolBar.add(showShadowDataButton);
            }

            showAltitudeButton.addActionListener(this);
            //interceptTargetButton.addActionListener(this);
            //gimbalTargetButton.addActionListener(this);
            showRNButton.addActionListener(this);
            showShadowDataButton.addActionListener(this);
            m_ToolBar.addSeparator();
        }
        
        for (Component comp : m_ToolBar.getComponents())
            comp.setFocusable(false);
        
        return m_ToolBar;
    }

        public JToolBar createTelemetryBar(boolean simple)
        {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(!simple);
        toolBar.add(gpsTelemetry);
        return toolBar;
    }

    /** 
     * This method returns the default menu bar 
     * 
     * @return JGeoMenuBar 
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.add(exitAction);
        JMenuItem menuItem = new JMenuItem("Save Search Goal...");
        menuItem.setActionCommand("saveSearchGoal");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Load Search Goal...");
        menuItem.setActionCommand("loadSearchGoal");
        menuItem.addActionListener(this);
        file.add(menuItem);
        file.addSeparator();
        menuItem = new JMenuItem("Save No Gos...");
        menuItem.setActionCommand("saveNoGos");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Load No Gos...");
        menuItem.setActionCommand("loadNoGos");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuBar.add(file);

        JMenu nav = new JMenu("Navigation");
        nav.add(panUp);
        nav.add(panDown);
        nav.add(panLeft);
        nav.add(panRight);
        nav.addSeparator();
        nav.add(zoomInAction);
        nav.add(zoomOutAction);
        menuBar.add(nav);

        JMenu geo = new JMenu("Geo Controls");
        geo.addSeparator();
        geo.add(orthoAction);
        geo.add(mercatorAction);
        menuBar.add(geo);

        JMenu tools = new JMenu("Tools");
        JMenuItem item = new JMenuItem("Config Table");
        item.setActionCommand("configTable");
        item.addActionListener(this);
        tools.add(item);
        item = new JMenuItem("Config");
        item.setActionCommand("config");
        item.addActionListener(this);
        tools.add(item);
        item = new JMenuItem("Classification");
        item.setActionCommand("classification");
        item.addActionListener(this);
        tools.add(item);
        tools.addSeparator();
        item = new JMenuItem("Clear Global Beliefs");
        item.setActionCommand("clearGlobalBeliefs");
        item.setToolTipText("Clears all beliefs in the swarm.\nThis functionality requires synced clocks.");
        item.addActionListener(this);
        tools.add(item);
        m_UnitsMenuItem = new JMenuItem ("Set Display Units");
        m_UnitsMenuItem.addActionListener(this);
        m_WindEstimationMenuItem = new JMenuItem ("Show Wind Estimation Panel");
        m_WindEstimationMenuItem.addActionListener(this);
        m_AglDisplayMenuItem = new JMenuItem ("Show AGL Display");
        m_AglDisplayMenuItem.addActionListener(this);

        tools.addSeparator();
        tools.add(m_UnitsMenuItem);
        tools.add(m_WindEstimationMenuItem);
        tools.add(m_AglDisplayMenuItem);
        tools.addSeparator();
        
        //User defined keep out Region
        item = new JMenuItem("Define Keep Out Region");
        item.setActionCommand("keepOutRegion");
        item.addActionListener(this);
        tools.add(item);

        menuBar.add(tools);


        return menuBar;
    }

    // JGeoMouseListener Interface 
    public void mousePressed(JGeoMouseEvent event)
    {
        if (goalButton.isSelected() || noGoButton.isSelected() || addButton.isSelected() || noGoRegionButton.isSelected())
        {
            startGoalPosition = event.getPosition();
        }
        else if (m_AddSimulatedCloud)
        {
            if (cloudPointAPosition == null)
            {
                cloudPointAPosition = event.getPosition();
                cloudPointBPosition = event.getPosition();
                jgeoRepaint();
            }
        }
        else if (getInDrawSafetyBoxMode())
        {
            _safetyBoxPanel.setSafetyBoxPosition1(event.getPosition().asLatLonAltPosition());
            jgeoRepaint();
        }
        else
        {
            super.mousePressed (event);
        }
    }

    public void mouseReleased(JGeoMouseEvent event)
    {
        if (goalButton.isSelected() || addButton.isSelected())
        {
            resetMatrix(event.getPosition(), SearchGoalBelief.GOAL_VALUE);
            startGoalPosition = null;
            currentGoalPosition = null;
            //System.err.println("mouse released: repainting");
            redrawSafetyBox = true;
            redrawSearchBelief = true;
            goalChanged = true;
            jgeoRepaint();
        }
        else if (noGoRegionButton.isSelected())
        {
            LatLonAltPosition p = event.getLatLonAltPosition();
            if (p != null && startGoalPosition != null)
            {
                startGoalPosition = null;
                currentGoalPosition = null;
            }
        }
        else if (noGoButton.isSelected())
        {
            startGoalPosition = null;
            currentGoalPosition = null;
            Point p = resetGoalMatrix.getPoint(event.getPosition());
            if (p != null)
            {
                resetMatrix(event.getPosition(), SearchGoalBelief.NO_GO_VALUE);
                jgeoRepaint();
                goalChanged = true;
            }
        }
        else if (getInDrawSafetyBoxMode())
        {
            _safetyBoxPanel.setSafetyBoxPosition2(event.getPosition().asLatLonAltPosition());
            _safetyBoxPanel.drawSafetyBoxMouseUp();
            jgeoRepaint();
        }
        else
        {
            super.mouseReleased (event);

        }
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        super.mouseWheelMoved (e);
    }

    protected void removeNoGo(AbsolutePosition p) {
        NoGoBelief noGo = (NoGoBelief) beliefManager.get(NoGoBelief.BELIEF_NAME);
        if (noGo != null) {
            HashMap regs = noGo.getAllRegions();
            Set keys = regs.keySet();
            Iterator itr = keys.iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Region reg = (Region) ((NoGoTimeName) regs.get(key)).getObstacle();
                if (reg != null && reg.contains(p)) {
                    beliefManager.put(NoGoBelief.createBelief(agentID, key, null));
                }
            }
        }
        jgeoRepaint();
    }

    public void mouseClicked(JGeoMouseEvent event) {
        if (removeNoGoRegionButton.isSelected()) {
            removeNoGo(event.getLatLonAltPosition());
        } else if (noGoRegionButton.isSelected()) {
            if (_points == null) {
                _points = new LinkedList();
                _showLines = true;
            }
            _points.add(event.getLatLonAltPosition());
        }else if(definingKeepOutRegion){
            if (_points == null) {
                _points = new LinkedList();
                _showLines = true;
            }
            _points.add(event.getLatLonAltPosition());
        }else {
            if (rulerButton.isSelected())
            {
                //ruler measurement
                super.mouseClicked(event);
                update ();
                
                return;
            }
            /*
            if (gimbalTargetButton.isSelected())
            {
                //add a new target where the mouse was clicked

                LatLonAltPosition pos = event.getPosition().asLatLonAltPosition();
                createSafeTargetLocation (pos);
                
                m_ToolButtonGroup.clearSelection();

                return;
            }
            */
            /*
            if (loiterPatternButton.isSelected())
            {
                m_ToolButtonGroup.clearSelection();
                synchronized (m_HoverMousePositionLock)
                {
                    m_HoverMousePosition = null;
                }
                if (event.getButton() == MouseEvent.BUTTON3)
                    return;
                
                synchronized (loiterPatternButton)
                {
                    LatLonAltPosition holdMousePosition = null;
                    holdMousePosition = event.getLatLonAltPosition().duplicate();
                
                    verifyNewRacetrackDefinitionBelief (holdMousePosition);
                    m_LoiterGhostStartPosition = holdMousePosition;
                }
           
                return;
            }
            */
            /*
            if (holdTrackPatternButton.isSelected())
            {
                //Send a hold track belief
                m_ToolButtonGroup.clearSelection();
                synchronized (m_HoverMousePositionLock)
                {
                    m_HoverMousePosition = null;
                }
                if (event.getButton() == MouseEvent.BUTTON3)
                    return;

                synchronized (holdTrackPatternButton)
                {
                    LatLonAltPosition mousePosition = null;
                    mousePosition = event.getLatLonAltPosition().duplicate();
                
                    m_TrackGhostPosition = mousePosition;
                    verifyNewManualInterceptPosition (m_TrackGhostPosition, true);
                }
                
                m_ToolBar.remove(holdTrackReleaseButton);
                m_ToolBar.add (holdTrackReleaseButton, m_HoldTrackReleaseButtonToolbarIndex);
                return;
            }
            */
            /*
            if (trackPatternButton.isSelected())
            {
                //Send a track belief
                m_ToolButtonGroup.clearSelection();
                synchronized (m_HoverMousePositionLock)
                {
                    m_HoverMousePosition = null;
                }
                if (event.getButton() == MouseEvent.BUTTON3)
                    return;

                synchronized (holdTrackPatternButton)
                {
                    LatLonAltPosition mousePosition = null;
                    mousePosition = event.getLatLonAltPosition().duplicate();
                
                    m_TrackGhostPosition = mousePosition;
                    verifyNewManualInterceptPosition (m_TrackGhostPosition, false);
                }
                
                //This should effectively cancel hold tracks
                m_ToolBar.remove(holdTrackReleaseButton);

                return;
            }
            */
            
            if (event.getButton() == JGeoMouseEvent.BUTTON1) {
                setViewCenter(event.getLatLonAltPosition());
                //Logger.getLogger("GLOBAL").info(event.getLatLonAltPosition());
                jgeoRepaint();
            }
            if (event.getButton() == JGeoMouseEvent.BUTTON2) {
                //Logger.getLogger("GLOBAL").info(event.getLatLonAltPosition());
            }
            if (targetButton.isSelected()) {
                //add a new target where the mouse was clicked
                String tmp = new String("target" + new Integer(target_counter++));
                //Logger.getLogger("GLOBAL").info("Adding " + tmp);
                LatLonAltPosition gimbalPosition = event.getLatLonAltPosition();
                Altitude gimbalAltitude = new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(gimbalPosition.getLatitude().getDoubleValue(Angle.DEGREES),
                                                                                              gimbalPosition.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS);
                gimbalPosition = new LatLonAltPosition(gimbalPosition.getLatitude(), gimbalPosition.getLongitude(), gimbalAltitude);
                beliefManager.put(new TargetCommandedBelief(agentID,
                        gimbalPosition,
                        Length.ZERO,
                        tmp));
                beliefManager.put(new ClassificationBelief(agentID,
                        tmp, Classification.DEFAULT));
            }
            
//            if (interceptTargetButton.isSelected())
//            {
//                //add circular orbit where clicked
//
//                WACSWaypointBelief wwb = (WACSWaypointBelief)beliefManager.get(WACSWaypointBelief.BELIEF_NAME);
//                double agl = wwb.getIntersectAltitude().getDoubleValue(Length.METERS);
//                LatLonAltPosition pos = event.getPosition().asLatLonAltPosition();
//                double groundAlt_m = DtedGlobalMap.getDted().getAltitude(pos.getLatitude().getDoubleValue(Angle.DEGREES), pos.getLongitude().getDoubleValue(Angle.DEGREES));
//                Altitude altMSL = new Altitude(wwb.getIntersectAltitude().getDoubleValue(Length.METERS) + groundAlt_m, Length.METERS);
//
//                LatLonAltPosition goalpos = new LatLonAltPosition(pos.getLatitude(),pos.getLongitude(),altMSL);
//
//                CircularOrbitBelief cob = new CircularOrbitBelief(agentID, goalpos, wwb.getIntersectRadius(), true);
//                beliefManager.put(cob);
//
//            }

            if (m_AddSimulatedCloud) {
                if (cloudPointBPosition != null) {
                    cloudPointBPosition = event.getPosition();
                }
                jgeoRepaint();
            }
        }
    }

    public void mouseEntered(JGeoMouseEvent event) {
    }

    public void mouseExited(JGeoMouseEvent event) 
    {
        synchronized (m_HoverMousePositionLock)
        {
            m_HoverMousePosition = null;
        }
    }

    public PrimitiveTypeGeocentricMatrix getResetMatrix() {
        return resetGoalMatrix;
    }

    private void resetMatrix(AbsolutePosition pos, float goalValue) {
        AbsolutePosition[] rPoints = new AbsolutePosition[4];
        LatLonAltPosition sgp = startGoalPosition.asLatLonAltPosition();
        LatLonAltPosition egp = pos.asLatLonAltPosition();
        rPoints[0] = sgp;
        rPoints[1] = new LatLonAltPosition(sgp.getLatitude(), egp.getLongitude(), egp.getAltitude());
        rPoints[2] = egp;
        rPoints[3] = new LatLonAltPosition(egp.getLatitude(), sgp.getLongitude(), sgp.getAltitude());

        PrimitiveTypeGeocentricMatrix newMatrix = RegionBelief.getSearchGoal(beliefManager, new Region(rPoints), resetGoalMatrix, 500.0, false);
        if (newMatrix != null) {
            resetGoalMatrix = newMatrix;
        }
    }

    protected void clearGoalMatrix() {
        int xSize = resetGoalMatrix.getXSize();
        int ySize = resetGoalMatrix.getYSize();

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                resetGoalMatrix.set(x, y, SearchGoalBelief.NOT_GOAL_VALUE);
            }
        }
        goalChanged = true;
    }

    public void mouseDragged(JGeoMouseEvent event)
    {
        if (goalButton.isSelected() || noGoButton.isSelected() || addButton.isSelected())
        {
            //System.err.println("Setting new goal");
            currentGoalPosition = event.getPosition();
            jgeoRepaint();
        }
        else if (m_AddSimulatedCloud)
        {
            if (cloudPointBPosition != null)
            {
                cloudPointBPosition = event.getPosition();
                goalChanged = true;
            }
            jgeoRepaint();
        }
        else if (getInDrawSafetyBoxMode())
        {
            _safetyBoxPanel.setSafetyBoxPosition2(event.getPosition().asLatLonAltPosition());
            jgeoRepaint();
        }
        /*else if (loiterPatternButton.isSelected())
        {
            synchronized (loiterPatternButton)
            {
                LatLonAltPosition holdMousePosition = null;
                holdMousePosition = event.getPosition().asLatLonAltPosition().duplicate();
                m_LoiterGhostEndPosition = holdMousePosition;
            }

            _drawOnlyOverlay = true;
            jgeoRepaint();
        }*/
        else
        {
            super.mouseDragged (event);
        }
    }

    public void mouseMoved(JGeoMouseEvent event)
    {
        LatLonAltPosition pos = event.getPosition().asLatLonAltPosition();
        
        m_MouseGroundLat = pos.getLatitude();
        m_MouseGroundLon = pos.getLongitude();
        m_MouseGroundAltM = new Altitude(DtedGlobalMap.getDted().getAltitudeMSL(pos.getLatitude().getDoubleValue(Angle.DEGREES),
                pos.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS);
        _drawOnlyOverlay = true;

        synchronized (m_HoverMousePositionLock)
        {
            m_HoverMousePosition = pos;
        }
        
        if (rulerButton.isSelected())
        {
            super.mouseMoved(event);
        }

        if ((System.currentTimeMillis() - _mouseHoverLastUpdate_ms) > 50)
        {
            _mouseHoverLastUpdate_ms = System.currentTimeMillis();

            double distanceToClosestElement_m = 10000;
            double distanceToClosestDetection_m = distanceToClosestElement_m;
            double distanceToClosestOrbit_m = distanceToClosestElement_m;
            double distanceToClosestRacetrack_m = distanceToClosestElement_m;

            CloudDetection closestDetection = null;
            CircularOrbitBelief closestOrbit = null;
            RacetrackOrbitBelief closestRacetrack = null;

            CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief) beliefManager.get(CloudDetectionBelief.BELIEF_NAME);
            if (cloudDetectionBelief != null)
            {
                synchronized(cloudDetectionBelief.getLock())
                {
                    for (CloudDetection cloudDetection : cloudDetectionBelief.getDetections())
                    {
                        double distanceToDetection_m = cloudDetection.getDetection().getRangeTo(event.getPosition()).getDoubleValue(Length.METERS);
                        if (distanceToDetection_m < distanceToClosestElement_m)
                        {
                            distanceToClosestElement_m = distanceToDetection_m;
                            distanceToClosestDetection_m = distanceToDetection_m;
                            closestDetection = cloudDetection;
                        }
                    }
                }
            }
            
            String agentMode = "unknown";
            AgentModeActualBelief agentModeBelief = (AgentModeActualBelief) beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null)
                agentMode = agentModeBelief.getMode(WACSAgent.AGENTNAME).getName();

            if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
            {
                TestCircularOrbitBelief tobBelief = (TestCircularOrbitBelief)beliefManager.get(TestCircularOrbitBelief.BELIEF_NAME);
                if (tobBelief != null)
                {
                    double distanceToOrbitCenter_m = tobBelief.getPosition().getRangeTo(event.getPosition()).getDoubleValue(Length.METERS);
                    double distanceToOrbitEdge_m = Math.abs(distanceToOrbitCenter_m - tobBelief.getRadius().getDoubleValue(Length.METERS));
                    if (distanceToOrbitEdge_m < distanceToClosestElement_m)
                    {
                        distanceToClosestElement_m = distanceToOrbitEdge_m;
                        
                        if (agentMode.equals(ParticleCloudPredictionBehavior.MODENAME) && !tobBelief.isRacetrack())
                        {
                            distanceToClosestOrbit_m = distanceToOrbitEdge_m;
                            closestOrbit = tobBelief.asCircularOrbitBelief();
                        }
                        else if (agentMode.equals(LoiterBehavior.MODENAME) && tobBelief.isRacetrack())
                        {
                            distanceToClosestRacetrack_m = distanceToOrbitEdge_m;
                            closestRacetrack = tobBelief.asRacetrackOrbitBelief();
                        }
                        
                    }
                }
            }
            else
            {
                CircularOrbitBelief circularOrbitBelief = (CircularOrbitBelief)beliefManager.get(CircularOrbitBelief.BELIEF_NAME);
                if (circularOrbitBelief != null && agentMode.equals(ParticleCloudPredictionBehavior.MODENAME))
                {
                    double distanceToOrbitCenter_m = circularOrbitBelief.getPosition().getRangeTo(event.getPosition()).getDoubleValue(Length.METERS);
                    double distanceToOrbitEdge_m = Math.abs(distanceToOrbitCenter_m - circularOrbitBelief.getRadius().getDoubleValue(Length.METERS));
                    if (distanceToOrbitEdge_m < distanceToClosestElement_m)
                    {
                        distanceToClosestElement_m = distanceToOrbitEdge_m;
                        distanceToClosestOrbit_m = distanceToOrbitEdge_m;
                        closestOrbit = circularOrbitBelief;
                    }
                }

                RacetrackOrbitBelief racetrackBelief = (RacetrackOrbitBelief)beliefManager.get(RacetrackOrbitBelief.BELIEF_NAME);
                if (racetrackBelief != null && agentMode.equals(LoiterBehavior.MODENAME))
                {
                    LatLonAltPosition position = new LatLonAltPosition (racetrackBelief.getLatitude1(), racetrackBelief.getLongitude1(), racetrackBelief.getFinalAltitudeMsl());
                    double distanceToRacetrackCenter_m = position.getRangeTo(event.getPosition()).getDoubleValue(Length.METERS);
                    double distanceToRacetrackEdge_m = Math.abs(distanceToRacetrackCenter_m - racetrackBelief.getRadius().getDoubleValue(Length.METERS));
                    if (distanceToRacetrackEdge_m < distanceToClosestElement_m)
                    {
                        distanceToClosestElement_m = distanceToRacetrackEdge_m;
                        distanceToClosestRacetrack_m = distanceToRacetrackEdge_m;
                        closestRacetrack = racetrackBelief;
                    }
                }
            }
            
            if (distanceToClosestElement_m < 50)
            {
                synchronized (m_mouseHoverLock)
                {
                    if (distanceToClosestDetection_m == distanceToClosestElement_m)
                    {
                        m_detectionMouseHoveredOn = closestDetection;
                        m_orbitMouseHoveredOn = null;
                        m_racetrackMouseHoveredOn = null;
                    }
                    else if (distanceToClosestOrbit_m == distanceToClosestElement_m && m_ShowNextOrbitOpt)
                    {
                        m_orbitMouseHoveredOn = closestOrbit;
                        m_detectionMouseHoveredOn = null;
                        m_racetrackMouseHoveredOn = null;
                    }
                    else if (distanceToClosestRacetrack_m == distanceToClosestElement_m)
                    {
                        m_orbitMouseHoveredOn = null;
                        m_detectionMouseHoveredOn = null;
                        m_racetrackMouseHoveredOn = closestRacetrack;
                    }
                    
                    m_mouseHoverPoint = event.getPoint();
                }
            }
            else
            {
                m_detectionMouseHoveredOn = null;
                m_orbitMouseHoveredOn = null;
                m_racetrackMouseHoveredOn = null;
            }
            
            if (m_ShowLocationBubbleOpt && _bubblePredictor.shouldShowBubble() && _bubblePredictor.hasMinMaxPos())
            {
                if (m_MouseGroundLat.compareTo(_bubblePredictor.getMinLatitude()) > 0 && m_MouseGroundLat.compareTo(_bubblePredictor.getMaxLatitude()) < 0 &&
                        m_MouseGroundLon.compareTo(_bubblePredictor.getMinLongitude()) > 0 && m_MouseGroundLon.compareTo(_bubblePredictor.getMaxLongitude()) < 0)
                {
                    m_bubbleMouseHoveredOn = true;
                    m_mouseHoverPoint = event.getPoint();
                }
                else
                {
                    m_bubbleMouseHoveredOn = false;
                }
            }
            else
            {
                m_bubbleMouseHoveredOn = false;
            }
        }

        jgeoRepaint();
    }

    public AgentTracker getAgentTracker() {
        return agentTracker;
    }
    private JDialog configDialog = null;
    private JTextField appliesToField;
    private JTextField propertyField;
    private JTextField valueField;

    public JDialog getConfigDialog() {
        if (configDialog != null) {
            return configDialog;
        }

        configDialog = new JDialog(parentFrame, "Set Configuration Property", false);
        configDialog.setSize(350, 150);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(4, 2));
        contentPane.add(new JLabel("Applies to:"));
        appliesToField = new JTextField();
        contentPane.add(appliesToField);
        contentPane.add(new JLabel("Property:"));
        propertyField = new JTextField();
        contentPane.add(propertyField);
        contentPane.add(new JLabel("Value:"));
        valueField = new JTextField();
        contentPane.add(valueField);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        JButton button = new JButton("OK");
        button.setActionCommand("okConfig");
        button.addActionListener(this);
        buttonPanel.add(button);
        configDialog.getRootPane().setDefaultButton(button);
        button = new JButton("Cancel");
        button.setActionCommand("cancelConfig");
        button.addActionListener(this);
        buttonPanel.add(button);
        contentPane.add(new JPanel());
        contentPane.add(buttonPanel);


        configDialog.setContentPane(contentPane);
        return configDialog;
    }
    private ConfigTableView _configTableDialog;

    public JDialog getConfigTableDialog() {
        if (_configTableDialog != null) {
            return _configTableDialog;
        }

        _configTableDialog = new ConfigTableView(parentFrame, "Property Configuration Table", false, beliefManager);
       
        return _configTableDialog;
    }
    
    JDialog classificationDialog = null;
    JComboBox targetComboBox;
    JComboBox classificationComboBox;
    Vector classifications;

    public JDialog getClassificationDialog() {
        boolean first = true;
        JPanel contentPane = null;
        if (classificationDialog == null) {
            classificationDialog = new JDialog(parentFrame, "Set Target Classification", false);
            classificationDialog.setSize(350, 150);

            contentPane = new JPanel();
            contentPane.setLayout(new GridLayout(2, 2));

            targetComboBox = new JComboBox();
            targetComboBox.setPreferredSize(new Dimension(150, 30));
            targetComboBox.addActionListener(this);
            targetComboBox.setActionCommand("targetSelection");
            JPanel panel = new JPanel();
            panel.add(targetComboBox);
            contentPane.add(panel);
        } else {
            first = false;
            targetComboBox.removeAllItems();
        }

        TargetActualBelief targetBel = (TargetActualBelief) beliefManager.get(TargetActualBelief.BELIEF_NAME);

        if (targetBel != null) {
            synchronized (targetBel) {
                Iterator itr = targetBel.getAll().iterator();
                while (itr.hasNext()) {
                    PositionTimeName ptn = (PositionTimeName) itr.next();
                    targetComboBox.addItem(ptn.getName());
                }
            }
        }

        if (!first) {
            return classificationDialog;
        }



        classifications = new Vector();
        classifications.add("default");
        classifications.add("unknown");
        classifications.add("target");
        classifications.add("friendly");
        classifications.add("asset");
        classifications.add("HVA");
        classifications.add("Neutralized");

        classificationComboBox = new JComboBox(classifications);
        classificationComboBox.setPreferredSize(new Dimension(150, 30));
        JPanel panel = new JPanel();
        panel.add(classificationComboBox);
        contentPane.add(panel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        JButton button = new JButton("OK");
        button.setActionCommand("okClassification");
        button.addActionListener(this);
        buttonPanel.add(button);
        classificationDialog.getRootPane().setDefaultButton(button);
        button = new JButton("Cancel");
        button.setActionCommand("cancelClassification");
        button.addActionListener(this);
        buttonPanel.add(button);
        contentPane.add(new JPanel());
        contentPane.add(buttonPanel);


        classificationDialog.setContentPane(contentPane);
        return classificationDialog;

    }

    public String classificationToString(int classification) {
        if (classification == Classification.UNKNOWN) {
            return "unknown";
        }
        if (classification == Classification.PERSON_TARGET) {
            return "person_target";
        }
        if (classification == Classification.VEHICLE_TARGET) {
            return "vehicle_target";
        }
        if (classification == Classification.FRIENDLY) {
            return "friendly";
        }
        if (classification == Classification.ASSET) {
            return "asset";
        }
        if (classification == Classification.HVA) {
            return "HVA";
        }
        if (classification == Classification.UGV) {
            return "UGV";
        }
        if (classification == Classification.UAV) {
            return "UAV";
        }
        if (classification == Classification.NEUTRALIZED) {
            return "Neutralized";
        }
        if (classification == Classification.NEUTRAL) {
            return "Neutral";
        }
        return "default";
    }

    protected void createCloud() {
        if (cloudPointAPosition != null && cloudPointBPosition != null) {
            Length distance = cloudPointAPosition.getRangeTo(cloudPointBPosition);
            distance = distance.plus(CLOUD_ELLIPSE_DIST);

            LatLonAltPosition aPos = new LatLonAltPosition(cloudPointAPosition.asLatLonAltPosition().getLatitude(),
                    cloudPointAPosition.asLatLonAltPosition().getLongitude(),
                    Altitude.ZERO);
            LatLonAltPosition bPos = new LatLonAltPosition(cloudPointBPosition.asLatLonAltPosition().getLatitude(),
                    cloudPointBPosition.asLatLonAltPosition().getLongitude(),
                    Altitude.ZERO);
            Ellipse e = new Ellipse(aPos, bPos, distance);
            CloudBelief cb = new CloudBelief(agentID,
                    e,
                    new Length(Config.getConfig().getPropertyAsDouble("SimulationMode.initialSimulatedCloudHeight_ft", 350), Length.FEET),
                    Altitude.ZERO);
            beliefManager.put(cb);
        }
        cloudPointAPosition = null;
        cloudPointBPosition = null;
    }

    protected void createGoal() {
        SearchGoalBelief goal = new SearchGoalBelief(agentID, resetGoalMatrix);
        beliefManager.put(goal);
        //Logger.getLogger("GLOBAL").info("Added goal");
        //jgeoRepaint();
        lockSelected = true;
        goalChanged = false;
    }

    public void setSafetyBox(Latitude latitude1,
                             Latitude latitude2,
                             Longitude longitude1,
                             Longitude longitude2)
    {
        Latitude northLatitude;
        Latitude southLatitude;
        Longitude westLongitude;
        Longitude eastLongitude;
        
        if (latitude1.isNorthOf(latitude2))
        {
            northLatitude = latitude1;
            southLatitude = latitude2;
        }
        else
        {
            northLatitude = latitude2;
            southLatitude = latitude1;
        }

        if (longitude1.isWestOf(longitude2))
        {
            westLongitude = longitude1;
            eastLongitude = longitude2;
        }
        else
        {
            westLongitude = longitude2;
            eastLongitude = longitude1;
        }

        synchronized (_safetyBoxLock)
        {
            _safetyBoxNWCorner = new LatLonAltPosition(northLatitude, westLongitude, Altitude.ZERO);
            AbsolutePosition safetyBoxNECorner = new LatLonAltPosition(northLatitude, eastLongitude, Altitude.POSITIVE_INFINITY);
            AbsolutePosition safetyBoxSWCorner = new LatLonAltPosition(southLatitude, westLongitude, Altitude.POSITIVE_INFINITY);
            _safetyBoxLatitudinalLength = _safetyBoxNWCorner.getRangeTo(safetyBoxSWCorner);
            _safetyBoxLongitudinalLength = _safetyBoxNWCorner.getRangeTo(safetyBoxNECorner);
        }
    }

    public void setInDrawSafetyBoxMode(boolean inDrawSafetyBoxMode)
    {
        synchronized (_safetyBoxLock)
        {
            _inDrawSafetyBoxMode = inDrawSafetyBoxMode;
        }
    }

    private boolean getInDrawSafetyBoxMode()
    {
        synchronized (_safetyBoxLock)
        {
            return _inDrawSafetyBoxMode;
        }
    }

    public void setSafetyBoxPanel(SafetyBoxPanel safetyBoxPanel)
    {
        _safetyBoxPanel = safetyBoxPanel;
    }

    public void setProposedSafetyBox(Latitude latitude1,
                                     Latitude latitude2,
                                     Longitude longitude1,
                                     Longitude longitude2)
    {
        if (latitude1 != null &&
            latitude2 != null &&
            longitude1 != null &&
            longitude2 != null)
        {
            Latitude northLatitude;
            Latitude southLatitude;
            Longitude westLongitude;
            Longitude eastLongitude;

            if (latitude1.isNorthOf(latitude2))
            {
                northLatitude = latitude1;
                southLatitude = latitude2;
            }
            else
            {
                northLatitude = latitude2;
                southLatitude = latitude1;
            }

            if (longitude1.isWestOf(longitude2))
            {
                westLongitude = longitude1;
                eastLongitude = longitude2;
            }
            else
            {
                westLongitude = longitude2;
                eastLongitude = longitude1;
            }

            synchronized (_safetyBoxLock)
            {
                _proposedSafetyBoxNWCorner = new LatLonAltPosition(northLatitude, westLongitude, Altitude.ZERO);
                AbsolutePosition safetyBoxNECorner = new LatLonAltPosition(northLatitude, eastLongitude, Altitude.POSITIVE_INFINITY);
                AbsolutePosition safetyBoxSWCorner = new LatLonAltPosition(southLatitude, westLongitude, Altitude.POSITIVE_INFINITY);
                _proposedSafetyBoxLatitudinalLength = _proposedSafetyBoxNWCorner.getRangeTo(safetyBoxSWCorner);
                _proposedSafetyBoxLongitudinalLength = _proposedSafetyBoxNWCorner.getRangeTo(safetyBoxNECorner);
            }
        }
        else
        {
            _proposedSafetyBoxNWCorner = null;
            _proposedSafetyBoxLatitudinalLength = null;
        }
    }

    //actionlistener interface

    public void actionPerformed(ActionEvent e) {
        try {

            if (e.getActionCommand().equals("saveSearchGoal")) {
                saveSearchGoalBelief();
                return;
            }
            if (e.getActionCommand().equals("loadSearchGoal")) {
                loadSearchGoalBelief();
                return;
            }
            if (e.getActionCommand().equals("saveNoGos")) {
                saveNoGos();
                return;
            }
            if (e.getActionCommand().equals("loadNoGos")) {
                loadNoGos();
                return;
            }

//           if (e.getSource() == showAltitudeButton)
//            {
//                int h = _display._MainSplitPane.getHeight();
//
//                if(altShowing)
//                {
//                    altShowing = false;
//                    showAltitudeButton.setText("Show Altitude");
//
//                    if(rnShowing)
//                    {
//                       if(h>rnDisplayPixels)
//                        _display._MainSplitPane.setDividerLocation(h-rnDisplayPixels);
//                        _display._SplitPane.setDividerLocation(0.0);
//                    }
//                    else
//                    {
//                        _display._MainSplitPane.setDividerLocation(1.0);
//
//                    }
//
//                }
//                else
//                {
//                    altShowing=true;
//                    showAltitudeButton.setText("Hide Altitude");
//
//                    if(rnShowing)
//                    {
//                        if (h > (altDisplayPixels + rnDisplayPixels))
//                        _display._MainSplitPane.setDividerLocation(h - altDisplayPixels - rnDisplayPixels );
//                        _display._SplitPane.setDividerLocation(altDisplayPixels);
//                    }
//                    else
//                    {
//                        if (h > altDisplayPixels)
//                        _display._MainSplitPane.setDividerLocation(h-altDisplayPixels);
//                        _display._SplitPane.setDividerLocation(altDisplayPixels);
//                    }
//                }
//                return;
//            }
//            if (e.getSource() == showRNButton)
//            {
//              int h = _display._MainSplitPane.getHeight();
//
//                if(rnShowing)
//                {
//                    rnShowing = false;
//                    showRNButton.setText("Show RN Histograms");
//
//                    if(altShowing)
//                    {
//                       if(h>altDisplayPixels)
//                       {
//                        _display._MainSplitPane.setDividerLocation(h-altDisplayPixels);
//                        _display._SplitPane.setDividerLocation(altDisplayPixels);
//                       }
//                    }
//                    else
//                    {
//                        _display._MainSplitPane.setDividerLocation(1.0);
//
//                    }
//
//                }
//                else
//                {
//                    rnShowing=true;
//                    showRNButton.setText("Hide RN Histograms");
//
//                    if(altShowing)
//                    {
//                        if (h > (altDisplayPixels + rnDisplayPixels))
//                        {
//                            _display._MainSplitPane.setDividerLocation(h - altDisplayPixels - rnDisplayPixels );
//                            _display._SplitPane.setDividerLocation(altDisplayPixels);
//                        }
//                    }
//                    else
//                    {
//                        if (h > rnDisplayPixels)
//                        {
//                            _display._SplitPane.setDividerLocation(0.0);
//                            _display._MainSplitPane.setDividerLocation(h-rnDisplayPixels);
//                        }
//
//
//                    }
//                }
//               return;
//            }

//            if (e.getSource() == gimbalTargetButton)
//            {
//                if(gimbalTargetButton.isSelected())
//                {
//                    interceptTargetButton.setSelected(false);
//                    CircularOrbitBelief cob = (CircularOrbitBelief)beliefManager.get(CircularOrbitBelief.BELIEF_NAME);
//                    if(cob != null && !cob.getUniqueAgentID().equals(WACSAgent.AGENTNAME))
//                    {
//
//                        cob = new CircularOrbitBelief(WACSAgent.AGENTNAME, cob.getPosition(), cob.getRadius(),cob.getIsClockwise());
//                        beliefManager.put(cob);
//
//                    }
//                }
//            }
//
//            if (e.getSource() == interceptTargetButton)
//            {
//                if(interceptTargetButton.isSelected())
//                {
//                    gimbalTargetButton.setSelected(false);
//                }
//                else
//                {
//                    CircularOrbitBelief cob = (CircularOrbitBelief)beliefManager.get(CircularOrbitBelief.BELIEF_NAME);
//                    if(cob != null && !cob.getUniqueAgentID().equals(WACSAgent.AGENTNAME))
//                    {
//
//                        cob = new CircularOrbitBelief(WACSAgent.AGENTNAME, cob.getPosition(), cob.getRadius(),cob.getIsClockwise());
//                        beliefManager.put(cob);
//
//                    }
//                }
//            }
/*
            if (e.getSource() == holdTrackReleaseButton)
            {
                //Put original hold track button back in
                m_ToolBar.remove(holdTrackReleaseButton);
                
                //how to release?
                releaseManualInterceptPosition ();
            }
*/
            if (e.getSource() == locateUavButton)
            {
                boolean success = false;
                AgentPositionBelief agPosBlf = (AgentPositionBelief)beliefManager.get(AgentPositionBelief.BELIEF_NAME);
                if (agPosBlf != null)
                {
                    PositionTimeName ptn = agPosBlf.getPositionTimeName(WACSAgent.AGENTNAME);
                    if (ptn != null)
                    {
                        setViewCenter (ptn.getPosition());
                        success = true;
                    }
                }
                
                if (!success)
                    JOptionPane.showMessageDialog(locateUavButton, "UAS Not Connected", "Error", JOptionPane.ERROR_MESSAGE);
            }


            if (e.getSource() == showAltitudeButton)
            {
                int h = _display._MainSplitPane.getHeight();

                if(altShowing)
                {
                    altShowing = false;
                    showAltitudeButton.setText("Show Altitude");

                    if(rnShowing)
                    {
                       if(h>rnDisplayPixels)
                       {
                        mainSplitPixels = (h-rnDisplayPixels);
                        splitPixels = 0;
                       }
                    }
                    else
                    {
                        mainSplitPixels = h;

                    }

                }
                else
                {
                    altShowing=true;
                    showAltitudeButton.setText("Hide Altitude");

                    if(rnShowing)
                    {
                        if (h > (altDisplayPixels + rnDisplayPixels))
                        {
                        mainSplitPixels = (h - altDisplayPixels - rnDisplayPixels );
                        splitPixels = altDisplayPixels;
                        }
                    }
                    else
                    {
                        if (h > altDisplayPixels)
                        {
                            mainSplitPixels = h-altDisplayPixels;
                            splitPixels = altDisplayPixels;
                        }
                    }
                }
                return;
            }
            if (e.getSource() == showRNButton)
            {
              int h = _display._MainSplitPane.getHeight();

                if(rnShowing)
                {
                    rnShowing = false;
                    showRNButton.setText("Show RN Histograms");

                    if(altShowing)
                    {
                       if(h>altDisplayPixels)
                       {
                        mainSplitPixels = h-altDisplayPixels;
                        splitPixels = altDisplayPixels;
                       }
                    }
                    else
                    {
                        mainSplitPixels = h;

                    }

                }
                else
                {
                    rnShowing=true;
                    showRNButton.setText("Hide RN Histograms");

                    if(altShowing)
                    {
                        if (h > (altDisplayPixels + rnDisplayPixels))
                        {
                            mainSplitPixels = (h - altDisplayPixels - rnDisplayPixels );
                            splitPixels = altDisplayPixels;
                        }
                    }
                    else
                    {
                        if (h > rnDisplayPixels)
                        {
                            
                           mainSplitPixels = h-rnDisplayPixels;
                           splitPixels = 0;
                        }

                        
                    }
                }
               return;
            }
            else if (e.getSource() == showShadowDataButton)
            {
                if (m_shadowDataForm == null)
                {
                    m_shadowDataForm = new ShadowDataForm(ShadowAutopilotInterface.s_instance);
                }

                m_shadowDataForm.setVisible(!m_shadowDataForm.isVisible());
            }

            else if (e.getSource() == rulerButton)
            {
                setRulerActivated (rulerButton.isSelected());
                update();
            }


            if (e.getSource() == m_UnitsMenuItem)
            {
                DisplayUnitsManager.getInstance().showDialog();
                return;
            }
            else if (e.getSource() == m_WindEstimationMenuItem)
            {
                if (m_WindPanel == null)
                {
                    m_WindPanel = new WindEstimationPanel(null, false, beliefManager);
                }
                m_WindPanel.setVisible(true);
            }
            else if (e.getSource() == m_AglDisplayMenuItem)
            {
                if (m_AglPanel == null)
                {
                    javax.swing.JFrame testFrame = new javax.swing.JFrame();
                    testFrame.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                    testFrame.setLocation(200, 200);
                    JPanel aglPanel = new AglDisplay(beliefManager);
                    testFrame.add(aglPanel);
                    testFrame.pack();
                    m_AglPanel = testFrame;
                }
                m_AglPanel.setVisible(true);
            }
            
            if (e.getSource() == finishButton) {
                if (noGoRegionButton.isSelected()) {
                    if (_points != null && _points.size() > 2) {
                        AbsolutePosition[] points = new AbsolutePosition[_points.size()];
                        for (int i = 0; i < points.length; i++) {
                            points[i] = (AbsolutePosition) _points.get(i);
                        }
                        Region reg = new Region(points);

                        //NJA
                        for (int x = 0; x < resetGoalMatrix.getXSize(); x++) {
                            for (int y = 0; y < resetGoalMatrix.getYSize(); y++) {
                                AbsolutePosition elementPosition = resetGoalMatrix.getPosition(x, y);
                                if (resetGoalMatrix.get(x, y) == 1 && reg.contains(elementPosition)) {
                                    resetGoalMatrix.set(x, y, -1);
                                }
                            }
                        }

                        NoGoBelief bel = NoGoBelief.createBelief(agentID, reg.toString(), reg);
                        beliefManager.put(bel);
                    }
                    _points = null;
                    _showLines = false;
                }
            }



            if (e.getActionCommand().equals("configTable")) {
                getConfigTableDialog().setVisible(true);
            } else if (e.getActionCommand().equals("targetSelection") && classifications != null) {
                String targetID = (String) targetComboBox.getSelectedItem();
                ClassificationBelief cBelief =
                        (ClassificationBelief) beliefManager.get(
                        ClassificationBelief.BELIEF_NAME);
                if (cBelief != null) {
                    ClassificationTimeName ctn = cBelief.getClassificationTimeName(targetID);

                    if (ctn != null) {
                        int i = classifications.indexOf(classificationToString(ctn.getClassification()));
                        if (i != -1) {
                            classificationComboBox.setSelectedIndex(i);
                        }
                    }
                }
            } else if (e.getActionCommand().equals("classification")) {
                getClassificationDialog().setVisible(true);
            } else if (e.getActionCommand().equals("okClassification")) {
                // TODO for _diety's sake, move me elsewhere
                String classificationStr = (String) classificationComboBox.getSelectedItem();
                int classification = Classification.DEFAULT;
                if (classificationStr.equals("unknown")) {
                    classification = Classification.UNKNOWN;
                } else if (classificationStr.equals("target")) {
                    classification = Classification.VEHICLE_TARGET;
                } else if (classificationStr.equals("friendly")) {
                    classification = Classification.FRIENDLY;
                } else if (classificationStr.equals("asset")) {
                    classification = Classification.ASSET;
                } else if (classificationStr.equals("HVA")) {
                    classification = Classification.HVA;
                } else if (classificationStr.equals("Neutralized")) {
                    classification = Classification.NEUTRALIZED;
                }

                String targetID = (String) targetComboBox.getSelectedItem();
                TargetActualBelief targetBel = (TargetActualBelief) beliefManager.get(TargetActualBelief.BELIEF_NAME);
                PositionTimeName ptn = targetBel.getPositionTimeName(targetID);

                if (ptn != null)
                {
                    //Logger.getLogger("GLOBAL").info("Setting classification of " + targetID + " to " + classificationStr);
                    beliefManager.put(new ClassificationBelief(agentID,
                            ptn.getName(), classification));
                }

                getClassificationDialog().setVisible(false);
            } else if (e.getActionCommand().equals("cancelClassification")) {
                getClassificationDialog().setVisible(false);
            } else if (e.getActionCommand().equals("config")) {
                getConfigDialog().setVisible(true);
            } else if (e.getActionCommand().equals("okConfig")) {
                /*ConfigProperty prop = null;
                if (appliesToField.getText().trim().equals("")) {
                    prop = new ConfigProperty(propertyField.getText(),
                            valueField.getText());
                } else {
                    prop = new ConfigProperty(propertyField.getText(),
                            valueField.getText(), appliesToField.getText());
                }
                remove
                beliefManager.put(new ConfigBelief(agentID, prop));
                getConfigDialog().setVisible(false);*/
            } else if (e.getActionCommand().equals("cancelConfig")) {
                getConfigDialog().setVisible(false);
            } else if (e.getActionCommand().equals("clearGlobalBeliefs")) {
                /*remove as is, replace later
                ConfigBelief cb = new ConfigBelief(agentID, new ConfigProperty(
                        "belief.earliestBeliefDate", "" + System.currentTimeMillis()));
                beliefManager.put(cb);*/
            } else if (lockButton.isSelected() && goalChanged) {
                createGoal();
                stopDefiningSimulatedCloud();
            } else if (goalButton.isSelected() || noGoButton.isSelected()) {
                //only clear matrix if we are coming off a lock
                if (lockSelected) {
                    clearGoalMatrix();
                    lockSelected = false;
                } else if (lockButton.isSelected()) {
                    createGoal();
                } else if (goalButton.isSelected() || noGoButton.isSelected()) {
                    //only clear matrix if we are coming off a lock
                    if (lockSelected) {
                        clearGoalMatrix();
                        lockSelected = false;
                    }
                } else if (addButton.isSelected()) {
                    if (lockSelected) {
                        lockSelected = false;
                    }
                } else if (e.getSource() == showDirection) {
                    redrawBearingBelief = true;
                    jgeoRepaint();
                } else {
                    // TODO repaint if something changes? has to be specific now that we
                    // use all the flags
                    //jgeoRepaint();
                }
            }
            else if (e.getActionCommand().equals ("Show Names"))
            {
                showNames (namesButton.isSelected());
            }
            else if (e.getActionCommand().equals ("Show Cloud"))
            {
                showCloud (showCloud.isSelected());
            }
            else if (e.getActionCommand().equals ("Show Bearing"))
            {
                showDirection (showDirection.isSelected());
            }
            else if (e.getActionCommand().equals ("Show Position History"))
            {
                showHistory (showHistory.isSelected());
            }
            else if (e.getActionCommand().equals ("Show Wind"))
            {
                showWind (showWind.isSelected());
            }
            else if(e.getActionCommand().equals("Add Simulated Cloud")){
                startDefiningSimulatedCloud();
            }else if(e.getActionCommand().equals("keepOutRegion")){
                if(definingKeepOutRegion){
                    stopDefiningKeepOutRegion();
                }else{
                    startDefiningKeepOutRegion();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void startDefiningKeepOutRegion()
    {
        definingKeepOutRegion = true;
    }
    
    public void stopDefiningKeepOutRegion() throws Exception
    {
        definingKeepOutRegion = false;
                    
        //create belief, clear list

        if(_points != null && _points.size()>2){

            LatLonAltPosition[] vertices = ((LinkedList<LatLonAltPosition>)_points).toArray(new LatLonAltPosition[_points.size()]);
            PolygonRegion region = new PolygonRegion(vertices);
            //region = region.convexHull(vertices);
            
            KeepOutRegionBelief belief = (KeepOutRegionBelief)beliefManager.get(KeepOutRegionBelief.BELIEF_NAME);

            if(belief == null){
                ArrayList<PolygonRegion> regions = new ArrayList<PolygonRegion>();
                regions.add(region);
                belief = new KeepOutRegionBelief(regions);

            }else{
                //ArrayList<PolygonRegion> regions = new ArrayList<PolygonRegion>();
                belief.getRegions().add(region);
            }
            belief.updateTimeStamp();
            beliefManager.put(belief);
            _showLines = false;
        }else{
            //TODO appropriate error message
        }
        _points = null;
    }
    
    public void startDefiningSimulatedCloud ()
    {
        m_AddSimulatedCloud = true;
    }
    
    public void stopDefiningSimulatedCloud ()
    {
        m_AddSimulatedCloud = false;
        createCloud();
    }
    
    public void showNames (Boolean show)
    {
        m_ShowNamesOpt = show;
    }
    
    public void showCloud (Boolean show)
    {
        m_ShowCloudOpt = show;
    }
    
    public void showNextOrbit (Boolean show)
    {
        m_ShowNextOrbitOpt = show;
    }
    
    public void showDirection (Boolean show)
    {
        m_ShowBearingOpt = show;
    }
    
    public void showWind (Boolean show)
    {
        m_ShowWindOpt = show;
    }
    
    public void showHistory (Boolean show)
    {
        m_ShowHistoryOpt = show;
    }
    
    public void showKeepOut (Boolean show)
    {
        m_ShowKeepOutOpt = show;
    }
    
    public void showSafetyBox (Boolean show)
    {
        m_ShowSafetyBoxOpt = show;
    }
    
    public void showLocationBubble (Boolean show)
    {
        m_ShowLocationBubbleOpt = show;
    }
    
    public void showScale (Boolean show)
    {
        m_ShowScaleOpt = show;
        m_PaintImmediately = true;
    }
    
    public void showMouseInfo (Boolean show)
    {
        m_ShowMouseInfoOpt = show;
    }
    
    public void showWindCompass (Boolean show)
    {
        m_ShowWindCompassOpt = show;
    }

    @Override
    public void unitsChanged() {
        if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
            setRulerUnits(SearchCanvas.FEET);
        else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.RANGE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
            setRulerUnits(SearchCanvas.METERS);
        
        update();
    }

    private void createSafeTargetLocation(LatLonAltPosition pos)
    {
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        double agl = Config.getConfig().getPropertyAsDouble("SearchCanvas.gimbalDefaultAGL.Meters", 0.0);

        int clas = Config.getConfig().getPropertyAsInteger(
                        "WACSAgent.gimbalTargetClassification", Classification.ASSET);

        //check if we can loiter (loiter radius) around target.  Move target if not safe
        /*currently not moving gimbal target inside safety box
        Length radius = m_LoiterRadius;
        if (radius == null)
            radius = new Length (Config.getConfig().getPropertyAsDouble("WacsSettingsDefaults.loiterRad_m", 1000.0), Length.METERS);
        LatLonAltPosition oldPos = pos;
        pos = _safetyBox.getSafeOrbitCenterPosition (pos, radius, true);*/

        Altitude dtedAlt = new Altitude(agl + DtedGlobalMap.getDted().getAltitudeMSL(pos.getLatitude().getDoubleValue(Angle.DEGREES),
                pos.getLongitude().getDoubleValue(Angle.DEGREES)), Length.METERS);
        
            pos = new LatLonAltPosition(pos.getLatitude(), pos.getLongitude(), dtedAlt);
            
            beliefManager.put(new TargetCommandedBelief(agentID,
                    pos,
                    Length.ZERO,
                    tmp));
            beliefManager.put(new ClassificationBelief(agentID,
                    tmp, clas));
    }
    
    private void releaseManualInterceptPosition ()
    {
        //Release
        ManualInterceptCommandedOrbitBelief newBelief = new ManualInterceptCommandedOrbitBelief (beliefManager.getName(), true);
        beliefManager.put(newBelief);
    }
    
    private void verifyNewManualInterceptPosition (LatLonAltPosition interceptPosition, boolean forceHoldPosition)
    {
        //Not checking safety of manual intercept position.  Relying on flight control to do that, I suppose
        ManualInterceptCommandedOrbitBelief newBelief = new ManualInterceptCommandedOrbitBelief (beliefManager.getName(), interceptPosition.getLatitude(), interceptPosition.getLongitude(), forceHoldPosition);
        beliefManager.put(newBelief);
    }

    private void verifyNewRacetrackDefinitionBelief(LatLonAltPosition loiterGhostPosition)
    {
        //Circular racetrack orbit, which means offset orbit with approach path.

        //Target needs to be at least "contact point distance" away from edge of orbit edge.  This would put contact point
        //on the radius.  Should be further away than that, so add a buffer factor, too
        //Or, if target is within orbit radius, let's center orbit on target automatically
        TargetActualBelief targets = (TargetActualBelief)beliefManager.get(TargetActualBelief.BELIEF_NAME);
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        if (targets != null && targets.getPositionTimeName(tmp) != null)
        {
            LatLonAltPosition gimTargPos = targets.getPositionTimeName(tmp).getPosition().asLatLonAltPosition();
            Length rangeFromCenterToTarget = gimTargPos.getRangeTo(loiterGhostPosition);

            //Length contactDistanceM = new Length (Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.ContactDistanceFromTarget.Meters", 1500), Length.METERS);
            Length firstRangeDistanceM = new Length (Config.getConfig().getPropertyAsInteger("ShadowDriver.RacetrackLoiter.FirstRangeDistanceFromTarget.Meters", 2500), Length.METERS);
            if (rangeFromCenterToTarget.isLessThan (m_LoiterRadius))
            {
                //Gimbal target is within loiter orbit.  Snap center of loiter to gimbal target
                JOptionPane.showMessageDialog(this, "Gimbal target and loiter center have been aligned!", "Warning", JOptionPane.WARNING_MESSAGE);
                RacetrackDefinitionCommandedBelief rtBlf = new RacetrackDefinitionCommandedBelief (gimTargPos);
                beliefManager.put(rtBlf);
                return;
            }
            else if(rangeFromCenterToTarget.isLessThan(m_LoiterRadius.plus(firstRangeDistanceM)))
            {
                //Gimbal target and loiter area are too close.  Show pop-up window and don't send it
                JOptionPane.showMessageDialog(this, "Gimbal target and loiter center and too close!  Loiter not updated!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        RacetrackDefinitionCommandedBelief rtBlf = new RacetrackDefinitionCommandedBelief (loiterGhostPosition);
        beliefManager.put(rtBlf);
    }




    /*
    public class RegionMenu extends JPopupMenu implements ActionListener {
    BeliefManager _belMgr;
    SearchCanvas _owner;
    String _name; //region name
    FalconviewRegion _falconviewRegion;

    JMenuItem _selectRegion;
    JMenuItem _deSelectRegion;

    public RegionMenu(SearchCanvas owner, BeliefManager belMgr, FalconviewRegion fr) {
    _belMgr = belMgr;
    _owner = owner;
    _selectRegion = new JMenuItem("Select Region");
    _deSelectRegion = new JMenuItem("De-Select Region");
    this.add(_selectRegion);
    this.add(_deSelectRegion);
    _selectRegion.addActionListener(this);
    _deSelectRegion.addActionListener(this);
    _falconviewRegion = fr;
    }


    public void show(int x, int y, String name) {
    _name = name;
    show(_owner, x, y);
    }

    public void actionPerformed(ActionEvent e) {
    if (e.getSource() == _selectRegion) {
    RegionBelief rb = (RegionBelief)_belMgr.get(RegionBelief.BELIEF_NAME);
    if (rb == null)
    return;
    rb.setSelected(_name);
    PrimitiveTypeGeocentricMatrix m = rb.getSearchGoal(_name, resetGoalMatrix);
    SearchGoalBelief sgb;
    if (m != null) {
    sgb = new SearchGoalBelief(WACSDisplayAgent.AGENTNAME, m);
    }
    else {
    sgb = new SearchGoalBelief(WACSDisplayAgent.AGENTNAME, resetGoalMatrix);
    }

    // send the region to falconview
    _falconviewRegion.sendRegion(_name, rb);

    _belMgr.put(sgb);
    }
    else if (e.getSource() == _deSelectRegion) {
    ((RegionBelief)_belMgr.get(RegionBelief.BELIEF_NAME)).setSelected(null);
    }
    }
    }
     */

    private void paintGradientCircles(JGeoGraphics jg, LinkedList<LatLonAltPosition> llaList, Color centerColor, Length circleRadius) 
    {
        for (LatLonAltPosition lla : llaList)
        {
            Point2D point = (Point2D)jg.getViewTransform().earthToMouse(lla);
            int radius = jg.getViewTransform().getLengthInPixels(circleRadius);
            float fracs[] = {0f, 1f};
            Color colors[] = new Color [2];
            colors[0] = centerColor;
            colors[1] = new Color (centerColor.getRed(), centerColor.getGreen(), centerColor.getBlue(), 0);

            RadialGradientPaint painter = new RadialGradientPaint(point, radius, fracs, colors);
            Paint oldPaint = jg.getPaint();
            jg.setPaint (painter);
            jg.fillOval(lla, circleRadius, circleRadius);
            jg.setPaint(oldPaint);
        }
    }
    
    public void updateEtdMinMaxDisplayValues(float minValue, float maxValue) {
        _EtdMinDisplayValue = minValue;
        _EtdMaxDisplayValue = maxValue;
    }
    
    private Color getColorForValue(float value) {
        if (value < _EtdMinDisplayValue) {
            value = _EtdMinDisplayValue;
        }
        
        if (value > _EtdMaxDisplayValue) {
            value = _EtdMaxDisplayValue;
        }
        
        float hue = MIN_HUE + (MAX_HUE - MIN_HUE) * (value - _EtdMinDisplayValue) / (_EtdMaxDisplayValue - _EtdMinDisplayValue) ;        
        return new Color(Color.HSBtoRGB(hue/360.0f, 1.0f, 1.0f));
    }
  
} // end class SearchDisplay 
 
 