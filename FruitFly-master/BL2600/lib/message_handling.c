/*** BeginHeader parse_and_build_c100,parse_and_build_anaconda,interpret_c100_command,interpret_anaconda_command */
scofunc int parse_and_build_c100(char * tokenBuffer, unsigned long rtc,char * udpBuffer);
scofunc int parse_and_build_anaconda(char * tokenBuffer, unsigned long rtc,char * udpBuffer);
scofunc void interpret_c100_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage, char* responseBuffer, int* responseLen);
scofunc int interpret_anaconda_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage);
/*** EndHeader */

/*** BeginHeader parse_and_build_ibac,parse_and_build_bladewerx,interpret_ibac_command,interpret_bladewerx_command,interpret_bladewerx_pump_command,save_bridgeport_message */
scofunc int parse_and_build_ibac(char * tokenBuffer, unsigned long rtc,char * udpBuffer);
scofunc int parse_and_build_bladewerx(char * tokenBuffer, unsigned long rtc, int msgLength, char * udpBuffer);
scofunc void interpret_ibac_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage);
scofunc void interpret_bladewerx_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage);
scofunc void interpret_bladewerx_pump_command(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char* LogMessage);
scofunc void save_bridgeport_message(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char* LogMessage);
/*** EndHeader */

/*** BeginHeader verifyCommand,build_heartbeat,interpret_pod_command,interpret_servo_command,interpret_fan_command,interpret_heater_command */
char verifyCommand (char* buffer);

//Type of sensor that is sending a message back to the PC
int build_heartbeat(char * udpBuffer, char biopodType, char lastLogCommandOn, char actualLogStateOn, unsigned long lastSerialReceiveC, unsigned long lastSerialReceiveF,
		char voltageEighthsLocal, unsigned long voltageUpdatedLocal,
      float temperature, unsigned long temperatureUpdated, float humidity, unsigned long humidityUpdated,
      char servo0ToggledOpen, char lastServo0Duty, char fanToggledOn, char heaterToggledOn,
      char servo0ManualOverride, char fanManualOverride, char heaterManualOverride,
      char temperatureLimitServo, char humidityLimitServo, char temperatureLimitFan,
      char humidityLimitFan, char temperatureLimitHeater, char humidityLimitHeater,
      int logErrCode);
scofunc void interpret_pod_command (char* incomingUdpbuffer, int bytesReceived, char* LogMessage);
scofunc void interpret_servo_command(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char biopodType, char* LogMessage);
scofunc void interpret_fan_command(char* incomingUdpbuffer, int bytesReceived, char* LogMessage);
#if BIOPOD_TYPE == 0
scofunc void interpret_heater_command(char* incomingUdpbuffer, int bytesReceived, char* LogMessage);
#endif

#define RABBIT_BOARD 0x00
#define SENSOR_IBAC 0x01
#define SENSOR_BRIDGEPORT 0x02
#define SENSOR_BLADEWERX 0x03
#define SENSOR_BLADEWERX_PUMP 0x04
#define SENSOR_C100 0x05
#define SENSOR_ANACONDA 0x06
#define SENSOR_TEMPERATURE 0x07
#define SENSOR_FAN 0x08
#define SENSOR_HEATER 0x09
#define SENSOR_SERVO 0x0A

/*** EndHeader */

#memmap xmem
#class auto

//Index in the header storing length of the message
#define LENGTH_INDEX 10
//Index in the message header storing message type
#define MSG_TYPE_INDEX 12

//Index in the message where body of data begins
#define MESSAGE_BODY_INDEX 13
//Index in the command where body of data begins
#define COMMAND_BODY_INDEX 12

const int MessageHeaderSize = MESSAGE_BODY_INDEX;
const int CommandHeaderSize = COMMAND_BODY_INDEX;
const int ChecksumSize = 1;



//Constant defining a heartbeat message
#define HEARTBEAT 0x00
//Sensor specific messages are defined in their files


