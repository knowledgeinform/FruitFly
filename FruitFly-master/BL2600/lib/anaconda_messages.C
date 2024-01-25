/*** BeginHeader do_anaconda_processing,do_anaconda_command */
int do_anaconda_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength);
int do_anaconda_command(char* incomingUcMessageBody, char* ser_cmd, char* LogMessage);

char voltageEighths;
unsigned long voltageUpdated;

/*** EndHeader */


#memmap xmem
#class auto


//Constant defining an Anaconda status message
#define ANACONDA_STATUS_TYPE 0x01
#define ANACONDA_LCDA_REPORT 0x02
#define ANACONDA_LCDB_REPORT 0x03
#define ANACONDA_TEXT 0x04
#define ANACONDA_LCDA_G_SPECTRA 0x05
#define ANACONDA_LCDA_H_SPECTRA 0x06
#define ANACONDA_LCDB_G_SPECTRA 0x07
#define ANACONDA_LCDB_H_SPECTRA 0x08


//Constants defining specific Anaconda commands
#define ANACONDA_SET_GPS 0x11
#define ANACONDA_MODE_IDLE 0x12
#define ANACONDA_MODE_SEARCH 0x13
#define ANACONDA_MODE_STANDBY 0x14
#define ANACONDA_MODE_AIRFRAME 0x15
#define ANACONDA_MODE_POD 0x16
#define ANACONDA_ACTION_DELETE 0x17
#define ANACONDA_SET_DATETIME 0x18
#define ANACONDA_SET_SERVO_OPENLIMIT 0x19
#define ANACONDA_SET_SERVO_CLOSEDLIMIT 0x1A
#define ANACONDA_SET_MANIFOLD_HEATERTEMP 0x1B
#define ANACONDA_SET_PITOT_HEATERTEMP 0x1C
#define ANACONDA_ACTION_SAVE_SETTINGS 0x1D
#define ANACONDA_DEBUG_OPT 0x1E
#define ANACONDA_SEND_LCDA_G 0x1F
#define ANACONDA_SEND_LCDA_H 0x20
#define ANACONDA_SEND_LCDB_G 0x21
#define ANACONDA_SEND_LCDB_H 0x22
#define ANACONDA_STOP_LCDA_G 0x23
#define ANACONDA_STOP_LCDA_H 0x24
#define ANACONDA_STOP_LCDB_G 0x25
#define ANACONDA_STOP_LCDB_H 0x26
#define ANACONDA_RESET_SAMPLEUSAGE 0x27
#define ANACONDA_RAW 0xFF

#define ANACONDA_DEBUG_PERMIT_DEBUG 0x3E
#define ANACONDA_DEBUG_FORBID_DEBUG 0x3F
#define ANACONDA_DEBUG_SERVO_OPEN 0x40
#define ANACONDA_DEBUG_SERVO_CLOSED 0x41
#define ANACONDA_DEBUG_MANIFOLD_HEATON 0x42
#define ANACONDA_DEBUG_MANIFOLD_HEATOFF 0x43
#define ANACONDA_DEBUG_PITOT_HEATON 0x44
#define ANACONDA_DEBUG_PITOT_HEATOFF 0x45
#define ANACONDA_DEBUG_PUMPS_ON 0x46
#define ANACONDA_DEBUG_PUMPS_OFF 0x47
#define ANACONDA_DEBUG_VALVES1_OPEN 0x48
#define ANACONDA_DEBUG_VALVES2_OPEN 0x49
#define ANACONDA_DEBUG_VALVES3_OPEN 0x4A
#define ANACONDA_DEBUG_VALVES4_OPEN 0x4B
#define ANACONDA_DEBUG_VALVES1_CLOSED 0x4C
#define ANACONDA_DEBUG_VALVES2_CLOSED 0x4D
#define ANACONDA_DEBUG_VALVES3_CLOSED 0x4E
#define ANACONDA_DEBUG_VALVES4_CLOSED 0x4F
#define ANACONDA_DEBUG_VALVESALL_CLOSED 0x50
#define ANACONDA_DEBUG_WRITE_TEST 0x51
#define ANACONDA_DEBUG_WRITE_TEST_LCDA 0x52
#define ANACONDA_DEBUG_WRITE_TEST_LCDB 0x53
#define ANACONDA_DEBUG_WRITE_TEST_SYS 0x54
#define ANACONDA_DEBUG_RESET_LCDA 0x60
#define ANACONDA_DEBUG_RESET_LCDB 0x61
#define ANACONDA_DEBUG_RELEASE_LCDA 0x62
#define ANACONDA_DEBUG_RELEASE_LCDB 0x63

#define ANACONDA_PACKED_LSB_FIRST 1
#define ANACONDA_MAX_NUM_AGENTS 20
#define ANACONDA_MAX_TEXT_SIZE 200
#define ANACONDA_MAX_SPECTRA_SIZE 512

//RTC conversions
#use "rtc_commands.c"

//Store sample used settings in flash
#use "flash_handler.c"


