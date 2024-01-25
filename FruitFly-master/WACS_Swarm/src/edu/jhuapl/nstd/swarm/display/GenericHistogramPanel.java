package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.cbrnPods.RNTotalCountsTracker;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.Belief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CompositeHistogramBelief;
import edu.jhuapl.nstd.swarm.belief.GammaDetectionBelief;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author fishmsm1
 */
public class GenericHistogramPanel extends JPanel implements Updateable
{

    protected ChartPanel chartPanel;
    protected JPanel chartHolder;
    protected JFreeChart chart;
    protected NumberAxis xaxis;
    protected ValueAxis yaxis;
    protected XYPlot plot;
    protected XYSeries series;
    private String blfType;
    private BeliefManager _belMgr;
    private Date lastChartTime, lastDetTime;
    private LinkedList<StoredSpectrum> history;
    private boolean live;
    private JLabel lblLiveTime, lblStats, lblDetection, lblAlert;
    private RNTotalCountsTracker totalCountsTracker;
    private Class detBlfType;
    private String detBlfMethod;
    private boolean logged;
    private int liveTimeDisplay, totCountsDisplay;
    private IntervalMarker interval;
    private ValueMarker rule, crosshair;
    private MouseResponder mouseResp;
    
    private static final double ZERO = 0.11;

    /**
     * Constructor for GenericHistogramPanel
     * 
     * @param mgr The belief manager
     * @param belief The BELIEF_NAME of belief to call upon when culling data. MUST be
     * a subclass of CompositeHistogramBelief.
     * @param xAxisLabel The label for the x axis
     * @param yAxisLabel The label for the y axis
     */
    public GenericHistogramPanel(BeliefManager mgr, String belief, RNTotalCountsTracker tracker, String xAxisLabel, String yAxisLabel)
    {
        super();
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//        timeWindow = Config.getConfig().getPropertyAsInteger(className + ".TimeWindowSpan.Ms", 60000); // ms

        _belMgr = mgr;
        
        // Set the belief to call
        blfType = belief;
        
        // Create the altitude panel
        initChartPanel();
        
        xaxis.setLabel(xAxisLabel);
        yaxis.setLabel(yAxisLabel);
        
        totalCountsTracker = tracker;
        
        lastChartTime = new Date(0);
        lastDetTime = new Date(0);
        live = true;
        history = new LinkedList<StoredSpectrum>();
        
        interval = new IntervalMarker(0, 0);
        interval.setPaint(Color.BLUE);
        interval.setAlpha(0.3f);
        plot.addDomainMarker(interval);
        
        rule = new ValueMarker(Double.MIN_VALUE);
        rule.setPaint(Color.BLUE);
        plot.addDomainMarker(rule);
        
        crosshair = new ValueMarker(Double.MIN_VALUE);
        crosshair.setPaint(Color.BLUE.darker());
        crosshair.setStroke(XYPlot.DEFAULT_CROSSHAIR_STROKE);
        plot.addRangeMarker(crosshair);
    }
    
    public GenericHistogramPanel(BeliefManager mgr, String belief, String xAxisLabel, String yAxisLabel)
    {
        this(mgr, belief, null, xAxisLabel, yAxisLabel);
    }
    
    public void setDetectionBelief(Class belief, String methodName)
    {
        detBlfType = belief;
        detBlfMethod = methodName;
    }
    
    public void setTracker(RNTotalCountsTracker tracker)
    {
        totalCountsTracker = tracker;
    }

