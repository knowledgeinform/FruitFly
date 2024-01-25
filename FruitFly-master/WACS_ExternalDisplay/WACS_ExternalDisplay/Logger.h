#ifndef LOGGER_H
#define LOGGER_H

#include "stdafx.h"
#include <iostream>
#include <string>
#include <list>

using namespace std;
class Logger;

/**
	\struct LoggerThreadParams
	\brief Object to pass required objects into write thread
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
struct LoggerThreadParams
{
	/**
	\brief Constructor
	*/
	LoggerThreadParams () {}

	Logger* m_Logger; //!< Pointer to Logger object to run
	bool* m_KillThread; //!< If true, thread should be killed
	bool* m_ThreadDone; //!< If true, thread has been killed
};

/**
	\class Logger
	\brief Object to log all user actions/errors to a log file
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class Logger
{
	public:

		/**
		 \brief Static method to retrieve the singleton instance of this class

		 \param filename If the singleton object doesn't exist yet, the filename to open at
		 \return Pointer to the singleton instance
		 */
		static Logger* getInstance (string filename);

		/**
		 \brief Static method to retrieve the singleton instance of this class.  Will lock if the config object
		 hasn't been created yet, by using the getInstance call specifying the filename to open

		 \return Pointer to the singleton instance
		 */
		static Logger* getInstance ();

		/**
		 \brief Static method to release the singleton object from memory
		 */
		static void releaseInstance ();

		/**
		 \brief Destructor, makes sure log closes gracefully
		 */
		~Logger ();

		/**
		 \brief Closes log file
		 */
		void close ();
		
		/**
		 \brief Add message to logging queue to be saved on internal thread

		 \param message Message to log, timestamp will be added before the message
		 */
		void logMessage (string message);

		/**
		 \brief Add message to logging queue to be saved on internal thread

		 \param message Message to log, timestamp will be added before the message
		 */
		void logMessage (char* message);

		/**
		 \brief Immediately log message, bypassing the log queue.

		 \param message Message to log, timestamp will be added before the message
		 */
		void immediateLogMessage (char* message);

		/**
		\brief Sets a handle to a listbox window to update with log data, if desired
		\param window Handle to listbox window
		*/
		void setListboxWnd (HWND window);

	private:

		/**
		 \brief Constructor, opens log file and starts write thread

		 \param filename Absolute path of filename to log to
		 */
		Logger (string filename);

		/**
		\brief Starts a new log file
		\return void
		*/
		void startNewLog ();

		/**
		\brief Returns the handle to the current log file
		\return Handle to the current log file
		*/
		FILE* getLogFile () {return m_LogFile;}

		/**
		\brief Returns a pointer to the log queue
		\return pointer to the log queue
		*/
		list <string>* getQueuePointer () {return &m_LogQueue;}

		/**
		\brief Adds timestamp/etc to message and writes to file immediately

		\param logger Logging object with queue and file pointer
		\param logMessage Message to add timestamp to and print
		*/
		static void writeToFile (Logger* logger, char* logMessage);

		/**
		\brief Returns the total number of bytes logged to the current log file
		\return total number of bytes logged to the current log file
		*/
		int getTotalBytesLogged () {return totalBytesLogged;}

		/**
		\brief Adds the parameter value to the running count of written bytes
		\param addBytes Count to add
		*/
		void addToTotalBytesLogged (int addBytes) {totalBytesLogged += addBytes;}

		/**
		\brief Number of bytes logged to the current file
		*/
		int totalBytesLogged;

		/**
		\brief Maximum number of bytes to log to each file
		*/
		static int maxLogFileSize;

		/**
		 \brief Singleton instance of this class
		 */
		static Logger* m_Logger;

		/**
		 \brief Filename of log
		 */
		string m_Filename;

		/**
		 \brief File pointer to log
		 */
		FILE* m_LogFile;

		/**
		\brief Text to update when adding timestamp/etc to log message
		*/
		static char* fullMessage;

		/**
		 \brief Boolean status of log - false once close has been called
		*/
		bool m_LogOpen;

		/**
		 \brief If true, thread should stop writing and shutdown
		 */
		bool m_KillThread;

		/**
		 \brief If true, thread has finished its loop
		 */
		bool m_ThreadDone;

		/**
		 \brief Queue of messages to write to log
		 */
		list <string> m_LogQueue;

		/**
		\brief Maximum size of log messages
		*/
		static int maxLogMessageSize;

		/**
		 \brief Parameters to send to thread function
		 */
		LoggerThreadParams* threadParams;

		/**
		 \brief Internal thread to write messages to log
		 */
		HANDLE m_LogThread;

		/**
		 \brief Thread function to write logs to file
		 */
		static void writeThread (void* params);

		/**
		\brief Locking object for writing
		*/
		static CRITICAL_SECTION writeLock;

		/**
		\brief Locking object for queue
		*/
		static CRITICAL_SECTION queueLock;

		/**
		\brief Locking object for list box
		*/
		static CRITICAL_SECTION listBoxLock;

		/**
		\brief Handle to listbox window to log to (in addition to file)
		*/
		HWND m_ListBoxWnd;

};

#endif