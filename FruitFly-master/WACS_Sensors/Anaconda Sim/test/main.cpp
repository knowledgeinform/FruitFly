#include <windows.h>
#include <string.h>

BOOL  openComPort  (const char* port, const char* baudrate);
void  closeComPort (void);
DWORD sendData     (const char* data, DWORD size);
DWORD receiveData  (char* data, DWORD size);


int formSampleMsg (char data[])
{
	data[0] = '$';
	data[1] = 'i';
	data[2] = 'n';
	data[3] = 'f';
	data[4] = 'o';
	data[5] = ',';
	data[6] = 'b';
	data[7] = 'e';
	data[8] = 'g';
	data[9] = 'i';
	data[10] = 'n';
	data[11] = 'n';
	data[12] = 'i';
	data[13] = 'n';
	data[14] = 'g';
	data[15] = ' ';
	data[16] = 'c';
	data[17] = 'o';
	data[18] = 'l';
	data[19] = 'l';
	data[20] = 'e';
	data[21] = 'c';
	data[22] = 't';
	data[23] = 'i';
	data[24] = 'o';
	data[25] = 'n';
	data[26] = ' ';
	data[27] = 'o';
	data[28] = 'f';
	data[29] = '\r';
	data[30] = '\n';
	data[31] = 0;
	return 32;
}

int formStatusMsg (char data[])
{
	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x80;
	data[3] = 0x20;
	data[4] = 0x00;
	/*data[5] = 0x11;	//Message time of 16846865
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x01;*/
	data[5] = 0x87;	//Message time of 1234567
	data[6] = 0xd6;
	data[7] = 0x12;
	data[8] = 0x00;



	data[9] = 0x01; //lcda status 1
	data[10] = 0x00;
	data[11] = 0x00;
	data[12] = 0x00;
	data[13] = 0x02; //lcdb status 2
	data[14] = 0x00;
	data[15] = 0x00;
	data[16] = 0x00;
	data[17] = 0x01; //lcdb req 1
	data[18] = 0x02; //lcdb act 2
	data[19] = 0x03; //lcda req 3
	data[20] = 0x04; //lcda act 4
	data[21] = 0x00; //manifold act temp 256
	data[22] = 0x01; 
	data[23] = 0x10; //manifold targ temp 16
	data[24] = 0x00;
	data[25] = 0x01; //pitot act temp 4097
	data[26] = 0x10;
	data[27] = 0x02; //pitot targ temp 8194
	data[28] = 0x20;
	data[29] = 0x08; //1.8 amps
	data[30] = 0x07;
	data[31] = 0x4c; //27.5 V
	data[32] = 0x04;
	data[33] = 0x01; //System 1 [debug permitted]
	data[34] = 0x00;
	data[35] = 0x00;
	data[36] = 0x00;
	data[37] = 0xff; //Unchecked for now
	return 38;
}

int formLcdaReport0 (char data[])
{
	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x81;
	data[3] = 0x09;
	data[4] = 0x00;
	data[5] = 0x11;	//Message time of 50401297
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x03;
	data[9] = 0x00; //temperature of 256
	data[10] = 0x01;
	data[11] = 0x01; //pressure of 4097
	data[12] = 0x10;
	data[13] = 0x00; //no agents
	data[14] = 0xff; //Unchecked for now
	return 15;
}

int formLcdaReport3 (char data[])
{
	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x81;
	data[3] = 0x0F;
	data[4] = 0x00;
	data[5] = 0x11;	//Message time of 50401297
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x03;
	data[9] = 0x00; //temperature of 256
	data[10] = 0x01;
	data[11] = 0x01; //pressure of 4097
	data[12] = 0x10;
	data[13] = 0x03; //no agents
	data[14] = 0x70; //agent ID, 112
	data[15] = 0x04; //bars, 4
	data[16] = 0x01; //agent ID, 1
	data[17] = 0x44; //bars, 68
	data[18] = 0x11; //agent ID, 17
	data[19] = 0x00; //bars, 0
	data[20] = 0xff; //Unchecked for now
	return 21;
}

