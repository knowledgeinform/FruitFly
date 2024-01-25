#include "RecordingPlayback.h"
#include "PAutoLockC.h"
#include <iostream>
#include <fstream>
#include <sstream>

static const int FRAME_SIZE = 76800;
static const int WARP_DATA_SIZE = 72;
static const int PLAYBACK_THREAD_SLEEP_TIME = 5;
static const int READ_THREAD_SLEEP_TIME = 5;

RecordingPlayback::RecordingPlayback(const string &tInputDirectory)
:m_oFrameReaderThread(*this)
{
	for (int i = 0; i < NUM_FRAME_BUFFERS; ++i)
	{
		m_pFrameBuffers[i].pBuffer = malloc(FRAME_SIZE + WARP_DATA_SIZE);	
	}
	m_nCurrFrameIndex = -1;
	m_nNewestFrameIndex = -1;
	m_bIsPlaying = false;
	m_bHasReachedEnd = false;
	m_bHasStarted = false;

	m_tInputDirectory = tInputDirectory;
	if (m_tInputDirectory[m_tInputDirectory.length() - 1] != '\\')
	{
		m_tInputDirectory += '\\';	
	}
	m_tInputDirectory += "*.frame";
}

RecordingPlayback::~RecordingPlayback()
{
	for (int i = 0; i < NUM_FRAME_BUFFERS; ++i)
	{
		free(m_pFrameBuffers[i].pBuffer);
	}
}

void RecordingPlayback::Play()
{
	if (!m_bHasStarted)
	{
		_ftime_s(&m_oRecordingStartLocalTime);
		Start();
		m_oFrameReaderThread.Start();
		m_bHasStarted = true;
	}

	PAutoLockC oLock(m_oModeMutex);
	m_bIsPlaying = true;	
}

void RecordingPlayback::Pause()
{
	PAutoLockC oLock(m_oModeMutex);
	m_bIsPlaying = false;
}

void RecordingPlayback::Rewind()
{
	PAutoLockC oLock(m_oFrameMutex);

	_ftime_s(&m_oRecordingStartLocalTime);
	m_oFrameReaderThread.Rewind();
}

bool RecordingPlayback::HasEnded() const
{
	PAutoLockC oLock(m_oModeMutex);
	return m_bHasReachedEnd;
}

void* RecordingPlayback::GetLatestFrame()
{
	PAutoLockC oLock(m_oFrameMutex);
	if (m_nCurrFrameIndex >= 0)
	{
		return m_pFrameBuffers[m_nCurrFrameIndex].pBuffer;
	}
	else
	{
		return NULL;
	}
}

void RecordingPlayback::ReadImageFile(const string &tFileName)
{
	m_oFrameMutex.Lock();
	FrameData* const pFrameData = &m_pFrameBuffers[(m_nNewestFrameIndex + 1) % NUM_FRAME_BUFFERS];
	m_oFrameMutex.Unlock();
	
	ExtractTimestampFromFileName(tFileName, *pFrameData);

	ifstream oFileIn(tFileName.c_str(), ios::in | ios::binary);
	float fFileFormatVersion;
	int nFrameSize;
	int nWarpDataSize;
	oFileIn.read((char*)&fFileFormatVersion, sizeof(fFileFormatVersion));
	oFileIn.read((char*)&nFrameSize, sizeof(nFrameSize));
	oFileIn.read((char*)&nWarpDataSize, sizeof(nWarpDataSize));

	if (nFrameSize != FRAME_SIZE)
	{
		throw new exception("File frame size does not match pre-allocated buffer size");
	}
	else if (nWarpDataSize != WARP_DATA_SIZE)
	{
		throw new exception("File warp data size does not match pre-allocated buffer size");
	}

	oFileIn.read((char*)pFrameData->pBuffer, nFrameSize + nWarpDataSize);

	PAutoLockC oLock(m_oFrameMutex);
	m_nNewestFrameIndex = (m_nNewestFrameIndex + 1 % NUM_FRAME_BUFFERS);
	if (m_nCurrFrameIndex < 0)
	{
		m_nCurrFrameIndex = m_nNewestFrameIndex;
	}
}

