#ifndef GLQUATERNION_H
#define GLQUATERNION_H

#include <windows.h>		// Header File For Windows
#include "include\gl\gl.h"			// Header File For The OpenGL32 Library
#include "include\gl\glu.h"			// Header File For The GLu32 Library
#include "include\gl\glaux.h"		// Header File For The Glaux Library
#include <math.h>


/**
	\class glQuaternion
	\brief An object to represent a quaternion for OpenGL math
	\author Kevin Murphy, John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class glQuaternion  
{
	public:
		/**
		\brief Constructor, sets all values to default
		\return none
		*/
		glQuaternion();

		/**
		\brief Destructor
		*/
		virtual ~glQuaternion();
		
		/**
		\brief Overloaded multiplication operator to combine two glQuaternion objects
		\param q Quaternion to post-multiply to this one
		\return glQuaternion Product of this object and q
		*/
		glQuaternion operator *(glQuaternion q);

		/**
		\brief Using the data of this glQuaternion object, populate a rotation/translation matrix
		\param pMatrix After execution, will be a rotation-translation matrix formed from this quaternion
		\return void
		*/
		void CreateMatrix(GLfloat *pMatrix);

		/**
		\brief Populate this quaternion using specified data
		\param x Rotation axis X vector
		\param y Rotation axis X vector
		\param z Rotation axis X vector
		\param degrees Angle of rotation about axis, degrees
		\return void
		*/
		void CreateFromAxisAngle(GLfloat x, GLfloat y, GLfloat z, GLfloat degrees);
		

	private:
		/**
		\brief Quaternion W
		*/
		GLfloat m_w;
		
		/**
		\brief Quaternion Z
		*/
		GLfloat m_z;
		
		/**
		\brief Quaternion Y
		*/
		GLfloat m_y;
		
		/**
		\brief Quaternion X
		*/
		GLfloat m_x;
};

#endif 
