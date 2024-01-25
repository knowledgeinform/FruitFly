#ifndef CIRCULARORBITBELIEF_H
#define CIRCULARORBITBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class CircularOrbitBelief
	\brief Class representing a WACS Swarm belief - desired circular orbit for UAV
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class CircularOrbitBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters and initializes GLUquadricObj
		*/
		CircularOrbitBelief ();

		/**
		\brief Constructor, sets specified parameters and initializes GLUquadricObj
		\param centerLatDecDeg Center latitude decimal degrees of circular orbit
		\param centerLonDecDeg Center longitude decimal degrees of circular orbit
		\param radiusM Radius meters of circular orbit
		\param altMslM Altitude meters MSL of circular orbit
		\param isClockwise If true, orbit is clockwise.  If false, orbit is counter-clockwise
		*/
		CircularOrbitBelief (double centerLatDecDeg, double centerLonDecDeg, double radiusM, double altMslM, bool isClockwise);

		/**
		\brief Destructor, kills GLUquadricObj
		*/
		~CircularOrbitBelief ();


		/**
		\brief Set parameters for the circular orbit
		\param centerLatDecDeg Center latitude decimal degrees of circular orbit
		\param centerLonDecDeg Center longitude decimal degrees of circular orbit
		\param radiusM Radius meters of circular orbit
		\param altMslM Altitude meters MSL of circular orbit
		\param isClockwise If true, orbit is clockwise.  If false, orbit is counter-clockwise
		\return void
		*/
		bool setCircularOrbit (double centerLatDecDeg, double centerLonDecDeg, double radiusM, double altMslM, bool isClockwise);
		
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
		\brief Accessor for center latitude of circular orbit
		\return Center latitude decimal degrees of circular orbit
		*/
		double getCenterLatDecDeg () {return m_CenterLatDecDeg;}
		
		/**
		\brief Accessor for center longitude of circular orbit
		\return Center longitude decimal degrees of circular orbit
		*/
		double getCenterLonDecDeg () {return m_CenterLonDecDeg;}
		
		/**
		\brief Accessor for radius of circular orbit
		\return Radius meters of circular orbit
		*/
		double getRadiusM () {return m_RadiusM;}
		
		/**
		\brief Accessor for altitude of circular orbit
		\return Altitude MSL meters of circular orbit
		*/
		double getAltMslM () {return m_AltMslM;}

		/**
		\brief Accessor for rotation direction of circular orbit
		\return True if orbit is clockwise, false if orbit is counter-clockwise
		*/
		bool getIsClockwise () {return m_IsClockwise;}

		/**
		\brief Modifier for center latitude of circular orbit
		\param centerLatDecDeg New center latitude decimal degrees of circular orbit
		\return void
		*/
		void setCenterLatDecDeg (double centerLatDecDeg) {m_CenterLatDecDeg = centerLatDecDeg;}
		
		/**
		\brief Modifier for center longitude of circular orbit
		\param centerLonDecDeg New center longitude decimal degrees of circular orbit
		\return void
		*/
		void setCenterLonDecDeg (double centerLonDecDeg) {m_CenterLonDecDeg = centerLonDecDeg;}
		
		/**
		\brief Modifier for radius of circular orbit
		\param radiusM New radius meters of circular orbit
		\return void
		*/
		void setRadiusM (double radiusM) {m_RadiusM = radiusM;}
		
		/**
		\brief Modifier for altitude of circular orbit
		\param altMslM New altitude MSL meters of circular orbit
		\return void
		*/
		void setAltMslM (double altMslM) {m_AltMslM = altMslM;}

		/**
		\brief Modifier for circular orbit direction
		\param isClockwise New circular orbit rotation direction; true=clockwise, false=counter-clockwise
		\return void
		*/
		void setIsClockwise (bool isClockwise) {m_IsClockwise = isClockwise;}

		
	private:

		double m_CenterLatDecDeg; //!< Center latitude decimal degrees of circular orbit
		double m_CenterLonDecDeg; //!< Center longitude decimal degrees of circular orbit
		double m_RadiusM; //!< Radius meters of circular orbit
		double m_AltMslM; //!< Altitude MSL meters of circular orbit
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