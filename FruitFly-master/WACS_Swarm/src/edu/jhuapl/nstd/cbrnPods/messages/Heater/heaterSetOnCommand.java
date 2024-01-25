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
public class heaterSetOnCommand extends cbrnPodCommand
{   
    public heaterSetOnCommand()
    {
         super(cbrnSensorTypes.SENSOR_HEATER, cbrnPodCommand.HEATER_SET_ON, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }

}
