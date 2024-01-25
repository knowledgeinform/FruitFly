package edu.jhuapl.nstd.swarm.belief;

import java.util.Date;


public class ZeroAirDataBelief  extends Belief
{
    public static final String BELIEF_NAME = "ZeroAirDataBelief";

    Double _altMSLm;
    Double _basePressurePa;

    public ZeroAirDataBelief(Double altMSLm, Double basePressurePa)
    {
        super();
        _altMSLm = altMSLm;
        _basePressurePa = basePressurePa;
        timestamp = new Date(System.currentTimeMillis());
    }

    public String toLogMessage ()
    {
        return _altMSLm + ", " + _basePressurePa;
    }

    void setAltMSLm (Double newAlt)
    {
        _altMSLm = new Double (newAlt);
    }

    void setAltMSLm (double newAlt)
    {
        _altMSLm = new Double (newAlt);
    }

    public Double getAltMSLm ()
    {
        return _altMSLm;
    }

    void setBasePressurePa (Double newPressurePa)
    {
        _basePressurePa = new Double (newPressurePa);
    }

    void setBasePressurePa (double newPressurePa)
    {
        _basePressurePa = new Double (newPressurePa);
    }

    public Double getBasePressurePa ()
    {
        return _basePressurePa;
    }

    
    @Override
    public void addBelief(Belief b)
    {
        ZeroAirDataBelief belief = (ZeroAirDataBelief) b;
        if (belief.getTimeStamp().compareTo(timestamp) > 0)
        {
            this.timestamp = belief.getTimeStamp();
            this._altMSLm = belief.getAltMSLm();
            this._basePressurePa = belief.getBasePressurePa();
        }
    }

    /**
     * Returns the unique name for this belief type.
     * @return A unique name for this belief type.
     */
    @Override
    public String getName()
    {
        return BELIEF_NAME;
    }

}
