/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
     *The alarm latch state is asserted when
    a biological particle alarm is triggered
    and continues to be asserted until
    cleared by the user. This allows for an
    alarm to be detected in the event
    communication is lost during an alarm
    event
 * @author southmk1
 */
public class IbacClearAlarm extends IbacCommand {
    public IbacClearAlarm() {
        super(BioPodMessage.IBAC_CLEAR_ALARM, 0);
    }
}
