/*** BeginHeader do_ibac_processing,do_ibac_command,ibac_sleep */
int do_ibac_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength);
void do_ibac_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage);
void ibac_sleep (char* ser_cmd);

char voltageEighths;
unsigned long voltageUpdated;

/*** EndHeader */


#memmap xmem
#class auto


//Constant defining an IBAC particle count message
#define PARTICLE_COUNT_TYPE 0x01
//Constant defining an IBAC diagnostics message
#define DIAGNOSTICS_TYPE 0x02

//Constants defining specific IBAC commands
#define IBAC_ALARM 0x11
#define IBAC_CLEAR_ALARM 0x12
#define IBAC_STATUS 0x13
#define IBAC_SLEEP 0x14
#define IBAC_TRACE_RATE 0x15
#define IBAC_DIAG_RATE 0x16
#define IBAC_AIR_SAMPLE 0x17
#define IBAC_COLLECT 0x18
#define IBAC_AUTO_COLLECT 0x19
#define IBAC_RAW 0xFF

//RTC conversions
#use "rtc_commands.c"



//Prototypes for message parsing
void parseIbacInfo(char * currentPosition, unsigned long rtc);
void parseFault(char * currentPosition, unsigned long rtc);
void parseIbacS(char * currentPosition, unsigned long rtc);
void parseTrace(char * currentPosition, unsigned long rtc);
void parseDiagnostics(char * currentPosition, unsigned long rtc);
void parseBaseline(char * currentPosition, unsigned long rtc);
char * getNextIBACToken(char * buffer);

//Prototypes for message building
int buildParticleCounts(char * udpBuffer, int* bodyLength);
int buildDiagnostics(char * udpBuffer, int* bodyLength);

//Data objects
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
   char StatusAndVersion[128];
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

//local states
IBacData localIBacData;
ParticleCountData localParticleCounts;
DiagnosticsData localDiagnostics;
BaselineData localBaseline;


