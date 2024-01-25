/* $Id: orltyps.h,v 1.75 2009-01-20 13:15:03 gertjan Exp $ */
#ifndef __ARVOO_ORLANDO_TYPS__
#define __ARVOO_ORLANDO_TYPS__

/*
   Orlando types
   
   Not all functions/types can be used for all Orlando 
   models. This is specified by next abbreviations:

   CL: Orlando Camera Link
   An: Orlando Analog

*/
#ifdef _TMS320C6X
   typedef unsigned char   ArvCHAR;
   typedef ArvCHAR*        ArvPCHAR;
   typedef unsigned char   ArvBYTE;
   typedef ArvBYTE         ArvBOOL;
   typedef unsigned short  ArvWORD;
   typedef unsigned int    ArvDWORD;
   typedef signed int      ArvSDWORD;
   typedef void*           ArvPVOID;
   typedef float           ArvFLOAT;
   #define  ArvFALSE      ((ArvBOOL)0)
   #define  ArvTRUE       ((ArvBOOL)1)

#else /* _TMS320C6X */
   #ifndef __ARVOO_DRIVER_TYPES__
      #include "arvtyp.h"
   #endif   
#endif /* !_TMS320C6X */

#define ORL_MAX_ENUM       0x7fffffff

#define HOST_MAX_CAPT_BUFFS   32UL     /* CL|An */

typedef enum _OrlRet
{
   ORL_RET_SUCCESS = 0,
   ORL_RET_ERROR,
   ORL_RET_DRV_ERROR,
   ORL_RET_DRV_TIMEOUT,
   ORL_RET_STRUCT_SIZE_INVALID,
   ORL_RET_PARM,
   ORL_RET_NOT_FOUND,
   ORL_RET_PMEM_ALLOC_FAILED,
   ORL_RET_NOT_READY,
   ORL_RET_INSUFFICIENT_BUFFER,
   ORL_RET_DRV_BUSY,
   ORL_RET_NOT_SUPPORTED,
   ORL_RET_FILE_IO_ERROR,

   NUM_ORL_RET
} OrlRet;

/*********************************************************************************/
/*                                                                               */
/*              Orlando CameraLink                                               */
/*                                                                               */
/*********************************************************************************/


/*********************************************************************************
          CL Serial/UART
*********************************************************************************/

typedef enum OrlBaudrateEnum
{
   ORL_BR_9600,
   ORL_BR_19200,
   ORL_BR_38400,
   ORL_BR_57600,
   ORL_BR_115200,
   ORL_BR_230400,
   ORL_BR_460800,
   ORL_BR_921600,
   NUM_ORL_BR,
   ORL_BR_UNKNOWN = ORL_MAX_ENUM
} OrlBaudrate;

/* Sources of Serial communication */
typedef enum OrlSerialSrcEnum
{
   ORL_SERSRC_NONE = 0,
   ORL_SERSRC_UART_TFG,                /* Uart write, Tx, TFG */
   ORL_SERSRC_RS232_TFG,               /* RS232 hdr, TFG */ 
   ORL_SERSRC_CL_SERTFG,               /* CL SERTFG */

   NUM_ORL_SERSRC,
   ORL_SERSRC_UNKNOWN = ORL_MAX_ENUM
} OrlSerialSrc;

typedef struct OrlSerialSettStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlSerialSrc   cl_sertc;            /* ORL_SERSRC_NONE, ORL_SERSRC_UART_TFG or ORL_SERSRC_RS232_TFG */
   OrlSerialSrc   uart_to_host;        /* ORL_SERSRC_NONE, ORL_SERSRC_RS232_TFG or ORL_SERSRC_CL_SERTFG */
   OrlSerialSrc   rs232_out;           /* ORL_SERSRC_NONE, ORL_SERSRC_CL_SERTFG or ORL_SERSRC_UART_TFG */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlSerialSett, *PSOrlSerialSett;

typedef struct OrlUartSettStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlBaudrate    baudrate;            /* baudrate (bits per sec) */
   ArvDWORD       reserved[6];         /* should be 0 */
} SOrlUartSett, *PSOrlUartSett;

#define ORL_UART_ERR_NONE  0x0000      /* success */
#define ORL_UART_ERR_PE    0x0010      /* parity error */
#define ORL_UART_ERR_FE    0x0020      /* framing error */
#define ORL_UART_ERR_OR_HW 0x0040      /* overrun in Uart buff */
#define ORL_UART_ERR_OR_SW 0x1000      /* overrun in sw buff */
#define ORL_UART_ERR_TO    0x2000      /* timeout */
#define ORL_UART_ERR_DRV   0x4000      /* error, low level */

typedef struct OrlUartReadWriteStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvPVOID       buffer;              /* [in/out] buffer with data if tx, buffer to receive data if rx */
   ArvDWORD       nbytes_to_trans;     /* [in] write: #bytes to send, read: req of rx #bytes */
   ArvDWORD       nbytes_trans;        /* [out] #bytes transferred */
   ArvDWORD       timeout;             /* [in] timeout in ms */
   ArvDWORD       err;                 /* [out] error bits ORL_UART_ERR_xxx */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlUartReadWrite, *PSOrlUartReadWrite;

/*********************************************************************************
          CL video acquisition
*********************************************************************************/

#define ORL_MAX_ROI_X         0xFFF0   /* CL, ROI: max x offset (pixels) */
#define ORL_MAX_ROI_Y         0xFFF0   /* CL, ROI: max y offset (lines) */
#define ORL_MAX_ROI_W         0xFFFE   /* CL, ROI: max width (pixels) */
#define ORL_MAX_ROI_H         0xFFFE   /* CL, ROI: max height (lines) */

typedef enum OrlCamScanEnum
{
   ORL_CAM_SCANTYPE_LINE = 0,          /* line scan camera */
   ORL_CAM_SCANTYPE_AREA,              /* area scan camera */

   NUM_ORL_CAM_SCANTYPE,
   ORL_CAM_SCANTYPE_UNKNOWN = ORL_MAX_ENUM
} OrlCamScanType;

typedef enum OrlCamColorEnum
{
   ORL_CAM_COLORTYPE_MONOCHROME,       /* monochrome video camera */
   ORL_CAM_COLORTYPE_RGB,              /* RGB video camera */
// ORL_CAM_COLORTYPE_BAYER,

   NUM_ORL_CAM_COLORTYPE,
   ORL_CAM_COLORTYPE_UNKNOWN = ORL_MAX_ENUM
} OrlCamColorType;

typedef enum OrlCamNumTapsEnum
{
   ORL_CAM_NTAP_1 = 0,                 /* one CameraLink tap */
   ORL_CAM_NTAP_2,                     /* two CameraLink taps */
   ORL_CAM_NTAP_3,                     /* three CameraLink taps */

   NUM_ORL_CAM_NTAP,
   ORL_CAM_NTAP_UNKNOWN = ORL_MAX_ENUM
} OrlCamNumTaps;

typedef enum OrlCamNBitsPixelEnum
{
   ORL_NBITS_PIXEL_8 = 0,              /* 8-bit/pixel */
   ORL_NBITS_PIXEL_10,                 /* 10-bit/pixel */
   ORL_NBITS_PIXEL_12,                 /* 12-bit/pixel */
   ORL_NBITS_PIXEL_14,                 /* 14-bit/pixel */
   ORL_NBITS_PIXEL_16,                 /* 16-bit/pixel */
   ORL_NBITS_PIXEL_RGB24,              /* 24-bit RGB/pixel, always 1 tap */

   NUM_NBITS_PIXEL,
   ORL_NBITS_PIXEL_UNKNOWN = ORL_MAX_ENUM
} OrlNBitsPixel;

typedef enum OrlPixelFormatEnum
{
   ORL_PIXFRMT_YCBCR422 = 0,           /* YCbCr 4:2:2 format, 1 plane */
   ORL_PIXFRMT_YCBCR422P,              /* YCbCr 4:2:2 format, 3 planes */
   ORL_PIXFRMT_Y,                      /* Y only (grey), 8 bit/pixel */
   ORL_PIXFRMT_RGB565,                 /* RGB565, 16 bit/pixel */
   ORL_PIXFRMT_RGB888,                 /* RGB888, 24 bit/pixel */
   ORL_PIXFRMT_Y10,                    /* Y only (grey), 10 bit/pixel, uses 2 byte */
   ORL_PIXFRMT_Y12,                    /* Y only (grey), 12 bit/pixel, uses 2 byte */
   ORL_PIXFRMT_Y14,                    /* Y only (grey), 14 bit/pixel, uses 2 byte */
   ORL_PIXFRMT_Y16,                    /* Y only (grey), 16 bit/pixel */

   NUM_ORL_PIXFRMT,
   UNKNOWN_ORL_PIXFRMT  
} OrlPixelFormat;

typedef enum OrlTriggerSrcEnum
{
   ORL_TRIGSRC_SW = 0,                 /* trigger by software (ORLF_xxx_SWTRIG) */
   ORL_TRIGSRC_LVAL_RISING,            /* CameraLinks LVAL rising edge */
   ORL_TRIGSRC_LVAL_FALLING,           /* CameraLinks LVAL falling edge */
   ORL_TRIGSRC_FVAL_RISING,            /* CameraLinks FVAL rising edge */
   ORL_TRIGSRC_FVAL_FALLING,           /* CameraLinks FVAL falling edge */
   ORL_TRIGSRC_TIMER0_RISING,          /* Timer 0 output rising edge */
   ORL_TRIGSRC_TIMER0_FALLING,         /* Timer 0 output falling edge */
   ORL_TRIGSRC_TIMER1_RISING,          /* Timer 1 output rising edge */
   ORL_TRIGSRC_TIMER1_FALLING,         /* Timer 1 output falling edge */
   ORL_TRIGSRC_GPIN0_RISING,           /* General Purpose 0 input rising edge */
   ORL_TRIGSRC_GPIN0_FALLING,          /* General Purpose 0 input falling edge */
   ORL_TRIGSRC_GPIN1_RISING,           /* General Purpose 1 input rising edge */
   ORL_TRIGSRC_GPIN1_FALLING,          /* General Purpose 0 input falling edge */
   ORL_TRIGSRC_START_OF_ROI,           /* Video Acquire reach start of ROI */
   ORL_TRIGSRC_END_OF_ROI,             /* Video Acquire reach end of ROI */

   NUM_ORL_TRIGSRC,
   UNKNOWN_ORL_TRIGSRC = ORL_MAX_ENUM
} OrlTriggerSrc;

