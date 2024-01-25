/*** BeginHeader  */
void network_task(void);
void network_init(void);

 /*** EndHeader */

#use "WebVariables.c"
#use "NetworkDefs.c"
#use "ConfigFile.c"
#use "SatMessages.c"
#use "SatModemDefs.c"
#use "dcrtcp.lib"



enum
{
   WAITING,
   OPENING,
   RUNNING,
   CLOSING,
	CLOSED
};


 // This is used to drive the state machine

typedef struct
{
	int 			state;
	udp_Socket	sock;
	longword		timer;
	long			pkts_sent;
   long			pkst_recd;
   int 			next_state;
   long			buffer;
} SocketState;

// Defines the states of the state machine

// This holds the state for each multicast socket
SocketState state[MAX_UDP_SOCKETS+1];
udp_Socket statussock;


// Initialize the state structure
void network_init(void)
{
	char ipstr[20];
	int readlength;
   int error = 0;
   int i=0;
   socket_count = 0;
   timeset = 0;

   readlength = sizeof(SocketParams);
	error = ReadFile("sockparams" , &sockparams, &readlength);
#ifdef ENABLE_PRINT
   printf("Reading Socket Parameters\n");
   printf("Read File: %d\n", error);
   printf("Bytes Read: %d\n", readlength);
   printf("Valid: %d\n", sockparams.valid);
   printf("Reconnect Wait Time: %d S\n", sockparams.reconnect_wait);
#endif


   if(readlength!= sizeof(SocketParams) || sockparams.valid != FILE_VALID)
   {
      printf("Load Socket Params is invalid - default values being used\n");
     	sockparams.valid = FILE_VALID;
		sockparams.reconnect_wait = DEFAULT_WAIT;
      sockparams.statusintervalms = 5000;

      error = SaveFile("sockparams",&sockparams, sizeof(SocketParams));
#ifdef ENABLE_PRINT
      printf("Saving Default Socket Parameters\n");
   	printf("Save File: %d\n", error);
   	printf("Valid: %d\n", sockparams.valid);
	   printf("Reconnect Wait Time: %d S\n", sockparams.reconnect_wait);
#endif
   };

   readlength = sizeof(SocketConfiguration)*(MAX_UDP_SOCKETS+1);

	error = ReadFile("sockconfig" , &sockconfig, &readlength);
#ifdef ENABLE_PRINT
   printf("Reading Socket Configuration\n");
   printf("Read File: %d\n", error);
   printf("Bytes Read: %d\n", readlength);
#endif



   if(readlength== sizeof(SocketConfiguration)*(MAX_UDP_SOCKETS+1))
   {
   	memset(state, 0, sizeof(state));

	   for(i=0; i<(MAX_UDP_SOCKETS+1); i++)
	   {
	      if(sockconfig[i].valid ==  FILE_VALID)
	      {
         	state[i].buffer = xalloc(MY_BUFFER_SIZE);
	         state[i].state = OPENING;
	         state[i].pkts_sent = 0;
	         state[i].pkst_recd = 0;
	         state[i].next_state = OPENING;
	#ifdef ENABLE_PRINT
	         printf("Entry %d Valid:\n", i);
	         printf("Destination Number: %d\n", sockconfig[i].destnum);
	         printf("IP Address: %s\n", inet_ntoa(ipstr,sockconfig[i].ip));
	         printf("Port: %d\n", sockconfig[i].port);
	         printf("Interface: %d\n", sockconfig[i].iface);
            printf("Remote: %d\n", sockconfig[0].remote==1?"Yes":"No");
	#endif
	         socket_count++;
	      }
	      else
	      {
	         state[i].state = CLOSED;
	         state[i].pkts_sent = 0;
	         state[i].pkst_recd = 0;
	         state[i].next_state = CLOSED;
	      }
	   }
   }


	if(readlength!= sizeof(SocketConfiguration)*(MAX_UDP_SOCKETS+1) || socket_count == 0)
   {
      printf("Socket Configuration is invalid - default values being used\n");
      sockconfig[0].valid =  FILE_VALID;
      sockconfig[0].destnum =  -1;
      sockconfig[0].ip = aton(DEFAULT_IP);
		sockconfig[0].port = DEFAULT_PORT_OUT;
      sockconfig[0].iface = IF_ETH0;
      sockconfig[0].remote = 1;
      sockconfig[0].enablestatus = 0;
      sockconfig[MAX_UDP_SOCKETS].valid =  FILE_VALID;
      sockconfig[MAX_UDP_SOCKETS].destnum =  -1;
      sockconfig[MAX_UDP_SOCKETS].ip = aton(DEFAULT_IP);
		sockconfig[MAX_UDP_SOCKETS].port = DEFAULT_PORT;
      sockconfig[MAX_UDP_SOCKETS].iface = IF_ETH0;
      sockconfig[MAX_UDP_SOCKETS].remote = 1;
      sockconfig[MAX_UDP_SOCKETS].enablestatus = 0;


      for(i=1; i<(MAX_UDP_SOCKETS); i++)
	   {
	      sockconfig[i].valid =  0;
	      sockconfig[i].destnum =  -1;
	      sockconfig[i].ip = aton(DEFAULT_IP);
	      sockconfig[i].port = DEFAULT_PORT;
	      sockconfig[i].iface = IF_ETH0;
         sockconfig[i].remote = 1;
         sockconfig[i].enablestatus = 0;
	   }

      socket_count = 1;
      memset(state, 0, sizeof(state));
      state[0].state = OPENING;
      state[0].pkts_sent = 0;
      state[0].pkst_recd = 0;
      state[0].next_state = OPENING;
      state[MAX_UDP_SOCKETS].state = OPENING;
      state[MAX_UDP_SOCKETS].pkts_sent = 0;
      state[MAX_UDP_SOCKETS].pkst_recd = 0;
      state[MAX_UDP_SOCKETS].next_state = OPENING;

      error = SaveFile("sockconfig",&sockconfig, sizeof(SocketConfiguration)*(MAX_UDP_SOCKETS+1));
#ifdef ENABLE_PRINT
      printf("Saving Default Network Configuration\n");
   	printf("Save File: %d\n", error);
   	printf("Valid: %d\n", sockconfig[0].valid );
      printf("IP Address: %s\n", inet_ntoa(ipstr,sockconfig[0].ip));
	   printf("Port: %d\n", sockconfig[0].port);
      printf("Interface: %d\n", sockconfig[0].iface);
      printf("Remote: %d\n", sockconfig[0].remote==1?"Yes":"No");
#endif
   }

   initSatMsgBuffers();

   update_socket_config_webvar();

}

