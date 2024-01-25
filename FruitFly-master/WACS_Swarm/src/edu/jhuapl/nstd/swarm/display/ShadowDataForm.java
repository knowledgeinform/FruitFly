/*
 * ShadowSimulatorForm.java
 *
 * Created on Nov 1, 2010, 11:31:20 AM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.CommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.TelemetryMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface;
import java.awt.Graphics;

/**
 *
 * @author abuck
 */
public class ShadowDataForm extends javax.swing.JFrame 
{
    
    /** Creates new form ShadowSimulatorForm */
    public ShadowDataForm(KnobsAutopilotInterface interf)
    {
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setLocation(200, 200);
        
        ShadowDataFormPanel shadowPanel = new ShadowDataFormPanel(interf);
        add(shadowPanel);
        pack();
    }

    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new ShadowDataForm(ShadowAutopilotInterface.s_instance).setVisible(true);
            }
        });
    }

    
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
