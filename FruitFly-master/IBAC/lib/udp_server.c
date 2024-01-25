/*** BeginHeader udp_init,parse_and_build,handle_udp_packets,send_udp_message,build_heartbeat */
scofunc char * handle_udp_packets(char * ser_cmd);
scofunc void parse_and_build(char * tokenBuffer, unsigned long rtc,char * udpBuffer);
scofunc int send_udp_message(char * buffer);
void udp_init(int local_port);
void build_heartbeat(char * udpBuffer, char * lastToken, unsigned long lastSerialReceive);

/*** EndHeader */

#memmap xmem
#class auto



#define LENGTH_INDEX 9
#define MSG_TYPE_INDEX 10
#define BODY_INDEX 11

#define HEARTBEAT 0x00
#define PARTICLE_COUNT_TYPE 0x01
#define DIAGNOSTICS_TYPE 0x02


#define SET_RTC 0x03

#define IBAC_COMMAND_TYPE 0x10
#define IBAC_ALARM 0x11
#define IBAC_CLEAR_ALARM 0x12
#define IBAC_STATUS 0x13
#define IBAC_SLEEP 0x14
#define IBAC_TRACE_RATE 0x15
#define IBAC_DIAG_RATE 0x16
#define IBAC_AIR_SAMPLE 0x17
#define IBAC_COLLECT 0x18
#define IBAC_AUTO_COLLECT 0x19
#define IBAC_RAW 0x1A


#define COMMAND_SYNC_CHAR '~'

#use "dcrtcp.lib"

//Prototypes
void parseInfo(char * currentPosition, unsigned long rtc);
void parseFault(char * currentPosition, unsigned long rtc);
void parseS(char * currentPosition, unsigned long rtc);
void parseTrace(char * currentPosition, unsigned long rtc);
void parseDiagnostics(char * currentPosition, unsigned long rtc);
void parseBaseline(char * currentPosition, unsigned long rtc);
char * getNextToken(char * buffer);

void buildParticleCounts(char * udpBuffer);
void buildDiagnostics(char * udpBuffer);

void setLength(char * buffer,int bodySize);
void buildHeader(char * buffer);
char calculateChecksum(char * bufferPtr, char * checksumPtr);
void sendMessage(char * buffer);
void resetIncoming();
int parseByte(char byte);

//incoming command handlers
void setAlarm(char * SerialCommandBuffer, int on);
void clearAlarm(char * SerialCommandBuffer );
void status(char * SerialCommandBuffer );
void sleep(char * SerialCommandBuffer );
void setTraceRate(char * SerialCommandBuffer, int rate);
void diagRate(char * SerialCommandBuffer, int rate);
void airSample(char * SerialCommandBuffer );
void collect(char * SerialCommandBuffer, int on);
void autoCollect(char * SerialCommandBuffer, int on, int runtime);


typedef struct	iBAC_data
{
	unsigned long LastPressureFault;
   unsigned char PressureAtFault;
   unsigned long LaserPowerLowFault;
   unsigned long LaserPowerAboveFault;
   unsigned long LaserCurrentOutFault;
	unsigned char LaserInitialCurrent;
   unsigned char LaserCurrentCurrent;
   unsigned long BackgroundLghtBelowFault;
   unsigned long LastCollectingSample;
   unsigned long LastUnitAlarm;
   unsigned char IsSystemReady;
   unsigned char CollectionDiskSpinning;
   unsigned char Sleeping;
   char StatusAndVersion[MAX_TOKEN_SIZE];
} IBacData;


typedef struct particle_count_data{
	unsigned long TimeStamp;
	unsigned int CSI;
   unsigned int CLI;
   unsigned int BCSI;
   unsigned int BCLI;
   unsigned long CSA;
   unsigned long CLA;
   unsigned long BCSA;
   unsigned long BCLA;
   unsigned int BpSA;
   unsigned int BpLA;
   unsigned int SFI;
   unsigned int SFA;
   int AlarmCounter;
   unsigned char ValidBaseline;
   unsigned char AlarmStatus;
   unsigned char AlarmLatchState;
} ParticleCountData;

