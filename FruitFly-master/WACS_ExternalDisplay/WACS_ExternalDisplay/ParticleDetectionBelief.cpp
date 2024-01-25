#include "stdafx.h"
#include "ParticleDetectionBelief.h"
#include "stdio.h"
#include "Config.h"
#include "WinFonts.h"

ParticleDetectionBelief::ParticleDetectionBelief ()
{
	m_LargeCounts = 0;
	m_SmallCounts = 0;
	m_BioLargeCounts = 0;
	m_BioSmallCounts = 0;

	readConfig();
}

ParticleDetectionBelief::ParticleDetectionBelief (char* detectionMessage)
{
	char detMsg [256];
	sprintf_s (&detMsg[0], sizeof(detMsg), "%s\0", detectionMessage);

	int startIdx = 0;
	while (detectionMessage[startIdx] == ' ')
		startIdx ++;
	sscanf (&detectionMessage[startIdx], "Large: %d   Small: %d\nBio Large: %d Bio Small: %d\n\0", &m_LargeCounts, &m_SmallCounts, &m_BioLargeCounts, &m_BioSmallCounts, sizeof(detMsg));

	readConfig();
}

ParticleDetectionBelief::ParticleDetectionBelief (int largeCounts, int smallCounts, int bioLargeCounts, int bioSmallCounts)
{
	m_LargeCounts = largeCounts;
	m_SmallCounts = smallCounts;
	m_BioLargeCounts = bioLargeCounts;
	m_BioSmallCounts = bioSmallCounts;
	
	readConfig();
}

ParticleDetectionBelief::~ParticleDetectionBelief ()
{
}

void ParticleDetectionBelief::readConfig()
{
	m_HeaderColorRed = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorRed", 0);
	m_HeaderColorGreen = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorGreen", 0);
	m_HeaderColorBlue = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorBlue", 0);
	m_HeaderColorAlpha = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorAlpha", 0.9);

	m_HeaderFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderFontSize", 18);
	m_HeaderHorizontalPixels = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.HeaderHorizontalPixels", 600);
	m_HeaderVerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.TextVerticalPixels", 100);

	m_TextColorRed = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.TextColorRed", 1);
	m_TextColorGreen = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.TextColorGreen", 1);
	m_TextColorBlue = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.TextColorBlue", 1);
	m_TextColorAlpha = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.TextColorAlpha", 0.8);

	m_RowFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.RowFontSize", 14);
	m_RowHorizontalPixels = Config::getInstance()->getValueAsDouble ("ParticleDetectionBelief.RowHorizontalPixels", 600);
	m_Row1VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row1VerticalPixels", 70);
	m_Row2VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row2VerticalPixels", 40);
}

void ParticleDetectionBelief::drawGlOverlay (int glWidth, int glHeight)
{
	//Write text
	glColor4f (m_HeaderColorRed, m_HeaderColorGreen, m_HeaderColorBlue, m_HeaderColorAlpha);
	WinFonts::glPrint2d (m_HeaderFontSize, m_HeaderHorizontalPixels, m_HeaderVerticalPixels, "IBAC\0");

	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
	char msg [256];
	sprintf_s (msg, 256, "Large: %d  Small: %d\0", m_LargeCounts, m_SmallCounts);
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row1VerticalPixels, msg);
	
	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
	sprintf_s (msg, 256, "Bio Large: %d  Bio Small: %d\0", m_BioLargeCounts, m_BioSmallCounts);
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row2VerticalPixels, msg);
}

void ParticleDetectionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	//Nothing to display for these right now.
	return;
}

void ParticleDetectionBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		//Set labels and values for parameters
		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %d\0", m_LargeCounts);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Large Counts: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_SmallCounts);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Small Counts: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %d\0", m_BioLargeCounts);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Bio Large Counts: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_BioSmallCounts);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Bio Small Counts: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		//Clear remaining data rows
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}

