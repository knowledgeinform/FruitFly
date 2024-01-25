package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;




import java.util.*;
import java.util.concurrent.*;

import java.io.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SNRBelief extends Belief implements BeliefExternalizable {

	public static final int SNR_MAX = Config.getConfig().getPropertyAsInteger("snr.max", 80);

	public static final String BELIEF_NAME = "SNRBelief";

	private transient ConcurrentHashMap<SNR,Long> _map;

	protected SNRBelief() {
		_map = new ConcurrentHashMap<SNR,Long>();
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
		propertyChange(null);
	}

	public SNRBelief(String agentID) {
		super(agentID);
		_map = new ConcurrentHashMap<SNR,Long>();
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
		propertyChange(null);
	}

	public SNRBelief(String agentID, HashMap<SNR,Long> map) {
		super(agentID);
		if (map == null)
			throw new IllegalArgumentException("Map cannot be null");
		_map = new ConcurrentHashMap(map);
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
		propertyChange(null);
	}

	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
	}

	public double getSNR(String agent1, String agent2) {
		//synchronized(_map) {
			Set<SNR> keys = _map.keySet();
			Iterator<SNR> itr = keys.iterator();
			while (itr.hasNext()) {
				SNR snr = itr.next();
				if (snr.equals(agent1,agent2)) {
					if (snr._snr > SNR_MAX)
						return 0.0;
					else
						return snr._snr;
				}
			}
		//}
		return 0.0;
	}

	public synchronized void update() 
        {
            try
            {
		super.update();
		long current = System.currentTimeMillis();
			for (SNR snr : _map.keySet()) {
				long difference = current - _map.get(snr);
				//Logger.getLogger("GLOBAL").info("snr: " + snr);
				if (difference > Config.getConfig().getPropertyAsLong("snrbelief.decaytime", 3000)) {
					Logger.getLogger("GLOBAL").info("add: removing " + snr + " because difference: " + difference);
					//synchronized(_map) {
						_map.remove(snr);
					//}
				}
			}
			//Logger.getLogger("GLOBAL").info("\n\n");
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
	}

	public Set<SNR> getKeys() {
		//synchronized(_map) {
			Set<SNR> toReturn = new HashSet<SNR>(_map.keySet());
			return toReturn;
		//}
	}
		
	public double getMTM(String agent1, String agent2) {
		Set<SNR> keys = _map.keySet();
		Iterator<SNR> itr = keys.iterator();
		while (itr.hasNext()) {
			SNR snr = itr.next();
			if (snr.equals(agent1,agent2))
				return snr._mtm;
		}
		return -1.0;
	}

	public ConcurrentHashMap<SNR,Long> getMap() {
		return _map;
	}

	public String toString() {
		String toReturn = "";
		for (SNR snr : _map.keySet()) {
			toReturn = toReturn.concat(snr + "\n");
		}
		return toReturn;
	}


	public synchronized void addBelief(Belief b) {
		SNRBelief snrBelief = (SNRBelief)b;
		ConcurrentHashMap<SNR,Long> newMap = snrBelief.getMap();

		if (newMap == null) {
			Logger.getLogger("GLOBAL").info("Map was null, ignoring");
			return;
		}

	   if (b.getTimeStamp().compareTo(timestamp) > 0)
   	   timestamp = b.getTimeStamp();

		//synchronized(_map) {

			Set<SNR> keys = newMap.keySet();
			Iterator<SNR> itr = keys.iterator();
			while (itr.hasNext()) {
				SNR snr = itr.next();
				if (!_map.containsKey(snr)) {
					_map.put(snr, newMap.get(snr));
				}
				else {
					long newTime = newMap.get(snr);
					long myTime = _map.get(snr);
					if (newTime > myTime) {
						_map.remove(snr);
						//Logger.getLogger("GLOBAL").info("add: " + snr + " " + (newTime - myTime));
						_map.put(snr,newTime);
					}
				}
			}
		//}
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
			//synchronized(_map) {
				_map = new ConcurrentHashMap<SNR,Long>();

				int index = 0;
				for (int i=0; i<numKeys; i++) {
					String a1 = ByteManipulator.getString(bytes, index, false);
					index += a1.length() + 2;
					String a2 = ByteManipulator.getString(bytes, index, false);
					index += a2.length() + 2;
					double snrValue = ByteManipulator.getFloat(bytes, index, false);
					index += 4;
					int mtm = ByteManipulator.getInt(bytes, index, false);
					index += 4;
					long time = ByteManipulator.getLong(bytes, index, false);
					index += 8;
					SNR snr = new SNR(a1, a2, snrValue, mtm);
					_map.put(snr, time);
				}
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public synchronized void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		Set<SNR> keys = getKeys();
		int numKeys = keys.size();
		Iterator<SNR> itr = keys.iterator();
		int numBytes = 0;
		while (itr.hasNext()) {
			numBytes += itr.next().numBytes();
		}

		//number of keys is the 1

		byte[] bytes = new byte[1 + 4 + numBytes + (numKeys * 8)];
		itr = keys.iterator();
		int index = 0;
		index = ByteManipulator.addByte(bytes, (byte)numKeys, index, false);
		index = ByteManipulator.addInt(bytes, numBytes + (numKeys * 8), index, false); 
		while (itr.hasNext()) {
			SNR snr = itr.next();
			byte[] snrBytes = snr.writeExternal();
			for (int i=0; i<snrBytes.length; i++) {
				index = ByteManipulator.addByte(bytes, snrBytes[i], index, false);
			}
			index = ByteManipulator.addLong(bytes, _map.get(snr), index, false);
		}
		out.write(bytes);
	}
}
