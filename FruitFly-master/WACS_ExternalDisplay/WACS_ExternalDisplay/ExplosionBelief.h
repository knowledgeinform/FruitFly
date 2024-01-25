#ifndef EXPLOSIONBELIEF_H
#define EXPLOSIONBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class ExplosionBelief
	\brief Class representing a WACS Swarm belief - created when an explosion is detected
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class ExplosionBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters
		*/
		ExplosionBelief ();

		/**
		\brief Constructor, sets specified parameters 
		\param timestampMs Timestamp of explosion detection
		\param centerLatDecDeg Center latitude decimal degrees of explosion
		\param centerLonDecDeg Center longitude decimal degrees of explosion
		\param altMslM Altitude meters MSL of explosion
		*/
		ExplosionBelief (long long timestampMs, double centerLatDecDeg, double centerLonDecDeg, double altMslM);

		/**
		\brief Destructor
		*/
		~ExplosionBelief ();

		/**
		\brief Destroys the static OpenGL objects
		\return void
		*/
		static void destroyGlObjects ();

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text when explosion occurs
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Display marker indicating 
		explosion has been detected if explosion belief is relatively recent

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
		\brief Accessor for timestamp milliseconds
		\return Timestamp milliseconds
		*/
		long long getMsgTimestampMs() {return m_MsgTimestampMs;}
		
		/**
		\brief Accessor for center latitude of explosion
		\return Center latitude decimal degrees of explosion
		*/
		double getCenterLatDecDeg () {return m_CenterLatDecDeg;}
		
		/**
		\brief Accessor for center longitude of explosion
		\return Center longitude decimal degrees of explosion
		*/
		double getCenterLonDecDeg () {return m_CenterLonDecDeg;}
		
		/**
		\brief Accessor for altitude of explosion
		\return Altitude MSL meters of explosion
		*/
		double getAltMslM () {return m_AltMslM;}

		/**
		\brief Modifier for timestamp milliseconds
		\param newMsgTimestampMs New timestamp milliseconds
		\return void
		*/
		void setMsgTimestampMs (long long newMsgTimestampMs) {m_MsgTimestampMs = newMsgTimestampMs;}
		
		/**
		\brief Modifier for center latitude of explosion
		\param centerLatDecDeg New center latitude decimal degrees of explosion
		\return void
		*/
		void setCenterLatDecDeg (double centerLatDecDeg) {m_CenterLatDecDeg = centerLatDecDeg;}
		
		/**
		\brief Modifier for center longitude of explosion
		\param centerLonDecDeg New center longitude decimal degrees of explosion
		\return void
		*/
		void setCenterLonDecDeg (double centerLonDecDeg) {m_CenterLonDecDeg = centerLonDecDeg;}
		
		/**
		\brief Modifier for altitude of explosion
		\param altMslM New altitude MSL meters of explosion
		\return void
		*/
		void setAltMslM (double altMslM) {m_AltMslM = altMslM;}

		
	private:

		long long m_MsgTimestampMs; //!< Timestamp milliseconds
		double m_CenterLatDecDeg; //!< Center latitude decimal degrees of circular orbit
		double m_CenterLonDecDeg; //!< Center longitude decimal degrees of circular orbit
		double m_AltMslM; //!< Altitude MSL meters of circular orbit
		
		double m_TextColorRed; //!< Red component of color of text
		double m_TextColorGreen; //!< Green component of color of text
		double m_TextColorBlue; //!< Blue component of color of text
		double m_TextColorAlpha; //!< Alpha component of color of text
		double m_TextFontSize; //!< Font size of explosion text
		double m_TextHorizontalPercentage; //!< Location of explosion text on screen, percentage from left
		double m_TextVerticalPercentage; //!< Location of explosion text on screen, percentage from top

		double m_MarkerColorRed; //!< Red component of color of marker
		double m_MarkerColorGreen; //!< Green component of color of marker
		double m_MarkerColorBlue; //!< Blue component of color of marker
		double m_MarkerColorAlpha; //!< Alpha component of color of marker
		double m_MarkerRadiusM; //!< Radius of sphere to indicate location of explosion

		int m_TimeToDisplayMessageSec; //!< Duration after explosion belief to display message


		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();

		/**
		\brief Initialize quadric object used to display OpenGL stuff
		\return void
		*/
		void initQuadric ();


		static GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying cloud location spheres
		static int displayList; //!< GL display list to display cloud location spheres
};


#endif