/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   BitMasks.h                                              */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :                                                           */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/BitMasks.h_V   1.1   May 23 2006 14:21:46   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/BitMasks.h_V  $
*
*   Rev 1.1   May 23 2006 14:21:46   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.0   Oct 15 2004 07:57:00   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__BIT_MASKS_H__INCLUDED_)

#define     __BIT_MASKS_H__INCLUDED_

////////////////////////////////////////////////////////////////////////////
//  Global Definitions

#define     BIT0                0x00000001
#define     BIT1                0x00000002
#define     BIT2                0x00000004
#define     BIT3                0x00000008
#define     BIT4                0x00000010
#define     BIT5                0x00000020
#define     BIT6                0x00000040
#define     BIT7                0x00000080

#define     BIT8                0x00000100
#define     BIT9                0x00000200
#define     BIT10               0x00000400
#define     BIT11               0x00000800
#define     BIT12               0x00001000
#define     BIT13               0x00002000
#define     BIT14               0x00004000
#define     BIT15               0x00008000

#define     BIT16               0x00010000
#define     BIT17               0x00020000
#define     BIT18               0x00040000
#define     BIT19               0x00080000
#define     BIT20               0x00100000
#define     BIT21               0x00200000
#define     BIT22               0x00400000
#define     BIT23               0x00800000

#define     BIT24               0x01000000
#define     BIT25               0x02000000
#define     BIT26               0x04000000
#define     BIT27               0x08000000
#define     BIT28               0x10000000
#define     BIT29               0x20000000
#define     BIT30               0x40000000
#define     BIT31               0x80000000

/*-----MACROS---------------------------------------------------------------*/

#define     BIT0_STATE(x)           ((x) & BIT0)
#define     BIT1_STATE(x)           ((x) & BIT1)
#define     BIT2_STATE(x)           ((x) & BIT2)
#define     BIT3_STATE(x)           ((x) & BIT3)
#define     BIT4_STATE(x)           ((x) & BIT4)
#define     BIT5_STATE(x)           ((x) & BIT5)
#define     BIT6_STATE(x)           ((x) & BIT6)
#define     BIT7_STATE(x)           ((x) & BIT7)

#define     BIT8_STATE(x)           ((x) & BIT8)
#define     BIT9_STATE(x)           ((x) & BIT9)
#define     BIT10_STATE(x)          ((x) & BIT10)
#define     BIT11_STATE(x)          ((x) & BIT11)
#define     BIT12_STATE(x)          ((x) & BIT12)
#define     BIT13_STATE(x)          ((x) & BIT13)
#define     BIT14_STATE(x)          ((x) & BIT14)
#define     BIT15_STATE(x)          ((x) & BIT15)

#define     BIT16_STATE(x)          ((x) & BIT16)
#define     BIT17_STATE(x)          ((x) & BIT17)
#define     BIT18_STATE(x)          ((x) & BIT18)
#define     BIT19_STATE(x)          ((x) & BIT19)
#define     BIT20_STATE(x)          ((x) & BIT20)
#define     BIT21_STATE(x)          ((x) & BIT21)
#define     BIT22_STATE(x)          ((x) & BIT22)
#define     BIT23_STATE(x)          ((x) & BIT23)

#define     BIT24_STATE(x)          ((x) & BIT24)
#define     BIT25_STATE(x)          ((x) & BIT25)
#define     BIT26_STATE(x)          ((x) & BIT26)
#define     BIT27_STATE(x)          ((x) & BIT27)
#define     BIT28_STATE(x)          ((x) & BIT28)
#define     BIT29_STATE(x)          ((x) & BIT29)
#define     BIT30_STATE(x)          ((x) & BIT30)
#define     BIT31_STATE(x)          ((x) & BIT31)

#define     LOW_NIBBLE(x)           ((x) & 0x0F)

#define     LOW_BYTE(x)             ((UCHAR)((x) & 0x0FF))

#define     LOW_WORD(x)             ((USHORT)((x) & 0xFFFF))
#define     HIGH_WORD(x)            LOW_WORD(((x) >> 16))

////////////////////////////////////////////////////////////////////////////

#endif  //  #if !defined(__BIT_MASKS_H__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
