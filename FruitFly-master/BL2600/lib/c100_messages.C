/*** BeginHeader do_c100_processing,do_c100_command,init_c100_states */
int do_c100_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength);
int do_c100_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage, char* responseBuffer, int* responseLen);

void verifyC100 (char* ser_cmd, char* log_message);

/*** EndHeader */


#memmap xmem
#class auto


//Constant defining an C100 status message
#define C100_STATUS_TYPE 0x01
//Constant defining an C100 action message
#define C100_ACTION_TYPE 0x02
//Constant defining a C100 flow status message
#define C100_FLOWSTATUS_TYPE 0x03
//Constant defining a C100 warning message
#define C100_WARNING_TYPE 0x04


//Constants defining specific C100 commands
#define C100_PRIME 0x11
#define C100_COLLECT_AND_PURGE 0x12
#define C100_COLLECT_ON 0x13
#define C100_COLLECT_OFF 0x14
#define C100_SAMPLE 0x15
#define C100_CLEAN 0x16
#define C100_STATUS 0x17
#define C100_CONFIG 0x18
#define C100_RESET_VIALS 0x19
#define C100_FLOWSTATUS 0x1A
#define C100_RAW 0xFF

#define C100_CONFIG_TIMEP2 0x31
#define C100_CONFIG_EXTRAP2 0x32
#define C100_CONFIG_PRIMING1 0x33
#define C100_CONFIG_PRIMINGD 0x34
#define C100_CONFIG_PRIMING2 0x35
#define C100_CONFIG_CL 0x36
#define C100_CONFIG_S 0x37
#define C100_CONFIG_RS 0x38
#define C100_CONFIG_VALVES 0x39

//RTC conversions
#use "rtc_commands.c"

//Store sample used settings in flash
#use "flash_handler.c"


//Prototypes for message parsing
void parseC100Info(char * currentPosition, unsigned long rtc);
void parseC100S(char * currentPosition, unsigned long rtc);
void parseC100FlowStatus(char * currentPosition, unsigned long rtc);
int parseC100Warning(char * currentPosition, unsigned long rtc, char * udpBuffer, int *bodyLength);
char * getNextC100Token(char * buffer);

//Prototypes for message building
int buildC100StatusMessage(char * udpBuffer, int* bodyLength);
int buildC100ActionMessage(char * udpBuffer, int* bodyLength);
int buildC100FlowStatusMessage(char * udpBuffer, int* bodyLength);
int buildC100WarningMessage(char * udpBuffer, int* bodyLength);

//Data objects
typedef struct	c100_status
{
	char StatusAndVersion [MAX_TOKEN_SIZE_C];
} C100Status;

typedef struct	c100_flowstatus
{
	char Status [MAX_TOKEN_SIZE_C];
} C100FlowStatus;

typedef struct	c100_warning
{
	char Warning [MAX_TOKEN_SIZE_C];
} C100Warning;

typedef struct c100_action
{
	char cleaningSampleLine;
   char generatingWetSample;
   char cleaningCollector;
   char dryCollectorStatus;
   char priming;
   char activeSample;
	char fluidError;
} C100Action;


//local states
C100Status localC100status;
C100Action localC100action;
C100FlowStatus localC100flowstatus;
C100Warning localC100warning;
char lastSampleCmd;
char lastRawCmdWasReset;
char everSentActionMessage;


