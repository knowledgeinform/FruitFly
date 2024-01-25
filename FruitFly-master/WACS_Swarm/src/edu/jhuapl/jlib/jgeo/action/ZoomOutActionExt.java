
package edu.jhuapl.jlib.jgeo.action;

import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import java.awt.event.ActionEvent;

/**
 *
 * @author humphjc1
 */
public class ZoomOutActionExt extends ZoomOutAction
{
    public interface ZoomOutActionChecker
    {
        public boolean canDoZoomOut ();
        public void zoomOutDone (int zoomSteps);
    }

    private ZoomOutActionChecker zoomChecker = null;

    public void setZoomOutActionChecker (ZoomOutActionChecker checker)
    {
        zoomChecker = checker;
    }

    public ZoomOutActionExt(JGeoCanvas canvas)
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
            zoomApproved = zoomChecker.canDoZoomOut();
        }


        if (zoomApproved)
        {
            super.actionPerformed (event);
            zoomChecker.zoomOutDone(1);
        }
    }
}
