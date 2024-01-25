package edu.jhuapl.nstd.cbrnPods;

import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCalibration;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaEthernetCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.GammaUsbCountRates;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage.GammaDetectorMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportCompositeHistogramMessage;
import edu.jhuapl.nstd.cbrnPods.messages.BridgeportMessages.bridgeportDetectionReportMessage;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.swarm.belief.CountItem;
import edu.jhuapl.spectra.AETNAResult;
import edu.jhuapl.spectra.AETNAServiceInterface2;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor for Bridgeport gamma data.  Sums foreground spectra and analyzes against background data.  Sends
 * composite histogram data and detection results.
 *
 * @author humphjc1
 */
public class bridgeportProcessor extends AbstractAETNAServiceProcessor
{
    /**
     * Class to pair count rates with histogram data
     */
    private class spectraPair
    {
        GammaCountRates countRates;
        RNHistogram histogram;
    }

    /**
     * Message to use when reporting that background is still being accumulated prior to processing data
     */
    static public String accumBackgroundMsg = "Accumulating background...";
    
    /**
     * Interface to send generated messages for
     */
    private cbrnPodGenericInterface m_Interface;
    
    /**
     * Timestamp of the last count rate received and not yet paired.  -1 If no
     * count rate has been received since last pairing
     */
    private long lastCountRateTimestamp = -1;
    
    /**
     * Last count rate object received that wasn't paired
     */
    private GammaEthernetCountRates lastCountRate = null;
    
    /**
     * Timestamp of the last histogram received and not yet paired.  -1 If no
     * histogram has been received since last pairing
     */
    private long lastHistogramTimestamp = -1;
    
    /**
     * Last histogram object received that wasn't paired
     */
    private RNHistogram lastHistogram = null;
    
    /**
     * Sync difference between histogram and count rate messages to pair them together.
     * Makes sure we don't pair un-synced data
     */
    private long pairingSyncThresholdMs;
    
    /**
     * List of spectra to be used as the foreground, this is the spectra as it is accumulated
     */
    private Vector <spectraPair> m_ForegroundSpectra;
    
    /**
     * Lock object for m_ForegroundSpectra object
     */
    private final Object m_ForegroundSpectraLock = new Object();
    
    /**
     * List of spectra to be used as the foreground, this is the spectra to actually process.
     */
    private Vector <spectraPair> m_ForegroundSpectraProcess;
    
    /**
     * Maximum duration of spectrum collection for summing foreground
     */
    private int maxForegroundSpectraTime;
    
    /**
     * List of spectra to be used as the background
     */
    private LinkedList <spectraPair> m_BackgroundSpectra;
    
    /**
     * Summed spectra for background processing
     */
    private float m_SumBackgroundSpectraRaw[] = new float [NUM_CHANNELS];
    
    /**
     * Summed spectra for foreground processing
     */
    private float m_SumForegroundSpectraRaw[] = new float [NUM_CHANNELS];
    
    /**
     * Summed spectra background after rebinning
     */
    private float m_SumBackgroundSpectraRebinned[] = new float [NUM_CHANNELS];

    /**
     * Summed spectra for foreground after rebinning
     */
    private float m_SumForegroundSpectraRebinned[] = new float [NUM_CHANNELS];

    /**
     * Total counts in m_SumForegroundSpectra array
     */
    private int m_ForegroundTotalCounts;
    
    /**
     * Sum of live times in foreground spectra
     */
    private double m_ForegroundTotalLiveTime;
    
    /**
     * Time foreground accumulation started running (millisecond timestamp)
     */
    private long m_ForegroundStartTime;
    
    /**
     * Time background accumulation started running (millisecond timestamp)
     */
    private long m_BackgroundStartTime;

    /**
     * Sum of live times in background spectra
     */
    private double m_BackgroundTotalLiveTime;
    
    /**
     * Maximum amount of time to spend summing background spectra
     */
    private int maxBackgroundSpectraTime;

    /**
     * Minimum number of counts expected in background data.  If the current foreground data has fewer counts, it must not be background
     */
    private double m_MinExpectedBackgroundCounts;

    /**
     * Maximum number of counts expected in background data.  If the current foreground data has more counts, it must not be background
     */
    private double m_MaxExpectedBackgroundCounts;

    private boolean m_ResetBackground = false;
    
    /**
     * Number of channels
     */
    public final static int NUM_CHANNELS = 1024;


    /**
     * Output file for logging
     */
    private File m_CurrentLogFile;

    /**
     * Counter for filenames to differentiate if they get too large
     */
    private int m_LogFileCounter = 0;

    /**
     * Name of output file name to log data
     */
    private String m_LogFilename;

    /**
     * Output stream for logging
     */
    private DataOutputStream m_LogStream;

    /**
     * Maximum file size for log files.  Starts a new file if we exceed this value.
     */
    private final static int MAX_LOG_FILE_BYTES = 10000000;

    /**
     * Folder to save log files to
     */
    private String m_LogFolderName;

    /**
     * Total number of spectra collected since powered on.  Used to skip a few potentially bad messages
     */
    private int totalSpectraCollected;

    /**
     * Number of initial spectra to skip for processing.  The first couple spectra seem to come through ugly, so we'll ignore
     * them for processing and background collection.
     */
    private int initialSpectraSkip;

    /**
     * If true, we are loading fake background data from the config file rather than collecting real background
     */
    boolean useFakeBackgroundData;

    /**
     * If useFakeBackgroundData is true, this is the string that contains the fake background data
     */
    String fakeBackgroundDataString;

    /**
     * If useFakeBackgroundData is true, this is the fake background data parsed into an array
     */
    int[] fakeBackgroundData;


    /**
     * Channels to permanently reset to zero when new data comes in.  Used to eliminate erroneous spikes
     */
    private int channelsToZero[] = null;
    

    /**
     * Create new object, start processing thread
     *
     * @param intr Pod interface to send message through
     */
    public bridgeportProcessor(cbrnPodGenericInterface intr)
    {
        this.setName ("WACS-BridgeportProcessor");
        setDaemon(true);
        readConfig ();
        
        m_Interface = intr;
        
        m_ForegroundSpectra = new Vector <spectraPair> ();
        m_BackgroundSpectra = new LinkedList <spectraPair> ();
        m_ForegroundSpectraProcess = null;
        totalSpectraCollected = 0;
        m_ResetBackground = false;

        setupLogFolder ();
        m_LogFilename = m_LogFolderName + "BridgeportGammaFitted_" + System.currentTimeMillis() + "_";

        //Start processing thread
        this.start ();
    }

