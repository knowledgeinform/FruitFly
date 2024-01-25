/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.wacs;

import edu.jhuapl.jlib.math.Altitude;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Latitude;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.Longitude;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.AnacondaModeEnum;
import edu.jhuapl.nstd.cbrnPods.messages.C100Messages.ParticleCollectorMode;
import edu.jhuapl.nstd.swarm.WACSAgent;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface.ShadowCommandMessage;
import edu.jhuapl.nstd.swarm.autopilot.ShadowOnboardAutopilotInterface.ShadowTelemetryMessage;
import edu.jhuapl.nstd.swarm.behavior.LoiterBehavior;
import edu.jhuapl.nstd.swarm.behavior.ParticleCloudPredictionBehavior;
import edu.jhuapl.nstd.swarm.belief.AgentModeActualBelief;
import edu.jhuapl.nstd.swarm.belief.AgentModeCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptActualBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.AllowInterceptCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBelief;
import edu.jhuapl.nstd.swarm.belief.AlphaSensorStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.AnacondaActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBelief;
import edu.jhuapl.nstd.swarm.belief.AnacondaStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.BeliefManagerWacs;
import edu.jhuapl.nstd.swarm.belief.CBRNHeartbeatBelief;
import edu.jhuapl.nstd.swarm.belief.CloudDetection;
import edu.jhuapl.nstd.swarm.belief.CloudDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlActualBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.EnableAutopilotControlCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.ExplosionBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeActualBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.ExplosionTimeCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.GammaStatisticsBelief;
import edu.jhuapl.nstd.swarm.belief.IbacActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBelief;
import edu.jhuapl.nstd.swarm.belief.IbacStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.IrExplosionAlgorithmEnabledBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionBelief;
import edu.jhuapl.nstd.swarm.belief.METPositionTimeName;
import edu.jhuapl.nstd.swarm.belief.GuaranteedVideoDataBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorActualStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleCollectorStateBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.belief.TASETelemetryBelief;
import edu.jhuapl.nstd.swarm.belief.TargetActualBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.TargetCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientFrameRequestBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderStatusBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientRecorderCmdBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientConverterCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientConverterCmdBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamCmdBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientStreamCmdBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBelief;
import edu.jhuapl.nstd.swarm.belief.VideoClientSatCommFrameRequestBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.VideoClientConversionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.SatCommImageTransmissionFinishedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointActualBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBelief;
import edu.jhuapl.nstd.swarm.belief.WACSWaypointCommandedBeliefSatComm;
import edu.jhuapl.nstd.swarm.belief.mode.Mode;
import edu.jhuapl.nstd.swarm.display.TimeSyncDialog;
import edu.jhuapl.nstd.swarm.maps.DtedGlobalMap;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.MathConstants;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author humphjc1
 */
public class PodSatCommMessageAribtrator extends SatCommMessageArbitrator
{
    //public static final String BELIEF_CREATORNAME = "satcommpod";
    
    private int m_PodStatusMsgPeriodMs;
    private long m_PodStatusMsgLastSentMs;
    private int m_ConnectivityTimeoutPeriodSec;
    private long m_ParticleDetectionMsgLastSentMs;
    private long m_ParticleDetectionMsgLastCheckedMs;
    private long m_ParticleDetectionMsgPeriodMs;
    private long m_ParticleDetectionCheckPeriodMs;
    private long m_ChemicalDetectionMsgLastSentMs;
    private long m_ChemicalDetectionMsgLastCheckedMs;
    private long m_ChemicalDetectionMsgPeriodMs;
    private long m_ChemicalDetectionCheckPeriodMs;
    private final Object m_GCSSegmentRequestLock = new Object();
    private GuaranteedImageSendThread m_GuaranteedImageSendThread = null;
    
    //Interface for shadow commands and telemetry, only relevant to WACS pod
    private ShadowOnboardAutopilotInterface m_ShadowAutopilotInterface = null;        
    private boolean m_SendCommandToWavsmImmediately;
    private boolean m_GCSReadyForSegment;
    private long m_LastWavsmTelemMessageTimestampMs;
    private long m_LastVideoDataMessageTimestampMs = -1;
    private long m_LastVideoConversionFinishedTimestampMs = -1;   
    private long m_LastSatCommTxCompleteTimestampMs = -1;
    private long m_LastVideoStreamStateTimestampMs = -1;
    private long m_LastVideoRecorderStateTimestampMs = -1;
    
    
    public PodSatCommMessageAribtrator (BeliefManagerWacs belMgr, InetAddress multicastSendVsmAddress, int multicastSendVsmPort, InetAddress multicastRecvVsmAddress, int multicastRecvVsmPort, int podStatusMsgPeriodMs, int connectivityTimeoutPeriodSec, int beliefNetworkAllowableLatencyMs, ShadowOnboardAutopilotInterface shadowAutopilotInterface)
    {
        super (belMgr, TimeSyncDialog.LOCATION_POD, multicastSendVsmAddress, multicastSendVsmPort, multicastRecvVsmAddress, multicastRecvVsmPort, beliefNetworkAllowableLatencyMs);
        m_PodStatusMsgPeriodMs = podStatusMsgPeriodMs;
        m_PodStatusMsgLastSentMs = -1;
        m_LastWavsmTelemMessageTimestampMs = -1;
        
        long defaultSendPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.DefaultMessageSendPeriod.Ms", 5000);
        
        m_ParticleDetectionMsgLastSentMs = -1;
        m_ParticleDetectionMsgLastCheckedMs = -1;
        m_ParticleDetectionMsgPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ParticleDetectionSendPeriod.Ms", defaultSendPeriodMs);
        m_ParticleDetectionCheckPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ParticleDetectionCheckPeriod.Ms", 500);
        m_ChemicalDetectionMsgLastSentMs = -1;
        m_ChemicalDetectionMsgLastCheckedMs = -1;
        m_ChemicalDetectionMsgPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ChemicalDetectionSendPeriod.Ms", defaultSendPeriodMs);;
        m_ChemicalDetectionCheckPeriodMs = Config.getConfig().getPropertyAsLong("SatComm.ChemicalDetectionCheckPeriod.Ms", 500);
        m_ConnectivityTimeoutPeriodSec = connectivityTimeoutPeriodSec;
        m_ShadowAutopilotInterface = shadowAutopilotInterface;
        m_SendCommandToWavsmImmediately = false;
        
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
        
        if ((getPacketDestination(bufferedMsg) & DESTINATION_WACSPOD) == 0)
            return false;   //This message wasn't intended for here, it might be a loopback message.  Ignore it.
        if (parseGenericPackets(messageType, bufferedMsg))
            return false;
        
        //Get timestamp of the packet, which will be the timestamp of the beliefs created by the message
        long dataTimestampMs = getPacketTimestampMs(bufferedMsg);
        
        //Process message and distrubute contents
        switch (messageType)
        {
            case MSGTYPE_GCSCOMMANDMESSAGE:
                processGcsCommand (bufferedMsg, dataTimestampMs);
                break;
            case MSGTYPE_WAVSMTOPODS:
                if (dataTimestampMs > m_LastWavsmTelemMessageTimestampMs)
                    processWavsmTelemMessage (bufferedMsg, dataTimestampMs);
                else
                    System.out.println ("Wavsm telemetry message received was not newer than last data - ignored: " + dataTimestampMs);
                break;
            default:
                System.out.println ("Unknown packet type: " + messageType + " received at " + getPacketTimestampMs(bufferedMsg));
                return false;
        }
        
        //Message parsed succesfully.
        return true;
    }
    
