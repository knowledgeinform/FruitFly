#include "stdafx.h"
#include "ShadowAgentPositionBelief.h"
#include "Config.h"
#include <cmath>

UavDisplayBase* ShadowAgentPositionBelief::m_UavDisplay = NULL;


ShadowAgentPositionBelief::ShadowAgentPositionBelief ()  : AgentPositionBelief()
{
	m_DisplayUav = true;
	readConfig();
}

ShadowAgentPositionBelief::ShadowAgentPositionBelief (AgentPosition* firstPosition) : AgentPositionBelief (firstPosition)
{
	m_DisplayUav = true;
	readConfig();
}

ShadowAgentPositionBelief::~ShadowAgentPositionBelief ()
{
}

void ShadowAgentPositionBelief::readConfig()
{
	string uavName;
	Config::getInstance()->getValue ("UavDisplay.UavName", uavName, "Shadow");
	if (uavName.compare ("Dakota") == 0)
		m_UavType = UavDisplayBase::DAKOTA;
	else //if (uavName.compare ("Shadow") == 0)
		m_UavType = UavDisplayBase::SHADOW;

	m_HistoryLineWidth = Config::getInstance()->getValueAsDouble ("ShadowAgent.History.LineWidth", 1);
	m_HistoryLineRedColor = Config::getInstance()->getValueAsDouble ("ShadowAgent.History.LineColor.Red", 1);
	m_HistoryLineGreenColor = Config::getInstance()->getValueAsDouble ("ShadowAgent.History.LineColor.Green", 1);
	m_HistoryLineBlueColor = Config::getInstance()->getValueAsDouble ("ShadowAgent.History.LineColor.Blue", 0);
}

void ShadowAgentPositionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (m_CurrentPosition != NULL && centerPointPosition != NULL)
	{
		//Draw UAV path/history
		if (m_AgentPositionHistory != NULL)
		{
			//glLineWidth (1.0f);
			//glColor3f (1.0f, 1.0f, 0.0f);
			glLineWidth (m_HistoryLineWidth);
			glColor3f (m_HistoryLineRedColor, m_HistoryLineGreenColor, m_HistoryLineBlueColor);
			glBegin (GL_LINE_STRIP);

			for( std::list<AgentPosition*>::iterator i(m_AgentPositionHistory->begin()), end(m_AgentPositionHistory->end()); i != end; ++i )
			{ 
				AgentPosition* nextPosition = (*i);

				if (nextPosition != NULL)
				{
					glVertex3d (estLonToMConv*(nextPosition->getLonDecDeg() - centerPointPosition->getLonDecDeg()), estLatToMConv*(nextPosition->getLatDecDeg() - centerPointPosition->getLatDecDeg()), nextPosition->getAltMslM() - centerPointPosition->getAltMslM());
				}
			}

			glEnd();
		}


		if (m_DisplayUav)
		{
			//Draw current UAV position
			glPushMatrix();
			glTranslated(  (estLonToMConv*(m_CurrentPosition->getLonDecDeg() - centerPointPosition->getLonDecDeg())), estLatToMConv*((m_CurrentPosition->getLatDecDeg() - centerPointPosition->getLatDecDeg())), (m_CurrentPosition->getAltMslM() - centerPointPosition->getAltMslM()) );
			glRotatef (-m_CurrentPosition->getHeadingDecDeg(), 0, 0, 1);

			if (m_UavDisplay == NULL)
			{
				if (m_UavType == UavDisplayBase::SHADOW)
					m_UavDisplay = new ShadowUavDisplay();
				else if (m_UavType == UavDisplayBase::DAKOTA)
					m_UavDisplay = new DakotaUavDisplay();
			}
			m_UavDisplay->displayUav();
			glPopMatrix();
		}
	}
}