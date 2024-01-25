/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.RigidSplitDock;
import com.javadocking.dock.SingleDock;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.WacsFloatDock;
import com.javadocking.dock.factory.SingleDockFactory;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockingMode;
import com.javadocking.drag.DraggerFactory;
import com.javadocking.drag.WacsStaticDraggerFactory;
import com.javadocking.drag.painter.CompositeDockableDragPainter;
import com.javadocking.drag.painter.DefaultRectanglePainter;
import com.javadocking.drag.painter.DockableDragPainter;
import com.javadocking.drag.painter.SwDockableDragPainter;
import com.javadocking.drag.painter.WacsImageDockableDragPainter;
import com.javadocking.drag.painter.WindowDockableDragPainter;
import com.javadocking.model.DockModel;
import com.javadocking.model.FloatDockModel;
import com.javadocking.model.WacsFloatDockFactory;
import com.javadocking.model.codec.DockModelPropertiesDecoder;
import com.javadocking.model.codec.DockModelPropertiesEncoder;
import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import edu.jhuapl.jlib.jgeo.action.MercatorAction;
import edu.jhuapl.jlib.jgeo.action.OrthographicAction;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.cbrnPods.bridgeportProcessor;
import edu.jhuapl.nstd.swarm.UpdateManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface;
import edu.jhuapl.nstd.swarm.belief.AlphaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.GammaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import static edu.jhuapl.nstd.swarm.display.SearchDisplay.NUM_LEVELS;
import edu.jhuapl.nstd.swarm.display.docking.BottomDockManager;
import edu.jhuapl.nstd.swarm.display.docking.LeftSideDockManager;
import edu.jhuapl.nstd.swarm.display.docking.RightSideDockManager;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

/**
 *
 * @author humphjc1
 */
public class GcsDisplay extends javax.swing.JFrame implements Updateable
{
    protected BeliefManagerWacs m_BeliefManager;
    protected AgentTracker agentTracker;
    
    protected RightSideDockManager m_rightSideDockManager;
    protected BottomDockManager m_bottomDockManager;
    protected LeftSideDockManager m_leftSideDockManager;
    protected JPanel m_ButtonToolbar;
    protected JPanel m_TelemetryBar;
    protected JPanel m_MainSinglePanel;
    
    protected JMenuBar m_MenuBar;
    //protected JMenu m_WindowMenu;
    protected JMenu m_DisplayMenu;
    //protected JMenu m_MissionMenu;
    
    protected SearchCanvas m_SearchCanvasPanel;
    protected WACSSettingsPanel m_UavControlPanel = null;
    protected WACSControlPanel m_SensorControlPanel = null;
    protected MissionManagerPanel m_MainControlPanel = null;
    //protected ManualControlPanel m_ManualControlPanel = null;
    protected AdvancedSettingsPanel m_AdvancedSettingsPanel = null;
    protected MissionStatusErrorsPanel m_MissionErrorsPanel = null;
    protected SensorSummaryStopLightPanel m_SensorSumaryStopLights = null;
    protected Nbc1ReportPanel m_Nbc1ReportPanel = null;
    protected SatCommStatusPanel m_SatCommStatusPanel = null;
    protected UavAutopilotPanel m_UavAutopilotPanel = null;
    protected ZeroAirDataPanel m_ZeroAirDataPanel = null;
    protected SystemStatusPanel m_SystemStatusPanel = null;
    protected ExplosionTimePanel m_ExplosionTimePanel = null;
    
    protected SafetyBoxPanel m_SafetyBoxPanel = null;
    protected long _safetyBoxTimestamp = 0;
    
//    protected RNHistogramDisplayGraphPanel m_GammaGraphPanel;
//    protected RNHistogramDisplayGraphPanel m_AlphaGraphPanel;
    protected AltitudeChartPanel m_AltitudeGraphPanel;
    protected EtdPanel m_EtdPanel;
    protected EtdTempPanel m_EtdTempPanel;
    protected EtdHistoryPanel m_EtdHistoryPanel;
    protected EtdErrorsPanel m_EtdErrorsPanel;
    protected IbacChartPanel m_IbacGraphPanel;
    protected AnacondaChartPanel m_AnacondaGraphPanel;
    protected BridgeportChartPanel m_BridgeportGraphPanel;
    protected CanberraChartPanel m_CanberraGraphPanel;
    protected AlphaChartPanel m_AlphaRateGraphPanel;
    protected BetaChartPanel m_BetaRateGraphPanel;
    protected GenericHistogramPanel m_GammaSpectrumPanel;
    protected GenericHistogramPanel m_AlphaSpectrumPanel;
    protected RNTotalCountsTracker gammaTotalCountsTracker;
    protected RNTotalCountsTracker alphaTotalCountsTracker;
    protected boolean accumlatingGammaBackground = false;
    protected int m_GammaCountsToAverageTracker;
    protected int m_GammaStdDevLimitTracker;
    protected int m_GammaMinimumCountsTracker;
    protected int m_AlphaCountsToAverageTracker;
    protected int m_AlphaStdDevLimitTracker;
    protected int m_AlphaMinimumCountsTracker;
    protected Date ghprevTime;
    protected Date ahprevTime;
    protected Date adprevTime;
    protected Date gdprevTime;
    
    public static final String DOCKMODEL_SOURCE = "wacsFloatDockModelSource.dck";
    private static final String MAINFRAME_ID = "GcsDisplayMainFrame";
    
