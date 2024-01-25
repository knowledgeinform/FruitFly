#include "OrlandoVideoCapture.h"
#include "EthernetVideoServer.h"
#include "PAutoLockC.h"
#include "orl_dll.h"
#include "interr.h"
#include <sstream>
#include <fstream>
#include <sys\timeb.h>
#include <windows.h>
using std::ostringstream;
using std::ofstream;
using std::ios;

static const unsigned int MAX_ORLANDO_VIDEO_CAPTURE_ERRORS = 100;
static const unsigned int NUM_ORLANDO_HOST_BUFFERS_PER_CHANNEL = 3;
bool OrlandoVideoCapture::s_boardInitialized = false;
OrlandoVideoCaptureThread OrlandoVideoCapture::s_videoCaptureThread;
string OrlandoVideoCapture::s_recordingDirectory;
typedef OrlandoVideoCapture* OrlandoVideoCapturePtr;
static OrlandoVideoCapturePtr s_moduleToVideoCaptureObjMap[2] = {NULL, NULL};
ORL_HANDLE OrlandoVideoCapture::s_boardHandle;
PMutexC OrlandoVideoCapture::s_videoCaptureObjectListMutex;
static EthernetVideoServer *s_pEthernetVideoServer = NULL;
static int s_nEthernetVideoListenPort = 3999;

int compare_string(const void* str1, const void* str2)
{
	return strcmp(*(char**)str1, *(char**)str2);
}


void OrlandoVideoCapture::InitializeBoard(const bool enableEthernetVideoServer)
{
	if (!s_boardInitialized)
	{
		if (enableEthernetVideoServer && s_pEthernetVideoServer == NULL)
		{
			s_pEthernetVideoServer = new EthernetVideoServer(s_nEthernetVideoListenPort, 320, 240);
			s_pEthernetVideoServer->Start();
			printf("Ethernet video server listening on port %i\n", s_nEthernetVideoListenPort);
		}

		OrlRet ret = LoadOrlandoLib();

		if (ret == ORL_RET_SUCCESS)
		{
			ret = FOrlOpen(1, &s_boardHandle);

			if (ret == ORL_RET_SUCCESS)
			{
				SOrlBoardInfo boardinfo;
				ORLINITS(boardinfo);
				ret = FOrlFunc(s_boardHandle, ORLF_GETBOARDINFO, &boardinfo);
				if ( ret == ORL_RET_SUCCESS )
				{
					printf( "OK: Board info: btype: %d [OrlBoardType].\n", boardinfo.btype );
					UploadFirmware(s_boardHandle);
				}
				else
				{
					throw std::exception("Failed getting board info");
				}
			}
			else
			{
				throw std::exception("Call to OrlOpen() failed");
			}

			s_boardInitialized = true;
			s_videoCaptureThread.Start();		
		}
		else
		{
			throw std::exception("Call to LoadOrlandoLib() failed");
		}
	}
}

void OrlandoVideoCapture::TerminateBoard()
{
	if (s_boardInitialized)
	{
		s_videoCaptureThread.Stop();

		::Sleep(500);

		s_boardInitialized = false;
		OrlClose(s_boardHandle);	
	}
}

OrlRet OrlandoVideoCapture::UploadFirmware(const ORL_HANDLE hbrd)
{
	OrlRet ret = ORL_RET_SUCCESS;
	SOrlFirmwareDir	fwdir;
	SOrlProgramFirmware fw;
	ORLINITS( fwdir );
	ORLINITS( fw );

//	fw.opt |= ORL_OPT_DSPEMUL_MODE;/* only used by ARVOO, otherwise: remove this option */
	printf( "_UploadFirmware opt:0x%lx\n", (unsigned long)fw.opt );

	fwdir.buffsize = 256;
	fwdir.dirstr = (ArvPCHAR)malloc( fwdir.buffsize );

	/* shows the firmware directory */

	printf( "Firmware files directory: " );
	ret = FOrlFunc( hbrd, ORLF_GET_FIRMWARE_DIR, &fwdir );
	if ( ret != ORL_RET_SUCCESS )
	{
		printf( "failed (err=%d)\n", (int)ret );
	}
	else
	{
		printf( "OK, result:\n\"%s\"\n\n", fwdir.dirstr );
	}
	free( fwdir.dirstr );
	fwdir.buffsize = 0;
	fwdir.dirstr = NULL;

	/* Upload firmware */
	printf( "Upload firmware to orlando board..." );
	ret = FOrlFunc( hbrd, ORLF_PROGRAM_FIRMWARE, &fw );
	if ( ret != ORL_RET_SUCCESS )
	{
		printf( "failed, ret:%d.\n", (int)ret );
	}
	else
	{
		printf( "OK\n" );
	}
	return ret;
}



