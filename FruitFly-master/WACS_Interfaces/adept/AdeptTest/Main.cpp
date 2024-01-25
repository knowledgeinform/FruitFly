#include "Adept.h"
#include <iostream>
#include <conio.h>
#include "PMutexC.h"
#include "PAutoLockC.h"

using namespace std;

static PMutexC s_oWarpMutex;
static float s_pOddWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
static float s_pEvenWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];

void WarpCaptureCallbackFunc(const float* const pOddWarpParams, const float* const pEvenWarpParams, const _timeb &oTimestamp)
{
	PAutoLockC oLock(s_oWarpMutex);
	memcpy(s_pOddWarpParams, pOddWarpParams, sizeof(s_pOddWarpParams));
	memcpy(s_pEvenWarpParams, pEvenWarpParams, sizeof(s_pEvenWarpParams));
}

int main()
{
	try
	{
		Adept oAdeptInterface;
		oAdeptInterface.InitializeBoard();
		cout << "Started Adept Interface" << endl;

		//oAdeptInterface.EnableImageStabilization();
		//cout << "Enabled Image Stabilization" << endl;

		oAdeptInterface.SetWarpCaptureCallback(WarpCaptureCallbackFunc, 1);

		float pOddWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
		float pEvenWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];

		while (!(_kbhit() && _getch() == 'q'))
		{
			s_oWarpMutex.Lock();
			memcpy(pOddWarpParams, s_pOddWarpParams, sizeof(pOddWarpParams));
			memcpy(pEvenWarpParams, s_pEvenWarpParams, sizeof(pEvenWarpParams));
			s_oWarpMutex.Unlock();

			for (int i = 0; i < Adept::NUM_WARP_PARAMS_PER_FIELD; ++i)
			{
				cout << "Odd Param" << i << ": " << pOddWarpParams[i] << endl;
			}
			for (int i = 0; i < Adept::NUM_WARP_PARAMS_PER_FIELD; ++i)
			{
				cout << "Even Param" << i << ": " << pEvenWarpParams[i] << endl;
			}
			cout << endl << endl;

			::Sleep(500);
		}

		oAdeptInterface.TerminateBoard();

	}
	catch (exception &e)
	{
		cerr << endl << "ERROR: " << e.what() << endl;
	}

	cout << "Press any key to terminate" << endl;
	_getch();

	return 0;
}