    public static final String STRIKE_TIME_TEXT = "Strike Time";
    public static final String ZERO_AIR_DATA_TEXT = "Zero Air Data";
    
    
    /**
     * Creates new form GcsDisplay
     */
    public GcsDisplay(BeliefManagerWacs belMgr) 
    {
        m_BeliefManager = belMgr;
        agentTracker = new AgentTracker(m_BeliefManager);
        
        m_GammaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaCountsToAverageTracker", 5);
        m_GammaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaStdDevLimitTracker", 3);
        m_GammaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaMinimumCountsTracker", 100);
        gammaTotalCountsTracker = new RNTotalCountsTracker(m_GammaCountsToAverageTracker, m_GammaStdDevLimitTracker, m_GammaMinimumCountsTracker);
        
        m_AlphaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaCountsToAverageTracker", 5);
        m_AlphaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaStdDevLimitTracker", 3);
        m_AlphaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaMinimumCountsTracker", 10);
        alphaTotalCountsTracker = new RNTotalCountsTracker(m_AlphaCountsToAverageTracker, m_AlphaStdDevLimitTracker, m_AlphaMinimumCountsTracker);
        
        initializeDockableDisplay ();
    }

    @Override
    public void update() 
    {
        //update the whole display
        
        try
        {
            if (m_SearchCanvasPanel != null)
            {
                m_SearchCanvasPanel.updateEtdMinMaxDisplayValues(m_EtdPanel._minEtdDisplayValue, m_EtdPanel._maxEtdDisplayValue);
                m_SearchCanvasPanel.update();
            }
            
            //
            // TODO: Update the safety box if it has changed
            //
            SafetyBoxBelief safetyBoxBelief = (SafetyBoxBelief)m_BeliefManager.get(SafetyBoxBelief.BELIEF_NAME);
            if (safetyBoxBelief != null &&
                (safetyBoxBelief.getTimeStamp().getTime() > _safetyBoxTimestamp || m_SafetyBoxPanel.unitsHaveChanged()))
            {
                m_SearchCanvasPanel.setSafetyBox(new Latitude(safetyBoxBelief.getLatitude1_deg(), Angle.DEGREES),
                                    new Latitude(safetyBoxBelief.getLatitude2_deg(), Angle.DEGREES),
                                    new Longitude(safetyBoxBelief.getLongitude1_deg(), Angle.DEGREES),
                                    new Longitude(safetyBoxBelief.getLongitude2_deg(), Angle.DEGREES));

                m_SafetyBoxPanel.setSafetyBoxBelief(safetyBoxBelief);

                _safetyBoxTimestamp = safetyBoxBelief.getTimeStamp().getTime();
            }
            
            if(m_SensorControlPanel != null)
            {
                m_SensorControlPanel.updateLabels();
            }
            
            if(m_UavControlPanel != null)
            {
                m_UavControlPanel.updateLabels();
            }
            
//            if (m_GammaGraphPanel != null)
//            {
//                GammaCompositeHistogramBelief ghbel = (GammaCompositeHistogramBelief) m_BeliefManager.get(GammaCompositeHistogramBelief.BELIEF_NAME);
//
//                if((ghbel != null && ghprevTime == null) ||(ghbel != null && ghprevTime!=null && ghbel.getTimeStamp().after(ghprevTime)))
//                {
//                    ghprevTime = ghbel.getTimeStamp();
//                    RNHistogram data = new RNHistogram(ghbel.getHistogramData());
//                    m_GammaGraphPanel.setLiveTime((int)(ghbel.getLiveTime()/1000));
//                    m_GammaGraphPanel.setStatMessage("Total Counts = " + ghbel.getSpectraCount());
//                    m_GammaGraphPanel.updateCurrentHistogram(data);
//
//
//                    if (!accumlatingGammaBackground)
//                    {
//                        String countsAlertMessage = gammaTotalCountsTracker.getCountsAlertMessage(ghbel.getSpectraCount());
//                        m_GammaGraphPanel.setStatAlertMessage(countsAlertMessage);
//                    }
//                }
//
//                GammaDetectionBelief gdbel = (GammaDetectionBelief)m_BeliefManager.get(GammaDetectionBelief.BELIEF_NAME);
//                if((gdbel != null && gdprevTime == null) ||(gdbel != null && gdprevTime!=null && gdbel.getTimeStamp().after(gdprevTime)))
//                {
//                    gdprevTime = gdbel.getTimeStamp();
//                     
//                    if (gdbel.getGammaDetections().startsWith(bridgeportProcessor.accumBackgroundMsg))
//                        accumlatingGammaBackground = true;
//                    else
//                        accumlatingGammaBackground = false;
//
//                    m_GammaGraphPanel.setDetectionMessage(gdbel.getGammaDetections());
//                    m_GammaGraphPanel.repaint();
//                }
//            }
//
//            if (m_AlphaGraphPanel != null)
//            {
//                AlphaCompositeHistogramBelief ahbel = (AlphaCompositeHistogramBelief) m_BeliefManager.get(AlphaCompositeHistogramBelief.BELIEF_NAME);
//
//                if((ahbel != null && ahprevTime == null) ||(ahbel != null && ahprevTime!=null && ahbel.getTimeStamp().after(ahprevTime)))
//                {
//                    ahprevTime = ahbel.getTimeStamp();
//                    RNHistogram data = new RNHistogram(ahbel.getHistogramData());
//                    m_AlphaGraphPanel.setLiveTime((int)(ahbel.getLiveTime()/1000));
//                    m_AlphaGraphPanel.setStatMessage("Total Counts = " + ahbel.getSpectraCount());
//                    m_AlphaGraphPanel.updateCurrentHistogram(data);
//
//                    String countsAlertMessage = alphaTotalCountsTracker.getCountsAlertMessage(ahbel.getSpectraCount());
//                                    m_AlphaGraphPanel.setStatAlertMessage(countsAlertMessage);
//                }
//
//                AlphaDetectionBelief adbel = (AlphaDetectionBelief)m_BeliefManager.get(AlphaDetectionBelief.BELIEF_NAME);
//                if((adbel != null && adprevTime == null) ||(adbel != null && adprevTime!=null && adbel.getTimeStamp().after(adprevTime)))
//                {
//                    adprevTime = adbel.getTimeStamp();
//                     
//                    m_AlphaGraphPanel.setDetectionMessage(adbel.getAlphaDetections());
//                    m_AlphaGraphPanel.repaint();
//                }
//            }

            if (m_AltitudeGraphPanel != null)
            {
                m_AltitudeGraphPanel.update();
            }
            
            if(m_EtdPanel != null)
            {
                m_EtdPanel.update();
            }
            
            if(m_EtdTempPanel != null) {
                m_EtdTempPanel.update();
            }
            
            if(m_EtdHistoryPanel != null)
            {
                m_EtdHistoryPanel.update();
            }
            
            if(m_EtdErrorsPanel !=null)
            {
                m_EtdErrorsPanel.update();
            }
            
            if (m_IbacGraphPanel != null)
            {
                m_IbacGraphPanel.update();
            }
            
            if (m_AnacondaGraphPanel != null)
            {
                m_AnacondaGraphPanel.update();
            }
            
            if (m_BetaRateGraphPanel != null)
            {
                m_BetaRateGraphPanel.update();
            }
            
            if (m_BridgeportGraphPanel != null)
            {
                m_BridgeportGraphPanel.update();
            }
            
            if (m_CanberraGraphPanel != null)
            {
                m_CanberraGraphPanel.update();
            }
            
            if (m_AlphaRateGraphPanel != null)
            {
                m_AlphaRateGraphPanel.update();
            }
            
            if (m_GammaSpectrumPanel != null)
            {
                m_GammaSpectrumPanel.update();
                
//                GammaCompositeHistogramBelief ghbel = (GammaCompositeHistogramBelief) m_BeliefManager.get(GammaCompositeHistogramBelief.BELIEF_NAME);
//
//                if((ghbel != null && ghprevTime == null) ||(ghbel != null && ghprevTime!=null && ghbel.getTimeStamp().after(ghprevTime)))
//                {
//                    ghprevTime = ghbel.getTimeStamp();
//                    m_GammaSpectrumPanel.setLiveTime((int)(ghbel.getLiveTime()/1000));
//                    m_GammaSpectrumPanel.setStatsMessage("Total Counts = " + ghbel.getSpectraCount());
//
//                    if (!accumlatingGammaBackground)
//                    {
//                        String countsAlertMessage = gammaTotalCountsTracker.getCountsAlertMessage(ghbel.getSpectraCount());
//                        m_GammaSpectrumPanel.setStatsAlert(countsAlertMessage);
//                    }
//                }
//
//                GammaDetectionBelief gdbel = (GammaDetectionBelief)m_BeliefManager.get(GammaDetectionBelief.BELIEF_NAME);
//                if((gdbel != null && gdprevTime == null) ||(gdbel != null && gdprevTime!=null && gdbel.getTimeStamp().after(gdprevTime)))
//                {
//                    gdprevTime = gdbel.getTimeStamp();
//                     
//                    if (gdbel.getGammaDetections().startsWith(bridgeportProcessor.accumBackgroundMsg))
//                        accumlatingGammaBackground = true;
//                    else
//                        accumlatingGammaBackground = false;
//
//                    m_GammaSpectrumPanel.setDetectionMessage(gdbel.getGammaDetections());
//                }
            }
            
            if (m_AlphaSpectrumPanel != null)
            {
                m_AlphaSpectrumPanel.update();
                
//                AlphaCompositeHistogramBelief ahbel = (AlphaCompositeHistogramBelief) m_BeliefManager.get(AlphaCompositeHistogramBelief.BELIEF_NAME);
//
//                if((ahbel != null && ahprevTime == null) ||(ahbel != null && ahprevTime!=null && ahbel.getTimeStamp().after(ahprevTime)))
//                {
//                    ahprevTime = ahbel.getTimeStamp();
//                    m_AlphaSpectrumPanel.setLiveTime((int)(ahbel.getLiveTime()/1000));
//                    m_AlphaSpectrumPanel.setStatsMessage("Total Counts = " + ahbel.getSpectraCount());
//
//                    String countsAlertMessage = alphaTotalCountsTracker.getCountsAlertMessage(ahbel.getSpectraCount());
//                    m_AlphaSpectrumPanel.setStatsAlert(countsAlertMessage);
//                }
//
//                AlphaDetectionBelief adbel = (AlphaDetectionBelief)m_BeliefManager.get(AlphaDetectionBelief.BELIEF_NAME);
//                if((adbel != null && adprevTime == null) ||(adbel != null && adprevTime!=null && adbel.getTimeStamp().after(adprevTime)))
//                {
//                    adprevTime = adbel.getTimeStamp();
//                     
//                    m_AlphaSpectrumPanel.setDetectionMessage(adbel.getAlphaDetections());
//                }
            }
            
            if (agentTracker != null)
            {
                agentTracker.update();
            }
            
            if (m_MainControlPanel != null)
            {
                m_MainControlPanel.update();
            }
            
            /*
            if (m_ManualControlPanel != null)
            {
                m_ManualControlPanel.update();
            }
            */
            
            if (m_AdvancedSettingsPanel != null)
            {
                m_AdvancedSettingsPanel.update();
            }
            
            if (m_MissionErrorsPanel != null)
            {
                m_MissionErrorsPanel.update();
            }
            
            if (m_SensorSumaryStopLights != null)
            {
                m_SensorSumaryStopLights.update();
            }
            
            if (m_Nbc1ReportPanel != null)
                m_Nbc1ReportPanel.update();
            
            if (m_SatCommStatusPanel != null)
                m_SatCommStatusPanel.update();
            
            if (m_UavAutopilotPanel != null)
                m_UavAutopilotPanel.update();
            
            if (m_ZeroAirDataPanel != null)
                m_ZeroAirDataPanel.update();
            
            if (m_SystemStatusPanel != null)
                m_SystemStatusPanel.update();
            
            if (m_ExplosionTimePanel != null)
                m_ExplosionTimePanel.update();
        }
        catch(Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
              e.printStackTrace();
        }
    }
    
    
    private void initializeDockableDisplay() 
    {
        //Create top Menu
        m_MenuBar = new JMenuBar();
        setJMenuBar(m_MenuBar);
        //m_WindowMenu = new JMenu("Windows");
        
        //Create the dock model
        FloatDockModel dockModel = null;
        RigidSplitDock graphSplit = null;
        SplitDock mainSplit = null;
        boolean dockModelInitialized = false;
        
        //Center panel for map canvas
        m_SearchCanvasPanel = createCanvas (m_BeliefManager, this);
        final Dockable centerDockable = new DefaultDockable("centerDockableId", m_SearchCanvasPanel, null, null, DockingMode.LEFT);
        
        //Right side for accessory panels
        m_rightSideDockManager = new RightSideDockManager();
        populateRightSideDock();
        
        //Bottom for graphs
        m_bottomDockManager = new BottomDockManager();
        populateBottomDock();
        
        //Left side for main controls
        m_leftSideDockManager = new LeftSideDockManager();
        populateLeftSideDock();
        
        //Single dockables to add toolbars, button bars, etc
        final Dockable graphSingleDockable = m_bottomDockManager.getGraphSingleDockable();
        final Dockable mainSingleDockable = getMainSingleDockable();
        
        long ignoreDockModelsBeforeTimeMs = 1375274744000L;
        //long ignoreDockModelsBeforeTimeMs = System.currentTimeMillis();
        //long ignoreDockModelsBeforeTimeMs = 0L;
        boolean ignoreDockModel = false;
        File file = new File(DOCKMODEL_SOURCE);
        if (file.exists())
        {
            if (file.lastModified() < ignoreDockModelsBeforeTimeMs)
            {
                ignoreDockModel = true;
            }
        }
        
        
        // Try to decode the dock model from file.
        DockModelPropertiesDecoder dockModelDecoder = new DockModelPropertiesDecoder();
        if (!ignoreDockModel && dockModelDecoder.canDecodeSource(DOCKMODEL_SOURCE))
        {
            try 
            {
                // Create the map with the dockables, that the decoder needs.
                Map dockablesMap = new HashMap();
                LinkedList <Object> dockables = m_rightSideDockManager.getDockableList();
                for (Object obj : dockables)
                {
                    if (obj instanceof Dockable)
                        dockablesMap.put (((Dockable)obj).getID(), ((Dockable)obj));
                }
                dockables = m_bottomDockManager.getDockableList();
                for (Object obj : dockables)
                {
                    if (obj instanceof Dockable)
                        dockablesMap.put (((Dockable)obj).getID(), ((Dockable)obj));
                }
                
                dockables = m_leftSideDockManager.getDockableList();
                for (Object obj : dockables)
                {
                    if (obj instanceof Dockable)
                        dockablesMap.put (((Dockable)obj).getID(), ((Dockable)obj));
                }
                
                dockablesMap.put (centerDockable.getID(), centerDockable);
                dockablesMap.put (graphSingleDockable.getID(), graphSingleDockable);
                dockablesMap.put (mainSingleDockable.getID(), mainSingleDockable);
                
                
                // Create the map with the owner windows, that the decoder needs.
                Map ownersMap = new HashMap();
                ownersMap.put(MAINFRAME_ID, this);

                // Create the map with the visualizers, that the decoder needs.
                Map visualizersMap = null;

                // Decode the file.
                dockModel = (FloatDockModel)dockModelDecoder.decode(DOCKMODEL_SOURCE, dockablesMap, ownersMap, visualizersMap);
                graphSplit = (RigidSplitDock)dockModel.getRootDock("graphSplitId");
                mainSplit = (SplitDock)dockModel.getRootDock("leftSplitId");
                
                WacsFloatDock floatDock = (WacsFloatDock)dockModel.getFloatDock(this);
                floatDock.setMainSplitDock(mainSplit);
                
                //Add custom dragger
                addWindowDragger ();
                
                m_bottomDockManager.addSplitDockToSinglePanel(graphSplit);
               // m_rightSideDockManager.addDockablesToDocks(false, m_WindowMenu);
                m_bottomDockManager.addDockablesToDocks(false, null);
                m_leftSideDockManager.addDockablesToDocks(false, null);
                initializeMainFrame (mainSplit, mainSingleDockable);
                
                //giveMenuItemsToManualControlPanel ();
                dockModelInitialized = true;
            }
            catch (FileNotFoundException fileNotFoundException){
                System.out.println("Could not find the file [" + DOCKMODEL_SOURCE + "] with the saved dock model.");
                System.out.println("Continuing with the default dock model.");
            }
            catch (IOException ioException){
                System.out.println("Could not decode a dock model: [" + ioException + "].");
                ioException.printStackTrace();
                System.out.println("Continuing with the default dock model.");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (!dockModelInitialized || dockModel == null || mainSplit == null || graphSplit == null)
        {
            dockModel = new FloatDockModel(DOCKMODEL_SOURCE);
            dockModel.setFloatDockFactory(new WacsFloatDockFactory());
            dockModel.addOwner(MAINFRAME_ID, this);
            DockingManager.setDockModel(dockModel);
            
            //Add custom dragger
            addWindowDragger ();

            // Give the float dock a different child dock factory.
            // We don't want the floating docks to be splittable.
            WacsFloatDock floatDock = (WacsFloatDock)dockModel.getFloatDock(this);
            floatDock.setChildDockFactory(new SingleDockFactory());
        
            //Center panel for map canvas
            SingleDock centerDock = new SingleDock();
            centerDock.addDockable(centerDockable, new Position (Position.LEFT));
            centerDock.setPreferredSize(new Dimension (1000,1000));

            //Right side for accessory panels
            RigidSplitDock canvasSplit = new RigidSplitDock();
            canvasSplit.addChildDock(centerDock, new Position (Position.LEFT));
            canvasSplit.addChildDock(m_rightSideDockManager.getDock(), new Position (Position.RIGHT));

            //Bottom for graphs
            graphSplit = new RigidSplitDock();
            graphSplit.addChildDock(canvasSplit, new Position (Position.TOP));
            graphSplit.addChildDock(m_bottomDockManager.getDock(), new Position (Position.BOTTOM));
            m_bottomDockManager.addSplitDockToSinglePanel(graphSplit);
            SingleDock graphSingleDock = m_bottomDockManager.getGraphSingleDock(graphSingleDockable, this);
            
            //Left side for main controls
            mainSplit = new SplitDock();
            mainSplit.addChildDock(m_leftSideDockManager.getDock(), new Position (Position.LEFT));
            mainSplit.addChildDock(graphSingleDock, new Position (Position.RIGHT));
            floatDock.setMainSplitDock(mainSplit);

            //Finalize dock model and create frame for window
            dockModel.addRootDock("graphSplitId", graphSplit, this);
            dockModel.addRootDock("leftSplitId", mainSplit, this);
            initializeMainFrame (mainSplit, mainSingleDockable);
            this.setSize (900,700);
            mainSplit.setDividerLocation(200);
            graphSplit.setDividerLocation(graphSplit.getHeight()-200);
            canvasSplit.setDividerLocation(canvasSplit.getWidth()-400);

           // m_rightSideDockManager.addDockablesToDocks(true, m_WindowMenu);
            m_bottomDockManager.addDockablesToDocks(true, null);
            m_leftSideDockManager.addDockablesToDocks(true, null);
            
            //giveMenuItemsToManualControlPanel();
            
            m_rightSideDockManager.finalizeDock();
            m_bottomDockManager.finalizeDock();
        }
        
        populateMenuBar();
        //m_MenuBar.add(m_WindowMenu);
        this.setTitle ("WACS Control Station");
        this.setVisible(true);
        
        addWindowListeners (dockModel, this);
    }
    
    /*
    private void giveMenuItemsToManualControlPanel()
    {
        //Provide access to Window menu strike time check menu item to manual control panel so that it can raise that window
        for (Component comp : m_WindowMenu.getMenuComponents())
        {
            if (comp instanceof JCheckBoxMenuItem)
            {
                if (((JMenuItem)comp).getText().equals (STRIKE_TIME_TEXT))
                    m_ManualControlPanel.setStrikeTimeMenuItem ((JCheckBoxMenuItem)comp);
                if (((JMenuItem)comp).getText().equals (ZERO_AIR_DATA_TEXT))
                    m_ManualControlPanel.setZeroAirDataMenuItem((JCheckBoxMenuItem)comp);
            }
        }
    }
*/
    
    protected void initializeMainFrame (SplitDock mainSplit, Dockable mainSingleDockable)
    {
        //Add main split to single panel
        mainSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_MainSinglePanel.add(mainSplit);
        m_MainSinglePanel.validate();
        
        //Control buttons across the top
        SingleDock mainSingleDock = getMainSingleDock(mainSingleDockable, this);

        //Initialize main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mainSingleDock);
        getContentPane().add(mainPanel);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    protected void addWindowDragger ()
    {
        DockableDragPainter swDockableDragPainterWithoutLabel = new SwDockableDragPainter(new DefaultRectanglePainter());
        DockableDragPainter windowDockableDragPainterWithoutLabel = new WindowDockableDragPainter(new DefaultRectanglePainter());
        DockableDragPainter imageDockableDragPainter = new WacsImageDockableDragPainter();
        CompositeDockableDragPainter compositeDockableDragPainter = new CompositeDockableDragPainter();
        compositeDockableDragPainter.addPainter(swDockableDragPainterWithoutLabel);
        compositeDockableDragPainter.addPainter(windowDockableDragPainterWithoutLabel);
        compositeDockableDragPainter.addPainter(imageDockableDragPainter);
        DraggerFactory draggerFactory = new WacsStaticDraggerFactory(compositeDockableDragPainter);
        DockingManager.setDraggerFactory(draggerFactory);
    }
    
    protected void addWindowListeners (final DockModel dockModel, final JFrame mainFrame)
    {
        // Listen when the window is closed. The workspace should be saved.
        this.addWindowListener(new WindowListener() 
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                int opt = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (opt == JOptionPane.NO_OPTION)
                    return;

                // Save the dock model.
		DockModelPropertiesEncoder encoder = new DockModelPropertiesEncoder();
		if (encoder.canSave(dockModel))
		{
                    try
                    {
                        encoder.save(dockModel);
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Error while saving the dock model.");
                        ex.printStackTrace();
                    }
		}
                
                mainFrame.dispose();
                System.exit(0);
            }

            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
    }
    
    protected SearchCanvas createCanvas(BeliefManager belMgr, JFrame owner) 
    {
        double startLat = Config.getConfig().getPropertyAsDouble("agent.startLat");
        double startLon = Config.getConfig().getPropertyAsDouble("agent.startLon");
        boolean useImageryFetcher = Config.getConfig().getPropertyAsBoolean("SearchDisplay.UseImageryFetcher", false);
        LatLonAltPosition viewCenter = null;
        Length viewRange = null;
        if (useImageryFetcher)
        {
            viewCenter = new LatLonAltPosition (new Latitude (startLat, Angle.DEGREES), new Longitude (startLon, Angle.DEGREES), new Altitude (0, Length.METERS));
            viewRange = new Length(16.0, Length.MILES);
            return new SearchCanvas(NUM_LEVELS, viewRange, 1024, 768, viewCenter, belMgr, null, owner, agentTracker,null);
        }
        else
        {
            viewCenter = LatLonAltPosition.ORIGIN;
            viewRange = new Length(2000.0, Length.MILES);
            return new SearchCanvas(NUM_LEVELS, viewRange, 1024, 768, JGeoCanvas.ORTHOGRAPHIC_PROJECTION, viewCenter, belMgr, null, owner, agentTracker,null);
        }
    }
    
    public Dockable getMainSingleDockable() 
    {
        m_ButtonToolbar = new JPanel();
        m_ButtonToolbar.setLayout(new BoxLayout(m_ButtonToolbar, BoxLayout.Y_AXIS));
        JToolBar mainToolbar = m_SearchCanvasPanel.createToolBar(true);
        m_ButtonToolbar.add (mainToolbar);
        m_ButtonToolbar.validate();
        m_ButtonToolbar.setPreferredSize(new Dimension (100, 45));
        m_ButtonToolbar.setMinimumSize(m_ButtonToolbar.getPreferredSize());
        
        m_TelemetryBar = new JPanel();
        m_TelemetryBar.setLayout(new BoxLayout(m_TelemetryBar, BoxLayout.Y_AXIS));
        JToolBar telemetryToolbar = m_SearchCanvasPanel.createTelemetryBar(true);
        m_TelemetryBar.add (telemetryToolbar);
        m_TelemetryBar.validate();
        m_TelemetryBar.setPreferredSize(new Dimension (100, 25));
        m_TelemetryBar.setMinimumSize(m_TelemetryBar.getPreferredSize());
        
        m_ButtonToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_TelemetryBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        m_MainSinglePanel = new JPanel();
        m_MainSinglePanel.setLayout(new BoxLayout(m_MainSinglePanel, BoxLayout.Y_AXIS));
        m_MainSinglePanel.add(m_ButtonToolbar);
        m_MainSinglePanel.add(m_TelemetryBar);
        m_MainSinglePanel.validate();
        Dockable mainSingleDockable = new DefaultDockable("mainSingleDockableId", m_MainSinglePanel, null, null, DockingMode.SINGLE);
        return mainSingleDockable;
    }
    
    public SingleDock getMainSingleDock(Dockable mainSingleDockable, JFrame parentFrame) 
    {
        SingleDock mainSingleDock = new SingleDock();
        mainSingleDock.addDockable(mainSingleDockable, new Position (Position.CENTER));                
        
        return mainSingleDock;
    }

    
    private void populateRightSideDock() 
    {
        //WACSVideoClientPanel videoClientPanel = new WACSVideoClientPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(videoClientPanel, true, "Video");
        
        //AglDisplay altitudePanel = new AglDisplay(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(altitudePanel, false, "UAS Altitude AGL");
        
//        if (Config.getConfig().getPropertyAsBoolean("FlightControl.useShadowDriver", false) && 
//                Config.getConfig().getPropertyAsBoolean("FlightControl.ControlUavThroughGCS", false))
//        {
//            ShadowDataFormPanel shadowPanel = new ShadowDataFormPanel(ShadowAutopilotInterface.s_instance);
//            m_rightSideDockManager.addScrollablePanel(shadowPanel, false, "Shadow Data");
//        }
        
        //m_rightSideDockManager.addMenuSeparator();
        
        //m_MissionErrorsPanel = new MissionStatusErrorsPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_MissionErrorsPanel, false, "WCA Panel");
        
        //m_SatCommStatusPanel = new SatCommStatusPanel(m_BeliefManager, m_SearchCanvasPanel);
        //m_rightSideDockManager.addScrollablePanel(m_SatCommStatusPanel, true, "Communication Status");
        
        //m_SystemStatusPanel = new SystemStatusPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_SystemStatusPanel, true, "System Status");
        
        //m_SensorSumaryStopLights = new SensorSummaryStopLightPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_SensorSumaryStopLights, true, "Sensor Summary");
        
        //m_Nbc1ReportPanel = new Nbc1ReportPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_Nbc1ReportPanel, true, "NBC-1 Report");
        
        //m_rightSideDockManager.addMenuSeparator();
        
        //KeepOutRegionPanel keepOutRegionPanel = new KeepOutRegionPanel(m_BeliefManager, m_SearchCanvasPanel);
        //m_rightSideDockManager.addScrollablePanel(keepOutRegionPanel, false, "Keep-Out Regions");
        
        //SimulatedCloudPanel simulatedCloudPanel = new SimulatedCloudPanel(m_BeliefManager, m_SearchCanvasPanel);
        //m_rightSideDockManager.addScrollablePanel(simulatedCloudPanel, false, "Simulate Cloud");
        
        //m_rightSideDockManager.addMenuSeparator();
        
        //m_ExplosionTimePanel = new ExplosionTimePanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_ExplosionTimePanel, false, STRIKE_TIME_TEXT);
        
        //m_ZeroAirDataPanel = new ZeroAirDataPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_ZeroAirDataPanel, false, ZERO_AIR_DATA_TEXT);
        
       // m_UavAutopilotPanel = new UavAutopilotPanel(m_BeliefManager);
        //m_rightSideDockManager.addScrollablePanel(m_UavAutopilotPanel, false, "UAS Autopilot");
        
        DisplayUnitsManager displayUnitsPanel = DisplayUnitsManager.getInstance();
        //m_rightSideDockManager.addScrollablePanel(displayUnitsPanel, false, "Display Units");
        
    }

    private void populateBottomDock() 
    {
        Dimension minDim = new Dimension(200,200);  
        
//        m_GammaGraphPanel = new RNHistogramDisplayGraphPanel();
//        m_GammaGraphPanel.selectLogScale(true);
//        m_GammaGraphPanel.setMinimumSize(minDim);
//        m_GammaGraphPanel.setTitle("Gamma Count Histogram");
//        m_bottomDockManager.addDraggablePanel (m_GammaGraphPanel, false, "Gamma Spectra");
//        
//        m_AlphaGraphPanel = new RNHistogramDisplayGraphPanel();
//        m_AlphaGraphPanel.selectLogScale(true);
//        m_AlphaGraphPanel.setMinimumSize(minDim);
//        m_AlphaGraphPanel.setTitle("Alpha Count Histogram");
//        m_bottomDockManager.addDraggablePanel (m_AlphaGraphPanel, false, "Alpha Spectra");
        
        m_AltitudeGraphPanel = new AltitudeChartPanel(m_BeliefManager);
        m_AltitudeGraphPanel.setMinimumSize(minDim);
        m_bottomDockManager.addDraggablePanel (m_AltitudeGraphPanel, true, "Altitude History");
        
        m_EtdPanel = new EtdPanel(m_BeliefManager);
        m_EtdPanel.setMinimumSize(minDim);
        m_bottomDockManager.addDraggablePanel(m_EtdPanel, true, "ETD Plot");

        m_EtdTempPanel = new EtdTempPanel(m_BeliefManager);
        m_EtdTempPanel.setMinimumSize(minDim);
        m_bottomDockManager.addDraggablePanel(m_EtdTempPanel, true, "ETD Temp");
        
        m_EtdHistoryPanel = new EtdHistoryPanel(m_BeliefManager);
        m_EtdHistoryPanel.setMinimumSize(minDim);
        m_bottomDockManager.addDraggablePanel(m_EtdHistoryPanel, true, "ETD History");
        
        m_EtdErrorsPanel = new EtdErrorsPanel(m_BeliefManager);
        m_EtdErrorsPanel.setMinimumSize(minDim);
        m_bottomDockManager.addDraggablePanel(m_EtdErrorsPanel, true, "ETD Errors");
        
        m_IbacGraphPanel = new IbacChartPanel(m_BeliefManager);
        // Ibac minimum dimension set in constructor
        //m_bottomDockManager.addDraggablePanel(m_IbacGraphPanel, false, "Particle Counts");
        
        m_AnacondaGraphPanel = new AnacondaChartPanel(m_BeliefManager);
        m_AnacondaGraphPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_AnacondaGraphPanel, false, "Chem Agents");
        
        m_BetaRateGraphPanel = new BetaChartPanel(m_BeliefManager);
        m_BetaRateGraphPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_BetaRateGraphPanel, false, "Net Beta Rate");
        
        m_BridgeportGraphPanel = new BridgeportChartPanel(m_BeliefManager);
        m_BridgeportGraphPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_BridgeportGraphPanel, false, "Gamma Rate");
        
        m_CanberraGraphPanel = new CanberraChartPanel(m_BeliefManager);
        m_CanberraGraphPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_CanberraGraphPanel, false, "Dosimeter Gamma Rate");
        
