/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod.messages;

/**
 * Turn on or off the sampler disc
 * @author southmk1
 */
public class IbacCollect extends IbacCommand{
    private boolean on;
    public IbacCollect() {
        super(BioPodMessage.IBAC_COLLECT, 1);
    }
    @Override
    public byte[] toByteArray()
    {
        writeDataByte(1, on?1:0);
        return super.toByteArray();
    }

    /**
     * @return the on
     */
    public boolean isOn() {
        return on;
    }

    /**
     * @param on the on to set
     */
    public void setOn(boolean on) {
        this.on = on;
    }
    
}
