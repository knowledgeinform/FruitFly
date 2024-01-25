/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.display.docking.TitleOverlayBar;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;

/**
 *
 * @author humphjc1
 */
public class ErrorControlBar extends javax.swing.JPanel 
{
    private final static ImageIcon m_ClockDefault_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/Clock_Default.png"));
    private final static ImageIcon m_ClockHighlight_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/Clock_Highlight.png"));
    private final static ImageIcon m_ClockPressed_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/Clock_Pressed.png"));
    private final static ImageIcon m_RedXDefault_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/RedX_Default.png"));
    private final static ImageIcon m_RedXHighlight_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/RedX_Highlight.png"));
    private final static ImageIcon m_RedXPressed_Icon = new javax.swing.ImageIcon(TitleOverlayBar.class.getResource("/icons/RedX_Pressed.png"));
    
    private MissionErrorManager.ErrorCodeBase m_ErrorCode;
    private Color m_BackgroundColor;
    
    //Time last time this error code was put to sleep
    private long m_SleepingErrorTimeMs = -1;
    private long m_SleepingTimeMs;
    private boolean m_IgnoreError = false;

    /**
     * Creates new form ErrorControlBar
     */
    public ErrorControlBar(MissionErrorManager.ErrorCodeBase errorCode) 
    {
        initComponents();
        m_ErrorCode = errorCode;
        jLabel1.setText (errorCode.m_AlarmText);
        invalidate();
        validate();
        
        m_SleepingTimeMs = Config.getConfig().getPropertyAsLong("StatusErrors.SleepTimeMs", 60000);
        m_BackgroundColor = this.getBackground();
        
        
        JToggleButton1.addMouseListener(new MouseListener() 
        {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) 
            {
                JToggleButton1.setIcon (m_ClockPressed_Icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
                if (JToggleButton1.isSelected())
                {
                    JToggleButton1.setIcon (m_ClockPressed_Icon);
                    m_SleepingErrorTimeMs = System.currentTimeMillis();
                    jLabel1.setBackground(m_BackgroundColor);
                }
                else 
                {
                    JToggleButton1.setIcon (m_ClockDefault_Icon);
                    m_SleepingErrorTimeMs = -1;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                JToggleButton1.setIcon (m_ClockHighlight_Icon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (JToggleButton1.isSelected())
                    JToggleButton1.setIcon (m_ClockPressed_Icon);
                else
                    JToggleButton1.setIcon (m_ClockDefault_Icon);
            }
        });
        
        
        
        JToggleButton2.addMouseListener(new MouseListener() 
        {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) 
            {
                JToggleButton2.setIcon (m_RedXPressed_Icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
                if (JToggleButton2.isSelected())
                {
                    JToggleButton2.setIcon (m_RedXPressed_Icon);
                    m_IgnoreError = true;
                    jLabel1.setBackground(m_BackgroundColor);
                }
                else 
                {
                    JToggleButton2.setIcon (m_RedXDefault_Icon);
                    m_IgnoreError = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                JToggleButton2.setIcon (m_RedXHighlight_Icon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (JToggleButton2.isSelected())
                    JToggleButton2.setIcon (m_RedXPressed_Icon);
                else
                    JToggleButton2.setIcon (m_RedXDefault_Icon);
            }
        });
    }
    
    public boolean ignoreError ()
    {
        return (m_IgnoreError || (System.currentTimeMillis() - m_SleepingErrorTimeMs) < m_SleepingTimeMs);
    }
    
    public boolean equals (Object o)
    {
        if (o == null || !(o instanceof ErrorControlBar))
            return false;
        
        return (((ErrorControlBar)o).m_ErrorCode == this.m_ErrorCode);
    }
    
    public void updateBackgroundColor (Color c)
    {
        if ((System.currentTimeMillis() - m_SleepingErrorTimeMs) > m_SleepingTimeMs)
        {
            if (JToggleButton1.isSelected())
            {
                JToggleButton1.setSelected(false);
                JToggleButton1.setIcon (m_ClockDefault_Icon);
            }
            
            if (!m_IgnoreError)
            {
                jLabel1.setBackground(c);
            }
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

        JToggleButton1 = new javax.swing.JToggleButton();
        JToggleButton2 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();

        JToggleButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        JToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Clock_Default.png"))); // NOI18N
        JToggleButton1.setToolTipText("Snooze this message");
        JToggleButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JToggleButton1.setFocusable(false);
        JToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JToggleButton1ActionPerformed(evt);
            }
        });

        JToggleButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        JToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/RedX_Default.png"))); // NOI18N
        JToggleButton2.setToolTipText("Ignore this message");
        JToggleButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JToggleButton2.setFocusable(false);
        JToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JToggleButton2ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("ERROR TEXT");
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(JToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(JToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(JToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(JToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void JToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JToggleButton1ActionPerformed
        
        /*if (JToggleButton1.isSelected())
        {
            JToggleButton1.setIcon (m_ClockPressed_Icon);
            m_SleepingErrorTimeMs = System.currentTimeMillis();
        }
        else 
        {
            JToggleButton1.setIcon (m_ClockDefault_Icon);
            m_SleepingErrorTimeMs = -1;
            jLabel1.setBackground(this.getBackground());
        }*/
    }//GEN-LAST:event_JToggleButton1ActionPerformed

    private void JToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JToggleButton2ActionPerformed
        /*m_IgnoreError = !m_IgnoreError;
        
        if (!m_IgnoreError)
        {
            m_SleepingErrorTimeMs = -1;
        }
        else
        {
            jLabel1.setBackground(this.getBackground());
        }*/
    }//GEN-LAST:event_JToggleButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton JToggleButton1;
    private javax.swing.JToggleButton JToggleButton2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
