#ifndef THREEDVIEWER_H
#define THREEDVIEWER_H

#include "stdafx.h"
#include "ConfirmDialog.h"
#include "RawDataDialog.h"

#include "BeliefCollection.h"
#include "SwarmSpyHandler.h"

class ThreeDViewer;

/**
	\struct ThreadParams
	\brief Parameters to pass into a thread to allow it to function and end gracefully
	\author John Humphreys
	\date 2010
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
struct ThreadParams
{
	/**
	\brief Default constructor, does nothing
	*/
	ThreadParams () {}

	ThreeDViewer* gui; //!< Pointer to the viewer GUI that created the thread so the GUI can be used
	bool* m_KillThread; //!< If true, this thread should end gracefully.  Set externally
	bool* m_ThreadDone; //!< If true, this thread has ended gracefully.  Set internally
};

/**
	\class ThreeDViewer
	\brief Main class for 3D Viewer application.  Instantiating the class and calling run() on
	the object will start the application and everything else is taken care of.

	\author John Humphreys
	\date 2010
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class ThreeDViewer {
	
	public:
		/**
		\brief Constructor, sets default parameters and stores the HINSTANCE parameter
		\param hInstance HINSTANCE parameter received at application startup
		\return none
		*/
		ThreeDViewer (HINSTANCE hInstance);

		/**
		\brief Destructor
		*/
		~ThreeDViewer ();
		
		/**
		\brief Main processing method.  Once called, the ThreeDViewer application will load and 
		be fully functional.
		\return void
		*/
		void run ();

	private:
		
		//General variables/functions required for 3D viewer

		/**
		\brief HWND object for this window
		*/
		HWND hWnd;	

		/**
		\brief HINSTANCE object for the application
		*/
		HINSTANCE hInst;

		/**
		\brief handle for 3D view window
		*/
		HWND MapViewWnd;

		/**
		\brief Dialog window that will ask for user confirmation before performing a certain action
		*/
		ConfirmDialog *myConfirmDialog;

		/**
		 Thread function to update OpenGL
		 */
		static void glPaintThread (void* params);

		/**
		Handle to the GL paint thread
		*/
		HANDLE hGLPaintThread;

		/**
		If true, request has been sent to end the gl thread
		*/
		bool m_KillGLThread;

		/**
		If true, the gl thread has completed gracefully
		*/
		bool m_GLThreadDone;

		/**
		\brief Framerate of OpenGL refresh, seconds.  Slow down if too much is being rendered
		*/
		float frameRate;

		/**
		\brief If true, the main window has been fully loaded - Constructor completed
		*/
		bool windowLoaded;
		
		/**
		\brief Title bar text
		*/
		TCHAR szTitle[MAX_LOADSTRING];

		/**
		\brief Main window class name
		*/
		TCHAR szWindowClass[MAX_LOADSTRING];
		
		/**
		\brief Called from constructor to initialize the window and begin custructing the application
		\return void
		*/
		void initDialog();

		/**
		\brief Called from initDialog to actually create the window and space all components throughout.  Also
		creates other necessary objects for program execution.
		\return Boolean status of dialog creation
		*/
		bool createDialog () ;

		/**
		\brief Runs a loop reading messages received for the application (NOT network messages, they are 
		WINDOWS messages) and updates the application as necessary.  The OpenGL scenes are updated in this loop
		\return void
		*/
		void runMsgLoop();
		
		/**
		\brief Registers the main window class so that Windows can instantiate the object
		\return ATOM
		*/
		ATOM MyRegisterClass();

		/**
		\brief Activate the entire window, seems to refresh everything as if the program
		just started
		\return void
		*/
		void activate();

		/**
		\brief Updates all windows and repaints everything
		\return void
		*/
		void updateWindows();
		
		/**
		\brief Load objects that are required immediately before the main dialog is created and 
		also perform any necessary updates/initializations
		\return void
		*/
		void preLoadObjects();
		
		/**
		\brief Load objects that are required immediately after the main dialog is created and 
		also perform any necessary updates/initializations
		\return void
		*/
		void postLoadObjects();
		
		/**
		\brief The user has chosen to close the window and shut down, but this version is safer because it
		requires approval from the ConfirmDialog window

		\param approved If true, the user has already approved this action, false otherwise
		\return void
		*/
		void exit(bool approved = false);

		/**
		\brief If true, the dialog is being destroyed
		*/
		static bool beingDestroyed;

		/**
		\brief Should be called after the window has been resized so that all inner components
		will be scaled and moved as necessary to have all objects shown
		\return void
		*/
		void resizeWindow();

		/**
		\brief Sets the minimum size for the window based on how many buttons must fit in each direction

		\param info MINMAXINFO object to hold the minimum size
		\return void
		*/
		void setMinMaxInfo(MINMAXINFO* info);

		
		/**
		\brief Main window message processing function
		\param hWnd HWND handle of object receiving the message
		\param message Message being received
		\param wParam WPARAM parameter for message
		\param lParam LPARAM parameter for message
		\return Status of message processing
		*/
		static LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);
		
		/**
		\brief Non-static Main window message processing function
		\param hWnd HWND handle of object receiving the message
		\param message Message being received
		\param wParam WPARAM parameter for message
		\param lParam LPARAM parameter for message
		\return Status of message processing
		*/
		LRESULT processMsg (HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);


		/**
		\brief Called once to determine how the window should be laid out, based on the resource file.  Will attempt to maintain the 
		layout during resizes based on the values computed here.
		*/
		void setWindowPreferredSizes ();

		/**
		\brief Called when user wants to reset the camera in the main view
		*/
		void resetCamera ();
 
		
		/**
		\brief Window width of entire window - excluding border sizes
		*/
		int wndWidth;
		
		/**
		\brief Window height of entire window - excluding border sizes
		*/
		int wndHeight;

		/**
		\brief Minimum window width of entire window
		*/
		int minWidth;
		
		/**
		\brief Minimum window height of entire window
		*/
		int minHeight;

		/**
		\brief Boolean whether the window needs to resize its components.  True when WM_SIZE message is received
		*/
		bool winNeedsResize;


		/**
		\brief Preferred distance of the left edge of the main 3D window to the left edge of the entire window, when side bar is shown
		*/
		int mapPrefDistFromLeft;
		
		/**
		\brief Preferred distance of the top edge of the main 3D window to the top edge of the entire window
		*/
		int mapPrefDistFromTop;
		
		/**
		\brief Preferred distance of the right edge of the main 3D window to the right edge of the entire window
		*/
		int mapPrefDistFromRight;
		
		/**
		\brief Preferred distance of the bottom edge of the main 3D window to the bottom edge of the entire window
		*/
		int mapPrefDistFromBottom;

		/**
		\brief Apparent height of the border at the top of the window
		*/
		int topBuffer;

		/**
		\brief Apparent width of the border at the sides of the window
		*/
		int sideBuffer;

		/**
		\brief Preferred distance from right edge of window for mouse label
		*/
		int mouseLabelPrefDistFromRight;
		
		/**
		\brief Preferred distance from bottom edge of window for mouse label
		*/
		int mouseLabelPrefDistFromBottom;
		
		/**
		\brief Preferred width of window for mouse label
		*/
		int mouseLabelPrefWidth;
		
		/**
		\brief Preferred height of window for mouse label
		*/
		int mouseLabelPrefHeight;

		/**
		\brief Preferred distance from left edge of window for mouse output
		*/
		int mouseOutputPrefDistFromRight;
		
		/**
		\brief Preferred distance from bottom edge of window for mouse output
		*/
		int mouseOutputPrefDistFromBottom;
		
		/**
		\brief Preferred width of window for mouse output
		*/
		int mouseOutputPrefWidth;
		
		/**
		\brief Preferred height of window for mouse output
		*/
		int mouseOutputPrefHeight;

		/**
		\brief Default amount of space used in the status bar.  Used to resize.
		*/
		int usedStatusSpace;

		/**
		\brief Default amount of clear space used in the status bar.  Used to resize.
		*/
		int clearStatusSpace;

		/**
		\brief Params to send to gl paint thread
		*/
		ThreadParams* glThreadParams;







		//WACS Specific classes

		BeliefCollection * m_BeliefCollection; //!< BeliefCollection object storing all of the current beliefs from the WACS Swarm network

		bool m_DoSocketSpy; //!< If true, SwarmSpy should be initialized to connect to the multicast port.  If false, should replay log data from file.
		bool m_SwarmSpyInitialized; //!< If true, SwarmSpy has been initialized and should be running.
		SwarmSpyHandler* m_SwarmSpyHandler; //!< Handler for SwarmSpy messages.  Updates the BeliefCollection object

		bool m_PauseSpyReplay; //!< If true, SwarmSpy replay mode is paused.

		/**
		\brief Initialize SwarmSpy connection and start threads to update current belief list
		\return void
		*/
		void initSwarmSpy (); 
		

		/**
		\brief Dialog window with raw data display
		*/
		RawDataDialog *myRawDataDialog;

		/**
		 \brief Thread function to update raw data tables
		 */
		static void rawDataUpdateThread (void* params);

		/**
		\brief Handle to the raw data updater thread
		*/
		HANDLE hRawDataUpdateThread;

		/**
		\brief If true, request has been sent to end the raw data updater  thread
		*/
		bool m_KillRawUpdateThread;

		/**
		\brief If true, the raw data updater  thread has completed gracefully
		*/
		bool m_RawUpdateThreadDone;

		/**
		\brief Framerate of raw data updater thread, seconds.  Slow down if updated too quickly.
		*/
		float frameRateRawUpdate;
		
		/**
		\brief If true, the raw data dialog needs to be updated as soon as possible.  Currently, this is done
		when the next message is received to keep the updates in the main thread.  A paint message should
		be generated when this flag is set to true to ensure a message is received quickly.
		*/
		bool updateRawDataNext;

		/**
		\brief Params to send to raw data update thread
		*/
		ThreadParams* rawUpdateThreadParams;
		
		/**
		\brief Update data is raw dialog.  Uses BeliefCollection to update tables
		\return void
		*/
		void updateRawDataDialog ();

		
		
};

#endif
