#ifndef DTEDPOINT_H
#define DTEDPOINT_H

#include "stdafx.h"

/**
	\class DTEDPoint
	\author John Humphreys

	\brief A DTEDPoint object holds geolocation information about a specific point, including
	lat/lon and altitude

	\note Copyright (c) 2008 Johns Hopkins University
	\note Applied Physics Laboratory.  All Rights Reserved
*/
class DTEDPoint
{
	public:
		/**
		 \brief Decimal latitude of this point, degrees
		*/
		double lat;

		/**
		 \brief Decimal longitude of this point, degrees
		*/
		double lon;

		/**
		 \brief Height above sea level of this point, meters
		*/
		double z;

		
		
		
		/**
		 \brief Default constructor, no location set.  
		
		 \return
		*/
		DTEDPoint();

		/**
		 \brief Constructor with lat/lon/optional z inputs for this point.  
		 Calls calcXY and sets UTM locations for this point.
		
		 \param newLat - The latitude value to set this point at, specified between -90 and 90 degrees
		 \param newLon - The longitude value to set this point at, specified bewteen -180 and 180 degrees
		 \param newZ - The ASL height to set this point at, specified in meters
		 \return
		*/
		DTEDPoint (double newLat, double newLon, double newZ);

		/**
		 \brief Establishes values position from lat/lon/optional z inputs for this point.  Sets visibility to false.
		 Calls calcXY and sets UTM locations for this point.
		
		 \param newLat - The latitude value to set this point at, specified between -90 and 90 degrees
		 \param newLon - The longitude value to set this point at, specified bewteen -180 and 180 degrees
		 \param newZ - The ASL height to set this point at, specified in meters
		 \return
		*/
		void setLatLonZ(double newLat, double newLon, double newZ);

};


#endif