#include "stdafx.h"
#include <iostream>
#include "WinFonts.h"

GLYPHMETRICSFLOAT WinFonts::gmf[];

GLvoid WinFonts::BuildGLFont(HDC hDC, GLint GLbase, GLint Fontsize) {
 	glDeleteLists(GLbase, 256);				// Delete All 96 Characters ( NEW )

	HFONT	font;						// Windows Font ID
	HFONT	oldfont;					// Used For Good House Keeping

	glRasterPos2d( 0.0, 0.0 );

	font = CreateFont(	-Fontsize,			// Height Of Font ( NEW )
				0,							// Width Of Font
				0,							// Angle Of Escapement
				0,							// Orientation Angle
				FW_BOLD,					// Font Weight
				FALSE,						// Italic
				FALSE,						// Underline
				FALSE,						// Strikeout
				ANSI_CHARSET,				// Character Set Identifier
				OUT_TT_PRECIS,				// Output Precision
				CLIP_DEFAULT_PRECIS,		// Clipping Precision
				ANTIALIASED_QUALITY,		// Output Quality
				FF_DONTCARE|DEFAULT_PITCH,	// Family And Pitch
				"Courier New");				// Font Name

	oldfont = (HFONT)SelectObject(hDC, font);		// Selects The Font We Want

	wglUseFontBitmaps(hDC, 0, 256, GLbase);			// Builds 96 Characters Starting At Character 32

	SelectObject(hDC, oldfont);				// Selects The Font We Want
	DeleteObject(font);					// Delete The Font
}

GLvoid WinFonts::KillFont(GLint GLbase)						// Delete The Font List
{
 	glDeleteLists(GLbase, 256);				// Delete All 96 Characters ( NEW )
}

GLvoid WinFonts::glPrint2d(GLint GLbase, double x, double y, const char *fmt, ...)				// Custom GL "Print" Routine
{
	float		length=0;
	char		text[256];			// Holds Our String
	va_list		ap;					// Pointer To List Of Arguments

	if (fmt == NULL)				// If There's No Text
		return;						// Do Nothing

	va_start(ap, fmt);				// Parses The String For Variables
    vsprintf_s(text, 256, fmt, ap);		// And Converts Symbols To Actual Numbers
	va_end(ap);						// Results Are Stored In Text

	glRasterPos2d( x, y );
	glPushAttrib(GL_LIST_BIT);		// Pushes The Display List Bits		( NEW )
	glListBase(GLbase);				// Sets The Base Character to 32	( NEW )
	glCallLists(strlen(text), GL_UNSIGNED_BYTE, text);	// Draws The Display List Text	( NEW )
	glPopAttrib();					// Pops The Display List Bits	( NEW )

}

GLvoid WinFonts::glPrint3d(GLint GLbase, double x, double y, double z, const char *fmt, ...)				// Custom GL "Print" Routine
{
	float		length=0;
	char		text[256];			// Holds Our String
	va_list		ap;					// Pointer To List Of Arguments

	if (fmt == NULL)				// If There's No Text
		return;						// Do Nothing

	va_start(ap, fmt);				// Parses The String For Variables
    vsprintf_s(text, 256, fmt, ap);		// And Converts Symbols To Actual Numbers
	va_end(ap);						// Results Are Stored In Text

	glRasterPos3d( x, y, z );
	glPushAttrib(GL_LIST_BIT);		// Pushes The Display List Bits		( NEW )
	glListBase(GLbase);				// Sets The Base Character to 32	( NEW )
	glCallLists(strlen(text), GL_UNSIGNED_BYTE, text);	// Draws The Display List Text	( NEW )
	glPopAttrib();					// Pops The Display List Bits	( NEW )

}