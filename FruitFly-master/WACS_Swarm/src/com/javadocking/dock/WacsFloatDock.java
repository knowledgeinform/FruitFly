package com.javadocking.dock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.javadocking.DockingManager;
import com.javadocking.dock.factory.DockFactory;
import com.javadocking.dock.factory.SplitDockFactory;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.event.ChildDockEvent;
import com.javadocking.util.DockingUtil;
import com.javadocking.util.SwingUtil;
import java.awt.event.ActionEvent;
import javax.swing.Action;

/**
 * <p>
 * This special dock contains all the floating dockables. It is a composite dock. The child docks are put in a 
 * dialog (javaw.swing.JDialog) and are floating on the screen.
 * </p>
 * <p>
 * Information on using float docks is in 
 * <a href="http://www.javadocking.com/developerguide/compositedock.html#FloatDock" target="_blank">How to Use Composite Docks</a> in 
 * <i>The Sanaware Developer Guide</i>.
 * </p>
 * <p>
 * A dockable can be added to this dock if 
 * <ul>
 * <li>the dockable has {@link DockingMode#FLOAT} as one of its possible docking modes.</li>
 * <li>the child dock factory can create a child dock for the dockable.</li>
 * </ul>
 * When a dockable is added, a child dock is created with the 'childDockFactory'. The dockable is added to 
 * the child dock. The child dock is put in a floating window.
 * </p>
 * <p>
 * There is an order for the floating child docks. Children with a lower index are on top of children with a higher
 * index.
 * </p>
 * <p>
 * The parent dock of the float dock is always null.
 * The float dock has a owner window. This is the window that that will be the owner of the dialogs that contain
 * the child docks.
 * </p>
 * 
 * @author Heidi Rakels.
 */
public class WacsFloatDock extends FloatDock
{
    /**
     * MODIFIED FOR WACS: If a tab dock is closed, then return it to the main tab dock
     */
    SplitDock m_MainSplitDock;

    
    
    /**
    * Constructs a float dock with no owner and a {@link SplitDockFactory}
    * as factory for the child docks.
    * 
    * MODIFIED FOR WACS: Use local window closing listener, not the parent one
    */
    public WacsFloatDock()
    {
        super ();
        windowClosingListener = new WindowClosingListener();
    }

    /**
    * Constructs a float dock with the given window as owner for the child dock windows 
    * and a {@link SplitDockFactory} as factory for creating child docks.	
    * 
    * MODIFIED FOR WACS: Use local window closing listener, not the parent one
    * 
    * @param	owner				The window that owns the floating windows created by this dock.
    */
    public WacsFloatDock(Window owner)
    {
        super (owner);
        windowClosingListener = new WindowClosingListener();
    }

    /**
    * Constructs a float dock with the given window as owner for the child dock windows 
    * and the given factory for the child docks.
    * 
    * MODIFIED FOR WACS: Use local window closing listener, not the parent one
    * 
    * @param	owner				The window that owns the floating windows created by this dock.
    * @param	childDockFactory	The factory for creating child docks.	
    */
    public WacsFloatDock(Window owner, DockFactory childDockFactory)
    {
        super (owner, childDockFactory);
        windowClosingListener = new WindowClosingListener();
    }
    
    
	
