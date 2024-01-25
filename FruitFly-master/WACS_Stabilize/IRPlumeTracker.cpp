#include "IRPlumeTracker.h"
  
IRPlumeTracker::IRPlumeTracker(void)
{
	m_IAlign.setParameter("repeatSteps",8.0);
	m_IAlign.setParameter("alignSteps",4.0);
	m_IAlign.setParameter("showAlignment",0.0);
	m_IAlign.setParameter("showImages",0.0);
	m_IAlign.setParameter("smoothSearch", 0.0);
	m_IAlign.setParameter("debugOn", 0);
	m_IAlign.setParameter("showDetailedImages", 0.0);

	setParameter("showTrackerImages", 0.0);
	setParameter("showDetections", 0.0);

	m_Width = getParameter("imageWidth", 320);
	m_Height = getParameter("imageHeight", 240);
	m_AlignWidth = getParameter("alignWidth", 240);
	m_AlignHeight = getParameter("alignHeight", 160);
	m_ROIWidth = getParameter("ROIWidth", 240);
	m_ROIHeight = getParameter("ROIHeight", 160);

	setParameter("numFrames", 4);
	setParameter("showTrackerImages", 1.0);
	setParameter("showDetections", 1.0);
	setParameter("detectonThreshold", 0.16);
	setParameter("errorThreshold", 0.60);

	m_NumFrames = getParameter("numFrames", 4);
	_showTrackerImages = (getParameter("showTrackerImages", 0.0)==1.0);
	_showDetections = (getParameter("showDetections", 0.0)==1.0);

	m_Initialized = false;
	m_fastAlign = false;

	m_Score = 0.0;
	m_PrevScore = 0.0;

}

IRPlumeTracker::~IRPlumeTracker(void)
{
}

void IRPlumeTracker::initialize(IplImage* newframe)
{

	m_Width = getParameter("imageWidth", 320);
	m_Height = getParameter("imageHeight", 240);
	m_AlignWidth = getParameter("alignWidth", 240);
	m_AlignHeight = getParameter("alignHeight", 160);
	m_ROIWidth = getParameter("ROIWidth", 240);
	m_ROIHeight = getParameter("ROIHeight", 160);

	m_NumFrames = getParameter("numFrames", 4);
	
	_showTrackerImages = (getParameter("showTrackerImages", 0.0)==1.0);
	_showDetections = (getParameter("showDetections", 0.0)==1.0);

	m_curIdx = 0;
	m_refIdx = 0;
	m_Cnt = 1;
	m_NativeHeight = newframe->height;
	m_NativeWidth = newframe->width;
	m_NativeDepth = newframe->depth;

	tempnativeimg  = cvCreateImage(cvSize(m_NativeWidth,m_NativeHeight), m_NativeDepth,1);
	tempnativefloatimg = cvCreateImage(cvSize(m_NativeWidth,m_NativeHeight), IPL_DEPTH_32F,1);

	m_fastAlign = m_AlignWidth != m_Width || m_AlignHeight != m_Height;

	m_Frames = new IplImage*[m_NumFrames];
	m_DiffFrames = new IplImage*[m_NumFrames];
	m_Transforms = new DVector*[m_NumFrames];
	if (m_fastAlign)
		m_AlignFrames = new IplImage*[m_NumFrames];

	for(int i=0; i<m_NumFrames; i++)
	{
		m_Frames[i] = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
		m_DiffFrames[i] = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
		m_Transforms[i] = new DVector(8);
		if (m_fastAlign)
			m_AlignFrames[i] = cvCreateImage(cvSize(m_AlignWidth,m_AlignHeight), IPL_DEPTH_32F,1);

	}

	//m_PrevImg = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
	//m_CurImg = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);

	warped = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
	diff = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
	tempimg = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
	accum = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);
	warpmask = cvCreateImage(cvSize(m_Width,m_Height), IPL_DEPTH_32F,1);

	
	if(m_fastAlign)
	{
		m_RefAlignImg = cvCreateImage(cvSize(m_AlignWidth,m_AlignHeight), IPL_DEPTH_32F,1);
		m_CurAlignImg = cvCreateImage(cvSize(m_AlignWidth,m_AlignHeight), IPL_DEPTH_32F,1);
	}

	m_Initialized = true;
}

