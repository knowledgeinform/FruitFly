/*
 * BridgeportPanel.java
 *
 * Created on January 25, 2010, 2:02 PM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaEthernetCountRates;
import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportEthernetMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportOutputDisplayGraph;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCalibration;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.cbrnPods.bridgeportProcessor;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportHistogramMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.spectra.AETNAResult.IsotopeConcentraion;
import edu.jhuapl.spectra.AETNAServiceInterface2;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;

/**
 *
 * @author  humphjc1
 */
public class BridgeportEthernetPanel extends javax.swing.JPanel implements BridgeportEthernetMessageListener, cbrnPodMessageListener {

    BridgeportOutputDisplayGraph m_DisplayGraphHist = new BridgeportOutputDisplayGraph();
    RNTotalCountsDisplayGraphPanel m_DisplayGraphCounts = new RNTotalCountsDisplayGraphPanel(300000);
    

    cbrnPodsInterface m_Pods;
    BeliefManager m_BelMgr = null;

    RNHistogramDisplayGraphPanel graphHist = null;
    RNTotalCountsDisplayGraphPanel graphCounts = null;
    boolean accumlatingBackground = false;

    RNTotalCountsTracker totalCountsTracker;
    int m_GammaCountsToAverageTracker;
    int m_GammaStdDevLimitTracker;
    int m_GammaMinimumCountsTracker;

    
    
