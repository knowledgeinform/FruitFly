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
public class anacondaSetServoClosedLimitCommand extends cbrnPodCommand
{
    private short limit;   //Desired limit
    
    public anacondaSetServoClosedLimitCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_SERVO_CLOSEDLIMIT, 3);
    }
    
    /**
     * @param newTemp Desired temperature
     */
    public anacondaSetServoClosedLimitCommand (short newLimit)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_SERVO_CLOSEDLIMIT, 3);
         setLimit (newLimit);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataShort(1, limit);
        return super.toByteArray();
    }

    /**
     * @return the Desired limit
     */
    public short getLimit() {
        return limit;
    }

    /**
     * @param newLimit Desired limit
     */
    public void setLimit(short newLimit) {
        this.limit = newLimit;
    }

}
