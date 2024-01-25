/*
 * AnacondaPanel.java
 *
 * Created on April 14, 2010, 8:23 AM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaActionDeleteCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaActionSaveSettingsCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaDebugOptCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDAReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDBReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeAirframeCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeIdleCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModePodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeSearchCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaModeStandbyCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaRawCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaResetSampleUsage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSendLcdaG;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSendLcdaH;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSendLcdbG;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSendLcdbH;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetDateTimeCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetGpsCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetManifoldTempCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetPitotTempCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetServoClosedLimitCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSetServoOpenLimitCommand;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStopLcdaG;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStopLcdaH;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStopLcdbG;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaStopLcdbH;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaTextMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  humphjc1
 */
public class AnacondaPanel extends javax.swing.JPanel implements cbrnPodMessageListener 
{
    cbrnPodsInterface m_Pods;
    
    ArrayList<Method> statusMethods ;
    ArrayList<Method> reportMethods ;
    ArrayList<Method> textMethods ;
    
    private JFrame m_AnacondaSpectraDebugWindow;
    
    
    /** Creates new form AnacondaPanel */
    public AnacondaPanel(cbrnPodsInterface pods) 
    {
        initComponents();
        
        msgOutput.setModel(new DefaultListModel());
        
        //Set-up status message table
        DefaultTableModel statusModel = new DefaultTableModel();
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable1.setModel(
                statusModel);
        statusMethods = new ArrayList<Method>();
        for (Method m : anacondaStatusMessage.class.getMethods()) {
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
        
        
        //Set-up LCDA message table
        DefaultTableModel reportAModel = new DefaultTableModel();
        DefaultTableModel reportBModel = new DefaultTableModel();
        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
        jTable2.setModel(
                reportAModel);
        jTable3.setModel(
                reportBModel);
        
        reportMethods = new ArrayList<Method>();
        for (Method m : anacondaLCDReportMessage.class.getMethods()) {
            reportMethods.add(m);
        }

        Collections.sort(reportMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : reportMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    reportAModel.addColumn(m.getName().substring(2));
                    reportBModel.addColumn(m.getName().substring(2));
                } else {
                    reportAModel.addColumn(m.getName().substring(3));
                    reportBModel.addColumn(m.getName().substring(3));
                }
            }
        } 
        
        
        //Set-up text message table
        DefaultTableModel textModel = new DefaultTableModel();
        jTable4.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable4.setModel(
                textModel);
        textMethods = new ArrayList<Method>();
        for (Method m : anacondaTextMessage.class.getMethods()) {
            textMethods.add(m);
        }

