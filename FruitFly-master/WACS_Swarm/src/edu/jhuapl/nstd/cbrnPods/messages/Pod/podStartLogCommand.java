package edu.jhuapl.nstd.cbrnPods.messages.Pod;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Pod command used to start the file logging.
 * 
 * @author humphjc1
 */
public class podStartLogCommand extends cbrnPodCommand
{
    public podStartLogCommand()
    {
         super(cbrnSensorTypes.RABBIT_BOARD, cbrnPodCommand.POD_LOG_NEW, 1);
    }
    
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        return super.toByteArray();
    }
}
