package com.javadocking.dock;

import javax.swing.JComponent;
import com.javadocking.dockable.Dockable;
import com.javadocking.util.SwingUtil;

/**
 * <p>
 * This is a dock that can contain zero, one or multiple dockables. 
 * The dockables are organized in a line.
 * </p>
 * <p>
 * Information on using line docks is in 
 * <a href="http://www.javadocking.com/developerguide/leafdock.html#LineDock" target="_blank">How to Use Laef Docks</a> in 
 * <i>The Sanaware Developer Guide</i>.
 * </p>
 * <p>
 * It is a leaf dock. It cannot contain other docks.
 * </p>
 * <p>
 * When it contains no dockable it is empty. It is never full. 
 * </p>
 * <p>
 * A dockable can be docked in this dock if:
 * <ul>
 * <li>it has <code>horizontalDockingMode</code> or <code>verticalDockingMode</code> as possible docking mode.</li>
 * <li>its content component is not null.</li>
 * </ul>
 * A composite dockable can also be docked in this dock if: 
 * <ul>
 * <li>all of its child dockables have <code>horizontalDockingMode</code> or <code>verticalDockingMode</code> as possible docking mode.</li>
 * <li>all of its child dockables have a content component that is not null.</li>
 * </ul>
 * </p>
 * <p>
 * The size of all the child dockables is the same when the <code>grid</code> property
 * is set to true, otherwise the size of the different child dockables will be according to the
 * preferred size of their content component.
 * </p>
 * <p>
 * The {@link com.javadocking.dock.Position} for dockables docked in this dock are one-dimensional.
 * The first position value of a child dockable is between 0 and the number of child dockables minus 1, 
 * it is the position in the line.
 * </p>
 * 
 * @author Heidi Rakels.
 */
public class WacsLineDock extends LineDock
{

	/**
	 * Constructs a horizontal line dock.
	 */
	public WacsLineDock()
	{
            super();
	}
	
	/**
	 * Constructs a line dock with the given orientation.
	 * 
	 * @param 	orientation		The orientation for the line dock.
	 * @param 	grid			True when the dockables will have the same size, false otherwise.
	 */
	public WacsLineDock(int orientation, boolean grid)
	{
            super (orientation, grid);
	}
	
	/**
	 * Constructs a line dock with the given orientation.
	 * 
	 * @param 	orientation		The orientation for the line dock.
	 * @param 	grid			True when the dockables will have the same size, false otherwise.
	 * @param 	horizontalDockingMode	The docking mode for a dockable that is docked in this line dock, 
	 * 									if the line dock has a horizontal orientation (ORIENTATION_HORIZONTAL).
	 * @param 	verticalDockingMode		The docking mode for a dockable that is docked in this line dock, 
	 * 									if the line dock has a horizontal orientation (ORIENTATION_VERTICAL).
	 */
	public WacsLineDock(int orientation, boolean grid, int horizontalDockingMode, int verticalDockingMode)
	{
            super (orientation, grid, horizontalDockingMode, verticalDockingMode);
	}


        /**
         * MODIFIED FOR WACS TO REMOVE LINE HANDLE IF ONLY ONE DOCKABLE SHOWN
         * 
         * @param dockable
         * @return 
         */
        @Override
	public boolean removeDockable(Dockable dockable)
	{
            boolean retVal = super.removeDockable(dockable);
            if (retVal)
            {
                verifyHandleNeeded(false);
                	
		// Repaint.
		SwingUtil.repaintParent(this);
            }
            return retVal;
	}

	/**
         * MODIFIED FOR WACS TO REMOVE LINE HANDLE IF ONLY ONE DOCKABLE SHOWN
         * 
         * @param dockable
         * @param position 
         */
	public void addDockable(Dockable dockable, Position position)
	{
            super.addDockable(dockable, position);

            verifyHandleNeeded(true);

            // Repaint.
            SwingUtil.repaintParent(this);
	}
        
        /**
         * Added for WACS, check if line handle is needed
         */
        private void verifyHandleNeeded(boolean adding)
        {
            if (dockablePanel.getComponentCount() == 1)
            {
                this.remove((JComponent)handle);
            }
            else if (adding && dockablePanel.getComponentCount() > 1)
            {
                this.remove((JComponent)handle);
                this.remove(dockablePanel);
                this.add((JComponent)handle);
                this.add(dockablePanel);
            }
        }
}
