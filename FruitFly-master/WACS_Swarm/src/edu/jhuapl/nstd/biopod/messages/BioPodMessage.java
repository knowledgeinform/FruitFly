/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

import java.util.Arrays;

/**
 *
 * @author southmk1
 */
public class BioPodMessage {

    //byte 0,1,2
    private char[] syncBytes = new char[3];
    //byte 3 //jch
    protected int sensorType; //jch
    //byte 3,4,5,6
    private int fromIP;
    //byte 7,8
    private int fromPort;
    //byte 9 jch total length of message
    private int length;
    //byte 10
    private int messageType;
    //byte 4..length-2
    private byte[] data;
    //byte length-1
    private int checksum;
    private int parseIndex;
    private int readIndex;
    protected static char SYNC_BYTE = '*';
    protected static int HeaderSize = 12; //jch
    protected static int ChecksumSize = 1;
    private boolean validMessage = false;

    //message types
    public static int HEARTBEAT = 0x00;
    public static int BIO_POD_PARTICLE_COUNT_MSG = 0x01;
    public static int BIO_POD_DIAGNOSTICS_MSG = 0x02;
    public static int SET_RTC = 0x03;
    public static int SHUTDOWN = 0xFF;

    public static int IBAC_COMMAND_TYPE = 0x10;
    
    public static int IBAC_ALARM = 0x11;
    public static int IBAC_CLEAR_ALARM = 0x12;
    public static int IBAC_STATUS = 0x13;
    public static int IBAC_SLEEP = 0x14;
    public static int IBAC_TRACE_RATE = 0x15;
    public static int IBAC_DIAG_RATE = 0x16;
    public static int IBAC_AIR_SAMPLE = 0x17;
    public static int IBAC_COLLECT = 0x18;
    public static int IBAC_AUTO_COLLECT = 0x19;
    public static int IBAC_RAW = 0x1A;
    

    public BioPodMessage() {
        this.syncBytes[0] = this.syncBytes[1] = this.syncBytes[2] = (char) SYNC_BYTE;
        this.length = HeaderSize + ChecksumSize;
        this.data = new byte[0];
    }

