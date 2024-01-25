/*** BeginHeader log_fs_init,log_fs_shutdown,log_new,log_file_init,check_create_dir,log_line,writeToFileFault */
void log_fs_init();
void log_fs_shutdown();
void log_new();
void log_file_init(char * file_name);
void check_create_dir(char * dir);
int isCardInserted ();

scofunc void log_msg(char* msgType, char * tokenBuffer, char forceNow);
void log_msg_header(char * tokenBuffer);
void log_header ();
extern int writeToFileFault;

extern char lastLogCommand;
extern int unMounted;

int logErrCode;
const int CARD_REMOVED_ERR_CODE = -1313;
/*** EndHeader */

#memmap xmem
#class auto

#ifndef LOG_TO_FILE
	#define LOG_TO_FILE 0
#endif
#define FAT_USE_FORWARDSLASH
#define LOG_QUEUE_SIZE 16
//#define LOG_MAX_LENGTH 128
#define FAT_BLOCK




// Call in the FAT filesystem support code.
#memmap xmem
#use "fat.lib"


//header
char log_header_text [128];

//Filename
char logFilename [128];

//static object for logging, should be safely shared between costates (won't ever print header while logging data)
char log_line_buffer[1024];

// When files are accessed, we need a FATfile structure.
FATfile my_file;
// Use the first mounted FAT partition.
fat_part *first_part;

int writeToFileFault;
long currFileSize;
long maxFileSize;

//True if shutdown has been called
int unMounted;
char lastLogCommand;



void log_fs_init()
{

   int i;
   int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.

#GLOBAL_INIT
{
	logErrCode = 0;
}

   logErrCode = 0;
   maxFileSize = 10000000;
   currFileSize = 0;

	if (lastLogCommand == 0)
   {
      writeToFileFault = 1;
	   unMounted = 1;
      return;
   }

   writeToFileFault = 0;
   unMounted = 0;

   if (nf_XD_Detect (1) < 0)
   {
	   printf ("INSERT xD CARD!");
      writeToFileFault = 1;
      unMounted = 1;
      setActualLogState(0);
      if (logErrCode == 0) logErrCode = CARD_REMOVED_ERR_CODE;
   }
   else
   {
       printf ("xD CARD DETECTED!!!");
   }

   if(!LOG_TO_FILE)
   {
   	return;
   }
	// Auto-mount the FAT file system, which populates the default mounted
	// partition list array that is provided in FAT_CONFIG.LIB.  This is the most
	// important information since, when you open a file, you need only to
	// specify the partition.  Also, tell auto-mount to use the default device
	// configuration flags at run time.
   while ((rc = fat_AutoMount(FDDF_MOUNT_PART_0 | FDDF_MOUNT_DEV_1)) == -EBUSY);
//   while ((rc = fat_AutoMount(FDDF_USE_DEFAULT)) == -EBUSY);
//   fatwtc_flushdev(word dev, word flags)
//    while ((rc = fat_AutoMount(FDDF_MOUNT_DEV_1 | FDDF_MOUNT_PART_0 | FDDF_UNCOND_DEV_FORMAT | FDDF_UNCOND_PART_FORMAT)) == -EBUSY);


	// Scan the populated mounted partitions list to find the first mounted
	// partition.  The number of configured fat devices, as well as the mounted
	// partition list, are provided for us in FAT_CONFIG.LIB.
	first_part = NULL;
	for (i = 0; i < num_fat_devices * FAT_MAX_PARTITIONS; ++i) {
		if ((first_part = fat_part_mounted[i]) != NULL) {
			// found a mounted partition, so use it
			break;
		}
	}

	// Check if a mounted partition was found
	if (first_part == NULL) {
		// No mounted partition found, ensure rc is set to a FAT error code.
		rc = (rc < 0) ? rc : -ENOPART;
	} else {
		// It is possible that a non-fatal error was encountered and reported,
		// even though fat_AutoMount() succeeded in mounting at least one
		// FAT partition.
		printf("fat_AutoMount() succeeded with return code %d.\n", rc);
		// We found a partition to work with, so ignore other error (if any).
		rc = 0;
	}

   // FAT return codes always follow the convention that a negative value
   // indicates an error.
	if (rc < 0) {
   	// An error occurred.  Here, we print out the numeric code.  You can
      // look in lib\filesystem\errno.lib to see what type of error it
      // really is.  Note that the values in errno.lib are the absolute
      // value of the return code.
   	if (rc == -EUNFORMAT)
      	printf("Device not Formatted, Please run Fmt_Device.c\n");
      else
	   	printf("fat_AutoMount() failed with return code %d.\n", rc);
		writeToFileFault = 1;
      unMounted = 1;
      if (logErrCode == 0) logErrCode = rc;
      setActualLogState(0);
      return;
   }

}

