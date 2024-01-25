/* $Id: arvtyp.h,v 1.49 2009-02-19 12:55:48 gertjan Exp $ */
/********************************************************************************
*
*                       (C) ARVOO Imaging Products BV
*  Reproduction in whole or in part is prohibited without the written 
*  consent of the copyright owner.
*
*  ARVOO reserves the right to make changes without notice at any time.  
*  ARVOO makes no warranty, expressed, implied or statutory, including but 
*  not limited to any implied warranty of merchantibility of fitness for any 
*  particular purpose, or that the use will not infringe any third party 
*  patent, copyright or trademark.  ARVOO must not be liable for any loss
*  or damage arising from its use.
*
********************************************************************************/

#ifndef __ARVOO_DRIVER_TYPES__
#define __ARVOO_DRIVER_TYPES__


#ifdef __RTL__
#else

#ifndef __ARVOO_DRIVER_HEADERS__
	#include <stdio.h> 
#endif

#endif


/*
	ARVOO general driver types
*/

/* standard calling convention */


#ifdef _WIN32
	#define ARV_CALL_CONV	__stdcall
#else
	#define ARV_CALL_CONV
#endif	/* _WIN32 */

#ifdef Memset
#define ARVZERO(p)		Memset(&p,0,sizeof(p))
#else
#ifndef __KERNEL__
	#if (defined ( __QNX4__) || defined(_MIPS) )
		#include <malloc.h>
	#else /* __QNX4__ */
		#include <memory.h>
	#endif /* !__QNX4__ */
	#define ARVZERO(p)		memset(&p,0,sizeof(p))
#endif /* __KERNEL */
#endif

#define ARV_INVALID_THREAD		0xFFFFFFFFUL

/* data types */
#ifdef _WIN32
	typedef unsigned char		ArvBYTE;		/* 1 byte unsigned */
	typedef signed char			ArvSBYTE;	/* 1 byte signed  */
	typedef unsigned short int	ArvWORD;		/* 2 bytes unsigned */
	typedef signed short int	ArvSWORD;	/* 2 bytes signed */
	typedef unsigned long int	ArvDWORD;	/* 4 bytes unsigned */ 
	typedef signed long int		ArvSDWORD;	/* 4 bytes signed */
#if defined(__GNUC__)
	typedef unsigned long long 	ArvQWORD;	/* 8 bytes unsigned */
	typedef long long				ArvSQWORD;	/* 8 bytes signed */
#else
	typedef unsigned __int64	ArvQWORD;	/* 8 bytes unsigned */
	typedef __int64				ArvSQWORD;	/* 8 bytes signed */
#endif /* !__GNUC__ */
	typedef void*				ArvPVOID;
	typedef char				ArvCHAR;
	typedef ArvCHAR*			ArvPCHAR;
	typedef FILE				ArvFILE;
	typedef ArvPVOID			ArvSEMAPHORE;
	typedef ArvPVOID			ArvMUTEX;
	typedef ArvDWORD			ArvTHREAD;
	typedef float				ArvFLOAT;	/* 4 bytes floating point */
	typedef double				ArvDOUBLE;	/* 8 bytes floating point */
	typedef void(*Arvthreadfunction)(ArvPVOID p);
	typedef ArvSQWORD				ArvTM;

	#define ARV_INFINITE					0xFFFFFFFFUL  /* == INFINITE, winbase.h */
	#define ARV_INVALID_MUTEX			NULL
	#define ARV_INVALID_SEMAPHORE		NULL
#endif /* _WIN32 */


#if defined(_LINUX) || defined(__QNX__)

//#include <semaphore.h>
	typedef unsigned char		ArvBYTE;		/* 1 byte unsigned */
	typedef signed char			ArvSBYTE;	/* 1 byte signed  */
	typedef unsigned short int	ArvWORD;		/* 2 bytes unsigned */
	typedef signed short int	ArvSWORD;	/* 2 bytes signed */
#if defined(__x86_64__)
	typedef unsigned int		ArvDWORD;	/* 4 bytes unsigned */ 
	typedef signed int			ArvSDWORD;	/* 4 bytes signed */
#else
	typedef unsigned long int	ArvDWORD;	/* 4 bytes unsigned */ 
	typedef signed long int		ArvSDWORD;	/* 4 bytes signed */
