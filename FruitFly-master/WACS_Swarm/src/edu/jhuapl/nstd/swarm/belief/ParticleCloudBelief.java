package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import java.util.Vector;

public class ParticleCloudBelief extends Belief implements BeliefExternalizable
{
    /**
     * The unique name of this belief type.
     */
    public static final String BELIEF_NAME = "ParticleCloudBelief";

    private class ParticleDisplacement
    {
        double displacementNorth_m = 0;
        double displacementEast_m = 0;
        double displacementUp_m = 0;
    }

    private LatLonAltPosition m_startPosition;
    private Vector<ParticleDisplacement> m_particleDisplacements = new Vector<ParticleDisplacement>();

    public ParticleCloudBelief(final LatLonAltPosition startPosition, final int numParticles)
    {
        m_startPosition = startPosition;
        for (int i = 0; i < numParticles; ++i)
        {
            m_particleDisplacements.add(new ParticleDisplacement());
        }
    }

    public LatLonAltPosition getStartPosition()
    {
        return m_startPosition;
    }

    

    /**
     * Retuns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    /**
     * adds Replaces the current belief with a new one
     * @param b The new belief
     */
    @Override
    public void addBelief(Belief b)
    {
        ParticleCloudBelief belief = (ParticleCloudBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            m_startPosition = belief.m_startPosition;
            m_particleDisplacements = belief.m_particleDisplacements;
        }
    }

}