typedef struct OrlRoiStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       xoffs;               /* x offset: num pixels to skip */
   ArvDWORD       yoffs;               /* y offset: num lines to skip */
   ArvDWORD       width;               /* x: num pixels to acquire */
   ArvDWORD       height;              /* y: num lines to acquire */
} SOrlRoi, *PSOrlRoi, SOrlRect, *PSOrlRect;

#define  ORL_ACQ_FLAG_USE_DVAL      0x0001   /* use CameraLinks DVAL signal */
#define  ORL_ACQ_FLAG_AUTO_PITCH    0x0002   /* rounds the ROI-width to multiply of 4 bytes */

typedef struct OrlVideoAcqSettingsStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
                                       
   /* camera properties */             
   OrlCamScanType cam_scantype;        /* [in] area/line scan */
   OrlCamColorType cam_coltype;        /* [in] monochrom/rgb */
   OrlCamNumTaps  cam_ntaps;           /* [in] #taps */
   OrlNBitsPixel  cam_nbits_pixel;     /* [in] #bits/pixel */
                                       
   /* acquire settings */              
   SOrlRoi        roi;                 /* [in] Region Of Interest */
   OrlTriggerSrc  start_trig;          /* [in] start trigger */
   OrlTriggerSrc  stop_trig;           /* [in] stop trigger */
   ArvDWORD       flags;               /* [in] ORL_ACQ_FLAG_xxxx */
                                       
   /* needed sizes */                         
   ArvDWORD       acq_bytes_line;      /* [out] #bytes/line */                                     
   ArvDWORD       buff_size_pages;     /* [out] #pages needed for complete frame */

   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlVideoAcqSett, *PSOrlVideoAcqSett;

#if 0
typedef enum OrlBayerPatternEnum
{
	ORL_BAYER_GB_RG = 0,
	ORL_BAYER_GR_BG,
	ORL_BAYER_RG_GB,
	ORL_BAYER_BG_GR,

	NUM_ORL_BAYER,
	UNKNOWN_ORL_BAYER = ORL_MAX_ENUM
} OrlBayerPattern;

/* aanroepen na INIT_VIDEO_ACQ, voor INIT_VIDEO_BUFF 
   ADD_BAYER_TO_RGB_CONVERSION

*/
typedef struct OrlBayerConvStruct {
   ArvDWORD size;
   OrlBayerPattern bayer;              /* input format */
   OrlPixelFormat format;              /* output format, only ORL_PIXFRMT_RGB888 accepted, yet */
   ArvDWORD       r_gain;              /* [0..255] 0: 1/64, 64: 1, 255: 4 */
   ArvDWORD       g_gain;
   ArvDWORD       b_gain;
   ArvDWORD       bytes_line;          /* [out] #bytes/line of resulting image */
   ArvDWORD       buff_size_pages;     /* [out] #pages per buffer of resulting image */
   ArvDWORD       reserved[2];
} SOrlBayerConv, *PSOrlBayerConv;
#endif

typedef struct OrlAcquireEnable
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       start;               /* 1: start, 0: stop */
   ArvDWORD    reserved[2];            /* should be 0 */
} SOrlAcquireEnable, *PSOrlAcquireEnable;

typedef struct OrlCLVideoInfo
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       clk_100khz;          /* measured CameraLink clock in 100KHz */
   ArvDWORD       nstrb;               /* measured #CL-Strobes per line */
   ArvDWORD       nlines;              /* measured  #Lines per frame */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlCLVideoInfo, *PSOrlCLVideoInfo;

/*********************************************************************************
          CL video buffering
*********************************************************************************/

typedef enum _OrlAcqMemMode
{
   ORL_ACQMEM_FIFO = 0,                /* FIFO buffer */
   ORL_ACQMEM_CIRCULAR_STOP_AT_SWTRIG, /* circular buffer, filling stops at SW trigger (ORLF_xxx_SWTRIG) */
   ORL_ACQMEM_CIRCULAR_STOP_AT_GP0,    /* circular buffer, filling stops at GP0 interrupt */
   ORL_ACQMEM_CIRCULAR_STOP_AT_GP1,    /* circular buffer, filling stops at GP1 interrupt */

   NUM_ORL_ACQMEM,
   UNKNOWN_ACQMEM = ORL_MAX_ENUM
} OrlAcqMemMode;

#define ORL_PAGE_SIZE_BYTES      0x1000   /* #bytes per host page */
#define ORL_PAGE_SIZE_KBYTES     0x4      /* 4Kbyte a page (4096 bytes) */

typedef struct OrlVideoBuffSettingsStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       vid_elmnt_size_pages;/* Element size (#pages) */
   ArvDWORD       acq_fifo_depth_ve;   /* acqff_depth: Acq FIFO dept (#elements) */
   ArvDWORD       acq_fifo_thresh_ve;  /* acqff_thresh: Acq FIFO Threshold (#elements) */
   OrlAcqMemMode  acq_fifo_mode;       /* memmode: Acq FIFO mode */
   ArvDWORD       hbuff_size_ve;       /* size of a host buffer (#elements) */
   ArvDWORD       host_fifo_depth_hbuffs; /* depth of Host FIFO (#host buffers) */
	ArvDWORD       hbuff_elmnt_size_pages; /* ipv vid_elmnt_size_pages */
   ArvDWORD       reserved[1];         /* should be 0 */
} SOrlVideoBuffSett, *PSOrlVideoBuffSett;

typedef ArvDWORD     VidElType;
#define ORL_ACQ_TYPE_EMPTY       0x0000
#define ORL_ACQ_TYPE_FIRST       0x0001   /* first element */
#define ORL_ACQ_TYPE_MIDDLE      0x0002
#define ORL_ACQ_TYPE_LAST        0x0004   /* last element */
#define ORL_ACQ_TYPE_COMPLETED   0x0008   /* LAST: full frame retrieved, FEN'd */
#define ORL_ACQ_TYPE_CORRUPTED   0x1000   /* LAST: not completed frame  SVidDataCorrupt */

typedef ArvDWORD     VidElFlags;
#define ORL_VIDFLAG_ERR_HBUFF_OVL      0x0001

typedef struct OrlHostBuffInfo
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       buffernr;            /* [in] buffer index */
   ArvPVOID       buffer;              /* [out] pointer to the buffer */
   ArvDWORD       numbytes;            /* [out] buffer size in bytes*/
   ArvDWORD       w;                   /* [out] width */
   ArvDWORD       h;                   /* [out] height */
   ArvDWORD       pitch;               /* [out] pitch */
   OrlNBitsPixel  bitspix;             /* [out] #bits/pixel */
   OrlCamColorType   coltype;          /* [out] monochr/RGB */
   ArvDWORD       nbytespix;           /* [out] #bytes/pixel */

   /* content of buffer: */
   VidElType      cont_type;           /* [out] bitwise ORed ORL_ACQ_TYPE_xxx */
   VidElFlags     cont_flags;          /* [out] bitwise ORed ORL_VIDFLAG_ERR_xxx */
   ArvDWORD       cont_nbytes;         /* [out] #bytes copied it buffer, so far */
   ArvDWORD       cont_framecnt;       /* [out] frame counter */
   ArvDWORD       cont_nstrb;          /* [out] #strobes/line */
   ArvDWORD       cont_nlines;         /* [out] lines/frame */

   ArvDWORD       num_in_hfifo;        /* [out] #buffers in host FIFO */

   OrlPixelFormat format;              /* [out] */
   ArvDWORD       reserved[1];         /* should be 0 */
} SOrlHostBuffInfo, *PSOrlHostBuffInfo;

typedef struct OrlHostBuffEmpty
{
   ArvDWORD       size;
   ArvDWORD       buffernr;            /* buffer index */
   ArvDWORD       reserved;            /* [in] should be 0 */
} SOrlHostBuffEmpty, *PSOrlHostBuffEmpty;

typedef struct OrlVideoBuffFillLevel
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       acq_fifo_fill_ve;    /* [out] #VE in on boards acq. FIFO */
   ArvDWORD       pci_fifo_fill_ve;    /* [out] #VE in on board PCI-transfer FIFO */
   ArvDWORD       host_fifo_fill_hbuffs; /* [out] #VE in host FIFO */
   ArvDWORD       reserved;            /* [in] should be 0 */
} SOrlVideoBuffFillLevel, *PSOrlVideoBuffFillLevel;

typedef struct OrlNullStruct
{
   ArvDWORD       size;
   ArvDWORD       res[6];
} SOrlNullStruct, *PSOrlNullStruct;

/*********************************************************************************
          CL,An interrupt handling
*********************************************************************************/

typedef ArvDWORD OrlInterrType;
#define ORL_INT_FRAME_GRAB    0x00000001UL /* CL,    VE acquired at board */
#define ORL_INT_FRAME_HOST    0x00000008UL /* CL,    VE transf. to host */
#define ORL_INT_MESSAGE       0x00000020UL /* CL,    message of board */
#define ORL_INT_CLVIDEO       0x00000040UL /* CL,    CameraLinks pixelclock is changed */
#define ORL_INT_GP0           0x00000080UL /* CL|An, GP input 0 interrupt */
#define ORL_INT_GP1           0x00000100UL /* CL|An, GP input 1 interrupt */
#define ORL_INT_ADC0          0x00000200UL /* An,    ADC 0 status changed (of input module 0) */
#define ORL_INT_ADC1          0x00000400UL /* An,    ADC 1 status changed (of input module 1) */

/* message/error bits */
#define ORL_ERR_FPGA_VIDFIFO_OV     0x00000001UL /* FPGAs video FIFO overflow */
#define ORL_ERR_ACQ_VIDFIFO_OV      0x00000002UL /* DSPs video FIFO overflow */
#define ORL_ERR_HOST_VIDFIFO_OV     0x00000004UL /* Host video FIFO overflow */
#define ORL_ERR_PRCSS_VIDFIFO_OV	0x00000008UL /* Processing video FIFO overflow */
#define ORL_MESS_ACQ_VIDFIFO_FULL   0x00010001UL /* Mess: FIFO full, ready */

