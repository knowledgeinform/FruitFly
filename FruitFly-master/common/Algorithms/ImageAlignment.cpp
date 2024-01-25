//---------------------------------------------------------------------------------------
/*!\class  File Name:	ImageAlignment.cpp
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for least squares optical flow (LSOF) alignment of images
//
// \note   Notes:	Algorithm based on LSOF designed by Tom Criss
//
// \note   Routines:  see ImageAlignment class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------


#include "ImageAlignment.h"
#include <time.h>
#include "NRC.h"

//const int ImageAlignment::smooth[] = {0,1,2,4,8,16,32};
const int ImageAlignment::smooth[] = {0,1,3,5,7,9,11,13,17,21,31,37,45};
float ImageAlignment::pixelMax = 0.0;
float ImageAlignment::pixelMin = 0.0;
//const int ImageAlignment::smooth[] = {0,0,1,1,3,9,11,13,17,21,31,37,45};
//const int ImageAlignment::smooth[] = {0,0,1,3,5,7,11,13,17,21,31,37,45};

//const int ImageAlignment::smooth[] = {0,1,2,3,5,7,10,13,17,21,31,37,45};
//const int ImageAlignment::smooth[] = {0,2,8,16,24,48,64,96,128,128,128,128};

/*! \fn ImageAlignment::ImageAlignment(void)
 *  \brief Constructor - initializes image transform to unity, and initializes parameters
 *  \exception 
 *  \return 
 */
ImageAlignment::ImageAlignment(void)
:imageTransform(8), tempTransform(8)
{
	imageTransform[0] = 1;
	imageTransform[1] = 0;
	imageTransform[2] = 0;
	imageTransform[3] = 0;
	imageTransform[4] = 1;
	imageTransform[5] = 0;
	imageTransform[6] = 0;
	imageTransform[7] = 0;

	_initialized = false;

	repeatSteps = (int)getParameter("repeatSteps", 15.0);
	showAlignment = getParameter("showAlignment", 0.0) == 1.0;
	showImages = getParameter("showImages", 0.0) == 1.0;
	showDetailedImages = getParameter("showDetailedImages", 0.0) == 1.0;
	pixelMin = getParameter("pixelMin", -1.0);
	pixelMax = getParameter("pixelMax", 1.0);
	_usePyramids = getParameter("usePyramids", 0.0) == 1.0;


}

/*! \fn ImageAlignment::~ImageAlignment(void)
 *  \brief destructor - cleans up allocated memory
 *  \exception 
 *  \return 
 */
ImageAlignment::~ImageAlignment(void)
{
	if(_initialized)
	{
		cvReleaseImage(&refImage);
		cvReleaseImage(&smoothedRefImage);
		cvReleaseImage(&smoothedAlignImage);
		cvReleaseImage(&mask);
		cvReleaseImage(&displayImage);
		free(refImageMask);
		free(alignImageMask);
		cvReleaseImage(&alignImage);
	}
}

/*! \fn void ImageAlignment::fillHoles(IplImage* img)
 *  \brief Dialates and contracts image to fill holes - used when converting from point set projection
 *  \param img - The image in which to fill holes.
 *  \exception 
 *  \return void
 */
void ImageAlignment::fillHoles(IplImage* img)
{
	IplImage* temp;
	temp = cvCreateImage(cvSize(img->width,img->height), IPL_DEPTH_32F,1);

	cvDilate(img,temp,NULL, 2);
	cvErode(temp,temp,NULL, 2);


	float *data = reinterpret_cast<float*>(img->imageData);
	float *fill = reinterpret_cast<float*>(temp->imageData);
	for(int i=0; i<img->width*img->height; i++)
	{
		if(data[i] <= pixelMin)
			data[i] = fill[i];
	}

	cvReleaseImage(&temp);
}

/*! \fn void ImageAlignment::initializeMask()
 *  \brief Initializes the image mask.  If the pixel value is below the min pixel value, the
 *  point is not used and mask is set to false.
 *  \exception 
 *  \return void
 */
void ImageAlignment::initializeMask()
{
	float *ref = reinterpret_cast<float*>(refImage->imageData);
	float *align = reinterpret_cast<float*>(alignImage->imageData);
	
	// set the ref and align Image mask to all true
	memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
	memset(alignImageMask,true,refImage->width*refImage->height*sizeof(bool));

	// loop through and set invalid pixels to have a false mask
	for(int i=0; i<refImage->width*refImage->height; i++)
	{
		if(ref[i] < pixelMin)
		refImageMask[i] = false;

		if(align[i] < pixelMin)
		alignImageMask[i] = false;
	}
}

/*! \fn void ImageAlignment::setRefImage(float* i, int rows, int cols, bool fill, float validMax, float validMin)
 *  \brief Initializes the image mask.  If the pixel value is below the min pixel value, the
 *  point is not used and mask is set to false.
 *  \param i - The input 2D array of floats representing the image
 *  \param rows - Number of rows in i
 *  \param cols - Number of columns in i
 *  \param fill - flag indicating whether or not to fill holes in the image
 *  \param validMax - The maximum valid pixel value for pixels in i - will be used for normalization
 *  \param validMin - The minimum valid pixel value for pixels in i - will be used for normalization
 *  \exception 
 *  \return void
 */
void ImageAlignment::setRefImage(float* i, int rows, int cols, bool fill, float validMax, float validMin)
{
	showImages = getParameter("showImages", 0.0) == 1.0;
	float *iplImg;

	//allocate memory for the masks
	refImageMask = (bool *)malloc(rows*cols*sizeof(bool));
	alignImageMask = (bool *)malloc(rows*cols*sizeof(bool));
	mask = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);

	//create 32-bit greyscale(one channel) images
	refImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
	smoothedRefImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
	smoothedAlignImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
	warpedImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
	alignImage = cvCreateImage(cvSize(refImage->width,refImage->height), IPL_DEPTH_32F,1);

	//copy i into the IPLImage
	iplImg = reinterpret_cast<float*>(refImage->imageData);
	if(validMax!=0 || validMin!=0)
		copyDataIntoImage(iplImg, i, rows, cols, validMax, validMin);
	else
		copyDataIntoImage(iplImg, i, rows, cols);

	// create an image used for display
	displayImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);

	//keep track of what memory has been allocated
	_initialized = true;

	//highPassFilter(refImage, displayImage, refImage->width/16);
	//cvCopy(displayImage, refImage);

	//Show the image
	if(showImages)
	{
		cvNormalize(refImage, displayImage, 1.0, 0.0, CV_MINMAX);
		cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref Image",displayImage);
		cvWaitKey(wait);
	}

	//Fill in image holes
	if(fill)
		fillHoles(refImage);

	//Show the image
	if(showImages)
	{
		cvNormalize(refImage, displayImage, 1.0, 0.0, CV_MINMAX);
		cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref Image",displayImage);
		cvWaitKey(wait);
	}

}

/*! \fn void ImageAlignment::setImageToAlign(float* i, int rows, int cols,  bool fill, float validMax, float validMin)
 *  \brief Initializes the image mask.  If the pixel value is below the min pixel value, the
 *  point is not used and mask is set to false.
 *  \param i - The input 2D array of floats representing the image
 *  \param rows - Number of rows in i
 *  \param cols - Number of columns in i
 *  \param fill - flag indicating whether or not to fill holes in the image
 *  \param validMax - The maximum valid pixel value for pixels in i - will be used for normalization
 *  \param validMin - The minimum valid pixel value for pixels in i - will be used for normalization
 *  \exception 
 *  \return void
 */
