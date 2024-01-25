/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.tase;

/**
 *
 * @author xud1
 */
    
public class RecorderStatus 
{
    public RecorderStatus (boolean hasReceivedTimeSync, 
                           boolean timestamp, 
                           boolean isRecording, 
                           boolean frameRate,
                           boolean numFramesInQueue, 
                           boolean numFramesDropped,
                           boolean isAdeptPresent, 
                           boolean numWarpQueueUnderflows)
    {
        this.hasReceivedTimeSync = hasReceivedTimeSync;
        this.timestamp = timestamp;
        this.isRecording = isRecording;
        this.frameRate = frameRate;
        this.numFramesInQueue = numFramesInQueue;
        this.numFramesDropped = numFramesDropped;
        this.isAdeptPresent = isAdeptPresent;
        this.numWarpQueueUnderflows = numWarpQueueUnderflows;
    }
    
    public RecorderStatus()
    {
        
    }
            
    public boolean hasReceivedTimeSync;
    public boolean timestamp;
    public boolean isRecording;
    public boolean frameRate;
    public boolean numFramesInQueue;
    public boolean numFramesDropped;
    public boolean isAdeptPresent;
    public boolean numWarpQueueUnderflows;
}
