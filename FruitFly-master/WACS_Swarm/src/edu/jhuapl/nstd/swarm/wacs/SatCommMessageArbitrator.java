/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs;

import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.SatCommStatusBeliefSatComm;
import edu.jhuapl.nstd.swarm.display.TimeSyncDialog;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.wacs.satcomm.CloudTrackingSatCommPacket;
import edu.jhuapl.nstd.swarm.wacs.satcomm.GenericSatCommPacket;
import edu.jhuapl.nstd.swarm.wacs.satcomm.LoiterCenterSatCommPacket;
import edu.jhuapl.nstd.swarm.wacs.satcomm.PodCommandSatCommPacket;
import edu.jhuapl.nstd.swarm.wacs.satcomm.SafetyBoxSatCommPacket;
import edu.jhuapl.nstd.swarm.wacs.satcomm.ProgressiveImageRequestSatCommPacket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashSet;

/**
 *
 * @author humphjc1
 */
public abstract class SatCommMessageArbitrator
{
    protected final static int WACS_SYNC_BYTE = 0x7E;
    
    public final static int MAX_DATABLOCK_LENGTH = 126;
    public final static int MSG_HEADER_SIZE = 10;
    public final static int MSG_CRC_SIZE = 2;
    
    public final static int DESTINATION_WACSPOD = 1;
    public final static int DESTINATION_WAVSM = 2;
    public final static int DESTINATION_WGVSM = 4;
    public final static int DESTINATION_WACSGCS = 8;
    
    public final static int MSGTYPE_INVALID = -1;
    public final static int MSGTYPE_RECEIPT = 127;
    public final static int MSGTYPE_CRITICALDOWNLINK = 0;
    public final static int MSGLENGTH_CRITICALDOWNLINK = 72;
    public final static int MSGTYPE_WAVSMTOPODS = 4;
    public final static int MSGLENGTH_WAVSMTOPODS = 120;
    public final static int MSGTYPE_PODSTOWAVSM = 5;
    public final static int MSGLENGTH_PODSTOWAVSM = 84;
    public final static int MSGTYPE_PODSTATUS = 32;
    public final static int MSGLENGTH_PODSTATUS = 44;
    public final static int MSGTYPE_PODSDETECTION = 33;
    public final static int MSGLENGTH_PODSDETECTION = 24;
    public final static int MSGTYPE_PODIMAGEDATASEGMENT = 34;
    public final static int MSGLENGTH_PODVIDEODATA = 137;
    public final static int MSGTYPE_PODIMAGETXSIZE = 35;
    public final static int MSGLENGTH_SATCOMMIMAGETXSIZE = 16; 
    
    public final static int MSGTYPE_GCSCOMMANDMESSAGE = 64;
    public final static int MSGLENGTH_GCSCOMMANDMESSAGE_PLUS = 14;
    public final static int GCSCMDCODE_SETEXPLOSIONTIME = 1;
    public final static int GCSCMDPARAMLENGTH_SETEXPLOSIONTIME = 4;
    public final static int GCSCMDCODE_SETIBACSTATE = 2;
    public final static int GCSCMDPARAMLENGTH_SETIBACSTATE = 2;
    public final static int GCSCMDCODE_SETWACSMODE = 3;
    public final static int GCSCMDPARAMLENGTH_SETWACSMODE = 2;
    public final static int GCSCMDCODE_SETWACSWAYPOINTSETTINGS = 4;
    public final static int GCSCMDPARAMLENGTH_SETWACSWAYPOINTSETTINGS = 10;
    public final static int GCSCMDCODE_SETCONTROLAP = 5;
    public final static int GCSCMDPARAMLENGTH_SETCONTROLAP = 2;
    public final static int GCSCMDCODE_SETTARGET = 6;
    public final static int GCSCMDPARAMLENGTH_SETTARGET = 20;
    public final static int GCSCMDCODE_SETALLOWINTERCEPT = 7;
    public final static int GCSCMDPARAMLENGTH_SETALLOWINTERCEPT = 2;
    public final static int GCSCMDCODE_SETANACONDASTATE = 8;
    public final static int GCSCMDPARAMLENGTH_SETANACONDASTATE = 2;
    public final static int GCSCMDCODE_SETC100STATE = 9;
    public final static int GCSCMDPARAMLENGTH_SETC100STATE = 2;
    public final static int GCSCMDCODE_SETALPHASTATE = 10;
    public final static int GCSCMDPARAMLENGTH_SETALPHASTATE = 2;
    public final static int GCSCMDCODE_SETFRAMEREQUEST = 11;
    public final static int GCSCMDPARAMLENGTH_SETFRAMEREQUEST = 1;
    public final static int GCSCMDCODE_SETVIDEOSEGMENTRECEIVED = 12;
    public final static int GCSCMDPARAMLENGTH_SETVIDEOSEGMENTRECEIVED = 4;   
    public final static int GCSCMDCODE_SETVIDEOCONVERTERSTATE = 13;
    public final static int GCSCMDPARAMLENGTH_SETVIDEOCONVERTERSTATE = 1;
    public final static int GCSCMDCODE_SETVIDEORECORDERSTATE = 14;
    public final static int GCSCMDPARAMLENGTH_SETVIDEORECORDERSTATE = 1;
    public final static int GCSCMDCODE_SETVIDEOSTREAMSTATE = 15;
    public final static int GCSCMDPARAMLENGTH_SETVIDEOSTREAMSTATE = 7;
    public final static int GCSCMDCODE_SETGUARANTEEDFRAMEREQUEST = 16;
    public final static int GCSCMDPARAMLENGTH_SETGUARANTEEDFRAMEREQUEST = 4;
    
