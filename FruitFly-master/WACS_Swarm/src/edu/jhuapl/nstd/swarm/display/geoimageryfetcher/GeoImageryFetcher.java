package edu.jhuapl.nstd.swarm.display.geoimageryfetcher;

import net.virtualearth.dev.webservices.v1.common.MapStyle;
import net.virtualearth.dev.webservices.v1.imagery.MapUriRequest;
import net.virtualearth.dev.webservices.v1.imagery.MapUriResponse;
import com.google.code.bing.webservices.client.BingMapsWebServicesClientFactory;
import com.google.code.bing.webservices.client.imagery.BingMapsImageryServiceClient;
import com.google.code.bing.webservices.client.imagery.BingMapsImageryServiceClient.MapUriRequestBuilder;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class GeoImageryFetcher
{
    private static final String CREDENTIALS_STRING = "Ag9FWHcNfGTB9oGyuxvFJbPZpUtJWagLQQq5JlXRRab0BbLLHmk-Zz3KlqkmpoSU";
    //private static final String CREDENTIALS_STRING = "AgBXisHgZAEfpDnT95skGJiYu_Oh9XgeAi7O0UJfhg_GdEYB2yeeETJ8ayQ-3kNE";
    //private static final double TILE_SIDE_SIZE_ARCDEGREES = 0.05;//0.033333333; //two arc minutes
    private static final double TILE_SIDE_SIZE_ARCDEGREES[] = new double [20];/*{0,
                                                                409.6,        //zoomLevel = 1
                                                                204.8,        //zoomLevel = 2
                                                                102.4,        //zoomLevel = 3
                                                                51.2,        //zoomLevel = 4
                                                                25.6,        //zoomLevel = 5
                                                                12.8,        //zoomLevel = 6
                                                                6.4,        //zoomLevel = 7
                                                                3.2,        //zoomLevel = 8
                                                                1.6,        //zoomLevel = 9
                                                                0.8,        //zoomLevel = 10
                                                                0.4,        //zoomLevel = 11
                                                                0.2,        //zoomLevel = 12
                                                                0.1,        //zoomLevel = 13
                                                                0.05,        //zoomLevel = 14
                                                                0.025,        //zoomLevel = 15
                                                                0.0125,        //zoomLevel = 16
                                                                0.00625,        //zoomLevel = 17
                                                                0.003125,        //zoomLevel = 18
                                                                0.0015625,        //zoomLevel = 19
                                                                };*/
    private static final int BING_LOGO_HEIGHT_PIXELS = 28;
    private static final int BING_MIN_IMAGE_SIZE_PIXELS = 80;
    private static final int BING_MAX_IMAGE_SIZE_PIXELS = 834;
    //private BingMapsWebServicesClientFactory m_bingMapsWebSercviceClientfactory;
    //private BingMapsImageryServiceClient m_bingMapsImageryServiceClient;
    private String m_imageDirectory;
    private FetchThread m_fetchThread;
    private DownloadThread m_downloadThread;
    private GeoImageryHandler m_imageryHandler;
    private boolean m_useInternet;
    private boolean m_requestInProgress;
    private boolean m_needsSubmit;
    private boolean m_requestedZoomOut;
    private int m_currentZoomLevel;
    private Stack<TileIdentifier> m_tilesToDownload = new Stack<TileIdentifier>();
    private Stack<TileIdentifier> m_tilesToFetch = new Stack<TileIdentifier>();
    private Set<TileIdentifier> m_previouslyLoadedTiles = new HashSet<TileIdentifier>();
    private final Object m_tilesLock = new Object();


    interface GeoImageryHandler
    {
        public void geoImageLoaded(TileIdentifier tileIdentifier);

        public void geoImageUnloaded(TileIdentifier tileIdentifier);
        
        public void geoTileJobFinished();
        
        public void geoNeedsZoomOut();
    }

    private class FetchThread extends Thread
    {
        public FetchThread ()
        {
            this.setName ("GeoImgFetcher-FetchThread");
        }
        
        private boolean m_KillThread = false;

        public void FetchTile(final double centerLatitude_deg, final double centerLongitude_deg, final int zoomLevel)
        {
            TileIdentifier tileIdentifier = new TileIdentifier();
            tileIdentifier.centerLatitude_deg = centerLatitude_deg;
            if (centerLongitude_deg > 180)
                tileIdentifier.centerLongitude_deg = centerLongitude_deg - 360;
            else if (centerLongitude_deg < -180)
                tileIdentifier.centerLongitude_deg = centerLongitude_deg + 360;
            else
                tileIdentifier.centerLongitude_deg = centerLongitude_deg;
            tileIdentifier.centerPosition = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg, Angle.DEGREES),
                                                                 new Longitude(tileIdentifier.centerLongitude_deg, Angle.DEGREES),
                                                                 Altitude.ZERO);
            tileIdentifier.zoomLevel = zoomLevel;

            synchronized (m_tilesLock)
            {
                m_tilesToFetch.push(tileIdentifier);
            }
        }

        public void killThread ()
        {
            m_KillThread = true;
        }
        
        public void startRequest()
        {
            m_requestInProgress = true;
            m_needsSubmit = true;
        }
        
        public void stopRequest()
        {
            m_requestInProgress = false;
        }
        
        public void clearQueue()
        {
            m_tilesToFetch.clear();
        }

        @Override
        public void run()
        {
            TileIdentifier tileToFetch;

            while (true)
            {
                if (m_KillThread)
                    break;

                tileToFetch = null;

                int numTileRequestsIgnored = 0;

                boolean tilePreviouslyLoaded = true;

                // If this tile is already in memory, ignore the request
                synchronized (m_tilesLock)
                {
                    if (m_tilesToFetch.empty())
                    {
                        tileToFetch = null;
                        
                        if (!m_requestInProgress && m_needsSubmit && !m_downloadThread.hasDownloads())
                        {
                            m_imageryHandler.geoTileJobFinished();
                            m_needsSubmit = false;
                        }
                    }
                    else
                    {
                        tileToFetch = m_tilesToFetch.pop();
                    }
                }

                synchronized (m_tilesLock)
                {
                    if (tileToFetch != null)
                    {
                        tilePreviouslyLoaded = m_previouslyLoadedTiles.contains(tileToFetch);
                    }
                }
                
                while (tileToFetch != null && tilePreviouslyLoaded)
                {
                    if (m_tilesToFetch.empty())
                    {
                        tileToFetch = null;
                    }
                    else
                    {
                        tileToFetch = m_tilesToFetch.pop();
                    }
                    ++numTileRequestsIgnored;

                    // Don't hog the CPU if we get a large request
                    // that we already loaded.
                    if ((numTileRequestsIgnored % 100) == 0)
                    {
                        try
                        {
                            sleep(50);
                        }
                        catch (Exception e)
                        {
                        }
                    }

                    synchronized (m_tilesLock)
                    {
                        if (tileToFetch != null)
                        {
                            tilePreviouslyLoaded = m_previouslyLoadedTiles.contains(tileToFetch);
                        }
                    }
                }


                if (tileToFetch != null)
                {
                    String filename = tileToFetch.getFilename(m_imageDirectory);

                    File imageFile = new File(filename);
                    if (imageFile.exists())
                    {
                        try
                        {
                            if (m_imageryHandler != null)
                            {
                                double tileVerticalSideLength_m = calculateTileVerticalSideLengthPaint_m(tileToFetch, tileToFetch.zoomLevel);
                                double tileHorizontalSideLength_m = calculateTileHorizontalSideLengthPaint_m(tileToFetch, tileToFetch.zoomLevel);
                                BufferedImage image = ImageIO.read(imageFile);
                                
                                if (checkValid(image))
                                {
                                    tileToFetch.height = new Length(tileVerticalSideLength_m, Length.METERS);
                                    tileToFetch.width = new Length(tileHorizontalSideLength_m, Length.METERS);
                                    tileToFetch.image = image;

                                    m_imageryHandler.geoImageLoaded(tileToFetch);
                                }
                                else
                                {
                                    if (!m_requestedZoomOut && tileToFetch.zoomLevel == m_currentZoomLevel)
                                    {
                                        m_requestedZoomOut = true;
                                        m_imageryHandler.geoNeedsZoomOut();
                                    }
                                    m_tilesToFetch.clear();
                                    continue;
                                }
                            }

                            synchronized (m_tilesLock)
                            {
                                m_previouslyLoadedTiles.add(tileToFetch);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }                        
                    }
                    else if (m_useInternet)
                    {
                        m_downloadThread.DownloadTile(tileToFetch);
                    }
                }
                else
                {
                    try
                    {
                        sleep(500);
                    }
                    catch (Exception e)
                    {                        
                    }
                }
            }
        }
    }

    private class DownloadThread extends Thread
    {
        public DownloadThread ()
        {
            this.setName ("GeoImgFetcher-DownloadThread");
        }
        
        private boolean m_KillThread = false;

        public void DownloadTile(final TileIdentifier tileIdentifier)
        {
            synchronized (m_tilesLock)
            {
                if (m_tilesToDownload.contains(tileIdentifier))
                    return;
                m_tilesToDownload.push(tileIdentifier);
            }
        }

        public void killThread ()
        {
            m_KillThread = true;
        }

        @Override
        public void run()
        {
            TileIdentifier tileToDownload;
            
            while (true)
            {
                if (m_KillThread)
                    break;

                tileToDownload = null;
                synchronized (m_tilesLock)
                {
                    if (m_tilesToDownload.empty())
                    {
                        tileToDownload = null;
                    }
                    else
                    {
                        tileToDownload = m_tilesToDownload.pop();
                    }
                }

                if (tileToDownload != null)
                {
                    try
                    {
                        double tileVerticalSideLengthDwnld_m = calculateTileVerticalSideLengthDownload_m(tileToDownload, tileToDownload.zoomLevel);
                        double tileHorizontalSideLengthDwnld_m = calculateTileHorizontalSideLengthDownload_m(tileToDownload, tileToDownload.zoomLevel);
                        double tileVerticalSideLengthPaint_m = calculateTileVerticalSideLengthPaint_m(tileToDownload, tileToDownload.zoomLevel);
                        double tileHorizontalSideLengthPaint_m = calculateTileHorizontalSideLengthPaint_m(tileToDownload, tileToDownload.zoomLevel);
                        double tileResolution_pixelsPerMeter = (1 / calculateTileResolution_metersPerPixel((float)tileToDownload.centerLatitude_deg, tileToDownload.zoomLevel));
                        int tileVerticalSideSize_pixels = (int)(tileVerticalSideLengthDwnld_m * tileResolution_pixelsPerMeter);
                        int tileHorizontalSideSize_pixels = (int)(tileHorizontalSideLengthDwnld_m * tileResolution_pixelsPerMeter);

                        BufferedImage tileImage = downloadTile(tileToDownload.centerLatitude_deg,
                                                               tileToDownload.centerLongitude_deg,
                                                               tileToDownload.zoomLevel,
                                                               tileHorizontalSideSize_pixels,
                                                               tileVerticalSideSize_pixels);

                        FileOutputStream fileOutputStream = new FileOutputStream(tileToDownload.getFilename(m_imageDirectory));
                        ImageIO.write(tileImage, "jpg", fileOutputStream);
                        fileOutputStream.close();

                        if (checkValid(tileImage))
                        {
                            tileToDownload.height = new Length(tileVerticalSideLengthPaint_m, Length.METERS);
                            tileToDownload.width = new Length(tileHorizontalSideLengthPaint_m, Length.METERS);
                            tileToDownload.image = tileImage;

                            if (m_imageryHandler != null)
                            {
                                m_imageryHandler.geoImageLoaded(tileToDownload);
                            }

                            synchronized (m_tilesLock)
                            {
                                m_previouslyLoadedTiles.add(tileToDownload);
                            }
                        }
                        else
                        {
                            if (!m_requestedZoomOut && tileToDownload.zoomLevel == m_currentZoomLevel)
                            {
                                m_requestedZoomOut = true;
                                m_imageryHandler.geoNeedsZoomOut();
                            }
                            m_tilesToDownload.clear();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        sleep(500);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        protected boolean hasDownloads()
        {
            return m_tilesToDownload.size() > 0;
        }
        
        protected void clearQueue()
        {
            m_tilesToDownload.clear();
        }
    }


    public GeoImageryFetcher(final String imageDirectory, final GeoImageryHandler geoImageryHandler, final boolean useInternet)
    {
        m_imageDirectory = imageDirectory;
        if (!m_imageDirectory.endsWith("/") && !m_imageDirectory.endsWith("\\"))
        {
            m_imageDirectory = m_imageDirectory.concat("/");
        }
        
        File imgDir = new File (m_imageDirectory);
        if (!imgDir.exists())
            imgDir.mkdirs();
        
        m_imageryHandler = geoImageryHandler;
        m_useInternet = useInternet;
        generate_TILE_SIDE_SIZE_ARCDEGREES ();
        
        m_requestInProgress = false;
        m_needsSubmit = false;
        m_requestedZoomOut = false;
        m_currentZoomLevel = -1;

        if (useInternet)
        {
            try
            {
                //m_bingMapsWebSercviceClientfactory = BingMapsWebServicesClientFactory.newInstance();
                //m_bingMapsImageryServiceClient = m_bingMapsWebSercviceClientfactory.createImageryServiceClient();
                
                URL testURL = new URL ("https://dev.virtualearth.net/");
                InputStream testStream = testURL.openStream();
            }
            catch (Exception e)
            {
                //could not connect to image client
                //m_bingMapsImageryServiceClient = null;
                
                JOptionPane.showMessageDialog(null, "Could not connect to imagery server, disabling imagery downloads", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (Error e)
            {
                //could not connect to image client
                //m_bingMapsImageryServiceClient = null;
                
                JOptionPane.showMessageDialog(null, "Could not connect to imagery server, disabling imagery downloads", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        m_fetchThread = new FetchThread();
        m_fetchThread.setDaemon(true);
        m_fetchThread.start();

        if (useInternet)// && m_bingMapsImageryServiceClient != null)
        {
            m_downloadThread = new DownloadThread();
            m_downloadThread.setDaemon(true);
            m_downloadThread.start();
        }
        else
        {
            m_downloadThread = null;
        }
    }

    public void killThreads ()
    {
        if (m_fetchThread != null)
            m_fetchThread.killThread();
        if (m_downloadThread != null)
            m_downloadThread.killThread();
    }

    private void generate_TILE_SIDE_SIZE_ARCDEGREES ()
    {
        TILE_SIDE_SIZE_ARCDEGREES[0] = 0; //Not using zoom level 0;

        int fixedZoomLevel = 14;
        //double fixedZoomSize = 0.05;
        double fixedZoomSize = 0.033333333;

        TILE_SIDE_SIZE_ARCDEGREES[fixedZoomLevel] = fixedZoomSize;
        for (int i = fixedZoomLevel+1; i < TILE_SIDE_SIZE_ARCDEGREES.length; i ++)
        {
            TILE_SIDE_SIZE_ARCDEGREES[i] = TILE_SIDE_SIZE_ARCDEGREES[i-1]/2;
        }
        for (int i = fixedZoomLevel-1; i > 0; i --)
        {
            TILE_SIDE_SIZE_ARCDEGREES[i] = TILE_SIDE_SIZE_ARCDEGREES[i+1]*2;
        }
    }
    
//    public void setTileBufferSize(int bufferSize)
//    {
//        m_tileBuffer = bufferSize;
//    }

    private double calculateTileVerticalSideLengthDownload_m(final TileIdentifier tileIdentifier, int zoomLevel)
    {
        LatLonAltPosition latitude1 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg + TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                            new Longitude(tileIdentifier.centerLongitude_deg, Angle.DEGREES),
                                                            Altitude.ZERO);
        LatLonAltPosition latitude2 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg - TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                            new Longitude(tileIdentifier.centerLongitude_deg, Angle.DEGREES),
                                                            Altitude.ZERO);
        return latitude1.getRangeTo(latitude2).getDoubleValue(Length.METERS) * 1.01;
        
        /*double rangeM = TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]*MathConstants.DEG2M*1.01;
        return rangeM;*/
    }
	
    private double calculateTileHorizontalSideLengthDownload_m(final TileIdentifier tileIdentifier, int zoomLevel)
    {
        double lowerLongitude = tileIdentifier.centerLongitude_deg - TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2;
        if (lowerLongitude < -180)
            lowerLongitude += 360;
        double upperLongitude = tileIdentifier.centerLongitude_deg + TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2;
        if (upperLongitude > 180)
            upperLongitude -= 360;


        LatLonAltPosition longitude1 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg, Angle.DEGREES),
                                                             new Longitude(upperLongitude, Angle.DEGREES),
                                                             Altitude.ZERO);
        LatLonAltPosition longitude2 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg, Angle.DEGREES),
                                                             new Longitude(lowerLongitude, Angle.DEGREES),
                                                             Altitude.ZERO);
        return longitude1.getRangeTo(longitude2).getDoubleValue(Length.METERS) * 1.01;
        
        /*double rangeM = TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]*MathConstants.DEG2M*1.01;
        return rangeM;*/
        
    }

    private double calculateTileVerticalSideLengthPaint_m(final TileIdentifier tileIdentifier, int zoomLevel)
    {
        /*LatLonAltPosition latitude1 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg + TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                            new Longitude(tileIdentifier.centerLongitude_deg, Angle.DEGREES),
                                                            Altitude.ZERO);
        LatLonAltPosition latitude2 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg - TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                            new Longitude(tileIdentifier.centerLongitude_deg, Angle.DEGREES),
                                                            Altitude.ZERO);
        return latitude1.getRangeTo(latitude2).getDoubleValue(Length.METERS) * 1.01;*/

        double rangeM = TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]*MathConstants.DEG2M*1.01;
        return rangeM;
    }

    private double calculateTileHorizontalSideLengthPaint_m(final TileIdentifier tileIdentifier, int zoomLevel)
    {
        /*LatLonAltPosition longitude1 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg, Angle.DEGREES),
                                                             new Longitude(tileIdentifier.centerLongitude_deg + TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                             Altitude.ZERO);
        LatLonAltPosition longitude2 = new LatLonAltPosition(new Latitude(tileIdentifier.centerLatitude_deg, Angle.DEGREES),
                                                             new Longitude(tileIdentifier.centerLongitude_deg - TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2, Angle.DEGREES),
                                                             Altitude.ZERO);
        return longitude1.getRangeTo(longitude2).getDoubleValue(Length.METERS) * 1.01;*/

        double rangeM = TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]*MathConstants.DEG2M*1.01;
        return rangeM;

    }
    
    private boolean checkValid(BufferedImage img)
    {
        double matches = 0;
        double total = 0;

        for (int row = 0; row < img.getHeight() * 0.05; row++)
        {
            for (int i = 0; i < img.getWidth(); i++)
            {
                try
                {
                    int rgb = img.getRGB(row, i) & 0xFFFFFF;
                    total++;

                    int red =   (rgb >> 16) & 0xFF;
                    int green = (rgb >>  8) & 0xFF;
                    int blue =  (rgb      ) & 0xFF;

                    if (Math.abs(red - 0xF5) < 2 && Math.abs(green - 0xF2) < 2 && Math.abs(blue - 0xED) < 5)
                    {
                        matches++;
                    }
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    // Ignore this pixel
                }
            }
        }
        
        return (matches / total < 0.8);
    }

    public int fetchRegion(final double northernmostLatitude_deg,
                            final double easternmostLongitude_deg,
                            final double southernmostLatitude_deg,
                            final double westernmostLongitude_deg,
                            final int zoomLevel)
    {
        m_currentZoomLevel = zoomLevel;
        
        final LatLonAltPosition startPosition = getCenterPositionOfTileContaining(northernmostLatitude_deg, easternmostLongitude_deg, zoomLevel);
        final LatLonAltPosition endPosition = getCenterPositionOfTileContaining(southernmostLatitude_deg, westernmostLongitude_deg, zoomLevel);

        double startLatitude_deg = startPosition.getLatitude().getDoubleValue(Angle.DEGREES);
        final double startLongitude_deg = startPosition.getLongitude().getDoubleValue(Angle.DEGREES);
        double endLatitude_deg = endPosition.getLatitude().getDoubleValue(Angle.DEGREES);
        final double endLongitude_deg = endPosition.getLongitude().getDoubleValue(Angle.DEGREES);

        if (startLatitude_deg > endLatitude_deg)
        {
            double temp = endLatitude_deg;
            endLatitude_deg = startLatitude_deg;
            startLatitude_deg = temp;
        }

        final double latitudeDelta_deg = startLatitude_deg - endLatitude_deg;
        double longitudeDelta_deg = endLongitude_deg - startLongitude_deg;
        if (endLongitude_deg < startLongitude_deg)
        {
            longitudeDelta_deg += 360;
        }
        final int numHorizontalTiles = 1+(int)Math.ceil(Math.abs(longitudeDelta_deg / TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]));
        final int numVerticalTiles = 1+(int)Math.ceil(Math.abs(latitudeDelta_deg / TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]));

        synchronized (m_tilesLock)
        {
            Set<TileIdentifier> tilesInThisRegion = new HashSet<TileIdentifier>();
            /*double currLongitude_deg = startLongitude_deg + (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] * ((numHorizontalTiles / 2) - 1));
            for (int i = 0; i < numHorizontalTiles; ++i)
            {
                double currLatitude_deg = startLatitude_deg + (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] * ((numVerticalTiles / 2) - 1));
                for (int j = 0; j < numVerticalTiles; ++j)
                {
                    m_fetchThread.FetchTile(currLatitude_deg, currLongitude_deg, zoomLevel);

                    TileIdentifier tileIdentifier = new TileIdentifier();
                    tileIdentifier.centerLatitude_deg = currLatitude_deg;
                    tileIdentifier.centerLongitude_deg = currLongitude_deg;
                    System.out.println ("add:" + tileIdentifier.centerLatitude_deg + "," + tileIdentifier.centerLongitude_deg);
                    tileIdentifier.zoomLevel = zoomLevel;
                    tilesInThisRegion.add(tileIdentifier);
                    //System.out.println ("tiles in region: z=" + zoomLevel + "; lat=" + currLatitude_deg + "; lon=" + currLongitude_deg);

                    int rowNum;
                    int midpoint = numVerticalTiles / 2 - 1;
                    if (j % 2 == 0)
                    {
                        rowNum = midpoint + ((j / 2) + 1);
                    }
                    else
                    {
                        rowNum = midpoint - ((j / 2) + 1);
                    }
                    currLatitude_deg = startLatitude_deg + (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] * rowNum);
                }

                int midpoint = numHorizontalTiles / 2 - 1;
                int columnNum;               
                if (i % 2 == 0)
                {
                    columnNum = midpoint + ((i / 2) + 1);
                }
                else
                {
                    columnNum = midpoint - ((i / 2) + 1);
                }
                currLongitude_deg = startLongitude_deg + (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] * columnNum);
            }*/

            m_requestedZoomOut = false;
            
            if (m_fetchThread != null)
                m_fetchThread.clearQueue();
            if (m_downloadThread != null)
                m_downloadThread.clearQueue();
            
            m_fetchThread.startRequest();
            for (int i = 0; i < numHorizontalTiles; ++i)
            {
                double currLongitude_deg = startLongitude_deg + i*TILE_SIDE_SIZE_ARCDEGREES[zoomLevel];

                for (int j = 0; j < numVerticalTiles; ++j)
                {
                    double currLatitude_deg = startLatitude_deg + j*TILE_SIDE_SIZE_ARCDEGREES[zoomLevel];
                    m_fetchThread.FetchTile(currLatitude_deg, currLongitude_deg, zoomLevel);

                    TileIdentifier tileIdentifier = new TileIdentifier();
                    tileIdentifier.centerLatitude_deg = currLatitude_deg;
                    tileIdentifier.centerLongitude_deg = currLongitude_deg;
                    tileIdentifier.zoomLevel = zoomLevel;
                    tilesInThisRegion.add(tileIdentifier);


                    //System.out.println ("tiles in region: z=" + zoomLevel + "; lat=" + currLatitude_deg + "; lon=" + currLongitude_deg);
                }
            }
            m_fetchThread.stopRequest();
            
            //
            // Determine which tiles to unload
            //
            Iterator<TileIdentifier> previouslyLoadedTileIterator = m_previouslyLoadedTiles.iterator();
            while (previouslyLoadedTileIterator.hasNext())
            {
                TileIdentifier tileIdentifier = previouslyLoadedTileIterator.next();
                if (!tilesInThisRegion.contains(tileIdentifier))
                {
//                    double tileVerticalSideLength_m = calculateTileVerticalSideLengthPaint_m(tileIdentifier, tileIdentifier.zoomLevel);
//                    double tileHorizontalSideLength_m = calculateTileHorizontalSideLengthPaint_m(tileIdentifier, tileIdentifier.zoomLevel);
                    m_imageryHandler.geoImageUnloaded(tileIdentifier);
                    previouslyLoadedTileIterator.remove();
                }
            }

            return tilesInThisRegion.size();
        }
    }


    //
    // The resolution of the tiles varies with latitude and zoom level
    //
    private float calculateTileResolution_metersPerPixel(final float latitude_deg, final int zoomLevel)
    {
        return 156543.04f * (float)Math.cos(Math.toRadians(latitude_deg)) / (float)Math.pow(2, zoomLevel);
        //return 156543.04f * (float)Math.cos(0) / (float)Math.pow(2, zoomLevel);
    }

    public int getZoomLevelFromPrefReqMetersPerPixel(double latitude_deg, double prefReqXMetersPerPixel)
    {
        int zoomLevel = (int)(Math.round(MathUtils.log2(156543.04f * (float)Math.cos(Math.toRadians(latitude_deg)) / prefReqXMetersPerPixel)));
        return zoomLevel;
    }


    private LatLonAltPosition getCenterPositionOfTileContaining(final double latitude_deg,
                                                                final double longitude_deg,
                                                                final int zoomLevel)
    {
        double northernmostLatitude_deg = Math.ceil(latitude_deg / TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]) * TILE_SIDE_SIZE_ARCDEGREES[zoomLevel];
        double westernmostLongitude_deg = Math.floor(longitude_deg / TILE_SIDE_SIZE_ARCDEGREES[zoomLevel]) * TILE_SIDE_SIZE_ARCDEGREES[zoomLevel];
        if (westernmostLongitude_deg < -180)
        {
            westernmostLongitude_deg += 360;
        }

        double centerLatitude_deg = northernmostLatitude_deg - (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2);
        double centerLongitude_deg = westernmostLongitude_deg + (TILE_SIDE_SIZE_ARCDEGREES[zoomLevel] / 2);
        
        return new LatLonAltPosition(new Latitude(centerLatitude_deg, Angle.DEGREES),
                                     new Longitude(centerLongitude_deg, Angle.DEGREES),
                                     Altitude.ZERO);
    }

    private BufferedImage downloadTile(final double centerLatitude_deg,
                                       final double centerLongitude_deg,
                                       int zoomLevel,
                                       int imageWidth_pixels,
                                       int imageHeight_pixels) throws Exception
    {
        //If image height is so large that it's more than double the expected size, get a zoomed out image and use it instead
        int zoomDivisor = 0;
        while (imageHeight_pixels > BING_MAX_IMAGE_SIZE_PIXELS*Math.pow(2,zoomDivisor))
            zoomDivisor ++;

        int origZoomLevel = zoomLevel;
        zoomLevel -= zoomDivisor;
        imageWidth_pixels /= Math.pow(2,zoomDivisor);
        imageHeight_pixels /= Math.pow(2,zoomDivisor);



        //The only thing that I'm concerned with right now is if imageHeight_pixels is too large.  That happens at high latitudes
        //because the computed resolution is based on latitude.  I don't care to explain the math.

        //The other error scenarios shouldn't exist.  We can't be less than the minimum because we can't zoom out that far.  We shouldn't
        //be above the imageWidth_pixels limit because it should be relatively fixed regardless of latitude.

        // Request a larger image so that that Bing logo doesn't cover up the area we are interested in
        imageHeight_pixels += (BING_LOGO_HEIGHT_PIXELS * 2);

        if (centerLatitude_deg > 82 || centerLatitude_deg < -82)
        {
            BufferedImage newImage = new BufferedImage(imageWidth_pixels, imageHeight_pixels, BufferedImage.TYPE_INT_RGB);
            return newImage;
        }
        if (imageHeight_pixels > BING_MAX_IMAGE_SIZE_PIXELS)
        {
            //Set up destination image
            imageHeight_pixels -= (BING_LOGO_HEIGHT_PIXELS * 2);
            BufferedImage newImage = new BufferedImage(imageWidth_pixels, imageHeight_pixels, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = newImage.createGraphics();

            //Determine number of divisions in latitude to download subtiles
            int heightDivisor = 1;
            int desiredPixelDivision = imageHeight_pixels/heightDivisor;
            while (desiredPixelDivision + (BING_LOGO_HEIGHT_PIXELS * 2) > BING_MAX_IMAGE_SIZE_PIXELS)
            {
                heightDivisor ++;
                desiredPixelDivision = (int)Math.ceil(imageHeight_pixels/(double)heightDivisor);
            }
            int reqdPixelsRemaining = imageHeight_pixels;

            //For each division in latitude, download subtile and add to destination image
            for (int i = 0; i < heightDivisor; i ++)
            {
                int subTilePixelHeight = desiredPixelDivision;
                if (subTilePixelHeight > reqdPixelsRemaining)
                    subTilePixelHeight = reqdPixelsRemaining;

                reqdPixelsRemaining -= subTilePixelHeight;

                //vertical center (pixel) of this sub-tile will be centerLatitude_deg - imageHeight_pixels + reqdPixelsRemaining + subTilePixelHeight/2
                double vertPixelsToLatDegConversion = (imageHeight_pixels/TILE_SIDE_SIZE_ARCDEGREES[origZoomLevel]);
                double subTile_centerLatitude_deg = centerLatitude_deg + (-imageHeight_pixels/2 + reqdPixelsRemaining + subTilePixelHeight/2)/vertPixelsToLatDegConversion;

                BufferedImage subImage = doDownloadTileSafeSize(subTile_centerLatitude_deg, centerLongitude_deg, zoomLevel, imageWidth_pixels, subTilePixelHeight + (BING_LOGO_HEIGHT_PIXELS * 2));
                g.drawImage(subImage, 0, imageHeight_pixels - reqdPixelsRemaining - subTilePixelHeight, null);
            }
            return newImage;
        }
        else
        {
            return doDownloadTileSafeSize(centerLatitude_deg, centerLongitude_deg, zoomLevel, imageWidth_pixels, imageHeight_pixels);
        }
    }

    private BufferedImage doDownloadTileSafeSize (final double centerLatitude_deg,
                                       final double centerLongitude_deg,
                                       final int zoomLevel,
                                       final int safeImageWidth_pixels,
                                       final int safeImageHeight_pixels) throws Exception
    {
//        ImageryMetadataRequestBuilder metaRequestBuilder = m_bingMapsImageryServiceClient.newImageryMetadataRequestBuilder();
//        metaRequestBuilder.withCredentials(CREDENTIALS_STRING, null);
//        metaRequestBuilder.withOptionsLocation(null, centerLatitude_deg, centerLongitude_deg);
//        metaRequestBuilder.withStyle(MapStyle.AERIAL);
//        metaRequestBuilder.withOptionsZoomLevel(zoomLevel);
//        
//        ImageryMetadataRequest request = metaRequestBuilder.getResult();
//        ImageryMetadataResponse resp = m_bingMapsImageryServiceClient.getImageryMetadata(request);
        
        
        /*MapUriRequestBuilder mapUriRequstBuilder = m_bingMapsImageryServiceClient.newMapUriRequestBuilder();
        mapUriRequstBuilder.withCredentials(CREDENTIALS_STRING, null);
        mapUriRequstBuilder.withCenter(null, centerLatitude_deg, centerLongitude_deg);
        mapUriRequstBuilder.withOptionsStyle(MapStyle.AERIAL);
        mapUriRequstBuilder.withOptionsZoomLevel(zoomLevel);

        mapUriRequstBuilder.withOptionsImageSize(safeImageWidth_pixels, safeImageHeight_pixels);
        MapUriRequest mapUriRequest = mapUriRequstBuilder.getResult();
        MapUriResponse mapUriResponse = m_bingMapsImageryServiceClient.getMapUri(mapUriRequest);
        URL tileURL = new URL(mapUriResponse.getUri());*/
        
        //URL tileURL = new URL("https://dev.virtualearth.net/rest/V1/Imagery/Map/Aerial/36.199999638,62.733332706000006/16?mapSize=392,540&key=Ag9FWHcNfGTB9oGyuxvFJbPZpUtJWagLQQq5JlXRRab0BbLLHmk-Zz3KlqkmpoSU");
        URL tileURL = new URL("https://dev.virtualearth.net/rest/V1/Imagery/Map/Aerial/" +
                                centerLatitude_deg + "," + centerLongitude_deg + "/" + //36.199999638,62.733332706000006/
                                zoomLevel + "?" + //16?
                                "mapSize=" + safeImageWidth_pixels + "," + safeImageHeight_pixels + //mapSize=392,540
                                "&key=" + CREDENTIALS_STRING);   //&key=Ag9FWHcNfGTB9oGyuxvFJbPZpUtJWagLQQq5JlXRRab0BbLLHmk-Zz3KlqkmpoSU");

        //
        // Download the image
        //        
        InputStream inputStream = tileURL.openStream();
        BufferedImage tileImage = ImageIO.read(inputStream);

        // Remove the Bing logo from the bottom of the image
        return tileImage.getSubimage(0, BING_LOGO_HEIGHT_PIXELS, safeImageWidth_pixels, safeImageHeight_pixels-BING_LOGO_HEIGHT_PIXELS*2);
    }
}

class TileIdentifier
{
    LatLonAltPosition centerPosition;
    double centerLatitude_deg;
    double centerLongitude_deg;
    int zoomLevel;
    public BufferedImage image;
    public Length width;
    public Length height;

    public String getFilename(String imageDirectory)
    {
        return String.format("%1$s%2$d_%3$.6f_%4$.6f.jpg", imageDirectory,
                             zoomLevel,
                             centerLatitude_deg,
                             centerLongitude_deg);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || !(other instanceof TileIdentifier))
        {
            return false;
        }
        else
        {
            TileIdentifier otherTile = (TileIdentifier) other;
            if (centerLatitude_deg == otherTile.centerLatitude_deg
                    && centerLongitude_deg == otherTile.centerLongitude_deg
                    && zoomLevel == otherTile.zoomLevel)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.centerLatitude_deg) ^ (Double.doubleToLongBits(this.centerLatitude_deg) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.centerLongitude_deg) ^ (Double.doubleToLongBits(this.centerLongitude_deg) >>> 32));
        hash = 79 * hash + this.zoomLevel;
        return hash;
    }
}
