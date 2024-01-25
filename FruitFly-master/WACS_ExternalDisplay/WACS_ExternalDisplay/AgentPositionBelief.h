#ifndef AGENTPOSITIONBELIEF_H
#define AGENTPOSITIONBELIEF_H

#include "stdafx.h"
#include <list>

#include "BeliefTemplate.h"
#include "AgentPosition.h"

using namespace std;


/**
	\class AgentPositionBelief
	\brief Historical list of AgentPosition objects
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class AgentPositionBelief : public BeliefTemplate
{
	public:

		/**
		\brief Create empty AgentPosition list and set current position to NULL
		*/
		AgentPositionBelief ();

		/**
		\brief Create AgentPosition list and add firstPosition to it
		\param firstPosition First position for position history
		*/
		AgentPositionBelief (AgentPosition* firstPosition);

		/**
		\brief Destructor, delete list
		*/
		~AgentPositionBelief ();

		/**
		\brief Add newPosition to AgentPosition history list and store it as the current position.

		Adding newPosition is dependent on checking timestamp of newPosition against the last received 
		position.  If message is not new, delete the message.  If the message is new, add newPosition 
		to history.
		\param newPosition AgentPosition to add to the history
		\return True if the newPosition was new data and was added to the list.  False if newPosition was not new and not added.
		*/
		bool addAgentPosition (AgentPosition* newPosition);

		/**
		\brief Clear AgentPosition history
		\return True if history was cleared, false if there was no history to clear
		*/
		bool clearAgentPositions ();

		/**
		\brief Returns pointer to AgentPosition history.
		\return Pointer to internal AgentPosition history.  Can not be modified
		*/
		const list<AgentPosition*> * const getAgentPositions () {return m_AgentPositionHistory;}

		/**
		\brief Returns pointer to AgentPosition history.
		\return Pointer to internal AgentPosition history.  WARNING - Can be modified and screw up this object
		*/
		list<AgentPosition*> * getModifiableAgentPositions () {return m_AgentPositionHistory;}

		/**
		\brief Paint the contents of this belief in an OpenGL scene.  This class is virtual and must be
		extended to define how the display the agent in OpenGL.
		\param centerPointPosition Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		virtual void drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv) = 0;

		/**
		\brief Update RawDataDialog object with data from this belief.  Rows of dialog are filled with 
		contents of private variables.
		\param dataRows List of data rows in a RawDataDialog object to be updated with data for this belief
		\return void
		*/
		void updateDataRows (DataRows* dataRows);

		/**
		\brief Read config settings
		\return void
		*/
		void readConfig();

	protected:

		static int MAX_AGENT_POSITIONS; //!< Maximum historical positions in AgentPosition list
		
		list<AgentPosition*>* m_AgentPositionHistory; //!< List of AgentPosition history
		AgentPosition* m_CurrentPosition; //!< Current AgentPosition, pointer to last item in AgentPosition history
};

#endif