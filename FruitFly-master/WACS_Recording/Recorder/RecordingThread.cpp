#include "RecordingThread.h"
#include "PAutoLockC.h"
#include <sys/timeb.h>
#include <fstream>
#include <sstream>
#include <windows.h>

static const float RECORDING_FORMAT_VERSION_NUM = 2.0f;
static const unsigned int RECORDING_FILE_DURATION_SEC = 5;
static const unsigned int RECORDING_FPS_MEASUREMENT_DURATION_SEC = 3;
static const unsigned int RECORDING_QUEUE_DROP_LENGTH = 5000;

RecordingThread::RecordingThread(const string &recordingDirectory, const unsigned int bufferSize, RecordingStatus &status, Adept* const pAdeptInterfaceThread)
:m_recordingDirectory(recordingDirectory),
 m_status(status),
 m_pAdeptInterfaceThread(pAdeptInterfaceThread),
 m_warpQueue(*WarpQueue::Instance())
{
	if (pAdeptInterfaceThread)
	{
		m_warpQueue.SetAdeptInterface(pAdeptInterfaceThread);
	}

	m_bufferSize = bufferSize;
	m_numFramesSaved = 0;
	m_fpsMeasurementNumFramesSaved = 0;
	m_numFramesDropped = 0;
	m_fileStartTime = 0;
	m_prevFramesPerSecond = 0;

	_timeb currTime;
	_ftime_s(&currTime);
	m_recordingMeasurementStartSeconds = currTime.time;
}

void RecordingThread::AddBufferToQueue(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum)
{
	BufferData* const pBufferData = new BufferData();
	pBufferData->pBuffer = (void*)pBuffer;
	if (m_pAdeptInterfaceThread)
	{
		if (!m_warpQueue.GetWarp(acquisitionTimeSec, acquisitionTimeMillisec, pBufferData->pOddWarpParams, pBufferData->pEvenWarpParams))
		{
			m_status.IncrementWarpQueueUnderflows();
		}
	}
	pBufferData->fileName = fileName;
	pBufferData->acquisitionTimeSec = acquisitionTimeSec;
	pBufferData->acquisitionTimeMillisec = acquisitionTimeMillisec;
	pBufferData->channelNum = channelNum;

	PAutoLockC lock(m_queueMutex);
	m_bufferQueue.push(pBufferData);
}

int RecordingThread::GetQueueLength() const
{
	PAutoLockC lock(m_queueMutex);
	return m_bufferQueue.size();
}

int RecordingThread::GetNumFramesRecorded() const
{
	PAutoLockC lock(m_statsMutex);
	return m_numFramesSaved;
}

int RecordingThread::GetNumFramesDropped() const
{
	PAutoLockC lock(m_statsMutex);
	return m_numFramesDropped;
}

void RecordingThread::ResetFrameCounts()
{
	PAutoLockC lock(m_statsMutex);
	m_numFramesSaved = 0;
	m_numFramesDropped = 0;
}

int RecordingThread::GetNumFramesRecordedPerSec()
{
	int framesPerSecond;

	_timeb currTime;
	_ftime_s(&currTime);

	PAutoLockC lock(m_statsMutex);
	__time64_t duration = currTime.time - m_recordingMeasurementStartSeconds;

	//
	// Measure for a few seconds before displaying new value
	//
	if (duration >= RECORDING_FPS_MEASUREMENT_DURATION_SEC)
	{
		framesPerSecond = (int)(((float)m_fpsMeasurementNumFramesSaved) / duration);
		m_prevFramesPerSecond = framesPerSecond;

		m_recordingMeasurementStartSeconds = currTime.time;
		m_fpsMeasurementNumFramesSaved = 0;			
	}	
	else
	{
		framesPerSecond = m_prevFramesPerSecond;
	}

	return framesPerSecond;
}

void RecordingThread::SetIsPaused(const bool isPaused)
{
	m_warpQueue.SetIsPaused(isPaused);
}

void RecordingThread::Run()
{
	const BufferData *pBufferData;

	float warpParams[Adept::NUM_WARP_PARAMS_PER_FIELD];
	int warpBufferSize;

	if (m_pAdeptInterfaceThread)
	{
		warpBufferSize = sizeof(float) * m_pAdeptInterfaceThread->NUM_WARP_PARAMS_PER_FIELD * 2;							
	}
	else
	{
		warpBufferSize = 0;
	}

	ofstream fileOut;
	while (true)
	{
		int queueLength = GetQueueLength();
		if (queueLength > 0)
		{
			//while (queueLength > 0)
			{
				m_queueMutex.Lock();
				pBufferData = m_bufferQueue.front();
				m_bufferQueue.pop();		
				m_queueMutex.Unlock();

				if (queueLength <= RECORDING_QUEUE_DROP_LENGTH || (queueLength <= (RECORDING_QUEUE_DROP_LENGTH*1.5) && (m_numFramesSaved % 3) != 1))
				{
					//
					// Put multiple images into the same file to speed up I/O.
					// After RECORDING_FILE_DURATION_SEC seconds, close the file
					// and start a new one.
					//
					if ((pBufferData->acquisitionTimeSec - m_fileStartTime) > RECORDING_FILE_DURATION_SEC)
					{
						if (fileOut.is_open())
						{
							fileOut.close();
						}

						//
						// Open the new file and write the following fields:
						//    -format version num
						//    -frame buffer size
                        //    -warp buffer size					
 						// The recording file has multiple frames inside it.
						//
						_timeb currTime;
						_ftime_s(&currTime);
						ostringstream recordingFileName;
						recordingFileName << m_recordingDirectory << currTime.time << ".rec";					
						fileOut.open(recordingFileName.str().c_str(), ios::binary | ios::out);
						fileOut.write((char*)&RECORDING_FORMAT_VERSION_NUM, sizeof RECORDING_FORMAT_VERSION_NUM);
						fileOut.write((char*)&m_bufferSize, sizeof m_bufferSize);
						fileOut.write((char*)&warpBufferSize, sizeof warpBufferSize);

						m_fileStartTime = pBufferData->acquisitionTimeSec;
					}

					fileOut.write(pBufferData->fileName.c_str(), pBufferData->fileName.length() + 1);
					fileOut.write((char*)pBufferData->pBuffer, m_bufferSize);
					fileOut.write((char*)pBufferData->pOddWarpParams, warpBufferSize / 2);
					fileOut.write((char*)pBufferData->pEvenWarpParams, warpBufferSize / 2);

					m_statsMutex.Lock();
					++m_numFramesSaved;	
					++m_fpsMeasurementNumFramesSaved;
					m_statsMutex.Unlock();
				}
				else
				{
					m_statsMutex.Lock();
					++m_numFramesDropped;	
					m_statsMutex.Unlock();
				}

				if (pBufferData)
				{
					delete[] pBufferData->pBuffer;
				}
				delete pBufferData;			

				queueLength = GetQueueLength();

				m_status.SetFrameRate(GetNumFramesRecordedPerSec());
				m_status.SetNumFramesInQueue(queueLength);
				m_status.SetNumFramesDropped(GetNumFramesDropped());			
			}
		}
		else
		{
			m_status.SetFrameRate(GetNumFramesRecordedPerSec());
			m_status.SetNumFramesInQueue(GetQueueLength());
			m_status.SetNumFramesDropped(GetNumFramesDropped());			
		}
		
		::Sleep(5);
	}
}