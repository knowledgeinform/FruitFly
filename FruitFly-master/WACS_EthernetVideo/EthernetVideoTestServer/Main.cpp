#include "EthernetVideoServer.h"
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <windows.h>
#include <stdio.h>

static const int TARGET_FRAMERATE_HZ = 10;
static const int MILLISECONDS_PER_FRAME = 1000 / TARGET_FRAMERATE_HZ;

static const int XDIM = 320;
static const int YDIM = 240;

int main()
{
	const int nListenPort = 3999;
	EthernetVideoServer oEthernetVideoServer(nListenPort, XDIM, YDIM);

	printf("Listening on port %i\n", nListenPort);

	DWORD dwLastFrameTime_ms = GetTickCount();
	

	const int nNumFrameBytes = XDIM * YDIM;
	unsigned char* const pTestFrame = (unsigned char*)malloc(nNumFrameBytes);

	unsigned char* pEncoderOutput = NULL;
	int nEncoderOutputSize = 0;

	int nFrameNum = 0;
	srand((unsigned)time(NULL));

	while(true)
	{
		unsigned char* pPixel = pTestFrame;

		for (int i = 0; i < YDIM; ++i)
		{
			for (int j = 0; j < XDIM; ++j)
			{
				//
				// Create expanding circle test pattern
				//
				if (sqrt(float((i - YDIM / 2) * (i - YDIM / 2) + (j - XDIM / 2) * (j - XDIM / 2))) <= (nFrameNum * 3 % 50))
				{
					*pPixel = 220;					
				}
				else
				{
					*pPixel = 120;
				}

				++pPixel;
			}
		}

		//
		// Send frame to be encoded and sent
		//
		oEthernetVideoServer.SetLatestFrame(pTestFrame, nNumFrameBytes);


		++nFrameNum;

		DWORD dwFrameDuration = GetTickCount() - dwLastFrameTime_ms;

		if (dwFrameDuration < MILLISECONDS_PER_FRAME)
		{
			Sleep(MILLISECONDS_PER_FRAME - dwFrameDuration);
		}
		else
		{
			Sleep(1);
		}

		dwLastFrameTime_ms = GetTickCount();
	}

	return 0;
}