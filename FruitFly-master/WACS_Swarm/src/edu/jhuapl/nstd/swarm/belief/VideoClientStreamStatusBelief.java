/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;
/**
 *
 * @author xud1
 */
public class VideoClientStreamStatusBelief extends Belief
{
    public static final String BELIEF_NAME = "VideoClientStreamStatusBelief";
    protected boolean mStreamStatus;
    
    public VideoClientStreamStatusBelief(String agentID, boolean status)
    {
        this(agentID, status, new Date(System.currentTimeMillis()));
    }
    
    public VideoClientStreamStatusBelief(String agentID, boolean status, Date time)
    {
        super(agentID);
        timestamp = time;
        mStreamStatus = status;
    }
        
    @Override
    protected void addBelief(Belief b)
    {
        VideoClientStreamStatusBelief belief = (VideoClientStreamStatusBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp)>0)
        {
            this.timestamp = belief.getTimeStamp();
            this.mStreamStatus = belief.getStreamState();
        }
    }
           
    public void setStreamStatus(boolean state)
    {
        mStreamStatus = state;
    }
    
    public boolean getStreamState()
    {
        return mStreamStatus;
    }    
    
   /**
   * Returns the unique name for this belief type.
   * @return A unique name for this belief type.
   */    
    public String getName()
    {
        return BELIEF_NAME;
    }    
}
