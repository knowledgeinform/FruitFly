/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.WindEstimateSourceActualBelief;
import edu.jhuapl.nstd.tase.TASE_Telemetry;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author stipeja1
 */
public class PiccoloBeliefUpdater implements Runnable, PropertyChangeListener
{

    private PiccoloAutopilotInterface _api;
    WindHistory m_WindHistory;
    private BeliefManager _belMgr;
    private int _planeID;
    private String _agentID;
    boolean simulate;
    boolean useExternalSim;
    boolean useWind;
    Pic_Telemetry lastPt;

    public PiccoloBeliefUpdater(PiccoloAutopilotInterface api, BeliefManager belMgr, String agentID, int planeID)
    {
        _api = api;
        _belMgr = belMgr;
        _planeID = planeID;
        _agentID = agentID;
        m_WindHistory = new WindHistory(agentID);
        lastPt = null;

        Config.getConfig().addPropertyChangeListener(this);
        propertyChange(null);
    }

    /**
     * The method is called whenever our global configuration changes
     */
    public void propertyChange(PropertyChangeEvent e)
    {
        simulate = Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate", false);
        useExternalSim = Config.getConfig().getPropertyAsBoolean("WACSAgent.useExternalSimulation", false);
    }

    public void run()
    {
        while (true)
        {
            try
            {
                WindEstimateSourceActualBelief windSourceBelief = (WindEstimateSourceActualBelief)_belMgr.get (WindEstimateSourceActualBelief.BELIEF_NAME);
                if (windSourceBelief != null)
                {
                    useWind = (windSourceBelief.getWindSource() == WindEstimateSourceActualBelief.WINDSOURCE_WACSAUTOPILOT);
                }
                
                if (simulate)
                {
                    if (!useExternalSim)
                    {
                        AgentPositionBelief b = (AgentPositionBelief) _belMgr.get(AgentPositionBelief.BELIEF_NAME);
                        double windSouth = Config.getConfig().getPropertyAsDouble("simulation.windsouth", 0);
                        double windWest = Config.getConfig().getPropertyAsDouble("simulation.windwest", 0);

    //                        windSouth = windSouth + (Math.random())*20-10;
    //                        windWest = windWest + (Math.random())*20-10;
    //                         double windHeading = Math.atan2(windWest,-windSouth);
    //                         double windSpeed = Math.sqrt(windSouth*windSouth +windWest*windWest);


                        double windHeading = (Math.random()) * 20 - 10;
                        double windSpeed = 5 + (Math.random()) * 20 - 10;

                        METPositionTimeName mtn = new METPositionTimeName(_agentID, new NavyAngle(windHeading, Angle.RADIANS), new Speed(windSpeed, Speed.METERS_PER_SECOND), b.getPositionTimeName(_agentID).getPosition(), new Date());
                        m_WindHistory.addMETPosition(mtn);

                        if (useWind)
                        {
                            METBelief metbel = new METBelief(_agentID, m_WindHistory.getAverageWind());
                            _belMgr.put(metbel);
                        }

                        METPositionBelief mbel = new METPositionBelief(_agentID, mtn);
                        _belMgr.put(mbel);

                        double lat = b.getPositionTimeName(_agentID).getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES);
                        double lon = b.getPositionTimeName(_agentID).getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES);
                        double alt = b.getPositionTimeName(_agentID).getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.METERS);

                        TASETelemetryBelief ttb = new TASETelemetryBelief(_agentID, new TASE_Telemetry(lat, lon, alt, 0, 0, 0, 1.0, 0));
                        _belMgr.put(ttb);

                        Pic_Telemetry pt = new Pic_Telemetry();
                        pt.Lat = lat;
                        pt.Lon = lon;
                        pt.AltWGS84 = alt;
                        pt.AltMSL = edu.jhuapl.nstd.swarm.util.MathUtils.convertAltAbvElliptoMsl (lat, lon, pt.AltWGS84);
                        pt.PDOP = 1.0;
                        pt.IndAirSpeed_mps = 0.0;
                        pt.OutsideAirTempC = 0.0;
                        pt.StaticPressPa = 101325;

                        /*pt.VelNorth = -3;
                        pt.VelEast = -3;
                        pt.IndAirSpeed = 4;
                        pt.TrueHeading = 270;
                        pt.Pitch = -10;
                        pt.Roll = 0;
                        pt.WindSouth = -10;
                        pt.WindWest = -10;
                        pt.Yaw = 0;*/

    //                         double ws = m_WindHistory.getAverageWind().getWindSpeed().getDoubleValue(Speed.METERS_PER_SECOND);
    //                         double wb = m_WindHistory.getAverageWind().getWindBearing().getDoubleValue(Angle.RADIANS);
    //                         pt.WindSouth = - Math.tan(wb)*
    //                         pt.WindWest

                        PiccoloTelemetryBelief ptb = new PiccoloTelemetryBelief(_agentID, pt);
                        _belMgr.put(ptb);
                    }
                }
                else
                {
                    //Logger.getLogger("GLOBAL").info("requesting for " + _planeID);
                    PlaneInfo info = _api.getPlaneInfo(_planeID);
                    if (info != null)
                    {
                        Latitude latitude = new Latitude(info.getLatitude(), Angle.DEGREES);
                        Longitude longitude = new Longitude(info.getLongitude(), Angle.DEGREES);
                        NavyAngle heading = new NavyAngle(info.getBearing(), Angle.DEGREES);
                        Altitude altitude = new Altitude(info.getAltitudeMSL(), Length.METERS);
                        LatLonAltPosition position = new LatLonAltPosition(latitude, longitude, altitude);
                        double gimbalAz = info.getGimbalAz();
                        double gimbalEl = info.getGimbalEl();
                        //Logger.getLogger("GLOBAL").info("position of " + _planeID + ": " + position);)
                        AgentPositionBelief positionBelief = new AgentPositionBelief(_agentID, position, heading);
                        AgentBearingBelief abb = new AgentBearingBelief(_agentID, heading, heading);

                        double windHeading = Math.atan2(-_api.m_Telemetry.WindWest, -_api.m_Telemetry.WindSouth);
                        double windSpeed = Math.sqrt(_api.getTelemetry().WindSouth * _api.getTelemetry().WindSouth + _api.getTelemetry().WindWest * _api.getTelemetry().WindWest);
                        NavyAngle winddir = new NavyAngle(windHeading, Angle.RADIANS);
                        winddir = winddir.plus(new Angle(180, Angle.DEGREES));
                        METPositionTimeName mtn = new METPositionTimeName(_agentID, winddir, new Speed(windSpeed, Speed.METERS_PER_SECOND), positionBelief.getPositionTimeName(_agentID).getPosition(), new Date());
                        m_WindHistory.addMETPosition(mtn);
                        if (useWind)
                        {
                            METBelief metbel = new METBelief(_agentID, m_WindHistory.getAverageWind());
                            _belMgr.put(metbel);
                        }
                        METPositionBelief mbel = new METPositionBelief(_agentID, mtn);

                        _belMgr.put(positionBelief);
                        _belMgr.put(abb);
                        _belMgr.put(mbel);
                    }

                    Pic_Telemetry pt = _api.getTelemetry();

                    if (pt != null && pt != lastPt)
                    {
                        _belMgr.put(new PiccoloTelemetryBelief(_agentID, pt));
                        lastPt = pt;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                continue;
            }

            try
            {
                Thread.sleep(Config.getConfig().getPropertyAsLong("PiccoloBeliefUpdater.UpdateSleep", 500));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}