OrlandoVideoCapture::OrlandoVideoCapture(OrlModule inputModule,
										 OrlPort inputPort,
										 OrlVideoTime videoTiming,
 										 ArvDWORD inputWidth,
										 ArvDWORD inputHeight,
										 ArvDWORD captureWidth,
										 ArvDWORD captureHeight,
							             OrlPixelFormat hostPixelFormat,
							             OrlFieldArrange hostLineArrangement, 
										 OrlVideoBCSMode bcsMode,
										 ArvDWORD brightness,
										 ArvSDWORD contrast,
										 ArvSDWORD saturation,
										 ArvSDWORD hue,
										 OrlVideoGainMode gainMode,
										 ArvDWORD gain_y_cvbs,
										 ArvDWORD gain_c)
:m_inputModule(inputModule),
 m_inputVideoTiming(videoTiming),
 m_inputWidth(inputWidth),
 m_inputHeight(inputHeight), 
 m_isAcquiringVideo(false),
 m_bufferIndexLatest(-1),
 m_bufferIndexClientProcessing(-1),
 m_isReadyForNextFrame(true),
 m_isRecordingEnabled(false),
 m_isAnalogOutputEnabled(false),
 m_frameArriveCallback(NULL)
{
	if (!s_boardInitialized)
	{
		InitializeBoard();
	}

	InitInput(inputModule,
		      inputPort,
			  videoTiming,
			  inputWidth,
			  inputHeight,
			  captureWidth,
			  captureHeight,
			  hostPixelFormat,
			  hostLineArrangement,
			  bcsMode,
			  brightness,
			  contrast,
			  saturation,
			  hue,
			  gainMode,
			  gain_y_cvbs,
			  gain_c);

	EnableInput();
}

OrlandoVideoCapture::OrlandoVideoCapture(OrlModule inputModule,
										 OrlPort inputPort,
										 OrlModule outputModule,
										 OrlPort outputPort,
										 OrlVideoTime videoTiming,
 										 ArvDWORD inputWidth,
										 ArvDWORD inputHeight,
										 ArvDWORD captureWidth,
										 ArvDWORD captureHeight,
							             OrlPixelFormat hostPixelFormat,
							             OrlFieldArrange hostLineArrangement, 
										 OrlVideoBCSMode bcsMode,
										 ArvDWORD brightness,
										 ArvSDWORD contrast,
										 ArvSDWORD saturation,
										 ArvSDWORD hue,
										 OrlVideoGainMode gainMode,
										 ArvDWORD gain_y_cvbs,
										 ArvDWORD gain_c)
:m_inputModule(inputModule),
 m_outputModule(outputModule),
 m_inputVideoTiming(videoTiming),
 m_inputWidth(inputWidth),
 m_inputHeight(inputHeight), 
 m_isAcquiringVideo(false),
 m_bufferIndexLatest(-1),
 m_bufferIndexClientProcessing(-1),
 m_isReadyForNextFrame(true),
 m_isRecordingEnabled(false),
 m_isAnalogOutputEnabled(true),
 m_frameArriveCallback(NULL)
{
	InitInput(inputModule,
		      inputPort,
			  videoTiming,
			  inputWidth,
			  inputHeight,
			  captureWidth,
			  captureHeight,
			  hostPixelFormat,
			  hostLineArrangement,
			  bcsMode,
			  brightness,
			  contrast,
			  saturation,
			  hue,
			  gainMode,
			  gain_y_cvbs,
			  gain_c);

	InitOutput(outputModule, outputPort);

	EnableInput();

	EnableOutput();
}

