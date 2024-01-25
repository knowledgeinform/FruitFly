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
public class GammaCalibration {

    private final int LOG_CALIBRATION_VERSION = 0;

    private double m_ADC_Speed;
    private double m_NumADC_Bits;
    private double m_ADC_FSR;
    private double m_Gain;
    private double m_MCA_CurrFSR;
    private double m_MCA_CurrMax;
    private double m_ChargePerUnit;
    private double m_DC_Offset;
    private double m_Temperature;
    private double m_MCA_AnodeCurrent;
    private double m_MCA_ROI_Avg;
    private double m_NumHistBytes;
    private double m_NumListBytes;
    private double m_NumTraceBytes;
    private double m_NumModule6Bytes;
    private double m_PacketCounter;

    public GammaCalibration()
    {

    }

    public GammaCalibration(GammaCalibration oldCopy)
    {
        this.m_ADC_Speed = oldCopy.m_ADC_Speed;
        this.m_NumADC_Bits = oldCopy.m_NumADC_Bits;
        this.m_ADC_FSR = oldCopy.m_ADC_FSR;
        this.m_Gain = oldCopy.m_Gain;
        this.m_MCA_CurrFSR = oldCopy.m_MCA_CurrFSR;
        this.m_MCA_CurrMax = oldCopy.m_MCA_CurrMax;
        this.m_ChargePerUnit = oldCopy.m_ChargePerUnit;
        this.m_DC_Offset = oldCopy.m_DC_Offset;
        this.m_Temperature = oldCopy.m_Temperature;
        this.m_MCA_AnodeCurrent = oldCopy.m_MCA_AnodeCurrent;
        this.m_MCA_ROI_Avg = oldCopy.m_MCA_ROI_Avg;
        this.m_NumHistBytes = oldCopy.m_NumHistBytes;
        this.m_NumListBytes = oldCopy.m_NumListBytes;
        this.m_NumTraceBytes = oldCopy.m_NumTraceBytes;
        this.m_NumModule6Bytes = oldCopy.m_NumModule6Bytes;
        this.m_PacketCounter = oldCopy.m_PacketCounter;
    }

    public byte[] toLogBytes ()
    {
        ByteBuffer retBuf = ByteBuffer.allocate(10*4);
        retBuf.order(ByteOrder.BIG_ENDIAN);

        retBuf.putInt(LOG_CALIBRATION_VERSION);
        retBuf.putDouble(m_ADC_Speed);
        retBuf.putDouble(m_NumADC_Bits);
        retBuf.putDouble(m_ADC_FSR);
        retBuf.putDouble(m_Gain);
        retBuf.putDouble(m_MCA_CurrFSR);
        retBuf.putDouble(m_MCA_CurrMax);
        retBuf.putDouble(m_ChargePerUnit);
        retBuf.putDouble(m_DC_Offset);
        retBuf.putDouble(m_Temperature);
        retBuf.putDouble(m_MCA_AnodeCurrent);
        retBuf.putDouble(m_MCA_ROI_Avg);
        retBuf.putDouble(m_NumHistBytes);
        retBuf.putDouble(m_NumListBytes);
        retBuf.putDouble(m_NumTraceBytes);
        retBuf.putDouble(m_NumModule6Bytes);
        retBuf.putDouble(m_PacketCounter);

        return retBuf.array();
    }

    public String toLogString ()
    {
        String retMsg = "";

        retMsg += m_ADC_Speed + ",";
        retMsg += m_NumADC_Bits + ",";
        retMsg += m_ADC_FSR + ",";
        retMsg += m_Gain + ",";
        retMsg += m_MCA_CurrFSR + ",";
        retMsg += m_MCA_CurrMax + ",";
        retMsg += m_ChargePerUnit + ",";
        retMsg += m_DC_Offset + ",";
        retMsg += m_Temperature + ",";
        retMsg += m_MCA_AnodeCurrent + ",";
        retMsg += m_MCA_ROI_Avg + ",";
        retMsg += m_NumHistBytes + ",";
        retMsg += m_NumListBytes + ",";
        retMsg += m_NumTraceBytes + ",";
        retMsg += m_NumModule6Bytes + ",";
        retMsg += m_PacketCounter + ",";

        return retMsg;
    }

    public void writeToStream(DataOutputStream output) throws IOException
    {
        output.writeInt(LOG_CALIBRATION_VERSION);
        output.writeDouble(m_ADC_Speed);
        output.writeDouble(m_NumADC_Bits);
        output.writeDouble(m_ADC_FSR);
        output.writeDouble(m_Gain);
        output.writeDouble(m_MCA_CurrFSR);
        output.writeDouble(m_MCA_CurrMax);
        output.writeDouble(m_ChargePerUnit);
        output.writeDouble(m_DC_Offset);
        output.writeDouble(m_Temperature);
        output.writeDouble(m_MCA_AnodeCurrent);
        output.writeDouble(m_MCA_ROI_Avg);
        output.writeDouble(m_NumHistBytes);
        output.writeDouble(m_NumListBytes);
        output.writeDouble(m_NumTraceBytes);
        output.writeDouble(m_NumModule6Bytes);
        output.writeDouble(m_PacketCounter);
    }