    /**
    * <p>
    * Adds the given dock as child dock to this dock. 
    * </p>
    * <p>
    * The dock is put in a dialog. This dialog is created with the method
    * {@link com.javadocking.component.SwComponentFactory#createJDialog(Window)} of 
    * the component factory of the {@link com.javadocking.DockingManager}.
    * </p>
    * <p>
    * There is a border set around the dock. This border is created with the method
    * {@link com.javadocking.component.SwComponentFactory#createFloatingBorder()} of 
    * the component factory of the docking manager.
    * </p>
    * <p>
    * The floating window is put at the given location. The window will have the given size.
    * If this size is null, then the preferred size is taken.
    * </p>
    * 
    * MODIFIED FOR WACS:  Added decoration to floating windows, but only if not tabDock (a bit of a hack)
    * 
    * @param 	dock						The child dock that is added to this float dock in a floating dialog.
    * @param 	location					The location for the dialog.
    * @param 	size						The size for the dialog. This may be null. In that case the preferred 
    * 										size is taken.
    */
    public void addChildDock(Dock dock, Point location, Dimension size) 
    {

        // Inform the listeners.
        dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, null, this, dock));

        // Calculate the location for the floating window.
        Point point = new Point(location.x, location.y);
        checkFloatingWindowLocation(point);

        // Create a panel for the dock.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(DockingManager.getComponentFactory().createFloatingBorder());
        panel.add((Component)dock, BorderLayout.CENTER);

        // Create the floating window.
        Window dialog = DockingManager.getComponentFactory().createWindow(this.owner);
        if (dialog instanceof JDialog)
        {
             ((JDialog)dialog).setContentPane(panel);
        }
        else
        {
             ((JFrame)dialog).setContentPane(panel);
        }

        // Add the listeners.
        dialog.addWindowFocusListener(new FloatDock.MoveToFrontListener(dock));
        Iterator iterator = windowFocusListeners.iterator();
        while(iterator.hasNext())
        {
                dialog.addWindowFocusListener((WindowFocusListener)iterator.next());
        }
        dialog.addWindowListener(windowClosingListener);

        // The size and location.
        if (size != null) 
        {
                dialog.setSize(size.width, size.height);
        } 
        else
        {
                dialog.pack();
        }
        dialog.setLocation(point.x - dialog.getInsets().left, point.y - dialog.getInsets().top);
        childDockWindows.put(dock, dialog);

        // Add the child dock.
        dock.setParentDock(this);
        childDocks.add(0, dock);

        // Inform the listeners.
        dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, dock));

        dialog.setVisible(true);
    }

    public void setMainSplitDock(SplitDock dock) 
    {
        m_MainSplitDock = dock;
    }

    /**
     * This listener listens when a dialog that contains a child dock closes.
     * It removes then the child from the float dock.
     * 
     * MODIFIED FOR WACS: When closing this window, close it as if the docking mechanisms did it, rather than closing a window
     * 
     * @author Heidi Rakels.
     */
    private class WindowClosingListener extends FloatDock.WindowClosingListener
    {
        // Implementations of WindowListener.

        @Override
        public void windowClosing(WindowEvent windowEvent)
        {

            // Get the window.
            Container contentPane = SwingUtil.getContentPane(windowEvent.getWindow());

            // Get the dock in the window.
            Dock childDock = (Dock)contentPane.getComponent(0);

            // Remove it from the list with child docks.
            //childDocks.remove(childDock);
            //childDockWindows.remove(childDock);

            // Get all the dockables in the dock tree.
            List childDockables = new ArrayList();
            DockingUtil.retrieveDockables(childDock, childDockables);

            // The dockables have no dock anymore.
            Iterator iterator = childDockables.iterator();
            while(iterator.hasNext())
            {
                Dockable dockable = (Dockable)iterator.next();
                if (dockable instanceof StateActionDockable)
                {
                    Action[][] allActions = ((StateActionDockable)dockable).getActions();
                    for (Action[] actionList : allActions)
                    {
                        for (Action action : actionList)
                        {
                            if (action instanceof DefaultDockableStateAction)
                            {
                                if (((DefaultDockableStateAction)action).getNewDockableState() == DockableState.CLOSED)
                                    ((DefaultDockableStateAction)action).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
                            }
                        }
                    }
                }
                else
                {
                    //Handle special case for tabs, since we can't get them back if we lose them.  Try to do dock them
                    //to the left of the main window in some graceful way when closed.  Find a tab dock over there if at all possible
                    //and reuse it.
                    if (childDock instanceof TabDock && m_MainSplitDock != null)
                    {
                        childDocks.remove(childDock);
                        childDockWindows.remove(childDock);
                        dockable.setState(DockableState.CLOSED, null);

                        childDock = new WacsTabDock();
                        ((TabDock)childDock).addDockable(dockable, new Position (Position.RIGHT));
                        Dock kidDock = m_MainSplitDock;
                        while (true)
                        {
                            while (kidDock != null && (kidDock instanceof SplitDock) && kidDock.isFull())
                                kidDock = ((SplitDock)kidDock).getChildDock(0);

                            if (kidDock != null && kidDock instanceof SplitDock)
                            {
                                if (!kidDock.isEmpty())
                                {
                                    Dock finalChildDock = ((SplitDock)kidDock).getChildDock(0);
                                    if (finalChildDock instanceof TabDock)
                                    {
                                        ((TabDock)finalChildDock).addDockable(dockable, new Position (Position.RIGHT));
                                        break;
                                    }
                                    else
                                    {
                                        kidDock = ((SplitDock)kidDock).getChildDock(0);
                                    }
                                }
                            }
                            else
                            {
                                m_MainSplitDock.addChildDock(childDock, new Position (Position.LEFT));
                                break;
                            }

                        }
                    }   
                }
            }

        }

        public void windowDeactivated(WindowEvent windowEvent) 
        {
                // Do nothing.
        }
        public void windowDeiconified(WindowEvent windowEvent) 
        {
                // Do nothing.
        }
        public void windowIconified(WindowEvent windowEvent) 
        {
                // Do nothing.
        }
        public void windowOpened(WindowEvent windowEvent) 
        {
                // Do nothing.
        }
        public void windowActivated(WindowEvent windowEvent) 
        {
                // Do nothing.
        }
        public void windowClosed(WindowEvent windowEvent) 
        {
                // Do nothing.
        }

    }
}