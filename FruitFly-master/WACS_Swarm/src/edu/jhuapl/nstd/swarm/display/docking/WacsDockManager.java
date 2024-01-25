/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dock.Dock;
import com.javadocking.dockable.Dockable;
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 *
 * @author humphjc1
 */
public abstract class WacsDockManager 
{
    protected LinkedList <Object> m_DockableList;
    protected LinkedList <Boolean> m_DockableShowList;
    protected LinkedList <String> m_DockableTextList;
    
    public WacsDockManager ()
    {
        m_DockableList = new LinkedList<Object>();
        m_DockableShowList = new LinkedList<Boolean>();
        m_DockableTextList = new LinkedList<String>();
    }
    
    public void addScrollablePanel (JPanel panel, boolean defaultShow, final String displayItemText)
    {
        DraggableJScrollPane aglScrollPane = new DraggableJScrollPane(panel);
        addDockableComponent(aglScrollPane, defaultShow, displayItemText);
    }
    
    public void addDraggablePanel (JPanel panel, boolean defaultShow, final String displayItemText)
    {
        DraggableJPanel draggable = new DraggableJPanel (panel);
        addDockableComponent(draggable, defaultShow, displayItemText);
    }
    
    protected void addDockableComponent (Dockable dockable, boolean defaultShow, String displayText)
    {
        m_DockableList.add(dockable);
        m_DockableShowList.add(defaultShow);
        m_DockableTextList.add(displayText);
    }
    
    public LinkedList<Object> getDockableList ()
    {
        return m_DockableList;
    }
    
    public abstract Dock getDock();
    public abstract void addDockableComponent (Component panel, boolean defaultShow, final String displayItemText);
    public abstract void addDockablesToDocks(boolean doAdd, Component toggleViewComponent);
}
