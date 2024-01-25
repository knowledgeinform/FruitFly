#ifndef VIEWER3D_H
#define VIEWER3D_H

#include "stdafx.h"
#include "glCamera.h"
#include "BeliefCollection.h"
#include "MapData.h"

/**
	\class Viewer3D
	\brief Presents a 3D OpenGL window scene.  The camera viewpoint for this object
	is a 3D world - the camera can be moved freely through space and can look in any direction
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class Viewer3D
{
	public:
		/**
		\brief Constructor, read config and set-up parameters
		\return none
		*/
		Viewer3D ();

		/**
		\brief Destructor
		*/
		~Viewer3D ();

		/**
		\brief Function to register the class so that the class can be instantiated easily later.  Should 
		only be called once and is done automatically
		\return void
		*/
		static void initViewer3D();

		/**
		\brief Resets camera to default position in config file
		\return void
		*/
		void setDefaultCameraView ();

		/**
		\brief Function that contains the OpenGL rendering code, called from the message
		processing loop
		\return void
		*/
		void paint ();

		/**
		\brief Function to update data shown in the status window.  Mouse position currently shown
		\return void
		*/
		void updateStatus();

		/**
		\brief Object to represent the viewing camera in the OpenGL scene
		*/
		static glCamera Cam;

		/**
		\brief Mutator function for hWnd
		\param newHwnd new value to set hWnd to
		\return void
		*/
		void setHWnd (HWND newHwnd) {hWnd = newHwnd;}

		/**
		\brief Accessor function for hWnd
		\return HWND value of hWnd
		*/
		HWND getHWnd () {return hWnd;}

		/**
		\brief Clears the values for hDC and hRC
		\return void
		*/
		void clearContexts();

		/**
		\brief Initialize contexts and formatting for the OpenGL scene - must be called before any OpenGL commands
		\return bool - Boolean status of set-up process
		*/
		bool setupContexts();

		/**
		\brief Global initialization function for a PIXELFORMATDESCRIPTOR.
		\return PIXELFORMATDESCRIPTOR - Fully populated PIXELFORMATDESCRIPTOR object
		*/
		static PIXELFORMATDESCRIPTOR pfdGlobalInit();

		/**
		\brief Resize OpenGL scene based on the new dimensions of the window
		\param newW New width of the window
		\param newH New height of the window
		\return void
		*/
		void resizeGL(int newW, int newH);

		/**
		\brief Window to update when mouse moves
		*/
		static HWND mouseOutputWnd;
		
		/**
		\brief Map/terrain data to display in the 3D view.  Has DTED elevation data and map imagery.
		*/
		MapData* m_MapData;


		/**
		\brief Converts from lat degrees to meters, estimate
		*/
		double estLatToMConv;

		/**
		\brief Converts from lon degrees to meters, estimate
		*/
		double estLonToMConv;

		/**
		\brief Toggles whether all view is temporarily restricted from updating - used to prevent
		flash when the menu is being used
		\param freeze If true, views will be frozen.  If false, they are unfrozen
		\return void
		*/
		static void freezeViews(bool freeze = true);

		/**
		If true, the main window has mouse focus
		*/
		static bool mainWinHasFocus;





		//WACS specific class for display of data

		static BeliefCollection* m_BeliefCollection; //!< BeliefCollection object of all current beliefs to be displayed

		static bool m_DisplayShadowOpt; //!< If true, paint the Shadow UAV
		static bool m_DisplayPicTelForShadowOpt; //!< If true, use Piccolo telemetry to display UAV.  If false, use AgentPositionBelief to display UAV.
		static bool m_DisplayOrbitOpt; //!< If true, display circular orbit belief
		static bool m_DisplayGcsOpt; //!< If true, display GCS position
		static bool m_DisplayParticlesOpt; //!< If true, display particle detections
		static bool m_DisplayChemicalOpt; //!< If true, display chemical detections
		static bool m_DisplayPredictedCloudOpt; //!< If true, display predicted cloud location
		static bool m_DisplayExplosionOpt; //!< If true, display explosion indicator
		static bool m_DisplayMetBeliefOpt; //!< If true, display MET data
		static bool m_DisplayIrExplosionAlgorithmEnabledBeliefOpt; //!< If true, display time until explosion indicator
		static bool m_DrawSensorOverlays; //!< If true, display sensor overlays
		static bool m_DisplayPropogatedDetectionsOpt; //!< If true, display propogated cloud detections
		static bool m_PauseDetectionPropogation; //!< If true, pause the propogated cloud detections
		static bool m_ConvertAltitudeToFeet; //!< If true, convert altitude to feet in main window
		static bool m_GLReady; //!< If true, OpenGL is loaded and updating.
		static bool m_DisplayLoiterApproachPathOpt; //!< If true, display loiter approach path
		static bool m_EnableVideoCapture;  //!< If true, current OpenGL view is being captured for video replay
		static bool Viewer3D::m_VideoCaptureInitialized; //!< If true, video replay capture has been activated and will need to be shut down gracefully



	private:

		/**
		\brief HWND object for this GLView window
		*/
		HWND hWnd;

		/**
		\brief Rendering context for this GLView window
		*/
		HGLRC hRC;

		/**
		\brief Drawing context for this GLView window
		*/
		HDC hDC;

		/**
		\brief If true, the OpenGL scene needs to be reinitialized
		*/
		bool needReset;

		/**
		\brief Vertical FOV degrees of OpenGL window
		*/
		double m_GlHeightFOVDeg;

		/**
		\brief Window width for this object
		*/
		int winW;

		/**
		\brief Window height for this object
		*/
		int winH;

		/**
		\brief If true, the user clicked and dragged the mouse.  False otherwise
		*/
		bool dragged;

		/**
		\brief When the mouse has been clicked in a GLView object, the last window-X position of the mouse
		*/
		static int CenterX;

		/**
		\brief When the mouse has been clicked in a GLView object, the last window-Y position of the mouse
		*/
		static int CenterY;

		/**
		\brief Static value that when true, signifies that a mouse button is clicked and is dragging through a GLView object
		*/
		static bool mouseInUse;

		/**
		\brief Static value that when mouseInUse is true, corresponds to the window that the mouse is affecting
		*/
		static HWND mouseInUseWnd;


		/**
		\brief Orthographic scale factor
		*/
		double orthoscale;

		/**
		\brief Inverse of the orthographic scale factor
		*/
		double invorthoscale;

		/**
		\brief Character to start loading character set from
		*/
		unsigned int minFont;

		/**
		\brief Character to stop loading character set at
		*/
		unsigned int maxFont;

		/**
		\brief Depth into screen to move camera.  Eliminates some complexities with previous zoom function
		*/
		GLfloat	screenz;


		/**
		\brief Read config settings from file and set parameters
		\return void
		*/
		void readConfig ();

		/**
		\brief Overridden function that contains the OpenGL initialization code
		\return void
		*/
		void initGL();

		/**
		\brief Function containing the OpenGL viewpoint initialization code
		\return void
		*/
		void setUpGL();

		/**
		\brief Function to raise the HDC and RDC contexts for this object so that OpenGL calls will be 
		made for this object.  Must be called before any OpenGL calls (for safety, I guess)
		\return Boolean status of context activations
		*/
		bool makeCurrent ();

		/**
		\brief Sets the background clear color for the OpenGL scene.  Dependent on time of day.
		\return void
		*/
		void setClearColor ();

		/**
		\brief Forces camera above terrain level.  Actually just determines whether the requested camera 
		position is above ground or not.  Doesn't move the camera.
		\param camera Proposed (not currently set) camera position
		\return True if proposed camera position is above ground.  False if underground.
		*/
		bool forceCameraAboveGround (glCamera& camera);

		/**
		\brief Function called when user chooses to zoom in to the window via a double-click/drag.  Updates 
		orthoscale for next	paint() call
		\return void
		*/
		void zoomIn();

		/**
		\brief Function called when user chooses to zoom out of the window via a double-click/drag.  Updates 
		orthoscale for next	paint() call
		\return void
		*/
		void zoomOut();

		/**
		\brief Determines the position of the mouse on screen and saves values into mouseLatDD and mouseLngDD
		\return void
		*/
		void ShowCursorPos();

		/**
		\brief Calculate global coordinates based on screen pixel coordinate
		\param screenX X pixel of coordinate to find global coordinate at
		\param screenY Y pixel of coordinate to find global coordinate at
		\param screenLat After return, filled with latitude value of global coordinate of given pixel
		\param screenLon After return, filled with longitude value of global coordinate of given pixel
		\param screenZ After return, filled with terrain altitude value of global coordinate of given pixel
		\param posOnGround After return, true if the given pixel location was on the ground.  If false, reference parameters designating global coordinate are incorrect
		\return void
		*/
		void getScreenToTerrain(int screenX, int screenY, double &screenLat, double &screenLon, double &screenZ, bool &posOnGround);

		/**
		\brief Determines how much the mouse has moved since the last call to this function and drag's the 
		user's camera perspective appropriately
		\return void
		*/
		void CheckMousePos();

		/**
		\brief Determines how much the mouse has moved since the last call to this function and rotates the user's
		camera perspecetive appropriately
		\return void
		*/
		void CheckMouseOrient();
		
		/**
		\brief Update coordinates that the GL camera is looking directly at.  The global coordinate of the
		3D viewer's center pixel.  
		
		Will calculate position based on current OpenGL frame buffer, so should
		be called when terrain map is displayed in OpenGL but nothing is overlayed on top.
		\return True if determined location is on the the ground
		*/
		bool updateScreenCenterLocation ();

		/**
		\brief Copy coordinates that the user's mouse is located at.  The global coordinate of the current
		mouse pixel.

		The calculated location is based on current OpenGL frame buffer, so it should be calculated
		when the terrain map is displayed in OpenGL but nothing is overlayed on top.  The current position
		of the mouse is always calculated in the GL paint function, this function merely copies that location
		into a separate variable for storage and use.
		\return True if determined location is on the ground
		*/
		bool updateMouseClickLocation ();

		/**
		\brief Determines how much the mouse wheel has been spun since the last call and zooms the camera
		appropriately
		\return void
		*/
		void CheckZoom();

		/**
		\brief If true, the user has the left button clicked right now
		*/
		bool leftClickDown;

		
		/**
		\brief Static value to store the latitude value where the mouse is located in the screen
		*/
		static double mouseLatDD;

		/**
		\brief Static value to store the longitude value where the mouse is located in the screen
		*/
		static double mouseLngDD;

		/**
		\brief Terrain altitude, meters, where the mouse currently is
		*/
		static double mouseTerrainAltM;

		static bool mouseOnGround; //!< If true, mouse is current at a pixel that is on the ground.

		static bool centerOnGround; //!< If true, screen's center pixel is currently at a pixel that is on the ground.

		bool rotationCenterLocationValid; //!< If true, a rotation point has been selected that is on the ground.

		double screenCenterRangeToCamera; //!< Distance from camera position to ground location at screen's center pixel

		glPoint screenCenterLocation; //!< Location on the ground of the screen's center pixel (If centerOnGround is true)

		glPoint mouseClickLocation; //!< Location on the ground where the mouse was last clicked (If mouseOnGround is true);

		double mouseClickRangeToCamera; //!< Distance from camera position to ground location at last mouse click

		double betaRad; //!< Angle (in pitch direction) from screenCenterLocation to mouseClickLocation during camera rotation
		double alphaRad; //!< Angle (in heading direction) from screenCenterLocation to mouseClickLocation during camera rotation
		
		double camErrX; //!< Offset error to shift camera by when doing camera rotations.  Haven't figured out how to get rid of this error, yet.
		double camErrY; //!< Offset error to shift camera by when doing camera rotations.  Haven't figured out how to get rid of this error, yet.
		double camErrZ; //!< Offset error to shift camera by when doing camera rotations.  Should be zero, but here for consistency

		double headingFromClick; //!< Heading difference from current heading to heading when click started camera rotation.  Used to rotate camErrX,camErrY around as heading changes

		int wndWidth; //!< Window width pixels
		int wndHeight; //!< Window height pixels

		int centerPixX; //!< Center X pixel of window
		int centerPixY; //!< Center Y pixel of window
		int mouseClickPixX; //!< X Pixel where mouse was last clicked
		int mouseClickPixY; //!< Y Pixel where mouse was last clicked

		bool localCamera; //!< If true, camera rotations should be done locally.  If false, camera rotates around a ground point
		bool centerRotation; //!< If true, camera rotation should be done around screen center.  If false, camera rotation should be done around mouse click point

	
		/**
		\brief Static value storing whether this class has been registered via a call to initViewer3D
		*/
		static bool initialized;

		/**
		\brief Center position for display.  This global position is fixed at OpenGL coordinate 0,0,0.  Used to help with double/float precision of large numbers
		*/
		AgentPosition* m_CenterPointPosition;

		/**
		\brief Starting camera latitude position
		*/
		double m_CameraStartLatDecDeg;
		
		/**
		\brief Starting camera longitude position
		*/
		double m_CameraStartLonDecDeg;
		
		/**
		\brief Starting camera altitude position
		*/
		double m_CameraStartAltM;

		/**
		\brief Starting camera heading
		*/
		double m_CameraStartHeading;
		
		/**
		\brief Starting camera pitch
		*/
		double m_CameraStartPitch;

		/**
		\brief If true, OpenGL scenes will not update to prevent flicker
		*/
		static bool freezeView;

		/**
		\brief Class name
		*/
		static TCHAR szClassName[MAX_LOADSTRING];	// the GLView3D class name

		double m_MinDtedLatDecDeg; //!< Minimum DTED latitude to load
		double m_MinDtedLonDecDeg; //!< Minimum DTED longitude to load
		double m_MaxDtedLatDecDeg; //!< Maximum DTED latitude to load
		double m_MaxDtedLonDecDeg; //!< Maximum DTED longitude to load
		char m_DtedFileExt[5]; //!< DTED file extension (including dot) to load
		int m_DtedDataInterval; //!< DTED data interval to load.
		char m_MapImageFilename[256]; //!< Image filename to load
		char m_BkgndMapImageFilename[256]; //!< Background image filename to load


		/**
		\brief Static message handler for paint commands - passes control to the paint() function of the 
		specified parameter
		\param view Object to pass paint commands to
		\param wParam WPARAM object passed into the message handler
		\param lParam LPARAM object passed into the message handler
		\return Status of onPaint call
		*/
		static LRESULT onPaint(Viewer3D *view, WPARAM wParam, LPARAM lParam);
		
		/**
		\brief Static message handler for all commands for this object.  Passes control as appropriate
		to accomplish tasks after locating the appropriate GLView object.
		\param hwnd Handle to the window receiving the message
		\param msg Message being sent
		\param wParam WPARAM object passed into the message handler
		\param lParam LPARAM object passed into the message handler
		\return Status of message processing
		*/
		static LRESULT CALLBACK wndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam);
		
};


#endif