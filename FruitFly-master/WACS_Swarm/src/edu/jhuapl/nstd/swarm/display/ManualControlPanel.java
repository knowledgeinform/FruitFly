/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.cbrnPods.TestPanels.AnacondaPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BladewerxPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BridgeportEthernetPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.BridgeportUsbPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.C100Panel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.CanberraGammaPanel;
import edu.jhuapl.nstd.cbrnPods.TestPanels.IbacPanel;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CanberraDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.GammaCompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.ModeTimeName;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCloudTrackingTypeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PlumeDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataRequiredBelief;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.SensorDebugWindowHandler;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

/**
 *
 * @author humphjc1
 */
public class ManualControlPanel extends javax.swing.JPanel implements MissionErrorManager.CommandedBeliefErrorListener, MissionErrorManager.TimedBeliefErrorListener, Updateable
{
    BeliefManager m_BeliefManager;
    
    private Date m_LastActualAgentModeUpdateTime;
    private Date m_LastCommandedAgentModeUpdateTime;
    private Date m_LastCommandedAllowInterceptTime;
    private Date m_LastActualAllowInterceptTime;
    private Date m_LastIrExpAlgorithmTime;
    private Date m_LastCommandedAnacondaStateTime;
    private Date m_LastActualAnacondaStateTime;
    private Date m_LastAnacondaDetectionTime;
    private Date m_LastCommandedIbacStateTime;
    private Date m_LastActualIbacStateTime;
    private Date m_LastIbacDetectionTime;
    private Date m_LastCommandedVideoStateTime;
    private Date m_LastActualVideoStateTime;
    private Date m_LastActualTrackingUpdatedTime;
    private Date m_LastPlumeDetUpdatedTime;
    
    private String m_AnacondaDelayText = null;
    private String m_IbacDelayText = null;
    private boolean m_AnacondaDelayTextChanged = false;
    private boolean m_IbacDelayTextChanged = false;
    private String m_PiccoloDelayText = null;
    private String m_TaseDelayText = null;
    private boolean m_PiccoloDelayTextChanged = false;
    private boolean m_TaseDelayTextChanged = false;
    private String m_BridgeportDelayText = null;
    private boolean m_BridgeportDelayTextChanged = false;
    private String m_CanberraDelayText = null;
    private boolean m_CanberraDelayTextChanged = false;
    
    private Date m_LastCommandedC100StateTime;
    private String m_C100DelayText = null;
    private Date m_LastActualC100StateTime;
    private boolean m_C100DelayTextChanged = false;
    private Date m_LastCommandedAlphaStateTime;
    private String m_AlphaDelayText = null;
    private Date m_LastActualAlphaStateTime;
    private boolean m_AlphaDelayTextChanged = false;
    private Date m_LastAlphaAnalysisTime = null;
    private Date m_LastAlphaHistogramTime = null;
    private Date m_LastGammaAnalysisTime = null;
    private Date m_LastGammaHistogramTime = null;
    private Date m_LastCanberraDetectionTime = null;
                
    DecimalFormat m_DecFormat1 = new DecimalFormat("0.#");
    DecimalFormat m_DecFormat2 = new DecimalFormat("0.##");
    DecimalFormat m_DecFormat3 = new DecimalFormat("0.###");
    
    JCheckBoxMenuItem m_StrikeTimeMenuItem;
    JCheckBoxMenuItem m_ZeroAirDataMenuItem;
    
    private JFrame m_AnacondaDebugPanel;
    private JFrame m_BladewerxDebugWindow;
    private JFrame m_BridgeportDebugWindow;
    private JFrame m_C110DebugWindow;
    private JFrame m_IBACDebugWindow;
    private JFrame m_CanberraDebugWindow;
    
    /**
     * Creates new form ManualControlPanel
     */
    public ManualControlPanel(BeliefManager belMgr) 
    {
        initComponents();
        
        m_BeliefManager = belMgr;
        m_StrikeTimeMenuItem = null;
        m_ZeroAirDataMenuItem = null;
        
        MissionErrorManager.getInstance().registerErrorListener(this);
        MissionErrorManager.getInstance().registerTimedErrorListener(this);
        
        boolean allowDebugWindows = Config.getConfig().getPropertyAsBoolean("ManualControlPanel.AllowDebugWindows", false);
        if (allowDebugWindows)
        {
            m_AnacondaDebugPanel = new JFrame("Chemical Sensor Debug Window");
            m_AnacondaDebugPanel.add(new AnacondaPanel(SensorDebugWindowHandler.getPodInterface(belMgr)));
            m_AnacondaDebugPanel.pack();
            jLabel5.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel5, m_AnacondaDebugPanel);
            
            m_BladewerxDebugWindow = new JFrame("Alpha Sensor Debug Window");
            m_BladewerxDebugWindow.add(new BladewerxPanel(SensorDebugWindowHandler.getPodInterface(belMgr), belMgr));
            m_BladewerxDebugWindow.pack();
            jLabel19.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel19, m_BladewerxDebugWindow);
            
