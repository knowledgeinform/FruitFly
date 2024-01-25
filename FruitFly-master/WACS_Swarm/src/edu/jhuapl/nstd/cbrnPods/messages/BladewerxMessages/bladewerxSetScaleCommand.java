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
public class bladewerxSetScaleCommand extends cbrnPodCommand
{
    /**
     * Value to set scale to
     */
    private char m_Scale;
    
    /**
     * Default constructor
     */
    public bladewerxSetScaleCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_SCALE_CMD, 2);
    }
    
    /**
     * @param newScale Value to set for scale
     */
    public bladewerxSetScaleCommand(char newScale)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_SCALE_CMD, 2);
         setScale(newScale);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, m_Scale);
        return super.toByteArray();
    }

    /**
     * @return the Scale value
     */
    public char getScale() {
        return m_Scale;
    }

    /**
     * @param newScale new Scale value
     */
    public void setScale(char newScale) {
        m_Scale = newScale;
    }

}