#endif
	typedef void*				ArvPVOID;
	typedef char				ArvCHAR;
	typedef ArvCHAR*			ArvPCHAR;
	typedef void(*Arvthreadfunction)(ArvPVOID p);
	typedef ArvDWORD			ArvMUTEX;
	typedef ArvDWORD				ArvSEMAPHORE;
	typedef ArvDWORD			ArvTHREAD;
	typedef float				ArvFLOAT;	/* 4 bytes floating point */
	typedef double				ArvDOUBLE;	/* 8 bytes floating point */

	#ifdef __QNX4__
		typedef struct _ArvQWORD {
			ArvDWORD	dwlow;
			ArvDWORD	dwhigh;				/* 8 bytes unsigned */
		} ArvQWORD;		

		typedef struct _ArvSQWORD {
			ArvDWORD	dwlow;
			ArvSDWORD	dwhigh;
		} ArvSQWORD;						/* 8 bytes signed */	
		typedef struct timespec			ArvTM;
	#else
		typedef unsigned long long	ArvQWORD;	/* 8 bytes unsigned */ 
		typedef signed long long	ArvSQWORD;	/* 8 bytes signed */
		typedef ArvQWORD				ArvTM;
	#endif

	#ifndef __RTL__
		typedef FILE				ArvFILE;
	#else
		typedef void				ArvFILE;
	#endif

	#define ARV_INFINITE					0xffffffffUL
	#define ARV_INVALID_MUTEX			0xffffffffUL
	#define ARV_INVALID_SEMAPHORE		0xffffffffUL

	#if !defined(__QNX4__) 
		
	#if defined(__QNX6__)
		typedef struct
		{
			ArvWORD    bfType;
			ArvDWORD   bfSize;
			ArvWORD    bfReserved1;
			ArvWORD    bfReserved2;
			ArvDWORD   bfOffBits; 
		} __attribute__((packed)) BITMAPFILEHEADER;  
	#else
		typedef struct
		{ //use __attribute__ ((packed)); for right struct size
			ArvWORD    bfType __attribute__ ((packed));
			ArvDWORD   bfSize __attribute__ ((packed));
			ArvWORD    bfReserved1 __attribute__ ((packed));
			ArvWORD    bfReserved2 __attribute__ ((packed));
			ArvDWORD   bfOffBits __attribute__ ((packed)); 
		} BITMAPFILEHEADER;  
	#endif  //__QNX6__

	#else
		
		typedef struct
		{
			ArvWORD    bfType;
			ArvDWORD   bfSize;
			ArvWORD    bfReserved1;
			ArvWORD    bfReserved2;
			ArvDWORD   bfOffBits; 
		} BITMAPFILEHEADER;  
	#endif	/* __QNX4__ */


	typedef struct
	{
		ArvDWORD  biSize;
		ArvSDWORD   biWidth;
		ArvSDWORD   biHeight;
		ArvWORD   biPlanes;
		ArvWORD   biBitCount;
		ArvDWORD  biCompression;
		ArvDWORD  biSizeImage;
		ArvSDWORD  biXPelsPerMeter;
		ArvSDWORD   biYPelsPerMeter;
		ArvDWORD  biClrUsed;
		ArvDWORD  biClrImportant; 
	} BITMAPINFOHEADER; 

	typedef struct 
	{
		ArvBYTE    rgbBlue;
		ArvBYTE    rgbGreen; 
		ArvBYTE    rgbRed;
		ArvBYTE    rgbReserved; 
	} RGBQUAD; 

	typedef struct 
	{
		BITMAPINFOHEADER bmiHeader; 
		RGBQUAD          bmiColors[1]; 
	} BITMAPINFO; 

	#define BI_RGB        0L
	#define BI_BITFIELDS  3L


	#define LOBYTE(w) ((unsigned char)w)
	#define HIBYTE(w) ((unsigned char)(((unsigned short)(w) >> 8 ) & 0xFF ))
	#define LOWORD(l) ((unsigned short)l)
	#define HIWORD(l) ((unsigned short)(((unsigned long)(l) >> 16 ) & 0xFFFF))

#endif /* _LINUX || __QNX__ */


#if defined(_MIPS) 
	#define ArvFALSE	0
	#define ArvTRUE	1

