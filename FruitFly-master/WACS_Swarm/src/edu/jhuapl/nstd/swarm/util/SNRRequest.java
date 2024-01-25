package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;


import java.io.*;

public class SNRRequest {
	public int _desiredSNR;
	public long _time;


	public SNRRequest(int desiredSNR) {
		_desiredSNR = desiredSNR;
		_time = System.currentTimeMillis();
	}

	public int numBytes() {
		return 4 + 8;
	}

	public byte[] writeExternal() {
		byte[] toReturn = new byte[numBytes()];
		int index = 0;
		index = ByteManipulator.addInt(toReturn, _desiredSNR ,index, false);
		index = ByteManipulator.addLong(toReturn, _time, index, false);
		return toReturn;
	}

	public String toString() {
		return _desiredSNR + " " + _time;
	}
}
