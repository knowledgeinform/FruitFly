
package edu.jhuapl.nstd.swarm.belief;

/**
 *
 * @author stipeja1
 */
public class GammaCompositeHistogramBelief extends CompositeHistogramBelief
{
    public static final String BELIEF_NAME = "GammaCompositeHistogram";

    public GammaCompositeHistogramBelief()
    {
    }

    public GammaCompositeHistogramBelief(String agentID, int[] data, long count, double time)
    {
        super(agentID, data, count, time);
    }

    /**
     * Returns the unique name for this belief type.
     *
     * @return A unique name for this belief type.
     */
    public String getName()
    {
        return BELIEF_NAME;
    }
}