void ImageAlignment::setImageToAlign(float* i, int rows, int cols,  bool fill, float validMax, float validMin)
{
	showImages = getParameter("showImages", 0.0) == 1.0;
	float *iplImg;

	// create IplImages
	IplImage*  tempAlignImage = cvCreateImage(cvSize(refImage->width,refImage->height), IPL_DEPTH_32F,1);

	//copy i image into the IplImage
	iplImg = reinterpret_cast<float*>(tempAlignImage->imageData);

	if(tempAlignImage->width != cols || tempAlignImage->height != rows)
	{
		// If the size of the Image to be Aligned is not the same as the reference
		if(validMax!=0 || validMin!=0)
			copyDataIntoImage(iplImg, i, rows, cols, validMax, validMin, tempAlignImage->height, tempAlignImage->width);
		else
			copyDataIntoImage(iplImg, i, rows, cols, 0, 0, tempAlignImage->height, tempAlignImage->width);
	}
	else
	{
		//If it is the same size
		if(validMax!=0 || validMin!=0)
			copyDataIntoImage(iplImg, i, rows, cols, validMax, validMin);
		else
			copyDataIntoImage(iplImg, i, rows, cols);
	}

	//if(alignImage == NULL)
	//alignImageSamp = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_8U,1);
	//cvConvertScale(alignImage,alignImageSamp,1.0,0);
	//cvCanny(alignImageSamp, alignImageSamp,50,150);

	//Show the image
	if(showImages)
	{
		cvCopy(tempAlignImage, displayImage);
		cvNamedWindow("Align Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Image",displayImage);
	}

	//if(showImages)
	//{
	//	cvNamedWindow("Align Image Samp", CV_WINDOW_AUTOSIZE); 
	//	cvShowImage("Align Image Samp",alignImageSamp);
	//}

	//Fill image holes
	if(fill)
		fillHoles(tempAlignImage);

	// Copy the tempAlignimage into the AlignImage
	cvCopy(tempAlignImage, alignImage);


	//Show the image
	if(showImages)
	{
		cvNormalize(alignImage, displayImage, 1.0, 0.0, CV_MINMAX);
		cvNamedWindow("Align Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Image",displayImage);
	}

	//Initialize and show the masks
	initializeMask();
	showMask();

	//delete temporary Align Image
	cvReleaseImage(&tempAlignImage);

}

/*! \fn void ImageAlignment::copyDataIntoImage(float* iplImage, float*imgData, int rows, int cols,  float maxI, float minI, int iplImageRows, int iplImageCols)
 *  \brief Copies data from a float array, imgData into an iplImage, handles case where images are not the same size
 *  \param iplImage - Pointer to the image data in an IplImage
 *  \param imgData - floating point array containing image data
 *  \param rows - Number of Rows in the imgData and the iplImage if iplImageRows & Cols is 0;
 *  \param cols - Number of Cols in the imgData and the iplImage if iplImageRows & Cols is 0;
 *  \param maxI - The maximum valid pixel value for pixels in i - will be used for normalization
 *  \param minI - The minimum valid pixel value for pixels in i - will be used for normalization
 *  \param iplImageRows - If the size of the iplImage is different than the imgData, then this specifies the rows in iplImage
 *  \param iplImageCols - If the size of the iplImage is different than the imgData, then this specifies the cols in iplImage
 *  \exception 
 *  \return void
 */
void ImageAlignment::copyDataIntoImage(float* iplImage, float*imgData, int rows, int cols,  float maxI, float minI, int iplImageRows, int iplImageCols)
{
	int i,j, ix, ix2;
	double temp;
	double diff, maxdiff;
	float lmin=FLT_MAX, lmax=-FLT_MAX;
	int count = rows*cols;

	//find the max and min pixel data in the incoming imgData
	for (i=0; i<(count); i++) 
	{
		if ( imgData[i] > lmax) lmax = imgData[i];
		if ( imgData[i] < lmin) lmin = imgData[i];
	}

	// if maxI or minI were specified (not NULL) then use them for the max and min pixel
	if(maxI != 0.0f || minI!= 0.0f)
	{
		if(lmax > maxI) lmax = maxI;
		if(lmin < minI) lmin = minI;
	}

	//calculate the pixel value range
	maxdiff = lmax-lmin;
	if(maxdiff < 0.01) maxdiff = 1.0;

	//if the destination image is different size than the source,
	//write the pixels in assuming the image centers are aligned
	//for source images smaller than the destination,  fill in the
	//empty areas with a pixel value less than pixelMin so they
	//will be masked off.
	if(iplImageRows > 0  && iplImageCols > 0)
	{
		int offX = (iplImageCols - cols)/2;
		int offY = (iplImageRows - rows)/2;

		//Step through the rows and columns
		for (j=0; j<iplImageRows; j++)
		{
			for (i=0; i<iplImageCols; i++) 
			{
				//Copy the data over with centers aligned
				ix = j*iplImageCols+i;
				ix2 = (j-offY)*cols + (i-offX);

				iplImage[ix] = pixelMin - 0.01f;

				if(!(j>=offY && (j-offY)<rows && i>=offX && (i-offX)<cols)) continue;

				diff = (imgData[ix2] - lmin);

				if(diff>maxdiff) diff=0;
				if(diff<0) diff=0;

				temp = pixelMin + diff / maxdiff * (pixelMax-pixelMin);
				iplImage[ix] = (float)(temp);
			}
		}

	}
	else
	{
		// If the iplImage and imgData are the same size, copying over is easier
		for (i=0; i<(count); i++) 
		{
			diff = (imgData[i] - lmin);

			if(diff>maxdiff) diff=0;
			if(diff<0) diff=0;

			temp = pixelMin + diff / maxdiff * (pixelMax-pixelMin);

			iplImage[i] = (float)(temp);
		}

	}

}

/*! \fn void ImageAlignment::copyByteDataIntoImage(float* iplImage, char*imgData, int rows, int cols)
 *  \brief Copies data from a float array, imgData into an iplImage, handles case where images are not the same size
 *  \param iplImage - Pointer to the image data in an IplImage
 *  \param imgData - floating point array containing image data
 *  \param rows - Number of Rows in the imgData and the iplImage if iplImageRows & Cols is 0;
 *  \param cols - Number of Cols in the imgData and the iplImage if iplImageRows & Cols is 0;
 *  \exception 
 *  \return void
 */
void ImageAlignment::copyByteDataIntoImage(float* iplImage, uchar*imgData, int rows, int cols)
{
	int i;
	float maxI = 255;
	float minI = 0;
	double temp;
	double diff, maxdiff;
	float lmin=FLT_MAX, lmax=-FLT_MAX;
	int count = rows*cols;

	//find the max and min pixel data in the incoming imgData
	for (i=0; i<(count); i++) 
	{
		if ( imgData[i] > lmax) lmax = imgData[i];
		if ( imgData[i] < lmin) lmin = imgData[i];
	}

	// if maxI or minI were specified (not NULL) then use them for the max and min pixel
	if(maxI != 0.0f || minI!= 0.0f)
	{
		if(lmax > maxI) lmax = maxI;
		if(lmin < minI) lmin = minI;
	}

	//calculate the pixel value range
	maxdiff = lmax-lmin;
	if(maxdiff < 0.01) maxdiff = 1.0;

	
	// If the iplImage and imgData are the same size, copying over is easier
	for (i=0; i<(count); i++) 
	{
		diff = (imgData[i] - lmin);

		if(diff>maxdiff) diff=0;
		if(diff<0) diff=0;

		temp = pixelMin + diff / maxdiff * (pixelMax-pixelMin);

		iplImage[i] = (float)(temp);
	}

}

/*! \fn inline Point2D ImageAlignment::getWarpedCoordinates(DVector transform, int colPos, int rowPos)
 *  \brief Applies projective image  transform to the specified coordinates
 *  \param transform - the 3x3 matrix transform to apply
 *  \param colPos - the pixel column value
 *  \param rowPos - the pixel row value
 *  \exception 
 *  \return Point2D the row and column of the transformed pixel location
 */
