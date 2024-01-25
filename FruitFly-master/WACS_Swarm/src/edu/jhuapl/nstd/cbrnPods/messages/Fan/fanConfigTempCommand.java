/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.Fan;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class fanConfigTempCommand extends cbrnPodCommand
{
    private byte temperature = 0;

    public fanConfigTempCommand()
    {
         super(cbrnSensorTypes.SENSOR_FAN, cbrnPodCommand.FAN_CONFIG_TEMP, 2);
    }
    
    /**
     * @param newDuty duty value
     */
    public fanConfigTempCommand(byte newTemp)
    {
         super(cbrnSensorTypes.SENSOR_FAN, cbrnPodCommand.FAN_CONFIG_TEMP, 2);
         setTemperature (newTemp);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, temperature);
        return super.toByteArray();
    }

    /**
     * @return the temperature value
     */
    public byte getTemperature() {
        return temperature;
    }

    /**
     * @param newVal temperature value
     */
    public void setTemperature(byte newVal) {
        temperature = newVal;
    }
}
