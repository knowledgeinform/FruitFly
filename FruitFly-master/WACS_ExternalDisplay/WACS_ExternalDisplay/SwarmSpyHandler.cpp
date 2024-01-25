#include "stdafx.h"
#include "SwarmSpyHandler.h"
#include "SwarmSpy.h"
#include "Endian.h"
#include <iostream>
#include <iomanip>
#include "math.h"
#include "Config.h"
using namespace std;


BeliefCollection* localBeliefCollection = NULL;
long long lastAgentPositionBeliefTimestampMs;
long long lastGCSPositionBeliefTimestampMs;
long long lastCircularOrbitBeliefTimestampMs;
long long lastPiccoloTelemetryBeliefTimestampMs;
long long lastParticleDetectionBeliefTimestampMs;
long long lastAnacondaDetectionBeliefTimestampMs;
long long lastAgentModeBeliefTimestampMs;
long long lastCloudDetectionBeliefTimestampMs[CloudDetection::NUM_CLOUDDETECTIONS_SOURCES];
long long lastCloudPredictionBeliefTimestampMs;
long long lastExplosionBeliefTimestampMs;
long long lastMetBeliefTimestampMs;
long long lastRacetrackOrbitBeliefTimestampMs;
long long lastIrExplosionAlgorithmEnabledBeliefTimestampMs;
long long lastLoiterApproachPathBeliefTimestampMs;
long long replayStartTimestampMs;

const int numChemPosToCheckOverlap = 5;
int m_MinAnacondaScoreToKeep = 0;
float lastChemLat[numChemPosToCheckOverlap], lastChemLon[numChemPosToCheckOverlap], lastChemAlt[numChemPosToCheckOverlap];

const int AGENTIDCODEPAIRS_LISTSIZE = 64;
char m_AgentIDCodePairs [AGENTIDCODEPAIRS_LISTSIZE][16]; //!< List of character strings that convert agent id code to agent id text



/**
\brief Swaps bytes in a short value
\param val short value to swap
\return swapped short value
*/
short ntohs(short val)
{
	unsigned char hold;
	unsigned char* c = (unsigned char*) (&val);
	hold = c[0];
	c[0] = c[1];
	c[1] = hold;

	return val;
}

SwarmSpyHandler::SwarmSpyHandler(BeliefCollection* beliefCollection)
{
	if (localBeliefCollection == NULL)
		localBeliefCollection = beliefCollection;

	m_MinAnacondaScoreToKeep = Config::getInstance()->getValueAsInt("SwarmSpyHandler.MinAnacondaScoreToKeep", 0);

	char configName [128];
	for (int i = 0; i < AGENTIDCODEPAIRS_LISTSIZE; i ++)
	{
		sprintf_s (configName, 128, "podAction.anaconda.agent%d\0", i);
		string name (configName);
		string value = "";
		string defval = "Unk";
		Config::getInstance()->getValue (name, value, defval);

		sprintf_s (m_AgentIDCodePairs[i], 16, "%s\0", value.c_str());
	}

	m_MinAnacondaScoreToKeep = m_MinAnacondaScoreToKeep;
}

void SwarmSpyHandler::initSocketSpy (bool logData, char* multicastAddress, int multicastPortNum)
{
	SwarmSpy_Reset();
	SwarmSpy_InitLive(multicastAddress, multicastPortNum, logData);
}

void SwarmSpyHandler::initReplaySpy (bool logData, char* filename, float replaySpeed)
{
	SwarmSpy_Reset();
	SwarmSpy_InitReplay(filename, replaySpeed, logData);

	//restart replay to get everything initialized properly
	restartReplaySpy ();
}

void SwarmSpyHandler::pauseReplaySpy (bool pause)
{
	SwarmSpy_PauseReplay (pause);
}

void SwarmSpyHandler::restartReplaySpy ()
{
	SwarmSpy_RestartReplay ();

	replayStartTimestampMs = -1;
	lastAgentPositionBeliefTimestampMs = -1;
	lastGCSPositionBeliefTimestampMs = -1;
	lastCircularOrbitBeliefTimestampMs = -1;
	lastPiccoloTelemetryBeliefTimestampMs = -1;
	lastParticleDetectionBeliefTimestampMs = -1;
	lastAnacondaDetectionBeliefTimestampMs = -1;
	lastAgentModeBeliefTimestampMs = -1;
	for (int i = 0; i < CloudDetection::NUM_CLOUDDETECTIONS_SOURCES; i ++)
		lastCloudDetectionBeliefTimestampMs[i] = -1;
	lastCloudPredictionBeliefTimestampMs = -1;
	lastExplosionBeliefTimestampMs = -1;
	lastMetBeliefTimestampMs = -1;
	lastRacetrackOrbitBeliefTimestampMs = -1;
	lastIrExplosionAlgorithmEnabledBeliefTimestampMs = -1;
	lastLoiterApproachPathBeliefTimestampMs = -1;

	for (int i = 0; i < numChemPosToCheckOverlap; i ++)
	{
		lastChemLat[i] = 0;
		lastChemLon[i] = 0;
		lastChemAlt[i] = 0;
	}
}

