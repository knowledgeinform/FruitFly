/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Turn on or off the sampler disc
 * @author southmk1
 */
public class ibacCollectCommand extends cbrnPodCommand
{
    private boolean on;
    
    public ibacCollectCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_COLLECT_CMD, 2);
    }
    
    /**
     * @param boolean Flag for alarm state
     */
    public ibacCollectCommand (boolean ison)
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_COLLECT_CMD, 2);
         setOn(ison);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, on?1:0);
        return super.toByteArray();
    }

    /**
     * @return the on
     */
    public boolean isOn() {
        return on;
    }

    /**
     * @param on the on to set
     */
    public void setOn(boolean on) {
        this.on = on;
    }
    
}