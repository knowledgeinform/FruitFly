#ifndef WINFONTS_H
#define WINFONTS_H

#include "stdafx.h"

/**
	\class WinFonts
	\brief A class that provides methods for building character fonts for OpenGL scenes.
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class WinFonts 
{
	public:

		/**
		\brief Build the character set for the given parameters
		\param hDC Device context to make the fonts for
		\param GLbase to begin generating at
		\param Fontsize Size of character font
		\return void
		*/
		static GLvoid BuildGLFont(HDC hDC, GLint GLbase, GLint Fontsize);
		
		/**
		\brief Delete the character set
		\param GLbase to begin deleting at
		\return void
		*/
		static GLvoid KillFont(GLint GLbase);

		/**
		\brief Prints the specified text in 2 dimensional OpenGL space
		\param GLbase Base character value
		\param x X position to print text
		\param y Y position to print text
		\param fmt Formatting of text to print
		\param ... Optional parameters to complete the printed string
		\return void
		*/
		static GLvoid glPrint2d(GLint GLbase, double x, double y, const char *fmt, ...);

		/**
		\brief Prints the specified text in 3 dimensional OpenGL space
		\param GLbase Base character value
		\param x X position to print text
		\param y Y position to print text
		\param z Z position to print text
		\param fmt Formatting of text to print
		\param ... Optional parameters to complete the printed string
		\return void
		*/
		static GLvoid glPrint3d(GLint GLbase, double x, double y, double z, const char *fmt, ...);

	private:

		/**
		\brief Array holding character symbols
		*/
		static GLYPHMETRICSFLOAT gmf[256];
};

#endif