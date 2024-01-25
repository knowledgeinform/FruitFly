
#include "stdafx.h"
#include "ShadowUavDisplay.h"
#include "Config.h"
#include <math.h>



ShadowUavDisplay::ShadowUavDisplay() : UavDisplayBase ()
{
	init ();
}

ShadowUavDisplay::~ShadowUavDisplay()
{
	
}

void ShadowUavDisplay::readConfig()
{
	planeScale = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.planeScale", 100);
	uavColorRed = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.uavColorRed", 0.6);
	uavColorBlue = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.uavColorBlue", 0.6);
	uavColorGreen = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.uavColorGreen", 0.6);
	highlightColorRed = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.highlightColorRed", 1.0);
	highlightColorBlue = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.highlightColorBlue", 0.01);
	highlightColorGreen = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.highlightColorGreen", 0.38);
	engineColorRed = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.engineColorRed", 0.0);
	engineColorBlue = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.engineColorBlue", 0.0);
	engineColorGreen = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.engineColorGreen", 0.0);
	fuselageLength = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.fuselageLength", 1.5);//m
	fuselageWidth = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.fuselageWidth", .3);//m
	fuselageHeight = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.fuselageHeight", .3);//m
	fuselageFrontLength = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.fuselageFrontLength", 0.2);//m
	fuselageFrontAngleRad = atan (fuselageFrontLength/(fuselageHeight/2));
	wingSpan = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.wingSpan", 4.3);//m
	wingTipHighlightColorLength = planeScale*Config::getInstance()->getValueAsDouble ("ShadowUavDisplay.wingTipHighlightLength", 1); //m
	wingTipHighlightColorLength = max (0, wingTipHighlightColorLength);
	highlightTail = Config::getInstance()->getValueAsBool ("ShadowUavDisplay.highlightTail", true);
	highlightNose = Config::getInstance()->getValueAsBool ("ShadowUavDisplay.highlightNose", true);
	wingDepth = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.wingDepth", .3);//m
	wingThickness = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.wingThickness", 0.1);//m
	wingRearFromCenter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.wingRearFromCenter", 0.3);//m
	wingUpFromCenter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.wingUpFromCenter", 0.1);//m
	tailRearFromWingRear = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailRearFromWingRear", 1.5);//m
	tailRodDiameter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailRodDiameter", 0.05);//m
	tailWidth = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailWidth", .8);//m
	tailThickness = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailThickness", 0.05);//m
	tailHeight = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailHeight", 0.2);//m
	tailDepth = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.tailDepth", 0.2);//m
	podDiameter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.podDiameter", 0.1);//m
	podLength = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.podLength", .6);//m
	podOutFromCenter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.podOutFromCenter", 1);//m
	podRearOfWingCenter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.podRearOfWingCenter", 0.05);//m
	propDiskAlpha = Config::getInstance()->getValueAsDouble("ShadowUavDisplay.propDiskAlpha", 0.5);
	engineDiameter = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.engineDiameter", 0.3);//m
	engineDepth = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.engineDepth", 0.2);//m
	propLength = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.propLength", 0.3);//m
	propThickness = planeScale*Config::getInstance()->getValueAsDouble("ShadowUavDisplay.propThickness", 0.02);//m
}

