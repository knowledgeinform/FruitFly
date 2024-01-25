/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages;

import java.util.Arrays;

/**
 *
 * @author humphjc1
 */
public abstract class cbrnPodBytes {

    //bytes 0-2
    protected char[] syncBytes = new char[3];
    //byte 3 
    protected int sensorType; 
    //bytes 4,5,6,7
    protected int fromIP;
    //byte 8,9
    protected int fromPort;
    
    //bytes 10,11 
    protected int length;
    
    
    //byte HeaderSize..length-2
    protected byte[] data;
    //byte length-1
    protected int checksum;
    
    private int readIndex;
    
    protected int HeaderSize;
    protected static int ChecksumSize = 1;
    
    
    protected int pre_toByteArray(byte[] byteArray)
    {
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
        byteArray[indx++] = (byte) (length >> 8);
        byteArray[indx++] = (byte) length;
        
        return indx;
    }
    
    public abstract byte[] toByteArray();
    
    public abstract String toLogString ();
    
    protected String preToLogString ()
    {
        String retVal = "";
        
        for (int i = 0; i < syncBytes.length; i ++)
            retVal += syncBytes[i] + "\t";
        
        retVal += sensorType + "\t";
        retVal += fromIP + "\t";
        retVal += fromPort + "\t";
        retVal += length + "\t";
        
        return retVal;
    }
    
    protected String postToLogString ()
    {
        String retVal = "";
        
        for (int i = 0; i < data.length; i ++)
            retVal += data[i] + "\t";
        
        retVal += checksum + "\t";
        return retVal;
    }
    
    protected void post_toByteArray(byte[] byteArray, int indx) 
    {
        int bodyLength = byteArray.length - indx - ChecksumSize; 
        //Body
        for (int i = 0; i < bodyLength; i++) {      
            byteArray[indx++] = data[i];
        }

        //Checksum
        byteArray[byteArray.length - ChecksumSize] = calculateChecksum(Arrays.copyOfRange(byteArray, 0, byteArray.length - ChecksumSize));
    }

    private byte calculateChecksum(byte[] bytes) {
        int chk = 0;
        for (byte b : bytes) {
            chk = (chk + b);
        }
        return (byte) (chk);
    }
    
    
    
    /**
     * @param fromIP the fromIP to set
     */
    public void setFromIP(int fromIP) {
        this.fromIP = fromIP;
    }
    
    /**
     * @return fromIP the fromIP
     */
    public int getFromIP() {
        return fromIP;
    }

    /**
     * @param fromPort the fromPort to set
     */
    public void setFromPort(int fromPort) {
        this.fromPort = fromPort;
    }
    
    /**
     * @return the fromPort
     */
    public int getFromPort() {
        return fromPort;
    }
    
    /**
     * @return the length
     */
    public int getWholeLength() {   
        return length;
    }

    public int getBodyLength() {    
        return length - HeaderSize - 1;
    }
    
    /**
     * @param length the length to set
     */
    public void setWholeLength(int length) {    
        this.length = length;
    }
    
    public void setBodyLength(int length) {
        this.length = length + HeaderSize + 1;  
    }
    
    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
    
    /**
     * @return the checksum 
     */
    public int getChecksum() {
        return checksum;
    }
    
    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void resetDataLength (int len)
    {
        this.data = new byte[len];
        this.length = len + HeaderSize + ChecksumSize;
    }
    
    public void setSensorType (int type)
    {
        sensorType = type;
    }
    
    public int getSensorType ()
    {
        return sensorType;
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
    
    protected float readDataFloat(int index) 
    {
        int bits = 0;
        int i = 0;
        for (int shifter = 3; shifter >= 0; shifter--) 
        {
            bits |= ((int) data[i+index] & 0xFF) << (shifter * 8);
            i++;
        }
 
        return Float.intBitsToFloat(bits);
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
    protected float readDataFloat() {
        float retval = readDataFloat(readIndex);
        readIndex+=4;
        return retval;

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
    
    protected int writeDataFloat(int index, float value) 
    {
        int i = Float.floatToRawIntBits(value);
        byte[] bytes = new byte[4];
        for (int j = 0; j < 4; j++) {
            int offset = (bytes.length - 1 - j) * 8;
            bytes[j] = (byte) ((i >>> offset) & 0xFF);
        }
        
        data[index++] = bytes[0];
        data[index++] = bytes[1];
        data[index++] = bytes[2];
        data[index++] = bytes[3];
        return 4;
    }
    
}
