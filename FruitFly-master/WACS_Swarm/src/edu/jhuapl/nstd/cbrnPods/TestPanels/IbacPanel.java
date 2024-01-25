/*
 * IbacPanel.java
 *
 * Created on January 20, 2010, 1:27 PM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacAirSampleCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacAlarmCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacAutoCollectCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacClearAlarmCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacCollectCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacDiagRateCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacDiagnosticsMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacParticleCountMessage;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacRawCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacSleepCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacStatusCommand;
import edu.jhuapl.nstd.cbrnPods.messages.IbacMessages.ibacTraceRateCommand;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
public class IbacPanel extends javax.swing.JPanel implements cbrnPodMessageListener {

    cbrnPodsInterface m_Pods;
    
    ArrayList<Method> diagnosticMethods;
    ArrayList<Method> particleCountMethods ;



    static final boolean logIbacData = true;
    DataOutputStream particleStream = null;
    DataOutputStream diagnosticsStream = null;
    
    
    /** Creates new form IbacPanel */
    public IbacPanel(cbrnPodsInterface pods) {
        initComponents();
        
        jtlMessages.setModel(new DefaultListModel());
        
        //Set-up diagnostics message table
        DefaultTableModel diagnosticsModel = new DefaultTableModel();
        jtbDiagnostics.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jtbDiagnostics.setModel(
                diagnosticsModel);
        diagnosticMethods = new ArrayList<Method>();
        for (Method m : ibacDiagnosticsMessage.class.getMethods()) {
            diagnosticMethods.add(m);
        }

        Collections.sort(diagnosticMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : diagnosticMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    diagnosticsModel.addColumn(m.getName().substring(2));
                } else {
                    diagnosticsModel.addColumn(m.getName().substring(3));
                }
            }
        }
        
        //Set-up status message table
        DefaultTableModel particleModel = new DefaultTableModel();
        jtbParticleDetection.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jtbParticleDetection.setModel(
                particleModel);
        particleCountMethods = new ArrayList<Method>();
        for (Method m : ibacParticleCountMessage.class.getMethods()) {
            particleCountMethods.add(m);
        }

        Collections.sort(particleCountMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : particleCountMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    particleModel.addColumn(m.getName().substring(2));
                } else {
                    particleModel.addColumn(m.getName().substring(3));
                }
            }
        }


        if (logIbacData)
        {
            try
            {
                File particleFile = new File ("./IbacLogs/particleCounts_" + System.currentTimeMillis() + ".csv");
                File diagnosticsFile = new File ("./IbacLogs/diagnostics_" + System.currentTimeMillis() + ".csv");
                if (!particleFile.getParentFile().exists())
                    particleFile.getParentFile().mkdirs();
                if (!particleFile.exists())
                    particleFile.createNewFile();
                if (!diagnosticsFile.exists())
                    diagnosticsFile.createNewFile();

                particleStream = new DataOutputStream(new BufferedOutputStream (new FileOutputStream (particleFile)));
                diagnosticsStream = new DataOutputStream(new BufferedOutputStream (new FileOutputStream (diagnosticsFile)));


                particleStream.writeBytes ("Timestamp,");
                particleStream.writeBytes ("Date/Time,");
                particleStream.writeBytes ("CSI,");
                particleStream.writeBytes ("CLI,");
                particleStream.writeBytes ("BCSI,");
                particleStream.writeBytes ("BCLI,");
                particleStream.writeBytes ("CSA,");
                particleStream.writeBytes ("CLA,");
                particleStream.writeBytes ("BCSA,");
                particleStream.writeBytes ("BCLA,");
                particleStream.writeBytes ("BpSA,");
                particleStream.writeBytes ("BpLA,");
                particleStream.writeBytes ("SFI,");
                particleStream.writeBytes ("SFA,");
                particleStream.writeBytes ("AlarmCounter,");
                particleStream.writeBytes ("ValidBaseline,");
                particleStream.writeBytes ("AlarmStatus,");
                particleStream.writeBytes ("AlarmLatchState,");
                particleStream.writeBytes ("BaselineDataTime,");
                particleStream.writeBytes ("BCLABaseline,");
                particleStream.writeBytes ("BpLABaseline,");
                particleStream.writeBytes ("SizeFractionBaseline,");
                particleStream.writeBytes ("\r\n");
                particleStream.flush();


                diagnosticsStream.writeBytes ("Timestamp,");
                diagnosticsStream.writeBytes ("Date/Time,");
                diagnosticsStream.writeBytes ("LastPressureFault,");
                diagnosticsStream.writeBytes ("PressureAtFault,");
                diagnosticsStream.writeBytes ("LaserPowerLowFault,");
                diagnosticsStream.writeBytes ("LaserPowerAboveFault,");
                diagnosticsStream.writeBytes ("LaserCurrentOutFault,");
                diagnosticsStream.writeBytes ("LaserInitialCurrent,");
                diagnosticsStream.writeBytes ("LaserCurrentCurrent,");
                diagnosticsStream.writeBytes ("BackgroundLghtBelowFault,");
                diagnosticsStream.writeBytes ("LastCollectingSample,");
                diagnosticsStream.writeBytes ("LastUnitAlarm,");
                diagnosticsStream.writeBytes ("IsSystemReady,");
                diagnosticsStream.writeBytes ("CollectionDiskSpinning,");
                diagnosticsStream.writeBytes ("Sleeping,");
                diagnosticsStream.writeBytes ("StatusAndVersion,");
                diagnosticsStream.writeBytes ("DiagnosticsTimeStamp,");
                diagnosticsStream.writeBytes ("OutletPressure,");
                diagnosticsStream.writeBytes ("PressureAlarm,");
                diagnosticsStream.writeBytes ("Temperature,");
                diagnosticsStream.writeBytes ("TemperatureAlarm,");
                diagnosticsStream.writeBytes ("LaserPowerMonitor,");
                diagnosticsStream.writeBytes ("LaserPowerAlarm,");
                diagnosticsStream.writeBytes ("LaserCurrentMonitor,");
                diagnosticsStream.writeBytes ("LaserCurrentMonitorAlarm,");
                diagnosticsStream.writeBytes ("BackgroundMonitor,");
                diagnosticsStream.writeBytes ("BackgroundAlarm,");
                diagnosticsStream.writeBytes ("InputVoltage,");
                diagnosticsStream.writeBytes ("InputVoltageAlarm,");
                diagnosticsStream.writeBytes ("InputCurrent,");
                diagnosticsStream.writeBytes ("InputCurrentAlarm,");
                diagnosticsStream.writeBytes ("\r\n");

                diagnosticsStream.flush();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        m_Pods = pods;
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_DIAGNOSTICS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_IBAC, cbrnPodMsg.IBAC_PARTICLE_COUNT_TYPE, this);

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
        if(m instanceof ibacDiagnosticsMessage){
            insertMessageArea("IBAC diagnostics message received!");
            handleDiagnosticsMessage((ibacDiagnosticsMessage)m);
        }
        if(m instanceof ibacParticleCountMessage){
            insertMessageArea("IBAC particle count message received!");
            handleParticleCountMessage((ibacParticleCountMessage)m);
        }
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
                    DefaultListModel model = ((DefaultListModel) jtlMessages.getModel());
                    model.insertElementAt(message, 0);
                    if (model.size() > 55)
                        model.setSize(55);
                }
            });
        
            
        }
        catch (Exception e)
        {
            //eat it
        }
    }
    
    public void handleDiagnosticsMessage (ibacDiagnosticsMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : diagnosticMethods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTableModel model = ((DefaultTableModel) jtbDiagnostics.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(35);
            }
        });





        if (logIbacData)
        {
            try
            {
                diagnosticsStream.writeBytes ("" + msg.getTimestampMs() + ",");
                diagnosticsStream.writeBytes ("" + Calendar.getInstance().get(Calendar.MONTH) + "/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" + Calendar.getInstance().get(Calendar.YEAR) + " " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND) + ",");
                diagnosticsStream.writeBytes ("" + msg.getLastPressureFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getPressureAtFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserPowerLowFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserPowerAboveFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserCurrentOutFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserInitialCurrent() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserCurrentCurrent() + ",");
                diagnosticsStream.writeBytes ("" + msg.getBackgroundLghtBelowFault() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLastCollectingSample() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLastUnitAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.isIsSystemReady() + ",");
                diagnosticsStream.writeBytes ("" + msg.isCollectionDiskSpinning() + ",");
                diagnosticsStream.writeBytes ("" + msg.isSleeping() + ",");
                diagnosticsStream.writeBytes ("" + msg.getDiagnosticsTimeStamp() + ",");
                diagnosticsStream.writeBytes ("" + msg.getOutletPressure() + ",");
                diagnosticsStream.writeBytes ("" + msg.isPressureAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getTemperature() + ",");
                diagnosticsStream.writeBytes ("" + msg.isTemperatureAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserPowerMonitor() + ",");
                diagnosticsStream.writeBytes ("" + msg.isLaserPowerAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getLaserCurrentMonitor() + ",");
                diagnosticsStream.writeBytes ("" + msg.isLaserCurrentMonitorAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getBackgroundMonitor() + ",");
                diagnosticsStream.writeBytes ("" + msg.isBackgroundAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getInputVoltage() + ",");
                diagnosticsStream.writeBytes ("" + msg.isInputVoltageAlarm() + ",");
                diagnosticsStream.writeBytes ("" + msg.getInputCurrent() + ",");
                diagnosticsStream.writeBytes ("" + msg.isInputCurrentAlarm() + ",");
                diagnosticsStream.writeBytes ("\r\n");

                diagnosticsStream.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        
    }
    
    public void handleParticleCountMessage (ibacParticleCountMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : particleCountMethods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(IbacPanel.class.getName()).log(Level.SEVERE, null, ex);
                    data.add(null);
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultTableModel model = ((DefaultTableModel) jtbParticleDetection.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(36);
            }
        });
        


        if (logIbacData)
        {
            try
            {
                particleStream.writeBytes ("" + msg.getTimestampMs() + ",");
                particleStream.writeBytes ("" + Calendar.getInstance().get(Calendar.MONTH) + "/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" + Calendar.getInstance().get(Calendar.YEAR) + " " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND) + ",");
                particleStream.writeBytes ("" + msg.getCSI() + ",");
                particleStream.writeBytes ("" + msg.getCLI() + ",");
                particleStream.writeBytes ("" + msg.getBCSI() + ",");
                particleStream.writeBytes ("" + msg.getBCLI() + ",");
                particleStream.writeBytes ("" + msg.getCSA() + ",");
                particleStream.writeBytes ("" + msg.getCLA() + ",");
                particleStream.writeBytes ("" + msg.getBCSA() + ",");
                particleStream.writeBytes ("" + msg.getBCLA() + ",");
                particleStream.writeBytes ("" + msg.getBpSA() + ",");
                particleStream.writeBytes ("" + msg.getBpLA() + ",");
                particleStream.writeBytes ("" + msg.getSFI() + ",");
                particleStream.writeBytes ("" + msg.getSFA() + ",");
                particleStream.writeBytes ("" + msg.getAlarmCounter() + ",");
                particleStream.writeBytes ("" + msg.isValidBaseline() + ",");
                particleStream.writeBytes ("" + msg.isAlarmStatus() + ",");
                particleStream.writeBytes ("" + msg.isAlarmLatchState() + ",");
                particleStream.writeBytes ("" + msg.getBaselineDataTime() + ",");
                particleStream.writeBytes ("" + msg.getBCLABaseline() + ",");
                particleStream.writeBytes ("" + msg.getBpLABaseline() + ",");
                particleStream.writeBytes ("" + msg.getSizeFractionBaseline() + ",");
                particleStream.writeBytes ("\r\n");
                particleStream.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jtbParticleDetection = new javax.swing.JTable();
        jbClearAlarm = new javax.swing.JButton();
        jbSleep = new javax.swing.JButton();
        jbAirSample = new javax.swing.JButton();
        jbStatus = new javax.swing.JButton();
        jcbAutoCollect = new javax.swing.JCheckBox();
        jtfTraceRate = new javax.swing.JTextField();
        jtfDiagRate = new javax.swing.JTextField();
        jtfAutoCollect = new javax.swing.JTextField();
        jbTraceRate = new javax.swing.JButton();
        jbDiagRate = new javax.swing.JButton();
        jbAutoCollect = new javax.swing.JButton();
        jcbAlarm = new javax.swing.JCheckBox();
        jbAlarm = new javax.swing.JButton();
        jcbCollect = new javax.swing.JCheckBox();
        jbCollect = new javax.swing.JButton();
        jtfRaw = new javax.swing.JTextField();
        jbRaw = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtlMessages = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtbDiagnostics = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jtbParticleDetection.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jtbParticleDetection);

        jbClearAlarm.setText("Clear Alarm");
        jbClearAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbClearAlarmActionPerformed(evt);
            }
        });

        jbSleep.setText("Sleep");
        jbSleep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSleepActionPerformed(evt);
            }
        });

        jbAirSample.setText("Air Sample");
        jbAirSample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAirSampleActionPerformed(evt);
            }
        });

        jbStatus.setText("Status");
        jbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbStatusActionPerformed(evt);
            }
        });

        jbTraceRate.setText("Set Trace Rate");
        jbTraceRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbTraceRateActionPerformed(evt);
            }
        });

        jbDiagRate.setText("Set Diag Rate");
        jbDiagRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDiagRateActionPerformed(evt);
            }
        });

        jbAutoCollect.setText("Auto Collect");
        jbAutoCollect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAutoCollectActionPerformed(evt);
            }
        });

        jbAlarm.setText("Alarm");
        jbAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAlarmActionPerformed(evt);
            }
        });

        jbCollect.setText("Collect");
        jbCollect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCollectActionPerformed(evt);
            }
        });

        jbRaw.setText("Send Raw");
        jbRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRawActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(jtlMessages);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jtbDiagnostics.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jtbDiagnostics);

        jLabel1.setText("Message traffic:              (Newest at the top)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jcbCollect)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jbCollect, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jcbAlarm)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jbAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                                    .addComponent(jbClearAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                    .addComponent(jbStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                    .addComponent(jbSleep, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                    .addComponent(jbAirSample, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                                .addGap(52, 52, 52)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jbRaw, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jbAutoCollect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jbDiagRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jbTraceRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jtfAutoCollect, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jcbAutoCollect))
                                            .addComponent(jtfDiagRate, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                            .addComponent(jtfTraceRate, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jtfRaw, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE))))
                .addContainerGap(22, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jbClearAlarm)
                            .addComponent(jbTraceRate)
                            .addComponent(jtfTraceRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jbDiagRate)
                            .addComponent(jtfDiagRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jbSleep)
                                .addComponent(jbAutoCollect)
                                .addComponent(jtfAutoCollect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jcbAutoCollect))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jbAirSample)
                            .addComponent(jtfRaw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jbRaw))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jcbAlarm))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbAlarm)))
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jbCollect)
                            .addComponent(jcbCollect)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jbClearAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbClearAlarmActionPerformed
    
    m_Pods.sendCommand(new ibacClearAlarmCommand());
    insertMessageArea ("Command sent: clear alarm");
}//GEN-LAST:event_jbClearAlarmActionPerformed

