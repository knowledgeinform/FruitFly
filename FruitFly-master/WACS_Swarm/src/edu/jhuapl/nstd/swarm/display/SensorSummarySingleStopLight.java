/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.belief.SensorSummary;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

/**
 *
 * @author humphjc1
 */
public class SensorSummarySingleStopLight extends javax.swing.JPanel implements Updateable
{
    private int m_SmallPanelBuffer = 5;
    private int m_TopPanelBuffer = 20;
    private int m_BackgroundReducedHeight = 20;
    private int m_TopPanelBaseHeight = 20;
    private int m_CurrDetectionBarThickness = 5;
    private int m_MaxDetectionBarThickness = 3;
    private int m_BarFontSize = 12;
    private int m_BarFontPixels = 12;
    private long m_ColorFlashTimeMs = 1000;
    
    private Color m_BaseRedColor = new Color (180, 27, 27);
    private Color m_BrightRedColor = new Color (255, 0, 0);
    private Color m_BaseYellowColor = new Color (240, 184, 0);
    private Color m_BrightYellowColor = new Color (255, 240, 0);
    private Color m_BaseGreenColor = new Color (0, 200, 0);
    
    
    private int m_BarWidth;
    private int m_BarHeight;
    private int m_CurrentDetectionLocY = 0;
    private int m_CurrentDetectionStringLocY = 0;
    private int m_MaxDetectionLocY = 0;
    private int m_MaxDetectionStringLocY = 0;
    private int m_TimeSinceDetectionTextLocY;
    private long m_LastYellowTimestampMs = 0;
    private long m_LastRedTimestampMs = 0;
    private Color m_CurrRedColor = m_BaseRedColor;
    private Color m_CurrYellowColor = m_BaseYellowColor;
    private boolean m_InBackgroundMode;
    
    private int m_GreenMinPos;
    private int m_YellowMinPos;
    private int m_RedMinPos;
    private int m_RedMaxPos;
    private boolean m_NeedRepaint;
    
    private SensorSummary m_SensorSummary = null;
    private final Object m_SensorSummaryLock = new Object();
    private String m_SensorText = "UNK";
    private String m_CurrentDetectionText = "";
    private String m_MaxDetectionText = "";
    private String m_TimeSinceDetectionText = "";
    
