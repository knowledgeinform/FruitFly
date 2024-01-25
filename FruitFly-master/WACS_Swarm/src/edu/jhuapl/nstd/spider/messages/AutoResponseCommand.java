/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class AutoResponseCommand extends SpiderMessage {
    private int sequence;
    public AutoResponseCommand()
    {
        super(SpiderMessageType.AUTO_RESPONSE,HeaderSize+ChecksumSize+1);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, sequence);
        return super.toByteArray();
    }

    /**
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
