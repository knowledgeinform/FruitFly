/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import edu.jhuapl.nstd.tase.TASE_Telemetry;
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
public class TASETelemetryBelief extends Belief implements BeliefExternalizable
{


        public static final String BELIEF_NAME = "TASETelemetryBelief";

    TASE_Telemetry _telemetry;

    public TASETelemetryBelief()
    {
        super();
    }

    public TASETelemetryBelief(String agentID,TASE_Telemetry t)
    {
        this (agentID, t, new Date (System.currentTimeMillis()));
    }
    
    public TASETelemetryBelief(String agentID,TASE_Telemetry t, Date time)
    {
        super(agentID);
        timestamp = time;
        _telemetry = t;
    }

    public void setTASETelemetry(TASE_Telemetry t)
    {
        _telemetry = t;
    }

    public TASE_Telemetry getTASETelemetry()
    {
        return _telemetry;
    }




    @Override
    protected void addBelief(Belief b)
    {
         TASETelemetryBelief belief = (TASETelemetryBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._telemetry = belief.getTASETelemetry();
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

		byte[] bytes = new byte[60];

		int index = 0;
		index = ByteManipulator.addDouble(bytes,_telemetry.AltEllip,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.CameraPitch,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.CameraRoll,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.CameraYaw,index,false);
        index = ByteManipulator.addInt(bytes,_telemetry.GPS_Status,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.Lat,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.Lon,index,false);
        index = ByteManipulator.addDouble(bytes,_telemetry.PDOP,index,false);
		out.write(bytes);
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
            _telemetry = new TASE_Telemetry();
			super.readExternal(in);

			byte[] bytes  = new byte[60];
			in.readFully(bytes);
			int index = 0;
			_telemetry.AltEllip = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.CameraPitch = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.CameraRoll = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.CameraYaw = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.GPS_Status = ByteManipulator.getInt(bytes, index, false);
			index += 4;
            _telemetry.Lat = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.Lon = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
            _telemetry.PDOP = ByteManipulator.getDouble(bytes, index, false);
			index += 8;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
