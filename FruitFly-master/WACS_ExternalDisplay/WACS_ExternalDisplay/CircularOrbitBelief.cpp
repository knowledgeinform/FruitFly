#include "stdafx.h"
#include "CircularOrbitBelief.h"
#include "Config.h"
#include <cmath>

int CircularOrbitBelief::m_ArrowAngleDeg = 0;
long CircularOrbitBelief::m_ArrowStartTimeSec = time(NULL);
	

CircularOrbitBelief::CircularOrbitBelief ()
{
	setCircularOrbit (0, 0, 1, 0, true);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

CircularOrbitBelief::CircularOrbitBelief (double centerLatDecDeg, double centerLonDecDeg, double radiusM, double altMslM, bool isClockwise)
{
	setCircularOrbit (centerLatDecDeg, centerLonDecDeg, radiusM, altMslM, isClockwise);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

CircularOrbitBelief::~CircularOrbitBelief ()
{
	gluDeleteQuadric(quadricObj);
}

void CircularOrbitBelief::readConfig()
{
	m_ArrowCount = Config::getInstance()->getValueAsInt ("CircularOrbitBelief.ArrowCount", 3);
	m_ArrowOrbitPeriodSec = Config::getInstance()->getValueAsInt ("CircularOrbitBelief.ArrowOrbitSec", 20);
	m_ArrowBackRadiusM = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowBackRadiusM", 20);
	m_ArrowLengthM = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowLengthM", 40);
	m_ArrowColorRed = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowColorRed", 0);
	m_ArrowColorGreen = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowColorGreen", 1);
	m_ArrowColorBlue = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowColorBlue", 0);
	m_ArrowColorAlpha = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.ArrowColorAlpha", 0.5);

	m_OrbitColorRed = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.OrbitColorRed", 0);
	m_OrbitColorGreen = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.OrbitColorGreen", 1);
	m_OrbitColorBlue = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.OrbitColorBlue", 0);
	m_OrbitLineWidth = Config::getInstance()->getValueAsDouble ("CircularOrbitBelief.OrbitLineWidth", 2);
}

bool CircularOrbitBelief::setCircularOrbit (double centerLatDecDeg, double centerLonDecDeg, double radiusM, double altMslM, bool isClockwise)
{
	setCenterLatDecDeg (centerLatDecDeg);
	setCenterLonDecDeg (centerLonDecDeg);
	setRadiusM (radiusM);
	setAltMslM (altMslM);
	setIsClockwise (isClockwise);

	//Convert meters to lat/lon distances for circular orbit display in lat/lon referenced GL scene
	m_RadiusLat = radiusM / (2*3.14159265358979323*6378137/360.0);
	m_RadiusLon = radiusM / (2*3.14159265358979323*6378137/360.0 * cos(centerLatDecDeg*3.14159265358979323/180));
		
	return true;
}

void CircularOrbitBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition == NULL)
		return;

	double offsetX = centerPointPosition->getLonDecDeg();
	double offsetY = centerPointPosition->getLatDecDeg();
	double offsetZ = centerPointPosition->getAltMslM();

	glColor3f (m_OrbitColorRed, m_OrbitColorGreen, m_OrbitColorBlue);
	glLineWidth (m_OrbitLineWidth);
	glBegin (GL_LINES);

	//Draw lines connecting the full orbit circle, one degree increments.
	for (int i = 1; i <= 90; i ++)
	{
		double iRad = i*3.14159265358979323/180;
		double iRadLast = (i-1)*3.14159265358979323/180;

		glVertex3d (estLonToMConv*(m_CenterLonDecDeg-m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_CenterLatDecDeg+m_RadiusLat*sin(iRad) - offsetY), m_AltMslM - offsetZ);
		glVertex3d (estLonToMConv*(m_CenterLonDecDeg-m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_CenterLatDecDeg+m_RadiusLat*sin(iRadLast) - offsetY), m_AltMslM - offsetZ);

		glVertex3d (estLonToMConv*(m_CenterLonDecDeg-m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_CenterLatDecDeg-m_RadiusLat*sin(iRad) - offsetY), m_AltMslM - offsetZ);
		glVertex3d (estLonToMConv*(m_CenterLonDecDeg-m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_CenterLatDecDeg-m_RadiusLat*sin(iRadLast) - offsetY), m_AltMslM - offsetZ);

		glVertex3d (estLonToMConv*(m_CenterLonDecDeg+m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_CenterLatDecDeg+m_RadiusLat*sin(iRad) - offsetY), m_AltMslM - offsetZ);
		glVertex3d (estLonToMConv*(m_CenterLonDecDeg+m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_CenterLatDecDeg+m_RadiusLat*sin(iRadLast) - offsetY), m_AltMslM - offsetZ);

		glVertex3d (estLonToMConv*(m_CenterLonDecDeg+m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_CenterLatDecDeg-m_RadiusLat*sin(iRad) - offsetY), m_AltMslM - offsetZ);
		glVertex3d (estLonToMConv*(m_CenterLonDecDeg+m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_CenterLatDecDeg-m_RadiusLat*sin(iRadLast) - offsetY), m_AltMslM - offsetZ);

	}
	glEnd();


	
	if (displayList < 0)
	{
		//Create display call list first time with spinning arrow
		displayList = glGenLists(1);
		glNewList(displayList,GL_COMPILE);	
		glColor4f (m_ArrowColorRed, m_ArrowColorGreen, m_ArrowColorBlue, m_ArrowColorAlpha);
		glEnable (GL_LIGHTING);
		gluCylinder (quadricObj, m_ArrowBackRadiusM, 0, m_ArrowLengthM, 30, 30);
		gluDisk (quadricObj, 0, m_ArrowBackRadiusM, 30, 30);
		glDisable (GL_LIGHTING);
		glEndList();
	}

	//Calculate rotation around orbit based on elapsed time and desired period
	double periodFrac = ((time(NULL)-m_ArrowStartTimeSec)%m_ArrowOrbitPeriodSec)/(double)m_ArrowOrbitPeriodSec;
	m_ArrowAngleDeg = 360 * periodFrac;
	if (m_IsClockwise)
		m_ArrowAngleDeg = 360 - m_ArrowAngleDeg;

	//Draw spinning arrows
	for (int i = 0; i < m_ArrowCount; i ++)
	{
		double currAngleDeg = m_ArrowAngleDeg + i/(double)m_ArrowCount*360;
		
		glPushMatrix();
		glTranslated (estLonToMConv*(m_CenterLonDecDeg - offsetX), estLatToMConv*(m_CenterLatDecDeg - offsetY), m_AltMslM - offsetZ);
		glRotated (currAngleDeg, 0, 0, 1);
		glTranslated (0, m_RadiusM, 0);
		if (!m_IsClockwise)
			glRotated (-90, 0, 1, 0);
		else
			glRotated (90, 0, 1, 0);
		glCallList (displayList);
		glPopMatrix();
	}

}


void CircularOrbitBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		//Set labels and values for parameters
		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_CenterLatDecDeg), (m_CenterLatDecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Center Latitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_CenterLonDecDeg), (m_CenterLonDecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Center Longitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_RadiusM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Orbit Radius: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_AltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Orbit Altitude MSL: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %s", m_IsClockwise?"CLOCKWISE":"COUNTER-CLOCKWISE");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Orbit Direction ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		//Clear remaining data rows
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "");
		}
	}

	return;
}

