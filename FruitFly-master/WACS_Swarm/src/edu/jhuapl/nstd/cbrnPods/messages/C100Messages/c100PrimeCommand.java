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
public class c100PrimeCommand extends cbrnPodCommand
{
    private int numSecs = 0;

    public c100PrimeCommand()
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_PRIME_CMD, 2);
    }
    
    /**
     * @param sec Number of seconds to set
     */
    public c100PrimeCommand(int secs)
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_PRIME_CMD, 2);
         setNumSecs(secs);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)numSecs);
        return super.toByteArray();
    }

    /**
     * @return the seconds value
     */
    public int getNumSecs() {
        return numSecs;
    }

    /**
     * @param sec Number of seconds to set
     */
    public void setNumSecs(int secs) {
        numSecs = Math.min(255,Math.max(0,secs));
    }
}
