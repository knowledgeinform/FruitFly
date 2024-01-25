/*
 * BridgeportOutputDisplayGraph.java
 *
 * Created on December 11, 2009, 12:26 PM
 */

package edu.jhuapl.nstd.cbrnPods;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.Arrays;


/**
 * Graph panel for RN Histogram data
 *
 * @author  humphjc1
 */
public class RNHistogramDisplayGraphPanel extends javax.swing.JPanel 
{

    /**
     * Histogram data for live histogram
     */
    RNHistogram m_LastData_Histogram = null;
    
    /**
     * Histogram data for classified/estimated histogram 
     */
    int m_LastClassified [] = null;

    /**
     * Histogram data for live histogram, logarithmic values
     */
    float m_LastData_HistogramLog[] = null;
    
    /**
     * Histogram data for accumulated histogram
     */
    RNHistogram m_Accumulated_Histogram = null;

    /**
     * Histogram data for accumulated histogram, logarithmic values
     */
    float m_Accumulated_HistogramLog[] = null;
    
    /**
     * A copy of the most recent graphics object that was passed in to be painted
     */
    Graphics2D g2d = null;

    /**
     * Title of graph
     */
    private String m_Title = "Count Graph";

    /**
     * Minimum counts to plot, regular scale
     */
    int count_minReg = 0;
    
    /**
     * Maximum counts to plot, regular scale
     */
    int count_maxReg = 0;

    /**
     * Minimum counts to plot, logarithmic scale
     */
    int count_minLog = -1;

    /**
     * Maximum counts to plot, logarithmic scale
     */
    int count_maxLog = 0;

    /**
     * Number of bins in histogram
     */
    int numBins = 0;

    /**
     * Label for y axis
     */
    private String m_YAxisLabel = "Counts";

    /**
     * Label for x axis
     */
    private String m_XAxisLabel = "Channel";
    
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

    /**
     * Live time for displayed histogram, seconds
     */
    private int m_LiveTimeS = 0;
    
    /** buffer values for edge of plotter.  x-axis will start x_min pixels from
     * the left edge and stop x_max pixels from the right edge.  y-axis will start
     * y_min pixels from the top edge and y_max pixels from the bottom edge
     */
    int m_LeftBuffer = 60;
    int m_RightBuffer = 20;
    int m_TopBuffer = 15;
    int m_BottomBuffer = 80;

    /**
     * If true, show live histogram.  If false, show accumulated histogram
     */
    private boolean m_ShowLiveData = true;

    /**
     * If true, show logarithmic count data scale.  If false, show regular scale
     */
    private boolean m_DisplayLogScale = false;

    /**
     * Formatter for 6 decimal places
     */
    DecimalFormat m_DecFormat6 = new DecimalFormat ("#.######");


    /**
     * Create graph panel.  No data set.
     */
    public RNHistogramDisplayGraphPanel()
    {
        super (true);
        initComponents();
        count_maxReg = 0;
        count_maxLog = 0;

        m_LastData_Histogram = null;
        m_Accumulated_Histogram = null;
    }

    /**
     * Provides a histogram to use for the accumulating histogram.  Needs to be called only once to
     * provide the object
     *
     * @param accumulatingHistogram Histogram object to use for accumulating data
     */
    public void addAccumulatingHistogram (RNHistogram accumulatingHistogram)
    {
        m_Accumulated_Histogram = accumulatingHistogram;
    }

    /**
     * Accessor for accumulating histogram
     * @return
     */
    public RNHistogram getAccumulatingHistogram ()
    {
        return m_Accumulated_Histogram;
    }

    /**
     * Clear data in accumulating histogram
     */
    public void resetAccumulatingHistogram ()
    {
        m_Accumulated_Histogram.clear();
        Arrays.fill (m_Accumulated_HistogramLog, 0.0f);

        if (m_ShowLiveData)
            chooseLiveHistogram();
        else
            chooseAccumHistogram();
    }

