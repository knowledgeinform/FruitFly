#ifndef RecordingThreadH
#define RecordingThreadH

#include "PThreadC.h"
#include "PMutexC.h"
#include "RecordingStatus.h"
#include "Adept.h"
#include "WarpQueue.h"
#include <queue>
#include <string>
#include <map>

using namespace std;


class RecordingThread : public PThreadC
{
	public:
		RecordingThread(const string &recordingDirectory, const unsigned int bufferSize, RecordingStatus &status, Adept* const pAdeptInterfaceThread);
		void AddBufferToQueue(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum);
		int GetQueueLength() const;
		int GetNumFramesRecorded() const;
		int GetNumFramesDropped() const;
		void ResetFrameCounts();
		int GetNumFramesRecordedPerSec();
		void SetIsPaused(const bool isPaused);

	protected:
		virtual void Run();

	private:
		struct BufferData
		{
			void *pBuffer;
			float pOddWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
			float pEvenWarpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
			string fileName;
			__time64_t acquisitionTimeSec;
			unsigned short acquisitionTimeMillisec;
			int channelNum;
		};

		WarpQueue &m_warpQueue;
		RecordingStatus &m_status;
		string m_recordingDirectory;
		queue<BufferData*> m_bufferQueue;
		mutable PMutexC m_queueMutex;
		mutable PMutexC m_statsMutex;
		unsigned int m_bufferSize;
		int m_numFramesSaved;
		int m_numFramesDropped;
		int m_fpsMeasurementNumFramesSaved;
		__time64_t m_fileStartTime;
		__time64_t m_recordingMeasurementStartSeconds;
		int m_prevFramesPerSecond;
		Adept* const m_pAdeptInterfaceThread;
};

#endif