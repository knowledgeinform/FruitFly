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
public class bladewerxSetAdcCommand extends cbrnPodCommand
{
    /**
     * Value to set ADC option to
     */
    private char m_Opt;
    
    /**
     * Default constructor
     */
    public bladewerxSetAdcCommand()
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_ADC_CMD, 2);
    }
    
    /**
     * @param newOpt Option for ADC value
     */
    public bladewerxSetAdcCommand(char newOpt)
    {
         super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodCommand.BLADEWERX_SET_ADC_CMD, 2);
         setOpt(newOpt);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, m_Opt);
        return super.toByteArray();
    }

    /**
     * @return the Opt value
     */
    public char getOpt() {
        return m_Opt;
    }

    /**
     * @param newOpt New Opt value
     */
    public void setOpt(char newOpt) {
        m_Opt = newOpt;
    }

}
