package edu.jhuapl.nstd.swarm.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Scrollbar;
import java.awt.geom.Ellipse2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleEdge;

import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Point;
import java.awt.event.InputEvent;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;


/**
 * This class is the base class for plotting multiple sets of dynamic time
 * series data. Each TimeSeries set is stored in a name, and referenced by its
 * unique name. Each set is represented in the line chart as a different colored
 * line.
 *
 * @author fishmsm1, olsoncc1
 *
 */
public abstract class DynamicTimeSeriesChartPanel extends JPanel implements AdjustmentListener, ActionListener
{

    protected Map<String, DataContainer> datasetMap;
    protected ChartPanel chartPanel;
    protected JPanel chartHolder;
    protected JFreeChart chart;
    protected DateAxis xaxis;
    protected ValueAxis yaxis;
    protected XYPlot plot;
    protected TimeSeriesCollection headers;
    protected XYLineAndShapeRenderer headRenderer;
    protected int timeWindow; // in milliseconds
    private static final Color[] COLORS =
    {
        Color.GREEN.darker(), Color.BLUE, Color.RED, Color.BLACK, Color.ORANGE, Color.MAGENTA
    };
    /**
     * Percentage of range to pad the top and bottom with
     */
    public double PLOT_PADDING_Y = 0.1;
    /**
     * Padding on domain in milliseconds
     */
    private static final int PLOT_PADDING_X = 500;
    /**
     * Minimum range of the y-axis
     */
    private double minRange = 5;
    /**
     * Size of POI dots
     */
    public static final double MARKER_SIZE = 10;
    /**
     * Width of lines on charts
     */
    public static final float LINE_WIDTH = 1.7f;
    /**
     * Says whether range should always include zero or not
     */
    private boolean includeZero = true;
    /**
     * Says whether negative values should not be displayed in the range
     */
    private boolean restrictPositive = true;
    /**
     * Vertical rule for the current mouse position
     */
    private ValueMarker rule;
    /**
     * Marks the selected interval (click-and-drag area) that is currently being displayed
     * in this chart's associated histogram (if there is one)
     */
    private IntervalMarker interval;
    /**
     * Marks a selected time to view in the histogram window
     */
    private ValueMarker marker;
    /**
     * Holds the last mouse position to prevent keep the rule from moving behind
     * when a data point is added
     */
    private double lastMousePos;
    /**
     * Holds a histogram to be updated (set in `setTimeFor` method)
     */
    private GenericHistogramPanel histogram;
    /**
     * The horizontal scrollbar that allows one to move around in time
     */
    private Scrollbar scrollbar;
    /**
     * A button next to the scrollbar that returns the scrollbar to the very right
     * and sets the chart to update dynamically
     */
    private JButton btnLive;
    /**
     * Indicates whether the chart is static/viewing a fixed region or 
     * and scrolling automatically to the most recent time
     */
    private boolean live;
    /**
     * A reference variable for the <code>scrollbar</code>. <code>Scrollbar</code>s
     * can only scroll through <code>int</code>s, so this variable is set with
     * the time to be associated with the value 0 on the scrollbar and then all
     * other values are added to find the actual time to scroll to.
     */
    private long firstTime;
    /**
     * Holds an instance of MouseResponder, which handles mouse events
     */
    private MouseResponder mouseResp;
    /**
     * Holds the current index for the COLORS array, allowing different lines on
     * a chart to cycle through different colors
     */
    private int colorIndex = 0;

    /**
     * Constructor for a generic dynamic time series chart.
     *
     * @param className Name of the inheriting subclass. Used to lookup the
     * <i>className</i>.TimeWindowSpan.Ms config property.
     */
    public DynamicTimeSeriesChartPanel(String className)
    {
        super();
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        timeWindow = Config.getConfig().getPropertyAsInteger(className + ".TimeWindowSpan.Ms", 60000); // ms
        datasetMap = new HashMap<String, DataContainer>();
        btnLive = new JButton("Go Live");
        btnLive.addActionListener(this);
        btnLive.setEnabled(false);
        scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 0, timeWindow, 0, 0);
        scrollbar.addAdjustmentListener(this);
        scrollbar.setBlockIncrement(timeWindow);
        scrollbar.setUnitIncrement(timeWindow/4);
        live = true;
        firstTime = 0;

        // Create the altitude panel
        initChartPanel();

        // Set up headers collection
        headers = new TimeSeriesCollection();
        headRenderer = new XYLineAndShapeRenderer(false, true);
        plot.setDataset(0, headers);
        plot.setRenderer(0, headRenderer);
        
        rule = new ValueMarker(0);
        rule.setPaint(Color.BLUE);
        plot.addDomainMarker(rule);
        
        marker = new ValueMarker(0);
        marker.setPaint(Color.BLUE);
        marker.setStroke(XYPlot.DEFAULT_CROSSHAIR_STROKE);
        plot.addDomainMarker(marker);
        
