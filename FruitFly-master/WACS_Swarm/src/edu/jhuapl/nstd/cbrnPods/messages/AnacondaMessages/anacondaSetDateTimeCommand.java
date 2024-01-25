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
public class anacondaSetDateTimeCommand extends cbrnPodCommand
{
    private long time;   //Current time, in seconds (not millis)
    
    public anacondaSetDateTimeCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_DATETIME, 5);
    }
    
    /**
     * @param rate New date/time for Anaconda, in seconds
     */
    public anacondaSetDateTimeCommand (long newTime)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_SET_DATETIME, 5);
         setTimeSec(newTime);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataInt(1, (int)time);
        return super.toByteArray();
    }

    /**
     * @return the Anaconda Time
     */
    public long getTimeSec() {
        return time;
    }

    /**
     * @param newTime New Time for Anaconda
     */
    public void setTimeSec(long newTime) {
        this.time = newTime;
    }


}
