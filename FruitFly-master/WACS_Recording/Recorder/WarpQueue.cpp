#include "WarpQueue.h"
#include "PAutoLockC.h"
#include <exception>

WarpQueue* WarpQueue::s_pInstance = NULL;
static int WARP_QUEUE_MAX_SIZE = 30;

WarpQueue* WarpQueue::Instance()
{
	if (!s_pInstance)
	{
		s_pInstance = new WarpQueue();
	}
	
	return s_pInstance;
}

WarpQueue::WarpQueue()
:m_bIsPaused(true)
{
}

void WarpQueue::SetIsPaused(const bool bIsPaused)
{
	PAutoLockC oLock(s_pInstance->m_oMutex);
	m_bIsPaused = bIsPaused;
}

bool WarpQueue::GetIsPaused() const
{
	PAutoLockC oLock(s_pInstance->m_oMutex);
	return m_bIsPaused;
}

void WarpQueue::SetAdeptInterface(Adept* const pAdeptInterface)
{
	m_pAdeptInterface = pAdeptInterface;
	m_pAdeptInterface->SetWarpCaptureCallback(WarpCaptureCallbackHandler, 1);
}

void WarpQueue::WarpCaptureCallbackHandler(const float* const pOddWarpParams, const float* const pEvenWarpParams, const _timeb &oTimestamp)
{
	PAutoLockC oLock(s_pInstance->m_oMutex);
	while (s_pInstance->m_oWarpQueue.size() >= WARP_QUEUE_MAX_SIZE)
	{
		s_pInstance->m_oWarpQueue.pop();
	}

	if (!s_pInstance->GetIsPaused())
	{
		WarpCapture* const pWarpCapture = new WarpCapture();
		memcpy(pWarpCapture->pOddWarpParams, pOddWarpParams, sizeof(float) * Adept::NUM_WARP_PARAMS_PER_FIELD);
		memcpy(pWarpCapture->pEvenWarpParams, pEvenWarpParams, sizeof(float) * Adept::NUM_WARP_PARAMS_PER_FIELD);
		pWarpCapture->timestamp = oTimestamp;
		s_pInstance->m_oWarpQueue.push(pWarpCapture);
	}
}

bool WarpQueue::GetWarp(const __time64_t &timestampSec, const unsigned short timestampMillisec, float* const pOddWarpParams, float* const pEvenWarpParams)
{
	bool bFound = false;
	WarpCapture *pPrevWarpCapture = NULL;
	WarpCapture *pCorrectWarpCapture;
	int nNumSleeps = 0;

	//
	// Loop through the queue until we find the warp that has a timestamp greater
	// than the requested time.  Then return the warp directly before that one since
	// there was probably a slight lag associated with grabbing the warp data from
	// the board.
	// 
	while(!bFound)
	{
		m_oMutex.Lock();
		int nNumInQueue = m_oWarpQueue.size();
		m_oMutex.Unlock();
		if (nNumInQueue > 0)
		{
			m_oMutex.Lock();
			WarpCapture *pWarpCapture = m_oWarpQueue.front();
			m_oMutex.Unlock();
			if ((pWarpCapture->timestamp.time > timestampSec) || (pWarpCapture->timestamp.time == timestampSec && pWarpCapture->timestamp.millitm > timestampMillisec))
			{
				bFound = true;
				if (pPrevWarpCapture == NULL)
				{					
					pCorrectWarpCapture = pWarpCapture;
				}
				else
				{
					pCorrectWarpCapture = pPrevWarpCapture;
				}
			}
			else
			{
				delete pPrevWarpCapture;
				m_oMutex.Lock();
				pPrevWarpCapture = m_oWarpQueue.front();
				m_oWarpQueue.pop();
				m_oMutex.Unlock();
			}
		}
		else
		{
			if (nNumSleeps < 3)
			{
				::Sleep(5);
				++nNumSleeps;
			}
			else
			{
				memset((void*)pOddWarpParams, 0, Adept::NUM_WARP_PARAMS_PER_FIELD * sizeof(float));
				memset((void*)pEvenWarpParams, 0, Adept::NUM_WARP_PARAMS_PER_FIELD * sizeof(float));
				return false;
			}
		}
	}


				////-------TEMP---------------
				//pWarpParams = new float[Adept::NUM_WARP_PARAMS];
				//for (int i = 0; i < Adept::NUM_WARP_PARAMS; ++i)
				//{
				//	if (i % 3 == 1)
				//	{
				//		pWarpParams[i] = 1;
				//	}
				//	else
				//	{
				//		pWarpParams[i] = 0;
				//	}
				//}
				////-------/TEMP--------------

	memcpy(pOddWarpParams, pCorrectWarpCapture->pOddWarpParams, Adept::NUM_WARP_PARAMS_PER_FIELD * sizeof(float));
	memcpy(pEvenWarpParams, pCorrectWarpCapture->pEvenWarpParams, Adept::NUM_WARP_PARAMS_PER_FIELD * sizeof(float));

	return true;
}

