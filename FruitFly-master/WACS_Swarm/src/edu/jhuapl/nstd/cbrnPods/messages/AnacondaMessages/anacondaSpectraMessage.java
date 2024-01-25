/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages;

import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;

/**
 *
 * @author humphjc1
 */
public class anacondaSpectraMessage extends cbrnPodMsg 
{
    long anacondaTimestamp;
    
    private final static int NUM_CHANNELS = 1024;
    short spectraData[] = new short [NUM_CHANNELS]; 
    
    
    public anacondaSpectraMessage(int sensorType, int messageType, int dataSize)
    {
        super(sensorType, messageType, dataSize);
    }

    @Override
    public void parseBioPodMessage(cbrnPodMsg m)
    {
        super.parseBioPodMessage(m);
        super.setReadIndex(0);

        timestampMs = 1000*readDataUnsignedInt();
        anacondaTimestamp = readDataUnsignedInt();
        
        Index = 0;           //takes values 0..15
        Reconstruction = 0;  //must be 32 bit to avoid 16 bit overflow
        
        for (int i = 0; i < NUM_CHANNELS/2; i ++)
        {
            //get nibble data, each byte representing 2 channels
            byte value = (byte)readDataByte();
            
            byte lowNibble = (byte)(value & (0x0F));
            byte highNibble = (byte)((value & (0xF0)) >> 4);
            
            spectraData[i*2] = ADPCM_Decode ((char)highNibble);
            spectraData[i*2+1] = ADPCM_Decode ((char)lowNibble);
                    
        }
        
    }
    
    public void setAnacondaTimestamp (long newAnacondaTimestamp)
    {
        anacondaTimestamp = newAnacondaTimestamp;
    }
    
    public long getAnacondaTimestamp ()
    {
        return anacondaTimestamp;
    }
    
    public short[] accessChannelData ()
    {
        return spectraData;
    }

    public int[] getSpectraData()
    {
        int[] retval = new int[spectraData.length];

        for (int i=0; i<spectraData.length; i++)
            retval[i] = (int) spectraData[i];
        
        return retval;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private final static int N_STEP_VALUES = 89;
    private final static int StepTable[] = {
          7,8,9,10,11,12,13,14,16,17,
          19,21,23,25,28,31,34,37,41,45,
          50,55,60,66,73,80,88,97,107,118,
          130,143,157,173,190,209,230,253,279,307,
          337,371,408,449,494,544,598,658,724,796,
          876,963,1060,1166,1282,1411,1552,1707,1878,2066,
          2272,2499,2749,3024,3327,3660,4026,4428,4871,5358,
          5894,6484,7132,7845,8630,9493,10442,11487,12635,13899,
          15289,16818,18500,20350,22385,24623,27086,29794,32767
        };

    private final static int IndexAdjustTable[] = {-1,-1,-1,-1,2,4,6,8,-1,-1,-1,-1,2,4,6,8};

    static int Index = 0;           //takes values 0..15
    static int Reconstruction = 0;  //must be 32 bit to avoid 16 bit overflow
    short ADPCM_Decode(char Nibble)
    {
        int StepValue = 0;              //takes values 7..32767
        int Delta = 0;                  //must be 32 bit to avoid 16 bit overflow

        //load step value based on current index
        StepValue = StepTable[Index];

        //initialise delta as half nibble bit weight
        Delta = StepValue >> 3;

        //conditionally add step weight
        if ((Nibble & 4) != 0)
        {
            Delta = Delta + StepValue;
        }

        if ((Nibble & 2) != 0)
        {
            Delta = Delta + (StepValue >> 1);
        }

        if ((Nibble & 1) != 0)
        {
            Delta = Delta + (StepValue >> 2);
        }

        //test sign bit and subtract or add delta value
        if ((Nibble & 8) != 0)
        {
            Reconstruction = Reconstruction - Delta;
        }
        else
        {
            Reconstruction = Reconstruction + Delta;
        }

        //test to avoid signed 16 bit overflow
        if (Reconstruction > 32767)
        {
            Reconstruction = 32767;
        }
        else if (Reconstruction < -32768)
        {
            Reconstruction = -32768;
        }

        //load a new quantizer StepValue size based on current nibble
        Index = Index + IndexAdjustTable[Nibble];

        //test to avoid under or overflowing table
        if (Index < 0)
        {
            Index = 0;
        }
        if (Index > (N_STEP_VALUES - 1))
        {
            Index = (N_STEP_VALUES - 1);
        }

        //return an expanded signed 16 bit result
        return ((short) Reconstruction);
    }


}
