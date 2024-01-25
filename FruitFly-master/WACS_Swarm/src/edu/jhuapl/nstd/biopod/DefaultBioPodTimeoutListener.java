/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod;

import edu.jhuapl.nstd.biopod.messages.BioPodMessage;
import java.util.Date;

/**
 *
 * @author southmk1
 */
public class DefaultBioPodTimeoutListener implements BioPodMessageTimeoutListener
{
    private long timeoutMs;
    private BioPodMessageListener listener;
    public DefaultBioPodTimeoutListener(BioPodMessageListener listener, int timeoutMs)
    {
        this.timeoutMs = new Date().getTime()+timeoutMs;
        this.listener = listener;
    }

    @Override
    public boolean isTimedOut() {
        return new Date().getTime()>timeoutMs;
    }

    @Override
    public void timedOut() {
        //TODO log
        //Ignore messages that are timed out.
    }

    @Override
    public void handleMessage(BioPodMessage m)
    {
       handleMessage(m);
    }

}
