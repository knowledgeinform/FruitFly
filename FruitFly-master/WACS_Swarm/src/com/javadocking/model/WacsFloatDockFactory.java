/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javadocking.model;

import com.javadocking.dock.FloatDock;
import com.javadocking.dock.WacsFloatDock;
import java.awt.Window;

/**
 *
 * @author humphjc1
 */
public class WacsFloatDockFactory extends DefaultFloatDockFactory
{
    @Override
    public FloatDock createFloatDock(Window owner)
    {
        if (super.getChildDockFactory() != null)
        {
            return new WacsFloatDock(owner, super.getChildDockFactory());
        }
        else
        {
            return new WacsFloatDock(owner);
        }
    }
}
