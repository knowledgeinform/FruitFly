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

import java.lang.*;
import java.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.collections.storage.*;
import edu.jhuapl.jlib.math.position.*;
import java.io.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;

/**
 * This class has immutable timestamped mode information for an agent
 *
 * <P>UNCLASSIFIED
 * @author Chad Hawthorne ext. 3728
 */



public class ModeTimeName extends TimeName implements BeliefExternalizable{
    /**
     * The mode.
     */    
  private Mode mode;
  

  protected ModeTimeName() {
	  super();
  }

  /**
   * Constructor for timestamped mode information.
   * @param mode The mode.
   * @param time Timestamp
   * @param name The unique agentid
   */  
  public ModeTimeName(Mode mode, Date time, String name){
    super(time, name);
    this.mode = mode;
  }
  
  public Mode getMode(){
    return mode;
  }
  
  @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof ModeTimeName))
        {
            return false;
        }
        return (getMode().getName().equals(((ModeTimeName)obj).getMode().getName()));
    }
  
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);

		byte[] bytes = new byte[2 + mode.getName().length()];

		ByteManipulator.addString(bytes, mode.getName(), 0, false);
		out.write(bytes);	
	}

	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);

		byte[] lenBytes = new byte[2];
		in.readFully(lenBytes, 0, 2);
		short strLen = ByteManipulator.getShort(lenBytes, 0, false);

		byte[] strBytes = new byte[strLen];
		in.readFully(strBytes);
		mode = new Mode(new String(strBytes));	
	}
}

//=============================== UNCLASSIFIED ==================================
