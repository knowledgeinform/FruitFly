/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Graph for total counts and altitude of RN data
 *
 * @author humphjc1
 */
public class RNTotalCountsDisplayGraphPanel extends javax.swing.JPanel
{
    /**
     * Object to track altitude and total counts for an instance in time
     */
    public class CountsAltitudeTime
    {
        public long m_TimeMs;
        public int m_TotalCounts;
        public double m_AltitudeM;
    }

    /**
     * List of counts and altitude values
     */
    LinkedList <CountsAltitudeTime> m_CountsAltitudeTimeList = new LinkedList <CountsAltitudeTime> ();


    /**
     * A copy of the most recent graphics object that was passed in to be painted
     */
    Graphics2D g2d = null;

    /**
     * Title of plot
     */
    private String m_Title = "Count Graph";

    /**
     * Min counts to plot
     */
    int counts_min = 0;
    
    /**
     * Max counts to plot
     */
    int counts_max = 0;

    /**
     * Min altitude to plot
     */
    double alt_minM = 0;

    /**
     * Max altitude to plot
     */
    double alt_maxM = 0;

    /**
     * Maximum time span to be plotted
     */
    long m_MaxTimeSpanMs = 0;
    
    /**
     * Timestamp of first (least recent) data to plot
     */
    long m_FirstTimeMs = 0;

    /**
     * Actual timespan of plotted data
     */
    long m_TimeSpanS = 0;

    /**
     * Counts y-axis label
     */
    private String m_Y1AxisLabel = "Counts";

    /**
     * Altitude y-axis label
     */
    private String m_Y2AxisLabel = "Altitude (m)";

    /**
     * x-axis label
     */
    private String m_XAxisLabel = "Time Since (s)";

    /**
     * Detection message to display, based on processing results
     */
    private String m_DetectionMessage = "No Detections";

    /**
     * Statistics message to display, based on processing results
     */
    private String m_StatMessage = "No Statistics";

    /**
     * Statistics alert message to display, based on history of total counts
     */
    private String m_StatAlertMessage = "";
    
    /** buffer values for edge of plotter.  x-axis will start x_min pixels from
     * the left edge and stop x_max pixels from the right edge.  y-axis will start
     * y_min pixels from the top edge and y_max pixels from the bottom edge
     */
    int m_LeftBuffer = 60;
    int m_RightBuffer = 60;
    int m_TopBuffer = 25;
    int m_BottomBuffer = 80;

    /**
     * Create graph panel.  Sets max timespan for plotting.
     * @param maxTimeSpanMs
     */
    public RNTotalCountsDisplayGraphPanel(long maxTimeSpanMs)
    {
        super (true);
        initComponents();
        counts_max = 0;

        m_MaxTimeSpanMs = maxTimeSpanMs;
    }

    /**
     * Sets a new datapoint for plotting
     * @param counts Total counts for this point
     * @param altitudeM Altitude for this point
     * @param timeMs Timestamp of the data point
     */
    public void updateCountsAltitudeTime(int counts, double altitudeM, long timeMs)
    {
        //Create data object for data
        CountsAltitudeTime newCAT = new CountsAltitudeTime();
        newCAT.m_TotalCounts = counts;
        newCAT.m_AltitudeM = altitudeM;
        newCAT.m_TimeMs = timeMs;


        //Remove all data that is too long ago (outside of max time window)
        Iterator <CountsAltitudeTime> catItr = m_CountsAltitudeTimeList.iterator();
        while (catItr.hasNext())
        {
            CountsAltitudeTime firstCAT = catItr.next();
            if (newCAT.m_TimeMs - firstCAT.m_TimeMs > m_MaxTimeSpanMs)
                catItr.remove();
            else
            {
                m_FirstTimeMs = firstCAT.m_TimeMs;
                break;
            }
        }

        //Add new data to end of list
        m_CountsAltitudeTimeList.add(newCAT);
        if (m_CountsAltitudeTimeList.size()==1)
            m_FirstTimeMs = newCAT.m_TimeMs;
        m_TimeSpanS = (newCAT.m_TimeMs - m_FirstTimeMs)/1000;


        //Get plotting limits of data
        catItr = m_CountsAltitudeTimeList.iterator();
        counts_max = 0;
        counts_min = 0;
        alt_maxM = 0;
        alt_minM = 0;
        while (catItr.hasNext())
        {
            CountsAltitudeTime currCAT = catItr.next();
            if (currCAT.m_TotalCounts > counts_max)
                counts_max = currCAT.m_TotalCounts;
            if (currCAT.m_TotalCounts < counts_min)
                counts_min = currCAT.m_TotalCounts;

            if (currCAT.m_AltitudeM > alt_maxM)
                alt_maxM = currCAT.m_AltitudeM;
            if (currCAT.m_AltitudeM < alt_minM)
                alt_minM = currCAT.m_AltitudeM;
        }

        //Paint panel
        repaint();
    }

