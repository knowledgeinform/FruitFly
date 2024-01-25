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
public class c100FlowStatusCommand extends cbrnPodCommand
{
    private char show;

    public c100FlowStatusCommand()
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_FLOWSTATUS_CMD, 2);
    }

    /**
     * @param showVal show value
     */
    public c100FlowStatusCommand(int showVal)
    {
         super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_FLOWSTATUS_CMD, 2);
         setShowVal(showVal);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)show);
        return super.toByteArray();
    }

    /**
     * @return the seconds value
     */
    public int getShowVal() {
        return show;
    }

    /**
     * @param newY New y value
     */
    public void setShowVal(int showVal) {
        show = (char)showVal;
    }

}
