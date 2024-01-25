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
package edu.jhuapl.nstd.swarm.util.diffmerge;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * This is the main class for the standalone Configuration File Diff/Merge graphical tool.
 * 
 * @author olsoncc1
 */
public class ConfigDiffMergeView extends JFrame {

    private static final String APP_VERSION = "v1.1";
	private ConfigDiffMergeTableModel _configTableModel;
    private JTable _configTable;
    private JTextField _baseFileTextField;
    private JTextField _comparedFileTextField;
    private JFileChooser _fileChooser;
    private JButton _compareButton;
    private JButton _saveAsButton;
    
    /** Custom color used for highlighting the "Base File" column. */
    public static final Color LIGHT_BLUE = new Color(87, 105, 227, 120);
    
    /** Custom color used for highlighting the "Compared File" column. */    
    public static final Color LIGHT_RED = new Color(220, 48, 52, 120);
    
    /** Custom color used for highlighting the edited cells in the "Selected Properties" column. */    
    public static final Color LIGHT_GREEN = new Color(44, 224, 53, 120);
    
    /** Custom color used for highlighting the properties that are the same between the two files. */
    public static final Color LIGHT_GRAY = new Color(196, 196, 196, 120);

    /**
     * Main method to run the Configuration File Diff/Merge Tool.
     * 
     * @param args none
     */
    public static void main(String[] args) {
        ConfigDiffMergeView configTableView = new ConfigDiffMergeView();
        configTableView.setVisible(true);
    }

    /**
     * Builds the Configuration File Diff/Merge Tool frame.
     */
    public ConfigDiffMergeView() {
        super("Swarm Configuration File Diff/Merge Tool " + APP_VERSION);
        buildGui();
    }

    /**
     * Creates the main GUI.
     */
    private void buildGui() {
        // Build the GUI
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JScrollPane configTableScrollPane = new JScrollPane(getConfigTable());
        
        mainPanel.add(createFileSelectionPanel());
        mainPanel.add(configTableScrollPane);

        getContentPane().add(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        placeInMiddlePartOfTheScreen();
        centerDialog(this);
    }
    
    /**
     * Displays an error dialog with the given title and message.
     * 
     * @param title
     * @param message
     * @param optionType JOptionPane option type - YES_NO_OPTION, YES_NO_CANCEL_OPTION, or OK_CANCEL_OPTION
     * @param messageType JOptionPane message type - ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE
     * 
     * @return integer indicating user's selected option
     */
    public int showConfirmDialog(String title, String message, int optionType, int messageType) {
        try {
            return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
        } catch(Exception e) {
            // ignore
        }
        
        return JOptionPane.DEFAULT_OPTION;
    }
    
    /**
     * Displays an error dialog with the given title and message.
     * 
     * @param title
     * @param message 
     */
    public void showErrorDialog(String title, String message) {
        try {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            // ignore
        }
    }
    
    /**
     * Displays a dialog with the given title and message.
     * 
     * @param title
     * @param message 
     */
    public void showMessageDialog(String title, String message) {
        try {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
        } catch(Exception e) {
            // ignore
        }
    }
    
    /**
     * Builds/returns the base configuration file text field.
     * 
     * @return 
     */
    public JTextField getBaseFileTextField() {
        if(_baseFileTextField == null) {
            _baseFileTextField = new JTextField();
        }
        
        return _baseFileTextField;
    }
    
    /**
     * Builds/returns the compared configuration file text field.
     * 
     * @return 
     */
    public JTextField getComparedFileTextField() {
        if(_comparedFileTextField == null) {
            _comparedFileTextField = new JTextField();
        }
        
        return _comparedFileTextField;
    }
    
    /**
     * Builds/returns the configuration file JFileChooser.
     * 
     * @return 
     */
    public JFileChooser getFileChooser() {
        if (_fileChooser == null) {
            _fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
            _fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            _fileChooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    String filename = file.getName();

                    if (file.isDirectory() || filename.toLowerCase().endsWith(".txt")) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public String getDescription() {
                    return "*.txt";
                }
            });
        }

