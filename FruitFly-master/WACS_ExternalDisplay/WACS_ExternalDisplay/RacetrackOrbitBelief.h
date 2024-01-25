#ifndef RACETRACKORBITBELIEF_H
#define RACETRACKORBITBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class RacetrackOrbitBelief
	\brief Class representing a WACS Swarm belief - desired racetrack loiter orbit for UAV
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class RacetrackOrbitBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters and initializes GLUquadricObj
		*/
		RacetrackOrbitBelief ();

		/**
		\brief Constructor, sets specified parameters and initializes GLUquadricObj
		\param lat1DecDeg Latitude decimal degrees of 1st point in orbit
		\param lon1DecDeg Longitude decimal degrees of 1st point in orbit
		\param lat2DecDeg Latitude decimal degrees of 2nd point in orbit
		\param lon2DecDeg Longitude decimal degrees of 2nd point in orbit
		\param radiusM Radius meters of circular orbit
		\param finalAltMslM Final altitude meters MSL of circular orbit
		\param standoffAltMslM Standoff altitude meters MSL of circular orbit
		\param isClockwise If true, orbit is clockwise.  If false, orbit is counter-clockwise
		*/
		RacetrackOrbitBelief (double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg, double radiusM, double finalAltMslM, double standoffAltMslM, bool isClockwise);

		/**
		\brief Destructor, kills GLUquadricObj
		*/
		~RacetrackOrbitBelief ();


		/**
		\brief Set parameters for the racetrack orbit
		\param lat1DecDeg Latitude decimal degrees of 1st point in orbit
		\param lon1DecDeg Longitude decimal degrees of 1st point in orbit
		\param lat2DecDeg Latitude decimal degrees of 2nd point in orbit
		\param lon2DecDeg Longitude decimal degrees of 2nd point in orbit
		\param radiusM Radius meters of circular orbit
		\param finalAltMslM Final altitude meters MSL of circular orbit
		\param standoffAltMslM Standoff altitude meters MSL of circular orbit
		\param isClockwise If true, orbit is clockwise.  If false, orbit is counter-clockwise
		\return void
		*/
		bool setRacetrackOrbit (double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg, double radiusM, double finalAltMslM, double standoffAltMslM, bool isClockwise);
		
		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Display circle at orbit path with rotating
		arrows showing direction of orbit

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv);

		/**
		\brief Update RawDataDialog object with data from this belief.  Rows of dialog are filled with 
		contents of private variables.
		\param dataRows List of data rows in a RawDataDialog object to be updated with data for this belief
		\return void
		*/
		void updateDataRows (DataRows* dataRows);

		/**
		\brief Return if the defined racetrack is a circle, not an oval.  If a circle, point 1 and point 2 will be equal.
		\return True if racetrack is a circle, otherwise false
		*/
		bool isCircleRacetrack();

		/**
		\brief Accessor for 1st point latitude of orbit
		\return 1st point latitude decimal degrees of orbit
		*/
		double getLat1DecDeg () {return m_Lat1DecDeg;}
		
		/**
		\brief Accessor for 1st point longitude of orbit
		\return 1st point longitude decimal degrees of orbit
		*/
		double getLon1DecDeg () {return m_Lon1DecDeg;}
		
		/**
		\brief Accessor for 2nd point latitude of orbit
		\return 2nd point latitude decimal degrees of orbit
		*/
		double getLat2DecDeg () {return m_Lat2DecDeg;}
		
		/**
		\brief Accessor for 2nd point longitude of orbit
		\return 2nd point longitude decimal degrees of orbit
		*/
		double getLon2DecDeg () {return m_Lon2DecDeg;}
		
		/**
		\brief Accessor for radius of orbit
		\return Radius meters of orbit
		*/
		double getRadiusM () {return m_RadiusM;}
		
		/**
		\brief Accessor for standoff altitude of orbit
		\return Standoff altitude MSL meters of orbit
		*/
		double getStandoffAltMslM () {return m_StandoffAltMslM;}

		/**
		\brief Accessor for final altitude of orbit
		\return Final altitude MSL meters of orbit
		*/
		double getFinalAltMslM () {return m_FinalAltMslM;}

		/**
		\brief Accessor for rotation direction of orbit
		\return True if orbit is clockwise, false if orbit is counter-clockwise
		*/
		bool getIsClockwise () {return m_IsClockwise;}


		/**
		\brief Modifier for 1st point latitude of orbit
		\param latDecDeg New 1st point latitude decimal degrees of orbit
		\return void
		*/
		void setLat1DecDeg (double latDecDeg) {m_Lat1DecDeg = latDecDeg;}
		
		/**
		\brief Modifier for 1st point longitude of orbit
		\param lonDecDeg New 1st point longitude decimal degrees of orbit
		\return void
		*/
		void setLon1DecDeg (double lonDecDeg) {m_Lon1DecDeg = lonDecDeg;}

		/**
		\brief Modifier for 2nd point latitude of orbit
		\param latDecDeg New 2nd point latitude decimal degrees of orbit
		\return void
		*/
		void setLat2DecDeg (double latDecDeg) {m_Lat2DecDeg = latDecDeg;}
		
		/**
		\brief Modifier for 2nd point longitude of orbit
		\param lonDecDeg New 2nd point longitude decimal degrees of orbit
		\return void
		*/
		void setLon2DecDeg (double lonDecDeg) {m_Lon2DecDeg = lonDecDeg;}
		
		/**
		\brief Modifier for radius of orbit
		\param radiusM New radius meters of orbit
		\return void
		*/
		void setRadiusM (double radiusM) {m_RadiusM = radiusM;}
		
		/**
		\brief Modifier for final altitude of orbit
		\param altMslM New final altitude MSL meters of orbit
		\return void
		*/
		void setFinalAltMslM (double altMslM) {m_FinalAltMslM = altMslM;}

		/**
		\brief Modifier for standoff altitude of orbit
		\param altMslM New standoff altitude MSL meters of orbit
		\return void
		*/
		void setStandoffAltMslM (double altMslM) {m_StandoffAltMslM = altMslM;}

		/**
		\brief Modifier for circular orbit direction
		\param isClockwise New circular orbit rotation direction; true=clockwise, false=counter-clockwise
		\return void
		*/
		void setIsClockwise (bool isClockwise) {m_IsClockwise = isClockwise;}

		
	private:

		double m_Lat1DecDeg; //!< Latitude decimal degrees of point 1 of orbit
		double m_Lon1DecDeg; //!< Longitude decimal degrees of point 1 of orbit
		double m_Lat2DecDeg; //!< Latitude decimal degrees of point 2 of orbit
		double m_Lon2DecDeg; //!< Longitude decimal degrees of point 2 of orbit
		double m_RadiusM; //!< Radius meters of circular orbit
		double m_FinalAltMslM; //!< Altitude MSL meters of standoff orbit
		double m_StandoffAltMslM; //!< Altitude MSL meters of final orbit
		bool m_IsClockwise; //!< Circular orbit rotation direction; true=clockwise, false=counter-clockwise
		
		double m_RadiusLat; //!< Radius of circular orbit, latitude decimal degrees distance from center
		double m_RadiusLon; //!< Radius of circular orbit, longitude decimal degrees distance from center

		int m_ArrowCount; //!< Number of arrows to spin around the orbit to show direction.
		int m_ArrowOrbitPeriodSec; //!< Seconds for each spinning arrow to traverse entire orbit
		double m_ArrowBackRadiusM; //!< Radius meters of backend of spinning arrows
		double m_ArrowLengthM; //!< Length meters of spinning arrows
		double m_ArrowColorRed; //!< Red component of color of spinning arrows
		double m_ArrowColorGreen; //!< Green component of color of spinning arrows
		double m_ArrowColorBlue; //!< Blue component of color of spinning arrows
		double m_ArrowColorAlpha; //!< Alpha component of color of spinning arrows
		double m_OrbitColorRed; //!< Red component of color of orbit line
		double m_OrbitColorGreen; //!< Green component of color of orbit line
		double m_OrbitColorBlue; //!< Blue component of color of orbit line
		double m_OrbitLineWidth; //!< Line width of orbit line
		
		static int m_ArrowAngleDeg; //!< Rotation angle from 0 to spin arrows around the orbit.
		static long m_ArrowStartTimeSec; //!< Timestamp seconds when spinning arrows started at 0 degrees rotation
		
		GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying spinning arrows
		int displayList; //!< GL display list to display spinning arrows

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};


#endif