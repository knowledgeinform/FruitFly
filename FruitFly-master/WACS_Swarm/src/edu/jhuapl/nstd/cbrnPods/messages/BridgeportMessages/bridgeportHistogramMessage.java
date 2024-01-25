/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class bridgeportHistogramMessage extends cbrnPodMsg
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

    
    public bridgeportHistogramMessage() {
        super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_HISTOGRAM, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        
        m_HistIndex = readDataInt();
        m_NumPackets = readDataByte();
        m_PacketIdx = readDataByte();
    }
    
    /**
     * Accessor for histogram index
     * @return
     */
    public int getHistIndex ()
    {
        return m_HistIndex;
    }
    
    /**
     * Modifier for histogram index
     * @param newVal
     */
    public void setHistIndex (int newVal)
    {
        m_HistIndex = newVal;
    }
    
    /**
     * Accessor for packet count
     * @return
     */
    public int getNumPackets ()
    {
        return m_NumPackets;
    }
    
    /**
     * Modifier for packet count
     * @param newVal
     */
    public void setNumPackets (int newVal)
    {
        m_NumPackets = newVal;
    }
    
    /**
     * Accessor for packet index
     * @return
     */
    public int getPacketIndex ()
    {
        return m_PacketIdx;
    }
    
    /**
     * Modifier for packet index
     * @param newVal
     */
    public void setPacketIndex (int newVal)
    {
        m_PacketIdx = newVal;
    }
}
