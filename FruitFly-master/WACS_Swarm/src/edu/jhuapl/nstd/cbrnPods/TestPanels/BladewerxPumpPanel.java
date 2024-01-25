/*
 * BladewerxPumpPanel.java
 *
 * Created on March 31, 2010, 8:29 AM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpPowerCommand;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages.bladewerxPumpStatusMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class BladewerxPumpPanel extends javax.swing.JPanel implements cbrnPodMessageListener {

    cbrnPodsInterface m_Pods;
    
    ArrayList<Method> statusMethods;
    
    
    /** Creates new form BladewerxPumpPanel */
    public BladewerxPumpPanel(cbrnPodsInterface pods) {
        initComponents();
        
        msgOutput.setModel(new DefaultListModel());
        
        //Set-up status message table
        DefaultTableModel statusModel = new DefaultTableModel();
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jTable1.setModel(
                statusModel);
        statusMethods = new ArrayList<Method>();
        for (Method m : bladewerxPumpStatusMessage.class.getMethods()) {
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
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodMsg.BLADEWERX_PUMP_STATUS_TYPE, this);


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
        if(m instanceof bladewerxPumpStatusMessage){
            insertMessageArea("Bladewerx Pump Status message received!");
            handleStatusMessage((bladewerxPumpStatusMessage)m);
        }
    }
    
    public void handleStatusMessage (bladewerxPumpStatusMessage msg)
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
                model.setRowCount(32);
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
                    if (model.size() > 53)
                        model.setSize(53);
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

        jButton1.setText("Pump On");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Pump Off");
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
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPumpPowerCommand (true);
    insertMessageArea ("Command sent: Pump on");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
    cbrnPodCommand cmd = new bladewerxPumpPowerCommand (false);
    insertMessageArea ("Command sent: Pump off");
    m_Pods.sendCommand(cmd);
}//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JList msgOutput;
    // End of variables declaration//GEN-END:variables

}
