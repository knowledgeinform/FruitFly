/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author humphjc1
 */
public class GammaEthernetCountRates extends GammaCountRates
{
    private final int LOG_STATISTICS_VERSION = 0;
    
    int m_RealTicks;
    int m_NumEvents;
    int m_NumTriggers;
    int m_DeadTicks;
    float m_EventRate;
    float m_TriggerRate;
    float m_DeadTimeFraction;
    float m_InputRate;
    

    public GammaEthernetCountRates() 
    {

    }

    public GammaEthernetCountRates(GammaEthernetCountRates oldCopy) 
    {
        this.m_RealTicks = oldCopy.m_RealTicks;
        this.m_NumEvents = oldCopy.m_NumEvents;
        this.m_NumTriggers = oldCopy.m_NumTriggers;
        this.m_DeadTicks = oldCopy.m_DeadTicks;
        setRealTime(oldCopy.getRealTime());
        this.m_EventRate = oldCopy.m_EventRate;
        this.m_TriggerRate = oldCopy.m_TriggerRate;
        this.m_DeadTimeFraction = oldCopy.m_DeadTimeFraction;
        this.m_InputRate = oldCopy.m_InputRate;
    }

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

        ByteBuffer retBuf = ByteBuffer.allocate(10*4);
        retBuf.order(ByteOrder.BIG_ENDIAN);

        retBuf.putInt(LOG_STATISTICS_VERSION);
        retBuf.putInt(m_RealTicks);
        retBuf.putInt(m_NumEvents);
        retBuf.putInt(m_NumTriggers);
        retBuf.putInt(m_DeadTicks);
        retBuf.putFloat(getRealTime());
        retBuf.putFloat(m_EventRate);
        retBuf.putFloat(m_TriggerRate);
        retBuf.putFloat(m_DeadTimeFraction);
        retBuf.putFloat(m_InputRate);            

        return retBuf.array();
    }
    
    public String toLogString ()
    {
        String retMsg = "";

        retMsg += m_RealTicks + ",";
        retMsg += m_NumEvents + ",";
        retMsg += m_NumTriggers + ",";
        retMsg += m_DeadTicks + ",";
        retMsg += getRealTime() + ",";
        retMsg += m_EventRate + ",";
        retMsg += m_TriggerRate + ",";
        retMsg += m_DeadTimeFraction + ",";
        retMsg += m_InputRate + ",";

        return retMsg;
    }

    public void writeToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_STATISTICS_VERSION);
        output.writeInt(m_RealTicks);
        output.writeInt(m_NumEvents);
        output.writeInt(m_NumTriggers);
        output.writeInt(m_DeadTicks);
        output.writeFloat(getRealTime());
        output.writeFloat(m_EventRate);
        output.writeFloat(m_TriggerRate);
        output.writeFloat(m_DeadTimeFraction);
        output.writeFloat(m_InputRate);
    }

    public void readFromStream(DataInputStream input) throws IOException 
    {
        int version = input.readInt();
        m_RealTicks = input.readInt();
        m_NumEvents = input.readInt();
        m_NumTriggers = input.readInt();
        m_DeadTicks = input.readInt();
        setRealTime(input.readFloat());
        m_EventRate = input.readFloat();
        m_TriggerRate = input.readFloat();
        m_DeadTimeFraction = input.readFloat();
        m_InputRate = input.readFloat();
    }

    public int getRealTicks()
    {
        return m_RealTicks;
    }

    public int getNumEvents()
    {
        return m_NumEvents;
    }

    public int getNumTriggers()
    {
        return m_NumTriggers;
    }

    public int getDeadTicks()
    {
        return m_DeadTicks;
    }

    public float getEventRate()
    {
        return m_EventRate;
    }

    public float getTriggerRate()
    {
        return m_TriggerRate;
    }

    public float getDeadTimeFraction()
    {
        return m_DeadTimeFraction;
    }

    public float getInputRate()
    {
        return m_InputRate;
    }
    
    public void setRealTicks(int newVal)
    {
        m_RealTicks = newVal;
    }

    public void setNumEvents(int newVal)
    {
        m_NumEvents = newVal;
    }

    public void setNumTriggers(int newVal)
    {
        m_NumTriggers = newVal;
    }

    public void setDeadTicks(int newVal)
    {
        m_DeadTicks = newVal;
    }

    public void setEventRate(float newVal)
    {
        m_EventRate = newVal;
    }

    public void setTriggerRate(float newVal)
    {
        m_TriggerRate = newVal;
    }

    public void setDeadTimeFraction(float newVal)
    {
        m_DeadTimeFraction = newVal;
    }

    public void setInputRate(float newVal)
    {
        m_InputRate = newVal;
    }

    public void writeToFlatFile(BufferedWriter writer) throws IOException 
    {
        String msg = "" + m_RealTicks + "\t"
                        + m_NumEvents + "\t"
                        + m_NumTriggers + "\t"
                        + m_DeadTicks + "\t"
                        + getRealTime() + "\t"
                        + m_EventRate + "\t"
                        + m_TriggerRate + "\t"
                        + m_DeadTimeFraction + "\t"
                        + m_InputRate + "\t";

        writer.write(msg);
    }

    public static void writeFlatFileHeader(BufferedWriter writer) throws IOException 
    {
        String msg = "RealTicks\t"
                        + "NumEvents\t"
                        + "NumTriggers\t"
                        + "DeadTicks\t"
                        + "RealTime\t"
                        + "EventRate\t"
                        + "TriggerRate\t"
                        + "DeadTimeFraction\t"
                        + "InputRate\t";

        writer.write(msg);
    }
}
