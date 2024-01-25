#include "stdafx.h"
#include "MetBelief.h"
#include "stdio.h"
#include "Config.h"
#include "WinFonts.h"
#include "math.h"
#include "Constants.h"

GLUquadricObj *MetBelief::quadricObj = NULL; //!< GL Quadric object to use for displaying cloud location spheres
int MetBelief::displayList = -1; //!< GL display list to display cloud location spheres


MetBelief::MetBelief()
{
	m_MetDefined = false;
	readConfig();
}

MetBelief::MetBelief(double windBearingFromDeg, double windSpeedMps, double tempF)
{
	m_MetDefined = true;

	setWindBearingFromDeg (windBearingFromDeg);
	setWindSpeedMps (windSpeedMps);
	setTemperatureF (tempF);
	
	readConfig();
}

MetBelief::~MetBelief()
{

}

void MetBelief::readConfig()
{
	m_ArrowLengthPixels = Config::getInstance()->getValueAsInt ("MetBelief.ArrowLengthPixels", 15);
	m_ArrowBackRadiusPixels = Config::getInstance()->getValueAsInt ("MetBelief.ArrowBackRadiusPixels", 7);
	m_CenterPixelsFromRightEdge = Config::getInstance()->getValueAsInt ("MetBelief.CenterPixelsFromRightEdge", 75);
	m_CenterPixelsFromTopEdge = Config::getInstance()->getValueAsInt ("MetBelief.CenterPixelsFromTopEdge", 75);
	m_DirLineLengthPixels = Config::getInstance()->getValueAsInt ("MetBelief.DirLineLengthPixels", 50);
	m_CompassLineWidth = Config::getInstance()->getValueAsInt ("MetBelief.CompassLineWidth", 2);
	m_WindLineWidth = Config::getInstance()->getValueAsInt ("MetBelief.WindLineWidth", 4);
	m_TextFontSize = 1000*Config::getInstance()->getValueAsDouble ("MetBelief.TextFontSize", 12);
	m_TextHorizOffsetPix = Config::getInstance()->getValueAsInt ("MetBelief.TextHorizOffsetPix", 20);
	m_TextVertOffsetPix = Config::getInstance()->getValueAsInt ("MetBelief.TextVertOffsetPix", 20);

	m_CompassColorRed = Config::getInstance()->getValueAsDouble ("MetBelief.CompassColorRed", .8);
	m_CompassColorGreen = Config::getInstance()->getValueAsDouble ("MetBelief.CompassColorGreen", .7);
	m_CompassColorBlue = Config::getInstance()->getValueAsDouble ("MetBelief.CompassColorBlue", .8);
	m_CompassColorAlpha = Config::getInstance()->getValueAsDouble ("MetBelief.CompassColorAlpha", .8);
	m_WindColorRed = Config::getInstance()->getValueAsDouble ("MetBelief.WindColorRed", 0.2);
	m_WindColorGreen = Config::getInstance()->getValueAsDouble ("MetBelief.WindColorGreen", 0.2);
	m_WindColorBlue = Config::getInstance()->getValueAsDouble ("MetBelief.WindColorBlue", 1.0);
	m_WindColorAlpha = Config::getInstance()->getValueAsDouble ("MetBelief.WindColorAlpha", 0.9);

	m_ShadedBoxColorRed = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxColorRed", 0.3);
	m_ShadedBoxColorGreen = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxColorGreen", .3);
	m_ShadedBoxColorBlue = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxColorBlue", .3);
	m_ShadedBoxColorAlpha = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxColorAlpha", .5);
	m_ShadedBoxHeightPixels = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxHeightPixels", 110);
	m_ShadedBoxWidthPixels = Config::getInstance()->getValueAsDouble ("MetBelief.ShadedBoxWidthPixels", 110);
}

void MetBelief::initQuadric()
{
	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	displayList = -1;
}

void MetBelief::destroyGlObjects ()
{
	if (displayList >= 0)
	{
		gluDeleteQuadric(quadricObj);
		displayList = -1;
	}
}

void MetBelief::setCameraAngles (double pitchDegrees, double headingDegrees)
{
	m_GlPitchDegrees = pitchDegrees;
	m_GlHeadingDegrees = headingDegrees;
}

void MetBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	
}

void MetBelief::drawGlOverlay (int glWidth, int glHeight)
{
	if (displayList < 0)
	{
		initQuadric();

		//Create display call list first time with arrow
		displayList = glGenLists(1);
		glNewList(displayList,GL_COMPILE);	
		glEnable (GL_LIGHTING);
		gluCylinder (quadricObj, m_ArrowBackRadiusPixels, 0, m_ArrowLengthPixels, 30, 30);
		gluDisk (quadricObj, 0, m_ArrowBackRadiusPixels, 30, 30);
		glDisable (GL_LIGHTING);
		glEndList();
	}

	//draw shaded box
	glColor4f (m_ShadedBoxColorRed, m_ShadedBoxColorGreen, m_ShadedBoxColorBlue, m_ShadedBoxColorAlpha);
	glBegin (GL_QUADS);
	glVertex3d (glWidth-m_ShadedBoxWidthPixels, glHeight-m_ShadedBoxHeightPixels, -glWidth+1);
	glVertex3d (glWidth, glHeight-m_ShadedBoxHeightPixels, -glWidth+1);
	glVertex3d (glWidth, glHeight, -glWidth+1);
	glVertex3d (glWidth-m_ShadedBoxWidthPixels, glHeight, -glWidth+1);
	glEnd();


	
	glTranslated (glWidth-m_CenterPixelsFromRightEdge, glHeight-m_CenterPixelsFromTopEdge, 0);
	glPushMatrix();
	glRotatef (m_GlPitchDegrees, 1.0, 0, 0.0);
	glRotatef (m_GlHeadingDegrees, 0, 0.0, 1.0);
	
	glColor4f (m_CompassColorRed, m_CompassColorGreen, m_CompassColorBlue, m_CompassColorAlpha);
	glLineWidth (m_CompassLineWidth);
	glBegin (GL_LINES);
	glVertex2d (0, 0);
	glVertex2d (0, m_DirLineLengthPixels);
	glVertex2d (0, 0);
	glVertex2d (m_DirLineLengthPixels, 0);
	glVertex2d (0, 0);
	glVertex2d (0, -m_DirLineLengthPixels);
	glVertex2d (0, 0);
	glVertex2d (-m_DirLineLengthPixels, 0);
	glEnd();

	glPushMatrix ();
	glRotated (90, -1, 0, 0);
	glTranslated (0, 0, m_DirLineLengthPixels-m_ArrowLengthPixels);
	glCallList (displayList);
	glPopMatrix();

	if (m_MetDefined)
	{
		glColor4f (m_WindColorRed, m_WindColorGreen, m_WindColorBlue, m_WindColorAlpha);
		glLineWidth (m_WindLineWidth);
		double x = m_DirLineLengthPixels * sin(DEG2RAD*m_WindBearingFromDeg);
		double y = m_DirLineLengthPixels * cos(DEG2RAD*m_WindBearingFromDeg);
		glBegin (GL_LINES);
		glVertex2d (0, 0);
		glVertex2d (x, y);
		glEnd ();

		glPushMatrix ();
		glRotated (90, 1, 0, 0);
		glRotated (m_WindBearingFromDeg ,0, -1, 0);
		glTranslated (0, 0, -m_ArrowLengthPixels/2);
		glCallList (displayList);
		glPopMatrix();

		glPopMatrix();
		char msg[128];
		sprintf_s (msg, 128, "%.1f m/s FROM %d deg\0", m_WindSpeedMps, (int)m_WindBearingFromDeg);
		double factor = cos(m_GlPitchDegrees*DEG2RAD);
		WinFonts::glPrint2d (m_TextFontSize, -m_DirLineLengthPixels-m_TextHorizOffsetPix, -factor*(m_DirLineLengthPixels)-m_TextVertOffsetPix, msg);
	}
	else
	{
		glPopMatrix();
	}
}
		
void MetBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %.1f deg\0", m_WindBearingFromDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Wind From Bearing \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m/s\0", m_WindSpeedMps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Wind Speed \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f deg-F\0", m_TemperatureF);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Temperature \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}

