package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*; 
 



import java.util.*; 
import java.util.concurrent.*; 
 
import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.collections.storage.*; 
import edu.jhuapl.jlib.math.position.*; 
import edu.jhuapl.nstd.swarm.*; 
import edu.jhuapl.nstd.swarm.util.*; 

import java.io.*; 
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener; 
 
public class IRBelief extends Belief implements BeliefExternalizable { 
	public static final String BELIEF_NAME = "IRBelief"; 
 
	protected double _heading;
	protected double _lat;
	protected double _lon;

	private transient boolean _selected = false;

	protected boolean _swap = Config.getConfig().getPropertyAsBoolean("IRBelief.swap");
 
	public IRBelief() { 
		this("temp", 0.0, 0.0, 0.0); 
	} 
 
	public IRBelief(String agentID, double heading, double lat, double lon) { 
		super(agentID); 
		_heading = heading;
		_lat = lat;
		_lon = lon;
		timestamp = new Date(TimeManagerFactory.getTimeManager().getTime()); 
		propertyChange(null); 
	} 
 
	public void propertyChange(PropertyChangeEvent e) { 
		super.propertyChange(e); 
	} 
 
	public double getHeading() { 
		return _heading;
	} 
 
	public double getLat() { 
		return _lat;
	} 
 
	public double getLon() { 
		return _lon;
	} 

	public synchronized void setSelected(boolean in) {
		_selected = in;
	}

	public boolean getSelected() {
		return _selected;
	}
 
	public synchronized void addBelief(Belief b) { 
		IRBelief irBelief = (IRBelief)b; 
 
		if (b.getTimeStamp().compareTo(timestamp) > 0) {
			timestamp = b.getTimeStamp(); 
			_heading = irBelief.getHeading();
			_lat = irBelief.getLat();
			_lon = irBelief.getLon();
		}
 
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
 
		IRBelief belief = null; 
 
		try { 
			belief = (IRBelief)clas.newInstance(); 
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
 
			byte[] data = new byte[4*3]; 
			in.readFully(data); 

			int index = 0;
			_heading = (double)ByteManipulator.getFloat(data, index, _swap);
			index += 4;
			_lat = (double)ByteManipulator.getFloat(data, index, _swap);
			index += 4;
			_lon = (double)ByteManipulator.getFloat(data, index, _swap);
			index += 4;
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
 
	public synchronized void writeExternal(DataOutput out) throws IOException { 
		super.writeExternal(out); 
		byte[] data = new byte[4*3]; //heading, lat, lon
		int index = 0; 
		index = ByteManipulator.addFloat(data, (float)_heading, index, _swap); 
		index = ByteManipulator.addFloat(data, (float)_lat, index, _swap); 
		index = ByteManipulator.addFloat(data, (float)_lon, index, _swap); 
 
		out.write(data); 
 
	} 
 
 
} 
