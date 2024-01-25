/*
 * Config.java
 *
 * Created on December 8, 2003, 9:32 AM
 */

package edu.jhuapl.nstd.swarm.util;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Config {

	private static Properties _defaultProps = null;
	private Properties _properties = null;
	private static HashMap<String, Config> _Configs = new HashMap<String, Config>();
	private static Config _defaultConfig;
	private static String _agentName = null;
	private static String _defaultFile = null;
        private static FileWriter m_LogWriter = null;

	private LinkedList _listeners;
        private static boolean LOG_TO_SCREEN = false;

	static
        {
		_agentName = System.getProperty("agent.name");
		if (_agentName == null)
                {
			System.err.println("[warning] You typically specify an agent name as a system property, -Dagent.name=");
		}
		_defaultFile = System.getProperty("defaultFile");
		if (_defaultFile == null)
                {
			_defaultFile = "defaultConfig.txt";
		}
		// not specifying an agent name means that you must always specify it when calling getConfig.

                try
                {
                    File folder = new File ("./ConfigLogs");
                    if (!folder.exists())
                        folder.mkdirs();

                    m_LogWriter = new FileWriter ("./ConfigLogs/ConfigLog_" + System.currentTimeMillis() + ".log");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    m_LogWriter = null;
                }
        }

	public static Config getConfig()
        {
            if (_defaultConfig == null) {
                    _defaultConfig = new Config(null);
            }

            if (_agentName != null && !_Configs.containsKey(_agentName)){
                _Configs.put(_agentName, new Config(_agentName));
            }

            if (_agentName != null)
                    return _Configs.get(_agentName);
            else
            {
                try
                {
                    m_LogWriter.write("No agent name set for Config class.  Using only default properties\r\n");m_LogWriter.flush();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                }
                return _defaultConfig;
            }
	}

	public static Config getConfig(String agentName)
        {
            if (_defaultConfig == null)
            {
                    _defaultConfig = new Config(null);
            }

            if (!_Configs.containsKey(agentName))
            {
                    _Configs.put(agentName, new Config(agentName));
            }

            return _Configs.get(agentName);
	}

	private Config(String name)
        {
            try
            {
                m_LogWriter.write ("Loading default config settings\r\n");m_LogWriter.flush();
                if (_defaultProps == null)
                {
                        String propFileName = _defaultFile;
                        System.err.println("Config: Using default file " + _defaultFile);
                        _defaultProps = new Properties();

                        File file = new File ("./config/" + propFileName);
                        if (!file.exists())
                        {
                            System.err.println("Default Config file " + _defaultFile + " cannot be found in file system.");
                        }
                        else
                        {
                            loadProperties (_defaultProps, file);
                        }
                }

                _properties = new Properties();
                _properties.putAll(_defaultProps);

                m_LogWriter.write ("Done loading default config settings\r\n");m_LogWriter.flush();
                m_LogWriter.write ("Loading agent config settings\r\n");m_LogWriter.flush();
                if (name != null)
                {
                        Properties localProps = new Properties();
                        String propFileName = name+"Config.txt";

                        File file = new File ("./config/" + propFileName);
                        if (!file.exists())
                        {
                            System.err.println("Local Config for " + name + ", " + propFileName + ", cannot be found.");
                        }
                        else
                        {
                            loadProperties (localProps, file);
                            checkDuplicates (_defaultProps, localProps);
                            _properties.putAll(localProps);
                        }
                }
                m_LogWriter.write ("Done loading agent config settings\r\n");m_LogWriter.flush();
            }
            catch (Exception e)
            {
                    e.printStackTrace();
            }

            _listeners = new LinkedList();
	}

        private void loadProperties (Properties props, File inputFile)
        {
            if (props == null || inputFile == null)
            {
                return;
            }
            
            try
            {
                BufferedReader reader = new BufferedReader (new FileReader (inputFile));

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    if (line.trim().startsWith("#"))
                        continue;
                    
                    //Get token
                    int tokenStartIdx = 0;
                    while (tokenStartIdx < line.length() && line.charAt(tokenStartIdx) == ' ') tokenStartIdx ++;
                    int tokenEndIdx = tokenStartIdx +1;
                    while (tokenEndIdx < line.length() && (line.charAt(tokenEndIdx) != ' ' && line.charAt(tokenEndIdx) != '=' && line.charAt(tokenEndIdx) != ':')) tokenEndIdx ++;
                    if (tokenStartIdx >= line.length() || (tokenEndIdx-tokenStartIdx) <= 0)
                    {
                        //no token found.  Skip
                        continue;
                    }
                    String token = line.substring(tokenStartIdx, tokenEndIdx);
                    
                    //Get value
                    int valueStartIdx = tokenEndIdx;
                    while (valueStartIdx < line.length() && (line.charAt(valueStartIdx) == ' ' || line.charAt(valueStartIdx) == '=' || line.charAt(valueStartIdx) == ':')) valueStartIdx ++;
                    int valueEndIdx = valueStartIdx +1;
                    while (valueEndIdx < line.length() && (line.charAt(valueEndIdx) != '\r' && line.charAt(valueEndIdx) != '\n' && line.charAt(valueEndIdx) != '#' && line.charAt(valueEndIdx) != ';')) valueEndIdx ++;
                    if (valueStartIdx >= line.length() || (valueEndIdx-valueStartIdx) <= 0)
                    {
                        //no token found.  Skip
                        continue;
                    }
                    String value = line.substring(valueStartIdx, valueEndIdx).trim();


                    //Load new value
                    String oldValue = props.getProperty(token);
                    if (oldValue != null)
                    {
                        m_LogWriter.write ("Overwriting value for token \'" + token + "\'  Old value \'" + oldValue + "\' : " + value + "\'\r\n");m_LogWriter.flush();
                    }
                    props.setProperty(token, value);

                }
            }
            catch (Exception e)
            {
                try
                {
                    m_LogWriter.write ("Error reading config file: " + inputFile.getAbsolutePath() + "\r\n");m_LogWriter.flush();
                    m_LogWriter.write (e.getMessage() + "\r\n");m_LogWriter.flush();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                System.out.println ("Error reading config file: " + inputFile.getAbsolutePath());
                e.printStackTrace();
            }
        }

        private void checkDuplicates (Properties defaultList, Properties newList) throws IOException
        {
            Set<String> propNames = newList.stringPropertyNames();
            Iterator<String> itr = propNames.iterator();
            while (itr.hasNext())
            {
                String prop = itr.next();

                String oldValue = defaultList.getProperty(prop);
                if (oldValue != null)
                {
                    m_LogWriter.write ("Overwriting value for token \'" + prop + "\'  Old value \'" + oldValue + "\' : " + newList.getProperty(prop) + "\'\r\n");m_LogWriter.flush();
                }

            }

        }

	public boolean hasProperty(String propName) {
		return _properties.containsKey(propName);
	}

	public synchronized String getProperty(String propName){
		String prop = _properties.getProperty(propName);
		if (prop != null){
			prop = prop.trim();
		}
		return prop;
	}

	public synchronized String getProperty(String propName, String defaulted){
		String prop = _properties.getProperty(propName);
		
		if (prop != null)
                {
                    prop = prop.trim();
                    return prop;
		}
		else
                {
                    logDefault (propName, defaulted);

                    return defaulted;
                }
	}

	public synchronized void setProperty(String name, String value)
        {
            String oldValue = _properties.getProperty(name);
            if (value != null && !value.equals(oldValue))
            {
                String logMsg = "Overwriting value for token \'" + name + "\'  Old value \'" + oldValue + "\' : " + value + "\'\r\n";
                try
                {
                    m_LogWriter.write (logMsg + "\r\n");m_LogWriter.flush();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (LOG_TO_SCREEN)
                        System.out.println (logMsg);
                }

                _properties.setProperty(name, value);
                firePropertyChangeEvent(name, oldValue, value);
            }
	}

        public synchronized void setProperties(Properties p)
        {
            //get all of the properties out of the properties parameter
            Enumeration e = p.propertyNames();

            while(e.hasMoreElements())
            {
                String name = (String)e.nextElement();
                String value = p.getProperty(name);
                _properties.setProperty(name, value);
            }
        }

        private void logDefault (String token, String defaultValue)
        {
            String logMsg = "Using default value for token \'" + token + "\' default: \'" + defaultValue + "\'";
            try
            {
                m_LogWriter.write (logMsg + "\r\n");m_LogWriter.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (LOG_TO_SCREEN)
                    System.out.println (logMsg);
            }
        }

	public long getPropertyAsLong(String propName, long defaulted) {
		String prop = getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Long.parseLong(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		}

                logDefault (propName, String.valueOf(defaulted));
		return defaulted;
	}

	public double getPropertyAsDouble(String propName)
        {
		return getPropertyAsDouble(propName, true);
	}

	public double getPropertyAsDouble(String propName, double defaulted) {
		String prop = getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Double.parseDouble(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		}
                logDefault (propName, String.valueOf(defaulted));
		return defaulted;
	}

	public double getPropertyAsDouble(String propName, boolean optional) {
		String prop = Config.getConfig().getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Double.parseDouble(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		} else if (!optional) {
	 		throw new IllegalArgumentException("Configuration property '"
				+ propName + "' must be set.");
		}
		return -1;
	}

        public long getPropertyAsLong(String propName) {
		return getPropertyAsLong(propName, true);
	}

	public long getPropertyAsLong(String propName, int defaulted) {
		String prop = getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Long.parseLong(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		}
                logDefault (propName, String.valueOf(defaulted));
		return defaulted;
	}

		public long getPropertyAsLong(String propName, boolean optional) {
		String prop = Config.getConfig().getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Long.parseLong(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		} else if (!optional) {
                    	throw new IllegalArgumentException("Configuration property '"
				+ propName + "' must be set.");
		}
		return -1;
	}

	public int getPropertyAsInteger(String propName) {
		return getPropertyAsInteger(propName, true);
	}

	public int getPropertyAsInteger(String propName, int defaulted) {
		String prop = getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Integer.parseInt(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		}
                logDefault (propName, String.valueOf(defaulted));
		return defaulted;
	}

	public int getPropertyAsInteger(String propName, boolean optional) {
		String prop = Config.getConfig().getProperty(propName);
		if (prop != null && !prop.equals("")) {
			try {
				return Integer.parseInt(prop);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Configuration property '"
					+ propName + "' must be a number.");
			}
		} else if (!optional) {
	 		throw new IllegalArgumentException("Configuration property '"
				+ propName + "' must be set.");
		}
                return -1;
	}

	public boolean getPropertyAsBoolean(String propName) {
		return getPropertyAsBoolean(propName, false);
	}

	public boolean getPropertyAsBoolean(String propName, boolean defaulted) {
		String prop = getProperty(propName);
		if (prop != null && !prop.equals("")) {
			return new Boolean(prop).booleanValue();
		}
                logDefault (propName, String.valueOf(defaulted));
		return defaulted;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (!_listeners.contains(l))
			_listeners.add(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		_listeners.remove(l);
	}

	protected void firePropertyChangeEvent(String name,
		String oldValue, String newValue)
	{
		PropertyChangeEvent e = new PropertyChangeEvent(
			this, name, oldValue, newValue);
		Iterator itr = _listeners.iterator();
		while (itr.hasNext()) {
			PropertyChangeListener l = (PropertyChangeListener)itr.next();
			l.propertyChange(e);
		}
	}
        
        /** Returns all properties, including the default properties. */
        public Properties getProperties() {
            return _properties;
        }

	public Properties getDefaultProperties() {
		return _defaultProps;
	}

}