    protected BioPodMessage(int messageType, int length) {
        this.messageType = messageType;
        this.length = length + HeaderSize + ChecksumSize;
        this.syncBytes[0] = this.syncBytes[1] = this.syncBytes[2] = (char) SYNC_BYTE;

        this.data = new byte[length];
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
            case 3: //jch
                sensorType = b; //jch
                break; //jch
            case 4: //jch
            case 5: //jch
            case 6: //jch
            case 7: //jch
                fromIP |= ((b&0xff) << ((7 - index) * 8)); //jch
                //fromIP |= ((b&0xff) << ((index-4) * 8)); //jch changed order or byte read
                break;
            case 8: //jch
            case 9: //jch
                fromPort |= ((b&0xff) << ((9 - index) * 8)); //jch
                break;
            case 10: //jch
                length = 0xff&b;
                data = new byte[length - HeaderSize - 1];   //jch to make length be the whole message (header + checksum)
                break;
            case 11: //jch
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
        if (b == bytes[bytes.length - 1]) {
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

    public boolean isValidMessage() {
        return validMessage;
    }

    public byte calculateChecksum(byte[] bytes) {
        int chk = 0;
        for (byte b : bytes) {
            chk = (chk + b);
        }
        return (byte) (chk);
    }

    public boolean isInstanceOf(BioPodMessage m) {
        return (m.getMessageType() == this.getMessageType());
    }

    public void parseBioPodMessage(BioPodMessage m) {
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

    public byte[] toByteArray() {
        byte[] byteArray = new byte[length];  //jch changed length meaning

        //Header
        int indx = 0;
        byteArray[indx++] = (byte) syncBytes[0];
        byteArray[indx++] = (byte) syncBytes[1];
        byteArray[indx++] = (byte) syncBytes[2];
        byteArray[indx++] = (byte) sensorType;
        byteArray[indx++] = (byte) (fromIP >> 24);
        byteArray[indx++] = (byte) (fromIP >> 16);
        byteArray[indx++] = (byte) (fromIP >> 8);
        byteArray[indx++] = (byte) (fromIP);
        byteArray[indx++] = (byte) (fromPort >> 8);
        byteArray[indx++] = (byte) (fromPort);
        byteArray[indx++] = (byte) length;
        byteArray[indx++] = (byte) messageType;


        int bodyLength = length - indx - 1; //jch
        //Body
        for (int i = 0; i < bodyLength; i++) {      //jch bodyLength changed since length is whole message now
            byteArray[indx++] = data[i];
        }

        //Checksum
        byteArray[length - 1] = calculateChecksum(Arrays.copyOfRange(byteArray, 0, length - 1));    //jch changed length meaning
        return byteArray;
    }

    protected int writeDataInt(int index, int value) {

        data[index++] = (byte) (value >> 24 & 0xff);
        data[index++] = (byte) (value >> 16 & 0xff);
        data[index++] = (byte) (value >> 8 & 0xff);
        data[index++] = (byte) (value & 0xff);
        return 4;
    }

    protected int writeDataShort(int index, int value) {
        data[index++] = (byte) (value >> 8 & 0xff);
        data[index++] = (byte) (value & 0xff);
        return 2;
    }

    protected int writeDataByte(int index, int value) {
        data[index++] = (byte) (value & 0xff);
        return 1;
    }

    protected int writeDataNibbleHigh(int index, int value) {
        data[index++] |= (byte) (value & 0xf) << 4;
        return 1;
    }

    protected int writeDataNibbleLow(int index, int value) {
        data[index++] |= (byte) (value & 0xf);
        return 1;
    }

    protected int writeDataBit(int index, int bitIndex, boolean value) {
        data[index++] |= (byte) (value ? 1 : 0) << bitIndex;
        return 1;
    }

    protected int readDataInt(int index) {

        int value = (data[index] << 24) |
                (data[index + 1] << 16) |
                (data[index + 2] << 8) |
                (data[index + 3]& 0xff);
        return value;
    }

    protected long readDataUnsignedInt(int index) {

        long value = 0x0FFFFFFFF&(((data[index]&0xff) << 24) |
                ((data[index + 1]&0xff) << 16) |
                ((data[index + 2]&0xff) << 8) |
                ((data[index + 3]&0xff)));
        return value;
    }

    protected int readDataShort(int index) {
        int value = 0xffff&((data[index] << 8) |
                (data[index + 1] & 0xff));
        return value;
    }

    protected int readDataByte(int index) {
        int value = 0xff&((data[index] & 0xff));
        return value;
    }
    protected boolean readDataBool(int index) {
        boolean value = readDataByte(index)>0;
        return value;
    }

    protected int readDataInt() {

        int retval = readDataInt(readIndex);
        readIndex+=4;
        return retval;
    }

    protected long readDataUnsignedInt() {
        long retval = readDataUnsignedInt(readIndex);
        readIndex+=4;
        return retval;
    }

    protected int readDataShort() {
        int retval = readDataShort(readIndex);
        readIndex+=2;
        return retval;
    }

    protected int readDataByte() {
        int retval = readDataByte(readIndex);
        readIndex+=1;
        return retval;

    }
    protected boolean readDataBool() {
        boolean retval = readDataBool(readIndex);
        readIndex+=1;
        return retval;

    }

 

    /**
     * @return the syncBytes
     */
    public char[] getSyncBytes() {
        return syncBytes;
    }

    /**
     * @param syncBytes the syncBytes to set
     */
    public void setSyncBytes(char[] syncBytes) {
        this.syncBytes = syncBytes;
    }

    /**
     * @return the fromIP
     */
    public int getFromIP() {
        return fromIP;
    }

    /**
     * @param fromIP the fromIP to set
     */
    public void setFromIP(int fromIP) {
        this.fromIP = fromIP;
    }

    /**
     * @return the fromPort
     */
    public int getFromPort() {
        return fromPort;
    }

    /**
     * @param fromPort the fromPort to set
     */
    public void setFromPort(int fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * @return the length
     */
    public int getWholeLength() {   //jch added function
        return length;
    }

    public int getBodyLength() {    //jch added function
        return length - HeaderSize - 1;
    }
    
    /**
     * @param length the length to set
     */
    public void setWholeLength(int length) {    //jch added function
        this.length = length;
    }
    
    public void setBodyLength(int length) {
        this.length = length + HeaderSize + 1;  //jch added function
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
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the checksum
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    /**
     * @param parseIndex the parseIndex to set
     */
    public void setParseIndex(int parseIndex) {
        this.parseIndex = parseIndex;
    }

    /**
     * @param aSYNC_BYTE the SYNC_BYTE to set
     */
    public static void setSYNC_BYTE(char aSYNC_BYTE) {
        SYNC_BYTE = aSYNC_BYTE;
    }

    /**
     * @param aHeaderSize the HeaderSize to set
     */
    public static void setHeaderSize(int aHeaderSize) {
        HeaderSize = aHeaderSize;
    }

    /**
     * @param aChecksumSize the ChecksumSize to set
     */
    public static void setChecksumSize(int aChecksumSize) {
        ChecksumSize = aChecksumSize;
    }

    /**
     * @param validMessage the validMessage to set
     */
    public void setValidMessage(boolean validMessage) {
        this.validMessage = validMessage;
    }

    /**
     * @return the readIndex
     */
    public int getReadIndex() {
        return readIndex;
    }

    /**
     * @param readIndex the readIndex to set
     */
    public void setReadIndex(int readIndex) {
        this.readIndex = readIndex;
    }



}
