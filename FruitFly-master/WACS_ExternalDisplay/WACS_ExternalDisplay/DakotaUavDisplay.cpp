
#include "stdafx.h"
#include "DakotaUavDisplay.h"
#include "Config.h"
#include <math.h>



DakotaUavDisplay::DakotaUavDisplay() : UavDisplayBase ()
{
	init ();
}

DakotaUavDisplay::~DakotaUavDisplay()
{
	
}

void DakotaUavDisplay::readConfig()
{
	planeScale = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.planeScale", 100);
	uavColorRed = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.uavColorRed", 0.95);
	uavColorBlue = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.uavColorBlue", 0.95);
	uavColorGreen = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.uavColorGreen", 0.95);
	podColorRed = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podColorRed", 0.6);
	podColorBlue = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podColorBlue", 0.6);
	podColorGreen = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podColorGreen", 0.6);
	fuselageFrontLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageFrontLength", .75);
	fuselageFrontWidth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageFrontWidth", .25);
	fuselageFrontHeight = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageFrontHeight", .25);
	fuselageCenterLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageCenterLength", .25);
	fuselageCenterWidth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageCenterWidth", .5);
	fuselageCenterHeight = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageCenterHeight", .6);
	fuselageRearLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageRearLength", 2);
	fuselageRearWidth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageRearWidth", .2);
	fuselageRearHeight = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.fuselageRearHeight", .2);
	wingSpan = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.wingSpan", 4.3);//m
	wingDepth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.wingDepth", .3);//m
	wingThickness = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.wingThickness", 0.1);//m
	wingRearFromCenter = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.wingRearFromCenter", 0.0);//m
	wingUpFromCenter = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.wingUpFromCenter", 0.15);//m
	podDiameter = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podDiameter", 0.1);//m
	podLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podLength", .6);//m
	podOutFromCenter = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podOutFromCenter", 1);//m
	podRearOfWingCenter = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.podRearOfWingCenter", 0.05);//m
	propLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.propLength", 0.3);//m
	propThickness = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.propThickness", 0.02);//m
	propDiskAlpha = Config::getInstance()->getValueAsDouble("DakotaUavDisplay.propDiskAlpha", 0.5);
	tailBaseLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.tailBaseLength", 0.7);
	tailTopLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.tailTopLength", 0.4);
	tailWidth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.tailWidth", 0.1);
	tailHeight = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.tailHeight", 0.5);
	elevatorWidth = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.elevatorWidth", 1);
	elevatorHeight = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.elevatorHeight", 0.08);
	elevatorLength = planeScale*Config::getInstance()->getValueAsDouble("DakotaUavDisplay.elevatorLength", 0.25);

	fuselageFrontSideAngleRad = atan ((fuselageCenterWidth-fuselageFrontWidth)/fuselageFrontLength/2);
	fuselageFrontSideAngleSin = sin(fuselageFrontSideAngleRad);
	fuselageFrontSideAngleCos = cos(fuselageFrontSideAngleRad);
	fuselageFrontTopAngleRad = atan ((fuselageCenterHeight-fuselageFrontHeight)/fuselageFrontLength/2);
	fuselageFrontTopAngleSin = sin(fuselageFrontTopAngleRad);
	fuselageFrontTopAngleCos = cos(fuselageFrontTopAngleRad);
	fuselageRearSideAngleRad = atan ((fuselageCenterWidth-fuselageRearWidth)/fuselageRearLength/2);
	fuselageRearSideAngleSin = sin(fuselageRearSideAngleRad);
	fuselageRearSideAngleCos = cos(fuselageRearSideAngleRad);
	fuselageRearTopAngleRad = atan ((fuselageCenterHeight-fuselageRearHeight)/fuselageRearLength/2);
	fuselageRearTopAngleSin = sin(fuselageRearTopAngleRad);
	fuselageRearTopAngleCos = cos(fuselageRearTopAngleRad);

}

