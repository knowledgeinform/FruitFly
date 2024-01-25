/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.mapping.sensors.gpsNmea.GpsReceiverInterfaceThread;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAStreamLogger;
import edu.jhuapl.nstd.swarm.util.*;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.piccolo.Pic_TelemetryListener;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.piccolo.Pic_Interface_NMEA;
import edu.jhuapl.nstd.swarm.ReplayNmeaEtdLogs;
import java.util.Random;
import edu.jhuapl.nstd.swarm.belief.NmeaRawMessageBelief;

/**
 *
 * @author stipeja1
 */
public class PiccoloAutopilotInterface implements AutoPilotInterface, Pic_TelemetryListener
{

    PlaneInfo m_PlaneInfo;
    Pic_Telemetry m_Telemetry;
    PiccoloBeliefUpdater m_Updater;
    Pic_Interface m_PicInterface;
    Pic_Interface_NMEA m_PicInterfaceNMEA;
    Boolean m_isNMEAFormat;
    GpsReceiverInterfaceThread m_GpsClient;
    Thread pt;
    Thread t;
    Thread nt;
    String _agentID;
    BeliefManager _belMgr;
    NMEAStreamLogger _nmeaStreamLogger;

    public PiccoloAutopilotInterface(BeliefManager belMgr, String agentID, int planeID)
    {
        _agentID = agentID;
        _belMgr = belMgr;
        
        String comport = Config.getConfig().getProperty("PiccoloAutopilotInterface.ComPort", "COM6");
        int planeNum = Config.getConfig().getPropertyAsInteger("PiccoloAutopilotInterface.planenum", 1);
        m_isNMEAFormat = Config.getConfig().getPropertyAsBoolean("PiccoloAutopilotInterface.isNMEAFormat", true);
        
        if(m_isNMEAFormat) {
            try {
                _nmeaStreamLogger = new NMEAStreamLogger(".\\rawNmeaLogs_formatted\\", 3);
                m_GpsClient = new GpsReceiverInterfaceThread(_nmeaStreamLogger);
                m_GpsClient.open(comport, 9600);
            } catch (Exception e) {
                m_GpsClient = null;
            }
            if(m_GpsClient!=null) {
                m_GpsClient.start();
            } else {
                //create a GPS Client but don't open port so that we can store data when reading from a file
                try {
                    _nmeaStreamLogger = new NMEAStreamLogger(".\\rawNmeaLogs_formatted\\", 3);
                    m_GpsClient = new GpsReceiverInterfaceThread(_nmeaStreamLogger);
                }  catch (Exception e) {
                    m_GpsClient = null;
                }             
            }
            
            m_PicInterfaceNMEA = new Pic_Interface_NMEA(planeNum, m_GpsClient);
            m_PicInterfaceNMEA.addPicTelemetryListener(this);
                 
            try
            {
                pt = (new Thread(m_PicInterfaceNMEA));
                pt.setName ("WACS-PiccoloAPInterface-NMEA");
            } catch (Exception e) {
		e.printStackTrace();
            }
            
            try
            {
                nt = (new Thread(_nmeaStreamLogger));
                nt.setName ("WACS-NMEAStreamLogger");
            } catch (Exception e) {
		e.printStackTrace();
            }
        } else {
            m_PicInterface = new Pic_Interface(planeNum, comport, 9600, false);
            m_PicInterface.addPicTelemetryListener(this);
            
            try {
                pt = (new Thread(m_PicInterface));
                pt.setName ("WACS-PiccoloAPInterface");
            } catch (Exception e) {
		e.printStackTrace();
            }
        }

        try
        {
            t = new Thread(new PiccoloBeliefUpdater(this, belMgr, agentID, planeID));
            t.setName ("WACS-PicBeliefUpdater");
	} catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setReplayNmeaEtdLogs(ReplayNmeaEtdLogs replayNmeaEtdLogs) {
        m_PicInterfaceNMEA.setReplayNmeaEtdLogs(replayNmeaEtdLogs);
    }
    
    public void start() {
        try {
            pt.start();
            t.start();
            nt.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem)
    {
        //if(telem.GPS_Status !=0)
        //{
            m_Telemetry = telem;
            m_PlaneInfo = new PlaneInfo(telem.Lat, telem.Lon, telem.AltWGS84, telem.AltMSL, telem.IndAirSpeed_mps, telem.Yaw);
            
            NmeaRawMessageBelief nmeaBelief = new NmeaRawMessageBelief(_agentID, currentTime, telem.rawMessage, telem.Lat, telem.Lon, telem.AltMSL);
            _belMgr.put(nmeaBelief);
            
       // }
//        System.out.println("Pic Telemetry => ");
//        System.out.println("\t(lat,lon,alt) = ("+telem.Lat+","+telem.Lon+","+telem.AltEllip+")");
//        System.out.println("\t(r,p,y,comp) = ("+telem.Roll+","+telem.Pitch+","+telem.Yaw+","+telem.TrueHeading+")");
//        System.out.println("\t(WindSouth,WindWest,IAS) = ("+telem.WindSouth+","+telem.WindWest+","+telem.IndAirSpeed+")");
//        System.out.println("\t(vnorth,vest,vdown) = ("+telem.VelNorth+","+telem.VelEast+","+telem.VelDown+")");
//        System.out.println("\t(PDOP,Status) = ("+telem.PDOP+","+telem.GPS_Status+")\n\n");
    }

    public PlaneInfo getPlaneInfo(int planeID) throws AutoPilotException
    {

        return m_PlaneInfo;
    }

    public void sendLoiter(int planeID, double lat, double lon, double alt, double speed, double radius) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendWaypoint(int planeID, double lat, double lon, double alt, double speed) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetGimbal(int planeID) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendGimbal(int planeID, double lat, double lon, double height) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void land(int planeID) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendWaypoint(double lat, double lon, double alt, double speed, int planeID, int commandNum, int totalCommands) throws AutoPilotException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void zeroAirData(Double altMSLm)
    {
        if(m_isNMEAFormat) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
             m_PicInterface.zeroAirData(altMSLm);         
        }
    }

    public void setAltimeter(Double basePressurePa)
    {
        if(m_isNMEAFormat) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
             m_PicInterface.setAltimeter(basePressurePa);           
        }
    }

    /**
     * @return the m_Telemetry
     */
    public Pic_Telemetry getTelemetry()
    {
        return m_Telemetry;
    }


}