#define OrlMessSevIsErr( m )  ((((m) & 0x000f0000) >> 16) == 0 ? ArvTRUE : ArvFALSE)
#define OrlMessSevIsMess( m ) ((((m) & 0x000f0000) >> 16) == 1 ? ArvTRUE : ArvFALSE)

typedef struct sSOrlCbParm
{
   ArvDWORD       size;
   OrlInterrType  inttype;             /* ORL_INT_xxxx, OR'ed */
   ArvDWORD       int_not_handled_yet; /* #interrupts not handled yet */
   ArvPVOID       cbparm;

   struct /* ORL_INT_FRAME_GRAB */
   {
      ArvDWORD       num_interr;
   } frame_grab;
   struct /* ORL_INT_FRAME_HOST */
   {
      ArvDWORD       num_interr;
   } frame_host;
   struct /* ORL_INT_MESSAGE */
   {
      ArvDWORD       mess;             /* ORL_ERR_xxxx or ORL_MESS_xxxx */
   } message;
   struct /* ORL_INT_CLVIDEO */
   {
      ArvDWORD       num_interr;
   } clvideo;
   struct /* ORL_INT_GP0 */
   {
      ArvDWORD       num_interr;
   } gp0;
   struct /* ORL_INT_GP1 */
   {
      ArvDWORD       num_interr;
   } gp1;

	ArvDWORD			opt;
   ArvDWORD       reserved[1];         /* should be 0 */
} SOrlCbParm, *PSOrlCbParm;

typedef ArvDWORD  (*OrlCbFunc)( PSOrlCbParm parm );

/*********************************************************************************
          CL timer
*********************************************************************************/

typedef enum OrlTimerIdEnum
{
   ORL_TIMERID_0 = 0,
   ORL_TIMERID_1,

   NUM_ORL_TIMERID,
   UNKNOWN_ORL_TIMERID = ORL_MAX_ENUM
} OrlTimerId;

/* Timer options */
#define ORL_TIMEROPT_DISABLE        0x0000UL    /* disable timer */
#define ORL_TIMEROPT_ENABLE         0x0001UL    /* enable timer  */
#define ORL_TIMEROPT_MODE_ONESHOT   0x0000UL    /* one-shot */
#define ORL_TIMEROPT_MODE_CONT      0x0010UL    /* continuous */
#define ORL_TIMEROPT_SEQ_LOWHIGH    0x0000UL    /* low high sequence */
#define ORL_TIMEROPT_SEQ_HIGHLOW    0x0020UL    /* high low sequence */

#define ORL_TIMER_CYCLE_FREQ              (66.0E6)       /* timer freq: 66MHz */
#define ORL_TIMER_CYCLE_TIME              (1.0/66.0E6)   /* timer period: 15.15 ns */

#define ORL_TIMER_MAX_CYCLES              0xFFFFFFFFUL
#define ORL_TIMER_MIN_TIME                ORL_TIMER_CYCLE_TIME
#define ORL_TIMER_MAX_TIME                ((ORL_TIMER_MAX_CYCLES)/ORL_TIMER_CYCLE_FREQ+ORL_TIMER_CYCLE_TIME)   /* 65.075 seconds */

typedef struct OrlTimerStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlTimerId     timerid;             /* Id of timer */
   OrlTriggerSrc  start_trig;          /* start trigger */
   OrlTriggerSrc  stop_trig;           /* stop trigger */
   ArvDWORD       opt;                 /* ORL_TIMEROPT_xxx, ORed */
   ArvDWORD       low_cycles;          /* low level, timer #cycles, 0 = 1 cycle */
   ArvDWORD       high_cycles;         /* high level, timer #cycles, 0 = 1 cycle */
   ArvDWORD    reserved[2];            /* should be 0 */
} SOrlTimer, *PSOrlTimer;

/*********************************************************************************
          CL software triggers
*********************************************************************************/

typedef enum OrlSWTrigEnum
{
   ORL_SWTRIG_START = 0,               /* Start SW trigger */
   ORL_SWTRIG_STOP,                    /* Stop SW trigger */

   NUM_ORL_SWTRIG,
   UNKNOWN_ORL_SWTRIG = ORL_MAX_ENUM
} OrlSWTrig;


typedef struct OrlTimerSWTrigStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlTimerId     timerid;             /* id of timer */
   OrlSWTrig      trig;                /* SW trigger */
   ArvDWORD    reserved[2];            /* should be 0 */
} SOrlTimerSWTrig, *PSOrlTimerSWTrig;

/**************************************************************************/
/*                                                                        */
/*              Orlando analog                                            */
/*                                                                        */
/**************************************************************************/

typedef enum OrlModuleEnum
{
   /* analog video in modules (ADC) */
   ORL_MODULE_VIDEO_IN0 = 0,
   ORL_MODULE_VIDEO_IN1 = 1,
   
   /* analog video out modules (DAC) */
   ORL_MODULE_VIDEO_OUT0 = 2,
   ORL_MODULE_VIDEO_OUT1 = 3,

   /* board to host modules */
   ORL_MODULE_BRD2HOST0 = 4,           /* board to host module 0 */  
   ORL_MODULE_BRD2HOST1 = 5,           /* board to host module 1 */
   
   /* host to board modules */
   ORL_MODULE_HOST2BRD0 = 6,           /* host to board module 0 */
   ORL_MODULE_HOST2BRD1 = 7,           /* host to board module 1 */
   ORL_MODULE_HOST2BRD2 = 8,           /* host to board module 2 */
   ORL_MODULE_HOST2BRD3 = 9,           /* host to board module 3 */
   ORL_MODULE_HOST2BRD4 = 10,          /* host to board module 4 */
   ORL_MODULE_HOST2BRD5 = 11,          /* host to board module 5 */

   /* video fusion modules */
   ORL_VMODULE_FUSION0 = 12,					
   ORL_VMODULE_FUSION1 = 13,
   ORL_VMODULE_FUSION2 = 14,
   ORL_VMODULE_FUSION3 = 15,
   ORL_VMODULE_FUSION4 = 16,
   ORL_VMODULE_FUSION5 = 17,
   ORL_VMODULE_FUSION6 = 18,
   ORL_VMODULE_FUSION7 = 19,
   
   /* overlay text modules */
   ORL_VMODULE_OVL_TEXT0 = 20,
   ORL_VMODULE_OVL_TEXT1 = 21,
   ORL_VMODULE_OVL_TEXT2 = 22,
   ORL_VMODULE_OVL_TEXT3 = 23,
   
   NUM_ORL_MODULE,

   UNKNOWN_ORL_MODULE
} OrlModule;

typedef enum OrlPortEnum
{
   ORL_PORT_CVBS0 = 0,                 /* CVBS 0, IN|OUT */
   ORL_PORT_CVBS1,                     /* CVBS 1, IN|OUT */
   ORL_PORT_CVBS2,                     /* CVBS 2, IN */
   ORL_PORT_CVBS3,                     /* CVBS 3, IN */
   ORL_PORT_CVBS4,                     /* CVBS 4, IN */
   ORL_PORT_YC0,                       /* YC 0, IN|OUT */
   ORL_PORT_YC1,                       /* YC 1, IN */
   ORL_PORT_RGB0,                      /* RGB, IN */

   /* only output: */
   ORL_PORT_CVBS_DUAL,                 /* CVBS 0 and 1, OUT */
   ORL_PORT_CVBS_YC,                   /* CVBS 1 and YC 0, OUT */
   ORL_PORT_RGB0_SYNC,                 /* RGB with sync, OUT */

   NUM_ORL_PORT
} OrlPort;

typedef enum OrlVideoTimeEnum
{
   ORL_VTIME_NTSC = 0,                 /* NTSC video */
   ORL_VTIME_PAL,                      /* PAL video */
   ORL_VTIME_SECAM,                    /* SECAM video */
   NUM_ORL_VTIME,
   UNKNOWN_ORL_VTIME
} OrlVideoTime;

typedef enum OrlVideoGainModeEnum
{
   ORL_VGAIN_AUTO = 0,                 /* automatic video gain */
   ORL_VGAIN_FIXED,                    /* fixed video gain */
   ORL_VGAIN_FIXED_DEFAULTS,           /* video gain fixed at 0dB */
   NUM_ORL_VGAIN
} OrlVideoGainMode;

typedef enum OrlVideoBCSModeEnum
{
   ORL_VBCS_NORMAL = 0,                /* Brightness, Contrast, Saturation adjustable */
   ORL_VBCS_DEFAULTS,                  /* BCS levels fixed at ITU levels */
   NUM_ORL_VBCS
} OrlVideoBCSMode;

typedef enum OrlFieldArrangeEnum
{
   ORL_FLD_INTERLACED = 0,             /* odd/even lines interlaced */
   ORL_FLD_DUAL,                       /* odd/even fields 'interlaced' */
   ORL_FLD_SINGLE,                     /* odd and even fields in own buffer */
   NUM_ORL_FLD,
   UNKNOWN_ORL_FLD
} OrlFieldArrange;

typedef enum OrlStreamStateEnum
{
   ORL_STRSTATE_UNKNOWN = 0,

   ORL_STRSTATE_NOT_INIT,              /* stream not initialized */
   ORL_STRSTATE_INIT,                  /* stream init, no buffers alloc'd */
   ORL_STRSTATE_READY_TO_RUN,          /* stream disabled (stream init & has buffs) */
   ORL_STRSTATE_RUN,                   /* stream enabled */

   NUM_ORL_STRSTATE
} OrlStreamState;

/* flags */
#define ORL_FLAG_MIRROR      0x00000001UL /* mirror y-axis */
#define ORL_FLAG_POWER_DOWN  0x00000002UL /* power down */
/*#define ORL_FLAG_COLOR_BAR   0x00000004UL *//* show color bar */
#define ORL_FLAG_NO_COLOR    0x00000008UL /* only b/w */
#define ORL_FLAG_INV_FID     0x00000010UL /* invert field-id */
//#define ORL_FLAG_HBUFF_MEM_BY_USER 0x00000020UL
#define ORL_FLAG_NOT_WAIT_FOR_NSYNC 0x00000040UL

