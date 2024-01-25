//Board type
#use "board_type.c"

//Serial read library
#use "cof_read_serial.c"

//UDP Networking library
#use "udp_server.c"

//Message parsing/building library
#use "message_handling.c"

//xD logging library
#use "log_line.c"

//RTC conversions
#use "rtc_commands.c"

#use "flash_handler.c"

#use "servo_messages.c"

#if BIOPOD_TYPE == 0
	#use "anaconda_messages.c"
	#use "c100_messages.c"
#endif

#if BIOPOD_TYPE == 1
	#use "ibac_messages.c"
	#use "bladewerx_messages.c"
   #use "bladewerx_pump_messages.c"
#endif

#use "customFuncs.c"

#use "temp_humidity.c"

#use "dio_pins.c"

#use "temp_humidity_logic.c"

#use "fan_messages.c"

#use "heater_messages.c"


#memmap xmem
#class auto

//Max size for data string read from serial
#define MAX_TOKEN_SIZE_C 128
#define MAX_TOKEN_SIZE_E 526

//Number of data strings to store in ciruclar buffer for processing
#define QUEUE_SIZE 5

#define CINBUFSIZE  127
#define COUTBUFSIZE 255
//#define EINBUFSIZE  1023
#define EINBUFSIZE 2047
//#define EINBUFSIZE  8191
#define EOUTBUFSIZE 255

#if BIOPOD_TYPE == 0
	//IP Address of this rabbit board
	#define LOCAL_IP  "192.168.100.52"

   //Parameters for Serial C
	#define SERCBAUD 57600
   //Parameters for Serial E
	#define SEREBAUD 115200

   #define REMOTE_PORT 1313

#else //BIOPOD_TYPE == 1
	#define LOCAL_IP  "192.168.100.53"

   //Parameters for Serial C
	#define SERCBAUD 115200
   //Parameters for Serial E
	#define SEREBAUD 57600

   //Port number the PC uses to connect to us
   #define REMOTE_PORT 1314
#endif


//Port number to communicate with PC from
#define LOCAL_PORT 1315

//IP Address of the PC connecting to us
#define REMOTE_IP  "192.168.100.255"


//Set IP Configuration
#define TCPCONFIG 1
#define _PRIMARY_STATIC_IP  LOCAL_IP
#define _PRIMARY_NETMASK    "255.255.255.0"

//Number of UDP connections to allocate memory for
#define MAX_OUTGOING_SOCKET_BUFFERS 1
//1 for incoming
#define MAX_UDP_SOCKET_BUFFERS (1+1+MAX_OUTGOING_SOCKET_BUFFERS)

//If 1, log data to file.  Otherwise, don't.
#define LOG_TO_FILE 1
#define STDIO_ENABLE_LONG_STRINGS

//Delay for sending heartbeat messages, seconds
#define HEARTBEAT_PERIOD 5

//Delay for requesting status from quiet sensors, seconds
#define VERSION_PERIOD 5

extern void (*c_rtsoff)();
extern void (*c_rtson)();



//Index in the queueIndexes array that relates to where processing is occurring for: receiving messages, logging messages, and parsing messages
enum msg_queue_index{
	msgQueueIndex=0,
   logQueueIndex,
   networkTransmitIndex,
   NUMBER_INDEXES
};

//Circular buffer holding all queued messages received from a single serial connection.  Knows what messages are being processed and how.
typedef struct {
   char msgQueue[QUEUE_SIZE][MAX_TOKEN_SIZE_C];
 	int msgLengthIfNecessary [QUEUE_SIZE];
	unsigned long msgTimeStamps[QUEUE_SIZE];
	int queueIndexes[NUMBER_INDEXES];
} msgContainerC;
//Circular buffer holding all queued messages received from a single serial connection.  Knows what messages are being processed and how.
typedef struct {
   char msgQueue[QUEUE_SIZE][MAX_TOKEN_SIZE_E];
 	int msgLengthIfNecessary [QUEUE_SIZE];
	unsigned long msgTimeStamps[QUEUE_SIZE];
	int queueIndexes[NUMBER_INDEXES];
} msgContainerE;

//For the index (relating to a value of msg_queue_index), of the specified message container, increment 1 (indicating we are ready to process the next message
void incrementIndexE(msgContainerE *serialMessages, int index);
void incrementIndexC(msgContainerC *serialMessages, int index);


void  timerb_isr();
void  timera_isr();

#use "dcrtcp.lib"


