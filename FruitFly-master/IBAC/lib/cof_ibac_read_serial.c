/*** BeginHeader cof_ibac_read_serial */
scofunc int cof_ibac_read_serial(char * tokenBuffer,int bufferSize);

/*** EndHeader */

#class auto
#memmap xmem  
scofunc int cof_ibac_read_serial(char * tokenBuffer,int bufferSize)
{
   int tokenIndex;
   int c;
   tokenIndex=0;
   while(tokenIndex<bufferSize)
   {
   	waitfor((c=serDgetc())&&c!=-1);
      tokenBuffer[tokenIndex]=tolower(c);
      if(c=='\n' && tokenIndex>0 &&tokenBuffer[tokenIndex-1]=='\r')
      	return tokenIndex++;
   	tokenIndex++;
   }
   return -1;
}