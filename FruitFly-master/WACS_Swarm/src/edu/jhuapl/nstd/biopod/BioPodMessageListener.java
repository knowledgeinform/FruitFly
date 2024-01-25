/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod;

import edu.jhuapl.nstd.biopod.messages.BioPodMessage;
import java.util.EventListener;

/**
 *
 * @author southmk1
 */
public interface BioPodMessageListener extends EventListener{
    public abstract void handleMessage(BioPodMessage m);
}
