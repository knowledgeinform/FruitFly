#include "OrlandoVideoCapture.h"
#include <conio.h>
#include <stdio.h>
#include <windows.h>
#include <winsock2.h>
#include <stdlib.h>
#include <sys\timeb.h>
#include "RecordingStatus.h"
#include "CommandListenerThread.h"
#include "RecordingThread.h"
#include "Adept.h"

static const int STATUS_BROADCAST_INTERVAL_SEC = 1;
static const short STATUS_BROADCAST_PORT = 5747;
static const char* STATUS_BROADCAST_ADDRESS = "192.168.1.255";
static const short COMMAND_LISTEN_PORT = 5378;

static char spinner[] = {'|', '/', '-', '\\'};


static RecordingThread *s_pRecordingThread = NULL;


void gotoxy(const int column, const int line)
{
	COORD coord;
	coord.X = column;
	coord.Y = line;
	SetConsoleCursorPosition(
	GetStdHandle( STD_OUTPUT_HANDLE ), coord);
}

void FrameArrivedCallback(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum)
{
	s_pRecordingThread->AddBufferToQueue(pBuffer, fileName, acquisitionTimeSec, acquisitionTimeMillisec, channelNum);
}

int main(const int argc, const char* const argv[])
{
	bool bTimesyncOnly;

	if ((argc > 1) && (!strcmp(argv[1], "-timesync")))
	{
		bTimesyncOnly = true;
	}
	else
	{
		bTimesyncOnly = false;
	}

	//
	// Clear the console window
	//
	system("cls");	

	if (!bTimesyncOnly)
	{
		OrlandoVideoCapture::InitializeBoard(true);
		printf("Initialized Orlando Board\n");
	}
	
		//
		// Initialize Orlando input/output channels
		//
	OrlandoVideoCapture *pChannel1;
	OrlandoVideoCapture *pChannel2;
	if (!bTimesyncOnly)	
	{
		pChannel1 = new OrlandoVideoCapture(ORL_MODULE_VIDEO_IN0, ORL_PORT_CVBS0, ORL_MODULE_VIDEO_OUT0, ORL_PORT_CVBS0); 
		pChannel2 = new OrlandoVideoCapture(ORL_MODULE_VIDEO_IN1, ORL_PORT_CVBS0);	
		pChannel1->InitializeRecording("channel1\\", FrameArrivedCallback);
		pChannel2->InitializeRecording("channel2\\", FrameArrivedCallback);
	}

	bool currentlyRecording = false;


	//
	// Initialze Adept tracker board, if present
	//
	bool isAdeptPresent = false;
	Adept oAdept;
	if (!bTimesyncOnly)
	{		
		isAdeptPresent = oAdept.IsBoardPresent();
		if(isAdeptPresent)
		{
			oAdept.InitializeBoard();
		}
	}


	//
	// Start recording thread
	//
	RecordingStatus status;
	status.SetIsRunning(true);
	status.SetHasReceivedTimeSync(false);
	status.SetIsRecording(false);
	status.SetFrameRate(0);
	status.SetNumFramesInQueue(0);
	status.SetNumFramesDropped(0);
	status.SetIsAdeptPresent(isAdeptPresent);
	status.SetNumWarpQueueUnderflows(0);

	if (!bTimesyncOnly)
	{
		if (isAdeptPresent)
		{
			s_pRecordingThread = new RecordingThread("d:\\recording\\", pChannel1->GetFrameBufferSize(), status, &oAdept);
		}
		else
		{
			s_pRecordingThread = new RecordingThread("d:\\recording\\", pChannel1->GetFrameBufferSize(), status, NULL);
		}
		s_pRecordingThread->Start();
	}


	//
	// Initialize winsock library	
	//
	int hStatusSocket;
	WSADATA wsaData;
	if (WSAStartup(0x0202, &wsaData) != 0)
	{
		printf("Could not initialize winsock library\n");
		exit(1);
	}


	//
	// Setup status broadcast socket
	//
	hStatusSocket = socket(AF_INET, SOCK_DGRAM, 0);
	if (hStatusSocket < 0)
	{
		printf("Could not create UDP status socket\n");
		exit(1);
	}
	struct sockaddr_in statusBroadcastAddress;
	statusBroadcastAddress.sin_family = AF_INET;
	statusBroadcastAddress.sin_port = htons(STATUS_BROADCAST_PORT);
	statusBroadcastAddress.sin_addr.s_addr = inet_addr(STATUS_BROADCAST_ADDRESS);


	RecordingStatus::StatusPacketBody statusPacket;

	CommandListenerThread commandListenerThread(status, COMMAND_LISTEN_PORT);
	commandListenerThread.Start();


	_timeb lastBroadcastTime, currTime;
	_ftime_s(&lastBroadcastTime);	


	for (int i = 0; status.GetIsRunning(); ++i)	
	{
		//if (oChannel1.HasErrors())
		//{
		//	printf(("Channel1: " + oChannel1.GetError() + "\n").c_str());
		//}

		//if (oChannel2.HasErrors())
		//{
		//	printf(("Channel2: " + oChannel2.GetError() + "\n").c_str());
		//}

		gotoxy (5, 12);
		printf("                                                                    "); //Clear line
		gotoxy (5, 12);
		if (status.GetIsRecording() && !bTimesyncOnly)
		{
			if (!currentlyRecording)
			{
				pChannel1->StartRecording();
				pChannel2->StartRecording();
				s_pRecordingThread->SetIsPaused(false);
				currentlyRecording = true;
			}
			printf("Recording: %3i fps   %c   Queue Length: %i  %c  Dropped Frames: %i", status.GetFrameRate(), spinner[i%4], status.GetNumFramesInQueue(), spinner[i%4], status.GetNumFramesDropped());
		}
		else
		{
			if (currentlyRecording)
			{
				pChannel1->StopRecording();
				pChannel2->StopRecording();
				s_pRecordingThread->SetIsPaused(true);
				currentlyRecording = false;
			}

			if (bTimesyncOnly)
			{
				printf("**************TIME-SYNC AND CONNECTIVITY MODE*******************");
			}
			else
			{
				printf("*************************NOT RECORDING*************************");
			}
		}


		_ftime_s(&currTime);
		if ((currTime.time - lastBroadcastTime.time) >= STATUS_BROADCAST_INTERVAL_SEC)
		{
			//
			// Broadcast status
			//
			status.GetStatusPacketBody(statusPacket);
		    sendto(hStatusSocket, (const char*)&statusPacket, sizeof statusPacket, 0, (struct sockaddr*)&statusBroadcastAddress, sizeof statusBroadcastAddress);

			lastBroadcastTime = currTime;
		}


		//
		// Quit the program if the 'q' key is pressed
		//
		if (_kbhit() && _getch() == 'q')
		{
			status.SetIsRunning(false);
		}

		::Sleep(100);
	}

	::Sleep(1000);


	OrlandoVideoCapture::TerminateBoard();
	printf("\n\nTerminated Orlando Board\n");

	oAdept.TerminateBoard();
	printf("\n\nTerminated Adept Board\n");
	

	WSACleanup();

	::Sleep(500);

	::ExitProcess(0);

	return 0;
}