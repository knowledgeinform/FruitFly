/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class anacondaDebugOptCommand extends cbrnPodCommand
{
    private int debugOpt;

    public anacondaDebugOptCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_DEBUG_OPT, 2);
    }
    
    /**
     * @param newDebugOpt new debugging option
     */
    public anacondaDebugOptCommand(int newDebugOpt)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_DEBUG_OPT, 2);
         setDebugOpt(newDebugOpt);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)debugOpt);
        return super.toByteArray();
    }

    /**
     * @return the Index value
     */
    public int getDebugOpt() {
        return debugOpt;
    }

    /**
     * @param newDebugOpt New index value
     */
    public void setDebugOpt(int newDebugOpt) {
        debugOpt = newDebugOpt;
    }

}
