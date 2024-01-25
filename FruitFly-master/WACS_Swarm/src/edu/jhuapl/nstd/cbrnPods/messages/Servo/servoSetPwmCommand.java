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
public class servoSetPwmCommand extends cbrnPodCommand
{
    private char dutyVal = 0;
    private char index = 0;

    public servoSetPwmCommand()
    {
        super(cbrnSensorTypes.SENSOR_SERVO, cbrnPodCommand.SERVO_SET_PWM, 3);
    }

    /**
     * @param index servo index
     * @param newDuty duty value
     */
    public servoSetPwmCommand(char index, char newDuty)
    {
        super(cbrnSensorTypes.SENSOR_SERVO, cbrnPodCommand.SERVO_SET_PWM, 3);
        setIndex (index);
        setDuty (newDuty);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, dutyVal);
        writeDataByte(2, index);
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

    /**
     * @return the duty value
     */
    public char getDuty() {
        return dutyVal;
    }

    /**
     * @param newVal duty value
     */
    public void setDuty(char newVal) {
        dutyVal = newVal;
    }

}
