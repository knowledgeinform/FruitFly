#use "cof_ibac_read_serial.c"
#use "log_line.c"
#use "udp_server.c"


#memmap xmem
#class auto

#define SERDBAUD 57600
#define DINBUFSIZE  255
#define DOUTBUFSIZE 255

#define MAX_TOKEN_SIZE 128
#define QUEUE_SIZE 32

#define LOCAL_IP  "176.16.2.250"
#define LOCAL_PORT 1010
#define REMOTE_IP  "176.16.2.200"
#define REMOTE_PORT 1010

#define TCPCONFIG 1
#define _PRIMARY_STATIC_IP  LOCAL_IP
#define _PRIMARY_NETMASK    "255.255.255.0"

#define MAX_OUTGOING_SOCKET_BUFFERS 1
//1 for incoming
#define MAX_UDP_SOCKET_BUFFERS (1+1+MAX_OUTGOING_SOCKET_BUFFERS)


#define LOG_TO_FILE 1

#define PULSE 5


#use "dcrtcp.lib"

void incrementIndex(int index);

enum msg_queue_index{
	msgQueueIndex=0,
   logQueueIndex,
   networkTransmitIndex,
   NUMBER_INDEXES
};




char msgQueue[QUEUE_SIZE][MAX_TOKEN_SIZE];
unsigned long msgTimeStamps[QUEUE_SIZE];
int queueIndexes[NUMBER_INDEXES];


void main()
{
	int i,line_size;
   char * udp_log_message;

   char ser_cmd[256];
   char udpParseResponse[256+12];
   char udpHeartBeat[256+12];
   char logFileName[128];
   unsigned long		rtc;					// time struct
   char failedHeartbeat[128];
	char failedTransmit[128];
   int retval;

	memset(msgQueue,0,sizeof(msgQueue));
   memset(queueIndexes,0,sizeof(queueIndexes));
	memset(msgTimeStamps,0,sizeof(msgTimeStamps));

   strcpy(failedHeartbeat,"Failed to send heartbeat\r\n");
   strcpy(failedTransmit,"Failed to transmit token\r\n");

   //Initialize log file
	rtc = read_rtc();
   sprintf(logFileName,"/rabbit.log"
   	);
   log_init(logFileName);

   serDopen(SERDBAUD);

   udp_init(LOCAL_PORT);


   while(1){
       tcp_tick(NULL);
	   //Serial Read Costate
	   costate readserial always_on{
	      //erase queue
	      memset(msgQueue[queueIndexes[msgQueueIndex]],0,sizeof(msgQueue[queueIndexes[msgQueueIndex]]));
	      wfd line_size = cof_ibac_read_serial(msgQueue[queueIndexes[msgQueueIndex]],MAX_TOKEN_SIZE-1);
	      //got a line of data
	      if(line_size>0)
	      {
	         //printf("%s\n",msgQueue[queueIndexes[msgQueueIndex]]);
            msgTimeStamps[queueIndexes[msgQueueIndex]] = read_rtc();
	         incrementIndex(msgQueueIndex);
	         for(i=0;i< NUMBER_INDEXES;i++)
	         {
	            //Token index has lapped someone moving much slower.
	            if(i!=msgQueueIndex&&queueIndexes[msgQueueIndex]==queueIndexes[i])
	            {
	               incrementIndex(i);
	            }
	         }

	      }

	   }

      //Logging Costate
      costate logdata always_on{
          waitfor(queueIndexes[logQueueIndex]!=queueIndexes[msgQueueIndex]);
          wfd log_msg(msgQueue[queueIndexes[logQueueIndex]]);
          incrementIndex(logQueueIndex);
       }

       //Parse and send UDP Packets
       costate transmit always_on{
      	waitfor(queueIndexes[networkTransmitIndex]!=queueIndexes[msgQueueIndex]);
         wfd parse_and_build(msgQueue[queueIndexes[networkTransmitIndex]],
         	msgTimeStamps[queueIndexes[networkTransmitIndex]],udpParseResponse);
         wfd retval=send_udp_message(udpParseResponse);
         if(retval<0)
         {
           wfd log_msg(failedTransmit);
         }
         incrementIndex(networkTransmitIndex);
       }

       //Receive and parse UPD Packets
       costate receive always_on{
         wfd udp_log_message = handle_udp_packets(ser_cmd);
         if(ser_cmd!=NULL)
         	wfd cof_serDwrite(ser_cmd, strlen(ser_cmd));
         if(strlen(udp_log_message)>0)
	         wfd log_msg(udp_log_message);

       }

       //heartbeat
       costate heartbeat always_on{
       	waitfor(IntervalSec(PULSE));
         build_heartbeat(udpHeartBeat,
         							msgQueue[(queueIndexes[msgQueueIndex]+QUEUE_SIZE-1)%QUEUE_SIZE],
                              msgTimeStamps[(queueIndexes[msgQueueIndex]+QUEUE_SIZE-1)%QUEUE_SIZE]
                              );
         wfd retval=send_udp_message(udpHeartBeat);
         if(retval<0)
         {
          wfd log_msg(failedHeartbeat);
         }

       }
	}


}

void incrementIndex(int index)
{
	queueIndexes[index]=(queueIndexes[index]+1)%QUEUE_SIZE;
}