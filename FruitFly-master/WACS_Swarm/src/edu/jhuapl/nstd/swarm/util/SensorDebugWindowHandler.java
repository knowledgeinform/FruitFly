/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterfaceTest;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 *
 * @author humphjc1
 */
public class SensorDebugWindowHandler 
{
    private static cbrnPodsInterface m_PodInterface;
    private final static String m_DebugPassword = "wacs!";
    
    public static cbrnPodsInterface getPodInterface(BeliefManager belMgr)
    {
        if (m_PodInterface == null)
        {
            cbrnPodsInterfaceTest podsTest = new cbrnPodsInterfaceTest(belMgr, false);
            podsTest.setVisible(false);
            m_PodInterface = podsTest.shareInterface();
        }
        return m_PodInterface;
    }
            
    public static void addSensorDebugDoubleClickEvent (final JLabel label, final JFrame debugFrame)
    {
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() >= 2)
                {
                    String passTry = null;
                    final JPasswordField pwd = new JPasswordField(16);  
                    new Thread ()
                    {
                        public void run ()
                        {
                            try {
                                Thread.sleep (100);
                            } catch (InterruptedException ex) {
                            }
                            pwd.requestFocusInWindow();
                        }
                        
                    }.start();
                    int action = JOptionPane.showConfirmDialog(label, pwd, "Password Required", JOptionPane.OK_CANCEL_OPTION);
                    if(action == JOptionPane.OK_OPTION)
                        passTry = new String(pwd.getPassword());  
                    
                    if (passTry != null)
                    {
                        if (passTry.equals (m_DebugPassword))
                            debugFrame.setVisible(true);
                        else
                            JOptionPane.showMessageDialog(label, "Invalid Password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                    
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
}