        interval = new IntervalMarker(0, 0);
        interval.setPaint(Color.BLUE);
        interval.setAlpha(0.3f);
        plot.addDomainMarker(interval);
    }

    /**
     * Builds the main XY time series plot.
     */
    protected void initChartPanel()
    {
        // Create the chart 
        chart = createChart();

        // Create ChartPanel for chart area
        chartPanel = new ChartPanel(chart, ChartPanel.DEFAULT_WIDTH, ChartPanel.DEFAULT_HEIGHT,
            50, ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT, 5000,
            ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, ChartPanel.DEFAULT_BUFFER_USED, false,
            true, true, false, true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setMinimumSize(new Dimension(200, 80));
        chartPanel.setAlignmentX(CENTER_ALIGNMENT);
        
        mouseResp = new MouseResponder();
        chartPanel.addMouseWheelListener(mouseResp);
        chartPanel.addMouseListener(mouseResp);
        chartPanel.addMouseMotionListener(mouseResp);
        
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.LEFT);
        legend.setItemFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        this.setBackground(Color.WHITE);
        
        chartHolder = new JPanel(){
            @Override
            public boolean isOptimizedDrawingEnabled()
            {
                return false;
            }
        };
        chartHolder.setLayout(new BoxLayout(chartHolder, BoxLayout.X_AXIS));
        chartHolder.add(chartPanel);
        chartHolder.setBackground(Color.WHITE);
        
        Dimension scrollSize = new Dimension(Integer.MAX_VALUE, 20);
        JPanel panScroll = new JPanel(){
            @Override
            public boolean isOptimizedDrawingEnabled()
            {
                return false;
            }
        };
        panScroll.setLayout(new BoxLayout(panScroll, BoxLayout.X_AXIS));
        panScroll.setMinimumSize(scrollSize);
        panScroll.setPreferredSize(scrollSize);
        panScroll.setMaximumSize(scrollSize);
        panScroll.setBackground(Color.WHITE);
        panScroll.add(scrollbar);
        panScroll.add(btnLive);
        
        this.add(Box.createVerticalStrut(10));
        this.add(chartHolder);
        this.setComponentZOrder(chartHolder, 0);
        this.add(panScroll);
        this.setComponentZOrder(panScroll, 2);
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
        xaxis = new DateAxis();
        xaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        xaxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
//        xaxis.setAutoRange(true);
//        xaxis.setFixedAutoRange(timeWindow);
        TickUnits unitSource = new TickUnits();
        for (int i = 1; i < 5; i++)
        {
            unitSource.add(new DateTickUnit(DateTickUnitType.SECOND, (int)(timeWindow/2000 * i)));
        }
        xaxis.setStandardTickUnits(unitSource);
        //xaxis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, timeWindow/2000));
        xaxis.setLabel("Time");

        yaxis = new NumberAxis();
        //yaxis.setAutoRange(true);
        //yaxis.setFixedAutoRange(RANGE_AXIS_LENGTH);
        yaxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        yaxis.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        Color gridColor = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), 150);
        plot = new XYPlot(null, xaxis, yaxis, null);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(gridColor);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        
        JFreeChart jfreechart = new JFreeChart(plot);
        jfreechart.setAntiAlias(true);

        // Sets background color of chart
        jfreechart.setBackgroundPaint(Color.WHITE);

        return jfreechart;
    }
    
    @Override
    public boolean isOptimizedDrawingEnabled()
    {
        return false;
    }

    /**
     * Creates a dataset with the specified name.
     *
     * @param collectionName the name of the dataset to be created
     * @return True if successful, false if there was already a collection with
     * that name.
     */
    protected boolean createTimeSeriesCollection(String collectionName)
    {
        int num = getNextIndex();
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        plot.setDataset(num, dataset);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setDrawSeriesLineAsPath(true);
        plot.setRenderer(num, renderer);
        
        if (!datasetMap.containsKey(collectionName)) // Checks to see if a 
        // series under the name
        // `collectionName` already exists
        {
            datasetMap.put(collectionName, new DataContainer(dataset, renderer, num));
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Creates a new TimeSeries and stores it in the map. Can pair with another
     * TimeSeries to create a shaded area.
     *
     * @param seriesName Name of the new TimeSeries.
     * @param inCollection Used as a key in the map of DataContainer objects. A
     * string that names the collection to be created or the collection that the
     * series will be added to.
     *
     * @return new TimeSeries, or null if the series or collection name was
     * invalid
     */
    protected TimeSeries createTimeSeries(String seriesName, String inCollection, boolean showCrosshair)
    {
        TimeSeries series = new TimeSeries(seriesName);
        XYLineAndShapeRenderer renderer;
        TimeSeriesCollection dataset;
        String itemName;
        Integer num;
        boolean skipPut = false;
        
        BasicStroke stroke = new BasicStroke(LINE_WIDTH);

        if (inCollection != null)
        {
            DataContainer coll = datasetMap.get(inCollection);

            if (coll == null)
            {
                num = getNextIndex();
                dataset = new TimeSeriesCollection(series);
                plot.setDataset(num, dataset);
                renderer = new XYLineAndShapeRenderer(true, true);
                renderer.setDrawSeriesLineAsPath(true);
                plot.setRenderer(num, renderer);
                renderer.setSeriesPaint(0, COLORS[colorIndex % COLORS.length]);
                renderer.setSeriesStroke(0, stroke);
            }
            else
            {
                renderer = (XYLineAndShapeRenderer) coll.renderer;
                dataset = coll.dataset;

                dataset.addSeries(series);
                if (showCrosshair)
                    coll.addMarker(seriesName, plot);

                renderer.setSeriesPaint(dataset.getSeriesCount() - 1, COLORS[colorIndex % COLORS.length]);
                
                if (coll.stroke != null)
                    renderer.setSeriesStroke(dataset.getSeriesCount() - 1, coll.stroke);
                else
                    renderer.setSeriesStroke(dataset.getSeriesCount() - 1, stroke);

                num = coll.index;

                skipPut = true;
            }

            itemName = inCollection;
        }
        else
        {
            num = getNextIndex();
            dataset = new TimeSeriesCollection(series);
            plot.setDataset(num, dataset);
            renderer = new XYLineAndShapeRenderer(true, false);
            plot.setRenderer(num, renderer);
            renderer.setSeriesPaint(0, COLORS[colorIndex % COLORS.length]);
            renderer.setSeriesStroke(0, stroke);            

            itemName = seriesName;
        }

        if (!datasetMap.containsKey(itemName) && !skipPut) // Checks to see if a 
        // series under the name
        // `itemName` already exists
        {
            DataContainer cont =  new DataContainer(dataset, renderer, num);
            if (showCrosshair)
                cont.addMarker(seriesName, plot);
            
            datasetMap.put(itemName, cont);
            colorIndex++;

            return series;
        }
        else if (skipPut)
        {
            colorIndex++;

            return series;
        }
        else
        {
            return null; // if there is already a series under `seriesName`
        }
    }

    /**
     * Creates a new TimeSeries and stores it in the map. Does not pair with
     * another TimeSeries to create a shaded area. Does include crosshairs
     *
     * @param seriesName Unique name of the new TimeSeries. Used as a key in the
     * map of DataContainer objects.
     *
     * @return new TimeSeries, or null if the seriesName was invalid
     */
    protected TimeSeries createTimeSeries(String seriesName)
    {
        return createTimeSeries(seriesName, null, true);
    }
    
    /**
     * Creates a new TimeSeries and stores it in the map. Does include crosshair
     * in the series.
     *
     * @param seriesName Unique name of the new TimeSeries. Used as a key in the
     * map of DataContainer objects.
     *
     * @return new TimeSeries, or null if the seriesName was invalid
     */
    protected TimeSeries createTimeSeries(String seriesName, String inCollection)
    {
        return createTimeSeries(seriesName, inCollection, true);
    }

    /**
     * Sets a collection to render with XYDifferenceRenderer.
     *
     * @param collectionName The unique key with which to identify the
     * collection that should be set to shade.
     * @param c The parameter specifies the color with which the area between
     * the two curves will be shaded.
     *
     * @return True if the collection was shaded correctly; false if there is no
     * collection with that name.
     */
    protected boolean shadeCollection(String collectionName, Color c)
    {
        DataContainer coll = datasetMap.get(collectionName);
        if (coll != null)
        {
            XYDifferenceRenderer rend = new XYDifferenceRenderer(c, c, false);
            plot.setRenderer(coll.index, rend);
            coll.renderer = rend;
            
            for (int i = 0; i < coll.dataset.getSeriesCount(); i++)
            {
                coll.renderer.setSeriesPaint(i, Color.GRAY);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the named TimeSeries. If the name does not exist in the
     * TimeSeries map, then a new TimeSeries entry is created in the map under
     * the given name.
     *
     * @param seriesName Unique name of the TimeSeries object. If no
     * collectionName is given, this is also assumed to be the name of the
     * collection.
     *
     * @return new or existing TimeSeries
     */
    protected TimeSeries getTimeSeries(String seriesName, String collectionName)
    {
        DataContainer cont = datasetMap.get(collectionName);
        TimeSeries series;

        if (cont == null)
        {
            cont = datasetMap.get(seriesName);
            if (cont != null)
            {
                series = (TimeSeries) datasetMap.get(seriesName).dataset.getSeries(seriesName);
            }
            else
            {
                series = createTimeSeries(seriesName, collectionName, true);
            }
        }
        else
        {
            series = (TimeSeries) cont.dataset.getSeries(seriesName);

            if (series == null)
            {
                series = createTimeSeries(seriesName, collectionName, true);

                if (cont.allHidden)
                {
                    changeVisibility(seriesName, collectionName, false);
                }
            }
        }

        return series;
    }
    
    protected YIntervalSeries getErrorSeries(String seriesName, String collectionName)
    {
        DataContainer cont;
        
        if (collectionName != null && datasetMap.containsKey(collectionName))
        {
            cont = datasetMap.get(collectionName);
        }
        else if (datasetMap.containsKey(seriesName))
        {
            cont = datasetMap.get(seriesName);
        }
        else
        {
            System.err.println("point not added");
            return null;
        }
        
        if (cont.errorDataset == null)
        {
            int num = getNextIndex();
            cont.errorDataset = new YIntervalSeriesCollection();
            plot.setDataset(num, cont.errorDataset);
            cont.errorRenderer = new XYErrorRenderer();
            plot.setRenderer(num, cont.errorRenderer);
        }
        
        int index = cont.errorDataset.indexOf(seriesName);
        if (index >= 0)
        {
            return cont.errorDataset.getSeries(index);
        }
        else
        {
            YIntervalSeries series = new YIntervalSeries(seriesName);
            cont.errorDataset.addSeries(series);
            cont.errorRenderer.setSeriesPaint(cont.errorDataset.getSeriesCount() - 1, cont.renderer.getSeriesPaint(cont.dataset.indexOf(seriesName)));
            return series;
        }
    }

    /**
     * Adds the given data value at the given time period for the TimeSeries of
     * the given name.
     *
     * @param seriesName Name of the TimeSeries
     * @param collectionName Name of the collection the TimeSeries is in
     * @param period Time
     * @param value
     */
    protected void addDataPoint(String seriesName, String collectionName, RegularTimePeriod period, double value)
    {
        long now = System.currentTimeMillis();
        
        TimeSeries series = getTimeSeries(seriesName, collectionName);
        series.addOrUpdate(period, value);

        if (series.getTimePeriod(series.getItemCount() - 1).equals(period))
        {
            attachHeader(seriesName, collectionName, period, value);
        }

        updateRange();
        int min = 0;
        int max = (int) (period.getLastMillisecond() - firstTime); //not a problem because the system will never run for more than int's maximum
        if (max < scrollbar.getMaximum())
        {
            max = scrollbar.getMaximum();
        }
        else if (live)
        {
            setDomainMax(period.getLastMillisecond());
        }
        int val = scrollbar.getValue();
        if (live)
        {
            val = max;
        }
        scrollbar.setValues(val, timeWindow, min, max);
    }

    /**
     * Adds the given data value at the given time period for the TimeSeries of
     * the given name.
     *
     * @param seriesName Name of the TimeSeries, also assumed to be the name of
     * the collection
     * @param period Time
     * @param value
     */
    protected void addDataPoint(String seriesName, RegularTimePeriod period, double value)
    {
        addDataPoint(seriesName, null, period, value);
    }
    
    protected void addDataPointWithError(String seriesName, String collectionName, RegularTimePeriod period, double value, double error)
    {
        addDataPoint(seriesName, collectionName, period, value);
        
        YIntervalSeries series = getErrorSeries(seriesName, collectionName);
        
        series.add(period.getFirstMillisecond(), value, value + error/2, value - error/2);
    }
    
    protected void addDataPointWithError(String seriesName, RegularTimePeriod period, double value, double error)
    {
        addDataPointWithError(seriesName, null, period, value, error);
    }
    
    /**
     * Updates the chart with the most recent data and scrolls forward if the chart
     * is set to live. Calls the updateData method, which should be implemented
     * in each individual chart to add all the new data points.
     */
    public synchronized void update()
    {
        boolean inside = false;
        if (chartPanel.getMousePosition() != null)
            inside = chartPanel.getScreenDataArea().contains(chartPanel.getMousePosition());
        
        if (!inside)
        {
            clearRule();
        }
        
        chart.setNotify(false);
        
        long cur = System.currentTimeMillis();
        int val = scrollbar.getValue();
        if (live)
        {
            val = (int)(cur - firstTime);
            setDomainMax(cur);
            
            if (inside)
                moveRule(xaxis.java2DToValue(lastMousePos, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge()), false);
        }
        
        if (firstTime == 0)
            firstTime = cur;
        
        scrollbar.setValues(val, timeWindow, scrollbar.getMinimum(), (int)(cur - firstTime));
        
        updateData();
        
        chart.setNotify(true);
    }
    
    /**
     * Called by the <code>update</code> method. Should pull down and add all 
     * new data points to their appropriate series.
     */
    protected abstract void updateData();

    /**
     * Multiplies all points in a series by some amount.
     *
     * @param seriesName Name of the TimeSeries
     * @param collectionName Name of the collection the TimeSeries is in, or
     * null to assume the value of seriesName
     * @param multiplier the amount to multiply each point by
     */
    protected void multiplyPoints(String seriesName, String collectionName, double multiplier)
    {
        TimeSeries series = getTimeSeries(seriesName, collectionName);
        
        if (series.getItemCount() > 0)
        {
            doMultiply(series, multiplier);
            
            updateRange();
        }
    }
    
    /**
     * Multiplies ALL series by the specified constant.
     * 
     * @param multiplier the amount to multiply by 
     */
    protected synchronized void multiplyPoints(double multiplier)
    {
        chart.setNotify(false);
        
        for (DataContainer cont : datasetMap.values())
        {
            for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            {
                doMultiply(cont.dataset.getSeries(i), multiplier);
            }
        }
        
        updateRange();

        chart.setNotify(true);
    }
    
    /**
     * Actually does the multiply operation.
     * 
     * @param series the series to operate on
     * @param multiplier the amount to multiply by
     */
    private void doMultiply(TimeSeries series, double multiplier)
    {
        for (int i = 0; i < series.getItemCount(); i++)
        {
            double newVal = series.getValue(i).doubleValue() * multiplier;
            series.update(i, newVal);
        }
    }
    
    /**
     * Removes all items from all series in the specified collection
     * 
     * @param collectionName the collection to be cleared
     */
    protected void clearCollection(String collectionName)
    {
        DataContainer cont = datasetMap.get(collectionName);
        
        if (cont != null)
        {
            for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            {
                cont.dataset.getSeries(i).clear();
            }
        }
    }

    /**
     * Adds a colored dot to the plot at the specified time and value.
     *
     * @param period the time period to position the dot at
     * @param value the value to position the dot at
     * @param c the color to make the dot
     */
    protected void addPOI(RegularTimePeriod period, double value, Color c)
    {
        long now = System.currentTimeMillis();
        DataContainer coll = datasetMap.get("POI");
        TimeSeries series;
        if (coll == null)
        {
            series = createTimeSeries("POI1", "POI", false);
            coll = datasetMap.get("POI");
            XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer(false, true);
            rend.setSeriesPaint(0, c);
            Shape circle = new Ellipse2D.Double(-5, -5, 10, 10);
            rend.setSeriesShape(0, circle);
            plot.setRenderer(coll.index, rend);
            coll.renderer = rend;
        }
        else
        {
            int seriesIndex = -1;
            for (int i = 0; i < coll.dataset.getSeriesCount(); i++)
            {
                if (coll.renderer.getSeriesPaint(i).equals(c))
                {
                    seriesIndex = i;
                }
            }
            if (seriesIndex == -1)
            {
                String name = "POI2";
                int count = 2;
                while (coll.dataset.getSeries(name) != null)
                {
                    name = "POI" + (++count);
                }
                series = new TimeSeries(name);
                coll.dataset.addSeries(series);
                coll.renderer.setSeriesPaint(coll.dataset.getSeriesCount() - 1, c);
                Shape circle = new Ellipse2D.Double(-MARKER_SIZE / 2, -MARKER_SIZE / 2, MARKER_SIZE, MARKER_SIZE);
                coll.renderer.setSeriesShape(coll.dataset.getSeriesCount() - 1, circle);
            }
            else
            {
                series = coll.dataset.getSeries(seriesIndex);
            }
        }

        if (now - period.getFirstMillisecond() <= timeWindow)
        {
            series.addOrUpdate(period, value);
        }
    }

    /**
     * Sets the icon to be displayed at the front of every series in the
     * specified collection.
     *
     * @param collectionName name of the collection to set the header for. Used
     * as the key in the dataset Map.
     * @param s the Shape to use for the header icon.
     * @return true if successful, false if the collectionName was invalid
     */
    protected boolean setHeader(String collectionName, Shape s)
    {
        DataContainer cont = datasetMap.get(collectionName);
        if (cont != null)
        {
            cont.header = s;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Hides or shows the specified series in the specified collection.
     *
     * @param seriesName name of the series
     * @param collectionName name of the collection, used as the key when
     * accessing the dataset Map if not null. If null, seriesName is used.
     * @param show if true, series will be set to display; if false, series will
     * be set to be hidden
     * @return true if successful, false otherwise
     */
    protected boolean changeVisibility(String seriesName, String collectionName, boolean show)
    {
        if (collectionName != null)
        {
            DataContainer cont = datasetMap.get(collectionName);
            if (cont != null && cont.dataset.indexOf(seriesName) != -1)
            {
                cont.renderer.setSeriesVisible(cont.dataset.indexOf(seriesName), show);
                if (cont.allHidden && show)
                {
                    cont.allHidden = false;
                }
                return true;
            }
            else if (cont != null && seriesName == null)
            {
                for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
                {
                    cont.renderer.setSeriesVisible(i, show);
                }
                cont.allHidden = !show;
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            DataContainer cont = datasetMap.get(seriesName);
            if (cont != null && cont.dataset.indexOf(seriesName) != -1)
            {
                cont.renderer.setSeriesVisible(cont.dataset.indexOf(seriesName), show);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Hides or shows the specified series in the specified collection.
     *
     * @param seriesName name of the series, assumed to also be the name of the
     * collection
     * @param show if true, series will be set to display; if false, series will
     * be set to be hidden
     * @return true if successful, false otherwise
     */
    protected boolean changeVisibility(String seriesName, boolean show)
    {
        return changeVisibility(seriesName, null, show);
    }

    /**
     * Removes the chart's legend.
     */
    protected void removeLegend()
    {
        chart.removeLegend();
    }
    
    /**
     * Matches the color of the first series specified to that of the second.
     * 
     * @param seriesName1 the name of the series to change the color of
     * @param collName1 the collection of the series to change to the color of
     * if null, seriesName1 is assumed to be the name of the collection as well
     * @param seriesName2 the name of the series with the color we will reference
     * @param collName2 the collection of the series with the color we will reference
     * if null, seriesName2 is assumed to be the name of the collection as well
     * 
     * @return true, if successful
     */
    protected boolean matchColor(String seriesName1, String collName1, String seriesName2, String collName2)
    {
        int series1, series2;
        XYItemRenderer rend1, rend2;
        
        if (collName1 != null)
        {
            DataContainer cont1 = datasetMap.get(collName1);
            if (cont1 != null && cont1.dataset.indexOf(seriesName1) != -1)
            {
                series1 = cont1.dataset.indexOf(seriesName1);
                rend1 = cont1.renderer;
            }
            else
            {
                return false;
            }
        }
        else
        {
            DataContainer cont1 = datasetMap.get(seriesName1);
            if (cont1 != null && cont1.dataset.indexOf(seriesName1) != -1)
            {
                series1 = cont1.dataset.indexOf(seriesName1);
                rend1 = cont1.renderer;
            }
            else
            {
                return false;
            }
        }
        
        if (collName2 != null)
        {
            DataContainer cont2 = datasetMap.get(collName2);
            if (cont2 != null && cont2.dataset.indexOf(seriesName2) != -1)
            {
                series2 = cont2.dataset.indexOf(seriesName2);
                rend2 = cont2.renderer;
            }
            else
            {
                return false;
            }
        }
        else
        {
            DataContainer cont2 = datasetMap.get(seriesName2);
            if (cont2 != null && cont2.dataset.indexOf(seriesName2) != -1)
            {
                series2 = cont2.dataset.indexOf(seriesName2);
                rend2 = cont2.renderer;
            }
            else
            {
                return false;
            }
        }
        
        rend1.setSeriesPaint(series1, rend2.getSeriesPaint(series2));
        
        return true;
    }
    
    /**
     * Matches the color of the first series specified to that of the second.
     * 
     * @param seriesName the name of the series
     * @param collectionName the collection of the series. If null, seriesName 
     * is assumed to be the name of the collection as well
     * @param stroke the stroke to be applied to the series
     * 
     * @return true, if successful
     */
    protected boolean setSeriesStroke(String seriesName, String collectionName, BasicStroke stroke)
    {
        int series;
        XYItemRenderer rend;
        
        if (collectionName != null)
        {
            DataContainer cont1 = datasetMap.get(collectionName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return false;
            }
        }
        else
        {
            DataContainer cont1 = datasetMap.get(seriesName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return false;
            }
        }
        
        rend.setSeriesStroke(series, stroke);
        
        return true;
    }

    /**
     * Adds an additional axis to the chart.
     *
     * @param seriesName the name of the series the axis is for
     * @param collectionName the name of the collection the series is in
     * @param axis the axis to be added
     * @return true if successful, false if series wasn't found
     */
    protected boolean addAxis(String seriesName, String collectionName, ValueAxis axis)
    {
        if (collectionName != null)
        {
            DataContainer cont = datasetMap.get(collectionName);
            if (cont != null)
            {
                plot.setRangeAxis(cont.index, axis);
                plot.mapDatasetToRangeAxis(cont.index, cont.index);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            DataContainer cont = datasetMap.get(seriesName);
            if (cont != null)
            {
                plot.setRangeAxis(cont.index, axis);
                plot.mapDatasetToRangeAxis(cont.index, cont.index);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * Sets up an interaction between this panel and a GenericHistogramPanel
     * wherein a click of a time on this panel changes which histogram is displayed.
     * Also enables the display features to go with this, such as the vertical rule.
     * 
     * @param hist The histogram panel to be controlled by this panel.
     */
    protected void setTimeFor(GenericHistogramPanel hist)
    {
        //plot.setDomainCrosshairVisible(true);
        
        histogram = hist;
    }
    
    /**
     * Handles all mouse events for the chart. Mouse events for all charts include:
     * moving the scroll wheel to zoom in or out. Mouse events only for histogram-controlling
     * charts include: clicking or clicking and dragging to select a time or 
     * time window that the associated histogram should display; moving the mouse
     * to move the vertical rule that is supposed to follow the mouse.
     */
    private class MouseResponder extends MouseAdapter
    {
        boolean isDown = false;
        double intervalStart;
        
        @Override
        public void mouseClicked(MouseEvent e)
        {
            moveInterval(0, 0);
            if (histogram != null)
            {
                double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
                long time = (long) xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());
            
                marker.setValue(time);
                histogram.goToTime(new Date(time));
            }
        }

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

                int old = timeWindow;
                int newWindow;
                if (e.getWheelRotation() < 0)
                {
                    newWindow = (int) ((3.0 / 4) * timeWindow);
                }
                else
                {
                    newWindow = (int) ((5.0 / 4) * timeWindow);
                }
                
                if (newWindow > 300000 || newWindow < 10000) // 5 minute max, 10 second min
                {
                    return;
                }
                else
                {
                    timeWindow = newWindow;
                }
                
                int delta = timeWindow - old;
                int newScroll = scrollbar.getValue() - delta;

                if (live)
                {
                    xaxis.setLowerBound(xaxis.getLowerBound() - delta);
                }
                else
                {
                    double newLower = xaxis.getLowerBound() - delta / 2.0;
                    double newUpper = xaxis.getUpperBound() + delta / 2.0;

                    if (newUpper > (long) scrollbar.getMaximum() + firstTime)
                    {
                        newLower = newLower - (newUpper - ((long) scrollbar.getMaximum() + firstTime));
                        newUpper = (long) scrollbar.getMaximum() + firstTime;
                        newScroll = scrollbar.getMaximum();
                        live = true;
                        btnLive.setEnabled(false);
                    }

                    xaxis.setLowerBound(newLower);
                    xaxis.setUpperBound(newUpper);
                }

                scrollbar.setValues(newScroll, timeWindow, scrollbar.getMinimum(), scrollbar.getMaximum());

                chart.setNotify(true);
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e)
        {
            isDown = true;
            
            if (histogram != null)
            {
                double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
                intervalStart = xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());

//                moveInterval(intervalStart, intervalStart);
                
                btnLive.setEnabled(true);
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e)
        {
            boolean shiftDown = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK;
            updateCrosshair(e.getPoint(), shiftDown);
            
            if (histogram != null)
            {
                marker.setValue(0);
                
                double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
                
                moveInterval(intervalStart, xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge()));
                
                histogram.goToTimeWindow(new Date((long)interval.getStartValue()), new Date((long)interval.getEndValue()));
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e)
        {
            isDown = false;
            
            if (histogram != null)
            {
                double x = chartPanel.translateScreenToJava2D(e.getPoint()).getX();
                double time = xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());

                moveInterval(intervalStart, time);
                histogram.goToTimeWindow(new Date((long)interval.getStartValue()), new Date((long)interval.getEndValue()));
            }
        }
        
        private void updateCrosshair(Point cursor, boolean shiftDown)
        {
            double x = chartPanel.translateScreenToJava2D(cursor).getX();
            lastMousePos = x;

            boolean inside = chartPanel.getScreenDataArea().contains(cursor);
            
            if (inside)
                moveRule(xaxis.java2DToValue(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge()), shiftDown);
            else
                clearRule();
        }
    }
    
    /**
     * Moves the vertical rule to a screen position x.
     * 
     * @param pos The Time value to move the rule to.
     * @param snap Indicates whether we should snap to real data points
     */
    private void moveRule(double pos, boolean snap)
    {
        rule.setStroke(XYPlot.DEFAULT_OUTLINE_STROKE);
        
        Millisecond evalTime;
        
        if (snap)
        {
            Millisecond closestLeft = null, closestRight = null;
            
            for (DataContainer cont : datasetMap.values())
            {
                for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
                {
                    TimeSeries series = cont.dataset.getSeries(i);

                    if (series.getItemCount() > 1)
                    {
                        int first = RendererUtilities.findLiveItemsLowerBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());
                        int last = RendererUtilities.findLiveItemsUpperBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());

                        for (int j = first; j <= last; j++)
                        {
                            if (series.getTimePeriod(j).getFirstMillisecond() > pos)
                            {
                                if (closestLeft == null || j > 0 && series.getTimePeriod(j - 1).compareTo(closestLeft) > 0)
                                    closestLeft = (Millisecond)series.getTimePeriod(j - 1);
                                if (closestRight == null || series.getTimePeriod(j).compareTo(closestRight) < 0)
                                    closestRight = (Millisecond)series.getTimePeriod(j);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (closestLeft == null || closestRight == null)
                return;
            
            evalTime = pos - closestLeft.getFirstMillisecond() < closestRight.getFirstMillisecond() - pos ? closestLeft : closestRight;
        }
        else
        {
            evalTime = new Millisecond(new Date((long)pos));
        }
        
        for (DataContainer cont : datasetMap.values())
        {
            for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            {
                TimeSeries series = cont.dataset.getSeries(i);
                ValueMarker crosshair = cont.crosshairs.get((String)series.getKey());
                
                if (crosshair != null)
                {
                    if (series.getItemCount() > 1 && (cont.renderer.getSeriesVisible(i) == null || cont.renderer.getSeriesVisible(i)))
                    {
                        crosshair.setStroke(XYPlot.DEFAULT_CROSSHAIR_STROKE);
                        
                        if (series.getValue(evalTime) != null)
                        {
                            crosshair.setValue(series.getValue(evalTime).doubleValue());
                            crosshair.setLabel(String.format("%2.2f", series.getValue(evalTime).doubleValue()));
                        }
                        else
                        {
                            int first = RendererUtilities.findLiveItemsLowerBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());
                            int last = RendererUtilities.findLiveItemsUpperBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());
                            boolean updated = false;
                            
                            for (int j = first; j <= last; j++)
                            {
                                if (series.getTimePeriod(j).compareTo(evalTime) > 0)
                                {
                                    Millisecond closestRight = (Millisecond)series.getTimePeriod(j);
                                    Millisecond closestLeft;
                                    if (j > 0)
                                        closestLeft = (Millisecond)series.getTimePeriod(j - 1);
                                    else
                                        closestLeft = closestRight;

                                    double slope = (series.getValue(closestRight).doubleValue() - series.getValue(closestLeft).doubleValue()) / (closestRight.getFirstMillisecond() - closestLeft.getFirstMillisecond());
                                    double val = (evalTime.getFirstMillisecond() - closestLeft.getFirstMillisecond()) * slope + series.getValue(closestLeft).doubleValue();
                                    
                                    crosshair.setValue(val);
                                    crosshair.setLabel(String.format("%2.2f", val));
                                    updated = true;
                                    break;
                                }
                            }
                            
                            if (!updated)
                            {
                                crosshair.setValue(Double.MIN_VALUE);
                                crosshair.setLabel("");
                            }
                        }
                    }
                    else
                    {
                        crosshair.setValue(Double.MIN_VALUE);
                        crosshair.setLabel("");
                    }
                }
            }
        }
        
        rule.setValue(evalTime.getFirstMillisecond());
    }
    
    private void clearRule()
    {
        rule.setStroke(new BasicStroke(0));
        
        for (DataContainer cont : datasetMap.values())
        {
            for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            {
                TimeSeries series = cont.dataset.getSeries(i);
                ValueMarker crosshair = cont.crosshairs.get((String)series.getKey());
                
                if (crosshair != null)
                {
                    crosshair.setStroke(new BasicStroke(0));
                    crosshair.setLabel("");
                }
            }
        }
    }
    
    /**
     * Moves the interval that selects what time range to display in this chart's
     * related histogram
     * @param startX the x-axis value to move the start to (NOT screen position)
     * @param stopX the x-axis value to move the stop to (NOT screen position)
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
    
    /**
     * Changes whether an individual collection is visible in the chart's legend
     * 
     * @param collectionName name of the collection
     * @param show true to show in legend, false to hide in legend
     */
    protected void setCollectionLegendVisible(String collectionName, boolean show)
    {
        DataContainer cont = datasetMap.get(collectionName);
        
        for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            cont.renderer.setSeriesVisibleInLegend(i, show);
    }
    
    /**
     * Changes the stroke to use when drawing the series in the named collection.
     * 
     * @param collectionName name of the collection
     * @param stroke stroke to apply
     */
    protected void setTimeSeriesCollectionStroke(String collectionName, Stroke stroke)
    {
        DataContainer cont = datasetMap.get(collectionName);
        
        if (cont != null)
        {
            cont.stroke = stroke;
        }
    }

    /**
     * Adds an additional axis to the chart.
     *
     * @param seriesName the name of the series the axis is for, assumed to be
     * collection name as well.
     * @param axis the axis to be added
     * @return true if successful, false if series wasn't found
     */
    protected boolean addAxis(String seriesName, ValueAxis axis)
    {
        return addAxis(seriesName, null, axis);
    }

    /**
     * @return the chart object
     */
    protected JFreeChart getChart()
    {
        return chart;
    }

    /**
     * Changes the label of the main y axis.
     * 
     * @param label the new label
     */
    protected void setAxisLabel(String label)
    {
        yaxis.setLabel(label);
    }
    
    /**
     * @return true if the plot always includes zero in the range, false otherwise
     */
    protected boolean includesZero()
    {
        return includeZero;
    }

    /**
     * @param includeZero set true to ensure that the plot always contains zero
     * in the range
     */
    protected void setIncludeZero(boolean includeZero)
    {
        this.includeZero = includeZero;
    }
    
    /**
     * @return the restrictPositive
     */
    protected boolean doesRestrictPositive()
    {
        return restrictPositive;
    }

    /**
     * @param restrictPositive the restrictPositive to set
     */
    protected void setRestrictPositive(boolean restrictPositive)
    {
        this.restrictPositive = restrictPositive;
    }
    
    /**
     * Retrieves the color that an individual series is being painted with.
     * 
     * @param seriesName the name of the series
     * @param collectionName the name of the collection. if null, seriesName is
     * also assumed to be the name of the collection
     * @return the series's color
     */
    protected Color getSeriesColor(String seriesName, String collectionName)
    {
        XYItemRenderer rend;
        int series;
        
        if (collectionName != null)
        {
            DataContainer cont1 = datasetMap.get(collectionName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return null;
            }
        }
        else
        {
            DataContainer cont1 = datasetMap.get(seriesName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return null;
            }
        }
        
        return (Color) rend.getSeriesPaint(series);
    }
    
    /**
     * Retrieves the color that an individual series is being painted with.
     * 
     * @param seriesName the name of the series, also assumed to be the name of
     * the collection
     * @return the series's color
     */
    public Color getSeriesColor(String seriesName)
    {
        return getSeriesColor(seriesName, null);
    }
    
    /**
     * Sets the color that an individual series is being painted with.
     * 
     * @param seriesName the name of the series
     * @param collectionName the name of the collection. if null, seriesName is
     * also assumed to be the name of the collection
     * @param c the series's color
     */
    protected void setSeriesColor(String seriesName, String collectionName, Color c)
    {
        XYItemRenderer rend;
        int series;
        
        if (collectionName != null)
        {
            DataContainer cont1 = datasetMap.get(collectionName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return;
            }
        }
        else
        {
            DataContainer cont1 = datasetMap.get(seriesName);
            if (cont1 != null && cont1.dataset.indexOf(seriesName) != -1)
            {
                series = cont1.dataset.indexOf(seriesName);
                rend = cont1.renderer;
            }
            else
            {
                return;
            }
        }
        
        rend.setSeriesPaint(series, c);
    }
    
    /**
     * Sets the color that an individual series is being painted with.
     * 
     * @param seriesName the name of the series, also assumed to be the name of
     * the collection
     * @param c the series's color
     */
    public void setSeriesColor(String seriesName, Color c)
    {
        setSeriesColor(seriesName, null, c);
    }

    /**
     * Gets an index for a renderer and dataset/collection
     *
     * @return the next free index in the plot
     */
    private int getNextIndex()
    {
        return plot.getDatasetCount() > plot.getRendererCount() ? plot.getDatasetCount() : plot.getRendererCount();
    }

    /**
     * Places a header icon onto the chart at the specified location. (The
     * correct location should be the front of whatever series the header is
     * associated with.)
     *
     * @param seriesName the name of the series
     * @param collectionName the name of the collection
     * @param period the period to anchor the header at
     * @param val the value to anchor the header at
     */
    private void attachHeader(String seriesName, String collectionName, RegularTimePeriod period, double val)
    {
        DataContainer cont = datasetMap.get(collectionName);
        Shape header;
        if (cont != null)
        {
            header = cont.header;
        }
        else
        {
            cont = datasetMap.get(seriesName);
            if (cont != null)
            {
                header = cont.header;
            }
            else
            {
                header = null;
            }
        }

        if (header != null)
        {
            TimeSeries series = headers.getSeries(seriesName);
            int index;
            if (series != null)
            {
                series.clear();
                series.add(period, val);
                index = headers.indexOf(series);
            }
            else
            {
                index = headers.getSeriesCount();
                series = new TimeSeries(seriesName);
                series.add(period, val);
                headers.addSeries(series);
            }
            headRenderer.setSeriesShape(index, header);
            headRenderer.setSeriesPaint(index, Color.BLACK);
        }
    }
    
    /**
     * Sets the vertical range for the series. In order to do so, this method
     * scans through all displayed series to find the highest and lowest values
     * and makes adjustments based on that if necessary. The reason we cannot
     * record max and min as data is added to the chart is that we only care
     * about the max and min displayed in the current window, which can change at
     * any time.
     */
    private void updateRange()
    {
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        
        for (DataContainer cont : datasetMap.values())
        {
            for (int i = 0; i < cont.dataset.getSeriesCount(); i++)
            {
                TimeSeries series = cont.dataset.getSeries(i);
                
                if (series.getItemCount() > 0)
                {
                    int first = RendererUtilities.findLiveItemsLowerBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());
                    int last = RendererUtilities.findLiveItemsUpperBound(cont.dataset, i, xaxis.getLowerBound(), xaxis.getUpperBound());
    //                int first = series.getIndex(new Millisecond(new Date((long)xaxis.getLowerBound())));
    //                int last = series.getIndex(new Millisecond(new Date((long)xaxis.getUpperBound())));

                    for (int j = first; j <= last; j++)
                    {
                        if (series.getValue(j).doubleValue() < min)
                            min = series.getValue(j).doubleValue();
                        if (series.getValue(j).doubleValue() > max || max == Double.MIN_VALUE)
                            max = series.getValue(j).doubleValue();
                    }
                }
            }
        }
        
        if (max != Double.MIN_VALUE && min != Double.MAX_VALUE)
        {
            if (max - min < minRange)
            {
                double diff = minRange - (max - min);
                
                max += diff / 2;
                min -= diff / 2;
            }
            
            // ---
            
            if (Double.isNaN(yaxis.getUpperBound() - yaxis.getLowerBound()))
            {
                yaxis.setUpperBound(max + (max - min) * 0.15);
                yaxis.setLowerBound(min - (max - min) * 0.15);
            }
            else
            {
                double high = yaxis.getUpperBound(), low = yaxis.getLowerBound();

                if (max > yaxis.getUpperBound() - (high - low) * 0.1)
                {
                    high = max + (high - low) * 0.5;
                }
                else if (max < yaxis.getUpperBound() - (high - low) * 0.5)
                {
                    high = max + (high - low) * 0.1;
                }

                if (min < yaxis.getLowerBound() + (high - low) * 0.1)
                {
                    low = min - (high - low) * 0.5;
                }
                else if (min > yaxis.getLowerBound() + (high - low) * 0.5)
                {
                    low = min - (high - low) * 0.1;
                }

                if (low < 0 && doesRestrictPositive() || (low > 0 && high > 0) && includeZero)
                {
                    low = 0;
                }
                else if (low < 0 && high < 0 && (includeZero || doesRestrictPositive()))
                {
                    high = 0;
                }

                yaxis.setLowerBound(low);
                yaxis.setUpperBound(high);
            }
        }
        else
        {
            // No way to set range, so leave it be.
        }
    }
    
    public void setMinimumRange(double range)
    {
        minRange = range;
    }
    
    public double getMinimumRange()
    {
        return minRange;
    }

    /**
     * Searches the specified dataset and looks for the the first series in that
     * dataset to have an item at the specified time period.
     * 
     * @param dataset the dataset to search
     * @param period the TimePeriod to check
     * @return the data item found, or null if nothing was found
     */
    private TimeSeriesDataItem findItemAtPeriod(TimeSeriesCollection dataset, RegularTimePeriod period)
    {
        TimeSeriesDataItem cur = null;
        int pos = 0;
        while (cur == null && pos < dataset.getSeriesCount())
        {
            cur = dataset.getSeries(pos).getDataItem(period);
            pos++;
        }
        return cur;
    }

    /**
     * Listener for the scrollbar
     * 
     * @param e the event object
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        live = false;
        btnLive.setEnabled(true);
        setDomainMax(firstTime + (long) e.getValue() + timeWindow);
//        moveRule(lastMousePos, false);
        // ^ e.getAdjustable().getVisibleAmount() == timeWindow

        if (!e.getValueIsAdjusting() && e.getValue() + timeWindow == e.getAdjustable().getMaximum())
        {
            goLive();
        }
    }
    
    /**
     * Sets the upper and lower bounds of the time axis using the provided
     * upperbound and the instance variable <code>timeWindow</code>, which
     * represents the amount of time that should be displayed on the chart.
     * 
     * @param time the upper bound to set
     */
    private void setDomainMax(long time)
    {
        plot.getDomainAxis().setUpperBound(time + PLOT_PADDING_X);
        plot.getDomainAxis().setLowerBound(time - timeWindow);
    }

    /**
     * The listener for the "Go Live" button. Be sure to call this in any subclass
     * if that subclass implements its own actionPerformed listener.
     * 
     * @param e the event object
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (btnLive.equals(e.getSource()))
        {
            goLive();
        }
    }

    /**
     * Returns the scrollbar and display window to the front of the incoming data
     * stream and sets the necessary flags so that the window continues to follow
     * the data stream. Essentially returns the chart to default behavior after
     * history was viewed using the scroll bar.
     */
    private void goLive()
    {
        live = true;
        scrollbar.setValues(scrollbar.getMaximum(), scrollbar.getVisibleAmount(), scrollbar.getMinimum(), scrollbar.getMaximum());
        setDomainMax(firstTime + (long) scrollbar.getMaximum());
        if (histogram != null) {
            histogram.goLive();
        }
        btnLive.setEnabled(false);
        marker.setValue(0);
        moveInterval(0, 0);
    }
}
/**
 * Holds extraneous information for each time series. Created to enable the
 * ability to put different time series in different renderers. This ability
 * means that some series can be used to build a shaded region or other special
 * feature.
 *
 * @author fishmsm1
 */
class DataContainer
{

    public TimeSeriesCollection dataset;
    public XYItemRenderer renderer;
    public Integer index; // the index of the Collection
    public HashMap<String, ValueMarker> crosshairs;
    public Shape header;
    public boolean allHidden;
    public Stroke stroke;
    
    public YIntervalSeriesCollection errorDataset;
    public XYErrorRenderer errorRenderer;

    public DataContainer(TimeSeriesCollection coll, XYItemRenderer rend, Integer seriesIndex, Shape shape)
    {
        dataset = coll;
        renderer = rend;
        index = seriesIndex;
        header = shape;
        allHidden = false;
        
        crosshairs = new HashMap<String, ValueMarker>();
    }

    public DataContainer(TimeSeriesCollection coll, XYItemRenderer rend, Integer seriesIndex)
    {
        this(coll, rend, seriesIndex, null);
    }
    
    public void addMarker(String seriesName, XYPlot plot)
    {
        ValueMarker mark = new ValueMarker(Integer.MIN_VALUE);
        mark.setPaint(Color.BLUE.darker());
        mark.setStroke(XYPlot.DEFAULT_CROSSHAIR_STROKE);
        mark.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        RectangleInsets inset = new RectangleInsets(10, 0, 0, 30);
        mark.setLabelOffset(inset);
        mark.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        mark.setLabelPaint(Color.WHITE);
        
        plot.addRangeMarker(index, mark, Layer.FOREGROUND);
        crosshairs.put(seriesName, mark);
    }
}
