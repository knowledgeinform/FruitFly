/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.wacs.sensors;

import java.util.EventListener;

/**
 *
 * @author humphjc1
 */
public interface C100MessageListener extends EventListener{
    public abstract void handleMessage(String msg);

}