private void jbSleepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSleepActionPerformed
    
    m_Pods.sendCommand(new ibacSleepCommand());
    insertMessageArea ("Command sent: sleep");
}//GEN-LAST:event_jbSleepActionPerformed

private void jbAirSampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAirSampleActionPerformed
    
    m_Pods.sendCommand(new ibacAirSampleCommand());
    insertMessageArea ("Command sent: air sample");
}//GEN-LAST:event_jbAirSampleActionPerformed

private void jbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbStatusActionPerformed
    
    m_Pods.sendCommand(new ibacStatusCommand());
    insertMessageArea ("Command sent: status");
}//GEN-LAST:event_jbStatusActionPerformed

private void jbTraceRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbTraceRateActionPerformed

    int rate;

    try {
        rate = Integer.parseInt(jtfTraceRate.getText());
    } catch (Exception e) {
        insertMessageArea("Error parsing trace rate");
        return;
    }

    ibacTraceRateCommand cmd = new ibacTraceRateCommand(rate);
    m_Pods.sendCommand(cmd);
    insertMessageArea ("Command sent: trace rate," + rate);
}//GEN-LAST:event_jbTraceRateActionPerformed

private void jbDiagRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDiagRateActionPerformed

    int rate;

    try {
        rate = Integer.parseInt(jtfDiagRate.getText());
    } catch (Exception e) {
        insertMessageArea("Error parsing diag rate");
        return;
    }

    ibacDiagRateCommand cmd = new ibacDiagRateCommand(rate);
    m_Pods.sendCommand(cmd);
    insertMessageArea ("Command sent: diag rate," + rate);
}//GEN-LAST:event_jbDiagRateActionPerformed