void log_fs_shutdown()
{
	if (unMounted == 1)
   	return;

   lastLogCommand = 0;
   logErrCode = 0;

   fat_Close (&my_file);

   // Since we are using blocking mode, it will not return until it has
   // closed all files and unmounted the partition & device.
   fat_UnmountDevice(first_part->dev);
   printf ("Log closed");
   writeToFileFault = 0xff;
   unMounted = 1;
   setActualLogState(0);
}

void log_new()
{
   struct tm		tm_rtc;
   long currRtc;
   char * curLogFilePos;


	logErrCode = 0;
   lastLogCommand = 1;
   currFileSize = 0;

   currRtc = read_rtc ();
   mktm (&tm_rtc, currRtc);
   //rtc.tm_sec = 59;							// change the time
	//rtc.tm_min = 59;
	//rtc.tm_hour = 23;
	//rtc.tm_mday = 31;
   //rtc.tm_mon = 12;
   //rtc.tm_year = 99;


   if (unMounted == 1 || writeToFileFault)
   {
     	log_fs_init ();
   }


   if(writeToFileFault || unMounted == 1)
   	return;

   curLogFilePos =  logFilename;

   //create yyyy
   curLogFilePos +=sprintf(curLogFilePos,"/%04d",tm_rtc.tm_year+1900);
   check_create_dir(logFilename);
   //create mm
   curLogFilePos +=sprintf(curLogFilePos,"/%02d",tm_rtc.tm_mon);
   check_create_dir(logFilename);
   //create dd
   curLogFilePos +=sprintf(curLogFilePos,"/%02d",tm_rtc.tm_mday);
   check_create_dir(logFilename);
   //create filename
   curLogFilePos +=sprintf(curLogFilePos,"/%02d%02d%02d.log",tm_rtc.tm_hour,tm_rtc.tm_min,tm_rtc.tm_sec);
   log_file_init(logFilename);
}


void check_create_dir(char * dir){

	int rc;

   rc = 0;

	printf("creating directory: %s\n",dir);
	rc = fat_CreateDir(first_part,dir);

   if (rc < 0)
   {
	 	printf("fat_CreateDir() failed with return code %d.\n", rc);
      if (logErrCode == 0) logErrCode = rc;
      setActualLogState(0);
   }

}

int isCardInserted ()
{
	 if (nf_XD_Detect (1) < 0)
    	return 0;
	 else
      return 1;
}

