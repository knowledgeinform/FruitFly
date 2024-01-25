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


import java.io.*;



import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.nstd.swarm.util.*;

/**
 * This is the belief that stores generic operator goals.
 * 
 * <P>UNCLASSIFIED
 */


public class OperatorGoalBelief extends Belief implements BeliefExternalizable {
	
	/**
	 * The unique name for this belief type.
	 */	
	public static final String BELIEF_NAME = "OperatorGoalBelief";
	
	protected ArrayList<OperatorGoal> _operatorGoals;

	//this is the number of ms that we will keep old operator goals
	//if they get older than this value they will be removed
	protected transient long degrade = 0;


	/**
	 * Empty constructor.
	 */	
	public OperatorGoalBelief() {
		propertyChange(null);
	}
	
 
	/**
	 * Builds a goal belief.
	 *
	 * @param agentID The id of the agent that created this belief.
	 */	
	public OperatorGoalBelief(String agentID, OperatorGoalType type, String goal) {
		super(agentID);

		// this goal is hardcore
		OperatorGoal og = new OperatorGoal(type, goal);
		init(og);
	}

	public OperatorGoalBelief(String agentID, OperatorGoal goal) {
		super(agentID);

		init(goal);
	}

	protected void init(OperatorGoal og) {
		_operatorGoals = new ArrayList<OperatorGoal>();
		timestamp = new Date();

		_operatorGoals.add(og);
		propertyChange(null);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		degrade = Config.getConfig().getPropertyAsLong("operatorgoalbelief.decaytime", 0);
	}

	/**
	 * Returns timestamped classifications of all known targets to the swarm.
	 * @return Returns a collection of ClassificationTimeNames
	 */	
	public synchronized ArrayList<OperatorGoal> getAll(){
		return _operatorGoals;
	}
	
	public ArrayList<OperatorGoal> getOperatorGoals(OperatorGoalType type) {
		ArrayList<OperatorGoal> realOgs = new ArrayList<OperatorGoal>();

		for (OperatorGoal og : _operatorGoals) {
			if (og.getType() == type) {
				realOgs.add(og);
			}
		}

		return realOgs;
	}

	/**
	 * Add a belief to this belief. They should be of the same type.
	 * @param b the belief to add to this belief
	 */	
	public synchronized void addBelief(Belief b){
		if (b.getTimeStamp().compareTo(timestamp) > 0)
			timestamp = b.getTimeStamp();
		
		OperatorGoalBelief belief = (OperatorGoalBelief)b;
		ArrayList<OperatorGoal> incomingGoals = belief.getAll();
		for (OperatorGoal og : incomingGoals) {
			int idx = _operatorGoals.indexOf(og);
			if (idx != -1) {
				if (og.isNewerThan(_operatorGoals.get(idx)))
					_operatorGoals.set(idx, og);
			} else {
				_operatorGoals.add(og);
			}
		}
	}
	
	
	public synchronized void offsetTime(Date localTime) {
		super.offsetTime(localTime);

		// the compensation is configured in Belief
		long diff = localTime.getTime() - transmissionTime.getTime() - 
			_latencyCompensation;
		for (OperatorGoal og : _operatorGoals) {
			og.offsetTime(diff);
		}
	}

	 /**
	 * Overrides the parent update to degrade the belief every update.
	 */	
	public void update()
        {
            try
            {
		super.update();
		//cycle through the collection and prune all beliefs older than our degrade value in ms
		decayBeliefs();
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
	}

	private synchronized void decayBeliefs(){
		if (degrade > 0) {
			ArrayList<Integer> removeList = new ArrayList<Integer>();
			int idx = 0;
			long currentTime = System.currentTimeMillis();
			for (OperatorGoal og : _operatorGoals) {
				if (currentTime - og.getTimestamp().getTime() > degrade)
				{
					removeList.add(idx);
				}

				idx++;
			}

			for (int i : removeList) {
				_operatorGoals.remove(i);
			}
		} 
	}
	
	/**
	 * Returns the unique name of this belief type.
	 * @return The unique name of this belief type.
	 */	
	public String getName(){
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
	
		OperatorGoalBelief belief = null;

		try {
			belief = (OperatorGoalBelief)clas.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		belief.readExternal(in);

		return belief;
	}

	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);

		byte[] bytes = new byte[2];

		int index = 0;
		index = ByteManipulator.addShort(bytes, (short)_operatorGoals.size(), 0, false);
		out.write(bytes);	

		for (OperatorGoal og : _operatorGoals) {
			og.writeExternal(out);
		}
	}

	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		
		_operatorGoals = new ArrayList<OperatorGoal>();

		byte[] bytes = new byte[2];
		in.readFully(bytes);
		int numElements = ByteManipulator.getShort(bytes, 0, false);

		for (int i = 0; i < numElements; i++) {
			OperatorGoal og = new OperatorGoal();
			og.readExternal(in);
			_operatorGoals.add(og);
		}
	}

	public String toString() {
		String ret = "[OperatorGoalBelief {";
		for (OperatorGoal og : _operatorGoals) {
			ret += og;
		}
		ret += "}]";
		return ret;
	}
}
//=============================== UNCLASSIFIED ==================================
