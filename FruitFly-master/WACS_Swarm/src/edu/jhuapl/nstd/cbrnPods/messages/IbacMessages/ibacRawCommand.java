/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages.IbacMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 * Send a raw string directly as a serial passthrough to device
 * @author southmk1
 */
public class ibacRawCommand extends cbrnPodCommand {

    private String commandString;

    public ibacRawCommand() {
        super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_RAW_CMD, 0);
    }
    
    /**
     * 
     * @param command New raw command to send to sensor
     */
    public ibacRawCommand(String command) {
        super(cbrnSensorTypes.SENSOR_IBAC, cbrnPodCommand.IBAC_RAW_CMD, 0);
        setCommandString(command);
    }
    
    @Override
    public byte[] toByteArray()
    {
        resetDataLength (commandString.length() + 1);
        
        writeDataByte(0, (char)commandType);
        
        for(int i=0; i<commandString.length();i++){
            writeDataByte(1+i, commandString.charAt(i));
        }

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
