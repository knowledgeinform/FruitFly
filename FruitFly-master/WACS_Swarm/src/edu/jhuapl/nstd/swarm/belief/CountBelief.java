
package edu.jhuapl.nstd.swarm.belief;

/**
 *
 * @author fishmsm1
 */
public class CountBelief extends Belief
{
    public static final String BELIEF_NAME = "CountBelief";
    
    /**
     * Number of counts during the specified period
     */
    private int counts;
    
    /**
     * Amount of time during which counts occurred
     */
    private int durationMs;
    
    public CountBelief()
    {
        counts = 0;
    }
    
    public CountBelief(int numCounts, int duration)
    {
        counts = numCounts;
        durationMs = duration;
    }

    @Override
    protected void addBelief(Belief belief)
    {
        CountBelief blf = (CountBelief) belief;
        //System.err.println("Adding");
        if (blf.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = blf.getTimeStamp();
            
            counts = blf.getCounts();
            durationMs = blf.getDurationMs();
        }
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * @return the counts
     */
    public int getCounts()
    {
        return counts;
    }

    /**
     * @return the durationMs
     */
    public int getDurationMs()
    {
        return durationMs;
    }
}
