/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dock.Dock;
import com.javadocking.dock.WacsLineDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SingleDock;
import com.javadocking.dock.SplitDock;
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author humphjc1
 */
public class BottomDockManager extends WacsDockManager
{
    WacsLineDock m_Dock;
    JPanel m_GraphToggleButtonPanel;
    JPanel m_GraphSinglePanel;
    JFrame m_ParentFrame;
    boolean m_FinalizeDock;
    
    public BottomDockManager ()
    {
        m_Dock = new WacsLineDock(WacsLineDock.ORIENTATION_HORIZONTAL, false);
        m_Dock.setRealSizeRectangle(false);
        
        m_GraphToggleButtonPanel = null;
        m_ParentFrame = null;
        m_FinalizeDock = false;
    }
    
    @Override
    public Dock getDock()
    {
        return m_Dock;
    }
    
    @Override
    public void addDockableComponent (Component panel, boolean defaultShow, final String displayItemText)
    {
        DraggableTitleOverlayPanel xPanel = new DraggableTitleOverlayPanel();
        xPanel.addPanel (displayItemText, panel);
        panel = xPanel;
        
        int[] states = {DockableState.NORMAL, DockableState.CLOSED};
        DefaultDockable defDockable = new DefaultDockable(displayItemText + "_dockableId", panel, null, null, DockingMode.HORIZONTAL_LINE | DockingMode.FLOAT | DockingMode.BOTTOM);
        Dockable wrapper = new StateActionDockable(defDockable, new DefaultDockableStateActionFactory(), new int[0]);
        final StateActionDockable dockable = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);
        
        xPanel.addCloseAction (new DefaultDockableStateAction(dockable, DockableState.CLOSED));
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
            
            final DockableToggleButton newButton = new DockableToggleButton(dockable, displayItemText, doAdd?defaultShow:dockable.getState()==DockableState.NORMAL);
            m_GraphToggleButtonPanel.add(newButton);

            if (doAdd)
            {
                m_Dock.addDockable(dockable, new Position (Position.RIGHT));
                if (!defaultShow)
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

                                newButton.setSelected(true);
                                newButton.doClick();
                                newButton.setSelected(false);

                                break;
                            }
                        }
                    }.start();
                }
            }    
        }
    }
    
    public void finalizeDock ()
    {
        m_FinalizeDock = true;
    }

    public Dockable getGraphSingleDockable() 
    {
        final JPanel bottomBar = new JPanel();
        bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.X_AXIS));
        bottomBar.setPreferredSize(new Dimension (100, 30));
        bottomBar.setMinimumSize(bottomBar.getPreferredSize());
        
        bottomBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        m_GraphSinglePanel = new JPanel();
        m_GraphSinglePanel.setLayout(new BoxLayout(m_GraphSinglePanel, BoxLayout.Y_AXIS));
        m_GraphSinglePanel.add(bottomBar);
        m_GraphSinglePanel.validate();
        Dockable graphSingleDockable = new DefaultDockable("graphSingleDockableId", m_GraphSinglePanel, null, null, DockingMode.SINGLE);
        m_GraphToggleButtonPanel = bottomBar;
        return graphSingleDockable;
    }
    
    public SingleDock getGraphSingleDock(Dockable graphSingleDockable, JFrame parentFrame) 
    {
        SingleDock graphSingleDock = new SingleDock();
        graphSingleDock.addDockable(graphSingleDockable, new Position (Position.CENTER));
        
        m_ParentFrame = parentFrame;
        return graphSingleDock;
    }
    
    public void addSplitDockToSinglePanel (SplitDock graphSplitDock)
    {
        graphSplitDock.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_GraphSinglePanel.add(graphSplitDock, 0);
        m_GraphSinglePanel.validate();
    }
    
    
    /**
    * A check box menu item to add or remove the dockable.
    */
   private class DockableToggleButton extends JToggleButton
   {
        public DockableToggleButton(Dockable dockable, String text, boolean show)
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
        private JToggleButton dockableToggleButton;

        public DockableMediator(Dockable dockable, JToggleButton dockableToggleButton) 
        {
             this.dockable = dockable;
             this.dockableToggleButton = dockableToggleButton;
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
                     dockableToggleButton.removeItemListener(this);
                     dockableToggleButton.setSelected(true);
                     dockableToggleButton.addItemListener(this);	
             }
             else
             {
                     dockableToggleButton.removeItemListener(this);
                     dockableToggleButton.setSelected(false);
                     dockableToggleButton.addItemListener(this);
             }
        }

        @Override
        public void dockingWillChange(DockingEvent dockingEvent) 
        {
            //Do nothing
        }
   }
}
