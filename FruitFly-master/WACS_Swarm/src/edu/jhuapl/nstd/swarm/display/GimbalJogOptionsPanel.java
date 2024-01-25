/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GimbalJogOptionsPanel.java
 *
 * Created on Jul 19, 2011, 12:36:41 PM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author humphjc1
 */
public class GimbalJogOptionsPanel extends javax.swing.JFrame {

    BeliefManager _beliefManager;
    protected String m_JogDistLastChosenUnits;
    protected String m_JogAltLastChosenUnits;



    /** Creates new form GimbalJogOptionsPanel */
    public GimbalJogOptionsPanel(BeliefManager belMgr) {
        initComponents();

        _beliefManager = belMgr;

        m_JogAltUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_JogAltUnitsInput.setSelectedIndex(1);
        m_JogDistUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        m_JogDistUnitsInput.setSelectedIndex(0);
        
    }

    private Double getJogDistM()
    {
        double jog =  Double.parseDouble(m_JogDistance.getText());
        Double jogM = null;
        if (m_JogDistUnitsInput.getSelectedItem().toString().equals ("ft"))
            jogM = new Double (jog*0.3048);
        else //if (m_JogDistUnitsInput.getSelectedItem().toString().equals ("m"))
            jogM = new Double (jog);

        return jogM;
    }

    private Double getJogAltM()
    {
        double jog =  Double.parseDouble(m_JogAltitude.getText());
        Double jogM = null;
        if (m_JogAltUnitsInput.getSelectedItem().toString().equals ("ft"))
            jogM = new Double (jog*0.3048);
        else //if (m_JogDistUnitsInput.getSelectedItem().toString().equals ("m"))
            jogM = new Double (jog);

        return jogM;
    }

    protected void updateGimbalTargetBelief(double offset, NavyAngle direction, double alt)
    {
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        TargetActualBelief targets = (TargetActualBelief)_beliefManager.get(TargetActualBelief.BELIEF_NAME);

        if(targets != null)
        {
            PositionTimeName ptn = targets.getPositionTimeName(tmp);
            if(ptn !=null)
            {
                LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                RangeBearingHeightOffset off = new RangeBearingHeightOffset(new Length(offset,Length.METERS),direction,new Length(alt,Length.METERS));
                AbsolutePosition newlla = lla.translatedBy(off);
                _beliefManager.put(new TargetCommandedBelief(WACSDisplayAgent.AGENTNAME,
							 newlla,
							 Length.ZERO,
							 tmp));
            }
        }

    }

