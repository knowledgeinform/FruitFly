/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.autopilot;

import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.nstd.swarm.autopilot.KnobsAutopilotInterface.TelemetryMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface.ShadowCommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface.ShadowTelemetryMessage;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.wacs.PodSatCommMessageAribtrator;
import edu.jhuapl.nstd.swarm.wacs.SatCommMessageArbitrator;
import edu.jhuapl.nstd.swarm.wacs.WacsMode;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author humphjc1
 */
public class ShadowWAVSMSimulator extends ShadowSimulator implements ShadowWavsmSimChangeListener
{
    ShadowWAVSMSimulatorDisplay m_DebugDisplay;
    
    double m_LoiterLatRad = 0.701342466;
    double m_LoiterLonRad = -1.97593807;
    double m_LoiterRadiusM = 1200;
    double m_LoiterAltMslM = 1700;
    boolean m_LoiterCWDir = false;
    WacsMode m_WacsMode = WacsMode.LOITER;
    boolean m_LoiterValid = true;
    boolean m_StrikeValid = true;
    double m_StrikeLatRad = 0.7008552;
    double m_StrikeLonRad = -1.97518456;
    double m_StrikeAltMslM = 1316;
    boolean m_TcdlActive = true;
    boolean m_SatCommThrottle = false;
    /*double m_LoiterLatRad = 0.66688;
    double m_LoiterLonRad = 2.220905;
    double m_LoiterRadiusM = 750;
    double m_LoiterAltMslM = 487;
    boolean m_LoiterCWDir = false;
    WacsMode m_WacsMode = WacsMode.LOITER;
    boolean m_LoiterValid = true;
    boolean m_StrikeValid = true;
    double m_StrikeLatRad = 0.666384;
    double m_StrikeLonRad = 2.220558;
    double m_StrikeAltMslM = 175;
    boolean m_TcdlActive = true;
    boolean m_SatCommThrottle = false;*/
    
    protected long m_sendDownlinkPeriod_ms;
    
    protected String m_PodSendMulticastString;
    protected int m_PodSendPort;
    protected String m_PodRecvMulticastString;
    protected int m_PodRecvPort;
    protected String m_SatCommSendMulticastString;
    protected int m_SatCommSendPort;
    protected String m_SatCommRecvMulticastString;
    protected int m_SatCommRecvPort;
    protected String m_RemoteSendMulticastString;
    protected int m_RemoteSendPort;
    protected String m_RemoteRecvMulticastString;
    protected int m_RemoteRecvPort;
    
    
    SendThread podSendThread;
    SendThread satCommSendThread;
    SendThread wgvsmSendThread;
    RecvThread podRecvThread;
    RecvThread satCommRecvThread;
    RecvThread remoteRecvThread;
    ParseCommandThread parseCommandThread;
    FormMessageThread formMessageThread;
    protected long m_LastPodMessageTimestampMs;
    
    public ShadowWAVSMSimulator()
    {
        this(false);
    }

