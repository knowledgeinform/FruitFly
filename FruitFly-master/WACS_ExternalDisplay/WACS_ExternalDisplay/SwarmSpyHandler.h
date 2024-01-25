#ifndef SWARMSPYHANDLER_H
#define SWARMSPYHANDLER_H

#include "stdafx.h"
#include "BeliefCollection.h"

/**
	\class SwarmSpyHandler
	\brief Manager for WACS Swarm beliefs coming from SwarmSpy.  Handles incoming beliefs and loads
	them into a BeliefCollection object
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class SwarmSpyHandler
{
	public:
		/**
		\brief Constructor, Stores reference to BeliefCollection
		\param beliefCollection BeliefCollection object to populate with beliefs received
		*/
		SwarmSpyHandler(BeliefCollection* beliefCollection);

		/**
		\brief Destructor
		*/
		~SwarmSpyHandler();

		/**
		\brief Intialize the SwarmSpy instance to listen on a multicast socket for messages
		\param logData If true, all bytes received will be logged to a replayable file
		\param multicastAddress IP Address to connect to
		\param multicastPortNum Port number to connect to
		\return void
		*/
		void initSocketSpy (bool logData, char* multicastAddress, int multicastPortNum);

		/**
		\brief Intialize the SwarmSpy instance to read messages from a log file
		\param logData If true, all bytes read will be logged to a replayable file
		\param filename Filename to read log data from
		\param replaySpeed Replay speed of file reading.  This number scales the delay between sent messages
		\return void
		*/
		void initReplaySpy (bool logData, char* filename, float replaySpeed);

		/**
		\brief If in replay mode, pause the replay.  If not in replay mode, this call has no effect.
		\param pause If true, pause the replay.  If false continue (unpause) replay.
		\return void
		*/
		void pauseReplaySpy (bool pause);

		/**
		\brief If in replay mode, restart the replay from the beginning of the file.  If not in replay mode, this call has no effect.
		\return void
		*/
		void restartReplaySpy ();


		/**
		\brief Register to receive AgentPositionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForAgentPositionBelief();
		
		/**
		\brief Register to receive CircularOrbitBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForCircularOrbitBelief();
		
		/**
		\brief Register to receive PiccoloTelemetryBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForPiccoloTelemetryBelief();
		
		/**
		\brief Register to receive ParticleDetectionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForParticleDetectionBelief();
		
		/**
		\brief Register to receive AnacondaDetectionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForAnacondaDetectionBelief();
		
		/**
		\brief Register to receive AgentModeBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForAgentModeBelief();
		
		/**
		\brief Register to receive CloudDetectionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForCloudDetectionBelief();

		/**
		\brief Register to receive CloudPredictionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForCloudPredictionBelief();

		/**
		\brief Register to receive ExplosionBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForExplosionBelief();

		/**
		\brief Register to receive MetBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForMetBelief();

		/**
		\brief Register to receive RacetrackOrbitBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForRacetrackOrbitBelief();

		/**
		\brief Register to receive IrExplosionAlgorithmEnabledBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForIrExplosionAlgorithmEnabledBelief();

		/**
		\brief Register to receive LoiterApproachPathBelief beliefs from the SwarmSpy to the appropriate 
		global function (defined below)
		\return void
		*/
		void registerForLoiterApproachPathBelief();

		/**
		\brief Restart a new logging file in SwarmSpy.  Will start logging if not already started, or start a new file if started previously.
		\return void
		*/
		void restartSpyLogging ();

	private:


};


/**
\brief Callback function that is called when a AgentPositionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void AgentPositionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a CircularOrbitBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void CircularOrbitBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a PiccoloTelemetryBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void PiccoloTelemetryBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a ParticleDetectionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void ParticleDetectionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a AnacondaDetectionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void AnacondaDetectionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Check code map to convert id code from belief to text string
\param lcdIdCode Code received from belief
\param lcdAgentId String to fill with agent ID text
\return void
*/
void getLcdAgentId (int lcdIdCode, char *lcdAgentId);

/**
\brief Callback function that is called when a AgentModeBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void AgentModeBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a CloudDetectionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void CloudDetectionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a CloudPredictionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void CloudPredictionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a ExplosionBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void ExplosionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a MetBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void MetBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a RacetrackOrbitBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void RacetrackOrbitBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a IrExplosionAlgorithmEnabledBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void IrExplosionAlgorithmEnabledBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);

/**
\brief Callback function that is called when a LoiterApproachPathBelief belief is received.
\param szBeliefName Name of belief received
\param pData Unpacked byte stream with data comprising the given belief.
\param dataLength Length of data in pData
\param timestamp_ms Timestamp of message
\return void
*/
void LoiterApproachPathBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms);


#endif