/* $Id: interr.c,v 1.1 2008-12-19 14:10:11 gertjan Exp $ */

#ifdef WIN32
#include <windows.h>
#endif

#include "orlando.h"
#include "orl_dll.h"
#include "interr.h"

/*
	Creating of a thread for handling interrupt. Depends on used OS.	
*/

#ifdef WIN32

DWORD WINAPI OrlHandleInterruptWin( LPVOID parm )
{
	PSOrlInterrHandleParm	ihparm;
	
	if ( parm )
	{
		ihparm = (PSOrlInterrHandleParm)parm;
		if ( ihparm->ihfunc )
		{
			ihparm->thread_is_running = ArvTRUE;
			ihparm->ihfunc(parm);/* call interr. handling function */
			ihparm->thread_is_running = ArvFALSE;
		}
	}
	return 0;
}

void OrlCreateInterruptHandlingThread( PSOrlInterrHandleParm ihparm )
{
	DWORD		threadid = 0;
	HANDLE	hthread = NULL;
	hthread = CreateThread( NULL, 0x1000, OrlHandleInterruptWin, ihparm, 0, &threadid );

	ihparm->thread_handle = (void *)hthread;
}

void OrlDestroyInterruptHandlingThread( PSOrlInterrHandleParm ihparm )
{
	HANDLE	hthread = NULL;
	int		esc_t = 0;
	if ( ihparm != NULL )
	{
		hthread = (HANDLE)ihparm->thread_handle;
		while( ihparm->thread_is_running && esc_t++ < 20 )
		{
			Sleep( 100 );/* 100 ms */
		}
		if ( ihparm->thread_is_running )
		{
			printf( "Thread not stopped\n" );
		}
		if ( hthread != NULL )
			CloseHandle( hthread );
		ihparm->thread_handle = NULL;
	}
}

#endif /* WIN32 */

#if defined(_LINUX) || defined(__QNXNTO__)

#include <pthread.h>
#include <errno.h>
#include <unistd.h>


void* OrlHandleInterruptLinux( void *parm )
{
	PSOrlInterrHandleParm	ihparm;

	if ( parm )
	{
		ihparm = (PSOrlInterrHandleParm)parm;
		if ( ihparm->ihfunc )
		{
			ihparm->thread_is_running = ArvTRUE;
			ihparm->ihfunc(parm);
			ihparm->thread_is_running = ArvFALSE;
		}
	}
	return NULL;
}

void OrlCreateInterruptHandlingThread( PSOrlInterrHandleParm ihparm )
{
	pthread_t				threadid = (pthread_t)0;
	pthread_attr_t			attr;
	int	iret;

	iret = pthread_attr_init( &attr );
	if ( iret == 0 )
	{
		iret = pthread_create(&threadid, &attr, OrlHandleInterruptLinux, (void *)ihparm);
	}
	if ( iret != 0 )
	{
		printf( "pthread_create failed errno:%d\n", errno );
	}
	ihparm->thread_handle = (void *)threadid;
}

void OrlDestroyInterruptHandlingThread( PSOrlInterrHandleParm ihparm )
{
	pthread_t				threadid = (pthread_t)0;
	int						esc_t = 0;
	if ( ihparm != NULL )
	{
		threadid = (pthread_t)ihparm->thread_handle;

		while ( ihparm->thread_is_running && esc_t++ < 20 )
		{
			usleep( 100 * 1000 );/* 100 ms */
		}
		if ( ihparm->thread_is_running )
		{
			printf( "Thread not stopped\n" );
		}
		pthread_join( threadid, NULL );
	}
}

#endif /* _LINUX || __QNXNTO__ */

