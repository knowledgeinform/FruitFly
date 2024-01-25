#ifndef CLOUDDETECTION_H
#define CLOUDDETECTION_H

#include "stdafx.h"


/**
	\class CloudDetection
	\brief A single cloud detection datapoint: location, strength, and type
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class CloudDetection
{
	public:
		/**
		\brief Default constructor, sets invalid parameters
		*/
		CloudDetection();

		/**
		\brief Constructor, sets specified parameters
		\param msgTimestampMs Timestamp milliseconds of detection
		\param latDecDeg Latitude decimal degrees of detection
		\param lonDecDeg Longitude decimal degrees of detection
		\param altMslM Altitude MSL meters of detection
		\param value Strength value of detection
		\param source Source of detection: 0=Chemical, 1=Particle
		*/
		CloudDetection (long msgTimestampMs, double latDecDeg, double lonDecDeg, double altMslM, float value, int source, int id);
		
		/**
		\brief Accessor for detection timestamp
		\return Timestamp milliseconds
		*/
		long long getMsgTimestampMs() {return m_MsgTimestampMs;}
		
		/**
		\brief Accessor for detection latitude
		\return Latitude decimal degrees
		*/
		double getLatDecDeg () {return m_LatDecDeg;}
		
		/**
		\brief Accessor for detection longitude
		\return Longitude decimal degrees
		*/
		double getLonDecDeg () {return m_LonDecDeg;}
		
		/**
		\brief Accessor for detection altitude
		\return Altitude MSL m
		*/
		double getAltMslM () {return m_AltMslM;}
		
		/**
		\brief Accessor for detection value
		\return Strength value
		*/
		float getValue () {return m_Value;}

		/**
		\brief Accessor for detection ID
		\return ID value
		*/
		int getId () {return m_ID;}
		
		/**
		\brief Accessor for detection source
		\return Source: 0=Chemical, 1=Particle
		*/
		int getSource () {return m_Source;}

		/**
		\brief Modifier for detection timestamp
		\param newMsgTimestampMs New timestamp milliseconds
		\return void
		*/
		void setMsgTimestampMs (long long newMsgTimestampMs) {m_MsgTimestampMs = newMsgTimestampMs;}
		
		/**
		\brief Modifier for detection latitude
		\param newLatDecDeg New latitude decimal degrees
		\return void
		*/
		void setLatDecDeg (double newLatDecDeg) {m_LatDecDeg = newLatDecDeg;}
		
		/**
		\brief Modifier for detection longitude
		\param newLonDecDeg New longitude decimal degrees
		\return void
		*/
		void setLonDecDeg (double newLonDecDeg) {m_LonDecDeg = newLonDecDeg;}
		
		/**
		\brief Modifier for detection altitude
		\param newAltMslM New altitude MSL m
		\return void
		*/
		void setAltMslM (double newAltMslM) {m_AltMslM = newAltMslM;}
		
		/**
		\brief Modifier for detection value
		\param newValue New strength value
		\return void
		*/
		void setValue (float newValue) {m_Value = newValue;}
		
		/**
		\brief Modifier for detection source
		\param newSource New source index: 0=Chemical, 1=Particle
		\return void
		*/
		void setSource (int newSource) {m_Source = newSource;}

		/**
		\brief Modified for detection ID
		\param newID New ID, source dependent
		\return void
		*/
		void setID (int newID) {m_ID = newID;}

		const static int PARTICLE_DETECTION = 1; //!< Enumeration value for detection being from a particle sensor source
		const static int CHEMICAL_DETECTION = 0; //!< Enumeration value for detection being from a chemical sensor source

		const static int NUM_CLOUDDETECTIONS_SOURCES = 5; //!< Maximum number of cloud detection sources.  Can be larger than actual, must not be smaller.

	private:

		long long m_MsgTimestampMs; //!< Timestamp milliseconds
		double m_LatDecDeg; //!< Latitude decimal degrees
		double m_LonDecDeg; //!< Longitude decimal degrees
		double m_AltMslM; //!< Altitude MSL m
		
		float m_Value; //!< Strength value
		int m_Source; //!< Detection source index
		int m_ID; //!< Detection ID
};

#endif