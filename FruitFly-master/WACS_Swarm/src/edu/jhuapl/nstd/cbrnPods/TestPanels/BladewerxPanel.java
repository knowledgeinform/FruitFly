package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxAETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCalibrationCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDetectionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDllDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxGetAdcCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxPowerOffCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxPowerOnCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxRawCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxSetAdcCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxSetGainCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxSetOffsetCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxSetScaleCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxSetThresholdCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxVersionCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpPowerCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetClosedCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetOpenCommand;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.spectra.AETNAResult.IsotopeConcentraion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  humphjc1
 */
public class BladewerxPanel extends javax.swing.JPanel implements cbrnPodMessageListener
{
    cbrnPodsInterface m_Pods;
    BeliefManager m_BelMgr = null;
    
    ArrayList<Method> detectionMethods;
    ArrayList<Method> statusMethods;
    ArrayList<Method> pumpMethods;
    
    RNHistogramDisplayGraphPanel graphHist = null;
    RNTotalCountsDisplayGraphPanel graphCounts = null;
    RNTotalCountsTracker totalCountsTracker;
    int m_AlphaCountsToAverageTracker;
    int m_AlphaStdDevLimitTracker;
    int m_AlphaMinimumCountsTracker;

    
    /** Creates new form BladewerxPanel */
    public BladewerxPanel(cbrnPodsInterface pods, BeliefManager belMgr) {
        initComponents();
        m_BelMgr = belMgr;

        m_AlphaCountsToAverageTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaCountsToAverageTracker", 5);
        m_AlphaStdDevLimitTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaStdDevLimitTracker", 3);
        m_AlphaMinimumCountsTracker = Config.getConfig().getPropertyAsInteger("RNTotalCountsTracker.AlphaMinimumCountsTracker", 10);
        totalCountsTracker = new RNTotalCountsTracker(m_AlphaCountsToAverageTracker, m_AlphaStdDevLimitTracker, m_AlphaMinimumCountsTracker);


        msgOutput.setModel(new DefaultListModel());
        
        
        //Set-up action message table
        DefaultTableModel detectionModel = new DefaultTableModel();
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable1.setModel(
                detectionModel);
        detectionMethods = new ArrayList<Method>();
        for (Method m : bladewerxDetectionMessage.class.getMethods()) {
            detectionMethods.add(m);
        }

        Collections.sort(detectionMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : detectionMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    detectionModel.addColumn(m.getName().substring(2));
                } else {
                    detectionModel.addColumn(m.getName().substring(3));
                }
            }
        }
        
        //Set-up status message table
        DefaultTableModel statusModel = new DefaultTableModel();
        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable2.setModel(
                statusModel);
        statusMethods = new ArrayList<Method>();
        for (Method m : bladewerxStatusMessage.class.getMethods()) {
            statusMethods.add(m);
        }

        Collections.sort(statusMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : statusMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    statusModel.addColumn(m.getName().substring(2));
                } else {
                    statusModel.addColumn(m.getName().substring(3));
                }
            }
        }
        
        //Set-up pump message table
        DefaultTableModel pumpModel = new DefaultTableModel();
        jTable3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable3.setModel(
                pumpModel);
        pumpMethods = new ArrayList<Method>();
        for (Method m : bladewerxPumpStatusMessage.class.getMethods()) {
            pumpMethods.add(m);
        }

        Collections.sort(pumpMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : pumpMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    pumpModel.addColumn(m.getName().substring(2));
                } else {
                    pumpModel.addColumn(m.getName().substring(3));
                }
            }
        }


        m_Pods = pods;
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_STATUS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_DETECTION_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodMsg.BLADEWERX_PUMP_STATUS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_COMPOSITE_HISTOGRAM, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_DLL_DETECTION_REPORT, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_AETNA_DETECTION_REPORT, this);
    }
    
    public boolean isGetter(Method m) {
        if (!m.getName().startsWith("get") && !m.getName().startsWith("is")) {
            return false;
        }
        if (m.getParameterTypes().length != 0) {
            return false;
        }
        if (void.class.equals(m.getReturnType())) {
            return false;
        }
        return true;

    }
    
    
    @Override
    public void handleMessage(cbrnPodMsg m) 
    {
        if (graphHist == null)
        {
            if (jPanel1.getWidth() > 0)
            {
                graphHist = new RNHistogramDisplayGraphPanel ();
                graphHist.setTitle("Alpha Count Histogram");
                graphHist.setSize(jPanel1.getWidth(), jPanel1.getHeight());
                jPanel1.add (graphHist);
                graphHist.setVisible(true);
            }
        }
        if (graphCounts == null)
        {
            if (jPanel1.getWidth() > 0)
            {
                graphCounts = new RNTotalCountsDisplayGraphPanel(300000);
                graphCounts.setTitle("Alpha Count Histogram");
                graphCounts.setSize(jPanel1.getWidth(), jPanel1.getHeight());
                jPanel1.add (graphCounts);
                graphCounts.setVisible(false);
            }
        }
        
        if(m instanceof bladewerxDetectionMessage)
        {
            insertMessageArea("Bladewerx Detection message received!");
            handleDetectionMessage((bladewerxDetectionMessage) m);
        }
        else if(m instanceof bladewerxStatusMessage)
        {
            insertMessageArea("Bladewerx Status message received!");
            handleStatusMessage((bladewerxStatusMessage) m);
        }
        else if(m instanceof bladewerxPumpStatusMessage)
        {
            insertMessageArea("Bladewerx Pump Status message received!");
            handlePumpMessage((bladewerxPumpStatusMessage) m);
        }
        else if(m instanceof bladewerxCompositeHistogramMessage)
        {
            insertMessageArea("Bladewerx Composite Histogram message received!");
            handleCompositeHistogramMessage((bladewerxCompositeHistogramMessage) m);
        }
        else if(m instanceof bladewerxDllDetectionReportMessage)
        {
            insertMessageArea("Bladewerx Detection Report message received!");
            handleDllDetectionReportMessage((bladewerxDllDetectionReportMessage) m);
        }
        else if(m instanceof bladewerxAETNADetectionReportMessage)
        {
            insertMessageArea("Bladewerx Detection Report message received!");
            handleAETNADetectionReportMessage((bladewerxAETNADetectionReportMessage) m);
        }
    }
    
    public void handleDetectionMessage (bladewerxDetectionMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : detectionMethods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTableModel model = ((DefaultTableModel) jTable1.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(29);
            }
        });
        
        
    }
    
    public void handleStatusMessage (bladewerxStatusMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : statusMethods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTableModel model = ((DefaultTableModel) jTable2.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(30);
            }
        });
        
        
    }
    
    public void handlePumpMessage (bladewerxPumpStatusMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : pumpMethods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTableModel model = ((DefaultTableModel) jTable3.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(31);
            }
        });
        
        
    }
    
    public void insertMessageArea(final String message)
    {
        try
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    DefaultListModel model = ((DefaultListModel) msgOutput.getModel());
                    model.insertElementAt(message, 0);
                    if (model.size() > 52)
                        model.setSize(52);
                }
            });
            
        }
        catch (Exception e)
        {
            //eat it
        }
    }

    private void handleCompositeHistogramMessage(bladewerxCompositeHistogramMessage msg) 
    {
        if (graphHist == null)
            return;
        graphHist.updateCurrentHistogram(new RNHistogram(msg.copySummedSpectra(), msg.copyClassifiedSpectra()));
        
        graphHist.setLiveTime((int)(msg.getLiveTime()/1000));
        graphHist.setStatMessage("Total Counts = " + msg.getTotalCounts());

        String countsAlertMessage = totalCountsTracker.getCountsAlertMessage(msg.getTotalCounts());
        graphHist.setStatAlertMessage(countsAlertMessage);


        if (graphCounts == null)
            return;

        double altitudeM = 0;
        if (m_BelMgr != null)
        {
            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)m_BelMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            if (picBlf != null)
                altitudeM = picBlf.getPiccoloTelemetry().AltWGS84;
        }
        graphCounts.updateCountsAltitudeTime((int) msg.getTotalCounts(), altitudeM, msg.getTimestampMs());
        graphCounts.setStatMessage("Total Counts = " + msg.getTotalCounts());
        graphCounts.setStatAlertMessage(countsAlertMessage);
    }
    
    private void handleAETNADetectionReportMessage(bladewerxAETNADetectionReportMessage message) 
    {
        if (graphHist == null)
            return;
        
        DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";
        Vector<IsotopeConcentraion> isotopes = message.accessIsotopes();
        
        if(isotopes.size() > 0)
        {
	        for (int i = 0; i < isotopes.size(); i ++)
	        {
	            detmsg += isotopes.get(i).isotope + ":" + df.format(isotopes.get(i).concentration) + "%";
	        }
        }
        else
        {
        	detmsg = "No Detection Results";
        }
        
        graphHist.setDetectionMessage(detmsg);

        if (graphCounts == null)
            return;
        
        graphCounts.setDetectionMessage(detmsg);
    }

    private void handleDllDetectionReportMessage(bladewerxDllDetectionReportMessage message) 
    {
        if (graphHist == null)
            return;
        
        DecimalFormat df = new DecimalFormat("#.###");
        String detmsg = "";
        int numResults = message.getNumIdResults();
        
        if(numResults > 0)
        {
            //detmsg = detmsg +  message.returnIdResult(0).m_IsotopeName +":" + df.format(message.returnIdResult(0).m_GoodnessOfFit);
            detmsg = detmsg +  message.returnIdResult(0).m_IsotopeName +":" + df.format(message.returnIdResult(0).m_Confidence);
            for(int i=1; i<numResults; i++)
            {
                //detmsg = detmsg + " - " + message.returnIdResult(i).m_IsotopeName +":" + df.format(message.returnIdResult(i).m_GoodnessOfFit);
                detmsg = detmsg + " - " + message.returnIdResult(i).m_IsotopeName +":" + df.format(message.returnIdResult(i).m_Confidence);
            }
        }
        else
        {
            detmsg = "No Detection Results";
        }
        
        graphHist.setDetectionMessage(detmsg);

        if (graphCounts == null)
            return;
        
        graphCounts.setDetectionMessage(detmsg);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgOutput = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jTextField4 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        jLabel1.setText("Message traffic:              (Newest at the top)");

        jScrollPane1.setViewportView(msgOutput);

        jLabel4.setText("Raw command: ");

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable2);

        jButton2.setText("Get Version");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Get Flow Rate");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Get Battery Level");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Set Scale");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Set Threshold");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("Set Gain");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Set Offset");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(jTable3);

        jButton9.setText("On");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Off");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel2.setText("Processing results:");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 214, Short.MAX_VALUE)
        );

        jButton11.setText("On");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("Off");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Open");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("Close");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel3.setText("Pump");

        jLabel5.setText("Servo");

        jLabel6.setText("MCA");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(170, 170, 170)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                                    .addComponent(jLabel2))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButton5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(74, 74, 74)
                                .addComponent(jButton6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addComponent(jButton7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(74, 74, 74)
                                .addComponent(jButton8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addGap(50, 50, 50)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton12)
                                .addGap(20, 20, 20)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton14)
                                .addGap(20, 20, 20)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton10)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jButton3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton5)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton6)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton7)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton8)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton11)
                        .addComponent(jButton12)
                        .addComponent(jButton13)
                        .addComponent(jButton14)
                        .addComponent(jButton9)
                        .addComponent(jButton10)
                        .addComponent(jLabel3)
                        .addComponent(jLabel5)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// 
    int rawBytesSize = 0;
    byte [] rawBytes = null;
    String rawData = jTextField3.getText();
    try
    {
        int length = rawData.length();
        rawBytes = new byte [length];
        
        int i, j;
        for (i = 0, j = 0; i < length - 2;)
        {
            if (rawData.charAt(i) == 'x')
            {
                //Assume a hex input
                int byte1 = Character.digit(rawData.charAt(i+1),10);
                int val1 = ((byte1&0xFF)) << 4;
                int byte2 = Character.digit(rawData.charAt(i+2),10);
                int val2 = ((byte2&0xFF));
                byte newByte = (byte)(val1 + val2);
                rawBytes[j++] = newByte;
                i += 3 ;
            }
            else 
            {
                //Assume actual byte was there, so use it
                rawBytes[j++] = (byte)rawData.charAt(i++);
            }
            rawBytesSize = j;
        }
        
        for (;i < length;)
        {
            //Add any trailing bytes
            rawBytes[j++] = (byte)rawData.charAt(i++);
            rawBytesSize = j;
        }
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing data for raw command");
        return;
    }
    
    cbrnPodCommand cmd = new bladewerxRawCommand(rawBytes);
    String outMsg = "Command sent: Raw command - " + rawBytesSize  + ": ";
    for (int i = 0; i < rawBytesSize; i ++)
        outMsg += rawBytes[i] + ",";
    insertMessageArea (outMsg);
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxVersionCommand();
    insertMessageArea ("Command sent: version");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxSetAdcCommand((char)0);
    insertMessageArea ("Command sent: Set ADC 0");
    m_Pods.sendCommand(cmd);
    
    cbrnPodCommand cmd2 = new bladewerxGetAdcCommand();
    insertMessageArea ("Command sent: Get ADC");
    m_Pods.sendCommand(cmd2);
    
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxSetAdcCommand((char)1);
    insertMessageArea ("Command sent: Set ADC 1");
    m_Pods.sendCommand(cmd);
    
    cbrnPodCommand cmd2 = new bladewerxGetAdcCommand();
    insertMessageArea ("Command sent: Get ADC");
    m_Pods.sendCommand(cmd2);
    
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
// TODO add your handling code here:
    try       
    {
        int val = Integer.parseInt(jTextField1.getText());
        if (val < 0)
            val = 0;
        if (val > 2)
            val = 2;
        
        cbrnPodCommand cmd0 = new bladewerxCalibrationCommand (true);
        insertMessageArea ("Command sent: Set calibration on");
        m_Pods.sendCommand(cmd0);
        
        cbrnPodCommand cmd = new bladewerxSetScaleCommand((char)val);
        insertMessageArea ("Command sent: Set scale - " + val);
        m_Pods.sendCommand(cmd);
        
        cbrnPodCommand cmd2 = new bladewerxCalibrationCommand (false);
        insertMessageArea ("Command sent: Set calibration off");
        m_Pods.sendCommand(cmd2);
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Command ignored");
    }
    
}//GEN-LAST:event_jButton5ActionPerformed

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
// TODO add your handling code here:
    try       
    {
        int val = Integer.parseInt(jTextField2.getText());
        if (val < 0)
            val = 0;
        if (val > 31)
            val = 31;
        cbrnPodCommand cmd0 = new bladewerxCalibrationCommand (true);
        insertMessageArea ("Command sent: Set calibration on");
        m_Pods.sendCommand(cmd0);
        
        cbrnPodCommand cmd = new bladewerxSetThresholdCommand((char)val);
        insertMessageArea ("Command sent: Set threshold - " + val);
        m_Pods.sendCommand(cmd);
        
        cbrnPodCommand cmd2 = new bladewerxCalibrationCommand (false);
        insertMessageArea ("Command sent: Set calibration off");
        m_Pods.sendCommand(cmd2);
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Command ignored");
    }
}//GEN-LAST:event_jButton6ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
// TODO add your handling code here:
    try       
    {
        int val = Integer.parseInt(jTextField4.getText());
        if (val < 0)
            val = 0;
        if (val > 31)
            val = 31;
        
        cbrnPodCommand cmd0 = new bladewerxCalibrationCommand (true);
        insertMessageArea ("Command sent: Set calibration on");
        m_Pods.sendCommand(cmd0);
        
        cbrnPodCommand cmd = new bladewerxSetGainCommand((char)val);
        insertMessageArea ("Command sent: Set gain - " + val);
        m_Pods.sendCommand(cmd);
        
        cbrnPodCommand cmd2 = new bladewerxCalibrationCommand (false);
        insertMessageArea ("Command sent: Set calibration off");
        m_Pods.sendCommand(cmd2);
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Command ignored");
    }
}//GEN-LAST:event_jButton7ActionPerformed

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
// TODO add your handling code here:
    try       
    {
        int val = Integer.parseInt(jTextField5.getText());
        if (val < 0)
            val = 0;
        if (val > 255)
            val = 255;
        
        cbrnPodCommand cmd0 = new bladewerxCalibrationCommand (true);
        insertMessageArea ("Command sent: Set calibration on");
        m_Pods.sendCommand(cmd0);
        
        cbrnPodCommand cmd = new bladewerxSetOffsetCommand((char)val);
        insertMessageArea ("Command sent: Set offset - " + val);
        m_Pods.sendCommand(cmd);
        
        cbrnPodCommand cmd2 = new bladewerxCalibrationCommand (false);
        insertMessageArea ("Command sent: Set calibration off");
        m_Pods.sendCommand(cmd2);
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Command ignored");
    }
}//GEN-LAST:event_jButton8ActionPerformed

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPumpPowerCommand (true);
    insertMessageArea ("Command sent: Pump on");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton9ActionPerformed

private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPumpPowerCommand (false);
    insertMessageArea ("Command sent: Pump off");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton10ActionPerformed

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPowerOnCommand();
    insertMessageArea ("Command sent: MCA On");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton11ActionPerformed

private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPowerOffCommand();
    insertMessageArea ("Command sent: MCA Off");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton12ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        cbrnPodCommand cmd = new servoSetClosedCommand((char)1);
        insertMessageArea ("Command sent: Servo Closed");
        m_Pods.sendCommandToBoard(cmd, 1);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        cbrnPodCommand cmd = new servoSetOpenCommand((char)1);
        insertMessageArea ("Command sent: Servo Open");
        m_Pods.sendCommandToBoard(cmd, 1);
    }//GEN-LAST:event_jButton13ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JList msgOutput;
    // End of variables declaration//GEN-END:variables

}
