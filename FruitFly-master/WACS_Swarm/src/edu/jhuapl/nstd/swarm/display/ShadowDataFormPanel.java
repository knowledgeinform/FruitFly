/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface;
import java.awt.Graphics;

/**
 *
 * @author humphjc1
 */
public class ShadowDataFormPanel extends javax.swing.JPanel implements DisplayUnitsManager.DisplayUnitsChangeListener
{
    private double m_roll_rad = 0;
    private double m_altitudeMSL_m = 0;
    private double m_heading_rad = 0;
    private double m_latitude_rad = 0;
    private double m_longitude_rad = 0;
    private double m_airspeed_mps = 0;
    private double m_commandedRoll_rad = 0;
    private double m_commandedAltitude_m = 0;
    private double m_commandedAirspeed_mps = 0;
    private final Object m_telemtryLock = new Object();
    private KnobsAutopilotInterface m_shadowInterface;
    
    
    /**
     * Creates new form ShadowDataFormPanel
     */
    public ShadowDataFormPanel(KnobsAutopilotInterface interf) 
    {
        initComponents();
        
        m_shadowInterface = interf;
        (new ShadowDataFormPanel.UpdateDataThread()).start();

        DisplayUnitsManager.addChangeListener(this);
    }
    
    
    private void setPlaneData(double roll_rad,
                              double altitudeMSL_m,
                              double heading_rad,
                              double latitude_rad,
                              double longitude_rad,
                              double airspeed_mps,
                              double commandedRoll_rad,
                              double commandedAltitude_m,
                              double commandedAirspeed_mps)
    {
        synchronized (m_telemtryLock)
        {
            m_roll_rad = roll_rad;
            m_altitudeMSL_m = altitudeMSL_m;
            m_heading_rad = heading_rad;
            m_latitude_rad = latitude_rad;
            m_longitude_rad = longitude_rad;
            m_airspeed_mps = airspeed_mps;
            m_commandedRoll_rad = commandedRoll_rad;
            m_commandedAltitude_m = commandedAltitude_m;
            m_commandedAirspeed_mps = commandedAirspeed_mps;
        }

        pnlVisualizedRoll.setRoll(roll_rad, commandedRoll_rad);

        repaint();
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);       

        synchronized (m_telemtryLock)
        {
            lblRoll.setText(String.format("%.2f deg", Math.toDegrees(m_roll_rad)));
            
            if (DisplayUnitsManager.getInstance().getUnits (DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                lblAltitude.setText(String.format("%.2f ft", m_altitudeMSL_m/0.3048));
            else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                lblAltitude.setText(String.format("%.2f m", m_altitudeMSL_m));

            lblHeading.setText(String.format("%.2f deg", Math.toDegrees(m_heading_rad)));
            lblLatitude.setText(String.format("%f deg", Math.toDegrees(m_latitude_rad)));
            lblLongitude.setText(String.format("%f deg", Math.toDegrees(m_longitude_rad)));

            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_MILESPERHOUR)
                lblAirspeed.setText(String.format("%.2f mph", m_airspeed_mps*2.236936));
             else if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_KNOTS)
                lblAirspeed.setText(String.format("%.2f kts", m_airspeed_mps*1.943844));
             else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_METERSPERSEC)
                lblAirspeed.setText(String.format("%.2f m/s", m_airspeed_mps));

            lblCommandedRoll.setText(String.format("%.2f deg", Math.toDegrees(m_commandedRoll_rad)));

