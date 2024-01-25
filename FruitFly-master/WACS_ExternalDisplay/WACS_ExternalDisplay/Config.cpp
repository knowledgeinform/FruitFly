#include "stdafx.h"
#include "Config.h"
#include "Logger.h"


Config* Config::m_Config = NULL;


Config* Config::getInstance (string filename)
{
	if (m_Config == NULL)
	{
		try 
		{
			m_Config = new Config (filename);
		}
		catch (exception e)
		{
			char text [256];
			sprintf_s (text, 256, "Config: Exception creating config object: %s\0", e.what());
			Logger::getInstance()->immediateLogMessage (text);
			throw e;
		}
	}
	return m_Config;
}

Config* Config::getInstance ()
{
	while (m_Config == NULL)
	{
		char text [256];
		sprintf_s (text, 256, "Config: Sleeping while waiting for config object to be created\0");
		Logger::getInstance()->logMessage (text);
		Sleep (100);
	}
	return m_Config;
}

void Config::releaseInstance ()
{
	if (m_Config != NULL)
	{
		delete m_Config;
	}
}


Config::Config(string filename)
{
	_configFilename = filename;
	
	//Read settings, or throw exception if error
	if (!this->readInConfigValues(_configFilename))
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: unable to read config file %s\n", _configFilename.c_str());
		Logger::getInstance()->immediateLogMessage (logBuf);

		throw exception(logBuf);
	}
}

void Config::getValue( string name, string &value, string defaultValue ) {
	bool found = false;

	for (int i=0; i<(int)_variableNames.size(); i++) {
		if (name == _variableNames[i]) {
			found = true;
			value = _variableValues[i];
			break;
		}
	}

	if (!found) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to get value of %s from file, defaulting to %s\0", name.c_str(), defaultValue.c_str());
		Logger::getInstance()->logMessage (logBuf);

		//Use default and move on safely
		value = defaultValue;
	}
}

void Config::getValue( string name, string &value ) {
	bool found = false;

	for (int i=0; i<(int)_variableNames.size(); i++) {
		if (name == _variableNames[i]) {
			found = true;
			value = _variableValues[i];
			break;
		}
	}

	if (!found) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to get value of %s from file\0", name.c_str());
		//Don't log because this should be caught anyway
		
		throw exception (logBuf);
	}
}

int Config::getValueAsInt( string name, int defaultValue) {
	string value;

	try {
		getValue(name,value);
	} catch ( exception e ) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to get value of %s from file, defaulting to %d\0", name.c_str(), defaultValue);
		Logger::getInstance()->logMessage (logBuf);

		return defaultValue;
	}

	return atoi(value.c_str());
}

double Config::getValueAsDouble( string name, double defaultValue ) {
	string value;

	try {
		getValue(name,value);
	} catch ( exception e ) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to get value of %s from file, defaulting to %f\0", name.c_str(), defaultValue);
		Logger::getInstance()->logMessage (logBuf);

		return defaultValue;
	}

	return atof(value.c_str());
}

float Config::getValueAsFloat( string name, float defaultValue) {
	return (float)getValueAsDouble(name, (float)defaultValue);
}

bool Config::getValueAsBool( string name, bool defaultValue ) {
	string value;

	try {
		getValue(name,value);
	} catch ( exception e ) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to get value of %s from file, defaulting to %d\0", name.c_str(), defaultValue);
		Logger::getInstance()->logMessage (logBuf);

		return defaultValue;
	}

	return ( (value=="true") || (value=="True") || (value=="TRUE") );
}

void Config::setValue( string name, string value ) {
	int i;
	bool found = false;

	for (i=0; i<(int)_variableNames.size(); i++) {
		if (name == _variableNames[i]) {
			found = true;
			_variableValues[i] = value;
			break;
		}
	}

	if (!found) {
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: unable to set value of %s\n", name.c_str());
		Logger::getInstance()->immediateLogMessage (logBuf);

		throw exception(logBuf);
	}
}

void Config::setValueAsInt( string name, int value ) {
	char valStr[32];

	sprintf_s(valStr, "%d", value);

	try {
		setValue(name, valStr);
	} catch (exception e) {
		throw e;
	}
}

void Config::setValueAsDouble( string name, double value ) {
	char valStr[32];

	sprintf_s(valStr, "%2.8f", value);

	try {
		setValue(name, valStr);
	} catch (exception e) {
		throw e;
	}
}

void Config::setValueAsBool( string name, bool value ) {
	string valStr;

	valStr = (value ? "true" : "false");

	try {
		setValue(name, valStr);
	} catch (exception e) {
		throw e;
	}
}


bool Config::readInConfigValues(string filename) {
	FILE *fp;
	int i;
	char line[1024], c;
	bool eof=false, ret=true;
	string strval;

	int err = fopen_s(&fp, filename.c_str(), "r" );
	if (!fp || err != 0) 
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: Unable to open Config file: %s\n", filename.c_str());
		Logger::getInstance()->immediateLogMessage (logBuf);
		sprintf_s(logBuf, 256, "Config: Error code: %d\n", err);
		Logger::getInstance()->immediateLogMessage (logBuf);

		//We'll continue and just use the defaults hardcoded in
		return true;
	}

	while (1) {

		i = 0; c = '\0';
		while (c != '\n' && c != '\r') {
			if (fscanf_s(fp, "%c", &c) == EOF) {
				eof = true;
				break;
			}
			line[i++] = c;
		}
		if (eof)
			break;

		line[i-1] = '\0'; /* overwrite the '\n' or '\r' */
		if (line[0] == '#' || strlen(line)==0)
			continue;

		//printf("LINE = %s\n", line);

		if (!strstr(line, "=")) {

			char logBuf [256];
			sprintf_s(logBuf, 256, "Config: Config file unable to find = in valid line: %s\n", line);
			Logger::getInstance()->logMessage (logBuf);
		
			continue;
		}

		// now we can throw in the new value
		i=0; strval = "";
		while (line[i]==' ') i++;
		while (line[i]!=' ' && line[i]!='=')
			strval += line[i++];
		_variableNames.insert(_variableNames.end(), strval);

		while (line[i] != '=') i++;
		while (line[i]==' ' || line[i]=='=')
			i++;

		_variableValues.insert(_variableValues.end(), &(line[i]));
	}
	fclose(fp);

	return ret; 
}

void Config::writeOutToFile() {
	printOutConfigValues(_configFilename);
}

void Config::printOutConfigValues(string filename) {
	FILE *fp;

	fopen_s( &fp, filename.c_str(), "w" );
	if (!fp) 
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Config: unable to open Config file for writing: %s\n", filename.c_str());
		Logger::getInstance()->immediateLogMessage (logBuf);

		return;
	}

	for (int i=0; i<(int)_variableNames.size(); i++) {
		fprintf(fp, "%s = %s\n", _variableNames[i].c_str(), _variableValues[i].c_str());
	}
	fclose(fp);
}