typedef struct diagnostics_data{
   unsigned long TimeStamp;
   unsigned char OutletPressure;
   unsigned char PressureAlarm;
   int Temperature;
   unsigned char TemperatureAlarm;
   unsigned int LaserPowerMonitor;
   unsigned char LaserPowerAlarm;
   unsigned int LaserCurrentMonitor;
   unsigned char LaserCurrentMonitorAlarm;
   unsigned int BackgroundMonitor;
   unsigned char BackgroundAlarm;
   unsigned int InputVoltage;
   unsigned char InputVoltageAlarm;
   unsigned int InputCurrent;
   unsigned char InputCurrentAlarm;
} DiagnosticsData;

typedef struct baseline_data{
	unsigned long TimeStamp;
   unsigned long BCLABaseline;
   unsigned int BpLABaseline;
	unsigned int SizeFractionBaseline;
}BaselineData;

typedef struct uc_message{
	char syncBytes[3];
   long returnIp;
   int returnPort;
   char length;
   char messageType;
   char body[255];
   char checksum;
} UCMessage;

udp_Socket incoming_sock;
udp_Socket outgoing_sock;
int localPort;
long remoteIp;
int remotePort;

//write buffer
char incomingUdpbuffer[256+12];
int incomingUdpbufferIndex;


UCMessage incomingUcMessage;

char LogMessage[128];

//local state
IBacData localIBacData;
ParticleCountData localParticleCounts;
DiagnosticsData localDiagnostics;
BaselineData localBaseline;


void udp_init(int local_port)
{
	sock_init(1);

	memset(&outgoing_sock,0,sizeof(outgoing_sock));
	memset(&incoming_sock,0,sizeof(incoming_sock));

   resetIncoming();


   //listen from all ip addresses
   localPort = local_port;
   if(!udp_open(&incoming_sock, local_port, 0, 0, NULL)) {
		printf("udp_open failed!\n");
		exit(0);
	}
   remoteIp = resolve(REMOTE_IP);
   remotePort = REMOTE_PORT;

   //init data structures
   memset(&localIBacData,0,sizeof(localIBacData));
   memset(&localParticleCounts,0,sizeof(localParticleCounts));
   memset(&localDiagnostics,0,sizeof(localDiagnostics));
   memset(&localBaseline,0,sizeof(localBaseline));

}



void resetIncoming()
{
 	memset(&incomingUcMessage,0,sizeof(incomingUcMessage));
   incomingUdpbufferIndex = 0;
}

int parseByte(char byte)
{
	switch(incomingUdpbufferIndex)
   {
   	case 0:
      case 1:
      case 2:
      	if(byte != COMMAND_SYNC_CHAR){
         	resetIncoming();
            return -1;
         }
         incomingUcMessage.syncBytes[incomingUdpbufferIndex] = byte;
			break;
         //IP
      case 3:
      case 4:
      case 5:
      case 6:
         incomingUcMessage.returnIp |= byte<<((6-incomingUdpbufferIndex)*8);
      	break;

         //PORT
      case 7:
      case 8:
         incomingUcMessage.returnPort |= byte<<((8-incomingUdpbufferIndex)*8);
      	break;

   	case 9:
         incomingUcMessage.length = byte;
         break;

      case 10:
         incomingUcMessage.messageType = byte;
         break;

      case 12+256:
      	//overflow buffer
         resetIncoming();
         return -1;

      default:
      	if(incomingUdpbufferIndex==incomingUcMessage.length+11){
         	if(incomingUcMessage.checksum == byte)
            {
            	return 1;
            }
            else{
            	resetIncoming();
               return -1;
            }
         } else
         {
	        incomingUcMessage.body[incomingUdpbufferIndex-11] = byte;
	       }

   }
   incomingUcMessage.checksum+=byte;
	incomingUdpbufferIndex++;
   return 0;

}
void setAlarm(char * SerialCommandBuffer, int on){
   sprintf(SerialCommandBuffer,"$alarm,%d\r",on);
}
void clearAlarm(char * SerialCommandBuffer){
   sprintf(SerialCommandBuffer,"$clear alarm\r");
}
void status(char * SerialCommandBuffer){
	sprintf(SerialCommandBuffer,"$status\r");
}
void sleep(char * SerialCommandBuffer){

   sprintf(SerialCommandBuffer,"$sleep\r");
}
void setTraceRate(char * SerialCommandBuffer, int rate){
   sprintf(SerialCommandBuffer,"$trace rate,%d\r",rate);
}
void diagRate(char * SerialCommandBuffer, int rate){
   sprintf(SerialCommandBuffer,"$diag rate,%d\r",rate);
}
void airSample(char * SerialCommandBuffer){
	sprintf(SerialCommandBuffer,"$air_sample\r");
}
void collect(char * SerialCommandBuffer, int on){
   sprintf(SerialCommandBuffer,"$collect,%d\r",on);
}
void autoCollect(char * SerialCommandBuffer, int on, int runtime){
   sprintf(SerialCommandBuffer,"$auto_collect,%d,%d\r",on,runtime);
}


