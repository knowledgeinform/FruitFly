/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.MissionErrorManager.ErrorCodeBase;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.MissionActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.MissionCommandedStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.JScrollPane;

/**
 *
 * @author humphjc1
 */
public class MissionManagerPanel extends javax.swing.JPanel implements MissionErrorManager.CommandedBeliefErrorListener
{
    BeliefManagerWacs m_BeliefManager;
    MissionStatusErrorsPanel m_MissionErrorsPanel;
    SensorSummaryStopLightPanel m_SensorSummaryPanel;
    
    private Date m_LastCommandedStateTime;
    private Date m_LastActualStateTime;
    
    private final Object m_AnacondaSampleUsedLock = new Object();
    private boolean m_AnacondaSample1Used;
    private boolean m_AnacondaSample2Used;
    private boolean m_AnacondaSample3Used;
    private boolean m_AnacondaSample4Used;
    private final Object m_C100SampleUsedLock = new Object();
    private boolean m_C100Sample1Used;
    private boolean m_C100Sample2Used;
    private boolean m_C100Sample3Used;
    private boolean m_C100Sample4Used;
    

    /**
     * Creates new form MainControlPanel
     */
    public MissionManagerPanel(BeliefManagerWacs belMgr) {
        initComponents();
        
        //final JPanel subPanel = new SensorSummaryPanel();
        m_SensorSummaryPanel = new SensorSummaryStopLightPanel(belMgr);
        m_SensorSummaryPanel.setBorder(jPanel5.getBorder());
        jPanel5.addComponentListener(new ComponentListener() 
        {
            @Override
            public void componentResized(ComponentEvent e) {
                m_SensorSummaryPanel.setSize(jPanel5.getSize());
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        jPanel5.add(m_SensorSummaryPanel);
        
        m_MissionErrorsPanel = new MissionStatusErrorsPanel(m_BeliefManager);
        m_MissionErrorsPanel.setBorder(jPanel3.getBorder());
        final JScrollPane scrollPane = new JScrollPane(m_MissionErrorsPanel);
        jPanel3.addComponentListener(new ComponentListener() 
        {
            @Override
            public void componentResized(ComponentEvent e) {
                scrollPane.setSize(jPanel3.getSize());
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        //jPanel3.add(m_MissionErrorsPanel);
        jPanel3.add(scrollPane);
        
        m_BeliefManager = belMgr;
        
        MissionErrorManager.getInstance().registerErrorListener(this);
    }
    
    public void update()
    {
        MissionCommandedStateBelief commandedState = (MissionCommandedStateBelief)m_BeliefManager.get(MissionCommandedStateBelief.BELIEF_NAME);
        if (commandedState == null || m_LastCommandedStateTime == null || commandedState.getTimeStamp().after(m_LastCommandedStateTime))
        {
            if (commandedState == null && m_LastCommandedStateTime == null)
            {
                jLabel6.setText(MissionCommandedStateBelief.getStateText(-1));
                m_LastCommandedStateTime = new Date(0);
            }
            else if (commandedState != null)
            {
                jLabel6.setText(commandedState.getStateText());
                m_LastCommandedStateTime = commandedState.getTimeStamp();
            }
        }
        
        MissionActualStateBelief actualState = (MissionActualStateBelief)m_BeliefManager.get(MissionActualStateBelief.BELIEF_NAME);
        if (actualState == null || m_LastActualStateTime == null || actualState.getTimeStamp().after(m_LastActualStateTime))
        {
            if (actualState == null && m_LastActualStateTime == null)
            {
                jLabel7.setText(MissionCommandedStateBelief.getStateText(-1));
                m_LastCommandedStateTime = new Date(0);
            }
            else if (actualState != null)
            {
                jLabel7.setText(actualState.getStateText());
                m_LastActualStateTime = actualState.getTimeStamp();
            }
        }
        
        if (m_MissionErrorsPanel != null)
        {
            m_MissionErrorsPanel.update();
        }
        
        if (m_SensorSummaryPanel != null)
        {
            m_SensorSummaryPanel.update();
        }
        
        boolean s1Used = false;
        boolean s2Used = false;
        boolean s3Used = false;
        boolean s4Used = false;
        ParticleCollectorActualStateBelief caBelief = (ParticleCollectorActualStateBelief)m_BeliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
        if(caBelief!=null)
        {
            synchronized (m_C100SampleUsedLock)
            {
                m_C100Sample1Used = caBelief.isSample1full();
                m_C100Sample2Used = caBelief.isSample2full();
                m_C100Sample3Used = caBelief.isSample3full();
                m_C100Sample4Used = caBelief.isSample4full();
            }
        }
        AnacondaActualStateBelief anBelief = (AnacondaActualStateBelief)m_BeliefManager.get(AnacondaActualStateBelief.BELIEF_NAME);
        if(anBelief!=null)
        {
            synchronized (m_AnacondaSampleUsedLock)
            {
                m_AnacondaSample1Used = caBelief.isSample1full();
                m_AnacondaSample2Used = caBelief.isSample2full();
                m_AnacondaSample3Used = caBelief.isSample3full();
                m_AnacondaSample4Used = caBelief.isSample4full();
            }
        }
    }
    
    private int getSampleIndex (boolean requireAnacondaFree, boolean requireC100Free)
    {
        int sampleNum = 0;
        try
        {
            sampleNum = Integer.parseInt((String)jComboBox1.getSelectedItem());
            
            boolean sampleUsed = false;
            if (requireAnacondaFree)
            {
                synchronized (m_AnacondaSampleUsedLock)
                {
                    if ((sampleNum == 1 && m_AnacondaSample1Used) || 
                            (sampleNum == 2 && m_AnacondaSample2Used) || 
                            (sampleNum == 3 && m_AnacondaSample3Used) || 
                            (sampleNum == 4 && m_AnacondaSample4Used))
                    {
                        sampleUsed = true;
                        sampleNum = -1;
                    }
                }
            }
            if (requireC100Free)
            {
                synchronized (m_C100SampleUsedLock)
                {
                    if ((sampleNum == 1 && m_C100Sample1Used) || 
                            (sampleNum == 2 && m_C100Sample2Used) || 
                            (sampleNum == 3 && m_C100Sample3Used) || 
                            (sampleNum == 4 && m_C100Sample4Used))
                    {
                        sampleUsed = true;
                        sampleNum = -1;
                    }
                }
            }
            
            if (sampleUsed)
                JOptionPane.showMessageDialog(this, "This sample has already been used.  Mission not updated.", "Error", JOptionPane.ERROR_MESSAGE);
            
            return sampleNum;
        }
        catch (Exception e)
        {
            //No sample selected, apparently - force user to pick one
            JOptionPane.showMessageDialog(this, "You must select an index for sample storage.  Mission not updated.", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    
    @Override
    public void handleCommandedBeliefError(ErrorCodeBase alarmCode, int alarmLevel) 
    {
        if (alarmCode == MissionErrorManager.COMMANDACTUALSYNC_MISSIONMODE_ERRORCODE)
        {
            alarmLabelBackground(jLabel7, alarmLevel);
        }
    }
    
    public void alarmLabelBackground (JLabel label, int alarmLevel)
    {
        if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
            alarmLabelBackground (label, Color.RED);
        else if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
            alarmLabelBackground (label, Color.ORANGE);
        else
            alarmLabelBackground (label, null);
    }
    
    public void alarmLabelBackground (JLabel label, Color backgroundColor)
    {
        if (backgroundColor != null && (!label.getBackground().equals (backgroundColor) || !label.isOpaque()))
        {    
            label.setBackground(backgroundColor);
            label.setOpaque(true);
        }
        else if (backgroundColor == null && label.isOpaque())
        {
            label.setOpaque(false);
            label.repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("WACS MISSION CONTROL");
        jLabel1.setOpaque(true);

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setText("INGRESS");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("PRE-FLIGHT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton5.setText("MANUAL");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton3.setText("STRIKE MODE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("EGRESS");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("SAMPLE STORAGE:");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jComboBox1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UNK", "1", "2", "3", "4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 20, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 113, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("COMMANDED:");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("ACTUAL:");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setText(" ");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText(" ");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addGap(15, 15, 15)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 179, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        MissionCommandedStateBelief belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.PREFLIGHT_STATE);
        m_BeliefManager.put(belief);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        int sampleNum = getSampleIndex(true, true);
        
        MissionCommandedStateBelief belief = null;
        switch (sampleNum)
        {
            case 1:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.SEARCH1_STATE);
                break;
            case 2:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.SEARCH2_STATE);
                break;
            case 3:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.SEARCH3_STATE);
                break;
            case 4:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.SEARCH4_STATE);
                break;
        };
        
        if (belief != null)
        {
            m_BeliefManager.put(belief);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        MissionCommandedStateBelief belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.INGRESS_STATE);
        m_BeliefManager.put(belief);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        
        int sampleNum = getSampleIndex(false, true);
        
        MissionCommandedStateBelief belief = null;
        switch (sampleNum)
        {
            case 1:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.EGRESS1_STATE);
                break;
            case 2:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.EGRESS2_STATE);
                break;
            case 3:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.EGRESS3_STATE);
                break;
            case 4:
                belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.EGRESS4_STATE);
                break;
        };
        
        if (belief != null)
        {
            m_BeliefManager.put(belief);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        MissionCommandedStateBelief belief = new MissionCommandedStateBelief (m_BeliefManager.getName(), MissionCommandedStateBelief.MANUAL_STATE);
        m_BeliefManager.put(belief);
    }//GEN-LAST:event_jButton5ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    // End of variables declaration//GEN-END:variables

}
