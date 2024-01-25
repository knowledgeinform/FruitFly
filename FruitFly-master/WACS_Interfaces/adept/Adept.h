#ifndef AdeptH
#define AdeptH

#include <windows.h>
#include "Adept104_PCI_Lib.h"
#include "PThreadC.h"
#include "PMutexC.h"
#include <sys/timeb.h>

struct Fixed_20_12
{
	unsigned integer : 20;
	unsigned decimal : 12;
};	

class Adept : public PThreadC
{
	public:
		typedef void (*WarpCaptureCallback)(const float* const pOddWarpParams, const float* const pEvenWarpParams, const _timeb &oTimestamp);	

		Adept();
		~Adept();

		bool IsBoardPresent() const;

		void InitializeBoard();
		void TerminateBoard();

		void EnableImageStabilization();
		void DisableImageStabilization();

		void SetWarpCaptureCallback(WarpCaptureCallback callbackFunc, const int nChannelNum);

		//
		// Client is responsible for passing arrays of length >= NUM_WARP_PARAMS_PER_FIELD
		//
		static const int NUM_WARP_PARAMS_PER_FIELD = 9;
		void GetChannelWarp(float pOddWarpParams[], float pEvenWarpParams[]);
		void GetChannelWarp(float pOddWarpParams[], float pEvenWarpParams[], _timeb &oTimestamp);




		//void GetAimpoint(Fixed_32_12 &oAimpointX, Fixed_32_12 &oAimpointY);

	protected:
		virtual void Run();


	private:
		AVT_COMPONENT_SET m_oAVTOddWarpComponentSet;
		AVT_COMPONENT_SET m_oAVTEvenWarpComponentSet;
		CAdept104_PCI m_oAdeptDriver;		
		LPCTSTR m_szErrorMessage;
		PMutexC m_oDataMutex;
		float* const m_pOddWarp;
		float* const m_pEvenWarp;
		_timeb m_oLastUpdateTime;
		WarpCaptureCallback m_pWarpCaptureCallback[2];
		bool m_bIsRunning;
};

#endif