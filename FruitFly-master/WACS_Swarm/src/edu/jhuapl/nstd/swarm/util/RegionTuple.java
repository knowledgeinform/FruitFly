package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;

import edu.jhuapl.jlib.math.*; 
import edu.jhuapl.jlib.collections.storage.*; 
import edu.jhuapl.jlib.math.position.*; 

import java.io.*;



import java.util.*;

public class RegionTuple {
	public Region _region;
	public long _timestamp;

	public RegionTuple(Region r, Date time) {
		_region = r;
		_timestamp = time.getTime();
	}

	public RegionTuple(Region r, long time) {
		_region = r;
		_timestamp = time;
	}

	public String write() {
		StringBuffer buf = new StringBuffer();
		buf.append("time: " + _timestamp + "\n");
		for (AbsolutePosition pos : _region.getPositions()) {
			buf.append(pos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) + " " + 
					pos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) + 
					pos.asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS) + "\n");
		}
		return buf.toString();
	}

	public double getAltitude() {
		return _region.getPositions()[0].asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);
	}
	
	public boolean equals(Object o) {
		return _timestamp == ((RegionTuple)o)._timestamp;
	}
}
