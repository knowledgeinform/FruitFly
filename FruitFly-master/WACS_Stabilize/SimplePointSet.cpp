//---------------------------------------------------------------------------------------
/*!\class  File Name:	SimplePointSet.cpp
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

#include "SimplePointSet.h"
#include "MultiCoreAlgorithm.h"
#include "NRC.h"
#include "Dirent.h"

#define TOO_CLOSE 0.01

/*! \fn SimplePointSet::SimplePointSet()
 *  \brief Default Constructor
 *  \exception 
 *  \return
 */
SimplePointSet::SimplePointSet()
{
	projection = false;
	populated = false;
	nx=NULL;
	ny=NULL;
	nz=NULL;
	this->i=NULL;
	points=NULL;
	fields=NULL;
	cindex=NULL;
	sampled=NULL;
	setHeaderData((char*)&pSetInfo,sizeof(pSetInfo));
}



/*! \fn SimplePointSet::SimplePointSet(const string &path,int npoints, int nfields)
 *  \brief Constructor for creating a new SimplePointSet
 *  \param path - The path to read/store the pointset
 *  \param npoints - The number of points to allocate
 *  \param numextrafields - The number of extra fields (in addition to x,y,z)
 *  \exception 
 *  \return void
 */
SimplePointSet::SimplePointSet(const string &path,int npoints, int numextrafields)
:DataFile(path,npoints,numextrafields+3)
{
	init(false);
}

/*! \fn SimplePointSet::SimplePointSet(const string &path,int npoints, int nfields)
 *  \brief Constructor for creating a new SimplePointSet
 *  \param path - The path to read/store the pointset
 *  \param npoints - The number of points to allocate
 *  \param numextrafields - The number of extra fields (in addition to x,y,z)
 *  \exception 
 *  \return void
 */
SimplePointSet::SimplePointSet(int npoints)
:DataFile(NULL,npoints,3)
{
	init(false);
}


/*! \fn void SimplePointSet::init(bool hasData)
 *  \brief Constructor for creating a new SimplePointSet
 *  \param hasData - Indicates whether this init is for a new Pointset, or one read from file(true)
 *  \exception 
 *  \return void
 */
void SimplePointSet::init(bool hasData)
{
	//Initialize pointers that may or may not be allocated memory
	nx=NULL;
	ny=NULL;
	nz=NULL;
	this->i=NULL;
	points=NULL;
	fields=NULL;
	cindex=NULL;
	sampled=NULL;
	projection = false;

	//Initialize variables
	fileName = this->path;
	numPoints = this->numRows;
	numExtraFields = this->numColumns - 3;
	populated = hasData;

	//Point offsets to appropriate DataFile superclass items
	if(this->offsets != NULL)
	{
		xoff = (float)offsets[0];
		yoff = (float)offsets[1];
		zoff = (float)offsets[2];
		if(numExtraFields >0)
		{
			foff = &offsets[3];
			fmax = &maxVals[3];
			fmin = &minVals[3];
		}
		else
		{
			foff = NULL;
			fmax = NULL;
			fmin = NULL;
		}
	}
	else
	{
		xoff = 0.0;
		yoff = 0.0;
		zoff = 0.0;
	}

	//Allocate bounds
	memset(&bounds, 0, sizeof(PointSetBounds));

	//point x,y,z to superclass data
	x = this->getColumn(0);
	y = this->getColumn(1);
	z = this->getColumn(2);

	//Populate bounds with superclass data
	bounds.xmax = getMaxVal(0);
	bounds.xmin = getMinVal(0);
	bounds.ymax = getMaxVal(1);
	bounds.ymin = getMinVal(1);
	bounds.zmax = getMaxVal(2);
	bounds.zmin = getMinVal(2);

	//allocate memory
	cindex = (int*)malloc(this->numPoints*sizeof(int));
	sampled = (int*)malloc(this->numPoints*sizeof(int));
	memset(cindex, -1, this->numPoints*sizeof(int));
	memset(sampled, 0, this->numPoints*sizeof(int));

	if(numExtraFields > 0)
	{
		fields = (float**)malloc(numExtraFields*sizeof(float*));
		for(int i=0; i< numExtraFields; i++)
		{
			fields[i] = this->getColumn(3+i);
		}
	}
	else
		fields = NULL;

	//point the header data to the header data structure for pointset - pSetInfo
	setHeaderData((char*)&pSetInfo,sizeof(pSetInfo));

}


/*! \fn SimplePointSet::~SimplePointSet()
 *  \brief Destructor - frees memory as necessary
 *  \exception 
 *  \return void
 */
SimplePointSet::~SimplePointSet()
{
	if(nx!=NULL)
		free(nx);
	if(ny!=NULL)
		free(ny);
	if(nz!=NULL)
		free(nz);
	if(cindex!=NULL)
		free(cindex);
	if(sampled!=NULL)
		free(sampled);
	if(fields!=NULL)
		free(fields);
	if(this->i!=NULL)
		free(this->i);

	//Points is a gridded linked list - This traverses data and frees all the PointNodes
	if(points!=NULL)
	{
		int cnt=0;
		PointNode* node, *next;
		for(int i=0; i<rows*cols; i++)
		{
			node = &points[i];
				if(node->next == NULL) continue;
			node = node->next;
			do
			{
				next=node->next;
				free(node);
				cnt++;
				node = next;
			}while(node!=NULL);
		}
		free(points);
	}
}

/*! \fn bool SimplePointSet::Read(char *fileName, bool readData)
 *  \brief Reads a binary file and initializes data structures 
 *  \param fileName - The file to read
 *  \param readData - Read all the data, or just the header info
 *  \exception 
 *  \return boolean success
 */
bool SimplePointSet::Read(char *fileName, bool readData)
{
	this->fileName = fileName;
	this->path = fileName;
	this->readBinaryFile(readData);
	this->init();
	return true;
}

/*! \fn bool SimplePointSet::Write(const char *fileName)
 *  \brief Writes a binary file and initializes data structures 
 *  \param fileName - The file to write
 *  \exception 
 *  \return boolean success
 */
bool SimplePointSet::Write(const char *fileName)
{
	this->writeBinaryFile(fileName);
	return true;
}

/*! \fn void SimplePointSet::getTransformedPoints(float*x, float*y, float*z, float refx, float refy, float refz)
 *  \brief Creates a copy of all points and applies the PointsetTransform and subtracts refx, etc.
 *  \param x - destination for the transformed x points
 *  \param y - destination for the transformed y points
 *  \param x - destination for the transformed z points
 *  \param refx - offset to subtract from x
 *  \param refy - offset to subtract from y
 *  \param refz - offset to subtract from z
 *  \exception 
 *  \return void
 */
void SimplePointSet::getTransformedPoints(float*x, float*y, float*z, float refx, float refy, float refz)
{
	for(int i=0; i<this->numPoints; i++)
	{
		x[i] = this->x[i];
		y[i] = this->y[i];
		z[i] = this->z[i];
		transform.rotateTranslateVector(x[i], y[i], z[i]);
		x[i] -= refx;
		y[i] -= refy;
		z[i] -= refz;
	}

}

/*! \fn void SimplePointSet::applyTransformToPoints()
 *  \brief Applies the PointsetTransform to all points in the SimplePointSet
 *         The bounding box is not updated
 *  \exception 
 *  \return void
 */
void SimplePointSet::applyTransformToPoints()
{
	double xx, yy, zz;
	for(int i=0; i<this->numPoints; i++)
	{
		xx = (double)x[i];
		yy = (double)y[i];
		zz = (double)z[i];
		transform.rotateTranslateVector(xx, yy, zz);
		x[i] = (float) (xx - xoff);
		y[i] = (float) (yy - yoff);
		z[i] = (float) (zz - zoff);
	}
}



