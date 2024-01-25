#include "PlumeDetector.h"
#include "OrlandoVideoCapture.h"
#include "edu_jhuapl_nstd_swarm_WACSAgent_ImageProcessingThread.h"
#include <stdio.h>
#include <conio.h>
#include <string.h>
#include <windows.h>
#include <time.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <time.h>
#include <winsock2.h>
#include "IRPlumeTracker.h"
#include "RecordingStatus.h"
#include "CommandListenerThread.h"
#include "RecordingThread.h"
#include "ConnectivityThread.h"


static JNIEnv* s_env = NULL;
static jobject s_obj = NULL;
static IRPlumeTracker ir;

clock_t bef, aft;

const unsigned int UPDATE_PERIOD_MS = 667;


static const short COMMAND_LISTEN_PORT = 5377;
	

//
// This is the callback handler for when the image processing code
// detects a plume
//
static void detectionStatus(const float alignScore, const float detectionScore)
{
	//
	// Pass callback to java code
	//
	if (s_env && s_obj)
	{
		jclass cls = s_env->GetObjectClass(s_obj);
		jmethodID callbackMethodId = s_env->GetMethodID(cls, "detectionStatus", "(FF)V");
		s_env->CallVoidMethod(s_obj, callbackMethodId, alignScore, detectionScore);
	}
}

//
// Function pointer type definition for detonation detection callback
//
void (*detectionStatusCallbackFunc)(float, float);

//
// This function is called from the java thread. It takes over the
// thread and runs indefinitely.
//
JNIEXPORT void JNICALL Java_edu_jhuapl_nstd_swarm_WACSAgent_00024ImageProcessingThread_imageProcessingThreadProc(JNIEnv *env, jobject obj)
{
	s_env = env;
	s_obj = obj;

	Run();
}

JNIEXPORT void JNICALL Java_edu_jhuapl_nstd_swarm_WACSAgent_00024ImageProcessingThread_setParameter(JNIEnv *env, jobject obj, jstring key, jfloat val)
{
	jboolean b;
   	s_env = env;
	const char* s = env->GetStringUTFChars(key,&b);	
	string skey(s);
	ir.setParameter(skey, val);
	ir.m_IAlign.setParameter(skey,val);
}

static RecordingThread *s_pRecordingThread = NULL;
static ConnectivityThread *s_pConnectivityThread = NULL;

void FrameArrivedCallback(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum)
{
	s_pRecordingThread->AddBufferToQueue(pBuffer, fileName, acquisitionTimeSec, acquisitionTimeMillisec, channelNum);	
}

void __declspec(dllexport) Run()
{
	double alignerror;
	double detection;

	string path;
	string base;
	int framestart;
	int framestep;
	int frame ; 
	stringstream fullpath; 
	IplImage* imgin;
	float* imgptr;
	bool test = false;
	bool orlando = true;


	if(orlando)
	{
		OrlandoVideoCapture::InitializeBoard(true);
	}

	//
	// Initialize Orlando input/output channels
	//
	OrlandoVideoCapture oChannel1(ORL_MODULE_VIDEO_IN0, ORL_PORT_CVBS0, ORL_MODULE_VIDEO_OUT0, ORL_PORT_CVBS0); 
	//OrlandoVideoCapture oChannel2(ORL_MODULE_VIDEO_IN1, ORL_PORT_CVBS0);	 

	if(orlando)
	{
		oChannel1.InitializeRecording("channel1\\", FrameArrivedCallback);
	}

		
		
	bool currentlyRecording = false;

	//
	// Start recording thread
	//
	RecordingStatus recordingStatus;
	recordingStatus.SetIsRunning(true);
	recordingStatus.SetHasReceivedTimeSync(true);
	recordingStatus.SetIsRecording(false);
	recordingStatus.SetFrameRate(0);
	recordingStatus.SetNumFramesInQueue(0);
	recordingStatus.SetNumFramesDropped(0);
	recordingStatus.SetIsAdeptPresent(false);
	recordingStatus.SetNumWarpQueueUnderflows(0);
	s_pRecordingThread = new RecordingThread("d:\\recording\\", oChannel1.GetFrameBufferSize(), recordingStatus, NULL);
	s_pRecordingThread->Start();


	//
	// Initialize winsock library	
	//	
	WSADATA wsaData;
	if (WSAStartup(0x0202, &wsaData) != 0)
	{
		printf("Could not initialize winsock library\n");
		exit(1);
	}

	//
	// Start connectivity thread
	//
	s_pConnectivityThread = new ConnectivityThread(recordingStatus);
	s_pConnectivityThread->Start();


	//
	// Start command listenter thread
	//
	CommandListenerThread commandListenerThread(recordingStatus, COMMAND_LISTEN_PORT);
	commandListenerThread.Start();

	
	const void* const pFrame = NULL;

	if(test)
	{
		path = "C://WACSTest/channel1/";
		base = "1242702290_31_";
		framestart = 2700;
		framestep = 20;
		frame = framestart; 
		fullpath << path << base << framestart << ".bmp";
		imgin = cvLoadImage(fullpath.str().c_str());
	}
	else
	{
		if(orlando)
		{
			imgin = cvCreateImage(cvSize(320,240), IPL_DEPTH_32F,1);
			imgptr = reinterpret_cast<float*>(imgin->imageData);
			const void* pFrame;
			do
			{
				pFrame = oChannel1.GetLatestFrame();
				if (pFrame == NULL)
				{
					::Sleep(10);
				}					
			}
			while(pFrame == NULL);
			ir.m_IAlign.copyByteDataIntoImage(imgptr,(uchar*)pFrame,240,320);
		}
	}

	ir.addFrame(imgin);


	DWORD dwLastUpdateTime_MS = ::GetTickCount();
	bool bRunning = true;
	while (bRunning)
	{
		bef = clock();
		if(test)
		{
			frame = frame + framestep;
			fullpath.str("");
			fullpath << path << base << frame << ".bmp";
			imgin = cvLoadImage(fullpath.str().c_str());
		}
		else
		{
			const void* pFrame;
			do
			{
				pFrame = oChannel1.GetLatestFrame();
				if (pFrame == NULL)
				{
					::Sleep(10);
				}					
			}
			while(pFrame == NULL);
			ir.m_IAlign.copyByteDataIntoImage(imgptr,(uchar*)pFrame,240,320);
		}

		ir.addFrame(imgin);
		aft = clock();
		printf("Cycle took %f seconds\n", double(aft-bef)/CLOCKS_PER_SEC);

		alignerror = ir.getAlignError();
		detection = ir.getDetection();

		detectionStatus((float)alignerror,(float)detection);

		if (recordingStatus.GetIsRecording())
		{
			if (!currentlyRecording)
			{
				oChannel1.StartRecording();
				s_pRecordingThread->SetIsPaused(false);
				currentlyRecording = true;
			}
		}
		else if (currentlyRecording)
		{
			oChannel1.StopRecording();
			s_pRecordingThread->SetIsPaused(true);
			currentlyRecording = false;		
		}


		//
		// Sleep until it's time to process a new frame
		//
		DWORD dwCurrTime_MS = ::GetTickCount();
		DWORD dwDeltaTime_MS = dwCurrTime_MS - dwLastUpdateTime_MS;
		if (dwDeltaTime_MS < UPDATE_PERIOD_MS)
		{
			::Sleep(UPDATE_PERIOD_MS - dwDeltaTime_MS);
		}

		dwLastUpdateTime_MS = dwCurrTime_MS;
	}

	if(orlando)
		OrlandoVideoCapture::TerminateBoard();
}