package com.javadocking.dock;

import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeListener;

import com.javadocking.DockingManager;
import com.javadocking.dockable.DockingMode;
import com.javadocking.drag.DragListener;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;
import edu.jhuapl.nstd.swarm.display.docking.TitleOverlayBar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JRootPane;

/**
 * <p>
 * This is a dock that can contain zero, one or multiple dockables. 
 * The dockables are organized in the tabs of a tabbed pane.
 * The tabbed pane of this dock is created with the component factory of the docking manager
 * ({@link com.javadocking.DockingManager#getComponentFactory()}) with the method
 * {@link com.javadocking.component.SwComponentFactory#createJTabbedPane()}.
 * </p>
 * <p>
 * Information on using tab docks is in 
 * <a href="http://www.javadocking.com/developerguide/leafdock.html#TabDock" target="_blank">How to Use Laef Docks</a> in 
 * <i>The Sanaware Developer Guide</i>.
 * </p>
 * <p>
 * This is a leaf dock. It cannot contain other docks.
 * </p>
 * <p>
 * When it contains no dockable it is empty. It is never full. 
 * </p>
 * <p>
 * A dockable can be docked in this dock if:
 * <ul>
 * <li>it has {@link DockingMode#TAB} as possible docking mode.</li>
 * <li>its content component is not null.</li>
 * </ul>
 * A composite dockable can also be docked in this dock if: 
 * <ul>
 * <li>all of its child dockables have {@link DockingMode#TAB} as possible docking mode.</li>
 * <li>all of its child dockables have a content component that is not null.</li>
 * </ul>
 * </p>
 * <p>
 * If the mouse is inside the priority rectangle, the dockable can be docked with priority (see {@link Priority#CAN_DOCK_WITH_PRIORITY}).
 * When the mouse is inside the panel of this dock, but outside the priority rectangle,
 * the dockable can be docked without priority (see {@link Priority#CAN_DOCK}).
 * The priority rectangle is a rectangle in the middle of the dock and retrieved with {@link #getPriorityRectangle(Rectangle)}.
 * </p>
 * <p>
 * The {@link com.javadocking.dock.Position} for dockables docked in this dock are one-dimensional.
 * The first position value of a child dockable is between 0 and the number of child dockables minus 1; 
 * it is the index of its tab.
 * </p>
 * 
 * MODIFIED FOR WACS to change layout, and resist adding components to dock until last minute
 * 
 * @author Heidi Rakels.
 */
public class WacsTabDock extends TabDock
{
	private boolean m_Initialized;
        
        private DragListener m_TabDockDragListener;
	
	/**
	 * Constructs a tab dock.
         * 
         * MODIFIED FOR WACS to change layout, and resist adding components to dock until last minute
	 */
	public WacsTabDock()
	{
		super ();
                
                this.removeAll();
            	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                m_TabDockDragListener = DockingManager.getDockDragListenerFactory().createDragListener(this);
		
                final WacsTabDock thisPtr = this;
                m_Initialized = false;
                this.addDockingListener(new DockingListener() {

                    @Override
                    public void dockingWillChange(DockingEvent de) 
                    {
                        if (!thisPtr.m_Initialized)
                        {
                            //Ensure tabdock has been initialized
                            thisPtr.initialize();
                        }
                    }

                    @Override
                    public void dockingChanged(DockingEvent de) {}
                });
                
                addMouseListener(m_TabDockDragListener);
                addMouseMotionListener(m_TabDockDragListener);
	}
        
        /**
         * Initialize panel after finding out who this dock's parent is, which affects whether the title bar has the close button
         */
        private void initialize ()
        {
            if (m_Initialized)
                return;
            
            boolean hideCloseBox;
            if (parentDock == null)
                hideCloseBox = true;
            else if (parentDock instanceof FloatDock)
                hideCloseBox = false;
            else
                hideCloseBox = true;
            
            TitleOverlayBar titleBar = new TitleOverlayBar(hideCloseBox);
            titleBar.setText("Controls");
            titleBar.addCloseAction(new Action() {

                @Override
                public Object getValue(String key) {
                    return null;
                }

                @Override
                public void putValue(String key, Object value) {
                }

                @Override
                public void setEnabled(boolean b) {
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                }

                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    if (getParentDock() instanceof FloatDock)
                    {
                        //This tabdock is floating, which means we should close the window and restore all tabs to the main dock
                        JRootPane rootPane = getRootPane();
                        Window container = (Window)rootPane.getParent();

                        WindowEvent wev = new WindowEvent(container, WindowEvent.WINDOW_CLOSING);
                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
                        container.dispose();;
                    }

                }
            });
            //Add title bar to panel
            add(titleBar);
            
            //Add tabbed pane no matter what
            add(tabbedPane);

            //Set this tab dock as initialized so we don't screw with the title bar again
            m_Initialized = true;
        }
        
        @Override
        public void setParentDock(CompositeDock parentDock)
	{
            super.setParentDock(parentDock);
            
            //We don't create the TabDock until we know who its parent is.  That way we can control how the title bar is added
            if (!m_Initialized)
                initialize ();	
	}
}
