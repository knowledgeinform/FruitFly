#include "stdafx.h"
#include <list>
#include "pthread.h"
#include "VideoCaptureHandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>



using namespace std;

static pthread_mutex_t imageData_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_t conversionThread;
bool killThread = false;
bool threadDone = false;
char serverSocketBuffer [100000000];

WSADATA w;							/* Used to open windows connection */
int bytes_received=-1;					/* Bytes received from client */
int client_length;					/* Length of client struct */
unsigned short port_number = 5000;			/* Port number to use */
SOCKET sd;							/* Socket descriptor of server */
SOCKET clientSock;
struct sockaddr_in server;			/* Information about the server */
struct sockaddr_in client;			/* Information about the client */
struct hostent *hp;					/* Information about this computer */
char host_name[256];				/* Name of the server */


class ImageData
{
	public:
		unsigned char* imageBytes;
		int imageWidth;
		int imageHeight;

		~ImageData(){delete [] imageBytes;}
};
list <ImageData*> imageDataQueue;


bool initializeTCPSocket()
{
	if (WSAStartup(0x0101, &w) != 0)
	{
		fprintf(stderr, "Could not open Windows connection.\n");
		return false;
	}

	sd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sd == INVALID_SOCKET)
	{
		fprintf(stderr, "Could not create socket.\n");
		WSACleanup();
		return false;
	}

	 // Set buffer size
	 int sendbuff = 1048576;
	 int res = setsockopt(sd, SOL_SOCKET, SO_SNDBUF, (char*)&sendbuff, sizeof(sendbuff));

	memset(&server, 0, sizeof(server));
    server.sin_family      = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port        = htons(port_number);

	if ( bind(sd, (struct sockaddr *) &server, sizeof(server)) < 0 ) 
	{
		fprintf(stderr, "ECHOSERV: Error calling bind()\n");
		return false;
    }

	if ( listen(sd, 1024) < 0 ) {
		fprintf(stderr, "ECHOSERV: Error calling listen()\n");
		return false;
    }


	fd_set readfds;
	FD_ZERO(&readfds);
	// Set server socket to set
	FD_SET(sd, &readfds);
	// Timeout parameter
	timeval tv = { 0 };
	tv.tv_sec = 20;

	int ret = select(0, &readfds, NULL, NULL, &tv);
	if (ret <= 0)
	{
		fprintf(stderr, "ECHOSERV: No sockets available from call to select()\n");
		return false;
	}

	if ( (clientSock = accept(sd, NULL, NULL) ) < 0 ) {
	    fprintf(stderr, "ECHOSERV: Error calling accept()\n");
	    return false;
	}

	return true;
}

void* VideoCapture_ConvertToVideoProc(void *arg)
{
	if (!initializeTCPSocket())
	{
		killThread = true;
		MessageBox (NULL, "Unable to start video capture threads!", "Error!", MB_OK);
	}
	
	while (bytes_received == -1 && !killThread)
	{
		bytes_received = recvfrom(clientSock, serverSocketBuffer, 1, 0, (struct sockaddr *)&client, &client_length);
	}

	while (!killThread)
	{
		if (imageDataQueue.size() > 0)
		{
			long long t1 = clock();
			pthread_mutex_lock(&imageData_mutex);
			ImageData* imgData = imageDataQueue.front();
			pthread_mutex_unlock(&imageData_mutex);
			
			long long t2 = clock();
			int bufferSize = 0;
			serverSocketBuffer[0] = 0;
			bufferSize ++;
			memcpy (&(serverSocketBuffer[bufferSize]), (char*)&imgData->imageWidth, 4);
			bufferSize +=4;
			memcpy (&(serverSocketBuffer[bufferSize]), (char*)&imgData->imageHeight, 4);
			bufferSize +=4;
			memcpy (&(serverSocketBuffer[bufferSize]), imgData->imageBytes, (imgData->imageWidth)*(imgData->imageHeight)*3);
			bufferSize += (imgData->imageWidth)*(imgData->imageHeight)*3;
			
			long long t3 = clock();
			int countSent = 0;
			while (countSent < bufferSize)
			{
				countSent += sendto(clientSock, &serverSocketBuffer[countSent], bufferSize-countSent, 0, (struct sockaddr *)&client, client_length);
			}
			long long t4 = clock();

			
			delete imgData;
			pthread_mutex_lock(&imageData_mutex);
			imageDataQueue.pop_front();
			pthread_mutex_unlock(&imageData_mutex);
			long long t5 = clock();
			t5 = t5;
		}
		else
		{
			Sleep (10);
		}
	}

	//send kill message
	int bufferSize = 1;
	serverSocketBuffer[0] = 1;
	//socket.send (serverSocketBuffer, bufferSize);
	sendto(clientSock, serverSocketBuffer, bufferSize, 0, (struct sockaddr *)&client, client_length);

	//read kill confirm
	bytes_received = -1;
	int maxCount = 20;
	int count = 0;
	while (bytes_received == -1)
	{
		Sleep (100);
		if (count++ > maxCount)
		{
			MessageBox (NULL, "Unable to stop video capture threads!", "Error!", MB_OK);
			break;
		}
		bytes_received = recvfrom(clientSock, serverSocketBuffer, 5, 0, (struct sockaddr *)&client, &client_length);
	}
	
	closesocket(sd);
	WSACleanup();
	

	//if anything is left in queue, delete it now
	pthread_mutex_lock(&imageData_mutex);
	while (imageDataQueue.size() > 0)
	{
		//delete imageDataQueue.front();
		imageDataQueue.pop_front();
	}
	pthread_mutex_unlock(&imageData_mutex);
	
	threadDone = true;
	//MessageBox(NULL, "Done!", "WACS", MB_OK);
	return 0;
}

void VideoCapture_StartThread ()
{
	//start java processing for video
	system ("START /B java -jar ./VideoRecording/WACS_ExtDisplayVideo.jar -Xmx1000m");

	//start image buffering thread
	int retVal = pthread_create(&conversionThread, NULL, VideoCapture_ConvertToVideoProc, NULL);
}

void VideoCapture_StopThread ()
{
	killThread = true;
	while (!threadDone)
	{
		Sleep (100);
	}
}

//assuming rgb format, one byte each
void VideoCapture_CapturePixels (int width, int height, unsigned char* imageData)
{
	if (killThread)
		return;
	if (imageDataQueue.size() > 50)
		return;

	unsigned char* imageDataCopy = new unsigned char [3*(width)*(height)];
	memcpy (imageDataCopy, imageData, 3*(width)*(height));
	ImageData* newData = new ImageData();;
	newData->imageWidth = width;
	newData->imageHeight = height;
	newData->imageBytes = imageDataCopy;
	pthread_mutex_lock(&imageData_mutex);
	imageDataQueue.push_back (newData);
	pthread_mutex_unlock(&imageData_mutex);
}


