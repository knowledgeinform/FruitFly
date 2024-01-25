/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   AvtLogDataTypes.h                                       */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :   Define the AVT_LOG_DATA data type, etc., for the        */
//                  Adept104 device driver, etc.                            */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/AvtLogDataTypes.h_V   1.1   May 23 2006 14:21:46   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/AvtLogDataTypes.h_V  $
*
*   Rev 1.1   May 23 2006 14:21:46   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.2   May 23 2006 13:59:06   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.1   Feb 14 2006 16:32:46   SmithGo
*   Events. DPR -> AVT copy fix.  IEEE support for EXIF
*
*   Rev 1.0   Oct 15 2004 07:56:58   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__AvtLogDataTypes_h__INCLUDED_)

#define     __AvtLogDataTypes_h__INCLUDED_

////////////////////////////////////////////////////////////////////////////

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

////////////////////////////////////////////////////////////////////////////
//  Include Files

#include    "AvtDataType.h"                 //  AVT_DATA

////////////////////////////////////////////////////////////////////////////
//  Global Definitions

//  Five seconds worth of log data at 60Hz, six seconds at 50Hz.
//  Used in ReadFile() accesses to the device driver.
//  See also AVT_LOG_DATA.
#define     AVT_LOG_FIFO_SIZE       300

#define     EXTRA_LOG_DATA          6       /*  For extended log format     */

////////////////////////////////////////////////////////////////////////////
//  Structures should be packed on 8 byte boundaries

#pragma     pack(push, 8)

////////////////////////////////////////////////////////////////////////////
//  Global Type Definitions

/*
 *      The standard AVT log message is comprised:
 *
 *          Sync flags          Not adjustable.
 *
 *          Message counter     Not adjustable.
 *
 *          Status flags        Single byte indicates track mode, etc.
 *
 *          Data X              32-bit data can be used for TBE (x-axis),
 *                              platform demand, etc.  Originally this could
 *                              be integer, 32,12 fixed point value or
 *                              32-bit IEEE float point but now only the
 *                              IEEE floating point type is supported.
 *
 *          Data Y              As for Data X (but for the y-axis).
 *
 *      This structure is used to configure the sources of the above data
 *      for the PCI equivalent of the standard log message.
 *
 *      Currently there is a restriction that the status flags and data must
 *      exist together in the same VME block.
 */
#if !defined(__structAVT_LOG_CONFIG__DEFINED_)

typedef     struct structAVT_LOG_CONFIG
                            {
                                ULONG   dwBlock;

                                ULONG   dwMessageCount,
                                        dwStatusFlags,
                                        dwDataX,
                                        dwDataY;

                            } AVT_LOG_CONFIG;

#define     __structAVT_LOG_CONFIG__DEFINED_

#endif  //  #if !defined(__structAVT_LOG_CONFIG__DEFINED_)

/*
 *      As per structAVT_LOG_CONFIG but supports extended log format.
 */
#if !defined(__structAVT_LOG_CONFIG_EX__DEFINED_)

typedef     struct structAVT_LOG_CONFIG_EX
                            {
                                ULONG   dwBlock;

                                ULONG   dwMessageCount,
                                        dwStatusFlags,
                                        dwDataX,
                                        dwDataY;

                                ULONG   dwExtraLogData[EXTRA_LOG_DATA];

                            } AVT_LOG_CONFIG_EX;

#define     __structAVT_LOG_CONFIG_EX__DEFINED_

#endif  //  #if !defined(__structAVT_LOG_CONFIG_EX__DEFINED_)

/*
 *      The PCI equivalent of the standard AVT log message.  The source of
 *      the data can be configured using AVT_LOG_CONFIG.
 *
 *      See also AVT_LOG_FIFO_SIZE.
 */
#if !defined(__structAVT_LOG_DATA__DEFINED_)

typedef     struct structAVT_LOG_DATA
                            {
                                AVT_DATA    MessageCount,
                                            StatusFlags,
                                            DataX,
                                            DataY;
                            } AVT_LOG_DATA;

#define     __structAVT_LOG_DATA__DEFINED_

#endif  //  #if !defined(__structAVT_LOG_DATA__DEFINED_)

/*
 *      As per structAVT_LOG_DATA but supports extended log format.  The
 *      source of the data can be configured using AVT_LOG_CONFIG_EX.
 */
#if !defined(__structAVT_LOG_DATA_EX__DEFINED_)

typedef     struct structAVT_LOG_DATA_EX
                            {
                                AVT_DATA    MessageCount,
                                            StatusFlags,
                                            DataX,
                                            DataY;

                                AVT_DATA    ExtraLogData[EXTRA_LOG_DATA];

                            } AVT_LOG_DATA_EX;

#define     __structAVT_LOG_DATA_EX__DEFINED_

#endif  //  #if !defined(__structAVT_LOG_DATA_EX__DEFINED_)

////////////////////////////////////////////////////////////////////////////

#pragma     pack(pop)

////////////////////////////////////////////////////////////////////////////

#endif  //  #if !defined(__AvtLogDataTypes_h__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
