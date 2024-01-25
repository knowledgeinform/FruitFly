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
import edu.jhuapl.nstd.swarm.autopilot.PlaneInfo;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;

import java.beans.*;

public class SimServer implements AutoPilotInterface {
	private DatagramSocket _sock;
	private InetAddress _addr;
	private String _agentID;
	private int _port = Config.getConfig().getPropertyAsInteger("simulator.server.port");
	private boolean _swap = Config.getConfig().getPropertyAsBoolean("simulator.swap", true);

	public SimServer(String id) {
		_agentID = id;
		try {
			_sock = new DatagramSocket();
			_addr = InetAddress.getByName(Config.getConfig().getProperty("simulator.server.addr"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] createMsg(double lat, double lon, double alt) {
		byte[] toReturn = new byte[_agentID.length()+2+12];
		int index = 0;
		index = ByteManipulator.addString(toReturn,_agentID,index,_swap);
		index = ByteManipulator.addFloat(toReturn,(float)lat,index,_swap);
		index = ByteManipulator.addFloat(toReturn,(float)lon,index,_swap);
		index = ByteManipulator.addFloat(toReturn,(float)alt,index,_swap);
		return toReturn;
	}

	public void sendWaypoint(int planeID, double lat, 
			double lon, double alt, double speed)	
	{
		byte[] msg = createMsg(lat,lon,alt);
		try {
			DatagramPacket pkt = new DatagramPacket(msg, msg.length, _addr, _port);
			Logger.getLogger("GLOBAL").info("SENDING WAYPOINT");
			_sock.send(pkt);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendWaypoint(double lat, 
			double lon, double alt, double speed,
			int planeID, int commandNum, int totalCommands)
	{

	}

	public void sendLoiter(int planeID, double lat, double lon, 
			double alt, double speed, double radius)
	{

	}

	public PlaneInfo getPlaneInfo(int planeID)
	{
		return null;
	}

	public void land(int planeID)
	{

	}

	public void resetGimbal(int planeID)
	{

	}

	public void sendGimbal(int planeID, double lat, double lon, double height)
	{

	}
}

