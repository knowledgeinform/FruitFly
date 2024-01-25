/*** BeginHeader log_init,log_line,writeToFileFault */
void log_init(char * file_name);
scofunc void log_msg(char * tokenBuffer);
extern int writeToFileFault;

/*** EndHeader */

#memmap xmem
#class auto

#ifndef LOG_TO_FILE
	#define LOG_TO_FILE 0
#endif
#define FAT_USE_FORWARDSLASH
#define LOG_QUEUE_SIZE 16
#define LOG_MAX_LENGTH 128

#define FAT_NOCACHE

// Call in the FAT filesystem support code.
#memmap xmem
#use "fat.lib"

//write buffer
char buf[128];
//filename
char fileName[128];

// When files are accessed, we need a FATfile structure.
FATfile my_file;
// Use the first mounted FAT partition.
fat_part *first_part;

int writeToFileFault;


void log_init(char * file_name)
{
	int i;
   int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.
	long prealloc;
   writeToFileFault = 0;

	//zero filename
   memset(fileName,0,sizeof(fileName));

   //copy in filename
   strcpy(fileName,file_name);
   if(!LOG_TO_FILE)
   {
   	return;
   }
	// Auto-mount the FAT file system, which populates the default mounted
	// partition list array that is provided in FAT_CONFIG.LIB.  This is the most
	// important information since, when you open a file, you need only to
	// specify the partition.  Also, tell auto-mount to use the default device
	// configuration flags at run time.
    while ((rc = fat_AutoMount(FDDF_USE_DEFAULT)) == -EBUSY);

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
      return;
   }
        // OK, filesystem exists and is ready to access.  Let's create a file.

   // Do not pre-allocate any more than the minimum necessary amount of
   // storage.
	prealloc = 1;
   printf("Opening file: %s\n",fileName);

   my_file.state = 0;			// Initialize filestate to Idle
   // Open (and maybe create) it...

	while ((rc = fat_CreateFile(first_part, fileName,
                                prealloc, NULL)
                                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_Open() failed to reopen sequential file with return code %d\n", rc);
      writeToFileFault=1;
      return;
   }

    printf("Opening file: %s\n",fileName);
   while ((rc = fat_Open(
                 first_part,	// First partition pointer from fat_AutoMount()
                 fileName,	// Name of file.  Always an absolute path name.
                 FAT_FILE,		// Type of object, i.e. a file.
                 FAT_SEQUENTIAL,	// Create the file if it does not exist.
                 &my_file,		// Fill in this structure with file details
                 &prealloc		// Number of bytes to allocate.
                )
                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_Open() failed to reopen sequential file with return code %d\n", rc);
      writeToFileFault=1;
      return;
   }
   while ((rc = fat_Seek(&my_file,
                                0, SEEK_END)
                                ) == -EBUSY);

	if (rc < 0) {
   	printf("fat_Seek() failed to reach end of file with return code %d\n", rc);
      writeToFileFault=1;
      return;
   }

}

debug scofunc void log_msg(char * tokenBuffer)
{

	struct tm		rtc;					// time struct
	int rc;		// Return code store.  Always need to check return codes from
   				// FAT library functions.

   char * optr; //output pointer in line buffer
   int recordlength, ocount; //length of line, number of chars written to output
   int prealloc;
	//log line buffer
	static char log_line_buffer[512];
   ocount=0;
   prealloc = 1;

   //clear out old line
   memset(log_line_buffer,0,sizeof(log_line_buffer));

	//////////////////////////////////////////////////
	// read current date/time via tm_rd

	tm_rd(&rtc);						// get time in struct tm

	//////////////////////////////////////////////////
	// change the date/time via tm_wr

   sprintf(log_line_buffer,"%04d-%02d-%02d:%02d:%02d:%02d :",
   	(int)rtc.tm_year+1900,	(int)rtc.tm_mon,	(int)rtc.tm_mday,	(int)rtc.tm_hour,	(int)rtc.tm_min,	(int)rtc.tm_sec);
   strcpy(&(log_line_buffer[strlen(log_line_buffer)]),tokenBuffer);

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
	       waitfor((rc = fat_Write(&my_file, optr, recordlength - ocount)) != -EBUSY);
	       if (rc < 0)
	       {
	          printf("fat_Write: rc = %d\n",rc);
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