//Constant defining a command to set the RTC of the board
#define SET_RTC 0x03
//Constant defining a command to stop logging
#define SHUTDOWN_LOG 0x04
//Constant defining a command to start a new log file
#define START_LOG 0x05
//Sensor specific commands are defined in their files

//Sync character that should be found in commands
#define COMMAND_SYNC_CHAR '~'

#use "board_type.c"

//Library
//#use "dcrtcp.lib"

#if BIOPOD_TYPE == 0
	#use "c100_messages.c"
	#use "anaconda_messages.c"
#else //if BIOPOD_TYPE == 1
	#use "ibac_messages.c"
	#use "bridgeport_messages.c"
	#use "bladewerx_messages.c"
	#use "bladewerx_pump_messages.c"
#endif

//RTC conversions
#use "rtc_commands.c"

#use "servo_messages.c"

//Logger
#use "log_line.c"


//Set the index in the message buffer with the appropriate length (bodySize + header size)
void setMessageLength(char * buffer,int bodySize);
//Generate header information for the message buffer
void buildHeader(char * buffer, char sensorType);
//Accessor for the length of the message, read from the buffer
int getLength(char * buffer);
//Calculates the checksum value for the message buffer
char calculateChecksum(char * bufferPtr, char * lastPtr);
//Reset the incoming buffer to start processing at 0
void resetIncoming();



//Structure with details of a message broken out - header information and body
typedef struct uc_message{
	char syncBytes[3];
   char sensorType;
   long returnIp;
   int returnPort;
   int length;
   char body[255];
   char checksum;
} UCMessage;

//Index in the incoming buffer where we are processing
int incomingUdpbufferIndex;
int spectraSendMessageCounter;
//Message currently being processed
UCMessage incomingUcMessage;
//Message to log back

//Set the index in the message buffer with the appropriate length (dataSize + header size + checksumSize)
void setMessageLength(char * buffer, int dataSize)
{
  	buffer[LENGTH_INDEX] = ((dataSize+MessageHeaderSize+ChecksumSize)>>8)&0xff;   //add header size  + checksum
  	buffer[LENGTH_INDEX+1] = (dataSize+MessageHeaderSize+ChecksumSize)&0xff;   //add header size  + checksum
}

//Accessor for the length of the message, read from the buffer
int getLength (char *buffer)
{
	int len;

   len = 0;
   len |= buffer[LENGTH_INDEX] << 8;
   len |= buffer[LENGTH_INDEX+1];
	return len;
}