int formLcdbReport3 (char data[])
{
	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x82;
	data[3] = 0x0F;
	data[4] = 0x00;
	data[5] = 0x11;	//Message time of 50401297
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x03;
	data[9] = 0x00; //temperature of 256
	data[10] = 0x01;
	data[11] = 0x01; //pressure of 4097
	data[12] = 0x10;
	data[13] = 0x03; //no agents
	data[14] = 0x70; //agent ID, 112
	data[15] = 0x04; //bars, 4
	data[16] = 0x01; //agent ID, 1
	data[17] = 0x44; //bars, 68
	data[18] = 0x11; //agent ID, 17
	data[19] = 0x00; //bars, 0
	data[20] = 0xff; //Unchecked for now
	return 21;
}

int formText (char data[])
{
	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x83;
	data[3] = 0x06;
	data[4] = 0x00;
	data[5] = 0x11;	//Message time of 50401297
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x03;
	data[9] = 0x41; //temperature of 256
	data[10] = 0x44;
	data[11] = 0xff; //Unchecked for now
	return 12;
}


char Output[] = 
{
    7,     5,     8,     0,     8,     0,     7,     7,     0,     9, 
    0,     9,     8,     8,     8,     8,     9,     8,     9,     8, 
    9,     9,     8,     9,     0,     9,     1,     9,     1,     9, 
    0,     8,     9,     9,     9,     9,     9,     0,     0,     1, 
    2,     0,     1,     1,     9,     9,    10,     9,    11,     9, 
   11,     9,     9,     1,     2,     2,     2,     1,     2,     1, 
    9,     1,     9,     9,     9,     0,     0,     1,     9,     0, 
    9,    10,     0,     1,     1,     1,     3,     1,     1,     9, 
    9,     9,     9,    10,     9,     9,     9,     0,     1,     0, 
    9,     0,     9,     9,     9,     9,     1,     1,     1,     2, 
    0,     1,     0,     9,     1,     0,     0,     1,     1,     1, 
    0,     9,     0,     9,    11,    10,     9,     9,     0,     1, 
    2,     0,     0,     0,     0,     9,     1,     0,     9,     0, 
    0,     9,     1,     2,     1,     1,     2,     1,     3,     1, 
    3,     0,     1,     1,     9,    10,     9,    10,     0,     1, 
    1,     1,     1,     3,     3,     1,     1,     1,     1,     9, 
    1,     1,     1,     1,     1,     3,     2,     1,     2,     1, 
    1,     1,     2,     1,     3,     3,     3,     4,     2,     3, 
    3,     2,     2,     2,     0,     0,     9,     9,     9,    11, 
    9,     9,    10,     1,     1,     2,     1,     4,     2,     3, 
    3,     5,     2,     2,     2,     3,     3,     7,     2,     3, 
    3,     5,     3,     3,     4,     3,     4,     3,     4,     3, 
    5,     4,     4,     5,     5,     5,     5,     5,     5,     4, 
    4,     4,     4,     3,     3,     3,     4,     2,     1,     1, 
    1,     0,     8,     9,    10,    11,    12,    12,    11,    13, 
   11,    12,    11,    11,    11,    11,    11,    10,    11,    10, 
   10,     9,     9,     9,     8,     9,     8,     8,     8,     0, 
    8,     8,     0,     8,     0,     8,     8,     0,     8,     0, 
    8,     0,     0,     8,     0,     0,     8,     0,     0,     8, 
    8,     0,     8,     8,     8,     8,     0,     9,     9,     1, 
    9,     9,     0,     0,     9,     9,     9,     9,     1,     0, 
    1,     2,     3,     2,     3,     1,     2,     9,     9,     9, 
    9,     9,     0,     0,     1,     1,     1,     3,     0,     0, 
    0,     9,     9,     9,     9,     9,     0,     9,     9,     1, 
    0,     1,     9,     1,     0,     9,     9,     0,     0,     9, 
   11,     9,     0,     0,     0,     0,     9,     0,     9,     9, 
    9,     9,     1,     1,     1,     0,     0,     0,     9,     9, 
    0,     9,     1,     9,     1,     1,     0,     1,     1,     1, 
    1,     1,     1,     1,     0,     1,     9,     1,     0,     9, 
    9,     9,    10,     0,     1,     9,     9,     1,     1,     0, 
    0,     0,     0,     9,    10,     9,     9,     0,     2,     0, 
    1,     2,     2,     2,     1,     2,     0,     9,    10,    10, 
   11,    11,    10,    10,     9,     9,     0,     1,     1,     2, 
    9,     1,     9,     0,     9,     0,     0,     9,    10,     0, 
    0,     0,     0,     1,     2,     1,     1,     1,     1,     1, 
    0,     9,     9,     9,     9,     0,     1,     1,     3,     1, 
    3,     1,     0,     9,     9,    11,    10,     9,     9,    10, 
    9,     1,     1,     9,     1,     1,     1,     0,     0,     1, 
    1,     1,     0,     9,     1,     9,     0,     9,     9,     9, 
    0,     9,     1,     0,     9,     9,     9,     0,     0,     0, 
    1,     1,     3,     0,     0,     9,     9,     9,     9,     0, 
    9,     0,     9,     9,     9,     0,     1,     1,     2,     3, 
    1,     1,     1,     1,     9,     9,    10,     9,     9,     0, 
    0,     1,     0,     0,     0,     9,     0,     9,     1,     0, 
    0,     0,     0,     9,     9,     9,     9,     9,     1,     0, 
    0,     0,     1,     1,     1,     9,     0,     9,     1,     0, 
    1,     9,     1,     0,     0,     1,     0,     1,     1,     1, 
    1,     0,     0,     9,     9,     9,    10,     0,     9,     9, 
    1,     1,     1,     1,     0,     9,     9,     9,     9,     0, 
    9,     9,     9,     9,     0,     0,     1,     1,     2,     1, 
    1,     9,     0,     0,     1,     0,     2,     1,     1,     1, 
    0,     0,    10,     9,    10,     9,    10,     9,     9,     9, 
    1,     1,     1,     1,     1,     0,     0,     9,     9,     9, 
    0,     0,     1,     1,     2,     0,     1,     9,     0,     0, 
    0,     1,     1,     0,     0,     0,     9,     9,    10,     9, 
    0,    10,     0,     9,     0,     0,     9,     9,     0,     9, 
    0,     1,     1,     0,     1,     1,     0,     1,     0,     1, 
    0,     0,     0,     1,     1,     9,     0,     9,     9,     9, 
    9,     9,     1,     3,     0,     1,     2,     0,     9,    11, 
    0,     0,     0,     0,     1,     1,     9,     1,     9,     0, 
    0,     1,     1,     9,     9,    10,     0,     9,     9,     0, 
    0,     1,     2,     1,     0,     1,     9,     0,     9,     9, 
    9,     1,     1,     1,     9,     0,     9,     9,     9,    10, 
    0,     1,     1,     1,     2,     1,     3,     9,     1,     9, 
    9,     9,    10,     9,     9,     1,     1,     1,     0,     9, 
    1,     0,     1,     1,     1,     1,     0,     9,     9,     9, 
    0,     9,     9,     1,     0,     0,     1,     1,     1,     0, 
    9,    11,     9,    11,    11,    11,     9,    11,     1,     0, 
    1,     3,     3,     3,     1,     1,     0,     0,     0,     9, 
    9,     0,     0,     1,     9,     0,     1,     0,     9,     1, 
    1,     1,     1,     0,     9,     9,     0,     1,     0,     0, 
    9,     2,     1,     0,     1,     1,     1,     1,     1,     9, 
    9,     9,    10,    10,     9,     9,     9,     9,     9,     1, 
    0,     0,     3,     1,     1,     1,     0,     9,    10,     9, 
    0,     9,     1,     1,     9,     0,     0,     9,     9,     0, 
    1,     1,     1,     0,     1,     1,     9,     0,     0,     0, 
    0,     1,     9,     1,     9,     9,     9,     0,     0,     1, 
    1,     1,     2,     1,     1,     1,     1,     1,     9,     1, 
    0,     9,    10,    10,     9,    10,     9,     1,     9,     1, 
    0,     1,     0,     0,     0,     1,     1,     1,     2,     0, 
    1,     9,     9,    11,     9,    10,     9,     1,     1,     1, 
    1,     2,     1,     1,     0,     9,     9,     9,     9,     0, 
    9,     0,     9,     0,     1,     1,     1,     1,     9,     0, 
    9,     9,    11,    10,     9,     9,    10,     0,     1,     1, 
    3,     3,     3,     2,     2,     2,     2,     9,     9,     9, 
   11,     9,    11,    10,     9,     9,     9,     1,     9,     1, 
    1,     9,     9,     0,     9,     1,     1,     1,     1,     1, 
    1,     0,     9,     1,     1,     2,     0,     0,     1,     1, 
    9,     9,     0,    10,     9,     9,     0,     9,     0,     0, 
    1,     0,     0,     1,     1,     9,     0,     1,     1,     0, 
    1,     0,     0,     9,     9,    10,     9,     9,     9,     9, 
    9,     9,     0,     1,     1,     2,     1,     3,     1,     1, 
    0,     0,     0,     9
};