    public ShadowWAVSMSimulator(final boolean useAPIOnly)
    {
        ShadowTelemetryMessage telemMessage = (ShadowTelemetryMessage)m_telemetryMessage;
                
        telemMessage.timestamp_ms = System.currentTimeMillis();
        telemMessage.m_CommandedAirspeedMps = m_airSpeed_mps;
        telemMessage.m_LoiterLatitudeRad = m_LoiterLatRad;
        telemMessage.m_LoiterLongitudeRad = m_LoiterLonRad;
        telemMessage.m_LoiterRadiusM = m_LoiterRadiusM;
        telemMessage.m_LoiterAltitudeMslM = m_LoiterAltMslM;
        telemMessage.m_LoiterCWDir = m_LoiterCWDir;
        telemMessage.m_CommandedGimbalMode = 3;
        telemMessage.m_CommandedWacsMode = m_WacsMode;
        telemMessage.m_LoiterValid = m_LoiterValid;
        telemMessage.m_StrikeLatitudeRad = m_StrikeLatRad;
        telemMessage.m_StrikeLongitudeRad = m_StrikeLonRad;
        telemMessage.m_StrikeAltitudeMslM = m_StrikeAltMslM;
        telemMessage.m_StrikeValid = m_StrikeValid;
        
        telemMessage.longitude_rad = m_longitude.getDoubleValue(Angle.RADIANS);
        telemMessage.latitude_rad = m_latitude.getDoubleValue(Angle.RADIANS);
        telemMessage.barometricAltitudeMsl_m = m_altitudeMSL_m;
        telemMessage.roll_rad = m_roll_rad;
        telemMessage.pitch_rad = 0;
        telemMessage.trueHeading_rad = m_heading_rad;
        telemMessage.trueAirspeed_mps = m_airSpeed_mps;
        telemMessage.indicatedAirspeed_mps = m_airSpeed_mps;
        telemMessage.groundSpeedNorth_mps = m_groundSpeed_mps * Math.cos(m_heading_rad);
        telemMessage.groundSpeedEast_mps = m_groundSpeed_mps * Math.sin(m_heading_rad);
        telemMessage.m_VerticalVelocityMps = 0;
        telemMessage.m_TCDLLinkActive = m_TcdlActive;
        telemMessage.m_SatCommThrottling = m_SatCommThrottle;
        
        m_sendDownlinkPeriod_ms = Config.getConfig().getPropertyAsInteger("ShadowSimulator.sendDownlinkPeriod.Ms", 5000);
        m_PodSendMulticastString = Config.getConfig().getProperty("ShadowSimulator.PodSendAddress", "233.1.3.3");
        m_PodSendPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.PodSendPort", 57190);
        m_PodRecvMulticastString = Config.getConfig().getProperty("ShadowSimulator.PodRecvAddress", "233.1.3.3");
        m_PodRecvPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.PodRecvPort", 57191);
        m_SatCommSendMulticastString = Config.getConfig().getProperty("ShadowSimulator.SatCommSendAddress", "233.1.3.3");
        m_SatCommSendPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.SatCommSendPort", 57190);
        m_SatCommRecvMulticastString = Config.getConfig().getProperty("ShadowSimulator.SatCommRecvAddress", "233.1.3.3");
        m_SatCommRecvPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.SatCommRecvPort", 57191);
        m_RemoteSendMulticastString = Config.getConfig().getProperty("ShadowSimulator.RemoteSendAddress", "233.1.3.2");
        m_RemoteSendPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.RemoteSendPort", 56190);
        m_RemoteRecvMulticastString = Config.getConfig().getProperty("ShadowSimulator.RemoteRecvAddress", "233.1.3.1");
        m_RemoteRecvPort = Config.getConfig().getPropertyAsInteger("ShadowSimulator.RemoteRecvPort", 56191);
        m_LastPodMessageTimestampMs = -1;
        
        if (!useAPIOnly)
        {
            (new ShadowSimulator.LatencyEmulationThread()).start();
            
            parseCommandThread = new ParseCommandThread();
            parseCommandThread.start();
            
            /*localSendThread = new SendThread("233.1.3.3", 57190);
            localSendThread.start();
            wgvsmSendThread = new SendThread("233.1.3.2", 56190);
            wgvsmSendThread.start();

            localRecvThread = new RecvThread("233.1.3.3", 57191);
            localRecvThread.start();
            remoteRecvThread = new RecvThread("233.1.3.1", 56191);
            remoteRecvThread.start();*/
            
            podSendThread = new SendThread(m_PodSendMulticastString, m_PodSendPort);
            podSendThread.start();
            satCommSendThread = new SendThread(m_SatCommSendMulticastString, m_SatCommSendPort);
            satCommSendThread.start();
            wgvsmSendThread = new SendThread(m_RemoteSendMulticastString, m_RemoteSendPort);
            wgvsmSendThread.start();

            podRecvThread = new RecvThread(m_PodRecvMulticastString, m_PodRecvPort);
            podRecvThread.start();
            satCommRecvThread = new RecvThread(m_SatCommRecvMulticastString, m_SatCommRecvPort);
            satCommRecvThread.start();
            remoteRecvThread = new RecvThread(m_RemoteRecvMulticastString, m_RemoteRecvPort);
            remoteRecvThread.start();
        
            formMessageThread = new FormMessageThread ();
            formMessageThread.start();
        }
        
        m_DebugDisplay = new ShadowWAVSMSimulatorDisplay(this);
        m_DebugDisplay.setVisible(true);
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
        
        if (m_commandMessage != null)
        {
            ShadowCommandMessage commandMessage = (ShadowCommandMessage)m_commandMessage;
            telemMessage.m_CommandedAirspeedMps = commandMessage.airspeedCommand_mps;
        }
        
        telemMessage.m_LoiterLatitudeRad = m_LoiterLatRad;
        telemMessage.m_LoiterLongitudeRad = m_LoiterLonRad;
        telemMessage.m_LoiterRadiusM = m_LoiterRadiusM;
        telemMessage.m_LoiterAltitudeMslM = m_LoiterAltMslM;
        telemMessage.m_LoiterCWDir = m_LoiterCWDir;
        telemMessage.m_CommandedGimbalMode = 3;
        telemMessage.m_CommandedWacsMode = m_WacsMode;
        telemMessage.m_LoiterValid = m_LoiterValid;
        telemMessage.m_StrikeLatitudeRad = m_StrikeLatRad;
        telemMessage.m_StrikeLongitudeRad = m_StrikeLonRad;
        telemMessage.m_StrikeAltitudeMslM = m_StrikeAltMslM;
        telemMessage.m_StrikeValid = m_StrikeValid;
        telemMessage.m_VerticalVelocityMps = 0;
        telemMessage.m_TCDLLinkActive = m_TcdlActive;
        telemMessage.m_SatCommThrottling = m_SatCommThrottle;
    }

