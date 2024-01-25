/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class anacondaRawCommand extends cbrnPodCommand
{
    /**
     * Message type value to insert in the Anaconda message
     */
    private int type;
    
    /**
     * Data field length to insert in the Anaconda message
     */
    private int dataLength;
    
    /**
     * Datafield to insert in the Anaconda message
     */
    private byte[] dataField;
    

    public anacondaRawCommand()
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_RAW, 0);
    }
    
    /**
     * 
     * @param newType
     * @param newLength
     * @param newData
     */
    public anacondaRawCommand(int newType, int newLength, byte[] newData)
    {
         super(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodCommand.ANACONDA_RAW, 0);
         setType (newType);
         setDataLength (newLength);
         setDataField (newData);
    }

    @Override
    public byte[] toByteArray()
    {
        resetDataLength (dataField.length + 4);
        
        writeDataByte(0, (char)commandType);
        writeDataByte(1, (char)type);
        writeDataShort(2, (short)dataLength);
        
        for(int i=0; i<dataField.length;i++){
            writeDataByte(4+i, dataField[i]);
        }
        
        return super.toByteArray();
    }
    
    /**
     * 
     * @param newType Type of raw message to send to Anaconda
     */
    void setType (int newType)
    {
        type = newType;
    }
    
    /**
     * 
     * @return Type of raw message to send to Anaconda
     */
    int getType ()
    {
        return type;
    }
    
    /**
     * 
     * @param newDataLength New length of data field
     */
    void setDataLength (int newDataLength)
    {
        dataLength = newDataLength;
    }
    
    /**
     * 
     * @return Length of data field
     */
    int getDataLength ()
    {
        return dataLength;
    }
    
    /**
     * 
     * @param newData Data field to be sent directly to Anaconda
     */
    void setDataField (byte[] newData)
    {
        dataField = newData.clone();
    }
    
    /**
     * 
     * @return Data field to be sent directly to Anaconda
     */
    byte[] getDataField ()
    {
        return dataField.clone();
    }
    
}
