//---------------------------------------------------------------------------------------
/*!\class  File Name:	ICPAlgorithm.cpp
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for doing ICP registration of 3D PointSets
//
// \note   Notes:	  Routines based on work by Szymon Rusinkiewicz
//
// \note   Routines:  see ICPAlgortihm class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------

#include "ICPAlgorithm.h"
#include "MultiCoreAlgorithm.h"
#include "PointSetTransform.h"
#include "FileUtilities.h"
#include <time.h>


bool ICPAlgorithm::useXYZ = false;

/*! \fn ICPAlgorithm::ICPAlgorithm(void)
 *  \brief Constructor that sets default parameters.
 *  \exception 
 *  \return
 */
ICPAlgorithm::ICPAlgorithm(void)
{
	setParameter("debugOn",0.0f);
	setParameter("maxiterations",100.0f);
	setParameter("initialerror",1.5f);
	setParameter("samprate",0.15f);	
	setParameter("preverrorweight",0.25f);
	setParameter("useXYZ", 0);

	iAlign.setParameter("showImages", 0.0);
	iAlign.setParameter("repeatSteps", 10.0);
}

/*! \fn ICPAlgorithm::~ICPAlgorithm(void)
 *  \brief Destructor
 *  \exception 
 *  \return 
 */
ICPAlgorithm::~ICPAlgorithm(void)
{
}


/*! \fn bool ICPAlgorithm::RegisterFile(PointSet* ref, PointSet* targ, float &rms, bool useAdjusted, float &initialRMS)
 *  \brief Function that does registration of two PointSets
 *  
 *  Specify parameter segmented=1.0 to do segmented, otherwise registration is unsegmented.
 *  \param ref - The reference Point Set
 *  \param targ - The target PointSet which is registered to the reference
 *  \param rms - error is returned in this value
 *  \param useAdjusted - if true, PointSets with _adj extensions are used for the registration
 *  \param initialRMS - estimate of initial error - to specify initial correspondance search area
 *  \exception 
 *  \return boolean success
 */
bool ICPAlgorithm::RegisterFile(PointSet* ref, PointSet* targ, float& rms, bool useAdjusted, float &initialRMS)
{
	bool retval;

	error = initialRMS*3;
	
	bool segmented = getParameter("segmented", false)==1.0;
	minDistThresh = getParameter("minDistThresh", 0.3);

	if(segmented)
	{
		retval = doRegistrationSegmented(ref, targ, rms, initialRMS);
	}
	else
	{
		retval = doRegistrationUnsegmented(ref, targ, rms, initialRMS);
	}
	
	return retval;
}

/*! \fn bool ICPAlgorithm::doRegistrationUnsegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS)
 *  \brief Function that does Unsegmented registration of two PointSets
 *  \param ref - The reference Point Set
 *  \param targ - The target PointSet which is registered to the reference
 *  \param rms - error is returned in this value
 *  \param initialRMS - estimate of initial error - to specify initial correspondance search area
 *  \exception 
 *  \return boolean success
 */
bool ICPAlgorithm::doRegistrationUnsegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS)
{
	int r,c,r1,c1, ci1,ci2,cj1,cj2;

	setParameter("initialerror", initialRMS);

	//Convert each scan to a projection - imaginary camera placed at offset above pointset 
	if(!ref->projection)
		ref->convertToProjection(0, 0, 20);

	if(!targ->projection)
		targ->convertToProjection(0, 0, 20);

	//Get the area of overlap for the two scans and remove 2m of edges
	PointSetBounds intersection = ref->bounds.getOverlap(&targ->bounds);
	//intersection.adjustBounds(-2, -2, -2);

	//Create two images that correspond to the overlap - one from each scan
	//clipToBounds allocates memory that must be cleaned up here
	float* image1 = ref->clipToBounds(&intersection, r, c, ci1, cj1);
	float* image2 = targ->clipToBounds(&intersection, r1, c1, ci2, cj2);

	//iAlign.setParameter("showImages", 1.0);

	//Align the images using Least Squares optical flow
	iAlign.setRefImage(image1, r, c,false,ref->pMax, ref->pMin);
	iAlign.setImageToAlign(image2, r1, c1,false,targ->pMax, targ->pMin);
	iAlign.alignImages();

	free(image1);
	free(image2);

	//targ->createVRML("test12before.vrml", ref, targ,false, true);


	//Align the two point sets using point to point minimization using the 
	//correspondances from the image alignment
	DMatrix tf = alignUsingImageTransform(	ref,targ,
											ci1, cj1, ci2, cj2,
											iAlign.imageTransform,
											&intersection);

	//Now do ICP registration on the pre-registered point sets
	doRegistration(ref, targ);

	//targ->createCorrelatedVRML("test12correlated.vrml", ref, targ, true);

	//The aligning transform is in the target pointset, but not applied to the points
	//This call applies the transform to the points, thus registering it.
	targ->applyTransformToPoints();


	return true;
}

/*! \fn bool ICPAlgorithm::doRegistrationSegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS)
 *  \brief Function that does Segmented registration of two PointSets
 *  \param ref - The reference Point Set
 *  \param targ - The target PointSet which is registered to the reference
 *  \param rms - error is returned in this value
 *  \param initialRMS - estimate of initial error - to specify initial correspondance search area
 *  \exception 
 *  \return boolean success
 */