            if (DisplayUnitsManager.getInstance().getUnits (DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
                lblCommandedAltitude.setText(String.format("%.2f ft", m_commandedAltitude_m/0.3048));
            else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
                lblCommandedAltitude.setText(String.format("%.2f m", m_commandedAltitude_m));

            if (DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_MILESPERHOUR)
                lblCommandedAirspeed.setText(String.format("%.2f mph", m_commandedAirspeed_mps*2.236936));
             else if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_KNOTS)
                lblCommandedAirspeed.setText(String.format("%.2f kts", m_commandedAirspeed_mps*1.943844));
             else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.AIRSPEED_UNITS) == DisplayUnitsManager.SPEED_METERSPERSEC)
                lblCommandedAirspeed.setText(String.format("%.2f m/s", m_commandedAirspeed_mps));
        }
    }

    @Override
    public void unitsChanged()
    {
        this.repaint();
    }

    private class UpdateDataThread extends Thread
    {
        @Override
        public void run()
        {
            if (m_shadowInterface == null)
                return;
            
            KnobsAutopilotInterface.TelemetryMessage telemetryMessage = m_shadowInterface.getBlankTelemetryMessage();
            KnobsAutopilotInterface.CommandMessage commandMessage = m_shadowInterface.getBlankCommandMessage();

            while (true)
            {
                m_shadowInterface.copyLatestTelemetryMessage(telemetryMessage);
                m_shadowInterface.copyLatestCommandMessage(commandMessage);
                setPlaneData(telemetryMessage.roll_rad,
                             telemetryMessage.barometricAltitudeMsl_m,
                             telemetryMessage.trueHeading_rad,
                             telemetryMessage.latitude_rad,
                             telemetryMessage.longitude_rad,
                             telemetryMessage.indicatedAirspeed_mps,
                             commandMessage.rollCommand_rad,
                             commandMessage.altitudeCommand_m,
                             commandMessage.airspeedCommand_mps);

                try
                {
                    Thread.sleep(250);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
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

        pnlVisualizedRoll = new edu.jhuapl.nstd.swarm.autopilot.VisualizedRollPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblLatitude = new javax.swing.JLabel();
        lblLongitude = new javax.swing.JLabel();
        lblHeading = new javax.swing.JLabel();
        lblAltitude = new javax.swing.JLabel();
        lblRoll = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblAirspeed = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblCommandedAltitude = new javax.swing.JLabel();
        lblCommandedRoll = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblCommandedAirspeed = new javax.swing.JLabel();

        pnlVisualizedRoll.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlVisualizedRollLayout = new javax.swing.GroupLayout(pnlVisualizedRoll);
        pnlVisualizedRoll.setLayout(pnlVisualizedRollLayout);
        pnlVisualizedRollLayout.setHorizontalGroup(
            pnlVisualizedRollLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 282, Short.MAX_VALUE)
        );
        pnlVisualizedRollLayout.setVerticalGroup(
            pnlVisualizedRollLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 180, Short.MAX_VALUE)
        );

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setText("Visualized Roll:");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel13.setText("Commanded:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Shadow Data");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("Actual:");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName(""); // NOI18N

        lblLatitude.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblLatitude.setText("0 deg");

        lblLongitude.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblLongitude.setText("0 deg");

        lblHeading.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblHeading.setText("0 deg");

        lblAltitude.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblAltitude.setText("0 m");

        lblRoll.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblRoll.setText("0 deg");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Altitude (MSL):");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Heading:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Roll:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setText("Longitude:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("Latitude:");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setText("Airspeed:");

        lblAirspeed.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblAirspeed.setText("0 m/s");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHeading)
                    .addComponent(lblAltitude)
                    .addComponent(lblRoll)
                    .addComponent(lblLatitude)
                    .addComponent(lblLongitude)
                    .addComponent(lblAirspeed))
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblRoll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblAltitude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblHeading))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblLatitude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblLongitude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lblAirspeed))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName(""); // NOI18N

        lblCommandedAltitude.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblCommandedAltitude.setText("0 m");

        lblCommandedRoll.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblCommandedRoll.setText("0 deg");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setText("Altitude (MSL):");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setText("Roll:");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setText("Airspeed:");

        lblCommandedAirspeed.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblCommandedAirspeed.setText("0 m/s");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCommandedAltitude)
                    .addComponent(lblCommandedRoll)
                    .addComponent(lblCommandedAirspeed))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(lblCommandedRoll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCommandedAltitude)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(lblCommandedAirspeed))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(pnlVisualizedRoll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 169, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(21, 21, 21)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(203, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(226, 226, 226))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jLabel13))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlVisualizedRoll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblAirspeed;
    private javax.swing.JLabel lblAltitude;
    private javax.swing.JLabel lblCommandedAirspeed;
    private javax.swing.JLabel lblCommandedAltitude;
    private javax.swing.JLabel lblCommandedRoll;
    private javax.swing.JLabel lblHeading;
    private javax.swing.JLabel lblLatitude;
    private javax.swing.JLabel lblLongitude;
    private javax.swing.JLabel lblRoll;
    private edu.jhuapl.nstd.swarm.autopilot.VisualizedRollPanel pnlVisualizedRoll;
    // End of variables declaration//GEN-END:variables
}
