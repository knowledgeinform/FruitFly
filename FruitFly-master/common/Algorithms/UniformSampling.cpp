//---------------------------------------------------------------------------------------
/*!\class  File Name:	UniformSampling.cpp
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


#include "UniformSampling.h"
#include "NRC.h"


/*! \fn UniformSampling::UniformSampling(PointSet* ps, int numPoints, PointSetBounds* bounds)
 *  \brief Fixed Number of Points Sampling Constructor
 *  \param ps - The pointset to sample
 *  \param numPoints - the integer number of points to sample
 *  \param bounds  - the bound in which to do the sampling
 *  \exception 
 *  \return 
 */
UniformSampling::UniformSampling(PointSet* ps, int numPoints, PointSetBounds* bounds)
	:SamplingAlgorithm(ps,numPoints, bounds)
{}


/*! \fn UniformSampling::UniformSampling(PointSet* ps, float pctPoints, PointSetBounds* bounds)
 *  \brief Percentage Sampling Constructor
 *  \param ps - The pointset to sample
 *  \param pctPoints - the percentage of the total points to sample
 *  \param bounds  - the bound in which to do the sampling
 *  \exception 
 *  \return 
 */
UniformSampling::UniformSampling(PointSet* ps, float pctPoints, PointSetBounds* bounds)
	:SamplingAlgorithm(ps, pctPoints, bounds)
{}

/*! \fn UniformSampling::~UniformSampling()
 *  \brief Destructor - free memory
 *  \exception 
 *  \return 
 */
UniformSampling::~UniformSampling()
{

}


/*! \fn int* UniformSampling::Sample()
 *  \brief Required override of the base class - Does the actual uniform sampling
 *  \exception 
 *  \return int array containg the indicies of the sampled points
 */
int* UniformSampling::Sample()
{	
	int actualNumPoints = 0;

	long id = -1;
	float* randnums = (float*)malloc((pointSet->numPoints+1)*sizeof(float));

	//Allocate memory for all of the points in the pointSet
	if(sampledIndices != NULL)free(sampledIndices);
	sampledIndices = (int*)malloc((pointSet->numPoints+1)*sizeof(int));

	//initialize the random number generator
	ran1(&id);
	id = 1;

	//Cycle through all the points
	for(int i=1; i<=pointSet->numPoints; i++)
	{
		//if the point is within the area of overlap, assign a random number
		if(bounds == NULL || bounds->isWithinXYBounds(pointSet->x[i],pointSet->y[i]))
		{
			actualNumPoints++;
			randnums[i] = ran1(&id);
		}
		else
		{
			//if not, assign a large number so it will get sorted to the end
			//put all out of bounds points at end
			randnums[i] = 1000.0;
		}
		sampledIndices[i] = i-1;
	}
	//if not enough points are available in bounds, report what is available
	if(actualNumPoints < this->numPoints)
		this->numPoints = actualNumPoints;

	//Do a simultaneous sort of the random numbers and the points
	//This provides a randomized list of points (without substitution) including all points
	//in the overlap region.  Just read into the list of indicies for the desired number of
	//sampled points.
	sort2(pointSet->numPoints, randnums, sampledIndices);
	free(randnums);
	
	return &sampledIndices[1];
}


