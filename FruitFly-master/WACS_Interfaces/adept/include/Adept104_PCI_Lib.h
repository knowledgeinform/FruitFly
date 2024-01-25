/*--------------------------------------------------------------------------*/
/*                                                                          */
/*                          OCTEC LTD (c)                                   */
/*                          =========                                       */
/*                                                                          */
/*  File        :   Adept104_PCI_Lib.h                                      */
/*                                                                          */
/*  Owner       :   Gordon Smith                                            */
/*                                                                          */
/*  Description :   Class definition for CAdept104_PCI.                     */
/*                                                                          */
/*--------------------------------------------------------------------------*/
/*
*$Header:   L:/DESIGN/in5201/14/217/h/VCS/Adept104_PCI_Lib.h_V   1.2   May 23 2006 14:21:44   SmithGo  $
*
*$Log:   L:/DESIGN/in5201/14/217/h/VCS/Adept104_PCI_Lib.h_V  $
*
*   Rev 1.2   May 23 2006 14:21:44   SmithGo
*   Force 8 byte structure alignment
*
*   Rev 1.1   May 05 2006 13:33:06   SmithGo
*   Changes to support DLL version of library
*
*   Rev 1.0   Oct 15 2004 08:11:20   GordonSm
*   Initial revision (in PVCS)
*
*---------------------------------------------------------------------------*/

#if !defined(__ADEPT104_PCI_LIB_H__INCLUDED_)

#define     __ADEPT104_PCI_LIB_H__INCLUDED_

////////////////////////////////////////////////////////////////////////////

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

////////////////////////////////////////////////////////////////////////////
//  Compiler switches
//
//  Used to help minimise the apparent number of error and warning messages.
//
#pragma     warning(disable: 4711)      //  Selected for auto-inlining

////////////////////////////////////////////////////////////////////////////
//  Include Files

#include    "AvtDataType.h"                 //  AVT_DATA
#include    "AvtLogDataTypes.h"             //  AVT_LOG_CONFIG, etc.
#include    "A104_AvtComponents.h"          //  AVT_COMPONENT_SET
#include    "A104_DataTypes.h"              //  A104_WRITE_REGISTER, etc.
#include    "A104_ErrorCodes.h"             //  USER_ERRORS
#include    "BitMasks.h"                    //  BIT16

////////////////////////////////////////////////////////////////////////////

//  _LIB is defined (by default, by Microsoft, in the VC++ project) when the
//  static libray version of CAdept is being generated.
//  It should be defined manually for Borland C++ Builder projects.
#if defined(_LIB)

    #undef      ADEPT104_PCI_DLL_API

    #define     ADEPT104_PCI_DLL_API

//  _USRDLL is defined (by default, by Microsoft, in the VC++ project) when
//  the dynamic link libray version of CAdept is being generated.
//  It should be defined manually for Borland C++ Builder projects.
#elif   defined(_USRDLL)

    #undef      ADEPT104_PCI_DLL_API

    //  The following "#if defined" block is the standard way of creating
    //  macros which make exporting from a DLL simpler.  All files within
    //  this DLL are compiled with the ADEPT104DLL_PCI_EXPORTS symbol
    //  defined on the command line.  This symbol should not be defined on
    //  any project that uses this DLL.  This way any other project whose
    //  source files include this file see ADEPT104_PCI_DLL_API functions as
    //  being imported from a DLL, whereas this DLL sees symbols defined
    //  with this macro as being exported.

    #if defined(ADEPT104DLL_PCI_EXPORTS)

        #define     ADEPT104_PCI_DLL_API    __declspec(dllexport)

    #else

        #define     ADEPT104_PCI_DLL_API    __declspec(dllimport)

    #endif

#else   //  Neither _LIB nor _USRDLL are defined

//  The library/DLL must have been built already and is now being used

    //  If ADEPT104_PCI_DLL_API hasn't been defined it's the library that's
    //  being used so a 'bland' ADEPT104_PCI_DLL_API is required
    #if !defined(ADEPT104_PCI_DLL_API)

        #define     ADEPT104_PCI_DLL_API

    #endif  //  #if !defined(ADEPT104_PCI_DLL_API)

