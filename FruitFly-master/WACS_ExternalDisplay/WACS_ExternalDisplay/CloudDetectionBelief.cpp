#include "stdafx.h"
#include "CloudDetectionBelief.h"
#include "Config.h"
#include "WinFonts.h"

CloudDetectionBelief::CloudDetectionBelief()
{	
	m_ChemicalDetections = new list <CloudDetection*>();
	m_ParticleDetections = new list <CloudDetection*>();

	m_ParticleDetectionColorRed = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ParticleDisplayColorRed", 0);
	m_ParticleDetectionColorGreen = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ParticleDisplayColorGreen", 0);
	m_ParticleDetectionColorBlue = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ParticleDisplayColorBlue", 1);
	m_ParticleDetectionColorAlpha = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ParticleDisplayColorAlpha", 0.6);
	m_CloudDetectionSizeM = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.DisplaySizeM", 25);

	m_ChemicalDetectionColorRed = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ChemicalDisplayColorRed", 1);
	m_ChemicalDetectionColorGreen = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ChemicalDisplayColorGreen", 1);
	m_ChemicalDetectionColorBlue = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ChemicalDisplayColorBlue", 0);
	m_ChemicalDetectionColorAlpha = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.ChemicalDisplayColorAlpha", 0.6);

	m_HeaderColorRed = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorRed", 0);
	m_HeaderColorGreen = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorGreen", 0);
	m_HeaderColorBlue = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorBlue", 0);
	m_HeaderColorAlpha = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorAlpha", 0.9);

	m_HeaderFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderFontSize", 18);
	m_HeaderHorizontalPixels = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.HeaderHorizontalPixels", 200);
	m_HeaderVerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.TextVerticalPixels", 100);

	m_RowFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.RowFontSize", 14);
	m_RowHorizontalPixels = Config::getInstance()->getValueAsDouble ("CloudDetectionBelief.RowHorizontalPixels", 200);
	m_Row1VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row1VerticalPixels", 70);
	m_Row2VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row2VerticalPixels", 40);
	
	
	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);

	displayList = -1;
}

CloudDetectionBelief::~CloudDetectionBelief()
{
	if (m_ChemicalDetections != NULL)
	{
		while (m_ChemicalDetections->size() > 0)
		{
			delete m_ChemicalDetections->front();
			m_ChemicalDetections->pop_front();
		}
		delete m_ChemicalDetections;
	}

	if (m_ParticleDetections != NULL)
	{
		while (m_ParticleDetections->size() > 0)
		{
			delete m_ParticleDetections->front();
			m_ParticleDetections->pop_front();
		}
		delete m_ParticleDetections;
	}

	gluDeleteQuadric(quadricObj);
}


void CloudDetectionBelief::addCloudDetection (CloudDetection* newDetection)
{
	if (newDetection->getSource() == CloudDetection::PARTICLE_DETECTION)
		addParticleDetection (newDetection);
	else if (newDetection->getSource() == CloudDetection::CHEMICAL_DETECTION)
		addChemicalDetection (newDetection);
}

void CloudDetectionBelief::addChemicalDetection (CloudDetection* newDetection)
{
	m_ChemicalDetections->push_back (newDetection);
	m_LastUpdatedSec = time(NULL);
	m_LastChemicalDetectionTime = m_LastUpdatedSec;
}

void CloudDetectionBelief::addParticleDetection (CloudDetection* newDetection)
{
	m_ParticleDetections->push_back (newDetection);
	m_LastUpdatedSec = time(NULL);
	m_LastParticleDetectionTime = m_LastUpdatedSec;
}

void CloudDetectionBelief::drawGlOverlay (int glWidth, int glHeight)
{
	//Write text
	glColor4f (m_HeaderColorRed, m_HeaderColorGreen, m_HeaderColorBlue, m_HeaderColorAlpha);
	WinFonts::glPrint2d (m_HeaderFontSize, m_HeaderHorizontalPixels, m_HeaderVerticalPixels, "WACS\0");

	glColor4f (m_ParticleDetectionColorRed, m_ParticleDetectionColorGreen, m_ParticleDetectionColorBlue, m_ParticleDetectionColorAlpha);
	char msg [256];
	sprintf_s (msg, 256, "# Particle Hits: %d\0", m_ParticleDetections->size());
	if (m_ParticleDetections->size() > 0)
		sprintf_s (&msg[strlen(msg)], 256-strlen(msg), " (%d sec ago)\0", (time(NULL) - m_LastParticleDetectionTime));
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row1VerticalPixels, msg);

	glColor4f (m_ChemicalDetectionColorRed, m_ChemicalDetectionColorGreen, m_ChemicalDetectionColorBlue, m_ChemicalDetectionColorAlpha);
	sprintf_s (msg, 256, "# Chemical Hits: %d\0", m_ChemicalDetections->size());
	if (m_ChemicalDetections->size() > 0)
		sprintf_s (&msg[strlen(msg)], 256-strlen(msg), " (%d sec ago)\0", (time(NULL) - m_LastChemicalDetectionTime));
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row2VerticalPixels, msg);
}

void CloudDetectionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition != NULL)
	{
		if (displayList < 0)
		{
			//First time, generate GL call list for spheres
			displayList = glGenLists(1);
			glNewList(displayList,GL_COMPILE);	
			gluSphere (quadricObj, m_CloudDetectionSizeM, 30, 30);
			glEndList();
		}

		glEnable (GL_LIGHTING);

		if (m_DisplayParticleDetections)
		{
			//Draw particle detections
			glColor4f (m_ParticleDetectionColorRed, m_ParticleDetectionColorGreen, m_ParticleDetectionColorBlue, m_ParticleDetectionColorAlpha);
			for( std::list<CloudDetection*>::iterator i(m_ParticleDetections->begin()), end(m_ParticleDetections->end()); i != end; ++i )
			{ 
				CloudDetection* nextPosition = (*i);

				if (nextPosition != NULL)
				{
					glPushMatrix();
					glTranslated (estLonToMConv*(nextPosition->getLonDecDeg() - centerPointPosition->getLonDecDeg()), estLatToMConv*(nextPosition->getLatDecDeg() - centerPointPosition->getLatDecDeg()), nextPosition->getAltMslM() - centerPointPosition->getAltMslM());
					glCallList (displayList);

					glPopMatrix();
				}
			}
		}

		if (m_DisplayChemicalDetections)
		{
			//Draw chemical detections
			glColor4f (m_ChemicalDetectionColorRed, m_ChemicalDetectionColorGreen, m_ChemicalDetectionColorBlue, m_ChemicalDetectionColorAlpha);
			for( std::list<CloudDetection*>::iterator i(m_ChemicalDetections->begin()), end(m_ChemicalDetections->end()); i != end; ++i )
			{ 
				CloudDetection* nextPosition = (*i);

				if (nextPosition != NULL)
				{
					glPushMatrix();
					glTranslated (estLonToMConv*(nextPosition->getLonDecDeg() - centerPointPosition->getLonDecDeg()), estLatToMConv*(nextPosition->getLatDecDeg() - centerPointPosition->getLatDecDeg()), nextPosition->getAltMslM() - centerPointPosition->getAltMslM());
					glCallList (displayList);

					glPopMatrix();
				}
			}
		}

		glDisable (GL_LIGHTING);

	}
}

void CloudDetectionBelief::updateDataRows (DataRows* dataRows)
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
	
		sprintf_s (text, sizeof(text), " %d\0", m_ChemicalDetections->size());
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Chem Hits Counts\0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_ParticleDetections->size());
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Particle Hits Counts\0");
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