#include "Adept.h"
#include "PAutoLockC.h"
#include <cstring>
#include <exception>
#include <iostream>

using namespace::std;

static const int ODD_WARP_DATA_BLOCKID = 168;
static const int EVEN_WARP_DATA_BLOCKID = 169;
static const int ODD_WARP_COMPONENT_SET_ID = 2;
static const int EVEN_WARP_COMPONENT_SET_ID = 3;

static const int UPDATE_THREAD_SLEEP_TIME_MS = 3;


Adept::Adept()
:m_pOddWarp(new float[NUM_WARP_PARAMS_PER_FIELD]),
 m_pEvenWarp(new float[NUM_WARP_PARAMS_PER_FIELD])
{
	m_pWarpCaptureCallback[0] = NULL;
	m_pWarpCaptureCallback[1] = NULL;

	memset(m_pOddWarp, 0, NUM_WARP_PARAMS_PER_FIELD * sizeof(float));
	memset(m_pEvenWarp, 0, NUM_WARP_PARAMS_PER_FIELD * sizeof(float));
}

Adept::~Adept()
{
	TerminateBoard();
}

void Adept::InitializeBoard()
{
	//
	// We have to register "componenets" that signify the 
	// blocks we need to read from
	//
	m_oAVTOddWarpComponentSet.ucCommandID = 0x30; //Register command
	m_oAVTOddWarpComponentSet.ucComponentSetID = ODD_WARP_COMPONENT_SET_ID;
	m_oAVTOddWarpComponentSet.ucReadWriteUsage = 0; //read-only 
	m_oAVTOddWarpComponentSet.ucNotification = 1;//on change
	m_oAVTOddWarpComponentSet.ucNotificationDelay = 10; //only send new values after they've changed 10 times to slow update-rate
	m_oAVTOddWarpComponentSet.ucComponents = NUM_WARP_PARAMS_PER_FIELD;
	m_oAVTOddWarpComponentSet.bRegistered = FALSE;

	m_oAVTEvenWarpComponentSet.ucCommandID = 0x30; //Register command
	m_oAVTEvenWarpComponentSet.ucComponentSetID = EVEN_WARP_COMPONENT_SET_ID;
	m_oAVTEvenWarpComponentSet.ucReadWriteUsage = 0; //read-only 
	m_oAVTEvenWarpComponentSet.ucNotification = 1; //on change
	m_oAVTOddWarpComponentSet.ucNotificationDelay = 10; //only send new values after they've changed 10 times to slow update-rate
	m_oAVTEvenWarpComponentSet.ucComponents = NUM_WARP_PARAMS_PER_FIELD;
	m_oAVTEvenWarpComponentSet.bRegistered = FALSE;

	for (int i = 0; i < NUM_WARP_PARAMS_PER_FIELD; ++i)
	{
		AVT_COMPONENT_ITEM componentItem;
		componentItem.wComponentID = ((i+1) << 8) | ODD_WARP_DATA_BLOCKID;
		componentItem.ucDataType = 0x50; //floating point
		componentItem.pszDescription = "Warp";
		m_oAVTOddWarpComponentSet.Component[i] = componentItem;

		componentItem.wComponentID = ((i+1) << 8) | EVEN_WARP_DATA_BLOCKID;
		m_oAVTEvenWarpComponentSet.Component[i] = componentItem;
	}
	
	if (!m_oAdeptDriver.LoadDeviceDriver(0, &m_szErrorMessage))
	{
		throw exception("Could not load Adept device driver");
	}


	if (!m_oAdeptDriver.ECIF_RegisterComponentSet(&m_oAVTOddWarpComponentSet, &m_szErrorMessage))
	{
		throw exception("Could not register odd warp component set");
	}

	if (!m_oAdeptDriver.ECIF_RegisterComponentSet(&m_oAVTEvenWarpComponentSet, &m_szErrorMessage))
	{
		throw exception("Could not register even warp component set");
	}

	Start();
}

void Adept::TerminateBoard()
{
	if (m_oAdeptDriver.DeviceDriverLoaded())
	{
		m_oDataMutex.Lock();
		m_bIsRunning = false;
		m_oDataMutex.Lock();
		::Sleep(250); //Wait for receive thread to quit

		if (!m_oAdeptDriver.ECIF_CancelRegistration(255, &m_szErrorMessage))
		{
			throw exception("Could not cancel Adept component registration");		
		}

		if (!m_oAdeptDriver.UnloadDeviceDriver(&m_szErrorMessage))
		{
			throw exception("Could not unload Adept device driver");
		}
	}
}

bool Adept::IsBoardPresent() const
{
	return (m_oAdeptDriver.InstalledAdept104s() > 0);
}