void IRPlumeTracker::convertImage(IplImage* newframe)
{
	bool proc = false;
	if(newframe->nChannels != 1)
	{
		cvSplit(newframe,tempnativeimg, NULL, NULL, NULL);

		if(newframe->depth != IPL_DEPTH_32F)
		{
			cvConvertScale(tempnativeimg,tempnativefloatimg,1.0,0);
			cvNormalize(tempnativefloatimg,tempnativefloatimg, 1.0, 0.0, CV_MINMAX);
			
			//cvNamedWindow("CurImage", CV_WINDOW_AUTOSIZE); 
			//cvShowImage("CurImage",tempnativefloatimg);
			//cvWaitKey(0);

			if( m_NativeWidth != m_Width || m_NativeHeight != m_Height)
				cvResize(tempnativefloatimg,m_CurImg,CV_INTER_NN);
			else
				cvCopy(tempnativefloatimg,m_CurImg);
		}
		else if( m_NativeWidth != m_Width || m_NativeHeight != m_Height)
		{
			cvResize(tempnativeimg,m_CurImg,CV_INTER_NN);
		}
		else
			cvCopy(tempnativeimg,m_CurImg);
	
	}
	else if(newframe->depth != IPL_DEPTH_32F)
	{
		cvConvertScale(newframe,tempnativefloatimg,1.0,0);
		cvNormalize(tempnativefloatimg,tempnativefloatimg, 1.0, 0.0, CV_MINMAX);
		if( m_NativeWidth != m_Width || m_NativeHeight != m_Height)
			cvResize(tempnativefloatimg,m_CurImg,CV_INTER_NN);
		else
			cvCopy(tempnativefloatimg,m_CurImg);
	}
	else if( m_NativeWidth != m_Width || m_NativeHeight != m_Height)
	{
		cvResize(newframe,m_CurImg,CV_INTER_NN);
	}
	else
	{
		cvCopy(newframe,m_CurImg);
	}

	//m_IAlign.highPassFilter(tempimg, m_CurImg, NULL, 5.1f);

	//	cvNamedWindow("Himage", CV_WINDOW_AUTOSIZE); 
	//	cvShowImage("Himage",m_CurImg);
	//	cvWaitKey(0);

	if(m_fastAlign)
	{
		
		cvSetImageROI(m_CurImg,cvRect((m_Width - m_AlignWidth)/2,(m_Height - m_AlignHeight)/2,m_AlignWidth,m_AlignHeight));
		cvCopy(m_CurImg,m_CurAlignImg);
		cvResetImageROI(m_CurImg);
		//cvResize(m_CurImg,m_CurAlignImg,CV_INTER_NN);

		//cvNamedWindow("CurAlignImage", CV_WINDOW_AUTOSIZE); 
		//cvShowImage("CurAlignImage",m_CurAlignImg);
		//cvWaitKey(0);
	}
	

}

void IRPlumeTracker::incIndex()
{
	m_PrevImg = m_Frames[m_curIdx];
	if(m_fastAlign)
	{
		m_RefAlignImg = m_AlignFrames[m_curIdx];
	}
	m_curIdx++;
	if(m_Cnt == m_NumFrames)
	{
		m_refIdx++;
		if(m_refIdx >= m_NumFrames)
		m_refIdx=0;
	}
	if(m_Cnt < m_NumFrames)
		m_Cnt++;
	if(m_curIdx >= m_NumFrames)
		m_curIdx=0;
	
	m_RefImg = m_Frames[m_refIdx];
	m_CurImg = m_Frames[m_curIdx];
	if(m_fastAlign)
	{
		m_CurAlignImg = m_AlignFrames[m_curIdx];
	}
}

double IRPlumeTracker::getAlignError()
{
	return m_AlignError;
}

double IRPlumeTracker::getDetection()
{
	return m_Score;
}

