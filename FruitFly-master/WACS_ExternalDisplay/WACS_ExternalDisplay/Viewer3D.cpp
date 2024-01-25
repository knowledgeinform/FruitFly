#include "stdafx.h"
#include "Viewer3D.h"
#include <tchar.h>
#include <math.h>
#include "WinFonts.h"
#include "Logger.h"
#include "DtedLoader.h"
#include "Config.h"
#include "DialogBase.h"
#include <iomanip>
#include "Utils.h"
#include "VideoCaptureHandler.h"

/*#include <fstream>
#include <ostream>
#include <iomanip>
#include "XvidEncoder.h"
using namespace std;*/

#pragma unmanaged

TCHAR Viewer3D::szClassName[] = _T("Viewer3D");
bool Viewer3D::initialized = false;
double Viewer3D::mouseLatDD = 0;
double Viewer3D::mouseLngDD = 0;
double Viewer3D::mouseTerrainAltM = 0;
bool Viewer3D::mouseOnGround = false;
bool Viewer3D::centerOnGround = false;
int Viewer3D::CenterX = 0;
int Viewer3D::CenterY = 0;
glCamera Viewer3D::Cam;
bool Viewer3D::mainWinHasFocus = true;
bool Viewer3D::mouseInUse = false;
HWND Viewer3D::mouseInUseWnd = NULL;
bool Viewer3D::freezeView = false;
HWND Viewer3D::mouseOutputWnd = NULL;
bool Viewer3D::m_GLReady = false;

BeliefCollection* Viewer3D::m_BeliefCollection = NULL;
bool Viewer3D::m_DisplayShadowOpt = false;
bool Viewer3D::m_DisplayPicTelForShadowOpt = false;
bool Viewer3D::m_DisplayOrbitOpt = false;
bool Viewer3D::m_DisplayGcsOpt = false;
bool Viewer3D::m_DisplayParticlesOpt = false;
bool Viewer3D::m_DisplayChemicalOpt = false;
bool Viewer3D::m_DisplayPredictedCloudOpt = false;
bool Viewer3D::m_DisplayExplosionOpt = false;
bool Viewer3D::m_DisplayMetBeliefOpt = false;
bool Viewer3D::m_DisplayIrExplosionAlgorithmEnabledBeliefOpt = false;
bool Viewer3D::m_DrawSensorOverlays = false;
bool Viewer3D::m_DisplayPropogatedDetectionsOpt = false;
bool Viewer3D::m_PauseDetectionPropogation = false;
bool Viewer3D::m_ConvertAltitudeToFeet = false;
bool Viewer3D::m_DisplayLoiterApproachPathOpt = false;
bool Viewer3D::m_EnableVideoCapture = false;
bool Viewer3D::m_VideoCaptureInitialized = false;

Viewer3D::Viewer3D ()
{
	orthoscale = 1.0;
	estLatToMConv = DEG2M;
	estLonToMConv = DEG2M;
	invorthoscale = 1.0/orthoscale;
	minFont = 1;
	maxFont = 32;
	screenz = 500.0f;						// Depth Into The Screen
	leftClickDown = false;

	m_MapData = new MapData();

	m_CenterPointPosition = NULL;
	readConfig();

	rotationCenterLocationValid = false;
	screenCenterRangeToCamera = 0;

	m_BeliefCollection = NULL;
}

void Viewer3D::readConfig()
{
	m_MinDtedLatDecDeg = Config::getInstance()->getValueAsDouble ("DtedLimits.MinLat.DecDeg", 0);
	m_MinDtedLonDecDeg = Config::getInstance()->getValueAsDouble ("DtedLimits.MinLon.DecDeg", 0);
	m_MaxDtedLatDecDeg = Config::getInstance()->getValueAsDouble ("DtedLimits.MaxLat.DecDeg", 1);
	m_MaxDtedLonDecDeg = Config::getInstance()->getValueAsDouble ("DtedLimits.MaxLon.DecDeg", 1);
	string configValue = "";
	Config::getInstance()->getValue ("DtedLoader.FileExt", configValue, ".dt2");
	sprintf_s (&m_DtedFileExt[0], 5, "%4s\0", configValue.c_str());
	m_DtedDataInterval = Config::getInstance()->getValueAsInt ("DtedLoader.DataInterval", 10);
	Config::getInstance()->getValue ("MapLoader.ImageFileName", configValue, "./image.jpg");
	sprintf_s (&m_MapImageFilename[0], 256, "%s\0", configValue.c_str());
	Config::getInstance()->getValue ("MapLoader.BkgndImageFileName", configValue, "./image.jpg");
	sprintf_s (&m_BkgndMapImageFilename[0], 256, "%s\0", configValue.c_str());

	m_CameraStartLatDecDeg = Config::getInstance()->getValueAsDouble ("CameraStart.LatDecDeg", 0.5);
	m_CameraStartLonDecDeg = Config::getInstance()->getValueAsDouble ("CameraStart.LonDecDeg", 0.5);
	m_CameraStartAltM = Config::getInstance()->getValueAsDouble ("CameraStart.AltM", 50);
	m_CameraStartHeading = Config::getInstance()->getValueAsDouble ("CameraStart.HeadingDeg", 0);
	m_CameraStartPitch = Config::getInstance()->getValueAsDouble ("CameraStart.PitchDeg", -90);
	m_GlHeightFOVDeg = Config::getInstance()->getValueAsDouble ("Viewer3D.GlHeightFOVDeg", 45);

	screenCenterLocation.x = m_CameraStartLonDecDeg;
	screenCenterLocation.y = m_CameraStartLatDecDeg;
	screenCenterLocation.z = m_CameraStartAltM;
		
}

