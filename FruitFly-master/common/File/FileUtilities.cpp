//---------------------------------------------------------------------------------------
/*!\class  File Name:	FileUtilities.cpp
// \author Author:		Myron Z. Brown, Jason A.Stipes
// \brief  Purpose:		Utilities for accessing files, adjusting file names
//
// \note   Notes:	
//
// \note   Routines: see Algorithm Class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2008
*/
//---------------------------------------------------------------------------------------

#include "FileUtilities.h"

/*! \fn void GetParentFolderName(const char *fullFileName, char *parentFolder)
 *  \brief Gets the parent folder name
 *  \param fullFileName - The full path
 *  \param parentFolder - returns the parent folder
 *  \exception 
 *  \return 
 */
void GetParentFolderName(const char *fullFileName, char *parentFolder)
{
	int ndx = 0;
	int len = (int)strlen(fullFileName);
	int ct = 0;
	for (int i=len-1;i>=0;i--)
	{
		if ((fullFileName[i] == '\\') || (fullFileName[i] == '/'))
		{
			ndx = i+1;
			ct++;
			if (ct == 2) break;
		}
	}
	strncpy(parentFolder, fullFileName, ndx);
	parentFolder[ndx] = '\0';
}

/*! \fn void RemoveFileName(const char *fullFileName, char *folderName)
 *  \brief Removes the file name
 *  \param fullFileName - The full path
 *  \param parentFolder - returns the parent folder name
 *  \exception 
 *  \return 
 */
void RemoveFileName(const char *fullFileName, char *folderName)
{
	int ndx = 0;
	int len = (int)strlen(fullFileName);
	for (int i=len-1;i>=0;i--)
	{
		if ((fullFileName[i] == '\\') || (fullFileName[i] == '/'))
		{
			ndx = i+1;
			break;
		}
	}
	strncpy(folderName, fullFileName, ndx);
	folderName[ndx] = '\0';
}

/*! \fn void RemoveFolderName(const char *fullFileName, char *adjustedFileName)
 *  \brief Removes the folder name
 *  \param fullFileName - The full path
 *  \param adjustedFileName - returns the adjusted file name
 *  \exception 
 *  \return 
 */
void RemoveFolderName(const char *fullFileName, char *adjustedFileName)
{
	int ndx = 0;
	int len = (int)strlen(fullFileName);
	for (int i=len-1;i>=0;i--)
	{
		if ((fullFileName[i] == '\\') || (fullFileName[i] == '/'))
		{
			ndx = i+1;
			break;
		}
	}
	strcpy(adjustedFileName, &fullFileName[ndx]);
}

/*! \fn void GetAdjustedFileNameBinary(const char *fullFileName, char *adjustedFileName)
 *  \brief Gets a binary adjusted filename (add _adj.bpf)
 *  \param fullFileName - The full path
 *  \param adjustedFileName - returns the adjusted file name
 *  \exception 
 *  \return 
 */
void GetAdjustedFileNameBinary(const char *fullFileName, char *adjustedFileName)
{
	int len = (int)strlen(fullFileName);
	strcpy(adjustedFileName, fullFileName);
	if (strcmp(&adjustedFileName[len-8],"_adj.bpf"))
		sprintf(&adjustedFileName[len-4], "_adj.bpf\0");
	else
		sprintf(&adjustedFileName[len-4], ".bpf\0");
}

/*! \fn void GetAdjustedFileNameASCII(const char *fullFileName, char *adjustedFileName)
 *  \brief Gets a ASCII adjusted filename (add _adj.txt)
 *  \param fullFileName - The full path
 *  \param adjustedFileName - returns the adjusted file name
 *  \exception 
 *  \return 
 */
void GetAdjustedFileNameASCII(const char *fullFileName, char *adjustedFileName)
{
	int len = (int)strlen(fullFileName);
	strcpy(adjustedFileName, fullFileName);
	if (strcmp(&adjustedFileName[len-8],"_adj.bpf"))
		sprintf(&adjustedFileName[len-4], "_adj.xyz\0");
	else
		sprintf(&adjustedFileName[len-4], ".xyz\0");
}


/*! \fn void GetAdjustedFileNameASCII(const char *fullFileName, char *adjustedFileName)
 *  \brief Gets a list of files in all sub-folders.
 *  \param pathName - The full path
 *  \param parentFolder - the parent folder
 *  \param fs - returns the list of folders
 *  \exception 
 *  \return 
 */
bool getFolders(char *pathName, char *parentFolder, vector<PathNameStruct> &fs)
{
	// Open the directory.
	DIR *dir;
	
	dir = opendir(pathName);
	if (dir == NULL) return false;

	// Loop on all files in folder.
	int len1 = (int)strlen(pathName);

	int len2=0;
	if(parentFolder != NULL)
		len2 = (int)strlen(parentFolder);
	if ((parentFolder == NULL) || (strcmp(&pathName[len1-len2], parentFolder) == 0))
	{
		PathNameStruct f;
		sprintf(f.pathName, pathName);
		fs.push_back(f);
	}
	while (1)
	{
		// Get the file name.
		dirent *dir_entry = readdir(dir);
		if (dir_entry == NULL) break;
		char fileName[1024];
		strcpy(fileName, dir_entry->d_name);
		if (fileName[0] == '.') continue;

		// Add to the list or keep looking.
		char newName[1024];
		sprintf(newName, "%s\\%s", pathName, fileName);
		getFolders(newName, parentFolder, fs);
	}

	// Close the directory.
	closedir(dir);

	return true;
}