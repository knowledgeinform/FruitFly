#ifndef GLPOINT_H
#define GLPOINT_H

#include "stdafx.h"

/**
	\class glPoint
	\brief An object to represent a point in an OpenGL scene
	\author Kevin Murphy, John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class glPoint  
{
	public:

		/**
		\brief Constructor, sets values to default
		\return none
		*/
		glPoint();

		/**
		\brief Destructor
		*/
		virtual ~glPoint();

		
		/**
		\brief z position of this point
		*/
		GLfloat z;
		
		/**
		\brief y position of this point
		*/
		GLfloat y;
		
		/**
		\brief x position of this point
		*/
		GLfloat x;
};

#endif 