int formSpectraReport (char data[])
{
	char realOutput[512];
	for (int i = 0; i < sizeof(Output); i +=2)
	{
		realOutput [i/2] = ((Output[i]&0x0F)<<4) | (Output[i+1]&0x0F);
	}

	data[0] = 0x7e;
	data[1] = 0x7e;
	data[2] = 0x84;
	data[3] = 0x04;
	data[4] = 0x02;
	data[5] = 0x11;	//Message time of 50401297
	data[6] = 0x10;
	data[7] = 0x01;
	data[8] = 0x03;
	
	/*for (int i = 9; i < 521; i ++)
	{
		data[i] = rand ()%256; //nibbles
	}*/
	memcpy (&data[9], realOutput, sizeof(realOutput));
	
	data[521] = 0xff; //Unchecked for now
	return 522;
}

void addChecksum (char data[], int idx)
{
	char checkSum = 0;

	for (int i = 2; i < idx; i ++)
	{
		checkSum += data[i];
	}
	data[idx] = checkSum;
}


int main ()
{
	//openComPort ("COM1", "115200");
	openComPort ("COM15", "115200");
	//openComPort ("COM1", "57600");

	char data [600];
	
	while (true)
	{
		//int bytes = formSampleMsg (data);
		int bytes = formStatusMsg (data);
		addChecksum (data, bytes-1);
		int sent = sendData (data, bytes);
		bytes = formLcdaReport3 (data);
		addChecksum (data, bytes-1);
		sent = sendData (data, bytes);
		//int bytes = formLcdbReport3 (data);
		//int bytes = formText (data);
		
		bytes = formSpectraReport (data);
		addChecksum (data, bytes-1);
		sent = sendData (data, bytes);
		Sleep (10);
		//break;
	}

	closeComPort ();
	
	return 0;
}