//Prototypes for message parsing
void parseAnacondaStatus(char * currentPosition);
void parseAnacondaLCDAReport(char* currentPosition);
void parseAnacondaLCDBReport(char* currentPosition);
void parseAnacondaText (char* currentPosition);
void parseAnacondaSpectra (char* currentPosition);

//Prototypes for message building
int buildAnacondaStatusMessage(char * udpBuffer, int* bodyLength);
int buildAnacondaLCDAReportMessage(char* udpBuffer, int* bodyLength);
int buildAnacondaLCDBReportMessage(char* udpBuffer, int* bodyLength);
int buildAnacondaTextMessage (char* udpBuffer, int* bodyLength);
int buildAnacondaSpectraMessage (char* udpBuffer, int* bodyLength, int messageType);



//Data objects
typedef struct	anaconda_status
{
	long messageTime;
   long lcdaStatus;
   long lcdbStatus;
   char lcdaCurrMode;
   char lcdaReqMode;
   char lcdbCurrMode;
   char lcdbReqMode;
   short manifoldHeatTargTemp;
   short manifoldHeatActTemp;
   short pitotHeatTargTemp;
   short pitotHeatActTemp;
   float supplyVoltage;
   float supplyCurrent;
   char pitotValveStatus;
   char manifoldHeatStatus;
   char pitotHeatStatus;
   char srsPumpSupply;
   char srsValveSupply;
   char srsValveDrive;
	char systemInformation;
   char lcdbInformation;
   char lcdaInformation;
   char lcdbTestFile;
   char lcdaTestFile;
   char delete4;
   char delete3;
   char delete2;
   char delete1;
   char lcdbHSpectrum;
   char lcdaHSpectrum;
   char lcdbGSpectrum;
   char lcdaGSpectrum;
   char lcdbBusy;
   char lcdaBusy;
   char lcdbReset;
   char lcdaReset;
   char rs232External;
   char podCommsValid;
   char externalCommsValid;
   char sdSocketEmpty;
   char srs4;
   char srs3;
   char srs2;
   char srs1;
   char debugStatus;
} AnacondaStatus;

typedef struct anaconda_agent_data
{
	char agentID;
   char bars;
} AnacondaAgentData;

typedef struct	anaconda_report
{
	long messageTime;
   short pressure;
   short temperature;
   char numAgents;
   AnacondaAgentData agents [ANACONDA_MAX_NUM_AGENTS];
} AnacondaReport;

typedef struct anaconda_text
{
	long messageTime;
	char messageLength;
   char messageTrunc;
   char text [ANACONDA_MAX_TEXT_SIZE];
} AnacondaText;

typedef struct anaconda_spectra
{
	long messageTime;
   char nibbleData[ANACONDA_MAX_SPECTRA_SIZE];
} AnacondaSpectra;


//local states
AnacondaStatus localAnacondastatus;
AnacondaReport localAnacondaReportA;
AnacondaReport localAnacondaReportB;
AnacondaText localAnacondaText;
AnacondaSpectra localAnacondaSpectra;

int do_anaconda_processing(char * tokenBuffer, unsigned long rtc,char * udpBuffer, int *bodyLength)
{
   #GLOBAL_INIT
	{
   	//Initialize local states once
      voltageEighths = 0;
		voltageUpdated = 0;
	}

	if (tokenBuffer[0] == 0x7E && tokenBuffer[1] == 0x7E)
   {
   	//Determine message type
		if (tokenBuffer[2] == 0x80)
      {
       	parseAnacondaStatus (&tokenBuffer[3]);
         return buildAnacondaStatusMessage (udpBuffer, bodyLength);
      }
      else if (tokenBuffer[2] == 0x81)
      {
       	parseAnacondaLCDAReport (&tokenBuffer[3]);
         return buildAnacondaLCDAReportMessage (udpBuffer, bodyLength);
      }
      else if (tokenBuffer[2] == 0x82)
      {
       	parseAnacondaLCDBReport (&tokenBuffer[3]);
         return buildAnacondaLCDBReportMessage (udpBuffer, bodyLength);
      }
      else if (tokenBuffer[2] == 0x83)
      {
       	parseAnacondaText (&tokenBuffer[3]);
         return buildAnacondaTextMessage (udpBuffer, bodyLength);
      }
      else if (tokenBuffer[2] == 0x84)
      {
       	parseAnacondaSpectra (&tokenBuffer[2]);
         return buildAnacondaSpectraMessage (udpBuffer, bodyLength, ANACONDA_LCDA_G_SPECTRA);
      }
      else if (tokenBuffer[2] == 0x85)
      {
       	parseAnacondaSpectra (&tokenBuffer[2]);
         return buildAnacondaSpectraMessage (udpBuffer, bodyLength, ANACONDA_LCDA_H_SPECTRA);
      }
      else if (tokenBuffer[2] == 0x86)
      {
       	parseAnacondaSpectra (&tokenBuffer[2]);
         return buildAnacondaSpectraMessage (udpBuffer, bodyLength, ANACONDA_LCDB_G_SPECTRA);
      }
      else if (tokenBuffer[2] == 0x87)
      {
       	parseAnacondaSpectra (&tokenBuffer[2]);
         return buildAnacondaSpectraMessage (udpBuffer, bodyLength, ANACONDA_LCDB_H_SPECTRA);
      }
   }

   return -1;
}

