#include "stdafx.h"
#include "PropogatingCloudDetectionBelief.h"
#include "Config.h"
#include "WinFonts.h"
#include "Constants.h"
#include "math.h"

PropogatingCloudDetectionBelief::PropogatingCloudDetectionBelief()
{	
	m_ParticleDetectionColorAlpha = Config::getInstance()->getValueAsDouble ("PropogatingCloudDetectionBelief.ParticleDisplayColorAlpha", 0.3);
	m_CloudDetectionSizeM = Config::getInstance()->getValueAsDouble ("PropogatingCloudDetectionBelief.DisplaySizeM", 10);
	m_ChemicalDetectionColorAlpha = Config::getInstance()->getValueAsDouble ("PropogatingCloudDetectionBelief.ChemicalDisplayColorAlpha", 0.3);
}

PropogatingCloudDetectionBelief::~PropogatingCloudDetectionBelief()
{
	
}

void PropogatingCloudDetectionBelief::propogateDetections (double windBearingToDeg, double windSpeedMps, double elapsedTimeMs, double refLatDecDeg)
{
	double movementDistanceMeters = windSpeedMps * elapsedTimeMs / 1000;
	double movementDistanceNorthMeters = movementDistanceMeters * cos (windBearingToDeg * DEG2RAD);
	double movementDistanceEastMeters = movementDistanceMeters * sin (windBearingToDeg * DEG2RAD);

	double estLatToMConv = DEG2M;
	double estLonToMConv = DEG2M*cos(refLatDecDeg*DEG2RAD);
	
	double movementDistanceNorthDecDeg = movementDistanceNorthMeters / estLatToMConv;
	double movementDistanceEastDecDeg = movementDistanceEastMeters / estLonToMConv;
	

	for( std::list<CloudDetection*>::iterator i(m_ParticleDetections->begin()), end(m_ParticleDetections->end()); i != end; ++i )
	{ 
		CloudDetection* nextPosition = (*i);

		if (nextPosition != NULL)
		{
			nextPosition->setLatDecDeg (nextPosition->getLatDecDeg() + movementDistanceNorthDecDeg);
			nextPosition->setLonDecDeg (nextPosition->getLonDecDeg() + movementDistanceEastDecDeg);
		}
	}

	for( std::list<CloudDetection*>::iterator i(m_ChemicalDetections->begin()), end(m_ChemicalDetections->end()); i != end; ++i )
	{ 
		CloudDetection* nextPosition = (*i);

		if (nextPosition != NULL)
		{
			nextPosition->setLatDecDeg (nextPosition->getLatDecDeg() + movementDistanceNorthDecDeg);
			nextPosition->setLonDecDeg (nextPosition->getLonDecDeg() + movementDistanceEastDecDeg);
		}
	}
}