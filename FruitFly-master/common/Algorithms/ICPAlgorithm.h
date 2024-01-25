//---------------------------------------------------------------------------------------
/*!\class  File Name:	ICPAlgorithm.cpp
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for doing ICP registration of 3D PointSets
//
// \note   Notes:	
//
// \note   Routines:  see ICPAlgortihm class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2008
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------


#pragma once
#include "registrationalgorithm.h"
#include "ImageAlignment.h"
#include "matrix.h"
#include "UniformSampling.h"
#include "NRC.h"


//!Structure containing the parameters necessary for the ICP loop when doing multi-threaded processing
	struct ICPParameters
	{
		float A[6][6];
		float B[6]; 
		PointSet* ref;
		PointSet* scan; 
		DMatrix* refToScan; 
		int* ixes; 
		int numSamples;
		float distThresh; 
		int npairs; 
		float sum;
	};

class ICPAlgorithm :public RegistrationAlgorithm
{

public:
	
	//!Constructor
	ICPAlgorithm(void);

	//!Destructor
	virtual ~ICPAlgorithm(void);

	//!Do registration of two scans
	float doRegistration(PointSet* ref, PointSet* scan);

	//!Do registration of multiple scans
	float doRegistration(PointSet** scans,  const int numScans);

	//!Register two scans - segmented or unsegmented
	bool RegisterFile(PointSet* ref, PointSet* targ, float &rms, bool useAdjusted, float &initialRMS);

	//!Method for doing rough alignment from output of Image Registration
	DMatrix alignUsingImageTransform(PointSet* ref, PointSet* scan,
											   int refICen, int refJCen, int scanICen, int scanJCen,
											   DVector R, PointSetBounds* intersect);

	//! Do registration without block segmentation
	bool doRegistrationUnsegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS);

	//! Do registration with block segmentation
	bool doRegistrationSegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS);

protected:
	//!Do the ICP algorithm for two scans
	float	doICP(PointSet* ref, PointSet* scan,  float initialerr = 1.5, float samprate = 0.30);

	//!Do the ICP algorithm for multiple scans
	float	doICP(PointSet** ref, PointSet* scan,  const int numRefs, float initialerr = 1.5, float samprate = 0.30);

	//!The core ICP algorithm
	void	ICPCore(double A[6][6], double B[6], PointSet* ref, PointSet* scan, DMatrix refToScan, int* ixes, int numSamples, double distThresh, int& npairs, double& sum);
	
	//!The core ICP algorithm, optimized for multiple processors
	void    ICPCoreMulti(double A[6][6], double B[6], PointSet* ref, PointSet* scan, DMatrix* refToScan, int* ixes, int numSamples, double distThresh, int& npairs, double& sum);
	
	//!Search nearby pixels for the nearest point
	static int		doLocalSearch(DVector pt1, PointSet* scan, int ix, int pixelSearchRange);

	//!Solve for the alignment transfomr point to plane
	bool	solveAlignmentTransform(PointSet* r2, double A[6][6], double B[6]);

	//!Solve for the alignment transfomr point to point
	DMatrix	solveAlignmentPointToPoint( double *x1, double *y1, double *z1,
									 double *x2, double *y2, double *z2,
									 int n, double zOffset=0.0);

	//!Alternate Method to solve for the alignment transfomr point to point
	void    solveAlignmentPointToPoint(  double *x1, double *y1, double *z1,
												double *x2, double *y2, double *z2,
												int n, double *transform);

	//!Alternate Method to solve for the alignment transfomr point to point
	void    solveAlignmentPointToPointOld(  float *x1, float *y1, float *z1,
												float *x2, float *y2, float *z2,
												int n, float *transform);

	//!Core ICP Loop used for MultiCore processing
	static void ICPLoop(int start, int stop, int step, ICPParameters* params);

	//!Object for doing initial image based alignment
	ImageAlignment iAlign;

	double minDistThresh;
	static bool useXYZ;
};
