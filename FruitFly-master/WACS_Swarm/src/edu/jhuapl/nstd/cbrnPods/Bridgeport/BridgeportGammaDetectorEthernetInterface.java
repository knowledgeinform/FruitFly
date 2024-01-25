/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.util.LocalIPAddress;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface to Bridgeport Gamma detector.  Receives messages and sends commands
 *
 * @author humphjc1
 */
public class BridgeportGammaDetectorEthernetInterface extends AbstractBridgeportGammaDetectorInterface
{

    /**
     * If true, write a bunch of debugging info to the screen
     */
    static final private boolean _VERSOSE = false;
    
    
    /**
     * Maximum size for read buffer
     */
    private final int MAX_BUFFER_SIZE = 16384;
    
    /**
     * Maximum size for write buffer
     */
    private final int MAX_COMMAND_SIZE = 1024;
   
    /**
     * Size of header for output commands
     */
    private final int OUTPUT_HEADER_SIZE = 8;
    
    /**
     * Size of command to send on UDP broadcast and to receive in reply
     */
    private final int UDP_BROADCAST_LENGTH = 64;
    
    /**
     * Size of header for data input
     */
    private final int INPUT_HEADER_SIZE = 12;
    
    /**
     * Port number for sending commands to Digi board
     */
    private final int COMMAND_PORT = 9933;
    
    /**
     * Port number for receiving data from Digi board
     */
    private final int DATA_PORT = 9932;
    
    /**
     * Port number to listen to for responses from UDP broadcast
     */
    private final int BROADCAST_REPLY_PORT = 9931;
    
    /**
     * Port number for sending UDP broadcast to get all connected instruments
     */
    private final int UDP_BROADCAST_TO_PORT = 9930;

    /**
     * Port number for sending UDP broadcast to get all connected instruments
     */
    private final int UDP_BROADCAST_FROM_PORT = 9913;
    
    /**
     * Maximum file size for recording files.  Starts a new file if we exceed this value.
     */
    private final int MAX_RECORDING_FILE_BYTES = 10000000;
    //private final int MAX_RECORDING_FILE_BYTES = 1000000;

    /**
     * Reduction in replay delay.  If 10, replay is 10x faster (sleep is 10x reduced)
     */
    //private final double PLAYBACK_THREAD_DELAY_REDUCTION = 30000.0;
    private final double PLAYBACK_THREAD_DELAY_REDUCTION = 5.0;
    
    /**
     * File size limit for flat files
     */
    private int MAX_FLAT_FILE_BYTES;
   
    /**
     * Bridgeport definitions
     */
    private final int CH_CTRL = 0;
    private final int CH_READ_SEL = 1;
    private final int CH_READ_FIRST = 2;
    private final int CH_RW_DEVNUM = 3;
    private final int CH_RW_INSTRNUM = 4;
    private final int CH_CMD_GROUP = 5;
    private final int CH_CMD_COMMAND = 6;
    private final int CH_CMD_ACTIONS = 7;
    private final int CH_CMD_DATA = 8;
    
    /**
     * Bridgeport definitions
     */
    private final short CMD_DONE = 0;
    private final short CMD_DAQ = 1;
    private final short CMD_SPI = 2;
    private final short CMD_CPU = 3;
    private final short CMD_ICOM = 4;
    private final short CMD_CCOM = 5;
    private final short CMD_API = 6;
    
    /**
     * Bridgeport definitions
     */
    private final short DAQ_CMD_SETUP = 0;
    private final short DAQ_CMD_SCAN = 1;
    private final short DAQ_CMD_HISTO = 2;
    private final short DAQ_CMD_LM = 3;
    private final short DAQ_CMD_TRACE = 4;
    
    /**
     * Bridgeport definitions
     */
    private final int DSETUP_SELECT = 0;
    private final int DSETUP_ACTIONS = 1;
    private final int DSETUP_R1 = 2;
    private final int DSETUP_R2 = 3;
    private final int DSETUP_DATA = 4;
    
    /**
     * Bridgeport definitions
     */
    private final int STATS_PERIOD = 0;
    private final int HISTO_PERIOD = 1;
    private final int MODE = 2;
    private final int CH_PATTERN = 3;
    
    /**
     * Bridgeport definitions
     */
    private final int DH_LEN = 0;
    private final int DH_FORMAT = 1;
    private final int DH_GRP = 2;
    private final int DH_TYPE = 3;
    private final int DH_DEVNUM = 4;
    private final int DH_CHNUM = 5;
    private final int DH_INSTRNUM = 6;
    private final int DH_NUM_BYTES = 8;
    
    /**
     * Bridgeport definitions
     */
    private final int FORMAT_CHAR = 0;
    private final int FORMAT_UCHAR = 1;
    private final int FORMAT_SHORT = 2;
    private final int FORMAT_USHORT = 3;
    private final int FORMAT_LONG = 4;
    private final int FORMAT_ULONG = 5;
    private final int FORMAT_XLONG = 6;
    private final int FORMAT_UXLONG = 7;
    private final int FORMAT_FLOAT = 8;
    private final int FORMAT_DOUBLE = 9;
    
    /**
     * Bridgeport definitions
     */
    private final int DH_TYPE_STATUS = 0;
    private final int DH_TYPE_VERSION = 1;
    private final int DH_TYPE_CALIBRATION = 2;
    private final int DH_TYPE_SETUP_F = 3;
    private final int DH_TYPE_SETUP_I = 4;
    private final int DH_TYPE_SETUP_C = 5;
    private final int DH_TYPE_RATES = 6;
    private final int DH_TYPE_HISTO = 7;
    private final int DH_TYPE_HISTO1 = 8;
    private final int DH_TYPE_LM = 9;
    private final int DH_TYPE_LLM = 10;
    private final int DH_TYPE_TRACE = 11;
    private final int DH_TYPE_LTRACE = 12;
    private final int DH_TYPE_ECHO = 13;
    private final int DH_TYPE_CCRATES = 14;
    private final int DH_TYPE_JHU = 15;
    
    /**
     * Bridgeport definitions
     */
    private final int CR_REAL_TICKS = 0;
    private final int CR_EVENTS = 1;
    private final int CR_TRIGGERS = 2;
    private final int CR_DEAD_TICKS = 3;
    private final int CR_REAL_TIME = 4;
    private final int CR_EVENT_RATE = 5;
    private final int CR_TRIGGER_RATE = 6;
    private final int CR_DEAD_TIME_FRACTION = 7;
    private final int CR_INPUT_RATE = 8;
    private final int CR_R1 = 9;
    
    /**
     * Bridgeport definitions
     */
    private final short CTRL_AUTO_SETUP = 1;
    private final short CTRL_SETUP_F = 2;
    private final short CTRL_SETUP_I = 3;
    private final short CTRL_SETUP_C = 4;
    private final short CTRL_RAD_SCAN = 5;
    private final short CTRL_TRACE = 6;
    private final short CTRL_LISTMODE = 7;
    private final short CTRL_PAR_SCAN = 8;
    
    /**
     * Indexes relating to type of message from the Bridgeport sensor
     */
    private final int LOG_HISTOGRAM = 0;
    private final int LOG_STATISTICS = 1;
    private final int LOG_CALIBRATION = 2;
    
    /**
     * Socket used to send commands to digi board
     */
    private Socket m_CommandSocket;
    
    /**
     * Socket used to listen for data connections from digi board
     */
    private ServerSocket m_DataSocketListener;
    
    /**
     * Socket used to get actual data from digi board
     */
    private Socket m_DataSocket;
    
    /**
     * Output stream to write bytes to socket
     */
    private OutputStream m_SocketOutputStream;
    
    /**
     * Input stream to read bytes from socket
     */
    private DataInputStream m_SocketInputStream;
    
    /**
     * Buffer array to read data header from socket connection
     */
    private ByteBuffer m_InputHeader = ByteBuffer.allocate(INPUT_HEADER_SIZE);
    
    /**
     * Endianness of input data array, specified by input header
     */
    private boolean m_InputBufferBigEndian = true;
    
