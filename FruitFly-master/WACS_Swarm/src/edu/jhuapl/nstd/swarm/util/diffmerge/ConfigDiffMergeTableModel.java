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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * Configuration Diff/Merge table model.
 * @author olsoncc1
 */
public final class ConfigDiffMergeTableModel extends AbstractTableModel {
        
    /** 
     * Stores the currently "selected" cells and their background color.
     * The key to the map is the concatenation of the row and column of the cell
     * delimited by a comma. E.g. "2,3" -> Color.RED
     */
    private Map<String, Color> _selectedCells;
    
    private List<ConfigDiffMergeTableRow> _tableRows; // All table rows
    private List<ConfigDiffMergePropertyValuePair> _basePairList; // entries in order they appear in file
    private List<ConfigDiffMergePropertyValuePair> _comparedPairList; // entries in order they appear in file
    private List<ConfigDiffMergePropertyValuePair> _basePairSortedList; // entries sorted by property name
    private List<ConfigDiffMergePropertyValuePair> _comparedPairSortedList; // entries sorted by property name

    /** Constructor. */
    public ConfigDiffMergeTableModel() {
        initializeModel();
    }
    
    /**
     * Returns the concatenation of the row and column of the cell
     * delimited by a comma. E.g. "2,3"
     * 
     * @param row
     * @param column
     * @return 
     */
    private String getCellCoordsString(int row, int column) {
        return row + "," + column;
    }
    
    /**
     * Returns true if the cell at the given row and column is "selected".
     * 
     * @param row
     * @param column
     * @return 
     */
    public boolean isSelectedCell(int row, int column) {
        return _selectedCells.keySet().contains(getCellCoordsString(row, column));
    }
    
    /**
     * Sets a new cell at the given row and column to be "selected", and sets the cell's 
     * background color to <i>Color c</i>.
     * 
     * @param row
     * @param column
     * @param c 
     */
    public void addSelectedCell(int row, int column, Color c) {
        _selectedCells.put(getCellCoordsString(row, column), c);
    }
    
    /**
     * Sets the cell at the given row and column to no long be "selected".
     * 
     * @param row
     * @param column 
     */
    public void removeSelectedCell(int row, int column) {
        _selectedCells.remove(getCellCoordsString(row, column));
    }
    
    /**
     * Returns the background color associated with the cell at the given
     * row and column.
     * 
     * @param row
     * @param column
     * @return 
     */
    public Color getSelectedCellBackgroundColor(int row, int column) {
        return _selectedCells.get(getCellCoordsString(row, column));
    }
    
    /**
     * Returns all of the property-value pair lines in the base config file in
     * the order in which they appear in the file.
     * 
     * @return 
     */
    public List<ConfigDiffMergePropertyValuePair> getBaseFileLines() {
        return _basePairList;
    }
    
    /**
     * Returns all of the property-value pair lines in the compared config file
     * in the order in which they appear in the file.
     * 
     * @return 
     */
    public List<ConfigDiffMergePropertyValuePair> getComparedFileLines() {
        return _comparedPairList;
    }
    
    /**
     * Returns the list of lines in the base config file sorted in alphabetical order.
     * 
     * @return 
     */
    public List<ConfigDiffMergePropertyValuePair> getSortedBaseFileLines() {
        return _basePairSortedList;
    }
    
    /**
     * Returns the list of lines in the compared config file sorted in alphabetical order.
     * 
     * @return 
     */
    public List<ConfigDiffMergePropertyValuePair> getSortedComparedFileLines() {
        return _comparedPairSortedList;
    }
    
    /**
     * Initializes the data structures used to store data in the table.
     */
    public void initializeModel() {
        // Initialize the list of selected cells.
        _selectedCells = new HashMap<String, Color>();
        
        // Initialize the list of table rows.
        _tableRows = new ArrayList<ConfigDiffMergeTableRow>();
        
        _basePairList = new ArrayList<ConfigDiffMergePropertyValuePair>();
        _comparedPairList = new ArrayList<ConfigDiffMergePropertyValuePair>();
        _basePairSortedList = new ArrayList<ConfigDiffMergePropertyValuePair>();
        _comparedPairSortedList = new ArrayList<ConfigDiffMergePropertyValuePair>();
    }
    
    /**
     * Iterates through the list of table rows looking for identical property names
     * and values between the base and compared config files. When an identical row is found,
     * it sets the correct cells as "selected".
     */
    public void initializeSelectedCells() {
        ConfigDiffMergeTableRow row = null;
        
        for(int i = 0; i < getRowCount(); i++) {
            row = getConfigDiffMergeTableRow(i);
            
            if(row.getBasePropertyValuePair() != null && row.getComparedPropertyValuePair() != null) {
                if(row.getBasePropertyValuePair().getFormattedString().equals(row.getComparedPropertyValuePair().getFormattedString())) {
                    addSelectedCell(i, 0, ConfigDiffMergeView.LIGHT_GRAY);
                    addSelectedCell(i, 1, ConfigDiffMergeView.LIGHT_GRAY);
                    addSelectedCell(i, 2, ConfigDiffMergeView.LIGHT_GRAY);
                }
            }
        }
    }
    
