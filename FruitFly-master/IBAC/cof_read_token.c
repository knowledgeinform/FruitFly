/*** BeginHeader cof_read_token */
scofunc int cof_read_token(char * tokenBuffer,int bufferSize);

/*** EndHeader */
scofunc int cof_read_token(char * tokenBuffer,int bufferSize)
{
   int tokenIndex;
   tokenIndex=0;
   while(tokenIndex<bufferSize)
   {
   	waitfor(c=serDgetc()&&c!=-1);
      tokenBuffer[tokenIndex]=c;
      if(c=='\n' && tokenIndex>0 &&token[tokenIndex-1]=='\r')
      	return tokenIndex;
      if(c==',')
      	return tokenIndex;
   }
   return -1;
}