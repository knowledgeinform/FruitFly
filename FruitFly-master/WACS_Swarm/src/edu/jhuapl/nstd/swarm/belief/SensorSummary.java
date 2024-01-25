/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author humphjc1
 */
public class SensorSummary 
{
    public long m_CurrDetectionTimeMs;
    public float m_CurrDetectionValue;
    public String m_CurrDetectionString;
    public float m_MaxDetectionValue;
    public long m_MaxDetectionValueTimeMs;
    public String m_MaxDetectionString;
    
    public float m_GreenLightMinValue;
    public float m_YellowLightMinValue;
    public float m_RedLightMinValue;
    public float m_RedLightMaxValue;
    public long m_LastAboveGreenDetectionTimeMs;
    public boolean m_InBackgroundCollection;

    public SensorSummary ()
    {
        m_CurrDetectionTimeMs = 0;
    }

    public SensorSummary (SensorSummary copyFrom)
    {
        m_CurrDetectionTimeMs = copyFrom.m_CurrDetectionTimeMs;
        m_CurrDetectionValue = copyFrom.m_CurrDetectionValue;
        m_CurrDetectionString = copyFrom.m_CurrDetectionString;
        m_MaxDetectionValue = copyFrom.m_MaxDetectionValue;
        m_MaxDetectionString = copyFrom.m_MaxDetectionString;
        m_MaxDetectionValueTimeMs = copyFrom.m_MaxDetectionValueTimeMs;

        m_GreenLightMinValue = copyFrom.m_GreenLightMinValue;
        m_YellowLightMinValue = copyFrom.m_YellowLightMinValue;
        m_RedLightMinValue = copyFrom.m_RedLightMinValue;
        m_RedLightMaxValue = copyFrom.m_RedLightMaxValue;
        m_LastAboveGreenDetectionTimeMs = copyFrom.m_LastAboveGreenDetectionTimeMs;
        m_InBackgroundCollection = copyFrom.m_InBackgroundCollection;
    }

    public SensorSummary (long currDetectionTimeMs, float currDetectionValue, String currDetectionString, float maxDetectionValue, String maxDetectionString, long maxDetectionValueTimeMs,
                            float greenLightMinValue, float yellowLightMinValue, float redLightMinValue, float redLightMaxValue, long lastAboveGreenDetectionTimeMs, boolean inBackgroundCollection)
    {
         m_CurrDetectionTimeMs = currDetectionTimeMs;
         m_CurrDetectionValue = currDetectionValue;
         m_CurrDetectionString = currDetectionString;
         m_MaxDetectionValue = maxDetectionValue;
         m_MaxDetectionString = maxDetectionString;
         m_MaxDetectionValueTimeMs = maxDetectionValueTimeMs;

         m_GreenLightMinValue = greenLightMinValue;
         m_YellowLightMinValue = yellowLightMinValue;
         m_RedLightMinValue = redLightMinValue;
         m_RedLightMaxValue = redLightMaxValue;
         m_LastAboveGreenDetectionTimeMs = lastAboveGreenDetectionTimeMs;
         m_InBackgroundCollection = inBackgroundCollection;
    }

    public void writeExternal(DataOutput out) throws IOException
    {
        out.writeLong(m_CurrDetectionTimeMs);
        out.writeFloat(m_CurrDetectionValue);
        if (m_CurrDetectionString != null)
        {
            out.writeBoolean(true);
            out.writeUTF(m_CurrDetectionString);
        }
        else
        {
            out.writeBoolean(false);
        }
        out.writeFloat(m_MaxDetectionValue);
        if (m_MaxDetectionString != null)
        {
            out.writeBoolean(true);
            out.writeUTF(m_MaxDetectionString);
        }
        else
        {
            out.writeBoolean(false);
        }
        out.writeLong(m_MaxDetectionValueTimeMs);

        out.writeFloat(m_GreenLightMinValue);
        out.writeFloat(m_YellowLightMinValue);
        out.writeFloat(m_RedLightMinValue);
        out.writeFloat(m_RedLightMaxValue);
        out.writeLong(m_LastAboveGreenDetectionTimeMs);
        out.writeBoolean(m_InBackgroundCollection);
    }

    public void readExternal(DataInput in) throws IOException
    {
        m_CurrDetectionTimeMs = in.readLong();
        m_CurrDetectionValue = in.readFloat();
        boolean stringPresent = in.readBoolean();
        if (stringPresent)
            m_CurrDetectionString = in.readUTF();
        else
            m_CurrDetectionString = null;
        m_MaxDetectionValue = in.readFloat();
        stringPresent = in.readBoolean();
        if (stringPresent)
            m_MaxDetectionString = in.readUTF();
        else
            m_MaxDetectionString = null;
        m_MaxDetectionValueTimeMs = in.readLong();

        m_GreenLightMinValue = in.readFloat();
        m_YellowLightMinValue = in.readFloat();
        m_RedLightMinValue = in.readFloat();
        m_RedLightMaxValue = in.readFloat();
        m_LastAboveGreenDetectionTimeMs = in.readLong();
        m_InBackgroundCollection = in.readBoolean();
    }
}
