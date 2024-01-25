package edu.jhuapl.nstd.swarm.action;

import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.*;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.cbrnPods.actions.podAction;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;

import edu.jhuapl.nstd.swarm.wacs.*;

public class SimulateCloudSensingAction implements Updateable
{

    private BeliefManager _beliefMgr;
    private String _agentID;

    public SimulateCloudSensingAction(BeliefManager beliefMgr, String agentID)
    {
        _beliefMgr = beliefMgr;
        _agentID = agentID;
    }

    @Override
    public void update()
    {
        try
        {
            if (Config.getConfig().getPropertyAsBoolean("WACSAgent.simulate", false)
                    || Config.getConfig().getPropertyAsBoolean("WACSAgent.simulateOnlyCloudSensing", false))
            {
                simulateCloudSensing();
            }
        }
        catch (Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }

    protected void simulateCloudSensing()
    {
        try
        {
            CloudBelief cb = (CloudBelief) _beliefMgr.get(CloudBelief.BELIEF_NAME);

            if (cb != null)
            {
                Ellipse ellipse = cb.getEllipse();
                AbsolutePosition position = getMyPosition();

                float value = 1.0f;
                Altitude bottomCloudAltitudeAGL = cb.getBottomAltitudeAGL();
                Altitude topCloudAltitudeAGL = cb.getBottomAltitudeAGL().plus(cb.getHeight());
                Altitude groundAltitudeBelowPlane = DtedGlobalMap.getDted().getJlibAltitude(position.asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES), position.asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES));
                Altitude myAltitudeAGL = position.asLatLonAltPosition().getAltitude().minus(groundAltitudeBelowPlane.asLength());
                if (ellipse.contains(position) && myAltitudeAGL.isGreaterThanOrEqualTo(bottomCloudAltitudeAGL) && myAltitudeAGL.isLessThanOrEqualTo(topCloudAltitudeAGL))
                {
                    Length distanceToCenterOfCloud = position.getRangeTo(ellipse.getCenter());
                    Length radiusAtAngle = ellipse.getRadius(ellipse.getCenter().getBearingTo(position));
                    double normalizedRelativePosition = radiusAtAngle.minus(distanceToCenterOfCloud).dividedBy(radiusAtAngle).getDoubleValue();
                    value = 9.0f * (float) normalizedRelativePosition;

                    double prob = Math.random();
                    short id = 0;
                    if (prob < 0.4)
                        podAction.addChemicalDetection(_beliefMgr, _agentID, position, value, id);
                    else if (prob > 0.6)
                        podAction.addParticleDetection(_beliefMgr, _agentID, position, value, id, (short)value);
                    else
                    {
                        podAction.addChemicalDetection(_beliefMgr, _agentID, position, value, id);
                        podAction.addParticleDetection(_beliefMgr, _agentID, position, value, id, (short)value);
                    }

                   // BioPodAction.addDetection(_beliefMgr, _agentID, position.translatedBy(new RangeBearingHeightOffset(new Length(0.01, Length.METERS), new NavyAngle(0.0,Angle.DEGREES), new Length(0.01, Length.METERS))), value);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected LatLonAltPosition getMyPosition()
    {
        AgentPositionBelief b = (AgentPositionBelief) _beliefMgr.get(AgentPositionBelief.BELIEF_NAME);
        if (b != null)
        {
            return b.getPositionTimeName(_agentID).getPosition().asLatLonAltPosition();
        }
        else
        {
            return null;
        }
    }
}
