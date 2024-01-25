package edu.jhuapl.nstd.cbrnPods.messages.Pod;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Pod command used to shutdown the file logger.
 * 
 * @author humphjc1
 */
public class podShutdownLogCommand extends cbrnPodCommand
{
    public podShutdownLogCommand()
    {
         super(cbrnSensorTypes.RABBIT_BOARD, cbrnPodCommand.POD_LOG_END, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }

}
