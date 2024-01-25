/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * turn alarm on or off based on isAlarm
 * @author southmk1
 */
public class ibacAlarmCommand extends cbrnPodCommand
{
    private boolean isAlarm = false;
    
    public ibacAlarmCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_ALARM_CMD, 2);
    }
    
    /**
     * @param boolean Flag for alarm state
     */
    public ibacAlarmCommand (boolean alarm)
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_ALARM_CMD, 2);
         setIsAlarm(alarm);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, isAlarm?1:0);
        return super.toByteArray();
    }

    /**
     * @return the isAlarm
     */
    public boolean isIsAlarm() {
        return isAlarm;
    }

    /**
     * @param isAlarm the isAlarm to set
     */
    public void setIsAlarm(boolean isAlarm) {
        this.isAlarm = isAlarm;
    }
   

   
}
