package edu.jhuapl.nstd.cbrnPods;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxAETNADetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDetectionMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDllDetectionReportMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxStatusMessage;
import edu.jhuapl.nstd.cbrnPods.messages.countMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import edu.jhuapl.nstd.swarm.belief.PiccoloTelemetryBelief;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.spectra.AETNAResult;
import edu.jhuapl.spectra.AETNAServiceInterface2;
import java.util.Date;

/**
 * Processor for Bladewerx data.  Forms spectra and sends composite spectra and detection messages to listener
 *
 * @author humphjc1
 */
public class bladewerxProcessor extends AbstractAETNAServiceProcessor implements cbrnPodMessageListener 
{
    /**
     * Data object to store spectra with a timestamp
     */
    public class SpectraPair
    {
        public long timestamp;
        public int [] m_Spectra;

        /**
         * Create object with timestamp and spectra
         *
         * @param newTime
         * @param newSpectra
         */
        SpectraPair (long newTime, int newSpectra [])
        {
            timestamp = newTime;
            m_Spectra = newSpectra;
        }
        
    };
    
    /**
     * Interface to send generated messages for
     */
    private cbrnPodGenericInterface m_Interface;
    
    /**
     * Current binned spectra to add to processing.  Accumulated and then added to the list on interval
     */
    private int m_CurrSpectra [];

    /**
     * Current binned spectra to add to processing.  Accumulated and then added to the list on interval
     */
    private int m_RebinnedSpectra [];
    
    /**
     * Used by the RNHistographDisplayGraphPanel to plot the classified spectrum over the raw spectrum
     */
    private float m_ClassifiedSpectra[] = null;
    
    /**
     * Timestamp when the last processed spectra begun
     */
    private long m_LastSpectraTime;
    
    /**
     * Locking object for m_CurrSpectra
     */
    private final Object m_CurrSpectraLock = new Object ();
    
    /**
     * Total counts in m_Spectra array;
     */
    private long m_CurrSpectraCount;
    
    /**
     * Total counts since last count rate message send
     */
    private int m_MsgCounts;
    
    private int m_GammaAndBeta;
    
    private int m_GammaOnly;
    
    /**
     * List of spectra to sum and process when updated
     */
    private LinkedList <SpectraPair> m_SpectraList = new LinkedList<SpectraPair> (); 
    
    /**
     * Summed spectra of m_SpectraList to process
     */
    private float m_SumSpectra [] = new float [NUM_CHANNELS];
    
    /**
     * Total counts in m_SumSpectra array;
     */
    private long m_SumSpectraCount;
    
    /**
     * Background channels to use for processing, loaded from config file
     */
    private int m_BackgroundChannels [];
    
    /**
     * Reference value for background channels
     */
    private boolean m_BackgroundReference [];
    
    /**
     * Isotope channels to use for processing, loaded from config file
     */
    private int m_IsotopeChannels [];
    
    /**
     * Reference value for isotope channels
     */
    private boolean m_IsotopeReference [];
    
    /**
     * Isotope names relating to m_IsotopeChannels list
     */
    private String m_IsotopeName [];
    
    
    /**
     * Goodness of fit result from last processing call
     */
    private float m_GoodnessOfFit [] = new float [1];
    
    /**
     * Elapsed time from last processing call
     */
    private int m_ElapsedTime [] = new int [1];
    
    /**
     * Fitted spectra from last processing call
     */
    private float m_FittedSpectra [] = new float [NUM_CHANNELS];
    
    /**
     * Raw spectra data returned from last dll request
     */
    int m_NativeRawSpectra[] = new int [NUM_CHANNELS];
    
    /**
     * Total counts returned from last dll request
     */
    int m_NativeTotalCounts[] = new int [1];
    
    /**
     * Counts under specific peak returned from last dll request
     */
    float m_NativePeakCounts[] = new float [1];
    
    /**
     * Variance under specific peak returned from last dll request
     */
    float m_NativePeakVariance[] = new float [1];
    
    /**
     * Channel under specific peak returned from last dll request
     */
    float m_NativePeakChannel[] = new float [1];
    
    /**
     * Channel number returned for a given peak from last dll request
     */
    short m_NativeChannel[] = new short [1];
    
    /**
     * Reference boolean returned for a given peak from last dll request
     */
    boolean m_NativeReference[] = new boolean [1];
    
    /**
     * Whether fast option was used for fitting
     */
    boolean m_FastOption;
    
    /**
     * Threshold for determining whether a GOF signals a positive detection
     */
    double positiveGOFThreshold;
    
    /**
     * Folder to save log files to
     */
    private String m_LogFolderName;
    
    /**
     * Output file for logging (raw)
     */
    private File m_CurrentLogFileRaw;
    
    /**
     * Counter for filenames to differentiate if they get too large (raw)
     */
    private int m_LogFileCounterRaw = 0;
    
    /**
     * Name of output file name to log data (raw)
     */
    private String m_LogFilenameRaw;
    
    /**
     * Output stream for logging (raw)
     */
    private DataOutputStream m_LogStreamRaw;
    
    /**
     * Output file for logging (fitted)
     */
    private File m_CurrentLogFileFitted;
    
    /**
     * Counter for filenames to differentiate if they get too large (fitted)
     */
    private int m_LogFileCounterFitted = 0;
    
    /**
     * Name of output file name to log data (fitted)
     */
    private String m_LogFilenameFitted;
    
    /**
     * Output stream for logging (fitted)
     */
    private DataOutputStream m_LogStreamFitted;
    
    
    /**
     * True while thread is running
     */
    private boolean m_Running;
    
    /**
     * How often to compile/log spectra
     */
    private float m_LogFrequencyMs;
    
    /**
     * How often to push out the number of counts
     */
    private int m_LogCountRateFrequencyMs;
    
    /**
     * At what number of counts to push update regardless of time
     */
    private int m_LogCountRateThreshold;
    
    /**
     * If total counts exceeds this threshold, we'll compile/log spectra even if we haven't passed time limit
     */
    private int m_LogCountThreshold;
    
    /**
     * Number of spectra intervals to add together for each processing call
     */
    private int m_NumSpectraToProcess;
    
    /**
     * Number of background peaks defined
     */
    private int backgroundPeaksDefined;
    
    /**
     * Number of isotope peaks defined
     */
    private int isotopePeaksDefined;
    
    /**
     * Last offset setting reported from MCA
     */
    private int m_Offset;
    
    /**
     * Last gain setting reported from MCA
     */
    private int m_Gain;
    
    /**
     * Last threshold setting reported from MCA
     */
    private int m_Threshold;
    
    /**
     * Last scale setting reported from MCA
     */
    private int m_Scale;
    
    
    /**
     * Number of channels
     */
    public final static int NUM_CHANNELS = 256;
    
    /**
     * Number of peaks DLL will accept
     */
    private final static int MAX_PEAKS_ALLOWED = 6;
    
    /**
     * Maximum file size for log files.  Starts a new file if we exceed this value.
     */
    private final static int MAX_LOG_FILE_BYTES = 10000000;
    

    /**
     * Channels to permanently reset to zero when new data comes in.  Used to eliminate erroneous spikes
     */
    private int channelsToZero[] = null;

    /**
     * Belief manager, used to get agent metrics
     */
    BeliefManager beliefManager;