    public final static int MSGTYPE_TIMESYNC = 81;
    public final static int MSGLENGTH_TIMESYNC = 22;
    public final static int MSGTYPE_SAFETYBOX = 82;
    public final static int MSGLENGTH_SAFETYBOX = 56;
    public final static int MSGTYPE_LOITERCENTER = 83;
    public final static int MSGLENGTH_LOITERCENTER = 20;
    public final static int MSGTYPE_PODCOMMAND = 84;
    public final static int MSGLENGTH_PODCOMMAND = 14;
    public final static int MSGTYPE_CLOUDTRACKINGTYPE = 85;
    public final static int MSGLENGTH_CLOUDTRACKINGTYPE = 14;
    public final static int MSGTYPE_FRAMEREQUEST = 86;
    public final static int MSGLENGTH_FRAMEREQUEST = 17;
    public final static int GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE = 120;   
    public final static int MSGTYPE_SATCOMMSTATUS = 126;
    
    protected final ConcurrentLinkedQueue <DatagramPacket> m_MulticastSendVsmQueue = new ConcurrentLinkedQueue<DatagramPacket>();
    protected MulticastSocket m_MulticastSendVsmSocket;
    protected InetAddress m_MulticastSendVsmIpAddr;
    protected int m_MulticastSendVsmPort;
    
    protected final ConcurrentLinkedQueue <DatagramPacket> m_MulticastReceivedVsmQueue = new ConcurrentLinkedQueue<DatagramPacket>();
    protected MulticastSocket m_MulticastRecvVsmSocket;
    protected InetAddress m_MulticastRecvVsmIpAddr;
    protected int m_MulticastRecvVsmPort;
    
    protected BeliefManagerWacs m_BeliefManager;
    protected int m_MyLocationType;
    
    protected int m_BeliefNetworkAllowableLatencyMs;
    
    //Pod Status Message fields
    protected long m_PodStatusFieldsTimestampMs;
    protected boolean m_CollectorXdLogState;
    protected boolean m_TrackerXdLogState;
    protected boolean m_CollectorRabbitConnected;
    protected boolean m_TrackerRabbitConnected;
    protected AnacondaModeEnum m_AnacondaMode;
    protected boolean m_AnacondaConnected;
    protected boolean m_IbacMode;
    protected boolean m_IbacConnected;
    protected ParticleCollectorMode m_C100Mode;
    protected boolean m_C100Connected;
    protected boolean m_AlphaMode;
    protected boolean m_AlphaConnected;
    protected boolean m_GammaConnected;
    protected boolean m_DosimeterConnected;
    protected boolean m_CollectorTHSensorConnected;
    protected boolean m_TrackerTHSensorConnected;
    protected boolean m_CollectorFanState;
    protected boolean m_CollectorHeatState;
    protected boolean m_CollectorAutoTH;
    protected boolean m_TrackerFanState;
    protected boolean m_TrackerHeatState;
    protected boolean m_TrackerAutoTH;
    protected boolean m_PiccoloConnected;
    protected boolean m_TASEConnected;
    protected boolean m_IRVideoRecState;
    protected boolean m_PODVideoStreamState;
    protected boolean m_PODVideoConversionFinished;
    protected boolean m_SatCommImageTxFinished;
    protected int m_SatCommImageTransmissionSize;
    protected int m_SatCommImageSegmentReceivedCount;
    protected double m_CollectorTempC;
    protected double m_CollectorHumidity;
    protected double m_TrackerTempC;
    protected double m_TrackerHumidity;
    protected WacsMode m_WacsMode;
    protected boolean m_AllowInterceptPermitted;
    protected boolean m_AllowInterceptRecommended;
    protected boolean m_ControlEnabledViaAutopilot;
    protected boolean m_WacsIsFullyAutonomous;
    protected double m_LoiterRadiusM;
    protected double m_InterceptRadiusM;
    protected double m_LoiterAltitudeAglFt;
    protected double m_InterceptAltitudeAglFt;
    protected double m_OffsetLoiterAltitudeAglFt;
    protected double m_CurrentTargetLatitudeDeg;
    protected double m_CurrentTargetLongitudeDeg;
    protected double m_CurrentTargetAltitudeMslFt;
    protected long m_ExpectedExplosionTimeMs;
    
    //Set Explosion Time Command fields
    protected long m_DesiredExplosionTimeMs;
    protected long m_DesiredExplosionTime_TimestampMs;
    protected long m_LastSentExpTime_TimestampMs;
    
    //Set Ibac State Command fields
    protected boolean m_DesiredIbacState;
    protected long m_DesiredIbacState_TimeMs;
    protected long m_LastSentIbacState_TimestampMs;    
    
    // Set Video Part Received fields
    protected int m_LastReceivedVideoSegmentHash = -1;
    protected long m_LastReceivedVideoSegment_TimeMs = -1;
    protected HashSet m_ReceivedVideoSegments = new HashSet();
    protected final Object m_VideoSegmentReceivedLock = new Object();
    
    //Set Frame Request Command fields
    protected long m_DesiredFrameRequest_TimeMs = -1; 
    
    //Set Guaranteed Frame Request Command fields
    protected long m_DesiredGuaranteedFrameRequest_TimeMs = -1;
    protected int m_DesiredGuaranteedDataReceipt;
      
