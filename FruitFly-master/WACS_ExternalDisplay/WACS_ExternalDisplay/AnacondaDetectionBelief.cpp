#include "stdafx.h"
#include "AnacondaDetectionBelief.h"
#include "stdio.h"
#include "Config.h"
#include "WinFonts.h"

AnacondaDetectionBelief::AnacondaDetectionBelief ()
{
	m_LcdaAgentID[0] = '\0';
	m_LcdaBars = -1;
	m_LcdbAgentID[0] = '\0';
	m_LcdbBars = -1;

	readConfig();
}

AnacondaDetectionBelief::AnacondaDetectionBelief (char* detectionMessage)
{
	char detMsg[256];
	sprintf_s (&detMsg[0], sizeof(detMsg), "%s\0", detectionMessage);

	int startIdx = 0;
	while (detMsg[startIdx] == ' ')
		startIdx ++;
	
	if (strlen (&detMsg[startIdx]) < 18)
	{
		//With all the formatting, the detection message should be greater than 18 bytes if it is valid
		m_LcdaAgentID[0] = '\0';
		m_LcdaBars = -1;
		m_LcdbAgentID[0] = '\0';
		m_LcdbBars = -1;
	}
	else
	{
		char lcdaStr[] = "LCDA: \0";
		int lcdaDetIndex = -1;
		char lcdbStr[] = "LCDB: \0";
		int lcdbDetIndex = -1;
		
		for (unsigned int i = startIdx; i < strlen(&detMsg[startIdx]) - strlen(lcdaStr); i ++)
		{
			if (memcmp (&detMsg[i], &lcdaStr[0], strlen (&lcdaStr[0])) == 0)
			{
				//This index is start of lcda message.  First byte after lcdaStr should be highest
				//concentration lcda message.
				lcdaDetIndex = i + strlen (&lcdaStr[0]);

				if (detMsg[lcdaDetIndex] == '\n' || detMsg[lcdaDetIndex] == '\0')
					lcdaDetIndex = -1;
				break;
			}
		}

		for (unsigned int i = startIdx; i < strlen(&detMsg[startIdx]) - strlen(lcdbStr); i ++)
		{
			if (memcmp (&detMsg[i], &lcdbStr[0], strlen (&lcdbStr[0])) == 0)
			{
				//This index is start of lcdb message.  First byte after lcdbStr should be highest
				//concentration lcdb message.
				lcdbDetIndex = i + strlen (&lcdbStr[0]);

				if (detMsg[lcdbDetIndex] == '\n' || detMsg[lcdbDetIndex] == '\0')
					lcdbDetIndex = -1;
				break;
			}
		}
		

		if (lcdaDetIndex >= 0)
		{
			sscanf (&detMsg[lcdaDetIndex], "%s\0", &m_LcdaAgentID[0], sizeof(detMsg));
			int lcdaColonIndex = -1;
			for (unsigned int i = 0; i < strlen(&m_LcdaAgentID[0]); i ++)
			{
				if (memcmp (&m_LcdaAgentID[i], ":", 1) == 0)
				{
					//This index is colon in middle of lcda message.  First byte after colon
					//is bar value
					lcdaColonIndex = i;
					break;
				}
			}

			if (lcdaColonIndex >= 0)
			{
				if (lcdaColonIndex < strlen(&m_LcdaAgentID[0]) - 1)
					sscanf (&m_LcdaAgentID[lcdaColonIndex+1], "%d\0", &m_LcdaBars, sizeof(m_LcdaAgentID));
				
				m_LcdaAgentID[lcdaColonIndex] = '\0';
			}
		}

		if (lcdbDetIndex >= 0)
		{
			sscanf (&detMsg[lcdbDetIndex], "%s\0", &m_LcdbAgentID[0], sizeof(detMsg));
			int lcdbColonIndex = -1;
			for (unsigned int i = 0; i < strlen(&m_LcdbAgentID[0]); i ++)
			{
				if (memcmp (&m_LcdbAgentID[i], ":", 1) == 0)
				{
					//This index is colon in middle of lcdb message.  First byte after colon
					//is bar value
					lcdbColonIndex = i;
					break;
				}
			}

			if (lcdbColonIndex >= 0)
			{
				if (lcdbColonIndex < strlen(&m_LcdbAgentID[0]) - 1)
					sscanf (&m_LcdbAgentID[lcdbColonIndex+1], "%d\0", &m_LcdbBars, sizeof(m_LcdbAgentID));

				m_LcdbAgentID[lcdbColonIndex] = '\0';
			}
		}
	}
		
	readConfig();
}