Viewer3D::~Viewer3D ()
{
	if (m_CenterPointPosition != NULL)
		delete m_CenterPointPosition;
}

void Viewer3D::setDefaultCameraView ()
{
	Cam.m_Position.x = 0.0;
	Cam.m_Position.y = 0.0;
	Cam.m_Position.z = 0.0;
	Cam.m_HeadingDegrees = -m_CameraStartHeading;
	Cam.m_PitchDegrees = -m_CameraStartPitch;
	Cam.updateTrig();
	Cam.m_Position.z = m_CameraStartAltM;
	while (!forceCameraAboveGround (Cam))
		Cam.m_Position.z += 5;
}

void Viewer3D::updateStatus ()
{
	char ds[1000];
	memset( ds, 0, 1000 );

	if (mouseOnGround)
		sprintf_s( ds, 1000, "Lat %10.5lf, Lng %10.5lf, Gnd-Alt %8.1lf %s\0", mouseLatDD, mouseLngDD, mouseTerrainAltM/(m_ConvertAltitudeToFeet?0.3048:1), m_ConvertAltitudeToFeet?"ft":"m" );
	
	SetWindowText (mouseOutputWnd, ds);

}

void Viewer3D::clearContexts()
{
	hDC = 0;
	hRC = 0;
}

void Viewer3D::initViewer3D()
{
	if (initialized)
		return;
	initialized = true;
    
	WNDCLASSEX wc;
    
    wc.cbSize         = sizeof(wc);
    wc.hInstance      = GetModuleHandle(0);
    wc.hCursor        = LoadCursor (NULL, IDC_ARROW);
    wc.hIcon          = 0;
    wc.lpszMenuName   = 0;
    wc.hbrBackground  = (HBRUSH)GetSysColorBrush(COLOR_BTNFACE);
    wc.style          = CS_DBLCLKS;
    wc.cbClsExtra     = 0;
    wc.cbWndExtra     = 0;
    wc.hIconSm        = 0;
	wc.lpszClassName  = szClassName;
    wc.lpfnWndProc    = (WNDPROC) wndProc;
	RegisterClassEx(&wc);
}

void Viewer3D::initGL()
{
	if (!makeCurrent ())
		return;
	
	glShadeModel(GL_SMOOTH);							// Enables Smooth Shading
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);				// Black Background
	glClearDepth(1.0f);									// Depth Buffer Setup
	glEnable(GL_DEPTH_TEST);							// Enables Depth Testing
	glEnable (GL_BLEND); 
	glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glDepthFunc(GL_LEQUAL);								// The Type Of Depth Test To Do
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);	// Really Nice Perspective Calculations
	glTexEnvi (GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);



	GLfloat LightAmbient[]= { 0.0f, 0.0f, 0.0f, 1.0f }; 	// Ambient Light Values ( NEW )
	GLfloat LightDiffuse[]= { 1.0f, 1.0f, 1.0f, 1.0f };		// Diffuse Light Values ( NEW )
	//GLfloat LightPosition[]= { 0.42427f, 0.42427f, 100000.8f, 0.0f };	// Light Position ( NEW )
	GLfloat LightPosition[]= { 5, 5, 3, 0.0f };	// Light Position ( NEW )

	glLightfv( GL_LIGHT1, GL_AMBIENT,  LightAmbient );	// Setup The Ambient Light
	glLightfv( GL_LIGHT1, GL_DIFFUSE,  LightDiffuse );	// Setup The Diffuse Light
	glLightfv( GL_LIGHT1, GL_POSITION, LightPosition );	// Position The Light
	glEnable( GL_LIGHT1 );								// Enable Light One
	//glEnable(GL_LIGHTING);

	// Now set up our max values for the camera
	Cam.m_MaxForwardVelocity = 5.0f;
	Cam.m_MaxPitchRate = 5.0f;
	Cam.m_MaxHeadingRate = 5.0f;
	Cam.m_PitchDegrees = -90.0f;
	Cam.m_HeadingDegrees = 0.0f;

	orthoscale = 1.0;
	invorthoscale = 1.0/orthoscale;
	Cam.m_Position.x = 0.0;
	Cam.m_Position.y = 0.0;
	Cam.m_Position.z = 0.0;
	float zeroAng = 0;
	Cam.m_HeadingDegrees = -zeroAng;
	Cam.m_PitchDegrees = -90.0;
	Cam.updateTrig();
	Cam.calcFromOffsets (0, 0, 0, estLatToMConv, estLonToMConv);	
	if (Cam.m_Position.z < 50)
		Cam.m_Position.z = 50;

	
	for (unsigned int ii=minFont; ii <= maxFont; ++ii) {
		WinFonts::BuildGLFont( hDC, ii*1000, ii);
	}
}

