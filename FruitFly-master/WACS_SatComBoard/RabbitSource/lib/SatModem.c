/*** BeginHeader  */
#use "SatModemDefs.c"


void modem_init();
void modem_task();
void SaveModemParams();


/*** EndHeader */

uint16 latency;
uint32 prevtime;

#use "WebVariables.c"
#use "NetworkDefs.c"
#use "ConfigFile.c"
#use "SatMessages.c"
#use "SatComBoard.c"

//#define BYPASS_MODEM
#define USE_FLOW_CONTROL
#define HEADER_WAIT  50
#define MESSAGE_WAIT 100

#define SERE_RTS_PORT PEDR //use port E data register
#define SERE_RTS_SHADOW PEDRShadow //use port E shadow register
#define SERE_RTS_BIT 2 //output
#define SERE_CTS_PORT PEDR //input
#define SERE_CTS_BIT 3

void modem_init()
{
	char ipstr[20];
	int readlength;
   int error = 0;
   int i=0;
   socket_count = 0;
   prevtime = SEC_TIMER;

   modem_disconnect_time = 0;
   modem_connect_time = 0;

   modem_status = 1;

   readlength = sizeof(ModemParams);
	error = ReadFile("modemparams" , &modem_params, &readlength);
	#ifdef ENABLE_PRINT
	   printf("Reading Modem Parameters\n");
	   printf("Read File: %d\n", error);
	   printf("Bytes Read: %d\n", readlength);
	   printf("Valid: %d\n", modem_params.valid);
	   printf("Dial Number: %s\n", modem_params.dialnumber);
	   printf("Master: %d S\n", modem_params.master);
	   printf("Encryption: %d\n", modem_params.encryption);
	   printf("Iridium: %d\n", modem_params.iridium);
	   printf("Password: %s\n", modem_params.password);
	#endif


   if(readlength!= sizeof(ModemParams) || modem_params.valid != FILE_VALID)
   {
      printf("Load Modem Params is invalid - default values being used\n");
     	modem_params.valid = FILE_VALID;
      printf("Bytes Read: %d\n", readlength);
	   modem_params.valid = FILE_VALID;
	   modem_params.dialnumber[0] = 0;
	   modem_params.master = 1;
	   modem_params.encryption = 0;
	   modem_params.iridium = 1;
	   modem_params.password[0] = 0;

      SaveModemParams();

   }
   update_modem_config_webvar();

   serEopen(MODEMBAUD);		//initialize

#ifdef USE_FLOW_CONTROL
   serEflowcontrolOn();
#endif

	serEwrFlush();				//clear buffers
	serErdFlush();

   SatCom_Power_Modem(1);

   modem_state = MODEM_INIT;

}

void SaveModemParams()
{
	int error = SaveFile("modemparams",&modem_params, sizeof(ModemParams));
	#ifdef ENABLE_PRINT
	      printf("Saving Default Modem Parameters\n");
	      printf("Save File: %d\n", error);
	      printf("Bytes Read: %d\n", readlength);
	      printf("Valid: %d\n", modem_params.valid);
	      printf("Dial Number: %s\n", modem_params.dialnumber);
	      printf("Master: %d S\n", modem_params.master);
	      printf("Encryption: %d\n", modem_params.encryption);
	      printf("Iridium: %d\n", modem_params.iridium);
	      printf("Password: %s\n", modem_params.password);
	#endif

   update_modem_config_webvar();
}