//Build a heartbeat message into the provided buffer.
int build_heartbeat(char * udpBuffer, char biopodType, char lastLogCommandOn, char actualLogStateOn, unsigned long lastSerialReceiveC, unsigned long lastSerialReceiveF,
			char voltageEighthsLocal, unsigned long voltageUpdatedLocal,
         float temperature, unsigned long temperatureUpdated, float humidity, unsigned long humidityUpdated,
         char servo0ToggledOpen, char lastServo0Duty, char fanToggledOn, char heaterToggledOn,
			char servo0ManualOverride, char fanManualOverride, char heaterManualOverride,
	      char temperatureLimitServo, char humidityLimitServo, char temperatureLimitFan,
   	   char humidityLimitFan, char temperatureLimitHeater, char humidityLimitHeater,
         int logErrCode)
{
	int index;
   char * bufferPtr;
	long rtc;
   long convTemp;
   long convHum;

   index=0;
   bufferPtr = &(udpBuffer[MESSAGE_BODY_INDEX]);

   //Add header, specifying the message came from the board itself
	buildHeader(udpBuffer, RABBIT_BOARD);

   //Set message type
   udpBuffer[MSG_TYPE_INDEX]= HEARTBEAT;

   //Add message timestamp
	rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Set network order
	lastSerialReceiveC = htonl(lastSerialReceiveC);
   lastSerialReceiveF = htonl(lastSerialReceiveF);
   voltageUpdatedLocal = htonl(voltageUpdatedLocal);
   temperatureUpdated = htonl(temperatureUpdated);
   humidityUpdated = htonl(humidityUpdated);
   logErrCode = htons (logErrCode);

	convTemp = (long) (1000l * temperature);
   convTemp = htonl (convTemp);
 	convHum = (long) (1000l * humidity);
   convHum = htonl (convHum);


   //Add board # message
   memcpy (bufferPtr, &biopodType, sizeof(biopodType));
   bufferPtr += (int)sizeof(biopodType);
   index += (int)sizeof(biopodType);

   //Add logging state info
   memcpy (bufferPtr, &lastLogCommandOn, sizeof(lastLogCommandOn));
   bufferPtr += (int)sizeof(lastLogCommandOn);
   index += (int)sizeof(lastLogCommandOn);

   memcpy (bufferPtr, &actualLogStateOn, sizeof(actualLogStateOn));
   bufferPtr += (int)sizeof(actualLogStateOn);
   index += (int)sizeof(actualLogStateOn);


   //Copy in last message recvd time from serial C
   memcpy(bufferPtr,&lastSerialReceiveC,sizeof(lastSerialReceiveC));
   bufferPtr+=(int)sizeof(lastSerialReceiveC);
   index+=(int)sizeof(lastSerialReceiveC);

   //Copy in last message recvd time from serial F
   memcpy(bufferPtr,&lastSerialReceiveF,sizeof(lastSerialReceiveF));
   bufferPtr+=(int)sizeof(lastSerialReceiveF);
   index+=(int)sizeof(lastSerialReceiveF);


   //Copy in last voltage reading
   memcpy(bufferPtr,&voltageEighthsLocal,sizeof(voltageEighthsLocal));
   bufferPtr+=(int)sizeof(voltageEighthsLocal);
   index+=(int)sizeof(voltageEighthsLocal);

   //Copy in last voltage recvd time
   memcpy(bufferPtr,&voltageUpdatedLocal,sizeof(voltageUpdatedLocal));
   bufferPtr+=(int)sizeof(voltageUpdatedLocal);
   index+=(int)sizeof(voltageUpdatedLocal);

   //Copy in last temperature
   memcpy(bufferPtr,&convTemp,sizeof(convTemp));
   bufferPtr+=(int)sizeof(convTemp);
   index+=(int)sizeof(convTemp);

   //Copy in last temperature recvd time
   memcpy(bufferPtr,&temperatureUpdated,sizeof(temperatureUpdated));
   bufferPtr+=(int)sizeof(temperatureUpdated);
   index+=(int)sizeof(temperatureUpdated);

   //Copy in last humidity
   memcpy(bufferPtr,&convHum,sizeof( convHum));
   bufferPtr+=(int)sizeof(convHum);
   index+=(int)sizeof(convHum);

   //Copy in last humidity recvd time
   memcpy(bufferPtr,&humidityUpdated,sizeof(humidityUpdated));
   bufferPtr+=(int)sizeof(humidityUpdated);
   index+=(int)sizeof(humidityUpdated);

   //Copy in last servo0 state
   memcpy(bufferPtr,&servo0ToggledOpen,sizeof(servo0ToggledOpen));
   bufferPtr+=(int)sizeof(servo0ToggledOpen);
   index+=(int)sizeof(servo0ToggledOpen);

   //Copy in last servo0 PWM
   memcpy(bufferPtr,&lastServo0Duty, sizeof(lastServo0Duty));
   bufferPtr+=(int)sizeof(lastServo0Duty);
   index+=(int)sizeof(lastServo0Duty);

   //Copy in last fan state
   memcpy(bufferPtr,&fanToggledOn,sizeof(fanToggledOn));
   bufferPtr+=(int)sizeof(fanToggledOn);
   index+=(int)sizeof(fanToggledOn);

   //Copy in last heater state
   memcpy(bufferPtr,&heaterToggledOn,sizeof(heaterToggledOn));
   bufferPtr+=(int)sizeof(heaterToggledOn);
   index+=(int)sizeof(heaterToggledOn);

   //Copy in last override state
   memcpy(bufferPtr,&servo0ManualOverride,sizeof(servo0ManualOverride));
   bufferPtr+=(int)sizeof(servo0ManualOverride);
   index+=(int)sizeof(servo0ManualOverride);

   //Copy in last override state
   memcpy(bufferPtr,&fanManualOverride,sizeof(fanManualOverride));
   bufferPtr+=(int)sizeof(fanManualOverride);
   index+=(int)sizeof(fanManualOverride);

   //Copy in last override state
   memcpy(bufferPtr,&heaterManualOverride,sizeof(heaterManualOverride));
   bufferPtr+=(int)sizeof(heaterManualOverride);
   index+=(int)sizeof(heaterManualOverride);

   //Copy in temp/humidity thresholds
   memcpy(bufferPtr,&temperatureLimitServo,sizeof(temperatureLimitServo));
   bufferPtr+=(int)sizeof(temperatureLimitServo);
   index+=(int)sizeof(temperatureLimitServo);
   memcpy(bufferPtr,&humidityLimitServo,sizeof(humidityLimitServo));
   bufferPtr+=(int)sizeof(humidityLimitServo);
   index+=(int)sizeof(humidityLimitServo);
   memcpy(bufferPtr,&temperatureLimitFan,sizeof(temperatureLimitFan));
   bufferPtr+=(int)sizeof(temperatureLimitFan);
   index+=(int)sizeof(temperatureLimitFan);
   memcpy(bufferPtr,&humidityLimitFan,sizeof(humidityLimitFan));
   bufferPtr+=(int)sizeof(humidityLimitFan);
   index+=(int)sizeof(humidityLimitFan);
   memcpy(bufferPtr,&temperatureLimitHeater,sizeof(temperatureLimitHeater));
   bufferPtr+=(int)sizeof(temperatureLimitHeater);
   index+=(int)sizeof(temperatureLimitHeater);
   memcpy(bufferPtr,&humidityLimitHeater,sizeof(humidityLimitHeater));
   bufferPtr+=(int)sizeof(humidityLimitHeater);
   index+=(int)sizeof(humidityLimitHeater);

   //Copy in last log error code
   memcpy(bufferPtr,&logErrCode,sizeof(logErrCode));
   bufferPtr+=(int)sizeof(logErrCode);
   index+=(int)sizeof(logErrCode);

   //Set Length
   setMessageLength(udpBuffer,index);

   //Set Checksum
	*bufferPtr = calculateChecksum(udpBuffer, bufferPtr);

   return getLength(udpBuffer);
}