private void jbAutoCollectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAutoCollectActionPerformed

    int rate;

    try {
        rate = Integer.parseInt(jtfAutoCollect.getText());
    } catch (Exception e) {
        insertMessageArea("Error parsing autocollect runtime interval");
        return;
    }

    ibacAutoCollectCommand cmd = new ibacAutoCollectCommand (jcbAutoCollect.isSelected(), rate);
    m_Pods.sendCommand(cmd);
    insertMessageArea ("Command sent: auto collect," + rate);
}//GEN-LAST:event_jbAutoCollectActionPerformed

private void jbAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAlarmActionPerformed
    
    ibacAlarmCommand cmd = new ibacAlarmCommand(jcbAlarm.isSelected());
    m_Pods.sendCommand(cmd);
    insertMessageArea ("Command sent: alarm");
}//GEN-LAST:event_jbAlarmActionPerformed

private void jbCollectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCollectActionPerformed

    ibacCollectCommand cmd = new ibacCollectCommand(jcbCollect.isSelected());
    m_Pods.sendCommand(cmd);
    insertMessageArea ("Command sent: collect");
}//GEN-LAST:event_jbCollectActionPerformed

private void jbRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRawActionPerformed
// 
    String text = jtfRaw.getText();
    cbrnPodCommand cmd = new ibacRawCommand(text + "\r");
    insertMessageArea ("Command sent: raw," + text);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jbRawActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton jbAirSample;
    private javax.swing.JButton jbAlarm;
    private javax.swing.JButton jbAutoCollect;
    private javax.swing.JButton jbClearAlarm;
    private javax.swing.JButton jbCollect;
    private javax.swing.JButton jbDiagRate;
    private javax.swing.JButton jbRaw;
    private javax.swing.JButton jbSleep;
    private javax.swing.JButton jbStatus;
    private javax.swing.JButton jbTraceRate;
    private javax.swing.JCheckBox jcbAlarm;
    private javax.swing.JCheckBox jcbAutoCollect;
    private javax.swing.JCheckBox jcbCollect;
    private javax.swing.JTable jtbDiagnostics;
    private javax.swing.JTable jtbParticleDetection;
    private javax.swing.JTextField jtfAutoCollect;
    private javax.swing.JTextField jtfDiagRate;
    private javax.swing.JTextField jtfRaw;
    private javax.swing.JTextField jtfTraceRate;
    private javax.swing.JList jtlMessages;
    // End of variables declaration//GEN-END:variables

}
