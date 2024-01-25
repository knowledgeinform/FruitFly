#ifndef RAWDATADIALOG_H
#define RAWDATADIALOG_H

#include "DialogBase.h"
class BeliefCollection;
#define IDREFRESH 32767

/**
	\struct DataRows
	\brief Reference data to labels in RawDataDialog window for posting updates
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
struct DataRows
{
	HWND m_hWnd; //!< Handle to window of RawDataDialog
	int m_DataRowsCount; //!< Number of label/value box pairs
	int* m_DataRowLabels; //!< Ordered label boxes names in RawDataDialog window
	int* m_DataRowValues; //!< Ordered value boxes names in RawDataDialog window
};


/**
	\class RawDataDialog
	\brief Dialog showing raw data from data objects in static labels
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class RawDataDialog : public DialogBase
{
	public:
		/**
		\brief Constructor, essentially does nothing
		
		\param newHinst Instance of application
		\param newhParent Parent window
		\param beliefCollection BeliefCollection that will be used to update the RawDataDialog values
		\return none
		*/
		RawDataDialog  (HINSTANCE newHinst, HWND newhParent, BeliefCollection* beliefCollection);
		
		/**
		\brief Destructor
		*/
		~RawDataDialog  ();
		
		/**
		\brief Function to call to make this dialog visible and useful.  After instantiation, dialog is still
		hidden and show() must be called to raise the dialog box.  May be called again to re-show dialog box if
		it has been closed

		\return bool - Boolean status of operation to show dialog box
		*/
		bool show ();

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
		\brief Returns whether the specified window is in this dialog.  Used to determine how to handle messages
		\param hwnd Window to check for
		\return bool - True if this dialog contiains the window, false otherwise
		*/
		bool containsWindow (HWND hwnd);

		/**
		\brief Accessor for DataRows object that stores all required information to update labels with raw
		data from data objects
		\return DataRows object for this RawDataDialog window
		*/
		DataRows* getDataRows () {return m_DataRows;}
		
	private:
		

		/**
		\brief Amount from the center of the parent window to offset this window so it looks centered
		*/
		int offsetXFromCenter;

		/**
		\brief Boxes to show raw data
		*/
		DataRows* m_DataRows;

		/**
		\brief BeliefCollection use to update boxes with raw data.
		*/
		BeliefCollection* m_BeliefCollection;

		static const int m_BeliefNamesSize = 14; //!< Number of belief names in the drop-down combo box for belief selection
		static char m_BeliefNames [m_BeliefNamesSize][256]; //!< Names of beliefs that can be chosen for display of raw data		
		
		/**
		\brief Create DataRows object for this window
		\return void
		*/
		void initDataRows ();
};

#endif