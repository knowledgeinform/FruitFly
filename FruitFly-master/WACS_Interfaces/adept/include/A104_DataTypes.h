/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   A104_DataTypes.h                                        */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :   Define the external data types for the Adept104 device  */
/*                  driver.                                                 */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/A104_DataTypes.h_V   1.1   May 23 2006 14:21:42   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/A104_DataTypes.h_V  $
*
*   Rev 1.1   May 23 2006 14:21:42   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.2   May 23 2006 13:58:58   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.1   Feb 14 2006 16:32:36   SmithGo
*   Events. DPR -> AVT copy fix.  IEEE support for EXIF
*
*   Rev 1.0   Oct 15 2004 07:56:54   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__A104_DataTypes_h__INCLUDED_)

#define     __A104_DataTypes_h__INCLUDED_

////////////////////////////////////////////////////////////////////////////

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

////////////////////////////////////////////////////////////////////////////
//  Include Files

#include    "AvtDataType.h"                 //  AVT_DATA

////////////////////////////////////////////////////////////////////////////
//  Global Definitions

//  Arbitrary number of query responses that will be stored before any are
//  discarded.
//  See also AVT_CMD_DATA.
#define     AVT_STATUS_FIFO_SIZE    64

//  Queue sizes for different fields that will be commanded/queried
#define     CMD_FIELDS_MAX          128
#define     QUERY_FIELDS_MAX        128

//  General VME area definitions
#define     VME_BLOCKS              256     /*  Block 0 never used          */
#define     VME_FIELDS              16      /*  Field 0 never used          */

//  Memory access types are used by A104_READ_COMMAND and A104_WRITE_COMMAND
#if !defined(__enumA104_MEMORY_ACCESS_TYPES__DEFINED_)

enum        enumA104_MEMORY_ACCESS_TYPES
                    {
                        //  General memory access types are read/write
                        A104MAT_SRAM,

                        //  Specific memory access types are read-only
                        A104MAT_INPUT_INTERRUPT_COUNT,
                    };

#define     __enumA104_MEMORY_ACCESS_TYPES__DEFINED_

//  A104MAT_INPUT_INTERRUPT_COUNT returns a ULONG value of the number of
//  interrupts from the Adept104.

#endif  //  #if !defined(__enumA104_MEMORY_ACCESS_TYPES__DEFINED_)

//  These events are used to signal user applications that certain things
//  have happened.
//
//  See also structA104_USER_EVENTS.
#if !defined(__enumA104_USER_EVENTS__DEFINED_)

enum        enumA104_USER_EVENTS
                    {
                        A104UE_NEW_BLOCK_AND_FIELD_DATA,
                        A104UE_NEW_ECIF_DATA,
                        A104UE_NEW_INTERRUPT_DPC,
                        A104UE_NEW_INTERRUPT_NO_DATA,
                        A104UE_NEW_LOG_DATA,

                        //  Must be last usable event enumeration
                        NO_OF_A104_USER_EVENTS
                    };

#define     __enumA104_USER_EVENTS__DEFINED_

#endif  //  #if !defined(__enumA104_USER_EVENTS__DEFINED_)

////////////////////////////////////////////////////////////////////////////
//  Structures should be packed on 8 byte boundaries

#pragma     pack(push, 8)

////////////////////////////////////////////////////////////////////////////
//  Global Type Definitions

/*
 *      Structure for storing data in command messages which go to the AVT
 *      (and for status replies from the AVT).
 *
 *          dwBlock     VME block number, 1 - 255, for command/status data.
 *
 *          dwField     VME field number, 1 - 15, for command/status data.
 *
 *          Data        Union for storing the various data types that can be
 *                      communicated to/from the AVT.
 *
 *      Used to read AVT status responses from the device driver (see
 *      AVT_STATUS_FIFO_SIZE, above).
 */
#if !defined(__structAVT_CMD_DATA__DEFINED_)

typedef     struct structAVT_CMD_DATA
                            {
                                ULONG       dwBlock,
                                            dwField;

                                AVT_DATA    Data;

                            } AVT_CMD_DATA;

