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

// Created by Steven Marshall under S7102XXXSTI


package edu.jhuapl.nstd.swarm.display;


import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.display.event.*;
import edu.jhuapl.nstd.swarm.util.Config;

import edu.jhuapl.jlib.math.*;



import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.WACSAgent;
import java.util.*;

/**
 * This class is notified when the position belief is updated, and keeps
 * track of which agents are in the field, and their current positions.
 * It then notifies its listeners of new agents. This class also allows 
 * for one agent to be selected for visualization or configuration tasks.
 */
public class AgentTracker implements Updateable
{
    private Map _agentMap;
    private LinkedList _listeners;
    private String _selectedAgent;
    private BeliefManager _belMgr;
    private int updatecount;

    private NavyAngle _avgWindDir;
    private Speed _avgWindSpeed;

    protected HashMap<String, TreeSet> _positionHistory;
    protected HashMap<String, TreeSet> _windHistory;
    protected HashMap<String, TreeSet> _detectionHistory;

	private Altitude _maxAlt;
	private Altitude _minAlt;

	private long _maxAltTime;
	private long _minAltTime;

	private int _maxHistoryAge = Config.getConfig().getPropertyAsInteger("AgentTracker.maxPositionHistoryAge.Ms", 240000);
        private int _maxWindHistoryAge = Config.getConfig().getPropertyAsInteger("AgentTracker.maxPositionHistoryAge.Ms", 240000);
        private int _maxDetectionHistoryAge = Config.getConfig().getPropertyAsInteger("AgentTracker.maxDetectionHistoryAge.Ms", 240000);

        long _lastDetectionTime_ms;

	public AgentTracker(BeliefManager beliefManager)
        {
            _agentMap = Collections.synchronizedMap(new HashMap());
            _listeners = new LinkedList();
            _belMgr = beliefManager;
            _positionHistory = new HashMap<String, TreeSet>();
            _windHistory = new HashMap<String, TreeSet>();
            _detectionHistory = new HashMap<String, TreeSet>();
	}

	public synchronized void update() 
        {
            try
            {
                updatecount++;

                AgentPositionBelief belief = (AgentPositionBelief)_belMgr.get(AgentPositionBelief.BELIEF_NAME);
                METPositionBelief metbel = (METPositionBelief)_belMgr.get(METPositionBelief.BELIEF_NAME);

                if (belief != null)
                {
                synchronized (belief)
                {
                            Iterator itr = belief.getAll().iterator();
                            while(itr.hasNext())
                            {
                                    PositionTimeName posTimeName = (PositionTimeName)itr.next();
                                    String name = posTimeName.getName();
                                    addPositionHistory(name, posTimeName);
                                    if (!_agentMap.containsKey(name)) {
                                            // don't care if the whole belief gets updated at once
                                            synchronized (_agentMap) {
                                                    _agentMap.put(name, posTimeName);
                                            }

                                            fireAgentTrackerEvent(AgentTrackerEvent.AGENT_ADDED, posTimeName);
                                    } else {
                                            synchronized (_agentMap) {
                                                    _agentMap.put(name, posTimeName);
                                            }
                                    }

                            }
                            Iterator i = _agentMap.values().iterator();
                            Collection c = belief.getAll();
                            ArrayList removeList = new ArrayList();
                            //if we have no positions in our beliefs, then our agent map should also be empty
                            if (c.size() == 0){
                                    //remove all agents
                                    while (i.hasNext()){
                                            PositionTimeName timeName = (PositionTimeName)i.next();
                                    removeList.add(timeName);
                                    }
                            }
                            else{
                                    //remove any positions that are in our map, but not in the belief
                                    while (i.hasNext()){
                                            //check to see if each value is in the position belief
                                            PositionTimeName timeName = (PositionTimeName)i.next();
                                            if (!c.contains(timeName)){
                                            removeList.add(timeName);
                                            }
                                    }
                            }

                            i = removeList.iterator();
                            while (i.hasNext()) {
                             PositionTimeName timeName = (PositionTimeName)i.next();
                             _agentMap.remove(timeName.getName());
                             fireAgentTrackerEvent(AgentTrackerEvent.AGENT_REMOVED, timeName);
                            }
                    }


                    if (metbel == null || (updatecount%5!=0))
                                    return;
                    synchronized (metbel)
                    {
                        Iterator itr = metbel.getAll().iterator();
                        while(itr.hasNext())
                        {
                            METPositionTimeName met = (METPositionTimeName)itr.next();
                            String name = met.getName();
                            addPositionWindHistory(name, met);
                        }

                    }
                    
 
                }            

    //        CloudDetectionBelief cloudDetectionBelief = (CloudDetectionBelief) _belMgr.get(CloudDetectionBelief.BELIEF_NAME);
    //
    //        if (cloudDetectionBelief != null)
    //        {
    //            String name = cloudDetectionBelief.getName();
    //            for (CloudDetection cloudDetection : cloudDetectionBelief.getDetections())
    //            {
    //                if (cloudDetection.getTime() > _lastDetectionTime_ms && cloudDetection.getValue() >= 1)
    //                {
    //                    addDetectionHistory(WACSAgent.AGENTNAME, cloudDetection);
    //                    _lastDetectionTime_ms = cloudDetection.getTime();
    //                }
    //            }
    //        }
            }
            catch (Exception e)
            {
                System.err.println ("Exception in update thread - caught and ignored");
                e.printStackTrace();
            }
        }