inline Point2D ImageAlignment::getWarpedCoordinates(DVector transform, int colPos, int rowPos)
{
	Point2D retval;
	double denom;

	denom = transform[6]*colPos + transform[7]*rowPos + 1.0;
	retval.x = (float)((transform[0]*colPos + transform[1]*rowPos + transform[2])/denom);
	retval.y = (float)((transform[3]*colPos + transform[4]*rowPos + transform[5])/denom);

	return retval;
}

/*! \fn float ImageAlignment::alignImages()
 *  \brief aligns the two images by calculating a 6DOF or 8DOF transform beween the ref and the align image
 *  \exception 
 *  \return the error as a float
 */
float ImageAlignment::alignImages(bool highpass)
{

	if(debugOn)
	{
		before = clock();
	}

	DVector bestTransform(8);
	double error;
	double rmsdiff;
	double minerror = 100.0;
	double sum=0;
	double maxr=0;
	double minr=10000;
	int isave = 0;

	//read in algorithm paramters
	repeatSteps = (int)getParameter("repeatSteps", 15.0);
	showImages = getParameter("showImages", 0.0) == 1.0;
	showAlignment = getParameter("showAlignment", 0.0) == 1.0;
	showDetailedImages = getParameter("showDetailedImages", 0.0) == 1.0;
	alignSteps = (int)getParameter("alignSteps", 5);
	bool smoothSearch = getParameter("smoothSearch", 0.0) == 1.0;
	

	//do a high pass filter on the align image
	if(highpass)
	{
		//highPassFilter(alignImage, warpedImage, alignImageMask, 11.1f);
		highPassFilter(alignImage, warpedImage, alignImageMask, 13.1f);
		cvCopy(warpedImage, alignImage);

		//do a high pass filter on the reference image
		//highPassFilter(refImage, warpedImage, refImageMask, 11.1f);
		highPassFilter(refImage, warpedImage, refImageMask, 13.1f);
		cvCopy(warpedImage, refImage);
	}

	//create and display an image showing the two unaligned images superimposed
	IplImage* Zdiff;
	if(showImages || showAlignment)
	{
		Zdiff = cvCreateImage(cvSize(refImage->width,refImage->height), IPL_DEPTH_32F,1);
		float * data = reinterpret_cast<float*>(Zdiff->imageData);
		float * rdata = reinterpret_cast<float*>(refImage->imageData);
		float * wdata = reinterpret_cast<float*>(alignImage->imageData);
		for(int i=0; i<Zdiff->width*Zdiff->height; i++)
		{
			if(refImageMask[i])
				data[i] = (rdata[i] + wdata[i]) /2;
			else
				data[i] = rdata[i];
		}
	
		cvNormalize(Zdiff,displayImage,1.0, 0.0, CV_MINMAX);
		cvNamedWindow("ZDiffBefore", CV_WINDOW_AUTOSIZE); 
		cvShowImage("ZDiffBefore",displayImage);

		cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref Image",refImage);

		cvNamedWindow("Align Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Image",alignImage);
	}

	imageTransform[0]=1.0;
	imageTransform[1]=0.0;
	imageTransform[2]=0.0;
	imageTransform[3]=0.0;
	imageTransform[4]=1.0;
	imageTransform[5]=0.0;
	imageTransform[6]=0.0;
	imageTransform[7]=0.0;

	// warp the Image to the ref - at this point there should be no image transform
	// but may want to add rough alignment or other offsets before this.
	warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

	//printf("ImageTransform\n");
	//printf("%f  %f  %f\n",imageTransform[0],imageTransform[1],imageTransform[2]);
	//printf("%f  %f  %f\n",imageTransform[3],imageTransform[4],imageTransform[5]);
	//printf("%f  %f  1\n",imageTransform[6],imageTransform[7]);


	if(smoothSearch)
	{
		for(int i=0; i<smoothSteps; i++)
		{
			cvCopy(alignImage, warpedImage);
			
			tempTransform = align(smooth[i]);

			memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
			warpImageToRef(warpedImage, alignImage, alignImageMask, tempTransform);

			if(showDetailedImages)
			{
				cvNormalize(warpedImage,displayImage,1.0,0.0,CV_MINMAX);
				cvNamedWindow("Warped Image", CV_WINDOW_AUTOSIZE); 
				cvShowImage("Warped Image",displayImage);
				cvWaitKey(wait);
			}
			
			error = scoreMatch();

			if(debugOn) printf("\n\nSmooth: %d    %f\n",smooth[i],error);

			 if (error < minerror)
			 {
				 minerror = error;
				 alignSteps = i;
			 }

			 //quit when uphill
			 if (minerror < 0.99*error)
				break;
		}
	}

	//copy the align image to the warped and clear the ref mask
	cvCopy(alignImage, warpedImage);
	memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
	minerror = 100.0;

	// step through repeatsteps at each alignsteps level of smoothing
	for(int i=alignSteps; i>=0; i--)
	{
		for(int j=0; j<repeatSteps; j++)
		{
			// do the alignment and get the incremental transform
			
			if(j<=3)
				tempTransform = align(smooth[i]);
			else
			    tempTransform = align(smooth[i],false);
			
			//adjust the imageTransform with the transform from the align method
			imageTransform = combineTransforms(tempTransform, imageTransform);

			if(debugOn) 
			{
				printf("ImageTransform\n");
				printf("%f  %f  %f\n",imageTransform[0],imageTransform[1],imageTransform[2]);
				printf("%f  %f  %f\n",imageTransform[3],imageTransform[4],imageTransform[5]);
				printf("%f  %f  1\n",imageTransform[6],imageTransform[7]);
			}

			// clear the ref mask and apply the transform to the align image and put in warpedImage
			memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
			warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

			//calculate the error metric of the alignment
			error = scoreMatch();

			//calculate the size of the current transform correction
			rmsdiff = getRMSError(tempTransform);
	
			//if this is the best alignment so far, save it
			if(error<minerror)
			{
				minerror = error;
				bestTransform = imageTransform;
			}
			else if(minerror - error < -0.010) break;

			//if we've stopped improving (i.e. incremental correction is really small) exit
			if (rmsdiff < 0.020 && i > 0) break;
            if (rmsdiff < 0.002) break;

		}//end for repeat steps

		//put the best Transform into the image Transform and adjust the image
		imageTransform = bestTransform;
		warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

		
		//show the mask, and the warped image
		if(showDetailedImages)
			{
				showMask();
				cvNormalize(warpedImage,displayImage,1.0,0.0,CV_MINMAX);
				cvNamedWindow("Warped Image", CV_WINDOW_AUTOSIZE); 
				cvShowImage("Warped Image",displayImage);
				cvWaitKey(wait);
			}

		if(debugOn)
		{
			printf("%f  %f  %f  %f  %f  %f  %f  %f\n",imageTransform[0],imageTransform[1],imageTransform[2],
			imageTransform[3],imageTransform[4],imageTransform[5],imageTransform[6],imageTransform[7]);
		}
	}// end for align steps

	if(debugOn)
	{
		after = clock();
		printf("Align Images took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);
	}

	//Show the final aligned images superimposed
	if(showImages || showAlignment)
	{
		float* data = reinterpret_cast<float*>(Zdiff->imageData);
		float* rdata = reinterpret_cast<float*>(refImage->imageData);
		float* wdata = reinterpret_cast<float*>(warpedImage->imageData);
		for(int i=0; i<Zdiff->width*Zdiff->height; i++)
		{
					data[i] = (rdata[i] + wdata[i]) /2;

		}
		cvNormalize(Zdiff,displayImage,1.0, 0.0, CV_MINMAX);
		cvNamedWindow("ZDiffAfter", CV_WINDOW_AUTOSIZE); 
		cvShowImage("ZDiffAfter",displayImage);


		cvReleaseImage(&Zdiff);
	}

	return (float)minerror;
}

/*! \fn DVector ImageAlignment::combineTransforms (DVector R1, DVector R2)
 *  \brief combines two transform - basically matrix multiplication
 *  \param R1 - the 8 parameter Transform vector
 *  \param R2 - the 8 parameter Transform vector to be adjusted
 *  \exception 
 *  \return the resultant transform as a 8 length DVector
 */
DVector ImageAlignment::combineTransforms (DVector R1, DVector R2)
{
	DVector retval(8);

	double rnorm = (R1[2]*R2[6]+R1[5]*R2[7]+1.0);

	if (abs(rnorm) > 1.0e-6) 
	{
	  retval[0] = (R1[0]*R2[0]+R1[3]*R2[1]+R1[6]*R2[2]) / rnorm;
	  retval[1] = (R1[1]*R2[0]+R1[4]*R2[1]+R1[7]*R2[2]) / rnorm;
	  retval[2] = (R1[2]*R2[0]+R1[5]*R2[1]+      R2[2]) / rnorm;
	  retval[3] = (R1[0]*R2[3]+R1[3]*R2[4]+R1[6]*R2[5]) / rnorm;
	  retval[4] = (R1[1]*R2[3]+R1[4]*R2[4]+R1[7]*R2[5]) / rnorm;
	  retval[5] = (R1[2]*R2[3]+R1[5]*R2[4]+      R2[5]) / rnorm;
	  retval[6] = (R1[0]*R2[6]+R1[3]*R2[7]+R1[6]      ) / rnorm;
	  retval[7] = (R1[1]*R2[6]+R1[4]*R2[7]+R1[7]      ) / rnorm;
	}
	else
	  retval = R1;

	return retval;

}

/*! \fn double ImageAlignment::getRMSError(DVector R)
 *  \brief Finds the average adjustment size in the transform.
 *  \param R - the 8 parameter Transform vector
 *  \exception 
 *  \return the size of the adjustment
 */
double ImageAlignment::getRMSError(DVector R)
{
	double sum = ((R[0]-1)*(R[0]-1)+ R[1]*R[1] + R[2]*R[2]+
				  R[3]*R[3] +(R[4]-1)*(R[4]-1) + R[5]*R[5]+	
                  R[6]*R[6] + R[7]*R[7])/8.0;

	return ::sqrt(sum);
}



/*! \fn void ImageAlignment::warpImageToRef(IplImage* imgOut, IplImage* imgIn, bool* imgMask, DVector R)
 *  \brief Performs a 2D image warp with bi-linear interpolation
 *  \param imgOut - the warped image
 *  \param imgIn  - the intial image
 *  \param imgMask - an image mask showing which points to ignore
 *  \param R - the warping 8DOF transform
 *  \exception 
 *  \return void
 */
void ImageAlignment::warpImageToRef(IplImage* imgOut, IplImage* imgIn, bool* imgMask, DVector R, IplImage* maskOut)
{
//    2 D IMAGE WARP WITH BI-LINEAR INTERPOLATION

	float* imgInData = reinterpret_cast<float*>(imgIn->imageData);
	float* imgOutData = reinterpret_cast<float*>(imgOut->imageData);
	float* mask;

	if (maskOut != NULL)
	{
		mask = reinterpret_cast<float*>(maskOut->imageData);
		cvSet(maskOut,cvScalar(0.0f));
	}

	double xij, yij, x, y, gij;
	int x0, y0, x1, y1;
	double f00, f10, f01, f11;
	double XX, YY;

	int rows = imgIn->height;
	int cols = imgIn->width;

	int j0 = rows/2;
	int i0 = cols/2;

	int imgCols = imgIn->width;

	double maxg = -100000000000.0;
	double ming = 100000000000.0;

	double xulimit = imgIn->width-1-0.01;
	double yulimit = imgIn->height-1-0.01;


	int si,sixp, siyp, sixyp;

	double denom;

	int ix;

	for(int i=0; i<cols; i++)
	  {
		  xij = i - i0;

		  for(int j=0; j<rows; j++)
		  {
			  yij = j - j0;

			  ix = j*cols+i;

			  if (!(imgMask==NULL) && !refImageMask[ix])
			  {
				  imgOutData[ix] = pixelMin;
				  continue;
			  }

			denom = R[6]*xij + R[7]*yij + 1.0;
			x = (R[0]*xij + R[1]*yij + R[2])/denom;
			y = (R[3]*xij + R[4]*yij + R[5])/denom;

			x=x+i0;
			y=y+j0;

			  if( x<0.01 || x > xulimit || y<0.01 || y>yulimit)
			  {
			     if(imgMask != NULL)
					refImageMask[ix] = false;
				 imgOutData[ix] = pixelMin;
				 continue;
			  }

			  x0 = (int)x;
			  y0 = (int)y;
			  x1 = x0 + 1;
			  y1 = y0 + 1;

			  si = y0*imgCols+x0;
			  siyp = y1*imgCols+x0;
			  sixp = y0*imgCols+x1;
			  sixyp = y1*imgCols+x1;

			  if(imgMask != NULL)
			  {
				  if(!imgMask[si] || !imgMask[siyp] || !imgMask[sixp] || !imgMask[sixyp]) 
				  {
					refImageMask[ix]=false;
					imgOutData[ix] = pixelMin;
					continue;
				  }
				  refImageMask[ix] = true;
			  }

			  XX = x - x0;
			  YY = y - y0;

			  f00 = imgInData[si];
			  f01 = imgInData[siyp];
			  f10 = imgInData[sixp];
			  f11 = imgInData[sixyp];

			  gij = f00 + XX*(f10-f00) + YY*(f01-f00)+XX*YY*(f00-f10-f01+f11);

			  //if(gij>maxg) maxg = gij;
			  //if(gij<ming) ming = gij;

			  if(gij > pixelMax) gij = pixelMax;
			  if(gij < pixelMin) gij = pixelMin;
	
			  if (maskOut != NULL)
					mask[ix] = 1.0;

			  imgOutData[ix] = (float)gij;

		  }
	  }
}

/*! \fn void ImageAlignment::showMask()
 *  \brief Shows the image mask for debugging purposes
 *  \exception 
 *  \return void
 */
void ImageAlignment::showMask(bool show)
{
	float * data = reinterpret_cast<float*>(mask->imageData);

	if(showDetailedImages || show)
	{
		for(int i=0; i<mask->width*mask->height; i++)
			if(refImageMask[i])
				data[i] = 1.0;
			else
				data[i] = 0.0;
		cvNamedWindow("Reference Mask", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Reference Mask",mask);
		cvWaitKey(0);
	}

	if(showDetailedImages)
	{
		for(int i=0; i<mask->width*mask->height; i++)
			if(alignImageMask[i])
				data[i] = 1.0;
			else
				data[i] = 0.0;
		cvNamedWindow("Align Mask", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Mask",mask);
		cvWaitKey(0);
	}

}

/*! \fn double ImageAlignment::scoreMatch()
 *  \brief Scores how well the ref fits to the warped image after alignment
 *  \exception 
 *  \return a double representing the alignment error
 */
double ImageAlignment::scoreMatch()
{
	debugOn = getParameter("debugOn", 0)>0;

	const int rows = refImage->height;
	const int cols = refImage->width;

	float* refData = reinterpret_cast<float*>(refImage->imageData);
	float* warpData = reinterpret_cast<float*>(warpedImage->imageData);

	double sum8 = 0.0;
	double wt_8 = 0.0;
	double rmsg;

	float r, w;
	float maxr = 0, maxw=0, minr=100000000, minw=1000000000;

	for(int i=0; i<cols*rows; i+=skip)
	{
        if (refImageMask[i])
		{
			r = refData[i];
			w = warpData[i];

            wt_8 += 1.0;
            sum8 += (r-w)*(r-w);
		}
	}

      if (wt_8 > 0.001)
		  rmsg = ::sqrt(sum8/wt_8);

	  else
          rmsg = 100.0;

	  if(debugOn)
	  {
		printf("Num:  %f  Sum: %f Err: %f\n",wt_8,sum8,rmsg);
	  }

      return  rmsg;
}

/*! \fn void ImageAlignment::smooth2D(IplImage* Z, IplImage* Zout, float width)
 *  \brief Smooths the image in 2D, ignoring pixels with "false" mask
 *  \param Z - the input image 
 *  \param Zout  - the output image
 *  \param width - number reflecting the amount of smoothing (larger is more)
 *  \exception 
 *  \return void
 */
void ImageAlignment::smooth2D(IplImage* Z, IplImage* Zout, float width, bool nomask)
{
//     two directional simple IIR filter in 2-D
//     NORMALIZE TO MAKE D.C. GAIN == UNITY
	  int numpass = 5;	// 5 is full Gaussian - 3 is faster and not bad
	  float FIR_test= 0.05f;
	  float epslon  = 0.05f;

      double a2 =  0.70739030; // ! 0.70739030 ! 0.7074071
      double b2 =  1.11974800; // ! 1.11974800 ! 1.1194160
      double c2 =  0.86117030; // ! 0.86117030 ! 0.8566601

      float wnew = 1.0;
      float wold = 0.0;

      int cols = Z->width;
      int rows = Z->height;
	  int ix;
	  cvCopy(Z,Zout);

	  bool* G = refImageMask;

      if (cols <= 3 || rows <=3) return;

      float awidth = abs(width);
      if (awidth < epslon) return;

	  float s, zlast;
	  bool good_last;

	  float* ZData = reinterpret_cast<float*>(Zout->imageData);

      if (awidth > FIR_test)
	  {
          s = 1.0f/awidth;
          wnew = (float)(sqrt((float)(numpass)) * s / (a2 + b2*s + c2*s*s));
          if (wnew > 1.0) wnew = 1.0;
          if (wnew < 0.0) wnew = 0.0;
          wold = 1.0f - wnew;

        // ACROSS:
          for(int j=0; j<rows; j++)
		  {
             for (int ipass=1; ipass<numpass; ipass++)
			 {
				
              // FORWARDS:
                good_last = false;
                 for(int i=0; i<cols; i++)
				 {
				   ix = j*cols+i;
                   if (good_last) 
				   {
                       zlast= wnew*ZData[ix] + wold*zlast;
                       ZData[ix] = zlast;
				   }
                   else
                       zlast = ZData[ix];
                   
				   if(!nomask)
					good_last = G[ix];
				   else
					good_last = true;
				 }

				// BACKWARDS:
                good_last = false;
                for(int i=cols-1; i>=0 ; i--)
				{
				   ix = j*cols+i;
                   if (good_last) 
				   {
                       zlast= wnew*ZData[ix] + wold*zlast;
                       ZData[ix] = zlast;
				   }
                   else
                       zlast = ZData[ix];

				  if(!nomask)
					good_last = G[ix];
				   else
					good_last = true;
				}
			}
		  }

		// DOWN:
          for(int i=0; i<cols; i++)
		  {
             for (int ipass=0; ipass<numpass; ipass++)
			 {
				 
              // FORWARDS:
                good_last = false;
                for(int j=0; j<rows; j++)
				{
					ix = j*cols+i;
                   if (good_last) 
				   {
                       zlast= wnew*ZData[ix] + wold*zlast;
                       ZData[ix] = zlast;
				   }
                   else
                       zlast = ZData[ix];
            
                   if(!nomask)
					good_last = G[ix];
				   else
					good_last = true;
				}

              //BACKWARDS:
                good_last = false;
                for(int j=rows-1; j>=0; j--)
				{
					ix = j*cols+i;
                   if (good_last) 
				   {
                       zlast= wnew*ZData[ix] + wold*zlast;
                       ZData[ix] = zlast;
				   }
                   else
				   {
                       zlast = ZData[ix];
				   }
                   
                   if(!nomask)
					good_last = G[ix];
				   else
					good_last = true;
				}
			 }
		  }

	  }

}

/*! \fn DVector ImageAlignment::align(int smoothWidth, bool SixDOF)
 *  \brief Performs the image alignment between ref and align images, returning the alignment transform
 *  \param smoothWidth - number reflecting the amount of smoothing (larger is more)
 *  \param SixDOF - Perform 6DOF alignment, otherwise, do 8DOF
 *  \exception 
 *  \return DVector (8 values) containing the transform
 */
DVector ImageAlignment::align(int smoothWidth, bool SixDOF)
{
	//Smooth the two images

	float* ref = reinterpret_cast<float *>(smoothedRefImage->imageData);
	float* align = reinterpret_cast<float *>(smoothedAlignImage->imageData);

	smooth2D(refImage, smoothedRefImage, (float)smoothWidth);
	smooth2D(warpedImage, smoothedAlignImage, (float)smoothWidth);

	int rows = refImage->height;
	int cols = refImage->width;

	if(showDetailedImages)
	{
		cvNormalize(warpedImage,displayImage,1.0,0.0,CV_MINMAX);
		cvNamedWindow("Warped Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Warped Image",displayImage);
		cvWaitKey(0);

		cvNormalize(smoothedRefImage,displayImage,1.0,0.0,CV_MINMAX);
		cvNamedWindow("Smoothed Ref", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Smoothed Ref",displayImage);
		cvWaitKey(0);

		cvNormalize(smoothedAlignImage,displayImage,1.0,0.0,CV_MINMAX);
		cvNamedWindow("Smoothed Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Smoothed Image",displayImage);
		cvWaitKey(0);
	}

	int index, indexjp, indexjm, indexip, indexim;
	double xij, yij, dgdx, dgdy, dgij, dg2;
	int j0 = rows/2;
	int i0 = cols/2;
	double A[8][8];
	double B[8];
	double ATemp[8];
	
	//clear the A and B Matrices
	for(int i=0; i<8; i++)
	{
		ATemp[i] = 0;
		B[i] = 0.0;
		for(int j=0; j<8; j++)
		{
			A[i][j] = 0.0;
		}
	}

	double maxa, maxb, maxc;
	double mina, minb, minc;

	maxa=maxb=maxc = 0;
	mina=minb=minc = 10000;

	//float* ref = reinterpret_cast<float *>(smoothedRefImage->imageData);
	//float* align = reinterpret_cast<float *>(smoothedAlignImage->imageData);

    //char* refs = reinterpret_cast<char *>(refImageSamp->imageData);
	//char* aligns = reinterpret_cast<char *>(alignImageSamp->imageData);

	//loop through rows and columns

	//for(int k=0; k<rows*cols*0.9; k++)
	//{

	for(int j=1; j<rows-1; j+=skip)
	{
		//int j = ((double)(rand()/32768.0) * (rows-1))+1 ;
		//int i = ((double)(rand()/32768.0) * (cols-1))+1 ;

		yij = (double)(j - j0);

		//if(yij >67)
		//	yij = yij;

		for(int i=1; i<cols-1; i+=skip)
		{
			index = j*cols + i;
			//if(refs[index] < 128 && aligns[index] < 128) continue;

			xij = (double)(i-i0);

			indexjp = (j+1)*cols + i;
			indexjm = (j-1)*cols + i;
			indexip = j*cols + i + 1;
			indexim = j*cols + i - 1;

			


			// if any of the surrounding pixels are masked, continue
			if(!refImageMask[index]) continue;
			if(!refImageMask[indexjp]) continue;
			if(!refImageMask[indexjm]) continue;
			if(!refImageMask[indexip]) continue;
			if(!refImageMask[indexim]) continue;

			// Calculate the image pixel gradients
			if(SixDOF)
			{
				dgij = ref[index] - align[index];
				dgdx = (align[indexip] - align[indexim])/2.0;
				dgdy = (align[indexjp] - align[indexjm])/2.0;
			}
			else
			{
				dgij = ref[index] - align[index];
				dgdx = (align[indexip] - align[indexim])/2.0;
				dgdy = (align[indexjp] - align[indexjm])/2.0;
				dg2 = dgij + dgdx*xij + dgdy*yij;
			}

			// use the gradients and the pixel values to fill in A and B matrices
			if(SixDOF)
			{
				ATemp[0] = dgdx*xij;
				ATemp[1] = dgdx*yij;
				ATemp[2] = dgdx;
				ATemp[3] = dgdy*xij;
				ATemp[4] = dgdy*yij;
				ATemp[5] = dgdy;


				B[0] += dgij*ATemp[0];
				B[1] += dgij*ATemp[1];
				B[2] += dgij*ATemp[2];
				B[3] += dgij*ATemp[3];
				B[4] += dgij*ATemp[4];
				B[5] += dgij*ATemp[5];

				//A*ATranspose
				for(int ii=0; ii<6; ii++)
					for(int jj=0; jj<6; jj++)
						A[ii][jj] += ATemp[ii] * ATemp[jj];


			}
			else
			{
				ATemp[0] = dgdx*xij;
				ATemp[1] = dgdx*yij;
				ATemp[2] = dgdx;
				ATemp[3] = dgdy*xij;
				ATemp[4] = dgdy*yij;
				ATemp[5] = dgdy;
				ATemp[6] = (-dg2)*xij;
				ATemp[7] = (-dg2)*yij;

				B[0] += dg2*ATemp[0];
				B[1] += dg2*ATemp[1];
				B[2] += dg2*ATemp[2];
				B[3] += dg2*ATemp[3];
				B[4] += dg2*ATemp[4];
				B[5] += dg2*ATemp[5];
				B[6] += dg2*ATemp[6];
				B[7] += dg2*ATemp[7];

				//A*ATranspose
				for(int ii=0; ii<8; ii++)
					for(int jj=0; jj<8; jj++)
						A[ii][jj] += ATemp[ii] * ATemp[jj];

			}

		
		}

	}

	DVector Ret(8);
	
	if(SixDOF)
	{
		// Put data into DVectors and DMatrices
		DVector R(6);
		DVector BB(6);
		DMatrix AA(6,6);
		for(int ii=0; ii<6; ii++)
				for(int jj=0; jj<6; jj++)
						AA[ii][jj] = A[ii][jj];

		for(int jj=0; jj<6; jj++)
						BB[jj] = B[jj];

		//invert Matrix
		AA = AA.i();

		//Multiply both sides by AA inverse to solve
		R = mult(AA, BB);

		Ret[0] = R[0]+1;
		Ret[1] = R[1];
		Ret[2] = R[2];
		Ret[3] = R[3];
		Ret[4] = R[4]+1;
		Ret[5] = R[5];
		Ret[6] = 0.0;
		Ret[7] = 0.0;
	}
	else
	{
		// Put data into DVectors and DMatrices
		DVector R(8);
		DVector BB(8);
		DMatrix AA(8,8);
		for(int ii=0; ii<8; ii++)
				for(int jj=0; jj<8; jj++)
						AA[ii][jj] = A[ii][jj];

		for(int jj=0; jj<8; jj++)
						BB[jj] = B[jj];

		//invert Matrix
		AA = AA.i();

		//Multiply both sides by AA inverse to solve
		R = mult(AA, BB);

		Ret[0] = R[0];
		Ret[1] = R[1];
		Ret[2] = R[2];
		Ret[3] = R[3];
		Ret[4] = R[4];
		Ret[5] = R[5];
		Ret[6] = R[6];
		Ret[7] = R[7];
	}

	return Ret;

}

/*! \fn void ImageAlignment::highPassFilter(IplImage* ImgIn, IplImage* ImgOut, bool* imgMask, float width)
 *  \brief Performs the image alignment between ref and align images, returning the alignment transform
 *  \param ImgIn - The input image
 *  \param ImgOut - The output image after high pass filtering
 *  \param imgMask - Mask of pixels to ignore
 *  \param width - The filter width
 *  \exception 
 *  \return void
 */
void ImageAlignment::highPassFilter(IplImage* ImgIn, IplImage* ImgOut, bool* imgMask, float width)
{
	double sum = 0.0;
	double mean,stdev;
	int count = 0;
	if(imgMask==NULL)
		smooth2D(ImgIn, ImgOut, width,true); 
	else
		smooth2D(ImgIn, ImgOut, width);
	float* data = reinterpret_cast<float*>(ImgOut->imageData);
	float* image = reinterpret_cast<float*>(ImgIn->imageData);
	for(int i=0; i<ImgOut->width*ImgOut->height; i++)
	{
		if(imgMask==NULL || imgMask[i])
		{
			data[i] = image[i] - data[i];
			sum += data[i];
			count++;
		}
		else
			data[i] = 0;
	}

		if(count == 0) 
			mean = 0.0;
		else
			mean = sum/count;
			sum=0;
	for(int i=0; i<ImgOut->width*ImgOut->height; i++)
	{	
		if(imgMask==NULL ||imgMask[i])
		{
			data[i] = data[i] - (float)mean;
			sum+=data[i]*data[i];
		}
	}
	stdev=::sqrt(sum/count);

	if(debugOn)
		printf("Num:  %d    Avg: %f   Std: %f\n",count,mean,stdev);

	for(int i=0; i<ImgOut->width*ImgOut->height; i++)
	{	
		if(imgMask==NULL || imgMask[i])
		{
			if(stdev != 0.0)
				data[i] = data[i]/(float)stdev;
			if(data[i]<pixelMin) data[i] = pixelMin;
			if(data[i]>pixelMax) data[i] = pixelMax;
		}
	}

}

/*! \fn void ImageAlignment::setRefImage(float* i, int rows, int cols, bool fill, float validMax, float validMin)
 *  \brief Initializes the image mask.  If the pixel value is below the min pixel value, the
 *  point is not used and mask is set to false.
 *  \param i - The input 2D array of floats representing the image
 *  \param rows - Number of rows in i
 *  \param cols - Number of columns in i
 *  \param fill - flag indicating whether or not to fill holes in the image
 *  \param validMax - The maximum valid pixel value for pixels in i - will be used for normalization
 *  \param validMin - The minimum valid pixel value for pixels in i - will be used for normalization
 *  \exception 
 *  \return void
 */
void ImageAlignment::setRefImage(IplImage * ref, int levels)
{
	showImages = getParameter("showImages", 0.0) == 1.0;


	int rows = ref->height;
	int cols = ref->width;

	//create 32-bit greyscale(one channel) images
	if(!_initialized)
	{
			//allocate memory for the masks
		refImageMask = (bool *)malloc(rows*cols*sizeof(bool));
		alignImageMask = (bool *)malloc(rows*cols*sizeof(bool));
		mask = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);

		warpedImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		displayImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		refImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		smoothedRefImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		smoothedAlignImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		warpedImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
		alignImage = cvCreateImage(cvSize(cols,rows), IPL_DEPTH_32F,1);
	}
	if(_usePyramids)
	{
		refPyramid = new IplImage*[levels];
		alignPyramid = new IplImage*[levels];

		int w = ref->width;
		int h = ref->height;
		for(int i=0; i<levels; i++)
		{
			refPyramid[i] = cvCreateImage(cvSize(w,h), IPL_DEPTH_32F,1);
			alignPyramid[i] = cvCreateImage(cvSize(w,h), IPL_DEPTH_32F,1);
			w=w/2;
			h=h/2;
		}
		refImage = refPyramid[0];
		alignImage = alignPyramid[0];

		highPassFilter(ref, refPyramid[0], refImageMask, 11.1f);

		for(int i=1; i<levels; i++)
		{
			cvPyrDown(refPyramid[i-1],refPyramid[i]);
		}

		//Show the image
		if(showImages)
		{
			cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
			cvShowImage("Ref Image",refPyramid[0]);
			cvWaitKey(wait);
		}
	}
	else
	{
		cvNormalize(ref,refImage);
	}


	//Show the image
	if(showImages)
	{

		cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref Image",ref);
		cvWaitKey(wait);
		
	}

	//keep track of what memory has been allocated
	_initialized = true;


}

/*! \fn void ImageAlignment::setImageToAlign(float* i, int rows, int cols,  bool fill, float validMax, float validMin)
 *  \brief Initializes the image mask.  If the pixel value is below the min pixel value, the
 *  point is not used and mask is set to false.
 *  \param i - The input 2D array of floats representing the image
 *  \param rows - Number of rows in i
 *  \param cols - Number of columns in i
 *  \param fill - flag indicating whether or not to fill holes in the image
 *  \param validMax - The maximum valid pixel value for pixels in i - will be used for normalization
 *  \param validMin - The minimum valid pixel value for pixels in i - will be used for normalization
 *  \exception 
 *  \return void
 */
void ImageAlignment::setImageToAlign(IplImage * align, int levels)
{
	showImages = getParameter("showImages", 0.0) == 1.0;

	//keep track of allocated memory for destructor

	if(_usePyramids)
	{
		highPassFilter(align, alignPyramid[0], refImageMask, 11.1f);
		for(int i=1; i<levels; i++)
		{
			cvPyrDown(alignPyramid[i-1],alignPyramid[i]);
		}
		//Show the image
		if(showImages)
		{
			cvNamedWindow("Align Image0", CV_WINDOW_AUTOSIZE); 
			cvShowImage("Align Image0",alignPyramid[0]);
			cvNamedWindow("Align Image1", CV_WINDOW_AUTOSIZE); 
			cvShowImage("Align Image1",alignPyramid[1]);
			cvNamedWindow("Align Image2", CV_WINDOW_AUTOSIZE); 
			cvShowImage("Align Image2",alignPyramid[2]);
			cvNamedWindow("Align Image3", CV_WINDOW_AUTOSIZE); 
			cvShowImage("Align Image3",alignPyramid[3]);
		}
	}
	else
	{
		cvNormalize(align,alignImage);
	}

	if(showImages)
	{
		cvNamedWindow("Align Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Image",align);
		cvWaitKey(wait);
	}

	//Initialize and show the masks
	initializeMask();
	showMask();

}


/*! \fn DVector ImageAlignment::align(int smoothWidth, bool SixDOF)
 *  \brief Performs the image alignment between ref and align images, returning the alignment transform
 *  \param smoothWidth - number reflecting the amount of smoothing (larger is more)
 *  \param SixDOF - Perform 6DOF alignment, otherwise, do 8DOF
 *  \exception 
 *  \return DVector (8 values) containing the transform
 */
DVector ImageAlignment::alignP(int level, bool SixDOF)
{
	//Smooth the two images

	float* ref = reinterpret_cast<float *>(refPyramid[level]->imageData);
	float* align = reinterpret_cast<float *>(alignPyramid[level]->imageData);


	int rows = refPyramid[level]->height;
	int cols = alignPyramid[level]->width;


	int index, indexjp, indexjm, indexip, indexim;
	double xij, yij, dgdx, dgdy, dgij, dg2;
	int j0 = rows/2;
	int i0 = cols/2;
	double A[8][8];
	double B[8];
	double ATemp[8];
	
	//clear the A and B Matrices
	for(int i=0; i<8; i++)
	{
		ATemp[i] = 0;
		B[i] = 0.0;
		for(int j=0; j<8; j++)
		{
			A[i][j] = 0.0;
		}
	}

	double maxa, maxb, maxc;
	double mina, minb, minc;

	maxa=maxb=maxc = 0;
	mina=minb=minc = 10000;

	for(int j=1; j<rows-1; j+=skip)
	{
		yij = (double)(j - j0);

		for(int i=1; i<cols-1; i+=skip)
		{
			index = j*cols + i;

			xij = (double)(i-i0);

			indexjp = (j+1)*cols + i;
			indexjm = (j-1)*cols + i;
			indexip = j*cols + i + 1;
			indexim = j*cols + i - 1;

			// if any of the surrounding pixels are masked, continue
			if(!refImageMask[index]) continue;
			if(!refImageMask[indexjp]) continue;
			if(!refImageMask[indexjm]) continue;
			if(!refImageMask[indexip]) continue;
			if(!refImageMask[indexim]) continue;

			// Calculate the image pixel gradients
			if(SixDOF)
			{
				dgij = ref[index] - align[index];
				dgdx = (align[indexip] - align[indexim])/2.0;
				dgdy = (align[indexjp] - align[indexjm])/2.0;
			}
			else
			{
				dgij = ref[index] - align[index];
				dgdx = (align[indexip] - align[indexim])/2.0;
				dgdy = (align[indexjp] - align[indexjm])/2.0;
				dg2 = dgij + dgdx*xij + dgdy*yij;
			}

			// use the gradients and the pixel values to fill in A and B matrices
			if(SixDOF)
			{
				ATemp[0] = dgdx*xij;
				ATemp[1] = dgdx*yij;
				ATemp[2] = dgdx;
				ATemp[3] = dgdy*xij;
				ATemp[4] = dgdy*yij;
				ATemp[5] = dgdy;


				B[0] += dgij*ATemp[0];
				B[1] += dgij*ATemp[1];
				B[2] += dgij*ATemp[2];
				B[3] += dgij*ATemp[3];
				B[4] += dgij*ATemp[4];
				B[5] += dgij*ATemp[5];

				//A*ATranspose
				for(int ii=0; ii<6; ii++)
					for(int jj=0; jj<6; jj++)
						A[ii][jj] += ATemp[ii] * ATemp[jj];
			}
			else
			{
				ATemp[0] = dgdx*xij;
				ATemp[1] = dgdx*yij;
				ATemp[2] = dgdx;
				ATemp[3] = dgdy*xij;
				ATemp[4] = dgdy*yij;
				ATemp[5] = dgdy;
				ATemp[6] = (-dg2)*xij;
				ATemp[7] = (-dg2)*yij;

				B[0] += dg2*ATemp[0];
				B[1] += dg2*ATemp[1];
				B[2] += dg2*ATemp[2];
				B[3] += dg2*ATemp[3];
				B[4] += dg2*ATemp[4];
				B[5] += dg2*ATemp[5];
				B[6] += dg2*ATemp[6];
				B[7] += dg2*ATemp[7];

				//A*ATranspose
				for(int ii=0; ii<8; ii++)
					for(int jj=0; jj<8; jj++)
						A[ii][jj] += ATemp[ii] * ATemp[jj];

			}

		
		}

	}

	DVector Ret(8);
	
	if(SixDOF)
	{
		// Put data into DVectors and DMatrices
		DVector R(6);
		DVector BB(6);
		DMatrix AA(6,6);
		for(int ii=0; ii<6; ii++)
				for(int jj=0; jj<6; jj++)
						AA[ii][jj] = A[ii][jj];

		for(int jj=0; jj<6; jj++)
						BB[jj] = B[jj];

		//invert Matrix
		AA = AA.i();

		//Multiply both sides by AA inverse to solve
		R = mult(AA, BB);

		Ret[0] = R[0]+1;
		Ret[1] = R[1];
		Ret[2] = R[2];
		Ret[3] = R[3];
		Ret[4] = R[4]+1;
		Ret[5] = R[5];
		Ret[6] = 0.0;
		Ret[7] = 0.0;
	}
	else
	{
		// Put data into DVectors and DMatrices
		DVector R(8);
		DVector BB(8);
		DMatrix AA(8,8);
		for(int ii=0; ii<8; ii++)
				for(int jj=0; jj<8; jj++)
						AA[ii][jj] = A[ii][jj];

		for(int jj=0; jj<8; jj++)
						BB[jj] = B[jj];

		//invert Matrix
		AA = AA.i();

		//Multiply both sides by AA inverse to solve
		R = mult(AA, BB);

		Ret[0] = R[0];
		Ret[1] = R[1];
		Ret[2] = R[2];
		Ret[3] = R[3];
		Ret[4] = R[4];
		Ret[5] = R[5];
		Ret[6] = R[6];
		Ret[7] = R[7];
	}

	return Ret;

}

/*! \fn float ImageAlignment::alignImages()
 *  \brief aligns the two images by calculating a 6DOF or 8DOF transform beween the ref and the align image
 *  \exception 
 *  \return the error as a float
 */
float ImageAlignment::alignImages()
{
	debugOn = (getParameter("debugOn",0.0f) > 0.0f);

	if(debugOn)
	{
		before = clock();
	}

	DVector bestTransform(8);
	double error;
	double rmsdiff;
	double minerror = 100.0;
	double sum=0;
	double maxr=0;
	double minr=10000;
	int isave = 0;

	//read in algorithm paramters
	repeatSteps = (int)getParameter("repeatSteps", 15.0);
	showImages = getParameter("showImages", 0.0) == 1.0;
	showAlignment = getParameter("showAlignment", 0.0) == 1.0;
	showDetailedImages = getParameter("showDetailedImages", 0.0) == 1.0;
	alignSteps = (int)getParameter("alignSteps", 5);
	bool smoothSearch = getParameter("smoothSearch", 0.0) == 1.0;
	

	//create and display an image showing the two unaligned images superimposed
	IplImage* Zdiff;
	if(showImages || showAlignment)
	{
		Zdiff = cvCreateImage(cvSize(refImage->width,refImage->height), IPL_DEPTH_32F,1);
		float * data = reinterpret_cast<float*>(Zdiff->imageData);
		float * rdata = reinterpret_cast<float*>(refImage->imageData);
		float * wdata = reinterpret_cast<float*>(alignImage->imageData);
		for(int i=0; i<Zdiff->width*Zdiff->height; i++)
		{
			if(refImageMask[i])
				data[i] = (rdata[i] + wdata[i]) /2;
			else
				data[i] = rdata[i];
		}
	
		cvNormalize(Zdiff,displayImage,1.0, 0.0, CV_MINMAX);
		cvNamedWindow("ZDiffBefore", CV_WINDOW_AUTOSIZE); 
		cvShowImage("ZDiffBefore",displayImage);

		cvNamedWindow("Ref Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref Image",refImage);

		cvNamedWindow("Align Image", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Align Image",alignImage);
	}

	// warp the Image to the ref - at this point there should be no image transform
	// but may want to add rough alignment or other offsets before this.
	warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

	if(smoothSearch)
	{
		for(int i=0; i<smoothSteps; i++)
		{
			cvCopy(alignImage, warpedImage);
			
			tempTransform = alignP(i);

			memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
			warpImageToRef(warpedImage, alignImage, alignImageMask, tempTransform);

			if(showDetailedImages)
			{
				cvNormalize(warpedImage,displayImage,1.0,0.0,CV_MINMAX);
				cvNamedWindow("Warped Image", CV_WINDOW_AUTOSIZE); 
				cvShowImage("Warped Image",displayImage);
				cvWaitKey(wait);
			}
			
			error = scoreMatch();

			if(debugOn) printf("\n\nSmooth: %d    %f\n",smooth[i],error);

			 if (error < minerror)
			 {
				 minerror = error;
				 alignSteps = i;
			 }

			 //quit when uphill
			 if (minerror < 0.99*error)
				break;
		}
	}

	//copy the align image to the warped and clear the ref mask
	cvCopy(alignImage, warpedImage);
	memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
	minerror = 100.0;

	// step through repeatsteps at each alignsteps level of smoothing
	for(int i=alignSteps-1; i>=0; i--)
	{
		for(int j=0; j<repeatSteps; j++)
		{
			// do the alignment and get the incremental transform
			
			if(j<=1)
				tempTransform = alignP(i);
			else
			    tempTransform = alignP(i,false);
			
			//adjust the imageTransform with the transform from the align method
			imageTransform = combineTransforms(tempTransform, imageTransform);

			if(debugOn) 
			{
				printf("ImageTransform\n");
				printf("%f  %f  %f\n",imageTransform[0],imageTransform[1],imageTransform[2]);
				printf("%f  %f  %f\n",imageTransform[3],imageTransform[4],imageTransform[5]);
				printf("%f  %f  1\n",imageTransform[6],imageTransform[7]);
			}

			// clear the ref mask and apply the transform to the align image and put in warpedImage
			memset(refImageMask,true,refImage->width*refImage->height*sizeof(bool));
			warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

			//calculate the error metric of the alignment
			error = scoreMatch();

			//calculate the size of the current transform correction
			rmsdiff = getRMSError(tempTransform);
	
			//if this is the best alignment so far, save it
			if(error<minerror)
			{
				minerror = error;
				bestTransform = imageTransform;
			}
			else if(minerror - error < -0.010) break;

			//if we've stopped improving (i.e. incremental correction is really small) exit
			if (rmsdiff < 0.020 && i > 0) break;
            if (rmsdiff < 0.002) break;

		}//end for repeat steps

		//put the best Transform into the image Transform and adjust the image
		imageTransform = bestTransform;
		warpImageToRef(warpedImage, alignImage, alignImageMask, imageTransform);

		
		//show the mask, and the warped image
		if(showDetailedImages)
			{
				showMask();
				cvNormalize(warpedImage,displayImage,1.0,0.0,CV_MINMAX);
				cvNamedWindow("Warped Image", CV_WINDOW_AUTOSIZE); 
				cvShowImage("Warped Image",displayImage);
				cvWaitKey(wait);
			}

		if(debugOn)
		{
			printf("%f  %f  %f  %f  %f  %f  %f  %f\n",imageTransform[0],imageTransform[1],imageTransform[2],
			imageTransform[3],imageTransform[4],imageTransform[5],imageTransform[6],imageTransform[7]);
		}
	}// end for align steps

	if(debugOn)
	{
		after = clock();
		printf("Align Images took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);
	}

	//Show the final aligned images superimposed
	if(showImages || showAlignment)
	{
		float* data = reinterpret_cast<float*>(Zdiff->imageData);
		float* rdata = reinterpret_cast<float*>(refImage->imageData);
		float* wdata = reinterpret_cast<float*>(warpedImage->imageData);
		for(int i=0; i<Zdiff->width*Zdiff->height; i++)
		{
					data[i] = (rdata[i] + wdata[i]) /2;

		}
		cvNormalize(Zdiff,displayImage,1.0, 0.0, CV_MINMAX);
		cvNamedWindow("ZDiffAfter", CV_WINDOW_AUTOSIZE); 
		cvShowImage("ZDiffAfter",displayImage);


		cvReleaseImage(&Zdiff);
	}

	return (float)minerror;
}