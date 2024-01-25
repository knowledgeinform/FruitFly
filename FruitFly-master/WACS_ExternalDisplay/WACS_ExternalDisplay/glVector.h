#ifndef GLVECTOR_H
#define GLVECTOR_H

#include <windows.h>		// Header File For Windows
#include "include\gl\gl.h"			// Header File For The OpenGL32 Library
#include "include\gl\glu.h"			// Header File For The GLu32 Library
#include "include\gl\glaux.h"		// Header File For The Glaux Library


/**
	\class glVector
	\brief An object to represent a vector for an OpenGL scene
	\author Kevin Murphy, John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class glVector  
{
	public:
		
		/**
		\brief Constructor, sets values to default
		\return none
		*/
		glVector();
		
		/**
		\brief Destructor
		*/
		virtual ~glVector();
		
		/**
		\brief Overloaded self-multiplication operator to multiply the vector by a scalar value
		\param scalar Value to multiply i, j, and k by
		\return void
		*/
		void operator *=(GLfloat scalar);
		
		/**
		\brief k-component of vector
		*/
		GLfloat k;
		
		/**
		\brief j-component of vector
		*/
		GLfloat j;
		
		/**
		\brief i-component of vector
		*/
		GLfloat i;
};

#endif
