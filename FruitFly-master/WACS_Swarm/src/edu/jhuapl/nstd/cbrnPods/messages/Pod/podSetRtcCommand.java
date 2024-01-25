package edu.jhuapl.nstd.cbrnPods.messages.Pod;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Pod command to set the RTC.
 * 
 * @author humphjc1
 */
public class podSetRtcCommand extends cbrnPodCommand
{
    private long rtc;   //Current time, in seconds (not millis)
    
    public podSetRtcCommand()
    {
         super(cbrnSensorTypes.RABBIT_BOARD, cbrnPodCommand.POD_SET_RTC, 5);
    }
    
    /**
     * @param rate New rtc for board, in seconds
     */
    public podSetRtcCommand (long newRtc)
    {
         super(cbrnSensorTypes.RABBIT_BOARD, cbrnPodCommand.POD_SET_RTC, 5);
         setRtcSec(newRtc);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataInt(1, (int)rtc);
        return super.toByteArray();
    }

    /**
     * @return the board rtc
     */
    public long getRtcSec() {
        return rtc;
    }

    /**
     * @param newRtc New rtc for board
     */
    public void setRtcSec(long newRtc) {
        this.rtc = newRtc;
    }

}
