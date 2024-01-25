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
public class anacondaActionDeleteCommand extends cbrnPodCommand
{
    private int index;

    public anacondaActionDeleteCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_ACTION_DELETE, 2);
    }
    
    /**
     * @param newIndex new index value
     */
    public anacondaActionDeleteCommand(int newIndex)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_ACTION_DELETE, 2);
         setIndex(newIndex);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)index);
        return super.toByteArray();
    }

    /**
     * @return the Index value
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param newIndex New index value
     */
    public void setIndex(int newIndex) {
        index = Math.min(4,Math.max(1,newIndex));
    }
}
