#include "ConnectivityThread.h"
#include <stdio.h>

static const int STATUS_BROADCAST_INTERVAL_SEC = 1;
static const short STATUS_BROADCAST_PORT = 5747;
static const char* STATUS_BROADCAST_ADDRESS = "192.168.100.255";

ConnectivityThread::ConnectivityThread(RecordingStatus &status)
:m_status(status)
{
}

void ConnectivityThread::Run()
{
	RecordingStatus::StatusPacketBody statusPacket;
	int hStatusSocket;


	//
	// Setup status broadcast socket
	//
	hStatusSocket = socket(AF_INET, SOCK_DGRAM, 0);
	if (hStatusSocket < 0)
	{
		printf("Could not create UDP status socket\n");
		exit(1);
	}
	else
	{
		printf("Created UDP status broadcast socket\n");
	}
	struct sockaddr_in statusBroadcastAddress;
	statusBroadcastAddress.sin_family = AF_INET;
	statusBroadcastAddress.sin_port = htons(STATUS_BROADCAST_PORT);
	statusBroadcastAddress.sin_addr.s_addr = inet_addr(STATUS_BROADCAST_ADDRESS);

	_timeb lastBroadcastTime, currTime;
	_ftime_s(&lastBroadcastTime);

	while (true)
	{
		_ftime_s(&currTime);
		if ((currTime.time - lastBroadcastTime.time) >= STATUS_BROADCAST_INTERVAL_SEC)
		{
			//
			// Broadcast status
			//
			m_status.GetStatusPacketBody(statusPacket);
			sendto(hStatusSocket, (const char*)&statusPacket, sizeof statusPacket, 0, (struct sockaddr*)&statusBroadcastAddress, sizeof statusBroadcastAddress);
	
			lastBroadcastTime = currTime;
		}

		Sleep(250);
	}

}