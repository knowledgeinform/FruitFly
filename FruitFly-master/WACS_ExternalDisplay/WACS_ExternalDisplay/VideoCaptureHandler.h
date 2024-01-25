#ifndef VIDEOCAPTUREHANDLER_H
#define VIDEOCAPTUREHANDLER_H

#include "stdafx.h"

void VideoCapture_StartThread ();
void VideoCapture_StopThread ();
void VideoCapture_CapturePixels (int width, int height, unsigned char* imageData);

#endif