//#include <semaphore.h>
	typedef unsigned char		ArvBYTE;		/* 1 byte unsigned */
	typedef signed char			ArvSBYTE;	/* 1 byte signed  */
	typedef unsigned short int	ArvWORD;		/* 2 bytes unsigned */
	typedef signed short int	ArvSWORD;	/* 2 bytes signed */
	typedef unsigned int		ArvDWORD;	/* 4 bytes unsigned */ 
	typedef signed int			ArvSDWORD;	/* 4 bytes signed */
	typedef unsigned long long	ArvQWORD;	/* 8 bytes unsigned */
	typedef long long			ArvSQWORD;	/* 8 bytes signed */
	typedef void*				ArvPVOID;
	typedef char				ArvCHAR;
	typedef ArvCHAR*			ArvPCHAR;
	typedef void(*Arvthreadfunction)(ArvPVOID p);
	typedef ArvDWORD			ArvMUTEX;
	typedef ArvDWORD			ArvSEMAPHORE;
	typedef ArvDWORD			ArvTHREAD;
	typedef float				ArvFLOAT;	/* 4 bytes floating point */
	typedef double				ArvDOUBLE;	/* 8 bytes floating point */


	#ifndef __RTL__
		typedef FILE				ArvFILE;
	#else
		typedef void				ArvFILE;
	#endif
	
	#define ARV_INFINITE		0xffffffffUL

	typedef struct
	{ //use __attribute__ ((packed)); for right struct size
	ArvWORD    bfType __attribute__ ((packed));
	ArvDWORD   bfSize __attribute__ ((packed));
	ArvWORD    bfReserved1 __attribute__ ((packed));
	ArvWORD    bfReserved2 __attribute__ ((packed));
	ArvDWORD   bfOffBits __attribute__ ((packed)); 
	} BITMAPFILEHEADER;  

	typedef struct
	{
		ArvDWORD  biSize;
		ArvSDWORD   biWidth;
		ArvSDWORD   biHeight;
		ArvWORD   biPlanes;
		ArvWORD   biBitCount;
		ArvDWORD  biCompression;
		ArvDWORD  biSizeImage;
		ArvSDWORD  biXPelsPerMeter;
		ArvSDWORD   biYPelsPerMeter;
		ArvDWORD  biClrUsed;
		ArvDWORD  biClrImportant; 
	} BITMAPINFOHEADER; 

	typedef struct 
	{
		ArvBYTE    rgbBlue;
		ArvBYTE    rgbGreen; 
		ArvBYTE    rgbRed;
		ArvBYTE    rgbReserved; 
	} RGBQUAD; 

	typedef struct 
	{
		BITMAPINFOHEADER bmiHeader; 
		RGBQUAD          bmiColors[1]; 
	} BITMAPINFO; 

	#define BI_RGB        0L
	#define BI_BITFIELDS  3L


	#define LOBYTE(w) ((unsigned char)w)
	#define HIBYTE(w) ((unsigned char)(((unsigned short)(w) >> 8 ) & 0xFF ))
	#define LOWORD(l) ((unsigned short)l)
	#define HIWORD(l) ((unsigned short)(((unsigned long)(l) >> 16 ) & 0xFFFF))

#endif /* _MIPS */


typedef	ArvBYTE				ArvBOOL;
#define	ArvFALSE	((ArvBOOL)0)
#define	ArvTRUE	((ArvBOOL)1)

#if defined(_WIN64) || defined(__x86_64__)
	typedef	ArvQWORD				ArvADDR;	/* contains address */
#else 
	typedef	ArvDWORD				ArvADDR;	/* contains address */
#endif	/* 32-bit */


/* ARVOO driver return values */
#define	ARV_RET_SUCCESS			0
#define	ARV_RET_ERROR			1
#define	ARV_RET_PARM			2
#define	ARV_RET_NOT_FOUND		3
#define	ARV_RET_TIMEOUT			4
#define	ARV_RET_BUSY			5
#define	ARV_RET_NOTENOUGHMEM	6
#define	ARV_RET_VERSION			7
#define	ARV_RET_BUFFERTOBIG		8
#define	ARV_RET_NOT_SUPPORTED	9
#define	ARV_RET_OVERFLOW			10

#define	ARVDRV_MAX_RET		99

/* return type */
typedef ArvDWORD ArvRet;