    /**
     * Builds the main XY series plot.
     */
    protected void initChartPanel()
    {
        // Create the chart 
        chart = createChart();
        chart.removeLegend();

        // Create ChartPanel for chart area
        chartPanel = new ChartPanel(chart, ChartPanel.DEFAULT_WIDTH, ChartPanel.DEFAULT_HEIGHT,
            50, ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT, 5000,
            ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, ChartPanel.DEFAULT_BUFFER_USED, false,
            true, true, false, true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setMinimumSize(new Dimension(200, 80));
        chartPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        mouseResp = new MouseResponder();
        chartPanel.addMouseListener(mouseResp);
        chartPanel.addMouseMotionListener(mouseResp);
        chartPanel.addMouseWheelListener(mouseResp);

        this.setBackground(Color.WHITE);

        chartHolder = new JPanel();
        chartHolder.setLayout(new BoxLayout(chartHolder, BoxLayout.X_AXIS));
        chartHolder.add(chartPanel);
        chartHolder.setBackground(Color.WHITE);
        chartHolder.setAlignmentX(LEFT_ALIGNMENT);

        this.add(Box.createVerticalStrut(10));
        this.add(chartHolder);
        
        Dimension statsDim = new Dimension(Integer.MAX_VALUE, 20);
                
        lblLiveTime = new JLabel("Histogram covers 0 seconds");
        JPanel panTime = new JPanel();
        panTime.setLayout(new BoxLayout(panTime, BoxLayout.X_AXIS));
        panTime.setAlignmentX(LEFT_ALIGNMENT);
        panTime.setBackground(Color.WHITE);
        panTime.setMinimumSize(statsDim);
        panTime.setPreferredSize(statsDim);
        panTime.setMaximumSize(statsDim);
        panTime.add(Box.createHorizontalStrut(5));
        panTime.add(lblLiveTime);
        
        lblStats = new JLabel("No statistics");
        lblAlert = new JLabel();
        lblAlert.setForeground(Color.RED);
        JPanel panStats = new JPanel();
        panStats.setLayout(new BoxLayout(panStats, BoxLayout.X_AXIS));
        panStats.setMinimumSize(statsDim);
        panStats.setPreferredSize(statsDim);
        panStats.setMaximumSize(statsDim);
        panStats.add(Box.createHorizontalStrut(5));
        panStats.add(lblStats);
        panStats.add(Box.createHorizontalStrut(20));
        panStats.add(lblAlert);
        panStats.setBackground(Color.WHITE);
        panStats.setAlignmentX(LEFT_ALIGNMENT);
        
        lblDetection = new JLabel("No detections");
        JPanel panDet = new JPanel();
        panDet.setLayout(new BoxLayout(panDet, BoxLayout.X_AXIS));
        panDet.setAlignmentX(LEFT_ALIGNMENT);
        panDet.setMinimumSize(statsDim);
        panDet.setPreferredSize(statsDim);
        panDet.setMaximumSize(statsDim);
        panDet.setBackground(Color.WHITE);
        panDet.add(Box.createHorizontalStrut(5));
        panDet.add(lblDetection);
        
        JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
        sep1.setMinimumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep1.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep1.setAlignmentX(LEFT_ALIGNMENT);
        
        JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
        sep2.setMinimumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setAlignmentX(LEFT_ALIGNMENT);
             
        this.add(panTime);
        this.add(sep1);
        this.add(panStats);
        this.add(sep2);
        this.add(panDet);
    }

    /**
     * Creates a XY plot of time series data.
     *
     * @param dataset the dataset.
     *
     * @return A XY plot of time series data.
     */
    protected JFreeChart createChart()
    {
        xaxis = new NumberAxis();
        xaxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        xaxis.setAutoRange(true);
//        xaxis.setFixedAutoRange(timeWindow);
//        xaxis.setTickUnit(  );
        xaxis.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        xaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        yaxis = new LogarithmicAxis("");
//        yaxis.setAutoRange(true);
        yaxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        yaxis.setLabelFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
//        yaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        logged = true;

        Color gridColor = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), 100);
        plot = new XYPlot(null, xaxis, yaxis, null);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(gridColor);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        series = new XYSeries("data");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
//        XYBarRenderer rend = new XYBarRenderer(0.05); // 0.05 is the margin between bars in %
//        rend.setShadowVisible(false);
//        rend.setBarPainter(new StandardXYBarPainter());
        XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer(true, false);
        plot.setDataset(dataset);
        plot.setRenderer(rend);

        JFreeChart jfreechart = new JFreeChart(plot);
        jfreechart.setAntiAlias(true);

        // Sets background color of chart
        jfreechart.setBackgroundPaint(Color.WHITE);