bool ICPAlgorithm::doRegistrationSegmented(PointSet* ref, PointSet* targ, float &rms, float &initialRMS)
{
	float blockSize = getParameter("blockSize", 50.0);

	int r,c,r1,c1, ci1,ci2,cj1,cj2;
	float *image1, *image2;

	PointSetTransform originalTransform;

	//Convert each scan to a projection - imaginary camera placed at offset above pointset 
	if(!ref->projection)
		ref->convertToProjection(0, 0, 20);
	if(!targ->projection)
		targ->convertToProjection(0, 0, 20);

	originalTransform = targ->transform;

	//Get the area of overlap for the two scans and remove 2m of edges
	PointSetBounds intersection = ref->bounds.getOverlap(&targ->bounds);
	intersection.adjustBounds(-2, -2, -2);

	// Segment the overlap area.
	int segCols = (int)ceil((intersection.xmax - intersection.xmin)/blockSize);
	int segRows = (int)ceil((intersection.ymax - intersection.ymin)/blockSize);
	PointSetTransform *transform = NULL;
	bool *success = NULL;
	
	try
	{
		transform = new PointSetTransform[segCols*segRows];
		success = new bool[segCols*segRows];
	}
	catch (bad_alloc&)
	{
		printf("Insufficient memory\n");
		if (transform) delete [] transform;
		if ( success ) delete [] success;
		return false;
	}

	// Estimate rigid body transforms.
	int k=0;
	printf("Aligning %d x %d segmented blocks.\n", segCols, segRows);
	for (int j=0;j<segRows;j++)
	{
		printf(".");
		for (int i=0;i<segCols;i++)
		{
			// Define the segment bounds. 
			PointSetBounds segmentBounds;
			segmentBounds.xmin = intersection.xmin + blockSize*i;
			segmentBounds.xmax = intersection.xmin + blockSize*(i+1);
			segmentBounds.ymin = intersection.ymin + blockSize*j;
			segmentBounds.ymax = intersection.ymin + blockSize*(j+1);
			segmentBounds.zmin = intersection.zmin;
			segmentBounds.zmax = intersection.zmax;

			// Estimate rigid body transform for this segment.

			//Create two images that correspond to the overlap - one from each scan
			image1 = ref->clipToBounds(&segmentBounds, r, c, ci1, cj1);
			image2 = targ->clipToBounds(&segmentBounds, r1, c1, ci2, cj2);\

			//Align the images using Least Squares optical flow
			iAlign.setRefImage(image1, r, c,true,ref->pMax, ref->pMin);
			iAlign.setImageToAlign(image2, r1, c1,true,targ->pMax, targ->pMin);
			iAlign.alignImages();

			//Align the two point sets using point to point minimization using the 
			//correspondances from the image alignment
			DMatrix tf = alignUsingImageTransform(	ref,targ,
													ci1, cj1, ci2, cj2,
													iAlign.imageTransform,
													&segmentBounds);

			//Now do ICP registration on the pre-registered point sets
			transform[k].rms = doRegistration(ref, targ);

			success[k] = true;
			transform[k] = targ->transform;
			transform[k].cx = (float)(segmentBounds.xmax - segmentBounds.xmin)/2.0f;
			transform[k].cy = (float)(segmentBounds.ymax - segmentBounds.ymin)/2.0f;
			transform[k].cz = (float)(segmentBounds.zmax - segmentBounds.zmin)/2.0f;

			targ->transform = originalTransform;

			//If local RMS much greater than global RMS, then fail.
			//if (error > (globalRMS * 3)) success[k] = false;

			k++;
		}
	}
	printf("\n");

	// Write transforms to log.
	char fname[1024], fdir[1024];
	int len = (int)strlen(targ->fileName.c_str());
	//RemoveFileName(targ->fileName.c_str(), fdir);
	sprintf(fname, "%s\\segmented_adjust_log.txt\0", fdir);

	
	FILE *fptr = fopen(fname, "a");
	int numSucceeded = 0;
	printf("File = %s\n",targ->fileName.c_str());
	printf("Number of segments attempted = %d\n", segCols*segRows);
	fprintf(fptr, "# Number of segments attempted = %d\n", segCols*segRows);
	fprintf(fptr, "# cx             cy               cz         tx         ty         tz         rx         ry         rz         rms            #pts\n");

	float rms_sum = 0.0f;
	for (int k=0;k<segCols*segRows;k++)
	{
		if (success[k] == false) continue;
		numSucceeded++;
		rms_sum += (float)transform[k].rms;
		fprintf(fptr, "%15.6f %15.6f %10.6f %10.6f %10.6f %10.6f %10.6f %10.6f %10.6f %10.6f %10d #sub-block %d\n",
			transform[k].cx, transform[k].cy, transform[k].cz,
			transform[k].tx, transform[k].ty, transform[k].tz, 
			transform[k].rx, transform[k].ry, transform[k].rz, 
			transform[k].rms, 
			transform[k].numPoints,
			k);
		

	}
	printf("Percent segments succeeded = %f\n", float(numSucceeded)* 100.0/(segCols*segRows));
	printf("Mean RMS of successfully registered segments = %f\n", rms_sum/numSucceeded);
	fprintf(fptr, "# Percent segments succeeded = %f\n", float(numSucceeded)* 100.0/(segCols*segRows));
	fprintf(fptr, "# Mean RMS of successfully registered segments = %f\n", rms_sum/numSucceeded);
	fclose(fptr);


	// Apply segmented transform to point coordinates.
	// Apply transform to point coordinates.
	float *xx = NULL;
	float *yy = NULL;
	float *zz = NULL;
	float *wsum = NULL;
	
	try
	{
		xx = new float[targ->numPoints];
		yy = new float[targ->numPoints];
		zz = new float[targ->numPoints];
		wsum = new float[targ->numPoints];
	}
	catch (bad_alloc&)
	{
		printf( "Insufficient memory.\n");
		if ( xx ) delete [] xx;
		if ( xx ) delete [] yy;
		if ( xx ) delete [] zz;
		if ( xx ) delete [] wsum;
		return false;
	}

	memset(xx, 0, targ->numPoints*sizeof(float));
	memset(yy, 0, targ->numPoints*sizeof(float));
	memset(zz, 0, targ->numPoints*sizeof(float));
	memset(wsum, 0, targ->numPoints*sizeof(float));

	for (int k=0;k<segCols*segRows;k++)
	{
		// Skip if registration failed for this block.
		if (success[k] == false) continue;

		// Otherwise, add weighted estimate to xyz values.
		DMatrix R(3,3);
		DVector T(3);
		PointSetTransform pst;
		R = pst.angles2rot(transform[k].rx, transform[k].ry, transform[k].rz);
		T[0] = transform[k].tx;
		T[1] = transform[k].ty;
		T[2] = transform[k].tz;

		float xt, yt, zt;

		for (int m=0;m<targ->numPoints;m++)
		{
			double xs = targ->x[m]  - transform[k].cx;
			double ys = targ->y[m]  - transform[k].cy;

			float dist = (float)sqrt(xs*xs + ys*ys);
			if (dist > blockSize*3)
				continue;
			float wt = 1.0f/(dist*dist*dist*dist + 0.001f);

			xt = targ->x[m];
			yt = targ->y[m];
			zt = targ->z[m];

			transform[k].rotateTranslateVector(xt,yt,zt);

			xx[m] += float(wt * (xt - targ->xoff));
			yy[m] += float(wt * (yt - targ->yoff));
			zz[m] += float(wt * (zt - targ->zoff));
			wsum[m] += wt;
		}
	}
	targ->bounds.xmin = FLT_MAX;
	targ->bounds.ymin = FLT_MAX;
	targ->bounds.xmax = -FLT_MAX;
	targ->bounds.ymax = -FLT_MAX;
	for (int m=0;m<targ->numPoints;m++)
	{
		if (wsum[m] > 0.0)
		{
			targ->x[m] = xx[m]/wsum[m];
			targ->y[m] = yy[m]/wsum[m];
			targ->z[m] = zz[m]/wsum[m];
		}

		 targ->x[m] += targ->xoff;
		 targ->y[m] += targ->yoff;
		 targ->z[m] += targ->zoff;

		double xs = targ->x[m];
		double ys = targ->y[m];
		double zs = targ->z[m];
		if (xs < targ->bounds.xmin) targ->bounds.xmin = xs;
		if (xs > targ->bounds.xmax) targ->bounds.xmax = xs;
		if (ys < targ->bounds.ymin) targ->bounds.ymin = ys;
		if (ys > targ->bounds.ymax) targ->bounds.ymax = ys;
	}

	// Deallocate memory.
	delete []xx;
	delete []yy;
	delete []zz;
	delete []wsum;
	delete []transform;
	delete []success;

	return true;
}


/*! \fn DMatrix ICPAlgorithm::alignUsingImageTransform(PointSet* ref, PointSet* targ,
 *											   int refICen, int refJCen, int scanICen, int scanJCen,
 *											   DVector R, PointSetBounds* intersect)
 *  \brief Using the 3x3 image transformation matrix, do a point to point mimimization with the
 *	points from the 3D point cloud - this could possibly be replaced with discrete math.
 *  The images used for alignment were clipped from the 3D point cloud projections.  The I and J Center 
 *  variable specify the center of the images (in pixel coordinates) in the projections.
 *  \param ref - The reference Point Set
 *  \param scan - The scan (to be aligned) PointSet which is registered to the reference
 *  \param refICen - the Column location in the ref projection of the image used for image alignment
 *  \param refJCen - the Row location in the ref projection of the image used for image alignment
 *  \param scanICen - the Column location in the scan projection of the image used for image alignment
 *  \param scanJCen - the Row location in the scan projection of the image used for image alignment
 *  \param R - the 3 x 3 matrix that aligns the projected images
 *  \param intersect - the PointSetBounds that specifies the area of overlap between point clouds
 *  \exception 
 *  \return 4x4 double matrix containing the transform in 3D point coordinates to align the scan with the ref
 */
