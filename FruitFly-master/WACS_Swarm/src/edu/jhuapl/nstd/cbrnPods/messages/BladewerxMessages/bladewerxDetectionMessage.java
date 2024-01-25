package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class bladewerxDetectionMessage extends cbrnPodMsg
{    
    /**
     * timestamp of detection
     */
    private long m_DetTimestamp;
    
    /**
     * Bin number for detection
     */
    private char m_Bin;
    
    
    public bladewerxDetectionMessage() {
        super(cbrnSensorTypes.SENSOR_BLADEWERX,cbrnPodMsg.BLADEWERX_DETECTION_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        m_DetTimestamp = readDataUnsignedInt();
        
        m_Bin = (char)readDataByte();
    }
    
    /**
     * @return the timestamp of the detection
     */
    public long getDetTimestamp() {
        return m_DetTimestamp;
    }

    /**
     * Set the timestamp of the detection
     * @param timestamp the timestamp to set
     */
    public void setDetTimestamp(long timestamp) {
        this.m_DetTimestamp = timestamp;
    }
    
    /**
     * Set bin
     * @param new value
     */
    public void setBin (char bin)
    {
        m_Bin = bin;
    }
    
    /**
     * Get bin
     * \return requested value
     */
    public int getBin ()
    {
        return m_Bin;
    }

}
