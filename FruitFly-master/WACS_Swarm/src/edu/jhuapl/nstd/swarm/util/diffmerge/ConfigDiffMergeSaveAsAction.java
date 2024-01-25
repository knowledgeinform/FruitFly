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
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class handles the "Save As..." button action. When this button is
 * clicked, the currently selected properties are written to a new configuration
 * file while trying to preserve the order in which they had in their original files.
 * 
 * @author olsoncc1
 */
class ConfigDiffMergeSaveAsAction implements ActionListener {
    
    private ConfigDiffMergeView _parentFrame;
    private ConfigDiffMergeTableModel _configTableModel;

    public ConfigDiffMergeSaveAsAction(ConfigDiffMergeView parentFrame) {
        _parentFrame = parentFrame;
        _configTableModel = _parentFrame.getConfigTableModel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        int returnVal = _parentFrame.getFileChooser().showSaveDialog(_parentFrame);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            int listSize = 0;
            
            if(_configTableModel.getBaseFileLines().size() > _configTableModel.getComparedFileLines().size()) {
                listSize = _configTableModel.getBaseFileLines().size();
            } else {
                listSize = _configTableModel.getComparedFileLines().size();
            }
            
            List<String> configFileLines = new ArrayList<String>(listSize);
            File newConfigFile = _parentFrame.getFileChooser().getSelectedFile();
            
            if(newConfigFile.exists()) {
                returnVal = _parentFrame.showConfirmDialog("Overwrite Selected File?",
                        "Do you want to overwrite the selected file?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                
                // If the user does not wish to overwrite the selected file, then do not save anything.
                if(returnVal == JOptionPane.NO_OPTION) {
                    return;
                }
            }
             
            // Initialize the list of base file lines.
            // All line numbers (starting at 0) should be unique, thus no overwriting at this point.
            for(ConfigDiffMergePropertyValuePair propValPair : _configTableModel.getBaseFileLines()) {
                if(propValPair.isCommentedLine()) {
                	// Used for comments and blank spaces
                    configFileLines.add(propValPair.getLineNumber(), propValPair.getRawLine());
                } else {
	                // Used for force "property = value" formatted string
                    configFileLines.add(propValPair.getLineNumber(), propValPair.getFormattedString());
                }
            }
            
            List<String> unmergedConfigFileLines = new ArrayList<String>();
            ConfigDiffMergeTableRow row = null;
            int lineNum = 0;
            String mergedStr = "";
            BufferedWriter writer = null;
            
            for(int i = 0; i < _configTableModel.getRowCount(); i++) {
                row = _configTableModel.getConfigDiffMergeTableRow(i);
                mergedStr = row.getMergedPropertyValueString();
                
                if(row.getBasePropertyValuePair() != null) {
                    lineNum = row.getBasePropertyValuePair().getLineNumber();
                    
                    // Possibly overwrite contents of base config file
                    configFileLines.set(lineNum, mergedStr);
                } else {
                    // If the base property-value pair is null and the merged property-value string is not null,
                    // then we don't know where to place the mergeStr in the base file.
                    if(mergedStr != null && !mergedStr.equals("")) {
                        unmergedConfigFileLines.add(mergedStr);
                    }
                }
            }
            
            try {
                try {
                    writer = new BufferedWriter(new FileWriter(newConfigFile));

                    // Write out the lines of the config file.
                    for(String line : configFileLines) {
                        writer.write(line + "\r\n");
                    }

                    // Write out the config property lines that couldn't be merged
                    if(unmergedConfigFileLines.size() > 0) {
                        writer.write("\r\n######## Unmerged Properties ########\r\n");
                        
                        for(String line : unmergedConfigFileLines) {
                            writer.write(line + "\r\n");
                        }
                    }
                    writer.write("\r\n");
                }
                finally {
                    if(writer != null) {
                        writer.close();
                        
                        // Show the user confirmation that the file was created.
                        _parentFrame.showMessageDialog("Configuration File Created Sucessfully",
                                "The new configuration file was created successfully.");
                    }
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    } // end actionPerformed()
}
