/**
	\class DTEDPoint
	\author John Humphreys

	\brief A DTEDPoint object holds geolocation information about a specific point, including
	lat/lon, UTM location, UTM normal vectors, and a boolean array pertaining to visibility of
	this point from certain ComPoints.

	\note Copyright (c) 2008 Johns Hopkins University
	\note Applied Physics Laboratory.  All Rights Reserved
*/

package edu.jhuapl.nstd.swarm.util;

public class DTEDPoint
{
	/**
	 Decimal latitude of this point, degrees
	*/
	public double lat;

	/**
	 Decimal longitude of this point, degrees
	*/
	public double lon;

	/**
	 Decimal UTM-X location of this point, meters.  Calculated automatically if 
	 non-default constructor is used.
	*/
	public double x;

	/**
	 Decimal UTM-Y location of this point, meters.  Calculated automatically if 
	 non-default constructor is used.
	*/
	public double y;

	/**
	 Height above sea level of this point, meters
	*/
	public double z;

	/**
	 UTM X-component of the normal vector of this point
	*/
	public double nx;

	/**
	 UTM Y-component of the normal vector of this point
	*/
	public double ny;

	/**
	 UTM Z-component of the normal vector of this point
	*/
	public double nz;

	/**
	 * Angle from vertical to the normal vector at this point
	 */
	public double normAngD;

	/**
	 * Boolean flag whether this point is landable, according to normal vectors from DTED
	 */
	public boolean landable;

	/**
	 Boolean to store whether this point is visible from a list of "light sources".
	*/
	public boolean visible;
	
	/**
	 * Conversion from degrees to radians
	 */
	private static double deg2rad = 3.14159265358979323/180.0;

	/**
	 * Just arrays used to simulate a pass-by-reference call to calcXY.  Put here to avoid reinstantiation every time
	 * and save some processing time (maybe?)
	 */
	private static double[] xList = new double[1];
	private static double[] yList = new double[1];


	/**
	 Default constructor, no location set.  Visibility all set to false
	
	 \return
	*/
	public DTEDPoint() {
		visible = false;
		landable = false;
	}

	/**
	 Constructor with lat/lon/optional z inputs for this point.  Sets visibility to false.
	 Calls calcXY and sets UTM locations for this point.
	
	 \param newLat - The latitude value to set this point at, specified between -90 and 90 degrees
	 \param newLon - The longitude value to set this point at, specified bewteen -180 and 180 degrees
	 \param newZ - The ASL height to set this point at, specified in meters
	 \return
	*/
	public DTEDPoint (double newLat, double newLon, double newZ) {
		visible = false;
		landable = false;
		setLatLonZ(newLat, newLon, newZ);
	}

	/**
	 Establishes values position from lat/lon/optional z inputs for this point.  Sets visibility to false.
	 Calls calcXY and sets UTM locations for this point.
	
	 \param newLat - The latitude value to set this point at, specified between -90 and 90 degrees
	 \param newLon - The longitude value to set this point at, specified bewteen -180 and 180 degrees
	 \param newZ - The ASL height to set this point at, specified in meters
	 \return
	*/
	public void setLatLonZ(double newLat, double newLon, double newZ) {
		lat = newLat;
		lon = newLon;
		z = newZ;

		// Calculate and set UTM position based on lat/lon
		calcXY(lat, lon, xList, yList);
		x = xList[0];
		y = yList[0];
		
		// Default visibility to false
		visible = false;
	}

	/**
	 Static method to convert latitude/longitude points to UTM X and Y.  This is a modifed UTM value - 
	 the negative values of Northing will remain negative and not be offset to facililate distance calculations
	
	 \param lat - The latitude position to convert to UTM, specified between -90 and 90 degrees
	 \param lon - The longitude position to convert to UTM, specified between -180 and 180 degrees
	 \param x - Upon return, the UTM-X position of the point, meters
	 \param y - Upon return, the UTM-Y position of the point, meters
	 \return
	*/
	public static void calcXY (double lat, double lon, double x[], double y[]) {
		// Adapted from http://www.gpsy.com/gpsinfo/geotoutm/gantz/LatLong-UTMconversion.cpp.txt
		double a = 6378135;		// equitorial radius, meters
		double eccSquared = Math.pow (0.081, 2.0);		// earth eccentricity
		double k0 = 0.9996;

		double LongOrigin;
		double eccPrimeSquared;
		double N, T, C, A, M;
		
		//Make sure the longitude is between -180.00 .. 179.9
		double LongTemp = (lon+180)-(int)((lon+180)/360)*360-180; // -180.00 .. 179.9;

		double LatRad = lat*deg2rad;
		double LongRad = LongTemp*deg2rad;
		double LongOriginRad;
		int    ZoneNumber;

		ZoneNumber = (int)((LongTemp + 180) / 6) + 1;
	  
		if( lat >= 56.0 && lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0 )
			ZoneNumber = 32;

	  // Special zones for Svalbard
		if( lat >= 72.0 && lat < 84.0 ) 
		{
		  if(      LongTemp >= 0.0  && LongTemp <  9.0 ) ZoneNumber = 31;
		  else if( LongTemp >= 9.0  && LongTemp < 21.0 ) ZoneNumber = 33;
		  else if( LongTemp >= 21.0 && LongTemp < 33.0 ) ZoneNumber = 35;
		  else if( LongTemp >= 33.0 && LongTemp < 42.0 ) ZoneNumber = 37;
		 }
		LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
		LongOriginRad = LongOrigin * deg2rad;

		//compute the UTM Zone from the latitude and longitude
		eccPrimeSquared = (eccSquared)/(1-eccSquared);

		N = a/Math.sqrt(1-eccSquared*Math.sin(LatRad)*Math.sin(LatRad));
		T = Math.tan(LatRad)*Math.tan(LatRad);
		C = eccPrimeSquared * Math.cos(LatRad) * Math.cos(LatRad);
		A = Math.cos(LatRad) * (LongRad - LongOriginRad);

		M = a*((1	- eccSquared/4		- 3*eccSquared*eccSquared/64	- 5*eccSquared*eccSquared*eccSquared/256)*LatRad 
					- (3*eccSquared/8	+ 3*eccSquared*eccSquared/32	+ 45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(2*LatRad)
										+ (15*eccSquared*eccSquared/256 + 45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(4*LatRad) 
										- (35*eccSquared*eccSquared*eccSquared/3072)*Math.sin(6*LatRad));
		
		x[0] = (double)(k0*N*(A+(1-T+C)*A*A*A/6
						+ (5-18*T+T*T+72*C-58*eccPrimeSquared)*A*A*A*A*A/120)
						+ 500000.0);

		y[0] = (double)(k0 * (M + N * Math.tan(LatRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
					 + (61-58*T+T*T+600*C-330*eccPrimeSquared)*A*A*A*A*A*A/720)));

		
		//if(lat < 0)
		//	y[0] += 10000000.0; //10000000 meter offset for southern hemisphere
	}
};