    //Set Video Converter State Command fields
    protected long m_DesiredVideoConverterState_TimeMs;
    
    //Set Video Recorder State Command Fields
    protected boolean m_DesiredVideoRecorderState;
    protected long m_DesiredVideoRecorderState_TimeMs;
    protected long m_LastSentRecorderState_TimestampMs;
    
    //Set Video Stream State Command fields
    protected boolean m_DesiredVideoStreamState;
    protected int[] m_DesiredClientHost;
    protected short m_DesiredClientPort;    
    protected long m_DesiredVideoStreamState_TimeMs;
    protected long m_LastSentVideoStreamState_TimestampMs;
    
    //Set Alpha State Command fields
    protected boolean m_DesiredAlphaState;
    protected long m_DesiredAlphaState_TimeMs;
    protected long m_LastSentAlphaState_TimestampMs;
    
    //Set Anaconda State Command fields
    protected AnacondaModeEnum m_DesiredAnacondaState;
    protected long m_DesiredAnacondaState_TimeMs;
    protected long m_LastSentAnacondaState_TimestampMs;
    
    //Set C100 State Command fields
    protected ParticleCollectorMode m_DesiredC100State;
    protected long m_DesiredC100State_TimeMs;
    protected long m_LastSentC100State_TimestampMs;
                     
    //Set WACS Mode Command fields
    protected WacsMode m_DesiredWacsMode;
    protected long m_DesiredWacsMode_TimeMs;
    protected long m_LastSentWacsMode_TimestampMs;
    
    //Set WACS Waypoint Settings
    protected double m_DesiredLoiterRadiusM;
    protected double m_DesiredInterceptRadiusM;
    protected double m_DesiredLoiterAltitudeAglFt;
    protected double m_DesiredInterceptAltitudeAglFt;
    protected double m_DesiredOffsetLoiterAltitudeAglFt;
    protected long m_DesiredWWB_TimestampMs;
    protected long m_LastSentWWB_TimestampMs;
    
    //Set 'Control Enabled Via Autopilot'
    protected boolean m_DesiredControlAP;
    protected long m_DesiredControlAP_TimestampMs;
    protected long m_LastSentControlAP_TimestampMs;
                    
    //Set gimbal target position
    protected double m_DesiredTargetLatitudeDeg;
    protected double m_DesiredTargetLongitudeDeg;
    protected double m_DesiredTargetAltitudeMslFt;
    protected long m_DesiredTargetTimestampMs;
    protected long m_LastSentTarget_TimestampMs;
    
    //Set 'Allow Intercept'
    protected boolean m_DesiredAllowIntercept;
    protected long m_DesiredAllowIntercept_TimestampMs;
    protected long m_LastSentAllowIntercept_TimestampMs;

    ConcurrentLinkedQueue<GenericSatCommPacket> m_GenericSatCommPackets = new ConcurrentLinkedQueue<GenericSatCommPacket>();
    
    //Particle count detection message
    protected int m_RecentMaxTotalParticleCountSinceLastSend;
    protected long m_RecentMaxTotalParticleCount_TimestampMs;
    
    //Chemical detection message
    protected int m_RecentMaxLcdBarsSinceLastSend;
    protected long m_RecentMaxLcdBars_TimestampMs;
    
    //Guaranteed video data message
    protected byte[] m_ProgressiveImageBuffer;
    
