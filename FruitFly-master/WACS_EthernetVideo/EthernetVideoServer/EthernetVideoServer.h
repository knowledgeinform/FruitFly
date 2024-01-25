#ifndef EthernetVideoServerH
#define EthernetVideoServerH

#include <winsock.h>
#include "PMutexC.h"
#include "PThreadC.h"

class EthernetVideoServer : public PThreadC
{
	public:
		EthernetVideoServer(const int nListenPort, const int nFrameWidth, const int nFrameHeight);
		~EthernetVideoServer();

		void SetLatestFrame(const unsigned char* const pLatestFrame, const int nNumFrameBytes);

		void Run();

	private:
		void InterfaceWithClient(const SOCKET hClientSocket);

		int m_nFrameWidth;
		int m_nFrameHeight;
		int m_nListenPort;
		unsigned char* m_pLatestFrame;
		int m_nLatestFrameBufferSize;
		unsigned char* m_pUnencodedBuffer;
		int m_nUnencodedBufferSize;
		bool m_bNewFrameArrived;
		PMutexC m_Mutex;
};


#endif