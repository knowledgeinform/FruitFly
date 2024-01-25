#include <windows.h>
#include <stdlib.h>
#include <stdio.h>

static const float GPS_TIME_CORRECTION_SECONDS = -15.0f;
static float recvdTimeCorrectionSeconds = 0;
//static const unsigned int TIME_UPDATE_PERIOD_MS = 60000;
static const unsigned int TIME_UPDATE_PERIOD_MS = 5000;
static unsigned int s_lastSystemTimeUpdate_ms = 0;
static int s_lastGPSSecond = 0;


BOOL verifyChecksum(const char* const pSentenceBuffer, const int numBytesInSentence)
{
	unsigned int checksum = 0;
	int i;
	unsigned int advertisedChecksum;

	if (pSentenceBuffer[numBytesInSentence - 3] != '*')
	{
		// Optional checksum not specified
		return TRUE;
	}
	else
	{
		for (i = 1; i < (numBytesInSentence - 3); ++i)
		{
			checksum ^= pSentenceBuffer[i];
		}

		sscanf_s(&pSentenceBuffer[numBytesInSentence - 2], "%x", &advertisedChecksum);

		return (checksum == advertisedChecksum);
	}
}

void UpdateSystemTime(const int hour,
					  const int minute,
					  const float second,
					  const int day,
					  const int month,
					  const int year)
{
	HANDLE      hToken;     // process token
	TOKEN_PRIVILEGES tp;    // token provileges
	TOKEN_PRIVILEGES oldtp; // old token privileges
	DWORD    dwSize = sizeof (TOKEN_PRIVILEGES);          
	LUID     luid;          
	SYSTEMTIME time;

	// Set the SE_SYSTEMTIME_NAME privilege to our current
    // process, so we can call SetSystemTime()
	if (!OpenProcessToken (GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY,
		&hToken))
	{
		printf ("OpenProcessToken() failed with code %d\n", GetLastError());
		return;
	}
	if (!LookupPrivilegeValue (NULL, SE_SYSTEMTIME_NAME, &luid))
	{
		printf ("LookupPrivilege() failed with code %d\n", GetLastError());
		CloseHandle (hToken);
		return;
	}

	ZeroMemory (&tp, sizeof (tp));
	tp.PrivilegeCount = 1;
	tp.Privileges[0].Luid = luid;
	tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

	/* Adjust Token privileges */
	if (!AdjustTokenPrivileges (hToken, FALSE, &tp, sizeof(TOKEN_PRIVILEGES), 
		&oldtp, &dwSize))
	{
		printf ("AdjustTokenPrivileges() failed with code %d\n", GetLastError());
		CloseHandle (hToken);
		return;
	}

	time.wHour = hour;
	time.wMinute = minute;
	time.wSecond = (int)second;
	time.wMilliseconds = (int)((second - ((int)second)) * 1000);
	time.wDay = day;
	time.wMonth = month;
	time.wYear = year;
	if (!SetSystemTime (&time))
	{
		printf ("SetSystemTime() failed with code %d\n", GetLastError());
		CloseHandle (hToken);
		return;
	}

	// disable SE_SYSTEMTIME_NAME again
	AdjustTokenPrivileges (hToken, FALSE, &oldtp, dwSize, NULL, NULL);
	if (GetLastError() != ERROR_SUCCESS)
	{
		printf ("AdjustTokenPrivileges() failed with code %d\n", GetLastError());
		CloseHandle (hToken);
		return;
	}

	CloseHandle (hToken);
}

char* strtok_with_blanks(char *pString, const char delimeter, char **pNextToken)
{
	char* s_pCurrToken;
	int i = 0;

	if (pString)
	{
		s_pCurrToken = pString;
	}
	else
	{
		s_pCurrToken = *pNextToken;
	}

	while ((s_pCurrToken[i] != delimeter) && (s_pCurrToken[i] != '\0'))
	{
		++i;
	}

	s_pCurrToken[i] = '\0';

	*pNextToken = &s_pCurrToken[i + 1];

	return s_pCurrToken;
}

