
package edu.jhuapl.jlib.jgeo.action;

import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import java.awt.event.ActionEvent;

/**
 *
 * @author humphjc1
 */
public class ZoomInActionExt extends ZoomInAction
{
    public interface ZoomInActionChecker
    {
        public boolean canDoZoomIn ();
        public void zoomInDone (int zoomSteps);
    }

    private ZoomInActionChecker zoomChecker = null;

    public void setZoomInActionChecker (ZoomInActionChecker checker)
    {
        zoomChecker = checker;
    }

    public ZoomInActionExt(JGeoCanvas canvas)
    {
        super (canvas);
    }

    public void actionPerformed(ActionEvent event)
    {
        boolean zoomApproved = false;
        if (zoomChecker == null)
        {
            zoomApproved = true;
        }
        else if (event == null)
        {
            //bypass check if null passed in
            zoomApproved = true;
        }
        else
        {
            zoomApproved = zoomChecker.canDoZoomIn();
        }

        
        if (zoomApproved)
        {
            super.actionPerformed (event);
            zoomChecker.zoomInDone(1);
        }        
    }
}
