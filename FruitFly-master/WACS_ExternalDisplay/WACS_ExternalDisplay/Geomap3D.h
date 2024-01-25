#ifndef GEOMAP3D_H
#define GEOMAP3D_H

#include "stdafx.h"
#include "AgentPosition.h"
#include "glPoint.h"
#include "DTEDObject.h"

/**
\brief Enumeration representing the image format 
*/
enum imgformats { RGB, MONO16, MONO8, RGBA, MONOS16, ALPHA8, RGB24, UNK };

/**
\brief Enumeration representing status of map load
*/
enum importstate {SUCCESS,BADFORMAT,IMPERROR,NOMEM,CANCELLED};


/**
	\struct textureTile
	\brief Structure holding data about an tile of an OpenGL texture.
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
struct textureTile
{
	/**
	GL Texture name
	*/
	GLuint texName;
	
	/**
	Percentage of entire image's width to start this tile
	*/
	double startXperc;
	
	/**
	Percentage of entire image's height to start this tile
	*/
	double startYperc;
	
	/**
	Percentage of entire image's width to stop this tile
	*/
	double endXperc;
	
	/**
	Percentage of entire image's height to stop this tile
	*/
	double endYperc;


	/**
	Texture coordinate of the left side of this tile
	*/
	double texCoordLeft;
	
	/**
	Texture coordinate of the right side of this tile
	*/
	double texCoordRight;
	
	/**
	Texture coordinate of the top side of this tile
	*/
	double texCoordTop;
	
	/**
	Texture coordinate of the bottom side of this tile
	*/
	double texCoordBottom;


	/**
	\brief Longitude decimal degrees of left edge of texture tile
	*/
	double minLon;
	
	/**
	\brief Latitude decimal degrees of upper edge of texture tile
	*/
	double maxLat; 
	
	/**
	\brief Longitude decimal degrees of right edge of texture tile
	*/
	double maxLon;
	
	/**
	\brief Latitude decimal degrees of lower edge of texture tile
	*/
	double minLat; 
};



/**
	\class Geomap3D
	\brief An object to store information about a map so it can be rendered in an OpenGL scene
	from file
	\author John Humphreys
	\date 2009
	\note Copyright: Johns Hopkins University Applied Physics Laboratory
	All rights reserved
*/
class Geomap3D {
	public:

		/**
		\brief Constructor, sets all default values
		\return none
		*/
		Geomap3D();

		/**
		\brief Destructor
		*/
		~Geomap3D();

		/**
		\brief Frees memory allocated for image data.  Can be called once textures are generated because
		raw image data is no longer needed
		\return void
		*/
		void freeImageData ();

		/**
		\brief Loads a map from file and stores necessary data for display
		\param imagefilename Filename of image 		
		\param abortFlag Boolean flag to abort generation in process.  Would be changed externally
		\param dtedData DTED data to use when generating 3D texture overlays of map
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\return Status of load
		*/
		bool importImage( const char *imagefilename, bool *abortFlag, DTEDObject* dtedData, AgentPosition* centerPoint);
		
		/**
		\brief Contains OpenGL commands to render the map into a GL scene in 3D
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\return void
		*/
		void draw( AgentPosition* centerPoint, double estLatToMConv, double estLonToMConv);

		/**
		\brief Generate call list for displaying map in 3D scene
		\param centerPoint Offset for all OpenGL locations.  This real-world location corresponds to OpenGL location 0,0,0.
		\param dtedData DTED data to use when generating 3D texture overlays of map
		\param estLatToMConv Estimated conversion from latitude to meters for the area.  Used to map latitudes in meter space
		\param estLonToMConv Estimated conversion from longitude to meters for the area.  Used to map longitudes in meter space
		\param terrainListSizeOut Size of terrainList that was generated after call
		\return List of display lists for all textures generated
		*/
		int* genTerrainList (AgentPosition* centerPoint, DTEDObject* dtedData, double estLatToMConv, double estLonToMConv, int &terrainListSizeOut);

		/**
		\brief 'Drive' compoenent of filename for loaded map
		*/
		char drive[10]; 

		/**
		\brief 'Directory' compoenent of filename for loaded map
		*/
		char dir[100]; 

		/**
		\brief 'Filename' compoenent of filename for loaded map
		*/
		char fname[10000]; 

		/**
		\brief 'Extension' compoenent of filename for loaded map
		*/
		char ext[10];

		/**
		\brief Full filename of loaded map
		*/
		char filename[10000];

		/**
		\brief Accessor for the width in meters of the loaded image
		\return width in meters of the loaded image
		*/
		double getWidthMeters () {return widthMeters;}
		
