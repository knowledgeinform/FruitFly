/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import edu.jhuapl.nstd.tase.TASE_PointingAngles;
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
 * @author humphjc1
 */
public class TASEPointingAnglesBelief extends Belief implements BeliefExternalizable
{
    public static final String BELIEF_NAME = "TASEPointingAnglesBelief";

    TASE_PointingAngles _pointAngles;

    public TASEPointingAnglesBelief()
    {
        super();
    }

    public TASEPointingAnglesBelief(String agentID,TASE_PointingAngles t)
    {
        this (agentID, t, new Date (System.currentTimeMillis()));
    }
    
    public TASEPointingAnglesBelief(String agentID,TASE_PointingAngles t, Date time)
    {
        super(agentID);
        timestamp = time;
        _pointAngles = t;
    }

    public void setTASEPointingAngles(TASE_PointingAngles t)
    {
        _pointAngles = t;
    }

    public TASE_PointingAngles getTASEPointingAngles()
    {
        return _pointAngles;
    }




    @Override
    protected void addBelief(Belief b)
    {
         TASEPointingAnglesBelief belief = (TASEPointingAnglesBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._pointAngles = belief.getTASEPointingAngles();
        }
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
    @Override
  public String getName()
  {
    return BELIEF_NAME;
  }


    @Override
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

    @Override
	public void writeExternal(DataOutput out) throws IOException {

		super.writeExternal(out);

		byte[] bytes = new byte[60];

		int index = 0;
		index = ByteManipulator.addDouble(bytes,_pointAngles.Pan,index,false);
                index = ByteManipulator.addDouble(bytes,_pointAngles.Tilt,index,false);	
                out.write(bytes);
	}

    @Override
	public void readExternal(DataInput in) throws IOException {
		try
		{
            _pointAngles = new TASE_PointingAngles();
			super.readExternal(in);

			byte[] bytes  = new byte[60];
			in.readFully(bytes);
			int index = 0;
			_pointAngles.Pan = ByteManipulator.getDouble(bytes, index, false);
			index += 8;
                        _pointAngles.Tilt = ByteManipulator.getDouble(bytes, index, false);
			index += 8;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
