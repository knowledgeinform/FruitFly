#include "stdafx.h"
#include "GCSDisplay.h"
#include "Config.h"
#include <math.h>

GCSDisplay::GCSDisplay()
{
	readConfig();
	displayList = -1;

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);

	generateCallList();

}

GCSDisplay::~GCSDisplay()
{
	gluDeleteQuadric(quadricObj);		
}

void GCSDisplay::readConfig()
{
	gcsScale = Config::getInstance()->getValueAsDouble("GCSDisplay.gcsScale", 100);
	truckColorRed = Config::getInstance()->getValueAsDouble("GCSDisplay.truckColorRed", 0.0);
	truckColorBlue = Config::getInstance()->getValueAsDouble("GCSDisplay.truckColorBlue", 0.0);
	truckColorGreen = Config::getInstance()->getValueAsDouble("GCSDisplay.truckColorGreen", 0.6);
	wheelColorRed = Config::getInstance()->getValueAsDouble("GCSDisplay.wheelColorRed", 0.0);
	wheelColorBlue = Config::getInstance()->getValueAsDouble("GCSDisplay.wheelColorBlue", 0.0);
	wheelColorGreen = Config::getInstance()->getValueAsDouble("GCSDisplay.wheelColorGreen", 0.0);
	wheelRadiusM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.wheelRadiusM", 0.2);
	wheelThicknessM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.wheelThicknessM", 0.2);
	truckLengthM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.truckLengthM", 5);
	truckWidthM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.truckWidthM", 2);
	truckHeightM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.truckHeightM", 2);
	cabLengthM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.cabLengthM", 1);
	cabWidthM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.cabWidthM", 2);
	cabHeightM = gcsScale*Config::getInstance()->getValueAsDouble("GCSDisplay.cabHeightM", 1.5);
	headingDecDeg = Config::getInstance()->getValueAsDouble("GCSDisplay.fixedHeadingDecDeg", 0);
}

void GCSDisplay::displayGCS()
{
	glCallList (displayList);
}

void GCSDisplay::generateCallList()
{
	displayList = glGenLists(1);
	// create the display list
	glNewList(displayList,GL_COMPILE);	
		

	//Enable lighting, set color, and display
	glEnable (GL_LIGHTING);
	glColor3f (truckColorRed, truckColorGreen, truckColorBlue);

	//Fixed heading offset from config file, not from network
	glRotatef (-headingDecDeg, 0, 0, 1);
	
	//truck body
	glBegin (GL_QUADS);
	{
		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (-1, 0, 0);
		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (-1, 0, 0);

		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (1, 0, 0);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (1, 0, 0);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (1, 0, 0);
		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (1, 0, 0);
		
		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 0, 1);

		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);

		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, -1, 0);
		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, -1, 0);
		glVertex3d (truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, -1, 0);
		glVertex3d (-truckWidthM/2, -truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, -1, 0);

		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 1, 0);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 1, 0);
		glVertex3d (truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 1, 0);
		glVertex3d (-truckWidthM/2, truckLengthM/2, wheelRadiusM*2+truckHeightM);
		glNormal3d (0, 1, 0);	
	}
	glEnd();

	//truck cab
	glBegin (GL_QUADS);
	{
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (-1, 0, 0);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (-1, 0, 0);
		
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (1, 0, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (1, 0, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (1, 0, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (1, 0, 0);
		
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 0, 1);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 0, 1);

		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 0, -1);

		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, -1, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, -1, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, -1, 0);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)-cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, -1, 0);

		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 1, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2);
		glNormal3d (0, 1, 0);
		glVertex3d (cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 1, 0);
		glVertex3d (-cabWidthM/2, (truckLengthM/2+cabLengthM/2)+cabLengthM/2, wheelRadiusM*2+cabHeightM);
		glNormal3d (0, 1, 0);		
	}
	glEnd();

	//Wheels
	glColor3f (wheelColorRed, wheelColorGreen, wheelColorBlue);
	
	glPushMatrix();
	glTranslated(-truckWidthM/2, -truckLengthM/2+wheelRadiusM, wheelRadiusM);
	glRotatef(90, 0, 1, 0);
	gluCylinder(quadricObj, wheelRadiusM, wheelRadiusM, wheelThicknessM, 30, 15);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glTranslated(0, 0, wheelThicknessM);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glPopMatrix();

	glPushMatrix();
	glTranslated(truckWidthM/2-wheelThicknessM, -truckLengthM/2+wheelRadiusM, wheelRadiusM);
	glRotatef(90, 0, 1, 0);
	gluCylinder(quadricObj, wheelRadiusM, wheelRadiusM, wheelThicknessM, 30, 15);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glTranslated(0, 0, wheelThicknessM);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glPopMatrix();

	glPushMatrix();
	glTranslated(-truckWidthM/2, truckLengthM/2-wheelRadiusM, wheelRadiusM);
	glRotatef(90, 0, 1, 0);
	gluCylinder(quadricObj, wheelRadiusM, wheelRadiusM, wheelThicknessM, 30, 15);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glTranslated(0, 0, wheelThicknessM);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glPopMatrix();

	glPushMatrix();
	glTranslated(truckWidthM/2-wheelThicknessM, truckLengthM/2-wheelRadiusM, wheelRadiusM);
	glRotatef(90, 0, 1, 0);
	gluCylinder(quadricObj, wheelRadiusM, wheelRadiusM, wheelThicknessM, 30, 15);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glTranslated(0, 0, wheelThicknessM);
	gluDisk (quadricObj, 0, wheelRadiusM, 30, 15);
	glPopMatrix();

	glDisable(GL_LIGHTING);


	glEndList();
}