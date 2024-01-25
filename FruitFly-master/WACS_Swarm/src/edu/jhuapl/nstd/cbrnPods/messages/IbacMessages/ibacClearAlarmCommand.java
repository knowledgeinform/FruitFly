/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
     *The alarm latch state is asserted when
    a biological particle alarm is triggered
    and continues to be asserted until
    cleared by the user. This allows for an
    alarm to be detected in the event
    communication is lost during an alarm
    event
 * @author southmk1
 */
public class ibacClearAlarmCommand extends cbrnPodCommand
{
    public ibacClearAlarmCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_CLEAR_ALARM_CMD, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }
}
