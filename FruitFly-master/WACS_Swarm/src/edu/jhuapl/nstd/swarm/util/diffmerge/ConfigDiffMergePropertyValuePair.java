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

/**
 * This class is used to store a property name, value, and the line number
 * parsed from a single line of the configuration file.
 * 
 * @author olsoncc1
 */
public class ConfigDiffMergePropertyValuePair implements Comparable<ConfigDiffMergePropertyValuePair> {
    /** Property name (i.e. property token) */
	private String _propertyName;
	
	/** Property value */
    private String _propertyValue;
    
    /** Raw line parsed from the configuration file. */
    private String _rawLine;
    
    /** Line number from the configuration file of this parsed line. */
    private int _lineNumber;
    
    /** True if the parsed line is a comment, else false. */
    private boolean _isCommentedLine;
    
    /** Comparator value for equals */
    private static final int EQUAL = 0;

    public ConfigDiffMergePropertyValuePair(String rawLine, int lineNumber) {
        _propertyName = "";
        _propertyValue = "";
        _rawLine = rawLine;
        _lineNumber = lineNumber;
        _isCommentedLine = true;
    }

    public ConfigDiffMergePropertyValuePair(String propertyName, String propertyValue, String rawLine, int lineNumber) {
        _propertyName = propertyName;
        _propertyValue = propertyValue;
        _rawLine = rawLine;
        _lineNumber = lineNumber;
        _isCommentedLine = false;
    }

    public String getRawLine() {
        return _rawLine;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public String getPropertyValue() {
        return _propertyValue;
    }

    public boolean isCommentedLine() {
        return _isCommentedLine;
    }
    
        public void setIsCommentedLine(boolean isComment) {
        this._isCommentedLine = isComment;
    }

    public void setLineNumber(int lineNumber) {
        this._lineNumber = lineNumber;
    }

    public void setPropertyName(String propertyName) {
        this._propertyName = propertyName;
    }

    public void setPropertyValue(String propertyValue) {
        this._propertyValue = propertyValue;
    }

    public void setRawLine(String rawLine) {
        this._rawLine = rawLine;
    }

    /**
     * Returns a formatted string of the property name and value in the form
     * "property = value". If either the property name or value variable 
     * of this class is null or the empty string, the empty string is returned.
     * 
     * @return 
     */
    public String getFormattedString() {

        if(_propertyName != null && !_propertyName.equals("") &&
                _propertyValue != null && !_propertyValue.equals("")) {
            return _propertyName + " = " + _propertyValue;
        } else {
            return "";
        }
    }

    @Override
    public int compareTo(ConfigDiffMergePropertyValuePair aThat) {
        if (this == aThat) {
            return EQUAL;
        }

        int comparison = this._propertyName.compareTo(aThat._propertyName);
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = this._propertyValue.compareTo(aThat._propertyValue);
        if (comparison != EQUAL) {
            return comparison;
        }

        return EQUAL;
    }
}