void Run_Tick()
{
	static char receipt[30];
   uint32 tib;
   uint32 currtime;
   uint16 mstime;
   int32 diff1;
   int16 diff2;
   int result, len, cnt;
   int ix = 0;
   char garbage[MAX_DATA_LENGTH];
   int gix=0;
   char buffer[MAX_DATA_LENGTH + HEADER_SIZE];
   uint32 rtime;
   SatMessage message;

   if(Read_Modem_Carrier_Detect()==1)
   {
   	modem_disconnect_time = SEC_TIMER;
   	modem_state = MODEM_CONNECT;
      return;
   }

	result = getSatMsgFromBuffer(0, &message);
   if(result == 0)
   {
     	result = getSatMsgFromBuffer(1, &message);
      if(result == 0)
	   {
	      result = getSatMsgFromBuffer(2, &message);
         if(result == 0)
	      {
	         result = getSatMsgFromBuffer(3, &message);
	      }
	   }
   }

   if(result == sizeof(SatMessage))
   {
    	if(serEwrFree()>=sizeof(SatMessage))
      {

      	printf("Ser Port Bytes Used: %d, bytes free %d\n", serEwrUsed(), serEwrFree());
      	serEwrite(message.msg, message.totmsglen);
         printf("Sending %d bytes via modem\n", message.totmsglen);

         tib =  MS_TIMER - message.timeinbuff;

        //Put Response Packet in Queue
         getPacketReceipt(receipt, &message, message.replydest, latency, tib);
         parseMessage(&message, receipt, 0, 30, 0);
         printf("\nPutting packet receipt to dest %d on queue!\n",message.destination);
         putReceivedMsgInBuffer(&message);
      }
      else
      {
         printf("$$$$$$$Not enough room in modem buffer\n");
      }
   }

   //scroll thorugh buffer until first character is SYNC
   while(serEpeek()!=SYNC && serErdUsed()>0)
   {
      garbage[gix++] = serEgetc();
      garbage[gix]=0;
      if(gix>MAX_DATA_LENGTH-1)
      	gix = 0;

      printf("%c", garbage[gix-1]);

      if(Read_Modem_Carrier_Detect()==1)
	   {
         modem_disconnect_time = SEC_TIMER;
	      modem_state = MODEM_CONNECT;
	      return;
	   }

   }

   if(serEpeek()==SYNC)
   {
     //read header
     cnt = 0;
     while(serErdUsed()<10 && cnt<HEADER_WAIT)
     {
       OSTimeDlyHMSM(0,0,0,10);
       cnt++;
     }
     if(cnt>=HEADER_WAIT)
     {
     		serErdFlush();
         printf("Failed to receive packet header via modem\n");
         return;
     }
     result = serEread(buffer, 10, 200);

     if(result == 10)
     {
     		len = getDataLen(buffer, &ix, 10);
         if(len <= MAX_DATA_LENGTH && len >=0)
         {
         	cnt = 0;
         	while(serErdUsed()<len+2 && cnt < MESSAGE_WAIT)
            {
             	OSTimeDlyHMSM(0,0,0,10);
            }
            if(cnt>=MESSAGE_WAIT)
            {
     				serErdFlush();
               printf("Failed to receive packet body via modem\n");
               return;
            }
            result = serEread(&buffer[10], len+2, 200);
            if(result == len+2)
            {
               parseMessage(&message, buffer, 0, result+10, 0);
               currtime = get1970RTC();
               mstime = MS_TIMER%1000 ;
               diff1 = currtime - message.timestamp;
               diff2 = mstime - message.timestamp_ms;
               //printf("CurrTime: %ld  Message Time: %ld, MSTime: %u  Message MS: %u",currtime,message.timestamp,mstime,message.timestamp_ms);

               diff1 = diff1*1000 + diff2;
               if(diff1 > 0)
               {
               	latency = diff1;
               }
               else
               {
               	latency = 0;
               }



               putReceivedMsgInBuffer(&message);
            }
            else if(result > 0)
            {
               printf("Incomplete Message: %d bytes",result);
            }
         }
     }
   }

}





void Init_Tick()
{
	char sendstring[128];
   char receivebuff[256];
   int i,result = 1;

   OSTimeDlyHMSM(0,0,5,0);

   while(result != 0)
   {
      result = serEread(receivebuff,255,200);
      if(result > 0)
      {
         receivebuff[result] = 0;
         i=0;
         while(receivebuff[i]==0 && i<result)
         {
         	i++;
         }
      	printf("%s",&receivebuff[i]);
      }
   }
	serErdFlush();
   serEwrFlush();				//clear buffers of startup messages

#ifdef USE_FLOW_CONTROL

   serEputs("AT&K3\r");
   result = waitforstring(receivebuff,256,"OK\r",5000);
   printf("%s", receivebuff);
   if(result != 1)
   {
     modem_state = MODEM_RESET;
     return;
   }

#else
   //Ignore DTR
   serEputs("AT&D0\r");
   result = waitforstring(receivebuff,256,"OK\r",5000);
   printf("%s", receivebuff);
   if(result != 1)
   {
     modem_state = MODEM_RESET;
     return;
   }

   serEputs("AT&K0\r");
   result = waitforstring(receivebuff,256,"OK\r",2000);
   printf("%s", receivebuff);
   if(result != 1)
   {
     modem_state = MODEM_RESET;
     return;
   }
#endif

	//Auto Answer
   serEputs("ATS0=1\r");
   result = waitforstring(receivebuff,256,"OK\r",2000);
   printf("%s", receivebuff);
   if(result != 1)
   {
     modem_state = MODEM_RESET;
     return;
   }


   //if(modem_params.updatekey == 1)
   if(0)
   {
   	modem_params.updatekey = 0;

	   //Send Set Key Command
	   sprintf(sendstring,"AT^KE='%s','%s'\r",modem_params.password,modem_params.encryptionkey);
	   serEputs(sendstring);
	   OSTimeDlyHMSM(0,0,3,0);
	   if(!checkforstring("OK"))
	   {
        printf("Turning Encryption off. Key Not Set\n");
        modem_params.encryption = 0;
        SaveModemParams();
	   }
      else
      {
	      printf("Set Key Worked\n") ;
	      SaveModemParams();
      }
   }

   if(0)
   {
   	//set encryption
   	//Send Use Encryption Command
	   sprintf(sendstring,"AT^UE='%s',%d\r",modem_params.password,modem_params.encryption);
	   serEputs(sendstring);
	   OSTimeDlyHMSM(0,0,1,0);
	   if(!checkforstring("OK"))
	   {
        printf("Turning Encryption off. Failed to Set Encryption\n");
        modem_params.encryption = 0;
        SaveModemParams();
	   }
      else
      {
	      if (modem_params.encryption == 1)
	      {
	          printf("Encryption Enabled\n");
	      }
	      else
	      {
	          printf("Encryption Disabled\n");
	      }
      }
   }


   //Turn Radio On
   serEputs("AT*S1\r");
   result = waitforstring(receivebuff,256,"OK\r",2000);
   printf("%s", receivebuff);
   if(result != 1)
   {
     modem_state = MODEM_RESET;
     return;
   }


   //Check SNR
   serEputs("AT+CSQF\r");
   result = waitforstring(receivebuff,256,"OK\r",2000);
   printf("%s", receivebuff);
   if(result==1)
   {
     printf("Signal Quality: %d\n", getSQ(receivebuff));
   }
   else
   {
     modem_state = MODEM_RESET;
     return;
   }

   modem_disconnect_time = SEC_TIMER;
   modem_state = MODEM_CONNECT;

}

