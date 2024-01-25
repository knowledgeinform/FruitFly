package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;


import java.io.*;



import java.util.*;

import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;

public class PacketInfoBelief extends TimeNameBelief {

    /**
     * The unique string for this belief type.
     */  
    public static final String BELIEF_NAME = "PacketInfoBelief";

    /**
     * Empty constructor.
     */  
    public PacketInfoBelief() { 	
      degradeProperty =  new String("packetInfoBelief.decaytime");
    }	
    
    public PacketInfoBelief(String agentID, int recvPackets, int sentPackets)
    {
      super(agentID);
      timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
      PacketInfoTimeName name = new PacketInfoTimeName(agentID, recvPackets, sentPackets, timestamp);
      synchronized(this){
        elements.put(agentID, name);
      }
      degradeProperty =  new String("packetInfoBelief.decaytime");
    }

	@Override
	public String getName() {
		//Returns unique name for this belief type
		return BELIEF_NAME;
	}
	/**
	 * Returns a timestamped position for a particular agent.
	 * @param agentID the agent id
	 * @return Returns the timestamped position for the agent passed in
	 */  
	public synchronized PacketInfoTimeName getPacketTimeName(String agentID){
	  return (PacketInfoTimeName)elements.get(agentID);
	}

	protected TimeName readTimeName(DataInput in) throws IOException {
		PacketInfoTimeName pitn = new PacketInfoTimeName();
		pitn.readExternal(in);
		return pitn;
	}
}