        return _fileChooser;
    }
    
    /**
     * Builds/returns the configuration table model.
     * 
     * @return 
     */
    public ConfigDiffMergeTableModel getConfigTableModel() {
        if(_configTableModel == null) {
            _configTableModel = new ConfigDiffMergeTableModel();
        }
        
        return _configTableModel;
    }
    
    /**
     * Builds/returns the configuration table.
     * 
     * @return 
     */
    public final JTable getConfigTable() {
        if(_configTable == null) {
            _configTable = new JTable(getConfigTableModel());
            _configTable.setBackground(Color.WHITE);
            _configTable.setFillsViewportHeight(true);
            _configTable.setShowGrid(true);
            _configTable.setCellSelectionEnabled(true);
            _configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            _configTable.addMouseListener(new ConfigDiffMergeTableMouseListener());

            // Relative column widths
            // Add cell renderers for each column.
            _configTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            _configTable.getColumnModel().getColumn(0).setCellRenderer(new PropertyCellRenderer());
            _configTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            _configTable.getColumnModel().getColumn(1).setCellRenderer(new PropertyCellRenderer());
            _configTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            _configTable.getColumnModel().getColumn(2).setCellRenderer(new PropertyCellRenderer());

            // Customize the table header
            JTableHeader header = _configTable.getTableHeader();
            header.setBackground(Color.LIGHT_GRAY);
            header.setForeground(Color.BLACK);
            header.setFont(new Font("Dialog", Font.BOLD, 13));
            header.setReorderingAllowed(false);
        }
        
        return _configTable;
    }
    
    /**
     * Builds/returns the "Compare" button.
     * 
     * @return 
     */
    public JButton getCompareButton() {
        if(_compareButton == null) {
            _compareButton = new JButton("Compare");
            _compareButton.addActionListener(new ConfigDiffMergeCompareAction(this));
        }
        
        return _compareButton;
    }
    
    /**
     * Builds/returns the "Save As..." button.
     * 
     * @return 
     */
    public JButton getSaveAsButton() {
        if(_saveAsButton == null) {
            _saveAsButton = new JButton("Save As...");
            _saveAsButton.setEnabled(false);
            _saveAsButton.addActionListener(new ConfigDiffMergeSaveAsAction(this));
        }
        
        return _saveAsButton;
    }

    /**
     * Builds the file selection panel that contains the two file selection
     * buttons, text fields, the "Compare" button, and the "Save As..." button.
     * @return 
     */
    private JPanel createFileSelectionPanel() {
        JPanel fileSelectionPanel;
        JButton baseBrowseButton;
        JButton comparedBrowseButton;
        JLabel baseFileLabel;
        JLabel comparedFileLabel;

        // Initialize the file selection components
        fileSelectionPanel = new JPanel();
        GroupLayout layout = new GroupLayout(fileSelectionPanel);
        fileSelectionPanel.setLayout(layout);
        baseBrowseButton = new JButton("Browse...");
        comparedBrowseButton = new JButton("Browse...");
        baseFileLabel = new JLabel("Base Config File:");
        comparedFileLabel = new JLabel("Compared Config File:");

        // Add action listeners to the browse buttons
        baseBrowseButton.addActionListener(new ConfigDiffMergeBrowserAction(this, getBaseFileTextField()));
        comparedBrowseButton.addActionListener(new ConfigDiffMergeBrowserAction(this, getComparedFileTextField()));

        // Add the components to the file selection panel
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(layout.createSequentialGroup().
                addGap(20, 20, 20).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addComponent(comparedFileLabel, GroupLayout.Alignment.TRAILING).
                addComponent(baseFileLabel, GroupLayout.Alignment.TRAILING)).
                addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).
                addComponent(getComparedFileTextField()).
                addComponent(getBaseFileTextField(), GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)).
                addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addComponent(comparedBrowseButton).
                addComponent(baseBrowseButton)).
                addGap(60, 60, 60).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addComponent(getCompareButton()).
                addComponent(getSaveAsButton())).
                addGap(20, 20, 20)));

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                addGroup(layout.createSequentialGroup().
                addGap(20, 20, 20).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(baseFileLabel).
                addComponent(getBaseFileTextField(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).
                addComponent(baseBrowseButton).
                addComponent(getCompareButton())).
                addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(comparedFileLabel).
                addComponent(getComparedFileTextField(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).
                addComponent(comparedBrowseButton).
                addComponent(getSaveAsButton())).
                addGap(20, 20, 20)));

        return fileSelectionPanel;
    }

    /** Expand the dialog to fill the middle part of the screen. */
    private void placeInMiddlePartOfTheScreen() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension halfScreen = new Dimension(2 * screen.width / 3, screen.height / 2);
        getContentPane().setPreferredSize(halfScreen);
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
    
    /**
     * This class is used to listen for double-clicks on the table for the
     * Config Diff/Merge Tool.
     */
    class ConfigDiffMergeTableMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                Point p = e.getPoint();
                int rowAtPoint = getConfigTable().rowAtPoint(p);
                int columnAtPoint = getConfigTable().columnAtPoint(p);

                // Use table.convertRowIndexToModel / table.convertColumnIndexToModel to convert to view indices
                int row = getConfigTable().convertRowIndexToModel(rowAtPoint);
                int column = getConfigTable().convertColumnIndexToModel(columnAtPoint);

                // Set the row's "Selected Properties" column value
                ConfigDiffMergeTableRow cdmTableRow = getConfigTableModel().getConfigDiffMergeTableRow(row);
                ConfigDiffMergePropertyValuePair cdmpvPair = null;
                ConfigDiffMergeTableModel model = getConfigTableModel();

                // Store the indexes of the row/column of the selected cell.                    
                // These indexes are then used in the custom renderer to color the selected cells.

                // Only listen to double-clicks on the first or second column.
                // The third column is editable if double-clicked, but this code doesn't handle editing.
                if (column == 0) {
                    cdmpvPair = cdmTableRow.getBasePropertyValuePair();

                    // If the cell is not currently selected
                    if (!model.isSelectedCell(row, column)) {
                        // Add the newly selected cell coordinates.
                        model.addSelectedCell(row, column, LIGHT_BLUE); // 1st column
                        model.addSelectedCell(row, 2, LIGHT_BLUE); // 3rd column

                        // Remove the selected cell in the 2nd column (if it exists).
                        if (model.isSelectedCell(row, 1)) {
                            model.removeSelectedCell(row, 1); // 2nd column
                        }

                        // Set the selected property string in 3rd column
                        if (cdmpvPair != null) {
                            cdmTableRow.setMergedPropertyValueString(cdmpvPair.getFormattedString());
                        } else {
                            cdmTableRow.setMergedPropertyValueString("");
                        }
                    } else {
                        // The cell is currently selected
                        // Remove the double-clicked cell
                        model.removeSelectedCell(row, column); // 1st column

                        // Remove the selected cell in the third column (if it exists).
                        if (model.isSelectedCell(row, 2)) {
                            model.removeSelectedCell(row, 2); // 3rd column

                            // Set the selected property string in 3rd column
                            if (cdmpvPair != null) {
                                cdmTableRow.setMergedPropertyValueString("");
                            }
                        }
                    }
                } // end if column == 0
                else if (column == 1) {
                    cdmpvPair = cdmTableRow.getComparedPropertyValuePair();

                    // If the cell is not currently selected
                    if (!model.isSelectedCell(row, column)) {
                        // Add the newly selected cell coordinates.
                        model.addSelectedCell(row, column, LIGHT_RED); // 2nd column
                        model.addSelectedCell(row, 2, LIGHT_RED); // 3rd column

                        // Remove the selected cell in the 1st column (if it exists).
                        if (model.isSelectedCell(row, 0)) {
                            model.removeSelectedCell(row, 0); // 1st column
                        }

                        // Set the selected property string in 3rd column
                        if (cdmpvPair != null) {
                            cdmTableRow.setMergedPropertyValueString(cdmpvPair.getFormattedString());
                        } else {
                            cdmTableRow.setMergedPropertyValueString("");
                        }
                    } else {
                        // The cell is currently selected
                        // Remove the double-clicked cell
                        model.removeSelectedCell(row, column); // 2nd column

                        // Remove the selected cell in the third column (if it exists).
                        if (model.isSelectedCell(row, 2)) {
                            model.removeSelectedCell(row, 2); // 3rd column

                            // Set the selected property string in 3rd column
                            if (cdmpvPair != null) {
                                cdmTableRow.setMergedPropertyValueString("");
                            }
                        }
                    }
                } // end if column == 1
                else if(column == 2) {
                    // Cannot capture double-clicks on this column from here,
                    // because the cell is editable.
                } // end if column == 2

                getConfigTableModel().fireTableDataChanged();
            } // end if double-click detected
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Not listening for this event.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Not listening for this event.
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Not listening for this event.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Not listening for this event.
        }
        
    }

    /**
     * This custom cell renderer is used for setting background colors to individual
     * cells of the table.
     */
    class PropertyCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (getConfigTableModel().isSelectedCell(row, column)) {
                cell.setBackground(getConfigTableModel().getSelectedCellBackgroundColor(row, column));
            } else {
                cell.setBackground(Color.WHITE);
            }

            return cell;
        }
    }
}
