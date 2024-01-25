#include "stdafx.h"
#include "windows.h"
#include "DTEDLoader.h"
#include "Constants.h"
#include "math.h"
#include "stdio.h"
#include "string.h"
#include "stdlib.h"
#include <sys/stat.h>
#include "Logger.h"

#include <iostream>
using namespace std;

DTEDObject* DTEDLoader::loadDTED (double lat1, double lon1, double lat2, double lon2, char dtExt[10], int spacing)
{
	int latInt1 = (int)floor(lat1);
	int latInt2 = (int)floor(lat2);
	latInt2 = min (latInt2, latInt1+1);

	int lonInt1 = (int)floor(lon1);
	int lonInt2 = (int)floor(lon2);
	lonInt2 = min (lonInt2, lonInt1+1);

	char filename [1024];
	DTEDObject* newDtedObject = new DTEDObject ();
	bool firstTile = true;
	for (int latCtr = latInt1; latCtr <= latInt2; latCtr ++)
	{
		for (int lonCtr = lonInt1; lonCtr <= lonInt2; lonCtr ++)
		{
			getDtedTileFilename (latCtr, lonCtr, filename);
			sprintf (filename, "%s%s\0", filename, dtExt);
			analyzeDTED (filename, lat1, lon1, lat2, lon2, spacing, newDtedObject, firstTile);
		}
	}


	return newDtedObject;
}

void DTEDLoader::getDtedTileFilename (int latInt, int lonInt, char filename[])
{
	int chooseLat = latInt;
	int chooseLon = lonInt;
	char latDir, lonDir;

	if (chooseLat < 0) {
		latDir = 's';
		chooseLat = abs(chooseLat);
	} else
		latDir = 'n';

	if (chooseLon < 0) {
		lonDir = 'w';
		chooseLon = abs(chooseLon);
	} else
		lonDir = 'e';

	// An example full filename (including directory structure would look like:
	//		dtedFolderName\w077\n37.dt2
	// Use the current location and form a likeness...

	char mypath[10000];
	GetModuleFileName(NULL, mypath, 10000);

	char drive[10]; char dir[10000]; char fname[10000]; char ext[10];
 	_splitpath( mypath, drive, dir, fname, ext );
	
	sprintf (filename, "%s%s..\\%c%03i\\%c%02i\0", drive, dir, lonDir, chooseLon, latDir, chooseLat);
}

