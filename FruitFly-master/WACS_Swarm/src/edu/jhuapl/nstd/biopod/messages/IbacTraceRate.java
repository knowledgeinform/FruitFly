/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Output rate of airsample command, in seconds.
 * @author southmk1
 */
public class IbacTraceRate extends IbacCommand {

    private int traceRate;

    public IbacTraceRate() {
        super(BioPodMessage.IBAC_TRACE_RATE, 1);
    }
    @Override
    public byte[] toByteArray() {
        writeDataByte(1, traceRate);
        return super.toByteArray();
    }

    /**
     * @return the traceRate
     */
    public int getTraceRate() {
        return traceRate;
    }

    /**
     * @param traceRate the traceRate to set
     */
    public void setTraceRate(int traceRate) {
        this.traceRate = traceRate;
    }
}
