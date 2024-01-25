/*
 * BridgeportOutputDisplayGraph.java
 *
 * Created on December 11, 2009, 12:26 PM
 */

package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.swarm.util.Config;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.StringTokenizer;


/**
 *
 * @author  humphjc1
 */
public class BridgeportOutputDisplayGraph extends javax.swing.JPanel {

    RNHistogram m_LastData_Histogram = null;
    float m_LastData_HistogramLog[] = null;
    
    RNHistogram m_Accumulated_Histogram = null;
    float m_Accumulated_HistogramLog[] = null;
    
    /**
     * A copy of the most recent graphics object that was passed in to be painted
     */
    Graphics2D g2d = null;
    
    String m_Title = "Count Graph";
    
    int count_minReg = 0;
    int count_maxReg = 0;
    int count_minLog = -1;
    int count_maxLog = 0;
    
    int numBins = 0;
    
    String vertical_name = "Channel";
    
    // buffer values for edge of plotter.  x-axis will start x_min pixels from
    // the left edge and stop x_max pixels from the right edge.  y-axis will start
    // y_min pixels from the top edge and y_max pixels from the bottom edge
    int x_min = 80;
    int x_max = 30;
    int y_min = 40;
    int y_max = 60;
    
    boolean m_ShowLiveData = true;

    boolean m_DisplayLogScale = false;

    /**
     * Channels to permanently reset to zero when new data comes in.  Used to eliminate erroneous spikes
     */
    private int channelsToZero[] = null;

    private boolean zeroReqChannels = false;

    /**
     * Formatter for 6 decimal places
     */
    DecimalFormat m_DecFormat6 = new DecimalFormat ("#.######");


    
    
    
    /** Creates new form BridgeportOutputDisplayGraph */
    public BridgeportOutputDisplayGraph() {
        super (true);
        initComponents();
        
        m_LastData_Histogram = null;
        m_Accumulated_Histogram = null;

        
        String channelsToZeroString = Config.getConfig().getProperty("Bridgeport.ChannelsToZero", "");
        StringTokenizer tokens = new StringTokenizer(channelsToZeroString, " ,\r\n");
        int count = tokens.countTokens();
        if (count > 0)
        {
            channelsToZero = new int [count];
            for (int i = 0; i < channelsToZero.length; i ++)
                channelsToZero[i] = Integer.parseInt(tokens.nextToken());
        }
    }
    
    public void addAccumulatingHistogram (RNHistogram accumulatingHistogram)
    {
        m_Accumulated_Histogram = accumulatingHistogram;
    }
    
    public RNHistogram getAccumulatingHistogram ()
    {
        return m_Accumulated_Histogram;
    }

    synchronized public void resetAccumulatingHistogram ()
    {
        m_Accumulated_Histogram.clear();
        Arrays.fill (m_Accumulated_HistogramLog, 1.0f);
        repaint();
    }
    
    public void updateCurrentHistogram(RNHistogram lastData_Histogram) {
        
        m_LastData_Histogram = lastData_Histogram;
        if (m_Accumulated_Histogram != null)
            m_Accumulated_Histogram.addFrom(m_LastData_Histogram);


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

        if (m_ShowLiveData)
            chooseLiveHistogram();
        else
            chooseAccumHistogram();
    }
    
    private int getScreenX(float value)
    {
        double percentFromMin = 0;
        if (m_LastData_Histogram == null)
            percentFromMin = .5;
        else
            //percentFromMin = (value) / ((double) (m_LastData_Histogram.getNumBins()));
            percentFromMin = (value) / ((double) (numBins));
        int screenX = (int)(x_min + (this.getWidth() - x_max - x_min)*percentFromMin);

        return screenX;
    }
    
    private int getScreenY(float value)
    {
        double percentFromMin = 0;
        if (m_DisplayLogScale)
            percentFromMin = (value-count_minLog)/((double)(count_maxLog - count_minLog));
        else
            percentFromMin = (value-count_minReg)/((double)(count_maxReg - count_minReg));

        int screenY = (int)(this.getHeight() - y_max - (this.getHeight() - y_min - y_max)*percentFromMin);
        return screenY;
    }
    
