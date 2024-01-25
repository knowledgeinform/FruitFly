#ifndef BELIEFTEMPLATE_H
#define BELIEFTEMPLATE_H

#include "stdafx.h"
#include "AgentPosition.h"
#include "RawDataDialog.h"
#include "time.h"

/**
	\class BeliefTemplate
	\brief Base class for all WACS Swarm belief objects.  Stores last updated timestamp and abstracts functions
	for display to OpenGL scene and RawDataDialog window
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class BeliefTemplate
{
	public:
		/**
		\brief Default constructor, sets last updated time
		*/
		BeliefTemplate () {m_LastUpdatedSec = time(NULL);}

		/**
		\brief Destructor
		*/
		virtual ~BeliefTemplate () {};


		/**
		\brief Paints the relevant contents of this belief as an overlay in an OpenGL scene.  Assumes screen coordinates are pixel
		dimensions, provided as parameters.
		\param glWidth Width pixels of GL scene
		\param glHeight Height pixels of GL scene
		\return void
		*/
		virtual void drawGlOverlay (int glWidth, int glHeight) {};

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  BeliefTemplate Class must be extended and 
		this function must be implemented by child class.

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		virtual void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv) = 0;

		/**
		\brief Update RawDataDialog object with data from this belief.  Rows of dialog are filled with 
		contents of private variables.  BeliefTemplate Class must be extended and this function must be 
		implemented by child class.
		\param dataRows List of data rows in a RawDataDialog object to be updated with data for this belief
		\return void
		*/
		virtual void updateDataRows (DataRows* dataRows) = 0;

	protected:

		long long m_LastUpdatedSec; //!< Timestamp seconds when this belief was last updated
};

#endif