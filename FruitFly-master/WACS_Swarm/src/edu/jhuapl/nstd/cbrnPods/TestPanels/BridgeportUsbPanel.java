/*
 * BridgeportPanel.java
 *
 * Created on January 25, 2010, 2:02 PM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.cbrnPods.bridgeportProcessor;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportOutputDisplayGraph;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.BridgeportUsbMessageListener;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage.GammaDetectorMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportHistogramMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.spectra.AETNAResult.IsotopeConcentraion;

/**
 *
 * @author  humphjc1
 */
public class BridgeportUsbPanel extends javax.swing.JPanel implements BridgeportUsbMessageListener, cbrnPodMessageListener {

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
    public BridgeportUsbPanel(cbrnPodsInterface pods, BeliefManager belMgr) {
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
    public void handleSensorDriverMessage(final SensorDriverMessage msg)
    {
        // TODO Assuming only one gamma detector message here!!!!
    	final GammaDetectorMessage gammaMsg = msg.getGammaDetectorMessageList().get(0);
    	final RNHistogram hist = gammaMsg.getSpectrum();
        
    	//Some stupid thing because I can't get the graph to resize itself in the constructor
        if (m_DisplayGraphHist.getWidth() == 0)
            m_DisplayGraphHist.setSize(m_Panel.getWidth(), m_Panel.getHeight());
        if (m_DisplayGraphCounts.getWidth() == 0)
            m_DisplayGraphCounts.setSize(m_Panel.getWidth(), m_Panel.getHeight());
        
        // Update the statistics values
        if(gammaMsg.getStatus())
        {
	        statusValueLabel.setText("CONNECTED");
	        statusValueLabel.setBackground(Color.GREEN);
        }
        else
        {
        	statusValueLabel.setText("DISCONNECTED");
	        statusValueLabel.setBackground(Color.RED);
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                realTimeValueLabel.setText(String.valueOf(gammaMsg.getRealTime()));
                liveTimeValueLabel.setText(String.valueOf(gammaMsg.getLiveTime()));
                deadTimeValueLabel.setText(String.valueOf(gammaMsg.getDeadTime()));
                totalCountValueLabel.setText(String.valueOf(gammaMsg.getTotalCount()));
                String countRateStr = String.format("%.6f", gammaMsg.getCountRate());
                countRateValueLabel.setText(countRateStr);
                temperatureValueLabel.setText(String.valueOf(msg.getTemperature()));
                highVoltageValueLabel.setText(String.valueOf(gammaMsg.getHighVoltage()));
                fineGainValueLabel.setText(String.valueOf(gammaMsg.getFineGain()));
                coarseGainValueLabel.setText(String.valueOf(gammaMsg.getCoarseGain()));
            }
        });
        
		// Update the histogram
        m_DisplayGraphHist.updateCurrentHistogram(hist);

