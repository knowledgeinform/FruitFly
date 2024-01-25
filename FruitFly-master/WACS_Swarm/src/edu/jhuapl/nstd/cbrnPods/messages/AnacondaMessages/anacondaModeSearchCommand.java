package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Command message used to put the Anaconda in SEARCH mode.
 * 
 * @author humphjc1
 */
public class anacondaModeSearchCommand extends cbrnPodCommand
{
    private int index;

    public anacondaModeSearchCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_MODE_SEARCH, 2);
    }
    
    /**
     * @param newIndex new index value
     */
    public anacondaModeSearchCommand(int newIndex)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_MODE_SEARCH, 2);
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
