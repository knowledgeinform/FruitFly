/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.Heater;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class heaterConfigHumidityCommand extends cbrnPodCommand
{
    private byte humidity = 0;

    public heaterConfigHumidityCommand()
    {
         super(cbrnSensorTypes.SENSOR_HEATER, cbrnPodCommand.HEATER_CONFIG_HUMIDITY, 2);
    }
    
    /**
     * @param newDuty duty value
     */
    public heaterConfigHumidityCommand(byte newHumidity)
    {
         super(cbrnSensorTypes.SENSOR_HEATER, cbrnPodCommand.HEATER_CONFIG_HUMIDITY, 2);
         setHumidity (newHumidity);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, humidity);
        return super.toByteArray();
    }

    /**
     * @return the humidity value
     */
    public byte getHumidity() {
        return humidity;
    }

    /**
     * @param newVal humidity value
     */
    public void setHumidity(byte newVal) {
        humidity = newVal;
    }

}
