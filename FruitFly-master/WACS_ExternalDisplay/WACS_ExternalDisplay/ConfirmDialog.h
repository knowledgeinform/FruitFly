#ifndef CONFIRMDIALOG_H
#define CONFIRMDIALOG_H

#include "stdafx.h"
#include "DialogBase.h"

#define EXITPROGRAM 0x0401

/**
	\class ConfirmDialog
	\brief Class to present a dialog that will force the user to confirm an action.  Keeps user from
	making stupid mistakes without knowing it
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/

class ConfirmDialog : public DialogBase
{
	public:

		/**
		\brief Constructor, essentially does nothing
		
		\param newHinst Instance of application
		\param newhParent Parent window
		\return none
		*/
		ConfirmDialog(HINSTANCE newHinst, HWND newhParent) ;

		/**
		\brief Destructor
		*/
		~ConfirmDialog ();

		/**
		\brief Static message processing function that receives all messages for all instances of the
		class.  Locates object based on hDlg parameters and passes the command to the object for processing

		\param hDlg HWND parameter for the object to receive the message
		\param message command to be processed
		\param wParam WPARAM for message
		\param lParam LPARAM for message
		\return LRESULT - status of message processing
		*/
		static LRESULT CALLBACK processMsg(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam);
		
		/**
		\brief Non-static message processing function that receives all messages for all instances of the
		class.  Locates object based on hDlg parameters and passes the command to the object for processing

		\param hDlg HWND parameter for the object to receive the message
		\param message command to be processed
		\param wParam WPARAM for message
		\param lParam LPARAM for message
		\return LRESULT - status of message processing
		*/
		LRESULT processMessage(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam);

		/**
		\brief Function to call to make this dialog visible and useful.  After instantiation, dialog is still
		hidden and show() must be called to raise the dialog box.  May be called again to re-show dialog box if
		it has been closed

		\return bool - Boolean status of operation to show dialog box
		*/
		bool show ();

		/**
		\brief Notify this window which type of confirmation it is asking for

		\param newType Value corresponding to a type of confirmation message
		\return void
		*/
		void setType (int newType);

		/**
		\brief Returns whether the specified window is in this dialog.  Used to determine how to handle messages
		\param hwnd Window to check for
		\return bool - True if this dialog contiains the window, false otherwise
		*/
		bool containsWindow (HWND hwnd);

	private:		

		/**
		\brief The text to ask the user to confirm
		*/
		char text [100];

		/**
		\brief A value corresponding to the text the user must confirm
		*/
		int type;

		/**
		\brief Amount from the center of the parent window to offset this window so it looks centered
		*/
		int offsetXFromCenter;
};

#endif