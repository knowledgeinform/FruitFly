/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import java.io.*;
import java.util.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.util.*;


/**
 *
 * @author stipeja1
 */

public class METPositionTimeName extends PositionTimeName 
{
	protected transient NavyAngle _windBearing;
	protected transient Speed _windSpeed;
	protected transient Temperature _temperature = Temperature.OFFICE_8_274_TEMPERATURE_ON_JANUARY_27_2006_09_24_AM;

	public METPositionTimeName() {}


	public METPositionTimeName(String agentID, NavyAngle windBearing, Speed windSpeed, AbsolutePosition pos, Date time)
	{
		super(pos, time, agentID);
		_windBearing = windBearing;
		_windSpeed = windSpeed;
	}

	public METPositionTimeName(String agentID, NavyAngle windBearing, Speed windSpeed, Temperature temp, AbsolutePosition pos, Date time)
	{
		super(pos, time, agentID);
		_windBearing = windBearing;
		_windSpeed = windSpeed;
		_temperature = temp;
	}

	public String toString() {
		return new String(name + " wind speed: " + _windSpeed + " wind bearing: " + _windBearing + " temp: " + _temperature);
	}

	public Speed getWindSpeed() { return _windSpeed; }
	public NavyAngle getWindBearing() { return _windBearing; }
	public Temperature getTemperature() { return _temperature; }


	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);

		byte[] bytes = new byte[4 + 4 + 4];

		int index = 0;
		index = ByteManipulator.addFloat(bytes,
				(float)_windBearing.getDoubleValue(Angle.DEGREES), index, false);
		index = ByteManipulator.addFloat(bytes,
				(float)_windSpeed.getDoubleValue(Speed.METERS_PER_SECOND), index, false);
		index = ByteManipulator.addFloat(bytes,
				(float)_temperature.getDoubleValue(Temperature.DEG_F), index, false);
		out.write(bytes);
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
			super.readExternal(in);

			byte[] bytes  = new byte[4 + 4 + 4];
			in.readFully(bytes);
			int index = 0;
			_windBearing = new NavyAngle(ByteManipulator.getFloat(bytes, index, false), Angle.DEGREES);
			index += 4;
			_windSpeed = new Speed(ByteManipulator.getFloat(bytes, index, false), Speed.METERS_PER_SECOND);
			index += 4;
			_temperature = new Temperature(ByteManipulator.getFloat(bytes, index, false), Temperature.DEG_F);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