    /**
     * Input new histogram data to the graph.
     *
     * @param lastData_Histogram New histogram data
     */
    public void updateCurrentHistogram(RNHistogram lastData_Histogram) 
    {
        //Set live histogram
        m_LastData_Histogram = lastData_Histogram;
        if (lastData_Histogram.getClassifiedData() != null)
        	m_LastClassified = lastData_Histogram.getClassifiedData().clone();

        //Add new data to accumulating histogram
        if (m_Accumulated_Histogram != null)
            m_Accumulated_Histogram.addFrom(m_LastData_Histogram);

        //Compute logarithmic data for live and accumulating histograms.
        if (m_LastData_HistogramLog == null || m_LastData_HistogramLog.length != m_LastData_Histogram.getNumBins())
            m_LastData_HistogramLog = new float [m_LastData_Histogram.getNumBins()];
        if (m_Accumulated_HistogramLog == null || m_Accumulated_HistogramLog.length != m_LastData_Histogram.getNumBins())
            m_Accumulated_HistogramLog = new float [m_LastData_Histogram.getNumBins()];
        Arrays.fill (m_LastData_HistogramLog, 0.0f);
        Arrays.fill (m_Accumulated_HistogramLog, 0.0f);
        for (int i = 0; i < m_LastData_Histogram.getNumBins(); i ++)
        {
            if (m_LastData_Histogram.getRawValue(i) == 0)
                m_LastData_HistogramLog[i] = count_minLog;
            else
                m_LastData_HistogramLog[i] = (float) Math.log10(m_LastData_Histogram.getRawValue (i));
            if (m_Accumulated_Histogram != null)
            {
                if (m_Accumulated_Histogram.getRawValue(i) == 0)
                    m_Accumulated_HistogramLog[i] = count_minLog;
                else
                    m_Accumulated_HistogramLog[i] = (float) Math.log10(m_Accumulated_Histogram.getRawValue (i));
            }
        }

        //Request a repaint
        if (m_ShowLiveData)
            chooseLiveHistogram();
        else
            chooseAccumHistogram();
    }
    
    /**
     * Return the screen coordinate relating to a certain bin (x-value)
     *
     * @param value Bin to find screen coordinate for
     * @return Screen coordinate for the bin
     */
    private int getScreenX(float value)
    {
        double percentFromMin = 0;
        if (m_LastData_Histogram == null)
            percentFromMin = .5;
        else
            //percentFromMin = (value) / ((double) (m_LastData_Histogram.getNumBins()));
            percentFromMin = (value) / ((double) (numBins));
        int screenX = (int)(m_LeftBuffer + (this.getWidth() - m_RightBuffer - m_LeftBuffer)*percentFromMin);
        
        return screenX;
    }
    
    /**
     * Return the screen coordinate relating to a certain y-value
     *
     * @param value y-value to find screen coordinate for
     * @return Screen coordinate for the value
     */
    private int getScreenY(float value)
    {
        double percentFromMin = 0;
        
        if (m_DisplayLogScale)
            percentFromMin = (value-count_minLog)/((double)(count_maxLog - count_minLog));
        else
            percentFromMin = (value-count_minReg)/((double)(count_maxReg - count_minReg));
        int screenY = (int)(this.getHeight() - m_BottomBuffer - (this.getHeight() - m_TopBuffer - m_BottomBuffer)*percentFromMin);
        
        return screenY;
    }

    /**
     * Paint the current live histogram in the panel
     */
    synchronized public void chooseLiveHistogram ()
    {
        m_ShowLiveData = true;
        count_maxReg = 0;
        count_maxLog = 0;

        //Find min max limits of data for plotting
        if (m_LastData_Histogram != null)
        {
            for (int i = 0; i < m_LastData_Histogram.getNumBins(); i ++)
            {
                if (m_LastData_Histogram.getRawValue(i)>count_maxReg)
                    count_maxReg = m_LastData_Histogram.getRawValue(i);

                if (m_LastData_HistogramLog[i] > count_maxLog)
                    count_maxLog = (int)Math.ceil(m_LastData_HistogramLog[i]);
            }
        }

        repaint();
    }

    /**
     * Paint the accumulated histogram in the panel
     */
    synchronized public void chooseAccumHistogram()
    {
        m_ShowLiveData = false;
        count_maxReg = 0;
        count_maxLog = 0;

        //Find min max limits of data for plotting
        if (m_Accumulated_Histogram != null)
        {
            for (int i = 0; i < m_Accumulated_Histogram.getNumBins(); i ++)
            {
                if (m_Accumulated_Histogram.getRawValue(i)>count_maxReg)
                    count_maxReg = m_Accumulated_Histogram.getRawValue(i);

                if (m_Accumulated_HistogramLog[i] > count_maxLog)
                    count_maxLog = (int)Math.ceil(m_Accumulated_HistogramLog[i]);
            }
        }

        repaint();
    }

