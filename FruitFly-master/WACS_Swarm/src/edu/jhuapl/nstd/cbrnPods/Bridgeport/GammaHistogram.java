/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author humphjc1
 */
public class GammaHistogram 
{
    private final int LOG_HISTOGRAM_VERSION = 0;
    
    
    /**
     * Histogram data
     */
    private int[] m_Bins;

    /**
     * Number of bins in histogram
     */
    private int m_NumBins;

    public GammaHistogram() 
    {
        m_NumBins = 1;
        m_Bins = new int[m_NumBins];

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_Bins[i] = 0;
        }
    }

    public GammaHistogram(int binCount) 
    {
        m_NumBins = binCount;
        m_Bins = new int[m_NumBins];

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_Bins[i] = 0;
        }
    }

    public GammaHistogram(GammaHistogram oldCopy) 
    {
        m_NumBins = oldCopy.getNumBins();
        m_Bins = new int[m_NumBins];

        copyFrom(oldCopy);
    }

    public GammaHistogram getCopyOfHistogram ()
    {
        GammaHistogram retVal = new GammaHistogram(this);
        return retVal;
    }

    public byte[] toLogBytes ()
    {
        /*String retMsg = "";

        retMsg += LOG_HISTOGRAM_VERSION + ":";
        retMsg += m_NumBins + ";";
        for (int i = 0; i < m_NumBins; i ++)
            retMsg += (byte)m_Bins[i] + ",";

        return retMsg.getBytes();*/

        ByteBuffer retBuf = ByteBuffer.allocate(m_NumBins + 2*4);
        retBuf.order(ByteOrder.BIG_ENDIAN);

        retBuf.putInt(LOG_HISTOGRAM_VERSION);
        retBuf.putInt(m_NumBins);
        for (int i = 0; i < m_NumBins; i ++)
            retBuf.put ((byte)m_Bins[i]);
        return retBuf.array();
    }
    
    public String toLogString ()
    {
        String retMsg = "";
        
        retMsg += m_NumBins + ";";
        for (int i = 0; i < m_NumBins; i ++)
            retMsg += m_Bins[i] + ",";
        
        return retMsg;
    }

    public void writeToStream(DataOutputStream output) throws IOException 
    {
        output.writeInt(LOG_HISTOGRAM_VERSION);
        output.writeInt(m_NumBins);

        for (int i = 0; i < m_NumBins; i++) 
        {
            output.writeInt(m_Bins[i]);
        }
    }

    public void readFromStream(DataInputStream input) throws IOException 
    {
        int version = input.readInt();
        m_NumBins = input.readInt();
        if (m_Bins == null || m_Bins.length != m_NumBins) 
        {
            m_Bins = new int[m_NumBins];
        }

        for (int i = 0; i < m_NumBins; i++) {
            m_Bins[i] = input.readInt();
        }
    }

    public int getNumBins() 
    {
        return m_NumBins;
    }

    public int getValue(int binIdx) 
    {
        try 
        {
            return m_Bins[binIdx];
        } 
        catch (Exception e) 
        {
            System.err.println("Bad bin index value in GammaHistogram: " + binIdx);
            return -1;
        }
    }

    public void setValue(int binIdx, int newValue) throws Exception 
    {
        m_Bins[binIdx] = newValue;
    }

    public boolean addFrom(GammaHistogram hist) 
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't add different sized gamma histograms");
            return false;
        }

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_Bins[i] += hist.m_Bins[i];
        }

        return true;
    }

    public boolean copyFrom(GammaHistogram hist) 
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't copy different sized gamma histograms");
            return false;
        }

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_Bins[i] = hist.m_Bins[i];
        }

        return true;
    }

    public boolean subtractFrom(GammaHistogram hist) 
    {
        if (hist.m_NumBins != m_NumBins) 
        {
            System.err.println("Can't subtract different sized gamma histograms");
            return false;
        }

        for (int i = 0; i < m_NumBins; i++) 
        {
            m_Bins[i] -= hist.m_Bins[i];
        }

        return true;
    }

    public void writeToFlatFile(BufferedWriter writer) throws IOException 
    {
        String msg = "";
        for (int i = 0; i < m_NumBins; i++) 
        {
            msg += m_Bins[i] + "\t";
        }
        
        writer.write(msg);
    }
    
    public static void writeFlatFileHeader(BufferedWriter writer) throws IOException 
    {
        String msg = "Spectra";
        writer.write(msg);
    }
}
