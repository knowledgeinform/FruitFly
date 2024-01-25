#ifndef DTEDOBJECT_H
#define DTEDOBJECT_H

#include "stdafx.h"
#include "DTEDPoint.h"

/**
	\class DTEDObject
	\author John Humphreys

	\brief An object to hold the entirety of the data from a DTED dataset, including a list of
	DTEDPoint objects to represent each point and some general information about the dataset.

	\note Copyright (c) 2008 Johns Hopkins University
	\note Applied Physics Laboratory.  All Rights Reserved
*/
class DTEDObject 
{
	friend class DTEDLoader;

	public:
		
		/**
		 \brief Default constructor, no parameters set.
		
		 \return
		*/
		DTEDObject();

		/**
		\brief Destructor
		*/
		~DTEDObject ();

		/**
		\brief Return the minimum value of altitude (plus some buffer room) to be above ground at this location

		\param latitude Latitude decmial degrees at point of interest
		\param longitude Longitude decmial degrees at point of interest
		\return Minimum altitude MSL m to guarantee height above ground
		*/
		double getMinZ (double latitude, double longitude);		

		/**
		\brief Set altitude value at a given location
		\param latDecDeg Latitude decimal degrees at point to set altitude for
		\param lonDecDeg Longitude decimal degrees at point to set altitude for
		\param elevM Elevation MSL m to set
		*/
		bool setZ (double latDecDeg, double lonDecDeg, double elevM);

		/**
		\brief Accessor for DTED elevation value at a given location
		\param latDecDeg Latitude decimal degrees at point of interest
		\param lonDecDeg Longitude decimal degrees at point of interest
		\return Elevation MSL m at point of interest
		*/
		double getZ (double latDecDeg, double lonDecDeg);

		/**
		\brief Accessor for DTED elevation value at a given index to the internal DTED array
		\param latIdx Latitude index at point of interest
		\param lonIdx Longitude index at point of interest
		\return Elevation MSL m at point of interest
		*/
		double getZ (int latIdx, int lonIdx);
		
		
		/**
		 \brief DTED file that this DTEDData object has/will read from.
		*/
		char filename[1024];

		/** 
		 \brief Longitude decimal degrees position of the anchor point for this data set
		*/
		double anchorLon;

		/** 
		 \brief Latitude decimal degrees position of the anchor point for this data set
		*/
		double anchorLat;

		/**
		 \brief Number of longitude points in the dataset
		*/
		int numLon;

		/**
		 \brief Number of latitude points in the dataset
		*/
		int numLat;

		/**
		 \brief Longitude decimal degrees resolution of data points, degrees
		*/
		double resLon;

		/**
		 \brief Latitude decimal degrees reslolution of data points, degrees
		*/
		double resLat;

		/**
		\brief Offset m to terrain data when requesting minimum terrain data
		*/
		double m_MinZOffsetM;

	private:

		/**
		 \brief List of DTEDPoint objects that represent each datapoint from the DTED dataset
		*/
		DTEDPoint*** dataArray;
		
};

#endif