/*! \fn void SimplePointSet::ApplyTransform(PointSetTransform transform, bool applyOffset)
 *  \brief Applies the PointsetTransform to all points in the SimplePointSet,updates bounding box
 *         Used in Gridded Registration and old Registration code
 *  \param transform - the PointSetTransform to apply
 *  \param applyOffset - whether or not to apply the offset after the transform
 *  \exception 
 *  \return void
 */
void SimplePointSet::ApplyTransform(PointSetTransform transform, bool applyOffset)
{
	double xs;
	double ys;
	double zs;
	double xt;
	double yt;
	double zt;

	// Apply transform to point coordinates.
	DMatrix R(3,3);
	DVector T(3);

	R = angles2rot(transform.rx, transform.ry, transform.rz);

	T[0] = transform.tx;
	T[1] = transform.ty;
	T[2] = transform.tz;

	bounds.xmin = FLT_MAX;
	bounds.ymin = FLT_MAX;
	bounds.zmin = FLT_MAX;
	bounds.xmax = -FLT_MAX;
	bounds.ymax = -FLT_MAX;
	bounds.zmax = -FLT_MAX;

	for (int m=0;m<numPoints;m++)
	{
		// Apply offset and move center of rotation (transform.cx,cy,cz) to 0,0,0)
		if(applyOffset)
		{
			xs = x[m] + xoff - transform.cx;
			ys = y[m] + yoff - transform.cy;
			zs = z[m] + zoff - transform.cz;
		}
		else
		{
			xs = x[m] - transform.cx;
			ys = y[m] - transform.cy;
			zs = z[m] - transform.cz;
		}

		//Apply Rotation Matrix and Add Translation,  add back center of rotation.
		xt = R.data[0]*xs + R.data[1]*ys + R.data[2]*zs + T[0] + transform.cx;
		yt = R.data[3]*xs + R.data[4]*ys + R.data[5]*zs + T[1] + transform.cy;
		zt = R.data[6]*xs + R.data[7]*ys + R.data[8]*zs + T[2] + transform.cz;

		//update bounding box
		if (xt < bounds.xmin) bounds.xmin = xt;
		if (xt > bounds.xmax) bounds.xmax = xt;
		if (yt < bounds.ymin) bounds.ymin = yt;
		if (yt > bounds.ymax) bounds.ymax = yt;
		if (zt < bounds.zmin) bounds.zmin = zt; 
		if (zt > bounds.zmax) bounds.zmax = zt; 


		if(applyOffset)
		{
			x[m] = (float)(xt - xoff);
			y[m] = (float)(yt - yoff);
			z[m] = (float)(zt - zoff);
		}
		else
		{
			x[m] = (float)xt;
			y[m] = (float)yt;
			z[m] = (float)zt;
		}

	}
}

/*! \fn int SimplePointSet::xyz_to_ij(float x, float y, float z)
 *  \brief This is the reverse calibration function, maps xyz point to ij pixel location for icp
 *  \param x - The x coordinate
 *  \param y - The y coordinate
 *  \param z - The z coordinate
 *  \exception 
 *  \return int - the index into the points grid that projects to that xyz coordinate
 */
int SimplePointSet::xyz_to_ij(float x, float y, float z)
{
		int xx, yy;
		if(projection)
		{
			xx = (int)((x/-z)/xFOV*cols/2+cols/2);
			yy = (int)((y/-z)/yFOV*rows/2+rows/2);
			if(yy<0 || xx < 0 || yy > rows || xx>cols)		
				return -1;
			return yy*cols + xx;
		}
		else
			return -1;

}

/*! \fn int SimplePointSet::xyz_to_ij(float x, float y, float z)
 *  \brief This is the reverse calibration function, maps xyz point to ij pixel location for icp
 *  \param x - The x coordinate
 *  \param y - The y coordinate
 *  \param z - The z coordinate
 *  \param ii - The returned pixel row
 *  \param jj - The returned pixel column
 *  \exception 
 *  \return void
 */
int SimplePointSet::xyz_to_ij(float x, float y, float z, int& ii, int& jj)
{

		if(projection)
		{
			ii = (int)((x/-z)/xFOV*cols/2+cols/2);
			jj = (int)((y/-z)/yFOV*rows/2+rows/2);

			if(jj<0) jj = 0;
			if(ii<0) ii = 0;
			if(jj>rows) jj=rows;
			if(ii>cols) ii=cols;

			//if(yy<0 || xx < 0 || yy > rows || xx>cols)		
			//	return -1;
			return jj*cols + ii;
		}
		else
			return -1;
}

/*! \fn void SimplePointSet::ijz_to_xy(float i, float j, float z, float&xx , float&yy)
 *  \brief This is the inverse reverse calibration function, maps ijz pixel location to xy coordinate
 *  \param i - The pixel row
 *  \param j - The pixel column
 *  \param z - The z coordinate
 *  \param xx - The returned x coordinate
 *  \param yy - The returned y coordinate
 *  \exception 
 *  \return void
 */
void SimplePointSet::ijz_to_xy(float i, float j, float z, float&xx , float&yy)
{
		if(projection)
		{
			xx =-z*(i-cols/2)*xFOV/(cols/2);
			yy =-z*(j-rows/2)*yFOV/(rows/2);
		}
}

/*! \fn DMatrix SimplePointSet::createWorldToViewTransform(float lookAtX, float lookAtY, float lookAtZ, float eyeX, float eyeY, float eyeZ)
 *  \brief Similar to gluLookAt, this generates the transform to put a point cloud into the coordinate
 *         frame of a camera location and orientation
 *  \param lookAtX - The x coordinate for where the camera is looking (typically center of the point cloud)
 *  \param lookAtY - The y coordinate for where the camera is looking (typically center of the point cloud)
 *  \param lookAtZ - The z coordinate for where the camera is looking (typically center of the point cloud)
 *  \param eyeX - The x coordinate for where the camera is located
 *  \param eyeY - The y coordinate for where the camera is located
 *  \param eyeZ - The z coordinate for where the camera is located
 *  \exception 
 *  \return DMatrix - the 4x4 rotation translation matrix
 */
DMatrix SimplePointSet::createWorldToViewTransform(float lookAtX, float lookAtY, float lookAtZ, float eyeX, float eyeY, float eyeZ)
{
	double viewx, viewy, viewz;
	
	DMatrix R(4,4);
	DMatrix T1(4,4);
	DMatrix T2(4,4);
	DVector forward(3);
	DVector side(3);
	DVector up(3);

	//Get rotation matrix to align new coordinate system with 

	//up vector
	up[0] = 0;
	up[1] = 1;
	up[2] = 0;

	// center - eye center is forward vector
	forward[0] = lookAtX - eyeX;
	forward[1] = lookAtY - eyeY;
	forward[2] = lookAtZ - eyeZ;

	viewz=forward[2];

    forward.normalize();

	//Make length of view vector the length of the original viewZ vector
	viewx=forward[0]*viewz;
	viewy=forward[1]*viewz;
	viewz=forward[2]*viewz;

	eyeX = lookAtX +(float)viewx;
	eyeY = lookAtY +(float)viewy;
	eyeZ = lookAtZ +(float)viewz;

    /* Side = forward x up */
    side = crossp(forward, up);
    side.normalize();

    /* Recompute up as: up = side x forward */
    up = crossp(side, forward);

	R[0][0] = side[0];
	R[1][0] = side[1];
	R[2][0] = side[2];

	R[0][1] = up[0];
	R[1][1] = up[1];
	R[2][1] = up[2];

	R[0][2] = -forward[0];
	R[1][2] = -forward[1];
	R[2][2] = -forward[2];

	R[0][3] = 0;
	R[1][3] = 0;
	R[2][3] = 0;
	R[3][3] = 1;


	//Rotation Matrix from normal unit axes to new  axes is in R

	//T1 is to translate the eye to 0,0,0 to make it the new origin of the points
	T1 = transform.formTranslationMatrix4(-eyeX,-eyeY,-eyeZ);

	//We need the inverse of R since we are now going to rotate the new axes z vector to
	//coincide with the normsal [ 0 0 1] z axis (and all points with it)
	R = R.fastInv();

	//Translate center of view to origin, perform rotation
	R = mult(R,T1);

	//Print out RT Matrix
	//for(int i=0; i<=3; i++)
	//	printf("%f %f %f %f\n",R[i][0],R[i][1],R[i][2],R[i][3]);


	//Now R may be used to take all points and put them in the new reference frame
	return R;
}