    /**
     * Last piccolo telemetry belief received from agent
     */
    PiccoloTelemetryBelief lastPicBlf = null;

    /**
     * Pressure/temperature shift correction factor
     */
    double m_ShiftCorrFactor;

    /**
     * Temperature at calibration
     */
    double m_TempCalK;

    /**
     * Pressure at calibration
     */
    double m_PressCalPa;

    /**
     * Channel offset for pressure correction
     */
    int m_ChannelOffset;

    /**
     * Index to start rebinning spectra.  Below this channel, bins are not shifted.
     */
    int m_RebinThreshold;

    /**
     * If true, do processing with the bladewerx DLL.  If false, use the AETNA Interface.  False not yet fully implemented
     */
    boolean processBladewerxDLL;
    

    /**
     * Create object
     * @param intr Pod Interface to send message through
     * @param belMgr Belief manager to get agent metrics
     */
    public bladewerxProcessor (cbrnPodGenericInterface intr, BeliefManager belMgr)
    {
        this.setName ("WACS-BladewerxProcessor");
        setDaemon(true);
        readConfig ();
        setupLogFolder ();
        beliefManager = belMgr;
        if (beliefManager != null)
            lastPicBlf = (PiccoloTelemetryBelief)beliefManager.get (PiccoloTelemetryBelief.BELIEF_NAME);

        //Setup log filenames
        m_LogFilenameRaw = m_LogFolderName + "BladewerxAlphaRaw_" + System.currentTimeMillis() + "_";
        m_LogFilenameFitted = m_LogFolderName + "BladewerxAlphaFitted_" + System.currentTimeMillis() + "_";

        m_Interface = intr;
        m_LastSpectraTime = -1;
        clearCurrSpectra ();
        clearCounts();
        

        //Start thread that listens for incoming spectra and processes them
        this.start ();
        
    }
    
    private void readConfig ()
    {
    	// TODO Don't read properties for DLL processing if we're not using the Bladewerx.dll.
    	aetnaClassifier = Config.getConfig().getProperty("BladewerxProcessor.ClassiferName", "sensor");
    	processBladewerxDLL = Config.getConfig().getPropertyAsBoolean("BladewerxProcessor.UseDLL", true);
    	m_LogFrequencyMs = 1000 * (float)Config.getConfig().getPropertyAsDouble("BladewerxProcessor.LogFrequency.Secs", 30);
        m_LogCountThreshold = Config.getConfig().getPropertyAsInteger ("BladewerxProcessor.LogCountThreshold", 100);
        m_LogCountRateFrequencyMs = 1000 * Config.getConfig().getPropertyAsInteger("BladewerxProcessor.LogCountRateFrequency.Secs", 30);
        m_LogCountRateThreshold = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.LogCountRateThreshold", 30);
        m_NumSpectraToProcess = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.NumSpectraToProcess", 5);
        m_LogFolderName = Config.getConfig().getProperty("Bladewerx.LogFolder", "./AlphaLogs");
        backgroundPeaksDefined = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.NumBackgroundPeaksDefined", 0);
        if (backgroundPeaksDefined > MAX_PEAKS_ALLOWED - 1)
            backgroundPeaksDefined = MAX_PEAKS_ALLOWED - 1;
        if (backgroundPeaksDefined < 0)
            backgroundPeaksDefined = 0;
        isotopePeaksDefined = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.NumIsotopePeaksDefined", 1);
        positiveGOFThreshold = Config.getConfig().getPropertyAsDouble("BladewerxProcessor.PositiveGOFThreadhold", 1.0);

        //Read background peaks from file
        readPeaks ();

        String channelsToZeroString = Config.getConfig().getProperty("Bladewerx.ChannelsToZero", "");
        StringTokenizer tokens = new StringTokenizer(channelsToZeroString, " ,\r\n");
        int count = tokens.countTokens();
        if (count > 0)
        {
            channelsToZero = new int [count];
            for (int i = 0; i < channelsToZero.length; i ++)
            {
                channelsToZero[i] = Integer.parseInt(tokens.nextToken());
            }
        }

        m_RebinThreshold = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.RebinMinThresholdBin", 50);
        if (m_RebinThreshold < 0)
            m_RebinThreshold = 0;
        if (m_RebinThreshold > 254)
            m_RebinThreshold = 254;

        //PT shift correction factors
        m_ShiftCorrFactor = Config.getConfig().getPropertyAsDouble ("BladewerxProcessor.ShiftCorrFactor", 25.0);
        m_TempCalK = Config.getConfig().getPropertyAsDouble ("BladewerxProcessor.TempCalK", 293.15);
        m_PressCalPa = Config.getConfig().getPropertyAsDouble ("BladewerxProcessor.PressCalPa", 101325);


    }
    
    /**
     * Accessor for the AETNA classified/estimated spectra.
     * 
     * @return
     */
    public float[] getClassifiedSpectra()
    {
    	return m_ClassifiedSpectra;
    }

    /**
     * Clear spectra of data
     */
    private void clearCurrSpectra ()
    {
        m_CurrSpectra = new int [NUM_CHANNELS];
        Arrays.fill (m_CurrSpectra, 0);
        m_RebinnedSpectra = new int [NUM_CHANNELS];
        Arrays.fill (m_RebinnedSpectra, 0);
        
        m_CurrSpectraCount = 0;
        
    }
    
    private void clearCounts()
    {
        m_MsgCounts = 0;
        m_GammaAndBeta = 0;
        m_GammaOnly = 0;
    }

    /**
     * For a already specified log folder name, ensure the log folder exists
     */
    private void setupLogFolder ()
    {
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
    }

    /**
     * Read peaks for isotope comparisons from config file.  Read background peaks and isotope peaks separately
     */
    private void readPeaks ()
    {
        if (backgroundPeaksDefined == 0)
        {
            //Nothing in Config file, use defaults
            /*backgroundPeaksDefined = 4;
            m_BackgroundChannels = new int [backgroundPeaksDefined];
            m_BackgroundReference = new boolean [backgroundPeaksDefined];
            
            m_BackgroundChannels[0] = 244;
            m_BackgroundReference[0] = false;
            m_BackgroundChannels[1] = 210;
            m_BackgroundReference[1] = true;
            m_BackgroundChannels[2] = 168;
            m_BackgroundReference[2] = true;
            m_BackgroundChannels[3] = 118;
            m_BackgroundReference[3] = true;
            
            System.out.println ("No alpha peaks defined in config file, using internal defaults");*/

            System.out.println ("Warning, no alpha peaks defined in config file!");
        }
        else
        {
            //Read background peaks, same for all isotopes
            m_BackgroundChannels = new int [backgroundPeaksDefined];
            m_BackgroundReference = new boolean [backgroundPeaksDefined];
            
            for (int i = 0; i < backgroundPeaksDefined; i ++)
            {
                m_BackgroundChannels[i] = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.BackgroundPeak" + i + ".Channel", 0);
                m_BackgroundReference[i] = Config.getConfig().getPropertyAsBoolean ("BladewerxProcessor.BackgroundPeak" + i + ".Reference", false);
            }
        }

        //Read isotope specific peaks
        m_IsotopeChannels = new int [isotopePeaksDefined];
        m_IsotopeReference = new boolean [isotopePeaksDefined];
        m_IsotopeName = new String [isotopePeaksDefined];
        for (int i = 0; i < isotopePeaksDefined; i ++)
        {
            m_IsotopeChannels[i] = Config.getConfig().getPropertyAsInteger("BladewerxProcessor.IsotopePeak" + i + ".Channel", 50);
            m_IsotopeReference[i] = Config.getConfig().getPropertyAsBoolean ("BladewerxProcessor.IsotopePeak" + i + ".Reference", false);
            m_IsotopeName[i] = Config.getConfig().getProperty ("BladewerxProcessor.IsotopePeak" + i + ".Name", "blank");
        }
    }