    @Override
    public void setLoiterLatRad(double newVal) 
    {
        m_LoiterLatRad = newVal;
    }
    
    @Override
    public void setLoiterLonRad(double newVal) 
    {
        m_LoiterLonRad = newVal;
    }
    
    @Override
    public void setLoiterRadiusM(double newVal) 
    {
        m_LoiterRadiusM = newVal;
    }
    
    @Override
    public void setLoiterAltMslM(double newVal) 
    {
        m_LoiterAltMslM = newVal;
    }
    
    @Override
    public void setLoiterCWDir(boolean newVal) 
    {
        m_LoiterCWDir = newVal;
    }
    
    @Override
    public void setWacsMode(WacsMode mode) 
    {
        m_WacsMode = mode;
    }
    
    @Override
    public void setLoiterValid(boolean newVal) 
    {
        m_LoiterValid = newVal;
    }
    
    @Override
    public void setStrikeValid(boolean newVal) 
    {
        m_StrikeValid = newVal;
    }
    
    @Override
    public void setStrikeLatRad(double newVal) 
    {
        m_StrikeLatRad = newVal;
    }
    
    @Override
    public void setStrikeLonRad(double newVal) 
    {
        m_StrikeLonRad = newVal;
    }
    
    @Override
    public void setStrikeAltMslM(double newVal) 
    {
        m_StrikeAltMslM = newVal;
    }
    
    @Override
    public void setTcdlActive(boolean newVal) 
    {
        m_TcdlActive = newVal;
    }
    
    @Override
    public void setSatCommThrottle(boolean newVal) 
    {
        m_SatCommThrottle = newVal;
    }
    
    private class ParseCommandThread extends Thread
    {
        ConcurrentLinkedQueue<DatagramPacket> m_InputQueue = new ConcurrentLinkedQueue<DatagramPacket>();
        
        public ParseCommandThread ()
        {
            this.setName ("WACS-ShadowSimParseCommandThread");
        }
        
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    DatagramPacket packet = m_InputQueue.poll();
                    if (packet != null)
                    {
                        byte []receiveData = packet.getData();
                        ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveData);
                    