DMatrix ICPAlgorithm::alignUsingImageTransform(PointSet* ref, PointSet* scan,
											   int refICen, int refJCen, int scanICen, int scanJCen,
											   DVector R, PointSetBounds* intersect)
{
	DMatrix retval(4,4);
	DMatrix scanInv(4,4);
	DMatrix refToScan(4,4);
	DVector pt14(4);
	DVector pt1(3);
	float samprate = getParameter("samprate",0.30f);
	double xij, yij, denom;
	int ii, jj;
	double d;

	double transform[16];

	//scan rows and columns
	int rows = scan->rows;
	int cols = scan->cols;

	double xulimit = cols-1-0.01;
	double yulimit = rows-1-0.01;

	//apply transform to the intersection limits for use in sampling only overlap
	PointSetTransform temp = scan->transform;
	temp.invert();
	intersect->applyTransform(&temp);

	int* ixes;
	int ix, numSamples;
	UniformSampling * samp = NULL;
	
	if(intersect==NULL || !intersect->isEmpty())
	{
		//Sample points at the specified rate from the overlapping region uniformly
		samp = new UniformSampling(ref,samprate,intersect);
		numSamples = samp->getNumSampledPoints();
		ixes = samp->Sample();
	}

	//calculate the translation rotation matrix to convert ref points to the scan coordinate frame
	scanInv = scan->transform.getRotationTranslationMatrix();
	scanInv = scanInv.fastInv();
	refToScan = mult(scanInv,ref->transform.getRotationTranslationMatrix());
	
	// declare arrays to hold the point arrays for least squares
	double* xa = (double*)malloc(numSamples*sizeof(double));
	double* ya = (double*)malloc(numSamples*sizeof(double));
	double* za = (double*)malloc(numSamples*sizeof(double));
	double* xb = (double*)malloc(numSamples*sizeof(double));
	double* yb = (double*)malloc(numSamples*sizeof(double));
	double* zb = (double*)malloc(numSamples*sizeof(double));
	int cnt = 0;

	//pt14 is a 4D point used to multiply with a 4x4 Translation rotation matrix. This turns on translation
	pt14[3] = 1.0;

	//Step through all sampled points
	for(int i=0; i<numSamples; i++)
	{
		// get the sampled point index
		ix = ixes[i];

		//Get the reference point at sampled index and put into 4D point Vector
		pt14[0] = ref->x[ix];
		pt14[1] = ref->y[ix];
		pt14[2] = ref->z[ix];

		//get the row and column (ii,jj) of the sampled point
		ref->xyz_to_ij(pt14[0],pt14[1],pt14[2],ii,jj);

		//convert the x,y,z of the point to the scan reference frame
		pt14 = mult(refToScan, pt14);

		//put reference point(after conversion to scan ref frame) in to arrays for least squares
		xa[cnt] = pt14[0];
		ya[cnt] = pt14[1];
		za[cnt] = pt14[2];

		//Get the x and y pixel location of the point referenced to the overlap image
		//Previously, images were extracted from each scan (ref and scan) of only the overlap
		//region.  The refICenter is the pixel center of the rectangular image extracted from the
		//reference scan of this overlap region.
		xij = ii-refICen;
		yij = jj-refJCen;

		//This section applies the image warping matrix (6DOF or projective) to get the corresponding
		//pixel value in the scan.  The overlapping images share optical centers, but the actual pixel index
		//of these centers is typically different for each scan.  The scan Optical center is added
		//to the result after applying the warping matrix to properly index into the scan for alignment.
		denom = R[6]*xij + R[7]*yij + 1.0;
		ii = (int)((R[0]*xij + R[1]*yij + R[2])/denom + scanICen);
		jj = (int)((R[3]*xij + R[4]*yij + R[5])/denom + scanJCen);

		//denom = R[6]*xij - R[7]*yij + 1.0;
		//ii = (int)((R[0]*xij - R[1]*yij + R[2])/denom + scanICen);
		//jj = (int)((-R[3]*xij + R[4]*yij - R[5])/denom + scanJCen);

		if(ii<0 || jj<0) continue;
		if(ii>=cols || jj>=rows) continue;

		//If the corresponding pixel in the scan is empty, continue
		if(scan->points[jj*cols+ii].count == 0)
			continue;

		//If there is a valid point at this pixel location, put it into the arrays for least squares.
		if(useXYZ)
		{
			xb[cnt] = scan->x[jj*cols+ii];
			yb[cnt] = scan->y[jj*cols+ii];
			zb[cnt] = scan->z[jj*cols+ii];
		}
		else
		{
			//If the corresponding pixel in the scan is empty, continue
			if(scan->points[jj*cols+ii].count == 0)
				continue;

			xb[cnt] = scan->points[jj*cols+ii].x;
			yb[cnt] = scan->points[jj*cols+ii].y;
			zb[cnt] = scan->points[jj*cols+ii].z;
		}
		d =(xb[cnt]-xa[cnt])*(xb[cnt]-xa[cnt]) + (yb[cnt]-ya[cnt])*(yb[cnt]-ya[cnt]) + (zb[cnt]-za[cnt])*(zb[cnt]-za[cnt]);

		if(d > (error*error*4))
			continue;


		ref->cindex[ix] = scan->points[jj*cols+ii].ix;
				
		cnt++;

	}

	//Perform a point to point least squares alignment - solve for the 4x4 translation/rot matrix.
	retval = solveAlignmentPointToPoint(xa,ya,za,xb,yb,zb,cnt,0.0);

	//scan->transform.multiplyByTransform(retval);

	//retval = mult(solveAlignmentPointToPoint(xa,ya,za,xb,yb,zb,cnt,scan->transform.csz-scan->transform.cz),retval);

	//clean up memory
	free(xa);
	free(ya);
	free(za);
	free(xb);
	free(yb);
	free(zb);

	if(samp != NULL)
		delete samp;

	//apply the transform(which is incremental) to the existing scan transform.
	scan->transform.multiplyByTransform(retval);

	return retval;
}

/*! \fn float ICPAlgorithm::doICP(PointSet** ref, PointSet* scan, const int numRefs, float initialerr, float samprate)

 *  \brief Function that does ICP registration of two PointSets
 *  \param ref - The reference Point Sets - an array of Point sets to which the scan will be registered collectively
 *  \param scan - The target PointSet which is registered to the reference
 *  \param numRefs - The number of refs to use for pseudo-gloabal alignment 
 *  \param initialerr - estimate of initial error - to specify initial correspondance search area
 *  \param samprate - The percentage of points to use for the registration
 *  \exception 
 *  \return boolean success
 */
float ICPAlgorithm::doICP(PointSet** ref, PointSet* scan, const int numRefs, float initialerr, float samprate)
{
	int i;
	double err = 9999;
	double distThresh = initialerr*6.0f;
	PointSetTransform temp;
	PointSetBounds *intersections;
	double* overlapAreas;
	double totOverlap = 0.0f;
	intersections = (PointSetBounds*)malloc(numRefs*sizeof(PointSetBounds));
	overlapAreas = (double*)malloc(numRefs*sizeof(double));
	int totPoints = 0;
	for (i=0; i<numRefs; i++)
	{
		intersections[i] = ref[i]->bounds.getOverlap(&scan->bounds);
		temp = ref[i]->transform;
		temp.invert();
		intersections[i].applyTransform(&temp);
		//contract bounds by error to prevent sampling around outer edge
		intersections[i].adjustBounds(-distThresh, -distThresh, -distThresh);
		overlapAreas[i] = (float)(intersections[i].xmax - intersections[i].xmin)* (float)(intersections[i].ymax - intersections[i].ymin);
		totOverlap += overlapAreas[i];
		totPoints+=ref[i]->numPoints;
	}
	totPoints/=numRefs;
	totPoints = (int)(totPoints*samprate);
	//totPoints is now the number of Points to sample;

	double A[6][6];
	double B[6];
	memset(&A[0][0], 0, 36*sizeof(double));
	memset(&B[0], 0, 6*sizeof(double));		// initialize A and B matrices to minimize point pair distances
	double sum = 0;
	int npairs = 0;
	int* ixes;
	int samples = 0;
	DMatrix scanInv(4,4);
	DMatrix refToScan(4,4);
	UniformSampling * samp;

	for (i=0; i<numRefs; i++)
	{
		int numSamples = (int)(overlapAreas[i]/ totOverlap * totPoints);
		samp = new UniformSampling(ref[i],numSamples,&intersections[i]);
		numSamples = samp->getNumSampledPoints();
		ixes = samp->Sample();

		scanInv = scan->transform.getRotationTranslationMatrix();
		scanInv = scanInv.fastInv();
		refToScan = mult(scanInv,ref[i]->transform.getRotationTranslationMatrix());

		ICPCore(A,B,ref[i],scan,refToScan,ixes,numSamples,distThresh,npairs,sum);
	}

	err = sqrt(sum / float(npairs));

	solveAlignmentTransform(scan,  A,  B);

	free(intersections);
	free(overlapAreas);
	delete samp;

	return err;
}

