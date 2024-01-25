/*** BeginHeader  */

#define SYNC 0x7E
#define MAX_DATA_LENGTH 128
#define HEADER_SIZE 10
#define MAIN_BUFF_SIZE 4095
#define BUFF_SIZE 1023

typedef struct
{
	uint8 sync;
   uint8 destination;
   uint16 msginfo;
   uint32 timestamp;
   uint16 timestamp_ms;
}SatHeader;


typedef struct
{
   uint32 timeinbuff;
   uint8 replydest;
	uint8 sync;
   uint8 destination;
   uint8 priority;
   uint8 type;
   uint16 length;
   uint32 timestamp;
   uint16 timestamp_ms;
   uint16 crc;
   uint8* data;
   uint8 totmsglen;
   uint8 msg[MAX_DATA_LENGTH + HEADER_SIZE];

} SatMessage;


int parseMessage(SatMessage* msg, uint8* buffer, int startix, int buffsize, int fromEnet);
int getDataLen(uint8* buffer, int* startix, int buffsize);
int putSatMsgInBuffer(int priority, SatMessage* msg);
int getSatMsgFromBuffer(int priority, SatMessage* msg);
void initSatMsgBuffers();
int putReceivedMsgInBuffer(SatMessage* msg);
int getReceivedMsgFromBuffer(SatMessage* msg);
int getBytesInSendBuffer();
int getBytesInReceiveBuffer();
void getPacketReceipt(uint8* receipt, SatMessage* msg, uint8 dest, int latency, uint32 tib);
int getBytesInBuffer(int priority);
void getStatusMsg(uint8* receipt);

extern char timeset;
//extern int modem_connected;

/*** EndHeader */


#ifndef _CBUF_LIB_
#use "CBUF.LIB"
#endif

#use "XMALLOC.LIB"
#use "RTC_Commands.c"
#use "SatModemDefs.c"
#use "NetworkDefs.c"

//int modem_connected;
char timeset;
static cbuf_t __far* MessageBuffers[4];
static cbuf_t __far* ReceivedMsgs;
static cbuf_t __far* Receipts;
static char __far BUFF0[MAIN_BUFF_SIZE+CBUF_OVERHEAD];
static char __far BUFF1[BUFF_SIZE+CBUF_OVERHEAD];
static char __far BUFF2[BUFF_SIZE+CBUF_OVERHEAD];
static char __far BUFF3[BUFF_SIZE+CBUF_OVERHEAD];
static char __far MSGS[MAIN_BUFF_SIZE+CBUF_OVERHEAD];
static char __far RCPTS[MAIN_BUFF_SIZE+CBUF_OVERHEAD];


// 2-byte number
int byteSwap16(uint16 i)
{
    return ((i>>8)&0xff)+((i << 8)&0xff00);
}

// 4-byte number
int byteSwap32(uint32 i)
{
    return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
}


void getPacketReceipt(uint8* receipt, SatMessage* msg, uint8 dest, int latency, uint32 tib)
{
	uint16 tempint;
   uint32 templong;
   uint8 *ip, *lp;
   ip = (uint8*)&tempint;
   lp = (uint8*)&templong;
	receipt[0] = SYNC;
   receipt[1] = dest;
	receipt[2] = 0x25;
   receipt[3] = 0xFC;
   templong = get1970RTC();
   receipt[4] = lp[3];
   receipt[5] = lp[2];
   receipt[6] = lp[1];
   receipt[7] = lp[0];
   tempint = MS_TIMER%1000;
   receipt[8] = ip[1];
   receipt[9] = ip[0];
   receipt[10] = msg->msg[2];
   receipt[11] = msg->msg[3];
   tempint = latency;
   receipt[12] = ip[1];
   receipt[13] = ip[0];
   templong = tib;
   receipt[14] = lp[3];
   receipt[15] = lp[2];
   receipt[16] = lp[1];
   receipt[17] = lp[0];
   templong = getBytesInSendBuffer();
   receipt[18] = lp[3];
   receipt[19] = lp[2];
   receipt[20] = lp[1];
   receipt[21] = lp[0];
   templong = msg->timestamp;
   receipt[22] = lp[3];
   receipt[23] = lp[2];
   receipt[24] = lp[1];
   receipt[25] = lp[0];
   tempint = msg->timestamp_ms;
   receipt[26] = ip[1];
   receipt[27] = ip[0];
   tempint = crc16_calc(receipt, 28, 0xFFFF);
   receipt[28] = ip[1];
   receipt[29] = ip[0];
}

void getStatusMsg(uint8* receipt)
{
	uint16 tempint;
   uint32 templong;
   uint8 *ip, *lp;
   uint8 dest = 0;
   int i;
   ip = (uint8*)&tempint;
   lp = (uint8*)&templong;

   for(i=7; i>=0; i--)
   {
   	if(sockconfig[i].enablestatus)
      {
      	dest += 1<<i;
   	}
   }

   //Header to all dests
   receipt[0] = SYNC;
   receipt[1] = dest;
	receipt[2] = 0x11;
   receipt[3] = 0xF8;
   templong = get1970RTC();
   receipt[4] = lp[3];
   receipt[5] = lp[2];
   receipt[6] = lp[1];
   receipt[7] = lp[0];
   tempint = MS_TIMER%1000;
   receipt[8] = ip[1];
   receipt[9] = ip[0];


   receipt[10] = (uint8)modem_state;
   receipt[11] = (uint8)!(0x00001&Read_Modem_Carrier_Detect());
   tempint = latency;
   receipt[12] = ip[1];
   receipt[13] = ip[0];
   templong = getBytesInSendBuffer();
   receipt[14] = lp[3];
   receipt[15] = lp[2];
   receipt[16] = lp[1];
   receipt[17] = lp[0];
   tempint = crc16_calc(receipt, 18, 0xFFFF);
   receipt[18] = ip[1];
   receipt[19] = ip[0];
}

