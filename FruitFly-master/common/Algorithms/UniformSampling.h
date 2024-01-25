//---------------------------------------------------------------------------------------
/*!\class  File Name:	UniformSampling.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for Uniform Sampling
//
// \note   Notes:	
//
// \note   Routines:  see UniformSampling class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------

#pragma once
#include "samplingalgorithm.h"

class UniformSampling : public SamplingAlgorithm
{
public:

	//!Constructor for sampling fixed number of points
	UniformSampling(PointSet* ps, int numPoints, PointSetBounds* bounds = NULL);

	//!Constructor for sampling percentage of points
	UniformSampling(PointSet* ps, float pctPoints, PointSetBounds* bounds = NULL);

	//!Destructor
	~UniformSampling();

	//!The sample method
	int* Sample();

};
