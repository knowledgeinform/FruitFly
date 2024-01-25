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
public class fanAutoControlCommand extends cbrnPodCommand
{   
    public fanAutoControlCommand()
    {
         super(cbrnSensorTypes.SENSOR_FAN, cbrnPodCommand.FAN_AUTO_CONTROL, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }

}
