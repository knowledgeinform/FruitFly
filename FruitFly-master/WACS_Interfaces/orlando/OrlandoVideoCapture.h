#include "PThreadC.h"
#include "PMutexC.h"
#include "orlando.h"
#include <queue>
#include <string>
#include <map>
using std::queue;
using std::string;
using std::map;

class OrlandoVideoCaptureThread : public PThreadC
{
	protected:
		virtual void Run();
};

class HostToBoardThread : public PThreadC
{
	public:
		HostToBoardThread(const string &inputDirectory, const int hostToBoardBufferNum, OrlModule hostToBoardModule);

	protected:
		virtual void Run();

	private:
		string m_inputDirectory;
		const int m_hostToBoardBufferNum;
		OrlModule m_hostToBoardModule;
};

//
// Interface to the Arvoo Orlando-AN Video Capture Board
//
// InitializeBoard() must be called before any OrlandoVideoCapture objects are instantiated.
// Subsequently, an OrlandoVideoCapture object should be created for each concurrent video input.
//
class OrlandoVideoCapture
{
	public:
		typedef void (*FrameArrivedCallback)(const void* const pBuffer, const string &fileName, const __time64_t &acquisitionTimeSec, const unsigned short acquisitionTimeMillisec, const int channelNum);

		static void InitializeBoard(const bool enableEthernetVideoServer = false);
		static void TerminateBoard();

		OrlandoVideoCapture(OrlModule inputModule,
			                OrlPort inputPort,
							OrlVideoTime videoTiming = ORL_VTIME_NTSC,
							ArvDWORD inputWidth = ORL_NTSC_WIDTH,
							ArvDWORD inputHeight = ORL_NTSC_FIELD_HEIGHT,
							ArvDWORD captureWidth = 320,
							ArvDWORD captureHeight = 240,
							OrlPixelFormat hostPixelFormat = ORL_PIXFRMT_Y,
							OrlFieldArrange hostLineArrangement = ORL_FLD_INTERLACED,//ORL_FLD_SINGLE, 
							OrlVideoBCSMode bcsMode = ORL_VBCS_DEFAULTS,
							ArvDWORD brightness = 128,
							ArvSDWORD contrast = 68,
							ArvSDWORD saturation = 64,
							ArvSDWORD hue = 0,
							OrlVideoGainMode gainMode = ORL_VGAIN_AUTO,
							ArvDWORD gain_y_cvbs = 250,
							ArvDWORD gain_c = 250);

		OrlandoVideoCapture(OrlModule inputModule,
			                OrlPort inputPort,
							OrlModule outputModule,
							OrlPort outputPort,
							OrlVideoTime videoTiming = ORL_VTIME_NTSC,
							ArvDWORD inputWidth = ORL_NTSC_WIDTH,
							ArvDWORD inputHeight = ORL_NTSC_FIELD_HEIGHT,
							ArvDWORD captureWidth = 320,
							ArvDWORD captureHeight = 240,
							OrlPixelFormat hostPixelFormat = ORL_PIXFRMT_Y,
							OrlFieldArrange hostLineArrangement = ORL_FLD_INTERLACED,//ORL_FLD_SINGLE, 
							OrlVideoBCSMode bcsMode = ORL_VBCS_DEFAULTS,
							ArvDWORD brightness = 128,
							ArvSDWORD contrast = 68,
							ArvSDWORD saturation = 64,
							ArvSDWORD hue = 0,
							OrlVideoGainMode gainMode = ORL_VGAIN_AUTO,
							ArvDWORD gain_y_cvbs = 250,
							ArvDWORD gain_c = 250);


		virtual ~OrlandoVideoCapture();
		
		bool HasErrors() const;
		string GetError();

		void* GetLatestFrame();

		void InitializeRecording(const string &recordingDirectory, FrameArrivedCallback frameArrivedCallback);
		void StartRecording();
		void StopRecording();

		void CreateHostToBoardOutput(OrlModule hostToBoardModule, OrlModule outputModule, OrlPort outputPort, const int width, const int height, const string &inputDirectory);

