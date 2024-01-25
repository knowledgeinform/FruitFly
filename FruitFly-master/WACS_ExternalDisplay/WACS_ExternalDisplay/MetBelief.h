#ifndef METBELIEF_H
#define METBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class MetBelief
	\brief Class representing a WACS Swarm belief - represents the current wind vector estimation
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class MetBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters
		*/
		MetBelief ();

		/**
		\brief Constructor, sets specified parameters 
		\param windBearingFromDeg Bearing wind is blowing from
		\param windSpeedMps wind speed, meters per second
		\param tempF temperature, Fahrenheit
		*/
		MetBelief (double windBearingFromDeg, double windSpeedMps, double tempF);

		/**
		\brief Destructor
		*/
		~MetBelief ();

		/**
		\brief Destroys the static OpenGL objects
		\return void
		*/
		static void destroyGlObjects ();

		/**
		\brief Sets camera angles of current GL scene.  Used to properly orient GL overlay (compass pointing direction)
		\param pitchDegrees Current pitch degrees of camera
		\param headingDegrees Current heading degrees of camera
		\return void
		*/
		void setCameraAngles (double pitchDegrees, double headingDegrees);

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws compass rose with indicated wind vector
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		void drawGlOverlay (int glWidth, int glHeight);


		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Does nothing

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
		\brief Accessor for wind bearing
		\return Wind bearing, from direction, degrees
		*/
		double getWindBearingFromDeg() {return m_WindBearingFromDeg;}
		
		/**
		\brief Accessor for wind speed
		\return Wind speed, meters per second
		*/
		double getWindSpeedMps() {return m_WindSpeedMps;}
		
		/**
		\brief Accessor for temperature
		\return Temperature, Fahrenheit degrees
		*/
		double getTemperatureF() {return m_TemperatureF;}

		/**
		\brief Accessor for wind data has been defined
		\return True if wind data has been defined, false if wind data is invalid
		*/
		bool getIsMetDefined () {return m_MetDefined;}
		

		/**
		\brief Modifier for wind bearing
		\param newWindBearingFromDeg New wind bearing, from direction, degrees
		\return void
		*/
		void setWindBearingFromDeg (double newWindBearingFromDeg) {m_WindBearingFromDeg = newWindBearingFromDeg;}

		/**
		\brief Modifier for wind speed
		\param m_WindSpeedMps New wind speed, meters per second
		\return void
		*/
		void setWindSpeedMps(double newWindSpeedMps) {m_WindSpeedMps = newWindSpeedMps;}
		
		/**
		\brief Modifier for temperature
		\param m_TemperatureF New temperature, Fahrenheit degrees
		\return void
		*/
		void setTemperatureF(double newTemperatureF) {m_TemperatureF = newTemperatureF;}


	private:

		double m_WindBearingFromDeg; //!< Center latitude decimal degrees of circular orbit
		double m_WindSpeedMps; //!< Center longitude decimal degrees of circular orbit
		double m_TemperatureF; //!< Altitude MSL meters of circular orbit
		bool m_MetDefined; //!< If true, MET data is defined and will be painted in GL
		
		double m_GlPitchDegrees; //!< Current pitch of camera in OpenGL scene
		double m_GlHeadingDegrees; //!< Current heading of camera in OpenGL scene
		

		int m_ArrowLengthPixels; //!< Length of arrow for direction vectors, in pixels
		int m_ArrowBackRadiusPixels; //!< Radius of back end of direction vectors, in pixels
		int m_CenterPixelsFromRightEdge; //!< Pixels from right edge of screen to put center of compass rose
		int m_CenterPixelsFromTopEdge; //!< Pixels from top edge of screen to put center of compass rose
		int m_DirLineLengthPixels; //!< Pixel length of direction vectors in compass rose
		int m_CompassLineWidth; //!< Width of compass lines
		int m_WindLineWidth; //!< Width of wind bearing line
		double m_TextFontSize; //!< Font size of explosion text
		int m_TextHorizOffsetPix; //!< Offset left of left edge of compass rose for MET text
		int m_TextVertOffsetPix; //!< Offset down from bottom edge of compass rose for MET text

		double m_CompassColorRed; //!< Red component of color of compass
		double m_CompassColorGreen; //!< Green component of color of compass
		double m_CompassColorBlue; //!< Blue component of color of compass
		double m_CompassColorAlpha; //!< Alpha component of color of compass
		double m_WindColorRed; //!< Red component of color of wind vector
		double m_WindColorGreen; //!< Green component of color of wind vector
		double m_WindColorBlue; //!< Blue component of color of wind vector
		double m_WindColorAlpha; //!< Alpha component of color of wind vector

		double m_ShadedBoxColorRed; //!< Shaded box text red color component
		double m_ShadedBoxColorGreen; //!< Shaded box text green color component
		double m_ShadedBoxColorBlue; //!< Shaded box text blue color component
		double m_ShadedBoxColorAlpha; //!< Shaded box text alpha color component
		double m_ShadedBoxHeightPixels; //!< Shaded box pixels height from bottom edge
		double m_ShadedBoxWidthPixels; //!< Shaded box pixels width from bottom edge

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


		static GLUquadricObj *quadricObj; //!< GL Quadric object to use for displaying pointing arrow (cone)
		static int displayList; //!< GL display list to display pointing arrow (cone)
};


#endif