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

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JTextField;

/**
 * This class is used to handle the Compare button action on the
 * Configuration File Diff/Merge Tool.
 * 
 * @author olsoncc1
 */
public class ConfigDiffMergeCompareAction extends AbstractAction {

    private ConfigDiffMergeView _parentFrame;

    public ConfigDiffMergeCompareAction(ConfigDiffMergeView parentFrame) {
        _parentFrame = parentFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JTextField baseFileTextField = _parentFrame.getBaseFileTextField();
        JTextField comparedFileTextField = _parentFrame.getComparedFileTextField();

        // Apply constraint checking to the text field values
        if (baseFileTextField.getText() != null && !baseFileTextField.getText().equals("")
                && comparedFileTextField.getText() != null && !comparedFileTextField.getText().equals("") && 
                !baseFileTextField.getText().equals(comparedFileTextField.getText())) {
            File baseFile = new File(baseFileTextField.getText());
            File comparedFile = new File(comparedFileTextField.getText());

            if (baseFile.exists() && comparedFile.exists()) {
                // Clear out all model changes.
                _parentFrame.getConfigTableModel().initializeModel();
                
                // Populate the model with properties from the base agent config file and compared agent config file.
                _parentFrame.getConfigTableModel().populateConfigDiffMergeTable(baseFile, comparedFile);
                
                // Initialize the "selected" cells in the table.
                _parentFrame.getConfigTableModel().initializeSelectedCells();
                
                // Enable the "Save As..." button.
                _parentFrame.getSaveAsButton().setEnabled(true);
            }
        } else {
            // Show error dialogs
            if (baseFileTextField.getText() == null || baseFileTextField.getText().equals("")) {
                _parentFrame.showErrorDialog("Missing Base Configuration File", "You must select a base configuration file first.");
            } else if(comparedFileTextField.getText() == null || comparedFileTextField.getText().equals("")) {
                _parentFrame.showErrorDialog("Missing Compared Configuration File", "You must select a compared configuration file first.");
            } else {
                _parentFrame.showErrorDialog("Invalid Selected Configuration Files", "You must select different configuration files to compare.");
            }
        }
    }
}
