/**
	\class DTEDObject
	\author John Humphreys

	\brief An object to hold the entirety of the data from a DTED file, including a list of
	DTEDPoint objects to represent each point and some general information about the dataset.

	\note Copyright (c) 2008 Johns Hopkins University
	\note Applied Physics Laboratory.  All Rights Reserved
*/

package edu.jhuapl.nstd.swarm.util;

public class DTEDObject {

	/**
	 DTED file that this DTEDData object has/will read from.
	*/
	public String filename;

	/**
	 List of DTEDPoint objects that represent each datapoint from the DTED file
	*/
	public DTEDPoint dataArray[][];

	/** 
	 Longitude position of the anchor point for this data set
	*/
	public double anchorLon;

	/** 
	 Latitude position of the anchor point for this data set
	*/
	public double anchorLat;

	/**
	 Number of longitude points in the dataset
	*/
	public int numLon;

	/**
	 Number of latitude points in the dataset
	*/
	public int numLat;

	/**
	 Longitude resolution of data points, degrees
	*/
	public double resLon;

	/**
	 Approximation of the UTM - X resolution of this dataset, calculated based on the anchor point, meters
	*/
	public double resXest;

	/**
	 Latitude reslolution of data points, degrees
	*/
	public double resLat;

	/**
	 Approximation of the UTM - Y resolution of this dataset, calculated based on the anchor point, meters
	*/
	public double resYest;

	

	/**
	 Default constructor, no parameters set.
	
	 \return
	*/
	public DTEDObject(){
		dataArray = null;
	}
	
	/**
	 Loop through each DTEDPoint and calculate an approximated normal vector by forming
	 crossing vectors over this point.
	
	 \return
	*/
	public void calcNorms()
	{
		// Scroll to each DTEDPoint
		for (int j = 0; j < numLon; j++)
		{
			for (int i = 0; i < numLat; i++)
			{
				calcNormPoint(i, j);
			}
		}
	}

	/**
	 * Calculate normal vector at the provided index in the DTED point array
	 * 
	 * @param i i-th (latitude) index of the point to calculate at
	 * @param j j-th (longitude) index of the point to calculate at
	 * @return none
	 */
	public void calcNormPoint(int i, int j)
	{
		double dx1, dx2;
		double dy1, dy2;
		double dz1, dz2;
		int p1i, p1j, p2i, p2j, p3i, p3j, p4i, p4j;
		double nx_f, ny_f, nz_f;
		double nx, ny, nz;
		double len;

		// Choose points for crossing vectors
		// p1 and p2 form crossing vector in latitude/UTM-Y/i direction
		if (i > 0)
			p1i = i - 1;
		else
			p1i = i;
		p1j = j;

		if (i < numLat - 1)
			p2i = i + 1;
		else
			p2i = i;
		p2j = j;

		// p3 and p4 form crossing vector in longitude/UTM-X/j direction
		if (j > 0)
			p3j = j - 1;
		else
			p3j = j;
		p3i = i;

		if (j < numLon - 1)
			p4j = j + 1;
		else
			p4j = j;
		p4i = i;

		// Compute differences
		dx1 = dataArray[p2i][p2j].x - dataArray[p1i][p1j].x;
		dy1 = dataArray[p2i][p2j].y - dataArray[p1i][p1j].y;
		dz1 = dataArray[p2i][p2j].z - dataArray[p1i][p1j].z;

		dx2 = dataArray[p4i][p4j].x - dataArray[p3i][p3j].x;
		dy2 = dataArray[p4i][p4j].y - dataArray[p3i][p3j].y;
		dz2 = dataArray[p4i][p4j].z - dataArray[p3i][p3j].z;

		// Calculate normal vector
		nx_f = (dy1 * dz2) - (dy2 * dz1);
		ny_f = (dz1 * dx2) - (dz2 * dx1);
		nz_f = (dx1 * dy2) - (dx2 * dy1);

		// Normalize vectors
		len = Math.sqrt(nx_f * nx_f + ny_f * ny_f + nz_f * nz_f);
		nx = nx_f / len * (nz_f < 0 ? -1 : 1);
		ny = ny_f / len * (nz_f < 0 ? -1 : 1);
		nz = nz_f / len * (nz_f < 0 ? -1 : 1);

		// Calculate angle from vertical
		dataArray[i][j].normAngD = Math.acos(nz) * 180.0 / Math.PI;

		dataArray[i][j].nx = nx;
		dataArray[i][j].ny = ny;
		dataArray[i][j].nz = nz;
	}
};
