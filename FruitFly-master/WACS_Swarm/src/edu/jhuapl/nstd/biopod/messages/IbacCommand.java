/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod.messages;

/**
 *
 * @author southmk1
 */
public class IbacCommand extends BioPodMessage{
    private int ibacMessageType;
    public IbacCommand(int messageType, int length)
    {
        super(BioPodMessage.IBAC_COMMAND_TYPE, length+1);
        ibacMessageType = messageType;
        sensorType = 1; //jch IBAC sensor type
        setSyncBytes(new char[]{'~','~','~'});
    }

     @Override
    public byte[] toByteArray()
    {
        writeDataByte(0, ibacMessageType);
        return super.toByteArray();
    }

}
