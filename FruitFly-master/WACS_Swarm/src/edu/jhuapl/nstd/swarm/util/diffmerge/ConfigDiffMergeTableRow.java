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
 * This class is used to store the contents of each column in the table used by
 * the Config Diff/Merge Tool.
 * 
 * @author olsoncc1
 */
class ConfigDiffMergeTableRow {
    private ConfigDiffMergePropertyValuePair _basePropertyValuePair;
    private ConfigDiffMergePropertyValuePair _comparedPropertyValuePair;
    private String _mergedPropertyValueString;
    
    public ConfigDiffMergeTableRow(ConfigDiffMergePropertyValuePair basePropertyValuePair, ConfigDiffMergePropertyValuePair comparedPropertyValuePair)
    {
        _basePropertyValuePair = basePropertyValuePair;
        _comparedPropertyValuePair = comparedPropertyValuePair;
        _mergedPropertyValueString = "";
    }

    public ConfigDiffMergePropertyValuePair getBasePropertyValuePair() {
        return _basePropertyValuePair;
    }

    public ConfigDiffMergePropertyValuePair getComparedPropertyValuePair() {
        return _comparedPropertyValuePair;
    }

    public String getMergedPropertyValueString() {
        return _mergedPropertyValueString;
    }

    public void setMergedPropertyValueString(String mergedPropertyValueString) {
        _mergedPropertyValueString = mergedPropertyValueString;
    }
}