    /**
     * Toggle whether data should be plotted using logarithmic y-scale
     * @param logScale True if use log scale, false if regular scale
     */
    synchronized public void selectLogScale (boolean logScale)
    {
        m_DisplayLogScale = logScale;
        repaint ();
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
        g2d.drawLine (m_LeftBuffer, this.getHeight()-m_BottomBuffer, this.getWidth()-m_RightBuffer, this.getHeight()-m_BottomBuffer);
        g2d.drawLine (m_LeftBuffer, this.getHeight()-m_BottomBuffer+1, this.getWidth()-m_RightBuffer, this.getHeight()-m_BottomBuffer+1);
        
        //Number of bins (x-values)
        if (m_LastData_Histogram == null)
            numBins = 256;
        else
            numBins = m_LastData_Histogram.getNumBins();

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
        if ((double)numBins/(double)x_ticks < (1.0 + 0.00001)) {
            x_ticks_act = 1;
        }
        else if ((double)numBins/(double)x_ticks < (2.0 + 0.00001)) {
            x_ticks_act = 2;
        }
        else if ((double)numBins/(double)x_ticks < (5.0 + 0.00001)) {
            x_ticks_act = 5;
        }
        else if ((double)numBins/(double)x_ticks < (10.0 + 0.00001)) {
            x_ticks_act = 10;
        }
        else if ((double)numBins/(double)x_ticks < (25.0 + 0.00001)) {
            x_ticks_act = 25;
        }
        else if ((double)numBins/(double)x_ticks < (50.0 + 0.00001)) {
            x_ticks_act = 50;
        }
        else if ((double)numBins/(double)x_ticks < (100.0 + 0.00001)) {
            x_ticks_act = 100;
        }
        else if ((double)numBins/(double)x_ticks < (200.0 + 0.00001)) {
            x_ticks_act = 200;
        }
        else if ((double)numBins/(double)x_ticks < (500.0 + 0.00001)) {
            x_ticks_act = 500;
        }
        else if ((double)numBins/(double)x_ticks < (1000.0 + 0.00001)) {
            x_ticks_act = 1000;
        }
        
        try {
            //Adjust number of bins (x-values) for actual displayed ticks based on the prior adjustment
            if (Math.abs(numBins)%x_ticks_act != 0)
                numBins += x_ticks_act-(Math.abs(numBins) %x_ticks_act);
            
            g2d.drawString ("0", m_LeftBuffer, this.getHeight()-m_BottomBuffer+20);
            
            for (int i = x_ticks_act; i <= (numBins); i += x_ticks_act) 
            {
                //Draw the tick
                g2d.drawLine ((int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(double)(numBins)), this.getHeight()-m_BottomBuffer-2,
                        (int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(numBins)), this.getHeight()-m_BottomBuffer+3);

                //Paint the value of the tick
                g2d.drawString (new Integer(0+(i)).toString(), (int)(m_LeftBuffer+i*(this.getWidth()-(m_LeftBuffer+m_RightBuffer))/(numBins)), this.getHeight()-m_BottomBuffer+20);
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }


        //Paint y-scale and data
        if (m_DisplayLogScale)
            paintLogScale (g);
        else
            paintRegular (g);


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
        g2d.drawString ("Histogram covers " + getLiveTime() + " s", 20, this.getHeight() - 42);
        g2d.drawString (getDetectionMessage(), 20, this.getHeight() - 10);
        g2d.drawString (getStatMessage(), 20, this.getHeight() - 26);

        //Paint stat alert message next to stat message
        g2d.setFont(boldArial);
        g2d.setColor (Color.red);
        g2d.drawString (getStatAlertMessage(), (int)(this.getWidth()*0.6), this.getHeight() - 26);
        g2d.setColor (Color.black);

        //Paint y-axis label rotated appropriately
        AffineTransform af = new AffineTransform();
        af.translate (20., (this.getHeight()-m_BottomBuffer-m_TopBuffer)/2 + m_TopBuffer);
        af.rotate (-Math.PI/2);
        FontRenderContext renderContext = new FontRenderContext (null, false, false);
        g2d.transform(af);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        TextLayout layout = new TextLayout (getYAxisLabel(), g2d.getFont(), renderContext);
        g2d.setFont(f);
        layout.draw(g2d, 0, 0);
        af.translate (-40.0, -300.);
        af.rotate (Math.PI);
        g2d.transform (af);
    }


    /**
     * Paint tick marks and data points for a regular y-scale
     * @param g
     */
    public void paintRegular (Graphics g)
    {
        if (count_maxReg < 1)
            count_maxReg = 1;

        int count_range = count_maxReg - count_minReg;
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
            //Adjust number of bins (x-values) for actual displayed ticks based on the prior adjustment
            if (Math.abs(count_range)%y_ticks_act != 0)
                count_maxReg += y_ticks_act-(Math.abs(count_range) %y_ticks_act);

            for (int i = 0; i <= (count_range); i += y_ticks_act)
            {
                //Draw the tick
                g2d.drawLine (m_LeftBuffer-2, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_maxReg-count_minReg))),
                        m_LeftBuffer+2,(int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_maxReg-count_minReg))));

                //Paint the value of the tick
                g2d.drawString (new Integer(count_maxReg-(i)).toString(), m_LeftBuffer-35, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_maxReg-count_minReg))));
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }



        RNHistogram hist = null;
        if (m_ShowLiveData)
            hist = m_LastData_Histogram;
        else
            hist = m_Accumulated_Histogram;

        if (hist == null)
            return;
        
        int lastX2,lastY2;
        g2d.setColor(Color.RED);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2));
        
        // Classified histogram
        if (m_LastClassified != null)
        {
        	lastX2 = getScreenX(0);
        	lastY2 = getScreenY(m_LastClassified[0]);
	        //Scroll through histogram and draw lines connecting data points
	        for (int i = 1; i < m_LastClassified.length; i ++)
	        {
	            int currX = getScreenX(i);
	            int currY = getScreenY(m_LastClassified[i]);
	
	            g2d.drawLine(lastX2, lastY2, currX, currY);
	
	            lastX2 = currX;
	            lastY2 = currY;
	
	        }
        }
        
        // Raw histogram
        g2d.setColor (Color.blue);
        g2d.setStroke(oldStroke);
        
        int lastX = getScreenX(0);
        int lastY = getScreenY(hist.getRawValue(0));

        //Scroll through histogram and draw lines connecting data points
        for (int i = 1; i < hist.getNumBins(); i ++)
        {
            int currX = getScreenX(i);
            int currY = getScreenY(hist.getRawValue(i));

            g2d.drawLine(lastX, lastY, currX, currY);

            lastX = currX;
            lastY = currY;

        }
    }

    public void paintLogScale (Graphics g)
    {
        if (count_maxLog < 1)
            count_maxLog = 1;

        int count_range = count_maxLog - count_minLog;
        int y_range = this.getHeight() - (m_TopBuffer+m_BottomBuffer);
        int y_ticks = (int)(y_range/25.0);

        //Display log scale every tick
        int y_ticks_act = 1;

        try {

            for (int i = 0; i <= (count_range); i += y_ticks_act)
            {
                //Draw tick
                g2d.drawLine (m_LeftBuffer-2, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_range))),
                        m_LeftBuffer+2,(int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_range))));

                //Paint value of tick
                int valueInt = (int)(Math.pow (10, count_maxLog-(i)));
                String valueString = "";
                if (valueInt > 0)
                    valueString = "" + valueInt;
                else
                    valueString = "" + m_DecFormat6.format(Math.pow (10, count_maxLog-(i)));
                g2d.drawString (valueString, m_LeftBuffer-35, (int)(m_TopBuffer+i*(this.getHeight()-(m_TopBuffer+m_BottomBuffer))/((double)(count_range))));
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }



        g2d.setColor (Color.blue);
        float hist[] = null;
        if (m_ShowLiveData)
            hist = m_LastData_HistogramLog;
        else
            hist = m_Accumulated_HistogramLog;

        if (hist == null)
            return;
        
        int lastX = getScreenX(0);
        int lastY = getScreenY(hist[0]);

        //Scroll through histogram and draw lines connecting data points
        for (int i = 1; i < hist.length; i ++)
        {
            int currX = getScreenX(i);
            int currY = getScreenY(hist[i]);

            g2d.drawLine(lastX, lastY, currX, currY);

            lastX = currX;
            lastY = currY;

        }
    }



