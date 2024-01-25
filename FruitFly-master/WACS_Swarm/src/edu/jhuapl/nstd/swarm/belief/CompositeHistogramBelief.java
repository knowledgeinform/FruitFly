
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
public class CompositeHistogramBelief extends Belief implements BeliefExternalizable
{

    public static final String BELIEF_NAME = "CompositeHistogramBelief";
    private int[] m_HistogramData;
    private long m_TotalCounts;
    private double m_LiveTime;
    private boolean background = false;

    public CompositeHistogramBelief()
    {
        timestamp = new Date();
    }

    public CompositeHistogramBelief(String agentID, int[] data)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        m_HistogramData = data.clone();
        m_TotalCounts = 0;
        m_LiveTime = 0;
    }

    public CompositeHistogramBelief(String agentID, int[] data, long count, double time)
    {
        super(agentID);
        timestamp = new Date(System.currentTimeMillis());
        m_HistogramData = data.clone();
        m_TotalCounts = count;
        m_LiveTime = time;
    }

    @Override
    protected void addBelief(Belief b)
    {
        CompositeHistogramBelief belief = (CompositeHistogramBelief) b;
        //System.err.println("Adding");
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            setIsBackground(belief.isBackground());
            setHistogramData(belief.getHistogramData().clone());
            setLiveTime(belief.getLiveTime());
            setSpectraCount(belief.getSpectraCount());
        }
    }

    /**
     * Returns the unique name for this belief type.
     *
     * @return A unique name for this belief type.
     */
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * @return the m_HistogramData
     */
    public int[] getHistogramData()
    {
        return m_HistogramData;
    }

    /**
     * @param m_HistogramData the m_HistogramData to set
     */
    public void setHistogramData(int[] m_HistogramData)
    {
        this.m_HistogramData = m_HistogramData;
    }

    /**
     * @return the m_TotalCounts
     */
    public long getSpectraCount()
    {
        return m_TotalCounts;
    }

    /**
     * @param m_TotalCounts the m_TotalCounts to set
     */
    public void setSpectraCount(long m_SpectraCount)
    {
        this.m_TotalCounts = m_SpectraCount;
    }

    /**
     * @return the m_LiveTime
     */
    public double getLiveTime()
    {
        return m_LiveTime;
    }

    /**
     * @param m_LiveTime the m_LiveTime to set
     */
    public void setLiveTime(double m_LiveTime)
    {
        this.m_LiveTime = m_LiveTime;
    }
    
    public void setTimeStamp(Date time)
    {
        timestamp = time;
    }
    
    public void setIsBackground(boolean isBackground)
    {
        background = isBackground;
    }

    public boolean isBackground()
    {
        return background;
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

        CompositeHistogramBelief belief = null;

        try
        {
            belief = (CompositeHistogramBelief) clas.newInstance();
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

        byte[] bytes = new byte[21 + m_HistogramData.length * 4];
        int index = 0;
        index = ByteManipulator.addLong(bytes, m_TotalCounts, index, false);
        index = ByteManipulator.addDouble(bytes, m_LiveTime, index, false);
        index = ByteManipulator.addBoolean(bytes, background, index, false);
        index = ByteManipulator.addInt(bytes, m_HistogramData.length, index, false);
        for (int i = 0; i < m_HistogramData.length; i++)
        {
            index = ByteManipulator.addInt(bytes, m_HistogramData[i], index, false);
        }
        out.write(bytes);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        byte[] bytes = new byte[21];
        in.readFully(bytes);

        int index = 0;
        m_TotalCounts = ByteManipulator.getLong(bytes, index, false);
        index += 8;
        m_LiveTime = ByteManipulator.getDouble(bytes, index, false);
        index += 8;
        background = ByteManipulator.getBoolean(bytes, index, false);
        index += 1;
        int len = ByteManipulator.getInt(bytes, index, false);
        index = 0;
        byte[] array = new byte[len * 4];
        m_HistogramData = new int[len];
        in.readFully(array);
        for (int i = 0; i < m_HistogramData.length; i++)
        {
            m_HistogramData[i] = ByteManipulator.getInt(array, index, false);
            index += 4;
        }

    }
}