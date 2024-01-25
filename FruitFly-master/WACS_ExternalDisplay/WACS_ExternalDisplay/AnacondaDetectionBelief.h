#ifndef ANACONDADETECTIONBELIEF_H
#define ANACONDADETECTIONBELIEF_H

#include "stdafx.h"
#include "BeliefTemplate.h"


/**
	\class AnacondaDetectionBelief
	\brief Class representing a WACS Swarm belief - detections from Anaconda sensor
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class AnacondaDetectionBelief : public BeliefTemplate
{
	public:
		/**
		\brief Default constructor.  Clears detection message and parsed values
		*/
		AnacondaDetectionBelief ();

		/**
		\brief Constructor, sets detection message and parses values from it
		*/
		AnacondaDetectionBelief (char* detectionMessage);

		/**
		\brief Constructor, sets detection values
		*/
		AnacondaDetectionBelief (char* lcdaAgentID, int lcdaBars, char* lcdbAgentID, int lcdbBars);

		/**
		\brief Destructor
		*/
		~AnacondaDetectionBelief ();

		/**
		\brief Accessor for LCDA agent ID parsed from detection message
		\param retVal After call, will be populated with LCDA agent ID
		\return void
		*/
		void getLcdaAgentID (char retVal[]);

		/**
		\brief Accessor for LCDA bars parsed from detection message
		\return LCDA bars value, -1 if detection message wasn't set/is invalid
		*/
		int getLcdaBars () {return m_LcdaBars;}
		
		/**
		\brief Accessor for LCDB agent ID parsed from detection message
		\param retVal After call, will be populated with LCDB agent ID
		\return void
		*/
		void getLcdbAgentID (char retVal[]);
		
		/**
		\brief Accessor for LCDA bars parsed from detection message
		\return LCDA bars value, -1 if detection message wasn't set/is invalid
		*/
		int getLcdbBars () {return m_LcdbBars;}

		/**
		\brief Modifier for LCDA agent ID
		\param newVal Text to set for LCDA agent ID
		\return void
		*/
		void setLcdaAgentID (char newVal[]);

		/**
		\brief Modifier for LCDA Bars
		\return LCDA Bars
		*/
		void setLcdaBars (int newVal) {m_LcdaBars = newVal;}
		
		/**
		\brief Modifier for LCDB agent ID
		\param newVal Text to set for LCDB agent ID
		\return void
		*/
		void setLcdbAgentID (char newVal[]);
		
		/**
		\brief Modifier for LCDB Bars
		\return LCDB Bars
		*/
		void setLcdbBars (int newVal) {m_LcdbBars = newVal;}

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text of latest Anaconda detection message
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  AnacondaDetectionBelief currently
		doesn't display anything in OpenGL.  Positive detections are displayed using CloudDetectionBelief.

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

	private:

		//char m_DetectionMessage[256]; //!< Detection message text received from belief
		char m_LcdaAgentID [128]; //!< LCDA Agent ID text parsed from detection message
		int m_LcdaBars; //!< LCDA bars value parsed from detection message
		char m_LcdbAgentID [128]; //!< LCDB Agent ID text parsed from detection message
		int m_LcdbBars; //!< LCDB bars value parsed from detection message
		
		double m_HeaderColorRed; //!< Header text red color component
		double m_HeaderColorGreen; //!< Header text green color component
		double m_HeaderColorBlue; //!< Header text blue color component
		double m_HeaderColorAlpha; //!< Header text alpha color component

		double m_HeaderFontSize; //!< Header text font size
		double m_HeaderHorizontalPixels; //!< Header text horizontal pixels location from left edge
		double m_HeaderVerticalPixels; //!< Header text vertical pixels location from bottom edge

		double m_TextColorRed; //!< Text red color component
		double m_TextColorGreen; //!< Text green color component
		double m_TextColorBlue; //!< Text blue color component
		double m_TextColorAlpha; //!< Text alpha color component

		double m_RowFontSize; //!< Data text font size
		double m_RowHorizontalPixels; //!< Data text horizontal pixels location from left edge
		double m_Row1VerticalPixels; //!< Data text row1 vertical pixels location from bottom edge
		double m_Row2VerticalPixels; //!< Data text row2 vertical pixels location from bottom edge

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};

#endif