/*! \fn float* SimplePointSet::clipToBounds(PointSetBounds* bounds, int& r, int&c, int& ci, int& cj)
 *  \brief This function generates a projected image of the pointset constrained by the bounds
 *  \param bounds - The bounds of the image to produce
 *  \param r - Returns the number of rows in the generated image
 *  \param c - Returns the number of columns in the generated image
 *  \param ci - Specifies the center pixel location (row) of the extracted subimage
 *  \param cj - Specifies the center pixel location (column) of the extracted subimage
 *  \exception 
 *  \return float* A 2D array (in 1D form) representing the image with floating point pixels
 *   It is the calling function's responsibility to free this memory when done
 */
float* SimplePointSet::clipToBounds(PointSetBounds* bounds, int& r, int&c, int& ci, int& cj)
{
	float* retval;

	int imin, jmin, imax, jmax, test, size, icnt, jcnt;

	//Points are stored in local coordinate frame and transform converts them
	//to global coordinates.  Get inverse to use on bounds which is stored
	//in global coordinates
	PointSetTransform tf = transform.getInverse();

	float x,y,z;
	
	//Get the location of the corner of the bounding box
	x = (float)bounds->xmin;
	y = (float)bounds->ymin;
	z = (float)bounds->zmax;
    //Get the location of this corner in local coords
	tf.rotateTranslateVector(x,y,z);

	//Do reverse calibration of this local coord to get pixel location
	 test = xyz_to_ij(x, y,z,imin,jmin);
	 if (test == -1) return NULL;

	 //Get the location of the other corner of the bounding box
	 x = (float)bounds->xmax;
	 y = (float)bounds->ymax;
	 z = (float)bounds->zmax;
	 //Get the location of this corner in local coords
	 tf.rotateTranslateVector(x,y,z);
	 //Do reverse calibration of this local coord to get pixel location
	 test = xyz_to_ij(x, y, z,imax,jmax);
	 if (test == -1) return NULL;

	 //now we have the pixel locations of the image corners
	 ci=(imax+imin)/2;
	 cj=(jmax+jmin)/2;

	 c = imax-imin;
	 r = jmax-jmin;
	 size = c*r;

	 //allocate memory for the resultant image
	 retval = (float*)malloc(size*sizeof(float));

	 icnt=jcnt=0;
	
	 //Copy the pixel values (z = depth) into the image
	 for(int i=imin; i<imax; i++)
	 {
		 jcnt=0;
		 for(int j=jmin; j<jmax; j++)
		 {
			 retval[jcnt*c+icnt] = points[j*cols+i].z;
			 jcnt++;
		 }
		 icnt++;
	 }

	return retval;
}

/*! \fn Point3D SimplePointSet::convertToProjection(float origOffsetX, float origOffsetY, float zRatio, float* zOffset, bool nearestPointOnly )
 *  \brief This function converts a point cloud into a projection from an imaginary camera placed at a location
 *  above the scene, where point locations are indexed by pixel location for use in ICP or other projection based algorithms
 *  \param origOffsetX - X offset from the center of the point cloud for the camera location
 *  \param origOffsetY - Y offset from the center of the point cloud for the camera location
 *  \param zRatio - The ratio of the z offset of the camera location, related to the width of the point cloud 
 *  i.e. if the point cloud is 50m wide, the z offset will be 1000m
 *  \param zOffset - If this is specified, it is used as the z offset for the camera location
 *  \param nearestPointOnly - Each pixel contains a linked list of points in order of distance from camera.  
 *  If this is true, only the point closest to the camera is stored.
 *  \exception 
 *  \return Point3D - The Location of the Camera
 */
Point3D SimplePointSet::convertToProjection(float origOffsetX, float origOffsetY, float zRatio, float* zOffset, bool nearestPointOnly )
{
	clock_t before, after;
	if(debugOn) before = clock();

	projection = true;

	this->nearestPointOnly=nearestPointOnly;

	float offsetZ;
	float origOffsetZ;

	// calculate the zoffset to use base on the function inputs
	if(zOffset != NULL && *zOffset > 0)
	{
		offsetZ = *zOffset;
	}
	else
	{
		offsetZ = (float)max(bounds.xmax-bounds.xmin, bounds.ymax-bounds.ymin) * zRatio;
		if(zOffset != NULL)
			*zOffset = offsetZ;
	}	

	origOffsetZ = abs(offsetZ);

	//Specify the center of the scan
	transform.csx = (float)(bounds.xmax + bounds.xmin) / 2.0f;
	transform.csy = (float)(bounds.ymax + bounds.ymin) / 2.0f;
	transform.csz = (float)(bounds.zmax + bounds.zmin) / 2.0f;

	//specify the origin of the transform (camera location)
	transform.cx = transform.csx + origOffsetX;
	transform.cy = transform.csy + origOffsetY;
	transform.cz = transform.csz + origOffsetZ;

	//Calculate the RT matrix necessary to put the point cloud into the reference frame of the camera
	transform.setRotationTranslationMatrix(createWorldToViewTransform(transform.csx,transform.csy,transform.csz,transform.cx,transform.cy,transform.cz));


	//Rotate the bounds on the x axis and determine the new width of the point set as viewed from the camera
	float xtemp, ytemp, ztemp,xtemp1, ytemp1, ztemp1;
	xtemp = (float)bounds.xmax;
	ytemp = transform.csy;
	ztemp = transform.csz;
	xtemp1 = (float)bounds.xmin;
	ytemp1 = transform.csy;
	ztemp1 = transform.csz;
	transform.rotateTranslateVector(xtemp,ytemp,ztemp);
	transform.rotateTranslateVector(xtemp1,ytemp1,ztemp1);
	float w =  abs(xtemp - xtemp1);

	//Rotate the bounds on the y axis and determine the new height of the point set as viewed from the camera
	xtemp = transform.csx;
	ytemp = (float)bounds.ymax;
	ztemp = transform.csz;
	xtemp1 = transform.csx;
	ytemp1 = (float)bounds.ymin;
	ztemp1 = transform.csz;
	transform.rotateTranslateVector(xtemp,ytemp,ztemp);
	transform.rotateTranslateVector(xtemp1,ytemp1,ztemp1);
	float h =  abs(ytemp - ytemp1);

	//Determine the angle of the field of view of the notional camera
	xFOV = atan2(w/2,origOffsetZ)*2;
	yFOV = atan2(h/2,origOffsetZ)*2;

	// Make enough columns and rows to store all the points
	cols = (int)::sqrt(w/h*numPoints);
	rows = (int)(cols*h/w);
	// Double, to make the points per pixel more sparse
	cols *=2;
	rows *=2;

	//Calculate the average spacing
	avgSpacing = (w/cols + h/rows)/2;

	//Allocate space for normals
	nx = (float*) malloc(numPoints*sizeof(float));
	ny = (float*) malloc(numPoints*sizeof(float));
	nz = (float*) malloc(numPoints*sizeof(float));

	memset(nx,0,numPoints*sizeof(float));
	memset(ny,0,numPoints*sizeof(float));
	memset(nz,0,numPoints*sizeof(float));

	// Allocate at least one PointNode per pixel
	points = (PointNode*)malloc(cols*rows*sizeof(PointNode));
	memset(points,0,(rows*cols)*sizeof(PointNode));
	
	int i, ix;
	int hit = 0;
	int max = 0;
	PointNode* temp;
	PointNode* newtemp;
	PointNode* prev;

	//allocate space for intensity values
	this->i = (int*)malloc(numPoints*sizeof(int));

	//Detect if we need to rotate the points, or just translate, which is easier
	bool translateOnly = transform.getRotationMatrix().isUnity();
	pMax = -FLT_MAX;
	pMin = FLT_MAX;


	//MultiCoreAlgorithm<void> m(&SimplePointSet::projectionLoop, 0, numPoints);
	double xx,yy,zz;
	
	//Loop through and adjust all the points
	for(i=0; i< numPoints; ++i)
	{
		xx = (double)x[i] + (double)xoff;
		yy = (double)y[i] + (double)yoff;
		zz = (double)z[i] + (double)zoff;

		//Take the z values and convert them to intensity values
		this->i[i] = (int)((z[i] - bounds.zmin)/(bounds.zmax-bounds.zmin)*3000 + 1500);

		//Do the actual point transformation
		if(translateOnly)
		{
			transform.translateVector(xx,yy,zz);
		}
		else
		{
			transform.rotateTranslateVector(xx,yy,zz);
		}

		x[i] = (float)xx;
		y[i] = (float)yy;
		z[i] = (float)zz;
		
		//Project the adjusted point into the notional camera and find the pixel row and column (combined into ix)
		ix = xyz_to_ij(x[i], y[i], z[i]);

		if(z[i] > pMax) pMax = z[i];
		if(z[i] < pMin) pMin = z[i];

		//Put the point information into the appropriate Point Node
		if(ix >=0)
		{
			// If no point has been entered at this i,j before
			if(points[ix].count == 0)
			{	
				points[ix].x = x[i];
				points[ix].y = y[i];
				points[ix].z = z[i];
				points[ix].count = 1;
				points[ix].ix = i;
			}
			else
			{
				//If a a point alread is in this pixel location

				//If we keep the nearest point only, check if current point is nearest and update
				if(nearestPointOnly)
				{
					if(z[i] > points[ix].z)
					{
						points[ix].x = x[i];
						points[ix].y = y[i];
						points[ix].z = z[i];
						points[ix].ix = i;
					}
				}
				else
				{

					//Put z values into xy bin in z order (nearest first)
					//This sorts in place, allocates a new Point Node
					//And does the necessary copying.
					temp = &points[ix];
					while (z[i] < temp->z)
					{
						temp->count++;	
						prev = temp;
						temp = temp->next;
						if(temp == NULL)
							break;
						
					}
					
					newtemp = (PointNode*)malloc(sizeof(PointNode));
					if(temp != NULL)
					{
						*newtemp = *temp;
						temp->x = x[i];
						temp->y = y[i];
						temp->z = z[i];
						temp->ix = i;
						temp->count ++; 
						temp->next = newtemp;

					}
					else
					{
						newtemp->x = x[i];
						newtemp->y = y[i];
						newtemp->z = z[i];
						newtemp->ix = i;
						newtemp->next = 0;
						newtemp->count = 1;
						prev->next = newtemp;
						
						
					}
				}

			}
	
		}
	}
	if(debugOn)
	{
		after = clock();
		printf("Build Projection took %f seconds\n", double(after-before)/CLOCKS_PER_SEC);
	}

	Point3D retval;

	//return the center of the camera
	retval.x = transform.cx;
	retval.y = transform.cy;
	retval.z = transform.cz;

	//Invert the tranform so applying it will convert the points back to the original reference frame
	this->transform.invert();

	//Calculate the normals of all points in the projection
	this->calculateNormals();

	return retval;

}


