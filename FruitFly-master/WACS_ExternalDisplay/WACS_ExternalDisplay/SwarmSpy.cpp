#include "stdafx.h"
#include <stdio.h>
#include <WinSock2.h>
#include <Ws2tcpip.h> //Required for multicast functionality
#include "pthread.h"
#include "hashtab.h" //hash table
#include "SwarmSpy.h"
#include "zlib.h"
#include "Endian.h"
#include "Config.h"
#include <sys/timeb.h>
#include "Logger.h"

#define SWARM_SPY_RECV_BUFFER_SIZE 1024
#define SWARM_SPY_LOG_QUEUE_SIZE 256
unsigned char pLogBuffer[SWARM_SPY_LOG_QUEUE_SIZE][SWARM_SPY_RECV_BUFFER_SIZE];
unsigned int pLogBufferSize[SWARM_SPY_LOG_QUEUE_SIZE];
_timeb pLogBufferTimestamp[SWARM_SPY_LOG_QUEUE_SIZE];
static int logBufferFirstIdx = -1;
static int logBufferUsedCount = -1;
static pthread_mutex_t log_mutex = PTHREAD_MUTEX_INITIALIZER;
static bool logData = false;
FILE* logFilePointer = NULL;
pthread_t logThread;
pthread_t receiveThread;
bool logThreadStarted = false;
bool replayPaused = false;
bool restartReplay = false;


static hashtab_t *s_pBeliefHandlerHashTable;
static pthread_mutex_t s_mutex = PTHREAD_MUTEX_INITIALIZER;
static char s_szSpyInfoArg[1024];
static char s_szLogInfoArg[1024];
static bool killThreads = false;
static int m_MaxReplayDelayMs = 5000;


void SwarmSpy_RegisterBeliefHandlerCallback(const char* const szBeliefName, BeliefHandlerCallbackFunc callbackFunc)
{
	pthread_mutex_lock(&s_mutex);
	ht_insert(s_pBeliefHandlerHashTable, (void*)szBeliefName, strlen(szBeliefName), (void*)&callbackFunc, sizeof(callbackFunc));
	pthread_mutex_unlock(&s_mutex);
}

int SwarmSpy_DecompressPacket(const unsigned char* const pCompressedData, const int compressedDataSize, unsigned char* const pDecompressionBuffer, const int decompressionBufferSize)
{
	int zlibCode;
	z_stream zlibStream;
    zlibStream.zalloc = Z_NULL;
    zlibStream.zfree = Z_NULL;
    zlibStream.opaque = Z_NULL;
    zlibStream.avail_in = 0;
    zlibStream.next_in = Z_NULL;
    if (inflateInit2(&zlibStream, 16+MAX_WBITS) != Z_OK)
	{
		fprintf(stderr, "ERROR: Could not initialize zlib stream\n");
		return -1;
	}

	zlibStream.avail_in = compressedDataSize;
	zlibStream.next_in = (Bytef*)pCompressedData;
	zlibStream.avail_out = decompressionBufferSize;
	zlibStream.next_out = pDecompressionBuffer;

	zlibCode = inflate(&zlibStream, Z_FINISH);
	inflateEnd(&zlibStream);

	if (zlibCode < 0)
	{
		fprintf(stderr, "ERROR: Error decompressing packet: %d\n", zlibCode);
		return -1;
	}

	return 0;
}

