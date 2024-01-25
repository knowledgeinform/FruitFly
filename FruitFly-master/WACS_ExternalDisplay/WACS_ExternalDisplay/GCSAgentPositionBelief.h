#ifndef GCSAGENTPOSITIONBELIEF_H
#define GCSAGENTPOSITIONBELIEF_H

#include "stdafx.h"
#include "AgentPositionBelief.h"
#include "GCSDisplay.h"

/**
	\class GCSAgentPositionBelief
	\brief Child class of AgentPositionBelief implemented to draw a GCS truck for the agent
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class GCSAgentPositionBelief : public AgentPositionBelief
{
	public:

		/**
		\brief Default constructor, calls parent constructor
		*/
		GCSAgentPositionBelief ();

		/**
		\brief Constructor, calls parent constructor
		\param firstPosition Position to locate GCS at
		*/
		GCSAgentPositionBelief (AgentPosition* firstPosition);

		/**
		\brief Destructor
		*/
		~GCSAgentPositionBelief ();

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Displays PGCS truck

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv);

		static GCSDisplay *m_GCSDisplay; //!< GCSDisplay object that displays a PGCS truck

	private:

};


#endif