    public void readFromStream(DataInputStream input) throws IOException
    {
        int version = input.readInt();
        m_ADC_Speed = input.readDouble();
        m_NumADC_Bits = input.readDouble();
        m_ADC_FSR = input.readDouble();
        m_Gain = input.readDouble();
        m_MCA_CurrFSR = input.readDouble();
        m_MCA_CurrMax = input.readDouble();
        m_ChargePerUnit = input.readDouble();
        m_DC_Offset = input.readDouble();
        m_Temperature = input.readDouble();
        m_MCA_AnodeCurrent = input.readDouble();
        m_MCA_ROI_Avg = input.readDouble();
        m_NumHistBytes = input.readDouble();
        m_NumListBytes = input.readDouble();
        m_NumTraceBytes = input.readDouble();
        m_NumModule6Bytes = input.readDouble();
        m_PacketCounter = input.readDouble();
    }


    public double getADC_Speed ()
    {
        return m_ADC_Speed;
    }
    public double getNumADC_Bits ()
    {
        return m_NumADC_Bits;
    }
    public double getADC_FSR ()
    {
        return m_ADC_FSR;
    }
    public double getGain ()
    {
        return m_Gain;
    }
    public double getMCA_CurrFSR ()
    {
        return m_MCA_CurrFSR;
    }
    public double getMCA_CurrMax ()
    {
        return m_MCA_CurrMax;
    }
    public double getChargePerUnit ()
    {
        return m_ChargePerUnit;
    }
    public double getDC_Offset ()
    {
        return m_DC_Offset;
    }
    public double getTemperature ()
    {
        return m_Temperature;
    }
    public double getMCA_AnodeCurrent ()
    {
        return m_MCA_AnodeCurrent;
    }
    public double getMCA_ROI_Avg ()
    {
        return m_MCA_ROI_Avg;
    }
    public double getNumHistBytes ()
    {
        return m_NumHistBytes;
    }
    public double getNumListBytes ()
    {
        return m_NumListBytes;
    }
    public double getNumTraceBytes ()
    {
        return m_NumTraceBytes;
    }
    public double getNumModule6Bytes ()
    {
        return m_NumModule6Bytes;
    }
    public double getPacketCounter ()
    {
        return m_PacketCounter;
    }

    public void setADC_Speed (double newValue)
    {
        m_ADC_Speed = newValue;
    }
    public void setNumADC_Bits (double newValue)
    {
        m_NumADC_Bits = newValue;
    }
    public void setADC_FSR (double newValue)
    {
        m_ADC_FSR = newValue;
    }
    public void setGain (double newValue)
    {
        m_Gain = newValue;
    }
    public void setMCA_CurrFSR (double newValue)
    {
        m_MCA_CurrFSR = newValue;
    }
    public void setMCA_CurrMax (double newValue)
    {
        m_MCA_CurrMax = newValue;
    }
    public void setChargePerUnit (double newValue)
    {
        m_ChargePerUnit = newValue;
    }
    public void setDC_Offset (double newValue)
    {
        m_DC_Offset = newValue;
    }
    public void setTemperature (double newValue)
    {
        m_Temperature = newValue;
    }
    public void setMCA_AnodeCurrent (double newValue)
    {
        m_MCA_AnodeCurrent = newValue;
    }
    public void setMCA_ROI_Avg (double newValue)
    {
        m_MCA_ROI_Avg = newValue;
    }
    public void setNumHistBytes (double newValue)
    {
        m_NumHistBytes = newValue;
    }
    public void setNumListBytes (double newValue)
    {
        m_NumListBytes = newValue;
    }
    public void setNumTraceBytes (double newValue)
    {
        m_NumTraceBytes = newValue;
    }
    public void setNumModule6Bytes (double newValue)
    {
        m_NumModule6Bytes = newValue;
    }
    public void setPacketCounter (double newValue)
    {
        m_PacketCounter = newValue;
    }

    public void writeToFlatFile(BufferedWriter writer) throws IOException
    {
        String msg = "" + m_ADC_Speed + "\t"
                        + m_NumADC_Bits + "\t"
                        + m_ADC_FSR + "\t"
                        + m_Gain + "\t"
                        + m_MCA_CurrFSR + "\t"
                        + m_MCA_CurrMax + "\t"
                        + m_ChargePerUnit + "\t"
                        + m_DC_Offset + "\t"
                        + m_Temperature + "\t"
                        + m_MCA_AnodeCurrent + "\t"
                        + m_MCA_ROI_Avg + "\t"
                        + m_NumHistBytes + "\t"
                        + m_NumListBytes + "\t"
                        + m_NumTraceBytes + "\t"
                        + m_NumModule6Bytes + "\t"
                        + m_PacketCounter + "\t";

        writer.write(msg);
    }

    public static void writeFlatFileHeader(BufferedWriter writer) throws IOException
    {
        String msg = "" + "ADC_Speed" + "\t"
                        + "NumADC_Bits" + "\t"
                        + "ADC_FSR" + "\t"
                        + "Gain" + "\t"
                        + "MCA_CurrFSR" + "\t"
                        + "MCA_CurrMax" + "\t"
                        + "ChargePerUnit" + "\t"
                        + "DC_Offset" + "\t"
                        + "Temperature" + "\t"
                        + "MCA_AnodeCurrent" + "\t"
                        + "MCA_ROI_Avg" + "\t"
                        + "NumHistBytes" + "\t"
                        + "NumListBytes" + "\t"
                        + "NumTraceBytes" + "\t"
                        + "NumModule6Bytes" + "\t"
                        + "PacketCounter" + "\t";

        writer.write(msg);
    }
}
