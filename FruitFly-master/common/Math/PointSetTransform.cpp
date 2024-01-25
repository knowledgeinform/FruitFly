//---------------------------------------------------------------------------------------
/*!\class  File Name:	PointSetTransform.cpp
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


#include "PointSetTransform.h"

#define PI      3.141592653597931
#define DTOR    (PI/180.0)
#define RTOD    (180.0/PI)


/*! \fn PointSetTransform::PointSetTransform()
 *  \brief Default Constructor
 *  \param 
 *  \exception 
 *  \return 
 */
PointSetTransform::PointSetTransform()
:RTMatrix(4,4), Temp(4)
{
	Temp[0] = 0;
	Temp[1] = 0;
	Temp[2] = 0;
	Temp[3] = 0;
	RTMatrix[3][3] = 1;
}

/*! \fn PointSetTransform::~PointSetTransform(void)
 *  \brief Destructor
 *  \param 
 *  \exception 
 *  \return 
 */
PointSetTransform::~PointSetTransform(void)
{
	
}

/*! \fn DMatrix PointSetTransform::getRotationMatrix()
 *  \brief Gets and returns only the rotation matrix
 *  \param 
 *  \exception 
 *  \return DMatrix - 3x3 Rotation Matrix
 */
DMatrix PointSetTransform::getRotationMatrix()
{
	DMatrix retval(3,3);
	for(int i=0; i<3; i++)
		for(int j=0; j<3; j++)
			retval[i][j] = RTMatrix[i][j];

	return retval;
}

/*! \fn void PointSetTransform::setRotationMatrix(DMatrix R)
 *  \brief Sets only the rotation matrix
 *  \param R - 3x3 Rotation Matrix 
 *  \exception 
 *  \return void
 */
void PointSetTransform::setRotationMatrix(DMatrix R)
{
	for(int i=0; i<3; i++)
		for(int j=0; j<3; j++)
			RTMatrix[i][j] = R[i][j];

	//Update the Euler angles
	rot2angles();

}

/*! \fn void PointSetTransform::setRotationTranslationMatrix(DMatrix RT)
 *  \brief Sets only the rotation translation matrix
 *  \param RT - 4x4 Rotation Matrix 
 *  \exception 
 *  \return void
 */
void PointSetTransform::setRotationTranslationMatrix(DMatrix RT)
{
	// set the RT Matrix
	RTMatrix = RT;
	if(RT.n_of_cols()==4 && RT.n_of_rows()==4)
	{
		//update the Euler Angles
		rot2angles();

		//update the translation values
		tx = RTMatrix[0][3];
		ty = RTMatrix[1][3];
		tz = RTMatrix[2][3];
	}
	else
	{
		//Handle Error
	}
}

/*! \fn DMatrix PointSetTransform::getRotationTranslationMatrix()
 *  \brief Gets the rotation translation matrix
 *  \exception 
 *  \return DMatrix the 4x4 Rotation Matrix 
 */
DMatrix PointSetTransform::getRotationTranslationMatrix()
{
	return RTMatrix;
}
	
/*! \fn DMatrix PointSetTransform::getRotationMatrix4()
 *  \brief Gets and returns only the rotation matrix as a 4x4 matrix
 *  \param 
 *  \exception 
 *  \return DMatrix - 4x4 Rotation Matrix (no translation)
 */
DMatrix PointSetTransform::getRotationMatrix4()
{
	DMatrix retval(4,4);
	retval = RTMatrix;
	//zero out translation
	retval[0][3] = 0;
	retval[1][3] = 0;
	retval[2][3] = 0;
	retval[3][3] = 1;

	return retval;
}

/*! \fn DMatrix PointSetTransform::formTranslationMatrix4(double tx, double ty, double tz)
 *  \brief Gets and returns only the translation matrix as a 4x4 matrix
 *  \param tx - the x translation
 *  \param ty - the y translation
 *  \param tz - the z translation
 *  \exception 
 *  \return DMatrix - 4x4 Translation Matrix (no rotation)
 */