#define     __structAVT_CMD_DATA__DEFINED_

#endif  //  #if !defined(__structAVT_CMD_DATA__DEFINED_)

/*
 *      See below.
 */
#if !defined(__structA104_READ_COMMAND__DEFINED_)

typedef     struct structA104_READ_COMMAND
                            {
                                //  c.f. enumA104_MEMORY_ACCESS_TYPES
                                ULONG   dwMemoryAccessType;

                                //  First address to be read
                                ULONG   dwStartAddress;

                                //  Number of UCHAR, USHORT or ULONG words
                                //  to read from, including the start
                                //  address
                                ULONG   dwWordCount;

                                //  Can be:
                                //      1 = UCHAR
                                //      2 = USHORT
                                //      4 = ULONG
                                ULONG   dwBytesPerWord;

                            } A104_READ_COMMAND;

#define     __structA104_READ_COMMAND__DEFINED_

#endif  //  #if !defined(__structA104_READ_COMMAND__DEFINED_)

/*
 *      See below.
 */
#if !defined(__structA104_WRITE_COMMAND__DEFINED_)

typedef     struct structA104_WRITE_COMMAND
                            {
                                //  Size of the structure with the malloc'd
                                //  bit in the pvWriteData member
                                ULONG   dwWriteCmdSize;

                                //  c.f. enumMEMORY_ACCESS_TYPES
                                ULONG   dwMemoryAccessType;

                                //  First address to be written
                                ULONG   dwStartAddress;

                                //  Number of UCHAR, USHORT or ULONG words
                                //  to write to, including the start address
                                ULONG   dwWordCount;

                                //  Can be:
                                //      1 = UCHAR
                                //      2 = USHORT
                                //      4 = ULONG
                                ULONG   dwBytesPerWord;

                                //  Start of the data to be written (the
                                //  address of this member needs to be cast
                                //  to the appropriate type to match
                                //  nBytesPerWord)
                                PVOID   pvWriteData;

                            } A104_WRITE_COMMAND;

#define     __structA104_WRITE_COMMAND__DEFINED_

#endif  //  #if !defined(__structA104_WRITE_COMMAND__DEFINED_)

/*
 *      Used for writing 32-bit data (dwRegisterData) to the Adept104
 *      register space (dwRegisterAddress).
 *
 *      The address of the 32-bit register should be within the range
 *      A104_REGISTER_START to A104_REGISTER_END.
 */
#if !defined(__structA104_WRITE_REGISTER__DEFINED_)

typedef     struct structA104_WRITE_REGISTER
                            {
                                ULONG   dwRegisterAddress;

                                ULONG   dwRegisterData;

                            } A104_WRITE_REGISTER;

#define     __structA104_WRITE_REGISTER__DEFINED_

#endif  //  #if !defined(__structA104_WRITE_REGISTER__DEFINED_)

/*
 *      These events are used to signal user applications that certain
 *      things have happened.  They should be created by the user
 *      application and passed into the driver.
 *
 *      When bEventValid is TRUE, hEvent can be used to set (when it's a
 *      valid event handle) or cancel (when it's NULL) the event.
 *
 *      See also enumA104_USER_EVENTS.
 */
#if !defined(__structA104_USER_EVENTS__DEFINED_)

typedef     struct structA104_USER_EVENT
                            {
                                BOOLEAN     bEventValid;

                                HANDLE      hEvent;

                            } A104_USER_EVENT;

typedef     struct structA104_USER_EVENTS
                            {
                                A104_USER_EVENT     Event[NO_OF_A104_USER_EVENTS];

                            } A104_USER_EVENTS;

#define     __structA104_USER_EVENTS__DEFINED_

#endif  //  #if !defined(__structA104_USER_EVENTS__DEFINED_)

////////////////////////////////////////////////////////////////////////////

#pragma     pack(pop)

////////////////////////////////////////////////////////////////////////////

#endif  //  #if !defined(__A104_DataTypes_h__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