/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(1200, 300));
        setPreferredSize(new java.awt.Dimension(500, 300));
        setRequestFocusEnabled(false);
    }// </editor-fold>//GEN-END:initComponents

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
    public String getYAxisLabel() {
        return m_YAxisLabel;
    }

    /**
     * @param m_YAxisLabel the m_YAxisLabel to set
     */
    public void setYAxisLabel(String m_YAxisLabel) {
        this.m_YAxisLabel = m_YAxisLabel;
    }

    /**
     * @return the m_ShowLiveData
     */
    public boolean isShowLiveData() {
        return m_ShowLiveData;
    }

    /**
     * @param m_ShowLiveData the m_ShowLiveData to set
     */
    public void ShowLiveData(boolean m_ShowLiveData)
    {
        if(m_ShowLiveData)
            chooseLiveHistogram();
        else
            chooseAccumHistogram();
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
     * @return the Stat alert message
     */
    public String getStatAlertMessage()
    {
        return m_StatAlertMessage;
    }

    /**
     *
     * @param newMessage New stat alert message
     */
    public void setStatAlertMessage(String newMessage)
    {
        m_StatAlertMessage = newMessage;
    }

    /**
     * @return the m_LiveTime
     */
    public int getLiveTime() {
        return m_LiveTimeS;
    }

    /**
     * @param m_LiveTime the m_LiveTime to set
     */
    public void setLiveTime(int m_LiveTime) {
        this.m_LiveTimeS = m_LiveTime;
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