//void SimplePointSet::projectionLoop(int start, int stop, int step, void)
//{
//	
//	int i, ix;
//	int hit = 0;
//	int max = 0;
//	PointNode* temp;
//	PointNode* newtemp;
//	PointNode* prev;
//	bool translateOnly = transform.getRotationMatrix().isUnity();
//	pMax = -FLT_MAX;
//	pMin = FLT_MAX;
//
//	for(int i=start; i<=stop; i+=step)
//	{
//		this->i[i] = (int)((z[i] - bounds.zmin)/(bounds.zmax-bounds.zmin)*3000 + 1500);
//		if(translateOnly)
//		{
//			transform.translateVector(x[i],y[i],z[i]);
//		}
//		else
//		{
//			transform.rotateTranslateVector(x[i],y[i],z[i]);
//		}
//		
//		ix = xyz_to_ij(x[i], y[i], z[i]);
//
//
//					if(z[i] > pMax) pMax = z[i];
//			if(z[i] < pMin) pMin = z[i];
//
//		if(ix >=0)
//		{
//
//			if(points[ix].count == 0)
//			{	
//				points[ix].x = x[i];
//				points[ix].y = y[i];
//				points[ix].z = z[i];
//				points[ix].count = 1;
//				points[ix].ix = i;
//			}
//			else
//			{
//				if(nearestPointOnly)
//				{
//					if(z[i] > points[ix].z)
//					{
//						points[ix].x = x[i];
//						points[ix].y = y[i];
//						points[ix].z = z[i];
//						points[ix].ix = i;
//					}
//				}
//				else
//				{
//					//Put z values into xy bin in z order (nearest first)
//					temp = &points[ix];
//					while (z[i] < temp->z)
//					{
//						temp->count++;
//						prev = temp;
//						temp = temp->next;
//						if(temp == NULL)
//							break;
//					}
//					newtemp = (PointNode*)malloc(sizeof(PointNode));
//					if(temp != NULL)
//					{
//						*newtemp = *temp;
//						temp->x = x[i];
//						temp->y = y[i];
//						temp->z = z[i];
//						temp->ix = i;
//						temp->next = newtemp;
//					}
//					else
//					{
//						newtemp->x = x[i];
//						newtemp->y = y[i];
//						newtemp->z = z[i];
//						newtemp->ix = i;
//						newtemp->next = 0;
//						newtemp->count = 1;
//						prev->next = newtemp;
//						
//					}
//				}
//
//			}
//	
//		}
//	}
//
//
//
//
//}

/*! \fn void SimplePointSet::calculateNormals()
 *  \brief This function calculates the normal vector at each point and stores vector components in nx, ny, nz
 *  This works by findin two crossing vectors by stepping forward and back in x and y, then doing the cross
 *  product of the two vectors.  Since the projection has gaps of several pixels, the algorithm searches in an
 *  expanding code in -x, +x, -y, +y for a valid point to use for the crossing vectors
 *  \exception 
 *  \return void
 */
