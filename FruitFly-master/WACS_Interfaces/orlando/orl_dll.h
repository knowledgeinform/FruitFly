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
*	$Id: orl_dll.h,v 1.1 2008-12-19 14:10:12 gertjan Exp $
*
*******************************************************************************/

#ifndef __ORLANDO_C_DLL__
#define __ORLANDO_C_DLL__

#include "orlando.h"

OrlRet LoadOrlandoLib( void );
void FreeOrlandoLib( void );

#ifdef WIN32
	/* Windows OS */
	OrlRet FOrlOpenLibrary( void );
	OrlRet FOrlCloseLibrary( void );
	OrlRet FOrlGetVersion( SOrlLibVer *ver );
	OrlRet FOrlGetNumBoards( ArvDWORD *num_boards );
	OrlRet FOrlGetBoardType( ArvDWORD boardnum, OrlBoardType *type );
	OrlRet FOrlOpen( ArvDWORD boardnum, ORL_HANDLE *orl_h );
	OrlRet FOrlClose( ORL_HANDLE orl_h );
	OrlRet FOrlFunc( ORL_HANDLE orl_h,  OrlFuncType ft, ArvPVOID p );
	OrlRet FOrlEnumToStr( OrlEnumType etype, ArvSDWORD itemofenum, ArvPCHAR strbuff, ArvDWORD buffsize );
	OrlRet FOrlInitStruct( ArvPVOID v, ArvDWORD size );

#else /* WIN32 */
	/* not Windows OS */
	#define FOrlOpenLibrary() OrlOpenLibrary()
	#define FOrlCloseLibrary( ) OrlCloseLibrary( )
	#define FOrlGetVersion( ver ) OrlGetVersion( ver )
	#define FOrlGetNumBoards( num_boards )	OrlGetNumBoards( num_boards )
	#define FOrlGetBoardType( boardnum, type )	OrlGetBoardType( boardnum, type )
	#define FOrlOpen( boardnum, orl_h ) OrlOpen( boardnum, orl_h )
	#define FOrlClose( orl_h ) OrlClose( orl_h )
	#define FOrlFunc( orl_h, ft, p ) OrlFunc( orl_h, ft, p )
	#define FOrlEnumToStr( etype, itemofenum, strbuff, buffsize ) OrlEnumToStr( etype, itemofenum, strbuff, buffsize )
	#define FOrlInitStruct( v, size ) OrlInitStruct( v, size )

#endif /* !WIN32 */

#define ORLINITS(v)	FOrlInitStruct(&v, sizeof(v))

#endif	/* __ORLANDO_C_DLL__ */
