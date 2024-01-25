//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.nstd.swarm.WACSDisplayAgent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;

/**
 * Configuration Table View
 * 
 * Displays all default and agent-specific properties stored in the <tt>Config</tt> class
 * in a table. The table allows the user to edit the property value and to sort on
 * any column by clicking on the column header.
 * 
 * @author olsoncc1
 */
public class ConfigTableView extends JDialog {

    private ConfigTableModel _configTableModel;
    private JTable _configTable;
    private BeliefManager _beliefManager;

    /**
     * TEST MAIN
     * @param args 
     */
    public static void main(String[] args) {
        
		EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
            	System.setProperty("agent.name", WACSDisplayAgent.AGENTNAME);
                ConfigTableView configTable = new ConfigTableView(null, "Property Configuration Table", true, null);
                configTable.setVisible(true);
            }
        });	
    }

    /**
     * Builds the Configuration Table frame.
     */
    public ConfigTableView(JFrame owner, String title, boolean isModal, BeliefManager belMgr) {
        super(owner, title, isModal);
        
        _beliefManager = belMgr;
        _configTableModel = new ConfigTableModel(_beliefManager);
        _configTable = new JTable(_configTableModel);
        _configTable.setBackground(Color.LIGHT_GRAY);
        _configTable.setFillsViewportHeight(true);

        // Relative column widths
        _configTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        _configTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        _configTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        _configTable.getColumnModel().getColumn(3).setPreferredWidth(20);

        // Allow the table to be sortable
        _configTable.getTableHeader().addMouseListener(new SortConfigTable());

        JScrollPane panel = new JScrollPane(_configTable);
        getContentPane().add(panel);

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        placeInMiddlePartOfTheScreen(this);
        centerDialog(this);
    }
    
    /**
     * Forces the table to repopulate it's table model
     * with the latest configuration property values.
     */
    public void refreshTable() {
    	_configTableModel.refreshTableModel();
    }

    /** Expand the dialog to fill the middle part of the screen. */
    private void placeInMiddlePartOfTheScreen(JDialog dialog) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension halfScreen = new Dimension(2 * screen.width / 3, screen.height / 2);
        dialog.setPreferredSize(halfScreen);
    }

    /**
     * Center the dialog on the screen.
     * 
     * <P>If the size of <tt>aWindow</tt> exceeds that of the screen, 
     * then the size of <tt>aWindow</tt> is reset to the size of the screen.
     * 
     * @param aWindow 
     */
    public static void centerDialog(Window aWindow) {
        // Note that the order here is important

        aWindow.pack();
        /*
         * If called from outside the event dispatch thread (as is 
         * the case upon startup, in the launch thread), then 
         * in principle this code is not thread-safe: once pack has 
         * been called, the component is realized, and (most) further
         * work on the component should take place in the event-dispatch 
         * thread. 
         *
         * In practice, it is exceedingly unlikely that this will lead 
         * to an error, since invisible components cannot receive events.
         */
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = aWindow.getSize();
        //ensure that no parts of aWindow will be off-screen
        if (window.height > screen.height) {
            window.height = screen.height;
        }
        if (window.width > screen.width) {
            window.width = screen.width;
        }
        int xCoord = (screen.width / 2 - window.width / 2);
        int yCoord = (screen.height / 2 - window.height / 2);
        aWindow.setLocation(xCoord, yCoord);
    }

    /** Sort the table. Listens for clicks on the table headers. */
    private final class SortConfigTable extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent aEvent) {
            int columnIdx = _configTable.getColumnModel().getColumnIndexAtX(aEvent.getX());
            _configTableModel.sortByColumn(columnIdx);
        }
    }
}
