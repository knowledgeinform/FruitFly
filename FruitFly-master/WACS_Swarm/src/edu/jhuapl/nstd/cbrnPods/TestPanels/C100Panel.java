/*
 * C100Panel.java
 *
 * Created on January 18, 2010, 2:58 PM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.*;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetClosedCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Servo.servoSetOpenCommand;
import edu.jhuapl.nstd.swarm.util.Config;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
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
public class C100Panel extends javax.swing.JPanel implements cbrnPodMessageListener {

    cbrnPodsInterface m_Pods;
    
    ArrayList<Method> actionMethods;
    ArrayList<Method> statusMethods ;
    
    
    /** Creates new form C100Panel */
    public C100Panel(cbrnPodsInterface pods) {
        initComponents();
        
        msgOutput.setModel(new DefaultListModel());
        
        //Set-up action message table
        DefaultTableModel actionModel = new DefaultTableModel();
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable1.setModel(
                actionModel);
        actionMethods = new ArrayList<Method>();
        for (Method m : c100ActionMessage.class.getMethods()) {
            actionMethods.add(m);
        }

        Collections.sort(actionMethods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : actionMethods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    actionModel.addColumn(m.getName().substring(2));
                } else {
                    actionModel.addColumn(m.getName().substring(3));
                }
            }
        }
        
        //Set-up status message table
        DefaultTableModel statusModel = new DefaultTableModel();
        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable2.setModel(
                statusModel);
        statusMethods = new ArrayList<Method>();
        for (Method m : c100StatusMessage.class.getMethods()) {
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


        m_Pods = pods;
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_STATUS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_ACTION_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_FLOWSTATUS_TYPE, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_C100, cbrnPodMsg.C100_WARNING_TYPE, this);
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
        if(m instanceof c100ActionMessage){
            insertMessageArea("C100 Action message received!");
            handleActionMessage((c100ActionMessage)m);
        }
        if(m instanceof c100StatusMessage){
            insertMessageArea("C100 Status message received!");
            handleStatusMessage((c100StatusMessage)m);
        }
        if(m instanceof c100FlowStatusMessage){
            insertMessageArea("C100 Flow Status message received!");
            handleFlowStatusMessage((c100FlowStatusMessage)m);
        }
        if(m instanceof c100WarningMessage){
            insertMessageArea("C100 Warning message received!");
            handleWarningMessage((c100WarningMessage)m);
        }
    }
    
    public void handleActionMessage (c100ActionMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : actionMethods) {
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
                model.setRowCount(33);
            }
        });
        
        
    }
    
    public void handleStatusMessage (c100StatusMessage msg)
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
                model.setRowCount(34);
            }
        });
        
        
    }

    private void handleFlowStatusMessage(c100FlowStatusMessage msg)
    {
        insertMessageArea(msg.getStatus());
    }

    private void handleWarningMessage(c100WarningMessage msg)
    {
        insertMessageArea(msg.getWarning());
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
                    if (model.size() > 54)
                        model.setSize(54);
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
        jButton8 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jButton14 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jButton16 = new javax.swing.JButton();
        jTextField12 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jButton19 = new javax.swing.JButton();
        jTextField9 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jButton15 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jLabel25 = new javax.swing.JLabel();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();

        jLabel1.setText("Message traffic:              (Newest at the top)");

        jScrollPane1.setViewportView(msgOutput);

        jButton8.setText("Status");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton2.setText("Prime System");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextField1.setText("5");

        jLabel2.setText("sec to prime");

        jButton3.setText("Collect and Purge");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Collector On");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Collector Off");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton7.setText("Clean Collector");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton6.setText("Wet Sample");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jTextField2.setText("1");

        jLabel3.setText("(1-4)");

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

        jLabel4.setText("Raw command: ");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel5.setText("sec");

        jButton10.setText("Set");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel6.setText("timep2; Time to run pump 2 at beginning of collection to purge tubing of liquid that might spin off collector");

        jLabel7.setText("sec");

        jButton11.setText("Set");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel8.setText("extrap2; Time to run pump 2 at end of sample and end of cleaning cycle to purge tubing");

        jLabel12.setText("sec");

        jButton14.setText("Set");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel13.setText("priming2; Time to run pump 2 at end of priming to purge tubing");

        jLabel10.setText("sec");

        jLabel9.setText("sec");

        jButton12.setText("Set");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Set");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jLabel11.setText("primingd; Time to run RAC dry after priming is complete to make sure no residual liquid is left on collector surface");

        jLabel14.setText("priming1; Time to run pump 1 at start of priming to fill tubing from bottle to RAC");

        jLabel16.setText("sec");

        jButton16.setText("Set");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jLabel19.setText("rpm");

        jButton19.setText("Set");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jLabel15.setText("sec");

        jButton15.setText("Set");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jLabel17.setText("s; Sampling cycle time (wet and dry)");

        jLabel18.setText("cl; Clean cycle time when pump 1 is on and RAC is \"pulsing on and off'.  See IDD details.");

        jLabel20.setText("rs; RPM of C100");

        jLabel26.setText("Valve control:");

        jLabel21.setText("#1:");

        jButton20.setText("Open");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jButton21.setText("Close");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jLabel22.setText("#2:");

        jButton22.setText("Open");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        jButton23.setText("Close");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jLabel23.setText("#3:");

        jButton24.setText("Open");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        jButton25.setText("Close");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        jLabel24.setText("#4:");

        jButton26.setText("Open");
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });

        jButton27.setText("Close");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });

        jLabel27.setText("All valves:");

        jButton28.setText("Open");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });

        jButton29.setText("Close");
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });

        jButton9.setText("Reset Vials");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton17.setText("Reset and Restart");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton30.setText("Open");
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });

        jLabel25.setText("#5:");

        jButton31.setText("Close");
        jButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton31ActionPerformed(evt);
            }
        });

        jButton32.setText("Show Flow Status");
        jButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton32ActionPerformed(evt);
            }
        });

        jButton33.setText("Hide Flow Status");
        jButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton33ActionPerformed(evt);
            }
        });

        jButton18.setText("Servo Open");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jButton34.setText("Servo Close");
        jButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton34ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel6))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel8))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel12)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton14)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel13))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9)
                                .addComponent(jLabel10))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButton12)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel14))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButton13)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel11))))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel16)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton16))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel19)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton19))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel15)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton15)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel17)
                                .addComponent(jLabel18)
                                .addComponent(jLabel20)))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel26)
                            .addGap(23, 23, 23)
                            .addComponent(jLabel21)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton20)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton21)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel22)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton22)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton23)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel23)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton24)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton25)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel24)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton26)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton27)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel25)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton30)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton31))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(137, 137, 137)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel3))
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 852, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(95, 95, 95)
                            .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel27)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton28)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton29))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton8)
                            .addGap(50, 50, 50)
                            .addComponent(jButton4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(50, 50, 50)
                            .addComponent(jButton18)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton34))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButton6)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8)
                    .addComponent(jButton9)
                    .addComponent(jButton7)
                    .addComponent(jButton5)
                    .addComponent(jButton4)
                    .addComponent(jButton18)
                    .addComponent(jButton34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jButton10)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jButton11)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jButton12)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jButton13)
                        .addComponent(jLabel11)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jButton14)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jButton15)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jButton16)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton19)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jLabel21)
                    .addComponent(jButton20)
                    .addComponent(jButton21)
                    .addComponent(jLabel22)
                    .addComponent(jButton22)
                    .addComponent(jButton23)
                    .addComponent(jLabel23)
                    .addComponent(jButton24)
                    .addComponent(jButton25)
                    .addComponent(jLabel24)
                    .addComponent(jButton26)
                    .addComponent(jButton27)
                    .addComponent(jLabel25)
                    .addComponent(jButton30)
                    .addComponent(jButton31))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(jButton28)
                    .addComponent(jButton29)
                    .addComponent(jButton32)
                    .addComponent(jButton33)
                    .addComponent(jButton17))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
