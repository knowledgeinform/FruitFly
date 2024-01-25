/* $Id: orlando.h,v 1.12 2009-02-11 13:12:10 gertjan Exp $ */
/*////////////////////////////////////////////////////////////////////////////////  
//
//                       (C) ARVOO Imaging Products BV
//  All rights are reserved.  Reproduction in whole or in part is not prohibited 
//  without the written consent of the copyright owner.
//    
//  ARVOO reserves the right to make changes without notice at any time.  
//  ARVOO makes no warranty, expressed, implied or statutory, including but 
//  not limited to any implied warranty of merchantibility of fitness for any 
//  particular purpose, or that the use will not infringe any third party 
//  patent, copyright or trademark.  ARVOO must not be liable for any loss
//  or damage arising from its use.
//
//////////////////////////////////////////////////////////////////////////////// */

#ifndef __ORLANDO_HEADER__
#define __ORLANDO_HEADER__

#ifndef ____ARVOO_ORLANDO_TYPS__
	#include "orltyps.h"
#endif
//#ifndef __ARVOO_DRIVER_TYPES__
//	#include "arvtyp.h"
//#endif

/* DLL calling convention */
#ifdef _WIN64
#define ORL_CALL_CONV	__fastcall
#elif _WIN32
#define ORL_CALL_CONV	__stdcall
#else
#define ORL_CALL_CONV
#endif 

#define ORLANDO_LIB_VERSION				109UL
#define ORLANDO_INTERFACE_VERSION		106UL

typedef	ArvDWORD	 ORL_HANDLE;
#define INVALID_ORL_HANDLE	((ORL_HANDLE)0xffff)

/* force the compiler to use enums with 4 bytes */
#define ORL_MAX_ENUM			0x7fffffff

#define ORLINITSTRUCT(v)	OrlInitStruct(&v, sizeof(v))

#ifdef _WIN32
	#ifdef _ORLANDO_EXPORT_
		/* When creating SDK library: */
		#define _IMPEXP_FUNC_C(r) __declspec(dllexport) r ORL_CALL_CONV
	#else 
		/* When import functions: */
		#ifndef __BORLANDC__
			#define _IMPEXP_FUNC_C(r)		__declspec(dllimport) r ORL_CALL_CONV
		#else
 			#define _IMPEXP_FUNC_C(r)		r _import ORL_CALL_CONV
		#endif
	#endif
#else
	#define _IMPEXP_FUNC_C(r) r
#endif

#if defined(__cplusplus)
	extern "C"{
#endif

/* Opens Library */
_IMPEXP_FUNC_C(OrlRet) OrlOpenLibrary( void );

/* Closes Library */
_IMPEXP_FUNC_C(OrlRet) OrlCloseLibrary( void );

/* Get SDK DLL version */
_IMPEXP_FUNC_C(OrlRet) OrlGetVersion( SOrlLibVer *ver );

/* Number of Orlando boards found */
_IMPEXP_FUNC_C(OrlRet) OrlGetNumBoards( ArvDWORD *num_boards );

/* Returns the Board type */
_IMPEXP_FUNC_C(OrlRet) OrlGetBoardType( ArvDWORD boardnum, OrlBoardType *type );

/* Open board  */
_IMPEXP_FUNC_C(OrlRet) OrlOpen( ArvDWORD boardnum, ORL_HANDLE *h );

/* Closes board */
_IMPEXP_FUNC_C(OrlRet) OrlClose( ORL_HANDLE h );

/* General function.  */
_IMPEXP_FUNC_C(OrlRet) OrlFunc( ORL_HANDLE h, OrlFuncType ft, ArvPVOID p );

_IMPEXP_FUNC_C(void) OrlInitStruct( ArvPVOID v, ArvDWORD size );

_IMPEXP_FUNC_C(OrlRet) OrlEnumToStr( OrlEnumType etype, ArvSDWORD itemofenum, ArvPCHAR strbuff, ArvDWORD buffsize );


#if defined(__cplusplus)
	}
#endif 

#undef _IMPEXP_FUNC_C

#endif	/* __ORLANDO_HEADER__ */