    synchronized public void chooseLiveHistogram ()
    {
        m_ShowLiveData = true;
        count_maxReg = 0;
        count_maxLog = 0;

        if (m_LastData_Histogram != null)
        {
            for (int i = 0; i < m_LastData_Histogram.getNumBins(); i ++)
            {
                boolean skip = false;
                if (zeroReqChannels && channelsToZero != null)
                {
                    for (int j = 0; j < channelsToZero.length; j ++)
                    {
                        if (i == channelsToZero[j])
                        {
                            skip = true;
                            break;
                        }
                    }
                }

                if (!skip)
                {
                    if (m_LastData_Histogram.getRawValue(i)>count_maxReg)
                        count_maxReg = m_LastData_Histogram.getRawValue(i);

                    if (m_LastData_HistogramLog[i] > count_maxLog)
                        count_maxLog = (int)Math.ceil(m_LastData_HistogramLog[i]);
                }
            }
        }
        
        repaint();
    }
    
    synchronized public void chooseAccumHistogram()
    {
        m_ShowLiveData = false;
        count_maxReg = 0;
        count_maxLog = 0;
        
        if (m_Accumulated_Histogram != null)
        {
            for (int i = 0; i < m_Accumulated_Histogram.getNumBins(); i ++)
            {
                boolean skip = false;
                if (zeroReqChannels && channelsToZero != null)
                {
                    for (int j = 0; j < channelsToZero.length; j ++)
                    {
                        if (i == channelsToZero[j])
                        {
                            skip = true;
                            break;
                        }
                    }
                }

                if (!skip)
                {
                    if (m_Accumulated_Histogram.getRawValue(i)>count_maxReg)
                        count_maxReg = m_Accumulated_Histogram.getRawValue(i);

                    if (m_Accumulated_HistogramLog[i] > count_maxLog)
                        count_maxLog = (int)Math.ceil(m_Accumulated_HistogramLog[i]);
                }
            }
        }
        
        repaint();
    }

    synchronized public void selectLogScale (boolean logScale)
    {
        m_DisplayLogScale = logScale;
        repaint ();
    }

    public void setZeroReqChannels (boolean zeroChannels)
    {
        zeroReqChannels = zeroChannels;
        if (m_ShowLiveData)
            chooseLiveHistogram();
        else
            chooseAccumHistogram();
    }
    
    @Override
    public void paintComponent (Graphics g) {

        super.paintComponent(g);

        if (m_LastData_Histogram == null)
            return;


        g2d = (Graphics2D) g;

        //Draw axes
        g2d.drawLine (x_min, y_min, x_min, this.getHeight()-y_max);
        g2d.drawLine (x_min+1, y_min, x_min+1, this.getHeight()-y_max);
        g2d.drawLine (x_min, this.getHeight()-y_max, this.getWidth()-x_max, this.getHeight()-y_max);
        g2d.drawLine (x_min, this.getHeight()-y_max+1, this.getWidth()-x_max, this.getHeight()-y_max+1);

        //Tickmarks and labels
        numBins = m_LastData_Histogram.getNumBins();
        int x_range = this.getWidth() - (x_min+x_max);
        int x_ticks = (int)(x_range / 35.0);

        // this parameter is the interval for ticks in the x-direction and is updated
        // based on the range.  It prevents labels from overlapping
        int x_ticks_act = 0;
        if ((double)numBins/(double)x_ticks < 1.0) {
            x_ticks_act = 1;
        }
        else if ((double)numBins/(double)x_ticks < 2.0) {
            x_ticks_act = 2;
        }
        else if ((double)numBins/(double)x_ticks < 5.0) {
            x_ticks_act = 5;
        }
        else if ((double)numBins/(double)x_ticks < 10.0) {
            x_ticks_act = 10;
        }
        else if ((double)numBins/(double)x_ticks < 25.0) {
            x_ticks_act = 25;
        }
        else if ((double)numBins/(double)x_ticks < 50.0) {
            x_ticks_act = 50;
        }
        else if ((double)numBins/(double)x_ticks < 100.0) {
            x_ticks_act = 100;
        }

        try {
            if (Math.abs(numBins)%x_ticks_act != 0)
                numBins += x_ticks_act-(Math.abs(numBins) %x_ticks_act);

            g2d.drawString ("0", x_min, this.getHeight()-y_max+20);

            for (int i = x_ticks_act; i <= (numBins); i += x_ticks_act)
            {
                g2d.drawLine ((int)(x_min+i*(this.getWidth()-(x_min+x_max))/(double)(numBins)), this.getHeight()-y_max-2,
                        (int)(x_min+i*(this.getWidth()-(x_min+x_max))/(numBins)), this.getHeight()-y_max+3);

                g2d.drawString (new Integer(0+(i)).toString(), (int)(x_min+i*(this.getWidth()-(x_min+x_max))/(numBins)), this.getHeight()-y_max+20);
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }


        if (m_DisplayLogScale)
            paintLogScale (g);
        else
            paintRegular (g);

        
        // Print axes labels - must be last
        g2d.setColor (Color.black);
        g2d.drawString (vertical_name, 245, this.getHeight() - 20);
        AffineTransform af = new AffineTransform();
        af.translate (20., 175.);
        af.rotate (-Math.PI/2);
        FontRenderContext renderContext = new FontRenderContext (null, false, false);
        g2d.transform(af);
        TextLayout layout = new TextLayout ("Counts",
                g2d.getFont(), renderContext);
        layout.draw(g2d, 0, 0);
        af.translate (-40.0, -500.);
        af.rotate (Math.PI);
        g2d.transform (af);
    }

    public void paintRegular (Graphics g)
    {
        if (count_maxReg < 1)
            count_maxReg = 1;

        int count_range = count_maxReg - count_minReg;
        int y_range = this.getHeight() - (y_min+y_max);
        int y_ticks = (int)(y_range/25.0);

        // this parameter is the interval for ticks in the y-direction and is updated
        // based on the range.  It prevents labels from overlapping
        int y_ticks_act = 0;
        if (count_range/(double)y_ticks < 1.0) {
            y_ticks_act = 1;
        }
        else if (count_range/(double)y_ticks < 2.0) {
            y_ticks_act = 2;
        }
        else if (count_range/(double)y_ticks < 5.0) {
            y_ticks_act = 5;
        }
        else if (count_range/(double)y_ticks < 10.0) {
            y_ticks_act = 10;
        }
        else if (count_range/(double)y_ticks < 25.0) {
            y_ticks_act = 25;
        }
        else if (count_range/(double)y_ticks < 50.0) {
            y_ticks_act = 50;
        }
        else if (count_range/(double)y_ticks < 100.0) {
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
            if (Math.abs(count_range)%y_ticks_act != 0)
                count_maxReg += y_ticks_act-(Math.abs(count_range) %y_ticks_act);

            for (int i = 0; i <= (count_range); i += y_ticks_act)
            {
                g2d.drawLine (x_min-2, (int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_maxReg-count_minReg))),
                        x_min+2,(int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_maxReg-count_minReg))));

