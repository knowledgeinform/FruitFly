//---------------------------------------------------------------------------------------
/*!\class  File Name:	FileUtilities.h
// \author Author:		Myron Z. Brown, Jason A.Stipes
// \brief  Purpose:		Utilities for accessing files, adjusting file names
//
// \note   Notes:	
//
// \note   Routines: see Algorithm Class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2008
*/
//---------------------------------------------------------------------------------------
#pragma once	

#include <vector>
#include "dirent.h"
#include <string>
using namespace std;

typedef struct
{
	char pathName[1024];
}
PathNameStruct;

	//!Remove the folder name from a path
	void RemoveFolderName(const char *fullFileName, char *adjustedFileName);

	//!Get the parent folder name from a path
	void GetParentFolderName(const char *fullFileName, char *parentFolder);

	//!Remove the file name from a path
	void RemoveFileName(const char *fullFileName, char *folderName);

	//!Get a binary adjusted filename (add _adj.bpf)
	void GetAdjustedFileNameBinary(const char *fullFileName, char *adjustedFileName);

	//!Get a binary adjusted filename (add _adj.xyz)
	void GetAdjustedFileNameASCII(const char *fullFileName, char *adjustedFileName);

	//!Get a list of the available folders
	bool getFolders(char *pathName, char *parentFolder, vector<PathNameStruct> &fs);