#endif  //  #if defined(_LIB)

////////////////////////////////////////////////////////////////////////////
//  Classes should be packed on 8 byte boundaries

#pragma     pack(push, 8)

////////////////////////////////////////////////////////////////////////////

class ADEPT104_PCI_DLL_API CAdept104_PCI
{
//  Construction/destruction
public:
    //  adept104_pci_lib.cpp
    CAdept104_PCI();
    virtual ~CAdept104_PCI();

//  Constants
private:
    //  The 'enum' hack avoids using #define in C++ (see Effective C++)
    enum    {   m_STRING_BUFF_LEN   = 1024  };

    //  events.cpp
    static LPCTSTR          m_pszEventNames[NO_OF_A104_USER_EVENTS];

    //  loader.cpp
    static LPCTSTR          m_pszDeviceDriverFilename;
    static LPCTSTR          m_pszRegistryKey98;
    static LPCTSTR          m_pszRegistryKey2000;

public:
    //  Error codes for CAdept104_PCI.
    //  The first error code that the device driver can generate is
    //  USER_ERRORS which is set such that there should be no conflict with
    //  error codes generated by the Windows API (see GetLastError()
    //  documentation).
    //  Offseting the CAdept104_PCI errors from the device driver errors is
    //  done to reduce confusion.

    //  The 'enum' hack avoids using #define in C++ (see Effective C++)
    enum    {
                m_FIRST_APP_ERROR       = BIT16,

                m_EVENT_HANDLE_CLOSED   = -1,
            };

    enum    enumCAdept104_PCI_ERROR_CODES
                    {
                        //  No error must be indicated by 0
                        A104EC_NO_ERROR                         = 0,

                        //  First 'real' error, hence m_FIRST_APP_ERROR
                        A104EC_WINDOWS_VERSION_NOT_SUPPORTED    = USER_ERRORS | m_FIRST_APP_ERROR,

                        A104EC_CANNOT_FIND_ANY_ADEPT104S,
                        A104EC_ILLEGAL_ADEPT104_ID,

                        A104EC_INVALID_INPUT_PARAMETER,

                        A104EC_DISABLED_IN_THIS_BUILD,

                        A104EC_EVENT_HANDLE_NULL,
                    };

//  Data
private:
    char        m_szErrorBuffer[m_STRING_BUFF_LEN];
    char        m_szExtraBuffer[m_STRING_BUFF_LEN];

    int         m_nLastError;               //  Windows API and/or CAdept104_PCI
    //  Windows 3 (16-bit), 95, 98 (and ME), (NT) 4, 2000 (and XP)
    int         m_nWindowsVersion;

    BOOL        m_bWindowsNT;               //  Including 2000/XP

    DWORD       m_dwAdept104,               //  Should match m_dwUnit in driver
                m_dwInstalledAdept104s;

    //  The handle errors are set to m_EVENT_HANDLE_CLOSED on initialisation,
    //  0 to indicate no error when creating the associated event or the
    //  return from the GetLastError() API otherwise
    DWORD       m_dwHandleErrors[NO_OF_A104_USER_EVENTS];

    HANDLE      m_hDevice;
    HANDLE      m_hFirstInstanceMutex;

    A104_USER_EVENTS    m_UserEvents;

//  Implementation
private:
    //  adept104_pci_lib.cpp
    void        _CheckWindowsVersion(void);

    //  cmd_query.cpp
    BOOL        _Command(AVT_CMD_DATA* pAvtCommand,
                            LPCTSTR* ppszError = NULL);

    //  device_io_ctrl.cpp
    LPCTSTR     _A104_PCI_Control(DWORD dwCtrlCode,
                                    void* pInput,
                                    DWORD dwInputSize,
                                    void* pOutput = NULL,
                                    DWORD dwOutputSize = 0,
                                    DWORD* pdwBytesRead = NULL);

