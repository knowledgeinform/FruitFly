//=============================== UNCLASSIFIED ================================== 
// 
// Copyright (c) 1999 The Johns Hopkins University/Applied Physics Laboratory 
// Developed by JHU/APL. 
// 
// This material may be reproduced by or for the U.S. Government pursuant to 
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988). 
// For any other permissions please contact JHU/APL. 
// 
//=============================== UNCLASSIFIED ================================== 
  
package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;


import java.io.*;
import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.math.position.*; 
import edu.jhuapl.nstd.swarm.util.*; 


public class CloudDetection {
        public static final short SOURCE_CHEMICAL = 0;
        public static final short SOURCE_PARTICLE = 1;
    
	protected float _lat;
	protected float _lon;
	protected float _alt;
	protected float _scaledValue;
	protected long _time;
        protected short _source;
        protected short _id;
        protected short _rawValue;
	protected boolean _swap = Config.getConfig().getPropertyAsBoolean("CloudDetectionBelief.swap", false);
    
	public CloudDetection() {

	}


	public CloudDetection(LatLonAltPosition pos, double scaledValue, short source, short id, short rawValue, long timeMs) {
		_lat = (float)pos.getLatitude().getDoubleValue(Angle.DEGREES);
		_lon = (float)pos.getLongitude().getDoubleValue(Angle.DEGREES);
		_alt = (float)pos.getAltitude().getDoubleValue(Length.METERS);
		_scaledValue = (float)scaledValue;
                _time = timeMs;
                _source = source;
                _rawValue = rawValue;
                _id = id;
	}

	public LatLonAltPosition getDetection() {
		return new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES), new Longitude(_lon, Angle.DEGREES), new Altitude(_alt, Length.METERS));
	}

	public float getLat_deg() { return _lat; }
	public float getLon_deg() { return _lon; }
	public float getAlt_m() { return _alt; }
	public float getScaledValue() { return _scaledValue; }
        public short getRawValue() { return _rawValue; }
	public long getTime() { return _time; }
        public short getSource() { return _source; }
        public short getId() {return _id;}

	/*public boolean equals(Object o) {
		if (o instanceof CloudDetection) {
			CloudDetection other = (CloudDetection)o;
			if (other.getLat_deg() == getLat_deg() &&
					other.getLon_deg() == getLon_deg() &&
					other.getAlt_m() == getAlt_m() &&
                                        other.getSource() == getSource() && 
                                        other.getId() == getId())
			{
				return true;
			}
			else 
				return false;
		}
		return false;
	}*/
        public boolean equals(Object o) {
		if (o instanceof CloudDetection) {
			CloudDetection other = (CloudDetection)o;
			if (other.getTime() == getTime() &&
                                        other.getSource() == getSource() && 
                                        other.getId() == getId())
			{
				return true;
			}
			else 
				return false;
		}
		return false;
	}

	public void degradeValue(double degrade) {
		_scaledValue *= degrade;
		//Logger.getLogger("GLOBAL").info("degrading: " + _value);
	}

	public synchronized byte[] writeExternal() throws IOException {
		byte[] data = new byte[4 * 4 + 8 + 2 + 2 + 2];
		int index = 0;
		index = ByteManipulator.addFloat(data, _lat, index, _swap);
		index = ByteManipulator.addFloat(data, _lon, index, _swap);
		index = ByteManipulator.addFloat(data, _alt, index, _swap);
		index = ByteManipulator.addFloat(data, _scaledValue, index, _swap);
		index = ByteManipulator.addLong(data, _time, index, _swap);
                index = ByteManipulator.addShort(data, _source, index, _swap);
                index = ByteManipulator.addShort(data, _id, index, _swap);
                index = ByteManipulator.addShort(data, _rawValue, index, _swap);
		return data;
	}

	public void readExternal(DataInput in) throws IOException {
		_lat = in.readFloat();
		_lon = in.readFloat();
		_alt = in.readFloat();
		_scaledValue = in.readFloat();
		_time = in.readLong();
                _source = in.readShort();
                _id = in.readShort();
                _rawValue = in.readShort();
        
	}

	public String toString() {
		return new String("lat: " + _lat + " lon: " + _lon + " alt: " + _alt + " time: " + _time + " scaledVal: " + _scaledValue + " source: "+ _source + " id: " + _id + " rawVal: " + _rawValue);
	}
}