/*! \fn float ICPAlgorithm::doICP(PointSet* ref, PointSet* scan, float initialerr, float samprate)

 *  \brief Function that does ICP registration of two PointSets
 *  \param ref - The reference Point Set - a Pointset to which the scan will be registered 
 *  \param scan - The target PointSet which is registered to the reference
 *  \param initialerr - estimate of initial error - to specify initial correspondance search area
 *  \param samprate - The percentage of points to use for the registration
 *  \exception 
 *  \return float containing error
 */
float ICPAlgorithm::doICP(PointSet* ref, PointSet* scan,  float initialerr, float samprate)
{
	//use initial error as std.  Multiply by 3 to do a 3 sigma search for nearest points
	double distThresh = initialerr*2.0f;
	distThresh = max(distThresh, minDistThresh);
	double err = 9999.0f;

	// initialize A and B matrices to minimize point pair distances
	double A[6][6];
	double B[6];
	memset(&A[0][0], 0, 36*sizeof(double));
	memset(&B[0], 0, 6*sizeof(double));		
	double sum = 0;
	int npairs = 0;


	//get the intersection in real coordinates, convert to local coordinates
	//by applying the ref's tranform's inverse
	PointSetBounds intersect = ref->bounds.getOverlap(&scan->bounds);
	PointSetTransform temp = ref->transform;
	temp.invert();
	intersect.applyTransform(&temp);

	//contract bounds by error to prevent sampling around outer edge
	intersect.adjustBounds(-distThresh, -distThresh, -distThresh);

	//make sure there is overlap
	if(!intersect.isEmpty())
	{
		//Uniformly sample the overlap region and put the sampled indexes in ixes
		UniformSampling * samp = new UniformSampling(ref,samprate,&intersect);
		int numSamples = samp->getNumSampledPoints();
		int* ixes = samp->Sample();

		// get the transform between the two scans (get scan tf matrix, inverse, multiply by ref tf matrix)
		DMatrix scanInv(4,4);
		DMatrix refToScan(4,4);
		scanInv = scan->transform.getRotationTranslationMatrix();
		scanInv = scanInv.fastInv();
		refToScan = mult(scanInv,ref->transform.getRotationTranslationMatrix());	


		//Run the main ICP loop
		//ICPCoreMulti(A,B,ref,scan,&refToScan,ixes,numSamples,distThresh,npairs,sum);
		ICPCore(A,B,ref,scan,refToScan,ixes,numSamples,distThresh,npairs,sum);

		//calculate the error
		err = 0;

		if(npairs > 0)
		{
			err = sqrt(sum / float(npairs));

			//solve the equation Ax = B, given A and B after Cholesky decomposition
			solveAlignmentTransform(scan,  A,  B);
		}

		delete samp;

	}

	return err;
}


/*! \fn float ICPAlgorithm::doRegistration(PointSet* ref, PointSet* scan)

 *  \brief Function that does registration of two PointSets
 *  \param ref - The reference Point Set - a Pointset to which the scan will be registered 
 *  \param scan - The target PointSet which is registered to the reference
 *  \exception 
 *  \return float containing error
 */