    public SatCommMessageArbitrator (BeliefManagerWacs belMgr, int locationType, InetAddress multicastSendVsmAddress, int multicastSendVsmPort, InetAddress multicastRecvVsmAddress, int multicastRecvVsmPort, int beliefNetworkAllowableLatencyMs)
    {
        try
        {
            m_BeliefManager = belMgr;
            m_MyLocationType = locationType;
            long defaultSendPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.DefaultMessageSendPeriod.Ms", 5000);
            
            SafetyBoxSatCommPacket safetyPacket = new SafetyBoxSatCommPacket(belMgr, MSGTYPE_SAFETYBOX, MSGLENGTH_SAFETYBOX, defaultSendPeriodMs);
            m_GenericSatCommPackets.add(safetyPacket);
            LoiterCenterSatCommPacket loiterPacket = new LoiterCenterSatCommPacket(belMgr, MSGTYPE_LOITERCENTER, MSGLENGTH_LOITERCENTER, defaultSendPeriodMs);
            m_GenericSatCommPackets.add(loiterPacket);
            PodCommandSatCommPacket podCommandPacket = new PodCommandSatCommPacket(belMgr, MSGTYPE_PODCOMMAND, MSGLENGTH_PODCOMMAND, defaultSendPeriodMs);
            m_GenericSatCommPackets.add(podCommandPacket);
            CloudTrackingSatCommPacket cloudTrackingPacket = new CloudTrackingSatCommPacket(belMgr, MSGTYPE_CLOUDTRACKINGTYPE, MSGLENGTH_CLOUDTRACKINGTYPE, defaultSendPeriodMs);
            m_GenericSatCommPackets.add(cloudTrackingPacket);
            ProgressiveImageRequestSatCommPacket guaranteedFramePacket = new ProgressiveImageRequestSatCommPacket(belMgr, MSGTYPE_FRAMEREQUEST, MSGLENGTH_FRAMEREQUEST, defaultSendPeriodMs);
            m_GenericSatCommPackets.add(guaranteedFramePacket);
            
            m_RecentMaxTotalParticleCountSinceLastSend = -1;
            m_RecentMaxTotalParticleCount_TimestampMs = 0;
            m_RecentMaxLcdBarsSinceLastSend = -1;
            m_RecentMaxLcdBars_TimestampMs = 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        m_MulticastSendVsmIpAddr = multicastSendVsmAddress;
        m_MulticastSendVsmPort = multicastSendVsmPort;
        m_MulticastRecvVsmIpAddr = multicastRecvVsmAddress;
        m_MulticastRecvVsmPort = multicastRecvVsmPort;
        m_BeliefNetworkAllowableLatencyMs = beliefNetworkAllowableLatencyMs;
        
        try
        {
            m_MulticastSendVsmSocket = new MulticastSocket(m_MulticastSendVsmPort);
            m_MulticastSendVsmSocket.joinGroup(m_MulticastSendVsmIpAddr);
        }
        catch (Exception e)
        {
            System.err.println ("Could not connect to multicast address: " + m_MulticastSendVsmIpAddr.getHostAddress() + ":" + m_MulticastSendVsmPort);
            e.printStackTrace();
            m_MulticastSendVsmSocket = null;
        }
        
        try
        {
            m_MulticastRecvVsmSocket = new MulticastSocket(m_MulticastRecvVsmPort);
            m_MulticastRecvVsmSocket.joinGroup(m_MulticastRecvVsmIpAddr);
        }
        catch (Exception e)
        {
            System.err.println ("Could not connect to multicast address: " + m_MulticastRecvVsmIpAddr.getHostAddress() + ":" + m_MulticastRecvVsmPort);
            e.printStackTrace();
            m_MulticastRecvVsmSocket = null;
        }
        
        
        new NetworkSendThread().start();
        new NetworkRecvThread(m_MulticastReceivedVsmQueue, m_MulticastRecvVsmSocket, m_MulticastRecvVsmIpAddr, m_MulticastRecvVsmPort).start();
    }
    
    protected final byte[] getBlankByteBuffer()
    {
        return (new byte [getFullMsgLength(MAX_DATABLOCK_LENGTH)]);
    }
    
    public final byte[] getMulticastByteBuffer(int msgDestination, int msgPriority, int msgType, int fullMsgLength, long msgTimeMs)
    {
        int dataBlockLength = getDataBlockLength (fullMsgLength);
        
        if (dataBlockLength > MAX_DATABLOCK_LENGTH || dataBlockLength <= 0)
            fullMsgLength = -1;
        
        //Check to see if message size is valid
        if (fullMsgLength > 0)
        {
            byte sendBuffer[] = getBlankByteBuffer ();
            //Populate message buffer with fields
            formMessageHeader (sendBuffer, msgDestination, msgPriority, msgType, dataBlockLength, msgTimeMs);

            return sendBuffer;
        }
        
        //Something wrong forming packet header
        System.err.println ("Invalid packet header for message type " + msgType);
        return null;
    }
    
    protected static final int getFullMsgLength (int dataBlockLength)
    {
        return MSG_HEADER_SIZE + dataBlockLength + MSG_CRC_SIZE;
    }
    
    public static final int getDataBlockLength (int fullMsgLength)
    {
        return fullMsgLength - MSG_HEADER_SIZE - MSG_CRC_SIZE;
    }
    
    public static final int getHeaderSize ()
    {
        return MSG_HEADER_SIZE;
    }
    
    public static final int getCrcSize()
    {
        return MSG_CRC_SIZE;
    }
    
    public static final void formMessageHeader (byte bufferedMsg[], int destinations, int priority, int messageType, int dataBlockLength, long msgTimestampMs)
    {
        bufferedMsg [0] = WACS_SYNC_BYTE;     //WACS Sync Byte
        bufferedMsg [1] = (byte)destinations;      //Destination field.  Multiple destinations 'OR'ed together.
        
        int msgInfo = 0;
        msgInfo |= (0x03 & priority);       //Add 2 bits worth of priority field to bits 0-1 of short
        msgInfo |= ((0x7F & messageType) << 2);     //Add 7 bits worth of message type field to bits 2-8 of short (bit shift left 2 bits)
        msgInfo |= ((0x7F & dataBlockLength) << 9);     //Add 7 bits of of data block length to bits 9-15 of short (bit shift left 9 bits)
        bufferedMsg [2] = (byte)(0xFF & (msgInfo >> 8));        //MSB-first Message info field
        bufferedMsg [3] = (byte)(0xFF & (msgInfo));             //MSB-first Message info field
        
        long epochSeconds = msgTimestampMs/1000;    //Seconds since 1/1/1970 Epoch
        int partialMillis = (int)(msgTimestampMs%1000);   //Partial millisconds after 'epochSeconds' value
        bufferedMsg [4] = (byte)(0xFF & (epochSeconds >> 24));        //MSB-first Timestamp seconds field
        bufferedMsg [5] = (byte)(0xFF & (epochSeconds >> 16));        //MSB-first Timestamp seconds field
        bufferedMsg [6] = (byte)(0xFF & (epochSeconds >> 8));        //MSB-first Timestamp seconds field
        bufferedMsg [7] = (byte)(0xFF & (epochSeconds));             //MSB-first Timestamp seconds field
        bufferedMsg [8] = (byte)(0xFF & (partialMillis >> 8));        //MSB-first Timestamp milliseconds field
        bufferedMsg [9] = (byte)(0xFF & (partialMillis));             //MSB-first Timestamp milliseconds field
    }
    
    public static final int verifyPacketHeader (byte bufferedMsg[], int msgLength)
    {
        if (bufferedMsg == null || bufferedMsg.length < msgLength)
            return MSGTYPE_INVALID;
        if (msgLength <= MSG_HEADER_SIZE || msgLength > getFullMsgLength(MAX_DATABLOCK_LENGTH))
            return MSGTYPE_INVALID;
        if (bufferedMsg [0] != WACS_SYNC_BYTE)
            return MSGTYPE_INVALID;
        
        int msgInfo = 0;
        msgInfo |= ((bufferedMsg[2]&0xFF) << 8);       //MSB-first Message info field
        msgInfo |= ((bufferedMsg[3]&0xFF));            //MSB-first Message info field
        int messageType = (0x7F & (msgInfo >> 2));      //Message type field is 7 bits long, shifted 2 bits
        int dataBlockLength = (0x7F & (msgInfo >> 9));  //Data block length is 7 bits long, shifted 9 bits

        if (dataBlockLength != getDataBlockLength(msgLength))
            return MSGTYPE_INVALID;
        
        return messageType;
    }
    
    public static final long getPacketTimestampMs (byte bufferedMsg[])
    {
        long epochSeconds = 0;
        epochSeconds |= ((bufferedMsg [4]&0xFF) << 24);      //MSB-first Timestamp seconds field
        epochSeconds |= ((bufferedMsg [5]&0xFF) << 16);      //MSB-first Timestamp seconds field
        epochSeconds |= ((bufferedMsg [6]&0xFF) << 8);       //MSB-first Timestamp seconds field
        epochSeconds |= ((bufferedMsg [7]&0xFF));            //MSB-first Timestamp seconds field
        int partialMillis = 0;
        partialMillis |= ((bufferedMsg [8]&0xFF) << 8);      //MSB-first Timestamp milliseconds field
        partialMillis |= ((bufferedMsg [9]&0xFF));           //MSB-first Timestamp milliseconds field
        
        long timestampMs = epochSeconds*1000L + partialMillis;
        return timestampMs;
    }
    
    public static final int getPacketDestination (byte bufferedMsg[])
    {
        int destination = 0;
        if (bufferedMsg[0] == 0x7E)
            destination |= (bufferedMsg[1]&0xFF);
        return destination;
                    
    }
    
    public static final int compute2ByteCRC_CCITT (byte bufferedMsg[], int crcCountLength)
    {
        int crc = 0xFFFFFFFF;
        
        for (int count = 0; count < crcCountLength; ++count)
        {
            int temp = (bufferedMsg[count] ^ (crc >> 8)) & 0xFF;
            crc = m_pCrcTable[temp] ^ (crc << 8);
        }

        return crc;
    }
    
    public static final boolean verifyPacketCrc (byte bufferedMsg[], int msgLength)
    {
        int computedCrc = compute2ByteCRC_CCITT(bufferedMsg, msgLength - MSG_CRC_SIZE);
        
        int msgCrc = 0;
        msgCrc |= ((bufferedMsg[msgLength - MSG_CRC_SIZE]&0xFF) << 8);       //MSB-first CRC field
        msgCrc |= ((bufferedMsg[msgLength - MSG_CRC_SIZE + 1]&0xFF));        //MSB-first CRC field
        
        //Computed CRC is an integer to preserve sign, but need to mask it to 16-bit short for actual comparison
        if (msgCrc != (computedCrc&0xFFFF))
            return false;
        return true;
    }
    
    public static void checkAndPrintReceiptMessage (byte []receiveData, ByteBuffer receiveBuffer)
    {
        //this receipt packet has a destination of this vsm, so just check and display its contents
        //Get timestamp of the packet, which will be the timestamp of the beliefs created by the message
        long packetTimestampMs = SatCommMessageArbitrator.getPacketTimestampMs(receiveData);

        receiveBuffer.rewind();

        //Clear header
        for (int i = 0; i < 10; i ++)
            receiveBuffer.get();

        int msgInfo = receiveBuffer.getShort();
        int priority = (0x03 & (msgInfo));              //Priority field is 2 bits long
        int msgType = (0x7F & (msgInfo >> 2));           //Message type is 7 bits long, shifted 2 bits
        int dataBlockLength = (0x7F & (msgInfo >> 9));  //Data block length is 7 bits long, shifted 9 bits

        int latencyMs = receiveBuffer.getShort();
        if (latencyMs < 0)
            latencyMs = Short.MAX_VALUE + latencyMs;
        long timeSpentInBufferMs = receiveBuffer.getInt();
        if (timeSpentInBufferMs < 0)
            timeSpentInBufferMs = Integer.MAX_VALUE + timeSpentInBufferMs;
        long backLogBytes = receiveBuffer.getInt();
        if (backLogBytes < 0)
            backLogBytes = Integer.MAX_VALUE + backLogBytes;

        long epochSeconds = receiveBuffer.getInt();
        if (epochSeconds < 0)
            epochSeconds = Integer.MAX_VALUE + epochSeconds;
        int partialMillis = receiveBuffer.getShort();
        if (partialMillis < 0)
            partialMillis = Short.MAX_VALUE + partialMillis;
        long msgTimestampMs = epochSeconds*1000L + partialMillis;

        System.out.println ("Packet receipt at " + packetTimestampMs + " for message [P=" + priority + "T=" + msgType + "L=" + dataBlockLength + "t=" + msgTimestampMs + 
                "]  Latency:" + latencyMs + "ms  TimeInBuffer:" + timeSpentInBufferMs + "ms  Backlog:" + backLogBytes + "bytes");
    }
    
    
    private static final double LON3PACKED_UPPERLIMIT = 179.9999785;
    private static final double LON3PACKED_LOWERLIMIT = -180;
    private static final int LON3PACKED_UPPERPACKED = 0x7FFFFF;
    private static final int LON3PACKED_LOWERPACKED = -0x800000;
    
    public static int computeLonDegAsRad3BytePacked (double lDecDeg)
    {
        return LON3PACKED_LOWERPACKED + (int)((LON3PACKED_UPPERPACKED-LON3PACKED_LOWERPACKED)*Math.max(0, Math.min(1, ((lDecDeg - LON3PACKED_LOWERLIMIT)/(LON3PACKED_UPPERLIMIT - LON3PACKED_LOWERLIMIT)))));
    }
    
    public static double extractLonDegFromRad3BytePacked (int packed)
    {
        if (packed > LON3PACKED_UPPERPACKED)
            packed += LON3PACKED_LOWERPACKED*2;
        return LON3PACKED_LOWERLIMIT + (LON3PACKED_UPPERLIMIT - LON3PACKED_LOWERLIMIT)*((double)packed - LON3PACKED_LOWERPACKED)/(LON3PACKED_UPPERPACKED-LON3PACKED_LOWERPACKED);
    }
    
    private static final double LAT3PACKED_UPPERLIMIT = 89.99998927;
    private static final double LAT3PACKED_LOWERLIMIT = -90;
    private static final int LAT3PACKED_UPPERPACKED = 0x7FFFFF;
    private static final int LAT3PACKED_LOWERPACKED = -0x800000;
    
    public static int computeLatDegAsRad3BytePacked (double lDecDeg)
    {
        return LAT3PACKED_LOWERPACKED + (int)((LAT3PACKED_UPPERPACKED-LAT3PACKED_LOWERPACKED)*Math.max(0, Math.min(1, ((lDecDeg - LAT3PACKED_LOWERLIMIT)/(LAT3PACKED_UPPERLIMIT - LAT3PACKED_LOWERLIMIT)))));
    }
    
    public static double extractLatDegFromRad3BytePacked (int packed)
    {
        if (packed > LAT3PACKED_UPPERPACKED)
            packed += LAT3PACKED_LOWERPACKED*2;
        return LAT3PACKED_LOWERLIMIT + (LAT3PACKED_UPPERLIMIT - LAT3PACKED_LOWERLIMIT)*((double)packed - LAT3PACKED_LOWERPACKED)/(LAT3PACKED_UPPERPACKED-LAT3PACKED_LOWERPACKED);
    }
    
    private static final double HEADING2PACKED_UPPERLIMIT = 179.9945068*MathConstants.DEG2RAD;
    private static final double HEADING2PACKED_LOWERLIMIT = -180*MathConstants.DEG2RAD;
    private static final int HEADING2PACKED_UPPERPACKED = 0x7FFF;
    private static final int HEADING2PACKED_LOWERPACKED = -0x8000;
    
    public static int computeHeadingRadAs2BytePacked (double headingRad)
    {
        if (headingRad > HEADING2PACKED_UPPERLIMIT)
            headingRad -= Math.PI*2;
        return HEADING2PACKED_LOWERPACKED + (int)((HEADING2PACKED_UPPERPACKED-HEADING2PACKED_LOWERPACKED)*Math.max(0, Math.min(1, ((headingRad - HEADING2PACKED_LOWERLIMIT)/(HEADING2PACKED_UPPERLIMIT - HEADING2PACKED_LOWERLIMIT)))));
    }
    
    public static double extractHeadingRadFrom2BytePacked (int packed)
    {
        if (packed > HEADING2PACKED_UPPERPACKED)
            packed += HEADING2PACKED_LOWERPACKED*2;
        return HEADING2PACKED_LOWERLIMIT + (HEADING2PACKED_UPPERLIMIT - HEADING2PACKED_LOWERLIMIT)*((double)packed - HEADING2PACKED_LOWERPACKED)/(HEADING2PACKED_UPPERPACKED-HEADING2PACKED_LOWERPACKED);
    }
    
    private static final double ROLL12BITPACKED_UPPERLIMIT = 39.98046875*MathConstants.DEG2RAD;
    private static final double ROLL12BITPACKED_LOWERLIMIT = -40*MathConstants.DEG2RAD;
    private static final int ROLL12BITPACKED_UPPERPACKED = 0x7FF;
    private static final int ROLL12BITPACKED_LOWERPACKED = -0x800;
    
    public static int computeRollRadAs12BitPacked (double rollRad)
    {
        return ROLL12BITPACKED_LOWERPACKED + (int)((ROLL12BITPACKED_UPPERPACKED-ROLL12BITPACKED_LOWERPACKED)*Math.max(0, Math.min(1, ((rollRad - ROLL12BITPACKED_LOWERLIMIT)/(ROLL12BITPACKED_UPPERLIMIT - ROLL12BITPACKED_LOWERLIMIT)))));
    }
    
    public static double extractRollRadFrom12BitPacked (int packed)
    {
        if (packed > ROLL12BITPACKED_UPPERPACKED)
            packed += ROLL12BITPACKED_LOWERPACKED*2;
        return ROLL12BITPACKED_LOWERLIMIT + (ROLL12BITPACKED_UPPERLIMIT - ROLL12BITPACKED_LOWERLIMIT)*((double)packed - ROLL12BITPACKED_LOWERPACKED)/(ROLL12BITPACKED_UPPERPACKED-ROLL12BITPACKED_LOWERPACKED);
    }
    
    private static final double PITCH12BITPACKED_UPPERLIMIT = 59.97070313*MathConstants.DEG2RAD;
    private static final double PITCH12BITPACKED_LOWERLIMIT = -60*MathConstants.DEG2RAD;
    private static final int PITCH12BITPACKED_UPPERPACKED = 0x7FF;
    private static final int PITCH12BITPACKED_LOWERPACKED = -0x800;
    
    public static int computePitchRadAs12BitPacked (double pitchRad)
    {
        return PITCH12BITPACKED_LOWERPACKED + (int)((PITCH12BITPACKED_UPPERPACKED-PITCH12BITPACKED_LOWERPACKED)*Math.max(0, Math.min(1, ((pitchRad - PITCH12BITPACKED_LOWERLIMIT)/(PITCH12BITPACKED_UPPERLIMIT - PITCH12BITPACKED_LOWERLIMIT)))));
    }
    
    public static double extractPitchRadFrom12BitPacked (int packed)
    {
        if (packed > PITCH12BITPACKED_UPPERPACKED)
            packed += PITCH12BITPACKED_LOWERPACKED*2;
        return PITCH12BITPACKED_LOWERLIMIT + (PITCH12BITPACKED_UPPERLIMIT - PITCH12BITPACKED_LOWERLIMIT)*((double)packed - PITCH12BITPACKED_LOWERPACKED)/(PITCH12BITPACKED_UPPERPACKED-PITCH12BITPACKED_LOWERPACKED);
    }
    
    public final boolean sendBufferedMsg (byte bufferedMsg[], int fullMsgLength, int bufferPos, int msgType)
    {
        try
        {
            //Compute CRC before sending message
            int crc = compute2ByteCRC_CCITT (bufferedMsg, bufferPos);
            bufferedMsg [bufferPos++] = (byte)(0xFF & (crc >> 8));        //MSB-first CRC field
            bufferedMsg [bufferPos++] = (byte)(0xFF & (crc));           //MSB-first CRC field
            
            if (bufferPos == fullMsgLength)
            {
                //Bytes add up correctly, add packet to buffer to be sent out
                DatagramPacket multicastSendPacket = new DatagramPacket(bufferedMsg, fullMsgLength, m_MulticastSendVsmIpAddr, m_MulticastSendVsmPort);
                m_MulticastSendVsmQueue.add(multicastSendPacket);
                
                return true;
            }
            
            //Byte count mismatch
            System.err.println ("Byte count mismatch:  " + bufferPos + " != " + fullMsgLength + " for message type " + msgType);
            return false;
        }
        catch (Exception e)
        {
            System.err.println ("Error sending SATCOM multicast packet from WACS pods");
            e.printStackTrace();
            return false;
        }
    }
    
    protected boolean parseGenericPackets (int messageType, byte [] bufferedMsg)
    {
        //Get timestamp of the packet, which will be the timestamp of the beliefs created by the message
        long dataTimestampMs = getPacketTimestampMs(bufferedMsg);
        
        if (messageType == MSGTYPE_TIMESYNC)
        {
            //We shouldn't receive time sync messages while the WACS software is running, so resolve this issue
            handleTimeSyncMessage (messageType, bufferedMsg);
            return true;
        }
        if (messageType == MSGTYPE_SATCOMMSTATUS)
        {
            //Parse status message of sat comm connection
            handleSatCommStatusMessage (messageType, bufferedMsg);
            return true;
        }
    
        boolean retVal = false;
        for (GenericSatCommPacket template : m_GenericSatCommPackets)
        {
            //Process message and distrubute contents
            if (messageType == template.getMessageType())
            {
                template.processMessage (bufferedMsg, MSG_HEADER_SIZE, dataTimestampMs);
                retVal = true;
                break;
            }
        }
        
        return retVal;
    }
    
    protected void setLastReceivedSegmentHash(int hash)
    {
        synchronized(m_VideoSegmentReceivedLock)
        {
            m_LastReceivedVideoSegmentHash = hash;
        }
    }
    
    protected int getLastReceivedSegmentHash()
    {
        int hash;
        
        synchronized(m_VideoSegmentReceivedLock)
        {
            hash = m_LastReceivedVideoSegmentHash;
        }
        return hash;
    }
    
    protected void handleSatCommStatusMessage (int messageType, byte [] bufferedMsg)
    {
        int bufferPos = MSG_HEADER_SIZE;
        
        int modemState = 0;
        modemState |= (0xFF & bufferedMsg[bufferPos++]);
        int modemStatus = 0;
        modemStatus |= (0xFF & bufferedMsg[bufferPos++]);
        int latencyMs = 0;
        latencyMs |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        latencyMs |= (0xFF & bufferedMsg[bufferPos++]);
        long backLogBytes = 0;
        backLogBytes |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        backLogBytes |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        backLogBytes |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        backLogBytes |= (0xFF & bufferedMsg[bufferPos++]);
        
        SatCommStatusBeliefSatComm blf = new SatCommStatusBeliefSatComm(modemState, modemStatus==1, latencyMs, backLogBytes);
        m_BeliefManager.put(blf);
    }
    
    protected void handleTimeSyncMessage (int messageType, byte [] bufferedMsg)
    {
        //The fact that we are receiving a time sync message here means we are running the WACS software on this side, but
        //the other side is potentially still running time sync software.  Send a time sync message letting it know that we are
        //already synced and running.

        //However, its possible that the other side is also running WACS and the time sync message was buffered/delayed.  If we send a 
        //time sync message, the other side will get it and think it is experiencing the same issue, and start an infinite loop
        //of time sync messages.  Need a way to know the difference


        //So, if we are sending the message from here, we will send a STATUS_SYNCCOMPLETE status, which means the sync process is complete,
        //whereas the sync program sends a STATUS_SYNCED status when its still running.  We should only send back a response sync
        //message is the received status is not STATUS_SYNCCOMPLETE

        ByteBuffer buf = ByteBuffer.wrap(bufferedMsg, MSG_HEADER_SIZE, bufferedMsg.length-MSG_HEADER_SIZE);
        int type = buf.get();
        long timeMs = buf.getLong();
        int status = buf.get();

        if (type != m_MyLocationType)
        {
            //This time sync message was received from the other location
            if (status != TimeSyncDialog.STATUS_SYNCCOMPLETE)
            {
                //This time sync message was sent by the time sync dialog at the other location, not the WACS software
                //Send STATUS_SYNCCOMPLETE status message
                System.out.println ("Time sync request received while WACS is running, sending time sync response");
                
                int sendMessageLength = MSGLENGTH_TIMESYNC;
                int sendBlockLength = getDataBlockLength(sendMessageLength);
                int sendMessageType = MSGTYPE_TIMESYNC;
                int priority = 2;
                byte sendBuffer[] = new byte[sendMessageLength];
                
                int destination;
                if (m_MyLocationType == TimeSyncDialog.LOCATION_GCS)
                    destination = DESTINATION_WACSPOD;
                else //if (m_MyLocationType == TimeSyncDialog.LOCATION_POD)
                    destination = DESTINATION_WACSGCS;
                SatCommMessageArbitrator.formMessageHeader(sendBuffer, destination, priority, sendMessageType, sendBlockLength, System.currentTimeMillis());

                ByteBuffer dataBlockBuffer = ByteBuffer.wrap(sendBuffer, SatCommMessageArbitrator.getHeaderSize(), SatCommMessageArbitrator.getDataBlockLength(sendMessageLength));
                dataBlockBuffer.put((byte)m_MyLocationType).putLong(System.currentTimeMillis()).put((byte)TimeSyncDialog.STATUS_SYNCCOMPLETE).array();
                int sendBufferPos = sendMessageLength - MSG_CRC_SIZE;
                sendBufferedMsg(sendBuffer, sendMessageLength, sendBufferPos, sendMessageType);
            }
        }
    }
    
    protected void formAndSendGenericPackets (int destination)
    {
        for (GenericSatCommPacket template : m_GenericSatCommPackets)
        {
            //Occassionally, check to see if this message should be checked and sent out.
            template.formAndSendBelief (destination, MSG_HEADER_SIZE, this);
        }
    }
    
    protected final class NetworkSendThread extends Thread
    {
        public NetworkSendThread ()
        {
            setDaemon (true);
        }
        
        public boolean sendBuffer (ConcurrentLinkedQueue <DatagramPacket> buffer, MulticastSocket socket)
        {
            if (buffer.isEmpty())
            {
                return false;
            }

            //Get the next packet to send and send it out
            try
            {
                DatagramPacket packetToSend = buffer.poll();
                socket.send(packetToSend);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return true;
        }
        
        public void run ()
        {            
            try
            {
                while (true)
                {
                    boolean somethingSentVsm = sendBuffer(m_MulticastSendVsmQueue, m_MulticastSendVsmSocket);
                    
                    if (!somethingSentVsm)
                    {
                        //If no packets to send, sleep for a bit
                        try 
                        {
                            Thread.sleep (10);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        continue;
                    }              
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };
    
    protected final class NetworkRecvThread extends Thread
    {
        protected ConcurrentLinkedQueue <DatagramPacket> m_ReceiveQueue;
        protected MulticastSocket m_ReceiveSocket;
        protected InetAddress m_ReceiveIpAddr;
        protected int m_ReceivePort;
        
    
        public NetworkRecvThread (ConcurrentLinkedQueue <DatagramPacket> queue, MulticastSocket socket, InetAddress ipAddr, int port)
        {
            setDaemon (true);
            m_ReceiveQueue = queue;
            m_ReceiveSocket = socket;
            m_ReceiveIpAddr = ipAddr;
            m_ReceivePort = port;
        }
        
        public void run ()
        {            
            try
            {
                while (true)
                {
                    //Get the next packet to read and queue it for processing
                    try
                    {
                        byte [] bufferedMsg = getBlankByteBuffer();
                        DatagramPacket multicastRecvPacket = new DatagramPacket(bufferedMsg, bufferedMsg.length, m_ReceiveIpAddr, m_ReceivePort);
                        
                        m_ReceiveSocket.receive(multicastRecvPacket);
                        
                        if (multicastRecvPacket != null)
                        {
                            //We received a packet, add it to the queue
                            m_ReceiveQueue.add(multicastRecvPacket);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    
                    //Don't need to sleep because recv call should block
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    
    final private static int m_pCrcTable [] =
    {
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
        0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
        0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
        0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
        0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
        0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
        0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
        0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
        0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
        0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
        0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
        0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
        0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
        0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
        0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
        0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
        0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
        0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
        0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
        0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
        0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
        0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
        0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
    };
}
