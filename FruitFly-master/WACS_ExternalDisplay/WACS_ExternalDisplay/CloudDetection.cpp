#include "stdafx.h"
#include "CloudDetection.h"

CloudDetection::CloudDetection()
{
	setMsgTimestampMs (0);
	setLatDecDeg (0);
	setLonDecDeg (0);
	setAltMslM (0);
	setValue (0);
	setSource (-1);
	setID (-1);
}

CloudDetection::CloudDetection (long msgTimestampMs, double latDecDeg, double lonDecDeg, double altMslM, float value, int source, int id)
{
	setMsgTimestampMs (msgTimestampMs);
	setLatDecDeg (latDecDeg);
	setLonDecDeg (lonDecDeg);
	setAltMslM (altMslM);
	setValue (value);
	setSource (source);
	setID (id);
}