void Viewer3D::setUpGL()
{
	glViewport( 0, 0, winW, winH );		// Reset The Current Viewport


	glMatrixMode(GL_PROJECTION);			// Select The Projection Matrix
	glLoadIdentity();						// Reset The Projection Matrix

	// Calculate The Aspect Ratio Of The Window
	gluPerspective( m_GlHeightFOVDeg, (GLfloat)winW/(GLfloat)winH, 1.0, 1000000.0 );
	

	glMatrixMode(GL_MODELVIEW);				// Select The Modelview Matrix
	glLoadIdentity();						// Reset The Modelview Matrix
	needReset = false;
}


void Viewer3D::ShowCursorPos() 
{
	updateStatus();
}

void Viewer3D::getScreenToTerrain(int screenX, int screenY, double &screenLat, double &screenLon, double &screenZ, bool &posOnGround)
{
	if (m_CenterPointPosition == NULL)
	{
		posOnGround = false;
		return;
	}

	// Read back OpenGL matrices
	GLdouble ProjMatrix[16];
	GLdouble ModelMatrix[16];
	GLint    ViewMatrix[4];
	glGetDoublev( GL_PROJECTION_MATRIX, ProjMatrix );
	glGetDoublev( GL_MODELVIEW_MATRIX, ModelMatrix );
	glGetIntegerv( GL_VIEWPORT, ViewMatrix );
	
	screenY = (float)ViewMatrix[3] - (float)screenY;
	
	GLdouble objx = 0.0;
	GLdouble objy = 0.0;
	GLdouble objz = 0.0;

	float z = 0.0;
	glReadPixels(screenX, screenY, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, &z);
	gluUnProject( screenX, screenY, z, ModelMatrix, ProjMatrix, ViewMatrix, &objx, &objy, &objz );

	if (1 - z < 1E-20)
		posOnGround = false;
	else
		posOnGround = true;

	objx /= estLonToMConv;
	objy /= estLatToMConv;
	
	objx += m_CenterPointPosition->getLonDecDeg();
	objy += m_CenterPointPosition->getLatDecDeg();
	objz += m_CenterPointPosition->getAltMslM();

	screenLat = objy;
	screenLon = objx;
	screenZ = objz;
}

void Viewer3D::CheckMousePos() 
{
	POINT pt;
	GetCursorPos(&pt);
	ScreenToClient(hWnd, &pt);

	int MouseX = pt.x;
	int MouseY = pt.y;
	
	double offsetScale = 16000 * 5000/screenCenterRangeToCamera;
	if (offsetScale < 100)
		offsetScale = 100;

	int verticalShift = MouseY - CenterY;
	double yOffset = verticalShift/offsetScale*estLonToMConv/estLatToMConv ;

	int lateralShift = MouseX - CenterX;
	double xOffset = -lateralShift/offsetScale ;
	
	glCamera newCamera = Cam;
	newCamera.calcFromOffsets (xOffset, yOffset, 0, estLatToMConv, estLonToMConv);

	if (forceCameraAboveGround (newCamera))
		Cam = newCamera;
	
	ShowCursorPos();
	
	CenterX = MouseX;
	CenterY = MouseY;	
}

bool Viewer3D::forceCameraAboveGround (glCamera& camera)
{
	if (m_CenterPointPosition == NULL)
		return false;

	double minZ = m_MapData->getMinZ (camera.m_Position.y+m_CenterPointPosition->getLatDecDeg(), camera.m_Position.x+m_CenterPointPosition->getLonDecDeg());

	if ((minZ-m_CenterPointPosition->getAltMslM()) > camera.m_Position.z)
		return false;
	return true;
}

