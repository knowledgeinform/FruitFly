/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ZeroAirDataFrame.java
 *
 * Created on Oct 13, 2010, 12:02:53 PM
 */

package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import javax.swing.JFrame;

/**
 *
 * @author humphjc1
 */
public class ZeroAirDataFrame extends javax.swing.JFrame 
{
    
    /** Creates new form ZeroAirDataFrame */
    public ZeroAirDataFrame(BeliefManager belMgr, double defAltMSLm, double defPressPa) 
    {
        ZeroAirDataPanel panel = new ZeroAirDataPanel(belMgr, defAltMSLm, defPressPa);
        this.setSize (panel.getPreferredSize());
        this.add(panel);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }   
}