void correctForGPSTimeError(int* const pYear,
							int* const pMonth,
							int* const pDay,
							int* const pHour,
							int* const pMinute,
							float* const pSecond)
{
	*pSecond += recvdTimeCorrectionSeconds;

	if (*pSecond < 0)
	{
		--(*pMinute);
		*pSecond += 60;
	}
	if (*pSecond > 59)
	{
		++(*pMinute);
		*pSecond -= 60;
	}

	if (*pMinute < 0)
	{
		--(*pHour);
		*pMinute += 60;
	}
	if (*pMinute > 59)
	{
		++(*pHour);
		*pMinute -= 60;
	}

	if (*pHour < 0)
	{
		--(*pDay);
		*pHour += 24;
	}
	if (*pHour > 23)
	{
		++(*pDay);
		*pHour -= 24;
	}

	if (*pDay < 1)
	{
		--(*pMonth);
		if (*pMonth == 1 ||
			*pMonth == 3 ||
			*pMonth == 5 ||
			*pMonth == 7 ||
			*pMonth == 8 ||
			*pMonth == 10 ||
            *pMonth == 12)
		{
			*pDay = 31;
		}
		else if (*pMonth == 2)
		{
			if (((*pYear) % 4 == 0) && ((*pYear) % 100 != 0))
			{
				*pDay = 29;
			}
			else
			{
				*pDay = 28;
			}
		}
		else
		{
			*pDay = 30;
		}
	}
	if ((*pMonth == 1 ||
			*pMonth == 3 ||
			*pMonth == 5 ||
			*pMonth == 7 ||
			*pMonth == 8 ||
			*pMonth == 10 ||
            *pMonth == 12)
			&& *pDay > 31)
	{
		++(*pMonth);
		*pDay = 1;
	}
	else if ((*pMonth == 2) && (*pYear % 4 == 0) && *pDay > 29)
	{
		++(*pMonth);
		*pDay = 1;
	}
	else if ((*pMonth == 2) && *pDay > 28)
	{
		++(*pMonth);
		*pDay = 1;
	}
	else if (*pDay > 30)
	{
		++(*pMonth);
		*pDay = 1;
	}
	
	if (*pMonth == 0)
	{
		--(*pYear);
		*pMonth = 12;
	}
	if (*pMonth == 13)
	{
		++(*pYear);
		*pMonth = 1;
	}
}

void parseSentence(char* pSentenceBuffer, const numBytesInSentence)
{
	char *pToken;
	int tokenNum = 0;
	int hour;
	int minute;
	float second;
	int day;
	int month;
	int year;
	char *pNextToken = NULL;


	if ((numBytesInSentence > 0) &&
		(pSentenceBuffer[0] == '$') &&
		verifyChecksum(pSentenceBuffer, numBytesInSentence))
	{
		pToken = strtok_with_blanks(pSentenceBuffer, ',', &pNextToken);
		while (pToken != NULL)
		{
			if (tokenNum == 0)
			{
				if (!strcmp(pToken, "$GPRMC"))
				{
					++tokenNum;
				}
				else
				{
					return;
				}
			}
			else if (tokenNum == 1)
			{
				sscanf_s(pToken, "%2d%2d%f", &hour, &minute, &second);
				if (hour < 0 || hour > 24 ||
					minute < 0 || minute > 60 ||
					second < 0 || second > 60)
				{
					return;
				}				
				++tokenNum;
			}
			else if (tokenNum == 9)
			{
				sscanf_s(pToken, "%2d%2d%2d", &day, &month, &year);

				if (day < 0 || day > 31 ||
					month < 0 || month > 12 ||
					year < 0 || year > 99)
				{
					return;
				}

				if ((s_lastGPSSecond != ((int)second)) &&
				    ((GetTickCount() - s_lastSystemTimeUpdate_ms) > TIME_UPDATE_PERIOD_MS))
				{
					correctForGPSTimeError(&year, &month, &day, &hour, &minute, &second);

					year += 2000;
					printf("Setting system time to: %02d:%02d:%02d %02d:%02d:%4d\n", hour, minute, (int)second, day, month, year);
					UpdateSystemTime(hour, minute, second, day, month, year);
					s_lastSystemTimeUpdate_ms = GetTickCount();
				}
				
				s_lastGPSSecond = (int)second;

				return;
			}
			else
			{
				++tokenNum;
			}

			pToken = strtok_with_blanks(NULL, ',', &pNextToken);
		}
	}
}

