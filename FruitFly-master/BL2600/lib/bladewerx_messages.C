/*** BeginHeader do_bladewerx_processing,do_bladewerx_command,verifyBladewerx */
int do_bladewerx_processing(char * tokenBuffer, unsigned long rtc,int msgLength, char * udpBuffer, int *bodyLength);
void do_bladewerx_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage);
void verifyBladewerx (char* ser_cmd, char* log_message);

void toggleBladewerxPower (char on);

char bladewerxRtsSet;
/*** EndHeader */

#memmap xmem
#class auto


//Constant defining a Bladewerx status message
#define BLADEWERX_STATUS_TYPE 0x01
//Constant defining a Bladewerx detection message
#define BLADEWERX_DETECTION_TYPE 0x02

//Constants defining specific Bladewerx commands
#define BLADEWERX_VERSION 0x11
#define BLADEWERX_GET_ADC 0x12
#define BLADEWERX_CALIBRATION_MODE 0x13
#define BLADEWERX_SET_ADC 0x14
#define BLADEWERX_SET_SCALE 0x15
#define BLADEWERX_SET_THRESHOLD 0x16
#define BLADEWERX_SET_GAIN 0x17
#define BLADEWERX_SET_OFFSET 0x18
#define BLADEWERX_POWER_ON 0x19
#define BLADEWERX_POWER_OFF 0x1A
#define BLADEWERX_RAW 0xFF

//RTC conversions
#use "rtc_commands.c"



//Prototypes for message parsing
void parseBladewerxCount(char * currentPosition);
char parseBladewerxVersion (char* currentPosition);
void parseBladewerxADC (char* currentPosition);

//Prototypes for message building
int buildBladewerxStatusMessage(char * udpBuffer, int* bodyLength);
int buildBladewerxDetectionMessage(char * udpBuffer, int* bodyLength);

//Data objects
typedef struct	bladewerx_status
{
	char scale;
   char threshold;
   char gain;
   char offset;
   int flow;
   int battery;
   char inCalib;
   char powerOn;
	char version [MAX_TOKEN_SIZE_C];
} BladewerxStatus;


typedef struct bladewerx_detection
{
   long time;
	char bin;
} BladewerxDetection;


//local states
BladewerxStatus localBladewerxStatus;
BladewerxDetection localBladewerxDetection;
char bladewerxAdcOpt;
char versionChecksum;
char bladewerxPowerToggledOn;


int do_bladewerx_processing(char * tokenBuffer, unsigned long rtc, int msgLength, char * udpBuffer, int *bodyLength)
{
   //Initialize
   char * currentPosition;

   #GLOBAL_INIT
	{
   	//Initialize local states once
	   localBladewerxStatus.scale = 0;
      localBladewerxStatus.threshold = 0;
      localBladewerxStatus.gain = 0;
      localBladewerxStatus.offset = 0;
      localBladewerxStatus.flow = 0;
      localBladewerxStatus.battery = 0;
      localBladewerxStatus.inCalib = 0;
      localBladewerxStatus.powerOn = 0;
      memset (localBladewerxStatus.version, 0, sizeof(localBladewerxStatus.version));

      localBladewerxDetection.bin = 0;
      localBladewerxDetection.time = 0;
      bladewerxAdcOpt = 0;
      bladewerxPowerToggledOn = 0;
	}

   currentPosition = tokenBuffer;
   memset(udpBuffer,0,sizeof(udpBuffer));

   if (bladewerxRtsSet == 1)
   {
   	//We're looking for command responses
      if (msgLength == 21)
      {
         (*c_rtsoff) ();
	      bladewerxRtsSet = 0;

			//Retrieving response to version request
         if (parseBladewerxVersion(currentPosition) == 1)
	      	return buildBladewerxStatusMessage(udpBuffer, bodyLength);
         else
         	return -1;

      }
      else if (msgLength == 2)
      {
       	//Assume we're getting ADC value returned
         parseBladewerxADC(currentPosition);
         (*c_rtsoff) ();
	      bladewerxRtsSet = 0;
      	return buildBladewerxStatusMessage(udpBuffer, bodyLength);
      }
      else
      {
      	//Don't know - err
        	printf ("Unknown or malformed token from Bladewerx: %d: unable to parse.\n\0",tokenBuffer[0]);
	      *bodyLength = 0;
         (*c_rtsoff) ();
	      bladewerxRtsSet = 0;
	      return -1;

      }

      (*c_rtsoff) ();
      bladewerxRtsSet = 0;
   }


   //Only thing we should ever get here is single detection bins
   parseBladewerxCount(currentPosition);
   return buildBladewerxDetectionMessage(udpBuffer, bodyLength);
}

