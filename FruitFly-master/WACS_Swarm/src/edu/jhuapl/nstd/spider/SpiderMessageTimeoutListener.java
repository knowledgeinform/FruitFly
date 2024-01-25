/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider;

/**
 *
 * @author southmk1
 */
public interface SpiderMessageTimeoutListener extends SpiderMessageListener{

    public boolean isTimedOut();
    public void timedOut();
}