            m_BridgeportDebugWindow = new JFrame("Gamma Sensor Debug Window");
            if (Config.getConfig().getPropertyAsBoolean("cbrnPodsInterfaceTest.useBridgeportUsbInterface", false))
                m_BridgeportDebugWindow.add(new BridgeportUsbPanel(SensorDebugWindowHandler.getPodInterface(belMgr), belMgr));
            else
                m_BridgeportDebugWindow.add(new BridgeportEthernetPanel(SensorDebugWindowHandler.getPodInterface(belMgr), belMgr));
            m_BridgeportDebugWindow.pack();
            jLabel24.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel24, m_BridgeportDebugWindow);
            
            m_C110DebugWindow = new JFrame("Particle Collector Debug Window");
            m_C110DebugWindow.add(new C100Panel(SensorDebugWindowHandler.getPodInterface(belMgr)));
            m_C110DebugWindow.pack();
            jLabel16.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel16, m_C110DebugWindow);
            
            m_IBACDebugWindow = new JFrame("Particle Sensor Debug Window");
            m_IBACDebugWindow.add(new IbacPanel(SensorDebugWindowHandler.getPodInterface(belMgr)));
            m_IBACDebugWindow.pack();
            jLabel10.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel10, m_IBACDebugWindow);
            
            m_CanberraDebugWindow = new JFrame("Dosimeter Debug Window");
            m_CanberraDebugWindow.add(new CanberraGammaPanel(SensorDebugWindowHandler.getPodInterface(belMgr)));
            m_CanberraDebugWindow.pack();
            jLabel28.setToolTipText("Double-click for debug window");
            SensorDebugWindowHandler.addSensorDebugDoubleClickEvent (jLabel28, m_CanberraDebugWindow);
        }
    }
    
    @Override
    public void update()
    {
        AgentModeCommandedBelief commandedAgentModeBelief = (AgentModeCommandedBelief)m_BeliefManager.get(AgentModeCommandedBelief.BELIEF_NAME);
        if (commandedAgentModeBelief != null && commandedAgentModeBelief.getModeTimeName(WACSAgent.AGENTNAME) != null)
        {
            ModeTimeName mtn = commandedAgentModeBelief.getModeTimeName(WACSAgent.AGENTNAME);
            if (m_LastCommandedAgentModeUpdateTime == null || mtn.getTime().after(m_LastCommandedAgentModeUpdateTime))
            {
                m_ModeButtonGroup.clearSelection();
                if (mtn.getMode().getName().equals (LoiterBehavior.MODENAME))
                {
                    jToggleButton9.setSelected(true);
                }
                else if (mtn.getMode().getName().equals (ParticleCloudPredictionBehavior.MODENAME))
                {
                    jToggleButton12.setSelected(true);
                }
                m_LastCommandedAgentModeUpdateTime = mtn.getTime();
            }
        }
        
        AgentModeActualBelief actualAgentModeBelief = (AgentModeActualBelief)m_BeliefManager.get(AgentModeActualBelief.BELIEF_NAME);
        if (actualAgentModeBelief != null && actualAgentModeBelief.getModeTimeName(WACSAgent.AGENTNAME) != null)
        {
            ModeTimeName mtn = actualAgentModeBelief.getModeTimeName(WACSAgent.AGENTNAME);
            if (m_LastActualAgentModeUpdateTime == null || mtn.getTime().after(m_LastActualAgentModeUpdateTime))
            {
                if (mtn.getMode().getName().equals (LoiterBehavior.MODENAME))
                {
                    jLabel32.setText("LOITER");
                }
                else if (mtn.getMode().getName().equals (ParticleCloudPredictionBehavior.MODENAME))
                {
                    jLabel32.setText("TRACKING");
                }
                else
                {
                    jLabel32.setText("IDLE");
                }
                m_LastActualAgentModeUpdateTime = mtn.getTime();
            }
        }
        
        AllowInterceptCommandedBelief allowInterceptCommandBelief = (AllowInterceptCommandedBelief)m_BeliefManager.get(AllowInterceptCommandedBelief.BELIEF_NAME);
        if (allowInterceptCommandBelief != null && (m_LastCommandedAllowInterceptTime == null || allowInterceptCommandBelief.getTimeStamp().after(m_LastCommandedAllowInterceptTime)))
        {
            jToggleButton24.setSelected(allowInterceptCommandBelief.getAllow());
            m_LastCommandedAllowInterceptTime = allowInterceptCommandBelief.getTimeStamp();
        }
        
        AllowInterceptActualBelief allowInterceptActualBelief = (AllowInterceptActualBelief)m_BeliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
        IrExplosionAlgorithmEnabledBelief irEnabledBelief = (IrExplosionAlgorithmEnabledBelief)m_BeliefManager.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
        if (allowInterceptActualBelief != null && ((m_LastActualAllowInterceptTime == null || allowInterceptActualBelief.getTimeStamp().after(m_LastActualAllowInterceptTime)) || (irEnabledBelief == null || m_LastIrExpAlgorithmTime == null || irEnabledBelief.getTimeStamp().after(m_LastIrExpAlgorithmTime))))
        {
            String text = "";
            if (allowInterceptActualBelief.getAllow())
                text = "YES";
            else
                text = "NO";
            m_LastActualAllowInterceptTime = allowInterceptActualBelief.getTimeStamp();
            
            if (irEnabledBelief != null)
            {
                if (irEnabledBelief.getEnabled())
                    text += "   (IR AUTO ON)";
                else
                    text += "   (IR AUTO OFF)";
                m_LastIrExpAlgorithmTime = irEnabledBelief.getTimeStamp();
            }
            
            jLabel34.setText(text);
        }
        
        AnacondaStateBelief anacondaCommandedStateBelief = (AnacondaStateBelief)m_BeliefManager.get(AnacondaStateBelief.BELIEF_NAME);
        if (anacondaCommandedStateBelief != null && (m_LastCommandedAnacondaStateTime == null || anacondaCommandedStateBelief.getTimeStamp().after(m_LastCommandedAnacondaStateTime)))
        {
            m_AnacondaButtonGroup.clearSelection();
            switch (anacondaCommandedStateBelief.getAnacondState())
            {
                case Standby:
                    jToggleButton2.setSelected(true);
                    break;
                case Search1:
                    jToggleButton3.setSelected(true);
                    break;
                case Search2:
                    jToggleButton4.setSelected(true);
                    break;
                case Search3:
                    jToggleButton5.setSelected(true);
                    break;
                case Search4:
                    jToggleButton6.setSelected(true);
                    break;
                case Idle:
                default:
                    jToggleButton1.setSelected(true);
                    break;
            }
            m_LastCommandedAnacondaStateTime = anacondaCommandedStateBelief.getTimeStamp();
        }
        
        if (m_AnacondaDelayText == null)
        {
            jLabel6.setText ("Not connected");
        }
        else
        {
            AnacondaActualStateBelief anacondaActualBelief = (AnacondaActualStateBelief)m_BeliefManager.get(AnacondaActualStateBelief.BELIEF_NAME);
            if (anacondaActualBelief != null && (m_LastActualAnacondaStateTime == null || anacondaActualBelief.getTimeStamp().after(m_LastActualAnacondaStateTime) || m_AnacondaDelayTextChanged))
            {
                jLabel6.setText (anacondaActualBelief.getStateText() + m_AnacondaDelayText);
                m_LastActualAnacondaStateTime = anacondaActualBelief.getTimeStamp();
                m_AnacondaDelayTextChanged = false;
            }
        }
        
        AnacondaDetectionBelief anacondaDetectionBelief = (AnacondaDetectionBelief)m_BeliefManager.get(AnacondaDetectionBelief.BELIEF_NAME);
        if (anacondaDetectionBelief != null && (m_LastAnacondaDetectionTime == null || anacondaDetectionBelief.getTimeStamp().after(m_LastAnacondaDetectionTime)))
        {
            String lcdaText = anacondaDetectionBelief.getLcdaDetectionString();
            String lcdbText = anacondaDetectionBelief.getLcdbDetectionString();
            
            if (lcdaText != null)
                jLabel7.setText (lcdaText);
            else
                jLabel7.setText ("LCDA:");
            
            if (lcdbText != null)
                jLabel8.setText (lcdbText);
            else
                jLabel8.setText ("LCDB:");
        
            m_LastAnacondaDetectionTime = anacondaDetectionBelief.getTimeStamp();
        }
        
        IbacStateBelief ibacCommandedStateBelief = (IbacStateBelief)m_BeliefManager.get(IbacStateBelief.BELIEF_NAME);
        if (ibacCommandedStateBelief != null && (m_LastCommandedIbacStateTime == null || ibacCommandedStateBelief.getTimeStamp().after(m_LastCommandedIbacStateTime)))
        {
            m_IbacButtonGroup.clearSelection();
            if (ibacCommandedStateBelief.getState())
            {
                jToggleButton10.setSelected(true);
            }
            else
            {
                jToggleButton7.setSelected(true);
            }
            m_LastCommandedIbacStateTime = ibacCommandedStateBelief.getTimeStamp();
        }
        
        if (m_IbacDelayText == null)
        {
            jLabel9.setText ("Not connected");
        }
        else
        {
            IbacActualStateBelief ibacActualBelief = (IbacActualStateBelief)m_BeliefManager.get(IbacActualStateBelief.BELIEF_NAME);
            if (ibacActualBelief != null && (m_LastActualIbacStateTime == null || ibacActualBelief.getTimeStamp().after(m_LastActualIbacStateTime) || m_IbacDelayTextChanged))
            {
                jLabel9.setText (ibacActualBelief.getStateText() + m_IbacDelayText);
                m_LastActualIbacStateTime = ibacActualBelief.getTimeStamp();
                m_IbacDelayTextChanged = false;
            }
        }
        
        ParticleDetectionBelief ibacDetectionBelief = (ParticleDetectionBelief)m_BeliefManager.get(ParticleDetectionBelief.BELIEF_NAME);
        if (ibacDetectionBelief != null && (m_LastIbacDetectionTime == null || ibacDetectionBelief.getTimeStamp().after(m_LastIbacDetectionTime)))
        {
            int totalCounts = ibacDetectionBelief.getLCI() + ibacDetectionBelief.getSCI();
            float bioPercent = 0;
            if (totalCounts > 0)
                bioPercent = ibacDetectionBelief.getBioPercent();
            
            jLabel12.setText ("Counts: " + totalCounts);
            jLabel11.setText ("Bio: " + m_DecFormat1.format(bioPercent) + "%");
            
            m_LastIbacDetectionTime = ibacDetectionBelief.getTimeStamp();
        }
        
        ParticleCollectorStateBelief c100CommandedStateBelief = (ParticleCollectorStateBelief)m_BeliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME);
        if (c100CommandedStateBelief != null && (m_LastCommandedC100StateTime == null || c100CommandedStateBelief.getTimeStamp().after(m_LastCommandedC100StateTime)))
        {
            m_C100ButtonGroup.clearSelection();
            switch (c100CommandedStateBelief.getParticleCollectorState())
            {
                case Collecting:
                    jToggleButton14.setSelected(true);
                    break;
                case Priming:
                    jToggleButton19.setSelected(true);
                    break;
                case Cleaning:
                    jToggleButton20.setSelected(true);
                    break;
                case StoringSample1:
                    jToggleButton17.setSelected(true);
                    break;
                case StoringSample2:
                    jToggleButton15.setSelected(true);
                    break;
                case StoringSample3:
                    jToggleButton16.setSelected(true);
                    break;
                case StoringSample4:
                    jToggleButton18.setSelected(true);
                    break;
                case Idle:
                default:
                    jToggleButton13.setSelected(true);
                    break;
            }
            m_LastCommandedC100StateTime = c100CommandedStateBelief.getTimeStamp();
        }
        
        if (m_C100DelayText == null)
        {
            jLabel13.setText ("Not connected");
        }
        else
        {
            ParticleCollectorActualStateBelief c100ActualBelief = (ParticleCollectorActualStateBelief)m_BeliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
            if (c100ActualBelief != null && (m_LastActualC100StateTime == null || c100ActualBelief.getTimeStamp().after(m_LastActualC100StateTime) || m_C100DelayTextChanged))
            {
                jLabel13.setText (c100ActualBelief.getStateText() + m_C100DelayText);
                m_LastActualC100StateTime = c100ActualBelief.getTimeStamp();
                m_C100DelayTextChanged = false;
            }
        }
        
        AlphaSensorStateBelief alphaCommandedStateBelief = (AlphaSensorStateBelief)m_BeliefManager.get(AlphaSensorStateBelief.BELIEF_NAME);
        if (alphaCommandedStateBelief != null && (m_LastCommandedAlphaStateTime == null || alphaCommandedStateBelief.getTimeStamp().after(m_LastCommandedAlphaStateTime)))
        {
            m_AlphaButtonGroup.clearSelection();
            if (alphaCommandedStateBelief.getState())
            {
                jToggleButton11.setSelected(true);
            }
            else
            {
                jToggleButton8.setSelected(true);
            }
            m_LastCommandedAlphaStateTime = alphaCommandedStateBelief.getTimeStamp();
        }
        
        if (m_AlphaDelayText == null)
        {
            jLabel20.setText ("Not connected");
        }
        else
        {
            AlphaSensorActualStateBelief alphaActualBelief = (AlphaSensorActualStateBelief)m_BeliefManager.get(AlphaSensorActualStateBelief.BELIEF_NAME);
            if (alphaActualBelief != null && (m_LastActualAlphaStateTime == null || alphaActualBelief.getTimeStamp().after(m_LastActualAlphaStateTime) || m_AlphaDelayTextChanged))
            {
                jLabel20.setText (alphaActualBelief.getStateText() + m_AlphaDelayText);
                m_LastActualAlphaStateTime = alphaActualBelief.getTimeStamp();
                m_AlphaDelayTextChanged = false;
            }
        }
        
        AlphaDetectionBelief alphaDetectionBelief = (AlphaDetectionBelief)m_BeliefManager.get(AlphaDetectionBelief.BELIEF_NAME);
        if (alphaDetectionBelief != null && (m_LastAlphaAnalysisTime == null || alphaDetectionBelief.getTimeStamp().after(m_LastAlphaAnalysisTime)))
        {
            String analysisText = alphaDetectionBelief.getAlphaDetections();
            analysisText = analysisText.substring(0, Math.min (analysisText.length(), 30));
            jLabel17.setText ("Analysis: " + analysisText);
            m_LastAlphaAnalysisTime = alphaDetectionBelief.getTimeStamp();
        }
        AlphaCompositeHistogramBelief alphaHistogramBelief = (AlphaCompositeHistogramBelief)m_BeliefManager.get(AlphaCompositeHistogramBelief.BELIEF_NAME);
        if (alphaHistogramBelief != null && (m_LastAlphaHistogramTime == null || alphaHistogramBelief.getTimeStamp().after(m_LastAlphaHistogramTime)))
        {
            long totalCounts = alphaHistogramBelief.getSpectraCount();
            jLabel18.setText ("Counts: " + totalCounts);
            m_LastAlphaHistogramTime = alphaHistogramBelief.getTimeStamp();
        }
        
        if (m_BridgeportDelayText == null)
            jLabel23.setText ("Not connected");
        else if (m_BridgeportDelayTextChanged)
            jLabel23.setText ("On " + m_BridgeportDelayText);
        
        GammaDetectionBelief gammaDetectionBelief = (GammaDetectionBelief)m_BeliefManager.get(GammaDetectionBelief.BELIEF_NAME);
        if (gammaDetectionBelief != null && (m_LastGammaAnalysisTime == null || gammaDetectionBelief.getTimeStamp().after(m_LastGammaAnalysisTime)))
        {
            String analysisText = gammaDetectionBelief.getGammaDetections();
            analysisText = analysisText.substring(0, Math.min (analysisText.length(), 30));
            jLabel21.setText ("Analysis: " + analysisText);
            m_LastGammaAnalysisTime = gammaDetectionBelief.getTimeStamp();
        }
        GammaCompositeHistogramBelief gammaHistogramBelief = (GammaCompositeHistogramBelief)m_BeliefManager.get(GammaCompositeHistogramBelief.BELIEF_NAME);
        if (gammaHistogramBelief != null && (m_LastGammaHistogramTime == null || gammaHistogramBelief.getTimeStamp().after(m_LastGammaHistogramTime)))
        {
            long totalCounts = gammaHistogramBelief.getSpectraCount();
            jLabel22.setText ("Counts: " + totalCounts);
            m_LastGammaHistogramTime = gammaHistogramBelief.getTimeStamp();
        }
        
        if (m_CanberraDelayText == null)
            jLabel27.setText ("Not connected");
        else if (m_CanberraDelayTextChanged)
            jLabel27.setText ("On " + m_CanberraDelayText);
        
        CanberraDetectionBelief canberraDetectionBelief = (CanberraDetectionBelief)m_BeliefManager.get(CanberraDetectionBelief.BELIEF_NAME);
        if (canberraDetectionBelief != null && (m_LastCanberraDetectionTime == null || canberraDetectionBelief.getTimeStamp().after(m_LastCanberraDetectionTime)))
        {
            long totalCounts = canberraDetectionBelief.getCount();
            double doseRate = canberraDetectionBelief.getFilteredDoseRate();
            jLabel25.setText ("Dose Rate: " + m_DecFormat2.format(doseRate) + " uG/hr");
            m_LastCanberraDetectionTime = canberraDetectionBelief.getTimeStamp();
        }
        
        ExplosionTimeRequiredBelief timeReqBelief = (ExplosionTimeRequiredBelief)m_BeliefManager.get (ExplosionTimeRequiredBelief.BELIEF_NAME);
        ExplosionTimeCommandedBelief timeBelief = (ExplosionTimeCommandedBelief)m_BeliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
        if (timeReqBelief != null && (timeBelief == null || timeReqBelief.getTimeStamp().after(timeBelief.getTimeStamp())))
        {
            //The belief that says we need to input a new explosion time is newer than the last time we updated the explosion time
            jButton1ActionPerformed (null);
        }
        
        ZeroAirDataRequiredBelief zeroReqBelief = (ZeroAirDataRequiredBelief)m_BeliefManager.get (ZeroAirDataRequiredBelief.BELIEF_NAME);
        ZeroAirDataBelief zeroBelief = (ZeroAirDataBelief)m_BeliefManager.get (ZeroAirDataBelief.BELIEF_NAME);
        if (zeroReqBelief != null && (zeroBelief == null || zeroReqBelief.getTimeStamp().after(zeroBelief.getTimeStamp())))
        {
            //The belief that says we need to zero the air data is newer than the last time we updated the setting
            jButton23ActionPerformed (null);
        }
                
                
        if (m_PiccoloDelayText == null)
            jLabel15.setText ("Not connected");
        else if (m_PiccoloDelayTextChanged)
            jLabel15.setText ("On " + m_PiccoloDelayText);
        
        
        if (m_TaseDelayText == null)
        {
            jLabel30.setText ("Not connected");
        }
        else if (m_TaseDelayTextChanged)
        {
            VideoClientRecorderStatusBelief videoRecStateBelief = (VideoClientRecorderStatusBelief)m_BeliefManager.get(VideoClientRecorderStatusBelief.BELIEF_NAME);
            if (videoRecStateBelief != null && (m_LastActualVideoStateTime == null || videoRecStateBelief.getTimeStamp().after(m_LastActualVideoStateTime) || m_IbacDelayTextChanged))
            {
                jLabel30.setText ((videoRecStateBelief.getState().isRecording?"Recording":"Not Recording") + m_TaseDelayText);
                m_LastActualVideoStateTime = videoRecStateBelief.getTimeStamp();
                m_TaseDelayTextChanged = false;
            }
            else if (videoRecStateBelief == null)
            {
                jLabel30.setText ("Unknown State" + m_TaseDelayText);
            }
            
        }
            
        VideoClientRecorderCmdBelief videoCommandedStateBelief = (VideoClientRecorderCmdBelief)m_BeliefManager.get(VideoClientRecorderCmdBelief.BELIEF_NAME);
        if (videoCommandedStateBelief != null && (m_LastCommandedVideoStateTime == null || videoCommandedStateBelief.getTimeStamp().after(m_LastCommandedVideoStateTime)))
        {
            m_IrRecorderButtonGroup.clearSelection();
            if (videoCommandedStateBelief.getRecorderCmd())
            {
                jToggleButton22.setSelected(true);
            }
            else
            {
                jToggleButton21.setSelected(true);
            }
            m_LastCommandedVideoStateTime = videoCommandedStateBelief.getTimeStamp();
        }
        
        ParticleCloudTrackingTypeActualBelief actualTrackingBelief = (ParticleCloudTrackingTypeActualBelief)m_BeliefManager.get (ParticleCloudTrackingTypeActualBelief.BELIEF_NAME);
        if (actualTrackingBelief != null && (m_LastActualTrackingUpdatedTime == null || actualTrackingBelief.getTimeStamp().after(m_LastActualTrackingUpdatedTime)))
        {
            jLabel1.setText (actualTrackingBelief.getTrackingType().toString());
            m_LastActualTrackingUpdatedTime = actualTrackingBelief.getTimeStamp();
        }
        
        PlumeDetectionBelief plumeDetBelief = (PlumeDetectionBelief)m_BeliefManager.get (PlumeDetectionBelief.BELIEF_NAME);
        if (plumeDetBelief != null && (m_LastPlumeDetUpdatedTime == null || plumeDetBelief.getTimeStamp().after(m_LastPlumeDetUpdatedTime)))
        {
            jLabel37.setText ("Align: " + m_DecFormat3.format(plumeDetBelief.getAlign()));
            jLabel38.setText ("Detect: " + m_DecFormat3.format(plumeDetBelief.getDetect()));
            m_LastPlumeDetUpdatedTime = plumeDetBelief.getTimeStamp();
        }
        
        ParticleCollectorActualStateBelief caBelief = (ParticleCollectorActualStateBelief)m_BeliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
        if(caBelief!=null)
        {
            toggleSampleButton (caBelief.isSample1full(), jToggleButton17);
            toggleSampleButton (caBelief.isSample2full(), jToggleButton15);
            toggleSampleButton (caBelief.isSample3full(), jToggleButton16);
            toggleSampleButton (caBelief.isSample4full(), jToggleButton18);
        }
        
        AnacondaActualStateBelief anBelief = (AnacondaActualStateBelief)m_BeliefManager.get(AnacondaActualStateBelief.BELIEF_NAME);
        if(anBelief!=null)
        {
            toggleSampleButton (anBelief.isSample1full(), jToggleButton3);
            toggleSampleButton (anBelief.isSample2full(), jToggleButton4);
            toggleSampleButton (anBelief.isSample3full(), jToggleButton5);
            toggleSampleButton (anBelief.isSample4full(), jToggleButton6);
        }
    }
    
    private void toggleSampleButton (boolean sampleFull, JToggleButton button)
    {
        if (sampleFull && button.isEnabled())
            button.setEnabled(false);
        else if (!sampleFull && !button.isEnabled())
            button.setEnabled(true);
    }
    
    @Override
    public void handleCommandedBeliefError(MissionErrorManager.ErrorCodeBase alarmCode, int alarmLevel) 
    {
        if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_WACSMODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel32, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_ANACONDAMODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel6, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_ALLOWINTERCEPT_ERRORCODE)
        {
            alarmLabelBackground(jLabel34, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.INFONEEDED_EXPTIMELOITER_ERRORCODE)
        {
            alarmLabelBackground(jButton1, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_IBACMODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel9, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_C100MODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel13, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_BLADEWERXMODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel20, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_IRRECORDSTATE_ERRORCODE)
        {
            alarmLabelBackground(jLabel30, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_WACSWAYPOINT_ERRORCODE ||
                alarmCode == MissionErrorManager.COMMANDACTUALSYNC_TARGETBELIEF_ERRORCODE ||
                alarmCode == MissionErrorManager.COMMANDACTUALSYNC_RACETRACKDEFINITION_ERRORCODE)
        {
            alarmLabelBackground(jLabel35, alarmLevel);
        }
        else if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_TRACKINGTYPE_ERRORCODE)
        {
            alarmLabelBackground(jLabel1, alarmLevel);
        }
    }
    
    @Override
    public void handleTimedBeliefError(MissionErrorManager.ErrorCodeBase errorCode, int alarmLevel, long delaySec) 
    {
        if (errorCode == MissionErrorManager.CONSTANTUPDATE_ANACONDADETECTION_ERRORCODE)
        {
            alarmLabelBackground(jLabel5, alarmLevel);
            
            m_AnacondaDelayText = getSensorDelayText (delaySec);
            m_AnacondaDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_IBACDETECTION_ERRORCODE)
        {
            alarmLabelBackground(jLabel10, alarmLevel);
            
            m_IbacDelayText = getSensorDelayText (delaySec);
            m_IbacDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.PODHEARTBEAT_C100RECV_ERRORCODE)
        {
            alarmLabelBackground(jLabel16, alarmLevel);
            
            m_C100DelayText = getSensorDelayText (delaySec);
            m_C100DelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.PODHEARTBEAT_BLADEWERXRECV_ERRORCODE)
        {
            alarmLabelBackground(jLabel19, alarmLevel);
            
            m_AlphaDelayText = getSensorDelayText (delaySec);
            m_AlphaDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_BRIDGEPORTSTATUS_ERRORCODE)
        {
            alarmLabelBackground(jLabel24, alarmLevel);
            
            m_BridgeportDelayText = getSensorDelayText (delaySec);
            m_BridgeportDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_CANBERRADETECTION_ERRORCODE)
        {
            alarmLabelBackground(jLabel28, alarmLevel);
            
            m_CanberraDelayText = getSensorDelayText (delaySec);
            m_CanberraDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_PICCOLOTELEMETRY_ERRORCODE)
        {
            alarmLabelBackground(jLabel14, alarmLevel);
            
            m_PiccoloDelayText = getSensorDelayText (delaySec);
            m_PiccoloDelayTextChanged = true;
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_TASETELEMETRY_ERRORCODE)
        {
            alarmLabelBackground(jLabel29, alarmLevel);
            
            m_TaseDelayText = getSensorDelayText (delaySec);
            m_TaseDelayTextChanged = true;
        }
    }
    
    public String getSensorDelayText (long delaySec)
    {
        String text;
        
        if (delaySec > 300)
            text = null;
        else if (delaySec > 120)
            text = " (+" + delaySec/60 + " min)";
        else
            text = " (+" + delaySec + " sec)";
        return text;
    }
    
    public void alarmLabelBackground (JComponent label, int alarmLevel)
    {
        if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
            alarmLabelBackground (label, Color.RED);
        else if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
            alarmLabelBackground (label, Color.ORANGE);
        else
        {
            //alarmLabelBackground (label, null);
            if (label instanceof JLabel && alarmLevel == MissionErrorManager.ALARMLEVEL_NOALARM)
            {
                alarmLabelBackground (label, Color.GREEN);
            }
            else
            {
                alarmLabelBackground (label, null);
            }
        }
    }
    
    public void alarmLabelBackground (JComponent label, Color backgroundColor)
    {
        if (backgroundColor != null && (!label.getBackground().equals (backgroundColor) || !label.isOpaque()))
        {    
            label.setBackground(backgroundColor);
            label.setOpaque(true);
            label.repaint();
        }
        else if (backgroundColor == null && label.isOpaque())
        {
            label.setOpaque(false);
            label.repaint();
        }
    }
    
    public void setStrikeTimeMenuItem(JCheckBoxMenuItem menuItem) 
    {
        m_StrikeTimeMenuItem = menuItem;
    }
    
    public void setZeroAirDataMenuItem(JCheckBoxMenuItem menuItem) 
    {
        m_ZeroAirDataMenuItem = menuItem;
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_ModeButtonGroup = new javax.swing.ButtonGroup();
        m_AnacondaButtonGroup = new javax.swing.ButtonGroup();
        m_IbacButtonGroup = new javax.swing.ButtonGroup();
        m_IrRecorderButtonGroup = new javax.swing.ButtonGroup();
        m_C100ButtonGroup = new javax.swing.ButtonGroup();
        m_AlphaButtonGroup = new javax.swing.ButtonGroup();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        jToggleButton17 = new javax.swing.JToggleButton();
        jToggleButton18 = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel20 = new javax.swing.JLabel();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jToggleButton19 = new javax.swing.JToggleButton();
        jToggleButton20 = new javax.swing.JToggleButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jToggleButton21 = new javax.swing.JToggleButton();
        jToggleButton22 = new javax.swing.JToggleButton();
        jSeparator7 = new javax.swing.JSeparator();
        jButton23 = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jToggleButton24 = new javax.swing.JToggleButton();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("CHEM SENSOR:");
        jLabel5.setToolTipText("");

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Not Connected");

        m_AnacondaButtonGroup.add(jToggleButton1);
        jToggleButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton1.setText("OFF");
        jToggleButton1.setPreferredSize(new java.awt.Dimension(89, 23));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        m_AnacondaButtonGroup.add(jToggleButton2);
        jToggleButton2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton2.setText("STANDBY");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        m_AnacondaButtonGroup.add(jToggleButton3);
        jToggleButton3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton3.setText("1");
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        m_AnacondaButtonGroup.add(jToggleButton4);
        jToggleButton4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton4.setText("2");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        m_AnacondaButtonGroup.add(jToggleButton5);
        jToggleButton5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton5.setText("3");
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        m_AnacondaButtonGroup.add(jToggleButton6);
        jToggleButton6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton6.setText("4");
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("LCDA: ");
        jLabel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel7.setPreferredSize(new java.awt.Dimension(79, 19));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("LCDB: ");
        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setText("Not Connected");

        m_IbacButtonGroup.add(jToggleButton7);
        jToggleButton7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton7.setText("OFF");
        jToggleButton7.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        m_IbacButtonGroup.add(jToggleButton10);
        jToggleButton10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton10.setText("ON");
        jToggleButton10.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton10ActionPerformed(evt);
            }
        });

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel10.setText("PARTICLE SENSOR:");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setText("Bio:");
        jLabel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel11.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setText("Counts:");
        jLabel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel12.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel13.setBackground(new java.awt.Color(255, 255, 255));
        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Not Connected");

        jLabel16.setBackground(new java.awt.Color(255, 255, 255));
        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel16.setText("PARTICLE COLLECTOR:");

        m_C100ButtonGroup.add(jToggleButton15);
        jToggleButton15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton15.setText("2");
        jToggleButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton15ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton16);
        jToggleButton16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton16.setText("3");
        jToggleButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton16ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton17);
        jToggleButton17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton17.setText("1");
        jToggleButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton17ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton18);
        jToggleButton18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton18.setText("4");
        jToggleButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton18ActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel17.setText("Analysis:");
        jLabel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel17.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel18.setText("Counts:");
        jLabel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel18.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel19.setBackground(new java.awt.Color(255, 255, 255));
        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel19.setText("ALPHA SENSOR:");

        jLabel20.setBackground(new java.awt.Color(255, 255, 255));
        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel20.setText("Not Connected");

        m_AlphaButtonGroup.add(jToggleButton8);
        jToggleButton8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton8.setText("OFF");
        jToggleButton8.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton8ActionPerformed(evt);
            }
        });

        m_AlphaButtonGroup.add(jToggleButton11);
        jToggleButton11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton11.setText("ON");
        jToggleButton11.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton11ActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel21.setText("Analysis:");
        jLabel21.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel21.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel22.setText("Counts:");
        jLabel22.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel22.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel23.setBackground(new java.awt.Color(255, 255, 255));
        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel23.setText("Not Connected");

        jLabel24.setBackground(new java.awt.Color(255, 255, 255));
        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel24.setText("GAMMA SENSOR:");

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel25.setText("Dose Rate: ");
        jLabel25.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel25.setPreferredSize(new java.awt.Dimension(90, 19));

        jLabel27.setBackground(new java.awt.Color(255, 255, 255));
        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel27.setText("Not Connected");

        jLabel28.setBackground(new java.awt.Color(255, 255, 255));
        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel28.setText("GAMMA DOSIMETER:");

        m_ModeButtonGroup.add(jToggleButton9);
        jToggleButton9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton9.setText("LOITER");
        jToggleButton9.setPreferredSize(new java.awt.Dimension(115, 25));
        jToggleButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton9ActionPerformed(evt);
            }
        });

        m_ModeButtonGroup.add(jToggleButton12);
        jToggleButton12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton12.setText("INTERCEPT");
        jToggleButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton12ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("EXP TIME");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton13);
        jToggleButton13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton13.setText("OFF");
        jToggleButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton13ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton14);
        jToggleButton14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton14.setText("ON");
        jToggleButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton14ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton19);
        jToggleButton19.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton19.setText("INIT");
        jToggleButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton19ActionPerformed(evt);
            }
        });

        m_C100ButtonGroup.add(jToggleButton20);
        jToggleButton20.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton20.setText("WASH");
        jToggleButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton20ActionPerformed(evt);
            }
        });

        jLabel14.setBackground(new java.awt.Color(255, 255, 255));
        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel14.setText("WACS AUTOPILOT:");

        jLabel15.setBackground(new java.awt.Color(255, 255, 255));
        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel15.setText("Not Connected");

        jLabel29.setBackground(new java.awt.Color(255, 255, 255));
        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel29.setText("WACS IR CAMERA:");

        jLabel30.setBackground(new java.awt.Color(255, 255, 255));
        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel30.setText("Not Connected");

        m_IrRecorderButtonGroup.add(jToggleButton21);
        jToggleButton21.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton21.setText("REC OFF");
        jToggleButton21.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton21ActionPerformed(evt);
            }
        });

        m_IrRecorderButtonGroup.add(jToggleButton22);
        jToggleButton22.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jToggleButton22.setText("REC ON");
        jToggleButton22.setPreferredSize(new java.awt.Dimension(90, 23));
        jToggleButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton22ActionPerformed(evt);
            }
        });

        jButton23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton23.setText("ZERO AIR DATA");
        jButton23.setPreferredSize(new java.awt.Dimension(90, 23));
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jLabel31.setBackground(new java.awt.Color(255, 255, 255));
        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel31.setText("WACS Mode:");

        jLabel32.setBackground(new java.awt.Color(255, 255, 255));
        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel32.setText("Not Connected");

        jLabel33.setBackground(new java.awt.Color(255, 255, 255));
        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel33.setText("Allow Strike?");

        jLabel34.setBackground(new java.awt.Color(255, 255, 255));
        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel34.setText("Not Connected");

        jToggleButton24.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton24.setText("ALLOW STRIKE");
        jToggleButton24.setPreferredSize(new java.awt.Dimension(115, 25));
        jToggleButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton24ActionPerformed(evt);
            }
        });

        jLabel35.setBackground(new java.awt.Color(255, 255, 255));
        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel35.setText("WACS SETTINGS");

        jLabel36.setBackground(new java.awt.Color(255, 255, 255));
        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel36.setText("Tracking:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Unknown");

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setText("CHANGE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel37.setText("Align: ");
        jLabel37.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel37.setPreferredSize(new java.awt.Dimension(79, 19));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel38.setText("Detect: ");
        jLabel38.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton6, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton8, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton11, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .addComponent(jSeparator6, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                    .addComponent(jSeparator7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(jButton23, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton17, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                            .addComponent(jToggleButton13, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton14, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                            .addComponent(jToggleButton15, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton16, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .addComponent(jToggleButton19, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jToggleButton18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggleButton20, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel31)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton9, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton12, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToggleButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jLabel1)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38))
                .addGap(18, 18, 18)
                .addComponent(jLabel35)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton10ActionPerformed
        //Ibac on
        IbacStateBelief newBelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, true);
        m_BeliefManager.put(newBelief);
    }//GEN-LAST:event_jToggleButton10ActionPerformed

    private void jToggleButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton17ActionPerformed
        //C100 Sample 1
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample1);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton17ActionPerformed

    private void jToggleButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton11ActionPerformed
        //Alpha pump on
        AlphaSensorStateBelief aBelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, true);
        m_BeliefManager.put(aBelief);
    }//GEN-LAST:event_jToggleButton11ActionPerformed

    private void jToggleButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton9ActionPerformed
        //Command loiter mode
        ExplosionTimeCommandedBelief expBlf = (ExplosionTimeCommandedBelief)m_BeliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
        if (expBlf == null && Config.getConfig().getPropertyAsBoolean("WacsSettingsDefaults.UseExplosionTimer", true))
        {
            //require explosion time
            ExplosionTimeRequiredBelief timeReqBelief = new ExplosionTimeRequiredBelief();
            m_BeliefManager.put (timeReqBelief);
            
            new Thread (){
                public void run ()
                {
                    while (true)
                    {
                        ExplosionTimeCommandedBelief expBlf = (ExplosionTimeCommandedBelief)m_BeliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
                        if (expBlf != null)
                            break;

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(WACSSettingsPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    AgentModeCommandedBelief loitermode = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
                    m_BeliefManager.put(loitermode);
                }

            }.start();
        }
        else
        {
            AgentModeCommandedBelief loitermode = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
            m_BeliefManager.put(loitermode);
        }
    }//GEN-LAST:event_jToggleButton9ActionPerformed

    private void jToggleButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton12ActionPerformed
        //Command intercept mode
        boolean failed = true;
        AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)m_BeliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
        if (allowInterceptBlf != null && allowInterceptBlf.getAllow())
        {
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
            if(targets != null)
            {
                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                if(positionTimeName != null)
                {
                    m_BeliefManager.put(new ExplosionBelief(positionTimeName.getPosition(), System.currentTimeMillis()));
                    m_BeliefManager.put(new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(ParticleCloudPredictionBehavior.MODENAME)));
                    failed = false;
                }
            }
        }
        
        if (failed)
        {
            jToggleButton9ActionPerformed (null);
            /*AgentModeCommandedBelief loitermode = new AgentModeCommandedBelief(WACSAgent.AGENTNAME, new Mode(LoiterBehavior.MODENAME));
            m_BeliefManager.put(loitermode);*/
        }
    }//GEN-LAST:event_jToggleButton12ActionPerformed

    private void jToggleButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton22ActionPerformed
        // IR Rec on
        VideoClientRecorderCmdBelief newBelief = new VideoClientRecorderCmdBelief(WACSDisplayAgent.AGENTNAME, true);
        m_BeliefManager.put(newBelief);
    }//GEN-LAST:event_jToggleButton22ActionPerformed

    private void jToggleButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton24ActionPerformed
        //Command allow strike state
        AllowInterceptCommandedBelief commandAllowBlf;
                
        if(jToggleButton24.isSelected())
        {
            commandAllowBlf = new AllowInterceptCommandedBelief (WACSDisplayAgent.AGENTNAME, true);
        }
        else
        {
            commandAllowBlf = new AllowInterceptCommandedBelief (WACSDisplayAgent.AGENTNAME, false);
        }
         m_BeliefManager.put(commandAllowBlf);
    }//GEN-LAST:event_jToggleButton24ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Input explosion time
        
        //JOptionPane.showMessageDialog(this, "This should raise the explosion time dialog");
        
        //Need to pretend the checkbox was clicked in GcsDisplay here
        /*
        if (m_StrikeTimeMenuItem != null)
        {
            if (!m_StrikeTimeMenuItem.isSelected())
                m_StrikeTimeMenuItem.doClick();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Error showing strike time dialog, find it in the Window menu", "Error", JOptionPane.ERROR_MESSAGE);
        }
        */
        
        
        /*if (m_TimeDialog == null)
        {
        switch to use docked panel
            m_TimeDialog = new ExplosionTimeDialog(m_BeliefManager);
            m_TimeDialog.setLocation(MouseInfo.getPointerInfo().getLocation());
        }

        m_TimeDialog.setVisible(true);*/
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        //Anaconda Idle
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Idle);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        //Anaconda Standby
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Standby);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        //Anaconda Search 1
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search1);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        //Anaconda Search 2
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search2);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        //Anaconda Search 3
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search3);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        //Anaconda Search 4
        AnacondaStateBelief anbelief = new AnacondaStateBelief(WACSDisplayAgent.AGENTNAME, AnacondaModeEnum.Search4);
        m_BeliefManager.put(anbelief);
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        //Ibac off
        IbacStateBelief newBelief = new IbacStateBelief(WACSDisplayAgent.AGENTNAME, false);
        m_BeliefManager.put(newBelief);
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        //Need to pretend the checkbox was clicked in GcsDisplay here
        /*
        if (m_ZeroAirDataMenuItem != null)
        {
            if (!m_ZeroAirDataMenuItem.isSelected())
                m_ZeroAirDataMenuItem.doClick();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Error showing zero air data dialog, find it in the Window menu", "Error", JOptionPane.ERROR_MESSAGE);
        }
        */
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jToggleButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton21ActionPerformed
        //IR Rec off
        VideoClientRecorderCmdBelief newBelief = new VideoClientRecorderCmdBelief(WACSDisplayAgent.AGENTNAME, false);
        m_BeliefManager.put(newBelief);
    }//GEN-LAST:event_jToggleButton21ActionPerformed

    private void jToggleButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton13ActionPerformed
        //C100 Off
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Idle);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton13ActionPerformed

    private void jToggleButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton14ActionPerformed
        //C100 On
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Collecting);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton14ActionPerformed

    private void jToggleButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton19ActionPerformed
        //C100 Prime
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Priming);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton19ActionPerformed

    private void jToggleButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton20ActionPerformed
        //C100 Clean
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.Cleaning);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton20ActionPerformed

    private void jToggleButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton15ActionPerformed
        //C100 Sample 2
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample2);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton15ActionPerformed

    private void jToggleButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton16ActionPerformed
        //C100 Sample 3
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample3);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton16ActionPerformed

    private void jToggleButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton18ActionPerformed
        //C100 Sample 4
        ParticleCollectorStateBelief cBelief = new ParticleCollectorStateBelief(WACSDisplayAgent.AGENTNAME, ParticleCollectorMode.StoringSample4);
        m_BeliefManager.put (cBelief);
    }//GEN-LAST:event_jToggleButton18ActionPerformed

    private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton8ActionPerformed
        //Alpha pump off
        AlphaSensorStateBelief aBelief = new AlphaSensorStateBelief(WACSDisplayAgent.AGENTNAME, false);
        m_BeliefManager.put(aBelief);
    }//GEN-LAST:event_jToggleButton8ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Set cloud tracking type
        String currentTypeName = "MIXTURE";
        ParticleCloudTrackingTypeActualBelief currBelief = (ParticleCloudTrackingTypeActualBelief)m_BeliefManager.get(ParticleCloudTrackingTypeActualBelief.BELIEF_NAME);
        if (currBelief != null)
        {
            if (currBelief.getTrackingType() == ParticleCloudPredictionBehavior.TRACKING_TYPE.MIXTURE)
                currentTypeName = "MIXTURE";
            else if (currBelief.getTrackingType() == ParticleCloudPredictionBehavior.TRACKING_TYPE.PARTICLE)
                currentTypeName = "PARTICLE";
            else if (currBelief.getTrackingType() == ParticleCloudPredictionBehavior.TRACKING_TYPE.CHEMICAL)
                currentTypeName = "CHEMICAL";

        }

        Object options[] = {"MIXTURE", "PARTICLES", "CHEMICALS"};
        int opt = JOptionPane.showOptionDialog(this, "What detection types should be tracked (Current: " + currentTypeName + ")?", "Choose Tracking Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        ParticleCloudTrackingTypeCommandedBelief belief = null;
        if (opt == 0)
            belief = new ParticleCloudTrackingTypeCommandedBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE.MIXTURE);
        else if (opt == 1)
            belief = new ParticleCloudTrackingTypeCommandedBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE.PARTICLE);
        else if (opt == 2)
            belief = new ParticleCloudTrackingTypeCommandedBelief(ParticleCloudPredictionBehavior.TRACKING_TYPE.CHEMICAL);

        if (belief != null)
            m_BeliefManager.put(belief);
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton23;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton17;
    private javax.swing.JToggleButton jToggleButton18;
    private javax.swing.JToggleButton jToggleButton19;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton20;
    private javax.swing.JToggleButton jToggleButton21;
    private javax.swing.JToggleButton jToggleButton22;
    private javax.swing.JToggleButton jToggleButton24;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.ButtonGroup m_AlphaButtonGroup;
    private javax.swing.ButtonGroup m_AnacondaButtonGroup;
    private javax.swing.ButtonGroup m_C100ButtonGroup;
    private javax.swing.ButtonGroup m_IbacButtonGroup;
    private javax.swing.ButtonGroup m_IrRecorderButtonGroup;
    private javax.swing.ButtonGroup m_ModeButtonGroup;
    // End of variables declaration//GEN-END:variables

    
}
