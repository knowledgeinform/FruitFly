/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.nstd.piccolo.Pic_Interface;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.util.XCommSerialPort;
import java.nio.ByteBuffer;

/**
 *
 * @author humphjc1
 */
public class PiccoloSimThread extends Thread
{        
    private XCommSerialPort  m_serialPort;
    private double latitude_deg;
    private double longitude_deg;
    private double altitudeMSL_m;
    private double heading_rad;
    private double pitch_rad;
    private double roll_rad;
    private double windSpeed_mps;
    private double airSpeed_mps;
    private double windBlowingToDirection_rad;
    private double latitude_deg_next;
    private double longitude_deg_next;
    private double altitudeMSL_m_next;
    private double heading_rad_next;
    private double pitch_rad_next;
    private double roll_rad_next;
    private double windSpeed_mps_next;
    private double airSpeed_mps_next;
    private double windBlowingToDirection_rad_next;
    private byte[] sendBuffer = new byte[256];
    private ByteBuffer sendByteBuffer = ByteBuffer.wrap(sendBuffer);
    final static private int GPS_ROLLOVER_CORRECTION_SEC = 315964809;
    final static private int LEAP_SECONDS = 34;
    final static private int SECONDS_PER_WEEK = 604800;
    
    private String m_piccoloSimComPortName;
    private int m_piccoloSimSerialBaudRate;
    private double m_piccoloSimAltitudeOffset_m;
    private final Object m_positionLock = new Object();
    


