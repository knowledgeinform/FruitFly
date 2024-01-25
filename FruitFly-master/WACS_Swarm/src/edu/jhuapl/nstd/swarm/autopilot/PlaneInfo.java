package edu.jhuapl.nstd.swarm.autopilot;

/**
 * Encapsulates everything a plane will report to us about it's current conditions.
 */
public class PlaneInfo {
	private int _planeID;
	private double _lat;
	private double _lon;
	private double _altWGS84;
        private double _altMSL;
	private double _speed;
	private double _bearing;
	private double _gimbalAz;
	private double _gimbalEl;
	private double _batteryVoltage;
	private double _airTime;

	public PlaneInfo(int planeID, double lat, 
			double lon, double altWGS84, double altMSL, double speed, double bearing, 
			double gimbalAz, double gimbalEl, double batteryVoltage, double airTime) 
	{
		this(planeID,lat,lon,altWGS84,altMSL,speed,bearing);
		_gimbalAz = gimbalAz;
		_gimbalEl = gimbalEl;
		_batteryVoltage = batteryVoltage;
		_airTime = airTime;
	}

	public PlaneInfo(double lat, double lon, double altWGS84, double altMSL, double speed, double bearing) {
		this(0, lat, lon, altWGS84, altMSL, speed, bearing);
	}
        
	public PlaneInfo(int planeID, double lat, double lon, double altWGS84, double altMSL, double speed, double bearing) {
		_planeID = planeID;
		_lat = lat;
		_lon = lon;
		_altWGS84 = altWGS84;
                _altMSL = altMSL;
		_speed = speed;
		_bearing = bearing;
		_gimbalAz = 0;
		_gimbalEl = 0;
	}

	public int getPlaneID() { return _planeID; }
	public double getLatitude() { return _lat; }
	public double getLongitude() { return _lon; }

	/** Returns the altitude in meters. */
	public double getAltitudeWGS84() { return _altWGS84; }
        public double getAltitudeMSL() { return _altMSL; }
	/** Returns the altitude in meters/second. */
	public double getSpeed() { return _speed; }
	/** Returns the altitude in degrees from true. */
	public double getBearing() { return _bearing; }
	public double getGimbalAz() { return _gimbalAz; }
	public double getGimbalEl() { return _gimbalEl; }
	public double getBatteryVoltage() { return _batteryVoltage; }
	/** Returns the air time in seconds */
	public double getAirTime() { return _airTime; }

	public String toString() {
		return String.format("[Plane id: %d lat: %3.3f lon %3.3f altWGS84: %3.1f altMSL: %3.1f speed: %4.1f bearing: %3.0f gimbalAz: %3.0f gimbalEl: %3.0f batteryVoltage: %3.0f airTime: %3.0f]",
				_planeID, _lat, _lon, _altWGS84, _altMSL, _speed, _bearing, _gimbalAz, _gimbalEl, _batteryVoltage, _airTime);
	}
}