void parseAnacondaStatus(char * currentPosition)
{
	int dataLength;
   short val;
   short val1;
   short val2;

	//Parse status message, starting at data length
  	dataLength = currentPosition[0] + (currentPosition[1]<<8);
	currentPosition += 2;

   localAnacondastatus.messageTime = convertBytesToLong (&currentPosition[0], 0);
   currentPosition += 4;


   if (ANACONDA_PACKED_LSB_FIRST == 0)
   {
      localAnacondastatus.lcdaStatus = convertBytesToLong (&currentPosition[0], 0);
		currentPosition += 4;
	   localAnacondastatus.lcdbStatus = convertBytesToLong (&currentPosition[0], 0);
   	currentPosition += 4;

   	localAnacondastatus.lcdaCurrMode = currentPosition[0];
   	localAnacondastatus.lcdaReqMode = currentPosition[1];
      localAnacondastatus.lcdbCurrMode = currentPosition[2];
      localAnacondastatus.lcdbReqMode = currentPosition[3];
      currentPosition += 4;

      localAnacondastatus.manifoldHeatTargTemp = currentPosition[0] + (currentPosition[1]<<8);
 	   localAnacondastatus.manifoldHeatActTemp = currentPosition[2] + (currentPosition[3]<<8);
	   currentPosition += 4;

      localAnacondastatus.pitotHeatTargTemp = currentPosition[0] + (currentPosition[1]<<8);
  	   localAnacondastatus.pitotHeatActTemp = currentPosition[2] + (currentPosition[3]<<8);
	   currentPosition += 4;

      val = currentPosition[0] + (currentPosition[1]<<8);
	   localAnacondastatus.supplyVoltage = val/4096.0*102.4;
      val = currentPosition[2] + (currentPosition[3]<<8);
	   localAnacondastatus.supplyCurrent = val/4096.0*4.096;
      currentPosition += 4;


   }
   else //if (ANACONDA_PACKED_LSB_FIRST == 1)
   {
      localAnacondastatus.lcdaStatus = convertBytesToLong (&currentPosition[0], 0);
		currentPosition += 4;
	   localAnacondastatus.lcdbStatus = convertBytesToLong (&currentPosition[0], 0);
   	currentPosition += 4;

   	localAnacondastatus.lcdaCurrMode = currentPosition[3];
   	localAnacondastatus.lcdaReqMode = currentPosition[2];
      localAnacondastatus.lcdbCurrMode = currentPosition[1];
      localAnacondastatus.lcdbReqMode = currentPosition[0];
      currentPosition += 4;

      localAnacondastatus.manifoldHeatTargTemp = currentPosition[2] + (currentPosition[3]<<8);
  	   localAnacondastatus.manifoldHeatActTemp = currentPosition[0] + (currentPosition[1]<<8);
	   currentPosition += 4;

      localAnacondastatus.pitotHeatTargTemp = currentPosition[2] + (currentPosition[3]<<8);
  	   localAnacondastatus.pitotHeatActTemp = currentPosition[0] + (currentPosition[1]<<8);
	   currentPosition += 4;

      val = currentPosition[2] + (currentPosition[3]<<8);
	   localAnacondastatus.supplyVoltage = val/4095.0*102.4;
      val = currentPosition[0] + (currentPosition[1]<<8);
	   localAnacondastatus.supplyCurrent = val/4095.0*4.096;
      currentPosition += 4;

   }

   //Only update voltage if voltage is over 1 V.  If reporting under 1 volt, the sensor shouldn't be operational
   if (localAnacondastatus.supplyVoltage > 1)
   {
   	voltageEighths = (char)(0x00FF&((int)(localAnacondastatus.supplyVoltage*8)));
	   voltageUpdated = localAnacondastatus.messageTime;
   }

   //Update anaconda sample usage once search mode is received and confirmed
   if (localAnacondastatus.lcdaCurrMode == localAnacondastatus.lcdbCurrMode)
   {
    	if (localAnacondastatus.lcdaCurrMode == 0x15)
      {
      	if (getAnacondaSampleUsed (0) == 0)
		      setAnacondaSampleUsed (0, 1);
      }
      if (localAnacondastatus.lcdaCurrMode == 0x16)
      {
      	if (getAnacondaSampleUsed (1) == 0)
		      setAnacondaSampleUsed (1, 1);
      }
      if (localAnacondastatus.lcdaCurrMode == 0x17)
      {
      	if (getAnacondaSampleUsed (2) == 0)
		      setAnacondaSampleUsed (2, 1);
      }
      if (localAnacondastatus.lcdaCurrMode == 0x18)
      {
      	if (getAnacondaSampleUsed (3) == 0)
		      setAnacondaSampleUsed (3, 1);
      }
   }


   val1 = currentPosition[0] + (currentPosition[1]<<8);
   val2 = currentPosition[2] + (currentPosition[3]<<8);
   localAnacondastatus.pitotValveStatus = ((val2 & (0x01<<15)) != 0x0000);
	localAnacondastatus.manifoldHeatStatus = ((val2 & (0x01<<14)) != 0x0000);
	localAnacondastatus.pitotHeatStatus = ((val2 & (0x01<<13)) != 0x0000);
	localAnacondastatus.srsPumpSupply = ((val2 & (0x01<<12)) != 0x0000);
	localAnacondastatus.srsValveSupply = ((val2 & (0x01<<11)) != 0x0000);
	localAnacondastatus.srsValveDrive = ((val2 & (0x01<<10)) != 0x0000);
   localAnacondastatus.systemInformation = ((val2 & (0x01<<9)) != 0x0000);
   localAnacondastatus.lcdbInformation = ((val2 & (0x01<<8)) != 0x0000);
   localAnacondastatus.lcdaInformation = ((val2 & (0x01<<7)) != 0x0000);
   localAnacondastatus.lcdbTestFile = ((val2 & (0x01<<6)) != 0x0000);
   localAnacondastatus.lcdaTestFile = ((val2 & (0x01<<5)) != 0x0000);
   localAnacondastatus.delete4 = ((val2 & (0x01<<4)) != 0x0000);
   localAnacondastatus.delete3 = ((val2 & (0x01<<3)) != 0x0000);
   localAnacondastatus.delete2 = ((val2 & (0x01<<2)) != 0x0000);
   localAnacondastatus.delete1 = ((val2 & (0x01<<1)) != 0x0000);
	localAnacondastatus.lcdbHSpectrum = ((val2 & (0x01)) != 0x0000);

	localAnacondastatus.lcdaHSpectrum = ((val1 & (0x01<<15)) != 0x0000);
   localAnacondastatus.lcdbGSpectrum = ((val1 & (0x01<<14)) != 0x0000);
   localAnacondastatus.lcdaGSpectrum = ((val1 & (0x01<<13)) != 0x0000);
   localAnacondastatus.lcdbBusy = ((val1 & (0x01<<12)) != 0x0000);
   localAnacondastatus.lcdaBusy = ((val1 & (0x01<<11)) != 0x0000);
   localAnacondastatus.lcdbReset = ((val1 & (0x01<<10)) != 0x0000);
   localAnacondastatus.lcdaReset = ((val1 & (0x01<<9)) != 0x0000);
	localAnacondastatus.rs232External = ((val1 & (0x01<<8)) != 0x0000);
	localAnacondastatus.podCommsValid = ((val1 & (0x01<<7)) != 0x0000);
	localAnacondastatus.externalCommsValid = ((val1 & (0x01<<6)) != 0x0000);
	localAnacondastatus.sdSocketEmpty = ((val1 & (0x01<<5)) != 0x0000);
	localAnacondastatus.srs4 = ((val1 & (0x01<<4)) != 0x0000);
	localAnacondastatus.srs3 = ((val1 & (0x01<<3)) != 0x0000);
	localAnacondastatus.srs2 = ((val1 & (0x01<<2)) != 0x0000);
	localAnacondastatus.srs1 = ((val1 & (0x01<<1)) != 0x0000);
	localAnacondastatus.debugStatus = ((val1 & (0x01)) != 0x0000);
}