		/**
		\brief Accessor for the height in meters of the loaded image
		\return height in meters of the loaded image
		*/
		double getHeightMeters () {return heightMeters;}

		/**
		\brief Accessor for the Longitude of the left edge of the loaded image
		\return Minimum longitude decimal degrees of image
		*/
		double getMinLon () { return m_MinLon;}
		
		/**
		\brief Accessor for the Latitude of the top edge of the loaded image
		\return Maximum latitude decimal degrees of image
		*/
		double getMaxLat () {return m_MaxLat;}

		/**
		\brief Return the percent completed of image generation
		\return Percent completed of image generation
		*/
		int getPercentComplete () {return percComplete;}

		/**
		\brief Set the percent completed of image generation and update the state window
		\param newPerc completed of image generation
		*/
		void setPercComplete (int newPerc);

		/**
		\brief Accessor for the image data.
		\param imageDepth After return, will have the number of bytes per pixel
		\param imageX After return, will have the x-dimension of the image
		\param imageY After return, will have the y-dimension of the image
		\return Image data 2D array
		*/
		unsigned char** getImageData (int& imageDepth, int& imageX, int& imageY);

		/**
		\brief Accessor for width in pixels of loaded image
		\return pixel width of image
		*/
		int getPixelWidth () {return imageX;}
		
		/**
		\brief Accessor for height in pixels of loaded image
		\return pixel height of image
		*/
		int getPixelHeight () {return imageY;}


	private:

		DTEDObject* m_DtedData; //!< DTED data used to generate 3D texturing

		/**
		\brief GL call lists that generates 3D maps
		*/
		int* terrainList;

		/**
		\brief Size of terrainList
		*/
		int terrainListSize;

		/**
		\brief Texture type enumeration
		*/
		GLenum mytextype;

		/**
		\brief Original image pixel width
		*/
		double img_orig_width;

		/**
		\brief Original image pixel height
		*/
		double img_orig_height;

		/**
		\brief Meter width of whole image
		*/
		double widthMeters;
		
		/**
		\brief Meter heigh of whole image
		*/
		double heightMeters;

		/**
		\brief Meters/pixel resolution of image width
		*/
		double resWMeters;
		
		/**
		\brief Meters/pixel resolution of image height
		*/
		double resHMeters;

		/**
		\brief Meters/pixel resolution of image width
		*/
		double resXMeters;
		
		/**
		\brief Meters/pixel resolution of image height
		*/
		double resYMeters;

		/**
		\brief Rotation about x axis of image
		*/
		double rotXRadians;

		/**
		\brief Rotation about y axis of image
		*/
		double rotYRadians;

		/**
		\brief Longitude decimal degrees of left edge of whole image
		*/
		double m_MinLon;
		
		/**
		\brief Latitude decimal degrees of upper edge of whole image
		*/
		double m_MaxLat; 

		double ulX; //!< UpperLeft X(longitude) coordinate of image
		double ulY; //!< UpperLeft X(latitude) coordinate of image
		double urX; //!< UpperRight X(longitude) coordinate of image
		double urY; //!< UpperRight X(latitude) coordinate of image
		double llX; //!< LowerLeft X(longitude) coordinate of image
		double llY; //!< LowerLeft X(latitude) coordinate of image
		double lrX; //!< LowerRight X(longitude) coordinate of image
		double lrY; //!< LowerRight X(latitude) coordinate of image

		/**
		\brief If true, this map has been successfully registered
		*/
		bool registered;

		/**
		\brief Number of image bytes we are expecting from the image.  If 3, we are getting RGB images.  Otherwise, will assume
		greyscale
		*/
		int desiredImageBytes;

		/**
		\brief If the image is still loading, percent complete generating textures
		*/
		int percComplete;

		/**
		\brief 2D Array of textures for the loaded mosaic image.  If the image is larger than hardware limits, it will be broken down
		in this object into smaller textures
		*/
		textureTile ** myTextures;

		/**
		\brief X Dimension of myTextures
		*/
		int numXTex;

		/**
		\brief Y Dimension of myTextures
		*/
		int numYTex;

		/**
		\brief If true, we will smooth shade images.  If false, images will be pixelated
		*/
		bool smoothShading;

		int m_MaxTextureSizeW; //!< Maximum texture width size desired.  Must be 2^n
		int m_MaxTextureSizeH; //!< Maximum texture height size desired.  Must be 2^n

		/**
		\brief Image data loaded from file
		*/
		unsigned char** imageData;
		
		/**
		\brief Number of image bytes - 3:RGB, 1:Luminance
		*/
		int imageDepth;

		/**
		\brief X pixels in image
		*/
		int imageX;

		/**
		\brief Y pixels in image
		*/
		int imageY;

};

#endif