void setRTC(unsigned long rtc)
{  struct tm time;
   mktm(&time, rtc);
   time.tm_year = time.tm_year-10;
	tm_wr(&time);
   SEC_TIMER = mktime(&time);
}


scofunc char *  handle_udp_packets( char * ser_cmd )
{
	int bytesReceived;
   int i;
   unsigned long rtc;
   char buf[256+12];

	memset(incomingUdpbuffer,0,sizeof(incomingUdpbuffer));
   waitfor( (bytesReceived= udp_recv(&incoming_sock, buf, sizeof(buf)))!=-1);
   //handle packet
   memset(LogMessage,0,sizeof(LogMessage));
   memset(ser_cmd,0,sizeof(ser_cmd));
   for(i=0;i<bytesReceived;i++)
   {
   	//parse byte
      if(parseByte(buf[i])>0)
      {
      	//dispatch full message
         switch((int)incomingUcMessage.messageType)
         {
            case SET_RTC:
                  memcpy(&rtc,&(incomingUcMessage.body[0]),sizeof(rtc));
                  sprintf(LogMessage,"SET_RTC received: %ul\r\n",ntohl(rtc));
                  setRTC(ntohl(rtc));
                  break;

            	break;
            case IBAC_COMMAND_TYPE:
            	switch((int)incomingUcMessage.body[0])
               {
               case IBAC_ALARM:
               	setAlarm(ser_cmd,incomingUcMessage.body[1]);
                  sprintf(LogMessage,"IBAC_ALARM received: %x\r\n",incomingUcMessage.body[1]);
                  break;
               case IBAC_CLEAR_ALARM:
               	sprintf(LogMessage,"IBAC_CLEAR_ALARM received.\r\n");
               	clearAlarm(ser_cmd);
               	break;
               case IBAC_STATUS:
                  sprintf(LogMessage,"IBAC_STATUS received.\r\n");
               	status(ser_cmd);
                  break;
               case IBAC_SLEEP:
               	sprintf(LogMessage,"IBAC_SLEEP received.\r\n");
               	sleep(ser_cmd);
                  break;
               case IBAC_TRACE_RATE:
               	sprintf(LogMessage,"IBAC_TRACE_RATE received: %d\r\n",incomingUcMessage.body[1]);
               	setTraceRate(ser_cmd,incomingUcMessage.body[1]);
                  break;
               case IBAC_DIAG_RATE:
               	sprintf(LogMessage,"IBAC_DIAG_RATE received: %d\r\n",incomingUcMessage.body[1]);
               	diagRate(ser_cmd,incomingUcMessage.body[1]);
                  break;
               case IBAC_AIR_SAMPLE:
               	sprintf(LogMessage,"IBAC_AIR_SAMPLE received.\r\n");
               	airSample(ser_cmd);
                  break;
               case IBAC_COLLECT:
               	sprintf(LogMessage,"IBAC_COLLECT received: %d\r\n",incomingUcMessage.body[1]);
               	collect(ser_cmd,incomingUcMessage.body[1]);
                  break;
               case IBAC_AUTO_COLLECT:
               	sprintf(LogMessage,"IBAC_AUTO_COLLECT received: %d, %d\r\n",incomingUcMessage.body[1],
	                  (incomingUcMessage.body[2]<<	24)|
	                  (incomingUcMessage.body[3]<<16)|
	                  (incomingUcMessage.body[4]<<8)|
	                  (incomingUcMessage.body[5]));
               	autoCollect(ser_cmd,incomingUcMessage.body[1],
                  (incomingUcMessage.body[2]<<24)|
                  (incomingUcMessage.body[3]<<16)|
                  (incomingUcMessage.body[4]<<8)|
                  (incomingUcMessage.body[5]));
                  break;

              case IBAC_RAW:
              		sprintf(LogMessage,"IBAC_RAW received: %s\r\n",incomingUcMessage.body+1);
                  //first byte is message subtype
                  sprintf(ser_cmd,incomingUcMessage.body+1);
               	printf("Writing raw command to serial\n");
	               break;
		  			default:
               	sprintf(LogMessage,"IBAC_UNKNOWN received: %x\r\n",incomingUcMessage.body[0]);
               	printf("Unable to parse UC_COMMAND_TYPE %d\n",incomingUcMessage.body[0] );
               }
            	break;
         	default:
            	sprintf(LogMessage,"BIOPOD_UNKNOWN received: %x\r\n",incomingUcMessage.messageType);
            	printf("Invalid message type received %d\n",incomingUcMessage.messageType);
         }
         //successfully got a full break
         localIBacData.Sleeping = 0;
         resetIncoming();
      }
   }


   return LogMessage;
}