bool SwarmSpy_ParsePacket(const unsigned char* const pPacket, const int packetSize)
{
    const int MAX_OPTION_LEN = 127;
    const unsigned char NOF_MASK = (char) 0x80;  //"next option flag" mask - indicates whether there is another option after this one.
    const int OPTION_LEN_OFFSET = 0;
    const int OPTION_TYPE_OFFSET = 1;
    const int OPTION_VALUE_OFFSET = 2;
	int packetByteIndex;
	int optionByteIndex;
	char szBeliefName[256];
	unsigned short beliefNameLength;
	void *pHashedCallbackPointer;
	#define SWARM_SPY_DECOMPRESSION_BUFFER_SIZE 8192
	unsigned char pDecompressionBuffer[SWARM_SPY_DECOMPRESSION_BUFFER_SIZE];
	const unsigned char *pUncompressedPacket;
	char szPublishingAgentName[256];
	unsigned short publishingAgentNameLength;
	long long timestamp_ms;
	long long transmissionTime_ms;
	bool retVal = false;

	if ((pPacket[0] & 0x1C) == 0x04)
	{
		//This is a compressed packet
		if (SwarmSpy_DecompressPacket(&pPacket[1], packetSize - 1, pDecompressionBuffer, SWARM_SPY_DECOMPRESSION_BUFFER_SIZE) != 0)
		{
			return false;
		}
		pUncompressedPacket = pDecompressionBuffer;
		packetByteIndex = 1;
	}
	else
	{
		pUncompressedPacket = pPacket;
		packetByteIndex = 2;
	}

	// Read header options	
	if ((pPacket[0] & NOF_MASK) == NOF_MASK)
	{	
		do
		{
			optionByteIndex = packetByteIndex;
			packetByteIndex += ((int) (pUncompressedPacket[optionByteIndex] & (~NOF_MASK))) + OPTION_VALUE_OFFSET;
		}
		while ((pUncompressedPacket[optionByteIndex] & NOF_MASK) == NOF_MASK);
	}

	// I'm not sure what these 8 bytes are.
	packetByteIndex += 8;

	beliefNameLength = ntohs(*(unsigned short*)&pUncompressedPacket[packetByteIndex]);
	packetByteIndex += sizeof(beliefNameLength);
	memcpy(szBeliefName, &pUncompressedPacket[packetByteIndex], beliefNameLength);
	szBeliefName[beliefNameLength] = '\0';
	beliefNameLength = strlen(szBeliefName);
	
	//printf("Received Belief: %s\n", szBeliefName);
	packetByteIndex += beliefNameLength;

	timestamp_ms = *(long long*)&pUncompressedPacket[packetByteIndex];
	EndianUtility::ByteSwapInt64(&timestamp_ms);
	packetByteIndex += sizeof(timestamp_ms);
	transmissionTime_ms = *(long long*)&pUncompressedPacket[packetByteIndex];
	EndianUtility::ByteSwapInt64(&transmissionTime_ms);
	packetByteIndex += sizeof(transmissionTime_ms);

	publishingAgentNameLength = ntohs(*(unsigned short*)&pUncompressedPacket[packetByteIndex]);
	packetByteIndex += sizeof(publishingAgentNameLength);
	if (publishingAgentNameLength > sizeof (szPublishingAgentName))
	{
		//fprintf(stderr, "  Found java-serialized packet: %s. Ignoring.\n", szBeliefName);
		return false;
	}
	memcpy(szPublishingAgentName, (char*)&pUncompressedPacket[packetByteIndex], publishingAgentNameLength);
	szPublishingAgentName[publishingAgentNameLength] = '\0';
	packetByteIndex += publishingAgentNameLength;

	pthread_mutex_lock(&s_mutex);
	if (!killThreads)
		pHashedCallbackPointer = ht_search(s_pBeliefHandlerHashTable, (void*)szBeliefName, beliefNameLength);
	else
		pHashedCallbackPointer  = NULL;
	pthread_mutex_unlock(&s_mutex);


	if (pHashedCallbackPointer != NULL)
	{
		if (strcmp (szBeliefName, "AgentPositionBelief") == 0 || strcmp (szBeliefName, "METBelief") == 0)
			retVal = false;
		else
			retVal = true;
	
		(**(BeliefHandlerCallbackFunc*)pHashedCallbackPointer)(szBeliefName, &pUncompressedPacket[packetByteIndex], packetSize - packetByteIndex, timestamp_ms);
	}
	return retVal;
}

