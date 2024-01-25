package edu.jhuapl.nstd.swarm.belief;

import java.util.logging.*;


import java.io.*;
import java.util.Date;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.belief.TimeName;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;

public class SensorTimeName extends PositionTimeName  implements BeliefExternalizable {
	//Position of sensor
	private AbsolutePosition m_Location;
	//Alarms detected by RDR
	private boolean m_RDRAlarmed;
	//Message header for belief data
	private byte[] m_Header;
	//Belief data bytes
	private byte[] m_BeliefData;
	
	public boolean getRDRAlarmed()
	{
		return m_RDRAlarmed;
	}
	public byte[] getHeader()
	{
		return m_Header;
	}
	public byte[] getBeliefData()
	{
		return m_BeliefData;
	}
	//Empty constructor
	public SensorTimeName() {}
	//Full constructor
	public SensorTimeName(AbsolutePosition Location, boolean RDRAlarmed, byte[] header, byte[] beliefData, Date time, String name)
	{
		super(Location, time, name);
		m_Location = Location;
		m_RDRAlarmed = RDRAlarmed;
		m_Header = header;
		m_BeliefData = beliefData;
	}
	public String toString()
	{
		return new String(m_Location + ", " + "RDR: " + m_RDRAlarmed + ", Length: " + m_BeliefData.length);
	}

	public String toMetricString() {
		return new String(m_Location.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) + " " +
				m_Location.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) + " " +
				m_RDRAlarmed);
	}

	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);

		LatLonAltPosition lla = m_Location.asLatLonAltPosition();
		float _lat = (float)lla.getLatitude().getDoubleValue(Angle.DEGREES);
		float _lon = (float)lla.getLongitude().getDoubleValue(Angle.DEGREES);
		float _alt = (float)lla.getAltitude().getDoubleValue(Length.METERS);
		//First 4 bytes (int) - length of belief data
		//Lat, Lon, Alt and RDR - 49 bytes
		//Header - 50 bytes
		//Then belief data
		byte[] bytes = new byte[4 + 49 + 50 + m_BeliefData.length];

		int index = 0;
		index = ByteManipulator.addInt(bytes, m_BeliefData.length, index, false);
		index = ByteManipulator.addFloat(bytes, _lat, index, false);
		index = ByteManipulator.addFloat(bytes, _lon, index, false);
		index = ByteManipulator.addFloat(bytes, _alt, index, false);
		if(m_RDRAlarmed)
			index = ByteManipulator.addByte(bytes, 1, index, false);
		else
			index = ByteManipulator.addByte(bytes, 0, index, false);
		for(int i = 0; i < m_Header.length; i++)
		{
			index = ByteManipulator.addByte(bytes, m_Header[i], index, false);
		}
		for(int i = 0; i < m_BeliefData.length; i++)
		{
			index = ByteManipulator.addByte(bytes, m_BeliefData[i], index, false);
		}
		out.write(bytes);	
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
			super.readExternal(in);
	
			//First 4 bytes (int) - length of belief data
			//Lat, Lon, Alt and RDR - 49 bytes
			//Header - 50 bytes
			//Then belief data
			int beliefDataLength = in.readInt();
			byte[] bytes  = new byte[49 + 50 + beliefDataLength];
			in.readFully(bytes);
			int index = 0;
			float _lat = ByteManipulator.getFloat(bytes, index, false);
			index += 4;
			float _lon = ByteManipulator.getFloat(bytes, index, false);
			index += 4;
			float _alt = ByteManipulator.getFloat(bytes, index, false);
			index += 4;
	
			m_Location = new LatLonAltPosition(new Latitude(_lat, Angle.DEGREES),
					new Longitude(_lon, Angle.DEGREES),
					new Altitude(_alt, Length.METERS));
			byte _rdr = ByteManipulator.getByte(bytes, index, false);
			index++;
			if(_rdr == 1)
				m_RDRAlarmed = true;
			else
				m_RDRAlarmed = false;
			m_Header = new byte[50];
			for(int i = 0; i < m_Header.length; i++)
			{
				m_Header[i] = ByteManipulator.getByte(bytes, index, false);
				index++;
			}
			m_BeliefData = new byte[beliefDataLength];
			for(int i = 0; i < m_BeliefData.length; i++)
			{
				m_BeliefData[i] = ByteManipulator.getByte(bytes, index, false);
				index++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
