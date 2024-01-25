/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Set the rtc of the microcontroller, sync time
 * @author southmk1
 */
public class PodSetRtc extends BioPodMessage {

    private long rtc;

    public PodSetRtc() {
        super(BioPodMessage.SET_RTC, 4);
        setSyncBytes(new char[]{'~','~','~'});
        sensorType = 0; //jch IBAC sensor type

    }
    public PodSetRtc(long rtc){
        this();
        this.rtc = rtc;
    }

    @Override
    public byte[] toByteArray() {
        writeDataInt(0, (int)(0xFFFFFFFF&rtc));
        return super.toByteArray();
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
}
