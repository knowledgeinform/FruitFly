#include "EthernetVideoServer.h"
#include "PAutoLockC.h"
#include <iostream>
#include <stdio.h>
#include "jpeglib.h"
using std::iostream;
using std::cerr;
using std::endl;

static const int IDLE_SLEEP_TIME_MS = 10;

EthernetVideoServer::EthernetVideoServer(const int nListenPort, const int nFrameWidth, const int nFrameHeight)
:m_nFrameWidth(nFrameWidth),
 m_nFrameHeight(nFrameHeight),
 m_nListenPort(nListenPort),
 m_nLatestFrameBufferSize(0),
 m_nUnencodedBufferSize(0),
 m_pLatestFrame(NULL),
 m_pUnencodedBuffer(NULL),
 m_bNewFrameArrived(false)
{
	// Start server thread
	Start();
}

EthernetVideoServer::~EthernetVideoServer()
{
	delete[] m_pLatestFrame;
	delete[] m_pUnencodedBuffer;
}

void EthernetVideoServer::Run()
{
    WSADATA wsaData;
    sockaddr_in localAddress;

	WSAStartup(MAKEWORD(2,2), &wsaData);

    localAddress.sin_family = AF_INET;
    localAddress.sin_addr.s_addr = INADDR_ANY; 
    localAddress.sin_port = htons((u_short)m_nListenPort); 

	SOCKET hSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (bind(hSocket, (sockaddr*)&localAddress, sizeof(localAddress)) < 0)
	{
		fprintf(stderr,"Ethernet video server socket bind failed\n");
		return;
	}

	InterfaceWithClient(hSocket);

    
}



void EthernetVideoServer::InterfaceWithClient(const SOCKET hClientSocket)
{
	bool bConnected = true;
	bool bNewFrameArrived;
	struct jpeg_compress_struct cinfo;
	JSAMPROW row_pointer[1];
	const unsigned long ulCompressionBufferSize = 76800;
	unsigned char* pCompressionBuffer = new unsigned char[ulCompressionBufferSize];
	const int JPEG_QUALITY = 95;
	const int FRAME_WIDTH = 320;
	const int FRAME_HEIGHT = 240;
	sockaddr_in remoteAddress;
	remoteAddress.sin_family = AF_INET;
	unsigned char pIncomingNetworkBuffer[6];
	struct jpeg_error_mgr jerr;
	int row_stride;

	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_compress(&cinfo);
	cinfo.image_width = FRAME_WIDTH;
	cinfo.image_height = FRAME_HEIGHT;
	cinfo.input_components = 1;
	cinfo.in_color_space = JCS_GRAYSCALE;
	jpeg_set_defaults(&cinfo);
	jpeg_set_quality(&cinfo, JPEG_QUALITY, TRUE);

	cinfo.image_width = cinfo.image_width;

	unsigned long ulBufferSize;
	unsigned long ulCompressedSize;
	bool bGotFirstFrame = false;

	while (true)
	{	
		//
		// Wait for frame request from client. Request contains
		// client IP address and listen port
		//
		int numBytesReceived = recv(hClientSocket, (char*)pIncomingNetworkBuffer, sizeof(pIncomingNetworkBuffer), 0);
		remoteAddress.sin_addr.s_addr = *(unsigned long*)pIncomingNetworkBuffer; 
		remoteAddress.sin_port = *(unsigned short*)&pIncomingNetworkBuffer[sizeof(remoteAddress.sin_addr.s_addr)];
		

		//
		// Send new frame to client
		//
		m_Mutex.Lock();
		bNewFrameArrived = m_bNewFrameArrived;
		m_Mutex.Unlock();

		if (bNewFrameArrived)
		{
			bGotFirstFrame = true;

			m_Mutex.Lock();
			if (m_nLatestFrameBufferSize > m_nUnencodedBufferSize)
			{
				m_pUnencodedBuffer = new unsigned char[m_nLatestFrameBufferSize];
				m_nUnencodedBufferSize = m_nLatestFrameBufferSize;
			}
			memcpy(m_pUnencodedBuffer, m_pLatestFrame, m_nLatestFrameBufferSize);
			m_bNewFrameArrived = false;
			m_Mutex.Unlock();

			//
			// Compress image as JPEG in memory
			//
			ulBufferSize = ulCompressionBufferSize;
			jpeg_mem_dest(&cinfo, &pCompressionBuffer, &ulBufferSize);
			jpeg_start_compress(&cinfo, TRUE);
			row_stride = FRAME_WIDTH;
			while (cinfo.next_scanline < cinfo.image_height) 
			{
				row_pointer[0] = &m_pUnencodedBuffer[cinfo.next_scanline * row_stride];
				jpeg_write_scanlines(&cinfo, row_pointer, 1);
			}
			ulCompressedSize = ulCompressionBufferSize - cinfo.dest->free_in_buffer;
			jpeg_finish_compress(&cinfo);			
		}

		if (bGotFirstFrame)
		{
			int nNumBytesSent = sendto(hClientSocket, (char*)pCompressionBuffer, ulCompressedSize, 0, (sockaddr*)&remoteAddress, sizeof(remoteAddress));
			if (nNumBytesSent < 0)
			{
				fprintf(stderr, "Ethernet video server error sending frame");
			}
		}
	}
}

void EthernetVideoServer::SetLatestFrame(const unsigned char* const pLatestFrame, const int nNumFrameBytes)
{
	PAutoLockC oLock(m_Mutex);

	if (nNumFrameBytes > m_nLatestFrameBufferSize)
	{
		m_pLatestFrame = new unsigned char[nNumFrameBytes];
		m_nLatestFrameBufferSize = nNumFrameBytes;
	}
	
	memcpy(m_pLatestFrame, pLatestFrame, nNumFrameBytes);
	m_bNewFrameArrived = true;
}

