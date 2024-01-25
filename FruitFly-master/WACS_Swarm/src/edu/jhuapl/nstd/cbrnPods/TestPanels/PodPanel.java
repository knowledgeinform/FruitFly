/*
 * PodPanel.java
 *
 * Created on January 25, 2010, 9:36 AM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podStartLogCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podSetRtcCommand;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podShutdownLogCommand;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowWAVSMSimulatorDisplay;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.display.ShadowDataForm;
import edu.jhuapl.nstd.swarm.util.ConfigTableView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  humphjc1
 */
public class PodPanel extends javax.swing.JPanel implements cbrnPodMessageListener {

	private cbrnPodsInterface m_Pods;
    private BeliefManager m_BelMgr;
    
    private ArrayList<Method> heartbeat0Methods;
    private ArrayList<Method> heartbeat1Methods;
    
    /** Config property table GUI */
    private ConfigTableView configTable;
    private ShadowDataForm m_shadowDataForm = null;
    private ShadowWAVSMSimulatorDisplay m_DebugDisplay;
    
    
    /** Creates new form PodPanel */
    public PodPanel(cbrnPodsInterface pods, BeliefManager bManager)
    {
        m_BelMgr = bManager;
    	initComponents();
        
        msgOutput.setModel(new DefaultListModel());
        
        //Set-up message 0 table
        DefaultTableModel heartbeat0Model = new DefaultTableModel();
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable1.setModel(
                heartbeat0Model);
        heartbeat0Methods = new ArrayList<Method>();
        for (Method m : podHeartbeatMessage.class.getMethods()) {
            heartbeat0Methods.add(m);
        }

        Collections.sort(heartbeat0Methods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : heartbeat0Methods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    heartbeat0Model.addColumn(m.getName().substring(2));
                } else {
                    heartbeat0Model.addColumn(m.getName().substring(3));
                }
            }
        }
        
        //Set-up message 1 table
        DefaultTableModel heartbeat1Model = new DefaultTableModel();
        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable2.setModel(
                heartbeat1Model);
        heartbeat1Methods = new ArrayList<Method>();
        for (Method m : podHeartbeatMessage.class.getMethods()) {
            heartbeat1Methods.add(m);
        }

        Collections.sort(heartbeat1Methods, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (Method m : heartbeat1Methods) {
            if (isGetter(m)) {
                if (m.getName().startsWith("is")) {
                    heartbeat1Model.addColumn(m.getName().substring(2));
                } else {
                    heartbeat1Model.addColumn(m.getName().substring(3));
                }
            }
        }


        m_Pods = pods;
        if (m_Pods != null)
        {
            m_Pods.addPersistentListener(cbrnSensorTypes.RABBIT_BOARD, cbrnPodMsg.POD_HEARTBEAT_TYPE, this);
        }

    }
    
    public boolean isGetter(Method m) 
    {
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
        if(m instanceof podHeartbeatMessage){
            insertMessageArea("Pod heartbeat message received: pod " + ((podHeartbeatMessage)m).getPodNumber());
            if (((podHeartbeatMessage)m).getPodNumber() == 0)
            {
                handleHeartbeat0Message((podHeartbeatMessage)m);
            }
            else // == 1
            {
                handleHeartbeat1Message((podHeartbeatMessage)m);
            }
            
            
        }
    }
    
    public void handleHeartbeat0Message (podHeartbeatMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : heartbeat0Methods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
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
                model.setRowCount(37);
            }
        });
        
        
    }
    
    public void handleHeartbeat1Message (podHeartbeatMessage msg)
    {
        final ArrayList data = new ArrayList();
        for (Method m : heartbeat1Methods) {
            if (isGetter(m)) {
                try {
                    data.add(m.invoke(msg));
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    data.add(null);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    data.add(null);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
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
                model.setRowCount(38);
            }
        });
        
        
    }
    
    public void insertMessageArea(final String message)
    {
        synchronized (this)
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
                        if (model.size() > 56)
                            model.setSize(56);
                    }
                });

            }
            catch (Exception e)
            {
                //eat it
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgOutput = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        jLabel1.setText("Message traffic:              (Newest at the top)");

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

        jScrollPane1.setViewportView(msgOutput);

        jButton1.setText("Sync RTC");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Stop Logging (Unmount)");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
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

        jButton3.setText("Start New Log (Mount)");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Show Piccolo Display");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setText("Show Shadow Data");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
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
                        .addGap(20, 20, 20)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 862, Short.MAX_VALUE))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jButton2)
                    .addComponent(jButton4)
                    .addComponent(jButton6))
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// 
    cbrnPodCommand cmd = new podSetRtcCommand(System.currentTimeMillis()/1000);
    insertMessageArea ("Command sent: set RTC (both boards)");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// 
    cbrnPodCommand cmd = new podShutdownLogCommand();
    insertMessageArea ("Command sent: shutdown (both boards)");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
// 
    cbrnPodCommand cmd = new podStartLogCommand();
    insertMessageArea ("Command sent: log new (both boards)");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    // TODO add your handling code here:
    Pic_Interface.showPicDisplay = true;
}//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        if (m_shadowDataForm == null)
        {
            m_shadowDataForm = new ShadowDataForm(ShadowOnboardAutopilotInterface.s_instance);
        }

        m_shadowDataForm.setVisible(!m_shadowDataForm.isVisible());
        
        if (m_DebugDisplay == null)
        {
            m_DebugDisplay = new ShadowWAVSMSimulatorDisplay(null);
            new Thread ()
            {
                public void run ()
                {
                    KnobsAutopilotInterface.CommandMessage copyCommandTo = ShadowOnboardAutopilotInterface.s_instance.getBlankCommandMessage();
                    KnobsAutopilotInterface.TelemetryMessage copyTelemetryTo = ShadowOnboardAutopilotInterface.s_instance.getBlankTelemetryMessage();
                    while (true)
                    {
                        ShadowOnboardAutopilotInterface.s_instance.copyLatestCommandMessage(copyCommandTo);
                        m_DebugDisplay.setCommandMessage ((ShadowOnboardAutopilotInterface.ShadowCommandMessage)copyCommandTo);
                        
                        ShadowOnboardAutopilotInterface.s_instance.copyLatestTelemetryMessage(copyTelemetryTo);
                        m_DebugDisplay.setTelemetryMessage((ShadowOnboardAutopilotInterface.ShadowTelemetryMessage)copyTelemetryTo);
                        
                        try {
                        Thread.sleep (250);} catch (Exception e) {e.printStackTrace();}
                    }

                }
            }.start();
        }
        m_DebugDisplay.setVisible(!m_DebugDisplay.isVisible());
    }//GEN-LAST:event_jButton6ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	if(configTable == null) {
		configTable = new ConfigTableView(null, "Property Configuration Table", false, m_BelMgr);
	} else {
		configTable.refreshTable();
	}
	
	configTable.setVisible(true);
}                                        


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JList msgOutput;
    // End of variables declaration//GEN-END:variables

}