DMatrix PointSetTransform::formTranslationMatrix4(double tx, double ty, double tz)
{
	DMatrix retval(4,4);

	for(int i=0; i<=3; i++)
		for(int j=0; j<=3; j++)
		{
			if(i==j)
				retval[i][j] = 1;
			else
				retval[i][j] = 0;
		}

	retval[0][3] = tx;
	retval[1][3] = ty;
	retval[2][3] = tz;

	return retval;

}

/*! \fn void PointSetTransform::setTranslation(double tx, double ty, double tz)
 *  \brief Sets  only the translation values
 *  \param tx - the x translation
 *  \param ty - the y translation
 *  \param tz - the z translation
 *  \exception 
 *  \return void
 */
void PointSetTransform::setTranslation(double tx, double ty, double tz)
{
	this->tx = tx;
	this->ty = ty;
	this->tz = tz;
	RTMatrix[0][3] = tx;
	RTMatrix[1][3] = ty;
	RTMatrix[2][3] = tz;
	RTMatrix[3][3] = 1;
}

/*! \fn DVector PointSetTransform::getTranslation()
 *  \brief Gets the translation values as a vector
 *  \exception 
 *  \return DVector 3x1 translation Vector
 */
DVector PointSetTransform::getTranslation()
{
	DVector retval(3);
	retval[0] = tx;
	retval[1] = ty;
	retval[2] = tz;

	return retval;
}

/*! \fn void PointSetTransform::setRotations(double rx, double ry, double rz)
 *  \brief Sets the rotation values (Euler Angles in degrees)
 *  \param rx - the x axis rotation
 *  \param ry - the y axis rotation
 *  \param rz - the z axis rotation
 *  \exception 
 *  \return void
 */
void PointSetTransform::setRotations(double rx, double ry, double rz)
{
	this->rx = rx;
	this->ry = ry;
	this->rz = rz;

	//Update the 4x4 matrix
	angles2rot();
}


/*! \fn void PointSetTransform::rot2angles()
 *  \brief Compute Euler angles from rotation matrix.
 *  \exception 
 *  \return void
 */
void PointSetTransform::rot2angles()
{
	// Compute Euler angles.
	ry = atan2(-RTMatrix[2][0],sqrt(RTMatrix[0][0]*RTMatrix[0][0]+RTMatrix[1][0]*RTMatrix[1][0]));
	rz = atan2(RTMatrix[1][0]/cos(ry),RTMatrix[0][0]/cos(ry));
	rx = atan2(RTMatrix[2][1]/cos(ry),RTMatrix[2][2]/cos(ry));

	// Check pitch angle.
	if (fabs(ry - PI/2.0) < 1e-8)
	{
		ry = PI/2.0;
		rz = 0.0;
		rx = atan2(RTMatrix[0][1],RTMatrix[1][1]);
	}
	else if (fabs(ry + PI/2.0) < 1e-8)
	{
		ry = -PI/2.0;
		rz = 0.0;
		rx = -atan2(RTMatrix[0][1],RTMatrix[1][1]); 
	}

	// Convert angles to degrees.
	rx *= RTOD;
	ry *= RTOD;
	rz *= RTOD;

}

/*! \fn void PointSetTransform::rot2angles()
 *  \brief Compute rotation matrix from Euler angles (double).
 *  \exception 
 *  \return void
 */
DMatrix PointSetTransform::angles2rot()
{
	// Convert angles to radians.
	rx *= DTOR;
	ry *= DTOR;
	rz *= DTOR;

	// Compute the rotation matrix.
	RTMatrix[0][0] = cos(rz)*cos(ry); 
	RTMatrix[1][0] = sin(rz)*cos(ry);
	RTMatrix[2][0] = -sin(ry);
	RTMatrix[0][1] = cos(rz)*sin(ry)*sin(rx)-sin(rz)*cos(rx);
	RTMatrix[1][1] = sin(rz)*sin(ry)*sin(rx)+cos(rz)*cos(rx);
	RTMatrix[2][1] = cos(ry)*sin(rx);
	RTMatrix[0][2] = cos(rz)*sin(ry)*cos(rx)+sin(rz)*sin(rx);
	RTMatrix[1][2] = sin(rz)*sin(ry)*cos(rx)-cos(rz)*sin(rx);
	RTMatrix[2][2] = cos(ry)*cos(rx);

	return RTMatrix;	
}