    /**
     * Return the screen coordinate relating to a certain x-value
     *
     * @param value x-value to find screen coordinate for
     * @return Screen coordinate for the value
     */
    private int getScreenX(long value)
    {
        double percentFromMin = 0;
        if (m_TimeSpanS == 0)
            percentFromMin = .5;
        else
            //percentFromMin = (value) / ((double) (m_LastData_Histogram.getNumBins()));
            percentFromMin = (value-m_FirstTimeMs) / ((double) (m_TimeSpanS*1000));
        int screenX = (int)(m_LeftBuffer + (this.getWidth() - m_RightBuffer - m_LeftBuffer)*percentFromMin);

        return screenX;
    }

    /**
     * Return the screen coordinate relating to a certain y-value on the counts scale
     *
     * @param value y-value to find screen coordinate for
     * @return Screen coordinate for the value
     */
    private int getScreenYCounts(int value)
    {
        double percentFromMin = 0;

        percentFromMin = (value-counts_min)/((double)(counts_max - counts_min));
        int screenY = (int)(this.getHeight() - m_BottomBuffer - (this.getHeight() - m_TopBuffer - m_BottomBuffer)*percentFromMin);

        return screenY;
    }

    /**
     * Return the screen coordinate relating to a certain y-value on the altitude scale
     *
     * @param value y-value to find screen coordinate for
     * @return Screen coordinate for the value
     */
    private int getScreenYAltitude(double value)
    {
        double percentFromMin = 0;

        percentFromMin = (value-alt_minM)/((double)(alt_maxM - alt_minM));
        int screenY = (int)(this.getHeight() - m_BottomBuffer - (this.getHeight() - m_TopBuffer - m_BottomBuffer)*percentFromMin);

        return screenY;
    }