/* Video stream handle */
typedef ArvDWORD OrlStrHandle;
#define ORL_INVALID_STRHANDLE 0xffffffffUL
#define ORLAN_MAX_STREAMS  4
#define ORLAN_MAX_PLANES   3

/* analog video standard dimensions */
#define ORL_PAL_WIDTH 720
#define ORL_PAL_FIELD_HEIGHT 288
#define ORL_PAL_FRAME_HEIGHT (ORL_PAL_FIELD_HEIGHT << 1)

#define ORL_NTSC_WIDTH 720
#define ORL_NTSC_FIELD_HEIGHT 242
#define ORL_NTSC_FRAME_HEIGHT (ORL_NTSC_FIELD_HEIGHT << 1)

/* overlay dimensions */
#define ORL_OVL_WIDTH         360
#define ORL_OVL_PAL_HEIGHT    288
#define ORL_OVL_NTSC_HEIGHT   242

/*********************************************************************************
          Orlando analog: analog input and output modules
*********************************************************************************/

typedef struct OrlVideoInputSettStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* input module */
   OrlPort        port;                /* video input port */
   OrlVideoTime   vtime;               /* PAL/NTSC */
   OrlVideoGainMode gain_mode;         /* fixed or auto gain */
   ArvDWORD       gain_y_cvbs;         /* [0..511] fixed gain for CVBS or Y */
   ArvDWORD       gain_c;              /* [0..511] fixed gain for C */
   OrlVideoBCSMode bcs_mode;           /* BCS at default or adjustable */
   ArvDWORD       brightness;          /* [0..255] */
   ArvSDWORD      contrast;            /* [-128..127] */
   ArvSDWORD      saturation;          /* [-128..127] */
   ArvSDWORD      hue;                 /* [-128..127] (not avail if RGB)*/
   ArvDWORD       flags;               /* bitwise ORL_FLAG_MIRROR, ORL_FLAG_POWER_DOWN, ORL_FLAG_INV_FID */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlVideoInputSett, *PSOrlVideoInputSett;

#define ORL_ADC_STAT_DCSTD0   0x00000001  /* 00: no colour  01: NTSC */
#define ORL_ADC_STAT_DCSTD1   0x00000002  /* 10: PAL        11: SECAM */
#define ORL_ADC_STAT_WIPA     0x00000004  /* White Peak control activated */
#define ORL_ADC_STAT_GLIMB    0x00000008  /* gain value at minimum */
#define ORL_ADC_STAT_GLIMT    0x00000010  /* gain value at maximum */
#define ORL_ADC_STAT_SLTCA    0x00000020  /* slow time constant active */
#define ORL_ADC_STAT_HLCK     0x00000040  /* HPLL not locked */
#define ORL_ADC_STAT_RDCAP    0x00000100  /* Ready for capture */
#define ORL_ADC_STAT_COPRO    0x00000200  /* Copy protection, Macrovision */
#define ORL_ADC_STAT_COLSTR   0x00000400  /* Macrovision Color Stripe scheme */
#define ORL_ADC_STAT_TYPE3    0x00000800  /* Macrovision copy prot. type 3 */
#define ORL_ADC_STAT_FIDT     0x00002000  /* 60Hz field rate, otherwise 50Hz */
#define ORL_ADC_STAT_HLVLN    0x00004000  /* h-sync or v-sync lost */
#define ORL_ADC_STAT_INTL     0x00008000  /* interlaced video */

typedef struct OrlVideoInputStatStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* [in] input module */
   ArvDWORD       decoder_stat;        /* [out] bitwise  ORL_ADC_STAT_xxx */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlVideoInputStat, *PSOrlVideoInputStat;

typedef struct OrlVideoOutputSettStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* output module */
   OrlPort        port;                /* output port */
   OrlVideoTime   vtime;               /* PAL or NTSC video timing */
   ArvDWORD       flags;               /* bitwise ORL_FLAG_POWER_DOWN, ORL_FLAG_NO_COLOR */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlVideoOutputSett, *PSOrlVideoOutputSett;

/*********************************************************************************
   Orlando analog: video streams
*********************************************************************************/

typedef struct OrlPlaneSize
{
   ArvDWORD       buff_size;           /* size of buffer in bytes */
   ArvDWORD       npages;              /* num. of host memory pages */
   ArvDWORD       hor_bytes;           /* (content) #bytes/line */
   ArvDWORD       bytes_used;          /* (content) #bytes used in buffer */
} SOrlPlaneSize, *PSOrlPlaneSize;

typedef struct OrlPlaneBuff
{
   SOrlPlaneSize  sz;
   ArvPVOID       pbuff;               /* ptr to buffer */
} SOrlPlaneBuff, *PSOrlPlaneBuff;

typedef struct OrlVideoInputToHostStream
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   SOrlVideoInputSett input;           /* [in] input module */
   SOrlRect       roi;                 /* [in] Region Of Interest */
   ArvDWORD       scale_width;         /* [in] #pixels/line after scaler */
   ArvDWORD       scale_height;        /* [in] lines/frame after scaler */
   OrlFieldArrange fieldarrange;       /* [in] output fields arrange */
   OrlPixelFormat format;              /* [in] output pixel format */
   ArvDWORD       reserved1;           /* [in] should be 0 */
   ArvDWORD       inp_buff_size;       /* [out] needed on board buffer size */
   ArvDWORD       outp_buff_size;      /* [out] needed on board buffer size */
   ArvDWORD       host_numplanes;      /* [out] #planes */
   SOrlPlaneSize  host_pl0;            /* [out] needed host buffer size of plane 0 */
   SOrlPlaneSize  host_pl1;            /* [out] needed host buffer size of plane 1 */
   SOrlPlaneSize  host_pl2;            /* [out] needed host buffer size of plane 2 */
   OrlStrHandle   hstream;             /* [out] stream handle */
   ArvDWORD       reserved2[2];        /* [in] should be 0 */
} SOrlVideoInputToHostStream, *PSOrlVideoInputToHostStream;

typedef struct OrlHostToVideoOutputStream
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   SOrlVideoOutputSett output;         /* [in] output module */
   SOrlRect       outp_region;         /* [in/out] input and output dimensions */
   OrlFieldArrange fieldarrange;       /* [in] field/frame of input iamge */
   OrlPixelFormat format;              /* [in] pixel format of input image */
   ArvDWORD       reserved1;           /* [in] should be 0 */
   ArvDWORD       inp_buff_size;       /* [out] needed onboard buffer size */
   ArvDWORD       outp_buff_size;      /* [out] needed onboard buffer size */
   ArvDWORD       host_numplanes;      /* [out] needed #planes at host */
   SOrlPlaneSize  host_pl0;            /* [out] needed host plane 0 size */
   SOrlPlaneSize  host_pl1;            /* [out] needed host plane 1 size*/
   SOrlPlaneSize  host_pl2;            /* [out] needed host plane 2 size*/
   OrlStrHandle   hstream;             /* [out] stream handle */
   ArvDWORD       reserved2[2];        /* [in] should be 0 */
} SOrlHostToVideoOutputStream, *PSOrlHostToVideoOutputStream;

typedef struct OrlInpToOutputStream
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   SOrlVideoInputSett input;           /* [in] input module */
   SOrlVideoOutputSett output;         /* [in] output module */
   SOrlRect       roi;                 /* [in] Region Of Interest at input */
   SOrlRect       outp_region;         /* [in] (scaled) output region */
   ArvDWORD       reserved1;           /* [in] should be 0 */
   ArvDWORD       inp_buff_size;       /* [out] needed buffer size */
   ArvDWORD       outp_buff_size;      /* [out] needed buffer size */
   OrlStrHandle   hstream;             /* [out] stream handle */
   ArvDWORD       reserved2[2];        /* [in] should be 0 */
} SOrlInpToOutputStream, *PSOrlInpToOutputStream;

typedef struct OrlStreamEnable
{
   ArvDWORD                size;       /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle            hstream;    /* [in] stream handle */
   ArvDWORD                enable;     /* [in] 0: disable, 1: enable stream */
   ArvDWORD                reserved;   /* [in] should be 0 */
} SOrlStreamEnable, *PSOrlStreamEnable, 
  SOrlFreeStrRes, *PSOrlFreeStrRes;

typedef struct OrlAllocBuff
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] stream handle */
   ArvDWORD       inp_onboard_buffs;   /* [in] #buffers on board at the video input */
   ArvDWORD       outp_onboard_buffs;  /* [in] #buffers on board at the video input */
   OrlAcqMemMode  onboard_mode;        /* [in] should be ORL_ACQMEM_FIFO(0) */
   ArvDWORD       reserved[4];         /* [in] should be 0 */
} SOrlAllocBuff, *PSOrlAllocBuff;

typedef struct OrlAllocPlHostBuff
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       numplanes;           /* [in] #planes (1 or 3) */
   SOrlPlaneBuff  plane0;              /* [in] size of plane 0 */
   SOrlPlaneBuff  plane1;              /* [in] size of plane 1 */
   SOrlPlaneBuff  plane2;              /* [in] size of plane 2 */
   ArvDWORD       flags;               /* [in] should be 0 */
   ArvDWORD       buffernr;            /* [out] buffer index of alloc'd buffer */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlAllocPlHostBuff, *PSOrlAllocPlHostBuff;

#define ORL_ANNOT_MAX_NUM_CHARS  16
typedef struct OrlPlHostBuffInfo
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       buffernr;            /* [in] buffer index */
   OrlStrHandle   hstream;             /* [out] stream handle rd/wr the buffer */
   ArvDWORD       numplanes;           /* [out] #planes */
   SOrlPlaneBuff  plane0;              /* [out] plane 0 info */
   SOrlPlaneBuff  plane1;              /* [out] plane 1 info */
   SOrlPlaneBuff  plane2;              /* [out] plane 2 info */
   ArvDWORD       width_pix;           /* [out] #pixels/line of image */
   ArvDWORD       height;              /* [out] #lines/frame of image */
   OrlFieldArrange fieldarrange;       /* [out] fiels arrange */
   OrlPixelFormat format;              /* [out] pixel format */
   ArvCHAR        annotstr[ORL_ANNOT_MAX_NUM_CHARS];/* [out] annotate string */
   OrlModule      module;
   ArvDWORD       reserved[1];         /* should be 0 */
} SOrlPlHostBuffInfo, *PSOrlPlHostBuffInfo;

