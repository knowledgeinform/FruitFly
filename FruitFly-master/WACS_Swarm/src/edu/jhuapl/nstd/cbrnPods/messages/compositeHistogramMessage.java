package edu.jhuapl.nstd.cbrnPods.messages;

import java.util.Arrays;

/**
 *
 * @author humphjc1
 */
public class compositeHistogramMessage extends cbrnPodMsg
{    
    /**
     * Summed spectra of moving window most recently processed
     */
    private int m_SumSpectra [];
    
    /**
     * Classified spectra
     */
    private int m_ClassifiedSpectra [];
    
    /**
     * Number of spectra summed together to form m_SumSpectra
     */
    private int m_NumSpectra;
    
    /**
     * Total counts in spectra
     */
    private long m_TotalCounts;
    
    /**
     * Live time of moving window
     */
    private double m_LiveTime;
    
    /**
     * Indicates whether this spectrum is to be used as background
     */
    private boolean background;
    
    
    public compositeHistogramMessage(int sensorType, int messageType, int length)
    {
        super(sensorType, messageType, length);
        clear();
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) 
    {
        compositeHistogramMessage msg = (compositeHistogramMessage) m;
        
        this.timestampMs = msg.timestampMs;
        this.m_SumSpectra = msg.m_SumSpectra.clone();
        this.m_ClassifiedSpectra = msg.m_ClassifiedSpectra.clone();
        this.m_NumSpectra = msg.m_NumSpectra;
        this.m_TotalCounts = msg.m_TotalCounts;
        this.m_LiveTime = msg.m_LiveTime;
        this.background = msg.background;
        this.messageType = msg.messageType;
        this.validMessage = true;
    }
    
    private void clear ()
    {
        timestampMs = 0;
        if (m_SumSpectra == null)
            m_SumSpectra = new int [1];
        Arrays.fill (m_SumSpectra, 0);
        
        if (m_ClassifiedSpectra == null)
        	m_ClassifiedSpectra = new int [1];
        Arrays.fill (m_ClassifiedSpectra, 0);
        
        m_NumSpectra = 0;
        m_TotalCounts = 0;
        m_LiveTime = 0;
        background = false;
    }  
    
    public int[] copySummedSpectra ()
    {
        return m_SumSpectra.clone();
    }
    
    public int[] copyClassifiedSpectra ()
    {
        return m_ClassifiedSpectra.clone();
    }
    
    public void setSummedSpectra (int newSpectra[])
    {
        m_SumSpectra = newSpectra.clone();
    }
    
    public void setSummedSpectra (float newSpectra[])
    {
        m_SumSpectra = new int [newSpectra.length];
        for (int i = 0; i < newSpectra.length; i ++)
            m_SumSpectra[i] = (int)newSpectra[i];
    }
    
    public void setClassifiedSpectra (int newSpectra[])
    {
        m_ClassifiedSpectra = newSpectra.clone();
    }
    
    public void setClassifiedSpectra (float newSpectra[])
    {
    	m_ClassifiedSpectra = new int [newSpectra.length];
        for (int i = 0; i < newSpectra.length; i ++)
        	m_ClassifiedSpectra[i] = (int)newSpectra[i];
    }
    
    public int getNumSpectra ()
    {
        return m_NumSpectra;
    }
    
    public void setNumSpectra (int newCount)
    {
        m_NumSpectra = newCount;
    }
    
    public long getTotalCounts ()
    {
        return m_TotalCounts;
    }
    
    public void setTotalCounts (long newCount)
    {
        m_TotalCounts = newCount;
    }
    
    public double getLiveTime ()
    {
        return m_LiveTime;
    }
    
    public void setLiveTime (double newTime)
    {
        m_LiveTime = newTime;
    }
    
    public void setIsBackground(boolean isBackground)
    {
        background = isBackground;
    }
    
    public boolean isBackground()
    {
        return background;
    }
}
