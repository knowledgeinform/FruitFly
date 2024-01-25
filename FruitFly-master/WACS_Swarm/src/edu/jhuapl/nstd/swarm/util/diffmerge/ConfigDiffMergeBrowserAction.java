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
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * This action is used to select a configuration file using a JFileChooser
 * for the Configuration File Diff/Merge Tool.
 * 
 * @author olsoncc1
 */
class ConfigDiffMergeBrowserAction extends AbstractAction {

    private ConfigDiffMergeView _parentFrame;
    private JTextField _configFileTextField;

    public ConfigDiffMergeBrowserAction(ConfigDiffMergeView parentFrame, JTextField configFileTextField) {
        super("Select a Configuration File");
        _parentFrame = parentFrame;
        _configFileTextField = configFileTextField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int returnVal = _parentFrame.getFileChooser().showOpenDialog(_parentFrame);

        // Get the selected file
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = _parentFrame.getFileChooser().getSelectedFile();

            if (selectedFile != null) {
                _configFileTextField.setText(selectedFile.getAbsolutePath());
            }
        }
    }
}
