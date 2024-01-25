/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javadocking.drag;

import com.javadocking.dock.Dock;
import com.javadocking.dock.WacsTabDock;
import com.javadocking.dockable.Dockable;
import com.javadocking.drag.painter.DockableDragPainter;

/**
 *
 * @author humphjc1
 */
public class WacsStaticDraggerFactory extends StaticDraggerFactory
{
    /**
    * Constructs a dragger factory with a default painter for painting the dragged dockables.
    */
   public WacsStaticDraggerFactory()
   {
       super ();
   }

   /**
    * Constructs a dragger factory with the given painter for painting the dragged dockables.
    * 
    * @param	dockableDragPainter 		The painter for painting the dragged dockables.
    */
   public WacsStaticDraggerFactory(DockableDragPainter dockableDragPainter)
   {
        super (dockableDragPainter);
   }
        
    /**
     * MODIFIED FOR WACS TO CREATE WacsStaticDockableDragger, not StaticDockableDragger
     */
    @Override
    public Dragger createDragger(Dockable dockable)
    {
        try
        {
            return new WacsStaticDockableDragger(dockable, getDockableDragPainter());
        }
        catch (Exception e)
        {
            return super.createDragger(dockable);
        }
    }
    
        /**
	 * MODIFIED FOR WACS to create WacsStaticTabDragger, not StaticTabDragger
	 */
    @Override
    public Dragger createDragger(Dock dock)
    {
        if (dock instanceof WacsTabDock)
        {
            return new WacsStaticTabDragger(getDockableDragPainter());
        }
        else
        {
            return super.createDragger(dock);
        }
    }
}
