//---------------------------------------------------------------------------------------
/*!\class  File Name:	PointSetTransform.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for 3D Point Sets Transform (4x4 Rotation Translation Matrix)
//
// \note   Notes:	
//
// \note   Routines:  see PointSetTransform class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------


#pragma once

#include "matrix.h"

class PointSetTransform
{
public:

	//!Constructor
	PointSetTransform(void);

	//!Destructor
	~PointSetTransform(void);

	//! The origin of the reference frame
	double cx;			
	double cy;
	double cz;

	//! The centroid of the scan
	double csx;			
	double csy;
	double csz;

	//!Translation
	double tx;			
	double ty;
	double tz;

	//!Euler Angles (degrees)
	double rx;			
	double ry;
	double rz;

	//! The translation/rotation 4x4 matrix
	DMatrix RTMatrix;

	//!utility variable to speed up calcs
	DVector Temp; 

	//!deprecated
	int numPoints;
	double rms;
	int iter;

	//!Sets the 3x3 rotation matrix
	void setRotationMatrix(DMatrix R);
	
	//!Sets the Euler angles
	void setRotations(double rx, double ry, double rz);

	//!Gets the 3x3 rotation matrix
	DMatrix getRotationMatrix();

	//!Gets the rotation matrix as a 4x4 matrix
	DMatrix getRotationMatrix4();

	//!Sets the translation values
	void setTranslation(double tx, double ty, double tz);

	//!Gets the translation as a 3x1 vector
	DVector getTranslation();

	//!Forms a 4x4 translation only matrix
	DMatrix formTranslationMatrix4(double tx, double ty, double tz);
	
	//!Sets the 4x4 Rotation Translation matrix
	void setRotationTranslationMatrix(DMatrix RT);

	//!Gets the 4x4 Rotation Translation matrix
	DMatrix getRotationTranslationMatrix();
	
	//!Rotates a vector
	void rotateVector(double &tx, double &ty, double &tz);

	//!Translates a vector float and double implementations
	void translateVector(float &tx, float &ty, float &tz);
	void translateVector(double &tx, double &ty, double &tz);

	//!Rotates and Translates a vector float and double implementations
	void rotateTranslateVector(float &tx, float &ty, float &tz);
	void rotateTranslateVector(double &tx, double &ty, double &tz);
	
	//!mulitply this transform by another
	void multiplyByTransform(DMatrix XF);

	//!Do a fast invert of this transform
	void invert();

	//!Get the fast invert of this transform, but don't apply to object
	PointSetTransform getInverse();

		//!Static version of conversion functions
	static DMatrix angles2rot(double rx, double ry, double rz);
	static void rot2angles(DMatrix R, double &rx, double &ry, double &rz);

protected:
	
	//! method to convert matrix to Euler Angles
	void rot2angles();

	//! method to convert  Euler Angles to matrix
 	DMatrix angles2rot();

};
