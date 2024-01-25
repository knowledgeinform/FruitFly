/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.SwingUtilities;

/**
 *
 * @author humphjc1
 */
public class AglDisplay extends javax.swing.JPanel implements Updateable, DisplayUnitsManager.DisplayUnitsChangeListener
{
    private BeliefManager _belMgr;
    private int _units = SearchCanvas.METERS;
    Thread thread = null;
    DecimalFormat format1D = new DecimalFormat ("#.#");

    double m_WarningAgl_m;
    Color m_WarningBkgdColor;
    double m_AlarmAgl_m;
    Color m_AlarmBkgdColor;
    Color m_ClearBkgdColor;
    
    
    /**
     * Creates new form AglDisplayPanel
     */
    public AglDisplay(BeliefManager mgr)
    {
        initComponents();

        _belMgr = mgr;
        DisplayUnitsManager.addChangeListener(this);
        unitsChanged();
        

        m_WarningAgl_m = 0.3048*Config.getConfig().getPropertyAsDouble("AglDisplay.WarningAgl.ft", 700);
        m_AlarmAgl_m = 0.3048*Config.getConfig().getPropertyAsDouble("AglDisplay.AlarmAgl.ft", 620);
        m_WarningBkgdColor = new Color (Config.getConfig().getPropertyAsInteger("AglDisplay.WarningColor.RGB", 16777011));
        m_AlarmBkgdColor = new Color (Config.getConfig().getPropertyAsInteger("AglDisplay.AlarmColor.RGB", 16724787));
        m_ClearBkgdColor = new Color (Config.getConfig().getPropertyAsInteger("AglDisplay.ClearColor.RGB", 15526360));


        thread = new Thread()
        {
            public void run ()
            {
                while (true)
                {
                    try
                    {
                        update();
                        Thread.sleep (500);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public void update() 
    {
        try
        {
            AgentPositionBelief uavPosBlf = (AgentPositionBelief)_belMgr.get(AgentPositionBelief.BELIEF_NAME);
            String mslText = "?", dtedText = "?", aglText = "?";
            Color aglBkgndColor = m_ClearBkgdColor;
            if (uavPosBlf != null)
            {
                PositionTimeName ptn = uavPosBlf.getPositionTimeName(WACSAgent.AGENTNAME);
                if (ptn != null)
                {
                    LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();

                    final double alt_m = lla.getAltitude().getDoubleValue(Length.METERS);
                    final double lat = lla.getLatitude().getDoubleValue(Angle.DEGREES);
                    final double lon = lla.getLongitude().getDoubleValue(Angle.DEGREES);

                    if (_units == SearchCanvas.FEET)
                        mslText = (format1D.format(alt_m/0.3048) + " ft");
                    else //if(_units == SearchCanvas.METERS)
                        mslText = (format1D.format(alt_m) + " m");

                    double dted_m = DtedGlobalMap.getDted().getAltitudeMSL(lat, lon);
                    if (dted_m >= 0)
                    {
                        double agl_m = (alt_m - dted_m);

                        if (agl_m < m_WarningAgl_m)
                            aglBkgndColor = m_WarningBkgdColor;
                        if (agl_m < m_AlarmAgl_m)
                            aglBkgndColor = m_AlarmBkgdColor;

                        if (_units == SearchCanvas.FEET)
                        {
                            dtedText = (format1D.format(dted_m/0.3048) + " ft");
                            aglText = (format1D.format(agl_m/0.3048) + " ft");
                        }
                        else //if(_units == SearchCanvas.METERS)
                        {
                            dtedText = (format1D.format(dted_m) + " m");
                            aglText = (format1D.format(agl_m) + " m");
                        }

                    }
                }
            }

            final String finalMslText = mslText;
            final String finalDtedText = dtedText;
            final String finalAglText = aglText;
            final Color finalAglBkgndColor = aglBkgndColor;
            SwingUtilities.invokeLater(new Thread ()
            {
                public void run ()
                {
                    m_MslOutput.setText (finalMslText);
                    m_DtedOutput.setText (finalDtedText);
                    m_AglOutput.setText (finalAglText);
                    //m_AglOutput.setOpaque(true);
                    //m_AglOutput.setBackground(finalAglBkgndColor);
                }
            });



            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief) _belMgr.get(PiccoloTelemetryBelief.BELIEF_NAME);
            String laserAglText = "?";
            Color laserAglBkgndColor = m_ClearBkgdColor;
            if (picBlf != null)
            {
                if (picBlf.getPiccoloTelemetry() != null)
                {
                    if (picBlf.getPiccoloTelemetry().AltLaserValid)
                    {
                        if (picBlf.getPiccoloTelemetry().AltLaser_m < m_WarningAgl_m)
                            laserAglBkgndColor = m_WarningBkgdColor;
                        if (picBlf.getPiccoloTelemetry().AltLaser_m < m_AlarmAgl_m)
                            laserAglBkgndColor = m_AlarmBkgdColor;

                        if (_units == SearchCanvas.FEET)
                            laserAglText = (format1D.format(picBlf.getPiccoloTelemetry().AltLaser_m/0.3048) + " ft");
                        else //if(_units == SearchCanvas.METERS)
                            laserAglText = (format1D.format(picBlf.getPiccoloTelemetry().AltLaser_m) + " m");
                    }
                }
            }

            final String finalLaserAglText = laserAglText;
            final Color finalLaserAglBkgndColor = laserAglBkgndColor;
            SwingUtilities.invokeLater(new Thread ()
            {
                public void run ()
                {
                    m_LaserAglOutput.setText (finalLaserAglText);
                    //m_LaserAglOutput.setOpaque(true);
                    //m_LaserAglOutput.setBackground(finalLaserAglBkgndColor);
                }
            });

        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();

            m_MslOutput.setText ("?");
            m_DtedOutput.setText ("?");
            m_AglOutput.setText ("?");
            m_LaserAglOutput.setText ("?");
        }
    }

    public void setUnits(int units)
    {
        _units = units;
    }

    @Override
    public void unitsChanged()
    {
        if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_FEET)
            setUnits (SearchCanvas.FEET);
        else //if(DisplayUnitsManager.getInstance().getUnits(DisplayUnitsManager.ALTITUDE_UNITS) == DisplayUnitsManager.LENGTH_METERS)
            setUnits (SearchCanvas.METERS);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        m_AglOutput = new javax.swing.JLabel();
        m_DtedOutput = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        m_LaserAglOutput = new javax.swing.JLabel();
        m_MslOutput = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel7.setText("Laser AGL: ");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel6.setText("Est AGL: ");

        m_AglOutput.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        m_AglOutput.setText(" ");
        m_AglOutput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        m_DtedOutput.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        m_DtedOutput.setText(" ");
        m_DtedOutput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("DTED: ");

        m_LaserAglOutput.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        m_LaserAglOutput.setText(" ");
        m_LaserAglOutput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        m_MslOutput.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        m_MslOutput.setText(" ");
        m_MslOutput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("MSL: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_AglOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(m_MslOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                                    .addComponent(m_DtedOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_LaserAglOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(m_AglOutput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(m_MslOutput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(m_DtedOutput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(m_LaserAglOutput))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel m_AglOutput;
    private javax.swing.JLabel m_DtedOutput;
    private javax.swing.JLabel m_LaserAglOutput;
    private javax.swing.JLabel m_MslOutput;
    // End of variables declaration//GEN-END:variables
}
