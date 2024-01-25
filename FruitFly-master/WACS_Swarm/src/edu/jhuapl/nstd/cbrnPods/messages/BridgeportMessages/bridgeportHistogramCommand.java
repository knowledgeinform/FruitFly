/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;
import java.util.LinkedList;

/**
 *
 * @author humphjc1
 */
public class bridgeportHistogramCommand extends cbrnPodCommand
{
    /**
     * Identifiying index of the histogram this message comprises
     */
    int m_HistIndex;
    
    /**
     * Number of packets required to create the m_HistIndex'th histogram
     */
    int m_NumPackets;
    
    /**
     * Index of the packet this represents
     */
    int m_PacketIdx;
    
    /**
     * Formatted data
     */
    byte[] m_Data;
    
    /**
     * Maximum length of data array for each histogram packet
     */
    static private int maxDataLength = 200;
    
    
    public bridgeportHistogramCommand()
    {
         super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodCommand.BRIDGEPORT_HISTOGRAM, 0);
    }
    
    public bridgeportHistogramCommand(byte[] newData)
    {
         super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodCommand.BRIDGEPORT_HISTOGRAM, 0);
         setData (newData);
    }
    
    
    @Override
    public byte[] toByteArray()
    {
        resetDataLength(m_Data.length + 9);
        
        writeDataByte(0, (char)commandType);
        writeDataInt(1, m_HistIndex);
        writeDataByte (5, (byte)m_NumPackets);
        writeDataByte (6, (byte)m_PacketIdx);
        writeDataByte(7, (byte)m_Data.length);
        for (int i = 8; i < 8+m_Data.length; i ++)
            writeDataByte(i, m_Data[i-8]);
        return super.toByteArray();
    }

    
    void setHistIndex (int newIdx)
    {
        m_HistIndex = newIdx;
    }
    
    int getHistIndex ()
    {
        return m_HistIndex;
    }
    
    void setNumPackets (int newNum)
    {
        m_NumPackets = newNum;
    }
    
    int getNumPackets ()
    {
        return m_NumPackets;
    }
    
    void setPacketIndex (int newIdx)
    {
        m_PacketIdx = newIdx;
    }
    
    int getPacketIndex ()
    {
        return m_PacketIdx;
    }    
    
    /**
     * @return the board rtc
     */
    public byte[] getData() {
        return m_Data.clone();
    }

    /**
     * @param newRtc New rtc for board
     */
    public void setData(byte[] newData) {
        this.m_Data = newData.clone();
    }
    
    
    /**
     * 
     * @param data Must be ascii character list of spectra, comma separated (including after last one)
     * @return
     */
    public static bridgeportHistogramCommand [] getCommandPackets (String data, int histIndex)
    {
        int lastIndex = 0;
        int index = 0;
        int subStart = 0;
        
        LinkedList <bridgeportHistogramCommand> newCommandList = new LinkedList <bridgeportHistogramCommand> ();
        while (index < data.length()-1 && index != -1)
        {
            index = data.indexOf(",", lastIndex+1);
            if (index - subStart > maxDataLength)
            {
                //Use last index
                bridgeportHistogramCommand newCmd = new bridgeportHistogramCommand(data.substring(subStart, lastIndex+1).getBytes());
                subStart = lastIndex + 1;
                newCommandList.add(newCmd);
            }
            else if (index == data.length() - 1)
            {
                bridgeportHistogramCommand newCmd = new bridgeportHistogramCommand(data.substring(subStart, index+1).getBytes());
                newCommandList.add(newCmd);
                break;
            }
            lastIndex = index;
        }
        
        
        bridgeportHistogramCommand [] cmdList = new bridgeportHistogramCommand[0];
        cmdList = (bridgeportHistogramCommand[]) newCommandList.toArray(cmdList);
        
        for (int i = 0; i < cmdList.length; i ++)
        {
            cmdList[i].m_HistIndex = histIndex;
            cmdList[i].m_NumPackets = cmdList.length;
            cmdList[i].m_PacketIdx = i+1;
            
        }
        
        return cmdList;
    }

}
