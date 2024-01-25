/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class bladewerxSetThresholdCommand extends cbrnPodCommand
{
    /**
     * Value to set Threshold to
     */
    private char m_Threshold;
    
    /**
     * Default constructor
     */
    public bladewerxSetThresholdCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_THRESHOLD_CMD, 2);
    }
    
    /**
     * @param newThreshold Value to set for Threshold
     */
    public bladewerxSetThresholdCommand(char newThreshold)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_THRESHOLD_CMD, 2);
         setThreshold(newThreshold);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, m_Threshold);
        return super.toByteArray();
    }

    /**
     * @return the Threshold value
     */
    public char getThreshold() {
        return m_Threshold;
    }

    /**
     * @param newThreshold new Threshold value
     */
    public void setThreshold(char newThreshold) {
        m_Threshold = newThreshold;
    }

}
