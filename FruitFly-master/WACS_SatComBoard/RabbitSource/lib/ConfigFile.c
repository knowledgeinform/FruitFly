#use "subfs.lib"


/*** BeginHeader SaveFile,ReadFile*/
int SaveFile(char * name, void * data, int data_len);
int ReadFile(char * name, void * data, int * data_len);

#define FILE_VALID 12345

/*** EndHeader */

//=========================================================================
//SaveConfig()
//=========================================================================
int SaveFile(char * name, void * data, int data_len)
{
	int rc;

	while ((rc = subfs_create(name, data, data_len)) == -EAGAIN);
	return rc;
}

int ReadFile(char * name, void * data, int * data_len)
{
	int rc;
	unsigned long len = *data_len;	// Initialize to expected buffer length

	while ((rc = subfs_read(name, data, 0, &len)) == -EAGAIN);
	*data_len = (int)len;
	return rc;
}





