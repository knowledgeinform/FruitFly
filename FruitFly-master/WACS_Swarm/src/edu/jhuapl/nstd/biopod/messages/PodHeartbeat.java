/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 *
 * @author southmk1
 */
public class PodHeartbeat extends BioPodMessage {
    private int status;
    private long rtc;
    private boolean sleeping;
    private long lastSerialReceive;
    
    private String lastToken;


    public PodHeartbeat() {
        super(BioPodMessage.HEARTBEAT, 0);
    }

    @Override
    public void parseBioPodMessage(BioPodMessage m) {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        setStatus(readDataShort());
        rtc = readDataUnsignedInt();
        sleeping = readDataBool();
        lastSerialReceive = readDataUnsignedInt();
        StringBuilder sb = new StringBuilder();
        while(super.getReadIndex()<super.getBodyLength())
        {
            sb.append((char)super.readDataByte());
        }
        lastToken = sb.toString();
    }

    /**
     * @return the rtc
     */
    public long getRtc() {
        return rtc;
    }

    /**
     * @param rtc the rtc to set
     */
    public void setRtc(long rtc) {
        this.rtc = rtc;
    }

    /**
     * @return the lastSerialReceive
     */
    public long getLastSerialReceive() {
        return lastSerialReceive;
    }

    /**
     * @param lastSerialReceive the lastSerialReceive to set
     */
    public void setLastSerialReceive(long lastSerialReceive) {
        this.lastSerialReceive = lastSerialReceive;
    }

    /**
     * @return the lastToken
     */
    public String getLastToken() {
        return lastToken;
    }

    /**
     * @param lastToken the lastToken to set
     */
    public void setLastToken(String lastToken) {
        this.lastToken = lastToken;
    }

    /**
     * @return the sleeping
     */
    public boolean isSleeping() {
        return sleeping;
    }

    /**
     * @param sleeping the sleeping to set
     */
    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
