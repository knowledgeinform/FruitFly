/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.messages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;

/**
 * Holds counts for the Bladewerx sensor
 * 
 * @author fishmsm1
 */
public class countMessage extends cbrnPodMsg
{
    private int m_AlphaCounts;
    private int m_ChanZeroCounts;
    private int m_ChanOneCounts;
    private long m_Start;
    /**
     * Duration in milliseconds
     */
    private long m_Duration;

    public countMessage()
    {
        super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.COUNT_ITEM, 0);
        
        m_AlphaCounts = 0;
        m_ChanZeroCounts = 0;
        m_ChanOneCounts = 0;
        m_Start = 0;
        m_Duration = 0;
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) 
    {
        countMessage msg = (countMessage) m;
        
        this.timestampMs = msg.timestampMs;
        setAlphaCounts(msg.getAlphaCounts());
        setChanZeroCounts(msg.getChanZeroCounts());
        setChanOneCounts(msg.getChanOneCounts());
        this.messageType = msg.messageType;
        setDuration(msg.getDuration());
        setStartTime(msg.getStartTime());
        this.validMessage = true;
    }
    
    public void setAlphaCounts(int counts)
    {
        m_AlphaCounts = counts;
    }
    
    public int getAlphaCounts()
    {
        return m_AlphaCounts;
    }
    
    public void setChanZeroCounts(int counts)
    {
        m_ChanZeroCounts = counts;
    }
    
    public int getChanZeroCounts()
    {
        return m_ChanZeroCounts;
    }
    
    public void setChanOneCounts(int counts)
    {
        m_ChanOneCounts = counts;
    }
    
    public int getChanOneCounts()
    {
        return m_ChanOneCounts;
    }
    
    public void setStartTime(long time)
    {
        m_Start = time;
    }
    
    public long getStartTime()
    {
        return m_Start;
    }
    
    public void setDuration(long time)
    {
        m_Duration = time;
    }
    
    public long getDuration()
    {
        return m_Duration;
    }
}
