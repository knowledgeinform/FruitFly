#include "ReceiverThread.h"
#include "DisplayWindow.h"
#include <winsock.h>
#include <stdio.h>
#include "jpeglib.h"
#include "setjmp.h"

extern char g_szEthernetVideoServerHostname[256];
extern int gnEthernetVideoServerPort;

static unsigned short RECIEVE_PORT = 3998;

CRITICAL_SECTION g_RequestFrameCriticalSection;

static bool s_bRequestFrame = false;

DWORD WINAPI RequestorThreadProc(LPVOID lpParam)
{
	hostent *pServerHostEntry = gethostbyname(g_szEthernetVideoServerHostname);
	sockaddr_in serverAddress;
	serverAddress.sin_addr.s_addr = *((unsigned long*)pServerHostEntry->h_addr);
	serverAddress.sin_family = AF_INET;
	serverAddress.sin_port = htons(gnEthernetVideoServerPort);

	SOCKET hSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

	char localHostname[80];
	gethostname(localHostname, sizeof(localHostname));
	hostent *pLocalHostEntry = gethostbyname(localHostname);
	unsigned long ulLocalIPAddress = *((unsigned long*)pLocalHostEntry->h_addr_list[0]);
	unsigned char pRequestBuffer[sizeof(serverAddress.sin_addr.s_addr) + sizeof(serverAddress.sin_port)];
	memcpy(pRequestBuffer, &ulLocalIPAddress, sizeof(ulLocalIPAddress));
	const unsigned short receivePort = htons(RECIEVE_PORT);
	memcpy(&pRequestBuffer[sizeof(serverAddress.sin_addr.s_addr)], &receivePort, sizeof(receivePort));

	bool bFrameRequested = false;

	do
	{
		EnterCriticalSection(&g_RequestFrameCriticalSection);
		bFrameRequested = s_bRequestFrame;
		LeaveCriticalSection(&g_RequestFrameCriticalSection);
		
		if (!bFrameRequested)
		{
			Sleep(250);
		}
	}
	while (!bFrameRequested);

	int nTimeOfLastRequest_ms;
	while (true)
	{
		EnterCriticalSection(&g_RequestFrameCriticalSection);
		bFrameRequested = s_bRequestFrame;
		s_bRequestFrame = false;  //Reset flag
		LeaveCriticalSection(&g_RequestFrameCriticalSection);

		if (bFrameRequested || (GetTickCount() - nTimeOfLastRequest_ms > 250))
		{
			sendto(hSocket, (char*)pRequestBuffer, sizeof(pRequestBuffer), 0, (sockaddr*)&serverAddress, sizeof(serverAddress));
			nTimeOfLastRequest_ms = GetTickCount();
		}

		Sleep(50);
	}
}


struct my_error_mgr {
  struct jpeg_error_mgr pub;	/* "public" fields */

  jmp_buf setjmp_buffer;	/* for return to caller */
};

typedef struct my_error_mgr * my_error_ptr;

/*
 * Here's the routine that will replace the standard error_exit method:
 */

METHODDEF(void)
my_error_exit (j_common_ptr cinfo)
{
  /* cinfo->err really points to a my_error_mgr struct, so coerce pointer */
  my_error_ptr myerr = (my_error_ptr) cinfo->err;

  /* Always display the message. */
  /* We could postpone this until after returning, if we chose. */
  (*cinfo->err->output_message) (cinfo);

  /* Return control to the setjmp point */
  longjmp(myerr->setjmp_buffer, 1);
}

DWORD WINAPI RecieverThreadProc(LPVOID lpParam)
{

	int nNumBytesReceived;
	const int nInputBufferSize = 76800;
	unsigned char* pInputBuffer = new unsigned char[nInputBufferSize];
	const int FRAME_WIDTH = 320;
	const int FRAME_HEIGHT = 240;
	unsigned char* pOutputBuffer = new unsigned char[FRAME_WIDTH * FRAME_HEIGHT];

	struct my_error_mgr jerr;
	struct jpeg_decompress_struct cinfo;
	JSAMPARRAY sampling_buffer = (JSAMPARRAY)new unsigned char[FRAME_WIDTH];
	int row_stride;

	// Initialize the JPEG decompression object.
	cinfo.err = jpeg_std_error(&jerr.pub);
	jerr.pub.error_exit = my_error_exit;
	jpeg_create_decompress(&cinfo);



	sockaddr_in localAddress;
    localAddress.sin_family = AF_INET;
    localAddress.sin_addr.s_addr = INADDR_ANY; 
    localAddress.sin_port = htons(RECIEVE_PORT); 

	SOCKET hSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (bind(hSocket, (sockaddr*)&localAddress, sizeof(localAddress)) < 0)
	{
		fprintf(stderr,"Ethernet video server socket bind failed\n");
		return 1;
	}

	while (true)
	{	
		EnterCriticalSection(&g_RequestFrameCriticalSection);
		s_bRequestFrame = true;
		LeaveCriticalSection(&g_RequestFrameCriticalSection);		

		nNumBytesReceived = recv(hSocket, (char*)pInputBuffer, nInputBufferSize, 0);

		// Specify data source to read JPEG data from
		jpeg_mem_src(&cinfo, pInputBuffer, nNumBytesReceived);
		
		jpeg_read_header(&cinfo, TRUE);
		jpeg_start_decompress(&cinfo);
		row_stride = cinfo.output_width * cinfo.output_components;
		sampling_buffer = (*cinfo.mem->alloc_sarray)((j_common_ptr) &cinfo, JPOOL_IMAGE, row_stride, 1);

		while (cinfo.output_scanline < cinfo.output_height)
		{
			jpeg_read_scanlines(&cinfo, sampling_buffer, 1);
			memcpy(&pOutputBuffer[cinfo.output_scanline * row_stride], &sampling_buffer[0][0], row_stride);
		}

		DisplayWindow_SetLatestFrame((unsigned char*)pOutputBuffer, FRAME_WIDTH, FRAME_HEIGHT);

		jpeg_finish_decompress(&cinfo);
	}

	return 0;
}