void resetIncoming()
{
 	//memset(incomingUcMessage,0,sizeof(incomingUcMessage));
   incomingUcMessage.sensorType = 0;
   incomingUcMessage.returnIp = 0;
   incomingUcMessage.returnPort = 0;
   incomingUcMessage.length = 0;
   incomingUcMessage.checksum = 0;

   incomingUdpbufferIndex = 0;
}


//Generate header information for the message buffer
void buildHeader(char * buffer, char sensorType)
{
   char * bufferPosition;
   long ip;
   int port;
   bufferPosition = buffer;

   //Sync bytes
   *bufferPosition = '*';
   bufferPosition++;
   *bufferPosition = '*';
   bufferPosition++;
   *bufferPosition = '*';
   bufferPosition++;

   //Sensor type
   *bufferPosition = sensorType;
   bufferPosition += (int)sizeof(sensorType);

   //Sender IP
   ip = htonl(resolve(_PRIMARY_STATIC_IP));
   memcpy(bufferPosition, &ip, sizeof(ip));
   bufferPosition+= (int)sizeof(ip);

   //Sender port
   port = htons(LOCAL_PORT);
   memcpy(bufferPosition, &port, sizeof(port));
   bufferPosition+= (int)sizeof(port);
}

//Calculates the checksum value for the message buffer
char calculateChecksum(char * bufferPtr, char * checkSumPtr)
{
   char checksum;
   checksum=0;
   while(bufferPtr<checkSumPtr)
   {
      checksum+=*bufferPtr;
   	bufferPtr++;
   }
   return checksum;
}