    /**
     * Paint panel
     * @param g
     */
    @Override
    public void paintComponent (Graphics g) {

        super.paintComponent(g);



        g2d = (Graphics2D) g;

        //Draw axes
        g2d.drawLine (m_LeftBuffer, m_TopBuffer, m_LeftBuffer, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (m_LeftBuffer+1, m_TopBuffer, m_LeftBuffer+1, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (this.getWidth()-m_RightBuffer, m_TopBuffer, this.getWidth()-m_RightBuffer, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (this.getWidth()-m_RightBuffer-1, m_TopBuffer, this.getWidth()-m_RightBuffer-1, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (m_LeftBuffer, this.getHeight()-m_BottomBuffer, this.getWidth()-m_RightBuffer, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (m_LeftBuffer, this.getHeight()-m_BottomBuffer+1, this.getWidth()-m_RightBuffer, this.getHeight()-m_BottomBuffer+1);

        long timeSpanActualS = m_TimeSpanS;
        //Screen space for x-axis data
        int x_range = this.getWidth() - (m_LeftBuffer+m_RightBuffer);
        //Estimate of how many tick marks can be used so that ticks are spaced nicely
        int x_ticks = (int)(x_range / 35.0);

        if (x_range < 1)
            x_range = 1;
        if (x_ticks < 1)
            x_ticks = 1;

        // this parameter is the interval for ticks in the x-direction and is updated
        // based on the range.  It prevents labels from overlapping.  Uses the estimate x_ticks,
        // but makes the interval a nice number
        int x_ticks_act = 0;
        if ((double)m_TimeSpanS/(double)x_ticks < (1.0 + 0.00001)) {
            x_ticks_act = 1;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (2.0 + 0.00001)) {
            x_ticks_act = 2;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (5.0 + 0.00001)) {
            x_ticks_act = 5;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (10.0 + 0.00001)) {
            x_ticks_act = 10;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (25.0 + 0.00001)) {
            x_ticks_act = 25;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (50.0 + 0.00001)) {
            x_ticks_act = 50;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (100.0 + 0.00001)) {
            x_ticks_act = 100;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (200.0 + 0.00001)) {
            x_ticks_act = 200;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (500.0 + 0.00001)) {
            x_ticks_act = 500;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (1000.0 + 0.00001)) {
            x_ticks_act = 1000;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (2000.0 + 0.00001)) {
            x_ticks_act = 2000;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (5000.0 + 0.00001)) {
            x_ticks_act = 5000;
        }
        else if ((double)m_TimeSpanS/(double)x_ticks < (10000.0 + 0.00001)) {
            x_ticks_act = 10000;
        }

        try {
            //Adjust time span (x-values) for actual displayed ticks based on the prior adjustment
            if (Math.abs(m_TimeSpanS)%x_ticks_act != 0)
                timeSpanActualS += x_ticks_act-(Math.abs(timeSpanActualS) %x_ticks_act);

            g2d.drawString (""+timeSpanActualS, m_LeftBuffer, this.getHeight()-m_BottomBuffer+20);

            for (int i = x_ticks_act; i <= (timeSpanActualS); i += x_ticks_act)
            {
                //Draw ticks
                g2d.drawLine ((int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(double)(timeSpanActualS)), this.getHeight()-m_BottomBuffer-2,
                        (int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(timeSpanActualS)), this.getHeight()-m_BottomBuffer+3);

                //Paint value for ticks
                g2d.drawString (new Integer(0+((int)timeSpanActualS-i)).toString(), (int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(timeSpanActualS)), this.getHeight()-m_BottomBuffer+20);
            }

            m_TimeSpanS = timeSpanActualS;
            m_FirstTimeMs = m_CountsAltitudeTimeList.getLast().m_TimeMs - m_TimeSpanS*1000;
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }

        //Paint y-axis and data points
        paintData (g);


        //Must be last !!!
        //Paint plot title, x-axis, and detection/stat messages
        g2d.setColor (Color.black);
        g2d.drawLine(0,this.getHeight() - 22, this.getWidth(), this.getHeight() - 22);
        g2d.drawLine(0,this.getHeight() - 39, this.getWidth(), this.getHeight() - 39);
        Font f = g2d.getFont();
        Font boldArial = new Font("Arial", Font.BOLD, 12);
        g2d.setFont(boldArial);
        g2d.drawString (getTitle(), this.getWidth()/2 - getTitle().length()*3, 20);
        g2d.drawString (getXAxisLabel(), this.getWidth()/2, this.getHeight() - m_BottomBuffer + 35);
        g2d.setFont(f);
        g2d.drawString (getDetectionMessage(), 20, this.getHeight() - 10);
        g2d.drawString (getStatMessage(), 20, this.getHeight() - 26);

        //Paint stat alert message next to stat message
        g2d.setFont(boldArial);
        g2d.setColor (Color.red);
        g2d.drawString (getStatAlertMessage(), (int)(this.getWidth()*0.6), this.getHeight() - 26);
        g2d.setColor (Color.black);

        //Paint y-axis labels rotated appropriately
        AffineTransform af = new AffineTransform();
        af.translate (20., (this.getHeight()-m_BottomBuffer-m_TopBuffer)/2 + m_TopBuffer);
        af.rotate (-Math.PI/2);
        FontRenderContext renderContext = new FontRenderContext (null, false, false);
        g2d.transform(af);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.blue);
        TextLayout layout = new TextLayout (getY1AxisLabel(), g2d.getFont(), renderContext);
        g2d.setFont(f);
        layout.draw(g2d, 0, 0);

        AffineTransform af2 = new AffineTransform();
        af2.translate (0, this.getWidth() - 30);
        FontRenderContext renderContext2 = new FontRenderContext (null, false, false);
        g2d.transform(af2);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.red);
        TextLayout layout2 = new TextLayout (getY2AxisLabel(), g2d.getFont(), renderContext2);
        g2d.setFont(f);
        layout2.draw(g2d, 0, 0);
    }

    /**
     * Paint tick marks and data points for y-scale
     * @param g
     */
    public void paintData (Graphics g)
    {
        //Counts side
        if (counts_max < 1)
            counts_max = 1;

        int count_range = counts_max - counts_min;
        //Screen space for y-axis data
        int y_range = this.getHeight() - (m_TopBuffer+m_BottomBuffer);
        //Estimate of how many tick marks can be used so that ticks are spaced nicely
        int y_ticks = (int)(y_range/25.0);

        if (y_range < 1)
            y_range = 1;
        if (y_ticks < 1)
            y_ticks = 1;

        // this parameter is the interval for ticks in the y-direction and is updated
        // based on the range.  It prevents labels from overlapping.  Uses the estimate y_ticks,
        // but makes the interval a nice number
        int y_ticks_act = 0;
        if (count_range/(double)y_ticks < (1.0 + 0.00001)) {
            y_ticks_act = 1;
        }
        else if (count_range/(double)y_ticks < (2.0 + 0.00001)) {
            y_ticks_act = 2;
        }
        else if (count_range/(double)y_ticks < (5.0 + 0.00001)) {
            y_ticks_act = 5;
        }
        else if (count_range/(double)y_ticks < (10.0 + 0.00001)) {
            y_ticks_act = 10;
        }
        else if (count_range/(double)y_ticks < (25.0 + 0.00001)) {
            y_ticks_act = 25;
        }
        else if (count_range/(double)y_ticks < (50.0 + 0.00001)) {
            y_ticks_act = 50;
        }
        else if (count_range/(double)y_ticks < (100.0 + 0.00001)) {
            y_ticks_act = 100;
        }
        else if (count_range/(double)y_ticks < (200.0 + 0.00001)) {
            y_ticks_act = 200;
        }
        else if (count_range/(double)y_ticks < (500.0 + 0.00001)) {
            y_ticks_act = 500;
        }
        else if (count_range/(double)y_ticks < (1000.0 + 0.00001)) {
            y_ticks_act = 1000;
        }
        else if (count_range/(double)y_ticks < (2000.0 + 0.00001)) {
            y_ticks_act = 2000;
        }
        else if (count_range/(double)y_ticks < (5000.0 + 0.00001)) {
            y_ticks_act = 5000;
        }
        else
        {
            int value = ((int)((count_range)/((double)y_ticks)));
            int nextHighestTen = 10;
            while ((value/=10) >= 1)
            {
                nextHighestTen *= 10;
            }

            y_ticks_act = nextHighestTen;
        }
        try {
            //Adjust counts range for actual displayed ticks based on the prior adjustment
            if (Math.abs(count_range)%y_ticks_act != 0)
                counts_max += y_ticks_act-(Math.abs(count_range) %y_ticks_act);

            for (int i = 0; i <= (count_range); i += y_ticks_act)
            {
                //Draw the tick
                g2d.drawLine (m_LeftBuffer-2, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(counts_max-counts_min))),
                        m_LeftBuffer+2,(int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(counts_max-counts_min))));

                //Draw the value of the tick
                g2d.drawString (new Integer(counts_max-(i)).toString(), m_LeftBuffer-35, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(counts_max-counts_min))));
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }


        if (m_CountsAltitudeTimeList.size() < 2)
            return;

        Iterator <CountsAltitudeTime> catItr = m_CountsAltitudeTimeList.iterator();
        int lastX = -1, lastYCounts = -1, lastYAlt = -1;
        int currX = -1, currYCounts = -1, currYAlt = -1;
        //Scroll through counts data and draw lines between datapoints
        while (catItr.hasNext())
        {
            CountsAltitudeTime currCAT = catItr.next();

            currX = getScreenX(currCAT.m_TimeMs);
            currYCounts = getScreenYCounts(currCAT.m_TotalCounts);
            currYAlt = getScreenYAltitude(currCAT.m_AltitudeM);

            if (!(lastX == -1 && lastYCounts == -1 && lastYAlt == -1))
            {
                g2d.setColor (Color.blue);
                g2d.drawLine(lastX, lastYCounts, currX, currYCounts);
            }

            lastX = currX;
            lastYCounts = currYCounts;
            lastYAlt = currYAlt;
        }



        g2d.setColor (Color.black);
        //Altitude side
        if (alt_maxM < 1)
            alt_maxM = 1;

        int alt_range = (int)(alt_maxM - alt_minM);
        //Screen space for y-axis data
        y_range = this.getHeight() - (m_TopBuffer+m_BottomBuffer);
        //Estimate of how many tick marks can be used so that ticks are spaced nicely
        y_ticks = (int)(y_range/25.0);

