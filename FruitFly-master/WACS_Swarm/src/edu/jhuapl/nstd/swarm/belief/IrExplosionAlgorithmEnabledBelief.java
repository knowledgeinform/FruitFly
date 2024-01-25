/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
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
public class IrExplosionAlgorithmEnabledBelief extends Belief
{
    public static final String BELIEF_NAME = "IrExplosionAlgorithmEnabledBelief";
    
    protected boolean _enabled;
    protected long _timeUntilExplosionMs;

    public IrExplosionAlgorithmEnabledBelief ()
    {
        super("unspecified");
        timestamp = null;
    }
        
    public IrExplosionAlgorithmEnabledBelief(String agentID)
    {
        super (agentID);
        timestamp = new Date(System.currentTimeMillis());
        _enabled = false;
        _timeUntilExplosionMs = Long.MAX_VALUE;
    }

    public IrExplosionAlgorithmEnabledBelief(String agentID, boolean s, long timeUntilExpMs)
    {
        this (agentID, s, timeUntilExpMs, new Date (System.currentTimeMillis()));
    }
            
    public IrExplosionAlgorithmEnabledBelief(String agentID, boolean s, long timeUntilExpMs, Date time)
    {
        super (agentID);
        timestamp = time;
        _enabled = s;
        _timeUntilExplosionMs = timeUntilExpMs;
    }

    public void setEnabled(boolean s)
    {
        _enabled = s;
    }

    public boolean getEnabled()
    {
        return _enabled;
    }
    
    public void setTimeUntilExplosionMs(long timeMs)
    {
        _timeUntilExplosionMs = timeMs;
    }

    public long getTimeUntilExplosionMs()
    {
        return _timeUntilExplosionMs;
    }


    @Override
    protected void addBelief(Belief b)
    {
         IrExplosionAlgorithmEnabledBelief belief = (IrExplosionAlgorithmEnabledBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._enabled = belief.getEnabled();
          this._timeUntilExplosionMs = belief.getTimeUntilExplosionMs();
        }
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeBoolean (_enabled);
        out.writeLong(_timeUntilExplosionMs);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        _enabled = in.readBoolean();
        _timeUntilExplosionMs = in.readLong();
    }

    @Override
    public byte[] serialize() throws IOException
    {

        setTransmissionTime();
        ByteArrayOutputStream baStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baStream);
        writeExternal(out);
        out.flush();
        out.close();
        return baStream.toByteArray();
    }

    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        IrExplosionAlgorithmEnabledBelief belief = null;

        try
        {
            belief = (IrExplosionAlgorithmEnabledBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }


      /**
   * Retuns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }
}
