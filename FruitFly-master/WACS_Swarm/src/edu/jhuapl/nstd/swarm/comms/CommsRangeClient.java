package edu.jhuapl.nstd.swarm.comms;

import java.util.logging.*;

import java.io.IOException;

import java.lang.reflect.*;



import java.util.*;

import java.io.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.WACSDisplayAgent;

import edu.jhuapl.nstd.swarm.util.*;

public class CommsRangeClient extends BeliefManagerClient
{
	protected boolean _acceptAll = false;
	public CommsRangeClient(BeliefManager mgr) throws IOException
	{
		super(mgr);
	}
	public CommsRangeClient(BeliefManager mgr, boolean acceptAll) throws IOException
	{
		super(mgr);
		_acceptAll = acceptAll;
	}
	public byte[] txPacket(){
		byte[] initPacket;
		if (!_sendSingleBeliefs) {
			initPacket = txAllAtOnce();
		} else {
			initPacket = txSingle();
		}
		
		float latitude,longitude,altitude = 0.0f;
		
		if (_beliefManager.getName().equals(WACSDisplayAgent.AGENTNAME)) {
			latitude = 0;
			longitude = 0;
			altitude = 0;
		}
		else {
			latitude = (float)_beliefManager.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
			longitude = (float)_beliefManager.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
			altitude = (float)_beliefManager.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
		}

		byte[] name = _beliefManager.getName().getBytes();
		
		byte[] position = new byte[24];
		int index = 0;
		ByteManipulator.addFloat(position, latitude, index, false);
		index += 8;
		ByteManipulator.addFloat(position, longitude, index, false);
		index += 8;
		ByteManipulator.addFloat(position, altitude, index, false);
		index += 8;
		
		int initPacketLength = 0;
		if (initPacket != null)
			initPacketLength = initPacket.length;
		byte[] fullPacket = new byte[initPacketLength + position.length + 2 + name.length];
		int counter = 0;
		for(int i = 0; i < initPacketLength; i++)
		{
			fullPacket[counter] = initPacket[i];
			counter++;
		}
		for(int i = 0; i < position.length; i++)
		{
			fullPacket[counter] = position[i];
			counter++;
		}
		ByteManipulator.addShort(fullPacket, (short)name.length, counter, false);
		counter += 2;
		for (int i = 0; i > name.length; i++) {
			fullPacket[counter] = name[i];
			counter++;
		}
		return fullPacket;
	}
	public void rxPacket(byte[] packet, PacketIdentifier packetID){
		//Logger.getLogger("GLOBAL").info("START RX------------------");
		try{
			ArrayList<Belief> beliefList = new ArrayList<Belief>();
			ArrayList<Belief> beliefListFinal = new ArrayList<Belief>();
			
			short beliefCount = ByteManipulator.getShort(packet, 0, false);
			
			ByteArrayInputStream baStream = new ByteArrayInputStream(
				packet, 2, packet.length - 2);

			byte[] lenBytes = new byte[2];
			boolean printme = false;

			for (int i = 0; i < beliefCount; i++) {
				baStream.read(lenBytes, 0, 2);
				short strLen = ByteManipulator.getShort(lenBytes, 0, false);

				byte[] strBytes = new byte[strLen];
				baStream.read(strBytes, 0, strLen);
				String type = new String(strBytes);			

				//Logger.getLogger("GLOBAL").info("Received" + type);
				Belief belief = null;
				Class clas = Class.forName("edu.jhuapl.nstd.swarm.belief." + type);

				// TODO method cache?
				Method m = clas.getMethod("deserialize", new Class[] {
					InputStream.class, Class.class});

				belief = (Belief)m.invoke(null, new Object[] {baStream, clas});
                //System.err.println("Belief " + type + " TransmissionTime: " + belief.getTransmissionTime());
                
				if (_useRelativeTime) {
					Date localTime = new Date();
					belief.offsetTime(localTime);
				}
				
				//_beliefManager.put(belief);
				if (belief.getName().equals("SearchGoalBelief")) {
					printme = true;
					beliefListFinal.add(belief);
				}
				else
					beliefList.add(belief);
			}
			//Now check positioning
			byte[] position = new byte[24];
			AbsolutePosition senderPosition = null;
			if(baStream.read(position, 0, position.length) != -1)
			{
				int index = 0;
				float latitude = ByteManipulator.getFloat(position, index, false);
				index += 8;
				float longitude = ByteManipulator.getFloat(position, index, false);
				index += 8;
				float altitude = ByteManipulator.getFloat(position, index, false);
				index += 8;
				senderPosition = new LatLonAltPosition(new Latitude(latitude, Angle.DEGREES), new Longitude(longitude, Angle.DEGREES), new Altitude(altitude, Length.METERS));
				if (printme) {
					Logger.getLogger("GLOBAL").info("Display Pos = " + senderPosition);
					Logger.getLogger("GLOBAL").info("my pos = " + _beliefManager.getPosition());
					if (_beliefManager.getPosition() != null)
						Logger.getLogger("GLOBAL").info("distance = " + _beliefManager.getPosition().getRangeTo(senderPosition));
				}

			}
			byte[] nameLen = new byte[2];
			baStream.read(nameLen,0,2);
			short nameLength = ByteManipulator.getShort(nameLen,0,false);
			byte[] strBytes = new byte[nameLength];
			baStream.read(strBytes,0,nameLength);
			String from = new String(strBytes);
			//Logger.getLogger("GLOBAL").info("came from " + from);
			if(senderPosition != null)
			{
				if(_acceptAll || _beliefManager.getPosition().getRangeTo(senderPosition).getDoubleValue(Length.METERS) 
						< Config.getConfig().getPropertyAsDouble("simulation.commsRange",100))//If closer than 100 meters to sender, put all beliefs
				{
					for(int i = 0; i < beliefList.size(); i++)
					{
						/*if(beliefList.get(i).getName().equals("SentelSensorBelief"))
						{
							Logger.getLogger("GLOBAL").info("Received packet with sensor belief.");
						}*/
						_beliefManager.put(beliefList.get(i));
					}
				}
			}
			for (Belief b : beliefListFinal) 
				_beliefManager.put(b);

			//Logger.getLogger("GLOBAL").info("DONE RX------------------");
				
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
