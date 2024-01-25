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
public class VideoClientRecorderCmdBelief extends Belief
{
    public static final String BELIEF_NAME = "VideoClientRecorderCmdBelief";
    protected boolean mRecorderCmd;
    
    public VideoClientRecorderCmdBelief(String agentID, Boolean cmd)
    {
        this(agentID, cmd, new Date(System.currentTimeMillis()));
    }
    
    public VideoClientRecorderCmdBelief(String agentID, Boolean cmd, Date time)
    {
        super(agentID);
        timestamp = time;
        mRecorderCmd = cmd;
    }
        
    @Override
    protected void addBelief(Belief b)
    {
        VideoClientRecorderCmdBelief belief = (VideoClientRecorderCmdBelief)b;
        
        if (belief.getTimeStamp().compareTo(timestamp)>0)
        {
            this.timestamp = belief.getTimeStamp();
            this.mRecorderCmd = belief.getRecorderCmd();
        }
    }
           
    public void setRecorderCmd(boolean cmd)
    {
        mRecorderCmd = cmd;
    }
    
    public boolean getRecorderCmd()
    {
        return mRecorderCmd;
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

