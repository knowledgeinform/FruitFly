/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display.docking;

import com.javadocking.dockable.DraggableContent;
import com.javadocking.drag.DragListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 *
 * @author humphjc1
 */
public class DraggableJPanel extends JPanel implements DraggableContent
{
    public DraggableJPanel ()
    {
        super ();
    }
    
    public DraggableJPanel (Component component)
    {
        super (new BorderLayout());
        this.setBorder(new LineBorder(Color.gray, 2, true));
        this.add(component);
    }
    
    @Override
    public void addDragListener(DragListener dl) 
    {
        addMouseListener(dl);
        addMouseMotionListener(dl);
    }
    
}
