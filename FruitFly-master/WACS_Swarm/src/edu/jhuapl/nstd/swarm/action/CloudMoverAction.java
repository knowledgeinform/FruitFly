package edu.jhuapl.nstd.swarm.action;

import java.util.logging.*;




import java.util.*;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;

public class CloudMoverAction implements Updateable
{

    protected BeliefManager _belMgr;
    protected Length _windSpeed = new Length(Config.getConfig().getPropertyAsDouble("SimulationMode.windspeed", 0), Length.METERS);
    protected NavyAngle _windBearing = new NavyAngle(Config.getConfig().getPropertyAsDouble("SimulationMode.windbearing", 0), Angle.DEGREES);
    protected double m_timeAccelerationCoefficient = Config.getConfig().getPropertyAsDouble("SimulationMode.timeAccelerationCoefficient",1);
    protected Angle _windBearingDelta;
    protected long _nextWindChange = 0;
    protected long _stretchFor = 600000;
    protected long _stretchStart = 0;
    protected long _lastUpdated = -1;
    protected double _plumeTopAltitudeCoeff = 6.9462; //average coeff from published plume papers
    protected double _plumeThicknessToTopAltitudeRatio = 0.5; //average ratio from published plume papers
    protected double _explosionEquivalentTNTPounds = 200;

    public CloudMoverAction(BeliefManager belMgr)
    {
        _belMgr = belMgr;
        _plumeTopAltitudeCoeff = Math.random() * 3.4 + 4.8;
        _plumeThicknessToTopAltitudeRatio = Math.random() * 0.2 + 0.35;
    }

    public void setPlumeTopAltitudeCoeff(final double plumeTopAltitudeCoeff)
    {
        _plumeTopAltitudeCoeff = plumeTopAltitudeCoeff;
    }

    public void setPlumeThicknessToTopAltitudeRatio(final double plumeThicknessToTopAltitudeRatio)
    {
        _plumeThicknessToTopAltitudeRatio = plumeThicknessToTopAltitudeRatio;
    }

    public void setExplosionEquivalentTNTPounds(final double explosionEquivalentTNTPounds)
    {
        _explosionEquivalentTNTPounds = explosionEquivalentTNTPounds;
    }

    @Override
    public void update()
    {
        try
        {
            if (_lastUpdated == -1)
            {
                _lastUpdated = System.currentTimeMillis();
                _stretchStart = _lastUpdated;
            }

            long now = System.currentTimeMillis();
            long difference = now - _lastUpdated;
            double diff = (double) difference / 1000.0;

            TrueWindSpeedBelief trueWindSpeedBelief = (TrueWindSpeedBelief) _belMgr.get(TrueWindSpeedBelief.BELIEF_NAME);
            METBelief metbel = (METBelief) _belMgr.get(METBelief.BELIEF_NAME);
            if (trueWindSpeedBelief != null)
            {
                _windBearing = trueWindSpeedBelief.getWindBearing();
                _windSpeed = new Length(trueWindSpeedBelief.getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND), Length.METERS);
            }
            else
            {
                boolean updated = false;
                if (metbel != null)
                {
                    METTimeName mtn = metbel.getMETTimeName(WACSAgent.AGENTNAME);
                    if (mtn != null)
                    {
                        _windBearing = mtn.getWindBearing();
                        _windSpeed = new Length(mtn.getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND), Length.METERS);
                        updated = true;
                    }
                }
                if (!updated)
                {
                    _windSpeed = new Length(Config.getConfig().getPropertyAsDouble("SimulationMode.windspeed", 0),
                            Length.METERS);

                    _windBearing = new NavyAngle(Config.getConfig().getPropertyAsDouble("SimulationMode.windbearing", 0),
                            Angle.DEGREES);
                }
            }
            

            CloudBelief cb = (CloudBelief) _belMgr.get(CloudBelief.BELIEF_NAME);
            if (cb != null)
            {
                Ellipse e = cb.getEllipse();
                difference = System.currentTimeMillis() - _lastUpdated;
                diff = (double) difference / 1000.0;
                RangeBearingHeightOffset windDisplacement = new RangeBearingHeightOffset(_windSpeed.times(diff * m_timeAccelerationCoefficient),
                                                                                         _windBearing,
                                                                                         Length.ZERO);
                e = e.move(windDisplacement);

                ExplosionBelief explosionBelief = (ExplosionBelief) _belMgr.get(ExplosionBelief.BELIEF_NAME);
                if (explosionBelief != null)
                {
                    double timeSinceExplosion_sec = (System.currentTimeMillis() - explosionBelief.getTime_ms()) / 1000.0f;
                    double topAlt_m = Math.pow(_explosionEquivalentTNTPounds, 0.25) * _plumeTopAltitudeCoeff * Math.sqrt(timeSinceExplosion_sec);
                    double plumeThickness_m = topAlt_m * _plumeThicknessToTopAltitudeRatio;
                    cb.setHeight(new Length(plumeThickness_m, Length.METERS));
                    cb.setBottomAltitudeAGL(new Altitude(topAlt_m - plumeThickness_m, Length.METERS));
                }


                if (now - _stretchStart < _stretchFor)
                {
                    e = e.stretch(_windSpeed.times(diff).dividedBy(4.0));
                }

                cb.setEllipse(e);
                _belMgr.put(cb);
            }
            _lastUpdated = System.currentTimeMillis();
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }
}
		
	
