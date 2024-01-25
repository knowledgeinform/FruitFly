/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.belief;

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
public class GammaDetectionBelief extends Belief
{
    public static final String BELIEF_NAME = "GammaDetectionBelief";

    String _gammaDetections;

    public GammaDetectionBelief()
    {        
    }

    public GammaDetectionBelief(String agentID, String detection)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _gammaDetections = detection;
    }

    public void setGammaDetections( String detection)
    {
        _gammaDetections = detection;
    }

    public String getGammaDetections()
    {
        return _gammaDetections;
    }

    @Override
    protected void addBelief(Belief b)
    {
         GammaDetectionBelief belief = (GammaDetectionBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._gammaDetections = belief.getGammaDetections();
        }
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

        GammaDetectionBelief belief = null;

        try
        {
            belief = (GammaDetectionBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    @Override
    public synchronized void writeExternal(DataOutput out) throws IOException
    {
        super.writeExternal(out);

        out.writeInt(_gammaDetections.length());
        for(int i = 0; i < _gammaDetections.length(); ++i)
        {
           out.writeByte((byte)_gammaDetections.charAt(i));
        }
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        int stringLength = in.readInt();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringLength; ++i)
        {
            stringBuilder.append((char)in.readByte());
        }

        _gammaDetections = stringBuilder.toString();
    }

      /**
   * Returns the unique name for this belief type.
   * @return A unique name for this belief type.
   */
  public String getName()
  {
    return BELIEF_NAME;
  }


}