    public PiccoloSimThread(String comPort, int baudRate, double altOffset_m)
    {
        this.setName ("WACS-PiccoloSimThread");
        try
        {
            m_piccoloSimComPortName = comPort;
            m_piccoloSimSerialBaudRate = baudRate;
            m_piccoloSimAltitudeOffset_m = altOffset_m;
            m_serialPort = new XCommSerialPort(m_piccoloSimComPortName, m_piccoloSimSerialBaudRate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTelemetry (double latitude_deg_next, double longitude_deg_next, double altitudeMSL_m_next,
                                double heading_rad_next, double pitch_rad_next, double roll_rad_next, 
                                double windSpeed_mps_next, double airSpeed_mps_next, double windBlowingToDirection_rad_next)
    {
        synchronized (m_positionLock)
        {
            this.latitude_deg_next = latitude_deg_next;
            this.longitude_deg_next = longitude_deg_next;
            this.altitudeMSL_m_next = altitudeMSL_m_next;
            this.heading_rad_next = heading_rad_next;
            this.pitch_rad_next = pitch_rad_next;
            this.roll_rad_next = roll_rad_next;
            this.windSpeed_mps_next = windSpeed_mps_next + 0.5; //adding an offset here so that the internal piccolo wind will be different than the external autopilot wind.
            this.airSpeed_mps_next = airSpeed_mps_next;
            this.windBlowingToDirection_rad_next = windBlowingToDirection_rad_next + 0.17; //adding an offset here so that the internal piccolo wind will be different than the external autopilot wind.
        }
    }
    
    public void run()
    {
        try
        {
           while (true)
            {
                synchronized (m_positionLock)
                {
                    latitude_deg = latitude_deg_next;
                    longitude_deg = longitude_deg_next;
                    altitudeMSL_m = altitudeMSL_m_next;
                    heading_rad = heading_rad_next;
                    pitch_rad = 0;
                    roll_rad = roll_rad_next;
                    windSpeed_mps = windSpeed_mps_next;
                    airSpeed_mps = airSpeed_mps_next;
                    windBlowingToDirection_rad = windBlowingToDirection_rad_next;
                }

                int payloadSize_bytes = 84;
                int innerPacketSize_bytes = payloadSize_bytes + 6;
                int outerPacketSize_bytes = innerPacketSize_bytes + 16;
                sendByteBuffer.clear();
                sendByteBuffer.put((byte)0x5A); //Outer sync bytes
                sendByteBuffer.put((byte)0xA5);
                sendByteBuffer.putShort((short)0xFFFF); //Dest
                sendByteBuffer.putShort((short)1); //Source
                sendByteBuffer.putShort((short)0); //Sequence
                sendByteBuffer.putShort((short)0); //Ack
                sendByteBuffer.put((byte)3); //Autopilot stream ID
                sendByteBuffer.put((byte)0); //Flags
                sendByteBuffer.put((byte)(innerPacketSize_bytes));
                byte headerChecksum = 0;
                for (int i = 0; i < 13; ++i)
                {
                    headerChecksum ^= sendBuffer[i];
                }
                sendByteBuffer.put(headerChecksum);

                sendByteBuffer.put((byte)0xA0); //Inner sync bytes
                sendByteBuffer.put((byte)0x05);
                sendByteBuffer.put((byte)69); //Hi-res telemetry message ID                    
                sendByteBuffer.put((byte)payloadSize_bytes); //Payload size

                final double trueAltitudeAGL_m = altitudeMSL_m - DtedGlobalMap.getDted().getJlibAltitude(latitude_deg, longitude_deg).getDoubleValue(Length.METERS);
                final double reportedAltitudeAGL_m = trueAltitudeAGL_m + m_piccoloSimAltitudeOffset_m;
                if (reportedAltitudeAGL_m > 655)
                    sendByteBuffer.put((byte)0xFD); //Data flags, with an agl bit set clear
                else
                    sendByteBuffer.put((byte)0xFF); //Data flags

                sendByteBuffer.put((byte)0); //Num actuators
                sendByteBuffer.putShort((short)0); //Limits
                sendByteBuffer.putInt(0); //Time since reset
                sendByteBuffer.putInt((int)(latitude_deg * 3600000));
                sendByteBuffer.putInt((int)(longitude_deg * 3600000));
                double altWgs84_m = MathUtils.convertMslToAltAbvEllip(latitude_deg, longitude_deg, altitudeMSL_m);
                int altitude_pic = (int)((altWgs84_m + 1000) * 100);
                sendByteBuffer.put((byte)((altitude_pic & 0xFF0000) >> 16));
                sendByteBuffer.put((byte)((altitude_pic & 0xFF00) >> 8));
                sendByteBuffer.put((byte)(altitude_pic & 0xFF));
                sendByteBuffer.put((byte)0); //reserved
                sendByteBuffer.putShort((short)0); //Satellite info
                sendByteBuffer.putShort((short)0); //groundspeed north
                sendByteBuffer.putShort((short)0); //groundspeed east
                sendByteBuffer.putShort((short)0); //groundspeed down
                sendByteBuffer.put((byte)0xC0); //Status bytes
                sendByteBuffer.put((byte)0);
                int gpsSeconds = (int)((System.currentTimeMillis() / 1000) - GPS_ROLLOVER_CORRECTION_SEC - LEAP_SECONDS);
                int gpsWeekNum = gpsSeconds / SECONDS_PER_WEEK;
                int gpsTimeOfWeek_ms = (gpsSeconds - (gpsSeconds / SECONDS_PER_WEEK)) * 1000;
                sendByteBuffer.putShort((short)gpsWeekNum);
                sendByteBuffer.putInt(gpsTimeOfWeek_ms);
                sendByteBuffer.putShort((short)(roll_rad * 10000));
                sendByteBuffer.putShort((short)(pitch_rad * 10000));
                sendByteBuffer.putShort((short)(heading_rad * 10000));
                sendByteBuffer.putShort((short)0); //baro altitude above GPS altitude
                sendByteBuffer.putShort((short)(Math.cos(windBlowingToDirection_rad) * windSpeed_mps * 100)); //wind from south
                sendByteBuffer.putShort((short)(Math.sin(windBlowingToDirection_rad) * windSpeed_mps * 100)); //wind from west
                sendByteBuffer.putShort((short)0); //left RPM
                sendByteBuffer.putShort((short)0); //right RPM
                sendByteBuffer.put((byte)0); //density ratio
                sendByteBuffer.put((byte)0); //outside air temp
                sendByteBuffer.putShort((short)(airSpeed_mps * 100 + 2000));
                sendByteBuffer.putShort((short)0); //static pressure
                sendByteBuffer.putShort((short)0); //roll rate
                sendByteBuffer.putShort((short)0); //pitch rate
                sendByteBuffer.putShort((short)0); //yaw rate
                sendByteBuffer.putShort((short)0); //X acceleration
                sendByteBuffer.putShort((short)0); //Y acceleration
                sendByteBuffer.putShort((short)0); //Z acceleration
                sendByteBuffer.putShort((short)0); //X magnetic field
                sendByteBuffer.putShort((short)0); //Y magnetic field
                sendByteBuffer.putShort((short)0); //Z magnetic field
                sendByteBuffer.putShort((short)(heading_rad * 10000)); //compass heading
                sendByteBuffer.putShort((short)(reportedAltitudeAGL_m * 100));
                sendByteBuffer.putShort((short)0); //fuel flow
                sendByteBuffer.putShort((short)0); //fuel remaining

                // Inner CRC
                final int innerCRC = Pic_Interface.CRC16(sendBuffer, 14, innerPacketSize_bytes - 2);
                sendByteBuffer.putShort((short)innerCRC);

                final int outerCRC = Pic_Interface.CRC16(sendBuffer, outerPacketSize_bytes - 2);
                sendByteBuffer.putShort((short)outerCRC);

                m_serialPort.sendBytes(sendBuffer, sendByteBuffer.position());

                Thread.sleep(20);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