                        int messageType = SatCommMessageArbitrator.verifyPacketHeader (receiveData, packet.getLength());
                        boolean crcGood = SatCommMessageArbitrator.verifyPacketCrc (receiveData, packet.getLength());

                        if (messageType == SatCommMessageArbitrator.MSGTYPE_INVALID || !crcGood)
                        {
                            //Packet got mangled from original version, just throw it away
                            System.err.println ("Bad packet received and ignored!");
                            continue;
                        }

                        if (messageType == SatCommMessageArbitrator.MSGTYPE_RECEIPT)
                        {
                            if ((SatCommMessageArbitrator.getPacketDestination(receiveData)&0xFF) == 0x02)
                            {
                                SatCommMessageArbitrator.checkAndPrintReceiptMessage (receiveData, receiveBuffer);
                            }
                            else
                            {
                                //Destination field says it shouldn't have gotten here
                                System.err.println ("Receipt message received that had wrong destination set - ignored!");
                            }
                            
                        }
                        else if (messageType == SatCommMessageArbitrator.MSGTYPE_PODSTOWAVSM)
                        {
                            //Get timestamp of the packet, which will be the timestamp of the beliefs created by the message
                            long dataTimestampMs = SatCommMessageArbitrator.getPacketTimestampMs(receiveData);
                            
                            if (dataTimestampMs > m_LastPodMessageTimestampMs)
                            {
                                m_LastPodMessageTimestampMs = dataTimestampMs;
                                receiveBuffer.rewind();

                                synchronized (m_commandMessageLock)
                                {
                                    if (m_DebugDisplay != null)
                                        m_DebugDisplay.setCommandMessage ((ShadowCommandMessage)m_commandMessage);

                                    ShadowCommandMessage commandMessage = new ShadowCommandMessage();

                                    //Clear header
                                    for (int i = 0; i < 10; i ++)
                                        receiveBuffer.get();

                                    receiveBuffer.get();
                                    receiveBuffer.get();

                                    commandMessage.timestamp_ms = dataTimestampMs;
                                    commandMessage.rollCommand_rad = receiveBuffer.getFloat();
                                    commandMessage.airspeedCommand_mps = receiveBuffer.getFloat();
                                    commandMessage.altitudeCommand_m = receiveBuffer.getFloat();
                                    commandMessage.m_CurrentOrbitRadiusM = receiveBuffer.getFloat();
                                    commandMessage.m_CurrentOrbitCWDir = (receiveBuffer.get()==1);
                                    receiveBuffer.get();
                                    receiveBuffer.get();
                                    receiveBuffer.get();
                                    commandMessage.m_CurrentOrbitAltitudeMslM = receiveBuffer.getFloat();
                                    commandMessage.m_CurrentOrbitAirspeedMps = receiveBuffer.getFloat();
                                    commandMessage.m_CurrentOrbitLatitudeRad = receiveBuffer.getDouble();
                                    commandMessage.m_CurrentOrbitLongitudeRad = receiveBuffer.getDouble();
                                    commandMessage.m_GimbalElevationRad = receiveBuffer.getFloat();
                                    commandMessage.m_GimbalAzimuthRad = receiveBuffer.getFloat();
                                    commandMessage.m_LaserAltitudeAglM = receiveBuffer.getFloat();
                                    commandMessage.m_WindSpeedMps = receiveBuffer.getFloat();
                                    commandMessage.m_WindDirFromRad = receiveBuffer.getFloat();
                                    byte wacsMode = receiveBuffer.get();
                                    switch (wacsMode)
                                    {
                                        case 1:
                                            commandMessage.m_WacsMode = WacsMode.INTERCEPT;
                                            break;
                                        case 0:
                                        default:
                                            commandMessage.m_WacsMode = WacsMode.LOITER;
                                            break;
                                    }
                                    commandMessage.m_PiccoloOperational = (receiveBuffer.get()==1);
                                    commandMessage.m_TaseOperational = (receiveBuffer.get()==1);
                                    commandMessage.m_WindEstimateConverged = (receiveBuffer.get()==1);

                                    if (m_incomingLatencyQueue.size() == INCOMING_LATENCY_QUEUE_SIZE)
                                    {
                                        // Throw away a command too keep the queue from overflowing
                                        m_incomingLatencyQueue.poll();
                                    }

                                    m_incomingLatencyQueue.add(commandMessage);
                                }
                            }
                            else
                            {
                                System.out.println ("Pod command message received was not newer than last data - ignored: " + dataTimestampMs);
                            }
                        }
                        else
                        {
                            //Didn't expect this type of message on the wavsm simulator
                            System.err.println ("Unknown Message received and ignored!  Type: " + messageType);
                        }
                    }
                    else
                    {
                        Thread.sleep (10);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void addToQueue (DatagramPacket packet)
        {
            m_InputQueue.add(packet);
        }
    }