    /*
     * Gracefully stop the processing thread
     */
    public void killThread ()
    {
        m_Running = false;
        nativeStop();
    }

    /**
     * Receive messages from the pods of alpha detections and status.
     * @param m Message sent from pod
     */
    @Override
    public void handleMessage(cbrnPodMsg m)
    {
        if(m instanceof bladewerxDetectionMessage)
        {
           handleBladewerxDetectionMessage((bladewerxDetectionMessage) m);
        }
        else if (m instanceof bladewerxStatusMessage)
        {
            handleBladewerxStatusMessage ((bladewerxStatusMessage) m);
        }
    }

    /**
     * Receive and parse a status message.  Extract settings for logging to file
     * @param msg status message
     */
    private void handleBladewerxStatusMessage (bladewerxStatusMessage msg)
    {
        m_Offset = msg.getOffset();
        m_Gain = msg.getGain();
        m_Threshold = msg.getThreshold();
        m_Scale = msg.getScale();
    }

    /**
     * Receive and parse a detection message.  Extra channel of detection and add to the current spectra
     * @param msg detection message
     */
    private void handleBladewerxDetectionMessage(bladewerxDetectionMessage msg) 
    {
        int bin = msg.getBin();
        if (bin < 0 || bin > NUM_CHANNELS)
        {
            System.out.println ("Bin out of range for Bladewerx, ignoring..." + bin);
            return;
        }

        //Lock and add to current spectra.  This spectra will be processed within the processing thread at a given interval.
        synchronized (m_CurrSpectraLock)
        {
            m_CurrSpectra[bin] += 1;

            //Can zero out low (beta) channels for processing through config file.  Ignore these channels when counting channel detections.
            boolean shouldBeZero = false;
            if (channelsToZero != null)
            {
                for (int j = 0; j < channelsToZero.length; j ++)
                {
                    if (bin == channelsToZero[j])
                    {
                        shouldBeZero = true;
                        break;
                    }
                }
            }
            if (!shouldBeZero)
            {
                m_CurrSpectraCount ++;
            }
        }
        
        if (bin == 0)
        {
            m_GammaAndBeta ++;
        }
        else if (bin == 1)
        {
            m_GammaOnly ++;
        }
        else
        {
            m_MsgCounts ++;
        }
    }

    /**
     * Compute the offset based on pressure and temperature corrections for data.
     * @return Offset to be applied to data
     */
    private int computeOffset ()
    {
        if (lastPicBlf != null)
        {
            //Algorithm pulled from Bladewerx PFAPL code
            double tempNow_K = lastPicBlf.getPiccoloTelemetry().OutsideAirTempC + 273.15;
            double pressNow_Pa = lastPicBlf.getPiccoloTelemetry().StaticPressPa;
            int nChannels = (int) (m_ShiftCorrFactor * (1.0f - (tempNow_K/m_TempCalK) * (m_PressCalPa/pressNow_Pa)));
            //if nChannels is very large, probably something wrong with temp/press data.  Don't shift
            if (Math.abs(nChannels) > 100)
                return 0;
            return nChannels;
        }
        return 0;
    }

    /**
     * Using a specified offset, rebin channels above a certain bin threshold to account for P and T correction
     * @param rebinThreshold Only rebind channels above this threshold
     * @param channelOffset Rebin channels by this amount
     */
    private void rebinCurrChannels (int rebinThreshold, int channelOffset)
    {
        for (int i = 0; i < rebinThreshold; i ++)
            m_RebinnedSpectra[i] = 0;
        for (int i = rebinThreshold; i < m_RebinnedSpectra.length && (i+channelOffset) < m_RebinnedSpectra.length; i ++)
        {
            if (i+channelOffset < 0)
                continue;
            m_RebinnedSpectra[i+channelOffset] = m_CurrSpectra[i];
        }
    }

