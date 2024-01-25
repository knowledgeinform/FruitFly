#include "stdafx.h"
#include "AgentModeBelief.h"
#include "stdio.h"


AgentModeBelief::AgentModeBelief()
{
	memcpy (m_Mode, "\0", 128);
	m_MsgTimestampMs = -1;
}

AgentModeBelief::AgentModeBelief(long long timestampMs, char mode[])
{
	memcpy (m_Mode, mode, 128);
	m_MsgTimestampMs = timestampMs;
}


AgentModeBelief::~AgentModeBelief()
{

}


void AgentModeBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	//No OpenGL display for AgentModeBelief
	return;
}

void AgentModeBelief::updateDataRows (DataRows* dataRows)
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
	
		sprintf_s (text, sizeof(text), " %llu\0", m_MsgTimestampMs);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Msg Timestamp: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %s\0", m_Mode);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Mode: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		//Clear remaining data rows
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}
}

void AgentModeBelief::getMode (char msg[], int maxLength)
{
	if (maxLength < strlen(m_Mode))
	{
		sprintf_s (&m_Mode[0], 1, "\0");
		return;
	}

	sprintf_s (&msg[0], sizeof(m_Mode), "%s\0", m_Mode);
}

void AgentModeBelief::setMode (char msg[])
{
	if (strlen(msg) > sizeof(m_Mode))
	{
		sprintf_s (&m_Mode[0], 1, "\0");
		return;
	}

	sprintf_s (&m_Mode[0], sizeof(m_Mode), "%s\0", msg);
}

bool AgentModeBelief::modeEquals (char checkMode[])
{
	if (strlen(checkMode) != strlen(m_Mode))
		return false;

	for (unsigned int i = 0; i < strlen (checkMode); i ++)
	{
		if (checkMode[i] != m_Mode[i])
			return false;
	}
	return true;
}