// 
    cbrnPodCommand cmd = new c100StatusCommand();
    insertMessageArea ("Command sent: status");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton8ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// 
    int sec = 0;
    try       
    {
        sec = Integer.parseInt(jTextField1.getText());
    }
    catch (Exception e)
    {
        sec = 0;
    }
    cbrnPodCommand cmd = new c100PrimeCommand(sec);
    insertMessageArea ("Command sent: prime," + sec);
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
// 
    cbrnPodCommand cmd = new c100CollectAndPurgeCommand();
    insertMessageArea ("Command sent: collectAndPurge");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
// 
    cbrnPodCommand cmd = new c100CollectOnCommand();
    insertMessageArea ("Command sent: collectOn");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
// 
    cbrnPodCommand cmd = new c100CollectOffCommand();
    insertMessageArea ("Command sent: collectOff");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton5ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
// 
    cbrnPodCommand cmd = new c100CleanCommand();
    insertMessageArea ("Command sent: clean");
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton7ActionPerformed

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
// 
    int y = 1;
    try       
    {
        y = Integer.parseInt(jTextField2.getText());
    }
    catch (Exception e)
    {
        insertMessageArea("Error parsing vial number for sample generation");
        return;
    }
    cbrnPodCommand cmd = new c100SampleCommand(y);
    insertMessageArea ("Command sent: sample," + y);
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton6ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// 
    String text = jTextField3.getText();
    cbrnPodCommand cmd = new c100RawCommand(text + "\r");
    insertMessageArea ("Command sent: raw," + text);
    m_Pods.sendCommand(cmd);
    
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField4.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: timep2");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_TIMEP2, sec);
    insertMessageArea ("Command sent: config timep2," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton10ActionPerformed

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField5.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: extrap2");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_EXTRAP2, sec);
    insertMessageArea ("Command sent: config extrap2," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton11ActionPerformed

private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField8.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: priming2");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_PRIMING2, sec);
    insertMessageArea ("Command sent: config priming2," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton14ActionPerformed

private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField6.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: priming1");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_PRIMING1, sec);
    insertMessageArea ("Command sent: config priming1," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton12ActionPerformed

private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField7.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: primingd");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_PRIMINGD, sec);
    insertMessageArea ("Command sent: config primingd," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton13ActionPerformed

private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField10.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: s");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_S, sec);
    insertMessageArea ("Command sent: config s," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton16ActionPerformed

private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField12.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: rs");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_RS, sec);
    insertMessageArea ("Command sent: config rs," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton19ActionPerformed

private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
// TODO add your handling code here:
    int sec;
    try       
    {
        sec = Integer.parseInt(jTextField9.getText());
        
    }
    catch (Exception e)
    {
        insertMessageArea ("Unable to parse input: cl");
        return;
    }
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_CL, sec);
    insertMessageArea ("Command sent: config cl," + sec);
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton15ActionPerformed

private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000001<<16) | (0x00000001));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton20ActionPerformed

private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000001<<16) | (0x00000000));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton21ActionPerformed

private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000002<<16) | (0x00000001));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton22ActionPerformed

