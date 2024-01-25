/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.Temperature;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.io.*;
import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.*;

/**
 *
 * @author kayjl1
 */
public class EtdDetection implements Comparable<EtdDetection>
{
	private Float _concentration;
        private AbsolutePosition _pos;
        private long _timeMs;
        
          /** Storage for serialization */
	private float _lat = 0.0f;
	private float _lon = 0.0f;
	private float _alt = 0.0f;
        
        public EtdDetection() {}

	public EtdDetection(Float concentration, AbsolutePosition pos, long timeMs) {
		_concentration = concentration;
                _pos = pos;
                _timeMs = timeMs;
	}

	public String toString() {
		return new String("concentration: " + _concentration + ", absolute position: " + _pos + ", time ms: " + _timeMs);
	}

	public Float getConcentration() { return _concentration; }
        
        public long getTime() { return _timeMs; }
        
        public AbsolutePosition getPosition() { return _pos; }
        
        public synchronized byte[] writeExternal() throws IOException {
		LatLonAltPosition lla = _pos.asLatLonAltPosition();
		_lat = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
		_lon = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
		_alt = (float)lla.getAltitude().getDoubleValue(Length.METERS);
                
		byte[] data = new byte[4 * 4 + 8];
		int index = 0;
                
                index = ByteManipulator.addFloat(data, _concentration, index, false);
		index = ByteManipulator.addFloat(data, _lat, index, false);
		index = ByteManipulator.addFloat(data, _lon, index, false);
		index = ByteManipulator.addFloat(data, _alt, index, false);
                index = ByteManipulator.addLong(data, _timeMs, index, false);
		return data;
	}

	public void readExternal(DataInput in) throws IOException {
                _concentration = in.readFloat();
		_lat = in.readFloat();
		_lon = in.readFloat();
		_alt = in.readFloat();
                _pos = new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES),
				new Longitude(_lon, Angle.DEGREES),
				new Altitude(_alt, Length.METERS));
                _timeMs = in.readLong();
        
	}
        
        public boolean equals(Object o) {
		if (o instanceof EtdDetection) {
			EtdDetection other = (EtdDetection)o;
			if (other.getTime() == getTime())
			{
				return true;
			}
			else 
				return false;
		}
		return false;
	}
        
        public int compareTo(EtdDetection etd) {
            if(_timeMs==etd.getTime()) {
                return 0;
            } else if (_timeMs<etd.getTime()) {
                return -1;
            } else {
                return 1;
            }
        }
}