DTEDObject* DTEDLoader::analyzeDTED (char filename[], double minLat, double minLon, double maxLat, double maxLon, int spacing, DTEDObject* dtedObject, bool &firstTile)
{
	FILE *fp;
	
	struct __stat64 statbuf;
	if(0 != _stat64(filename, &statbuf)) {
		Logger::getInstance()->logMessage ("DTEDLoader:: Error - DTED File not found" + (string(filename)));
		return NULL;
	}
	__int64 ssize = statbuf.st_size;


	// Build the new object
	fp = fopen(filename, "rb");
	if (fp == NULL)
	{
		Logger::getInstance()->logMessage ("DTEDLoader:: Error - DTED File not found" + (string(filename)));
		return NULL;
	}

	unsigned int current = 0;
	char buffer[800];
	fread(buffer, sizeof(char), 1, fp);
	short lastElev = 0;


	double currAnchorLat = 0;
	double currAnchorLon = 0;
	double currResLon = 0;
	double currResLat = 0;
	double currNumLon = 0;
	double currNumLat = 0;

	
	while (current < ssize) {
		if (buffer[0] == 'V') {
			
			fread(buffer, sizeof(char), 79, fp);
			current += 80;
		} else if (buffer[0] == 'H') {
			
			fread(buffer, sizeof(char), 79, fp);
			current += 80;
		} else if (buffer[0] == 'U') {
			
			fread(&buffer[1], sizeof(char), 2, fp);
			buffer[3] = '\0';
			if (strcmp(buffer, "UHL")) continue;
			fread(&buffer[1], sizeof(char), 1, fp);
			
			// Longitude Degrees
			char lngdega[4];  lngdega[3] = '\0';
			fread(lngdega, sizeof(char), 3, fp);
			int lngdeg = atoi(lngdega);

			// Longitude Minutes
			char lngmina[3]; lngmina[2] = '\0';
			fread(lngmina, sizeof(char), 2, fp);
			int lngmin = atoi(lngmina);

			// Longitude Seconds
			char lngseca[3]; lngseca[2] = '\0';
			fread(lngseca, sizeof(char), 2, fp);
			int lngsec = atoi(lngseca);

			// Longitude Hemisphere
			char lnghem[1];
			fread(lnghem, sizeof(char), 1, fp);

			// Latitude Degrees
			char latdega[4]; latdega[3] = '\0';
			fread(latdega, sizeof(char), 3, fp);
			int latdeg = atoi(latdega);

			// Latitude Minutes
			char latmina[3];  latmina[2] = '\0';
			fread(latmina, sizeof(char), 2, fp);
			int latmin = atoi(latmina);

			// Latitude Seconds
			char latseca[3]; latseca[2] = '\0';
			fread(latseca, sizeof(char), 2, fp);
			int latsec = atoi(latseca);

			// Latitude Hemisphere
			char lathem[1];
			fread(lathem, sizeof(char), 1, fp);

			currAnchorLat = latdeg + latmin*INV60 + latsec*INV3600;
			currAnchorLon = lngdeg + lngmin*INV60 + lngsec*INV3600;

			if (lathem[0] == 'S') currAnchorLat *= -1;
			if (lnghem[0] == 'W') currAnchorLon *= -1;

			// Longitude data interval
			char lngint[5]; lngint[4] = '\0';
			fread(lngint, sizeof(char), 4, fp);
			currResLon = atof(lngint)*0.1/3600;

			// Latitude data interval
			char latint[5]; latint[4] = '\0';
			fread(latint, sizeof(char), 4, fp);
			currResLat = atof(latint)*0.1/3600;

			// Vertical Accuracy
			char vertacc[4];
			fread(vertacc, sizeof(char), 4, fp);

			// Security Code
			char security[3];
			fread(security, sizeof(char), 3, fp);

			// Unique Reference
			char ref[12];
			fread(ref, sizeof(char), 12, fp);

			// Number of Longitude Points
			char widtha[5]; widtha[4] = '\0';
			fread(widtha, sizeof(char), 4, fp);
			currNumLon = atoi(widtha);

			// Number of Latitude Points
			char heighta[5]; heighta[4] = '\0';
			fread(heighta, sizeof(char), 4, fp);
			currNumLat = atoi(heighta);

			// Multiple Accuracy
			char multacc[1];
			fread(multacc, sizeof(char), 1, fp);

			// Reserved
			fread(buffer, sizeof(char), 24, fp);


			if (firstTile)
			{
				//Only set DTED object parameters when reading the first tile.  These parameters
				//will be preserved and used to load/merge subsequent tiles
				dtedObject->resLat = currResLat * spacing;
				dtedObject->resLon = currResLon * spacing;

				int minLatIdx = (int)((minLat - currAnchorLat)/dtedObject->resLat);
				int minLonIdx = (int)((minLon - currAnchorLon)/dtedObject->resLon);
				dtedObject->anchorLat = currAnchorLat + dtedObject->resLat*minLatIdx;
				dtedObject->anchorLon = currAnchorLon + dtedObject->resLon*minLonIdx;

				int maxLatIdx = (int)((maxLat - currAnchorLat)/dtedObject->resLat) + 1;
				int maxLonIdx = (int)((maxLon - currAnchorLon)/dtedObject->resLon) + 1;
				dtedObject->numLat = maxLatIdx - minLatIdx + 1;
				dtedObject->numLon = maxLonIdx - minLonIdx + 1;

				//Create data array for z values
				if (dtedObject->dataArray == NULL) 
				{
					dtedObject->dataArray = new DTEDPoint** [dtedObject->numLat];
					for (int i = 0; i < dtedObject->numLat; i++) {
						dtedObject->dataArray[i] = new DTEDPoint* [dtedObject->numLon];
						for (int j = 0; j < dtedObject->numLon; j++) {
							dtedObject->dataArray[i][j] = new DTEDPoint(i*dtedObject->resLat + dtedObject->anchorLat, j*dtedObject->resLon + dtedObject->anchorLon, 0);
						}
					}
				}

				firstTile = false;
			}
			
			current += 80;
		} else if (buffer[0] == 'D') {
			
			fread(buffer, sizeof(char), 647, fp);
			current += 648;
		} else if (buffer[0] == 'A') {
						
			fread(buffer, sizeof(char), 500, fp);
			fread(buffer, sizeof(char), 500, fp);
			fread(buffer, sizeof(char), 500, fp);
			fread(buffer, sizeof(char), 500, fp);
			fread(buffer, sizeof(char), 500, fp);
			fread(buffer, sizeof(char), 199, fp);
			current += 2700;
		} else if (buffer[0] == 'E') {
						
			fread(buffer, sizeof(char), 3, fp);
			current += 4;
		} else if (buffer[0] == (char)170) {
				
			// Data Block Count
			fread(&buffer, sizeof(char), 3, fp);

			// Longitude Count
			short lngct[1];
			fread(lngct, sizeof(short), 1, fp);
			swapShort(lngct[0]);

			
			// Latitude Count
			short latct[1];
			fread(latct, sizeof(short), 1, fp);
			swapShort(latct[0]);

			int ct;
			int ctStart = 0;
			int ctEnd = currNumLat;
			signed short elevation[1];

			//Read a single longitude line of DTED datas and load into DTED object
			for (ct = ctStart; ct < ctEnd; ++ct) {

				fread(elevation, sizeof(short), 1, fp);
				swapShort(elevation[0]);

				if (elevation[0] < 0) {
					elevation[0] = -32768 - elevation[0];
				}				
				if (elevation[0] < 0)
					elevation[0] = lastElev;

				if (lngct[0] % spacing == 0)
				{
					if (ct % spacing == 0)
					{
						//Offset indicies by .5 cells to ensure we put data in the right cell
						dtedObject->setZ (currAnchorLat + (ct+0.5)*currResLat,
											currAnchorLon + (lngct[0]+0.5)*currResLon,
											(double)elevation[0]);
					}
				}					
				lastElev = elevation[0];
			}

			// Checksum
			fread(&buffer, sizeof(char), 4, fp);
			
			current += (12 + 2*currNumLat);
		}
		fread(buffer, sizeof(char), 1, fp);
		++current;
	}
	fclose(fp);

	return dtedObject;
}


void DTEDLoader::swapShort (short &val)
{
	unsigned char hold;
	unsigned char* c = (unsigned char*) (&val);
	hold = c[0];
	c[0] = c[1];
	c[1] = hold;
}