scofunc void parse_and_build(char * tokenBuffer, unsigned long rtc,char * udpBuffer)
{
   char * currentPosition;
   currentPosition = tokenBuffer;
   memset(udpBuffer,0,sizeof(udpBuffer));
	//parse the first token
   if(strstr(currentPosition,"$info")==currentPosition)
   {
	   //parse info
      parseInfo(currentPosition,rtc);
      buildDiagnostics(udpBuffer);

   } else
   if(strstr(currentPosition,"$fault")==currentPosition)
   {
	   //parse fault
      parseFault(currentPosition,rtc);
      buildDiagnostics(udpBuffer);
   } else
   if(strstr(currentPosition,"$s,")==currentPosition)
   {
	   //parse s
      parseS(currentPosition,rtc);
      buildDiagnostics(udpBuffer);
   } else
   if(strstr(currentPosition,"$trace")==currentPosition)
   {
	   //parse trace
      parseTrace(currentPosition,rtc);
      buildParticleCounts(udpBuffer);
   } else
   if(strstr(currentPosition,"$diagnostics")==currentPosition)
   {
	   //parse diagnostics
      parseDiagnostics(currentPosition,rtc);
      buildDiagnostics(udpBuffer);
   } else
   if(strstr(currentPosition,"$baseline")==currentPosition)
   {
	   //parse baseline
      parseBaseline(currentPosition,rtc);
      buildParticleCounts(udpBuffer);
   }else
   /* Responses */
   if(strstr(currentPosition,"$sleep")==currentPosition)
   {
	   //sleeping response
      printf("Sleeping!\n");
   	localIBacData.Sleeping = 1;
      buildDiagnostics(udpBuffer);
   } else  {
		printf("Unknown or malformed token, %s, aborting\n",tokenBuffer);
      return;
   }


}

char * getNextToken(char * buffer)
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

