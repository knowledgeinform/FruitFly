/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.CommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.TelemetryMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface.ShadowCommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowAutopilotInterface.ShadowTelemetryMessage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author humphjc1
 */
public class ShadowGCSSimulator extends ShadowSimulator
{
    
    public ShadowGCSSimulator()
    {
        this(false);
    }

    public ShadowGCSSimulator(final boolean useAPIOnly)
    {
        ShadowTelemetryMessage telemMessage = (ShadowTelemetryMessage)m_telemetryMessage;
                
        telemMessage.knobsModeCommand = 1;
        telemMessage.insMode = 2;
        telemMessage.longitude_rad = m_longitude.getDoubleValue(Angle.RADIANS);
        telemMessage.latitude_rad = m_latitude.getDoubleValue(Angle.RADIANS);
        telemMessage.groundSpeedNorth_mps = m_groundSpeed_mps * Math.cos(m_heading_rad);
        telemMessage.groundSpeedEast_mps = m_groundSpeed_mps * Math.sin(m_heading_rad);
        telemMessage.trueHeading_rad = m_heading_rad;
        telemMessage.magneticDeclination_rad = 0;
        telemMessage.roll_rad = m_roll_rad;
        telemMessage.pitch_rad = 0;
        telemMessage.gpsAltitude_m = m_altitudeMSL_m;
        telemMessage.barometricAltitudeMsl_m = m_altitudeMSL_m;
        telemMessage.verticalSpeed_mps = 0;
        telemMessage.verticalAcceleration_mps2 = 0;
        telemMessage.indicatedAirspeed_mps = m_airSpeed_mps;
        telemMessage.trueAirspeed_mps = m_airSpeed_mps;
        telemMessage.airTemp_k = 0;
        telemMessage.gpsPositionalDilutionOfPrecision = 0;
        telemMessage.fuelLevel_l = 0;
        telemMessage.gpsTimeOfWeek_s = 0;
        telemMessage.gpsWeekNumber = 0;
        telemMessage.flapsPosition = 0;
        
        if (!useAPIOnly)
        {
            (new ShadowSimulator.LatencyEmulationThread()).start();
            (new ReceiveThread()).start();
            (new SendThread()).start();
        }
    }
    
    @Override
    protected TelemetryMessage getNewTelemetryMessage ()
    {
        return new ShadowTelemetryMessage();
    }
            
            
    @Override
    protected void fillTelemetryMessageDetails (TelemetryMessage telemetryMessage)
    {
        ShadowTelemetryMessage telemMessage = (ShadowTelemetryMessage)telemetryMessage;
        telemMessage.gpsAltitude_m = telemMessage.barometricAltitudeMsl_m;
    }
            
    
    private class ReceiveThread extends Thread
    {
        public ReceiveThread ()
        {
            this.setName ("WACS-ShadowSimReceiveThread");
        }
        
