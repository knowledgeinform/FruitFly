package edu.jhuapl.nstd.util;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;


public class EllipsoidToMSL {
	private float[][] _ellipsoidMap;

	public EllipsoidToMSL() {
		readInEllipsoidMap();
	}

	private void readInEllipsoidMap() {
		String filename = "geoidHeight15min.img";
		int i, j, val;
		byte[] b = new byte[4];

		try {
			_ellipsoidMap = new float[1441][721];

			InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
			if (is == null) {
				System.out.println("Error: unable to open ellipsoid file: "+filename);
				return;
			}
			DataInputStream dis = new DataInputStream(is);

			for (j=720; j>=0; j--) {
				for (i=0; i<1441; i++) {
					try {
						dis.readFully(b);
						val = (((b[3] & 0xFF) << 24) 
							   + ((b[2] & 0xFF) << 16)
							   + ((b[1] & 0xFF) << 8)
							   + ((b[0] & 0xFF) << 0));
						_ellipsoidMap[i][j] = Float.intBitsToFloat(val);
					} catch ( EOFException eof ) {
						System.out.println ("ERROR: End of File; not enough data in ellipsoid map");
						break;
					}
				}
			}

			/*for (i=0; i<8; i++) {
				for (j=0; j<8; j++)
					printf("[%d,%d]=%f ", i, j, ellipsoidMap[i][j]);
				printf("\n");
			} */
			dis.close();
		} catch ( IOException ioe ) {
			System.out.println ("ERROR: error reading ellipsoid map");
			ioe.printStackTrace();
		}
	} 

	public double getGeoidHeight(double lat, double lon) {
		double swLat, swLon, topLat, leftLon, btmLat, rightLon;
		int col1, col2, row1, row2;
		float upLeft, upRight, btmLeft, btmRight;
		double left, right;
		double geoidHeight;
		double perRightOfLeft, perDownOfTop;

		if (lon < 0)
			lon += 360.0;
		if (lat<-90.0 || lat>90.0 || lon<0 || lon>360) {
			System.out.println("ERROR: lat/lon ("+lat+","+lon+") out of bounds");
			return 0.0;
		}

		swLat = ((int)(lat*4.0) / 4.0);	/* range -90 to 90 */
		swLon = ((int)(lon*4.0) / 4.0);	/* range 0 to 360 */

		col1 = (int)Math.round( (double)(swLon*4.0) );
		col2 = col1+1;
		row1 = (int)Math.round( (double)((swLat+90.0)*4.0) );
		row2 = row1+1;
		if (row2>720) {
			row1--;
			row2--;
		}
		if (col2>1440) {
			col1--;
			col2--;
		}

		topLat = ((row1/4.0) - 90.0);
		btmLat = ((row2/4.0) - 90.0);
		leftLon = (col1/4.0);
		rightLon = (col2/4.0);

		perRightOfLeft = (lon-leftLon) / (rightLon-leftLon);
		perDownOfTop = (lat-topLat) / (btmLat-topLat);

		upLeft = _ellipsoidMap[col1][row1];
		upRight = _ellipsoidMap[col2][row1];
		btmLeft = _ellipsoidMap[col1][row2];
		btmRight = _ellipsoidMap[col2][row2];


		left = (upLeft*(1.0-perDownOfTop)) + (btmLeft*perDownOfTop);
		right = (upRight*(1.0-perDownOfTop)) + (btmRight*perDownOfTop);
		geoidHeight = (left*(1.0-perRightOfLeft)) + (right*perRightOfLeft);

		System.out.println("Geoid Height("+lat+","+lon+") found as "+geoidHeight);

		return geoidHeight;
	}


	public static void main(String[] args) {

		EllipsoidToMSL e2m = new EllipsoidToMSL();
		double val = e2m.getGeoidHeight(32.765, -76.345);
		
	}

}
