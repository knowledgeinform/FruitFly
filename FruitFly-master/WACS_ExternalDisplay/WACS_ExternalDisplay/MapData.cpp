#include "stdafx.h"
#include "MapData.h"
#include "Config.h"

MapData::MapData ()
{
	m_DtedObject = NULL;
	m_MapObject = NULL;
	m_BkgndMapObject = NULL;

	m_BackgroundMapShiftMeters = Config::getInstance()->getValueAsDouble ("MapData.BackgroundMapShiftMeters", 50);
}

MapData::~MapData ()
{
	if (m_DtedObject != NULL)
		delete m_DtedObject;

	if (m_MapObject != NULL)
		delete m_MapObject;

	if (m_BkgndMapObject != NULL)
		delete m_BkgndMapObject;
}

void MapData::setDtedObject (DTEDObject* dted)
{
	if (m_DtedObject != NULL)
		delete m_DtedObject;

	m_DtedObject = dted;
}

void MapData::loadMap(char *mapFilename, AgentPosition* centerPoint)
{
	if (m_DtedObject == NULL)
		return;
	if (centerPoint == NULL)
		return;
	if (m_MapObject != NULL)
		delete m_MapObject;
	
	m_MapObject = new Geomap3D ();
	bool abort = false;
	m_MapObject->importImage (mapFilename, &abort, m_DtedObject, centerPoint);
}

void MapData::loadBackground(char *mapFilename, AgentPosition* centerPoint)
{
	if (m_DtedObject == NULL)
		return;
	if (centerPoint == NULL)
		return;
	if (m_BkgndMapObject != NULL)
		delete m_BkgndMapObject;
	
	m_BkgndMapObject = new Geomap3D ();
	bool abort = false;

	//don't paint where regular map is.  Skip over block for main map image.
	m_BkgndMapObject->importImage (mapFilename, &abort, m_DtedObject, centerPoint);
}

void MapData::paintMap(AgentPosition* centerPoint, double estLatToMConv, double estLonToMConv)
{
	if (m_BkgndMapObject != NULL)
	{
		//Shift background down
		glTranslated (0, 0, -m_BackgroundMapShiftMeters);
		m_BkgndMapObject->draw (centerPoint, estLatToMConv, estLonToMConv);
		glTranslated (0, 0, m_BackgroundMapShiftMeters);
	}

	if (m_MapObject != NULL)
	{
		m_MapObject->draw (centerPoint, estLatToMConv, estLonToMConv);
	}
}

double MapData::getMinZ (double lat, double lon)
{
	if (m_DtedObject == NULL)
		return 0;

	return m_DtedObject->getMinZ (lat, lon);
}

double MapData::getTerrainZM (double lat, double lon)
{
	if (m_DtedObject == NULL)
		return 0;

	return m_DtedObject->getZ (lat, lon);
}