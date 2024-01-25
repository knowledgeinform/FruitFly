/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.piccolo.Pic_RawStreamListener;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.piccolo.Pic_TelemetryListener;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class PiccoloSimulator extends Thread implements Pic_TelemetryListener, Pic_RawStreamListener
{
    private static Pic_Interface _groundStationInterface;
    
    private double m_updateRateHz;
    private long m_updatePeriod_ms;
    private long m_lastUpdateTime_ms;
    
    private int m_pccPort;
    private String m_pccIpAddr;
    private int m_pccPlaneNum;
    
    private int m_forwardingPort;
    private ServerSocket m_SocketServer;
    private PiccoloSimThread m_PicSimThread;
    private double m_piccoloSimAltitudeOffset_m;
    private String m_piccoloSimComPortName;    
    private int m_piccoloSimSerialBaudRate;
    private byte m_CommandByteBuffer[] = new byte [10000];
    
    
    ConcurrentLinkedQueue<Pic_Telemetry> m_PicTelemBuffer = new ConcurrentLinkedQueue<Pic_Telemetry>();
    ConcurrentLinkedQueue<byte[]> m_RawStreamBuffer = new ConcurrentLinkedQueue<byte[]>();
    ConcurrentLinkedQueue<Integer> m_RawStreamSizeBuffer = new ConcurrentLinkedQueue<Integer>();
    
    
    

    public PiccoloSimulator() 
    {
        m_pccPort = Config.getConfig().getPropertyAsInteger("PiccoloSimulator.PCC.port", 2000);
        m_pccIpAddr = Config.getConfig().getProperty("PiccoloSimulator.PCC.ipaddr", "176.16.2.180");
        m_pccPlaneNum = Config.getConfig().getPropertyAsInteger("PiccoloSimulator.PCC.planenum", 1);
        m_forwardingPort = Config.getConfig().getPropertyAsInteger("PiccoloSimulator.Forwarding.port", 2000);
        m_updateRateHz = Config.getConfig().getPropertyAsDouble("PiccoloSimulator.updateRateHz");
        m_piccoloSimAltitudeOffset_m = Config.getConfig().getPropertyAsDouble("PiccoloSimulator.piccoloSimAltitudeOffset_m");
        m_piccoloSimComPortName = Config.getConfig().getProperty("PiccoloSimulator.piccoloSimOutputSerialPort");
        m_piccoloSimSerialBaudRate = Config.getConfig().getPropertyAsInteger("PiccoloSimulator.piccoloSimSerialBaudRate");
        
        m_updatePeriod_ms = (long)(1000 / m_updateRateHz);
        m_lastUpdateTime_ms = System.currentTimeMillis();
        
        _groundStationInterface = new Pic_Interface(m_pccPlaneNum, m_pccIpAddr, m_pccPort, true);
        _groundStationInterface.addPicTelemetryListener(this);
        _groundStationInterface.addPicRawStreamListener(this);
        Thread pt = (new Thread(_groundStationInterface));
        pt.setName ("WACS-PiccoloGSInterface");
        pt.start();
        
        try
        {
            m_SocketServer = new ServerSocket(m_forwardingPort);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            m_SocketServer = null;
        }
        
        m_PicSimThread = new PiccoloSimThread(m_piccoloSimComPortName, m_piccoloSimSerialBaudRate, m_piccoloSimAltitudeOffset_m);
        m_PicSimThread.setDaemon(true);
        m_PicSimThread.start();

    }

    
    @Override
    public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem)
    {
        System.out.println("Pic Telemetry => ");
        System.out.println("\t(lat,lon,altWGS84) = (" + telem.Lat + "," + telem.Lon + "," + telem.AltWGS84 + ")");
        System.out.println("\t(r,p,y,comp) = (" + telem.Roll + "," + telem.Pitch + "," + telem.Yaw + "," + telem.TrueHeading + ")");
        System.out.println("\t(WindSouth,WindWest,IAS) = (" + telem.WindSouth + "," + telem.WindWest + "," + telem.IndAirSpeed_mps + ")");
        System.out.println("\t(vnorth,vest,vdown) = (" + telem.VelNorth + "," + telem.VelEast + "," + telem.VelDown + ")");
        System.out.println("\t(PDOP,Status) = (" + telem.PDOP + "," + telem.GPS_Status + ")\n\n");
        
        m_PicTelemBuffer.add(telem);
    }
    
    @Override
    public void handlePic_RawStream(byte[] bytesRead, int numBytesRead) 
    {
        System.out.println ("Received " + numBytesRead + " bytes through Piccolo simulator stream");
        
        m_RawStreamBuffer.add(bytesRead);
        m_RawStreamSizeBuffer.add(numBytesRead);
    }
    
    public void run()
    {
        while (true)
        {
            Socket socket = null;
            try
            {
                socket = m_SocketServer.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();

                try { 
                    Thread.sleep (1000);
                } catch (InterruptedException ex) { ex.printStackTrace();}
                
                continue;
            }
            
            
            try
            {        
                while (true)
                {
                    long startTime_ms = System.currentTimeMillis();
                    double timeSinceLastUpdate_sec = ((double)(startTime_ms - m_lastUpdateTime_ms)) / 1000.0;
                    m_lastUpdateTime_ms = startTime_ms;

                    while (m_RawStreamSizeBuffer.size() > 0)
                    {
                        int size = m_RawStreamSizeBuffer.poll();
                        byte bytes[] = m_RawStreamBuffer.poll();
                        
                        socket.getOutputStream().write(bytes, 0, size);
                    }
                    
                    int availCommandBytes = socket.getInputStream().available();
                    if (availCommandBytes > 0)
                    {
                        if (availCommandBytes > m_CommandByteBuffer.length)
                            m_CommandByteBuffer = new byte [availCommandBytes*2];
                        
                        //command bytes to send to PCC from WACS
                        int commandBytes = socket.getInputStream().read(m_CommandByteBuffer);
                        _groundStationInterface.sendRawCommandBytes (m_CommandByteBuffer, commandBytes);
                    }
                    
                    while (m_PicTelemBuffer.size() > 0)
                    {
                        Pic_Telemetry telem = m_PicTelemBuffer.poll();
                        double windSpeed_mps = Math.sqrt (telem.WindSouth*telem.WindSouth + telem.WindWest*telem.WindWest);
                        double windBlowToDir_rad = Math.atan2(telem.WindWest, telem.WindSouth);
                        m_PicSimThread.setTelemetry(telem.Lat, telem.Lon, telem.AltMSL, telem.Yaw*MathConstants.DEG2RAD, telem.Pitch*MathConstants.DEG2RAD, telem.Roll*MathConstants.DEG2RAD, windSpeed_mps, telem.IndAirSpeed_mps, windBlowToDir_rad);
                    }

                    long loopDuration_ms = System.currentTimeMillis() - startTime_ms;

                    if (loopDuration_ms < m_updatePeriod_ms)
                    {
                        Thread.sleep(m_updatePeriod_ms - loopDuration_ms);
                    }
                    else
                    {
                        Thread.sleep(1);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    
    public static void main (String args[])
    {
        PiccoloSimulator simulator = new PiccoloSimulator();
        simulator.run();
    }
}
