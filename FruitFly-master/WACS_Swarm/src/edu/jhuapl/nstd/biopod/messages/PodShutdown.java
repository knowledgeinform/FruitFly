/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.biopod.messages;

/**
 *
 * @author southmk1
 */
public class PodShutdown extends BioPodMessage {

    public PodShutdown() {
        super(BioPodMessage.SHUTDOWN, 0);
        setSyncBytes(new char[]{'~','~','~'});
    }
}
