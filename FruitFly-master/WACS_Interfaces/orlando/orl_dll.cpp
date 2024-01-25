/*******************************************************************************
*
*                       (C) ARVOO Imaging Products BV
*  All rights are reserved.  Reproduction in whole or in part is prohibited 
*  without the written consent of the copyright owner.
*    
*  ARVOO reserves the right to make changes without notice at any time.  
*  ARVOO makes no warranty, expressed, implied or statutory, including but 
*  not limited to any implied warranty of merchantibility of fitness for any 
*  particular purpose, or that the use will not infringe any third party 
*  patent, copyright or trademark.  ARVOO must not be liable for any loss
*  or damage arising from its use.
*
********************************************************************************

********************************************************************************
*
*	$Id: orl_dll.c,v 1.1 2008-12-19 14:10:11 gertjan Exp $
*
*	Functions for explicitly linking to the orlando.dll.
*		
*
*******************************************************************************/

#include "orl_dll.h"
//#include "orlmain.h"


#ifdef WIN32
	#include <windows.h>
	#define		ORLLIBNAME		"orlando.dll"

	HMODULE		Hmod;
#endif

OrlRet LoadOrlandoLib( )
{
	OrlRet		ret;
	#ifdef WIN32

		Hmod = LoadLibrary( ORLLIBNAME );
		if ( Hmod == NULL )
		{
			printf( "LoadLibrary of %s failed (0x%x).\n", ORLLIBNAME, GetLastError() );
			return( ORL_RET_ERROR );
		}
	#endif 

	ret = FOrlOpenLibrary( );
	return( ret );
}

void FreeOrlandoLib( )
{
	FOrlCloseLibrary( );

	#ifdef WIN32
		FreeLibrary( Hmod );
	#endif
}


/* Windows 32/64 part */
#ifdef WIN32

/* typedefs for GetProcAddress function */

typedef  OrlRet (ORL_CALL_CONV* FNOrlOpenLibrary)( void );

/* _IMPEXP_FUNC_C( OrlRet) OrlCloseLibrary( void ); */
typedef  OrlRet (ORL_CALL_CONV* FNOrlCloseLibrary)( void );

/*_IMPEXP_FUNC_C( OrlRet) OrlGetVersion( SOrlLibVer *ver ); */
typedef  OrlRet (ORL_CALL_CONV* FNOrlGetVersion)(SOrlLibVer *);

/*_IMPEXP_FUNC_C( OrlRet) OrlGetNumBoards( ArvDWORD *num_cards );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlGetNumBoards)( ArvDWORD *);

/*_IMPEXP_FUNC_C( OrlRet) GetOrlBoardType( ArvDWORD boardnum,  OrlBoardType *type );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlGetBoardType)( ArvDWORD,  OrlBoardType * );

/*_IMPEXP_FUNC_C( OrlRet) OrlOpen( ArvDWORD boardnum,  ORL_HANDLE *orl_h );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlOpen)(ArvDWORD, ORL_HANDLE * );

/*_IMPEXP_FUNC_C( OrlRet) OrlClose( ORL_HANDLE orl_handle );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlClose)( ORL_HANDLE );


/*_IMPEXP_FUNC_C( OrlRet) OrlFunc( ORL_HANDLE orl_h,  OrlFuncType ft, ArvPVOID *p );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlFunc)(ORL_HANDLE, OrlFuncType, ArvPVOID * );

/* OrlRet FOrlEnumToStr( OrlEnumType etype, ArvSDWORD itemofenum, ArvPCHAR strbuff, ArvDWORD buffsize ); */
typedef  OrlRet (ORL_CALL_CONV* FNOrlEnumToStr)( OrlEnumType etype, ArvSDWORD itemofenum, ArvPCHAR strbuff, ArvDWORD buffsize );

/*_IMPEXP_FUNC_C(void) OrlInitStruct(ArvPVOID, ArvDWORD size );*/
typedef  OrlRet (ORL_CALL_CONV* FNOrlInitStruct)( ArvPVOID, ArvDWORD );