typedef struct OrlFrmTransf
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] not used */
   ArvDWORD       buffernr;            /* [in] host buffer index */
   OrlModule      module;              /* [in]  host module */
   ArvDWORD       reserved[3];         /* should be 0 */
} SOrlFrmTransf, *PSOrlFrmTransf;

typedef struct OrlStreamInfo
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       streamix;            /* [in/out] video stream index (first is 0) */
   OrlStrHandle   hstream;             /* [in/out] video stream handle */
   ArvDWORD       request_is_hstream;  /* [in] stream index or handle requested */
   OrlModule      inp_module;          /* [out] input module of this stream */
   OrlModule      outp_module;         /* [out] output module of this stream */
   OrlStreamState stream_state;        /* [out] state of stream */
   ArvDWORD       reserved[4];
} SOrlStreamInfo, *PSOrlStreamInfo;

typedef struct OrlPCIStat
{
   ArvDWORD       size;						/* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       wr_mb_s;					/* [out] MByte/s of last board to host transfer */
   ArvDWORD       rd_mb_s;					/* [out] MByte/s of last host to board transfer */
   ArvDWORD       master_abort_count;	/* [out] #PCI master abort errors */
   ArvDWORD       target_abort_count;  /* [out] #PCI target abort errors */
   ArvDWORD       reserved[2];
} SOrlPCIStat, *PSOrlPCIStat;

/*********************************************************************************
          Orlando analog: overlay
*********************************************************************************/

typedef enum OrlFontTypeEnum
{
   ORL_STDFONT_8X8 = 0,                /* font size: 8x8 pixels */
   ORL_STDFONT_16X16 = 1,              /* font size: 16x16 pixels */
   ORL_STDFONT_24X24 = 2,              /* font size: 24x24 pixels */

   NUM_ORL_FONTTYPES
} OrlFontType;

typedef enum OrlFrmAnnotTypeEnum
{
   ORL_ANNOT_COUNTER = 0,              /* frame annotator is a counter */
   ORL_ANNOT_FRM_COUNTER,              /* frame annotator is a time-frame counter */

   NUM_ORL_ANNOT,
   ORL_ANNOT_UNKNOWN
} OrlFrmAnnotType;

typedef enum OrlOvlColorIxEnum 
{
   ORL_OVL_COLOR_TRANS = 0,            /* transparent (video) */
   ORL_OVL_COLOR_WHITE = 1,            /* various colors */
   ORL_OVL_COLOR_YELLOW = 2,
   ORL_OVL_COLOR_CYAN = 3,
   ORL_OVL_COLOR_GREEN = 4,
   ORL_OVL_COLOR_MAGENTA = 5,
   ORL_OVL_COLOR_RED = 6,
   ORL_OVL_COLOR_BLUE = 7,
   ORL_OVL_COLOR_BLACK = 8,
   ORL_OVL_COLOR_GREY1 = 9,            /* light grey */
   ORL_OVL_COLOR_GREY2 = 10,
   ORL_OVL_COLOR_GREY3 = 11,
   ORL_OVL_COLOR_GREY4 = 12,           /* very dark grey */
   ORL_OVL_COLOR_RES1 = 13,            /* reserved */
   ORL_OVL_COLOR_RES2 = 14,            /* reserved */
   ORL_OVL_COLOR_RES3 = 15,            /* reserved */
   NUM_ORL_OVL_COLORS
} OrlOvlColor;

typedef struct OrlFrmAnnotValue
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* video stream handle */
   ArvDWORD       val0;                /* if ORL_ANNOT_COUNTER: counter, if ORL_ANNOT_FRM_COUNTER: hours */
   ArvDWORD       val1;                /* min 0..59 */
   ArvDWORD       val2;                /* sec 0..59 */
   ArvDWORD       val3;                /* frm 0..24 (PAL) or 0..29 (NTSC)*/
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlFrmAnnotValue, *PSOrlFrmAnnotValue;

typedef struct OrlInitFrmAnnot
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] video stream handle */
   ArvDWORD       enable;              /* [in] annotate: 0=disable, 1=enable */
   OrlFrmAnnotType type;               /* [in] annotate type */
   SOrlFrmAnnotValue value;            /* [in] current annotate value */
   ArvDWORD       reserved[6];         /* [in] should be 0 */
} SOrlInitFrmAnnot,*PSOrlInitFrmAnnot;

typedef struct OrlAnnotView
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       enable;              /* annot.value in overlay: 0= disable, 1= enable */
   ArvDWORD       posrx;               /* right pos in pixels, ovl-coord */
   ArvDWORD       posty;               /* top pos in lines, ovlcoord */
   OrlFontType    font;                /* font (size) */
   /* fgcolor TODO (white)
      bgcolor      (black)
   */
   ArvDWORD    reserved[4];            /* [in] should be 0 */
} SOrlAnnotView, *PSOrlAnnotView;

typedef struct OrlUsrOvlView
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       width;               /* [out] pixs/line of overlay buffer */
   ArvDWORD       height;              /* [out] line/fr of overlay buffer */
   SOrlPlaneSize  plbuff;              /* [out] needed buff size */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlUsrOvlView, *PSOrlUsrOvlView;

typedef struct OrlInitOverlay
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] stream handle */
   ArvDWORD       enable;              /* [in] overlay enable(1) or disable(0) */
   SOrlAnnotView  vw_annot;            /* [in] sett for annotate in overlay */
   SOrlUsrOvlView vw_user;             /* [out] returns host overlay buffer size */
   ArvDWORD       reserved[4];         /* [in] should be 0 */
} SOrlInitOverlay, *PSOrlInitOverlay;

/*********************************************************************************
          Orlando analog: interrupts
*********************************************************************************/

typedef ArvDWORD OrlStrInterrType;
typedef ArvDWORD OrlModInterrType;

/* vstream interrupts */
#define ORL_STRINT_FRM_IN              0x00000001UL /* frame acquired */
#define ORL_STRINT_FRM_RDY_FOR_OUT     0x00000002UL /* frame ready for output */
#define ORL_STRINT_FRM_OUT             0x00000004UL /* frame out */
#define ORL_STRINT_FRM_SKIPPED         0x00000008UL /* frame skipped (not to out) */
#define ORL_STRINT_OVL_TRANSF_RDY      0x00000010UL /* host overlay buff copied to onboard */
#define ORL_STRINT_VSTRMASK            0x0000001fUL /* stream interrupts mask */

/* module interrupts */
/* bits: */
#define ORL_MODINT_FRM_IN_BIT          0
#define ORL_MODINT_FRM_RDY_FOR_OUT_BIT 1
#define ORL_MODINT_FRM_OUT_BIT         2
#define ORL_MODINT_FRM_SKIPPED_BIT     3
//#define ORL_MODINT_OVL_TRANSF_RDY_BIT  4
#define ORL_MODINT_RESERVED  4

/* mask: */
#define ORL_MODINT_FRM_IN              (1<<ORL_MODINT_FRM_IN_BIT)/*0x00000001UL*/ /* frame acquired */
#define ORL_MODINT_FRM_RDY_FOR_OUT     (1<<ORL_MODINT_FRM_RDY_FOR_OUT_BIT)/*0x00000002UL*/ /* frame ready for output */
#define ORL_MODINT_FRM_OUT             (1<<ORL_MODINT_FRM_OUT_BIT)/*0x00000004UL*/ /* frame out */
#define ORL_MODINT_FRM_SKIPPED         (1<<ORL_MODINT_FRM_SKIPPED_BIT)/*0x00000008UL*/ /* frame skipped (not to out) */
//#define ORL_MODINT_OVL_TRANSF_RDY      (1<<ORL_MODINT_OVL_TRANSF_RDY_BIT)/*0x00000010UL*/ /* host overlay buff copied to onboard */
//#define ORL_MODINT_VSTRMASK            0x0000001fUL /* stream interrupts mask */
//#define ORL_NUM_MODULE_INTERR				5 /* max. #interrupts of module */
#define ORL_MODINT_VSTRMASK            0x000000fUL /* stream interrupts mask */
#define ORL_NUM_MODULE_INTERR          5 /* max. #interrupts of module */

typedef struct OrlInitInterrStream
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] stream handle */
   OrlStrInterrType interr_mask;       /* [in] OR'd ORL_STRINT_xxxx stream bits */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlInitInterrStream, *PSOrlInitInterrStream;

typedef enum OrlEdgeEnum
{
   ORL_EDGE_NONE = 0,                  /* CL|An, no edge, disable */
   ORL_EDGE_FALLING,                   /* CL|An, falling edge */
   ORL_EDGE_RISING,                    /* CL|An, rising edge  */
   ORL_EDGE_BOTH,                      /* CL|An, falling & rising edges */
  
   NUM_ORL_EDGE,
   UNKNOWN_ORL_EDGE = ORL_MAX_ENUM
} OrlEdge;

typedef struct OrlInitInterrBoard
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlInterrType interr_mask;          /* [in] OR'd ORL_INT_xxxx board bits ORL_INT_GP0,ORL_INT_GP1,ORL_INT_ADC0,ORL_INT_ADC1 */
   OrlEdge        gp0_edge;            /* [in] edge if ORL_INT_GP0 */
   OrlEdge        gp1_edge;            /* [in] edge if ORL_INT_GP1 */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlInitInterrBoard, *PSOrlInitInterrBoard;

typedef struct OrlWaitInterrStream
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlStrHandle   hstream;             /* [in] stream handle */
   ArvDWORD       stopped;             /* [out] 0: running, 1: vstream interr handling stopped */
   ArvDWORD       num_frms_in;         /* [out] #ORL_STRINT_FRM_IN after last wait */
   ArvDWORD       num_frms_rdy_for_out;/* [out] #ORL_STRINT_FRM_RDY_FOR_OUT after last wait */
   ArvDWORD       num_frms_out;        /* [out] #ORL_STRINT_FRM_OUT after last wait */
   ArvDWORD       num_frms_skipped;    /* [out] #ORL_STRINT_FRM_SKIPPED after last wait */
   ArvDWORD       num_ovl_in;          /* [out] #ORL_STRINT_OVL_TRANSF_RDY after last wait */
   ArvDWORD       reserved[4];         /* [in] should be 0 */
} SOrlWaitInterrStream, *PSOrlWaitInterrStream;