        if (y_range < 1)
            y_range = 1;
        if (y_ticks < 1)
            y_ticks = 1;

        // this parameter is the interval for ticks in the y-direction and is updated
        // based on the range.  It prevents labels from overlapping.  Uses the estimate y_ticks,
        // but makes the interval a nice number
        y_ticks_act = 0;
        if (alt_range/(double)y_ticks < (1.0 + 0.00001)) {
            y_ticks_act = 1;
        }
        else if (alt_range/(double)y_ticks < (2.0 + 0.00001)) {
            y_ticks_act = 2;
        }
        else if (alt_range/(double)y_ticks < (5.0 + 0.00001)) {
            y_ticks_act = 5;
        }
        else if (alt_range/(double)y_ticks < (10.0 + 0.00001)) {
            y_ticks_act = 10;
        }
        else if (alt_range/(double)y_ticks < (25.0 + 0.00001)) {
            y_ticks_act = 25;
        }
        else if (alt_range/(double)y_ticks < (50.0 + 0.00001)) {
            y_ticks_act = 50;
        }
        else if (alt_range/(double)y_ticks < (100.0 + 0.00001)) {
            y_ticks_act = 100;
        }
        else if (alt_range/(double)y_ticks < (200.0 + 0.00001)) {
            y_ticks_act = 200;
        }
        else if (alt_range/(double)y_ticks < (500.0 + 0.00001)) {
            y_ticks_act = 500;
        }
        else if (alt_range/(double)y_ticks < (1000.0 + 0.00001)) {
            y_ticks_act = 1000;
        }
        else if (alt_range/(double)y_ticks < (2000.0 + 0.00001)) {
            y_ticks_act = 2000;
        }
        else if (alt_range/(double)y_ticks < (5000.0 + 0.00001)) {
            y_ticks_act = 5000;
        }
        else if (alt_range/(double)y_ticks < (10000.0 + 0.00001)) {
            y_ticks_act = 10000;
        }
        else if (alt_range/(double)y_ticks < (20000.0 + 0.00001)) {
            y_ticks_act = 20000;
        }
        else if (alt_range/(double)y_ticks < (50000.0 + 0.00001)) {
            y_ticks_act = 50000;
        }
        else
        {
            int value = ((int)((alt_range)/((double)y_ticks)));
            int nextHighestTen = 10;
            while ((value/=10) >= 1)
            {
                nextHighestTen *= 10;
            }

            y_ticks_act = nextHighestTen;
        }
        try {
            //Adjust counts range for actual displayed ticks based on the prior adjustment
            if (Math.abs(alt_range)%y_ticks_act != 0)
                alt_maxM += y_ticks_act-(Math.abs(alt_range) %y_ticks_act);

            for (int i = 0; i <= (alt_range); i += y_ticks_act)
            {
                //Draw the tick
                g2d.drawLine (this.getWidth()-m_RightBuffer-2, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(alt_maxM-alt_minM))),
                        this.getWidth()-m_RightBuffer+2,(int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(alt_maxM-alt_minM))));

                //Draw the value of the tick
                g2d.drawString (new Integer((int)alt_maxM-(i)).toString(), this.getWidth()-m_RightBuffer+5, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(alt_maxM-alt_minM))));
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }


        if (m_CountsAltitudeTimeList.size() < 2)
            return;

        catItr = m_CountsAltitudeTimeList.iterator();
        lastX = -1; lastYCounts = -1; lastYAlt = -1;
        currX = -1; currYCounts = -1; currYAlt =-1;
        //Scroll through altitude data and draw lines between datapoints
        while (catItr.hasNext())
        {
            CountsAltitudeTime currCAT = catItr.next();

            currX = getScreenX(currCAT.m_TimeMs);
            currYCounts = getScreenYCounts(currCAT.m_TotalCounts);
            currYAlt = getScreenYAltitude(currCAT.m_AltitudeM);

            if (!(lastX == -1 && lastYCounts == -1 && lastYAlt == -1))
            {
                g2d.setColor (Color.red);
                g2d.drawLine(lastX, lastYAlt, currX, currYAlt);
            }

            lastX = currX;
            lastYCounts = currYCounts;
            lastYAlt = currYAlt;
        }
    }

    



