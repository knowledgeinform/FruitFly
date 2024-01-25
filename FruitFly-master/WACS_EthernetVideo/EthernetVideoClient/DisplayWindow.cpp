#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include <windows.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <tchar.h>
#include <math.h>
#include <GL/gl.h> 
#include <GL/glu.h> 
#include <winsock.h>
#include "ReceiverThread.h"
#include "IniReader.h"
#include "IniWriter.h"

char g_szEthernetVideoServerHostname[256];
int gnEthernetVideoServerPort;

static TCHAR szWindowClass[] = _T("WACSEthernetVideoPlayer");
static TCHAR szTitle[] = _T("WACS Ethernet Video Player");

static const int VIDEO_WIDTH_PIXELS = 320;
static const int VIDEO_HEIGHT_PIXELS = 240;
static const int VIDEO_NUM_BYTES_PER_PIXEL = 1;
static const int VIDEO_BUFFER_SIZE = VIDEO_WIDTH_PIXELS * VIDEO_HEIGHT_PIXELS * VIDEO_NUM_BYTES_PER_PIXEL;

static const int GL_FONT_START_INDEX = 1000;

static const DWORD VIDEO_TIMEOUT_PERIOD_MS = 2000;

static HWND s_hWindow;

static DWORD s_dwFrameTime = 0;
static DWORD s_dwPrevFrameTime = 0;
static BOOL s_bNewFrameArrived = FALSE;
static unsigned char* s_pLatestFrameBuffer = new unsigned char[VIDEO_BUFFER_SIZE];
static unsigned char* s_pDisplayBuffer = new unsigned char[VIDEO_BUFFER_SIZE];
static float s_fFramesPerSecond = 0;
static TCHAR szFrameRate[128];

static HWND  s_hWnd; 
static HDC   s_hDC; 
static HGLRC s_hRC; 

static float s_fPixelWidth = 1;
static float s_fPixelHeight = 1;

static int s_nGLHeight;
static int s_nGLWidth;

extern CRITICAL_SECTION g_RequestFrameCriticalSection;



static CRITICAL_SECTION s_LatestFrameCriticalSection;
static HANDLE s_NewFrameEvent;


BOOL bSetupPixelFormat(HDC hdc) 
{ 
    PIXELFORMATDESCRIPTOR pfd, *ppfd; 
    int pixelformat; 
 
    ppfd = &pfd; 
 
    ppfd->nSize = sizeof(PIXELFORMATDESCRIPTOR); 
    ppfd->nVersion = 1; 
    ppfd->dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL |  
                        PFD_DOUBLEBUFFER; 
    ppfd->dwLayerMask = PFD_MAIN_PLANE; 
    ppfd->iPixelType = PFD_TYPE_COLORINDEX; 
    ppfd->cColorBits = 8; 
    ppfd->cDepthBits = 16; 
    ppfd->cAccumBits = 0; 
    ppfd->cStencilBits = 0; 
 
    pixelformat = ChoosePixelFormat(hdc, ppfd); 
 
    if ( (pixelformat = ChoosePixelFormat(hdc, ppfd)) == 0 ) 
    { 
        MessageBox(NULL, "ChoosePixelFormat failed", "Error", MB_OK); 
        return FALSE; 
    } 
 
    if (SetPixelFormat(hdc, pixelformat, ppfd) == FALSE) 
    { 
        MessageBox(NULL, "SetPixelFormat failed", "Error", MB_OK); 
        return FALSE; 
    } 
 
    return TRUE; 
} 



 
GLvoid resize(GLsizei width, GLsizei height) 
{ 
	glViewport(0, 0, width, height);

    glMatrixMode(GL_PROJECTION); 
    glLoadIdentity();
	gluOrtho2D(0, width, height, 0);
    glMatrixMode(GL_MODELVIEW); 

	s_fPixelWidth = width / ((float)VIDEO_WIDTH_PIXELS);
	s_fPixelHeight = height / ((float)VIDEO_HEIGHT_PIXELS);

	s_nGLHeight = height;
	s_nGLWidth = width;
}     
  
GLvoid initializeGL(GLsizei width, GLsizei height) 
{ 
    glDisable(GL_DEPTH_TEST); 
	glDisable(GL_LIGHTING);

	SelectObject(s_hDC, GetStockObject(SYSTEM_FONT)); 
	wglUseFontBitmaps(s_hDC, 0, 255, GL_FONT_START_INDEX); 
}

void drawText(const char* const szText, const int nX, const int nY)
{
	glListBase(GL_FONT_START_INDEX);
	glRasterPos2i(nX, nY);
	glCallLists(strlen(szText), GL_UNSIGNED_BYTE, szText);
}

