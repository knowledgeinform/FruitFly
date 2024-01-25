/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs;

import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.METPositionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBeliefSatComm;
import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaLCDReportMessage.AnacondaDataPair;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.cbrnPods.messages.Pod.podHeartbeatMessage;
import edu.jhuapl.nstd.piccolo.Pic_Telemetry;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.CircularOrbitBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBelief;
import edu.jhuapl.nstd.swarm.belief.RacetrackOrbitBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.SatCommImageTransmissionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.SatCommImageTransmissionFinishedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TestCircularOrbitBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConverterCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConversionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConversionFinishedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.display.TimeSyncDialog;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import edu.jhuapl.nstd.swarm.util.MathUtils;
import edu.jhuapl.nstd.tase.TASE_Telemetry;
import edu.jhuapl.nstd.tase.RecorderStatus;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;
import java.util.BitSet;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author humphjc1
 */
public class GCSSatCommMessageAbritrator extends SatCommMessageArbitrator
{
    //public static final String BELIEF_CREATORNAME = "satcommgcs";
    
    private long m_ExpTimeChangeMessagePeriodMs;
    private long m_IbacStateChangeMessagePeriodMs;
    private long m_VideoRecorderStateChangeMessagePeriodMs;
    private long m_VideoStreamStateChangeMessagePeriodMs;
    private long m_WacsModeChangeMessagePeriodMs;
    private long m_WWBChangeMessagePeriodMs;
    private long m_ControlAPChangeMessagePeriodMs;
    private long m_TargetChangeMessagePeriodMs;
    private long m_AllowInterceptChangeMessagePeriodMs;
    private long m_AnacondaStateChangeMessagePeriodMs;
    private long m_C100StateChangeMessagePeriodMs;
    private long m_AlphaStateChangeMessagePeriodMs;
    
    private long m_DesiredRecorderState_LastAcceptedTimeMs;
    private long m_DesiredVideoStreamState_LastAcceptedTimeMs;
    private long m_DesiredIbacState_LastAcceptedTimeMs;
    private long m_DesiredAnacondaState_LastAcceptedTimeMs;
    private long m_DesiredC100State_LastAcceptedTimeMs;
    private long m_DesiredAlphaState_LastAcceptedTimeMs;
    private short m_VideoClientPort; 
    private short m_VideoClientProgressListeningPort;
    private boolean m_VideoConversionInProgress;
    private boolean m_SatCommImageTxInProgress;
    private BlockingQueue<byte[]> m_GuaranteedVideoSegmentQueue = new LinkedBlockingQueue<byte[]>();
    private static final int GUARANTEED_IMAGE_SEGMENT_RECEIVE_TIMEOUT_MS = 10000;
    private static final Object m_SatCommImageRxSizeLock = new Object();
    
    //Fields received through critical downlink report, only relevant to GCS
    double m_CurrentAltitudeMslM;
    double m_CurrentLatitudeDeg;
    double m_CurrentIASMps;
    double m_CurrentLongitudeDeg;
    double m_CurrentEngineRPM;
    double m_CurrentHeadingRad;
    double m_CurrentRollRad;
    double m_CurrentBusVoltage;
    double m_CurrentPitchRad;
    double m_OutsideAirTempC;
    double m_GndVelNorthMps;
    double m_GndVelEastMps;
    double m_VertVelMps;
    double m_VertAccMps2;
    double m_RemFuelPercent;
    double m_FuelBurnRatePPS;
    double m_CommandedIASMps;
    double m_CurrentOrbitLatitudeDeg;
    boolean m_CurrentOrbitCWDir;
    double m_CurrentOrbitLongitudeDeg;
    double m_CurrentOrbitRadiusM;
    double m_CurrentOrbitAltMslFt;
    double m_LaserAltAglFt;
    boolean m_LaserAltValid;
    double m_WindSpeedMps;
    double m_WindDirFromDeg;
    double m_CurrBaroPressPa;
    double m_CurrSeaLevelPressPa;
    long m_WacsStateTimestampMs;
    boolean m_AvsmCommsStatus;
    boolean m_WacsCommsStatus;
    boolean m_TcldUplinkStatus;
    boolean m_SatComUplinkStatus;
    boolean m_LoiterLoaded;
    WacsMode m_WacsMode_avsm = WacsMode.NO_MODE;
    boolean m_PiccoloConnected_avsm;
    boolean m_TaseConnected_avsm;
    boolean m_WindConverged;
    boolean m_StrikePointLoaded;
    boolean m_WacsIsPowered;
    long m_CriticalDownlinkFieldsTimestampMs;
    
    //Cloud detection
    short m_RecentParticleDetectionBioPercent;
    long m_RecentParticleDetectionTimestampMs;
    double m_RecentParticleDetectionAltMslFt;
    double m_RecentParticleLatitudeDeg;
    double m_RecentParticleLongitudeDeg;
    short m_RecentMaxLcdBarsAgentIDCode;
    long m_RecentChemicalDetectionTimestampMs;
    double m_RecentChemicalDetectionAltMslFt;
    double m_RecentChemicalLatitudeDeg;
    double m_RecentChemicalLongitudeDeg;
    
        
    public GCSSatCommMessageAbritrator (BeliefManagerWacs belMgr, InetAddress multicastSendVsmAddress, int multicastSendVsmPort, InetAddress multicastRecvVsmAddress, int multicastRecvVsmPort, int beliefNetworkAllowableLatencyMs)
    {
        super (belMgr, TimeSyncDialog.LOCATION_GCS, multicastSendVsmAddress, multicastSendVsmPort, multicastRecvVsmAddress, multicastRecvVsmPort, beliefNetworkAllowableLatencyMs);

        long defaultSendPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.DefaultMessageSendPeriod.Ms", 5000);
        m_ExpTimeChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ExpTimeSendPeriod.Ms", defaultSendPeriodMs);
        m_IbacStateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.IbacStateSendPeriod.Ms", defaultSendPeriodMs);
        m_VideoRecorderStateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.VideoRecorderStateSendPeriod.Ms", defaultSendPeriodMs);
        m_VideoStreamStateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.VideoStreamStateSendPeriod.Ms", defaultSendPeriodMs);        
        m_WacsModeChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.WacsModeSendPeriod.Ms", defaultSendPeriodMs);
        m_WWBChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.WWBSendPeriod.Ms", defaultSendPeriodMs);
        m_ControlAPChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ControlAPSendPeriod.Ms", defaultSendPeriodMs);
        m_TargetChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.TargetSendPeriod.Ms", defaultSendPeriodMs);
        m_AllowInterceptChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.AllowInterceptSendPeriod.Ms", defaultSendPeriodMs);
        m_AnacondaStateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.AnacondaStateSendPeriod.Ms", defaultSendPeriodMs);
        m_C100StateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.C100StateSendPeriod.Ms", defaultSendPeriodMs);
        m_AlphaStateChangeMessagePeriodMs = Config.getConfig().getPropertyAsLong("SatComm.AlphaStateSendPeriod.Ms", defaultSendPeriodMs);
        m_VideoClientPort = (short)Config.getConfig().getPropertyAsInteger("EthernetVideoClient.LocalPort", 4995);
        m_VideoClientProgressListeningPort = 4994;
        
        m_DesiredIbacState_LastAcceptedTimeMs = -1;
        m_DesiredAnacondaState_LastAcceptedTimeMs = -1;
        m_DesiredC100State_LastAcceptedTimeMs = -1;
        m_DesiredAlphaState_LastAcceptedTimeMs = -1;
        m_DesiredRecorderState_LastAcceptedTimeMs = -1;
        m_DesiredVideoStreamState_LastAcceptedTimeMs = -1;
                
        new PacketProcessorThread().start();
        new PacketGeneratorThread().start();
    }
    
    
    private boolean parseReceivedPacket (byte bufferedMsg[], int msgLength)
    {
        //First, verify that packet was received uncorrupted
        int messageType = verifyPacketHeader (bufferedMsg, msgLength);
        boolean crcGood = verifyPacketCrc (bufferedMsg, msgLength);
        
        if (messageType == MSGTYPE_INVALID || !crcGood)
        {
            //Packet got mangled from original version, just throw it away
            System.err.println ("Bad packet received and ignored!");
            return false;
        }
                    
        if ((getPacketDestination(bufferedMsg) & DESTINATION_WACSGCS) == 0)
            return false;   //This message wasn't intended for here, it might be a loopback message.  Ignore it.
        if (parseGenericPackets(messageType, bufferedMsg))
            return false;
        
        if (messageType == SatCommMessageArbitrator.MSGTYPE_RECEIPT) 
        {
            if ((SatCommMessageArbitrator.getPacketDestination(bufferedMsg) & 0xFF) == 0x08) 
            {
                ByteBuffer receiveBuffer = ByteBuffer.wrap(bufferedMsg);
                SatCommMessageArbitrator.checkAndPrintReceiptMessage(bufferedMsg, receiveBuffer);
            } 
            else 
            {
                //Destination field says it shouldn't have gotten here
                System.err.println("Receipt message received that had wrong destination set - ignored!");
            }
            return true;
        }        
        //Get timestamp of the packet, which will be the timestamp of the beliefs created by the message
        long dataTimestampMs = getPacketTimestampMs(bufferedMsg);
        
        //Process message and distrubute contents
        switch (messageType)
        {
            case MSGTYPE_PODSTATUS:
                processWacsPodStatusMsg (bufferedMsg, dataTimestampMs);
                break;
            case MSGTYPE_CRITICALDOWNLINK:
                processCriticalDownlinkMsg (bufferedMsg, dataTimestampMs);
                break;
            case MSGTYPE_PODSDETECTION:
                processWacsPodDetectionMessage (bufferedMsg, dataTimestampMs);
                break;
            case MSGTYPE_PODIMAGEDATASEGMENT:
                processWacsPodImageSegmentMessage (bufferedMsg);
                break;
            case MSGTYPE_PODIMAGETXSIZE:
                processWacsImageTransmissionSizeMessage (bufferedMsg);
                break;
            default:
                System.out.println ("Unknown packet type: " + messageType + " received at " + getPacketTimestampMs(bufferedMsg));
                return false;
        };
        
        //Message parsed succesfully.
        return true;
    }
    
