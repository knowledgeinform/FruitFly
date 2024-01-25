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
public class VideoClientFrameRequestBelief extends Belief
{
    public static final String BELIEF_NAME = "VideoClientFrameRequestBelief";
    
    
    public VideoClientFrameRequestBelief(String agentID)
    {
        this(agentID, new Date(System.currentTimeMillis()));
    }
    
    public VideoClientFrameRequestBelief(String agentID,Date time)
    {
        super(agentID);
        timestamp = time;
    }
            
    @Override
    protected void addBelief(Belief b)
    {
        VideoClientFrameRequestBelief belief = (VideoClientFrameRequestBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp)>0)
        {
            this.timestamp = belief.getTimeStamp();
        }
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
