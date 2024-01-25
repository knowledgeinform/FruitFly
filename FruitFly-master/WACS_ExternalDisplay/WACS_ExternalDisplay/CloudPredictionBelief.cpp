#include "stdafx.h"
#include "CloudPredictionBelief.h"
#include "stdio.h"
#include "Config.h"

GLUquadricObj *CloudPredictionBelief::quadricObj = NULL; //!< GL Quadric object to use for displaying cloud location spheres
int CloudPredictionBelief::displayList = -1; //!< GL display list to display cloud location spheres


CloudPredictionBelief::CloudPredictionBelief()
{

	readConfig();
}

CloudPredictionBelief::CloudPredictionBelief(double matrixBottomNWCornerPosition_LatDeg,
								double matrixBottomNWCornerPosition_LonDeg,
								double matrixBottomNWCornerAltitudeMSL_m,
								double matrixTopSECornerPosition_LatDeg,
								double matrixTopSECornerPosition_LonDeg,
								double matrixTopSECornerAltitudeMSL_m,
								int matrixSizeX_Cells,
								int matrixSizeY_Cells,
								double matrixCellHorizontalSideLength_m,
								double cloudMinAltitudeMsl_m,
								double cloudMaxAltitudeMsl_m,
								double predictedCloudCenterPosition_LatDeg,
								double predictedCloudCenterPosition_LonDeg,
								double predictedCloudCenterAltitudeMSL_m,
								double predictedCloudInterceptPosition_LatDeg,
								double predictedCloudInterceptPosition_LonDeg,
								double predictedCloudInterceptAltitudeMSL_m
								)
{
	setMatrixBottomNWCornerPosition_LatDeg (matrixBottomNWCornerPosition_LatDeg);
	setMatrixBottomNWCornerPosition_LonDeg (matrixBottomNWCornerPosition_LonDeg);
	setMatrixBottomNWCornerAltitudeMSL_m(matrixBottomNWCornerAltitudeMSL_m);
	setMatrixTopSECornerPosition_LatDeg (matrixTopSECornerPosition_LatDeg);
	setMatrixTopSECornerPosition_LonDeg (matrixTopSECornerPosition_LonDeg);
	setMatrixTopSECornerAltitudeMSL_m(matrixTopSECornerAltitudeMSL_m);
	setMatrixSizeX_Cell (matrixSizeX_Cells);
	setMatrixSizeY_Cells (matrixSizeY_Cells);
	setMatrixCellHorizontalSideLength_m (matrixCellHorizontalSideLength_m);
	setCloudMinAltitudeMsl_m (cloudMinAltitudeMsl_m);
	setCloudMaxAltitudeMsl_m (cloudMaxAltitudeMsl_m);
	setPredictedCloudCenterPosition_LatDeg (predictedCloudCenterPosition_LatDeg);
	setPredictedCloudCenterPosition_LonDeg (predictedCloudCenterPosition_LonDeg);
	setPredictedCloudCenterAltitudeMSL_m (predictedCloudCenterAltitudeMSL_m);
	setPredictedCloudInterceptPosition_LatDeg (predictedCloudInterceptPosition_LatDeg);
	setPredictedCloudInterceptPosition_LonDeg (predictedCloudInterceptPosition_LonDeg);
	setPredictedCloudInterceptAltitudeMSL_m (predictedCloudInterceptAltitudeMSL_m);	

	readConfig();
}

CloudPredictionBelief::~CloudPredictionBelief()
{
}

void CloudPredictionBelief::readConfig()
{
	m_CloudLocationColorRed = Config::getInstance()->getValueAsDouble ("CloudPredictionBelief.DisplayColorRed", 0.4);
	m_CloudLocationColorGreen = Config::getInstance()->getValueAsDouble ("CloudPredictionBelief.DisplayColorGreen", 0.4);
	m_CloudLocationColorBlue = Config::getInstance()->getValueAsDouble ("CloudPredictionBelief.DisplayColorBlue", 1);
	m_CloudLocationColorAlpha = Config::getInstance()->getValueAsDouble ("CloudPredictionBelief.DisplayColorAlpha", 0.4);
	m_CloudLocationSizeM = Config::getInstance()->getValueAsDouble ("CloudPredictionBelief.DisplaySizeM", 100);
}

void CloudPredictionBelief::initQuadric()
{
	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);
	displayList = -1;
}

void CloudPredictionBelief::destroyGlObjects ()
{
	if (displayList >= 0)
	{
		gluDeleteQuadric(quadricObj);
		displayList = -1;
	}
}

void CloudPredictionBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition != NULL)
	{
		if (displayList < 0)
		{
			initQuadric();

			//First time, generate GL call list for spheres
			displayList = glGenLists(1);
			glNewList(displayList,GL_COMPILE);	
			gluSphere (quadricObj, m_CloudLocationSizeM, 30, 30);
			glEndList();
		}

		glEnable (GL_LIGHTING);

		//Draw current cloud center location 
		glColor4f (m_CloudLocationColorRed, m_CloudLocationColorGreen, m_CloudLocationColorBlue, m_CloudLocationColorAlpha);
		
		glPushMatrix();
		glTranslated (estLonToMConv*(m_PredictedCloudCenterPosition_LonDeg - centerPointPosition->getLonDecDeg()), estLatToMConv*(m_PredictedCloudCenterPosition_LatDeg - centerPointPosition->getLatDecDeg()), m_PredictedCloudCenterAltitudeMSL_m - centerPointPosition->getAltMslM());
		glCallList (displayList);

		glPopMatrix();

		glDisable (GL_LIGHTING);

	}
}
		
void CloudPredictionBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_MatrixBottomNWCornerPosition_LatDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Max Lat \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_MatrixBottomNWCornerPosition_LonDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Min Lon \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_MatrixBottomNWCornerAltitudeMSL_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Min Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_MatrixTopSECornerPosition_LatDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Min Lat \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_MatrixTopSECornerPosition_LonDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Max Lon \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_MatrixTopSECornerAltitudeMSL_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Max Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_MatrixSizeX_Cells);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix X Cells \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_MatrixSizeY_Cells);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Y Cells \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_MatrixCellHorizontalSideLength_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Matrix Cell Length \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_CloudMinAltitudeMsl_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Min Cloud Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.1f m\0", m_CloudMaxAltitudeMsl_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Max Cloud Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_PredictedCloudCenterPosition_LatDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Est Cloud Center Lat  \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_PredictedCloudCenterPosition_LonDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Est Cloud Center Lon \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_PredictedCloudCenterAltitudeMSL_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Est Cloud Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.6f deg\0", m_PredictedCloudInterceptPosition_LatDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Intercept Pos Lat  \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f deg\0", m_PredictedCloudInterceptPosition_LonDeg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Intercept Pos Lon \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_PredictedCloudInterceptAltitudeMSL_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Intercept Alt MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}