    private void processCriticalDownlinkMsg (byte bufferedMsg[], long dataTimestampMs)
    {
        int bufferPos = MSG_HEADER_SIZE;
        
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        
        short currentAltFt = 0;
        currentAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentAltFt |= (0xFF & bufferedMsg[bufferPos++]);
        int currentLatRadPacked = 0;
        currentLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currentIASPacked = 0;
        currentIASPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int currentLonRadPacked = 0;
        currentLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        short currentEngineRpmPacked = 0;
        currentEngineRpmPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currentHeadingPacked = 0;
        currentHeadingPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentHeadingPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte byte22 = bufferedMsg[bufferPos++];
        short currentRollPacked = 0;
        currentRollPacked |= (0x0F & byte22) << 8;
        currentRollPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currentBusVoltagePacked = 0;
        currentBusVoltagePacked |= (0xF0 & byte22) >> 4;
        
        byte byte24 = bufferedMsg[bufferPos++];
        short currentPitchPacked = 0;
        currentPitchPacked |= (0x0F & byte24) << 8;
        currentPitchPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int currentAvFlightTermState = 0;
        currentAvFlightTermState |= (0x30 & byte24) >> 4;
        int currentAvFlightTermMode = 0;
        currentAvFlightTermMode |= (0x40 & byte24) >> 6;
        
        short currentAvCylinderTempPacked = 0;
        currentAvCylinderTempPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currentAvEngineRotorAirTempPacked = 0;
        currentAvEngineRotorAirTempPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currentOutsideAirTempPacked = 0;
        currentOutsideAirTempPacked |= (0xFF & bufferedMsg[bufferPos++]);
        byte currentGndVelNPacked = 0;
        currentGndVelNPacked |= (0xFF & bufferedMsg[bufferPos++]);
        byte currentGndVelEPacked = 0;
        currentGndVelEPacked |= (0xFF & bufferedMsg[bufferPos++]);
        byte currentVertVelPacked = 0;
        currentVertVelPacked |= (0xFF & bufferedMsg[bufferPos++]);
        byte currentVertAccelPacked = 0;
        currentVertAccelPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short remainingFuelPacked = 0;
        remainingFuelPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short fuelBurnRatePacked = 0;
        fuelBurnRatePacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        short commandedSpeedPacked = 0;
        commandedSpeedPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int orbitLatRadPacked = 0;
        orbitLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        orbitLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        orbitLatRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte byte39 = bufferedMsg[bufferPos++];
        short currentMissionWaypoint = 0;
        currentMissionWaypoint |= (0x7F & byte39);
        short orbitDir = 0;
        orbitDir |= (0x80 & byte39);
        
        int orbitLonRadPacked = 0;
        orbitLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        orbitLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        orbitLonRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short orbitRadiusPacked = 0;
        orbitRadiusPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short orbitAltPacked = 0;
        orbitAltPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        orbitAltPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte byte46 = bufferedMsg[bufferPos++];
        short currentGimbalAzPacked = 0;
        currentGimbalAzPacked |= (0x0F & byte46) << 8;
        currentGimbalAzPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int ignoredValue = 0;
        ignoredValue |= (0xF0 & byte46) >> 4;
        
        byte byte48 = bufferedMsg[bufferPos++];
        short currentGimbalElevPacked = 0;
        currentGimbalElevPacked |= (0x0F & byte48) << 8;
        currentGimbalElevPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int ignoredValue2 = 0;
        ignoredValue |= (0xF0 & byte48) >> 4;
        
        short laserAltPacked = 0;
        laserAltPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        laserAltPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short windSpeedPacked = 0;
        windSpeedPacked |= (0xFF & bufferedMsg[bufferPos++]);
        byte windDirPacked = 0;
        windDirPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currBaroPressPacked = 0;
        currBaroPressPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short currSeaLevelPressPacked = 0;
        currSeaLevelPressPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        long wacsStateTimeSec = 0;
        wacsStateTimeSec |= ((bufferedMsg [bufferPos++]&0xFF) << 24);      //MSB-first Timestamp seconds field
        wacsStateTimeSec |= ((bufferedMsg [bufferPos++]&0xFF) << 16);      //MSB-first Timestamp seconds field
        wacsStateTimeSec |= ((bufferedMsg [bufferPos++]&0xFF) << 8);       //MSB-first Timestamp seconds field
        wacsStateTimeSec |= ((bufferedMsg [bufferPos++]&0xFF));            //MSB-first Timestamp seconds field
        int wacsStateTimeMs = 0;
        wacsStateTimeMs |= ((bufferedMsg [bufferPos++]&0xFF) << 8);      //MSB-first Timestamp milliseconds field
        wacsStateTimeMs |= ((bufferedMsg [bufferPos++]&0xFF));           //MSB-first Timestamp milliseconds field
        
        byte byte62 = bufferedMsg[bufferPos++];
        boolean avsmCommsStatus = ((0x01 & (byte62)) != 0);
        boolean wacsCommsStatus = ((0x01 & (byte62 >> 1)) != 0);
        boolean tcdlUplinkStatus = ((0x01 & (byte62 >> 2)) != 0);
        boolean satComUplinkStatus = ((0x01 & (byte62 >> 3)) != 0);
        boolean wacsLoiterGood = ((0x01 & (byte62 >> 7)) != 0);
        
        byte byte63 = bufferedMsg[bufferPos++];
        int wacsMode = (0x07 & (byte63));
        int avsmMode = (0xF8 & (byte63));
        
        byte byte64 = bufferedMsg[bufferPos++];
        boolean piccoloOperational = ((0x01 & (byte64)) != 0);
        boolean taseOperational = ((0x01 & (byte64 >> 1)) != 0);
        boolean windEstConverged = ((0x01 & (byte64 >> 2)) != 0);
        boolean strikePointLoaded = ((0x01 & (byte64 >> 4)) != 0);
        boolean wacsIsPowered = ((0x01 & (byte64 >> 5)) != 0);
        
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_CurrentAltitudeMslM = currentAltFt*MathConstants.FT2M;
        m_CurrentLatitudeDeg = Math.max(-90, Math.min(90, extractLatDegFromRad3BytePacked(currentLatRadPacked)));
        if (m_CurrentLatitudeDeg == -90)
            m_CurrentLatitudeDeg = 90;
        m_CurrentIASMps = currentIASPacked*77.167/250;
        m_CurrentLongitudeDeg = Math.max(-180, Math.min(180, extractLonDegFromRad3BytePacked(currentLonRadPacked)));
        if (m_CurrentLongitudeDeg == -180)
            m_CurrentLongitudeDeg = 180;
        
        m_CurrentEngineRPM = currentEngineRpmPacked*100;
        m_CurrentHeadingRad = extractHeadingRadFrom2BytePacked(currentHeadingPacked);
        m_CurrentRollRad = extractRollRadFrom12BitPacked(currentRollPacked);
        m_CurrentBusVoltage = currentBusVoltagePacked+16;
        m_CurrentPitchRad = extractPitchRadFrom12BitPacked(currentPitchPacked);
        
        //currentAvFlightTermState;
        //currentAvFlightTermMode;
        //currentAvCylinderTempPacked;
        //currentAvEngineRotorAirTempPacked;
        
        m_OutsideAirTempC = currentOutsideAirTempPacked*0.784314 - 101;
        m_GndVelNorthMps = currentGndVelNPacked*77.167/127;
        m_GndVelEastMps = currentGndVelEPacked*77.167/127;
        m_VertVelMps = currentVertVelPacked/10.0;
        m_VertAccMps2 = currentVertAccelPacked*9.8/100;
        m_RemFuelPercent = remainingFuelPacked/2.0;
        m_FuelBurnRatePPS = fuelBurnRatePacked*.02/255;
        
        m_CommandedIASMps = commandedSpeedPacked*77.167/250;
        m_CurrentOrbitLatitudeDeg = Math.max(-90, Math.min(90, extractLatDegFromRad3BytePacked(orbitLatRadPacked)));
        if (m_CurrentOrbitLatitudeDeg == -90)
            m_CurrentOrbitLatitudeDeg = 90;
        
        //currentMissionWaypoint;
        
        m_CurrentOrbitCWDir = (orbitDir == 0);
        m_CurrentOrbitLongitudeDeg = Math.max(-180, Math.min(180, extractLonDegFromRad3BytePacked(orbitLonRadPacked)));
        if (m_CurrentOrbitLongitudeDeg == -180)
            m_CurrentOrbitLongitudeDeg = 180;
        m_CurrentOrbitRadiusM = orbitRadiusPacked * 20;
        m_CurrentOrbitAltMslFt = orbitAltPacked;
        
        //currentGimbalAzPacked;
        //currentGimbalElevPacked;
        
        if (laserAltPacked == 0x8000)
        {
            m_LaserAltAglFt = 0;
            m_LaserAltValid = false;
        }
        else
        {
            m_LaserAltAglFt = laserAltPacked;
            m_LaserAltValid = true;
        }
        m_WindSpeedMps = windSpeedPacked*77.167/250;
        m_WindDirFromDeg = windDirPacked*2;
        m_CurrBaroPressPa = currBaroPressPacked*110000.0/250;
        m_CurrSeaLevelPressPa = currSeaLevelPressPacked*112500.0/255;
        
        m_WacsStateTimestampMs = wacsStateTimeSec*1000L + wacsStateTimeMs;
        
        m_AvsmCommsStatus = avsmCommsStatus;
        m_WacsCommsStatus = wacsCommsStatus;
        m_TcldUplinkStatus = tcdlUplinkStatus;
        m_SatComUplinkStatus = satComUplinkStatus;
        m_LoiterLoaded = wacsLoiterGood;
        
        if (wacsMode == 0)
            m_WacsMode_avsm = WacsMode.LOITER;
        else if (wacsMode == 1)
            m_WacsMode_avsm = WacsMode.INTERCEPT;
        else
            m_WacsMode_avsm = WacsMode.NO_MODE;
        
        //avsmMode;
        
        m_PiccoloConnected_avsm = piccoloOperational;
        m_TaseConnected_avsm = taseOperational;
        m_WindConverged = windEstConverged;
        m_StrikePointLoaded = strikePointLoaded;
        m_WacsIsPowered = wacsIsPowered;
        
        m_CriticalDownlinkFieldsTimestampMs = dataTimestampMs;        
    }
    