        @Override
        public void run()
        {
            try
            {
                byte[] receiveData = new byte[1024];
                ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveData);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                DatagramSocket receiveSocket = new DatagramSocket(m_receivePort);

                while (true)
                {
                    receiveSocket.receive(packet);
                    int calculatedChecksum = calcChecksum(receiveData, packet.getLength() - 4);
                    int receivedChecksum = receiveBuffer.getInt(packet.getLength() - 4);

                    if (calculatedChecksum == receivedChecksum)
                    {
                        receiveBuffer.rewind();

                        synchronized (m_commandMessageLock)
                        {
                            ShadowCommandMessage commandMessage = new ShadowCommandMessage();
                            commandMessage.timestamp_ms = System.currentTimeMillis();
                            commandMessage.messageLength = receiveBuffer.getInt();
                            commandMessage.messageVersion = receiveBuffer.getInt();
                            commandMessage.busNumber = receiveBuffer.getInt();
                            commandMessage.knobsModeReport = receiveBuffer.getInt();
                            commandMessage.altitudeCommand_m = receiveBuffer.getDouble();
                            commandMessage.rollCommand_rad = receiveBuffer.getDouble();
                            commandMessage.airspeedCommand_mps = receiveBuffer.getDouble();
                            commandMessage.throttlePercentageCommand = receiveBuffer.getInt();
                            commandMessage.throttleEnable = receiveBuffer.getInt();

                            if (m_incomingLatencyQueue.size() == INCOMING_LATENCY_QUEUE_SIZE)
                            {
                                // Throw away a command too keep the queue from overflowing
                                m_incomingLatencyQueue.poll();
                            }

                            m_incomingLatencyQueue.add(commandMessage);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread
    {
        public SendThread ()
        {
            this.setName ("WACS-ShadowSimSendThread");
        }
        
        @Override
        public void run()
        {
            try
            {               
                byte[] sendData = new byte[176];
                ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
                InetAddress remoteIPAddress = InetAddress.getByName(m_remoteHostname);
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, remoteIPAddress, m_remotePort);
                DatagramSocket socket = new DatagramSocket();

                while (true)
                {
                    long startTime_ms = System.currentTimeMillis();

                    sendBuffer.rewind();

                    synchronized (m_telemetryMessageLock)
                    {
                        if (m_telemetryMessage != null)
                        {
                            ShadowTelemetryMessage telemMessage = (ShadowTelemetryMessage)m_telemetryMessage;
                            
                            sendBuffer.putInt(telemMessage.messageLength);
                            sendBuffer.putInt(telemMessage.messageVersion);
                            sendBuffer.putInt(telemMessage.busNumber);
                            sendBuffer.putInt(telemMessage.knobsModeCommand);
                            sendBuffer.putInt(telemMessage.insMode);
                            sendBuffer.putDouble(telemMessage.longitude_rad);
                            sendBuffer.putDouble(telemMessage.latitude_rad);
                            sendBuffer.putDouble(telemMessage.groundSpeedNorth_mps);
                            sendBuffer.putDouble(telemMessage.groundSpeedEast_mps);
                            sendBuffer.putDouble(telemMessage.trueHeading_rad);
                            sendBuffer.putDouble(telemMessage.magneticDeclination_rad);
                            sendBuffer.putDouble(telemMessage.roll_rad);
                            sendBuffer.putDouble(telemMessage.pitch_rad);
                            sendBuffer.putDouble(telemMessage.gpsAltitude_m);
                            sendBuffer.putDouble(telemMessage.barometricAltitudeMsl_m);
                            sendBuffer.putDouble(telemMessage.verticalSpeed_mps);
                            sendBuffer.putDouble(telemMessage.verticalAcceleration_mps2);
                            sendBuffer.putDouble(telemMessage.indicatedAirspeed_mps);
                            sendBuffer.putDouble(telemMessage.trueAirspeed_mps);
                            sendBuffer.putDouble(telemMessage.airTemp_k);
                            sendBuffer.putDouble(telemMessage.gpsPositionalDilutionOfPrecision);
                            sendBuffer.putDouble(telemMessage.fuelLevel_l);
                            sendBuffer.putDouble(telemMessage.gpsTimeOfWeek_s);
                            sendBuffer.putInt(telemMessage.gpsWeekNumber);
                            sendBuffer.putInt(telemMessage.flapsPosition);
                            sendBuffer.putInt(calcChecksum(sendData, sendBuffer.position()));
                        }
                    }

                    socket.send(packet);


                    long loopDuration_ms = System.currentTimeMillis() - startTime_ms;

                    if (loopDuration_ms < m_sendPeriod_ms)
                    {
                        Thread.sleep(m_sendPeriod_ms - loopDuration_ms);
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
    
    static public void main(String[] args)
    {
        Thread.currentThread().setName ("WACS-ShadowSimulator");
        ShadowGCSSimulator simulator = new ShadowGCSSimulator(false);
        simulator.run();
    }

}
