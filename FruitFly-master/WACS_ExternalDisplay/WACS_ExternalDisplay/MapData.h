#ifndef MAPDATA_H
#define MAPDATA_H

#include "stdafx.h"
#include "DTEDObject.h"
#include "Geomap3D.h"

/**
	\class MapData
	\brief Object holding data about local terrain.  Has DTED data and geo-referenced terrain maps
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class MapData
{
	public:
		/**
		\brief Default constructor, clear variables
		*/
		MapData ();

		/**
		\brief Destructor, destroys objects
		*/
		~MapData();

		/**
		\brief Stores the DTEDObject for later use
		\param dted Loaded DTED data for a given region
		\return void
		*/
		void setDtedObject (DTEDObject* dted);

		/**
		\brief Loads terrain map image from file and generates a texture call list for display into
		an OpenGL scene.
		\param mapFilename Filename of image
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\return void
		*/
		void loadMap (char* mapFilename, AgentPosition* centerPoint);

		/**
		\brief Loads background terrain map image from file and generates a texture call list for display into
		an OpenGL scene.
		\param mapFilename Filename of image
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\return void
		*/
		void loadBackground (char* mapFilename, AgentPosition* centerPoint);

		/**
		\brief Calls the generated display list to draw the 3D terrain maps in an OpenGL scene.
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void paintMap (AgentPosition* centerPoint, double estLatToMConv, double estLonToMConv);

		/**
		\brief Return the minimum value of altitude (plus some buffer room) to be above ground at this location

		\param lat Latitude decmial degrees at point of interest
		\param lon Longitude decmial degrees at point of interest
		\return Minimum altitude MSL m to guarantee height above ground
		*/
		double getMinZ (double lat, double lon);

		/**
		\brief Accessor for DTED elevation value at a given location
		\param lat Latitude decimal degrees at point of interest
		\param lon Longitude decimal degrees at point of interest
		\return Elevation MSL m at point of interest
		*/
		double getTerrainZM (double lat, double lon);

	private:

		DTEDObject* m_DtedObject; //!< DTED Data loaded for map
		Geomap3D* m_MapObject; //!< Image data generated into textures for OpenGL display
		Geomap3D* m_BkgndMapObject; //!< Image data generated into textures for OpenGL display

		double m_BackgroundMapShiftMeters; //!< Meters to shift background map down (z) so it doesn't overlap main map image
};


#endif