    private void processGcsCommand (byte bufferedMsg[], long dataTimestampMs)
    {
        System.out.println("processGcsCommand called...");
        int bufferPos = MSG_HEADER_SIZE;
        
        int commandCode = (0xff & bufferedMsg[bufferPos++]);
        int paramLength = (0xff & bufferedMsg[bufferPos++]);
        
        switch (commandCode)
        {
            case GCSCMDCODE_SETEXPLOSIONTIME:
                if (paramLength != GCSCMDPARAMLENGTH_SETEXPLOSIONTIME)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetExplosionTime (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETIBACSTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETIBACSTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetIbacState (bufferedMsg, dataTimestampMs, bufferPos);
                break;
                
            case GCSCMDCODE_SETFRAMEREQUEST:
                if (paramLength != GCSCMDPARAMLENGTH_SETFRAMEREQUEST)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetFrameRequest (dataTimestampMs);
                break;   
                
            case GCSCMDCODE_SETVIDEOSEGMENTRECEIVED:
                if (paramLength != GCSCMDPARAMLENGTH_SETVIDEOSEGMENTRECEIVED)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetVideoSegmentReceived (bufferedMsg, dataTimestampMs, bufferPos);
                break;   
                
            case GCSCMDCODE_SETVIDEOCONVERTERSTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETVIDEOCONVERTERSTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetVideoConverterState (dataTimestampMs);
                break;             
                
            case GCSCMDCODE_SETVIDEORECORDERSTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETVIDEORECORDERSTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetVideoRecorderState (bufferedMsg, dataTimestampMs, bufferPos);
                break;        
                
            case GCSCMDCODE_SETVIDEOSTREAMSTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETVIDEOSTREAMSTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetVideoStreamState (bufferedMsg, dataTimestampMs, bufferPos);
                break;                     
                
            case GCSCMDCODE_SETGUARANTEEDFRAMEREQUEST:
                if (paramLength != GCSCMDPARAMLENGTH_SETGUARANTEEDFRAMEREQUEST)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetGuaranteedFrameRequest (bufferedMsg, dataTimestampMs, bufferPos);
                break;                  
                
            case GCSCMDCODE_SETWACSMODE:
                if (paramLength != GCSCMDPARAMLENGTH_SETWACSMODE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetWacsMode (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETWACSWAYPOINTSETTINGS:
                if (paramLength != GCSCMDPARAMLENGTH_SETWACSWAYPOINTSETTINGS)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetWWB (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETCONTROLAP:
                if (paramLength != GCSCMDPARAMLENGTH_SETCONTROLAP)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetControlAP (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETTARGET:
                if (paramLength != GCSCMDPARAMLENGTH_SETTARGET)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetDesiredTarget (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETALLOWINTERCEPT:
                if (paramLength != GCSCMDPARAMLENGTH_SETALLOWINTERCEPT)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetAllowIntercept (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETANACONDASTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETANACONDASTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetAnacondaState (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETC100STATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETC100STATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetC100State (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            case GCSCMDCODE_SETALPHASTATE:
                if (paramLength != GCSCMDPARAMLENGTH_SETALPHASTATE)
                    System.out.println ("Invalid command parameter length: " + paramLength + " received for code " + commandCode + " at " + getPacketTimestampMs(bufferedMsg));
                else
                    processSetAlphaState (bufferedMsg, dataTimestampMs, bufferPos);
                break;
            
            default:
                System.out.println ("Unknown GCS command code: " + commandCode + " received at " + getPacketTimestampMs(bufferedMsg));
        }
    }
    
    private void processSetExplosionTime (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        long expTimeSec = 0;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        expTimeSec |= (0xFF & bufferedMsg[bufferPos++]);
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredExplosionTimeMs = expTimeSec*1000;
        m_DesiredExplosionTime_TimestampMs = dataTimestampMs;
    }
    
    private void processSetGuaranteedFrameRequest (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {       
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int receipt;        
        receipt = ByteBuffer.wrap(bufferedMsg, bufferPos, 4).getInt();
        bufferPos +=4;    
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredGuaranteedFrameRequest_TimeMs = dataTimestampMs;
        m_DesiredGuaranteedDataReceipt = receipt;
    }
    
    private void processSetFrameRequest (long dataTimestampMs)
    {
        m_DesiredFrameRequest_TimeMs = dataTimestampMs;
    }
    
    private void processSetVideoSegmentReceived(byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int segmentHash = ByteBuffer.wrap(bufferedMsg, bufferPos, 4).getInt();
        bufferPos +=4;                 
        
        System.out.println("POD_SATCOMM_ARBITRATOR: Received video segment request with hash receipt = " + segmentHash + "\n");
        setLastReceivedSegmentHash(segmentHash);
        setGCSReadyForImageSegment(true);
    }
    
    private void processSetVideoConverterState (long dataTimestampMs)
    {
        m_DesiredVideoConverterState_TimeMs = dataTimestampMs;
    }
    
    private void processSetVideoRecorderState (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////     
        boolean commandedState;
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////        
        m_DesiredVideoRecorderState = commandedState;
        m_DesiredVideoRecorderState_TimeMs = dataTimestampMs;        
    }
    
    private void processSetVideoStreamState (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////     
        boolean commandedState;
        int[] hostname = new int[4];
        short port = 0;
        
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
        for( int i = 0; i < 4; i++ )
        {
            hostname[i] = (0xFF & bufferedMsg[bufferPos++]);
        }
        
        port |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        port |= (0xFF & bufferedMsg[bufferPos++]);
                        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////        
        m_DesiredVideoStreamState = commandedState;
        m_DesiredClientHost = hostname;
        m_DesiredClientPort = port;
        m_DesiredVideoStreamState_TimeMs = dataTimestampMs;             
    }
    
    private void processSetIbacState (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        boolean commandedState;
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredIbacState = commandedState;
        m_DesiredIbacState_TimeMs = dataTimestampMs;
    }
    
    private void processSetAlphaState (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        boolean commandedState;
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredAlphaState = commandedState;
        m_DesiredAlphaState_TimeMs = dataTimestampMs;
    }
    
    private void processSetAnacondaState (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        AnacondaModeEnum commandedState;
        commandedState = AnacondaModeEnum.fromValue(0xFF & bufferedMsg[bufferPos++]);
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredAnacondaState = commandedState;
        m_DesiredAnacondaState_TimeMs = dataTimestampMs;
    }
    
    private void processSetC100State (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        ParticleCollectorMode commandedState;
        commandedState = ParticleCollectorMode.fromValue(0xFF & bufferedMsg[bufferPos++]);
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredC100State = commandedState;
        m_DesiredC100State_TimeMs = dataTimestampMs;
    }
            
    private void processSetWacsMode (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        WacsMode commandedMode;
        commandedMode = WacsMode.fromValue(0xFF & bufferedMsg[bufferPos++]);
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredWacsMode = commandedMode;
        m_DesiredWacsMode_TimeMs = dataTimestampMs;
    }
    
    private void processSetWWB (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int finalLoiterAltAglFt = 0;
        finalLoiterAltAglFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        finalLoiterAltAglFt |= (0xFF & bufferedMsg[bufferPos++]);
        int offsetLoiterAltAglFt = 0;
        offsetLoiterAltAglFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        offsetLoiterAltAglFt |= (0xFF & bufferedMsg[bufferPos++]);
        int loiterRadiusFt = 0;
        loiterRadiusFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        loiterRadiusFt |= (0xFF & bufferedMsg[bufferPos++]);
        int interceptAltAglFt = 0;
        interceptAltAglFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        interceptAltAglFt |= (0xFF & bufferedMsg[bufferPos++]);
        int interceptRadiusFt = 0;
        interceptRadiusFt |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        interceptRadiusFt |= (0xFF & bufferedMsg[bufferPos++]);
        
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredLoiterAltitudeAglFt = finalLoiterAltAglFt;
        m_DesiredOffsetLoiterAltitudeAglFt = offsetLoiterAltAglFt;
        m_DesiredLoiterRadiusM = loiterRadiusFt*MathConstants.FT2M;
        m_DesiredInterceptAltitudeAglFt = interceptAltAglFt;
        m_DesiredInterceptRadiusM = interceptRadiusFt*MathConstants.FT2M;
        m_DesiredWWB_TimestampMs = dataTimestampMs;
    }
    
    private void processSetControlAP (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        boolean commandedState;
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredControlAP = commandedState;
        m_DesiredControlAP_TimestampMs = dataTimestampMs;
    }
    
    private void processSetAllowIntercept (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        boolean commandedState;
        commandedState = (0xFF & bufferedMsg[bufferPos++]) != 0;
                
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredAllowIntercept = commandedState;
        m_DesiredAllowIntercept_TimestampMs = dataTimestampMs;
    }
    
    private void processSetDesiredTarget (byte bufferedMsg[], long dataTimestampMs, int bufferPos)
    {
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        int altitudeMslFtMINT = 0;
        altitudeMslFtMINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        altitudeMslFtMINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        altitudeMslFtMINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        altitudeMslFtMINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        long latitudeDegLONG = 0;
        latitudeDegLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        long longitudeDegLONG = 0;
        longitudeDegLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        m_DesiredTargetAltitudeMslFt = Float.intBitsToFloat(altitudeMslFtMINT);
        m_DesiredTargetLatitudeDeg = Double.longBitsToDouble(latitudeDegLONG);
        m_DesiredTargetLongitudeDeg = Double.longBitsToDouble(longitudeDegLONG);
        m_DesiredTargetTimestampMs = dataTimestampMs;
    }
    
    private void processWavsmTelemMessage (byte bufferedMsg[], long dataTimestampMs)
    {
        int bufferPos = MSG_HEADER_SIZE;
        
        ShadowTelemetryMessage telemetryMessage = new ShadowTelemetryMessage();
        
        //////////////////////////////
        //Extract bytes from message buffer
        //////////////////////////////
        bufferPos++;
        bufferPos++;
        
        int commandSpeedMpsINT = 0;
        commandSpeedMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandSpeedMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandSpeedMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandSpeedMpsINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        
        long commandLoiterLatitudeRadLONG = 0;
        commandLoiterLatitudeRadLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        /*commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandLoiterLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        long commandLoiterLongitudeRadLONG = 0;
        commandLoiterLongitudeRadLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        /*commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandLoiterLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        int commandLoiterRadiusMINT = 0;
        commandLoiterRadiusMINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandLoiterRadiusMINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandLoiterRadiusMINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandLoiterRadiusMINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int commandLoiterAltitudeMslMINT = 0;
        commandLoiterAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandLoiterAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandLoiterAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandLoiterAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte commandLoiterDirFlag = bufferedMsg[bufferPos++];
        byte commandGimbalModeFlag = bufferedMsg[bufferPos++];
        byte commandWacsModeFlag = bufferedMsg[bufferPos++];
        byte commandLoiterValidFlag = bufferedMsg[bufferPos++];
        
        long commandStrikeLatitudeRadLONG = 0;
        commandStrikeLatitudeRadLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        /*commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandStrikeLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        long commandStrikeLongitudeRadLONG = 0;
        commandStrikeLongitudeRadLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        /*commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandStrikeLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        int commandStrikeAltitudeMslMINT = 0;
        commandStrikeAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        commandStrikeAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        commandStrikeAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        commandStrikeAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte commandStrikeValidFlag = bufferedMsg[bufferPos++];
        
        bufferPos ++;
        bufferPos ++;
        bufferPos ++;
        
        long currentLatitudeRadLONG = 0;
        currentLatitudeRadLONG = ((ByteBuffer)(ByteBuffer.wrap(bufferedMsg, bufferPos, 8))).getLong();
        bufferPos += 8;
        /*currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentLatitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        long currentLongitudeRadLONG = 0;
        currentLongitudeRadLONG = ByteBuffer.wrap(bufferedMsg, bufferPos, 8).getLong();
        bufferPos += 8;
        /*currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 56;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 48;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 40;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 32;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentLongitudeRadLONG |= (0xFF & bufferedMsg[bufferPos++]);*/
        
        int currentAltitudeMslMINT = 0;
        currentAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentAltitudeMslMINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentRollRadINT = 0;
        currentRollRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentRollRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentRollRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentRollRadINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentPitchRadINT = 0;
        currentPitchRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentPitchRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentPitchRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentPitchRadINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentHeadingRadINT = 0;
        currentHeadingRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentHeadingRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentHeadingRadINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentHeadingRadINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentIASMpsINT = 0;
        currentIASMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentIASMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentIASMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentIASMpsINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentGndVelNorthMpsINT = 0;
        currentGndVelNorthMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentGndVelNorthMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentGndVelNorthMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentGndVelNorthMpsINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentGndVelEastMpsINT = 0;
        currentGndVelEastMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentGndVelEastMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentGndVelEastMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentGndVelEastMpsINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        int currentVerticalVelMpsINT = 0;
        currentVerticalVelMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 24;
        currentVerticalVelMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 16;
        currentVerticalVelMpsINT |= (0xFF & bufferedMsg[bufferPos++]) << 8;
        currentVerticalVelMpsINT |= (0xFF & bufferedMsg[bufferPos++]);
        
        byte tcdlLinkActiveFlag = bufferedMsg[bufferPos++];
        byte satCommThrottlingFlag = bufferedMsg[bufferPos++];
        
        
        ////////////////////////
        //Convert message fields to local fields
        ////////////////////////
        telemetryMessage.timestamp_ms = dataTimestampMs;
        m_LastWavsmTelemMessageTimestampMs = dataTimestampMs;
        telemetryMessage.m_CommandedAirspeedMps = Float.intBitsToFloat(commandSpeedMpsINT);
        telemetryMessage.m_LoiterLatitudeRad = Double.longBitsToDouble(commandLoiterLatitudeRadLONG);
        telemetryMessage.m_LoiterLongitudeRad = Double.longBitsToDouble(commandLoiterLongitudeRadLONG);
        telemetryMessage.m_LoiterRadiusM = Float.intBitsToFloat(commandLoiterRadiusMINT);
        telemetryMessage.m_LoiterAltitudeMslM = Float.intBitsToFloat(commandLoiterAltitudeMslMINT);
        telemetryMessage.m_LoiterCWDir = (commandLoiterDirFlag == 1);
        telemetryMessage.m_CommandedGimbalMode = commandGimbalModeFlag;
        
        if (commandWacsModeFlag == 0)
            telemetryMessage.m_CommandedWacsMode = WacsMode.LOITER;
        else if (commandWacsModeFlag == 1)
            telemetryMessage.m_CommandedWacsMode = WacsMode.INTERCEPT;
        else //if (commandLoiterDirFlag == 2)
            telemetryMessage.m_CommandedWacsMode = WacsMode.LOITER;
        
        telemetryMessage.m_LoiterValid = (commandLoiterValidFlag==1);
        telemetryMessage.m_StrikeLatitudeRad = Double.longBitsToDouble(commandStrikeLatitudeRadLONG);
        telemetryMessage.m_StrikeLongitudeRad = Double.longBitsToDouble(commandStrikeLongitudeRadLONG);
        telemetryMessage.m_StrikeAltitudeMslM = Float.intBitsToFloat(commandStrikeAltitudeMslMINT);
        telemetryMessage.m_StrikeValid = (commandStrikeValidFlag==1);
        telemetryMessage.latitude_rad = Double.longBitsToDouble(currentLatitudeRadLONG);
        telemetryMessage.longitude_rad = Double.longBitsToDouble(currentLongitudeRadLONG);
        telemetryMessage.barometricAltitudeMsl_m = Float.intBitsToFloat(currentAltitudeMslMINT);
        telemetryMessage.roll_rad = Float.intBitsToFloat(currentRollRadINT);
        telemetryMessage.pitch_rad = Float.intBitsToFloat(currentPitchRadINT);
        telemetryMessage.trueHeading_rad = Float.intBitsToFloat(currentHeadingRadINT);
        
        telemetryMessage.indicatedAirspeed_mps = Float.intBitsToFloat(currentIASMpsINT);
        telemetryMessage.trueAirspeed_mps = telemetryMessage.indicatedAirspeed_mps;
        
        telemetryMessage.groundSpeedNorth_mps = Float.intBitsToFloat(currentGndVelNorthMpsINT);
        telemetryMessage.groundSpeedEast_mps = Float.intBitsToFloat(currentGndVelEastMpsINT);
        telemetryMessage.m_VerticalVelocityMps = Float.intBitsToFloat(currentVerticalVelMpsINT);
        telemetryMessage.m_TCDLLinkActive = (tcdlLinkActiveFlag == 1);
        telemetryMessage.m_SatCommThrottling = (satCommThrottlingFlag == 1);
        
        //Use new telemetry message
        m_ShadowAutopilotInterface.setTelemetryMessage(telemetryMessage);
        m_SendCommandToWavsmImmediately = true;
    }
    
    private void publishCurrentBeliefs ()
    {
        //Commanded Explosion time belief
        ExplosionTimeCommandedBelief expTimeBelief = (ExplosionTimeCommandedBelief) m_BeliefManager.get(ExplosionTimeCommandedBelief.BELIEF_NAME);
        Date desiredExpTimeTimestamp = new Date (m_DesiredExplosionTime_TimestampMs);
        if (m_DesiredExplosionTimeMs > 0 && (expTimeBelief == null || (expTimeBelief.getTimeStamp().before(desiredExpTimeTimestamp) && expTimeBelief.getTime_ms() != m_DesiredExplosionTimeMs)))
        {
            ExplosionTimeCommandedBeliefSatComm expTimeBeliefSatComm = new ExplosionTimeCommandedBeliefSatComm(m_BeliefManager.getName(), m_DesiredExplosionTimeMs, desiredExpTimeTimestamp);
            m_BeliefManager.put (expTimeBeliefSatComm);
        }
        
        //Commanded IBAC state belief
        IbacStateBelief ibacStateBelief = (IbacStateBelief) m_BeliefManager.get(IbacStateBelief.BELIEF_NAME);
        Date ibacStateTimestamp = new Date (m_DesiredIbacState_TimeMs);
        if (m_DesiredIbacState_TimeMs > 0 && (ibacStateBelief == null || (ibacStateBelief.getTimeStamp().before(ibacStateTimestamp)/* && ibacStateBelief.getState() != m_DesiredIbacState*/)))
        {
            IbacStateBeliefSatComm ibacStateBeliefSatComm = new IbacStateBeliefSatComm(m_BeliefManager.getName(), m_DesiredIbacState, ibacStateTimestamp);
            m_BeliefManager.put (ibacStateBeliefSatComm);
        }
        
        //Commanded frame request belief
        VideoClientFrameRequestBelief frameRequestBelief = (VideoClientFrameRequestBelief) m_BeliefManager.get(VideoClientFrameRequestBelief.BELIEF_NAME);
        Date frameRequestTimestamp = new Date (m_DesiredFrameRequest_TimeMs);
        if (m_DesiredFrameRequest_TimeMs > 0 && (frameRequestBelief == null || frameRequestBelief.getTimeStamp().before(frameRequestTimestamp)))
        {
            VideoClientFrameRequestBeliefSatComm frameRequestBeliefSatComm = new VideoClientFrameRequestBeliefSatComm(m_BeliefManager.getName(), frameRequestTimestamp);
            m_BeliefManager.put(frameRequestBeliefSatComm);
        }
                     
        //Commanded guaranteed frame request belief
        VideoClientSatCommFrameRequestBelief guaranteedFrameRequestBelief = (VideoClientSatCommFrameRequestBelief) m_BeliefManager.get(VideoClientSatCommFrameRequestBelief.BELIEF_NAME);
        Date guaranteedFrameRequestTimestamp = new Date (m_DesiredGuaranteedFrameRequest_TimeMs);
        if (m_DesiredGuaranteedFrameRequest_TimeMs > 0 && (guaranteedFrameRequestBelief == null || guaranteedFrameRequestBelief.getTimeStamp().before(guaranteedFrameRequestTimestamp)))
        {
            VideoClientSatCommFrameRequestBeliefSatComm guaranteedFrameRequestBeliefSatComm = new VideoClientSatCommFrameRequestBeliefSatComm(m_BeliefManager.getName(), m_DesiredGuaranteedDataReceipt, false, guaranteedFrameRequestTimestamp);
            m_BeliefManager.put(guaranteedFrameRequestBeliefSatComm);
        }
        
        //Commanded video converter command belief 
        VideoClientConverterCmdBelief videoConverterCmdBelief = (VideoClientConverterCmdBelief) m_BeliefManager.get(VideoClientConverterCmdBelief.BELIEF_NAME);
        Date converterCmdTimestamp = new Date (m_DesiredVideoConverterState_TimeMs);
        if (m_DesiredVideoConverterState_TimeMs > 0 && (videoConverterCmdBelief == null || videoConverterCmdBelief.getTimeStamp().before(converterCmdTimestamp)))
        {
            System.out.println("Publishing Convert Command SatComm Belief");
            VideoClientConverterCmdBeliefSatComm videoConverterCmdBeliefSatComm = new VideoClientConverterCmdBeliefSatComm(m_BeliefManager.getName(), converterCmdTimestamp);
            m_BeliefManager.put(videoConverterCmdBeliefSatComm);
        }
        
        //Commanded video recorder command belief 
        VideoClientRecorderCmdBelief recorderCmdBelief = (VideoClientRecorderCmdBelief) m_BeliefManager.get(VideoClientRecorderCmdBelief.BELIEF_NAME);
        Date recorderCmdTimestamp = new Date(m_DesiredVideoRecorderState_TimeMs);
        if (m_DesiredVideoRecorderState_TimeMs > 0 && (recorderCmdBelief == null || recorderCmdBelief.getTimeStamp().before(recorderCmdTimestamp)))
        {
            System.out.println("Publishing Record Command SatComm Belief");
            VideoClientRecorderCmdBeliefSatComm recorderCmdBeliefSatComm = new VideoClientRecorderCmdBeliefSatComm(m_BeliefManager.getName(), m_DesiredVideoRecorderState, recorderCmdTimestamp);
            m_BeliefManager.put(recorderCmdBeliefSatComm);
        }        
        
        //Commanded video stream command belief
        VideoClientStreamCmdBelief streamCmdBelief = (VideoClientStreamCmdBelief) m_BeliefManager.get(VideoClientStreamCmdBelief.BELIEF_NAME);
        Date streamCmdTimeStamp = new Date (m_DesiredVideoStreamState_TimeMs);
        if (m_DesiredVideoStreamState_TimeMs > 0 && (streamCmdBelief == null || streamCmdBelief.getTimeStamp().before(streamCmdTimeStamp)))
        {
            System.out.println("Publishing Stream Command SatComm Belief");
            
            // reconstruct the host ip address string
            String hostName = Integer.toString(m_DesiredClientHost[0]) + "." + 
                              Integer.toString(m_DesiredClientHost[1]) + "." +
                              Integer.toString(m_DesiredClientHost[2]) + "." +
                              Integer.toString(m_DesiredClientHost[3]);
            VideoClientStreamCmdBeliefSatComm streamCmdBeliefSatComm = new VideoClientStreamCmdBeliefSatComm(m_BeliefManager.getName(), hostName, m_DesiredClientPort, m_DesiredVideoStreamState, streamCmdTimeStamp);
            m_BeliefManager.put(streamCmdBeliefSatComm);
        }
        
        //Commanded Alpha state belief
        AlphaSensorStateBelief alphaStateBelief = (AlphaSensorStateBelief) m_BeliefManager.get(AlphaSensorStateBelief.BELIEF_NAME);
        Date alphaStateTimestamp = new Date (m_DesiredAlphaState_TimeMs);
        if (m_DesiredAlphaState_TimeMs > 0 && (alphaStateBelief == null || (alphaStateBelief.getTimeStamp().before(alphaStateTimestamp)/* && alphaStateBelief.getState() != m_DesiredAlphaState*/)))
        {
            AlphaSensorStateBeliefSatComm alphaStateBeliefSatComm = new AlphaSensorStateBeliefSatComm(m_BeliefManager.getName(), m_DesiredAlphaState, alphaStateTimestamp);
            m_BeliefManager.put (alphaStateBeliefSatComm);
        }
        
        //Commanded Anaconda state belief
        AnacondaStateBelief anacondaStateBelief = (AnacondaStateBelief) m_BeliefManager.get(AnacondaStateBelief.BELIEF_NAME);
        Date anacondaStateTimestamp = new Date (m_DesiredAnacondaState_TimeMs);
        if (m_DesiredAnacondaState_TimeMs > 0 && (anacondaStateBelief == null || (anacondaStateBelief.getTimeStamp().before(anacondaStateTimestamp)/* && anacondaStateBelief.getAnacondState() != m_DesiredAnacondaState*/)))
        {
            AnacondaStateBeliefSatComm anacondaStateBeliefSatComm = new AnacondaStateBeliefSatComm(m_BeliefManager.getName(), m_DesiredAnacondaState, anacondaStateTimestamp);
            m_BeliefManager.put (anacondaStateBeliefSatComm);
        }
        
        //Commanded C100 state belief
        ParticleCollectorStateBelief c100StateBelief = (ParticleCollectorStateBelief) m_BeliefManager.get(ParticleCollectorStateBelief.BELIEF_NAME);
        Date c100StateTimestamp = new Date (m_DesiredC100State_TimeMs);
        if (m_DesiredC100State_TimeMs > 0 && (c100StateBelief == null || (c100StateBelief.getTimeStamp().before(c100StateTimestamp)/* && c100StateBelief.getParticleCollectorState() != m_DesiredC100State*/)))
        {
            ParticleCollectorStateBeliefSatComm c100StateBeliefSatComm = new ParticleCollectorStateBeliefSatComm(m_BeliefManager.getName(), m_DesiredC100State, c100StateTimestamp);
            m_BeliefManager.put (c100StateBeliefSatComm);
        }
        
        //Commanded WACS mode belief
        AgentModeActualBelief agModeBelief = (AgentModeActualBelief) m_BeliefManager.get(AgentModeActualBelief.BELIEF_NAME);
        Date agentModeTimestamp = new Date (m_DesiredWacsMode_TimeMs);
        if (m_DesiredWacsMode_TimeMs > 0 && (agModeBelief == null || (agModeBelief.getTimeStamp().before(agentModeTimestamp) && !agModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals(m_DesiredWacsMode.getModeString()))))
        {
            String gimbalTargetName = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            TargetActualBelief targets = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
            if(targets != null)
            {
                PositionTimeName positionTimeName = targets.getPositionTimeName(gimbalTargetName);

                if(positionTimeName != null)
                {
                    AgentModeActualBelief actualAgModeBelief = (AgentModeActualBelief) m_BeliefManager.get(AgentModeActualBelief.BELIEF_NAME);
                    if (m_DesiredWacsMode.equals (WacsMode.INTERCEPT) && actualAgModeBelief.getMode(WACSAgent.AGENTNAME).getName().equals (LoiterBehavior.MODENAME))
                    {
                        //If this is the transition from loiter to intercept, set explosion belief
                        ExplosionBeliefSatComm explosionBeliefSatComm = new ExplosionBeliefSatComm(positionTimeName.getPosition(), agentModeTimestamp.getTime());
                        m_BeliefManager.put (explosionBeliefSatComm);
                    }
                    
                    AgentModeCommandedBeliefSatComm agentModeBeliefSatComm = new AgentModeCommandedBeliefSatComm(m_BeliefManager.getName(), m_DesiredWacsMode.getModeString(), agentModeTimestamp);
                    m_BeliefManager.put (agentModeBeliefSatComm);
                }
            }
        }
        
        //Commanded WACS waypoint settings belief
        WACSWaypointCommandedBelief wwbBelief = (WACSWaypointCommandedBelief) m_BeliefManager.get(WACSWaypointCommandedBelief.BELIEF_NAME);
        Date wwbTimestamp = new Date (m_DesiredWWB_TimestampMs);
        if (m_DesiredWWB_TimestampMs > 0 && (wwbBelief == null || (wwbBelief.getTimeStamp().before(wwbTimestamp) && 
                                                                        (Math.abs(wwbBelief.getFinalLoiterAltitude().getDoubleValue(Length.FEET) - m_DesiredLoiterAltitudeAglFt) > 1.5 || 
                                                                         Math.abs(wwbBelief.getStandoffLoiterAltitude().getDoubleValue(Length.FEET) - m_DesiredOffsetLoiterAltitudeAglFt) > 1.5 || 
                                                                        Math.abs(wwbBelief.getLoiterRadius().getDoubleValue(Length.METERS) - m_DesiredLoiterRadiusM) > 1.5 || 
                                                                        Math.abs(wwbBelief.getIntersectAltitude().getDoubleValue(Length.FEET) - m_DesiredInterceptAltitudeAglFt) > 1.5 || 
                                                                        Math.abs(wwbBelief.getIntersectRadius().getDoubleValue(Length.METERS) - m_DesiredInterceptRadiusM) > 1.5))))
        {
            WACSWaypointCommandedBeliefSatComm wwbBeliefSatComm = new WACSWaypointCommandedBeliefSatComm(m_BeliefManager.getName(), 
                    new Altitude (m_DesiredInterceptAltitudeAglFt, Length.FEET),
                    new Length (m_DesiredInterceptRadiusM, Length.METERS),
                    new Altitude (m_DesiredLoiterAltitudeAglFt, Length.FEET),
                    new Altitude (m_DesiredOffsetLoiterAltitudeAglFt, Length.FEET),
                    new Length (m_DesiredLoiterRadiusM, Length.METERS),
                    new Date (m_DesiredWWB_TimestampMs));
            m_BeliefManager.put (wwbBeliefSatComm);
        }

        //Be sure commanded allow intercept is being used
        EnableAutopilotControlCommandedBelief commandEnableBlf = (EnableAutopilotControlCommandedBelief)m_BeliefManager.get(EnableAutopilotControlCommandedBelief.BELIEF_NAME);
        boolean enableControl = (commandEnableBlf != null && commandEnableBlf.getAllow());           
        if (enableControl != m_DesiredControlAP)
        {
            EnableAutopilotControlCommandedBeliefSatComm commandEnableBlfSatComm = new EnableAutopilotControlCommandedBeliefSatComm (WACSAgent.AGENTNAME, m_DesiredControlAP, new Date (m_DesiredControlAP_TimestampMs));
            m_BeliefManager.put(commandEnableBlfSatComm);
        }
        
        //Commanded WACS gimbal target belief
        TargetCommandedBelief targetBelief = (TargetCommandedBelief) m_BeliefManager.get(TargetCommandedBelief.BELIEF_NAME);
        Date targetTimestamp = new Date (m_DesiredTargetTimestampMs);
        if (m_DesiredTargetTimestampMs > 0 && (targetBelief == null || (targetBelief.getTimeStamp().before(targetTimestamp))))
        {
            String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
            PositionTimeName ptn = null;
            if (targetBelief != null)
                ptn = targetBelief.getPositionTimeName(tmp);
            
            if (ptn == null || Math.abs(ptn.getPosition().asLatLonAltPosition().getLatitude().getDoubleValue(Angle.DEGREES) - m_DesiredTargetLatitudeDeg) > 0.0001 ||
                    Math.abs(ptn.getPosition().asLatLonAltPosition().getLongitude().getDoubleValue(Angle.DEGREES) - m_DesiredTargetLongitudeDeg) > 0.0001 ||
                    Math.abs(ptn.getPosition().asLatLonAltPosition().getAltitude().getDoubleValue(Length.FEET) - m_DesiredTargetAltitudeMslFt) > 0.0001)
            {
                LatLonAltPosition targetPos = new LatLonAltPosition(new Latitude (m_DesiredTargetLatitudeDeg, Angle.DEGREES), 
                                                                    new Longitude (m_DesiredTargetLongitudeDeg, Angle.DEGREES), 
                                                                    new Altitude (m_DesiredTargetAltitudeMslFt, Length.FEET));
                TargetCommandedBeliefSatComm newTargetBelief = new TargetCommandedBeliefSatComm(WACSAgent.AGENTNAME, targetPos, Length.ZERO, tmp, targetTimestamp);
                m_BeliefManager.put(newTargetBelief);
            }
        }
        
        //Be sure commanded allow intercept is being used
        AllowInterceptCommandedBelief commandAllowBlf = (AllowInterceptCommandedBelief)m_BeliefManager.get(AllowInterceptCommandedBelief.BELIEF_NAME);
        boolean allowIntercept = (commandAllowBlf != null && commandAllowBlf.getAllow());           
        if (allowIntercept != m_DesiredAllowIntercept)
        {
            AllowInterceptCommandedBeliefSatComm commandAllowBlfSatComm = new AllowInterceptCommandedBeliefSatComm (WACSAgent.AGENTNAME, m_DesiredAllowIntercept, new Date (m_DesiredAllowIntercept_TimestampMs));
            m_BeliefManager.put(commandAllowBlfSatComm);
        }
    }
    
    public boolean formAndSendWacsPodStatusMsg ()
    {
        int msgDestination = DESTINATION_WACSGCS;
        int msgPriority = 2;
        int msgType = MSGTYPE_PODSTATUS;
        int fullMsgLength = MSGLENGTH_PODSTATUS;
        
        //Message timestamp is based on when the data was captured, not message is formed
        long msgTimeMs = m_PodStatusFieldsTimestampMs;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_PodStatusMsgLastSentMs = System.currentTimeMillis();

        int podStatusBits = 0;
        podStatusBits |= ((m_CollectorXdLogState?0x01:0x00));
        podStatusBits |= ((m_TrackerXdLogState?0x01:0x00) << 1);
        podStatusBits |= ((m_CollectorRabbitConnected?0x01:0x00) << 2);
        podStatusBits |= ((m_TrackerRabbitConnected?0x01:0x00) << 3);
        podStatusBits |= ((m_AnacondaMode.getValue() & 0x07) << 4);            //3 bits
        podStatusBits |= ((m_AnacondaConnected?0x01:0x00) << 7);
        podStatusBits |= ((m_IbacMode?0x01:0x00) << 8);
        podStatusBits |= ((m_IbacConnected?0x01:0x00) << 9);
        podStatusBits |= ((m_C100Mode.getValue() & 0x07) << 10);               //3 bits
        podStatusBits |= ((m_C100Connected?0x01:0x00) << 13);
        podStatusBits |= ((m_AlphaMode?0x01:0x00) << 14);
        podStatusBits |= ((m_AlphaConnected?0x01:0x00) << 15);
        podStatusBits |= ((m_GammaConnected?0x01:0x00) << 16);
        podStatusBits |= ((m_DosimeterConnected?0x01:0x00) << 17);
        podStatusBits |= ((m_CollectorTHSensorConnected?0x01:0x00) << 18);
        podStatusBits |= ((m_TrackerTHSensorConnected?0x01:0x00) << 19);
        podStatusBits |= ((m_CollectorFanState?0x01:0x00) << 20);
        podStatusBits |= ((m_CollectorHeatState?0x01:0x00) << 21);
        podStatusBits |= ((m_CollectorAutoTH?0x01:0x00) << 22);
        podStatusBits |= ((m_TrackerFanState?0x01:0x00) << 23);
        podStatusBits |= ((m_TrackerHeatState?0x01:0x00) << 24);
        podStatusBits |= ((m_TrackerAutoTH?0x01:0x00) << 25);
        podStatusBits |= ((m_PiccoloConnected?0x01:0x00) << 26);
        podStatusBits |= ((m_TASEConnected?0x01:0x00) << 27);
        podStatusBits |= ((m_IRVideoRecState?0x01:0x00) << 28);
        podStatusBits |= ((m_PODVideoStreamState?0x01:0x00) << 29);
        podStatusBits |= ((m_PODVideoConversionFinished?0x01:0x00) << 30);
        podStatusBits |= ((m_SatCommImageTxFinished?0x01:0x00) << 31);
        
        // reset
        m_SatCommImageTxFinished = false;
        m_PODVideoConversionFinished = false;
        
        byte collTempC = (byte)(Math.floor(Math.max(-128, Math.min(127, m_CollectorTempC))));
        byte collHumidity = (byte)(Math.floor(Math.max(0, Math.min(100, m_CollectorHumidity))));
        byte trackerTempC = (byte)Math.floor(Math.max(-128, Math.min(127, m_TrackerTempC)));
        byte trackerHumidity = (byte)(Math.floor(Math.max(0, Math.min(100, m_TrackerHumidity))));

        int wacsStateBits = 0;
        wacsStateBits |= ((m_WacsMode.getValue() & 0x07));                //3 bits
        wacsStateBits |= ((m_AllowInterceptPermitted?0x01:0x00) << 3);
        wacsStateBits |= ((m_AllowInterceptRecommended?0x01:0x00) << 4);
        wacsStateBits |= ((m_ControlEnabledViaAutopilot?0x01:0x00) << 5);
        wacsStateBits |= ((m_WacsIsFullyAutonomous?0x01:0x00) << 6);

        short loiterRadiusM = (short)Math.round (m_LoiterRadiusM);
        short interceptRadiusM = (short)Math.round (m_InterceptRadiusM);

        short loiterAltFt = (short)Math.round (m_LoiterAltitudeAglFt);
        short interceptAltFt = (short)Math.round (m_InterceptAltitudeAglFt);
        short offsetLoiterAltFt = (short)Math.round (m_OffsetLoiterAltitudeAglFt);

        int latRadPacked = computeLatDegAsRad3BytePacked (m_CurrentTargetLatitudeDeg);
        int lonRadPacked = computeLonDegAsRad3BytePacked (m_CurrentTargetLongitudeDeg);
        short targetAltFt = (short)Math.floor (m_CurrentTargetAltitudeMslFt - DtedGlobalMap.getDted().getAltitudeMSL(m_CurrentTargetLatitudeDeg, m_CurrentTargetLongitudeDeg));

        long expTimeSec = (m_ExpectedExplosionTimeMs/1000);

        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (podStatusBits >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (podStatusBits >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (podStatusBits >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (podStatusBits));

        sendBuffer[bufferPos++] = collTempC;
        sendBuffer[bufferPos++] = collHumidity;
        sendBuffer[bufferPos++] = trackerTempC;
        sendBuffer[bufferPos++] = trackerHumidity;

        sendBuffer[bufferPos++] = (byte)(0xFF & (wacsStateBits));

        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterRadiusM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterRadiusM));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptRadiusM >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptRadiusM));

        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (loiterAltFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (interceptAltFt));
        sendBuffer[bufferPos++] = (byte)(0xFF & (offsetLoiterAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (offsetLoiterAltFt));

        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (targetAltFt));

        sendBuffer[bufferPos++] = (byte)(0xFF & (expTimeSec >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (expTimeSec >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (expTimeSec >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (expTimeSec));

        sendBuffer[bufferPos++] = 0;
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    public void checkParticleDetectionBelief()
    {
        m_ParticleDetectionMsgLastCheckedMs = System.currentTimeMillis();
        
        ParticleDetectionBelief pdb = (ParticleDetectionBelief) m_BeliefManager.get(ParticleDetectionBelief.BELIEF_NAME);
        if (pdb != null)
        {
            if (pdb.getLCI() + pdb.getSCI() > m_RecentMaxTotalParticleCountSinceLastSend && pdb.getTimeStamp().getTime() > m_ParticleDetectionMsgLastSentMs && pdb.getTimeStamp().getTime() <= m_ParticleDetectionMsgLastCheckedMs)
            {
                m_RecentMaxTotalParticleCountSinceLastSend = pdb.getLCI() + pdb.getSCI();
                m_RecentMaxTotalParticleCount_TimestampMs = pdb.getTimeStamp().getTime();
            }
        }
    }
    
    public void checkChemicalDetectionBelief()
    {
        m_ChemicalDetectionMsgLastCheckedMs = System.currentTimeMillis();
        
        AnacondaDetectionBelief adb = (AnacondaDetectionBelief) m_BeliefManager.get(AnacondaDetectionBelief.BELIEF_NAME);
        if (adb != null)
        {
            int maxBars = adb.getMaxLcdBars();
            if (maxBars > m_RecentMaxLcdBarsSinceLastSend && adb.getTimeStamp().getTime() > m_ChemicalDetectionMsgLastSentMs && adb.getTimeStamp().getTime() <= m_ChemicalDetectionMsgLastCheckedMs)
            {
                m_RecentMaxLcdBarsSinceLastSend = maxBars;
                m_RecentMaxLcdBars_TimestampMs = adb.getTimeStamp().getTime();
            }
        }
    }
    
    public CloudDetection checkCloudDetectionBelief (long prevTimeMs, long currTimeMs, int cloudSource)
    {        
        CloudDetection maxDetection = null;
        CloudDetectionBelief cBelief = (CloudDetectionBelief)m_BeliefManager.get (CloudDetectionBelief.BELIEF_NAME);
        if (cBelief != null && cBelief.getNumDetections() != 0)
        {
            synchronized (cBelief.getLock())
            {
                List <CloudDetection> detList = (List <CloudDetection>)cBelief.getDetections();
                for (CloudDetection det : detList)
                {
                    if (det.getTime() > prevTimeMs && det.getTime() <= currTimeMs)
                    {
                        //This detection happened during the time since we last tried to send this message
                        if (det.getSource() == cloudSource)
                        {
                            if (maxDetection == null || det.getScaledValue() > maxDetection.getScaledValue())
                            {
                                maxDetection = det;
                            }
                        }
                    }
                }
            }
        }
        
        return maxDetection;
    }
    
    public boolean formAndSendParticleDetectionMsg()
    {
        long currTimeMs = System.currentTimeMillis();
        long msgTimeMs = m_RecentMaxTotalParticleCount_TimestampMs;
        boolean retVal = false;
        
        if (msgTimeMs > 0 && m_RecentMaxTotalParticleCountSinceLastSend >= 0)
        {
            CloudDetection maxDetectionSinceLastSend = checkCloudDetectionBelief (m_ParticleDetectionMsgLastSentMs, currTimeMs, CloudDetection.SOURCE_PARTICLE);
            if (maxDetectionSinceLastSend != null)
                msgTimeMs = maxDetectionSinceLastSend.getTime();

            retVal = formAndSendCloudDetectionMsg (CloudDetection.SOURCE_PARTICLE, msgTimeMs, maxDetectionSinceLastSend, m_RecentMaxTotalParticleCountSinceLastSend);
        }
        m_ParticleDetectionMsgLastSentMs = currTimeMs;
        m_RecentMaxTotalParticleCountSinceLastSend = -1;
        
        return retVal;
    }
    
    public boolean formAndSendChemicalDetectionMsg()
    {
        long currTimeMs = System.currentTimeMillis();
        long msgTimeMs = m_RecentMaxLcdBars_TimestampMs;
        boolean retVal = false;
        
        if (msgTimeMs > 0 && m_RecentMaxLcdBarsSinceLastSend >= 0)
        {
            CloudDetection maxDetectionSinceLastSend = checkCloudDetectionBelief (m_ChemicalDetectionMsgLastSentMs, currTimeMs, CloudDetection.SOURCE_CHEMICAL);
            if (maxDetectionSinceLastSend != null)
                msgTimeMs = maxDetectionSinceLastSend.getTime();

            retVal = formAndSendCloudDetectionMsg (CloudDetection.SOURCE_CHEMICAL, msgTimeMs, maxDetectionSinceLastSend, m_RecentMaxLcdBarsSinceLastSend);
        }
        m_ChemicalDetectionMsgLastSentMs = currTimeMs;
        m_RecentMaxLcdBarsSinceLastSend = -1;
        
        return retVal;
    }

    public boolean formAndSendSatCommImageTransmissionSize(int transmissionSize)
    {
        int msgDestination = DESTINATION_WACSGCS;
        int msgPriority = 2;
        int msgType = MSGTYPE_PODIMAGETXSIZE;
        int fullMsgLength = MSGLENGTH_SATCOMMIMAGETXSIZE;

        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer(msgDestination, msgPriority, msgType, fullMsgLength, System.currentTimeMillis());
        if(sendBuffer== null)
        {
            System.out.println("FormAndSendVIdeoDataMsg: Failed to get multicast byte buffer!");
            return false;
        }

        ///////////////////////////////////////////////////////
        //Define message fields            
        //Get the maximum size raw data chunk that we can fit in the satcomm packets 
        int bufferPos = MSG_HEADER_SIZE;

        ///////////////////////////////////////////////////////
        //Populate message buffer with fields            
        sendBuffer[bufferPos++] = (byte)(0xFF & (transmissionSize >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (transmissionSize >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (transmissionSize >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (transmissionSize));      

        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg(sendBuffer, fullMsgLength, bufferPos, msgType);        
    }
    
    public boolean formAndSendVideoDataMsg(int segmentCount, int segmentID, byte[] segmentData)
    {
        int msgDestination = DESTINATION_WACSGCS;
        int msgPriority = 2;
        int msgType = MSGTYPE_PODIMAGEDATASEGMENT;
        int fullMsgLength = MSGLENGTH_PODVIDEODATA;

        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer(msgDestination, msgPriority, msgType, fullMsgLength, System.currentTimeMillis());
        if(sendBuffer== null)
        {
            System.out.println("FormAndSendVIdeoDataMsg: Failed to get multicast byte buffer!");
            return false;
        }

        ///////////////////////////////////////////////////////
        //Define message fields            
        //Get the maximum size raw data chunk that we can fit in the satcomm packets 
        int bufferPos = MSG_HEADER_SIZE;

        ///////////////////////////////////////////////////////
        //Populate message buffer with fields            
        sendBuffer[bufferPos++] = (byte) (0xFF & (segmentCount >> 8));
        sendBuffer[bufferPos++] = (byte) (0xFF & (segmentCount));
        sendBuffer[bufferPos++] = (byte) (0xFF & (segmentID >> 8));
        sendBuffer[bufferPos++] = (byte) (0xFF & (segmentID));
        sendBuffer[bufferPos++] = (byte) (0xFF & segmentData.length);        
        System.arraycopy(segmentData, 0, sendBuffer, bufferPos, segmentData.length);
        bufferPos += GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE;

        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg(sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    private void GetImageSegments(HashMap<Integer,byte[]> segments, byte[] imageData)
    {
        int pos = 0;
        int segmentID = 1;
        int numFullPackets = imageData.length/GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE;
        boolean hasExtraBytes = (imageData.length % GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE != 0);
        
        for( int i = 0; i < numFullPackets; i++)
        {
            byte[] segment = Arrays.copyOfRange(imageData, pos, pos + GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE);
            segments.put(segmentID, segment);
            segmentID++;
            pos += GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE;
        }
        
        if(hasExtraBytes)
        {
            byte[] segment = Arrays.copyOfRange(imageData, pos, imageData.length);
            segments.put(segmentID, segment);
        }
    }
    
    private class GuaranteedImageSendThread extends Thread
    {       
        @Override
        public void run()
        {              
            System.out.println("GuaranteedImageSendThread: GuaranteedImageSendThread started!" + "\n");
            HashMap<Integer,byte[]> outgoingImageSegments = new LinkedHashMap(); 
            HashSet deliveredSegments = new HashSet();
            byte[] outgoingData = m_ProgressiveImageBuffer;
            byte[] currentSegment = null;
            int currentSegmentID = 1;
            int currentSegmentHash = -1; 
            int dataLength = outgoingData.length;
            int segmentCount = (int)(Math.ceil((double)dataLength / (double)GUARANTEED_VIDEO_MSG_PAYLOAD_SIZE)); 
        
            // break video data into 120 byte or less segments
            GetImageSegments(outgoingImageSegments, outgoingData);
                                   
            while(true)
            {              
                //System.out.println("GuaranteedImageSendThread: running...");
                if ( (getGCSReadyForImageSegment() && !outgoingImageSegments.isEmpty()))
                {       
                    setGCSReadyForImageSegment(false);                    
                    // if received segment request and there are segments to send or if this will be the first segment to be sent
                    if(outgoingImageSegments.size() == segmentCount)
                    {
                        System.out.println("GuaranteedImageSendThread: POD received first request, ready to send " + segmentCount + " image segments..." + "\n");
                    }    
   
                    // verify delivery of segments, receipt of -1 indicates the client requesting first segment                
                    if (getLastReceivedSegmentHash() == currentSegmentHash)
                    {
                        currentSegmentID = outgoingImageSegments.keySet().iterator().next();
                        currentSegment = outgoingImageSegments.get(currentSegmentID);                        
                        currentSegmentHash = (BitSet.valueOf(currentSegment)).hashCode();
                        
                        formAndSendVideoDataMsg(segmentCount, currentSegmentID, currentSegment);
                        System.out.println("GuaranteedImageSendThread: Sent segment " + currentSegmentID + " of " + segmentCount + ", hash = " + currentSegmentHash + ", size = " + currentSegment.length + "\n");
                        outgoingImageSegments.remove(currentSegmentID);
                        deliveredSegments.add(currentSegmentHash);
                    }
                    else
                    {
                        // segment receipt did not match the last sent segment hash, resend if the segment hasn't already been delivered
                        formAndSendVideoDataMsg(segmentCount, currentSegmentID, currentSegment);
                        System.out.println("GuaranteedImageSendThread: Resent " + currentSegmentID + " of " + segmentCount + " hash = " + currentSegmentHash + "\n");
                    }
                    
                    try
                    {                                            
                        Thread.sleep(200);
                    }
                    catch(InterruptedException ex)
                    {
                        System.out.println("GuaranteedImageSendThread: GuaranteedImageSendThread interrupted!" + "\n");
                    }
                }
                else if (outgoingImageSegments.isEmpty())
                {
                    // all segments have been delivered, terminate thread
                    System.out.println("GuaranteedImageSendThread: All " + segmentCount + " image segments has been delivered!");
                    System.out.println("GuaranteedImageSendThread: GuaranteedImageSendThread terminating..." + "\n");
                    return;                       
                }
            }            
        }               
    }
    
    public boolean formAndSendCloudDetectionMsg(int cloudSource, long messageTimeMs, CloudDetection maxDetection, int maxDetectionCount)
    {
        int msgDestination = DESTINATION_WACSGCS;
        int msgPriority = 2;
        int msgType = MSGTYPE_PODSDETECTION;
        int fullMsgLength = MSGLENGTH_PODSDETECTION;
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, messageTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        short detectionType = -1;
        if (cloudSource == CloudDetection.SOURCE_PARTICLE)
            detectionType = 0x01;
        else if (cloudSource == CloudDetection.SOURCE_CHEMICAL)
            detectionType = 0x00;
        if (detectionType == -1)
            return false;
        
        short detectionAltFt = 0;
        int latRadPacked = 0;
        int lonRadPacked = 0;
        if (maxDetection == null)
        {
            detectionType |= (0x3F << 2); 
        }
        else
        {
            detectionType |= ((maxDetection.getId() & 0x3F) << 2);
        
            detectionAltFt = (short)Math.round (maxDetection.getAlt_m()/MathConstants.FT2M);
            latRadPacked = computeLatDegAsRad3BytePacked (maxDetection.getLat_deg());
            lonRadPacked = computeLonDegAsRad3BytePacked (maxDetection.getLon_deg());
        }
        
        int detectionCount = (int)maxDetectionCount;
        
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = (byte)(0xFF & (detectionType));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (latRadPacked));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (lonRadPacked));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (detectionAltFt >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (detectionAltFt));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (detectionCount >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (detectionCount));
        
        sendBuffer[bufferPos++] = 0;
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
            
    public boolean formAndSendWavsmCommandMsg()
    {
        if (m_ShadowAutopilotInterface == null)
        {
            System.out.println ("Onboard Shadow Autopilot interface not avaialble to send flight commands to");
            return false;
        }
        
        int msgDestination = DESTINATION_WAVSM;
        int msgPriority = 0;
        int msgType = MSGTYPE_PODSTOWAVSM;
        int fullMsgLength = MSGLENGTH_PODSTOWAVSM;
        
        long msgTimeMs = System.currentTimeMillis();
        
        //Get a new buffer, filling in the header information
        byte sendBuffer[] = getMulticastByteBuffer (msgDestination, msgPriority, msgType, fullMsgLength, msgTimeMs);
        if (sendBuffer == null)
            return false;
        
        int bufferPos = MSG_HEADER_SIZE;
        
        ///////////////////////////////////////////////////////
        //Define message fields
        m_SendCommandToWavsmImmediately = false;
        
        ShadowCommandMessage commandMessage = new ShadowCommandMessage ();
        m_ShadowAutopilotInterface.copyLatestCommandMessage(commandMessage);
        
        float reqRollRad = (float)commandMessage.rollCommand_rad;
        int reqRollRadINT = Float.floatToIntBits(reqRollRad);
        float reqAirspeedMps = (float)commandMessage.airspeedCommand_mps;
        int reqAirspeedMpsINT = Float.floatToIntBits(reqAirspeedMps);
        float reqAltitudeMslMeters = (float)commandMessage.altitudeCommand_m;
        int reqAltitudeMslMetersINT = Float.floatToIntBits(reqAltitudeMslMeters);
        float orbitRadiusM = (float)commandMessage.m_CurrentOrbitRadiusM;
        int orbitRadiusMINT = Float.floatToIntBits(orbitRadiusM);
        byte orbitDirectionFlag = (byte)(commandMessage.m_CurrentOrbitCWDir?1:2);
        float orbitAltMslM = (float)commandMessage.m_CurrentOrbitAltitudeMslM;
        int orbitAltMslMINT = Float.floatToIntBits(orbitAltMslM);
        float orbitAirspeedMps = (float)commandMessage.m_CurrentOrbitAirspeedMps;
        int orbitAirspeedMpsINT = Float.floatToIntBits(orbitAirspeedMps);
        double orbitLatitudeRad = commandMessage.m_CurrentOrbitLatitudeRad;
        long orbitLatitudeRadLONG = Double.doubleToLongBits(orbitLatitudeRad);
        double orbitLongitudeRad = commandMessage.m_CurrentOrbitLongitudeRad;
        long orbitLongitudeRadLONG = Double.doubleToLongBits(orbitLongitudeRad);
        float gimbalElevRad = (float)commandMessage.m_GimbalElevationRad;
        int gimbalElevRadINT = Float.floatToIntBits(gimbalElevRad);
        float gimbalAzRad = (float)commandMessage.m_GimbalAzimuthRad;
        int gimbalAzRadINT = Float.floatToIntBits(gimbalAzRad);
        float aglAltM = (float)commandMessage.m_LaserAltitudeAglM;
        int aglAltMINT = Float.floatToIntBits(aglAltM);
        float windSpeedMps = (float)commandMessage.m_WindSpeedMps;
        int windSpeedMpsINT = Float.floatToIntBits(windSpeedMps);
        float windDirFromRad = (float)commandMessage.m_WindDirFromRad;
        int windDirFromRadINT = Float.floatToIntBits(windDirFromRad);
        byte wacsModeFlag = 0;
        switch (commandMessage.m_WacsMode)
        {
            case LOITER:
                wacsModeFlag = 0;
                break;
            case INTERCEPT:
                wacsModeFlag = 1;
                break;
        }
        byte picStatusFlag = (byte)(commandMessage.m_PiccoloOperational?1:0);
        byte taseStatusFlag = (byte)(commandMessage.m_TaseOperational?1:0);
        byte windEstFlag = (byte)(commandMessage.m_WindEstimateConverged?1:0);
        
        
        ///////////////////////////////////////////////////////
        //Populate message buffer with fields
        sendBuffer[bufferPos++] = 0;
        sendBuffer[bufferPos++] = 0;
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqRollRadINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqRollRadINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqRollRadINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqRollRadINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAirspeedMpsINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAirspeedMpsINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAirspeedMpsINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAirspeedMpsINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAltitudeMslMetersINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAltitudeMslMetersINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAltitudeMslMetersINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (reqAltitudeMslMetersINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitRadiusMINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitRadiusMINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitRadiusMINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitRadiusMINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitDirectionFlag));
        
        sendBuffer[bufferPos++] = 0;
        sendBuffer[bufferPos++] = 0;
        sendBuffer[bufferPos++] = 0;
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAltMslMINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAltMslMINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAltMslMINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAltMslMINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAirspeedMpsINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAirspeedMpsINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAirspeedMpsINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitAirspeedMpsINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLatitudeRadLONG));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 56));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 48));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 40));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 32));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (orbitLongitudeRadLONG));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalElevRadINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalElevRadINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalElevRadINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalElevRadINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalAzRadINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalAzRadINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalAzRadINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (gimbalAzRadINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (aglAltMINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (aglAltMINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (aglAltMINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (aglAltMINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (windSpeedMpsINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windSpeedMpsINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windSpeedMpsINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windSpeedMpsINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (windDirFromRadINT >> 24));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windDirFromRadINT >> 16));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windDirFromRadINT >> 8));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windDirFromRadINT));
        
        sendBuffer[bufferPos++] = (byte)(0xFF & (wacsModeFlag));
        sendBuffer[bufferPos++] = (byte)(0xFF & (picStatusFlag));
        sendBuffer[bufferPos++] = (byte)(0xFF & (taseStatusFlag));
        sendBuffer[bufferPos++] = (byte)(0xFF & (windEstFlag));
        
        sendBuffer[bufferPos++] = 0;
        sendBuffer[bufferPos++] = 0;
        
        ///////////////////////////////////////////////////////
        //Send buffered message out on sat-com, computing CRC first at 'bufferPos'.
        return sendBufferedMsg (sendBuffer, fullMsgLength, bufferPos, msgType);
    }
    
    private void setGCSReadyForImageSegment(boolean ready)
    {
        synchronized(m_GCSSegmentRequestLock)
        {
            m_GCSReadyForSegment = ready;
        }
    }
    
    private boolean getGCSReadyForImageSegment()
    {
        boolean ready;
        
        synchronized(m_GCSSegmentRequestLock)
        {
            ready = m_GCSReadyForSegment;
        }
        
        return ready;
    }
    
    static public void main (String args[])
    {
        //Since double occupies 8 bytes allocate a buffer of size 8
        byte [] bytes = ByteBuffer.allocate(8).putDouble(0.701342466).array();
        System.out.println(Arrays.toString(bytes));
        if (true)
            return;
        
        try
        {
            byte multicastList[] = new byte[] {(byte)233,(byte)1,(byte)3,(byte)3};
            InetAddress multicastIpAddr = Inet4Address.getByAddress(multicastList);
            int multicastSendVsmPort = 57191;
            int multicastRecvVsmPort = 57190;
            int podStatusPeriodMs = 3000;
            int connectivityTimeoutSec = 20;
            int beliefNetworkLatency = 5000;

            PodSatCommMessageAribtrator testObj = new PodSatCommMessageAribtrator(null, multicastIpAddr, multicastSendVsmPort, multicastIpAddr, multicastRecvVsmPort, podStatusPeriodMs, connectivityTimeoutSec, beliefNetworkLatency, null);
            
            /*byte test[] = {0x7e, 0x01, 0x08, 0x04, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            int fullLength = test.length;
            testObj.fill2ByteCRC_CCITT(test, fullLength);*/
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
                    //Pod Heartbeat Message
                    CBRNHeartbeatBelief cbrnHB = (CBRNHeartbeatBelief)m_BeliefManager.get(CBRNHeartbeatBelief.BELIEF_NAME);
                    
                    //Anaconda Actual State
                    AnacondaActualStateBelief anBelief = (AnacondaActualStateBelief)m_BeliefManager.get(AnacondaActualStateBelief.BELIEF_NAME);
                    
                    //Ibac Actual State
                    IbacActualStateBelief iBelief = (IbacActualStateBelief)m_BeliefManager.get(IbacActualStateBelief.BELIEF_NAME);
                    
                    //C110 Actual Commanded State
                    ParticleCollectorActualStateBelief caBelief = (ParticleCollectorActualStateBelief)m_BeliefManager.get(ParticleCollectorActualStateBelief.BELIEF_NAME);
                    
                    //Alpha Actual State
                    AlphaSensorActualStateBelief aBelief = (AlphaSensorActualStateBelief)m_BeliefManager.get(AlphaSensorActualStateBelief.BELIEF_NAME);
                    
                    //Gamma Statistics
                    GammaStatisticsBelief gBelief = (GammaStatisticsBelief)m_BeliefManager.get(GammaStatisticsBelief.BELIEF_NAME);
             
                    //Piccolo Position Belief
                    METPositionBelief mpBelief = (METPositionBelief)m_BeliefManager.get(METPositionBelief.BELIEF_NAME);
                    
                    //TASE Telemetry Belief
                    TASETelemetryBelief ttBelief = (TASETelemetryBelief)m_BeliefManager.get(TASETelemetryBelief.BELIEF_NAME);
            
                    //WACS Agent Mode
                    AgentModeActualBelief agModeBlf = (AgentModeActualBelief) m_BeliefManager.get(AgentModeActualBelief.BELIEF_NAME);

                    //Recommend Allow Intercept
                    IrExplosionAlgorithmEnabledBelief allowBlf = (IrExplosionAlgorithmEnabledBelief) m_BeliefManager.get(IrExplosionAlgorithmEnabledBelief.BELIEF_NAME);
            
                    //WACS Waypoint Belief
                    WACSWaypointActualBelief wwBelief = (WACSWaypointActualBelief)m_BeliefManager.get(WACSWaypointActualBelief.BELIEF_NAME);

                    //Gimbal Target belief
                    TargetActualBelief targetsBelief = (TargetActualBelief)m_BeliefManager.get(TargetActualBelief.BELIEF_NAME);
                    
                    //Expected Explosion time belief
                    ExplosionTimeActualBelief expTimeBelief = (ExplosionTimeActualBelief) m_BeliefManager.get(ExplosionTimeActualBelief.BELIEF_NAME);

                    //OutGoing Guaranteed video Data Belief
                    GuaranteedVideoDataBelief videoDataBelief = (GuaranteedVideoDataBelief) m_BeliefManager.get(GuaranteedVideoDataBelief.BELIEF_NAME);                    
                    
                    //Video Recorder Status Belief
                    VideoClientRecorderStatusBelief recorderStatusBelief = (VideoClientRecorderStatusBelief) m_BeliefManager.get(VideoClientRecorderStatusBelief.BELIEF_NAME);
                    
                    //Video Streaming State Belief
                    VideoClientStreamStatusBelief videoStreamStatusBelief = (VideoClientStreamStatusBelief) m_BeliefManager.get(VideoClientStreamStatusBelief.BELIEF_NAME);
                    
                    //Video Conversion Finished Belief
                    VideoClientConversionFinishedBelief videoConvFinishedBelief = (VideoClientConversionFinishedBelief) m_BeliefManager.get(VideoClientConversionFinishedBelief.BELIEF_NAME);
                    
                    SatCommImageTransmissionFinishedBelief satcommImageTxFinishedBelief = (SatCommImageTransmissionFinishedBelief) m_BeliefManager.get(SatCommImageTransmissionFinishedBelief.BELIEF_NAME);
                    
                    long currentTimeSec = System.currentTimeMillis()/1000;
                    long dtSec;
                    
                    //Use the current time as default, in case the belief that would normally fill this in isn't around.
                    //Currently, nothing actually sets this timestamp because nothing updates often enough in the status message
                    //to make it reliably worth it.
                    m_PodStatusFieldsTimestampMs = System.currentTimeMillis();
                    
                    if(cbrnHB!=null && (cbrnHB.getTimestampMs(cbrnPodsInterface.COLLECTOR_POD) > 0 || cbrnHB.getTimestampMs(cbrnPodsInterface.TRACKER_POD) > 0))
                    {
                        //Pod 0 Heartbeat
                        long cbrnHBTimestampCollPodSec = cbrnHB.getTimestampMs(cbrnPodsInterface.COLLECTOR_POD)/1000;
                        dtSec = currentTimeSec - cbrnHBTimestampCollPodSec;
                        if (dtSec > m_ConnectivityTimeoutPeriodSec)
                            m_CollectorRabbitConnected = false;
                        else
                            m_CollectorRabbitConnected = true;

                        //Pod 1 Heartbeat
                        long cbrnHBTimestampTrackPodSec = cbrnHB.getTimestampMs(cbrnPodsInterface.TRACKER_POD)/1000;
                        dtSec = currentTimeSec - cbrnHBTimestampTrackPodSec;
                        if (dtSec > m_ConnectivityTimeoutPeriodSec)
                            m_TrackerRabbitConnected = false;
                        else
                            m_TrackerRabbitConnected = true;

                        //XD Logging State
                        m_CollectorXdLogState = cbrnHB.getActualLogStateOn(cbrnPodsInterface.COLLECTOR_POD);
                        m_TrackerXdLogState = cbrnHB.getActualLogStateOn(cbrnPodsInterface.TRACKER_POD);

                        //Temp and Humidity Readings
                        m_CollectorTempC = cbrnHB.getTemperature(cbrnPodsInterface.COLLECTOR_POD);
                        m_CollectorHumidity = cbrnHB.getHumidity(cbrnPodsInterface.COLLECTOR_POD);
                        m_TrackerTempC = cbrnHB.getTemperature(cbrnPodsInterface.TRACKER_POD);
                        m_TrackerHumidity = cbrnHB.getHumidity(cbrnPodsInterface.TRACKER_POD);

                        //Collector Temp Data Update Time
                        dtSec = cbrnHBTimestampCollPodSec - cbrnHB.getTemperatureUpdatedSec(cbrnPodsInterface.COLLECTOR_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_CollectorRabbitConnected)
                            m_CollectorTHSensorConnected = false;
                        else
                            m_CollectorTHSensorConnected = true;

                        //Tracker Temp Data Update Time
                        dtSec = cbrnHBTimestampTrackPodSec - cbrnHB.getTemperatureUpdatedSec(cbrnPodsInterface.TRACKER_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_TrackerRabbitConnected)
                            m_TrackerTHSensorConnected = false;
                        else
                            m_TrackerTHSensorConnected = true;

                        //Anaconda Data Update Time
                        dtSec = cbrnHBTimestampCollPodSec - cbrnHB.getLastERecvSec(cbrnPodsInterface.COLLECTOR_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_CollectorRabbitConnected)
                            m_AnacondaConnected = false;
                        else
                            m_AnacondaConnected = true;

                        //C100 Data Update Time
                        dtSec = cbrnHBTimestampCollPodSec - cbrnHB.getLastCRecvSec(cbrnPodsInterface.COLLECTOR_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_CollectorRabbitConnected)
                            m_C100Connected = false;
                        else
                            m_C100Connected = true;

                        //Alpha Detector Data Update Time
                        dtSec = cbrnHBTimestampTrackPodSec - cbrnHB.getLastCRecvSec(cbrnPodsInterface.TRACKER_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_TrackerRabbitConnected)
                            m_AlphaConnected = false;
                        else
                            m_AlphaConnected = true;

                        //IBAC Data Update Time
                        dtSec = cbrnHBTimestampTrackPodSec - cbrnHB.getLastERecvSec(cbrnPodsInterface.TRACKER_POD);
                        if (dtSec > m_ConnectivityTimeoutPeriodSec || !m_TrackerRabbitConnected)
                            m_IbacConnected = false;
                        else
                            m_IbacConnected = true;

                        //Heater States
                        if(cbrnHB.getHeaterToggledOn(cbrnPodsInterface.COLLECTOR_POD) == 1)
                            m_CollectorHeatState = true;
                        else
                            m_CollectorHeatState = false;

                        if(cbrnHB.getHeaterToggledOn(cbrnPodsInterface.TRACKER_POD) == 1)
                            m_TrackerHeatState = true;
                        else
                            m_TrackerHeatState = false;

                        //Fan States
                        if(cbrnHB.getfanToggledOn(cbrnPodsInterface.COLLECTOR_POD) == 1)
                            m_CollectorFanState = true;
                        else
                            m_CollectorFanState = false;

                        if(cbrnHB.getfanToggledOn(cbrnPodsInterface.TRACKER_POD) == 1)
                            m_TrackerFanState = true;
                        else
                            m_TrackerFanState = false;
                        
                        if (cbrnHB.getFanManualOverride(cbrnPodsInterface.COLLECTOR_POD) || cbrnHB.getHeaterManualOverride(cbrnPodsInterface.COLLECTOR_POD) || cbrnHB.getServoManualOverride(cbrnPodsInterface.COLLECTOR_POD))
                            m_CollectorAutoTH = false;
                        else
                            m_CollectorAutoTH = true;
                        
                        if (cbrnHB.getFanManualOverride(cbrnPodsInterface.TRACKER_POD) || cbrnHB.getHeaterManualOverride(cbrnPodsInterface.TRACKER_POD) || cbrnHB.getServoManualOverride(cbrnPodsInterface.TRACKER_POD))
                            m_TrackerAutoTH = false;
                        else
                            m_TrackerAutoTH = true;
                    }

                    if (anBelief != null)
                    {
                        //Anaconda actual mode
                        m_AnacondaMode = anBelief.getAnacondState();
                        
                        //This will ensure the selected mode is valid for the network.  If it's not, it will be changed to 'IDLE'.
                        m_AnacondaMode = AnacondaModeEnum.fromValue(m_AnacondaMode.getValue());
                    }
                    else
                        m_AnacondaMode = AnacondaModeEnum.Idle;


                    if(iBelief !=null)
                    {
                        //Ibac actual mode
                        m_IbacMode = iBelief.getState();
                    }

                    if(caBelief!=null)
                    {
                        //C100 actual mode
                        m_C100Mode = caBelief.getParticleCollectorState();
                        
                        //This will ensure the selected mode is valid for the network.  If it's not, it will be changed to 'IDLE'.
                        m_C100Mode = ParticleCollectorMode.fromValue(m_C100Mode.getValue());
                    }
                    else
                        m_C100Mode = ParticleCollectorMode.Idle;

                    if(aBelief!=null)
                    {
                        //Alpha actual mode
                        m_AlphaMode = aBelief.getState();
                    }

                    if (gBelief != null)
                    {
                        //Gamma Data Update Time
                        dtSec = currentTimeSec - gBelief.getTimeStamp().getTime()/1000L;
                        if (dtSec > m_ConnectivityTimeoutPeriodSec)
                            m_GammaConnected = false;
                        else
                            m_GammaConnected = true;
                    }

                    //////////////////////////
                    //////////////////////////
                    //TODO:  Need to implement this once the dosimeter belief exists
                    //////////////////////////
                    //////////////////////////
                    m_DosimeterConnected = false;

                    if(mpBelief!=null)
                    {
                        METPositionTimeName mtn = mpBelief.getMETPositionTimeName(WACSAgent.AGENTNAME);
                        if (mtn != null)
                        {
                            //Piccolo update time
                            dtSec = currentTimeSec - mtn.getTime().getTime()/1000L;
                            if (dtSec > m_ConnectivityTimeoutPeriodSec)
                                m_PiccoloConnected = false;
                            else
                                m_PiccoloConnected = true;
                        }
                    }

                    if(ttBelief!=null)
                    {
                        //TASE update time
                        dtSec = currentTimeSec - ttBelief.getTimeStamp().getTime()/1000L;
                        if (dtSec > m_ConnectivityTimeoutPeriodSec)
                            m_TASEConnected = false;
                        else
                            m_TASEConnected = true;
                    }

                    if (agModeBlf != null)
                    {
                        //Current WACS mode
                        Mode wacsMode = agModeBlf.getMode(WACSAgent.AGENTNAME);
                        
                        if (wacsMode != null && wacsMode.getName().equals (LoiterBehavior.MODENAME))
                            m_WacsMode = WacsMode.LOITER;
                        else if (wacsMode != null && wacsMode.getName().equals (ParticleCloudPredictionBehavior.MODENAME))
                            m_WacsMode = WacsMode.INTERCEPT;
                        else 
                            m_WacsMode = WacsMode.NO_MODE;
                        //////////////////////////
                        //////////////////////////
                        //TODO:  Need to implement more options once the possible modes exist
                        //////////////////////////
                        //////////////////////////
                        /*else if (wacsMode != null && wacsMode.getName().equals ("ingress"))
                            m_WacsMode = WacsMode.INGRESS;
                        else if (wacsMode != null && wacsMode.getName().equals ("egress"))
                            m_WacsMode = WacsMode.EGRESS;*/
                    }
                    else
                        m_WacsMode = WacsMode.NO_MODE;

                    if (allowBlf == null || (allowBlf.getEnabled()))
                    {
                        //Based on explosion time and plane position, WACS recommends having IR explosion algorithm being enabled to allow intercept
                        m_AllowInterceptRecommended = true;
                    }
                    else
                    {
                        m_AllowInterceptRecommended = false;
                    }

                    //User has allowed WACS to switch from loiter to intercept mode
                    AllowInterceptActualBelief allowInterceptBlf = (AllowInterceptActualBelief)m_BeliefManager.get(AllowInterceptActualBelief.BELIEF_NAME);
                    m_AllowInterceptPermitted = (allowInterceptBlf != null && allowInterceptBlf.getAllow());
                    
                    //User has selected to allow WACS to control autopilot
                    boolean autopilotControlEnabled = false;
                    EnableAutopilotControlActualBelief actualControl = (EnableAutopilotControlActualBelief)m_BeliefManager.get (EnableAutopilotControlActualBelief.BELIEF_NAME);
                    if (actualControl != null && actualControl.getAllow())
                        autopilotControlEnabled = true;
                    m_ControlEnabledViaAutopilot = autopilotControlEnabled;

                    //////////////////////////
                    //////////////////////////
                    //TODO:  Need to implement this once the fully autonomous WACS mode exists
                    //////////////////////////
                    //////////////////////////
                    m_WacsIsFullyAutonomous = false;

                    if(wwBelief != null)
                    {
                        //WACS waypoint settings
                        m_LoiterAltitudeAglFt = wwBelief.getFinalLoiterAltitude().getDoubleValue(Length.FEET);
                        m_OffsetLoiterAltitudeAglFt = wwBelief.getStandoffLoiterAltitude().getDoubleValue(Length.FEET);
                        m_LoiterRadiusM = wwBelief.getLoiterRadius().getDoubleValue(Length.METERS);
                        m_InterceptAltitudeAglFt = wwBelief.getIntersectAltitude().getDoubleValue(Length.FEET);
                        m_InterceptRadiusM = wwBelief.getIntersectRadius().getDoubleValue(Length.METERS);
                    }

                    if(targetsBelief != null)
                    {
                        //Gimbal target location
                        String tmp = Config.getConfig().getProperty("WACSAgent.gimbalTargetName", "gimbalTarget");
                        PositionTimeName ptn = targetsBelief.getPositionTimeName(tmp);
                        if(ptn !=null)
                        {
                            LatLonAltPosition lla = ptn.getPosition().asLatLonAltPosition();
                            m_CurrentTargetAltitudeMslFt = lla.getAltitude().getDoubleValue(Length.FEET);
                            m_CurrentTargetLatitudeDeg = lla.getLatitude().getDoubleValue(Angle.DEGREES);
                            m_CurrentTargetLongitudeDeg = lla.getLongitude().getDoubleValue(Angle.DEGREES);
                        }
                    }

                    if (expTimeBelief != null)
                    {
                        //Expected explosion time
                        m_ExpectedExplosionTimeMs = expTimeBelief.getTime_ms();
                    }            

                    if(videoDataBelief != null && videoDataBelief.getTimeStamp().getTime() > m_LastVideoDataMessageTimestampMs)
                    {
                        // if new progressive image is available, then send via sat comm 
                        m_LastVideoDataMessageTimestampMs = videoDataBelief.getTimeStamp().getTime();
                        m_ProgressiveImageBuffer = videoDataBelief.getData();
                                                
                        if ( m_GuaranteedImageSendThread != null && m_GuaranteedImageSendThread.isAlive())
                        { 
                            try
                            {
                                System.out.println("POD_SATCOMM_ARBITRATOR: Starting a new GuaranteedVideoSendThread to send progressive image with size = " + m_ProgressiveImageBuffer.length);
                                m_GuaranteedImageSendThread.interrupt();
                                m_GuaranteedImageSendThread.join();
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                        m_GuaranteedImageSendThread = new GuaranteedImageSendThread();
                        m_GuaranteedImageSendThread.start();
                    }
                    
                    if (recorderStatusBelief != null && recorderStatusBelief.getTimeStamp().getTime() > m_LastVideoRecorderStateTimestampMs)
                    {
                        //Recorder state
                        m_LastVideoRecorderStateTimestampMs = recorderStatusBelief.getTimeStamp().getTime();
                        m_IRVideoRecState = recorderStatusBelief.getState().isRecording;                        
                    }
                    
                    if (videoStreamStatusBelief != null && videoStreamStatusBelief.getTimeStamp().getTime() > m_LastVideoStreamStateTimestampMs)
                    {
                        //Video stream state
                        m_LastVideoStreamStateTimestampMs = videoStreamStatusBelief.getTimeStamp().getTime();
                        m_PODVideoStreamState = videoStreamStatusBelief.getStreamState();
                    }
                    
                    if (videoConvFinishedBelief != null && videoConvFinishedBelief.getTimeStamp().getTime() > m_LastVideoConversionFinishedTimestampMs)
                    {
                        //Video conversion finished
                        m_LastVideoConversionFinishedTimestampMs = videoConvFinishedBelief.getTimeStamp().getTime();
                        m_PODVideoConversionFinished = true;
                    }
                    
                    if (satcommImageTxFinishedBelief != null && satcommImageTxFinishedBelief.getTimeStamp().getTime() > m_LastSatCommTxCompleteTimestampMs)
                    {
                        //SatComm image transmission completed
                        m_LastSatCommTxCompleteTimestampMs = satcommImageTxFinishedBelief.getTimeStamp().getTime();
                        m_SatCommImageTxFinished = true;
                    }
                    
                    //Pod Status Message data has been updated, send it out.
                    if (System.currentTimeMillis() - m_PodStatusMsgLastSentMs > m_PodStatusMsgPeriodMs)
                        formAndSendWacsPodStatusMsg();
                    
                    if (m_SendCommandToWavsmImmediately && m_ControlEnabledViaAutopilot)
                        formAndSendWavsmCommandMsg();
                    
                    if (System.currentTimeMillis() - m_ParticleDetectionMsgLastCheckedMs > m_ParticleDetectionCheckPeriodMs)
                        checkParticleDetectionBelief();
                    
                    if (System.currentTimeMillis() - m_ParticleDetectionMsgLastSentMs > m_ParticleDetectionMsgPeriodMs)
                        formAndSendParticleDetectionMsg();
                    
                    if (System.currentTimeMillis() - m_ChemicalDetectionMsgLastCheckedMs > m_ChemicalDetectionCheckPeriodMs)
                        checkChemicalDetectionBelief();
                    
                    if (System.currentTimeMillis() - m_ChemicalDetectionMsgLastSentMs > m_ChemicalDetectionMsgPeriodMs)
                        formAndSendChemicalDetectionMsg();
                    
                    
                    formAndSendGenericPackets (DESTINATION_WACSGCS);
                    
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
