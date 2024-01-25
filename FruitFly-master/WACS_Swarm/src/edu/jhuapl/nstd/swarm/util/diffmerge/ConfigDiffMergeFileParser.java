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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is used as a configuration file parser tailored from the 
 * <code>edu.jhuapl.nstd.swarm.util.Config</code> class.
 * 
 * @author olsoncc1
 */
public class ConfigDiffMergeFileParser {    
    /**
     * Modified from Config.loadProperties(Properties props, File inputFile)
     * 
     * @param inputFile
     * @return 
     */
    public List<ConfigDiffMergePropertyValuePair> loadProperties(File inputFile) {
        int lineNumber = -1;
        List<ConfigDiffMergePropertyValuePair> properties = new ArrayList<ConfigDiffMergePropertyValuePair>();
        
        if (inputFile == null || !inputFile.exists())
        {
            return properties;
        }
        
        try
        {
            BufferedReader reader = new BufferedReader (new FileReader(inputFile));

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim(); // Initialize the next line in the file
                lineNumber++;       // Increase the line counter
                
                if (line.equals("") || line.startsWith("#")) {
                    properties.add(new ConfigDiffMergePropertyValuePair(line, lineNumber));
                    continue;
                }

                // Get token
                int tokenStartIdx = 0;
                while (tokenStartIdx < line.length() && line.charAt(tokenStartIdx) == ' ') tokenStartIdx ++;
                int tokenEndIdx = tokenStartIdx +1;
                while (tokenEndIdx < line.length() && (line.charAt(tokenEndIdx) != ' ' && line.charAt(tokenEndIdx) != '=' && line.charAt(tokenEndIdx) != ':')) tokenEndIdx ++;
                if (tokenStartIdx >= line.length() || (tokenEndIdx-tokenStartIdx) <= 0)
                {
                    //no token found.  Skip
                    properties.add(new ConfigDiffMergePropertyValuePair(line, lineNumber));
                    continue;
                }
                String token = line.substring(tokenStartIdx, tokenEndIdx);

                // Get value
                int valueStartIdx = tokenEndIdx;
                while (valueStartIdx < line.length() && (line.charAt(valueStartIdx) == ' ' || line.charAt(valueStartIdx) == '=' || line.charAt(valueStartIdx) == ':')) valueStartIdx ++;
                int valueEndIdx = valueStartIdx +1;
                while (valueEndIdx < line.length() && (line.charAt(valueEndIdx) != '\r' && line.charAt(valueEndIdx) != '\n' && line.charAt(valueEndIdx) != '#' && line.charAt(valueEndIdx) != ';')) valueEndIdx ++;
                if (valueStartIdx >= line.length() || (valueEndIdx-valueStartIdx) <= 0)
                {
                    //no token found.  Skip
                    properties.add(new ConfigDiffMergePropertyValuePair(line, lineNumber));
                    continue;
                }
                String value = line.substring(valueStartIdx, valueEndIdx).trim();

                // Store the token and value
               properties.add(new ConfigDiffMergePropertyValuePair(token, value, line, lineNumber));
            }
        }
        catch (Exception e)
        {
            Logger.getLogger("GLOBAL").info("Error reading config file: " + inputFile.getAbsolutePath() + "\r\n");
            Logger.getLogger("GLOBAL").info(e.getMessage() + "\r\n");
        }
        
        return properties;
    } // end loadProperties()
}