scofunc int parse_and_build_c100(char * tokenBuffer, unsigned long rtc,char * udpBuffer)
{
   int length;
   int msgType;

   //Generate header information
   buildHeader (udpBuffer, SENSOR_C100);

   //Process C100 specific messages, index will be the size of the body data in the udp buffer after return, msgType is type of message received
   #if BIOPOD_TYPE == 0
 	  msgType = do_c100_processing(tokenBuffer, rtc, &(udpBuffer[MESSAGE_BODY_INDEX]), &length);
   #endif

   //Don't send messages for unparsed tokens
   if (msgType < 0)
   	return 0;

   udpBuffer[MSG_TYPE_INDEX] = (char)msgType;

   //Set Length
   setMessageLength(udpBuffer, length);

   //Set Checksum
   udpBuffer[MESSAGE_BODY_INDEX+length] = calculateChecksum(udpBuffer, &(udpBuffer[MESSAGE_BODY_INDEX+length]));

   return getLength(udpBuffer);
}

scofunc int parse_and_build_ibac(char * tokenBuffer, unsigned long rtc,char * udpBuffer)
{
	int length;
   char msgType;

   //Generate header information
   buildHeader (udpBuffer, SENSOR_IBAC);

   //Process IBAC specific messages, index will be the size of the body data in the udp buffer after return, msgType is type of message received
   #if BIOPOD_TYPE == 1
 	  msgType = do_ibac_processing(tokenBuffer, rtc, &(udpBuffer[MESSAGE_BODY_INDEX]), &length);
   #endif
   udpBuffer[MSG_TYPE_INDEX] = msgType;

   //Don't send messages for unparsed tokens
   if (msgType < 0)
   	return 0;

   //Set Length
   setMessageLength(udpBuffer, length);

   //Set Checksum
   udpBuffer[MESSAGE_BODY_INDEX+length] = calculateChecksum(udpBuffer, &(udpBuffer[MESSAGE_BODY_INDEX+length]));

   return getLength(udpBuffer);
}

scofunc int parse_and_build_anaconda(char * tokenBuffer, unsigned long rtc,char * udpBuffer)
{
   int length;
   char msgType;

#GLOBAL_INIT
{
	spectraSendMessageCounter = 0;
}

   //Generate header information
   buildHeader (udpBuffer, SENSOR_ANACONDA);

   //Process ANACONDA specific messages, index will be the size of the body data in the udp buffer after return, msgType is type of message received
   #if BIOPOD_TYPE == 0
 	  msgType = do_anaconda_processing(tokenBuffer, rtc, &(udpBuffer[MESSAGE_BODY_INDEX]), &length);
   #endif
   udpBuffer[MSG_TYPE_INDEX] = msgType;

   //Don't send messages for unparsed tokens
   if (msgType < 0)
   	return 0;

   //Set Length
   setMessageLength(udpBuffer, length);

   //Set Checksum
   udpBuffer[MESSAGE_BODY_INDEX+length] = calculateChecksum(udpBuffer, &(udpBuffer[MESSAGE_BODY_INDEX+length]));

   if (tokenBuffer[2] >= 0x84)
   {
    	if (spectraSendMessageCounter++ %3 != 0)
	      return 0;
   }

   return getLength(udpBuffer);
}

