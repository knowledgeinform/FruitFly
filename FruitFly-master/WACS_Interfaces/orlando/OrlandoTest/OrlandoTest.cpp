#include "OrlandoVideoCapture.h"
#include <stdio.h>
#include <conio.h>
#include <windows.h>

static int s_nNumFramesReceived = 0;

void FrameArrivedCallback(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum)
{
	++s_nNumFramesReceived;
}


void __declspec(dllexport) RunTest()
{
	OrlandoVideoCapture::InitializeBoard();
	printf("Initialized Orlando Board\n");
	
	//
	// Initialize Orlando input/output channels
	//
	OrlandoVideoCapture oChannel1(ORL_MODULE_VIDEO_IN0, ORL_PORT_CVBS0, ORL_MODULE_VIDEO_OUT0, ORL_PORT_CVBS0); 
	//oChannel1.InitializeRecording("channel1\\", FrameArrivedCallback);
	//oChannel1.StartRecording();

	while (true)
	{
		char* pFrame = (char*)oChannel1.GetLatestFrame();
		::Sleep(250);
		printf("%d\n", (int)pFrame);
	}
}

int main()
{
	RunTest();

	return 0;
}