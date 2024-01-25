#include "stdafx.h"
#include "Geomap3D.h"
#include "Constants.h"
#include <math.h>
#include <string>
#include <sys/stat.h>
#include "Logger.h"
#include "Config.h"
using namespace std;

#include "include\gdal\gdal_priv.h"
#pragma comment(lib, "lib/gdal/gdal_i.lib")


Geomap3D::Geomap3D() {
	registered = false;
	
	mytextype = 0;
	
	img_orig_width = 0.0;
	img_orig_height = 0.0;
	m_DtedData = NULL;

	numXTex = 0;
	numYTex = 0;
	terrainList = NULL;

	myTextures = NULL;

	m_MinLon = 0.0;
	m_MaxLat = 0.0;
	percComplete = 0;
	imageData = NULL;
	imageDepth = 0;
	imageX = 0;
	imageY = 0;

	desiredImageBytes = Config::getInstance()->getValueAsInt ("Geomap3D.ImageBytes", 1);
	smoothShading = Config::getInstance()->getValueAsBool ("MosaicBase.SmoothShading", false);

	m_MaxTextureSizeW = Config::getInstance()->getValueAsInt ("Geomap3D.MaxTextureSize", 8192);
	m_MaxTextureSizeH = Config::getInstance()->getValueAsInt ("Geomap3D.MaxTextureSize", 8192);
}

Geomap3D::~Geomap3D() 
{
	freeImageData ();
	if (myTextures != NULL)
	{
		for (int i = 0; i < numXTex; i ++)
		{
			for (int j = 0; j < numYTex; j ++)
			{
				glDeleteTextures(1, &(myTextures[i][j].texName));
			}
			delete [] myTextures[i];
		}
		delete []myTextures;
	}

	if (terrainList != NULL)
		delete [] terrainList;
}

void Geomap3D::freeImageData ()
{
	if (imageData != NULL)
	{
		for (int i = 0; i < imageDepth; i ++)
		{
			if (imageData[i] != NULL)
				delete [] imageData[i];
		}
		delete []imageData;
		imageData = NULL;
	}
}
	
void Geomap3D::setPercComplete (int newPerc)
{
	percComplete = newPerc;
}

unsigned char** Geomap3D::getImageData (int& retImageDepth, int& retImageX, int& retImageY)
{
	retImageDepth = imageDepth;
	retImageX = imageX;
	retImageY = imageY;

	return imageData;
}
		
