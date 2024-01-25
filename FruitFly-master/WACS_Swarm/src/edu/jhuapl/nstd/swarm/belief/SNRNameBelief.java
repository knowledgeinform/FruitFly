package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;




import java.util.*;
import java.util.concurrent.*;

import java.io.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;

public class SNRNameBelief extends Belief implements BeliefExternalizable {

	public static final String BELIEF_NAME = "SNRNameBelief";

	private transient ConcurrentHashMap<Long,String> _map;

	protected SNRNameBelief() {
		_map = new ConcurrentHashMap<Long,String>();
	}

	public SNRNameBelief(String agentID) {
		super(agentID);
		_map = new ConcurrentHashMap<Long,String>();
	}

	public SNRNameBelief(HashMap<Long,String> map) {
		if (map == null)
			throw new IllegalArgumentException("Map cannot be null");
		_map = new ConcurrentHashMap<Long,String>(map);
	}

	public ConcurrentHashMap<Long,String> getMap() {
		return _map;
	}

	public String getName(long macAddress) {
		return _map.get(macAddress);
	}
		

	public synchronized void addBelief(Belief b) {
		if (b.getTimeStamp().compareTo(timestamp) > 0)
			timestamp = b.getTimeStamp();

		SNRNameBelief snrNameBelief = (SNRNameBelief)b;
		ConcurrentHashMap<Long,String> newMap = snrNameBelief.getMap();

		if (newMap == null) {
			Logger.getLogger("GLOBAL").info("Map was null, ignoring");
			return;
		}

		_map.putAll(newMap);

	}

	public String getName() {
		return BELIEF_NAME;
	}

	public byte[] serialize() throws IOException {
		setTransmissionTime();
		ByteArrayOutputStream baStream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baStream);

		writeExternal(out);
		out.flush();
		out.close();
		return baStream.toByteArray();
	}

	public static Belief deserialize(InputStream iStream, Class clas) throws IOException {
		DataInputStream in = new DataInputStream(iStream);
		
		Belief belief = null;

		try {
			belief = (Belief)clas.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		belief.readExternal(in);

		return belief;
	}

	public void readExternal(DataInput in) throws IOException {
		try {
			super.readExternal(in);
			int numKeys = (int)in.readByte();
			int numBytes = in.readInt();
			byte[] bytes = new byte[numBytes];
			in.readFully(bytes);
			_map = new ConcurrentHashMap<Long,String>();

			int index = 0;
			for (int i=0; i<numKeys; i++) {
				long l = ByteManipulator.getLong(bytes, index, false);
				index += 4;
				String name = ByteManipulator.getString(bytes, index, false);
				index += name.length() + 2;
				_map.put(l, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public synchronized void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		Collection<String> vals = _map.values();
		int numVals = vals.size();
		int numBytes = 0;
		for (String s : vals) {
			numBytes += s.getBytes().length;
		}

		//number of vals is the 1
		//numVals * 8 represents all of the longs
		//numVals * 2 represents all of the headers for the Strings

		byte[] bytes = new byte[1 + 4 + numBytes + (numVals * 8) + (numVals * 2)];
		int index = 0;
		index = ByteManipulator.addByte(bytes, (byte)numVals, index, false);
		index = ByteManipulator.addInt(bytes, numBytes + (numVals * 8) + (numVals * 2), index, false); 
		Set<Long> keys = _map.keySet();
		for (Long l : keys) {
			index = ByteManipulator.addLong(bytes, l, index, false);
			index = ByteManipulator.addString(bytes, _map.get(l), index, false);
		}
			
		out.write(bytes);
	}
}
