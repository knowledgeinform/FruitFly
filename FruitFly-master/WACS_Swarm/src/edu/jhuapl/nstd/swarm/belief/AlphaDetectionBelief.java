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
public class AlphaDetectionBelief extends Belief
{
public static final String BELIEF_NAME = "AlphaDetectionBelief";

    String _alphaDetections;

    public AlphaDetectionBelief()
    {
    }

    public AlphaDetectionBelief(String agentID, String detection)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        _alphaDetections = detection;
    }

    public void setAlphaDetections( String detection)
    {
        _alphaDetections = detection;
    }

    public String getAlphaDetections()
    {
        return _alphaDetections;
    }

    @Override
    protected void addBelief(Belief b)
    {
         AlphaDetectionBelief belief = (AlphaDetectionBelief)b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp)>0 )
        {
          this.timestamp = belief.getTimeStamp();
          this._alphaDetections = belief.getAlphaDetections();
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

        AlphaDetectionBelief belief = null;

        try
        {
            belief = (AlphaDetectionBelief) clas.newInstance();
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

        out.writeInt(_alphaDetections.length());
        for(int i = 0; i < _alphaDetections.length(); ++i)
        {
           out.writeByte((byte)_alphaDetections.charAt(i));
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

        _alphaDetections = stringBuilder.toString();
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