void parseBladewerxCount(char * currentPosition)
{
	localBladewerxDetection.time = get1970RTC();
   localBladewerxDetection.bin = currentPosition[0];
}

char parseBladewerxVersion (char* currentPosition)
{
	int len;
   char checkSum;
   int i;
   checkSum = 0;

   versionChecksum = currentPosition [20];

   len = strlen (currentPosition);
   sprintf (localBladewerxStatus.version, "%s\0", currentPosition);
   /*for (i = 0; i < len; i ++)
   {
   	checkSum += currentPosition[0];
      currentPosition ++;
   }*/

   //ignore \0 byte
   //currentPosition ++;
   currentPosition += len + 1;

   localBladewerxStatus.scale = currentPosition [2];
   localBladewerxStatus.offset = (currentPosition [3] << 3);
   localBladewerxStatus.gain = currentPosition [0];
   localBladewerxStatus.threshold = currentPosition [1];

   checkSum += localBladewerxStatus.scale;
   checkSum += localBladewerxStatus.offset;
   checkSum += localBladewerxStatus.gain;
   checkSum += localBladewerxStatus.threshold;
   checkSum += currentPosition[4]; //unused
   checkSum += currentPosition[5]; //unused
   checkSum += currentPosition[6]; //unused

   if (checkSum == versionChecksum)
   	return 1;
   else
      return 0;
}

void parseBladewerxADC (char* currentPosition)
{
	if (bladewerxAdcOpt == 0)
      localBladewerxStatus.flow = (currentPosition[0]<<8) + (currentPosition[1]);
   if (bladewerxAdcOpt == 1)
      localBladewerxStatus.battery = (currentPosition[0]<<8) + (currentPosition[1]);
}


int buildBladewerxDetectionMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   BladewerxDetection nDetection;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nDetection, &localBladewerxDetection, sizeof(localBladewerxDetection));

   nDetection.time = htonl (nDetection.time);

   //copy in data
   memcpy(bufferPtr,&nDetection,sizeof(nDetection));
   bufferPtr+=(int)sizeof(nDetection);
   index+=(int)sizeof(nDetection);

   *bodyLength = index;
   return BLADEWERX_DETECTION_TYPE;
}

int buildBladewerxStatusMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;
 	char servo1ToggledOpen;
   char lastServo1Duty;
   char servo1ManualOverride;

   BladewerxStatus nStatus;

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

   //Set power status
   localBladewerxStatus.powerOn = bladewerxPowerToggledOn;

   //Copy in body,
	//copy locally
   memcpy(&nStatus, &localBladewerxStatus, sizeof(localBladewerxStatus));

   //set byte order of data
	nStatus.flow = htons(nStatus.flow);
	nStatus.battery = htons(nStatus.battery);

   //copy in data
   memcpy(bufferPtr,&nStatus,sizeof(nStatus));
   bufferPtr+=(int)sizeof(nStatus);
   index+=(int)sizeof(nStatus);


   *bodyLength = index;
	return BLADEWERX_STATUS_TYPE;
}

void getVersion(char * SerialCommandBuffer)
{
   SerialCommandBuffer[0] = 0x20;
   SerialCommandBuffer[1] = 0;
}

void getADC(char * SerialCommandBuffer)
{
   SerialCommandBuffer[0] = 0x21;
   SerialCommandBuffer[1] = 0;
}

