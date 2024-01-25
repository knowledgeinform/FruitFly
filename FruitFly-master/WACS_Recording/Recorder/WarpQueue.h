#ifndef WarpQueueH
#define WarpQueueH

#include "Adept.h"
#include <queue>
#include <sys/timeb.h>
#include "PMutexC.h"

using namespace std;

class WarpQueue
{
	public:
		static WarpQueue* Instance();

		void SetAdeptInterface(Adept* const pAdeptInterface);

		bool GetWarp(const __time64_t &timestampSec, const unsigned short timestampMillisec, float* const pOddWarpParams, float* const pEvenWarpParams);

		static void WarpCaptureCallbackHandler(const float* const pOddWarpParams, const float* const pEvenWarpParams, const _timeb &oTimestamp);

		void SetIsPaused(const bool bIsPaused);

	private:
		WarpQueue();
		bool GetIsPaused() const;

		struct WarpCapture
		{
			float pOddWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
			float pEvenWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
			_timeb timestamp;
		};

		static WarpQueue *s_pInstance;
		Adept *m_pAdeptInterface;
		queue<WarpCapture* const> m_oWarpQueue;
		bool m_bIsPaused;
		mutable PMutexC m_oMutex;
};

#endif