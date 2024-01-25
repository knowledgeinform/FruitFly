/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dock.Dock;
import com.javadocking.dock.Position;
import com.javadocking.dock.WacsTabDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import java.awt.Component;

/**
 *
 * @author humphjc1
 */
public class LeftSideDockManager extends WacsDockManager
{
    WacsTabDock m_Dock;
    
    public LeftSideDockManager ()
    {
        m_Dock = new WacsTabDock();
    }
    
    @Override
    public Dock getDock()
    {
        return m_Dock;
    }
    
    @Override
    public void addDockableComponent (Component panel, boolean defaultShow, final String displayItemText)
    {
        final Dockable dockable = new DefaultDockable(displayItemText + "_dockableId", panel, displayItemText, null, DockingMode.TAB | DockingMode.FLOAT | DockingMode.LEFT | DockingMode.TOP | DockingMode.BOTTOM);
        
        addDockableComponent(dockable, defaultShow, displayItemText);
    }    
    
    @Override
    public void addDockablesToDocks(boolean doAdd, Component toggleViewComponent) 
    {
        for (int i = 0; i < m_DockableList.size(); i ++)
        {
            Object obj = m_DockableList.get (i);
            if (!(obj instanceof Dockable))
            {
                continue;
            }
            
            Dockable dockable = (Dockable)obj;
            boolean defaultShow = m_DockableShowList.get(i);
            String displayItemText = m_DockableTextList.get(i);
            
            if (defaultShow && doAdd)
            {
                m_Dock.addDockable(dockable, new Position (Position.RIGHT));
            }
        }
    }
}