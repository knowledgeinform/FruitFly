#ifndef DTEDLOADER_H
#define DTEDLOADER_H

#include "stdafx.h"
#include "DTEDObject.h"

/**
	\class DTEDLoader
	\brief Static class to load DTED files into memory and store as a DTEDObject
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class DTEDLoader
{
	public:

		/**
		\brief Loads a DTED file containing the specified lat/lon points

		\param lat1 Bottom-left corner of region of interest to load DTED for.  Latitude decimal degrees
		\param lon1 Bottom-left corner of region of interest to load DTED for.  Longitude decimal degrees
		\param lat2 Upper-right corner of region of interest to load DTED for.  Latitude decimal degrees
		\param lon2 Upper-right corner of region of interest to load DTED for.  Longitude decimal degrees
		\param dtExt Filename extension (including the dot) of DTED files to load
		\param spacing Interval to load data.  If 2, load every other point.  If 3, load every 3rd point.  Used to downsample large areas for memory preservation
		\return DTEDObject pointer to DTED object
		*/
		static DTEDObject* loadDTED (double lat1, double lon1, double lat2, double lon2, char dtExt[10], int spacing);

	private:

		/**
		\brief Loads a DTED file from file and adds it to dtedObject.  If first file being loaded, dtedObject is populated with general
		data of the region to be loaded.

		\param filename Filename of DTED file to merge into dtedObject
		\param minLat Bottom-left corner of region of interest of dtedObject.  Latitude decimal degrees
		\param minLon Bottom-left corner of region of interest of dtedObject.  Longitude decimal degrees
		\param maxLat Upper-right corner of region of interest of dtedObject.  Latitude decimal degrees
		\param maxLon Upper-right corner of region of interest of dtedObject.  Longitude decimal degrees
		\param spacing Interval to load data.  If 2, load every other point.  If 3, load every 3rd point.  Used to downsample large areas for memory preservation
		\param dtedObject DTEDObject to fill with DTED elevation data.  Will have sizes and limits set the first time, then elevations udpated with each subsequent call with different files
		\param firstTile If true, this if the first tile generating data for dtedObject.  dtedObject needs its limits and sizes established
		\return DTEDObject pointer to DTED object
		*/
		static DTEDObject* analyzeDTED (char filename[], double minLat, double minLon, double maxLat, double maxLon, int spacing, DTEDObject* dtedObject, bool &firstTile);

		/**
		\brief Swaps bytes in a short value
		\param val short value to swap
		\return void
		*/
		static void swapShort (short &val);

		/**
		\brief Get filename for dted file
		\param latInt Integer latitude value of Southern side of DTED tile
		\param lonInt Integer longitude value of Western side of DTED tile
		\param filename After call, will have the filename of the desired DTED file.
		\return void
		*/
		static void getDtedTileFilename (int latInt, int lonInt, char filename[]);
};

#endif