SwarmSpyHandler::~SwarmSpyHandler ()
{
	//Unregister for beliefs
	SwarmSpy_RegisterBeliefHandlerCallback("AgentPositionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("CircularOrbitBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("PiccoloTelemetryBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("ParticleDetectionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("AnacondaDetectionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("AgentModeBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("CloudDetectionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("CloudPredictionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("ExplosionBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("METBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("RacetrackOrbitBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("IrExplosionAlgorithmEnabledBelief", NULL);
	SwarmSpy_RegisterBeliefHandlerCallback("LoiterApproachPathBelief", NULL);

	SwarmSpy_Destroy();
}

void SwarmSpyHandler::registerForAgentPositionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("AgentPositionBelief", AgentPositionBeliefHandler);
}

void SwarmSpyHandler::registerForCircularOrbitBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("CircularOrbitBelief", CircularOrbitBeliefHandler);
}

void SwarmSpyHandler::registerForPiccoloTelemetryBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("PiccoloTelemetryBelief", PiccoloTelemetryBeliefHandler);
}

void SwarmSpyHandler::registerForParticleDetectionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("ParticleDetectionBelief", ParticleDetectionBeliefHandler);
}

void SwarmSpyHandler::registerForAgentModeBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("AgentModeBelief", AgentModeBeliefHandler);
}

void SwarmSpyHandler::registerForCloudDetectionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("CloudDetectionBelief", CloudDetectionBeliefHandler);
}

void SwarmSpyHandler::registerForAnacondaDetectionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("AnacondaDetectionBelief", AnacondaDetectionBeliefHandler);
}

void SwarmSpyHandler::registerForCloudPredictionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("CloudPredictionBelief", CloudPredictionBeliefHandler);
}

void SwarmSpyHandler::registerForExplosionBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("ExplosionBelief", ExplosionBeliefHandler);
}

void SwarmSpyHandler::registerForMetBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("METBelief", MetBeliefHandler);
}

void SwarmSpyHandler::registerForRacetrackOrbitBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("RacetrackOrbitBelief", RacetrackOrbitBeliefHandler);
}

void SwarmSpyHandler::registerForIrExplosionAlgorithmEnabledBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("IrExplosionAlgorithmEnabledBelief", IrExplosionAlgorithmEnabledBeliefHandler);
}

void SwarmSpyHandler::registerForLoiterApproachPathBelief()
{
	SwarmSpy_RegisterBeliefHandlerCallback("LoiterApproachPathBelief", LoiterApproachPathBeliefHandler);
}

void SwarmSpyHandler::restartSpyLogging ()
{
	SwarmSpy_RestartLogging();
}



void AgentPositionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	short numTimeNames;
	char agentName[64];
	unsigned short agentNameLength;
	unsigned char *pRead = (unsigned char*)pData;
	long long positionTimestamp_ms;
	float latitude_degFloat;
	float longitude_degFloat;
	float altitude_mFloat;
	float heading_degFloat;
	float error_mFloat;
	int latitude_degInt;
	int longitude_degInt;
	int altitude_mInt;
	int heading_degInt;
	int error_mInt;
	int i;
	
	ShadowAgentPositionBelief* newAgentPositionBelief = NULL;
	GCSAgentPositionBelief* newGCSPositionBelief = NULL;

	numTimeNames = ntohs(*(short*)pRead);
	pRead += sizeof (numTimeNames);
	for (i = 0; i < numTimeNames; ++i)
	{
		positionTimestamp_ms = *(long long*)pRead;
		EndianUtility::ByteSwapInt64(&positionTimestamp_ms);
		pRead += sizeof(positionTimestamp_ms);

		agentNameLength = ntohs(*(unsigned short*)pRead);
		pRead += sizeof(agentNameLength);

		memcpy(agentName, pRead, agentNameLength);
		pRead += agentNameLength;
		agentName[agentNameLength] = '\0';

		latitude_degInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&latitude_degInt);
		latitude_degFloat = *(float*)&latitude_degInt;
		pRead += sizeof(latitude_degInt);
		
		longitude_degInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&longitude_degInt);
		longitude_degFloat = *(float*)&longitude_degInt;
		pRead += sizeof(longitude_degInt);

		altitude_mInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&altitude_mInt);
		altitude_mFloat = *(float*)&altitude_mInt;
		pRead += sizeof(altitude_mInt);

		heading_degInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&heading_degInt);
		heading_degFloat = *(float*)&heading_degInt;
		pRead += sizeof(heading_degInt);

		error_mInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&error_mInt);
		error_mFloat = *(float*)&error_mInt;
		pRead += sizeof(error_mInt);


		if (strcmp (agentName, "wacsagent\0") == 0)
		{
			if (lastAgentPositionBeliefTimestampMs < timestamp_ms)
			{
				AgentPosition* newAgentPosition = new AgentPosition (positionTimestamp_ms, latitude_degFloat, longitude_degFloat, altitude_mFloat, heading_degFloat);
				if (newAgentPositionBelief == NULL)
					newAgentPositionBelief = new ShadowAgentPositionBelief ();
				
				newAgentPositionBelief->addAgentPosition (newAgentPosition);
				lastAgentPositionBeliefTimestampMs = timestamp_ms;
			}
		}
		else if (strcmp (agentName, "display\0") == 0)
		{
			if (lastGCSPositionBeliefTimestampMs < timestamp_ms)
			{
				AgentPosition* newGCSPosition = new AgentPosition (positionTimestamp_ms, latitude_degFloat, longitude_degFloat, altitude_mFloat, heading_degFloat);
				if (newGCSPositionBelief == NULL)
					newGCSPositionBelief = new GCSAgentPositionBelief ();
				
				newGCSPositionBelief->addAgentPosition (newGCSPosition);
				lastGCSPositionBeliefTimestampMs = timestamp_ms;
			}
		}

	}

	if (newAgentPositionBelief != NULL && localBeliefCollection != NULL)
		localBeliefCollection->updateAgentPositionBelief (newAgentPositionBelief);
	if (newGCSPositionBelief != NULL && localBeliefCollection != NULL)
		localBeliefCollection->updateGCSPositionBelief (newGCSPositionBelief);
}


void CircularOrbitBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	double radius_m;
	double latitude_deg;
	double longitude_deg;
	double altitude_m;
	unsigned char isClockwise;

	char *pRead = (char*)pData;

	radius_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&radius_m);
	pRead += sizeof(radius_m);

	latitude_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&latitude_deg);
	pRead += sizeof(latitude_deg);

	longitude_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&longitude_deg);
	pRead += sizeof(longitude_deg);

	altitude_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&altitude_m);
	pRead += sizeof(altitude_m);

	isClockwise = *(unsigned char*)pRead;
	pRead += sizeof(isClockwise);

	
	if (lastCircularOrbitBeliefTimestampMs < timestamp_ms)
	{
		CircularOrbitBelief* newBelief = new CircularOrbitBelief (latitude_deg, longitude_deg, radius_m, altitude_m, isClockwise==1);
		if (localBeliefCollection != NULL)
		{
			localBeliefCollection->updateCircularOrbitBelief (newBelief);
			lastCircularOrbitBeliefTimestampMs = timestamp_ms;
		}
	}
}

void RacetrackOrbitBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	double radius_m;
	double latitude1_deg;
	double longitude1_deg;
	double latitude2_deg;
	double longitude2_deg;
	double finalAltitude_m;
	double standoffAltitude_m;
	unsigned char isClockwise;

	char *pRead = (char*)pData;

	latitude1_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&latitude1_deg);
	pRead += sizeof(latitude1_deg);

	longitude1_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&longitude1_deg);
	pRead += sizeof(longitude1_deg);

	latitude2_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&latitude2_deg);
	pRead += sizeof(latitude2_deg);

	longitude2_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&longitude2_deg);
	pRead += sizeof(longitude2_deg);

	finalAltitude_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&finalAltitude_m);
	pRead += sizeof(finalAltitude_m);

	standoffAltitude_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&standoffAltitude_m);
	pRead += sizeof(standoffAltitude_m);

	radius_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&radius_m);
	pRead += sizeof(radius_m);

	isClockwise = *(unsigned char*)pRead;
	pRead += sizeof(isClockwise);

	
	if (lastRacetrackOrbitBeliefTimestampMs < timestamp_ms)
	{
		RacetrackOrbitBelief* newBelief = new RacetrackOrbitBelief (latitude1_deg, longitude1_deg, latitude2_deg, longitude2_deg, radius_m, finalAltitude_m, standoffAltitude_m, isClockwise==1);
		if (localBeliefCollection != NULL)
		{
			localBeliefCollection->updateRacetrackOrbitBelief (newBelief);
			lastRacetrackOrbitBeliefTimestampMs = timestamp_ms;
		}
	}
}

void PiccoloTelemetryBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	double altitudeWGS84_m;
	double altitudeMSL_m;
	int gpsStatus;
	double indicatedAirSpeed_mps;
	double latitude_deg;
	double longitude_deg;	
	double gpsPDOP;
	double pitch_deg;
	double roll_deg;
	double trueHeading_deg;
	double velocityDown_mps;
	double velocityEast_mps;
	double velocityNorth_mps;
	double windFromSouth_mps;
	double windFromWest_mps;
	double yaw_deg;
	double staticPressure_pa;
	double outsideAirTemperature_C;
	double altLaser_m;
	bool altLaserValid;
	
	char *pRead = (char*)pData;


	altitudeWGS84_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&altitudeWGS84_m);
	pRead += sizeof(altitudeWGS84_m);

	if (timestamp_ms > 1334269000000) //Messages before Apr 12, 2012 didn't include this field
	{
		altitudeMSL_m = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&altitudeMSL_m);
		pRead += sizeof(altitudeMSL_m);
	}
	else
	{
		altitudeMSL_m = altitudeWGS84_m;
	}

	gpsStatus = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&gpsStatus);
	pRead += sizeof(gpsStatus);
	
	indicatedAirSpeed_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&indicatedAirSpeed_mps);
	pRead += sizeof(indicatedAirSpeed_mps);

	latitude_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&latitude_deg);
	pRead += sizeof(latitude_deg);

	longitude_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&longitude_deg);
	pRead += sizeof(longitude_deg);

	gpsPDOP = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&gpsPDOP);
	pRead += sizeof(gpsPDOP);

	pitch_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&pitch_deg);
	pRead += sizeof(pitch_deg);

	roll_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&roll_deg);
	pRead += sizeof(roll_deg);

	trueHeading_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&trueHeading_deg);
	pRead += sizeof(trueHeading_deg);

	velocityDown_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&velocityDown_mps);
	pRead += sizeof(velocityDown_mps);

	velocityEast_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&velocityEast_mps);
	pRead += sizeof(velocityEast_mps);

	velocityNorth_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&velocityNorth_mps);
	pRead += sizeof(velocityNorth_mps);

	windFromSouth_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&windFromSouth_mps);
	pRead += sizeof(windFromSouth_mps);

	windFromWest_mps = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&windFromWest_mps);
	pRead += sizeof(windFromWest_mps);

	yaw_deg = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&yaw_deg);
	pRead += sizeof(yaw_deg);

	staticPressure_pa = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&staticPressure_pa);
	pRead += sizeof(staticPressure_pa);

	outsideAirTemperature_C = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&outsideAirTemperature_C);
	pRead += sizeof(outsideAirTemperature_C);

	altLaser_m = *(double*)pRead;
	EndianUtility::ByteSwapDouble(&altLaser_m);
	pRead += sizeof(altLaser_m);

	altLaserValid = *(bool*)pRead;
	pRead += sizeof(altLaserValid);

	if (lastPiccoloTelemetryBeliefTimestampMs < timestamp_ms)
	{
		PiccoloTelemetryBelief* newBelief = new PiccoloTelemetryBelief (altitudeWGS84_m, altitudeMSL_m, gpsStatus, indicatedAirSpeed_mps, latitude_deg, longitude_deg, gpsPDOP, pitch_deg, roll_deg, trueHeading_deg, velocityDown_mps, velocityEast_mps, velocityNorth_mps, windFromSouth_mps, windFromWest_mps, yaw_deg, staticPressure_pa, outsideAirTemperature_C, altLaser_m, altLaserValid);
		if (localBeliefCollection != NULL)
		{
			localBeliefCollection->updatePiccoloTelemetryBelief (newBelief);
			lastPiccoloTelemetryBeliefTimestampMs = timestamp_ms;
		}
	}
}

void ParticleDetectionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	if (timestamp_ms < 1364575006000L)
	{
		//Old belief format
		int size = 0;
		char msg[256];

		char *pRead = (char*)pData;

		size = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&size);
		pRead += sizeof(size);

		memcpy_s (&msg[0], sizeof(msg), &pRead[0], size);
		pRead += size;
		msg[size] = '\0';

		if (lastParticleDetectionBeliefTimestampMs < timestamp_ms)
		{
			ParticleDetectionBelief* newBelief = new ParticleDetectionBelief (msg);
			localBeliefCollection->updateParticleDetectionBelief (newBelief);
			lastParticleDetectionBeliefTimestampMs = timestamp_ms;
		}
	}
	else
	{
		//New belief format
		int largeCounts;
		int smallCounts;
		int bioLargeCounts;
		int bioSmallCounts;
		char *pRead = (char*)pData;

		largeCounts = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&largeCounts);
		pRead += sizeof(largeCounts);

		smallCounts = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&smallCounts);
		pRead += sizeof(smallCounts);

		bioLargeCounts = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&bioLargeCounts);
		pRead += sizeof(bioLargeCounts);

		bioSmallCounts = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&bioSmallCounts);
		pRead += sizeof(bioSmallCounts);
	

		if (lastParticleDetectionBeliefTimestampMs < timestamp_ms)
		{
			ParticleDetectionBelief* newBelief = new ParticleDetectionBelief (largeCounts, smallCounts, bioLargeCounts, bioSmallCounts);
			localBeliefCollection->updateParticleDetectionBelief (newBelief);
			lastParticleDetectionBeliefTimestampMs = timestamp_ms;
		}
	}
	
}

void AnacondaDetectionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	AnacondaDetectionBelief* newBelief = NULL;

	if (timestamp_ms < 1364575006000L)
	{
		//Old belief format
		int size = 0;
		char msg[256];

		char *pRead = (char*)pData;


		size = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&size);
		pRead += sizeof(size);

		memcpy_s (&msg[0], sizeof(msg), &pRead[0], size);
		pRead += size;
		msg[size] = '\0';

		if (lastAnacondaDetectionBeliefTimestampMs < timestamp_ms)
		{
			newBelief = new AnacondaDetectionBelief (msg);
		}
	}
	else
	{
		//New belief format
		int lcdaCount;
		int lcdaBars = 0;
		int lcdaIdCode = 0;
		char *pRead = (char*)pData;

		lcdaCount = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&lcdaCount);
		pRead += sizeof(lcdaCount);

		for (int i = 0; i < lcdaCount; i ++)
		{
			int testBars = 0;
			int testIdCode = 0;

			testBars = *(int*)pRead;
			EndianUtility::ByteSwapInt32(&testBars);
			pRead += sizeof(testBars);

			testIdCode = *(int*)pRead;
			EndianUtility::ByteSwapInt32(&testIdCode);
			pRead += sizeof(testIdCode);

			if (testBars > lcdaBars)
			{
				lcdaBars = testBars;
				lcdaIdCode = testIdCode;
			}
		}

		int lcdbCount;
		int lcdbBars = 0;
		int lcdbIdCode = 0;
		
		lcdbCount = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&lcdbCount);
		pRead += sizeof(lcdbCount);

		for (int i = 0; i < lcdbCount; i ++)
		{
			int testBars = 0;
			int testIdCode = 0;

			testBars = *(int*)pRead;
			EndianUtility::ByteSwapInt32(&testBars);
			pRead += sizeof(testBars);

			testIdCode = *(int*)pRead;
			EndianUtility::ByteSwapInt32(&testIdCode);
			pRead += sizeof(testIdCode);

			if (testBars > lcdbBars)
			{
				lcdbBars = testBars;
				lcdbIdCode = testIdCode;
			}
		}

		if (lastAnacondaDetectionBeliefTimestampMs < timestamp_ms)
		{
			char lcdaAgentId [8] = "\0";
			if (lcdaBars > 0)
				getLcdAgentId (lcdaIdCode, lcdaAgentId);
			char lcdbAgentId [8] = "\0";
			if (lcdbBars > 0)
				getLcdAgentId (lcdbIdCode, lcdbAgentId);
			newBelief = new AnacondaDetectionBelief (lcdaAgentId, lcdaBars, lcdbAgentId, lcdbBars);
		}
	}


	if (newBelief != NULL)
	{
		if (newBelief->getLcdaBars() < m_MinAnacondaScoreToKeep)
		{
			newBelief->setLcdaBars (0);
			newBelief->setLcdaAgentID ("\0");
		}
		if (newBelief->getLcdbBars() < m_MinAnacondaScoreToKeep)
		{
			newBelief->setLcdbBars (0);
			newBelief->setLcdbAgentID ("\0");
		}

		localBeliefCollection->updateAnacondaDetectionBelief (newBelief);
		lastAnacondaDetectionBeliefTimestampMs = timestamp_ms;
	}
}

void getLcdAgentId (int lcdIdCode, char *lcdAgentId)
{
	if (lcdIdCode < 0 || lcdIdCode >= AGENTIDCODEPAIRS_LISTSIZE)
		sprintf (lcdAgentId, "Unk\0");
	else
		sprintf (lcdAgentId, "%s\0", m_AgentIDCodePairs[lcdIdCode]);
}

void AgentModeBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	short numTimeNames;
	char agentName[64];
	unsigned short agentNameLength;
	char *pRead = (char*)pData;
	long long positionTimestamp_ms;
	short modeLength = 0;
	char mode[128];
	int i;
	
	AgentModeBelief* newAgentModeBelief = NULL; 

	numTimeNames = ntohs(*(short*)pRead);
	pRead += sizeof (numTimeNames);
	for (i = 0; i < numTimeNames; ++i)
	{
		positionTimestamp_ms = *(long long*)pRead;
		EndianUtility::ByteSwapInt64(&positionTimestamp_ms);
		pRead += sizeof(positionTimestamp_ms);

		agentNameLength = ntohs(*(unsigned short*)pRead);
		pRead += sizeof(agentNameLength);

		memcpy(agentName, pRead, agentNameLength);
		pRead += agentNameLength;
		agentName[agentNameLength] = '\0';

		modeLength = *(short*)pRead;
		EndianUtility::ByteSwapInt16(&modeLength);
		pRead += sizeof(modeLength);

		memcpy(mode, pRead, modeLength);
		pRead += modeLength;
		mode[modeLength] = '\0';


		if (strcmp (agentName, "wacsagent\0") == 0)
		{
			if (lastAgentModeBeliefTimestampMs < timestamp_ms && newAgentModeBelief == NULL)
			{
				newAgentModeBelief = new AgentModeBelief (positionTimestamp_ms, mode);
				lastAgentModeBeliefTimestampMs = timestamp_ms;
			}
		}
	}

	if (newAgentModeBelief != NULL && localBeliefCollection != NULL)
			localBeliefCollection->updateAgentModeBelief (newAgentModeBelief);
}


