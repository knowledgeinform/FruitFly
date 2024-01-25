#include "stdafx.h"
#include "ExplosionBelief.h"
#include "stdio.h"
#include "Config.h"
#include "WinFonts.h"

GLUquadricObj *ExplosionBelief::quadricObj = NULL; //!< GL Quadric object to use for displaying cloud location spheres
int ExplosionBelief::displayList = -1; //!< GL display list to display cloud location spheres


ExplosionBelief::ExplosionBelief()
{

	readConfig();
}

ExplosionBelief::ExplosionBelief(long long timestampMs, double centerLatDecDeg, double centerLonDecDeg, double altMslM)
{
	setMsgTimestampMs (timestampMs);
	setCenterLatDecDeg (centerLatDecDeg);
	setCenterLonDecDeg (centerLonDecDeg);
	setAltMslM (altMslM);

	readConfig();
}

ExplosionBelief::~ExplosionBelief()
{

}

void ExplosionBelief::readConfig()
{
	m_TextColorRed = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextColorRed", 1.0);
	m_TextColorGreen = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextColorGreen", .1);
	m_TextColorBlue = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextColorBlue", .1);
	m_TextColorAlpha = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextColorAlpha", .9);
	m_TextFontSize = 1000*Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextFontSize", 30);
	m_TextHorizontalPercentage = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextHorizontalPercentage", .5);
	m_TextVerticalPercentage = Config::getInstance()->getValueAsDouble ("ExplosionBelief.TextVerticalPercentage", .05);

	m_MarkerColorRed = Config::getInstance()->getValueAsDouble ("ExplosionBelief.MarkerColorRed", 1.0);
	m_MarkerColorGreen = Config::getInstance()->getValueAsDouble ("ExplosionBelief.MarkerColorGreen", .2);
	m_MarkerColorBlue = Config::getInstance()->getValueAsDouble ("ExplosionBelief.MarkerColorBlue", .2);
	m_MarkerColorAlpha = Config::getInstance()->getValueAsDouble ("ExplosionBelief.MarkerColorAlpha", .8);	
	m_MarkerRadiusM = Config::getInstance()->getValueAsDouble ("ExplosionBelief.MarkerRadiusM", 200);

	m_TimeToDisplayMessageSec = Config::getInstance()->getValueAsInt ("ExplosionBelief.TimeToDisplayMessageSec", 600);
}

void ExplosionBelief::initQuadric()
{
	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	displayList = -1;
}

void ExplosionBelief::destroyGlObjects ()
{
	if (displayList >= 0)
	{
		gluDeleteQuadric(quadricObj);
		displayList = -1;
	}
}

void ExplosionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition != NULL)
	{
		if (displayList < 0)
		{
			initQuadric();

			//First time, generate GL call list for spheres
			displayList = glGenLists(1);
			glNewList(displayList,GL_COMPILE);	
			gluSphere (quadricObj, m_MarkerRadiusM, 30, 30);
			glEndList();
		}

		if ((time(NULL) - m_LastUpdatedSec) < m_TimeToDisplayMessageSec)
		{
			//Draw explosion marker
			glEnable (GL_LIGHTING);
			glColor4f (m_MarkerColorRed, m_MarkerColorGreen, m_MarkerColorBlue, m_MarkerColorAlpha);
			
			glPushMatrix();
			glTranslated (estLonToMConv*(m_CenterLonDecDeg - centerPointPosition->getLonDecDeg()), estLatToMConv*(m_CenterLatDecDeg - centerPointPosition->getLatDecDeg()), m_AltMslM - centerPointPosition->getAltMslM());
			glCallList (displayList);
			glPopMatrix();

			glDisable (GL_LIGHTING);
			
		}

	}
}

void ExplosionBelief::drawGlOverlay (int glWidth, int glHeight)
{
	if ((time(NULL) - m_LastUpdatedSec) < m_TimeToDisplayMessageSec)
	{
		//Write explosion text
		glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
		WinFonts::glPrint2d (m_TextFontSize, glWidth*m_TextHorizontalPercentage, glHeight*m_TextVerticalPercentage, "Explosion Detected!");
	}
}
		
void ExplosionBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %llu\0", m_MsgTimestampMs);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Message timestamp\0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f deg\0", m_CenterLatDecDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Explosion Pos Lat  \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_CenterLonDecDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Explosion Pos Lon \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_AltMslM);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Explosion Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		if (time(NULL) - time(NULL)/1000 < m_TimeToDisplayMessageSec)
			sprintf_s (text, sizeof(text), " yes\0");
		else
			sprintf_s (text, sizeof(text), " no\0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Explosion recent? \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);


		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}

