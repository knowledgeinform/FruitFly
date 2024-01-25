/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages;

/**
 *
 * @author humphjc1
 */
public class cbrnPodMsg extends cbrnPodBytes {

    /**
     * byte 12 message type
     */
    protected int messageType;
    
    /**
     * Message time
     */
    protected long timestampMs;
    
    /**
     * True if message was parsed successfully
     */
    protected boolean validMessage = false;
    
    private int parseIndex;
    protected static char SYNC_BYTE = '*';
    
    
    public final static int POD_HEARTBEAT_TYPE = 0x00;
    
    public final static int C100_STATUS_TYPE = 0x01;
    public final static int C100_ACTION_TYPE = 0x02;
    public final static int C100_FLOWSTATUS_TYPE = 0x03;
    public final static int C100_WARNING_TYPE = 0x04;

    public final static int SERVO_STATUS_TYPE = 0x01;
    
    public final static int IBAC_PARTICLE_COUNT_TYPE = 0x01;
    public final static int IBAC_DIAGNOSTICS_TYPE = 0x02;
    
    public final static int BLADEWERX_STATUS_TYPE = 0x01;
    public final static int BLADEWERX_DETECTION_TYPE = 0x02;
    public final static int BLADEWERX_COMPOSITE_HISTOGRAM = 0x03;
    public final static int BLADEWERX_DLL_DETECTION_REPORT = 0x04;
    public final static int BLADEWERX_AETNA_DETECTION_REPORT = 0x05;
    
    public final static int COUNT_ITEM = 0x06;
    
    public final static int BLADEWERX_PUMP_STATUS_TYPE = 0x01;
    
    public final static int BRIDGEPORT_STATISTICS = 0x01;
    public final static int BRIDGEPORT_HISTOGRAM = 0x02;
    public final static int BRIDGEPORT_CONFIGURATION = 0x03;
    public final static int BRIDGEPORT_COMPOSITE_HISTOGRAM = 0x04;
    public final static int BRIDGEPORT_DETECTION_REPORT = 0x05;
    // COUNT_ITEM also applicable for bridgeport
    
    public final static int ANACONDA_STATUS_TYPE = 0x01;
    public final static int ANACONDA_LCDA_REPORT = 0x02;
    public final static int ANACONDA_LCDB_REPORT = 0x03;
    public final static int ANACONDA_TEXT = 0x04;
    public final static int ANACONDA_LCDA_G_SPECTRA = 0x05;
    public final static int ANACONDA_LCDA_H_SPECTRA = 0x06;
    public final static int ANACONDA_LCDB_G_SPECTRA = 0x07;
    public final static int ANACONDA_LCDB_H_SPECTRA = 0x08;
    
    public final static int CANBERRA_DETECTION_REPORT = 0x01;
    
    
    public cbrnPodMsg() {
        this.syncBytes[0] = this.syncBytes[1] = this.syncBytes[2] = (char) SYNC_BYTE;
        HeaderSize = 13;
        
        resetDataLength (0);
    }

    /**
     * 
     * @param sensorType
     * @param messageType
     * @param length Data array length, not total byte stream length
     */
    protected cbrnPodMsg(int sensorType, int messageType, int length) {
        this.sensorType = sensorType;
        this.messageType = messageType;
        this.syncBytes[0] = this.syncBytes[1] = this.syncBytes[2] = (char) SYNC_BYTE;
        HeaderSize = 13;
        
        resetDataLength (length);
    }
    
    public boolean isInstanceOf(cbrnPodMsg m) {
        boolean result = m.getMessageType() == this.getMessageType() && m.getSensorType() == this.getSensorType();
        return (result);
    }
    
    public void parseBioPodMessage(cbrnPodMsg m)
    {
        setValidMessage(m.isValidMessage());
        syncBytes[0] = m.syncBytes[0];
        syncBytes[1] = m.syncBytes[1];
        syncBytes[2] = m.syncBytes[2];
        sensorType = m.sensorType;
        fromIP =  m.fromIP;
        fromPort = m.fromPort;
        length = m.length;
        setChecksum((int) m.checksum);
        setMessageType((int) m.messageType);
        this.setData(m.data.clone());
    }
    
    public void parseByte(byte b) {
        if (parseIndex < HeaderSize) {
            parseHeaderByte(parseIndex, b);
        } else if (parseIndex < length - ChecksumSize) {
            //body
            parseBody(parseIndex, b);
        } else {
            //checksum
            parseChecksum(parseIndex, b);
        }
    }

    protected void parseHeaderByte(int index, byte b) {
        switch (index) {
            case 0:
            case 1:
            case 2:
                if (b == SYNC_BYTE) {
                    syncBytes[index] = (char) b;
                } else {
                    clearMessage();
                    return;
                }
                break;
            case 3: 
                sensorType = b; 
                break; 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
                fromIP |= ((b&0xff) << ((7 - index) * 8)); 
                break;
            case 8: 
            case 9: 
                fromPort |= ((b&0xff) << ((9 - index) * 8));
                break;
            case 10: 
                length = (0xff&b) << 8;
                break;
            case 11:
                length |= (b&0xff);
                data = new byte[length - HeaderSize - ChecksumSize];   //length is the whole message size (header + checksum)
                break;
            case 12: 
                messageType = b;
                break;
            default:
                clearMessage();
                return;
        }
        parseIndex++;
    }

    protected void parseBody(int index, byte b) {
        try {
            data[index - HeaderSize] = b;
        } catch (Exception e) {
            clearMessage();
            return;
        }
        parseIndex++;
    }

    protected void parseChecksum(int index, byte b) {
        /*4.6	Checksum
        Checksum: this is defined as Checksum = 0 –(8 bit sum over packet except the checksum). Therefore an 8 bit sum over the whole packet including the checksum = 0.
         */
        byte[] bytes = toByteArray();
        if (b == bytes[bytes.length - ChecksumSize]) {
            //valid message
            setChecksum(b);
            setValidMessage(true);
        } else {
            clearMessage();
        }
    }

    protected void clearMessage() {
        syncBytes[0] = syncBytes[1] =syncBytes[2] = 0;
        setChecksum(0);
        setFromIP(0);
        setFromPort(0);
        setWholeLength(0);
        setData(new byte[0]);
        setMessageType(0);
        setValidMessage(false);
        setParseIndex(0);
    }
    
    @Override
    public byte[] toByteArray() 
    {
        
        byte[] byteArray = new byte[length];  
        int indx = pre_toByteArray (byteArray);
        
        byteArray[indx++] = (byte) messageType;

        post_toByteArray(byteArray, indx);
        
        return byteArray;
    }
    
    /**
     * @param parseIndex the parseIndex to set
     */
    public void setParseIndex(int parseIndex) {
        this.parseIndex = parseIndex;
    }
    
    /**
     * @param validMessage the validMessage to set
     */
    public void setValidMessage(boolean validMessage) {
        this.validMessage = validMessage;
    }
    
    public boolean isValidMessage() {
        return validMessage;
    }
    
    /**
     * @return the messageType
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    
    /**
     * @return the timestamp
     */
    public long getTimestampMs()
    {
        return timestampMs;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestampMs(long timestampMs)
    {
        this.timestampMs = timestampMs;
    }
    
    public String toLogString ()
    {
        return preToLogString () + messageType + "\t" + postToLogString ();
    }
}
