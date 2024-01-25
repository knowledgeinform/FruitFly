//---------------------------------------------------------------------------------------
/*!\class  File Name:	SimplePointSet.h
// \author Author:		Jason A.Stipes
// \brief  Purpose:		Class for 3D Point Sets
//
// \note   Notes:	
//
// \note   Routines:  see SimplePointSet class
//
// \note   Copyright (c) 2008 Johns Hopkins University 
// \note   Applied Physics Laboratory.  All Rights Reserved.
*/
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------

#pragma once

#include <string>
#include "DataFile.h"
#include "Matrix.h"
#include "PointSetTransform.h"
#include "PointSetBounds.h"
#include "float.h"
#include <time.h>

using namespace std;

//Node
typedef struct PointNode 
{
	int count;
	PointNode* next;
	float x;
	float y;
	float z;
	int ix;
};


typedef struct PointSet
{
	int version;
	int coord;
	int zone;
	float spacing;
};


// Class for generic point set data.
class SimplePointSet: public DataFile
{
public:

	//!Default Constructor
	SimplePointSet(void);

	//!Constructor for Empty PointSet
	SimplePointSet(int npoints);

	//!Constructor for Empty PointSet
	SimplePointSet(const string &path,int npoints, int numextrafields);

	//!Destructor
	virtual ~SimplePointSet(void);

	//!Data initialization, memory allocation
	void init(bool hasData=true);

	//!Convert the Point Cloud to a projective reference frame
	Point3D convertToProjection(float origOffsetX, float origOffsetY,float zRatio = 20, float* zOffset=NULL, bool nearestPointOnly=false );
	
	//!Determine the transform to put point cloud into a virtual camera's reference frame
	DMatrix createWorldToViewTransform(float lookAtX, float lookAtY, float lookAtZ, float eyeX, float eyeY, float eyeZ);

	//!Apply the Transform - legacy
	void ApplyTransform(PointSetTransform transform, bool applyOffset=true); // EMW
	
	//!Copy points and apply transform to copy
	void getTransformedPoints(float*x, float*y, float*z, float refx=0, float refy=0, float refz=0);
	
	//!Apply transform to this object's points
	void applyTransformToPoints();

	//!get the projections's pixel boundaries for the 3D bounds
	float* clipToBounds(PointSetBounds* bounds, int& r, int&c, int& ci, int& cj);

	//!Calculate the normals at every point
	void calculateNormals();

	//!Copy a subset of the pointset's points
	bool CopySubset(PointSet &pset, bool *keep);

	//!Read a Binary file - deprecated
	bool Read(char *fileName, bool readData);

	//!Write a binary file - deprecated
	bool Write(const char *fileName);

	//!Converts an ASCII file to binary, calculating spacing if specified
	bool ConvertASCIIFile(char *fileName, int utmZone, int numExtraFields, bool *useOffset, int numHeaderLines, bool computeSpacing);
	
	//!Converts an ASCII folder of files to binary, calculating spacing if specified
	bool ConvertASCIIFolder(char *folderName, int utmZone, int numExtraVals, bool *useOffset, int numHeaderLines, bool computeSpacing);
	
	//!Several functions for outputting a VRML file of the point set
	void createVRML(char* path, PointSet* ps1, bool shownormals=true, bool applyxf=false);
	void createVRML(char* path, PointSet* ps1, PointSet* ps2, bool shownormals=true, bool applyxf=false);
	void createVRML(char* path, PointSet** psets, int numPointSets, int* hues, bool shownormals=false, bool applyxf=true); 
	void createSpecialVRML(char* path, PointSet* ps1);
	void createCorrelatedVRML(char* path, PointSet* ps1, PointSet* ps2, bool applyxf=false ); 

	//!Applies z noise to the pointset
	void ApplyNoise(float sigma);

	//!Automatically calculates the average point spacing
	float GetHorizontalSpacing();

	//!reverse calibration method to get row, column from xyz
	int xyz_to_ij(float x, float y, float z);

	//!reverse calibration method to get row, column from xyz - alternate return method
	int xyz_to_ij(float x, float y, float z, int& ii, int& jj);

	//!inverse reverse calibration method to get row, column from xyz - alternate return method
	void ijz_to_xy(float i, float j, float z, float&xx , float&yy);

	//! Turns on debug output
	static const bool debugOn = true;

	//!True if this poinset has been converted to a projection
	bool projection;

	//!When converting to a projection, only save the point closest to the virtual camera
	bool nearestPointOnly;

	//! The number of rows in the projected image
	int rows;

	//! The number of columns in the projected image
	int cols;

	//! The Field of view (radians) in the x direction of the projected image
	float xFOV;

	//! The Field of view (radians) in the y direction of the projected image
	float yFOV;

	//! The average spacing of points
	float avgSpacing;

	//!The header data structure for the PointSet DataFile
	PointSet pSetInfo;

	//! The filename of the pointset
	string fileName;

	//! The number of points in the pointset
	int numPoints;

	//! The number of extra fields in the pointset (in addition to x,y,z)
	int numExtraFields;

	//! Indicates if the actual data has been filled in
	bool populated;

	//! The bounding cube for the pointset
	PointSetBounds bounds;

	//! The transform that puts' the pointset's local coordinates into global coordinates
	PointSetTransform transform;

	//! A 2D array of points mapping to the pixels of the projected point set
	PointNode* points;

	//deprecated
	float pMax;
	float pMin;

	//!The x y and z coordinate arrays
	float *x;
	float *y;
	float *z;

	//! the x y and z normal vector component arrays
	float *nx;
	float *ny;
	float *nz;

	//! The intensity array
	int	  *i;

	//! A 2D vector of the extra fields
	float **fields;

	//! Correlating index.  Keeps track of what index a given point corresponds to in another point set
	int	  *cindex;

	//! Array of sampled indices
	int   *sampled;

	//! The x y and z offsets 
	float xoff;
	float yoff;
	float zoff;

	//! An arry of the extra field offsets
	double *foff;

	//! The extra field max and min values
	double *fmax;
	double *fmin;

};