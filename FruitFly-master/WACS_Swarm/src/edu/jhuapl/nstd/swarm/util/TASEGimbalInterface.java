/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.util;

import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.TASEPointingAnglesBelief;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.tase.TASE_Interface;
import edu.jhuapl.nstd.tase.TASE_PointingAngles;
import edu.jhuapl.nstd.tase.TASE_PointingAnglesListener;
import edu.jhuapl.nstd.tase.TASE_Telemetry;
import edu.jhuapl.nstd.tase.TASE_TelemetryListener;

/**
 *
 * @author stipeja1
 */
public class TASEGimbalInterface implements TASE_PointingAnglesListener, TASE_TelemetryListener
{
    TASE_Interface m_TASE;
    private TASE_Telemetry m_Telemetry;
    private TASE_PointingAngles m_Angles;
    private BeliefManager _beliefManager;
    private String _agentID;
    private int _planeID;


    public TASEGimbalInterface(BeliefManager belMgr, String agentID, int planeID) throws Exception
    {
            _beliefManager = belMgr;
            _agentID = agentID;
            _planeID = planeID;
            m_TASE = new TASE_Interface(Config.getConfig().getProperty("TASEGimbalInterface.ComPort", "COM2"));
	    m_TASE.addTASEPointingAnglesListener(this);
	    m_TASE.addTASETelemetryListener(this);

	    (new Thread(m_TASE)).start();
            m_TASE.sendVPS_DisableTracking();
            m_TASE.sendVPS_DisableSymbology();

    }

    public void stowGimbal()
    {
        m_TASE.sendTASE_Stow();
        m_TASE.sendTASE_RetractDeploy(false);
    }

    public void deployGimbal()
    {
        m_TASE.sendTASE_RetractDeploy(true);
    }
    
    
    public void setTASELookLocation( double lat, double lon, double altEllip,
                    double vNorth, double vEast, double vDown ) 
    {
        m_TASE.sendTASE_SPOI(lat, lon, altEllip, vNorth, vEast, vDown);
    }

    public void setTASELookLocation( double lat, double lon, double altEllip)
    {
        m_TASE.sendTASE_SPOI(lat, lon, altEllip, 0.0, 0.0, 0.0);
    }

    public void setTaseLookingForward()
    {
        m_TASE.sendTASE_PointForward();
    }

    public void setTasePanTilt(double panDegrees, double tiltDegrees)
    {
        m_TASE.sendTASE_PointPanTilt(panDegrees, tiltDegrees);
    }

    @Override
    public void handleTASE_PointingAngles(TASE_PointingAngles angles) 
    {
        m_Angles = angles;
        System.out.println("TASE Pointing Angles => (pan,tilt) = ("+
						   angles.Pan+","+angles.Tilt+")\n");
        
        TASEPointingAnglesBelief tpab = new TASEPointingAnglesBelief (_agentID, angles);
        _beliefManager.put(tpab);
    }

    @Override
    public void handleTASE_Telemetry(TASE_Telemetry telem) 
    {
        m_Telemetry = telem;
        System.out.println("TASE Telemetry => ");
		System.out.println("\t(lat,lon,alt) = ("+telem.Lat+","+telem.Lon+","+telem.AltEllip+")");
		System.out.println("\t(roll,pitch,yaw) = ("+telem.CameraRoll+","+telem.CameraPitch+","+telem.CameraYaw+")");
		System.out.println("\t(PDOP,status) = ("+telem.PDOP+","+telem.GPS_Status+")\n");

        
        TASETelemetryBelief ttb = new TASETelemetryBelief(_agentID, telem);
        _beliefManager.put(ttb);
    }

    /**
     * @return the m_Telemetry
     */
    public TASE_Telemetry getM_Telemetry()
    {
        return m_Telemetry;
    }

    /**
     * @return the m_Angles
     */
    public TASE_PointingAngles getM_Angles()
    {
        return m_Angles;
    }

}
