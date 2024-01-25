#include "stdafx.h"
#include "PiccoloTelemetryBelief.h"
#include "stdio.h"
#include "math.h"
#include "Config.h"
#include "WinFonts.h"

UavDisplayBase* PiccoloTelemetryBelief::m_UavDisplay = NULL;


PiccoloTelemetryBelief::PiccoloTelemetryBelief ()
{
	readConfig ();
}

PiccoloTelemetryBelief::PiccoloTelemetryBelief (double altitudeWGS84_m, double altitudeMSL_m, int GpsStatus, double indicatedAirSpeed_mps, double latitude_deg, double longitude_deg, double gpsPDOP, double pitch_deg, double roll_deg, double trueHeading_deg, double velocityDown_mps, double velocityEast_mps, double velocityNorth_mps, double windFromSouth_mps, double windFromWest_mps, double yaw_deg, double staticPressure_pa, double outsideAirTemperature_C, double altLaser_m, bool altLaserValid)
{
	setAltitudeWGS84_m (altitudeWGS84_m);
	setAltitudeMSL_m (altitudeMSL_m);
	setGpsStatus(GpsStatus);
	setIndicatedAirSpeed_mps(indicatedAirSpeed_mps);
	setLatitude_deg(latitude_deg);
	setLongitude_deg(longitude_deg);
	setGpsPDOP(gpsPDOP);
	setPitch_deg(pitch_deg);
	setRoll_deg(roll_deg);
	setTrueHeading_deg(trueHeading_deg);
	setVelocityDown_mps(velocityDown_mps);
	setVelocityEast_mps(velocityEast_mps);
	setVelocityNorth_mps(velocityNorth_mps);
	setWindFromSouth_mps(windFromSouth_mps);
	setWindFromWest_mps(windFromWest_mps);
	setYaw_deg(yaw_deg);
	setStaticPressure_pa(staticPressure_pa);
	setOutsideAirTemperature_C(outsideAirTemperature_C);
	setAltLaser_m(altLaser_m);
	setAltLaserValid(altLaserValid);

	readConfig ();
}

PiccoloTelemetryBelief::~PiccoloTelemetryBelief ()
{

}

void PiccoloTelemetryBelief::readConfig ()
{
	string uavName;
	Config::getInstance()->getValue ("UavDisplay.UavName", uavName, "Shadow");
	if (uavName.compare ("Dakota") == 0)
		m_UavType = UavDisplayBase::DAKOTA;
	else //if (uavName.compare ("Shadow") == 0)
		m_UavType = UavDisplayBase::SHADOW;

	m_HeaderColorRed = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorRed", 0);
	m_HeaderColorGreen = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorGreen", 0);
	m_HeaderColorBlue = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorBlue", 0);
	m_HeaderColorAlpha = Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderColorAlpha", 0.9);

	m_HeaderFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.HeaderFontSize", 18);
	m_HeaderHorizontalPixels = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.HeaderHorizontalPixels", 20);
	m_HeaderVerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.TextVerticalPixels", 100);

	m_TextColorRed = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.TextColorRed", 1);
	m_TextColorGreen = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.TextColorGreen", 1);
	m_TextColorBlue = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.TextColorBlue", 1);
	m_TextColorAlpha = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.TextColorAlpha", 0.8);

	m_RowFontSize = 1000*Config::getInstance()->getValueAsDouble ("SensorOverlays.RowFontSize", 14);
	m_RowHorizontalPixels = Config::getInstance()->getValueAsDouble ("PiccoloTelemetryBelief.RowHorizontalPixels", 20);
	m_Row1VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row1VerticalPixels", 70);
	m_Row2VerticalPixels = Config::getInstance()->getValueAsDouble ("SensorOverlays.Row2VerticalPixels", 40);
}

