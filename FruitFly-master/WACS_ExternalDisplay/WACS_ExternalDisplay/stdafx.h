// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#include "targetver.h"

#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>
#include <windowsx.h>

#define GL_VERSION_1_5
#include "include\gl\gl.h"								// Header File For The OpenGL32 Library
#include "include\gl\glu.h"							// Header File For The GLu32 Library
#include "include\gl\glut.h"								// Header File For The GLu32 Library
#include "include\gl\glaux.h"
#include "include\GL\glext.h"
#include "include\glh\glh_genext.h"

#include "Mmsystem.h"

// C RunTime Header Files
#include <stdlib.h>
#include <malloc.h>
#include <memory.h>
#include <tchar.h>

#include <process.h>
#include <time.h>
#include <CommCtrl.h> 


#define MAX_LOADSTRING 100

//static PFNGLACTIVETEXTUREPROC glActiveTexture = NULL;
static PFNGLACTIVETEXTUREPROC glActiveTexture = NULL;
static PFNGLMULTITEXCOORD2DPROC glMultiTexCoord2d = NULL;

// TODO: reference additional headers your program requires here