/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(1200, 300));
        setPreferredSize(new java.awt.Dimension(500, 300));
        setRequestFocusEnabled(false);
    }// </editor-fold>

    /**
     * @return the m_Title
     */
    public String getTitle() {
        return m_Title;
    }

    /**
     * @param m_Title the m_Title to set
     */
    public void setTitle(String m_Title) {
        this.m_Title = m_Title;
    }

    /**
     * @return the m_YAxisLabel
     */
    public String getY1AxisLabel() {
        return m_Y1AxisLabel;
    }

    /**
     * @param m_YAxisLabel the m_YAxisLabel to set
     */
    public void setY1AxisLabel(String m_YAxisLabel) {
        this.m_Y1AxisLabel = m_YAxisLabel;
    }

    /**
     * @return the m_YAxisLabel
     */
    public String getY2AxisLabel() {
        return m_Y2AxisLabel;
    }

    /**
     * @param m_YAxisLabel the m_YAxisLabel to set
     */
    public void setY2AxisLabel(String m_YAxisLabel) {
        this.m_Y2AxisLabel = m_YAxisLabel;
    }

    /**
     * @return the m_XAxisLabel
     */
    public String getXAxisLabel() {
        return m_XAxisLabel;
    }

    /**
     * @param m_XAxisLabel the m_XAxisLabel to set
     */
    public void setM_XAxisLabel(String m_XAxisLabel) {
        this.m_XAxisLabel = m_XAxisLabel;
    }

    /**
     * @return the m_DetectionMessage
     */
    public String getDetectionMessage() {
        return m_DetectionMessage;
    }

    /**
     * @param m_DetectionMessage the m_DetectionMessage to set
     */
    public void setDetectionMessage(String m_DetectionMessage) {
        this.m_DetectionMessage = m_DetectionMessage;
    }

    /**
     * @return the m_StatMessage
     */
    public String getStatMessage() {
        return m_StatMessage;
    }

    /**
     * @param m_StatMessage the m_StatMessage to set
     */
    public void setStatMessage(String m_StatMessage) {
        this.m_StatMessage = m_StatMessage;
    }

    /**
     *
     * @return Stat alert message
     */
    public String getStatAlertMessage()
    {
        return m_StatAlertMessage;
    }

    /**
     *
     * @param newMessage Stat alert message
     */
    public void setStatAlertMessage(String newMessage)
    {
        m_StatAlertMessage = newMessage;
    }
}
