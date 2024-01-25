/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.MissionErrorManager.ErrorCodeBase;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultHighlighter;

/**
 *
 * @author humphjc1
 */
public class MissionStatusErrorsPanel extends javax.swing.JPanel implements Updateable, MissionErrorManager.CommandedBeliefErrorListener, MissionErrorManager.TimedBeliefErrorListener
{

    BeliefManager m_BeliefManager;
    private long m_LastUpdateTimestampMs;
    private int m_HighestAlarm;
    
    private class TimedAlarmLevel
    {
        public int m_AlarmLevel;
        public long m_DelaySec;
        
        public TimedAlarmLevel (int alarmLevel, long delaySec)
        {
            m_AlarmLevel = alarmLevel;
            m_DelaySec = delaySec;
        }
    }
    
    ConcurrentHashMap<MissionErrorManager.ErrorCodeBase, Object> m_CurrentErrorCodes = new ConcurrentHashMap<MissionErrorManager.ErrorCodeBase, Object>();
    ConcurrentHashMap<MissionErrorManager.ErrorCodeBase, ErrorControlBar> m_CurrentErrorBars = new ConcurrentHashMap<MissionErrorManager.ErrorCodeBase, ErrorControlBar>();
    
    
    /**
     * Creates new form MissionStatusErrorsPanel
     */
    public MissionStatusErrorsPanel(BeliefManager belMgr) 
    {
        initComponents();
        
        m_BeliefManager = belMgr;
        m_LastUpdateTimestampMs = -1;
        m_HighestAlarm = 0;
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        
        MissionErrorManager.getInstance().registerErrorListener(this);
        MissionErrorManager.getInstance().registerTimedErrorListener(this);
    }
    
    @Override
    public void update ()
    {
        if (System.currentTimeMillis() - m_LastUpdateTimestampMs < 1000)
            return;
        m_LastUpdateTimestampMs = System.currentTimeMillis();
        
        try
        {
            m_HighestAlarm = MissionErrorManager.ALARMLEVEL_NOALARM;
            boolean itemsRemoved = false;
            Set<Entry<MissionErrorManager.ErrorCodeBase, Object>> currentSet = m_CurrentErrorCodes.entrySet();
            for (Entry<MissionErrorManager.ErrorCodeBase, Object> entry : currentSet)
            {
                ErrorCodeBase errorCode = entry.getKey();
                Object alarmObject = entry.getValue();
                
                int alarmLevel = MissionErrorManager.ALARMLEVEL_NOALARM;
                String text = "";
                if (alarmObject instanceof Integer)
                {
                    alarmLevel = (Integer)alarmObject;
                    text = errorCode.m_AlarmText;
                }
                else if (alarmObject instanceof TimedAlarmLevel)
                {
                    alarmLevel = ((TimedAlarmLevel)alarmObject).m_AlarmLevel;
                    long timeDelaySec = ((TimedAlarmLevel)alarmObject).m_DelaySec;
                    text = errorCode.m_AlarmText;
                    if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
                    {
                        if (timeDelaySec > 120)
                            text += timeDelaySec/60 + " minutes";
                        else
                            text += timeDelaySec + " sec";
                    }
                    else if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
                        text += "Not connected";
                }

                ErrorControlBar extgBar = m_CurrentErrorBars.get (errorCode);
                if (alarmLevel != MissionErrorManager.ALARMLEVEL_NOALARM)
                {   
                    if (extgBar == null)
                    {
                        extgBar = new ErrorControlBar(errorCode);
                        jPanel1.add(extgBar);
                        m_CurrentErrorBars.put(errorCode, extgBar);
                    }
                    
                    if (!extgBar.ignoreError())
                        m_HighestAlarm = Math.max (m_HighestAlarm, alarmLevel);
                    
                    if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
                        extgBar.updateBackgroundColor(Color.RED);
                    if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
                        extgBar.updateBackgroundColor(Color.ORANGE);
                }
                else
                {
                    if (extgBar != null)
                    {
                        jPanel1.remove (extgBar);
                        m_CurrentErrorBars.remove(errorCode);
                        itemsRemoved = true;
                    }
                }
            }
            if (itemsRemoved)
            {
                jPanel1.invalidate();
                jPanel1.validate();
                jPanel1.revalidate();
                jPanel1.repaint();
            }
            
            Color backgroundColor;
            String text = "";
            if (m_HighestAlarm == MissionErrorManager.ALARMLEVEL_ERROR)
            {
                text = "WARNINGS";
                backgroundColor = Color.RED;
            }
            else if (m_HighestAlarm == MissionErrorManager.ALARMLEVEL_WARNING)
            {
                backgroundColor = Color.ORANGE;
                text = "CAUTIONS";
            }
            else if (m_HighestAlarm == MissionErrorManager.ALARMLEVEL_NOTIFICATION)
            {
                backgroundColor = Color.GREEN;
                text = "STATUS GOOD";
            }
            else
            {
                text = "STATUS GOOD";
                backgroundColor = Color.GREEN;
                //jTextArea1.setText("No Errors to Report");
            }
            alarmLabelBackground (jLabel4, text, backgroundColor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }

    public void alarmLabelBackground (JLabel label, String text, Color backgroundColor)
    {
        if (!label.getText().equals (text))
            label.setText (text);
        
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
    
    @Override
    public void handleCommandedBeliefError(MissionErrorManager.ErrorCodeBase errorCode, int alarmLevel) 
    {
        m_CurrentErrorCodes.put(errorCode, new Integer (alarmLevel));
    }
    
    @Override
    public void handleTimedBeliefError(ErrorCodeBase errorCode, int alarmLevel, long delaySec) 
    {
        m_CurrentErrorCodes.put (errorCode, new TimedAlarmLevel (alarmLevel, delaySec));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("MISSION STATUS:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("UNKNOWN");

        jPanel1.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables


}
