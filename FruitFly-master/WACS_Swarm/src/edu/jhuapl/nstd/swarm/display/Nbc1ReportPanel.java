/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.nstd.math.CoordConversions.PositionStringFormatter;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.Nbc1ReportBelief;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author humphjc1
 */
public class Nbc1ReportPanel extends javax.swing.JPanel implements Updateable
{
    BeliefManager m_BeliefManager;
    
    private long m_LastUpdatedReportTimeMs;
    
    private Color m_BaseFlashColor = null;
    private Color m_BrightFlashColor = new Color (255, 0, 0);
    private long m_ColorFlashTimeMs = 15000;
    
    ConcurrentHashMap<String, Nbc1ReportBelief> m_AllRecentNbcReports = new ConcurrentHashMap<String, Nbc1ReportBelief> ();
    

    /**
     * Creates new form Nbc1ReportPanel
     */
    public Nbc1ReportPanel(BeliefManager belMgr) 
    {
        initComponents();
        
        m_BeliefManager = belMgr;
        m_LastUpdatedReportTimeMs = -1;
        m_BaseFlashColor = jLabel11.getBackground();
        jLabel11.setOpaque(true);
        jScrollPane1.setVisible (false);
    }
    
    @Override
    public void update ()
    {
        Nbc1ReportBelief belief = (Nbc1ReportBelief)m_BeliefManager.get(Nbc1ReportBelief.BELIEF_NAME);
        if (belief != null)
        {
            Nbc1ReportBelief beliefCopy = new Nbc1ReportBelief(belief);
            
            if (beliefCopy.getTimeStamp().getTime() > m_LastUpdatedReportTimeMs)
            {
                m_AllRecentNbcReports.put(beliefCopy.getReportNumber(), beliefCopy);
                jComboBox1.addItem(beliefCopy.getReportNumber());
                jComboBox1.setSelectedItem(beliefCopy.getReportNumber());
                
                setCurrentFields (beliefCopy);
                m_LastUpdatedReportTimeMs = beliefCopy.getTimeStamp().getTime();
            }
            
            //at least update time/color since last update
            long ageMs = Math.max(0, (System.currentTimeMillis() - beliefCopy.getTimeStamp().getTime()));
            long ageSec = ageMs/1000;
            if (ageSec < 60)
                jLabel11.setText("" + ageSec + " sec");
            else if (ageSec < 3600)
                jLabel11.setText("" + ageSec/60 + " min, " + (ageSec-(ageSec/60)*60) + " sec");
            else 
                jLabel11.setText("" + ageSec/3600 + " hr");
            
            if (ageMs > 0)
            {
                Color baseColor = m_BaseFlashColor;
                Color brightColor = m_BrightFlashColor;

                float percentBright = ((float)m_ColorFlashTimeMs - ageMs)/m_ColorFlashTimeMs;
                percentBright = Math.min (1, Math.max (0, percentBright));
                Color currColor = getCurrFlashColor (baseColor, brightColor, percentBright);
                if (!jLabel11.getBackground().equals(currColor))
                    jLabel11.setBackground(currColor);
            }
        }
    }
    
    private void setCurrentFields (Nbc1ReportBelief belief)
    {
        //The data in the NBC report is new, update the panels
        jScrollPane1.setVisible (true);
        jLabel14.setText (belief.getReportNumber());

        String positionText = "";
        if (false) //if (latlon)
        {
            double latDecDeg = belief.getDetectionLocation().getLatitude().getDoubleValue(Angle.DEGREES);
            boolean latNegative = (latDecDeg < 0);
            latDecDeg = Math.abs(latDecDeg);
            int latDegINT = (int)latDecDeg;
            double latDecMin = (latDecDeg-latDegINT)*60;
            int latMinINT = (int)(latDecMin);
            int latSecINT = (int)((latDecMin-latMinINT)*60);
            positionText += latDegINT + "-" + latMinINT + "-" + latSecINT + (latNegative?"S":"N") + " ";
            double lonDecDeg = belief.getDetectionLocation().getLongitude().getDoubleValue(Angle.DEGREES);
            boolean lonNegative = (lonDecDeg < 0);
            lonDecDeg = Math.abs(lonDecDeg);
            int lonDegINT = (int)lonDecDeg;
            double lonDecMin = (lonDecDeg-lonDegINT)*60;
            int lonMinINT = (int)(lonDecMin);
            int lonSecINT = (int)((lonDecMin-lonMinINT)*60);
            positionText += lonDegINT + "-" + lonMinINT + "-" + lonSecINT + (lonNegative?"W":"E");
        }
        else //if (mgrs)
        {
            double latDecDeg = belief.getDetectionLocation().getLatitude().getDoubleValue(Angle.DEGREES);
            double lonDecDeg = belief.getDetectionLocation().getLongitude().getDoubleValue(Angle.DEGREES);
            positionText = PositionStringFormatter.formatLatLonAsMGRS(latDecDeg, lonDecDeg);
        }
        
        jLabel2.setText (positionText);

        jLabel4.setText (MathUtils.getMilDateTimeGroup(belief.getDetectionTime().getTime()));
        jLabel6.setText (belief.getDetectionString());

        double windBearingFromDeg = belief.getWindBearingFrom().getDoubleValue(Angle.DEGREES);
        double windSpeedKts = belief.getWindSpeed().getDoubleValue(Speed.KNOTS);
        jLabel8.setText ((int)windBearingFromDeg + " deg " + ((int)Math.round(windSpeedKts)) + " kts");
    }
    
    private Color getCurrFlashColor (Color baseColor, Color brightColor, float percentBright)
    {
        return new Color (Math.min(255, Math.max (0, (int)(baseColor.getRed()*(1-percentBright) + brightColor.getRed()*(percentBright)))),
                    Math.min(255, Math.max (0, (int)(baseColor.getGreen()*(1-percentBright) + brightColor.getGreen()*(percentBright)))),
                    Math.min(255, Math.max (0, (int)(baseColor.getBlue()*(1-percentBright) + brightColor.getBlue()*(percentBright)))));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jComboBox1 = new javax.swing.JComboBox();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Detection Location: ");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Detection Date/Time: ");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Detection Type: ");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("Wind From/Speed: ");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel12.setText("Latest Report Age:");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel13.setText("Report Number: ");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel15.setText("Line ALPHA");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel17.setText("Line BRAVO");

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel18.setText("Line HOTEL");

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel19.setText("Line SIERRA");

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel20.setText("Line YANKEE");

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel21.setText("Line ZULU BRAVO");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setText("Remarks:");

        jScrollPane1.setBorder(null);

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Agent detected by the WMD Aerial Collection System (WACS) CBRN sensor mounted on a Shadow UAS following CWMD offensive operations engagement.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        String reportNumber = jComboBox1.getSelectedItem().toString();
        Nbc1ReportBelief belief = (Nbc1ReportBelief)m_AllRecentNbcReports.get(reportNumber);
        if (belief != null)
        {
            setCurrentFields (belief);
        }
        
    }//GEN-LAST:event_jComboBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
