#include "stdafx.h"
#include "BeliefCollection.h"
#include "LockObject.h"
#include "Config.h"
#include "Logger.h"
#include "math.h"
#include "Viewer3D.h"

void* BeliefCollection_PropogationThreadProc(void *arg);

BeliefCollection::BeliefCollection ()
{
	m_CurrentBeliefs = new BeliefTemplate* [m_BeliefListSize] ();
	m_RawDataDisplayOpt = -1;
	m_DataRows = NULL;

	m_GlPitchDegrees = 0;
	m_GlHeadingDegrees = 0;
	m_GlWidth = 50;
	m_GlHeight = 50;
	m_DtedObject = NULL;

	InitializeCriticalSection (&m_CurrentBeliefsLock);

	m_ShadedBoxColorRed = Config::getInstance()->getValueAsDouble ("SensorOverlays.ShadedBoxColorRed", 0.3);
	m_ShadedBoxColorGreen = Config::getInstance()->getValueAsDouble ("SensorOverlays.ShadedBoxColorGreen", .3);
	m_ShadedBoxColorBlue = Config::getInstance()->getValueAsDouble ("SensorOverlays.ShadedBoxColorBlue", .3);
	m_ShadedBoxColorAlpha = Config::getInstance()->getValueAsDouble ("SensorOverlays.ShadedBoxColorAlpha", .5);
	m_ShadedBoxHeightPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.ShadedBoxHeightPixels", 110);

	resetBeliefs ();

	m_PropogationThreadParams = NULL;
	startPropogationThread ();
}

BeliefCollection::~BeliefCollection ()
{
	stopPropogationThread();

	clearBeliefs();
	delete [] m_CurrentBeliefs;
}

void BeliefCollection::clearBeliefs ()
{
	LockObject lock (&m_CurrentBeliefsLock);

	//Delete static display lists for beliefs
	if (m_CurrentBeliefs[m_AgentPositionBeliefIdx] != NULL)
	{
		if (((ShadowAgentPositionBelief*)m_CurrentBeliefs[m_AgentPositionBeliefIdx])->m_UavDisplay != NULL)
		{
			delete ((ShadowAgentPositionBelief*)m_CurrentBeliefs[m_AgentPositionBeliefIdx])->m_UavDisplay;
			((ShadowAgentPositionBelief*)m_CurrentBeliefs[m_AgentPositionBeliefIdx])->m_UavDisplay = NULL;
		}
	}
	if (m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx] != NULL)
	{
		if (((PiccoloTelemetryBelief*)m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx])->m_UavDisplay != NULL)
		{
			delete ((PiccoloTelemetryBelief*)m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx])->m_UavDisplay;
			((PiccoloTelemetryBelief*)m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx])->m_UavDisplay = NULL;
		}
	}
	if (m_CurrentBeliefs[m_GCSPositionBeliefIdx] != NULL)
	{
		if (((GCSAgentPositionBelief*)m_CurrentBeliefs[m_GCSPositionBeliefIdx])->m_GCSDisplay != NULL)
		{
			delete ((GCSAgentPositionBelief*)m_CurrentBeliefs[m_GCSPositionBeliefIdx])->m_GCSDisplay;
			((GCSAgentPositionBelief*)m_CurrentBeliefs[m_GCSPositionBeliefIdx])->m_GCSDisplay = NULL;
		}
	}

	//Delete beliefs
	for (int i = 0; i < m_BeliefListSize; i ++)
	{
		if (m_CurrentBeliefs[i] != NULL)
		{
			delete m_CurrentBeliefs[i];
			m_CurrentBeliefs[i] = NULL;
		}
	}

	CloudPredictionBelief::destroyGlObjects();
	ExplosionBelief::destroyGlObjects();
	MetBelief::destroyGlObjects();
}

void BeliefCollection::resetBeliefs ()
{
	clearBeliefs();

	//Add MET belief with no wind data, so compass rose is drawn immediately.
	MetBelief* emptyMetBelief = new MetBelief ();
	this->updateMetBelief (emptyMetBelief);
}