OrlRet FOrlOpenLibrary( )
{
	OrlRet		ret;
	FNOrlOpenLibrary	func;
#ifdef _WIN64
	func = (FNOrlOpenLibrary)GetProcAddress( Hmod, "OrlOpenLibrary" );
#else
	func = (FNOrlOpenLibrary)GetProcAddress( Hmod, "_OrlOpenLibrary@0" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlOpenLibrary.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( );
	return( ret );
}
/* FGetOrlClose
		Frees the orlando.dll (only if caller is last DLL-user)

*/
OrlRet FOrlCloseLibrary( )
{
	OrlRet		ret;
	FNOrlCloseLibrary	func;
#ifdef _WIN64
	func = (FNOrlCloseLibrary)GetProcAddress( Hmod, "OrlCloseLibrary" );
#else
	func = (FNOrlCloseLibrary)GetProcAddress( Hmod, "_OrlCloseLibrary@0" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlCloseLibrary.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( );
	return( ret );
}

/* FOrlGetVersion
		Retrieves the version info of the orlando.dll/lib

		output:
			ver:	version numbers
*/
OrlRet FOrlGetVersion( SOrlLibVer *ver )
{
	OrlRet		ret;
	FNOrlGetVersion		func;
#ifdef _WIN64
	func = (FNOrlGetVersion)GetProcAddress( Hmod, "OrlGetVersion" );
#else
	func = (FNOrlGetVersion)GetProcAddress( Hmod, "_OrlGetVersion@4" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlGetVersion.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( ver );
	return( ret );
}

/* FOrlGetNumBoards
		Retrieves the number of Orlando framegrabbers found.

		output:
			num_cards:	#found
*/
OrlRet FOrlGetNumBoards( ArvDWORD *num_boards )
{
	OrlRet					ret;
	FNOrlGetNumBoards		func;
#ifdef _WIN64
	func = (FNOrlGetNumBoards)GetProcAddress( Hmod, "OrlGetNumBoards" );
#else
	func = (FNOrlGetNumBoards)GetProcAddress( Hmod, "_OrlGetNumBoards@4" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlGetNumBoards.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( num_boards );
	return( ret );
}

/* FOrlGetOrlBoardType
		Retrieves the Orlando board type

		input
			boardnum:	Orlando board number (first is 1)
		output:
			type:			Orlando board type 
*/
OrlRet FOrlGetBoardType( ArvDWORD cardnum, OrlBoardType *type )
{
	OrlRet		ret;
	FNOrlGetBoardType	func;
#ifdef _WIN64
	func = (FNOrlGetBoardType)GetProcAddress( Hmod, "OrlGetBoardType" );
#else
	func = (FNOrlGetBoardType)GetProcAddress( Hmod, "_OrlGetBoardType@8" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlGetBoardType.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( cardnum, type );
	return( ret );
}

/* FOrlOpen
		Opens and intializes a Orlando framegrabber.

		input
			boardnum:	Orlando board number (first is 1)
		output:
			orl_h:		Orlando handle
*/
OrlRet FOrlOpen( ArvDWORD boardnum,  ORL_HANDLE *orl_h )
{
	OrlRet		ret;
	FNOrlOpen				func;
#ifdef _WIN64
	func = (FNOrlOpen)GetProcAddress( Hmod, "OrlOpen" );
#else
	func = (FNOrlOpen)GetProcAddress( Hmod, "_OrlOpen@8" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlOpen.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( boardnum, orl_h );
	return( ret );
}

/* FOrlClose
		Frees used Orlando resources.

		input
			orl_h:		Orlando handle
*/
OrlRet FOrlClose( ORL_HANDLE orl_handle )
{
	OrlRet		ret;
	FNOrlClose				func;
#ifdef _WIN64
	func = (FNOrlClose)GetProcAddress( Hmod, "OrlClose" );
#else
	func = (FNOrlClose)GetProcAddress( Hmod, "_OrlClose@4" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlClose.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( orl_handle );
	return( ret );
}


/* FOrlFunc
		Sets or gets a Orlando function.

		input
			orl_h:		Orlando handle
			ft:			Function type 
		input/output:
			p:				Pointer to the function parameter(s)
*/
OrlRet FOrlFunc( ORL_HANDLE orl_h,  OrlFuncType ft, void *p )
{
	 OrlRet		ret;
	FNOrlFunc				func;
#ifdef _WIN64	
	func = (FNOrlFunc)GetProcAddress( Hmod, "OrlFunc" );
#else
	func = (FNOrlFunc)GetProcAddress( Hmod, "_OrlFunc@12" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlFunc.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( orl_h, ft, (ArvPVOID*)p );
	return( ret );
}


OrlRet FOrlEnumToStr( OrlEnumType etype, ArvSDWORD itemofenum, ArvPCHAR strbuff, ArvDWORD buffsize )
{
	OrlRet			ret;
	FNOrlEnumToStr	func;
#ifdef _WIN64
	func = (FNOrlEnumToStr)GetProcAddress( Hmod, "OrlEnumToStr" );
#else
	func = (FNOrlEnumToStr)GetProcAddress( Hmod, "_OrlEnumToStr@16" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlEnumToStr.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( etype, itemofenum, strbuff, buffsize );
	return( ret );
}

/* FOrlInitStruct
		Initializes Orlando parameter structs.

		input/output:
			v:				Pointer a Orlando struct
		input
			size:			Size of struct in bytes.

*/
OrlRet FOrlInitStruct( ArvPVOID v, ArvDWORD size )
{
	OrlRet					ret;
	FNOrlInitStruct		func;
#ifdef _WIN64
	func = (FNOrlInitStruct)GetProcAddress( Hmod, "OrlInitStruct" );
#else
	func = (FNOrlInitStruct)GetProcAddress( Hmod, "_OrlInitStruct@8" );
#endif
	if ( func == NULL )
	{
		printf( "Failed to call GetProcAddress: OrlInitStruct.\n" );
		return( ORL_RET_ERROR );
	}
	ret = func( v, size );
	return( ret );
}

#endif	/* WIN32 */
