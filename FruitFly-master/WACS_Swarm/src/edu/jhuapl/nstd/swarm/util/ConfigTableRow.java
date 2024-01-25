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

import java.util.Comparator;

/**
 * Class used to store the contents of each row in the Config Table.
 * @author olsoncc1
 */
public class ConfigTableRow implements Comparable<ConfigTableRow> {

    private String _propertyName;
    private String _propertyValue;
    private String _configFile;
    private Boolean _overridden;
    private static final int EQUAL = 0;

    public ConfigTableRow(String propertyName, String propertyValue, String configFile, Boolean overridden) {
        this._propertyName = propertyName;
        this._propertyValue = propertyValue;
        this._configFile = configFile;
        this._overridden = overridden;
    }

    public Boolean isOverridden() {
        return _overridden;
    }

    public void setOverridden(Boolean overridden) {
        this._overridden = overridden;
    }

    public String getConfigFile() {
        return _configFile;
    }

    public void setConfigFile(String configFile) {
        this._configFile = configFile;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public void setPropertyName(String propertyName) {
        this._propertyName = propertyName;
    }

    public String getPropertyValue() {
        return _propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this._propertyValue = propertyValue;
    }

    /** 
     * Default sort by property name, then by config file, 
     * overridden flag, and finally by the property value. 
     * The sorting algorithm is case-insensitive.
     */
    @Override
    public int compareTo(ConfigTableRow aThat) {
        if (this == aThat) {
            return EQUAL;
        }

        int comparison = this._propertyName.toLowerCase().compareTo(aThat._propertyName.toLowerCase());
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = this._configFile.toLowerCase().compareTo(aThat._configFile.toLowerCase());
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = this._overridden.compareTo(aThat._overridden);
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = this._propertyValue.toLowerCase().compareTo(aThat._propertyValue.toLowerCase());
        if (comparison != EQUAL) {
            return comparison;
        }

        return EQUAL;
    }
    
    /** 
     * Sort by config file, then by the overridden flag, property name,
     * and finally by the property value.
     * The sorting algorithm is case-insensitive.
     */
    static Comparator<ConfigTableRow> CONFIG_FILE_SORT = new Comparator<ConfigTableRow>() {

        @Override
        public int compare(ConfigTableRow aThis, ConfigTableRow aThat) {
            if (aThis == aThat) {
                return EQUAL;
            }

            int comparison = aThis._configFile.toLowerCase().compareTo(aThat._configFile.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._overridden.compareTo(aThat._overridden);
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._propertyName.toLowerCase().compareTo(aThat._propertyName.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._propertyValue.toLowerCase().compareTo(aThat._propertyValue.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            return EQUAL;
        }
    };
  
  /** 
   * Sort by the overridden flag, then by the config file, property name,
   * and then finally by the property value.
   * The sorting algorithm is case-insensitive.
   */
  static Comparator<ConfigTableRow> OVERRIDDEN_SORT = new Comparator<ConfigTableRow>() {

        @Override
        public int compare(ConfigTableRow aThis, ConfigTableRow aThat) {
            if (aThis == aThat) {
                return EQUAL;
            }

            int comparison = aThis._overridden.compareTo(aThat._overridden);
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._configFile.toLowerCase().compareTo(aThat._configFile.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._propertyName.toLowerCase().compareTo(aThat._propertyName.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._propertyValue.toLowerCase().compareTo(aThat._propertyValue.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            return EQUAL;
        }
    };
  
   /**
    * Sort by property value, then by property name,
    * config file, and finally by the overridden flag.
    * The sorting algorithm is case-insensitive.
    */
    static Comparator<ConfigTableRow> PROP_VAL_SORT = new Comparator<ConfigTableRow>() {

        @Override
        public int compare(ConfigTableRow aThis, ConfigTableRow aThat) {
            if (aThis == aThat) {
                return EQUAL;
            }

            int comparison = aThis._propertyValue.toLowerCase().compareTo(aThat._propertyValue.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._propertyName.toLowerCase().compareTo(aThat._propertyName.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._configFile.toLowerCase().compareTo(aThat._configFile.toLowerCase());
            if (comparison != EQUAL) {
                return comparison;
            }

            comparison = aThis._overridden.compareTo(aThat._overridden);
            if (comparison != EQUAL) {
                return comparison;
            }

            return EQUAL;
        }
    };
}