void parseAnacondaLCDReport (char* currentPosition, AnacondaReport* report)
{
  	int dataLength;
   short val;
   int i;

	//Parse report message, starting at data length
  	dataLength = currentPosition[0] + (currentPosition[1]<<8);
	currentPosition += 2;

   report->messageTime = convertBytesToLong (&currentPosition[0], 0);
   currentPosition += 4;


   if (ANACONDA_PACKED_LSB_FIRST == 0)
   {
      report->pressure = currentPosition[2] + (currentPosition[3]<<8);
 	   report->temperature = currentPosition[0] + (currentPosition[1]<<8);
	   currentPosition += 4;
   }
   else //if (ANACONDA_PACKED_LSB_FIRST == 1)
   {
      report->pressure = currentPosition[0] + (currentPosition[1]<<8);
 	   report->temperature = currentPosition[2] + (currentPosition[3]<<8);
	   currentPosition += 4;
   }

	report->numAgents = currentPosition[0];
   currentPosition ++;

   for (i = 0; i < report->numAgents; i ++)
   {
    	report->agents[i].agentID = currentPosition[0];
      report->agents[i].bars = currentPosition[1];
      currentPosition += 2;
   }
}

void parseAnacondaLCDAReport(char* currentPosition)
{
 	parseAnacondaLCDReport (currentPosition, &localAnacondaReportA);
}

void parseAnacondaLCDBReport(char* currentPosition)
{
	parseAnacondaLCDReport (currentPosition, &localAnacondaReportB);
}