typedef struct OrlWaitInterrBoard
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       stopped;             /* [out] board interr handling is stopped */
   ArvDWORD       num_gp0_in;          /* [out] #ORL_INT_GP0 after last wait */
   ArvDWORD       num_gp1_in;          /* [out] #ORL_INT_GP1 after last wait */
   ArvDWORD       num_adc0;            /* [out] #ORL_INT_ADC0 after last wait */
   ArvDWORD       num_adc1;            /* [out] #ORL_INT_ADC1 after last wait */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlWaitInterrBoard, *PSOrlWaitInterrBoard;

/*********************************************************************************
          An, streams by modules
*********************************************************************************/
/* YCbCr values.
 * bits 23..16 15..8 7..0
 *           Y    Cb   Cr
 * Y range: 16..235
 * Cb/Cr range: 16.240
*/
#define ORL_YCBCR_WHITE   (180<<16|128<<8|128)
#define ORL_YCBCR_YELLOW  (162<<16| 44<<8|142)
#define ORL_YCBCR_CYAN    (131<<16|156<<8|44)
#define ORL_YCBCR_GREEN   (112<<16| 72<<8|58)
#define ORL_YCBCR_MAGENTA ( 84<<16|184<<8|198)
#define ORL_YCBCR_RED     ( 65<<16|100<<8|212)
#define ORL_YCBCR_BLUE    ( 35<<16|212<<8|114)
#define ORL_YCBCR_BLACK   ( 16<<16|128<<8|128)

typedef struct _OrlModule {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* [in] name of module */
   ArvDWORD       enable;              /* [in] 0: disable module, 1: enable module */
   ArvDWORD       reserved[4];
} SOrlModule, *PSOrlModule;

typedef enum _OrlModState
{
   ORL_MODSTATE_UNKNOWN,
   ORL_MODSTATE_NOT_INIT,              /* after Release  */
   ORL_MODSTATE_INIT,                  /* after Init or Disable -> enabling possible */
   ORL_MODSTATE_GOTO_STOP,             /* temp. state after 'Disable' */
   ORL_MODSTATE_ENABLED,               /* after Enable */
   NUM_ORL_MODSTATE
} OrlModState;

typedef struct _OrlModInfo {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* [in] name of module */
   OrlModState    state;               /* [out] module state: ORL_MODSTATE_xxx */
   ArvDWORD       reserved[4];
} SOrlModInfo, *PSOrlModInfo;

typedef struct _OrlModInSett {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_in;           /* [in] name of analog input module (ORL_MODULE_VIDEO_INx) */
   OrlPort        port;                /* [in] video input port */
   OrlVideoTime   vtime;               /* [in] PAL/NTSC video timing */
   OrlVideoGainMode gain_mode;         /* [in] fixed or auto gain */
   ArvDWORD       gain_y_cvbs;         /* [in] [0..511] fixed gain for CVBS or Y */
   ArvDWORD       gain_c;              /* [in] [0..511] fixed gain for C */
   OrlVideoBCSMode bcs_mode;           /* [in] BCS at default or adjustable */
   ArvDWORD       brightness;          /* [in] [0..255] */
   ArvSDWORD      contrast;            /* [in] [-128..127] */
   ArvSDWORD      saturation;          /* [in] [-128..127] */
   ArvSDWORD      hue;                 /* [in] [-128..127] (not avail if RGB)*/
   ArvDWORD       flags;               /* [in] bitwise ORL_FLAG_MIRROR, ORL_FLAG_POWER_DOWN, ORL_FLAG_INV_FID */
   SOrlRect       roi;                 /* [in] ROI of a video field */
   ArvDWORD       scale_width_out;     /* [in] width after video scaler */
   ArvDWORD       scale_height_out;    /* [in] field height after video scaler */
   ArvDWORD       reserved[5];
} SOrlModInSett, *PSOrlModInSett;

typedef struct _OrlModOutSett {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_out;          /* [in] name analog output module (ORL_MODULE_VIDEO_OUTx) */
   OrlModule      module_src;          /* [in] video source module */
   ArvDWORD       pos_x_src;           /* [in] x-offset of video source */
   ArvDWORD       pos_y_src;           /* [in] y-field-offset video source */
   ArvDWORD       bg_color_out;        /* [in] YCbCr color of resulting background */ 
   OrlPort        port;                /* [in] output port */
   OrlVideoTime   vtime;               /* [in] PAL or NTSC video timing */
   ArvDWORD       flags;               /* [in] bitwise ORL_FLAG_POWER_DOWN, ORL_FLAG_NO_COLOR */
   ArvDWORD       outp_width;          /* [out] resulting #pixels/line (720) */
   ArvDWORD       outp_height;         /* [out] resulting #lines/field (288/242) */
   ArvDWORD       reserved[5];
} SOrlModOutSett, *PSOrlModOutSett;

typedef enum _OrlFusionType 
{
   ORL_FUSION_IN1_OVER_IN0,            /* fuse in1 and in0, in1 is foreground */
   ORL_FUSION_IN1_OVER_IN0_KEYING,     /* same, in1 uses Chroma keying, uses key_color */
   NUM_ORL_FUSION,
   ORL_FUSION_UNKNOWN = ORL_MAX_ENUM
} OrlFusionType;

typedef struct OrlColorRange {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       reserved1;           /* reserved, should be 0 */
   ArvDWORD       start0;              /* Y low */
   ArvDWORD       end0;                /* Y high, >= start */
   ArvDWORD       start1;              /* Cb low */
   ArvDWORD       end1;                /* Cb high, >= start */
   ArvDWORD       start2;              /* Cr low */
   ArvDWORD       end2;                /* Cr high, >= start */
   ArvDWORD       reserved[2];
} SOrlColorRange, *PSOrlColorRange;

typedef struct _OrlModFusionSett {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_fusion;       /* [in] name of a fusion module (ORL_VMODULE_FUSIONx) */
   OrlModule      module_src0;         /* [in] video source 0 module */
   ArvDWORD       pos_x_src0;          /* [in] x-offset of video source 0 */
   ArvDWORD       pos_y_src0;          /* [in] y-offset of video source 0 */
   OrlModule      module_src1;         /* [in] video source 1 module */
   ArvDWORD       pos_x_src1;          /* [in] x-offset of video source 1 */
   ArvDWORD       pos_y_src1;          /* [in] y-offset of video source 1 */
   ArvDWORD       bg_color_out;        /* [in] color of resulting background */ 
   OrlModule      module_sync;         /* [in] input module which is sync of result (module_src0 or module_src1)*/
   OrlFusionType  fusion_type;         /* [in] how to fuse both inputs */
   SOrlColorRange key_color;           /* [in] source 1 key color if keying */
   ArvDWORD       outp_width;          /* [in] resulting #pixels/line of output */
   ArvDWORD       outp_height;         /* [in] resulting #lines/field of output */
   ArvDWORD       flags;               /* [in] bitwise: ORL_FLAG_NOT_WAIT_FOR_NSYNC */
   ArvDWORD       reserved[5];
} SOrlModFusionSett, *PSOrlModFusionSett;

/*** Overlay text ***/

typedef enum _OrlTxtAlign
{
   ORL_TXT_ALIGN_LEFT,                 /* alignment left */
   ORL_TXT_ALIGN_RIGHT,                /* alignment right */
   NUM_ORL_TXT_ALIGN,
   UNKNOWN_ORL_TXT_ALIGN = ORL_MAX_ENUM
} OrlTxtAlign;

typedef struct _OrlOvlTxtFrmt {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       in_overlay_video;    /* [in] show overlay text in video 1=true, 0=false */
   ArvDWORD       posx;                /* [in] x-offset of text */
   ArvDWORD       posty;               /* [in] top y-offset of text */
   OrlTxtAlign    align;               /* [in] left/right alignment */
   OrlFontType    font;                /* [in] text font */
   OrlOvlColor    font_color;          /* [in] color of font */
   OrlOvlColor    bg_color;            /* [in] color of font background */
   ArvDWORD       txt_width;           /* [out] #pixels of text in video field */
   ArvDWORD       txt_height;          /* [out] #lines of text in video field */
   ArvDWORD       reserved[2];
} SOrlOvlTxtFrmt, *PSOrlOvlTxtFrmt;

typedef enum _OrlAnTrigSrc
{
   ORL_TRIG_VIDIN0_FRM,                /* video input 0 frame trigger */
   ORL_TRIG_VIDIN1_FRM,                /* video input 1 frame trigger */
   ORL_TRIG_HOST2BRD0,                 /* host-to-board module x trigger */
   ORL_TRIG_HOST2BRD1,
   ORL_TRIG_HOST2BRD2,
   ORL_TRIG_HOST2BRD3,
   ORL_TRIG_HOST2BRD4,
   ORL_TRIG_HOST2BRD5,
   
   NUM_ORLAN_TRIG,
   UNKNOWN_ORLAN_TRIG = ORL_MAX_ENUM
} OrlAnTrigSrc;

typedef ArvDWORD OrlOvlItemHandle;     /* overlay item handle */

#define ORL_INVALID_OVL_ITEM_HANDLE 0UL  /* invalid overlay item handle */
#define ORL_MAX_OVL_ITEMS 8            /* max #items in overlay */
#define ORL_MAX_OVLIT_TEXT_LEN 47      /* max #characters per line in overlay*/

typedef struct _OrlOvlStaticText {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_ovl;          /* [in] name of item parent (overlay text module) */
   OrlOvlItemHandle handle;            /* [in/out] handle of item. [in] if change, [out] if new */
   SOrlOvlTxtFrmt txt_format;          /* [in] format of text in overlay */
   ArvCHAR        text_str[ORL_MAX_OVLIT_TEXT_LEN+1]; /* [in] text string */
   ArvDWORD       reserved[2];
} SOrlOvlStaticText, *PSOrlOvlStaticText;