scofunc int parse_and_build_bladewerx(char * tokenBuffer, unsigned long rtc, int msgLength, char * udpBuffer)
{
	int length;
   char msgType;

   //Generate header information
   buildHeader (udpBuffer, SENSOR_BLADEWERX);

   //Process BLADEWERX specific messages, index will be the size of the body data in the udp buffer after return, msgType is type of message received
   #if BIOPOD_TYPE == 1
 	  msgType = do_bladewerx_processing(tokenBuffer, rtc, msgLength, &(udpBuffer[MESSAGE_BODY_INDEX]), &length);
	#endif
   udpBuffer[MSG_TYPE_INDEX] = msgType;

   //Don't send messages for unparsed tokens
   if (msgType < 0)
   	return 0;

   //Set Length
   setMessageLength(udpBuffer, length);

   //Set Checksum
   udpBuffer[MESSAGE_BODY_INDEX+length] = calculateChecksum(udpBuffer, &(udpBuffer[MESSAGE_BODY_INDEX+length]));

   return getLength(udpBuffer);
}


//Verify the sync characters of the command and determine which sensor it applies to.
char verifyCommand (char* buffer)
{
 	char* bufPtr;
   char retVal;
   int i;

   retVal = 0;
   bufPtr = buffer;
   for (i = 0; i < 3; i ++)
   {
	   if (*bufPtr != COMMAND_SYNC_CHAR)
		   retVal = (char)-1;
   	bufPtr ++;
   }

   if (retVal < 0)
   {
   	//We didn't sync, return error code
	   return retVal;
	}
   else
   {
   	//We are synced, return the next byte which corresponds to the sensor type
   	return *bufPtr;
   }
}

//Parse bytes of the command message.
int parseByte(char byte)
{
	switch(incomingUdpbufferIndex)
   {
   	//Header data up to byte 11
   	case 0:
      case 1:
      case 2:
      	if(byte != COMMAND_SYNC_CHAR){
         	resetIncoming();
            return -1;
         }
         incomingUcMessage.syncBytes[incomingUdpbufferIndex] = byte;
			break;
      case 3:
      	incomingUcMessage.sensorType = byte;
         break;

         //IP
      case 4:
      case 5:
      case 6:
      case 7:
         incomingUcMessage.returnIp |= byte<<((7-incomingUdpbufferIndex)*8);
      	break;

         //PORT
      case 8:
      case 9:
         incomingUcMessage.returnPort |= byte<<((9-incomingUdpbufferIndex)*8);
      	break;

   	case 10:
      case 11:
         incomingUcMessage.length |= (byte&0xFF)<<((11-incomingUdpbufferIndex)*8);

         break;

      case 13+256:
      	//overflow buffer, reset buffers
         resetIncoming();
         return -1;

      default:
      	if(incomingUdpbufferIndex==incomingUcMessage.length-1)
         {
         	if(incomingUcMessage.checksum == byte)
            {
               //Set next byte of body text to \0 just to be sure...
               incomingUcMessage.body[incomingUdpbufferIndex-12] = 0;

               //Checksum is valid, return valid message indicator
               return 1;
            }
            else
            {
            	//Checksum invalid, reset buffers
            	resetIncoming();
               return -1;
            }
         }
         else
         {
         	//This is just 'body' data after the header - store it
	        incomingUcMessage.body[incomingUdpbufferIndex-12] = byte;
	      }

   }
   incomingUcMessage.checksum+=byte;
	incomingUdpbufferIndex++;
   return 0;

}

scofunc void interpret_ibac_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage)
{
	int i;

   memset(ser_cmd,0,sizeof(ser_cmd));

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
       	//We have a full message read in to command the IBAC
         //Pass processing onto IBAC specific parser...

         #if BIOPOD_TYPE == 1
 	  			do_ibac_command (incomingUcMessage.body, ser_cmd, LogMessage);
         #endif

      }
   }

}

