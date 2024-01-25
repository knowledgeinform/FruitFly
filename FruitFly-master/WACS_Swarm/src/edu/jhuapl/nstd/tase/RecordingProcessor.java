/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.tase;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.VideoClientConversionFinishedBelief;
/**
 *
 * @author xud1
 */
public class RecordingProcessor 
{
    private native static void GenerateAvi(String format, String inputDir, String outputDir, int frameNum);
    private ConvertVideoThread conversionThread = null;    
    private String format;
    private String inputDir;
    private String outputDir;
    private int frameNum;
    private BeliefManager beliefManager;
    private String agentID;    
    
    public RecordingProcessor(String lFormat, String lInputDir, String lOutputDir, int lFrameNum, BeliefManager lBeliefManager, String lAgentID)
    {
        format = lFormat;;
        inputDir = lInputDir;
        outputDir = lOutputDir;
        frameNum = lFrameNum;     
        beliefManager = lBeliefManager;
        agentID = lAgentID;
    }    
    
    static
    {
        try
        {
            System.loadLibrary("RecordingProcessor");
        }
        catch(UnsatisfiedLinkError ex)
        {
            System.out.println(ex.getLocalizedMessage());
            System.out.println(ex.getMessage());            
        }
    }    
    
    private class ConvertVideoThread extends Thread
    {       
        @Override 
        public void run()
        {
            GenerateAvi(format, inputDir, outputDir, frameNum);   
            VideoClientConversionFinishedBelief rcb = new VideoClientConversionFinishedBelief(agentID);
            beliefManager.put(rcb);            
        }
    }
    
    public void convert()
    {
        if(conversionThread == null)
        {
            conversionThread = new ConvertVideoThread();
            conversionThread.start();
        }
        else if(!conversionThread.isAlive())
        {
            conversionThread = new ConvertVideoThread();
            conversionThread.start();
        }
    }
}
