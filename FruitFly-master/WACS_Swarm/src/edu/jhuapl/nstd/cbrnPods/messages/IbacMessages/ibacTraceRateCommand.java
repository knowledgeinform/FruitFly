/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Output rate of airsample command, in seconds.
 * @author southmk1
 */
public class ibacTraceRateCommand extends cbrnPodCommand
{
    private int traceRate; //In 1/10 Seconds
    
    public ibacTraceRateCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_TRACE_RATE_CMD, 2);
    }
    
    /**
     * @param rate New reporting rate
     */
    public ibacTraceRateCommand (int rate)
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_TRACE_RATE_CMD, 2);
         setRate(rate);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)traceRate);
        return super.toByteArray();
    }

    /**
     * @return the reporting rate
     */
    public int getRate() {
        return traceRate;
    }

    /**
     * @param rate New reporting rate
     */
    public void setRate(int rate) {
        this.traceRate = rate;
    }
}