void parseInfo(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	if(strstr(currentPosition,"collecting sample")==currentPosition)
	{
   	localIBacData.LastCollectingSample = rtc;
	} else
	if(strstr(currentPosition,"the unit has alarmed")==currentPosition)
	{
	   localIBacData.LastUnitAlarm = rtc;
	} else
	if(strstr(currentPosition,"system ready")==currentPosition)
	{
   	localIBacData.IsSystemReady = 1;
	} else
	if (strstr(currentPosition,"revision")==currentPosition)
	{
		strcpy(localIBacData.StatusAndVersion,currentPosition);
	} else
	{
		printf("Unhandled $info string: %s\n",currentPosition);
	}
}

void parseFault(char * currentPosition, unsigned long rtc)
{
    //get fault code
    int faultCode;

   //skip type
    if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
    faultCode = atoi(currentPosition);
    switch(faultCode)
    {
    	case 10:
      	localIBacData.LastPressureFault=rtc;
         currentPosition = strchr(currentPosition,'=')+1;
         localIBacData.LaserCurrentCurrent = (int)(atof(currentPosition)*10);
      	break;
      case 20:
      	if(strstr(currentPosition,"above")!=NULL)
	        	localIBacData.LaserPowerAboveFault=rtc;
         else
	         localIBacData.LaserPowerAboveFault=rtc;
      	break;
      case 30:
        	localIBacData.LaserCurrentOutFault=rtc;
         currentPosition = strchr(currentPosition,'=')+1;
         localIBacData.LaserCurrentCurrent = atoi(currentPosition);
      	break;
      case 40:
        	localIBacData.BackgroundLghtBelowFault=rtc;
      	break;
      default:
      	printf("Unhanded fault message %s\n",currentPosition);
    }
}

void parseS(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	//skip version number
   if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   //skip serial number
   if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   //get collection disk status
   localIBacData.CollectionDiskSpinning = atoi(currentPosition);
   //ignore faults for now.
}

void parseTrace(char * currentPosition, unsigned long rtc)
{
   localParticleCounts.TimeStamp = rtc;

   //skip type
	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localParticleCounts.CSI = atoi(currentPosition);

   if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.CLI = atoi(currentPosition);

   if((currentPosition = getNextToken(currentPosition))==NULL)
   	return;
   localParticleCounts.BCSI = atoi(currentPosition);

   if((currentPosition = getNextToken(currentPosition))==NULL)
      return;
   localParticleCounts.BCLI = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.CSA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.CLA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.BCSA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
  	localParticleCounts.BCLA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.BpSA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localParticleCounts.BpLA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.SFI = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.SFA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localParticleCounts.AlarmCounter = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.ValidBaseline = (unsigned char)atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.AlarmStatus = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
   localParticleCounts.AlarmLatchState = atoi(currentPosition);
}

void parseDiagnostics(char * currentPosition, unsigned long rtc)
{
	localDiagnostics.TimeStamp = rtc;

   //skip type
   if((currentPosition = getNextToken(currentPosition))==NULL)
		return;

	localDiagnostics.OutletPressure = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.PressureAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.Temperature =(int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.TemperatureAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserPowerMonitor = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserPowerAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserCurrentMonitor = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserCurrentMonitorAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.BackgroundMonitor = (int)(atof(currentPosition)*100);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.BackgroundAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputVoltage = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputVoltageAlarm = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputCurrent = atoi(currentPosition);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputCurrentAlarm = atoi(currentPosition);

}

void parseBaseline(char * currentPosition, unsigned long rtc)
{
   //skip type
	localBaseline.TimeStamp = rtc;
 	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localBaseline.BCLABaseline = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localBaseline.BpLABaseline = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextToken(currentPosition))==NULL)
		return;
	localBaseline.SizeFractionBaseline = (int)(atof(currentPosition)*10) ;
}

