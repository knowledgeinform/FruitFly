#include "stdafx.h"
#include "DTEDPoint.h"
#include "math.h"
#include "Constants.h"


DTEDPoint::DTEDPoint() {
	
}

DTEDPoint::DTEDPoint (double newLat, double newLon, double newZ) {
	setLatLonZ(newLat, newLon, newZ);
}

void DTEDPoint::setLatLonZ(double newLat, double newLon, double newZ) {
	lat = newLat;
	lon = newLon;
	z = newZ;
}