typedef struct _OrlOvlCounter {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_ovl;          /* [in] name of item parent (overlay text module) */
   OrlOvlItemHandle handle;            /* [in/out] handle of item. [in] if change, [out] if new */
   SOrlOvlTxtFrmt txt_format;          /* [in] format of text overlay */
   SOrlFrmAnnotValue value;            /* [in] value of counter */
   OrlAnTrigSrc   update_src;          /* [in] source which increments counter */
   OrlFrmAnnotType format;             /* [in] type of counter */
   ArvDWORD       reserved[2];
} SOrlOvlCounter, *PSOrlOvlCounter;

typedef struct _OrlOvlItem {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_ovl;          /* [in] name of item parent (overlay text module) */
   OrlOvlItemHandle handle;            /* [in] handle of item */
   ArvDWORD       reserved[2];
} SOrlOvlItem, *PSOrlOvlItem;

typedef struct _OrlModTxtOverlaySett {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_ovl;          /* [in] name of overlay module */
   ArvDWORD       outp_width;          /* [in] resulting image width (max 720 pix, multiple of 4 */
   ArvDWORD       outp_height;         /* [in] resulting image height (max 242/288 lines) */
   ArvDWORD       reserved[4];
} SOrlModTxtOverlaySett, *PSOrlModTxtOverlaySett;

typedef struct _OrlModHostSett {
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module_host;         /* [in] name of a host module ORL_MODULE_BRD2HOSTx/ORL_MODULE_HOST2BRDx */
   OrlModule      module_src;          /* [in] video source, only if b2h */
   ArvDWORD       pos_x_src;           /* [in] x-offset of video source, only if b2h */
   ArvDWORD       pos_y_src;           /* [in] y-offset of video source, only if b2h */
   ArvDWORD       bg_color_out;        /* [in] background color of result, only if b2h */
   OrlFieldArrange fieldarrange_host;  /* [in] field arrangement at host */
   OrlPixelFormat format_host;         /* [in] pixel format at host */
   ArvDWORD       width;               /* [in] width in pixels */
   ArvDWORD       height;              /* [in] height in lines/field */
   ArvDWORD       host_numplanes;      /* [out] #planes */
   SOrlPlaneSize  host_pl0;            /* [out] needed host buffer size of plane 0 */
   SOrlPlaneSize  host_pl1;            /* [out] needed host buffer size of plane 1 */
   SOrlPlaneSize  host_pl2;            /* [out] needed host buffer size of plane 2 */
   ArvDWORD       reserved[5];
} SOrlModHostSett, *PSOrlModHostSett;

/* interrupt */
typedef struct _OrlModInitInterr
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlModule      module;              /* [in] module which should generate interrupts */
   OrlModInterrType interr_mask;       /* [in] interrupt mask (0=no interrupts) */
   ArvDWORD       reserved[2];
} SOrlModInitInterr, *PSOrlModInitInterr;

typedef struct OrlModWaitInterr
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       stopped;             /* [out] 0: running, 1: module interr handling stopped */
   ArvDWORD       num_interr[NUM_ORL_MODULE][ORL_NUM_MODULE_INTERR];/* out [module][interr] */
   ArvDWORD       reserved[4][ORL_NUM_MODULE_INTERR];   /* [in] should be 0 */
} SOrlModWaitInterr, *PSOrlModWaitInterr;

/*********************************************************************************
          CL and An, board initialization
*********************************************************************************/

#define ORL_INITOPT_POST_CB 0x0001

typedef struct sSOrlInit
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlCbFunc      cbfunc;              /* Callback function, only CL, otherwise set NULL */
   ArvPVOID       cbparm;              /* Callback parameter, only CL, otherwise set NULL */
   ArvDWORD       opt;                 /* Callback option ORL_INITOPT_xxx */
   ArvDWORD       reserved[5];         /* [in] should be 0 */
} SOrlInit, *PSOrlInit;

typedef struct sSOrlLibVer
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       lib_ver;             /* orlando library version */
   ArvDWORD       intf_ver;            /* orlando API version (should be comp. with ORLANDO_INTERFACE_VERSION) */
   ArvDWORD    reserved[2];            /* should be 0 */
} SOrlLibVer, *PSOrlLibVer;

typedef enum _OrlBoardType
{
   ORL_BOARDT_CL_PCIE = 0,             /* CameraLink PCI-E */
   ORL_BOARDT_CL_PC104,                /* CameraLink PC104plus */
   ORL_BOARDT_ANALOG_PCIE,             /* Analog PCI-E */
   ORL_BOARDT_ANALOG_PC104,            /* Analog PC104plus */

   NUM_ORL_BOARDT,
   UNKNOWN_ORL_BOARDT = ORL_MAX_ENUM
} OrlBoardType;

typedef struct sSOrlBoardInfo
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */

   OrlBoardType   btype;               /* [out] board type */
   ArvDWORD       board_version;       /* [out] PCB revision */
   ArvDWORD       id;                  /* [out] internal use */
   ArvDWORD       td;                  /* [out] internal use */

   ArvDWORD       fhii;
   ArvDWORD       fb;
   ArvDWORD       fsii;
   ArvDWORD       fsiiv;

   ArvDWORD       dsp_id;
   ArvDWORD       dsp_revid;
   ArvDWORD       dhii;
   ArvDWORD       dsii;
   ArvDWORD       dsiiv;

   ArvPCHAR       str;                 /* [in/out] stringable version of this struct */
   ArvDWORD       buffsize;            /* [in/out] size of str buffer in bytes */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlBoardInfo, *PSOrlBoardInfo;

typedef struct sSOrlFirmwareDir
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvPCHAR       dirstr;              /* ptr to firmware directory string */
   ArvDWORD       buffsize;            /* size of dirstr buffer */
   ArvDWORD       reserved[2];         /* [in] should be 0 */
} SOrlFirmwareDir, *PSOrlFirmwareDir;

#define ORL_OPT_DSPEMUL_MODE    0x01000000
#define ORL_OPT_ONLY_F          0x02000000
#define ORL_OPT_ONLY_D          0x04000000

typedef struct sSOrlProgramFirmwareStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       opt;                 /* ORL_OPT_xxx, internal use. set to 0 */
   ArvDWORD       reserved[6];         /* [in] should be 0 */
} SOrlProgramFirmware, *PSOrlProgramFirmware;

/*********************************************************************************
          CL and An, inputs / outputs
*********************************************************************************/

typedef enum OrlInputPinEnum
{                      
   ORL_INP_GPIN0 = 0,                  /* CL|An, General Purpose input 0 */
   ORL_INP_GPIN1,                      /* CL|An, General Purpose input 1 */

   NUM_ORL_INP,
   UNKNOWN_ORL_INPP = ORL_MAX_ENUM
} OrlInputPin;

typedef enum OrlOutputPinEnum
{                      
   ORL_OUTP_GPOUT0 = 0,                /* CL|An, General Purpose output 0 */
   ORL_OUTP_GPOUT1,                    /* CL|An, General Purpose output 1 */
   ORL_OUTP_CC1,                       /* CL,    CameraLink Control 1 output */
   ORL_OUTP_CC2,                       /* CL,    CameraLink Control 2 output */
   ORL_OUTP_CC3,                       /* CL,    CameraLink Control 3 output */
   ORL_OUTP_CC4,                       /* CL,    CameraLink Control 4 output */

   NUM_ORL_OUTP,
   UNKNOWN_ORL_OUTP = ORL_MAX_ENUM
} OrlOutputPin;

typedef enum OrlLevelSrcEnum
{                             
   ORL_LEVSRC_LOGIC_LOW = 0,           /* CL|An logic low level */
   ORL_LEVSRC_LOGIC_HIGH,              /* CL|An logic high level */
   ORL_LEVSRC_LVAL,                    /* CL    CameraLinks LVAL signal */
   ORL_LEVSRC_LVAL_INV,                /* CL    id, inverted */
   ORL_LEVSRC_FVAL,                    /* CL    CameraLinks FVAL signal */
   ORL_LEVSRC_FVAL_INV,                /* CL    id, inverted */
   ORL_LEVSRC_TIMER0,                  /* CL    Timer 0 output */
   ORL_LEVSRC_TIMER0_INV,              /* CL    id, inverted */
   ORL_LEVSRC_TIMER1,                  /* CL    Timer 1 output */
   ORL_LEVSRC_TIMER1_INV,              /* CL    id, inverted */
   ORL_LEVSRC_GPIN0,                   /* CL|An General Purpose 0 input */
   ORL_LEVSRC_GPIN0_INV,               /* CL|An id, inverted */
   ORL_LEVSRC_GPIN1,                   /* CL|An General Purpose 1 input */
   ORL_LEVSRC_GPIN1_INV,               /* CL|An id, inverted */
   ORL_LEVSRC_ROI,                     /* CL    video inside ROI */
   ORL_LEVSRC_ROI_INV,                 /* CL    id, inverted */
                                       
   ORL_LEVSRC_ADC0_HSYNC,              /* An HSync at ADC0 */
   ORL_LEVSRC_ADC0_HSYNC_INV,          /* An id, inverted */
   ORL_LEVSRC_ADC0_VSYNC,              /* An VSync at ADC0 */
   ORL_LEVSRC_ADC0_VSYNC_INV,          /* An id, inverted */
   ORL_LEVSRC_ADC1_HSYNC,              /* An HSync at ADC1 */
   ORL_LEVSRC_ADC1_HSYNC_INV,          /* An id, inverted */
   ORL_LEVSRC_ADC1_VSYNC,              /* An VSync at ADC1 */
   ORL_LEVSRC_ADC1_VSYNC_INV,          /* An id, inverted */

   NUM_ORL_LEVSRC,
   UNKNOWN_ORL_LEVSRC = ORL_MAX_ENUM
} OrlLevelSrc;

typedef struct OrlOutpStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlOutputPin   outp;                /* [in] a board output pin */
   OrlLevelSrc    src;                 /* [in] source for output */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlOutput, *PSOrlOutput;

typedef struct OrlInpStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlInputPin    inp;                 /* [in] a board input pin */
   ArvDWORD       level;               /* [out] level of pin */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlInput, *PSOrlInput;

typedef struct OrlInpInterrStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   OrlInputPin    inp;                 /* input which should generate an interrupt */
   OrlEdge        edge;                /* interrupt edge */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlInpInterr, *PSOrlInpInterr;

/*********************************************************************************
          CL and An, capture saving
*********************************************************************************/