#if BIOPOD_TYPE == 1
scofunc void sendBladewerxCmd (char* ser_cmd_send, char* cMsgBuffer, char* serialCErrHeader)
{
	int line_sizeR;

 	while (bladewerxRtsSet == 1)
   {
		waitfor (IntervalMs (1));
   }

	//Set RTS high until command response received
   (*c_rtson)();
   bladewerxRtsSet = 1;

   //All commands here are 1 bytes
   wfd cof_serCwrite(ser_cmd_send, 1);

   //Read response for commands that provide it
   if (ser_cmd_send[0] == 0x20)
   {
      //read 22 bytes and buffer to parse
      memset (cMsgBuffer, 0, 32);
      wfd line_sizeR = cof_read_serial_C_fixed_immediate(cMsgBuffer,21,5000);
      if (line_sizeR == 21)
         cMsgBuffered = (char)line_sizeR;
      else
      {
      	waitfor (IntervalMs (1000));
         (*c_rtsoff)();
         bladewerxRtsSet = 0;
         wfd log_msg(serialCErrHeader, "Did not receive enough bytes on serial C after version cmd, likely not connected.\r\n\0", 0);
      }
   }
   else if (ser_cmd_send[0] == 0x21)
   {
      //read 2 bytes and buffer to parse
      wfd line_sizeR = cof_read_serial_C_fixed_immediate(cMsgBuffer,2,5000);
      if (line_sizeR > 0)
         cMsgBuffered = (char)line_sizeR;
      else
      {
         (*c_rtsoff)();
         bladewerxRtsSet = 0;
         wfd log_msg(serialCErrHeader, "Did not receive enough bytes on serial C after getADC cmd, likely not connected.\r\n\0", 0);
      }
   }
   else
   {
      (*c_rtsoff)();
      bladewerxRtsSet = 0;
   }
}
#endif


