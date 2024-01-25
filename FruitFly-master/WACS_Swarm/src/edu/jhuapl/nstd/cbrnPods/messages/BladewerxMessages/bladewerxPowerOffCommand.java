/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class bladewerxPowerOffCommand extends cbrnPodCommand
{
    public bladewerxPowerOffCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_POWER_OFF_CMD, 1);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }
}