static HANDLE _hCom;


BOOL openComPort (const char* port, const char* baudrate)
{
  char buildStr[50];
  DCB dcb;       
  COMMTIMEOUTS timeouts = {0};
             
            
  _hCom = CreateFile(port,
                     GENERIC_READ | GENERIC_WRITE,
                     0,
                     0,
                     OPEN_EXISTING,
                     0,
                     0);

  if(_hCom == INVALID_HANDLE_VALUE)   
  {
    _hCom = NULL;
	int err = GetLastError();
    return FALSE;
  }

  /* set timeouts */
  timeouts.ReadTotalTimeoutConstant    = 100;
  timeouts.ReadTotalTimeoutMultiplier  = 0;
  timeouts.WriteTotalTimeoutMultiplier = 0;
  timeouts.WriteTotalTimeoutConstant   = 0;
  if(SetCommTimeouts(_hCom, &timeouts) == FALSE)
    return FALSE;

  dcb.DCBlength = sizeof(DCB);
  if(GetCommState(_hCom, &dcb) == FALSE)
    return FALSE;

  /* Simplified way of setting up the COM port using strings: */
  buildStr[0] = '\0';
  strcat(buildStr, "baud=");
  strcat(buildStr, baudrate);
  strcat(buildStr," parity=N data=8 stop=1");

  /* (A more effective way is to setup the members of the DCB struct manually, 
     then you don't need BuildCommDCB) */

  BuildCommDCB(buildStr, &dcb);
  return SetCommState(_hCom, &dcb);
}  


void closeComPort(void)
{
  CloseHandle(_hCom);
}


DWORD sendData (const char* data, DWORD size)
{
  DWORD numberOfBytesWritten;
  
  WriteFile(_hCom,
            data,
            size,
            &numberOfBytesWritten,
            0);

  return numberOfBytesWritten;
}


DWORD receiveData (char* data, DWORD size)
{
  DWORD numberOfBytesRead;
  
  ReadFile(_hCom,
           data,
           size,
           &numberOfBytesRead,
           0);

  return numberOfBytesRead;
}
