/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages;

import java.util.EventListener;

/**
 *
 * @author humphjc1
 */
public interface cbrnPodMessageListener extends EventListener
{
    public abstract void handleMessage(cbrnPodMsg m);
}