    /**
     * Buffer array to read data from socket connection
     */
    private ByteBuffer m_InputBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);

    /**
     * Buffer array to read data from socket connection
     */
    private ByteBuffer m_ExtraBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);

    /**
     * If true, extra bytes wound up in a TCP packet and we should parse them out
     */
    private boolean m_ProcessExtraBytes = false;

    /**
     * If processExtraBytes is true, the number of extra bytes that wound up in the TCP packet
     */
    private int m_ExtraBytesReceived = 0;
    
    /**
     * Endianness of output header array
     */
    private boolean m_OutputHeaderBigEndian = false;
    
    /**
     * Buffer array to form header for output commands in, words are 16 bit, so need to multiply size by 2
     */
    private ByteBuffer m_OutputHeader = ByteBuffer.allocate(OUTPUT_HEADER_SIZE * 2);
    
    /**
     * Endianness of output command array
     */
    private boolean m_OutputBufferBigEndian = false;
    
    /**
     * Buffer array to form output commands in
     */
    private ByteBuffer m_OutputBuffer = ByteBuffer.allocate(MAX_COMMAND_SIZE);
    
    /**
     * Array to form actual command that will be sent
     */
    private byte[] m_OutputCommand = new byte[m_OutputBuffer.capacity() + m_OutputHeader.capacity()];
    
    /**
     * True until a histogram has been read.  Allows the first one to be zeroes without reconnecting
     */
    private boolean m_FirstHistoRead = true;
    
    /**
     * Locking object for input buffer processing
     */
    private final Object m_Lock = new Object();
    
    
    /**
     * Allow external object to lock on input buffer processing
     * @return Locking object
     */
    public Object getLock() {
        return m_Lock;
    }
    
    /**
     * Histogram object to store the most recent data captured from the gamma detector
     */
    private RNHistogram m_LastData_Histogram = null;
    
    /**
     * Object storing count rate information from the most recent message from the gamma detector
     */
    private GammaEthernetCountRates m_LastData_CountRates = null;

    /**
     * Object storing calibration data from the most recent message from the gamma detector
     */
    private GammaCalibration m_LastData_Calibration = null;
    
    /**
     * IP Address of Digi board
     */
    private String m_IPAddress;
    
    /**
     * True once thread has progressed far enough to start the socket listener methods.  Will pause request for polling
     * until true
     */
    private boolean m_SocketListenerStarted = false;
    
    /**
     * True once thread has progressed far enough to start the broadcast reply listener methods.  Will pause UDP broadcast
     * until true
     */
    private boolean m_BroadcastListenerStarted = false;
    
    /**
     * Counter for filenames to differentiate if they get too large
     */
    private int m_LogFileCounter = 0;

    /**
     * Counter for filenames to differentiate if they get too large
     */
    private int m_RawFileCounter = 0;

    /**
     * Counter for filenames to differentiate flat files if they get too large
     */
    private int m_FlatFileCounter = 0;
    
    /**
     * Name of output file name to log data
     */
    private String m_LogFilename;

    /**
     * Name of output file name to log raw tcp data
     */
    private String m_RawFilename;

    /**
     * Output stream to save data from Bridgeport gamma detector
     */
    private DataOutputStream m_LogStream = null;

    /**
     * Output stream for tcp stream data
     */
    private DataOutputStream m_RawStream = null;
    
    /**
     * Output file that m_LogStream points to right now, can change
     */
    private File m_CurrentLogFile = null;

    /**
     * Output file that m_RawStream points to right now, can change
     */
    private File m_CurrentRawFile = null;
    
    /**
     * Buffered writer object to save flat files to
     */
    private BufferedWriter m_FlatFileWriter = null;
    
    /**
     * Flat file object
     */
    private File m_FlatFile = null;

    /**
     * If true, broadcast thread will be run until a connection is made.  Should be true unless the Bridgeport sensor is already sending
     * messages
     */
    private boolean runBroadcastThread = true;
    
    /**
     * List of listeners for Bridgeport messages.  These listeners must be registered and will receive all messages from sensor
     */
    private LinkedList <BridgeportEthernetMessageListener> _listeners = new LinkedList<BridgeportEthernetMessageListener>();
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURABLE SETTINGS
    /////////////////////////////////////////////////////////////////////////////////////////////
   
    /**
     * Tube number, labeled on side of tube.
     */
    private int m_TubeNumber;

    /**
     * Predefined setup index to use.  If zero, we will use the setup variables listed here.  If nonzero,
     * we will tell the MCA to use predefined settings that are loaded on it.  Maximum of 7
     */
    private short m_Setup_UsePredefinedSetup;
    
    /**
     * If true, user specifies the HV value
     */
    private boolean m_Setup_HVSpecified;
    
    /**
     * If m_HVSpecified is true, then this is the HV value to send to the MCA to use
     */
    private float m_Setup_UserSpecifiedHV;
    
    /**
     * Boolean flag for whether HV is on
     */
    private boolean m_Setup_HVOn;
    
    /**
     * Period at which statistics are updated in instrument memory.  Set to zero to suppress
     */
    private float m_Scan_StatisticsPeriod;
    
    /**
     * Period at which histograms are updated in instrument memory.  Set to zero to suppress
     */
    private float m_Scan_HistogramPeriod;
    
    /**
     * If true, enable histogram bank switching
     */
    private boolean m_Scan_HistogramBankSwitching;
    
    /**
     * If true, clear statistics after updates
     */
    private boolean m_Scan_ClearStatisticsUpdate;
    
    /**
     * If true, clear histograms after updates
     */
    private boolean m_Scan_ClearHistogramUpdate;

    /**
     * If true, report pulse heights in histogram instead of energies
     */
    private boolean m_Scan_HistogramPulseHeights;
    
    /**
     * Not really sure, but Bridgeport said we can ignore it and leave it zero.
     */
    private int m_Scan_ChannelPattern;
    
    /**
     * If true, save all data received from the sensor to file, otherwise save nothing
     */
    private boolean m_LogData;
    
    /**
     * If true, save all raw tcp data to file
     */
    private boolean m_LogTcpData;

    /**
     * If true, save all data to a flat file, assuming periods are the same
     */
    private boolean m_SaveFlatFile;
    
    /**
     * Folder to save logs and flat files to
     */
    private String m_LogFolderName;

    /**
     * FineGain
     */
    short m_Setup_FineGain;

    /**
     * BaselineThresh
     */
    short m_Setup_BaselineThresh;

    /**
     * PulseThresh
     */
    short m_Setup_PulseThresh;

    /**
     * HoldoffTime
     */
    short m_Setup_HoldoffTime;

    /**
     * IntegrationTime
     */
    short m_Setup_IntegrationTime;

    /**
     * ROI
     */
    short m_Setup_ROI;

    /**
     * PretriggerTime
     */
    short m_Setup_PretriggerTime;

    /**
     * RequestLowWord
     */
    short m_Setup_RequestLowWord;

    /**
     * RequestHighWord
     */
    short m_Setup_RequestHighWord;

    /**
     * PIDTime
     */
    short m_Setup_PIDTime;

    /**
     * PileUp
     */
    short m_Setup_PileUp;

    /**
     * Gain
     */
    short m_Setup_Gain;

    /**
     * Masks
     */
    short m_Setup_Masks;

    /**
     * Pulser
     */
    short m_Setup_Pulser;

    /**
     * Actions
     */
    short m_Setup_Actions;


    /**
     * Constructor, sets up log files and begins data receive thread
     * @param belMgr Belief Manager from Swarm.  Can be null, but must be defined to include proper metrics data in log files
     */

    public BridgeportGammaDetectorEthernetInterface(BeliefManager belMgr)
    {
        super(belMgr);
    	this.setName ("WACS-BridgeportInterface");
        setDaemon(true);
        readConfig ();
        beliefManager = belMgr;
        
        m_IPAddress = null;
        m_CommandSocket = null;
        m_DataSocket = null;
        m_DataSocketListener = null;
        
        //Set up log files
        while (m_LogFolderName.endsWith ("\\") || m_LogFolderName.endsWith("/"))
            m_LogFolderName = m_LogFolderName.substring(0, m_LogFolderName.length() - 1);
        
        try
        {
            File outputFolder = new File (m_LogFolderName);
            if (!outputFolder.exists())
                outputFolder.mkdirs();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        m_LogFolderName += "/";
        

        m_LogFilename = m_LogFolderName + "BridgeportGammaRaw_" + System.currentTimeMillis() + "_";
        m_RawFilename = m_LogFolderName + "BridgeportGammaTcpRaw_" + System.currentTimeMillis() + "_";

        if (m_OutputHeaderBigEndian) 
            m_OutputHeader.order(ByteOrder.BIG_ENDIAN);
        else
            m_OutputHeader.order(ByteOrder.LITTLE_ENDIAN);
        
        if (m_OutputBufferBigEndian) 
            m_OutputBuffer.order(ByteOrder.BIG_ENDIAN);
        else 
            m_OutputBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        m_InputHeader.order(ByteOrder.BIG_ENDIAN);
        
        if (m_Scan_HistogramPeriod != m_Scan_StatisticsPeriod)
        {
            m_SaveFlatFile = false;
            System.err.println ("Can't save flat file if periods aren't the same!");
        }

        //Start dat receive thread
        this.start();
    }
    
    /**
     * Constructor to preprare for playback of .dat files
     * 
     * @param dataFile Log file to being playback from
     * @param tcpStream If true, data file is raw TCP data.  If false, data is formatted by this interface
     */
//    public BridgeportGammaDetectorEthernetInterface(String dataFile, boolean tcpStream)
//    {
//        setDaemon(true);
//        
//        m_PlaybackEnabled = true;
//        m_LogFileCounter = parseLogCounter (dataFile);
//        
//        //Trim off the .dat and index counter of the filename for easier file changes
//        int endIdx = dataFile.lastIndexOf("_") + 1;
//        m_LogFilename = dataFile.substring(0, endIdx);
//        
//        this.start();
//        
//    }

    /**
     * Read config settings
     */
    void readConfig ()
    {
        Config config = Config.getConfig();

        m_TubeNumber = config.getPropertyAsInteger("Bridgeport.GammaTubeNumber");
        m_Setup_UsePredefinedSetup = (short) config.getPropertyAsInteger("Bridgeport.UsePredefinedSetup", 2);
        m_Setup_HVSpecified = config.getPropertyAsBoolean("Bridgeport.HVSpecified", true);
        m_Setup_UserSpecifiedHV = (float) config.getPropertyAsDouble("Bridgeport.UserSpecifiedHV.Volt.Tube" + m_TubeNumber, 1100);
        m_Setup_HVOn = config.getPropertyAsBoolean("Bridgeport.HVOn", true);
        m_Scan_HistogramPeriod = (float) config.getPropertyAsDouble("Bridgeport.HistogramPeriod.Secs", 5);
        m_Scan_StatisticsPeriod = (float) config.getPropertyAsDouble("Bridgeport.StatisticsPeriod.Secs", 10);
        m_Scan_HistogramBankSwitching = config.getPropertyAsBoolean("Bridgeport.HistogramBankSwitching", false);
        m_Scan_ClearStatisticsUpdate = config.getPropertyAsBoolean("Bridgeport.ClearStatisticsOnUpdate", true);
        m_Scan_ClearHistogramUpdate = config.getPropertyAsBoolean("Bridgeport.ClearHistogramOnUpdate", true);
        m_Scan_HistogramPulseHeights = config.getPropertyAsBoolean("Bridgeport.HistogramPulseHeights", false);
        m_Scan_ChannelPattern = config.getPropertyAsInteger("Bridgeport.ChannelPattern", 0);
        m_LogData = config.getPropertyAsBoolean("Bridgeport.LogData", true);
        m_LogTcpData = config.getPropertyAsBoolean("Bridgeport.LogTcpData", true);
        m_SaveFlatFile = config.getPropertyAsBoolean("Bridgeport.SaveFlatFile", true);
        m_LogFolderName = config.getProperty("Bridgeport.LogFolder", "./GammaLogs");
        MAX_FLAT_FILE_BYTES = config.getPropertyAsInteger("Bridgeport.FlatFileMaxSize.Bytes", 1000000);


        m_Setup_FineGain = (short)config.getPropertyAsInteger("Bridgeport.FineGain.Tube" + m_TubeNumber, 20000);
        m_Setup_BaselineThresh = (short)config.getPropertyAsInteger("Bridgeport.BaselineThreshold.Tube" + m_TubeNumber, 10);
        m_Setup_PulseThresh = (short)config.getPropertyAsInteger("Bridgeport.PulseThreshold.Tube" + m_TubeNumber, 25);
        m_Setup_HoldoffTime = (short)config.getPropertyAsInteger("Bridgeport.HoldOffTime", 100);
        m_Setup_IntegrationTime = (short)config.getPropertyAsInteger("Bridgeport.IntegrationTime", 24);
        m_Setup_ROI = (short)config.getPropertyAsInteger("Bridgeport.ROI", 24);
        m_Setup_PretriggerTime = (short)config.getPropertyAsInteger("Bridgeport.PretriggerTime", 100);
        m_Setup_RequestLowWord = (short)config.getPropertyAsInteger("Bridgeport.RequestLowWord", 38531);
        m_Setup_RequestHighWord = (short)config.getPropertyAsInteger("Bridgeport.RequestHighWord", 5);
        m_Setup_PIDTime = (short)config.getPropertyAsInteger("Bridgeport.PIDTime", 0);
        m_Setup_PileUp = (short)config.getPropertyAsInteger("Bridgeport.PileUp", 0);
        m_Setup_Gain = (short)config.getPropertyAsInteger("Bridgeport.Gain", 0);
        m_Setup_Masks = (short)config.getPropertyAsInteger("Bridgeport.Masks", 2);
        m_Setup_Pulser = (short)config.getPropertyAsInteger("Bridgeport.Pulser", 0);
        m_Setup_Actions = (short)config.getPropertyAsInteger("Bridgeport.Actions", 0);

    }
    
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    public void addBridgeportMessageListener(BridgeportEthernetMessageListener listener)
    {
    	_listeners.add(listener);
    }
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    public void removeBridgeportMessageListener(BridgeportEthernetMessageListener listener)
    {
    	_listeners.remove(listener);
    }
    
    /**
     * Notify listeners of a histogram message
     * 
     * @param msg Message to send
     */
    void notifyListeners (RNHistogram msg)
    {
        Iterator <BridgeportEthernetMessageListener> itr;
        itr = _listeners.listIterator();
        while (itr.hasNext())
        {
            BridgeportEthernetMessageListener listener = (BridgeportEthernetMessageListener) itr.next();
            listener.handleHistogramMessage(msg);
        }
    }
    
    /**
     * Notify listeners of a count rate message
     * 
     * @param msg Message to send
     */
    void notifyListeners (GammaEthernetCountRates msg)
    {
        Iterator <BridgeportEthernetMessageListener> itr;
        itr = _listeners.listIterator();
        while (itr.hasNext())
        {
            BridgeportEthernetMessageListener listener = (BridgeportEthernetMessageListener) itr.next();
            listener.handleStatisticsMessage(msg);
        }
    }

    /**
     * Notify listeners of a count rat message
     *
     * @param msg Message to send
     */
    void notifyListeners (GammaCalibration msg)
    {
        Iterator <BridgeportEthernetMessageListener> itr;
        itr = _listeners.listIterator();
        while (itr.hasNext())
        {
            BridgeportEthernetMessageListener listener = (BridgeportEthernetMessageListener) itr.next();
            listener.handleCalibrationMessage(msg);
        }
    }
    

    /**
     * Connect to the digi board controlling the Bridgeport gamma detector over ethernet connection.  Set-up streams.  We have modified
     * the digi board so that it maintains a static IP address.
     * 
     * @return
     */
    private boolean initializeOutput() 
    {
        try 
        {
            m_CommandSocket = new Socket(m_IPAddress, COMMAND_PORT);
            m_SocketOutputStream = m_CommandSocket.getOutputStream();
            if (_VERSOSE)
                System.out.println("Connected to command socket.");
        } 
        catch (Exception e) 
        {
            System.err.println("Error: Could not connect to socket at \"" + m_IPAddress + "\" on port: " + COMMAND_PORT + ".\n  Connect Bridgeport gamma detector failed.");
            return false;
        }

        return true;
    }

    /**
     * Populate the first 7 words (14 bytes) of the command header when CMD_GROUP is DAQ.  Uses the specified command number
     * 
     * @param commandNumber Command number relating to the list of options in Ch 3, DAQ Commands, of Command and Data Interface
     */
    private void setupDaqHeader(short commandNumber) 
    {
        short ctrlVal = 0;
        ctrlVal |= (m_OutputHeaderBigEndian ? 0x0001 : 0x0000);
        ctrlVal |= (m_OutputBufferBigEndian ? 0x0002 : 0x0000);
        if (commandNumber == DAQ_CMD_SETUP) 
            ctrlVal |= 0x0004;        
        
        //We force the data to be inserted as little endian per the spec
        m_OutputHeader.order(ByteOrder.LITTLE_ENDIAN);
        m_OutputHeader.putShort(CH_CTRL * 2, ctrlVal);

        //Switch back to what we wanted, if big endian
        if (m_OutputHeaderBigEndian) 
            m_OutputHeader.order(ByteOrder.BIG_ENDIAN);
 
        short zeroS = 0;
        m_OutputHeader.putShort(CH_READ_SEL * 2, zeroS);

        m_OutputHeader.putShort(CH_READ_FIRST * 2, zeroS);

        short oneS = 1;
        m_OutputHeader.putShort(CH_RW_DEVNUM * 2, zeroS);

        m_OutputHeader.putShort(CH_RW_INSTRNUM * 2, zeroS);

        short command = CMD_DAQ;
        m_OutputHeader.putShort(CH_CMD_GROUP * 2, command);

        command = commandNumber;
        m_OutputHeader.putShort(CH_CMD_COMMAND * 2, command);

        m_OutputHeader.putShort(CH_CMD_ACTIONS * 2, zeroS);
    }

    /**
     * Send requested settings command to Bridgeport.
     *
     * @return True if succesfully sent
     */
    private boolean sendSettings() 
    {
        setupDaqHeader(DAQ_CMD_SETUP);


        //Setup Setting
        short select = m_Setup_UsePredefinedSetup;
        //Apparently only bit patterns is supported, so bits 3=0, 4=1
        select |= 0x0010;
        //HV on?
        select |= 0x0020 * (m_Setup_HVSpecified ? 1 : 0);
        m_OutputBuffer.putShort(DSETUP_SELECT * 2, select);

        select = 0x0003;
        select |= 0x0004 * (m_Setup_HVOn ? 1 : 0);
        m_OutputBuffer.putShort(DSETUP_ACTIONS * 2, select);

        short zeroS = 0;
        m_OutputBuffer.putShort(DSETUP_R1 * 2, zeroS);

        m_OutputBuffer.putShort(DSETUP_R1 * 2, zeroS);


        //Can use predefined setups within Bridgeport sensor or settings defined in config file
        if (m_Setup_UsePredefinedSetup > 0 && m_Setup_UsePredefinedSetup < 8)
        {
            m_OutputBuffer.putFloat(DSETUP_DATA * 2, m_Setup_UserSpecifiedHV);

            if (!sendCommand(12)) 
                return false;
            
        } 
        else if (m_Setup_UsePredefinedSetup > 7) 
        {
            System.err.println("The MCA interface supports predefined settings only up to option 7.  Choose a");
            System.err.println("predefined setup option for Bridgeport gamma detector.");

            return false;
        } 
        else 
        {
            int itr = 0;
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_FineGain);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_BaselineThresh);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_PulseThresh);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_HoldoffTime);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_IntegrationTime);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_ROI);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_PretriggerTime);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, (short)m_Setup_UserSpecifiedHV);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_RequestLowWord);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_RequestHighWord);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_PIDTime);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_PileUp);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_Gain);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_Masks);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_Pulser);
            m_OutputBuffer.putShort((DSETUP_DATA+itr++) * 2, m_Setup_Actions);

            if (!sendCommand((DSETUP_DATA+itr) * 2))
                return false;
        }

        return true;
    }

    /**
     * Write the contents of the header and output buffer to the socket stream.  Uses the specified length parameter to 
     * send only a portion of the output buffer
     * 
     * @param length Number of bytes to send from the output buffer
     */
    private boolean sendCommand(int length) 
    {
        if (!waitForListener()) 
            return false;        
        
        //Connect to socket every time.
        if (!initializeOutput()) 
            return false;

        //Add header bytes
        byte[] header = m_OutputHeader.array();
        for (int i = 0; i < header.length; i++) {
            m_OutputCommand[i] = header[i];
        }

        //Add command bytes
        byte[] command = m_OutputBuffer.array();
        for (int i = 0; i < length; i++) {
            m_OutputCommand[i + header.length] = command[i];
        }

        //Send command
        try 
        {
            m_SocketOutputStream.write(m_OutputCommand, 0, header.length + length);
            m_CommandSocket.close();
            if (_VERSOSE)
                System.out.println("Command sent");
            
            return true;
        } 
        catch (Exception e) 
        {
            System.err.println("Could not send output command to network socket for Bridgeport gamma detector");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hangs until the listener thread for messages has been started
     *
     * @return True if listener thread is started, false otherwise
     */
    private boolean waitForListener() 
    {
        int numTries = 0;
        int maxTries = 500;
        while (!m_SocketListenerStarted && numTries++ < maxTries) 
        {
            try 
            {
                Thread.sleep(100);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(BridgeportGammaDetectorEthernetInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Don't wait all day, time out eventually.
        if (numTries >= maxTries) 
        {
            if (_VERSOSE)
                System.out.println("Error - thread to listen for incoming data sockets from Bridgeport gamma detector wasn't started quick enough.");
            return false;
        }
        return true;

    }

    /**
     * Hangs until listener thread for broadcast replies has been started
     *
     * @return True if listener thread is started
     */
    private boolean waitForBroadcastListener() {
        int numTries = 0;
        int maxTries = 500;
        while (!m_BroadcastListenerStarted && numTries++ < maxTries) 
        {
            try 
            {
                Thread.sleep(100);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(BridgeportGammaDetectorEthernetInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Don't wait all day, time out eventually.
        if (numTries >= maxTries) 
        {
            System.out.println("Error - thread to listen for incoming broadcast replies from Bridgeport gamma detector wasn't started quick enough.");
            return false;
        }
        return true;

    }

    /**
     * This will send the DAQ_CMD_SCAN command to the gamma sensor to start polling from the board.
     * @return boolean
     */
    public boolean requestPolling() 
    {
        if (!sendSettings()) 
            return false;
        
        setupDaqHeader(DAQ_CMD_SCAN);

        //For histogram bank switching, periods must be equal
        if (m_Scan_HistogramBankSwitching && (!(Math.abs(m_Scan_HistogramPeriod - m_Scan_StatisticsPeriod) < 0.00001))) 
            return false;
        
        m_OutputBuffer.putFloat(STATS_PERIOD * 4, m_Scan_StatisticsPeriod);

        m_OutputBuffer.putFloat(HISTO_PERIOD * 4, m_Scan_HistogramPeriod);

        int mode = 0;
        mode |= 0x0001 * (m_Scan_HistogramBankSwitching ? 1 : 0);
        //Auto report statistics and histograms, bits 1 and 2
        mode |= 0x0006;
        mode |= 0x0008 * (m_Scan_ClearStatisticsUpdate ? 1 : 0);
        mode |= 0x0010 * (m_Scan_ClearHistogramUpdate ? 1 : 0);
        //Don't stop DAQ so bit 5 stays 0
        mode |= 0x0040 * (m_Scan_HistogramPulseHeights ? 1 : 0);
        //m_OutputBuffer.putInt(MODE*4, mode);
        m_OutputBuffer.putFloat(MODE * 4, (float) mode);

        //Channel pattern
        m_OutputBuffer.putInt(CH_PATTERN * 4, m_Scan_ChannelPattern);

        if (!sendCommand(16)) 
            return false;
        
        return true;

    }

    /**
     * This will send the stop command to gracefully stop the DAQ from the Bridgeport sensor
     * 
     * @return True if message sent, false otherwise
     */
    public boolean sendStopCommand() 
    {
        setupDaqHeader(DAQ_CMD_SCAN);

        m_OutputBuffer.putFloat(STATS_PERIOD * 4, 0);

        m_OutputBuffer.putFloat(HISTO_PERIOD * 4, 0);

        int mode = 32;
        //stop DAQ 
        m_OutputBuffer.putFloat(MODE * 4, (float) mode);

        //Channel pattern
        m_OutputBuffer.putInt(CH_PATTERN * 4, m_Scan_ChannelPattern);

        if (!sendCommand(16)) 
            return false;
        
        return true;

    }

    /**
     * Read Bridgeport message data from the socket
     *
     * @param buffer Buffer to read into
     * @param offset Offset into buffer to read to
     * @param toRead Numebr of bytes to read
     * @return Number of bytes read
     * @throws IOException
     */
    private int readSocket(byte[] buffer, int offset, int toRead) throws IOException
    {
        int bytes = m_SocketInputStream.read(buffer, offset, toRead);

        // Log raw TCP bytes
        if (bytes > 0)
            saveRawTcpBytes(buffer, bytes);

        return bytes;
    }

    /**
     * Read message data from Bridgeport sensor
     *
     * @return True if message received
     * @throws IOException
     */
    private boolean readFromSensor() throws IOException 
    {
        int numBytesReceived = 0;
        int addBytes = 0;
        int numTries = 0;

        try 
        {
            do 
            {
                //Try to read header bytes from message first
                addBytes = readSocket(m_InputHeader.array(), numBytesReceived, INPUT_HEADER_SIZE - numBytesReceived);

                if (addBytes > 0)
                    numBytesReceived += addBytes;
                Thread.sleep(1);
                
            } while (numBytesReceived < INPUT_HEADER_SIZE && numTries++ < 10);  //Loop until entire header is read

        } 
        catch (Exception e) 
        {
            throw new IOException("Could not retrieve data header from Bridgeport gamma detector.");
        }

        //If full header has been read...
        if (numBytesReceived == INPUT_HEADER_SIZE) 
        {
            int headerSize = (int) m_InputHeader.get(DH_LEN);
            if (headerSize == INPUT_HEADER_SIZE) 
            {
                //We got a full header and it most likely is in sync.  Get number of bytes for data and read.  Processing is done later
                int dataBytes = (int) m_InputHeader.getInt(DH_NUM_BYTES);
                if (dataBytes <= 0) 
                    return false;
                
                try 
                {
                    numBytesReceived = 0;
                    numTries = 0;

                    do 
                    {
                        //Read rest of message bytes
                        numBytesReceived += readSocket(m_InputBuffer.array(), numBytesReceived, dataBytes - numBytesReceived);
                        Thread.sleep(1);
                        
                    } while (numBytesReceived < dataBytes && numTries < 10); //Loop until entire message is read

                    //Try to read any extra bytes on the buffer in case they were leftover
                    m_ExtraBytesReceived = readSocket (m_ExtraBuffer.array(), 0, m_ExtraBuffer.array().length);

                    //If extra bytes were read, mark a flag to process them
                    if (m_ExtraBytesReceived > 0)
                        m_ProcessExtraBytes = true;

                    if (numBytesReceived == dataBytes)
                    {
                        //we read in the right number of data bytes, success!
                        return true;
                    } 
                    else 
                    {
                        resync();
                        //we might have been out of sync before this
                        System.err.println("Had to resync");
                        throw new IOException("Message traffic is unsync'ed from Bridgeport gamma detector.");
                    }

                } 
                catch (Exception e) 
                {
                    throw new IOException("Could not retrieve data header from Bridgeport gamma detector.");
                }
            } 
            else 
            {
                resync();
                //This wasn't the first word of the data header, we must be out of sync.
                System.err.println("Had to resync");
                throw new IOException("Message traffic is unsync'ed from Bridgeport gamma detector.");
            }
        } 
        else 
        {
            if (_VERSOSE)
                System.out.println("No message received: " + numBytesReceived);
            //Could not read full header, just ignore and try next time
        }

        return false;
    }

    /**
     * Read all bytes on the socket to clear out the read buffer.  This way our next read should start
     * at the beginning of a message
     */
    private void resync() 
    {
        int numBytesReceived = 0;
        do 
        {
            try 
            {
                numBytesReceived = readSocket(m_InputBuffer.array(), 0, MAX_BUFFER_SIZE);
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        } 
        while (numBytesReceived > 0);
    }

    /**
     * Process message from Bridgeport sensor
     *
     * @return False if we should try to reconnect because data seems invalid
     * @throws java.io.IOException
     */
    private boolean processDataFields() throws IOException 
    {
        int headerBytes = 0;
        int sequence = 0;
        int dataFormat = 0;
        int dataGroup = 0;
        int dataType = 0;
        int deviceNumber = 0;
        int channelNumber = 0;
        int instrumentNumber = 0;
        int dataBytes = 0;


        //Parse header bytes
        headerBytes = (int) m_InputHeader.get(DH_LEN);

        byte format = m_InputHeader.get(DH_FORMAT);
        m_InputBufferBigEndian = ((format & 0x0080) != 0);
        if (m_InputBufferBigEndian) 
            m_InputBuffer.order(ByteOrder.BIG_ENDIAN);
        else 
            m_InputBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        sequence = (format >> 4) & 0x0003;

        dataFormat = (format & 0x000F);

        dataGroup = (int) m_InputHeader.get(DH_GRP);

        dataType = (int) m_InputHeader.get(DH_TYPE);

        deviceNumber = (int) m_InputHeader.get(DH_DEVNUM);

        instrumentNumber = (int) m_InputHeader.getShort(DH_INSTRNUM);

        dataBytes = (int) m_InputHeader.getInt(DH_NUM_BYTES);


        //Parse message based on dataType flag
        synchronized (m_Lock) 
        {
            switch (dataType) 
            {
                case DH_TYPE_STATUS:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Status");
                    break;
                case DH_TYPE_VERSION:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Version");
                    break;
                case DH_TYPE_CALIBRATION:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Calibration");
                    break;
                case DH_TYPE_SETUP_F:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Setup F");
                    break;
                case DH_TYPE_SETUP_I:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Setup I");
                    break;
                case DH_TYPE_SETUP_C:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Setup C");
                    break;
                case DH_TYPE_RATES:
                    //Read count rate data
                    readCountRates();

                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Count Rates");
                    if (_VERSOSE)
                        System.out.println("Num Events: " + m_LastData_CountRates.m_NumEvents);

                    //Log to file and send to listeners
                    updateStatisticsToOthers (true);

                    break;
                case DH_TYPE_HISTO:
                {
                    //Read histogram data
                    int binCount = 0;
                    if (dataFormat == FORMAT_USHORT)
                        binCount = dataBytes / 2;
                    else if (dataFormat == FORMAT_ULONG || dataFormat == FORMAT_LONG)
                        binCount = dataBytes / 4;
                    else
                        throw new IOException("Invalid format for histogram data");

                    int sumCounts = readHistogram (0, binCount, dataFormat);

                    if (sumCounts == 0 && !m_FirstHistoRead)
                    {
                        //No counts received, likely a connectivity issue.  Try to reconnect
                        return false;
                    }

                    m_FirstHistoRead = false;

                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Histogram data");
                    if (_VERSOSE)
                        System.out.println("Counts: " + sumCounts);

                    //Log to file and send to listeners
                    updateHistogramToOthers (true);

                    break;
                }
                case DH_TYPE_JHU:
                {
                    //This is a combined packet with all datat at once.

                    //Read count rates
                    readCountRates();
                    
                    //Read histogram spectrum
                    int binCount = 0;
                    /*if (dataFormat == FORMAT_USHORT)
                        binCount = dataBytes / 2;
                    else if (dataFormat == FORMAT_ULONG || dataFormat == FORMAT_LONG)
                        binCount = dataBytes / 4;
                    else 
                        throw new IOException("Invalid format for histogram data");*/
                    //Fixed bin count of 1024
                    binCount = 1024;

                    int histStartIndex = CR_R1*4;
                    int sumCounts = readHistogram(histStartIndex, binCount, dataFormat);
                    
                    //read calibration settings
                    int calibrationStartIndex = CR_R1*4 + binCount * 4;
                    readCalibration(calibrationStartIndex);


                    //Check data
                    if (sumCounts == 0 && !m_FirstHistoRead)
                    {
                        //No counts received, likely a connectivity issue.  Try to reconnect
                        return false;
                    }
                    
                    m_FirstHistoRead = false;

                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Merged Packet");
                    if (_VERSOSE)
                        System.out.println("Num Events in CR: " + m_LastData_CountRates.m_NumEvents);
                    if (_VERSOSE)
                        System.out.println("Counts in Histo: " + sumCounts);
                    if (_VERSOSE)
                        System.out.println("Tube temperature: " + m_LastData_Calibration.getTemperature());

                    //Log to file and send to listeners
                    updateStatisticsToOthers (true);
                    updateCalibrationToOthers (true);
                    updateHistogramToOthers (true);
                    
                    break;
                }
                case DH_TYPE_HISTO1:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Histo1");
                    break;
                case DH_TYPE_LM:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: List Mode");
                    break;
                case DH_TYPE_TRACE:
                    if (_VERSOSE)
                        System.out.println("Bridgeport gamma detector message received: Trace");
                    break;

            }
        }
        return true;
    }

    /**
     * Parse count rates data from input buffer
     */
    private void readCountRates ()
    {
        if (m_LastData_CountRates == null)
            m_LastData_CountRates = new GammaEthernetCountRates();

        m_LastData_CountRates.m_RealTicks = (int) m_InputBuffer.getFloat(CR_REAL_TICKS*4);
        m_LastData_CountRates.m_NumEvents = (int) m_InputBuffer.getFloat(CR_EVENTS*4);
        m_LastData_CountRates.m_NumTriggers = (int) m_InputBuffer.getFloat(CR_TRIGGERS*4);
        m_LastData_CountRates.m_DeadTicks = (int) m_InputBuffer.getFloat(CR_DEAD_TICKS*4);
        m_LastData_CountRates.setRealTime(m_InputBuffer.getFloat(CR_REAL_TIME*4));
        m_LastData_CountRates.m_EventRate = m_InputBuffer.getFloat(CR_EVENT_RATE*4);
        m_LastData_CountRates.m_TriggerRate = m_InputBuffer.getFloat(CR_TRIGGER_RATE*4);
        m_LastData_CountRates.m_DeadTimeFraction = m_InputBuffer.getFloat(CR_DEAD_TIME_FRACTION*4);
        m_LastData_CountRates.m_InputRate = m_InputBuffer.getFloat(CR_INPUT_RATE*4);
    }

    /**
     * Parse histogram data from input buffer
     * @param startIdx Starting index in the buffer to read from
     * @param binCount Number of bins of histogram data to read
     * @param dataFormat Format of data
     * @return Total counts
     * @throws IOException
     */
    private int readHistogram (int startIdx, int binCount, int dataFormat) throws IOException
    {
        if (m_LastData_Histogram == null)
        {
            m_LastData_Histogram = new RNHistogram(binCount);
        }
        int sumCounts = 0;

        int value = 0;
        for (int i = 0; i < binCount; i++)
        {
            //Offset by size of count rate packet
            if (dataFormat == FORMAT_USHORT)
                value = m_InputBuffer.getShort(startIdx + i * 2);
            else if (dataFormat == FORMAT_ULONG || dataFormat == FORMAT_LONG)
                value = m_InputBuffer.getInt(startIdx + i * 4);

            try
            {
                sumCounts += value;

                m_LastData_Histogram.setRawValue(i, value);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new IOException("Invalid bin number for histogram data");
            }

        }
        System.out.println ();

        return sumCounts;
    }

    /**
     * Read calibration data from input buffer
     *
     * @param startIdx Starting index in the buffer to read from
     */
    private void readCalibration (int startIdx)
    {
        if (m_LastData_Calibration == null)
            m_LastData_Calibration = new GammaCalibration();


        int idx = 0;
        m_LastData_Calibration.setADC_Speed(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setNumADC_Bits(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setADC_FSR(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setGain(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setMCA_CurrFSR(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setMCA_CurrMax(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setChargePerUnit(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setDC_Offset(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setTemperature(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setMCA_AnodeCurrent(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setMCA_ROI_Avg(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setNumHistBytes(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setNumListBytes(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setNumTraceBytes(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setNumModule6Bytes(m_InputBuffer.getFloat(startIdx + (idx++)*4));
        m_LastData_Calibration.setPacketCounter(m_InputBuffer.getFloat(startIdx + (idx++)*4));
    }

    /**
     * Allows the interface to send statistics data out.  It can save to disk (if boolean permits it) or send elsewhere on the network 
     * 
     * @param save If true, data will be saved to file.  If false, will not save.  This is generally true during live data collection and false during playback
     */
    private void updateStatisticsToOthers(boolean save)
    {
        if (m_LogData && save) 
            saveStatisticsData();
        
        if (m_SaveFlatFile)
            saveStatisticsFlatFile ();
        
        notifyListeners(m_LastData_CountRates);
    }
    
    /**
     * Allows the interface to send histogram data out.  It can send to the internal GUI, save to disk (if boolean permits it), or
     * send elsewhere on the network 
     * 
     * @param save If true, data will be saved to file.  If false, will not save.  This is generally true during live data collection and false during playback
     */
    private void updateHistogramToOthers (boolean save)
    {
        if (m_LogData && save) 
            saveHistogramData();
        
        if (m_SaveFlatFile)
            saveHistogramFlatFile ();

        notifyListeners(m_LastData_Histogram);
    }

    /**
     * Allows the interface to send calibration data out.  It can send to the internal GUI, save to disk (if boolean permits it), or
     * send elsewhere on the network
     *
     * @param save If true, data will be saved to file.  If false, will not save.  This is generally true during live data collection and false during playback
     */
    private void updateCalibrationToOthers (boolean save)
    {
        if (m_LogData && save)
            saveCalibrationData();

        if (m_SaveFlatFile)
            saveCalibrationFlatFile ();

        notifyListeners(m_LastData_Calibration);
    }
    
    /**
     * Get the counter value specified in the provided filename
     * 
     * @param filename Filename to find index from
     * @return Index of log file
     */
    private int parseLogCounter (String filename)
    {
        int startIdx = filename.lastIndexOf("_") + 1;
        int endIdx = filename.lastIndexOf(".");
        
        return Integer.parseInt (filename.substring(startIdx, endIdx));
    }
            
    /**
     * Makes a new log file, used so we don't make ridiculously large files.  Sets all streams up so that we are
     * ready to continue writing once this function is done.
     */
    private void ensureLogFile() 
    {
        if (m_CurrentLogFile == null || m_CurrentLogFile.length() > MAX_RECORDING_FILE_BYTES) 
        {
            //We need a new file
            try 
            {
                if (m_LogStream != null) 
                {
                    m_LogStream.flush();
                    m_LogStream.close();
                }
            } 
            catch (Exception e) 
            {
                System.err.println("Could not flush output stream before making new log file for Bridgeport gamma detector");
            }

            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", m_LogFilename, m_LogFileCounter++, ".dat");
            m_CurrentLogFile = new File(format.out().toString());

            try 
            {
                m_LogStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_CurrentLogFile), 4096));
            } 
            catch (Exception e) 
            {
                m_CurrentLogFile = null;
                m_LogStream = null;
                System.err.println("Could not create log file for Bridgeport gamma detector");
            }
        }
    }

    /**
     * Makes a new flat file, sets all streams up so that we are
     * ready to continue writing once this function is done.
     */
    private void ensureFlatFile() 
    {
        if (m_FlatFile == null ||  m_FlatFile.length() > MAX_FLAT_FILE_BYTES) 
        {
            //We need a new file
            try 
            {
                if (m_FlatFileWriter != null) 
                {
                    m_FlatFileWriter.flush();
                    m_FlatFileWriter.close();
                }
            } 
            catch (Exception e) 
            {
                System.err.println("Could not flush output stream before making new flat file for Bridgeport gamma detector");
            }
            
            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", m_LogFilename, m_FlatFileCounter++, ".flt");
            m_FlatFile = new File(format.out().toString());
            
            try
            {
                OutputStream ist = new FileOutputStream(m_FlatFile);
                m_FlatFileWriter = new BufferedWriter(new OutputStreamWriter(ist));
                
                writeFlatFileHeader (m_FlatFileWriter);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Makes a new log file, used so we don't make ridiculously large files.  Sets all streams up so that we are
     * ready to continue writing once this function is done.
     */
    private void ensureRawFile()
    {
        if (m_CurrentRawFile == null || m_CurrentRawFile.length() > MAX_RECORDING_FILE_BYTES)
        {
            //We need a new file
            try
            {
                if (m_RawStream != null)
                {
                    m_RawStream.flush();
                    m_RawStream.close();
                }
            }
            catch (Exception e)
            {
                System.err.println("Could not flush output stream before making new raw file for Bridgeport gamma detector");
            }

            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", m_RawFilename, m_RawFileCounter++, ".tcp");
            m_CurrentRawFile = new File(format.out().toString());

            try
            {
                m_RawStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_CurrentRawFile), 4096));
            }
            catch (Exception e)
            {
                m_CurrentRawFile = null;
                m_RawStream = null;
                System.err.println("Could not create raw file for Bridgeport gamma detector");
            }
        }
    }
    
    /**
     * This is called when we hit the end of a playback file.  Returns whether a next file is available.
     * 
     * @return boolean True if a new file has been located, false if we're done with this chain
     */
    private boolean getNextPlaybackFile() 
    {
        Formatter format = new Formatter();
        format.format("%1$s%2$05d%3$s", m_LogFilename, m_LogFileCounter++, ".dat");
        m_CurrentLogFile = new File(format.out().toString());

        try 
        {
            m_SocketInputStream = new DataInputStream(new FileInputStream(m_CurrentLogFile));
        } 
        catch (Exception e) 
        {
            return false;
        }
        
        return true;
    }

    /**
     * Save raw TCP bytes to file
     *
     * @param bytes Data bytes to log
     * @param length Length of byte array to log
     */
    private void saveRawTcpBytes (byte[] bytes, int length)
    {
        ensureRawFile();
        
        try 
        {
            writeBytesToStream(m_RawStream, bytes, length);
        } 
        catch (Exception e) 
        {
            System.err.println("Could not write log data to file for Bridgeport gamma detector");
        }
    }

    /**
     * Write bytes to an output stream
     *
     * @param output Output stream to write to
     * @param bytes Bytes to write
     * @param length Number of bytes to write
     * @throws IOException
     */
    private void writeBytesToStream (DataOutputStream output, byte [] bytes, int length) throws IOException
    {
        output.writeLong (System.currentTimeMillis());
        output.writeInt (length);
        output.write(bytes, 0, length);
        output.flush();
    }

    /**
     * Save histogram data to a flat file
     */
    private void saveHistogramFlatFile ()
    {
        //We will only let flat file be generated for incoming statistics messages.  That way, we won't get histogram
        //data printed first and screw up the columns
        
        try 
        {
            writeHistogramFlatFile(m_FlatFileWriter);
            writeMetricsDataFlatFile (m_FlatFileWriter);
        } 
        catch (Exception e) 
        {
            System.err.println("Could not write flat file data for Bridgeport gamma detector");
        }
    }

    /**
     * Save histogram data to a binary format readable by this program
     */
    private void saveHistogramData() 
    {
        ensureLogFile();
        
        try 
        {
            writeHistogramToStream(m_LogStream);
        } 
        catch (Exception e) 
        {
            System.err.println("Could not write log data to file for Bridgeport gamma detector");
        }
    }

    /**
     * Write histogram data in a binary format to the stream
     * @param output Stream to write to
     * @throws IOException
     */
    private void writeHistogramToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_HISTOGRAM);
        output.writeLong(System.currentTimeMillis());
        m_LastData_Histogram.writeToStream(output);
        output.flush();
    }

    /**
     * Write header data defining the columns in the flat file
     *
     * @param writer Stream to write to
     * @throws IOException
     */
    private void writeFlatFileHeader (BufferedWriter writer) throws IOException
    {
        writer.write ("TimestampMillis\t");
        GammaEthernetCountRates.writeFlatFileHeader (writer);
        writer.write ("TimestampMillis\t");
        GammaCalibration.writeFlatFileHeader (writer);
        writer.write ("TimestampMillis\t");
        RNHistogram.writeFlatFileHeader (writer);
        for (int i = 0; i < m_LastData_Histogram.getNumBins()-1; i++)
            writer.write ("\t");
        
        writer.write ("Lat\tLon\tAltEllip\tP(Pa)\tT(C)\t");
        writer.newLine();
    }

    /**
     * Write histogram data to stream in flat file format
     *
     * @param writer Stream to write to
     * @throws IOException
     */
    private void writeHistogramFlatFile (BufferedWriter writer) throws IOException
    {
        if (writer == null)
            return;
        
        writer.write ("" + System.currentTimeMillis() + "\t");
        m_LastData_Histogram.writeToFlatFile (writer);

        //No newlines to keep data on same line for single timestep
        //writer.newLine();
        //writer.flush();
    }

    /**
     * Write metrics data to flat file
     *
     * @param writer Stream to write to
     * @throws IOException
     */
    private void writeMetricsDataFlatFile (BufferedWriter writer) throws IOException
    {
        if (writer == null)
            return;

        //If no belief manager, write junk to file to preserve columns
        if (beliefManager == null)
            writer.write ("-1000\t-1000\t-1000\t-1000\t-1000");
        else
        {
            PiccoloTelemetryBelief picBlf = (PiccoloTelemetryBelief)beliefManager.get (PiccoloTelemetryBelief.BELIEF_NAME);
            if (picBlf == null || picBlf.getPiccoloTelemetry() == null)
                writer.write ("-1000\t-1000\t-1000\t-1000\t-1000");
            else
            {
                writer.write (picBlf.getPiccoloTelemetry().Lat + "\t");
                writer.write (picBlf.getPiccoloTelemetry().Lon+ "\t");
                writer.write (picBlf.getPiccoloTelemetry().AltWGS84 + "\t");
                writer.write (picBlf.getPiccoloTelemetry().StaticPressPa + "\t");
                writer.write (picBlf.getPiccoloTelemetry().OutsideAirTempC + "\t");
            }
        }

        writer.newLine();
        writer.flush();
    }

    /**
     * Write count rates data to flat file.
     */
    private void saveStatisticsFlatFile() 
    {
        if (m_SaveFlatFile)
            ensureFlatFile();

        try 
        {
            writeStatisticsFlatFile(m_FlatFileWriter);
        } 
        catch (Exception e) 
        {
            System.err.println("Could not write flat file data for Bridgeport gamma detector");
        }
    }

    /**
     * Write count rates data to binary file in format that can be read by this interface
     */
    private void saveStatisticsData() 
    {
        ensureLogFile();
        
        try 
        {
            writeStatisticsToStream(m_LogStream);
        } 
        catch (Exception e) 
        {
            System.err.println("Could not write log data to file for Bridgeport gamma detector");
        }
    }

    /**
     * Write statistics data to stream in binary format
     *
     * @param output Output stream to write to
     * @throws IOException
     */
    private void writeStatisticsToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_STATISTICS);
        output.writeLong(System.currentTimeMillis());
        m_LastData_CountRates.writeToStream(output);
        output.flush();
    }

    /**
     * Write statistics in a flat file format to the given stream
     *
     * @param writer Stream to write to
     * @throws IOException
     */
    private void writeStatisticsFlatFile (BufferedWriter writer) throws IOException
    {
        if (writer == null)
            return;
        
        writer.write ("" + System.currentTimeMillis() + "\t");
        m_LastData_CountRates.writeToFlatFile (writer);
    }

    /**
     * Write calibration data to a flat file
     */
    private void saveCalibrationFlatFile ()
    {
        //We will only let flat file be generated for incoming statistics messages.  That way, we won't get calibration
        //data printed first and screw up the columns
        //if (m_SaveFlatFile)
        //    ensureFlatFile();

        try
        {
            writeCalibrationFlatFile(m_FlatFileWriter);
        }
        catch (Exception e)
        {
            System.err.println("Could not write flat file data for Bridgeport gamma detector");
        }
    }

    /**
     * Write calibration data in flat file format to the given stream
     * @param writer Stream to write to
     * @throws IOException
     */
    private void writeCalibrationFlatFile (BufferedWriter writer) throws IOException
    {
        if (writer == null)
            return;

        writer.write ("" + System.currentTimeMillis() + "\t");
        m_LastData_Calibration.writeToFlatFile (writer);
    }

    /**
     * Save calibration data in a binary format that can be read by this interface
     */
    private void saveCalibrationData()
    {
        ensureLogFile();

        try
        {
            writeCalibrationToStream(m_LogStream);
        }
        catch (Exception e)
        {
            System.err.println("Could not write log data to file for Bridgeport gamma detector");
        }
    }

    /**
     * Write calibration data in binary format to the given stream
     *
     * @param output Stream to write to
     * @throws IOException
     */
    private void writeCalibrationToStream(DataOutputStream output) throws IOException
    {
        output.writeInt(LOG_CALIBRATION);
        output.writeLong(System.currentTimeMillis());
        m_LastData_Calibration.writeToStream(output);
        output.flush();
    }

    /**
     * Send a UDP broadcast that will be received by the Bridgeport sensor.  The sensor will reply with its settings and the interface
     * will then connect to it.  Will wait until reply listener is started, first
     *
     * @return True if broadcast was succesfully sent
     */
    private boolean udpBroadcast() 
    {
        waitForBroadcastListener();

        byte[] broadcastBytes = new byte[UDP_BROADCAST_LENGTH];

        try 
        {
            //Get our IP address so we know what subnet we're on
            InetAddress myIpAddress = LocalIPAddress.getLocalHost();
            
            if (myIpAddress.isLoopbackAddress()) 
            {
                System.out.println("Error getting IP address of local computer");
                return false;
            } 
            else 
            {
                System.out.println("Assuming my IP address is: " + myIpAddress.getHostAddress());
            }

            //Switch to broadcast on subnet
            byte[] ipAddr = myIpAddress.getAddress();
            ipAddr[3] = (byte) 255;

            InetAddress broadcastIpAddress = InetAddress.getByAddress(ipAddr);
            System.out.println("Sending UDP broadcast on subnet: " + broadcastIpAddress.getHostAddress());

            DatagramSocket socket = new DatagramSocket(UDP_BROADCAST_FROM_PORT);
            DatagramPacket packet = new DatagramPacket(broadcastBytes, UDP_BROADCAST_LENGTH, broadcastIpAddress, UDP_BROADCAST_TO_PORT);

            socket.send(packet);
            socket.close();
            return true;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
    }
        return false;
    }

    /**
     * Receive and process reply from UDP broadcast
     *
     * @param broadcastInputStream Stream to receive reply from
     */
    private void processBroadcastReply(DataInputStream broadcastInputStream) 
    {
        byte[] recvd = new byte[UDP_BROADCAST_LENGTH];
        try 
        {
            //Read reply
            int bytesRead = broadcastInputStream.read(recvd);
            if (bytesRead > 10) 
            {
                //Extract IP address of Briddeport sensor
                m_IPAddress = (recvd[6] < 0 ? 256 + recvd[6] : recvd[6]) + "." + (recvd[7] < 0 ? 256 + recvd[7] : recvd[7]) + "." + (recvd[8] < 0 ? 256 + recvd[8] : recvd[8]) + "." + (recvd[9] < 0 ? 256 + recvd[9] : recvd[9]);
            } 
            else 
            {
                System.err.println("Unable to extract IP address from UDP reply from Bridgeport gamma detector");
                m_IPAddress = "127.0.0.1";
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * @return True if finished gracefully or non-recoverable error, false if we should reconnect
     */
    @Override
    protected boolean runLive()
    {
        m_Running = true;
        m_FirstHistoRead = true;

        ServerSocket broadcastReplyListener = null;
        Socket broadcastReplySocket = null;
        DataInputStream broadcastInputStream = null;
        runBroadcastThread = true;

        //Send UDP broadcast to Bridgeport sensor.  Will first wait for reply listener to start up
        Thread th = new Thread() {

            @Override
            public void run() 
            {
                while (runBroadcastThread)
                {
                    udpBroadcast();
                    
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        th.setName ("WACS-BridgeportBroadcastThread");
        th.start();


        //Set up broadcast reply listener.
        try 
        {
            broadcastReplyListener = new ServerSocket(BROADCAST_REPLY_PORT);

            System.out.println("Listening for incoming broadcast replies...");
            m_BroadcastListenerStarted = true;
            broadcastReplySocket = broadcastReplyListener.accept();

            //Broadcast reply has been received, process it and continue
            runBroadcastThread = false;
            broadcastInputStream = new DataInputStream(broadcastReplySocket.getInputStream());

            System.out.println("Broadcast reply received!");
        } 
        catch (Exception e) 
        {
            System.err.println("Could not connect to incoming broadcast reply");
            return true;
        }


        //process the broadcast replies
        try 
        {
            //We're only going to accept one
            processBroadcastReply(broadcastInputStream);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            try 
            {
                broadcastReplySocket.close();
                broadcastInputStream.close();
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        }



        //This will command the detector to start sending data back, but wait until the listeners are up
        Thread t = new Thread() {

            @Override
            public void run() 
            {
                requestPolling();
            }
        };
        t.setName ("WACS-BridgeportRequestPollingThread");
        t.start();





        //Now start the socket connection thread that will receive data coming back
        try 
        {
            m_DataSocketListener = new ServerSocket(DATA_PORT);
            m_DataSocketListener.setSoTimeout(1000);
            System.out.println("Listening for incoming connections...");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }



        long lastMsgRecv = System.currentTimeMillis();
        //If a message hasn't been received in misMsgRecvTime time, we should try to reconnect because something is wrong
        float minMsgRecvTime = 2.5f*Math.min (m_Scan_HistogramPeriod, m_Scan_StatisticsPeriod);  //times 2.5 to allow a small buffer or lost message
        boolean needReconnect = false;
        boolean dontSendStop = false;
        while (m_Running) 
        {
            try 
            {
                m_SocketListenerStarted = true;
                //Connect to a new socket for every incoming message
                m_DataSocket = m_DataSocketListener.accept();
            } 
            catch (SocketTimeoutException e)
            {
                //Do nothing.  Socket timed out, so we'll just keep trying to accept connections
                
                //If it took way too long, try to reconnect and send UDP broadcast
                if ((System.currentTimeMillis() - lastMsgRecv)/1000.0f > minMsgRecvTime)
                {
                    //No messages, probable power cycle.
                    //Resend UDP and reconnect.
                    needReconnect = true;
                    dontSendStop = true;
                    m_Running = false;
                    System.out.println ("Long delay in receiving histograms, attempting re-connect.");
                }
                 
                continue;
            }
            catch (Exception e) 
            {
                System.err.println("Could not connect to incoming data socket");
                continue;
            }

            boolean readingData = true;
            try 
            {
                //Got a socket connection from Bridgeport
                m_SocketInputStream = new DataInputStream(m_DataSocket.getInputStream());
                if (_VERSOSE)
                    System.out.println("Data socket connected!");
                
                while (readingData) 
                {
                    if (readFromSensor()) 
                    {
                        //Message was read fully and byte counts all match up
                        readingData = false;
                        lastMsgRecv = System.currentTimeMillis();
                        if (!processDataFields())
                        {
                            //Histogram count was zero, likely need to reconnect
                            needReconnect = true;
                            m_Running = false;
                            System.out.println ("Histogram all zeroes, attempting re-connect.");
                        }

                        //Extra bytes wound up in TCP packet, let's try to parse them.  This is an attempted fix to the packet merging from
                        //Bridgeport, but has not been succesfully tested.  Bridgeport is instead changing their message format so it
                        //probably doesn't matter anymore anyway.
                        if (m_ProcessExtraBytes)
                        {
                            String logMsg = "####****TCP packets got merged!";
                            logMsg += "Timestamp=" + System.currentTimeMillis();
                            logMsg += "ExtraPackets=" + m_ExtraBytesReceived + ":";
                            logMsg += "****####";
                            saveRawTcpBytes(logMsg.getBytes(), logMsg.length());
                            for (int i = 0; i < m_ExtraBytesReceived; i ++)
                            {
                                m_InputBuffer.array()[i] = m_ExtraBuffer.array()[i];
                            }
                            m_ProcessExtraBytes = false;

                            if (!processDataFields())
                            {
                                //Histogram count was zero, likely need to reconnect
                                needReconnect = true;
                                m_Running = false;
                                System.out.println ("Histogram all zeroes, attempting re-connect.");
                            }
                        }
                        
                    } 
                    else
                    {
                        try 
                        {
                            Thread.sleep(500);
                        } 
                        catch (Exception e) 
                        {
                            e.printStackTrace();
                        }
                    }
                }
            } 
            catch (Exception e) 
            {
                //e.printStackTrace();
                //Do nothing, we'll just try to reconnect
            } 
            finally 
            {
                try 
                {
                    m_DataSocket.close();
                    m_SocketInputStream.close();
                } 
                catch (IOException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }

        if (!dontSendStop)
            sendStopCommand();

        try 
        {
            if (m_CommandSocket.isConnected()) 
                m_CommandSocket.close();
            
            if (m_DataSocket!= null && m_DataSocket.isConnected())
            {
                m_DataSocket.close();
                m_SocketInputStream.close();
            }
            
            if (m_DataSocketListener.isBound()) 
                m_DataSocketListener.close();
            
            if (broadcastReplyListener.isBound())
                broadcastReplyListener.close();
            
            if (broadcastReplySocket.isBound())
            {
                broadcastReplySocket.close();
                broadcastInputStream.close();
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return (!needReconnect);
    }

    
    /**
     * Run playback of messages from log files
     */
    protected void runPlayback()
    {
        m_Running = true;
        long lastReadTime = -1;
        
        while (m_Running) 
        {
            try 
            {
                int dataType = -1;
                try 
                {
                    //Get type of message
                    dataType = m_SocketInputStream.readInt();
                }
                catch (Exception e)
                {
                    //We reached the end of a file, either because m_SocketInputStream is null (haven't started yet) or we read past EOF.
                    if (!getNextPlaybackFile())
                    {
                        //We didn't get a new file
                        m_Running = false;
                        continue;
                    }
                    
                    dataType = m_SocketInputStream.readInt();
                }
                
                //Get timestamp of message
                long dataTime = m_SocketInputStream.readLong();

                //Delay until message should be sent, considering speed adjustment
                if (lastReadTime > 0)
                {
                    Thread.sleep ((long)((dataTime - lastReadTime)/PLAYBACK_THREAD_DELAY_REDUCTION));
                }
                lastReadTime = dataTime;
                
                synchronized (m_Lock)
                {
                    if (dataType == LOG_HISTOGRAM)
                    {
                        //Read and send histogram message
                        if (m_LastData_Histogram == null) 
                            m_LastData_Histogram = new RNHistogram();
                        
                        m_LastData_Histogram.readFromStream(m_SocketInputStream);
                        updateHistogramToOthers(false);
                        
                        System.out.println("Bridgeport gamma detector message playback: Histogram");
                    }
                    else if (dataType == LOG_STATISTICS)
                    {
                        //Read and send count rates message
                        if (m_LastData_CountRates == null) 
                            m_LastData_CountRates = new GammaEthernetCountRates();
                        
                        m_LastData_CountRates.readFromStream(m_SocketInputStream);
                        updateStatisticsToOthers(false);
                        
                        System.out.println("Bridgeport gamma detector message playback: Count Rates");
                    }
                    else if (dataType == LOG_CALIBRATION)
                    {
                        //Read and send calibration message
                        if (m_LastData_Calibration == null)
                            m_LastData_Calibration = new GammaCalibration();

                        m_LastData_Calibration.readFromStream(m_SocketInputStream);
                        updateCalibrationToOthers(false);

                        System.out.println("Bridgeport gamma detector message playback: Calibration");
                    }
                }
            } 
            catch (Exception e) 
            {
                m_Running = false;
                System.out.println ("Playback file, index " + m_LogFileCounter + ", ended abruptly: " + m_LogFilename);
            } 
        }

        try 
        {
            m_SocketInputStream.close();
            
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        System.out.println ("Playback ended for logs: " + m_LogFilename);
    }

    //This just fakes random data from the Bridgeport, shouldn't be used other than testing interfaces.
    /*private void runPlayback2 ()
    {
        m_Running = true;
        long lastReadTime = -1;
        int idx = 0;

        while (m_Running)
        {
            try
            {

                Thread.sleep (2000);

                synchronized (m_Lock)
                {
                    m_LastData_Histogram = new RNHistogram(1024);
                    if (m_LastData_CountRates == null)
                        m_LastData_CountRates = new GammaCountRates();

                    m_LastData_Histogram.setValue((idx++)%1024, (int) (100 + Math.random() * 50) + (idx>50?idx>100?0:2000:0));
                    updateHistogramToOthers(false);
                    updateStatisticsToOthers(false);

                    System.out.println("Bridgeport gamma detector message playback: Histogram");
                }
            }
            catch (Exception e)
            {
                m_Running = false;
            }
        }

        try
        {
            m_SocketInputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println ("Playback ended for logs: " + m_LogFilename);
    }*/
}
