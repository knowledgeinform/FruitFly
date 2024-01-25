/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dock.Dock;
import com.javadocking.dock.WacsLineDock;
import com.javadocking.dock.Position;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 *
 * @author humphjc1
 */
public class RightSideDockManager extends WacsDockManager
{
    WacsLineDock m_Dock;
    boolean m_FinalizeDock;
    
    
    public RightSideDockManager ()
    {
        m_Dock = new WacsLineDock(WacsLineDock.ORIENTATION_VERTICAL, false);
        m_Dock.setRealSizeRectangle(false);
        m_FinalizeDock = false;
    }
    
    @Override
    public Dock getDock()
    {
        return m_Dock;
    }
    
    public void addMenuSeparator ()
    {
        JSeparator sep = new JSeparator();
        m_DockableList.add(sep);
        m_DockableShowList.add(true);
        m_DockableTextList.add("sep");
    }
    
    @Override
    public void addDockableComponent (Component panel, boolean defaultShow, String displayItemText)
    {
        DraggableTitleOverlayPanel xPanel = new DraggableTitleOverlayPanel();
        xPanel.addPanel (displayItemText, panel);
        panel = xPanel;
        
        int[] states = {DockableState.NORMAL, DockableState.CLOSED};
        DefaultDockable defDockable = new DefaultDockable(displayItemText + "_dockableId", panel, null, null, DockingMode.VERTICAL_LINE | DockingMode.FLOAT | DockingMode.RIGHT);
        Dockable wrapper = new StateActionDockable(defDockable, new DefaultDockableStateActionFactory(), new int[0]);
        final StateActionDockable dockable = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);
        
        xPanel.addCloseAction (new DefaultDockableStateAction(dockable, DockableState.CLOSED));
        addDockableComponent(dockable, defaultShow, displayItemText);
    }
    
    public void finalizeDock ()
    {
        m_FinalizeDock = true;
    }

    @Override
    public void addDockablesToDocks(boolean doAdd, Component toggleViewComponent) 
    {
        for (int i = 0; i < m_DockableList.size(); i ++)
        {
            Object obj = m_DockableList.get (i);
            if (!(obj instanceof Dockable))
            {
                if (toggleViewComponent != null && toggleViewComponent instanceof JMenu && obj instanceof JSeparator)
                {
                    JMenu menu = (JMenu)toggleViewComponent;
                    menu.add((JSeparator)obj);
                }
                continue;
            }
            
            Dockable dockable = (Dockable)obj;
            boolean defaultShow = m_DockableShowList.get(i);
            String displayItemText = m_DockableTextList.get(i);
            
            final DockableMenuItem menuItem;

            if (toggleViewComponent != null && toggleViewComponent instanceof JMenu)
            {
                JMenu menu = (JMenu)toggleViewComponent;
                menuItem = new DockableMenuItem(dockable, displayItemText, doAdd?defaultShow:dockable.getState()==DockableState.NORMAL);
                menu.add(menuItem);
            }
            else
            {
                menuItem = null;
            }

            if (doAdd)
            {
                m_Dock.addDockable(dockable, new Position (Position.BOTTOM));
                if (!defaultShow && menuItem != null)
                {
                    new Thread () {
                        public void run ()
                        {
                            while (true)
                            {
                                if (!m_FinalizeDock)
                                {
                                    try {
                                        Thread.sleep (100);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(RightSideDockManager.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    continue;
                                }

                                menuItem.setSelected(true);
                                menuItem.doClick();
                                menuItem.setSelected(false);

                                break;
                            }
                        }
                    }.start();
                }
            }
        }
    }
    
    
    
    
    /**
    * A check box menu item to add or remove the dockable.
    */
   private class DockableMenuItem extends JCheckBoxMenuItem
   {
        public DockableMenuItem(Dockable dockable, String text, boolean show)
        {
            super(text, show);
            DockableMediator dockableMediator = new DockableMediator(dockable, this);
            dockable.addDockingListener(dockableMediator);
            addItemListener(dockableMediator);
        }
   }
   
   /**
    * A listener that listens when menu items with dockables are selected and deselected.
    * It also listens when dockables are closed or docked.
    */
   private class DockableMediator implements ItemListener, DockingListener
   {

        private Dockable dockable;
        private Action closeAction;
        private Action restoreAction;
        private JMenuItem dockableMenuItem;

        public DockableMediator(Dockable dockable, JMenuItem dockableMenuItem) 
        {
             this.dockable = dockable;
             this.dockableMenuItem = dockableMenuItem;
             closeAction = new DefaultDockableStateAction(dockable, DockableState.CLOSED);
             restoreAction = new DefaultDockableStateAction(dockable, DockableState.NORMAL);

        }

        @Override
        public void itemStateChanged(ItemEvent itemEvent)
        {
             dockable.removeDockingListener(this);
             if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
             {
                  // Close the dockable.
                  closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
             } 
             else 
             {
                 // Restore the dockable.
                 restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Restore"));
             }

             dockable.addDockingListener(this);
        }

        @Override
        public void dockingChanged(DockingEvent dockingEvent) 
        {
             if (dockingEvent.getDestinationDock() != null)
             {
                     dockableMenuItem.removeItemListener(this);
                     dockableMenuItem.setSelected(true);
                     dockableMenuItem.addItemListener(this);	
             }
             else
             {
                     dockableMenuItem.removeItemListener(this);
                     dockableMenuItem.setSelected(false);
                     dockableMenuItem.addItemListener(this);
             }
        }

        @Override
        public void dockingWillChange(DockingEvent dockingEvent) 
        {
            //Do nothing
        }
   }
}
