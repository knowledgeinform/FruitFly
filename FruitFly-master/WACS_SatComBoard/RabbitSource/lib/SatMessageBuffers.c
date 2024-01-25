

#use "SatMessages.c"
/*** BeginHeader  */

int putSatMsgInBuffer(int priority, SatMessage* msg);

int getSatMsgFromBuffer(int priority, SatMessage* msg);

int initSatMsgBuffers();

static char bufPtrs[4];


/*** EndHeader */



int initSatMsgBuffers()
{
	unsigned long cxbufPtrPA;
	int i;
	static char cxbufSeg, * cxbufPtr;
	xheapInit(0x1008,5);
	for(i=0; i<4; i++)
   {
   	  cxbufPtrPA = xmalloc4kalign(0x1008);
        // get the 16 bit address of the buffer
   	  bufPtrs[i]  = XPTR_EOFFSET(cxbufPtrPA);
        cxbufSeg =  XPTR_XPC(cxbufPtrPA);

        // initialize circular buffer for 4095 bytes
   	  cxbuf_init(bufPtrs[i],0x0fff,cxbufSeg);
   }

}


int putSatMsgInBuffer(int priority, SatMessage* msg)
{
	if(!(priority>= && priority<4))
   	return -1;

   return buffer_put( bufPtrs[priority], msg, sizeof(SatMessage));
}


getSatMsgFromBuffer(int priority, SatMessage* msg)
{
	if(!(priority>= && priority<4))
   	return -1;

   return buffer_get( bufPtrs[priority], msg, sizeof(SatMessage));
}


 /*
   // shows stats
   printf("free = %d\n", cxbuf_free(cxbufPtr,cxbufSeg));
   printf("used = %d\n\n", cxbuf_used(cxbufPtr,cxbufSeg));

	// print buffer contents
   for(i=0;i<26;i++)
   {
  		printf("%c",cxbuf_getch(cxbufPtr,cxbufSeg));
   }

   // shows stats
   printf("\n\nfree = %d\n", cxbuf_free(cxbufPtr,cxbufSeg));
   printf("used = %d\n", cxbuf_used(cxbufPtr,cxbufSeg));

	// stuff buffer with chars until no more fit
   i = 0;
   while( cxbuf_free(cxbufPtr,cxbufSeg))
   {
		cxbuf_putch( cxbufPtr,32+(i%90),cxbufSeg);
		i++;
   }

   // i is char count in buffer
	printf("\n\n%d characters put\n\n",i);

	// spit out buffer contents until empty
   while(i)
   {
   	i--;
  		printf("%c",cxbuf_getch(cxbufPtr,cxbufSeg));
   } */