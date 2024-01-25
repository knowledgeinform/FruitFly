
package edu.jhuapl.nstd.swarm.comms;

import java.util.logging.*;

public class PositionData {
	private double _lat;
	private double _lon;
	private double _heading;
	private double _altitude;

	public PositionData(double lat, double lon, double heading, double alt) {
		_lat = lat;
		_lon = lon;
		_heading = heading;
		_altitude = alt;
	}

	public PositionData() {
		_lat = _lon = _heading = _altitude = -1;
	}

	public void setLat(double lat) { _lat = lat; }
	public void setLon(double lon) { _lon = lon; }
	public void setHeading(double heading) { _heading = heading; }
	public void setAltitude(double altitude) { _altitude = altitude; }

	public double getLat() { return _lat; }
	public double getLon() { return _lon; }
	public double getHeading() { return _heading; } 
	public double getAltitude() { return _altitude; }
	public String toString() { return new String("lat: " + _lat + " lon: " + _lon + " heading: " + _heading); }
}