void parseAnacondaText (char* currentPosition)
{
   int dataLength;
   short val;
   int i;

	//Parse text message, starting at data length
  	dataLength = currentPosition[0] + (currentPosition[1]<<8);
	currentPosition += 2;

   localAnacondaText.messageTime = convertBytesToLong (&currentPosition[0], 0);
   currentPosition += 4;

	localAnacondaText.messageLength = dataLength - 4;
   if (localAnacondaText.messageLength > ANACONDA_MAX_TEXT_SIZE)
   {
   	localAnacondaText.messageTrunc = 1;
     	localAnacondaText.messageLength = ANACONDA_MAX_TEXT_SIZE;
   }
   else
		localAnacondaText.messageTrunc = 0;

   for (i = 0; i < localAnacondaText.messageLength; i ++)
   {
   	localAnacondaText.text[i] = currentPosition[0];
      currentPosition ++;
   }

   if (localAnacondaText.messageTrunc == 1)
      localAnacondaText.text[ANACONDA_MAX_TEXT_SIZE-1] = 0;
}

void parseAnacondaSpectra (char* currentPosition)
{
	int dataLength;
   int i;

   currentPosition += 1;

  	dataLength = currentPosition[0] + (currentPosition[1]<<8);
	currentPosition += 2;

   if (dataLength != 516)
   {
		localAnacondaSpectra.messageTime = -1;
      return;
   }

   localAnacondaSpectra.messageTime = convertBytesToLong (&currentPosition[0], 0);
   currentPosition += 4;

   for (i = 0; i < 512; i ++)
   {
    	localAnacondaSpectra.nibbleData[i] = currentPosition[0];
      currentPosition += 1;
   }
}

int buildAnacondaStatusMessage(char * udpBuffer, int *bodyLength)
{
	int index;
   char * bufferPtr;
	long rtc;
   char i;
   char usedVal;

   AnacondaStatus nStatus;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nStatus, &localAnacondastatus, sizeof(localAnacondastatus));

   //set byte order
   nStatus.messageTime = htonl (nStatus.messageTime);
   nStatus.lcdaStatus = htonl (nStatus.lcdaStatus);
   nStatus.lcdbStatus = htonl (nStatus.lcdbStatus);
   nStatus.manifoldHeatTargTemp = htons (nStatus.manifoldHeatTargTemp);
   nStatus.manifoldHeatActTemp = htons (nStatus.manifoldHeatActTemp);
   nStatus.pitotHeatTargTemp = htons (nStatus.pitotHeatTargTemp);
   nStatus.pitotHeatActTemp = htons (nStatus.pitotHeatActTemp);
   //TODO: Verify this works

   nStatus.supplyVoltage = flipFloat (nStatus.supplyVoltage);
   nStatus.supplyCurrent = flipFloat (nStatus.supplyCurrent);


   //copy in data
   memcpy(bufferPtr,&nStatus,sizeof(nStatus));
   bufferPtr+=(int)sizeof(nStatus);
   index+=(int)sizeof(nStatus);

   //copy in usage data
  	for (i = 0; i < 4; i ++)
   {
		usedVal = getAnacondaSampleUsed (i);
	   memcpy(bufferPtr,&usedVal,sizeof(usedVal));
	   bufferPtr+=(int)sizeof(usedVal);
	   index+=(int)sizeof(usedVal);
   }

   *bodyLength = index;
	return ANACONDA_STATUS_TYPE;
}

void buildAnacondaLCDReportMessage(char* udpBuffer, int* bodyLength, AnacondaReport* report)
{
   int index;
   char * bufferPtr;
	long rtc;
   int i;

   AnacondaReport nReport;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nReport, report, sizeof(*report));


   //set byte order
   nReport.messageTime = htonl (nReport.messageTime);
   nReport.pressure = htons (nReport.pressure);
   nReport.temperature = htons (nReport.temperature);

   //copy in data, individiually to manage agent data pairs
   memcpy(bufferPtr,&nReport.messageTime,sizeof(nReport.messageTime));
   bufferPtr+=(int)sizeof(nReport.messageTime);
   index+=(int)sizeof(nReport.messageTime);

   memcpy(bufferPtr,&nReport.pressure,sizeof(nReport.pressure));
   bufferPtr+=(int)sizeof(nReport.pressure);
   index+=(int)sizeof(nReport.pressure);

   memcpy(bufferPtr,&nReport.temperature,sizeof(nReport.temperature));
   bufferPtr+=(int)sizeof(nReport.temperature);
   index+=(int)sizeof(nReport.temperature);

   memcpy(bufferPtr,&nReport.numAgents,sizeof(nReport.numAgents));
   bufferPtr+=(int)sizeof(nReport.numAgents);
   index+=(int)sizeof(nReport.numAgents);

   for (i = 0; i < nReport.numAgents; i ++)
   {
   	memcpy(bufferPtr,&nReport.agents[i].agentID,sizeof(nReport.agents[i].agentID));
	   bufferPtr+=(int)sizeof(nReport.agents[i].agentID);
	   index+=(int)sizeof(nReport.agents[i].agentID);

   	memcpy(bufferPtr,&nReport.agents[i].bars,sizeof(nReport.agents[i].bars));
	   bufferPtr+=(int)sizeof(nReport.agents[i].bars);
	   index+=(int)sizeof(nReport.agents[i].bars);
   }

   *bodyLength = index;
}

