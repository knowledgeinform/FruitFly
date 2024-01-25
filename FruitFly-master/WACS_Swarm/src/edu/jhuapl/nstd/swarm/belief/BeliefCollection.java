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

package edu.jhuapl.nstd.swarm.belief;

import java.lang.*;
import java.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.nstd.swarm.*;
import java.io.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * <P>UNCLASSIFIED
 */

public abstract class BeliefCollection extends Belief {
  
  protected transient HashMap elements = new HashMap();

  /** Storage for serialized version */
  protected TimeName[] timeNames;
  
  //this is the property that we will look for in the configuration file
  protected transient String degradeProperty = new String("");

  //this is the number of ms that we will keep old position names around
  //if they get older than this value they will be removed
  //the degrade value should be set by setting the decayProperty to point
  //to the appropriate property in the config file
  protected long degrade = 0;
  

  /**
	* A date at which no beliefs are allowed to exist previous to it.
	*/
	protected Date earliestBeliefDate = new Date(0);

  public BeliefCollection() {
	  this(null);
  }
  
  public BeliefCollection(String agentID) {
    super(agentID);
    if (degradeProperty != null && !degradeProperty.equals(""))
        degrade = Config.getConfig().getPropertyAsLong(degradeProperty, 0);
  }
  
  public void propertyChange(PropertyChangeEvent e) {
    super.propertyChange(e);
    earliestBeliefDate = new Date(Config.getConfig().getPropertyAsLong("belief.earliestBeliefDate", 0));
    if (degradeProperty != null && !degradeProperty.equals("")){
		 degrade = Config.getConfig().getPropertyAsLong(degradeProperty, 0);
    }
  }

  /**
   * Overrides the parent update to degrade the belief every update.
   */  
  public void update(){
    super.update();
    //cycle through the collection and prune all beliefs older than our degrade value in ms
    decayBeliefs();
  }
  
  /**
   * Returns timestamped positions of all elements of this belief collection
   * @return Returns a collection of TimeNames
   */	
  public synchronized Collection getAll(){
	 ArrayList list = new ArrayList(elements.size());
    list.addAll(elements.values());
    return list;
  }
  
  /**
   * Add a belief to this belief. They should be of the same type.
   * @param b the belief to add to this belief
   */	
  public synchronized void addBelief(Belief b){
    if (degrade <= 0 && degradeProperty != null && !degradeProperty.equals("")){
      degrade =  Config.getConfig().getPropertyAsLong(degradeProperty, 0);
    }
    if (b.getTimeStamp().compareTo(timestamp) > 0)
      timestamp = b.getTimeStamp();
    
    BeliefCollection belief = (BeliefCollection)b;
    Collection c = belief.getAll();
    Iterator i = c.iterator();
    while(i.hasNext()){
      TimeName tn = (TimeName)i.next();
      TimeName oldtn = (TimeName)elements.get(tn.getName());
      if (oldtn == null || tn.isNewerThan(oldtn)){
				elements.put(tn.getName(), tn);
      }
    }
  }
  
  public synchronized TimeName getTimeName(String name){
    return (TimeName)elements.get(name);
  }
  
  public synchronized void offsetTime(Date localTime) {
	  super.offsetTime(localTime);

    // the compensation is configured in Belief
    long diff = localTime.getTime() - transmissionTime.getTime() - 
      _latencyCompensation;
    Iterator itr = elements.values().iterator();
    while (itr.hasNext()) {
      TimeName tn = (TimeName)itr.next();
      tn.offsetTime(diff);
    }
  }

  protected synchronized void decayBeliefs(){
		if (degrade > 0) {
			Collection c= elements.values();
			ArrayList removeList = new ArrayList();
			Iterator i = c.iterator();
			while(i.hasNext()){
				TimeName timeName = (TimeName)i.next();
				long time = timeName.getTime().getTime();
				// SJM TODO XXX abs??
				if (timeName.getTime().compareTo(earliestBeliefDate) < 0 ||
						Math.abs(time-TimeManagerFactory.getTimeManager().getTime()) > degrade){
					removeList.add(timeName.getName());
				}
			}

			i = removeList.iterator();
			while (i.hasNext()) {
				String name = (String)i.next();
				elements.remove(name);
			}
		} 
  }

	protected void writeObject(java.io.ObjectOutputStream out)
		throws IOException
	{
		timeNames = new TimeName[elements.size()];
		int i = 0;
		for (Iterator itr = elements.values().iterator(); itr.hasNext(); i++) {
			timeNames[i] = (TimeName)itr.next();
		}

		out.defaultWriteObject();
	}  

	protected void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		elements = new HashMap();
		for (int i = 0; i < timeNames.length; i++) {
			elements.put(timeNames[i].getName(), timeNames[i]);
		}
	}  
}
//=============================== UNCLASSIFIED ==================================
