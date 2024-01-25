package com.javadocking.drag.painter;

import java.awt.Point;
import java.awt.Rectangle;
import com.javadocking.dock.Dock;
import com.javadocking.dockable.Dockable;

/**
 * <p>
 * This dockable drag painter shows a window with the image of the dockable
 * at the current mouse position, when the dockable is dragged.
 * </p>
 * <p>
 * This window is also visible outside the owner window.
 * </p>
 * <p>
 * The dock cursor or cannotdock cursor is shown on the window.
 * </p>
 * <p>
 * Several properties define the size of the image of the dockable:
 * <ul>
 * <li>preferredReduceFactor</li>
 * <li>minImageSize</li>
 * <li>maxImageSize</li>
 * <li>minReduceFactor</li>
 * </ul>
 * </p>
 * 
 * @author Heidi Rakels.
 */
public class WacsImageDockableDragPainter extends ImageDockableDragPainter
{
    /**
     * Modified for WACS to bring window to front before final paint
     * @param newDockable
     * @param dock
     * @param rectangle
     * @param locationInDestinationDock 
     */
    @Override
    public void paintDockableDrag(Dockable newDockable, Dock dock, Rectangle rectangle, Point locationInDestinationDock)
    {
        super.paintDockableDrag(newDockable, dock, rectangle, locationInDestinationDock);
        window.toFront();
    }
}
