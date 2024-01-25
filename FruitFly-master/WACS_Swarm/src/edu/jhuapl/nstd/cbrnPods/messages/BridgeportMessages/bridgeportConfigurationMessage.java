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
public class bridgeportConfigurationMessage extends cbrnPodMsg
{    
    /**
     * Length of data logged
     */
    int m_Length;

    
    public bridgeportConfigurationMessage() {
        super(cbrnSensorTypes.SENSOR_BRIDGEPORT, cbrnPodMsg.BRIDGEPORT_CONFIGURATION, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        m_Length = readDataByte();
    }
    
    /**
     * Accessor for data size
     * @return
     */
    public int getLength ()
    {
        return m_Length;
    }
    
    /**
     * Modifier for data size
     * @param newVal
     */
    public void setLength (int newVal)
    {
        m_Length = newVal;
    }

}
