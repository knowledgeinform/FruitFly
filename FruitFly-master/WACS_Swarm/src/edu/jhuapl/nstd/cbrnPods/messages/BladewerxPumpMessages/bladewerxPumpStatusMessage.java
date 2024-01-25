/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class bladewerxPumpStatusMessage extends cbrnPodMsg
{
    /**
     * Power state of pump
     */
    private boolean m_PumpOn;
    
    
    public bladewerxPumpStatusMessage() {
        super(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodMsg.BLADEWERX_PUMP_STATUS_TYPE, 0);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        
        m_PumpOn = readDataBool ();
    }
    
    /**
     * Set power state
     * @param new value
     */
    public void setPower (boolean newOn)
    {
        m_PumpOn = newOn;
    }
    
    /**
     * Get power state 
     * \return requested value
     */
    public boolean getPower ()
    {
        return m_PumpOn;
    }
}
