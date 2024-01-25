/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

import javax.swing.text.AbstractDocument.LeafElement;

/**
 * Send a raw string directly as a serial passthrough to device
 * @author southmk1
 */
public class IbacRaw extends IbacCommand {

    private String commandString;

    public IbacRaw() {
        super(BioPodMessage.IBAC_AUTO_COLLECT, 0);
    }
    @Override
    public byte[] toByteArray()
    {
        for(int i=0; i<commandString.length();i++){
            writeDataInt(1+i, commandString.charAt(i));
        }
        setWholeLength(getWholeLength()+commandString.length());

        return super.toByteArray();
    }

    /**
     * @return the commandString
     */
    public String getCommandString() {
        return commandString;
    }

    /**
     * @param commandString the commandString to set
     */
    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }
    
}
