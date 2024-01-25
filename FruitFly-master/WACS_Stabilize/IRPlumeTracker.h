//---------------------------------------------------------------------------------------
/*!\class  File Name:	IRPlumeTracker.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Plume Tracking Algorithm using IR Images of Plume
//
// \note   Notes:	Plume Tracking Algorithm based on LSOF designed by Tom Criss
//
// \note   Routines:  see ImageAlignment class
//
// \note   Copyright (c) 2009 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
// \date   2009
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
#include "ImageAlignment.h"



class IRPlumeTracker: public Algorithm
{
public:
	IRPlumeTracker(void);
	~IRPlumeTracker(void);

	void addFrame(IplImage* newframe);
	
	double getAlignError();
	double getDetection();

	ImageAlignment m_IAlign;

protected:
	void convertImage(IplImage* newframe);
	void incIndex();
	void initialize(IplImage* newframe);

	
	IplImage**     m_Frames;
	IplImage**     m_AlignFrames;
	IplImage**     m_DiffFrames;
	DVector** m_Transforms;
	int m_NumFrames;
	float* m_PrevPtr;
	float* m_CurPtr;
	bool m_Initialized;
	IplImage* m_PrevImg;
	IplImage* m_CurImg;
	IplImage* m_RefImg;
	IplImage* m_RefAlignImg;
	IplImage* m_CurAlignImg;
	IplImage* tempnativeimg;
	IplImage* tempnativefloatimg;
	IplImage* tempimg;
	IplImage* warped;
	IplImage* diff; 
	IplImage* accum;
	IplImage* warpmask;

	int m_curIdx;
	int m_prevIdx;
	int m_refIdx;
	int m_Cnt;

	int m_Width;
	int m_Height;
	int m_NativeWidth;
	int m_NativeHeight;
	int m_NativeDepth;
	int m_AlignWidth;
	int m_AlignHeight;
	int m_ROIWidth;
	int m_ROIHeight;
	double m_Score;
	double m_AlignError;
	double m_PrevScore;

	bool m_fastAlign;
	bool _showTrackerImages;
	bool _showDetections;

	clock_t before, after;


};