	/**
	 * Returns the currently selected agent
	 */
	public synchronized String getSelectedAgent() {
		return _selectedAgent;
	}

	/**
	 * Sets the currently selected agent, notifying all listeners
	 */
	public synchronized void setSelectedAgent(String name) {
		setSelectedAgent(name, null);
	}
	
	/**
	 * Sets the currently selected agent, but does not send a message to the
	 * specified listener.
	 */
	public synchronized void setSelectedAgent(
		String name, AgentTrackerListener ignore) 
	{
		_selectedAgent = name;
		if (name == null)
			fireAgentTrackerEvent(AgentTrackerEvent.AGENT_SELECTED,
				null, ignore);
		else
			fireAgentTrackerEvent(AgentTrackerEvent.AGENT_SELECTED,
				(PositionTimeName)_agentMap.get(name), ignore);
	}
	
	/**
	 * Clear all agent knowledge
	 */
	public void clear() {
		boolean clearedSelection = false;

		// TODO make sure this is correct. Maybe we should make our locking less
		// (or more) granular.
		synchronized (_agentMap) {
			synchronized (this) {
				_agentMap.clear();

				if (_selectedAgent != null) {
					clearedSelection = true;
					_selectedAgent = null;
				}
			}
		}

		// TODO send remove events
		if (clearedSelection)
			fireAgentTrackerEvent(AgentTrackerEvent.AGENT_SELECTED, null);
	}
	
	/**
	 * Retrieve currently cached position information from the specified agent.
	 */
	public PositionTimeName getAgentInfo(String name) {
		PositionTimeName ptn = null;
		synchronized (_agentMap) {
			ptn = (PositionTimeName)_agentMap.get(name);
		}
		return ptn;
	}

	/**
	 * Retrieve currently cached position information from the specified agent that is
	 * closest to the specified time. Note that this method will return the oldest date
	 * available if the time is earlier than the cut off for position history caching.
	 */
	public PositionTimeName getAgentInfo(String name, Date time)
        {
            synchronized(_positionHistory)
            {
		TreeSet t = _positionHistory.get(name);

		SortedSet s = t.tailSet(
				new PositionTimeName(null, time, ""));

		return (s == null) ? null : (PositionTimeName)s.first();
            }
	}

	/** 
	 * Returns a list of the names of the agents currently being tracked
	 */
	public Iterator getAllAgents() {
		return _agentMap.keySet().iterator();
	}

	public int getAgentCount() {
		return _agentMap.size();
	}



