package com.javadocking.dock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import com.javadocking.DockingManager;
import static com.javadocking.dock.CompositeDock.CHILD_DOCK_PREFIX;
import static com.javadocking.dock.SplitDock.PROPERTY_LAST_WIDTH;
import com.javadocking.dock.factory.CompositeDockFactory;
import com.javadocking.dock.factory.DockFactory;
import com.javadocking.dock.factory.LeafDockFactory;
import com.javadocking.dock.factory.SplitDockFactory;
import com.javadocking.dock.factory.TabDockFactory;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockingMode;
import com.javadocking.event.ChildDockEvent;
import com.javadocking.util.PropertiesUtil;
import com.javadocking.util.SwingUtil;
import java.awt.Window;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * This is a composite dock that can contain zero, one, or two child docks. It can not contain dockables.
 * When dockables are added, child docks are created, and the dockables are added to the child docks.
 * </p>
 * <p>
 * Information on using split docks is in 
 * <a href="http://www.javadocking.com/developerguide/compositedock.html#SplitDock" target="_blank">How to Use Composite Docks</a> in 
 * <i>The Sanaware Developer Guide</i>.
 * </p>
 * <p>
 * When there is only one child dock, this dock is located in the center of this dock. When there are 2
 * child docks, there is one at the top and one at the bottom, or there is one at the left and one at the
 * right. The child docks are put in a split pane created with the component factory of the docking manager
 * {@link com.javadocking.DockingManager#getComponentFactory()} with the method
 * {@link com.javadocking.component.SwComponentFactory#createJSplitPane()}.
 * </p>
 * <p>
 * The positions for child docks of this dock are one-dimensional.
 * The possible child docks and their first position values are:
 * <ul>
 * <li>The child dock in the center if there is only one child dock: Position.CENTER.</li>
 * <li>The child dock at the top: {@link Position#TOP}.</li>
 * <li>The child dock at the bottom: {@link Position#BOTTOM}.</li>
 * <li>The child dock at the right: {@link Position#RIGHT}.</li>
 * <li>The child dock at the left: {@link Position#LEFT}.</li>
 * </ul>
 * </p>
 * <p>
 * A dockable can only be added if it has as one of its docking modes:
 * <ul>
 * <li>{@link DockingMode#LEFT}</li>
 * <li>{@link DockingMode#RIGHT}</li>
 * <li>{@link DockingMode#TOP}</li> 
 * <li>{@link DockingMode#BOTTOM}</li> 
 * </ul>
 * It can only be added in a position that corresponds with one of its docking modes.
 * </p>
 * <p>
 * If the mouse is inside a priority rectangle, the dockable can be docked with priority (see {@link Priority#CAN_DOCK_WITH_PRIORITY}).
 * When the mouse is inside the panel of this dock, but outside the priority rectangles,
 * the dockable can be docked, but without priority (see {@link Priority#CAN_DOCK}).
 * When the dock is empty, the only  priority rectangle is in the middle of the dock.
 * When there is already a child dock, there are 4 priority rectangles at the 4 borders of the panel of this dock.
 * </p>
 * <p>
 * When this dock contains no child docks, it is empty. When this dock contains two child docks, it is full. 
 * </p>
 * <p>
 * When this dock is empty, and a first dockable is added, a child dock is created with the factory retrieved by {@link #getChildDockFactory()}. 
 * The dockable is added to the child dock. The child dock is put in the center of this panel.
 * </p>
 * <p>
 * When there is already one child dock in the center, and a new dockable is added, again a child dock is created 
 * with the factory retrieved by {@link #getChildDockFactory()}. 
 * The dockable is added to the child dock. But now for both child docks a split dock is created with the factory 
 * retrieved by {@link #getCompositeChildDockFactory()}. The child docks are added to these new docks, and these new docks
 * are put at the left and the right side of the split of the split pane. These new docks are now the child docks of this split dock.
 * </p>
 * 
 * @author Heidi Rakels.
 */
public class RigidSplitDock extends SplitDock
{
        /*
         * MODIFIED FOR WACS:  Stop making splitdocks within splitdocks when adding child docks
         * 
         */
	public boolean addDockable(Dockable dockableToAdd, Point relativeLocation, Point dockableOffset)
	{
		
		// Verify the conditions for adding the dockable.
		if (getDockPriority(dockableToAdd, relativeLocation) == Priority.CANNOT_DOCK)
		{
			// We are not allowed to dock the dockable in this dock.
			return false;
		}
		
		// Are we still empty?
		if (singleChildDock == null)
		{
			// Create the leaf child dock.
			Dock newChildDock = childDockFactory.createDock(dockableToAdd, DockingMode.CENTER);
			
			// Add the dockable.
			newChildDock.addDockable(dockableToAdd, new Point(), dockableOffset);
			
			// Inform the listeners.
			dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, null, this, newChildDock));

			// Add the child dock.
			singleChildDock = newChildDock;
			singleChildDock.setParentDock(this);
			add((Component)singleChildDock, BorderLayout.CENTER);
			
			// Inform the listeners.
			dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, newChildDock));
		}
		else
		{
			// Get the position for the new dockable.
			int position = getDockPosition(relativeLocation, dockableToAdd);

			// Inform the listeners about the removal.
			dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, this, null, singleChildDock));

			// Remove everything.
			Dock oldSingleChildDock = singleChildDock;
			singleChildDock = null;
			this.remove((Component)oldSingleChildDock);
			
			// Inform the listeners about the removal.
			dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, this, null, oldSingleChildDock));

			// Get the preferred size of the current child dock. We need it later.
			Dimension currentChildDockPreferredSize = ((Component)oldSingleChildDock).getPreferredSize();

			// Create the new child split docks.
			Dock leftSplitDock = null;
			Dock rightSplitDock = null;
                        
                        
                        Dock newChildDock = childDockFactory.createDock(dockableToAdd, DockingMode.CENTER);
                        newChildDock.addDockable(dockableToAdd, new Point(), dockableOffset);
            
						
			// Add the dockables to this docks.
			try
			{
				switch (position)
				{
					case Position.LEFT:
						rightSplitDock = oldSingleChildDock;
						leftSplitDock = newChildDock;
						break;
					case Position.TOP:
						rightSplitDock = oldSingleChildDock;
						leftSplitDock = newChildDock;
						break;
					case Position.RIGHT:
						leftSplitDock = oldSingleChildDock;
						rightSplitDock = newChildDock;
						break;
					case Position.BOTTOM:
						leftSplitDock = oldSingleChildDock;
						rightSplitDock = newChildDock;
						break;
				}
			}
			catch (ClassCastException exception)
			{
				System.out.println("The splitChildDockFactory should create a com.javadocking.dock.SplitDock for the modes DockingMode.LEFT, DockingMode.RIGHT, DockingMode.TOP and DockingMode.BOTTOM.");
				exception.printStackTrace();
			}
			
			// Where has to be the divider.
			// Get the prefered size of the dockable.
			int dockingMode = getDockingMode(position);
			Dimension preferredSize = childDockFactory.getDockPreferredSize(dockableToAdd, dockingMode);
			int dividerLocation = this.getSize().width / 2;
			if (position == Position.LEFT)
			{
				int dockingWidth = getChildDockWidth(preferredSize.width, currentChildDockPreferredSize.width, getSize().width, position);
				dividerLocation = dockingWidth;
			} 
			else if (position == Position.RIGHT)
			{
				int dockingWidth = getChildDockWidth(preferredSize.width, currentChildDockPreferredSize.width, getSize().width, position);
				dividerLocation = this.getSize().width - dockingWidth;
			} 
			else if (position == Position.TOP)
			{
				int dockingHeight = getChildDockWidth(preferredSize.height, currentChildDockPreferredSize.height, getSize().height, position);
				dividerLocation = dockingHeight;
			} 
			else if (position == Position.BOTTOM)
			{
				int dockingHeight = getChildDockWidth(preferredSize.height, currentChildDockPreferredSize.height, getSize().height, position);
				dividerLocation = this.getSize().height - dockingHeight;
			}
			
			// How do we have to split the pane?
			int orientation = HORIZONTAL_SPLIT;
			if ((position == Position.TOP) || (position == Position.BOTTOM))
			{
				orientation = VERTICAL_SPLIT;
			}

			// Create the split pane.
			setDocks(leftSplitDock, rightSplitDock, orientation, dividerLocation);
		}
		
		// Repaint.
		SwingUtil.repaintParent(this);
		
		return true;
		
	}
	
        /*
         * MODIFIED FOR WACS:  Stop making splitdocks within splitdocks when adding child docks
         * 
         */
	public void addChildDock(Dock dockToAdd, Position position) throws IllegalStateException
	{
		
		// Check if this dock is full.
		if (isFull())
		{
			throw new IllegalStateException("This dock is full.");
		}

		if (isEmpty())
		{
			// Inform the listeners about the add.
			dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, null, this, dockToAdd));

			setSingleChildDock(dockToAdd);
			
			// Inform the listeners about the add.
			dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, dockToAdd));

		}
		else
		{
			//TODO take same things of addDockable together.
			
			// Inform the listeners about the removal.
			dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, this, null, singleChildDock));

			// Remove everything.
			Dock oldSingleChildDock = singleChildDock;
			singleChildDock = null;
			this.remove((Component)oldSingleChildDock);
			
			// Inform the listeners about the removal.
			dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, this, null, oldSingleChildDock));

			// Get the preferred size of the current child dock. We need it later.
			Dimension currentChildDockPreferredSize = ((Component)oldSingleChildDock).getPreferredSize();

			// Get the position for the new dock.
			int splitPosition = Position.RIGHT;
			if (position.getDimensions() == 1)
			{
				int possiblePosition = position.getPosition(0);
				if ((possiblePosition == Position.LEFT) ||
					(possiblePosition == Position.RIGHT) ||
					(possiblePosition == Position.TOP) ||
					(possiblePosition == Position.BOTTOM))
				{
					splitPosition = possiblePosition;
				}
			}

			// Create the new child split docks.
			Dock leftSplitDock = null;
			Dock rightSplitDock = null;
						
			// Add the dockable and child dock to this docks.
			try
			{
				switch (splitPosition)
				{
					case Position.LEFT:
						leftSplitDock = dockToAdd;
						rightSplitDock = oldSingleChildDock;
						break;
					case Position.TOP:
						leftSplitDock = dockToAdd;
						rightSplitDock = oldSingleChildDock;
						break;
					case Position.RIGHT:
						leftSplitDock = oldSingleChildDock;
						rightSplitDock = dockToAdd;
						break;
					case Position.BOTTOM:
						leftSplitDock = oldSingleChildDock;
						rightSplitDock = dockToAdd;
						break;
				}
			}
			catch (ClassCastException exception)
			{
				System.out.println("The splitChildDockFactory should create a " +
						"com.javadocking.dock.SplitDock for the modes DockingMode.LEFT, DockingMode.RIGHT, DockingMode.TOP and DockingMode.BOTTOM.");
				exception.printStackTrace();
			}
			
			// Where has to be the divider.
			// Get the prefered size of the dockable.
			Dimension preferredSize = ((Component)dockToAdd).getPreferredSize();
			int dockWidth = this.getSize().width;
			int dockHeight = this.getSize().height;
			if ((dockWidth == 0) && (dockHeight == 0)) {
				dockWidth = lastWidth;
				dockHeight = lastHeight;
			}
			int dividerLocation = dockWidth / 2;
			if (splitPosition == Position.LEFT)
			{
				int dockingWidth = getChildDockWidth(preferredSize.width, currentChildDockPreferredSize.width, dockWidth, splitPosition);
				dividerLocation = dockingWidth;
			} 
			else if (splitPosition == Position.RIGHT)
			{
				int dockingWidth = getChildDockWidth(preferredSize.width, currentChildDockPreferredSize.width, dockWidth, splitPosition);
				dividerLocation = dockWidth - dockingWidth;
			} 
			else if (splitPosition == Position.TOP)
			{
				int dockingHeight = getChildDockWidth(preferredSize.height, currentChildDockPreferredSize.height, dockHeight, splitPosition);
				dividerLocation = dockingHeight;
			} 
			else if (splitPosition == Position.BOTTOM)
			{
				int dockingHeight = getChildDockWidth(preferredSize.height, currentChildDockPreferredSize.height, dockHeight, splitPosition);
				dividerLocation = dockHeight - dockingHeight;
			}
			
			// How do we have to split the pane?
			int orientation = HORIZONTAL_SPLIT;
			if ((splitPosition == Position.TOP) || (splitPosition == Position.BOTTOM))
			{
				orientation = VERTICAL_SPLIT;
			}

			// Create the split pane.
			setDocks(leftSplitDock, rightSplitDock, orientation, dividerLocation);
		}

		// Repaint.
		SwingUtil.repaintParent(this);
	}
        
        
        /**
         * Modified for WACS so that it doesn't require split docks as children
         */
        public void loadProperties(String prefix, Properties properties, Map childDocks, Map dockablesMap, Window owner) throws IOException
	{
		
		// Load the class and properties of the leaf child dock factory.
		try
		{
			String leafChildDockFactoryClassName = LeafDockFactory.class.getName();
			leafChildDockFactoryClassName = PropertiesUtil.getString(properties, prefix + "leafChildDockFactory", leafChildDockFactoryClassName);
			Class leafChildDockFactoryClazz = Class.forName(leafChildDockFactoryClassName);
			childDockFactory = (DockFactory)leafChildDockFactoryClazz.newInstance();
			childDockFactory.loadProperties(prefix + "leafChildDockFactory.", properties);
		}
		catch (ClassNotFoundException exception)
		{
			System.out.println("Could not create the leaf child dock factory.");
			exception.printStackTrace();
			childDockFactory = new TabDockFactory();
		}
		catch (IllegalAccessException exception)
		{
			System.out.println("Could not create the leaf child dock factory.");
			exception.printStackTrace();
			childDockFactory = new TabDockFactory();
		}
		catch (InstantiationException exception)
		{
			System.out.println("Could not create the leaf child dock factory.");
			exception.printStackTrace();
			childDockFactory = new TabDockFactory();
		}

		// Load the class and properties of the child dock factory.
		try
		{
			String splitChildDockFactoryClassName = SplitDockFactory.class.getName();
			splitChildDockFactoryClassName = PropertiesUtil.getString(properties, prefix + "splitChildDockFactory", splitChildDockFactoryClassName);
			Class splitChildDockFactoryClazz = Class.forName(splitChildDockFactoryClassName);
			compositeChildDockFactory = (CompositeDockFactory)splitChildDockFactoryClazz.newInstance();
			compositeChildDockFactory.loadProperties(prefix + "splitChildDockFactory.", properties);
		}
		catch (ClassNotFoundException exception)
		{
			System.out.println("Could not create the split child dock factory.");
			exception.printStackTrace();
			compositeChildDockFactory = new SplitDockFactory();
		}
		catch (IllegalAccessException exception)
		{
			System.out.println("Could not create the split child dock factory.");
			exception.printStackTrace();
			compositeChildDockFactory = new SplitDockFactory();
		}
		catch (InstantiationException exception)
		{
			System.out.println("Could not create the split child dock factory.");
			exception.printStackTrace();
			compositeChildDockFactory = new SplitDockFactory();
		}

		lastWidth = PropertiesUtil.getInteger(properties, prefix + PROPERTY_LAST_WIDTH, lastWidth);
		lastHeight = PropertiesUtil.getInteger(properties, prefix + PROPERTY_LAST_HEIGHT, lastHeight);
		removeLastEmptyChild = PropertiesUtil.getBoolean(properties, prefix + PROPERTY_REMOVE_LAST_EMPTY_CHILD, removeLastEmptyChild);

		// How many child docks do we have? 0, 1 or 2.
		if (childDocks != null)
		{
			int childCount = childDocks.keySet().size();
			if (childCount == 1)
			{
				// Add the only child.
				Iterator iterator = childDocks.values().iterator();
				Dock childDock = (Dock)iterator.next();
				
				// Inform the listeners about the add.
				dockingEventSupport.fireDockingWillChange(new ChildDockEvent(this, null, this, childDock));

				setSingleChildDock(childDock);
				
				// Inform the listeners about the add.
				dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, childDock));

			}
			else if (childCount == 2)
			{
				// Get the IDs of the child docks.
				Iterator iterator = childDocks.keySet().iterator();
				String firstChildDockId = (String)iterator.next();
				String secondChildDockId = (String)iterator.next();
				
				// Get the position of the first child.
				Position position = null;
				position = Position.getPositionProperty(properties, prefix + CHILD_DOCK_PREFIX + firstChildDockId + "." + Position.PROPERTY_POSITION, position);
				int firstChildPosition = position.getPosition(0);
				
				// Get the left and right split child dock.
				Dock leftSplitDock = null;
				Dock rightSplitDock = null;
				if ((firstChildPosition == Position.LEFT) || (firstChildPosition == Position.TOP))
				{
					leftSplitDock = (Dock)childDocks.get(firstChildDockId);
					rightSplitDock = (Dock)childDocks.get(secondChildDockId);
				}
				else
				{
					leftSplitDock = (Dock)childDocks.get(secondChildDockId);
					rightSplitDock = (Dock)childDocks.get(firstChildDockId);	
				}

				// Get the orientation and divider location property.
				int orientation = HORIZONTAL_SPLIT;
				int dividerLocation = 200;
				orientation = PropertiesUtil.getInteger(properties, prefix + PROPERTY_ORIENTATION, orientation);
				dividerLocation = PropertiesUtil.getInteger(properties, prefix + PROPERTY_DIVIDER_LOCATION, dividerLocation);
				lastDividerLocation = dividerLocation;
				
				// Set both child docks in a splitpane.
				setDocks(leftSplitDock, rightSplitDock, orientation, dividerLocation);
			}
		}
		
	}
}
