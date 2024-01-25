/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.Servo;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class servoSetOpenCommand extends cbrnPodCommand
{
    private char index = 0;

    public servoSetOpenCommand(char index)
    {
        super(cbrnSensorTypes.SENSOR_SERVO, cbrnPodCommand.SERVO_SET_OPEN, 2);
        setIndex(index);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, index);
        return super.toByteArray();
    }

    /**
     * @return the index value
     */
    public char getIndex() {
        return index;
    }

    /**
     * @param index index value
     */
    public void setIndex(char index) {
        this.index = index;
    }
}