    //  ecif.cpp
    BOOL        _ecifCommand(DWORD dwCtrlCode,
                                void* pInput,
                                DWORD dwInputSize,
                                LPCTSTR* ppszError = NULL);

    //  errors.cpp
    LPCTSTR     _GetLastErrorCode_A104pci(void);
    LPCTSTR     _GetLastErrorCode_CAdept104_PCI(void);
    LPCTSTR     _GetWindowsErrorDescription(DWORD dwErrorCode);

    //  events.cpp
    HANDLE      _eventsCreateEvent(LPCTSTR pszEventNameRoot);

    //  loader.cpp
    DWORD       _CountInstalledDevices(void);
    DWORD       _CountInstalledDevicesWin98(HKEY hKey);

    //  registers.cpp
    BOOL        _WriteRegister(A104_WRITE_REGISTER* pRegister,
                                LPCTSTR* ppszError = NULL);

public:
    //  adept104_pci_lib.cpp
    //  Override default assignment operator (DO NOT USE)
    CAdept104_PCI operator = (const CAdept104_PCI& ref_RHS);

    //  Override default copy constructor (DO NOT USE)
    CAdept104_PCI(const CAdept104_PCI& ref_Adept104);

    //  cmd_query.cpp
    BOOL        Command(ULONG dwBlock,
                            ULONG dwField,
                            UCHAR ucData,
                            LPCTSTR* ppszError = NULL);
    BOOL        Command(ULONG dwBlock,
                            ULONG dwField,
                            USHORT wData,
                            LPCTSTR* ppszError = NULL);
    BOOL        Command(ULONG dwBlock,
                            ULONG dwField,
                            ULONG dwData,
                            LPCTSTR* ppszError = NULL);
    BOOL        Command(ULONG dwBlock,
                            ULONG dwField,
                            double dData,
                            LPCTSTR* ppszError = NULL);
    BOOL        Query(ULONG dwBlock,
                        ULONG dwField,
                        LPCTSTR* ppszError = NULL);
    BOOL        ReadStatusReplies(AVT_CMD_DATA* pStatusReplyBuffer,
                                    DWORD* pdwBufferSize,
                                    LPCTSTR* ppszError = NULL);

    //  ecif.cpp
    BOOL        ECIF_CancelRegistration(UCHAR ucComponentSetID,
                                            LPCTSTR* ppszError = NULL);
    BOOL        ECIF_ReadData(AVT_COMPONENT_SET* pReplyBuffer,
                                DWORD* pdwBufferSize,
                                LPCTSTR* ppszError = NULL);
    BOOL        ECIF_RegisterComponentSet(AVT_COMPONENT_SET* pACS,
                                            LPCTSTR* ppszError = NULL);
    BOOL        ECIF_TransientComponentSet(AVT_COMPONENT_SET* pACS,
                                            LPCTSTR* ppszError = NULL);
    BOOL        ECIF_UpdateComponentSet(AVT_COMPONENT_SET* pACS,
                                            LPCTSTR* ppszError = NULL);

    //  errors.cpp
    LPCTSTR     GetLastErrorCode(void);
    int         GetLastErrorCode(void) const
                    {
                    return(m_nLastError);
                    }
    BOOL        IsLastErrorCode(int nErrorCode) const
                    {
                    return(m_nLastError == nErrorCode);
                    }

    //  events.cpp
    BOOL        CloseAllEventHandles(void);
    DWORD       CloseEventHandle(int nEvent,
                                    LPCTSTR* ppszError);
    BOOL        CloseKernelEvents(LPCTSTR* ppszError);
    BOOL        CreateAllEventHandles(void);
    BOOL        CreateEventHandle(int nEvent,
                                    LPCTSTR* ppszError);
    BOOL        CreateKernelEvents(LPCTSTR* ppszError);
    LPCTSTR     GetEventHandle(int nEvent,
                                PHANDLE phEvent);
    DWORD       GetEventHandleError(int nEvent) const;

