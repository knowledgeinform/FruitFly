#include "stdafx.h"
#include "DialogBase.h"

DialogBase::DialogBase(HINSTANCE newHinst, HWND newhParent)
{
	hWnd = NULL;
	hInst = newHinst;
	hParent = newhParent;

}

DialogBase::~DialogBase ()
{
	
}

BOOL DialogBase::checkwindow(HWND mywin) 
{
	BringWindowToTop(mywin);
	return IsWindow(mywin);

}

void DialogBase::getWindowPosition (int &winLeft, int &winTop)
{
	if (hWnd == NULL)
		return;
	getWindowPosition (hWnd, winLeft, winTop);
}

void DialogBase::positionWindow (int winLeft, int winTop) {
	WINDOWPLACEMENT winplace;
	GetWindowPlacement (hWnd, &winplace);
	int winWidth = winplace.rcNormalPosition.right - winplace.rcNormalPosition.left;
	int winHeight = winplace.rcNormalPosition.bottom - winplace.rcNormalPosition.top;

	winplace.rcNormalPosition.left = winLeft;
	winplace.rcNormalPosition.right = winplace.rcNormalPosition.left + winWidth;
	winplace.rcNormalPosition.top = winTop;
	winplace.rcNormalPosition.bottom = winplace.rcNormalPosition.top + winHeight;
	SetWindowPlacement (hWnd, &winplace);
}

void DialogBase::getWindowPosition (HWND window, int &winLeft, int &winTop) {
	if (window == NULL)
		return;
	
	RECT rect;
	GetWindowRect (window, &rect);
	winLeft = (int)rect.left;
	winTop = (int)rect.top;
}

void DialogBase::getWindowSize (HWND window, int &winWidth, int &winHeight) {
	if (window == NULL)
		return;

	RECT rect;
	GetWindowRect (window, &rect);
	winWidth = rect.right - rect.left;
	winHeight = rect.bottom - rect.top;
}