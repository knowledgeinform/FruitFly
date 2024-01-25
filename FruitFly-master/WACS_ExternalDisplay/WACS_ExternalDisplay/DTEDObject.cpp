#include "stdafx.h"
#include "windows.h"
#include "DTEDObject.h"
#include "Constants.h"
#include "Config.h"


DTEDObject::DTEDObject()
{
	dataArray = NULL;

	m_MinZOffsetM = Config::getInstance()->getValueAsDouble ("DtedObject.MinZOffsetM", 25); 
}

DTEDObject::~DTEDObject()
{
	for (int i = 0; i < numLat; i ++)
	{
		for (int j = 0; j < numLon; j ++)
		{
			delete dataArray [i][j];
		}
		delete [] dataArray [i];
	}
	delete [] dataArray;
}

double DTEDObject::getMinZ (double latitude, double longitude)
{

	double myLat, myLon;

	myLat = latitude;
	myLon = longitude;

	int myLatIdx = (myLat - anchorLat)/resLat;
	int myLonIdx = (myLon - anchorLon)/resLon;

	if (myLatIdx < 0)
		myLatIdx = 0;
	if (myLonIdx < 0)
		myLonIdx = 0;
	if (myLatIdx > numLat-2)
		myLatIdx = numLat-2;
	if (myLonIdx > numLon-2)
		myLonIdx = numLon-2;

	double z1 = dataArray[myLatIdx][myLonIdx]->z;
	double z2 = dataArray[myLatIdx+1][myLonIdx]->z;
	double z3 = dataArray[myLatIdx][myLonIdx+1]->z;
	double z4 = dataArray[myLatIdx+1][myLonIdx+1]->z;

	double max12 = max (z1, z2);
	double max34 = max (z3, z4);

	//Maximum altitude of rectangle enclosing latitude,longitude
	double max1234 = max (max12, max34);

	//Add offset buffer to maximum altitude
	return (max1234+m_MinZOffsetM);
}

bool DTEDObject::setZ (double latDecDeg, double lonDecDeg, double elevM)
{
	int myLatIdx = (latDecDeg - anchorLat)/resLat;
	int myLonIdx = (lonDecDeg - anchorLon)/resLon;


	if (myLatIdx < 0)
		return false;
	if (myLonIdx < 0)
		return false;
	if (myLatIdx > numLat-1)
		return false;
	if (myLonIdx > numLon-1)
		return false;

	dataArray[myLatIdx][myLonIdx]->z = elevM;
	return true;
}

double DTEDObject::getZ (double latDecDeg, double lonDecDeg)
{
	int myLatIdx = (latDecDeg - anchorLat)/resLat;
	int myLonIdx = (lonDecDeg - anchorLon)/resLon;

	return getZ (myLatIdx, myLonIdx);
}

double DTEDObject::getZ (int myLatIdx, int myLonIdx)
{
	if (myLatIdx < 0)
		return 0;
	if (myLonIdx < 0)
		return 0;
	if (myLatIdx > numLat-1)
		return 0;
	if (myLonIdx > numLon-1)
		return 0;

	return dataArray[myLatIdx][myLonIdx]->z;
}