void SwarmSpy_QueueLogPacket(const unsigned char* const pPacket, const int packetSize)
{
	if (logBufferFirstIdx < 0)
	{
		//Log thread never started.  Don't log anything.
		return;
	}

	pthread_mutex_lock(&log_mutex);

	//If buffer is full, skip ahead and log message
	if (logBufferUsedCount == SWARM_SPY_LOG_QUEUE_SIZE)
	{
		fprintf(stderr, "Overlapping log buffer, dropping message!\n");
		logBufferFirstIdx = (logBufferFirstIdx + 1)%SWARM_SPY_LOG_QUEUE_SIZE;
		logBufferUsedCount --;
	}

	//Store message in log buffer.  This might be outside the mutex because the size counter won't be updated
	//until the data is valid.  This will prevent the log thread from taking the message until it's ready.
	int logIdx = (logBufferFirstIdx + logBufferUsedCount)%SWARM_SPY_LOG_QUEUE_SIZE;
	memcpy (&pLogBuffer[logIdx][0], &pPacket[0], packetSize);
	pLogBuffer[logIdx][packetSize] = '\0';
	pLogBufferSize[logIdx] = packetSize;
	_ftime (&pLogBufferTimestamp[logIdx]);
	logBufferUsedCount ++;

	pthread_mutex_unlock(&log_mutex);
}

void* SwarmSpy_ReceiveThreadProc(void *arg)
{
	typedef enum {OAM = 0x0, APP_PERIODIC = 0x01, APP_ON_DEMAND = 0x02} SwarmPacketCategory;
	unsigned char pReceiveBuffer[SWARM_SPY_RECV_BUFFER_SIZE];
	const char* multicastAddress;
	short multicastPort;
	struct ip_mreq multicastGroupInfo;
	const int reusePortFlag = 1;
	SOCKET hSocket;
	struct sockaddr_in localAddr;
	int connected = 1;
	int numBytesReceived;
	//SwarmPacketCategory packetCategory;

	


	// Extract address and port values from argument string
	multicastAddress = strtok((char*)arg, ":");
	multicastPort = atoi(strtok(NULL, ":"));



	//
	// Create the socket
	//
	hSocket = socket (AF_INET, SOCK_DGRAM, 0);
	if (hSocket <  0)
	{
		fprintf(stderr, "Error creating socket\n");
		return NULL;
	}


	//
	// Allow other applications to bind to this same multicast port
	//
	if (setsockopt(hSocket, SOL_SOCKET, SO_REUSEADDR, (char *)&reusePortFlag, sizeof(reusePortFlag)) != 0)
	{
		fprintf(stderr, "Error making port reusable\n");
		closesocket(hSocket);
		return NULL;
	}


	//
	// Bind to the local port
	//
	memset((char *) &localAddr, 0, sizeof(localAddr));
	localAddr.sin_family = AF_INET;
	localAddr.sin_port = htons(multicastPort);
	localAddr.sin_addr.s_addr  = INADDR_ANY;
	if (bind(hSocket, (struct sockaddr*)&localAddr, sizeof(localAddr)))
	{
		fprintf(stderr, "binding datagram socket");
		closesocket(hSocket);
		return NULL;
	}


	//
	// Join the multicast group
	//
    multicastGroupInfo.imr_multiaddr.s_addr = inet_addr(multicastAddress);
    multicastGroupInfo.imr_interface.s_addr = INADDR_ANY;
	if (setsockopt(hSocket, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char*)&multicastGroupInfo, sizeof(multicastGroupInfo)) != 0)
	{
		fprintf(stderr, "Error joining multicast group\n");
		closesocket(hSocket);
		return NULL;
	}
	

	//
	// Read socket data
	//
	do
	{
		numBytesReceived = recv(hSocket, (char*)pReceiveBuffer, SWARM_SPY_RECV_BUFFER_SIZE, 0);
		if (numBytesReceived <= 0)
		{
			connected = 0;
		}
		else
		{			
			SwarmSpy_QueueLogPacket (pReceiveBuffer, numBytesReceived);
			SwarmSpy_ParsePacket(pReceiveBuffer, numBytesReceived);
		}
	}
	while (connected && !killThreads);

	closesocket(hSocket);
	WSACleanup();
	return NULL;
}

