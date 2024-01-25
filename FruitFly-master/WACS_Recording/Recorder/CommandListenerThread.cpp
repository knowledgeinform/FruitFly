#include "CommandListenerThread.h"
#include "RecordingStatus.h"
#include "stdio.h"
#include <winsock2.h>
#include <stdlib.h>

CommandListenerThread::CommandListenerThread(RecordingStatus &status, const short listenPort)
:m_status(status),
 m_listenPort(listenPort)
{
}

void CommandListenerThread::Run()
{
	//
	// Setup command listener socket
	//
	int hSocket = socket(AF_INET, SOCK_DGRAM, 0);
	if (hSocket < 0)
	{
		printf("Could not create UDP command listener socket\n");
		exit(1);
	}
	else
	{
		printf("Created UDP command listener socket\n");
	}
	struct sockaddr_in listenAddress;
	listenAddress.sin_family = AF_INET;
	listenAddress.sin_port = htons(m_listenPort);
	listenAddress.sin_addr.s_addr=htonl(INADDR_ANY);
	bind(hSocket,(struct sockaddr *)&listenAddress,sizeof(listenAddress));


	//
	// Listen for command packets
	//
	char buffer[128];
	const unsigned char* const pPacketTypeId = (unsigned char*)buffer;
	void* const pData = (void*)(((int)buffer) + 1);
	int numBytesReceived;
	while (true)
	{
		numBytesReceived = recv(hSocket, buffer, sizeof buffer, 0);		

		if (numBytesReceived > 0)
		{
			switch ((INCOMING_PACKET_TYPE)(*pPacketTypeId))
			{
				case COMMAND:
					if (numBytesReceived == ((sizeof CommandPacket) + 1))
					{
						int cmd = ntohl(((CommandPacket*)(pData))->command);
						switch(ntohl(((CommandPacket*)(pData))->command))
						{
							case RECORD:
								m_status.SetIsRecording(true);
								break;
							case PAUSE:
								m_status.SetIsRecording(false);
								break;
							case SHUTDOWN:
								// Shutdown the whole computer
								system("shutdown -s -t 0");
								break;
						}			
					}
					else if (numBytesReceived < 0)
					{
						printf("Error reading command from socket. WSAErrorNum: %i", WSAGetLastError());
					}
					break;

				case TIMESYNC:
					if (numBytesReceived == ((sizeof TimeSyncPacket) + 1))
					{
						m_status.SetHasReceivedTimeSync(true);

						//
						// Convert from network to host byte ordering
						//
						SYSTEMTIME* const pTime = &(((TimeSyncPacket*)pData)->time);
						pTime->wYear = ntohs(pTime->wYear);
						pTime->wMonth = ntohs(pTime->wMonth);
						pTime->wDayOfWeek = ntohs(pTime->wDayOfWeek);
						pTime->wDay = ntohs(pTime->wDay);
						pTime->wHour = ntohs(pTime->wHour);
						pTime->wMinute = ntohs(pTime->wMinute);
						pTime->wSecond = ntohs(pTime->wSecond);
						pTime->wMilliseconds = ntohs(pTime->wMilliseconds);
						::SetSystemTime(pTime);		
						exit(0);
					}
					else if (numBytesReceived < 0)
					{
						printf("Error reading timesync from socket. WSAErrorNum: %i", WSAGetLastError());
					}
					break;
			}
		}
		else
		{
			int errorNum = WSAGetLastError();
			printf("Error reading incoming packet type from socket. WSAErrorNum: %i", errorNum);
		}

		::Sleep(50);
	}
}