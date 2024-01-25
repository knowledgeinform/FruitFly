#include "stdafx.h"
#include "Logger.h"
#include "LockObject.h"
#include "Utils.h"

Logger* Logger::m_Logger = NULL;
int Logger::maxLogMessageSize = 1096;
char* Logger::fullMessage = NULL;
CRITICAL_SECTION Logger::writeLock;
CRITICAL_SECTION Logger::queueLock;
CRITICAL_SECTION Logger::listBoxLock;
int Logger::maxLogFileSize = 10000000;


Logger* Logger::getInstance (string filename)
{
	if (m_Logger == NULL)
	{
		try 
		{
			m_Logger = new Logger (filename);
		}
		catch (exception e)
		{
			char logBuf [256];
			sprintf_s(logBuf, 256, "Logger: Exception creating logging object %s\0", filename);
			
			MessageBox (NULL, logBuf, "Warning!", MB_OK);
			throw e;
		}
	}
	return m_Logger;
}

Logger* Logger::getInstance ()
{
	while (m_Logger == NULL)
	{
		Sleep (100);
	}
	return m_Logger;
}

void Logger::releaseInstance ()
{
	if (m_Logger != NULL)
	{
		delete m_Logger;
	}
}


Logger::Logger (string filename)
{
	int lastSlash = filename.find_last_of ("\\");
	if (lastSlash != string::npos)
	{
		string foldername = filename.substr (0, lastSlash);
		::CreateDirectory (foldername.c_str(), NULL);
	}

	m_Filename = filename;
	m_LogFile = NULL;

	startNewLog ();
	m_KillThread = false;
	m_ThreadDone = false;

	threadParams = new LoggerThreadParams ();
	threadParams->m_Logger = this;
	threadParams->m_KillThread = &m_KillThread;
	threadParams->m_ThreadDone = &m_ThreadDone;

	fullMessage = new char [maxLogMessageSize];
	InitializeCriticalSection (&writeLock);
	InitializeCriticalSection (&queueLock);
	InitializeCriticalSection (&listBoxLock);
	m_ListBoxWnd = NULL;

	m_LogThread = (HANDLE)_beginthread( &Logger::writeThread, 100000, threadParams);
	if (m_LogThread == NULL)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Logger: Unable to start logging thread\0");
		
		MessageBox (NULL, logBuf, "Warning!", MB_OK);
		m_ThreadDone = true;
		return;
	}
}

void Logger::startNewLog ()
{
	totalBytesLogged = 0;

	if (m_LogFile != NULL)
		fclose (m_LogFile);

	setWorkingDirectory ();

	char filename [256];
	sprintf_s (filename, 256, "%s_%d.txt", m_Filename.c_str(), (long)time(NULL));
	int err = fopen_s (&m_LogFile, filename, "w");
	if (m_LogFile == NULL || err != 0)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Logger: Unable to open log file %s\0", filename);
		
		MessageBox (NULL, logBuf, "Warning!", MB_OK);
		m_ThreadDone = true;
		return;
	}

	m_LogOpen = true;
}

Logger::~Logger ()
{
	setListboxWnd (NULL);

	if (m_LogOpen)
		this->close();

	delete [] fullMessage;

	DeleteCriticalSection (&writeLock);
	DeleteCriticalSection (&queueLock);
}

void Logger::close ()
{
	m_LogOpen = false;
	m_KillThread = true;

	while (!m_ThreadDone)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Logger: Waiting for thread to end in destructor\0");
		Logger::getInstance()->immediateLogMessage (logBuf);

		
		Sleep (100);
	}

	char logBuf [256];
	sprintf_s(logBuf, 256, "Logger: Ending in destructor\0");
	Logger::getInstance()->immediateLogMessage (logBuf);

	fclose (m_LogFile);
	delete threadParams;
}

void Logger::setListboxWnd (HWND window) 
{
	LockObject lock (&listBoxLock); 
	m_ListBoxWnd = window;
}

void Logger::logMessage (string message)
{
	LockObject lock (&queueLock);
	m_LogQueue.push_back (message);
}

