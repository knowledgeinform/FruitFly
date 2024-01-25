//---------------------------------------------------------------------------------------
/*!\class  File Name:	SamplingAlgorithm.h
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


#pragma once

#include "PointSet.h"
#include "PointSetBounds.h"
#include "Algorithm.h"

class SamplingAlgorithm : public Algorithm
{
public:

	//!Constructor for sampling fixed number of points
	SamplingAlgorithm(PointSet* ps, int numPoints, PointSetBounds* bounds = NULL);

	//!Constructor for sampling percentage of points
	SamplingAlgorithm(PointSet* ps, float pctPoints, PointSetBounds* bounds = NULL);

	//!Returns actual number of sampled points
	int  getNumSampledPoints();

	//!Sample method, must be overridden by inheriting classes
	virtual int* Sample()=0;

	//!Destructor
	virtual ~SamplingAlgorithm(void);


protected:

	//!The number of sampled points
	int numPoints;

	//!Array of the sampled indices
	int* sampledIndices;

	//!The pointset to sample
	PointSet* pointSet;

	//!The bounds to which to constrain sampling
	PointSetBounds* bounds;

};