scofunc void interpret_pod_command(char* incomingUdpbuffer, int bytesReceived, char* LogMessage)
{
   int i;
   unsigned long rtc;
   unsigned long check;
   struct tm time;

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
       	//We have a full message read in to command the board
         //Process it here...

        	if (incomingUcMessage.body[0] == SET_RTC)
         {
         	//Set RTC of board
            memcpy(&rtc,&(incomingUcMessage.body[1]),sizeof(rtc));
            rtc = ntohl (rtc);
            set1970RTC (rtc);
            wfd log_msg (NULL, "Set RTC, making new log\r\n\0", 1);
            log_new();
            sprintf(LogMessage,"SET_RTC received: %ld\r\n\0",rtc);

         }
        	else if (incomingUcMessage.body[0] == SHUTDOWN_LOG)
         {
         	//Set RTC of board
            sprintf(LogMessage,"SHUTDOWN_LOG received: \r\n\0");
            wfd log_msg (NULL, "Shutdown log commanded\r\n\0", 1);
            log_fs_shutdown();
         }
        	else if (incomingUcMessage.body[0] == START_LOG)
         {
         	//Set RTC of board
            sprintf(LogMessage,"START_LOG received: \r\n\0");
            wfd log_msg (NULL, "Start new log commanded\r\n\0", 1);
            log_new();
         }
      	else
         {
          	sprintf(LogMessage,"UNKNOWN board command received: %x\r\n\0",incomingUcMessage.body[0]);
            printf("Unable to parse COMMAND_TYPE %d\n\0",incomingUcMessage.body[0] );
      	}
         //printf ("passed through");
      }
   }

/*   if (incomingUdpbuffer[12] == 0x05)
   {
      log_new();
   }
   else if (incomingUdpbuffer[12] = 0x04)
   {
      log_fs_shutdown();
   }*/

}

scofunc void interpret_c100_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage, char* responseBuffer, int* responseLen)
{
	int i;
   char msgType;

   memset(ser_cmd,0,sizeof(ser_cmd));

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
       	//We have a full message read in to command the C100
         //Pass processing onto C100 specific parser...
         #if BIOPOD_TYPE == 0
 	  			msgType = do_c100_command (incomingUcMessage.body, ser_cmd, LogMessage, &(responseBuffer[MESSAGE_BODY_INDEX]), responseLen);
         #endif

         if (msgType > 0)
         {
	         //Generate header information
	      	buildHeader (responseBuffer, SENSOR_C100);

	         responseBuffer[MSG_TYPE_INDEX] = msgType;

	         //Set Length
	         setMessageLength(responseBuffer, *responseLen);

	         //Set Checksum
	         responseBuffer[MESSAGE_BODY_INDEX+*responseLen] = calculateChecksum(responseBuffer, &(responseBuffer[MESSAGE_BODY_INDEX+*responseLen]));

	         *responseLen = getLength(responseBuffer);
         }

      }
   }

}

scofunc int interpret_anaconda_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage)
{
	int i;

   memset(ser_cmd,0,sizeof(ser_cmd));

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
         //We have a full message read in to command the Anaconda
         //Pass processing onto Anaconda specific parser...
         #if BIOPOD_TYPE == 0
 	  			return do_anaconda_command (incomingUcMessage.body, ser_cmd, LogMessage);
         #endif
      }
   }
   return -1;

}

scofunc void interpret_bladewerx_command(char* incomingUdpbuffer, int bytesReceived, char* ser_cmd, char* LogMessage)
{
	int i;

   memset(ser_cmd,0,sizeof(ser_cmd));

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
      	//We have a full message read in to command the Bladewerx
         //Pass processing onto Bladewerx specific parser...

			#if BIOPOD_TYPE == 1
 	  			do_bladewerx_command (incomingUcMessage.body, ser_cmd, LogMessage);
         #endif
      }
   }


}

