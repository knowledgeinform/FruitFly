//=============================== UNCLASSIFIED ==================================
//
// Copyright (c) 2007 The Johns Hopkins University/Applied Physics Laboratory
// Developed by JHU/APL.
//
// This material may be reproduced by or for the U.S. Government pursuant to
// the copyright license under the clause at DFARS 252.227-7013 (OCT 1988).
// For any other permissions please contact JHU/APL.
//
//=============================== UNCLASSIFIED ==================================


package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;




import java.util.*;

import java.io.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;

import edu.jhuapl.nstd.swarm.util.*;

/**
 * 
 *
 * <P>UNCLASSIFIED
 */


public class OperatorGoal implements BeliefExternalizable, Serializable {
	
	protected Date _timestamp;

	/** The time after the timestamp in which this goal should no longer
	 * be activated. Relative so the timestamp can be altered when using
	 * relative time comms.
	 *
	 * Goals that have timed out can be safely culled. See active.
	 */
	protected long _timeout;

	protected OperatorGoalType _type;

	protected String _goal;
	
	/** Goals can be flagged as inactive by the operator to halt their
	 * progress in the swarm. These must remain in transmission until
	 * the timeout is met or indefinately if no global decay is set. */
	protected boolean _active = true;


	protected final static HashMap<Integer, OperatorGoalType> _operatorGoalMap;

	static {
		_operatorGoalMap = new HashMap<Integer, OperatorGoalType>();
		for (OperatorGoalType type : OperatorGoalType.values()) {
			_operatorGoalMap.put(type.ordinal(), type);
		}
	}

	protected OperatorGoal() {}

	/**
	 */	
	public OperatorGoal(OperatorGoalType type, String goal) {
		_timestamp = new Date();
		_type = type;
		_goal = goal;
	}

	public OperatorGoal(OperatorGoalType type, String goal, long timeout) {
		this(type, goal);
		_timeout = timeout;
	}

	public OperatorGoal(OperatorGoalType type, String goal, boolean active) {
		this(type, goal);
		_active = active;
	}

	public OperatorGoalType getType() {	return _type; }
	public String getGoal() { return _goal; }
	public Date getTimestamp() { return _timestamp; }
	public long getTimeout() { return _timeout; }
	public boolean isActive() { return _active; }

	public void setActive(boolean active) {
		if (active == _active)
			return;

		_timestamp = new Date();
		_active = active;
	}
	
	public boolean isTimedOut() {
		return (System.currentTimeMillis() - _timestamp.getTime() + _timeout) > 0;
	}

	public boolean isNewerThan(OperatorGoal og) {
		return _timestamp.compareTo(og.getTimestamp()) > 0;
	}
 
	public void offsetTime(long diff) {
		_timestamp.setTime(_timestamp.getTime() + diff);
	}

	public void writeExternal(DataOutput out) throws IOException {
		byte[] bytes = new byte[8 + 8 + 1 + 4 + 2 + _goal.length()];

		int index = 0;
		index = ByteManipulator.addLong(bytes, _timestamp.getTime(), index, false);
		index = ByteManipulator.addLong(bytes, _timeout, index, false);
		index = ByteManipulator.addBoolean(bytes, _active, index, false);
		index = ByteManipulator.addInt(bytes, _type.ordinal(), index, false);
		index = ByteManipulator.addString(bytes, _goal, index, false);

		out.write(bytes);	
	}

	public void readExternal(DataInput in) throws IOException {
		byte[] bytes = new byte[8 + 8 + 1 + 4];
		in.readFully(bytes);

		int index = 0;
		_timestamp = new Date(ByteManipulator.getLong(bytes, index, false));
		index += 8;
		_timeout = ByteManipulator.getLong(bytes, index, false);
		index += 8;
		_active = ByteManipulator.getBoolean(bytes, index, false);
		index += 1;
		_type = _operatorGoalMap.get(ByteManipulator.getInt(bytes, index, false));
		index += 4;

		_goal = ByteManipulator.getString(in, false);
	}

	public int hashCode() {
		return (_goal + _type).hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof OperatorGoal))
			return false;
		OperatorGoal og = (OperatorGoal)o;
		if (og.getType().equals(_type) && og.getGoal().equals(_goal))
			return true;
		return false;
	}

	public String toString() {
		return "[" + (_active ? "+" : "-") + "OperatorGoal " + _type + " " + _goal 
			+ " " + _timestamp + "]";
	}
}

//=============================== UNCLASSIFIED ==================================