void SwarmSpy_OpenLogFile (char* filename)
{
	if (logFilePointer != 0)
		fclose (logFilePointer);

	logFilePointer = fopen (filename, "wb");
}

void* SwarmSpy_LogThreadProc(void *arg)
{
	logThreadStarted = true;
	char filename[1024];
	sprintf_s (&filename[0], 1024, "%s\0", (char*)arg);

	bool connected = true;

	//
	// Log data
	//
	do
	{
		if (logBufferUsedCount > 0)
		{
			pthread_mutex_lock(&log_mutex);
			
				
			if (fwrite((const void*) &pLogBufferTimestamp[logBufferFirstIdx],sizeof(_timeb),1,logFilePointer) == 0)
				connected = false;
			if (fwrite((const void*) &pLogBufferSize[logBufferFirstIdx],sizeof(int),1,logFilePointer) == 0)
				connected = false;
			if (fwrite ((const void*) &pLogBuffer[logBufferFirstIdx][0], sizeof(char), pLogBufferSize[logBufferFirstIdx], logFilePointer) == 0)
				connected = false;

			logBufferFirstIdx = (logBufferFirstIdx + 1)%SWARM_SPY_LOG_QUEUE_SIZE;
			logBufferUsedCount--;

			pthread_mutex_unlock(&log_mutex);
		}
	}
	while (connected && !killThreads);

	fclose (logFilePointer);

	return NULL;
}

void SwarmSpy_RestartLogging ()
{
	pthread_mutex_lock(&log_mutex);

	sprintf_s (s_szLogInfoArg, 1024, "./SwarmLogs/Log_%d.log", time (NULL));

	logBufferFirstIdx = 0;
	logBufferUsedCount = 0;

	SwarmSpy_OpenLogFile (s_szLogInfoArg);

	pthread_mutex_unlock(&log_mutex);
	
	if (!logThreadStarted)
	{
		// Startup the log thread
		logThreadStarted = true;
		pthread_create(&logThread, NULL, SwarmSpy_LogThreadProc, s_szLogInfoArg);
	}
}