/*! \fn void PointSetTransform::rotateVector(float &tx, float &ty, float &tz)
 *  \brief Applies rotation to a vector.
 *  \param tx - the x value of the vector to rotate
 *  \param ty - the y value of the vector to rotate
 *  \param tz - the z value of the vector to rotate
 *  \exception 
 *  \return void
 */
void PointSetTransform::rotateVector(double &tx, double &ty, double &tz)
{
	DVector T(4);
	T[0] = tx;
	T[1] = ty;
	T[2] = tz;
	T[3] = 0;
	T = mult(RTMatrix,T);
	tx = T[0];
	ty = T[1];
	tz = T[2];
}

/*! \fn void PointSetTransform::multiplyByTransform(DMatrix XF)
 *  \brief Multiplies this transform by another
 *  \param XF - the 4x4 matrix by which to multiply this RT matrix
 *  \exception 
 *  \return void
 */
void PointSetTransform::multiplyByTransform(DMatrix XF)
{
	this->RTMatrix = mult(RTMatrix, XF);

	rot2angles();
	tx = RTMatrix[0][3];
	ty = RTMatrix[1][3];
	tz = RTMatrix[2][3];
}


/*! \fn void PointSetTransform::rotateVector(float &tx, float &ty, float &tz)
 *  \brief Rotate and Translate a Vector
 *  \param tx - the x value of the vector to rotate/translate
 *  \param ty - the y value of the vector to rotate/translate
 *  \param tz - the z value of the vector to rotate/translate
 *  \exception 
 *  \return void
 */
void PointSetTransform::rotateTranslateVector(float &tx, float &ty, float &tz)
{
	this->Temp[0] = tx;
	this->Temp[1] = ty;
	this->Temp[2] = tz;
	this->Temp[3] = 1;
	this->Temp = mult(RTMatrix,this->Temp);
	tx = (float)this->Temp[0];
	ty = (float)this->Temp[1];
	tz = (float)this->Temp[2];
}

/*! \fn void PointSetTransform::rotateTranslateVector(double &tx, double &ty, double &tz)
 *  \brief Rotate and Translate a Vector - double function prototype
 *  \param tx - the x value of the vector to rotate/translate
 *  \param ty - the y value of the vector to rotate/translate
 *  \param tz - the z value of the vector to rotate/translate
 *  \exception 
 *  \return void
 */
void PointSetTransform::rotateTranslateVector(double &tx, double &ty, double &tz)
{
	this->Temp[0] = tx;
	this->Temp[1] = ty;
	this->Temp[2] = tz;
	this->Temp[3] = 1;
	this->Temp = mult(RTMatrix,this->Temp);
	tx = this->Temp[0];
	ty = this->Temp[1];
	tz = this->Temp[2];
}

/*! \fn void PointSetTransform::translateVector(float &tx, float &ty, float &tz)
 *  \brief Translate a Vector
 *  \param tx - the x value of the vector to rotate/translate
 *  \param ty - the y value of the vector to rotate/translate
 *  \param tz - the z value of the vector to rotate/translate
 *  \exception 
 *  \return void
 */
void PointSetTransform::translateVector(float &tx, float &ty, float &tz)
{
	tx += this->tx;
	ty += this->ty;
	tz += this->tz;
	
}

