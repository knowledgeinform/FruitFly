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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.ConfigBelief;
import edu.jhuapl.nstd.swarm.belief.ConfigProperty;

/**
 * Configuration property table model.
 * @author olsoncc1
 */
public final class ConfigTableModel extends AbstractTableModel {

    private BeliefManager _beliefManager;
	private List<ConfigTableRow> _tableRows;
    private int _numClicks = 0;
    private static final String AGENT_CONFIG_FILE = System.getProperty("agent.name");
    private static final String DEFAULT_CONFIG_FILE = "default";

    /** Constructor. */
    public ConfigTableModel(BeliefManager beliefManager) {
    	_beliefManager = beliefManager;
    	refreshTableModel();
    }
    
    /**
     * Initializes, populates, and sorts the table rows. 
     */
    public void refreshTableModel() {
    	_tableRows = new ArrayList<ConfigTableRow>();
        populateConfigTable();
        Collections.sort(_tableRows);
    }
    
    /**
     * Populates the table model with the Properties stored in the Config class.
     */
    private void populateConfigTable() {
        Properties defaultProperties = Config.getConfig().getDefaultProperties();
        Properties properties = Config.getConfig().getProperties();
        ConfigTableRow row;
        boolean overridden;
        String configFile;
        
        for(Object property : properties.keySet()) {
            // Get the property value
            String value = properties.getProperty((String)property);
            
            // Check if the property has been overridden in an agent file
            if(defaultProperties.containsKey(property)) {
                String overriddenValue = defaultProperties.getProperty((String) property);
                
                if(!value.equals(overriddenValue)) {
                    overridden = true;
                    configFile = AGENT_CONFIG_FILE;
                } else {
                    overridden = false;
                    configFile = DEFAULT_CONFIG_FILE;
                }
            } else {
                overridden = false;
                configFile = AGENT_CONFIG_FILE;
            }
            
            row = new ConfigTableRow((String)property, value, configFile, overridden);
            _tableRows.add(row);
        }
        
        fireTableDataChanged();
    }
    
    /**
     * Returns true if the cell in the row <i>row</i> and in column <i>col</i>
     * is editable, else returns false.
     * 
     * @param row row index
     * @param col column index
     * @return 
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if(col == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     *  Sets the value of the edited property.
     *
     *  @param  aValue   value to assign to cell
     *  @param  rowIndex   row of cell
     *  @param  columnIndex  column of cell
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ConfigTableRow row = getConfigTableRow(rowIndex);
        
        if(columnIndex == 0) {
            // NOT USED!!!!
            // Update the ConfigTableRow object
            //row.setPropertyName((String)aValue);
            
            // Update the property
            
        } else if(columnIndex == 1) {
            // Update the ConfigTableRow object
            row.setPropertyValue((String)aValue);
            row.setConfigFile(AGENT_CONFIG_FILE);
            row.setOverridden(true);
            
            // Update the property
            Config.getConfig().setProperty(row.getPropertyName(), (String)aValue);
            
            if(_beliefManager != null) {
            	_beliefManager.put(new ConfigBelief(AGENT_CONFIG_FILE, new ConfigProperty(row.getPropertyName(), (String)aValue)));
            }
        } else {
            // Do not allow the configuration file or overridden column to be editable.
        }
        
        //fireTableCellUpdated(rowIndex, columnIndex);
        fireTableDataChanged();
    }

    /** Return the selected {@link ConfigTableRow}. */
    public ConfigTableRow getConfigTableRow(int aRow) {
        return _tableRows.get(aRow);
    }

    /**
     * Sort the table rows.
     * @param aIdx 
     */
    public void sortByColumn(int aIdx) {
        _numClicks++;
        
        if (aIdx == 0) {
            //natural sorting of the ConfigTableRow class
            Collections.sort(_tableRows);
        } else {
            Comparator<ConfigTableRow> comparator = null;
            if (aIdx == 1) {
                comparator = ConfigTableRow.PROP_VAL_SORT;
            } else if (aIdx == 2) {
                comparator = ConfigTableRow.CONFIG_FILE_SORT;
            } else if (aIdx == 3) {
                comparator = ConfigTableRow.OVERRIDDEN_SORT;
            }
            Collections.sort(_tableRows, comparator);
        }
        
        if ((_numClicks % 2) == 0) {
            Collections.reverse(_tableRows);
        }
        
        fireTableDataChanged();
    }

    /** Return the number of columns in the table. */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /** Return the number of rows in the table. */
    @Override
    public int getRowCount() {
        return _tableRows.size();
    }

    /** Return the <tt>Object</tt> in a specific table cell. */
    @Override
    public Object getValueAt(int aRow, int aCol) {
        Object result = null;
        ConfigTableRow row = _tableRows.get(aRow);
        if (aCol == 0) {
            result = row.getPropertyName();
        } else if (aCol == 1) {
            result = row.getPropertyValue();
        } else if (aCol == 2) {
            result = row.getConfigFile();
        } else if (aCol == 3) {
            result = row.isOverridden();
        }
        return result;
    }

    /** Return the name of a specific column. */
    @Override
    public String getColumnName(int aIdx) {
        String result = "";
        if (aIdx == 0) {
            result = "Name";
        } else if (aIdx == 1) {
            result = "Value";
        } else if (aIdx == 2) {
            result = "Configuration File";
        } else if (aIdx == 3) {
            result = "Overridden";
        }
        return result;
    }
}
