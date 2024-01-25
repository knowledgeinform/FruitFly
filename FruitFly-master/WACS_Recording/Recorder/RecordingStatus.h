#ifndef RecordingStatusH
#define RecordingStatusH

#include "PMutexC.h"
#include "PAutoLockC.h"
#include <memory.h>
#include <winsock2.h>
#include <sys/timeb.h>

//
// Thread-safe status accessor class
//
class RecordingStatus
{
	public:
		struct StatusPacketBody
		{
			int hasReceivedTimeSync;
			int timestamp;
			int isRecording;
			int frameRate;
			int numFramesInQueue;
			int numFramesDropped;
			int isAdeptPresent;
			int numWarpQueueUnderflows;
		};

		void SetIsRunning(const bool isRunning)
		{
			PAutoLockC lock(m_mutex);
			m_isRunning = isRunning;
		}
		bool GetIsRunning() const
		{
			PAutoLockC lock(m_mutex);
			return m_isRunning;
		}

		void SetHasReceivedTimeSync(const bool hasReceivedTimeSync)
		{
			PAutoLockC lock(m_mutex);
			m_hasReceivedTimeSync = hasReceivedTimeSync;
		}
		bool GetHasReceivedTimeSync() const
		{
			PAutoLockC lock(m_mutex);
			return m_hasReceivedTimeSync;
		}

		void SetIsRecording(const bool isRecording)
		{
			PAutoLockC lock(m_mutex);
			m_isRecording = isRecording;
		}
		bool GetIsRecording() const
		{
			PAutoLockC lock(m_mutex);
			return m_isRecording;
		}

		void SetFrameRate(const int frameRate)
		{
			PAutoLockC lock(m_mutex);
			m_frameRate = frameRate;
		}
		int GetFrameRate() const
		{
			PAutoLockC lock(m_mutex);
			return m_frameRate;
		}

		void SetNumFramesInQueue(const int numFramesInQueue)
		{
			PAutoLockC lock(m_mutex);
			m_numFramesInQueue = numFramesInQueue;
		}
		int GetNumFramesInQueue() const
		{
			PAutoLockC lock(m_mutex);
			return m_numFramesInQueue;
		}

		void SetNumFramesDropped(const int numFramesDropped)
		{
			PAutoLockC lock(m_mutex);
			m_numFramesDropped = numFramesDropped;
		}
		int GetNumFramesDropped() const
		{
			PAutoLockC lock(m_mutex);
			return m_numFramesDropped;
		}

		void SetIsAdeptPresent(const int isAdeptPresent)
		{
			PAutoLockC lock(m_mutex);
			m_isAdeptPresent = (isAdeptPresent == 0 ? false : true);
		}
		int GetIsAdeptPresent() const
		{
			PAutoLockC lock(m_mutex);
			return m_isAdeptPresent;
		}

		void SetNumWarpQueueUnderflows(const int numWarpQueueUnderflows)
		{
			PAutoLockC lock(m_mutex);
			m_numWarpQueueUnderflows = numWarpQueueUnderflows;
		}
		void IncrementWarpQueueUnderflows()
		{
			PAutoLockC lock(m_mutex);
			++m_numWarpQueueUnderflows;
		}
		int GetNumWarpQueueUnderflows() const
		{
			PAutoLockC lock(m_mutex);
			return m_numWarpQueueUnderflows;
		}

		void GetStatusPacketBody(StatusPacketBody &packetBody) const
		{
			//
			// Convert the status data into network-byte-order format
			//
			_timeb currTime;
			_ftime_s(&currTime);
			PAutoLockC lock(m_mutex);
			packetBody.hasReceivedTimeSync = htonl((int)m_hasReceivedTimeSync);
			packetBody.timestamp = htonl((int)currTime.time);
			packetBody.isRecording = htonl((int)m_isRecording);
			packetBody.frameRate = htonl(m_frameRate);
			packetBody.numFramesInQueue = htonl(m_numFramesInQueue);
			packetBody.numFramesDropped = htonl(m_numFramesDropped);
			packetBody.isAdeptPresent = htonl((int)m_isAdeptPresent);
			packetBody.numWarpQueueUnderflows = htonl(m_numWarpQueueUnderflows);
		}

	private:
		mutable PMutexC m_mutex;
		bool m_hasReceivedTimeSync;
		bool m_isRunning;
		bool m_isRecording;
		int m_frameRate;
		int m_numFramesInQueue;
		int m_numFramesDropped;
		bool m_isAdeptPresent;
		int m_numWarpQueueUnderflows;

};
#endif