void PiccoloTelemetryBelief::drawGlOverlay (int glWidth, int glHeight)
{
	//Write text
	glColor4f (m_HeaderColorRed, m_HeaderColorGreen, m_HeaderColorBlue, m_HeaderColorAlpha);
	WinFonts::glPrint2d (m_HeaderFontSize, m_HeaderHorizontalPixels, m_HeaderVerticalPixels, "UAV\0");

	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
	char msg [256];
	sprintf_s (msg, 256, "Alt MSL: %.1f %s\0", m_AltitudeMSL_m/(m_ConvertAltitudeToFeet?0.3048:1), m_ConvertAltitudeToFeet?"ft":"m");
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row1VerticalPixels, msg);
	
	glColor4f (m_TextColorRed, m_TextColorGreen, m_TextColorBlue, m_TextColorAlpha);
	sprintf_s (msg, 256, "Alt AGL: %.1f %s %s\0", ((m_AltLaserValid?m_AltLaser_m:m_AltDtedAgl_m))/(m_ConvertAltitudeToFeet?0.3048:1), m_ConvertAltitudeToFeet?"ft":"m", (m_AltLaserValid?"(LASER)":""));
	WinFonts::glPrint2d (m_RowFontSize, m_RowHorizontalPixels, m_Row2VerticalPixels, msg);
}

void PiccoloTelemetryBelief::drawGL(AgentPosition* centerPointPosition, double estLatToMConv, double estLonToMConv)
{
	if (centerPointPosition != NULL)
	{
		glPushMatrix();
		glTranslated(  (estLonToMConv*(getLongitude_deg() - centerPointPosition->getLonDecDeg())), estLatToMConv*((getLatitude_deg() - centerPointPosition->getLatDecDeg())), (getAltitudeMSL_m() - centerPointPosition->getAltMslM()) );
		glRotatef (-getYaw_deg(), 0, 0, 1);
		glRotatef (getPitch_deg(), 1, 0, 0);
		glRotatef (getRoll_deg(), 0, 1, 0);

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

void PiccoloTelemetryBelief::updateDataRows (DataRows* dataRows)
{
	if (dataRows != NULL)
	{
		int idx = 0;
		char text[256];

		int secsSinceUpdate = time(NULL) - m_LastUpdatedSec;
		sprintf_s (text, sizeof(text), " %d seconds\0", secsSinceUpdate);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Time Since Update: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
	
		sprintf_s (text, sizeof(text), " %.1f m\0", m_AltitudeWGS84_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Altitude WGS84 \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m\0", m_AltitudeMSL_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Altitude MSL \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %d\0", m_GpsStatus);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "GPS Status \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f m/s\0", m_IndicatedAirSpeed_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "IAS \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f %c\0", fabs(m_Latitude_deg), (m_Latitude_deg>0?'N':'S'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Latitude: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.6f %c\0", fabs(m_Longitude_deg), (m_Longitude_deg>0?'E':'W'));
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Longitude: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
			
		sprintf_s (text, sizeof(text), " %.1f\0", m_GpsPDOP);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "PDOP: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);

		sprintf_s (text, sizeof(text), " %.2f deg\0", m_Pitch_deg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Pitch: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f deg\0", m_Roll_deg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Roll: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f deg\0", m_Yaw_deg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Yaw: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f deg\0", m_TrueHeading_deg);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Heading: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m/s\0", m_VelocityDown_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Vel Down: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m/s\0", m_VelocityEast_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Vel East: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m/s\0", m_VelocityNorth_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Vel North: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m/s\0", m_WindFromSouth_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Wind From South: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m/s\0", m_WindFromWest_mps);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Wind From West: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f Pa\0", m_StaticPressure_pa);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Static Pressure: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.1f C\0", m_OutsideAirTemperature_C);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "OAT: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %s\0", m_AltLaserValid?"TRUE":"FALSE");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Laser AGL Valid?: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		sprintf_s (text, sizeof(text), " %.2f m\0", m_AltLaser_m);
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[idx]), "Laser AGL: \0");
		SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[idx++]), text);
		
		for (int i = idx; i < dataRows->m_DataRowsCount; i ++)
		{
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowLabels[i]), "\0");
			SetWindowText (GetDlgItem (dataRows->m_hWnd, dataRows->m_DataRowValues[i]), "\0");
		}
	}

	return;
}
