/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SafetyBoxPanel.java
 *
 * Created on Oct 5, 2010, 2:20:28 PM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.SafetyBoxBelief;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;


public class SafetyBoxPanel extends javax.swing.JPanel
{
    SearchCanvas m_canvas;
    BeliefManager m_beliefManager;
    SafetyBoxBelief m_safetyBoxBelief = null;

    boolean m_UnitsChanged;
    final DecimalFormat m_DecFormat2 = new DecimalFormat ("#.##");

    /** Creates new form SafetyBoxPanel */
    public SafetyBoxPanel(SearchCanvas canvas, BeliefManager beliefMgr)
    {
        initComponents();

        m_UnitsChanged = false;
        m_canvas = canvas;
        m_beliefManager = beliefMgr;

        m_canvas.setSafetyBoxPanel(this);


        minAlt_units.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        minAlt_units.setSelectedIndex(1);
        maxAlt_units.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        maxAlt_units.setSelectedIndex(1);
        minAlt_type.setModel (new DefaultComboBoxModel (new Object[] {"AGL", "MSL"}));
        minAlt_type.setSelectedIndex(1);
        maxAlt_type.setModel (new DefaultComboBoxModel (new Object[] {"AGL", "MSL"}));
        maxAlt_type.setSelectedIndex(1);
        minRadius_units.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        minRadius_units.setSelectedIndex(0);
        maxRadius_units.setModel (new DefaultComboBoxModel (new Object[] {"m", "ft"}));
        maxRadius_units.setSelectedIndex(0);
    }

    public boolean unitsHaveChanged ()
    {
        return m_UnitsChanged;
    }

    public void setSafetyBoxBelief(SafetyBoxBelief safetyBoxBelief)

    {
        if (!chkAllowEditing.isSelected())
        {
            txtLatitude1_deg.setText(Double.toString(safetyBoxBelief.getLatitude1_deg()));
            txtLatitude2_deg.setText(Double.toString(safetyBoxBelief.getLatitude2_deg()));
            txtLongitude1_deg.setText(Double.toString(safetyBoxBelief.getLongitude1_deg()));
            txtLongitude2_deg.setText(Double.toString(safetyBoxBelief.getLongitude2_deg()));
            txtMinAlt.setText(m_DecFormat2.format((safetyBoxBelief.getMinAltitude_m()/(minAlt_units.getSelectedItem().toString().equals("ft")?0.3048:1))));
            txtMaxAlt.setText(m_DecFormat2.format((safetyBoxBelief.getMaxAltitude_m()/(maxAlt_units.getSelectedItem().toString().equals("ft")?0.3048:1))));
            if (safetyBoxBelief.getMinAlt_IsAGL())
                minAlt_type.setSelectedItem("AGL");
            else
                minAlt_type.setSelectedItem("MSL");
            if (safetyBoxBelief.getMaxAlt_IsAGL())
                maxAlt_type.setSelectedItem("AGL");
            else
                maxAlt_type.setSelectedItem("MSL");
            txtMinRadius.setText(m_DecFormat2.format((safetyBoxBelief.getMinRadius_m()/(minRadius_units.getSelectedItem().toString().equals("ft")?0.3048:1))));
            txtMaxRadius.setText(m_DecFormat2.format((safetyBoxBelief.getMaxRadius_m()/(maxRadius_units.getSelectedItem().toString().equals("ft")?0.3048:1))));
            m_UnitsChanged = false;
        }

        m_safetyBoxBelief = safetyBoxBelief;
    }

    public void setSafetyBoxPosition1(LatLonAltPosition position1)
    {
        txtLatitude1_deg.setText(Double.toString(position1.getLatitude().getDoubleValue(Angle.DEGREES)));
        txtLongitude1_deg.setText(Double.toString(position1.getLongitude().getDoubleValue(Angle.DEGREES)));

        txtLatitude2_deg.setText(Double.toString(position1.getLatitude().getDoubleValue(Angle.DEGREES)));
        txtLongitude2_deg.setText(Double.toString(position1.getLongitude().getDoubleValue(Angle.DEGREES)));

        m_canvas.setProposedSafetyBox(new Latitude(Double.parseDouble(txtLatitude1_deg.getText()), Angle.DEGREES),
                                      new Latitude(Double.parseDouble(txtLatitude2_deg.getText()), Angle.DEGREES),
                                      new Longitude(Double.parseDouble(txtLongitude1_deg.getText()), Angle.DEGREES),
                                      new Longitude(Double.parseDouble(txtLongitude2_deg.getText()), Angle.DEGREES));
    }
    
