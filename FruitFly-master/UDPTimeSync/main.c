#include <windows.h>

static const short LISTEN_PORT = 6378;

unsigned short calcChecksum(const char* const pPacket, const int dataLength)
{
	int i;
	unsigned short checksum = 0;
	const unsigned short *pWord = (unsigned short*)pPacket;

	for (i = 0; i < dataLength; i+=2)
	{
		checksum += *pWord;
		++pWord;
	}

	return checksum;
}

int main()
{
	int hStatusSocket;
	WSADATA wsaData;
	int hSocket;
	struct sockaddr_in listenAddress;
	char pBuffer[64];
	int numBytesReceived;
	unsigned short incomingChecksum;
	SYSTEMTIME* pTime;

	printf("UDP Timesync\n\n");

	//
	// Initialize winsock library	
	//
	if (WSAStartup(0x0202, &wsaData) != 0)
	{
		printf("Could not initialize winsock library\n");
		exit(1);
	}

	//
	// Setup listener socket
	//
	hSocket = socket(AF_INET, SOCK_DGRAM, 0);
	if (hSocket < 0)
	{
		printf("Could not create UDP command listener socket\n");
		exit(1);
	}
	else
	{
		printf("Created listener socket\n");
	}
	
	listenAddress.sin_family = AF_INET;
	listenAddress.sin_port = htons(LISTEN_PORT);
	listenAddress.sin_addr.s_addr=htonl(INADDR_ANY);
	bind(hSocket,(struct sockaddr *)&listenAddress, sizeof(listenAddress));

	

	while (1)
	{
		printf("Listening for sync packet...\n");
		numBytesReceived = recv(hSocket, pBuffer, sizeof pBuffer, 0);

		if (numBytesReceived == (sizeof(SYSTEMTIME) + 2))
		{
			incomingChecksum = ntohs(*(unsigned short*)(&pBuffer[numBytesReceived - 2]));

			if (incomingChecksum == calcChecksum(pBuffer, numBytesReceived - 2))
			{
				pTime = (SYSTEMTIME*)pBuffer;
				pTime->wYear = ntohs(pTime->wYear);
				pTime->wMonth = ntohs(pTime->wMonth);
				pTime->wDayOfWeek = ntohs(pTime->wDayOfWeek);
				pTime->wDay = ntohs(pTime->wDay);
				pTime->wHour = ntohs(pTime->wHour);
				pTime->wMinute = ntohs(pTime->wMinute);
				pTime->wSecond = ntohs(pTime->wSecond);
				pTime->wMilliseconds = ntohs(pTime->wMilliseconds);
				SetSystemTime(pTime);	
				printf("Timesync successful %d-%02d-%02d %02d:%02d:%02d.%03d\n", pTime->wYear, pTime->wMonth, pTime->wDay, pTime->wHour, pTime->wMinute, pTime->wSecond, pTime->wMilliseconds);
			}
			else
			{
				printf("Received packet with bad checksum\n");
			}
		}
	}
}
