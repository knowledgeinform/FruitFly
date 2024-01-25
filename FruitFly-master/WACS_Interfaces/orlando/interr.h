/* $Id: interr.h,v 1.1 2008-12-19 14:10:11 gertjan Exp $ */
#ifndef __ORLC_INTERRUPT_HANDLING__
#define __ORLC_INTERRUPT_HANDLING__

typedef void  (*IHFunc)( void * );

typedef struct _OrlInterrHandleParm
{
	IHFunc	ihfunc;
	void		*ihparm;
	void		*thread_handle;
	ArvBOOL	thread_is_running;
} SOrlInterrHandleParm, *PSOrlInterrHandleParm;

typedef struct {
	SOrlInterrHandleParm	ih;
	ORL_HANDLE	hbrd;
	OrlModule	b2h_module;
	ArvDWORD		hostbufferix;
} InToHostDataStruct, *PInToHostDataStruct;

void OrlCreateInterruptHandlingThread( PSOrlInterrHandleParm ihparm );
void OrlDestroyInterruptHandlingThread( PSOrlInterrHandleParm ihparm );

#endif /* __ORLC_INTERRUPT_HANDLING__ */
