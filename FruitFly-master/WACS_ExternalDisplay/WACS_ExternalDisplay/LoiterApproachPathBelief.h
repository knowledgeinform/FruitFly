#ifndef LOITERAPPROACHPATHBELIEF_H
#define LOITERAPPROACHPATHBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class LoiterApproachPathBelief
	\brief Class representing a WACS Swarm belief - approach path from loiter orbit for UAV
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class LoiterApproachPathBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters and initializes GLUquadricObj
		*/
		LoiterApproachPathBelief ();

		/**
		\brief Constructor, sets specified parameters and initializes GLUquadricObj
		\param firstRangeLatDecDeg Latitude decimal degrees of first range position on approach path
		\param firstRangeLonDecDeg Longitude decimal degrees of first range position on approach path 
		\param firstRangeAltMslM Altitude meters MSL of first range position on approach path
		\param contactLatDecDeg Latitude decimal degrees of desired contact point on approach path
		\param contactLonDecDeg Longitude decimal degrees of desired contact point on approach path 
		\param contactAltMslM Altitude meters MSL of desired contact point on approach path
		\param safeRangeLatDecDeg Latitude decimal degrees of minimum safe distance point on approach path
		\param safeRangeLonDecDeg Longitude decimal degrees of minimum safe distance point on approach path 
		\param safeRangeAltMslM Altitude meters MSL of minimum safe distance point on approach path
		\param isPathValid Boolean whether path is valid (safe) or not
		*/
		LoiterApproachPathBelief (double firstRangeLatDecDeg, double firstRangeLonDecDeg, double firstRangeAltMslM,
									double contactLatDecDeg, double contactLonDecDeg, double contactAltMslM,
									double safeRangeLatDecDeg, double safeRangeLonDecDeg, double safeRangeAltMslM,
									bool isPathValid);

		/**
		\brief Destructor, kills GLUquadricObj
		*/
		~LoiterApproachPathBelief ();


		/**
		\brief Set parameters for the circular orbit
		\param firstRangeLatDecDeg Latitude decimal degrees of first range position on approach path
		\param firstRangeLonDecDeg Longitude decimal degrees of first range position on approach path 
		\param firstRangeAltMslM Altitude meters MSL of first range position on approach path
		\param contactLatDecDeg Latitude decimal degrees of desired contact point on approach path
		\param contactLonDecDeg Longitude decimal degrees of desired contact point on approach path 
		\param contactAltMslM Altitude meters MSL of desired contact point on approach path
		\param safeRangeLatDecDeg Latitude decimal degrees of minimum safe distance point on approach path
		\param safeRangeLonDecDeg Longitude decimal degrees of minimum safe distance point on approach path 
		\param safeRangeAltMslM Altitude meters MSL of minimum safe distance point on approach path
		\param isPathValid Boolean whether path is valid (safe) or not
		\return void
		*/
		bool setLoiterApproachPath (double firstRangeLatDecDeg, double firstRangeLonDecDeg, double firstRangeAltMslM,
									double contactLatDecDeg, double contactLonDecDeg, double contactAltMslM,
									double safeRangeLatDecDeg, double safeRangeLonDecDeg, double safeRangeAltMslM,
									bool isPathValid);
		
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
		\brief Accessor for latitude of first range point on approach path
		\return Latitude decimal degrees of first range point on approach path
		*/
		double getFirstRangeLatDecDeg () {return m_FirstRangeLatDecDeg;}
		
		/**
		\brief Accessor for longitude of first range point on approach path
		\return Longitude decimal degrees of first range point on approach path
		*/
		double getFirstRangeDecDeg () {return m_FirstRangeLonDecDeg;}
		
		/**
		\brief Accessor for altitude of first range point on approach path
		\return Altitude MSL meters of first range point on approach path
		*/
		double getFirstRangeAltMslM () {return m_FirstRangeAltMslM;}

		/**
		\brief Accessor for latitude of desired contact point on approach path
		\return Latitude decimal degrees of desired contact point on approach path
		*/
		double getContactLatDecDeg () {return m_ContactLatDecDeg;}
		
		/**
		\brief Accessor for longitude of desired contact point on approach path
		\return Longitude decimal degrees of desired contact point on approach path
		*/
		double getContactDecDeg () {return m_ContactLonDecDeg;}
		
		/**
		\brief Accessor for altitude of desired contact point on approach path
		\return Altitude MSL meters of desired contact point on approach path
		*/
		double getContactAltMslM () {return m_ContactAltMslM;}

		/**
		\brief Accessor for latitude of minimum safe distance point on approach path
		\return Latitude decimal degrees of minimum safe distance point on approach path
		*/
		double getSafeLatDecDeg () {return m_SafeLatDecDeg;}
		
		/**
		\brief Accessor for longitude of minimum safe distance point on approach path
		\return Longitude decimal degrees of minimum safe distance point on approach path
		*/
		double getSafeDecDeg () {return m_SafeLonDecDeg;}
		
		/**
		\brief Accessor for altitude of minimum safe distance point on approach path
		\return Altitude MSL meters of minimum safe distance point on approach path
		*/
		double getSafeAltMslM () {return m_SafeAltMslM;}

		/**
		\brief Accessor for whether path is valid
		\return True if approach path is valid, false if invalid (not safe)
		*/
		bool getIsPathValid () {return m_IsPathValid;}

		
		
		
		
		/**
		\brief Modifier for latitude of first range point on approach path
		\param firstRangeLatDecDeg New latitude decimal degrees of first range point on approach path
		\return void
		*/
		void setFirstRangeLatDecDeg (double firstRangeLatDecDeg) {m_FirstRangeLatDecDeg = firstRangeLatDecDeg;}
		
		/**
		\brief Modifier for longitude of first range point on approach path
		\param firstRangeLonDecDeg New longitude decimal degrees of first range point on approach path
		\return void
		*/
		void setFirstRangeLonDecDeg (double firstRangeLonDecDeg) {m_FirstRangeLonDecDeg = firstRangeLonDecDeg;}
		
		/**
		\brief Modifier for altitude of first range point on approach path
		\param firstRangeAltMslM New altitude MSL meters of first range point on approach path
		\return void
		*/
		void setFirstRangeAltMslM (double firstRangeAltMslM) {m_FirstRangeAltMslM = firstRangeAltMslM;}

		/**
		\brief Modifier for latitude of desired contact point on approach path
		\param contactLatDecDeg New latitude decimal degrees of desired contact point on approach path
		\return void
		*/
		void setContactLatDecDeg (double contactLatDecDeg) {m_ContactLatDecDeg = contactLatDecDeg;}
		
		/**
		\brief Modifier for longitude of desired contact point on approach path
		\param contactLonDecDeg New longitude decimal degrees of desired contact point on approach path
		\return void
		*/
		void setContactLonDecDeg (double contactLonDecDeg) {m_ContactLonDecDeg = contactLonDecDeg;}
		
		/**
		\brief Modifier for altitude of desired contact point on approach path
		\param contactAltMslM New altitude MSL meters of desired contact point on approach path
		\return void
		*/
		void setContactAltMslM (double contactAltMslM) {m_ContactAltMslM = contactAltMslM;}

		/**
		\brief Modifier for latitude of minimum safe distance point point on approach path
		\param safeLatDecDeg New latitude decimal degrees of minimum safe distance point point on approach path
		\return void
		*/
		void setSafeLatDecDeg (double safeLatDecDeg) {m_SafeLatDecDeg = safeLatDecDeg;}
		
		/**
		\brief Modifier for longitude of minimum safe distance point point on approach path
		\param safeLonDecDeg New longitude decimal degrees of minimum safe distance point point on approach path
		\return void
		*/
		void setSafeLonDecDeg (double safeLonDecDeg) {m_SafeLonDecDeg = safeLonDecDeg;}
		
		/**
		\brief Modifier for altitude of minimum safe distance point point on approach path
		\param safeAltMslM New altitude MSL meters of minimum safe distance point point on approach path
		\return void
		*/
		void setSafeAltMslM (double safeAltMslM) {m_SafeAltMslM = safeAltMslM;}

		/**
		\brief Modifier for whether path is valid
		\param isClockwise New circular orbit rotation direction; true=clockwise, false=counter-clockwise
		\return void
		*/
		void setIsPathValid (bool isPathValid) {m_IsPathValid = isPathValid;}

		
	private:

		double m_FirstRangeLatDecDeg; //!< Latitude decimal degrees of first range point on approach path
		double m_FirstRangeLonDecDeg; //!< Longitude decimal degrees of first range point on approach path
		double m_FirstRangeAltMslM; //!< Altitude MSL meters of first range point on approach path
		double m_ContactLatDecDeg; //!< Latitude decimal degrees of desired contact point on approach path
		double m_ContactLonDecDeg; //!< Longitude decimal degrees of desired contact point on approach path
		double m_ContactAltMslM; //!< Altitude MSL meters of desired contact point on approach path
		double m_SafeLatDecDeg; //!< Latitude decimal degrees of minimum safe distance point point on approach path
		double m_SafeLonDecDeg; //!< Longitude decimal degrees of minimum safe distance point point on approach path
		double m_SafeAltMslM; //!< Altitude MSL meters of minimum safe distance point point on approach path
		bool m_IsPathValid; //!< Whether path is valid (safe) or not; true=valid, false=invalid
		
		double m_ValidMarkerColorRed; //!< Red component of color of valid approach path point
		double m_ValidMarkerColorGreen; //!< Green component of color of valid approach path point
		double m_ValidMarkerColorBlue; //!< Blue component of color of valid approach path point
		double m_ValidMarkerColorAlpha; //!< Alpha component of color of valid approach path point
		double m_ValidMarkerRadiusM; //!< Radius of valid approach path point marker, meters
		
		GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying spinning arrows
		int displayList; //!< GL display list to display approach path point marker

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};


#endif