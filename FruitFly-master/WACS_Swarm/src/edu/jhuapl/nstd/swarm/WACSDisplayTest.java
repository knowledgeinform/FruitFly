/*
 * WACSDisplayTest.java
 *
 * Created on April 21, 2010, 8:49 AM
 */

package edu.jhuapl.nstd.swarm;

import edu.jhuapl.nstd.swarm.belief.BeliefManagerImpl;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.comms.BeliefManagerClient;

/**
 *
 * @author  humphjc1
 */
public class WACSDisplayTest extends javax.swing.JFrame implements Updateable {

    BeliefManagerImpl belMgr = null;
    RobotUpdateManager upMgr = null;
    

    /** Creates new form WACSDisplayTest */
    public WACSDisplayTest(BeliefManagerImpl newBelMgr, RobotUpdateManager newUpMgr) 
    {
        initComponents();
        
        belMgr = newBelMgr;
        upMgr = newUpMgr;
        
        this.setVisible(true);
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
        hbTimestampP0 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        hbTimestampP1 = new javax.swing.JTextField();
        syncButton = new javax.swing.JButton();
        logNewButton = new javax.swing.JButton();
        shutdownButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Pod 0 Hearbeat Timestamp:");

        hbTimestampP0.setText("hbTimestampP0");

        jLabel2.setText("Pod 1 Hearbeat Timestamp:");

        hbTimestampP1.setText("hbTimestampP1");

        syncButton.setText("Sync RTC");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        logNewButton.setText("Log New");
        logNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logNewButtonActionPerformed(evt);
            }
        });

        shutdownButton.setText("Shutdown");
        shutdownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shutdownButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(hbTimestampP0, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(hbTimestampP1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logNewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(shutdownButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(hbTimestampP0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(hbTimestampP1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(logNewButton)
                    .addComponent(shutdownButton))
                .addContainerGap(202, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_syncButtonActionPerformed

    private void logNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logNewButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_logNewButtonActionPerformed

    private void shutdownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shutdownButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_shutdownButtonActionPerformed

    
    
    @Override
    public void update() 
    {
        if (belMgr == null)
            return;
        
        CBRNHeartbeatBelief bbel = (CBRNHeartbeatBelief)belMgr.get(CBRNHeartbeatBelief.BELIEF_NAME);
        if(bbel !=null)
        {
            if (hbTimestampP0.isValid())
                hbTimestampP0.setText ("" + bbel.getTimestampMs(0));
            if (hbTimestampP1.isValid())
                hbTimestampP1.setText ("" + bbel.getTimestampMs(1));
        }
        else
        {
            if (hbTimestampP0.isValid())
                hbTimestampP0.setText ("N/A");
            if (hbTimestampP1.isValid())
                hbTimestampP1.setText ("N/A");
        }
    }
    
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) 
    {
        try
        {
            BeliefManagerImpl belMgr = new BeliefManagerImpl(WACSDisplayAgent.AGENTNAME);
            RobotUpdateManager upMgr = new RobotUpdateManager();
            WACSDisplayTest display = null;

            /*java.awt.EventQueue.invokeLater(new Runnable() 
            {
                public void run() 
                {*/
                    display = new WACSDisplayTest(belMgr, upMgr);
                    display.setVisible(true);
                /*}
            });*/

            upMgr.register(belMgr, Updateable.BELIEF);
            upMgr.register(display, Updateable.DISPLAY);
            upMgr.start();


            //create a BeliefManagerClient
            BeliefManagerClient client = new BeliefManagerClient(belMgr);
            while (true)
            {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hbTimestampP0;
    private javax.swing.JTextField hbTimestampP1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton logNewButton;
    private javax.swing.JButton shutdownButton;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables

}