void setCalibration(char * SerialCommandBuffer, char opt)
{
   SerialCommandBuffer[0] = 0x23 - opt;
   SerialCommandBuffer[1] = 0;
}

void setADC(char * SerialCommandBuffer, char val)
{
   SerialCommandBuffer[0] = 0x40 + val;
   SerialCommandBuffer[1] = 0;
   bladewerxAdcOpt = val;
}

void setScale(char * SerialCommandBuffer, char val)
{
   SerialCommandBuffer[0] = 0x80 + val;
   SerialCommandBuffer[1] = 0;
}

void setThreshold(char * SerialCommandBuffer, char val)
{
   SerialCommandBuffer[0] = 0xA0 + val;
   SerialCommandBuffer[1] = 0;
}

void setGain(char * SerialCommandBuffer, char val)
{
   SerialCommandBuffer[0] = 0xC0 + val;
   SerialCommandBuffer[1] = 0;
}

void setOffset(char * SerialCommandBuffer, char val)
{
   SerialCommandBuffer[0] = 0xE0 + (val >> 3);
   SerialCommandBuffer[1] = 0;
}

void toggleBladewerxPower (char on)
{
	digHout (bladewerxPowerHC, on);
   bladewerxPowerToggledOn = on;
}

void do_bladewerx_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage)
{
   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case BLADEWERX_VERSION:
       	getVersion (ser_cmd);
         sprintf(LogMessage,"BLADEWERX_VERSION received.\r\n\0");
         break;
      case BLADEWERX_GET_ADC:
        	sprintf(LogMessage,"BLADEWERX_GET_ADC received.\r\n\0");
        	getADC(ser_cmd);
        	break;
      case BLADEWERX_CALIBRATION_MODE:
         sprintf(LogMessage,"BLADEWERX_CALIBRATION_MODE received: %x\r\n\0", incomingUcMessageBody[1]);
        	setCalibration(ser_cmd, incomingUcMessageBody[1]);
         break;
      case BLADEWERX_SET_ADC:
        	sprintf(LogMessage,"BLADEWERX_SET_ADC received: %x\r\n\0", incomingUcMessageBody[1]);
        	setADC(ser_cmd, incomingUcMessageBody[1]);
         break;
      case BLADEWERX_SET_SCALE:
        	sprintf(LogMessage,"BLADEWERX_SET_SCALE received: %x\r\n\0",incomingUcMessageBody[1]);
        	setScale(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case BLADEWERX_SET_THRESHOLD:
        	sprintf(LogMessage,"BLADEWERX_SET_THRESHOLD received: %x\r\n\0",incomingUcMessageBody[1]);
        	setThreshold(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case BLADEWERX_SET_GAIN:
        	sprintf(LogMessage,"BLADEWERX_SET_GAIN received: %x\r\n\0",incomingUcMessageBody[1]);
        	setGain(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case BLADEWERX_SET_OFFSET:
        	sprintf(LogMessage,"BLADEWERX_SET_OFFSET received: %x\r\n\0",incomingUcMessageBody[1]);
        	setOffset(ser_cmd,incomingUcMessageBody[1]);
      	break;
		case BLADEWERX_POWER_ON:
      	sprintf(LogMessage,"BLADEWERX_POWER_ON received. \r\n\0");
         toggleBladewerxPower (1);
         break;
      case BLADEWERX_POWER_OFF:
      	sprintf(LogMessage,"BLADEWERX_POWER_OFF received. \r\n\0");
         toggleBladewerxPower (0);
         break;
      case BLADEWERX_RAW:
      	sprintf(LogMessage,"BLADEWERX_RAW received: %s\r\n\0",incomingUcMessageBody+1);
         //first byte is message subtype
         sprintf(ser_cmd,incomingUcMessageBody+1);
         printf("Writing raw command to serial\n");
	      break;
		default:
        	sprintf(LogMessage,"BLADEWERX_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse BLADEWERX UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
      }
}

void verifyBladewerx (char* ser_cmd, char* log_message)
{
   getVersion (ser_cmd);
   sprintf (log_message, "BLADEWERX_VERSION sent for interval polling\r\n\0");
}