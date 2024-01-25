//---------------------------------------------------------------------------------------
/*!\class  File Name:	PointSetBounds.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for 3D Point Sets Bounds
//
// \note   Notes:	
//
// \note   Routines:  see PointSetBounds class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------

#pragma once

#include "PointSetTransform.h"


class PointSetBounds
{
public:
	
	//!The corners of the bounding cube
	double xmin;
	double xmax;
	double ymin;
	double ymax;
	double zmin;
	double zmax;

	//!Constructor
	PointSetBounds(void);

	//!Destructor
	~PointSetBounds(void);

	//!Finds if there is an intersection between two PointSetBounds
	bool intersect(PointSetBounds* bounds);

	//!Finds the intersection of two PointSetBounds
	PointSetBounds getOverlap(PointSetBounds* bounds);

	//!Checks if the bounding box has positive area(volume)
	bool isEmpty();

	//!Checks if the point is within the bounding cube
	bool isWithinBounds(float x, float y, float z);

	//!Checks if the point is within the xy bounding box
	bool isWithinXYBounds(float x, float y);

	//!Adjusts the bounds by the specified increment in each axis
	void adjustBounds(float x, float y, float z);

	//!Rotates/translates the bounding box
	void applyTransform(PointSetTransform* tf);

};