void CloudDetectionBeliefHandler (const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs >= 0 && lastCloudDetectionBeliefTimestampMs[0] <= 0)
	{
		for (int i = 0; i < CloudDetection::NUM_CLOUDDETECTIONS_SOURCES; i ++)
			lastCloudDetectionBeliefTimestampMs[i] = replayStartTimestampMs; //Set last cloud detection timestamp to first timestamp in file - prevents old cloud detections from being displayed
	}
	else if (replayStartTimestampMs <= 0)
		return;   //Don't use the first cloud detection belief if it's the first message in the file.


	unsigned short numDetections;
	int latInt, lonInt, altInt;
	int valueInt;
	float latFloat, lonFloat, altFloat;
	float valueFloat;
	long long timeMs;
	int source;
	int id = 0;
	char *pRead = (char*)pData;
	
	numDetections = ntohs(*(unsigned short*)pRead);
	pRead += sizeof(numDetections);
	
	long long newCloudDetectionBeliefTimestampMs[CloudDetection::NUM_CLOUDDETECTIONS_SOURCES];
	for (int i = 0; i < CloudDetection::NUM_CLOUDDETECTIONS_SOURCES; i ++)
		newCloudDetectionBeliefTimestampMs[i] = lastCloudDetectionBeliefTimestampMs[i];
	
	for (int i = 0; i < numDetections; ++i)
	{
		latInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&latInt);
		latFloat = *(float*)&latInt;
		pRead += sizeof(latInt);
		
		lonInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&lonInt);
		lonFloat = *(float*)&lonInt;
		pRead += sizeof(lonInt);
		
		altInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&altInt);
		altFloat = *(float*)&altInt;
		pRead += sizeof(altInt);
		
		valueInt = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&valueInt);
		valueFloat = *(float*)&valueInt;
		pRead += sizeof(valueInt);
		
		timeMs = *(long long*)pRead;
		EndianUtility::ByteSwapInt64(&timeMs);
		pRead += sizeof(timeMs);

		source = *(int*)pRead;
		EndianUtility::ByteSwapInt32(&source);
		pRead += sizeof(source);

		if (timestamp_ms > 1364575006000L)
		{
			//New belief format
			id = *(int*)pRead;
			EndianUtility::ByteSwapInt32(&id);
			pRead += sizeof(id);
		}

		if (timeMs > lastCloudDetectionBeliefTimestampMs[source])
		{
			if (source == CloudDetection::CHEMICAL_DETECTION)
			{
				bool ignoreDetection = false;
				for (int i = 0; i < numChemPosToCheckOverlap; i ++)
				{
					//Check to make sure we don't overlap multiple chemical detections.  Check the last X detections to see if we already have marked this location.
					if ((fabs(latFloat - lastChemLat[i]) < 0.00001) && 
						(fabs(lonFloat - lastChemLon[i]) < 0.00001) && 
						(fabs(altFloat - lastChemAlt[i]) < 0.00001))
						ignoreDetection = true;    
				}
				if ((int)valueFloat < m_MinAnacondaScoreToKeep)
					ignoreDetection = true;

				if (ignoreDetection)
					continue;  //In this case, we recently had a detection at the current location.  Ignore it and check the next one
	
				for (int i = numChemPosToCheckOverlap-1; i > 0; i --)
				{
					lastChemLat[i] = lastChemLat[i-1];
					lastChemLon[i] = lastChemLon[i-1];
					lastChemAlt[i] = lastChemAlt[i-1];
				}
				lastChemLat[0] = latFloat;
				lastChemLon[0] = lonFloat;
				lastChemAlt[0] = altFloat;
				
			}

			localBeliefCollection->updateCloudDetectionBelief (new CloudDetection (timeMs, latFloat, lonFloat, altFloat, valueFloat, source, id));
			newCloudDetectionBeliefTimestampMs[source] = max(timeMs, newCloudDetectionBeliefTimestampMs[source]);
		}
	}

	for (int i = 0; i < CloudDetection::NUM_CLOUDDETECTIONS_SOURCES; i ++)
		lastCloudDetectionBeliefTimestampMs[i] = newCloudDetectionBeliefTimestampMs[i];
}

