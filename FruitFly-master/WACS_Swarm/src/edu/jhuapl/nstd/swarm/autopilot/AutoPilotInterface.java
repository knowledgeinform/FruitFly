package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.util.*;
import java.util.logging.*;

public interface AutoPilotInterface {
	public void sendWaypoint(int planeID, double lat, 
			double lon, double alt, double speed)
		throws AutoPilotException;

	public void sendWaypoint(double lat, 
			double lon, double alt, double speed,
			int planeID, int commandNum, int totalCommands)
		throws AutoPilotException;

	public void sendLoiter(int planeID, double lat, double lon, 
			double alt, double speed, double radius)
		throws AutoPilotException;

	/**
	 * Gets the latest plane info from the autopilot. This method is expected 
	 * to be non-blocking.
	 */
	public PlaneInfo getPlaneInfo(int planeID)
		throws AutoPilotException;

	public void land(int planeID)
		throws AutoPilotException;

	public void resetGimbal(int planeID)
		throws AutoPilotException;

	public void sendGimbal(int planeID, double lat, double lon, double height)
		throws AutoPilotException;

}
