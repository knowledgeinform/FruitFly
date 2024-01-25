#ifndef AGENTPOSITION_H
#define AGENTPOSITION_H

#include "stdafx.h"


/**
	\class AgentPosition
	\brief 3D Position and heading object
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class AgentPosition
{
	public:
		/**
		\brief Default constructor, does nothing
		*/
		AgentPosition ();
		
		/**
		\brief Constructor, sets variables as specified
		\param msgTimestampMs New timestamp milliseconds value
		\param latDecDeg New latitude decimal degrees value
		\param lonDecDeg New longitude decimal degrees value
		\param altMslM New altitude meters MSL value
		\param headingDecDeg New heading decimal degrees value
		*/
		AgentPosition (long long msgTimestampMs, double latDecDeg, double lonDecDeg, double altMslM, double headingDecDeg);

		/**
		\brief Copy constructor
		\param position Position to copy settings from
		*/
		AgentPosition (AgentPosition* position);

		/**
		\brief Accessor for timestamp milliseconds
		\return Timestamp milliseconds
		*/
		long long getMsgTimestampMs() {return m_MsgTimestampMs;}
		
		/**
		\brief Accessor for latitude
		\return Latitude decimal degrees
		*/
		double getLatDecDeg () {return m_LatDecDeg;}
		
		/**
		\brief Accessor for longitude
		\return Longitude decimal degrees
		*/
		double getLonDecDeg () {return m_LonDecDeg;}
		
		/**
		\brief Accessor for altitude
		\return Altitude MSL meters
		*/
		double getAltMslM () {return m_AltMslM;}
		
		/**
		\brief Accessor for heading
		\return Heading decimal degrees
		*/
		double getHeadingDecDeg () {return m_HeadingDecDeg;}

		/**
		\brief Modifier for timestamp milliseconds
		\param newMsgTimestampMs New timestamp milliseconds
		\return void
		*/
		void setMsgTimestampMs (long long newMsgTimestampMs) {m_MsgTimestampMs = newMsgTimestampMs;}
		
		/**
		\brief Modifier for latitude
		\param newLatDecDeg New latitude decimal degrees
		\return void
		*/
		void setLatDecDeg (double newLatDecDeg) {m_LatDecDeg = newLatDecDeg;}
		
		/**
		\brief Modifier for longitude
		\param newLonDecDeg New longitude decimal degrees
		\return void
		*/
		void setLonDecDeg (double newLonDecDeg) {m_LonDecDeg = newLonDecDeg;}
		
		/**
		\brief Modifier for altitude
		\param newAltMslM New altitude MSL meters
		\return void
		*/
		void setAltMslM (double newAltMslM) {m_AltMslM = newAltMslM;}
		
		/**
		\brief Modifier for heading
		\param newHeadingDecDeg New heading decimal degrees
		\return void
		*/
		void setHeadingDecDeg (double newHeadingDecDeg) {m_HeadingDecDeg = newHeadingDecDeg;}

	private:

		long long m_MsgTimestampMs; //!< Timestamp milliseconds
		double m_LatDecDeg; //!< Latitude decimal degrees
		double m_LonDecDeg; //!< Longitude decimal degrees
		double m_AltMslM; //!< Altitude MSL meters
		double m_HeadingDecDeg; //!< Heading decimal degrees
};

#endif