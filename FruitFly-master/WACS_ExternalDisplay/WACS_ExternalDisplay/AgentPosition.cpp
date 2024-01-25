#include "stdafx.h"
#include "AgentPosition.h"

AgentPosition::AgentPosition (long long msgTimestampMs, double latDecDeg, double lonDecDeg, double altMslM, double headingDecDeg)
{
	setMsgTimestampMs (msgTimestampMs);
	setLatDecDeg (latDecDeg);
	setLonDecDeg (lonDecDeg);
	setAltMslM (altMslM);
	setHeadingDecDeg (headingDecDeg);

}


AgentPosition::AgentPosition (AgentPosition* position)
{
	if (position == NULL)
		return;

	setMsgTimestampMs (position->getMsgTimestampMs());
	setLatDecDeg (position->getLatDecDeg());
	setLonDecDeg (position->getLonDecDeg());
	setAltMslM (position->getAltMslM());
	setHeadingDecDeg (position->getHeadingDecDeg());
}