private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000002<<16) | (0x00000000));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton23ActionPerformed

private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000003<<16) | (0x00000001));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton24ActionPerformed

private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000003<<16) | (0x00000000));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton25ActionPerformed

private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000004<<16) | (0x00000001));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton26ActionPerformed

private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000004<<16) | (0x00000000));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton27ActionPerformed

private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
// TODO add your handling code here:
    jButton20ActionPerformed(null);
    jButton22ActionPerformed(null);
    jButton24ActionPerformed(null);
    jButton26ActionPerformed(null);
    jButton30ActionPerformed(null);
}//GEN-LAST:event_jButton28ActionPerformed

private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
// TODO add your handling code here:
    jButton21ActionPerformed(null);
    jButton23ActionPerformed(null);
    jButton25ActionPerformed(null);
    jButton27ActionPerformed(null);
    jButton31ActionPerformed(null);
}//GEN-LAST:event_jButton29ActionPerformed

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new c100ResetVialsCommand();
    insertMessageArea ("Command sent: Reset vials");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton9ActionPerformed

private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
    // TODO add your handling code here:
    String resetText = "xc:$reset";
    String restartText = "$restart";

    cbrnPodCommand resetCmd = new c100RawCommand(resetText + "\r");
    cbrnPodCommand restartCmd = new c100RawCommand(restartText + "\r");

    m_Pods.sendCommand(resetCmd);
    insertMessageArea ("Command sent: " + resetText);

    try {Thread.sleep(1000);
    } catch (InterruptedException ex) { Logger.getLogger(C100Panel.class.getName()).log(Level.SEVERE, null, ex); }

    m_Pods.sendCommand(restartCmd);
    insertMessageArea ("Command sent: " + restartText);


}//GEN-LAST:event_jButton17ActionPerformed

private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_jTextField3ActionPerformed

private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000005<<16) | (0x00000001));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton30ActionPerformed

private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new c100ConfigCommand((char)cbrnPodCommand.C100_CONFIG_VALVES, (0x00000005<<16) | (0x00000000));
    insertMessageArea ("Command sent: config valves");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton31ActionPerformed

private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new c100FlowStatusCommand(1);
    insertMessageArea ("Command sent: show flow values");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton32ActionPerformed

private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
    // TODO add your handling code here:
    cbrnPodCommand cmd = new c100FlowStatusCommand(0);
    insertMessageArea ("Command sent: hide flow values");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton33ActionPerformed

    private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
        cbrnPodCommand cmd = new servoSetClosedCommand((char)1);
        insertMessageArea ("Command sent: close servo");
        m_Pods.sendCommandToBoard(cmd, 0);
    }//GEN-LAST:event_jButton34ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        cbrnPodCommand cmd = new servoSetOpenCommand((char)1);
        insertMessageArea ("Command sent: open servo");
        m_Pods.sendCommandToBoard(cmd, 0);
    }//GEN-LAST:event_jButton18ActionPerformed



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
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField12;
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
