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
public class bladewerxSetGainCommand extends cbrnPodCommand
{
    /**
     * Value to set Gain to
     */
    private char m_Gain;
    
    /**
     * Default constructor
     */
    public bladewerxSetGainCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_GAIN_CMD, 2);
    }
    
    /**
     * @param newGain Value to set for Gain
     */
    public bladewerxSetGainCommand(char newGain)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_GAIN_CMD, 2);
         setGain(newGain);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, m_Gain);
        return super.toByteArray();
    }

    /**
     * @return the Gain value
     */
    public char getGain() {
        return m_Gain;
    }

    /**
     * @param newGain new Gain value
     */
    public void setGain(char newGain) {
        m_Gain = newGain;
    }

}
