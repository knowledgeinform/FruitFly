/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class anacondaResetSampleUsage extends cbrnPodCommand
{
    public anacondaResetSampleUsage()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_RESET_SAMPLEUSAGE, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }
}
