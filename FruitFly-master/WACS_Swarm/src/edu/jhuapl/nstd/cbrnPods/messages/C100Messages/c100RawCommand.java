/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.C100Messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodCommand;

/**
 *
 * @author humphjc1
 */
public class c100RawCommand extends cbrnPodCommand {

    private String commandString;

    public c100RawCommand() {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_RAW_CMD, 0);
    }
    
    /**
     * 
     * @param command New raw command to send to sensor
     */
    public c100RawCommand(String command) {
        super(cbrnSensorTypes.SENSOR_C100, cbrnPodCommand.C100_RAW_CMD, 0);
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
