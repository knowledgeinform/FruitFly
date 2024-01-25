/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author humphjc1
 */
public class DisplayUnitsManager extends javax.swing.JPanel 
{
    public interface DisplayUnitsChangeListener
    {
        public void unitsChanged ();
    }

    public final static int ALTITUDE_UNITS = 0;
    public final static int RANGE_UNITS = 1;
    public final static int WINDSPEED_UNITS = 2;
    public final static int AIRSPEED_UNITS = 3;
    public final static int POSITION_UNITS = 4;
    public final static int MAX_UNITS_IN_LIST = 10;

    public final static int LENGTH_METERS = 0;
    public final static int LENGTH_FEET = 1;

    public final static int SPEED_METERSPERSEC = 0;
    public final static int SPEED_MILESPERHOUR = 1;
    public final static int SPEED_KNOTS = 2;
    
    public final static int POSITION_DD = 0;
    public final static int POSITION_DM = 1;
    public final static int POSITION_DMS = 2;
    public final static int POSITION_MGRS = 3;
    

    private static DisplayUnitsManager m_UnitsManager = null;
    private static final ArrayList <DisplayUnitsChangeListener> m_UnitChangeListeners = new ArrayList <DisplayUnitsChangeListener> ();

    private int m_UnitsList [];
    
    /**
     * Preferences package in window registry.  
     */
    protected Preferences m_Prefs = Preferences.userNodeForPackage(edu.jhuapl.nstd.swarm.display.DisplayUnitsManager.class);
    

    public static DisplayUnitsManager getInstance()
    {
        if (m_UnitsManager == null)
        {
            m_UnitsManager = new DisplayUnitsManager();
            m_UnitsManager.setDefaults();
        }

        return m_UnitsManager;
    }

    public void setUnits (int unitType, int chosenUnits)
    {
        if (unitType < MAX_UNITS_IN_LIST && unitType >= 0)
        {
            m_UnitsList[unitType] = chosenUnits;
            notifyListeners ();
        }
    }

    public int getUnits (int unitType)
    {
        if (unitType < MAX_UNITS_IN_LIST && unitType >= 0)
        {
            return m_UnitsList[unitType];
        }
        return -1;
    }

    public static void addChangeListener (DisplayUnitsChangeListener listener)
    {
        synchronized (m_UnitChangeListeners)
        {
            m_UnitChangeListeners.add(listener);
        }
    }

    public static void removeChangeListener (DisplayUnitsChangeListener listener)
    {
        synchronized (m_UnitChangeListeners)
        {
            m_UnitChangeListeners.remove(listener);
        }
    }

    public void notifyListeners ()
    {
        synchronized (m_UnitChangeListeners)
        {
            for (DisplayUnitsChangeListener itr : m_UnitChangeListeners)
            {
                itr.unitsChanged();
            }
        }
    }
    
    
    /**
     * Creates new form DisplayUnitsManagerPanel
     */
    public DisplayUnitsManager() 
    {    
        initComponents();

        m_UnitsList = new int [MAX_UNITS_IN_LIST];

        m_AltitudeUnitsInput.setModel(new DefaultComboBoxModel (new Object [] {"m", "ft"}));
        
        m_RangeUnitsInput.setModel(new DefaultComboBoxModel (new Object [] {"m", "ft"}));
        
        m_WindSpeedUnitsInput.setModel(new DefaultComboBoxModel (new Object [] {"m/s", "mph", "kts"}));
        
        m_AircraftSpeedUnitsInput.setModel(new DefaultComboBoxModel (new Object [] {"m/s", "mph", "kts"}));
        
        m_PositionUnitsInput.setModel (new DefaultComboBoxModel (new Object[] {"DD", "DM", "DMS", "MGRS"}));
    }

    private void setDefaults ()
    {
        String altUnits = m_Prefs.get ("DisplayUnitsManager.Default.Altitude", "ft");
        if (altUnits.equals ("ft"))
            m_AltitudeUnitsInput.setSelectedIndex(1);
        else //if (altUnits.equals ("m"))
            m_AltitudeUnitsInput.setSelectedIndex(0);

        String rngUnits = m_Prefs.get ("DisplayUnitsManager.Default.Range", "m");
        if (rngUnits.equals ("ft"))
            m_RangeUnitsInput.setSelectedIndex(1);
        else //if (rngUnits.equals ("m"))
            m_RangeUnitsInput.setSelectedIndex(0);

        String wspdUnits = m_Prefs.get ("DisplayUnitsManager.Default.WindSpeed", "kts");
        if (wspdUnits.equals ("kts"))
            m_WindSpeedUnitsInput.setSelectedIndex(2);
        else if(wspdUnits.equals("mph"))
            m_WindSpeedUnitsInput.setSelectedIndex(1);
        else //if (wspdUnits.equals ("m/s"))
            m_WindSpeedUnitsInput.setSelectedIndex(0);

        String aspdUnits = m_Prefs.get ("DisplayUnitsManager.Default.AirSpeed", "kts");
        if (aspdUnits.equals ("kts"))
            m_AircraftSpeedUnitsInput.setSelectedIndex(2);
        else if(aspdUnits.equals("mph"))
            m_AircraftSpeedUnitsInput.setSelectedIndex(1);
        else //if (aspdUnits.equals ("m/s"))
            m_AircraftSpeedUnitsInput.setSelectedIndex(0);
        
        String positionUnits = m_Prefs.get ("DisplayUnitsManager.Default.Position", "DD");
        if (positionUnits.equals ("MGRS"))
            m_PositionUnitsInput.setSelectedIndex(3);
        else if(positionUnits.equals("DMS"))
            m_PositionUnitsInput.setSelectedIndex(2);
        else if(positionUnits.equals("DM"))
            m_PositionUnitsInput.setSelectedIndex(1);
        else //if (aspdUnits.equals ("DD"))
            m_PositionUnitsInput.setSelectedIndex(0);
    }