typedef enum OrlBMTypeEnum
{
   ORL_BM_BMP = 0,                     /* Windows BMP format */
   ORL_BM_RAW,                         /* RAW format 'memory copy' */
   ORL_BM_BMP_UD,                      /* Windows BMP format, negative Height */
   ORL_BM_JPEG,                        /* JPEG format */
// ORL_BM_TIFF,                        /* TIFF format */
   
   NUM_ORL_BM,
   UNKNOWN_ORL_BM = ORL_MAX_ENUM
} OrlBMType;

typedef struct OrlSaveFileStruct
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvPCHAR       filename;            /* complete file name */
   ArvDWORD       filenamelength;      /* length of file name */
   ArvDWORD       hostbuffernr;        /* host buffer nr. to store */
   OrlBMType      bmtype;              /* bitmap type */
   ArvDWORD       quality;             /* only used if bmtype is JPEG 0..100 */
   ArvDWORD       reserved[2];         /* should be 0 */
} SOrlSaveFile, *PSOrlSaveFile, SOrlLoadFile, *PSOrlLoadFile;


/*********************************************************************************
          CL and An,    Orlando functions enum
*********************************************************************************/

#define FIRST_ORLF_GET     1
#define FIRST_ORLF_SET     1000

typedef enum _OrlFunc
{
   /* Get functions */                 /* Board Parameter */
   ORLF_GETBOARDINFO = FIRST_ORLF_GET, /* CL|An PSOrlBoardInfo */
   ORLF_GET_LOG,                       /* CL|An PSOrlLog */
   ORLF_READ_UART,                     /* CL    Uart PSOrlUartReadWrite */
   ORLF_UART_GET_BYTES_AVAIL,          /* CL    PSOrlUartReadWrite */
   ORLF_GET_HBUFFINFO,                 /* CL    PSOrlHostBuffInfo kan weg ? todo */
   ORLF_GET_INPUT,                     /* CL|An PSOrlInput */
   ORLF_HFIFO_GET,                     /* CL    PSOrlHostBuffInfo */
   ORLF_GET_VIDEO_BUFF_FILL,           /* CL    PSOrlVideoBuffFillLevel */
   ORLF_GET_FIRMWARE_DIR,              /* CL|An PSOrlFirmwareDir */
   ORLF_GET_CLVIDEO_INFO,              /* CL    PSOrlCLVideoInfo */

   ORLF_GET_VIDEOIN_STAT,              /* An    PSOrlVideoInputStat */
   ORLF_GET_STREAM_INFO,               /* An    obs. PSOrlStreamInfo */
   ORLF_GET_PL_HBUFFINFO,              /* An    PSOrlPlHostBuffInfo */

   ORLF_WAIT_INTERR_STREAM,            /* An    obs. PSOrlWaitInterrStream*/
   ORLF_WAIT_INTERR_BOARD,             /* An    PSOrlWaitInterrBoard */

   ORLF_WAIT_INTERR_MODULE,            /* An    PSOrlModWaitInterr */
   ORLF_GET_MODULE_INFO,               /* An    PSOrlModInfo */
	ORLF_DUMP_DSP_LOGS,
   
   /* Set functions */
   ORLF_SET_LOG = FIRST_ORLF_SET,      /* CL|An PSOrlLog */
   ORLF_INIT,                          /* CL    PSOrlInit */
   ORLF_INIT_VIDEO_ACQ,                /* CL    PSOrlVideoAcqSett */
   ORLF_INIT_VIDEO_BUFF,               /* CL    PSOrlVideoBuffSett */
   ORLF_ACQUIRE_ENABLE,                /* CL    PSOrlAcquireEnable */
   ORLF_ACQ_STOP_SWTRIG,               /* CL    PSOrlNullStruct */
   ORLF_SET_SERIAL_SETTINGS,           /* CL    PSOrlSerialSett */
   ORLF_SET_UART_SETTINGS,             /* CL    Uart PSOrlUartSett */
   ORLF_WRITE_UART,                    /* CL    Uart PSOrlUartReadWrite */
   ORLF_PURGE_UART,                    /* CL    PSOrlNullStruct */
   ORLF_VE_TO_HOST,                    /* CL    PSOrlNullStruct */

   ORLF_HFIFO_PUT_EMPTY,               /* CL    PSOrlHostBuffEmpty mark buffer empty */
   ORLF_PURGE_VIDEO_BUFF,              /* CL    PSOrlNullStruct */

   ORLF_SAVE_HOSTBUFF,                 /* CL|An PSOrlSaveFile */
   ORLF_SET_FIRMWARE_DIR,              /* CL|An PSOrlFirmwareDir */
   ORLF_PROGRAM_FIRMWARE,              /* CL|An PSOrlProgramFirmware */
   ORLF_SET_OUTPUT,                    /* CL|An PSOrlOutput */
   ORLF_SET_INPUT_INTERR,              /* CL    PSOrlInpInterr */
   ORLF_SET_TIMER,                     /* CL    PSOrlTimer */
   ORLF_TIMER_SWTRIG,                  /* CL    PSOrlTimerSWTrig */

   ORLF_INIT_VIDEO_TO_HOST_STREAM,     /* An    obs. PSOrlVideoInputToHostStream */
   ORLF_INIT_HOST_TO_VIDEO_STREAM,     /* An    obs. PSOrlHostToVideoOutputStream */
   ORLF_INIT_VIDEO_IN_TO_OUT_STREAM,   /* An    obs. PSOrlInpToOutputStream */
   ORLF_ENABLE_STREAM,                 /* An    obs. PSOrlStreamEnable */
   ORLF_INIT_INTERR_STREAM,            /* An    obs. PSOrlInitInterrStream */
   ORLF_INIT_INTERR_BOARD,             /* An    PSOrlInitInterrBoard */
   ORLF_ALLOC_BUFFS,                   /* An    obs. PSOrlAllocBuff */
   ORLF_ALLOC_HBUFF,                   /* An    PSOrlAllocPlHostBuff */
   ORLF_FREE_HBUFF,                    /* An    PSOrlAllocPlHostBuff */
   ORLF_FRM_TRANSF,                    /* An    PSOrlFrmTransf */
   ORLF_FREE_STREAM_RESOURCES,         /* An    obs. PSOrlFreeStrRes */
   ORLF_LOAD_HOSTBUFF,                 /* An    PSOrlLoadFile */

   ORLF_INIT_OVERLAY,                  /* An    obs. PSOrlInitOverlay */
   ORLF_INIT_ANNOTATION,               /* An    obs. PSOrlInitFrmAnnot */
   ORLF_CHG_ANNOTATION_VALUE,          /* An    obs. PSOrlFrmAnnotValue */
   ORLF_USEROVL_TRANSF,                /* An    obs. PSOrlFrmTransf */

   ORLF_GET_PCI_STAT,                  /* An    PSOrlPCIStat */

   ORLF_INIT_MODULE_IN,                /* An    PSOrlModInSett */
   ORLF_INIT_MODULE_OUT,               /* An    PSOrlModOutSett */
   ORLF_INIT_VMODULE_FUSION,           /* An    PSOrlModFusionSett */
   ORLF_INIT_VMODULE_TEXT_OVL,         /* An    PSOrlModTxtOverlaySett */
   ORLF_ADDOVL_STAT_TEXT,              /* An    PSOrlOvlStaticText */
   ORLF_ADDOVL_STAT_COUNTER,           /* An    PSOrlOvlCounter */
   ORLF_REMOVEOVL_ITEM,                /* An    PSOrlOvlItem */
   ORLF_INIT_MODULE_BRD2HOST,          /* An    PSOrlModHostSett */
   ORLF_INIT_MODULE_HOST2BRD,          /* An    PSOrlModHostSett */
   ORLF_INIT_INTERR_MODULE,            /* An    PSOrlModInitInterr */
   ORLF_MODULE_ENABLE,                 /* An    PSOrlModule */
   ORLF_MODULE_RELEASE,                /* An    PSOrlModule */

   //   ORLF_SET_BAYER_CONV,                /* CL    PSOrlBayerConv */

   ORLF_MAX_NUM
} OrlFuncType;

typedef enum eOrlEnumType
{
   ORLRET = 0,
   ORLBOARDTYPE,
   ORLSERIALSRC,
   ORLBAUDRATE,
   ORLCAMSCANTYPE,
   ORLCAMCOLORTYPE,
   ORLCAMNUMTAPS,
   ORLNBITSPIXEL,
   ORLTRIGGERSRC,
   ORLLEVELSRC,
   ORLACQMEMMODE,
   ORLINPUTPIN,
   ORLOUTPUTPIN,
   ORLEDGE,
   ORLTIMERID,
   ORLSWTRIG,
   ORLBMTYPE,
   ORLFUNCTYPE,
   ARVLOGLEV,

   ORLMODULE,
   ORLPORT,
   ORLVIDEOTIME,
   ORLVIDEOGAINMODE,
   ORLVIDEOBCSMODE,
   ORLFIELDARRANGE,
   ORLPIXELFORMAT,
   ORLSTREAMSTATE,
   ORLFONTTYPE,
   ORLFRMANNOTTYPE,
   ORLOVLCOLOR,
   ORLMODSTATE,
   ORLTXTALIGN,
   ORLANTRIGSRC,
	ORLFUSIONTYPE,
//	ORLBAYERPATTERN,

   NUM_ORLENUM,
   ORLENUM_UNKNOWN = ORL_MAX_ENUM
} OrlEnumType;

#ifndef _TMS320C6X
typedef struct sSOrlLog
{
   ArvDWORD       size;                /* [in] size of struct, set by ORLINITSTRUCT(v) */
   ArvDWORD       system;              /* [in] 0: Orlando log, 1: System log */
   ArvCHAR        filename[MAX_LOG_FILENAMELENGTH+1];/* [in] log filename */
   ArvLogLev      level;               /* [in] log-level, see arvtyp.h */
   ArvDWORD       flags;               /* [in] bitwise ARV_LOGFLAG_xxxx */
   ArvDWORD       reserved;            /* [in] should be 0 */
} SOrlLog, *PSOrlLog;
#endif   /* !_TMS320C6X */

#endif /* __ARVOO_ORLANDO_TYPS__ */
