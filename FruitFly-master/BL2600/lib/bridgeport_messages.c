/*** BeginHeader do_bridgeport_save */
char do_bridgeport_save (char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen);

/*** EndHeader */


#define BRIDGEPORT_STATISTICS 0x01
#define BRIDGEPORT_HISTOGRAM 0x02
#define BRIDGEPORT_CONFIGURATION 0x03

char buildBridgeportMessage (char* incomingUcMessageBody, char* responseBuffer, int* responseLen)
{
   int index;
   char * bufferPtr;
	long rtc;

   index=0;
   bufferPtr = responseBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //if histogram data, add more information
   if (incomingUcMessageBody[0] == BRIDGEPORT_HISTOGRAM)
   {
    	memcpy (bufferPtr, &incomingUcMessageBody[1], 7);
      bufferPtr+=7;
	   index+=7;
   }
   else
   {
		memcpy (bufferPtr, &incomingUcMessageBody[1], 1);
      bufferPtr+=1;
	   index+=1;
   }

   *responseLen = index;
	return incomingUcMessageBody[0];
}

char do_bridgeport_save (char* incomingUcMessageBody, char* LogMessage, char* responseBuffer, int* responseLen)
{
   char len;
	long histVal;

  	//Type of message is in first byte, then any necessary data follows in the body data field
   switch((int)incomingUcMessageBody[0])
   {
      case BRIDGEPORT_STATISTICS:
   	   len = incomingUcMessageBody[1];
		   //Ignore checksum in print message
   		incomingUcMessageBody[2+len] = 0;

         sprintf(LogMessage,"Bridgeport statistics: %s\r\n\0",incomingUcMessageBody+2);
         break;
      case BRIDGEPORT_HISTOGRAM:
         len = incomingUcMessageBody[7];
		   //Ignore checksum in print message
   		incomingUcMessageBody[8+len] = 0;

         histVal = ((incomingUcMessageBody[1]&0xFF) << 24) | ((incomingUcMessageBody[2]&0xFF) << 16) | ((incomingUcMessageBody[3]&0xFF) << 8) | ((incomingUcMessageBody[4]&0xFF));
         sprintf(LogMessage,"Bridgeport hist: %ld (%d of %d): %s\r\n\0",histVal, incomingUcMessageBody[6], incomingUcMessageBody[5], incomingUcMessageBody+8);
         break;
      case BRIDGEPORT_CONFIGURATION:
	      len = incomingUcMessageBody[1];
		   //Ignore checksum in print message
   		incomingUcMessageBody[2+len] = 0;

         sprintf(LogMessage,"Bridgeport configuration: %s\r\n\0",incomingUcMessageBody+2);
         break;

		default:
        	sprintf(LogMessage,"BRIDGEPORT_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse C100 UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
   }

   return buildBridgeportMessage (incomingUcMessageBody, responseBuffer, responseLen);
}