AnacondaDetectionBelief::AnacondaDetectionBelief (char* lcdaAgentID, int lcdaBars, char* lcdbAgentID, int lcdbBars)
{
	sprintf_s (&m_LcdaAgentID[0], sizeof(m_LcdaAgentID), "%s\0", lcdaAgentID);
	lcdaBars = -1;
	sprintf_s (&m_LcdbAgentID[0], sizeof(m_LcdbAgentID), "%s\0", lcdbAgentID);
	lcdbBars = -1;
	
	readConfig();
}

AnacondaDetectionBelief::~AnacondaDetectionBelief ()
{
}

void AnacondaDetectionBelief::readConfig()
{
	m_HeaderColorRed = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorRed", 0);
	m_HeaderColorGreen = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorGreen", 0);
	m_HeaderColorBlue = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorBlue", 0);
	m_HeaderColorAlpha = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorAlpha", 0.9);

	m_HeaderFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderFontSize", 18);
	m_HeaderHorizontalPixels = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.HeaderHorizontalPixels", 400);
	m_HeaderVerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.TextVerticalPixels", 100);

	m_TextColorRed = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.TextColorRed", 1);
	m_TextColorGreen = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.TextColorGreen", 1);
	m_TextColorBlue = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.TextColorBlue", 1);
	m_TextColorAlpha = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.TextColorAlpha", 0.8);

	m_RowFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.RowFontSize", 14);
	m_RowHorizontalPixels = Config::getInstance()->getValueAsDouble ("AnacondaDetectionBelief.RowHorizontalPixels", 400);
	m_Row1VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row1VerticalPixels", 70);
	m_Row2VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row2VerticalPixels", 40);
}

void AnacondaDetectionBelief::getLcdaAgentID (char retVal[])
{
	if (sizeof(retVal) < strlen(m_LcdaAgentID))
	{
		sprintf_s (&retVal[0], 1, "\0");
		return;
	}

	sprintf_s (&retVal[0], sizeof(m_LcdaAgentID), "%s\0", m_LcdaAgentID);
}

void AnacondaDetectionBelief::getLcdbAgentID (char retVal[])
{
	if (sizeof(retVal) < strlen(m_LcdbAgentID))
	{
		sprintf_s (&retVal[0], 1, "\0");
		return;
	}

	sprintf_s (&retVal[0], sizeof(m_LcdbAgentID), "%s\0", m_LcdbAgentID);
}

void AnacondaDetectionBelief::setLcdaAgentID (char newVal[])
{
	if (strlen(newVal) > sizeof(m_LcdaAgentID))
	{
		sprintf_s (&m_LcdaAgentID[0], 1, "\0");
		return;
	}

	sprintf_s (&m_LcdaAgentID[0], sizeof(m_LcdaAgentID), "%s\0", newVal);
}

void AnacondaDetectionBelief::setLcdbAgentID (char newVal[])
{
	if (strlen(newVal) > sizeof(m_LcdbAgentID))
	{
		sprintf_s (&m_LcdbAgentID[0], 1, "\0");
		return;
	}

	sprintf_s (&m_LcdbAgentID[0], sizeof(m_LcdbAgentID), "%s\0", newVal);
}

void AnacondaDetectionBelief::drawGlOverlay (int glWidth, int glHeight)
{
	//Write text
	glColor4f (m_HeaderColorRed, m_HeaderColorGreen, m_HeaderColorBlue, m_HeaderColorAlpha);
	WinFonts::glPrint2d (m_HeaderFontSize, m_HeaderHorizontalPixels, m_HeaderVerticalPixels, "ANACONDA\0");

	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
	
	char msg1 [256];
	sprintf_s (msg1, 256, "LCDA - \0");
	if (m_LcdaBars > 0 && strlen (m_LcdaAgentID) > 0)
	{
		sprintf_s (&msg1[strlen(msg1)], 256-strlen(msg1), "%s:%d\0", m_LcdaAgentID, m_LcdaBars);
	}
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row1VerticalPixels, msg1);

	char msg2 [256];
	sprintf_s (msg2, 256, "LCDB - \0");
	if (m_LcdbBars > 0 && strlen (m_LcdbAgentID) > 0)
	{
		sprintf_s (&msg2[strlen(msg2)], 256-strlen(msg2), "%s:%d\0", m_LcdbAgentID, m_LcdbBars);
	}
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row2VerticalPixels, msg2);
}

void AnacondaDetectionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	//Nothing to display for these right now.
	return;
}

void AnacondaDetectionBelief::updateDataRows (DataRows* dataRows)
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
	
		sprintf_s (text, sizeof(text), " %s\0", m_LcdaAgentID);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "LCDA Agent ID: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_LcdaBars);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "LCDA Bars: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %s\0", m_LcdbAgentID);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "LCDB Agent ID: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_LcdbBars);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "LCDB Bars: \0");
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