//Main
void main()
{
	//Variable definitions
	msgContainerC serialCMessages;
   msgContainerE serialEMessages;
   char incomingUdpbuffer [256];
   char udp_log_message [256];
   #if BIOPOD_TYPE == 0
	   char anacondaConvertedMessage [600];
   #endif
   char ser_cmd[128];
   char ser_cmd_versions[128];
   char versions_log_message [128];
   char udpParseResponseC[128];
   char udpParseResponseE[600];
   char udpForcedResponse[128];
   char failedHeartbeat[64];
	char failedTransmit[64];
   char udpHeartBeat[64];
   char serialCRecvHeader [64];
   char serialERecvHeader [64];
   char serialCErrHeader [32];
   char serialEErrHeader [32];
   char udpForcedErrHeader [32];
   char cMsgBuffer [32];
   char thMsg [64];
   #if BIOPOD_TYPE == 1
		char bladewerxConvertedMessage [8];
   #endif
   int udpForcedResponseLen;
	int i, line_size, line_sizeR;
   int retval;
   int msgLenC, msgLenE, msgLenBeat;
   int idx;
   int printPowerMessage;
   int channel;	//Used for keyboard input debugging
   char sensorType;
   int bytesReceived;
	float temperature;
   float newTemperature;
   long temperatureUpdated;
	float humidity;
   float newHumidity;
	long humidityUpdated;
   char thStatus;
   #if BIOPOD_TYPE == 0
   	int anacondaSendSize;
	   int anacondaCheckItr;
	   char anacondaLogItr;
   #endif
   char breakDelay;
   char stopInitLoop;
   long rtc;
   char firstHeartbeat;


   // Set up vector to ISR for PWM control
  	SetVectIntern(0x0B, timerb_isr);
   WrPortI(TBCR, &TBCRShadow, 0x01);	// clock timer B with perclk/2
													// set interrupt level to 1
	WrPortI(TBM1R, NULL, 0x00);
	WrPortI(TBL1R, NULL, 0x00);			// set initial match
   WrPortI(TBCSR, &TBCSRShadow, 0x03);	// enable timer B and B1 match interrupts*/


   // Initialize the controller
	brdInit();




/*   serCflowcontrolOn();
   (*c_rtsoff)();
   customSerModeOpt2 ();
   serCflowcontrolOff();
   (*c_rtsoff)();



   (*c_rtsoff)();
	bladewerxRtsSet = 0;

   printf ("Delaying load...\r\n\0");

	breakDelay = 0;
   while (breakDelay == 0)
   {
    	costate delayCostate always_on
      {
       	waitfor (DelaySec (1));
         breakDelay = 1;
      }
   }

   (*c_rtson)();
	bladewerxRtsSet = 1;

   printf ("Delay while RTS set...\r\n\0");
  	breakDelay = 0;
   while (breakDelay == 0)
   {
    	costate delayCostate always_on
      {
       	waitfor (DelaySec (1));
         breakDelay = 1;
      }
   }


   (*c_rtsoff)();
	bladewerxRtsSet = 0;        */

   //TODO: Only enable used outputs
   //Using all digital outputs
   digOutConfig(0xFFFF);
   digHoutConfig(0xFF);

   //TODO: Verify this will make it work on new board!!!
//   _sysIsSoftReset ();
	retrieveFlashData();
   lastLogCommand = getActualLogState();

   if (getServoOpenPWM(0) < 0)
   	setServoOpenPWM(0, 0);
   if (getServoOpenPWM(0) > 100)
   	setServoOpenPWM(0, 100);
   if (getServoClosedPWM(0) < 0)
   	setServoClosedPWM(0, 0);
   if (getServoClosedPWM(0) > 100)
   	setServoClosedPWM(0, 100);
   if (getServoOpenPWM(1) < 0)
   	setServoOpenPWM(1, 0);
   if (getServoOpenPWM(1) > 100)
   	setServoOpenPWM(1, 100);
   if (getServoClosedPWM(1) < 0)
   	setServoClosedPWM(1, 0);
   if (getServoClosedPWM(1) > 100)
   	setServoClosedPWM(1, 100);

   pwmPeriod = PWM_PREF_PERIOD;
   pwmHilo = 1;
   setPWMClosed (0);
   setPWMClosed (1);

   unMounted = 1;
   firstHeartbeat = 1;

   idx = 0;
	temperature = 0;
	humidity = 0;
   thStatus = 0;
   temperatureUpdated = 0;
   humidityUpdated = 0;

   //Open serial ports
   serCopen(SERCBAUD);
   serEopen(SEREBAUD);

   //Setup message types, depends on which board we're operating on
   #if BIOPOD_TYPE == 0
      //Write an initialization log message to file
   	log_msg_header ("BioPod type 0:\r\n Serial C: C100\r\n Serial E: Anaconda\r\n\r\n\0");

      //Port C is 3 wire, Port F is unused, Port E is 3 wire
	   serMode (0);
   #else //BIOPOD_TYPE == 1
      log_msg_header ("BioPod type 1:\r\n Serial C: Bladewerx\r\n Serial E: IBAC\r\n\r\n\0");

      //Do this first so RTS is off when initialized
      serCflowcontrolOn();
	   (*c_rtsoff) ();
	   serCflowcontrolOff();
      bladewerxRtsSet = 0;

      //Port C is 5 wire with CTS/RTS on Port F, Port E is 3 wire
      customSerModeOpt2 ();	//Use this to prevent RTS from being set by default

		(*c_rtsoff) ();
   #endif

   cMsgBuffered = 0;

   printPowerMessage = 0;
//TODO: Uncomment this line!
   if (lastLogCommand == 1)
   {
     	log_new ();
      printPowerMessage = 1;
   }

  	memset(serialCMessages.msgQueue,0,sizeof(serialCMessages.msgQueue));
   memset(serialCMessages.queueIndexes,0,sizeof(serialCMessages.queueIndexes));
	memset(serialCMessages.msgTimeStamps,0,sizeof(serialCMessages.msgTimeStamps));
	memset(serialCMessages.msgLengthIfNecessary,0,sizeof(serialCMessages.msgLengthIfNecessary));
  	memset(serialEMessages.msgQueue,0,sizeof(serialEMessages.msgQueue));
   memset(serialEMessages.queueIndexes,0,sizeof(serialEMessages.queueIndexes));
	memset(serialEMessages.msgTimeStamps,0,sizeof(serialEMessages.msgTimeStamps));

   strcpy(failedHeartbeat,"Failed to send heartbeat on UDP\r\n\0");
   strcpy(failedTransmit,"Failed to send message on UDP\r\n\0");
   strcpy(serialCRecvHeader, "Serial port C message received: \0");
   strcpy(serialERecvHeader, "Serial port E message received: \0");
   strcpy(serialCErrHeader, "Serial port C error: \0");
   strcpy(serialEErrHeader, "Serial port E error: \0");
	strcpy(udpForcedErrHeader, "UDP command response error: \0");

   //Initialize udp network
   udp_init(LOCAL_PORT);
   printf ("Connected!\n\0");

   tempHumidityControls (24.5, 0, thMsg);

   #if BIOPOD_TYPE == 1
	   stopInitLoop = 0;
	   while (stopInitLoop == 0)
	   {
	      costate initLoop always_on
	      {
	         if(ser_cmd!=NULL)
	         {
	            ibac_sleep (ser_cmd);
	            wfd cof_serEwrite(ser_cmd, strlen(ser_cmd));

               toggleBladewerxPower(1);
	            stopInitLoop = 1;
	         }
	      }
	   }
   #endif


	while (1)
   {
   	//perform low-level networking tasks
    	tcp_tick(NULL);

      //Serial C Read Costate
	   costate readSerialC always_on
      {

      	if (printPowerMessage == 1)
         {
      		sprintf (udp_log_message, "Power cycled on.\r\n\0");
		      wfd log_msg (NULL, udp_log_message, 0);
            printPowerMessage = 0;
         }

	      //Clear room for the new message
	      memset(serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]],0,sizeof(serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]]));

        	//Wait until we read in a full message from serial port C, terminated with endline characters for C100, single bytes for Bladewerx
         #if BIOPOD_TYPE == 1
	         if (cMsgBuffered > 0)
	         {
	            //Use a message that was buffered from serial C in another process (responses from Bladewerx)
	            line_size = cMsgBuffered;
	            memcpy (serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]], cMsgBuffer, line_size);
	            serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]][line_size] = 0;
	            serialCMessages.msgLengthIfNecessary[serialCMessages.queueIndexes[msgQueueIndex]] = line_size;
	            cMsgBuffered = 0;
	         }
	         else
	         {
	            wfd line_size = cof_read_serial_C_fixed(serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]],1);
	            serialCMessages.msgLengthIfNecessary[serialCMessages.queueIndexes[msgQueueIndex]] = line_size;
	         }

            //This was the signal that a message has been buffered somewhere else and we should get it immediately
	         if (line_size == -13)
   	      	abort;
         #else //BIOPOD_TYPE == 0
           	 wfd line_size = cof_read_serial_C(serialCMessages.msgQueue[serialCMessages.queueIndexes[msgQueueIndex]],MAX_TOKEN_SIZE_C-1);
         #endif

         if(line_size>0)
	      {
         	//A non-null message was received...

            //Update timestamp of message recv
            serialCMessages.msgTimeStamps[serialCMessages.queueIndexes[msgQueueIndex]] = get1970RTC();

            //Next message will be received after the current one in the circular buffer
	         incrementIndexC(&serialCMessages, msgQueueIndex);

            //Scroll through all other message task indicies and be sure we're not about to catch up to one.  If so, nudge the slow one ahead.
	         for(i=0;i< NUMBER_INDEXES;i++)
	         {
               if(i!=msgQueueIndex&&serialCMessages.queueIndexes[msgQueueIndex]==serialCMessages.queueIndexes[i])
	            {
               	//Task 'i' is too slow - push it ahead one step and lose the chance to process that message.
	               incrementIndexC(&serialCMessages, i);
                  printf ("Skipped a message from serialC for processing type: %d\n", i);
	            }
	         }

	      }

	   }

      //Serial E Read Costate
	   costate readSerialE always_on
      {
         //Clear room for the new message
			memset(serialEMessages.msgQueue[serialEMessages.queueIndexes[msgQueueIndex]],0,sizeof(serialEMessages.msgQueue[serialEMessages.queueIndexes[msgQueueIndex]]));

         //Wait until we read in a fully message from serial port E, terminated with endline characters for IBAC, size specified in message for Anaconda
         #if BIOPOD_TYPE == 0
         	wfd line_size = cof_read_serial_E_anaconda(serialEMessages.msgQueue[serialEMessages.queueIndexes[msgQueueIndex]],MAX_TOKEN_SIZE_E-1);
            serialEMessages.msgLengthIfNecessary[serialEMessages.queueIndexes[msgQueueIndex]] = line_size;
         #else
         	wfd line_size = cof_read_serial_E(serialEMessages.msgQueue[serialEMessages.queueIndexes[msgQueueIndex]],MAX_TOKEN_SIZE_E-1);
         #endif

         if (line_size == -1313)
         {
				wfd log_msg (NULL, "Bad checksum from Anaconda, ignoring message and resetting buffer...\r\n\0", 0);
         }
         else if (line_size == -1314)
         {
				wfd log_msg (NULL, "Possible buffer overflow from Anaconda, resetting buffer...\r\n\0", 0);
         }
         if(line_size>0)
	      {
         	//A non-null message was received...

            //Update timestamp of message recv
            serialEMessages.msgTimeStamps[serialEMessages.queueIndexes[msgQueueIndex]] = get1970RTC();

            //Next message will be received after the current one in the circular buffer
            incrementIndexE(&serialEMessages, msgQueueIndex);

            //Scroll through all other message task indicies and be sure we're not about to catch up to one.  If so, nudge the slow one ahead.
            for(i=0;i< NUMBER_INDEXES;i++)
	         {
               if(i!=msgQueueIndex&&serialEMessages.queueIndexes[msgQueueIndex]==serialEMessages.queueIndexes[i])
	            {
	               //Task 'i' is too slow - push it ahead one step and lose the chance to process that message.
                  incrementIndexE(&serialEMessages, i);
                  printf ("Skipped a message from serialE for processing type: %d\n", i);
	            }
	         }
	      }
	   }


      //Logging Costate from serialC
      costate logSerialC always_on
      {
      	//Wait until a message is read in before we try to log it
      	waitfor(serialCMessages.queueIndexes[logQueueIndex]!=serialCMessages.queueIndexes[msgQueueIndex]);

         #if BIOPOD_TYPE == 0
	        	//Log message as normal
	         wfd log_msg(serialCRecvHeader, serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]], 0);
         #else
         	if (serialCMessages.msgLengthIfNecessary[serialCMessages.queueIndexes[logQueueIndex]] == 21)
	         {
	            //Bladewerx version response message
	            sprintf (bladewerxConvertedMessage, "%s,%d,%d,%d,%d\r\n\0", serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]],
	                                 serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][strlen(serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]])+1],
	                                 serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][strlen(serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]])+2],
	                                 serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][strlen(serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]])+3],
	                                 serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][strlen(serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]])+4]);
	            wfd log_msg(serialCRecvHeader, bladewerxConvertedMessage, 0);
	         }
	         else if (serialCMessages.msgLengthIfNecessary[serialCMessages.queueIndexes[logQueueIndex]] == 2)
	         {
	            //Bladewerx adc response message
	            sprintf (bladewerxConvertedMessage, "%d\r\n\0", (serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][0] << 8 | serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][1]));
	            wfd log_msg(serialCRecvHeader, bladewerxConvertedMessage, 0);
	         }
	         else
	         {
	            //Log message as converted hex for single detection bin messages
	            sprintf (bladewerxConvertedMessage, "%d\r\n\0", serialCMessages.msgQueue[serialCMessages.queueIndexes[logQueueIndex]][0] );
	            wfd log_msg(serialCRecvHeader, bladewerxConvertedMessage, 0);
	         }
         #endif

         //Next iteration will process the next available message in the circular buffer
         incrementIndexC(&serialCMessages, logQueueIndex);
      }

      //Logging Costate from serialE
      costate logSerialE always_on
      {
      	//Wait until a message is read in before we try to log it
      	waitfor(serialEMessages.queueIndexes[logQueueIndex]!=serialEMessages.queueIndexes[msgQueueIndex]);

         //Log message
         #if BIOPOD_TYPE == 0
				//memset (anacondaConvertedMessage, 0, sizeof(anacondaConvertedMessage));
            anacondaConvertedMessage [0] = 0;
            for (anacondaLogItr = 0; anacondaLogItr < serialEMessages.msgLengthIfNecessary[serialEMessages.queueIndexes[logQueueIndex]]; anacondaLogItr ++)
            {
					sprintf(&anacondaConvertedMessage[strlen(anacondaConvertedMessage)], "%x,\0", serialEMessages.msgQueue[serialEMessages.queueIndexes[logQueueIndex]][anacondaLogItr]);
               if (strlen(anacondaConvertedMessage) > 590)
	               break;
            }
            sprintf(&anacondaConvertedMessage[strlen(anacondaConvertedMessage)], "\r\n\0");
         	wfd log_msg(serialERecvHeader, anacondaConvertedMessage, 0);
         #else
            wfd log_msg(serialERecvHeader, serialEMessages.msgQueue[serialEMessages.queueIndexes[logQueueIndex]], 0);
         #endif

         //Next iteration will process the next available message in the circular buffer
         incrementIndexE(&serialEMessages, logQueueIndex);
      }


      //Parse serial C and send UDP Packets
      costate parseSerialC always_on
      {
      	//Wait until a message is read in before we try to read it
      	waitfor(serialCMessages.queueIndexes[networkTransmitIndex]!=serialCMessages.queueIndexes[msgQueueIndex]);

         //Choose the sensor that sent the message (based on pod type).  Then parse the message and generate a message to send over the network
         #if BIOPOD_TYPE == 0
             wfd msgLenC = parse_and_build_c100(serialCMessages.msgQueue[serialCMessages.queueIndexes[networkTransmitIndex]],
	         	serialCMessages.msgTimeStamps[serialCMessages.queueIndexes[networkTransmitIndex]],udpParseResponseC);
	      #else // BIOPOD_TYPE == 1
             wfd msgLenC = parse_and_build_bladewerx(serialCMessages.msgQueue[serialCMessages.queueIndexes[networkTransmitIndex]],
	         	serialCMessages.msgTimeStamps[serialCMessages.queueIndexes[networkTransmitIndex]],
               serialCMessages.msgLengthIfNecessary[serialCMessages.queueIndexes[networkTransmitIndex]],
               udpParseResponseC);
			#endif

         if (msgLenC > 0)
         {
         	//Send the formatted message to UDP based on the serial message
	         wfd retval=send_udp_message(udpParseResponseC, msgLenC);

            //Log if error, otherwise assume it was sent ok
	         if(retval<0)
	         {
	           wfd log_msg(serialCErrHeader, failedTransmit, 0);
	         }
         }

         //Next iteration will process the next available message in the circular buffer
         incrementIndexC(&serialCMessages, networkTransmitIndex);
      }

      //Parse serial E and send UDP Packets
      costate parseSerialE always_on
      {
         //Wait until a message is read in before we try to read it
         waitfor(serialEMessages.queueIndexes[networkTransmitIndex]!=serialEMessages.queueIndexes[msgQueueIndex]);

         //Choose the sensor that sent the message (based on pod type).  Then parse the message and generate a message to send over the network
         #if BIOPOD_TYPE == 0
         	wfd msgLenE = parse_and_build_anaconda(serialEMessages.msgQueue[serialEMessages.queueIndexes[networkTransmitIndex]],
					serialEMessages.msgTimeStamps[serialEMessages.queueIndexes[networkTransmitIndex]],udpParseResponseE);
	      #else // BIOPOD_TYPE == 1
            wfd msgLenE = parse_and_build_ibac(serialEMessages.msgQueue[serialEMessages.queueIndexes[networkTransmitIndex]],
	         	serialEMessages.msgTimeStamps[serialEMessages.queueIndexes[networkTransmitIndex]],udpParseResponseE);
         #endif

         if (msgLenE > 0)
         {
            //Send the formatted message to UDP based on the serial message
	         wfd retval=send_udp_message(udpParseResponseE, msgLenE);

            //Log if error, otherwise assume it was sent ok
	         if(retval<0)
	         {
	           wfd log_msg(serialEErrHeader, failedTransmit, 0);
	         }
         }

         //Next iteration will process the next available message in the circular buffer
         incrementIndexE(&serialEMessages, networkTransmitIndex);
       }


      //Receive and parse UPD Packets
      costate receive_udp always_on
      {
      	wfd bytesReceived = udp_recv_packet (incomingUdpbuffer, sizeof(incomingUdpbuffer));
         sensorType = verifyCommand (incomingUdpbuffer);
         udp_log_message[0] = 0;
         udpForcedResponseLen = 0;


         if (sensorType == SENSOR_SERVO)
         {
            wfd interpret_servo_command(incomingUdpbuffer, bytesReceived, udpForcedResponse, &udpForcedResponseLen, BIOPOD_TYPE, udp_log_message);

            //We're not going to response anymore.  Servo will be handled in heartbeat message
         }
         else if (sensorType == SENSOR_FAN)
         {
            wfd interpret_fan_command(incomingUdpbuffer, bytesReceived, udp_log_message);
      	}
         #if BIOPOD_TYPE == 0
         else if (sensorType == SENSOR_HEATER)
         {
            wfd interpret_heater_command(incomingUdpbuffer, bytesReceived, udp_log_message);
      	}
         #endif
         else if (sensorType == RABBIT_BOARD)
         {
         	//Handle a board message, like setting the RTC.
            wfd interpret_pod_command(incomingUdpbuffer, bytesReceived, udp_log_message);
      	}
         #if BIOPOD_TYPE == 0
	         else if (sensorType == SENSOR_C100)
	         {
	            //C100 is on serial port C of biopod 0
	            wfd interpret_c100_command(incomingUdpbuffer, bytesReceived, ser_cmd, udp_log_message, udpForcedResponse, &udpForcedResponseLen);

	            if(ser_cmd!=NULL)
	               wfd cof_serCwrite(ser_cmd, strlen(ser_cmd));


	            if (udpForcedResponseLen > 0)
	            {
	                //Send the formatted message to UDP based on the serial message
	                wfd retval=send_udp_message(udpForcedResponse, udpForcedResponseLen);

	                //Log if error, otherwise assume it was sent ok
	                if(retval<0)
	                {
	                  wfd log_msg(udpForcedErrHeader, failedTransmit, 0);
	                }
	            }

	         }
            else if (sensorType == SENSOR_ANACONDA)
         	{
	            //Anaconda is on serial port E of biopod 0
	            wfd anacondaSendSize = interpret_anaconda_command(incomingUdpbuffer, bytesReceived, ser_cmd, udp_log_message);

	            if(ser_cmd!=NULL && anacondaSendSize > 0)
	            {
	               wfd cof_serEwrite(ser_cmd, anacondaSendSize);

	               //verify command sent
	               printf ("Anaconda command:\r\n    \0");
	               for (anacondaCheckItr = 0; anacondaCheckItr < anacondaSendSize; anacondaCheckItr ++)
	               {
	                  printf ("%x ", ser_cmd[anacondaCheckItr]);
	               }
	               printf("\r\n\0");

	            }
	         }
         #else //if BIOPOD_TYPE == 0
	         else if (sensorType == SENSOR_IBAC && BIOPOD_TYPE == 1)
	         {
	            //IBAC is on serial port E of biopod 1
	            wfd interpret_ibac_command(incomingUdpbuffer, bytesReceived, ser_cmd, udp_log_message);

	            if(ser_cmd!=NULL)
	               wfd cof_serEwrite(ser_cmd, strlen(ser_cmd));
	         }
	         else if (sensorType == SENSOR_BLADEWERX && BIOPOD_TYPE == 1)
	         {
	            //Bladewerx is on serial port C of biopod 1
	            wfd interpret_bladewerx_command(incomingUdpbuffer, bytesReceived, ser_cmd, udp_log_message);

	            if(ser_cmd!=NULL)
	            {
	               wfd sendBladewerxCmd (ser_cmd, cMsgBuffer, serialCErrHeader);
	            }

	         }
	         else if (sensorType == SENSOR_BLADEWERX_PUMP && BIOPOD_TYPE == 1)
	         {
	            //Bladewerx pump is only on biopod 1
	            wfd interpret_bladewerx_pump_command(incomingUdpbuffer, bytesReceived, udpForcedResponse, &udpForcedResponseLen, udp_log_message);

	            if (udpForcedResponseLen > 0)
	            {
	                //Send the formatted message to UDP based on the serial message
	                wfd retval=send_udp_message(udpForcedResponse, udpForcedResponseLen);

	                //Log if error, otherwise assume it was sent ok
	                if(retval<0)
	                {
	                  wfd log_msg(udpForcedErrHeader, failedTransmit, 0);
	                }
	            }
	         }

	         else if (sensorType == SENSOR_BRIDGEPORT && BIOPOD_TYPE == 1)
	         {
	            //Bridgeport messages are logged to biopod 1
	            wfd save_bridgeport_message(incomingUdpbuffer, bytesReceived, udpForcedResponse, &udpForcedResponseLen, udp_log_message);
	            if (udpForcedResponseLen > 0)
	            {
	               //Send the formatted message to UDP based on the serial message
	               wfd retval=send_udp_message(udpForcedResponse, udpForcedResponseLen);

	               //Log if error, otherwise assume it was sent ok
	               if(retval<0)
	               {
	                 wfd log_msg(udpForcedErrHeader, failedTransmit, 0);
	               }
	            }
	         }
         #endif
         else
         {
          	sprintf (udp_log_message, "Unexpected command for sensor #%d: %d\r\n\0", sensorType, incomingUdpbuffer[3]);
         }

         if(strlen(udp_log_message)>0)
         {
				wfd log_msg(NULL, udp_log_message, 0);
         }
       }


       //Send a version request for select sensors periodically to make sure we're still connected
       costate versions always_on
       {
		 	waitfor(DelaySec(VERSION_PERIOD));

        	//C100 is on serial port C of biopod 0
         #if BIOPOD_TYPE == 0
         	verifyC100 (ser_cmd_versions, versions_log_message);
	         if(ser_cmd_versions !=NULL)
            {
	         	wfd cof_serCwrite(ser_cmd_versions, strlen(ser_cmd_versions));
	         }

            if(strlen(versions_log_message)>0)
	         {
					wfd log_msg(NULL, versions_log_message, 0);
	         }
         //Bladewerx is on serial port C of biopod 1
         #else //if (BIOPOD_TYPE == 1)
         	verifyBladewerx (ser_cmd_versions, versions_log_message);
	         if(ser_cmd_versions !=NULL)
            {
					wfd sendBladewerxCmd (ser_cmd_versions, cMsgBuffer, serialCErrHeader);
	         }

            if(strlen(versions_log_message)>0)
	         {
					wfd log_msg(NULL, versions_log_message, 0);
	         }
         #endif
       }

      //Send a heartbeat message
      costate heartbeat always_on
      {
      	if (firstHeartbeat == 1)
         {
	         //Delay a little the first time, just to stagger the heartbeat off the versions period
   	    	waitfor(DelaySec(1));
            thStatus = EE03_status();
	         waitfor(DelaySec(1));
            firstHeartbeat = 0;
         }
         else
         {
      		//Delay
	         waitfor(DelaySec(HEARTBEAT_PERIOD-1));
	         thStatus = EE03_status();
	         waitfor(DelaySec(1));
         }


         //Do temperature humidity receive
         if (thStatus == 0)
         {
         	newTemperature = Temp_read();
	         newHumidity = RH_read(); // read humidity

            if (newTemperature > -300)
            {
            	temperature = newTemperature;
               temperatureUpdated = get1970RTC();
            }
            if (newHumidity > -1)
            {
            	humidity = newHumidity;
               humidityUpdated = get1970RTC();
         	}

            sprintf(thMsg, "Temp = %f\r\n\0", temperature);
            wfd log_msg (NULL, thMsg, 0);
         	sprintf(thMsg, "Humidity = %f\r\n\0", humidity);
            wfd log_msg (NULL, thMsg, 0);

            tempHumidityControls (temperature, humidity, thMsg);
	         if (strlen (thMsg) > 0)
   	      	wfd log_msg (NULL, thMsg, 0);

         }
         else
         {
         	sprintf(thMsg, "TH Failed, status = %d\r\n\0", thStatus);
            wfd log_msg (NULL, thMsg, 0);
         }




         //Check card state
         if (isCardInserted() == 0)
 	     	{
         	//Card isn't in.
            logErrCode = CARD_REMOVED_ERR_CODE;
      	}
         else if (logErrCode == CARD_REMOVED_ERR_CODE)
         {
          	logErrCode = 0;
         }
         if (logErrCode != 0)
         {
          	setActualLogState(0);
         }


         //Generate the heartbeat message
         msgLenBeat = build_heartbeat(udpHeartBeat,
         							BIOPOD_TYPE,
                              lastLogCommand,
                              getActualLogState(),
                              serialCMessages.msgTimeStamps[(serialCMessages.queueIndexes[msgQueueIndex]+QUEUE_SIZE-1)%QUEUE_SIZE],
                              serialEMessages.msgTimeStamps[(serialEMessages.queueIndexes[msgQueueIndex]+QUEUE_SIZE-1)%QUEUE_SIZE],
                              voltageEighths, voltageUpdated,
                              temperature, temperatureUpdated,
                              humidity, humidityUpdated,
                              servoToggledOpen[0], getLastServoDuty(0), fanToggledOn, heaterToggledOn,
										servoManualOverride[0], fanManualOverride, heaterManualOverride,
                              (char)getTemperatureLimitServo(), (char)getHumidityLimitServo(), (char)getTemperatureLimitFan(),
                              (char)getHumidityLimitFan(), (char)getTemperatureLimitHeater(), (char)getHumidityLimitHeater(),
                              logErrCode
                              );

			//Send the heartbeat message
         wfd retval=send_udp_message(udpHeartBeat, msgLenBeat);

         //Log if error, otherwise assume it was sent ok
         if(retval<0)
         {
            wfd log_msg(NULL, failedHeartbeat, 0);
         }
       }

       //TODO: Remove this costate before release.  Use it for debugging
       costate keyboard always_on
       {
       	if (kbhit() != 0)
         {
       		channel = getchar();

            if (channel == 'q' || channel == 'Q')
	         {
	            log_fs_shutdown ();
	         }
            if (channel == 'n' || channel == 'N')
	         {
	            log_new ();
	         }
            if (channel == 'u' || channel == 'U')
	         {
	            incPWM (0, 10);
               servoManualOverride[0] = 1;
               printf ("servo at: %d\r\n", pwmPulseWidth);
	         }
            if (channel == 'd' || channel == 'D')
	         {
	            decPWM (0, 10);
               servoManualOverride[0] = 1;
               printf ("servo at: %d\r\n", pwmPulseWidth);
	         }
 			#if BIOPOD_TYPE == 1
            if (channel == 'h')
            {
            	(*c_rtson)();
				   bladewerxRtsSet = 1;
         	}
            if (channel == 'l')
            {
               (*c_rtsoff)();
				   bladewerxRtsSet = 0;
            }
 			#endif


            /*else if (channel == 'n' || channel == 'N')
	         {
	            log_new ();
	         } */
         }
      }
   }

  	WrPortI(TBCSR, &TBCSRShadow, 0x00);	// disable timer B and its interrupt

   log_fs_shutdown();
}

