package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 * A belief that holds count rates submitted by the Bladewerx sensor. This is
 * updated fairly frequently by podAction.java. Every time the Bladewerx sensor
 * sends a count message, podAction receives it and pushes the belief with the
 * new count rate. Primarily used for displaying the Alpha Rate and Beta Rate 
 * strip charts in the GCS display.
 * 
 * @author fishmsm1
 */
public class BladewerxStatisticsBelief extends Belief
{
    public static final String BELIEF_NAME = "BladewerxStatisticsBelief";
    
    private int alphaCounts;
    private int chanZeroCounts; // Represents Gammas and Betas
    private int chanOneCounts; // Represents only Gammas
    private double duration; // In seconds
    
    public BladewerxStatisticsBelief()
    {
        this(0, 0, 0, 0, System.currentTimeMillis());
    }
    
    public BladewerxStatisticsBelief(int alphas, int chan0, int chan1, double duration)
    {
        this(alphas, chan0, chan1, duration, System.currentTimeMillis());
    }
    
    public BladewerxStatisticsBelief(int alphas, int chan0, int chan1, double duration, long timestampMs)
    {
        alphaCounts = alphas;
        chanZeroCounts = chan0;
        chanOneCounts = chan1;
        this.duration = duration;
        timestamp = new Date(timestampMs);
    }

    @Override
    protected void addBelief(Belief belief)
    {
        BladewerxStatisticsBelief blf = (BladewerxStatisticsBelief) belief;
        //System.err.println("Adding");
        if (blf.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = blf.getTimeStamp();
            
            alphaCounts = blf.alphaCounts;
            chanZeroCounts = blf.chanZeroCounts;
            chanOneCounts = blf.chanOneCounts;
            duration = blf.duration;
        }
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * @return the alpha rate
     */
    public double getAlphaRate()
    {
        return ((double)alphaCounts) / duration;
    }
    
    /**
     * @return the beta rate
     */
    public double getBetaRate()
    {
        return ((double)chanZeroCounts - chanOneCounts) / duration;
    }
    
    public int getChannelZeroCounts()
    {
        return chanZeroCounts;
    }
    
    public int getChannelOneCounts()
    {
        return chanOneCounts;
    }
    
    public double getDuration()
    {
        return duration;
    }
}
