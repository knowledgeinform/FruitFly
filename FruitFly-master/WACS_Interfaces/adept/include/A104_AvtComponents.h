/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   A104_AvtComponents.h                                    */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :                                                           */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/A104_AvtComponents.h_V   1.1   May 23 2006 14:21:42   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/A104_AvtComponents.h_V  $
*
*   Rev 1.1   May 23 2006 14:21:42   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.2   May 23 2006 13:58:56   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.1   Feb 14 2006 16:32:36   SmithGo
*   Events. DPR -> AVT copy fix.  IEEE support for EXIF
*
*   Rev 1.0   Oct 15 2004 07:56:52   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__A104_AVT_COMPONENTS_H__INCLUDED_)

#define     __A104_AVT_COMPONENTS_H__INCLUDED_

////////////////////////////////////////////////////////////////////////////

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

////////////////////////////////////////////////////////////////////////////
//  Include Files

#include    "A104_DataTypes.h"              //  AVT_DATA

////////////////////////////////////////////////////////////////////////////
//  Global Definitions

#define     MAX_AVT_COMPONENTS      16      /*  Per component registration  */

#define     MAX_AVT_COMPONENT_SETS  256

#define     ACS_INPUT_FIFO_SIZE     32      /*  Input to driver from AVT    */
#define     ACS_OUTPUT_FIFO_SIZE    256     /*  Output from driver to AVT   */

////////////////////////////////////////////////////////////////////////////
//  Structures should be packed on 8 byte boundaries

#pragma     pack(push, 8)

////////////////////////////////////////////////////////////////////////////
//  Global Type Definitions

/*
 *  AVT_COMPONENT_ITEM
 *
 *      wComponentID        The component ID can be a "genuine" 16-bit ID or
 *                          it can be constructed from a legacy VME block
 *                          field address in which case the ID is set to
 *                          (256 * field number) + block number.
 *
 *      Data                The data is component and registration dependent
 *                          (c.f. ucDataType).
 *
 *                          Note that u8_StringLength is used in conjunction
 *                          with CDT_U_STRING data types and
 *                          AVT_COMPONENT_SET::u8_String, see below.
 *
 *                          Note also that ucDoubleValue is included for the
 *                          benefit of the device driver in which it is best
 *                          not to include any floating point accesses at
 *                          all, if possible, in the interests of speed and
 *                          not corruptig the floating point "state" for the
 *                          user application(s).
 *
 *      ucDataType          The data type is for the interface only, it is
 *                          not related to the internal data type within the
 *                          AVT (c.f. CDT_U_INT, etc.).
 *
 *      pszDescription      Optional text description of the component.
 */
#if !defined(__structAVT_COMPONENT_ITEM__DEFINED_)

typedef     struct structAVT_COMPONENT_ITEM
                            {
                                USHORT      wComponentID;

                                union   {
                                            CHAR    s8_Value;
                                            UCHAR   u8_Value;
                                            SHORT   s16_Value;
                                            USHORT  u16_Value;
                                            LONG    s32_Value;
                                            ULONG   u32_Value;
                                            float   fValue;
                                            double  dValue;

                                            UCHAR   u8_StringLength;

                                            UCHAR   ucDoubleValue[sizeof(double)];

                                        } Data;

                                UCHAR       ucDataType;

                                const char* pszDescription;

                            } AVT_COMPONENT_ITEM;

#define     __structAVT_COMPONENT_ITEM__DEFINED_

#endif  //  #if !defined(__structAVT_COMPONENT_ITEM__DEFINED_)

/*
 *  AVT_COMPONENT_SET
 *
 *      ucCommandID
 *
 *      ucComponentSetID
 *
 *      ucReadWriteUsage
 *
 *      ucNotification
 *
 *      ucNotificationDelay
 *
 *      ucComponents
 *
 *      bRegistered
 *
 *      ucResponseID
 *
 *      ucResponseStatus
 *
 *      dTimeOfLastUpdate
 *
 *      Component
 *
 *      u8_String               Only used for CDT_U_STRING data types, of
 *                              which only one is supported per registration,
 *                              when it contains a variable lemgth string
 *                              (which is NOT NULL-terminated) up to 255
 *                              characters long.
 *                              The length is set in
 *                              AVT_COMPONENT_ITEM::u8_StringLength, see
 *                              above.
 */
#if !defined(__structAVT_COMPONENT_SET__DEFINED_)

typedef     struct structAVT_COMPONENT_SET
                            {
                                UCHAR               ucCommandID;

                                UCHAR               ucComponentSetID;

                                UCHAR               ucReadWriteUsage;

                                UCHAR               ucNotification,
                                                    ucNotificationDelay;

                                UCHAR               ucComponents;

                                BOOLEAN             bRegistered;

                                double              dTimeOfLastUpdate;

                                UCHAR               ucResponseID;

                                UCHAR               ucResponseStatus;

                                AVT_COMPONENT_ITEM  Component[MAX_AVT_COMPONENTS];

                                CHAR                u8_String[256];

                            } AVT_COMPONENT_SET;

#define     __structAVT_COMPONENT_SET__DEFINED_

#endif  //  #if !defined(__structAVT_COMPONENT_SET__DEFINED_)

#if !defined(__structECIF_MESSAGE_COUNTERS__DEFINED_)

typedef     struct structECIF_MESSAGE_COUNTERS
                            {
                                USHORT  wRxMessages;

                                USHORT  wRegistrations,
                                        wDeregistrations,
                                        wReads,
                                        wUpdates,
                                        wTransients;

                                USHORT  wRegistrationErrors,
                                        wDeregistrationErrors,
                                        wReadErrors,
                                        wUpdateErrors,
                                        wTransientErrors;

                                USHORT  wUnexpectedErrors;

                                USHORT  wRegistrationACKs,
                                        wDeregistrationACKs,
                                        wReadACKs,
                                        wUpdateACKs,
                                        wTransientACKs;

                                USHORT  wUnexpectedACKs;

                                USHORT  wRegistrationNAKs,
                                        wDeregistrationNAKs,
                                        wReadNAKs,
                                        wUpdateNAKs,
                                        wTransientNAKs;

                                USHORT  wUnexpectedNAKs;

                            } ECIF_MESSAGE_COUNTERS;

#define     __structECIF_MESSAGE_COUNTERS__DEFINED_

#endif  //  #if !defined(__structECIF_MESSAGE_COUNTERS__DEFINED_)

////////////////////////////////////////////////////////////////////////////

#pragma     pack(pop)

////////////////////////////////////////////////////////////////////////////

#endif  //  #if !defined(__A104_AVT_COMPONENTS_H__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
