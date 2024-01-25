//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2003 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================
package edu.jhuapl.nstd.swarm.simulator;

import java.util.logging.*;

import java.net.*;

import java.io.*;




import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.ui.math.*;
import edu.jhuapl.nstd.swarm.util.Config;

import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.action.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.behavior.group.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.autopilot.AutoPilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.AutoPilotException;

import java.beans.*;

/**
 * This class should take the desired bearing from our AI and project it out a distance to give the UAV a waypoint 
 * to fly to.
 */
public class ServerAction implements Updateable, PropertyChangeListener {
  
  BeliefManager beliefManager;
  
  String _agentID = "";

  BehaviorGroup behaviorGroup;

	protected AvoidObstacleAction _avoidObstacleAction;

	private AutoPilotInterface _api;

	int _planeID = -1;

	protected double _altitude = 0;

	private double _waypointDistance = 
		Config.getConfig().getPropertyAsDouble("uav.waypointDistance", 100.0);
    

  public ServerAction(BeliefManager mgr, String agentID, BehaviorGroup behavior,
			AutoPilotInterface api, int id) 
	{
    this._agentID = agentID;
    this.beliefManager = mgr;
    this.behaviorGroup = behavior;
		_planeID = id;
		_api = api;
		_avoidObstacleAction = new AvoidObstacleAction(mgr);
    Config.getConfig().addPropertyChangeListener(this);
  }

	protected void sendWaypointRegular(AbsolutePosition pos,
			RangeBearingHeightOffset r, double altitude) 
		throws AutoPilotException
	{
		double lat,lon;
		pos = pos.translatedBy(new RangeBearingHeightOffset(
											 new Length(_waypointDistance, Length.METERS), 
					 						 r.getBearing(), 
											 Length.ZERO));
		lat = Accessor.getDoubleValue(pos.asLatLonAltPosition().getLatitude(), Angle.DEGREES);
		lon = Accessor.getDoubleValue(pos.asLatLonAltPosition().getLongitude(), Angle.DEGREES);
		UAVWaypointBelief uav = new UAVWaypointBelief(_agentID, pos);
		beliefManager.put(uav);
		_api.sendWaypoint(_planeID, lat, lon, altitude,  
								Config.getConfig().getPropertyAsDouble(_agentID+".speed",10.0));

	}

  
  public void update() { 
		try {
			AbsolutePosition p;
			AbsolutePosition currentPosition;
			Altitude altitude = null;
			NavyAngle currentBearing;
			_waypointDistance = 
					Config.getConfig().getPropertyAsDouble("uav.waypointDistance", 100.0);

			AgentPositionBelief b = (AgentPositionBelief)beliefManager.get(AgentPositionBelief.BELIEF_NAME);
			if (b != null)
			{
				p = b.getPositionTimeName(_agentID).getPosition();
				currentPosition = p;
				Logger.getLogger("GLOBAL").info("-----Current pos " + p);
				currentBearing = b.getPositionTimeName(_agentID).getHeading();
				
				RangeBearingHeightOffset r = null;
				r = _avoidObstacleAction.avoidObstacle(_agentID, p, currentBearing);
				if (r == null)
					r = behaviorGroup.getResult();
				if (!r.getRange().equals(Length.ZERO))
					sendWaypointRegular(currentPosition,r,_altitude);

				else {}
      }
		} catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  
	protected void printWaypoint(int agentID, double lat, double lon, double alt, double msl) {
		Logger.getLogger("GLOBAL").info("---------------------\n" + agentID + " going to send:");
		Logger.getLogger("GLOBAL").info("lat: " + lat + "\nlon: " + lon + "\nalt: " + alt + 
						"\nspeed: " + Config.getConfig().getPropertyAsDouble(agentID+".speed",10.0) +
						"\nplaneid: " + _planeID + "\nwaypointDistance: " + _waypointDistance + 
						"\nmsl: " + msl 
						+ "\n-------------------");
	}

  
  public void propertyChange(PropertyChangeEvent e) 
  {
      Config config = Config.getConfig();
      _waypointDistance = config.getPropertyAsDouble("uav.waypointDist", 100);	
  }
  
} // end class UAVFlyAction

