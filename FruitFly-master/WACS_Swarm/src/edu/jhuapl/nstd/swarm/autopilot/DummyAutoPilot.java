package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.util.*;
import java.util.logging.*;




import java.util.*;

public class DummyAutoPilot implements AutoPilotInterface {

	/**
	 * Stored plane info - replicates the last waypoint or loiter
	 */
	private HashMap<Integer, PlaneInfo> _planeInfos;

	public DummyAutoPilot() {
		_planeInfos = new HashMap<Integer, PlaneInfo>();
		
	}

	public void sendWaypoint(double lat, 
			double lon, double alt, double speed,
			int planeID, int commandNum, int totalCommands)
		throws AutoPilotException
	{
		sendWaypoint(planeID, lat, lon, alt, speed);
	}

	public void sendWaypoint(int planeID, double lat, double lon, double alt, double speed) {
		Logger.getLogger("GLOBAL").info("-----Sending Waypoint------");
		Logger.getLogger("GLOBAL").info("planeID: " + planeID);
		Logger.getLogger("GLOBAL").info("lat: " + lat);
		Logger.getLogger("GLOBAL").info("lon: " + lon);
		Logger.getLogger("GLOBAL").info("alt: " + alt);
		Logger.getLogger("GLOBAL").info("speed: " + speed);
		Logger.getLogger("GLOBAL").info("---------------------------");

		_planeInfos.put(planeID, new PlaneInfo(planeID, lat, lon, alt, speed, 0.0));
	}

	public void sendLoiter(int planeID, double lat, double lon, double alt, double speed,
												 double radius)
	{
		Logger.getLogger("GLOBAL").info("-----Sending Loiter------");
		Logger.getLogger("GLOBAL").info("planeID: " + planeID);
		Logger.getLogger("GLOBAL").info("lat: " + lat);
		Logger.getLogger("GLOBAL").info("lon: " + lon);
		Logger.getLogger("GLOBAL").info("alt: " + alt);
		Logger.getLogger("GLOBAL").info("speed: " + speed);
		Logger.getLogger("GLOBAL").info("radius: " + radius);
		Logger.getLogger("GLOBAL").info("-------------------------");

		_planeInfos.put(planeID, new PlaneInfo(planeID, lat, lon, alt, speed, 0.0));
	}

	public PlaneInfo getPlaneInfo(int planeID) {
		//Logger.getLogger("GLOBAL").info("-----Getting PlaneInfo------");

		if (_planeInfos.containsKey(planeID))
			return _planeInfos.get(planeID);
		else 
			return new PlaneInfo(planeID, 5.0, 6.0, 200.0,32.0, 33.0);
	}

	public void land(int planeID) {
		Logger.getLogger("GLOBAL").info("-----Landing------");
		Logger.getLogger("GLOBAL").info("planeID: " + planeID);
		Logger.getLogger("GLOBAL").info("------------------");
	}

	public void resetGimbal(int planeID)
		throws AutoPilotException
	{
		Logger.getLogger("GLOBAL").info("-----Resetting Gimbal------");
		Logger.getLogger("GLOBAL").info("planeID: " + planeID);
		Logger.getLogger("GLOBAL").info("------------------");

	}

	public void sendGimbal(int planeID, double lat, double lon, double height)
		throws AutoPilotException 
	{
		Logger.getLogger("GLOBAL").info("-----Sending Gimbal------");
		Logger.getLogger("GLOBAL").info("planeID: " + planeID);
		Logger.getLogger("GLOBAL").info("lat: " + lat);
		Logger.getLogger("GLOBAL").info("lon: " + lon);
		Logger.getLogger("GLOBAL").info("height: " + height);
		Logger.getLogger("GLOBAL").info("------------------");
	}
}