void OrlandoVideoCapture::InitInput(OrlModule inputModule,
									OrlPort inputPort,
									OrlVideoTime videoTiming,
									ArvDWORD inputWidth,
									ArvDWORD inputHeight,
									ArvDWORD captureWidth,
									ArvDWORD captureHeight,
									OrlPixelFormat hostPixelFormat,
									OrlFieldArrange hostLineArrangement, 
									OrlVideoBCSMode bcsMode,
									ArvDWORD brightness,
									ArvSDWORD contrast,
									ArvSDWORD saturation,
									ArvSDWORD hue,
									OrlVideoGainMode gainMode,
									ArvDWORD gain_y_cvbs,
									ArvDWORD gain_c)
{
	//
	// Initialize input module
	//
	SOrlModInSett moduleSettings;
	ORLINITS(moduleSettings);
	moduleSettings.module_in = inputModule;
	moduleSettings.port = inputPort;
	moduleSettings.vtime = videoTiming;
	moduleSettings.gain_mode = gainMode;
	moduleSettings.gain_y_cvbs = gain_y_cvbs;
	moduleSettings.gain_c = gain_c;
	moduleSettings.bcs_mode = bcsMode;
	moduleSettings.brightness = brightness;
	moduleSettings.contrast = contrast;
	moduleSettings.saturation = saturation;
	moduleSettings.hue = hue;
	moduleSettings.flags = 0;
	moduleSettings.roi.xoffs = 12;
	moduleSettings.roi.yoffs = 21;
	moduleSettings.roi.width = inputWidth - 24;
	moduleSettings.roi.height = inputHeight - 21;
	moduleSettings.scale_width_out = captureWidth;
	moduleSettings.scale_height_out = (hostLineArrangement == ORL_FLD_INTERLACED) ? captureHeight / 2 : captureHeight;
	OrlRet ret = FOrlFunc(s_boardHandle, ORLF_INIT_MODULE_IN, &moduleSettings);	



	//
	// Initialize board to host module
	//
	if (inputModule == ORL_MODULE_VIDEO_IN0)
	{
		m_boardToHostModule = ORL_MODULE_BRD2HOST0;
	}
	else
	{
		m_boardToHostModule = ORL_MODULE_BRD2HOST1;
	}
	SOrlModHostSett boardToHostSettings;
	ORLINITS(boardToHostSettings);
	boardToHostSettings.module_host = m_boardToHostModule;
	boardToHostSettings.module_src = inputModule;
	boardToHostSettings.pos_x_src = 0;
	boardToHostSettings.pos_y_src = 0;
	boardToHostSettings.bg_color_out = ORL_YCBCR_BLACK;
	boardToHostSettings.fieldarrange_host = hostLineArrangement;
	boardToHostSettings.format_host = hostPixelFormat;
	boardToHostSettings.width = moduleSettings.scale_width_out;
	boardToHostSettings.height = moduleSettings.scale_height_out;
	ret = FOrlFunc(s_boardHandle, ORLF_INIT_MODULE_BRD2HOST, &boardToHostSettings);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error initializing board to host module");
		return;
	}


	//
	// Allocate host buffers
	//
	SOrlAllocPlHostBuff hostBuffer;
	ORLINITS(hostBuffer);
	hostBuffer.numplanes = boardToHostSettings.host_numplanes;
	hostBuffer.plane0.sz = boardToHostSettings.host_pl0;
	hostBuffer.plane1.sz = boardToHostSettings.host_pl1;
	hostBuffer.plane2.sz = boardToHostSettings.host_pl2;
	
	for (int i = 0; i < NUM_ORLANDO_HOST_BUFFERS_PER_CHANNEL; ++i)
	{
		ret = FOrlFunc(s_boardHandle, ORLF_ALLOC_HBUFF, &hostBuffer);

		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error allocating host buffer #" + i);
			return;
		}		

		m_hostBuffers[i].isInUse = false;
		m_hostBuffers[i].bufferNum = hostBuffer.buffernr;
		m_hostBuffers[i].pBuffer = hostBuffer.plane0.pbuff;
	}

	m_bufferSize = hostBuffer.plane0.sz.buff_size;
	
	s_videoCaptureObjectListMutex.Lock();	
	s_moduleToVideoCaptureObjMap[m_boardToHostModule - ORL_MODULE_BRD2HOST0] = this;
	s_videoCaptureObjectListMutex.Unlock();

	//
	// Enable interrupts on the board to host module
	//
	SOrlModInitInterr boardToHostInterruptSettings;
	ORLINITS(boardToHostInterruptSettings);
	boardToHostInterruptSettings.module = m_boardToHostModule;
	boardToHostInterruptSettings.interr_mask = ORL_MODINT_VSTRMASK;
	ret = FOrlFunc(s_boardHandle, ORLF_INIT_INTERR_MODULE, &boardToHostInterruptSettings );

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error enabling interrupts on board to host module");
		return;
	}
}