//For the index (relating to a value of msg_queue_index), of the specified message container, increment 1 (indicating we are ready to process the next message
void incrementIndexC(msgContainerC *serialMessages, int index)
{
	(*serialMessages).queueIndexes[index]=((*serialMessages).queueIndexes[index]+1)%QUEUE_SIZE;
}
//For the index (relating to a value of msg_queue_index), of the specified message container, increment 1 (indicating we are ready to process the next message
void incrementIndexE(msgContainerE *serialMessages, int index)
{
	(*serialMessages).queueIndexes[index]=((*serialMessages).queueIndexes[index]+1)%QUEUE_SIZE;
}

#asm
timerb_isr::
	push  af                   ; save registers
	push  bc
	push  hl
	push  de

	ioi   ld a, (TBCSR)        ; load B1, B2 interrupt flags (clears flag)

	ld		hl, (isrCount)
	inc	hl                   ; increment counter
	ld		(isrCount), hl

	ld		a, (pwmHilo)
	or		a                    ; set z if zero, nz if non-zero
	jr    nz, high             ; if 1, jump to high section

low:                          ; otherwise, we're low and wait for period count
	ld    de, (pwmPeriod)
	ld    a, d                 ; do 16 bit compare (in two steps)
	cp    h                    ; between count (in hl) and period
	jr    nz, done
	ld    a, e
	cp    l
	jr    nz, done             ; set the line high when period count reached