        m_AlphaRateGraphPanel = new AlphaChartPanel(m_BeliefManager);
        m_AlphaRateGraphPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_AlphaRateGraphPanel, false, "Alpha Rate");
        
        m_AlphaSpectrumPanel = new GenericHistogramPanel(m_BeliefManager, AlphaCompositeHistogramBelief.BELIEF_NAME, alphaTotalCountsTracker, "Channel", "Counts");
        m_AlphaSpectrumPanel.setDetectionBelief(AlphaDetectionBelief.class, "getAlphaDetections");
        m_AlphaSpectrumPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_AlphaSpectrumPanel, false, "Alpha Spectrum");
        m_AlphaRateGraphPanel.setTimeFor(m_AlphaSpectrumPanel);
        
        m_GammaSpectrumPanel = new GenericHistogramPanel(m_BeliefManager, GammaCompositeHistogramBelief.BELIEF_NAME, gammaTotalCountsTracker, "Channel", "Counts");
        m_GammaSpectrumPanel.setDetectionBelief(GammaDetectionBelief.class ,"getGammaDetections");
        m_GammaSpectrumPanel.setMinimumSize(minDim);
        //m_bottomDockManager.addDraggablePanel(m_GammaSpectrumPanel, false, "Gamma Spectrum");
        m_BridgeportGraphPanel.setTimeFor(m_GammaSpectrumPanel);
        m_CanberraGraphPanel.setTimeFor(m_GammaSpectrumPanel);
    }

    private void populateLeftSideDock() 
    {
        m_MainControlPanel = new MissionManagerPanel(m_BeliefManager);
        m_leftSideDockManager.addScrollablePanel (m_MainControlPanel, true, "Mission Manager");
        
        //m_ManualControlPanel = new ManualControlPanel(m_BeliefManager);
        //m_leftSideDockManager.addScrollablePanel (m_ManualControlPanel, true, "Manual Controls");
        
        m_AdvancedSettingsPanel = new AdvancedSettingsPanel(m_BeliefManager);
        m_leftSideDockManager.addScrollablePanel (m_AdvancedSettingsPanel, true, "Settings");
        
        m_SafetyBoxPanel = new SafetyBoxPanel(m_SearchCanvasPanel, m_BeliefManager);
        m_leftSideDockManager.addScrollablePanel (m_SafetyBoxPanel, true, "Safety Box");
        
        m_UavControlPanel = new WACSSettingsPanel(m_SearchCanvasPanel, m_BeliefManager);
        m_leftSideDockManager.addScrollablePanel (m_UavControlPanel, true, "UAV");
                
        //m_SensorControlPanel = new WACSControlPanel(m_SearchCanvasPanel, m_BeliefManager);
        m_leftSideDockManager.addScrollablePanel (m_SensorControlPanel, true, "Sensors");
    }
    
    private void populateMenuBar()
    {
        //m_MissionMenu = new JMenu("Mission");
        //m_MenuBar.add(m_MissionMenu);
        //JMenuItem loadMissionMenuItem = new JMenuItem("Load Mission");
        //m_MissionMenu.add(loadMissionMenuItem);
        //JMenuItem saveMissionMenuItem = new JMenuItem("Save Mission");
        //m_MissionMenu.add(saveMissionMenuItem);
        
        m_DisplayMenu = new JMenu("Display");
        m_MenuBar.add(m_DisplayMenu);
        
        //Projection type
        OrthographicAction orthographicAction = new OrthographicAction(m_SearchCanvasPanel);
        JRadioButtonMenuItem orthographicMenuItem = new JRadioButtonMenuItem(orthographicAction);
        MercatorAction mercatorAction = new MercatorAction(m_SearchCanvasPanel);
        JRadioButtonMenuItem mercatorMenuItem = new JRadioButtonMenuItem(mercatorAction);
        ButtonGroup projectionGroup = new ButtonGroup();
        projectionGroup.add(orthographicMenuItem);
        projectionGroup.add(mercatorMenuItem);
        int currentProjection = m_SearchCanvasPanel.getProjectionType();
        if (currentProjection == JGeoCanvas.ORTHOGRAPHIC_PROJECTION)
            orthographicMenuItem.setSelected(true);
        else if (currentProjection == JGeoCanvas.MERCATOR_PROJECTION)
            mercatorMenuItem.setSelected(true);
        m_DisplayMenu.add(orthographicMenuItem);
        m_DisplayMenu.add(mercatorMenuItem);
        m_DisplayMenu.addSeparator();
        
        
        //Display toggles
        try
        {
            Class [] booleanParameterType = {Boolean.class};
            addDisplayToggle (true, "Show Names", m_SearchCanvasPanel.getClass().getMethod("showNames", booleanParameterType));
            //addDisplayToggle (true, "Show Cloud", m_SearchCanvasPanel.getClass().getMethod("showCloud", booleanParameterType));
            addDisplayToggle (false, "Show Bearing", m_SearchCanvasPanel.getClass().getMethod("showDirection", booleanParameterType));
            addDisplayToggle (true, "Show Position History", m_SearchCanvasPanel.getClass().getMethod("showHistory", booleanParameterType));
            addDisplayToggle (false, "Show Wind", m_SearchCanvasPanel.getClass().getMethod("showWind", booleanParameterType));
            
            addDisplayToggle (true, "Show Keep-Out Regions", m_SearchCanvasPanel.getClass().getMethod("showKeepOut", booleanParameterType));
            addDisplayToggle (true, "Show Safety Box", m_SearchCanvasPanel.getClass().getMethod("showSafetyBox", booleanParameterType));
            ToggleCallable[] projectionCalls = new ToggleCallable[] 
                {new ToggleCallable(m_SearchCanvasPanel.getClass().getMethod("showLocationBubble", booleanParameterType), m_SearchCanvasPanel),
                 new ToggleCallable(m_AltitudeGraphPanel.getClass().getMethod("showAltitudeBubble", booleanParameterType), m_AltitudeGraphPanel)};
            addDisplayToggle (true, "Show Location Projection", projectionCalls);
            addDisplayToggle (true, "Show Scale", m_SearchCanvasPanel.getClass().getMethod("showScale", booleanParameterType));
            addDisplayToggle (true, "Show Mouse Information", m_SearchCanvasPanel.getClass().getMethod("showMouseInfo", booleanParameterType));
            addDisplayToggle (true, "Show Wind Compass", m_SearchCanvasPanel.getClass().getMethod("showWindCompass", booleanParameterType));
            addDisplayToggle (true, "Show Planned Orbit", m_SearchCanvasPanel.getClass().getMethod("showNextOrbit", booleanParameterType));
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private class ToggleCallable
    {
        final Method method;
        Object target;
        
        public ToggleCallable(final Method method, Object target)
        {
            this.method = method;
            this.target = target;
        }
        
        public void invoke(boolean show) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            method.invoke(target, show);
        }
    }

    private void addDisplayToggle (boolean defaultShow, String displayName, final Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        final JCheckBoxMenuItem showNamesMenuItem = new JCheckBoxMenuItem(displayName, defaultShow);
        showNamesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                try {
                    method.invoke (m_SearchCanvasPanel, showNamesMenuItem.isSelected());
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        method.invoke (m_SearchCanvasPanel, defaultShow);
        m_DisplayMenu.add(showNamesMenuItem);
    }
    
    private void addDisplayToggle (boolean defaultShow, String displayName, final ToggleCallable[] toggles)
    {
        final JCheckBoxMenuItem showNamesMenuItem = new JCheckBoxMenuItem(displayName, defaultShow);
        showNamesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                try {
                    for (ToggleCallable toggle : toggles)
                        toggle.invoke (showNamesMenuItem.isSelected());
                    
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        try {
            for (ToggleCallable toggle : toggles)
                toggle.invoke (defaultShow);
        } catch (Exception ex) {
            Logger.getLogger(GcsDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_DisplayMenu.add(showNamesMenuItem);
    }
}