void IRPlumeTracker::addFrame(IplImage* newframe)
{
	float* image1;
	float* image2;
	IplImage* tmp;
	float error;

	_showTrackerImages = (getParameter("showTrackerImages", 0.0)==1.0);
	_showDetections = (getParameter("showDetections", 0.0)==1.0);

	//cvNamedWindow("CurImage", CV_WINDOW_AUTOSIZE); 
	//cvShowImage("CurImage",newframe);
	//cvWaitKey(0);

	if(!m_Initialized)
	{
		initialize(newframe);
		m_PrevImg = m_Frames[0];
		m_CurImg = m_Frames[0];
	
		if(m_fastAlign)
		{
			//tmp = m_RefAlignImg;
			m_RefAlignImg = m_AlignFrames[0];
			m_CurAlignImg = m_AlignFrames[0];
		}

		convertImage(newframe);

		return;
	}

	incIndex();
	convertImage(newframe);
	
	//cvCopy(m_CurImg,m_Frames[m_curIdx]);
	//m_RefImg = m_Frames[m_refIdx];
	//m_CurImg = m_Frames[m_curIdx];

	if(m_fastAlign)
	{
		/*image1 = reinterpret_cast<float*>(m_RefAlignImg->imageData);
		image2 = reinterpret_cast<float*>(m_CurAlignImg->imageData);
		m_IAlign.setRefImage(image1, m_AlignHeight, m_AlignWidth, false,0, 0);
		m_IAlign.setImageToAlign(image2, m_AlignHeight, m_AlignWidth, false,0, 0);*/
		m_IAlign.setRefImage(m_RefAlignImg, 4);
		m_IAlign.setImageToAlign(m_CurAlignImg, 4);
	}
	else
	{
		image1 = reinterpret_cast<float*>(m_PrevImg->imageData);
		image2 = reinterpret_cast<float*>(m_CurImg->imageData);
		m_IAlign.setRefImage(image1, m_Height, m_Width,false,0, 0);
		m_IAlign.setImageToAlign(image2, m_Height, m_Width,false,0, 0);
		//m_IAlign.setRefImage(m_PrevImg, 4);
		//m_IAlign.setImageToAlign(m_CurImg, 4);
	}

	cvAbsDiff(m_PrevImg,m_CurImg,diff);
	if(_showTrackerImages)
	{
		cvNamedWindow("DiffBefore", CV_WINDOW_AUTOSIZE); 
		cvShowImage("DiffBefore",diff);
	}

	before = clock();
	m_AlignError = m_IAlign.alignImages(true);
	after = clock();
	printf("Align Images took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);
	printf("Align Error: %f \n", m_AlignError);

	if(m_AlignError > getParameter("errorThreshold", 0.60))
	{
		int ix = m_curIdx;
		m_refIdx = m_curIdx;
		m_PrevImg = m_Frames[m_curIdx];
		m_CurImg = m_Frames[m_curIdx];
		m_RefImg = m_Frames[m_curIdx];
		if(m_fastAlign)
		{
			m_CurAlignImg = m_AlignFrames[m_curIdx];
			m_RefAlignImg = m_AlignFrames[m_curIdx];
		}
		m_Cnt=1;
		cvWaitKey(1);
		return;
	}

	*m_Transforms[m_curIdx] = m_IAlign.imageTransform;

	DVector imageTransform(8);
	int ix = m_curIdx;
	imageTransform = m_IAlign.imageTransform;
	for(int j=0; j<m_Cnt-2; j++)
	{
		ix--;
		if (ix<0) ix = m_NumFrames-1;
		imageTransform = m_IAlign.combineTransforms(imageTransform, *m_Transforms[ix]);
	}

	//ix = m_curIdx;
	//ix--;
	//if (ix<0) ix = m_NumFrames-1;

	//m_IAlign.warpImageToRef(warped,m_CurImg,NULL,m_IAlign.imageTransform);
	m_IAlign.warpImageToRef(warped,m_CurImg,NULL,imageTransform,warpmask);

	cvAbsDiff(m_RefImg,warped,diff);

	cvMul(warpmask,diff,diff);

	cvErode(diff,diff);
	cvDilate(diff,diff);
	

	if(_showTrackerImages)
	{
		cvNamedWindow("Cur", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Cur",m_CurImg);

		cvNamedWindow("Ref", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Ref",m_RefImg);

		cvNamedWindow("Warped", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Warped",warped);

		cvNamedWindow("DiffAfter", CV_WINDOW_AUTOSIZE); 
		cvShowImage("DiffAfter",diff);
		cvWaitKey(1);
	}

	CvMat* kernel = cvCreateMat(9,9,CV_32F);
	cvSet(kernel,cvScalar(1.0/81));
	cvFilter2D(diff,diff,kernel,cvPoint(kernel->cols/2,kernel->rows/2));

	//if(removeedges)
	//{
	//	cvSmooth(fedges,fedges,CV_GAUSSIAN, 1.5,1.5);
	//	cvSub(sum,fedges,sum);
	//}

	//CvScalar mean;
	//CvScalar stdev;
	//cvAvgSdv(diff,&mean,&stdev);
	
	//cvThreshold(diff,diff,thresh,1.0,CV_THRESH_BINARY);
	//cvSetImageROI(diff,cvRect(0,0,320,240));

	if(_showTrackerImages)
	{
		cvNamedWindow("MotionDetection1", CV_WINDOW_AUTOSIZE); 
		cvShowImage("MotionDetection1",diff);	
	}


	cvCopy(diff, m_DiffFrames[m_curIdx]);
	ix = m_curIdx;
	cvSetZero(accum);

	for(int j=0; j<m_Cnt-1; j++)
	{
		cvAcc(m_DiffFrames[ix],accum);
		ix--;
		if (ix<0) ix = m_NumFrames-1;
	}
	cvConvertScale(accum, diff, (double)(1.0/(m_Cnt-1)), 0);
	double thresh = getParameter("detectionThreshold", 0.12);
	cvSetImageROI(diff,cvRect((m_Width-m_ROIWidth)/2.0,(m_Height-m_ROIHeight)/2.0,m_ROIWidth,m_ROIHeight));
	cvThreshold(diff,diff,thresh,1.0,CV_THRESH_BINARY);

	CvScalar mean = cvAvg(diff);
	m_Score = 0.8 * (mean.val[0] * m_ROIWidth * m_ROIHeight) + 0.2 * m_PrevScore;
	m_PrevScore = m_Score;

	printf("**********************Detection Score: %f **********************\n", m_Score);

	cvResetImageROI(diff);

	if(_showDetections)
	{
		cvNamedWindow("Detection", CV_WINDOW_AUTOSIZE); 
		cvShowImage("Detection",diff);		
	}

	if(_showDetections || _showTrackerImages)
		cvWaitKey(1);

	tmp = m_PrevImg;
	m_PrevImg = m_CurImg;
	m_CurImg = tmp;

	if(m_fastAlign)
	{
		tmp = m_RefAlignImg;
		m_RefAlignImg = m_CurAlignImg;
		m_CurAlignImg = tmp;
	}
}