void Viewer3D::CheckMouseOrient() 
{
	POINT pt;
	GetCursorPos(&pt);
	ScreenToClient(hWnd, &pt);

	int MouseX = pt.x;
	int MouseY = pt.y;

	glCamera newCamera = Cam;
	glCamera lastCamera = Cam;

	double pitchShift = (MouseY - CenterY)*0.25;
	newCamera.m_PitchDegrees += -pitchShift;

	double headingShift = (MouseX - CenterX)*0.25;
	if (localCamera)
		newCamera.m_HeadingDegrees += -headingShift;
	else
		newCamera.m_HeadingDegrees += headingShift;

	newCamera.m_HeadingDegrees = fmod(newCamera.m_HeadingDegrees,360);

	
	if (localCamera)
	{
		//If only rotating camera, we don't do any shifting.  Done.

		if (newCamera.m_PitchDegrees < -179)
			newCamera.m_PitchDegrees = -179;
		if (newCamera.m_PitchDegrees > -1)
			newCamera.m_PitchDegrees = -1;
	
		newCamera.updateTrig();

	}
	else
	{
		if (centerRotation)
		{
			if (newCamera.m_PitchDegrees < -100)
				newCamera.m_PitchDegrees = -100;
			if (newCamera.m_PitchDegrees > -1)
				newCamera.m_PitchDegrees = -1;
		
			newCamera.updateTrig();

			if (centerOnGround)
			{
				double lonShift = screenCenterRangeToCamera*newCamera.sinHeading*newCamera.sinPitch/estLonToMConv;
				double latShift = screenCenterRangeToCamera*newCamera.cosHeading*newCamera.sinPitch/estLatToMConv;
				double zShift = screenCenterRangeToCamera*newCamera.cosPitch;

				newCamera.m_Position.x = screenCenterLocation.x + lonShift - m_CenterPointPosition->getLonDecDeg();
				newCamera.m_Position.y = screenCenterLocation.y + latShift - m_CenterPointPosition->getLatDecDeg();
				newCamera.m_Position.z = screenCenterLocation.z + zShift - m_CenterPointPosition->getAltMslM();
			}
		}
		else
		{
			if (newCamera.m_PitchDegrees < -100)
			{
				pitchShift -= -85-newCamera.m_PitchDegrees;
				newCamera.m_PitchDegrees = -100; 
			}
			if (newCamera.m_PitchDegrees > 0)
			{
				pitchShift -= 0-newCamera.m_PitchDegrees;
				newCamera.m_PitchDegrees = 0;
			}
		
			newCamera.updateTrig();



			//add stuff to move camera with rotations
			if (!rotationCenterLocationValid)
			{
				//First click for rotation, store rotation point
				rotationCenterLocationValid = updateMouseClickLocation ();

				//distance from click point to current camera point, positive from click point
				double deltaX = (mouseClickLocation.x - newCamera.m_Position.x - m_CenterPointPosition->getLonDecDeg())*estLonToMConv;
				double deltaY = (mouseClickLocation.y - newCamera.m_Position.y - m_CenterPointPosition->getLatDecDeg())*estLatToMConv;
				double deltaZ = mouseClickLocation.z - newCamera.m_Position.z - m_CenterPointPosition->getAltMslM();

				int pixDiffX = mouseClickPixX - centerPixX;
				int pixDiffY = mouseClickPixY - centerPixY;

				betaRad = ((double)pixDiffY)/wndHeight*m_GlHeightFOVDeg * DEG2RAD;
				alphaRad = -((double)pixDiffX)/wndHeight*m_GlHeightFOVDeg * DEG2RAD;

				mouseClickRangeToCamera = (-deltaZ)/sin(betaRad+(90+lastCamera.m_PitchDegrees)*DEG2RAD);



				//get error to offset camera on initial rotation
				double newdeltaX = mouseClickRangeToCamera*sin(alphaRad)*sin(betaRad+PI/2)*lastCamera.cosHeading -
								mouseClickRangeToCamera*cos(alphaRad)*sin(betaRad+(180+lastCamera.m_PitchDegrees)*DEG2RAD)*lastCamera.sinHeading;

				double newdeltaY = -mouseClickRangeToCamera*cos(alphaRad)*sin(betaRad+(180+lastCamera.m_PitchDegrees)*DEG2RAD)*lastCamera.cosHeading -
								mouseClickRangeToCamera*sin(alphaRad)*sin(betaRad+PI/2)*lastCamera.sinHeading;
				
				double newdeltaZ = mouseClickRangeToCamera*sin(betaRad+(90+lastCamera.m_PitchDegrees)*DEG2RAD);

				camErrX = (deltaX + newdeltaX);
				camErrY = (deltaY + newdeltaY);
				camErrZ = (deltaZ + newdeltaZ);
				headingFromClick = 0;

			}

			if (rotationCenterLocationValid)
			{
				double deltaX = mouseClickRangeToCamera*sin(alphaRad)*sin(betaRad+PI/2)*newCamera.cosHeading -
								mouseClickRangeToCamera*cos(alphaRad)*sin(betaRad+(180+newCamera.m_PitchDegrees)*DEG2RAD)*newCamera.sinHeading;

				double deltaY = -mouseClickRangeToCamera*cos(alphaRad)*sin(betaRad+(180+newCamera.m_PitchDegrees)*DEG2RAD)*newCamera.cosHeading -
								mouseClickRangeToCamera*sin(alphaRad)*sin(betaRad+PI/2)*newCamera.sinHeading;
				
				double deltaZ = mouseClickRangeToCamera*sin(betaRad+(90+newCamera.m_PitchDegrees)*DEG2RAD);

				//Offset camera away from click point as heading rotates
				headingFromClick += headingShift;
				double errCorrX = camErrX*cos(headingFromClick*DEG2RAD) + camErrY*sin(headingFromClick*DEG2RAD);
				double errCorrY = camErrY*cos(headingFromClick*DEG2RAD) - camErrX*sin(headingFromClick*DEG2RAD);
				double errCorrZ = camErrZ;

				double newLon = mouseClickLocation.x + (deltaX-errCorrX)/estLonToMConv;
				double newLat = mouseClickLocation.y + (deltaY-errCorrY)/estLatToMConv;
				double newZ = mouseClickLocation.z + (deltaZ-errCorrZ);

				newCamera.m_Position.x = newLon - m_CenterPointPosition->getLonDecDeg();
				newCamera.m_Position.y = newLat - m_CenterPointPosition->getLatDecDeg();
				newCamera.m_Position.z = newZ - m_CenterPointPosition->getAltMslM();

			}

		}
	}

	if (forceCameraAboveGround (newCamera))
		Cam = newCamera;
	ShowCursorPos();

	CenterX = MouseX;
	CenterY = MouseY;
}

