/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Histogram of RN data
 *
 * @author humphjc1
 */
public class RNHistogram
{
    private final int LOG_HISTOGRAM_VERSION = 0;
    /**
     * Histogram data
     */
    private int[] m_RawData;
    
    /**
     * Classified data. Has same bins as raw data.
     */
    private int[] m_ClassifiedData;

    /**
     * Number of bins in histogram
     */
    private int m_NumBins;


    /**
     * Create a histogram with given raw data
     * @param raw data Histogram data
     */
    public RNHistogram(int[] rawData)
    {
        m_NumBins = rawData.length;
        m_RawData = rawData.clone();
    }
    
    /**
     * Create a histogram with given raw and classified data
     * @param raw data Histogram
     * @param classified data Histogram
     */
    public RNHistogram(int[] rawData, int[] classifiedData)
    {
        m_NumBins = rawData.length;
        m_RawData = rawData.clone();
        m_ClassifiedData = classifiedData.clone();
    }
    
    /**
     * Create a histogram with length of 1
     */
    public RNHistogram()
    {
    	this(1);
    }

    /**
     * Create a histogram with given length
     * @param binCount
     */
    public RNHistogram(int binCount)
    {
        m_NumBins = binCount;
        m_RawData = new int[m_NumBins];
        m_ClassifiedData = new int[m_NumBins];

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_RawData[i] = 0;
            m_ClassifiedData[i] = 0;
        }
    }

    /**
     * Create a histogram as a copy of the parameter
     * @param oldCopy
     */
    public RNHistogram(RNHistogram oldCopy)
    {
        m_NumBins = oldCopy.getNumBins();
        m_RawData = new int[m_NumBins];
        m_ClassifiedData = new int[m_NumBins];

        copyFrom(oldCopy);
    }

    /**
     * Create a new histogram as a copy of this one
     * @return Copy of this histogram
     */
    public RNHistogram getCopyOfHistogram ()
    {
        RNHistogram retVal = new RNHistogram(this);
        return retVal;
    }

    /**
	 * @return the m_RawData
	 */
	public int[] getRawData()
	{
		return m_RawData;
	}

	/**
	 * @param m_RawData the m_RawData to set
	 */
	public void setRawData(int[] m_RawData)
	{
		this.m_RawData = m_RawData;
	}
	
    /**
	 * @return the m_ClassifiedData
	 */
	public int[] getClassifiedData()
	{
		return m_ClassifiedData;
	}

	/**
	 * @param classifiedData the m_ClassifiedData to set
	 */
	public void setClassifiedData(int[] classifiedData)
	{
		this.m_ClassifiedData = classifiedData;
	}

	/**
     * Clear data from raw and classified histogram bins
     */
    public void clear ()
    {
        Arrays.fill(m_RawData, 0);
        Arrays.fill(m_ClassifiedData, 0);
    }

    /**
     * Return a byte array with histogram data for logging
     * @return
     */
    public byte[] toLogBytes ()
    {
        ByteBuffer retBuf = ByteBuffer.allocate(m_NumBins + 2*4);
        retBuf.order(ByteOrder.BIG_ENDIAN);

        retBuf.putInt(LOG_HISTOGRAM_VERSION);
        retBuf.putInt(m_NumBins);
        for (int i = 0; i < m_NumBins; i ++)
            retBuf.put ((byte)m_RawData[i]);
        return retBuf.array();
    }

    /**
     * Return a readable string with histogram data for logging
     * @return
     */
    public String toLogString ()
    {
        String retMsg = "";
        
        retMsg += m_NumBins + ";";
        for (int i = 0; i < m_NumBins; i ++)
            retMsg += m_RawData[i] + ",";
        
        return retMsg;
    }

    /**
     * Write histogram data to byte stream
     *
     * @param output Stream to write to
     * @throws IOException
     */
    public void writeToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_HISTOGRAM_VERSION);
        output.writeInt(m_NumBins);

        for (int i = 0; i < m_NumBins; i++) 
        {
            output.writeInt(m_RawData[i]);
        }
    }

    /**
     * Read histogram data from byte stream, must have been written/formatted by 'writeToStream'
     *
     * @param input Stream to read from
     * @throws IOException
     */
    public void readFromStream(DataInputStream input) throws IOException 
    {
        int version = input.readInt();
        m_NumBins = input.readInt();
        if (m_RawData == null || m_RawData.length != m_NumBins) 
        {
            m_RawData = new int[m_NumBins];
        }

        for (int i = 0; i < m_NumBins; i++) {
            m_RawData[i] = input.readInt();
        }
    }

    /**
     * Accessor for number of bins in histogram
     *
     * @return
     */
    public int getNumBins() 
    {
        return m_NumBins;
    }

    /**
     * Accessor for raw histogram bin index
     *
     * @param binIdx Index of histogram to access
     * @return
     */
    public int getRawValue(int binIdx) 
    {
        try 
        {
            return m_RawData[binIdx];
        } 
        catch (Exception e) 
        {
            System.err.println("Bad bin index value in GammaHistogram: " + binIdx);
            return -1;
        }
    }

    /**
     * Modifier for raw histogram bin index
     *
     * @param binIdx Index of histogram to modify
     * @param newValue New value for histogram at binIdx
     * @throws IndexOutOfBoundsException
     */
    public void setRawValue(int binIdx, int newValue) throws IndexOutOfBoundsException
    {
        m_RawData[binIdx] = newValue;
    }

    /**
     * Adds data from parameter histogram to the raw histogram.  Adds, does not overwrite.
     *
     * @param hist Histogram to add to this one
     * @return
     */
    public boolean addFrom(RNHistogram hist)
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't add different sized gamma histograms");
            return false;
        }
        
        for (int i = 0; i < m_NumBins; i++) 
        {
            m_RawData[i] += hist.m_RawData[i];
        }

        return true;
    }

    /**
     * Copies raw and classified (if any) data from parameter histogram to this histogram.
     *
     * @param hist Histogram to copy into this one
     * @return False if bin counts don't match
     */
    public boolean copyFrom(RNHistogram hist)
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't copy different sized gamma histograms");
            return false;
        }

        if(hist.m_ClassifiedData.length > 0)
        {
        	for (int i = 0; i < m_NumBins; i++) 
	        {
	            m_RawData[i] = hist.m_RawData[i];
	            m_ClassifiedData[i] = hist.m_ClassifiedData[i];
	        }
        }
        else
        {
	        for (int i = 0; i < m_NumBins; i++) 
	        {
	            m_RawData[i] = hist.m_RawData[i];
	        }
        }

        return true;
    }


    /**
     * Subtracts data from parameter histogram to the raw histogram.  Subtracts, does not overwrite
     *
     * @param hist Histogram to subtract from this one
     * @return
     */
    public boolean subtractFrom(RNHistogram hist)
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't subtract different sized gamma histograms");
            return false;
        }

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_RawData[i] -= hist.m_RawData[i];
        }

        return true;
    }

    /**
     * Write raw histogram data to flat file, readable format
     *
     * @param writer File writer to write flat file
     * @throws IOException
     */
    public void writeToFlatFile(BufferedWriter writer) throws IOException 
    {
        String msg = "";
        for (int i = 0; i < m_NumBins; i++) 
        {
            msg += m_RawData[i] + "\t";
        }
        
        writer.write(msg);
    }

    /**
     * Write column header data for flat file
     *
     * @param writer File writer to write data to
     * @throws IOException
     */
    public static void writeFlatFileHeader(BufferedWriter writer) throws IOException 
    {
        String msg = "Spectra\t";
        writer.write(msg);
    }
}
