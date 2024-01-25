/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.io.*;
import java.util.*;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;

/**
 *
 * @author stipeja1
 */


/**
 * A belief for transmitting MET or meteorological data.
 */
public class METPositionBelief extends AgentPositionBelief
{

    /**
     * The unique string for this belief type.
     */
    public static final String BELIEF_NAME = "METPositionBelief";

    public METPositionBelief()
    {
        this ("unspecified");
    }
    
    /**
     * Empty constructor.
     */
    public METPositionBelief(String agentID)
    {
        this (agentID, new METPositionTimeName(agentID, NavyAngle.ZERO, Speed.ZERO, LatLonAltPosition.ORIGIN, new Date (System.currentTimeMillis())));
    }
    
    public METPositionBelief(String agentID, METPositionTimeName met)
    {
        this (agentID, met, new Date(TimeManagerFactory.getTimeManager().getTime()));
    }

    public METPositionBelief(String agentID, METPositionTimeName met, Date time)
    {
      super(agentID,met.getPosition());
      timestamp = time;
      synchronized(this){
        elements.put(agentID, met);
      }
      degradeProperty =  new String("METPositionBelief.decaytime");
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
	public synchronized METPositionTimeName getMETPositionTimeName(String agentID)
    {
	  return (METPositionTimeName)elements.get(agentID);
	}

    protected TimeName readTimeName(DataInput in) throws IOException {
		TimeName ptn = new METPositionTimeName();
		ptn.readExternal(in);
		return ptn;
	}


//    public byte[] serialize() throws IOException
//    {
//		setTransmissionTime();
//		ByteArrayOutputStream baStream = new ByteArrayOutputStream();
//		DataOutputStream out = new DataOutputStream(baStream);
//
//		writeExternal(out);
//		out.flush();
//		out.close();
//		return baStream.toByteArray();
//	}
//
//	public static Belief deserialize(InputStream iStream, Class clas) throws IOException
//    {
//		DataInputStream in = new DataInputStream(iStream);
//
//		METPositionBelief belief = null;
//
//		try {
//			belief = (METPositionBelief)clas.newInstance();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//        		belief.readExternal(in);
//
//		return belief;
//	}

}