    private class FormMessageThread extends Thread
    {
        public FormMessageThread ()
        {
            this.setName ("WACS-ShadowSimFormMessageThread");
        }
        
        @Override
        public void run()
        {
            long lastSentTelemetryMessageTimeMs = -1;
            long lastSentCriticalDownlinkTimeMs = -1;
            
            while (true)
            {
                long currTimeMs = System.currentTimeMillis();
                boolean sleep = true;
                
                if (currTimeMs - lastSentTelemetryMessageTimeMs > m_sendPeriod_ms)
                {
                    formAndSendTelemetryMessage();
                    lastSentTelemetryMessageTimeMs = currTimeMs;
                    sleep = false;
                }
                if (currTimeMs - lastSentCriticalDownlinkTimeMs > m_sendDownlinkPeriod_ms)
                {
                    formAndSendCriticalDownlinkMessage();
                    lastSentCriticalDownlinkTimeMs = currTimeMs;
                    sleep = false;
                }
                
                if (sleep)
                {
                    try {
                        Thread.sleep (10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ShadowWAVSMSimulator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
        private void formAndSendTelemetryMessage()
        {
            try
            {            
                byte[] sendData = new byte[120];
                ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
                
                synchronized (m_telemetryMessageLock)
                {
                    ShadowTelemetryMessage telemMessage = (ShadowTelemetryMessage)m_telemetryMessage;
                    if (m_telemetryMessage != null)
                        telemMessage = (ShadowTelemetryMessage)m_telemetryMessage;
                    else
                        telemMessage = new ShadowTelemetryMessage();
                    
                    if (m_DebugDisplay != null)
                        m_DebugDisplay.setTelemetryMessage ((ShadowTelemetryMessage)m_telemetryMessage);

                    //Form header
                    SatCommMessageArbitrator.formMessageHeader(sendData, 1, 0, 4, 108, System.currentTimeMillis());
                    //Skip over header
                    sendBuffer.position(10);



                    sendBuffer.put((byte)0);
                    sendBuffer.put((byte)0);

                    sendBuffer.putFloat ((float)telemMessage.m_CommandedAirspeedMps);
                    sendBuffer.putDouble (telemMessage.m_LoiterLatitudeRad);
                    sendBuffer.putDouble (telemMessage.m_LoiterLongitudeRad);
                    sendBuffer.putFloat ((float)telemMessage.m_LoiterRadiusM);
                    sendBuffer.putFloat ((float)telemMessage.m_LoiterAltitudeMslM);
                    sendBuffer.put((byte)(telemMessage.m_LoiterCWDir?1:2));
                    sendBuffer.put((byte)telemMessage.m_CommandedGimbalMode);
                    if (telemMessage.m_CommandedWacsMode == WacsMode.INTERCEPT)
                        sendBuffer.put((byte)1);
                    else if (telemMessage.m_CommandedWacsMode == WacsMode.LOITER)
                        sendBuffer.put((byte)0);
                    else
                        sendBuffer.put((byte)0);
                    sendBuffer.put((byte)(telemMessage.m_LoiterValid?1:0));
                    sendBuffer.putDouble (telemMessage.m_StrikeLatitudeRad);
                    sendBuffer.putDouble (telemMessage.m_StrikeLongitudeRad);
                    sendBuffer.putFloat ((float)telemMessage.m_StrikeAltitudeMslM);
                    sendBuffer.put((byte)(telemMessage.m_StrikeValid?1:0));
                    sendBuffer.put((byte)0);
                    sendBuffer.put((byte)0);
                    sendBuffer.put((byte)0);

                    sendBuffer.putDouble (telemMessage.latitude_rad);
                    sendBuffer.putDouble (telemMessage.longitude_rad);
                    sendBuffer.putFloat ((float)telemMessage.barometricAltitudeMsl_m);
                    sendBuffer.putFloat ((float)telemMessage.roll_rad);
                    sendBuffer.putFloat ((float)telemMessage.pitch_rad);
                    sendBuffer.putFloat ((float)telemMessage.trueHeading_rad);
                    sendBuffer.putFloat ((float)telemMessage.indicatedAirspeed_mps);
                    sendBuffer.putFloat ((float)telemMessage.groundSpeedNorth_mps);
                    sendBuffer.putFloat ((float)telemMessage.groundSpeedEast_mps);
                    sendBuffer.putFloat ((float)telemMessage.m_VerticalVelocityMps);
                    sendBuffer.put((byte)(telemMessage.m_TCDLLinkActive?1:0));
                    sendBuffer.put((byte)(telemMessage.m_SatCommThrottling?1:0));

                    //Compute CRC before sending message
                    int crc = SatCommMessageArbitrator.compute2ByteCRC_CCITT (sendData, sendData.length-2);
                    sendData [sendData.length-2] = (byte)(0xFF & (crc >> 8));        //MSB-first CRC field
                    sendData [sendData.length-1] = (byte)(0xFF & (crc));           //MSB-first CRC field
                }
                
                podSendThread.queuePacket(sendData);
                satCommSendThread.queuePacket(sendData);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        private void formAndSendCriticalDownlinkMessage()
        {
            try
            {            
                byte[] sendData = new byte[72];
                ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
                
                synchronized (m_commandMessageLock)
                {
                    ShadowCommandMessage commandMessage = null;
                    if (m_commandMessage != null)
                        commandMessage = (ShadowCommandMessage)m_commandMessage;
                    else
                        commandMessage = new ShadowCommandMessage();
                        
                    //Form header
                    SatCommMessageArbitrator.formMessageHeader(sendData, 12, 0, 0, 60, System.currentTimeMillis());
                    //Skip over header
                    sendBuffer.position(10);



                    sendBuffer.putShort ((short)(m_altitudeMSL_m/MathConstants.FT2M));
                    int val = SatCommMessageArbitrator.computeLatDegAsRad3BytePacked(m_latitude.getDoubleValue (Angle.DEGREES));
                    sendBuffer.put ((byte)((val>>16) & 0xFF));
                    sendBuffer.put ((byte)((val>>8) & 0xFF));
                    sendBuffer.put ((byte)((val) & 0xFF));
                    sendBuffer.put((byte)(m_airSpeed_mps*250/77.167));
                    val = (SatCommMessageArbitrator.computeLonDegAsRad3BytePacked(m_longitude.getDoubleValue (Angle.DEGREES)));
                    sendBuffer.put ((byte)((val>>16) & 0xFF));
                    sendBuffer.put ((byte)((val>>8) & 0xFF));
                    sendBuffer.put ((byte)((val) & 0xFF));
                    sendBuffer.put ((byte)50);  //engine speed rpm
                    sendBuffer.putShort((short)SatCommMessageArbitrator.computeHeadingRadAs2BytePacked(m_heading_rad));
                    sendBuffer.putShort((short)SatCommMessageArbitrator.computeRollRadAs12BitPacked(m_roll_rad));  //ignoring bus voltage
                    sendBuffer.putShort((short)SatCommMessageArbitrator.computePitchRadAs12BitPacked(0.1));  //ignoring some values
                    sendBuffer.put ((byte)100); //engine temp
                    sendBuffer.put ((byte)110); //engine air temp
                    sendBuffer.put ((byte)80); //ota
                    sendBuffer.put ((byte)(m_groundDisplacement_m*Math.cos(m_groundDisplacementDirection_rad)*127/77.167));
                    sendBuffer.put ((byte)(m_groundDisplacement_m*Math.sin(m_groundDisplacementDirection_rad)*127/77.167));
                    sendBuffer.put ((byte)(m_ascendRate_mps*10));
                    sendBuffer.put ((byte)0); //vert accel
                    sendBuffer.put ((byte)125); //fuel rem
                    sendBuffer.put ((byte)100); //fuel rate
                    sendBuffer.put ((byte)(m_commandedAirspeed_mps*250/77.167));

                    val = (SatCommMessageArbitrator.computeLatDegAsRad3BytePacked(commandMessage.m_CurrentOrbitLatitudeRad/MathConstants.DEG2RAD));
                    sendBuffer.put ((byte)((val>>16) & 0xFF));
                    sendBuffer.put ((byte)((val>>8) & 0xFF));
                    sendBuffer.put ((byte)((val) & 0xFF));
                    if (commandMessage.m_CurrentOrbitCWDir)
                        sendBuffer.put((byte)(0));
                    else
                        sendBuffer.put((byte)(0x80));
                    val = (SatCommMessageArbitrator.computeLonDegAsRad3BytePacked(commandMessage.m_CurrentOrbitLongitudeRad/MathConstants.DEG2RAD));
                    sendBuffer.put ((byte)((val>>16) & 0xFF));
                    sendBuffer.put ((byte)((val>>8) & 0xFF));
                    sendBuffer.put ((byte)((val) & 0xFF));
                    sendBuffer.put ((byte)(Math.round(commandMessage.m_CurrentOrbitRadiusM/20)));
                    sendBuffer.putShort ((short)(commandMessage.m_CurrentOrbitAltitudeMslM/MathConstants.FT2M));

                    sendBuffer.putShort ((byte)0); //gimbal azimuth
                    sendBuffer.putShort ((byte)0); //gimbal elevation

                    sendBuffer.putShort ((short)(commandMessage.m_LaserAltitudeAglM/MathConstants.FT2M));
                    sendBuffer.put ((byte)(commandMessage.m_WindSpeedMps*250/77.167));
                    sendBuffer.put ((byte)(commandMessage.m_WindDirFromRad/2/MathConstants.DEG2RAD));
                    sendBuffer.put ((byte)(240)); //Pressure
                    sendBuffer.put ((byte)(242)); //Pressure
                    sendBuffer.putInt ((int)(commandMessage.timestamp_ms/1000));
                    sendBuffer.putShort ((short)(commandMessage.timestamp_ms%1000));
                    sendBuffer.put ((byte)(0x8F));
                    if (commandMessage.m_WacsMode.equals (WacsMode.INTERCEPT))
                        sendBuffer.put ((byte)1);
                    else //if (commandMessage.m_WacsMode.equals (WacsMode.LOITER))
                        sendBuffer.put ((byte)0);
                    sendBuffer.put ((byte)(
                            (commandMessage.m_PiccoloOperational?0x01:0x00) | 
                            (commandMessage.m_TaseOperational?0x02:0x00) | 
                            (commandMessage.m_WindEstimateConverged?0x04:0x00) | 
                            0x10 | 
                            0x20));
                    sendBuffer.put ((byte)0); //shadow stuff;
                    sendBuffer.put ((byte)0); //shadow stuff;
                    sendBuffer.put ((byte)0); //shadow stuff;
                    sendBuffer.put ((byte)0); //shadow stuff;
                    sendBuffer.put ((byte)0); //shadow stuff;
                    
                    //Compute CRC before sending message
                    int crc = SatCommMessageArbitrator.compute2ByteCRC_CCITT (sendData, sendData.length-2);
                    sendData [sendData.length-2] = (byte)(0xFF & (crc >> 8));        //MSB-first CRC field
                    sendData [sendData.length-1] = (byte)(0xFF & (crc));           //MSB-first CRC field
                }
                
                podSendThread.queuePacket(sendData);
                satCommSendThread.queuePacket(sendData);
                wgvsmSendThread.queuePacket(sendData);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private class SendThread extends Thread 
    {
        ConcurrentLinkedQueue<DatagramPacket> m_DataToSend = new ConcurrentLinkedQueue<DatagramPacket>();
        
        String m_MulticastHostname;
        InetAddress m_MulticastAddress;
        int m_MulticastPort;
        
        public SendThread (String hostName, int commPort)
        {
            this.setName ("WACS-WAVSMSimSendThread" + hostName + ":" + commPort);
            m_MulticastHostname = hostName;
            try
            {
                m_MulticastAddress = InetAddress.getByName(m_MulticastHostname);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            m_MulticastPort = commPort;
        }
        
        public void run ()
        {
            try
            {            
                InetAddress remoteIPAddress = InetAddress.getByName(m_MulticastHostname);
                MulticastSocket socket;
                socket = new MulticastSocket(m_MulticastPort);
                socket.joinGroup(remoteIPAddress);

                while (true)
                {
                    DatagramPacket packet = m_DataToSend.poll();
                    if (packet != null)
                    {
                        packet.setPort(m_MulticastPort);
                        packet.setAddress(m_MulticastAddress);
                        socket.send(packet);
                    }
                    else
                    {
                        Thread.sleep (10);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void queuePacket (byte[] sendData)
        {
            try
            {
                InetAddress remoteIPAddress = InetAddress.getByName(m_MulticastHostname);
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, remoteIPAddress, m_MulticastPort);

                queuePacket(packet);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void queuePacket (DatagramPacket packet)
        {
            m_DataToSend.add(packet);
            
        }
    }
    
    
    
    
    
    private class RecvThread extends Thread 
    {
        String m_RecvMulticastHostname;
        int m_RecvMulticastPort;
        
        public RecvThread (String hostName, int port)
        {
            this.setName ("WACS-WAVSMSimRecvThread" + hostName + ":" + port);
            m_RecvMulticastHostname = hostName;
            m_RecvMulticastPort = port;
        }
        
        public void run ()
        {
            try
            {            
                MulticastSocket receiveSocket;
                receiveSocket = new MulticastSocket(m_RecvMulticastPort);
                InetAddress remoteIPAddress = InetAddress.getByName(m_RecvMulticastHostname);
                receiveSocket.joinGroup(remoteIPAddress);

                while (true)
                {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                    receiveSocket.receive(packet);
                    //System.out.println ("Recv packet from " + m_RecvMulticastPort);
                    
                    int messageType = SatCommMessageArbitrator.verifyPacketHeader (receiveData, packet.getLength());
                    boolean crcGood = SatCommMessageArbitrator.verifyPacketCrc (receiveData, packet.getLength());

                    if (messageType == SatCommMessageArbitrator.MSGTYPE_INVALID || !crcGood)
                    {
                        //Packet got mangled from original version, just throw it away
                        System.err.println ("Bad packet received and ignored!  Type: " + messageType);
                        continue;
                    }
                    if (messageType == SatCommMessageArbitrator.MSGTYPE_PODSTOWAVSM || messageType == SatCommMessageArbitrator.MSGTYPE_RECEIPT)
                    {
                        parseCommandThread.addToQueue(packet);
                        continue;
                    }
                    
                    byte[] buffer = packet.getData();
                    if ((SatCommMessageArbitrator.getPacketDestination(buffer)&0x04) != 0 || (SatCommMessageArbitrator.getPacketDestination(buffer)&0x08) != 0)
                    {
                        podSendThread.queuePacket(packet);
                        satCommSendThread.queuePacket(packet);
                        wgvsmSendThread.queuePacket(packet);
                    }   
                    else if ((SatCommMessageArbitrator.getPacketDestination(buffer)&0x01) != 0)
                    {
                        podSendThread.queuePacket(packet);
                        satCommSendThread.queuePacket(packet);
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
        ShadowWAVSMSimulator simulator = new ShadowWAVSMSimulator(false);
        simulator.run();
    }

}
