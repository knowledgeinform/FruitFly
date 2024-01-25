#ifndef CommandListenerThreadH
#define CommandListenerThreadH

class RecordingStatus;
#include "PThreadC.h"
#include <windows.h>


enum INCOMING_PACKET_TYPE {COMMAND = 0, TIMESYNC};
enum COMMAND_ID {RECORD = 0, PAUSE, SHUTDOWN};

struct CommandPacket
{	
	COMMAND_ID command;
};

struct TimeSyncPacket
{
	SYSTEMTIME time;
};

class CommandListenerThread : public PThreadC
{
	public:
		CommandListenerThread(RecordingStatus &status, const short listenPort);

	protected:
		virtual void Run();

	private:
		RecordingStatus &m_status;
		int m_listenPort;
};

#endif