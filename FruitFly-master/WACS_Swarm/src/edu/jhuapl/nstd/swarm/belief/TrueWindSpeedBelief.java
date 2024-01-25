package edu.jhuapl.nstd.swarm.belief;

import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.nstd.swarm.TimeManagerFactory;
import java.util.Date;


public class TrueWindSpeedBelief extends Belief
{
    public static final String BELIEF_NAME = "TrueWindSpeedBelief";
    private Speed m_windSpeed;
    private NavyAngle m_windBearing;

    public TrueWindSpeedBelief(final Speed windSpeed, final NavyAngle windBearing)
    {
        timestamp = new Date(TimeManagerFactory.getTimeManager().getTime());
        m_windSpeed = windSpeed;
        m_windBearing = windBearing;
    }

    public Speed getWindSpeed()
    {
        return m_windSpeed;
    }

    public NavyAngle getWindBearing()
    {
        return m_windBearing;
    }

    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

    @Override
    public void addBelief(Belief b)
    {
        TrueWindSpeedBelief belief = (TrueWindSpeedBelief)b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            timestamp = belief.getTimeStamp();
            m_windSpeed = belief.getWindSpeed();
            m_windBearing = belief.getWindBearing();
        }
    }
}
