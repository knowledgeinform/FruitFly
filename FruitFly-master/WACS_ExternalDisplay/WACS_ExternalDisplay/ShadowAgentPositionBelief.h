#ifndef SHADOWAGENTPOSITIONBELIEF_H
#define SHADOWAGENTPOSITIONBELIEF_H

#include "stdafx.h"
#include "AgentPositionBelief.h"
#include "ShadowUavDisplay.h"
#include "DakotaUavDisplay.h"

/**
	\class ShadowAgentPositionBelief
	\brief Child class of AgentPositionBelief implemented to draw a Shadow UAV with wing-mounted WACS pods for the agent
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class ShadowAgentPositionBelief : public AgentPositionBelief
{
	public:

		/**
		\brief Default constructor, calls parent constructor
		*/
		ShadowAgentPositionBelief ();
		
		/**
		\brief Constructor, calls parent constructor
		\param firstPosition Position to locate UAV at
		*/
		ShadowAgentPositionBelief (AgentPosition* firstPosition);
		
		/**
		\brief Destructor
		*/
		~ShadowAgentPositionBelief ();

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Displays UAV and it's path

		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv);

		/**
		\brief Toggle whether the UAV should be painted or not.
		\param displayUav If true, paint the UAV.  If false, ignore the UAV and only paint the path
		\return void
		*/
		void toggleDisplayUav (bool displayUav) {m_DisplayUav = displayUav;}

		static UavDisplayBase *m_UavDisplay; //!< UavDisplayBase object that displays a UAV with wing-mounted pods

	private:

		bool m_DisplayUav; //!< If true, paint the UAV.  If false, ignore the UAV and only paint the path

		double m_HistoryLineWidth; //!< Line width of UAV history
		double m_HistoryLineRedColor; //!< Red color component of UAV history line
		double m_HistoryLineGreenColor; //!< Green color component of UAV history line
		double m_HistoryLineBlueColor; //!< Blue color component of UAV history line

		UavDisplayBase::UAV_TYPES m_UavType; //!< Type of UAV to display

		/**
		\brief Read config settings from file
		\return void
		*/
		void readConfig();
};


#endif