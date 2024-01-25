package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;

/**
 * A belief that holds count rates submitted by the Bladewerx sensor. This is
 * updated fairly frequently by podAction.java. Every time the Bladewerx sensor
 * sends a count message, podAction receives it and pushes the belief with the
 * new count rate. Used for displaying the Alpha Rate strip chart in the GCS
 * display.
 * 
 * @author fishmsm1
 */
public class AlphaStatisticsBelief extends Belief
{
    public static final String BELIEF_NAME = "AlphaStatisticsBelief";
    
    /**
     * Number of counts during the specified period
     */
    private double rate;
    
    public AlphaStatisticsBelief()
    {
        this(0, System.currentTimeMillis());
    }
    
    public AlphaStatisticsBelief(double rate)
    {
        this(rate, System.currentTimeMillis());
    }
    
    public AlphaStatisticsBelief(double rate, long timestampMs)
    {
        this.rate = rate;
        timestamp = new Date(timestampMs);
    }

    @Override
    protected void addBelief(Belief belief)
    {
        AlphaStatisticsBelief blf = (AlphaStatisticsBelief) belief;
        //System.err.println("Adding");
        if (blf.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = blf.getTimeStamp();
            
            rate = blf.getRate();
        }
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * @return the rate
     */
    public double getRate()
    {
        return rate;
    }
}