    private void processWacsPodStatusMsg (byte bufferedMsg[], long dataTimestampMs)
    {
        int bufferPos = MSG_HEADER_SIZE;
        
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int podStatusBits = 0;
        podStatusBits |= ((bufferedMsg[bufferPos++]&0xFF) << 24);
        podStatusBits |= ((bufferedMsg[bufferPos++]&0xFF) << 16);
        podStatusBits |= ((bufferedMsg[bufferPos++]&0xFF) << 8);
        podStatusBits |= ((bufferedMsg[bufferPos++]&0xFF));
                        
        byte collTempC = bufferedMsg[bufferPos++];
        byte collHumidity = bufferedMsg[bufferPos++];
        byte trackerTempC = bufferedMsg[bufferPos++];
        byte trackerHumidity = bufferedMsg[bufferPos++];

        int wacsStateBits = (0xFF & bufferedMsg[bufferPos++]);

        short loiterRadiusM = 0;
        loiterRadiusM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        loiterRadiusM |= (0xFF & bufferedMsg[bufferPos++]);
        short interceptRadiusM = 0;
        interceptRadiusM |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        interceptRadiusM |= (0xFF & bufferedMsg[bufferPos++]);
        
        short loiterAltFt = 0;
        loiterAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        loiterAltFt |= (0xFF & bufferedMsg[bufferPos++]);
        short interceptAltFt = 0;
        interceptAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        interceptAltFt |= (0xFF & bufferedMsg[bufferPos++]);
        short offsetLoiterAltFt = 0;
        offsetLoiterAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        offsetLoiterAltFt |= (0xFF & bufferedMsg[bufferPos++]);
        
        int latRadPacked = 0;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int lonRadPacked = 0;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        short targetAltFt = 0;
        targetAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        targetAltFt |= (0xFF & bufferedMsg[bufferPos++]);

        long expTimeSec = 0;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]);
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_CollectorXdLogState = ((0x01 & (podStatusBits)) != 0);
        m_TrackerXdLogState = ((0x01 & (podStatusBits >> 1)) != 0);
        m_CollectorRabbitConnected = ((0x01 & (podStatusBits >> 2)) != 0);
        m_TrackerRabbitConnected = ((0x01 & (podStatusBits >> 3)) != 0);
        m_AnacondaMode = AnacondaModeEnum.fromValue(0x07 & (podStatusBits >> 4));            //3 bits
        m_AnacondaConnected = ((0x01 & (podStatusBits >> 7)) != 0);
        m_IbacMode = ((0x01 & (podStatusBits >> 8)) != 0);
        m_IbacConnected = ((0x01 & (podStatusBits >> 9)) != 0);
        m_C100Mode = ParticleCollectorMode.fromValue(0x07 & (podStatusBits >> 10));               //3 bits
        m_C100Connected = ((0x01 & (podStatusBits >> 13)) != 0);
        m_AlphaMode = ((0x01 & (podStatusBits >> 14)) != 0);
        m_AlphaConnected = ((0x01 & (podStatusBits >> 15)) != 0);
        m_GammaConnected = ((0x01 & (podStatusBits >> 16)) != 0);
        m_DosimeterConnected = ((0x01 & (podStatusBits >> 17)) != 0);
        m_CollectorTHSensorConnected = ((0x01 & (podStatusBits >> 18)) != 0);
        m_TrackerTHSensorConnected = ((0x01 & (podStatusBits >> 19)) != 0);
        m_CollectorFanState = ((0x01 & (podStatusBits >> 20)) != 0);
        m_CollectorHeatState = ((0x01 & (podStatusBits >> 21)) != 0);
        m_CollectorAutoTH = ((0x01 & (podStatusBits >> 22)) != 0);
        m_TrackerFanState = ((0x01 & (podStatusBits >> 23)) != 0);
        m_TrackerHeatState = ((0x01 & (podStatusBits >> 24)) != 0);
        m_TrackerAutoTH = ((0x01 & (podStatusBits >> 25)) != 0);
        m_PiccoloConnected = ((0x01 & (podStatusBits >> 26)) != 0);
        m_TASEConnected = ((0x01 & (podStatusBits >> 27)) != 0);
        m_IRVideoRecState = ((0x01 & (podStatusBits >> 28)) != 0);
        m_PODVideoStreamState = ((0x01 & (podStatusBits >> 29)) != 0);
        m_PODVideoConversionFinished = ((0x01 & (podStatusBits >> 30)) != 0);
        m_SatCommImageTxFinished = ((0x01 & (podStatusBits >> 31)) != 0);
        
        m_CollectorTempC = collTempC;
        m_CollectorHumidity = collHumidity;
        m_TrackerTempC = trackerTempC;
        m_TrackerHumidity = trackerHumidity;

        m_WacsMode = WacsMode.fromValue(0x07 & wacsStateBits);                  //3 bits
        m_AllowInterceptPermitted = ((0x01 & (wacsStateBits >> 3)) != 0);
        m_AllowInterceptRecommended = ((0x01 & (wacsStateBits >> 4)) != 0);
        m_ControlEnabledViaAutopilot = ((0x01 & (wacsStateBits >> 5)) != 0);
        m_WacsIsFullyAutonomous = ((0x01 & (wacsStateBits >> 6)) != 0);        
            
        m_LoiterRadiusM = loiterRadiusM;
        m_InterceptRadiusM = interceptRadiusM;
        
        m_LoiterAltitudeAglFt = loiterAltFt;
        m_InterceptAltitudeAglFt = interceptAltFt;
        m_OffsetLoiterAltitudeAglFt = offsetLoiterAltFt;
        
        m_CurrentTargetLatitudeDeg = Math.max(-90, Math.min(90, extractLatDegFromRad3BytePacked(latRadPacked)));
        if (m_CurrentTargetLatitudeDeg == -90)
            m_CurrentTargetLatitudeDeg = 90;
        m_CurrentTargetLongitudeDeg = Math.max(-180, Math.min(180, extractLonDegFromRad3BytePacked(lonRadPacked)));
        if (m_CurrentTargetLongitudeDeg == -180)
            m_CurrentTargetLongitudeDeg = 180;
        m_CurrentTargetAltitudeMslFt = targetAltFt + DtedGlobalMap.getDted().getAltitudeMSL(m_CurrentTargetLatitudeDeg, m_CurrentTargetLongitudeDeg);
        
        m_ExpectedExplosionTimeMs = expTimeSec*1000;
        m_PodStatusFieldsTimestampMs = dataTimestampMs;
    }

    
    private class GuarantneedImageReceiveThread extends Thread
    {
        @Override
        public void run()
        {
            System.out.println("GuarantneedImageReceiveThread: GuaratneedImageReceiveThread started!" + "\n");            
            byte[] receivedData = null;            
            
            // send the first segment request with receipt hash = -1, signal ready to receive segments
            setLastReceivedSegmentHash(-1);
            formAndSendVideoSegmentRequest();
                
            while(true)
            {                                
                try
                {
                    receivedData = m_GuaranteedVideoSegmentQueue.poll(GUARANTEED_IMAGE_SEGMENT_RECEIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    
                    if( receivedData == null)
                    {
                        // possible loss of data, tell POD to resend last segmentData
                        System.out.println("GuarantneedImageReceiveThread: Receiver timed out, resending request!" + "\n");
                        formAndSendVideoSegmentRequest();
                    }
                    else
                    {
                        //////////////////////////////
                        //Extract bytes from message buffer
                        //////////////////////////////        
                        int bufferPos = MSG_HEADER_SIZE;                        
                        int segmentCount = 0;
                        segmentCount |= (0xFF & receivedData[bufferPos++]) << 8;
                        segmentCount |= (0xFF & receivedData[bufferPos++]);

                        int segmentID = 0;
                        segmentID |= (0xFF & receivedData[bufferPos++]) << 8;
                        segmentID |= (0xFF & receivedData[bufferPos++]);     
                        
                        int payloadLength = 0;
                        payloadLength |= (0xFF & receivedData[bufferPos++]);
                        
                        // unpack the actual data segmentData
                        byte[] segmentData = new byte[payloadLength];
                        System.arraycopy(receivedData, bufferPos, segmentData, 0, payloadLength);
                        bufferPos += payloadLength;                           
                        
                        // update most recently received segmentData hash
                        int segmentHash = (BitSet.valueOf(segmentData)).hashCode();
                        setLastReceivedSegmentHash(segmentHash);
                        System.out.println("GuarantneedImageReceiveThread: Received segment: " + segmentID + " of " + segmentCount + ", hash = " + segmentHash + ", size = " + payloadLength + "\n");    
                        formAndSendVideoSegmentRequest();
                        
                        if (m_ReceivedVideoSegments.add(segmentHash))
                        {
                            // send sat comm image transmission progress to video client via udp
                            try
                            {
                                DatagramSocket progressSocket = null;
                                double progress = (double)segmentID / (double)segmentCount;
                                byte[] progressBytes = ByteBuffer.allocate(8).putDouble(progress).array();
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                out.write(progressBytes);
                                byte[] outgoingPacket = out.toByteArray();

                                progressSocket = new DatagramSocket();
                                DatagramPacket packet = new DatagramPacket(outgoingPacket, outgoingPacket.length, InetAddress.getByName("localhost"), m_VideoClientProgressListeningPort);
                                progressSocket.send(packet);   
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                            }
                            
                            if(segmentID == 1)
                            {
                                // received the first segmentData of the image, allocate for N(total segments) - 1 segments since we know their exact size
                                m_ProgressiveImageBuffer = new byte[(segmentCount - 1)*GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE];
                                System.arraycopy(segmentData, 0, m_ProgressiveImageBuffer, (segmentID - 1) * GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE, segmentData.length);
                            }                        
                            else if (segmentID == segmentCount)
                            {                                
                                // received the last segment, allocate a new buffer to hold what has been received so far and the last segment which can have size of 1 to 120
                                byte[] finalImageBuffer = new byte[m_ProgressiveImageBuffer.length + segmentData.length];
                                System.arraycopy(m_ProgressiveImageBuffer, 0, finalImageBuffer, 0, m_ProgressiveImageBuffer.length);
                                System.arraycopy(segmentData, 0, finalImageBuffer, m_ProgressiveImageBuffer.length, segmentData.length);
                                DatagramSocket socket = null;

                                try
                                {
                                    // data packet = data_size + data;
                                    byte[] dataLength = ByteBuffer.allocate(4).putInt(finalImageBuffer.length).array();
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    out.write(dataLength);
                                    out.write(finalImageBuffer);
                                    byte[] outgoingFrame = out.toByteArray();

                                    socket = new DatagramSocket();
                                    DatagramPacket packet = new DatagramPacket(outgoingFrame, outgoingFrame.length, InetAddress.getByName("localhost"), m_VideoClientPort);
                                    System.out.println("GuarantneedImageReceiveThread: Received all segments, sending reconstructed image to video display!" + "\n");
                                    socket.send(packet);      
                                    
                                    // reset state and terminate thread
                                    m_ProgressiveImageBuffer = null;
                                    m_ReceivedVideoSegments.clear();         
                                    
                                    System.out.println("GuarantneedImageReceiveThread: GuarantneedImageReceiveThread terminating...");
                                    return;
                                }
                                catch (SocketException | UnknownHostException ex)
                                {
                                    socket.disconnect();
                                    socket.close();
                                    ex.printStackTrace();
                                }
                                catch (IOException ex)
                                {
                                    ex.printStackTrace();
                                }                                
                            }
                            else
                            {
                                if(m_ProgressiveImageBuffer != null)
                                {
                                    // append received data to the progressive image buffer
                                    System.arraycopy(segmentData, 0, m_ProgressiveImageBuffer, (segmentID - 1) * GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE, segmentData.length);        
                                }
                                else
                                {
                                    System.out.println("GuarantneedImageReceiveThread: received repeated fragments from previous iteration due to network latency, packet ignored" + "\n");
                                }
                            }                        
                        }
                        else
                        {
                            // already received this segmentData
                            System.out.println("GuarantneedImageReceiveThread: Already received " + segmentID + " of " + segmentCount + ", hash = " + segmentHash + "\n");                    
                        }
                    }                                        
                }
                catch (InterruptedException ex)
                {
                    System.out.println("GuarantneedImageReceiveThread: GuaratneedImageReceiveThread Blocking Queue poll interrupted!");
                    ex.printStackTrace();
                }
            }
        }       
    }
    
    private void processWacsPodImageSegmentMessage (byte bufferedMsg[])
    {        
        try
        {
            m_GuaranteedVideoSegmentQueue.put(bufferedMsg);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }        
    }    
    
    private void processWacsImageTransmissionSizeMessage (byte bufferedMsg[])
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////        
        int bufferPos = MSG_HEADER_SIZE;
        int transmissionSize = 0;
        transmissionSize |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        transmissionSize |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        transmissionSize |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        transmissionSize |= (0xFF & bufferedMsg[bufferPos++]);
   
        setSatCommImageTransmissionSize(transmissionSize);
        System.out.println("Received sat comm image transmission size = " + transmissionSize);
    }
    
    private void processWacsPodDetectionMessage (byte bufferedMsg[], long dataTimestampMs)
    {
        int bufferPos = MSG_HEADER_SIZE;
        
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int detectionType = 0;
        detectionType |= ((bufferedMsg[bufferPos++]&0xFF));
                        
        int latRadPacked = 0;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        latRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        int lonRadPacked = 0;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        lonRadPacked |= (0xFF & bufferedMsg[bufferPos++]);
        
        short detectionAltFt = 0;
        detectionAltFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        detectionAltFt |= (0xFF & bufferedMsg[bufferPos++]);
        
        int detectionCount = 0;
        detectionCount |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        detectionCount |= (0xFF & bufferedMsg[bufferPos++]);

        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        int recentCloudDetectionType = (0x03 & (detectionType));
        short recentCloudDetectionIdCode = (short)((0x03F & (detectionType >> 2)));
        boolean cloudDetectionValid = false;
        double recentCloudDetectionAltMslFt = 0;
        double recentCloudLatitudeDeg = 0;
        double recentCloudLongitudeDeg = 0;
        if (recentCloudDetectionIdCode != 0x03F)
        {
            cloudDetectionValid = true;

            recentCloudDetectionAltMslFt = detectionAltFt;
            recentCloudLatitudeDeg = Math.max(-90, Math.min(90, extractLatDegFromRad3BytePacked(latRadPacked)));
            if (recentCloudLatitudeDeg == -90)
                recentCloudLatitudeDeg = 90;
            recentCloudLongitudeDeg = Math.max(-180, Math.min(180, extractLonDegFromRad3BytePacked(lonRadPacked)));
            if (recentCloudLongitudeDeg == -180)
                recentCloudLongitudeDeg = 180;
        }
        
        
        if (recentCloudDetectionType == CloudDetection.SOURCE_PARTICLE)
        {
            if (cloudDetectionValid)
            {
                m_RecentParticleDetectionBioPercent = (short)(recentCloudDetectionIdCode*2);
                m_RecentParticleDetectionTimestampMs = dataTimestampMs;
                m_RecentParticleDetectionAltMslFt = recentCloudDetectionAltMslFt;
                m_RecentParticleLatitudeDeg = recentCloudLatitudeDeg;
                m_RecentParticleLongitudeDeg = recentCloudLongitudeDeg;
            }
            
            m_RecentMaxTotalParticleCount_TimestampMs = dataTimestampMs;
            m_RecentMaxTotalParticleCountSinceLastSend = detectionCount;
        }
        else if (recentCloudDetectionType == CloudDetection.SOURCE_CHEMICAL)
        {
            if (cloudDetectionValid)
            {
                m_RecentMaxLcdBarsAgentIDCode = recentCloudDetectionIdCode;
                m_RecentChemicalDetectionTimestampMs = dataTimestampMs;
                m_RecentChemicalDetectionAltMslFt = recentCloudDetectionAltMslFt;
                m_RecentChemicalLatitudeDeg = recentCloudLatitudeDeg;
                m_RecentChemicalLongitudeDeg = recentCloudLongitudeDeg;
            }
            
            m_RecentMaxLcdBars_TimestampMs = dataTimestampMs;
            m_RecentMaxLcdBarsSinceLastSend = detectionCount;
        }
    }
    
    private void publishCurrentBeliefs ()
    {
        //Here, we need the actual beliefs, not the current satcomm beliefs, for fields that are updated by the status message
        //Since the timestamp in the status message is only so useful, we need to compare the timestamp of the status message
        //vs the actual belief, and only overwrite if the status message is much newer than the actual belief
        //So far, this is only needed for beliefs created on the GCS for data received through the status message
        
        //Pod Heartbeat Message
        CBRNHeartbeatBelief cbrnHB = (CBRNHeartbeatBelief)m_BeliefManager.get(CBRNHeartbeatBelief.BELIEF_NAME, true);

        //Anaconda Actual State
        AnacondaActualStateBelief anBelief = (AnacondaActualStateBelief)m_BeliefManager.get(AnacondaActualStateBelief.BELIEF_NAME, true);

        //Ibac Actual State
        IbacActualStateBelief iBelief = (IbacActualStateBelief)m_BeliefManager.get(IbacActualStateBelief.BELIEF_NAME, true);

        //C110 Actual Commanded State
        ParticleCollectorActualStateBelief caBelief = (ParticleCollectorActualStateBelief)m_BeliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME, true);

        //Alpha Actual State
        AlphaSensorActualStateBelief aBelief = (AlphaSensorActualStateBelief)m_BeliefManager.get(AlphaSensorActualStateBelief.BELIEF_NAME, true);

        //Gamma Statistics
        GammaStatisticsBelief gBelief = (GammaStatisticsBelief)m_BeliefManager.get(GammaStatisticsBelief.BELIEF_NAME, true);

        //Piccolo Position Belief
        METPositionBelief mpBelief = (METPositionBelief)m_BeliefManager.get(METPositionBelief.BELIEF_NAME, true);
        
        //Piccolo Telemetry belief
        PiccoloTelemetryBelief ptBelief = (PiccoloTelemetryBelief) m_BeliefManager.get(PiccoloTelemetryBelief.BELIEF_NAME, true);
        
        //Agent Position belief
        AgentPositionBelief agPosBelief = (AgentPositionBelief) m_BeliefManager.get(AgentPositionBelief.BELIEF_NAME, true);

        //TASE Telemetry Belief
        TASETelemetryBelief ttBelief = (TASETelemetryBelief)m_BeliefManager.get(TASETelemetryBelief.BELIEF_NAME, true);

        //WACS Agent Mode
        AgentModeActualBelief agModeBlf = (AgentModeActualBelief) m_BeliefManager.get(AgentModeActualBelief.BELIEF_NAME, true);

        //Recommend Allow Intercept
        IrExplosionAlgorithmEnabledBelief allowBlf = (IrExplosionAlgorithmEnabledBelief) m_BeliefManager.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME, true);

        //WACS Waypoint Belief
        WACSWaypointCommandedBelief wwBelief = (WACSWaypointCommandedBelief)m_BeliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME, true);

        //Gimbal Target belief
        TargetCommandedBelief targetsBelief = (TargetCommandedBelief)m_BeliefManager.get(TargetCommandedBelief.BELIEF_NAME, true);

        //Expected Explosion time belief
        ExplosionTimeActualBelief expTimeBelief = (ExplosionTimeActualBelief) m_BeliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME, true);
        
        //Enable control via autopilot belief
        EnableAutopilotControlActualBelief apControlEnabledBelief = (EnableAutopilotControlActualBelief) m_BeliefManager.get(EnableAutopilotControlActualBelief.BELIEF_NAME, true);

        //Video Streaming Status Belief
        VideoClientStreamStatusBelief videoStreamStatusBelief = (VideoClientStreamStatusBelief) m_BeliefManager.get(VideoClientStreamStatusBelief.BELIEF_NAME, true);
        
        //Video Recorder Status Belief
        VideoClientRecorderStatusBelief videoRecorderStatusBelief = (VideoClientRecorderStatusBelief) m_BeliefManager.get(VideoClientRecorderStatusBelief.BELIEF_NAME, true);        
                    
        Date podStatusDate = new Date (m_PodStatusFieldsTimestampMs);
        Date podStatusLatencyDate = new Date (m_PodStatusFieldsTimestampMs - m_BeliefNetworkAllowableLatencyMs);
        long podStatusFieldsTimestampSec = m_PodStatusFieldsTimestampMs/1000;
        
        Date criticalDownlinkDate = new Date (m_CriticalDownlinkFieldsTimestampMs);
        Date criticalDownlinkLatencyDate = new Date (m_CriticalDownlinkFieldsTimestampMs - m_BeliefNetworkAllowableLatencyMs);
        Date criticalDownlinkWacsStateDate = new Date (m_WacsStateTimestampMs);
        boolean useCriticalDownlinkWacsStateFields = criticalDownlinkWacsStateDate.after(podStatusDate);
        
        
        //Pod Heartbeat Message
        if (cbrnHB == null || (cbrnHB.getTimeStamp().before(podStatusLatencyDate) && (cbrnHB.getTimestampMs(cbrnPodsInterface.COLLECTOR_POD) > 0 || cbrnHB.getTimestampMs(cbrnPodsInterface.TRACKER_POD) > 0)))
        {
            CBRNHeartbeatBeliefSatComm oldCbrnHBSatComm = (CBRNHeartbeatBeliefSatComm)m_BeliefManager.get(CBRNHeartbeatBeliefSatComm.BELIEF_NAME);
            
            podHeartbeatMessage pod0Msg = new podHeartbeatMessage();
            pod0Msg.setPodNumber (cbrnPodsInterface.COLLECTOR_POD);
            podHeartbeatMessage pod1Msg = new podHeartbeatMessage();
            pod1Msg.setPodNumber (cbrnPodsInterface.TRACKER_POD);

            //Pod 0 Heartbeat
            if (m_CollectorRabbitConnected)
                pod0Msg.setTimestampMs(m_PodStatusFieldsTimestampMs);
            else
                pod0Msg.setTimestampMs(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getTimestampMs(cbrnPodsInterface.COLLECTOR_POD):-1);

            //Pod 1 Heartbeat
            if (m_TrackerRabbitConnected)
                pod1Msg.setTimestampMs(m_PodStatusFieldsTimestampMs);
            else
                pod1Msg.setTimestampMs(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getTimestampMs(cbrnPodsInterface.TRACKER_POD):-1);

            //XD Logging State
            pod0Msg.setActualLogStateOn (m_CollectorXdLogState);
            pod1Msg.setActualLogStateOn (m_TrackerXdLogState);

            //Temp and Humidity Readings
            pod0Msg.setTemperature((float)m_CollectorTempC);
            pod0Msg.setHumidity((float)m_CollectorHumidity);
            pod1Msg.setTemperature((float)m_TrackerTempC);
            pod1Msg.setHumidity((float)m_TrackerHumidity);

            //Collector Temp Data Update Time
            if (m_CollectorTHSensorConnected)
                pod0Msg.setTemperatureUpdatedSec(podStatusFieldsTimestampSec);
            else
                pod0Msg.setTemperatureUpdatedSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getTemperatureUpdatedSec(cbrnPodsInterface.COLLECTOR_POD):-1);

            //Tracker Temp Data Update Time
            if (m_TrackerTHSensorConnected)
                pod1Msg.setTemperatureUpdatedSec(podStatusFieldsTimestampSec);
            else
                pod1Msg.setTemperatureUpdatedSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getTemperatureUpdatedSec(cbrnPodsInterface.TRACKER_POD):-1);

            //Anaconda Data Update Time
            if (m_AnacondaConnected)
                pod0Msg.setLastERecvSec(podStatusFieldsTimestampSec);
            else
                pod0Msg.setLastERecvSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getLastERecvSec(cbrnPodsInterface.COLLECTOR_POD):-1);

            //C100 Data Update Time
            if (m_C100Connected)
                pod0Msg.setLastCRecvSec(podStatusFieldsTimestampSec);
            else
                pod0Msg.setLastCRecvSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getLastCRecvSec(cbrnPodsInterface.COLLECTOR_POD):-1);

            //Alpha Detector Data Update Time
            if (m_AlphaConnected)
                pod1Msg.setLastCRecvSec(podStatusFieldsTimestampSec);
            else
                pod1Msg.setLastCRecvSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getLastCRecvSec(cbrnPodsInterface.TRACKER_POD):-1);

            //IBAC Data Update Time
            if (m_IbacConnected)
                pod1Msg.setLastERecvSec(podStatusFieldsTimestampSec);
            else
                pod1Msg.setLastERecvSec(oldCbrnHBSatComm!=null?oldCbrnHBSatComm.getLastERecvSec(cbrnPodsInterface.TRACKER_POD):-1);

            //Heater States
            pod0Msg.setHeaterToggledOn(m_CollectorHeatState?1:0);
            pod1Msg.setHeaterToggledOn(m_TrackerHeatState?1:0);

            //Fan States
            pod0Msg.setFanToggledOn(m_CollectorFanState?1:0);
            pod1Msg.setFanToggledOn(m_TrackerFanState?1:0);
            
            //Auto TH States
            pod0Msg.setFanManualOverride(!m_CollectorAutoTH);
            pod0Msg.setHeaterManualOverride(!m_CollectorAutoTH);
            pod0Msg.setServo0ManualOverride(!m_CollectorAutoTH);
            pod1Msg.setFanManualOverride(!m_TrackerAutoTH);
            pod1Msg.setHeaterManualOverride(!m_TrackerAutoTH);
            pod1Msg.setServo0ManualOverride(!m_TrackerAutoTH);
                    
            CBRNHeartbeatBeliefSatComm cbrnHBSatComm = new CBRNHeartbeatBeliefSatComm(m_BeliefManager.getName(), pod0Msg, pod1Msg, podStatusDate);
            m_BeliefManager.put(cbrnHBSatComm);
        }
        
        //Enable control via autopilot 
        if (apControlEnabledBelief == null || (apControlEnabledBelief.getTimeStamp().before(podStatusLatencyDate) && apControlEnabledBelief.getAllow()!= m_ControlEnabledViaAutopilot))
        {
            EnableAutopilotControlActualBeliefSatComm controlAutopilotSatComm = new EnableAutopilotControlActualBeliefSatComm (m_BeliefManager.getName(), m_ControlEnabledViaAutopilot, podStatusDate);
            m_BeliefManager.put(controlAutopilotSatComm);
        }
        
        //Anaconda Actual State
        if (anBelief == null || (anBelief.getTimeStamp().before(podStatusLatencyDate) && anBelief.getAnacondState() != m_AnacondaMode))
        {
            AnacondaActualStateBeliefSatComm anBeliefSatComm = new AnacondaActualStateBeliefSatComm(m_BeliefManager.getName(), m_AnacondaMode, podStatusDate);
            m_BeliefManager.put (anBeliefSatComm);
        }
        
        //Ibac Actual State
        if (iBelief == null || (iBelief.getTimeStamp().before(podStatusLatencyDate) && iBelief.getState() != m_IbacMode))
        {
            IbacActualStateBeliefSatComm iBeliefSatComm = new IbacActualStateBeliefSatComm(m_BeliefManager.getName(), m_IbacMode, podStatusDate);
            m_BeliefManager.put (iBeliefSatComm);
        }
        
        //C110 Actual Commanded State
        if (caBelief == null || (caBelief.getTimeStamp().before(podStatusLatencyDate) && caBelief.getParticleCollectorState() != m_C100Mode))
        {
            ParticleCollectorActualStateBeliefSatComm caBeliefSatComm = new ParticleCollectorActualStateBeliefSatComm(m_BeliefManager.getName(), m_C100Mode, podStatusDate);
            m_BeliefManager.put (caBeliefSatComm);
        }
        
        //Alpha Actual State
        if (aBelief == null || (aBelief.getTimeStamp().before(podStatusLatencyDate) && aBelief.getState() != m_AlphaMode))
        {
            AlphaSensorActualStateBeliefSatComm aBeliefSatComm = new AlphaSensorActualStateBeliefSatComm(m_BeliefManager.getName(), m_AlphaMode, podStatusDate);
            m_BeliefManager.put (aBeliefSatComm);
        }
        
        //Gamma Statistics
        if (gBelief == null || gBelief.getTimeStamp().before(podStatusLatencyDate))
        {
            GammaStatisticsBeliefSatComm gBeliefSatComm = new GammaStatisticsBeliefSatComm(m_BeliefManager.getName(), new Date (m_GammaConnected?m_PodStatusFieldsTimestampMs:-1));
            m_BeliefManager.put (gBeliefSatComm);
        }

        if (useCriticalDownlinkWacsStateFields)
        {
            //Update WACS MET belief based on data in critical downlink message (it's newer than the pod status message)
            checkAndUpdateMetPositionBelief (mpBelief, criticalDownlinkLatencyDate, m_PiccoloConnected_avsm, m_CriticalDownlinkFieldsTimestampMs);
        }
        else
        {
            //Update WACS MET belief based on data in pod status message (it's newer than the critical downlink report)
            checkAndUpdateMetPositionBelief (mpBelief, podStatusLatencyDate, m_PiccoloConnected, m_PodStatusFieldsTimestampMs);
        }
        
        //Piccolo telemetry belief
        if (ptBelief == null || ptBelief.getTimeStamp().before(criticalDownlinkLatencyDate))
        {
            if (m_PiccoloConnected_avsm)
            {
                Pic_Telemetry picTelem = new Pic_Telemetry();
                picTelem.Lat = m_CurrentLatitudeDeg;
                picTelem.Lon = m_CurrentLongitudeDeg;
                picTelem.AltWGS84 = MathUtils.convertMslToAltAbvEllip(picTelem.Lat, picTelem.Lon, m_CurrentAltitudeMslM);
                picTelem.AltMSL = m_CurrentAltitudeMslM;
                picTelem.AltLaser_m = m_LaserAltAglFt*MathConstants.FT2M;
                picTelem.Roll = m_CurrentRollRad/MathConstants.DEG2RAD;
                picTelem.Pitch = m_CurrentPitchRad/MathConstants.DEG2RAD;
                picTelem.Yaw = m_CurrentHeadingRad/MathConstants.DEG2RAD;
                picTelem.TrueHeading = picTelem.Yaw;
                picTelem.WindSouth = m_WindSpeedMps*Math.sin(m_WindDirFromDeg);
                picTelem.WindWest = m_WindSpeedMps*Math.cos(m_WindDirFromDeg);
                picTelem.VelNorth = m_GndVelNorthMps;
                picTelem.VelEast = m_GndVelEastMps ;
                picTelem.VelDown = -m_VertVelMps;
                picTelem.IndAirSpeed_mps = m_CurrentIASMps;
                picTelem.PDOP = 0;
                picTelem.GPS_Status = 0;
                picTelem.StaticPressPa = m_CurrBaroPressPa;
                picTelem.OutsideAirTempC = m_OutsideAirTempC;
                picTelem.AltLaserValid = m_LaserAltValid;
    
                PiccoloTelemetryBeliefSatComm ptBeliefSatComm = new PiccoloTelemetryBeliefSatComm(WACSAgent.AGENTNAME, picTelem, criticalDownlinkDate);
                m_BeliefManager.put(ptBeliefSatComm);
            }
        }
        
        //Agent Position belief
        if (agPosBelief == null || agPosBelief.getPositionTimeName(WACSAgent.AGENTNAME) == null || agPosBelief.getPositionTimeName(WACSAgent.AGENTNAME).getTime().before(criticalDownlinkLatencyDate))
        {
            if (m_PiccoloConnected_avsm)
            {
                LatLonAltPosition agentPos = new LatLonAltPosition(new Latitude(m_CurrentLatitudeDeg, Angle.DEGREES), new Longitude(m_CurrentLongitudeDeg, Angle.DEGREES), new Altitude(m_CurrentAltitudeMslM, Length.METERS));
                NavyAngle heading = new NavyAngle(m_CurrentHeadingRad, Angle.RADIANS);
                
                AgentPositionBeliefSatComm agPosSatComm = new AgentPositionBeliefSatComm (WACSAgent.AGENTNAME, agentPos, heading, Length.ZERO, criticalDownlinkDate);
                m_BeliefManager.put(agPosSatComm);
            }
        }
        
        if (useCriticalDownlinkWacsStateFields)
        {
            //Update TASE based on data in critical downlink message (it's newer than the pod status message)
            checkAndUpdateTaseTelemetryBelief (ttBelief, criticalDownlinkLatencyDate, m_TaseConnected_avsm, m_CriticalDownlinkFieldsTimestampMs);
        }
        else
        {
            //Update TASE based on data in pod status message (it's newer than the critical downlink report)
            checkAndUpdateTaseTelemetryBelief (ttBelief, podStatusLatencyDate, m_TASEConnected, m_PodStatusFieldsTimestampMs);
        }
        
        if (useCriticalDownlinkWacsStateFields)
        {
            //Update agent mode based on data in critical downlink message (it's newer than the pod status message)
            checkAndUpdateAgentModeBelief (agModeBlf, criticalDownlinkLatencyDate, m_WacsMode_avsm, criticalDownlinkDate);
        }
        else
        {
            //Update agent mode based on data in pod status message (it's newer than the critical downlink report)
            checkAndUpdateAgentModeBelief (agModeBlf, podStatusLatencyDate, m_WacsMode, podStatusDate);
        }
        
        long timeRemainingMs = m_ExpectedExplosionTimeMs - System.currentTimeMillis();
        //Recommend Allow Intercept
        if (allowBlf == null || (allowBlf.getTimeStamp().before(podStatusLatencyDate) && (allowBlf.getEnabled() != m_AllowInterceptRecommended || allowBlf.getTimeUntilExplosionMs() > timeRemainingMs)))
        {
            IrExplosionAlgorithmEnabledBeliefSatComm allowBlfSatComm = new IrExplosionAlgorithmEnabledBeliefSatComm(m_BeliefManager.getName(), m_AllowInterceptRecommended, timeRemainingMs, podStatusDate);
            m_BeliefManager.put (allowBlfSatComm);
        }
        
        if (useCriticalDownlinkWacsStateFields && m_WacsMode_avsm != null)
        {
            //Update commanded orbit, could be loiter or intercept type orbit
            checkAndUpdateCurrentOrbitBelief (criticalDownlinkLatencyDate, m_WacsMode_avsm, criticalDownlinkDate);        
        }
        else if (!useCriticalDownlinkWacsStateFields && m_WacsMode != null)
        {
            //Update commanded orbit, could be loiter or intercept type orbit
            checkAndUpdateCurrentOrbitBelief (podStatusLatencyDate, m_WacsMode, podStatusDate);        
        }
        
        //If WACS Waypoint Belief received through satcomm is newer than and different than the belief set by the GCS user...
        if (wwBelief == null || (wwBelief.getTimeStamp().before(podStatusLatencyDate) && m_LoiterRadiusM != 0 && m_InterceptRadiusM != 0 && 
                                                        ((Math.abs(wwBelief.getFinalLoiterAltitude().getDoubleValue(Length.FEET) - m_LoiterAltitudeAglFt) > 1.5) ||
                                                         (Math.abs(wwBelief.getStandoffLoiterAltitude().getDoubleValue(Length.FEET) - m_OffsetLoiterAltitudeAglFt) > 1.5) ||
                                                         (Math.abs(wwBelief.getLoiterRadius().getDoubleValue(Length.METERS) - m_LoiterRadiusM) > 1.5) ||
                                                         (Math.abs(wwBelief.getIntersectAltitude().getDoubleValue(Length.FEET) - m_InterceptAltitudeAglFt) > 1.5) ||
                                                         (Math.abs(wwBelief.getIntersectRadius().getDoubleValue(Length.METERS) - m_InterceptRadiusM) > 1.5))))
        {
            Altitude la = new Altitude (m_LoiterAltitudeAglFt, Length.FEET);
            Altitude ola = new Altitude (m_OffsetLoiterAltitudeAglFt, Length.FEET);
            Length lr = new Length (m_LoiterRadiusM, Length.METERS);
            Altitude ia = new Altitude (m_InterceptAltitudeAglFt, Length.FEET);
            Length ir = new Length (m_InterceptRadiusM, Length.METERS);
            WACSWaypointActualBeliefSatComm newWwBeliefSatComm = new WACSWaypointActualBeliefSatComm(m_BeliefManager.getName(), ia, ir, la, ola, lr, podStatusDate);
            
            //if new satcomm belief is different than old satcomm belief, then repost the new one
            WACSWaypointActualBeliefSatComm oldWwBeliefSatComm = (WACSWaypointActualBeliefSatComm)m_BeliefManager.getSatCommBelief(WACSWaypointActualBeliefSatComm.BELIEF_NAME);
            if (oldWwBeliefSatComm == null || 
                    !newWwBeliefSatComm.getFinalLoiterAltitude().equals (oldWwBeliefSatComm.getFinalLoiterAltitude()) || 
                    !newWwBeliefSatComm.getIntersectAltitude().equals (oldWwBeliefSatComm.getIntersectAltitude()) || 
                    !newWwBeliefSatComm.getIntersectRadius().equals (oldWwBeliefSatComm.getIntersectRadius()) || 
                    !newWwBeliefSatComm.getLoiterRadius().equals (oldWwBeliefSatComm.getLoiterRadius()) || 
                    !newWwBeliefSatComm.getStandoffLoiterAltitude().equals (oldWwBeliefSatComm.getStandoffLoiterAltitude())
                    )
            {
                m_BeliefManager.put (newWwBeliefSatComm);
            }
            
        }
        
        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
        //Gimbal Target belief
        if (targetsBelief == null || (targetsBelief.getTimeStamp().before(podStatusLatencyDate) && m_CurrentTargetAltitudeMslFt != 0 && 
                                                        (targetsBelief.getPositionTimeName(tmp) == null ||
                                                         (Math.abs(targetsBelief.getPositionTimeName(tmp).getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) - m_CurrentTargetLatitudeDeg) > .00003) ||
                                                         (Math.abs(targetsBelief.getPositionTimeName(tmp).getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) - m_CurrentTargetLongitudeDeg) > .00003) ||
                                                         (Math.abs(targetsBelief.getPositionTimeName(tmp).getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET) - m_CurrentTargetAltitudeMslFt) > 1.5))))
        {
            LatLonAltPosition targetPosition = new LatLonAltPosition(new Latitude (m_CurrentTargetLatitudeDeg, Angle.DEGREES), new Longitude (m_CurrentTargetLongitudeDeg, Angle.DEGREES), new Altitude(m_CurrentTargetAltitudeMslFt, Length.FEET));
            TargetActualBeliefSatComm targetsBeliefSatComm = new TargetActualBeliefSatComm(m_BeliefManager.getName(), targetPosition, Length.ZERO, tmp, podStatusDate);
            m_BeliefManager.put (targetsBeliefSatComm);
        }
        
        //Expected Explosion time belief
        if (m_ExpectedExplosionTimeMs > 0 && (expTimeBelief == null || (expTimeBelief.getTimeStamp().before(podStatusLatencyDate) && Math.abs(expTimeBelief.getTime_ms() - m_ExpectedExplosionTimeMs) > 1500)))
        {
            ExplosionTimeActualBeliefSatComm expTimeBeliefSatComm = new ExplosionTimeActualBeliefSatComm(m_BeliefManager.getName(), m_ExpectedExplosionTimeMs, podStatusDate);
            m_BeliefManager.put (expTimeBeliefSatComm);
        }
        
        //Video Stream Status Belief
        if (videoStreamStatusBelief == null || (videoStreamStatusBelief.getTimeStamp().before(podStatusLatencyDate) && videoStreamStatusBelief.getStreamState() != m_PODVideoStreamState))
        {
            VideoClientStreamStatusBeliefSatComm vidStreamStatusBeliefSatComm = new VideoClientStreamStatusBeliefSatComm(m_BeliefManager.getName(), m_PODVideoStreamState);
            m_BeliefManager.put(vidStreamStatusBeliefSatComm);
        }
        
        //Video Recorder Status Belief
        if (videoRecorderStatusBelief == null || (videoRecorderStatusBelief.getTimeStamp().before(podStatusLatencyDate) && videoRecorderStatusBelief.getState().isRecording != m_IRVideoRecState))
        {          
            VideoClientRecorderStatusBeliefSatComm vidRecStatusBeliefSatComm = new VideoClientRecorderStatusBeliefSatComm(m_BeliefManager.getName(), new RecorderStatus(false, false, m_IRVideoRecState, false, false, false, false, false));
            m_BeliefManager.put(vidRecStatusBeliefSatComm);
        }
        
        //Video Converter Finished Belief
        if(m_VideoConversionInProgress)
        {
            VideoClientConversionFinishedBelief videoConvesionFinishedBelief = (VideoClientConversionFinishedBelief) m_BeliefManager.get(VideoClientConversionFinishedBelief.BELIEF_NAME, true);
            
            if ( (videoConvesionFinishedBelief == null || (videoConvesionFinishedBelief.getTimeStamp().before(podStatusLatencyDate))) && m_PODVideoConversionFinished)
            {          
                VideoClientConversionFinishedBeliefSatComm vidConvFinishedBeliefSatComm = new VideoClientConversionFinishedBeliefSatComm(m_BeliefManager.getName());
                m_BeliefManager.put (vidConvFinishedBeliefSatComm);  
                m_VideoConversionInProgress = false;
            }            
        }        
        
        //SatComm Image Transmission Finished Belief
        if(m_SatCommImageTxInProgress)
        {
            SatCommImageTransmissionFinishedBelief satCommImageTxFinishedBelief = (SatCommImageTransmissionFinishedBelief) m_BeliefManager.get(SatCommImageTransmissionFinishedBelief.BELIEF_NAME, true);
            
            if ( (satCommImageTxFinishedBelief == null || (satCommImageTxFinishedBelief.getTimeStamp().before(podStatusLatencyDate))) && m_SatCommImageTxFinished)
            {
                System.out.println("Sending SatComm transmission finished belief!!!");
                SatCommImageTransmissionFinishedBeliefSatComm satCommImageTxFinishedBeliefSatComm = new SatCommImageTransmissionFinishedBeliefSatComm(m_BeliefManager.getName());
                m_BeliefManager.put(satCommImageTxFinishedBeliefSatComm);
                m_SatCommImageTxInProgress = false;
            }
        }
        
        //Particle Cloud Detection belief
        CloudDetectionBelief cdBelief = (CloudDetectionBelief) m_BeliefManager.get(CloudDetectionBelief.BELIEF_NAME);
        boolean particleCloudBeliefUpdated = false;
        if (m_RecentParticleDetectionTimestampMs > 0 && (cdBelief == null || (cdBelief.getTimeStamp().getTime() < m_RecentParticleDetectionTimestampMs)))
        {
            LatLonAltPosition lla = new LatLonAltPosition(new Latitude(m_RecentParticleLatitudeDeg, Angle.DEGREES), new Longitude (m_RecentParticleLongitudeDeg, Angle.DEGREES), new Altitude (m_RecentParticleDetectionAltMslFt, Length.FEET));
            CloudDetectionBeliefSatComm cdBeliefSatComm = new CloudDetectionBeliefSatComm (m_BeliefManager.getName(), lla, 0.0, CloudDetection.SOURCE_PARTICLE, (short)(m_RecentParticleDetectionBioPercent/2), (short)m_RecentMaxTotalParticleCountSinceLastSend, m_RecentParticleDetectionTimestampMs);
            m_BeliefManager.put (cdBeliefSatComm);
            particleCloudBeliefUpdated = true;
            m_RecentParticleDetectionTimestampMs = -1;
        }
        
        boolean chemicalCloudBeliefUpdated = false;
        //Chemical Cloud Detection belief
        if (m_RecentChemicalDetectionTimestampMs > 0 && (cdBelief == null || (cdBelief.getTimeStamp().getTime() < m_RecentChemicalDetectionTimestampMs)))
        {
            LatLonAltPosition lla = new LatLonAltPosition(new Latitude(m_RecentChemicalLatitudeDeg, Angle.DEGREES), new Longitude (m_RecentChemicalLongitudeDeg, Angle.DEGREES), new Altitude (m_RecentChemicalDetectionAltMslFt, Length.FEET));
            CloudDetectionBeliefSatComm cdBeliefSatComm = new CloudDetectionBeliefSatComm (m_BeliefManager.getName(), lla, m_RecentMaxLcdBarsSinceLastSend, CloudDetection.SOURCE_CHEMICAL, m_RecentMaxLcdBarsAgentIDCode, (short)m_RecentMaxLcdBarsSinceLastSend, m_RecentChemicalDetectionTimestampMs);
            m_BeliefManager.put (cdBeliefSatComm);
            chemicalCloudBeliefUpdated = true;
            m_RecentChemicalDetectionTimestampMs = -1;
        }

        //Particle Detection Belief
        ParticleDetectionBelief pdBelief = (ParticleDetectionBelief)m_BeliefManager.get(ParticleDetectionBelief.BELIEF_NAME, true);
        if (m_RecentMaxTotalParticleCount_TimestampMs > 0 && (pdBelief == null || (pdBelief.getTimeStamp().getTime() < m_RecentMaxTotalParticleCount_TimestampMs - m_BeliefNetworkAllowableLatencyMs)))
        {
            int bioCount = 0;
            if (particleCloudBeliefUpdated)
                bioCount = (int)(m_RecentMaxTotalParticleCountSinceLastSend*m_RecentParticleDetectionBioPercent/100.0);
            ParticleDetectionBeliefSatComm pdBeliefSatComm = new ParticleDetectionBeliefSatComm(m_BeliefManager.getName(), m_RecentMaxTotalParticleCountSinceLastSend, 0, bioCount, 0, m_RecentMaxTotalParticleCount_TimestampMs);
            m_BeliefManager.put (pdBeliefSatComm);
            m_RecentMaxTotalParticleCount_TimestampMs = -1;
        }
        
        //Anaconda Detection Belief
        AnacondaDetectionBelief adBelief = (AnacondaDetectionBelief)m_BeliefManager.get(AnacondaDetectionBelief.BELIEF_NAME, true);
        if (m_RecentMaxLcdBars_TimestampMs > 0 &&  (adBelief == null || (adBelief.getTimeStamp().getTime() < m_RecentMaxLcdBars_TimestampMs - m_BeliefNetworkAllowableLatencyMs)))
        {
            int agentID = 0;
            if (chemicalCloudBeliefUpdated)
                agentID = (int)(m_RecentMaxLcdBarsAgentIDCode);
            
            if (m_RecentMaxLcdBarsSinceLastSend > 0 && agentID > 0)
            {
                AnacondaDataPair lcdaList[] = new AnacondaDataPair[1];
                lcdaList[0] = anacondaLCDReportMessage.newDataPairInstance();
                lcdaList[0].bars = m_RecentMaxLcdBarsSinceLastSend;
                lcdaList[0].agentID = agentID;
                AnacondaDetectionBelief adBeliefSatComm = new AnacondaDetectionBeliefSatComm(m_BeliefManager.getName(), lcdaList, null, m_RecentMaxLcdBars_TimestampMs);
                m_BeliefManager.put (adBeliefSatComm);
            }
            else
            {
                AnacondaDetectionBelief adBeliefSatComm = new AnacondaDetectionBeliefSatComm(m_BeliefManager.getName(), null, null, m_RecentMaxLcdBars_TimestampMs);
                m_BeliefManager.put (adBeliefSatComm);
            }
            m_RecentMaxLcdBars_TimestampMs = -1;
        }
    }
    
    private void checkAndUpdateCurrentOrbitBelief (Date latencyDate, WacsMode wacsMode, Date orbitDate)
    {
        //Racetrack Orbit Belief
        RacetrackOrbitBelief rtBelief = (RacetrackOrbitBelief)m_BeliefManager.get(RacetrackOrbitBelief.BELIEF_NAME, true);
        
        //Circular Orbit Belief
        CircularOrbitBelief cirBelief = (CircularOrbitBelief)m_BeliefManager.get(CircularOrbitBelief.BELIEF_NAME, true);
        
        if (wacsMode.equals (WacsMode.LOITER))
        {
            //Racetrack (loiter) belief (actual orbit, not proposed definition)
            if (rtBelief == null || (rtBelief.getTimeStamp().before(latencyDate)))
            {
                Latitude lat = new Latitude (m_CurrentOrbitLatitudeDeg, Angle.DEGREES);
                Longitude lon = new Longitude (m_CurrentOrbitLongitudeDeg, Angle.DEGREES);
                
                Altitude finalAlt = new Altitude (m_CurrentOrbitAltMslFt, Length.FEET);
                Altitude standoffAlt = new Altitude (m_CurrentOrbitAltMslFt, Length.FEET);
                //WACS Waypoint Belief
                WACSWaypointActualBelief wwBelief = (WACSWaypointActualBelief)m_BeliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
                if (wwBelief != null)
                {
                    finalAlt = (Altitude)wwBelief.getFinalLoiterAltitude().clone();
                    standoffAlt = (Altitude)wwBelief.getStandoffLoiterAltitude().clone();
                }
        
                boolean clockwise = m_CurrentOrbitCWDir;
                Length radius = new Length (m_CurrentOrbitRadiusM, Length.METERS);
                
                if (radius.isGreaterThan(Length.ZERO))
                {
                    if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                    {
                        TestCircularOrbitBeliefSatComm tobBeliefSatComm = new TestCircularOrbitBeliefSatComm (WACSAgent.AGENTNAME, lat, lon, finalAlt, standoffAlt, radius, clockwise, 0, 0, 0, orbitDate, null);
                        m_BeliefManager.put(tobBeliefSatComm);
                    }
                    else
                    {
                        RacetrackOrbitBeliefSatComm racetrackBeliefSatCommm = new RacetrackOrbitBeliefSatComm (lat, lon, finalAlt, standoffAlt, radius, clockwise, orbitDate);
                        m_BeliefManager.put (racetrackBeliefSatCommm);
                    }
                }
            }
        }
        else if (wacsMode.equals (WacsMode.INTERCEPT))
        {
            //Circular (intrecept) belief 
            if (cirBelief == null || (cirBelief.getTimeStamp().before(latencyDate)))
            {
                Latitude lat = new Latitude (m_CurrentOrbitLatitudeDeg, Angle.DEGREES);
                Longitude lon = new Longitude (m_CurrentOrbitLongitudeDeg, Angle.DEGREES);
                Altitude alt = new Altitude (m_CurrentOrbitAltMslFt, Length.FEET);
                LatLonAltPosition centerPosition = new LatLonAltPosition(lat, lon, alt);
                boolean clockwise = m_CurrentOrbitCWDir;
                Length radius = new Length (m_CurrentOrbitRadiusM, Length.METERS);

                if (radius.isGreaterThan(Length.ZERO))
                {
                    if (Config.getConfig().getPropertyAsBoolean("WACSAgent.Display.ShowDestinationOrbitsOnly", false))
                    {
                        TestCircularOrbitBeliefSatComm tobBeliefSatComm = new TestCircularOrbitBeliefSatComm(WACSAgent.AGENTNAME, new LatLonAltPosition(lat, lon, alt), radius, clockwise, 0, 0, 0, orbitDate);
                        m_BeliefManager.put(tobBeliefSatComm);
                    }
                    else
                    {
                        CircularOrbitBeliefSatComm circularBeliefSatCommm = new CircularOrbitBeliefSatComm (WACSAgent.AGENTNAME, centerPosition, radius, clockwise, orbitDate);
                        m_BeliefManager.put (circularBeliefSatCommm);
                    }
                }
            }
        }
    }
    
    private void checkAndUpdateMetPositionBelief (METPositionBelief mpBelief, Date latencyDate, boolean piccoloConnected, long piccoloStatusTimestampMs)
    {
        //Piccolo Position Belief
        if (mpBelief == null || mpBelief.getTimeStamp().before(latencyDate))
        {
            if (piccoloConnected)
            {
                METPositionBeliefSatComm oldMpBeliefSatComm = (METPositionBeliefSatComm)m_BeliefManager.get(METPositionBeliefSatComm.BELIEF_NAME);
                long lastTime = -1;
                if (oldMpBeliefSatComm != null)
                    lastTime = oldMpBeliefSatComm.getTimeStamp().getTime();
                
                METPositionTimeName metTN = new METPositionTimeName(m_BeliefManager.getName(), NavyAngle.NORTH, Speed.ZERO, LatLonAltPosition.ORIGIN, new Date (piccoloConnected?piccoloStatusTimestampMs:lastTime));
                METPositionBeliefSatComm mpBeliefSatComm = new METPositionBeliefSatComm(m_BeliefManager.getName(), metTN, metTN.getTime());
                m_BeliefManager.put (mpBeliefSatComm);
            }
        }
    }
    
    private void checkAndUpdateTaseTelemetryBelief (TASETelemetryBelief ttBelief, Date latencyDate, boolean taseConnected, long taseStatusTimestampMs)
    {
        if (ttBelief == null || ttBelief.getTimeStamp().before(latencyDate))
        {
            if (taseConnected)
            {
                TASETelemetryBeliefSatComm oldTtBeliefSatComm = (TASETelemetryBeliefSatComm)m_BeliefManager.get(TASETelemetryBeliefSatComm.BELIEF_NAME);
                long lastTime = -1;
                if (oldTtBeliefSatComm != null)
                    lastTime = oldTtBeliefSatComm.getTimeStamp().getTime();

                //TASE Telemetry Belief
                TASE_Telemetry tTelem = new TASE_Telemetry();
                TASETelemetryBeliefSatComm ttBeliefSatComm = new TASETelemetryBeliefSatComm(m_BeliefManager.getName(), tTelem, new Date (taseConnected?taseStatusTimestampMs:lastTime));
                m_BeliefManager.put (ttBeliefSatComm);
            }
        }
    }
    
    private void checkAndUpdateAgentModeBelief (AgentModeActualBelief agModeBlf, Date latencyDate, WacsMode wacsMode, Date modeDate)
    {
        if (agModeBlf == null || (agModeBlf.getTimeStamp().before(latencyDate) && (agModeBlf.getMode(WACSAgent.AGENTNAME) == null || !agModeBlf.getMode(WACSAgent.AGENTNAME).getName().equals (wacsMode.getModeString()))))
        {
            if (wacsMode != null && wacsMode != WacsMode.NO_MODE)
            {
                AgentModeActualBeliefSatComm agModeBlfSatComm = new AgentModeActualBeliefSatComm(WACSAgent.AGENTNAME, wacsMode.getModeString(), modeDate);
                m_BeliefManager.put (agModeBlfSatComm);
            }
        }
    }
    
    public boolean formAndSendExpectedExplosionTime ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETEXPLOSIONTIME;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETEXPLOSIONTIME;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredExplosionTime_TimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentExpTime_TimestampMs = System.currentTimeMillis();
        long explosionTimeSec = m_DesiredExplosionTimeMs/1000;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (explosionTimeSec >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (explosionTimeSec >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (explosionTimeSec >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (explosionTimeSec));
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendAlphaState ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETALPHASTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETALPHASTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredAlphaState_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentAlphaState_TimestampMs = System.currentTimeMillis();
        boolean state = m_DesiredAlphaState;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendVideoSegmentRequest()
    {        
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETVIDEOSEGMENTRECEIVED;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETVIDEOSEGMENTRECEIVED;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_LastReceivedVideoSegment_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;

        ///////////////////////////////////////////////////////
        //Define message fields      
        int segmentHash = getLastReceivedSegmentHash();      
        System.out.println("GCS_SATCOMM_ARBITRATOR: Sending segment request with receipt hash = " + segmentHash);
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));  
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (segmentHash >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (segmentHash >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (segmentHash >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & segmentHash);        
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType); 
    }
    
    public boolean formAndSendVideoConverterState()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETVIDEOCONVERTERSTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETVIDEOCONVERTERSTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_DesiredVideoConverterState_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        // no message field needed        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));                        
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);            
    }
    
    public boolean formAndSendRecorderState()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;  
        int cmdCode = GCSCMDCODE_SETVIDEORECORDERSTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETVIDEORECORDERSTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_DesiredVideoRecorderState_TimeMs;        
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields        
        boolean state = m_DesiredVideoRecorderState;
        m_LastSentRecorderState_TimestampMs = System.currentTimeMillis();
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));                        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);    
    }
    
    public boolean formAndSendVideoStreamState()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;  
        int cmdCode = GCSCMDCODE_SETVIDEOSTREAMSTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETVIDEOSTREAMSTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_DesiredVideoStreamState_TimeMs;        
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields        
        boolean state = m_DesiredVideoStreamState;
        int[] host = m_DesiredClientHost;
        short port = m_DesiredClientPort;
        m_LastSentVideoStreamState_TimestampMs = System.currentTimeMillis();
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));                        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        sendBuffer[bufferPos++] = (byte)(0xFF & (host[0]));
        sendBuffer[bufferPos++] = (byte)(0xFF & (host[1]));
        sendBuffer[bufferPos++] = (byte)(0xFF & (host[2]));
        sendBuffer[bufferPos++] = (byte)(0xFF & (host[3]));
        sendBuffer[bufferPos++] = (byte)(0xFF & (port >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (port));
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);    
    }
    
    public boolean formAndSendGuaranteedFrameRequest()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;  
        int cmdCode = GCSCMDCODE_SETGUARANTEEDFRAMEREQUEST;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETGUARANTEEDFRAMEREQUEST;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_DesiredGuaranteedFrameRequest_TimeMs;        
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields        
        int receipt = m_DesiredGuaranteedDataReceipt;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));           
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (receipt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & receipt);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);         
    }
    
    public boolean formAndSendFrameRequest()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETFRAMEREQUEST;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETFRAMEREQUEST;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;        
        long msgTimeMs = m_DesiredFrameRequest_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);        
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        // no message field needed        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));                
        sendBuffer[bufferPos++] = (byte)(0);
        

        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);                
    }
    
    public boolean formAndSendIbacState ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETIBACSTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETIBACSTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredIbacState_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentIbacState_TimestampMs = System.currentTimeMillis();
        boolean state = m_DesiredIbacState;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendAnacondaState()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETANACONDASTATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETANACONDASTATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredAnacondaState_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentAnacondaState_TimestampMs = System.currentTimeMillis();
        int state = m_DesiredAnacondaState.getValue();
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendC100State()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETC100STATE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETC100STATE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredC100State_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentC100State_TimestampMs = System.currentTimeMillis();
        int state = m_DesiredC100State.getValue();
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendWacsMode ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETWACSMODE;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETWACSMODE;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredWacsMode_TimeMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentWacsMode_TimestampMs = System.currentTimeMillis();
        WacsMode mode = m_DesiredWacsMode;
        int modePacket = (mode.getValue() & 0x07);
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (modePacket));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendWacsWaypointSettings ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETWACSWAYPOINTSETTINGS;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETWACSWAYPOINTSETTINGS;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredWWB_TimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentWWB_TimestampMs = System.currentTimeMillis();
        int loiterFinalAltFt = (int)(Math.round(m_DesiredLoiterAltitudeAglFt));
        int loiterOffsetAltFt = (int)(Math.round(m_DesiredOffsetLoiterAltitudeAglFt));
        int loiterRadiusFt = (int)(Math.round(m_DesiredLoiterRadiusM/MathConstants.FT2M));
        int interceptAltFt = (int)(Math.round(m_DesiredInterceptAltitudeAglFt));
        int interceptRadiusFt = (int)(Math.round(m_DesiredInterceptRadiusM/MathConstants.FT2M));        
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterFinalAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterFinalAltFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterOffsetAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterOffsetAltFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterRadiusFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterRadiusFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptAltFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptRadiusFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptRadiusFt));
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendControlAP ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETCONTROLAP;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETCONTROLAP;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredControlAP_TimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentControlAP_TimestampMs = System.currentTimeMillis();
        boolean state = m_DesiredControlAP;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendAllowIntercept ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETALLOWINTERCEPT;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETALLOWINTERCEPT;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredAllowIntercept_TimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentAllowIntercept_TimestampMs = System.currentTimeMillis();
        boolean state = m_DesiredAllowIntercept;
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (state?1:0));
        sendBuffer[bufferPos++] = (byte)(0);
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public boolean formAndSendDesiredTarget ()
    {
        int msgDestination = DESTINATION_WACSPOD;
        int msgPriority = 2;
        int msgType = MSGTYPE_GCSCOMMANDMESSAGE;
        int cmdCode = GCSCMDCODE_SETTARGET;
        int cmdParamLength = GCSCMDPARAMLENGTH_SETTARGET;
        int fullMsgLength = MSGLENGTH_GCSCOMMANDMESSAGE_PLUS + cmdParamLength;
        
        //Message timestamp is based on when the data was created, not message is formed
        long msgTimeMs = m_DesiredTargetTimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_LastSentTarget_TimestampMs = System.currentTimeMillis();
        float targetAltMslFt = (float)m_DesiredTargetAltitudeMslFt;
        int targetAltMslFtINT = Float.floatToIntBits(targetAltMslFt);
        double targetLatDeg = m_DesiredTargetLatitudeDeg;
        long targetLatDegLONG = Double.doubleToLongBits(targetLatDeg);
        double targetLonDeg = m_DesiredTargetLongitudeDeg;
        long targetLonDegLONG = Double.doubleToLongBits(targetLonDeg);
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdCode));
        sendBuffer[bufferPos++] = (byte)(0xFF & (cmdParamLength));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltMslFtINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltMslFtINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltMslFtINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltMslFtINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLatDegLONG));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetLonDegLONG));
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
        
    private void setSatCommImageTransmissionSize(int transmissionSize)
    {
        synchronized(m_SatCommImageRxSizeLock)
        {
            m_SatCommImageTransmissionSize = transmissionSize;
        }
    }
    
    private int getSatCommImageTransmissionSize()
    {
        int transmissionSize;
        
        synchronized(m_SatCommImageRxSizeLock)
        {
            transmissionSize = m_SatCommImageTransmissionSize;
        }
        return transmissionSize;
    }
    
    static public void main (String args[])
    {
        try
        {
            byte multicastList[] = new byte[] {(byte)233,(byte)1,(byte)3,(byte)3};
            InetAddress multicastIpAddr = Inet4Address.getByAddress(multicastList);
            int multicastSendVsmPort = 57190;
            int multicastRecvVsmPort = 57191;
            int podStatusPeriodMs = 3000;
            int connectivityTimeoutSec = 20;
            int beliefNetworkLatency = 5000;

            GCSSatCommMessageAbritrator testObj = new GCSSatCommMessageAbritrator(null, multicastIpAddr, multicastSendVsmPort, multicastIpAddr, multicastRecvVsmPort, beliefNetworkLatency);
            
            byte buf[] = {126, 10, 64, -126, 81, 7, -53, -126, 0, 78, 0, 0, 0, 0, -44, -1, -44, -1, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0, -128, 0, 0, 16, -38, 0, 0, 0, 0, 0, 104, -113};
            testObj.parseReceivedPacket(buf, buf.length);
            System.exit (0);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
    
    private class PacketGeneratorThread extends Thread
    {
        public PacketGeneratorThread ()
        {
            setDaemon (true);
        }
        
        public void run ()
        {
            while (true)
            {
                if (m_BeliefManager != null)
                {
                    long currTimeMs = System.currentTimeMillis();
                    
                    //Expected Explosion time belief (as set by user)
                    ExplosionTimeCommandedBelief expTimeBelief = (ExplosionTimeCommandedBelief) m_BeliefManager.get(ExplosionTimeCommandedBelief.BELIEF_NAME, true);
                            
                    if(expTimeBelief != null)
                    {
                        long desiredTimeMs = expTimeBelief.getTime_ms();
                        desiredTimeMs = 1000*Math.min(Integer.MAX_VALUE-1, desiredTimeMs/1000);
                        
                        //If expected explosion time (read from WACS POD STATUS message) differs from the current belief (set by GCS user)
                        if (Math.abs(m_ExpectedExplosionTimeMs - desiredTimeMs) > 2001 && m_PodStatusFieldsTimestampMs > 0)
                        {
                            m_DesiredExplosionTimeMs = desiredTimeMs;
                            
                            m_DesiredExplosionTime_TimestampMs = expTimeBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the explosion time, (to allow for network issues)
                            if (currTimeMs - m_LastSentExpTime_TimestampMs > m_ExpTimeChangeMessagePeriodMs)
                            {
                                formAndSendExpectedExplosionTime();
                            }
                        }
                    }
                    
                    //Commanded Ibac state, as set by user
                    IbacStateBelief ibacStateBelief = (IbacStateBelief) m_BeliefManager.get(IbacStateBelief.BELIEF_NAME, true);
                            
                    if(ibacStateBelief != null)
                    {
                        //If commanded Ibac mode matches actual Ibac mode, then the current command doesn't need to be sent again
                        if (m_IbacConnected && m_IbacMode == ibacStateBelief.getState() && m_PodStatusFieldsTimestampMs > ibacStateBelief.getTimeStamp().getTime())
                        {
                            m_DesiredIbacState_LastAcceptedTimeMs = ibacStateBelief.getTimeStamp().getTime();
                        }
                        
                        //If actual ibac state differs from the current commanded belief (set by GCS user)
                        if (m_IbacConnected && m_IbacMode != ibacStateBelief.getState() && m_PodStatusFieldsTimestampMs > 0 && ibacStateBelief.getTimeStamp().getTime() > m_DesiredIbacState_LastAcceptedTimeMs)
                        {
                            m_DesiredIbacState = ibacStateBelief.getState();
                            m_DesiredIbacState_TimeMs = ibacStateBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the ibac state, (to allow for network issues)
                            if (currTimeMs - m_LastSentIbacState_TimestampMs > m_IbacStateChangeMessagePeriodMs)
                            {
                                formAndSendIbacState();
                            }
                        }
                    }
                                        
                    //Guaranteed frame request sent by the video client
                    VideoClientSatCommFrameRequestBelief satcommFrameRequestBelief = (VideoClientSatCommFrameRequestBelief) m_BeliefManager.get(VideoClientSatCommFrameRequestBelief.BELIEF_NAME, true);

                    if(satcommFrameRequestBelief != null)
                    {
                        if( satcommFrameRequestBelief.getTimeStamp().getTime() > m_DesiredGuaranteedFrameRequest_TimeMs )
                        {            
                            m_DesiredGuaranteedFrameRequest_TimeMs = satcommFrameRequestBelief.getTimeStamp().getTime();  
                            
                            if(!satcommFrameRequestBelief.getLastInterlaceReceived())
                            {            
                                // new SatComm image request has been received, start the segment receive thread
                                new GuarantneedImageReceiveThread().start();
                            }
                            else
                            {
                                // last progressive frame has been received, set this flag to start checking POD status for SatComm TX finished status 
                                m_SatCommImageTxInProgress = true;
                            }
                        }
                    }                    
                    
                        
                    //Non-guaranteed frame request sent by the video client
                    VideoClientFrameRequestBelief frameRequestBelief = (VideoClientFrameRequestBelief) m_BeliefManager.get(VideoClientFrameRequestBelief.BELIEF_NAME, true);
                    
                    if(frameRequestBelief != null)
                    {         
                        if( frameRequestBelief.getTimeStamp().getTime() > m_DesiredFrameRequest_TimeMs )
                        {
                            m_DesiredFrameRequest_TimeMs = frameRequestBelief.getTimeStamp().getTime();
                            formAndSendFrameRequest();
                            //System.out.println("GCS_SATCOMM_ARBITRATOR: Sent non-guaranteed frame request satcomm!");
                        }
                    }
                                        
                    //Commanded video recorder state, as set by user
                    VideoClientRecorderCmdBelief recorderCmdBelief = (VideoClientRecorderCmdBelief) m_BeliefManager.get(VideoClientRecorderCmdBelief.BELIEF_NAME, true);
                    
                    if (recorderCmdBelief != null)
                    {
                        //If commanded recorder state matches actual recorder state, then the current command doesn't need to be sent again
                        if (m_IRVideoRecState == recorderCmdBelief.getRecorderCmd() && m_PodStatusFieldsTimestampMs > recorderCmdBelief.getTimeStamp().getTime())
                        {
                            m_DesiredRecorderState_LastAcceptedTimeMs = recorderCmdBelief.getTimeStamp().getTime();
                        }
                        
                        if (m_IRVideoRecState != recorderCmdBelief.getRecorderCmd() && m_PodStatusFieldsTimestampMs > 0 && recorderCmdBelief.getTimeStamp().getTime() > m_DesiredRecorderState_LastAcceptedTimeMs)
                        {
                            m_DesiredVideoRecorderState = recorderCmdBelief.getRecorderCmd();
                            m_DesiredVideoRecorderState_TimeMs = recorderCmdBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the recorder state, (to allow for network issues)
                            if (currTimeMs - m_LastSentRecorderState_TimestampMs > m_VideoRecorderStateChangeMessagePeriodMs)
                            {
                                formAndSendRecorderState();
                            }
                        }
                    }
                    
                    //Commanded video stream state, as set by user
                    VideoClientStreamCmdBelief streamCmdBelief = (VideoClientStreamCmdBelief) m_BeliefManager.get(VideoClientStreamCmdBelief.BELIEF_NAME, true);
                    
                    if (streamCmdBelief != null)
                    {
                        //If commanded stream state matches actual stream state, then the current command doesn't need to be sent again
                        if (m_PODVideoStreamState == streamCmdBelief.getStreamCmd() && m_PodStatusFieldsTimestampMs > streamCmdBelief.getTimeStamp().getTime())
                        {
                            m_DesiredVideoStreamState_LastAcceptedTimeMs = streamCmdBelief.getTimeStamp().getTime();
                        }
                        
                        if (m_PODVideoStreamState != streamCmdBelief.getStreamCmd() && m_PodStatusFieldsTimestampMs > 0 && streamCmdBelief.getTimeStamp().getTime() > m_DesiredVideoStreamState_LastAcceptedTimeMs)
                        {
                            m_DesiredVideoStreamState = streamCmdBelief.getStreamCmd();
                            m_DesiredClientHost = streamCmdBelief.getClientIpAsIntegerArray();
                            m_DesiredClientPort = streamCmdBelief.getClientPort();
                            m_DesiredVideoStreamState_TimeMs = streamCmdBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the video stream state, (to allow for network issues)
                            if (currTimeMs - m_LastSentVideoStreamState_TimestampMs >  m_VideoStreamStateChangeMessagePeriodMs)
                            {
                                formAndSendVideoStreamState();
                            }
                        }
                    }
                    
                    //Commanded video converter state, as set by user
                    VideoClientConverterCmdBelief videoConverterStateBelief = (VideoClientConverterCmdBelief) m_BeliefManager.get(VideoClientConverterCmdBelief.BELIEF_NAME, true);
                    
                    if (videoConverterStateBelief != null)
                    {
                        if( videoConverterStateBelief.getTimeStamp().getTime() > m_DesiredVideoConverterState_TimeMs )
                        {                            
                            m_DesiredVideoConverterState_TimeMs = videoConverterStateBelief.getTimeStamp().getTime();
                            m_VideoConversionInProgress = true;
                            formAndSendVideoConverterState();
                        }
                    }
                    
                    
                    //Commanded alpha pump state, as set by user
                    AlphaSensorStateBelief alphaStateBelief = (AlphaSensorStateBelief) m_BeliefManager.get(AlphaSensorStateBelief.BELIEF_NAME, true);
                            
                    if(alphaStateBelief != null)
                    {
                        //If commanded alpha mode matches actual alpha mode, then the current command doesn't need to be sent again
                        if (m_AlphaConnected && m_AlphaMode == alphaStateBelief.getState() && m_PodStatusFieldsTimestampMs > alphaStateBelief.getTimeStamp().getTime())
                        {
                            m_DesiredAlphaState_LastAcceptedTimeMs = alphaStateBelief.getTimeStamp().getTime();
                        }
                        
                        //If actual alpha state differs from the current commanded belief (set by GCS user)
                        if (m_AlphaConnected && m_AlphaMode != alphaStateBelief.getState() && m_PodStatusFieldsTimestampMs > 0 && alphaStateBelief.getTimeStamp().getTime() > m_DesiredAlphaState_LastAcceptedTimeMs)
                        {
                            m_DesiredAlphaState = alphaStateBelief.getState();
                            m_DesiredAlphaState_TimeMs = alphaStateBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the alpha state, (to allow for network issues)
                            if (currTimeMs - m_LastSentAlphaState_TimestampMs > m_AlphaStateChangeMessagePeriodMs)
                            {
                                formAndSendAlphaState();
                            }
                        }
                    }
                    
                    //Commanded Anaconda state, as set by user
                    AnacondaStateBelief anacondaStateBelief = (AnacondaStateBelief) m_BeliefManager.get(AnacondaStateBelief.BELIEF_NAME, true);
                            
                    if(anacondaStateBelief != null)
                    {
                        //If commanded Anaconda mode matches actual Anaconda mode, then the current command doesn't need to be sent again
                        if (m_AnacondaConnected && m_AnacondaMode == anacondaStateBelief.getAnacondState() && m_PodStatusFieldsTimestampMs > anacondaStateBelief.getTimeStamp().getTime())
                        {
                            m_DesiredAnacondaState_LastAcceptedTimeMs = anacondaStateBelief.getTimeStamp().getTime();
                        }
                        
                        //If actual anaconda state differs from the current commanded belief (set by GCS user)
                        if (m_AnacondaConnected && m_AnacondaMode != anacondaStateBelief.getAnacondState() && m_PodStatusFieldsTimestampMs > 0 && anacondaStateBelief.getTimeStamp().getTime() > m_DesiredAnacondaState_LastAcceptedTimeMs)
                        {
                            m_DesiredAnacondaState = anacondaStateBelief.getAnacondState();
                            m_DesiredAnacondaState_TimeMs = anacondaStateBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the anaconda state, (to allow for network issues)
                            if (currTimeMs - m_LastSentAnacondaState_TimestampMs > m_AnacondaStateChangeMessagePeriodMs)
                            {
                                formAndSendAnacondaState();
                            }
                        }
                    }
                            
                    //Commanded C100 state, as set by user
                    ParticleCollectorStateBelief c100StateBelief = (ParticleCollectorStateBelief) m_BeliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME, true);
                            
                    if(c100StateBelief != null)
                    {
                        //If commanded C100 mode matches actual C100 mode, then the current command doesn't need to be sent again
                        if (m_C100Connected && m_C100Mode == c100StateBelief.getParticleCollectorState() && m_PodStatusFieldsTimestampMs > c100StateBelief.getTimeStamp().getTime())
                        {
                            m_DesiredC100State_LastAcceptedTimeMs = c100StateBelief.getTimeStamp().getTime();
                        }
                        
                        //If actual c100 state differs from the current commanded belief (set by GCS user)
                        if (m_C100Connected && m_C100Mode != c100StateBelief.getParticleCollectorState() && m_PodStatusFieldsTimestampMs > 0 && c100StateBelief.getTimeStamp().getTime() > m_DesiredC100State_LastAcceptedTimeMs)
                        {
                            m_DesiredC100State = c100StateBelief.getParticleCollectorState();
                            m_DesiredC100State_TimeMs = c100StateBelief.getTimeStamp().getTime();
                            
                            //If it's been a little bit since we last sent the C100 state, (to allow for network issues)
                            if (currTimeMs - m_LastSentC100State_TimestampMs > m_C100StateChangeMessagePeriodMs)
                            {
                                formAndSendC100State();
                            }
                        }
                    }
                    
                    //Commanded WACS mode, as set by user
                    AgentModeCommandedBelief agentModeBelief = (AgentModeCommandedBelief) m_BeliefManager.get(AgentModeCommandedBelief.BELIEF_NAME);
                            
                    if(agentModeBelief != null && m_WacsMode != null)
                    {
                        WacsMode reqMode = null;
                        try
                        {
                            reqMode = WacsMode.fromString(agentModeBelief.getMode(WACSAgent.AGENTNAME).getName());
                        }
                        catch (Exception e)
                        {}
                                
                        //If actual agent mode differs from the current commanded belief (set by GCS user)
                        if (reqMode != null && !reqMode.equals (WacsMode.NO_MODE) && !m_WacsMode.equals (reqMode) && m_PodStatusFieldsTimestampMs > 0)
                        {
                            m_DesiredWacsMode = reqMode;
                            m_DesiredWacsMode_TimeMs = agentModeBelief.getTimeStamp().getTime();
                            
                            //Current WACS mode being used
                            AgentModeCommandedBelief modeBeliefCurrent = (AgentModeCommandedBelief) m_BeliefManager.get(AgentModeCommandedBelief.BELIEF_NAME);
                            if (modeBeliefCurrent.getName().endsWith(BeliefManagerWacs.SATCOMM_EXTENSION))
                            {
                                //This means we're using the belief send down the satcomm link, not the local settings.  In this case, there's
                                //no reason to resend the local belief.
                            }
                            //If it's been a little bit since we last sent the wacs mode, (to allow for network issues)
                            else if (currTimeMs - m_LastSentWacsMode_TimestampMs > m_WacsModeChangeMessagePeriodMs)
                            {
                                formAndSendWacsMode();
                            }
                        }
                    }
                    
                    //WACS Waypoint settings, as set by user
                    WACSWaypointCommandedBelief wwbBelief = (WACSWaypointCommandedBelief) m_BeliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME, true);
                            
                    if(wwbBelief != null)
                    {
                        //If actual waypoint settings differ from the current commanded belief (set by GCS user) (allow for rounding in satcomm message)
                        //WACS Waypoint Belief
                        if (wwbBelief != null && (m_PodStatusFieldsTimestampMs > 0 && 
                                                                        ((Math.abs(wwbBelief.getFinalLoiterAltitude().getDoubleValue(Length.FEET) - m_LoiterAltitudeAglFt) > 1.5) ||
                                                                         (Math.abs(wwbBelief.getStandoffLoiterAltitude().getDoubleValue(Length.FEET) - m_OffsetLoiterAltitudeAglFt) > 1.5) ||
                                                                         (Math.abs(wwbBelief.getLoiterRadius().getDoubleValue(Length.METERS) - m_LoiterRadiusM) > 1.5) ||
                                                                         (Math.abs(wwbBelief.getIntersectAltitude().getDoubleValue(Length.FEET) - m_InterceptAltitudeAglFt) > 1.5) ||
                                                                         (Math.abs(wwbBelief.getIntersectRadius().getDoubleValue(Length.METERS) - m_InterceptRadiusM) > 1.5))))
                        {
                            m_DesiredLoiterRadiusM = wwbBelief.getLoiterRadius().getDoubleValue(Length.METERS);
                            m_DesiredInterceptRadiusM = wwbBelief.getIntersectRadius().getDoubleValue(Length.METERS);
                            m_DesiredLoiterAltitudeAglFt = wwbBelief.getFinalLoiterAltitude().getDoubleValue(Length.FEET);
                            m_DesiredInterceptAltitudeAglFt = wwbBelief.getIntersectAltitude().getDoubleValue(Length.FEET);
                            m_DesiredOffsetLoiterAltitudeAglFt = wwbBelief.getStandoffLoiterAltitude().getDoubleValue(Length.FEET);
                            m_DesiredWWB_TimestampMs = wwbBelief.getTimeStamp().getTime();

                            //Current WACS Waypoint settings being used
                            WACSWaypointActualBelief wwbBeliefCurrent = (WACSWaypointActualBelief) m_BeliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);
                            if (wwbBeliefCurrent != null && wwbBeliefCurrent.getName().endsWith(BeliefManagerWacs.SATCOMM_EXTENSION))
                            {
                                //This means we're using the belief send down the satcomm link, not the local settings.  In this case, there's
                                //no reason to resend the local belief.
                            }
                            //If it's been a little bit since we last sent the waypoint settings, (to allow for network issues)
                            else if (currTimeMs - m_LastSentWWB_TimestampMs > m_WWBChangeMessagePeriodMs)
                            {
                                formAndSendWacsWaypointSettings();
                            }
                        }
                    }
                        
                    //Control enable via autopilot setting
                    boolean autopilotControlEnabled = false;
                    EnableAutopilotControlCommandedBelief actualControl = (EnableAutopilotControlCommandedBelief)m_BeliefManager.get (EnableAutopilotControlCommandedBelief.BELIEF_NAME);
                    if (actualControl != null && actualControl.getAllow())
                        autopilotControlEnabled = true;
                    if (autopilotControlEnabled != m_ControlEnabledViaAutopilot && m_PodStatusFieldsTimestampMs > 0)
                    {
                        //If 'enable control via autopilot' value differs from the current commanded belief (set by GCS user)
                        m_DesiredControlAP = autopilotControlEnabled;
                        m_DesiredControlAP_TimestampMs = System.currentTimeMillis();

                        //If it's been a little bit since we last sent command, (to allow for network issues)
                        if (currTimeMs - m_LastSentControlAP_TimestampMs > m_ControlAPChangeMessagePeriodMs)
                        {
                            formAndSendControlAP();
                        }
                    }
                    
                    //Allow intercept setting
                    AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)m_BeliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
                    boolean allowIntercept = (allowInterceptBlf != null && allowInterceptBlf.getAllow());
                    
                    if (allowIntercept != m_AllowInterceptPermitted && m_PodStatusFieldsTimestampMs > 0)
                    {
                        //If 'allow intercept' value differs from the current commanded belief (set by GCS user)
                        m_DesiredAllowIntercept = allowIntercept;
                        m_DesiredAllowIntercept_TimestampMs = System.currentTimeMillis();

                        //If it's been a little bit since we last sent command, (to allow for network issues)
                        if (currTimeMs - m_LastSentAllowIntercept_TimestampMs > m_AllowInterceptChangeMessagePeriodMs)
                        {
                            formAndSendAllowIntercept();
                        }
                    }
                    
                    //Set gimbal target
                    TargetCommandedBelief targetBelief = (TargetCommandedBelief)m_BeliefManager.get(TargetCommandedBelief.BELIEF_NAME, true);
                    
                    if (targetBelief != null && m_PodStatusFieldsTimestampMs > 0 && targetBelief.getTimeStamp().getTime() > m_LastSentTarget_TimestampMs)
                    {
                        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                        PositionTimeName ptn = targetBelief.getPositionTimeName(tmp);
                        if (ptn != null)
                        {
                            LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                            if (Math.abs(lla.getLatitude().getDoubleValue(Angle.DEGREES) - m_CurrentTargetLatitudeDeg) > .0001 || 
                                    Math.abs(lla.getLongitude().getDoubleValue(Angle.DEGREES) - m_CurrentTargetLongitudeDeg) > .0001 || 
                                    Math.abs(lla.getAltitude().getDoubleValue(Length.FEET) - m_CurrentTargetAltitudeMslFt) > 1)
                            {
                                //If current target lcoation differs from the current commanded belief (set by GCS user)
                                m_DesiredTargetLatitudeDeg = lla.getLatitude().getDoubleValue(Angle.DEGREES);
                                m_DesiredTargetLongitudeDeg = lla.getLongitude().getDoubleValue(Angle.DEGREES);
                                m_DesiredTargetAltitudeMslFt = lla.getAltitude().getDoubleValue(Length.FEET);
                                m_DesiredTargetTimestampMs = targetBelief.getTimeStamp().getTime();
                                
                                //If it's been a little bit since we last sent the target information, (to allow for network issues)
                                if (currTimeMs - m_LastSentTarget_TimestampMs > m_TargetChangeMessagePeriodMs)
                                {
                                    formAndSendDesiredTarget();
                                }
                            }
                        }
                    }
                    
                    formAndSendGenericPackets (DESTINATION_WACSPOD);
                    
                        /*get satcomm boards working fully
                        test with collector pod, C100
                        send version to ben, plus ICD, jar files, config files
                        fill in remaining gcs beliefs
                        */
                    
                    try
                    {
                        Thread.sleep (10);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private class PacketProcessorThread extends Thread 
    {
        public PacketProcessorThread ()
        {
            setDaemon(true);
        }
        
        public void run ()
        {
            try
            {
                while (true)
                {
                    if (m_MulticastReceivedVsmQueue.isEmpty())
                    {
                        //If no packets to process, sleep for a bit
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
                    
                    //Get the next packet to parse and work on it
                    try
                    {
                        DatagramPacket packetToParse = m_MulticastReceivedVsmQueue.poll();
                        if (packetToParse != null && parseReceivedPacket (packetToParse.getData(), packetToParse.getLength()))
                            publishCurrentBeliefs();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }                    
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
