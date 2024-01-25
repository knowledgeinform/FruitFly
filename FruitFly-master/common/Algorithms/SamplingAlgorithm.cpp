//---------------------------------------------------------------------------------------
/*!\class  File Name:	SamplingAlgorithm.cpp
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Base Class for Sampling Algorithms
//
// \note   Notes:	
//
// \note   Routines:  see SamplingAlgorithm class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------


#include "SamplingAlgorithm.h"

/*! \fn SamplingAlgorithm::SamplingAlgorithm(PointSet* ps, int numPoints, PointSetBounds* bounds)
 *  \brief Fixed Number of Points Sampling Constructor
 *  \param ps - The pointset to sample
 *  \param numPoints - the integer number of points to sample
 *  \param bounds  - the bound in which to do the sampling
 *  \exception 
 *  \return 
 */
SamplingAlgorithm::SamplingAlgorithm(PointSet* ps, int numPoints, PointSetBounds* bounds)
{
	pointSet = ps;
	this->numPoints = numPoints;
	this->bounds = bounds;
	sampledIndices = NULL;
}

/*! \fn SamplingAlgorithm::SamplingAlgorithm(PointSet* ps, float pctPoints, PointSetBounds* bounds)
 *  \brief Percentage Sampling Constructor
 *  \param ps - The pointset to sample
 *  \param pctPoints - the percentage of the total points to sample
 *  \param bounds  - the bound in which to do the sampling
 *  \exception 
 *  \return 
 */
SamplingAlgorithm::SamplingAlgorithm(PointSet* ps, float pctPoints, PointSetBounds* bounds)
{
	pointSet = ps;
	if(pctPoints > 1.0)
		pctPoints /= 100.0;
	this->numPoints = (int)(ps->numPoints * pctPoints);
	this->bounds = bounds;
	sampledIndices = NULL;
}

/*! \fn SamplingAlgorithm::~SamplingAlgorithm()
 *  \brief Destructor - free memory
 *  \exception 
 *  \return 
 */
SamplingAlgorithm::~SamplingAlgorithm()
{
	if(sampledIndices != NULL)
		free(sampledIndices);
}

/*! \fn int  SamplingAlgorithm::getNumSampledPoints()
 *  \brief Destructor - free memory
 *  \exception 
 *  \return the number of points sampled
 */
int  SamplingAlgorithm::getNumSampledPoints()
{
	return numPoints;
}