                g2d.drawString (new Integer(count_maxReg-(i)).toString(), x_min-35, (int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_maxReg-count_minReg))));
            }
        }
        catch (ArithmeticException e) {
            e.printStackTrace();
            return;
        }



        g2d.setColor (Color.blue);
        RNHistogram hist = null;
        if (m_ShowLiveData)
            hist = m_LastData_Histogram;
        else
            hist = m_Accumulated_Histogram;

        int lastX = getScreenX(0);
        int lastY = getScreenY(hist.getRawValue(0));
        if (zeroReqChannels && channelsToZero != null)
        {
            for (int j = 0; j < channelsToZero.length; j ++)
            {
                if (channelsToZero[j] == 0)
                {
                    lastY = getScreenY(0);
                    break;
                }
            }

        }

        for (int i = 1; i < hist.getNumBins(); i ++)
        {
            int currX = getScreenX(i);
            float value = hist.getRawValue(i);
            if (zeroReqChannels && channelsToZero != null)
            {
                for (int j = 0; j < channelsToZero.length; j ++)
                {
                    if (i == channelsToZero[j])
                    {
                        value = 0;
                        break;
                    }
                }

            }
            int currY = getScreenY(value);

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
        int y_range = this.getHeight() - (y_min+y_max);
        int y_ticks = (int)(y_range/25.0);

        //Display log scale every tick
        int y_ticks_act = 1;
        
        try {
            
            for (int i = 0; i <= (count_range); i += y_ticks_act)
            {
                g2d.drawLine (x_min-2, (int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_range))),
                        x_min+2,(int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_range))));


                int valueInt = (int)(Math.pow (10, count_maxLog-(i)));
                String valueString = "";
                if (valueInt > 0)
                    valueString = "" + valueInt;
                else
                    valueString = "" + m_DecFormat6.format(Math.pow (10, count_maxLog-(i)));
                g2d.drawString (valueString, x_min-35, (int)(y_min+i*(this.getHeight()-(y_min+y_max))/((double)(count_range))));
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

        int lastX = getScreenX(0);
        int lastY = getScreenY(hist[0]);

        for (int i = 1; i < hist.length; i ++)
        {
            int currX = getScreenX(i);
            float value = hist[i];
            if (zeroReqChannels && channelsToZero != null)
            {
                for (int j = 0; j < channelsToZero.length; j ++)
                {
                    if (i == channelsToZero[j])
                    {
                        value = 0;
                        break;
                    }
                }

            }
            int currY = getScreenY(value);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