int buildAnacondaLCDAReportMessage(char* udpBuffer, int* bodyLength)
{
	buildAnacondaLCDReportMessage (udpBuffer, bodyLength, &localAnacondaReportA);
   return ANACONDA_LCDA_REPORT;
}

int buildAnacondaLCDBReportMessage(char* udpBuffer, int* bodyLength)
{
	buildAnacondaLCDReportMessage (udpBuffer, bodyLength, &localAnacondaReportB);
   return ANACONDA_LCDB_REPORT;
}

int buildAnacondaTextMessage (char* udpBuffer, int* bodyLength)
{
   int index;
   char * bufferPtr;
	long rtc;

   AnacondaText nText;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nText, &localAnacondaText, sizeof(localAnacondaText));

   //set byte order
   nText.messageTime = htonl (nText.messageTime);

   //copy in data, individiually to manage text
   memcpy(bufferPtr,&nText.messageTime,sizeof(nText.messageTime));
   bufferPtr+=(int)sizeof(nText.messageTime);
   index+=(int)sizeof(nText.messageTime);

   memcpy(bufferPtr,&nText.messageLength,sizeof(nText.messageLength));
   bufferPtr+=(int)sizeof(nText.messageLength);
   index+=(int)sizeof(nText.messageLength);

   memcpy(bufferPtr,&nText.messageTrunc,sizeof(nText.messageTrunc));
   bufferPtr+=(int)sizeof(nText.messageTrunc);
   index+=(int)sizeof(nText.messageTrunc);

	memcpy(bufferPtr,&nText.text, nText.messageLength);
   bufferPtr+=(int)nText.messageLength;
   index+=(int)nText.messageLength;

   *bodyLength = index;
   return ANACONDA_TEXT;

}

int buildAnacondaSpectraMessage (char* udpBuffer, int* bodyLength, int messageType)
{
   int index;
   char * bufferPtr;
	long rtc;

   AnacondaSpectra nSpectra;

   index=0;
   bufferPtr = udpBuffer;

   //add a message timestamp
   rtc = htonl(get1970RTC());
   memcpy(bufferPtr,&rtc,sizeof(rtc));
   bufferPtr+=(int)sizeof(rtc);
   index+=(int)sizeof(rtc);

   //Copy in body,
	//copy locally
   memcpy(&nSpectra, &localAnacondaSpectra, sizeof(localAnacondaSpectra));

   //set byte order
   nSpectra.messageTime = htonl (nSpectra.messageTime);

   //copy in data
   memcpy(bufferPtr,&nSpectra,sizeof(nSpectra));
   bufferPtr+=(int)sizeof(nSpectra);
   index+=(int)sizeof(nSpectra);


   *bodyLength = index;
   return messageType;
	//return ANACONDA_SPECTRA_TYPE;
}


