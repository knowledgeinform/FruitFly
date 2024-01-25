
package edu.jhuapl.nstd.swarm.simulator;

import java.util.logging.*;




import java.util.*;

import java.io.*;
import java.net.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.display.*;
import edu.jhuapl.nstd.swarm.action.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.behavior.group.*;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.Classification;
import edu.jhuapl.nstd.swarm.belief.ClassificationBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.comms.*;
import edu.jhuapl.nstd.swarm.util.*;

public class SimClient implements Runnable {
	private BeliefManager _belMgr;
	private String _agentID;
	private MulticastSocket _sock;
	private String _groupAddr = Config.getConfig().getProperty("simulator.groupaddr");
	private boolean _swap = Config.getConfig().getPropertyAsBoolean("simulator.swap", true);
	private boolean _targetServer = Config.getConfig().getPropertyAsBoolean("simulator.targetserver",false);

	public SimClient(BeliefManager belMgr, String agentID) {
		_belMgr = belMgr;
		_agentID = agentID;
		try {
			InetAddress groupAddr = InetAddress.getByName(_groupAddr);
			_sock = new MulticastSocket(Config.getConfig().getPropertyAsInteger("simulator.client.port"));
			_sock.joinGroup(groupAddr);
			//_sock = new DatagramSocket(Config.getConfig().getPropertyAsInteger("simulator.client.port"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		byte[] buff, data;
		DatagramPacket packet;
		while(true) {
			try {
				buff = new byte[1024];
				packet = new DatagramPacket(buff,buff.length);
				_sock.receive(packet);
				data = packet.getData();
				String name = ByteManipulator.getString(data,0,_swap);
				Logger.getLogger("GLOBAL").info("name: " + name);
				if (!name.equals(_agentID))
					continue;

				else if (name.equalsIgnoreCase("target") && _targetServer) {
					int len = (int)ByteManipulator.getShort(data,0,_swap);
					int index = len + 2;
					float lat = ByteManipulator.getFloat(data,index,_swap);
					Logger.getLogger("GLOBAL").info("LAT: " + lat);
					index += 4;
					float lon = ByteManipulator.getFloat(data,index,_swap);
					Logger.getLogger("GLOBAL").info("LON: " + lon);
					index += 4;
					float alt = ByteManipulator.getFloat(data,index,_swap);
					Logger.getLogger("GLOBAL").info("ALT: " + alt);
					index += 4;
					float head = ByteManipulator.getFloat(data,index,_swap);
					index += 4;
					Latitude latitude = new Latitude(lat, Angle.DEGREES);
					Longitude longitude = new Longitude(lon, Angle.DEGREES);
					NavyAngle heading = new NavyAngle(head, Angle.DEGREES);
					Altitude altitude = new Altitude(alt, Length.METERS);
					LatLonAltPosition position = new LatLonAltPosition(latitude,longitude,altitude);
					TargetCommandedBelief tb = new TargetCommandedBelief(name, position, Length.ZERO, name);
					ClassificationBelief cb = new ClassificationBelief(name,name,Classification.VEHICLE_TARGET);
					_belMgr.put(tb);
					_belMgr.put(cb);
					continue;
				}
				Logger.getLogger("GLOBAL").info("RECEIVED PACKET");
				int len = (int)ByteManipulator.getShort(data,0,_swap);
				int index = len + 2;
				float lat = ByteManipulator.getFloat(data,index,_swap);
				Logger.getLogger("GLOBAL").info("LAT: " + lat);
				index += 4;
				float lon = ByteManipulator.getFloat(data,index,_swap);
				Logger.getLogger("GLOBAL").info("LON: " + lon);
				index += 4;
				float alt = ByteManipulator.getFloat(data,index,_swap);
				Logger.getLogger("GLOBAL").info("ALT: " + alt);
				index += 4;
				float head = ByteManipulator.getFloat(data,index,_swap);
				index += 4;
				Latitude latitude = new Latitude(lat, Angle.DEGREES);
				Longitude longitude = new Longitude(lon, Angle.DEGREES);
				NavyAngle heading = new NavyAngle(head, Angle.DEGREES);
				Altitude altitude = new Altitude(alt, Length.METERS);
				LatLonAltPosition position = new LatLonAltPosition(latitude,longitude,altitude);
				Logger.getLogger("GLOBAL").info("position of " + _agentID +": " + position);
				AgentPositionBelief positionBelief = new AgentPositionBelief(_agentID,position,heading);
				AgentBearingBelief abb = new AgentBearingBelief(_agentID,heading,heading);
				_belMgr.put(positionBelief);
				_belMgr.put(abb);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			try {
				Thread.sleep(Config.getConfig().getPropertyAsLong("vc.sleep",0));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	

    
}