void SimplePointSet::calculateNormals()
{

	// This specifies the search pattern (to find a valid point) in the pixel array
	int searchx[] = {-1,-2,-1,-1,-3,-2,-2,-2,-2,-4,-3,-3,-3,-3,-2,-2,-5,-4,-4,-6};
	int searchy[] = {0 ,0 , 1,-1, 0, 1,-1, 2,-2, 0, 1,-1, 2,-2, 3,-2, 0, 1,-1, 0};

	//int searchx[] = {-1,-2,-3,-4,-5,-6,-2,-3,-4,-5,-6,-2,-3,-4,-5,-6,-3,-4,-3,-4};
	//int searchy[] = { 0 ,0 ,0, 0, 0, 0, 1, 1, 1, 1, 1,-1,-1,-1,-1,-1,-2,-2, 2, 2};
	int numsearch = 20;

	// set up subx and addx for use in calculating normals - see below
	// calculate vectors for normal calculation
	// u is point to right(+x) of current point minus point to left of current point(-x)
	// v is point below(+y) current point minus point above current point(-y)
	// if either of these is beyond the grid (we're on edge), use current point instead
	// this is what the addx, subx, addy, suby is doing

	int i,j, k;
	int normix;
	int x1, x2, x3, x4, y1, y2, y3, y4;
	float a1, a2, a3, b1, b2, b3;
	DVector normal(3);

	for(i=0; i<rows; i++)
	{
		for(j=0; j<cols; j++)
		{

			if (points[i*cols + j].count > 0)
			{
				x1=j-1;
				y1=i;
				x2=j+1;
				y2=i;
				x3=j;
				y3=i-1;
				x4=j;
				y4=i+1;

				if(j==0)
					x1=j;
				if(j==cols)
					x2=j;
				if(i==0)
					y3=i;
				if(i==rows)
					y4=i;

				int basex = 0;
				int basey = 0;
				if(points[y1*cols+(x1)].count < 1)
				{
					basex = x1;
					basey = y1;
					for(k=0; k<numsearch; k++)
					{
						x1 = basex + searchx[k];
						y1 = basey + searchy[k];
						if(points[y1*cols+(x1)].count > 0)
							break;
					}
					if(k == numsearch)
					{
						//points[i*cols+j].count = 0.0;
						continue;
					}
				}
				if(points[y2*cols+(x2)].count < 1)
				{
					basex = x2;
					basey = y2;
					for(k=0; k<numsearch; k++)
					{
						x2 = basex - searchx[k];
						y2 = basey + searchy[k];
						if(points[y2*cols+(x2)].count > 0)
							break;
					}
					if(k == numsearch)
					{
						//points[i*cols+j].count = 0.0;
						continue;
					}
				}
				if(points[y3*cols+(x3)].count < 1)
				{
					basex = x3;
					basey = y3;
					for(k=0; k<numsearch; k++)
					{
						x3 = basex + searchy[k];
						y3 = basey + searchx[k];
						if(points[y3*cols+(x3)].count > 0)
							break;
					}
					if(k == numsearch)
					{
						//points[i*cols+j].count = 0.0;
						continue;
					}
				}
				if(points[y4*cols+(x4)].count < 1)
				{
					basex = x4;
					basey = y4;
					for(k=0; k<numsearch; k++)
					{
						x4 = basex - searchy[k];
						y4 = basey + searchx[k];
						if(points[y4*cols+(x4)].count > 0)
							break;
					}
					if(k == numsearch)
					{
						//points[i*cols+j].count = 0.0;
						continue;
					}
				}
				if((x1 == x2 && y1==y2) || (x3==x4 && y3==y4))
				{
					points[i*cols+j].count = 0;
					continue;
					
				}

				a1 = points[y1*cols+(x1)].x - points[y2*cols+(x2)].x;
				a2 = points[y1*cols+(x1)].y - points[y2*cols+(x2)].y;
				a3 = points[y1*cols+(x1)].z - points[y2*cols+(x2)].z;
				b1 = points[y3*cols+(x3)].x - points[y4*cols+(x4)].x;
				b2 = points[y3*cols+(x3)].y - points[y4*cols+(x4)].y;
				b3 = points[y3*cols+(x3)].z - points[y4*cols+(x4)].z;

				// do cross product of the two vectors to get the normal (not normalized)
				
				normix = points[i*cols+j].ix;

				// Make sure we cross in the correct order so Z is up
				if((a1 > 0 && b2 > 0) || a1 <= 0 && b2 <= 0)
				{
					nx[normix] = a2*b3 - a3*b2;
					ny[normix] = a3*b1 - a1*b3;
					nz[normix] = a1*b2 - a2*b1;
				}
				else
				{
					nx[normix] = b2*a3 - b3*a2;
					ny[normix] = b3*a1 - b1*a3;
					nz[normix] = b1*a2 - b2*a1;

				}


				if(nz[normix] != 0 ||  nx[normix] != 0 || ny[normix] != 0)
				{
					if(nz[normix] < nx[normix] || nz[normix] < ny[normix])
						normalize(&nx[normix],&ny[normix],&nz[normix]);
				}

				normalize(&nx[normix],&ny[normix],&nz[normix]);

			}
		}
	}
}

/*! \fn bool SimplePointSet::CopySubset(SimplePointSet &pset, bool *keep)
 *  \brief This function plots two PointSets into a single VRML file and draws lines between point correspondences
 *  \param pset - The pointset from which to copy the points
 *  \param keep - boolean mask of valid points
 *  \exception 
 *  \return boolean success
 */
bool SimplePointSet::CopySubset(SimplePointSet &pset, bool *keep)
{
	// Count the points.
	numPoints = 0;
	for (int i=0;i<pset.numPoints;i++) if (keep[i]) numPoints++;

	bool ok = Allocate(numPoints, pset.numExtraFields+3);

	if ( !ok ) 
	{
		return false;
	}
	init();

	// Copy the attributes.
	// bounds = pset.bounds;
	numExtraFields = pset.numExtraFields;
	pSetInfo.zone = pset.pSetInfo.zone;
	pSetInfo.coord = pset.pSetInfo.coord;
	pSetInfo.spacing = pset.pSetInfo.spacing;

	xoff = pset.xoff;
	yoff = pset.yoff;
	zoff = pset.zoff;

	populated = true;

	// Copy the data.
	int ct=0;
	for (int i=0;i<numExtraFields;i++) 
	{
		foff[i] = pset.foff[i];
		fmin[i] = FLT_MAX;
		fmax[i] = -FLT_MAX;
		strcpy(labels[i], pset.labels[i]);
	}

	bounds.xmin = FLT_MAX;
	bounds.xmax = -FLT_MAX;
	bounds.ymin = FLT_MAX;
	bounds.ymax = -FLT_MAX;
	bounds.zmin = FLT_MAX;
	bounds.zmax = -FLT_MAX;

	for (int i=0;i<pset.numPoints;i++)
	{
		if (keep[i])
		{
			x[ct] = pset.x[i];
			y[ct] = pset.y[i];
			z[ct] = pset.z[i];
			
			if (x[ct] + xoff < bounds.xmin) bounds.xmin = x[ct] + xoff;
			if (x[ct] + xoff > bounds.xmax) bounds.xmax = x[ct] + xoff;
			if (y[ct] + yoff < bounds.ymin) bounds.ymin = y[ct] + yoff;
			if (y[ct] + yoff > bounds.ymax) bounds.ymax = y[ct] + yoff;
			if (z[ct] + zoff < bounds.zmin) bounds.zmin = z[ct] + zoff;
			if (z[ct] + zoff > bounds.zmax) bounds.zmax = z[ct] + zoff;		
			for (int j=0;j<numExtraFields;j++) 
			{
				fields[j][ct] = pset.fields[j][i];
				if (fields[j][ct] < fmin[j]) fmin[j] = fields[j][ct] + foff[j];
				if (fields[j][ct] > fmax[j]) fmax[j] = fields[j][ct] + foff[j];
			}
			ct++;
		}
	}

	maxVals[0] = bounds.xmax;
	maxVals[1] = bounds.ymax;
	maxVals[2] = bounds.zmax;

	minVals[0] = bounds.xmin;
	minVals[1] = bounds.ymin;
	minVals[2] = bounds.zmin;

	offsets[0] = xoff;
	offsets[1] = yoff;
	offsets[2] = zoff;

	for ( int i=0; i < numExtraFields; ++i )
	{
		offsets[i+3] = foff[i];
	}

	return true;
}

/*! \fn void SimplePointSet::createSpecialVRML(char* path, PointSet* ps1)
 *  \brief This function plots the number of points in a given pixel (of the points data structure) and color codes them
 *  \param path - the filename in which to store the VRML file
 *  \param ps1 - The pointset to convert to VRML
 *  \exception 
 *  \return void
 */
