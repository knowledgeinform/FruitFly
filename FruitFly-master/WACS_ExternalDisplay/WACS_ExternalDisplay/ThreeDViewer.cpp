#include "stdafx.h"
//#include "vld.h"		//Uncomment for Visual Leak Detector
#include "ThreeDViewer.h"
#include "Logger.h"
#include "Config.h"
#include "Resource.h"
#include "Constants.h"
#include <vector>
#include "Viewer3D.h"
#include "Utils.h"
#include "shlobj.h"
#include "guicon.h"
#include "VideoCaptureHandler.h"

/**
\brief Listbox window to log everything to
*/
HWND logWindow;

/**
\brief Message handler for Log dialog
 */
LRESULT CALLBACK LogWndProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam);



// *********************************************
// Main Thread
// *********************************************

int APIENTRY _tWinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPTSTR    lpCmdLine,
                     int       nCmdShow)
{

	try 
	{
		#ifdef _DEBUG
			RedirectIOToConsole();
		#endif

		//Create logger window
		logWindow = CreateDialogParam(NULL, MAKEINTRESOURCE(IDD_LOGBOX), NULL, (DLGPROC) LogWndProc, NULL); 
		SendMessage (GetDlgItem (logWindow, IDC_LOGLIST), LB_SETHORIZONTALEXTENT, (WPARAM)1400, 0);

		//Initialize file to log to so we don't hang when the Logger is used
		Logger::getInstance ("Logs\\log")->setListboxWnd (GetDlgItem (logWindow, IDC_LOGLIST));

		//Initialize config file to read from so we don't hang when the Config object is used
		Config::getInstance ("config.txt");
		
		//Create GUI
		ThreeDViewer* gui = new ThreeDViewer(hInstance);

		//Start message loop
		gui->run ();

		//When message loop ends, program is done.  Delete GUI.
		delete gui;

		//Re-write config settings to file in case user changed any
		Config::getInstance()->writeOutToFile ();

		//Close Config and Logger singletons
		Config::releaseInstance();
		Logger::releaseInstance();

		//Destory logger window
		DestroyWindow (logWindow);
		return 0;

	}
	catch (...)
	{
		MessageBox (NULL, "Exception!  Check log file for details", "Exception!  Check log file for details", MB_OK);
		return -1;
	}
}







HFONT font3 = (HFONT)GetStockObject(DEFAULT_GUI_FONT);
bool ThreeDViewer::beingDestroyed = false;