bool Viewer3D::updateScreenCenterLocation ()
{
	wndWidth, wndHeight;
	DialogBase::getWindowSize (hWnd, wndWidth, wndHeight);

	centerPixX = wndWidth/2;
	centerPixY = wndHeight/2;

	double centerLat, centerLon, centerZ;
	getScreenToTerrain (centerPixX, centerPixY, centerLat, centerLon, centerZ, centerOnGround);
	
	if (centerOnGround)
	{
		screenCenterLocation.x = centerLon;
		screenCenterLocation.y = centerLat;
		screenCenterLocation.z = centerZ;

		double range = sqrt(pow((screenCenterLocation.y - Cam.m_Position.y - m_CenterPointPosition->getLatDecDeg())*estLatToMConv, 2.0) + pow((screenCenterLocation.x - Cam.m_Position.x - m_CenterPointPosition->getLonDecDeg())*estLonToMConv, 2.0) + pow((double)(screenCenterLocation.z - Cam.m_Position.z - m_CenterPointPosition->getAltMslM()), 2.0));
		screenCenterRangeToCamera = range;
		
		return true;
	}
	else
	{
		return false;
	}
}

bool Viewer3D::updateMouseClickLocation ()
{
	if (mouseOnGround)
	{
		mouseClickLocation.x = mouseLngDD;
		mouseClickLocation.y = mouseLatDD;
		mouseClickLocation.z = mouseTerrainAltM;

		POINT pt;
		GetCursorPos(&pt);
		ScreenToClient(hWnd, &pt);

		int MouseX = pt.x;
		int MouseY = pt.y;
		mouseClickPixX = MouseX;
		mouseClickPixY = MouseY;

		double range = sqrt(pow((mouseClickLocation.y - Cam.m_Position.y - m_CenterPointPosition->getLatDecDeg())*estLatToMConv, 2.0) + pow((mouseClickLocation.x - Cam.m_Position.x - m_CenterPointPosition->getLonDecDeg())*estLonToMConv, 2.0) + pow((double)(mouseClickLocation.z - Cam.m_Position.z - m_CenterPointPosition->getAltMslM()), 2.0));
		mouseClickRangeToCamera = range;
		
		return true;
	}
	else
	{
		return false;
	}
}

void Viewer3D::CheckZoom() 
{
	POINT pt;
	GetCursorPos(&pt);
	ScreenToClient(hWnd, &pt);

	int MouseX = pt.x;
	int MouseY = pt.y;

	int verticalShift = (MouseY - CenterY)*100* (screenCenterRangeToCamera/7000);
	double zOffset = -verticalShift;

	glCamera newCamera = Cam;
	newCamera.calcFromOffsets (0, 0, zOffset, estLatToMConv, estLonToMConv);

	if (forceCameraAboveGround (newCamera))
		Cam = newCamera;
	ShowCursorPos();
	
	CenterX = MouseX;
	CenterY = MouseY;
}

void Viewer3D::zoomIn()
{
	int verticalShift = 250* (screenCenterRangeToCamera/900);
	double zOffset = -verticalShift;

	glCamera newCamera = Cam;
	newCamera.calcFromOffsets (0, 0, zOffset, estLatToMConv, estLonToMConv);
	
	if (forceCameraAboveGround (newCamera))
		Cam = newCamera;
	
	ShowCursorPos();
}

void Viewer3D::zoomOut()
{
	int verticalShift = -250* (screenCenterRangeToCamera/900);
	double zOffset = -verticalShift;

	glCamera newCamera = Cam;
	newCamera.calcFromOffsets (0, 0, zOffset, estLatToMConv, estLonToMConv);

	if (forceCameraAboveGround (newCamera))
		Cam = newCamera;
	ShowCursorPos();
}

int m_CaptureImgWidth = 0;
int m_CaptureImgHeight = 0;
unsigned char* m_CaptureImgBytes = NULL;
bool m_VideoCaptureInitialized = false;

