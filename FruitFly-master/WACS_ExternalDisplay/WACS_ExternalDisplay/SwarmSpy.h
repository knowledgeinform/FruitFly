#ifndef SwarmSpyH
#define SwarmSpyH

#include "stdafx.h"

/**
\brief Template for a callback function that is called when a specific belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
typedef void (*BeliefHandlerCallbackFunc)(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);


/**
\brief Initialize Swarm spy connection.  Create necessary threads and start running.

\param address This is the IP address to connect to
\param port This is the port number to connect to
\param logData If true, all bytes passing through the Swarm Spy will be logged to a timestamped file to be replayed
\return void
*/
void SwarmSpy_InitLive(const char* const address, const int port, const bool logData);

/**
\brief Initialize Swarm spy replay thread.  Create necessary threads and start running.

\param filename This is the filename to read from
\param replaySpeed This is the replay speed factor
\param logData If true, all bytes passing through the Swarm Spy will be logged to a timestamped file to be replayed using this function
\return void
*/
void SwarmSpy_InitReplay(const char* const filename, const float replaySpeed, const bool logData);

/**
\brief Stop threads.
\return void
*/
void SwarmSpy_Reset();

/**
\brief If in replay mode, pause the replay.  If not in replay mode, this has no effect.
\param pause If true, pause the replay.  If false continue (unpause) replay.
\return void
*/
void SwarmSpy_PauseReplay (bool pause);

/**
\brief If in replay mode, restart the replay from the beginning of the file.  If not in replay mode, this call has no effect.
\return void
*/
void SwarmSpy_RestartReplay ();

/**
\brief Stop threads and destroy callback list
\return void
*/
void SwarmSpy_Destroy();

/**
\brief Restart (or start for the first time) logging Spy data in a new log file
\return void
*/
void SwarmSpy_RestartLogging ();

/**
\brief Register a callback function for the Spy for a specific belief name
\param szBeliefName Name of belief to register for
\param callbackFunc Function to call when specific belief is received
\return void
*/
void SwarmSpy_RegisterBeliefHandlerCallback(const char* const szBeliefName, BeliefHandlerCallbackFunc callbackFunc);


#endif