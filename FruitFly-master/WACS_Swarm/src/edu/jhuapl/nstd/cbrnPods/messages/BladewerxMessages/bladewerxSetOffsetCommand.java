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
public class bladewerxSetOffsetCommand extends cbrnPodCommand
{
    /**
     * Value to set Offset to
     */
    private char m_Offset;
    
    /**
     * Default constructor
     */
    public bladewerxSetOffsetCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_OFFSET_CMD, 2);
    }
    
    /**
     * @param newOffset Value to set for Offset
     */
    public bladewerxSetOffsetCommand(char newOffset)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_OFFSET_CMD, 2);
         setOffset(newOffset);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, m_Offset);
        return super.toByteArray();
    }

    /**
     * @return the Offset value
     */
    public char getOffset() {
        return m_Offset;
    }

    /**
     * @param newOffset new Offset value
     */
    public void setOffset(char newOffset) {
        m_Offset = newOffset;
    }

}
