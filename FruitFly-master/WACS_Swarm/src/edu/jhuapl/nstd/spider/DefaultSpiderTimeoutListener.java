/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider;

import edu.jhuapl.nstd.spider.messages.SpiderMessage;
import java.util.Date;

/**
 *
 * @author southmk1
 */
public class DefaultSpiderTimeoutListener implements SpiderMessageTimeoutListener{
    private long timeoutMs;
    private SpiderMessageListener listener;
    public DefaultSpiderTimeoutListener(SpiderMessageListener listener, int timeoutMs)
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
    public void handleMessage(SpiderMessage m) {
       handleMessage(m);
    }

}
