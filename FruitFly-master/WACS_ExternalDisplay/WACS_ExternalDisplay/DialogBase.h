#ifndef DIALOGBASE_H
#define DIALOGBASE_H

#include "stdafx.h"

/**
	\class DialogBase
	\brief An abstract object to be inherited to make dialog boxes for the application.  Must implement abstract
	functions as well as a static message handling procedure in children.
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class DialogBase
{
	public:
		/**
		\brief Constructor for object, must pass in pointer to the the HINSTANCE and 
		HWND parameters from the owner. 

		\param newHinst HINSTANCE parameter defined by owner
		\param newhParent HWND parameter of owner
		\return none
		*/
		DialogBase (HINSTANCE newHinst, HWND newhParent);

		/**
		\brief Destructor
		*/
		~DialogBase ();
		
		/**
		\brief Abstract function to call to make this dialog visible and useful.  After instantiation, dialog is still
		hidden and show() must be called to raise the dialog box.  May be called again to re-show dialog box if
		it has been closed

		\return bool - Boolean status of operation to show dialog box
		*/
		virtual bool show () = 0;
		
		/**
		\brief Return true if the dialog box is visible (from a call to show()), or return false
		if the dialog has never been raised or has since been closed

		\return bool - Boolean status of dialog visibility
		*/
		bool isVisible () {return visible;}

		/**
		\brief Mutator function for hWnd
		\param newHwnd new hWnd value
		\return void
		*/
		void setHWnd (HWND newHwnd) {hWnd = newHwnd;}

		/**
		\brief Accessor function for hWnd
		\return HWND hWnd
		*/
		HWND gethWnd () {return hWnd;}

		/**
		\brief Store the current window location into the specified parameters
		\param winLeft Will be loaded with the current 'left' position of the window
		\param winTop Will be loaded with the current 'top' position of the window
		\return void
		*/
		void getWindowPosition (int &winLeft, int &winTop);

		/**
		\brief Static functoin to store the specified window location into the specified parameters
		\param window Handle to the window to record window positions from
		\param winLeft Will be loaded with the current 'left' position of the window
		\param winTop Will be loaded with the current 'top' position of the window
		\return void
		*/
		static void getWindowPosition (HWND window, int &winLeft, int &winTop);

		/**
		\brief Static function to store the specified window size into the specified parameters
		\param window Handle to the window to record size positions from
		\param winWidth Will be loaded with the current width of the window
		\param winHeight Will be loaded with the current height of the window
		\return void
		*/
		static void getWindowSize (HWND window, int &winWidth, int &winHeight);
		
	protected:
		/**
		\brief Handle to this window
		*/
		HWND hWnd;

		/**
		\brief Handle to the owner's window
		*/
		HWND hParent;

		/**
		\brief Application instance
		*/
		HINSTANCE hInst;

		/**
		\brief Boolean status of whether this dialog box is visible or not
		*/
		bool visible;

		/**
		\brief Function to move this window to the specified screen coordinates
		\param winLeft 'Left' position to move window to
		\param winTop 'Top' position to move window to
		\return void
		*/
		void positionWindow (int winLeft, int winTop);

		/**
		\brief Determine if window pointed to by mywin is a valid window and is visible
		\param mywin HWND object to check status for
		\return bool Boolean status of window validity
		*/
		BOOL checkwindow(HWND mywin) ;

		/**
		\brief Abstract, non-static message processing function that must be overridden by child classes.  Receives
		parameters from static function and handles commands as necessary.

		\param hDlg HWND parameter for the message-receiving object
		\param message command to be processed
		\param wParam WPARAM for message
		\param lParam LPARAM for message
		\return bool - Boolean status of message processing
		*/
		virtual LRESULT processMessage(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam) = 0;

		// Must also implement static handler in child classes
};

#endif