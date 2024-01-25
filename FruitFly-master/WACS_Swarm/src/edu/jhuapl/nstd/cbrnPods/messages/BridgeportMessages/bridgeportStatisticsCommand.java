/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class bridgeportStatisticsCommand extends cbrnPodCommand
{
    byte[] m_Data;
    
    public bridgeportStatisticsCommand()
    {
         super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodCommand.BRIDGEPORT_STATISTICS, 0);
    }
    
    public bridgeportStatisticsCommand(byte[] newData)
    {
         super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodCommand.BRIDGEPORT_STATISTICS, 0);
         setData (newData);
    }
    
    
    @Override
    public byte[] toByteArray()
    {
        resetDataLength(m_Data.length + 2);
        
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)m_Data.length);
        
        for (int i = 2; i < 2+m_Data.length; i ++)
            writeDataByte(i, m_Data[i-2]);
        return super.toByteArray();
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
    @Override
    public void setData(byte[] newData) {
        this.m_Data = newData.clone();
    }

}