int do_c100_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength)
{
   //Initialize
   char * currentPosition;
   int retVal;

   #GLOBAL_INIT
	{
   	//Initialize local states once
	   localC100action.cleaningSampleLine = 0;
	   localC100action.generatingWetSample = 0;
	   localC100action.cleaningCollector = 0;
	   localC100action.dryCollectorStatus = 0;
	   localC100action.priming = 0;
      localC100action.activeSample = 0;
      localC100action.fluidError = 0;
      lastSampleCmd = 0;
      lastRawCmdWasReset = 0;
      everSentActionMessage = 0;

	   memset (localC100status.StatusAndVersion, 0, sizeof(localC100status.StatusAndVersion));
	}

   currentPosition = tokenBuffer;
   memset(udpBuffer,0,sizeof(udpBuffer));

	//parse the first token, then parse the whole message and either create a message depending on the token
   if(strstr(currentPosition,"$info,")==currentPosition)
   {
	   //parse info
      parseC100Info(currentPosition,rtc);
      return buildC100ActionMessage(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$s,")==currentPosition)
   {
	   //parse s
      parseC100S(currentPosition,rtc);
      if (everSentActionMessage == 0)
      {
         everSentActionMessage = 1;
	      return buildC100ActionMessage(udpBuffer, bodyLength);
      }
      else
      {
      	return buildC100StatusMessage(udpBuffer, bodyLength);
      }
   }
   else if(strstr(currentPosition,"s=")==currentPosition)
   {
	   //parse flow status
      parseC100FlowStatus(currentPosition,rtc);
      return buildC100FlowStatusMessage(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$warning")==currentPosition)
   {
	   //parse warning
      retVal = parseC100Warning(currentPosition,rtc,udpBuffer,bodyLength);
      if (retVal != 0)
      	return retVal;
      return buildC100WarningMessage(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$prime")==currentPosition)
   {
   	//Command echo, ignore it
   }
   else if(strstr(currentPosition,"$collect")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$sample")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$clean")==currentPosition)
   {
   	//Command echo, ignore it
      //Normally we ignore this, but this is apparently the only notice that we get (outside of the status message)
      //that cleaning has started.  What we used to read no longer exists.
      localC100action.cleaningCollector = 1;
      localC100action.dryCollectorStatus = 0;
   }
	else if(strstr(currentPosition,"$status")==currentPosition)
   {
   	//Command echo, ignore it
      //printf ("Got status echo, so we'll ignore it.");
   }
 	else if(strstr(currentPosition,"$invalid")==currentPosition)
   {
   	//Invalid command received by C100
     	printf ("Invalid command sent to C100: %s: unable to process.\n\0",tokenBuffer);
      *bodyLength = 0;
      return -1;
   }
	else
   {
   	printf ("Unknown or malformed token from C100: %s: unable to parse.\n\0",tokenBuffer);
      *bodyLength = 0;
      return -1;
   }

   //Default action for command echos
   *bodyLength = 0;
   return -1;

}

char * getNextC100Token(char * buffer)
{
	//get next token
	if((buffer = strchr(buffer,','))==NULL)
		return NULL;
   buffer++; //skip ','
   //read out any leading white space
   while(*buffer!=0&&isspace(*buffer))
   	buffer++;
   //check if we reached end of string
   if(buffer==0)
   	return NULL;
   //otherwise, return updated buffer pointer
   return buffer;
}

void parseC100Info(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextC100Token(currentPosition))==NULL)
		return;

   if(strstr(currentPosition,"starting priming")==currentPosition)
	{
   	localC100action.priming = 1;
      localC100action.fluidError = 0;
      localC100action.dryCollectorStatus = 0;
	}
   else if(strstr(currentPosition,"priming complete")==currentPosition)
	{
   	localC100action.priming = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
      localC100action.fluidError = 0;
	}
   else if(strstr(currentPosition,"prime complete")==currentPosition)
	{
   	localC100action.priming = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
      localC100action.fluidError = 0;
	}
   else if(strstr(currentPosition,"collector on")==currentPosition)
	{
   	localC100action.dryCollectorStatus = 1;

		//Determine if we are purging line
      if((currentPosition = getNextC100Token(currentPosition))==NULL)
			return;

      if(strstr(currentPosition,"purging sample line")==currentPosition)
		{
   		localC100action.cleaningSampleLine = 1;
         localC100action.dryCollectorStatus = 0;
		}
	}
   else if(strstr(currentPosition,"collector turned on")==currentPosition)
	{
   	localC100action.dryCollectorStatus = 1;

  		//Determine if we are purging line
      if((currentPosition = getNextC100Token(currentPosition))==NULL)
			return;

      if(strstr(currentPosition,"purging sample line")==currentPosition)
		{
   		localC100action.cleaningSampleLine = 1;
         localC100action.dryCollectorStatus = 0;
		}
	}
   else if(strstr(currentPosition,"collector turned off")==currentPosition)
	{
   	localC100action.dryCollectorStatus = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
	}
   else if(strstr(currentPosition,"collector off")==currentPosition)
	{
   	localC100action.dryCollectorStatus = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
	}
   else if(strstr(currentPosition,"purging sample line")==currentPosition)
	{
   	localC100action.cleaningSampleLine = 1;
      localC100action.dryCollectorStatus = 0;
	}
 	else if(strstr(currentPosition,"sample line cleared")==currentPosition)
	{
   	localC100action.cleaningSampleLine = 0;
	}
   else if(strstr(currentPosition,"beginning collection of")==currentPosition)
	{
   	localC100action.generatingWetSample = 1;
      localC100action.fluidError = 0;
      localC100action.dryCollectorStatus = 0;

      localC100action.activeSample = lastSampleCmd;
      setC100SampleUsed (localC100action.activeSample-1, 1);
	}
   else if(strstr(currentPosition,"sample complete")==currentPosition)
	{
   	localC100action.generatingWetSample = 0;
      localC100action.fluidError = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
	}
   else if(strstr(currentPosition,"beginning clean")==currentPosition)
	{
   	localC100action.cleaningCollector = 1;
      localC100action.fluidError = 0;
      localC100action.dryCollectorStatus = 0;
	}
   else if(strstr(currentPosition,"clean complete")==currentPosition)
	{
   	localC100action.cleaningCollector = 0;
      setPWMClosed(1);
      servoManualOverride[1] = 0;
      localC100action.fluidError = 0;
	}
	else if(strstr(currentPosition,"has detected fluid")!=NULL)
   {
    	localC100action.fluidError = 0;
   }
	else
	{
		printf("Unhandled $info string: %s\n",currentPosition);
	}
}

void parseC100S(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextC100Token(currentPosition))==NULL)
		return;

   //Copy line of version/status, don't bother parsing
   sprintf (localC100status.StatusAndVersion, "%s\0", currentPosition);
}

void parseC100FlowStatus(char * currentPosition, unsigned long rtc)
{
   //Copy whole line of don't bother parsing
   sprintf (localC100flowstatus.Status, "%s\0", currentPosition);
}

int parseC100Warning(char * currentPosition, unsigned long rtc, char * udpBuffer, int *bodyLength)
{
   //skip type
	if((currentPosition = getNextC100Token(currentPosition))==NULL)
		return 0;

  	if(strstr(currentPosition,"has not detected fluid")!=NULL)
   {
    	localC100action.fluidError = 1;

      return buildC100ActionMessage(udpBuffer, bodyLength);
   }

   //Copy line of warning
   sprintf (localC100warning.Warning, "%s\0", currentPosition);
   return 0;
}

int buildC100ActionMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;
   char i;
   char usedVal;

   C100Action nAction;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nAction, &localC100action, sizeof(localC100action));

   //copy in data
   memcpy(bufferPtr,&nAction,sizeof(nAction));
   bufferPtr+=(int)sizeof(nAction);
   index+=(int)sizeof(nAction);

   //copy in used stats
	for (i = 0; i < 4; i ++)
   {
		usedVal = getC100SampleUsed (i);
	   memcpy(bufferPtr,&usedVal,sizeof(usedVal));
	   bufferPtr+=(int)sizeof(usedVal);
	   index+=(int)sizeof(usedVal);
   }

   *bodyLength = index;
   return C100_ACTION_TYPE;
}

