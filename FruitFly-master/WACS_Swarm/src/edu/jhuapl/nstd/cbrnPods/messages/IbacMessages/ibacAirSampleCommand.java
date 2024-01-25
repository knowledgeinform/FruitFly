/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Trigger an air sample. unit will respond with a $trace,
 * which the uc will send back as a particle count message
 * @author southmk1
 */
public class ibacAirSampleCommand extends cbrnPodCommand
{
    public ibacAirSampleCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_AIR_SAMPLE_CMD, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }
}