void SimplePointSet::createSpecialVRML(char* path, PointSet* ps1) 
{
	FILE *fxx;
	int i, numPrinted=0;
	float r, g, b;

	// colored point cloud output
	fxx = fopen( path, "w+" );
	fprintf(fxx, "#VRML V1.0 ascii\n\nSeparator {\nMaterial {\n	diffuseColor [");

	for (i=0; i<ps1->cols*ps1->rows; i++) 
	{
			if(ps1->points[i].count==1)
			{
				b = 0xFF;
				g = 0.0;
				r = 0.0;
			}
			if(ps1->points[i].count==2)
			{
				b = 0.0;
				g = 0xFF;
				r = 0.0;
			}
			if(ps1->points[i].count==3)
			{
				b = 0.0;
				g = 0.0;
				r = 0xFF;
			}
			if(ps1->points[i].count>=4)
			{
				b = 0xFF;
				g = 0xFF;
				r = 0.0;
			}
			if(ps1->points[i].count>0)
			{
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", r, g, b);
				numPrinted++;
			}
			
	}

	fprintf(fxx, "  ]\n}\n\nMaterialBinding {\n	value PER_VERTEX_INDEXED\n}\n\n  Coordinate3 {\n	  point [");
	for (i=0; i<ps1->cols*ps1->rows; i++)
	{ 
			if(ps1->points[i].count>0)
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps1->points[i].x, ps1->points[i].y, ps1->points[i].z);
	}

	fprintf(fxx, "	  ]\n  }\n\n    SimplePointSet {\n      startIndex 0\n      numPoints %i\n    }\n}", numPrinted);



	fclose(fxx);
}

/*! \fn void SimplePointSet::createVRML(char* path, PointSet* ps1, bool shownormals, bool applyxf) 
 *  \brief This function converts the SimplePointSet into a VRML file
 *  \param path - the filename in which to store the VRML file
 *  \param ps1 - The pointset to convert to VRML
 *  \param shownormals - If true, the normals are shown as small lines indicating the vector direction
 *  \param applyxf - If true, the pointsettransform is applied before plotting out the points
 *  \exception 
 *  \return void
 */
void SimplePointSet::createVRML(char* path, PointSet* ps1, bool shownormals, bool applyxf) 
{
	FILE *fxx;
	int i, numPrinted=0;
	float r, g, b;

	// colored point cloud output
	fxx = fopen( path, "w+" );
	fprintf(fxx, "#VRML V1.0 ascii\n\nSeparator {\nMaterial {\n	diffuseColor [");

	for (i=0; i<ps1->numPoints; i++) 
	{

		r = (float)ps1->i[i]/5000.0f;
		g = (float)ps1->i[i]/5000.0f;
		b = (float)ps1->i[i]/5000.0f;

		fprintf(fxx, "%6.8f %6.8f %6.8f,\n", r, g, b);
		numPrinted++;

	}
	float x,y,z;
	fprintf(fxx, "  ]\n}\n\nMaterialBinding {\n	value PER_VERTEX_INDEXED\n}\n\n  Coordinate3 {\n	  point [");
	for (i=0; i<ps1->numPoints; i++)
	{ 
		if(applyxf)
		{
			x = ps1->x[i];
			y = ps1->y[i];
			z = ps1->z[i];
			ps1->transform.rotateTranslateVector(x,y,z);
			x = x - ps1->transform.tx;
			y = y - ps1->transform.ty;
			z = z - ps1->transform.tz;

			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
		}
		else
		{
			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps1->x[i], ps1->y[i], ps1->z[i]);
		}
	}

	fprintf(fxx, "	  ]\n  }\n\n    SimplePointSet {\n      startIndex 0\n      numPoints %i\n    }\n}", numPrinted);

	if(shownormals)
	{
		double sf = 2;
		
		for (i=0; i<ps1->numPoints; i+=5)
		{
			if(!(ps1->nx[i]==0 && ps1->ny[i]==0  && ps1->nz[i]==0) )
			{
				fprintf(fxx, "\nCoordinate3 {\n	point [");
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps1->x[i], ps1->y[i], ps1->z[i]);
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps1->x[i]+ sf*ps1->nx[i], ps1->y[i]+ sf*ps1->ny[i], ps1->z[i]+ sf*ps1->nz[i]);
				fprintf(fxx, "] \n} \n IndexedLineSet{coordIndex[0 1]}\n");
			}
		}
	}

	fclose(fxx);
}

/*! \fn void SimplePointSet::createVRML(char* path, PointSet** psets, int numPointSets, int* hues, bool shownormals, bool applyxf) 
 *  \brief This function converts numerous PointSets into a single VRML file, coloring the sets using hues
 *  \param path - the filename in which to store the VRML file
 *  \param psets - Pointer to the list of pointsets to plot into the VRML file
 *  \param numPointSets - The number of pointsets to plot
 *  \param hues - an array of integers indicating the color in which to paint the corresponding point set
 *   Color is 0-8 where binary is 0b00000rgb
 *  \param shownormals - If true, the normals are shown as small lines indicating the vector direction
 *  \param applyxf - If true, the pointsettransform is applied before plotting out the points
 *  \exception 
 *  \return void
 */
void SimplePointSet::createVRML(char* path, PointSet** psets, int numPointSets, int* hues, bool shownormals, bool applyxf) 
{
	FILE *fxx;
	int i,n, numPrinted=0;
	float r, g, b;

	// colored point cloud output
	fxx = fopen( path, "w+" );
	fprintf(fxx, "#VRML V1.0 ascii\n\nSeparator {\nMaterial {\n	diffuseColor [");

	float inten = 0;

	for(n=0; n<numPointSets; n++)
	{
		for (i=0; i<psets[n]->numPoints; i++) 
		{
			inten = psets[n]->i[i]/5000.0f;
			r=g=b=0;
			if(!(hues[n] & 0x00000001)) r = inten;
			if(!(hues[n] & 0x00000002)) g = inten;
			if(!(hues[n] & 0x00000004)) b = inten;
		
			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", r, g, b);
			numPrinted++;
		}

	}
	float x,y,z;
	fprintf(fxx, "  ]\n}\n\nMaterialBinding {\n	value PER_VERTEX_INDEXED\n}\n\n  Coordinate3 {\n	  point [");
	
	for(n=0; n<numPointSets; n++)
	{
	
		for (i=0; i<psets[n]->numPoints; i++)
		{ 
			if(applyxf)
			{
				x = psets[n]->x[i];
				y = psets[n]->y[i];
				z = psets[n]->z[i];
				psets[n]->transform.rotateTranslateVector(x,y,z);
	
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
			}
			else
			{
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", psets[n]->x[i], psets[n]->y[i], psets[n]->z[i]);
			}
		}

	}

	fprintf(fxx, "	  ]\n  }\n\n    SimplePointSet {\n      startIndex 0\n      numPoints %i\n    }\n}", numPrinted);

	if(shownormals)
	{
		double sf = 2;

		for(n=0; n<numPointSets; n++)
		{
			for (i=0; i<psets[n]->numPoints; i+=5)
			{
				if(!(psets[n]->nx[i]==0 && psets[n]->ny[i]==0  && psets[n]->nz[i]==0) )
				{
					fprintf(fxx, "\nCoordinate3 {\n	point [");
					fprintf(fxx, "%6.8f %6.8f %6.8f,\n", psets[n]->x[i], psets[n]->y[i], psets[n]->z[i]);
					fprintf(fxx, "%6.8f %6.8f %6.8f,\n", psets[n]->x[i]+ sf*psets[n]->nx[i], psets[n]->y[i]+ sf*psets[n]->ny[i], psets[n]->z[i]+ sf*psets[n]->nz[i]);
					fprintf(fxx, "] \n} \n IndexedLineSet{coordIndex[0 1]}\n");
				}
			}
		}
	}

	fclose(fxx);
}

/*! \fn void SimplePointSet::createVRML(char* path, PointSet* ps1, PointSet* ps2, bool shownormals, bool applyxf) 

 *  \brief This function plots two PointSets into a single VRML file
 *  \param path - the filename in which to store the VRML file
 *  \param ps1 - The first pointset
 *  \param ps1 - The second pointset
 *  \param shownormals - If true, the normals are shown as small lines indicating the vector direction
 *  \param applyxf - If true, the pointsettransform is applied before plotting out the points
 *  \exception 
 *  \return void
 */