    public void setSafetyBoxPosition2(LatLonAltPosition position2)
    {
        if (txtLatitude1_deg.getText() == null || txtLatitude1_deg.getText().length() == 0 ||
            txtLongitude1_deg.getText() == null || txtLongitude1_deg.getText().length() == 0)
        {
            txtLatitude1_deg.setText(Double.toString(position2.getLatitude().getDoubleValue(Angle.DEGREES)));
            txtLongitude1_deg.setText(Double.toString(position2.getLongitude().getDoubleValue(Angle.DEGREES)));
        }

        txtLatitude2_deg.setText(Double.toString(position2.getLatitude().getDoubleValue(Angle.DEGREES)));
        txtLongitude2_deg.setText(Double.toString(position2.getLongitude().getDoubleValue(Angle.DEGREES)));

        m_canvas.setProposedSafetyBox(new Latitude(Double.parseDouble(txtLatitude1_deg.getText()), Angle.DEGREES),
                                      new Latitude(Double.parseDouble(txtLatitude2_deg.getText()), Angle.DEGREES),
                                      new Longitude(Double.parseDouble(txtLongitude1_deg.getText()), Angle.DEGREES),
                                      new Longitude(Double.parseDouble(txtLongitude2_deg.getText()), Angle.DEGREES));
    }

