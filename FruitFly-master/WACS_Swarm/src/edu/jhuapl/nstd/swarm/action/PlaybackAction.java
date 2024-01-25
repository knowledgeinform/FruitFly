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

// File created: Tue Nov  9 13:10:34 1999

package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;

import java.lang.*;



import java.util.*;

import java.io.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.action.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import edu.jhuapl.nstd.swarm.util.Config;
/**
 * This is the behavior that will be used for file playback
 */

public class PlaybackAction implements Updateable {

	private BufferedReader _reader;
	private BeliefManager _beliefMgr;
	private String _agentID;
	private LatLonAltPosition _last = null;
	private NavyAngle _lastHeading = null;
	private HashMap<String,LatLonAltPosition> _lastPositions = new HashMap<String,LatLonAltPosition>();
	
	
	public PlaybackAction(BeliefManager belMgr, String agentID) {
		_beliefMgr = belMgr;
		_agentID = agentID;
		String fileName = Config.getConfig().getProperty(agentID + ".playback.name");
		try {
			Logger.getLogger("GLOBAL").info(agentID + " reading " + fileName);
			_reader = new BufferedReader(new FileReader(new File(fileName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update() 
        {
            	if (_reader == null) return;
		try {
			String line = _reader.readLine();
			if (line == null) {
				_reader.close();
				_reader = null;
				Logger.getLogger("GLOBAL").info("done");
				return;
			}

			StringTokenizer tok = new StringTokenizer(line);
			String agentID = tok.nextToken();
			double lat = Double.valueOf(tok.nextToken()).doubleValue();
			tok.nextToken();
			double lon = Double.valueOf(tok.nextToken()).doubleValue();
			LatLonAltPosition pos = 	new LatLonAltPosition(new Latitude(lat, Angle.DEGREES),
																											new Longitude(lon, Angle.DEGREES),
																											new Altitude(0, Length.METERS));
			NavyAngle head = NavyAngle.NORTH;
			LatLonAltPosition last = _lastPositions.get(agentID);
			if (last != null) {
				if (last.equals(pos)) {
					head = _lastHeading;
				}
				else
					head = last.getBearingTo(pos);
			}
			AgentPositionBelief positionBelief = new AgentPositionBelief(
					_agentID, 
					pos,
					head);

			_lastHeading = head;
			AgentBearingBelief abb = new AgentBearingBelief(agentID, head, head);
			_lastPositions.put(agentID,pos);
			_beliefMgr.put(positionBelief);
			_beliefMgr.put(abb);
		} catch (Exception e) {
			System.err.println ("Exception in update thread - caught and ignored");
                        e.printStackTrace();
		}
	}
}
			
