/*** BeginHeader  */
#define SER_DMA_DISABLE

// serial buffer size
#define EINBUFSIZE  1023
#define EOUTBUFSIZE 255

// serial baud rate

#define MODEMBAUD 19200

#define SERE_TXPORT PEDR
#define SERE_RXPORT PEDR

// Used for the configuration structure
typedef struct
{
   int valid;
   char iridium;
   char dialnumber[32];
   char master;
   char encryption;
   char password[32];
   char newpassword[32];
   char keyphrase[32];
   char encryptionkey[65];
   char key[32];
   char updatekey;
} ModemParams;

enum
{
   MODEM_INIT,
   MODEM_WAIT,
   MODEM_CONNECT,
   MODEM_DELAY,
	MODEM_RUN,
	MODEM_RESET
};

extern ModemParams modem_params;
extern int modem_state;
extern uint16 latency;
extern int modem_status;
extern uint32 modem_connect_time;
extern uint32 modem_disconnect_time;

void update_modem_config_webvar();
int checkforstring(char* msg);
int waitforstring(char* buff, int buffsize, char* strtofind, uint32 timeoutmS);
int getSQ(char *str);
/*** EndHeader */

#use "WebVariables.c"
uint16 latency;
int modem_state;
ModemParams modem_params;
int modem_status;
uint32 modem_connect_time;
uint32 modem_disconnect_time;

char modemstatestring[32];
char modemstatusstring[100];


char* getModemStatus()
{
	if(Read_Modem_Carrier_Detect())
   {
   	strcpy(&modemstatusstring[0], "<B>MODEM STATUS:<span style=\"background-color: #FF0000\">SAT LINK DOWN</SPAN></B>\n");

   }
   else
   {
      strcpy(&modemstatusstring[0], "<B>MODEM STATUS: <span style=\"background-color: #00FF00\">SAT LINK UP</SPAN></B>\n");
   }
   return  &modemstatusstring[0];
}




char* getModemState()
{
	switch(modem_state)
   {
   	case  MODEM_INIT:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: INITIALIZING</B>");
         break;
      case  MODEM_WAIT:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: WAITING</B>");
         break;
      case  MODEM_CONNECT:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: CONNECTING</B>");
         break;
      case  MODEM_DELAY:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: DELAY</B>");
         break;
      case  MODEM_RUN:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: RUNNING</B>");
         break;
       case  MODEM_RESET:
      	strcpy(&modemstatestring[0], "<B>MODEM STATE: RESET</B>");
         break;

   }
   return  &modemstatestring[0];
}




int waitforstring(char* buff, int buffsize, char* strtofind, uint32 timeoutmS)
{
   uint32 t1;
   uint32 t2;
   uint32 time;
   int result=0;
   int bcount = 0;
   int usesec = 0;
   int ssize = strlen(strtofind);

   if(timeoutmS > 5000)
   {
   	t2 = SEC_TIMER;
      t1 = SEC_TIMER;
      timeoutmS = timeoutmS/1000;
      usesec = 1;
   }
   else
   {
   	t1 = MS_TIMER;
      t2 = MS_TIMER;
   }

   while(t2-t1 < timeoutmS && bcount < (buffsize-1))
   {
   	OSTimeDlyHMSM(0,0,0,10);
      if(usesec)
	   {
	      t2 = SEC_TIMER;
	   }
	   else
	   {
	      t2 = MS_TIMER;
	   }
		result = serEgetc();
      printf("%c",result);

      //printf("%x",result);

      if(result != -1)
      {
      	if(result==0)
         {
           buff[bcount] = 0;
         }
         else
         {
	         buff[bcount++] = result;
	         if(bcount >= ssize)
	         {
	            buff[bcount] = 0;
	            if(strstr(&buff[bcount-ssize],strtofind)!=0)
	            {
	               return 1;
	            }

	         }
         }
      }
   }
   buff[bcount] = 0;
   if(t2-t1 >= timeoutmS)
   	return -1; //timeout
   else
   	return -2;//buffer full
}





int checkforstring(char* msg)
{
	static char tempbuf[64];
   int result;
	result = serEread(tempbuf, 64, 200);
   tempbuf[result] = 0;
   printf("%d Bytes Read:\n %s\n",result,tempbuf);
	if(result >=strlen(msg) && strstr(tempbuf,msg)!=0)
   {
   		//null terminate buffer
   		tempbuf[result] = 0;
      	return 1;
   }
   return 0;
}


int getSQ(char *str)
{
	char *ptr;
	int val;
	const char* delim = ":,";

    ptr = strtok(str, delim); // +CSQF
    ptr = strtok(NULL, delim);
    if (ptr == NULL || strlen(ptr) == 0)
    {
       val = -1;
    }
    else
    {
    	val = atoi(ptr);
    }

	 printf("Modem: Signal Strength: %d\r\n", val);

    return val;

}


int checkforstrings(char** msgs, int num)
{
	static char tempbuf[64];
   int i,result;
	result = serEread(tempbuf, 64, 200);
   tempbuf[result] = 0;
   printf("%d Bytes Read:\n %s\n",result,tempbuf);
   for(i=0; i<num; i++)
   {
	   if(result >=strlen(msgs[i]) && strstr(tempbuf,msgs[i])!=0)
	   {
	         return i+1;
	   }
   }
   return 0;
}





void update_modem_config_webvar()
{
   if(modem_params.master)
   {
   	sprintf(ismaster,"Yes");
      sprintf(ismasterchecked,"checked");
   }
   else
   {
   	sprintf(ismaster,"No");
      sprintf(ismasterchecked,"");
   }
   if(modem_params.iridium)
   {
   	sprintf(sattype,"Iridium");
   }
   else
   {
   	sprintf(sattype,"Globalstar");
   }
   if(modem_params.encryption)
   {
		sprintf(useencryption,"Yes");
      sprintf(useencryptionchecked,"checked");
   }
   else
   {
		sprintf(useencryption,"No");
      sprintf(useencryptionchecked,"");
   }

   if(modem_status)
   {
   	sprintf(modemstatus,"Enabled");
   }
   else
   {
      sprintf(modemstatus,"Paused");
   }

   strcpy(dialnumber,modem_params.dialnumber);
   strcpy(password,modem_params.password);


}





