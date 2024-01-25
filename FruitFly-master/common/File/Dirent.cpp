//---------------------------------------------------------------------------------------
// File Name:	Dirent.cpp
// Author:		Myron Z. Brown
// Purpose:		Directory manipulation routines.
//
// Notes:		Derived from DIRLIB.C by Matt J. Weinstein
//				Updated by Jeremy Bettis <jeremy@hksys.com>
//				Revised by Colin Peters <colin@fu.is.saga-u.ac.jp>
//				Simplified for use without MinGW by Myron Brown
//
// Routines:	opendir
//				readdir
//				closedir
//				rewinddir
//				telldir
//				seekdir
//
// Copyright (c) 2005 Johns Hopkins University 
// Applied Physics Laboratory.  All Rights Reserved.
//
//---------------------------------------------------------------------------------------

#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <io.h>
#include <direct.h>
#include <windows.h>

#include "Dirent.h"

#define SUFFIX	("*")
#define	SLASH	("\\")


//---------------------------------------------------------------------------------------
// Open a directory for reading.
//---------------------------------------------------------------------------------------
DIR* opendir(const char *szPath)
{
	DIR *nd;
	unsigned long rc;
	char szFullPath[MAX_PATH];

	errno = 0;

	// Check for NULL string.
	if (!szPath)
	{
		errno = EFAULT;
		return NULL;
	}
	if (szPath[0] == ('\0'))
	{
		errno = ENOTDIR;
		return NULL;
	}

	// Get file attributes and check for directory.
	rc = GetFileAttributes(szPath);
	if (rc == -1)
	{
		errno = ENOENT;
		return NULL;
	}
	if (!(rc & FILE_ATTRIBUTE_DIRECTORY))
	{
		errno = ENOTDIR;
		return NULL;
	}

	// Make an absolute pathname.
	_fullpath(szFullPath, szPath, MAX_PATH);

	// Allocate memory for the directory structure.
	nd = (DIR*)malloc(sizeof(DIR) + strlen(szFullPath) + strlen(SLASH) + strlen(SUFFIX));
	if (!nd)
	{
		errno = ENOMEM;
		return NULL;
	}

	// Create the search expression.
	strcpy(nd->dd_name, szFullPath);

	// Add on a slash if the path does not end with one.
	if (nd->dd_name[0] != ('\0') &&
		nd->dd_name[strlen(nd->dd_name) - 1] != ('/') &&
		nd->dd_name[strlen(nd->dd_name) - 1] != ('\\'))
	{
		strcat(nd->dd_name, SLASH);
	}

	// Add on the search pattern.
	strcat(nd->dd_name, SUFFIX);

	// Initialize the structure.
	nd->dd_handle = -1;
	nd->dd_stat = 0;
	nd->dd_dir.d_ino = 0;
	nd->dd_dir.d_reclen = 0;
	nd->dd_dir.d_namlen = 0;
	nd->dd_dir.d_name = nd->dd_dta.name;

	return nd;
}


//---------------------------------------------------------------------------------------
// Read the directory structure.
//---------------------------------------------------------------------------------------
dirent* readdir(DIR * dirp)
{
	errno = 0;

	// Check for valid DIR struct.
	if (!dirp)
	{
		errno = EFAULT;
		return NULL;
	}
	if (dirp->dd_dir.d_name != dirp->dd_dta.name)
	{
		errno = EINVAL;
		return NULL;
	}

	// Read next entry.
	if (dirp->dd_stat < 0)
	{
		return NULL;
	}
	else if (dirp->dd_stat == 0)
	{
		// Start the search.
		dirp->dd_handle = (long)_findfirst (dirp->dd_name, &(dirp->dd_dta));
		if (dirp->dd_handle == -1) 
			dirp->dd_stat = -1;
		else
			dirp->dd_stat = 1;
	}
	else
	{
		// Get the next search entry.
		if (_findnext(dirp->dd_handle, &(dirp->dd_dta)))
		{
			// Close when at the end.
			_findclose(dirp->dd_handle);
			dirp->dd_handle = -1;
			dirp->dd_stat = -1;
		}
		else
		{
			// Update status.
			dirp->dd_stat++;
		}
	}

	// Check the status.
	if (dirp->dd_stat > 0)
	{
		dirp->dd_dir.d_namlen = (unsigned short)strlen(dirp->dd_dir.d_name);
		return &dirp->dd_dir;
	}
	return NULL;
}

//---------------------------------------------------------------------------------------
// Close a directory.
//---------------------------------------------------------------------------------------
int closedir (DIR * dirp)
{
	int rc;

	errno = 0;
	rc = 0;

	// Check for invalid DIR structure.
	if (!dirp)
	{
		errno = EFAULT;
		return -1;
	}
	
	// Close the directory.
	if (dirp->dd_handle != -1)
	{
		rc = _findclose(dirp->dd_handle);
	}

	// Delete the dir structure.
	free (dirp);

	return rc;
}


//---------------------------------------------------------------------------------------
// Rewind a directory to the beginning.
//---------------------------------------------------------------------------------------
void rewinddir (DIR * dirp)
{
	errno = 0;

	// Check for invalid DIR structure.
	if (!dirp)
	{
		errno = EFAULT;
		return;
	}

	// Close the directory.
	if (dirp->dd_handle != -1)
	{
		_findclose (dirp->dd_handle);
	}

	// Reset the directory handle.
	dirp->dd_handle = -1;
	dirp->dd_stat = 0;
}


//---------------------------------------------------------------------------------------
// Tell the current position in the directory stream. 
//---------------------------------------------------------------------------------------
long telldir (DIR * dirp)
{
	errno = 0;
	if (!dirp)
	{
		errno = EFAULT;
		return -1;
	}
	return dirp->dd_stat;
}


//---------------------------------------------------------------------------------------
// Seek to given position in directory stream.
//---------------------------------------------------------------------------------------
void seekdir (DIR * dirp, long lPos)
{
	errno = 0;

	// Check for invalid DIR structure.
	if (!dirp)
	{
		errno = EFAULT;
		return;
	}

	// If valid position selected, seek to it.
	if (lPos < -1)
	{
		errno = EINVAL;
		return;
	}
	else if (lPos == -1)
	{
		if (dirp->dd_handle != -1)
		{
			_findclose (dirp->dd_handle);
		}
		dirp->dd_handle = -1;
		dirp->dd_stat = -1;
	}
	else
	{
		rewinddir (dirp);
		while ((dirp->dd_stat < lPos) && readdir (dirp));
	}
}