void buildParticleCounts(char * udpBuffer)
{
	int index;
   char * bufferPtr;
	long rtc;

   ParticleCountData nParticleCount;
   BaselineData nBaseline;

   index=0;
   bufferPtr = &(udpBuffer[BODY_INDEX]);

   //Add header
	buildHeader(udpBuffer);

   //Set message type
   udpBuffer[MSG_TYPE_INDEX]= PARTICLE_COUNT_TYPE ;

   //add a message timestamp
   rtc = htonl(read_rtc());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nParticleCount, &localParticleCounts, sizeof(localParticleCounts));
	memcpy(&nBaseline, &localBaseline, sizeof(localBaseline));

   //set byte order of particle count data
	nParticleCount.TimeStamp = (unsigned long) htonl((long)nParticleCount.TimeStamp);
	nParticleCount.CSI = htons(nParticleCount.CSI);
	nParticleCount.CLI = htons(nParticleCount.CLI);
	nParticleCount.BCSI = htons(nParticleCount.BCSI);
	nParticleCount.BCLI = htons(nParticleCount.BCLI);
	nParticleCount.CSA = (unsigned long) htonl((long)nParticleCount.CSA);
	nParticleCount.CLA = (unsigned long) htonl((long)nParticleCount.CLA);
	nParticleCount.BCSA = (unsigned long) htonl((long)nParticleCount.BCSA);
	nParticleCount.BCLA = (unsigned long) htonl((long)nParticleCount.BCLA);
	nParticleCount.BpSA = htons(nParticleCount.BpSA);
	nParticleCount.BpLA = htons(nParticleCount.BpLA);
	nParticleCount.SFI = htons(nParticleCount.SFI);
	nParticleCount.SFA = htons(nParticleCount.SFA);
	nParticleCount.AlarmCounter = htons(nParticleCount.AlarmCounter);

	//set byte order of baseline data
	nBaseline.TimeStamp = (unsigned long) htonl(nBaseline.TimeStamp);
	nBaseline.BCLABaseline = (unsigned long) htonl(nBaseline.BCLABaseline);
	nBaseline.BpLABaseline = htons(nBaseline.BpLABaseline);
	nBaseline.SizeFractionBaseline = htons(nBaseline.SizeFractionBaseline);

   //copy in data
   memcpy(bufferPtr,&nParticleCount,sizeof(nParticleCount));
   bufferPtr+=(int)sizeof(nParticleCount);
   index+=(int)sizeof(nParticleCount);
	memcpy(bufferPtr,&nBaseline,sizeof(nBaseline));
   bufferPtr+=(int)sizeof(nBaseline);
   index+=(int)sizeof(nBaseline);

   //Set Length
   setLength(udpBuffer,index);

   //Set Checksum
	*bufferPtr = calculateChecksum(udpBuffer, bufferPtr);

}