void CloudPredictionBeliefHandler (const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	char *pRead = (char*)pData;
	
	long long nwbLatInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&nwbLatInt);
	double nwbLat = *(double*)&nwbLatInt;
	pRead += sizeof(nwbLatInt);

	long long nwbLonInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&nwbLonInt);
	double nwbLon = *(double*)&nwbLonInt;
	pRead += sizeof(nwbLonInt);

	long long nwbAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&nwbAltInt);
	double nwbAltM = *(double*)&nwbAltInt * 0.3048;
	pRead += sizeof(nwbAltInt);

	long long setLatInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&setLatInt);
	double setLat = *(double*)&setLatInt;
	pRead += sizeof(setLatInt);

	long long setLonInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&setLonInt);
	double setLon = *(double*)&setLonInt;
	pRead += sizeof(setLonInt);

	long long setAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&setAltInt);
	double setAltM = *(double*)&setAltInt * 0.3048;
	pRead += sizeof(setAltInt);

	int xCells = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&xCells);
	pRead += sizeof(xCells);

	int yCells = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&yCells);
	pRead += sizeof(yCells);

	long long cellSizeInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&cellSizeInt);
	double cellSizeM = *(double*)&cellSizeInt;
	pRead += sizeof(cellSizeInt);

	long long minAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&minAltInt);
	double minAltM = *(double*)&minAltInt;
	pRead += sizeof(minAltInt);

	long long maxAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&maxAltInt);
	double maxAltM = *(double*)&maxAltInt;
	pRead += sizeof(maxAltInt);

	long long currLatInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currLatInt);
	double currLat = *(double*)&currLatInt;
	pRead += sizeof(currLatInt);

	long long currLonInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currLonInt);
	double currLon = *(double*)&currLonInt;
	pRead += sizeof(currLonInt);

	long long currAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currAltInt);
	double currAltM = *(double*)&currAltInt;
	pRead += sizeof(currAltInt);

	long long interceptLatInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&interceptLatInt);
	double interceptLat = *(double*)&interceptLatInt;
	pRead += sizeof(interceptLatInt);

	long long interceptLonInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&interceptLonInt);
	double interceptLon = *(double*)&interceptLonInt;
	pRead += sizeof(interceptLonInt);

	long long interceptAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&interceptAltInt);
	double interceptAltM = *(double*)&interceptAltInt;
	pRead += sizeof(interceptAltInt);

	//not parsing matrix data
	

	if (timestamp_ms > lastCloudPredictionBeliefTimestampMs)
	{
		//Add belief to collection
		localBeliefCollection->updateCloudPredictionBelief (new CloudPredictionBelief (nwbLat,
																						nwbLon,
																						nwbAltM,
																						setLat,
																						setLon,
																						setAltM,
																						xCells,
																						yCells,
																						cellSizeM,
																						minAltM,
																						maxAltM,
																						currLat,
																						currLon,
																						currAltM,
																						interceptLat,
																						interceptLon,
																						interceptAltM));
		lastCloudPredictionBeliefTimestampMs = timestamp_ms;
	}
}


void ExplosionBeliefHandler (const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	char *pRead = (char*)pData;
	
	long long msgTimestampMs = *(long long*) pRead;
	EndianUtility::ByteSwapInt64(&msgTimestampMs);
	pRead += sizeof(msgTimestampMs);

	long long currLatInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currLatInt);
	double currLat = *(double*)&currLatInt;
	pRead += sizeof(currLatInt);

	long long currLonInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currLonInt);
	double currLon = *(double*)&currLonInt;
	pRead += sizeof(currLonInt);

	long long currAltInt = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&currAltInt);
	double currAltM = *(double*)&currAltInt;
	pRead += sizeof(currAltInt);

	
	if (timestamp_ms > lastExplosionBeliefTimestampMs)
	{
		//Add belief to collection
		localBeliefCollection->updateExplosionBelief (new ExplosionBelief (msgTimestampMs, currLat, currLon, currAltM));
		lastExplosionBeliefTimestampMs = timestamp_ms;
	}
}

