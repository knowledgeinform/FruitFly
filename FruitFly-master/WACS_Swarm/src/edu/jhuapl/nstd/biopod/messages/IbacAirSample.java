/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Trigger an air sample. unit will respond with a $trace,
 * which the uc will send back as a particle count message
 * @author southmk1
 */
public class IbacAirSample extends IbacCommand {

    public IbacAirSample() {
        super(BioPodMessage.IBAC_AIR_SAMPLE,1 );
    }
}