void buildDiagnostics(char * udpBuffer)
{
	int index;
   char * bufferPtr;
	long rtc;

   IBacData nIBacData;
   DiagnosticsData nDiagnosticsData;

   index=0;
   bufferPtr = &(udpBuffer[BODY_INDEX]);

   //Add header
	buildHeader(udpBuffer);

   //Set message type
   udpBuffer[MSG_TYPE_INDEX]= DIAGNOSTICS_TYPE;

   //add a message timestamp
   rtc = htonl(read_rtc());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nIBacData, &localIBacData, sizeof(localIBacData));
	memcpy(&nDiagnosticsData, &localDiagnostics, sizeof(localDiagnostics));
   //set byte order, these are timestamps
   nIBacData.LastPressureFault = htonl(nIBacData.LastPressureFault);
   nIBacData.LaserPowerLowFault =  htonl(nIBacData.LaserPowerLowFault);
   nIBacData.LaserPowerAboveFault =  htonl(nIBacData.LaserPowerAboveFault);
   nIBacData.LaserCurrentOutFault = htonl(nIBacData.LaserCurrentOutFault);
   nIBacData.BackgroundLghtBelowFault =  htonl(nIBacData.BackgroundLghtBelowFault);
   nIBacData.LastCollectingSample = htonl(nIBacData.LastCollectingSample);
   nIBacData.LastUnitAlarm =htonl(nIBacData.LastUnitAlarm);

   nDiagnosticsData.TimeStamp = htonl(nDiagnosticsData.TimeStamp);
   nDiagnosticsData.Temperature = htons(nDiagnosticsData.Temperature);
   nDiagnosticsData.LaserPowerMonitor = htons(nDiagnosticsData.LaserPowerMonitor);
   nDiagnosticsData.LaserCurrentMonitor =  htons(nDiagnosticsData.LaserCurrentMonitor);
   nDiagnosticsData.BackgroundMonitor = htons(nDiagnosticsData.BackgroundMonitor);
   nDiagnosticsData.InputVoltage =  htons(nDiagnosticsData.InputVoltage);
   nDiagnosticsData.InputCurrent = htons(nDiagnosticsData.InputCurrent);

   //copy in data
   memcpy(bufferPtr,&nIBacData,sizeof(nIBacData));
   bufferPtr+=(int)sizeof(nIBacData);
   index+=(int)sizeof(nIBacData);
	memcpy(bufferPtr,&nDiagnosticsData,sizeof(nDiagnosticsData));
   bufferPtr+=(int)sizeof(nDiagnosticsData);
   index+=(int)sizeof(nDiagnosticsData);

   //Set Length
   setLength(udpBuffer,index);

   //Set Checksum
	*bufferPtr = calculateChecksum(udpBuffer, bufferPtr);


}

void setLength(char * buffer, int bodySize)
{
  buffer[LENGTH_INDEX] = bodySize+11+1;   //add header size = 11 + checksum = 1
}

void buildHeader(char * buffer)
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

   //IP
   ip = htonl(resolve(_PRIMARY_STATIC_IP));
   memcpy(bufferPosition, &ip, sizeof(ip));
   bufferPosition+= (int)sizeof(ip);

   //port
   port = htons(LOCAL_PORT);
   memcpy(bufferPosition, &port, sizeof(port));
   bufferPosition+= (int)sizeof(port);
}

char calculateChecksum(char * bufferPtr, char * checksumPtr)
{
   char checksum;
   checksum=0;
   while(bufferPtr<checksumPtr)
   {
      checksum+=*bufferPtr;
   	bufferPtr++;
   }
   return checksum;
}

scofunc int send_udp_message(char * buffer)
{
	int length;
   int retval;
   length = buffer[LENGTH_INDEX] + 12;
	if(!udp_open(&outgoing_sock, localPort, remoteIp, remotePort, NULL)) {
		printf("udp_open for response failed!\n");
      return -1;
	}
   retval = udp_send(&outgoing_sock, buffer, length);
	if (retval < 0) {
   	printf("Error sending message\n");
   }
   sock_close(&outgoing_sock);
	return retval;
}

void build_heartbeat(char * udpBuffer, char * lastToken, unsigned long lastSerialReceive)
{
	int index;
   char * bufferPtr;
	long rtc;

   IBacData nIBacData;
   DiagnosticsData nDiagnosticsData;

   index=0;
   bufferPtr = &(udpBuffer[BODY_INDEX]);

   //Add header
	buildHeader(udpBuffer);

   //Set message type
   udpBuffer[MSG_TYPE_INDEX]= HEARTBEAT;

   //add a message timestamp
   rtc = htonl(read_rtc());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	*bufferPtr =  localIBacData.Sleeping;
   bufferPtr++;
   index++;
   memcpy(bufferPtr,&rtc,sizeof(lastSerialReceive));
   bufferPtr+=(int)sizeof(lastSerialReceive);
   index+=(int)sizeof(lastSerialReceive);

   memcpy(bufferPtr,lastToken,strlen(lastToken));
   bufferPtr+=strlen(lastToken);
   index+=strlen(lastToken);

   //Set Length
   setLength(udpBuffer,index);

   //Set Checksum
	*bufferPtr = calculateChecksum(udpBuffer, bufferPtr);
}