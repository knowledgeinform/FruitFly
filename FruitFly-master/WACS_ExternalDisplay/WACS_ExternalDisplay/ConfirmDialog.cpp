#include "stdafx.h"
#include <iostream>
#include "ConfirmDialog.h"
#include "Resource.h"
#include "Config.h"
#include "Viewer3D.h"


ConfirmDialog::ConfirmDialog(HINSTANCE newHinst, HWND newhParent) : DialogBase (newHinst, newhParent)
{
	hWnd = NULL;

	offsetXFromCenter = Config::getInstance()->getValueAsInt ("ConfirmDialog.OffsetXFromCenter", 200);
}

ConfirmDialog::~ConfirmDialog()
{

}

bool ConfirmDialog::show ()
{
	// If window is already visible, don't try again
	if (checkwindow(hWnd)) 
		return FALSE;

	ConfirmDialog *thisPtr = this;
	hWnd = CreateDialogParam(hInst, MAKEINTRESOURCE(IDD_CONFIRMDIALOG), hParent, (DLGPROC) processMsg, (long)thisPtr); 

	RECT rect;
	GetWindowRect (hParent, &rect);
	
	positionWindow ((rect.right + rect.left)/2 - offsetXFromCenter, (rect.bottom + rect.top)/2);
	ShowWindow(hWnd, SW_SHOWNORMAL); 
	visible = true;
	Viewer3D::mainWinHasFocus = false;
	return 1;
}

bool ConfirmDialog::containsWindow (HWND hwnd)
{
	return (hwnd == hWnd
		|| hwnd == GetDlgItem (hWnd, ID_YES)
		|| hwnd == GetDlgItem (hWnd, ID_NO));
}

LRESULT CALLBACK ConfirmDialog::processMsg(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam) {
	
	// Get pointer to the object to send the message to
	LONG_PTR lptr = GetWindowLongPtr (hDlg, GWLP_USERDATA);
	ConfirmDialog* dlg = NULL;
	dlg = reinterpret_cast<ConfirmDialog*> ((LPARAM)lptr);

	// Until WM_INITDIALOG is first called, do default processing
	if (!dlg && message != WM_INITDIALOG)
		return DefWindowProc(hDlg, message, wParam, lParam);
	// First (and only) time WM_INITDIALOG is called, setup object for later access
	else if (message == WM_INITDIALOG)
	{
		try {
			dlg = reinterpret_cast<ConfirmDialog*>(lParam);
			SetWindowLongPtr (hDlg, GWLP_USERDATA, (LONG)lParam);
			dlg->hWnd = hDlg;
		}
		catch (...)
		{
			MessageBox (NULL, "Error extracting ConfirmDialog type", "Error", MB_OK|MB_ICONEXCLAMATION);
			dlg = NULL;
		}
		
	}
	
	if (dlg == NULL)
	{
		MessageBox (NULL, "Could not extract ConfirmDialog object", "Error", MB_OK|MB_ICONEXCLAMATION);
		return false;
	}
	
	// Forward message for processing
	return dlg->processMessage(hDlg, message, wParam, lParam);
	
}

LRESULT ConfirmDialog::processMessage(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam)
{
	
	switch (message) {
		int wmId, wmEvent;

		case WM_INITDIALOG:

			return TRUE;

		case WM_PAINT: {
			PAINTSTRUCT ps;
			RECT updateRect;
			if (!GetUpdateRect(hDlg,&updateRect,false)) return 0;
			if (!BeginPaint(hDlg,&ps)) return 0;

			EndPaint(hDlg, &ps);
			break;

		}
		case WM_COMMAND: {
			wmId    = LOWORD(wParam); 
			wmEvent = HIWORD(wParam); 

			// Parse the menu selections:
			switch (wmId) {
				case ID_YES: {
					SendMessage (hParent, ID_YES, (WPARAM) type, 0);
					
					DestroyWindow (hDlg);
					Viewer3D::mainWinHasFocus = true;
					return TRUE;
				}

				case ID_NO: {
					SendMessage (hParent, ID_NO, (WPARAM) type, 0);

					DestroyWindow (hDlg);
					Viewer3D::mainWinHasFocus = true;
					return TRUE;
				}
			}
		}
	}
	return FALSE;
}

void ConfirmDialog::setType (int newType)
{
	switch (newType) 
	{
		case EXITPROGRAM:
			type = EXITPROGRAM;
			sprintf_s (text, 100, "exit\0");
			break;
	}

	char fullText [1000] = {0};
	sprintf_s (fullText, 1000, "Are you sure you want to %s?\0", text);
	SetWindowText (GetDlgItem (hWnd, IDC_CONFIRMTEXT), fullText);
  
}