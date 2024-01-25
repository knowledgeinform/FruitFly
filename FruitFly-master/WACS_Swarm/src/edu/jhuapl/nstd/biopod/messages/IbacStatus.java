/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 * Get the version, etc.
 * @author southmk1
 */
public class IbacStatus extends IbacCommand {

    public IbacStatus() {
        super(BioPodMessage.IBAC_STATUS, 0);
    }
}
