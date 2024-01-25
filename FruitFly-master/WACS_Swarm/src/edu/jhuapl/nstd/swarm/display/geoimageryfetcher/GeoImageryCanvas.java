package edu.jhuapl.nstd.swarm.display.geoimageryfetcher;

import edu.jhuapl.jlib.jgeo.JGeoCanvas;
import edu.jhuapl.jlib.jgeo.JGeoGraphics;
import edu.jhuapl.jlib.jgeo.ViewTransform;
import edu.jhuapl.jlib.jgeo.action.ZoomInActionExt;
import edu.jhuapl.jlib.jgeo.action.ZoomOutActionExt;
import edu.jhuapl.jlib.jgeo.event.JGeoMouseEvent;
import edu.jhuapl.jlib.jgeo.event.JGeoMouseListener;
import edu.jhuapl.jlib.jgeo.event.JGeoMouseMotionListener;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.jlib.math.position.RangeBearingHeightOffset;
import edu.jhuapl.nstd.swarm.display.DisplayUnitsManager;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class GeoImageryCanvas extends JGeoCanvas implements GeoImageryFetcher.GeoImageryHandler,
                                                            JGeoMouseListener,
                                                            JGeoMouseMotionListener,
                                                            MouseWheelListener,
                                                            ZoomInActionExt.ZoomInActionChecker,
                                                            ZoomOutActionExt.ZoomOutActionChecker
{
    private static final boolean VERBOSE = false;
    
    private GeoImageryFetcher m_geoImageryFetcher;
    private HashSet<TileIdentifier> m_tileList = new HashSet<TileIdentifier>();
    private HashSet<TileIdentifier> m_loadingTileList = new HashSet<TileIdentifier>();
    protected int m_zoomLevelActual;
    protected int m_zoomLevelRequestedInteger;
    protected double m_zoomLevelRequestedDouble;
    protected double m_ZoomStepFraction;
    private TileRequestorThread m_tileRequestorThread;
    protected AbsolutePosition m_mousePrevPosition = null;
    protected long m_lastRepaintTime_ms = 0;
    
    private double m_topLatitude_deg = 0;
    private double m_bottomLatitude_deg = 0;
    private double m_leftLongitude_deg = 0;
    private double m_rightLongitude_deg = 0;
    
    protected BufferedImage m_ImageBuffer;
    protected Color m_BackgroundColor;
    protected boolean m_ShowBackgroundImage;
    
    /**
     * If true, the current viewpoint has changed.  Used by subclasses to determine if graphics view has been modified and should be reloaded
     */
    protected boolean m_ViewChangedSinceRepaint;


    /**
     * Actions for navigating display
     */
    protected ZoomInActionExt zoomInAction;
    /**
     * Actions for navigating display
     */
    protected ZoomOutActionExt zoomOutAction;

    /**
     * Position of mouse
     */
    private LatLonAltPosition m_MousePosition = null;

    /**
     * Pixel of mouse in canvas coordinates
     */
    private Cell m_RulerEndPixel = null;
    
    /**
     * Lock object for ruler tool
     */
    private final Object m_RulerLock = new Object ();

    /**
     * If true, use ruler tool in display
     */
    private boolean m_RulerActivated = false;

    /**
     * Start point for ruler measurement
     */
    private LatLonAltPosition m_RulerStartPoint = null;

    /**
     * End point for ruler measurement
     */
    private LatLonAltPosition m_RulerEndPoint = null;

    /**
     * Range between ruler points
     */
    private Length m_RulerRange = null;

    
    /**
     * Formatter with 2 decimal places
     */
    private DecimalFormat m_DecFormat2 = new DecimalFormat ("#.##");

    /**
     * Minimum zoom level for imagery.  Most zoomed out display can be
     */
    protected int m_MinZoomLevel;

    /**
     * Maximum zoom level for imagery.  Most zoomed in display can be
     */
    protected int m_MaxZoomLevel;

    /**
     * If true, repaint immediately on next call to paint.
     */
    protected boolean m_PaintImmediately = false;
    
    /**
     * If true, update settings based on resize next time the canvas is painted
     */
    protected boolean m_ForceUpdateSizeNextPaint = false;

   /**
     * Units selected for display in ruler tool
     */
    private int m_RulerUnitsSelected = 0;

    /**
     * If true, ruler is being modified
     */
    private boolean m_DraggingRuler = false;

    /**
     * Last pixel location of mouse click/drag action
     */
    private Cell m_MousePrevPixel = null;

    /**
     * Distance spanning scale in metric units (m or km)
     */
    private double m_ScaleMetricDistance;

    /**
     * Units of scaleMetricDistance
     */
    private String m_ScaleMetricDistanceUnits;

    /**
     * Pixels spanning metric distance for scale
     */
    private int m_ScaleMetricScreenPixels;

    /**
     * Distance spanning scale in English units (ft or nau miles)
     */
    private double m_ScaleEnglishDistance;

    /**
     * Units of scaleEnglishDistance
     */
    private String m_ScaleEnglishDistanceUnits;

    /**
     * Pixels spanning English distance for scale
     */
    private int m_ScaleEnglishScreenPixels;

    /**
     * Timestamp of most recent mouse click.  Used to determine if a click was fast enough
     * to be a double click
     */
    private long m_LastMouseClickMs;

    /**
     * Max time allowed between clicks for a double click event to be generated
     */
    private long m_DoubleClickSpeedMs = 500;
    
    protected double m_ZoomFactor = Config.getConfig().getPropertyAsDouble("SearchCanvas.ZoomFactor", 2.0);
    
    protected int m_TileBufferSize = Config.getConfig().getPropertyAsInteger("GeoImageryCanvas.TileBuffer", 5);

    /**
     * @return the m_BackgroundColor
     */
    public Color getBackgroundColor() {
        return m_BackgroundColor;
    }

    /**
     * @param m_BackgroundColor the m_BackgroundColor to set
     */
    public void setBackgroundColor(Color m_BackgroundColor) {
        this.m_BackgroundColor = m_BackgroundColor;
    }

    /**
     * @return the m_ShowBackgroundImage
     */
    public boolean isShowBackgroundImage() {
        return m_ShowBackgroundImage;
    }

    /**
     * @param m_ShowBackgroundImage the m_ShowBackgroundImage to set
     */
    public void setShowBackgroundImage(boolean m_ShowBackgroundImage) {
        this.m_ShowBackgroundImage = m_ShowBackgroundImage;
    }

    /**
     * Provides access to JGeoCanvas constructor bypassing this imagery fetcher stuff.
     * 
     * @param numberOfLevels
     * @param range
     * @param width
     * @param height
     * @param projectionType
     * @param center
     */
    public GeoImageryCanvas (final int numberOfLevels, final Length range,
            final int width, final int height,
            final int projectionType,
            final LatLonAltPosition center,
            final int minZoom, final int maxZoom)
    {
        super (numberOfLevels, range, width, height, height, center);

        zoomInAction = new ZoomInActionExt(this);
        zoomOutAction = new ZoomOutActionExt(this);
        zoomInAction.setZoomFactor(1/m_ZoomFactor);
        zoomOutAction.setZoomFactor(m_ZoomFactor);
        zoomInAction.setZoomInActionChecker(this);
        zoomOutAction.setZoomOutActionChecker(this);
        zoomInAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/ZoomIn32.png")));
        zoomOutAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/ZoomOut32.png")));
        
        m_BackgroundColor = new Color(7, 75, 150);
        m_ShowBackgroundImage = true;
        m_ViewChangedSinceRepaint = true;
        
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void componentShown(ComponentEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
    
    public void ShowBackground()
    {
        m_ShowBackgroundImage = true; 
    }
    
     public void HideBackground()
    {
        m_ShowBackgroundImage = false; 
    }
    

    
    public GeoImageryCanvas(final int numCanvasLevels,
                            final Length range,
                            final int width,
                            final int height,
                            final LatLonAltPosition center,
                            final String imageDirectory, final boolean useInternet,
                            final int minZoom, final int maxZoom)
    {
        super (numCanvasLevels, range, width, height, JGeoCanvas.ORTHOGRAPHIC_PROJECTION, center);
        //super (numCanvasLevels, range, width, height, JGeoCanvas.MERCATOR_PROJECTION, center);

        m_geoImageryFetcher = new GeoImageryFetcher(imageDirectory, this, useInternet);
        m_MinZoomLevel = minZoom;
        m_MaxZoomLevel = maxZoom;
        m_ZoomStepFraction = m_ZoomFactor-1;
        
        m_BackgroundColor = new Color(7, 75, 150);
        m_ShowBackgroundImage = true;
        m_ViewChangedSinceRepaint = true;

        int prefTilePixelSize = 600;
        int prefXTiles = (int)Math.ceil(((float)width)/prefTilePixelSize);
        int prefReqPixelSize = 500;
        double prefReqXMetersPerPixel = (range.getDoubleValue(Length.METERS)/prefXTiles)/prefReqPixelSize;

        int initialZoomLevel = m_geoImageryFetcher.getZoomLevelFromPrefReqMetersPerPixel (center.getLatitude().getDoubleValue(Angle.DEGREES), prefReqXMetersPerPixel);
        m_zoomLevelActual = initialZoomLevel;
        m_zoomLevelRequestedInteger = initialZoomLevel;
        m_zoomLevelRequestedDouble = initialZoomLevel;
//        m_tileLists.put(m_zoomLevelRequestedInteger, new HashSet<TileIdentifier>());

        m_tileRequestorThread = new TileRequestorThread(this);
        m_tileRequestorThread.setDaemon(true);
        m_tileRequestorThread.start();

        zoomInAction = new ZoomInActionExt(this);
        zoomOutAction = new ZoomOutActionExt(this);
        double inverseToDouble = 1/m_ZoomStepFraction;
        double geoZoomFactor = Math.pow(2, 1/inverseToDouble);
        zoomInAction.setZoomFactor(1/geoZoomFactor);
        zoomOutAction.setZoomFactor(geoZoomFactor);
        zoomInAction.setZoomInActionChecker(this);
        zoomOutAction.setZoomOutActionChecker(this);

        zoomInAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/ZoomIn32.png")));
        zoomOutAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon (getClass().getResource("/icons/ZoomOut32.png")));
        
        
        addJGeoMouseListener(this);
        addJGeoMouseMotionListener(this);
        addMouseWheelListener(this);

        updateScale();
        
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
    }
    
//    public void setTileBufferSize(int bufferSize)
//    {
//        m_geoImageryFetcher.setTileBufferSize(bufferSize);
//    }

    @Override
    public void jgeoUpdateComponent(JGeoGraphics graphics)
    {
        jgeoPaintComponent(graphics);
    }

    @Override
    public void jgeoPaintComponent(JGeoGraphics jg)
    {
        paintCanvas(jg, false);
        //paintScale(jg);
    }
    
    private void checkOffscreenImage() 
    {
    Dimension d = getSize();
        if (m_ImageBuffer == null || m_ImageBuffer.getWidth(null) != d.width
                || m_ImageBuffer.getHeight(null) != d.height)
        {
            m_ImageBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_3BYTE_BGR);
        }
    }
    
    private void paintOffscreen(Graphics g) 
    {
            synchronized (this)
            {
//                Set<TileIdentifier> tileList = m_tileList.get(Math.max(Math.min(m_zoomLevelRequestedInteger, m_MaxZoomLevel), m_MinZoomLevel));
                if (m_tileList != null)
                {
                    for (TileIdentifier tile : m_tileList)
                    {
                        doPaintTile(g, tile);
                    }
                }
                
                if (m_loadingTileList != null)
                {
                    for (TileIdentifier tile : m_loadingTileList)
                    {
                        if (tile.zoomLevel == m_zoomLevelRequestedInteger)
                        {
                            doPaintTile(g, tile);
                        }
                    }
                }
            }

    }
    
    private void doPaintTile(Graphics g, TileIdentifier tile)
    {
        try
        {
            int width, height;
            
            java.awt.Point screenCenter = view.earthToMouse(tile.centerPosition);
            if (getProjectionType() == ORTHOGRAPHIC_PROJECTION)
            {
                width = view.getLengthInPixels(tile.width.times(Math.cos(tile.centerPosition.getLatitude().getDoubleValue(Angle.RADIANS))));
            }
            else
            {
                width = view.getLengthInPixels(tile.width);
            }
            height = view.getLengthInPixels(tile.height);

            if (screenCenter != null)
            {
                g.drawImage(tile.image, screenCenter.x - (width / 2),
                            screenCenter.y - (height / 2), width,
                            height, null);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void paintCanvas (JGeoGraphics jg, boolean paintNow)
    {
        if (m_lastRepaintTime_ms == 0)
            updateScale();
        if (m_ForceUpdateSizeNextPaint)
        {
            updateSize ();
            m_ForceUpdateSizeNextPaint = false;
        }
        
        if (System.currentTimeMillis() - m_lastRepaintTime_ms > 50 || paintNow || m_PaintImmediately)
        {
            m_PaintImmediately = false;
            //jg.clear();
            
            
                Dimension d = getSize();
                checkOffscreenImage();
                Graphics offG = m_ImageBuffer.getGraphics();
                offG.setColor(m_BackgroundColor);
                offG.fillRect(0, 0, d.width, d.height);
                // Draw into the offscreen image.
                
                if(m_ShowBackgroundImage)
                {
                    paintOffscreen(m_ImageBuffer.getGraphics());
                }
                // Put the offscreen image on the screen.
                jg.drawImage(m_ImageBuffer, 0, 0, null);
    
                

//            synchronized (this)
//            {
//                Set<TileIdentifier> tileList = m_tileLists.get(m_zoomLevel);
//                if (tileList != null)
//                {
//                    for (TileIdentifier tile : tileList)
//                    {
//                        try
//                        {
//                            if(getProjectionType() == ORTHOGRAPHIC_PROJECTION)
//                            {
//                               jg.drawImage(tile.centerPosition, tile.width.times(Math.cos(tile.centerPosition.getLatitude().getDoubleValue(Angle.RADIANS))), tile.height, tile.image);
//                        
//                            }
//                            else
//                            {
//                               jg.drawImage(tile.centerPosition, tile.width, tile.height, tile.image);
//                            
//                            }
//                                
//                            
//                           
//                        }
//                        catch (Exception e)
//                        {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
            
            m_lastRepaintTime_ms = System.currentTimeMillis();
            m_ViewChangedSinceRepaint = false;
        }
    }

    public void paintRuler (JGeoGraphics jg)
    {
        if (m_RulerActivated)
        {
            synchronized (m_RulerLock)
            {
                if (m_RulerActivated && m_RulerEndPoint != null)
                {
                    double rangeM = m_RulerRange.getDoubleValue(Length.METERS);
                    String textToDisplay = "";
                    if (m_RulerUnitsSelected == DisplayUnitsManager.LENGTH_FEET)
                        textToDisplay = "  " + m_DecFormat2.format(rangeM/.3048) + " ft";
                    else //if (m_RulerUnitsSelected == DisplayUnitsManager.LENGTH_METERS)
                        textToDisplay = "  " + m_DecFormat2.format(rangeM) + " m";
                    
                    jg.setColor (Color.YELLOW);
                    if (m_RulerStartPoint != null)
                        jg.drawLine(m_RulerStartPoint, m_RulerEndPoint);

                    if (m_RulerEndPixel != null)
                    {
                        jg.setColor (new Color(0, 0, 0, 0.5f));
                        jg.fillRoundRect(m_RulerEndPixel.x, m_RulerEndPixel.y-15, 100+((Math.max(0,textToDisplay.length()-20))*5), 20, 5, 5);
                    }

                    jg.setColor (Color.YELLOW);
                    jg.drawString(textToDisplay, m_RulerEndPoint);
                }
            }
        }
    }

    /**
     * Sets the units to use for displaying the ruler distance
     *
     * @param unitsIndex
     */
    public void setRulerUnits(int unitsIndex)
    {
        m_RulerUnitsSelected = unitsIndex;

        if (m_RulerActivated)
        {
            m_PaintImmediately = true;
            jgeoRepaint();
        }
    }
    
    public void paintScale (JGeoGraphics jg)
    {   
        //jg.clear();
        
        synchronized (m_RulerLock)
        {
            int startPixelX = 10;
            int startPixelY = this.getHeight()-10;
            int pixelSideBufferMinX = 5;
            int pixelSideBufferMaxX = 50;
            int pixelSpanY = 50;
            int tickSize = 5;
            int metricTextOffset = -10;
            int englishTextOffset = 20;

            int boxSpanX = Math.max (m_ScaleEnglishScreenPixels, m_ScaleMetricScreenPixels) + pixelSideBufferMinX + pixelSideBufferMaxX;

            jg.setColor (new Color(0, 0, 0, 0.5f));
            jg.fillRoundRect(startPixelX, startPixelY-pixelSpanY, boxSpanX, pixelSpanY, pixelSideBufferMinX, pixelSideBufferMinX);

            jg.setColor (Color.YELLOW);
            jg.drawLine (startPixelX + pixelSideBufferMinX, startPixelY - pixelSpanY/2, startPixelX + pixelSideBufferMinX + Math.max (m_ScaleEnglishScreenPixels, m_ScaleMetricScreenPixels), startPixelY - pixelSpanY/2);
            jg.drawLine (startPixelX + pixelSideBufferMinX, startPixelY - pixelSpanY/2 - tickSize, startPixelX + pixelSideBufferMinX, startPixelY - pixelSpanY/2 + tickSize);
            jg.drawLine (startPixelX + pixelSideBufferMinX + m_ScaleMetricScreenPixels, startPixelY - pixelSpanY/2 - tickSize, startPixelX + pixelSideBufferMinX + m_ScaleMetricScreenPixels, startPixelY - pixelSpanY/2);
            jg.drawString(m_ScaleMetricDistance + m_ScaleMetricDistanceUnits, startPixelX + pixelSideBufferMinX + m_ScaleMetricScreenPixels, startPixelY - pixelSpanY/2 + metricTextOffset);
            jg.drawLine (startPixelX + pixelSideBufferMinX + m_ScaleEnglishScreenPixels, startPixelY - pixelSpanY/2 + tickSize, startPixelX + pixelSideBufferMinX + m_ScaleEnglishScreenPixels, startPixelY - pixelSpanY/2);
            jg.drawString(m_ScaleEnglishDistance + m_ScaleEnglishDistanceUnits, startPixelX + pixelSideBufferMinX + m_ScaleEnglishScreenPixels, startPixelY - pixelSpanY/2 + englishTextOffset);
        }
    }
    
    public void forceRepaint ()
    {
        m_PaintImmediately = true;
        jgeoRepaint();
    }
    
    @Override
    public void setProjectionType (int projectionType)
    {
        super.setProjectionType(projectionType);
        updateScale();
        forceRepaint();
    }

    public void updateScale ()
    {
        if (m_RulerLock == null)
            return;
        
        synchronized (m_RulerLock)
        {
            double desiredWindowRatioForScale = 0.15;

            //Total range at center of screen
            double totalRangeMetersX = this.getCurrentView().getHorizontalRange().getDoubleValue(Length.METERS);
            if (this.getProjectionType() == JGeoCanvas.MERCATOR_PROJECTION)
                totalRangeMetersX = this.getCurrentView().getHorizontalRange().getDoubleValue(Length.METERS) * Math.cos(this.getCurrentView().getCenter().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.RADIANS));
            double desiredRangeMetersX = desiredWindowRatioForScale * totalRangeMetersX;

            if (desiredRangeMetersX > 1000)
            {
                //Use large units, km and nm
                double actualRangeKm = MathUtils.getCleanValue (desiredRangeMetersX / MathConstants.KM2M);
                m_ScaleMetricDistance = actualRangeKm;
                m_ScaleMetricDistanceUnits = "km";
                m_ScaleMetricScreenPixels = (int)(Math.round(actualRangeKm*MathConstants.KM2M / totalRangeMetersX * this.getWidth()));

                double actualRangeNm = MathUtils.getCleanValue (desiredRangeMetersX / MathConstants.NM2M);
                m_ScaleEnglishDistance = actualRangeNm;
                m_ScaleEnglishDistanceUnits = "nm";
                m_ScaleEnglishScreenPixels = (int)(Math.round(actualRangeNm*MathConstants.NM2M / totalRangeMetersX * this.getWidth()));
            }
            else
            {
                //Use small units, m and ft
                double actualRangeMeters = MathUtils.getCleanValue (desiredRangeMetersX);
                m_ScaleMetricDistance = actualRangeMeters;
                m_ScaleMetricDistanceUnits = "m";
                m_ScaleMetricScreenPixels = (int)(Math.round(actualRangeMeters / totalRangeMetersX * this.getWidth()));

                double actualRangeFeet = MathUtils.getCleanValue (desiredRangeMetersX / MathConstants.FT2M);
                m_ScaleEnglishDistance = actualRangeFeet;
                m_ScaleEnglishDistanceUnits = "ft";
                m_ScaleEnglishScreenPixels = (int)(Math.round(actualRangeFeet*MathConstants.FT2M / totalRangeMetersX * this.getWidth()));
            }

            /*m_ScaleMetricDistance = 100;
            m_ScaleMetricDistanceUnits = "m";
            m_ScaleMetricScreenPixels = 300;
            m_ScaleEnglishDistance = 300;
            m_ScaleEnglishDistanceUnits = "ft";
            m_ScaleEnglishScreenPixels = 273;*/
            
            //If the scale changed, then the viewpoint must have changed.
            m_ViewChangedSinceRepaint = true;
        }

    }

    public void killImageryThread()
    {
        if (m_geoImageryFetcher != null)
        {
            m_geoImageryFetcher.killThreads();
            m_geoImageryFetcher = null;
        }
        if (m_tileRequestorThread != null)
        {
            m_tileRequestorThread.killThreads();
            m_tileRequestorThread = null;
        }
    }


    @Override
    public void geoImageLoaded(TileIdentifier tileIdentifier)
    {
//        LatLonAltPosition centerPosition = new LatLonAltPosition(new Latitude(centerLatitude_deg, Angle.DEGREES),
//                                                                 new Longitude(centerLongitude_deg, Angle.DEGREES),
//                                                                 Altitude.ZERO);
//        TileIdentifier tileIdentifier = new TileIdentifier();
//        tileIdentifier.image = image;
//        tileIdentifier.centerPosition = centerPosition;
//        tileIdentifier.width = new Length(imageWidth_m, Length.METERS);
//        tileIdentifier.height = new Length(imageHeight_m, Length.METERS);

        synchronized (this)
        {
            if (m_zoomLevelRequestedInteger == m_zoomLevelActual)
            {
                m_tileList.add(tileIdentifier);
            }
            else
            {
                m_loadingTileList.add(tileIdentifier);
            }
            
            m_PaintImmediately = true;
            jgeoRepaint();
            
//            if (!m_tileList.containsKey(zoomLevel))
//            {
//                m_tileList.put(zoomLevel, new HashSet<TileIdentifier>());
//            }
//
//            m_tileList.get(zoomLevel).add(tileIdentifier);
        }
    }

    @Override
    public void geoImageUnloaded(TileIdentifier tileIdentifier)
    {
//        LatLonAltPosition centerPosition = new LatLonAltPosition(new Latitude(centerLatitude_deg, Angle.DEGREES),
//                                                                 new Longitude(centerLongitude_deg, Angle.DEGREES),
//                                                                 Altitude.ZERO);
//        TileIdentifier tileIdentifier = new TileIdentifier();
//        tileIdentifier.image = null;
//        tileIdentifier.centerPosition = centerPosition;
//        tileIdentifier.width = new Length(imageWidth_m, Length.METERS);
//        tileIdentifier.height = new Length(imageHeight_m, Length.METERS);

//        synchronized (this)
//        {
//            if (m_zoomLevelRequestedInteger == m_zoomLevelActual)
//            {
//                m_tileList.remove(tileIdentifier);
//            }
//        }
    }
    
    @Override
    public void geoTileJobFinished()
    {
        if (m_loadingTileList.size() > 0 && m_zoomLevelRequestedInteger != m_zoomLevelActual)
        {
            int curZoom = m_zoomLevelRequestedInteger <= m_MaxZoomLevel ? m_zoomLevelRequestedInteger : m_MaxZoomLevel;
            
            Object[] tileArr = m_loadingTileList.toArray();
            int zl = 0;
            for (int i = 0; i < tileArr.length; i++)
            {
                TileIdentifier tile = (TileIdentifier) tileArr[i];
                zl = tile.zoomLevel;
                if (tile.zoomLevel != curZoom)
                {
                    m_loadingTileList.remove(tile);
                }
            }
            
            if (m_loadingTileList.size() > 0)
            {
                m_tileList = m_loadingTileList;
                m_loadingTileList = new HashSet<TileIdentifier>();
                m_zoomLevelActual = curZoom;

                m_PaintImmediately = true;
                jgeoRepaint();
                
                if (VERBOSE)
                    System.out.println("tiles updated");
            }
            else
            {
                if (VERBOSE)
                {
                    System.out.println("skipped paint on tile update");
                    System.out.printf("requested zoom=%d, curZoom=%d, last tile level=%d\n", m_zoomLevelRequestedInteger, curZoom, zl);
                }
            }
        }
        else
        {
            if (VERBOSE)
                System.out.println("tile job finished, but no tiles in queue, in thread "  + Thread.currentThread().getName());
        }
    }
    
    @Override
    public void geoNeedsZoomOut()
    {
        int old = m_zoomLevelRequestedInteger;
        if (m_zoomLevelRequestedInteger > m_MaxZoomLevel)
            m_zoomLevelRequestedInteger = m_MaxZoomLevel - 1;
        else if (m_zoomLevelRequestedInteger <= m_MinZoomLevel + 1)
            m_zoomLevelRequestedInteger = m_MinZoomLevel;
        else
            m_zoomLevelRequestedInteger -= 1;
        
        if (VERBOSE)
            System.out.printf("[%s]: found invalid tile, autozooming from %d to %d\n", Thread.currentThread().getName(), old, m_zoomLevelRequestedInteger);
        
        m_geoImageryFetcher.fetchRegion(m_topLatitude_deg, m_leftLongitude_deg, m_bottomLatitude_deg, m_rightLongitude_deg, m_zoomLevelRequestedInteger);
        
        m_loadingTileList.clear();
    }

    public void mousePressed(JGeoMouseEvent event)
    {
        m_mousePrevPosition = event.getPosition();
        m_MousePrevPixel = new Cell (event.getX(), event.getY());
    }

    public void mouseReleased(JGeoMouseEvent event)
    {
        m_mousePrevPosition = null;
        m_MousePrevPixel = null;
    }

    public void mouseEntered(JGeoMouseEvent event)
    {
    }

    public void mouseExited(JGeoMouseEvent event)
    {
    }

    public void mouseClicked(JGeoMouseEvent event)
    {
        long currMouseClickMs = System.currentTimeMillis();
        if (currMouseClickMs - m_LastMouseClickMs < m_DoubleClickSpeedMs && !m_RulerActivated)
        {
            boolean zoomIn = false;
            //This was a double click
            if (event.getButton() == JGeoMouseEvent.BUTTON1)
            {
                //zoom in for left click
                zoomIn = true;
            }
            
            if (m_geoImageryFetcher != null)
            {
                if (zoomIn && !canDoZoomIn())
                    return;
                if (!zoomIn && !canDoZoomOut())
                    return;
            }

            synchronized (this)
            {
                if (zoomIn)
                    zoomInAction.actionPerformed(null);
                else
                    zoomOutAction.actionPerformed(null);
            }

            if (m_RulerActivated)
            {
                synchronized (m_RulerLock)
                {
                    if (m_RulerActivated && m_RulerEndPoint != null)
                    {
                        Point newRulerEndPixel = this.getCurrentView().earthToMouse(m_RulerEndPoint);
                        m_RulerEndPixel = new Cell ((int)newRulerEndPixel.getX(), (int)newRulerEndPixel.getY());
                    }
                }
            }

            updateScale ();
            m_PaintImmediately = true;
        
            //Prevent a triple click from being 2 double clicks
            m_LastMouseClickMs = -1;
        }
        else
        {
            m_LastMouseClickMs = currMouseClickMs;
        }


        if (m_RulerActivated)
        {
            synchronized (m_RulerLock)
            {
                if (m_RulerActivated && (!m_DraggingRuler || m_RulerStartPoint == null))
                {
                    m_RulerStartPoint = event.getLatLonAltPosition();
                    m_RulerEndPoint = event.getLatLonAltPosition();
                    m_RulerEndPixel = new Cell (event.getX(), event.getY());
                    m_RulerRange = m_RulerStartPoint.getRangeTo(m_RulerEndPoint);
                    m_DraggingRuler = true;

                    m_PaintImmediately = true;
                    jgeoRepaint();
                }
                else if(m_RulerActivated && m_DraggingRuler)
                {
                    m_RulerEndPoint = event.getLatLonAltPosition();
                    m_RulerEndPixel = new Cell (event.getX(), event.getY());
                    m_RulerRange = m_RulerStartPoint.getRangeTo(m_RulerEndPoint);
                    m_DraggingRuler = false;

                    m_PaintImmediately = true;
                    jgeoRepaint();
                }

            }
        }
    }

    public void mouseDragged(JGeoMouseEvent event)
    {
        synchronized (this)
        {
            if (m_mousePrevPosition != null)
            {
                RangeBearingHeightOffset mouseDisplacement = m_mousePrevPosition.getRangeBearingHeightOffsetFrom(event.getPosition());
                //if (mouseDisplacement.getRange().isGreaterThan(new Length(250, Length.METERS)))
               // {
                    //System.out.println ("Old View center: " + getViewCenter());
                    //System.out.println ("To View center: " + getViewCenter().translatedBy(mouseDisplacement));
                    //setViewCenter(getViewCenter().translatedBy(mouseDisplacement));
                    LatLonAltPosition oldCenter = getViewCenter();
                    LatLonAltPosition newCenter = getViewCenter().translatedBy(mouseDisplacement).asLatLonAltPosition();
                    if (newCenter.getLatitude().getDoubleValue(Angle.DEGREES) > 82)
                        newCenter = new LatLonAltPosition(new Latitude (82, Angle.DEGREES), newCenter.getLongitude(), newCenter.getAltitude());
                    else if (newCenter.getLatitude().getDoubleValue(Angle.DEGREES) < -82)
                        newCenter = new LatLonAltPosition(new Latitude (-82, Angle.DEGREES), newCenter.getLongitude(), newCenter.getAltitude());
                    setViewCenter(newCenter);
                    //System.out.println ("Pan: " + mouseDisplacement.toString());
                    //System.out.println ("New View center: " + getViewCenter());
                    m_mousePrevPosition = event.getPosition().translatedBy(mouseDisplacement);

                    updateScale ();
                    jgeoRepaint();

               // }
            }
        }
        
         synchronized (m_RulerLock)
        {
            if (m_RulerEndPixel != null)
            {
                //Bug where dragged message shows up before clicked message sometimes?
                if (m_MousePrevPixel == null)
                {
                    m_MousePrevPixel = new Cell (event.getX(), event.getY());
                }
                
                int xDiff = event.getX() - m_MousePrevPixel.x;
                int yDiff = event.getY() - m_MousePrevPixel.y;

                m_RulerEndPixel.x += xDiff;
                m_RulerEndPixel.y += yDiff;

                m_MousePrevPixel.x = event.getX();
                m_MousePrevPixel.y = event.getY();

            }
        }
    }

    public void mouseMoved(JGeoMouseEvent event)
    {
        m_MousePosition = event.getLatLonAltPosition();
        m_LastMouseClickMs = -1;
         
        if (m_RulerActivated)
        {
            boolean updated = false;
            synchronized (m_RulerLock)
            {
                //if (m_RulerActivated && m_DraggingRuler && m_RulerStartPoint != null)
                if (m_RulerActivated)
                {
                    if (m_DraggingRuler || m_RulerStartPoint == null)
                    {
                        m_RulerEndPoint = event.getLatLonAltPosition();
                        m_RulerEndPixel = new Cell (event.getX(), event.getY());
                    }
                    
                    if (m_RulerStartPoint != null)
                        m_RulerRange = m_RulerStartPoint.getRangeTo(m_RulerEndPoint);
                    else
                        m_RulerRange = Length.ZERO;
                    updated = true;
                }
            }

            if (updated)
            {
                m_PaintImmediately = true;
                jgeoRepaint();
            }
        }
    }

    @Override
    public boolean canDoZoomIn()
    {
        if (m_geoImageryFetcher != null)
        {
            //if (m_zoomLevel >= m_MaxZoomLevel)
             //   return false;
        }
        return true;
    }
    
    @Override
    public void zoomInDone(int zoomSteps)
    {
        zoomDone(zoomSteps);
    }

    @Override
    public boolean canDoZoomOut()
    {
        if (m_geoImageryFetcher != null)
        {
            if (m_zoomLevelRequestedDouble - m_ZoomStepFraction < m_MinZoomLevel)
                return false;
        }
        return true;
    }

    @Override
    public void zoomOutDone(int zoomSteps)
    {
        zoomDone(-zoomSteps);
    }
    
    private void zoomDone(int signedZoomSteps)
    {
        updateScale ();
        m_PaintImmediately = true;
        m_zoomLevelRequestedDouble += signedZoomSteps*m_ZoomStepFraction;
        m_zoomLevelRequestedInteger = (int)m_zoomLevelRequestedDouble;
        
        if (m_RulerActivated)
        {
            synchronized (m_RulerLock)
            {
                if (m_RulerActivated && m_RulerEndPoint != null)
                {
                    Point newRulerEndPixel = this.getCurrentView().earthToMouse(m_RulerEndPoint);
                    m_RulerEndPixel = new Cell ((int)newRulerEndPixel.getX(), (int)newRulerEndPixel.getY());
                }
            }
        }
    }

    long lastWheelMovedProc = -1;
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int notchesMoved = e.getWheelRotation();
        if (e.getWhen() <= lastWheelMovedProc)
            return;
        lastWheelMovedProc = e.getWhen();

        if (m_geoImageryFetcher != null)
        {
            if (notchesMoved < 0 && !canDoZoomIn())
                return;
            if (notchesMoved > 0 && !canDoZoomOut())
                return;
        }

        synchronized (this)
        {
            for (int ctr = 0; ctr < notchesMoved; ctr++)
                zoomOut();
            for (int ctr = 0; ctr > notchesMoved; ctr--)
                zoomIn();

        }
        
        if (m_RulerActivated)
        {
            synchronized (m_RulerLock)
            {
                if (m_RulerActivated && m_RulerEndPoint != null)
                {
                    Point newRulerEndPixel = this.getCurrentView().earthToMouse(m_RulerEndPoint);
                    m_RulerEndPixel = new Cell ((int)newRulerEndPixel.getX(), (int)newRulerEndPixel.getY());
                }
            }
        }
        
        updateScale ();
        m_PaintImmediately = true;
    }
    
    public void zoomIn ()
    {
        zoomInAction.actionPerformed(null);
    }

    public void zoomOut()
    {
        zoomOutAction.actionPerformed(null);
    }
    
    @Override
    public void setSize (int w, int h)
    {
        super.setSize(w, h);
        
        updateSize ();
    }
    
    @Override
    public void setSize (Dimension newSize)
    {
        super.setSize(newSize);

        updateSize ();
    }
    
    public void updateSize ()
    {
        if (m_RulerActivated)
        {
            synchronized (m_RulerLock)
            {
                if (m_RulerActivated && m_RulerEndPoint != null)
                {
                    Point newRulerEndPixel = this.getCurrentView().earthToMouse(m_RulerEndPoint);
                    m_RulerEndPixel = new Cell ((int)newRulerEndPixel.getX(), (int)newRulerEndPixel.getY());
                }
            }
        }
        
        if (!m_ForceUpdateSizeNextPaint)
            m_ForceUpdateSizeNextPaint = true;
        
        updateScale ();
        forceRepaint();
    }

     /**
     * Sets flag whether to use ruler tool in the canvas
     *
     * @param flag
     */
    public void setRulerActivated(boolean flag)
    {
        synchronized (m_RulerLock)
        {
            m_RulerActivated = flag;

            m_RulerStartPoint = null;
            m_RulerEndPoint = null;
            m_RulerEndPixel = null;

            //jgeoRepaint();
            //m_PaintImmediately = true;
        }
    }
    
    public class TileRequestorThread extends Thread
    {
        private int m_prevZoomLevel;
        private GeoImageryCanvas m_parent;
        private boolean m_KillThread;

        public TileRequestorThread(final GeoImageryCanvas parent)
        {
            this.setName ("GeoImgCanvas-TileRequestorThread");
            //this.setPriority(Thread.MIN_PRIORITY);
            m_parent = parent;
            m_KillThread = false;
        }
        
        public void killThreads()
        {
            m_KillThread = true;
        }

        @Override
        public void run()
        {
            while (!m_KillThread)
            {
                ViewTransform viewTransform = getCurrentView();
                if (viewTransform != null)
                {
                    LatLonAltPosition topLeftPosition = viewTransform.mouseToEarth(new Point(0, 0)).asLatLonAltPosition();
                    LatLonAltPosition bottomRightPosition = viewTransform.mouseToEarth(new Point(getWidth(), getHeight())).asLatLonAltPosition();

                    double topLatitude_deg = topLeftPosition.getLatitude().getDoubleValue(Angle.DEGREES);
                    double bottomLatitude_deg = bottomRightPosition.getLatitude().getDoubleValue(Angle.DEGREES);
                    double leftLongitude_deg = topLeftPosition.getLongitude().getDoubleValue(Angle.DEGREES);
                    double rightLongitude_deg = bottomRightPosition.getLongitude().getDoubleValue(Angle.DEGREES);

                    int currZoomLevel;
                    synchronized (m_parent)
                    {
                        currZoomLevel = m_zoomLevelRequestedInteger;
                    }


                    if (topLatitude_deg != m_topLatitude_deg ||
                        bottomLatitude_deg != m_bottomLatitude_deg ||
                        leftLongitude_deg != m_leftLongitude_deg ||
                        rightLongitude_deg != m_rightLongitude_deg ||
                        m_prevZoomLevel != currZoomLevel)
                    {
                        m_topLatitude_deg = topLatitude_deg;
                        m_bottomLatitude_deg = bottomLatitude_deg;
                        m_leftLongitude_deg = leftLongitude_deg;
                        m_rightLongitude_deg = rightLongitude_deg;
                        m_zoomLevelRequestedInteger = Math.max(Math.min(m_zoomLevelRequestedInteger, m_MaxZoomLevel), m_MinZoomLevel);

                        m_geoImageryFetcher.fetchRegion(m_topLatitude_deg, m_leftLongitude_deg, m_bottomLatitude_deg, m_rightLongitude_deg, m_zoomLevelRequestedInteger);
                        
                        if (VERBOSE)
                            System.out.printf("fetching new region at zoom level %d\n", m_zoomLevelRequestedInteger);
                    }

                    m_prevZoomLevel = currZoomLevel;
                }

                try
                {
                    sleep(1000);
                }
                catch (Exception e)
                {
                }
            }
        }
    }
}
