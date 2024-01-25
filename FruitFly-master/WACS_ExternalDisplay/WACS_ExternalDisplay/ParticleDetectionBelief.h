#ifndef PARTICLEDETECTIONBELIEF_H
#define PARTICLEDETECTIONBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"
#include "AgentPosition.h"

/**
	\class ParticleDetectionBelief
	\brief Class representing a WACS Swarm belief - detections from particle sensor
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class ParticleDetectionBelief : public BeliefTemplate
{
	public:
		/**
		\brief Default constructor.  Clears detection message and parsed values
		*/
		ParticleDetectionBelief ();

		/**
		\brief Constructor, sets detection message and parses values from it
		*/
		ParticleDetectionBelief (char* detectionMessage);
		
		/**
		\brief Constructor, sets detection values
		*/
		ParticleDetectionBelief (int largeCounts, int smallCounts, int bioLargeCounts, int bioSmallCounts);
		
		/**
		\brief Destructor
		*/
		~ParticleDetectionBelief ();

		/**
		\brief Accessor for parsed large counts value
		\return Large counts
		*/
		int getLargeCounts () {return m_LargeCounts;}
		
		/**
		\brief Accessor for parsed small counts value
		\return Small counts
		*/
		int getSmallCounts () {return m_SmallCounts;}
		
		/**
		\brief Accessor for parsed large bio counts value
		\return Large bio counts
		*/
		int getBioLargeCounts () {return m_BioLargeCounts;}
		
		/**
		\brief Accessor for parsed small bio counts value
		\return Small bio counts
		*/
		int getBioSmallCounts () {return m_BioSmallCounts;}

		/**
		\brief Modifier for parsed large counts value
		\param counts New large counts value
		\return void
		*/
		void setLargeCounts (int counts) {m_LargeCounts = counts;}
		
		/**
		\brief Modifier for parsed small counts value
		\param counts New small counts value
		\return void
		*/
		void setSmallCounts (int counts) {m_SmallCounts = counts;}
		
		/**
		\brief Modifier for parsed large bio counts value
		\param counts New large bio counts value
		\return void
		*/
		void setBioLargeCounts (int counts) {m_BioLargeCounts = counts;}
		
		/**
		\brief Modifier for parsed small bio counts value
		\param counts New small bio counts value
		\return void
		*/
		void setBioSmallCounts (int counts) {m_BioSmallCounts = counts;}

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text of latest Ibac detection message
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  ParticleDetectionBelief currently
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

		int m_LargeCounts; //!< Large counts parsed from detection message
		int m_SmallCounts; //!< Small counts parsed from detection message
		int m_BioLargeCounts; //!< Large Bio counts parsed from detection message
		int m_BioSmallCounts; //!< Small Bio counts parsed from detection message

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
		\brief Parse detection message and extract particle counts information
		\return void
		*/
		//void parseDetectionMessage ();

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};


#endif