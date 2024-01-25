#include <Windows.h>
#include <stdio.h>
#include "SwarmSpy.h"
#include "Endian.h"


void AgentPositionBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	short numTimeNames;
	char agentName[64];
	unsigned short agentNameLength;
	char *pRead = (char*)pData;
	long long positionTimestamp_ms;
	float latitude_deg;
	float longitude_deg;
	float altitude_m;
	float heading_deg;
	float error_m;
	int i;

	numTimeNames = ntohs(*(short*)pRead);
	pRead += sizeof (numTimeNames);
	for (i = 0; i < numTimeNames; ++i)
	{
		positionTimestamp_ms = *(long long*)pRead;
		ByteSwapInt64(&positionTimestamp_ms);
		pRead += sizeof(positionTimestamp_ms);

		agentNameLength = ntohs(*(unsigned short*)pRead);
		pRead += sizeof(agentNameLength);

		memcpy(agentName, pRead, agentNameLength);
		pRead += agentNameLength;
		agentName[agentNameLength] = '\0';

		latitude_deg = *(float*)pRead;
		ByteSwapFloat(&latitude_deg);
		pRead += sizeof(latitude_deg);
		
		longitude_deg = *(float*)pRead;
		ByteSwapFloat(&longitude_deg);
		pRead += sizeof(longitude_deg);

		altitude_m = *(float*)pRead;
		ByteSwapFloat(&altitude_m);
		pRead += sizeof(altitude_m);

		heading_deg = *(float*)pRead;
		ByteSwapFloat(&heading_deg);
		pRead += sizeof(heading_deg);

		error_m = *(float*)pRead;
		ByteSwapFloat(&error_m);
		pRead += sizeof(error_m);

		printf("  %lld %s %f %f %f %f %f\n", positionTimestamp_ms, agentName, latitude_deg, longitude_deg, altitude_m, heading_deg, error_m);
	}
}

void CircularOrbitBeliefHandler(const char* const szBeliefName, const unsigned char* const pData, const int dataLength, const long long timestamp_ms)
{
	double radius_m;
	double latitude_deg;
	double longitude_deg;
	double altitude_m;
	unsigned char isClockwise;

	char *pRead = (char*)pData;

	radius_m = *(double*)pRead;
	ByteSwapDouble(&radius_m);
	pRead += sizeof(radius_m);

	latitude_deg = *(double*)pRead;
	ByteSwapDouble(&latitude_deg);
	pRead += sizeof(latitude_deg);

	longitude_deg = *(double*)pRead;
	ByteSwapDouble(&longitude_deg);
	pRead += sizeof(longitude_deg);

	altitude_m = *(double*)pRead;
	ByteSwapDouble(&altitude_m);
	pRead += sizeof(altitude_m);

	isClockwise = *(unsigned char*)pRead;
	pRead += sizeof(isClockwise);

	printf("  %f %f %f %f %s\n", radius_m, latitude_deg, longitude_deg, altitude_m, isClockwise? "CW" : "CCW");
}


int main()
{
	SwarmSpy_Init("224.0.0.175", 38116);

	SwarmSpy_RegisterBeliefHandlerCallback("AgentPositionBelief", AgentPositionBeliefHandler);
	SwarmSpy_RegisterBeliefHandlerCallback("CircularOrbitBelief", CircularOrbitBeliefHandler);

	while (1)
	{
		Sleep(10000);
	}

	return 0;
}