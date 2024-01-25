package com.javadocking.drag;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

import com.javadocking.dock.Dock;
import com.javadocking.dock.FloatDock;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.drag.painter.DefaultRectanglePainter;
import com.javadocking.drag.painter.DockableDragPainter;
import com.javadocking.drag.painter.SwDockableDragPainter;

/**
 * <p>
 * This is a class for dragging one {@link com.javadocking.dockable.Dockable}. 
 * </p>
 * <p>
 * The {@link com.javadocking.dock.Dock}s that are used in the application should inherit 
 * from the java.awt.Component class.
 * </p>
 * @author Heidi Rakels.
 */
public class WacsStaticDockableDragger extends StaticDockableDragger
{
    	/**
	 * Constructs a dragger with a default painter for painting the dragged dockables: 
	 * a {@link SwDockableDragPainter} with a {@link DefaultRectanglePainter}.
	 * 
	 * @param 	draggedDockable				The dockable that will be dragged by this dragger.
	 * @throws	IllegalArgumentException	If the dockable is null.
	 */
	public WacsStaticDockableDragger(Dockable draggedDockable)
	{
		super (draggedDockable);
	}
	
	/**
	 * Constructs a dragger with the given painter for painting the dragged dockables.
	 * 
	 * @param 	draggedDockable				The dockable that will be dragged by this dragger.
	 * @param	dockableDragPainter 		The painter for painting the dragged dockables.
	 * @throws	IllegalArgumentException	If the dockable is null.
	 */
	public WacsStaticDockableDragger(Dockable draggedDockable, DockableDragPainter dockableDragPainter)
	{
		super (draggedDockable, dockableDragPainter);
	}
	
        
	/**
	 * MODIFIED FOR WACS TO HANDLE CASE OF DESTINATIONDOCK = FLOATDOCK, FORCE RECTANGLE TO BE PAINTED ON FLOATDOCK, RATHER THAN
         * WHATEVER PARENT CLASS IS DOING
	 */
        @Override
	public void drag(MouseEvent mouseEvent) 
	{
		
		// Get the component of the mouse event.  
		Component mouseComponent = (Component)mouseEvent.getSource();

		// Get the mouse location in screen coordinates.
		computeScreenLocation(mouseEvent);

		// Do we have to move an externalized dockable?
		if (draggedDockable.getState() == DockableState.EXTERNALIZED)
		{
			// Move the dockable.
			ExternalizedDraggerSupport.moveExternalizedDockable(draggedDockable, screenLocation, dockableOffset);
			return;
		}

		// Get the destination dock for this position for the dockable that we are dragging.
		Dock[] destinationDocks = dockRetriever.retrieveHighestPriorityDock(screenLocation, draggedDockable);
		if (destinationDocks == null)
		{
			// We have no destination dock any more. Clean up what was painted before.
			clearPainting();

			// Set the 'cannot dock' cursor.
			cursorManager.setCursor(mouseComponent, retrieveCanNotDockCursor());

			return;
		}
		Dock destinationDock = destinationDocks[0];

		// Do we have a destination dock?
		if (destinationDock != null)
		{
                    
			// Does the destination dock inherit from java.awt.Component or is it the float dock?
			if (destinationDock instanceof Component)
			{
                            	// Get the docking rectangle for the destination dock.
				locationInDestinationDock.setLocation(screenLocation.x, screenLocation.y);
				SwingUtilities.convertPointFromScreen(locationInDestinationDock, (Component)destinationDock);
				destinationDock.retrieveDockingRectangle(draggedDockable, locationInDestinationDock, dockableOffset, dockableDragRectangle);

				// Paint the new rectangle.
				dockableDragPainter.paintDockableDrag(draggedDockable, destinationDock, dockableDragRectangle, locationInDestinationDock);
					
				// Set the 'can dock' cursor.
				cursorManager.setCursor((Component)destinationDock, retrieveCanDockCursor());
				
			}
			else if (destinationDock instanceof FloatDock)
			{
                            //WACS
                            destinationDock.retrieveDockingRectangle(draggedDockable, screenLocation, dockableOffset, dockableDragRectangle);
                            // Convert this rectangle to the origindock.
                            locationInDestinationDock.setLocation(dockableDragRectangle.x, dockableDragRectangle.y);
                            //SwingUtilities.convertPointFromScreen(locationInDestinationDock, (Component)originDock);
                            dockableDragRectangle.setLocation(locationInDestinationDock);
                            locationInDestinationDock.setLocation(locationInDestinationDock.x + dockableOffset.x, locationInDestinationDock.y + dockableOffset.y);

                            // Paint the new rectangle.
                            dockableDragPainter.paintDockableDrag(draggedDockable, destinationDock, dockableDragRectangle, locationInDestinationDock);
                            //end WACS
                            
                            	
				// Set the 'can dock' cursor.
				cursorManager.setCursor(mouseComponent, retrieveCanDockCursor());
				
			}
			else
			{
				// Currentle this should not happen. All docks, except the float dock inherit from java.awt.Component.
				// We have a dock where we cannot paint. Clean up what was painted before.
				clearPainting();
				
				// Set the 'can dock' cursor.
				cursorManager.setCursor(mouseComponent, retrieveCanDockCursor());
			}

		}
		else
		{
			// We have no destination dock any more. Clean up what was paintedbefore.
			clearPainting();

			// Set the 'cannot dock' cursor.
			cursorManager.setCursor(mouseComponent, retrieveCanNotDockCursor());
		}
		
	}	
}