int buildC100StatusMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;
 	char servo1ToggledOpen;
   char lastServo1Duty;
   char servo1ManualOverride;

   C100Status nStatus;

   index=0;
   bufferPtr = udpBuffer;
  	servo1ToggledOpen = servoToggledOpen[1];
   lastServo1Duty = getLastServoDuty(1);
   servo1ManualOverride = servoManualOverride[1];

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in last servo1 state
   memcpy(bufferPtr,&servo1ToggledOpen,sizeof(servo1ToggledOpen));
   bufferPtr+=(int)sizeof(servo1ToggledOpen);
   index+=(int)sizeof(servo1ToggledOpen);

   //Copy in last servo1 PWM
   memcpy(bufferPtr,&lastServo1Duty, sizeof(lastServo1Duty));
   bufferPtr+=(int)sizeof(lastServo1Duty);
   index+=(int)sizeof(lastServo1Duty);

   //Copy in last override state
   memcpy(bufferPtr,&servo1ManualOverride,sizeof(servo1ManualOverride));
   bufferPtr+=(int)sizeof(servo1ManualOverride);
   index+=(int)sizeof(servo1ManualOverride);


   //Copy in body,
	//copy locally
   memcpy(&nStatus, &localC100status, sizeof(localC100status));

   //copy in data
   memcpy(bufferPtr,&nStatus,sizeof(nStatus));
   bufferPtr+=(int)sizeof(nStatus);
   index+=(int)sizeof(nStatus);


   *bodyLength = index;
	return C100_STATUS_TYPE;
}