    /**
     * Read config values
     *
     */
    private void readConfig ()
    {
        pairingSyncThresholdMs = Config.getConfig().getPropertyAsLong("BridgeportProcessor.PairingSyncThreshold.Ms", 1000);
        maxForegroundSpectraTime = Config.getConfig().getPropertyAsInteger("BridgeportProcessor.MaxForegroundSpectraTime.Secs", 60);
        maxBackgroundSpectraTime = Config.getConfig().getPropertyAsInteger("BridgeportProcessor.MaxBackgroundSpectraTime.Secs", 600);
        m_LogFolderName = Config.getConfig().getProperty("Bridgeport.LogFolder", "./GammaLogs");
        aetnaClassifier = Config.getConfig().getProperty("BridgeportProcessor.ClassiferName", "sensor");
        useFakeBackgroundData = Config.getConfig().getPropertyAsBoolean ("BridgeportProcessor.UseFakeBackgroundData", false);
        initialSpectraSkip = Config.getConfig().getPropertyAsInteger("BridgeportProcessor.InitialSpectraSkip", 2);
        //double m_Scan_HistogramPeriod = (float) Config.getConfig().getPropertyAsDouble("Bridgeport.StatisticsPeriod.Secs", 10);
        m_MinExpectedBackgroundCounts = maxForegroundSpectraTime*Config.getConfig().getPropertyAsInteger("BridgeportProcessor.MinExpectedBackgroundCounts.PerSec", 0);
        m_MaxExpectedBackgroundCounts = maxForegroundSpectraTime*Config.getConfig().getPropertyAsInteger("BridgeportProcessor.MaxExpectedBackgroundCounts.PerSec", 200);
        if (useFakeBackgroundData)
        {
            System.out.println ("Using fake background data from config file for Bridgeport gamma processing...");
            fakeBackgroundDataString = Config.getConfig().getProperty("BridgeportProcessor.FakeBackgroundData", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,2,1,1,0,2,0,1,0,1,2,1,8,5,7,12,2,10,17,20,17,17,18,11,17,13,22,24,27,23,21,26,27,24,29,19,26,21,22,25,24,26,20,23,25,29,25,27,22,26,29,21,21,33,35,27,25,34,39,34,37,31,35,34,30,39,39,30,40,34,45,43,43,47,45,36,45,34,36,44,43,45,47,55,65,55,54,61,59,59,48,65,50,70,42,54,51,84,66,75,78,84,71,69,68,57,68,60,71,77,93,64,64,96,74,74,71,79,84,80,60,75,88,57,78,87,59,66,76,81,82,76,90,80,91,77,80,86,87,86,85,106,90,90,75,93,94,91,78,87,81,83,89,98,94,94,81,87,94,95,93,97,92,84,83,78,91,88,82,78,96,97,97,92,101,87,78,90,87,89,91,86,95,89,67,103,109,92,97,77,75,94,59,75,97,96,94,94,84,87,76,82,66,84,77,76,88,88,95,76,91,79,86,103,95,86,70,77,90,87,86,81,77,71,87,93,79,66,78,87,64,88,84,79,68,88,90,71,80,66,85,77,65,90,80,62,70,61,64,70,56,71,76,72,64,59,54,64,69,81,71,70,51,63,68,61,67,58,69,56,51,66,72,76,66,55,67,48,66,55,72,56,68,61,62,59,73,63,59,56,69,69,55,75,59,70,57,54,52,67,58,64,53,64,38,56,52,52,53,51,45,67,53,58,64,49,45,47,54,55,51,62,49,64,45,57,44,71,47,41,52,56,60,53,59,50,54,45,55,56,51,43,57,39,44,52,46,44,35,42,45,56,58,42,48,46,49,55,39,44,55,57,50,45,36,40,44,48,47,43,69,52,38,42,59,45,54,45,45,43,53,44,51,40,56,43,42,33,57,40,33,44,56,41,47,45,39,29,38,46,39,39,30,42,42,43,37,34,44,41,35,32,40,40,41,33,48,21,41,43,34,38,42,26,38,24,27,30,33,30,33,24,24,32,24,38,38,38,32,27,25,24,29,19,32,28,26,28,29,38,25,32,31,19,23,18,36,27,20,29,26,27,30,26,24,29,22,24,30,19,28,19,27,32,26,14,23,18,15,21,19,23,24,21,33,28,23,17,18,24,20,17,21,27,18,19,21,18,18,25,23,23,22,21,15,23,18,22,21,20,14,17,26,22,24,17,18,25,19,18,13,21,29,27,12,20,14,13,13,22,21,24,20,13,14,22,17,16,17,20,18,19,9,19,15,25,10,16,13,20,17,16,14,22,17,17,11,13,11,21,12,21,27,9,11,12,23,22,7,13,16,15,14,8,18,16,21,24,14,18,22,8,13,20,16,15,22,27,19,22,17,18,9,16,22,16,17,9,16,19,9,13,14,14,10,12,11,10,15,24,15,12,19,16,14,17,22,11,14,11,10,15,8,14,17,13,9,16,15,9,6,9,15,12,14,15,12,16,14,12,16,13,15,9,13,10,10,17,11,12,8,5,14,14,7,18,8,8,19,7,10,14,11,12,7,10,8,12,15,7,11,12,12,9,8,11,12,7,9,10,15,9,5,10,15,11,8,5,13,10,7,12,13,14,9,5,9,8,7,7,7,6,9,13,11,5,5,10,6,10,10,6,7,9,11,11,7,5,9,7,8,10,5,10,10,6,6,6,10,6,11,10,15,9,7,5,12,7,4,5,9,5,11,6,8,9,13,9,8,9,11,11,12,4,5,8,9,8,4,9,15,9,6,11,8,2,9,5,10,8,9,7,9,4,10,10,11,9,14,12,8,3,8,8,6,8,8,7,5,9,8,4,10,7,7,7,11,6,7,8,4,9,3,4,5,3,9,10,5,12,8,8,6,9,6,8,6,11,7,8,5,3,8,4,12,7,7,8,9,12,7,7,3,8,12,9,5,6,3,3,4,4,10,7,10,5,8,7,7,5,8,6,12,5,3,6,7,5,6,4,8,2,1,3,6,5,4,5,8,8,8,5,6,6,6,6,7,3,5,5,3,4,3,6,6,4,4,6,10,6,2,9,7,4,2,11,4,7,6,7,6,8,6,10,6,13,2,6,5,6,5,5,9,7,8,4,7,2,4,9,4,9,3,4,4,9,5,8,4,5,4,3,8,11,9,6,6,7,4,11,4,4,6,4,6,5,6,2,2,4,6,4,3,4,5,9,3,5,3,4,5,6,5,9,5,7,0");
            StringTokenizer tokens = new StringTokenizer(fakeBackgroundDataString, " ,\r\n");
            fakeBackgroundData = new int [tokens.countTokens()];
            for (int i = 0; i < fakeBackgroundData.length; i ++)
                fakeBackgroundData[i] = Integer.parseInt(tokens.nextToken());
            
        }

        String channelsToZeroString = Config.getConfig().getProperty("Bridgeport.ChannelsToZero", "");
        StringTokenizer tokens = new StringTokenizer(channelsToZeroString, " ,\r\n");
        int count = tokens.countTokens();
        if (count > 0)
        {
            channelsToZero = new int [count];
            for (int i = 0; i < channelsToZero.length; i ++)
                channelsToZero[i] = Integer.parseInt(tokens.nextToken());
        }
    }

    /**
     * Reset background accumulation next time foreground spectra is processed.
     */
    public void resetBackground ()
    {
        m_ResetBackground = true;
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
     * Try to pair histogram and counts messages.  These messages are received
     * by the processor separately, but should be merged for logging.
     *
     * @return True if a pair was made
     */
    boolean tryPairData ()
    {
        if (lastCountRate == null || lastHistogram == null)
            return false;
        if (lastCountRateTimestamp < 0 || lastHistogramTimestamp < 0)
            return false;

        //Messages must have been received with a certain time threshold of each other to be paired
        if (Math.abs (lastCountRateTimestamp - lastHistogramTimestamp) > pairingSyncThresholdMs)
            return false;
        
        addToList(lastCountRate, lastHistogram);

        lastCountRate = null;
        lastHistogram = null;
        lastCountRateTimestamp = lastHistogramTimestamp = -1;
        return true;
    }
    
    private void addToList(GammaCountRates countRates, RNHistogram histogram)
    {
        spectraPair newPair = new spectraPair ();
        newPair.countRates = countRates;
        newPair.histogram = histogram;
        
        //Skip a number of initial messages because data seems to be corrupt during boot-up
        if (totalSpectraCollected++ >= initialSpectraSkip)
        {
            //Add data to foreground collection list
            synchronized (m_ForegroundSpectraLock)
            {
                m_ForegroundSpectra.add(newPair);
            }
        }
        else
        {
            System.out.println ("Bridgeport Processor ignoring initial spectra collection: " + totalSpectraCollected + " of " + initialSpectraSkip);
        }
    }
    
    /**
     * Receives incoming data bundle from the Bridgeport sensor over USB
     * that contains count rates and a histogram.
     * 
     * @param m the message from SensorDriver
     */
    void addUsbData(SensorDriverMessage m)
    {
        for (GammaDetectorMessage msg : m.getGammaDetectorMessageList())
        {
            GammaUsbCountRates rates = new GammaUsbCountRates();
            rates.setCoarseGain(msg.getCoarseGain());
            rates.setFineGain(msg.getFineGain());
            rates.setCountRate(msg.getCountRate());
            rates.setDeadTime(msg.getDeadTime());
            rates.setHighVoltage(msg.getHighVoltage());
            rates.setLiveTime(msg.getLiveTime());
            rates.setRealTime((float)msg.getRealTime()/1000); // Only number actually used by processor
            rates.setSampleTime(msg.getSampleTime());
            rates.setTotalCount(msg.getTotalCount());
            rates.setTemperature(m.getTemperature());
            rates.setTimestamp(m.getTimestamp());
            
            RNHistogram hist = new RNHistogram(msg.getSpectrum());
            
            if (channelsToZero != null)
            {
                for (int zeroChannel : channelsToZero)
                {
                    if (zeroChannel <= hist.getNumBins())
                    {
                        hist.setRawValue(zeroChannel, 0);
                    }
                }
            }

            addToList(rates, hist);
        }
    }

    /**
     * Receive incoming count rate data to the processor.  Store and try to pair with a histogram
     * before adding to processor
     * @param m Count rate data
     */
    void incomingData(GammaEthernetCountRates m) 
    {
        long currTimeMillis = System.currentTimeMillis();
        if (lastCountRate != null)
        {
            System.out.println ("Gamma count rate message not paired before new count rate message, dropping timestamp:" + lastCountRateTimestamp);
        }
        
        lastCountRate = new GammaEthernetCountRates (m);
        lastCountRateTimestamp = currTimeMillis;
        System.out.println ("Gamma count rate message received");

        if (!tryPairData ())
        {
            //No pairing, we'll store to try pairing later
            System.out.println ("Unable to pair gamma data, histTime=" + lastHistogramTimestamp + ", rateTime=" + lastCountRateTimestamp);
        }
    }

    /**
     * Receive incoming histogram data to the processor.  Store and try to pair with a count rate
     * data object before adding to processor
     * @param m Histogram data
     */
    void incomingData(RNHistogram m) {
        long currTimeMillis = System.currentTimeMillis();
        if (lastHistogram != null)
        {
            System.out.println ("Gamma Histogram message not paired before new histogram message, dropping timestamp:" + lastHistogramTimestamp);
        }
        
        lastHistogram = new RNHistogram (m);
        lastHistogramTimestamp = currTimeMillis;
        System.out.println ("Gamma Histogram message received");
        
        //Zero bins as requested in config file
        if (channelsToZero != null)
            {
                for (int zeroChannel : channelsToZero)
                {
                    if (zeroChannel <= lastHistogram.getNumBins())
                    {
                        lastHistogram.setRawValue(zeroChannel, 0);
                    }
                }
            }

        
        if (!tryPairData ())
        {
            //No pairing, we'll store to try pairing later
            System.out.println ("Unable to pair gamma data, histTime=" + lastHistogramTimestamp + ", rateTime=" + lastCountRateTimestamp);
        }
    }

    /**
     * Recieve calibration data, this currently has no use in the processor
     * @param m Calibration data
     */
    void incomingData(GammaCalibration m)
    {
        //Ignoring calibration message in processor for now

    }

    /**
     * Sum spectra in the foreground and background spectra lists into two separate spectra
     */

    private void sumSpectra ()
    {
        Arrays.fill (m_SumForegroundSpectraRaw, 0);
        Arrays.fill (m_SumBackgroundSpectraRaw, 0);
        m_ForegroundTotalCounts = 0;
        m_ForegroundTotalLiveTime = 0;
        m_BackgroundTotalLiveTime = 0;

        //Sum foreground
        for (int i = 0; i < m_ForegroundSpectraProcess.size(); i ++)
        {
            spectraPair currPair = m_ForegroundSpectraProcess.get (i);
            for (int j = 0; j < currPair.histogram.getNumBins (); j++)
            {
                m_SumForegroundSpectraRaw[j] += currPair.histogram.getRawValue (j);
                m_ForegroundTotalCounts += currPair.histogram.getRawValue (j);
                m_ForegroundTotalLiveTime += currPair.countRates.getRealTime();
            }
        }

        //Sum background
        for (int i = 0; i < m_BackgroundSpectra.size(); i ++)
        {
            spectraPair currPair = m_BackgroundSpectra.get (i);
            for (int j = 0; j < currPair.histogram.getNumBins (); j++)
            {
                m_SumBackgroundSpectraRaw[j] += currPair.histogram.getRawValue (j);
                m_BackgroundTotalLiveTime += currPair.countRates.getRealTime();
            }
        }
    }

    /**
     * Be sure a results log file is open and not too large for logging data
     *
     */
    private void ensureLogFile ()
    {
        //raw file
        if (m_CurrentLogFile == null || m_CurrentLogFile.length() > MAX_LOG_FILE_BYTES)
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
                logProcessingResultsHeader (m_LogStream);
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
     * Write header data showing what columns are logged with the processing results
     * @param logStream Stream to log header to
     * @throws Exception
     */
    private void logProcessingResultsHeader (DataOutputStream logStream) throws Exception
    {
        logStream.writeBytes ("Log Time\t");
        logStream.writeBytes ("Spectra Time\t");
        logStream.writeBytes ("a0\ta1\ta2\ta3\teLow\taetnaGain\t");

        logStream.writeBytes ("Foreground Spectra Count\t");
        logStream.writeBytes ("Foreground Live Time\t");
        logStream.writeBytes ("Background Live Time\t");

        logStream.writeBytes ("Background Spectra\t");
        for (int i = 0; i < m_SumBackgroundSpectraRaw.length-1; i ++)
            logStream.writeBytes ("\t");

        logStream.writeBytes ("Most Likely Isotope\t");
        logStream.writeBytes ("Most Likely Conc\t");

        logStream.writeBytes ("Classified Spectra\t");
        for (int i = 0; i < m_SumBackgroundSpectraRaw.length-1; i ++)
            logStream.writeBytes ("\t");

        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Write processing results to file
     *
     * @param logStream Stream to log results to
     * @param spectraTime Spectra time or received spectra, used to match up results with raw dat
     * @param result Processing results
     * @throws Exception
     */
    private void logProcessingResults (DataOutputStream logStream, long spectraTime, AETNAResult result) throws Exception
    {
        logStream.writeBytes (System.currentTimeMillis() + "\t");
        logStream.writeBytes (spectraTime + "\t");
        logStream.writeBytes (a0 + "\t" + a1 + "\t" + a2 + "\t" + a3 + "\t" + eLow + "\t" + aetnaGain + "\t");
        logStream.writeBytes (m_ForegroundSpectraProcess.size() + "\t");
        logStream.writeBytes (m_ForegroundTotalLiveTime + "\t");

        logStream.writeBytes (m_BackgroundTotalLiveTime + "\t");
        for (int i = 0; i < m_SumBackgroundSpectraRaw.length; i ++)
        {
            logStream.writeBytes (m_SumBackgroundSpectraRaw[i] + "\t");
        }

        String mostLikelyIsotope = "unknown";
        float mostLikelyIsotopeConc = 0.0f;
        if (result.isotopes.size() > 0)
        {
            mostLikelyIsotope = result.isotopes.get(0).isotope;
            mostLikelyIsotopeConc = result.isotopes.get(0).concentration;
        }
        logStream.writeBytes (mostLikelyIsotope + "\t");
        logStream.writeBytes(mostLikelyIsotopeConc + "\t");

        float array [] = result.estimated_spectrum;
        for (int i = 0; i < array.length; i ++)
        {
            logStream.writeBytes (array[i] + "\t");
        }

        logStream.writeBytes ("\r\n");
        logStream.flush();
    }

    /**
     * Add spectra to background spectra list
     *
     * @param toAdd Spectra to be added to background
     */
    private void addToBackground (Vector <spectraPair> toAdd)
    {
        int numToPop = Math.max (0, (m_BackgroundSpectra.size() + toAdd.size()) - maxBackgroundSpectraTime);

        //Ensure background list doesn't grow too large
        for (int i = 0; i < numToPop; i ++)
            m_BackgroundSpectra.pop();


        System.out.println ("Adding " + toAdd.size() + " spectra to background");
        for (int i = 0; i < toAdd.size(); i ++)
        {
            spectraPair newPair = toAdd.get(i);
            m_BackgroundSpectra.push(newPair);
        }

    }

    /**
     * Processing thread
     */
    @Override
    public void run ()
    {
        m_Running = true;

        //Load fake background data if desired
        if (useFakeBackgroundData)
        {
            RNHistogram newHist = new RNHistogram (fakeBackgroundData);
            GammaEthernetCountRates newRates = new GammaEthernetCountRates ();
            newRates.setRealTime(-13);
            spectraPair newPair = new spectraPair();
            newPair.histogram = newHist;
            newPair.countRates = newRates;
            Vector<spectraPair> toAdd = new Vector <spectraPair> ();
            toAdd.add(newPair);
            addToBackground(toAdd);
        }
        
        
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

                        //First crack at setting AENTA gain, will be changed later anyway
                        aetnaGain = 0;
                        if (!aetna.setClassifierParameters(aetnaClassifier, a0, a1, a2, a3, eLow, aetnaGain))
                            throw new Exception ("Could not set classifier ID for gamma processing");

                        //Send detection message reporting first spectra is being collected
                        bridgeportDetectionReportMessage newMessage = new bridgeportDetectionReportMessage ();
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
                    
                    m_BackgroundStartTime = System.currentTimeMillis();
                    m_ForegroundStartTime = System.currentTimeMillis();
                    
                    continue;
                }
                catch (IOException ex)
                {
                    Logger.getLogger(bridgeportProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println ("Unable to connect to AETNA service.");
                    m_aetnaConnected = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            //Processing begins
            synchronized (m_ForegroundSpectraLock)
            {   
                int duration = (int) (System.currentTimeMillis() - m_ForegroundStartTime);
                
                //Collect foreground data until required spectra count is reached
                if (duration/1000 >= maxForegroundSpectraTime)
                {
                    //Then move spectra to separate list to allow foreground to be collected still
                    m_ForegroundSpectraProcess = m_ForegroundSpectra;
                    m_ForegroundSpectra = new Vector <spectraPair> ();
                    m_ForegroundStartTime = System.currentTimeMillis();
                }
            }

            //When data is ready for processing...
            if (m_ForegroundSpectraProcess != null)
            {
                sumSpectra ();

                // If the user requests to reset the gathered background, then clear it.
                if (m_ResetBackground)
                {
                    m_BackgroundSpectra.clear();
                    
                    m_BackgroundStartTime = System.currentTimeMillis();

                    //Load fake background data if desired
                    if (useFakeBackgroundData)
                    {
                        RNHistogram newHist = new RNHistogram (fakeBackgroundData);
                        GammaEthernetCountRates newRates = new GammaEthernetCountRates ();
                        newRates.setRealTime(-13);
                        spectraPair newPair = new spectraPair();
                        newPair.histogram = newHist;
                        newPair.countRates = newRates;
                        Vector<spectraPair> toAdd = new Vector <spectraPair> ();
                        toAdd.add(newPair);
                        addToBackground(toAdd);
                    }
                    m_ResetBackground = false;
                }

                //have data to process
                if (useFakeBackgroundData)
                {
                    //background data should already be loaded, use it and don't bother checking size limits
                }
                else
                {
                    int duration = (int) (System.currentTimeMillis() - m_BackgroundStartTime);
                    
                    if (duration/1000 < maxBackgroundSpectraTime)
                    {
                        System.out.println ("Gamma background not accumulated enough, adding current foreground to background");
                        addToBackground(m_ForegroundSpectraProcess);
                        sumSpectra();
                        m_ForegroundSpectraProcess = null;

                        //While accumulating background data, send messages with histogram data and message reporting background is being accumulated
                        bridgeportDetectionReportMessage newMessage = new bridgeportDetectionReportMessage ();
                        long currTime = System.currentTimeMillis();
                        newMessage.setTimestampMs (currTime);
                        newMessage.addIsotope(accumBackgroundMsg, (100*(double)(duration/1000)/maxBackgroundSpectraTime));
                        m_Interface.insertMessage(newMessage);

                        bridgeportCompositeHistogramMessage newHistMsg = new bridgeportCompositeHistogramMessage();
                        newHistMsg.setTimestampMs(currTime);
                        newHistMsg.setSummedSpectra(m_SumBackgroundSpectraRaw);
                        newHistMsg.setNumSpectra(m_BackgroundSpectra.size());
                        newHistMsg.setTotalCounts (0);
                        newHistMsg.setLiveTime(m_BackgroundTotalLiveTime);
                        newHistMsg.setIsBackground(true);
                        m_Interface.insertMessage (newHistMsg);

                        continue;
                    }
                }

                long currTime = System.currentTimeMillis();
                //get/calculate/set appropriate settings for processing
                double newGain = aetna.autoGainBackground(m_SumBackgroundSpectraRaw);

                /*int backgroundCounts = 0;
                System.out.println ("Computing AETNA gain for background:");
                for (int i= 0; i < m_SumBackgroundSpectraRaw.length; i ++)
                {
                    System.out.print (m_SumBackgroundSpectraRaw[i] + ",");
                    backgroundCounts += m_SumBackgroundSpectraRaw[i];
                }
                System.out.println ("\nNew gain = " + newGain);
                System.out.println ("Total counts in background: " + backgroundCounts);
                System.out.println ("Total background over " + m_BackgroundTotalLiveTime/1000/60 + " minutes");*/
                
                aetnaGain = newGain;
                aetna.setClassifierParameters(aetnaClassifier, a0, a1, a2, a3, eLow, aetnaGain);
                
                try
                {
                    //adjust data and process it
                    float[] energyBinBoundaries = aetna.getEnergyBinBoundaries(aetnaClassifier);
                    m_SumForegroundSpectraRebinned = rebinChannels(m_SumForegroundSpectraRaw, energyBinBoundaries, a0, a1, a2, a3, eLow, aetnaGain);
                    m_SumBackgroundSpectraRebinned = rebinChannels(m_SumBackgroundSpectraRaw, energyBinBoundaries, a0, a1, a2, a3, eLow, aetnaGain);
                    AETNAResult result = aetna.classify(aetnaClassifier, m_SumForegroundSpectraRebinned, m_SumBackgroundSpectraRebinned);
                    
                    System.out.println("[bridgeportProcessor.run()]: Most likely isotope: " + result.mostLikelyIsotope);

                    /*int foregroundCounts = 0;
                    System.out.println ("Computing AETNA Result for foreground (raw):");
                    for (int i= 0; i < m_SumForegroundSpectraRaw.length; i ++)
                    {
                        System.out.print (m_SumForegroundSpectraRaw[i] + ",");
                        foregroundCounts += m_SumForegroundSpectraRaw[i];
                    }
                    System.out.println ("\nTotal counts in foreground: " + foregroundCounts);
                    System.out.println ("Total foreground over " + m_ForegroundTotalLiveTime/1000/60 + " minutes");*/
                   
                    
                    //if (!useFakeBackgroundData && mostLikelyIsotope.equals ("background"))     
                    //We'll use the same fake data over and over again if fake data provided

                    //if background data, add to background window
                    if (m_ForegroundTotalCounts >= m_MinExpectedBackgroundCounts && m_ForegroundTotalCounts <= m_MaxExpectedBackgroundCounts)
                    {
                        if (result.isotopes.isEmpty() || (result.isotopes.get(0).isotope.startsWith("background") && result.isotopes.get(0).concentration > 0.75))
                        {
                            addToBackground(m_ForegroundSpectraProcess);
                        }
                    }

                    //log
                    ensureLogFile ();
                    logProcessingResults (m_LogStream, currTime, result);

                    //send detection report
                    bridgeportDetectionReportMessage newMessage = new bridgeportDetectionReportMessage ();
                    newMessage.setTimestampMs (currTime);

                    System.out.println ("Isotope concentrations:" );
                    for (int i = 0; i < result.isotopes.size(); i ++)
                    {
                        AETNAResult.IsotopeConcentraion isotope = result.isotopes.get(i);
                        newMessage.addIsotope(isotope.isotope, isotope.concentration*100);
                        System.out.println (isotope.isotope + ": " + isotope.concentration);
                    }
                    m_Interface.insertMessage(newMessage);

                    //Send a single composite histogram message regardless of results
                    bridgeportCompositeHistogramMessage newHistMsg = new bridgeportCompositeHistogramMessage();
                    newHistMsg.setTimestampMs(currTime);
                    newHistMsg.setSummedSpectra(m_SumForegroundSpectraRaw);
                    newHistMsg.setClassifiedSpectra(result.estimated_spectrum);
                    newHistMsg.setNumSpectra(m_ForegroundSpectraProcess.size());
                    newHistMsg.setTotalCounts (m_ForegroundTotalCounts);
                    newHistMsg.setLiveTime(m_ForegroundTotalLiveTime);
                    
                    
                    
                    if (m_Interface != null)
	                {
	                    m_Interface.insertMessage(newHistMsg);
	                }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.err.println ("Could not process gamma data.");
                    m_ForegroundSpectraProcess = null;
                } 

                m_ForegroundSpectraProcess = null;
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
        
    }
    

    	/**
	 * Converts the given array from channel space to energy space, using the given binning structure and 
	 * calibration parameters
	 * @param array The channel data
	 * @param energyBins The binning structure @see edu.jhuapl.spectra.IAETNAServiceInterface#getEnergyBinBoundaries(java.lang.String)
	 * @param a0   
	 * @param a1
	 * @param a2
	 * @param a3
	 * @param eLow
	 * @param newGain The gain to be used.  This will change over time
	 * @return A new array that is in energy space
	 */
	public static float[] rebinChannels(
			float[] array,
			float[] energyBins,
			double a0,
			double a1,
			double a2,
			double a3,
			double eLow,
			double newGain)
	{
		double tempGain = 1D + newGain;
		double numChans = array.length;
		double x;

		float[] cal = null;
		float[] ret = null;
		double start, end;
		int startChan, endChan;
		double percent;
		float count;
		if (energyBins != null)
		{
			startChan = 0;
			endChan = 1;

			cal = new float[array.length];
			for (int i = 0; i < cal.length; i++)
			{
				x = (double) i / numChans;
				cal[i] = (float) (a0 + (a1 * x) + (a2 * x * x) + (a3 * x * x * x) + (eLow / (1D + (60D * x))));
				cal[i] *= tempGain;
			}

			ret = new float[energyBins.length - 1];
			for (int i = 0; i < ret.length; i++)
				ret[i] = 0;

			boolean done = false;

			for (int i = 0; i < ret.length - 1; i++)
			{
				start = energyBins[i];
				end = energyBins[i + 1];
				while (cal[startChan] < start)
				{
					startChan++;
					if (startChan >= cal.length)
					{
						done = true;
						startChan = cal.length - 1;
						break;
					}
				}

				while (cal[endChan] < end)
				{
					endChan++;
					if (endChan >= cal.length)
					{
						done = true;
						endChan = cal.length - 1;
						break;
					}
				}

				count = 0;
				for (int j = startChan; j < endChan; j++)
				{
					ret[i] += array[j];
					count++;
				}

				if (count == 0)
				{
					if (startChan > 0)
					{
						percent = (float) ((start - cal[startChan - 1]) / ((cal[startChan]) - cal[startChan - 1]));
						ret[i] += ((array[startChan] - array[startChan - 1]) * percent) + array[startChan - 1];
						count++;
					}

					if (endChan < energyBins.length)
					{
						percent = (float) ((end - cal[endChan - 1]) / ((cal[endChan]) - cal[endChan - 1]));
						ret[i] += ((array[endChan] - array[endChan - 1]) * percent) + array[endChan - 1];
						count++;
					}
				}

				//				System.out.println("i: " + i + " count: " + count);

				if (count != 0)
					ret[i] /= count;
				ret[i] = ret[i] < 0 ? 0 : ret[i];

				if (done)
					break;

				//				if(cal[startChan]>=start && cal[startChan]<end)
				//				{
				//					count = 0;
				//					for(int j = startChan; j<endChan; j++)
				//					{
				//						ret[i] += array[j];
				//						count++;
				//					}
				//					ret[i] /= count;
				//				}
				//				else if(startChan != 0 || (startChan == 0 && endChan != 0)) 
				//				{
				//					startChan = startChan==0?0:startChan-1;
				//					percent = (start-cal[startChan])/(cal[endChan]-cal[startChan]);
				//					ret[i] = (float)((array[endChan]-array[startChan])*percent)+array[startChan];
				//				}
				//				
				//				if(Float.isNaN(ret[i])) ret[i] = 0;
			}
		}

		//		System.out.println("testHist.length: " + ret.length);

		return ret;
	}

        public static void main(String args[])
        {
            AETNAServiceInterface2 aetna = null;
            try {
                aetna = new AETNAServiceInterface2();
            } catch (IOException ex) {
                Logger.getLogger(bridgeportProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }


            //0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0,1.0,2.0,1.0,0.0,2.0,5.0,2.0,2.0,1.0,5.0,4.0,3.0,2.0,6.0,4.0,2.0,2.0,3.0,7.0,6.0,3.0,3.0,2.0,4.0,7.0,2.0,3.0,4.0,5.0,6.0,1.0,1.0,3.0,3.0,1.0,2.0,2.0,4.0,6.0,2.0,8.0,2.0,3.0,5.0,6.0,4.0,4.0,2.0,4.0,6.0,3.0,5.0,4.0,7.0,5.0,4.0,5.0,3.0,4.0,7.0,8.0,10.0,8.0,9.0,7.0,6.0,5.0,8.0,8.0,9.0,10.0,15.0,8.0,9.0,10.0,9.0,8.0,9.0,7.0,15.0,10.0,13.0,7.0,9.0,4.0,12.0,11.0,16.0,8.0,13.0,11.0,15.0,8.0,12.0,9.0,8.0,10.0,10.0,17.0,18.0,13.0,13.0,9.0,13.0,9.0,15.0,15.0,8.0,14.0,15.0,23.0,15.0,19.0,14.0,18.0,9.0,10.0,18.0,11.0,8.0,16.0,12.0,16.0,15.0,16.0,17.0,12.0,11.0,13.0,16.0,15.0,7.0,17.0,16.0,14.0,22.0,13.0,9.0,20.0,17.0,12.0,16.0,17.0,15.0,18.0,13.0,14.0,24.0,18.0,21.0,15.0,19.0,21.0,15.0,13.0,22.0,10.0,17.0,14.0,18.0,21.0,15.0,20.0,15.0,20.0,14.0,10.0,14.0,17.0,18.0,12.0,10.0,12.0,11.0,12.0,19.0,15.0,17.0,12.0,16.0,15.0,9.0,11.0,13.0,19.0,16.0,17.0,14.0,21.0,13.0,23.0,20.0,16.0,14.0,13.0,11.0,14.0,13.0,12.0,16.0,11.0,19.0,11.0,10.0,12.0,11.0,9.0,18.0,19.0,10.0,13.0,16.0,10.0,15.0,17.0,15.0,20.0,11.0,13.0,13.0,15.0,22.0,11.0,8.0,15.0,5.0,9.0,16.0,13.0,8.0,13.0,13.0,13.0,14.0,18.0,9.0,14.0,14.0,12.0,15.0,12.0,12.0,10.0,8.0,12.0,12.0,6.0,11.0,12.0,12.0,10.0,11.0,6.0,10.0,15.0,12.0,9.0,14.0,7.0,9.0,9.0,11.0,9.0,13.0,7.0,6.0,11.0,14.0,10.0,12.0,11.0,9.0,8.0,9.0,10.0,14.0,9.0,12.0,13.0,6.0,10.0,12.0,10.0,9.0,9.0,8.0,7.0,11.0,9.0,8.0,14.0,9.0,6.0,10.0,12.0,12.0,11.0,9.0,15.0,11.0,4.0,9.0,7.0,4.0,5.0,13.0,11.0,12.0,8.0,11.0,10.0,10.0,8.0,5.0,6.0,9.0,9.0,9.0,5.0,10.0,10.0,5.0,5.0,3.0,14.0,9.0,6.0,6.0,5.0,9.0,12.0,11.0,12.0,9.0,14.0,7.0,8.0,5.0,12.0,12.0,9.0,9.0,12.0,8.0,5.0,17.0,8.0,10.0,10.0,9.0,4.0,7.0,9.0,8.0,10.0,7.0,5.0,3.0,14.0,11.0,8.0,11.0,9.0,4.0,12.0,9.0,10.0,9.0,7.0,12.0,11.0,5.0,3.0,12.0,7.0,13.0,13.0,6.0,7.0,6.0,7.0,10.0,5.0,7.0,11.0,3.0,7.0,4.0,3.0,10.0,9.0,13.0,11.0,5.0,8.0,6.0,4.0,7.0,5.0,9.0,0.0,7.0,8.0,5.0,2.0,8.0,3.0,4.0,7.0,5.0,2.0,7.0,5.0,4.0,2.0,4.0,4.0,8.0,6.0,4.0,7.0,4.0,5.0,5.0,5.0,6.0,5.0,2.0,2.0,6.0,4.0,3.0,2.0,3.0,6.0,2.0,0.0,1.0,4.0,10.0,4.0,6.0,3.0,6.0,4.0,7.0,5.0,4.0,3.0,3.0,4.0,2.0,4.0,3.0,2.0,4.0,3.0,4.0,8.0,8.0,5.0,4.0,4.0,7.0,4.0,3.0,3.0,5.0,7.0,8.0,2.0,2.0,6.0,4.0,4.0,3.0,6.0,3.0,7.0,4.0,3.0,5.0,7.0,3.0,4.0,5.0,4.0,3.0,4.0,1.0,5.0,1.0,4.0,6.0,5.0,4.0,0.0,5.0,1.0,5.0,5.0,2.0,1.0,5.0,2.0,4.0,1.0,0.0,1.0,4.0,2.0,3.0,0.0,3.0,2.0,3.0,4.0,3.0,3.0,1.0,4.0,4.0,0.0,1.0,4.0,2.0,4.0,3.0,3.0,1.0,1.0,6.0,2.0,6.0,3.0,5.0,1.0,5.0,2.0,5.0,2.0,1.0,2.0,5.0,2.0,3.0,10.0,3.0,3.0,1.0,1.0,6.0,3.0,4.0,1.0,0.0,3.0,3.0,2.0,1.0,1.0,3.0,0.0,1.0,3.0,4.0,0.0,5.0,3.0,2.0,3.0,1.0,3.0,1.0,5.0,2.0,2.0,1.0,3.0,4.0,4.0,5.0,2.0,3.0,0.0,0.0,3.0,2.0,10.0,4.0,3.0,4.0,1.0,9.0,1.0,1.0,1.0,1.0,5.0,2.0,2.0,2.0,1.0,5.0,2.0,2.0,1.0,2.0,3.0,3.0,2.0,2.0,0.0,5.0,3.0,1.0,1.0,2.0,1.0,3.0,2.0,3.0,2.0,2.0,2.0,1.0,1.0,1.0,3.0,3.0,1.0,2.0,3.0,4.0,0.0,3.0,1.0,1.0,0.0,1.0,1.0,1.0,2.0,1.0,0.0,2.0,1.0,2.0,1.0,1.0,1.0,2.0,3.0,2.0,1.0,1.0,3.0,3.0,4.0,0.0,1.0,2.0,1.0,3.0,1.0,2.0,2.0,2.0,1.0,1.0,2.0,0.0,1.0,1.0,1.0,0.0,2.0,4.0,0.0,4.0,0.0,2.0,1.0,1.0,1.0,0.0,1.0,1.0,0.0,1.0,1.0,3.0,2.0,1.0,2.0,0.0,0.0,5.0,0.0,1.0,3.0,1.0,0.0,0.0,3.0,3.0,2.0,2.0,1.0,1.0,0.0,0.0,1.0,1.0,2.0,1.0,5.0,1.0,0.0,2.0,1.0,2.0,1.0,1.0,3.0,4.0,2.0,2.0,0.0,0.0,5.0,2.0,2.0,0.0,1.0,1.0,2.0,2.0,0.0,0.0,3.0,0.0,2.0,1.0,1.0,2.0,0.0,1.0,3.0,1.0,0.0,4.0,2.0,2.0,3.0,2.0,2.0,2.0,2.0,1.0,0.0,0.0,1.0,0.0,1.0,1.0,2.0,0.0,0.0,1.0,0.0,2.0,4.0,0.0,1.0,3.0,1.0,1.0,2.0,2.0,0.0,0.0,2.0,0.0,1.0,4.0,1.0,1.0,0.0,1.0,0.0,1.0,1.0,5.0,1.0,1.0,2.0,1.0,2.0,0.0,1.0,1.0,2.0,0.0,1.0,1.0,1.0,1.0,1.0,1.0,0.0,2.0,1.0,1.0,2.0,0.0,1.0,2.0,1.0,1.0,1.0,1.0,0.0,0.0,0.0,1.0,2.0,0.0,2.0,1.0,2.0,1.0,0.0,1.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,2.0,1.0,2.0,1.0,2.0,0.0,0.0,0.0,1.0,0.0,2.0,3.0,1.0,0.0,1.0,0.0,0.0,0.0,1.0,0.0,2.0,0.0,1.0,0.0,1.0,0.0,0.0,3.0,2.0,4.0,3.0,0.0,2.0,0.0,4.0,0.0,1.0,1.0,2.0,0.0,2.0,1.0,1.0,1.0,1.0,3.0,0.0,1.0,1.0,1.0,2.0,1.0,0.0,0.0,1.0,1.0,0.0,2.0,1.0,2.0,1.0,1.0,1.0,1.0,1.0,1.0,0.0,1.0,2.0,2.0,0.0,2.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,0.0,0.0,1.0,1.0,1.0,0.0,1.0,0.0,0.0,2.0,1.0,0.0,0.0,1.0,1.0,0.0,0.0,1.0,0.0
            float m_SumBackgroundSpectra [] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,
                    0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,
                    0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,
                    1.0f,0.0f,1.0f,1.0f,2.0f,1.0f,0.0f,2.0f,5.0f,2.0f,2.0f,1.0f,5.0f,4.0f,3.0f,2.0f,6.0f,4.0f,2.0f,2.0f,3.0f,7.0f,6.0f,3.0f,
                    3.0f,2.0f,4.0f,7.0f,2.0f,3.0f,4.0f,5.0f,6.0f,1.0f,1.0f,3.0f,3.0f,1.0f,2.0f,2.0f,4.0f,6.0f,2.0f,8.0f,2.0f,3.0f,5.0f,6.0f,
                    4.0f,4.0f,2.0f,4.0f,6.0f,3.0f,5.0f,4.0f,7.0f,5.0f,4.0f,5.0f,3.0f,4.0f,7.0f,8.0f,10.0f,8.0f,9.0f,7.0f,6.0f,5.0f,8.0f,8.0f,
                    9.0f,10.0f,15.0f,8.0f,9.0f,10.0f,9.0f,8.0f,9.0f,7.0f,15.0f,10.0f,13.0f,7.0f,9.0f,4.0f,12.0f,11.0f,16.0f,8.0f,13.0f,11.0f,
                    15.0f,8.0f,12.0f,9.0f,8.0f,10.0f,10.0f,17.0f,18.0f,13.0f,13.0f,9.0f,13.0f,9.0f,15.0f,15.0f,8.0f,14.0f,15.0f,23.0f,15.0f,
                    19.0f,14.0f,18.0f,9.0f,10.0f,18.0f,11.0f,8.0f,16.0f,12.0f,16.0f,15.0f,16.0f,17.0f,12.0f,11.0f,13.0f,16.0f,15.0f,7.0f,
                    17.0f,16.0f,14.0f,22.0f,13.0f,9.0f,20.0f,17.0f,12.0f,16.0f,17.0f,15.0f,18.0f,13.0f,14.0f,24.0f,18.0f,21.0f,15.0f,19.0f,
                    21.0f,15.0f,13.0f,22.0f,10.0f,17.0f,14.0f,18.0f,21.0f,15.0f,20.0f,15.0f,20.0f,14.0f,10.0f,14.0f,17.0f,18.0f,12.0f,10.0f,
                    12.0f,11.0f,12.0f,19.0f,15.0f,17.0f,12.0f,16.0f,15.0f,9.0f,11.0f,13.0f,19.0f,16.0f,17.0f,14.0f,21.0f,13.0f,23.0f,20.0f,
                    16.0f,14.0f,13.0f,11.0f,14.0f,13.0f,12.0f,16.0f,11.0f,19.0f,11.0f,10.0f,12.0f,11.0f,9.0f,18.0f,19.0f,10.0f,13.0f,16.0f,
                    10.0f,15.0f,17.0f,15.0f,20.0f,11.0f,13.0f,13.0f,15.0f,22.0f,11.0f,8.0f,15.0f,5.0f,9.0f,16.0f,13.0f,8.0f,13.0f,13.0f,
                    13.0f,14.0f,18.0f,9.0f,14.0f,14.0f,12.0f,15.0f,12.0f,12.0f,10.0f,8.0f,12.0f,12.0f,6.0f,11.0f,12.0f,12.0f,10.0f,11.0f,
                    6.0f,10.0f,15.0f,12.0f,9.0f,14.0f,7.0f,9.0f,9.0f,11.0f,9.0f,13.0f,7.0f,6.0f,11.0f,14.0f,10.0f,12.0f,11.0f,9.0f,8.0f,9.0f,
                    10.0f,14.0f,9.0f,12.0f,13.0f,6.0f,10.0f,12.0f,10.0f,9.0f,9.0f,8.0f,7.0f,11.0f,9.0f,8.0f,14.0f,9.0f,6.0f,10.0f,12.0f,12.0f,
                    11.0f,9.0f,15.0f,11.0f,4.0f,9.0f,7.0f,4.0f,5.0f,13.0f,11.0f,12.0f,8.0f,11.0f,10.0f,10.0f,8.0f,5.0f,6.0f,9.0f,9.0f,9.0f,
                    5.0f,10.0f,10.0f,5.0f,5.0f,3.0f,14.0f,9.0f,6.0f,6.0f,5.0f,9.0f,12.0f,11.0f,12.0f,9.0f,14.0f,7.0f,8.0f,5.0f,12.0f,12.0f,
                    9.0f,9.0f,12.0f,8.0f,5.0f,17.0f,8.0f,10.0f,10.0f,9.0f,4.0f,7.0f,9.0f,8.0f,10.0f,7.0f,5.0f,3.0f,14.0f,11.0f,8.0f,11.0f,
                    9.0f,4.0f,12.0f,9.0f,10.0f,9.0f,7.0f,12.0f,11.0f,5.0f,3.0f,12.0f,7.0f,13.0f,13.0f,6.0f,7.0f,6.0f,7.0f,10.0f,5.0f,7.0f,
                    11.0f,3.0f,7.0f,4.0f,3.0f,10.0f,9.0f,13.0f,11.0f,5.0f,8.0f,6.0f,4.0f,7.0f,5.0f,9.0f,0.0f,7.0f,8.0f,5.0f,2.0f,8.0f,3.0f,
                    4.0f,7.0f,5.0f,2.0f,7.0f,5.0f,4.0f,2.0f,4.0f,4.0f,8.0f,6.0f,4.0f,7.0f,4.0f,5.0f,5.0f,5.0f,6.0f,5.0f,2.0f,2.0f,6.0f,4.0f,
                    3.0f,2.0f,3.0f,6.0f,2.0f,0.0f,1.0f,4.0f,10.0f,4.0f,6.0f,3.0f,6.0f,4.0f,7.0f,5.0f,4.0f,3.0f,3.0f,4.0f,2.0f,4.0f,3.0f,2.0f,
                    4.0f,3.0f,4.0f,8.0f,8.0f,5.0f,4.0f,4.0f,7.0f,4.0f,3.0f,3.0f,5.0f,7.0f,8.0f,2.0f,2.0f,6.0f,4.0f,4.0f,3.0f,6.0f,3.0f,7.0f,
                    4.0f,3.0f,5.0f,7.0f,3.0f,4.0f,5.0f,4.0f,3.0f,4.0f,1.0f,5.0f,1.0f,4.0f,6.0f,5.0f,4.0f,0.0f,5.0f,1.0f,5.0f,5.0f,2.0f,1.0f,
                    5.0f,2.0f,4.0f,1.0f,0.0f,1.0f,4.0f,2.0f,3.0f,0.0f,3.0f,2.0f,3.0f,4.0f,3.0f,3.0f,1.0f,4.0f,4.0f,0.0f,1.0f,4.0f,2.0f,4.0f,
                    3.0f,3.0f,1.0f,1.0f,6.0f,2.0f,6.0f,3.0f,5.0f,1.0f,5.0f,2.0f,5.0f,2.0f,1.0f,2.0f,5.0f,2.0f,3.0f,10.0f,3.0f,3.0f,1.0f,1.0f,
                    6.0f,3.0f,4.0f,1.0f,0.0f,3.0f,3.0f,2.0f,1.0f,1.0f,3.0f,0.0f,1.0f,3.0f,4.0f,0.0f,5.0f,3.0f,2.0f,3.0f,1.0f,3.0f,1.0f,5.0f,
                    2.0f,2.0f,1.0f,3.0f,4.0f,4.0f,5.0f,2.0f,3.0f,0.0f,0.0f,3.0f,2.0f,10.0f,4.0f,3.0f,4.0f,1.0f,9.0f,1.0f,1.0f,1.0f,1.0f,5.0f,
                    2.0f,2.0f,2.0f,1.0f,5.0f,2.0f,2.0f,1.0f,2.0f,3.0f,3.0f,2.0f,2.0f,0.0f,5.0f,3.0f,1.0f,1.0f,2.0f,1.0f,3.0f,2.0f,3.0f,2.0f,
                    2.0f,2.0f,1.0f,1.0f,1.0f,3.0f,3.0f,1.0f,2.0f,3.0f,4.0f,0.0f,3.0f,1.0f,1.0f,0.0f,1.0f,1.0f,1.0f,2.0f,1.0f,0.0f,2.0f,1.0f,
                    2.0f,1.0f,1.0f,1.0f,2.0f,3.0f,2.0f,1.0f,1.0f,3.0f,3.0f,4.0f,0.0f,1.0f,2.0f,1.0f,3.0f,1.0f,2.0f,2.0f,2.0f,1.0f,1.0f,2.0f,
                    0.0f,1.0f,1.0f,1.0f,0.0f,2.0f,4.0f,0.0f,4.0f,0.0f,2.0f,1.0f,1.0f,1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,1.0f,3.0f,2.0f,1.0f,2.0f,
                    0.0f,0.0f,5.0f,0.0f,1.0f,3.0f,1.0f,0.0f,0.0f,3.0f,3.0f,2.0f,2.0f,1.0f,1.0f,0.0f,0.0f,1.0f,1.0f,2.0f,1.0f,5.0f,1.0f,0.0f,
                    2.0f,1.0f,2.0f,1.0f,1.0f,3.0f,4.0f,2.0f,2.0f,0.0f,0.0f,5.0f,2.0f,2.0f,0.0f,1.0f,1.0f,2.0f,2.0f,0.0f,0.0f,3.0f,0.0f,2.0f,
                    1.0f,1.0f,2.0f,0.0f,1.0f,3.0f,1.0f,0.0f,4.0f,2.0f,2.0f,3.0f,2.0f,2.0f,2.0f,2.0f,1.0f,0.0f,0.0f,1.0f,0.0f,1.0f,1.0f,2.0f,
                    0.0f,0.0f,1.0f,0.0f,2.0f,4.0f,0.0f,1.0f,3.0f,1.0f,1.0f,2.0f,2.0f,0.0f,0.0f,2.0f,0.0f,1.0f,4.0f,1.0f,1.0f,0.0f,1.0f,0.0f,
                    1.0f,1.0f,5.0f,1.0f,1.0f,2.0f,1.0f,2.0f,0.0f,1.0f,1.0f,2.0f,0.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,0.0f,2.0f,1.0f,1.0f,2.0f,
                    0.0f,1.0f,2.0f,1.0f,1.0f,1.0f,1.0f,0.0f,0.0f,0.0f,1.0f,2.0f,0.0f,2.0f,1.0f,2.0f,1.0f,0.0f,1.0f,0.0f,0.0f,2.0f,0.0f,0.0f,
                    0.0f,0.0f,2.0f,1.0f,2.0f,1.0f,2.0f,0.0f,0.0f,0.0f,1.0f,0.0f,2.0f,3.0f,1.0f,0.0f,1.0f,0.0f,0.0f,0.0f,1.0f,0.0f,2.0f,0.0f,
                    1.0f,0.0f,1.0f,0.0f,0.0f,3.0f,2.0f,4.0f,3.0f,0.0f,2.0f,0.0f,4.0f,0.0f,1.0f,1.0f,2.0f,0.0f,2.0f,1.0f,1.0f,1.0f,1.0f,3.0f,
                    0.0f,1.0f,1.0f,1.0f,2.0f,1.0f,0.0f,0.0f,1.0f,1.0f,0.0f,2.0f,1.0f,2.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,0.0f,1.0f,2.0f,2.0f,
                    0.0f,2.0f,0.0f,1.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,
                    1.0f,0.0f,0.0f,0.0f,1.0f,1.0f,1.0f,0.0f,1.0f,0.0f,0.0f,2.0f,1.0f,0.0f,0.0f,1.0f,1.0f,0.0f,0.0f,1.0f,0.0f};


            String aetnaClassifier;
            double a0;
            double a1;
            double a2;
            double a3;
            double eLow;
            double aetnaGain;




            a0 = -1.88289;
            a1 = 3189.84692;
            a2 = 1220.75110;
            a3 = -637.52142;
            eLow = 0.00000;
            aetnaClassifier = "-backpack-d50738";
            aetnaGain = 0;


            if (!aetna.setClassifierParameters(aetnaClassifier, a0, a1, a2, a3, eLow, aetnaGain))
                System.out.println ("Error setting classifier parameters");


            double newGain = aetna.autoGainBackground(m_SumBackgroundSpectra);


            System.out.println ("Computing AETNA gain for background:");
            for (int i= 0; i < m_SumBackgroundSpectra.length; i ++)
            {
                System.out.print (m_SumBackgroundSpectra[i] + "\n");
            }
            System.out.println ("\nNew gain = " + newGain);

            aetnaGain = newGain;
            aetna.setClassifierParameters(aetnaClassifier, a0, a1, a2, a3, eLow, aetnaGain);
        }
}
