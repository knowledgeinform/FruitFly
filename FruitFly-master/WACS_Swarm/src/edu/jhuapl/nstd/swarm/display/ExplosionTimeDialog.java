/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExplosionTimeDialog.java
 *
 * Created on Jun 29, 2011, 8:06:40 AM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import javax.swing.JFrame;

/**
 *
 * @author humphjc1
 */
public class ExplosionTimeDialog extends javax.swing.JFrame
{
    /** Creates new form ExplosionTimeDialog */
    public ExplosionTimeDialog(BeliefManager belMgr) 
    {
        ExplosionTimePanel panel = new ExplosionTimePanel(belMgr);
        this.setSize (panel.getPreferredSize());
        this.add(panel);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }   
}