void DakotaUavDisplay::generateCallList()
{
	displayList = glGenLists(1);
	// create the display list
	glNewList(displayList,GL_COMPILE);	
		

	//Enable lighting, set color, and display
	glEnable (GL_LIGHTING);
	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);

	//fuselage front section
	glBegin (GL_QUADS);
	{
		glNormal3d (-fuselageFrontSideAngleCos, fuselageFrontSideAngleSin, 0);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		
		glNormal3d (0, fuselageFrontTopAngleSin, fuselageFrontTopAngleCos);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		
		glNormal3d (fuselageFrontSideAngleCos, fuselageFrontSideAngleSin, 0);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		
		glNormal3d (0, fuselageFrontTopAngleSin, -fuselageFrontTopAngleCos);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
	}
	glEnd();

	//fuselage center section
	glBegin (GL_QUADS);
	{
		glNormal3d (-1, 0, 0);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		
		glNormal3d (0, 0, 1);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		
		glNormal3d (1, 0, 0);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		
		glNormal3d (0, 0, -1);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
	}
	glEnd();

	//fuselage rear section
	glBegin (GL_QUADS);
	{
		glNormal3d (-fuselageRearSideAngleCos, -fuselageRearSideAngleSin, 0);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		
		glNormal3d (0, -fuselageRearTopAngleSin, fuselageRearTopAngleCos);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		
		glNormal3d (fuselageRearSideAngleCos, -fuselageRearSideAngleSin, 0);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, fuselageCenterHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
		
		glNormal3d (0, -fuselageRearTopAngleSin, -fuselageRearTopAngleCos);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
		glVertex3d (fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageCenterWidth/2, -fuselageCenterLength/2, -fuselageCenterHeight/2);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
	}
	glEnd();

	//rear end fuselage
	glBegin (GL_QUADS);
	{
		glNormal3d (0, -1, 0);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
		glVertex3d (-fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
		glVertex3d (fuselageRearWidth/2, -fuselageCenterLength/2-fuselageRearLength, -fuselageRearHeight/2);
		
	}
	glEnd();
	
	//front end fuselage
	glBegin (GL_QUADS);
	{
		glNormal3d (0, 1, 0);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		glVertex3d (-fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, fuselageFrontHeight/2);
		glVertex3d (fuselageFrontWidth/2, fuselageCenterLength/2+fuselageFrontLength, -fuselageFrontHeight/2);
		
	}
	glEnd();
	

	//wings
	glPushMatrix();
	glTranslated (0, -wingRearFromCenter, wingUpFromCenter);
	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);
	glBegin(GL_QUADS);
	{
		glNormal3d (0, -1, 0);
		glVertex3d (-wingSpan/2, -wingDepth, 0);
		glVertex3d (wingSpan/2, -wingDepth, 0);
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		
		glNormal3d (0, 0, 1);
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glVertex3d (wingSpan/2, 0, wingThickness);
		glVertex3d (-wingSpan/2, 0, wingThickness);
		
		glNormal3d (0, 1, 0);
		glVertex3d (-wingSpan/2, 0, wingThickness);
		glVertex3d (wingSpan/2, 0, wingThickness);
		glVertex3d (wingSpan/2, 0, 0);
		glVertex3d (-wingSpan/2, 0, 0);
		
		glNormal3d (0, 0, -1);
		glVertex3d (-wingSpan/2, 0, 0);
		glVertex3d (wingSpan/2, 0, 0);
		glVertex3d (wingSpan/2, -wingDepth, 0);
		glVertex3d (-wingSpan/2, -wingDepth, 0);
	}
	glEnd();
	
	//wing ends
	glBegin(GL_QUADS);
	{
		glNormal3d (-1, 0, 0);
		glVertex3d (-wingSpan/2, -wingDepth, 0);
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		glVertex3d (-wingSpan/2, 0, wingThickness);
		glVertex3d (-wingSpan/2, 0, 0);
		
		glNormal3d (1, 0, 0);
		glVertex3d (wingSpan/2, -wingDepth, 0);
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glVertex3d (wingSpan/2, 0, wingThickness);
		glVertex3d (wingSpan/2, 0, 0);
		
	}
	glEnd();
	glPopMatrix();

	//Tail 
	glPushMatrix();
	glTranslated (0, -fuselageCenterLength/2-fuselageRearLength, fuselageRearHeight/2);
	glBegin (GL_QUADS);
	{
		glNormal3d (-1, 0, 0);
		glVertex3d (-tailWidth/2, 0, 0);
		glVertex3d (-tailWidth/2, tailBaseLength, 0);
		glVertex3d (-tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (-tailWidth/2, 0, tailHeight);
		
		glNormal3d (0, 0, 1);
		glVertex3d (-tailWidth/2, 0, tailHeight);
		glVertex3d (-tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (tailWidth/2, 0, tailHeight);
		
		glNormal3d (1, 0, 0);
		glVertex3d (tailWidth/2, 0, tailHeight);
		glVertex3d (tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (tailWidth/2, tailBaseLength, 0);
		glVertex3d (tailWidth/2, 0, 0);
		
		glNormal3d (0, 0, -1);
		glVertex3d (tailWidth/2, 0, 0);
		glVertex3d (tailWidth/2, tailBaseLength, 0);
		glVertex3d (-tailWidth/2, tailBaseLength, 0);
		glVertex3d (-tailWidth/2, 0, 0);
	}
	glEnd();

	//Tail Front Side
	glBegin (GL_QUADS);
	{
		glNormal3d (0, 1, 0);
		glVertex3d (-tailWidth/2, tailBaseLength, 0);
		glVertex3d (-tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (tailWidth/2, tailTopLength, tailHeight);
		glVertex3d (tailWidth/2, tailBaseLength, 0);
	}
	glEnd();

	//Tail Back Side
	glBegin (GL_QUADS);
	{
		glNormal3d (0, -1, 0);
		glVertex3d (-tailWidth/2, 0, 0);
		glVertex3d (-tailWidth/2, 0, tailHeight);
		glVertex3d (tailWidth/2, 0, tailHeight);
		glVertex3d (tailWidth/2, 0, 0);
	}
	glEnd();
	glPopMatrix();

	//Elevator
	glPushMatrix();
	glTranslated (0, -fuselageCenterLength/2-fuselageRearLength+elevatorLength, fuselageRearHeight/2);
	glBegin (GL_QUADS);
	{
		glNormal3d (-1, 0, 0);
		glVertex3d (-elevatorWidth/2, -elevatorLength, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, 0, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, 0, 0);
		glVertex3d (-elevatorWidth/2, -elevatorLength, 0);
		
		glNormal3d (0, 0, 1);
		glVertex3d (-elevatorWidth/2, -elevatorLength, 0);
		glVertex3d (-elevatorWidth/2, 0, 0);
		glVertex3d (elevatorWidth/2, 0, 0);
		glVertex3d (elevatorWidth/2, -elevatorLength, 0);
		
		glNormal3d (1, 0, 0);
		glVertex3d (elevatorWidth/2, -elevatorLength, 0);
		glVertex3d (elevatorWidth/2, 0, 0);
		glVertex3d (elevatorWidth/2, 0, -elevatorHeight);
		glVertex3d (elevatorWidth/2, -elevatorLength, -elevatorHeight);
		
		glNormal3d (0, 0, -1);
		glVertex3d (elevatorWidth/2, -elevatorLength, -elevatorHeight);
		glVertex3d (elevatorWidth/2, 0, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, 0, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, -elevatorLength, -elevatorHeight);
	}
	glEnd();
	
	//Elevator Front Side
	glBegin (GL_QUADS);
	{
		glNormal3d (0, 1, 0);
		glVertex3d (-elevatorWidth/2, 0, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, 0, 0);
		glVertex3d (elevatorWidth/2, 0, 0);
		glVertex3d (elevatorWidth/2, 0, -elevatorHeight);
	}
	glEnd();

	//Elevator Back Side
	glBegin (GL_QUADS);
	{
		glNormal3d (0, -1, 0);
		glVertex3d (-elevatorWidth/2, -elevatorLength, -elevatorHeight);
		glVertex3d (-elevatorWidth/2, -elevatorLength, 0);
		glVertex3d (elevatorWidth/2, -elevatorLength, 0);
		glVertex3d (elevatorWidth/2, -elevatorLength, -elevatorHeight);
	}
	glEnd();
	glPopMatrix();

	
	glColor3f (podColorRed, podColorGreen, podColorBlue);
	//Pods
	glPushMatrix();
	glTranslated(podOutFromCenter, -wingRearFromCenter-wingDepth/2+podLength/2-podRearOfWingCenter, wingUpFromCenter-podDiameter/2);
	glPushMatrix();
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, podDiameter/2, podDiameter/2, podLength, 30, 15);
	glPopMatrix();			
	gluSphere(quadricObj, podDiameter/2, 30, 15);
	glTranslated(0, -podLength, 0);
	gluSphere(quadricObj, podDiameter/2, 30, 15);
	glPopMatrix();

	glPushMatrix();
	glTranslated(-podOutFromCenter, -wingRearFromCenter-wingDepth/2+podLength/2-podRearOfWingCenter, wingUpFromCenter-podDiameter/2);
	glPushMatrix();
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, podDiameter/2, podDiameter/2, podLength, 30, 15);
	glPopMatrix();			
	gluSphere(quadricObj, podDiameter/2, 30, 15);
	glTranslated(0, -podLength, 0);
	gluSphere(quadricObj, podDiameter/2, 30, 15);
	glPopMatrix();

	glColor4f (podColorRed, podColorRed, podColorRed, propDiskAlpha);
	//Prop disk
	glPushMatrix();
	glTranslated(0, fuselageCenterLength/2+fuselageFrontLength+propThickness, 0);
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, propLength, propLength, propThickness, 30, 15);
	gluDisk (quadricObj, 0, propLength, 30, 15);
	glTranslated(0, 0, propThickness);
	glRotatef(180, 0, 0, 1);
	gluDisk (quadricObj, 0, propLength, 30, 15);
	glPopMatrix();

	glDisable(GL_LIGHTING);
	glEndList();
}