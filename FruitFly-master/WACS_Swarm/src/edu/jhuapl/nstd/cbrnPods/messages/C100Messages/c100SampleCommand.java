/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class c100SampleCommand extends cbrnPodCommand
{
    private int Y = 1;

    public c100SampleCommand()
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_SAMPLE_CMD, 2);
    }
    
    /**
     * @param newY y value
     */
    public c100SampleCommand(int newY)
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_SAMPLE_CMD, 2);
         setY(newY);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)Y);
        return super.toByteArray();
    }

    /**
     * @return the seconds value
     */
    public int getY() {
        return Y;
    }

    /**
     * @param newY New y value
     */
    public void setY(int newY) {
        Y = Math.min(4,Math.max(1,newY));
    }

}
