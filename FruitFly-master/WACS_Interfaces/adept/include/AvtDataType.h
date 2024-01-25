/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   AvtDataType.h                                           */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :   Define the AVT_DATA data type for the Adept104 device   */
/*                  driver, etc.                                            */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/AvtDataType.h_V   1.1   May 23 2006 14:21:46   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/AvtDataType.h_V  $
*
*   Rev 1.1   May 23 2006 14:21:46   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.2   May 23 2006 13:59:04   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.1   Feb 14 2006 16:32:46   SmithGo
*   Events. DPR -> AVT copy fix.  IEEE support for EXIF
*
*   Rev 1.0   Oct 15 2004 07:56:58   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__AvtDataType_h__INCLUDED_)

#define     __AvtDataType_h__INCLUDED_

////////////////////////////////////////////////////////////////////////////

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

////////////////////////////////////////////////////////////////////////////
//  Global Definitions

////////////////////////////////////////////////////////////////////////////
//  Structures should be packed on 8 byte boundaries

#pragma     pack(push, 8)

////////////////////////////////////////////////////////////////////////////
//  Global Type Definitions

/*
 *      Structure for storing data in command/status and log messages which
 *      go to and from the AVT.
 *
 *      If the incoming data from the AVT is 'smaller' than 32 bits use:
 *
 *          uc[3] for byte format
 *
 *          w[1] for word-format
 */
#if !defined(__structAVT_DATA__DEFINED_)

typedef     struct structAVT_DATA
                            {
                                union   {
                                            UCHAR   uc[4];
                                            USHORT  w[2];
                                            long    l;
                                            ULONG   dw;
                                            float   f;
                                        };
                            } AVT_DATA;

#define     __structAVT_DATA__DEFINED_

#endif  //  #if !defined(__structAVT_DATA__DEFINED_)

////////////////////////////////////////////////////////////////////////////

#pragma     pack(pop)

////////////////////////////////////////////////////////////////////////////

#endif  //  #if !defined(__AvtDataType_h__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
