#include "stdafx.h"
#include "Utils.h"
#include "stdio.h"
#include "Logger.h"
#include "math.h"
#include "Constants.h"

BOOL FormatFilterString(UINT uID, LPSTR lpszString, HINSTANCE hInst) {
  int nLength, nCtr = 0;
  char chWildCard;
  
  *lpszString = 0;
  
  nLength = LoadString( hInst, uID, lpszString, 260 );
  
  chWildCard = lpszString[nLength-1];
  
  while( lpszString[nCtr] ) {
    if( lpszString[nCtr] == chWildCard ) lpszString[nCtr] = 0;
    nCtr++;
  }
  
  return TRUE;
}

BOOL myGetOpenFileName(HWND hWnd, char szFilter[], char myFileName[], char myTitleName[]) {
	
	OPENFILENAME ofn;
	ofn.lStructSize = sizeof(OPENFILENAME);
	ofn.hwndOwner = hWnd;
	ofn.hInstance = NULL;
	ofn.lpstrFilter = szFilter;
	ofn.lpstrCustomFilter = NULL;
	ofn.nMaxCustFilter = 0;
	ofn.nFilterIndex = 0;
	ofn.lpstrFile = myFileName;
	ofn.nMaxFile = 26000;
	ofn.lpstrFileTitle = NULL;
	ofn.nMaxFileTitle = 0;
	ofn.lpstrInitialDir = NULL;
	ofn.lpstrTitle = myTitleName;
	ofn.Flags = OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST | OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_EXPLORER | OFN_SHAREAWARE;
	ofn.nFileOffset = 0;
	ofn.nFileExtension = 0;
	ofn.lpstrDefExt = NULL;
	ofn.lCustData = 0;
	ofn.lpfnHook = NULL;
	ofn.lpTemplateName = NULL;
	ofn.hwndOwner = NULL;

	BOOL success = GetOpenFileName(&ofn);
	if (!success) {
//CDERR_DIALOGFAILURE
		DWORD myerror = CommDlgExtendedError();
		if (myerror) {
			sprintf_s( ofn.lpstrFile, sizeof(ofn.lpstrFile), "\0" );
			success = GetOpenFileName(&ofn);
		}
	}
	return success;
}

void setWorkingDirectory ()
{
	char currDirectory [MAX_PATH - 2];
	GetModuleFileName (NULL, currDirectory, MAX_PATH - 2);
	int lastSlash = -1;
	for (int i = strlen (currDirectory); i >= 0; i --)
	{
		if (currDirectory[i] == '/' || currDirectory[i] == '\\')
		{
			lastSlash = i;
			break;
		}
	}
	if (lastSlash >= 0)
		currDirectory[lastSlash + 1] = 0;
	
	if (SetCurrentDirectory (currDirectory) == 0)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "setWorkingDirectory: Unable to set current working directory to %s", currDirectory);
		Logger::getInstance()->logMessage (logBuf);
	}
}

string makeLocaDirAbsolute (string localPath)
{
	const int size = 26000;
	char absPathC [size];
	LPSTR* folder = NULL;
	setWorkingDirectory ();
	GetFullPathName (localPath.c_str(), size, absPathC, folder);

	string absPath (absPathC);
	return absPath;
}
