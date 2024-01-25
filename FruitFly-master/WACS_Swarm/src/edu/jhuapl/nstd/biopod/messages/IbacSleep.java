/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Put the unit in low power mode (sleep),
unit will wake up if it receives any
command over the serial port (except
another $sleep command). When it
wakes up, the unit will reset itself. The
unit will run through the initialization
process and output the $info,system
ready message.
 *
 * In practical experience, it doesn't always wake from sleep with some commands
 * $status and $air_sample seem to work.
 * @author southmk1
 */
public class IbacSleep extends IbacCommand {

    public IbacSleep() {
        super(BioPodMessage.IBAC_SLEEP, 0);
    }
    
}