void Adept::EnableImageStabilization()
{
	
	if (!m_oAdeptDriver.Command(11, 8, 2UL, &m_szErrorMessage))
	{
		throw exception("Could not set peephole video source");
	}

	if (!m_oAdeptDriver.Command(68, 8, 2UL, &m_szErrorMessage))
	{
		throw exception("Could not enable image stabilization");
	}

	if (!m_oAdeptDriver.Command(68, 1, 2UL, &m_szErrorMessage))
	{
		throw exception("Could not enable RST warp engine");
	}
}

void Adept::DisableImageStabilization()
{
	
	if (!m_oAdeptDriver.Command(11, 8, 0UL, &m_szErrorMessage))
	{
		throw exception("Could not set peephole video source");
	}

	if (!m_oAdeptDriver.Command(68, 8, 0UL, &m_szErrorMessage))
	{
		throw exception("Could not disable image stabilization");
	}

	if (!m_oAdeptDriver.Command(68, 1, 0UL, &m_szErrorMessage))
	{
		throw exception("Could not disable RST warp engine");
	}
}

void Adept::SetWarpCaptureCallback(WarpCaptureCallback callbackFunc, const int nChannelNum)
{
	PAutoLockC oLock(m_oDataMutex);
	m_pWarpCaptureCallback[nChannelNum - 1] = callbackFunc;
}

void Adept::GetChannelWarp(float pOddWarpParams[], float pEvenWarpParams[])
{
	PAutoLockC oLock(m_oDataMutex);
	memcpy(pOddWarpParams, m_pOddWarp, sizeof pOddWarpParams);	
	memcpy(pEvenWarpParams, m_pOddWarp, sizeof pEvenWarpParams);	
}

void Adept::GetChannelWarp(float pOddWarpParams[], float pEvenWarpParams[], _timeb &oTimestamp)
{
	PAutoLockC oLock(m_oDataMutex);
	GetChannelWarp(pOddWarpParams, pEvenWarpParams);
	memcpy(&oTimestamp, &m_oLastUpdateTime, sizeof oTimestamp);
}

void Adept::Run()
{

	::Sleep(1000); //Make sure board has had time to initialize properly


	AVT_COMPONENT_SET pResponseComponentSets[10];
	DWORD dwSizeOfResponseBuffer;
	bool bIsRunning;

	do
	{
		try
		{
			bool bValueChanged = false;

			//
			// Update timestamp associate with these responses
			//
			m_oDataMutex.Lock();
			_ftime_s(&m_oLastUpdateTime);
			m_oDataMutex.Unlock();


			//
			// Read latest responses from board
			//
			dwSizeOfResponseBuffer = sizeof(pResponseComponentSets);
			if (!m_oAdeptDriver.ECIF_ReadData(pResponseComponentSets, &dwSizeOfResponseBuffer, &m_szErrorMessage))
			{
				throw exception("Could not read warp data response");
			}


			//
			// Parse the reponse components sets.  Each set has a list of
			// component items, which each contain one field value.
			//
			float* pCurrWarp;
			int numComponentSets = dwSizeOfResponseBuffer / sizeof(AVT_COMPONENT_SET);
			for (int i = 0; i < numComponentSets; ++i)
			{					
				if (pResponseComponentSets[i].ucComponentSetID == ODD_WARP_COMPONENT_SET_ID)
				{
					pCurrWarp = m_pOddWarp;
				}
				else
				{
					pCurrWarp = m_pEvenWarp;
				}

				for (int j = 0; j < pResponseComponentSets[i].ucComponents; ++j)
				{
					AVT_COMPONENT_ITEM &currComponent = pResponseComponentSets[i].Component[j];

					//
					// If this value has changed, update it and set the value changed flag
					//
					int paramIndex = (currComponent.wComponentID >> 8) - 1;
					switch (currComponent.ucDataType)
					{
						case 0x50:
							if (pCurrWarp[paramIndex] != currComponent.Data.fValue)
							{
								bValueChanged = true;
								pCurrWarp[paramIndex] = currComponent.Data.fValue;
							}
					}
				}
			}

			//
			// Pass the new values to the client's callback function
			//
			if (bValueChanged && m_pWarpCaptureCallback[0])
			{				
				m_pWarpCaptureCallback[0](m_pOddWarp, m_pEvenWarp, m_oLastUpdateTime); 
			}

			::Sleep(UPDATE_THREAD_SLEEP_TIME_MS);
		}
		catch (exception &e)
		{
			cerr << e.what();
		}


		//
		// Thread-safe update of our running flag.  This flag is set to false
		// by the main thread when it's time to terminate the application.
		//
		m_oDataMutex.Lock();
		bIsRunning = m_bIsRunning;
		m_oDataMutex.Unlock();
	}
	while(bIsRunning);
}
