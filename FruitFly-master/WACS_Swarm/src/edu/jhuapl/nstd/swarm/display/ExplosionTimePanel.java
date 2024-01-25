/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import com.toedter.calendar.JDateChooser;
import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;

/**
 *
 * @author humphjc1
 */
public class ExplosionTimePanel extends javax.swing.JPanel implements Updateable, MissionErrorManager.CommandedBeliefErrorListener
{
    BeliefManager m_BeliefManager;
    
    int m_Year = 0;
    int m_Month = 0;
    int m_Day = 0;
    int m_Hour = 0;
    int m_Minute = 0;
    int m_Second = 0;
    Date m_LastActualTimeUpdateTime = null;
    
    private int m_HoursOffset = -1000;
    boolean m_TimeSyncSet;
    
    JDateChooser m_DateChooser;
    SpinnerDateModel m_TimeModel;

    /**
     * Creates new form ExplosionTimePanel
     */
    public ExplosionTimePanel(BeliefManager belMgr) 
    {
        initComponents();
        m_BeliefManager = belMgr;
        
        update();
        
        addCalendarInput();
        
        MissionErrorManager.getInstance().registerErrorListener(this);
    }
    
    @Override
    public void handleCommandedBeliefError(MissionErrorManager.ErrorCodeBase errorCode, int alarmLevel) 
    {
        if (errorCode == MissionErrorManager.COMMANDACTUALSYNC_EXPLOSIONTIME_ERRORCODE)
        {
            if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
                jLabel1.setBackground(Color.RED);
            else if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
                jLabel1.setBackground(Color.ORANGE);
            else if (alarmLevel == MissionErrorManager.ALARMLEVEL_NOTIFICATION)
                jLabel1.setBackground(this.getBackground());
            else 
                jLabel1.setBackground(Color.GREEN);
        }
    }
    
    private void addCalendarInput()
    {
        //date chooser
        long currTimePlus30Min = System.currentTimeMillis() + 30*60*1000;
        Date currDatePlus30Min = new Date (currTimePlus30Min);
        m_DateChooser = new JDateChooser(currDatePlus30Min);
        m_DateChooser.setFont(new java.awt.Font("Tahoma", 0, 14));
        m_DateChooser.requestFocusInWindow();

        //time chooser
        m_TimeModel = new SpinnerDateModel(currDatePlus30Min, null, null, Calendar.YEAR);
        JSpinner timeSpinner = new JSpinner(m_TimeModel);
        JComponent timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setFont (new java.awt.Font("Tahoma", 0, 14));
        timeSpinner.setEditor(timeEditor);

        //layout
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.X_AXIS));
        leftSide.add(m_DateChooser);
        JPanel centerSide = new JPanel();
        centerSide.setLayout(new BoxLayout(centerSide, BoxLayout.X_AXIS));
        centerSide.add(timeSpinner);
        
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.X_AXIS));
        jPanel1.add (leftSide);
        jPanel1.add (centerSide);
        jPanel1.revalidate();
    }
    
    private int getUTCHoursOffset ()
    {
        Calendar cal = Calendar.getInstance(getLocale());
        return (int)(Math.round ((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 60 * 1000.0)));
    }
    
    private void setUTCHoursOffsetLabel (int hoursOffset)
    {
        jLabel5.setText ("(UTC" + (hoursOffset>=0?"+":"") + hoursOffset + ")");
        m_HoursOffset = hoursOffset;
    }
    
    public void update()
    {
        int hoursOffset = getUTCHoursOffset();
        if (hoursOffset != m_HoursOffset)
            setUTCHoursOffsetLabel (hoursOffset);
        
        ExplosionTimeActualBelief expBlf = (ExplosionTimeActualBelief)m_BeliefManager.get (ExplosionTimeActualBelief.BELIEF_NAME);
        if (expBlf != null && (m_LastActualTimeUpdateTime == null || expBlf.getTimeStamp().after(m_LastActualTimeUpdateTime)))
        {
            Date dt = new Date (expBlf.getTime_ms());
            m_Year = dt.getYear() + 1900;
            m_Month = dt.getMonth() + 1;
            m_Day = dt.getDate();
            m_Hour = dt.getHours();
            m_Minute = dt.getMinutes();
            m_Second = dt.getSeconds();
            
            jLabel1.setText (m_Year + "/" + lead2DigitZeros(m_Month) + "/" + lead2DigitZeros(m_Day) + " " + lead2DigitZeros(m_Hour) + ":" + lead2DigitZeros(m_Minute) + ":" + lead2DigitZeros(m_Second));
            m_LastActualTimeUpdateTime = expBlf.getTimeStamp();
        }
        
    }
    
    private String lead2DigitZeros (int val)
    {
        return String.format ("%02d", val);
    }
    
    private void setTimeBoxes ()
    {
        ExplosionTimeCommandedBelief expBlf = (ExplosionTimeCommandedBelief)m_BeliefManager.get (ExplosionTimeCommandedBelief.BELIEF_NAME);
        setTimeBoxes(expBlf);
    }

    private void setTimeBoxes (ExplosionTimeCommandedBelief expBlf)
    {
        if (expBlf != null)
        {
            Date dt = new Date (expBlf.getTime_ms());
            
            if (m_DateChooser != null)
                m_DateChooser.setDate(dt);
            if (m_TimeModel != null)
            {
                m_TimeModel.setValue (dt);
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

        jButton7 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_SetButton = new javax.swing.JButton();

        jButton7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton7.setText("NOW");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton5.setText("T-30 SECONDS");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton6.setText("T-1 HOUR");
        jButton6.setMinimumSize(new java.awt.Dimension(121, 25));
        jButton6.setPreferredSize(new java.awt.Dimension(121, 25));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton3.setText("T-5 MINUTES");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton4.setText("T-1 MINUTE");
        jButton4.setMinimumSize(new java.awt.Dimension(121, 25));
        jButton4.setPreferredSize(new java.awt.Dimension(121, 25));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setText("T-30 MINUTES");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton2.setText("T-10 MINUTES");
        jButton2.setMinimumSize(new java.awt.Dimension(121, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(121, 25));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("Expected Strike Time:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("NOT SET");
        jLabel1.setOpaque(true);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel8.setText("Set Strike Time:");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("(UTC+12)");

        m_SetButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        m_SetButton.setText("SET");
        m_SetButton.setPreferredSize(new java.awt.Dimension(121, 25));
        m_SetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_SetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_SetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_SetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(0, 151, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis());
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1000*30);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1*60*60*1000);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1000*5*60);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1000*1*60);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1000*30*60);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, System.currentTimeMillis() + 1000*10*60);
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void m_SetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_SetButtonActionPerformed
        // TODO add your handling code here:
        Date date = m_DateChooser.getDate();
        Date time = m_TimeModel.getDate();
        date.setHours(time.getHours());
        date.setMinutes(time.getMinutes());
        date.setSeconds(time.getSeconds());
                
        ExplosionTimeCommandedBelief timeBlf = new ExplosionTimeCommandedBelief (WACSAgent.AGENTNAME, date.getTime());
        m_BeliefManager.put (timeBlf);
        setTimeBoxes(timeBlf);
    }//GEN-LAST:event_m_SetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton m_SetButton;
    // End of variables declaration//GEN-END:variables
}
