#ifndef ReceiverThreadH
#define ReceiverThreadH

#include <windows.h>

DWORD WINAPI RequestorThreadProc(LPVOID lpParam);
DWORD WINAPI RecieverThreadProc(LPVOID lpParam);

#endif