/* ARVOO driver handle */
typedef ArvDWORD ArvHandle;
#define INVALID_ARVDRV_HANDLE		0xffffffffUL
#define INVALID_ARVDRV_ID			0xffffffffUL

/* sytem log handle */
#define HSYSLOG				(ArvHandle)0xffff	

/* Bit mask constants */
#define MASK_00   0x00000001    /* Mask value for bit 0 */
#define MASK_01   0x00000002    /* Mask value for bit 1 */
#define MASK_02   0x00000004    /* Mask value for bit 2 */
#define MASK_03   0x00000008    /* Mask value for bit 3 */
#define MASK_04   0x00000010    /* Mask value for bit 4 */
#define MASK_05   0x00000020    /* Mask value for bit 5 */
#define MASK_06   0x00000040    /* Mask value for bit 6 */
#define MASK_07   0x00000080    /* Mask value for bit 7 */
#define MASK_08   0x00000100    /* Mask value for bit 8 */
#define MASK_09   0x00000200    /* Mask value for bit 9 */
#define MASK_10   0x00000400    /* Mask value for bit 10 */
#define MASK_11   0x00000800    /* Mask value for bit 11 */
#define MASK_12   0x00001000    /* Mask value for bit 12 */
#define MASK_13   0x00002000    /* Mask value for bit 13 */
#define MASK_14   0x00004000    /* Mask value for bit 14 */
#define MASK_15   0x00008000    /* Mask value for bit 15 */
#define MASK_16   0x00010000    /* Mask value for bit 16 */
#define MASK_17   0x00020000    /* Mask value for bit 17 */
#define MASK_18   0x00040000    /* Mask value for bit 18 */
#define MASK_19   0x00080000    /* Mask value for bit 19 */
#define MASK_20   0x00100000    /* Mask value for bit 20 */
#define MASK_21   0x00200000    /* Mask value for bit 21 */
#define MASK_22   0x00400000    /* Mask value for bit 22 */
#define MASK_23   0x00800000    /* Mask value for bit 23 */
#define MASK_24   0x01000000    /* Mask value for bit 24 */
#define MASK_25   0x02000000    /* Mask value for bit 25 */
#define MASK_26   0x04000000    /* Mask value for bit 26 */
#define MASK_27   0x08000000    /* Mask value for bit 27 */
#define MASK_28   0x10000000    /* Mask value for bit 28 */
#define MASK_29   0x20000000    /* Mask value for bit 29 */
#define MASK_30   0x40000000    /* Mask value for bit 30 */
#define MASK_31   0x80000000    /* Mask value for bit 31 */

#define MASK_B0   0x000000ff    /* Mask value for byte 0 */
#define MASK_B1   0x0000ff00    /* Mask value for byte 1 */
#define MASK_B2   0x00ff0000    /* Mask value for byte 2 */
#define MASK_B3   0xff000000    /* Mask value for byte 3 */

#define MASK_W0   0x0000ffff    /* Mask value for word 0 */
#define MASK_W1   0xffff0000    /* Mask value for word 1 */

#define MASK_PA   0xfffffffc    /* Mask value for physical address */
#define MASK_PR   0xfffffffe 	/* Mask value for protection register */
#define MASK_ER   0xffffffff    /* Mask value for the entire register */

#define MASK_NONE 0x00000000    /* No mask */

#define ARVDRV_MAX_ENUM		0x7fffffff /* force enum type to 4 bytes */
typedef enum tLogLev
{
	LEV_MESS = 0,
	LEV_WARN,
	LEV_ERR,
	LEV_SHOW,		/* always show */

	LEV_LOG_OFF,	/* no logging */

	NUM_LOGLEVS,

	UNKNOWN_LEV = ARVDRV_MAX_ENUM	
} ArvLogLev;

#define MAX_LOG_FILENAMELENGTH		255

/* Log file flags defs */
#define	ARV_LOGFLAG_DISABLE_FLUSH	0x1

typedef struct LogSettStruct
{
	ArvCHAR		filename[MAX_LOG_FILENAMELENGTH+1];
	ArvLogLev	level;
	ArvDWORD		flags;	/* bitwise ARV_LOGFLAG_xxxx */
} ArvLogSett, *PArvLogSett;


#endif	/* __ARVOO_DRIVER_TYPES__ */