bool Geomap3D::importImage(const char *imagefilename, bool* abortFlag, DTEDObject* dtedData, AgentPosition* centerPoint) 
{
	m_DtedData = dtedData;

	if (*abortFlag) return BADFORMAT;
	setPercComplete (0);
	char logBuf [256];
	sprintf_s(logBuf, 256, "Geomap3D: Generating image: %s\0", imagefilename);
	Logger::getInstance()->logMessage (logBuf);

	registered = false;

	//open image in gdal
	GDALDataset  *poDataset;
    GDALAllRegister();
	if (*abortFlag) return BADFORMAT;
	setPercComplete (3);

    poDataset = (GDALDataset *) GDALOpen( imagefilename, GA_ReadOnly );
    if( poDataset == NULL )
    {
        char text [256];
		sprintf_s (text, 256, "Geomap3D: Unable to open image using GDAL %s\0", imagefilename);
		Logger::getInstance()->logMessage (text);

		char errText [256];
		CPLErr errClass = CE_None;
		int errNum = 0;
		CPLError (errClass, errNum, errText);
		if (errClass == CE_Warning)
		{
			sprintf_s (text, 256, "Geomap3D: CE_Warning, code %d\0", errNum);
		}
		else if (errClass == CE_Failure)
		{
			sprintf_s (text, 256, "Geomap3D: CE_Failure, code %d\0", errNum);
		}
		else if (errClass == CE_Fatal)
		{
			sprintf_s (text, 256, "Geomap3D: CE_Fatal, code %d\0", errNum);
		}
		else
		{
			sprintf_s (text, 256, "Geomap3D: Unknown error, code %d\0", errNum);
		}
		Logger::getInstance()->logMessage (text);

		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
    }

	
	double        adfGeoTransform[6];
	if( poDataset->GetGeoTransform( adfGeoTransform ) != CE_None )
    {
		char text [256];
		sprintf_s (text, 256, "Geomap3D: Unable to georeference image using GDAL %s\0", imagefilename);
		Logger::getInstance()->logMessage (text);

		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}	
	
	if (*abortFlag) 
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (5);
	
	unsigned int imheight = poDataset->GetRasterYSize();
	unsigned int imwidth = poDataset->GetRasterXSize();
	
	//Setup image format
	GLenum imageFormat;
	GLenum imageType;
	GDALDataType gdalType;
	
	//Check if we are looking for the same number of image bytes as provided in the file
	int loadedImageBytes = poDataset->GetRasterCount();
	if (loadedImageBytes != desiredImageBytes)
	{
		int newBytes;
		if (desiredImageBytes == 3 && loadedImageBytes > desiredImageBytes)
			newBytes = 3;
		else
			newBytes = 1;

		char logBuf [256];
		sprintf_s(logBuf, 256, "Geomap3D: Found %d raster bands but looking for %d.  Simplifying to %d raster bands: %s\0", loadedImageBytes, desiredImageBytes, newBytes, imagefilename);
		Logger::getInstance()->logMessage (logBuf);

		//Ignore others, just use known raster bands
		desiredImageBytes = newBytes;
	}

	if (desiredImageBytes == 3)
	{
		imageFormat = GL_RGB;
		imageType = GL_UNSIGNED_BYTE;
		gdalType = GDT_Byte;
	}
	else
	{
		desiredImageBytes = 1;

		imageFormat = GL_LUMINANCE;
		imageType = GL_UNSIGNED_BYTE;
		gdalType = GDT_Byte;
	}


	if (*abortFlag)
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (7);

	
	strcpy_s( filename, 10000, imagefilename );
	_splitpath_s ( filename, drive, 10, dir, 100, fname, 10000, ext, 10);

	img_orig_width  = imwidth;
	img_orig_height = imheight;

	GLint texwidth = 0;

	int tw = imwidth;
	int th = imheight;

	
	//Determine the maximum texture size OpenGL will let us use per tile.  Store in tw and th
	mytextype = GL_TEXTURE_2D;
	//glEnable(mytextype);					// Enable Texture Mapping

	tw = 1 << (int) ceil(log((double)imwidth) * INVLN2);
	th = 1 << (int) ceil(log((double)imheight) * INVLN2);

	if (tw > m_MaxTextureSizeW)
		tw = m_MaxTextureSizeW;
	if (th > m_MaxTextureSizeH)
		th = m_MaxTextureSizeH;

	glTexImage2D(GL_PROXY_TEXTURE_2D, 0, imageFormat, tw+2, th+2, 1, imageFormat, imageType, NULL);
	glGetTexLevelParameteriv(GL_PROXY_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, &texwidth);

	int count = 0;
	while (texwidth == 0) {
		if (tw > th) tw /= 2; else th /= 2;
		glTexImage2D(GL_PROXY_TEXTURE_2D, 0, imageFormat, tw+2, th+2, 1, imageFormat, imageType, NULL);
		glGetTexLevelParameteriv(GL_PROXY_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, &texwidth);

		if (count ++ > 100)
		{
			char logBuf [256];
			sprintf_s(logBuf, 256, "Geomap3D: Stuck in a loop trying to determine OpengGL texture sizes for mosaic: %s\0", imagefilename);
			Logger::getInstance()->immediateLogMessage (logBuf);

			if (poDataset != NULL)
				GDALClose (poDataset);
			return BADFORMAT;
		}
	}

	if (*abortFlag) 
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (10);

	unsigned char *texData = NULL;
	unsigned char *pafScanline = NULL;
	int   nXSize = 0;
	int   nYSize = 0;
	GDALRasterBand  *poBand;

	imageData = new unsigned char* [desiredImageBytes];
	for (int i = 0; i < desiredImageBytes; i ++)
		imageData[i] = NULL;
	imageDepth = desiredImageBytes;

	for (int i = 1; i <= desiredImageBytes; i ++)
	{
		//Get raster data from image.  
		poBand = poDataset->GetRasterBand( i );

		nXSize = poBand->GetXSize();
		nYSize = poBand->GetYSize();
		imageX = nXSize;
		imageY = nYSize;

		if (pafScanline == NULL)
			pafScanline = new unsigned char [nXSize*nYSize];
		if (texData == NULL)		
			texData = (unsigned char *) malloc(desiredImageBytes*nXSize*nYSize);
		
		if (texData == NULL || pafScanline == NULL)
		{
			char text [256];
			sprintf_s (text, 256, "Geomap3D: Memory overload - could not retrieve memory from full-scale image: %s\0", imagefilename);
			Logger::getInstance()->immediateLogMessage (text);
			
			throw exception (text);
		}

		
		if (poBand->RasterIO( GF_Read, 0, 0, nXSize, nYSize, 
						pafScanline, nXSize, nYSize, gdalType, 
						0, 0 ))
		{
			char logBuf [256];
			sprintf_s(logBuf, 256, "Geomap3D: Error getting rasterIO raster #%d and size: %dx%d\0", i, nXSize, nYSize);
			Logger::getInstance()->immediateLogMessage (logBuf);

			if (poDataset != NULL)
				GDALClose (poDataset);

			return BADFORMAT;
		}

		//Extract data pixels from image
		for (int j = 0; j < nXSize; j ++)
		{
			for (int k = 0; k < nYSize; k ++)
			{
				texData[ (k*nXSize + j)*desiredImageBytes + (i-1) ] = pafScanline [k*nXSize + j];
			}

			if (*abortFlag)
			{
				if (poDataset != NULL)
					GDALClose (poDataset);
				return BADFORMAT;
			}
			int newPerc = (40.0f/desiredImageBytes * j/nXSize) + 10 +   (40.0f*(i-1)/desiredImageBytes) ;
			if (newPerc != getPercentComplete())
				setPercComplete (newPerc);
		}

		imageData[i-1] = pafScanline;
		pafScanline = NULL;
	}


	if (*abortFlag) 
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (50);

	//At this point, assume texData is single array with rgb interleaved for pixels, unless something didn't match up.  In that case,
	//it's just luminance.
	
	//Generate 2D array of textures to create the entire mosaic
	numXTex = ceil (img_orig_width / (tw));
	numYTex = ceil (img_orig_height / (th));

	
	
	myTextures = new textureTile* [numXTex];
	for (int i = 0; i < numXTex; i ++)
	{
		myTextures[i] = new textureTile [numYTex];
		for (int j = 0; j < numYTex; j++)
		{
			glGenTextures(1, &(myTextures[i][j].texName));
		}
	}
	

	
	//Bind textures for each tile of the original image
	int tempTexDataSize = (tw+2)*(th+2)*desiredImageBytes;
	int texDataSize = nXSize*nYSize*desiredImageBytes;
	unsigned char* tempTexData = new unsigned char [tempTexDataSize];
	int startSrcX, startSrcY;
	int startDstX, startDstY;
	int lastSrcX, lastSrcY;
	int lastDstX, lastDstY;
	for (int i = 0; i < numXTex; i ++)
	{
		for (int j = 0; j < numYTex; j ++)
		{
			if (*abortFlag)
			{
				if (poDataset != NULL)
				GDALClose (poDataset);
				return BADFORMAT;
			}
			int newPerc = (40 * (j + i*numYTex) / ((float)numYTex * (numXTex+1)) + 50);
			if (newPerc != getPercentComplete())
				setPercComplete (newPerc);

			if (i > 0)
			{
				startSrcX = i*tw - 1;
				startDstX = 0;
				myTextures[i][j].startXperc = (startSrcX+1)/(img_orig_width);
			}
			else
			{
				startSrcX = 0; //Can't get border to left of tile for first column
				startDstX = 1;
				myTextures[i][j].startXperc = 0;
			}
			
			if (j > 0)
			{	
				//startSrcY = j*th - border;
				startSrcY = j*th - 1;
				startDstY = 0;
				myTextures[i][j].startYperc = (startSrcY+1)/(img_orig_height);
			}
			else
			{
				startSrcY = 0; //Can't get border above tile for first row
				startDstY = 1; 
				myTextures[i][j].startYperc = 0;
			}

			if (i < numXTex - 1)
			{

				lastSrcX = (i+1)*tw;
				lastDstX = tw + 1;
				myTextures[i][j].endXperc = (lastSrcX)/(img_orig_width);
				myTextures[i][j].texCoordRight = (tw+2.0)/(tw+2.0f);
			}
			else
			{
				lastSrcX = img_orig_width - 1;	//Can't get border to right of last column
				lastDstX = img_orig_width - (i*tw);
				myTextures[i][j].endXperc = 1;
				myTextures[i][j].texCoordRight = (lastDstX+1)/(tw+2.0f);
			}
			
			if (j < numYTex - 1)
			{
				lastSrcY = (j+1)*th;
				lastDstY = th + 1;
				myTextures[i][j].endYperc = (lastSrcY)/(img_orig_height);
				myTextures[i][j].texCoordBottom = (th+2.0)/(th+2.0f);
			}
			else
			{
				lastSrcY = img_orig_height - 1;  //Can't get border below last row
				lastDstY = img_orig_height - (j*th);
				myTextures[i][j].endYperc = 1;
				myTextures[i][j].texCoordBottom = (lastDstY+1)/(th+2.0f);
			}


			memset (tempTexData, 0, tempTexDataSize);

			myTextures[i][j].texCoordLeft = 0/(tw+2.0f);
			myTextures[i][j].texCoordTop =  0/(th+2.0f);
			
			//Do data copy
			for (int k = startDstY,l = startSrcY; k <= lastDstY; k ++, l ++)
			{
				int startPositionDst = (k)/*row of tile*/  *   /*width of tile*/(tw+2)*desiredImageBytes   +   /*offset from edge*/startDstX*desiredImageBytes;
				int startPositionSrc = (l)/*row of image*/ *  /*width of image*/img_orig_width*desiredImageBytes  +   /*offset from edge*/startSrcX*desiredImageBytes;
				int sizeCopy = (lastSrcX - startSrcX + 1)*desiredImageBytes;
				int sizeCheck = (lastDstX - startDstX + 1)*desiredImageBytes;

				if (sizeCopy != sizeCheck)
				{
					char text [100];
					sprintf_s (text, 100, "Geomap3D: Copy size mismatch: %d %d", sizeCopy, sizeCheck);
					string msg (text);
					Logger::getInstance()->logMessage (msg);
					continue;
				}
				else if (sizeCopy < 0)
					continue;

				if (startPositionDst + sizeCopy > tempTexDataSize || startPositionSrc + sizeCopy > texDataSize)
				{
					char logBuf [256];
					sprintf_s(logBuf, 256, "Geomap3D: Exception copying data for texture tiles: startDst=%d,startSrc=%d,size=%d,arraySizeDst=%d,arraySizeSrc=%d\0", startPositionDst, startPositionSrc, sizeCopy, tempTexDataSize, texDataSize);
					Logger::getInstance()->logMessage (logBuf);

					continue;
				}
				memcpy (&tempTexData[startPositionDst], &texData[startPositionSrc], sizeCopy);					

			}

			

			//Bind
			glBindTexture (mytextype, myTextures[i][j].texName);
			glPixelStorei (GL_UNPACK_ALIGNMENT, 1);
			if (smoothShading)
				glTexParameteri (mytextype, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			else
				glTexParameteri (mytextype, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			
			glTexParameteri (mytextype, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri (mytextype, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri (mytextype, GL_TEXTURE_WRAP_T, GL_CLAMP);
			glTexEnvf (GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
			glTexImage2D (mytextype, 0, imageFormat, tw+2, th+2, 1, imageFormat, imageType, tempTexData);
			glBindTexture (mytextype, 0);

		}
	}

	glBindTexture (mytextype, 0);
	delete []tempTexData;
	free (texData);


	if (*abortFlag)
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (90);

	pafScanline = NULL;
	
	if (*abortFlag)
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (95);



	//Register image
	resWMeters = adfGeoTransform[1];
	widthMeters = resWMeters * (img_orig_width);

	resHMeters = -adfGeoTransform[5];
	heightMeters = resHMeters * (img_orig_height);

	m_MinLon = adfGeoTransform[0] - resWMeters/2;

	m_MaxLat = adfGeoTransform[3] + resHMeters/2;

	ulX = m_MinLon;
	ulY = m_MaxLat;
	urX = adfGeoTransform[0] + widthMeters;
	urY = m_MaxLat;
	llX = m_MinLon;
	llY = adfGeoTransform[3] - heightMeters;
	lrX = adfGeoTransform[0] + widthMeters;
	lrY = adfGeoTransform[3] - heightMeters;
	

	for (int i = 0; i < numXTex; i ++)
	{
		for (int j = 0; j < numYTex; j ++)
		{
			myTextures[i][j].minLon = m_MinLon + widthMeters*myTextures[i][j].startXperc;
			myTextures[i][j].maxLon = m_MinLon + widthMeters*myTextures[i][j].endXperc;
			myTextures[i][j].maxLat = m_MaxLat - heightMeters*myTextures[i][j].startYperc;
			myTextures[i][j].minLat = m_MaxLat - heightMeters*myTextures[i][j].endYperc;
		}
	}

	registered = true;

	if (*abortFlag)
	{
		if (poDataset != NULL)
			GDALClose (poDataset);
		return BADFORMAT;
	}
	setPercComplete (99);

	if (poDataset != NULL)
		GDALClose (poDataset);


	setPercComplete (100);

	sprintf_s(logBuf, 256, "Geomap3D: Generating image complete: %s\0", imagefilename);
	Logger::getInstance()->logMessage (logBuf);

	return SUCCESS;
}

void Geomap3D::draw(AgentPosition* centerPoint, double estLatToMConv, double estLonToMConv) 
{	
	if (!registered) return;
	if (!myTextures) return;
	
	if (!glActiveTexture) {
		glActiveTexture = (PFNGLACTIVETEXTUREPROC)GLH_EXT_GET_PROC_ADDRESS("glActiveTexture");
	}
	if (!glMultiTexCoord2d) {
		glMultiTexCoord2d = (PFNGLMULTITEXCOORD2DPROC)GLH_EXT_GET_PROC_ADDRESS("glMultiTexCoord2d");
	}

	if (glActiveTexture) {
		glActiveTexture(GL_TEXTURE0);
	}
	
	glEnable(mytextype);					// Enable Texture Mapping
	glTexEnvf (GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);

	if (terrainList == NULL)
	{
		char logBuf [256];
		sprintf_s(logBuf, 256, "Geomap3D: Generating 3D image textures: %s\0", filename);
		Logger::getInstance()->logMessage (logBuf);
		
		terrainList = genTerrainList(centerPoint, m_DtedData, estLatToMConv, estLonToMConv, terrainListSize);
		freeImageData ();
		
		sprintf_s(logBuf, 256, "Geomap3D: Generating 3D image textures complete: %s\0", filename);
		Logger::getInstance()->logMessage (logBuf);
	}
	
	for (int i = 0; i < terrainListSize; i ++)
		glCallList (terrainList[i]);

	glDisable(mytextype);		
}

int* Geomap3D::genTerrainList (AgentPosition* centerPoint, DTEDObject* dtedData, double estLatToMConv, double estLonToMConv, int &terrainListSizeOut)
{
	//terrainListSizeOut = numXTex*numYTex;
	terrainListSizeOut = 1;
	int* terrainList = new int [terrainListSizeOut];
	for (int i = 0; i < terrainListSizeOut; i ++)
	{
		int terrainDL = glGenLists(1);
		// create the display list
		glNewList(terrainDL,GL_COMPILE);	
		terrainList[i] = terrainDL;

		glShadeModel(GL_SMOOTH);							// Enables Smooth Shading
		if (i == 0)
		{
			textureTile* currTexture;
			int lastLonZ = 1313;
			
			for (int i = 0; i < numXTex; i ++)
			{
				for (int j = 0; j < numYTex; j ++)
				{
					currTexture = &myTextures[i][j];
					
					glBindTexture (mytextype, currTexture->texName);
					

					int startLonIdx = (int)((currTexture->minLon - dtedData->anchorLon) / dtedData->resLon);
					int startLatIdx = (int)((currTexture->minLat - dtedData->anchorLat) / dtedData->resLat);
					int endLonIdx = (int)((currTexture->maxLon - dtedData->anchorLon) / dtedData->resLon);
					int endLatIdx = (int)((currTexture->maxLat - dtedData->anchorLat) / dtedData->resLat);

					for (double latIdx = startLatIdx; latIdx < endLatIdx; latIdx ++)
					{
						glBegin( GL_TRIANGLE_STRIP);
						for (double lonIdx = startLonIdx; lonIdx <= endLonIdx; lonIdx ++)
						{

							glTexCoord2d (currTexture->texCoordLeft + (lonIdx-startLonIdx)/(endLonIdx-startLonIdx)*(currTexture->texCoordRight-currTexture->texCoordLeft), currTexture->texCoordBottom - (latIdx-startLatIdx)/(endLatIdx-startLatIdx)*(currTexture->texCoordBottom-currTexture->texCoordTop));
							glVertex3d( estLonToMConv*(currTexture->minLon + (lonIdx-startLonIdx)/(endLonIdx-startLonIdx)*(currTexture->maxLon-currTexture->minLon) - centerPoint->getLonDecDeg()), estLatToMConv*(currTexture->minLat + (latIdx-startLatIdx)/(endLatIdx-startLatIdx)*(currTexture->maxLat-currTexture->minLat) - centerPoint->getLatDecDeg()), dtedData->getZ((int)latIdx,(int)lonIdx) - centerPoint->getAltMslM());

							glTexCoord2d( currTexture->texCoordLeft + (lonIdx-startLonIdx)/(endLonIdx-startLonIdx)*(currTexture->texCoordRight-currTexture->texCoordLeft), currTexture->texCoordBottom - (latIdx+1-startLatIdx)/(endLatIdx-startLatIdx)*(currTexture->texCoordBottom-currTexture->texCoordTop));
							glVertex3d( estLonToMConv*(currTexture->minLon + (lonIdx-startLonIdx)/(endLonIdx-startLonIdx)*(currTexture->maxLon-currTexture->minLon) - centerPoint->getLonDecDeg()), estLatToMConv*(currTexture->minLat + (latIdx+1-startLatIdx)/(endLatIdx-startLatIdx)*(currTexture->maxLat-currTexture->minLat) - centerPoint->getLatDecDeg()), dtedData->getZ((int)latIdx+1,(int)lonIdx) - centerPoint->getAltMslM());

						}
						glEnd();	
							
					}
					
				}
			}



		}

		glEndList();
	}
	// return the list index so that the application can use it
	return(terrainList);
}