void RecordingPlayback::ExtractTimestampFromFileName(const string &tFilename, FrameData &oFrameData)
{
	string::size_type firstDelimiterIndex = tFilename.find("_");
	istringstream timestampSecondsStream(tFilename.substr(0, firstDelimiterIndex));
	timestampSecondsStream >> oFrameData.lTimestampSec;
	
	string::size_type secondDelimiterIndex = tFilename.find("_");
	istringstream timestampMillisecondsStream(tFilename.substr(firstDelimiterIndex + 1, secondDelimiterIndex - firstDelimiterIndex));
	timestampMillisecondsStream >> oFrameData.nTimestampMillisec;
}

void RecordingPlayback::Run()
{
	while (true)
	{
		if (m_nCurrFrameIndex >= 0)
		{
			_timeb oCurrTime;
			_ftime_s(&oCurrTime);
			m_oFrameMutex.Lock();
			__time64_t lRealtimeElapsedSeconds = oCurrTime.time - m_oRecordingStartLocalTime.time;
			unsigned short nRealtimeElapsedMilliseconds = oCurrTime.millitm - m_oRecordingStartLocalTime.millitm;
			
			int nNewCurrFrameIndex = m_nCurrFrameIndex;
			for (int i = 1; i <= (m_nNewestFrameIndex - m_nCurrFrameIndex); ++i)
			{
				if (((m_pFrameBuffers[m_nCurrFrameIndex + i].lTimestampSec - m_lFirstFileTimeSeconds) > 0) ||
					(((m_pFrameBuffers[m_nCurrFrameIndex + i].lTimestampSec - m_lFirstFileTimeSeconds) == 0) && (m_pFrameBuffers[m_nCurrFrameIndex + i].nTimestampMillisec - m_lFirstFileTimeMilliseconds >= 0)))
				{
					++nNewCurrFrameIndex;
				}
			}

			m_nCurrFrameIndex = nNewCurrFrameIndex;

			m_oFrameMutex.Unlock();
			
		}

		::Sleep(PLAYBACK_THREAD_SLEEP_TIME);
	}
}

FrameReaderThread::FrameReaderThread(RecordingPlayback &oRecordingPlayback)
:m_oRecordingPlayback(oRecordingPlayback)
{
}

void FrameReaderThread::Run()
{
	// Initialize file list
	Rewind();


	string tFileName;
	int nNumFramesReadAhead;

	while (true)
	{
		m_oRecordingPlayback.m_oFrameMutex.Lock();
		if(m_bMoreFiles)
		{
			if (m_oRecordingPlayback.m_nNewestFrameIndex >= m_oRecordingPlayback.m_nCurrFrameIndex)
			{
				nNumFramesReadAhead = m_oRecordingPlayback.m_nNewestFrameIndex - m_oRecordingPlayback.m_nCurrFrameIndex;
			}
			else
			{
				nNumFramesReadAhead = (m_oRecordingPlayback.m_nNewestFrameIndex + m_oRecordingPlayback.NUM_FRAME_BUFFERS) - m_oRecordingPlayback.m_nCurrFrameIndex;
			}			
			m_oRecordingPlayback.m_oFrameMutex.Unlock();

			if (nNumFramesReadAhead < (RecordingPlayback::NUM_FRAME_BUFFERS - 1))
			{
				m_oRecordingPlayback.ReadImageFile(m_oFindFileData.cFileName);
			}

			m_oRecordingPlayback.m_oFrameMutex.Lock();
			m_bMoreFiles = FindNextFile(m_hFindFiles, &m_oFindFileData);
			m_oRecordingPlayback.m_oFrameMutex.Unlock();
		}
		else
		{
			m_oRecordingPlayback.m_oFrameMutex.Unlock();
		}

		::Sleep(READ_THREAD_SLEEP_TIME);
	}
}

void FrameReaderThread::Rewind()
{
	m_oRecordingPlayback.m_oFrameMutex.Lock();
	m_oRecordingPlayback.m_nCurrFrameIndex = -1;
	m_oRecordingPlayback.m_nCurrFrameIndex = -1;
	m_hFindFiles = ::FindFirstFile(m_oRecordingPlayback.m_tInputDirectory.c_str(), &m_oFindFileData);
	m_oRecordingPlayback.m_oFrameMutex.Unlock();

	if (m_hFindFiles == INVALID_HANDLE_VALUE)
	{
		cerr << "Invalid directory specified" << endl;
		m_bMoreFiles = false;
	}
	else
	{
		m_bMoreFiles = true;
	}
}