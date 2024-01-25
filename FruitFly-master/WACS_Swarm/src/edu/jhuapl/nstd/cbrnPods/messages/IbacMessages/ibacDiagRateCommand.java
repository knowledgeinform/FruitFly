/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Output rate of diagnostics portion of Diagnostics message to use.
 * Information on alarms and last time of alarm, etc affected
 * @author southmk1
 */
public class ibacDiagRateCommand extends cbrnPodCommand
{
    private int diagRate; //In Seconds
    
    public ibacDiagRateCommand()
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_DIAG_RATE_CMD, 2);
    }
    
    /**
     * @param rate New reporting rate
     */
    public ibacDiagRateCommand (int rate)
    {
         super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_DIAG_RATE_CMD, 2);
         setRate(rate);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)diagRate);
        return super.toByteArray();
    }

    /**
     * @return the reporting rate
     */
    public int getRate() {
        return diagRate;
    }

    /**
     * @param rate New reporting rate
     */
    public void setRate(int rate) {
        this.diagRate = rate;
    }
     
}