//c   WrPortE(WR_BANK1, NULL, 0x00);  //set the line high
	xor   a                    ; value = 0x00 (inverted logic!)
	ioe   ld (0x2001), a       ; write to port

	ld    (isrCount), a        ; reset count
	ld    (isrCount + 1), a
	ld    (pwmState), a        ; clear pwmState to set both pins high
	inc   a                    ; set hilo to high = 0x00 + 1
	ld    (pwmHilo), a
	jr    done

high:                         ; we're high, so wait for pulse width count
//Servo 1, Index 0, Pin 15
	ld    a, (pwmState)
	ld    c, a                 ; set c to value of pwmState
	ld    de, (pwmPulseWidth)
	ld    a, d                 ; do 16 bit compare (in two steps)
	cp    h                    ; between count (in hl) and pwidth
	jr    nz, high2
	ld    a, e
	cp    l
	jr    nz, high2            ; set the line low when pulse width count reached

//c  WrPortE(WR_BANK1, NULL, 0x80); //set the line low

	ld    a, 0x80              ; OR flag with 0x80 to turn off 15
	or    c
	ld    c, a

high2:
//Servo 2, Index 1, Pin 14
	ld    de, (pwmPulseWidth + 2)
	ld    a, d                 ; do 16 bit compare (in two steps)
	cp    h                    ; between count (in hl) and pwidth
	jr    nz, setbit
	ld    a, e
	cp    l
	jr    nz, setbit           ; set the line low when pulse width count reached

	ld    a, 0x40              ; OR flag with 0x40 to turn off 14
	or    c
	ld    c, a

setbit:
	ld    a, c
	ld    (pwmState), a        ; save state to variable
	ioe   ld (0x2001), a       ; write to port

	cp    0xC0
	jr    nz, done             ; if both pins not yet low, keep going

	xor   a                    ; else set hilo to low (0)
	ld    (pwmHilo), a

done:
	xor   a
	ioi   ld (TBM1R), a        ; set up next B1 match (at timer=0000h)
	ioi   ld (TBL1R), a        ; NOTE:  you _need_ to reload the match
	                           ;  register after every interrupt!
	pop   de
	pop   hl                   ; restore registers
	pop   bc
	pop   af

	ipres                      ; restore interrupts
	ret                        ; return
#endasm