void BeliefCollection::startPropogationThread ()
{
	m_KillPropogationThread = false;
	m_PropogationThreadDone = false;

	m_PropogationThreadParams = new PropogationThreadParams ();
	m_PropogationThreadParams->m_CurrentBeliefsLock = &m_CurrentBeliefsLock;
	m_PropogationThreadParams->m_CurrentBeliefs = m_CurrentBeliefs;
	m_PropogationThreadParams->m_PropogatedCloudDetectionBeliefIdx = m_PropogatedCloudDetectionBeliefIdx;
	m_PropogationThreadParams->m_MetBeliefIdx = m_MetBeliefIdx;
	m_PropogationThreadParams->m_KillThread = &m_KillPropogationThread;
	m_PropogationThreadParams->m_ThreadDone = &m_PropogationThreadDone;
	m_PropogationThreadParams->m_PausePropogation = &Viewer3D::m_PauseDetectionPropogation;
	 
	// Startup the thread
	pthread_create(&m_PropogationThread, NULL, BeliefCollection_PropogationThreadProc, m_PropogationThreadParams);
}

void BeliefCollection::stopPropogationThread ()
{
	m_KillPropogationThread = true;

	int maxLoops = 500;
	while (!m_PropogationThreadDone && (maxLoops--)>0)
	{
		Sleep (10);
	}

	delete m_PropogationThreadParams;
	m_PropogationThreadParams = NULL;

	if (maxLoops == 0)
	{
		Logger::getInstance()->logMessage ("BeliefCollection: Cloud not stop propogating thread.");
	}
}

void BeliefCollection::setDtedObject (DTEDObject* dted)
{
	//if (m_DtedObject != NULL)
	//	delete m_DtedObject;
	//Don't delete in BeliefCollection, assume that the MapData object will take care of it

	m_DtedObject = dted;
}

void BeliefCollection::setWindowSize (int winW, int winH)
{
	m_GlWidth = winW;
	m_GlHeight = winH;
}

void BeliefCollection::setCameraAngles (double pitchDegrees, double headingDegrees)
{
	m_GlPitchDegrees = pitchDegrees;
	m_GlHeadingDegrees = headingDegrees;
}

void BeliefCollection::setDataRows (DataRows* dataRows) 
{
	LockObject lock (&m_CurrentBeliefsLock);
	m_DataRows = dataRows;
}

