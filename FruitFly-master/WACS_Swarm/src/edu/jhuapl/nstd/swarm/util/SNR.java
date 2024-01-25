package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;


import java.io.*;
import edu.jhuapl.nstd.swarm.belief.*;

public class SNR {
	public String _agent1;
	public String _agent2;
	public double _snr;
	public int _mtm;


	public SNR(String a1, String a2, double snr, int mtm) {
		_agent1 = a1;
		_agent2 = a2;
		_snr = snr;
		if (_snr > SNRBelief.SNR_MAX)
			_snr = 0.0;
		_mtm = mtm;
	}

	public boolean equals(String a1, String a2) {
		if (_agent1.equalsIgnoreCase(a1) &&
				_agent2.equalsIgnoreCase(a2))
			return true;
		return false;
	}

	public boolean equals(Object other) {
		SNR o = (SNR)other;
		if (o._agent1.equalsIgnoreCase(_agent1) &&
				o._agent2.equalsIgnoreCase(_agent2))
			return true;
		return false;
	}

	public int hashCode() {
		return (_agent1 + _agent2).hashCode();	
	}

	public int numBytes() {
		//2 bytes + agent1 length
		//2 bytes + agent2 length
		//4 bytes snr
		//4 bytes mtm
		return 4 + _agent1.length() + _agent2.length() + 4 + 4;
	}

	public byte[] writeExternal() {
		byte[] toReturn = new byte[numBytes()];
		int index = 0;
		index = ByteManipulator.addString(toReturn, _agent1 ,index, false);
		index = ByteManipulator.addString(toReturn, _agent2, index, false);
		index = ByteManipulator.addFloat(toReturn, (float)_snr, index, false);
		index = ByteManipulator.addInt(toReturn, _mtm, index, false);
		return toReturn;
	}

	public String toString() {
		return _agent1 + " " + _agent2 + " " + _snr + " " + _mtm;
	}
}