void OrlandoVideoCapture::InitOutput(OrlModule outputModule,
									 OrlPort outputPort)
{
		m_outputModule = outputModule;

		if (outputModule == ORL_MODULE_VIDEO_OUT0)
		{
			m_overlayModule = ORL_VMODULE_OVL_TEXT0;
			m_fusionModule = ORL_VMODULE_FUSION0;
		}
		else
		{
			m_overlayModule = ORL_VMODULE_OVL_TEXT1;
			m_fusionModule = ORL_VMODULE_FUSION1;
		}


		//
		// Initialize the overlay module
		//
		SOrlModTxtOverlaySett overlayModuleSettings;
		ORLINITS(overlayModuleSettings);
		overlayModuleSettings.module_ovl = m_overlayModule;
		overlayModuleSettings.outp_width = m_inputWidth;
		overlayModuleSettings.outp_height = m_inputHeight;
		OrlRet ret = FOrlFunc(s_boardHandle, ORLF_INIT_VMODULE_TEXT_OVL, &overlayModuleSettings);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error intializing overlay module");
		}

		//
		// Initialize the fusion module
		//
		SOrlColorRange key_color_range;
		SOrlModFusionSett fusionModuleSettings;
		ORLINITS(key_color_range);
		ORLINITS(fusionModuleSettings);
		// Make black pixels on the overlay transparent
		key_color_range.start0 = 0x10; //y
		key_color_range.end0 = 0x11;   //y
		key_color_range.start1 = 0x80; //cb
		key_color_range.end1 = 0x81;   //cb
		key_color_range.start2 = 0x80; //cr
		key_color_range.end2 = 0x81;   //cr
		fusionModuleSettings.module_fusion = m_fusionModule;
		fusionModuleSettings.module_src0 = m_inputModule;
		fusionModuleSettings.pos_x_src0 = 0;
		fusionModuleSettings.pos_y_src0 = 0;
		fusionModuleSettings.module_src1 = m_overlayModule;
		fusionModuleSettings.pos_x_src1 = 0;
		fusionModuleSettings.pos_y_src1 = 0;
		fusionModuleSettings.bg_color_out = ORL_YCBCR_BLACK;
		fusionModuleSettings.module_sync = fusionModuleSettings.module_src0;
		fusionModuleSettings.fusion_type = ORL_FUSION_IN1_OVER_IN0_KEYING;
		fusionModuleSettings.key_color = key_color_range;
		fusionModuleSettings.outp_width = m_inputWidth;
		fusionModuleSettings.outp_height = m_inputHeight;
		fusionModuleSettings.flags = ORL_FLAG_NOT_WAIT_FOR_NSYNC;
		ret = FOrlFunc(s_boardHandle, ORLF_INIT_VMODULE_FUSION, &fusionModuleSettings);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error intializing fusion module");
		}

		//
		// Initialize the output module
		//
		SOrlModOutSett outputModuleSettings;
		ORLINITS(outputModuleSettings);
		outputModuleSettings.module_out = outputModule;
		outputModuleSettings.module_src = m_inputModule;//m_fusionModule;
		outputModuleSettings.pos_x_src = 0;
		outputModuleSettings.pos_y_src = 0;
		outputModuleSettings.bg_color_out = ORL_YCBCR_BLACK;
		outputModuleSettings.port = outputPort;
		outputModuleSettings.vtime = ORL_VTIME_NTSC;
		outputModuleSettings.flags = 0UL;
		ret = FOrlFunc(s_boardHandle, ORLF_INIT_MODULE_OUT, &outputModuleSettings);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error intializing output module");
		}
}

void OrlandoVideoCapture::EnableInput()
{

	//
	// Enable the input module
	//
	SOrlModule inputModuleEnable;
	ORLINITS(inputModuleEnable);
	inputModuleEnable.module = m_inputModule;
	inputModuleEnable.enable = 1;
	OrlRet ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &inputModuleEnable);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error enabling input module");
		return;
	}


	//
	// Enable the board to host module
	//
	SOrlModule boardToHostModule;
	ORLINITS(boardToHostModule);
	boardToHostModule.module = m_boardToHostModule;
	boardToHostModule.enable = 1;
	FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &boardToHostModule);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error enabling host to board module");
		return;
	}
}

void OrlandoVideoCapture::EnableOutput()
{
		//
		// Enable the overlay module
		//
		SOrlModule overlayModule;
		ORLINITS(overlayModule);
		overlayModule.module = m_overlayModule;
		overlayModule.enable = 1;
		OrlRet ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &overlayModule);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error enabling overlay module");
		}

		//
		// Enable the fusion module
		//
		SOrlModule fusionModule;
		ORLINITS(fusionModule);
		fusionModule.module = m_fusionModule;
		fusionModule.enable = 1;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &fusionModule);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error enabling fusion module");
		}

		//
		// Enable the output module
		//
		SOrlModule omodule;
		ORLINITS(omodule);
		omodule.module = m_outputModule;
		omodule.enable = 1;
		ret= FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &omodule);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error enabling output module");
		}
}

OrlandoVideoCapture::~OrlandoVideoCapture()
{
	DisableOutput();

	//
	// Disable the input module
	//
	SOrlModule inputModuleData;
	ORLINITS(inputModuleData);
	inputModuleData.module = m_inputModule;
	inputModuleData.enable = 0;
	FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &inputModuleData);

	//
	// Destroy the input module
	//
	FOrlFunc(s_boardHandle, ORLF_MODULE_RELEASE, &inputModuleData);

	//
	// Free host buffers
	//
	SOrlAllocPlHostBuff hostBuffer;
	ORLINITS(hostBuffer);
	for (int i = 0; i < NUM_ORLANDO_HOST_BUFFERS_PER_CHANNEL; ++i)
	{
		hostBuffer.buffernr = m_hostBuffers[i].bufferNum;
		FOrlFunc(s_boardHandle, ORLF_FREE_HBUFF, &hostBuffer);
	}
}

