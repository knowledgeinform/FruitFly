package com.javadocking.drag;

import com.javadocking.DockingManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.javadocking.dock.Dock;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.CompositeDockable;
import com.javadocking.dockable.DefaultCompositeDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;
import com.javadocking.drag.painter.DefaultRectanglePainter;
import com.javadocking.drag.painter.DockableDragPainter;
import com.javadocking.drag.painter.SwDockableDragPainter;
import com.javadocking.util.DockingUtil;

/**
 * <p>
 * This is a class for dragging one dockable or all the dockables in a {@link com.javadocking.dock.CompositeTabDock}.
 * One dockable can be dragged by dragging the tab. All the dockables
 * can be dragged by dragging another part of the tabbed pane. A {@link com.javadocking.dockable.CompositeDockable}
 * is created with the dockables of the javax.swing.JTabbedPane.
 * </p>
 * <p>
 * The {@link com.javadocking.dock.Dock}s that are used in the application should inherit 
 * from the java.awt.Component class.
 * </p>
 * <p>
 * With this class a tab of the javax.swing.JTabbedPane can also be dragged. This happens, when one tab is dragged,
 * and the mouse is over the header of the JTabbedPane.
 * 
 * MODIFIED FOR WACS to better handle dragging for WACS implementation
 * </p>
 * 
 * @author Heidi Rakels.
 */
public class WacsStaticTabDragger extends StaticTabDragger
{
	/**
	 * Constructs a dragger with a default painter for painting the dragged dockables: 
	 * a {@link SwDockableDragPainter} with a {@link DefaultRectanglePainter}.
	 */
	public WacsStaticTabDragger()
	{
		super ();
	}
	
	/**
	 * Constructs a dragger with the given painter for painting the dragged dockables.
	 * 
	 * @param	dockableDragPainter 		The painter for painting the dragged dockables.
	 */
	public WacsStaticTabDragger(DockableDragPainter dockableDragPainter)
	{
		super (dockableDragPainter);
	}

	
        /**
         * MODIFIED FOR WACS to better handle dragging tabs for WACS implementation
         * @param mouseEvent
         * @return 
         */
        @Override
        public boolean startDragging(MouseEvent mouseEvent) 
	{
		
		// Get the mouse position and the component of the mouse event. 
            
                //WACS: This item was renamed
		Component mouseComponentOrig = (Component)mouseEvent.getSource();
		int x = mouseEvent.getX();
		int y = mouseEvent.getY();

		// Reset the fields.
		reset();
                
                
                //WACS: This item was created to represent the tabbedPane component, which might differ from the TabDock component
                Component mouseComponentTabbedPane = mouseComponentOrig;
                if (mouseComponentTabbedPane instanceof TabDock)
                {
                    mouseComponentTabbedPane = ((TabDock)mouseComponentOrig).getTabbedPane();
                }
                

		// Initialize the fields for docking.

		// Is the mouse component a JTabbedPane?
                //WACS: This uses the newly created tabbedPane object, if possible
		if (mouseComponentTabbedPane instanceof JTabbedPane)
		{
			// Is the ancestor component a TabDock?
                        //WACS: This uses the newly created tabbedPane object, if possible
			Component ancestorComponent = (Component) SwingUtilities.getAncestorOfClass(Component.class, mouseComponentTabbedPane);
			if (ancestorComponent instanceof TabDock)
			{
				// Does the dock has dockables docked in it?
				TabDock tabDock = (TabDock) ancestorComponent;
				if (tabDock.getDockableCount() > 0)
				{
					// We can start dragging.
                                        //WACS: This uses the newly created tabbedPane object, if possible
					sourceTabbedPane = (JTabbedPane)mouseComponentTabbedPane;
					originDock = tabDock;
					
					// Calculate the dockable offset.
					dockableOffset.setLocation(x, y);
                                        dockableOffset = SwingUtilities.convertPoint(mouseComponentOrig, dockableOffset, sourceTabbedPane);
	
					// Get the selected tab and its dockable.
					oldTabIndex = sourceTabbedPane.indexAtLocation(dockableOffset.x, dockableOffset.y);
					if (oldTabIndex >= 0)
					{
						// One tab is selected. The dockable that is docked in the tab will be dragged.
						Component tabComponent = sourceTabbedPane.getComponentAt(oldTabIndex);
						draggedDockable = tabDock.retrieveDockableOfComponent(tabComponent);
						if (draggedDockable != null)
						{
							// Make sure the offset is not larger than the dockable size.
							Dimension size = draggedDockable.getContent().getPreferredSize();
							if (dockableOffset.x > size.getWidth())
							{
								dockableOffset.x = (int)(Math.round(size.getWidth()));
							}
							if (dockableOffset.y > size.getHeight())
							{
								dockableOffset.y = (int)(Math.round(size.getHeight()));
							}

							// We have a dockable to drag.
							return true;
						}
					}
					else
					{
						// No tab is selected.
						
						// Are there any tabs?
						if (sourceTabbedPane.getTabCount() <= 0)
						{
							return false;
						}
						
						// Check if the component of the selected dockable is not clicked.
						Component selectedComponent = sourceTabbedPane.getSelectedComponent();
						Point helpPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
                                                helpPoint = SwingUtilities.convertPoint(mouseComponentOrig, helpPoint, selectedComponent);
						if (selectedComponent.contains(helpPoint))
						{
							return false;
						}
						
						// A composite dockable is created with the dockables that are docked in the different tab.
						// This composite dockable is dragged.
						// Create the composite dockable.
						Dockable[] dockables = new Dockable[sourceTabbedPane.getTabCount()];
						for (int index = 0; index < sourceTabbedPane.getTabCount(); index++)
						{
							dockables[index] = tabDock.retrieveDockableOfComponent(sourceTabbedPane.getComponentAt(index));
							if (dockables[index] == null)
							{
								return false;
							}
						}
						draggedDockable = new DefaultCompositeDockable(dockables, sourceTabbedPane.getSelectedIndex());
						draggedDockable.setState(DockableState.NORMAL, originDock);
						
						// Make sure the offset is not larger than the dockable size.
						Dimension size = DockingUtil.getCompositeDockablePreferredSize((CompositeDockable)draggedDockable, DockingMode.TAB);
						if (dockableOffset.x > size.getWidth())
						{
							dockableOffset.x = (int)(Math.round(size.getWidth()));
						}
						if (dockableOffset.y > size.getHeight())
						{
							dockableOffset.y = (int)(Math.round(size.getHeight()));
						}

						// We have a dockable to drag.
						return true;
					}
				}
			}
		}
		
		// We can not drag.
		return false;
		
	}
        