/*! \fn void PointSetTransform::translateVector(double &tx, double &ty, double &tz)
 *  \brief Translate a Vector - double function prototype
 *  \param tx - the x value of the vector to rotate/translate
 *  \param ty - the y value of the vector to rotate/translate
 *  \param tz - the z value of the vector to rotate/translate
 *  \exception 
 *  \return void
 */
void PointSetTransform::translateVector(double &tx, double &ty, double &tz)
{
	tx = tx + this->tx;
	ty = ty + this->ty;
	tz = tz + this->tz;
	
}

/*! \fn void PointSetTransform::invert()
 *  \brief Do a fast invert of the 4x4 Rotation/Translation Matrix
 *  \exception 
 *  \return void
 */
void PointSetTransform::invert()
{
	RTMatrix = RTMatrix.fastInv();

	rot2angles();
	tx = RTMatrix[0][3];
	ty = RTMatrix[1][3];
	tz = RTMatrix[2][3];
}

/*! \fn PointSetTransform PointSetTransform::getInverse()
 *  \brief Do a fast invert of the 4x4 Rotation/Translation Matrix
 *  \exception 
 *  \return PointSetTransform of the inverse without actually inverting the current transform
 */
PointSetTransform PointSetTransform::getInverse()
{
	PointSetTransform retval;

	retval.setRotationTranslationMatrix(RTMatrix.fastInv());

	return retval;
}

/*! \fn void rot2angles(DMatrix R, double &rx, double &ry, double &rz)
 *  \brief Static version of compute Euler angles from rotation matrix.
 *  \param R - The rotation matrix to convert to angles
 *  \param rx - The rotation around the x axis
 *  \param ry - The rotation around the y axis
 *  \param rz - The rotation around the z axis
 *  \exception 
 *  \return void
 */
void PointSetTransform::rot2angles(DMatrix R, double &rx, double &ry, double &rz)
{
	// Compute Euler angles.
	ry = atan2(-R[2][0],sqrt(R[0][0]*R[0][0]+R[1][0]*R[1][0]));
	rz = atan2(R[1][0]/cos(ry),R[0][0]/cos(ry));
	rx = atan2(R[2][1]/cos(ry),R[2][2]/cos(ry));

	// Check pitch angle.
	if (fabs(ry - PI/2.0) < 1e-8)
	{
		ry = PI/2.0;
		rz = 0.0;
		rx = atan2(R[0][1],R[1][1]);
	}
	else if (fabs(ry + PI/2.0) < 1e-8)
	{
		ry = -PI/2.0;
		rz = 0.0;
		rx = -atan2(R[0][1],R[1][1]); 
	}

	// Convert angles to degrees.
	rx *= RTOD;
	ry *= RTOD;
	rz *= RTOD;
}

/*! \fn void rot2angles(DMatrix R, double &rx, double &ry, double &rz)
 *  \brief Compute rotation matrix from Euler angles (double).
 *  \param rx - The rotation around the x axis
 *  \param ry - The rotation around the y axis
 *  \param rz - The rotation around the z axis
 *  \exception 
 *  \return the rotation matrix
 */
DMatrix PointSetTransform::angles2rot(double rx, double ry, double rz)
{
	DMatrix R(3,3);

	// Convert angles to radians.
	rx *= DTOR;
	ry *= DTOR;
	rz *= DTOR;

	// Compute the rotation matrix.
	R[0][0] = cos(rz)*cos(ry); 
	R[1][0] = sin(rz)*cos(ry);
	R[2][0] = -sin(ry);
	R[0][1] = cos(rz)*sin(ry)*sin(rx)-sin(rz)*cos(rx);
	R[1][1] = sin(rz)*sin(ry)*sin(rx)+cos(rz)*cos(rx);
	R[2][1] = cos(ry)*sin(rx);
	R[0][2] = cos(rz)*sin(ry)*cos(rx)+sin(rz)*sin(rx);
	R[1][2] = sin(rz)*sin(ry)*cos(rx)-cos(rz)*sin(rx);
	R[2][2] = cos(ry)*cos(rx);

	return R;	
}
