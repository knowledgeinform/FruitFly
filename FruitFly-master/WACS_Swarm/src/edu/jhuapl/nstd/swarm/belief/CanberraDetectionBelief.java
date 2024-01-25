package edu.jhuapl.nstd.swarm.belief;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


/**
 * Holds detection data from the Canberra gamma ray detector.
 *
 * @author fishmsm1
 */
public class CanberraDetectionBelief extends Belief
{

    public static final String BELIEF_NAME = "CanberraDetectionBelief";
    /**
     * Detection message count.
     */
    private int count;
    /**
     * Filtered dose rate in uG/h.
     */
    private double filteredDoseRate;
    /**
     * Unfiltered dose rate in uG/h.
     */
    private double unfilteredDoseRate;
    /**
     * Mission accumulated dose in uG.
     */
    private double missionAccumulatedDose;
    /**
     * Peak rate in uG/h.
     */
    private double peakRate;
    /**
     * Temperature in Celsius.
     */
    private double temperature;
    /**
     * Number of hrs, mins, secs after the device was powered on.
     */
    private String relativeTime;

    public CanberraDetectionBelief()
    {
        this(System.currentTimeMillis());
    }
    
    public CanberraDetectionBelief(long time)
    {
        this.timestamp = new Date(time);
    }
    
    @Override
    protected void addBelief(Belief belief)
    {
        CanberraDetectionBelief cdb = (CanberraDetectionBelief) belief;
        
        if (timestamp.before(cdb.timestamp))
        {
            this.timestamp = cdb.getTimeStamp();
            count = cdb.getCount();
            filteredDoseRate = cdb.getFilteredDoseRate();
            unfilteredDoseRate = cdb.getUnfilteredDoseRate();
            missionAccumulatedDose = cdb.getMissionAccumulatedDose();
            peakRate = cdb.getPeakRate();
            temperature = cdb.getTemperature();
            relativeTime = cdb.getRelativeTime();
        }
    }

    public void update()
    {
        try
        {
            super.update();
        }
        catch (Exception e)
        {
            System.err.println("Exception in update thread - caught and ignored");
            e.printStackTrace();
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
    
    public static Belief deserialize(InputStream iStream, Class cls) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        CanberraDetectionBelief belief = null;

        try
        {
            belief = (CanberraDetectionBelief) cls.newInstance();
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
        
        out.writeInt(count);
        out.writeDouble(filteredDoseRate);
        out.writeDouble(unfilteredDoseRate);
        out.writeDouble(missionAccumulatedDose);
        out.writeDouble(peakRate);
        out.writeDouble(temperature);
        out.writeChars(relativeTime);
    }

    @Override
    public synchronized void readExternal(DataInput in) throws IOException
    {
        super.readExternal(in);

        count = in.readInt();
        filteredDoseRate = in.readDouble();
        unfilteredDoseRate = in.readDouble();
        missionAccumulatedDose = in.readDouble();
        peakRate = in.readDouble();
        temperature = in.readDouble();
        relativeTime = in.readLine();
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * @return the count
     */
    public int getCount()
    {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count)
    {
        this.count = count;
    }

    /**
     * @return the filteredDoseRate
     */
    public double getFilteredDoseRate()
    {
        return filteredDoseRate;
    }

    /**
     * @param filteredDoseRate the filteredDoseRate to set
     */
    public void setFilteredDoseRate(double filteredDoseRate)
    {
        this.filteredDoseRate = filteredDoseRate;
    }

    /**
     * @return the unfilteredDoseRate
     */
    public double getUnfilteredDoseRate()
    {
        return unfilteredDoseRate;
    }

    /**
     * @param unfilteredDoseRate the unfilteredDoseRate to set
     */
    public void setUnfilteredDoseRate(double unfilteredDoseRate)
    {
        this.unfilteredDoseRate = unfilteredDoseRate;
    }

    /**
     * @return the missionAccumulatedDose
     */
    public double getMissionAccumulatedDose()
    {
        return missionAccumulatedDose;
    }

    /**
     * @param missionAccumulatedDose the missionAccumulatedDose to set
     */
    public void setMissionAccumulatedDose(double missionAccumulatedDose)
    {
        this.missionAccumulatedDose = missionAccumulatedDose;
    }

    /**
     * @return the peakRate
     */
    public double getPeakRate()
    {
        return peakRate;
    }

    /**
     * @param peakRate the peakRate to set
     */
    public void setPeakRate(double peakRate)
    {
        this.peakRate = peakRate;
    }

    /**
     * @return the temperature
     */
    public double getTemperature()
    {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }

    /**
     * @return the relativeTime
     */
    public String getRelativeTime()
    {
        return relativeTime;
    }

    /**
     * @param relativeTime the relativeTime to set
     */
    public void setRelativeTime(String relativeTime)
    {
        this.relativeTime = relativeTime;
    }
}
