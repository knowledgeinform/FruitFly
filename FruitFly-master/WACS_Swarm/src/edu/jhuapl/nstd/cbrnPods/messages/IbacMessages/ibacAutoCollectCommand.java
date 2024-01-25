/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * auto collection based on alarm. either on or off, and
 * collector will run for at least runtime.
 * @author southmk1
 */
public class ibacAutoCollectCommand extends cbrnPodCommand
{
    //Default on
    private boolean on;
    
    //Default 60
    private int minimumRuntime;
    
    
    public ibacAutoCollectCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_AUTO_COLLECT_CMD, 6);
    }
    
    /**
     * @param rate New reporting rate
     */
    public ibacAutoCollectCommand (boolean newOn, int newMinRuntime)
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_AUTO_COLLECT_CMD, 6);
         setOn(newOn);
         setMinimumRuntime(newMinRuntime);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, on?1:0);
        writeDataInt(2, minimumRuntime);
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

    /**
     * @return the minimumRuntime
     */
    public int getMinimumRuntime() {
        return minimumRuntime;
    }

    /**
     * @param minimumRuntime the minimumRuntime to set
     */
    public void setMinimumRuntime(int minimumRuntime) {
        this.minimumRuntime = minimumRuntime;
    }
}
