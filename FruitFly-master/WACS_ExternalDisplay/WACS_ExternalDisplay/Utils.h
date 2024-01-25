/*
A file with utility functions/variables for global use
*/

#ifndef CBDUTILS_H
#define CBDUTILS_H

#include "stdafx.h"
#include "commdlg.h"
#include <string>
using namespace std;

/**
\brief Format a filter string to eliminate file types

\param uId ID of string
\param lpszString location to store string
\param hInst Instance of this application
\return BOOL status
*/
BOOL FormatFilterString(UINT uID, LPSTR lpszString, HINSTANCE hInst);

/**
\brief Raise a dialog allowing the user to choose a file

\param hWnd Parent window
\szFilter Filter string
\myFileName Location to store filename afterwards
\myTitleName Title of dialog window
\return BOOL status
*/
BOOL myGetOpenFileName(HWND hWnd, char szFilter[], char myFileName[], char myTitleName[]);

/**
\brief Sets the working directory to where the application is running.  Makes sure files are read and saved from the right place
*/
void setWorkingDirectory ();

/**
\brief Converts a (possibly) local path to absolute
*/
string makeLocaDirAbsolute (string localPath);

#endif

