/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.nstd.swarm.util.Config;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author stipeja1
 */
public class AlphaCompositeHistogramBelief extends CompositeHistogramBelief
{

    public static final String BELIEF_NAME = "AlphaCompositeHistogram";

    public AlphaCompositeHistogramBelief()
    {
    }

    public AlphaCompositeHistogramBelief(String agentID, int[] data, long count, double time)
    {
        super(agentID, data, count, time);
    }

    public static Belief deserialize(InputStream iStream, Class clas) throws IOException
    {
        DataInputStream in = new DataInputStream(iStream);

        AlphaCompositeHistogramBelief belief = null;

        try
        {
            belief = (AlphaCompositeHistogramBelief) clas.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        belief.readExternal(in);

        return belief;
    }

    /**
     * Retuns the unique name for this belief type.
     *
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }
}
