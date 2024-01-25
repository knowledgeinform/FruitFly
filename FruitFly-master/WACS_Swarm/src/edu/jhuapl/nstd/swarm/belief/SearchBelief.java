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

package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;

import java.lang.*;



import java.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.jlib.collections.storage.*;
import java.awt.Point;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;

import java.io.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This is the interface for the repository for all beliefs
 *
 * <P>UNCLASSIFIED
 *
 * @author Chad Hawthorne ext. 3728
 */

public class SearchBelief extends Belief implements PropertyChangeListener, BeliefExternalizable {

    /**
     * The name of this belief type.
     */    
  public static final String BELIEF_NAME = "SearchBelief";

  /**
   * The value in the search grid that corresponds to a completely searched area. Value is 1.0.
   */  
  public static final float SEARCHED_VALUE = 1.0f;
  /**
   * The value in the searched grid that corresponds to a half search grid cell. Value is 0.5
   */  
  public static final float HALF_SEARCHED_VALUE = 0.5f;
  /**
   * Corresponds to an un-searched grid cell. Value is 0.0
   */  
  public static final float NOT_SEARCHED_VALUE = 0.0f;

  protected PrimitiveTypeGeocentricMatrix matrix;

  /**
   * The value to multiply the grid by every timestep to degrade the belief.
   */  
  public float degrade;

  /**
   * Value used for logging. This should be removed and we should use java 1.4 logging features.
   */  
  public transient int searchednessVerbosityLevel = 0; // enable debug output

  public transient boolean abortOnMatrixMismatch = Config.getConfig().getPropertyAsBoolean(
		"SearchBelief.abortOnMatrixMismatch", false);

  /**
   * Empty Constructor
   */  
  
  public static double getPercentSearched(SearchBelief belief, SearchGoalBelief goal)
  {
      //get the matrices for the beliefs
      PrimitiveTypeGeocentricMatrix goalMatrix = goal.getStateSpace();
      PrimitiveTypeGeocentricMatrix currentStateMatrix = belief.getStateSpace();
		
		if (goalMatrix == null) return -1.0;
		
      //for every non-zero value in the goal matrix, calculate the vector
      //for the SearchBelief
      int xSize = goalMatrix.getXSize();
      int ySize = goalMatrix.getYSize();
      int counter = 0;
      double sumSearched = 0.0;
      for (int xCount = 0;xCount<xSize;xCount++){
	for (int yCount = 0;yCount<ySize;yCount++){
	  if (goalMatrix.get(xCount, yCount) == 1){
	    counter++;
	    AbsolutePosition elementPosition = currentStateMatrix.getPosition(xCount, yCount);
	    float factor = currentStateMatrix.get(xCount, yCount);
	    sumSearched = sumSearched + factor;
	  }
	}
      }
      if (counter <= 0)
      {
	return -1.0;
      }
      else{
	return sumSearched/counter;
      }
    }
  
  
  public SearchBelief(){
	  propertyChange(null);
  }

  /**
   * Creates a new search grid with all cells set to 0.0 using the GridFactory.
   * @param agentID The name of the agent creating this search belief.
   */  
  public SearchBelief(String agentID){
      this (agentID, GridFactory.createCartesianMatrix());
	  // TODO SJM this is terrible (creating new matrix each time)
  }
  
  /**
   * @param agentID The name of the agent creating this search belief.
   * @param matrix
   */
  public SearchBelief(String agentID, PrimitiveTypeGeocentricMatrix matrix) {
      super(agentID);
      this.matrix = matrix;
      timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());

      propertyChange(null);
  }
  
  public SearchBelief(String agentID, PrimitiveTypeGeocentricMatrix matrix,
            AbsolutePosition position) {
      this(agentID, matrix);
      Point p = matrix.getPoint(position);
      if (p != null){
        matrix.set(p, SEARCHED_VALUE); // SET THE VALUE
      }
  }

	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
	  degrade = (float)(1.0 - Config.getConfig().getPropertyAsDouble("searchDecay", 0.0));
		//Logger.getLogger("GLOBAL").info("search decay is now: " + degrade);
	 
	}
  
  /**
   * This constructor is used to set the searchedness grid to reflect that some area has been searched.
   * @param agentID The id of the agent that created this belief.
   * @param position The position of the agent to apply a new "search" for.
   * @param compassBearing The bearing of the agent to apply a new search for.
   * @param searchednessTable The existing searchedness table. This will be modified to reflect a new search at the position and bearing passed in as a parameter.
   */  
  public SearchBelief(String agentID, AbsolutePosition position, NavyAngle compassBearing, double[][] searchednessTable)
  {
    this(agentID);
    
    Point p = matrix.getPoint(position);

    //this simple search grid change was added for the sept-04 demo
    
    if (p != null){
      matrix.set(p, SEARCHED_VALUE); // SET THE VALUE
    }
  }
  
  /**
   * This constructor is used to set the searchedness grid to reflect that some area has been searched.
   * @param agentID The id of the agent that created this belief.
   * @param position The position of the agent to apply a new "search" for.
   * @param searchednessTable The existing searchedness table. This will be modified to reflect a new search at the position and bearing passed in as a parameter.
   */  
  public SearchBelief(String agentID, AbsolutePosition position)
  {
    this(agentID);
   
    Point p = matrix.getPoint(position);

    //this simple search grid change was added for the sept-04 demo
    
    if (p != null){
      matrix.set(p, SEARCHED_VALUE); // SET THE VALUE
    }
  }

  /**
   * Returns the cells in this grid belief.
   * @return The matrix of grid cells.
   */  
  public PrimitiveTypeGeocentricMatrix getStateSpace(){
    return matrix;
  }

  /** NOTE: Only do this if you really know what you're doing. Stay away John Cole. */
	public void setStateSpace(PrimitiveTypeGeocentricMatrix matrix) {
		this.matrix = matrix;
		timestamp = new Date();
	}

  /**
   * The implementation of the add belief method for the SearchBelief.
   * @param b The belief to add to the SearchBelief.
   */  
  public void addBelief(Belief b){
    if (b.getTimeStamp().compareTo(timestamp) > 0)
      timestamp = b.getTimeStamp();

    SearchBelief belief = (SearchBelief)b;
    PrimitiveTypeGeocentricMatrix m = belief.getStateSpace();
	 if (abortOnMatrixMismatch)
	    matrix.add(m);
	 else {
		 try {
			 matrix.add(m);
		 } catch (Exception e) {
			 System.err.println("Warning: " + e.getMessage());
		 }
	 }
  }

  /**
   * Overrides the parent update to degrade the belief every update.
   */  
  public void update()
  {
      try
      {
        if (degrade != 0.0){
            matrix.multiplyScalar(degrade);
            super.update();
        }
      }
      catch (Exception e)
      {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
      }
  }

  /**
   * Returns the unique name for this belief type.
   * @return
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
		SearchBelief belief = new SearchBelief();

		belief.readExternal(in);

		return belief;
	}

	public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
		matrix.writeExternal(out);
	}

	public void readExternal(DataInput in) throws IOException {
        super.readExternal(in); 
		matrix = new PrimitiveTypeGeocentricMatrix();
		matrix.readExternal(in);
	}
}

//=============================== UNCLASSIFIED ==================================
