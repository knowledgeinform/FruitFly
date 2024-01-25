/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.piccolo;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.GpsReceiverInterfaceThread;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAEvent;
import edu.jhuapl.nstd.mapping.sensors.gpsNmea.NMEAStreamLogger;
import edu.jhuapl.nstd.swarm.ReplayNmeaEtdLogs;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import edu.jhuapl.nstd.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kayjl1
 */
public class Pic_Interface_NMEA implements Runnable {
	private int _autopilotNum;
        private GpsReceiverInterfaceThread m_GpsClient;
        private GpsReceiverInterfaceThread.GpsReading m_GpsData;

	private List<Pic_TelemetryListener> _telemListeners = new ArrayList<Pic_TelemetryListener>();
        
        private String _piccoloSource = "File";
        private double _startLat;
        private double _startLon;
        private double _startAlt;
        private int _piccoloPeriodMs = 500;
        
        private ReplayNmeaEtdLogs _replayNmeaEtdLogs = null;

	public Pic_Interface_NMEA(int apNum, GpsReceiverInterfaceThread gpsClient)
	{
	    _autopilotNum = apNum;
            m_GpsClient = gpsClient;
           _piccoloSource = Config.getConfig().getProperty("Piccolo.source", "Serial");
           _piccoloPeriodMs = Config.getConfig().getPropertyAsInteger("Piccolo.periodMs", 500);
           _startLat = Config.getConfig().getPropertyAsDouble("agent.startLat", false);
           _startLon = Config.getConfig().getPropertyAsDouble("agent.startLon", false);
           _startAlt = Config.getConfig().getPropertyAsDouble("agent.launchHeight.Feet", false);
	}

	public void addPicTelemetryListener(Pic_TelemetryListener obj) {
		_telemListeners.add(obj);
	}
	public void removePicTelemetryListener(Pic_TelemetryListener obj) {
		_telemListeners.remove(obj);
	}
        
    public void setReplayNmeaEtdLogs(ReplayNmeaEtdLogs replayNmeaEtdLogs) {
        _replayNmeaEtdLogs = replayNmeaEtdLogs;
    }

	
    @Override
    public void run() {
        while (true) {
            
            if(_piccoloSource.equalsIgnoreCase("Serial")) {
                m_GpsData = new GpsReceiverInterfaceThread.GpsReading();
                m_GpsClient.copyLatestData(m_GpsData);

                if(m_GpsData!=null && m_GpsData.m_FixQuality!=GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID) {
                    Pic_Telemetry telem = new Pic_Telemetry();
                    telem.Lat = m_GpsData.m_LatDecDeg;
                    telem.Lon = m_GpsData.m_LonDecDeg;
                    telem.AltWGS84 = m_GpsData.m_GeoidHeightAboveWGS84Meters;
                    telem.AltMSL = m_GpsData.m_AltitudeMSLMeters;
                    telem.rawMessage = m_GpsData.m_rawMessage;

                    for (Pic_TelemetryListener listen : _telemListeners)
                        listen.handlePic_Telemetry(System.currentTimeMillis(), telem);
                }
                
                try {
                    Thread.sleep(_piccoloPeriodMs);
                } catch (InterruptedException ex) {
                    System.out.println("Error with Thread.sleep");
                }
            } else if(_piccoloSource.equalsIgnoreCase("Sim")){
                Pic_Telemetry telem = new Pic_Telemetry();
                Random rand = new Random();
                telem.Lat = _startLat+rand.nextDouble()/10.0;
                telem.Lon = _startLon+rand.nextDouble()/10.0;
                telem.AltWGS84 = _startAlt;
                telem.AltMSL = _startAlt;
                
                // assuming positive/North lat
                int latDegrees = (int) Math.floor(telem.Lat);
                double latMinutes = (telem.Lat - Math.floor(telem.Lat))*60;
                latMinutes = Math.round(latMinutes*10000.0)/10000.0;
                telem.Lat = latDegrees+latMinutes/60;
                String latString = String.format("%02d%07.4f",latDegrees, latMinutes);
                
                // assuming negative/West lon
                double posLon = Math.abs(telem.Lon);
                int lonDegrees = (int) Math.floor(posLon);
                double lonMinutes = (posLon - Math.floor(posLon))*60;
                lonMinutes = Math.round(lonMinutes*10000.0)/10000.0;
                telem.Lon = -1*(lonDegrees+lonMinutes/60);
                String lonString = String.format("0%02d%07.4f", lonDegrees, lonMinutes);
                
                String altStringWGS84 = String.format("%.1f", telem.AltWGS84);
                String altStringMSL = String.format("%.1f", telem.AltMSL);
                
                DateFormat dateFormat = new SimpleDateFormat("hhmmss");
                long currentTime = System.currentTimeMillis();
                Date dateTime = new Date(currentTime);
                String ggaTime = dateFormat.format(dateTime);
                
                //note: not logging the whole message, just lat/lon/alt since that's all we'll be reading back in later
                telem.rawMessage = "$GPGGA," + ggaTime + "," + latString + ",N," + lonString + ",W,1,06,2.8," + altStringMSL + ",M," + altStringWGS84 + ",M,,*62";
                
                for (Pic_TelemetryListener listen : _telemListeners)
                        listen.handlePic_Telemetry(currentTime, telem);
                
                try {
                    Thread.sleep(_piccoloPeriodMs);
                } catch (InterruptedException ex) {
                    System.out.println("Error with Thread.sleep");
                }
            } else if (_piccoloSource.equalsIgnoreCase("File")) {           
                //necessary for nmea messages to display when running using ant, not necessary but doesn't hurt anything when running in netbeans
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    System.out.println("Error with Thread.sleep");
                }
                         
                if(_replayNmeaEtdLogs!=null) {
                    String _nmeaMessage = _replayNmeaEtdLogs.getNmeaMessage();

                    if(_nmeaMessage!=null) {
                        m_GpsClient.dataDetectedEvent(new NMEAEvent("", _nmeaMessage));
                        m_GpsData = new GpsReceiverInterfaceThread.GpsReading();
                        m_GpsClient.copyLatestData(m_GpsData);                

                        if(m_GpsData!=null && m_GpsData.m_FixQuality!=GpsReceiverInterfaceThread.GpsReading.FixQuality.INVALID) {
                            Pic_Telemetry telem = new Pic_Telemetry();
                            telem.Lat = m_GpsData.m_LatDecDeg;
                            telem.Lon = m_GpsData.m_LonDecDeg;
                            telem.AltWGS84 = m_GpsData.m_GeoidHeightAboveWGS84Meters;
                            telem.AltMSL = m_GpsData.m_AltitudeMSLMeters;
                            telem.rawMessage = m_GpsData.m_rawMessage;

                            for (Pic_TelemetryListener listen : _telemListeners)
                                listen.handlePic_Telemetry(System.currentTimeMillis(), telem);
                        }
                    }
                    
                }
            } else {
                System.out.println("Piccolo.source should be Serial, Sim, or File");
            }
        }
    }
}
