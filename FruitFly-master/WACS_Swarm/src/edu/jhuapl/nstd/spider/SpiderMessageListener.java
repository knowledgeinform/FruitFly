/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider;

import edu.jhuapl.nstd.spider.messages.SpiderMessage;
import java.util.EventListener;

/**
 *
 * @author southmk1
 */
public interface SpiderMessageListener extends EventListener{
    public abstract void handleMessage(SpiderMessage m);
}
