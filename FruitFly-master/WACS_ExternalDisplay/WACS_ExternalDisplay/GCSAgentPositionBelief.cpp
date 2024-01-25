#include "stdafx.h"
#include "GCSAgentPositionBelief.h"
#include <cmath>

GCSDisplay* GCSAgentPositionBelief::m_GCSDisplay = NULL;

GCSAgentPositionBelief::GCSAgentPositionBelief ()  : AgentPositionBelief()
{
}

GCSAgentPositionBelief::GCSAgentPositionBelief (AgentPosition* firstPosition) : AgentPositionBelief (firstPosition)
{
}

GCSAgentPositionBelief::~GCSAgentPositionBelief ()
{
}

void GCSAgentPositionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (m_CurrentPosition != NULL && centerPointPosition != NULL)
	{
		glPushMatrix();
		glTranslated(  (estLonToMConv*(m_CurrentPosition->getLonDecDeg() - centerPointPosition->getLonDecDeg())), estLatToMConv*((m_CurrentPosition->getLatDecDeg() - centerPointPosition->getLatDecDeg())), (m_CurrentPosition->getAltMslM() - centerPointPosition->getAltMslM()) );
		
		//First time, create the OpenGL display object.  This will be deleted by the BeliefCollection when finished.
		if (m_GCSDisplay == NULL)
			m_GCSDisplay = new GCSDisplay();

		m_GCSDisplay->displayGCS();
		glPopMatrix();
	}
}