#include "stdafx.h"
#include "AgentPositionBelief.h"
#include "Config.h"
#include <cmath>

int AgentPositionBelief::MAX_AGENT_POSITIONS = -1;

AgentPositionBelief::AgentPositionBelief ()
{
	m_AgentPositionHistory = new list<AgentPosition*> ();
	m_CurrentPosition = NULL;

	readConfig();
}

AgentPositionBelief::AgentPositionBelief (AgentPosition* firstPosition)
{
	m_AgentPositionHistory = new list<AgentPosition*> ();
	m_CurrentPosition = NULL;

	addAgentPosition (firstPosition);

	readConfig();
}

AgentPositionBelief::~AgentPositionBelief ()
{
	if (m_AgentPositionHistory != NULL)
	{
		while (m_AgentPositionHistory->size() > 0)
		{
			delete m_AgentPositionHistory->front();
			m_AgentPositionHistory->pop_front();
		}
		delete m_AgentPositionHistory;

		//Don't need to delete m_CurrentPosition because it's object is already in m_AgentPositionHistory
	}
}

void AgentPositionBelief::readConfig()
{
	if (MAX_AGENT_POSITIONS < 0)
		MAX_AGENT_POSITIONS = Config::getInstance()->getValueAsInt ("AgentPositionBelief.MaxAgentPositions", 100000);
}

bool AgentPositionBelief::addAgentPosition (AgentPosition* newPosition)
{
	if (newPosition == NULL)
		return false;

	if (m_AgentPositionHistory != NULL)
	{
		if (m_AgentPositionHistory->size() >= MAX_AGENT_POSITIONS)
		{
			//If max size reached, pop off the oldest position
			delete m_AgentPositionHistory->front();
			m_AgentPositionHistory->pop_front ();
		}

		if (m_CurrentPosition != NULL)
		{
			if (m_CurrentPosition->getMsgTimestampMs() >= newPosition->getMsgTimestampMs())
			{
				//If newPosition is not new data, delete newPosition and don't add it
				delete newPosition;
				return false;
			}
			else if (newPosition->getLatDecDeg() > 90 || newPosition->getLatDecDeg() < -90 || newPosition->getLonDecDeg() < -180 || newPosition->getLonDecDeg() > 180)
			{
				//If newPosition has invalid lat/lon data, delete newPosition and don't add it
				delete newPosition;
				return false;
			}
			else
			{
				//Store newPosition in history list and keep a pointer to it as current position
				m_AgentPositionHistory->push_back (newPosition);
				m_CurrentPosition = newPosition;
				m_LastUpdatedSec = time(NULL);
				return true;
			}
		}
		else
		{
			//Position is NULL.  Add it to the list in case this is being used as a reference/break point
			m_AgentPositionHistory->push_back (newPosition);
			m_CurrentPosition = newPosition;
			m_LastUpdatedSec = time(NULL);
			return true;
		}
	}

	//If not added, delete newPosition
	delete newPosition;
	return false;
}

bool AgentPositionBelief::clearAgentPositions ()
{
	if (m_AgentPositionHistory != NULL)
	{
		while (m_AgentPositionHistory->size() > 0)
			m_AgentPositionHistory->pop_front();

		return true;
	}
	return false;
}

void AgentPositionBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		if (m_CurrentPosition != NULL)
		{
			int idx = 0;
			char text[256];

			//Set labels and values for parameters
			int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
			sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
			unsigned long long val = m_CurrentPosition->getMsgTimestampMs();
			sprintf_s (text, sizeof(text), " %llu\0", val);
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Msg Timestamp: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
			sprintf_s (text, sizeof(text), " %.6f %c\0", fabs(m_CurrentPosition->getLatDecDeg()), (m_CurrentPosition->getLatDecDeg()>0?'N':'S'));
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Latitude: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
			sprintf_s (text, sizeof(text), " %.6f %c\0", fabs(m_CurrentPosition->getLonDecDeg()), (m_CurrentPosition->getLonDecDeg()>0?'E':'W'));
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Longitude: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
			sprintf_s (text, sizeof(text), " %.1f m\0", fabs(m_CurrentPosition->getAltMslM()));
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Altitude MSL: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
			sprintf_s (text, sizeof(text), " %.1f deg\0", fabs(m_CurrentPosition->getHeadingDecDeg()));
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Heading: \0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
			//Clear remaining data rows
			for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
			{
				SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
				SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
			}


		}
	}
}