        double altitudeM = 0;
        if (m_BelMgr != null)
        {
            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)m_BelMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if (picBlf != null)
                altitudeM = picBlf.getPiccoloTelemetry().AltWGS84;
        }
        if (m_DisplayGraphHist.getAccumulatingHistogram() == null)
            m_DisplayGraphHist.addAccumulatingHistogram(hist.getCopyOfHistogram());
        int sumCounts = 0;
        for (int i = 0; i < hist.getNumBins(); i++)
            sumCounts += hist.getRawValue(i);
        m_DisplayGraphCounts.updateCountsAltitudeTime(sumCounts, altitudeM, System.currentTimeMillis());
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
            jPanel1.add(graphHist);
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

        countRateLabel = new javax.swing.JLabel();
        liveTimeLabel = new javax.swing.JLabel();
        deadTimeLabel = new javax.swing.JLabel();
        realTimeLabel = new javax.swing.JLabel();
        liveTimeValueLabel = new javax.swing.JLabel();
        deadTimeValueLabel = new javax.swing.JLabel();
        realTimeValueLabel = new javax.swing.JLabel();
        countRateValueLabel = new javax.swing.JLabel();
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
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        totalCountLabel = new javax.swing.JLabel();
        totalCountValueLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        statusValueLabel = new javax.swing.JLabel();
        temperatureLabel = new javax.swing.JLabel();
        temperatureValueLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        setHighVoltageTextField = new javax.swing.JTextField();
        setHighVoltageButton = new javax.swing.JButton();
        setFineGainButton = new javax.swing.JButton();
        setFineGainTextField = new javax.swing.JTextField();
        highVoltageLabel = new javax.swing.JLabel();
        highVoltageValueLabel = new javax.swing.JLabel();
        fineGainValueLabel = new javax.swing.JLabel();
        fineGainLabel = new javax.swing.JLabel();
        coarseGainLabel = new javax.swing.JLabel();
        coarseGainValueLabel = new javax.swing.JLabel();

        countRateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        countRateLabel.setText("Count Rate");

        liveTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        liveTimeLabel.setText("Live Time");

        deadTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        deadTimeLabel.setText("Dead Time");

        realTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        realTimeLabel.setText("Real Time");

        liveTimeValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        liveTimeValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        liveTimeValueLabel.setText(" ");
        liveTimeValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        liveTimeValueLabel.setOpaque(true);

        deadTimeValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        deadTimeValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        deadTimeValueLabel.setText(" ");
        deadTimeValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        deadTimeValueLabel.setOpaque(true);

        realTimeValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        realTimeValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        realTimeValueLabel.setText(" ");
        realTimeValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        realTimeValueLabel.setOpaque(true);

        countRateValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        countRateValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        countRateValueLabel.setText(" ");
        countRateValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        countRateValueLabel.setOpaque(true);

        m_Panel.setPreferredSize(new java.awt.Dimension(200, 100));

        javax.swing.GroupLayout m_PanelLayout = new javax.swing.GroupLayout(m_Panel);
        m_Panel.setLayout(m_PanelLayout);
        m_PanelLayout.setHorizontalGroup(
            m_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 710, Short.MAX_VALUE)
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
            .addGap(0, 910, Short.MAX_VALUE)
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

        totalCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalCountLabel.setText("Total Count");

        totalCountValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        totalCountValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        totalCountValueLabel.setText(" ");
        totalCountValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        totalCountValueLabel.setOpaque(true);

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusLabel.setText("Status");

        statusValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        statusValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusValueLabel.setText(" ");
        statusValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        statusValueLabel.setOpaque(true);

        temperatureLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        temperatureLabel.setText("Temperature (C)");

        temperatureValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        temperatureValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        temperatureValueLabel.setText(" ");
        temperatureValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        temperatureValueLabel.setOpaque(true);

        setHighVoltageButton.setText("Set High Voltage");
        setHighVoltageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setHighVoltageButtonActionPerformed(evt);
            }
        });

        setFineGainButton.setText("Set Fine Gain");
        setFineGainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFineGainButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(setFineGainButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(setHighVoltageButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(setFineGainTextField)
                    .addComponent(setHighVoltageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                .addContainerGap(527, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setHighVoltageButton)
                    .addComponent(setHighVoltageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setFineGainButton)
                    .addComponent(setFineGainTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        highVoltageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        highVoltageLabel.setText("High Voltage (V)");

        highVoltageValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        highVoltageValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        highVoltageValueLabel.setText(" ");
        highVoltageValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        highVoltageValueLabel.setOpaque(true);

        fineGainValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        fineGainValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        fineGainValueLabel.setText(" ");
        fineGainValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        fineGainValueLabel.setOpaque(true);

        fineGainLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fineGainLabel.setText("Fine Gain");

        coarseGainLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        coarseGainLabel.setText("Coarse Gain");

        coarseGainValueLabel.setBackground(new java.awt.Color(255, 255, 255));
        coarseGainValueLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        coarseGainValueLabel.setText(" ");
        coarseGainValueLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        coarseGainValueLabel.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 458, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addGap(144, 144, 144))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jLabel1)
                        .addGap(136, 136, 136)
                        .addComponent(jLabel11)
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_AccumButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(m_LiveButton)
                                .addGap(95, 95, 95)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox2)
                                    .addComponent(jCheckBox1))))
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox3))
                .addGap(184, 184, 184))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 945, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(15, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(temperatureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(temperatureValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(deadTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deadTimeValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(statusValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(liveTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(liveTimeValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(realTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(realTimeValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(countRateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                            .addComponent(totalCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(countRateValueLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                            .addComponent(totalCountValueLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel12))
                                .addGap(33, 33, 33))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(highVoltageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(highVoltageValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(coarseGainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(coarseGainValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(fineGainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fineGainValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_Panel, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE))))
                .addGap(29, 29, 29))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel11))
                    .addGroup(layout.createSequentialGroup()
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
                            .addComponent(statusValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(realTimeValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(realTimeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(liveTimeLabel)
                            .addComponent(liveTimeValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deadTimeLabel)
                            .addComponent(deadTimeValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(totalCountValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalCountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(countRateValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(countRateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(temperatureValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(temperatureLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(highVoltageValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(highVoltageLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fineGainValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fineGainLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(coarseGainValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(coarseGainLabel))
                        .addGap(63, 63, 63)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jButton2)
                    .addComponent(jCheckBox4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
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

private void setHighVoltageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setHighVoltageButtonActionPerformed
    String newHighVoltage = setHighVoltageTextField.getText();
    if(newHighVoltage != null && !newHighVoltage.equals(""))
    {
        try
        {
            m_Pods.setHighVoltage(Double.parseDouble(newHighVoltage));
        }
        catch(NumberFormatException nfe)
        {
            // TODO What to do when the user provides a non-numeric value here?
        }
    }
}//GEN-LAST:event_setHighVoltageButtonActionPerformed

private void setFineGainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFineGainButtonActionPerformed
    String newFineGain = setFineGainTextField.getText();
    if(newFineGain != null && !newFineGain.equals(""))
    {
        try
        {
            m_Pods.setFineGain(Double.parseDouble(newFineGain));
        }
        catch(NumberFormatException nfe)
        {
            // TODO What to do when the user provides a non-numeric value here?
        }
    }
}//GEN-LAST:event_setFineGainButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel coarseGainLabel;
    private javax.swing.JLabel coarseGainValueLabel;
    private javax.swing.JLabel countRateLabel;
    private javax.swing.JLabel countRateValueLabel;
    private javax.swing.JLabel deadTimeLabel;
    private javax.swing.JLabel deadTimeValueLabel;
    private javax.swing.JLabel fineGainLabel;
    private javax.swing.JLabel fineGainValueLabel;
    private javax.swing.JLabel highVoltageLabel;
    private javax.swing.JLabel highVoltageValueLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel liveTimeLabel;
    private javax.swing.JLabel liveTimeValueLabel;
    private javax.swing.JRadioButton m_AccumButton;
    private javax.swing.JRadioButton m_LiveButton;
    private javax.swing.JPanel m_Panel;
    private javax.swing.JLabel realTimeLabel;
    private javax.swing.JLabel realTimeValueLabel;
    private javax.swing.JButton setFineGainButton;
    private javax.swing.JTextField setFineGainTextField;
    private javax.swing.JButton setHighVoltageButton;
    private javax.swing.JTextField setHighVoltageTextField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusValueLabel;
    private javax.swing.JLabel temperatureLabel;
    private javax.swing.JLabel temperatureValueLabel;
    private javax.swing.JLabel totalCountLabel;
    private javax.swing.JLabel totalCountValueLabel;
    // End of variables declaration//GEN-END:variables


}