bool OrlandoVideoCapture::HasErrors() const
{
	PAutoLockC lock(m_errorMutex);
	return m_errorQueue.size() > 0;
}

string OrlandoVideoCapture::GetError()
{
	PAutoLockC lock(m_errorMutex);
	string error = m_errorQueue.front();
	m_errorQueue.pop();

	return error;
}

void OrlandoVideoCapture::AddErrorToQueue(const string &error)
{
	PAutoLockC lock(m_errorMutex);
	m_errorQueue.push(error);
}

void* OrlandoVideoCapture::GetLatestFrame()
{
	void *pFrame = NULL;

	PAutoLockC lock(m_frameMutex);

	if (m_bufferIndexClientProcessing >= 0)
	{
		//
		// Free the previously processed frame and get a pointer to 
		// the latest frame
		//		
		m_hostBuffers[m_bufferIndexClientProcessing].isInUse = false;
	}

	if (m_bufferIndexLatest >= 0)
	{
		m_bufferIndexClientProcessing = m_bufferIndexLatest;
		pFrame = m_hostBuffers[m_bufferIndexLatest].pBuffer;
	}

	return pFrame;
}

void OrlandoVideoCapture::InitializeRecording(const string &recordingDirectory, FrameArrivedCallback frameArrivedCallback)
{
	PAutoLockC lock(m_recordingMutex);
	m_recordingDirectory = recordingDirectory;
	m_frameArriveCallback = frameArrivedCallback;
}

void OrlandoVideoCapture::StartRecording()
{
	PAutoLockC lock(m_recordingMutex);
	m_isRecordingEnabled = true;
}

void OrlandoVideoCapture::StopRecording()
{
	PAutoLockC lock(m_recordingMutex);
	m_isRecordingEnabled = false;
}

void OrlandoVideoCapture::CreateHostToBoardOutput(OrlModule hostToBoardModule, OrlModule outputModule, OrlPort outputPort, const int width, const int height, const string &inputDirectory)
{
	OrlRet ret;


	//
	// Initialize the host to board module
	//
	SOrlModHostSett hostToBoardModuleSettings;
	ORLINITSTRUCT(hostToBoardModuleSettings);
	hostToBoardModuleSettings.module_host = ORL_MODULE_HOST2BRD0;
	hostToBoardModuleSettings.fieldarrange_host = ORL_FLD_INTERLACED;
	hostToBoardModuleSettings.format_host = ORL_PIXFRMT_Y;
	hostToBoardModuleSettings.width = width;
	hostToBoardModuleSettings.height = height;
	OrlFunc(s_boardHandle, ORLF_INIT_MODULE_HOST2BRD, &hostToBoardModuleSettings);

	//
	// Initialize the output module
	//
	SOrlModOutSett outputModuleSettings;
	ORLINITS(outputModuleSettings);
	outputModuleSettings.module_out = outputModule;
	outputModuleSettings.module_src = hostToBoardModule;//m_fusionModule;
	outputModuleSettings.pos_x_src = 0;
	outputModuleSettings.pos_y_src = 0;
	outputModuleSettings.bg_color_out = ORL_YCBCR_BLACK;
	outputModuleSettings.port = outputPort;
	outputModuleSettings.vtime = ORL_VTIME_NTSC;
	outputModuleSettings.flags = 0UL;
	ret = FOrlFunc(s_boardHandle, ORLF_INIT_MODULE_OUT, &outputModuleSettings);
	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error intializing output module");
	}

	//
	// Allocate host buffer for host to board module
	//
	SOrlAllocPlHostBuff allocbuff;
	ORLINITS(allocbuff);
	allocbuff.numplanes = hostToBoardModuleSettings.host_numplanes;
	allocbuff.plane0.sz = hostToBoardModuleSettings.host_pl0;
	allocbuff.plane1.sz = hostToBoardModuleSettings.host_pl1;
	allocbuff.plane2.sz = hostToBoardModuleSettings.host_pl2;

	ret = FOrlFunc(s_boardHandle, ORLF_ALLOC_HBUFF, &allocbuff);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error allocating host to board buffer");
	}


	/**************************************************************/

	//
	// Enable the host to board module
	// 
	SOrlModule hostToBoardModuleEnable;
	ORLINITS(hostToBoardModuleEnable);
	hostToBoardModuleEnable.module = hostToBoardModule;
	hostToBoardModuleEnable.enable = 1;
	ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &hostToBoardModuleEnable);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error enabling host to board module");
		return;
	}	

	//
	// Enable the output module
	//
	SOrlModule outputModuleEnable;
	ORLINITS(outputModuleEnable);
	outputModuleEnable.module = outputModule;
	outputModuleEnable.enable = 1;
	ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &outputModuleEnable);

	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error enabling output module");
		return;
	}	


	m_pHostToBoardThread = new HostToBoardThread(inputDirectory, allocbuff.buffernr, hostToBoardModule);
	m_pHostToBoardThread->Start();
}