void Viewer3D::paint ()
{
	if (m_BeliefCollection == NULL)
	{
		Logger::getInstance()->logMessage ("No belief collection in Viewer3D::paint");
		return;
	}
	if (m_MapData == NULL)
	{
		Logger::getInstance()->logMessage ("No map data in Viewer3D::paint");
		return;
	}

	if (m_CenterPointPosition == NULL)
	{
		//Load DTED and image data in GL thread first time this is called
		DTEDObject* dted = DTEDLoader::loadDTED (m_MinDtedLatDecDeg, m_MinDtedLonDecDeg, m_MaxDtedLatDecDeg, m_MaxDtedLonDecDeg, m_DtedFileExt, m_DtedDataInterval);
		m_MapData->setDtedObject (dted);
		m_BeliefCollection->setDtedObject (dted);

		double centerAltMslM = m_MapData->getTerrainZM (m_CameraStartLatDecDeg, m_CameraStartLonDecDeg);
		estLonToMConv = DEG2M*cos(m_CameraStartLatDecDeg*DEG2RAD);

		if (m_CenterPointPosition == NULL)
			m_CenterPointPosition = new AgentPosition (0, m_CameraStartLatDecDeg, m_CameraStartLonDecDeg, centerAltMslM, 0);

		m_MapData->loadMap (m_MapImageFilename, m_CenterPointPosition);
		m_MapData->loadBackground (m_BkgndMapImageFilename, m_CenterPointPosition);

		setDefaultCameraView ();
	
		
	}

	makeCurrent();

	//Notify that GL thread is up and ready to go
	m_GLReady = true;
	
	setClearColor ();
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);			// Clear The Screen And The Depth Buffer
	glLoadIdentity();		// Reset The View


	glPushMatrix();
	glRotatef (Cam.m_PitchDegrees, 1.0, 0, 0.0);
	glRotatef (Cam.m_HeadingDegrees, 0, 0.0, 1.0);
	glTranslatef (-Cam.m_Position.x*estLonToMConv, -Cam.m_Position.y*estLatToMConv, -Cam.m_Position.z);
	glColor3f( 1.0f, 1.0f, 1.0f );

	glEnable(GL_COLOR_MATERIAL);
	GLfloat mat_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	GLfloat mat_specular[] = { 0.5f, 0.5f, 0.5f, 1.0f };
	glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE, mat_diffuse);
	glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat_specular);

	//Draw background map/dted
	if (m_MapData != NULL)
	{
		glPushMatrix();
		
		glColor3f( 1.0f, 1.0f, 1.0f );
		m_MapData->paintMap (m_CenterPointPosition, estLatToMConv, estLonToMConv);

		glPopMatrix();
	}

	//Update mouse position while only thing displayed is background map.  Otherwise, other overlays
	//screw up the unproject method
	POINT mousepos;
	GetCursorPos(&mousepos);
	ScreenToClient(hWnd, &mousepos);
	float MouseX = (float)mousepos.x;
	float MouseY = (float)mousepos.y;
	getScreenToTerrain (MouseX, MouseY, mouseLatDD, mouseLngDD, mouseTerrainAltM, mouseOnGround);
	updateScreenCenterLocation ();






	//Wacs specific display stuff
	if (m_BeliefCollection != NULL)
	{
		m_BeliefCollection->drawGL (m_CenterPointPosition, estLatToMConv, estLonToMConv, m_DisplayShadowOpt, m_DisplayGcsOpt, m_DisplayOrbitOpt, m_DisplayParticlesOpt, m_DisplayChemicalOpt, m_DisplayPicTelForShadowOpt, m_DisplayPredictedCloudOpt, m_DisplayExplosionOpt, m_DisplayMetBeliefOpt, m_DisplayIrExplosionAlgorithmEnabledBeliefOpt, m_DisplayPropogatedDetectionsOpt, m_DisplayLoiterApproachPathOpt);
	}

	glPopMatrix();

	//Draw WACS specific overlays
	if (m_BeliefCollection != NULL)
	{
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho (0, winW, 0, winH, -winW, winW);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		m_BeliefCollection->setWindowSize (winW, winH);
		m_BeliefCollection->setCameraAngles (Cam.m_PitchDegrees, Cam.m_HeadingDegrees);
		m_BeliefCollection->drawGlOverlay(m_DisplayShadowOpt, m_DisplayGcsOpt, m_DisplayOrbitOpt, m_DisplayParticlesOpt, m_DisplayChemicalOpt, m_DisplayPicTelForShadowOpt, m_DisplayPredictedCloudOpt, m_DisplayExplosionOpt, m_DisplayMetBeliefOpt, m_DisplayIrExplosionAlgorithmEnabledBeliefOpt, m_DrawSensorOverlays, m_ConvertAltitudeToFeet);
	
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
	
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
	}

	
	SwapBuffers(hDC);			// Swap Buffers (Double Buffering)


	



	if (m_EnableVideoCapture)
	{
		//First time, initialize the video capture recording
		if (!m_VideoCaptureInitialized)
		{
			m_VideoCaptureInitialized = true;

			//setup buffer to copy pixels into
			int actWidth = winW + (4-(winW%4));
			int actHeight = winH + (4-(winH%4));
			if (m_CaptureImgWidth != actWidth || m_CaptureImgHeight != actHeight)
			{
				if (m_CaptureImgBytes != NULL)
					delete [] m_CaptureImgBytes;
				m_CaptureImgWidth = actWidth;
				m_CaptureImgHeight = actHeight;
				m_CaptureImgBytes = new unsigned char [3*(m_CaptureImgWidth)*(m_CaptureImgHeight)];
			}
			glReadBuffer (GL_BACK);

			//start thread for buffering and sending to java
			VideoCapture_StartThread();
		}

		//copy pixels from view into local buffer
		glReadPixels (0, 0, m_CaptureImgWidth, m_CaptureImgHeight, GL_RGB, GL_UNSIGNED_BYTE, m_CaptureImgBytes);

		//copy local buffer into external buffer
		VideoCapture_CapturePixels (m_CaptureImgWidth, m_CaptureImgHeight, m_CaptureImgBytes);
	}

	




	/*if (!init)
	{
		init = true;
		char szXvidFileName[MAX_PATH];
		sprintf_s (szXvidFileName, 100, "sampleVid\0");
		strcat(szXvidFileName, ".avi");
		InitEncoding(szXvidFileName, actWidth, actHeight);
	}
	count ++;

	if (count < 100)
	{
		EncodeFrame((unsigned char*)imageBytes);
	}
	else
	{
		TermiateEncoding ();
	}*/




	/*count ++;
	char filename [256];
	sprintf_s (filename, 256, "image_%d.pgm", count);
	ofstream output (filename);
	output << "P2" << endl << actWidth << endl << actHeight << endl << 255 << endl;
	for (int h = actHeight-1; h >= 0; h --)
	{
		for (int w = 0; w < actWidth; w ++)
		{
			output << (w>0?" ":"") << (int)(imageBytes[h*(imgWidth+1)*3 + w*3]);
		}
		output << endl;
	}
	output.close();*/




	/*count ++;
	char filename [256];
	sprintf_s (filename, 256, "image_%d.raw", count);
	ofstream output (filename, std::ios_base::binary);
	output.write ((const char*)&actWidth, sizeof(actWidth));
	output.write ((const char*)&actHeight, sizeof(actHeight));
	//output << actWidth << actHeight;
	for (int h = 0; h < actHeight; h ++)
	{
		for (int w = 0; w < actWidth; w ++)
		{
			output.write ((char*)&imageBytes[h*(imgWidth+1)*3 + w*3], 1);
			output.write ((char*)&imageBytes[h*(imgWidth+1)*3 + w*3 +1], 1);
			output.write ((char*)&imageBytes[h*(imgWidth+1)*3 + w*3 +2], 1);
		}
	}
	output.close();*/
}