void* SwarmSpy_ReplayThreadProc(void *arg)
{
	unsigned char pReadBuffer[SWARM_SPY_RECV_BUFFER_SIZE];
	int numBytesRead;
	int connected = 1;
	
	const char* filename = strtok((char*)arg, ":");
	float replaySpeed = atof(strtok(NULL, ":"));

	FILE* fp = fopen (filename, "rb");
	fseek(fp, 0L, SEEK_END);
	int sz = ftell(fp);
	fseek(fp, 0L, SEEK_SET);
		
	
	_timeb currMsgTimestamp;
	_timeb lastMsgTimestamp;
	_timeb currActTimestamp;
	_timeb lastActTimestamp;
	
	int msgLength;
	float reqElapsedTimeMs = 0;
	long minSleepTimeMs = Config::getInstance()->getValueAsInt ("SwarmSpy.ReplayThread.MinSleepMs", 50);
	
	//
	// Read log file data
	//
	do
	{	
		if (replayPaused)
		{
			Sleep (100);
			continue;
		}

		if (restartReplay)
		{
			fseek(fp, 0L, SEEK_SET);
			restartReplay = false;
			connected = 1;

			_ftime (&currActTimestamp);
			_ftime (&lastActTimestamp);
			lastMsgTimestamp.time = -1;
		}

		if (!connected)
		{
			//Maintain thread now, so we can restart replay at the beginning without restarting thread.
			Sleep (100);
			continue;
		}

		if (fread ((void*)&currMsgTimestamp, sizeof(_timeb), 1, fp) != 1)
		{
			connected = 0;
			continue;
		}
		if (fread ((void*)&msgLength, sizeof(int), 1, fp) != 1)
		{
			connected = 0;
			continue;
		}
		if (fread ((void*)&pReadBuffer, sizeof(char), msgLength, fp) != msgLength)
		{
			connected = 0;
			continue;
		}

		SwarmSpy_QueueLogPacket (pReadBuffer, msgLength);
		if (SwarmSpy_ParsePacket(pReadBuffer, msgLength))
		{
			//Only throttle replay speed on messages that are not agent position beleif.  This eliminates the long delay
			//in the beginning when we just get GCS position beliefs
			_ftime (&currActTimestamp);
			if (lastMsgTimestamp.time == -1)
				lastMsgTimestamp = currMsgTimestamp;

			long msgElapsedTimeMs = (currMsgTimestamp.time*1000+currMsgTimestamp.millitm) - (lastMsgTimestamp.time*1000+lastMsgTimestamp.millitm);
			long actElapsedTimeMs = (currActTimestamp.time*1000+currActTimestamp.millitm) - (lastActTimestamp.time*1000+lastActTimestamp.millitm);
			reqElapsedTimeMs += msgElapsedTimeMs/replaySpeed - actElapsedTimeMs;
			if (reqElapsedTimeMs < 0)
				reqElapsedTimeMs = 0;
			if (reqElapsedTimeMs > m_MaxReplayDelayMs)
				reqElapsedTimeMs = m_MaxReplayDelayMs;
			
			if (reqElapsedTimeMs > minSleepTimeMs)
			{
				Sleep ((long)reqElapsedTimeMs);
				reqElapsedTimeMs -= (long)reqElapsedTimeMs;
			}
			
			lastActTimestamp = currActTimestamp;
			lastMsgTimestamp = currMsgTimestamp;
		}
	}
	while (/*connected && */!killThreads);

	fclose (fp);
	return NULL;
}

void SwarmSpy_InitLive(const char* const address, const int port, const bool logData)
{
	killThreads = false;

	// Create the belief handler callback hash table
	s_pBeliefHandlerHashTable = ht_init(32, NULL);


	const char* multicastAddress = address;
	const int multicastPortNum = port;

	struct WSAData wsaData;
	int nCode;

	if ((nCode = WSAStartup(MAKEWORD(2, 2), &wsaData)) != 0)
	{
		fprintf(stderr, "WSAStartup() returned error code: %d\n", nCode);
	}		

	// Startup the network receive thread
	sprintf(s_szSpyInfoArg, "%s:%d", multicastAddress, multicastPortNum);
	int retVal = pthread_create(&receiveThread, NULL, SwarmSpy_ReceiveThreadProc, s_szSpyInfoArg);


	if (logData)
	{
		SwarmSpy_RestartLogging ();
	}
}

void SwarmSpy_InitReplay(const char* const filename, const float replaySpeed, const bool logData)
{
	killThreads = false;

	// Create the belief handler callback hash table
	s_pBeliefHandlerHashTable = ht_init(32, NULL);


	const char* replayFilename = filename;
	
	m_MaxReplayDelayMs = Config::getInstance()->getValueAsInt ("SwarmSpy.MaxReplayDelayMs", 5000);

	//do replay thread
	pthread_t replayThread;
	sprintf_s (s_szSpyInfoArg, 1024, "%s:%f", replayFilename, replaySpeed);

	// Startup the log thread
	pthread_create(&replayThread, NULL, SwarmSpy_ReplayThreadProc, s_szSpyInfoArg);


	if (logData)
	{
		SwarmSpy_RestartLogging ();
	}
}

void SwarmSpy_Reset()
{
	killThreads = true;

	Sleep (200);
}

void SwarmSpy_PauseReplay (bool pause)
{
	replayPaused = pause;
}

void SwarmSpy_RestartReplay ()
{
	restartReplay = true;
}

void SwarmSpy_Destroy ()
{
	SwarmSpy_Reset ();

	if (s_pBeliefHandlerHashTable != NULL)
		ht_destroy(s_pBeliefHandlerHashTable);
}