int setGps(char* ser_cmd, long timestamp, long lat, long lon, int alt)
{
	int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x10;	//GPS Data 0x10
   ser_cmd[3] = 0x0F;
   ser_cmd[4] = 0x00;	//15 bytes length
   ser_cmd[5] = 0x01;	//valid
   convertLongToBytes (timestamp, &ser_cmd[6], 0);
   convertLongToBytes (lat, &ser_cmd[10], 0);
   convertLongToBytes (lon, &ser_cmd[14], 0);
   convertIntToBytes (alt, &ser_cmd[18], 0);

   checksum = 0;
	for (itr = 2; itr < 20; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[20] = checksum;
   return 21;
}

int modeIdle(char *ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x14;	//Mode Idle
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int modeSearch(char *ser_cmd, char opt)
{
   int itr;
   char checksum;
   char usedCheck;

   usedCheck = getAnacondaSampleUsed (opt-1);
   if (usedCheck == 0)
   {
   	ser_cmd[0] = 0x7E;
	   ser_cmd[1] = 0x7E;

	   if (opt < 1)
	      opt = 1;
	   if (opt > 4)
	      opt = 4;
	   ser_cmd[2] = 0x15 + opt-1; //Mode Search index
	   ser_cmd[3] = 0x00;
	   ser_cmd[4] = 0x00;   //0 bytes length

	   checksum = 0;
	   for (itr = 2; itr < 5; itr ++)
	   {
	      checksum += ser_cmd[itr];
	   }
	   ser_cmd[5] = checksum;
      return 6;
   }
   else
   {
	   return -1;
   }
}

int modeStandby(char *ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x19;	//Mode Standby
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int modeAirframe( char *ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x1A;	//Mode Airframe
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int modePod (char *ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x1B;	//Mode Pod
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int actionDelete(char *ser_cmd, char opt)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   if (opt < 1)
	   opt = 1;
   if (opt > 4)
   	opt = 4;
   ser_cmd[2] = 0x1F + opt-1;	//Action Delete index
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int setDateTime(char* ser_cmd, long timestamp)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x28;	//Set DateTime
   ser_cmd[3] = 0x04;
   ser_cmd[4] = 0x00;	//4 bytes length
   convertLongToBytes (timestamp, &ser_cmd[5], 0);

   checksum = 0;
	for (itr = 2; itr < 9; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[9] = checksum;
   return 10;
}

int setServoOpenLimit(char* ser_cmd, int val)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x29;	//Set Servo Open Limit
   ser_cmd[3] = 0x02;
   ser_cmd[4] = 0x00;	//2 bytes length
   convertIntToBytes (val, &ser_cmd[5], 0);

   checksum = 0;
	for (itr = 2; itr < 7; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[7] = checksum;
   return 8;
}

int setServoClosedLimit(char* ser_cmd, int val)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x2A;	//Set Servo Closed Limit
   ser_cmd[3] = 0x02;
   ser_cmd[4] = 0x00;	//2 bytes length
   convertIntToBytes (val, &ser_cmd[5], 0);

   checksum = 0;
	for (itr = 2; itr < 7; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[7] = checksum;
   return 8;
}

int setManifoldHeaterTemp(char* ser_cmd, int val)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x2B;	//Set Manifold Heater Temperature
   ser_cmd[3] = 0x02;
   ser_cmd[4] = 0x00;	//2 bytes length
   convertIntToBytes (val, &ser_cmd[5], 0);

   checksum = 0;
	for (itr = 2; itr < 7; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[7] = checksum;
   return 8;
}

int setPitotHeaterTemp(char* ser_cmd, int val)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x2C;	//Set Pitot Heater Temperature
   ser_cmd[3] = 0x02;
   ser_cmd[4] = 0x00;	//2 bytes length
   convertIntToBytes (val, &ser_cmd[5], 0);

   checksum = 0;
	for (itr = 2; itr < 7; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[7] = checksum;
   return 8;
}

int saveSettings(char *ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x2D;	//Action Save Settings
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int setDebugOpt (char* ser_cmd, int debugOpt)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = debugOpt;	//Debugging option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int rawCommand(char* ser_cmd,char msgType,int computeSize, char *rawDataBytes)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = msgType;
   convertIntToBytes (computeSize, &ser_cmd[3], 0);
   memcpy (&ser_cmd[5], rawDataBytes, computeSize);

   checksum = 0;
	for (itr = 2; itr < 5+computeSize; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5+computeSize] = checksum;
   return (6+computeSize);
}

int sendLcdaG(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x01;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;
}

int sendLcdaH(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x02;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int sendLcdbG(char* ser_cmd)
{
	int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x03;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int sendLcdbH(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x04;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int stopLcdaG(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x05;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int stopLcdaH(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x06;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int stopLcdbG(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x07;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int stopLcdbH(char* ser_cmd)
{
   int itr;
   char checksum;

	ser_cmd[0] = 0x7E;
   ser_cmd[1] = 0x7E;

   ser_cmd[2] = 0x08;	//Spectra option
   ser_cmd[3] = 0x00;
   ser_cmd[4] = 0x00;	//0 bytes length

   checksum = 0;
	for (itr = 2; itr < 5; itr ++)
   {
		checksum += ser_cmd[itr];
   }
   ser_cmd[5] = checksum;
   return 6;

}

int do_anaconda_command (char* incomingUcMessageBody, char* ser_cmd, char* LogMessage)
{
	long computeTimestamp;
	long computeLat;
  	long computeLon;
   int computeAlt;
   int computeSize;

   //Type of command is in first byte, then any necessary data follows in the body data field
 	switch((int)incomingUcMessageBody[0])
   {
      case ANACONDA_SET_GPS:
         computeTimestamp = convertBytesToLong (&incomingUcMessageBody[1], 1);
         computeLat = convertBytesToLong (&incomingUcMessageBody[5], 1);
         computeLon = convertBytesToLong (&incomingUcMessageBody[9], 1);
         computeAlt = convertBytesToInt (&incomingUcMessageBody[13], 1);

         sprintf(LogMessage,"ANACONDA_SET_GPS received: %ld,%ld,%ld,%d\r\n\0",computeTimestamp, computeLat, computeLon, computeAlt);

         return setGps(ser_cmd,computeTimestamp, computeLat, computeLon, computeAlt);
         break;
      case ANACONDA_MODE_IDLE:
        	sprintf(LogMessage,"ANACONDA_MODE_IDLE received.\r\n\0");
        	return modeIdle(ser_cmd);
        	break;
      case ANACONDA_MODE_SEARCH:
        	sprintf(LogMessage,"ANACONDA_MODE_SEARCH received: %x\r\n\0",incomingUcMessageBody[1]);
        	return modeSearch(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case ANACONDA_MODE_STANDBY:
        	sprintf(LogMessage,"ANACONDA_MODE_STANDBY received.\r\n\0");
        	return modeStandby(ser_cmd);
        	break;
      case ANACONDA_MODE_AIRFRAME:
        	sprintf(LogMessage,"ANACONDA_MODE_AIRFRAME received.\r\n\0");
       	return modeAirframe(ser_cmd);
        	break;
      case ANACONDA_MODE_POD:
        	sprintf(LogMessage,"ANACONDA_MODE_POD received.\r\n\0");
        	return modePod(ser_cmd);
        	break;
      case ANACONDA_ACTION_DELETE:
        	sprintf(LogMessage,"ANACONDA_ACTION_DELETE received: %x\r\n\0",incomingUcMessageBody[1]);
        	return actionDelete(ser_cmd,incomingUcMessageBody[1]);
      	break;
      case ANACONDA_SET_DATETIME:
	      computeTimestamp = convertBytesToLong (&incomingUcMessageBody[1], 1);

        	sprintf(LogMessage,"ANACONDA_SET_DATETIME received: %ld\r\n\0",computeTimestamp);
        	return setDateTime(ser_cmd,computeTimestamp);
      	break;
		case ANACONDA_SET_SERVO_OPENLIMIT:
	      computeAlt = convertBytesToInt (&incomingUcMessageBody[1], 1);

        	sprintf(LogMessage,"ANACONDA_SET_SERVO_OPENLIMIT received: %d\r\n\0",computeAlt);
        	return setServoOpenLimit(ser_cmd,computeAlt);
      	break;
		case ANACONDA_SET_SERVO_CLOSEDLIMIT:
	      computeAlt = convertBytesToInt (&incomingUcMessageBody[1], 1);

        	sprintf(LogMessage,"ANACONDA_SET_SERVO_CLOSEDLIMIT received: %d\r\n\0",computeAlt);
        	return setServoClosedLimit(ser_cmd,computeAlt);
      	break;
		case ANACONDA_SET_MANIFOLD_HEATERTEMP:
         computeAlt = convertBytesToInt (&incomingUcMessageBody[1], 1);

        	sprintf(LogMessage,"ANACONDA_SET_MANIFOLD_HEATERTEMP received: %d\r\n\0",computeAlt);
        	return setManifoldHeaterTemp(ser_cmd,computeAlt);
      	break;
  		case ANACONDA_SET_PITOT_HEATERTEMP:
         computeAlt = convertBytesToInt (&incomingUcMessageBody[1], 1);

        	sprintf(LogMessage,"ANACONDA_SET_PITOT_HEATERTEMP received: %d\r\n\0",computeAlt);
        	return setPitotHeaterTemp(ser_cmd,computeAlt);
      	break;
      case ANACONDA_ACTION_SAVE_SETTINGS:
        	sprintf(LogMessage,"ANACONDA_ACTION_SAVE_SETTINGS received.\r\n\0");
        	return saveSettings(ser_cmd);
        	break;
		case ANACONDA_DEBUG_OPT:
        	sprintf(LogMessage,"ANACONDA_DEBUG_OPT received: %x\r\n\0",incomingUcMessageBody[1]);
        	return setDebugOpt(ser_cmd,incomingUcMessageBody[1]);
      case ANACONDA_SEND_LCDA_G:
      	sprintf (LogMessage, "ANACONDA_SEND_LCDA_G received.\r\n\0");
         return sendLcdaG(ser_cmd);
      case ANACONDA_SEND_LCDA_H:
      	sprintf (LogMessage, "ANACONDA_SEND_LCDA_H received.\r\n\0");
         return sendLcdaH(ser_cmd);
      case ANACONDA_SEND_LCDB_G:
      	sprintf (LogMessage, "ANACONDA_SEND_LCDB_G received.\r\n\0");
         return sendLcdbG(ser_cmd);
      case ANACONDA_SEND_LCDB_H:
      	sprintf (LogMessage, "ANACONDA_SEND_LCDB_H received.\r\n\0");
         return sendLcdbH(ser_cmd);
      case ANACONDA_STOP_LCDA_G:
      	sprintf (LogMessage, "ANACONDA_STOP_LCDA_G received.\r\n\0");
         return stopLcdaG(ser_cmd);
      case ANACONDA_STOP_LCDA_H:
      	sprintf (LogMessage, "ANACONDA_STOP_LCDA_H received.\r\n\0");
         return stopLcdaH(ser_cmd);
      case ANACONDA_STOP_LCDB_G:
      	sprintf (LogMessage, "ANACONDA_STOP_LCDB_G received.\r\n\0");
         return stopLcdbG(ser_cmd);
      case ANACONDA_STOP_LCDB_H:
      	sprintf (LogMessage, "ANACONDA_STOP_LCDB_H received.\r\n\0");
         return stopLcdbH(ser_cmd);
      case ANACONDA_RESET_SAMPLEUSAGE:
       	sprintf(LogMessage,"ANACONDA_RESET_SAMPLEUSAGE received.\r\n\0");
         setAnacondaSampleUsed ((char)-1, (char)0);
         return 0;

//TODO: Verify data bytes get through
      case ANACONDA_RAW:
        	sprintf(LogMessage,"ANACONDA_RAW received: %x\r\n\0",incomingUcMessageBody[1]);

         computeSize = convertBytesToInt (&incomingUcMessageBody[2], 1);
        	return rawCommand(ser_cmd,incomingUcMessageBody[1],computeSize,&incomingUcMessageBody[4]);
		default:
        	sprintf(LogMessage,"ANACONDA_UNKNOWN received: %x\r\n\0",incomingUcMessageBody[0]);
        	printf("Unable to parse ANACONDA UC_COMMAND_TYPE %d\n\0",incomingUcMessageBody[0] );
      }
}