unsigned int OrlandoVideoCapture::GetFrameBufferSize() const
{
	return m_bufferSize;
}

void OrlandoVideoCapture::FramesArrivedInBoardBuffer(const int numFrames)
{
	// Get the current time so we can timestamp the frame
	_timeb timebuffer;
	_ftime_s(&timebuffer);	

	//
	// Transfer frame to host buffer
	//
	SOrlFrmTransf frameTransfer;
	ORLINITS(frameTransfer);
	frameTransfer.module = m_boardToHostModule;

	for (int i = 0; i < numFrames; ++i)
	{
		//
		// Find a free host buffer to transfer the frame to
		//
		int freeBufferIndex = -1;
		m_frameMutex.Lock();
		for (int bufferIndex = 0; bufferIndex < NUM_HOST_BUFFERS && freeBufferIndex < 0; ++bufferIndex)
		{
			if (!m_hostBuffers[bufferIndex].isInUse)
			{
				m_hostBuffers[bufferIndex].isInUse = true;
				freeBufferIndex = bufferIndex;
			}
		}
		m_frameMutex.Unlock();

		if (freeBufferIndex >= 0)
		{
			frameTransfer.buffernr = m_hostBuffers[freeBufferIndex].bufferNum;
			FrameCapture frameCapture;
			frameCapture.bufferIndex = freeBufferIndex;
			frameCapture.timestampSeconds = timebuffer.time;
			frameCapture.timestampMilliseconds = timebuffer.millitm;
			m_bufferTransferQueue.push(frameCapture);
			OrlRet ret = FOrlFunc(s_boardHandle, ORLF_FRM_TRANSF, &frameTransfer);
			if (ret != ORL_RET_SUCCESS)
			{
				AddErrorToQueue("Error transferring frame to host buffer");
				return;
			}

		}
		else
		{
			AddErrorToQueue("Host buffer overflow");
			return;
		}
	}
}

void OrlandoVideoCapture::FramesArrivedInHostBuffer(const int numFrames)
{
	//
	// Make a local copy of the mutex-protected recording settings
	//
	m_recordingMutex.Lock();
	bool isRecordingEnabled = m_isRecordingEnabled;
	string recordingDirectory = m_recordingDirectory;
	m_recordingMutex.Unlock();

	for (int i = 0; i < numFrames; ++i)
	{
		FrameCapture frameCapture = m_bufferTransferQueue.front();
		m_bufferTransferQueue.pop();


		//
		// Send latest frame to ethernet video server
		//
		s_pEthernetVideoServer->SetLatestFrame((unsigned char*)m_hostBuffers[frameCapture.bufferIndex].pBuffer, m_bufferSize);



		//
		// If recording is enabled, copy frame buffer and send it to frame arrived callback function
		//		
		if (isRecordingEnabled)
		{		

			SOrlSaveFile saveFileInfo;
			ORLINITS(saveFileInfo);
			ostringstream fileName;
			fileName << recordingDirectory << frameCapture.timestampSeconds << "_" << frameCapture.timestampMilliseconds;
	
			char *pBuffer = new char[m_bufferSize];
			memcpy(pBuffer, m_hostBuffers[frameCapture.bufferIndex].pBuffer, m_bufferSize);
			//s_pRecordingThread->AddBufferToQueue(pBuffer, fileName.str(), timebuffer.time);
			if (m_frameArriveCallback)
			{
				m_frameArriveCallback(pBuffer, fileName.str(), frameCapture.timestampSeconds, frameCapture.timestampMilliseconds, (int)m_inputModule);
			}

			//ArvCHAR	filenameBuffer[128];			
			//strcpy(filenameBuffer, fileName.str().c_str());
			//saveFileInfo.filename = filenameBuffer;
			//saveFileInfo.filenamelength = fileName.str().length();
			//saveFileInfo.hostbuffernr = m_hostBuffers[bufferIndex].bufferNum;
			//saveFileInfo.bmtype = ORL_BM_RAW;//ORL_BM_BMP_UD;
			//OrlRet ret = FOrlFunc(s_boardHandle, ORLF_SAVE_HOSTBUFF, &saveFileInfo);
			//if (ret != ORL_RET_SUCCESS)
			//{
			//	AddErrorToQueue("Error saving host buffer to file");
			//	return;
			//}

			//ostringstream fileName2;
			//fileName2 << recordingDirectory << timebuffer.time << "_" << timebuffer.millitm << ".jpg";
			//strcpy(filenameBuffer, fileName2.str().c_str());
			//saveFileInfo.bmtype = ORL_BM_JPEG;
			//saveFileInfo.quality = 75;
			//saveFileInfo.filename = filenameBuffer;
			//saveFileInfo.filenamelength = fileName2.str().length();
			//ret = FOrlFunc(s_boardHandle, ORLF_SAVE_HOSTBUFF, &saveFileInfo);
		}

		if (i == (numFrames - 1))
		{
			//
			// This is the latest frame in the buffer. Save it.
			//
			m_frameMutex.Lock();
			if ((m_bufferIndexLatest >= 0) && (m_bufferIndexLatest != m_bufferIndexClientProcessing))
			{
				m_hostBuffers[m_bufferIndexLatest].isInUse = false;
			}
			m_bufferIndexLatest = frameCapture.bufferIndex;
			m_frameMutex.Unlock();
		}
		else
		{
			m_frameMutex.Lock();
			m_hostBuffers[frameCapture.bufferIndex].isInUse = false;
			m_frameMutex.Unlock();
		}
	}
}