    /**
     * Be sure a raw log file is open and not too large for logging data
     *
     */
    private void ensureLogFileRaw ()
    {
        //raw file
        if (m_CurrentLogFileRaw == null || m_CurrentLogFileRaw.length() > MAX_LOG_FILE_BYTES) 
        {
            //We need a new file
            try 
            {
                if (m_LogStreamRaw != null) 
                {
                    m_LogStreamRaw.flush();
                    m_LogStreamRaw.close();
                }
            } 
            catch (Exception e) 
            {
                System.err.println("Could not flush output stream before making new log file for Bladewerx alpha detector");
            }

            //Rename new file
            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", m_LogFilenameRaw, m_LogFileCounterRaw++, ".dat");
            m_CurrentLogFileRaw = new File(format.out().toString());
            

            try 
            {
            	if(!m_CurrentLogFileRaw.createNewFile())
                {
            		m_CurrentLogFileRaw = null;
                    m_LogStreamRaw = null;
            		System.err.println("Could not create log file for Bladewerx alpha detector");
                }
            	
            	//Open file
                m_LogStreamRaw = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_CurrentLogFileRaw), 4096));
                writeSpectraSliceHeader (m_LogStreamRaw);
            } 
            catch (Exception e) 
            {
                m_CurrentLogFileRaw = null;
                m_LogStreamRaw = null;
                System.err.println("Could not create log file for Bladewerx alpha detector");
            }
        }
    }
    
    /**
     * Be sure a fitted results log file is open and not too large for logging data
     *
     */
    private void ensureLogFileFitted ()
    {
        //fitted file
        if (m_CurrentLogFileFitted == null || m_CurrentLogFileFitted.length() > MAX_LOG_FILE_BYTES) 
        {
            //We need a new file
            try 
            {
                if (m_LogStreamFitted != null) 
                {
                    m_LogStreamFitted.flush();
                    m_LogStreamFitted.close();
                }
            } 
            catch (Exception e) 
            {
                System.err.println("Could not flush output stream before making new log file for Bladewerx alpha detector");
            }

            //Rename log file
            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", m_LogFilenameFitted, m_LogFileCounterFitted++, ".dat");
            m_CurrentLogFileFitted = new File(format.out().toString());

            try 
            {
                //Open log file
                m_LogStreamFitted = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_CurrentLogFileFitted), 4096));
                writeFittedResultsHeader (m_LogStreamFitted);
            } 
            catch (Exception e) 
            {
                m_CurrentLogFileFitted = null;
                m_LogStreamFitted = null;
                System.err.println("Could not create log file for Bladewerx alpha detector");
            }
        }
    }

    /**
     * Write header data to a log stream detailing what columns are logged for in the raw log file
     *
     * @param logStream Stream to write to
     * @throws Exception
     */
    private void writeSpectraSliceHeader (DataOutputStream logStream) throws Exception
    {
        logStream.writeBytes ("Log time\t");
        logStream.writeBytes ("Spectra time\t");

        logStream.writeBytes ("Offset\t");
        logStream.writeBytes ("Gain\t");
        logStream.writeBytes ("Threshold\t");
        logStream.writeBytes ("Scale\t");

        logStream.writeBytes ("Spectra\t");
        for (int i = 0; i < (NUM_CHANNELS-1); i ++)
            logStream.writeBytes ("\t");

        logStream.writeBytes ("Lat\tLon\tAltEllip\tP(Pa)\tT(C)\t");
        logStream.writeBytes ("Rebin threshold\tChannel offset\tRebinned Spectra\t");
        
        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Write data for a single spectra slice to a log file.  Include settings and spectra.
     *
     * @param logStream Stream to write to
     * @param spectraTime Timestamp of received spectra, used to relate raw data with fitted data
     * @throws Exception
     */
    private void writeSpectraSlice (DataOutputStream logStream, long spectraTime) throws Exception
    {
        logStream.writeBytes (System.currentTimeMillis() + "\t");
        logStream.writeBytes (spectraTime + "\t");

        logStream.writeBytes (m_Offset + "\t");
        logStream.writeBytes (m_Gain + "\t");
        logStream.writeBytes (m_Threshold + "\t");
        logStream.writeBytes (m_Scale + "\t");
        
        for (int i = 0; i < NUM_CHANNELS; i ++)
        {
            logStream.writeBytes (m_CurrSpectra[i] + "\t");
        }

        //Don't write endlines so we can write metrics data
        //logStream.writeBytes ("\r\n");
        //logStream.flush();
    }

    /**
     * Write rebinned spectra data for a single spectra.
     *
     * @param logStream Log stream to write to
     * @throws Exception
     */
    private void writeRebinnedSpectra (DataOutputStream logStream) throws Exception
    {
        logStream.writeBytes (m_RebinThreshold + "\t");
        logStream.writeBytes (m_ChannelOffset + "\t");

        for (int i = 0; i < NUM_CHANNELS; i ++)
        {
            logStream.writeBytes (m_RebinnedSpectra[i] + "\t");
        }

        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Get the latest piccolo telemetry belief from the belief manager
     */
    private void getPicTelemBelief ()
    {
        if (beliefManager == null)
            lastPicBlf = null;
        else
        {
            lastPicBlf = (PiccoloTelemetryBelief)beliefManager.get (PiccoloTelemetryBelief.BELIEF_NAME);
            if (lastPicBlf == null || lastPicBlf.getPiccoloTelemetry() == null)
                lastPicBlf = null;
        }

    }

    /**
     * Write metrics data to a log file
     *
     * @param logStream Stream to write to
     * @throws IOException
     */
    private void writeMetricsData (DataOutputStream logStream) throws IOException
    {
        if (logStream == null)
            return;

        if (lastPicBlf == null)
            logStream.writeBytes ("-1000\t-1000\t-1000\t-1000\t-1000\t");
        else
        {
            logStream.writeBytes (lastPicBlf.getPiccoloTelemetry().Lat + "\t");
            logStream.writeBytes (lastPicBlf.getPiccoloTelemetry().Lon+ "\t");
            logStream.writeBytes (lastPicBlf.getPiccoloTelemetry().AltWGS84 + "\t");
            logStream.writeBytes (lastPicBlf.getPiccoloTelemetry().StaticPressPa + "\t");
            logStream.writeBytes (lastPicBlf.getPiccoloTelemetry().OutsideAirTempC + "\t");
            
        }

        //Don't write end line bytes so we can write rebinned channels on same line
        //logStream.writeBytes("\r\n");
        //logStream.flush();
    }

    /**
     * Write column names for fitted results log file
     *
     * @param logStream Stream to write to
     * @throws Exception
     */
    private void writeFittedResultsHeader (DataOutputStream logStream) throws Exception
    {
        logStream.writeBytes ("Log time\t");
        logStream.writeBytes ("Spectra time\t");

        logStream.writeBytes ("Goodness of fit\t");
        logStream.writeBytes ("Spectra slice count\t");
        logStream.writeBytes ("Peak Data Count\t");
        logStream.writeBytes ("Peak Data [Channel,Reference,PeakCounts,PeakVariance,PeakChannel]\t");
        for (int i = 0; i < (backgroundPeaksDefined+1)*5-1; i ++)
            logStream.writeBytes ("\t");

        logStream.writeBytes ("Fitted Spectra\t");

        logStream.writeBytes ("\r\n");
        logStream.flush();
    }
    
    /**
     * Write results of processing to a log file.
     *
     * @param logStream Stream to write to
     * @param spectraTime Timestamp of received spectra.  Can use this to match up with raw data log
     * @throws Exception
     */
    private void writeFittedResults (DataOutputStream logStream, AETNAResult result, long spectraTime) throws Exception
    {   
        logStream.writeBytes (System.currentTimeMillis() + "\t");
        logStream.writeBytes (spectraTime + "\t");
        
        if (result == null)
        {
            logStream.writeBytes("0\t");
        }
        else
        {
            logStream.writeBytes(result.isotopes.size() + "\t");

            for (int i = 0; i < result.isotopes.size(); i ++)
            {
                AETNAResult.IsotopeConcentraion isotope = result.isotopes.get(i);
                logStream.writeBytes (isotope.isotope + "\t" + isotope.concentration + "\t");
            }

            float array [] = result.estimated_spectrum;
            for (int i = 0; i < array.length; i ++)
            {
                logStream.writeBytes (array[i] + "\t");
            }
        }
        
        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Write results of processing to a log file.
     *
     * @param logStream Stream to write to
     * @param spectraTime Timestamp of received spectra.  Can use this to match up with raw data log
     * @throws Exception
     */
    private void writeFittedResults (DataOutputStream logStream, long spectraTime) throws Exception
    {   
        logStream.writeBytes (System.currentTimeMillis() + "\t");
        logStream.writeBytes (spectraTime + "\t");
        logStream.writeBytes (m_GoodnessOfFit[0] + "\t");
        logStream.writeBytes (m_SpectraList.size() + "\t");
        logStream.writeBytes ((backgroundPeaksDefined + 1) + "\t");
        for (int i = 0; i < backgroundPeaksDefined + 1; i ++)
        {
            readPeakData ((short)i);
            
            logStream.writeBytes (m_NativeChannel[0] + "\t" + m_NativeReference[0] + "\t" + m_NativePeakCounts[0] + "\t" + m_NativePeakVariance[0] + "\t" + m_NativePeakChannel[0] + "\t");
        }
        for (int i = 0; i < NUM_CHANNELS; i ++)
        {
            logStream.writeBytes (m_FittedSpectra[i] + "\t");
        }
        
        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Log all components of a spectra to the raw log file
     *
     * @param spectraTime Timestamp of received spectra
     */
    private void logSpectraSlice (long spectraTime)
    {
        ensureLogFileRaw();
        
        try 
        {
            writeSpectraSlice(m_LogStreamRaw, spectraTime);
            writeMetricsData (m_LogStreamRaw);
            writeRebinnedSpectra (m_LogStreamRaw);
            m_LogStreamRaw.flush();
        } 
        catch (Exception e) 
        {
            System.err.println("Could not raw write log data to file for Bladewerx alpha detector");
        }
    }

    /**
     * Read peak data for an isotope of interest
     * @param idx Index in the config file of the isotope
     */
    private void readPeakData (short idx)
    {
        nativeGetFitParameters(idx, m_NativePeakCounts, m_NativePeakVariance, m_NativePeakChannel);
        nativeGetPeakChannel(idx, m_NativeChannel);
        nativeGetReferencePeak(idx, m_NativeReference);
    }

    /**
     * Sum spectra in the spectra list into a single spectra
     */
    private void sumSpectraBins ()
    {
        //Sum bins
        Arrays.fill(m_SumSpectra, 0);
        m_SumSpectraCount = 0;
        for (int i = 0; i < m_SpectraList.size(); i ++)
        {
            int[] spectra = m_SpectraList.get(i).m_Spectra;
            
            for (int j = 2; j < NUM_CHANNELS; j ++)
            {
                m_SumSpectra[j] += spectra[j];
                m_SumSpectraCount += spectra[j];
            }
        }

        //Zero requested channels for processing
        if (channelsToZero != null)
        {
            for (int j = 0; j < channelsToZero.length; j ++)
            {
                m_SumSpectraCount -= (int)m_SumSpectra[channelsToZero[j]];
                m_SumSpectra[channelsToZero[j]] = 0;
            }
        }
    }

    /**
     * Do processing of spectra data to compute GoF values for isotopes
     *
     * @param spectraTime Timestamp of received spectra
     * @return
     */
    private void processSpectra (long spectraTime)
    {
        //Do processing
        if (nativeRawSpectrum(m_SumSpectra) != 0)
            System.out.println ("Possible error calling fittedSpectrum");
        m_FastOption = m_SumSpectraCount < 500;
        if (nativeFitSpectrum(m_FastOption) != 0)
            System.out.println ("Possible error calling fitSpectrum");
        
        //Get results
        if (nativeGetFitResults(m_GoodnessOfFit, m_ElapsedTime) != 0)
            System.out.println ("Possible error calling getFitResults");
        if (nativeGetFittedSpectrum (m_FittedSpectra) != 0)
            System.out.println ("Possible error calling getFittedSpectrum");
        
        //Get/log parameters
        try
        {
            writeFittedResults (m_LogStreamFitted, spectraTime);
        }
        catch (Exception e)
        {
            System.err.println("Could not write fitted log data to file for Bladewerx alpha detector");
        }
    }
        
    
    @Override
    public void run ()
    {
        if (processBladewerxDLL)
            runBladewerxDLL();
        else
            runAetnaInterface();
    }


    private void runBladewerxDLL()
    {
        //Initialize DLL processor
        nativeInitialize ();

        int channelList[] = new int [backgroundPeaksDefined+1];
        boolean referenceList[] = new boolean [backgroundPeaksDefined+1];
        float peakCountsList[] = new float [backgroundPeaksDefined+1];
        float peakVarianceList[] = new float [backgroundPeaksDefined+1];
        float peakChannelList[] = new float [backgroundPeaksDefined+1];
                
        long lastLog = System.currentTimeMillis();
        long lastStartTime = lastLog;
        long lastLogCount = lastLog;
        long currTime;
        m_Running = true;
        
        while (m_Running)
        {            
            currTime = System.currentTimeMillis();
            //If we have elapsed a certain time period or received a certain number of counts, we will conclude a spectra collection,
            //add it to the processor, and start a new collection
            
            if (currTime - lastLogCount > m_LogCountRateFrequencyMs || (m_MsgCounts + (m_GammaAndBeta - m_GammaOnly)) > m_LogCountRateThreshold)
            {
                countMessage newMsg = new countMessage();
                newMsg.setTimestampMs(currTime);
                newMsg.setStartTime(lastLogCount);
                newMsg.setAlphaCounts(m_MsgCounts);
                newMsg.setChanZeroCounts(m_GammaAndBeta);
                newMsg.setChanOneCounts(m_GammaOnly);
                
                if (m_Interface != null)
                {
                    m_Interface.insertMessage(newMsg);
                }
                
                lastLogCount = currTime;
                clearCounts();
            }
            
            if (currTime - lastLog > m_LogFrequencyMs || m_CurrSpectraCount > m_LogCountThreshold)
            {
                synchronized (m_CurrSpectraLock)
                {
                    //Get piccolo data
                    getPicTelemBelief ();

                    //Get P/T correction
                    m_ChannelOffset = computeOffset();

                    //Rebin data based on correction
                    rebinCurrChannels (m_RebinThreshold, m_ChannelOffset);

                    //Log data
                    logSpectraSlice (currTime);

                    //If processing list is full, remove earliest datapoint
                    if (m_SpectraList.size() == m_NumSpectraToProcess)
                    {
                        lastStartTime = m_SpectraList.peekLast().timestamp;
                        m_SpectraList.removeLast();
                        
                    }

                    //Add current spectra to processing list
                    m_SpectraList.push(new SpectraPair (currTime, m_RebinnedSpectra));

                    //Clear current spectra to resume spectra collection
                    clearCurrSpectra();
                }

                //Combine spectra list into a single spectra for processing
                sumSpectraBins ();
                ensureLogFileFitted();

                //Define background peaks for processing
                nativeResetPeaks();
                for (int i = 0; i < backgroundPeaksDefined; i ++)
                {
                   nativePutPeakChannel((short)i, (short)m_BackgroundChannels[i]);
                   nativePutReferencePeak ((short)i, m_BackgroundReference[i]);
                }

                bladewerxDllDetectionReportMessage newDetMsg = new bladewerxDllDetectionReportMessage ();
                double backgroundGoF = 0;
                //For each isotope of interest defined...
                for (int i = -1; i < isotopePeaksDefined; i ++)
                {
                    //If i is -1, we'll compute for background first.  Otherwise, add isotope of interest peak
                    if (i >= 0)
                    {
                        nativePutPeakChannel((short)backgroundPeaksDefined, (short)m_IsotopeChannels[i]);
                        nativePutReferencePeak ((short)backgroundPeaksDefined, m_IsotopeReference[i]);
                    }
                    nativeStart ();

                    //Do processing algorithms
                    processSpectra (currTime);

                    //Extract data for logging fitted results
                    for (int j = 0; j < backgroundPeaksDefined + 1; j ++)
                    {
                        readPeakData((short)j);
                        channelList[j] = m_NativeChannel[0];
                        referenceList[j] = m_NativeReference[0];
                        peakCountsList[j] = m_NativePeakCounts[0];
                        peakVarianceList[j] = m_NativePeakVariance[0];
                        peakChannelList[j] = m_NativePeakChannel[0];
                    }

                    if (i >= 0)
                    {
                        //Add result of processing for isotope to detection message
                        newDetMsg.addIdResult(i>=0?m_IsotopeName[i]:"background", m_GoodnessOfFit[0], backgroundGoF, m_ElapsedTime[0], m_FastOption,
                            channelList, referenceList, peakCountsList, peakVarianceList, peakChannelList);
                    }
                    else if (i == -1)
                    {
                        //If processing for background, just store background GoF value for comparison to isotope GoF values.
                        backgroundGoF = m_GoodnessOfFit[0];
                    }
                    System.out.println ("Processing alpha for isotope: " + (i>=0?m_IsotopeChannels[i]:"background") + ", GoF = " + m_GoodnessOfFit[0] + ", confidence = " + (i>=0?newDetMsg.returnIdResult(i).m_Confidence:0));
                    
                    nativeStop ();
                }

                //Send detection message detailing all isotopes to interface
                if (newDetMsg.getNumIdResults() > 0 && m_Interface != null)
                    m_Interface.insertMessage(newDetMsg);
                
                //Send a single composite histogram message regardless of results
                bladewerxCompositeHistogramMessage newHistMsg = new bladewerxCompositeHistogramMessage();
                newHistMsg.setTimestampMs(currTime);
                newHistMsg.setSummedSpectra(m_SumSpectra);
                newHistMsg.setNumSpectra(m_SpectraList.size());
                newHistMsg.setTotalCounts (m_SumSpectraCount);
                newHistMsg.setLiveTime(m_SpectraList.peekFirst().timestamp - lastStartTime);
                if (m_Interface != null)
                    m_Interface.insertMessage (newHistMsg);

                lastLog = currTime;
            }
            else
            {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(bladewerxProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        nativeStop ();
    }

    /**
     * Processing method that uses the AETNA service for alpha spectra analysis
     */
    private void runAetnaInterface() 
    {                
        long lastLog = System.currentTimeMillis();
        long lastStartTime = lastLog;
        long lastLogCount = lastLog;
        long currTime;
        m_Running = true;
        
        while (m_Running)
        {            
            //Be sure we are connected to AETNA before trying to do anything...
            if (!m_aetnaConnected)
            {
                try
                {
                    aetna = new AETNAServiceInterface2();
                    m_aetnaConnected = true;

                    try
                    {
                        getLibrarySettings();

                        // First crack at setting AENTA gain.
                        // TODO Should gain be automatically adjusted? 
                        aetnaGain = 0;
                        if (!aetna.setClassifierParameters(aetnaClassifier, a0, a1, a2, a3, eLow, aetnaGain))
                            throw new Exception ("Could not set classifier ID for alpha processing");

                        //Send detection message reporting first spectra is being collected
                        bladewerxAETNADetectionReportMessage newMessage = new bladewerxAETNADetectionReportMessage ();
                        newMessage.setTimestampMs (System.currentTimeMillis());
                        newMessage.addIsotope("Collecting first spectra window...", 0);
                        
                        if(m_Interface != null)
                        {
                        	m_Interface.insertMessage(newMessage);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    Thread.sleep(1000);
                    continue;
                }
                catch (IOException ex)
                {
                    Logger.getLogger(bladewerxProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println ("Unable to connect to AETNA service.");
                    m_aetnaConnected = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        	
            // Get current time
        	currTime = System.currentTimeMillis();
                
            if (currTime - lastLogCount > m_LogCountRateFrequencyMs)
            {
                countMessage newMsg = new countMessage();
                newMsg.setTimestampMs(currTime);
                newMsg.setStartTime(lastLogCount);
                newMsg.setDuration(currTime - lastLogCount);
                newMsg.setAlphaCounts(m_MsgCounts);
                newMsg.setChanZeroCounts(m_GammaAndBeta);
                newMsg.setChanOneCounts(m_GammaOnly);
                
                if (m_Interface != null)
                {
                    m_Interface.insertMessage(newMsg);
                }
                
                lastLogCount = currTime;
                clearCounts();
            }
        	
            //If we have elapsed a certain time period or received a certain number of counts, we will conclude a spectra collection,
            //add it to the processor, and start a new collection
            if (currTime - lastLog > m_LogFrequencyMs || m_CurrSpectraCount > m_LogCountThreshold)
            {
                synchronized (m_CurrSpectraLock)
                {
                    //Get piccolo data
                    getPicTelemBelief ();

                    //Get P/T correction
                    m_ChannelOffset = computeOffset();

                    //Rebin data based on correction
                    rebinCurrChannels(m_RebinThreshold, m_ChannelOffset);

                    //Log data
                    logSpectraSlice(currTime);

                    //If processing list is full, remove earliest datapoint
                    if (m_SpectraList.size() == m_NumSpectraToProcess)
                    {
                        lastStartTime = m_SpectraList.peekLast().timestamp;
                        m_SpectraList.removeLast();
                    }

                    //Add current spectra to processing list
                    m_SpectraList.push(new SpectraPair (currTime, m_RebinnedSpectra));

                    //Clear current spectra to resume spectra collection
                    clearCurrSpectra();
                }

                //Combine spectra list into a single spectra for processing
                sumSpectraBins();
                
                try
                {               
                    // Do not provide an all zero spectra to AETNA.
                    AETNAResult result = null;
                    if(m_SumSpectraCount != 0)
                    {
                        //adjust data and process it
                        float[] energyBinBoundaries = aetna.getEnergyBinBoundaries(aetnaClassifier);
                        m_SumSpectra = rebinChannels(m_SumSpectra, energyBinBoundaries, a0, a1, a2, a3, eLow, aetnaGain);

                        result = aetna.classify(aetnaClassifier, m_SumSpectra);


                        float classSpec[] = result.estimated_spectrum;
                        m_ClassifiedSpectra = classSpec.clone();
                    }	

                    // Ensure that the log file can hold the spectra data before logging.
                    ensureLogFileFitted();

                    // Log the spectra data.
                    try
                    {
                        writeFittedResults(m_LogStreamFitted, result, currTime);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Could not write fitted log data to file for Bladewerx alpha detector");
                    }

                    //Add result of processing for isotope to detection message
                    bladewerxAETNADetectionReportMessage newMessage = new bladewerxAETNADetectionReportMessage();
                    newMessage.setTimestampMs (currTime);

                    if (result != null)
                    {
                        for (int i = 0; i < result.isotopes.size(); i ++)
                        {
                            AETNAResult.IsotopeConcentraion isotopeConcentration = result.isotopes.get(i);
                            newMessage.addIsotope(isotopeConcentration.isotope, isotopeConcentration.concentration*100);
                            System.out.println ("Processing alpha for isotope: " + isotopeConcentration.isotope + ", concentration = " + isotopeConcentration.concentration);
                        }
                    }

                    if (m_Interface != null)
                    {
                            m_Interface.insertMessage(newMessage);
                    }

                    //Send a single composite histogram message regardless of results
                    bladewerxCompositeHistogramMessage newHistMsg = new bladewerxCompositeHistogramMessage();
                    newHistMsg.setTimestampMs(currTime);
                    newHistMsg.setSummedSpectra(m_SumSpectra);
                    newHistMsg.setClassifiedSpectra(result==null?m_SumSpectra:result.estimated_spectrum);
                    newHistMsg.setNumSpectra(m_SpectraList.size());
                    newHistMsg.setTotalCounts (m_SumSpectraCount);
                    newHistMsg.setLiveTime(m_SpectraList.peekFirst().timestamp - lastStartTime);

                    if (m_Interface != null)
                    {
                        m_Interface.insertMessage(newHistMsg);
                    }
	                
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.err.println ("Could not process alpha data.");
                } 

                lastLog = currTime;
            }
            else
            {
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(bladewerxProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    

    /**
     * Native function calls to Bladewerx.dll processing algorithms
     * @return
     */
    private native int nativeInitialize ();
    private native int nativeStart ();
    private native int nativeStop ();
    private native int nativeReset ();
    private native int nativeResetPeaks ();
    private native int nativeFitSpectrum (boolean bFast);
    private native int nativeGetSpectrum (float plSpectrum [], int plTotalCounts[]);
    private native int nativeGetFittedSpectrum (float pSpectrum []);
    private native int nativeGetFitParameters (short nPeak, float pCounts[], float pVariance[], float pChannel[]);
    private native int nativeGetFitResults (float pGoF[], int plElapsed []);
    private native int nativeGetPeakChannel (short nPeak, short pnChannel[]);
    private native int nativePutPeakChannel (short nPeak, short nChannel);
    private native int nativeGetReferencePeak (short nPeak, boolean pbReference[]);
    private native int nativePutReferencePeak (short nPeak, boolean bReference);
    private native int nativeRawSpectrum (float plSpectrum []);

    static
    {
        try
        {
            //
            // Load the c++ dll that talks to the laser directly
            //
            System.loadLibrary("Bladewerx");
            
            //System.loadLibrary("C:\\Documents and Settings\\HumphJC1\\My Documents\\wacs\\Bladewerx\\Debug\\Bladewerx");  
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println(e.getLocalizedMessage());
            System.err.println(e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println ("Unknown exception loading bwPFAPL library.");
        }
    }







    /**
     * Test input stream to read playback files during playback
     */
    private static BufferedReader m_SocketInputStream = null;

    /**
     * Test input file to be read during playback
     */
    private static File m_CurrentLogFile = null;

    /**
     * Test variable, true when playback is running
     */
    private static boolean playbackRunning = false;

    /**
     * Test variable, how much faster log data should be replayed compared to live time
     */
    private final static double PLAYBACK_THREAD_SPEED_REDUCTION = 5.0;

    /**
     * Test variable, counter of log files being played back
     */
    private static int m_LogFileCounter = 0;
    
    /**
     * Name of output file name to log data
     */
    private static String m_LogFilename;

    /**
     * Test function to playback raw data logs from file for re-processing
     *
     * @return True if new file found, false otherwise
     */
    public static boolean getNextPlaybackFile (final String logFilename)
    {
        Formatter format = new Formatter();
        format.format("%1$s%2$05d%3$s", logFilename, m_LogFileCounter++, ".dat");
        m_CurrentLogFile = new File(format.out().toString());

        try
        {
            m_SocketInputStream = new BufferedReader (new InputStreamReader (new FileInputStream (m_CurrentLogFile)));
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
    
    /**
     * Get the counter value specified in the provided filename
     * 
     * @param filename Filename to find index from
     * @return Index of log file
     */
    public static int parseLogCounter (String filename)
    {
        int startIdx = filename.lastIndexOf("_") + 1;
        int endIdx = filename.lastIndexOf(".");
        
        return Integer.parseInt (filename.substring(startIdx, endIdx));
    }
    
    /**
     * Creates and returns a Thread that parses the given alpha raw data file line-by-line and populates
     * @param process bladewerxProcessor
     * @param playbackLogFilename Absolute path to an alpha raw data file
     * 
     * @return File playback Thread
     */
    public static Thread createFilePlaybackThread(final bladewerxProcessor process, final String playbackLogFilename)
    {
		m_LogFileCounter = parseLogCounter (playbackLogFilename);
        
        //Trim off the .dat and index counter of the filename for easier file changes
        int endIdx = playbackLogFilename.lastIndexOf("_") + 1;
        final String partialLogFilename = playbackLogFilename.substring(0, endIdx);
        
    	Thread playback = new Thread ()
        {
            public void run ()
            {
                playbackRunning = true;
                long lastReadTime = -1;
                process.m_LogCountThreshold = 0;
                int lineNum = 1;

                while (playbackRunning)
                {
                    try
                    {
                        String line = null;
                        try
                        {
                            line = m_SocketInputStream.readLine();
                        }
                        catch (Exception e)
                        {
                            //We reached the end of a file, either because m_SocketInputStream is null (haven't started yet) or we read past EOF.
                            if (!getNextPlaybackFile(partialLogFilename))
                            {
                                //We didn't get a new file
                                playbackRunning = false;
                                continue;
                            }

                            line = m_SocketInputStream.readLine(); // Skip the first line of the file, because it contains column header names.
                            line = m_SocketInputStream.readLine();
                        }

                        //Read raw data from log file
                        String [] tokens = line.split("\t");
                        int idx = 0;
                        long logTime = Long.parseLong(tokens[idx++]);
                        long spectraTime = Long.parseLong(tokens[idx++]);

                        if (lastReadTime > 0)
                        {
                            Thread.sleep ((long)((spectraTime - lastReadTime)/PLAYBACK_THREAD_SPEED_REDUCTION));
                        }
                        lastReadTime = spectraTime;

                        int offset = Integer.parseInt(tokens[idx++]);
                        int gain = Integer.parseInt(tokens[idx++]);
                        int threshold = Integer.parseInt(tokens[idx++]);
                        int scale = Integer.parseInt(tokens[idx++]);
                        
                        // Reconstruct the status message
                        bladewerxStatusMessage msg = new bladewerxStatusMessage();
                        msg.setOffset(offset);
                        msg.setGain(gain);
                        msg.setThreshold(threshold);
                        msg.setScale(scale);
                        process.handleBladewerxStatusMessage(msg);

                        synchronized (process.m_CurrSpectraLock)
                        {
							/*
                            //int americium[] = {312,84,13,2,0,1,0,0,0,1,12,2,4,5,2,1,14,4,2,2,2,2,3,1,2,2,3,1,2,2,2,2,15,1,2,2,1,0,4,0,2,3,7,1,1,4,13,2,16,14,2,9,3,2,2,2,5,18,2,3,7,3,3,1,3,24,18,17,0,10,4,3,4,4,3,6,7,17,8,7,8,4,10,20,6,8,7,6,8,8,3,9,6,10,9,10,13,8,8,21,17,16,14,15,20,24,20,20,23,31,29,31,38,34,43,39,55,76,78,72,89,100,105,124,158,145,132,184,199,220,259,261,242,301,313,302,341,339,371,355,424,406,410,488,426,454,440,499,567,556,580,590,620,677,613,680,727,679,668,498,325,148,60,17,3,0,0,0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,0,12,0,0,0,0,0,0,0,1,0,0,16};
                            int americium[] = {171,140,7,30,109,358,546,533,381,301,77,50,37,18,5,0,84,1,3,2,4,6,5,6,5,7,7,7,10,10,4,4,88,5,8,4,12,12,6,4,9,7,8,6,3,2,89,3,92,90,7,3,6,4,5,4,8,91,5,1,8,4,9,5,11,172,96,92,8,6,5,4,11,8,10,8,14,91,6,7,7,10,7,93,10,8,14,8,8,6,11,16,10,8,16,15,14,9,8,11,17,15,19,17,17,38,25,42,38,52,34,45,59,53,80,64,79,80,192,120,108,135,163,181,202,251,276,320,354,385,432,455,525,563,650,672,750,796,879,977,952,1122,1056,1170,1188,1239,1360,1469,1485,1449,1550,1545,1612,1670,1698,1807,1731,1902,1960,2153,2326,2375,2295,2364,2325,2104,1619,1099,627,273,106,44,7,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,1,1,2,0,0,1,0,0,0,0,1,1,1,0,0,0,2,1,0,0,56,0,0,1,28,0,1,0,0,3,0,0,0,1,1,1,0,0,1,111};
                            //int background[] = {13814,2368,1612,887,385,154,58,25,23,26,37,44,41,51,34,42,23,17,29,12,16,9,10,7,10,6,7,3,3,2,1,1,0,0,0,1,0,0,2,1,1,1,1,2,0,1,0,0,0,0,2,0,0,1,0,0,0,1,0,0,0,2,0,0,1,1,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,1,0,1,2,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,1,0,1,0,0,2,0,0,0,1,0,0,1,0,0,1,1,1,1,2,0,0,2,0,2,1,0,3,0,1,1,2,2,3,0,2,0,4,1,0,3,1,1,1,0,1,2,3,3,3,3,2,2,3,1,2,3,1,5,3,2,2,5,3,9,3,9,3,1,8,18,5,10,11,2,6,5,2,2,2,4,5,1,2,2,1,8,2,2,7,0,4,6,7,3,6,3,7,6,5,6,8,7,4,7,7,3,3,5,7,9,14,15,13,12,11,11,13,13,12,22,21,17,35,21,25,35,34,24,38,39,33,39,27,49,48,47,53,50,38,25,14,6,0,1,23};
                            int background[] = {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,5,8,7,5,10,42,40,22,22,20,24,25,20,54,570,699,257,643,619,442,423,352,275,211,159,158,121,121,78,85,55,68,70,52,37,28,30,38,31,37,29,36,30,26,25,27,28,35,26,24,24,24,22,34,23,33,36,29,41,32,22,37,30,23,34,32,26,43,32,31,40,31,47,36,29,41,42,31,45,48,49,42,42,52,41,49,39,45,58,55,59,73,49,85,72,80,56,75,86,92,69,77,82,81,96,111,97,102,118,105,141,129,122,136,144,167,149,166,167,188,190,201,216,210,220,229,271,238,280,327,301,330,321,350,348,365,357,414,418,429,476,580,515,679,595,681,665,702,696,817,801,879,953,948,996,1075,1077,1115,1292,1393,1341,1395,1557,1614,1531,1603,1548,1419,1287,1166,929,668,521,410,312,257,237,255,266,241,250,311,304,299,336,350,383,432,465,472,573,549,581,679,700,726,829,852,853,954,1054,1142,1155,1299,1376,1419,1524,1730,1821,1935,2106,2174,2342,2506,2768,2896,3103,3262,3716,3860,4012,4303,4827,5888,5558,6015,6527,6849,7565,8071,8694,8667,8727,8443,7444,6208,4882,3260,47738};

                            double scaleAm = 10.0;
                            int zeroIndicies = 0;
                            int binShift = 0;
                            int counts [] = new int [256];
                            for (int i = 0; i < americium.length; i ++)
                            {
                                try{
                                    counts[i+binShift] = (int)Math.ceil(americium[i]*scaleAm + background[i]);
                                }
                                catch (Exception e)
                                {}
                            }
                            for (int i = 0; i < zeroIndicies; i ++)
                                counts[i] = 0;

                            //int counts[] = {10,10,10,10,10,10,10,25,23,27,49,46,45,56,36,43,37,21,31,14,18,11,13,8,12,8,10,4,5,4,3,3,15,1,2,3,1,0,6,1,3,4,8,3,1,5,13,2,16,14,4,9,3,3,2,2,5,19,2,3,7,5,3,1,4,25,19,17,0,10,4,4,4,4,3,6,8,17,8,7,8,4,11,20,6,8,7,6,9,8,4,9,7,12,9,10,13,8,9,21,17,17,14,15,20,24,20,20,23,31,30,31,39,34,43,41,55,76,78,73,89,100,106,124,158,146,133,185,200,222,259,261,244,301,315,303,341,342,371,356,425,408,412,491,426,456,440,503,568,556,583,591,621,678,613,681,729,682,671,501,328,150,62,20,4,2,3,1,5,3,3,3,5,3,9,4,9,3,1,8,18,5,10,11,2,6,5,2,2,2,4,5,2,3,2,1,8,2,2,7,0,4,6,7,3,7,3,7,6,5,7,9,7,4,7,7,3,3,5,7,9,14,15,13,12,11,11,13,13,12,22,21,17,35,21,26,36,35,24,38,39,33,40,27,61,48,47,53,50,38,25,14,7,0,1,39};
                            //int counts[] = {13846,2377,1614,888,385,155,58,25,23,27,39,45,42,52,35,43,25,18,30,13,17,10,11,8,11,7,8,4,4,3,2,2,2,1,1,2,1,0,3,1,2,2,2,3,1,2,2,1,2,2,3,1,1,2,1,1,1,3,1,1,1,3,1,1,2,4,3,2,0,1,1,2,1,1,1,1,2,2,1,1,1,1,2,2,1,1,1,1,2,1,2,1,2,3,1,1,2,1,2,3,2,3,2,2,2,3,2,2,3,4,4,4,5,4,5,6,6,8,8,9,9,10,12,13,16,16,15,20,21,24,26,27,27,31,34,32,35,37,38,37,44,43,43,52,43,48,44,54,58,56,61,60,63,69,62,69,75,71,70,53,36,17,8,5,2,2,3,1,5,3,3,3,5,3,9,4,9,3,1,8,18,5,10,11,2,6,5,2,2,2,4,5,2,3,2,1,8,2,2,7,0,4,6,7,3,7,3,7,6,5,7,9,7,4,7,7,3,3,5,7,9,14,15,13,12,11,11,13,13,12,22,21,17,35,21,26,36,35,24,38,39,33,40,27,51,48,47,53,50,38,25,14,7,0,1,25};

							*/
                        	
                            //Add data from file to processing spectra lists.  Can fake data with hardcoded data above by choosing
                            //to set data based on counts variable, rather than token list from file
                            System.out.print ("Spectra #" + lineNum++ + ": ");
                            for (int i = 0; i < 256; i ++)
                            {
                                // int value = counts[i];
                                int value = (int)Float.parseFloat(tokens[idx++]);
                                //Can zero out low (beta) channels through config file for real processing, do it manually here.
                                //if (i < 50)
                                //    value = 0;
                                
                                process.m_CurrSpectra[i] = value;
                                process.m_CurrSpectraCount += value;

                                System.out.print (value + ",");
                            }
                            System.out.println ();
                        }
                        
                        int rebinThreshold = Integer.parseInt(tokens[idx++]);
                        int channelOffset = Integer.parseInt(tokens[idx++]);
                        
                        System.out.println ("Spectra loaded from file.");

                        System.out.println ("Waiting for processor to grab spectra...");
                        while (process.m_CurrSpectraCount > 0)
                            Thread.sleep(1);
                        System.out.println ("Processor has spectra");

                    }
                    catch (Exception e)
                    {                    	
                    	playbackRunning = false;
                        System.out.println ("Playback file, index " + m_LogFileCounter + ", ended abruptly: " + m_CurrentLogFile);
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

                System.out.println ("Playback ended for logs: " + m_CurrentLogFile);
            }
        };
        
        return playback;
    }

    /**
     * This main function is a tester to replay Bladewerx log files for re-processing
     * @param args
     */
    public static void main (String args[])
    {
        final bladewerxProcessor process = new bladewerxProcessor(null, null);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(bladewerxProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String alphaRawLogFile = "C:\\Documents and Settings\\olsoncc1\\workspace\\WACS\\WACS_Swarm\\AlphaLogs\\BladewerxAlphaRaw_1337623415227_00000.dat";
        
		Thread playback = createFilePlaybackThread(process, alphaRawLogFile);
        playback.start();
    }
}
