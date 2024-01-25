#include <stdio.h>
#include <WinSock2.h>
#include <Ws2tcpip.h> //Required for multicast functionality
#include "pthread.h"
#include "hashtab.h" //hash table
#include "SwarmSpy.h"
#include "zlib.h"
#include "Endian.h"


static hashtab_t *s_pBeliefHandlerHashTable;
static pthread_mutex_t s_mutex = PTHREAD_MUTEX_INITIALIZER;
static char s_szAddressInfoArg[24];


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

void SwarmSpy_ParsePacket(const unsigned char* const pPacket, const int packetSize)
{
    const int MAX_OPTION_LEN = 127;
    const unsigned char NOF_MASK = (byte) 0x80;  //"next option flag" mask - indicates whether there is another option after this one.
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

	if ((pPacket[0] & 0x1C) == 0x04)
	{
		//This is a compressed packet
		if (SwarmSpy_DecompressPacket(&pPacket[1], packetSize - 1, pDecompressionBuffer, SWARM_SPY_DECOMPRESSION_BUFFER_SIZE) != 0)
		{
			return;
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
	printf("Received Belief: %s\n", szBeliefName);
	packetByteIndex += beliefNameLength;

	timestamp_ms = *(long long*)&pUncompressedPacket[packetByteIndex];
	ByteSwapInt64(&timestamp_ms);
	packetByteIndex += sizeof(timestamp_ms);
	transmissionTime_ms = *(long long*)&pUncompressedPacket[packetByteIndex];
	ByteSwapInt64(&transmissionTime_ms);
	packetByteIndex += sizeof(transmissionTime_ms);

	publishingAgentNameLength = ntohs(*(unsigned short*)&pUncompressedPacket[packetByteIndex]);
	packetByteIndex += sizeof(publishingAgentNameLength);
	if (publishingAgentNameLength > sizeof (szPublishingAgentName))
	{
		fprintf(stderr, "  Found java-serialized packet: %s. Ignoring.\n", szBeliefName);
		return;
	}
	memcpy(szPublishingAgentName, (char*)&pUncompressedPacket[packetByteIndex], publishingAgentNameLength);
	szPublishingAgentName[publishingAgentNameLength] = '\0';
	packetByteIndex += publishingAgentNameLength;

	pthread_mutex_lock(&s_mutex);
	pHashedCallbackPointer = ht_search(s_pBeliefHandlerHashTable, (void*)szBeliefName, beliefNameLength);
	pthread_mutex_unlock(&s_mutex);

	if (pHashedCallbackPointer != NULL)
	{
		(**(BeliefHandlerCallbackFunc*)pHashedCallbackPointer)(szBeliefName, &pUncompressedPacket[packetByteIndex], packetSize - packetByteIndex, timestamp_ms);
	}
}

void* SwarmSpy_ReceiveThreadProc(void *arg)
{
	typedef enum {OAM = 0x0, APP_PERIODIC = 0x01, APP_ON_DEMAND = 0x02} SwarmPacketCategory;
	#define SWARM_SPY_RECV_BUFFER_SIZE 1024
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
			SwarmSpy_ParsePacket(pReceiveBuffer, numBytesReceived);
		}
	}
	while (connected);

	closesocket(hSocket);
	WSACleanup();
	return NULL;
}

void SwarmSpy_Init(const char* const multicastAddress, const int multicastPortNum)
{
    struct WSAData wsaData;
	int nCode;
	pthread_t receiveThread;

	// Create the belief handler callback hash table
	s_pBeliefHandlerHashTable = ht_init(32, NULL);


    if ((nCode = WSAStartup(MAKEWORD(2, 2), &wsaData)) != 0)
	{
		fprintf(stderr, "WSAStartup() returned error code: %d\n", nCode);
    }		

	// Startup the network receive thread
	sprintf(s_szAddressInfoArg, "%s:%d", multicastAddress, multicastPortNum);
	pthread_create(&receiveThread, NULL, SwarmSpy_ReceiveThreadProc, s_szAddressInfoArg);
}