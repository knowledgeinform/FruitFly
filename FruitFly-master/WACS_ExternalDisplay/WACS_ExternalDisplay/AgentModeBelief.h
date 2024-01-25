#ifndef AGENTMODEBELIEF_H
#define AGENTMODEBELIEF_H

#include "stdafx.h"
#include "BeliefTemplate.h"


/**
	\class AgentModeBelief
	\brief Class representing a WACS Swarm belief - current mode of an agent
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class AgentModeBelief : public BeliefTemplate
{
	public:

		/**
		\brief Constructor, clears mode text and timestamp
		*/
		AgentModeBelief();

		/**
		\brief Constructor, sets specified timestamp and mode text

		\param timestampMs Milliseconds timestamp
		\param mode Text to use as agent mode
		*/
		AgentModeBelief(long long timestampMs, char mode[]);

		/**
		\brief Destructor
		*/
		~AgentModeBelief();


		/**
		\brief Paint the contents of this belief in an OpenGL scene.  Currently, AgentModeBelief is not
		displayed in OpenGL
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
		\brief Accessor for current mode.  Returns mode text in parameter
		\param msg After call, will be populated with current mode text
		\param maxLength Length of msg array
		\return void
		*/
		void getMode (char msg[], int maxLength);

		/**
		\brief Modifier for current mode.  Sets current mode to text in parameter
		\param msg New mode text for this belief
		\return void
		*/
		void setMode (char msg[]);

		/**
		\brief Comparison check current mode.  Return true if current mode matches mode passed in
		\param checkMode Mode text to compare
		\return True if mode matches passed in text, false otherwise
		*/
		bool modeEquals (char checkMode[]);

		/**
		\brief Accessor for timestamp value
		\return Timestamp milliseconds
		*/
		long long getMsgTimestampMs() {return m_MsgTimestampMs;}

		/**
		\brief Modifier for timestamp value
		\param newMsgTimestampMs New timestamp milliseconds
		\return void
		*/
		void setMsgTimestampMs (long long newMsgTimestampMs) {m_MsgTimestampMs = newMsgTimestampMs;}
		

	private:

		char m_Mode [128]; //!< Text of current agent mode
		long long m_MsgTimestampMs; //!< Timestamp milliseconds

};

#endif