ThreeDViewer::ThreeDViewer (HINSTANCE hInstance)
{
	//General 3D Viewer initialization
	windowLoaded = false;
	
	HFONT hf1;
	HDC hdc;
	long lfHeight1;
	updateRawDataNext = false;
    
	hdc = GetDC(NULL);	
	lfHeight1 = -MulDiv(32, GetDeviceCaps(hdc, LOGPIXELSY), 72);
	
	ReleaseDC(NULL, hdc);

	hf1 = CreateFont(lfHeight1, 0, 0, 0, 0, TRUE, 0, 0, 0, 0, 0, 0, 0, "Arial");
	
	if(hf1)
	{
		DeleteObject(font3);
		font3 = hf1;
	}

	hInst = hInstance;
	hWnd = NULL;
	winNeedsResize = false;
	frameRate = Config::getInstance()->getValueAsFloat ("GL.Framerate.Seconds", .1);
	frameRateRawUpdate = Config::getInstance()->getValueAsFloat ("RawDataDialog.Framerate.Seconds", 2);
	topBuffer = Config::getInstance()->getValueAsInt ("ThreeDViewer.TopBufferSize", 49);
	sideBuffer = Config::getInstance()->getValueAsInt ("ThreeDViewer.SideBufferSize", 6);
	minWidth = Config::getInstance()->getValueAsInt ("ThreeDViewer.MinimumWidth", 855);
	minHeight = Config::getInstance()->getValueAsInt ("ThreeDViewer.MinimumHeight", 658);

	myConfirmDialog = NULL;
	myRawDataDialog = NULL;
	
	//Window is ready to go
	windowLoaded = true;

	char logBuf [256];
	string logString;
	sprintf_s(logBuf, 256, "ThreeDViewer: Starting window creation.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);


	initDialog();

	//set up gl thread
	m_KillGLThread = false;
	m_GLThreadDone = false;
	
	glThreadParams = new ThreadParams ();
	glThreadParams->gui = this;
	glThreadParams->m_KillThread = &m_KillGLThread;
	glThreadParams->m_ThreadDone = &m_GLThreadDone;
	
	int stackSize = Config::getInstance()->getValueAsInt ("ThreadStackSize.GLPaintThread.Bytes", 100000);
	hGLPaintThread = (HANDLE)_beginthread( &ThreeDViewer::glPaintThread, stackSize, glThreadParams);
	if (hGLPaintThread == NULL)
	{
		char text [256];
		sprintf_s (text, 256, "Unable to start GL paint thread\0");
		Logger::getInstance()->logMessage (text);
		throw new exception (text);
	}

	
	sprintf_s(logBuf, 256, "ThreeDViewer: Activating window.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);

	setWindowPreferredSizes ();
	SetFocus (hWnd);
	activate();
	SetFocus (hWnd);
	winNeedsResize = true;
	
	sprintf_s(logBuf, 256, "ThreeDViewer: Window creation complete.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);






	//WACS Specific initialization

	sprintf_s(logBuf, 256, "ThreeDViewer: Creating belief collection.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);

	m_BeliefCollection = new BeliefCollection ();
	m_SwarmSpyHandler = new SwarmSpyHandler (m_BeliefCollection);
	
	m_DoSocketSpy = Config::getInstance()->getValueAsBool ("SwarmSpy.DoSocketSpy", true);
	m_SwarmSpyInitialized = false;
	if (m_DoSocketSpy)
	{
		//If socket connection, start spy immediately so we can log and display data that comes
		//before GL is ready.
		initSwarmSpy ();	
	}
	else 
	{
		//If replay spy, start spy later once GL is up and ready so we don't miss anything.
	}

	
	
	Viewer3D::m_BeliefCollection = m_BeliefCollection;

	if (myRawDataDialog == NULL)
	{
		myRawDataDialog = new RawDataDialog (hInst, hWnd, m_BeliefCollection);
	}

	//wacs display stuff
	Viewer3D::m_DisplayShadowOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayShadowOpt", true);
	Viewer3D::m_DisplayPicTelForShadowOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayPicTelForShadowOpt", false);
	Viewer3D::m_DisplayOrbitOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayOrbitOpt", true);
	Viewer3D::m_DisplayGcsOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayGcsOpt", true);
	Viewer3D::m_DisplayParticlesOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayParticlesOpt", true);
	Viewer3D::m_DisplayChemicalOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayChemicalOpt", true);
	Viewer3D::m_DisplayPredictedCloudOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayPredictedCloud", true);
	Viewer3D::m_DisplayExplosionOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayExplosionOpt", true);
	Viewer3D::m_DisplayMetBeliefOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayMetBeliefOpt", true);
	Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayIrExplosionAlgorithmEnabledBeliefOpt", true);
	Viewer3D::m_DrawSensorOverlays = Config::getInstance()->getValueAsBool ("Viewer3D::DisplaySensorOverlaysOpt", true);
	Viewer3D::m_DisplayPropogatedDetectionsOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayPropogatedDetectionsOpt", true);
	Viewer3D::m_DisplayLoiterApproachPathOpt = Config::getInstance()->getValueAsBool ("Viewer3D::DisplayLoiterApproachPathOpt", true);
	Viewer3D::m_PauseDetectionPropogation = false;
	Viewer3D::m_ConvertAltitudeToFeet = Config::getInstance()->getValueAsBool ("Viewer3D::ConvertAltitudeToFeet", false);
	m_PauseSpyReplay = Config::getInstance()->getValueAsBool ("ThreeDViewer::StartSpyReplayPaused", false);
	m_SwarmSpyHandler->pauseReplaySpy (m_PauseSpyReplay);

	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYSHADOW, MF_BYCOMMAND|((Viewer3D::m_DisplayShadowOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_USEPICTEL, MF_BYCOMMAND|((Viewer3D::m_DisplayPicTelForShadowOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYORBIT, MF_BYCOMMAND|((Viewer3D::m_DisplayOrbitOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYPGCS, MF_BYCOMMAND|((Viewer3D::m_DisplayGcsOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYPARTICLEDETECTIONS, MF_BYCOMMAND|((Viewer3D::m_DisplayParticlesOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYCHEMICALDETECTIONS, MF_BYCOMMAND|((Viewer3D::m_DisplayChemicalOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYPREDICTEDCLOUD, MF_BYCOMMAND|((Viewer3D::m_DisplayPredictedCloudOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYEXPLOSIONINDICATOR, MF_BYCOMMAND|((Viewer3D::m_DisplayExplosionOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYMETDATA, MF_BYCOMMAND|((Viewer3D::m_DisplayMetBeliefOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYIREXPLOSIONALGORITHMENABLED, MF_BYCOMMAND|((Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYSENSOROVERLAYS, MF_BYCOMMAND|((Viewer3D::m_DrawSensorOverlays)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYPROPOGATEDDETECTIONS, MF_BYCOMMAND|((Viewer3D::m_DisplayPropogatedDetectionsOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), ID_OPTIONS_DISPLAYLOITERAPPROACHPATH, MF_BYCOMMAND|((Viewer3D::m_DisplayLoiterApproachPathOpt)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_OPTIONS_PAUSEDETECTIONPROPOGATION, MF_BYCOMMAND|((Viewer3D::m_PauseDetectionPropogation)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 1), ID_UNITS_ALTITUDEINFEET, MF_BYCOMMAND|((Viewer3D::m_ConvertAltitudeToFeet)?MF_CHECKED:MF_UNCHECKED));
	CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_SWARMSPY_PAUSEREPLAY, MF_BYCOMMAND|((m_PauseSpyReplay)?MF_CHECKED:MF_UNCHECKED));



	//set up raw data update thread
	m_KillRawUpdateThread = false;
	m_RawUpdateThreadDone = false;
	
	rawUpdateThreadParams = new ThreadParams ();
	rawUpdateThreadParams->gui = this;
	rawUpdateThreadParams->m_KillThread = &m_KillRawUpdateThread;
	rawUpdateThreadParams->m_ThreadDone = &m_RawUpdateThreadDone;
	
	int stackSizeRawUpdate = Config::getInstance()->getValueAsInt ("ThreadStackSize.RawUpdatePaintThread.Bytes", 100000);
	hRawDataUpdateThread = (HANDLE)_beginthread( &ThreeDViewer::rawDataUpdateThread, stackSizeRawUpdate, rawUpdateThreadParams);
	if (hRawDataUpdateThread == NULL)
	{
		char text [256];
		sprintf_s (text, 256, "Unable to start raw data update thread\0");
		Logger::getInstance()->logMessage (text);
		throw new exception (text);
	}


	sprintf_s(logBuf, 256, "ThreeDViewer: Belief Collection initialized.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);


	
}

ThreeDViewer::~ThreeDViewer ()
{
	//Update config file WACS settings
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayShadowOpt", Viewer3D::m_DisplayShadowOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayPicTelForShadowOpt", Viewer3D::m_DisplayPicTelForShadowOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayOrbitOpt", Viewer3D::m_DisplayOrbitOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayGcsOpt", Viewer3D::m_DisplayGcsOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayParticlesOpt", Viewer3D::m_DisplayParticlesOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayChemicalOpt", Viewer3D::m_DisplayChemicalOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayPredictedCloud", Viewer3D::m_DisplayPredictedCloudOpt );
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayExplosionOpt", Viewer3D::m_DisplayExplosionOpt);
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayMetBeliefOpt", Viewer3D::m_DisplayMetBeliefOpt);
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayIrExplosionAlgorithmEnabledBeliefOpt", Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt);
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplaySensorOverlaysOpt", Viewer3D::m_DrawSensorOverlays);
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayPropogatedDetectionsOpt", Viewer3D::m_DisplayPropogatedDetectionsOpt);
	Config::getInstance()->setValueAsBool ("Viewer3D::DisplayLoiterApproachPathOpt", Viewer3D::m_DisplayLoiterApproachPathOpt);
	Config::getInstance()->setValueAsBool ("Viewer3D::ConvertAltitudeToFeet", Viewer3D::m_ConvertAltitudeToFeet);
	
	
	Logger::getInstance()->setListboxWnd (NULL);

	m_KillGLThread = true;
	while (!m_GLThreadDone)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "ThreeDViewer: Waiting for gl thread to end in destructor\0");
		Logger::getInstance()->logMessage (logBuf);

		Sleep (100);
	}

	m_KillRawUpdateThread= true;
	while (!m_RawUpdateThreadDone)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "ThreeDViewer: Waiting for raw update thread to end in destructor\0");
		Logger::getInstance()->logMessage (logBuf);

		Sleep (100);
	}

	if (myConfirmDialog)
		delete myConfirmDialog;

	if (myRawDataDialog)
		delete myRawDataDialog;

	DestroyWindow (hWnd);

	if (m_SwarmSpyHandler)
		delete m_SwarmSpyHandler;

	if (m_BeliefCollection)
		delete m_BeliefCollection;
	
	if (font3)
		DeleteObject (font3);

	if (glThreadParams != NULL)
		delete glThreadParams;

	if (rawUpdateThreadParams != NULL)
		delete rawUpdateThreadParams;
}

void ThreeDViewer::run ()
{
	//Start message processing loop
	runMsgLoop();
}

void ThreeDViewer::initSwarmSpy ()
{
	if (m_SwarmSpyInitialized)
		return;
	m_SwarmSpyInitialized = true;

	char logBuf [256];
	string logString;
	sprintf_s(logBuf, 256, "ThreeDViewer: Starting swarm spy.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);


	bool logData = Config::getInstance()->getValueAsBool ("SwarmSpy.LogData", true);
	if (m_DoSocketSpy)
	{
		string address;
		Config::getInstance()->getValue ("SwarmSpy.MulitCastAddress", address, string ("224.0.0.175"));
		int port = Config::getInstance()->getValueAsInt("SwarmSpy.Port", 38116);
	
		m_SwarmSpyHandler->initSocketSpy (logData, (char*)address.c_str(), port);
	}
	else
	{
		string filename;
		Config::getInstance()->getValue ("SwarmSpy.ReplayFilename", filename, string ("./SwarmLogs/dugwayLog_5_25_2011_BT.log"));
		float replaySpeed = Config::getInstance()->getValueAsFloat("SwarmSpy.ReplaySpeed", 50);
	
		m_SwarmSpyHandler->initReplaySpy (logData, (char*)filename.c_str(), replaySpeed);
	}

	m_SwarmSpyHandler->registerForAgentPositionBelief();	
	m_SwarmSpyHandler->registerForCircularOrbitBelief();
	m_SwarmSpyHandler->registerForPiccoloTelemetryBelief();
	m_SwarmSpyHandler->registerForParticleDetectionBelief();
	m_SwarmSpyHandler->registerForAnacondaDetectionBelief();
	m_SwarmSpyHandler->registerForAgentModeBelief();
	m_SwarmSpyHandler->registerForCloudDetectionBelief();
	m_SwarmSpyHandler->registerForCloudPredictionBelief();
	m_SwarmSpyHandler->registerForExplosionBelief();
	m_SwarmSpyHandler->registerForMetBelief();
	m_SwarmSpyHandler->registerForRacetrackOrbitBelief();
	m_SwarmSpyHandler->registerForIrExplosionAlgorithmEnabledBelief();
	m_SwarmSpyHandler->registerForLoiterApproachPathBelief();
	
	sprintf_s(logBuf, 256, "ThreeDViewer: Swarm spy initialized.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);
}

void ThreeDViewer::initDialog()
{
	// Initialize global strings
	LoadString( hInst, IDS_APP_TITLE, szTitle, MAX_LOADSTRING );
	LoadString( hInst, IDC_CRATRGUI, szWindowClass, MAX_LOADSTRING );
	
	MyRegisterClass();

	
	char logBuf [256];
	string logString;
	sprintf_s(logBuf, 256, "ThreeDViewer: Processing pre-loader.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);
	preLoadObjects();

	
	sprintf_s(logBuf, 256, "ThreeDViewer: Generating window.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);
	Viewer3D::freezeViews ();
	
	if (createDialog ())
		Viewer3D::freezeViews (false);

	
	sprintf_s(logBuf, 256, "ThreeDViewer: Processing post-loader.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);
	postLoadObjects();
}

bool ThreeDViewer::createDialog () 
{
	//Initialize GL classes
	Viewer3D::initViewer3D ();
	
	char logBuf [256];
	string logString;
	sprintf_s(logBuf, 256, "ThreeDViewer: Instantiating window object.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);

	//Actually create the main window
	hWnd = CreateDialogParam(NULL, MAKEINTRESOURCE(IDD_MAIN), NULL, (DLGPROC) WndProc, (long)this); 

	if(hWnd == NULL)
    {
		DWORD error = GetLastError();

        MessageBox(NULL, "Window Creation Failed!", "Error!",
            MB_ICONEXCLAMATION | MB_OK);
        return false;
    }
	sprintf_s(logBuf, 256, "ThreeDViewer: Window instantiated.\0");
	logString = (string)logBuf;
	Logger::getInstance()->logMessage (logString);

	MapViewWnd = GetDlgItem (hWnd, IDC_MAP_VIEW);
	Viewer3D::mouseOutputWnd = GetDlgItem (hWnd, IDC_MOUSEOUTPUT);


	ShowWindow(hWnd, SW_SHOW);
    UpdateWindow(hWnd);
	SetForegroundWindow(hWnd);					// Slightly Higher Priority
	SetFocus(hWnd);								// Sets Keyboard Focus To The Window

	SendMessage (MapViewWnd, UPDATE_WINDOW, 0, 0);
	return true;
}

void ThreeDViewer::setWindowPreferredSizes ()
{
	int x, y, width, height;

	DialogBase::getWindowSize (hWnd, wndWidth, wndHeight);
	wndHeight -= topBuffer;
	wndWidth -= sideBuffer;

	DialogBase::getWindowPosition (GetDlgItem (hWnd, IDC_MAP_VIEW), x, y);
	DialogBase::getWindowSize (GetDlgItem (hWnd, IDC_MAP_VIEW), width, height);
	mapPrefDistFromLeft = x - sideBuffer;
	mapPrefDistFromTop = y - topBuffer;
	mapPrefDistFromRight = wndWidth - x - width + sideBuffer;
	mapPrefDistFromBottom = wndHeight - y - height + topBuffer;

	//Status bar stuff	
	DialogBase::getWindowPosition (GetDlgItem (hWnd, IDC_MOUSELABEL), x, y);
	DialogBase::getWindowSize (GetDlgItem (hWnd, IDC_MOUSELABEL), width, height);
	mouseLabelPrefDistFromRight = wndWidth - x + sideBuffer;
	mouseLabelPrefDistFromBottom = wndHeight - y + topBuffer;
	mouseLabelPrefWidth = width;
	mouseLabelPrefHeight = height;

	DialogBase::getWindowPosition (GetDlgItem (hWnd, IDC_MOUSEOUTPUT), x, y);
	DialogBase::getWindowSize (GetDlgItem (hWnd, IDC_MOUSEOUTPUT), width, height);
	mouseOutputPrefDistFromRight = wndWidth - x + sideBuffer;
	mouseOutputPrefDistFromBottom = wndHeight - y + topBuffer;
	mouseOutputPrefWidth = width;
	mouseOutputPrefHeight = height;

	usedStatusSpace = mouseLabelPrefWidth + mouseOutputPrefWidth + width;
	clearStatusSpace = wndWidth - usedStatusSpace;
	usedStatusSpace = mouseLabelPrefWidth + mouseOutputPrefWidth;
}

void ThreeDViewer::resizeWindow()
{
	InvalidateRect (hWnd, NULL, FALSE);
	
	DialogBase::getWindowSize (hWnd, wndWidth, wndHeight);
	wndHeight -= topBuffer;
	wndWidth -= sideBuffer;
		
	MoveWindow (GetDlgItem (hWnd, IDC_MAP_VIEW), mapPrefDistFromLeft, mapPrefDistFromTop, wndWidth - mapPrefDistFromRight - mapPrefDistFromLeft, wndHeight - mapPrefDistFromTop - mapPrefDistFromBottom, true);


	//Bottom status bar
	InvalidateRect (GetDlgItem (hWnd, IDC_MOUSELABEL), NULL, TRUE);
	InvalidateRect (GetDlgItem (hWnd, IDC_MOUSEOUTPUT), NULL, TRUE);
	MoveWindow (GetDlgItem (hWnd, IDC_MOUSELABEL), wndWidth - mouseLabelPrefDistFromRight, wndHeight - mouseLabelPrefDistFromBottom, mouseLabelPrefWidth, mouseLabelPrefHeight, true);
	MoveWindow (GetDlgItem (hWnd, IDC_MOUSEOUTPUT), wndWidth - mouseOutputPrefDistFromRight, wndHeight - mouseOutputPrefDistFromBottom, mouseOutputPrefWidth, mouseOutputPrefHeight, true);

	UpdateWindow (GetDlgItem (hWnd, IDC_MOUSELABEL));
	UpdateWindow (GetDlgItem (hWnd, IDC_MOUSEOUTPUT));

	winNeedsResize = false;

	ShowWindow (hWnd, SW_SHOW);
	UpdateWindow (hWnd);

}

void ThreeDViewer::setMinMaxInfo(MINMAXINFO* info)
{
	info->ptMinTrackSize.x = minWidth;
	info->ptMinTrackSize.y = minHeight;
}

void ThreeDViewer::runMsgLoop()
{
	HACCEL hAccelTable;
	hAccelTable = LoadAccelerators(hInst, (LPCTSTR)IDC_CRATRGUI);

	MSG Msg;
	
	bool stop = false;
	
	while(GetMessage(&Msg, NULL, 0, 0) > 0)
    {
		if (updateRawDataNext)
		{
			updateRawDataDialog();
			updateRawDataNext = false;
		}

		if (Msg.message == WM_PAINT && (Msg.hwnd == MapViewWnd))
		{
			//Remove opengl from wm_paint
			ValidateRect (Msg.hwnd, NULL);
			continue;
		}
		
		if (Msg.message == WM_KEYDOWN || Msg.message == WM_KEYUP)
		{
			//Capture all keyboard events, unless they were directed to specific windows that can handle them.
			if ((myConfirmDialog == NULL || !myConfirmDialog->containsWindow (Msg.hwnd))
				&& (myRawDataDialog == NULL || !myRawDataDialog->containsWindow (Msg.hwnd))
				)
				Msg.hwnd = hWnd;
		}
		
		if (!TranslateAccelerator(Msg.hwnd, hAccelTable, &Msg)) 
		{
			//Send tabs/enter to confirm dialog box
			if((myConfirmDialog == NULL || !IsDialogMessage(myConfirmDialog->gethWnd(), &Msg))
				&& (myRawDataDialog == NULL || !IsDialogMessage(myRawDataDialog->gethWnd(), &Msg))
				)
			{
				TranslateMessage(&Msg);
				DispatchMessage(&Msg);
			}
		}
    }
}

void ThreeDViewer::updateRawDataDialog ()
{
	if (m_BeliefCollection != NULL)
		m_BeliefCollection->updateRawDataDialog();
}

void ThreeDViewer::rawDataUpdateThread (void* paramsVd)
{
	clock_t lastUpdate = clock();
	clock_t currUpdate;
	Sleep (5000);

	try
	{
		ThreadParams* params = (ThreadParams*)paramsVd;
		ThreeDViewer* gui = params->gui;
		
		while (!*(params->m_KillThread))
		{
			double secondsElapsed = (double)((currUpdate = clock()) - lastUpdate)/CLOCKS_PER_SEC;
			
			// At frameRate, update everything
			if (secondsElapsed > gui->frameRateRawUpdate)
			{
				gui->updateRawDataNext = true;
				
				//This just forces a message into the queue.  It happens to be a GL message that we ignore later.
				InvalidateRect (gui->MapViewWnd, NULL, FALSE);
			
				lastUpdate = currUpdate;

			}
			else
				Sleep (10);
		}

		*(params->m_ThreadDone) = true;
		_endthread ();
	}
	catch (exception e)
	{
		char text [256];
		sprintf_s (text, 256, "ThreeDViewer: Unhandled exception in raw data update thread: %s\0", e.what());
		Logger::getInstance()->immediateLogMessage (text);
		throw e;
	}
}

void ThreeDViewer::glPaintThread (void* paramsVd)
{
	clock_t lastUpdate = clock();
	clock_t currUpdate;
	Sleep (5000);

	try
	{
		ThreadParams* params = (ThreadParams*)paramsVd;
		ThreeDViewer* gui = params->gui;
		
		while (!*(params->m_KillThread))
		{
			double secondsElapsed = (double)((currUpdate = clock()) - lastUpdate)/CLOCKS_PER_SEC;
			
			// At frameRate, update everything
			if (secondsElapsed > gui->frameRate)
			{
				//Resize windows when necessary
				if (gui->winNeedsResize)
				{
					gui->resizeWindow ();
				}

				gui->updateWindows();	
			
				lastUpdate = currUpdate;




				//If replay WACS SwarmSpy, start spy after GL is up and ready so we don't miss the 
				//beginning of the file.
				if (!gui->m_DoSocketSpy && !gui->m_SwarmSpyInitialized)
				{
					while (!Viewer3D::m_GLReady)
					{
						Sleep (100);
					}
					gui->initSwarmSpy ();	
				}

			}
			else
				Sleep (10);
		}

		*(params->m_ThreadDone) = true;
		_endthread ();
	}
	catch (exception e)
	{
		char text [256];
		sprintf_s (text, 256, "ThreeDViewer: Unhandled exception in gl paint thread: %s\0", e.what());
		Logger::getInstance()->immediateLogMessage (text);
		throw e;
	}
}



ATOM ThreeDViewer::MyRegisterClass()
{
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX); 
	wcex.style			= CS_HREDRAW | CS_VREDRAW | CS_OWNDC;		// Redraw On Move, And Own DC For Window
	wcex.lpfnWndProc	= (WNDPROC)WndProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= hInst;
	wcex.hIcon			= LoadIcon(hInst, (LPCTSTR)IDI_ICON2);
	wcex.hCursor		= LoadCursor(NULL, IDC_ARROW);
	wcex.hbrBackground	= (HBRUSH)(COLOR_WINDOW+1);
	wcex.lpszMenuName	= MAKEINTRESOURCE (IDC_CRATRGUI);
	wcex.lpszClassName	= szWindowClass;
	wcex.hIconSm		= LoadIcon(hInst, (LPCTSTR)IDI_ICON2);

	if(!RegisterClassEx(&wcex))
    {
        MessageBox(NULL, "Window Registration Failed!", "Error!",
            MB_ICONEXCLAMATION | MB_OK);
        return false;
    }
	return true;
}

void ThreeDViewer::exit (bool approved)
{
	if (!approved)
	{
		Viewer3D::mainWinHasFocus = false;
		//Force user to confirm action
		if (!myConfirmDialog)
			myConfirmDialog = new ConfirmDialog (hInst, hWnd);
	
		myConfirmDialog->show();
		myConfirmDialog->setType (EXITPROGRAM);
	}
	else
	{
		//stop thread for buffering and sending to java
		if (Viewer3D::m_VideoCaptureInitialized)
		{
			Viewer3D::m_EnableVideoCapture = false;
			Sleep (100);
			VideoCapture_StopThread();
		}

		Logger::getInstance()->setListboxWnd (NULL);
		char logBuf [256];
		sprintf_s(logBuf, 256, "ThreeDViewer: User requested exit\n");
		Logger::getInstance()->logMessage ((string)logBuf);

		Sleep (100);
		DestroyWindow (hWnd);
		hWnd = NULL;

	}
}

void ThreeDViewer::activate()
{
	UpdateWindow (hWnd);
}

void ThreeDViewer::updateWindows()
{
	SendMessage (MapViewWnd, MY_PAINT, 0, 0);
}

void ThreeDViewer::preLoadObjects()
{
	//Nothing to preload at this point
}

void ThreeDViewer::postLoadObjects()
{
	//Nothing to postload at this point
}

void ThreeDViewer::resetCamera ()
{
	SendMessage (MapViewWnd, RESET_VIEWS, 0, 0);
	SetFocus (MapViewWnd);
}


LRESULT CALLBACK ThreeDViewer::WndProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	LONG_PTR lptr = GetWindowLongPtr (hwnd, GWLP_USERDATA);
	ThreeDViewer* control = NULL;
	control = reinterpret_cast<ThreeDViewer*> ((LPARAM)lptr);

	if (!control&& message != WM_INITDIALOG)
		return DefWindowProc(hwnd, message, wParam, lParam);
	else if (message == WM_INITDIALOG)
	{
		try {
			control = reinterpret_cast<ThreeDViewer*>(lParam);
			SetWindowLongPtr (hwnd, GWLP_USERDATA, (LONG)lParam);
			control->hWnd = hwnd;
		}
		catch (...)
		{
			MessageBox (NULL, "Error extracting ThreeDViewer", "Error", MB_OK|MB_ICONEXCLAMATION);
			control = NULL;
		}
		
	}
	
	if (control == NULL)
	{
		MessageBox (NULL, "Could not extract ThreeDViewer", "Error", MB_OK|MB_ICONEXCLAMATION);
		return false;
	}
	
	return control->processMsg (hwnd, message, wParam, lParam);

}


LRESULT ThreeDViewer::processMsg (HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)
{

	switch(message)
    {
		
		int wmId, wmEvent;
		
		case WM_INITDIALOG: 
		{
			HANDLE hIcon = LoadImage(hInst, "./Bitmaps/bigIcon.ico", IMAGE_ICON, 32, 32, LR_LOADFROMFILE |  LR_DEFAULTCOLOR);
			int err = GetLastError ();
			if(hIcon)
			{
				HANDLE old = (HANDLE)SendMessage(hWnd, WM_SETICON, ICON_BIG, (LPARAM)hIcon);
				old = old;
			}
			
			return TRUE;
		}
		
		case WM_CTLCOLORSTATIC:
		{
			//Change color styles of static controls
			break;
		}

		case WM_CTLCOLORDLG:
		{
			//Change color style of dialog window
			break;
		}

		case WM_DRAWITEM:
		{
			break;
		}

		case WM_SETFOCUS: {
			Viewer3D::mainWinHasFocus = true;
			return TRUE;
		}

		case WM_KILLFOCUS: {
			Viewer3D::mainWinHasFocus = false;
			return TRUE;
		}

		case WM_ACTIVATE:

			activate();
			Viewer3D::mainWinHasFocus = true;
			break;
		case WM_CLOSE: 
		{
			exit();
			return 0;
		}
		case MOUSE_UNCLICKED:
		{
			return 0;
		}
		case WM_SIZE: 
		{
			winNeedsResize = true;
			return 0;
		}
		case WM_SIZING:
		{
			if (Viewer3D::m_EnableVideoCapture)
			{
				RECT rect = { 0 };
				GetWindowRect(hwnd, &rect );
				
				RECT *newRect = (RECT*)lParam;
				newRect->bottom = rect.bottom;
				newRect->top = rect.top;
				newRect->right = rect.right;
				newRect->left = rect.left;
			}

			return TRUE;
		}
		case WM_GETMINMAXINFO: 
		{
			MINMAXINFO* pMinMaxInfo = (MINMAXINFO*)lParam;
			if (Viewer3D::m_EnableVideoCapture)
			{
				MINMAXINFO* pMinMaxInfo = (MINMAXINFO*)lParam;
				
				RECT rect = { 0 };
				
				GetWindowRect(hwnd, &rect );
				// Set the maximum size. Used while maximizing.
				pMinMaxInfo->ptMaxSize.x = rect.right-rect.left;
				pMinMaxInfo->ptMaxSize.y = rect.bottom-rect.top;
				pMinMaxInfo->ptMaxTrackSize.x = rect.right-rect.left;
				pMinMaxInfo->ptMaxTrackSize.y = rect.bottom-rect.top;
				pMinMaxInfo->ptMinTrackSize.x = rect.right-rect.left;
				pMinMaxInfo->ptMinTrackSize.y = rect.bottom-rect.top;
				
			}
			setMinMaxInfo(pMinMaxInfo);

			return 0;
		}
		case WM_DESTROY: {
			PostQuitMessage(0);
			break;
		}
	
		 case WM_MOUSEMOVE:
		{
			return 0;
		}
		case WM_RBUTTONDOWN:
		{
			return 0;
		}


		case ID_YES: 
		{
			//Response to ConfirmDialog pop-up
			Viewer3D::mainWinHasFocus = true;
			switch ((int)wParam) {
				case EXITPROGRAM:
					exit (true);
					break;
				
			}
			break;
		}
		case ID_NO :
		{
			//Response to ConfirmDialog pop-up
			Viewer3D::mainWinHasFocus = true;
			break;
		}

		case WM_KEYDOWN: 
		{
			return 0;
		}

		case WM_KEYUP: 
		{			
			return 0;
		}
		
		case WM_COMMAND: {
			wmId    = LOWORD(wParam); 
			wmEvent = HIWORD(wParam); 
			
			// Parse the menu selections:
			switch (wmId) 
			{

				case ID_LOG_LOG:
				{
					Viewer3D::mainWinHasFocus = true;
					ShowWindow (logWindow, SW_SHOW);
					break;
				}
				case ID_OPTIONS_DISPLAYSHADOW:
				{
					Viewer3D::m_DisplayShadowOpt = !Viewer3D::m_DisplayShadowOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayShadowOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_USEPICTEL:
				{
					Viewer3D::m_DisplayPicTelForShadowOpt = !Viewer3D::m_DisplayPicTelForShadowOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayPicTelForShadowOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYORBIT:
				{
					Viewer3D::m_DisplayOrbitOpt = !Viewer3D::m_DisplayOrbitOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayOrbitOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYPGCS:
				{
					Viewer3D::m_DisplayGcsOpt = !Viewer3D::m_DisplayGcsOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayGcsOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYPARTICLEDETECTIONS:
				{
					Viewer3D::m_DisplayParticlesOpt = !Viewer3D::m_DisplayParticlesOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayParticlesOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYCHEMICALDETECTIONS:
				{
					Viewer3D::m_DisplayChemicalOpt = !Viewer3D::m_DisplayChemicalOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayChemicalOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYPREDICTEDCLOUD:
				{
					Viewer3D::m_DisplayPredictedCloudOpt = !Viewer3D::m_DisplayPredictedCloudOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayPredictedCloudOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYEXPLOSIONINDICATOR:
				{
					Viewer3D::m_DisplayExplosionOpt = !Viewer3D::m_DisplayExplosionOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayExplosionOpt)?MF_CHECKED:MF_UNCHECKED));
					break;	
				}
				case ID_OPTIONS_DISPLAYMETDATA:
				{
					Viewer3D::m_DisplayMetBeliefOpt = !Viewer3D::m_DisplayMetBeliefOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayMetBeliefOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				case ID_OPTIONS_DISPLAYIREXPLOSIONALGORITHMENABLED:
				{
					Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt = !Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt)?MF_CHECKED:MF_UNCHECKED));
					break;	
				}
				case ID_OPTIONS_DISPLAYSENSOROVERLAYS:
				{
					Viewer3D::m_DrawSensorOverlays = !Viewer3D::m_DrawSensorOverlays;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DrawSensorOverlays)?MF_CHECKED:MF_UNCHECKED));
					break;
				}

				case ID_OPTIONS_DISPLAYPROPOGATEDDETECTIONS:
				{
					Viewer3D::m_DisplayPropogatedDetectionsOpt = !Viewer3D::m_DisplayPropogatedDetectionsOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayPropogatedDetectionsOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}

				case ID_OPTIONS_DISPLAYLOITERAPPROACHPATH:
				{
					Viewer3D::m_DisplayLoiterApproachPathOpt = !Viewer3D::m_DisplayLoiterApproachPathOpt;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 0), wmId, MF_BYCOMMAND|((Viewer3D::m_DisplayLoiterApproachPathOpt)?MF_CHECKED:MF_UNCHECKED));
					break;
				}

				case ID_OPTIONS_PAUSEDETECTIONPROPOGATION:
				{
					Viewer3D::m_PauseDetectionPropogation = !Viewer3D::m_PauseDetectionPropogation;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), wmId, MF_BYCOMMAND|((Viewer3D::m_PauseDetectionPropogation)?MF_CHECKED:MF_UNCHECKED));
					break;
				}
				
				case ID_OPTIONS_SHOWRAWTABLES:
				{
					myRawDataDialog->show();
					break;
				}

				case ID_UNITS_ALTITUDEINFEET:
				{
					Viewer3D::m_ConvertAltitudeToFeet = !Viewer3D::m_ConvertAltitudeToFeet;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 1), wmId, MF_BYCOMMAND|((Viewer3D::m_ConvertAltitudeToFeet)?MF_CHECKED:MF_UNCHECKED));
					break; 
				}

				case ID_SWARMSPY_RESTARTLOGGING:
				{
					if (m_SwarmSpyHandler != NULL && m_SwarmSpyInitialized)
					{
						m_SwarmSpyHandler->restartSpyLogging ();
					}
					break;
				}

				case ID_SWARMSPY_PAUSEREPLAY:
				{
					if (m_SwarmSpyHandler != NULL && m_SwarmSpyInitialized)
					{
						m_PauseSpyReplay = !m_PauseSpyReplay;
						CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), wmId, MF_BYCOMMAND|((m_PauseSpyReplay)?MF_CHECKED:MF_UNCHECKED));
						
						m_SwarmSpyHandler->pauseReplaySpy (m_PauseSpyReplay);
					
					}
					break;
				}

				case ID_SWARMSPY_PAUSEALL:
				{
					if (m_SwarmSpyHandler != NULL && m_SwarmSpyInitialized)
					{
						m_PauseSpyReplay = true;
						CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_SWARMSPY_PAUSEREPLAY, MF_BYCOMMAND|((m_PauseSpyReplay)?MF_CHECKED:MF_UNCHECKED));
						
						m_SwarmSpyHandler->pauseReplaySpy (m_PauseSpyReplay);


						Viewer3D::m_PauseDetectionPropogation = true;
						CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_OPTIONS_PAUSEDETECTIONPROPOGATION, MF_BYCOMMAND|((Viewer3D::m_PauseDetectionPropogation)?MF_CHECKED:MF_UNCHECKED));
					}
					break;
				}

				case ID_SWARMSPY_UNPAUSEALL:
				{
					if (m_SwarmSpyHandler != NULL && m_SwarmSpyInitialized)
					{
						m_PauseSpyReplay = false;
						CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_SWARMSPY_PAUSEREPLAY, MF_BYCOMMAND|((m_PauseSpyReplay)?MF_CHECKED:MF_UNCHECKED));
						
						m_SwarmSpyHandler->pauseReplaySpy (m_PauseSpyReplay);


						Viewer3D::m_PauseDetectionPropogation = false;
						CheckMenuItem (GetSubMenu(GetMenu (hWnd), 2), ID_OPTIONS_PAUSEDETECTIONPROPOGATION, MF_BYCOMMAND|((Viewer3D::m_PauseDetectionPropogation)?MF_CHECKED:MF_UNCHECKED));
					}
					break;
				}

				case ID_SWARMSPY_RESTARTREPLAY:
				{
					if (m_SwarmSpyHandler != NULL && m_SwarmSpyInitialized)
					{
						m_SwarmSpyHandler->restartReplaySpy ();
						m_BeliefCollection->resetBeliefs();
					}
					break;
				}

				case ID_VIEW_RESETVIEW:
				{
					this->resetCamera();
					break;
				}

				case ID_VIDEOCAPTURE_ENABLED:
				{
					Viewer3D::m_EnableVideoCapture = !Viewer3D::m_EnableVideoCapture;
					CheckMenuItem (GetSubMenu(GetMenu (hWnd), 4), wmId, MF_BYCOMMAND|((Viewer3D::m_EnableVideoCapture)?MF_CHECKED:MF_UNCHECKED));
					break;
				}

				default: 
				{

					return FALSE;

				}
			}
			break;
		}
		
		default: 
		{			
            return FALSE;
		}
    }
    return 1;

}

LRESULT CALLBACK LogWndProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
	UNREFERENCED_PARAMETER(lParam);
	switch (message)
	{
		case WM_INITDIALOG:
			return (INT_PTR)TRUE;

		case WM_CLOSE: 
		{
			ShowWindow (hDlg, SW_HIDE);
			return 0;
		}
	
	}
	return (INT_PTR)FALSE;
}