        Collections.sort(textMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : textMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    textModel.addColumn(m.getName().substring(2));
                } else {
                    textModel.addColumn(m.getName().substring(3));
                }
            }
        }


        m_Pods = pods;
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_STATUS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_REPORT, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_REPORT, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_TEXT, this);

        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_G_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_H_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_G_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_H_SPECTRA, this);

        m_AnacondaSpectraDebugWindow = new JFrame("Chemical Sensor Spectra Window");
        m_AnacondaSpectraDebugWindow.add(new AnacondaSpectraPanel(m_Pods));
        m_AnacondaSpectraDebugWindow.pack();
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
        if(m instanceof anacondaStatusMessage){
            insertMessageArea("Anaconda Status message received!");
            handleStatusMessage((anacondaStatusMessage)m);
        }
        else if(m instanceof anacondaLCDAReportMessage || (m instanceof anacondaLCDReportMessage && m.getMessageType() == cbrnPodMsg.ANACONDA_LCDA_REPORT)) {
            insertMessageArea("Anaconda LCDA Report message received!");
            handleLCDAMessage((anacondaLCDAReportMessage)m);
        }
        else if(m instanceof anacondaLCDBReportMessage || (m instanceof anacondaLCDReportMessage && m.getMessageType() == cbrnPodMsg.ANACONDA_LCDB_REPORT)){
            insertMessageArea("Anaconda LCDB Report message received!");
            handleLCDBMessage((anacondaLCDBReportMessage)m);
        }
        else if(m instanceof anacondaTextMessage){
            insertMessageArea("Anaconda Text message received!");
            handleTextMessage((anacondaTextMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDAGMessage){
            insertMessageArea("Anaconda LCDA G spectra received!");
            handleSpectraMessage((anacondaSpectraLCDAGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBGMessage){
            insertMessageArea("Anaconda LCDB G spectra received!");
            handleSpectraMessage((anacondaSpectraLCDBGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDAHMessage){
            insertMessageArea("Anaconda LCDA H spectra received!");
            handleSpectraMessage((anacondaSpectraLCDAHMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBHMessage){
            insertMessageArea("Anaconda LCDB H spectra received!");
            handleSpectraMessage((anacondaSpectraLCDBHMessage)m);
        }
        
    }
    
    public void handleStatusMessage (anacondaStatusMessage msg)
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
                DefaultTableModel model = ((DefaultTableModel) jTable1.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(25);
            }
        });
        
        
    }
    
    public void handleLCDAMessage (anacondaLCDAReportMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : reportMethods) {
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
                model.setRowCount(26);            }
        });
        
    }
    
    public void handleLCDBMessage (anacondaLCDBReportMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : reportMethods) {
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
                model.setRowCount(27);
            }
        });
        
        
    }
    
    public void handleTextMessage (anacondaTextMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : textMethods) {
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
                DefaultTableModel model = ((DefaultTableModel) jTable4.getModel());
                model.insertRow(0, data.toArray());
                model.setRowCount(28);
            }
        });
        
    }
    
    void handleSpectraMessage(anacondaSpectraLCDAGMessage msg)
    {
        
    }
            
    void handleSpectraMessage(anacondaSpectraLCDBGMessage msg)
    {
        
    }
            
    void handleSpectraMessage(anacondaSpectraLCDAHMessage msg)
    {
        
    }

    void handleSpectraMessage(anacondaSpectraLCDBHMessage msg)
    {
        
    }
    
    public void insertMessageArea(final String message)
    {
        try
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run() {
                    DefaultListModel model = ((DefaultListModel) msgOutput.getModel());
                    model.insertElementAt(message, 0);
                    if (model.size() > 50)
                            model.setSize(50);
                }
            });
        }
        catch (Exception e)
        {
            //eat it
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgOutput = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jTextField6 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jTextField13 = new javax.swing.JTextField();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jSeparator3 = new javax.swing.JSeparator();
        jButton42 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton43 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();

        jLabel1.setText("Message traffic:              (Newest at the top)");

        jScrollPane1.setViewportView(msgOutput);

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

        jButton1.setText("Set GPS");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Idle");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Search");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Standby");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Airframe");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Pod");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel2.setText("Timestamp:");

        jLabel3.setText("Latitude:");

        jLabel4.setText("Longitude:");

        jLabel5.setText("Altitude:");

        jButton7.setText("Delete");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Set Date/Time");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("Set Servo Open Limit");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Set Servo Closed Limit");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("Set Manifold Heat Temp");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("Set Pitot Heat Temp");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Raw Command");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("Debug On");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel6.setText("Type:");

        jLabel7.setText("Data Fields:");

        jButton15.setText("Debug Off");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton16.setText("Servo Open");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jButton17.setText("Servo Close");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setText("M Heat On");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jButton19.setText("M Heat Off");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jButton20.setText("P Heat On");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jButton21.setText("Pumps On");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jButton22.setText("Pumps Off");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        jButton23.setText("P Heat Off");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jButton24.setText("V1 Open");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        jButton25.setText("V2 Open");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        jButton26.setText("V3 Open");
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });

        jButton27.setText("V4 Open");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });

        jButton28.setText("V2 Close");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });

        jButton29.setText("V3 Close");
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });

        jButton30.setText("V1 Close");
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });

        jButton31.setText("V4 Close");
        jButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton31ActionPerformed(evt);
            }
        });

        jButton32.setText("All V Close");
        jButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton32ActionPerformed(evt);
            }
        });

        jButton33.setText("Write Test");
        jButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton33ActionPerformed(evt);
            }
        });

        jButton36.setText("Write Sys");
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });

        jButton37.setText("Reset LCDA");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });

        jButton38.setText("Reset LCDB");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });

        jButton39.setText("Rel Reset LCDA");
        jButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton39ActionPerformed(evt);
            }
        });

        jButton40.setText("Rel Reset LCDB");
        jButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton40ActionPerformed(evt);
            }
        });

        jButton41.setText("Action Save Settings");
        jButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton41ActionPerformed(evt);
            }
        });

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

        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

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
        jScrollPane4.setViewportView(jTable3);

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jTable4);

        jButton42.setText("Send");
        jButton42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton42ActionPerformed(evt);
            }
        });

        jLabel8.setText("LCDA G Spectra");

        jButton43.setText("Stop");
        jButton43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton43ActionPerformed(evt);
            }
        });

        jLabel9.setText("LCDA H Spectra");

        jButton44.setText("Send");
        jButton44.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton44ActionPerformed(evt);
            }
        });

        jButton45.setText("Stop");
        jButton45.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton45ActionPerformed(evt);
            }
        });

        jLabel10.setText("LCDB G Spectra");

        jButton46.setText("Send");
        jButton46.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton46ActionPerformed(evt);
            }
        });

        jButton47.setText("Stop");
        jButton47.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton47ActionPerformed(evt);
            }
        });

        jLabel11.setText("LCDB H Spectra");

        jButton48.setText("Send");
        jButton48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton48ActionPerformed(evt);
            }
        });

        jButton49.setText("Stop");
        jButton49.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton49ActionPerformed(evt);
            }
        });

        jButton50.setText("Sync");
        jButton50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton50ActionPerformed(evt);
            }
        });

        jButton34.setText("Reset Usage");
        jButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton34ActionPerformed(evt);
            }
        });

        jButton35.setText("Spectra Window");
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, 0, 1, Short.MAX_VALUE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField6)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton50)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton8)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButton23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton29))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton36, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                    .addComponent(jButton32, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                    .addComponent(jButton21, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                    .addComponent(jButton22, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                    .addComponent(jButton33, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton40)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField13, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel5)
                                                .addComponent(jLabel3))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(29, 29, 29)
                                                    .addComponent(jLabel4))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jLabel2)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jSeparator1))
                                    .addComponent(jButton34)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jButton35, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jButton42)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jButton43)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(jLabel9))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jButton46)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jButton47)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jButton44)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jButton45))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jButton48)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jButton49))))))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton3)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton7)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton41)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton8)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton50))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton9)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton10)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton12))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton34)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jButton42)
                            .addComponent(jButton43)
                            .addComponent(jLabel9)
                            .addComponent(jButton44)
                            .addComponent(jButton45))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(jButton46)
                            .addComponent(jButton47)
                            .addComponent(jLabel11)
                            .addComponent(jButton48)
                            .addComponent(jButton49))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton13)
                            .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton37)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton39)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton40))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton24)
                                    .addComponent(jButton32))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton25)
                                    .addComponent(jButton21))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton26)
                                    .addComponent(jButton22))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton27)
                                    .addComponent(jButton33))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton30)
                                    .addComponent(jButton36))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton28))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton29)
                                    .addComponent(jButton20))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton23)
                                    .addComponent(jButton31))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
    
    long timestamp;
    double lat, lon;
    short alt;
    try
    {
        timestamp = Long.parseLong(jTextField2.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing timestamp for GPS command");
        return;
    }
    
    try
    {
        lat = Double.parseDouble(jTextField3.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing lat for GPS command");
        return;
    }
    
    try
    {
        lon = Double.parseDouble(jTextField4.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing lon for GPS command");
        return;
    }
    
    try
    {
        alt = Short.parseShort(jTextField5.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing alt for GPS command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetGpsCommand(timestamp, lat, lon, alt);
    insertMessageArea ("Command sent: GPS" + timestamp + "," + lat + "," + lon + "," + alt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_RELEASE_RESET_LCDB;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton40ActionPerformed

private void jButton39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton39ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_RELEASE_RESET_LCDA;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton39ActionPerformed

private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_RESET_LCDB;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton38ActionPerformed

private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_RESET_LCDA;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton37ActionPerformed

private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_WRITE_TESTFILE_SYS;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton36ActionPerformed

private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_WRITE_TESTFILES;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton33ActionPerformed

private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVESALL_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton32ActionPerformed

private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES4_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton31ActionPerformed

private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES3_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton29ActionPerformed

private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES2_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton28ActionPerformed

private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES1_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton30ActionPerformed

private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES4_OPEN;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton27ActionPerformed

private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES3_OPEN;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton26ActionPerformed

private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES2_OPEN;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton25ActionPerformed

private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_VALVES1_OPEN;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton24ActionPerformed

private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_PUMPS_OFF;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton22ActionPerformed

private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_PUMPS_ON;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton21ActionPerformed

private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_PITOT_HEATOFF;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton23ActionPerformed

private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_PITOT_HEATON;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton20ActionPerformed

private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_SERVO_CLOSED;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton17ActionPerformed

private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_SERVO_OPEN;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton16ActionPerformed

private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_FORBID_DEBUG;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton15ActionPerformed

private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_PERMIT_DEBUG;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton14ActionPerformed

private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
// TODO add your handling code here:
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_MANIFOLD_HEATOFF;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton19ActionPerformed

private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
// TODO add your handling code here:
    
    
    int opt = cbrnPodCommand.ANACONDA_DEBUG_MANIFOLD_HEATON;
    
    cbrnPodCommand cmd = new anacondaDebugOptCommand (opt);
    insertMessageArea ("Command sent: Debug option - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton18ActionPerformed

private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
// TODO add your handling code here:
    int type;
    try
    {
        type = Integer.parseInt(jTextField12.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing value for raw command message type");
        return;
    }
    
    int rawBytesSize = 0;
    byte [] rawBytes = null;
    String rawData = jTextField13.getText();
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
    
    cbrnPodCommand cmd = new anacondaRawCommand (type, rawBytesSize, rawBytes);
    
    String outMsg = "Command sent: Raw command - " + type + "," + rawBytesSize  + ": ";
    for (int i = 0; i < rawBytesSize; i ++)
        outMsg += rawBytes[i] + ",";
    insertMessageArea (outMsg);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton13ActionPerformed

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
// TODO add your handling code here:
    short val;
    try
    {
        val = Short.parseShort(jTextField10.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing value for set manifold heater temperature command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetManifoldTempCommand (val);
    insertMessageArea ("Command sent: Set manifold heater temperature - " + val);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton11ActionPerformed

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
// TODO add your handling code here:
    int time;
    try
    {
        time = Integer.parseInt(jTextField7.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing time for set date/time command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetDateTimeCommand (time);
    insertMessageArea ("Command sent: Set Date/Time - " + time);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton8ActionPerformed

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
// TODO add your handling code here:
    short val;
    try
    {
        val = Short.parseShort(jTextField8.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing value for set servo open limit command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetServoOpenLimitCommand (val);
    insertMessageArea ("Command sent: Set servo open limit - " + val);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton9ActionPerformed

private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
// TODO add your handling code here:
    short val;
    try
    {
        val = Short.parseShort(jTextField9.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing value for set servo closed limit command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetServoClosedLimitCommand (val);
    insertMessageArea ("Command sent: Set servo closed limit - " + val);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton10ActionPerformed

private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
// TODO add your handling code here:
    short val;
    try
    {
        val = Short.parseShort(jTextField11.getText());
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing value for set pitot heater temperature command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaSetPitotTempCommand (val);
    insertMessageArea ("Command sent: Set pitot heater temperature - " + val);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton12ActionPerformed

private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaActionSaveSettingsCommand();
    insertMessageArea ("Command sent: Save settings");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton41ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaModeIdleCommand();
    insertMessageArea ("Command sent: Mode Idle");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
// TODO add your handling code here:
    int opt;
    try
    {
        opt = Integer.parseInt(jTextField1.getText());
        if (opt < 0)
            opt = 0;
        if (opt > 4)
            opt = 4;
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing opt for search command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaModeSearchCommand (opt);
    insertMessageArea ("Command sent: Mode Search - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaModeStandbyCommand();
    insertMessageArea ("Command sent: Mode Standby");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaModeAirframeCommand();
    insertMessageArea ("Command sent: Mode Airframe");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton5ActionPerformed

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaModePodCommand();
    insertMessageArea ("Command sent: Mode Pod");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton6ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
// TODO add your handling code here:
    int opt;
    try
    {
        opt = Integer.parseInt(jTextField6.getText());
        if (opt < 0)
            opt = 0;
        if (opt > 4)
            opt = 4;
    }
    catch (Exception e)
    {
        insertMessageArea ("Error parsing opt for delete command");
        return;
    }
    
    cbrnPodCommand cmd = new anacondaActionDeleteCommand (opt);
    insertMessageArea ("Command sent: Action Delete - " + opt);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton7ActionPerformed

private void jButton42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton42ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaSendLcdaG();
    insertMessageArea ("Command sent: Send LCDA G");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton42ActionPerformed

private void jButton43ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton43ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaStopLcdaG();
    insertMessageArea ("Command sent: Stop LCDA G");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton43ActionPerformed

private void jButton44ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton44ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaSendLcdaH();
    insertMessageArea ("Command sent: Send LCDA H");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton44ActionPerformed

private void jButton45ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton45ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaStopLcdaH();
    insertMessageArea ("Command sent: Stop LCDA H");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton45ActionPerformed

private void jButton46ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton46ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaSendLcdbG();
    insertMessageArea ("Command sent: Send LCDB G");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton46ActionPerformed

private void jButton47ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton47ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaStopLcdbG();
    insertMessageArea ("Command sent: Stop LCDB G");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton47ActionPerformed

private void jButton48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton48ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaSendLcdbH();
    insertMessageArea ("Command sent: Send LCDB H");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton48ActionPerformed

private void jButton49ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton49ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaStopLcdbH();
    insertMessageArea ("Command sent: Stop LCDB H");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton49ActionPerformed

private void jButton50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton50ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new anacondaSetDateTimeCommand (System.currentTimeMillis()/1000);
    insertMessageArea ("Command sent: Set Date/Time sync'ed");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton50ActionPerformed

    private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
        // TODO add your handling code here:
        cbrnPodCommand cmd = new anacondaResetSampleUsage();
        insertMessageArea ("Command sent: Reset sample usage");
        m_Pods.sendCommand(cmd);
    }//GEN-LAST:event_jButton34ActionPerformed

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        // TODO add your handling code here:
        m_AnacondaSpectraDebugWindow.setVisible(true);
    }//GEN-LAST:event_jButton35ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JList msgOutput;
    // End of variables declaration//GEN-END:variables

}