int main(int argc, char **argv)
{
	char errorMsg[128];
	char pReceiveBuffer[128];
	char pSentenceBuffer[128];
	int numBytesInSentence = 0;
	DCB dcbConfig;
	HANDLE hSerialPort;
	//OVERLAPPED overlapped;
	DWORD numBytesRead = 0;
	unsigned int i;
	DWORD errorNum;
	char fromGps;
	char toGps;
	



	if (argc < 2)
	{
		MessageBox(NULL, "Serial port not specified", "", 0);
		return 1;
	}
	if (argc < 3)
	{
		MessageBox(NULL, "Assumed input time reference not specified (gps or utc)", "", 0);
		return 1;
	}
	if (argc < 4)
	{
		MessageBox(NULL, "Desired resulting time reference not specified (gps or utc)", "", 0);
		return 1;
	}
	
	if (strcmp (argv[2], "gps\0") == 0 || strcmp (argv[2], "w7\0") == 0)
		fromGps = 1;
	else
		fromGps = 0;
	if (strcmp (argv[3], "gps\0") == 0)
		toGps = 1;
	else
		toGps = 0;
	
	if (toGps)
		printf ("Setting system time to gps,");
	else
		printf ("Setting system time to utc,");
	if (fromGps)
		printf (" assuming from receiver as gps.\r\n");
	else
		printf (" assuming from receiver as utc.\r\n");


	if (fromGps == toGps)
		recvdTimeCorrectionSeconds = 0;
	else if (fromGps && !toGps)
		recvdTimeCorrectionSeconds = GPS_TIME_CORRECTION_SECONDS;
	else if (!fromGps && toGps)
		recvdTimeCorrectionSeconds = -GPS_TIME_CORRECTION_SECONDS;
	
	
	hSerialPort = CreateFile(argv[1],  
						     GENERIC_READ | GENERIC_WRITE, 
						     0, 
						     0, 
						     OPEN_EXISTING,
						     0,//FILE_FLAG_OVERLAPPED,
						     0);

	if (hSerialPort == INVALID_HANDLE_VALUE)
	{
		sprintf_s(errorMsg, sizeof(errorMsg), "Could not open port %s\n", argv[1]);
		MessageBox(NULL, errorMsg, "", 0);
		return 1;
	}

	

	if(GetCommState(hSerialPort, &dcbConfig))
	{
		dcbConfig.BaudRate = 4800;
		dcbConfig.ByteSize = 8;
		dcbConfig.Parity = NOPARITY;
		dcbConfig.StopBits = ONESTOPBIT;
		dcbConfig.fBinary = TRUE;
		dcbConfig.fParity = TRUE;
	}
	else
	{
		return 1;
	}


	if(!SetCommState(hSerialPort, &dcbConfig))
	{
		return 1;
	}


	while (TRUE)
	{
		errorNum = GetLastError();
		if (!ReadFile(hSerialPort, pReceiveBuffer, sizeof(pReceiveBuffer), &numBytesRead, NULL))
		{
			errorNum = GetLastError();
			return 1;
		}

		for (i = 0; i < numBytesRead; ++i)
		{
			if (pReceiveBuffer[i] == '$')
			{
				numBytesInSentence = 0;
			}
			else if (pReceiveBuffer[i] == '\n')
			{
				parseSentence(pSentenceBuffer, numBytesInSentence);
				numBytesInSentence = 0;
			}
			else if (numBytesInSentence == sizeof(pSentenceBuffer))
			{
				numBytesInSentence = 0;
			}
			else
			{
				++numBytesInSentence;
			}


			pSentenceBuffer[numBytesInSentence] = pReceiveBuffer[i];
		}
	}

	return 0;
}