int do_ibac_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength)
{
   //Initialize
   char * currentPosition;

   #GLOBAL_INIT
   {
		localIBacData.LastPressureFault = 0;
	   localIBacData.PressureAtFault = 0;
	   localIBacData.LaserPowerLowFault = 0;
	   localIBacData.LaserPowerAboveFault = 0;
	   localIBacData.LaserCurrentOutFault = 0;
	   localIBacData.LaserInitialCurrent = 0;
	   localIBacData.LaserCurrentCurrent = 0;
	   localIBacData.BackgroundLghtBelowFault = 0;
	   localIBacData.LastCollectingSample = 0;
	   localIBacData.LastUnitAlarm = 0;
	   localIBacData.IsSystemReady = 0;
	   localIBacData.CollectionDiskSpinning = 0;
	   localIBacData.Sleeping = 0;
	   memset (localIBacData.StatusAndVersion, 0, MAX_TOKEN_SIZE_E);

	   localParticleCounts.TimeStamp = 0;
	   localParticleCounts.CSI = 0;
	   localParticleCounts.CLI = 0;
	   localParticleCounts.BCSI = 0;
	   localParticleCounts.BCLI = 0;
	   localParticleCounts.CSA = 0;
	   localParticleCounts.CLA = 0;
	   localParticleCounts.BCSA = 0;
	   localParticleCounts.BCLA = 0;
	   localParticleCounts.BpSA = 0;
	   localParticleCounts.BpLA = 0;
	   localParticleCounts.SFI = 0;
	   localParticleCounts.SFA = 0;
	   localParticleCounts.AlarmCounter = 0;
	   localParticleCounts.ValidBaseline = 0;
	   localParticleCounts.AlarmStatus = 0;
	   localParticleCounts.AlarmLatchState = 0;

      localDiagnostics.TimeStamp = 0;
	   localDiagnostics.OutletPressure = 0;
	   localDiagnostics.PressureAlarm = 0;
	   localDiagnostics.Temperature = 0;
	   localDiagnostics.TemperatureAlarm = 0;
	   localDiagnostics.LaserPowerMonitor = 0;
	   localDiagnostics.LaserPowerAlarm = 0;
	   localDiagnostics.LaserCurrentMonitor = 0;
	   localDiagnostics.LaserCurrentMonitorAlarm = 0;
	   localDiagnostics.BackgroundMonitor = 0;
	   localDiagnostics.BackgroundAlarm = 0;
	   localDiagnostics.InputVoltage = 0;
	   localDiagnostics.InputVoltageAlarm = 0;
	   localDiagnostics.InputCurrent = 0;
	   localDiagnostics.InputCurrentAlarm = 0;

	   localBaseline.TimeStamp = 0;
	   localBaseline.BCLABaseline = 0;
	   localBaseline.BpLABaseline = 0;
	   localBaseline.SizeFractionBaseline = 0;

      voltageEighths = 0;
		voltageUpdated = 0;
   }

   currentPosition = tokenBuffer;
   memset(udpBuffer,0,sizeof(udpBuffer));

	//parse the first token, then parse the whole message and either create a diagnostics message or a particle count message depending on the token
   if(strstr(currentPosition,"$info,")==currentPosition)
   {
	   //parse info
      parseIbacInfo(currentPosition,rtc);
      return buildDiagnostics(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$fault,")==currentPosition)
   {
	   //parse fault
      parseFault(currentPosition,rtc);
      return buildDiagnostics(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$s,")==currentPosition)
   {
	   //parse s
      memset (localIBacData.StatusAndVersion, 0, sizeof(localIBacData.StatusAndVersion));
      parseIbacS(currentPosition,rtc);
      return buildDiagnostics(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$trace,")==currentPosition)
   {
	   //parse trace
      parseTrace(currentPosition,rtc);
      return buildParticleCounts(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$diagnostics,")==currentPosition)
   {
	   //parse diagnostics
      parseDiagnostics(currentPosition,rtc);
      return buildDiagnostics(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$baseline,")==currentPosition)
   {
	   //parse baseline
      parseBaseline(currentPosition,rtc);
      return buildParticleCounts(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$sleep")==currentPosition)
   {
	   //sleeping response
      printf("Sleeping!\n");
   	localIBacData.Sleeping = 1;
      return buildDiagnostics(udpBuffer, bodyLength);
   }
   else if(strstr(currentPosition,"$alarm")==currentPosition)
   {
   	//Command echo, ignore it
   }
   else if(strstr(currentPosition,"$clear alarm")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$status")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$trace rate")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$diag rate")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$air_sample")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$collect")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$auto_collect")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$prog")==currentPosition)
   {
   	//Command echo, ignore it
   }
	else if(strstr(currentPosition,"$invalid")==currentPosition)
   {
   	//Invalid command received by IBAC
     	printf ("Invalid command sent to IBAC: %s: unable to process.\n\0",tokenBuffer);
      *bodyLength = 0;
   	return -1;
   }
   else
   {
   	printf ("Unknown or malformed token from IBAC: %s: unable to parse.\n\0",tokenBuffer);
      *bodyLength = 0;
      return -1;
   }

   //Default action for command echos
   *bodyLength = 0;
   return -1;

}

char * getNextIBACToken(char * buffer)
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

void parseIbacInfo(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
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
    if((currentPosition = getNextIBACToken(currentPosition))==NULL)
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

void parseIbacS(char * currentPosition, unsigned long rtc)
{
   //skip type
	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;

   //Version number
   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;

   //Copy line of version/status
   sprintf (localIBacData.StatusAndVersion, "%s\0", currentPosition);

   //Serial number
   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;

   //get collection disk status
   localIBacData.CollectionDiskSpinning = atoi(currentPosition);
   //ignore faults for now.
}

void parseTrace(char * currentPosition, unsigned long rtc)
{
   localParticleCounts.TimeStamp = rtc;

   //skip type
	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localParticleCounts.CSI = atoi(currentPosition);

   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.CLI = atoi(currentPosition);

   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
   	return;
   localParticleCounts.BCSI = atoi(currentPosition);

   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
      return;
   localParticleCounts.BCLI = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.CSA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.CLA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.BCSA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
  	localParticleCounts.BCLA = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.BpSA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localParticleCounts.BpLA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.SFI = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.SFA = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localParticleCounts.AlarmCounter = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.ValidBaseline = (unsigned char)atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.AlarmStatus = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
   localParticleCounts.AlarmLatchState = atoi(currentPosition);
}

void parseDiagnostics(char * currentPosition, unsigned long rtc)
{
	localDiagnostics.TimeStamp = rtc;

   //skip type
   if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;

	localDiagnostics.OutletPressure = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.PressureAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.Temperature =(int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.TemperatureAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserPowerMonitor = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserPowerAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserCurrentMonitor = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.LaserCurrentMonitorAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.BackgroundMonitor = (int)(atof(currentPosition)*100);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.BackgroundAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputVoltage = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputVoltageAlarm = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputCurrent = atoi(currentPosition);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localDiagnostics.InputCurrentAlarm = atoi(currentPosition);


   //Only update voltage if voltage is over 1 V.  If reporting under 1 volt, the sensor shouldn't be operational
   if (localDiagnostics.InputVoltage/10 > 1)
   {
   	//Multiply by 8 to get eighth units, by divide by ten because InputVoltage is 10x larger than it should be (to make it an integer)
   	voltageEighths = (char)(0x00FF&((int)(localDiagnostics.InputVoltage*8/10.0)));
   	voltageUpdated = localDiagnostics.TimeStamp;
   }

}

void parseBaseline(char * currentPosition, unsigned long rtc)
{
   //skip type
	localBaseline.TimeStamp = rtc;
 	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localBaseline.BCLABaseline = (long)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localBaseline.BpLABaseline = (int)(atof(currentPosition)*10);

	if((currentPosition = getNextIBACToken(currentPosition))==NULL)
		return;
	localBaseline.SizeFractionBaseline = (int)(atof(currentPosition)*10) ;
}


int buildParticleCounts(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   ParticleCountData nParticleCount;
   BaselineData nBaseline;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
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

   *bodyLength = index;
   return PARTICLE_COUNT_TYPE;
}

int buildDiagnostics(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;

   IBacData nIBacData;
   DiagnosticsData nDiagnosticsData;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
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

   *bodyLength = index;
	return DIAGNOSTICS_TYPE;
}

void setAlarm(char * SerialCommandBuffer, int on)
{
   sprintf(SerialCommandBuffer,"$alarm,%d\r\0",on);
}
void clearAlarm(char * SerialCommandBuffer)
{
   sprintf(SerialCommandBuffer,"$clear alarm\r\0");
}
void statusIbac(char * SerialCommandBuffer)
{
	sprintf(SerialCommandBuffer,"$status\r\0");
}
void ibac_sleep(char * SerialCommandBuffer)
{
   sprintf(SerialCommandBuffer,"$sleep\r\0");
}
void setTraceRate(char * SerialCommandBuffer, int rate)
{
   sprintf(SerialCommandBuffer,"$trace rate,%d\r\0",rate);
}
void diagRate(char * SerialCommandBuffer, int rate)
{
   sprintf(SerialCommandBuffer,"$diag rate,%d\r\0",rate);
}
void airSample(char * SerialCommandBuffer)
{
	sprintf(SerialCommandBuffer,"$air sample\r\0");
}
void collect(char * SerialCommandBuffer, int on)
{
   sprintf(SerialCommandBuffer,"$collect,%d\r\0",on);
}
void autoCollect(char * SerialCommandBuffer, int on, int runtime)
{
   sprintf(SerialCommandBuffer,"$auto collect,%d,%d\r\0",on,runtime);
}

void do_ibac_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage)
{
   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case IBAC_ALARM:
       	setAlarm(ser_cmd,incomingUcMessageBody[1]);
         sprintf(LogMessage,"IBAC_ALARM received: %x\r\n\0",incomingUcMessageBody[1]);
         break;
      case IBAC_CLEAR_ALARM:
        	sprintf(LogMessage,"IBAC_CLEAR_ALARM received.\r\n\0");
        	clearAlarm(ser_cmd);
        	break;
      case IBAC_STATUS:
         sprintf(LogMessage,"IBAC_STATUS received.\r\n\0");
        	statusIbac(ser_cmd);
         break;
      case IBAC_SLEEP:
        	sprintf(LogMessage,"IBAC_SLEEP received.\r\n\0");
        	ibac_sleep(ser_cmd);
         break;
      case IBAC_TRACE_RATE:
        	sprintf(LogMessage,"IBAC_TRACE_RATE received: %d\r\n\0",incomingUcMessageBody[1]);
        	setTraceRate(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case IBAC_DIAG_RATE:
        	sprintf(LogMessage,"IBAC_DIAG_RATE received: %d\r\n\0",incomingUcMessageBody[1]);
        	diagRate(ser_cmd,incomingUcMessageBody[1]);
         break;
      case IBAC_AIR_SAMPLE:
        	sprintf(LogMessage,"IBAC_AIR_SAMPLE received.\r\n\0");
        	airSample(ser_cmd);
         break;
      case IBAC_COLLECT:
        	sprintf(LogMessage,"IBAC_COLLECT received: %d\r\n\0",incomingUcMessageBody[1]);
        	collect(ser_cmd,incomingUcMessageBody[1]);
         break;
      case IBAC_AUTO_COLLECT:
        	sprintf(LogMessage,"IBAC_AUTO_COLLECT received: %d, %d\r\n\0",incomingUcMessageBody[1],
	           (incomingUcMessageBody[2]<<	24)|
	           (incomingUcMessageBody[3]<<16)|
	           (incomingUcMessageBody[4]<<8)|
	           (incomingUcMessageBody[5]));
        	autoCollect(ser_cmd,incomingUcMessageBody[1],
              (incomingUcMessageBody[2]<<24)|
              (incomingUcMessageBody[3]<<16)|
              (incomingUcMessageBody[4]<<8)|
              (incomingUcMessageBody[5]));
         break;

      case IBAC_RAW:
      	sprintf(LogMessage,"IBAC_RAW received: %s\r\n\0",incomingUcMessageBody+1);
         //first byte is message subtype
         sprintf(ser_cmd,incomingUcMessageBody+1);
         printf("Writing raw command to serial\n");
	      break;
		default:
        	sprintf(LogMessage,"IBAC_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse IBAC UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
      }
      localIBacData.Sleeping = 0;
}