void OrlandoVideoCapture::DisableOutput()
{
	if (m_isAnalogOutputEnabled)
	{
		m_isAnalogOutputEnabled = false;

		SOrlModule module;
		ORLINITS(module);

		//
		// Disable the output module
		//
		module.module = m_outputModule;
		module.enable = 0;
		OrlRet ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error disabling output module");
		}

		//
		// Disable the fusion module
		//
		module.module = m_fusionModule;
		module.enable = 0;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error disabling fusion module");
		}

		//
		// Disable the overlay module
		//
		module.module = m_overlayModule;
		module.enable = 0;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_ENABLE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error disabling overlay module");
		}



		//
		// Destroy the output module
		//
		module.module = m_outputModule;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_RELEASE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error destroying output module");
		}


		//
		// Destroy the fusion module
		//
		module.module = m_fusionModule;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_RELEASE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error desctroying fusion module");
		}

		//
		// Destroy the overlay module
		//
		module.module = m_overlayModule;
		ret = FOrlFunc(s_boardHandle, ORLF_MODULE_RELEASE, &module);
		if (ret != ORL_RET_SUCCESS)
		{
			AddErrorToQueue("Error destroying overlay module");
		}
	}
}

OrlOvlItemHandle OrlandoVideoCapture::AddTextOverlay(const string &text,
													 const ArvDWORD x,
													 const ArvDWORD y,
													 const OrlFontType font,
													 const OrlOvlColor fontColor)
{
	SOrlModTxtOverlaySett sett;
	SOrlOvlStaticText txt_item;
	ORLINITS(sett);
	ORLINITS(txt_item);
	sett.module_ovl = m_overlayModule;
	sett.outp_width = m_inputWidth;
	sett.outp_height = m_inputHeight;
	FOrlFunc(s_boardHandle, ORLF_INIT_VMODULE_TEXT_OVL, &sett);
	txt_item.module_ovl = sett.module_ovl;
	txt_item.handle = ORL_INVALID_OVL_ITEM_HANDLE;
	strcpy_s(txt_item.text_str, text.c_str());
	txt_item.txt_format.in_overlay_video = 1;
	txt_item.txt_format.posx = x;
	txt_item.txt_format.posty = y;
	txt_item.txt_format.align = ORL_TXT_ALIGN_LEFT;
	txt_item.txt_format.font = font;
	txt_item.txt_format.font_color = fontColor;
	OrlRet ret = FOrlFunc(s_boardHandle, ORLF_ADDOVL_STAT_TEXT, &txt_item);
	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error adding text overlay");
	}

	return txt_item.handle;
}

void OrlandoVideoCapture::UpdateTextOverlay(const OrlOvlItemHandle handle,
											const string &text,
											const ArvDWORD x,
										    const ArvDWORD y,
										    const OrlFontType font,
										    const OrlOvlColor fontColor)
{
	SOrlOvlStaticText txt_item;
	ORLINITS(txt_item);
	txt_item.module_ovl = m_overlayModule;
	txt_item.handle = handle;
	strcpy_s(txt_item.text_str, text.c_str());
	txt_item.txt_format.in_overlay_video = 1;
	txt_item.txt_format.posx = x;
	txt_item.txt_format.posty = y;
	txt_item.txt_format.align = ORL_TXT_ALIGN_LEFT;
	txt_item.txt_format.font = font;
	txt_item.txt_format.font_color = fontColor;
	OrlRet ret = FOrlFunc(s_boardHandle, ORLF_ADDOVL_STAT_TEXT, &txt_item);
	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error updating text overlay");
	}
}

void OrlandoVideoCapture::RemoveTextOverlay(const OrlOvlItemHandle handle)
{
	SOrlOvlItem overlayItem;
	ORLINITS(overlayItem);
	overlayItem.module_ovl = m_overlayModule;
	overlayItem.handle = handle;
	OrlRet ret = FOrlFunc(s_boardHandle, ORLF_REMOVEOVL_ITEM, &overlayItem);
	if (ret != ORL_RET_SUCCESS)
	{
		AddErrorToQueue("Error removing text overlay");
	}
}

