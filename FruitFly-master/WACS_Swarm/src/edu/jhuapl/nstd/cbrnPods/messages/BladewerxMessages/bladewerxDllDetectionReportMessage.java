package edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages;

import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import edu.jhuapl.nstd.cbrnPods.messages.BladewerxMessages.bladewerxDllDetectionReportMessage.bladewerxIDResults.bladewerxPeakData;

import java.util.Vector;

/**
 *
 * @author stipeja1
 */
public class bladewerxDllDetectionReportMessage extends cbrnPodMsg
{        
    /**
     * Data for results of each isotope search
     */
    public class bladewerxIDResults
    {
        /**
         * Name of isotope looking for
         */
        public String m_IsotopeName;
        
        /**
         * Goodness of fit of spectra to peaks
         */
        public double m_GoodnessOfFit;
        
        /**
         * Goodness of fit for background spectra
         */
        public double m_BkgndGoF;

        /**
         * Calculated "confidence" value that this isotope is present
         */
        public double m_Confidence;
        
        /**
         * Elapsed time in processing
         */
        public double m_ElapsedTime;
        
        /**
         * Fast option used in processing
         */
        public boolean m_FastOption;
        
        /**
         * Peaks defined
         */
        public class bladewerxPeakData
        {
            /**
             * Channel of peak
             */
            public int m_Channel;
            
            /**
             * Whether m_Channel is a reference
             */
            public boolean m_Reference;
            
            /**
             * Counts under peak
             */
            public float m_PeakCounts;
            
            /**
             * Variance of peak
             */
            public float m_PeakVariance;
            
            /**
             * Fitted channel of peak
             */
            public float m_PeakChannel;
            
            /**
             * Default constructor
             */
            public bladewerxPeakData ()
            {
                this.m_Channel = 0;
                this.m_PeakChannel = 0;
                this.m_PeakCounts = 0;
                this.m_PeakVariance = 0;
                this.m_Reference = false;
            }
            
            /**
             * Copy constructor
             * @param copy
             */
            public bladewerxPeakData (bladewerxPeakData copy)
            {
                this.m_Channel = copy.m_Channel;
                this.m_PeakChannel = copy.m_PeakChannel;
                this.m_PeakCounts = copy.m_PeakCounts;
                this.m_PeakVariance = copy.m_PeakVariance;
                this.m_Reference = copy.m_Reference;
            }
        }
        
        /**
         * Peak data for peaks defined for this isotope
         */
        public bladewerxPeakData m_PeakData [];
        
        
        /**
         * Default constructor
         */
        public bladewerxIDResults ()
        {
            this.m_IsotopeName = "N/A";
            this.m_GoodnessOfFit = 0;
            this.m_BkgndGoF = 0;
            this.m_Confidence = 0;
            this.m_ElapsedTime = 0;
            this.m_FastOption = false;
            
            this.m_PeakData = new bladewerxPeakData [0];
        }
        
        /**
         * Copy Constructor
         * @param copy
         */
        public bladewerxIDResults (bladewerxIDResults copy)
        {
            this.m_IsotopeName = copy.m_IsotopeName;
            this.m_GoodnessOfFit = copy.m_GoodnessOfFit;
            this.m_BkgndGoF = copy.m_BkgndGoF;
            this.m_Confidence = copy.m_Confidence;
            this.m_ElapsedTime = copy.m_ElapsedTime;
            this.m_FastOption = copy.m_FastOption;
            
            this.m_PeakData = new bladewerxPeakData [copy.m_PeakData.length];
            for (int i = 0; i < m_PeakData.length; i ++)
            {
                m_PeakData[i] = new bladewerxPeakData (copy.m_PeakData[i]);
            }
        }
        
        public void addPeakData (int idx, int channel, boolean reference, float peakCounts, float peakVariance, float peakChannel)
        {
            bladewerxPeakData newPeak = new bladewerxPeakData ();
            newPeak.m_Channel = channel;
            newPeak.m_PeakChannel = peakChannel;
            newPeak.m_PeakCounts = peakCounts;
            newPeak.m_PeakVariance = peakVariance;
            newPeak.m_Reference = reference;
            
            m_PeakData[idx] = newPeak;
        }
    }
    
    /**
     * Results of each processing of isotopes
     */
    Vector <bladewerxIDResults> m_IdResults;
    
    
    
    public bladewerxDllDetectionReportMessage() 
    {
        super(cbrnSensorTypes.SENSOR_BLADEWERX, cbrnPodMsg.BLADEWERX_DLL_DETECTION_REPORT, 0);
        clear();
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m) 
    {
        bladewerxDllDetectionReportMessage msg = (bladewerxDllDetectionReportMessage) m;
        
        this.timestampMs = msg.timestampMs;
        this.m_IdResults = new Vector <bladewerxIDResults> ();
        for (int i = 0; i < msg.m_IdResults.size(); i ++)
            this.m_IdResults.add (new bladewerxIDResults (msg.m_IdResults.elementAt(i)));
        
        this.messageType = msg.messageType;
        this.validMessage = true;
    }
    
    private void clear ()
    {
        timestampMs = 0;
        if (m_IdResults == null)
            m_IdResults = new Vector <bladewerxIDResults> ();
    }
    
    public void addIdResult (String isotopeName, double goodnessOfFit, double bkgndGoF, double elapsedTime, boolean fastOption,
                        int channel[], boolean reference[], float peakCounts[], float peakVariance[], float peakChannel[])
    {
        bladewerxIDResults newResult = new bladewerxIDResults ();
        newResult.m_IsotopeName = isotopeName;
        newResult.m_FastOption = fastOption;
        newResult.m_GoodnessOfFit = goodnessOfFit;
        newResult.m_ElapsedTime = elapsedTime;
        newResult.m_BkgndGoF = bkgndGoF;

        double ratio = newResult.m_BkgndGoF/newResult.m_GoodnessOfFit;
        if (ratio <= 1)
            newResult.m_Confidence = 0;
        else if (newResult.m_GoodnessOfFit < 1E-20)
            newResult.m_Confidence = 0;
        else
            newResult.m_Confidence = (((double)ratio)/(1+ratio)-0.5)*2;

        int numPeaks = Math.min(channel.length, Math.min (reference.length, Math.min(peakCounts.length, Math.min(peakVariance.length, peakChannel.length))));
        newResult.m_PeakData = new bladewerxPeakData [numPeaks];
        for (int i = 0; i < newResult.m_PeakData.length; i ++)
            newResult.addPeakData (i, channel[i], reference[i], peakCounts[i], peakVariance[i], peakChannel[i]);   
        
        m_IdResults.add(newResult);
    }
    
    /**
     * Accessor for number of IdResult objects
     * @return
     */
    public int getNumIdResults () 
    {
        return m_IdResults.size();
    }
    
    public bladewerxIDResults returnIdResult (int idx)
    {
        if (idx < m_IdResults.size())
            return m_IdResults.elementAt(idx);
        
        return null;
    }
}