/*** BeginHeader cof_read_serial_C,cof_read_serial_E_anaconda */
scofunc int cof_read_serial_C(char * tokenBuffer,int bufferSize);
scofunc int cof_read_serial_E_anaconda(char* tokenBuffer, int bufferSize);
/*** EndHeader */

#class auto
#memmap xmem
 

//Read data from serial port C into buffer
scofunc int cof_read_serial_C(char * tokenBuffer,int bufferSize)
{
   int tokenIndex;
   int c;
   tokenIndex=0;
   while(tokenIndex<bufferSize)
   {
		waitfor((c=serCgetc())&&c!=-1);

      tokenBuffer[tokenIndex]=tolower(c);
      if(c=='\n' && tokenIndex>0 &&tokenBuffer[tokenIndex-1]=='\r')
      	return tokenIndex++;
      if(c=='\r' && tokenIndex>0 &&tokenBuffer[tokenIndex-1]=='\n')
      	return tokenIndex++;
   	tokenIndex++;
   }
   return -1;
}

scofunc int cof_read_serial_E_anaconda(char* tokenBuffer, int bufferSize)
{
	int dataLength;
   int tokenIndex;
   int c;
   char checkSum;
   int bytesToRead;
   int readBytes;
   int buffOffset;

   tokenIndex=0;
   checkSum = 0;
   readBytes = 0;
   bytesToRead = 0;
   buffOffset = 0;

   //Wait until we get a valid sync char
   waitfor((c=serEgetc())&&c==0x7E);
   tokenBuffer[tokenIndex]=c;
   //checkSum += c; ignored in checksum
   tokenIndex++;

   //Wait until we get another valid sync char
   waitfor((c=serEgetc())&&c==0x7E);
   tokenBuffer[tokenIndex]=c;
   //checkSum += c; ignored in checksum
   tokenIndex++;

   //Wait until we get a valid character (message type)
   waitfor((c=serEgetc())&&c!=-1);
   tokenBuffer[tokenIndex]=c;
   checkSum += c;
   tokenIndex++;

   //Wait until we get 2 more valid characters (data length)
   waitfor((c=serEgetc())>=0);
   tokenBuffer[tokenIndex]=c;
   checkSum += c;
   tokenIndex++;
   waitfor((c=serEgetc())>= 0);
   tokenBuffer[tokenIndex]=c;
   checkSum += c;
   tokenIndex++;

	dataLength = tokenBuffer[3] + (tokenBuffer[4]<<8);
   dataLength += 6;	//Already read 5 bytes, also include checksum at end

   if (dataLength > bufferSize || dataLength < 0)
   {
   	serErdFlush ();
	   return -1314;
   }

	bytesToRead = dataLength - 5;
   buffOffset = 5;
   while (true)
   {
	   waitfor (readBytes = serEread (&tokenBuffer[buffOffset], bytesToRead, 500));
      if (readBytes != bytesToRead)
      {
      	bytesToRead = dataLength - buffOffset;
         buffOffset += readBytes;
      }
      else
      {
      	break;
      }
   }

   for (c=5; c < dataLength-1; c++)
   {
    	checkSum += tokenBuffer[c];
   }

  	if (checkSum == tokenBuffer[dataLength-1])
		return dataLength;
   else
   {
      serErdFlush ();
   	return -1313;
   }


   return -13;
}


/*** BeginHeader cof_read_serial_C_fixed,cof_read_serial_C_fixed_immediate,cof_read_serial_E */
scofunc int cof_read_serial_C_fixed(char * tokenBuffer,int bufferSize);
scofunc int cof_read_serial_C_fixed_immediate(char * tokenBuffer,int bufferSize, int maxIterations);
scofunc int cof_read_serial_E(char * tokenBuffer,int bufferSize);

int cMsgBuffered;
/*** EndHeader */


//Read fixed-size data from serial port C into buffer
scofunc int cof_read_serial_C_fixed_immediate(char * tokenBuffer,int bufferSize, int maxIterations)
{
   int tokenIndex;
   int c;
   int itr;

   tokenIndex=0;
   itr = 0;
   while(tokenIndex<bufferSize && itr<maxIterations)
   {
      c=serCgetc();
      if (c!=-1)
      {
      	tokenBuffer[tokenIndex] = c;
	      tokenIndex++;
      }
      itr ++;
   }
   return tokenIndex;
}

scofunc int cof_read_serial_C_fixed(char * tokenBuffer,int bufferSize)
{
   int tokenIndex;
   int c;
   tokenIndex=0;
   while(tokenIndex<bufferSize)
   {
   	//On serial C from Bladewerx (pod 1), we might get messages outside of this loop.  This check will inject them.
		waitfor(cMsgBuffered > 0 || ((c=serCgetc())>=0));

      if (cMsgBuffered > 0)
      {
      	//Waitfor was short-circuited and we shouldn't have lost any bytes
         //Skip to the front of the costate next time to get the buffered message in
       	return -13;
      }

      tokenBuffer[tokenIndex] = c;
	   tokenIndex++;
   }
   return tokenIndex;
}

//Read data from serial port Einto buffer
scofunc int cof_read_serial_E(char * tokenBuffer,int bufferSize)
{
   int tokenIndex;
   int c;
   tokenIndex=0;
   while(tokenIndex<bufferSize)
   {
   	waitfor((c=serEgetc())&&c!=-1);
      tokenBuffer[tokenIndex]=tolower(c);
      if(c=='\n' && tokenIndex>0 &&tokenBuffer[tokenIndex-1]=='\r')
      	return tokenIndex++;
   	tokenIndex++;
   }
   return -1;
}