int buildC100FlowStatusMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   C100FlowStatus nStatus;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nStatus, &localC100flowstatus, sizeof(localC100flowstatus));

   //copy in data
   memcpy(bufferPtr,&nStatus,sizeof(nStatus));
   bufferPtr+=(int)sizeof(nStatus);
   index+=(int)sizeof(nStatus);

   *bodyLength = index;
	return C100_FLOWSTATUS_TYPE;
}

int buildC100WarningMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   C100Warning nWarning;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nWarning, &localC100warning, sizeof(localC100warning));

   //copy in data
   memcpy(bufferPtr,&nWarning,sizeof(nWarning));
   bufferPtr+=(int)sizeof(nWarning);
   index+=(int)sizeof(nWarning);

   *bodyLength = index;
	return C100_WARNING_TYPE;
}

void prime(char * SerialCommandBuffer, char sec)
{
   sprintf(SerialCommandBuffer,"$prime,%d\r\0",sec);
   setPWMOpen(1);
   servoManualOverride[1] = 0;
}
void collectAndPurge(char * SerialCommandBuffer)
{
   sprintf(SerialCommandBuffer,"$collect\r\0");
   setPWMOpen(1);
   servoManualOverride[1] = 0;
}
void collectOn(char * SerialCommandBuffer)
{
	sprintf(SerialCommandBuffer,"$collect,1\r\0");
   setPWMOpen(1);
   servoManualOverride[1] = 0;
}
void collectOff(char * SerialCommandBuffer)
{
   sprintf(SerialCommandBuffer,"$collect,0\r\0");
   // Collect off handled in the parser
}
void sample(char * SerialCommandBuffer, char y)
{
	char sampleUsed;

   sampleUsed = getC100SampleUsed (y-1);
   if (sampleUsed == 0)
   {
   	sprintf(SerialCommandBuffer,"$sample %d\r\0",y);
	   setPWMOpen(1);
	   servoManualOverride[1] = 0;
	   lastSampleCmd = y;
   }
}
void clean(char * SerialCommandBuffer)
{
	sprintf(SerialCommandBuffer,"$clean\r\0");
   setPWMOpen(1);
   servoManualOverride[1] = 0;
}
void statusC100(char * SerialCommandBuffer)
{
	sprintf(SerialCommandBuffer,"$status\r\0");
}
void flowStatus(char * SerialCommandBuffer, char show)
{
	if (show == 0)
	   sprintf(SerialCommandBuffer,"$z,0\r\0");
   else
	   sprintf(SerialCommandBuffer,"$z,1\r\0");
}