    public void drawSafetyBoxMouseUp()
    {
        btnDrawNewBox.setText("Draw New Box");
        btnDrawNewBox.setBackground(btnSend.getBackground());
        m_canvas.setInDrawSafetyBoxMode(false);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtMinAlt = new javax.swing.JTextField();
        txtMaxAlt = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        minAlt_units = new javax.swing.JComboBox();
        maxAlt_units = new javax.swing.JComboBox();
        minAlt_type = new javax.swing.JComboBox();
        maxAlt_type = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtMinRadius = new javax.swing.JTextField();
        txtMaxRadius = new javax.swing.JTextField();
        minRadius_units = new javax.swing.JComboBox();
        maxRadius_units = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtLatitude1_deg = new javax.swing.JTextField();
        txtLongitude1_deg = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtLatitude2_deg = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtLongitude2_deg = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        btnDrawNewBox = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        chkMakePermanent = new javax.swing.JCheckBox();
        chkAllowEditing = new javax.swing.JCheckBox();

        setAutoscrolls(true);
        setInheritsPopupMenu(true);
        setMaximumSize(new java.awt.Dimension(300, 800));
        setMinimumSize(new java.awt.Dimension(250, 800));
        setPreferredSize(new java.awt.Dimension(270, 800));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("WAYPOINT BOUNDING BOX");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Altitude");

        jLabel9.setText("Min:");

        txtMinAlt.setColumns(13);
        txtMinAlt.setEnabled(false);
        txtMinAlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMinAltActionPerformed(evt);
            }
        });

        txtMaxAlt.setColumns(13);
        txtMaxAlt.setEnabled(false);
        txtMaxAlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaxAltActionPerformed(evt);
            }
        });

        jLabel14.setText("Max:");

        minAlt_units.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        minAlt_units.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minAlt_unitsActionPerformed(evt);
            }
        });

        maxAlt_units.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        maxAlt_units.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxAlt_unitsActionPerformed(evt);
            }
        });

        minAlt_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        minAlt_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minAlt_typeActionPerformed(evt);
            }
        });

        maxAlt_type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        maxAlt_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxAlt_typeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxAlt_type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(minAlt_type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtMaxAlt, 0, 0, Short.MAX_VALUE)
                            .addComponent(txtMinAlt, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxAlt_units, 0, 0, Short.MAX_VALUE)
                            .addComponent(minAlt_units, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMinAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(minAlt_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMaxAlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)
                            .addComponent(maxAlt_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(minAlt_units, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxAlt_units, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Radius");

        jLabel11.setText("Min:");

        jLabel16.setText("Max:");

        txtMinRadius.setColumns(13);
        txtMinRadius.setEnabled(false);
        txtMinRadius.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMinRadiusActionPerformed(evt);
            }
        });

        txtMaxRadius.setColumns(13);
        txtMaxRadius.setEnabled(false);
        txtMaxRadius.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaxRadiusActionPerformed(evt);
            }
        });

        minRadius_units.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        minRadius_units.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minRadius_unitsActionPerformed(evt);
            }
        });

        maxRadius_units.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        maxRadius_units.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxRadius_unitsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtMaxRadius, 0, 0, Short.MAX_VALUE)
                    .addComponent(txtMinRadius, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(minRadius_units, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxRadius_units, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtMinRadius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minRadius_units, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaxRadius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(maxRadius_units, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Position");

        jLabel12.setText("Latitude 1 (deg):");

        txtLatitude1_deg.setColumns(13);
        txtLatitude1_deg.setEnabled(false);
        txtLatitude1_deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLatitude1_degActionPerformed(evt);
            }
        });
        txtLatitude1_deg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtLatitude1_degKeyTyped(evt);
            }
        });

        txtLongitude1_deg.setColumns(13);
        txtLongitude1_deg.setEnabled(false);
        txtLongitude1_deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLongitude1_degActionPerformed(evt);
            }
        });
        txtLongitude1_deg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtLongitude1_degKeyTyped(evt);
            }
        });

        jLabel13.setText("Latitude 2 (deg):");

        txtLatitude2_deg.setColumns(13);
        txtLatitude2_deg.setEnabled(false);
        txtLatitude2_deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLatitude2_degActionPerformed(evt);
            }
        });
        txtLatitude2_deg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtLatitude2_degKeyTyped(evt);
            }
        });

        jLabel17.setText("Longitude 2 (deg):");

        txtLongitude2_deg.setColumns(13);
        txtLongitude2_deg.setEnabled(false);
        txtLongitude2_deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLongitude2_degActionPerformed(evt);
            }
        });

        jLabel18.setText("Longitude 1 (deg):");

        btnDrawNewBox.setText("Draw New Box");
        btnDrawNewBox.setEnabled(false);
        btnDrawNewBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDrawNewBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDrawNewBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel17))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtLongitude2_deg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtLatitude2_deg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtLatitude1_deg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtLongitude1_deg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                        .addGap(21, 21, 21))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtLatitude1_deg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtLongitude1_deg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtLatitude2_deg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtLongitude2_deg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDrawNewBox)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        btnSend.setText("Send");
        btnSend.setEnabled(false);
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        chkMakePermanent.setText("Make Permanent");
        chkMakePermanent.setEnabled(false);
        chkMakePermanent.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        chkAllowEditing.setText("Allow Editing");
        chkAllowEditing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllowEditingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addContainerGap(90, Short.MAX_VALUE))
            .addComponent(chkMakePermanent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(chkAllowEditing)
                .addContainerGap(89, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chkAllowEditing)
                .addGap(9, 9, 9)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chkMakePermanent)
                .addGap(18, 18, 18)
                .addComponent(btnSend)
                .addContainerGap(236, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtMinAltActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtMinAltActionPerformed
    {//GEN-HEADEREND:event_txtMinAltActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtMinAltActionPerformed

    private void txtMaxAltActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtMaxAltActionPerformed
    {//GEN-HEADEREND:event_txtMaxAltActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtMaxAltActionPerformed

    private void txtMinRadiusActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtMinRadiusActionPerformed
    {//GEN-HEADEREND:event_txtMinRadiusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMinRadiusActionPerformed

    private void txtMaxRadiusActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtMaxRadiusActionPerformed
    {//GEN-HEADEREND:event_txtMaxRadiusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaxRadiusActionPerformed

    private void txtLatitude1_degActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtLatitude1_degActionPerformed
    {//GEN-HEADEREND:event_txtLatitude1_degActionPerformed

    }//GEN-LAST:event_txtLatitude1_degActionPerformed

    private void txtLongitude1_degActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtLongitude1_degActionPerformed
    {//GEN-HEADEREND:event_txtLongitude1_degActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLongitude1_degActionPerformed

    private void txtLatitude2_degActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtLatitude2_degActionPerformed
    {//GEN-HEADEREND:event_txtLatitude2_degActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLatitude2_degActionPerformed

    private void txtLongitude2_degActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtLongitude2_degActionPerformed
    {//GEN-HEADEREND:event_txtLongitude2_degActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLongitude2_degActionPerformed

    private void btnDrawNewBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDrawNewBoxActionPerformed
    {//GEN-HEADEREND:event_btnDrawNewBoxActionPerformed
        if (btnDrawNewBox.getText().equals("Draw New Box"))
        {
            m_canvas.setInDrawSafetyBoxMode(true);
            btnDrawNewBox.setBackground(new Color(1.0f, 0.7f, 0.1f));
            btnDrawNewBox.setText("Cancel Drawing");
        }
        else
        {
            m_canvas.setInDrawSafetyBoxMode(false);
            btnDrawNewBox.setBackground(btnSend.getBackground());
            btnDrawNewBox.setText("Draw New Box");
        }
    }//GEN-LAST:event_btnDrawNewBoxActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnSendActionPerformed
    {//GEN-HEADEREND:event_btnSendActionPerformed
        try
        {
            double lat1 = Double.parseDouble(txtLatitude1_deg.getText());
            double lon1 = Double.parseDouble(txtLongitude1_deg.getText());
            double lat2 = Double.parseDouble(txtLatitude2_deg.getText());
            double lon2 = Double.parseDouble(txtLongitude2_deg.getText());
            double maxAlt_m = 0;
            double minAlt_m = 0;
            boolean maxAlt_IsAGL = false;
            boolean minAlt_IsAGL = false;
            
            maxAlt_m = Double.parseDouble(txtMaxAlt.getText())*(maxAlt_units.getSelectedItem().toString().equals("ft")?0.3048:1);
            minAlt_m = Double.parseDouble(txtMinAlt.getText())*(minAlt_units.getSelectedItem().toString().equals("ft")?0.3048:1);
            maxAlt_IsAGL = (maxAlt_type.getSelectedItem() == "AGL");
            minAlt_IsAGL = (minAlt_type.getSelectedItem() == "AGL");
            
            double maxRad_m = Double.parseDouble(txtMaxRadius.getText())*(maxRadius_units.getSelectedItem().toString().equals("ft")?0.3048:1);
            double minRad_m = Double.parseDouble(txtMinRadius.getText())*(minRadius_units.getSelectedItem().toString().equals("ft")?0.3048:1);


            SafetyBoxBelief safetyBoxBelief = new SafetyBoxBelief(WACSDisplayAgent.AGENTNAME,
                                                                  lat1,
                                                                  lon1,
                                                                  lat2,
                                                                  lon2,
                                                                  maxAlt_m,
                                                                  maxAlt_IsAGL,
                                                                  minAlt_m,
                                                                  minAlt_IsAGL,
                                                                  maxRad_m,
                                                                  minRad_m,
                                                                  chkMakePermanent.isSelected());
            m_beliefManager.put(safetyBoxBelief);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Invalid safety box values");
        }
    }//GEN-LAST:event_btnSendActionPerformed

    private void txtLatitude1_degKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_txtLatitude1_degKeyTyped
    {//GEN-HEADEREND:event_txtLatitude1_degKeyTyped
        try
        {
            m_canvas.setProposedSafetyBox(new Latitude(Double.parseDouble(txtLatitude1_deg.getText()), Angle.DEGREES),
                                          new Latitude(Double.parseDouble(txtLatitude2_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude1_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude2_deg.getText()), Angle.DEGREES));
        }
        catch (Exception e)
        {
        }
    }//GEN-LAST:event_txtLatitude1_degKeyTyped

    private void txtLongitude1_degKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_txtLongitude1_degKeyTyped
    {//GEN-HEADEREND:event_txtLongitude1_degKeyTyped
        try
        {
            m_canvas.setProposedSafetyBox(new Latitude(Double.parseDouble(txtLatitude1_deg.getText()), Angle.DEGREES),
                                          new Latitude(Double.parseDouble(txtLatitude2_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude1_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude2_deg.getText()), Angle.DEGREES));
        }
        catch (Exception e)
        {
        }
    }//GEN-LAST:event_txtLongitude1_degKeyTyped

    private void txtLatitude2_degKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_txtLatitude2_degKeyTyped
    {//GEN-HEADEREND:event_txtLatitude2_degKeyTyped
        try
        {
            m_canvas.setProposedSafetyBox(new Latitude(Double.parseDouble(txtLatitude1_deg.getText()), Angle.DEGREES),
                                          new Latitude(Double.parseDouble(txtLatitude2_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude1_deg.getText()), Angle.DEGREES),
                                          new Longitude(Double.parseDouble(txtLongitude2_deg.getText()), Angle.DEGREES));
        }
        catch (Exception e)
        {
        }
    }//GEN-LAST:event_txtLatitude2_degKeyTyped

    private void chkAllowEditingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chkAllowEditingActionPerformed
    {//GEN-HEADEREND:event_chkAllowEditingActionPerformed
        if (chkAllowEditing.isSelected())
        {
            txtLatitude1_deg.setEnabled(true);
            txtLatitude2_deg.setEnabled(true);
            txtLongitude1_deg.setEnabled(true);
            txtLongitude2_deg.setEnabled(true);
            txtMaxAlt.setEnabled(true);
            txtMinAlt.setEnabled(true);
            maxAlt_type.setEnabled(true);
            minAlt_type.setEnabled(true);
            
            txtMinRadius.setEnabled(true);
            txtMaxRadius.setEnabled(true);
            btnDrawNewBox.setEnabled(true);
            btnSend.setEnabled(true);
            chkMakePermanent.setEnabled(true);
        }
        else
        {
            txtLatitude1_deg.setEnabled(false);
            txtLatitude2_deg.setEnabled(false);
            txtLongitude1_deg.setEnabled(false);
            txtLongitude2_deg.setEnabled(false);
            txtMaxAlt.setEnabled(false);
            txtMinAlt.setEnabled(false);
            maxAlt_type.setEnabled(false);
            minAlt_type.setEnabled(false);
            txtMinRadius.setEnabled(false);
            txtMaxRadius.setEnabled(false);
            
            btnDrawNewBox.setEnabled(false);
            btnSend.setEnabled(false);
            chkMakePermanent.setEnabled(false);
            
            btnDrawNewBox.setBackground(btnSend.getBackground());
            btnDrawNewBox.setText("Draw New Box");
            m_canvas.setInDrawSafetyBoxMode(false);

            m_canvas.setProposedSafetyBox(null, null, null, null);
            if (m_safetyBoxBelief != null)
            {
                setSafetyBoxBelief(m_safetyBoxBelief);
            }
        }
    }//GEN-LAST:event_chkAllowEditingActionPerformed

    private void minAlt_unitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minAlt_unitsActionPerformed
        // TODO add your handling code here:
        m_UnitsChanged = true;
    }//GEN-LAST:event_minAlt_unitsActionPerformed

    private void maxAlt_unitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxAlt_unitsActionPerformed
        // TODO add your handling code here:
        m_UnitsChanged = true;
    }//GEN-LAST:event_maxAlt_unitsActionPerformed

    private void minRadius_unitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minRadius_unitsActionPerformed
        // TODO add your handling code here:
        m_UnitsChanged = true;
    }//GEN-LAST:event_minRadius_unitsActionPerformed

    private void maxRadius_unitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxRadius_unitsActionPerformed
        // TODO add your handling code here:
        m_UnitsChanged = true;
    }//GEN-LAST:event_maxRadius_unitsActionPerformed

private void minAlt_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minAlt_typeActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_minAlt_typeActionPerformed

private void maxAlt_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxAlt_typeActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_maxAlt_typeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDrawNewBox;
    private javax.swing.JButton btnSend;
    private javax.swing.JCheckBox chkAllowEditing;
    private javax.swing.JCheckBox chkMakePermanent;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox maxAlt_type;
    private javax.swing.JComboBox maxAlt_units;
    private javax.swing.JComboBox maxRadius_units;
    private javax.swing.JComboBox minAlt_type;
    private javax.swing.JComboBox minAlt_units;
    private javax.swing.JComboBox minRadius_units;
    private javax.swing.JTextField txtLatitude1_deg;
    private javax.swing.JTextField txtLatitude2_deg;
    private javax.swing.JTextField txtLongitude1_deg;
    private javax.swing.JTextField txtLongitude2_deg;
    private javax.swing.JTextField txtMaxAlt;
    private javax.swing.JTextField txtMaxRadius;
    private javax.swing.JTextField txtMinAlt;
    private javax.swing.JTextField txtMinRadius;
    // End of variables declaration//GEN-END:variables

}
