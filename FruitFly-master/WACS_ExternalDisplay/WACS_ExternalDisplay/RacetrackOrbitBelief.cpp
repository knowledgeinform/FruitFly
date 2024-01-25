#include "stdafx.h"
#include "RacetrackOrbitBelief.h"
#include "Config.h"
#include <cmath>

int RacetrackOrbitBelief::m_ArrowAngleDeg = 0;
long RacetrackOrbitBelief::m_ArrowStartTimeSec = time(NULL);
	

RacetrackOrbitBelief::RacetrackOrbitBelief ()
{
	setRacetrackOrbit (0, 0, 0, 0, 1, 0, 0, true);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

RacetrackOrbitBelief::RacetrackOrbitBelief (double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg, double radiusM, double finalAltMslM, double standoffAltMslM, bool isClockwise)
{
	setRacetrackOrbit (lat1DecDeg, lon1DecDeg, lat2DecDeg, lon2DecDeg, radiusM, finalAltMslM, standoffAltMslM, isClockwise);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

RacetrackOrbitBelief::~RacetrackOrbitBelief ()
{
	gluDeleteQuadric(quadricObj);
}

void RacetrackOrbitBelief::readConfig()
{
	m_ArrowCount = Config::getInstance()->getValueAsInt ("RacetrackOrbitBelief.ArrowCount", 3);
	m_ArrowOrbitPeriodSec = Config::getInstance()->getValueAsInt ("RacetrackOrbitBelief.ArrowOrbitSec", 20);
	m_ArrowBackRadiusM = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowBackRadiusM", 20);
	m_ArrowLengthM = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowLengthM", 40);
	m_ArrowColorRed = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowColorRed", 0);
	m_ArrowColorGreen = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowColorGreen", 1);
	m_ArrowColorBlue = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowColorBlue", 0);
	m_ArrowColorAlpha = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.ArrowColorAlpha", 0.5);

	m_OrbitColorRed = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.OrbitColorRed", 0);
	m_OrbitColorGreen = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.OrbitColorGreen", 1);
	m_OrbitColorBlue = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.OrbitColorBlue", 0);
	m_OrbitLineWidth = Config::getInstance()->getValueAsDouble ("RacetrackOrbitBelief.OrbitLineWidth", 2);
}

bool RacetrackOrbitBelief::setRacetrackOrbit (double lat1DecDeg, double lon1DecDeg, double lat2DecDeg, double lon2DecDeg, double radiusM, double finalAltMslM, double standoffAltMslM, bool isClockwise)
{
	setLat1DecDeg (lat1DecDeg);
	setLon1DecDeg (lon1DecDeg);
	setLat2DecDeg (lat2DecDeg);
	setLon2DecDeg (lon2DecDeg);
	setRadiusM (radiusM);
	setFinalAltMslM (finalAltMslM);
	setStandoffAltMslM (standoffAltMslM);
	setIsClockwise (isClockwise);

	//Convert meters to lat/lon distances for circular orbit display in lat/lon referenced GL scene
	m_RadiusLat = radiusM / (2*3.14159265358979323*6378137/360.0);
	m_RadiusLon = radiusM / (2*3.14159265358979323*6378137/360.0 * cos(lat1DecDeg*3.14159265358979323/180));
		
	return true;
}

void RacetrackOrbitBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition == NULL)
		return;

	double offsetX = centerPointPosition->getLonDecDeg();
	double offsetY = centerPointPosition->getLatDecDeg();
	double offsetZ = centerPointPosition->getAltMslM();

	glColor3f (m_OrbitColorRed, m_OrbitColorGreen, m_OrbitColorBlue);
	glLineWidth (m_OrbitLineWidth);
	glBegin (GL_LINES);

	double probableAltM = 0;  //This is based on the assumption that clockwise orbits are offset, counter-clockwise are around target
	if (m_IsClockwise)
	{
		probableAltM = m_StandoffAltMslM;
	}
	else
	{
		probableAltM = m_FinalAltMslM;
	}

	if (isCircleRacetrack())
	{
		//Draw lines connecting the full orbit circle, one degree increments.
		for (int i = 1; i <= 90; i ++)
		{
			double iRad = i*3.14159265358979323/180;
			double iRadLast = (i-1)*3.14159265358979323/180;

			glVertex3d (estLonToMConv*(m_Lon1DecDeg-m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_Lat1DecDeg+m_RadiusLat*sin(iRad) - offsetY), probableAltM - offsetZ);
			glVertex3d (estLonToMConv*(m_Lon1DecDeg-m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_Lat1DecDeg+m_RadiusLat*sin(iRadLast) - offsetY), probableAltM - offsetZ);

			glVertex3d (estLonToMConv*(m_Lon1DecDeg-m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_Lat1DecDeg-m_RadiusLat*sin(iRad) - offsetY), probableAltM - offsetZ);
			glVertex3d (estLonToMConv*(m_Lon1DecDeg-m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_Lat1DecDeg-m_RadiusLat*sin(iRadLast) - offsetY), probableAltM - offsetZ);

			glVertex3d (estLonToMConv*(m_Lon1DecDeg+m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_Lat1DecDeg+m_RadiusLat*sin(iRad) - offsetY), probableAltM - offsetZ);
			glVertex3d (estLonToMConv*(m_Lon1DecDeg+m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_Lat1DecDeg+m_RadiusLat*sin(iRadLast) - offsetY), probableAltM - offsetZ);

			glVertex3d (estLonToMConv*(m_Lon1DecDeg+m_RadiusLon*cos(iRad) - offsetX), estLatToMConv*(m_Lat1DecDeg-m_RadiusLat*sin(iRad) - offsetY), probableAltM - offsetZ);
			glVertex3d (estLonToMConv*(m_Lon1DecDeg+m_RadiusLon*cos(iRadLast) - offsetX), estLatToMConv*(m_Lat1DecDeg-m_RadiusLat*sin(iRadLast) - offsetY), probableAltM - offsetZ);

		}
		glEnd();
	}



	if (isCircleRacetrack())
	{
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
			glTranslated (estLonToMConv*(m_Lon1DecDeg - offsetX), estLatToMConv*(m_Lat1DecDeg - offsetY), probableAltM - offsetZ);
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

}

bool RacetrackOrbitBelief::isCircleRacetrack()
{
	if (fabs(m_Lat1DecDeg - m_Lat2DecDeg) < 0.000001 && fabs(m_Lon1DecDeg - m_Lon2DecDeg) < 0.000001)
		return true;

	return false;
}

void RacetrackOrbitBelief::updateDataRows (DataRows* dataRows)
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

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_Lat1DecDeg), (m_Lat1DecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Latitude 1: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_Lon1DecDeg), (m_Lon1DecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Longitude 1: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_Lat2DecDeg), (m_Lat2DecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Latitude 2: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_Lon2DecDeg), (m_Lon2DecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Longitude 2: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_RadiusM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Orbit Radius: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_FinalAltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Final Altitude MSL: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_StandoffAltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Standoff Altitude MSL: ");
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

