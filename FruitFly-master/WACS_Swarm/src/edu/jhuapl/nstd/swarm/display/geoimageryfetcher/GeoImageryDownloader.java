/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.display.geoimageryfetcher;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author humphjc1
 */
public class GeoImageryDownloader implements GeoImageryFetcher.GeoImageryHandler
{
    String folder = ".\\imagesDownloaded_3_1_2011";
    double minLat = 29;
    double maxLat = 39;
    double minLon = 60;
    double maxLon = 75;
    int minZoomLevel = 6;
    int maxZoomLevel = 12;
    int m_ReqdTilesInRegion = 0;
    int m_TilesLoadedCounter = 0;

    GeoImageryFetcher m_geoImageryFetcher = null;

    /**
     * Message dialog for dted generation status
     */
    JOptionPane m_GeneratorStatusFrame = null;

    /**
     * Message dialog for dted generation status
     */
    JDialog m_GeneratorStatusDialog = null;

    /**
     * Flag to displaying DTED generator status to user.  If false, don't regenerate
     */
    boolean m_DtedGeneratorStatusDisplayFlag = false;

    /**
     * Parent frame for status dialog.
     */
    JFrame m_ParentFrame = null;




    public GeoImageryDownloader ()
    {
        downloadImagery();
        m_ParentFrame = null;
    }

    public GeoImageryDownloader (JFrame parentFrame, String folder, double minLat, double maxLat, double minLon, double maxLon, int minZoom, int maxZoom)
    {
        this.folder = folder;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minZoomLevel = minZoom;
        this.maxZoomLevel = maxZoom;
        this.m_ParentFrame = parentFrame;

        downloadImagery ();
    }

    private void downloadImagery ()
    {

        m_geoImageryFetcher = new GeoImageryFetcher(folder, this, true);

        m_ReqdTilesInRegion = 0;
        m_TilesLoadedCounter = 0;
        for (int i = maxZoomLevel; i >= minZoomLevel; i --)
            m_ReqdTilesInRegion += m_geoImageryFetcher.fetchRegion(maxLat, minLon, minLat, maxLon, i);
    
    }

    public void kill ()
    {
        if (m_geoImageryFetcher != null)
            m_geoImageryFetcher.killThreads();
        m_geoImageryFetcher = null;
        
    }


    public static void main (String args[])
    {

        GeoImageryDownloader geo = new GeoImageryDownloader();

        while (true)
        {
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GeoImageryDownloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void geoImageLoaded(BufferedImage image, double centerLatitude_deg, double centerLongitude_deg, double width_m, double height_m, int zoomLevel)
    {
        System.out.println ("Tile downloaded succesfully: " + centerLatitude_deg + ", " + centerLongitude_deg + ", " + zoomLevel);
        m_TilesLoadedCounter ++;


        final String statusMsg = "Loaded tile " + m_TilesLoadedCounter + " of " + m_ReqdTilesInRegion + " total tiles to load";

        try
        {
            if (m_GeneratorStatusFrame == null)
            {
                m_GeneratorStatusFrame = new JOptionPane ();
            }

            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {

                    //Set status message
                    m_GeneratorStatusFrame.setMessage(statusMsg);

                    if (m_GeneratorStatusDialog == null)
                    {
                        m_GeneratorStatusDialog = m_GeneratorStatusFrame.createDialog(m_ParentFrame, "Imagery download status");
                    }

                    if (!m_DtedGeneratorStatusDisplayFlag)
                    {
                        m_DtedGeneratorStatusDisplayFlag = true;
                        new Thread ()
                        {
                            public void run ()
                            {
                                //Raise dialog
                                m_GeneratorStatusDialog.setVisible(true);

                                Object selectedValue = m_GeneratorStatusFrame.getValue();
                                if (selectedValue.equals(JOptionPane.UNINITIALIZED_VALUE))
                                    return;

                                m_GeneratorStatusDialog.setVisible(false);
                                m_GeneratorStatusFrame.setValue(JOptionPane.UNINITIALIZED_VALUE);
                                m_DtedGeneratorStatusDisplayFlag = false;
                            }
                        }.start();

                    }
                }
            });
        }
        catch (Exception e)
        {
            //ignore exception, we're just displaying status that doesn't really matter.
        }
    }

    public void geoImageUnloaded(double centerLatitude_deg, double centerLongitude_deg, double width_m, double height_m, int zoomLevel)
    {

    }

    @Override
    public void geoImageLoaded(TileIdentifier tileIdentifier)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void geoImageUnloaded(TileIdentifier tileIdentifier)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void geoTileJobFinished()
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void geoNeedsZoomOut()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
