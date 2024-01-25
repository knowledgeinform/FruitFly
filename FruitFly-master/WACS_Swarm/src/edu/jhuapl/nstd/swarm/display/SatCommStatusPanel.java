/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.MissionErrorManager;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.SatCommStatusBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.WacsHeartbeatBelief;
import java.awt.Color;


    /**
 *
 * @author humphjc1
 */
public class SatCommStatusPanel extends javax.swing.JPanel implements Updateable, MissionErrorManager.TimedBeliefErrorListener
{
    BeliefManager m_BeliefManager;
    SearchCanvas m_SearchCanvas;
    
    
    /**
     * Creates new form SatCommStatusPanel
     */
    public SatCommStatusPanel(BeliefManager belMgr, SearchCanvas searchCanvas) 
    {
        initComponents();
        
        m_BeliefManager = belMgr;
        m_SearchCanvas = searchCanvas;
        MissionErrorManager.getInstance().registerTimedErrorListener(this);
    }
    
    @Override
    public void update ()
    {
    }

    @Override
    public void handleTimedBeliefError(MissionErrorManager.ErrorCodeBase errorCode, int alarmLevel, long delaySec) 
    {
        if (errorCode == MissionErrorManager.CONSTANTUPDATE_WACSHEARTBEAT_ERRORCODE)
        {
            if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
            {
                jLabel2.setBackground(Color.RED);
                jLabel2.setText("NOT CONNECTED");
            }
            else if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
            {
                jLabel2.setBackground(Color.ORANGE);
                jLabel2.setText("CONNECTED");
            }
            else
            {
                jLabel2.setBackground(Color.GREEN);
                jLabel2.setText("CONNECTED");
            }

            if (delaySec < 60)
            {
                jLabel4.setText (delaySec + " sec");
            }
            else if (delaySec < 3600)
            {
                jLabel4.setText (delaySec/60 + " min," + (delaySec-delaySec/60) + " sec");
            }
            else
            {
                jLabel4.setText ("No Comms");
            }
        }
        else if (errorCode == MissionErrorManager.CONSTANTUPDATE_SATCOMMHEARTBEAT_ERRORCODE)
        {
            if (alarmLevel == MissionErrorManager.ALARMLEVEL_ERROR)
            {
                //If we don't have a link to the satcomm board, then just display error code immediately
                jLabel7.setBackground(Color.RED);
                jLabel7.setText("NOT CONNECTED");
                jLabel5.setText ("No Comms");
            }
            else
            {
                //We do have a recent link to the satcomm board, so detect if the data says we're good
                SatCommStatusBeliefSatComm blf = (SatCommStatusBeliefSatComm)m_BeliefManager.get (SatCommStatusBeliefSatComm.BELIEF_NAME);
                if (blf != null)
                {
                    if (!blf.getModemStatus() || blf.getModemState() != SatCommStatusBeliefSatComm.MODEMSTATE_RUN)
                    {
                        //We are not in full up-and-running mode, display warning
                        jLabel7.setBackground(Color.ORANGE);
                        jLabel7.setText("LOST SAT LINK");
                    }
                    else
                    {
                        //Good to go
                        if (alarmLevel == MissionErrorManager.ALARMLEVEL_WARNING)
                            jLabel7.setBackground(Color.ORANGE);
                        else
                            jLabel7.setBackground(Color.GREEN);
                        jLabel7.setText("CONNECTED");
                    }
                    
                    int latencySec = blf.getLatencyMs()/1000;
                    if (latencySec < 60)
                    {
                        jLabel5.setText (latencySec + " sec");
                    }
                    else if (latencySec < 65)
                    {
                        jLabel5.setText (latencySec/60 + " min," + (latencySec-(latencySec/60)*60) + " sec");
                    }
                    else
                    {
                        jLabel5.setText ("No Comms");
                    }
                }
                else
                {
                    //We've never gotten a sat comm status belief.  I don't think this case is possible
                    jLabel7.setBackground(Color.RED);
                    jLabel7.setText("NOT CONNECTED");
                    jLabel5.setText ("No Comms");
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setName(""); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Direct Link Status:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Unknown");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel2.setOpaque(true);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Latency:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("Unknown");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Unknown");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Latency:");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("Unknown");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel7.setOpaque(true);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel8.setText("Satellite Link Status:");

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("CENTER MAP ON UAS");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        AgentPositionBelief agPosBlf = (AgentPositionBelief)m_BeliefManager.get(AgentPositionBelief.BELIEF_NAME);
        if (agPosBlf != null)
        {
            PositionTimeName ptn = agPosBlf.getPositionTimeName(WACSAgent.AGENTNAME);
            if (ptn != null)
            {
                m_SearchCanvas.setViewCenter (ptn.getPosition());
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    // End of variables declaration//GEN-END:variables
}