void Viewer3D::setClearColor ()
{
	time_t rawtime;
	struct tm * timeinfo;

	time ( &rawtime );
	timeinfo = localtime (&rawtime);

	//If between 6AM and 6PM, use blue sky.  Otherwise, use black sky
	if (timeinfo->tm_hour > 6 && timeinfo->tm_hour < 18)
		glClearColor (.07, .71, .96, 1);
	else
		glClearColor (0, 0, 0, 1);
}

void Viewer3D::freezeViews (bool freeze)
{
	freezeView = freeze;
}

bool Viewer3D::makeCurrent ()
{
	for (int i = 0; i < 10; i ++)
	{
		if(wglMakeCurrent(hDC, hRC))
			break;

		Sleep (5);
		if (i == 9)
		{				// Try To Activate The Rendering Context
			int err = GetLastError ();
			MessageBox(NULL,"Can't Activate The GL Rendering Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
			return false;							// Return FALSE
		}
	}
	if (needReset)
		setUpGL();
	return true;
}

void Viewer3D::resizeGL(int newW, int newH)
{
	winW = newW;
	winH = newH;
	needReset = true;
}

PIXELFORMATDESCRIPTOR Viewer3D::pfdGlobalInit ()
{
	PIXELFORMATDESCRIPTOR pfd =	// pfd Tells Windows How We Want Things To Be
			{
				sizeof(PIXELFORMATDESCRIPTOR),	// Size Of This Pixel Format Descriptor
				1,								// Version Number
				PFD_DRAW_TO_WINDOW |			// Format Must Support Window
				PFD_SUPPORT_OPENGL |			// Format Must Support OpenGL
				PFD_DOUBLEBUFFER,				// Must Support Double Buffering
				PFD_TYPE_RGBA,					// Request An RGBA Format
				32,							// Select Our Color Depth
				0, 0, 0, 0, 0, 0,				// Color Bits Ignored
				0,								// No Alpha Buffer
				0,								// Shift Bit Ignored
				0,								// No Accumulation Buffer
				0, 0, 0, 0,						// Accumulation Bits Ignored
				16,								// 16Bit Z-Buffer (Depth Buffer)
				0,								// No Stencil Buffer
				0,								// No Auxiliary Buffer
				PFD_MAIN_PLANE,					// Main Drawing Layer
				0,								// Reserved
				0, 0, 0							// Layer Masks Ignored
			};
	return pfd;
}

bool Viewer3D::setupContexts()
{
	PIXELFORMATDESCRIPTOR pfd = pfdGlobalInit ();

	if (!(hDC = GetDC(hWnd)))	{			// Did We Get A Device Context?
		MessageBox(NULL,"Can't Create A GL Device Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return false;
	}
	unsigned int PixelFormat;			// Holds The Results After Searching For A Match
	if (!(PixelFormat = ChoosePixelFormat(hDC,&pfd))) {				// Did Windows Find A Matching Pixel Format?
		MessageBox(NULL,"Can't Find A Suitable PixelFormat.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return false;
	}

	if(!SetPixelFormat(hDC, PixelFormat, &pfd)) {				// Are We Able To Set The Pixel Format?
		MessageBox(NULL,"Can't Set The PixelFormat.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return false;
	}

	if (!(hRC = wglCreateContext(hDC))) {		// Are We Able To Get A Rendering Context?
		MessageBox(NULL,"Can't Create A GL Rendering Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return false;
	}

	return true;
}


LRESULT CALLBACK Viewer3D::wndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	Viewer3D *viewBasic = NULL;
	if (msg == WM_NCCREATE)
	{
		try {
 			viewBasic = new Viewer3D();
			SetWindowLongPtr (hwnd, GWLP_USERDATA, (LONG_PTR)viewBasic);
			viewBasic->setHWnd( hwnd);
			viewBasic->clearContexts();


		}
		catch (...)
		{
			MessageBox (NULL, "Error extracting GLView type", "Error", MB_OK|MB_ICONEXCLAMATION);
		}
		
	}
	else
	{
		LONG_PTR lptr = GetWindowLongPtr (hwnd, GWLP_USERDATA);
		viewBasic = reinterpret_cast<Viewer3D*> ((LPVOID)lptr);
	}


	Viewer3D *view = (Viewer3D*) viewBasic;


	int newWidth, newHeight;
	bool rightMessage = false;
	switch(msg)
    {
	case WM_NCCREATE:
        
		if (!view->setupContexts())
			return false;
		if (!view->makeCurrent ( ))
			return false;
		
		view->initGL();
		view->setUpGL();
	
		// Continue with window creation.
        return TRUE;

    case WM_NCDESTROY:
		delete view;
        break;

	case RESET_VIEWS:
		view->setDefaultCameraView();
		break;

	// Draw the control
	case WM_PAINT:
		return onPaint(view, wParam, lParam);
	case MY_PAINT:
		return onPaint(view, wParam, lParam);
	
	case WM_SETFOCUS:
		mainWinHasFocus = true;
		break;
	case WM_KILLFOCUS:
		mainWinHasFocus = false;
		break;
	
	//// Prevent flicker
	case WM_ERASEBKGND:
		return 1;

	case WM_SIZE:
		newWidth = LOWORD(lParam);
		newHeight = HIWORD(lParam);
		view->resizeGL (newWidth, newHeight);
		break;

	case WM_MOUSEWHEEL: 
		
		if (!view->makeCurrent())
			return false;
		
		if ((signed short)HIWORD(wParam) > 0)
			view->zoomIn();
		else if ((signed short)HIWORD(wParam) < 0)
			view->zoomOut();

		break;
	case WM_LBUTTONDBLCLK:

		if (!view->makeCurrent())
			return false;
		
		view->zoomIn();
		break;

	case WM_RBUTTONDBLCLK:

		if (!view->makeCurrent())
			return false;
		
		view->zoomOut();
		break;

	case WM_RBUTTONUP:
		rightMessage = true;
	case WM_LBUTTONUP: 
		view->mouseInUse = false;

		if (!view->makeCurrent())
			return false;
		
		if (!rightMessage)
		{
			view->leftClickDown = false;		
			view->dragged = true;

			//stop rotation
			view->rotationCenterLocationValid = false;
		}
		break;


	case WM_RBUTTONDOWN:
		rightMessage = true;
	case WM_LBUTTONDOWN: 
		{

		SetFocus (hwnd);
		view->dragged = false;
		if (!rightMessage)
		{
			view->leftClickDown = true;
			view->dragged = true;
		}
		
		
		if (!view->makeCurrent())
			return false;
		
		view->mouseInUse = true;
		view->mouseInUseWnd = hwnd;
		POINT pt;
		GetCursorPos(&pt);
		ScreenToClient(hwnd, &pt);

		CenterX = pt.x;
		CenterY = pt.y;

		short shiftkey = GetKeyState(VK_SHIFT);
		view->localCamera = (shiftkey < 0);

		short ctrlkey = GetKeyState (VK_CONTROL);
		view->centerRotation = (ctrlkey < 0);

		if (!view->mouseOnGround)
			view->localCamera = true;

		//stop any old rotation
		view->rotationCenterLocationValid = false;
	
		
		break;
		}
	case WM_MOUSEMOVE:
		
		view->dragged = true;
		if (freezeView)
			break;

		if (!view->makeCurrent())
			return false;
		
		view->updateStatus();

		if (view->mouseInUse && view->mouseInUseWnd != hwnd)
			return 0;

		if (Viewer3D::mainWinHasFocus)
			SetFocus(hwnd);
		
		if ((wParam&MK_LBUTTON) && (wParam&MK_RBUTTON)) {
			view->CheckZoom();
			
		} else if (wParam&MK_LBUTTON) {
			view->CheckMouseOrient();
			
		} else if (wParam&MK_RBUTTON) {
			view->CheckMousePos();
			
		} else {
			view->ShowCursorPos();
		}

		return 0;
	
	case UPDATE_WINDOW:
		view->needReset = true;
		SendMessage (hwnd, WM_PAINT, 0, 0);
		return 1;

	default:
        break;
    }

    return DefWindowProc(hwnd, msg, wParam, lParam);
}

LRESULT Viewer3D::onPaint(Viewer3D *view, WPARAM wParam, LPARAM lParam)
{
	if (freezeView)
		return true;
	
	view->paint();
	
	return 0;
}