void ShadowUavDisplay::generateCallList()
{
	displayList = glGenLists(1);
	// create the display list
	glNewList(displayList,GL_COMPILE);	
		

	//Enable lighting, set color, and display
	glEnable (GL_LIGHTING);
	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);

	//fuselage
	glBegin (GL_QUADS);
	{
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (-1, 0, 0);
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (-1, 0, 0);
		
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, 0, 1);
		glVertex3d (-fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, 0, 1);
		glVertex3d (fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, 0, 1);
		glVertex3d (fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, 0, 1);
		
		glVertex3d (fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (1, 0, 0);
		glVertex3d (fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (1, 0, 0);
		glVertex3d (fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (1, 0, 0);
		glVertex3d (fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (1, 0, 0);
		
		glVertex3d (fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, 0, -1);
		glVertex3d (fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, 0, -1);
		glVertex3d (-fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, 0, -1);
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, 0, -1);
		
	}
	glEnd();
	//rear end fuselage
	glBegin (GL_QUADS);
	{
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, -1, 0);
		glVertex3d (-fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, -1, 0);
		glVertex3d (fuselageWidth/2, -fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, -1, 0);
		glVertex3d (fuselageWidth/2, -fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, -1, 0);
		
	}
	glEnd();
	//front end fuselage
	if (highlightNose)
		glColor3f (highlightColorRed, highlightColorGreen, highlightColorBlue);
	glBegin (GL_TRIANGLES);
	{
		glVertex3d (-fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, cos(fuselageFrontAngleRad), sin(fuselageFrontAngleRad));
		glVertex3d (fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (0, cos(fuselageFrontAngleRad), sin(fuselageFrontAngleRad));
		glVertex3d (0, fuselageLength/2 + fuselageFrontLength, 0);
		glNormal3d (0, cos(fuselageFrontAngleRad), sin(fuselageFrontAngleRad));

		glVertex3d (-fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, cos(fuselageFrontAngleRad), -sin(fuselageFrontAngleRad));
		glVertex3d (fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (0, cos(fuselageFrontAngleRad), -sin(fuselageFrontAngleRad));
		glVertex3d (0, fuselageLength/2 + fuselageFrontLength, 0);
		glNormal3d (0, cos(fuselageFrontAngleRad), -sin(fuselageFrontAngleRad));
		
		
		glVertex3d (fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
		glVertex3d (fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
		glVertex3d (0, fuselageLength/2 + fuselageFrontLength, 0);
		glNormal3d (sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
		
		glVertex3d (-fuselageWidth/2, fuselageLength/2, fuselageHeight/2);
		glNormal3d (-sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
		glVertex3d (-fuselageWidth/2, fuselageLength/2, -fuselageHeight/2);
		glNormal3d (-sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
		glVertex3d (0, fuselageLength/2 + fuselageFrontLength, 0);
		glNormal3d (-sin(fuselageFrontAngleRad), cos(fuselageFrontAngleRad), 0);
	}
	glEnd();

	//wings
	glPushMatrix();
	glTranslated (0, -wingRearFromCenter, wingUpFromCenter);
	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);
	//wings (inside, unhighlighted)
	glBegin(GL_QUADS);
	{
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 0, 1);
		
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 1, 0);
		
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, 0, -1);
	}
	glEnd();
	glColor3f (highlightColorRed, highlightColorGreen, highlightColorBlue);
	//wings (outside, highlighted)
	glBegin(GL_QUADS);
	{
		glVertex3d (-wingSpan/2, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (-wingSpan/2, 0, wingThickness);
		glNormal3d (0, 0, 1);
		
		glVertex3d (-wingSpan/2, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 1, 0);
		glVertex3d (-wingSpan/2, 0, 0);
		glNormal3d (0, 1, 0);
		
		glVertex3d (-wingSpan/2, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (-wingSpan/2 + wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (-wingSpan/2, -wingDepth, 0);
		glNormal3d (0, 0, -1);


		glVertex3d (wingSpan/2, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, -1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (0, -1, 0);
		
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 0, 1);
		glVertex3d (wingSpan/2, 0, wingThickness);
		glNormal3d (0, 0, 1);
		
		glVertex3d (wingSpan/2, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, wingThickness);
		glNormal3d (0, 1, 0);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 1, 0);
		glVertex3d (wingSpan/2, 0, 0);
		glNormal3d (0, 1, 0);
		
		glVertex3d (wingSpan/2, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, 0, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (wingSpan/2 - wingTipHighlightColorLength, -wingDepth, 0);
		glNormal3d (0, 0, -1);
		glVertex3d (wingSpan/2, -wingDepth, 0);
		glNormal3d (0, 0, -1);
	}
	glEnd();
	//wing ends
	if (wingTipHighlightColorLength < 0.0000001)
		glColor3f (uavColorRed, uavColorGreen, uavColorBlue);
	glBegin(GL_QUADS);
	{
		glVertex3d (-wingSpan/2, -wingDepth, 0);
		glNormal3d (-1, 0, 0);
		glVertex3d (-wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (-1, 0, 0);
		glVertex3d (-wingSpan/2, 0, wingThickness);
		glNormal3d (-1, 0, 0);
		glVertex3d (-wingSpan/2, 0, 0);
		glNormal3d (-1, 0, 0);
		
		glVertex3d (wingSpan/2, -wingDepth, 0);
		glNormal3d (1, 0, 0);
		glVertex3d (wingSpan/2, -wingDepth, wingThickness);
		glNormal3d (1, 0, 0);
		glVertex3d (wingSpan/2, 0, wingThickness);
		glNormal3d (1, 0, 0);
		glVertex3d (wingSpan/2, 0, 0);
		glNormal3d (1, 0, 0);
		
	}
	glEnd();
	glPopMatrix();

	
	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);
	//Tail rods
	glPushMatrix();
	glTranslated(tailWidth/2, -wingRearFromCenter-wingDepth, wingUpFromCenter+wingThickness/2);
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, tailRodDiameter/2, tailRodDiameter/2, tailRearFromWingRear, 30, 15);
	glPopMatrix();
	glPushMatrix();
	glTranslated(-tailWidth/2, -wingRearFromCenter-wingDepth, wingUpFromCenter+wingThickness/2);
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, tailRodDiameter/2, tailRodDiameter/2, tailRearFromWingRear, 30, 15);
	glPopMatrix();

	//Tail
	if (highlightTail)
		glColor3f (highlightColorRed, highlightColorGreen, highlightColorBlue);
	glPushMatrix();
	glTranslated(0, -wingRearFromCenter-wingDepth-tailRearFromWingRear, wingUpFromCenter+wingThickness/2);
	glBegin (GL_QUADS);
	{
		glVertex3d (0, 0, tailHeight+tailThickness/2);
		glVertex3d (tailWidth/2, 0, -tailThickness/2);
		glVertex3d (tailWidth/2, tailDepth, -tailThickness/2);
		glVertex3d (0, tailDepth, tailHeight+tailThickness/2);
		
		glVertex3d (0, 0, tailHeight+tailThickness/2);
		glVertex3d (-tailWidth/2, 0, -tailThickness/2);
		glVertex3d (-tailWidth/2, tailDepth, -tailThickness/2);
		glVertex3d (0, tailDepth, tailHeight+tailThickness/2);
		
	}
	glEnd();
	glPopMatrix();

	glColor3f (uavColorRed, uavColorGreen, uavColorBlue);
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



	glColor3f (engineColorRed, engineColorGreen, engineColorBlue);
	//Engine
	glPushMatrix();
	glTranslated(0, -fuselageLength/2, 0);
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, engineDiameter/2, engineDiameter/2, engineDepth, 30, 15);
	gluDisk (quadricObj, 0, engineDiameter/2, 30, 15);
	glTranslated(0, 0, engineDepth);
	gluDisk (quadricObj, 0, engineDiameter/2, 30, 15);
	glPopMatrix();

	glColor4f (uavColorRed, uavColorGreen, uavColorBlue, propDiskAlpha);
	//Prop disk
	glPushMatrix();
	glTranslated(0, -fuselageLength/2 - engineDepth, 0);
	glRotatef(90, 1, 0, 0);
	gluCylinder(quadricObj, propLength, propLength, propThickness, 30, 15);
	gluDisk (quadricObj, 0, propLength, 30, 15);
	glTranslated(0, 0, propThickness);
	gluDisk (quadricObj, 0, propLength, 30, 15);
	glPopMatrix();


	glDisable(GL_LIGHTING);


	glEndList();
}