void Logger::logMessage (char* message)
{
	string msg (message);
	
	LockObject lock (&queueLock);
	m_LogQueue.push_back (msg);
}

void Logger::immediateLogMessage (char* message)
{
	writeToFile (this, message);
}

void Logger::writeToFile (Logger* logger, char* logMessage)
{
	time_t currTimeMillis;
	tm currTimeStruct;
	time (&currTimeMillis);
	localtime_s (&currTimeStruct, &currTimeMillis);
	
	{
		LockObject lock (&writeLock);
		if (logger->getTotalBytesLogged() > maxLogFileSize)
		{
			//Calculate timestamp
			sprintf_s (fullMessage, maxLogMessageSize, "%d/%d/%d %d:%02d:%02d - \0", currTimeStruct.tm_mon+1, currTimeStruct.tm_mday, currTimeStruct.tm_year+1900,
																currTimeStruct.tm_hour, currTimeStruct.tm_min, currTimeStruct.tm_sec);
			
			//Add message to timestamp
			sprintf_s (&fullMessage[strlen(fullMessage)], maxLogMessageSize - strlen(fullMessage), "New log started due to file size limits\n\0");

			//Write to file
			int num = fprintf (logger->getLogFile (), "%s", fullMessage);

			//Start new log
			logger->startNewLog ();
		}



		//Calculate timestamp
		sprintf_s (fullMessage, maxLogMessageSize, "%d/%d/%d %d:%02d:%02d - \0", currTimeStruct.tm_mon+1, currTimeStruct.tm_mday, currTimeStruct.tm_year+1900,
															currTimeStruct.tm_hour, currTimeStruct.tm_min, currTimeStruct.tm_sec);
		//Add message to timestamp
		sprintf_s (&fullMessage[strlen(fullMessage)], maxLogMessageSize - strlen(fullMessage), "%s\n\0", logMessage);

		//Write to file
		int num = fprintf (logger->getLogFile (), "%s", fullMessage);
		fflush (logger->getLogFile ());

		logger->addToTotalBytesLogged (num);
	}

	{
		LockObject lock (&listBoxLock);
		if (logger->m_ListBoxWnd != NULL)
		{
			fullMessage[strlen(fullMessage)-1] = '\0';
			SendMessage (logger->m_ListBoxWnd, LB_ADDSTRING, 0, (LPARAM)fullMessage);
			int lastIndex = (int) SendMessage (logger->m_ListBoxWnd, LB_GETCOUNT, 0, 0) - 1;
			SendMessage (logger->m_ListBoxWnd, LB_SETTOPINDEX, (WPARAM)lastIndex, 0);
		}
	}
}

void Logger::writeThread (void* paramsVd)
{
	try
	{
		bool messageReceived = false;
		char* messageChars = new char[maxLogMessageSize]; 
		LoggerThreadParams* params = (LoggerThreadParams*)paramsVd;
		list <string>* m_LogQueue = params->m_Logger->getQueuePointer ();
		
		while (!*(params->m_KillThread) || m_LogQueue->size() > 0)
		{
			//Put lock in scope to delete it quicker
			{
				LockObject lock (&queueLock);
				if (m_LogQueue->size() > 0)
				{
					//Get message from queue
					string message = m_LogQueue->front();
					memset (messageChars, 0, maxLogMessageSize);
					memcpy (messageChars, message.c_str(), min((int)message.size(), maxLogMessageSize));
					m_LogQueue->pop_front();
					messageReceived = true;
				}
				else
				{
					messageReceived = false;
				}
			}

			if (messageReceived)
			{
				writeToFile (params->m_Logger, (char*)messageChars);
			}
			else
			{
				Sleep (100);
			}	

		}

		delete []messageChars;
		*(params->m_ThreadDone) = true;
		_endthread ();
	}
	catch (exception e)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Logger: Unhandled exception in logging thread: %s\0", e.what());

		MessageBox (NULL, logBuf, "Warning!", MB_OK);
		Logger::getInstance()->immediateLogMessage (logBuf);
		throw e;
	}
}
		