scofunc void interpret_bladewerx_pump_command(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char* LogMessage)
{
	int i;
  	char msgType;


   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
         //Generate header information
	      buildHeader (responseBuffer, SENSOR_BLADEWERX_PUMP);

         //We have a full message read in to command the Bladewerx
         //Pass processing onto Bladewerx specific parser...
         #if BIOPOD_TYPE == 1
 	  			msgType = do_bladewerx_pump_command (incomingUcMessage.body, LogMessage, &(responseBuffer[MESSAGE_BODY_INDEX]), responseLen);
         #endif
 	      responseBuffer[MSG_TYPE_INDEX] = msgType;

	      //Don't send messages for unparsed message
	      if (msgType < 0)
				return;

	      //Set Length
	      setMessageLength(responseBuffer, *responseLen);

	      //Set Checksum
	      responseBuffer[MESSAGE_BODY_INDEX+*responseLen] = calculateChecksum(responseBuffer, &(responseBuffer[MESSAGE_BODY_INDEX+*responseLen]));

	      *responseLen = getLength(responseBuffer);

/*         *responseLen = 19;
         sprintf(responseBuffer, "hello world y ou suckasdf\0");*/
      }
   }


}

scofunc void save_bridgeport_message(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char* LogMessage)
{
	int i;
	char msgType;

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
         //Generate header information
	      buildHeader (responseBuffer, SENSOR_BRIDGEPORT);

       	//We have a full message read in from the Bridgeport sensor
         //Pass processing onto Bridgeport specific parser...
         #if BIOPOD_TYPE == 1
 	  			msgType = do_bridgeport_save (incomingUcMessage.body, LogMessage, &(responseBuffer[MESSAGE_BODY_INDEX]), responseLen);
         #endif
 	      responseBuffer[MSG_TYPE_INDEX] = msgType;

	      //Don't send messages for unparsed message
	      if (msgType < 0)
	         return;

	      //Set Length
	      setMessageLength(responseBuffer, *responseLen);

	      //Set Checksum
	      responseBuffer[MESSAGE_BODY_INDEX+*responseLen] = calculateChecksum(responseBuffer, &(responseBuffer[MESSAGE_BODY_INDEX+*responseLen]));

	      *responseLen = getLength(responseBuffer);
      }
   }

}

scofunc void interpret_servo_command(char* incomingUdpbuffer, int bytesReceived, char* responseBuffer, int* responseLen, char biopodType, char* LogMessage)
{
	int i;
	char msgType;

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
         //Generate header information
	      buildHeader (responseBuffer, SENSOR_SERVO);

       	//We have a full message read in from the Bridgeport sensor
         //Pass processing onto Bridgeport specific parser...
         msgType = do_servo_command (incomingUcMessage.body, LogMessage, &(responseBuffer[MESSAGE_BODY_INDEX]), responseLen, biopodType);
 	      responseBuffer[MSG_TYPE_INDEX] = msgType;

	      //Don't send messages for unparsed message
	      if (msgType < 0)
	         return;

	      //Set Length
	      setMessageLength(responseBuffer, *responseLen);

	      //Set Checksum
	      responseBuffer[MESSAGE_BODY_INDEX+*responseLen] = calculateChecksum(responseBuffer, &(responseBuffer[MESSAGE_BODY_INDEX+*responseLen]));

	      *responseLen = getLength(responseBuffer);
      }
   }
}

scofunc void interpret_fan_command(char* incomingUdpbuffer, int bytesReceived, char* LogMessage)
{
   int i;
	char msgType;

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
       	//We have a full message read in for the fan sensor
         //Pass processing onto fan specific parser...
         do_fan_command (incomingUcMessage.body, LogMessage);
      }
   }
}

#if BIOPOD_TYPE == 0
scofunc void interpret_heater_command(char* incomingUdpbuffer, int bytesReceived, char* LogMessage)
{
   int i;
	char msgType;

   resetIncoming();

   for(i=0;i<bytesReceived;i++)
   {
   	//parse bytes until checksum is valid
      if(parseByte(incomingUdpbuffer[i])>0)
      {
       	//We have a full message read in for the heater
         //Pass processing onto heater specific parser...
         do_heater_command (incomingUcMessage.body, LogMessage);
      }
   }
}
#endif

