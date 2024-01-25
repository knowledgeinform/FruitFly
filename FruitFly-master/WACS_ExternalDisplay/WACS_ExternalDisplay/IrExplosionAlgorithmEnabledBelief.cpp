#include "stdafx.h"
#include "IrExplosionAlgorithmEnabledBelief.h"
#include "stdio.h"
#include "Config.h"
#include "WinFonts.h"


IrExplosionAlgorithmEnabledBelief::IrExplosionAlgorithmEnabledBelief()
{

	readConfig();
}

IrExplosionAlgorithmEnabledBelief::IrExplosionAlgorithmEnabledBelief(bool enabled, double timeUntilExplosionMs)
{
	setEnabled (enabled);
	setTimeUntilExplosionMs (timeUntilExplosionMs);
	
	readConfig();
}

IrExplosionAlgorithmEnabledBelief::~IrExplosionAlgorithmEnabledBelief()
{

}

void IrExplosionAlgorithmEnabledBelief::readConfig()
{
	m_TextColorRed = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextColorRed", 1.0);
	m_TextColorGreen = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextColorGreen", 1.0);
	m_TextColorBlue = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextColorBlue", 1.0);
	m_TextColorAlpha = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextColorAlpha", .8);
	m_TextFontSize = 1000*Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextFontSize", 20);
	m_TextHorizontalPixels = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextHorizontalPixels", 10);
	m_TextVerticalPixels = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.TextVerticalPixels", 20);

	m_ShadedBoxColorRed = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxColorRed", 0.3);
	m_ShadedBoxColorGreen = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxColorGreen", .3);
	m_ShadedBoxColorBlue = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxColorBlue", .3);
	m_ShadedBoxColorAlpha = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxColorAlpha", .5);
	m_ShadedBoxHeightPixels = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxHeightPixels", 40);
	m_ShadedBoxWidthPixels = Config::getInstance()->getValueAsDouble ("IrExplosionAlgorithmEnabledBelief.ShadedBoxWidthPixels", 300);
}

void IrExplosionAlgorithmEnabledBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	//nothing in 3d world for now
}

void IrExplosionAlgorithmEnabledBelief::drawGlOverlay (int glWidth, int glHeight)
{
	//draw shaded box
	glColor4f (m_ShadedBoxColorRed, m_ShadedBoxColorGreen, m_ShadedBoxColorBlue, m_ShadedBoxColorAlpha);
	glBegin (GL_QUADS);
	glVertex3d (0, glHeight-m_ShadedBoxHeightPixels, -glWidth+1);
	glVertex3d (m_ShadedBoxWidthPixels, glHeight-m_ShadedBoxHeightPixels, -glWidth+1);
	glVertex3d (m_ShadedBoxWidthPixels, glHeight, -glWidth+1);
	glVertex3d (0, glHeight, -glWidth+1);
	glEnd();


	//Write explosion time text
	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);

	int totalSeconds = (int)(m_TimeUntilExplosionMs/1000);
    int minutes = abs(totalSeconds/60);
    int seconds = abs(totalSeconds) - minutes*60;

	char timeMsg [256];
	if (totalSeconds >= 0)
		sprintf_s (timeMsg, 256, "Time Until Expected Explosion: \0");
	else
		sprintf_s (timeMsg, 256, "Time After Expected Explosion: \0");
    
    if (minutes > 0)
        sprintf_s (&timeMsg[strlen(timeMsg)], 256-strlen(timeMsg), "%d min, %d sec\0", minutes, seconds);
    else
        sprintf_s (&timeMsg[strlen(timeMsg)], 256-strlen(timeMsg), "%d sec\0", seconds);
    
	WinFonts::glPrint2d (m_TextFontSize, m_TextHorizontalPixels, glHeight-m_TextVerticalPixels, timeMsg);
}
		
void IrExplosionAlgorithmEnabledBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %d ms\0", m_TimeUntilExplosionMs);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time until Explosion\0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		if (m_Enabled)
			sprintf_s (text, sizeof(text), " yes\0");
		else
			sprintf_s (text, sizeof(text), " no\0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Algorithm Enabled? \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}