		unsigned int GetFrameBufferSize() const;

		OrlOvlItemHandle AddTextOverlay(const string &text,
			                            const ArvDWORD x,
										const ArvDWORD y,
										const OrlFontType font = ORL_STDFONT_8X8,
										const OrlOvlColor fontColor = ORL_OVL_COLOR_RED);
		void UpdateTextOverlay(const OrlOvlItemHandle handle,
			                   const string &text,
			                   const ArvDWORD x,
							   const ArvDWORD y,
							   const OrlFontType font = ORL_STDFONT_8X8,
							   const OrlOvlColor fontColor = ORL_OVL_COLOR_RED);
		void RemoveTextOverlay(const OrlOvlItemHandle handle);


		friend class OrlandoVideoCaptureThread;	
		friend class HostToBoardThread;

	protected:				
		static OrlRet UploadFirmware(const ORL_HANDLE hbrd);
		void FramesArrivedInBoardBuffer(const int numFrames);
		void FramesArrivedInHostBuffer(const int numFrames);
		static bool s_boardInitialized;
		static ORL_HANDLE s_boardHandle;
		static OrlandoVideoCaptureThread s_videoCaptureThread;
		static string s_recordingDirectory;
		static const int NUM_HOST_BUFFERS = 3;
		static PMutexC s_videoCaptureObjectListMutex;			

	private:	
		struct HostBuffer
		{
			bool isInUse;
			ArvDWORD bufferNum;
			ArvPVOID pBuffer;
		};

		struct FrameCapture
		{
			int bufferIndex;
			__time64_t timestampSeconds;
			unsigned short timestampMilliseconds;
		};

		void InitInput(OrlModule inputModule,
			           OrlPort inputPort,
					   OrlVideoTime videoTiming = ORL_VTIME_NTSC,
					   ArvDWORD inputWidth = ORL_NTSC_WIDTH,
					   ArvDWORD inputHeight = ORL_NTSC_FIELD_HEIGHT,
					   ArvDWORD captureWidth = 320,
					   ArvDWORD captureHeight = 240,
					   OrlPixelFormat hostPixelFormat = ORL_PIXFRMT_Y,
					   OrlFieldArrange hostLineArrangement = ORL_FLD_INTERLACED,//ORL_FLD_SINGLE, 
					   OrlVideoBCSMode bcsMode = ORL_VBCS_DEFAULTS,
					   ArvDWORD brightness = 128,
					   ArvSDWORD contrast = 68,
					   ArvSDWORD saturation = 64,
					   ArvSDWORD hue = 0,
					   OrlVideoGainMode gainMode = ORL_VGAIN_AUTO,
					   ArvDWORD gain_y_cvbs = 250,
					   ArvDWORD gain_c = 250);
		void InitOutput(OrlModule outputModule,
			            OrlPort outputPort);
		void EnableInput();
		void EnableOutput();
		void DisableOutput();

		int FindFreeBuffer();

		void AddErrorToQueue(const string &error);	

		FrameArrivedCallback m_frameArriveCallback;
		HostBuffer m_hostBuffers[NUM_HOST_BUFFERS];
		OrlModule m_inputModule;
		OrlModule m_boardToHostModule;
		OrlModule m_outputModule;
		OrlModule m_overlayModule;
		OrlModule m_fusionModule;
		OrlVideoTime m_inputVideoTiming;
		ArvDWORD m_inputWidth;
		ArvDWORD m_inputHeight;		
		queue<string> m_errorQueue;
		queue<FrameCapture> m_bufferTransferQueue;
		bool m_isAcquiringVideo;
		bool m_isReadyForNextFrame;	
		PMutexC m_frameCountMutex;
		PMutexC m_recordingMutex;		
		PMutexC m_frameMutex;
		mutable PMutexC m_errorMutex;		
		int m_bufferIndexLatest;
		int m_bufferIndexClientProcessing;
		bool m_isRecordingEnabled;
		string m_recordingDirectory;
		bool m_isAnalogOutputEnabled;
		unsigned int m_bufferSize;	
		HostToBoardThread *m_pHostToBoardThread;
};
