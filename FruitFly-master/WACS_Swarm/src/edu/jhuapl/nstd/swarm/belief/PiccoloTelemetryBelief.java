/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author st
 */
public class PiccoloTelemetryBelief extends Belief implements BeliefExternalizable
{


        public static final String BELIEF_NAME = "PiccoloTelemetryBelief";

    Pic_Telemetry _telemetry;

    public PiccoloTelemetryBelief()
    {
        super();
    }

    public PiccoloTelemetryBelief(String agentID,Pic_Telemetry t)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _telemetry = t;
    }
    
    public PiccoloTelemetryBelief(String agentID,Pic_Telemetry t, Date time)
    {
        super(agentID);
        timestamp = time;
        _telemetry = t;
    }
    
    public void setPiccoloTelemetry(Pic_Telemetry t)
    {
        _telemetry = t;
    }

    public Pic_Telemetry getPiccoloTelemetry()
    {
        return _telemetry;
    }




    @Override
    protected void addBelief(Belief b)
    {
         PiccoloTelemetryBelief belief = (PiccoloTelemetryBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._telemetry = belief.getPiccoloTelemetry();
        }
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
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

		Belief belief = null;

		try {
			belief = (Belief)clas.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		belief.readExternal(in);

		return belief;
	}

	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);

		byte[] bytes = new byte[152];

		int index = 0;
		index = ByteManipulator.addDouble(bytes,_telemetry.AltWGS84,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.AltMSL,index,false);
                index = ByteManipulator.addInt(bytes,_telemetry.GPS_Status,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.IndAirSpeed_mps,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.Lat,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.Lon,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.PDOP,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.Pitch,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.Roll,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.TrueHeading,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.VelDown,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.VelEast,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.VelNorth,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.WindSouth,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.WindWest,index,false);
                index = ByteManipulator.addDouble(bytes,_telemetry.Yaw,index,false);
                index = ByteManipulator.addDouble(bytes, _telemetry.StaticPressPa, index, false);
                index = ByteManipulator.addDouble(bytes, _telemetry.OutsideAirTempC, index, false);
                index = ByteManipulator.addDouble(bytes, _telemetry.AltLaser_m, index, false);
                index = ByteManipulator.addBoolean(bytes, _telemetry.AltLaserValid, index, false);
		out.write(bytes);
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
                        _telemetry = new Pic_Telemetry();
			super.readExternal(in);

			byte[] bytes  = new byte[152];
			in.readFully(bytes);
			int index = 0;
			_telemetry.AltWGS84 = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.AltMSL = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.GPS_Status = ByteManipulator.getInt(bytes, index, false);
                                    index += 4;
                        _telemetry.IndAirSpeed_mps = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.Lat = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.Lon = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.PDOP = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.Pitch = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.Roll = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.TrueHeading = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.VelDown = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.VelEast = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.VelNorth = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.WindSouth = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.WindWest = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.Yaw = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.StaticPressPa = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.OutsideAirTempC = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.AltLaser_m = ByteManipulator.getDouble(bytes, index, false);
                                    index += 8;
                        _telemetry.AltLaserValid = ByteManipulator.getBoolean(bytes, index, false);
                 

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
