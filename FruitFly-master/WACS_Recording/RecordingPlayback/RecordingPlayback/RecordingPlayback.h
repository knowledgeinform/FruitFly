#ifndef RecordingPlaybackH
#define RecordingPlaybackH

#include <windows.h>
#include <string>
#include <sys/timeb.h>
#include "PThreadC.h"
#include "PMutexC.h"

using namespace std;

class RecordingPlayback;

class FrameReaderThread : public PThreadC
{
	public:
		FrameReaderThread(RecordingPlayback &oRecordingPlayback);

		void Rewind();

	protected:
		virtual void Run();

	private:
		RecordingPlayback &m_oRecordingPlayback;
		HANDLE m_hFindFiles;
		WIN32_FIND_DATA m_oFindFileData;
		bool m_bMoreFiles;
};


class RecordingPlayback : public PThreadC
{
	public:
		RecordingPlayback(const string &tInputDirectory);
		virtual ~RecordingPlayback();

		void Play();
		void Pause();
		void Rewind();
		bool HasEnded() const;

		void* GetLatestFrame();

		friend class FrameReaderThread;

	protected:
		virtual void Run();

	private:
		struct FrameData
		{
			long long int lTimestampSec;
			unsigned short nTimestampMillisec;
			void *pBuffer;
		};

		void ReadImageFile(const string &tFileName);
		void ExtractTimestampFromFileName(const string &tFilename, FrameData &oFrameData);
		string m_tInputDirectory;
		bool m_bIsPlaying;
		bool m_bHasReachedEnd;
		bool m_bHasStarted;
		mutable PMutexC m_oFrameMutex;
		mutable PMutexC m_oModeMutex;
		_timeb m_oRecordingStartLocalTime;
		__time64_t m_lFirstFileTimeSeconds;
		unsigned short m_lFirstFileTimeMilliseconds;
		FrameReaderThread m_oFrameReaderThread;

		static const int NUM_FRAME_BUFFERS = 3;
		FrameData m_pFrameBuffers[NUM_FRAME_BUFFERS];
		int m_nCurrFrameIndex;
		int m_nNewestFrameIndex;

};


#endif