        /**
	 * MODIFIED FOR WACS to better handle dragging tabs for WACS implementation.  Only affects
         * if we are moving tabs around since the added header space screws things up.
	 */
        @Override
	public void stopDragging(MouseEvent mouseEvent)
	{

            // Reset the old cursor.
            cursorManager.resetCursor();

            // Clear what we painted.
            clearPainting();

            // Are we dragging a tab?
            int tabIndex = tabDragged(mouseEvent);
            if (tabIndex >= 0)
            {

                    // Move the tabs.
                    if ((oldTabIndex >= 0) && (tabIndex != oldTabIndex))
                    {
                            Point locationInOriginDock = new Point(mouseEvent.getPoint().x, mouseEvent.getPoint().y);
                            //WACS: Look-up position in WacsTabDock, not the tabbedPane
                            locationInOriginDock = SwingUtilities.convertPoint((Component)originDock, locationInOriginDock, (Component)originDock);
                            if (!originDock.equals(draggedDockable.getDock())) {
                                    throw new IllegalStateException("The origin dock is not the parent of the dockable.");
                            }				
                            DockingManager.getDockingExecutor().changeDocking(draggedDockable, originDock, locationInOriginDock, new Point(0, 0));
                    }

                    // No dragging anymore.
                    reset();

                    return;

            }
            
            //WACS: Have to convert back from tabbedpane coordinates to tabdock coordinates
            dockableOffset = SwingUtilities.convertPoint(sourceTabbedPane, dockableOffset, originDock);
            super.stopDragging(mouseEvent);
        }
        
        /**
	 * MODIFIED FOR WACS to better handle dragging tabs for WACS implementation.
	 * 
	 */
        @Override
	protected void computeScreenLocation(MouseEvent mouseEvent)
	{
            screenLocation.setLocation(mouseEvent.getX(), mouseEvent.getY());
            //WACS: Look-up position in WacsTabDock, not the tabbedPane
            SwingUtilities.convertPointToScreen(screenLocation, (Component)originDock);
            //WACS: Shift screen location by originDock location, not really sure why this is needed - the window shifts slightly without this part.
            screenLocation.setLocation(screenLocation.getX()-originDock.getX(), screenLocation.getY()-originDock.getY());	
	}
}