void receiveMessage()
{

}



   static uint32 statust1;

// Do processing on the multicast sockets
void network_tick(void)
{
	static char buffer[BUFFER_SIZE];
   SatMessage msg;
   SatMessage outgoingmsg;
   static uint8 receipt[30], status[20];
	auto int i,j,d;
	auto int retval;
   int result;
	auto longword remip;
	auto word remport;
	auto char ipbuf[16];
   int msgvalid;
   int sendstatus = 0;

   // Process each socket
   msgvalid = 0;

   if(result=getBytesInReceiveBuffer()>= sizeof(SatMessage))
	{
   	if(result=getReceivedMsgFromBuffer(&outgoingmsg)==sizeof(SatMessage))
      {
      	msgvalid = 1;
      }
   }



   //process status socket
   if(MS_TIMER >= statust1)
   {
      statust1 = MS_TIMER + sockparams.statusintervalms;
      getStatusMsg(status);
      sendstatus = 1;
   }


	for (i = MAX_UDP_SOCKETS; i >= 0; i--)
   {


		switch (state[i].state)
		{

         case WAITING:
            if (chk_timeout(state[i].timer))
            {
              state[i].state = state[i].next_state;
            }
         	break;

         case OPENING:
            // Open the sockets
            if(sockconfig[i].valid == FILE_VALID && (i==MAX_UDP_SOCKETS || sockconfig[i].remote!=1))
            {

               retval = 0;
               for(j=i+1; j<=MAX_UDP_SOCKETS; j++)
               {
                  if(i<MAX_UDP_SOCKETS && sockconfig[i].port == sockconfig[j].port
      	            && sockconfig[i].ip == sockconfig[j].ip && sockconfig[j].valid == FILE_VALID && (sockconfig[j].remote != 1 || j==MAX_UDP_SOCKETS))
	               {
	                  state[i].sock = state[j].sock;
	                  retval = 1;
                     break;
	               }

               }
               if(retval==0)
               {
                  retval = udp_extopen(&state[i].sock, sockconfig[i].iface,
                                    sockconfig[i].port, sockconfig[i].ip,
                                    sockconfig[i].port, NULL, state[i].buffer, MY_BUFFER_SIZE|UDP_MODE_TX_BUFFER);
               }

	            if (retval == 0)
	            {
	               // Error opening the socket
	               printf("Error opening socket %d!\n", i);
	               printf("Will Try Reconnect in %d seconds!\n",sockparams.reconnect_wait);
	               state[i].timer = set_timeout(sockparams.reconnect_wait);
	               state[i].state = WAITING;
	               state[i].next_state = OPENING;
	            }
	            else
	            {
	               // Set the interval timer, and advance to the next state
	               state[i].timer = set_timeout(sockparams.reconnect_wait);
	               state[i].state = RUNNING;
	               state[i].next_state = RUNNING;
	            }
            }
            else
            {
            		state[i].state = CLOSED;
            }
	         break;
	      case RUNNING:
			    // Check for any incoming datagrams

            //if(sockconfig[i].valid == FILE_VALID && (i==8 || sockconfig[i].remote!=1))
            //{
            retval = -1;
            if(sockconfig[i].valid == FILE_VALID && (i==MAX_UDP_SOCKETS || (sockconfig[MAX_UDP_SOCKETS].remote!=1 && sockconfig[i].remote!=1)))
            {
            	tcp_tick(&state[i].sock);
	            retval = udp_recvfrom(&state[i].sock, buffer, BUFFER_SIZE, &remip, &remport);
               //printf("UDP Socket %d RECVFROM: %d\n", i,retval);
            }
	         if (retval < -1)
            {
	            // Error reading from the socket
	            printf("Error reading from socket %d!\n", i);
               printf("Will Try Reconnect in %d seconds!\n",sockparams.reconnect_wait);
               sock_close(&state[i].sock);
               state[i].timer = set_timeout(sockparams.reconnect_wait);
	            state[i].state = WAITING;
               state[i].next_state = OPENING;
	         }
	         else if (retval >= 12)
            {
            	printf("*");
	            //Process the message received
	            result = parseMessage(&msg, buffer, 0, retval,1);

               if(result>=0)// && sockconfig[msg.destination-1].valid==FILE_VALID && sockconfig[msg.destination-1].remote==1)
               {

                    result = -1;
                    for(j=0; j<MAX_UDP_SOCKETS; j++)
                    {
                       if(((msg.destination>>j & 0x01) == 1) && sockconfig[j].valid==FILE_VALID && sockconfig[j].remote==1)
                       {
                          result = 1;
                          break;
                       }
                    }


                   if(result==1)
                   {
                   	 msg.replydest = sockconfig[i].destnum;
                      msg.timeinbuff = MS_TIMER;
                      printf("\nPutting Message priority: %d, type: %d, dest: %d into Sat Send Buffer from Socket %d time: %u \n", msg.priority, msg.type, msg.destination, i, msg.timestamp);

                      if(modem_state == MODEM_RUN)
                      {
                      		putSatMsgInBuffer(msg.priority, &msg);
                      }
                      else
                      {

             	       			//Send Response Packet
	                            if(sockconfig[i].destnum != 0xFF)
	                            {
	                               for(j=0; j<MAX_UDP_SOCKETS; j++)
	                                {
	                                  if(((sockconfig[i].destnum>>j & 0x01) == 1) && sockconfig[j].valid==FILE_VALID && sockconfig[j].remote!=1)
	                                  {
	                                     d=(int)pow(2.0,(float)j);
	                                     getPacketReceipt(receipt, &msg, d, -1, -1);
	                                     retval = udp_send(&state[j].sock, &receipt, 30);
	                                     printf("\nSending packet receipt to dest %d!\n",d);
	                                      state[i].pkts_sent += 1;
	                                      if (retval < 0)
	                                      {
	                                         // Error sending from the socket
	                                         printf("Error sending from socket %d!\n", i);
	                                         printf("Will Try Reconnect in %d seconds!\n",sockparams.reconnect_wait);
	                                         sock_close(&state[i].sock);
	                                         state[i].timer = set_timeout(sockparams.reconnect_wait);
	                                         state[i].state = WAITING;
	                                         state[i].next_state = OPENING;
	                                      }
	                                   }
	                                 }
	                              }

	                     }

               	}


               }
               else
               {
                  printf("\n#####################Socket Error: %d\n",sock_error(&state[i].sock, 1));
               }
               //Send Response Packet

	            //inet_ntoa(ipbuf, remip);
	            //printf("Received %d Bytes on socket %d from %s port %u:\n", retval, i, ipbuf,remport);

	         }

            //Send Message(s)
            	if(msgvalid)
               {
               	if(msgvalid && ((outgoingmsg.destination>>i & 0x01) == 1))
                  {
	            		retval = udp_send(&state[i].sock, &outgoingmsg.msg, outgoingmsg.totmsglen);
                     printf("\nSending %d recieved from modem on socket %d!\n",outgoingmsg.totmsglen, i);
	            		state[i].pkts_sent += 1;
             	   	if (retval < 0)
	                  {
	                     // Error sending from the socket
	                     printf("Error sending from socket %d!\n", i);
	                     printf("Will Try Reconnect in %d seconds!\n",sockparams.reconnect_wait);
	                     sock_close(&state[i].sock);
	                     state[i].timer = set_timeout(sockparams.reconnect_wait);
	                     state[i].state = WAITING;
	                     state[i].next_state = OPENING;
	                  }
                  }
               }


               //Send Status
 	          if(sendstatus && sockconfig[i].enablestatus && !sockconfig[i].remote)
              {
                     retval = udp_send(&state[i].sock, &status, 20);
                     //printf("\nSending status packet from modem on socket %d!\n", i);
	            		state[i].pkts_sent += 1;
             	   	if (retval < 0)
	                  {
	                     // Error sending from the socket
	                     printf("Error sending from socket %d!\n", i);
	                     printf("Will Try Reconnect in %d seconds!\n",sockparams.reconnect_wait);
	                     sock_close(&state[i].sock);
	                     state[i].timer = set_timeout(sockparams.reconnect_wait);
	                     state[i].state = WAITING;
	                     state[i].next_state = OPENING;
	                  }

              }


	         break;
	      case CLOSING:
	         sock_close(&state[i].sock);
	         state[i].state = CLOSED;
	         break;
	      case CLOSED:
	         // Do nothing
	         break;
	      default:

		}
	}
}

void network_task(void)
{
   static uint32 t1,t2;
   static uint32 diff;
   t1 = MS_TIMER;
   statust1 = MS_TIMER;
	for(;;)
   {
		network_tick();

      t2=MS_TIMER;
      diff = t2-t1;
      if(diff >= 100)
      {
         t1 = t2;
        update_content();
        http_handler();
      }

      //OSTimeDlyHMSM(0,0,0,20);

	}
}