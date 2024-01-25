/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
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
 * @author stipeja1
 */



public class WACSWaypointBaseBelief extends Belief implements BeliefExternalizable
{
    public static final String BELIEF_NAME = "WACSWaypointBaseBelief";
    private Altitude _finalloiteralt;
    private Altitude _standoffloiteralt;
    private Length _loiterrad;
    private Altitude _intersectalt;
    private Length _intersectrad;

    public WACSWaypointBaseBelief()
    {
        super();
    }

    public WACSWaypointBaseBelief(String agentID,  Altitude intersectalt, Length intersectrad, Altitude finalloiteralt, Altitude standoffloiteralt, Length loiterrad)
    {
        this (agentID, intersectalt, intersectrad, finalloiteralt, standoffloiteralt, loiterrad, new Date (System.currentTimeMillis()));
    }
            
    public WACSWaypointBaseBelief(String agentID,  Altitude intersectalt, Length intersectrad, Altitude finalloiteralt, Altitude standoffloiteralt, Length loiterrad, Date time)
    {
        super(agentID);
        timestamp = time;
       _standoffloiteralt = standoffloiteralt;
       _finalloiteralt = finalloiteralt;
       _loiterrad = loiterrad;
       _intersectalt = intersectalt;
       _intersectrad = intersectrad;
    }

    @Override
    protected void addBelief(Belief b)
    {
         WACSWaypointBaseBelief belief = (WACSWaypointBaseBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
           this.timestamp = belief.getTimeStamp();
            setFinalLoiterAltitude((Altitude) belief.getFinalLoiterAltitude());
            setStandoffLoiterAltitude((Altitude) belief.getStandoffLoiterAltitude());
            setLoiterRadius((Length) belief.getLoiterRadius());
            setIntersectAltitude((Altitude) belief.getIntersectAltitude());
            setIntersectrad((Length) belief.getIntersectRadius());
        }
    }
    
    @Override
    public boolean equals (Object obj)
    {
        if (obj == null || !(obj instanceof WACSWaypointBaseBelief))
        {
            return false;
        }

        return (Math.abs(_finalloiteralt.getDoubleValue(Length.METERS) - (((WACSWaypointBaseBelief)obj)._finalloiteralt.getDoubleValue(Length.METERS))) < 0.5 && 
                Math.abs(_standoffloiteralt.getDoubleValue(Length.METERS) - (((WACSWaypointBaseBelief)obj)._standoffloiteralt.getDoubleValue(Length.METERS))) < 0.5 && 
                Math.abs(_loiterrad.getDoubleValue(Length.METERS) - (((WACSWaypointBaseBelief)obj)._loiterrad.getDoubleValue(Length.METERS))) < 0.5 && 
                Math.abs(_intersectalt.getDoubleValue(Length.METERS) - (((WACSWaypointBaseBelief)obj)._intersectalt.getDoubleValue(Length.METERS))) < 0.5 && 
                Math.abs(_intersectrad.getDoubleValue(Length.METERS) - (((WACSWaypointBaseBelief)obj)._intersectrad.getDoubleValue(Length.METERS))) < 0.5 
                );
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

		byte[] bytes = new byte[40];

		int index = 0;
		index = ByteManipulator.addDouble(bytes,getFinalLoiterAltitude().getDoubleValue(Length.METERS),index,false);
                index = ByteManipulator.addDouble(bytes,getStandoffLoiterAltitude().getDoubleValue(Length.METERS),index,false);
                index = ByteManipulator.addDouble(bytes,getLoiterRadius().getDoubleValue(Length.METERS),index,false);
                index = ByteManipulator.addDouble(bytes,getIntersectAltitude().getDoubleValue(Length.METERS),index,false);
                index = ByteManipulator.addDouble(bytes,getIntersectRadius().getDoubleValue(Length.METERS),index,false);
		out.write(bytes);
	}

	public void readExternal(DataInput in) throws IOException {
		try
		{
			super.readExternal(in);

			byte[] bytes  = new byte[40];
			in.readFully(bytes);
			int index = 0;
			setFinalLoiterAltitude(new Altitude(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
			index += 8;
                        setStandoffLoiterAltitude(new Altitude(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
			index += 8;
                        setLoiterRadius(new Length(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
			index += 8;
                        setIntersectAltitude(new Altitude(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
			index += 8;
                        setIntersectrad(new Length(ByteManipulator.getDouble(bytes, index, false),Length.METERS));
			index += 8;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

    /**
     * @return the _loiteralt
     */
    public Altitude getFinalLoiterAltitude() {
        return _finalloiteralt;
    }

    /**
     * @param loiteralt the _loiteralt to set
     */
    public void setFinalLoiterAltitude(Altitude loiteralt) {
        this._finalloiteralt = loiteralt;
    }

    /**
     * @return the _loiteralt
     */
    public Altitude getStandoffLoiterAltitude() {
        return _standoffloiteralt;
    }

    /**
     * @param loiteralt the _loiteralt to set
     */
    public void setStandoffLoiterAltitude(Altitude loiteralt) {
        this._standoffloiteralt = loiteralt;
    }

    /**
     * @return the _loiterrad
     */
    public Length getLoiterRadius() {
        return _loiterrad;
    }

    /**
     * @param loiterrad the _loiterrad to set
     */
    public void setLoiterRadius(Length loiterrad) {
        this._loiterrad = loiterrad;
    }

    /**
     * @return the _intersectalt
     */
    public Altitude getIntersectAltitude() {
        return _intersectalt;
    }

    /**
     * @param intersectalt the _intersectalt to set
     */
    public void setIntersectAltitude(Altitude intersectalt) {
        this._intersectalt = intersectalt;
    }

    /**
     * @return the _intersectrad
     */
    public Length getIntersectRadius() {
        return _intersectrad;
    }

    /**
     * @param intersectrad the _intersectrad to set
     */
    public void setIntersectrad(Length intersectrad) {
        this._intersectrad = intersectrad;
    }
    
    @Override
    public String getName() {
        return BELIEF_NAME;
    }

}
