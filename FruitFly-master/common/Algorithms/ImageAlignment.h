//---------------------------------------------------------------------------------------
/*!\class  File Name:	ImageAlignment.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for least squares optical flow (LSOF) alignment of images
//
// \note   Notes:	Algorithm based on LSOF designed by Tom Criss
//
// \note   Routines:  see ImageAlignment class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2008
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------

#pragma once

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <time.h>
#include "Algorithm.h"
#include "matrix.h"


class ImageAlignment: public Algorithm
{
public:
	//!Constructor
	ImageAlignment(void);

	//!Destructor
	~ImageAlignment(void);

	//!Set the reference image to which to align
	void setRefImage(float* i, int rows, int cols, bool fillHoles=false, float validMax=0.0f, float validMin=0.0f);
	
	//!Set the image to align
	void setImageToAlign(float* i, int rows, int cols, bool fillHoles=false, float validMax=0.0f, float validMin=0.0f);
	
	//!Does the alignment
	float alignImages(bool highpass);

	float alignImages();

	//!Copies floating point pixel data into an iplImage
	void copyDataIntoImage(float* iplImage, float* imgData, int rows, int cols, float maxI=0, float minI=0, int iplImageRows=0, int iplImageCols=0);
	
	static void copyByteDataIntoImage(float* iplImage, uchar*imgData, int rows, int cols);

	//!Combines image transforms by mulitplying them
	DVector combineTransforms (DVector R1, DVector R2);

	//!Applies the image transform to a row, column pixel location
	Point2D getWarpedCoordinates(DVector transform, int rowPos, int colPos);

	//!Warps an image using the image transform
	void warpImageToRef(IplImage* imgOut, IplImage* imgIn, bool* imgMask, DVector R, IplImage* maskOut = NULL);

	//!Gets the size of the adjustment in the transform
	double getRMSError(DVector R);

	//!Scores the alignment
	double scoreMatch();

	//!Does the actual image alignment
	DVector align(int smoothWidth, bool SixDOF=true);
	//!Does the actual image alignment
	DVector alignP(int level, bool SixDOF=true);

	//!Performs 2D smoothing
	void smooth2D(IplImage* Z, IplImage* Zout, float width, bool nomask=false);

	//!Filles holes with dilation and erosion
	void fillHoles(IplImage* img);

	//!Clears the image mask
	void initializeMask();

	//!High pass filters an image
	void highPassFilter(IplImage* ImgIn, IplImage* ImgOut, bool* imgMask, float width);

	//!Shows the masks for debugging purposes
	void showMask(bool show = false);

	void setRefImage(IplImage * ref, int levels);
	void setImageToAlign(IplImage * align, int levels);

	//!The image transform (8 length vector)
	DVector imageTransform;

	//!OpenCV images used in the algorithm
	IplImage *refImage, *alignImage, *warpedImage, *displayImage;
	IplImage *smoothedRefImage, *smoothedAlignImage, *mask;
	IplImage *refImageSamp, *alignImageSamp;
	//!Flags to keep track of what memory has been allocated

	//!Image masks
	bool* refImageMask;
	bool* alignImageMask;
	bool _initialized;
	bool _usePyramids;

protected:
	//!Utility variable to hold temp transform
	DVector tempTransform;

	//!Number of steps to repeat a portion of the alignment algorithm
	int repeatSteps;

	//!Shows images for debugging
	bool showImages;
	bool showAlignment;

	//!Shows more images for debugging
	bool showDetailedImages;

	//!The number of smoothing steps available in the algorithm
	static const int smoothSteps = 13;
	static const int smooth[]; 

	//! The min and max pixel values
	static float pixelMin;
	static float pixelMax;

	//! Variable for timing the algorithm
	clock_t before;
	clock_t after;

	//! the number of steps to perform during the alignment procedure
	int alignSteps;

	//! Variable to turn on debug information
	bool debugOn;

	IplImage ** refPyramid;
	IplImage ** alignPyramid;

	//! Constant showing how many data points to skip (speeds up algorithm)
	static const int skip = 1;

	static const int wait = 1;

};
