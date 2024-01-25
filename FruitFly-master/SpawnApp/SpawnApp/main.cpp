#include <windows.h>
#include <string>
#include <process.h>
#include "IniFile.h"
#include "ProcessUtil.h"

using namespace std;

typedef struct
{
	short messageID;
	short messageLength;
} SpawnPacket;


void fixClasspath(string &str_in)
{
     for(int i = 0; i < str_in.length(); i++)
     {
		if (str_in[i] == '&')
		{
			str_in[i] = ';';
		}
     }    
}

//int APIENTRY WinMain(HINSTANCE hInstance,
//                     HINSTANCE hPrevInstance,
//                     LPSTR     lpCmdLine,
//                     int       nCmdShow)
int main()
{
	const string tIniFileName = "SpawnApp.ini";

	CIniFile oIniFileReader;
	oIniFileReader.OpenIniFile(tIniFileName.c_str());
	string tListenPort = oIniFileReader.ReadString("SpawnApp", "ListenPort", "");
	string tExecutable1Name = oIniFileReader.ReadString("SpawnApp", "Executable1Name", "");
	string tExecutable2Name = oIniFileReader.ReadString("SpawnApp", "Executable2Name", "");
	string tWorkingDirectory = oIniFileReader.ReadString("SpawnApp", "WorkingDirectory", "");

	fixClasspath(tExecutable1Name);
	fixClasspath(tExecutable2Name);


	::PROCESS_INFORMATION process1InformationStruct;
	::PROCESS_INFORMATION process2InformationStruct;

	process1InformationStruct.hProcess = 0;
	process2InformationStruct.hProcess = 0;

	WSAData wsaData;
	WSAStartup(MAKEWORD(1, 1), &wsaData);

	SOCKADDR_IN oAddr; 
	oAddr.sin_family = AF_INET;  

	oAddr.sin_port = htons(atoi(tListenPort.c_str()));

	oAddr.sin_addr.s_addr = htonl(INADDR_ANY);  

	SOCKET hServerSocket;
	hServerSocket = socket (AF_INET, SOCK_STREAM, IPPROTO_TCP);

	bind(hServerSocket, (LPSOCKADDR)&oAddr, sizeof(oAddr));

	listen(hServerSocket, SOMAXCONN);

	while (true)
	{
		printf("Waiting for client connection...\n");
		SOCKET hClientSocket = accept(hServerSocket, NULL, NULL);

		bool socketTerminated = false;
		do
		{
			printf("\nWaiting for spawn command...\n");

			SpawnPacket spawnPacket;
			int totalBytesReceived = 0;
			int numBytesReceived;
			do
			{
				numBytesReceived = recv(hClientSocket, (char*)(((int)&spawnPacket) + totalBytesReceived), sizeof(SpawnPacket) - totalBytesReceived, 0);
				totalBytesReceived += numBytesReceived;
			} 
			while (totalBytesReceived < sizeof(SpawnPacket) && numBytesReceived > 0);

			if (numBytesReceived > 0)
			{
				spawnPacket.messageID = ntohs(spawnPacket.messageID);
				spawnPacket.messageLength = ntohs(spawnPacket.messageLength);

				if ((spawnPacket.messageID == 0) && (spawnPacket.messageLength == sizeof(spawnPacket)))
				{
					if (process1InformationStruct.hProcess != 0)
					{
						TerminateProcess(process1InformationStruct.hProcess, 0);
						CloseHandle(process1InformationStruct.hProcess);
						CloseHandle(process1InformationStruct.hThread);
					}

					if (process2InformationStruct.hProcess != 0)
					{
						TerminateProcess(process2InformationStruct.hProcess, 0);
						CloseHandle(process2InformationStruct.hProcess);
						CloseHandle(process2InformationStruct.hThread);
					}

					::STARTUPINFO startupInfo;
					

					::ZeroMemory(&startupInfo,sizeof(startupInfo));
					startupInfo.cb = sizeof(startupInfo);

					printf("Spawning WACS Agent\n");
					::CreateProcess(NULL,
									(char*)tExecutable1Name.c_str(),
									NULL,
									NULL,
									FALSE,
									CREATE_NEW_CONSOLE,
									NULL,
									tWorkingDirectory.c_str(),
									&startupInfo,
									&process1InformationStruct);

					printf("Spawning WACS Display\n");
					::CreateProcess(NULL,
									(char*)tExecutable2Name.c_str(),
									NULL,
									NULL,
									FALSE,
									CREATE_NEW_CONSOLE,
									NULL,
									tWorkingDirectory.c_str(),
									&startupInfo,
									&process2InformationStruct);
				}
				else
				{
					// Dump unrecognized packet
					char dummy;
					for (int i = 0; i < spawnPacket.messageLength; ++i)
					{
						recv(hClientSocket, &dummy, 1, 0);
					}
				}
			}
			else
			{
				socketTerminated = true;
			}
		//else if (cCommand == 1)
		//{
		//	//
		//	// Forcefully terminate application
		//	//
		//	KILL_PROC_BY_NAME(tProcessName.c_str());
		//}
		}
		while (!socketTerminated);

		closesocket(hClientSocket);
	}

	return 0;
}