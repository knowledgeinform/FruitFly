#include "stdafx.h"
#include "LoiterApproachPathBelief.h"
#include "Config.h"
#include <cmath>

LoiterApproachPathBelief::LoiterApproachPathBelief ()
{
	setLoiterApproachPath (0, 0, 0, 0, 0, 0, 0, 0, 0, false);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

LoiterApproachPathBelief::LoiterApproachPathBelief (double firstRangeLatDecDeg, double firstRangeLonDecDeg, double firstRangeAltMslM, double contactLatDecDeg, double contactLonDecDeg, double contactAltMslM, double safeRangeLatDecDeg, double safeRangeLonDecDeg, double safeRangeAltMslM, bool isPathValid)
{
	setLoiterApproachPath (firstRangeLatDecDeg, firstRangeLonDecDeg, firstRangeAltMslM, contactLatDecDeg, contactLonDecDeg, contactAltMslM, safeRangeLatDecDeg, safeRangeLonDecDeg, safeRangeAltMslM, isPathValid);

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	readConfig();

	displayList = -1;
}

LoiterApproachPathBelief::~LoiterApproachPathBelief ()
{
	gluDeleteQuadric(quadricObj);
}

void LoiterApproachPathBelief::readConfig()
{
	m_ValidMarkerColorRed = Config::getInstance()->getValueAsDouble ("LoiterApproachPathBelief.ValidMarkerColorRed", 0);
	m_ValidMarkerColorGreen = Config::getInstance()->getValueAsDouble ("LoiterApproachPathBelief.ValidMarkerColorGreen", 1);
	m_ValidMarkerColorBlue = Config::getInstance()->getValueAsDouble ("LoiterApproachPathBelief.ValidMarkerColorBlue", 0);
	m_ValidMarkerColorAlpha = Config::getInstance()->getValueAsDouble ("LoiterApproachPathBelief.ValidMarkerColorAlpha", 0.5);
	m_ValidMarkerRadiusM = Config::getInstance()->getValueAsDouble ("LoiterApproachPathBelief.ValidMarkerRadiusM", 25);
	
}

bool LoiterApproachPathBelief::setLoiterApproachPath (double firstRangeLatDecDeg, double firstRangeLonDecDeg, double firstRangeAltMslM, double contactLatDecDeg, double contactLonDecDeg, double contactAltMslM, double safeLatDecDeg, double safeLonDecDeg, double safeAltMslM, bool isPathValid)
{
	setFirstRangeLatDecDeg (firstRangeLatDecDeg);
	setFirstRangeLonDecDeg (firstRangeLonDecDeg);
	setFirstRangeAltMslM (firstRangeAltMslM);
	setContactLatDecDeg (contactLatDecDeg);
	setContactLonDecDeg (contactLonDecDeg);
	setContactAltMslM (contactAltMslM);
	setSafeLatDecDeg (safeLatDecDeg);
	setSafeLonDecDeg (safeLonDecDeg);
	setSafeAltMslM (safeAltMslM);
	setIsPathValid (isPathValid);
		
	return true;
}

void LoiterApproachPathBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition == NULL)
		return;

	double offsetX = centerPointPosition->getLonDecDeg();
	double offsetY = centerPointPosition->getLatDecDeg();
	double offsetZ = centerPointPosition->getAltMslM();

	glColor4f (m_ValidMarkerColorRed, m_ValidMarkerColorGreen, m_ValidMarkerColorBlue, m_ValidMarkerColorAlpha);
	if (displayList < 0)
	{
		//Create display call list first time with spinning arrow
		displayList = glGenLists(1);
		glNewList(displayList,GL_COMPILE);	
		gluSphere (quadricObj, m_ValidMarkerRadiusM, 30, 30);
		glEndList();
	}

	if (m_IsPathValid)
	{
		if (m_FirstRangeAltMslM > 0)
		{
			glPushMatrix();
			glTranslated (estLonToMConv*(m_FirstRangeLonDecDeg - offsetX), estLatToMConv*(m_FirstRangeLatDecDeg - offsetY), m_FirstRangeAltMslM - offsetZ);
			glCallList (displayList);
			glPopMatrix();
		}

		if (m_ContactAltMslM > 0)
		{
			glPushMatrix();
			glTranslated (estLonToMConv*(m_ContactLonDecDeg - offsetX), estLatToMConv*(m_ContactLatDecDeg - offsetY), m_ContactAltMslM - offsetZ);
			glCallList (displayList);
			glPopMatrix();
		}

		if (m_SafeAltMslM > 0)
		{
			glPushMatrix();
			glTranslated (estLonToMConv*(m_SafeLonDecDeg - offsetX), estLatToMConv*(m_SafeLatDecDeg - offsetY), m_SafeAltMslM - offsetZ);
			glCallList (displayList);
			glPopMatrix();
		}
	}
}


void LoiterApproachPathBelief::updateDataRows (DataRows* dataRows)
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

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_FirstRangeLatDecDeg), (m_FirstRangeLatDecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "First Range Latitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_FirstRangeLonDecDeg), (m_FirstRangeLonDecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "First Range Longitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_FirstRangeAltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "First Range Altitude MSL: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_ContactLatDecDeg), (m_ContactLatDecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Contact Latitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_ContactLonDecDeg), (m_ContactLonDecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Contact Longitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_ContactAltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Contact Altitude MSL: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_SafeLatDecDeg), (m_SafeLatDecDeg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Safe Point Latitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f %c", fabs(m_SafeLonDecDeg), (m_SafeLonDecDeg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Safe Point Longitude: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m", m_SafeAltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Safe Point Altitude MSL: ");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %s", m_IsPathValid?"Valid":"Invalid");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Is Path Valid?");
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

