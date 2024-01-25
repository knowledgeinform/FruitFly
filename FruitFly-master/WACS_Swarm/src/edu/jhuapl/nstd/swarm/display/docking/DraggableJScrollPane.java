/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dockable.DraggableContent;
import com.javadocking.drag.DragListener;
import java.awt.Component;
import javax.swing.JScrollPane;

/**
 *
 * @author humphjc1
 */
public class DraggableJScrollPane extends JScrollPane implements DraggableContent
{
    public DraggableJScrollPane (Component component)
    {
        super (component);
    }
    
    @Override
    public void addDragListener(DragListener dl) 
    {
        addMouseListener(dl);
        addMouseMotionListener(dl);
    }
    
}