    private void addPositionWindHistory(String name, METPositionTimeName p )
    {
        synchronized(_windHistory)
        {
            Collection c = _windHistory.get(WACSAgent.AGENTNAME);
            int cnt;
            if (c!=null)
            {
                cnt = c.size();
                if(cnt < 2)
                {
                    _avgWindSpeed = p.getWindSpeed();
                    _avgWindDir = p.getWindBearing();
                }
                else
                {
                    LatLonAltPosition startPosition = LatLonAltPosition.ORIGIN;
                    RangeBearingHeightOffset avgcomponent = new RangeBearingHeightOffset(new Length(_avgWindSpeed.getDoubleValue(Speed.METERS_PER_SECOND)*(cnt-1)/cnt,Length.METERS), _avgWindDir, new Length(0,Length.METERS));
                    RangeBearingHeightOffset newcomponent = new RangeBearingHeightOffset(new Length(p.getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND)/cnt,Length.METERS), p.getWindBearing(), new Length(0,Length.METERS));
                    startPosition = startPosition.translatedBy(avgcomponent).asLatLonAltPosition();
                    startPosition = startPosition.translatedBy(newcomponent).asLatLonAltPosition();
                    _avgWindDir = LatLonAltPosition.ORIGIN.getBearingTo(startPosition);
                    _avgWindSpeed = new Speed(LatLonAltPosition.ORIGIN.getRangeTo(startPosition).getDoubleValue(Length.METERS),Speed.METERS_PER_SECOND);
                }
            }

            TreeSet t = _windHistory.get(name);

                    if (t == null)
            {
                t = new TreeSet(new TimeComparator());
                    _windHistory.put(name, t);
                    }
                    t.add(p);

                    // this could be quite expensive. We have to dupe the set because of
                    // concurrent modification exceptions

                    SortedSet subSet = new TreeSet(t.headSet(new METPositionTimeName("", null, null, null, new Date(System.currentTimeMillis() - _maxWindHistoryAge))));
                    if (subSet != null && subSet.size() > 0)
                            t.removeAll(subSet);
        }

    }
	
	private void addPositionHistory(String name, PositionTimeName p) 
        {
            synchronized(_positionHistory)
            {
		TreeSet t = _positionHistory.get(name);
		if (t == null) {
		t = new TreeSet(new TimeComparator());
			_positionHistory.put(name, t);
		}
		t.add(p);

		// this could be quite expensive. We have to dupe the set because of
		// concurrent modification exceptions
		SortedSet subSet = new TreeSet(t.headSet(new PositionTimeName(null, new Date(System.currentTimeMillis() - _maxHistoryAge), "")));
		if (subSet != null && subSet.size() > 0)
			t.removeAll(subSet);

		long now = System.currentTimeMillis();

		if (_maxAlt == null || p.getPosition().asLatLonAltPosition().getAltitude().isHigherThan(_maxAlt)) {
			_maxAlt = p.getPosition().asLatLonAltPosition().getAltitude();
			_maxAltTime = now;
		}
		
		if (_minAlt == null || p.getPosition().asLatLonAltPosition().getAltitude().isLowerThan(_minAlt)) {
			_minAlt = p.getPosition().asLatLonAltPosition().getAltitude();
			_minAltTime = now;
		}

		if (now - _maxAltTime > _maxHistoryAge) 
			recalculateMax();
		if (now - _minAltTime > _maxHistoryAge)
			recalculateMin();
            }
	}


	private void recalculateMax() 
        {
            synchronized(_positionHistory)
            {
		_maxAlt = null;
	
		Iterator<TreeSet> treeItr = _positionHistory.values().iterator();
		while (treeItr.hasNext())
                {
			Iterator<PositionTimeName> ptnItr = treeItr.next().iterator();
			while (ptnItr.hasNext())
                        {
				PositionTimeName p = ptnItr.next();

				if (_maxAlt == null || p.getPosition().asLatLonAltPosition().getAltitude().isHigherThan(_maxAlt))
					_maxAlt = p.getPosition().asLatLonAltPosition().getAltitude();
			}
		}
            }
	}

	
	private void recalculateMin() 
        {
            synchronized(_positionHistory)
            {
		_minAlt = null;
	
		Iterator<TreeSet> treeItr = _positionHistory.values().iterator();
		while (treeItr.hasNext())
                {
			Iterator<PositionTimeName> ptnItr = treeItr.next().iterator();
			while (ptnItr.hasNext())
                        {
				PositionTimeName p = ptnItr.next();
                                if (_minAlt == null || p.getPosition().asLatLonAltPosition().getAltitude().isLowerThan(_minAlt))
                                	_minAlt = p.getPosition().asLatLonAltPosition().getAltitude();
			}
		}
            }
	}


	public TreeSet<PositionTimeName> getPositionHistory(String name)
        {
            synchronized(_positionHistory)
            {
		return (TreeSet<PositionTimeName>)_positionHistory.get(name).clone();
            }
	}


    public TreeSet<PositionTimeName> getDetectionHistory(String name)
    {
        synchronized(_detectionHistory)
        {
            return (TreeSet<PositionTimeName>)_detectionHistory.get(name).clone();
        }
    }

    public TreeSet<PositionTimeName> getWindHistory(String name)
    {
        synchronized(_windHistory)
        {
            if (_windHistory == null || name == null || _windHistory.get(name) == null)
                return null;
            return (TreeSet<PositionTimeName>)_windHistory.get(name).clone();
        }
    }

	public Altitude getMinimumAltitude() {
		synchronized(_positionHistory)
                {
                    return _minAlt;
                }
	}

	public Altitude getMaximumAltitude() {
		synchronized(_positionHistory)
                {
                    return _maxAlt;
                }
	}
	
	/**
	 * Add a listener for tracked agent events
	 */
	public void addAgentTrackerListener(AgentTrackerListener l) {
		if (!_listeners.contains(l))
			_listeners.add(l);
	}
	
	/**
	 * Removes a listener for tracked agent events
	 */
	public void removeAgentTrackerListener(AgentTrackerListener l) {
		_listeners.remove(l);
	}

	protected void fireAgentTrackerEvent(int type, PositionTimeName ptn) {
		fireAgentTrackerEvent(type, ptn, null);
	}

	protected void fireAgentTrackerEvent(int type, PositionTimeName ptn,
			AgentTrackerListener ignore) 
	{
		Iterator itr = _listeners.iterator();
		while (itr.hasNext()) {
			AgentTrackerListener listener = (AgentTrackerListener)itr.next();
			if (listener != ignore)
				listener.trackedAgentUpdate(
					new AgentTrackerEvent(this, type, ptn));
		}
	}


	public static class TimeComparator implements Comparator{
		public TimeComparator() {}
		
		public int compare(Object o1, Object o2){
			PositionTimeName p1 = (PositionTimeName)o1;
			PositionTimeName p2 = (PositionTimeName)o2;
			
			Date t1 = p1.getTime();
			Date t2 = p2.getTime();
			
			return t1.compareTo(t2);
		} 
	}

        public static class DetectionTimeComparator implements Comparator{
		public DetectionTimeComparator() {}

		public int compare(Object o1, Object o2){
			CloudDetection d1 = (CloudDetection)o1;
			CloudDetection d2 = (CloudDetection)o2;

                        return (int)(d1.getTime() - d2.getTime());
		}
	}
}
