//---------------------------------------------------------------------------------------
// File Name:	Dirent.h
// Author:		Myron Z. Brown
// Purpose:		Contains routines for manipulating directories of files.
//
// Notes:		Derived from DIRLIB.C by Matt J. Weinstein
//				Updated by Jeremy Bettis <jeremy@hksys.com>
//				Revised by Colin Peters <colin@fu.is.saga-u.ac.jp>
//				Simplified for use without MinGW by Myron Brown
//
// Copyright (c) 2005 Johns Hopkins University 
// Applied Physics Laboratory.  All Rights Reserved.
//
//---------------------------------------------------------------------------------------

#pragma once

#include <io.h>

//---------------------------------------------------------------------------------------
// Structures.
//---------------------------------------------------------------------------------------
typedef struct
{
	long			d_ino;			// Always zero. 
	unsigned short	d_reclen;		// Always zero. 
	unsigned short	d_namlen;		// Length of name in d_name. 
	char*			d_name;			// File name. 
}
dirent;

typedef struct
{
	struct _finddata_t	dd_dta;		// disk transfer area for this dir
	dirent				dd_dir;		// dirent struct to return from dir
	long				dd_handle;	// _findnext handle
	int					dd_stat;	// 0 = not started; -1 = off end; else index
	char				dd_name[1];	//given path for dir with search pattern

}
DIR;

//---------------------------------------------------------------------------------------
// Function prototypes.
//---------------------------------------------------------------------------------------
DIR*	opendir (const char*);
dirent*	readdir (DIR*);
int		closedir (DIR*);
void	rewinddir (DIR*);
long	telldir (DIR*);
void	seekdir (DIR*, long);


