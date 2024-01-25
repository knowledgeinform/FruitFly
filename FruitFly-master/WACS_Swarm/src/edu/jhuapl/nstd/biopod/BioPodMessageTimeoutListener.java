/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod;

/**
 *
 * @author southmk1
 */
public interface BioPodMessageTimeoutListener extends BioPodMessageListener{

    public boolean isTimedOut();
    public void timedOut();
}
