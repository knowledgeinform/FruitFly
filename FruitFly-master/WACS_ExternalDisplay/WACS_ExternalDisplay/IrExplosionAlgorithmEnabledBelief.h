#ifndef IREXPLOSIONALGORITHMENABLEDBELIEF_H
#define IREXPLOSIONALGORITHMENABLEDBELIEF_H


#include "stdafx.h"
#include "BeliefTemplate.h"

/**
	\class IrExplosionAlgorithmEnabledBelief
	\brief Class representing a WACS Swarm belief - holds time until expected explosion
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class IrExplosionAlgorithmEnabledBelief : public BeliefTemplate
{
	public:

		/**
		\brief Default Constructor.  Sets empty parameters
		*/
		IrExplosionAlgorithmEnabledBelief ();

		/**
		\brief Constructor, sets specified parameters 
		\param enabled If true, IR detection algorithms are enabled
		\param timeUntilExplosionMs Time until explosion, milliseconds
		*/
		IrExplosionAlgorithmEnabledBelief (bool enabled, double timeUntilExplosionMs);

		/**
		\brief Destructor
		*/
		~IrExplosionAlgorithmEnabledBelief ();

		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.  Draws text of explosion time
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight);

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
		\brief Accessor for time until exlposion
		\return Timestamp milliseconds until explosion
		*/
		long long getTimeUntilExplosionMs() {return m_TimeUntilExplosionMs;}

		/**
		\brief Accessor for whether IR algorithm is enabled
		\return True if IR explosion detection algorithms are enabled
		*/
		bool getEnabled () {return m_Enabled;}
		
		/**
		\brief Modifier for timestamp until explosion
		\param newTimeUntilExplosionMs New timestamp milliseconds until explosion
		\return void
		*/
		void setTimeUntilExplosionMs (long long newTimeUntilExplosionMs) {m_TimeUntilExplosionMs = newTimeUntilExplosionMs;}

		/**
		\brief Modifier for whether IR algorithm is enabled
		\param new enabled boolean
		\return void
		*/
		void setEnabled (bool newEnabled) {m_Enabled = newEnabled;}
		
	private:

		bool m_Enabled; //!< If true, IR explosion detection algorithms are active
		long long m_TimeUntilExplosionMs; //!< Milliseconds until expected explosion
		
		double m_TextColorRed; //!< Red component of color of text
		double m_TextColorGreen; //!< Green component of color of text
		double m_TextColorBlue; //!< Blue component of color of text
		double m_TextColorAlpha; //!< Alpha component of color of text
		double m_TextFontSize; //!< Font size of explosion text
		double m_TextHorizontalPixels; //!< Location of explosion text on screen, pixels from left
		double m_TextVerticalPixels; //!< Location of explosion text on screen, pixels from top

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
};


#endif