void MetBeliefHandler (const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	char *pRead = (char*)pData;

	//Read number of timenames
	short numElementsShort = *(short*)pRead;
	EndianUtility::ByteSwapInt16(&numElementsShort);
	int numElements = *(short*)&numElementsShort;
	pRead += sizeof(numElementsShort);

	//Read timename header stuff
	long long timeBytes = *(long long*)pRead;
	EndianUtility::ByteSwapInt64(&timeBytes);
	long long timestamp = *(long long*)&timeBytes;
	pRead += sizeof(timeBytes);

	short strLengthShort = *(short*)pRead;
	EndianUtility::ByteSwapInt16(&strLengthShort);
	int strLength = *(short*)&strLengthShort;
	pRead += sizeof(strLengthShort);

	pRead += strLength;
	

	//Read Met specific data, assume first timename is valid
	int windBearingInt = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&windBearingInt);
	double windBearingDeg = *(float*)&windBearingInt;
	pRead += sizeof(windBearingInt);
	windBearingDeg = fmod(windBearingDeg + 180, 360);

	int windSpeedInt = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&windSpeedInt);
	double windSpeedMps = *(float*)&windSpeedInt;
	pRead += sizeof(windSpeedInt);

	int temperatureInt = *(int*)pRead;
	EndianUtility::ByteSwapInt32(&temperatureInt);
	double temperatureF = *(float*)&temperatureInt;
	pRead += sizeof(temperatureInt);

	
	if (timestamp_ms > lastMetBeliefTimestampMs)
	{
		//Add belief to collection
		localBeliefCollection->updateMetBelief (new MetBelief (windBearingDeg, windSpeedMps, temperatureF));
		lastMetBeliefTimestampMs = timestamp_ms;
	}
}

void IrExplosionAlgorithmEnabledBeliefHandler (const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	char *pRead = (char*)pData;
	
	bool enabled = *(unsigned char*)pRead;
	pRead += sizeof(enabled);

	long long timeUntilExplosionMs = *(long long*) pRead;
	EndianUtility::ByteSwapInt64(&timeUntilExplosionMs);
	pRead += sizeof(timeUntilExplosionMs);
	
	if (timestamp_ms > lastIrExplosionAlgorithmEnabledBeliefTimestampMs)
	{
		//Add belief to collection
		localBeliefCollection->updateIrExplosionAlgorithmEnabledBelief (new IrExplosionAlgorithmEnabledBelief (enabled, timeUntilExplosionMs));
		lastIrExplosionAlgorithmEnabledBeliefTimestampMs = timestamp_ms;
	}
}

void LoiterApproachPathBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	if (replayStartTimestampMs <= 0)
		replayStartTimestampMs = timestamp_ms;

	unsigned char goodData;
	double latitude_deg1,latitude_deg2,latitude_deg3;
	double longitude_deg1,longitude_deg2,longitude_deg3;
	double altitude_m1,altitude_m2,altitude_m3;
	unsigned char isValid;

	char *pRead = (char*)pData;

	goodData = *(unsigned char*)pRead;
	pRead += sizeof(goodData);
	if (goodData == 0)
	{
		latitude_deg1 = 0;
		longitude_deg1 = 0;
		altitude_m1 = -1000;
	}
	else
	{
		latitude_deg1 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&latitude_deg1);
		pRead += sizeof(latitude_deg1);

		longitude_deg1 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&longitude_deg1);
		pRead += sizeof(longitude_deg1);

		altitude_m1 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&altitude_m1);
		pRead += sizeof(altitude_m1);		
	}

	goodData = *(unsigned char*)pRead;
	pRead += sizeof(goodData);
	if (goodData == 0)
	{
		latitude_deg2 = 0;
		longitude_deg2 = 0;
		altitude_m2 = -1000;
	}
	else
	{
		latitude_deg2 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&latitude_deg2);
		pRead += sizeof(latitude_deg2);

		longitude_deg2 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&longitude_deg2);
		pRead += sizeof(longitude_deg2);

		altitude_m2 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&altitude_m2);
		pRead += sizeof(altitude_m2);		
	}

	goodData = *(unsigned char*)pRead;
	pRead += sizeof(goodData);
	if (goodData == 0)
	{
		latitude_deg3 = 0;
		longitude_deg3 = 0;
		altitude_m3 = -1000;
	}
	else
	{
		latitude_deg3 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&latitude_deg3);
		pRead += sizeof(latitude_deg3);

		longitude_deg3 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&longitude_deg3);
		pRead += sizeof(longitude_deg3);

		altitude_m3 = *(double*)pRead;
		EndianUtility::ByteSwapDouble(&altitude_m3);
		pRead += sizeof(altitude_m3);		
	}

	isValid = *(unsigned char*)pRead;
	pRead += sizeof(isValid);

	
	if (lastLoiterApproachPathBeliefTimestampMs < timestamp_ms)
	{
		LoiterApproachPathBelief* newBelief = new LoiterApproachPathBelief (latitude_deg1, longitude_deg1, altitude_m1, 
																			latitude_deg2, longitude_deg2, altitude_m2, 
																			latitude_deg3, longitude_deg3, altitude_m3, 
																			isValid==1);

		if (localBeliefCollection != NULL)
		{
			localBeliefCollection->updateLoiterApproachPathBelief(newBelief);
			lastLoiterApproachPathBeliefTimestampMs = timestamp_ms;
		}
	}
}

