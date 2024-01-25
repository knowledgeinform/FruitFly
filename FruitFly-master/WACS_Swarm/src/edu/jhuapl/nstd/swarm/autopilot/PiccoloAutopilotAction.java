/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.Updateable;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.action.AvoidObstacleAction;
import edu.jhuapl.nstd.swarm.action.ReturnToBaseAction;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.behavior.group.BehaviorGroup;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.GimbalDeployBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataActualBelief;
import edu.jhuapl.nstd.swarm.belief.ZeroAirDataBelief;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 *
 * @author stipeja1
 */
public class PiccoloAutopilotAction implements  Updateable, PropertyChangeListener
{
    Date prevTargetTime;
    Date prevGimbalTime;
    Date prevZeroTime;
    Double prevZeroAltMSLm;
    Double prevBasePressurePa;
    BeliefManager _beliefManager;
    String _agentID = "";
    BehaviorGroup _behaviorGroup;
    private PiccoloAutopilotInterface _api;
    private TASEGimbalInterface _tgi;
    int _planeID = -1;
    String _lastAgentMode = "";
    int m_TimeoutTimeGimbalToStandby_ms;
    double m_GimbalStandbyTiltDegrees;

    protected Altitude _setAltAGL = Altitude.ZERO;
    
    protected AvoidObstacleAction _avoidObstacleAction;
    protected ReturnToBaseAction _returnToBaseAction;

    
    public PiccoloAutopilotAction(BeliefManager mgr, String agentID, BehaviorGroup behavior,
			PiccoloAutopilotInterface api, TASEGimbalInterface tgi,  int id)
    {
      	_agentID = agentID;
        _beliefManager = mgr;
        _behaviorGroup = behavior;
        _planeID = id;
        _api = api;
        _tgi = tgi;



        //_avoidObstacleAction = new AvoidObstacleAction(mgr);
	//_returnToBaseAction = new ReturnToBaseAction(mgr,_agentID,api,id);
	//setAltitude(new Altitude(Config.getConfig().getPropertyAsDouble("preferredAltitude"), Length.FEET));

	Config.getConfig().addPropertyChangeListener(this);

    }


    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        m_TimeoutTimeGimbalToStandby_ms = Config.getConfig().getPropertyAsInteger("PiccoloAutopilotAction.GimbalToStandbyTimeoutTime.Ms", 20000);
        m_GimbalStandbyTiltDegrees = Config.getConfig().getPropertyAsDouble ("PiccoloAutopilotAction.GimbalStandbyTilt.Degrees", 20);
    }

    @Override
    public void update()
    {
        try
        {
            AgentModeActualBelief agentModeBelief = (AgentModeActualBelief)_beliefManager.get(AgentModeActualBelief.BELIEF_NAME);
            if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(LoiterBehavior.MODENAME))
            {
                String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                TargetActualBelief targets = (TargetActualBelief)_beliefManager.get(TargetActualBelief.BELIEF_NAME);

                if((!_lastAgentMode.equals(LoiterBehavior.MODENAME) && targets != null) || (targets != null && prevTargetTime == null) ||(targets != null && prevTargetTime!=null && targets.getTimeStamp().after(prevTargetTime)))
                {
                    prevTargetTime = targets.getTimeStamp();
                    PositionTimeName ptn = targets.getPositionTimeName(tmp);

                    if(ptn !=null)
                    {
                        LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                        _tgi.setTASELookLocation(lla.getLatitude().getDoubleValue(Angle.DEGREES),
                                lla.getLongitude().getDoubleValue(Angle.DEGREES),
                                lla.getAltitude().getDoubleValue(Length.METERS));

                    }
                }
            }
            else if ((agentModeBelief != null) && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null && agentModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(ParticleCloudPredictionBehavior.MODENAME))
            {
                if(System.currentTimeMillis()-agentModeBelief.getTimeStamp().getTime() > m_TimeoutTimeGimbalToStandby_ms)
                _tgi.setTasePanTilt(0, -m_GimbalStandbyTiltDegrees);
            }



                 //Deploy or Stow Gimbal on Change
                GimbalDeployBelief bel = (GimbalDeployBelief)_beliefManager.get(GimbalDeployBelief.BELIEF_NAME);

                if((bel != null && prevGimbalTime == null) ||(bel != null && prevGimbalTime!=null && bel.getTimeStamp().after(prevGimbalTime)))
                {
                    prevGimbalTime = bel.getTimeStamp();
                    if(bel.isDeployed())
                    {
                        _tgi.deployGimbal();
                    }
                    else
                    {
                        _tgi.stowGimbal();
                    }

                }



                ZeroAirDataBelief zbel = (ZeroAirDataBelief)_beliefManager.get(ZeroAirDataBelief.BELIEF_NAME);

                if((zbel != null && prevZeroTime == null) ||(zbel != null && prevZeroTime!=null && zbel.getTimeStamp().after(prevZeroTime)))
                {
                    prevZeroTime = zbel.getTimeStamp();
                    prevZeroAltMSLm = zbel.getAltMSLm ();
                    prevBasePressurePa = zbel.getBasePressurePa();
                    _api.setAltimeter(prevBasePressurePa);
                    _api.zeroAirData(prevZeroAltMSLm);
                    
                    ZeroAirDataActualBelief actualBel = new ZeroAirDataActualBelief(prevZeroAltMSLm, prevBasePressurePa);
                    _beliefManager.put(actualBel);
                }

                if (agentModeBelief != null && agentModeBelief.getMode(WACSAgent.AGENTNAME) != null)
                    _lastAgentMode = agentModeBelief.getMode(WACSAgent.AGENTNAME).getName();

        }
        catch(Exception e)
        {
            System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
        }
    }



}