void OrlandoVideoCaptureThread::Run()
{
	SOrlModWaitInterr interruptData;
	ORLINITS(interruptData);

	while (true)
	{
		//
		// Block thread until board interrupts occur
		//
		OrlFunc(OrlandoVideoCapture::s_boardHandle, ORLF_WAIT_INTERR_MODULE, &interruptData);

		//
		// Loop through all modules to check for interrupts. Pass any interrupts
		// on to the channel-specific capture objects
		//
		for (int moduleNum = 0; moduleNum < NUM_ORL_MODULE; ++moduleNum)
		{
			int numFramesInHostBuffer = interruptData.num_interr[moduleNum][ORL_MODINT_FRM_OUT_BIT];
			if (numFramesInHostBuffer > 0)
			{			
				if (s_moduleToVideoCaptureObjMap[moduleNum - ORL_MODULE_BRD2HOST0] != NULL)
				{
					s_moduleToVideoCaptureObjMap[moduleNum - ORL_MODULE_BRD2HOST0]->FramesArrivedInHostBuffer(numFramesInHostBuffer);
				}
			}

			int numFramesInBoardBuffer = interruptData.num_interr[moduleNum][ORL_MODINT_FRM_RDY_FOR_OUT_BIT];
			if (numFramesInBoardBuffer > 0)
			{
				if (s_moduleToVideoCaptureObjMap[moduleNum - ORL_MODULE_BRD2HOST0] != NULL)
				{
					s_moduleToVideoCaptureObjMap[moduleNum - ORL_MODULE_BRD2HOST0]->FramesArrivedInBoardBuffer(numFramesInBoardBuffer);
				}
			}
		}
	}
}

HostToBoardThread::HostToBoardThread(const string &inputDirectory, const int hostToBoardBufferNum, OrlModule hostToBoardModule)
:m_inputDirectory(inputDirectory),
 m_hostToBoardBufferNum(hostToBoardBufferNum),
 m_hostToBoardModule(hostToBoardModule)
{
	if (m_inputDirectory[m_inputDirectory.length() - 1] != '\\')
	{
		m_inputDirectory.append("\\");
	}
}

void HostToBoardThread::Run()
{
	HANDLE hFindFiles;
	WIN32_FIND_DATA oFindFileData;
	BOOL bMoreFiles;
	string searchString = m_inputDirectory + "*.bmp";
	hFindFiles = ::FindFirstFile(searchString.c_str(), &oFindFileData);

	if (hFindFiles == INVALID_HANDLE_VALUE)
	{
		printf("Invalid host to board input directory specified\n");
		return;
	}


	//
	// Create an array containing all the file names
	//
	int nNumInputFiles = 0;
	int nInputFileNamesArrayLength = 1024;
	char **pInputFileNames = (char**)malloc(nInputFileNamesArrayLength * sizeof(char*));
	do
	{
		//
		// Resize array if necessary
		//
		if (nNumInputFiles == nInputFileNamesArrayLength)
		{
			nInputFileNamesArrayLength *= 2;
			realloc(pInputFileNames, nInputFileNamesArrayLength * sizeof(char*));
		}

		//
		// Add current file name to the array
		//
		pInputFileNames[nNumInputFiles] = (char*)malloc(strlen(oFindFileData.cFileName) + 1);
		strcpy(pInputFileNames[nNumInputFiles], oFindFileData.cFileName);
		++nNumInputFiles;

		bMoreFiles = FindNextFile(hFindFiles, &oFindFileData);
	}
	while(bMoreFiles);


	qsort(pInputFileNames, nNumInputFiles, sizeof(char*), compare_string);

	for (int i = 0; i < nNumInputFiles; ++i)
	{
		SOrlLoadFile	sfile;
		SOrlFrmTransf	stransf;

		ORLINITS( sfile );
		sfile.filename  = pInputFileNames[i];
		sfile.filenamelength = (ArvDWORD)strlen(pInputFileNames[i]);
		sfile.hostbuffernr = m_hostToBoardBufferNum;
		sfile.bmtype = ORL_BM_BMP_UD;

		OrlRet ret = FOrlFunc(OrlandoVideoCapture::s_boardHandle, ORLF_LOAD_HOSTBUFF, &sfile );

		if (ret == ORL_RET_SUCCESS)
		{
			// transfer image to board
			ORLINITS( stransf );
			stransf.buffernr = m_hostToBoardBufferNum;
			stransf.module = m_hostToBoardModule;
			ret = FOrlFunc(OrlandoVideoCapture::s_boardHandle, ORLF_FRM_TRANSF, &stransf );

			if (ret != ORL_RET_SUCCESS)
			{
				printf("Error transferring host to board frame from to board\n");
			}
		}
		else
		{
			printf("Error loading host to board frame from file\n");
		}
	}
}