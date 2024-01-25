#ifndef ConnectivityThreadH
#define ConnectivityThreadH

#include "PThreadC.h"
#include "RecordingStatus.h"

class ConnectivityThread :	public PThreadC
{
	public:
		ConnectivityThread(RecordingStatus &status);

		virtual void Run();

	private:
		RecordingStatus &m_status;
};

#endif