GLvoid drawFrame(GLvoid) 
{ 
	static int lastFrameRateUpdateTime = 0;


	glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT); 
 
	//
	// If a new frame has arrived since our last paint, copy it to the display buffer
	//
	EnterCriticalSection(&s_LatestFrameCriticalSection);
	if (s_bNewFrameArrived)
	{
		memcpy(s_pDisplayBuffer, s_pLatestFrameBuffer, VIDEO_BUFFER_SIZE);
		s_bNewFrameArrived = FALSE;
		if (s_dwFrameTime - lastFrameRateUpdateTime > 1000)
		{
			//
			// Take moving average of framerate to give it some inertia
			//
			s_fFramesPerSecond = ((1000.0f / (s_dwFrameTime - s_dwPrevFrameTime)) + s_fFramesPerSecond * 1) / 2;

			lastFrameRateUpdateTime = s_dwFrameTime;
		}
	}
	LeaveCriticalSection(&s_LatestFrameCriticalSection);



	//
	// Copy frame in memory buffer into GL buffer
	//
	float fPixelStartX;
	float fPixelStartY = VIDEO_HEIGHT_PIXELS;
	glBegin(GL_QUADS);
	const unsigned char* pCurrPixelColor = &s_pDisplayBuffer[0];
	for (int j = 0; j < VIDEO_HEIGHT_PIXELS; ++j)	
	{
		fPixelStartX = 0;			

		for (int i = 0; i < VIDEO_WIDTH_PIXELS; ++i)
		{
			unsigned char pixelLuminance = *pCurrPixelColor;
			glColor3ub(pixelLuminance, pixelLuminance, pixelLuminance);
			glVertex2f(fPixelStartX, VIDEO_HEIGHT_PIXELS - fPixelStartY);
			glVertex2f(fPixelStartX, VIDEO_HEIGHT_PIXELS - fPixelStartY - s_fPixelHeight);
			glVertex2f(fPixelStartX + s_fPixelWidth, VIDEO_HEIGHT_PIXELS - fPixelStartY - s_fPixelHeight);
			glVertex2f(fPixelStartX + s_fPixelWidth, VIDEO_HEIGHT_PIXELS - fPixelStartY);

			pCurrPixelColor += VIDEO_NUM_BYTES_PER_PIXEL;
			fPixelStartX += s_fPixelWidth;
		}

		fPixelStartY -= s_fPixelHeight;
	}
	glEnd();


	if ((GetTickCount() - s_dwFrameTime) > VIDEO_TIMEOUT_PERIOD_MS)
	{
		//
		// Video has timed out
		//
		glColor3ub(255, 0, 0);
		drawText("No Video", s_nGLWidth / 2 - 25, s_nGLHeight / 2 + 5);
	}
	else
	{
		//sprintf(szFrameRate, "%2d fps", (int)floor(s_fFramesPerSecond + 0.5)); //round fps to nearest integer and create a string from it
		glColor3ub(0, 0, 255);
		drawText(szFrameRate, s_nGLWidth - 40, s_nGLHeight - 6);
	}
		
 
	SwapBuffers(s_hDC);
} 





LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    PAINTSTRUCT ps;
	RECT rect; 
		
    switch (message)
    {
		case WM_CREATE:
			s_hDC = GetDC(hWnd); 
			if (!bSetupPixelFormat(s_hDC)) 
				PostQuitMessage (0); 
	 
			s_hRC = wglCreateContext(s_hDC); 
			wglMakeCurrent(s_hDC, s_hRC); 
			GetClientRect(hWnd, &rect); 
			initializeGL(rect.right, rect.bottom); 
			break; 

		case WM_PAINT:
			BeginPaint(hWnd, &ps); 
			EndPaint(hWnd, &ps);
			break;
		case WM_SIZE: 
			GetClientRect(hWnd, &rect); 
			resize(rect.right, rect.bottom); 
			break; 
		case WM_DESTROY:
			//PostQuitMessage(0);
			ExitProcess(0);
			break;
		default:
			return DefWindowProc(hWnd, message, wParam, lParam);
			break;
    }

    return 0;
}