void SimplePointSet::createVRML(char* path, PointSet* ps1, PointSet* ps2, bool shownormals, bool applyxf) 
{
	FILE *fxx;
	int i, numPrinted=0;
	float r, g, b;

	// colored point cloud output
	fxx = fopen( path, "w+" );
	fprintf(fxx, "#VRML V1.0 ascii\n\nSeparator {\nMaterial {\n	diffuseColor [");

	for (i=0; i<ps1->numPoints; i++) 
	{

		r = (float)ps1->i[i]/5000.0f;
		g = (float)ps1->i[i]/5000.0f;
		b = (float)ps1->i[i]/5000.0f;

		fprintf(fxx, "%6.8f %6.8f %6.8f,\n", r, g, b);
		numPrinted++;

	}
	for (i=0; i<ps2->numPoints; i++) 
	{

		r = 0.0f;
		g = (float)ps2->i[i]/3000.0f;
		b = (float)ps2->i[i]/3000.0f;

		fprintf(fxx, "%6.8f %6.8f %6.8f,\n", r, g, b);
		numPrinted++;

	}
	fprintf(fxx, "  ]\n}\n\nMaterialBinding {\n	value PER_VERTEX_INDEXED\n}\n\n  Coordinate3 {\n	  point [");
	
	float x,y,z;
	for (i=0; i<ps1->numPoints; i++)
	{ 
		if(applyxf)
		{
			x = ps1->x[i];
			y = ps1->y[i];
			z = ps1->z[i];
			ps1->transform.rotateTranslateVector(x,y,z);
			//x = x - ps1->transform.tx;
			//y = y - ps1->transform.ty;
			//z = z - ps1->transform.tz;

			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
		}
		else
		{
			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps1->x[i], ps1->y[i], ps1->z[i]);
		}
	}

	for (i=0; i<ps2->numPoints; i++)
	{ 
		if(applyxf)
		{
			x = ps2->x[i];
			y = ps2->y[i];
			z = ps2->z[i];
			ps2->transform.rotateTranslateVector(x,y,z);
			//x = x - ps1->transform.tx;
			//y = y - ps1->transform.ty;
			//z = z - ps1->transform.tz;

			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
		}
		else
		{
			fprintf(fxx, "%6.8f %6.8f %6.8f,\n", ps2->x[i], ps2->y[i], ps2->z[i]);
		}
	}
	fprintf(fxx, "	  ]\n  }\n\n    SimplePointSet {\n      startIndex 0\n      numPoints %i\n    }\n}", numPrinted);

	if(shownormals)
	{
		double sf = 2;
		
		for (i=0; i<ps1->numPoints; i+=5)
		{
			if(!(ps1->nx[i]==0 && ps1->ny[i]==0  && ps1->nz[i]==0) )
			{
				x = ps1->x[i];
				y = ps1->y[i];
				z = ps1->z[i];
				if(applyxf)
				{	
					ps1->transform.rotateTranslateVector(x,y,z);
	/*				x = x - ps1->transform.tx;
					y = y - ps1->transform.ty;
					z = z - ps1->transform.tz;*/
				}		
				fprintf(fxx, "\nCoordinate3 {\n	point [");
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x + sf*ps1->nx[i], y + sf*ps1->ny[i], z + sf*ps1->nz[i]);
				fprintf(fxx, "] \n} \n IndexedLineSet{coordIndex[0 1]}\n");
			}
		}
		for (i=0; i<ps2->numPoints; i+=5)
		{
			if(!(ps2->nx[i]==0 && ps2->ny[i]==0  && ps2->nz[i]==0) )
			{
				x = ps2->x[i];
				y = ps2->y[i];
				z = ps2->z[i];
				if(applyxf)
				{	
					ps2->transform.rotateTranslateVector(x,y,z);
					//x = x - ps1->transform.tx;
					//y = y - ps1->transform.ty;
					//z = z - ps1->transform.tz;
				}
				fprintf(fxx, "\nCoordinate3 {\n	point [");
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x, y, z);
				fprintf(fxx, "%6.8f %6.8f %6.8f,\n", x + sf*ps2->nx[i], y + sf*ps2->ny[i], z + sf*ps2->nz[i]);
				fprintf(fxx, "] \n} \n IndexedLineSet{coordIndex[0 1]}\n");
			}
		}
	}

	fclose(fxx);
}

/*! \fn void SimplePointSet::createCorrelatedVRML(char* path, PointSet* ps1, PointSet* ps2, bool applyxf )
 *  \brief This function plots two PointSets into a single VRML file and draws lines between point correspondences
 *  \param path - the filename in which to store the VRML file
 *  \param ps1 - The first pointset
 *  \param ps1 - The second pointset
 *  \param applyxf - If true, the pointsettransform is applied before plotting out the points
 *  \exception 
 *  \return void
 */
void SimplePointSet::createCorrelatedVRML(char* path, PointSet* ps1, PointSet* ps2, bool applyxf ) 
{
	FILE *fxp;
	int i, numPrinted=0;
	float r, g, b;
	int samp=0;

	// colored point cloud output
	fxp = fopen( path, "w+" );
	fprintf(fxp, "#VRML V1.0 ascii\n\nSeparator {\nMaterial {\n	diffuseColor [");

	for (i=0; i<ps1->numPoints; i++) 
	{
			if(ps1->sampled[i] == 1)
			{
				r = 0xff;
				g = 0x00;
				b = 0x00;
				samp++;
			}
			else
			{
				r = (float)ps1->i[i]/5000.0f;
				g = (float)ps1->i[i]/5000.0f;
				b = (float)ps1->i[i]/5000.0f;
			}
			
			fprintf(fxp, "%6.8f %6.8f %6.8f,\n", r, g, b);
			numPrinted++;

	}

	for (i=0; i<ps2->numPoints; i++) 
	{

		r =  0.0f;
		g = (float)ps2->i[i]/3000.0f;
		b = (float)ps2->i[i]/3000.0f;

		fprintf(fxp, "%6.8f %6.8f %6.8f,\n", r, g, b);
		numPrinted++;

	}
	fprintf(fxp, "  ]\n}\n\nMaterialBinding {\n	value PER_VERTEX_INDEXED\n}\n\n  Coordinate3 {\n	  point [");
	
	float x,y,z;
	for (i=0; i<ps1->numPoints; i++)
	{ 
		if(applyxf)
		{
			x = ps1->x[i];
			y = ps1->y[i];
			z = ps1->z[i];
			ps1->transform.rotateTranslateVector(x,y,z);
			x = x - ps1->transform.tx;
			y = y - ps1->transform.ty;
			z = z - ps1->transform.tz;

			fprintf(fxp, "%6.8f %6.8f %6.8f,\n", x, y, z);
		}
		else
		{
			fprintf(fxp, "%6.8f %6.8f %6.8f,\n", ps1->x[i], ps1->y[i], ps1->z[i]);
		}
	}

	for (i=0; i<ps2->numPoints; i++)
	{ 
		if(applyxf)
		{
			x = ps2->x[i];
			y = ps2->y[i];
			z = ps2->z[i];
			ps2->transform.rotateTranslateVector(x,y,z);
			x = x - ps1->transform.tx;
			y = y - ps1->transform.ty;
			z = z - ps1->transform.tz;

			fprintf(fxp, "%6.8f %6.8f %6.8f,\n", x, y, z);
		}
		else
		{
			fprintf(fxp, "%6.8f %6.8f %6.8f,\n", ps2->x[i], ps2->y[i], ps2->z[i]);
		}
	}
	fprintf(fxp, "	  ]\n  }\n\n    SimplePointSet {\n      startIndex 0\n      numPoints %i\n    }\n}", numPrinted);

	
	for (i=0; i<ps1->numPoints; i++)
	{
			if(ps1->cindex[i] >=0 && ps1->cindex[i]< ps1->numPoints)
			{
				fprintf(fxp, "\nCoordinate3 {\n	point [");
				if(applyxf)
				{
					x = ps1->x[i];
					y = ps1->y[i];
					z = ps1->z[i];
					ps1->transform.rotateTranslateVector(x,y,z);
					x = x - ps1->transform.tx;
					y = y - ps1->transform.ty;
					z = z - ps1->transform.tz;

					fprintf(fxp, "%6.8f %6.8f %6.8f,\n", x, y, z);

					x = ps2->x[ps1->cindex[i]];
					y = ps2->y[ps1->cindex[i]];
					z = ps2->z[ps1->cindex[i]];
					ps2->transform.rotateTranslateVector(x,y,z);
					x = x - ps1->transform.tx;
					y = y - ps1->transform.ty;
					z = z - ps1->transform.tz;

					fprintf(fxp, "%6.8f %6.8f %6.8f,\n", x, y, z);

				}
				else
				{
					fprintf(fxp, "%6.8f %6.8f %6.8f,\n", ps1->x[i], ps1->y[i], ps1->z[i]);
					fprintf(fxp, "%6.8f %6.8f %6.8f,\n", ps2->x[ps1->cindex[i]], ps2->y[ps1->cindex[i]], ps2->z[ps1->cindex[i]]);
				}
				fprintf(fxp, "] \n} \n IndexedLineSet{coordIndex[0 1]}\n");
			}
		
		
	}

	fclose(fxp);
}