    /**
     * Populates the table model with the Properties stored in the Config class.
     * @param baseAgentFile Base file to parse.
     * @param comparedAgentFile Compared file to parse.
     */
    public void populateConfigDiffMergeTable(File baseAgentFile, File comparedAgentFile) {
        
        ConfigDiffMergeTableRow row;
        ConfigDiffMergeFileParser parser = new ConfigDiffMergeFileParser();
        
        _basePairList = parser.loadProperties(baseAgentFile);
        _basePairSortedList = new ArrayList<ConfigDiffMergePropertyValuePair>(_basePairList);
        Collections.sort(_basePairSortedList);
        
        _comparedPairList = parser.loadProperties(comparedAgentFile);
        _comparedPairSortedList = new ArrayList<ConfigDiffMergePropertyValuePair>(_comparedPairList);
        Collections.sort(_comparedPairSortedList);
        
        // Populate the table model with the base agent properties
        int b = 0; // index into the _baseKeySortedList
        int c = 0; // index into the _comparedKeySortedList
        ConfigDiffMergePropertyValuePair baseProperty;
        ConfigDiffMergePropertyValuePair comparedProperty;
        
        while((b <= _basePairSortedList.size()-1) && (c <= _comparedPairSortedList.size()-1)) {
            baseProperty = _basePairSortedList.get(b);
            comparedProperty = _comparedPairSortedList.get(c);
            
            // Skip invalid base properties
            if(baseProperty.isCommentedLine()) {
                b++;
                continue;
            }
            
            // Skip invalid compared properties
            if(comparedProperty.isCommentedLine()) {
                c++;
                continue;
            }
            
            // Compare the two valid properties
            int comparison = baseProperty.compareTo(comparedProperty);
            
            if(comparison == 0) {
                // Both lists have the same property
                row = new ConfigDiffMergeTableRow(baseProperty, comparedProperty);
                row.setMergedPropertyValueString(baseProperty.getFormattedString());
                b++;
                c++;
            } else if(comparison < 0) {
                // The base agent property is not in the compared agent config file.
            	// If the two properties have the same property name, put them in the same table row.
            	if(baseProperty.getPropertyName().equals(comparedProperty.getPropertyName())) {
            		row = new ConfigDiffMergeTableRow(baseProperty, comparedProperty);
            		c++;
            	} else {
                	row = new ConfigDiffMergeTableRow(baseProperty, null);
            	}
                b++;
            } else {
                // The compared agent property is not in the base agent config file.
            	// If the two properties have the same property name, put them in the same table row.
            	if(baseProperty.getPropertyName().equals(comparedProperty.getPropertyName())) {
            		row = new ConfigDiffMergeTableRow(baseProperty, comparedProperty);
            		b++;
            	} else {
            		row = new ConfigDiffMergeTableRow(null, comparedProperty);
            	}
                c++;
            }
            
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
        if(col == 2) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     *  Sets the value of the edited property.
     *
     *  @param  value   value to assign to cell
     *  @param  rowIndex   row of cell
     *  @param  columnIndex  column of cell
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ConfigDiffMergeTableRow row = getConfigDiffMergeTableRow(rowIndex);
        String valStr = (String) value;
        if(columnIndex == 2) {
            // If the value has changed, then color it green and fire a TableCellUpdated event
            if(!valStr.equals(row.getMergedPropertyValueString())) {
                row.setMergedPropertyValueString(valStr);
                addSelectedCell(rowIndex, columnIndex, ConfigDiffMergeView.LIGHT_GREEN);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    /** Return the selected {@link ConfigDiffMergeTableRow}. */
    public ConfigDiffMergeTableRow getConfigDiffMergeTableRow(int rowIndex) {
        return _tableRows.get(rowIndex);
    }

    /** Return the number of columns in the table. */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /** Return the number of rows in the table. */
    @Override
    public int getRowCount() {
        return _tableRows.size();
    }

    /** Return the <tt>Object</tt> in a specific table cell. */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        ConfigDiffMergeTableRow row = _tableRows.get(rowIndex);
        if (columnIndex == 0) {
            if(row.getBasePropertyValuePair() == null)
                result = "";
            else
                result = row.getBasePropertyValuePair().getFormattedString();
        } else if (columnIndex == 1) {
            if(row.getComparedPropertyValuePair() == null)
                result = "";
            else
                result = row.getComparedPropertyValuePair().getFormattedString();
        } else if (columnIndex == 2) {
            result = row.getMergedPropertyValueString();
        }
        return result;
    }

    /** Return the name of a specific column. */
    @Override
    public String getColumnName(int index) {
        String result = "";
        if (index == 0) {
            result = "Base Properties";
        } else if (index == 1) {
            result = "Compared Properties";
        } else if (index == 2) {
            result = "Selected Properties";
        }
        
        return result;
    }
}