    //  loader.cpp
    BOOL        DeviceDriverLoaded(void) const
                    {
                    return(INVALID_HANDLE_VALUE != m_hDevice);
                    }
    LPCTSTR     GetAdept104_ID(DWORD dwAdept104);
    DWORD       InstalledAdept104s(void) const
                    {
                    return(m_dwInstalledAdept104s);
                    }
    BOOL        LoadDeviceDriver(DWORD dwAdept104 = 0,
                                    LPCTSTR* ppszError = NULL);
    BOOL        UnloadDeviceDriver(LPCTSTR* ppszError = NULL);

    //  logging.cpp
    BOOL        ConfigureDataLogging(AVT_LOG_CONFIG* pWrite,
                                        AVT_LOG_CONFIG* pRead = NULL,
                                        LPCTSTR* ppszError = NULL);
    BOOL        ConfigureDataLoggingEx(AVT_LOG_CONFIG_EX* pWrite,
                                        AVT_LOG_CONFIG_EX* pRead = NULL,
                                        LPCTSTR* ppszError = NULL);
    BOOL        GetDataLoggingConfiguration(AVT_LOG_CONFIG* pRead,
                                                LPCTSTR* ppszError = NULL);
    BOOL        GetDataLoggingConfigurationEx(AVT_LOG_CONFIG_EX* pRead,
                                                LPCTSTR* ppszError = NULL);
    BOOL        ReadLogData(AVT_LOG_DATA* pLogDataBuffer,
                                DWORD* pdwBufferSize,
                                LPCTSTR* ppszError = NULL);
    BOOL        ReadLogDataEx(AVT_LOG_DATA_EX* pLogDataBuffer,
                                DWORD* pdwBufferSize,
                                LPCTSTR* ppszError = NULL);

    //  memory.cpp
    BOOL        ReadMemory(DWORD dwReadAddress,
                            BYTE* pucReadData,
                            LPCTSTR* ppszError = NULL);
    BOOL        ReadMemory(DWORD dwReadAddress,
                            WORD* pwReadData,
                            LPCTSTR* ppszError = NULL);
    BOOL        ReadMemory(DWORD dwReadAddress,
                            DWORD* pdwReadData,
                            LPCTSTR* ppszError = NULL);
    BOOL        ReadMemory(A104_READ_COMMAND* pReadCommand,
                            void* pvReadData,
                            DWORD* pdwReadDataSize,
                            LPCTSTR* ppszError = NULL);
    BOOL        WriteMemory(DWORD dwWriteAddress,
                                BYTE ucWriteData,
                                LPCTSTR* ppszError = NULL);
    BOOL        WriteMemory(DWORD dwWriteAddress,
                                WORD wWriteData,
                                LPCTSTR* ppszError = NULL);
    BOOL        WriteMemory(DWORD dwWriteAddress,
                                DWORD dwWriteData,
                                LPCTSTR* ppszError = NULL);
    BOOL        WriteMemory(A104_WRITE_COMMAND* pWriteData,
                                DWORD dwWriteDataSize,
                                LPCTSTR* ppszError = NULL);

    //  registers.cpp
    BOOL        AssertReset(LPCTSTR* ppszError = NULL); //  Disabled in current build
    BOOL        EnableInterrupts(BOOL bEnable,
                                    LPCTSTR* ppszError = NULL);
    BOOL        InterruptAVT(LPCTSTR* ppszError = NULL);
    BOOL        PulseReset(LPCTSTR* ppszError = NULL);
    BOOL        ReadRegister(ULONG dwRegister,
                                PULONG pdwData,
                                LPCTSTR* ppszError = NULL);
    BOOL        RemoveReset(LPCTSTR* ppszError = NULL); //  Disabled in current build
    BOOL        WriteRegister(ULONG dwRegister,
                                ULONG dwData,
                                LPCTSTR* ppszError = NULL);
};

////////////////////////////////////////////////////////////////////////////

#pragma     pack(pop)

////////////////////////////////////////////////////////////////////////////

#endif  //  !defined(__ADEPT104_PCI_LIB_H__INCLUDED_)

////////////////////////////////////////////////////////////////////////////