    public void showDialog ()
    {
        javax.swing.JFrame testFrame = new javax.swing.JFrame();
        testFrame.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        testFrame.setLocation(200, 200);
        testFrame.add(this);
        testFrame.pack();
        testFrame.setVisible(true);   
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        m_RangeUnitsInput = new javax.swing.JComboBox();
        m_AltitudeUnitsInput = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        m_AircraftSpeedUnitsInput = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        m_WindSpeedUnitsInput = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        m_PositionUnitsInput = new javax.swing.JComboBox();

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Range Units:");

        m_RangeUnitsInput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_RangeUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_RangeUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_RangeUnitsInputActionPerformed(evt);
            }
        });

        m_AltitudeUnitsInput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_AltitudeUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_AltitudeUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AltitudeUnitsInputActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Aircraft Speed Units:");

        m_AircraftSpeedUnitsInput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_AircraftSpeedUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_AircraftSpeedUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AircraftSpeedUnitsInputActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Altitude Units:");

        m_WindSpeedUnitsInput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_WindSpeedUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_WindSpeedUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_WindSpeedUnitsInputActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Wind Speed Units:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Position Units:");

        m_PositionUnitsInput.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_PositionUnitsInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_PositionUnitsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_PositionUnitsInputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_RangeUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AltitudeUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_WindSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_AircraftSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_PositionUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(m_AltitudeUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(m_RangeUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(m_WindSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(m_AircraftSpeedUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(m_PositionUnitsInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_RangeUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_RangeUnitsInputActionPerformed
        // TODO add your handling code here:
        String text = m_RangeUnitsInput.getSelectedItem().toString();
        if (text.equals("m"))
            setUnits (RANGE_UNITS, LENGTH_METERS);
        else if(text.equals("ft"))
            setUnits (RANGE_UNITS, LENGTH_FEET);
        
        m_Prefs.put ("DisplayUnitsManager.Default.Range", text);
    }//GEN-LAST:event_m_RangeUnitsInputActionPerformed

    private void m_AltitudeUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AltitudeUnitsInputActionPerformed
        // TODO add your handling code here:
        String text = m_AltitudeUnitsInput.getSelectedItem().toString();
        if (text.equals("m"))
            setUnits (ALTITUDE_UNITS, LENGTH_METERS);
        else if(text.equals("ft"))
            setUnits (ALTITUDE_UNITS, LENGTH_FEET);
        
        m_Prefs.put ("DisplayUnitsManager.Default.Altitude", text);
    }//GEN-LAST:event_m_AltitudeUnitsInputActionPerformed

    private void m_AircraftSpeedUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AircraftSpeedUnitsInputActionPerformed
        // TODO add your handling code here:
        String text = m_AircraftSpeedUnitsInput.getSelectedItem().toString();
        if (text.equals("m/s"))
            setUnits (AIRSPEED_UNITS, SPEED_METERSPERSEC);
        else if(text.equals("mph"))
            setUnits (AIRSPEED_UNITS, SPEED_MILESPERHOUR);
        else if(text.equals("kts"))
            setUnits (AIRSPEED_UNITS, SPEED_KNOTS);
        
        m_Prefs.put ("DisplayUnitsManager.Default.AirSpeed", text);
    }//GEN-LAST:event_m_AircraftSpeedUnitsInputActionPerformed

    private void m_WindSpeedUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_WindSpeedUnitsInputActionPerformed
        // TODO add your handling code here:
        String text = m_WindSpeedUnitsInput.getSelectedItem().toString();
        if (text.equals("m/s"))
            setUnits (WINDSPEED_UNITS, SPEED_METERSPERSEC);
        else if(text.equals("mph"))
            setUnits (WINDSPEED_UNITS, SPEED_MILESPERHOUR);
        else if(text.equals("kts"))
            setUnits (WINDSPEED_UNITS, SPEED_KNOTS);
        
        m_Prefs.put ("DisplayUnitsManager.Default.WindSpeed", text);
    }//GEN-LAST:event_m_WindSpeedUnitsInputActionPerformed

    private void m_PositionUnitsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_PositionUnitsInputActionPerformed
        // TODO add your handling code here:
        String text = m_PositionUnitsInput.getSelectedItem().toString();
        if (text.equals("DD"))
            setUnits (POSITION_UNITS, POSITION_DD);
        else if(text.equals("DM"))
            setUnits (POSITION_UNITS, POSITION_DM);
        else if(text.equals("DMS"))
            setUnits (POSITION_UNITS, POSITION_DMS);
        else if(text.equals("MGRS"))
            setUnits (POSITION_UNITS, POSITION_MGRS);
        
        m_Prefs.put ("DisplayUnitsManager.Default.Position", text);
    }//GEN-LAST:event_m_PositionUnitsInputActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox m_AircraftSpeedUnitsInput;
    private javax.swing.JComboBox m_AltitudeUnitsInput;
    private javax.swing.JComboBox m_PositionUnitsInput;
    private javax.swing.JComboBox m_RangeUnitsInput;
    private javax.swing.JComboBox m_WindSpeedUnitsInput;
    // End of variables declaration//GEN-END:variables
}