void Reset_Tick()
{
	serEclose();
	SatCom_Power_Modem(0);
   if(modem_status == 1)
   {
	   OSTimeDlyHMSM(0,0,3,0);
	   modem_init();
   }
}



void Connect_Tick()
{
      uint32 t1, t2;
      int modemsq = 0;
		char receivebuff[256];
	   int result;
	   char sendstring[32];
	   int count = 0;
	   char connected = 0;
      serEputs("AT+CSQF\r");
      result = waitforstring(receivebuff,256,"OK\r",2000);
   	printf("%s", receivebuff);
      if(result==1)
  	 	{
	      modemsq = getSQ(receivebuff);
	   }
      else
   	{
    		modem_state = MODEM_RESET;
     		return;
   	}


      if(modemsq >= 1)
      {
  	    	if(modem_params.master)
	      {
            //Return if already connected
            if(Read_Modem_Carrier_Detect()==0)
   	      {
	            return;
	         }

	        sprintf(sendstring, "ATD%s\r", modem_params.dialnumber);
           serEputs(sendstring);

	      }

         t1 = t2 = MS_TIMER;

         while(t2-t1 < 60000)
         {
         	t2 = MS_TIMER;

            	result = Read_Modem_Carrier_Detect();
            	printf("Modem Carrier: %d\n",result);

               if(result==1)
	            {
               	OSTimeDlyHMSM(0,0,2,0);
	               modem_state = MODEM_CONNECT;

	            }
	            else
	            {
                 printf("Time to Connect: %u sec\n", (SEC_TIMER-modem_disconnect_time));
                 modem_connect_time = SEC_TIMER;
	              modem_state = MODEM_RUN;
                 break;
	            }

         }
      }
      return;
}




void Modem_Tick()
{
	int count;
   if(modem_status == 0)
   		modem_state = MODEM_RESET;
	switch (modem_state)
   {
		case MODEM_INIT:
#ifdef BYPASS_MODEM
         modem_state = MODEM_RUN;
#else
			Init_Tick();
#endif
			break;
		case MODEM_DELAY:
			break;
     case MODEM_WAIT:
			break;
		case MODEM_RUN:
         if(SEC_TIMER != prevtime)
         {
         	prevtime = SEC_TIMER;
         	printf("Modem Connected for %u seconds.\n",prevtime - modem_connect_time);
         }
         Run_Tick();
			break;
		case MODEM_CONNECT:
			Connect_Tick();
			break;
		case MODEM_RESET:
			Reset_Tick();
			break;
		default:
			modem_state = MODEM_RESET;
			break;
	}
}

void modem_task()
{
	static uint32 t1,t2;
   static uint32 diff;
	while (1)
	{
   	t1 = MS_TIMER;
		Modem_Tick();
      t2=MS_TIMER;
      diff = t2-t1;
      if(diff>1)printf("#Mt: %u\n",diff);
      OSTimeDlyHMSM(0,0,0,25);

	}
}