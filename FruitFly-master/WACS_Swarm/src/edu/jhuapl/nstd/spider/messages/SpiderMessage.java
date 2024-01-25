/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

import java.util.Arrays;

/**
 *
 * @author southmk1
 */
public class SpiderMessage {

    //byte 0
    private int syncByte;
    //byte 1 upper
    private int fromId;
    //byte 1 lower
    private int toId;
    //byte 2
    private int length;
    //byte 3 upper
    private int priority;
    //byte 3 lower
    private int messageType;
    //byte 4..length-2
    private byte[] data;
    //byte length-1
    private int checksum;
    private int parseIndex;
    protected static int SYNC_BYTE = 126;
    protected static int HeaderSize = 4;
    protected static int ChecksumSize = 1;
    private boolean validMessage = false;

    public SpiderMessage() {
        this.syncByte = SYNC_BYTE;
        this.length = HeaderSize + ChecksumSize;
        this.data = new byte[0];
    }

    protected SpiderMessage(int messageType, int length) {
        this.messageType = messageType;
        this.length = length;
        this.syncByte = SYNC_BYTE;

        this.data = new byte[length - HeaderSize - 1];
    }

    public void parseByte(byte b) {
        if (parseIndex < HeaderSize) {
            parseHeaderByte(parseIndex, b);
        } else if (parseIndex < length - 1) {
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
                if (b == SYNC_BYTE) {
                    syncByte = b;
                } else {
                    return;
                }
                break;
            case 1:
                fromId = (byte) (b >> 4 & 0x0f);
                toId = (byte) (b & 0x0f);
                break;
            case 2:
                length = b & 0xff;
                if (length - HeaderSize - 1 >= 0) {
                    data = new byte[length - HeaderSize - 1];
                } else {
                    clearMessage();
                    return;
                }
                break;
            case 3:
                priority = (byte) (b >> 6 & 0x03);
                messageType = b & 0x3F;
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
            checksum = b;
            validMessage = true;
        } else {
            clearMessage();
        }
    }

    protected void clearMessage() {
        syncByte = 0;
        checksum = 0;
        fromId = 0;
        toId = 0;
        length = 0;
        data = new byte[0];
        priority = 0;
        messageType = 0;
        validMessage = false;
        parseIndex = 0;
    }

    public boolean isValidMessage() {
        return validMessage;
    }

    public byte calculateChecksum(byte[] bytes) {
        int chk = 0;
        for (byte b : bytes) {
            chk = (chk + b);
        }
        return (byte) (-chk);
    }

    public boolean isInstanceOf(SpiderMessage m) {
        return (m.getMessageType() == this.getMessageType());
    }

    public void parseSpiderMessage(SpiderMessage m) {
        validMessage = m.isValidMessage();
        syncByte = m.getSyncByte();
        fromId = m.getFromId();
        toId = m.getToId();
        priority = m.getPriority();
        checksum = m.getChecksum();
        messageType = m.getMessageType();
        this.data = m.data.clone();
    }

    public byte[] toByteArray() {
        byte[] byteArray = new byte[length];

        //Header
        byteArray[0] = (byte) syncByte;
        byteArray[1] = (byte) (fromId << 4 | (toId & 0x0f));
        byteArray[2] = (byte) length;
        byteArray[3] = (byte) (priority << 6 | (messageType & 0x3f));

        //Body
        for (int i = 4; i < length - 1; i++) {
            byteArray[i] = data[i - 4];
        }

        //Checksum
        byteArray[length - 1] = calculateChecksum(Arrays.copyOfRange(byteArray, 0, length - 1));
        return byteArray;
    }

    protected int writeDataInt(int index, int value) {
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) (value >> 8 & 0xff);
        data[index++] = (byte) (value >> 16 & 0xff);
        data[index++] = (byte) (value >> 24 & 0xff);
        return 4;
    }

    protected int writeDataShort(int index, int value) {
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) (value >> 8 & 0xff);
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

        int value = (data[index + 3] << 24) |
                (data[index + 2] << 16) |
                (data[index + 1] << 8) |
                (data[index + 0]);
        return value;
    }

    protected long readDataUnsignedInt(int index) {

        long value = (data[index + 3] << 24) |
                (data[index + 2] << 16) |
                (data[index + 1] << 8) |
                (data[index + 0]);
        return value;
    }

    protected int readDataShort(int index) {
        int value = (data[index + 1] << 8) |
                (data[index] & 0xff);
        return value;
    }

    protected int readDataByte(int index) {
        int value = (data[index] & 0xff);
        return value;
    }

    protected int readDataNibbleHigh(int index) {
        int value = (data[index] >> 4 & 0x0f);
        return value;
    }

    protected int readDataNibbleLow(int index) {
        int value = (data[index] & 0x0f);
        return value;
    }

    protected boolean readDataBitAsBool(int index, int bitIndex) {
        int value = (data[index] >> bitIndex & 0x01);
        return value > 0;
    }

    protected int readDataBit(int index, int bitIndex) {
        int value = (data[index] >> bitIndex & 0x01);
        return value;
    }

    /**
     * @return the syncByte
     */
    public int getSyncByte() {
        return syncByte;
    }

    /**
     * @param syncByte the syncByte to set
     */
    public void setSyncByte(int syncByte) {
        this.syncByte = syncByte;
    }

    /**
     * @return the fromId
     */
    public int getFromId() {
        return fromId;
    }

    /**
     * @param fromId the fromId to set
     */
    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    /**
     * @return the toId
     */
    public int getToId() {
        return toId;
    }

    /**
     * @param toId the toId to set
     */
    public void setToId(int toId) {
        this.toId = toId;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
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
}
