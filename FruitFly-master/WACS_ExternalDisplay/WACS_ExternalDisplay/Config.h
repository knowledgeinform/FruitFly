#ifndef CONFIG_H
#define CONFIG_H

#include "stdafx.h"
#include <iostream>
#include <string>
#include <vector>

using namespace std;


/**
	\class Config
	\brief Config class to store config settings from file and provide access to program.  Can also overwrite config file and save new settings
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class Config {

	public:

		/**
		 \brief Static method to retrieve the singleton instance of this class

		 \param filename If the singleton object doesn't exist yet, the filename to open at
		 \return Pointer to the singleton instance
		 */
		static Config* getInstance (string filename);

		/**
		 \brief Static method to retrieve the singleton instance of this class.  Will lock if the config object
		 hasn't been created yet, by using the getInstance call specifying the filename to open

		 \return Pointer to the singleton instance
		 */
		static Config* getInstance ();

		/**
		 \brief Static method to release the singleton object from memory
		 */
		static void releaseInstance ();

		/**
		 \brief Destructor
		 */
		~Config() {;}
		
		/**
		 \brief Accessor for a config setting, provided as string

		 \param name Name of config setting
		 \param value After return, the desired setting value
		 \param defaultValue Value to use if config value isn't found
		 */
		void getValue( string name, string &value, string defaultValue );
		
		/**
		 \brief Accessor for a config setting, provided as integer

		 \param name Name of config setting
		 \param defaultValue Value to use if config value isn't found
		 \return The desired setting value, cast to an integer
		 */
		int getValueAsInt( string name, int defaultValue );
		
		/**
		 \brief Accessor for a config setting, provided as double

		 \param name Name of config setting
		 \param defaultValue Value to use if config value isn't found
		 \return The desired setting value, cast to an double
		 */
		double getValueAsDouble( string name, double defaultValue );
		
		/**
		 \brief Accessor for a config setting, provided as float

		 \param name Name of config setting
		 \param defaultValue Value to use if config value isn't found
		 \return The desired setting value, cast to an float
		 */
		float getValueAsFloat( string name, float defaultValue );
		
		/**
		 \brief Accessor for a config setting, provided as boolean

		 \param name Name of config setting
		 \param defaultValue Value to use if config value isn't found
		 \return The desired setting value, cast to an boolean
		 */
		bool getValueAsBool( string name, bool defaultValue );

		
		/**
		 \brief Modifier for a config setting, set as string

		 \param name Name of config setting
		 \param value The value to set for the desired setting
		 */
		void setValue( string name, string value );
		
		/**
		 \brief Modifier for a config setting, set as integer

		 \param name Name of config setting
		 \param value The value to set for the desired setting
		 */
		void setValueAsInt( string name, int value );
		
		/**
		 \brief Modifier for a config setting, set as double

		 \param name Name of config setting
		 \param value The value to set for the desired setting
		 */
		void setValueAsDouble( string name, double value );
		
		/**
		 \brief Modifier for a config setting, set as boolean

		 \param name Name of config setting
		 \param value The value to set for the desired setting
		 */
		void setValueAsBool( string name, bool value );

		/**
		 \brief Write config settings back to the file they were read from to save updates for next time
		 */
		void writeOutToFile();

	private:

		/**
		 \brief Constructor, opens config file and reads settings

		 \param filename Filename of string to open - absolute path
		 */
		Config(string filename);

		/**
		 \brief Singleton instance of this class
		 */
		static Config* m_Config;

		
		/**
		 \brief Private accessor for a config setting, provided as string.  Will throw exception if token not found

		 \param name Name of config setting
		 \param value After return, the desired setting value
		 */
		void getValue( string name, string &value);


		/**
		 \brief Read config settings from file

		 \param filename File to read from
		 \return Boolean status of read
		 */
		bool readInConfigValues(string filename);

		/**
		 \brief Write config settings to file

		 \param filename File to write to
		 */
		void printOutConfigValues(string filename);

		/**
		 \brief Filename for the config settings
		 */
		string _configFilename;

		/**
		 \brief Configuration setting names
		 */
		vector<string> _variableNames;
		
		/**
		 \brief Configuration setting values
		 */
		vector<string> _variableValues;
		
};

#endif
