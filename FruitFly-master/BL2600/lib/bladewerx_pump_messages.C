/*** BeginHeader do_bladewerx_pump_command */
char do_bladewerx_pump_command (char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen);

/*** EndHeader */

#memmap xmem
#class auto

#use "dio_pins.c"


//Constant defining a Bladewerx status message
#define BLADEWERX_PUMP_STATUS_TYPE 0x01

//Constants defining specific Bladewerx commands
#define BLADEWERX_PUMP_CONTROL 0x11

//RTC conversions
#use "rtc_commands.c"



//Prototypes for message building
int buildBladewerxPumpStatusMessage(char * udpBuffer, int* bodyLength);
void togglePumpPower(char * SerialCommandBuffer, char val);

//Data objects
typedef struct	bladewerx_pump_status
{
   char onStatus;
} BladewerxPumpStatus;


//local states
BladewerxPumpStatus localBladewerxPumpStatus;

int buildBladewerxPumpStatusMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   BladewerxPumpStatus nStatus;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nStatus, &localBladewerxPumpStatus, sizeof(localBladewerxPumpStatus));

   //set byte order of data

   //copy in data
   memcpy(bufferPtr,&nStatus,sizeof(nStatus));
   bufferPtr+=(int)sizeof(nStatus);
   index+=(int)sizeof(nStatus);


   *bodyLength = index;
	return BLADEWERX_PUMP_STATUS_TYPE;
}

void togglePower(char val)
{
	if (val == 0)
   {
   	setPWMClosed (1);
      servoManualOverride[1] = 0;
      servoToggledOpen[1] = 0;
   	digHout (pumpOutputHC, 0);
      localBladewerxPumpStatus.onStatus = 0;
   }
   else
   {
   	setPWMOpen (1);
      servoManualOverride[1] = 0;
      servoToggledOpen[1] = 1;
		digHout (pumpOutputHC, 1);
      localBladewerxPumpStatus.onStatus = 1;
   }
}

char do_bladewerx_pump_command (char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen)
{
   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case BLADEWERX_PUMP_CONTROL:
        	sprintf(LogMessage,"BLADEWERX_PUMP_CONTROL received: %x\r\n\0",incomingUcMessageBody[1]);
        	togglePower(incomingUcMessageBody[1]);
      	break;
		default:
        	sprintf(LogMessage,"BLADEWERX_PUMP_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse BLADEWERX_PUMP UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
   }

   return buildBladewerxPumpStatusMessage (responseBuffer, responseLen);
}