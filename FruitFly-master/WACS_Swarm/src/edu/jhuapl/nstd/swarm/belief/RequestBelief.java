package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;




import java.util.*;

import java.io.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;

public class RequestBelief extends Belief implements BeliefExternalizable {

	public static final String BELIEF_NAME = "RequestBelief";
	protected String _origin;
	protected String _end;
	protected int _desiredSNR;
	protected boolean _completed;

	public RequestBelief(String agentID) {
		super(agentID);
		_origin = agentID;
		timestamp = new Date(System.currentTimeMillis());
	}

	public RequestBelief(String agentID, boolean completed) {
		super(agentID);
		_completed = completed;
	}

	public void addBelief(Belief b) {
		RequestBelief rb = (RequestBelief)b;
		if (rb.getTimeStamp().compareTo(timestamp) > 0) {
			_origin = rb.getOrigin();
			_end = rb.getEnd();
			_completed = rb.getCompleted();
			timestamp = rb.getTimeStamp();
		}
	}

	public String getOrigin() { return _origin; }
	public String getEnd() { return _end; }
	public boolean getCompleted() { return _completed; }


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
			int numBytes = in.readInt();
			byte[] bytes = new byte[numBytes];
			in.readFully(bytes);

			int index = 0;
			_origin = ByteManipulator.getString(bytes,index,false);
			index += (_origin.length() + 2);
			_end = ByteManipulator.getString(bytes,index,false);
			index += (_end.length() + 2);
			_desiredSNR = ByteManipulator.getInt(bytes,index,false);
			index += 4;
			int comp = (int)ByteManipulator.getByte(bytes,index,false);
			_completed = (comp == 1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public synchronized void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);

		int numBytes = 4 + _origin.length() + 2 + _end.length() + 2 + 4 + 1;

		byte[] bytes = new byte[numBytes];
		int index = 0;
		index = ByteManipulator.addInt(bytes, numBytes, index, false);
		index = ByteManipulator.addString(bytes, _origin, index, false); 
		index = ByteManipulator.addString(bytes, _end, index, false); 
		index = ByteManipulator.addInt(bytes, _desiredSNR, index, false);
		byte comp = (_completed ? (byte)1 : (byte)0);
		index = ByteManipulator.addByte(bytes, comp, index, false);
		out.write(bytes);
	}
}