/*! \fn void SimplePointSet::ApplyNoise(float sigma)
 *  \brief This function applies noise in the z dimension to the pointset
 *  \param sigma - the standard deviation of the noise to apply
 *  \exception 
 *  \return void
 */
void SimplePointSet::ApplyNoise(float sigma)
{
	if (sigma == 0.0) return;
	long seed = 0;
	for (int m=0;m<numPoints;m++)
	{
		float dz = gasdev(&seed) * sigma;
		z[m] += dz;
	}
}

/*! \fn bool SimplePointSet::ConvertASCIIFolder(char *folderName, int utmZone, int numExtraVals, bool *useOffset, int numHeaderLines, bool computeSpacing)
 *  \brief This function plots two PointSets into a single VRML file and draws lines between point correspondences
 *  \param folderName - folder path
 *  \param utmZone - the utmZone
 *  \param numExtraVals - The number of columns to read in (above the standard x,y,z
 *  \param useOffset - boolean array of which columns to use offsets
 *  \param numHeaderLines - The number of ASCII header lines to ignore
 *  \param  computeSpacing - if true, automatically determine the average spacing 
 *  \exception 
 *  \return boolean success
 */
bool SimplePointSet::ConvertASCIIFolder(char *folderName, int utmZone, int numExtraVals, bool *useOffset, int numHeaderLines, bool computeSpacing)
{
	// Open the directory.
	DIR *dir = opendir(folderName);
	if (dir == NULL) return false;

	// Loop on all files in folder.
	while (1)
	{
		// Get the file name.
		dirent *dir_entry = readdir(dir);
		if (dir_entry == NULL) break;
		char fileName[1024];
		strcpy(fileName, dir_entry->d_name);
		if (fileName[0] == '.') continue;

		char newName[1024];
		sprintf(newName, "%s\\%s", folderName, fileName);
		if ( strcmp(&fileName[dir_entry->d_namlen - 4], ".xyz") == 0 )
			ConvertASCIIFile(newName, utmZone, numExtraVals, useOffset, numHeaderLines, computeSpacing);
	}

	// Close the directory.
	closedir(dir);

	return true;
}


/*! \fn bool SimplePointSet::ConvertASCIIFile(char *fileName, int utmZone, int numExtraVals, bool *useOffset, int numHeaderLines, bool computeSpacing)
 *  \brief This function plots two PointSets into a single VRML file and draws lines between point correspondences
 *  \param fileName - filename
 *  \param utmZone - the utmZone
 *  \param numExtraVals - The number of columns to read in (above the standard x,y,z
 *  \param useOffset - boolean array of which columns to use offsets
 *  \param numHeaderLines - The number of ASCII header lines to ignore
 *  \param  computeSpacing - if true, automatically determine the average spacing 
 *  \exception 
 *  \return void
 */
bool SimplePointSet::ConvertASCIIFile(char *fileName, int utmZone, int numExtraVals, bool *useOffset, int numHeaderLines, bool computeSpacing)
{

	SimplePointSet* ps = new SimplePointSet(fileName,numHeaderLines,' ',numExtraVals);

	ps->pSetInfo.coord = 1;
	ps->pSetInfo.zone = utmZone;
	ps->pSetInfo.version = 1;

	// Get horizontal spacing.
	ps->pSetInfo.spacing = 0.0f;
	if (computeSpacing)
	{
		ps->pSetInfo.spacing = ps->GetHorizontalSpacing();
		if ( ps->pSetInfo.spacing < 0.0 )
		{
			printf("Error calculating grid spacing\n");
			return false;
		}
		printf( "Calculated horizontal spacing: %f for %s\n", ps->pSetInfo.spacing, fileName );
	}


	char fname[1024];
	int len = (int)strlen(fileName);
	strcpy(fname, fileName);
	sprintf(&fname[len-4], ".bpf\0");

	ps->writeBinaryFile(fname);

	delete ps;

	return true;
}


/*! \fn int floatCompare(const void *a, const void *b)
 *  \brief Utility function to compare floats
 *  \param a - first number
 *  \param b - second number for comparison
 *  \exception 
 *  \return int, result of comparison, -1 for less than, 1 for >=
 */
int floatCompare(const void *a, const void *b)
{
	float *aa = NULL, *bb = NULL;
	aa = (float *)a;
	bb = (float *)b;
	if (*aa < *bb)
		return -1;
	else
		return 1;
}

/*! \fn float SimplePointSet::GetHorizontalSpacing()
 *  \brief This function uses the raw point data to estimate the average point spacing
 *  \exception 
 *  \return the estimated point spacing as a float
 */
float SimplePointSet::GetHorizontalSpacing()
{
	double pi = 3.1415926535;
	double twopi = 2*pi;

	long seed=0;
	int numSamples = 500;//100;
	float *distances = NULL;
	try
	{
		distances = new float[numSamples];
	}
	catch (bad_alloc&)
	{
		printf("Insufficient memory computing spacing\n");
		return -1.0;
	}

	for (int m=0;m<numSamples;m++)
	{
		float minDist = FLT_MAX;
		float angle=0.0;
		int i = int(ran1(&seed) * double(this->numPoints-1));
		for (int j=0;j<numPoints;j++)
		{
			if (i == j) continue;
			float dx = fabs(x[i] - x[j]);
			float dy = fabs(y[i] - y[j]);
			float dist = max(dx,dy);
			if (dist < minDist)
			{
				minDist = dist;
				if ((y[i] - y[j]) == 0.0)
					angle = 0.0;
				else
					angle = atan((x[i] - x[j]) / (y[i] - y[j]));
			}
		}
		minDist = FLT_MAX;
		for (int j=0;j<numPoints;j++)
		{
			if (i == j) continue;
			float dx = fabs(x[i] - x[j]);
			float dy = fabs(y[i] - y[j]);
			float ang;
			if ((y[i] - y[j]) == 0.0)
				ang = 0.0;
			else
				ang = atan((x[i] - x[j]) / (y[i] - y[j]));
			float dang = fabs(ang - angle);
			if (dang > pi) dang -= (float)pi;
			if (dang < pi/4.0) continue;					// at least 45 degrees away
			float dist = max(dx,dy);
			if (dist < minDist)
			{
				minDist = dist;
			}
		}
		distances[m] = minDist;
	}
	qsort((void *)distances, numSamples, sizeof(float), floatCompare);
	float spacing = distances[numSamples/2];
	delete []distances;
	return spacing;
}