    private final Color m_OverlayBackgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.6f);
    private static LinkedList<SensorSummarySingleStopLight> m_UpdateList;
    private static Thread m_UpdateThread;
            
    
    /**
     * Creates new form SensorSummaryStopLightPanel
     */
    public SensorSummarySingleStopLight(final String sensorName) 
    {
        this.setBorder (new LineBorder(Color.BLACK, 1));
        
        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) 
            {
                updateBarSizes(m_InBackgroundMode);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }
            @Override
            public void componentShown(ComponentEvent e) {
            }
            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        
        m_SensorText = sensorName;
        m_UpdateList.add(this);
        repaint();
    }
    
    static 
    {
        m_UpdateList = new LinkedList<SensorSummarySingleStopLight>();
        
        //make a static update thread
        m_UpdateThread = new Thread ()
        {
            public void run ()
            {
                while (true)
                {
                    for (SensorSummarySingleStopLight s : m_UpdateList)
                    {
                        long redFlashTimeMs = System.currentTimeMillis() - s.m_LastRedTimestampMs;
                        long yellowFlashTimeMs = System.currentTimeMillis() - s.m_LastYellowTimestampMs;
                        if ((s.m_LastRedTimestampMs > 0 && redFlashTimeMs < s.m_ColorFlashTimeMs) || (s.m_LastYellowTimestampMs > 0 && yellowFlashTimeMs < s.m_ColorFlashTimeMs))
                        {
                            s.update();
                        }
                    }
                    
                    try {
                        Thread.sleep (75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SensorSummarySingleStopLight.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
        };
        m_UpdateThread.setName("SensorSummarySingleStopLight-UpdateThread");
        m_UpdateThread.start();
    }
    
    private void updateBarSizes (boolean reduced)
    {
        m_BarWidth = getWidth() - m_SmallPanelBuffer*2;
        m_TopPanelBuffer = m_TopPanelBaseHeight + (reduced?m_BackgroundReducedHeight:0);
        m_BarHeight = (getHeight() - m_SmallPanelBuffer - m_TopPanelBuffer)/3;
        m_GreenMinPos = m_TopPanelBuffer+m_BarHeight*3;
        m_YellowMinPos = m_TopPanelBuffer+m_BarHeight*2;
        m_RedMinPos = m_TopPanelBuffer+m_BarHeight;
        m_RedMaxPos = m_TopPanelBuffer;
        m_NeedRepaint = true;
    }
    
    @Override
    public void update()
    {
        SensorSummary summary = null;
        synchronized (m_SensorSummaryLock)
        {
            summary = m_SensorSummary;
        }
        if (summary == null)
        {
            if (m_NeedRepaint)
                repaint();
            return;
        }
            
        
        int currPos = interpData (summary, summary.m_CurrDetectionValue);
        String detectionString = summary.m_CurrDetectionString;
        m_CurrentDetectionText = "" + NumberFormat.getNumberInstance(Locale.US).format((int)summary.m_CurrDetectionValue) + ((detectionString!=null&&detectionString.length()>0)?(": "+detectionString):"");
        m_CurrentDetectionLocY = currPos;
        m_CurrentDetectionStringLocY = m_CurrentDetectionLocY + m_CurrDetectionBarThickness/2 + m_BarFontPixels;
        if (m_CurrentDetectionStringLocY > m_GreenMinPos)
            m_CurrentDetectionStringLocY = m_CurrentDetectionLocY - m_CurrDetectionBarThickness/2;
        
        int maxPos = interpData (summary, summary.m_MaxDetectionValue);
        String maxDetectionString = summary.m_MaxDetectionString;
        m_MaxDetectionText = "" + NumberFormat.getNumberInstance(Locale.US).format((int)summary.m_MaxDetectionValue) + ((maxDetectionString!=null&&maxDetectionString.length()>0)?(": "+maxDetectionString):"");
        m_MaxDetectionLocY = maxPos;
        m_MaxDetectionStringLocY = m_MaxDetectionLocY - m_MaxDetectionBarThickness/2;
        if (m_MaxDetectionStringLocY < m_RedMaxPos)
            m_MaxDetectionStringLocY = m_MaxDetectionLocY + m_MaxDetectionBarThickness/2 + m_BarFontPixels;
        
        if (Math.abs(m_MaxDetectionStringLocY - m_CurrentDetectionStringLocY) < m_BarFontPixels)
            m_MaxDetectionStringLocY = -1;

        long lastDetMs = summary.m_LastAboveGreenDetectionTimeMs;
        //If a positive detection occurred in the past (but not presently), then print time since last detection
        if (lastDetMs > 0 && summary.m_CurrDetectionValue < summary.m_YellowLightMinValue)
        {
            int secSinceLastDet = (int)((System.currentTimeMillis() - lastDetMs)/1000);
            String timeSinceDetectionText = "";
            if (secSinceLastDet < 120)
                timeSinceDetectionText = secSinceLastDet + " s";
            else if (secSinceLastDet < 3600)
                timeSinceDetectionText = secSinceLastDet/60 + " min";
            else
                timeSinceDetectionText = secSinceLastDet/3600 + " hr";
            m_TimeSinceDetectionTextLocY = m_YellowMinPos;
            m_TimeSinceDetectionText = timeSinceDetectionText;
        }
        else
        {
            m_TimeSinceDetectionText = "";
            m_TimeSinceDetectionTextLocY = -1;
        }
        
        //Remove overlap text
        if (Math.abs(m_CurrentDetectionStringLocY - m_TimeSinceDetectionTextLocY) < m_BarFontPixels)
            m_TimeSinceDetectionTextLocY = -1;
        else if (Math.abs(m_MaxDetectionStringLocY - m_TimeSinceDetectionTextLocY) < m_BarFontPixels)
            m_TimeSinceDetectionTextLocY = -1;
        
        long redFlashTimeMs = System.currentTimeMillis() - m_LastRedTimestampMs;
        if (m_LastRedTimestampMs > 0)
        {
            Color baseColor = m_BaseRedColor;
            Color brightColor = m_BrightRedColor;
            
            float percentBright = ((float)m_ColorFlashTimeMs - redFlashTimeMs)/m_ColorFlashTimeMs;
            percentBright = Math.min (1, Math.max (0, percentBright));
            Color currColor = getCurrFlashColor (baseColor, brightColor, percentBright);
            m_CurrRedColor = currColor;
        }
        long yellowFlashTimeMs = System.currentTimeMillis() - m_LastYellowTimestampMs;
        if (m_LastYellowTimestampMs > 0)
        {
            Color baseColor = m_BaseYellowColor;
            Color brightColor = m_BrightYellowColor;
            
            float percentBright = ((float)m_ColorFlashTimeMs - yellowFlashTimeMs)/m_ColorFlashTimeMs;
            percentBright = Math.min (1, Math.max (0, percentBright));
            Color currColor = getCurrFlashColor (baseColor, brightColor, percentBright);
            m_CurrYellowColor = currColor;
        }
        
        
        this.repaint ();
    }
    
    private Color getCurrFlashColor (Color baseColor, Color brightColor, float percentBright)
    {
        return new Color (Math.min(255, Math.max (0, (int)(baseColor.getRed()*(1-percentBright) + brightColor.getRed()*(percentBright)))),
                    Math.min(255, Math.max (0, (int)(baseColor.getGreen()*(1-percentBright) + brightColor.getGreen()*(percentBright)))),
                    Math.min(255, Math.max (0, (int)(baseColor.getBlue()*(1-percentBright) + brightColor.getBlue()*(percentBright)))));
    }
    
    public void update (SensorSummary summary)
    {
        if (summary == null)
        {
            if (m_NeedRepaint)
                repaint();
            return;
        }
        
        float yellowMinVal = summary.m_YellowLightMinValue;
        float redMinVal = summary.m_RedLightMinValue;
        float value = summary.m_CurrDetectionValue;
                
        synchronized (m_SensorSummaryLock)
        {
            if (m_SensorSummary != null && summary.m_CurrDetectionTimeMs > m_SensorSummary.m_CurrDetectionTimeMs)
            {
                //This is new data determine if any colors need to flash
                if (value >= yellowMinVal && value < redMinVal)
                {
                    //Current position is in yellow bar
                    m_LastYellowTimestampMs = System.currentTimeMillis();
                }
                else if (value >= redMinVal)
                {
                    //Current position is in red bar
                    m_LastRedTimestampMs = System.currentTimeMillis();
                }
            }
            
            if (summary.m_InBackgroundCollection != m_InBackgroundMode)
            {
                m_InBackgroundMode = summary.m_InBackgroundCollection;
                updateBarSizes(m_InBackgroundMode);
            }
            
            m_SensorSummary = new SensorSummary(summary);
        }
        update();
    }
    
    private int interpData (SensorSummary summary, float value)
    {
        float greenMinVal = summary.m_GreenLightMinValue;
        float yellowMinVal = summary.m_YellowLightMinValue;
        float redMinVal = summary.m_RedLightMinValue;
        float redMaxVal = summary.m_RedLightMaxValue;
        
        int greenMinPos = m_GreenMinPos;
        int yellowMinPos = m_YellowMinPos;
        int redMinPos = m_RedMinPos;
        int redMaxPos = m_RedMaxPos;
        
        //interpolate current position on screen
        int currPos = greenMinPos;
        if (value >= greenMinVal && value <= yellowMinVal)
        {
            //Current position is in green bar
            currPos = (int)(((value-greenMinVal)/(yellowMinVal-greenMinVal)*(yellowMinPos-greenMinPos)) + greenMinPos);
        }
        else if (value >= yellowMinVal && value <= redMinVal)
        {
            //Current position is in yellow bar
            currPos = (int)(((value-yellowMinVal)/(redMinVal-yellowMinVal)*(yellowMinPos-greenMinPos)) + yellowMinPos);
        }
        else if (value >= redMinVal)
        {
            //Current position is in red bar
            currPos = (int)(((value-redMinVal)/(redMaxVal-redMinVal)*(yellowMinPos-greenMinPos)) + redMinPos);
            if (currPos < redMaxPos)
                currPos = redMaxPos;
            
        }
        return currPos;
    }
    
    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        m_NeedRepaint = false;

        //RYG blocks
        g.setColor (m_CurrRedColor);
        g.fillRect(m_SmallPanelBuffer, m_RedMaxPos, m_BarWidth, m_BarHeight);
        g.setColor (m_CurrYellowColor);
        g.fillRect(m_SmallPanelBuffer, m_RedMinPos, m_BarWidth, m_BarHeight);
        g.setColor (m_BaseGreenColor);
        g.fillRect(m_SmallPanelBuffer, m_YellowMinPos, m_BarWidth, m_BarHeight);
        
        g.setColor (Color.BLACK);
        Font oldFont = g.getFont();
        g.setFont(new Font ("Tahoma", 1, 16));
        
        //Sensor Title
        g.drawString(m_SensorText, m_SmallPanelBuffer, m_TopPanelBaseHeight-1);
        
        //Background alert
        if (m_InBackgroundMode)
        {
            g.setColor (Color.RED);
            //g.drawString("STARTING", m_SmallPanelBuffer, m_TopPanelBaseHeight+m_BackgroundReducedHeight-1);
            g.drawString("INIT", m_SmallPanelBuffer, m_TopPanelBaseHeight+m_BackgroundReducedHeight-1);
        }
        
        //Current information bar
        g.setColor (Color.BLACK);
        g.fillRect(0, m_CurrentDetectionLocY - m_CurrDetectionBarThickness/2, m_BarWidth + m_SmallPanelBuffer*2, m_CurrDetectionBarThickness);
        
        g.setFont(new Font ("Tahoma", 1, m_BarFontSize));
        //Maximum information
        g.setColor (Color.DARK_GRAY);
        g.fillRect(0, m_MaxDetectionLocY - m_MaxDetectionBarThickness/2, m_BarWidth + m_SmallPanelBuffer*2, m_MaxDetectionBarThickness);
        if (m_MaxDetectionStringLocY > 0)
        {
            g.setColor(m_OverlayBackgroundColor);
            g.fillRoundRect(m_SmallPanelBuffer*2, m_MaxDetectionStringLocY-m_BarFontPixels, m_BarWidth-m_SmallPanelBuffer*2, m_BarFontPixels+1, m_MaxDetectionBarThickness, m_MaxDetectionBarThickness);
            g.setColor (Color.WHITE);
            g.drawString(m_MaxDetectionText, m_SmallPanelBuffer*2, m_MaxDetectionStringLocY);
        }
        
        //Current information text
        g.setColor (Color.BLACK);
        if (m_CurrentDetectionStringLocY > 0)
        {
            g.setColor(m_OverlayBackgroundColor);
            g.fillRoundRect(m_SmallPanelBuffer*2, m_CurrentDetectionStringLocY-m_BarFontPixels+1, m_BarWidth-m_SmallPanelBuffer*2, m_BarFontPixels+1, m_CurrDetectionBarThickness, m_CurrDetectionBarThickness);
            g.setColor (Color.WHITE);
            g.drawString(m_CurrentDetectionText, m_SmallPanelBuffer*2, m_CurrentDetectionStringLocY);
        }
        
        //Time since last detection
        g.setColor (Color.DARK_GRAY);
        if (m_TimeSinceDetectionTextLocY > 0)
        {
            g.setColor(m_OverlayBackgroundColor);
            g.fillRoundRect(m_SmallPanelBuffer*2, m_TimeSinceDetectionTextLocY-m_BarFontPixels, m_BarWidth-m_SmallPanelBuffer*2, m_BarFontPixels+1, m_CurrDetectionBarThickness, m_CurrDetectionBarThickness);
            g.setColor (Color.WHITE);
            g.drawString(m_TimeSinceDetectionText, m_SmallPanelBuffer*2, m_TimeSinceDetectionTextLocY);
        }
        
        
        g.setFont (oldFont);
    }

}