void c100Config (char* SerialCommandBuffer, char* LogMessage, char debugOpt, long param)
{
	int valveNum;
   int valveOpen;
   char valveChar;

	if (debugOpt == C100_CONFIG_TIMEP2)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$timep2,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_EXTRAP2)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$extrap2,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_PRIMING1)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$priming1,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_PRIMINGD)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$primingd,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_PRIMING2)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$priming2,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_CL)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$cl,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_S)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$s,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_RS)
   {
    	sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$rs,%ld\r\0", param);
   }
	else if (debugOpt == C100_CONFIG_VALVES)
   {
   	valveNum = (int)(param >> 16);
      if (valveNum > 5)
         valveNum = 5;
		else if (valveNum < 1)
      	valveNum = 1;

      valveOpen = (int)param;
      if (valveOpen != 0)
      	valveChar = 'n';
   	else
      	valveChar = 'f';

      sprintf(LogMessage,"C100_CONFIG received: %x,%ld\r\n\0",debugOpt, param);
		sprintf (SerialCommandBuffer, "$v%c,%d\r\0", valveChar, valveNum);
   }
   else
   {
      sprintf(LogMessage,"C100_CONFIG received, unknown option: %x\r\n\0",debugOpt);
      sprintf(SerialCommandBuffer, "\0");
   }

}

int do_c100_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage, char* responseBuffer, int* responseLen)
{
	char debugOpt;
   long param;

   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case C100_PRIME:
       	prime(ser_cmd,incomingUcMessageBody[1]);
         sprintf(LogMessage,"C100_PRIME received: %x\r\n\0",incomingUcMessageBody[1]);
         break;
      case C100_COLLECT_AND_PURGE:
        	sprintf(LogMessage,"C100_COLLECT_AND_PURGE received.\r\n\0");
        	collectAndPurge(ser_cmd);
        	break;
      case C100_COLLECT_ON:
         sprintf(LogMessage,"C100_COLLECT_ON received.\r\n\0");
        	collectOn(ser_cmd);
         break;
      case C100_COLLECT_OFF:
        	sprintf(LogMessage,"C100_COLLECT_OFF received.\r\n\0");
        	collectOff(ser_cmd);
         break;
      case C100_SAMPLE:
        	sprintf(LogMessage,"C100_SAMPLE received: %d\r\n\0",incomingUcMessageBody[1]);
        	sample(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case C100_CLEAN:
        	sprintf(LogMessage,"C100_CLEAN received.\r\n\0");
        	clean(ser_cmd);
         break;
      case C100_STATUS:
        	sprintf(LogMessage,"C100_STATUS received.\r\n\0");
        	statusC100(ser_cmd);
         break;
      case C100_CONFIG:
      	debugOpt = incomingUcMessageBody[1];
         param = convertBytesToLong (&incomingUcMessageBody[2], 1);
         c100Config (ser_cmd, LogMessage, debugOpt, param);
         break;
      case C100_RESET_VIALS:
        	sprintf(LogMessage,"C100_RESET_VIALS received.\r\n\0");
         setC100SampleUsed ((char)-1, (char)0);

         return buildC100ActionMessage (responseBuffer, responseLen);
         break;

      case C100_FLOWSTATUS:
        	sprintf(LogMessage,"C100_FLOWSTATUS received: %d\r\n\0",incomingUcMessageBody[1]);
        	flowStatus(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case C100_RAW:
      	sprintf(LogMessage,"C100_RAW received: %s\r\n\0",incomingUcMessageBody+1);
         //first byte is message subtype
         sprintf(ser_cmd,incomingUcMessageBody+1);
         printf("Writing raw command to serial\n");

         if (lastRawCmdWasReset == 1 && strcmp (ser_cmd, "$restart\r") == 0)
         {
          	//this is probably the second call of a reset and restart call.  This hack will
            //clear the local action state and return new action message
            localC100action.cleaningSampleLine = 0;
	         localC100action.generatingWetSample = 0;
	         localC100action.cleaningCollector = 0;
	         localC100action.dryCollectorStatus = 0;
	         localC100action.priming = 0;
            localC100action.fluidError = 0;
            lastRawCmdWasReset = 0;
            return buildC100ActionMessage (responseBuffer, responseLen);
         }
  			if (strcmp (ser_cmd, "xc:$reset\r") == 0)
         {
         	lastRawCmdWasReset = 1;
         }
         else
         {
          	lastRawCmdWasReset = 0;
         }


	      break;
		default:
        	sprintf(LogMessage,"C100_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse C100 UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
      }

   return -1;
}

void verifyC100 (char* ser_cmd, char* log_message)
{
	statusC100 (ser_cmd);
   sprintf (log_message, "C100_STATUS sent for interval polling\r\n\0");
}