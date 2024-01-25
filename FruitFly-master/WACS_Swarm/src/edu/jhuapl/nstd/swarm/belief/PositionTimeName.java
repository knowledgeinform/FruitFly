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
import edu.jhuapl.nstd.swarm.util.*;

/**
 * This is the interface for the repository for all beliefs
 *
 * <P>UNCLASSIFIED
 *
 * @author Chad Hawthorne ext. 3728
 */


public class PositionTimeName extends TimeName implements BeliefExternalizable {
  protected transient AbsolutePosition pos;
  protected transient NavyAngle heading;
  protected transient Length error = Length.ZERO;
  
  /** Storage for serialization */
	private float _lat = 0.0f;
	private float _lon = 0.0f;
	private float _alt = 0.0f;
	private float _heading = 0.0f;
	private float _error = 0.0f;

	protected PositionTimeName() {}

  /**
   * Constructor to associate a position, timestamp, and unique agentid
   * @param pos position
   * @param time timestamp
   * @param name unique agentid
   */  
  public PositionTimeName(AbsolutePosition pos, Date time, String name)
  {
    super(time, name);
    this.pos = pos;
  }
  
  public PositionTimeName(AbsolutePosition pos, Date time, String name, Length error) 
  { 
     this(pos, time, name);
     this.error = error;
  }
  
   public PositionTimeName(AbsolutePosition pos, Date time, String name, NavyAngle heading) 
  { 
     this(pos, time, name);
     this.heading = heading;
  }
   
   public PositionTimeName(AbsolutePosition pos, Date time, String name, NavyAngle heading, Length error) 
  { 
     this(pos, time, name);
     this.heading = heading;
     this.error = error;
  }
   
   @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof PositionTimeName))
        {
            return false;
        }
        return (Math.abs(pos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) - ((PositionTimeName)(obj)).pos.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES)) < 0.00001 && 
                Math.abs(pos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) - ((PositionTimeName)(obj)).pos.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES)) < 0.00001 && 
                Math.abs(pos.asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET) - ((PositionTimeName)(obj)).pos.asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET)) < 1.5
                );
    }
  
  /**
   * Returns the position.
   * @return position
   */  
  public AbsolutePosition getPosition(){
    return pos;
  }
  
  public NavyAngle getHeading()
  {
    return heading;
  }
  
	public Length getError() {
		return error;
	}

	private void writeObject(java.io.ObjectOutputStream out)
		throws IOException
	{
		LatLonAltPosition lla = pos.asLatLonAltPosition();
		_lat = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
		_lon = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
		_alt = (float)lla.getAltitude().getDoubleValue(Length.METERS);
		_heading = (float)heading.getDoubleValue(Angle.DEGREES);
		_error = (float)error.getDoubleValue(Length.METERS);

		out.defaultWriteObject();
	}  

	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		pos = new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES),
				new Longitude(_lon, Angle.DEGREES),
				new Altitude(_alt, Length.METERS));
		heading = new NavyAngle(_heading, Angle.DEGREES);
		error = new Length(_error, Length.METERS);
	}  

	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);

		LatLonAltPosition lla = pos.asLatLonAltPosition();
		_lat = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
		_lon = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
		_alt = (float)lla.getAltitude().getDoubleValue(Length.METERS);
		_heading = Float.NaN;
		if (heading != null)
			_heading = (float)heading.getDoubleValue(Angle.DEGREES);
        if(error != null)
            _error = (float)error.getDoubleValue(Length.METERS);

		byte[] bytes = new byte[5 * 4];

		int index = 0;
		index = ByteManipulator.addFloat(bytes, _lat, index, false);
		index = ByteManipulator.addFloat(bytes, _lon, index, false);
		index = ByteManipulator.addFloat(bytes, _alt, index, false);
		index = ByteManipulator.addFloat(bytes, _heading, index, false);
		index = ByteManipulator.addFloat(bytes, _error, index, false);
		out.write(bytes);	
	}

	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);

		byte[] bytes  = new byte[5 * 4];
		in.readFully(bytes);
		int index = 0;
		_lat = ByteManipulator.getFloat(bytes, index, false);
		index += 4;
		_lon = ByteManipulator.getFloat(bytes, index, false);
		index += 4;
		_alt = ByteManipulator.getFloat(bytes, index, false);
		index += 4;
		_heading = ByteManipulator.getFloat(bytes, index, false);
		index += 4;
		_error = ByteManipulator.getFloat(bytes, index, false);
		index += 4;

		pos = new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES),
				new Longitude(_lon, Angle.DEGREES),
				new Altitude(_alt, Length.METERS));
		if (_heading != Float.NaN)
			heading = new NavyAngle(_heading, Angle.DEGREES);
		error = new Length(_error, Length.METERS);
	}

	public String toString() {
		return "[" + name + " " + pos + " @" + time + "]";
	}
}

//=============================== UNCLASSIFIED ==================================
