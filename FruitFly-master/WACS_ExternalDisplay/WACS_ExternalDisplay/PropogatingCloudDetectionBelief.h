#ifndef PROPOGATINGCLOUDDETECTIONBELIEF_H
#define PROPOGATINGCLOUDDETECTIONBELIEF_H

#include "stdafx.h"
#include "CloudDetectionBelief.h"

/**
	\class PropogatingCloudDetectionBelief
	\brief Historical collection of CloudDetection datapoints, propogated with wind over time
	\author John Humphreys
	\date 2011
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class PropogatingCloudDetectionBelief : public CloudDetectionBelief
{
	public:
		/**
		\brief Default constructor, generate lists and GLUquadricObj instance
		*/
		PropogatingCloudDetectionBelief();

		/**
		\brief Destructor, destroy GLUquadricObj instance and delete lists
		*/
		~PropogatingCloudDetectionBelief();

		/**
		\brief Propogate the list of detections in the direction of wind.  Maintain altitude for now.
		\param windBearingToDeg Direction wind is blowing to, in degrees from North
		\param windSpeedMps Speed of wind vector, meters per second
		\param elapsedTimeMs Time to propogate particles over, milliseconds
		\param refLatDecDeg Reference latitude value to compute degree-to-meter conversion
		\return void
		*/
		void propogateDetections (double windBearingToDeg, double windSpeedMps, double elapsedTimeMs, double refLatDecDeg);

	private:

};

#endif