    private String changeLengthUnits (final JTextField inputField, JComboBox unitsField, String lastUnits, final String fieldType)
    {
        final String oldText = inputField.getText();
        final String oldUnits = lastUnits;
        final String newUnits = unitsField.getSelectedItem().toString();
        final javax.swing.JFrame parent = this;

        if (oldText != null && !oldText.equals (""))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                     try
                    {
                        double oldValue = Double.parseDouble(oldText);

                        if (newUnits.equals ("m") && oldUnits != null && oldUnits.equals ("ft"))
                        {
                            double newValue = oldValue*0.3048;
                            inputField.setText (newValue + "");
                            inputField.setCaretPosition(0);
                        }
                        else if(newUnits.equals("ft") && oldUnits != null && oldUnits.equals("m"))
                        {
                            double newValue = oldValue/0.3048;
                            inputField.setText (newValue + "");
                            inputField.setCaretPosition(0);
                        }

                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(parent, "Error parsing " + fieldType + ": " + oldText);
                        e.printStackTrace ();
                    }
                }
            });
        }

        lastUnits = newUnits;
        return lastUnits;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_JogDN = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        m_JogSW = new javax.swing.JButton();
        m_JogUp = new javax.swing.JButton();
        m_JogAltitude = new javax.swing.JTextField();
        m_JogDistance = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        m_JogNW = new javax.swing.JButton();
        m_JogN = new javax.swing.JButton();
        m_JogNE = new javax.swing.JButton();
        m_JogE = new javax.swing.JButton();
        m_JogW = new javax.swing.JButton();
        m_JogSE = new javax.swing.JButton();
        m_JogS = new javax.swing.JButton();
        m_JogDistUnitsInput = new javax.swing.JComboBox();
        m_JogS2 = new javax.swing.JButton();
        m_JogAltUnitsInput = new javax.swing.JComboBox();
        m_JogS1 = new javax.swing.JButton();

        setTitle("Gimbal Jog");

        m_JogDN.setText("DN");
        m_JogDN.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogDN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogDNActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel10.setText("Jog Distance");

        m_JogSW.setText("SW");
        m_JogSW.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogSW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogSWActionPerformed(evt);
            }
        });

        m_JogUp.setText("UP");
        m_JogUp.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogUpActionPerformed(evt);
            }
        });

        m_JogAltitude.setText("10.0");
        m_JogAltitude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogAltitudeActionPerformed(evt);
            }
        });

        m_JogDistance.setText("10.0");
        m_JogDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogDistanceActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel11.setText("Jog Altitude");

        m_JogNW.setText("NW");
        m_JogNW.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogNW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogNWActionPerformed(evt);
            }
        });

        m_JogN.setText("N");
        m_JogN.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogNActionPerformed(evt);
            }
        });

        m_JogNE.setText("NE");
        m_JogNE.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogNE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogNEActionPerformed(evt);
            }
        });

        m_JogE.setText("E");
        m_JogE.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogEActionPerformed(evt);
            }
        });

        m_JogW.setText("W");
        m_JogW.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogWActionPerformed(evt);
            }
        });

        m_JogSE.setText("SE");
        m_JogSE.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogSE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogSEActionPerformed(evt);
            }
        });

        m_JogS.setFont(new java.awt.Font("Tahoma", 1, 11));
        m_JogS.setText("JOG");
        m_JogS.setBorder(null);
        m_JogS.setBorderPainted(false);
        m_JogS.setContentAreaFilled(false);
        m_JogS.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogSActionPerformed(evt);
            }
        });

        m_JogDistUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_JogDistUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogDistUnitsInputActionPerformed(evt);
            }
        });

        m_JogS2.setFont(new java.awt.Font("Tahoma", 1, 11));
        m_JogS2.setBorder(null);
        m_JogS2.setBorderPainted(false);
        m_JogS2.setContentAreaFilled(false);
        m_JogS2.setEnabled(false);
        m_JogS2.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogS2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogS2ActionPerformed(evt);
            }
        });

        m_JogAltUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_JogAltUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogAltUnitsInputActionPerformed(evt);
            }
        });

        m_JogS1.setText("S");
        m_JogS1.setPreferredSize(new java.awt.Dimension(25, 15));
        m_JogS1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_JogS1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(m_JogSW, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogW, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogNW, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_JogS1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogN, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_JogSE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_JogNE, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_JogS2, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addComponent(m_JogDN, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_JogUp, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_JogAltitude, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_JogDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_JogAltUnitsInput, 0, 0, Short.MAX_VALUE)
                            .addComponent(m_JogDistUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_JogNW, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogN, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogNE, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogUp, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_JogW, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogS, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogE, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogS2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_JogSW, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogS1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogSE, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogDN, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(m_JogDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogDistUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(m_JogAltitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_JogAltUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_JogDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogDNActionPerformed
        Double jogM = getJogAltM();
        updateGimbalTargetBelief(0,NavyAngle.NORTH,-jogM);
}//GEN-LAST:event_m_JogDNActionPerformed

    private void m_JogSWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogSWActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.SOUTHWEST,0);
}//GEN-LAST:event_m_JogSWActionPerformed

    private void m_JogUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogUpActionPerformed
        Double jogM = getJogAltM();
        updateGimbalTargetBelief(0,NavyAngle.NORTH,jogM);
}//GEN-LAST:event_m_JogUpActionPerformed

    private void m_JogAltitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogAltitudeActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_m_JogAltitudeActionPerformed

    private void m_JogDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogDistanceActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_m_JogDistanceActionPerformed

    private void m_JogNWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogNWActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.NORTHWEST,0);
}//GEN-LAST:event_m_JogNWActionPerformed

    private void m_JogNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogNActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.NORTH,0);
}//GEN-LAST:event_m_JogNActionPerformed

    private void m_JogNEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogNEActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.NORTHEAST,0);
}//GEN-LAST:event_m_JogNEActionPerformed

    private void m_JogEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogEActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.EAST,0);
}//GEN-LAST:event_m_JogEActionPerformed

    private void m_JogWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogWActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.WEST,0);
}//GEN-LAST:event_m_JogWActionPerformed

    private void m_JogSEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogSEActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.SOUTHEAST,0);
}//GEN-LAST:event_m_JogSEActionPerformed

    private void m_JogSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogSActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.SOUTH,0);
}//GEN-LAST:event_m_JogSActionPerformed

    private void m_JogDistUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogDistUnitsInputActionPerformed
        // TODO add your handling code here:
        m_JogDistLastChosenUnits = changeLengthUnits(m_JogDistance, m_JogDistUnitsInput, m_JogDistLastChosenUnits, "jog distance");
}//GEN-LAST:event_m_JogDistUnitsInputActionPerformed

    private void m_JogS2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogS2ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_m_JogS2ActionPerformed

    private void m_JogAltUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogAltUnitsInputActionPerformed
        // TODO add your handling code here:
        m_JogAltLastChosenUnits = changeLengthUnits(m_JogAltitude, m_JogAltUnitsInput, m_JogAltLastChosenUnits, "jog altitude");
}//GEN-LAST:event_m_JogAltUnitsInputActionPerformed

    private void m_JogS1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_JogS1ActionPerformed
        Double jogM = getJogDistM();
        updateGimbalTargetBelief(jogM,NavyAngle.SOUTH,0);
}//GEN-LAST:event_m_JogS1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JComboBox m_JogAltUnitsInput;
    private javax.swing.JTextField m_JogAltitude;
    private javax.swing.JButton m_JogDN;
    private javax.swing.JComboBox m_JogDistUnitsInput;
    private javax.swing.JTextField m_JogDistance;
    private javax.swing.JButton m_JogE;
    private javax.swing.JButton m_JogN;
    private javax.swing.JButton m_JogNE;
    private javax.swing.JButton m_JogNW;
    private javax.swing.JButton m_JogS;
    private javax.swing.JButton m_JogS1;
    private javax.swing.JButton m_JogS2;
    private javax.swing.JButton m_JogSE;
    private javax.swing.JButton m_JogSW;
    private javax.swing.JButton m_JogUp;
    private javax.swing.JButton m_JogW;
    // End of variables declaration//GEN-END:variables

}
