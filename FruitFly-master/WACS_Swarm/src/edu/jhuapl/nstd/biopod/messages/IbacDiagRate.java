/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Output rate of diagnostics portion of Diagnostics message to use.
 * Information on alarms and last time of alarm, etc affected
 * @author southmk1
 */
public class IbacDiagRate extends IbacCommand {

    private int diagRate; //In Seconds

    public IbacDiagRate() {
        super(BioPodMessage.IBAC_DIAG_RATE, 5);
    }
     @Override
    public byte[] toByteArray()
    {
        writeDataByte(1, diagRate);
        return super.toByteArray();
    }

    /**
     * @return the diagRate
     */
    public int getDiagRate() {
        return diagRate;
    }

    /**
     * @param diagRate the diagRate to set
     */
    public void setDiagRate(int diagRate) {
        this.diagRate = diagRate;
    }
     
}