void log_file_init(char * file_name)
{
   int i;
   int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.
	long prealloc;

   if(!LOG_TO_FILE)
   {
   	return;
   }
	  // OK, filesystem exists and is ready to access.  Let's create a file.

   // Do not pre-allocate any more than the minimum necessary amount of
   // storage.
	prealloc = 1;
   printf("Creating file: %s\n",file_name);

   my_file.state = 0;			// Initialize filestate to Idle
   // Open (and maybe create) it...

	while ((rc = fat_CreateFile(first_part, file_name,
                                prealloc, NULL)
                                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_CreateFile() failed to create file with return code %d\n", rc);

      writeToFileFault=1;
      if (logErrCode == 0) logErrCode = rc;
      setActualLogState(0);
      return;
   }

    printf("Opening file: %s\n",file_name);
   while ((rc = fat_Open(
                 first_part,	// First partition pointer from fat_AutoMount()
                 file_name,	// Name of file.  Always an absolute path name.
                 FAT_FILE,		// Type of object, i.e. a file.
                 FAT_SEQUENTIAL,	// Create the file if it does not exist.
                 &my_file,		// Fill in this structure with file details
                 &prealloc		// Number of bytes to allocate.
                )
                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_Open() failed to open sequential file with return code %d\n", rc);
      writeToFileFault=1;
      if (logErrCode == 0) logErrCode = rc;
      setActualLogState(0);
      return;
   }
   while ((rc = fat_Seek(&my_file,
                                0, SEEK_END)
                                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_Seek() failed to reach end of file with return code %d\n", rc);
      writeToFileFault=1;
      if (logErrCode == 0) logErrCode = rc;
      setActualLogState(0);
      return;
   }

   setActualLogState(1);
   writeToFileFault = 0;

   log_header ();
}

debug scofunc void log_msg(char* msgType, char * tokenBuffer, char forceNow)
{
	struct tm		rtc;					// time struct
   unsigned long rtcRTC;

   int prealloc;
	//log line buffer
//	static char log_line_buffer[512];
   int typeLength;
   int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.
	int recordlength, ocount; //length of line, number of chars written to output
   char * optr; //output pointer in line buffer
   char makeNewLogFile;


   LOG_MSG_START:

   ocount=0;
   makeNewLogFile = 0;


   prealloc = 1;

   if (getActualLogState() == 0)
   	return;
   if (currFileSize > maxFileSize)
   {
   	//log message indicating file split
      makeNewLogFile = 1;

      sprintf (&tokenBuffer[strlen(tokenBuffer)], "\r\n\r\nLog file size limit reached, starting new log...\r\n\0");
      printf ("Make new file based on size limit...");
   }


   //clear out old line
   memset(log_line_buffer,0,sizeof(log_line_buffer));

	//////////////////////////////////////////////////
	// read current date/time via tm_rd

	rtcRTC = read_rtc();						// get time in struct tm
   mktm(&rtc, rtcRTC);

	//////////////////////////////////////////////////
	// change the date/time via tm_wr

   //Add timestamp
   sprintf(&log_line_buffer[strlen(log_line_buffer)],"%04d-%02d-%02d %02d:%02d:%02d :",
   	(int)rtc.tm_year+1900,	(int)rtc.tm_mon,	(int)rtc.tm_mday,	(int)rtc.tm_hour,	(int)rtc.tm_min,	(int)rtc.tm_sec);

   //Add message type to line
   typeLength = 0;
   if (msgType != NULL)
   {
   	typeLength = strlen (msgType);
	   strcpy (&log_line_buffer[strlen(log_line_buffer)], msgType);
   }

   //Add message to log line
   strcpy(&(log_line_buffer[strlen(log_line_buffer)]),tokenBuffer);


   //initialize optr
   optr = log_line_buffer;

   printf(log_line_buffer);


   if(LOG_TO_FILE&&!writeToFileFault)
   {
	   recordlength = strlen(log_line_buffer);
      currFileSize += recordlength;

	   // Write to it...
	   //NonBlocking Write
      while (ocount < recordlength)   // Loop until entire record is written
	   {
      	if (forceNow == 0)
         {
		       waitfor((rc = fat_Write(&my_file, optr, recordlength - ocount)) != -EBUSY);
         }
         else
         {
				while ((rc = fat_Write(&my_file, optr, recordlength - ocount)) == -EBUSY);
         }

	       if (rc < 0)
	       {
	          printf("fat_Write: rc = %d\n",rc);
             if (logErrCode == 0) logErrCode = rc;
             setActualLogState(0);
             return;
	       }
	       optr += rc;          // Move output pointer
	       ocount += rc;        // Add number of characters written
	    }

/*
   	waitfor ((rc = fat_SyncPartition(first_part))!= -EBUSY);
	   if (rc < 0) {
	      printf("fat_SyncPartition() failed with return code %d\n", rc);

	      // In real applications which don't exit(), we would probably want to
	      // close the file and continue with something else.
	      return;
	   }
      fat_tick();       */
   }


   if (makeNewLogFile == 1)
   {
    	log_new();
      printf ("File created based on size limit...");
      msgType = NULL;
      sprintf (tokenBuffer, "Last file closed due to file size, continuing here...\r\n\0");
      forceNow = 1;
      goto LOG_MSG_START;
      //wfd log_msg (NULL, "Last file closed due to file size, continuing here...\n", 1);
   }
}

void log_msg_header(char * tokenBuffer)
{
	memset (log_header_text, 0, sizeof(log_header_text));
 	memcpy (log_header_text, tokenBuffer, strlen(tokenBuffer));
}

void log_header ()
{

//	waitfor (log_msg (NULL, log_header_text, 1));

	struct tm		rtc;					// time struct
   unsigned long rtcRTC;
	int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.

   char * optr; //output pointer in line buffer
   int recordlength, ocount; //length of line, number of chars written to output
   int prealloc;
	//log line buffer
//	static char log_line_buffer[512];

   ocount=0;
   prealloc = 1;

   //clear out old line
   memset(log_line_buffer,0,sizeof(log_line_buffer));

	//////////////////////////////////////////////////
	// read current date/time via tm_rd

	rtcRTC = read_rtc();						// get time in struct tm
   mktm(&rtc, rtcRTC);

 	//////////////////////////////////////////////////
	// change the date/time via tm_wr
   sprintf(log_line_buffer,"%04d-%02d-%02d %02d:%02d:%02d :",
   	(int)rtc.tm_year+1900,	(int)rtc.tm_mon,	(int)rtc.tm_mday,	(int)rtc.tm_hour,	(int)rtc.tm_min,	(int)rtc.tm_sec);
   strcpy(&(log_line_buffer[strlen(log_line_buffer)]),log_header_text);

   //initialize optr
   optr = log_line_buffer;

   printf(log_line_buffer);

   if(LOG_TO_FILE&&!writeToFileFault)
   {
	   recordlength = strlen(log_line_buffer);

	   // Write to it...
	   //NonBlocking Write
      while (ocount < recordlength)   // Loop until entire record is written
	   {
	       while ((rc = fat_Write(&my_file, optr, recordlength - ocount)) == -EBUSY);
	       if (rc < 0)
	       {
	          printf("fat_Write: rc = %d\n",rc);
             if (logErrCode == 0) logErrCode = rc;
             setActualLogState(0);
             return;
	       }
	       optr += rc;          // Move output pointer
	       ocount += rc;        // Add number of characters written
	    }

/*
   	waitfor ((rc = fat_SyncPartition(first_part))!= -EBUSY);
	   if (rc < 0) {
	      printf("fat_SyncPartition() failed with return code %d\n", rc);

	      // In real applications which don't exit(), we would probably want to
	      // close the file and continue with something else.
	      return;
	   }
      fat_tick();       */
   }
}