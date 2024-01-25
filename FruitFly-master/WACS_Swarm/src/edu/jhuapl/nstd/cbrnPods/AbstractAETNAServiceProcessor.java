package edu.jhuapl.nstd.cbrnPods;

import edu.jhuapl.nstd.swarm.util.Config;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import edu.jhuapl.spectra.AETNAServiceInterface2;

public abstract class AbstractAETNAServiceProcessor extends Thread
{
	/**
	 * AETNA processing service.
	 */
	protected AETNAServiceInterface2 aetna;

	/**
	 * AETNA processing classifier.
	 */
	protected String aetnaClassifier;

	/**
	 * AETNA processing classifier parameters.
	 */
	protected double a0, a1, a2, a3, eLow, aetnaGain;
	
    /**
     * True if we've successfully connected to AETNA service.
     * Defaults to false.
     */
	protected boolean m_aetnaConnected = false;
	
    /**
     * True while processing thread is running.
     * Defaults to false.
     */
	protected boolean m_Running = false;
        
        protected String m_AETNALibFolderBaseFolderName;
        
    public AbstractAETNAServiceProcessor ()
    {
        m_AETNALibFolderBaseFolderName = Config.getConfig().getProperty("AETNAService.BaseFolder", "C:\\Program Files\\JHUAPL\\AETNAService");
        if (!m_AETNALibFolderBaseFolderName.endsWith("\\") && !m_AETNALibFolderBaseFolderName.endsWith("/"))
            m_AETNALibFolderBaseFolderName += "\\";
    }
	
	/**
     * Subclass processing threads must implement the run method.
     */
    @Override
    public abstract void run();
    
    /**
     * Gracefully stop the processing thread
     */
    public void killThread()
    {
    	m_Running = false;
    }
    
	/**
	 * Access library settings from AETNAService for logging.
	 * 
	 * @throws Exception
	 */
	protected void getLibrarySettings() throws Exception
	{
		//Lib file used by AETNAservice
		String aetnaLibFile = m_AETNALibFolderBaseFolderName + "isotopeTemplateData\\" + aetnaClassifier + ".det";
		System.out.println("Reading AETNA file: " + aetnaLibFile);

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(aetnaLibFile));
			String lineIn = null;
			StringTokenizer st = null;
			String token = null;
			int counter = 0;

			while ((lineIn = br.readLine()) != null)
			{
				st = new StringTokenizer(lineIn, " \t\r");

				if (st.countTokens() < 2)
					throw new Exception("Invalid token count in AETNA library file: " + aetnaLibFile);

				token = st.nextToken();
				token = st.nextToken();

				//Readable tokens in file
				if (counter == 0)
					a0 = Double.parseDouble(token);
				else if (counter == 1)
					a1 = Double.parseDouble(token);
				else if (counter == 2)
					a2 = Double.parseDouble(token);
				else if (counter == 3)
					a3 = Double.parseDouble(token);
				else if (counter == 4)
					eLow = Double.parseDouble(token);
				else
					break; // ignore the rest of the library file

				counter++;
			}

			br.close();
		}
		catch (Exception e)
		{
			System.err.println("Failed to load AETNA library file: " + aetnaLibFile);
			throw e;
		}
		
		System.out.println("a0: " + a0 + " a1: " + a1 + " a2: " + a2 + " a3: " + a3 + " eLow: " + eLow);
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
			}
		}

		return ret;
	}

	/**
	 * Returns the AETNA processing classifier.
	 * 
	 * @return the aetnaClassifier
	 */
	public String getAETNAClassifier()
	{
		return aetnaClassifier;
	}

	/**
	 * Returns the AETNA processing parameter <i>a0</i>.
	 * 
	 * @return the a0
	 */
	public double getA0()
	{
		return a0;
	}

	/**
	 * Returns the AETNA processing parameter <i>a1</i>.
	 * 
	 * @return the a1
	 */
	public double getA1()
	{
		return a1;
	}

	/**
	 * Returns the AETNA processing parameter <i>a2</i>.
	 * 
	 * @return the a2
	 */
	public double getA2()
	{
		return a2;
	}

	/**
	 * Returns the AETNA processing parameter <i>a3</i>.
	 * 
	 * @return the a3
	 */
	public double getA3()
	{
		return a3;
	}

	/**
	 * Returns the AETNA processing parameter <i>eLow</i>.
	 * 
	 * @return the eLow
	 */
	public double getELow()
	{
		return eLow;
	}

	/**
	 * Returns the AETNA processing parameter <i>gain</i>.
	 * 
	 * @return the aetnaGain
	 */
	public double getAETNAGain()
	{
		return aetnaGain;
	}

	/**
	 * Sets the AETNA classifier.
	 * 
	 * @param aetnaClassifier 
	 * 				AETNA classifier name 
	 */
	public void setAetnaClassifier(String aetnaClassifier)
	{
		this.aetnaClassifier = aetnaClassifier;
	}

	/**
	 * Sets the AETNA processing parameter <i>a0</i>.
	 * 
	 * @param a0
	 *            the new <i>a0</i> parameter value
	 */
	public void setA0(double a0)
	{
		this.a0 = a0;
	}

	/**
	 * Sets the AETNA processing parameter <i>a1</i>.
	 * 
	 * @param a1
	 *            the new <i>a1</i> parameter value
	 */
	public void setA1(double a1)
	{
		this.a1 = a1;
	}

	/**
	 * Sets the AETNA processing parameter <i>a2</i>.
	 * 
	 * @param a2
	 *            the new <i>a2</i> parameter value
	 */
	public void setA2(double a2)
	{
		this.a2 = a2;
	}

	/**
	 * Sets the AETNA processing parameter <i>a3</i>.
	 * 
	 * @param a3
	 *            the new <i>a3</i> parameter value
	 */
	public void setA3(double a3)
	{
		this.a3 = a3;
	}

	/**
	 * Sets the AETNA processing parameter <i>eLow</i>.
	 * 
	 * @param eLow
	 *            the new <i>eLow</i> parameter value
	 */
	public void setELow(double eLow)
	{
		this.eLow = eLow;
	}

	/**
	 * Sets the AETNA processing parameter <i>gain</i>.
	 * 
	 * @param aetnaGain
	 *            the new <i>gain</i> parameter value
	 */
	public void setAetnaGain(double aetnaGain)
	{
		this.aetnaGain = aetnaGain;
	}
}
