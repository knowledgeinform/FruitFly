package edu.jhuapl.nstd.swarm.action;

import edu.jhuapl.nstd.swarm.autopilot.PlaneInfo;
import edu.jhuapl.nstd.swarm.autopilot.AutoPilotInterface;
import java.util.logging.*;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.*;

import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.math.*;




import java.util.*;



public class ReturnToBaseAction {

	protected AbsolutePosition _launchPosition;

	protected RangeBearingHeightOffset _result;

	protected BeliefManager _belMgr = null;
	protected String _agentID = null;
	protected AutoPilotInterface _api = null;
	protected int _planeID = -1;
	protected boolean _goingHome = false;


	public ReturnToBaseAction(BeliefManager beliefMgr, String agentID, AutoPilotInterface api,
			int planeID) 
	{
		_belMgr = beliefMgr;
		_agentID = agentID;
		_api = api;
		_planeID = planeID;
		double lat = Config.getConfig().getPropertyAsDouble("rtb.launch.lat",0.0);
		double lon = Config.getConfig().getPropertyAsDouble("rtb.launch.lon",0.0);
		if (lat != 0.0 && lon != 0)
			_launchPosition = new LatLonAltPosition(new Latitude(lat, Angle.DEGREES),
																							new Longitude(lon, Angle.DEGREES),
																							Altitude.ZERO);
		else
			_launchPosition = null;
	}

	protected AbsolutePosition getMyPosition() {
		AgentPositionBelief b = (AgentPositionBelief)_belMgr.get(AgentPositionBelief.BELIEF_NAME);
		if (b != null) {
			return b.getPositionTimeName(_agentID).getPosition();
		}
		else
			return null;
	}

	public RangeBearingHeightOffset goHome() {
		if (_launchPosition == null) 
			return null;
		PlaneInfo info;
		try {
			info = _api.getPlaneInfo(_planeID);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (info == null)
			return null;
		AbsolutePosition myPos = getMyPosition();
		if (myPos == null) {
			return null;
		}
		else if (info.getAirTime() > Config.getConfig().getPropertyAsDouble("rtb.airTime", Double.MAX_VALUE)) {
			return myPos.getRangeBearingHeightOffsetTo(_launchPosition);
		}
		/*else if (myPos.getRangeTo(_launchPosition).isGreaterThan(new Length(
						Config.getConfig().getPropertyAsDouble("rtb.distance", 60000), Length.METERS)))
		{
			return myPos.getRangeBearingHeightOffsetTo(_launchPosition);
		}*/
		
		else {
			return null;
		}
	}
}