void BeliefCollection::drawGlOverlay (bool drawAgentPositionBelief, bool drawGCSAgentPositionBelief, bool drawCircularOrbitBelief, bool drawBiopodDetectionBelief, bool drawAnacondaDetectionBelief, bool uavDisplayFromPicTel, bool drawPredictedCloud, bool drawExplosionBelief, bool drawMetBelief, bool drawIrExplosionAlgorithmEnabledBelief, bool drawSensorOverlays, bool convertAltitudeToFeet)
{
	if (m_CurrentBeliefs != NULL)
	{
		LockObject lock (&m_CurrentBeliefsLock);

		if (drawSensorOverlays)
		{
			drawSensorOverlayShadedBox (m_GlWidth, m_GlHeight);
		}

		char mode [128];
		if (m_CurrentBeliefs[m_AgentModeBeliefIdx] != NULL)
			((AgentModeBelief*)m_CurrentBeliefs[m_AgentModeBeliefIdx])->getMode (mode, 128);
		else
			sprintf_s (mode, 128, "\0");


		if (drawCircularOrbitBelief) 
		{
			if (strcmp (mode, "loiter\0") == 0 && m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx] != NULL)
				m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
			else if (strcmp (mode, "loiter\0") != 0 && m_CurrentBeliefs[m_CircularOrbitBeliefIdx] != NULL)
				m_CurrentBeliefs[m_CircularOrbitBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawIrExplosionAlgorithmEnabledBelief && m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx] != NULL)
		{
			if (strcmp (mode, "loiter\0") == 0 && m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx] != NULL)
				m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawAgentPositionBelief && m_CurrentBeliefs[m_AgentPositionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_AgentPositionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawSensorOverlays && m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx] != NULL)
		{
			((PiccoloTelemetryBelief*)m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx])->convertAltitudeToFeet (convertAltitudeToFeet);
			m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawGCSAgentPositionBelief && m_CurrentBeliefs[m_GCSPositionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_GCSPositionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}
		
		if (drawSensorOverlays && m_CurrentBeliefs[m_CloudDetectionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_CloudDetectionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawSensorOverlays && m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawSensorOverlays && m_CurrentBeliefs[m_ParticleDetectionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_ParticleDetectionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawPredictedCloud && m_CurrentBeliefs[m_CloudPredictionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_CloudPredictionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawExplosionBelief && m_CurrentBeliefs[m_ExplosionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_ExplosionBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}

		if (drawMetBelief && m_CurrentBeliefs[m_MetBeliefIdx] != NULL)
		{
			((MetBelief*)m_CurrentBeliefs[m_MetBeliefIdx])->setCameraAngles (m_GlPitchDegrees, m_GlHeadingDegrees);
			m_CurrentBeliefs[m_MetBeliefIdx]->drawGlOverlay (m_GlWidth, m_GlHeight);
		}
	}
}

void BeliefCollection::drawSensorOverlayShadedBox (int glWidth, int glHeight)
{
	//Draw text shaded box
	glColor4f (m_ShadedBoxColorRed, m_ShadedBoxColorGreen, m_ShadedBoxColorBlue, m_ShadedBoxColorAlpha);
	glBegin (GL_QUADS);
	glVertex3d (0, 0, 0);
	glVertex3d (glWidth, 0, 0);
	glVertex3d (glWidth, m_ShadedBoxHeightPixels, 0);
	glVertex3d (0, m_ShadedBoxHeightPixels, 0);
	glEnd();
}

void BeliefCollection::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv, bool drawAgentPositionBelief, bool drawGCSAgentPositionBelief, bool drawCircularOrbitBelief, bool drawBiopodDetectionBelief, bool drawAnacondaDetectionBelief, bool uavDisplayFromPicTel, bool drawPredictedCloud, bool drawExplosionBelief, bool drawMetBelief, bool drawIrExplosionAlgorithmEnabledBelief, bool drawPropogatedDetections, bool drawLoiterApproachPath)
{
	if (m_CurrentBeliefs != NULL)
	{
		LockObject lock (&m_CurrentBeliefsLock);

		char mode [128];
		if (m_CurrentBeliefs[m_AgentModeBeliefIdx] != NULL)
			((AgentModeBelief*)m_CurrentBeliefs[m_AgentModeBeliefIdx])->getMode (mode, 128);
		else
			sprintf_s (mode, 128, "\0");
		
		
		if (drawCircularOrbitBelief) 
		{
			if (strcmp (mode, "loiter\0") == 0 && m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx] != NULL)
				m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
			else if (strcmp (mode, "loiter\0") != 0 && m_CurrentBeliefs[m_CircularOrbitBeliefIdx] != NULL)
				m_CurrentBeliefs[m_CircularOrbitBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawIrExplosionAlgorithmEnabledBelief && m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx] != NULL)
		{
			if (strcmp (mode, "loiter\0") == 0 && m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx] != NULL)
				m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawAgentPositionBelief && m_CurrentBeliefs[m_AgentPositionBeliefIdx] != NULL)
		{
			//If displaying UAV by Piccolo Telemetry, only display AgentPositionBelief's historical positions
			((ShadowAgentPositionBelief*)m_CurrentBeliefs[m_AgentPositionBeliefIdx])->toggleDisplayUav (!uavDisplayFromPicTel);
			m_CurrentBeliefs[m_AgentPositionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawAgentPositionBelief && uavDisplayFromPicTel && m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx]->drawGL(centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawGCSAgentPositionBelief && m_CurrentBeliefs[m_GCSPositionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_GCSPositionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}
		
		if ((drawAnacondaDetectionBelief || drawBiopodDetectionBelief)&& m_CurrentBeliefs[m_CloudDetectionBeliefIdx] != NULL)
		{
			//Toggle whether particles and/or chemical detections should be drawn
			((CloudDetectionBelief*)m_CurrentBeliefs[m_CloudDetectionBeliefIdx])->toggleDisplayParticles (drawBiopodDetectionBelief);
			((CloudDetectionBelief*)m_CurrentBeliefs[m_CloudDetectionBeliefIdx])->toggleDisplayChemical (drawAnacondaDetectionBelief);
			m_CurrentBeliefs[m_CloudDetectionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		//if (drawPropogatedDetections && (drawAnacondaDetectionBelief || drawBiopodDetectionBelief) && m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx] != NULL)
		if (drawPropogatedDetections && m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx] != NULL)
		{
			//Toggle whether particles and/or chemical detections should be drawn
			//((PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx])->toggleDisplayParticles (drawBiopodDetectionBelief);
			((PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx])->toggleDisplayParticles (true);
			//((PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx])->toggleDisplayChemical (drawAnacondaDetectionBelief);
			((PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx])->toggleDisplayChemical (true);
			m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawPredictedCloud && m_CurrentBeliefs[m_CloudPredictionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_CloudPredictionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawExplosionBelief && m_CurrentBeliefs[m_ExplosionBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_ExplosionBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawMetBelief && m_CurrentBeliefs[m_MetBeliefIdx] != NULL)
		{
			m_CurrentBeliefs[m_MetBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}

		if (drawLoiterApproachPath && m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx] != NULL)
		{
			if (strcmp (mode, "loiter\0") == 0)
				m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx]->drawGL (centerPointPosition, estLatToMConv, estLonToMConv);
		}
	}
}

void BeliefCollection::updateRawDataDialog ()
{
	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_RawDataDisplayOpt >= 0)
	{
		//Update RawDataDialog rows with appropriate values from selected belief
		if (m_CurrentBeliefs[m_RawDataDisplayOpt] != NULL)
			m_CurrentBeliefs[m_RawDataDisplayOpt]->updateDataRows (m_DataRows);
		else
			clearDisplayRawData ();
	}
}



bool BeliefCollection::updateAgentPositionBelief (AgentPositionBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	AgentPositionBelief* currentBelief = (AgentPositionBelief*)m_CurrentBeliefs[m_AgentPositionBeliefIdx];
	if (currentBelief == NULL)
	{
		//If first belief, store it
		m_CurrentBeliefs[m_AgentPositionBeliefIdx] = newBelief;
	}
	else
	{
		//If not first belief, pull all positions from newBelief and add to the current belief
		list<AgentPosition*>* newPositions = newBelief->getModifiableAgentPositions();
		while (newPositions->size() > 0)
		{
			currentBelief->addAgentPosition (newPositions->front());
			newPositions->pop_front();
		}

		if (newBelief != NULL)
			delete newBelief;
	}

	return true;

}

bool BeliefCollection::updateCircularOrbitBelief (CircularOrbitBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	CircularOrbitBelief* currentBelief = (CircularOrbitBelief*)m_CurrentBeliefs[m_CircularOrbitBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_CircularOrbitBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateRacetrackOrbitBelief (RacetrackOrbitBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	RacetrackOrbitBelief* currentBelief = (RacetrackOrbitBelief*)m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateGCSPositionBelief (AgentPositionBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	AgentPositionBelief* currentBelief = (AgentPositionBelief*)m_CurrentBeliefs[m_GCSPositionBeliefIdx];
	if (currentBelief != NULL)
	{
		/*if (fabs(currentBelief->getAgentPositions()->front()->getLatDecDeg() - newBelief->getAgentPositions()->front()->getLatDecDeg()) < 0.0000001
			&& fabs(currentBelief->getAgentPositions()->front()->getLonDecDeg() - newBelief->getAgentPositions()->front()->getLonDecDeg()) < 0.0000001
			&& fabs(currentBelief->getAgentPositions()->front()->getAltMslM() - newBelief->getAgentPositions()->front()->getAltMslM()) < 1
			)
		{
			//New belief is same position as old belief, keep old belief??
			delete newBelief;
			return true;
		}*/
		delete currentBelief;
	}
		
	m_CurrentBeliefs[m_GCSPositionBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updatePiccoloTelemetryBelief(PiccoloTelemetryBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	PiccoloTelemetryBelief* currentBelief = (PiccoloTelemetryBelief*)m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx] = newBelief;
	
	if (m_DtedObject != NULL)
	{
		newBelief->setAltDtedAgl_m (newBelief->getAltitudeMSL_m() - m_DtedObject->getZ (newBelief->getLatitude_deg(), newBelief->getLongitude_deg()));
	}

	return true;
}

bool BeliefCollection::updateParticleDetectionBelief (ParticleDetectionBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	ParticleDetectionBelief* currentBelief = (ParticleDetectionBelief*)m_CurrentBeliefs[m_ParticleDetectionBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_ParticleDetectionBeliefIdx] = newBelief;

	
	if (m_CurrentBeliefs[m_CloudDetectionBeliefIdx] == NULL)
	{
		//Add Cloud Detection belief with no detections (garbage source value), so overlay is drawn immediately when sensor data is coming in
		CloudDetection* emptyCloudDet = new CloudDetection (0, 0, 0, 0, 0, 10000, 10000);
		this->updateCloudDetectionBelief (emptyCloudDet);
	}
	
	return true;
}

bool BeliefCollection::updateAnacondaDetectionBelief (AnacondaDetectionBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	AnacondaDetectionBelief* currentBelief = (AnacondaDetectionBelief*)m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx] = newBelief;
	
	if (m_CurrentBeliefs[m_CloudDetectionBeliefIdx] == NULL)
	{
		//Add Cloud Detection belief with no detections (garbage source value), so overlay is drawn immediately when sensor data is coming in
		CloudDetection* emptyCloudDet = new CloudDetection (0, 0, 0, 0, 0, 10000, 10000);
		this->updateCloudDetectionBelief (emptyCloudDet);
	}

	return true;
}

bool BeliefCollection::updateAgentModeBelief (AgentModeBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	AgentModeBelief* currentBelief = (AgentModeBelief*)m_CurrentBeliefs[m_AgentModeBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_AgentModeBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateCloudDetectionBelief (CloudDetection* newDetection)
{
	if (newDetection == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Add newDetection data to current CloudDetectionBelief
	CloudDetectionBelief* currentBelief = (CloudDetectionBelief*)m_CurrentBeliefs[m_CloudDetectionBeliefIdx];
	if (currentBelief == NULL)
	{
		m_CurrentBeliefs[m_CloudDetectionBeliefIdx] = new CloudDetectionBelief ();
	}

	((CloudDetectionBelief*)m_CurrentBeliefs[m_CloudDetectionBeliefIdx])->addCloudDetection (newDetection);


	//Also add newDetection data to current PropogatedCloudDetectionBelief
	CloudDetection* newDetectionCopy = new CloudDetection (newDetection->getMsgTimestampMs(), newDetection->getLatDecDeg(), newDetection->getLonDecDeg(), newDetection->getAltMslM(), newDetection->getValue(), newDetection->getSource(), newDetection->getId());
	PropogatingCloudDetectionBelief* currentPropBelief = (PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx];
	if (currentPropBelief == NULL)
	{
		m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx] = new PropogatingCloudDetectionBelief ();
	}

	((PropogatingCloudDetectionBelief*)m_CurrentBeliefs[m_PropogatedCloudDetectionBeliefIdx])->addCloudDetection (newDetectionCopy);
	
	return true;
}

bool BeliefCollection::updateCloudPredictionBelief (CloudPredictionBelief * newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	CloudPredictionBelief* currentBelief = (CloudPredictionBelief*)m_CurrentBeliefs[m_CloudPredictionBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_CloudPredictionBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateExplosionBelief (ExplosionBelief * newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	ExplosionBelief* currentBelief = (ExplosionBelief*)m_CurrentBeliefs[m_ExplosionBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_ExplosionBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateMetBelief (MetBelief * newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	MetBelief* currentBelief = (MetBelief*)m_CurrentBeliefs[m_MetBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_MetBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateIrExplosionAlgorithmEnabledBelief (IrExplosionAlgorithmEnabledBelief * newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	IrExplosionAlgorithmEnabledBelief* currentBelief = (IrExplosionAlgorithmEnabledBelief*)m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx] = newBelief;
	
	return true;
}

bool BeliefCollection::updateLoiterApproachPathBelief (LoiterApproachPathBelief* newBelief)
{
	if (newBelief == NULL)
		return false;

	LockObject lock (&m_CurrentBeliefsLock);
	
	//Replace current belief
	LoiterApproachPathBelief* currentBelief = (LoiterApproachPathBelief*)m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx];
	if (currentBelief != NULL)
		delete currentBelief;
		
	m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx] = newBelief;
	
	return true;
}

void BeliefCollection::displayAgentPositionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_AgentPositionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_AgentPositionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_AgentPositionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayGCSPositionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_GCSPositionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_GCSPositionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_GCSPositionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayCircularOrbitBeliefRawData () 
{
	m_RawDataDisplayOpt = m_CircularOrbitBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_CircularOrbitBeliefIdx] != NULL)
		m_CurrentBeliefs[m_CircularOrbitBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayRacetrackOrbitBeliefRawData () 
{
	m_RawDataDisplayOpt = m_RacetrackOrbitBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx] != NULL)
		m_CurrentBeliefs[m_RacetrackOrbitBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayPiccoloTelemetryBeliefRawData () 
{
	m_RawDataDisplayOpt = m_PiccoloTelemetryBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx] != NULL)
		m_CurrentBeliefs[m_PiccoloTelemetryBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayParticleDetectionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_ParticleDetectionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_ParticleDetectionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_ParticleDetectionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayAnacondaDetectionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_AnacondaDetectionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_AnacondaDetectionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayAgentModeBeliefRawData () 
{
	m_RawDataDisplayOpt = m_AgentModeBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_AgentModeBeliefIdx] != NULL)
		m_CurrentBeliefs[m_AgentModeBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayCloudDetectionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_CloudDetectionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_CloudDetectionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_CloudDetectionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayCloudPredictionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_CloudPredictionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_CloudPredictionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_CloudPredictionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayExplosionBeliefRawData () 
{
	m_RawDataDisplayOpt = m_ExplosionBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_ExplosionBeliefIdx] != NULL)
		m_CurrentBeliefs[m_ExplosionBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayMetBeliefRawData () 
{
	m_RawDataDisplayOpt = m_MetBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_MetBeliefIdx] != NULL)
		m_CurrentBeliefs[m_MetBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayIrExplosionAlgorithmEnabledBeliefRawData () 
{
	m_RawDataDisplayOpt = m_IrExplosionAlgorithmEnabledBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx] != NULL)
		m_CurrentBeliefs[m_IrExplosionAlgorithmEnabledBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::displayLoiterApproachPathBeliefRawData () 
{
	m_RawDataDisplayOpt = m_LoiterApproachPathBeliefIdx;

	LockObject lock (&m_CurrentBeliefsLock);
	if (m_DataRows != NULL && m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx] != NULL)
		m_CurrentBeliefs[m_LoiterApproachPathBeliefIdx]->updateDataRows (m_DataRows);
}

void BeliefCollection::clearDisplayRawData() 
{
	if (m_DataRows != NULL)
	{
		//Clear all RawDataDialog rows
		for (int i = 0; i < m_DataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (m_DataRows->m_hWnd, m_DataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (m_DataRows->m_hWnd, m_DataRows->m_DataRowValues[i]), "\0");
		}
	}
}


void* BeliefCollection_PropogationThreadProc(void *arg)
{
	PropogationThreadParams* params = (PropogationThreadParams*) arg;
	int propogationUpdatePeriod_ms = Config::getInstance()->getValueAsInt ("BeliefCollection.PropgationUpdatePeriodMs", 1000);
	int referenceLatDecDeg = Config::getInstance()->getValueAsDouble ("CameraStart.LatDecDeg", 40);
	
	bool inReplayMode = !Config::getInstance()->getValueAsBool ("SwarmSpy.DoSocketSpy", true);
	float replaySpeed = Config::getInstance()->getValueAsFloat ("SwarmSpy.ReplaySpeed", 50);

	
	clock_t startingClockTick = clock();

	while (!*(params->m_KillThread))
	{
		clock_t nowClock = clock();
		long millisSinceUpdate = (nowClock - startingClockTick)*1000.0/CLOCKS_PER_SEC;
		if (millisSinceUpdate > propogationUpdatePeriod_ms)
		{
			if (*(params->m_PausePropogation))
			{
			}
			else
			{
				LockObject lock (params->m_CurrentBeliefsLock);
				if ((params->m_CurrentBeliefs[params->m_PropogatedCloudDetectionBeliefIdx]) != NULL)
				{
					if (params->m_CurrentBeliefs[params->m_MetBeliefIdx] != NULL && ((MetBelief*)params->m_CurrentBeliefs[params->m_MetBeliefIdx])->getIsMetDefined())
					{
						if (inReplayMode)
							millisSinceUpdate *= replaySpeed;
		
						double windToDecDeg = fmod (180 + ((MetBelief*)params->m_CurrentBeliefs[params->m_MetBeliefIdx])->getWindBearingFromDeg(), 360);
						double windSpeedMps = ((MetBelief*)params->m_CurrentBeliefs[params->m_MetBeliefIdx])->getWindSpeedMps ();
						((PropogatingCloudDetectionBelief*)params->m_CurrentBeliefs[params->m_PropogatedCloudDetectionBeliefIdx])->propogateDetections (windToDecDeg, windSpeedMps, millisSinceUpdate, referenceLatDecDeg);
					}
				}
			}

			startingClockTick = nowClock;
		}

		Sleep (50);
	}

	*(params->m_ThreadDone) = true;
	return 0;
}