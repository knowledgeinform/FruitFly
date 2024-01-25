package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;


import java.io.*;



import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.*;

public class PacketInfoTimeName extends TimeName  implements BeliefExternalizable {
	protected int _recvPackets;
	protected int _sentPackets;
	
	//Empty constructor
	public PacketInfoTimeName() {}
	//Full constructor
	public PacketInfoTimeName(String agentID, int recvPackets, int sentPackets, Date time)
	{
		super(time, agentID);
		_recvPackets = recvPackets;
		_sentPackets = sentPackets;
	}
	public String toString() {
		//return new String(name + ", rx: " + _recvPackets + ", tx: " + _sentPackets);
		return new String(name + " " + _recvPackets + " " + _sentPackets);
	}

	public int getRxPackets() { return _recvPackets; }
	public int getTxPackets() { return _sentPackets; }


	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);

		byte[] bytes = new byte[4 + 4];

		int index = 0;
		index = ByteManipulator.addInt(bytes, _recvPackets, index, false);
		index = ByteManipulator.addInt(bytes, _sentPackets, index, false);
		out.write(bytes);	
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
			super.readExternal(in);
	
			byte[] bytes  = new byte[4 + 4];
			in.readFully(bytes);
			int index = 0;
			_recvPackets = ByteManipulator.getInt(bytes, index, false);
			index += 4;
			_sentPackets = ByteManipulator.getInt(bytes, index, false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
