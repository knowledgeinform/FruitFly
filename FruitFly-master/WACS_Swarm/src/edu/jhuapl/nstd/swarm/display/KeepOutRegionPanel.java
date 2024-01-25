/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;

/**
 *
 * @author humphjc1
 */
public class KeepOutRegionPanel extends javax.swing.JPanel 
{
    BeliefManager m_BeliefManager;
    SearchCanvas m_SearchCanvasPanel;
            

    /**
     * Creates new form KeepOutRegionPanel
     */
    public KeepOutRegionPanel(BeliefManager beliefManager, SearchCanvas searchCanvasPanel) 
    {
        initComponents();
        
        m_BeliefManager = beliefManager;
        m_SearchCanvasPanel = searchCanvasPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();

        jToggleButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton1.setText("Define New Region");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        if (m_SearchCanvasPanel != null)
        {
            try
            {
                if (jToggleButton1.isSelected())
                    m_SearchCanvasPanel.startDefiningKeepOutRegion ();
                else
                    m_SearchCanvasPanel.stopDefiningKeepOutRegion ();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}