float ICPAlgorithm::doRegistration(PointSet* ref, PointSet* scan)
{
	clock_t before, after;

	PointSetTransform bestTF;
	float bestError = FLT_MAX;

	useXYZ = getParameter("useXYZ",0.0) == 1.0;
	float initialerr = getParameter("initialerror",1.5);

	float samprate = getParameter("samprate",0.30f);
	error = getParameter("error",9999.0f);
	debugOn = (getParameter("debugOn",1.0f) > 0.0f);
	int maxcnt = (int)getParameter("maxiterations",500);
	float preverrorweight = getParameter("preverrorweight",0.75);
	if (preverrorweight < 0.0f || preverrorweight >1.0f)
		preverrorweight = 0.75f;
	float currerrorweight = 1.0f - preverrorweight;

	int cnt = 0;
	float preverror = 1000;


	if(debugOn) before = clock();
	
	// do initial ICP pass to initialize error and previouserror
	error = preverror = doICP(ref, scan, initialerr, samprate);
	printf("ICP Initial Error: %f \n", error);
	error =  doICP(ref, scan, error, samprate);
	printf("ICP Initial Error: %f \n", error);

	bestTF = scan->transform;
	bestError = error;
	
	
	if(debugOn) after = clock();
	if(debugOn) printf("Align took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);

	// Then do ICP in a loop until error stops decreasing
	// Error is filtered using IIR filter spec'd by preverrorweight
	do
	{
		cnt++;	
		if(debugOn) before = clock();
		error = doICP(ref, scan, error, samprate);
		if(error < bestError)
		{
			bestError = error;
			bestTF = scan->transform;
		}

		if(debugOn) after = clock();
		if(debugOn) printf("Align took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);
		preverror = preverrorweight * preverror + currerrorweight * error;

		printf("ICP Error Pass %d: %f \n",cnt, error);
	}
	while ((error - preverror)<-0.0001 && cnt < maxcnt);

	scan->transform = bestTF;
	printf("Best ICP error: %f\n",bestError);

	return error;
}

/*! \fn float ICPAlgorithm::doRegistration(PointSet** scans,  const int numScans)

 *  \brief Function that does registration of a PointSet to numerous references simultaneously
 *  \param scans - The group of scans to be aligned pseudo-globally 
 *  \param numScans - The number of scans
 *  \exception 
 *  \return float containing error
 */
float ICPAlgorithm::doRegistration(PointSet** scans,  const int numScans)
{
	useXYZ = getParameter("useXYZ",0.0) == 1.0;
	float initialerr = getParameter("initialerror",1.5);
	float samprate = getParameter("samprate",0.30f);
	error = getParameter("error",9999.0f);
	bool debugon = (getParameter("debugon",0.0f) > 0.0f);
	int maxcnt = (int)getParameter("maxiterations",500);
	float preverrorweight = getParameter("preverrorweight",0.75);
	if (preverrorweight < 0.0f || preverrorweight >1.0f)
		preverrorweight = 0.75f;
	float currerrorweight = 1.0f - preverrorweight;

	// register the first two scans
	float totalerror = doRegistration(scans[0], scans[1]);

	int cnt = 0;
	float error = 0;;
	float preverror = 1000;

	//now loop through the remaining scans, aligning each to all previous scans
	//a proportional percentage of points will be pulled from each scan based
	//on their area of overlap with the current scan to be registered.
	//The scan will then be registered to this composite scan made of all the
	//previous scans.
	for(int n=2; n<numScans; n++)
	{
		cnt = 0;
		error = 0;;
		preverror = 1000;
		
		//do initial multi-scan ICP to initialize error and preverror
		error = preverror = doICP(scans, scans[n], n, initialerr, samprate);

		// Then do ICP in a loop until error stops decreasing
		// Error is filtered using IIR filter spec'd by preverrorweight
		do
		{
			cnt++;	
			error = doICP(scans, scans[n], n, error, samprate);
			preverror = preverrorweight * preverror + currerrorweight * error;

			if(debugon)
				printf("ICP Error Pass %d: %f \n",cnt, error);
		}
		while (error < preverror && cnt < maxcnt);

	totalerror += error;
	}

	//calculate average error
	error = totalerror / (numScans-1);

	return error;
}


/*! \fn DMatrix ICPAlgorithm::solveAlignmentPointToPoint(  float *x1, float *y1, float *z1,
												float *x2, float *y2, float *z2,
												int n, float *transform)

 *  \brief Function that aligns to sets of points given their x,y,z coordinates
 *  This is not used for the normal ICP algorithm, instead point to plane
 *  minimization is used because it allows the scans to slide over each other.
 *  \param x1 - the x coordinates of the first point set
 *  \param y1 - the y coordinates of the first point set
 *  \param z1 - the z coordinates of the first point set
 *  \param x2 - the x coordinates of the second point set
 *  \param y2 - the y coordinates of the second point set
 *  \param z2 - the z coordinates of the second point set
 *  \param n - The number of points
 *	\param transform - the aligning transform (4x4) is returned in this float array
 *  \exception 
 *  \return void
 */
DMatrix ICPAlgorithm::solveAlignmentPointToPoint(  double *x1, double *y1, double *z1,
												double *x2, double *y2, double *z2,
												int n, double zOffset)
{
	double A[6][6], B[6], sum;
	int i,j,k;

	for(i=0; i<6; i++)
	{
		B[i]=0;
		for(j=0;j<6;j++)
		{
			A[i][j] = 0;
		}
	}

	for (i=0; i<n; i++) 
	{		
		B[0] += (z1[i]-zOffset)*y2[i] - y1[i]*(z2[i]-zOffset);
		B[1] += -(z1[i]-zOffset)*x2[i]+x1[i]*(z2[i]-zOffset);
		B[2] += y1[i]*x2[i]-x1[i]*y2[i];
		B[3] += x1[i] - x2[i];
		B[4] += y1[i] - y2[i];
		B[5] += z1[i] - z2[i];
		A[0][0] += (z1[i]-zOffset)*(z1[i]-zOffset)+y1[i]*y1[i];
		A[0][1] += -y1[i]*x1[i];
		A[0][2] += -(z1[i]-zOffset)*x1[i];
		A[0][3] += 0;
		A[0][4] += -(z1[i]-zOffset);
		A[0][5] += y1[i];
		A[1][1] += (z1[i]-zOffset)*(z1[i]-zOffset)+x1[i]*x1[i];
		A[1][2] += -z1[i]*y1[i];
		A[1][3] += (z1[i]-zOffset);
		A[1][4] += 0;
		A[1][5] += -x1[i];
		A[2][2] += y1[i]*y1[i]+x1[i]*x1[i];
		A[2][3] += -y1[i];
		A[2][4] += x1[i];
		A[2][5] += 0;
		A[3][3] += 1;
		A[3][4] += 0;
		A[3][5] += 0;
		A[4][4] += 1;
		A[4][5] += 0;
		A[5][5] += 1;
	}
	
	double diag[6];
	choldc(A, diag); 

	double x[6];
	cholsl(A, diag, B, x);

	// Interpret results
	double sinx = x[0];
	double cosx = sqrt(1.0 - sinx*sinx);
	double siny = x[1];
	double cosy = sqrt(1.0 - siny*siny);
	double sinz = x[2];
	double cosz = sqrt(1.0 - sinz*sinz);

	double alignxf[16] = { cosy*cosz, sinx*siny*cosz + cosx*sinz, -cosx*siny*cosz + sinx*sinz, 0,
			      -cosy*sinz, -sinx*siny*sinz + cosx*cosz, cosx*siny*sinz + sinx*cosz, 0,
			      siny, -sinx*cosy, cosx*cosy, 0,
			      x[3], x[4], x[5], 1 };


	PointSetTransform pst;

	DMatrix mat(4,4);
	for(int i=0; i<16; i++)
	{
		mat[i%4][i/4] = alignxf[i];
	}

	if(zOffset != 0.0)
	{
		DMatrix applyzoff(4,4);
		DMatrix unapplyzoff(4,4);
		applyzoff = pst.formTranslationMatrix4(0,0,-zOffset);
		unapplyzoff = pst.formTranslationMatrix4(0,0,zOffset);
		mat = mult(mat,applyzoff);
		mat = mult(unapplyzoff, mat);
	}
	return mat;

}


/*! \fn void ICPAlgorithm::solveAlignmentPointToPoint(  float *x1, float *y1, float *z1,
												float *x2, float *y2, float *z2,
												int n, float *transform)

 *  \brief Function that aligns to sets of points given their x,y,z coordinates
 *  This is not used for the normal ICP algorithm, instead point to plane
 *  minimization is used because it allows the scans to slide over each other.
 *  \param x1 - the x coordinates of the first point set
 *  \param y1 - the y coordinates of the first point set
 *  \param z1 - the z coordinates of the first point set
 *  \param x2 - the x coordinates of the second point set
 *  \param y2 - the y coordinates of the second point set
 *  \param z2 - the z coordinates of the second point set
 *  \param n - The number of points
 *	\param transform - the aligning transform (4x4) is returned in this float array
 *  \exception 
 *  \return void
 */
void ICPAlgorithm::solveAlignmentPointToPoint(  double *x1, double *y1, double *z1,
												double *x2, double *y2, double *z2,
												int n, double *transform)
{
	double A[6][6], B[6], sum;
	int i,j,k;

	for(i=0; i<6; i++)
	{
		B[i]=0;
		for(j=0;j<6;j++)
		{
			A[i][j] = 0;
		}
	}

	for (i=0; i<n; i++) 
	{		
		B[0] += z1[i]*y2[i] - y1[i]*z2[i];
		B[1] += -z1[i]*x2[i]+x1[i]*z2[i];
		B[2] += y1[i]*x2[i]-x1[i]*y2[i];
		B[3] += x1[i] - x2[i];
		B[4] += y1[i] - y2[i];
		B[5] += z1[i] - z2[i];
		A[0][0] += z1[i]*z1[i]+y1[i]*y1[i];
		A[0][1] += -y1[i]*x1[i];
		A[0][2] += -z1[i]*x1[i];
		A[0][3] += 0;
		A[0][4] += -z1[i];
		A[0][5] += y1[i];
		A[1][1] += z1[i]*z1[i]+x1[i]*x1[i];
		A[1][2] += -z1[i]*y1[i];
		A[1][3] += z1[i];
		A[1][4] += 0;
		A[1][5] += -x1[i];
		A[2][2] += y1[i]*y1[i]+x1[i]*x1[i];
		A[2][3] += -y1[i];
		A[2][4] += x1[i];
		A[2][5] += 0;
		A[3][3] += 1;
		A[3][4] += 0;
		A[3][5] += 0;
		A[4][4] += 1;
		A[4][5] += 0;
		A[5][5] += 1;
	}
	
	double diag[6];
	choldc(A, diag); 

	double x[6];
	cholsl(A, diag, B, x);

	// Interpret results
	double sinx = x[0];
	double cosx = sqrt(1.0 - sinx*sinx);
	double siny = x[1];
	double cosy = sqrt(1.0 - siny*siny);
	double sinz = x[2];
	double cosz = sqrt(1.0 - sinz*sinz);

	double alignxf[16] = { cosy*cosz, sinx*siny*cosz + cosx*sinz, -cosx*siny*cosz + sinx*sinz, 0,
			      -cosy*sinz, -sinx*siny*sinz + cosx*cosz, cosx*siny*sinz + sinx*cosz, 0,
			      siny, -sinx*cosy, cosx*cosy, 0,
			      x[3], x[4], x[5], 1 };


	for (i=0;i<16;i++) 
	{
		transform[i] = alignxf[i];
	}
}

/*! \fn bool ICPAlgorithm::solveAlignmentTransform(PointSet* r2, float A[6][6], float B[6]) 
 *  \brief After all the elements in the B and A matricies are summed in the main loop,
 *  This routine does a cholesky decomposition and solves, then puts the appropriately adjusted
 *	solution into the 4x4 matrix
 *  \param r2 - the PointSet - it's 4x4 matrix is multiplied by the adjusting transform
 *  \param A - the 6x6 matrix of A values
 *  \param B - the 1x6 matrix of B values
 *  \exception 
 *  \return bool success
 */
bool ICPAlgorithm::solveAlignmentTransform(PointSet* r2, double A[6][6], double B[6]) 
{
	DMatrix applyzoff(4,4);
	DMatrix unapplyzoff(4,4);
	double cx, cy, cz;
	double sx, sy, sz;
	double diag[6];

	//solve equations
	if (!choldc(A, diag)) 
	{
		printf("Couldn't find transform.\n");
		return false;
	}

	double x[6];
	cholsl(A, diag, B, x);

	// Interpret results
	sx = x[0];
	cx = sqrt(1.0 - sx*sx);
	sy = x[1];
	cy = sqrt(1.0 - sy*sy);
	sz = x[2];
	cz = sqrt(1.0 - sz*sz);

	// use the solution to fill in the 4x4 translation rotation matrix
	DMatrix alignxf(4,4);

	alignxf[0][0] = cy*cz;
	alignxf[1][0] = sx*sy*cz + cx*sz;
	alignxf[2][0] = -cx*sy*cz + sx*sz;
	alignxf[3][0] = 0.0f;
	alignxf[0][1] = -cy*sz;
	alignxf[1][1] = -sx*sy*sz + cx*cz;
	alignxf[2][1] = cx*sy*sz + sx*cz;
	alignxf[3][1] = 0.0f;
	alignxf[0][2] = sy;
	alignxf[1][2] = -sx*cy;
	alignxf[2][2] = cx*cy;
	alignxf[3][2] = 0.0f;
	alignxf[0][3] = x[3];
	alignxf[1][3] = x[4];
	alignxf[2][3] = x[5];
	alignxf[3][3] = 1.0f;


	applyzoff = r2->transform.formTranslationMatrix4(0,0,r2->transform.cz-r2->transform.csz);
	unapplyzoff = r2->transform.formTranslationMatrix4(0,0,r2->transform.csz - r2->transform.cz);
		 
	alignxf = mult(alignxf,applyzoff);
	alignxf = mult(unapplyzoff, alignxf);

	//the solution is incremental, so just multiply the existing transform by this adjustment
	r2->transform.multiplyByTransform(alignxf);

	return true;
}


/*! \fn int ICPAlgorithm::doLocalSearch(DVector pt1, PointSet* scan, int ix, int pixelSearchRange) 
 *  \brief The 3D points in a Pointset are mapped into a projection and pixelized (mapped to a 2D grid)
 *	Given a coordinate, this algorithms searches nearby points to find the actual closest point 
 *  \param pt1 - the point to which we'd like to find the closest point in scan
 *  \param scan - the scan to search for the closest point
 *  \param ix - the best guess of the corresponding index in scan from doing a projection into the scan
 *  \param pixelSearchRange - how far away (in pixels) to search for the closest point
 *  \exception 
 *  \return the index to the closest point in scan
 */
int ICPAlgorithm::doLocalSearch(DVector pt1, PointSet* scan, int ix, int pixelSearchRange)
{
	int pr = pixelSearchRange;
	int bestix = 0;
	float bestdist = 1000.0;
	int tempix = 0;
	float distance2 = 0.0f;
	PointNode* node;
	int sx,sy;

	// do local search
	bestix = ix;
	bestdist = 1000.0;
	tempix = 0;
	distance2 = 0.0f;

	// Loop in the x axis through the pixels within pixelSearchRange
	for(sx=-pr; sx<=pr; sx++)
	{
		// Loop in the y axis through the pixels within pixelSearchRange
		for(sy=-pr; sy<=pr; sy++)
		{
			//get the index and make sure we're still in the grid
			tempix = ix + sy*scan->cols + sx;
			if(tempix < 0 || tempix > scan->rows*scan->cols)
				continue;

			//At each grid space, there is a linked list of points, loop through all and calculate
			//the distance to pt1, looking for the closest point
			for(int n = 0; n < scan->points[tempix].count; n++)
			{
				node = &scan->points[tempix];
				distance2 = (float)dist2(pt1,(double)node->x,(double)node->y,(double)node->z);
				
				// If this is the closest point, keep track of it
				if(distance2 < bestdist)
				{
					bestdist = distance2;
					bestix = tempix;
				}
				node = node->next;
			}
		}
	}
	
	//return the index to the closest point
	return bestix;
}


/*! \fn void ICPAlgorithm::ICPCoreMulti(float A[6][6], float B[6], PointSet* ref, PointSet* scan, DMatrix* refToScan, int* ixes, int numSamples, float distThresh, int& npairs, float& sum)
 *  \brief The core ICP routine, set up for multiple threads
 *  \param A - the 6X6 matrix that collects the sum of computations from all points for Ax=B
 *  \param B - 1 x 6 matrix that collects the sum of computations for all points for Ax=B
 *  \param ref - the reference PointSet
 *  \param scan - the PointSet to be aligned to the reference
 *  \param refToScan - the transform between the scan and the reference
 *  \param ixes - the indexes of sampled points (from the reference)
 *  \param numSamples - the number of sampled points
 *  \param npairs - updated by the algorithm - after culling, the number of points used for alignment
 *  \param sum - updated by tha algorithm - a necessary intermediate data product
 *  \exception 
 *  \return void
 */
void ICPAlgorithm::ICPCoreMulti(double A[6][6], double B[6], PointSet* ref, PointSet* scan, DMatrix* refToScan, int* ixes, int numSamples, double distThresh, int& npairs, double& sum)
{
	
	//Create the MultiCoreAlgorthms, passing in ICP loop
	MultiCoreAlgorithm<ICPParameters> m(&ICPAlgorithm::ICPLoop, 0, numSamples);
	
	//Create and fill in the paramters
	ICPParameters* params = (ICPParameters*)malloc(m.getNumThreads()*sizeof(ICPParameters));
	memset(params,0,m.getNumThreads()*sizeof(ICPParameters));

	for(int i=0; i<m.getNumThreads(); i++)
	{
		params[i].ref=ref;
		params[i].scan=scan;
		params[i].refToScan = refToScan;
		params[i].ixes=ixes;
		params[i].numSamples = numSamples;
		params[i].distThresh = distThresh;
		//params[i].npairs = 0;
		//params[i].sum = 0;

	}

	//run the threads
	m.RunThreads(params);

	//collect and combine the thread results
	for(int i=0; i<m.getNumThreads(); i++)
	{
		for(int j=0;j<6; j++)
			for(int k=0;k<6; k++)
				A[j][k]+= params[i].A[j][k];
		for(int j=0;j<6; j++)
			B[j]+= params[i].B[j];

		npairs += params[i].npairs;

		sum += params[i].sum;
	}

}

/*! \fn void ICPAlgorithm::ICPLoop(int start, int stop, int step, ICPParameters* params)
 *  \brief The same as the ICPCore routine but parameters are passed in as a structure
 *  and only a range of indexes are evaluated.  This loop is spawned as multiple threads 
 *  \param start - the loop start index
 *  \param stop - the loop stop index
 *  \param step - the loop step
 *  \param params - the structure containing the paramters necessary for the loop (see ICPCore)
 *  \exception 
 *  \return void
 */
void ICPAlgorithm::ICPLoop(int start, int stop, int step, ICPParameters* params)
{
	clock_t before, after;
	before = clock();

	float distance2 = 0.0f;
	DVector pt14(4);
	DVector pt1(3);
	float d;
	float ccx, ccy, ccz;
	float nx, ny, nz;
	DVector pt2(3);
	DVector normal(3);
	DVector cc(3);
	pt14[3] = 1.0;
	int ix; 

	int pixelSearchRange = (int)ceil(params->distThresh/params->scan->avgSpacing);

		for(int k=start; k<=stop; k+=step)
		{
				pt14[0] = params->ref->x[params->ixes[k]];
				pt14[1] = params->ref->y[params->ixes[k]];
				pt14[2] = params->ref->z[params->ixes[k]];
				
				// Do projection from base image r1 to r2
				// TransformMatrix is 4x4 so point must be 1x4
				pt14 = mult(*params->refToScan, pt14);

				//Put point back in to 3 vector for remaining operations
				pt1[0] = pt14[0];
				pt1[1] = pt14[1];
				pt1[2] = pt14[2];
				
				ix = params->scan->xyz_to_ij((float)pt1[0],(float)pt1[1],(float)pt1[2]);

				ix = doLocalSearch(pt1, params->scan, ix, pixelSearchRange);

				//ix return -1 if out of range
				if(ix < 0)
					continue;
	
				if(params->scan->points[ix].count == 0)
					continue;








				if(useXYZ)
				{
					pt2[0] = params->scan->x[ix];
					pt2[1] = params->scan->y[ix];
					pt2[2] = params->scan->z[ix];
				}
				else
				{
					if(params->scan->points[ix].count == 0)
						continue;

					pt2[0] = params->scan->points[ix].x;
					pt2[1] = params->scan->points[ix].y;
					pt2[2] = params->scan->points[ix].z;
				}

				distance2 = (float)dist2(pt1,pt2);
				if ( distance2 > params->distThresh)
					continue;

				params->ref->sampled[params->ixes[k]] = 1;
				params->ref->cindex[params->ixes[k]] = params->scan->points[ix].ix;


				if(useXYZ)
				{
					params->ref->cindex[params->ixes[k]] = ix;

					// get normal of point on scan
					normal[0] = params->scan->nx[ix];
					normal[1] = params->scan->ny[ix];
					normal[2] = params->scan->nz[ix];
				}
				else
				{
					params->ref->cindex[params->ixes[k]] = params->scan->points[ix].ix;

					// get normal of point on scan
					normal[0] = params->scan->nx[params->scan->points[ix].ix];
					normal[1] = params->scan->ny[params->scan->points[ix].ix];
					normal[2] = params->scan->nz[params->scan->points[ix].ix];
				}

				// sum of vector differences times the normal
				d = (float)((pt1[0] - pt2[0]) * normal[0] + (pt1[1] - pt2[1]) * normal[1] + (pt1[2] - pt2[2]) * normal[2]);

				//cc is cross product of the point and its normal
				cc = crossp(pt2,normal);
				ccx = (float)cc[0];
				ccy = (float)cc[1];
				ccz = (float)cc[2];
				nx = (float)normal[0];
				ny = (float)normal[1];
				nz = (float)normal[2];

				params->npairs++;
				params->sum += d * d;
				params->B[0] += d * ccx;
				params->B[1] += d * ccy;
				params->B[2] += d * ccz;
				params->B[3] += d * nx;
				params->B[4] += d * ny;
				params->B[5] += d * nz;
				params->A[0][0] += ccx * ccx;
				params->A[0][1] += ccx * ccy;
				params->A[0][2] += ccx * ccz;
				params->A[0][3] += ccx * nx;
				params->A[0][4] += ccx * ny;
				params->A[0][5] += ccx * nz;
				params->A[1][1] += ccy * ccy;
				params->A[1][2] += ccy * ccz;
				params->A[1][3] += ccy * nx;
				params->A[1][4] += ccy * ny;
				params->A[1][5] += ccy * nz;
				params->A[2][2] += ccz * ccz;
				params->A[2][3] += ccz * nx;
				params->A[2][4] += ccz * ny;
				params->A[2][5] += ccz * nz;
				params->A[3][3] += nx * nx;
				params->A[3][4] += nx * ny;
				params->A[3][5] += nx * nz;
				params->A[4][4] += ny * ny;
				params->A[4][5] += ny * nz;
				params->A[5][5] += nz * nz;

		}
		after = clock();
		printf("ICP Loop took %f seconds  ", double(after-before)/CLOCKS_PER_SEC);
}

/*! \fn void ICPAlgorithm::ICPCore(float A[6][6], float B[6], PointSet* ref, PointSet* scan, DMatrix* refToScan, int* ixes, int numSamples, float distThresh, int& npairs, float& sum)
 *  \brief The core ICP routine, set up for multiple threads
 *  \param A - the 6X6 matrix that collects the sum of computations from all points for Ax=B
 *  \param B - 1 x 6 matrix that collects the sum of computations for all points for Ax=B
 *  \param ref - the reference PointSet
 *  \param scan - the PointSet to be aligned to the reference
 *  \param refToScan - the transform between the scan and the reference
 *  \param ixes - the indexes of sampled points (from the reference)
 *  \param numSamples - the number of sampled points
 *  \param npairs - updated by the algorithm - after culling, the number of points used for alignment
 *  \param sum - updated by tha algorithm - a necessary intermediate data product
 *  \exception 
 *  \return void
 */
void ICPAlgorithm::ICPCore(double A[6][6], double B[6], PointSet* ref, PointSet* scan, DMatrix refToScan, int* ixes, int numSamples, double distThresh, int& npairs, double& sum)
{
	clock_t before, after;
	before = clock();

	double distThresh2 = distThresh*distThresh;
	double distance2 = 0.0f;
	DVector pt14(4);
	DVector pt1(3);
	double d;
	double ccx, ccy, ccz;
	double nx, ny, nz;
	DVector pt2(3);
	DVector normal(3);
	DVector cc(3);
	pt14[3] = 1.0;
	int ix; 

	int pixelSearchRange = (int)ceil(distThresh/scan->avgSpacing);

	double zOffset = scan->transform.csz - scan->transform.cz;

		for(int k=0; k<numSamples; k++)
		{
				pt14[0] = ref->x[ixes[k]];
				pt14[1] = ref->y[ixes[k]];
				pt14[2] = ref->z[ixes[k]];
				
				// Do projection from base image r1 to r2
				// TransformMatrix is 4x4 so point must be 1x4
				pt14 = mult(refToScan, pt14);

				//Put point back in to 3 vector for remaining operations
				pt1[0] = pt14[0];
				pt1[1] = pt14[1];
				pt1[2] = pt14[2];
				
				ix = scan->xyz_to_ij(pt1[0],pt1[1],pt1[2]);

				ix = doLocalSearch(pt1, scan, ix, pixelSearchRange);


				//ix return -1 if out of range
				if(ix < 0 || ix>scan->rows*scan->cols)
					continue;
	
				if(useXYZ)
				{
					pt2[0] = scan->x[ix];
					pt2[1] = scan->y[ix];
					pt2[2] = scan->z[ix];
				}
				else
				{
					if(scan->points[ix].count == 0)
						continue;

					pt2[0] = scan->points[ix].x;
					pt2[1] = scan->points[ix].y;
					pt2[2] = scan->points[ix].z;
				}

				pt1[2] = pt1[2]-zOffset;

				distance2 = dist2(pt1,pt2);
				if ( distance2 > distThresh2)
					continue;

				ref->sampled[ixes[k]] = 1;
				ref->cindex[ixes[k]] = scan->points[ix].ix;

				if(useXYZ)
				{
					ref->cindex[ixes[k]] = ix;

					// get normal of point on scan
					normal[0] = scan->nx[ix];
					normal[1] = scan->ny[ix];
					normal[2] = scan->nz[ix];
				}
				else
				{
					ref->cindex[ixes[k]] = scan->points[ix].ix;

					// get normal of point on scan
					normal[0] = scan->nx[scan->points[ix].ix];
					normal[1] = scan->ny[scan->points[ix].ix];
					normal[2] = scan->nz[scan->points[ix].ix];
				}

				// sum of vector differences times the normal
				d = ((pt1[0] - pt2[0]) * normal[0] + (pt1[1] - pt2[1]) * normal[1] + (pt1[2] - pt2[2]) * normal[2]);

				//cc is cross product of the point and its normal
				cc = crossp(pt2,normal);
				ccx = cc[0];
				ccy = cc[1];
				ccz = cc[2];
				nx = normal[0];
				ny = normal[1];
				nz = normal[2];

				npairs++;
				sum += d * d;
				B[0] += d * ccx;
				B[1] += d * ccy;
				B[2] += d * ccz;
				B[3] += d * nx;
				B[4] += d * ny;
				B[5] += d * nz;
				A[0][0] += ccx * ccx;
				A[0][1] += ccx * ccy;
				A[0][2] += ccx * ccz;
				A[0][3] += ccx * nx;
				A[0][4] += ccx * ny;
				A[0][5] += ccx * nz;
				A[1][1] += ccy * ccy;
				A[1][2] += ccy * ccz;
				A[1][3] += ccy * nx;
				A[1][4] += ccy * ny;
				A[1][5] += ccy * nz;
				A[2][2] += ccz * ccz;
				A[2][3] += ccz * nx;
				A[2][4] += ccz * ny;
				A[2][5] += ccz * nz;
				A[3][3] += nx * nx;
				A[3][4] += nx * ny;
				A[3][5] += nx * nz;
				A[4][4] += ny * ny;
				A[4][5] += ny * nz;
				A[5][5] += nz * nz;

		}

		after = clock();
		printf("ICP Core %f seconds  ", double(after-before)/CLOCKS_PER_SEC);
}


/*! \fn void ICPAlgorithm::solveAlignmentPointToPoint(  float *x1, float *y1, float *z1,
												float *x2, float *y2, float *z2,
												int n, float *transform)

 *  \brief Function that aligns to sets of points given their x,y,z coordinates
 *  This is not used for the normal ICP algorithm, instead point to plane
 *  minimization is used because it allows the scans to slide over each other.
 *  \param x1 - the x coordinates of the first point set
 *  \param y1 - the y coordinates of the first point set
 *  \param z1 - the z coordinates of the first point set
 *  \param x2 - the x coordinates of the second point set
 *  \param y2 - the y coordinates of the second point set
 *  \param z2 - the z coordinates of the second point set
 *  \param n - The number of points
 *	\param transform - the aligning transform (4x4) is returned in this float array
 *  \exception 
 *  \return void
 */
void ICPAlgorithm::solveAlignmentPointToPointOld(  float *x1, float *y1, float *z1,
												float *x2, float *y2, float *z2,
												int n, float *transform)
{
	double **A, *B ,**ATA, *ATB, sum;
	int i,j,k;
		
	A = (double**)malloc(3*n*sizeof(double*));
	B = (double*)malloc(3*n*sizeof(double));
	ATA = (double**)malloc(6*sizeof(double*));
	ATB = (double*)malloc(3*n*sizeof(double));

	double AA[6][6];
	double BB[6];

	for (i = 0;i<3*n;i++) {
		A[i] = (double*)malloc(6*sizeof(double));
		for (j = 0; j < 6; j++) {
			A[i][j] = 0;
		}
		B[i] = 0;
		ATB[i] = 0;
	}

	for (i = 0; i<6; i++) {
		ATA[i] = (double*)malloc(6*sizeof(double));
		for (j = 0; j<6; j++) {
			ATA[i][j] = 0;
		}
	}

	for (i=0; i<n; i++) {
		A[3*i][0] = 0;
		A[3*i][1] = z2[i];
		A[3*i][2] = -1.0f*y2[i];
		A[3*i][3] = 1.0;
		A[3*i][4] = 0.0;
		A[3*i][5] = 0.0;
		A[3*i+1][0] = z2[i];
		A[3*i+1][1] = 0.0;
		A[3*i+1][2] = x2[i];
		A[3*i+1][3] = 0.0;
		A[3*i+1][4] = 1.0;
		A[3*i+1][5] = 0.0;
		A[3*i+2][0] = -1.0f*y2[i];
		A[3*i+2][1] = -1.0f*x2[i];
		A[3*i+2][2] = 0.0;
		A[3*i+2][3] = 0.0;
		A[3*i+2][4] = 0.0;
		A[3*i+2][5] = 1.0;
		B[3*i] = x1[i] - x2[i];
		B[3*i+1] = y1[i] - y2[i];
		B[3*i+2] = z1[i] - z2[i];
	}
	
	//ATA = A' * A
	for (i=0; i<6; i++) {
		for (j=i; j<6; j++) {
			sum = 0.0;
			for (k=0; k<3*n; k++) {
				sum += A[k][i] * A[k][j];
			}
			AA[i][j] = sum;
			if (i != j)
				AA[j][i] = sum;
		}
	}

	//ATB = A' * B
	for (i=0; i<6; i++) {
		sum = 0.0;
		for (k=0; k<3*n; k++) {
			sum += A[k][i] * B[k];
		}
		BB[i] = sum;
	}

	double diag[6];
	choldc(AA, diag); 

	double x[6];
	cholsl(AA, diag, BB, x);

	// Interpret results
	float sinx = x[0];
	float cosx = (double)sqrt(1.0 - sinx*sinx);
	float siny = x[1];
	float cosy = (double)sqrt(1.0 - siny*siny);
	float sinz = x[2];
	float cosz = (double)sqrt(1.0 - sinz*sinz);

	float alignxf[16] = { cosy*cosz, sinx*siny*cosz + cosx*sinz, -cosx*siny*cosz + sinx*sinz, 0,
			      -cosy*sinz, -sinx*siny*sinz + cosx*cosz, cosx*siny*sinz + sinx*cosz, 0,
			      siny, -sinx*cosy, cosx*cosy, 0,
			      x[3], x[4], x[5], 1 };


	for (i=0;i<16;i++) {
		transform[i] = alignxf[i];
	}
	
	//cleanup
	for (i = 0;i<3*n;i++)
		free(A[i]);

	for (i=0; i<6; i++) {
		free(ATA[i]);
	}
	free(ATA);
	free(ATB);
	free(A);
	free(B);
}


//Test code for Point to Point
//int cnt = 4;
//	DMatrix mat(4,4);
//	double tf[16];
//
//	double* xa = (double*)malloc(cnt*sizeof(double));
//	double* ya = (double*)malloc(cnt*sizeof(double));
//	double* za = (double*)malloc(cnt*sizeof(double));
//	double* xb = (double*)malloc(cnt*sizeof(double));
//	double* yb = (double*)malloc(cnt*sizeof(double));
//	double* zb = (double*)malloc(cnt*sizeof(double));
//
//	PointSetTransform tform;
//
//	xa[0] = -70;
//	ya[0] = -70;
//	za[0] = 500;
//	xb[0] = -70;
//	yb[0] = -70;
//	zb[0] = 505;
//
//	xa[1] = 70;
//	ya[1] = -70;
//	za[1] = 500;
//	xb[1] = 70;
//	yb[1] = -70;
//	zb[1] = 495;
//
//	xa[2] = -70;
//	ya[2] = 70;
//	za[2] = 500;
//	xb[2] = -70;
//	yb[2] = 70;
//	zb[2] = 505;
//
//	xa[3] = 70;
//	ya[3] = 70;
//	za[3] = 500;
//	xb[3] = 70;
//	yb[3] = 70;
//	zb[3] = 495;
//
//
//	mat = solveAlignmentPointToPoint(xa,ya,za,xb,yb,zb,cnt,500);
//
//
//	tform.setRotationTranslationMatrix(mat);
//
//
//	for(int jk =0; jk<cnt;jk++)
//	{
//		printf( "Zdiff before: %f \n", zb[jk]-za[jk]);
//		tform.rotateTranslateVector(xb[jk],yb[jk],zb[jk]);
//		printf( "Zdiff after : %f \n", zb[jk]-za[jk]);
//	}