    /** Creates new form BridgeportPanel */
    public BridgeportEthernetPanel(cbrnPodsInterface pods, BeliefManager belMgr) {
        initComponents();
        
        m_Pods = pods;
        m_BelMgr = belMgr;

        m_GammaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaCountsToAverageTracker", 5);
        m_GammaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaStdDevLimitTracker", 3);
        m_GammaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.GammaMinimumCountsTracker", 100);
        totalCountsTracker = new RNTotalCountsTracker(m_GammaCountsToAverageTracker, m_GammaStdDevLimitTracker, m_GammaMinimumCountsTracker);


        ButtonGroup histoType = new ButtonGroup();
        histoType.add(m_LiveButton);
        histoType.add(m_AccumButton);
        
        this.setVisible(true);
        
        m_DisplayGraphHist.setSize(m_Panel.getWidth(), m_Panel.getHeight());
        m_Panel.add (m_DisplayGraphHist);
        m_DisplayGraphHist.setVisible(true);
        m_DisplayGraphCounts.setSize(m_Panel.getWidth(), m_Panel.getHeight());
        m_Panel.add (m_DisplayGraphCounts);
        m_DisplayGraphCounts.setVisible(false);
        
        m_Pods.addBridgeportListener(this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_CONFIGURATION, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_HISTOGRAM, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_STATISTICS, this);

        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_COMPOSITE_HISTOGRAM, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_DETECTION_REPORT, this);
    }
    
    @Override
    public void handleHistogramMessage(RNHistogram m)
    {
        //Some stupid thing because I can't get the graph to resize itself in the constructor
        if (m_DisplayGraphHist.getWidth() == 0)
            m_DisplayGraphHist.setSize(m_Panel.getWidth(), m_Panel.getHeight());
        if (m_DisplayGraphCounts.getWidth() == 0)
            m_DisplayGraphCounts.setSize(m_Panel.getWidth(), m_Panel.getHeight());

        m_DisplayGraphHist.updateCurrentHistogram (m);

        double altitudeM = 0;
        if (m_BelMgr != null)
        {
            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)m_BelMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if (picBlf != null)
                altitudeM = picBlf.getPiccoloTelemetry().AltWGS84;
        }
        if (m_DisplayGraphHist.getAccumulatingHistogram() == null)
            m_DisplayGraphHist.addAccumulatingHistogram(m.getCopyOfHistogram());
        int sumCounts = 0;
        for (int i = 0; i < m.getNumBins(); i++)
            sumCounts += m.getRawValue(i);
        m_DisplayGraphCounts.updateCountsAltitudeTime(sumCounts, altitudeM, System.currentTimeMillis());
    }

    @Override
    public void handleStatisticsMessage(final GammaEthernetCountRates m) 
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                m_statOut1.setText(" " + m.getRealTicks());
                m_statOut2.setText(" " + m.getNumEvents());
                m_statOut3.setText(" " + m.getNumTriggers());
                m_statOut4.setText(" " + m.getDeadTicks());
                m_statOut5.setText(" " + m.getRealTime());
                m_statOut6.setText(" " + m.getEventRate());
                m_statOut7.setText(" " + m.getTriggerRate());
                m_statOut8.setText(" " + m.getDeadTimeFraction());
                m_statOut9.setText(" " + m.getInputRate()); 
            }
        });
        
    }

    @Override
    public void handleCalibrationMessage (final GammaCalibration m)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                m_calibOut1.setText (" " + m.getADC_Speed());
                m_calibOut2.setText (" " + m.getNumADC_Bits());
                m_calibOut3.setText (" " + m.getADC_FSR());
                m_calibOut4.setText (" " + m.getGain());
                m_calibOut5.setText (" " + m.getMCA_CurrFSR());
                m_calibOut6.setText (" " + m.getMCA_CurrMax());
                m_calibOut7.setText (" " + m.getChargePerUnit());
                m_calibOut8.setText (" " + m.getDC_Offset());
                m_calibOut9.setText (" " + m.getTemperature());
                m_calibOut10.setText (" " + m.getMCA_AnodeCurrent());
                m_calibOut11.setText (" " + m.getMCA_ROI_Avg());
                m_calibOut12.setText (" " + m.getNumHistBytes());
                m_calibOut13.setText (" " + m.getNumListBytes());
                m_calibOut14.setText (" " + m.getNumTraceBytes());
                m_calibOut15.setText (" " + m.getNumModule6Bytes());
                m_calibOut16.setText (" " + m.getPacketCounter());
            }
        });
        
    }
    
    
    @Override
    public void handleMessage(cbrnPodMsg m) 
    {
        if (graphHist == null && jPanel1.getWidth() > 0)
        {
            graphHist = new RNHistogramDisplayGraphPanel ();
            graphHist.setTitle("Gamma Count Histogram");
            graphHist.setSize(jPanel1.getWidth(), jPanel1.getHeight());
            graphHist.selectLogScale(true);
            jPanel1.add (graphHist);
            graphHist.setVisible(true);
        }
        if (graphCounts == null && jPanel1.getWidth() > 0)
        {
            graphCounts = new RNTotalCountsDisplayGraphPanel (300000);
            graphCounts.setTitle("Gamma Total Counts");
            graphCounts.setSize(jPanel1.getWidth(), jPanel1.getHeight());
            jPanel1.add (graphCounts);
            graphCounts.setVisible (false);
        }
        
        if (m instanceof bridgeportHistogramMessage)
        {
            String text = "" + ((bridgeportHistogramMessage)m).getHistIndex() + " (" + ((bridgeportHistogramMessage)m).getPacketIndex() + " of " + ((bridgeportHistogramMessage)m).getNumPackets() + ")";
            jLabel13.setText(text);
        }
        else if(m instanceof bridgeportCompositeHistogramMessage)
        {
            if (graphHist == null || graphCounts == null)
                return;
            handleCompositeHistogramMessage((bridgeportCompositeHistogramMessage)m);
        }
        else if(m instanceof bridgeportDetectionReportMessage)
        {
            if (graphHist == null || graphCounts == null)
                return;
            handleDetectionReportMessage((bridgeportDetectionReportMessage)m);
        }
        
    }


    private void handleCompositeHistogramMessage(bridgeportCompositeHistogramMessage msg)
    {
        graphHist.setLiveTime((int)(msg.getLiveTime()/1000));
        graphHist.setStatMessage("Total Counts = " + msg.getTotalCounts());

        if (!accumlatingBackground)
        {
            String countsAlertMessage = totalCountsTracker.getCountsAlertMessage(msg.getTotalCounts());
            graphHist.setStatAlertMessage(countsAlertMessage);
        }

        graphHist.updateCurrentHistogram(new RNHistogram(msg.copySummedSpectra(), msg.copyClassifiedSpectra()));
        graphCounts.setStatMessage("Total Counts = " + msg.getTotalCounts());

        if (!accumlatingBackground)
        {
            String countsAlertMessage = totalCountsTracker.getCountsAlertMessage(msg.getTotalCounts());
            graphCounts.setStatAlertMessage(countsAlertMessage);
        }

        double altitudeM = 0;
        if (m_BelMgr != null)
        {
            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)m_BelMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if (picBlf != null)
                altitudeM = picBlf.getPiccoloTelemetry().AltWGS84;
        }
        graphCounts.updateCountsAltitudeTime((int)msg.getTotalCounts(), altitudeM, msg.getTimestampMs());
    }

    private void handleDetectionReportMessage(bridgeportDetectionReportMessage msg)
    {
        DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";

        Vector <IsotopeConcentraion> isotopes = msg.accessIsotopes();
        for (int i = 0; i < isotopes.size(); i ++)
        {
            detmsg += isotopes.get(i).isotope + ":" + df.format (isotopes.get(i).concentration) + "% - ";

            if (i == 0 && isotopes.get(i).isotope.equals(bridgeportProcessor.accumBackgroundMsg))
                accumlatingBackground = true;
            else if (i == 0)
                accumlatingBackground = false;
        }
        
        graphHist.setDetectionMessage(detmsg);
        graphHist.repaint();
        graphCounts.setDetectionMessage(detmsg);
        graphCounts.repaint();
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel10 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        m_statOut1 = new javax.swing.JLabel();
        m_statOut2 = new javax.swing.JLabel();
        m_statOut3 = new javax.swing.JLabel();
        m_statOut4 = new javax.swing.JLabel();
        m_statOut5 = new javax.swing.JLabel();
        m_statOut6 = new javax.swing.JLabel();
        m_statOut7 = new javax.swing.JLabel();
        m_statOut8 = new javax.swing.JLabel();
        m_statOut9 = new javax.swing.JLabel();
        m_Panel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        m_LiveButton = new javax.swing.JRadioButton();
        m_AccumButton = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        m_calibOut5 = new javax.swing.JLabel();
        m_calibOut6 = new javax.swing.JLabel();
        m_calibOut3 = new javax.swing.JLabel();
        m_calibOut4 = new javax.swing.JLabel();
        m_calibOut9 = new javax.swing.JLabel();
        m_calibOut7 = new javax.swing.JLabel();
        m_calibOut8 = new javax.swing.JLabel();
        m_calibOut13 = new javax.swing.JLabel();
        m_calibOut12 = new javax.swing.JLabel();
        m_calibOut11 = new javax.swing.JLabel();
        m_calibOut10 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        m_calibOut2 = new javax.swing.JLabel();
        m_calibOut1 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        m_calibOut15 = new javax.swing.JLabel();
        m_calibOut14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        m_calibOut16 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel31 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Input Rate");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Real Ticks");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Events");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Triggers");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Dead Ticks");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Real Time");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Event Rate");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Trigger Rate");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Dead Time Frac");

        m_statOut1.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut1.setText(" ");
        m_statOut1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut1.setOpaque(true);

        m_statOut2.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut2.setText(" ");
        m_statOut2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut2.setOpaque(true);

        m_statOut3.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut3.setText(" ");
        m_statOut3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut3.setOpaque(true);

        m_statOut4.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut4.setText(" ");
        m_statOut4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut4.setOpaque(true);

        m_statOut5.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut5.setText(" ");
        m_statOut5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut5.setOpaque(true);

        m_statOut6.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut6.setText(" ");
        m_statOut6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut6.setOpaque(true);

        m_statOut7.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut7.setText(" ");
        m_statOut7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut7.setOpaque(true);

        m_statOut8.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut8.setText(" ");
        m_statOut8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut8.setOpaque(true);

        m_statOut9.setBackground(new java.awt.Color(255, 255, 255));
        m_statOut9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_statOut9.setText(" ");
        m_statOut9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_statOut9.setOpaque(true);

        m_Panel.setPreferredSize(new java.awt.Dimension(200, 100));

        javax.swing.GroupLayout m_PanelLayout = new javax.swing.GroupLayout(m_Panel);
        m_Panel.setLayout(m_PanelLayout);
        m_PanelLayout.setHorizontalGroup(
            m_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 782, Short.MAX_VALUE)
        );
        m_PanelLayout.setVerticalGroup(
            m_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 285, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setText("Statistics Data");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel11.setText("Histogram Data");

        m_LiveButton.setSelected(true);
        m_LiveButton.setText("Live Data");
        m_LiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_LiveButtonActionPerformed(evt);
            }
        });

        m_AccumButton.setText("Accumulated Data");
        m_AccumButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AccumButtonActionPerformed(evt);
            }
        });

        jLabel12.setText("Last hist recvd on BL2600:");

        jLabel13.setText("N/A");

        jCheckBox1.setText("Log Scale Y");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setText("Zero Req Channels (Display Only)");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 931, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
        );

        jLabel14.setText("Processing results:");

        jButton1.setText("Reset Accum");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        m_calibOut5.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut5.setText(" ");
        m_calibOut5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut5.setOpaque(true);

        m_calibOut6.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut6.setText(" ");
        m_calibOut6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut6.setOpaque(true);

        m_calibOut3.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut3.setText(" ");
        m_calibOut3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut3.setOpaque(true);

        m_calibOut4.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut4.setText(" ");
        m_calibOut4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut4.setOpaque(true);

        m_calibOut9.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut9.setText(" ");
        m_calibOut9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut9.setOpaque(true);

        m_calibOut7.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut7.setText(" ");
        m_calibOut7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut7.setOpaque(true);

        m_calibOut8.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut8.setText(" ");
        m_calibOut8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut8.setOpaque(true);

        m_calibOut13.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut13.setText(" ");
        m_calibOut13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut13.setOpaque(true);

        m_calibOut12.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut12.setText(" ");
        m_calibOut12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut12.setOpaque(true);

        m_calibOut11.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut11.setText(" ");
        m_calibOut11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut11.setOpaque(true);

        m_calibOut10.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut10.setText(" ");
        m_calibOut10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut10.setOpaque(true);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("# Trace Bytes");

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("# Listmode Bytes");

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Packet Counter");

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("# Module6 Bytes");

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("ADC Speed");

        m_calibOut2.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut2.setText(" ");
        m_calibOut2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut2.setOpaque(true);

        m_calibOut1.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut1.setText(" ");
        m_calibOut1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut1.setOpaque(true);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("MCA ROI Avg");

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("# Hist Bytes");

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Temperature");

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("MCA Anode Curr");

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Gain");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("ADC FSR");

        m_calibOut15.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut15.setText(" ");
        m_calibOut15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut15.setOpaque(true);

        m_calibOut14.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut14.setText(" ");
        m_calibOut14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut14.setOpaque(true);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("# ADC Bits");

        m_calibOut16.setBackground(new java.awt.Color(255, 255, 255));
        m_calibOut16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_calibOut16.setText(" ");
        m_calibOut16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        m_calibOut16.setOpaque(true);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("MCA Curr Max");

        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("Charge / Unit");

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("DC Offset");

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel30.setText("MCA Curr FSR");

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel31.setText("Calibration Data");

        jCheckBox3.setText("Total Counts Plot");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jCheckBox4.setText("Total Counts Plot");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        jButton2.setText("Reset Background");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut4, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut3, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut8, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut7, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut6, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut5, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(70, 70, 70)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut10, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut11, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut12, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut9, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut15, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut16, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut14, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_calibOut13, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(158, 158, 158))
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel31)
                .addContainerGap(972, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 982, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(127, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 982, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 352, Short.MAX_VALUE)
                                        .addComponent(jButton2)
                                        .addGap(282, 282, 282)
                                        .addComponent(jCheckBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(m_statOut1, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                            .addComponent(m_statOut2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(m_statOut9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel12))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 782, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(468, 468, 468)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(m_AccumButton)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(m_LiveButton)
                                        .addGap(95, 95, 95)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jCheckBox2)
                                            .addComponent(jCheckBox1))))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jLabel1)
                        .addGap(136, 136, 136)
                        .addComponent(jLabel11)))
                .addContainerGap(127, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel11)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_LiveButton)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_AccumButton)
                            .addComponent(jCheckBox2)
                            .addComponent(jButton1))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(m_statOut1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(m_statOut2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(m_statOut3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(m_statOut4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(m_statOut5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(m_statOut6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(m_statOut7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(m_statOut8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(m_statOut9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addComponent(m_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jButton2)
                    .addComponent(jCheckBox4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel31)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_calibOut1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(m_calibOut5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel20)
                    .addComponent(m_calibOut9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(m_calibOut13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(m_calibOut10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_calibOut14, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut11, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(m_calibOut15, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23)
                            .addComponent(m_calibOut16, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(m_calibOut8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void m_LiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_LiveButtonActionPerformed
    m_DisplayGraphHist.chooseLiveHistogram();
}//GEN-LAST:event_m_LiveButtonActionPerformed

private void m_AccumButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AccumButtonActionPerformed
    m_DisplayGraphHist.chooseAccumHistogram();
}//GEN-LAST:event_m_AccumButtonActionPerformed

private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
    // TODO add your handling code here:
    m_DisplayGraphHist.selectLogScale (jCheckBox1.isSelected());
}//GEN-LAST:event_jCheckBox1ActionPerformed

private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
    // TODO add your handling code here:
    m_DisplayGraphHist.setZeroReqChannels(jCheckBox2.isSelected());
}//GEN-LAST:event_jCheckBox2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
    m_DisplayGraphHist.resetAccumulatingHistogram();
}//GEN-LAST:event_jButton1ActionPerformed

private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
    // TODO add your handling code here:
    boolean showCounts = jCheckBox3.isSelected();

    m_DisplayGraphHist.setVisible(!showCounts);
    m_DisplayGraphCounts.setVisible(showCounts);
}//GEN-LAST:event_jCheckBox3ActionPerformed

private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
    // TODO add your handling code here:
    boolean showCounts = jCheckBox4.isSelected();

    graphHist.setVisible(!showCounts);
    graphCounts.setVisible(showCounts);
}//GEN-LAST:event_jCheckBox4ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    // TODO add your handling code here:

    if (m_Pods != null)
        m_Pods.resetBridgeportBackgroundSpectra();
}//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JRadioButton m_AccumButton;
    private javax.swing.JRadioButton m_LiveButton;
    private javax.swing.JPanel m_Panel;
    private javax.swing.JLabel m_calibOut1;
    private javax.swing.JLabel m_calibOut10;
    private javax.swing.JLabel m_calibOut11;
    private javax.swing.JLabel m_calibOut12;
    private javax.swing.JLabel m_calibOut13;
    private javax.swing.JLabel m_calibOut14;
    private javax.swing.JLabel m_calibOut15;
    private javax.swing.JLabel m_calibOut16;
    private javax.swing.JLabel m_calibOut2;
    private javax.swing.JLabel m_calibOut3;
    private javax.swing.JLabel m_calibOut4;
    private javax.swing.JLabel m_calibOut5;
    private javax.swing.JLabel m_calibOut6;
    private javax.swing.JLabel m_calibOut7;
    private javax.swing.JLabel m_calibOut8;
    private javax.swing.JLabel m_calibOut9;
    private javax.swing.JLabel m_statOut1;
    private javax.swing.JLabel m_statOut2;
    private javax.swing.JLabel m_statOut3;
    private javax.swing.JLabel m_statOut4;
    private javax.swing.JLabel m_statOut5;
    private javax.swing.JLabel m_statOut6;
    private javax.swing.JLabel m_statOut7;
    private javax.swing.JLabel m_statOut8;
    private javax.swing.JLabel m_statOut9;
    // End of variables declaration//GEN-END:variables



}
