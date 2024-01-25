/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxPumpMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class bladewerxPumpPowerCommand extends cbrnPodCommand
{
    /**
     * Whether to toggle on or off power for pump
     */
    private boolean m_On = true;
    
    /**
     * Default constructor
     */
    public bladewerxPumpPowerCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodCommand.BLADEWERX_PUMP_CONTROL_COMMAND, 2);
    }
    
    /**
     * @param newOn Whether to set on or not
     */
    public bladewerxPumpPowerCommand(boolean newOn)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX_PUMP, cbrnPodCommand.BLADEWERX_PUMP_CONTROL_COMMAND, 2);
         setOn(newOn);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (m_On?1:0));
        return super.toByteArray();
    }

    /**
     * @return the On value
     */
    public boolean getOn() {
        return m_On;
    }

    /**
     * @param newOn New On value
     */
    public void setOn(boolean newOn) {
        m_On = newOn;
    }

}
