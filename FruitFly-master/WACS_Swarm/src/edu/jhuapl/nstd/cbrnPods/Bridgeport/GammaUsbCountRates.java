
package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author fishmsm1
 */
public class GammaUsbCountRates extends GammaCountRates
{
    private int LOG_STATISTICS_VERSION = 1;
    
    private double m_CoarseGain;
    private double m_FineGain;
    private double m_CountRate;
    private int m_DeadTime;
    private double m_HighVoltage;
    private int m_LiveTime;
    private int m_SampleTime;
    private int m_TotalCount;
    private double m_Temperature;
    private int m_Timestamp;
    
    public byte[] toLogBytes ()
    {
        /*String retMsg = "";

        retMsg += LOG_STATISTICS_VERSION + ":";
        retMsg += m_RealTicks + ",";
        retMsg += m_NumEvents + ",";
        retMsg += m_NumTriggers + ",";
        retMsg += m_DeadTicks + ",";
        retMsg += m_RealTime + ",";
        retMsg += m_EventRate + ",";
        retMsg += m_TriggerRate + ",";
        retMsg += m_DeadTimeFraction + ",";
        retMsg += m_InputRate + ",";

        return retMsg.getBytes();*/

        ByteBuffer retBuf = ByteBuffer.allocate(7*4 + 5*8);
        retBuf.order(ByteOrder.BIG_ENDIAN);

        retBuf.putInt(LOG_STATISTICS_VERSION);
        retBuf.putDouble(m_CoarseGain);
        retBuf.putDouble(m_FineGain);
        retBuf.putDouble(m_CountRate);
        retBuf.putInt(m_DeadTime);
        retBuf.putDouble(m_HighVoltage);
        retBuf.putInt(m_LiveTime);
        retBuf.putInt(m_SampleTime);
        retBuf.putFloat(getRealTime());
        retBuf.putInt(m_TotalCount);
        retBuf.putDouble(m_Temperature);
        retBuf.putInt(m_Timestamp);

        return retBuf.array();
    }
    
    public String toLogString ()
    {
        String retMsg = "";

        retMsg += m_CoarseGain + ",";
        retMsg += m_FineGain + ",";
        retMsg += m_CountRate + ",";
        retMsg += m_DeadTime + ",";
        retMsg += m_HighVoltage + ",";
        retMsg += m_LiveTime + ",";
        retMsg += m_SampleTime + ",";
        retMsg += getRealTime() + ",";
        retMsg += m_TotalCount + ",";
        retMsg += m_Temperature + ",";
        retMsg += m_Timestamp + ",";

        return retMsg;
    }

    public void writeToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_STATISTICS_VERSION);
        output.writeDouble(m_CoarseGain);
        output.writeDouble(m_FineGain);
        output.writeDouble(m_CountRate);
        output.writeInt(m_DeadTime);
        output.writeDouble(m_HighVoltage);
        output.writeInt(m_LiveTime);
        output.writeInt(m_SampleTime);
        output.writeFloat(getRealTime());
        output.writeInt(m_TotalCount);
        output.writeDouble(m_Temperature);
        output.writeInt(m_Timestamp);
    }

    public void readFromStream(DataInputStream input) throws IOException 
    {
        int version = input.readInt();
        m_CoarseGain = input.readDouble();
        m_FineGain = input.readDouble();
        m_CountRate = input.readDouble();
        m_DeadTime = input.readInt();
        m_HighVoltage = input.readDouble();
        m_LiveTime = input.readInt();
        m_SampleTime = input.readInt();
        setRealTime(input.readFloat());
        m_TotalCount = input.readInt();
        m_Temperature = input.readDouble();
        m_Timestamp = input.readInt();
    }

    /**
     * @return the m_CoarseGain
     */
    public double getCoarseGain() {
        return m_CoarseGain;
    }

    /**
     * @param m_CoarseGain the m_CoarseGain to set
     */
    public void setCoarseGain(double m_CoarseGain) {
        this.m_CoarseGain = m_CoarseGain;
    }

    /**
     * @return the m_FineGain
     */
    public double getFineGain() {
        return m_FineGain;
    }

    /**
     * @param m_FineGain the m_FineGain to set
     */
    public void setFineGain(double m_FineGain) {
        this.m_FineGain = m_FineGain;
    }

    /**
     * @return the m_CountRate
     */
    public double getCountRate() {
        return m_CountRate;
    }

    /**
     * @param m_CountRate the m_CountRate to set
     */
    public void setCountRate(double m_CountRate) {
        this.m_CountRate = m_CountRate;
    }

    /**
     * @return the m_DeadTime
     */
    public int getDeadTime() {
        return m_DeadTime;
    }

    /**
     * @param m_DeadTime the m_DeadTime to set
     */
    public void setDeadTime(int m_DeadTime) {
        this.m_DeadTime = m_DeadTime;
    }

    /**
     * @return the m_HighVoltage
     */
    public double getHighVoltage() {
        return m_HighVoltage;
    }

    /**
     * @param m_HighVoltage the m_HighVoltage to set
     */
    public void setHighVoltage(double m_HighVoltage) {
        this.m_HighVoltage = m_HighVoltage;
    }

    /**
     * @return the m_LiveTime
     */
    public int getLiveTime() {
        return m_LiveTime;
    }

    /**
     * @param m_LiveTime the m_LiveTime to set
     */
    public void setLiveTime(int m_LiveTime) {
        this.m_LiveTime = m_LiveTime;
    }

    /**
     * @return the m_SampleTime
     */
    public int getSampleTime() {
        return m_SampleTime;
    }

    /**
     * @param m_SampleTime the m_SampleTime to set
     */
    public void setSampleTime(int m_SampleTime) {
        this.m_SampleTime = m_SampleTime;
    }

    /**
     * @return the m_TotalCount
     */
    public int getTotalCount() {
        return m_TotalCount;
    }

    /**
     * @param m_TotalCount the m_TotalCount to set
     */
    public void setTotalCount(int m_TotalCount) {
        this.m_TotalCount = m_TotalCount;
    }

    /**
     * @return the m_Temperature
     */
    public double getTemperature() {
        return m_Temperature;
    }

    /**
     * @param m_Temperature the m_Temperature to set
     */
    public void setTemperature(double m_Temperature) {
        this.m_Temperature = m_Temperature;
    }

    /**
     * @return the m_Timestamp
     */
    public int getTimestamp() {
        return m_Timestamp;
    }

    /**
     * @param m_Timestamp the m_Timestamp to set
     */
    public void setTimestamp(int m_Timestamp) {
        this.m_Timestamp = m_Timestamp;
    }

}