int WINAPI WinMain(HINSTANCE hInstance,
                   HINSTANCE hPrevInstance,
                   LPSTR lpCmdLine,
                   int nCmdShow)
{
	InitializeCriticalSection(&s_LatestFrameCriticalSection);
	InitializeCriticalSection(&g_RequestFrameCriticalSection);
	s_NewFrameEvent = CreateEvent(NULL, FALSE, TRUE, TEXT("NewFrameEvent")); 

	TCHAR szIniPath[MAX_PATH] = "";
	GetCurrentDirectory(sizeof(szIniPath) - 1, szIniPath);
	strcat(szIniPath, "\\EthernetVideoClient.ini");

	memset(s_pDisplayBuffer, 0, VIDEO_BUFFER_SIZE);

	CIniReader oIniReader(szIniPath);
	const char* const szHostname = oIniReader.ReadString("RemoteServer", "hostname", NULL);
	const int nPort = oIniReader.ReadInteger("RemoteServer", "port", 0);

	if (szHostname == NULL || nPort == 0)
	{
		::MessageBox(NULL, "Invalid .ini configuration file", "Error", 0);
		return 1;
	}


	//
	// Set the global variables that communicate the video server host info to the receiver thread
	//
	strcpy_s(g_szEthernetVideoServerHostname, sizeof(g_szEthernetVideoServerHostname), szHostname);
	gnEthernetVideoServerPort = nPort;


	//
	// Create window attributes class
	//
    WNDCLASSEX wcex;
    wcex.cbSize = sizeof(WNDCLASSEX);
    wcex.style          = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc    = WndProc;
    wcex.cbClsExtra     = 0;
    wcex.cbWndExtra     = 0;
    wcex.hInstance      = hInstance;
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_APPLICATION));
    wcex.hCursor        = LoadCursor(NULL, IDC_ARROW);
    wcex.hbrBackground  = (HBRUSH)CreateSolidBrush(RGB(0, 0, 0));
    wcex.lpszMenuName   = NULL;
    wcex.lpszClassName  = szWindowClass;
    wcex.hIconSm        = LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_APPLICATION));


	//
	// Register window attributes class
	//
    if (!RegisterClassEx(&wcex))
    {
        MessageBox(NULL,
            _T("Call to RegisterClassEx failed!"),
			_T("Error"),
            NULL);

        return 1;
    }

	//
	// Create window using above attributes
	//
	s_hWindow = CreateWindow(
		szWindowClass,
		szTitle,
		WS_OVERLAPPEDWINDOW,
		CW_USEDEFAULT,
		CW_USEDEFAULT,
		VIDEO_WIDTH_PIXELS + 8,//add border width to video width to get total window width
		VIDEO_HEIGHT_PIXELS + 34,//add titlebar and border height to video height to get total window height
		NULL,
		NULL,
		hInstance,
		NULL
	);
	if (!s_hWindow)
	{
		MessageBox(NULL,
			_T("Call to CreateWindow failed!"),
			_T("Error"),
			NULL);

		return 1;
	}


	//
	// Make the window visible
	// 
	ShowWindow(s_hWindow, nCmdShow);
	UpdateWindow(s_hWindow);


	WSADATA wsaData;
	WSAStartup(MAKEWORD(2,2), &wsaData); 

	//
	// Spawn frame requestor thread
	//
	DWORD dwThreadId;
	CreateThread(NULL,               
				 0,                  
				 RequestorThreadProc, 
				 NULL,               
				 0,                  
				 &dwThreadId);   

	//
	// Spawn network receiver thread
	//
	CreateThread(NULL,               
				 0,                  
				 RecieverThreadProc, 
				 NULL,               
				 0,                  
				 &dwThreadId);   

	MSG        msg; 
    while (1) 
	{ 
		//
		// Process all windows messages in queue
		//
		while (PeekMessage(&msg, NULL, 0, 0, PM_NOREMOVE) == TRUE) 
        { 
            if (GetMessage(&msg, NULL, 0, 0)) 
            { 
                TranslateMessage(&msg); 
                DispatchMessage(&msg); 
            }
			else 
			{ 
                return TRUE; 
            } 
        } 

		WaitForSingleObject(s_NewFrameEvent, 250);


		//
		// Draw latest frame onto window
		//
        drawFrame(); 
    } 




	DeleteCriticalSection(&s_LatestFrameCriticalSection);
    return (int)msg.wParam;

}

void DisplayWindow_SetLatestFrame(const unsigned char* const pNewFrame, const int nFrameWidth, const int nFrameHeight)
{
	BOOL bPrevFrameAlreadyProcessed;
	EnterCriticalSection(&s_LatestFrameCriticalSection);
	bPrevFrameAlreadyProcessed = !s_bNewFrameArrived;
	LeaveCriticalSection(&s_LatestFrameCriticalSection);


	if (bPrevFrameAlreadyProcessed)
	{
		EnterCriticalSection(&s_LatestFrameCriticalSection);

		memcpy(s_pLatestFrameBuffer, pNewFrame, VIDEO_BUFFER_SIZE);
		s_bNewFrameArrived = TRUE;
		s_dwPrevFrameTime = s_dwFrameTime;
		s_dwFrameTime = GetTickCount();

		LeaveCriticalSection(&s_LatestFrameCriticalSection);

		SetEvent(s_NewFrameEvent);

		//
		// Force the display window to refresh
		//
		InvalidateRect(s_hWindow, NULL, FALSE);
		UpdateWindow(s_hWindow);
	}
}