int getBytesInBuffer(int priority)
{
   return buffer_used(MessageBuffers[priority]);
}

int getBytesInSendBuffer()
{
	int retval=0;

   retval += buffer_used(MessageBuffers[0]);
   retval += buffer_used(MessageBuffers[1]);
   retval += buffer_used(MessageBuffers[2]);
   retval += buffer_used(MessageBuffers[3]);

   return retval;
}


int getBytesInReceiveBuffer()
{
  return buffer_used(ReceivedMsgs);
}

int getDataLen(uint8* buffer, int* startix, int buffsize)
{
	int i = *startix;
   int datalen;


   while(buffer[i] != SYNC && i < (buffsize - (sizeof(SatHeader) +2)))
   {
   	i++;
   }

   //Check if we have a Sync in the buffer
   if(buffer[i] == SYNC)
   {
   	//If so, get length of data section
   	datalen = (buffer[i+2] >> 1 ) & 0x007F;

      *startix = i;

      return datalen;
   }
   else
   {
   	//if not, no valid message in buffer
   	return -1;
   }
}


// 	return values:
//	  		>0 :  Success, total message length returned
//       -1 :  No valid message in Buffer
//       -2 :  Not enough Data in Buffer
//       -3 :  Bad CRC
int parseMessage(SatMessage* msg, uint8* buffer, int startix, int buffsize,int fromEnet)
{

	uint16 calccrc;
   uint16 crc;
   uint16 tempint;
   int datalen;
   uint8* b;

   datalen = getDataLen(buffer, &startix, buffsize);
   if(datalen < 0)
   	return -1;

   //We have a sync and data length, make sure whole message is in buffer
   if(buffsize-startix >= sizeof(SatHeader)+datalen+2)
   {
   	//Whole message is in buffer, check CRC
      b = (uint8*)&crc;
      *b =  buffer[startix+sizeof(SatHeader)+datalen+1];
      *(b+1) =  buffer[startix+sizeof(SatHeader)+datalen];
      calccrc = crc16_calc( &buffer[startix],sizeof(SatHeader)+datalen, 0xFFFF);


      if(calccrc != crc)
      {
      	printf("Bad CRC\n");

      	return -3;
      }

      //Message is good fill in structure;
      msg->crc = crc;
      msg->totmsglen = sizeof(SatHeader)+datalen+2;
      msg->sync = buffer[startix];
      msg->destination = buffer[startix+1];
      msg->priority = buffer[startix+3] & 0x03;
      b = (uint8*)&tempint;
      *b =  buffer[startix+3];
      *(b+1) = buffer[startix+2];
      msg->type = (tempint >> 2 ) & 0x007F;
      msg->length = datalen;
      b = (uint8*)&msg->timestamp;
      *b =  buffer[startix+7];
      *(b+1) =  buffer[startix+6];
      *(b+2) =  buffer[startix+5];
      *(b+3) =  buffer[startix+4];

      b = (uint8*)&msg->timestamp_ms;
      *b =  buffer[startix+9];
      *(b+1) = buffer[startix+8];
      msg->data = &buffer[startix+10];
      _f_memcpy(msg->msg, &buffer[startix], msg->totmsglen);

      if(timeset==0 && fromEnet==1)
      {
      	printf("Setting Local Clock %d", msg->timestamp);
       	set1970RTC(msg->timestamp);
         timeset = 1;
      }


      return msg->totmsglen;

   }
   else
   {
   	if(fromEnet)
   		printf("Incomplete Message in Ethernet buffer\n");
      else
         printf("Incomplete Message in Serial buffer\n");
   	return -2;
   }

}

void initSatMsgBuffers()
{
      buffer_init( ReceivedMsgs = (cbuf_t __far *) &MSGS[0], MAIN_BUFF_SIZE);

   	buffer_init( MessageBuffers[0] = (cbuf_t __far *) &BUFF0[0], MAIN_BUFF_SIZE);
      buffer_init( MessageBuffers[1] = (cbuf_t __far *) &BUFF1[0], BUFF_SIZE);
      buffer_init( MessageBuffers[2] = (cbuf_t __far *) &BUFF2[0], BUFF_SIZE);
      buffer_init( MessageBuffers[3] = (cbuf_t __far *) &BUFF3[0], BUFF_SIZE);


}

int putReceivedMsgInBuffer(SatMessage* msg)
{
    return buffer_put(ReceivedMsgs, (byte __far *)msg, sizeof(SatMessage));
}

int getReceivedMsgFromBuffer(SatMessage* msg)
{
   return buffer_get(ReceivedMsgs, (byte __far *)msg, sizeof(SatMessage));
}


int putSatMsgInBuffer(int priority, SatMessage* msg)
{
	if(!(priority >= 0 && priority<4))
   	return -1;

   return buffer_put( MessageBuffers[priority], (byte __far *)msg, sizeof(SatMessage));
}


int getSatMsgFromBuffer(int priority, SatMessage* msg)
{
	if(!(priority >=0 && priority<4))
   	return -1;

   return buffer_get( MessageBuffers[priority], (byte __far *)msg, sizeof(SatMessage));
}