        return jfreechart;
    }

    /**
     * Sets the height of a bar.
     *
     * @param bin The bin to change or set the value for.
     * @param val The value of the bin/the height the bar will be set to.
     */
    protected void addDataPoint(int bin, int val)
    {
        double cleaned = val;
        if (val <= 0)
        {
            cleaned = ZERO;
        }
        
        int index = series.indexOf(bin);
        if (index >= 0)
        {
            cleaned += series.getY(index).doubleValue();
            series.remove(index);
        }
        
        series.add(bin, cleaned);
    }
    
    @Override
    public synchronized void update()
    {
        boolean inside = false;
        
        try {
            if (chartPanel.getMousePosition() != null)
                inside = chartPanel.getScreenDataArea().contains(chartPanel.getMousePosition());
        } catch (NullPointerException e) {
            // Ignore, keep inside as false
        }
        
        if (!inside)
        {
            clearRule();
        }
        
        try
        {
            updateChart();
        }
        catch (Exception e)
        {
            System.err.println("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
//    long time = 0; // Test variable
//    int count =  0; // Test variable
    private void updateChart()
    {
        synchronized (this)
        {
            CompositeHistogramBelief blf = (CompositeHistogramBelief) _belMgr.get(blfType);
            
            // TEST CODE
//            blf = new CompositeHistogramBelief();
//            int[] arr;
//            if (time == 0 || System.currentTimeMillis() > time + 15000)
//            {
//                time = System.currentTimeMillis();
//                count ++;
//            }
//            if (count <= 5)
//            {
//                arr = new int[1024];
//                /*for (int i = 0; i < arr.length; i++)
//                {
//                    arr[i] = (int) (2 * Math.random()) + 5;
//                }
//                arr[24] = 0;*/
//                
//                int americium[] = {171,140,7,30,109,358,546,533,381,301,77,50,37,18,5,0,84,1,3,2,4,6,5,6,5,7,7,7,10,10,4,4,88,5,8,4,12,12,6,4,9,7,8,6,3,2,89,3,92,90,7,3,6,4,5,4,8,91,5,1,8,4,9,5,11,172,96,92,8,6,5,4,11,8,10,8,14,91,6,7,7,10,7,93,10,8,14,8,8,6,11,16,10,8,16,15,14,9,8,11,17,15,19,17,17,38,25,42,38,52,34,45,59,53,80,64,79,80,192,120,108,135,163,181,202,251,276,320,354,385,432,455,525,563,650,672,750,796,879,977,952,1122,1056,1170,1188,1239,1360,1469,1485,1449,1550,1545,1612,1670,1698,1807,1731,1902,1960,2153,2326,2375,2295,2364,2325,2104,1619,1099,627,273,106,44,7,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,1,1,2,0,0,1,0,0,0,0,1,1,1,0,0,0,2,1,0,0,56,0,0,1,28,0,1,0,0,3,0,0,0,1,1,1,0,0,1,111};
//                int bkg[] = {0,4703,212,87,77,133,175,336,730,1173,1613,1856,1696,1541,1362,1490,1640,1852,2178,2550,2746,3113,3249,3641,3869,4118,4194,4371,4385,4338,4147,4158,4046,3946,3875,3654,3616,3615,3508,3324,3322,3141,3084,3114,2891,2917,2768,2708,2712,2508,2419,2346,2256,2286,2100,2083,2011,2011,1948,1874,1782,1707,1738,1705,1649,1614,1566,1403,1522,1405,1355,1387,1294,1244,1234,1198,1155,1180,1103,1099,1014,1010,979,1028,1000,965,891,869,834,799,746,710,778,687,727,679,627,643,638,619,614,648,580,583,555,543,559,511,481,489,483,447,514,474,490,471,441,426,432,450,422,407,394,411,427,364,370,342,301,307,285,291,252,279,259,263,245,232,236,264,261,234,226,232,205,224,206,227,209,195,208,168,197,202,198,149,161,207,148,186,153,173,168,184,150,183,205,167,156,168,169,156,172,175,179,148,151,141,143,135,137,122,149,137,129,122,123,132,124,129,127,125,132,117,131,112,129,123,132,142,148,132,134,153,135,156,126,147,122,154,123,120,109,120,116,111,105,104,106,95,80,82,79,66,84,88,97,89,83,90,84,75,63,62,69,86,78,67,69,64,65,69,68,56,61,53,77,55,55,63,76,60,53,64,64,67,71,52,67,70,33,46,57,55,76,51,61,46,52,52,68,71,67,69,37,61,49,45,51,49,47,52,47,54,60,53,48,54,53,54,46,57,46,54,54,45,48,48,50,44,39,44,33,46,45,44,56,38,40,52,43,45,36,48,51,41,47,37,39,47,34,38,40,38,42,50,40,42,44,54,35,41,38,38,36,31,28,29,36,28,40,33,26,31,34,32,35,35,34,35,39,31,28,30,40,29,29,48,32,42,32,48,34,39,52,38,27,35,43,42,28,36,39,37,31,34,30,43,37,32,30,37,35,37,25,27,25,40,24,33,31,36,31,33,28,33,21,26,21,39,24,25,23,26,32,31,24,29,28,30,21,32,25,26,17,13,24,27,24,18,21,30,19,20,23,22,15,33,25,20,19,22,7,20,19,17,17,20,17,14,19,18,18,16,22,18,12,23,27,19,22,22,25,23,19,15,16,22,14,29,26,28,22,25,25,25,22,20,26,25,27,21,32,31,32,21,19,20,28,22,36,28,24,22,28,43,31,29,31,28,20,23,20,16,30,21,24,23,24,19,22,25,20,25,17,27,14,17,31,25,14,15,10,16,14,11,5,14,8,13,15,12,16,17,11,9,7,16,8,10,13,7,9,11,9,7,14,12,13,5,7,8,3,10,4,15,9,10,12,15,12,9,5,10,9,19,5,8,10,13,3,9,7,11,14,10,11,8,3,12,14,4,7,15,10,8,9,10,12,9,15,5,15,5,7,9,7,10,10,18,15,11,10,11,12,9,10,7,15,8,9,9,8,11,9,10,11,8,6,7,8,9,7,8,12,7,6,4,3,7,8,10,8,9,5,7,7,7,8,8,3,5,9,5,7,4,5,4,6,7,3,7,5,3,6,8,6,6,6,5,4,12,3,6,5,7,2,5,6,5,3,5,3,1,6,6,4,9,2,3,5,6,6,3,3,5,7,5,4,7,3,5,5,3,4,5,7,4,5,3,5,10,11,4,5,3,6,7,10,5,4,7,10,4,4,5,4,7,1,2,6,4,6,5,9,2,6,8,6,5,4,5,3,4,7,6,7,4,10,4,4,3,5,5,4,5,5,3,4,8,2,4,5,5,6,3,3,6,4,4,5,1,2,3,5,4,5,4,3,8,3,2,1,2,3,1,6,9,6,5,4,3,6,3,1,3,2,8,4,2,6,5,2,3,6,7,6,4,3,5,3,2,1,6,4,1,2,2,6,3,5,3,6,6,3,6,2,1,5,2,1,3,0,3,3,7,4,4,1,1,6,4,4,1,5,4,3,2,4,2,5,2,4,1,3,5,6,1,2,0,6,1,2,3,2,4,3,0,2,7,4,4,4,4,4,5,3,3,3,3,0,4,1,3,5,6,6,3,5,3,5,4,4,1,1,1,3,5,1,5,3,4,7,3,2,1,4,5,4,3,2,1,1,0,3,1,1,1,1,4,1,2,4,2,2,3,2,2,3,0,3,1,1,4,2,1,3,5,3,0,1,7,1,1,1,1,4,2,0,2,1,1,3,5,3,0,1,1,3,1,3,1,1,2,0,4,1,1,1,1,0,0,2,1,0,3,3,1,1,3,1,2,2,1,3,2,3,0,3,0,2,3,2,1,3,3,2,0,2,3,3,1,0,0,3,6,2,2,5,2,1,2,2,2,4,1,5,2,3,2,0,2,3,3,1,4,2,2,1,4,1,2,3,2,1,2,4,2,2,2,3,0,3};
//                arr = bkg;
//            }
//            else
//            {
//                arr = new int[256];
//                for (int i = 0; i < arr.length; i++)
//                {
//                    if (i == 0)
//                        arr[i] = 50;
//                    else if (i == 20)
//                        arr[i] = 0;
//                    else
//                        arr[i] = 250 / i;
//                }
//            }
//            blf.setTimeStamp(new Date(time));
//            blf.setHistogramData(arr);
//            blf.setLiveTime(15000);
//            blf.setSpectraCount(321);
            // END TEST CODE
            
            if (blf != null && blf.getTimeStamp().after(lastChartTime))
            {
                int[] data = blf.getHistogramData();
                long liveTime = (long)blf.getLiveTime();
                int counts = (int)blf.getSpectraCount();
                String alertMsg = totalCountsTracker.getCountsAlertMessage(blf.getSpectraCount());
                
                recordSpectrum(data, blf.getTimeStamp(), liveTime, counts, "", alertMsg, blf.isBackground());
                lastChartTime = blf.getTimeStamp();

                if (live)
                {
                    clearSpectra();
                    loadSpectrum(history.getLast());
                }
            }
            
            // TEST CODE
//            GammaDetectionBelief det = new GammaDetectionBelief("agent007", "background- 100%");
//            _belMgr.put(det);
            // END TEST CODE
            
            /*
             * The following block uses Java's Reflection system to call upon
             * a class set at runtime (in GcsDisplay.java). This class is the
             * detection belief for whatever sensor this chart is being used
             * for.
             */
            if (detBlfType != null)
            {
                try
                {
                    Belief detectionBlf = (Belief) detBlfType.cast(_belMgr.get((String) detBlfType.getField("BELIEF_NAME").get(null)));
                    
                    if (detectionBlf != null && detectionBlf.getTimeStamp().after(lastDetTime))
                    {
                        history.getLast().detection = (String) detBlfType.getMethod(detBlfMethod).invoke(detectionBlf);
                        if (live)
                        {
                            clearSpectra();
                            loadSpectrum(history.getLast());
                        }
                        lastDetTime = detectionBlf.getTimeStamp();
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Reflection error");
                }
            }
        }
    }
    
    public void goToTime(Date time)
    {
        live = false;
        clearSpectra();
        
        if (history.size() > 1)
        {
            Iterator<StoredSpectrum> iter = history.iterator();
            StoredSpectrum cur = iter.next();
            
            while (iter.hasNext() && time.after(cur.end))
            {
                cur = iter.next();
            }
            
            loadSpectrum(cur);
        }
        else if (history.size() == 1)
        {
            loadSpectrum(history.getFirst());
        }
    }
    
    public synchronized void goToTimeWindow(Date start, Date end)
    {
        live = false;
        clearSpectra();
        
        LinkedList<StoredSpectrum> toLoad = new LinkedList<StoredSpectrum>();
        
        if (history.size() > 1)
        {
            Iterator<StoredSpectrum> iter = history.iterator();
            StoredSpectrum cur;
            
            while (iter.hasNext())
            {
                cur = iter.next();
                
                if (start.before(cur.end) && end.after(new Date(cur.end.getTime() - cur.liveTime)))
                {
                    if (cur.background && toLoad.size() > 0 && toLoad.getLast().background)
                    {
                        if (end.after(toLoad.getLast().end))
                        {
                            toLoad.removeLast();
                            toLoad.add(cur);
                        }
                    }
                    else
                    {
                        toLoad.add(cur);
                    }
                }
            }
            
            for (StoredSpectrum spec : toLoad) {
                loadSpectrum(spec);
            }
            
            if (toLoad.size() > 1)
            {
                setStatsAlert("");
                setDetectionMessage("Spectrum Not Analyzed");
            }
            else if (toLoad.size() == 0)
            {
                loadSpectrum(history.getLast());
            }
        }
        else if (history.size() == 1)
        {
            loadSpectrum(history.getFirst());
        }
    }
    
    public void goLive()
    {
        goToTime(new Date());
        live = true;
    }
    
    public void setStatsMessage(int counts)
    {
        lblStats.setText("Total Counts = " + counts);
        totCountsDisplay = counts;
    }
    
    public void setStatsAlert(String alert)
    {
        lblAlert.setText(alert);
    }
    
    public void setDetectionMessage(String message)
    {
        lblDetection.setText(message);
    }
    
    public void setLiveTime(int time)
    {
        lblLiveTime.setText("Histogram covers " + time + " seconds");
        liveTimeDisplay = time;
    }
    
    private void loadSpectrum(StoredSpectrum spectrum)
    {
        chart.setNotify(false);
        
        for (int i = 0; i < spectrum.data.length; i++)
        {
            addDataPoint(i, spectrum.data[i]);
        }
        
        chart.setNotify(true);
        
        setLiveTime((int)spectrum.liveTime/1000 + liveTimeDisplay);
        setStatsMessage(spectrum.totalCounts + totCountsDisplay);
        setDetectionMessage(spectrum.detection);
        setStatsAlert(spectrum.alert);
        
        updateRange();
    }
    
    private void clearSpectra()
    {
        series.clear();
        setLiveTime(0);
        setStatsMessage(0);
        setStatsAlert("");
        setDetectionMessage("");
    }
    
    private void recordSpectrum(int[] data, Date timestamp, long liveTime, int totCounts, String detMsg, String alertMsg, boolean isBackground)
    {
        history.add(new StoredSpectrum(data, timestamp, liveTime,
                totCounts, detMsg, alertMsg, isBackground));
    }
    
    /**
     * Sets the vertical range for the series. In order to do so, this method
     * scans through all displayed series to find the highest and lowest values
     * and makes adjustments based on that if necessary. The reason we cannot
     * record max and min as data is added to the chart is that we only care
     * about the max and min displayed in the current window, which can change
     * at any time.
     */
    private void updateRange()
    {
        double max = 0, min = Double.MAX_VALUE;

        if (series.getItemCount() > 0)
        {
            for (int j = (int) Math.floor(xaxis.getRange().getLowerBound()); j <= (int) Math.ceil(xaxis.getRange().getUpperBound()); j++)
            {
                int index = series.indexOf(j);

                if (index >= 0)
                {
                    if (series.getY(index).doubleValue() < min)
                    {
                        min = series.getY(index).doubleValue();
                    }
                    if (series.getY(index).doubleValue() > max || max == Double.MIN_VALUE)
                    {
                        max = series.getY(index).doubleValue();
                    }
                }
            }
        }

        if (max != 0 && min != Double.MAX_VALUE)
        {
            yaxis.setUpperBound(max * 2);
            
//            if (min != ZERO)
//                yaxis.setLowerBound(min / 2);
//            else
                yaxis.setLowerBound(ZERO);
        }
        else
        {
            // No way to set range, so leave it be.
        }
    }

    private class MouseResponder extends MouseAdapter
    {
        double intervalStart;
        
        @Override
        public void mouseMoved(MouseEvent e)
        {
            boolean shiftDown = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK;
            
            updateCrosshair(e.getPoint(), shiftDown);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
            {
                chart.setNotify(false);

                double old = xaxis.getRange().getLength();
                double newWindow, min, max;
                if (e.getWheelRotation() < 0)
                {
                    newWindow = ((3.0 / 4) * old);
                    if (newWindow < 2) {
                        return;
                    }

                    if (newWindow > series.getMaxX() - series.getMinX() + 50)
                    {
                        newWindow = series.getMaxX() - series.getMinX() + 50;
                    }

                    
                }
                else
                {
                    newWindow = ((5.0 / 4) * old);

                    if (newWindow > series.getMaxX() - series.getMinX() + 50)
                    {
                        newWindow = series.getMaxX() - series.getMinX() + 50;
                    }
                    
//                    double delta = newWindow - old;
//                    min = xaxis.getLowerBound() - delta / 2;
//                    max = xaxis.getUpperBound() + delta / 2;
                }
                
                double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
                double pos = xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());

                double leftRatio = (pos - xaxis.getLowerBound()) / old;

                min = pos - newWindow * leftRatio;
                max = pos + newWindow * (1 - leftRatio);
                
                if (min < series.getMinX() - 10)
                {
                    double delta = series.getMinX() - 10 - min;
                    min = series.getMinX() - 10;
                    max = Math.min(max + delta, series.getMaxX() + 40);
                }
                if (max > series.getMaxX() + 40)
                {
                    double delta = max - series.getMaxX() - 40;
                    max = series.getMaxX() + 40;
                    min = Math.max(series.getMinX() - 10, min - delta);
                }
                
                zoomTo(min, max);
                
                chart.setNotify(true);
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e)
        {
            double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
            intervalStart = xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());
        }
        
        @Override
        public void mouseDragged(MouseEvent e)
        {
            double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();

            moveInterval(intervalStart, xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge()));
        }
        
        @Override
        public void mouseReleased(MouseEvent e)
        {
            double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
            double pos = xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());

            moveInterval(intervalStart, pos);

            if (Math.abs(pos - intervalStart) > 2)
            {
                zoomTo(intervalStart, pos);
            }
            else // Ignore this request
            {
                moveInterval(0, 0);
            }
        }
        
        private void zoomTo(double start, double stop)
        {
            moveInterval(0, 0);
            
            if (start > stop)
            {
                double tmp = stop;
                stop = start;
                start = tmp;
            }
            
            xaxis.setRange(start, stop);
            updateRange();
        }

        /**
         * Moves the interval that selects what time range to display in this
         * chart's related histogram
         *
         * @param startX the x-axis value to move the start to (NOT screen
         * position)
         * @param stopX the x-axis value to move the stop to (NOT screen
         * position)
         */
        private void moveInterval(double start, double stop)
        {
//        double start = xaxis.java2DToValue(startX, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());
//        double stop = xaxis.java2DToValue(stopX, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());

            if (start <= stop)
            {
                interval.setStartValue(start);
                interval.setEndValue(stop);
            }
            else
            {
                interval.setStartValue(stop);
                interval.setEndValue(start);
            }
        }
        
        private void updateCrosshair(Point cursor, boolean shiftDown)
        {
            double x = chartPanel.translateScreenToJava2D(cursor).getX();
            
            boolean inside = chartPanel.getScreenDataArea().contains(cursor);
            
            if (inside)
                moveRule(xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge()));
            else
                clearRule();
        }
    }
    
    /**
     * Moves the vertical rule to a screen position x.
     * 
     * @param pos The Time value to move the rule to.
     */
    private void moveRule(double pos)
    {
        rule.setStroke(XYPlot.DEFAULT_OUTLINE_STROKE);
        crosshair.setStroke(XYPlot.DEFAULT_CROSSHAIR_STROKE);
        
        double eval = Math.round(pos);
        
        rule.setValue(eval);
        
        int index = series.indexOf(eval);
        if (index >= series.getMinX() && index <= series.getMaxX())
            crosshair.setValue(series.getY(index).doubleValue());
    }
    
    private void clearRule()
    {
        rule.setStroke(new BasicStroke(0));
        crosshair.setStroke(new BasicStroke(0));
    }
}

class StoredSpectrum
{
    public Date end;
    public int[] data;
    public long liveTime;
    public int totalCounts;
    public String detection, alert;
    public boolean background;
    
    public StoredSpectrum(int[] spectrum, Date time, long liveTime, int totCounts, String detText, String alertText, boolean isBackground)
    {
        data = spectrum;
        end = time;
        this.liveTime = liveTime;
        
        totalCounts = totCounts;
        
        if (detText != null && !detText.equals(""))
            detection = detText;
        else
            detection = "No detections";
        
        if (alertText != null)
            alert = alertText;
        else
            alert = "";
        
        background = isBackground;
    }
}