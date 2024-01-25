//---------------------------------------------------------------------------------------
/*!\class  File Name:	PointSetBounds.cpp
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
//---------------------------------------------------------------------------------------

#include "PointSetBounds.h"


#define MAX(a,b) (a>b?a:b)
#define MIN(a,b) (a<b?a:b)

/*! \fn PointSetBounds::PointSetBounds(void)
 *  \brief Default Constructor - initializes to 0
 *  \param 
 *  \exception 
 *  \return 
 */
PointSetBounds::PointSetBounds(void)
{
	xmin = 0.0;
	ymin = 0.0;
	zmin = 0.0;
	xmax = 0.0;
	ymax = 0.0;
	zmax = 0.0;
}

/*! \fn PointSetBounds::~PointSetBounds(void)
 *  \brief Destructor
 *  \param 
 *  \exception 
 *  \return 
 */
PointSetBounds::~PointSetBounds(void)
{

}

/*! \fn bool PointSetBounds::intersect(PointSetBounds* bounds)
 *  \brief Performs an intersection of the current bounding box with another one
 *  \param bounds -The bounding box with which to check intersection
 *  \exception 
 *  \return True if the boxes intersect, false otherwise
 */
bool PointSetBounds::intersect(PointSetBounds* bounds)
{
	PointSetBounds b;

	b = getOverlap(bounds);

	return !b.isEmpty();
}

/*! \fn PointSetBounds PointSetBounds::getOverlap(PointSetBounds* bounds)
 *  \brief Finds the overlap of the current bounding box with another one
 *  \param bounds -The bounding box with which to check overlap
 *  \exception 
 *  \return PointSetBounds containing the overlap area
 */
PointSetBounds PointSetBounds::getOverlap(PointSetBounds* bounds)
{
	PointSetBounds overlap;

	// Compute rectangle bounds of overlap region
	overlap.xmin = MAX(xmin, bounds->xmin);
	overlap.xmax = MIN(xmax, bounds->xmax);
	overlap.ymin = MAX(ymin, bounds->ymin);
	overlap.ymax = MIN(ymax, bounds->ymax);
	overlap.zmin = MAX(zmin, bounds->zmin);
	overlap.zmax = MIN(zmax, bounds->zmax);

	if(isEmpty())
	{
		overlap.xmin = overlap.ymin = overlap.zmin = overlap.xmax = overlap.ymax = overlap.zmax = 0.0;
	}
	
	return overlap;
}

/*! \fn bool PointSetBounds::isEmpty()
 *  \brief Determines if the PointSetBounds object is not empty (i.e. has positive area)
 *  \exception 
 *  \return true if the bound is empty (i.e. not positive area)
 */
bool PointSetBounds::isEmpty()
{
	if (xmin >= xmax) return true;
	if (ymin >= ymax) return true;
	//if (zmin >= zmax) return true;
	
	return false;
}

/*! \fn bool PointSetBounds::isWithinBounds(float x, float y, float z)
 *  \brief Determines if the point specified is within the bounds
 *  \param x - the x coordinate
 *  \param y - the y coordinate
 *  \param z - the z coordinate
 *  \exception 
 *  \return true if the point is within the bounds
 */
bool PointSetBounds::isWithinBounds(float x, float y, float z)
{
	return (x>=xmin && x<=xmax && y>=ymin && y<=ymax && z>=zmin && z<=zmax);
}

/*! \fn bool PointSetBounds::isWithinXYBounds(float x, float y)
 *  \brief Determines if the point specified is within the XY bounds
 *  \param x - the x coordinate
 *  \param y - the y coordinate
 *  \exception 
 *  \return true if the point is within the bounds
 */
bool PointSetBounds::isWithinXYBounds(float x, float y)
{
	return (x>=xmin && x<=xmax && y>=ymin && y<=ymax);
}

/*! \fn void PointSetBounds::adjustBounds(float x, float y, float z)
 *  \brief Adds the specified amounts to each bounds coordinate max and min
 *   Adding a buffer of the specified width all the way around the overlap
 *  \param x - the x amount by which to increase/decrease the xmax and xmin
 *  \param y - the y amount by which to increase/decrease the ymax and ymin
 *  \param z - the z amount by which to increase/decrease the zmax and zmin
 *  \exception 
 *  \return void
 */
void PointSetBounds::adjustBounds(float x, float y, float z)
{
	this->xmax += x;
	this->xmin -= x;
	this->ymax += y;
	this->ymin -= y;
	this->zmax += z;
	this->zmin -= z;
}

/*! \fn void PointSetBounds::applyTransform(PointSetTransform* tf)
 *  \brief Applies the transform to the bounds, thus adjusting the bounds
 *  \param tf - The transform to use in rotating/translating the bounds
 *  \exception 
 *  \return void
 */
void PointSetBounds::applyTransform(PointSetTransform* tf)
{
	float x,y,z;
	x = (float)xmin;
	y = (float)ymin;
	z = (float)zmin;
	tf->rotateTranslateVector(x,y,z);
	xmin = x;
	ymin = y;
	zmin = z;
	x = (float)xmax;
	y = (float)ymax;
	z = (float)zmax;
	tf->rotateTranslateVector(x,y,z);
	xmax = x;
	ymax = y;
	zmax = z;
}